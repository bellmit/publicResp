-- Drops temp table if it exists already.
DROP TABLE IF EXISTS religion_mapping_nabidh;
DROP TABLE IF EXISTS marital_status_mapping_nabidh;
DROP TABLE IF EXISTS department_nabidh;
DROP TABLE IF EXISTS discharge_type_nabidh;
DROP TABLE IF EXISTS nationality_master_nabidh;
DROP TABLE IF EXISTS state_master_nabidh;
DROP TABLE IF EXISTS country_master_nabidh;
DROP TABLE IF EXISTS diagnosis_statuses_nabidh;
DROP TABLE IF EXISTS diagnostics_departments_nabidh;
DROP TABLE IF EXISTS race_master_nabidh;
DROP TABLE IF EXISTS medicine_route_nabidh;

-- Religion Mapping
CREATE TABLE religion_mapping_nabidh (
    nabidh_code CHARACTER VARYING,
    nabidh_description CHARACTER VARYING,
    hms_name CHARACTER VARYING,
    is_default CHARACTER VARYING
);
insert into religion_mapping_nabidh values ('OTH','Other','Agnostic',null);
insert into religion_mapping_nabidh values ('OTH','Other','Atheist',null);
insert into religion_mapping_nabidh values ('OTH','Other','Baha''i',null);
insert into religion_mapping_nabidh values ('BUD','Buddhist','Buddhist',null);
insert into religion_mapping_nabidh values ('OTH','Other','Chinese Folk Religionist',null);
insert into religion_mapping_nabidh values ('CHR','Christian','Christian',null);
insert into religion_mapping_nabidh values ('OTH','Other','Confucian',null);
insert into religion_mapping_nabidh values ('ERL','Ethnic Religionist','Ethnic Religionist',null);
insert into religion_mapping_nabidh values ('HIN','Hindu','Hindu',null);
insert into religion_mapping_nabidh values ('MOS','Muslim','Islam',null);
insert into religion_mapping_nabidh values ('JAI','Jain','Jain',null);
insert into religion_mapping_nabidh values ('JEW','Jewish','Jewish',null);
insert into religion_mapping_nabidh values ('MOS','Muslim','Muslim',null);
insert into religion_mapping_nabidh values ('OTH','Other','Other',null);
insert into religion_mapping_nabidh values ('OTH','Other','Parsi',null);
insert into religion_mapping_nabidh values ('SHN','Shintoist','Shintoist',null);
insert into religion_mapping_nabidh values ('SIK','Sikh','Sikh',null);
insert into religion_mapping_nabidh values ('OTH','Other','Spiritist',null);
insert into religion_mapping_nabidh values ('VAR','Unknown','Unknown',null);
INSERT INTO code_sets 
(SELECT nextval('code_sets_seq'), 2, cs.id, religion_id, nabidh_description, nabidh_code, null 
    FROM religion_mapping_nabidh rmt 
    JOIN religion_master rm ON (lower(rm.religion_name) = lower(rmt.hms_name))
    JOIN code_systems cs ON (cs.label = 'NABIDH')
    WHERE rm.religion_id NOT IN (
        SELECT cs.entity_id FROM code_sets cs, code_systems css WHERE cs.code_system_category_id=2 AND cs.code_system_id=css.id AND css.label = 'NABIDH'));
DROP TABLE IF EXISTS religion_mapping_nabidh;

-- Marital Status Mapping
CREATE TABLE marital_status_mapping_nabidh (
    nabidh_code CHARACTER VARYING,
    nabidh_description CHARACTER VARYING,
    hms_name CHARACTER VARYING,
    is_default CHARACTER VARYING
);
insert into marital_status_mapping_nabidh values ('A','Separated','Separated',null);
insert into marital_status_mapping_nabidh values ('A','Separated','Legally Separated',null);
insert into marital_status_mapping_nabidh values ('D','Divorced','Divorced',null);
insert into marital_status_mapping_nabidh values ('M','Married','Married',null);
insert into marital_status_mapping_nabidh values ('O','Other','Domestic Partner',null);
insert into marital_status_mapping_nabidh values ('O','Other','Registered Domestic Partner',null);
insert into marital_status_mapping_nabidh values ('S','Single','Single',null);
insert into marital_status_mapping_nabidh values ('B','Unmarried','Unmarried',null);
insert into marital_status_mapping_nabidh values ('U','Unknown','Unreported',null);
insert into marital_status_mapping_nabidh values ('W','Widowed','Widowed',null);
insert into marital_status_mapping_nabidh values ('O','Other','Living Together',null);
insert into marital_status_mapping_nabidh values ('N','Annulled','Annuled',null);
insert into marital_status_mapping_nabidh values ('O','Other','Common Law',null);
insert into marital_status_mapping_nabidh values ('O','Other','Interlocutory',null);
insert into marital_status_mapping_nabidh values ('O','Other','Other',null);
insert into marital_status_mapping_nabidh values ('U','Unknown','Unknown',null);
INSERT INTO code_sets 
(SELECT nextval('code_sets_seq'),3, cs.id, msm.marital_status_id, msmt.nabidh_description, msmt.nabidh_code, null 
    FROM marital_status_mapping_nabidh msmt 
    JOIN marital_status_master msm ON (lower(msm.marital_status_name) = lower(msmt.hms_name))
    JOIN code_systems cs ON (cs.label = 'NABIDH')
    WHERE msm.marital_status_id NOT IN (
        SELECT cs.entity_id FROM code_sets cs, code_systems css WHERE cs.code_system_category_id=3 AND cs.code_system_id=css.id AND css.label = 'NABIDH'));
DROP TABLE IF EXISTS marital_status_mapping_nabidh;

