-- liquibase formatted sql
-- changeset sanjana:added-center-timezone splitStatements:false

ALTER TABLE hospital_center_master ADD COLUMN center_timezone varchar(50);
update hospital_center_master set center_timezone = (SELECT current_setting('TIMEZONE'));

CREATE OR REPLACE FUNCTION get_timezone() RETURNS varchar AS
$$ 
BEGIN
RETURN (SELECT current_setting('TIMEZONE')); 
END;
$$ LANGUAGE plpgsql;

ALTER TABLE hospital_center_master ALTER COLUMN center_timezone SET DEFAULT get_timezone();
