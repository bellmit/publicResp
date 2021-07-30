-- liquibase formatted sql
-- changeset abhishekv31:index_tax_for_tax_related_tables failOnError:false
create index sctd_item_subgroup_id_idx on sales_claim_tax_details(item_subgroup_id);
