-- liquibase formatted sql
-- changeset sindur:alter-physical-stock-take-pk
ALTER TABLE physical_stock_take_detail
DROP CONSTRAINT pstd_physical_stock_take_fk,
ALTER COLUMN stock_take_id TYPE VARCHAR(15);

ALTER TABLE physical_stock_take
ALTER COLUMN stock_take_id drop default,
ALTER COLUMN stock_take_id TYPE VARCHAR(15),
DROP COLUMN stock_take_number;

ALTER TABLE physical_stock_take_detail
ADD CONSTRAINT pstd_physical_stock_take_fk
FOREIGN KEY (stock_take_id) REFERENCES physical_stock_take;

ALTER SEQUENCE physical_stock_take_id_seq RENAME to physical_stock_take_seq;
ALTER SEQUENCE physical_stock_take_detail_id_seq RENAME to physical_stock_take_detail_seq;

ALTER TABLE physical_stock_take_detail 
ALTER COLUMN stock_take_detail_id set DEFAULT nextval('physical_stock_take_detail_seq'::regclass);

ALTER TABLE physical_stock_take_audit_log 
ALTER COLUMN stock_take_id TYPE VARCHAR(15);

ALTER TABLE physical_stock_take_detail_audit_log
ALTER COLUMN stock_take_id TYPE VARCHAR(15);



