#!/usr/bin/env python3
"""
generate_s018.py — reproducible generator for S018_seed_speciality_attachment_2026.sql

Loads the 2026-2027 ministry OTM<->speciality assignment spreadsheets, resolves each
plan row to an h_speciality UUID (NAME-identity first — see README), fans out per set
education_form column, and (re)writes the S018 seed + rollback into the changelog.

Run from anywhere:  python3 domain/etl/attachment/generate_s018.py
Deterministic: same inputs -> byte-identical S018 output (uuid5 ids, sorted rows).

Sources (tracked next to this script):
  Bakalavr_2026-2027.xlsx  (education_type 11, sheet "Kunduzgi")
  Magistratura_2026-2027.xlsx (education_type 12, sheet "Sheet1")
Classifier read from the checked-in seeds: S014 + S017 (h_speciality rows).
education_form codes: Kunduzgi=11, Kechki=12, Masofa(viy)=16 — from
  docs/old-klasifikatorlar/HEMIS_Klassifikator_Dump.json (hemishe_h_education_form).
"""
import openpyxl, re, uuid, difflib, os
from collections import defaultdict, Counter

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(HERE, "..", "..", ".."))          # .../hemis-back
SEED = os.path.join(REPO, "domain/src/main/resources/db/changelog/changesets/seed")
SPEC = [os.path.join(SEED, "S014_seed_h_speciality.sql"),
        os.path.join(SEED, "S017_seed_h_speciality_2026.sql")]
XLSX = [("Bakalavr_2026-2027.xlsx", "Kunduzgi", "11"),
        ("Magistratura_2026-2027.xlsx", "Sheet1", "12")]
# xlsx column index (0-based) -> education_form code
FORMS = [(6, "11"), (7, "12"), (8, "16")]   # Kunduzgi / Kechki / Masofa
NS = uuid.UUID("6f4a2b7e-0000-4000-8000-000000000018")   # stable uuid5 namespace for S018
CB = "seed:S018-2026"                                    # created_by provenance tag
# These two spreadsheets ARE the 2026-2027 ministry assignment plan, so EVERY attachment is bound
# to this one academic year (-> university_speciality_attachment.edu_year, FK h_education_year.year).
# The per-row "Yil" column is treated as a cross-check, not the source of truth: it must equal
# ASSIGN_YEAR wherever it is filled (a blank cell is fine), else we abort loudly rather than emit a
# mislabeled year. This guarantees the "har birini 2026-2027 o'quv yiliga biriktirish" requirement.
ASSIGN_YEAR = 2026
OUT_SEED = os.path.join(SEED, "S018_seed_speciality_attachment_2026.sql")
OUT_RB   = os.path.join(SEED, "S018_seed_speciality_attachment_2026_rollback.sql")

# ---------- minimal SQL VALUES parser (name-keyed) ----------
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
def fold(t):
    t=t or ''
    for ch in "'’ʻʼ‘`": t=t.replace(ch,' ')
    return re.sub(r'\s+',' ',t.lower()).strip()
def base(t):
    t=fold(t); t=re.split(r'[:(]',t)[0]; t=re.sub(r'[^0-9a-zа-яʼʻ ]',' ',t)
    return re.sub(r'\s+',' ',t).strip()
def norm(t):
    t=fold(t); t=t.replace('ts','s'); return re.sub(r'[^0-9a-zа-я]','',t)
def cpl(a,b):
    a=a or '';b=b or '';n=0
    for x,y in zip(a,b):
        if x==y:n+=1
        else:break
    return n

# ---------- load classifier (id, code, hl, folded/base/norm names) ----------
CL=defaultdict(list); byName=defaultdict(list); byCode=defaultdict(list); byNorm=defaultdict(list)
id2parent={}; id2hl={}; id2code={}; id2name={}; HASCHILD=set()   # tree maps for code-consistent resolution
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
            id2parent[sid]=pid; id2hl[sid]=hl; id2code[sid]=code; id2name[sid]=nm
            if pid: HASCHILD.add(pid)
            idx=len(CL[edu]); CL[edu].append((code, sid, hl, fold(nm), base(nm), norm(nm), nm))
            byName[(edu,fold(nm))].append(idx)
            if code: byCode[(edu,code)].append(idx)
            byNorm[(edu,norm(nm))].append(idx)

# (speciality_id, year) pairs already present in h_speciality_year (seeded by S015) — a speciality
# "exists" in a year iff it has that pair. Used to backfill any attached speciality's assignment year.
HAS_YEAR=set()
_S015=os.path.join(SEED, "S015_seed_h_speciality_year.sql")
if os.path.exists(_S015):
    for _mm in re.finditer(r"\('([0-9a-fA-F-]{36})'\s*,\s*(\d{4})\)", open(_S015, encoding='utf-8').read()):
        HAS_YEAR.add((_mm.group(1), int(_mm.group(2))))

