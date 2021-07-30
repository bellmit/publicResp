-- liquibase formatted sql
-- changeset javalkarvinay:cons-uom-value-update-for-non-hospital-items

ALTER TABLE patient_other_prescriptions ADD column cons_uom_id INTEGER;
ALTER TABLE patient_other_prescriptions_audit ADD column cons_uom_id INTEGER;
ALTER TABLE doctor_other_favourites ADD column cons_uom_id INTEGER;

INSERT INTO consumption_uom_master (consumption_uom,status) 
    SELECT DISTINCT consumption_uom,status FROM 
        (SELECT DISTINCT consumption_uom,'A' AS status FROM patient_other_prescriptions WHERE consumption_uom is not null AND consumption_uom != '' AND consumption_uom NOT IN (SELECT consumption_uom FROM consumption_uom_master)
        UNION
        SELECT DISTINCT consumption_uom,'A' AS status FROM doctor_other_favourites WHERE consumption_uom is not null AND consumption_uom != '' AND consumption_uom NOT IN (SELECT consumption_uom FROM consumption_uom_master)
    ) as foo;

UPDATE patient_other_prescriptions pop SET cons_uom_id = c.cons_uom_id FROM consumption_uom_master c WHERE pop.consumption_uom = c.consumption_uom;
UPDATE doctor_other_favourites dof SET cons_uom_id = c.cons_uom_id FROM consumption_uom_master c WHERE dof.consumption_uom = c.consumption_uom;

ALTER TABLE patient_other_prescriptions RENAME consumption_uom TO obsolete_consumption_uom;
ALTER TABLE patient_other_prescriptions_audit RENAME consumption_uom TO obsolete_consumption_uom;
ALTER TABLE doctor_other_favourites RENAME consumption_uom TO obsolete_consumption_uom;

