-- liquibase formatted sql
-- changeset rajendratalekar:create-services-prescribed-docprescid-idx failOnError:false

create index services_prescribed_doc_presc_id_idx on services_prescribed(doc_presc_id);