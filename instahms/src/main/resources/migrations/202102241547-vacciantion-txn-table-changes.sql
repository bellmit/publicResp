-- liquibase formatted sql
-- changeset riyapoddar-13:Adding-additional-columns-in-patient-vaccination

ALTER TABLE patient_vaccination ADD COLUMN medicine_id INTEGER;
ALTER TABLE patient_vaccination ADD FOREIGN KEY(medicine_id) REFERENCES store_item_details(medicine_id);
ALTER TABLE patient_vaccination ADD COLUMN vaccine_category_id INTEGER;
ALTER TABLE patient_vaccination ADD FOREIGN KEY(vaccine_category_id) REFERENCES vaccine_category_master(vaccine_category_id);
ALTER TABLE patient_vaccination ADD COLUMN medicine_quantity INTEGER;
ALTER TABLE patient_vaccination ADD COLUMN cons_uom_id INTEGER;
ALTER TABLE patient_vaccination ADD FOREIGN KEY(cons_uom_id) REFERENCES consumption_uom_master(cons_uom_id);
ALTER TABLE patient_vaccination ADD COLUMN site_id INTEGER;
ALTER TABLE patient_vaccination ADD FOREIGN KEY(site_id) REFERENCES iv_infusionsites(id);
ALTER TABLE patient_vaccination ADD COLUMN route_of_admin INTEGER;
ALTER TABLE patient_vaccination ADD FOREIGN KEY(route_of_admin) REFERENCES medicine_route(route_id);
ALTER TABLE patient_vaccination ALTER COLUMN vaccination_datetime TYPE timestamp without time zone;
