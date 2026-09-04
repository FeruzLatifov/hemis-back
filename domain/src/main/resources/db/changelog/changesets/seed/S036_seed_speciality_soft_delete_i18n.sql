-- =====================================================
-- S036: SEED TRANSLATIONS — mutaxassislik SOFT DELETE (M013) + tiklash
-- =====================================================
-- Author: hemis-team
-- Date: 2026-08-25
-- Purpose:
--   M013 h_speciality o'chirishini JISMONIY -> SOFT ga o'tkazdi. Frontend uchun yangi matnlar:
--     • O'chirish dialogi: "Bu amalni qaytarib bo'lmaydi" (S006, prodda) endi YOLG'ON —
--       o'rniga "hamma joydan yashiriladi, keyinchalik tiklash mumkin".
--     • "O'chirilgan mutaxassisliklar" savati: sarlavha, "Tiklash" tugmasi, bo'sh holat,
--       "O'chirilgan sana"/"Kim o'chirdi" ustunlari, muvaffaqiyat toast'i.
--     • Ikkita yangi 422 qoida kodi matni: SPECIALITY_RESTORE_IDENTITY_TAKEN va
--       SPECIALITY_RESTORE_PARENT_DELETED.
--   Jami: 9 ta YANGI kalit.
--
--   YANGI seed, chunki S006/S010/S032..S035 prodda (central_hemis) allaqachon qo'llangan —
--   qo'llangan changeset TAHRIRLANMAYDI. system_message — yagona haqiqat manbai;
--   `sync:translations` frontend JSON'larini (en/oz/ru/uz) shu yerdan qayta yozadi, ya'ni
--   seedsiz kalitlar keyingi sinxronda JIMGINA yo'qoladi.
-- Pattern: S035 (5 argumentli _seed_msg helper — S006 da aniqlangan; en-US = kalitning o'zi).
-- Safety: _seed_msg ichida ON CONFLICT (message_key) DO UPDATE — idempotent.
-- =====================================================

DO $$
BEGIN

PERFORM _seed_msg('label',  'Deleted specialities', 'O''chirilgan mutaxassisliklar', 'Ўчирилган мутахассисликлар', 'Удалённые специальности');
PERFORM _seed_msg('label',  'Restore', 'Tiklash', 'Тиклаш', 'Восстановить');
PERFORM _seed_msg('label',  'Speciality restored', 'Mutaxassislik tiklandi', 'Мутахассислик тикланди', 'Специальность восстановлена');
PERFORM _seed_msg('label',  'Nothing deleted yet', 'Hozircha o''chirilgan yozuv yo''q', 'Ҳозирча ўчирилган ёзув йўқ', 'Пока ничего не удалено');
PERFORM _seed_msg('label',  'Deleted at', 'O''chirilgan sana', 'Ўчирилган сана', 'Дата удаления');
PERFORM _seed_msg('label',  'Deleted by', 'Kim o''chirdi', 'Ким ўчирди', 'Кем удалено');
PERFORM _seed_msg('label',  'The speciality is hidden everywhere and can be restored later', 'Mutaxassislik hamma joydan yashiriladi, keyinchalik tiklash mumkin', 'Мутахассислик ҳамма жойдан яширилади, кейинчалик тиклаш мумкин', 'Специальность скрывается везде, позже её можно восстановить');
PERFORM _seed_msg('label',  'A live speciality already uses this code and name', 'Bu kod va nom bilan tirik mutaxassislik allaqachon mavjud', 'Бу код ва ном билан тирик мутахассислик аллақачон мавжуд', 'Действующая специальность уже использует этот код и название');
PERFORM _seed_msg('label',  'The parent speciality is deleted — restore the parent first', 'Ota mutaxassislik o''chirilgan — avval uni tiklang', 'Ота мутахассислик ўчирилган — аввал уни тикланг', 'Родительская специальность удалена — сначала восстановите её');

END $$;
