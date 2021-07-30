-- liquibase formatted sql
-- changeset rajendratalekar:create-patient-search-token-begins-with-index

create index patient_search_token_begins_with_index ON patient_search_tokens(token varchar_pattern_ops, reversed, entity);
