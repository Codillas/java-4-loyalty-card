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
           '$2y$10$8FXtZX8uxWpWLKhK781ZMe1.z.dAR1Rnr9WsJMc21uJsTifqzlHQW', -- admin123
           'SUPER_ADMIN',
           'ACTIVE',
           NOW(),
           NOW()
       );