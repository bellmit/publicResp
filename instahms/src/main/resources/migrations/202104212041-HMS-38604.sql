-- liquibase formatted sql
-- changeset manjular:creating_index_for_performace_fix failOnError:false

CREATE INDEX bill_charge_consultation_type_id_idx ON bill_charge(consultation_type_id);
CREATE INDEX receipts_payment_mode_id_idx ON receipts(payment_mode_id);
CREATE INDEX receipts_counter_idx ON receipts(counter);
