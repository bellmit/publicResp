-- liquibase formatted sql
-- changeset allabakash:Bill-ip-deposit-set-off-old-column

ALTER TABLE bill ADD COLUMN ip_deposit_set_off_old VARCHAR(100) NOT NULL DEFAULT '0'  ;

UPDATE bill SET ip_deposit_set_off_old = ip_deposit_set_off WHERE ip_deposit_set_off > 0;

COMMENT ON COLUMN bill.ip_deposit_set_off_old IS 'ip_deposit_set_off column backup for old value';