-- Department mapping
CREATE TABLE department_nabidh (
    nabidh_code CHARACTER VARYING,
    nabidh_description CHARACTER VARYING,
    hms_name CHARACTER VARYING,
    is_default CHARACTER VARYING
);
insert into department_nabidh values ('4','Anesthesia','Anaesthesia',null);
insert into department_nabidh values ('4','Anesthesia','Anaesthesiology',null);
insert into department_nabidh values ('4','Anesthesia','Anaesthetics',null);
insert into department_nabidh values ('4','Anesthesia','Anaesthetist',null);
insert into department_nabidh values ('4','Anesthesia','ANESTHESIA',null);
insert into department_nabidh values ('4','Anesthesia','Anesthesiologist',null);
insert into department_nabidh values ('4','Anesthesia','Anesthesiology',null);
insert into department_nabidh values ('5','Audiology','AUDIOLOGIST',null);
insert into department_nabidh values ('5','Audiology','Audiology',null);
insert into department_nabidh values ('5','Audiology','Auditory System',null);
insert into department_nabidh values ('5','Audiology','OCMC AUDIOLOGY',null);
insert into department_nabidh values ('7','Cardiology','Cardiologist',null);
insert into department_nabidh values ('7','Cardiology','Cardiology',null);
insert into department_nabidh values ('7','Cardiology','CARDIOLOGY - Teleconsultation',null);
insert into department_nabidh values ('7','Cardiology','CARDIOLOGY-Tele Consult',null);
insert into department_nabidh values ('7','Cardiology','CARDIOLOGY-Teleconsultation',null);
insert into department_nabidh values ('7','Cardiology','LLHMC CARDIOLOGY',null);
insert into department_nabidh values ('11','Dermatology','Dermatalogy',null);
insert into department_nabidh values ('11','Dermatology','Dermatolgoy',null);
insert into department_nabidh values ('11','Dermatology','Dermatologist',null);
insert into department_nabidh values ('11','Dermatology','Dermatology',null);
insert into department_nabidh values ('11','Dermatology','DERMATOLOGY - Teleconsultation',null);
insert into department_nabidh values ('11','Dermatology','DERMATOLOGY And WOUND CARE',null);
insert into department_nabidh values ('11','Dermatology','Dermatology Specialty',null);
insert into department_nabidh values ('11','Dermatology','DERMATOLOGY-Tele Consult',null);
insert into department_nabidh values ('11','Dermatology','DERMATOLOGY-Teleconsultation',null);
insert into department_nabidh values ('11','Dermatology','Integumentary System',null);
insert into department_nabidh values ('11','Dermatology','LLHMC DERMATOLOGY',null);
insert into department_nabidh values ('11','Dermatology','Skin Care',null);
insert into department_nabidh values ('12','Diagnostic Radiology','DIAGNOSTICS',null);
insert into department_nabidh values ('13','Emergency Medicine','ACCIDENT & EMERGENCY',null);
insert into department_nabidh values ('13','Emergency Medicine','ACCIDENT AND EMERGENCY',null);
insert into department_nabidh values ('13','Emergency Medicine','E&M',null);
insert into department_nabidh values ('13','Emergency Medicine','Emergency',null);
insert into department_nabidh values ('13','Emergency Medicine','Emergency Department',null);
insert into department_nabidh values ('13','Emergency Medicine','Emergency Medicine',null);
insert into department_nabidh values ('13','Emergency Medicine','Emergency Room',null);
insert into department_nabidh values ('13','Emergency Medicine','ER',null);
insert into department_nabidh values ('13','Emergency Medicine','Urgent Care',null);
insert into department_nabidh values ('13','Emergency Medicine','URGENT CARE (Elective)',null);
insert into department_nabidh values ('13','Emergency Medicine','URGENT CARE (Emergency)',null);
insert into department_nabidh values ('15','Endodontics','Endodontist',null);
insert into department_nabidh values ('16','Family Medicine','Family Medicine',null);
insert into department_nabidh values ('16','Family Medicine','FAMILY MEDICINE - Teleconsultation',null);
insert into department_nabidh values ('16','Family Medicine','FAMILY MEDICINE-Tele Consult',null);
insert into department_nabidh values ('16','Family Medicine','LLHMC FAMILY MEDICINE',null);
insert into department_nabidh values ('16','Family Medicine','SPECIALIST FAMILY MEDICNE',null);
insert into department_nabidh values ('16','Family Medicine','SPECIALIST FAMILY MEDICNE - Teleconsultation',null);
insert into department_nabidh values ('19','General Dentist','GENERAL DENTIST',null);
insert into department_nabidh values ('19','General Dentist','General Dentistry',null);
insert into department_nabidh values ('19','General Dentist','GP Dentist',null);
insert into department_nabidh values ('20','General Practitioner','OCMC GENERAL PRACTITIONER',null);
insert into department_nabidh values ('20','General Practitioner','G.P',null);
insert into department_nabidh values ('20','General Practitioner','G.P.CLINICS',null);
insert into department_nabidh values ('20','General Practitioner','General',null);
insert into department_nabidh values ('20','General Practitioner','General / Aesthetic Medicine',null);
insert into department_nabidh values ('20','General Practitioner','GENERAL MEDICINE',null);
insert into department_nabidh values ('20','General Practitioner','General Physician',null);
insert into department_nabidh values ('20','General Practitioner','General Pra',null);
insert into department_nabidh values ('20','General Practitioner','General Practice',null);
insert into department_nabidh values ('20','General Practitioner','General Practitioner',null);
insert into department_nabidh values ('20','General Practitioner','General Practitioner (GP)',null);
insert into department_nabidh values ('20','General Practitioner','GENERAL PRACTITIONER-Teleconsultation',null);
insert into department_nabidh values ('20','General Practitioner','General Service',null);
insert into department_nabidh values ('20','General Practitioner','GP',null);
insert into department_nabidh values ('20','General Practitioner','GP (Pediatrics)',null);
insert into department_nabidh values ('20','General Practitioner','GP Clinic',null);
insert into department_nabidh values ('20','General Practitioner','GP Medicine',null);
insert into department_nabidh values ('20','General Practitioner','GP Practitioner',null);
insert into department_nabidh values ('20','General Practitioner','GP-MEDICAL',null);
insert into department_nabidh values ('20','General Practitioner','Medical Practitioner',null);
insert into department_nabidh values ('20','General Practitioner','OCMC GP Room 1',null);
insert into department_nabidh values ('20','General Practitioner','OCMC GP Room 2',null);
insert into department_nabidh values ('20','General Practitioner','OCMC GP Room 3',null);
insert into department_nabidh values ('20','General Practitioner','OCMC GP Room 4',null);
insert into department_nabidh values ('20','General Practitioner','Physician',null);
insert into department_nabidh values ('21','Genito Urinary Medicine','Male Genital System',null);
insert into department_nabidh values ('23','Intensive care','ICU',null);
insert into department_nabidh values ('23','Intensive care','Intensivist',null);
insert into department_nabidh values ('24','Internal Medicine','PULMONORY',null);
insert into department_nabidh values ('24','Internal Medicine','Internal Med/Gastro',null);
insert into department_nabidh values ('24','Internal Medicine','Internal Medice',null);
insert into department_nabidh values ('24','Internal Medicine','Internal Medicine',null);
insert into department_nabidh values ('24','Internal Medicine','INTERNAL MEDICINE - Teleconsultation',null);
insert into department_nabidh values ('24','Internal Medicine','Internal Medicine And Pulmonary Diseases',null);
insert into department_nabidh values ('24','Internal Medicine','INTERNAL MEDICINE-Tele Consult',null);
insert into department_nabidh values ('24','Internal Medicine','INTERNAL MEDICINE-Teleconsultation',null);
insert into department_nabidh values ('24','Internal Medicine','LLHMC INTERNAL MEDICINE',null);
insert into department_nabidh values ('25','Medical Genetics','Genetic Lab',null);
insert into department_nabidh values ('25','Medical Genetics','Genetics',null);
insert into department_nabidh values ('27','Neurology','Nervous System',null);
insert into department_nabidh values ('27','Neurology','NEUROLOGIST',null);
insert into department_nabidh values ('27','Neurology','Neurology',null);
insert into department_nabidh values ('27','Neurology','NEUROLOGY- Teleconsultation',null);
insert into department_nabidh values ('27','Neurology','NEUROLOGY-Tele Consult',null);
insert into department_nabidh values ('27','Neurology','Specialist Neurologist',null);
insert into department_nabidh values ('29','Obstetrics & Gynecology','LLHMC GYNECOLOGY',null);
insert into department_nabidh values ('29','Obstetrics & Gynecology','Gynaecology',null);
insert into department_nabidh values ('29','Obstetrics & Gynecology','Gynaecology And Obstretics',null);
insert into department_nabidh values ('29','Obstetrics & Gynecology','Gynecologist',null);
insert into department_nabidh values ('29','Obstetrics & Gynecology','Gynecology',null);
insert into department_nabidh values ('29','Obstetrics & Gynecology','GYNECOLOGY AND OBSTERICS',null);
insert into department_nabidh values ('29','Obstetrics & Gynecology','Gynecology And Obstetrics',null);
insert into department_nabidh values ('29','Obstetrics & Gynecology','Gynecology Clinic',null);
insert into department_nabidh values ('29','Obstetrics & Gynecology','Gynecolology',null);
insert into department_nabidh values ('29','Obstetrics & Gynecology','GYNEOCOLOGY',null);
insert into department_nabidh values ('29','Obstetrics & Gynecology','OB-GYNE',null);
insert into department_nabidh values ('29','Obstetrics & Gynecology','OB/Gyn',null);
insert into department_nabidh values ('29','Obstetrics & Gynecology','OBSTETRICS',null);
insert into department_nabidh values ('29','Obstetrics & Gynecology','OBSTETRICS / GYNAECOLOGY',null);
insert into department_nabidh values ('29','Obstetrics & Gynecology','Obstetrics & gynaecology',null);
insert into department_nabidh values ('29','Obstetrics & Gynecology','OBSTETRICS & GYNAECOLOGY-Tele Consult',null);
insert into department_nabidh values ('29','Obstetrics & Gynecology','OBSTETRICS & GYNAECOLOGY-Teleconsultation',null);
insert into department_nabidh values ('29','Obstetrics & Gynecology','Obstetrics & Gynecology',null);
insert into department_nabidh values ('29','Obstetrics & Gynecology','Obstetrics And Gynecology',null);
insert into department_nabidh values ('29','Obstetrics & Gynecology','Obstetrics Gynecology',null);
insert into department_nabidh values ('29','Obstetrics & Gynecology','Specialist OB-GYNE',null);
insert into department_nabidh values ('30','Occupational Medicine','Occupational Health',null);
insert into department_nabidh values ('30','Occupational Medicine','OCCUPATIONAL MEDICINE',null);
insert into department_nabidh values ('30','Occupational Medicine','Occupational Therapist',null);
insert into department_nabidh values ('30','Occupational Medicine','Occupational Therapy',null);
insert into department_nabidh values ('31','Ophthalmology','Eye & Ocular Adnexa',null);
insert into department_nabidh values ('31','Ophthalmology','OCMC OPHTHALMOLOGY',null);
insert into department_nabidh values ('31','Ophthalmology','Ophthalmologist',null);
insert into department_nabidh values ('31','Ophthalmology','Ophthalmology',null);
insert into department_nabidh values ('31','Ophthalmology','Ophthalmology NNMC',null);
insert into department_nabidh values ('31','Ophthalmology','Opthalmology',null);
insert into department_nabidh values ('32','Oral and Maxilofacial Surgery','ORAL & FACIO-MAXILLARY SURGERY',null);
insert into department_nabidh values ('32','Oral and Maxilofacial Surgery','Oral And Maxillofacial Surgery',null);
insert into department_nabidh values ('34','Orthodontics','Orthodontic',null);
insert into department_nabidh values ('34','Orthodontics','Orthodontics',null);
insert into department_nabidh values ('34','Orthodontics','Orthodontist',null);
insert into department_nabidh values ('34','Orthodontics','Specialist Dentist - Orthodontic',null);
insert into department_nabidh values ('34','Orthodontics','SPECIALIST ORTHODONTICS',null);
insert into department_nabidh values ('34','Orthodontics','SPECIALIST ORTHODONTIST',null);
insert into department_nabidh values ('35','Otolaryngology','Otolaryngology',null);
insert into department_nabidh values ('36','Pathology','Clinical Pathologist',null);
insert into department_nabidh values ('36','Pathology','Clinical Pathology',null);
insert into department_nabidh values ('36','Pathology','Laboratory',null);
insert into department_nabidh values ('36','Pathology','LLHMC PHLEBOTOMY',null);
insert into department_nabidh values ('36','Pathology','LLHMC02 PHLEBOTOMY',null);
insert into department_nabidh values ('36','Pathology','OCMC LABORATORY',null);
insert into department_nabidh values ('36','Pathology','PATHOLOGIST',null);
insert into department_nabidh values ('36','Pathology','Pathology',null);
insert into department_nabidh values ('37','Pediatric Dentistry','Specialist Dentist Paediatric',null);
insert into department_nabidh values ('38','Pediatrics','LLHMC PEDIATRIC',null);
insert into department_nabidh values ('38','Pediatrics','PAEDIATRIC',null);
insert into department_nabidh values ('38','Pediatrics','Paediatrician',null);
insert into department_nabidh values ('38','Pediatrics','Paediatrics',null);
insert into department_nabidh values ('38','Pediatrics','PAEDIATRICS-Teleconsultation',null);
insert into department_nabidh values ('38','Pediatrics','PEADIATRICS',null);
insert into department_nabidh values ('38','Pediatrics','Pediatric',null);
insert into department_nabidh values ('38','Pediatrics','Pediatric Nephrology',null);
insert into department_nabidh values ('38','Pediatrics','Pediatric Pulmonology',null);
insert into department_nabidh values ('38','Pediatrics','Pediatrician',null);
insert into department_nabidh values ('38','Pediatrics','Pediatrics',null);
insert into department_nabidh values ('38','Pediatrics','PEDIATRICS-Tele Consult',null);
insert into department_nabidh values ('38','Pediatrics','Specialist Pediatric',null);
insert into department_nabidh values ('38','Pediatrics','Specialist Pediatrician',null);
insert into department_nabidh values ('38','Pediatrics','SPECIALIST PEDIATRICS',null);
insert into department_nabidh values ('39','Periodontics','PEDODONTIST',null);
insert into department_nabidh values ('39','Periodontics','PERIODONTIST',null);
insert into department_nabidh values ('40','Physical Medicine and Rehabilitation','Physical Medicine & Rehabilitation',null);
insert into department_nabidh values ('40','Physical Medicine and Rehabilitation','Physical Medicine And Rehabilitation',null);
insert into department_nabidh values ('40','Physical Medicine and Rehabilitation','Physio_recovery',null);
insert into department_nabidh values ('40','Physical Medicine and Rehabilitation','Physiotheraphy',null);
insert into department_nabidh values ('40','Physical Medicine and Rehabilitation','Physiotherapist',null);
insert into department_nabidh values ('40','Physical Medicine and Rehabilitation','PhysioTherapy',null);
insert into department_nabidh values ('40','Physical Medicine and Rehabilitation','Physiotherepy',null);
insert into department_nabidh values ('40','Physical Medicine and Rehabilitation','Physiotherphy',null);
insert into department_nabidh values ('40','Physical Medicine and Rehabilitation','Rehabilitation',null);
insert into department_nabidh values ('41','Plastic Surgery','Plastic',null);
insert into department_nabidh values ('41','Plastic Surgery','Plastic Surgeon',null);
insert into department_nabidh values ('41','Plastic Surgery','Plastic Surgery',null);
insert into department_nabidh values ('42','Prosthodontics','Specialist - Prosthodontic',null);
insert into department_nabidh values ('42','Prosthodontics','Specialist Prosthodontics',null);
insert into department_nabidh values ('43','Psychiatry','Consultant Psychiatrist',null);
insert into department_nabidh values ('43','Psychiatry','PHYSIATRIST',null);
insert into department_nabidh values ('43','Psychiatry','PSYCHIATRIST',null);
insert into department_nabidh values ('43','Psychiatry','Psychiatry',null);
insert into department_nabidh values ('43','Psychiatry','Specialist Psychiatrist',null);
insert into department_nabidh values ('46','Radiology','LLHMC RADIOLOGY',null);
insert into department_nabidh values ('46','Radiology','OCMC RADIOLOGY',null);
insert into department_nabidh values ('46','Radiology','Radiographer',null);
insert into department_nabidh values ('46','Radiology','Radiologist',null);
insert into department_nabidh values ('46','Radiology','Radiology',null);
insert into department_nabidh values ('50','Urology','Urinary System',null);
insert into department_nabidh values ('50','Urology','Urology',null);
insert into department_nabidh values ('50','Urology','UROLOGY - Teleconsultation',null);
insert into department_nabidh values ('50','Urology','UROLOGY-Tele Consult',null);
insert into department_nabidh values ('50','Urology','UROLOGY-Teleconsultation',null);
insert into department_nabidh values ('50','Urology','Urology1',null);
insert into department_nabidh values ('51','General Surgery','LAPROSCOPIC SURGERY',null);
insert into department_nabidh values ('51','General Surgery','Eneral Surgery',null);
insert into department_nabidh values ('51','General Surgery','General Surgeon',null);
insert into department_nabidh values ('51','General Surgery','General surgery',null);
insert into department_nabidh values ('51','General Surgery','GENERAL SURGERY - Teleconsultation',null);
insert into department_nabidh values ('51','General Surgery','General Surgery Specialist And Osteopath',null);
insert into department_nabidh values ('51','General Surgery','GENERAL SURGERY-Tele Consulatation',null);
insert into department_nabidh values ('51','General Surgery','GENERAL SURGERY-Tele Consult',null);
insert into department_nabidh values ('51','General Surgery','Surgery',null);
insert into department_nabidh values ('52','Gastroenterology','Digestive System',null);
insert into department_nabidh values ('52','Gastroenterology','Endoscopy',null);
insert into department_nabidh values ('52','Gastroenterology','Gastroenterologist',null);
insert into department_nabidh values ('52','Gastroenterology','Gastroenterology',null);
insert into department_nabidh values ('52','Gastroenterology','GASTROENTEROLOGY - Teleconsultation',null);
insert into department_nabidh values ('52','Gastroenterology','GASTROENTEROLOGY-Teleconsultation',null);
insert into department_nabidh values ('52','Gastroenterology','Gastrology',null);
insert into department_nabidh values ('52','Gastroenterology','OPD-GASTROENTEROLOGY',null);
insert into department_nabidh values ('55','Cardiovascular Disease','Cardiovascular System',null);
insert into department_nabidh values ('55','Cardiovascular Disease','CHEST MEDICINE',null);
insert into department_nabidh values ('56','Dermatology & Genito Urinary Medicine','DERMATOLOGY AND VENOROLOGY',null);
insert into department_nabidh values ('57','Endocrinology','Endocrine System',null);
insert into department_nabidh values ('57','Endocrinology','Endocrinology',null);
insert into department_nabidh values ('58','Endocrinology','Diabetes And Endocrinology',null);
insert into department_nabidh values ('58','Endocrinology','DIABETOLOGY',null);
insert into department_nabidh values ('64','Infectious Diseases','Infectious Diseases',null);
insert into department_nabidh values ('67','Medical Oncology','Oncology',null);
insert into department_nabidh values ('68','Neonatology','Neonatology',null);
insert into department_nabidh values ('69','Nephrology','Nephrology',null);
insert into department_nabidh values ('69','Nephrology','Nephrology And Urology',null);
insert into department_nabidh values ('69','Nephrology','Specialist Nephrologist',null);
insert into department_nabidh values ('70','Neurological Surgery','NEURO SURGERY',null);
insert into department_nabidh values ('70','Neurological Surgery','NEURO SURGERY-Tele Consult',null);
insert into department_nabidh values ('70','Neurological Surgery','Neurological Surgery',null);
insert into department_nabidh values ('70','Neurological Surgery','Neurosurgery',null);
insert into department_nabidh values ('71','Obstetrics & Gynecology / Maternal & Fetal Medicine','Maternity',null);
insert into department_nabidh values ('71','Obstetrics & Gynecology / Maternal & Fetal Medicine','Maternity Care & Delivery',null);
insert into department_nabidh values ('72','Obstetrics & Gynecology /Reproductive Endicronology/ Infertility','Gynecology And IVF',null);
insert into department_nabidh values ('72','Obstetrics & Gynecology /Reproductive Endicronology/ Infertility','IVF',null);
insert into department_nabidh values ('72','Obstetrics & Gynecology /Reproductive Endicronology/ Infertility','Ob-Gyn IVF',null);
insert into department_nabidh values ('74','Orthopedic Surgery','LLHMC ORTHOPEDICS',null);
insert into department_nabidh values ('74','Orthopedic Surgery','ORTHO',null);
insert into department_nabidh values ('74','Orthopedic Surgery','Orthopaedic',null);
insert into department_nabidh values ('74','Orthopedic Surgery','Orthopaedics',null);
insert into department_nabidh values ('74','Orthopedic Surgery','ORTHOPAEDICS - Teleconsultation',null);
insert into department_nabidh values ('74','Orthopedic Surgery','ORTHOPAEDICS-Teleconsultation',null);
insert into department_nabidh values ('74','Orthopedic Surgery','ORTHOPEADICSS',null);
insert into department_nabidh values ('74','Orthopedic Surgery','Orthopedic',null);
insert into department_nabidh values ('74','Orthopedic Surgery','Orthopedic Surgeon',null);
insert into department_nabidh values ('74','Orthopedic Surgery','Orthopedic Surgery',null);
insert into department_nabidh values ('74','Orthopedic Surgery','Orthopedics',null);
insert into department_nabidh values ('74','Orthopedic Surgery','ORTHOPEDICS-Tele Consult',null);
insert into department_nabidh values ('74','Orthopedic Surgery','Orthopediste',null);
insert into department_nabidh values ('75','Orthopedic Surgery/ Traumatology','Musculoskeletal System',null);
insert into department_nabidh values ('77','Pediatric Cardiology','Pediatric Cardiology',null);
insert into department_nabidh values ('83','Pediatric Neurology','Pediatric Neurology',null);
insert into department_nabidh values ('84','Pediatric Surgery','PAEDIATRIC SURGERY',null);
insert into department_nabidh values ('84','Pediatric Surgery','PEDIATRIC SURGERY',null);
insert into department_nabidh values ('85','Pain Medicine','Pain Medicine',null);
insert into department_nabidh values ('86','Pediatrics/ Pulmonology','Pulmonology',null);
insert into department_nabidh values ('86','Pediatrics/ Pulmonology','PULMONOLOGY - Teleconsultation',null);
insert into department_nabidh values ('86','Pediatrics/ Pulmonology','PULMONOLOGY-Teleconsultation',null);
insert into department_nabidh values ('86','Pediatrics/ Pulmonology','Pulmonory',null);
insert into department_nabidh values ('86','Pediatrics/ Pulmonology','PULMONORY MEDICINE',null);
insert into department_nabidh values ('87','Reproductive Medicine and Infertility','Reproductive Endocrinology And Infertility',null);
insert into department_nabidh values ('87','Reproductive Medicine and Infertility','Reproductive System & Intersex',null);
insert into department_nabidh values ('88','Respiratory Medicine','RHEUMATOLOGY',null);
insert into department_nabidh values ('88','Respiratory Medicine','Respiratory System',null);
insert into department_nabidh values ('88','Respiratory Medicine','Respiratory Therapist',null);
insert into department_nabidh values ('88','Respiratory Medicine','Respiratory Therapy',null);
insert into department_nabidh values ('88','Respiratory Medicine','Respiratory/Hospital Director',null);
insert into department_nabidh values ('89','Rheumatology','Rheumatology',null);
insert into department_nabidh values ('89','Rheumatology','RHEUMATOLOGY / INTERNAL MEDICINE',null);
insert into department_nabidh values ('91','Dental','Dental',null);
insert into department_nabidh values ('91','Dental','Dental Clinic',null);
insert into department_nabidh values ('91','Dental','Dentist',null);
insert into department_nabidh values ('91','Dental','Dentistry',null);
insert into department_nabidh values ('91','Dental','LLHMC DENTAL',null);
insert into department_nabidh values ('92','Diet','Diet & Nutrition',null);
insert into department_nabidh values ('92','Diet','DIETARY',null);
insert into department_nabidh values ('92','Diet','Dietetics',null);
insert into department_nabidh values ('92','Diet','DieteticsOLD',null);
insert into department_nabidh values ('92','Diet','Dietician',null);
insert into department_nabidh values ('92','Diet','Dietitian',null);
insert into department_nabidh values ('92','Diet','Dietration Clinic',null);
insert into department_nabidh values ('92','Diet','Nutrition',null);
insert into department_nabidh values ('92','Diet','Nutrition And Dietetics',null);
insert into department_nabidh values ('94','Speech Therapy','Hearing & Speech',null);
insert into department_nabidh values ('94','Speech Therapy','Speech Therapist',null);
insert into department_nabidh values ('94','Speech Therapy','Speech Therapy',null);
insert into department_nabidh values ('94','Speech Therapy','Speech Therapy Pathologist',null);
insert into department_nabidh values ('95','Podiatry','Podiatry',null);
insert into department_nabidh values ('96','Pharmacy','Pharmacist',null);
insert into department_nabidh values ('96','Pharmacy','Pharmacy',null);
insert into department_nabidh values ('103','Home Care','Home Care',null);
insert into department_nabidh values ('103','Home Care','HOMECARE',null);
insert into department_nabidh values ('103','Home Care','MidWifery & Homecare',null);
insert into department_nabidh values ('106','Nursing','Nurse',null);
insert into department_nabidh values ('106','Nursing','Nurse Assistant',null);
insert into department_nabidh values ('106','Nursing','Nursing',null);
insert into department_nabidh values ('106','Nursing','Nursing Desk',null);
insert into department_nabidh values ('109','Customer Services','CUSTOMER CARE',null);
insert into department_nabidh values ('109','Customer Services','Customer Services',null);
insert into department_nabidh values ('131','Bariatric Medicine','Bariatric',null);
insert into department_nabidh values ('133','Psychology','CDA PSYCHOLOGIST',null);
insert into department_nabidh values ('133','Psychology','Psychologist',null);
insert into department_nabidh values ('133','Psychology','Psychology',null);
insert into department_nabidh values ('133','Psychology','Psychomotor Therapy',null);
insert into department_nabidh values ('134','Speech and Language Therapy','Speech And Language',null);
insert into department_nabidh values ('135','Geriatric Medicine','GENERAL MEDICINE',null);
insert into department_nabidh values ('136','Cardiothoracic surgery','Cardiothoracic Surgery',null);
insert into department_nabidh values ('136','Cardiothoracic surgery','CARDIOTHORASIC SURGERY',null);
insert into department_nabidh values ('136','Cardiothoracic surgery','Thoracic',null);
insert into department_nabidh values ('137','Behavioral Health','Behavioural Therapy',null);
insert into department_nabidh values ('138','Home Health Services','Home Vist',null);
insert into department_nabidh values ('140','Optometry','Optometry',null);
insert into department_nabidh values ('144','Chiropractic Medicine','Chiropractic',null);
insert into department_nabidh values ('153','Otology/Neuro Otology','Otolarngology',null);
insert into department_nabidh values ('156','Professional Counseling','Counsellor',null);
insert into department_nabidh values ('158','Referral Physician','Referral',null);
insert into department_nabidh values ('160','Sports Medicine','Sports Medicine',null);
insert into department_nabidh values ('160','Sports Medicine','Sports Therapy And Rehabilitaton',null);
insert into department_nabidh values ('162','ENT','LLHMC ENT',null);
insert into department_nabidh values ('162','ENT','ENT',null);
insert into department_nabidh values ('162','ENT','ENT - Teleconsultation',null);
insert into department_nabidh values ('162','ENT','ENT-Tele Consult',null);
insert into department_nabidh values ('162','ENT','ENT-Teleconsultation',null);
insert into department_nabidh values ('162','ENT','ENTT',null);
insert into department_nabidh values ('163','Vascular Surgery','VASCULAR SURGERY',null);
insert into department_nabidh values ('164','Beauty Clinic','Aesthetic',null);
insert into department_nabidh values ('164','Beauty Clinic','Aesthetics',null);
insert into department_nabidh values ('164','Beauty Clinic','Aesthetics & Plastic Surgery',null);
insert into department_nabidh values ('164','Beauty Clinic','Beauticion',null);
insert into department_nabidh values ('164','Beauty Clinic','Beauty',null);
insert into department_nabidh values ('164','Beauty Clinic','Beauty And Laser',null);
insert into department_nabidh values ('164','Beauty Clinic','Beauty Shop',null);
insert into department_nabidh values ('164','Beauty Clinic','Bioaesthetics',null);
insert into department_nabidh values ('164','Beauty Clinic','COSMATOLOGY',null);
insert into department_nabidh values ('164','Beauty Clinic','Cosmesurge',null);
insert into department_nabidh values ('164','Beauty Clinic','COSMETIC',null);
insert into department_nabidh values ('164','Beauty Clinic','COSMETICS',null);
insert into department_nabidh values ('164','Beauty Clinic','COSMETICSsss',null);
insert into department_nabidh values ('164','Beauty Clinic','Cosmetology',null);
insert into department_nabidh values ('164','Beauty Clinic','Cosmotology',null);
insert into department_nabidh values ('164','Beauty Clinic','Facial & Laser',null);
insert into department_nabidh values ('164','Beauty Clinic','Facial & Laser Department',null);
insert into department_nabidh values ('164','Beauty Clinic','Hydra Faical',null);
insert into department_nabidh values ('164','Beauty Clinic','Laser',null);
insert into department_nabidh values ('164','Beauty Clinic','Laser & Hydra Faical',null);
insert into department_nabidh values ('164','Beauty Clinic','Laser 1',null);
insert into department_nabidh values ('164','Beauty Clinic','Laser 2',null);
insert into department_nabidh values ('164','Beauty Clinic','Laser Department',null);
insert into department_nabidh values ('164','Beauty Clinic','Laser Dept',null);
insert into department_nabidh values ('164','Beauty Clinic','Laser Hair Removal',null);
insert into department_nabidh values ('164','Beauty Clinic','Laser Technician',null);
insert into department_nabidh values ('165','Medical/Surgical','Other Surgery',null);
insert into department_nabidh values ('167','Clinical Psychology','Clinical Psychologist',null);
INSERT INTO code_sets 
(SELECT nextval('code_sets_seq'), 4, cs.id, d.id, nabidh_description, nabidh_code, null 
    FROM department_nabidh dt 
    JOIN department d ON (lower(d.dept_name) = lower(dt.hms_name))
    JOIN code_systems cs ON (cs.label = 'NABIDH')
    WHERE d.id NOT IN (
        SELECT cs.entity_id FROM code_sets cs, code_systems css WHERE cs.code_system_category_id=4 AND cs.code_system_id=css.id AND css.label = 'NABIDH'));
