CREATE OR REPLACE FUNCTION split_doctor_name() 
RETURNS VOID AS $BODY$ 
DECLARE
    doctorRecord RECORD;
    salutationRecord RECORD;
    docName varchar(250);
    docNameWithNoSalutation varchar(250);
    docFirstName varchar(50);
    docMiddleName varchar(100);
    docLastName varchar(50);
    docNameWithNoSalutationArray TEXT[];
    arrayLength int;
    notUpdated varchar(1);
    docFirstNameLength int;
    docMiddleNameLength int;
    docLastNameLength int;
    docNameWithNoSalutationLength int;
BEGIN
    FOR doctorRecord IN SELECT doctor_id,doctor_name,doc_first_name FROM doctors LOOP
        docFirstName = doctorRecord.doc_first_name;
        docName = doctorRecord.doctor_name;
        notUpdated = 't';
        IF docFirstName IS NULL OR docFirstName = '' THEN
            FOR salutationRecord IN SELECT salutation_id,concat(salutation,'%') as salutationPattern,salutation FROM salutation_master ORDER BY length(salutation) DESC LOOP
                IF docName ILIKE salutationRecord.salutationPattern THEN
                    docNameWithNoSalutation = TRIM(SUBSTRING(docName,length(salutationRecord.salutation)+1));
                    docNameWithNoSalutationArray = string_to_array(docNameWithNoSalutation, ' ');
                    arrayLength = array_length(docNameWithNoSalutationArray,1);
                    IF arrayLength = 1 THEN
                        UPDATE doctors SET doc_salutation_id=salutationRecord.salutation_id,doc_first_name=docNameWithNoSalutationArray[1] WHERE doctor_id = doctorRecord.doctor_id;
                    ELSIF arrayLength = 2 THEN
                        UPDATE doctors SET doc_salutation_id=salutationRecord.salutation_id,doc_first_name=docNameWithNoSalutationArray[1],doc_last_name=docNameWithNoSalutationArray[arrayLength] WHERE doctor_id = doctorRecord.doctor_id;
                    ELSE
                        docNameWithNoSalutationLength = LENGTH(docNameWithNoSalutation);
                        docFirstNameLength = LENGTH(docNameWithNoSalutationArray[1]);
                        docLastNameLength = LENGTH(docNameWithNoSalutationArray[arrayLength]);
                        docMiddleName = TRIM(SUBSTRING(docNameWithNoSalutation,docFirstNameLength+1));
                        docMiddleNameLength = LENGTH(docMiddleName);
                        docMiddleName = TRIM(SUBSTRING(docMiddleName,1,docMiddleNameLength-docLastNameLength));
                        UPDATE doctors SET doc_salutation_id=salutationRecord.salutation_id,doc_first_name=docNameWithNoSalutationArray[1],doc_last_name=docNameWithNoSalutationArray[arrayLength],doc_middle_name=docMiddleName WHERE doctor_id = doctorRecord.doctor_id;
                    END IF;
                    notUpdated = 'f';
                    EXIT;
                END IF;
            END LOOP;
            IF notUpdated = 't' THEN
                docNameWithNoSalutation = TRIM(docName);
                docNameWithNoSalutationArray = string_to_array(docNameWithNoSalutation, ' ');
                arrayLength = array_length(docNameWithNoSalutationArray,1);
                IF arrayLength = 1 THEN
                    UPDATE doctors SET doc_first_name=docNameWithNoSalutationArray[1] WHERE doctor_id = doctorRecord.doctor_id;
                ELSIF arrayLength = 2 THEN
                    UPDATE doctors SET doc_first_name=docNameWithNoSalutationArray[1],doc_last_name=docNameWithNoSalutationArray[2] WHERE doctor_id = doctorRecord.doctor_id;
                ELSE
                    docNameWithNoSalutationLength = LENGTH(docNameWithNoSalutation);
                    docFirstNameLength = LENGTH(docNameWithNoSalutationArray[1]);
                    docLastNameLength = LENGTH(docNameWithNoSalutationArray[arrayLength]);
                    docMiddleName = TRIM(SUBSTRING(docNameWithNoSalutation,docFirstNameLength+1));
                    docMiddleNameLength = LENGTH(docMiddleName);
                    docMiddleName = TRIM(SUBSTRING(docMiddleName,1,docMiddleNameLength-docLastNameLength));
                    UPDATE doctors SET doc_first_name=docNameWithNoSalutationArray[1],doc_last_name=docNameWithNoSalutationArray[arrayLength],doc_middle_name=docMiddleName WHERE doctor_id = doctorRecord.doctor_id;
                END IF;
                notUpdated = 'f';
            END IF;
        END IF;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql';

select split_doctor_name();
DROP function split_doctor_name();