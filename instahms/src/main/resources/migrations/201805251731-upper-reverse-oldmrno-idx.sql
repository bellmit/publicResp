-- liquibase formatted sql
-- changeset satishl2772:adding-upper-reverse-oldmrno-idx

CREATE INDEX patient_details_oldmrno_upper_ends_with_idx ON patient_details USING btree (UPPER(reverse((COALESCE(oldmrno, ''::character varying))::text)) varchar_pattern_ops);
