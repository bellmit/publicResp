-- liquibase formatted sql
-- changeset manjular:creating-index-on-charge_tax_id-in-bill_charge_claim_tax failOnError:false

create index bill_charge_claim_tax_charge_tax_idx on bill_charge_claim_tax(charge_tax_id);
