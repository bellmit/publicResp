-- liquibase formatted sql
-- changeset abhishekv31:create_index_for_bill_charge_adjustment failOnError:false
create index bca_bill_no_idx on bill_charge_adjustment(bill_no);
create index bca_charge_adjustment_id_idx on bill_charge_adjustment(bill_charge_adjustment_id);
create index bca_charge_id_idx on bill_charge_adjustment(charge_id);

