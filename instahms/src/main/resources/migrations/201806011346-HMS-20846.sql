-- liquibase formatted sql
-- changeset SirishaRL:indexes
CREATE INDEX idx_planned_date_ttd on tooth_treatment_details(date(planned_date));
CREATE INDEX ttd_completed_by_idx on tooth_treatment_details(completed_by);
CREATE INDEX ttd_mr_no_idx on tooth_treatment_details(mr_no);
CREATE INDEX ttd_planned_by on tooth_treatment_details(planned_by);
CREATE INDEX ttd_planned_date_idx on tooth_treatment_details(planned_date);
CREATE INDEX ttd_service_id_idx on tooth_treatment_details(service_id);
CREATE INDEX ttd_service_prescribed_id on tooth_treatment_details(service_prescribed_id);