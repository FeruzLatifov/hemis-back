#!/usr/bin/env python3
"""
reconcile_s018.py — INDEPENDENT audit: does EVERY row of the two OTM request xlsx
get attached? Replays generate_s018's load+resolve, but captures the disposition of
*every physical row* (not just the ones the generator counts), so silent drops surface:

  A. header/group rows        (shifr not 8-digit)     -> not a request, OK
  B. data row, no form flag   (Kunduzgi/Kechki/Masofa all empty) -> resolves but 0 attachment
  C. data row, L3 code not in 2026 classifier         -> dropped (unresolved)
  D. data row, L4 profile not resolvable              -> dropped (problematic)
  E. data row, resolved, >=1 form                     -> attached
  F. malformed shifr (present, digits, but not 8 and not a small header index)

Then cross-checks the resulting (otmid,sid,form) set against the live DB.
"""
import openpyxl, re, os
from collections import defaultdict, Counter

HERE=os.path.dirname(os.path.abspath(__file__))
REPO=os.path.abspath(os.path.join(HERE,"..","..",".."))
SEED=os.path.join(REPO,"domain/src/main/resources/db/changelog/changesets/seed")
SPEC=[os.path.join(SEED,"S014_seed_h_speciality.sql"),os.path.join(SEED,"S017_seed_h_speciality_2026.sql")]
YEARSRC=[os.path.join(SEED,"S015_seed_h_speciality_year.sql"),os.path.join(SEED,"S017_seed_h_speciality_2026.sql")]
XLSX=[("Bakalavr_2026-2027.xlsx","Kunduzgi","11"),("Magistratura_2026-2027.xlsx","Sheet1","12")]
FORMS=[(6,"11"),(7,"12"),(8,"16")]

# ---- reuse generator's parsers (copied verbatim) ----
def parse_tuples(s):
    out=[];i=0;n=len(s);d=0;q=False;c=''
    while i<n:
        ch=s[i]
        if q:
            if ch=="'":
                if i+1<n and s[i+1]=="'": c+="''";i+=2;continue
                q=False;c+=ch;i+=1;continue
            c+=ch;i+=1;continue
        if ch=="'": q=True;c+=ch;i+=1;continue
        if ch=='(':
            d+=1
            if d==1: c='';i+=1;continue
            c+=ch;i+=1;continue
        if ch==')':
            d-=1
            if d==0: out.append(c);i+=1;continue
            c+=ch;i+=1;continue
        if d>=1: c+=ch
        i+=1
    return out
def split_fields(t):
    f=[];i=0;n=len(t);q=False;c=''
    while i<n:
        ch=t[i]
        if q:
            if ch=="'":
                if i+1<n and t[i+1]=="'": c+="''";i+=2;continue
                q=False;c+=ch;i+=1;continue
            c+=ch;i+=1;continue
        if ch=="'": q=True;c+=ch;i+=1;continue
        if ch==',': f.append(c.strip());c='';i+=1;continue
        c+=ch;i+=1
    f.append(c.strip());return f
def unq(v):
    v=v.strip()
    if v=='NULL': return None
    if v.startswith("'") and v.endswith("'"): return v[1:-1].replace("''","'")
    return v
def norm(t):
    t=t or ''
    for ch in "'’ʻʼ‘`": t=t.replace(ch,' ')
    t=t.lower().replace(':',' ').replace('(',' ').replace(')',' ')
    return re.sub(r'\s+',' ',t).strip()
def prof(t): return norm(t.split(':',1)[1]) if ':' in t else norm(t)

# ---- classifier ----
CL=defaultdict(list); byCode=defaultdict(list); id2parent={}; spec={}
for p in SPEC:
    txt=open(p,encoding='utf-8').read()
    for mm in re.finditer(r"INSERT INTO h_speciality \(([^)]*)\) VALUES(.*?)(?:ON CONFLICT|;\s*$)", txt, re.S|re.M):
        cols=[c.strip() for c in mm.group(1).split(",")]
        for tup in parse_tuples(mm.group(2)):
            r=dict(zip(cols,[unq(x) for x in split_fields(tup)]))
            edu=r.get('education_type'); code=r.get('code'); nm=r.get('name_uz')
            if edu not in ('11','12') or not nm: continue
            hl=r.get('hierarchy_level'); hl=int(hl) if hl and str(hl).isdigit() else None
            sid=r.get('id'); pid=r.get('parent_id')
            spec[sid]=(code,edu,hl,nm); id2parent[sid]=pid
            idx=len(CL[edu]); CL[edu].append((code,sid,hl,nm))
            if code: byCode[(edu,code)].append(idx)
children=defaultdict(list)
for _edu in CL:
    for _idx,(c,sid,hl,nm) in enumerate(CL[_edu]):
        pp=id2parent.get(sid)
        if pp: children[pp].append((_edu,_idx))
