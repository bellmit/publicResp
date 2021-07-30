-- liquibase formatted sql
-- changeset manjular:index_on_bill_column_selfpay_batch_id failOnError:false

CREATE INDEX bill_selfpay_batch_id_idx ON bill(selfpay_batch_id);
