--liquibase formatted sql
-- 001-create-schema.sql
-- changeset anton.lappa:001


CREATE TABLE customers
(
    id           UUID PRIMARY KEY,
    name         TEXT        NOT NULL,
    email        TEXT        NOT NULL UNIQUE,
    password     TEXT        NOT NULL,
    phone_number TEXT        NOT NULL,
    role         TEXT        NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL
);

CREATE TABLE admins
(
    id           UUID PRIMARY KEY,
    name         TEXT        NOT NULL,
    email        TEXT        NOT NULL UNIQUE,
    password     TEXT        NOT NULL,
    role         TEXT        NOT NULL,
    status       TEXT        NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL,
    phone_number TEXT        NOT NULL

);

CREATE TABLE cards
(
    id          UUID PRIMARY KEY,
    customer_id UUID        NOT NULL UNIQUE REFERENCES customers (id),
    balance     INT         NOT NULL,
    direction   TEXT        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    is_active   BOOLEAN     NOT NULL
);

CREATE TABLE transactions
(
    id         UUID PRIMARY KEY,
    admin_id   UUID REFERENCES admins (id),
    card_id    UUID REFERENCES cards (id),
    direction  TEXT        NOT NULL,
    amount     INT         NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    status     TEXT        NOT NULL,
    note       TEXT
);