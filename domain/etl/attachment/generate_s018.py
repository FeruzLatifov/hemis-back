#!/usr/bin/env python3
"""
generate_s018.py — reproducible generator for S018 (OTM<->speciality attachment, 2026-2027).

STRATEGY (2026-anchored, no year backfill):
  * The classifier's 2026 set is the ground truth. In 2026, each L3 (Yo'nalish) CODE is UNIQUE
    (236 Bakalavr + 598 Magistr), so a Kateg-1 plan row resolves by CODE+2026 alone — no name
    match, and the old same-code L3 (not in 2026) is automatically excluded (code-twin fixed).
  * A Kateg-2 (L4, Ichki yo'nalish) plan row resolves ONLY among the CHILDREN of the resolved
    2026 L3, by normalized name. If the profile is not a child of the 2026 L3 (renamed-away L4
    under an old L3, or a brand-new profile absent from the classifier), it is NOT attached —
    it is written to a "problematic" .xlsx for manual curation.
  * NO h_speciality_year backfill (S018b removed): we never link a speciality to a year. Only
    specialities already in 2026 are attached; an L4 inherits 2026 from its 2026 L3 parent.

Sources (tracked next to this script):
  Bakalavr_2026-2027.xlsx  (education_type 11, sheet "Kunduzgi")
  Magistratura_2026-2027.xlsx (education_type 12, sheet "Sheet1")
Classifier + years read from the checked-in seeds: S014+S017 (h_speciality), S015+S017 (years).
Deterministic: same inputs -> byte-identical S018 output (uuid5 ids, sorted rows).
"""
import openpyxl, re, uuid, os
from collections import defaultdict, Counter

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(HERE, "..", "..", ".."))
SEED = os.path.join(REPO, "domain/src/main/resources/db/changelog/changesets/seed")
DOCS = os.path.abspath(os.path.join(REPO, "..", "docs", "mutaxasisliklar", "otm biriktirish"))
SPEC = [os.path.join(SEED, "S014_seed_h_speciality.sql"),
        os.path.join(SEED, "S017_seed_h_speciality_2026.sql")]
YEARSRC = [os.path.join(SEED, "S015_seed_h_speciality_year.sql"),
           os.path.join(SEED, "S017_seed_h_speciality_2026.sql")]
XLSX = [("Bakalavr_2026-2027.xlsx", "Kunduzgi", "11"),
        ("Magistratura_2026-2027.xlsx", "Sheet1", "12")]
FORMS = [(6, "11"), (7, "12"), (8, "16")]                # Kunduzgi / Kechki / Masofa(viy)
NS = uuid.UUID("6f4a2b7e-0000-4000-8000-000000000018")   # stable uuid5 namespace for S018
CB = "seed:S018-2026"
ASSIGN_YEAR = 2026                                       # every attachment's own edu_year
OUT_SEED = os.path.join(SEED, "S018_seed_speciality_attachment_2026.sql")
OUT_RB   = os.path.join(SEED, "S018_seed_speciality_attachment_2026_rollback.sql")
OUT_XLSX = os.path.join(DOCS, "muammoli_L4_profillar_2026.xlsx")

# ---------- minimal SQL VALUES parser ----------
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
def norm(t):   # full-name normalizer: strip apostrophes, ':' '(' ')', lower, collapse spaces
    t=t or ''
    for ch in "'’ʻʼ‘`": t=t.replace(ch,' ')
    t=t.lower().replace(':',' ').replace('(',' ').replace(')',' ')
    return re.sub(r'\s+',' ',t).strip()
def prof(t):   # ':' dan keyingi profil qismi (L4 uchun)
    return norm(t.split(':',1)[1]) if ':' in t else norm(t)

# ---------- load classifier ----------
# spec[sid] = (code, edu, level, name);  children[parent_sid] = [(edu, idx)]
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
            spec[sid]=(code, edu, hl, nm); id2parent[sid]=pid
            idx=len(CL[edu]); CL[edu].append((code, sid, hl, nm))
            if code: byCode[(edu,code)].append(idx)
children=defaultdict(list)
for _edu in CL:
    for _idx,(c,sid,hl,nm) in enumerate(CL[_edu]):
        pp=id2parent.get(sid)
        if pp: children[pp].append((_edu,_idx))

# ---------- load years -> 2026 set ----------
HAS2026=set()
for p in YEARSRC:
    for blk in re.finditer(r"INSERT INTO h_speciality_year[^;]*;", open(p,encoding='utf-8').read(), re.S):
        for m in re.finditer(r"\('([0-9a-fA-F-]{36})'\s*,\s*(\d{4})\)", blk.group(0)):
            if int(m.group(2))==2026: HAS2026.add(m.group(1))

