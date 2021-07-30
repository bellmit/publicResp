-- liquibase formatted sql
-- changeset sandeep:remittance-reconciliation-migrations

INSERT INTO modules_activated VALUES ('mod_remittance_reconciliation', 'N');


-- have to check how this will fit in new model
CREATE TABLE bill_remittance (
	bill_no VARCHAR(15) NOT NULL,
	remittance_id INTEGER NOT NULL,
	remarks VARCHAR(100),
	writeOff boolean DEFAULT false,
	status VARCHAR(1) DEFAULT 'D',
	PRIMARY KEY (bill_no, remittance_id)
);

COMMENT ON TABLE bill_remittance IS '{ "type": "Txn", "comment": "Holds information regarding bill and a remittance" }';

ALTER TABLE payment_mode_master ADD COLUMN sponsor_applicable VARCHAR(1) DEFAULT 'N';
UPDATE payment_mode_master SET sponsor_applicable = 'Y' WHERE mode_id IN (3,4,5);
