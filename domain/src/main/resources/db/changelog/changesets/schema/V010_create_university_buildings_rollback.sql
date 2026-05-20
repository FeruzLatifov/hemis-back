-- V010 rollback — dependency reverse tartibida
-- Lifecycle avval (building'ga FK), keyin building, keyin classifier'lar
DROP TABLE IF EXISTS building_lifecycle CASCADE;
DROP TABLE IF EXISTS university_building CASCADE;
DROP TABLE IF EXISTS h_roof_type CASCADE;
DROP TABLE IF EXISTS h_construction_material CASCADE;
DROP TABLE IF EXISTS h_building_category CASCADE;