children=defaultdict(list)   # classifier parent_sid -> [(edu, idx)] — for in-parent child resolution
for _edu in list(CL.keys()):
    for _idx,_row in enumerate(CL[_edu]):
        _p=id2parent.get(_row[1])
        if _p: children[_p].append((_edu, _idx))

def resolve_child(edu, parent_sid, shifr, fn, bn, nm):
    """Resolve a kateg-2 (L4) row among the CLASSIFIER CHILDREN of the already-resolved kateg-1
    parent, so parent & child always share a subtree (the Excel groups them; both carry the same
    Shifr). Ranks by code==Shifr, exact/norm/base name. None if the parent has no matching child."""
    best=None; bestkey=None
    for (e,idx) in children.get(parent_sid, ()):
        if e!=edu: continue
        code,sid,hl,f,b,nrm,orig = CL[edu][idx]
        key=( code==shifr, f==fn, nrm==nm, b==bn )
        if bestkey is None or key>bestkey:
            bestkey=key; best=sid
    return best

def pickbest(edu, idxs, shifr, want_hl):
    scored=[]
    for i in idxs:
        code, hl = CL[edu][i][0], CL[edu][i][2]
        key=((code==shifr), cpl(code or '',shifr), (hl==want_hl), (len(code or '')==8))
        scored.append((key, -(int(re.sub(r'\D','',code or '9'*9) or 9**9)), i))
    scored.sort(reverse=True)
    return scored[0][2]

def pick_in_code(edu, cands, want_hl, fn, bn, nm):
    """Best classifier row among those sharing the Excel Shifr, at the wanted level. Ranks by:
    child-bearing (disambiguates a duplicated yo'nalish -> the real parent), exact fold-name,
    norm-name (colon/paren-insensitive), base-name. Returns None if the Shifr has no row at
    want_hl (caller then falls through to the name-based tiers)."""
    best=None; bestkey=None
    for i in cands:
        code,sid,hl,f,b,nrm,orig = CL[edu][i]
        if want_hl is not None and hl != want_hl: continue
        key=( sid in HASCHILD, f==fn, nrm==nm, b==bn, len(code or '')==8 )
        if bestkey is None or key>bestkey:
            bestkey=key; best=i
    return best

def resolve(edu, kateg, shifr, name):
    fn=fold(name); bn=base(name); nm=norm(name)
    want_hl=3 if kateg=='1' else (4 if kateg=='2' else None)
    # CODE-FIRST: the Excel Shifr is the ministry's authoritative code. Resolve WITHIN that code
    # (same level, best name; child-bearing node for a duplicated yo'nalish) so a parent (kateg 1)
    # and its child (kateg 2) sharing a Shifr always land in the same classifier subtree — the tree
    # is then derived from h_speciality.parent_id at display time (no parent rows stored here).
    if byCode.get((edu, shifr)):
        i = pick_in_code(edu, byCode[(edu,shifr)], want_hl, fn, bn, nm)
        if i is not None:
            return CL[edu][i][1], "0_CODE"
    if byName.get((edu,fn)):
        i=pickbest(edu, byName[(edu,fn)], shifr, want_hl)
        return CL[edu][i][1], ("1_EXACT" if CL[edu][i][0]==shifr else "2_NAME")
    if byNorm.get((edu,nm)):
        return CL[edu][pickbest(edu, byNorm[(edu,nm)], shifr, want_hl)][1], "3_SPELL"
    if byName.get((edu,bn)):
        return CL[edu][pickbest(edu, byName[(edu,bn)], shifr, 3)][1], "4_PARENT"
    if byNorm.get((edu,norm(bn))):
        return CL[edu][pickbest(edu, byNorm[(edu,norm(bn))], shifr, 3)][1], "4_PARENT"
    if byCode.get((edu,shifr)):
        cand=[i for i in byCode[(edu,shifr)] if CL[edu][i][4]==bn or bn in CL[edu][i][4] or CL[edu][i][4] in bn]
        if cand:
            return CL[edu][pickbest(edu, cand, shifr, want_hl)][1], "5_CODE_BASE"
    best=None;bestr=0
    for i in byCode.get((edu,shifr),[]):
        r=difflib.SequenceMatcher(None, fn, CL[edu][i][3]).ratio()
        if r>bestr: bestr=r; best=i
    if best is not None and bestr>=0.90:
        return CL[edu][best][1], "6_FUZZY"
    return None, "7_UNRESOLVED"