# 2026 L3 by (edu, code) — UNIQUE (verified: 236 Bak + 598 Mag, 0 dup)
l3_2026 = {}
for edu in CL:
    for (c,sid,hl,nm) in CL[edu]:
        if hl==3 and sid in HAS2026 and c:
            l3_2026[(edu,c)] = sid        # unique — last wins, but there is only one

def resolve_l3(edu, code):
    return l3_2026.get((edu, code))       # unique 2026 L3 for this code (or None)

def resolve_l4(edu, l3_sid, name):
    """Match a Kateg-2 profile among the 2026 L3's OWN L4 children, by normalized name."""
    pf=norm(name); pp=prof(name); best=None
    for (e,idx) in children.get(l3_sid, ()):
        if e!=edu: continue
        c,sid,hl,nm = CL[edu][idx]
        if hl!=4: continue
        cn=norm(nm)
        if cn==pf: return sid
        if pp and (cn.endswith(pp) or pp in cn): best=sid
    return best

# ---------- load xlsx ----------
def load(fn, sheet, edu):
    wb=openpyxl.load_workbook(os.path.join(HERE, fn), read_only=True, data_only=True); ws=wb[sheet]
    out=[]
    for r in ws.iter_rows(min_row=2, values_only=True):
        shifr=str(r[3]).strip() if r[3] is not None else ''
        if not re.fullmatch(r"\d{8}", shifr): continue     # skip OTM header rows (shifr=1..N)
        otmid=str(r[1]).strip() if r[1] is not None else ''
        kateg=str(r[2]).strip() if r[2] is not None else ''
        name=str(r[4] or '').strip()
        forms=[c for (ci,c) in FORMS if (r[ci] is not None and str(r[ci]).strip()!='')]
        out.append((edu, otmid, kateg, shifr, name, forms))
    wb.close(); return out
rows=[]
for fn,sheet,edu in XLSX: rows+=load(fn, sheet, edu)

# ---------- resolve ----------
attach=set()                 # (otmid, sid, form)
prob=[]                      # problematic Kateg-2 rows
cur_l3=None; cur_code=None; cur_otm=None
cnt=Counter()
for edu,otmid,kateg,shifr,name,forms in rows:
    if kateg=='1':
        sid=resolve_l3(edu, shifr)
        cur_l3, cur_code, cur_otm = sid, shifr, otmid
        if sid is None:
            cnt['L3-2026-YOQ']+=1; prob.append((edu,otmid,'L3',shifr,name,'L3 2026-da yoq',''))
            continue
        cnt['L3-ok']+=1
        for fc in forms: attach.add((otmid, sid, fc))
    elif kateg=='2':
        sid = resolve_l4(edu, cur_l3, name) if (cur_l3 is not None and otmid==cur_otm) else None
        if sid is not None:
            cnt['L4-ok']+=1
            for fc in forms: attach.add((otmid, sid, fc))
        else:
            # categorize: profil klassifikatorда bor (boshqa L3 da) yoki umuman yo'q
            l4_same=[CL[edu][i] for i in byCode.get((edu,shifr),[]) if CL[edu][i][2]==4]
            # EXACT name match (normalized) among the code's L4 -> the profile genuinely exists,
            # only under an old (non-2026) yo'nalish. If it is NOT an exact match we do NOT suggest an
            # approximate one — the classifier column simply reads "mavjud emas".
            name_hit=sorted({x[3] for x in l4_same if norm(x[3])==norm(name)})
            if name_hit:
                cat="Profil klassifikatorda bor, lekin 2026-yildagi yo'nalish ostida emas (eski yo'nalishga tegishli, nomi o'zgartirilgan bo'lishi mumkin)"
                cls="; ".join(name_hit)                          # full name(s), no truncation
            elif l4_same:
                cat="Bu yo'nalish kodida klassifikatorda ichki yo'nalishlar bor, ammo aynan shu profil (nomi) mavjud emas"
                cls="mavjud emas"
            else:
                cat="Bu yo'nalish kodida klassifikatorda birorta ham ichki yo'nalish yo'q"
                cls="mavjud emas"
            cnt['L4-problem']+=1
            prob.append((edu,otmid,'L4',shifr,name,cat,cls))

print("resolution:", dict(sorted(cnt.items())))
rows_out=[]
for uc,sid,ef in sorted(attach):
    rid=str(uuid.uuid5(NS, f"{uc}|{sid}|{ef}|{ASSIGN_YEAR}"))
    rows_out.append((rid, uc, sid, ef))
print(f"attachment rows (dedup): {len(rows_out)}   forms:", dict(Counter(r[3] for r in rows_out)),
      "  OTM:", len({r[1] for r in rows_out}))
# distinct problematic L4 profiles
prob_distinct = sorted({(e,c,n,cat,cand) for (e,ot,lv,c,n,cat,cand) in prob if lv=='L4'})
prob_otmcnt = Counter((e,c,n) for (e,ot,lv,c,n,cat,cand) in prob if lv=='L4')
print(f"problematic L4 (distinct profil): {len(prob_distinct)}   (jami reja qatori: {sum(1 for x in prob if x[2]=='L4')})")