DROP TABLE IF EXISTS department_nabidh;

-- Discharge type mapping
CREATE TABLE discharge_type_nabidh (
    nabidh_code CHARACTER VARYING,
    nabidh_description CHARACTER VARYING,
    hms_name CHARACTER VARYING,
    is_default CHARACTER VARYING
);
insert into discharge_type_nabidh values ('1','Discharged with approval','Normal',null);
insert into discharge_type_nabidh values ('2','Discharged against Medical advice','DAMA',null);
insert into discharge_type_nabidh values ('3','Discharged absent without leave','Absconded',null);
insert into discharge_type_nabidh values ('4','Discharge transfer to acute care','',null);
insert into discharge_type_nabidh values ('5','Deceased','Death',null);
insert into discharge_type_nabidh values ('6','Not discharged','',null);
insert into discharge_type_nabidh values ('7','Discharge transfer to non-acute care','',null);
insert into discharge_type_nabidh values ('8','Discharged to home or self-care (routine discharge)','',null);
insert into discharge_type_nabidh values ('9','Discharged/transferred to another short-term general hospital for inpatient care','Referred To',null);
insert into discharge_type_nabidh values ('10','Discharged/transferred to skilled nursing facility (SNF)','',null);
insert into discharge_type_nabidh values ('11','Discharged/transferred to an intermediate care facility (ICF)','',null);
insert into discharge_type_nabidh values ('12','Discharged/transferred to another type of institution for inpatient care or referred for outpatient services to another institution','',null);
insert into discharge_type_nabidh values ('13','Discharged/transferred to home under care of organized home health service organization','',null);
insert into discharge_type_nabidh values ('14','Left against medical advice or discontinued care','',null);
insert into discharge_type_nabidh values ('15','Discharged/transferred to home under care of Home IV provider','',null);
insert into discharge_type_nabidh values ('16','Admitted as an inpatient to this hospital','',null);
insert into discharge_type_nabidh values ('17','Discharge to be defined at state level, if necessary','',null);
insert into discharge_type_nabidh values ('18','Expired','',null);
insert into discharge_type_nabidh values ('19','Expired to be defined at state level, if necessary','',null);
insert into discharge_type_nabidh values ('20','Still patient or expected to return for outpatient services','',null);
insert into discharge_type_nabidh values ('21','Still patient to be defined at state level, if necessary','',null);
insert into discharge_type_nabidh values ('22','Expired at home','',null);
insert into discharge_type_nabidh values ('23','Expired in a medical facility; e.g., hospital, SNF, ICF, or free-standing hospice','',null);
insert into discharge_type_nabidh values ('24','Expired - place unknown','',null);
INSERT INTO code_sets 
(SELECT nextval('code_sets_seq'), 5, cs.id, discharge_type_id, nabidh_description, nabidh_code, null 
    FROM discharge_type_nabidh dtt 
    JOIN discharge_type_master dtm ON (lower(dtm.discharge_type) = lower(dtt.hms_name))
    JOIN code_systems cs ON (cs.label = 'NABIDH')
    WHERE dtm.discharge_type_id NOT IN (
        SELECT cs.entity_id FROM code_sets cs, code_systems css WHERE cs.code_system_category_id=5 AND cs.code_system_id=css.id AND css.label = 'NABIDH'));
