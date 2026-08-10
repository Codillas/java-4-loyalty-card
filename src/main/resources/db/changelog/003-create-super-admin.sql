--liquibase formatted sql

--changeset anton:003-insert-super-admin

INSERT INTO admins (
    id,
    name,
    email,
    phone_number,
    password,
    role,
    status,
    created_at,
    updated_at
)
VALUES (
           '9f0e3d06-2976-4b86-906c-79c4a513c27e',
           'Super Admin',
           'super@mail.com',
           '+491234565430',
           '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- admin123
           'SUPER_ADMIN',
           'ACTIVE',
           NOW(),
           NOW()
       );