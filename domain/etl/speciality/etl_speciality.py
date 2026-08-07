#!/usr/bin/env python3
"""
ETL: 2_Bakalavr.xlsx + 3_Magistr.xlsx -> normalized intermediate for h_speciality import.
Sxemadan mustaqil normalizatsiya:
  - education_level qo'shish (BACHELOR/MASTER)
  - self-ref (parent==id) -> parent=NULL
  - apostrof 6-variant -> U+02BB (ʻ);  name_search = folded lower
  - hierarchy_level: null bo'lsa parent.level+1 dan derive
  - yil: dot=oraliq kengaytir, comma=ro'yxat, single, null -> distinct int lar
  - review_status = APPROVED (xlsx 5367) | NEEDS_REVIEW (53 tirik-DB-yangi, yilsiz)
Manba: 2_Bakalavr.xlsx + 3_Magistr.xlsx (5367) + live_new_bachelor.csv + live_new_master.csv (53).
Chiqish: etl_speciality.csv (5420) + etl_speciality_year.csv (1:N) + statistika.
"""
import openpyxl, csv, os
from collections import Counter, defaultdict

NOCODE = {'shifrsiz', '-', ''}          # kodsiz belgilar -> NULL (review)

COLS = ['id','code','name_uz','name_ru','name_en','parent_id','edu_form','level','year','active','checked','version','created','created_by']
APOS = "'’ʻʼ‘`"          # 6 variant kuzatilgan
CANON = 'ʻ'          # ʻ  (U+02BB MODIFIER LETTER TURNED COMMA — o'zbek standarti)

def load(f, level):
    wb = openpyxl.load_workbook(f, read_only=True, data_only=True); ws = wb.worksheets[0]
    out = []
    for r in ws.iter_rows(min_row=2, values_only=True):
        if all(v is None for v in r): continue
        d = dict(zip(COLS, r)); d['education_level'] = level
        out.append(d)
    wb.close(); return out

def load_new_csv(f):
    """53 tirik-DB-yangi qator: level=None (derive), yilsiz, review=NEEDS_REVIEW."""
    out = []
    for r in csv.DictReader(open(f)):
        code = (r.get('code') or '').strip()
        d = {
            'id': r['id'], 'code': (None if code.lower() in NOCODE else code),
            'name_uz': r.get('name_uz'), 'name_ru': r.get('name_ru') or None, 'name_en': r.get('name_en') or None,
            'parent_id': (r.get('parent_id') or '').strip() or None,
            'edu_form': r.get('edu_form') or None, 'level': None,
            'year': (r.get('_year') or '').strip() or None,
            'active': (r.get('active','').lower() in ('t','true','1')),
            'checked': (r.get('is_checked','').lower() in ('t','true','1')),
            'version': r.get('version') or 1, 'created': r.get('created'), 'created_by': r.get('created_by'),
            'education_level': r['education_level'], 'review_status': 'NEEDS_REVIEW',
        }
        out.append(d)
    return out

def norm_apos(s):
    if s is None: return None
    return ''.join(CANON if ch in APOS else ch for ch in str(s))

def fold(s):
    """Identity/search fold. MUST match the SQL h_speciality_fold() (V018) byte-for-byte:
    apostrophe-variant -> space, lower, whitespace-collapse. No NFKD unaccent — the SQL fold that
    backs the GENERATED name_search column cannot call a non-IMMUTABLE unaccent, and Uzbek Latin has
    no combining marks so NFKD was a no-op here anyway. This value is used ONLY for ETL-side identity
    grouping now; the DB regenerates name_search itself, so the two folds must agree exactly."""
    if s is None: return ''
    s = ''.join(' ' if ch in APOS else ch for ch in str(s))
    return ' '.join(s.lower().split())

def parse_years(y):
    if y is None: return []
    s = str(y).strip()
    if not s: return []
    years = set()
    for part in s.split(','):
        part = part.strip()
        if not part: continue
        if '.' in part:                       # oraliq: 2024.2026 -> 2024,2025,2026
            a, b = part.split('.', 1)
            try:
                a, b = int(a), int(b)
                if a <= b and b - a < 30:      # aqlli chegara
                    years.update(range(a, b + 1))
                else:
                    years.update([a, b])
            except ValueError:
                pass
        else:
            try: years.add(int(part))
            except ValueError: pass
    return sorted(years)