DROP TABLE IF EXISTS discharge_type_nabidh;

-- Nationality Mapping
CREATE TABLE nationality_master_nabidh (
    nabidh_code CHARACTER VARYING,
    nabidh_description CHARACTER VARYING,
    hms_name CHARACTER VARYING,
    is_default CHARACTER VARYING
);
insert into nationality_master_nabidh values ('Afghan','Afghanistan','Afghanistan',null);
insert into nationality_master_nabidh values ('Albanian','Albania','Albania',null);
insert into nationality_master_nabidh values ('Algerian','Algeria','Algeria',null);
insert into nationality_master_nabidh values ('Andorran','Andorra','Andorra',null);
insert into nationality_master_nabidh values ('Angolan','Angola','Angola',null);
insert into nationality_master_nabidh values ('Antiguan','Antigua and Barbuda','Anguilla',null);
insert into nationality_master_nabidh values ('Argentine','Argentina','Argentina',null);
insert into nationality_master_nabidh values ('Armenian','Armenia','Armenia',null);
insert into nationality_master_nabidh values ('Australian','Australia','Australia',null);
insert into nationality_master_nabidh values ('Austrian','Austria','Austria',null);
insert into nationality_master_nabidh values ('Azerbaijani','Azerbaijan','Azerbaijan',null);
insert into nationality_master_nabidh values ('Bahamian','Bahamas','Bahamas',null);
insert into nationality_master_nabidh values ('Bahraini','Bahrain','Bahrain',null);
insert into nationality_master_nabidh values ('Bangladeshi','Bangladesh','Bangladesh',null);
insert into nationality_master_nabidh values ('Barbadian','Barbados','Barbados',null);
insert into nationality_master_nabidh values ('Belarusian','Belarus','Belarus',null);
insert into nationality_master_nabidh values ('Belgian','Belgium','Belgium',null);
insert into nationality_master_nabidh values ('Belizean','Belize','Belize',null);
insert into nationality_master_nabidh values ('Beninese','Benin','Benin',null);
insert into nationality_master_nabidh values ('Bhutanese','Bhutan','Bhutan',null);
insert into nationality_master_nabidh values ('Bolivian','Bolivia','Bolivia',null);
insert into nationality_master_nabidh values ('Bosnian','Bosnia and Herzegovina','Bonaire, Sint Eustatius and Saba',null);
insert into nationality_master_nabidh values ('Motswana','Botswana','Botswana',null);
insert into nationality_master_nabidh values ('British','United Kingdom','British Indian Ocean Territory',null);
insert into nationality_master_nabidh values ('British','United Kingdom','British Overseas Citizen',null);
insert into nationality_master_nabidh values ('British','United Kingdom','British Overseas Territories Citizen',null);
insert into nationality_master_nabidh values ('British','United Kingdom','British Protected Person',null);
insert into nationality_master_nabidh values ('British','United Kingdom','British Subject',null);
insert into nationality_master_nabidh values ('Bruneian','Brunei Darussalam','Brunei Darussalam',null);
insert into nationality_master_nabidh values ('Bulgarian','Bulgaria','Bulgaria',null);
insert into nationality_master_nabidh values ('Burkinabe','Burkina Faso','Burkina Faso',null);
insert into nationality_master_nabidh values ('Burundian','Burundi','Burundi',null);
insert into nationality_master_nabidh values ('Cameroonian','Cameroon','Cameroon',null);
insert into nationality_master_nabidh values ('Canadian','Canada','Canada',null);
insert into nationality_master_nabidh values ('Central African','Central African Republic','Central African Republic',null);
insert into nationality_master_nabidh values ('Chadian','Chad','Chad',null);
insert into nationality_master_nabidh values ('Chilean','Chile','Chile',null);
insert into nationality_master_nabidh values ('Chinese','China','China',null);
insert into nationality_master_nabidh values ('Colombian','Colombia','Colombia',null);
insert into nationality_master_nabidh values ('Comoran','Comoros','Comoros',null);
insert into nationality_master_nabidh values ('Congolese','Congo; Democratic Republic of the Congo','Congo',null);
insert into nationality_master_nabidh values ('Congolese','Congo; Democratic Republic of the Congo','Congo (Democratic Republic of the)',null);
insert into nationality_master_nabidh values ('Cook Islander','Cook Islands','Cook Islands',null);
insert into nationality_master_nabidh values ('Costa Rican','Costa Rica','Costa Rica',null);
insert into nationality_master_nabidh values ('Ivorian','Côte d''Ivoire','Cote d Ivoire',null);
insert into nationality_master_nabidh values ('Croatian','Croatia','Croatia',null);
insert into nationality_master_nabidh values ('Cuban','Cuba','Cuba',null);
insert into nationality_master_nabidh values ('Cypriot','Cyprus','Cyprus',null);
insert into nationality_master_nabidh values ('Czech','Czech Republic','Czechia',null);
insert into nationality_master_nabidh values ('Danish','Denmark','Denmark',null);
insert into nationality_master_nabidh values ('Djiboutian','Djibouti','Djibouti',null);
insert into nationality_master_nabidh values ('Dominican','Dominica; Dominican Republic','Dominica',null);
insert into nationality_master_nabidh values ('Ecuadorian','Ecuador','Ecuador',null);
insert into nationality_master_nabidh values ('Egyptian','Egypt','Egypt',null);
insert into nationality_master_nabidh values ('Salvadoran','El Salvador','El Salvador',null);
insert into nationality_master_nabidh values ('Equatoguinean','Equatorial Guinea','Equatorial Guinea',null);
insert into nationality_master_nabidh values ('Eritrean','Eritrea','Eritrea',null);
insert into nationality_master_nabidh values ('Estonian','Estonia','Estonia',null);
insert into nationality_master_nabidh values ('Ethiopian','Ethiopia','Ethiopia',null);
insert into nationality_master_nabidh values ('Fijian','Fiji','Fiji',null);
insert into nationality_master_nabidh values ('Finnish','Finland','Finland',null);
insert into nationality_master_nabidh values ('French','France','France',null);
insert into nationality_master_nabidh values ('French','France','French Guiana',null);
insert into nationality_master_nabidh values ('French','France','French Polynesia',null);
insert into nationality_master_nabidh values ('French','France','French Southern Territories',null);
insert into nationality_master_nabidh values ('Gabonese','Gabon','Gabon',null);
insert into nationality_master_nabidh values ('Gambian','Gambia','Gambia',null);
insert into nationality_master_nabidh values ('Georgian','Georgia','Georgia',null);
insert into nationality_master_nabidh values ('German','Germany','Germany',null);
insert into nationality_master_nabidh values ('Ghanaian','Ghana','Ghana',null);
insert into nationality_master_nabidh values ('Greek','Greece','Greece',null);
insert into nationality_master_nabidh values ('Grenadian','Grenada','Grenada',null);
insert into nationality_master_nabidh values ('Guatemalan','Guatemala','Guadeloupe',null);
insert into nationality_master_nabidh values ('Guinean','Guinea; Guinea-Bissau','Guinea',null);
insert into nationality_master_nabidh values ('Guinean','Guinea; Guinea-Bissau','Guinea-Bissau',null);
insert into nationality_master_nabidh values ('Guyanese','Guyana','Guyana',null);
insert into nationality_master_nabidh values ('Haitian','Haiti','Haiti',null);
insert into nationality_master_nabidh values ('Honduran','Honduras','Honduras',null);
insert into nationality_master_nabidh values ('Hungarian','Hungary','Hungary',null);
insert into nationality_master_nabidh values ('Icelandic','Iceland','Iceland',null);
insert into nationality_master_nabidh values ('Indian','India','India',null);
insert into nationality_master_nabidh values ('Indian','India','INDIA',null);
insert into nationality_master_nabidh values ('Indonesian','Indonesia','Indonesia',null);
insert into nationality_master_nabidh values ('Iranian','Iran','Iran',null);
insert into nationality_master_nabidh values ('Iraqi','Iraq','Iraq',null);
insert into nationality_master_nabidh values ('Irish','Ireland','Ireland',null);
insert into nationality_master_nabidh values ('Israeli','Israel','Israel',null);
insert into nationality_master_nabidh values ('Italian','Italy','Italy',null);
insert into nationality_master_nabidh values ('Jamaican','Jamaica','Jamaica',null);
insert into nationality_master_nabidh values ('Japanese','Japan','Japan',null);
insert into nationality_master_nabidh values ('Jordanian','Jordan','Jordan',null);
insert into nationality_master_nabidh values ('Kazakhstani','Kazakhstan','Kazakhstan',null);
insert into nationality_master_nabidh values ('Kenyan','Kenya','Kenya',null);
insert into nationality_master_nabidh values ('I-Kiribati','Kiribati','Kiribati',null);
insert into nationality_master_nabidh values ('Kuwaiti','Kuwait','Kuwait',null);
insert into nationality_master_nabidh values ('Kyrgyzstani','Kyrgyzstan','Kyrgyzstan',null);
insert into nationality_master_nabidh values ('Laotian','Lao People''s Democratic Republic','Laos',null);
insert into nationality_master_nabidh values ('Latvian','Latvia','Latvia',null);
insert into nationality_master_nabidh values ('Lebanese','Lebanon','Lebanon',null);
insert into nationality_master_nabidh values ('Basotho','Lesotho','Lesotho',null);
insert into nationality_master_nabidh values ('Liberian','Liberia','Liberia',null);
insert into nationality_master_nabidh values ('Libyan','Libyan Arab Jamahiriya','Libya',null);
insert into nationality_master_nabidh values ('Lithuanian','Lithuania','Lithuania',null);
insert into nationality_master_nabidh values ('Luxembourg','Luxembourg','Luxembourg',null);
insert into nationality_master_nabidh values ('Macedonian','Republic of Macedonia','Macedonia',null);
insert into nationality_master_nabidh values ('Malagasy','Madagascar','Madagascar',null);
insert into nationality_master_nabidh values ('Malawian','Malawi','Malawi',null);
insert into nationality_master_nabidh values ('Malaysian','Malaysia','Malaysia',null);
insert into nationality_master_nabidh values ('Maldivian','Maldives','Maldives',null);
insert into nationality_master_nabidh values ('Malian','Mali','Mali',null);
insert into nationality_master_nabidh values ('Maltese','Malta','Malta',null);
insert into nationality_master_nabidh values ('Marshallese','Marshall Islands','Marshall Islands',null);
insert into nationality_master_nabidh values ('Mauritanian','Mauritania','Mauritania',null);
insert into nationality_master_nabidh values ('Mauritian','Mauritius','Mauritius',null);
insert into nationality_master_nabidh values ('Mexican','Mexico','Mexico',null);
insert into nationality_master_nabidh values ('Micronesian','Micronesia','Micronesia',null);
insert into nationality_master_nabidh values ('Moldovan','Republic of Moldova','Moldova',null);
insert into nationality_master_nabidh values ('Monegasque','Monaco','Monaco',null);
insert into nationality_master_nabidh values ('Mongolian','Mongolia','Mongolia',null);
insert into nationality_master_nabidh values ('Montenegrin','Montenegro','Montenegro',null);
insert into nationality_master_nabidh values ('Moroccan','Morocco','Morocco',null);
insert into nationality_master_nabidh values ('Mozambican','Mozambique','Mozambique',null);
insert into nationality_master_nabidh values ('Myanmarese','Myanmar','Myanmar',null);
insert into nationality_master_nabidh values ('Namibian','Namibia','Namibia',null);
insert into nationality_master_nabidh values ('Nauruan','Nauru','Nauru',null);
insert into nationality_master_nabidh values ('Nepalese','Nepal','Nepal',null);
insert into nationality_master_nabidh values ('Dutch','Netherlands','Netherlands',null);
insert into nationality_master_nabidh values ('New Zealand','New Zealand','New Zealand',null);
insert into nationality_master_nabidh values ('Nicaraguan','Nicaragua','Nicaragua',null);
insert into nationality_master_nabidh values ('Nigerien','Niger','Niger',null);
insert into nationality_master_nabidh values ('Nigerian','Nigeria','Nigeria',null);
insert into nationality_master_nabidh values ('Niuean','Niue','Niue',null);
insert into nationality_master_nabidh values ('Korean','Republic of Korea; DPRK','North Korea',null);
insert into nationality_master_nabidh values ('Norwegian','Norway','Norway',null);
insert into nationality_master_nabidh values ('Omani','Oman','Oman',null);
insert into nationality_master_nabidh values ('Pakistani','Pakistan','Pakistan',null);
insert into nationality_master_nabidh values ('Palauan','Palau','Palau',null);
insert into nationality_master_nabidh values ('Palestinian','Palestine','Palestine',null);
insert into nationality_master_nabidh values ('Panamanian','Panama','Panama',null);
insert into nationality_master_nabidh values ('Papua New Guinean','Papua New Guinea','Papua New Guinea',null);
insert into nationality_master_nabidh values ('Paraguayan','Paraguay','Paraguay',null);
insert into nationality_master_nabidh values ('Peruvian','Peru','Peru',null);
insert into nationality_master_nabidh values ('Philippine','Philippines','Philippines',null);
insert into nationality_master_nabidh values ('Polish','Poland','Poland',null);
insert into nationality_master_nabidh values ('Portuguese','Portugal','Portugal',null);
insert into nationality_master_nabidh values ('Qatari','Qatar','Qatar',null);
insert into nationality_master_nabidh values ('Romanian','Romania','Romania',null);
insert into nationality_master_nabidh values ('Russian','Russian Federation','Russian Federation',null);
insert into nationality_master_nabidh values ('Rwandan','Rwanda','Rwanda',null);
insert into nationality_master_nabidh values ('Kittitian','Saint Kitts and Nevis','Saint Kitts and Nevis',null);
insert into nationality_master_nabidh values ('Saint Lucian','Saint Lucia','Saint Lucia',null);
insert into nationality_master_nabidh values ('Vincentian','Saint Vincent and the Grenadines','Saint Vincent and the Grenadines',null);
insert into nationality_master_nabidh values ('Samoan','Samoa','Samoa',null);
insert into nationality_master_nabidh values ('Sammarinese','San Marino','San Marino',null);
insert into nationality_master_nabidh values ('Sao Tomean','Sao Tome and Principe','Sao Tome and Principe',null);
insert into nationality_master_nabidh values ('Saudi','Saudi Arabia','Saudi Arabia',null);
insert into nationality_master_nabidh values ('Senegalese','Senegal','Senegal',null);
insert into nationality_master_nabidh values ('Serbian','Serbia','Serbia',null);
insert into nationality_master_nabidh values ('Seychellois','Seychelles','Seychelles',null);
insert into nationality_master_nabidh values ('Sierra Leonean','Sierra Leone','Sierra Leone',null);
insert into nationality_master_nabidh values ('Singapore','Singapore','Singapore',null);
insert into nationality_master_nabidh values ('Slovak','Slovakia','Slovakia',null);
insert into nationality_master_nabidh values ('Slovenian','Slovenia','Slovenia',null);
insert into nationality_master_nabidh values ('Solomon Islander','Solomon Islands','Solomon Islands',null);
insert into nationality_master_nabidh values ('Somali','Somalia','Somalia',null);
insert into nationality_master_nabidh values ('South African','South Africa','South Africa',null);
insert into nationality_master_nabidh values ('Korean',,'South Korea',null);
insert into nationality_master_nabidh values ('Sudanese','Sudan','South Sudan',null);
insert into nationality_master_nabidh values ('Bidoun','Without Nationality','Sovereign Military Order of Malta',null);
insert into nationality_master_nabidh values ('Spanish','Spain','Spain',null);
insert into nationality_master_nabidh values ('Sri Lankan','Sri Lanka','Sri Lanka',null);
insert into nationality_master_nabidh values ('Sudanese','Sudan','Sudan',null);
insert into nationality_master_nabidh values ('Surinamese','Suriname','Suriname',null);
insert into nationality_master_nabidh values ('Swazi','Swaziland','Swaziland',null);
insert into nationality_master_nabidh values ('Swedish','Sweden','Sweden',null);
insert into nationality_master_nabidh values ('Swiss','Switzerland','Switzerland',null);
insert into nationality_master_nabidh values ('Syrian','Syrian Arab Republic','Syrian Arab Republic',null);
insert into nationality_master_nabidh values ('Tajikistani','Tajikistan','Tajikistan',null);
insert into nationality_master_nabidh values ('Tanzanian','United Republic of Tanzania','Tanzania',null);
insert into nationality_master_nabidh values ('Thai','Thailand','Thailand',null);
insert into nationality_master_nabidh values ('Timorese','Timor-Leste','Timor-Leste',null);
insert into nationality_master_nabidh values ('Togolese','Togo','Togo',null);
insert into nationality_master_nabidh values ('Bidoun','Without Nationality','Tokelau',null);
insert into nationality_master_nabidh values ('Tongan','Tonga','Tonga',null);
insert into nationality_master_nabidh values ('Trinidadian','Trinidad and Tobago','Trinidad and Tobago',null);
insert into nationality_master_nabidh values ('Tunisian','Tunisia','Tunisia',null);
insert into nationality_master_nabidh values ('Turkish','Turkey','Turkey',null);
insert into nationality_master_nabidh values ('Turkmen','Turkmenistan','Turkmenistan',null);
insert into nationality_master_nabidh values ('Bidoun','Without Nationality','Turks and Caicos Islands',null);
insert into nationality_master_nabidh values ('Tuvaluan','Tuvalu','Tuvalu',null);
insert into nationality_master_nabidh values ('Ugandan','Uganda','Uganda',null);
insert into nationality_master_nabidh values ('Ukrainian','Ukraine','Ukraine',null);
insert into nationality_master_nabidh values ('Emirati','United Arab Emirates','United Arab Emirates',null);
insert into nationality_master_nabidh values ('Bidoun','Without Nationality','United Arab Emirates_inactive',null);
insert into nationality_master_nabidh values ('British','United Kingdom','United Kingdom',null);
insert into nationality_master_nabidh values ('Bidoun','Without Nationality','United Nations Organization',null);
insert into nationality_master_nabidh values ('Bidoun','Without Nationality','United Nations Specialized Agency',null);
insert into nationality_master_nabidh values ('American','United States of America','United States of America',null);
insert into nationality_master_nabidh values ('Uruguayan','Uruguay','Uruguay',null);
insert into nationality_master_nabidh values ('Uzbek','Uzbekistan','Uzbekistan',null);
insert into nationality_master_nabidh values ('Ni-Vanuatu','Vanuatu','Vanuatu',null);
insert into nationality_master_nabidh values ('Venezuelan','Venezuela','Venezuela',null);
insert into nationality_master_nabidh values ('Vietnamese','Viet Nam','Viet Nam',null);
insert into nationality_master_nabidh values ('Bidoun','Without Nationality','Virgin Islands (British)',null);
insert into nationality_master_nabidh values ('Bidoun','Without Nationality','Virgin Islands (U.S.)',null);
insert into nationality_master_nabidh values ('Bidoun','Without Nationality','Wallis and Futuna',null);
insert into nationality_master_nabidh values ('Sahrawian','Western Sahara','Western Sahara',null);
insert into nationality_master_nabidh values ('Yemeni','Yemen','Yemen',null);
insert into nationality_master_nabidh values ('Zambian','Zambia','Zambia',null);
insert into nationality_master_nabidh values ('Zimbabwean','Zimbabwe','Zimbabwe',null);
INSERT INTO code_sets 
(SELECT nextval('code_sets_seq'), 6, cs.id, cm.id, nabidh_description, nabidh_code, null 
    FROM nationality_master_nabidh cmt 
    JOIN country_master cm ON (lower(cm.country_name) = lower(cmt.hms_name))
    JOIN code_systems cs ON (cs.label = 'NABIDH')
    WHERE cm.id NOT IN (
        SELECT cs.entity_id FROM code_sets cs, code_systems css WHERE cs.code_system_category_id=6 AND cs.code_system_id=css.id AND css.label = 'NABIDH'));
