-- =====================================================
-- M012 ROLLBACK: partial unique -> to'liq UNIQUE
-- =====================================================
-- DIQQAT: bu rollback MUVAFFAQIYATSIZ bo'lishi MUMKIN va bu ATAYLAB shunday.
--
-- M012'dan keyin o'chirilgan hisobning client_id'si qayta ishlatilgan bo'lishi mumkin
-- (aynan shu buning maqsadi edi). Unda jadvalda bir xil client_id'li ikkita qator turadi:
-- biri o'chirilgan, biri tirik. To'liq UNIQUE'ni tiklash uchun ulardan birini YO'Q QILISH kerak.
--
-- Ma'lumotni jimgina o'chirib yubormaymiz: rollback aniq xabar bilan to'xtaydi va operator
-- qaysi qatorlarni qo'lda hal qilishini o'zi ko'radi. "Avtomatik tozalash" bu yerda
-- qaytarib bo'lmaydigan zarar demakdir.
-- =====================================================

DO $$
DECLARE
    v_dupes integer;
    v_row   record;
BEGIN
    SELECT count(*) INTO v_dupes
      FROM (SELECT client_id FROM oauth_client GROUP BY client_id HAVING count(*) > 1) d;

    IF v_dupes > 0 THEN
        RAISE NOTICE 'M012 rollback: % ta takrorlanuvchi client_id topildi —', v_dupes;
        FOR v_row IN
            SELECT client_id, count(*) AS n,
                   count(*) FILTER (WHERE deleted_at IS NULL) AS tirik
              FROM oauth_client GROUP BY client_id HAVING count(*) > 1 ORDER BY client_id
        LOOP
            RAISE NOTICE '   client_id=% jami=% tirik=%', v_row.client_id, v_row.n, v_row.tirik;
        END LOOP;
        RAISE EXCEPTION 'M012 rollback TO''XTATILDI: yuqoridagi client_id lar to''liq UNIQUE''ni buzadi. '
                        'Ortiqcha (odatda o''chirilgan) qatorlarni qo''lda hal qiling, so''ng rollbackni takrorlang.';
    END IF;
END $$;

DROP INDEX IF EXISTS uq_oauth_client_client_id_live;

ALTER TABLE oauth_client
    ADD CONSTRAINT oauth_client_client_id_key UNIQUE (client_id);