# ---------- emit S018 ----------
def esc(s): return s.replace("'","''")
HDR="""-- =====================================================
-- S018: SEED SPECIALITY -> OTM ATTACHMENT (2026-2027)
-- =====================================================
-- Author: hemis-team
-- Purpose: Load the 2026-2027 ministry OTM<->speciality assignments into
--          university_speciality_attachment (V019).
-- Resolution (2026-anchored, no year backfill):
--   L3 (Kateg-1): resolved by CODE + 2026 year-link -> the UNIQUE 2026 Yo'nalish (in 2026 every
--                 L3 code is unique, so the old same-code L3 is auto-excluded; code-twin solved).
--   L4 (Kateg-2): resolved ONLY among the 2026 L3's OWN children, by name. A profile that is not
--                 a child of the 2026 L3 (renamed-away or brand-new) is NOT attached here — it is
--                 exported to docs/.../muammoli_L4_profillar_2026.xlsx for manual curation.
--   NO h_speciality_year backfill: only specialities already in 2026 are attached.
-- Year:    every attachment's own edu_year = 2026 (FK h_education_year.year); this does NOT modify
--          the classifier's h_speciality_year.
-- Fan-out: one row per set education_form column (Kunduzgi=11, Kechki=12, Masofa=16).
-- Keying:  id = uuid5(ns, university_code|speciality_id|education_form|edu_year). status='ACTIVE'.
-- Idempotent: ON CONFLICT (university_code, speciality_id, education_form, edu_year) WHERE deleted_at IS NULL.
-- Generated by domain/etl/attachment/generate_s018.py. DO NOT hand-edit.
-- =====================================================
"""
BATCH=500; lines=[HDR]
for i in range(0,len(rows_out),BATCH):
    chunk=rows_out[i:i+BATCH]
    lines.append("INSERT INTO university_speciality_attachment (id, university_code, speciality_id, education_form, edu_year, status, created_by) VALUES")
    lines.append(",\n".join(f"  ('{rid}', '{esc(uc)}', '{sid}', '{ef}', {ASSIGN_YEAR}, 'ACTIVE', '{CB}')" for rid,uc,sid,ef in chunk))
    lines.append("ON CONFLICT (university_code, speciality_id, education_form, edu_year) WHERE deleted_at IS NULL DO NOTHING;\n")
open(OUT_SEED,"w").write("\n".join(lines))
open(OUT_RB,"w").write(
    "-- =====================================================\n"
    "-- S018 ROLLBACK: remove the 2026-2027 seeded OTM<->speciality attachments.\n"
    "-- Targets only seed-provenance rows (created_by tag); user-created attachments untouched.\n"
    "-- =====================================================\n"
    f"DELETE FROM university_speciality_attachment WHERE created_by = '{CB}';\n")
print("wrote:", OUT_SEED)

# ---------- emit problematic L4 as .xlsx (full text, no truncation) ----------
from openpyxl.styles import Alignment, Font, PatternFill
wb=openpyxl.Workbook(); ws=wb.active
assert ws is not None
ws.title="Muammoli L4 2026"
headers=[
    "Dastur",                                  # Bakalavr / Magistr
    "Kod",                                     # 8-xonali yo'nalish kodi (profil shu kodni ulashadi)
    "Reja (exel) dagi ichki yo'nalish nomi",   # vazirlik so'ragan L4 profil, to'liq
    "Nima uchun biriktirilmadi (sabab)",       # to'liq izoh
    "Klassifikatorda topilgan nom",            # aynan mos nom, yoki "mavjud emas"
    "Nechta OTM so'ragan",                     # ustuvorlik
]
ws.append(headers)
edulbl={'11':'Bakalavr','12':'Magistr'}
for (e,c,n,cat,cand) in prob_distinct:
    ws.append([edulbl.get(e,e), c, n, cat, cand, prob_otmcnt.get((e,c,n),0)])
# header style
hf=Font(bold=True, color="FFFFFF"); hfill=PatternFill("solid", fgColor="305496")
for cell in ws[1]:
    cell.font=hf; cell.fill=hfill
    cell.alignment=Alignment(horizontal="center", vertical="center", wrap_text=True)
# wide columns + wrap so nothing is cut off
for col,w in zip("ABCDEF",[11,13,58,62,52,16]): ws.column_dimensions[col].width=w
for row in ws.iter_rows(min_row=2):
    for cell in row:
        cell.alignment=Alignment(vertical="top", wrap_text=True)
ws.freeze_panes="A2"
wb.save(OUT_XLSX)
print("wrote:", OUT_XLSX, f"({len(prob_distinct)} qator)")