HAS2026=set()
for p in YEARSRC:
    for blk in re.finditer(r"INSERT INTO h_speciality_year[^;]*;", open(p,encoding='utf-8').read(), re.S):
        for m in re.finditer(r"\('([0-9a-fA-F-]{36})'\s*,\s*(\d{4})\)", blk.group(0)):
            if int(m.group(2))==2026: HAS2026.add(m.group(1))
l3_2026={}
for edu in CL:
    for (c,sid,hl,nm) in CL[edu]:
        if hl==3 and sid in HAS2026 and c: l3_2026[(edu,c)]=sid
def resolve_l3(edu,code): return l3_2026.get((edu,code))
def resolve_l4(edu,l3_sid,name):
    pf=norm(name); pp=prof(name); best=None
    for (e,idx) in children.get(l3_sid,()):
        if e!=edu: continue
        c,sid,hl,nm=CL[edu][idx]
        if hl!=4: continue
        cn=norm(nm)
        if cn==pf: return sid
        if pp and (cn.endswith(pp) or pp in cn): best=sid
    return best
import json as _json
_mapf=os.path.join(HERE,"muammoli_l4_2026_map.json")
MMAP=_json.load(open(_mapf,encoding='utf-8')) if os.path.exists(_mapf) else {}

# ---- walk EVERY physical row of both request sheets ----
disp=Counter()
attach=set()
noform=[]; unl3=[]; unl4=[]; malformed=[]
per_otm_req=defaultdict(set)   # otmid -> set of resolved sid (for coverage)
total_phys=0
cur_l3=cur_otm=None
for fn,sheet,edu in XLSX:
    wb=openpyxl.load_workbook(os.path.join(HERE,fn),read_only=True,data_only=True); ws=wb[sheet]
    for r in ws.iter_rows(min_row=2, values_only=True):
        total_phys+=1
        shifr=str(r[3]).strip() if r[3] is not None else ''
        otmid=str(r[1]).strip() if r[1] is not None else ''
        kateg=str(r[2]).strip() if r[2] is not None else ''
        name=str(r[4] or '').strip()
        forms=[c for (ci,c) in FORMS if (r[ci] is not None and str(r[ci]).strip()!='')]
        if not re.fullmatch(r"\d{8}", shifr):
            # header/group row OR malformed. small integer 1..999 or blank = header.
            if shifr=='' or re.fullmatch(r"\d{1,3}", shifr):
                disp['A_header']+=1
            else:
                disp['F_malformed']+=1; malformed.append((edu,otmid,kateg,shifr,name))
            continue
        # data row
        if kateg=='1':
            sid=resolve_l3(edu,shifr); cur_l3,cur_otm=sid,otmid
            if sid is None:
                disp['C_unresolved_L3']+=1; unl3.append((edu,otmid,shifr,name)); continue
        elif kateg=='2':
            sid=MMAP.get(f"{edu}|{shifr}|{norm(name)}")
            if sid is None:
                sid=resolve_l4(edu,cur_l3,name) if (cur_l3 is not None and otmid==cur_otm) else None
            if sid is None:
                disp['D_unresolved_L4']+=1; unl4.append((edu,otmid,shifr,name)); continue
        else:
            disp['C_unresolved_L3']+=1; unl3.append((edu,otmid,shifr,'KATEG?='+kateg)); continue
        # resolved
        if not forms:
            disp['B_resolved_noform']+=1; noform.append((edu,otmid,kateg,shifr,name));
            per_otm_req[otmid].add(sid); continue
        disp['E_attached']+=1
        per_otm_req[otmid].add(sid)
        for fc in forms: attach.add((otmid,sid,fc))
    wb.close()

print("========== HAR BIR FIZIK QATOR TAQDIRI ==========")
print("jami fizik qator (2 fayl, sarlavhasiz):", total_phys)
for k in ['A_header','E_attached','B_resolved_noform','C_unresolved_L3','D_unresolved_L4','F_malformed']:
    print(f"  {k:24}: {disp.get(k,0)}")
print("data qatorlar (A va F dan tashqari):", total_phys-disp.get('A_header',0)-disp.get('F_malformed',0))
print("biriktirish (otmid,sid,form) unikal:", len(attach))
print("biriktirilgan OTM:", len({a[0] for a in attach}))

def show(title,rows,lim=25):
    print(f"\n--- {title}: {len(rows)} ---")
    for x in rows[:lim]: print("   ",x)
    if len(rows)>lim: print(f"    ... +{len(rows)-lim} more")
show("B: hal bo'ldi-yu FORMA belgilanmagan (0 biriktirish)", noform)
show("C: L3 kodi 2026 klassifikatorda YO'Q (tashlangan)", unl3)
show("D: L4 profil hal bo'lmadi (tashlangan)", unl4)
show("F: shifr buzuq (8-xonali emas, sarlavha ham emas)", malformed)

# ---- write attach set for DB cross-check ----
with open(os.path.join(HERE,"_recon_attach.txt"),"w") as f:
    for uc,sid,ef in sorted(attach): f.write(f"{uc}|{sid}|{ef}\n")
print("\nwrote _recon_attach.txt for DB diff")
