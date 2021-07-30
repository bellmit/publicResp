-- liquibase formatted sql
-- changeset tejakilaru:diag-template-changes-form-prescriptions failOnError:false

INSERT INTO pack_master (package_id, package_name, package_code, description, type, service_sub_group_id, valid_from_date, valid_to_date, package_active, approval_process_by, package_type)
select package_id, package_name, package_code, description ,'T' as type, service_sub_group_id, valid_from as valid_from_date , valid_till as valid_to_date, 'I' as package_active, modified_by as approval_process_by , 'd'
FROM packages WHERE visit_applicability='o';

INSERT INTO package_componentdetail (pack_ob_id, package_id, activity_id, activity_type, activity_qty, activity_units, activity_remarks, display_order, consultation_type_id, charge_head)
SELECT pc.package_content_id::text AS pack_ob_id, pc.package_id, pc.activity_id, pc.activity_type, pc.activity_qty, pc.activity_qty_uom AS activity_units, pc.activity_remarks, pc.display_order, pc.consultation_type_id, pc.charge_head 
FROM package_contents pc JOIN pack_master pm using (package_id) where pm.type='T';

INSERT INTO package_center_master (package_center_id, pack_id, center_id, status)
SELECT cpa.center_package_id AS package_center_id, cpa.package_id AS pack_id, cpa.center_id, 'A' AS status
FROM center_package_applicability cpa JOIN pack_master pm ON (pm.package_id = cpa.package_id) WHERE pm.type='T' ;

UPDATE package_componentdetail pc SET activity_description = (SELECT d.test_name FROM diagnostics d where d.test_id=pc.activity_id) 
WHERE exists (select 1 from package_componentdetail pcd join pack_master pm on (pm.package_id=pcd.package_id AND pm.type='T') where pcd.pack_ob_id=pc.pack_ob_id);
