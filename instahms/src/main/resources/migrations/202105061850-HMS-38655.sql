-- liquibase formatted sql
-- changeset manjular:index-on-bill-charge-claim-claim-id-charge-id failOnError:false

create index bill_charge_claim_charge_id_claim_idx on bill_charge_claim(claim_id, charge_id);

