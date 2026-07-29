--liquibase formatted sql
-- 002-rename-customers-role-to-status.sql
-- changeset anton.lappa:002

ALTER TABLE customers RENAME COLUMN role TO status;
