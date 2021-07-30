-- liquibase formatted sql
-- changeset adityabhatia02:rename-primary-key-confidentiality-grp-master

ALTER TABLE confidentiality_grp_master RENAME COLUMN id to confidentiality_grp_id;
ALTER TABLE confidentiality_grp_master ADD COLUMN created_by character varying(30);
ALTER TABLE confidentiality_grp_master ADD COLUMN modified_by character varying(30);
ALTER TABLE confidentiality_grp_master ADD COLUMN modified_at timestamp without time zone default now();

ALTER TABLE confidentiality_grp_master ADD CONSTRAINT fk_confidentiality_grp_master_created_by FOREIGN KEY (created_by)
REFERENCES u_user(emp_username);
ALTER TABLE confidentiality_grp_master ADD CONSTRAINT fk_confidentiality_grp_master_modified_by FOREIGN KEY (modified_by)
REFERENCES u_user(emp_username);

ALTER TABLE user_confidentiality_association ADD COLUMN created_by character varying(30);
ALTER TABLE user_confidentiality_association ADD COLUMN modified_by character varying(30);
ALTER TABLE user_confidentiality_association ADD COLUMN modified_at timestamp without time zone default now();

ALTER TABLE user_confidentiality_association ADD CONSTRAINT fk_user_confidentiality_association_created_by FOREIGN KEY (created_by)
REFERENCES u_user(emp_username);
ALTER TABLE user_confidentiality_association ADD CONSTRAINT fk_user_confidentiality_association_modified_by FOREIGN KEY (modified_by)
REFERENCES u_user(emp_username);