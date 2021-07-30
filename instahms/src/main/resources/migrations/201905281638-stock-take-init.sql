-- liquibase formatted sql
-- changeset anupamamr:initial-tables-for-stock-take-feature

CREATE SEQUENCE physical_stock_take_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE physical_stock_take (
	stock_take_id INTEGER DEFAULT nextval('physical_stock_take_id_seq'::regclass) NOT NULL PRIMARY KEY,
	store_id INTEGER,
	stock_take_number character varying(15) NOT NULL,
	initiated_by character varying(30) NOT NULL,
	initiated_datetime timestamp without time zone DEFAULT NOW(),
	completed_by character varying(30),
	completed_datetime timestamp without time zone,
	reconciled_by character varying(30),
	reconciled_datetime timestamp without time zone,
	approved_by character varying(30),
	approved_datetime timestamp without time zone,
	status character varying(1),
	user_name character varying(30),
	mod_time timestamp without time zone DEFAULT NOW()
);

CREATE SEQUENCE physical_stock_take_detail_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE physical_stock_take_detail (
	stock_take_detail_id INTEGER DEFAULT nextval('physical_stock_take_id_seq'::regclass) NOT NULL PRIMARY KEY,
	stock_take_id INTEGER NOT NULL,
	item_batch_id INTEGER NOT NULL,
	physical_stock_qty NUMERIC(10,4) NULL,
	system_stock_qty NUMERIC(10,4),
	recorded_datetime timestamp without time zone,
	stock_adjustment_reason_id INTEGER,
       	user_name character varying(30),
        mod_time timestamp without time zone DEFAULT NOW()
);

COMMENT ON TABLE physical_stock_take IS
	        '{ "type": "Txn", "comment": "Contains all inventory physical stock takes initiated store-wise." }';
COMMENT ON COLUMN physical_stock_take.status IS 'Physical Stock Take status: (I)nitiated)/Counting in (P)rogress/(C)ompleted/(R)econciled/(A)pproved';
COMMENT ON SEQUENCE physical_stock_take_id_seq IS
		'{ "type": "Txn", "comment": "Sequence ID of the physical stock take" }';

COMMENT ON TABLE physical_stock_take_detail IS
                '{ "type": "Txn", "comment": "Contains all counted stock quantities for items item-batch wise." }';
COMMENT ON SEQUENCE physical_stock_take_detail_id_seq IS
	        '{ "type": "Txn", "comment": "Sequence ID of the physical stock take items" }';

CREATE TABLE physical_stock_take_audit_log (
	log_id bigint DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
    	stock_take_id integer,
    	store_id  integer,
    	user_name character varying,
	mod_time timestamp without time zone DEFAULT now() NOT NULL,
    	operation character varying,
    	field_name character varying,
    	old_value character varying,
    	new_value character varying
);

CREATE TABLE physical_stock_take_detail_audit_log (
	log_id bigint DEFAULT nextval('audit_logid_sequence'::regclass) NOT NULL,
	stock_take_detail_id integer,
	stock_take_id  integer,
	item_batch_id integer,
	user_name character varying,
	mod_time timestamp without time zone DEFAULT now() NOT NULL,
	operation character varying,
	field_name character varying,
	old_value character varying,
	new_value character varying
);

COMMENT ON table physical_stock_take_audit_log is '{ "type": "Txn", "comment": "Stock Take audit log" }';
COMMENT ON table physical_stock_take_detail_audit_log is '{"type": "Txn", "comment": "Stock Take Item audit log"}';

