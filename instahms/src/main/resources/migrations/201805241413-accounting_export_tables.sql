-- liquibase formatted sql
-- changeset anupama-practo:Tables_to_track_the_accounting_export_time_and_status

--CREATE SEQUENCE export_account_names_seq
--	START WITH 1
--	INCREMENT BY 1
--	NO MAXVALUE
--	NO MINVALUE
--	CACHE 1;

--CREATE TABLE export_account_names (

--  id           integer not null default nextval('export_account_names_seq'),
--  account_id   character varying(255), 
--  account_name character varying(255), 
--  account_type character varying(255), 
--  description  character varying(255)
--) ;

--CREATE SEQUENCE accounting_export_log_seq
--	START WITH 1
--	INCREMENT BY 1
--	NO MAXVALUE
--	NO MINVALUE
--	CACHE 1;

--CREATE TABLE accounting_export_log (
--  export_id integer not null,
--  export_date timestamp without time zone
--);


--CREATE SEQUENCE accounting_export_journal_seq
--	START WITH 1
--	INCREMENT BY 1
--	NO MAXVALUE
--	NO MINVALUE
--	CACHE 1;

--CREATE TABLE accounting_export_journal (
--  export_id integer not null,
--  journal_id integer not null,
--  journal_date timestamp without time zone,
--  status integer not null default 0
--);

--CREATE SEQUENCE accounting_export_voucher_seq
--	START WITH 1
--	INCREMENT BY 1
--	NO MAXVALUE
--	NO MINVALUE
--	CACHE 1;

--CREATE TABLE accounting_export_voucher (
--  export_id integer not null,
-- journal_id integer not null,
--  voucher_log_id integer not null,
--  guid character varying,
--  status integer not null default 0
--);


