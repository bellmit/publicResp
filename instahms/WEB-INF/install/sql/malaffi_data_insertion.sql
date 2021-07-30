-- Religion Mapping
DROP TABLE IF EXISTS religion_mapping_temp;
CREATE TABLE religion_mapping_temp (
    malaffi_code CHARACTER VARYING,
    malaffi_description CHARACTER VARYING,
    hms_name CHARACTER VARYING,
    is_default CHARACTER VARYING
);

\COPY religion_mapping_temp FROM '/tmp/malaffi_dataset/religion.csv' CSV HEADER;

INSERT INTO code_sets 
(SELECT nextval('code_sets_seq'), 2, 1, religion_id, malaffi_description, malaffi_code,
    CASE WHEN ((lower(is_default) = 'true' OR lower(is_default) = 'yes') AND rm.status='A') THEN true ELSE null END AS is_default
    FROM religion_mapping_temp rmt 
    JOIN religion_master rm ON (lower(rm.religion_name) = lower(rmt.hms_name))
    WHERE rm.religion_id NOT IN (
        SELECT entity_id FROM code_sets WHERE code_system_category_id=2 AND code_system_id=1));

DROP TABLE religion_mapping_temp;

-- Marital Status Mapping
DROP TABLE IF EXISTS marital_status_mapping_temp;
CREATE TABLE marital_status_mapping_temp (
    malaffi_code CHARACTER VARYING,
    malaffi_description CHARACTER VARYING,
    hms_name CHARACTER VARYING,
    is_default CHARACTER VARYING
);

\COPY marital_status_mapping_temp FROM '/tmp/malaffi_dataset/marital_status.csv' CSV HEADER;

INSERT INTO code_sets 
(SELECT nextval('code_sets_seq'),3, 1, msm.marital_status_id, msmt.malaffi_description, msmt.malaffi_code,
    CASE WHEN ((lower(is_default) = 'true' OR lower(is_default) = 'yes') AND msm.status='A') THEN true ELSE null END AS is_default
    FROM marital_status_mapping_temp msmt 
    JOIN marital_status_master msm ON (lower(msm.marital_status_name) = lower(msmt.hms_name))
    WHERE msm.marital_status_id NOT IN (
        SELECT entity_id FROM code_sets WHERE code_system_category_id=3 AND code_system_id=1));

DROP TABLE marital_status_mapping_temp;

-- Department mapping
DROP TABLE IF EXISTS department_temp;
CREATE TABLE department_temp (
    malaffi_code CHARACTER VARYING,
    malaffi_description CHARACTER VARYING,
    hms_name CHARACTER VARYING,
    is_default CHARACTER VARYING
);

\COPY department_temp FROM '/tmp/malaffi_dataset/department.csv' CSV HEADER;

INSERT INTO code_sets 
(SELECT nextval('code_sets_seq'), 4, 1, id, malaffi_description, malaffi_code,
    CASE WHEN ((lower(is_default) = 'true' OR lower(is_default) = 'yes') AND d.status='A') THEN true ELSE null END AS is_default
    FROM department_temp dt 
    JOIN department d ON (lower(d.dept_name) = lower(dt.hms_name))
    WHERE d.id NOT IN (
        SELECT entity_id FROM code_sets WHERE code_system_category_id=4 AND code_system_id=1));

DROP TABLE department_temp;

-- Discharge status mapping
DROP TABLE IF EXISTS discharge_type_temp;
CREATE TABLE discharge_type_temp (
    malaffi_code CHARACTER VARYING,
    malaffi_description CHARACTER VARYING,
    hms_name CHARACTER VARYING,
    is_default CHARACTER VARYING
);

\COPY discharge_type_temp FROM '/tmp/malaffi_dataset/discharge_type.csv' CSV HEADER;

INSERT INTO code_sets 
(SELECT nextval('code_sets_seq'), 5, 1, discharge_type_id, malaffi_description, malaffi_code,
    CASE WHEN ((lower(is_default) = 'true' OR lower(is_default) = 'yes') AND dtm.status='A') THEN true ELSE null END AS is_default
    FROM discharge_type_temp dtt 
    JOIN discharge_type_master dtm ON (lower(dtm.discharge_type) = lower(dtt.hms_name))
    WHERE dtm.discharge_type_id NOT IN (
        SELECT entity_id FROM code_sets WHERE code_system_category_id=5 AND code_system_id=1));

DROP TABLE discharge_type_temp;

-- Nationality Mapping
DROP TABLE IF EXISTS nationality_master_temp;
CREATE TABLE nationality_master_temp (
    malaffi_code CHARACTER VARYING,
    malaffi_description CHARACTER VARYING,
    hms_name CHARACTER VARYING,
    is_default CHARACTER VARYING
);

\COPY nationality_master_temp FROM '/tmp/malaffi_dataset/nationality.csv' CSV HEADER;

