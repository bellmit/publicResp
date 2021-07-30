-- liquibase formatted sql
-- changeset sanjana:changing-unique-appt-constraint

DROP INDEX unique_appt;
CREATE UNIQUE INDEX unique_appt ON scheduler_appointments USING btree (unique_appt_ind, prim_res_id, appointment_time,res_sch_id);