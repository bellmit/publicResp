-- liquibase formatted sql
-- changeset sanjana.goyal:custom-fields-master-migration splitStatements:false


-- blood group
CREATE OR REPLACE FUNCTION migrate_blood_group_value()
	RETURNS void AS $$
DECLARE
	var_custom_field_name character varying(50);
BEGIN
	select name from reg_custom_fields where display_type='dropdown' and upper(label) in ('BLOOD GROUP','BLOOD TYPE','GOLONGAN DARAH') into var_custom_field_name;

	IF var_custom_field_name != '' THEN
		EXECUTE FORMAT('update patient_details set blood_group_id = (select blood_group_id from blood_group_master where blood_group_name=%L)
		 where  %I in (%L,%L,%L,%L,%L,%L) ', 'A+',var_custom_field_name,'A +VE','A +ve','A POSITIVE','A Positive','A+','A+ve');
		EXECUTE FORMAT('update patient_details set blood_group_id = (select blood_group_id from blood_group_master where blood_group_name=%L)
				 where  %I in (%L,%L,%L,%L,%L) ', 'A-',var_custom_field_name,'A -VE', 'A NEGATIVE', 'A Negative', 'A-','A-ve');

		EXECUTE FORMAT('update patient_details set blood_group_id = (select blood_group_id from blood_group_master where blood_group_name=%L)
				 where  %I in (%L,%L,%L) ', 'A1-',var_custom_field_name,'A1 -ve','A1 Negative','A1-ve');
		EXECUTE FORMAT('update patient_details set blood_group_id = (select blood_group_id from blood_group_master where blood_group_name=%L)
				 where  %I in (%L,%L,%L) ', 'A1+',var_custom_field_name,'A1 +ve','A1 Positive','A1+ve');

		EXECUTE FORMAT('update patient_details set blood_group_id = (select blood_group_id from blood_group_master where blood_group_name=%L)
				 where  %I in (%L,%L) ', 'A1B-',var_custom_field_name,'A1B -ve','A1B Negative');
		EXECUTE FORMAT('update patient_details set blood_group_id = (select blood_group_id from blood_group_master where blood_group_name=%L)
				 where  %I in (%L,%L,%L,%L) ', 'A1B+',var_custom_field_name,'A1B +ve','A1B Positive','A1B+','A1B+ve');

		EXECUTE FORMAT('update patient_details set blood_group_id = (select blood_group_id from blood_group_master where blood_group_name=%L)
				 where  %I in (%L,%L) ', 'A2-',var_custom_field_name,'A2 -ve','A2 Negative');
		EXECUTE FORMAT('update patient_details set blood_group_id = (select blood_group_id from blood_group_master where blood_group_name=%L)
				 where  %I in (%L,%L) ', 'A2+',var_custom_field_name,'A2 +ve','A2 Positive');

		EXECUTE FORMAT('update patient_details set blood_group_id = (select blood_group_id from blood_group_master where blood_group_name=%L)
				 where  %I in (%L,%L) ', 'A2B-',var_custom_field_name,'A2B -ve','A2B Negative');
		EXECUTE FORMAT('update patient_details set blood_group_id = (select blood_group_id from blood_group_master where blood_group_name=%L)
				 where  %I in (%L,%L) ', 'A2B+',var_custom_field_name,'A2B +ve','A2B Positive');

		EXECUTE FORMAT('update patient_details set blood_group_id = (select blood_group_id from blood_group_master where blood_group_name=%L)
				 where  %I in (%L,%L,%L,%L,%L) ', 'AB+',var_custom_field_name,'AB POSITIVE','AB Positive','AB+','AB+ve','AB +VE');
		EXECUTE FORMAT('update patient_details set blood_group_id = (select blood_group_id from blood_group_master where blood_group_name=%L)
				 where  %I in (%L,%L,%L,%L,%L) ', 'AB-',var_custom_field_name,'AB -VE','AB NEGATIVE','AB Negative','AB-','AB-ve');

		EXECUTE FORMAT('update patient_details set blood_group_id = (select blood_group_id from blood_group_master where blood_group_name=%L)
				 where  %I in (%L,%L,%L,%L,%L) ', 'B-',var_custom_field_name,'B NEGATIVE','B Negative','B-','B-ve','B -VE');
		EXECUTE FORMAT('update patient_details set blood_group_id = (select blood_group_id from blood_group_master where blood_group_name=%L)
				 where  %I in (%L,%L,%L,%L,%L,%L) ', 'B+',var_custom_field_name,'B +VE','B +ve','B POSITIVE','B Positive','B+','B+ve');

		EXECUTE FORMAT('update patient_details set blood_group_id = (select blood_group_id from blood_group_master where blood_group_name=%L)
				 where  %I in (%L,%L,%L,%L,%L) ', 'O-',var_custom_field_name,'O -VE','O NEGATIVE','O Negative','O-','O-ve');
		EXECUTE FORMAT('update patient_details set blood_group_id = (select blood_group_id from blood_group_master where blood_group_name=%L)
				 where  %I in (%L,%L,%L,%L,%L) ', 'O+',var_custom_field_name,'O +VE','O POSITIVE','O Positive','O+','O+ve');
	END IF;
END;
$$
LANGUAGE plpgsql;
SELECT migrate_blood_group_value();


-- marital status

CREATE OR REPLACE FUNCTION migrate_marital_status_value()
	RETURNS void AS $$
DECLARE
	var_custom_field_name character varying(50);
BEGIN
	select name from reg_custom_fields where display_type='dropdown' and upper(label) in ('MARITAL STATUS','MARITIAL STATUS','MARTIAL STATUS','STATUS PERNIKAHAN','MARITAL SEX') into var_custom_field_name;

	IF var_custom_field_name != '' THEN
		EXECUTE FORMAT('update patient_details set marital_status_id = (select marital_status_id from marital_status_master where marital_status_name=%L)
				where  %I in (%L,%L,%L,%L,%L,%L,%L,%L,%L) ','Widowed',var_custom_field_name,'WIDOW','WIDOWED','Widow','Widow/Widower','Widowed','Widower','widowed','Janda','Duda');

		EXECUTE FORMAT('update patient_details set marital_status_id = (select marital_status_id from marital_status_master where marital_status_name=%L)
				where  %I in (%L,%L) ','Unmarried',var_custom_field_name,'Not Married','Unmarried');

		EXECUTE FORMAT('update patient_details set marital_status_id = (select marital_status_id from marital_status_master where marital_status_name=%L)
				where  %I in (%L,%L,%L) ','Single',var_custom_field_name,'SINGLE','Single','Belum Menikah');

		EXECUTE FORMAT('update patient_details set marital_status_id = (select marital_status_id from marital_status_master where marital_status_name=%L)
				where  %I in (%L,%L,%L,%L) ','Separated',var_custom_field_name,'SEPARATED','SEPERATED','Separated','Seperated');

		EXECUTE FORMAT('update patient_details set marital_status_id = (select marital_status_id from marital_status_master where marital_status_name=%L)
				where  %I in (%L,%L,%L,%L,%L,%L,%L,%L) ','Other',var_custom_field_name,'CHILD','Child','Engaged','IN A RELATIONSHIP','Not To Mention',
				'OTHER','Other','Others');

		EXECUTE FORMAT('update patient_details set marital_status_id = (select marital_status_id from marital_status_master where marital_status_name=%L)
				where  %I in (%L,%L,%L,%L) ','Married',var_custom_field_name,'MARRIED','Marrd.','Married','Menikah');

		EXECUTE FORMAT('update patient_details set marital_status_id = (select marital_status_id from marital_status_master where marital_status_name=%L)
				where  %I in (%L) ','Living Together',var_custom_field_name,'CO-HABITING');

		EXECUTE FORMAT('update patient_details set marital_status_id = (select marital_status_id from marital_status_master where marital_status_name=%L)
				where  %I in (%L,%L,%L,%L,%L,%L) ','Divorced',var_custom_field_name,'DIVORCED','Diverse','Divorc','Divorce','Divorced','Divorcee');

		EXECUTE FORMAT('update patient_details set marital_status_id = (select marital_status_id from marital_status_master where marital_status_name=%L)
				where  %I in (%L) ','Common Law',var_custom_field_name,'Living Common Law');

	END IF;
END;
$$
LANGUAGE plpgsql;
SELECT migrate_marital_status_value();

-- religion

CREATE OR REPLACE FUNCTION migrate_religion_value()
	RETURNS void AS $$
DECLARE
	var_custom_field_name character varying(50);
BEGIN
	select name from reg_custom_fields where display_type='dropdown' and upper(label) in ('RELIGION', 'ETHINICITY', 'ETHNIC GROUP','AGAMA') into var_custom_field_name;

	IF var_custom_field_name != '' THEN
		EXECUTE FORMAT('update patient_details set religion_id = (select religion_id from religion_master where religion_name=%L)
		 where  %I in (%L,%L,%L,%L,%L,%L,%L,%L,%L,%L) ','Buddhist',var_custom_field_name,'BAUDH','BUDDHISM','BUDDHIST','BUDDISM','BUDDIST','BUDHIST',
		 'Buddhism','Buddhist','Budhist','Budha');
		EXECUTE FORMAT('update patient_details set religion_id = (select religion_id from religion_master where religion_name=%L)
		where  %I in (%L,%L,%L,%L,%L,%L,%L,%L,%L,%L,%L,%L,%L,%L,%L,%L,%L) ','Christian',var_custom_field_name,'7TH DAY ADVENTIST','ALLIANCE','BORN AGAIN','CATHOLIC',
		'CHRISTIAN','CHRISTIANITY','CHRISTIANS','Christian','IGLESIA NI CRISTO',E'JEHOVA\'S WITNESSES',E'JEHOVAH\'S WITNESS','JEHOVAS WITNESS','PROTESTANT',
		'ROMAN CATHOLIC','SEVENTH-DAY ADVENTIST','Kristen','Katholik');

		EXECUTE FORMAT('update patient_details set religion_id = (select religion_id from religion_master where religion_name=%L)
		where  %I in (%L,%L,%L,%L) ','Hindu',var_custom_field_name,'HINDU','HINDUISM','HINDUS','Hindu');

		EXECUTE FORMAT('update patient_details set religion_id = (select religion_id from religion_master where religion_name=%L)
		where  %I in (%L,%L,%L,%L,%L) ','Islam',var_custom_field_name,'ISLAM','Islam','MUSLIM','MUSLIMS','Muslim');

		EXECUTE FORMAT('update patient_details set religion_id = (select religion_id from religion_master where religion_name=%L)
		where  %I in (%L,%L,%L,%L) ','Jain',var_custom_field_name,'JAIN','JAIN DIGAMBAR','JAINISM','Jain');

		EXECUTE FORMAT('update patient_details set religion_id = (select religion_id from religion_master where religion_name=%L)
		where  %I in (%L,%L,%L,%L,%L,%L) ','Other',var_custom_field_name,'DATING DAAN','FREE THINKER','OTHER','OTHERS','Other','others');

		EXECUTE FORMAT('update patient_details set religion_id = (select religion_id from religion_master where religion_name=%L)
		where  %I in (%L) ','Parsi',var_custom_field_name,'Parcy');
		EXECUTE FORMAT('update patient_details set religion_id = (select religion_id from religion_master where religion_name=%L)
		where  %I in (%L,%L,%L) ','Sikh',var_custom_field_name,'SIKH','SIKHISM','Sikh');

		EXECUTE FORMAT('update patient_details set religion_id = (select religion_id from religion_master where religion_name=%L)
		where  %I in (%L) ','Spiritist',var_custom_field_name,'UECFI');
		EXECUTE FORMAT('update patient_details set religion_id = (select religion_id from religion_master where religion_name=%L)
		where  %I in (%L,%L,%L) ','Unknown',var_custom_field_name,'INC','NO RELIGION','Lain-Lain');
	END IF;
END;
$$
LANGUAGE plpgsql;
SELECT migrate_religion_value();
