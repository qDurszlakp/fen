--liquibase formatted sql

--changeset mood:12
ALTER TABLE AUDIT ADD COLUMN STATUS_CODE INT;
