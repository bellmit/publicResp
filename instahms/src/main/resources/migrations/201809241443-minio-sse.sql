-- liquibase formatted sql
-- changeset adityabhatia02:minio_sse_table_creation

CREATE TABLE minio_sse (
   onerow_id bool PRIMARY KEY DEFAULT TRUE,
   sse_key varchar(100) NOT NULL,
   CONSTRAINT onerow_uni CHECK (onerow_id)
);

SELECT comment_on_table_or_sequence_if_exists('minio_sse', true, 'Master', 'Minio sse key');

