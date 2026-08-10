--liquibase formatted sql
-- 004-fix-cards-schema.sql
-- changeset anton.lappa:004

ALTER TABLE cards
    DROP COLUMN IF EXISTS direction,
    DROP COLUMN IF EXISTS is_active,
    ADD COLUMN status TEXT NOT NULL;