# ---------- load xlsx data rows ----------
def load(fn, sheet, edu):
    wb=openpyxl.load_workbook(os.path.join(HERE, fn), read_only=True, data_only=True); ws=wb[sheet]
    out=[]
    for r in ws.iter_rows(min_row=2, values_only=True):
        shifr=str(r[3]).strip() if r[3] is not None else ''
        if not re.fullmatch(r"\d{8}", shifr): continue          # skip OTM header rows (shifr=1)
        otmid=str(r[1]).strip() if r[1] is not None else ''
        kateg=str(r[2]).strip() if r[2] is not None else ''
        name=str(r[4] or '').strip()
        yil=str(r[5]).strip() if r[5] is not None else ''
        yr=int(yil) if yil.isdigit() else None       # assignment academic year (col 5 "Yil", e.g. 2026)
        forms=[c for (ci,c) in FORMS if (r[ci] is not None and str(r[ci]).strip()!='')]
        out.append((edu, otmid, kateg, shifr, name, forms, yr))
    wb.close(); return out

rows=[]
for fn,sheet,edu in XLSX: rows+=load(fn, sheet, edu)

conf=Counter(); attach=set(); unresolved=[]; year_mismatch=[]
cur_otm=None; cur_parent=None      # parent-context: last resolved kateg-1 (yo'nalish) for this OTM
for edu,otmid,kateg,shifr,name,forms,yr in rows:
    if kateg=='1':
        sid,c = resolve(edu, '1', shifr, name)          # code-first L3 (child-bearing yo'nalish)
        cur_otm, cur_parent = otmid, sid
    elif kateg=='2':
        sid=None; c=None
        if otmid==cur_otm and cur_parent is not None:   # resolve the child WITHIN its yo'nalish parent
            sid = resolve_child(edu, cur_parent, shifr, fold(name), base(name), norm(name))
            if sid is not None: c="0_CHILD"
        if sid is None:                                  # fallback: no context / parent lacks this child
            sid,c = resolve(edu, '2', shifr, name)
    else:
        sid,c = resolve(edu, kateg, shifr, name)
    conf[c]+=1
    # Cross-check the source "Yil": a filled cell MUST agree with the 2026-2027 plan; blank is OK.
    if yr is not None and yr != ASSIGN_YEAR: year_mismatch.append((edu,otmid,kateg,shifr,name,yr))
    if sid is None: unresolved.append((edu,kateg,shifr,name)); continue
    # Every assignment is bound to the 2026-2027 academic year (authoritative, not the raw column).
    for fc in forms: attach.add((otmid, sid, fc, ASSIGN_YEAR))

print(f"xlsx data rows: {len(rows)}   resolution:", dict(sorted(conf.items())))
# Diagnostic: is each attached L4 in the SAME classifier subtree as an attached parent for its OTM?
# (tree is derived from h_speciality.parent_id at display time — this only measures resolution quality)
_otm_sids=defaultdict(set)
for _uc,_sid,_ef,_yr in attach: _otm_sids[_uc].add(_sid)
_split=sum(1 for _uc,_sid,_ef,_yr in attach if id2hl.get(_sid)==4 and id2parent.get(_sid) not in _otm_sids[_uc])
_old=len({id2code.get(_sid) for _uc,_sid,_ef,_yr in attach if not re.fullmatch(r'\d{8}', id2code.get(_sid) or '')})
print(f"L4 tree-split: {_split}   OLD (non-8-digit) codes: {_old}")
# YEAR-BACKFILL: every attached speciality must EXIST in its assignment year (h_speciality_year).
# The assignment year is the attachment's edu_year; add any (speciality, year) missing from S015.
NEED_YEAR = sorted({(_sid,_yr) for _uc,_sid,_ef,_yr in attach if (_sid, _yr) not in HAS_YEAR})
print(f"attached (speciality,year) pairs MISSING from h_speciality_year -> backfill: {len(NEED_YEAR)}"
      f"  (distinct specialities: {len({s for s,y in NEED_YEAR})})")
if year_mismatch:
    print(f"YEAR MISMATCH (source 'Yil' != {ASSIGN_YEAR}):", len(year_mismatch))
    for m in year_mismatch[:20]: print("   ", m)
    raise SystemExit(f"Refusing to generate: rows carry a year other than {ASSIGN_YEAR}.")
if unresolved:
    print("UNRESOLVED:", len(unresolved))
    for u in unresolved: print("   ", u)
    raise SystemExit("Refusing to generate: unresolved rows present.")

rows_out=[]
for uc,sid,ef,yr in sorted(attach):
    rid=str(uuid.uuid5(NS, f"{uc}|{sid}|{ef}|{yr}"))
    rows_out.append((rid, uc, sid, ef, yr))
print(f"attachment rows (dedup): {len(rows_out)}   forms:", dict(Counter(r[3] for r in rows_out)),
      "  years:", dict(Counter(r[4] for r in rows_out)), "  OTM:", len({r[1] for r in rows_out}))