DROP TABLE IF EXISTS nationality_master_nabidh;

-- State Mapping
CREATE TABLE state_master_nabidh (
    nabidh_code CHARACTER VARYING,
    nabidh_description CHARACTER VARYING,
    hms_name CHARACTER VARYING,
    is_default CHARACTER VARYING
);
insert into state_master_nabidh values ('1','Abu Dhabi','ABU DHABI',null);
insert into state_master_nabidh values ('2','Dubai','DUBAI',null);
insert into state_master_nabidh values ('3','Sharjah','SHARJAH',null);
insert into state_master_nabidh values ('4','Ajman','AJMAN',null);
insert into state_master_nabidh values ('5','Ras al Khaimah','RAS AL KHAIMAH',null);
insert into state_master_nabidh values ('6','Fujairah','FUJAIRAH',null);
insert into state_master_nabidh values ('0','Other','UMM AL QUWAIN',null);
INSERT INTO code_sets 
(SELECT nextval('code_sets_seq'),7, cs.id, sm.id, nabidh_description, nabidh_code, null 
    FROM state_master_nabidh smt 
    JOIN state_master sm ON (lower(sm.state_name) = lower(smt.hms_name))
    JOIN code_systems cs ON (cs.label = 'NABIDH')
    WHERE sm.id NOT IN (
        SELECT cs.entity_id FROM code_sets cs, code_systems css WHERE cs.code_system_category_id=7 AND cs.code_system_id=css.id AND css.label = 'NABIDH'));
DROP TABLE IF EXISTS state_master_nabidh;

