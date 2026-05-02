-- =====================================================
-- V005: full_name ustunini activity_log ga qo'shish
-- =====================================================
-- V001 dagi CREATE TABLE IF NOT EXISTS allaqachon yaratilgan
-- jadvalga ustun qo'shmaydi. Eski deploylardagi instans uchun
-- idempotent ALTER kerak.
-- =====================================================

ALTER TABLE activity_log ADD COLUMN IF NOT EXISTS full_name VARCHAR(255);
