#!/usr/bin/env python3
"""
Muammoli L4 2026 fix -> S017 (klassifikator) + aniq ID-xarita (generate_s018 uchun).

Manba: muammoli_L4_2026_reviewed.xlsx (ko'rib chiqilgani-muammoli_L4_2026.xlsx nusxasi).
Reviewer izohi (E ustuni) — 3 amal, ISHONCHLI sibling-direction matcher bilan tasdiqlangan:
  * "янги қўшиш"      -> YARAT   : bazada yo'q -> yangi L4 (2026 L3 ostiga) + 2026 yil
  * "шу отага ўтказиш"-> KO'CHIR : bazada bor (eski otada) -> parent_id ni 2026 L3 ga UPDATE (+2026)
  * "отаси тўғри..."  -> BIRIKTIR: bazada bor (2026 L3 ostida, imlo variant) -> KLASSIFIKATOR TEGILMAYDI

Muhim:
  - Matcher = profil-qismi (>=0.80) VA ota-yo'nalish nomi (>=0.70). Butun klassifikator (har kod).
  - RE-PARENT faqat parent_id ni o'zgartiradi (NOM/KOD saqlanadi -> uq o'zgармайди, imlo-to'qnashuv yo'q).
    Baza imlosi TO'G'RI (reviewer: "imloviy xato to'g'risi bazada"); reja nomi variant.
  - Reja-nomi(variant) -> mavjud sid moslashini generate_s018 ANIQ XARITA orqali biladi (fuzzy EMAS):
    muammoli_l4_2026_map.json = { "edu|code|norm(reja_nomi)": speciality_id }.
Chiqish:
  - S017 oxiriga MARKER-blok: INSERT(yarat) + UPDATE parent_id(ko'chir) + h_speciality_year(2026).
  - muammoli_l4_2026_map.json (generate_s018 o'qiydi).
"""
import re, os, uuid, json, difflib, openpyxl
from collections import defaultdict

HERE = os.path.dirname(os.path.abspath(__file__))
SEED = os.path.normpath(os.path.join(HERE, '..', '..', 'src', 'main', 'resources', 'db', 'changelog', 'changesets', 'seed'))
S017 = os.path.join(SEED, 'S017_seed_h_speciality_2026.sql')
S017_RB = os.path.join(SEED, 'S017_seed_h_speciality_2026_rollback.sql')
REVIEWED = os.path.join(HERE, 'muammoli_L4_2026_reviewed.xlsx')
MAP_OUT = os.path.join(HERE, 'muammoli_l4_2026_map.json')
NS = uuid.UUID('6f9619ff-8b86-d011-b42d-00cf4fc964ff')
MARK = '-- >>> MUAMMOLI-L4-2026-FIX >>>'
ENDMARK = '-- <<< MUAMMOLI-L4-2026-FIX <<<'

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
    return v[1:-1].replace("''","'") if v.startswith("'") and v.endswith("'") else v
# generate_s018.py bilan BIR XIL norm (uq/xarita kaliti uchun): apostrof+':'+'()'-> space, lower, collapse
def norm(t):
    t=t or ''
    for ch in "'’ʻʼ‘`": t=t.replace(ch,' ')
    return re.sub(r'\s+',' ',t.lower().replace(':',' ').replace('(',' ').replace(')',' ')).strip()
# fold = h_speciality_fold (V018): faqat apostrof->space, lower, collapse (':' '()' STRIP EMAS)
def fold(t):
    t=''.join(' ' if ch in "'’ʻʼ‘`" else ch for ch in (t or ''))
    return ' '.join(t.lower().split())
# matcher yordamchilari (',' '-' ni ham strip -> qattiqroq profil/dir taqqoslash)
def mnorm(t):
    t=t or ''
    for ch in "'’ʻʼ‘`":t=t.replace(ch,' ')
    return re.sub(r'\s+',' ',t.lower().replace(':',' ').replace('(',' ').replace(')',' ').replace(',',' ').replace('-',' ')).strip()
