--liquibase formatted sql

--changeset durszlak:8
CREATE TABLE AUDIT
(
    AUDIT_ID      BIGSERIAL PRIMARY KEY,
    URL           TEXT NOT NULL,
    USER_UUID     UUID NOT NULL,
    ACTION_TIME   TIMESTAMPTZ NOT NULL
);
