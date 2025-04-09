ALTER TABLE users
    DROP COLUMN first_name,
    DROP COLUMN last_name;

ALTER TABLE users
    ADD COLUMN "name" VARCHAR(100) NOT NULL;