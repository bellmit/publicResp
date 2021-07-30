-- liquibase formatted sql
-- changeset dilipsaikumar:default-migratin_dept_package_applicability

INSERT INTO dept_package_applicability
	(package_id, dept_id, created_by, created_at, modified_by, modified_at)
	SELECT package_id,'*' as dept_id,'InstaAdmin' as created_by,NOW() as created_at,NULL as modified_by,NULL as modified_at FROM temp_pack_master_packages_mapping 
	where package_id not in (select package_id from dept_package_applicability);