# hemis-back seed papka (S014/S015 shu yerga yoziladi)
SEED_DIR = os.path.normpath(os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', 'src', 'main', 'resources', 'db', 'changelog', 'changesets', 'seed'))

def sql_str(v):
    """SQL literal: None->NULL, apostrof escape, aks holda 'text'."""
    if v is None or v == '': return 'NULL'
    return "'" + str(v).replace("'", "''") + "'"

def sql_bool(v):
    return 'true' if v in (True, 'True', 'true', 't', '1', 1) else 'false'

def emit_seed_sql(rows, year_rows, batch=500):
    # ---- S014: h_speciality ----
    # name_search is a GENERATED column in V018 (h_speciality_fold(name_uz)) — must NOT be inserted.
    # edu_form was dropped from the classifier (education form lives on the OTM attachment, not here).
    cols = ('id','code','name_uz','name_ru','name_en','education_level',
            'review_status','parent_id','hierarchy_level','active','is_checked','version')
    header = (
        "-- =====================================================\n"
        "-- S014: Unified speciality classifier seed (h_speciality)\n"
        "-- =====================================================\n"
        "-- Source: docs/mutaxasisliklar/2_Bakalavr.xlsx + 3_Magistr.xlsx (5367 APPROVED)\n"
        "--         + live-DB-new rows live_new_bachelor.csv/live_new_master.csv (53 NEEDS_REVIEW)\n"
        "-- Generated by etl_speciality.py. DO NOT hand-edit — re-run the ETL.\n"
        "-- Backs V018 h_speciality (bespoke UUID-keyed tree classifier, ADR-0006).\n"
        "-- Rows ordered by tree depth so parent_id FK resolves in-order.\n"
        "-- Two conflict strategies protect frontend curation on runOnChange re-seed:\n"
        "--   * APPROVED (xlsx-owned): DO UPDATE, but guarded WHERE review_status='APPROVED'\n"
        "--     AND updated_at IS NULL -> a row the curator edited in the FE (updated_at set)\n"
        "--     is never overwritten; untouched xlsx rows stay refreshed to source-of-truth.\n"
        "--   * NEEDS_REVIEW (live-DB-new): DO NOTHING -> inserted once, then owned by the FE\n"
        "--     forever (promotion to APPROVED never re-imports the raw WIP values).\n"
        "-- NOTE: additive-only — a row/year dropped from the source is NOT deleted here;\n"
        "--       source-drift reconciliation is out-of-band (see plan R3).\n"
        "-- IDENTITY-CONSOLIDATED: year-versioned (edu,code,name) twins are merged to ONE row\n"
        "--   carrying the union of years, so the seed satisfies uq_h_speciality_identity on first load.\n"
        "-- name_search is a GENERATED column in V018 (not inserted here); edu_form was dropped.\n"
        "-- =====================================================\n\n")
    updates = ("code=EXCLUDED.code, name_uz=EXCLUDED.name_uz, name_ru=EXCLUDED.name_ru, "
               "name_en=EXCLUDED.name_en, "
               "education_level=EXCLUDED.education_level, parent_id=EXCLUDED.parent_id, "
               "hierarchy_level=EXCLUDED.hierarchy_level, "
               "active=EXCLUDED.active, is_checked=EXCLUDED.is_checked")

    def row_values(r):
        return "  (" + ",".join([
            sql_str(r['id']), sql_str(r['code']), sql_str(r['name_uz']), sql_str(r['name_ru']),
            sql_str(r['name_en']), sql_str(r['education_level']),
            sql_str(r['review_status']), sql_str(r['parent_id']),
            (str(r['level']) if r['level'] is not None else 'NULL'),
            sql_bool(r['active']), sql_bool(r['checked']),
            str(r['version'] or 1),
        ]) + ")"

    # APPROVED (xlsx) first — NEEDS_REVIEW parents live in the APPROVED set, so this
    # ordering + DEFERRABLE FK guarantees parent-before-child.
    approved = [r for r in rows if r['review_status'] == 'APPROVED']
    review   = [r for r in rows if r['review_status'] == 'NEEDS_REVIEW']
    lines = [header]

    def emit_block(rowset, conflict_clause):
        for i in range(0, len(rowset), batch):
            chunk = rowset[i:i+batch]
            lines.append(f"INSERT INTO h_speciality ({','.join(cols)}) VALUES")
            lines.append(",\n".join(row_values(r) for r in chunk))
            lines.append(conflict_clause + "\n")

    lines.append(f"-- APPROVED ({len(approved)}) — xlsx source-of-truth, refreshed unless FE-edited\n")
    emit_block(approved, f"ON CONFLICT (id) DO UPDATE SET {updates}\n"
                         "  WHERE h_speciality.review_status = 'APPROVED' AND h_speciality.updated_at IS NULL;")
    lines.append(f"\n-- NEEDS_REVIEW ({len(review)}) — insert once, then owned by the frontend\n")
    emit_block(review, "ON CONFLICT (id) DO NOTHING;")

    open(f"{SEED_DIR}/S014_seed_h_speciality.sql", 'w').write("\n".join(lines))
    open(f"{SEED_DIR}/S014_seed_h_speciality_rollback.sql", 'w').write(
        "-- Rollback S014: remove seeded speciality rows.\n"
        "-- FK order: attachment (ON DELETE RESTRICT) -> year (CASCADE) -> speciality.\n"
        "-- Attachments are deleted first or the RESTRICT FK aborts the rollback.\n"
        "DELETE FROM h_speciality_attachment;\nDELETE FROM h_speciality_year;\nDELETE FROM h_speciality;\n")

    # ---- S015: h_speciality_year ----
    ylines = [
        "-- =====================================================\n"
        "-- S015: Normalized speciality years (h_speciality_year)\n"
        "-- =====================================================\n"
        "-- Source: etl_speciality.py — dot-range expanded (2024.2026->2024,2025,2026),\n"
        "--         comma-list split. Years exist only for APPROVED (xlsx) rows;\n"
        "--         the 53 NEEDS_REVIEW rows have no year (filled in the frontend).\n"
        "-- ON CONFLICT (speciality_id, year) DO NOTHING.\n"
        "-- =====================================================\n"]
    for i in range(0, len(year_rows), batch):
        chunk = year_rows[i:i+batch]
        ylines.append("INSERT INTO h_speciality_year (speciality_id, year) VALUES")
        ylines.append(",\n".join(f"  ({sql_str(sid)},{yr})" for sid, yr in chunk))
        ylines.append("ON CONFLICT (speciality_id, year) DO NOTHING;\n")
    open(f"{SEED_DIR}/S015_seed_h_speciality_year.sql", 'w').write("\n".join(ylines))
    open(f"{SEED_DIR}/S015_seed_h_speciality_year_rollback.sql", 'w').write(
        "-- Rollback S015: remove seeded year rows.\nDELETE FROM h_speciality_year;\n")

    print(f"[sql] S014 ({len(rows)} qator) + S015 ({len(year_rows)} qator) -> {SEED_DIR}")

def main():
    xlsx_rows = load('2_Bakalavr.xlsx', 'BACHELOR') + load('3_Magistr.xlsx', 'MASTER')
    for r in xlsx_rows: r['review_status'] = 'APPROVED'
    new_rows = load_new_csv('live_new_bachelor.csv') + load_new_csv('live_new_master.csv')
    rows = xlsx_rows + new_rows
    bymap = {r['id']: r for r in rows}

    # self-ref -> NULL
    self_fixed = 0
    for r in rows:
        if r['parent_id'] == r['id']:
            r['parent_id'] = None; self_fixed += 1

    # level derive (null -> parent.level+1; parent ham null bo'lsa root=1)
    derived = 0
    def resolve_level(r, seen=None):
        if r['level'] is not None: return r['level']
        seen = seen or set()
        if r['id'] in seen: return None          # tsikl himoyasi
        seen.add(r['id'])
        p = bymap.get(r['parent_id'])
        if p is None: return 1
        pl = resolve_level(p, seen)
        return (pl + 1) if pl is not None else None
    for r in rows:
        if r['level'] is None:
            r['level'] = resolve_level(r); derived += 1

    # apostrof + search + review_status + years
    year_rows = []
    for r in rows:
        for k in ('name_uz','name_ru','name_en'):
            r[k] = norm_apos(r[k])
        r['name_search'] = fold(r['name_uz'])
        for yr in parse_years(r['year']):
            year_rows.append((r['id'], yr))

    # ---- Identity consolidation (V018 uq_h_speciality_identity: education_level, code, name_search) ----
    # The xlsx carries year-versioned TWIN rows: the same (edu, code, name) as two+ UUIDs (a redundant
    # sibling twin, or a parent-child self-dup at level N and N+1). The DB now forbids that. Collapse each
    # group into ONE survivor carrying the UNION of every member's years; re-point any child of a dropped
    # member to the survivor so no subtree is orphaned. Survivor = shallowest level (so a parent-child
    # self-dup keeps the PARENT and never self-cycles), then the one that already has years, then id.
    years_by = defaultdict(set)
    for sid, yr in year_rows:
        years_by[sid].add(yr)
    groups = defaultdict(list)
    for r in rows:
        groups[(r['education_level'], r['code'] or None, r['name_search'])].append(r)
    remap = {}   # dropped_id -> survivor_id
    for members in groups.values():
        if len(members) == 1:
            continue
        survivor = sorted(members, key=lambda r: (
            r['level'] if r['level'] is not None else 99,
            0 if years_by.get(r['id']) else 1,
            str(r['id'])))[0]
        for r in members:
            if r['id'] != survivor['id']:
                remap[r['id']] = survivor['id']
                years_by[survivor['id']] |= years_by.pop(r['id'], set())
    consolidated = len(remap)
    rows = [r for r in rows if r['id'] not in remap]

    def resolve_id(pid, _seen=None):   # chase remap chain (3+ member groups), stop on cycle
        _seen = _seen or set()
        while pid in remap and pid not in _seen:
            _seen.add(pid); pid = remap[pid]
        return pid
    for r in rows:
        if r['parent_id']:
            np = resolve_id(r['parent_id'])
            r['parent_id'] = None if np == r['id'] else np   # never self-reference
    year_rows = sorted((sid, yr) for sid, ys in years_by.items() for yr in ys)
    bymap = {r['id']: r for r in rows}   # rebuild for the depth() sort below

    # ---- Self-checks: the seed MUST satisfy the DB constraints on first fresh load ----
    _idkeys = [(r['education_level'], r['code'] or None, r['name_search']) for r in rows]
    assert len(_idkeys) == len(set(_idkeys)), \
        f"identity collision remains after consolidation ({len(_idkeys)-len(set(_idkeys))})"
    assert not any(r['parent_id'] == r['id'] for r in rows), "self-referencing parent after consolidation"
    _ids = {r['id'] for r in rows}
    assert not any(r['parent_id'] and r['parent_id'] not in _ids for r in rows), \
        "dangling parent after consolidation"

    # topologik chuqurlik (FK tartibi: parent har doim boladan oldin INSERT bo'lsin)
    def depth(r, seen=None):
        seen = seen or set()
        if r['id'] in seen or not r['parent_id']: return 0
        seen.add(r['id']); p = bymap.get(r['parent_id'])
        return 0 if p is None else depth(p, seen) + 1
    for r in rows: r['_depth'] = depth(r)
    rows.sort(key=lambda r: (r['_depth'], r['education_level'], str(r['code'] or '~')))

    # yozish — CSV (tekshiruv uchun)
    sp_path = 'etl_speciality.csv'; yr_path = 'etl_speciality_year.csv'
    with open(sp_path, 'w', newline='') as fh:
        w = csv.writer(fh)
        w.writerow(['id','code','name_uz','name_ru','name_en','name_search','parent_id',
                    'education_level','edu_form','hierarchy_level','year_raw','active','is_checked','version','review_status'])
        for r in rows:
            w.writerow([r['id'], r['code'], r['name_uz'], r['name_ru'], r['name_en'], r['name_search'],
                        r['parent_id'], r['education_level'], r['edu_form'], r['level'],
                        (str(r['year']) if r['year'] is not None else ''), r['active'], r['checked'], r['version'], r['review_status']])
    with open(yr_path, 'w', newline='') as fh:
        w = csv.writer(fh); w.writerow(['speciality_id','year'])
        w.writerows(year_rows)

    # yozish — Liquibase seed SQL (S014 + S015)
    emit_seed_sql(rows, year_rows)

    # statistika
    print(f"[etl] jami qator      : {len(rows)}  (BACHELOR {sum(1 for r in rows if r['education_level']=='BACHELOR')} + MASTER {sum(1 for r in rows if r['education_level']=='MASTER')})")
    print(f"[etl] konsolidatsiya  : {consolidated} identity-dup birlashtirildi (uq_h_speciality_identity)")
    print(f"[etl] review_status    : {dict(Counter(r['review_status'] for r in rows))}")
    print(f"[etl] kodsiz (NULL code): {sum(1 for r in rows if not r['code'])}")
    # parent integritet (butun to'plam ichida)
    allids = set(bymap)
    dangling = sum(1 for r in rows if r['parent_id'] and r['parent_id'] not in allids)
    print(f"[etl] dangling parent   : {dangling}  (0 bo'lishi kerak)")
    print(f"[etl] self-ref->NULL  : {self_fixed}")
    print(f"[etl] level derive     : {derived}")
    print(f"[etl] level hali null   : {sum(1 for r in rows if r['level'] is None)}")
    print(f"[etl] year rows (1:N)  : {len(year_rows)}  (unikal spec yilbor: {len(set(y[0] for y in year_rows))})")
    print(f"[etl] yil taqsimot     : {dict(Counter(y[1] for y in year_rows))}")
    # apostrof tekshiruv: chiqishда faqat CANON qolganmi
    leftover = Counter()
    for r in rows:
        for ch in (r['name_uz'] or ''):
            if ch in "'’ʼ‘`": leftover[ch]+=1
    print(f"[etl] apostrof qoldiq (CANON'dan tashqari): {dict(leftover)}  (0 bo'lishi kerak)")
    print(f"[etl] yozildi: {sp_path} ({len(rows)}) + {yr_path} ({len(year_rows)})")

if __name__ == '__main__':
    main()