def profpart(name):
    if ':' in name: p=name.split(':',1)[1]
    elif '(' in name and ')' in name: p=name[name.find('(')+1:name.rfind(')')]
    else: p=name
    return mnorm(p)
def dirpart(name):
    if ':' in name: return mnorm(name.split(':',1)[0])
    if '(' in name: return mnorm(name[:name.find('(')])
    return mnorm(name)
def sim(a,b): return difflib.SequenceMatcher(None,a,b).ratio()

def strip_block(txt):
    return re.sub(re.escape(MARK)+r".*?"+re.escape(ENDMARK)+r"\n?", "", txt, flags=re.S).rstrip()+"\n"
def sql(v): return 'NULL' if v is None else "'"+str(v).replace("'","''")+"'"

def main():
    base_s017 = strip_block(open(S017,encoding='utf-8').read())
    S014txt = open(os.path.join(SEED,'S014_seed_h_speciality.sql'),encoding='utf-8').read()
    spec={}; id2parent={}
    for txt in [S014txt, base_s017]:
        for mm in re.finditer(r"INSERT INTO h_speciality \(([^)]*)\) VALUES(.*?)(?:ON CONFLICT|;\s*$)", txt, re.S|re.M):
            cols=[c.strip() for c in mm.group(1).split(",")]
            for tup in parse_tuples(mm.group(2)):
                r=dict(zip(cols,[unq(x) for x in split_fields(tup)]))
                edu=r.get('education_type'); code=r.get('code'); nm=r.get('name_uz')
                if edu not in ('11','12') or not nm: continue
                hl=r.get('hierarchy_level'); hl=int(hl) if hl and str(hl).isdigit() else None
                spec[r.get('id')]=dict(edu=edu,code=code,hl=hl,nm=nm)
                id2parent[r.get('id')]=r.get('parent_id')
    HAS2026=set()
    for src in [open(os.path.join(SEED,'S015_seed_h_speciality_year.sql'),encoding='utf-8').read(), base_s017]:
        for blk in re.finditer(r"INSERT INTO h_speciality_year[^;]*;", src, re.S):
            for m in re.finditer(r"\('([0-9a-fA-F-]{36})'\s*,\s*(\d{4})\)", blk.group(0)):
                if int(m.group(2))==2026: HAS2026.add(m.group(1))
    l3_2026={}
    for sid,d in spec.items():
        if d['hl']==3 and sid in HAS2026 and d['code']: l3_2026[(d['edu'],d['code'])]=sid
    # L4 indeks: (sid, edu, profpart, parent-dirpart)
    L4idx=[]
    for sid,d in spec.items():
        if d['hl']==4:
            par=id2parent.get(sid); pn=spec.get(par,{}).get('nm','')
            L4idx.append((sid, d['edu'], profpart(d['nm']), dirpart(pn) or dirpart(d['nm'])))
    # identity (uq) index
    ident={(d['edu'],d['code'],fold(d['nm'])):sid for sid,d in spec.items()}

    # token-moslik: "tili" generic so'zni tashlab, distinktiv tokenlar SONI teng va har biri imlo-mos
    # (koreys~Kores, pushtu==pushtu). "arab tili"({arab}) vs "arab adabiyoti"({arab,adabiyoti}) -> son
    # teng emas -> MOS EMAS (soxta signal yo'q).
    STOP={'tili'}
    def toks(s): return [t for t in s.split() if t not in STOP]
    def tokmatch(a,b):
        A,B=toks(a),toks(b)
        if not A or not B or len(A)!=len(B): return False
        used=[False]*len(B)
        for x in A:
            hit=False
            for j,y in enumerate(B):
                if not used[j] and sim(x,y)>=0.85: used[j]=True; hit=True; break
            if not hit: return False
        return True
    def find_existing(edu,code,name,prefer_child_of=None):
        # Reviewer izohida KOD aniq berilgan -> bir xil nomli egizaklarni KOD bilan ajratamiz.
        # Ustuvorlik (tuple, desc): prefer_child_of > (kod==Kod VA profil-token-mos) > profil-ball > ota-ball.
        # Kod faqat profil GENUINE (tokmatch yoki ps>=0.92) mos bo'lsagina afzal — "rus~fors adabiyoti"
        # kabi umumiy-so'z (adabiyoti) soxta signalini oldini oladi (u yerda kod-mos egizak profil emas).
        l3=l3_2026.get((edu,code)); l3dir=dirpart(spec.get(l3,{}).get('nm',''))
        pf=profpart(name); cand=[]
        for sid,e,cpf,cdir in L4idx:
            if e!=edu: continue
            ds=sim(l3dir,cdir)
            if ds<0.70: continue
            tm=tokmatch(pf,cpf); ps=sim(pf,cpf)
            if ps<0.80 and not tm: continue
            if tm: ps=max(ps,0.90)
            code_ok=1 if (spec.get(sid,{}).get('code','')[:6]==(code or '')[:6] and (tm or ps>=0.92)) else 0
            prefer=1 if (prefer_child_of is not None and id2parent.get(sid)==prefer_child_of) else 0
            cand.append((prefer, code_ok, round(ps,2), round(ds,2), sid))
        if not cand: return None
        cand.sort(reverse=True)
        return cand[0][4]

    # AVTORITATIV izoh-A resolver: 2_Bakalavr/3_Magistr (docs) — KOD-DIREKSIYA (birinchi 6) + PROFIL o'xshashligi
    # bo'yicha 6/7-egizakni topadi. 5* (eski) egizak KO'CHIRILMAYDI. Fuzzy find_existing'dan ishonchliroq.
    XLS=os.path.normpath(os.path.join(HERE,'..','..','..','..','docs','mutaxasisliklar'))
    _byd=defaultdict(list)
    for _f,_edu in [("2_Bakalavr.xlsx",'11'),("3_Magistr.xlsx",'12')]:
        _w=openpyxl.load_workbook(os.path.join(XLS,_f),read_only=True,data_only=True)
        for _r in list(_w.active.iter_rows(values_only=True))[1:]:
            if _r[0] is None or str(_r[7])!='4': continue
            _c=str(_r[1]).strip()
            _byd[(_edu,_c[:6])].append((_c, str(_r[0]).strip(), profpart(_r[2] or '')))
        _w.close()
    _used67=set()
    def resolve_67(edu,code,name):
        pf=profpart(name); best=None; bs=-1
        for c,cid,cpf in sorted(_byd.get((edu,(code or '')[:6]),[]), key=lambda x:x[0]):
            if c[:1] not in ('6','7') or cid not in spec: continue   # faqat 6/7 + bizning klassifikatorda bor
            s=sim(pf,cpf)
            if s>bs and cid not in _used67: bs=s; best=cid
        if best: _used67.add(best)
        return best

    ws=openpyxl.load_workbook(REVIEWED,data_only=True).active
    xr=list(ws.iter_rows(values_only=True))[1:]
    EDU={'Bakalavr':'11','Magistr':'12'}
    A="Мавжуд, шу отага ўтказиш"; B="Шу отага янги қўшиш"; C="Отаси тўғри турибди, ўзгармайди"
    # 2026 L3 otasining TO'LIQ yil to'plami — ko'chirilgan L4 shu yillarni oladi (foydalanuvchi: {2024,2026}).
    years_of=defaultdict(set)
    for src in [S014txt, open(os.path.join(SEED,'S015_seed_h_speciality_year.sql'),encoding='utf-8').read(), base_s017]:
        for blk in re.finditer(r"INSERT INTO h_speciality_year[^;]*;", src, re.S):
            for m in re.finditer(r"\('([0-9a-fA-F-]{36})'\s*,\s*(\d{4})\)", blk.group(0)):
                years_of[m.group(1)].add(int(m.group(2)))

    inserts=[]; reparents=[]; year_ids=[]; reparent_year=[]; mapping={}; flagged=[]
    n_create=n_reparent=n_attach=n_general=0
    # AMAL = reviewer IZOHi (matcher faqat mavjudni TOPADI, qayta tasniflAMAYDI):
    #   yangi  -> YARAT (uq to'qnashuvi bo'lsa mavjudga xarita)
    #   o'tkaz -> mavjudni topib 2026 L3 ga RE-PARENT
    #   tegilm -> mavjudni topib XARITA (klassifikator O'ZGARMAYDI)
    for r in xr:
        edu=EDU[r[0]]; code=str(r[1]); name=r[2].strip(); izoh=r[4]
        l3=l3_2026.get((edu,code)); key=f"{edu}|{code}|{norm(name)}"
        if izoh==B:                                    # YANGI QO'SHISH
            ik=(edu,code,fold(name))
            if ik in ident:                            # (edu,code,fold) bor (L4 nomi=L3 nomi = umumiy) -> xarita
                mapping[key]=ident[ik]; n_general+=1; continue
            nid=str(uuid.uuid5(NS,f"muammoli-l4-2026|{edu}|{code}|{norm(name)}"))
            inserts.append((nid,code,name,edu,l3)); ident[ik]=nid
            year_ids.append(nid); mapping[key]=nid; n_create+=1
        elif izoh==A:                                  # "Мавжуд, шу отага ўтказиш" -> 2_Bakalavr'dan 6/7-egizakni
            sid=resolve_67(edu,code,name)              # reviewed "Ota" (2026 L3) ga KO'CHIR. 5* (eski) egizak tegilmaydi.
            if sid:
                if id2parent.get(sid)!=l3:
                    reparents.append((sid,l3)); id2parent[sid]=l3; n_reparent+=1
                else:
                    n_attach+=1                        # allaqachon 2026 L3 ostida
                reparent_year.append((sid,l3)); mapping[key]=sid   # yil -> yangi 2026 L3 otasi yillari (almashtirish)
            else:
                flagged.append(('o\'tkazish-topilmadi',edu,code,name)); n_create+=1  # fallback: yarat
                nid=str(uuid.uuid5(NS,f"muammoli-l4-2026|{edu}|{code}|{norm(name)}"))
                inserts.append((nid,code,name,edu,l3)); year_ids.append(nid); mapping[key]=nid
        else:                                          # OTASI TO'G'RI TURIBDI, O'ZGARMAYDI -> attach, no change
            sid=find_existing(edu,code,name,prefer_child_of=l3)  # faqat 2026 L3 bolasini biriktir (eski egizak emas)
            if sid:
                mapping[key]=sid; n_attach+=1          # klassifikator TEGILMAYDI
            else:
                flagged.append(('tegilmaydi-topilmadi',edu,code,name)); n_create+=1
                nid=str(uuid.uuid5(NS,f"muammoli-l4-2026|{edu}|{code}|{norm(name)}"))
                inserts.append((nid,code,name,edu,l3)); year_ids.append(nid); mapping[key]=nid
    year_ids=list(dict.fromkeys(year_ids))

    # ---- S017 blok ----
    cols="id,code,name_uz,name_oz,name_ru,name_en,education_type,review_status,parent_id,hierarchy_level,active,is_checked,version"
    L=[MARK,
       "-- Manba: muammoli_L4_2026_reviewed.xlsx (reviewer izohi) + ishonchli sibling-direction matcher.",
       f"--   YARAT(yangi L4): {n_create}  ·  KO'CHIR(re-parent): {n_reparent}  ·  BIRIKTIR(2026 L3'da bor): {n_attach}  ·  umumiy=L3: {n_general}",
       "--   RE-PARENT faqat parent_id (nom/kod SAQLANADI -> uq o'zgармайди). Reja-nomi -> mavjud sid",
       "--   moslashini generate_s018 aniq xarita (muammoli_l4_2026_map.json) orqali biladi (fuzzy EMAS).",
       f"INSERT INTO h_speciality ({cols}) VALUES"]
    L.append(",\n".join(
        f"  ({sql(nid)},{sql(code)},{sql(name)},NULL,NULL,NULL,{sql(edu)},'APPROVED',{sql(l3)},4,true,false,1)"
        for (nid,code,name,edu,l3) in inserts) + "\nON CONFLICT (id) DO NOTHING;")
    L.append("-- Re-parent: mavjud L4 -> 2026 L3 (faqat parent_id, nom o'zgармайди):")
    for sid,l3 in reparents:
        L.append(f"UPDATE h_speciality SET parent_id={sql(l3)}, updated_at=CURRENT_TIMESTAMP, "
                 f"updated_by='seed:S017-muammoli-l4' WHERE id={sql(sid)};")
    # Yil (yaratilgan izoh-B L4): 2026.
    if year_ids:
        L.append("-- Yil (yaratilgan L4): 2026")
        L.append("INSERT INTO h_speciality_year (speciality_id, year) VALUES")
        L.append(",\n".join(f"  ({sql(i)}, 2026)" for i in year_ids) + "\nON CONFLICT (speciality_id, year) DO NOTHING;")
    # Yil (KO'CHIRILGAN izoh-A L4): eski yillar O'CHIRILADI, yangi 2026 L3 otasining YILLARI olinadi
    # (foydalanuvchi talabi: "eskisini o'rniga yangi otasini yilini olsin"). years_of otaning yillari.
    if reparent_year:
        L.append("-- Yil (ko'chirilgan L4): eski yil o'chirilib, yangi 2026 L3 otasining yillari olinadi:")
        L.append("DELETE FROM h_speciality_year WHERE speciality_id IN ("
                 + ",".join(sql(sid) for sid, l3 in reparent_year) + ");")
        ry_rows = [(sid, y) for sid, l3 in reparent_year for y in sorted(years_of.get(l3) or {2026})]
        if ry_rows:
            L.append("INSERT INTO h_speciality_year (speciality_id, year) VALUES")
            L.append(",\n".join(f"  ({sql(sid)}, {y})" for sid, y in ry_rows) + "\nON CONFLICT (speciality_id, year) DO NOTHING;")
    L.append(ENDMARK)
    open(S017,'w',encoding='utf-8').write(base_s017.rstrip()+"\n\n"+"\n".join(L)+"\n")

    # ---- rollback ----
    base_rb = strip_block(open(S017_RB,encoding='utf-8').read())
    ins_ids=[x[0] for x in inserts]
    all_year=list(dict.fromkeys(year_ids+[sid for sid,l3 in reparent_year]))
    RB=[MARK, "-- Fix rollback: yangi L4 o'chirish + yil linklarini olib tashlash (fresh-build: baza noldan)."]
    if all_year: RB.append("DELETE FROM h_speciality_year WHERE speciality_id IN ("+",".join(sql(i) for i in all_year)+");")
    if ins_ids: RB.append("DELETE FROM h_speciality WHERE id IN ("+",".join(sql(i) for i in ins_ids)+");")
    # re-parent rollback asl parent kerak -> base_s017/S014'dan qayta o'qish murakkab; fresh-build'da ishlatilmaydi.
    RB.append("-- (re-parent asl-holati fresh-build'da tiklanmaydi; baza noldan quriladi.)")
    RB.append(ENDMARK)
    open(S017_RB,'w',encoding='utf-8').write(base_rb.rstrip()+"\n\n"+"\n".join(RB)+"\n")

    json.dump(mapping, open(MAP_OUT,'w',encoding='utf-8'), ensure_ascii=False, indent=0)

    print(f"[fix] YARAT (izoh=yangi)      : {n_create}")
    print(f"[fix] RE-PARENT (izoh=o'tkaz) : {n_reparent}")
    print(f"[fix] BIRIKTIR (topildi)      : {n_attach}")
    print(f"[fix] umumiy=L3 (L4 nomi=L3)  : {n_general}")
    print(f"[fix] FLAGGED (topilmadi->yarat): {len(flagged)}")
    for w,e,c,n in flagged: print(f"[fix]   {w}: {e} {c} | {n}")
    print(f"[fix] xarita yozuvlari   : {len(mapping)}  -> {os.path.basename(MAP_OUT)}")
    print(f"[fix] yil(2026) links    : {len(year_ids)}")

if __name__ == '__main__':
    main()
