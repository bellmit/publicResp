-- liquibase formatted sql
-- changeset satishl2772:adding-indexes-to-improve-performance-HMS-20764

CREATE INDEX scheduler_appointment_mr_no_idx ON scheduler_appointments(mr_no);
CREATE INDEX scheduler_appointment_center_id_idx ON scheduler_appointments(center_id);
CREATE INDEX scheduler_appointment_date_idx ON scheduler_appointments(date(appointment_time));
