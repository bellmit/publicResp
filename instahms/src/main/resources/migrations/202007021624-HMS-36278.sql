-- liquibase formatted sql
-- changeset manjular:adding-colums-dyna-package-excluded-to-bill-charge failOnError:false

ALTER TABLE bill_charge ADD COLUMN dyna_package_excluded character varying(1);

