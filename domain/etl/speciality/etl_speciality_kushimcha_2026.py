#!/usr/bin/env python3
"""
ETL (qo'shimcha): Бакалавр-кушимча-2026.xlsx + Магистр-кушимча-2026.xlsx
  -> S017_seed_h_speciality_2026.sql (additive; S014/S015 ga TEGMAYDI)

Nega alohida ETL:
  - Bu fayllar referens 2_Bakalavr/3_Magistr xlsx dan boshqacha shakl:
      * UUID YO'Q         -> deterministik uuid5 (kalit = edu|code, biznes-kalit; nom EMAS,
        chunki mashina-transliteratsiya nomlar keyin tuzatilishi mumkin -> re-run fork qilmasin)
      * parent UUID YO'Q  -> mavjud h_speciality daraxtidan kod+nom bo'yicha topiladi
      * nom KIRILL         -> lotin transliteratsiya (name_uz), asl kirill review CSV da saqlanadi
      * name_ru/name_en YO'Q -> NULL

IZOH (note) ustuni — 3 xil MA'NO (avval bittaga qorishtirilgan edi, adversarial review topdi):
  * "...қўшиш керак" / "янги қўшиш керак"  -> ADD_NEW: yangi UUID qator + yil 2026
  * "...буни 2026 га ҳам боғлаш керак"      -> LINK_ONLY: mavjud qatorga faqat yil bog'lash
                                              (yangi qator EMAS) — seed'ga KIRITILMAYDI
  * "...шифри ўзгарган"                     -> CODE_CHANGED: kod qayta tayinlangan (migration)
                                              — mavjud 2026 bog'lanishni retire qilish kerak
  * ADD_NEW, lekin base(code,edu) allaqachon 2026 ga bog'langan -> DUP_2026 (near-duplicate)
  LINK_ONLY + CODE_CHANGED + DUP_2026 -> avtomatik seed'ga KIRMAYDI, alohida REVIEW hisobotга
  (bularda mavjud S014 data'ni retire/reconcile qilish kerak — vazirlik intentini tasdiqlash shart).

Kod "to'qnashuvi" (yangi 60411500=Davlat auditi vs base 60411500=Biznes-tahlil, yillari 2021-2023
2026 dan ajralgan) bug EMAS — klassifikator kodi yildan-yilga qayta tayinlanadi. Model A: yangi
UUID qator, base saqlanadi. (Ambiguity faqat base ALSO 2026 ga bog'langanda -> DUP_2026 -> escalate.)

Chiqish:
  - etl_speciality_kushimcha_2026.csv          (audit: 61 clean-add, kirill+lotin+parent)
  - etl_speciality_kushimcha_2026_REVIEW.csv   (5 escalate: base holati + tavsiya)
  - <SEED_DIR>/S017_seed_h_speciality_2026.sql (+ _rollback.sql)  — faqat 61 clean-add
"""
import openpyxl, csv, re, uuid, os
from collections import Counter