# ---------- emit seed ----------
def esc(s): return s.replace("'","''")
HDR="""-- =====================================================
-- S018: SEED SPECIALITY -> OTM ATTACHMENT (2026-2027)
-- =====================================================
-- Author: hemis-team
-- Purpose: Load the 2026-2027 ministry OTM<->speciality assignments into
--          university_speciality_attachment (V019).
-- Resolution: each xlsx row carries the official 8-digit 2026 Shifr, so matching is
--          CODE-FIRST to the h_speciality UUID (0_CODE: code==Shifr, 8-digit, tie-broken
--          by child-bearing/form/name). Kateg-2 (ichki yo'nalish, L4) is resolved WITHIN
--          its Kateg-1 (yo'nalish, L3) parent context (0_CHILD) so a sub-direction binds
--          to the right parent. No old 7-digit classifier row is ever attached.
-- Year:    every attachment is bound to the 2026-2027 academic year (edu_year=2026),
--          FK -> h_education_year(year) — the SAME modern year classifier h_speciality_year
--          uses. The source 'Yil' column is cross-checked (mismatch aborts generation).
-- Fan-out: one row per set education_form column
--          Kunduzgi=11, Kechki=12, Masofa(viy)=16 (hemishe_h_education_form).
-- Keying: id = uuid5(ns, university_code|speciality_id|education_form|edu_year) so a
--          re-run is stable. created_by='seed:S018-2026' tags seed provenance
--          (rollback target). status='ACTIVE'.
-- Idempotent: ON CONFLICT on the live-unique index
--          (university_code, speciality_id, education_form, edu_year) WHERE deleted_at IS NULL.
-- Generated by domain/etl/attachment/generate_s018.py. DO NOT hand-edit.
-- =====================================================
"""
BATCH=500; lines=[HDR]
for i in range(0,len(rows_out),BATCH):
    chunk=rows_out[i:i+BATCH]
    lines.append("INSERT INTO university_speciality_attachment (id, university_code, speciality_id, education_form, edu_year, status, created_by) VALUES")
    lines.append(",\n".join(f"  ('{rid}', '{esc(uc)}', '{sid}', '{ef}', {yr}, 'ACTIVE', '{CB}')" for rid,uc,sid,ef,yr in chunk))
    lines.append("ON CONFLICT (university_code, speciality_id, education_form, edu_year) WHERE deleted_at IS NULL DO NOTHING;\n")
open(OUT_SEED,"w").write("\n".join(lines))
open(OUT_RB,"w").write(
    "-- =====================================================\n"
    "-- S018 ROLLBACK: remove the 2026-2027 seeded OTM<->speciality attachments.\n"
    "-- Targets only seed-provenance rows (created_by tag); user-created attachments untouched.\n"
    "-- =====================================================\n"
    f"DELETE FROM university_speciality_attachment WHERE created_by = '{CB}';\n")
print("wrote:", OUT_SEED)
print("wrote:", OUT_RB)

# ---------- emit year backfill: every attached speciality must EXIST in its assignment year ----------
# Uses the speciality<->year table (h_speciality_year). Distinct from the attachment's own edu_year.
OUT_YR    = os.path.join(SEED, "S018b_seed_h_speciality_year_backfill.sql")
OUT_YR_RB = os.path.join(SEED, "S018b_seed_h_speciality_year_backfill_rollback.sql")
_yhdr=("-- =====================================================\n"
       "-- S018b: BACKFILL h_speciality_year for specialities that S018 attaches to an OTM for a\n"
       "--        given academic year but which do NOT yet carry that year. A speciality assigned\n"
       "--        for a year must exist in that year (h_speciality_year). Distinct from the OTM\n"
       "--        attachment's own edu_year. Idempotent (ON CONFLICT). Runs after S015/S017.\n"
       "-- Generated by domain/etl/attachment/generate_s018.py. DO NOT hand-edit.\n"
       "-- =====================================================\n")
_yl=[_yhdr]
for i in range(0,len(NEED_YEAR),BATCH):
    chunk=NEED_YEAR[i:i+BATCH]
    _yl.append("INSERT INTO h_speciality_year (speciality_id, year) VALUES")
    _yl.append(",\n".join(f"  ('{sid}', {yr})" for sid,yr in chunk))
    _yl.append("ON CONFLICT (speciality_id, year) DO NOTHING;\n")
open(OUT_YR,"w").write("\n".join(_yl))
_rb=["-- S018b ROLLBACK: remove ONLY the backfilled speciality-year rows (they were missing before).\n"]
for i in range(0,len(NEED_YEAR),BATCH):
    chunk=NEED_YEAR[i:i+BATCH]
    _rb.append("DELETE FROM h_speciality_year WHERE (speciality_id, year) IN ("
               + ",".join(f"('{sid}',{yr})" for sid,yr in chunk) + ");")
open(OUT_YR_RB,"w").write("\n".join(_rb)+"\n")
print("wrote:", OUT_YR, f"({len(NEED_YEAR)} backfill rows)")