-- Country Mapping
CREATE TABLE country_master_nabidh (
    nabidh_code CHARACTER VARYING,
    nabidh_description CHARACTER VARYING,
    hms_name CHARACTER VARYING,
    is_default CHARACTER VARYING
);
insert into country_master_nabidh values ('4','Afghanistan','Afghanistan',null);
insert into country_master_nabidh values ('1','Other','African Development Bank',null);
insert into country_master_nabidh values ('1','Other','African Export-Import Bank',null);
insert into country_master_nabidh values ('248','Åland Islands','Åland Islands',null);
insert into country_master_nabidh values ('8','Albania','Albania',null);
insert into country_master_nabidh values ('12','Algeria','Algeria',null);
insert into country_master_nabidh values ('16','American Samoa','American Samoa',null);
insert into country_master_nabidh values ('20','Andorra','Andorra',null);
insert into country_master_nabidh values ('24','Angola','Angola',null);
insert into country_master_nabidh values ('660','Anguilla','Anguilla',null);
insert into country_master_nabidh values ('1','Other','Antarctica',null);
insert into country_master_nabidh values ('28','Antigua and Barbuda','Antigua and Barbuda',null);
insert into country_master_nabidh values ('32','Argentina','Argentina',null);
insert into country_master_nabidh values ('51','Armenia','Armenia',null);
insert into country_master_nabidh values ('533','Aruba','Aruba',null);
insert into country_master_nabidh values ('36','Australia','Australia',null);
insert into country_master_nabidh values ('40','Austria','Austria',null);
insert into country_master_nabidh values ('31','Azerbaijan','Azerbaijan',null);
insert into country_master_nabidh values ('44','Bahamas','Bahamas',null);
insert into country_master_nabidh values ('48','Bahrain','Bahrain',null);
insert into country_master_nabidh values ('50','Bangladesh','Bangladesh',null);
insert into country_master_nabidh values ('52','Barbados','Barbados',null);
insert into country_master_nabidh values ('112','Belarus','Belarus',null);
insert into country_master_nabidh values ('56','Belgium','Belgium',null);
insert into country_master_nabidh values ('84','Belize','Belize',null);
insert into country_master_nabidh values ('204','Benin','Benin',null);
insert into country_master_nabidh values ('60','Bermuda','Bermuda',null);
insert into country_master_nabidh values ('64','Bhutan','Bhutan',null);
insert into country_master_nabidh values ('68','Bolivia (Plurinational State of)','Bolivia',null);
insert into country_master_nabidh values ('535','Bonaire, Sint Eustatius and Saba','Bonaire, Sint Eustatius and Saba',null);
insert into country_master_nabidh values ('70','Bosnia and Herzegovina','Bosnia and Herzegovina',null);
insert into country_master_nabidh values ('72','Botswana','Botswana',null);
insert into country_master_nabidh values ('1','Other','Bouvet Island',null);
insert into country_master_nabidh values ('76','Brazil','Brazil',null);
insert into country_master_nabidh values ('92','British Virgin Islands','British Indian Ocean Territory',null);
insert into country_master_nabidh values ('1','Other','British National',null);
insert into country_master_nabidh values ('1','Other','British Overseas Citizen',null);
insert into country_master_nabidh values ('1','Other','British Overseas Territories Citizen',null);
insert into country_master_nabidh values ('1','Other','British Protected Person',null);
insert into country_master_nabidh values ('1','Other','British Subject',null);
insert into country_master_nabidh values ('96','Brunei Darussalam','Brunei Darussalam',null);
insert into country_master_nabidh values ('100','Bulgaria','Bulgaria',null);
insert into country_master_nabidh values ('854','Burkina Faso','Burkina Faso',null);
insert into country_master_nabidh values ('108','Burundi','Burundi',null);
insert into country_master_nabidh values ('132','Cabo Verde','Cabo Verde',null);
insert into country_master_nabidh values ('116','Cambodia','Cambodia',null);
insert into country_master_nabidh values ('120','Cameroon','Cameroon',null);
insert into country_master_nabidh values ('124','Canada','Canada',null);
insert into country_master_nabidh values ('1','Other','Caribbean Community or one of its emissaries',null);
insert into country_master_nabidh values ('136','Cayman Islands','Cayman Islands',null);
insert into country_master_nabidh values ('140','Central African Republic','Central African Republic',null);
insert into country_master_nabidh values ('148','Chad','Chad',null);
insert into country_master_nabidh values ('152','Chile','Chile',null);
insert into country_master_nabidh values ('156','China','China',null);
insert into country_master_nabidh values ('1','Other','Christmas Island',null);
insert into country_master_nabidh values ('1','Other','Cocos (Keeling) Islands',null);
insert into country_master_nabidh values ('170','Colombia','Colombia',null);
insert into country_master_nabidh values ('1','Other','Common Market for Eastern and Southern Africa',null);
insert into country_master_nabidh values ('174','Comoros','Comoros',null);
insert into country_master_nabidh values ('178','Congo','Congo',null);
insert into country_master_nabidh values ('180','Democratic Republic of the Congo','Congo (Democratic Republic of the)',null);
insert into country_master_nabidh values ('184','Cook Islands','Cook Islands',null);
insert into country_master_nabidh values ('188','Costa Rica','Costa Rica',null);
insert into country_master_nabidh values ('384','Côte d''Ivoire','Cote d Ivoire',null);
insert into country_master_nabidh values ('191','Croatia','Croatia',null);
insert into country_master_nabidh values ('192','Cuba','Cuba',null);
insert into country_master_nabidh values ('531','Curaçao','Curaçao',null);
insert into country_master_nabidh values ('196','Cyprus','Cyprus',null);
insert into country_master_nabidh values ('203','Czech Republic','Czechia',null);
insert into country_master_nabidh values ('208','Denmark','Denmark',null);
insert into country_master_nabidh values ('262','Djibouti','Djibouti',null);
insert into country_master_nabidh values ('212','Dominica','Dominica',null);
insert into country_master_nabidh values ('214','Dominican Republic','Dominican Republic',null);
insert into country_master_nabidh values ('1','Other','Economic Community of West African States',null);
insert into country_master_nabidh values ('218','Ecuador','Ecuador',null);
insert into country_master_nabidh values ('818','Egypt','Egypt',null);
insert into country_master_nabidh values ('222','El Salvador','El Salvador',null);
insert into country_master_nabidh values ('226','Equatorial Guinea','Equatorial Guinea',null);
insert into country_master_nabidh values ('232','Eritrea','Eritrea',null);
insert into country_master_nabidh values ('233','Estonia','Estonia',null);
insert into country_master_nabidh values ('231','Ethiopia','Ethiopia',null);
insert into country_master_nabidh values ('1','Other','European Union',null);
insert into country_master_nabidh values ('238','Falkland Islands (Malvinas)','Falkland Islands (Malvinas)',null);
insert into country_master_nabidh values ('234','Faeroe Islands','Faroe Islands',null);
insert into country_master_nabidh values ('242','Fiji','Fiji',null);
insert into country_master_nabidh values ('246','Finland','Finland',null);
insert into country_master_nabidh values ('250','France','France',null);
insert into country_master_nabidh values ('254','French Guiana','French Guiana',null);
insert into country_master_nabidh values ('258','French Polynesia','French Polynesia',null);
insert into country_master_nabidh values ('1','Other','French Southern Territories',null);
insert into country_master_nabidh values ('266','Gabon','Gabon',null);
insert into country_master_nabidh values ('270','Gambia','Gambia',null);
insert into country_master_nabidh values ('268','Georgia','Georgia',null);
insert into country_master_nabidh values ('276','Germany','Germany',null);
insert into country_master_nabidh values ('288','Ghana','Ghana',null);
insert into country_master_nabidh values ('292','Gibraltar','Gibraltar',null);
insert into country_master_nabidh values ('300','Greece','Greece',null);
insert into country_master_nabidh values ('304','Greenland','Greenland',null);
insert into country_master_nabidh values ('308','Grenada','Grenada',null);
insert into country_master_nabidh values ('312','Guadeloupe','Guadeloupe',null);
insert into country_master_nabidh values ('316','Guam','Guam',null);
insert into country_master_nabidh values ('320','Guatemala','Guatemala',null);
insert into country_master_nabidh values ('831','Guernsey','Guernsey',null);
insert into country_master_nabidh values ('324','Guinea','Guinea',null);
insert into country_master_nabidh values ('624','Guinea-Bissau','Guinea-Bissau',null);
insert into country_master_nabidh values ('328','Guyana','Guyana',null);
insert into country_master_nabidh values ('332','Haiti','Haiti',null);
insert into country_master_nabidh values ('1','Other','Heard Island and McDonald Islands',null);
insert into country_master_nabidh values ('340','Honduras','Honduras',null);
insert into country_master_nabidh values ('344','China, Hong Kong Special Administrative Region','Hong Kong',null);
insert into country_master_nabidh values ('348','Hungary','Hungary',null);
insert into country_master_nabidh values ('352','Iceland','Iceland',null);
insert into country_master_nabidh values ('356','India','INDIA',null);
insert into country_master_nabidh values ('356','India','India',null);
insert into country_master_nabidh values ('360','Indonesia','Indonesia',null);
insert into country_master_nabidh values ('1','Other','International Criminal Police Organization',null);
insert into country_master_nabidh values ('364','Iran (Islamic Republic of)','Iran',null);
insert into country_master_nabidh values ('368','Iraq','Iraq',null);
insert into country_master_nabidh values ('372','Ireland','Ireland',null);
insert into country_master_nabidh values ('833','Isle of Man','Isle of Man',null);
insert into country_master_nabidh values ('376','Israel','Israel',null);
insert into country_master_nabidh values ('380','Italy','Italy',null);
insert into country_master_nabidh values ('388','Jamaica','Jamaica',null);
insert into country_master_nabidh values ('392','Japan','Japan',null);
insert into country_master_nabidh values ('832','Jersey','Jersey',null);
insert into country_master_nabidh values ('400','Jordan','Jordan',null);
insert into country_master_nabidh values ('398','Kazakhstan','Kazakhstan',null);
insert into country_master_nabidh values ('404','Kenya','Kenya',null);
insert into country_master_nabidh values ('296','Kiribati','Kiribati',null);
insert into country_master_nabidh values ('414','Kuwait','Kuwait',null);
insert into country_master_nabidh values ('417','Kyrgyzstan','Kyrgyzstan',null);
insert into country_master_nabidh values ('418','Lao People''s Democratic Republic','Laos',null);
insert into country_master_nabidh values ('428','Latvia','Latvia',null);
insert into country_master_nabidh values ('422','Lebanon','Lebanon',null);
insert into country_master_nabidh values ('426','Lesotho','Lesotho',null);
insert into country_master_nabidh values ('430','Liberia','Liberia',null);
insert into country_master_nabidh values ('434','Libya','Libya',null);
insert into country_master_nabidh values ('438','Liechtenstein','Liechtenstein',null);
insert into country_master_nabidh values ('440','Lithuania','Lithuania',null);
insert into country_master_nabidh values ('442','Luxembourg','Luxembourg',null);
insert into country_master_nabidh values ('446','China, Macao Special Administrative Region','Macao',null);
insert into country_master_nabidh values ('807','The former Yugoslav Republic of Macedonia','Macedonia',null);
insert into country_master_nabidh values ('450','Madagascar','Madagascar',null);
insert into country_master_nabidh values ('454','Malawi','Malawi',null);
insert into country_master_nabidh values ('458','Malaysia','Malaysia',null);
insert into country_master_nabidh values ('462','Maldives','Maldives',null);
insert into country_master_nabidh values ('466','Mali','Mali',null);
insert into country_master_nabidh values ('470','Malta','Malta',null);
insert into country_master_nabidh values ('584','Marshall Islands','Marshall Islands',null);
insert into country_master_nabidh values ('474','Martinique','Martinique',null);
insert into country_master_nabidh values ('478','Mauritania','Mauritania',null);
insert into country_master_nabidh values ('480','Mauritius','Mauritius',null);
insert into country_master_nabidh values ('175','Mayotte','Mayotte',null);
insert into country_master_nabidh values ('484','Mexico','Mexico',null);
insert into country_master_nabidh values ('583','Micronesia (Federated States of)','Micronesia',null);
insert into country_master_nabidh values ('498','Republic of Moldova','Moldova',null);
insert into country_master_nabidh values ('492','Monaco','Monaco',null);
insert into country_master_nabidh values ('496','Mongolia','Mongolia',null);
insert into country_master_nabidh values ('499','Montenegro','Montenegro',null);
insert into country_master_nabidh values ('500','Montserrat','Montserrat',null);
insert into country_master_nabidh values ('504','Morocco','Morocco',null);
insert into country_master_nabidh values ('508','Mozambique','Mozambique',null);
insert into country_master_nabidh values ('104','Myanmar','Myanmar',null);
insert into country_master_nabidh values ('516','Namibia','Namibia',null);
insert into country_master_nabidh values ('520','Nauru','Nauru',null);
insert into country_master_nabidh values ('524','Nepal','Nepal',null);
insert into country_master_nabidh values ('528','Netherlands','Netherlands',null);
insert into country_master_nabidh values ('540','New Caledonia','New Caledonia',null);
insert into country_master_nabidh values ('554','New Zealand','New Zealand',null);
insert into country_master_nabidh values ('558','Nicaragua','Nicaragua',null);
insert into country_master_nabidh values ('562','Niger','Niger',null);
insert into country_master_nabidh values ('566','Nigeria','Nigeria',null);
insert into country_master_nabidh values ('570','Niue','Niue',null);
insert into country_master_nabidh values ('574','Norfolk Island','Norfolk Island',null);
insert into country_master_nabidh values ('408','Democratic People''s Republic of Korea','North Korea',null);
insert into country_master_nabidh values ('580','Northern Mariana Islands','Northern Mariana Islands',null);
insert into country_master_nabidh values ('578','Norway','Norway',null);
insert into country_master_nabidh values ('512','Oman','Oman',null);
insert into country_master_nabidh values ('1','Other','OTHER',null);
insert into country_master_nabidh values ('586','Pakistan','Pakistan',null);
insert into country_master_nabidh values ('585','Palau','Palau',null);
insert into country_master_nabidh values ('1','Other','Palestine',null);
insert into country_master_nabidh values ('591','Panama','Panama',null);
insert into country_master_nabidh values ('598','Papua New Guinea','Papua New Guinea',null);
insert into country_master_nabidh values ('600','Paraguay','Paraguay',null);
insert into country_master_nabidh values ('1','Other','Person of unspecified nationality',null);
insert into country_master_nabidh values ('604','Peru','Peru',null);
insert into country_master_nabidh values ('608','Philippines','Philippines',null);
insert into country_master_nabidh values ('612','Pitcairn','Pitcairn',null);
insert into country_master_nabidh values ('616','Poland','Poland',null);
insert into country_master_nabidh values ('620','Portugal','Portugal',null);
insert into country_master_nabidh values ('630','Puerto Rico','Puerto Rico',null);
insert into country_master_nabidh values ('634','Qatar','Qatar',null);
insert into country_master_nabidh values ('1','Other','Refugee',null);
insert into country_master_nabidh values ('1','Other','Refugee as defined in Article 1 of the 1951',null);
insert into country_master_nabidh values ('410','Republic of Korea','Resident of Kosovo',null);
insert into country_master_nabidh values ('638','Réunion','Réunion',null);
insert into country_master_nabidh values ('642','Romania','Romania',null);
insert into country_master_nabidh values ('643','Russian Federation','Russian Federation',null);
insert into country_master_nabidh values ('646','Rwanda','Rwanda',null);
insert into country_master_nabidh values ('652','Saint Barthélemy','Saint Barthélemy',null);
insert into country_master_nabidh values ('654','Saint Helena','Saint Helena, Ascension and Tristan da Cunha',null);
insert into country_master_nabidh values ('659','Saint Kitts and Nevis','Saint Kitts and Nevis',null);
insert into country_master_nabidh values ('662','Saint Lucia','Saint Lucia',null);
insert into country_master_nabidh values ('663','Saint Martin (French part)','Saint Martin (French)',null);
insert into country_master_nabidh values ('666','Saint Pierre and Miquelon','Saint Pierre and Miquelon',null);
insert into country_master_nabidh values ('670','Saint Vincent and the Grenadines','Saint Vincent and the Grenadines',null);
insert into country_master_nabidh values ('882','Samoa','Samoa',null);
insert into country_master_nabidh values ('674','San Marino','San Marino',null);
insert into country_master_nabidh values ('678','Sao Tome and Principe','Sao Tome and Principe',null);
insert into country_master_nabidh values ('682','Saudi Arabia','Saudi Arabia',null);
insert into country_master_nabidh values ('686','Senegal','Senegal',null);
insert into country_master_nabidh values ('688','Serbia','Serbia',null);
insert into country_master_nabidh values ('690','Seychelles','Seychelles',null);
insert into country_master_nabidh values ('694','Sierra Leone','Sierra Leone',null);
insert into country_master_nabidh values ('702','Singapore','Singapore',null);
insert into country_master_nabidh values ('534','Sint Maarten (Dutch part)','Sint Maarten (Dutch)',null);
insert into country_master_nabidh values ('703','Slovakia','Slovakia',null);
insert into country_master_nabidh values ('705','Slovenia','Slovenia',null);
insert into country_master_nabidh values ('90','Solomon Islands','Solomon Islands',null);
insert into country_master_nabidh values ('706','Somalia','Somalia',null);
insert into country_master_nabidh values ('710','South Africa','South Africa',null);
insert into country_master_nabidh values ('1','Other','South Georgia and the South Sandwich Islands',null);
insert into country_master_nabidh values ('408','Democratic People''s Republic of Korea','South Korea',null);
insert into country_master_nabidh values ('728','South Sudan','South Sudan',null);
insert into country_master_nabidh values ('470','Malta','Sovereign Military Order of Malta',null);
insert into country_master_nabidh values ('724','Spain','Spain',null);
insert into country_master_nabidh values ('144','Sri Lanka','Sri Lanka',null);
insert into country_master_nabidh values ('275','State of Palestine','Stateless Person',null);
insert into country_master_nabidh values ('729','Sudan','Sudan',null);
insert into country_master_nabidh values ('740','Suriname','Suriname',null);
insert into country_master_nabidh values ('744','Svalbard and Jan Mayen Islands','Svalbard and Jan Mayen',null);
insert into country_master_nabidh values ('748','Swaziland','Swaziland',null);
insert into country_master_nabidh values ('752','Sweden','Sweden',null);
insert into country_master_nabidh values ('756','Switzerland','Switzerland',null);
insert into country_master_nabidh values ('760','Syrian Arab Republic','Syrian Arab Republic',null);
insert into country_master_nabidh values ('1','Other','Taiwan',null);
insert into country_master_nabidh values ('762','Tajikistan','Tajikistan',null);
insert into country_master_nabidh values ('834','United Republic of Tanzania','Tanzania',null);
insert into country_master_nabidh values ('764','Thailand','Thailand',null);
insert into country_master_nabidh values ('626','Timor-Leste','Timor-Leste',null);
insert into country_master_nabidh values ('768','Togo','Togo',null);
insert into country_master_nabidh values ('772','Tokelau','Tokelau',null);
insert into country_master_nabidh values ('776','Tonga','Tonga',null);
insert into country_master_nabidh values ('780','Trinidad and Tobago','Trinidad and Tobago',null);
insert into country_master_nabidh values ('788','Tunisia','Tunisia',null);
insert into country_master_nabidh values ('792','Turkey','Turkey',null);
insert into country_master_nabidh values ('795','Turkmenistan','Turkmenistan',null);
insert into country_master_nabidh values ('796','Turks and Caicos Islands','Turks and Caicos Islands',null);
insert into country_master_nabidh values ('798','Tuvalu','Tuvalu',null);
insert into country_master_nabidh values ('800','Uganda','Uganda',null);
insert into country_master_nabidh values ('804','Ukraine','Ukraine',null);
insert into country_master_nabidh values ('784','United Arab Emirates','United Arab Emirates',null);
insert into country_master_nabidh values ('784','United Arab Emirates','United Arab Emirates_inactive',null);
insert into country_master_nabidh values ('826','United Kingdom of Great Britain and Northern Ireland','United Kingdom',null);
insert into country_master_nabidh values ('1','Other','United Nations Organization',null);
insert into country_master_nabidh values ('1','Other','United Nations Specialized Agency',null);
insert into country_master_nabidh values ('840','United States of America','United States of America',null);
insert into country_master_nabidh values ('858','Uruguay','Uruguay',null);
insert into country_master_nabidh values ('1','Other','US Minor Outlying Islands',null);
insert into country_master_nabidh values ('860','Uzbekistan','Uzbekistan',null);
insert into country_master_nabidh values ('548','Vanuatu','Vanuatu',null);
insert into country_master_nabidh values ('1','Other','Vatican City',null);
insert into country_master_nabidh values ('862','Venezuela (Bolivarian Republic of)','Venezuela',null);
insert into country_master_nabidh values ('704','Viet Nam','Viet Nam',null);
insert into country_master_nabidh values ('850','United States Virgin Islands','Virgin Islands (British)',null);
insert into country_master_nabidh values ('850','United States Virgin Islands','Virgin Islands (U.S.)',null);
insert into country_master_nabidh values ('876','Wallis and Futuna Islands','Wallis and Futuna',null);
insert into country_master_nabidh values ('732','Western Sahara','Western Sahara',null);
insert into country_master_nabidh values ('887','Yemen','Yemen',null);
insert into country_master_nabidh values ('894','Zambia','Zambia',null);
insert into country_master_nabidh values ('716','Zimbabwe','Zimbabwe',null);
INSERT INTO code_sets 
(SELECT nextval('code_sets_seq'),8, cs.id, cm.id, nabidh_description, nabidh_code, null 
    FROM country_master_nabidh cmt 
    JOIN country_master cm ON (lower(cm.country_name) = lower(cmt.hms_name))
    JOIN code_systems cs ON (cs.label = 'NABIDH')
    WHERE cm.id NOT IN (
        SELECT cs.entity_id FROM code_sets cs, code_systems css WHERE cs.code_system_category_id=8 AND cs.code_system_id=css.id AND css.label = 'NABIDH'));
