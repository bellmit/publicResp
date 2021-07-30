-- liquibase formatted sql
-- changeset mancini2802:created-index-for-other-prescribed-items


CREATE INDEX other_services_prescribed_package_ref_is_null_idx ON other_services_prescribed USING btree (package_ref NULLS FIRST);
CREATE INDEX other_services_prescribed_cancel_status_idx ON other_services_prescribed USING btree (cancel_status);
CREATE INDEX other_services_prescribed_doctor_id_idx ON other_services_prescribed USING btree (doctor_id);
CREATE INDEX other_services_prescribed_service_group_idx ON other_services_prescribed USING btree (service_group);

CREATE INDEX services_prescribed_package_ref_is_null_idx ON services_prescribed USING btree (package_ref NULLS FIRST);
CREATE INDEX services_prescribed_doctor_id_idx ON services_prescribed USING btree(doctor_id);

CREATE INDEX equipment_prescribed_package_ref_is_null_idx ON equipment_prescribed USING btree (package_ref NULLS FIRST);
CREATE INDEX equipment_prescribed_cancel_status_idx ON equipment_prescribed USING btree(cancel_status);
CREATE INDEX equipment_prescribed_doctor_id_idx ON equipment_prescribed USING btree(doctor_id);
CREATE INDEX equipment_prescribed_used_from_idx ON equipment_prescribed USING btree(used_from);
CREATE INDEX equipment_prescribed_used_till_idx ON equipment_prescribed USING btree(used_till);

CREATE INDEX diet_prescribed_package_ref_is_null_idx ON diet_prescribed USING btree (package_ref NULLS FIRST);
CREATE INDEX diet_prescribed_meal_timing_idx ON diet_prescribed USING btree(meal_timing);
CREATE INDEX diet_prescribed_status_idx ON diet_prescribed USING btree(status);
CREATE INDEX diet_prescribed_ordered_by_idx ON diet_prescribed USING btree(ordered_by);
CREATE INDEX diet_prescribed_diet_id_idx ON diet_prescribed USING btree(diet_id);
