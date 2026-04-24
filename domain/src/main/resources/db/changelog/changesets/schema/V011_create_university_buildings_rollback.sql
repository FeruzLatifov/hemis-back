-- V014 rollback — dependency reverse tartibida
-- Lifecycle avval (building'ga FK), keyin building, keyin classifier'lar
DROP TABLE IF EXISTS building_lifecycle CASCADE;
DROP TABLE IF EXISTS university_building CASCADE;
DROP TABLE IF EXISTS roof_type CASCADE;
DROP TABLE IF EXISTS construction_material CASCADE;
DROP TABLE IF EXISTS building_category CASCADE;