DROP TABLE IF EXISTS country_master_nabidh;

-- Diagnosis_ststus
CREATE TABLE diagnosis_statuses_nabidh (
    nabidh_code CHARACTER VARYING,
    nabidh_description CHARACTER VARYING,
    hms_name CHARACTER VARYING,
    is_default CHARACTER VARYING
);
insert into diagnosis_statuses_nabidh values ('A','Admitting','Admitting',null);
insert into diagnosis_statuses_nabidh values ('A','Admitting','Other',null);
insert into diagnosis_statuses_nabidh values ('W','Working','Working',null);
insert into diagnosis_statuses_nabidh values ('W','Working','Probable',null);
insert into diagnosis_statuses_nabidh values ('F','Final','Final',null);
insert into diagnosis_statuses_nabidh values ('F','Final','Confirmed',null);
INSERT INTO code_sets 
(SELECT nextval('code_sets_seq'),9, cs.id, ds.diagnosis_status_id, nabidh_description, nabidh_code, null 
    FROM diagnosis_statuses_nabidh dst 
    JOIN diagnosis_statuses ds ON (lower(ds.diagnosis_status_name) = lower(dst.hms_name))
    JOIN code_systems cs ON (cs.label = 'NABIDH')
    WHERE ds.diagnosis_status_id NOT IN (
        SELECT cs.entity_id FROM code_sets cs, code_systems css WHERE cs.code_system_category_id=9 AND cs.code_system_id=css.id AND css.label = 'NABIDH'));
DROP TABLE IF EXISTS diagnosis_statuses_nabidh;

-- Diagnostic Department
CREATE TABLE diagnostics_departments_nabidh (
    nabidh_code CHARACTER VARYING,
    nabidh_description CHARACTER VARYING,
    hms_name CHARACTER VARYING,
    is_default CHARACTER VARYING
);
insert into diagnostics_departments_nabidh values ('CH','Chemistry','Biochemistry',null);
insert into diagnostics_departments_nabidh values ('RAD','Radiology','Bone And Joint Studies',null);
insert into diagnostics_departments_nabidh values ('RAD','Radiology','Breast Radiology',null);
insert into diagnostics_departments_nabidh values ('LAB','Laboratory','Clinical Pathology',null);
insert into diagnostics_departments_nabidh values ('CT','CAT Scan','CT SCAN',null);
insert into diagnostics_departments_nabidh values ('RAD','Radiology','Diagnostic Imaging',null);
insert into diagnostics_departments_nabidh values ('HM','Hematology','Hematology',null);
insert into diagnostics_departments_nabidh values ('CP','Cytopathology','Histopathology & Cytopathology',null);
insert into diagnostics_departments_nabidh values ('LAB','Laboratory','Hormones',null);
insert into diagnostics_departments_nabidh values ('LAB','Laboratory','Laboratory',null);
insert into diagnostics_departments_nabidh values ('NMR','Nuclear Magnetic Resonance','M.R.I SCAN',null);
insert into diagnostics_departments_nabidh values ('OTH','Other','MAMMOGRAPHY',null);
insert into diagnostics_departments_nabidh values ('MB','Microbiology','Microbiology',null);
insert into diagnostics_departments_nabidh values ('OTH','Other','Molecular Medicine',null);
insert into diagnostics_departments_nabidh values ('VUS','Vascular Ultrasound','Noninvasive Vascular Diagnostic Studies',null);
insert into diagnostics_departments_nabidh values ('NMS','Nuclear Medicine Scan','Nuclear Medicine',null);
insert into diagnostics_departments_nabidh values ('RT','Radiation Therapy','Radiation Oncology',null);
insert into diagnostics_departments_nabidh values ('RAD','Radiology','Radiologic Guidance',null);
insert into diagnostics_departments_nabidh values ('RAD','Radiology','Radiology',null);
insert into diagnostics_departments_nabidh values ('SR','Serology','Serology',null);
insert into diagnostics_departments_nabidh values ('OTH','Other','Supervision & Interpretation',null);
insert into diagnostics_departments_nabidh values ('RUS','Radiology Ultrasound','ULTRA SOUND SCAN',null);
insert into diagnostics_departments_nabidh values ('RAD','Radiology','X RAY',null);
INSERT INTO code_sets 
(SELECT nextval('code_sets_seq'),1, cs.id, ds.diag_dept_id, nabidh_description, nabidh_code, null 
    FROM diagnostics_departments_nabidh dst 
    JOIN diagnostics_departments ds ON (lower(ds.ddept_name) = lower(dst.hms_name))
    JOIN code_systems cs ON (cs.label = 'NABIDH')
    WHERE ds.diag_dept_id NOT IN (
        SELECT cs.entity_id FROM code_sets cs, code_systems css WHERE cs.code_system_category_id=1 AND cs.code_system_id=css.id AND css.label = 'NABIDH'));
DROP TABLE IF EXISTS diagnostics_departments_nabidh;

-- Race
CREATE TABLE race_master_nabidh (
    nabidh_code CHARACTER VARYING,
    nabidh_description CHARACTER VARYING,
    hms_name CHARACTER VARYING,
    is_default CHARACTER VARYING
);
insert into race_master_nabidh values ('2028-9','Asian','Asian',null);
insert into race_master_nabidh values ('2106-3','European','White',null);
insert into race_master_nabidh values ('2131-1','Other Race','Other Race',null);
insert into race_master_nabidh values ('2131-1','Other Race','Native Hawaiian or Other Pacific Islander',null);
insert into race_master_nabidh values ('1002-5','American','American Indian or Alaska Native',null);
insert into race_master_nabidh values ('2054-5','African','Black or African American',null);
INSERT INTO code_sets 
(SELECT nextval('code_sets_seq'),13, cs.id, ds.race_id, nabidh_description, nabidh_code, null 
    FROM race_master_nabidh dst 
    JOIN race_master ds ON (lower(ds.race_name) = lower(dst.hms_name))
    JOIN code_systems cs ON (cs.label = 'NABIDH')
    WHERE ds.race_id NOT IN (
        SELECT cs.entity_id FROM code_sets cs, code_systems css WHERE cs.code_system_category_id=13 AND cs.code_system_id=css.id AND css.label = 'NABIDH'));
