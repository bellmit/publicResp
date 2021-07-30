-- liquibase formatted sql
-- changeset adeshatole:add-indices-for-package-report


CREATE index bed_operation_package_ref_idx ON bed_operation_schedule USING btree(package_ref);
CREATE index services_prescribed_operation_ref_idx ON services_prescribed USING btree(operation_ref);                     
CREATE index bed_operation_schedule_prescribed_id_idx ON bed_operation_schedule USING btree(prescribed_id);