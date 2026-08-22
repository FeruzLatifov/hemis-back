#!/usr/bin/env python3
"""Har bir @EntityGraph(attributePaths = {...}) yo'lini entity maydonlariga solishtiradi.

Nega kerak: @EntityGraph yo'llari startupda EMAS, so'rov bajarilganda tekshiriladi.
Shuning uchun eskirgan yo'l ilovani ko'tarilishiga xalaqit bermaydi va faqat
foydalanuvchi o'sha endpointni ochganda 500 beradi (2026-08-21: `category`).
"""
import re, sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent

# Entity nomi -> maydon nomlari
entities = {}
for f in ROOT.rglob('domain/src/main/java/**/entity/**/*.java'):
    src = f.read_text(errors='replace')
    if '@Entity' not in src and '@MappedSuperclass' not in src:
        continue
    name = f.stem
    fields = set(re.findall(r'^\s*(?:private|protected|public)\s+[\w.<>,\[\]\s?]+?\s+(\w+)\s*(?:=|;)',
                            src, re.M))
    parent = re.search(r'class\s+\w+\s+extends\s+(\w+)', src)
    entities[name] = (fields, parent.group(1) if parent else None)

def all_fields(name, seen=None):
    seen = seen or set()
    if name in seen or name not in entities:
        return set()
    seen.add(name)
    fields, parent = entities[name]
    return fields | (all_fields(parent, seen) if parent else set())

problems, checked = [], 0
for f in sorted(ROOT.rglob('domain/src/main/java/**/repository/*.java')):
    src = f.read_text(errors='replace')
    m = re.search(r'extends\s+[\w<>,\s.]*?(?:JpaRepository|CrudRepository|PagingAndSortingRepository)\s*<\s*(\w+)', src)
    if not m:
        continue
    entity = m.group(1)
    fields = all_fields(entity)
    if not fields:
        continue
    for lineno, line in enumerate(src.splitlines(), 1):
        g = re.search(r'@EntityGraph\s*\(\s*attributePaths\s*=\s*\{([^}]*)\}', line)
        if not g:
            continue
        for raw in re.findall(r'"([^"]+)"', g.group(1)):
            checked += 1
            root_attr = raw.split('.')[0]
            if root_attr not in fields:
                problems.append((f.relative_to(ROOT), lineno, entity, raw))

print(f"Tekshirildi: {checked} ta attributePath, {len(entities)} ta entity")
if problems:
    print(f"\n❌ MOS KELMAYDIGAN {len(problems)} ta yo'l:")
    for path, ln, ent, attr in problems:
        print(f"   {path}:{ln}  {ent} da '{attr}' maydoni YO'Q")
    sys.exit(1)
print("✅ hammasi entity maydonlariga mos")
