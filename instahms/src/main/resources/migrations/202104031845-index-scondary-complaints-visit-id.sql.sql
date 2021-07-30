-- liquibase formatted sql
-- changeset rajendratalekar:index-secondary-complaints-visit-id failOnError:false

create index secondary_complaints_visit_id_idx on secondary_complaints(visit_id);
