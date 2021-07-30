-- liquibase formatted sql
-- changeset amarnath:motherhood-queries failOnError:false validCheckSum: ANY
ALTER TABLE center_integration_details ADD COLUMN http_method varchar(100);
ALTER TABLE center_integration_details ADD COLUMN http_body varchar(1000);
INSERT INTO action_rights (select role_id, 'allow_easyrewardz_coupon_redemption', 'N' from u_role);
