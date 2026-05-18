-- =====================================================
-- V016 ROLLBACK: WEBHOOK INFRASTRUCTURE
-- =====================================================
-- Author: hemis-team
-- Date: 2026-05-13
-- Purpose: V016 da yaratilgan webhook_target + webhook_delivery_log
--          jadvallarini va indekslarini olib tashlash.
--
-- DIQQAT: Bu rollback PRODUCTION'da bajarilsa:
--   - Barcha webhook target'lar (224 OTM URL'lari) o'chadi
--   - Delivery log audit (event history) yo'qoladi
--   - Outbound event push to'xtaydi
-- =====================================================

-- 1. webhook_delivery_log indekslari va jadvali
DROP INDEX IF EXISTS idx_webhook_delivery_university;
DROP INDEX IF EXISTS idx_webhook_delivery_dlq;
DROP INDEX IF EXISTS idx_webhook_delivery_retry;
DROP INDEX IF EXISTS idx_webhook_delivery_target_status;
DROP INDEX IF EXISTS idx_webhook_delivery_event;
DROP INDEX IF EXISTS uq_webhook_delivery_event_target_attempt;

DROP TABLE IF EXISTS webhook_delivery_log;

-- 2. webhook_target indekslari va jadvali
DROP INDEX IF EXISTS idx_webhook_target_active;
DROP INDEX IF EXISTS uq_webhook_target_university_code;

DROP TABLE IF EXISTS webhook_target;