SEED_DIR = os.path.normpath(os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', 'src', 'main', 'resources', 'db', 'changelog', 'changesets', 'seed'))
BASE_CSV = 'etl_speciality.csv'         # S014 manbasi (mavjud daraxt) — parent + collision
YEAR_CSV = 'etl_speciality_year.csv'    # S015 manbasi (yil bog'lanishlari) — 2026 ambiguity
NS = uuid.UUID('6f9619ff-8b86-d011-b42d-00cf4fc964ff')

# --- O'zbek kirill -> lotin (ʻ = U+02BB standarti; S014 barcha apostrofni ʻ ga normallashtirgan) ---
APOS = "'’ʻʼ‘`"
M = {
    'ё':'yo','ж':'j','ц':'ts','щ':'sh','ю':'yu','я':'ya','ъ':'ʻ','ь':'',
    'ў':'oʻ','қ':'q','ғ':'gʻ','ҳ':'h','ч':'ch','ш':'sh',
    'а':'a','б':'b','в':'v','г':'g','д':'d','е':'e','з':'z','и':'i','й':'y',
    'к':'k','л':'l','м':'m','н':'n','о':'o','п':'p','р':'r','с':'s','т':'t',
    'у':'u','ф':'f','х':'x','ы':'i','э':'e',
}
YOTA = {'е':'ye','ё':'yo','ю':'yu','я':'ya'}   # ъ + yumshoq unli -> obʻekt xato emas, obyekt

def translit(s):
    if s is None: return None
    s = str(s).replace('\xa0', ' ')
    out, i = [], 0
    while i < len(s):
        ch = s[i]; low = ch.lower()
        # ъ + {е,ё,ю,я} -> ye/yo/yu/ya (loanword: объект -> obyekt), aks holda ъ -> ʻ
        if low == 'ъ' and i + 1 < len(s) and s[i+1].lower() in YOTA:
            nxt = s[i+1]; rep = YOTA[nxt.lower()]
            if ch.isupper() or nxt.isupper(): rep = rep[0].upper() + rep[1:]
            out.append(rep); i += 2; continue
        rep = M.get(low)
        if rep is None:
            out.append(ch); i += 1; continue
        if ch.isupper(): rep = (rep[0].upper() + rep[1:]) if rep else rep
        out.append(rep); i += 1
    return re.sub(r'\s+', ' ', ''.join(out)).strip()

def nc(c):
    return re.sub(r'\s| ', '', str(c)).strip() if c is not None else None

def fold_search(s):
    """name_search: MUST match S014 etl_speciality.fold() and the SQL h_speciality_fold() (V018)
    byte-for-byte — apostrophe-variant -> space, lower, whitespace-collapse. No NFKD (the DB-generated
    name_search can't call a non-IMMUTABLE unaccent; Uzbek Latin has no combining marks anyway)."""
    if s is None: return ''
    s = ''.join(' ' if ch in APOS else ch for ch in str(s))
    return ' '.join(s.lower().split())

def fold_match(s):
    """Parent moslashtirish (looser): apostrof+tire->space, ts->s (ц ambiguity)."""
    return ' '.join(fold_search(s).replace('-', ' ').replace('ts', 's').split())

def year_tokens(raw):
    return [int(y) for y in re.split(r'[.,]', str(raw or '')) if y.strip().isdigit()]

# --- mavjud daraxt + yil holati ---
def load_base():
    rows = list(csv.DictReader(open(BASE_CSV)))
    l2, byce = {}, {}
    for r in rows:
        if r['hierarchy_level'] == '2':
            l2.setdefault((nc(r['code']), r['education_level']), []).append(r)
        byce.setdefault((nc(r['code']), r['education_level']), []).append(r)
    year_by_id = {}
    for r in csv.DictReader(open(YEAR_CSV)):
        year_by_id.setdefault(r['speciality_id'], set()).add(int(r['year']))
    return l2, byce, year_by_id

def resolve_parent(l2, code, edu, sec_fold):
    cands = l2.get((code, edu), [])
    if not cands:
        return None, 'NO-CANDIDATE'
    named = [c for c in cands if fold_match(c['name_uz']) == sec_fold]
    if len(named) == 1:
        return named[0]['id'], 'name'
    pool = named or cands
    pool = sorted(pool, key=lambda c: max(year_tokens(c['year_raw']) or [0]), reverse=True)
    return pool[0]['id'], ('name+recent' if named else 'recent-fallback')

def classify(code, edu, name_search, byce, year_by_id):
    """Ministry identity rule (user-confirmed): a leaf is decided by (education_level, code, name),
    NOT by the free-text note. If an existing base row has the SAME (edu, code, name_search):
      * it already has 2026  -> NOOP (fresh build already reproduces it via S014/S015)
      * it lacks 2026        -> LINK (add only the 2026 year to that existing id)
    Otherwise -> ADD_NEW (a genuinely new row: new code, OR a new name under an existing code).
    Nothing is ever retired (same-code/different-name and same-name/different-code both stay).
    The 'шифри ўзгарган' / 'боғлаш керак' notes are informational only — this data-driven rule
    subsumes them and needs no per-note escalation."""
    for r in byce.get((code, edu), []):
        if r['name_search'] == name_search:      # exact (edu, code, name) identity match
            return ('NOOP', r['id']) if 2026 in year_by_id.get(r['id'], set()) else ('LINK', r['id'])
    return ('ADD_NEW', None)

def base_state(code, edu, byce, year_by_id):
    return [(r['name_uz'], sorted(year_by_id.get(r['id'], set())), r['id'])
            for r in byce.get((code, edu), [])]

def parse_file(path, edu, l2, byce, year_by_id):
    wb = openpyxl.load_workbook(path, read_only=True, data_only=True)
    ws = wb.worksheets[0]
    cur = None
    add, link, noop = [], [], []
    for r in ws.iter_rows(values_only=True):
        if all(v is None for v in r): continue
        cells = list(r) + [None] * 6
        code, name, lvl, note = nc(cells[1]), cells[2], cells[3], cells[5]
        lvl = int(str(lvl)) if str(lvl).strip().isdigit() else None
        note = str(note).strip() if note is not None else ''
        if lvl == 2:
            cur = (code, fold_match(translit(name)), translit(name))
        if not note:
            continue
        cyr = str(name).replace('\xa0', ' ').strip()
        lat = translit(name)
        ns = fold_search(lat)
        kind, existing_id = classify(code, edu, ns, byce, year_by_id)
        if kind == 'ADD_NEW':
            pid, how = resolve_parent(l2, cur[0], edu, cur[1]) if cur else (None, 'NO-SECTION')
            add.append({
                'id': str(uuid.uuid5(NS, f"hemis-spec-2026|{edu}|{code}")),
                'code': code, 'name_cyr': cyr, 'name_uz': lat, 'name_search': ns,
                'education_level': edu, 'review_status': 'APPROVED',
                'parent_id': pid, 'parent_code': cur[0] if cur else None,
                'parent_name': cur[2] if cur else None, 'parent_how': how,
                'hierarchy_level': lvl or 3, 'year': 2026,
            })
        elif kind == 'LINK':          # exact identity exists but lacks 2026 -> add only the year
            link.append({'edu': edu, 'code': code, 'name_uz': lat,
                         'existing_id': existing_id, 'note': note})
        else:                          # NOOP — already reproduced by S014/S015 with 2026
            noop.append({'edu': edu, 'code': code, 'name_uz': lat, 'existing_id': existing_id,
                         'note': note, 'base': base_state(code, edu, byce, year_by_id)})
    wb.close()
    return add, link, noop

def sql_str(v):
    return 'NULL' if v is None or v == '' else "'" + str(v).replace("'", "''") + "'"

def emit(leaves, links):
    # name_search is GENERATED in V018 (h_speciality_fold(name_uz)) — must NOT be inserted.
    # edu_form was dropped from the classifier. name_oz = original Cyrillic (authoritative source).
    cols = ('id','code','name_uz','name_oz','name_ru','name_en','education_level',
            'review_status','parent_id','hierarchy_level','active','is_checked','version')
    def row_sql(r):
        return "  (" + ",".join([
            sql_str(r['id']), sql_str(r['code']), sql_str(r['name_uz']), sql_str(r['name_cyr']),
            'NULL', 'NULL', sql_str(r['education_level']), sql_str(r['review_status']),
            sql_str(r['parent_id']), str(r['hierarchy_level']), 'true', 'false', '1',
        ]) + ")"
    hdr = (
        "-- =====================================================\n"
        "-- S017: 2026 supplementary specialities (h_speciality + h_speciality_year)\n"
        "-- =====================================================\n"
        "-- Source: docs/mutaxasisliklar/Бакалавр-кушимча-2026.xlsx + Магистр-кушимча-2026.xlsx\n"
        f"--         ({len(leaves)} NEW L3 leaves, year 2026). Generated by etl_speciality_kushimcha_2026.py.\n"
        "-- DO NOT hand-edit — re-run the ETL.\n"
        "-- IDENTITY RULE (ministry, user-confirmed): a leaf is placed by (education_level, code, name),\n"
        "--   NOT by its free-text note. A leaf whose (edu, code, name) does NOT already exist is inserted\n"
        "--   as a NEW uuid5 row + 2026 — this covers 'code re-assigned' (шифри ўзгарган: same name, new\n"
        "--   code) and 'a new name under an existing code' alike. A leaf whose exact (edu, code, name)\n"
        "--   already exists is a NO-OP (it already carries 2026 via S014/S015) or a LINK (only the 2026\n"
        "--   year is added to that existing id, below). NOTHING is retired: same-code/different-name and\n"
        "--   same-name/different-code both stay distinct rows (they differ in the identity key).\n"
        "-- ADDITIVE over S014/S015 — those are NOT touched. One code may be shared by several rows with\n"
        "--   different names (e.g. MASTER 70530402 = Gidrogeologiya AND Suv resurslarini boshqarish); the\n"
        "--   (education_level, code, name_search) UNIQUE permits that and each row's 2026 link is its own.\n"
        "-- id = uuid5(edu|code) — stable across later name corrections so a re-run never forks.\n"
        "-- name_oz = original Cyrillic (authoritative ministry source); name_uz = machine transliteration,\n"
        "--   name_ru/name_en may be enriched later in the FE. review_status = APPROVED — these are official\n"
        "--   2026 ministry specialities (Excel source), so like S014 they are born APPROVED and therefore\n"
        "--   distributable to the 224 OTMs. name_search is a GENERATED column (V018) — not inserted here;\n"
        "--   edu_form was dropped. ON CONFLICT (id) DO NOTHING never overwrites FE edits. Escalations\n"
        "--   (NO-OP / LINK) are logged to the ETL REVIEW CSV.\n"
        "-- =====================================================\n\n")
    lines = [hdr,
             f"INSERT INTO h_speciality ({','.join(cols)}) VALUES",
             ",\n".join(row_sql(r) for r in leaves),
             "ON CONFLICT (id) DO NOTHING;\n\n",
             "-- Years (all 2026; FK -> h_education_year(2026), seeded in V018).\n",
             "INSERT INTO h_speciality_year (speciality_id, year) VALUES",
             ",\n".join(f"  ({sql_str(r['id'])},2026)" for r in leaves),
             "ON CONFLICT (speciality_id, year) DO NOTHING;\n"]
    if links:
        lines += [
            "\n-- LINK-only: an existing (edu,code,name) row the 2026 list re-confirms but that lacked the\n"
            "-- 2026 year. Add ONLY the year to the existing id (no new row, nothing retired).\n",
            "INSERT INTO h_speciality_year (speciality_id, year) VALUES",
            ",\n".join(f"  ({sql_str(l['existing_id'])},2026)" for l in links),
            "ON CONFLICT (speciality_id, year) DO NOTHING;\n"]
    open(f"{SEED_DIR}/S017_seed_h_speciality_2026.sql", 'w').write("\n".join(lines))

    ids = ",\n  ".join(sql_str(r['id']) for r in leaves)
    rb = ["-- Rollback S017: remove ONLY the 2026 supplementary NEW rows (targeted by id).\n",
          "-- Year rows first (FK), then the speciality rows. S014/S015 data untouched.\n",
          f"DELETE FROM h_speciality_year WHERE speciality_id IN (\n  {ids}\n);\n",
          f"DELETE FROM h_speciality WHERE id IN (\n  {ids}\n);\n"]
    if links:
        link_ids = ",\n  ".join(sql_str(l['existing_id']) for l in links)
        rb.append("-- LINK rollback: drop only the 2026 year S017 added to these pre-existing rows.\n"
                  f"DELETE FROM h_speciality_year WHERE year = 2026 AND speciality_id IN (\n  {link_ids}\n);\n")
    open(f"{SEED_DIR}/S017_seed_h_speciality_2026_rollback.sql", 'w').write("".join(rb))

def main():
    l2, byce, year_by_id = load_base()
    a1, k1, n1 = parse_file('Бакалавр-кушимча-2026.xlsx', 'BACHELOR', l2, byce, year_by_id)
    a2, k2, n2 = parse_file('Магистр-кушимча-2026.xlsx', 'MASTER', l2, byce, year_by_id)
    leaves, links, noops = a1 + a2, k1 + k2, n1 + n2

    with open('etl_speciality_kushimcha_2026.csv', 'w', newline='') as fh:
        w = csv.writer(fh)
        w.writerow(['id','code','name_cyr','name_uz','education_level','hierarchy_level',
                    'parent_id','parent_code','parent_name','parent_how','year','review_status'])
        for r in leaves:
            w.writerow([r['id'], r['code'], r['name_cyr'], r['name_uz'], r['education_level'],
                        r['hierarchy_level'], r['parent_id'], r['parent_code'], r['parent_name'],
                        r['parent_how'], r['year'], r['review_status']])

    # REVIEW CSV now DOCUMENTS the two escalation outcomes (NOOP / LINK) — no row is excluded from the
    # seed anymore; every leaf is either an ADD_NEW row (in S017) or resolves to an existing S014 id.
    with open('etl_speciality_kushimcha_2026_REVIEW.csv', 'w', newline='') as fh:
        w = csv.writer(fh)
        w.writerow(['edu','code','name_uz','resolution','existing_id','note','base_rows(name|years|id)'])
        for r in noops:
            bs = ' ;; '.join(f"{n} | {y} | {i[:8]}" for n, y, i in r['base'])
            w.writerow([r['edu'], r['code'], r['name_uz'], 'NOOP (already has 2026)',
                        (r['existing_id'] or '')[:8], r['note'], bs])
        for l in links:
            w.writerow([l['edu'], l['code'], l['name_uz'], 'LINK (2026 added to existing)',
                        (l['existing_id'] or '')[:8], l['note'], ''])

    emit(leaves, links)

    # --- Self-checks: the seed MUST load under uq_h_speciality_identity + parent FK on a fresh DB ---
    base_keys = {(k[0], k[1], r['name_search']) for k, rs in byce.items() for r in rs}
    base_ids = {r['id'] for rs in byce.values() for r in rs}
    leaf_keys = [(r['code'], r['education_level'], r['name_search']) for r in leaves]
    assert len(leaf_keys) == len(set(leaf_keys)), 'duplicate identity within S017 leaves'
    clash = [k for k in leaf_keys if k in base_keys]
    assert not clash, f'S017 leaf identity collides with S014 base: {clash[:3]}'
    miss_p = [r['code'] for r in leaves if r['parent_id'] and r['parent_id'] not in base_ids]
    assert not miss_p, f'S017 parent_id not found in S014: {miss_p[:3]}'

    print(f"[etl] ADD_NEW rows   : {len(leaves)}  (BACHELOR {sum(1 for r in leaves if r['education_level']=='BACHELOR')} + MASTER {sum(1 for r in leaves if r['education_level']=='MASTER')})")
    print(f"[etl] LINK (year+2026): {len(links)}")
    print(f"[etl] NOOP (had 2026) : {len(noops)}  -> etl_speciality_kushimcha_2026_REVIEW.csv")
    for r in noops + links:
        print(f"[etl]   - {r['edu']} {r['code']} '{r['name_uz'][:34]}'  note='{r['note']}'")
    print(f"[etl] parent resolve : {dict(Counter(r['parent_how'] for r in leaves))}")
    print(f"[etl] parent YO'Q    : {sum(1 for r in leaves if not r['parent_id'])} (0 kerak)")
    print(f"[etl] UUID dup       : {len([k for k,c in Counter(r['id'] for r in leaves).items() if c>1])} (0 kerak)")
    print(f"[etl] identity vs base: 0 clash (assert o'tdi)")
    leftover = Counter(ch for r in leaves for ch in (r['name_uz'] or '') if ch in "'’ʼ‘`")
    print(f"[etl] apostrof qoldiq: {dict(leftover)} (0 -> hammasi ʻ)")

if __name__ == '__main__':
    main()
