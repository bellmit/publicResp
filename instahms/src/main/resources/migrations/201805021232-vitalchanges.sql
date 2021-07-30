-- liquibase formatted sql

-- changeset vishwas07:vitals-for-consultation

CREATE SEQUENCE vitals_default_details_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

DROP TABLE IF EXISTS vitals_default_details;
CREATE TABLE vitals_default_details (
    vital_default_id              integer DEFAULT nextval('vitals_default_details_seq'::regclass) PRIMARY KEY NOT NULL,
    param_id                      integer NOT NULL,
    center_id                     integer NOT NULL,
    dept_id                       varchar(10) NOT NULL,
    mandatory                     character(1) default 'N'
);  
  
ALTER TABLE visit_vitals ADD COLUMN vital_status CHAR(1) NOT NULL DEFAULT 'P';

ALTER TABLE visit_vitals ADD COLUMN finalized_by CHARACTER VARYING(100);

ALTER TABLE visit_vitals ADD COLUMN finalized_date_time timestamp without time zone;
