-- liquibase formatted sql
-- changeset satishl2772:district-master-and-its-transaction-queries
 CREATE TABLE district_master (
    district_id character varying(10) NOT NULL,
    district_name character varying(70) NOT NULL,
    status character(1) DEFAULT 'A'::bpchar,
    state_id character varying(10) NOT NULL
);
 ALTER TABLE district_master
    ADD CONSTRAINT district_master_pkey PRIMARY KEY (district_id);
 CREATE SEQUENCE district_master_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 INSERT INTO unique_number(type_number,start_number,prefix,suffi,pattern) 
VALUES('district_master', 1, 'DT', 'DT', '0000');
 ALTER TABLE city ADD COLUMN district_id  character varying(10);
ALTER TABLE referral ADD COLUMN referal_doctor_area_id  character varying(10);
ALTER TABLE referral ADD COLUMN referal_doctor_city_id  character varying(10);
 ALTER TABLE registration_preferences ADD COLUMN enable_district character(1) NOT NULL DEFAULT 'N';
ALTER TABLE registration_preferences ADD COLUMN show_referrral_doctor_filter character(1) NOT NULL DEFAULT 'N';
ALTER TABLE registration_preferences ADD COLUMN allow_auto_entry_of_area character(1) NOT NULL DEFAULT 'Y';
