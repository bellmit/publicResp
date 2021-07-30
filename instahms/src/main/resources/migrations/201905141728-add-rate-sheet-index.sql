-- liquibase formatted sql
-- changeset mohamedanees:adding-rate-sheet-creation-indexes

CREATE INDEX package_charges_org_id_idx ON package_charges(org_id);
CREATE INDEX dyna_package_category_limits_org_id_idx ON dyna_package_category_limits(org_id);