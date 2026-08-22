#!/usr/bin/env python3
"""Native query natijasidagi xavfli vaqt kastlarini tekshiradi.

NEGA: entityManager.createNativeQuery(...) Object[] qaytaradi va `timestamp`
ustuni uchun QAYSI Java tipi kelishi Hibernate versiyasiga bog'liq:
Hibernate 5 -> java.sql.Timestamp, Hibernate 6+ -> java.time.LocalDateTime.
Kast runtime amali, shuning uchun kompilyator ham, testlar ham tutmaydi.

Ikki xil zarar:
  1. `(java.sql.Timestamp) row[i]`  -> ClassCastException -> HTTP 500
     (Sentry MINISTRY-HEMIS-BACK-12/13: fakultet va kafedra tafsiloti)
  2. `toLocalDate(Object)` yordamchisi LocalDateTime holatini qoplamasa
     -> JIMGINA null: xato yo'q, Sentry izi yo'q, sana UI'dan yo'qoladi.

Yechim ikkalasi uchun ham bitta: uz.hemis.common.util.JdbcTemporal.
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
HELPER = 'uz.hemis.common.util.JdbcTemporal'

# `(java.sql.Timestamp) row[3]` / `(Timestamp) r[0]` kabi to'g'ridan-to'g'ri kast
DIRECT_CAST = re.compile(r'\(\s*(?:java\.sql\.)?Timestamp\s*\)\s*[A-Za-z_]\w*\s*\[')

# Lokal `private static LocalDate toX(Object ...) { ... }` yordamchisi
LOCAL_HELPER = re.compile(
    r'private\s+static\s+(?:LocalDate|LocalDateTime)\s+(\w+)\s*\(\s*Object[^)]*\)\s*\{(.*?)\n    \}',
    re.S)


def main() -> int:
    problems = []
    for path in sorted(ROOT.glob('*/src/main/java/**/*.java')):
        if path.name == 'JdbcTemporal.java':
            continue
        text = path.read_text(encoding='utf-8', errors='replace')
        rel = path.relative_to(ROOT)

        for n, line in enumerate(text.splitlines(), 1):
            if DIRECT_CAST.search(line) and not line.lstrip().startswith(('*', '//')):
                problems.append((rel, n,
                                 'to\'g\'ridan-to\'g\'ri (java.sql.Timestamp) kasti — '
                                 'Hibernate 6 LocalDateTime qaytaradi, bu ClassCastException beradi'))

        for m in LOCAL_HELPER.finditer(text):
            name, body = m.group(1), m.group(2)
            if HELPER.rsplit('.', 1)[-1] in body:
                continue                       # JdbcTemporal'ga delegatsiya qilingan — OK
            if 'instanceof' not in body:
                continue                       # kast-asosli emas, boshqa mantiq
            if 'instanceof LocalDateTime' in body:
                continue                       # holat qoplangan — OK
            n = text[:m.start()].count('\n') + 1
            problems.append((rel, n,
                             f'`{name}(Object)` LocalDateTime holatini qoplamaydi — '
                             f'timestamp ustuni JIMGINA null bo\'ladi. {HELPER} ga delegatsiya qiling'))

    if problems:
        print('🔴 Xavfli native-query vaqt kastlari:')
        for rel, n, msg in problems:
            print(f'  {rel}:{n}\n      {msg}')
        return 1

    print('✅ native-query vaqt kastlari xavfsiz')
    return 0


if __name__ == '__main__':
    sys.exit(main())
