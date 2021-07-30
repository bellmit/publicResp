-- liquibase formatted sql
-- changeset rajendratalekar:create-preauth-prescription-activities-patientpresid-idx failOnError:false

create index preauth_prescription_activities_patient_pres_id_idx on preauth_prescription_activities(patient_pres_id);
	