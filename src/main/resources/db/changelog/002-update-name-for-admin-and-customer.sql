-- 002-rename-name-columns.sql
-- Customers & Admins: drop last_name, rename first_name -> name
--liquibase formatted sql
-- changeset anton.lappa


ALTER TABLE customers
    DROP COLUMN last_name;

ALTER TABLE customers
    RENAME COLUMN first_name TO name;

ALTER TABLE admins
    DROP COLUMN last_name;

ALTER TABLE admins
    RENAME COLUMN first_name TO name;