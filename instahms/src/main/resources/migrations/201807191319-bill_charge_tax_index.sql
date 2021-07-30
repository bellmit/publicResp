-- liquibase formatted sql
-- changeset SirishaRL:intoducing_index_on_charge_id_on_bill_charge_tax_table

CREATE INDEX idx_bill_charge_tax_charge_id ON bill_charge_tax USING btree(charge_id);