DROP TABLE IF EXISTS race_master_nabidh;

-- Medicine Route
CREATE TABLE medicine_route_nabidh (
    nabidh_code CHARACTER VARYING,
    nabidh_description CHARACTER VARYING,
    hms_name CHARACTER VARYING,
    is_default CHARACTER VARYING
);
insert into medicine_route_nabidh values ('ROA159','Arteriovenous','ARTERIOVENOUS',null);
insert into medicine_route_nabidh values ('ROA148','Buccal','BUCCAL',null);
insert into medicine_route_nabidh values ('ROA192','Cardiac Perfusion','CARDIAC PERFUSION',null);
insert into medicine_route_nabidh values ('ROA192','Cardiac Perfusion','CARDIAC PERFUSION',null);
insert into medicine_route_nabidh values ('ROA160','Central Nerve Block','CENTRAL NERVE BLOCK',null);
insert into medicine_route_nabidh values ('ROA160','Central Nerve Block','CENTRAL NERVE BLOCK',null);
insert into medicine_route_nabidh values ('ROA161','Deep Sc','DEEP SC',null);
insert into medicine_route_nabidh values ('ROA161','Deep Sc','DEEP SC',null);
insert into medicine_route_nabidh values ('ROA001','Dental','DENTAL',null);
insert into medicine_route_nabidh values ('ROA162','Dermal','DERMAL',null);
insert into medicine_route_nabidh values ('ROA162','Dermal','DERMAL',null);
insert into medicine_route_nabidh values ('ROA163','Endotracheal','ENDOTRACHEAL',null);
insert into medicine_route_nabidh values ('ROA163','Endotracheal','ENDOTRACHEAL',null);
insert into medicine_route_nabidh values ('ROA007','Epidural','EPIDURAL',null);
insert into medicine_route_nabidh values ('ROA011','Genital','GENITAL',null);
insert into medicine_route_nabidh values ('ROA164','Iliohypogastric (Nerve Block)','ILIOHYPOGASTRIC (NERVE BLOCK)',null);
insert into medicine_route_nabidh values ('ROA164','Iliohypogastric (Nerve Block)','ILIOHYPOGASTRIC (NERVE BLOCK)',null);
insert into medicine_route_nabidh values ('ROA165','Ilionguinal','ILIONGUINAL',null);
insert into medicine_route_nabidh values ('ROA032','Implant','IMPLANT',null);
insert into medicine_route_nabidh values ('ROA033','Infiltration Dental Block','INFILTRATION DENTAL BLOCK',null);
insert into medicine_route_nabidh values ('ROA034','Inhalation','INHALATION',null);
insert into medicine_route_nabidh values ('ROA036','Intra-Amniotic','INTRA-AMNIOTIC',null);
insert into medicine_route_nabidh values ('ROA166','Intra-Arterial','INTRA-ARTERIAL',null);
insert into medicine_route_nabidh values ('ROA166','Intra-Arterial','INTRA-ARTERIAL',null);
insert into medicine_route_nabidh values ('ROA042','Intra-Mastitis','INTRA-MASTITIS',null);
insert into medicine_route_nabidh values ('ROA045','Intra-Periodontal Pocket','INTRA-PERIODONTAL POCKET',null);
insert into medicine_route_nabidh values ('ROA046','Intra-Peritoneal','INTRA-PERITONEAL',null);
insert into medicine_route_nabidh values ('ROA172','Intra-Pulmonary','INTRA-PULMONARY',null);
insert into medicine_route_nabidh values ('ROA172','Intra-Pulmonary','INTRA-PULMONARY',null);
insert into medicine_route_nabidh values ('ROA050','Intra-Vesically','INTRA-VESICALLY',null);
insert into medicine_route_nabidh values ('ROA167','Intrabursal','INTRABURSAL',null);
insert into medicine_route_nabidh values ('ROA167','Intrabursal','INTRABURSAL',null);
insert into medicine_route_nabidh values ('ROA109','Intracardiac','INTRACARDIAC',null);
insert into medicine_route_nabidh values ('ROA039','Intracavernosal','INTRACAVERNOSAL',null);
insert into medicine_route_nabidh values ('ROA168','Intracavitiry','INTRACAVITIRY',null);
insert into medicine_route_nabidh values ('ROA168','Intracavitiry','INTRACAVITIRY',null);
insert into medicine_route_nabidh values ('ROA112','Intradermal','INTRADERMAL',null);
insert into medicine_route_nabidh values ('ROA041','Intragluteally','INTRAGLUTEALLY',null);
insert into medicine_route_nabidh values ('ROA169','Intralesional','INTRALESIONAL',null);
insert into medicine_route_nabidh values ('ROA170','Intramedullarily','INTRAMEDULLARILY',null);
insert into medicine_route_nabidh values ('ROA043','Intrammamary','INTRAMMAMARY',null);
insert into medicine_route_nabidh values ('ROA171','Intraosseous','INTRAOSSEOUS',null);
insert into medicine_route_nabidh values ('ROA171','Intraosseous','INTRAOSSEOUS',null);
insert into medicine_route_nabidh values ('ROA123','Intrapleural','INTRAPLEURAL',null);
insert into medicine_route_nabidh values ('ROA173','Intraspinal','INTRASPINAL',null);
insert into medicine_route_nabidh values ('ROA173','Intraspinal','INTRASPINAL',null);
insert into medicine_route_nabidh values ('ROA174','Intrasublesional','INTRASUBLESIONAL',null);
insert into medicine_route_nabidh values ('ROA174','Intrasublesional','INTRASUBLESIONAL',null);
insert into medicine_route_nabidh values ('ROA175','Intrasynovial','INTRASYNOVIAL',null);
insert into medicine_route_nabidh values ('ROA175','Intrasynovial','INTRASYNOVIAL',null);
insert into medicine_route_nabidh values ('ROA049','Intravaginal','INTRAVAGINAL',null);
insert into medicine_route_nabidh values ('ROA114','Intraventricular','INTRAVENTRICULAR',null);
insert into medicine_route_nabidh values ('ROA141','Irrigation','IRRIGATION',null);
insert into medicine_route_nabidh values ('ROA051','IV','IV',null);
insert into medicine_route_nabidh values ('ROA176','IV Bolus','IV BOLUS',null);
insert into medicine_route_nabidh values ('ROA176','IV Bolus','IV BOLUS',null);
insert into medicine_route_nabidh values ('ROA052','IV Catheter','IV CATHETER',null);
insert into medicine_route_nabidh values ('ROA012','IM','IV IM',null);
insert into medicine_route_nabidh values ('ROA053','IV Infusion','IV INFUSION',null);
insert into medicine_route_nabidh values ('ROA065','Local Oral Dental','LOCAL ORAL DENTAL',null);
insert into medicine_route_nabidh values ('ROA101','Miscellaneous','MISCELLANEOUS',null);
insert into medicine_route_nabidh values ('ROA067','Nasal','NASAL',null);
insert into medicine_route_nabidh values ('ROA071','Nebulisation','NEBULISATION',null);
insert into medicine_route_nabidh values ('ROA072','Ocular','OCULAR',null);
insert into medicine_route_nabidh values ('ROA074','Oral','ORAL',null);
insert into medicine_route_nabidh values ('ROA177','Orofacial','OROFACIAL',null);
insert into medicine_route_nabidh values ('ROA177','Orofacial','OROFACIAL',null);
insert into medicine_route_nabidh values ('ROA178','Oropharyngeal','OROPHARYNGEAL',null);
insert into medicine_route_nabidh values ('ROA178','Oropharyngeal','OROPHARYNGEAL',null);
insert into medicine_route_nabidh values ('ROA084','Otic','OTIC',null);
insert into medicine_route_nabidh values ('ROA085','Parenteral','PARENTERAL',null);
insert into medicine_route_nabidh values ('ROA179','Perianal','PERIANAL',null);
insert into medicine_route_nabidh values ('ROA180','Periarticular','PERIARTICULAR',null);
insert into medicine_route_nabidh values ('ROA180','Periarticular','PERIARTICULAR',null);
insert into medicine_route_nabidh values ('ROA181','Peribulbar','PERIBULBAR',null);
insert into medicine_route_nabidh values ('ROA182','Peripheral Nerve Block','PERIPHERAL NERVE BLOCK',null);
insert into medicine_route_nabidh values ('ROA087','Peritoneal Dialysis','PERITONEAL DIALYSIS',null);
insert into medicine_route_nabidh values ('ROA088','Rectal','RECTAL',null);
insert into medicine_route_nabidh values ('ROA090','Spinal','SPINAL',null);
insert into medicine_route_nabidh values ('ROA103','Subarachnoid','SUBARACHNOID',null);
insert into medicine_route_nabidh values ('ROA183','Subconjunctival','SUBCONJUNCTIVAL',null);
insert into medicine_route_nabidh values ('ROA147','Subdermal','SUBDERMAL',null);
insert into medicine_route_nabidh values ('ROA147','Subdermal','SUBDERMAL',null);
insert into medicine_route_nabidh values ('ROA184','Sympathetic Nerve Block','SYMPATHETIC NERVE BLOCK',null);
insert into medicine_route_nabidh values ('ROA184','Sympathetic Nerve Block','SYMPATHETIC NERVE BLOCK',null);
insert into medicine_route_nabidh values ('ROA094','Topical- Scalp','TOPICAL- SCALP',null);
insert into medicine_route_nabidh values ('ROA185','Transcervical','TRANSCERVICAL',null);
insert into medicine_route_nabidh values ('ROA185','Transcervical','TRANSCERVICAL',null);
insert into medicine_route_nabidh values ('ROA095','Transdermal','TRANSDERMAL',null);
insert into medicine_route_nabidh values ('ROA096','Transmucosal','TRANSMUCOSAL',null);
insert into medicine_route_nabidh values ('ROA100','Urethral','URETHRAL',null);
INSERT INTO code_sets 
(SELECT nextval('code_sets_seq'),10, cs.id, ds.route_id, nabidh_description, nabidh_code, null 
    FROM medicine_route_nabidh dst 
    JOIN medicine_route ds ON (lower(ds.route_name) = lower(dst.hms_name))
    JOIN code_systems cs ON (cs.label = 'NABIDH')
    WHERE ds.route_id NOT IN (
        SELECT cs.entity_id FROM code_sets cs, code_systems css WHERE cs.code_system_category_id=10 AND cs.code_system_id=css.id AND css.label = 'NABIDH'));
DROP TABLE IF EXISTS medicine_route_nabidh;

-- Default values for code_system_categories
UPDATE code_sets set is_default = true WHERE id = (SELECT id FROM code_sets WHERE code_system_category_id = 2 AND short_code='OTH' AND label='Other' ORDER BY id ASC LIMIT 1);
UPDATE code_sets set is_default = true WHERE id = (SELECT id FROM code_sets WHERE code_system_category_id = 3 AND short_code='U' AND label='Unknown' ORDER BY id ASC LIMIT 1);
UPDATE code_sets set is_default = true WHERE id = (SELECT id FROM code_sets WHERE code_system_category_id = 4 AND short_code='20' AND label= 'General Practitioner' ORDER BY id ASC LIMIT 1);
UPDATE code_sets set is_default = true WHERE id = (SELECT id FROM code_sets WHERE code_system_category_id = 5 AND short_code='1' AND label= 'Discharged with approval' ORDER BY id ASC LIMIT 1);
UPDATE code_sets set is_default = true WHERE id = (SELECT id FROM code_sets WHERE code_system_category_id = 6 AND short_code='BID' AND label= 'Bidoun' ORDER BY id ASC LIMIT 1);
UPDATE code_sets set is_default = true WHERE id = (SELECT id FROM code_sets WHERE code_system_category_id = 7 AND short_code='0' AND label= 'Others' ORDER BY id ASC LIMIT 1);
UPDATE code_sets set is_default = true WHERE id = (SELECT id FROM code_sets WHERE code_system_category_id = 8 AND short_code='BID' AND label= 'Without Nationality' ORDER BY id ASC LIMIT 1);
UPDATE code_sets set is_default = true WHERE id = (SELECT id FROM code_sets WHERE code_system_category_id = 9 AND short_code='A' AND label= 'Admitting' ORDER BY id ASC LIMIT 1);
