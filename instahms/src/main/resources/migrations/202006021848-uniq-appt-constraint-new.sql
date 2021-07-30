-- liquibase formatted sql
-- changeset raeshmika:<fixing-unique-appointment-constraint-for-recurrence-appointments>

DROP INDEX unique_appt;
CREATE UNIQUE INDEX unique_appt ON scheduler_appointments USING btree (unique_appt_ind, prim_res_id, appointment_time, res_sch_id, mr_no, contact_id);