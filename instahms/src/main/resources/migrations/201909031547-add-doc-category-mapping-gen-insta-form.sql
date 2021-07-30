-- liquibase formatted sql
-- changeset pranays:<HMS-32961-added-doc-category-mapping-gen-insta-form>

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_GEN_INSTA_FORM', 'CLN');