-- M010: h_building_category klassifikatorini olib tashlash — h_building_type (V023) o'rnini bosdi.
-- h_building_type (35 kod 11-45) bino turini BATAFSIL beradi; h_building_category (V010, 6 qo'pol kod)
-- ortiqcha. Tahlil: frontend ishlatmaydi, biznes-logika yo'q, building_lifecycle category ustunlari
-- hech qachon yozilmagan (o'lik), 0 bino → ma'lumot yo'qolmaydi. V010 prod-live → alohida additive M.

-- 1) building_lifecycle: REPURPOSED (kategoriya-o'zgarishi) ustunlari — o'lik (hech qachon yozilmagan).
--    Avval CHECK'ni tushiramiz (u ustunlarga tayanadi), keyin ustunlar (inline FK ham tushadi).
--    event_type CHECK'даги 'REPURPOSED' qiymati ataylab qoldiriladi (zararsiz — hech qachon yozilmaydi).
ALTER TABLE building_lifecycle DROP CONSTRAINT IF EXISTS chk_bl_repurposed;
ALTER TABLE building_lifecycle DROP COLUMN IF EXISTS previous_category_code;
ALTER TABLE building_lifecycle DROP COLUMN IF EXISTS new_category_code;

-- 2) university_building.category_code (building_type_code bilan almashtirilgan).
--    DROP COLUMN inline FK'ni ham avtomat tushiradi.
DROP INDEX IF EXISTS idx_ub_category;
ALTER TABLE university_building DROP COLUMN IF EXISTS category_code;

-- 3) h_building_category klassifikatori — endi referens yo'q, o'chiriladi.
DROP TABLE IF EXISTS h_building_category;