INSERT INTO code_sets 
(SELECT nextval('code_sets_seq'), 6, 1, id, malaffi_description, malaffi_code,
    CASE WHEN ((lower(is_default) = 'true' OR lower(is_default) = 'yes') AND cm.status='A') THEN true ELSE null END AS is_default
    FROM nationality_master_temp cmt 
    JOIN country_master cm ON (lower(cm.country_name) = lower(cmt.hms_name))
    WHERE cm.id NOT IN (
        SELECT entity_id FROM code_sets WHERE code_system_category_id=6 AND code_system_id=1));

DROP TABLE nationality_master_temp;

-- State Mapping
DROP TABLE IF EXISTS state_master_temp;
CREATE TABLE state_master_temp (
    malaffi_code CHARACTER VARYING,
    malaffi_description CHARACTER VARYING,
    hms_name CHARACTER VARYING,
    is_default CHARACTER VARYING
);

\COPY state_master_temp FROM '/tmp/malaffi_dataset/state.csv' CSV HEADER;

INSERT INTO code_sets 
(SELECT nextval('code_sets_seq'),7, 1, id, malaffi_description, malaffi_code,
    CASE WHEN ((lower(is_default) = 'true' OR lower(is_default) = 'yes') AND sm.status='A') THEN true ELSE null END AS is_default
    FROM state_master_temp smt 
    JOIN state_master sm ON (lower(sm.state_name) = lower(smt.hms_name))
    WHERE sm.id NOT IN (
        SELECT entity_id FROM code_sets WHERE code_system_category_id=7 AND code_system_id=1));

INSERT INTO code_sets
(SELECT nextval('code_sets_seq'),7,1,id,'Others','0',null FROM state_master sm
    WHERE sm.id NOT in (SELECT entity_id FROM code_sets WHERE code_system_category_id=7 AND code_system_id=1));

DROP TABLE state_master_temp;

-- Country Mapping
DROP TABLE IF EXISTS country_master_temp;
CREATE TABLE country_master_temp (
    malaffi_code CHARACTER VARYING,
    malaffi_description CHARACTER VARYING,
    hms_name CHARACTER VARYING,
    is_default CHARACTER VARYING
);

\COPY country_master_temp FROM '/tmp/malaffi_dataset/country.csv' CSV HEADER;

INSERT INTO code_sets 
(SELECT nextval('code_sets_seq'),8, 1, id, malaffi_description, malaffi_code,
    CASE WHEN ((lower(is_default) = 'true' OR lower(is_default) = 'yes') AND cm.status='A') THEN true ELSE null END AS is_default
    FROM country_master_temp cmt 
    JOIN country_master cm ON (lower(cm.country_name) = lower(cmt.hms_name))
    WHERE cm.id NOT IN (
        SELECT entity_id FROM code_sets WHERE code_system_category_id=8 AND code_system_id=1));

DROP TABLE country_master_temp;

-- Diagnosis Status Mapping
DROP TABLE IF EXISTS diagnosis_statuses_temp;
CREATE TABLE diagnosis_statuses_temp (
    malaffi_code CHARACTER VARYING,
    malaffi_description CHARACTER VARYING,
    hms_name CHARACTER VARYING,
    is_default CHARACTER VARYING
);

\COPY diagnosis_statuses_temp FROM '/tmp/malaffi_dataset/diagnosis_statuses.csv' CSV HEADER;

INSERT INTO code_sets 
(SELECT nextval('code_sets_seq'),9, 1, ds.diagnosis_status_id, malaffi_description, malaffi_code,
    CASE WHEN ((lower(is_default) = 'true' OR lower(is_default) = 'yes') AND ds.status='A') THEN true ELSE null END AS is_default
    FROM diagnosis_statuses_temp dst 
    JOIN diagnosis_statuses ds ON (lower(ds.diagnosis_status_name) = lower(dst.hms_name))
    WHERE ds.diagnosis_status_id NOT IN (
        SELECT entity_id FROM code_sets WHERE code_system_category_id=9 AND code_system_id=1));

DROP TABLE diagnosis_statuses_temp;

-- Medicine Route Mapping
DROP TABLE IF EXISTS medicine_route_temp;
CREATE TABLE medicine_route_temp (
    malaffi_code CHARACTER VARYING,
    malaffi_description CHARACTER VARYING,
    hms_name CHARACTER VARYING,
    is_default CHARACTER VARYING
);

\COPY medicine_route_temp FROM '/tmp/malaffi_dataset/medicine_route.csv' CSV HEADER;

INSERT INTO code_sets 
(SELECT nextval('code_sets_seq'),10, 1, mr.route_id, malaffi_description, malaffi_code,
    CASE WHEN ((lower(is_default) = 'true' OR lower(is_default) = 'yes') AND mr.status='A') THEN true ELSE null END AS is_default
    FROM medicine_route_temp mrt 
    JOIN medicine_route mr ON (lower(mr.route_name) = lower(mrt.hms_name))
    WHERE mr.route_id NOT IN (
        SELECT entity_id FROM code_sets WHERE code_system_category_id=10 AND code_system_id=1));

DROP TABLE medicine_route_temp;