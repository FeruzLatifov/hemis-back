#!/usr/bin/env python3
"""
Endpoint konfiguratsiyalarini kategoriyalar bo'yicha alohida fayllarga ajratish (v4)
Oddiyroq regex-based yondashuv - har bir {id: ... } objectni topish
"""

import re
import os
import glob

# all-endpoints.js ni o'qish
with open('endpoints/all-endpoints.js', 'r', encoding='utf-8') as f:
    content = f.read()

# const endpoints = [ va ]; ni olib tashlash
content = re.sub(r'^\s*const endpoints = \[\s*', '', content)
content = re.sub(r'\s*\];\s*$', '', content)

def find_matching_brace(text, start_idx):
    """Ochilgan { uchun yopiladigan } ni topish"""
    count = 0
    i = start_idx
    in_string = False
    string_char = None

    while i < len(text):
        char = text[i]

        # String escape
        if i > 0 and text[i-1] == '\\':
            i += 1
            continue

        # String boshlanishi/tugashi
        if char in '"\'`' and not in_string:
            in_string = True
            string_char = char
        elif char == string_char and in_string:
            in_string = False
            string_char = None

        # String ichida bo'lmasak, brace sanash
        if not in_string:
            if char == '{':
                count += 1
            elif char == '}':
                count -= 1
                if count == 0:
                    return i

        i += 1

    return -1

# Har bir endpoint objectni topish - id: dan boshlanadi
# Pattern: whitespace + { + (whitespace/newline) + id:
endpoint_pattern = re.compile(r'(\s*)\{(\s*\n?\s*)id:\s*\d+')

objects = []
pos = 0
while True:
    match = endpoint_pattern.search(content, pos)
    if not match:
        break

    # { pozitsiyasini topish
    brace_start = content.find('{', match.start())
    if brace_start == -1:
        break

    # Yopiladigan } ni topish
    brace_end = find_matching_brace(content, brace_start)
    if brace_end == -1:
        print(f"Warning: Matching brace not found at position {brace_start}")
        pos = match.end()
        continue

    obj_str = content[brace_start:brace_end+1]
    objects.append(obj_str)
    pos = brace_end + 1

print(f"Jami {len(objects)} endpoint topildi")

# Har bir object uchun category ni olish
def get_category(obj_str):
    match = re.search(r'category:\s*"([^"]+)"', obj_str)
    if match:
        return match.group(1)
    return None

# Kategoriyalar bo'yicha guruhlash
categories = {}
for obj in objects:
    cat = get_category(obj)
    if cat:
        if cat not in categories:
            categories[cat] = []
        categories[cat].append(obj)

print(f"\n{len(categories)} kategoriya topildi:")
for cat in sorted(categories.keys(), key=lambda x: int(x.split('.')[0]) if x.split('.')[0].isdigit() else 0):
    print(f"  - {cat}: {len(categories[cat])} endpoint")

# Fayl nomini yaratish
def to_filename(cat):
    parts = cat.split('.', 1)
    num = parts[0]
    name = parts[1] if len(parts) > 1 else 'unknown'
    name = re.sub(r"['\"]", '', name)
    name = re.sub(r'[^a-zA-Z0-9\s-]', '', name)
    name = name.strip().lower()
    name = re.sub(r'\s+', '-', name)
    if len(name) > 40:
        name = name[:40]
    return f"{num}-{name}.js"

# Eski fayllarni o'chirish
for f in glob.glob('endpoints/[0-9]*.js'):
    os.remove(f)
    print(f"O'chirildi: {f}")

if os.path.exists('endpoints/_index.js'):
    os.remove('endpoints/_index.js')

print("\nYangi fayllar yaratilmoqda...\n")

# Har bir kategoriya uchun fayl yaratish
created_files = []
for cat in sorted(categories.keys(), key=lambda x: int(x.split('.')[0]) if x.split('.')[0].isdigit() else 0):
    num = cat.split('.')[0]
    filename = to_filename(cat)
    filepath = f"endpoints/{filename}"

    # Endpoint objectlarni birlashtirish
    objects_str = ',\n    '.join(categories[cat])

    # Fayl content
    file_content = f"""// {cat} endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_{num} = [
    // ============================================
    // {cat} ({len(categories[cat])} endpoint)
    // ============================================
    {objects_str}
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {{
    module.exports = endpoints_{num};
}}
"""

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(file_content)

    created_files.append({
        'filename': filename,
        'var_name': f"endpoints_{num}",
        'num': num,
        'name': cat,
        'count': len(categories[cat])
    })
    print(f"  {filename} ({len(categories[cat])} endpoint)")

# _index.js yaratish
total_endpoints = sum(f['count'] for f in created_files)
index_content = f"""// Barcha endpoint kategoriyalarini birlashtiruvchi fayl
// Auto-generated - DO NOT EDIT DIRECTLY
// Jami: {len(created_files)} kategoriya, {total_endpoints} endpoint

const endpoints = [
"""

for f in sorted(created_files, key=lambda x: int(x['num']) if x['num'].isdigit() else 0):
    index_content += f"    // {f['name']} ({f['count']} endpoint)\n"
    index_content += f"    ...endpoints_{f['num']},\n"

index_content += f"""];

console.log('Endpoints loaded:', endpoints.length, 'endpoints from {len(created_files)} categories');
"""

with open('endpoints/_index.js', 'w', encoding='utf-8') as f:
    f.write(index_content)

print(f"\n_index.js yaratildi")

# HTML uchun script taglar
print("\n" + "="*60)
print("HTML ga qo'shish uchun script taglar:")
print("="*60 + "\n")

sorted_files = sorted(created_files, key=lambda x: int(x['num']) if x['num'].isdigit() else 0)
for f in sorted_files:
    print(f'    <script src="endpoints/{f["filename"]}"></script>')

print('    <script src="endpoints/_index.js"></script>')

print(f"\n\nJami: {len(created_files)} kategoriya, {total_endpoints} endpoint")
