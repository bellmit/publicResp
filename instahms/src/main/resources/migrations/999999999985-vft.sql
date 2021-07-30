-- liquibase formatted sql
-- changeset adityabhatia02:create-views.sql splitStatements:false runAlways:true 
-- validCheckSum: ANY

-- Views, Functions and triggers: this SQL is run ON every upgrade AS well AS initial schema
-- creation. Since these only install views, functions and triggers, these can be installed
-- even if they already exist, so we don't need to store any deltas.
--
-- Rules:
--  * To add or modify a view/function/trigger: just edit this file and check in. DO NOT
--    add to db_changes.sql
--  * To delete obsolete views/functions/triggers: remove from this file
--  * Note that functions are sensitive to the parameters, that is, you cannot change a
--    function's parameter list. You have to drop the old one and create a new one.
--  * Ensure that you always drop a view/function/trigger using IF EXISTS before
--    creating a new one.
--  * Views can depend ON other views. Ensure that the order of creation is such that
--    dependent views come later.
--
--ALTER TABLE receipts ALTER COLUMN realized TYPE VARCHAR;

SET client_min_messages = warning;

--
-- Receipt with receipt usage view.
--
DROP VIEW IF EXISTS receipt_usage_view CASCADE;
CREATE VIEW receipt_usage_view AS
    SELECT r.*,
    CASE WHEN ru.entity_type = 'package_id' THEN ru.entity_id ELSE null END AS package_id,
    CASE WHEN ru.entity_type = 'visit_type' THEN 'I' WHEN ru.entity_type = 'bill_type' THEN ru.entity_id ELSE 'B' END AS deposit_available_for,
    ru.receipt_id AS receipt_usage_id,
    CASE WHEN ru.entity_type = 'visit_type' THEN entity_id ELSE null END AS visit_type,
    CASE WHEN ru.entity_type = 'bill_type' THEN entity_id ELSE null END AS bill_type
    FROM receipts r
    LEFT JOIN (
        SELECT
            receipt_id, entity_type, entity_id
        FROM receipt_usage
        WHERE entity_type IN ('package_id', 'visit_type', 'bill_type')
    ) ru
    ON ru.receipt_id = r.receipt_id;

--- Patient deposit view ---
DROP VIEW IF EXISTS patient_deposits_view CASCADE;
CREATE VIEW patient_deposits_view AS
    SELECT r.*,
    CASE WHEN ru.entity_type = 'package_id' THEN ru.entity_id ELSE null END AS package_id,
    CASE WHEN ruppd.entity_type = 'pat_package_id' THEN ruppd.entity_id ELSE null END AS pat_package_id,
    CASE WHEN ru.entity_type = 'visit_type' THEN UPPER(ru.entity_id) WHEN ru.entity_type = 'bill_type' THEN ru.entity_id ELSE 'B' END AS deposit_available_for,
    ru.receipt_id AS receipt_usage_id,
    CASE WHEN ru.entity_type = 'visit_type' THEN ru.entity_id ELSE null END AS visit_type,
    CASE WHEN ru.entity_type = 'bill_type' THEN ru.entity_id ELSE null END AS bill_type,
    ref.refund_tax_amount,ref.refund_tax_rate
    FROM receipts r
    LEFT JOIN (
        SELECT
            receipt_id, entity_type, entity_id
        FROM receipt_usage
        WHERE entity_type IN ('package_id', 'visit_type', 'bill_type')
    ) ru
    ON ru.receipt_id = r.receipt_id
	LEFT JOIN receipt_usage ruppd ON r.receipt_id = ruppd.receipt_id AND ruppd.entity_type = 'pat_package_id'
    LEFT JOIN LATERAL (
         SELECT refund_receipt_id,SUM(tax_amount) as refund_tax_amount,
                SUM(tax_rate) as refund_tax_rate FROM receipt_refund_reference rrr
                WHERE rrr.refund_receipt_id=r.receipt_id GROUP BY
                refund_receipt_id) ref ON ref.refund_receipt_id=r.receipt_id
    WHERE is_deposit;

--- Patient Package deposits view ---
DROP VIEW IF EXISTS patient_package_deposits_view CASCADE;
CREATE VIEW patient_package_deposits_view AS
    SELECT r.*,
    rup.package_id,
    rupp.pat_package_id,
    'B'::character varying AS deposit_available_for,
    null::character varying as visit_type,
    null::character varying as bill_type,
    r.receipt_id AS receipt_usage_id
    FROM receipts r
    JOIN (
        SELECT
            receipt_id, entity_id AS package_id
        FROM receipt_usage
        WHERE entity_type IN ('package_id')
    ) rup
    ON rup.receipt_id = r.receipt_id
    JOIN (
        SELECT
            receipt_id, entity_id AS pat_package_id
        FROM receipt_usage
        WHERE entity_type IN ('pat_package_id')
    ) rupp
    ON rupp.receipt_id = r.receipt_id
    WHERE is_deposit;

--- Patient IP deposits view ---
DROP VIEW IF EXISTS patient_ip_deposits_view CASCADE;
CREATE VIEW patient_ip_deposits_view AS
    SELECT r.*,
    null::character varying as package_id,
    null::character varying as pat_package_id,
    'I'::character varying AS deposit_available_for,
    ru.visit_type,
    null::character varying as bill_type,
    r.receipt_id AS receipt_usage_id
    FROM receipts r
    JOIN LATERAL (
        SELECT 
            receipt_id, entity_id AS visit_type
        FROM receipt_usage
        WHERE entity_type IN ('visit_type') AND entity_id='i'
        AND receipt_usage.receipt_id = r.receipt_id AND r.is_deposit
    ) ru
    ON ru.receipt_id = r.receipt_id AND r.is_deposit;

------------- function used in some views ----------------
DROP FUNCTION IF EXISTS commacat(text, text) CASCADE;
CREATE OR REPLACE FUNCTION commacat(initialstr text, newstr text) RETURNS text AS $$
  BEGIN
    IF newstr IS NULL OR newstr = '' THEN
      RETURN initialstr;
    ELSIF initialstr = '' THEN
	  RETURN newstr;
	ELSE
      RETURN initialstr || ', ' || newstr;
    END IF;
  END;
$$ LANGUAGE plpgsql;

CREATE AGGREGATE textcat_commacat(
  basetype    = text,
  sfunc       = commacat,
  stype       = text,
  initcond    = ''
);

--
-- Function distinct_count is an aggregate function that is useful in
-- report builders, to be used AS an aggFunc
--
DROP FUNCTION IF EXISTS final_distinct_count(text[]) CASCADE;
CREATE OR REPLACE FUNCTION final_distinct_count(accum text[]) RETURNS text AS $$
BEGIN
	RETURN COUNT(*) FROM (SELECT DISTINCT UNNEST(accum)) AS X;
END;
$$ LANGUAGE plpgsql;

CREATE AGGREGATE distinct_count(
  basetype    = text,
  sfunc       = array_append,
  stype       = text[],
  initcond    = '{}',
  finalfunc   = final_distinct_count
);

DROP FUNCTION IF EXISTS linecat(text, text) CASCADE;
CREATE OR REPLACE FUNCTION linecat(initialstr text, newstr text) RETURNS text AS $$
  BEGIN
    IF newstr IS NULL OR newstr = '' THEN
      RETURN initialstr;
    ELSIF initialstr = '' THEN
	  RETURN newstr;
	ELSE
      RETURN initialstr || E',\r\n' || newstr;
    END IF;
  END;
$$ LANGUAGE plpgsql;

CREATE AGGREGATE textcat_linecat(
  basetype    = text,
  sfunc       = linecat,
  stype       = text,
  initcond    = ''
);

DROP FUNCTION IF EXISTS generalfun(text, text) CASCADE;
CREATE OR REPLACE FUNCTION generalfun(initialstr text, newstr text) RETURNS text AS $$
  BEGIN
    IF newstr IS NULL OR newstr = '' THEN
      RETURN initialstr;
    ELSIF initialstr = '' THEN
	  RETURN newstr;
	ELSE
      RETURN initialstr || '*`' || newstr;
    END IF;
  END;
$$ LANGUAGE plpgsql;

CREATE AGGREGATE textcat_generalfun(
  basetype    = text,
  sfunc       = generalfun,
  stype       = text,
  initcond    = ''
);


-- Function to nullify empty field(s) in a given table.
-- Can be used if the field names exists AS a series of a given index range.
-- Ex: patient_details table has fields : custom_field1, custom_field2, custom_field3, ...
-- To nulliify this function is used AS SELECT nullifyEmptyFields('patient_details','custom_field','',0,13);

CREATE OR REPLACE FUNCTION nullifyEmptyFields (tbl TEXT, field_name TEXT,
   suffix_text TEXT, start_index integer, end_index integer) RETURNS VOID AS $$
DECLARE
	fld_index integer;
	fld_name text;
	empty_str text;
BEGIN
	fld_index:=start_index;
	empty_str:= E'''';
 WHILE fld_index <= end_index
 LOOP
	IF fld_index > 0 THEN fld_name:=field_name || fld_index; END IF;

	IF suffix_text IS NOT NULL AND suffix_text != ''
	  THEN fld_name:=fld_name || suffix_text; END IF;

	EXECUTE	'UPDATE ' || quote_ident(tbl) || ' SET '|| fld_name
	|| '= null where '|| 'trim('||fld_name||') = '|| empty_str || empty_str;
	fld_index := fld_index + 1;
 END LOOP;
END;
$$ LANGUAGE plpgsql;

-- 
-- Function to safely cast text to numeric. 
-- defaults to 0 if non-numeric input like null, empty string or inputs like -, --.
--
CREATE OR REPLACE FUNCTION cast_to_numeric(value text) RETURNS numeric AS $$
BEGIN
    return cast(value::varchar as numeric);
exception
    when invalid_text_representation then
        return 0;
END;
$$ LANGUAGE plpgsql immutable;

--
-- Function to safely cast text to int. 
-- defaults to 0 if non-numeric input like null, empty string or inputs like -, --.
--
CREATE OR REPLACE FUNCTION cast_to_int(value text) RETURNS int AS $$
BEGIN
    return cast(value::varchar as int);
exception
    when invalid_text_representation then
        return 0;
END;
$$ LANGUAGE plpgsql immutable;

--
-- Function to calcuate bmi
--
CREATE OR REPLACE FUNCTION calculate_bmi(height numeric, weight numeric) RETURNS numeric AS $$
BEGIN
    return trunc((weight * 10000) / (height * height), 1);
exception
    when division_by_zero then
        return 0;
END;
$$ LANGUAGE plpgsql immutable;

--
-- returns the patient full name for a patient, given mr_no
--
CREATE OR REPLACE FUNCTION get_patient_name(mrno character varying)
  RETURNS character varying AS $BODY$

DECLARE
	rec RECORD;
	fullName character varying;
BEGIN
	IF mrno is not null
		THEN
			SELECT salutation , patient_name , middle_name , last_name INTO rec
			FROM patient_details WHERE mr_no = mrno;

			fullName := get_patient_name(rec.salutation, rec.patient_name, rec.middle_name, rec.last_name);

		ELSE fullName := '';

	END IF;

	RETURN fullName;
END;
$BODY$ LANGUAGE 'plpgsql';

--
-- returns the patient full name for patient, given salutation, patient_name, middle_name, last_name
-- (use get_patient_full_name, which requires a join to salutation master instead of this one,
-- AS this function is slow, it needs to do a SELECT ON salutation master for every row.)
--
CREATE OR REPLACE FUNCTION get_patient_name(p_salutation_id character varying,
	patient_name character varying, middle_name character varying, last_name character varying)
RETURNS character varying AS $BODY$
DECLARE
	sal RECORD;
	fullName character varying;
BEGIN
	fullName := '';

	-- return null if patient_name itself is null. This is mandatory, but is used WHEN coalescing
	-- to get retail customer name. If we return '', then, coalesce will not work, it will return '' itself.
	IF patient_name IS NULL THEN
		return NULL;
	END IF;

	SELECT salutation AS salutation INTO sal FROM salutation_master WHERE salutation_id = p_salutation_id;

	IF sal.salutation is not null
		THEN fullName := fullName || sal.salutation;
	END IF;

	IF patient_name is not null
		THEN fullName := fullName ||' '|| patient_name;
	END IF;

	IF middle_name is not null
		THEN fullName := fullName ||' ' || middle_name;
	END IF;

	IF last_name is not null
		THEN fullName := fullName ||' '|| last_name;
	END IF;

	RETURN fullName;
END;
$BODY$ LANGUAGE 'plpgsql';

--
-- Utility function to get the financial year start given a date. Useful in some
-- custom reports.
--
DROP FUNCTION IF EXISTS get_fin_year_start(date) CASCADE;
CREATE OR REPLACE FUNCTION get_fin_year_start(dt date) RETURNS date AS $BODY$
BEGIN
	RETURN date_trunc('year', dt - interval '3 months') + interval '3 months';
END;
$BODY$ LANGUAGE 'plpgsql';

--
-- Same AS previous function, but takes in the actual saultation instead of the salutation
-- ID. This is much faster for queries returning many rows, since a join with salutation
-- master fetches all salutations at once instead of getting the salutation ON each row.
--
CREATE OR REPLACE FUNCTION get_patient_full_name(salutation character varying,
	patient_name character varying, middle_name character varying, last_name character varying)
RETURNS character varying AS $BODY$
DECLARE
	fullName character varying;
BEGIN
	fullName := '';

	-- return null if patient_name itself is null. This is mandatory, but is used WHEN coalescing
	-- to get retail customer name. If we return '', then, coalesce will not work, it will return '' itself.
	IF patient_name IS NULL THEN
		return NULL;
	END IF;

	IF salutation is not null
		THEN fullName := fullName || salutation;
	END IF;

	IF patient_name is not null
		THEN fullName := fullName ||' '|| patient_name;
	END IF;

	IF middle_name is not null
		THEN fullName := fullName ||' ' || middle_name;
	END IF;

	IF last_name is not null
		THEN fullName := fullName ||' '|| last_name;
	END IF;

	RETURN fullName;
END;
$BODY$ LANGUAGE 'plpgsql';


DROP FUNCTION IF EXISTS bill_charge_totals_trigger() CASCADE;
CREATE OR REPLACE FUNCTION bill_charge_totals_trigger() RETURNS TRIGGER AS $BODY$
DECLARE
	changed boolean;
	oldBill text;
BEGIN
	changed := false; oldBill := null;

	IF (TG_OP = 'INSERT') THEN
		changed := true;

	ELSIF (TG_OP = 'UPDATE') THEN
		-- cancellation of charge should trigger update of all amounts.
		IF (NEW.status != OLD.status) THEN
			changed := true;
		-- for bill type conversion and OP-IP conversion, we may move charges from one bill to another
		-- this should also trigger update of all amounts, and also update two bills
		ELSIF (NEW.bill_no != OLD.bill_no) THEN
			changed := true;
			oldBill := OLD.bill_no;
		ELSE
			IF (NEW.amount != OLD.amount) THEN changed := true; END IF;
			IF (NEW.discount != OLD.discount) THEN changed := true; END IF;
			IF (COALESCE(NEW.insurance_claim_amount,0) != COALESCE(OLD.insurance_claim_amount,0))
				THEN changed := true; END IF;
			IF (COALESCE(NEW.claim_recd_total,0) != COALESCE(OLD.claim_recd_total,0))
				THEN changed := true; END IF;
			IF (COALESCE(NEW.return_amt,0) != COALESCE(OLD.return_amt,0))
				THEN changed := true; END IF;
			IF (COALESCE(NEW.return_insurance_claim_amt,0) != COALESCE(OLD.return_insurance_claim_amt,0))
				THEN changed := true; END IF;
			IF (COALESCE(NEW.tax_amt,0) != COALESCE(OLD.tax_amt,0))
				THEN changed := true; END IF;
			IF (COALESCE(NEW.sponsor_tax_amt,0) != COALESCE(OLD.sponsor_tax_amt,0))
				THEN changed := true; END IF;
		END IF;
	END IF;

	IF changed THEN
		UPDATE bill b SET
		total_amount =
			coalesce((SELECT sum(amount) FROM bill_charge WHERE bill_no = b.bill_no AND status != 'X'),0),
		total_discount =
			coalesce((SELECT sum(discount) FROM bill_charge WHERE bill_no = b.bill_no AND status != 'X'),0),
		total_claim = (
			CASE WHEN is_tpa THEN
				coalesce((SELECT sum(insurance_claim_amount) FROM bill_charge WHERE bill_no = b.bill_no
						AND status != 'X'),0)
			ELSE 0 END),
		total_claim_return = (
			CASE WHEN is_tpa THEN
				coalesce((SELECT sum(return_insurance_claim_amt) FROM bill_charge WHERE bill_no = b.bill_no
						AND status != 'X'),0)
			ELSE 0 END),
		claim_recd_amount = (
			CASE WHEN is_tpa THEN
				coalesce((SELECT sum(claim_recd_total) FROM bill_charge WHERE bill_no = b.bill_no
						AND status != 'X'),0)
			ELSE 0 END),
		total_tax = coalesce((SELECT sum(tax_amt) FROM bill_charge WHERE bill_no = b.bill_no AND status != 'X'),0),
		total_claim_tax = coalesce((SELECT sum(sponsor_tax_amt) FROM bill_charge WHERE bill_no = b.bill_no AND status != 'X'),0),
		total_original_tax_amt = coalesce((SELECT sum(original_tax_amt) FROM bill_charge WHERE bill_no = b.bill_no AND status != 'X'),0)
		WHERE bill_no IN (NEW.bill_no, oldBill);
	END IF;

	RETURN NEW;
END;
$BODY$ language plpgsql;

DROP TRIGGER IF EXISTS bill_charge_totals_trigger ON bill_charge CASCADE;
CREATE CONSTRAINT TRIGGER bill_charge_totals_trigger
	AFTER INSERT OR UPDATE
	ON bill_charge
	DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
	EXECUTE PROCEDURE bill_charge_totals_trigger();
	
	
DROP FUNCTION IF EXISTS bill_charge_tax_totals_trigger() CASCADE;
CREATE OR REPLACE FUNCTION bill_charge_tax_totals_trigger() RETURNS TRIGGER AS $BODY$
BEGIN
	IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN
		UPDATE bill_charge bc SET
			tax_amt = coalesce((SELECT sum(tax_amount) FROM bill_charge_tax bct WHERE bct.charge_id = NEW.charge_id),0),
			original_tax_amt = coalesce((SELECT sum(original_tax_amt) FROM bill_charge_tax bct WHERE bct.charge_id = NEW.charge_id),0)
			WHERE charge_id = NEW.charge_id;			
	ELSIF (TG_OP = 'DELETE') THEN
		UPDATE bill_charge bc SET
			tax_amt = coalesce((SELECT sum(tax_amount) FROM bill_charge_tax bct WHERE bct.charge_id = OLD.charge_id),0),
			original_tax_amt = coalesce((SELECT sum(original_tax_amt) FROM bill_charge_tax bct WHERE bct.charge_id = OLD.charge_id),0)
			WHERE charge_id = OLD.charge_id;
	END IF;		
 	RETURN NEW;
END;
$BODY$ language plpgsql;
	
DROP TRIGGER IF EXISTS bill_charge_tax_totals_trigger ON bill_charge_tax CASCADE;
CREATE CONSTRAINT TRIGGER bill_charge_tax_totals_trigger
	AFTER INSERT OR UPDATE OR DELETE
	ON bill_charge_tax
	DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
	EXECUTE PROCEDURE bill_charge_tax_totals_trigger();
	
DROP FUNCTION IF EXISTS bill_charge_claim_tax_totals_trigger() CASCADE;
CREATE OR REPLACE FUNCTION bill_charge_claim_tax_totals_trigger() RETURNS TRIGGER AS $BODY$
BEGIN
	IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN
		UPDATE bill_charge_claim bc SET
		tax_amt = coalesce((SELECT sum(sponsor_tax_amount) FROM bill_charge_claim_tax bcclt WHERE (bcclt.charge_id = NEW.charge_id AND bcclt.claim_id = NEW.claim_id)),0)
		WHERE charge_id = NEW.charge_id AND claim_id = NEW.claim_id;
		
	ELSIF (TG_OP = 'DELETE') THEN
		UPDATE bill_charge_claim bc SET
		tax_amt = coalesce((SELECT sum(sponsor_tax_amount) FROM bill_charge_claim_tax bcclt WHERE (bcclt.charge_id = OLD.charge_id AND bcclt.claim_id = OLD.claim_id)),0)
		WHERE charge_id = OLD.charge_id AND claim_id = OLD.claim_id;
	END IF;
	RETURN NEW;
END;
$BODY$ language plpgsql;
	
DROP TRIGGER IF EXISTS bill_charge_claim_tax_totals_trigger ON bill_charge_claim_tax CASCADE;
CREATE CONSTRAINT TRIGGER bill_charge_claim_tax_totals_trigger
	AFTER INSERT OR UPDATE OR DELETE
	ON bill_charge_claim_tax
	DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
	EXECUTE PROCEDURE bill_charge_claim_tax_totals_trigger();

--
-- Similar trigger to update claim amount WHEN attaching/detaching from tpa
-- Total claim is the only one affected
--
DROP FUNCTION IF EXISTS bill_claim_totals_trigger() CASCADE;
CREATE OR REPLACE FUNCTION bill_claim_totals_trigger() RETURNS TRIGGER AS $BODY$
BEGIN
	IF (NEW.is_tpa != OLD.is_tpa) THEN
		NEW.total_claim =
			CASE WHEN NEW.is_tpa THEN
				coalesce((SELECT sum(insurance_claim_amount) FROM bill_charge WHERE bill_no = NEW.bill_no
					AND status != 'X'),0)
			ELSE 0 END;
	END IF;

	RETURN NEW;
END;
$BODY$ language plpgsql;

DROP TRIGGER IF EXISTS bill_claim_totals_trigger ON bill CASCADE;
CREATE TRIGGER bill_claim_totals_trigger
	BEFORE UPDATE ON bill
	FOR EACH ROW
	EXECUTE PROCEDURE bill_claim_totals_trigger();

DROP FUNCTION IF EXISTS patient_confidentiality_check(patientGroup integer, mrNo character varying) CASCADE;
CREATE OR REPLACE FUNCTION patient_confidentiality_check(patientGroup integer, mrNo character varying)
   RETURNS BOOLEAN as $$
DECLARE
  result boolean;
BEGIN
    result := false;
	SELECT (patientGroup in (SELECT ufa.confidentiality_grp_id from user_confidentiality_association ufa
	                         JOIN confidentiality_grp_master cgm 
	                         ON (cgm.confidentiality_grp_id = ufa.confidentiality_grp_id)
		              where emp_username = current_setting('application.username')
		              AND ufa.status = 'A' and cgm.status = 'A' UNION SELECT 0) OR
	            mrNo in (SELECT mr_no from user_mrno_association
	                     where emp_username = current_setting('application.username')) into result OR
	        current_setting('application.username') = '_system');
return result;
END;
$$
LANGUAGE 'plpgsql';
	
DROP FUNCTION IF EXISTS getBillAuditNumber(billType text, visitType text, restrictionType text, centerId integer) CASCADE;

DROP FUNCTION IF EXISTS getBillAuditNumber(billType text, visitType text, restrictionType text, centerId integer, isTpa text, isCreditNote text) CASCADE;
CREATE OR REPLACE FUNCTION getBillAuditNumber(billType text, visitType text,
		restrictionType text, centerId integer, isTpa text, isCreditNote text) RETURNS text AS
$BODY$
DECLARE
	rec RECORD;
BEGIN
	SELECT pattern_id AS id INTO rec
		FROM hosp_bill_audit_seq_prefs
	WHERE priority = (
		SELECT min(priority) FROM hosp_bill_audit_seq_prefs
		 WHERE (bill_type = billType or bill_type ='*')
		 	AND (visit_type = visitType or visit_type = '*')
			AND (restriction_type = restrictionType OR restriction_type = '*')
			AND (center_id = centerId OR center_id = 0)
			AND (is_tpa = isTpa OR is_tpa = '*')
			AND (is_credit_note =isCreditNote OR is_credit_note ='*')
	);
	return generate_id(rec.id);
END;
$BODY$ LANGUAGE 'plpgsql';

--
-- Trigger to insert a row into reward_points_earnings table.
-- eligible_value is the sum of amount eligible for earning reward points.
-- E/R: Earned/Reversed. WHEN bill is closed/reopened, eligible amount is totalled.
-- The points are increased if bill is closed and decreased if bill is reopened.
--
-- The patient reward points earned, redeemed are updated.
--

DROP FUNCTION IF EXISTS bill_audit_reward_points() CASCADE;
CREATE OR REPLACE FUNCTION bill_audit_reward_points() RETURNS trigger AS $BODY$
DECLARE
	rec RECORD;
	pnt RECORD;
	rateplan RECORD;
	rewardMrnoRec RECORD;
	billMrnoRec RECORD;
	visitCenter integer;
	lastEntry RECORD;
	changed boolean;
	earning_points integer;
	earning_amount numeric;
	old_paid_amt numeric;
	new_paid_amt numeric;
	isCreditNote text;
	isTpa text;
BEGIN
	changed := false;
	earning_points := 0;
	earning_amount := 0;
	isCreditNote := '*';
	isTpa := '*';

	SELECT center_id INTO visitCenter FROM patient_registration WHERE patient_id = NEW.visit_id;
	
	IF (NEW.visit_type = 'r') THEN
		SELECT center_id INTO visitCenter FROM store_retail_customers WHERE customer_id = NEW.visit_id;
	END IF;
	IF (NEW.visit_type = 't') THEN
		SELECT center_id INTO visitCenter FROM incoming_sample_registration WHERE incoming_visit_id = NEW.visit_id;
	END IF;
	
	SELECT
  		CASE 
  			WHEN total_amount < 0 THEN 't' 
  			ELSE 'f'
  		END AS is_credit_note INTO isCreditNote
	FROM bill WHERE bill_no = NEW.bill_no;
	
	IF (NEW.is_tpa) THEN
		isTpa := 't';
	ELSE
		isTpa := 'f';
	END IF;
	
	IF (TG_OP = 'UPDATE'  AND  (NEW.status = 'F' OR NEW.status = 'C') AND NEW.audit_control_number IS NULL) THEN
		UPDATE bill SET audit_control_number = getBillAuditNumber(NEW.bill_type, NEW.visit_type,
		NEW.restriction_type, visitCenter, isTpa, isCreditNote) WHERE bill_no = NEW.bill_no  AND  audit_control_number IS NULL;
	END IF;

	if (NEW.is_tpa != OLD.is_tpa AND NEW.is_tpa) THEN
		UPDATE bill SET points_redeemed = 0, points_redeemed_amt = 0 WHERE bill_no = NEW.bill_no;
	END IF;

	SELECT mr_no INTO billMrnoRec FROM patient_registration WHERE patient_id = NEW.visit_id;

	IF (TG_OP = 'UPDATE' AND OLD.status = NEW.status AND NEW.status = 'A') THEN
		UPDATE reward_points_status rps SET open_points_redeemed =
			COALESCE((SELECT sum(COALESCE(points_redeemed, 0)) FROM bill b
			JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
			WHERE rps.mr_no = pr.mr_no AND b.status = 'A'), 0)
		WHERE rps.mr_no = billMrnoRec.mr_no;
	END IF;

	IF (NEW.is_tpa) THEN
		RETURN NEW;
	END IF;

	old_paid_amt := (coalesce(OLD.total_receipts,0)	+ coalesce(OLD.deposit_set_off,0)
							+ coalesce(OLD.points_redeemed_amt,0));

	new_paid_amt := (coalesce(NEW.total_receipts,0)	+ coalesce(NEW.deposit_set_off,0)
							+ coalesce(NEW.points_redeemed_amt,0));

	IF (TG_OP = 'UPDATE') THEN
		IF ( (OLD.status != NEW.status
			AND (NEW.status = 'A' OR NEW.status = 'F' OR NEW.status = 'C'))
			OR (NEW.status = 'C' AND old_paid_amt != new_paid_amt) ) THEN
			changed := true;
		END IF;
	END IF;

	IF (changed) THEN

		EXECUTE ' SELECT CASE WHEN points_earning_amt
					IS NOT NULL AND points_earning_amt > 0
				  	THEN points_earning_points ELSE 1 END
				  FROM generic_preferences ' INTO earning_points;

		EXECUTE ' SELECT COALESCE(points_earning_amt,0)
				  FROM generic_preferences ' INTO earning_amount;

		IF (earning_amount != 0) THEN

			SELECT coalesce((SELECT sum(amount) FROM bill_charge bc
				JOIN service_sub_groups ssg ON (ssg.service_sub_group_id = bc.service_sub_group_id)
				WHERE bc.bill_no = NEW.bill_no AND bc.status != 'X'
				AND ssg.eligible_to_earn_points = 'Y'), 0) AS eligible_amount
			INTO rec ;

			SELECT coalesce(eligible_to_earn_points, 'N') AS eligible_to_earn_points FROM organization_details
				WHERE org_id = NEW.bill_rate_plan_id
			INTO rateplan;

			IF (rec.eligible_amount > 0 AND rateplan.eligible_to_earn_points = 'Y') THEN

				SELECT coalesce((SELECT sum( floor(amount/earning_amount) * earning_points )
					FROM bill_charge bc
					JOIN service_sub_groups ssg ON (ssg.service_sub_group_id = bc.service_sub_group_id)
					WHERE bc.bill_no = NEW.bill_no AND bc.status != 'X'
					AND ssg.eligible_to_earn_points = 'Y'), 0) AS eligible_points
				INTO pnt ;

				IF (billMrnoRec.mr_no IS NOT NULL AND pnt.eligible_points != 0) THEN

					IF (NEW.status = 'A') THEN

						IF (NEW.total_amount = new_paid_amt) THEN

							INSERT INTO reward_points_earnings(entry_id, entry_type,
									mr_no, bill_no, eligible_value, points)
							VALUES(nextval('reward_points_earnings_seq'), 'R'::text,
									billMrnoRec.mr_no::text, NEW.bill_no,
									-rec.eligible_amount::numeric, -floor(pnt.eligible_points));

						END IF;
					ELSIF (NEW.status = 'C') THEN

						IF (NEW.total_amount = new_paid_amt) THEN

							SELECT mr_no INTO rewardMrnoRec FROM reward_points_status
								WHERE mr_no = billMrnoRec.mr_no;

							IF (rewardMrnoRec IS NULL OR rewardMrnoRec.mr_no IS NULL) THEN
								INSERT INTO reward_points_status(mr_no) VALUES(billMrnoRec.mr_no);
							END IF;

							SELECT entry_type INTO lastEntry FROM reward_points_earnings
								WHERE bill_no = NEW.bill_no ORDER BY date DESC LIMIT 1;

							IF (lastEntry IS NOT NULL AND lastEntry.entry_type
								IS NOT NULL AND lastEntry.entry_type = 'E') THEN
							ELSE

								INSERT INTO reward_points_earnings(entry_id, entry_type,
										mr_no, bill_no, eligible_value, points)
								VALUES(nextval('reward_points_earnings_seq'), 'E'::text,
										billMrnoRec.mr_no::text, NEW.bill_no,
										rec.eligible_amount::numeric, floor(pnt.eligible_points));
							END IF;
						END IF;
					END IF;
				END IF;
			END IF;
		END IF;

		UPDATE bill SET points_earned = COALESCE((SELECT sum(points) FROM reward_points_earnings
			WHERE bill_no = NEW.bill_no),0) WHERE bill_no = NEW.bill_no;

		UPDATE reward_points_status rps SET points_earned = (SELECT sum(points)
			FROM reward_points_earnings rpe	WHERE rps.mr_no = rpe.mr_no)
		WHERE rps.mr_no = billMrnoRec.mr_no;

		UPDATE reward_points_status rps SET points_redeemed =
			COALESCE((SELECT sum(COALESCE(points_redeemed, 0)) FROM bill b
			JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
			WHERE rps.mr_no = pr.mr_no AND (b.status = 'F' OR b.status = 'C')), 0)
		WHERE rps.mr_no = billMrnoRec.mr_no;

		UPDATE reward_points_status rps SET open_points_redeemed =
			COALESCE((SELECT sum(COALESCE(points_redeemed, 0)) FROM bill b
			JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
			WHERE rps.mr_no = pr.mr_no AND b.status = 'A'), 0)
		WHERE rps.mr_no = billMrnoRec.mr_no;

	END IF;

	RETURN NEW;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS bill_audit_reward_points ON bill CASCADE;
CREATE TRIGGER bill_audit_reward_points
  AFTER UPDATE
  ON bill
  FOR EACH ROW
  EXECUTE PROCEDURE bill_audit_reward_points();


-- Triggers to INSERT, UPDATE AND DELETE PBM prescriptions.
-- Modified to fix bug 42966
DROP FUNCTION IF EXISTS copy_pbm_patient_prescriptions() CASCADE;
CREATE OR REPLACE FUNCTION copy_pbm_patient_prescriptions() RETURNS trigger AS $BODY$
DECLARE
	patientPrescRec RECORD;
	opPrescRec RECORD;
	pbmPrescRec RECORD;
	pbmPrescId integer;
	consPatientId character varying;
	patientRec RECORD;
	erx_enabled_for_center character(1);
	pbm_enabled_for_center character(1);
	centerHealthAuth character varying;
BEGIN
	erx_enabled_for_center := 'N';
	pbm_enabled_for_center := 'N';

    SELECT pbm_presc_id INTO pbmPrescId FROM pbm_prescription
	WHERE pbm_presc_id = NEW.pbm_presc_id
	AND status = 'O' AND (pbm_request_id IS NULL OR pbm_request_id = '');

    IF (pbmPrescId IS NOT NULL AND pbmPrescId != 0) THEN

	SELECT dc.patient_id, pr.center_id, health_authority into patientRec from doctor_consultation dc
	join patient_registration pr ON dc.patient_id = pr.patient_id
	join hospital_center_master hcm ON pr.center_id = hcm.center_id
	where consultation_id = NEW.erx_consultation_id;

	consPatientId := patientRec.patient_id;
	centerHealthAuth := patientRec.health_authority;

	IF (centerHealthAuth IS NOT NULL) THEN
    		IF (lower(centerHealthAuth) = 'haad') THEN
    			pbm_enabled_for_center := 'Y';
    		ELSIF (lower(centerHealthAuth) = 'dha') THEN
    			erx_enabled_for_center := 'Y' ;
    		END IF;
    	END IF;

    FOR opPrescRec IN
	SELECT pmp.*, pbm.*, pp.special_instr FROM patient_prescription pp
	JOIN patient_medicine_prescriptions pmp ON (pp.patient_presc_id=pmp.op_medicine_pres_id)
 	JOIN pbm_prescription pbm USING(pbm_presc_id)
	WHERE pbm_presc_id = pbmPrescId
	AND drug_count = (SELECT count(*)::integer
		FROM patient_medicine_prescriptions WHERE pbm_presc_id = pbmPrescId)
    LOOP
    	SELECT consultation_id, prescribed_date INTO patientPrescRec
    	FROM patient_prescription
    	WHERE presc_type='Medicine' AND store_item=true AND patient_presc_id=opPrescRec.op_medicine_pres_id;

		SELECT op_medicine_pres_id, mod_time, updated_in_pbm INTO pbmPrescRec
		FROM pbm_medicine_prescriptions
		WHERE op_medicine_pres_id = opPrescRec.op_medicine_pres_id;

		IF (pbmPrescRec IS NOT NULL) THEN
			IF (pbmPrescRec.updated_in_pbm = 'N') THEN
			UPDATE pbm_medicine_prescriptions
			   SET op_medicine_pres_id=opPrescRec.op_medicine_pres_id,
				consultation_id=patientPrescRec.consultation_id,
			       frequency=opPrescRec.frequency,
			       medicine_quantity=opPrescRec.medicine_quantity,
			       medicine_remarks=opPrescRec.medicine_remarks,
			       prescribed_date=patientPrescRec.prescribed_date,
			       mod_time=opPrescRec.mod_time,
			       activity_due_date=opPrescRec.activity_due_date,
			       medicine_id=opPrescRec.medicine_id,
			       route_of_admin=opPrescRec.route_of_admin,
			       strength=opPrescRec.strength,
			       generic_code=opPrescRec.generic_code,
			       item_form_id=opPrescRec.item_form_id,
			       item_strength=opPrescRec.item_strength,
			       visit_id=consPatientId,
			       pbm_presc_id=opPrescRec.pbm_presc_id,
			       duration_units=opPrescRec.duration_units,
			       duration=opPrescRec.duration,
			       item_strength_units=opPrescRec.item_strength_units,
			       erx_status=opPrescRec.erx_status,
			       erx_denial_code=opPrescRec.erx_denial_code,
			       erx_denial_remarks=opPrescRec.erx_denial_remarks,
			       erx_approved_quantity=opPrescRec.erx_approved_quantity,
			       consumption_uom=opPrescRec.consumption_uom,
			       send_for_erx=opPrescRec.send_for_erx,
			       special_instr=opPrescRec.special_instr
			  WHERE op_medicine_pres_id = opPrescRec.op_medicine_pres_id;
			END IF;
		ELSE
			IF (pbm_enabled_for_center = 'Y' or (erx_enabled_for_center = 'Y' AND opPrescRec.send_for_erx = 'Y')) THEN
				INSERT INTO pbm_medicine_prescriptions(
					op_medicine_pres_id, consultation_id, frequency,
					medicine_quantity, medicine_remarks, prescribed_date,
					mod_time, issued_qty, medicine_id,
					route_of_admin, strength, generic_code, item_form_id,
					item_strength, visit_id, pbm_presc_id, duration_units,
					duration, item_strength_units,
					erx_status, erx_denial_code,
					erx_denial_remarks, erx_approved_quantity,
					consumption_uom, send_for_erx, updated_in_pbm, special_instr)
				VALUES (
					opPrescRec.op_medicine_pres_id, patientPrescRec.consultation_id,
					opPrescRec.frequency, opPrescRec.medicine_quantity,
					opPrescRec.medicine_remarks,patientPrescRec.prescribed_date,
					opPrescRec.mod_time, opPrescRec.issued_qty, opPrescRec.medicine_id,
					opPrescRec.route_of_admin, opPrescRec.strength,
					opPrescRec.generic_code, opPrescRec.item_form_id,
					opPrescRec.item_strength, consPatientId,
					opPrescRec.pbm_presc_id, opPrescRec.duration_units,
					opPrescRec.duration, opPrescRec.item_strength_units,
					opPrescRec.erx_status, opPrescRec.erx_denial_code,
					opPrescRec.erx_denial_remarks, opPrescRec.erx_approved_quantity,
					opPrescRec.consumption_uom, opPrescRec.send_for_erx, 'N', opPrescRec.special_instr);
				END IF;

		END IF;
	END LOOP;
   END IF;
RETURN NEW;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS copy_pbm_patient_prescriptions ON pbm_prescription CASCADE;

DROP FUNCTION IF EXISTS delete_pbm_patient_prescriptions() CASCADE;
CREATE OR REPLACE FUNCTION delete_pbm_patient_prescriptions() RETURNS trigger AS $BODY$
BEGIN
	DELETE FROM pbm_medicine_prescriptions  WHERE
	op_medicine_pres_id = OLD.op_medicine_pres_id AND mod_time = OLD.mod_time;
RETURN OLD;
END;
$BODY$ LANGUAGE 'plpgsql';


DROP TRIGGER IF EXISTS delete_pbm_patient_prescriptions ON patient_medicine_prescriptions CASCADE;
CREATE TRIGGER delete_pbm_patient_prescriptions
  BEFORE DELETE
  ON patient_medicine_prescriptions
  FOR EACH ROW
  EXECUTE PROCEDURE delete_pbm_patient_prescriptions();


-- Triggers to INSERT AND UPDATE PBM Observations.
-- If pbm_observations_master is empty THEN there will be no entry in pbm_presc_observations.
-- This means WHEN mod_eclaim_pbm module is enabled and there are entries in pbm_observations_master
-- the observations are saved for a insured or non-insured patient prescription.
DROP FUNCTION IF EXISTS update_pbm_presc_observation() CASCADE;
CREATE OR REPLACE function update_pbm_presc_observation() RETURNS TRIGGER AS $BODY$
DECLARE
	pbmMasterObs RECORD;
	valueColName text;
	unitsColName text;
	colId integer;
	newValue text;
	newUnits text;
	obsId integer;
BEGIN
FOR pbmMasterObs IN
	SELECT id, patient_med_presc_value_column,
	patient_med_presc_units_column, observation_type, code
	FROM pbm_observations_master WHERE status = 'A' AND required = 'Y'
    LOOP
        valueColName := pbmMasterObs.patient_med_presc_value_column;
        unitsColName := pbmMasterObs.patient_med_presc_units_column;
        colId := pbmMasterObs.id;
        newUnits := '';
        -- use related master table for observation value if exists
        IF valueColName = 'item_form_id' THEN
	        EXECUTE 'SELECT item_form_name FROM item_form_master WHERE item_form_id = ('
	         || quote_literal(NEW) || '::' || TG_RELID::regclass || ').'
	         || quote_ident(valueColName) INTO newValue;

	    ELSEIF valueColName = 'route_of_admin' THEN
	        EXECUTE 'SELECT route_name FROM medicine_route WHERE route_id = ('
	         || quote_literal(NEW) || '::' || TG_RELID::regclass || ').'
	         || quote_ident(valueColName) INTO newValue;

	    ELSEIF valueColName = '' THEN

	    ELSE
	    	EXECUTE 'SELECT (' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').'
	         || quote_ident(valueColName) INTO newValue;
	    END IF;

		-- use related master table for observation value type/units if exists
        IF unitsColName IS NOT NULL AND unitsColName != '' THEN
        	IF unitsColName = 'item_strength_units' THEN
		        EXECUTE 'SELECT unit_name FROM strength_units WHERE unit_id = ('
		         || quote_literal(NEW) || '::' || TG_RELID::regclass || ').'
		         || quote_ident(unitsColName) INTO newUnits;

	        ELSEIF unitsColName = 'duration_units' THEN
				EXECUTE 'SELECT unit_name FROM duration_units WHERE unit = ('
		         || quote_literal(NEW) || '::' || TG_RELID::regclass || ').'
		         || quote_ident(unitsColName) INTO newUnits;

		    ELSEIF unitsColName = '' THEN

	        ELSE
	        	EXECUTE 'SELECT (' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').'
		         || quote_ident(unitsColName) INTO newUnits;
		    END IF;
        END IF;
        IF newValue IS NOT NULL THEN
		-- check if a row for the observation already exists
		SELECT observation_id FROM pbm_presc_observations ppo
		WHERE pbm_medicine_pres_id = NEW.pbm_medicine_pres_id
		AND obs_id = colId INTO obsId;

		IF obsId IS NULL THEN
			-- insert new observation (observation_id is auto generated by a sequence)
		INSERT INTO pbm_presc_observations (pbm_medicine_pres_id, obs_id, value, value_type) VALUES(NEW.pbm_medicine_pres_id, colId, newValue, newUnits);

		ELSE
			-- update the existing observation, maybe the value has changed.
		UPDATE pbm_presc_observations SET value=newValue, value_type=newUnits
			WHERE observation_id = obsId;
		END IF;
	END IF;
  END LOOP;
RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS update_pbm_presc_observation ON pbm_medicine_prescriptions CASCADE;
CREATE TRIGGER update_pbm_presc_observation
  AFTER INSERT OR UPDATE
  ON pbm_medicine_prescriptions
  FOR EACH ROW
  EXECUTE PROCEDURE update_pbm_presc_observation();


DROP FUNCTION IF EXISTS insert_pbm_presc_observation() CASCADE;

DROP TRIGGER IF EXISTS insert_pbm_presc_observation ON pbm_medicine_prescriptions CASCADE;

CREATE OR REPLACE FUNCTION is_outhouse_test(testid character varying,centerid integer)
	RETURNS boolean AS $BODY$
DECLARE
    isOuthouse boolean;
BEGIN
	IF testid is not null
		THEN
		  IF centerid != 0
		    THEN
				IF EXISTS (SELECT 1
					from diag_outsource_detail dod
					where test_id = testid and source_center_id = centerid and dod.status='A') THEN

					isOuthouse = true;
				ELSE isOuthouse = false;
				END IF;
		    ELSE
				IF EXISTS (SELECT 1 from diag_outsource_detail
					where test_id = testid and status = 'A') THEN

					isOuthouse = true;
				ELSE isOuthouse = false;
				END IF;
		  END IF;
	ELSE
		isOuthouse = false;
	END IF;
RETURN isOuthouse;
END;
$BODY$ LANGUAGE 'plpgsql';

--- Trigger to insert Bill Adjustments ON bill update---

DROP FUNCTION IF EXISTS bill_adjustment_trigger_on_bill_update() CASCADE;
CREATE OR REPLACE FUNCTION bill_adjustment_trigger_on_bill_update() RETURNS trigger AS $BODY$
DECLARE
	seq_no varchar;
BEGIN

	IF (NEW.status != OLD.status OR NEW.finalized_date != OLD.finalized_date OR NEW.closed_date != OLD.closed_date
		OR NEW.username != OLD.username OR  NEW.claim_recd_amount != OLD.claim_recd_amount
		OR NEW.deposit_set_off != OLD.deposit_set_off
		OR NEW.total_amount != OLD.total_amount OR NEW.total_discount != OLD.total_discount
		OR NEW.total_claim != OLD.total_claim OR NEW.total_receipts != OLD.total_receipts
		OR NEW.primary_total_sponsor_receipts != OLD.primary_total_sponsor_receipts
		OR NEW.dyna_package_charge != OLD.dyna_package_charge OR NEW.total_claim_return != OLD.total_claim_return
		OR NEW.bill_rate_plan_id != OLD.bill_rate_plan_id OR NEW.claim_recd_unalloc_amount != OLD.claim_recd_unalloc_amount
		OR NEW.secondary_total_sponsor_receipts != OLD.secondary_total_sponsor_receipts
		OR NEW.points_redeemed_amt != OLD.points_redeemed_amt) THEN
	SELECT generate_id('BILL_ADJ_DEFAULT') INTO seq_no;

	INSERT INTO bill_adjustment (bill_adjustment_id,bill_no, open_date, status, finalized_date, closed_date,
			username, claim_recd_amount, deposit_set_off, total_amount, total_discount,
			total_claim, total_receipts, primary_total_sponsor_receipts, dyna_package_charge,
			total_claim_return,bill_rate_plan_id,claim_recd_unalloc_amount,secondary_total_sponsor_receipts,
			points_redeemed_amt,mod_time)
	VALUES (seq_no, NEW.bill_no, NEW.open_date, NEW.status, NEW.finalized_date, NEW.closed_date,NEW.username,
			NEW.claim_recd_amount-OLD.claim_recd_amount, NEW.deposit_set_off-OLD.deposit_set_off,
			NEW.total_amount-OLD.total_amount, NEW.total_discount-OLD.total_discount,
			NEW.total_claim-OLD.total_claim, NEW.total_receipts-OLD.total_receipts, NEW.primary_total_sponsor_receipts-OLD.primary_total_sponsor_receipts,
			NEW.dyna_package_charge-OLD.dyna_package_charge,
			NEW.total_claim_return-OLD.total_claim_return,NEW.bill_rate_plan_id,NEW.claim_recd_unalloc_amount-OLD.claim_recd_unalloc_amount,
			NEW.secondary_total_sponsor_receipts-OLD.secondary_total_sponsor_receipts,NEW.points_redeemed_amt-OLD.points_redeemed_amt,
			current_timestamp);
	END IF;

	RETURN NULL;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS bill_adjustment_trigger_on_bill_update ON bill CASCADE;
CREATE TRIGGER bill_adjustment_trigger_on_bill_update
	AFTER UPDATE ON bill
	FOR EACH ROW EXECUTE PROCEDURE bill_adjustment_trigger_on_bill_update();


--- Trigger to insert Bill Charge Adjustments ON bill charge update---
DROP FUNCTION IF EXISTS bill_charge_adjustment_trigger_on_bill_charge_update() CASCADE;
CREATE OR REPLACE FUNCTION bill_charge_adjustment_trigger_on_bill_charge_update() RETURNS trigger AS $BODY$
DECLARE
	seq_no varchar;
	oldPatientCategory integer;
	oldPriSpnr varchar;
	oldSecSpnr varchar;
	patCategoryId int;
	priSponsorId varchar;
	secSponsorId varchar;
BEGIN
	patCategoryId := null; oldPatientCategory := null;
	priSponsorId := null; oldPriSpnr := null;
	secSponsorId := null; oldSecSpnr := null;

	SELECT pr.patient_category_id INTO oldPatientCategory
		FROM bill b
		JOIN patient_registration pr ON(pr.patient_id = b.visit_id)
	WHERE b.bill_no=OLD.bill_no;

	SELECT bcl.sponsor_id INTO oldPriSpnr
		FROM bill b
		JOIN bill_claim bcl ON(b.bill_no = bcl.bill_no)
	WHERE bcl.priority = 1 AND b.bill_no = OLD.bill_no ;

	SELECT bcl.sponsor_id INTO oldSecSpnr
		FROM bill b
		JOIN bill_claim bcl ON(b.bill_no = bcl.bill_no)
	WHERE bcl.priority = 2 AND b.bill_no = OLD.bill_no ;

	SELECT pr.patient_category_id INTO patCategoryId
		FROM bill b
		JOIN patient_registration pr ON(pr.patient_id = b.visit_id)
	WHERE b.bill_no=NEW.bill_no;

	SELECT bcl.sponsor_id INTO priSponsorId
		FROM bill b
		JOIN bill_claim bcl ON(b.bill_no = bcl.bill_no)
	WHERE bcl.priority = 1 AND b.bill_no = NEW.bill_no ;

	SELECT bcl.sponsor_id INTO secSponsorId
		FROM bill b
		JOIN bill_claim bcl ON(b.bill_no = bcl.bill_no)
	WHERE bcl.priority = 2 AND b.bill_no = NEW.bill_no ;

	IF (NEW.status != OLD.status AND NEW.status = 'X' AND (NEW.charge_group = 'BED' OR
		NEW.charge_group = 'ICU' OR NEW.charge_head = 'LTAX')) THEN
		SELECT generate_id('BILL_CHARGE_ADJ_DEFAULT') INTO seq_no;
		INSERT INTO bill_charge_adjustment (bill_charge_adjustment_id, charge_id, bill_no,act_rate,act_quantity,amount,
				discount,paid_amount,posted_date,status,username,mod_time,orig_rate,package_unit,doctor_amount,
				insurance_claim_amount,referal_amount,out_house_amount,prescribing_dr_amount,overall_discount_amt,
				dr_discount_amt, pres_dr_discount_amt,ref_discount_amt,hosp_discount_amt,claim_recd_total,
				return_insurance_claim_amt,return_amt,return_qty,redeemed_points,amount_included,qty_included,
				orig_insurance_claim_amount,charge_head,charge_group,patient_category_id,primary_sponsor_id,
				secondary_sponsor_id,service_sub_group_id,tax_amt,sponsor_tax_amt)
		VALUES (seq_no, NEW.charge_id, NEW.bill_no, 0-OLD.act_rate,0-OLD.act_quantity,0-OLD.amount,
				0-OLD.discount,0-OLD.paid_amount,NEW.posted_date,NEW.status,NEW.username,current_timestamp,
				0-OLD.orig_rate,NEW.package_unit,0-OLD.doctor_amount,0-OLD.insurance_claim_amount,
				0-OLD.referal_amount,0-coalesce(OLD.out_house_amount,0.00),
				0-OLD.prescribing_dr_amount,0-OLD.overall_discount_amt,
				0-OLD.dr_discount_amt, 0-OLD.pres_dr_discount_amt,
				0-OLD.ref_discount_amt,0-OLD.hosp_discount_amt,0-OLD.claim_recd_total,
				0-OLD.return_insurance_claim_amt,
				0-OLD.return_amt,0-OLD.return_qty,NEW.redeemed_points,0-oLD.amount_included,
				0-OLD.qty_included,0-OLD.orig_insurance_claim_amount,NEW.charge_head,NEW.charge_group,
				patCategoryId,priSponsorId,secSponsorId,NEW.service_sub_group_id,0-OLD.tax_amt,0-OLD.sponsor_tax_amt);

	ELSEIF (NEW.act_rate != OLD.act_rate OR NEW.act_quantity != OLD.act_quantity OR NEW.amount != OLD.amount
			OR NEW.discount != OLD.discount OR NEW.paid_amount != OLD.paid_amount OR NEW.posted_date != OLD.posted_date
			OR NEW.status != OLD.status OR NEW.username != OLD.username OR NEW.orig_rate != OLD.orig_rate
			OR NEW.package_unit != OLD.package_unit OR NEW.doctor_amount != OLD.doctor_amount OR NEW.insurance_claim_amount != OLD.insurance_claim_amount
			OR NEW.referal_amount != OLD.referal_amount OR coalesce(NEW.out_house_amount,0.00) != coalesce(OLD.out_house_amount,0.00)
			OR NEW.prescribing_dr_amount != OLD.prescribing_dr_amount
			OR NEW.overall_discount_amt != OLD.overall_discount_amt OR NEW.dr_discount_amt != OLD.dr_discount_amt OR NEW.pres_dr_discount_amt != OLD.pres_dr_discount_amt
			OR NEW.ref_discount_amt != OLD.ref_discount_amt OR NEW.hosp_discount_amt != OLD.hosp_discount_amt OR NEW.claim_recd_total != OLD.claim_recd_total
			OR NEW.return_insurance_claim_amt != OLD.return_insurance_claim_amt OR NEW.return_amt != OLD.return_amt OR NEW.return_qty != OLD.return_qty
			OR NEW.redeemed_points != OLD.redeemed_points OR NEW.amount_included != OLD.amount_included OR NEW.qty_included != OLD.qty_included
			OR NEW.orig_insurance_claim_amount != OLD.orig_insurance_claim_amount
			OR NEW.charge_head != OLD.charge_head OR NEW.charge_group != OLD.charge_group OR NEW.service_sub_group_id != OLD.service_sub_group_id
			OR NEW.tax_amt != OLD.tax_amt OR NEW.sponsor_tax_amt != OLD.sponsor_tax_amt) THEN
		SELECT generate_id('BILL_CHARGE_ADJ_DEFAULT') INTO seq_no;
		INSERT INTO bill_charge_adjustment (bill_charge_adjustment_id, charge_id, bill_no,act_rate,act_quantity,amount,
				discount,paid_amount,posted_date,status,username,mod_time,orig_rate,package_unit,doctor_amount,
				insurance_claim_amount,referal_amount,out_house_amount,prescribing_dr_amount,overall_discount_amt,
				dr_discount_amt, pres_dr_discount_amt,ref_discount_amt,hosp_discount_amt,claim_recd_total,
				return_insurance_claim_amt,return_amt,return_qty,redeemed_points,amount_included,qty_included,
				orig_insurance_claim_amount,charge_head,charge_group,patient_category_id,primary_sponsor_id,
				secondary_sponsor_id,service_sub_group_id,tax_amt,sponsor_tax_amt)
		VALUES (seq_no, NEW.charge_id, NEW.bill_no,NEW.act_rate-OLD.act_rate,NEW.act_quantity-OLD.act_quantity,NEW.amount-OLD.amount,
				NEW.discount-OLD.discount,NEW.paid_amount-OLD.paid_amount,NEW.posted_date,NEW.status,NEW.username,current_timestamp,
				NEW.orig_rate-OLD.orig_rate,NEW.package_unit,NEW.doctor_amount-OLD.doctor_amount,NEW.insurance_claim_amount-OLD.insurance_claim_amount,
				NEW.referal_amount-OLD.referal_amount,coalesce(NEW.out_house_amount,0.00)-coalesce(OLD.out_house_amount,0.00),
				NEW.prescribing_dr_amount-OLD.prescribing_dr_amount,NEW.overall_discount_amt-OLD.overall_discount_amt,
				NEW.dr_discount_amt-OLD.dr_discount_amt, NEW.pres_dr_discount_amt-OLD.pres_dr_discount_amt,
				NEW.ref_discount_amt-OLD.ref_discount_amt,NEW.hosp_discount_amt-OLD.hosp_discount_amt,NEW.claim_recd_total-OLD.claim_recd_total,
				NEW.return_insurance_claim_amt-OLD.return_insurance_claim_amt,
				NEW.return_amt-OLD.return_amt,NEW.return_qty-OLD.return_qty,NEW.redeemed_points,NEW.amount_included-oLD.amount_included,
				NEW.qty_included-OLD.qty_included,NEW.orig_insurance_claim_amount-OLD.orig_insurance_claim_amount,
				NEW.charge_head, NEW.charge_group,patCategoryId,priSponsorId,secSponsorId,NEW.service_sub_group_id, 
				NEW.tax_amt-OLD.tax_amt,NEW.sponsor_tax_amt-OLD.sponsor_tax_amt);

	ELSEIF  (NEW.bill_no != OLD.bill_no) THEN
		INSERT INTO bill_charge_adjustment (bill_charge_adjustment_id, charge_id, bill_no,act_rate,act_quantity,amount,
				discount,paid_amount,posted_date,status,username,mod_time,orig_rate,package_unit,doctor_amount,
				insurance_claim_amount,referal_amount,out_house_amount,prescribing_dr_amount,overall_discount_amt,
				dr_discount_amt, pres_dr_discount_amt,ref_discount_amt,hosp_discount_amt,claim_recd_total,
				return_insurance_claim_amt,return_amt,return_qty,redeemed_points,amount_included,qty_included,
				orig_insurance_claim_amount,charge_head,charge_group,patient_category_id,primary_sponsor_id,
				secondary_sponsor_id,service_sub_group_id,tax_amt,sponsor_tax_amt)
		VALUES (generate_id('BILL_CHARGE_ADJ_DEFAULT'), OLD.charge_id, OLD.bill_no, 0-OLD.act_rate, 0-OLD.act_quantity,
				0-OLD.amount, 0-OLD.discount, 0-OLD.paid_amount, OLD.posted_date, OLD.status, OLD.username, current_timestamp,
				0-OLD.orig_rate, OLD.package_unit, 0-OLD.doctor_amount, 0-OLD.insurance_claim_amount, 0-OLD.referal_amount,
				0-OLD.out_house_amount, 0-OLD.prescribing_dr_amount, 0-OLD.overall_discount_amt,
				0-OLD.dr_discount_amt, 0-OLD.pres_dr_discount_amt, 0-OLD.ref_discount_amt, 0-OLD.hosp_discount_amt,
				0-OLD.claim_recd_total, 0-OLD.return_insurance_claim_amt, 0-OLD.return_amt, 0-OLD.return_qty, OLD.redeemed_points,
				0-OLD.amount_included, 0-OLD.qty_included, 0-OLD.orig_insurance_claim_amount, OLD.charge_head, OLD.charge_group,
				oldPatientCategory,oldPriSpnr,oldSecSpnr,OLD.service_sub_group_id, 0-OLD.tax_amt,0-OLD.sponsor_tax_amt);

		INSERT INTO bill_charge_adjustment (bill_charge_adjustment_id, charge_id, bill_no,act_rate,act_quantity,amount,
				discount,paid_amount,posted_date,status,username,mod_time,orig_rate,package_unit,doctor_amount,
				insurance_claim_amount,referal_amount,out_house_amount,prescribing_dr_amount,overall_discount_amt,
				dr_discount_amt, pres_dr_discount_amt,ref_discount_amt,hosp_discount_amt,claim_recd_total,
				return_insurance_claim_amt,return_amt,return_qty,redeemed_points,amount_included,qty_included,
				orig_insurance_claim_amount,charge_head,charge_group,patient_category_id,primary_sponsor_id,
				secondary_sponsor_id,service_sub_group_id,tax_amt,sponsor_tax_amt)
		VALUES (generate_id('BILL_CHARGE_ADJ_DEFAULT'), NEW.charge_id, NEW.bill_no, NEW.act_rate, NEW.act_quantity,
				NEW.amount, NEW.discount, NEW.paid_amount, NEW.posted_date, NEW.status, NEW.username, current_timestamp,
				NEW.orig_rate, NEW.package_unit, NEW.doctor_amount, NEW.insurance_claim_amount, NEW.referal_amount,
				NEW.out_house_amount, NEW.prescribing_dr_amount, NEW.overall_discount_amt,
				NEW.dr_discount_amt, NEW.pres_dr_discount_amt, NEW.ref_discount_amt, NEW.hosp_discount_amt,
				NEW.claim_recd_total, NEW.return_insurance_claim_amt, NEW.return_amt, NEW.return_qty, NEW.redeemed_points,
				NEW.amount_included, NEW.qty_included, NEW.orig_insurance_claim_amount, NEW.charge_head, NEW.charge_group,
				patCategoryId,priSponsorId,secSponsorId,NEW.service_sub_group_id,NEW.tax_amt,NEW.sponsor_tax_amt);
	END IF;
	RETURN NULL;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS bill_charge_adjustment_trigger_on_bill_charge_update ON bill_charge CASCADE;
CREATE CONSTRAINT TRIGGER bill_charge_adjustment_trigger_on_bill_charge_update
	AFTER UPDATE ON bill_charge
	DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
	EXECUTE PROCEDURE bill_charge_adjustment_trigger_on_bill_charge_update();


--- Trigger to insert Bill Claim Adjustment ON bill update ---
DROP FUNCTION IF EXISTS bill_claim_adjustment_trigger_on_bill_update() CASCADE;
CREATE OR REPLACE FUNCTION bill_claim_adjustment_trigger_on_bill_update() RETURNS trigger AS $BODY$
DECLARE
	seq_no varchar;
BEGIN
	IF(NEW.approval_amount != OLD.approval_amount OR NEW.insurance_deduction != OLD.insurance_deduction
		OR NEW.primary_total_claim != OLD.primary_total_claim OR NEW.secondary_total_claim != OLD.secondary_total_claim
		OR NEW.primary_approval_amount != OLD.primary_approval_amount
		OR NEW.secondary_approval_amount != OLD.secondary_approval_amount) THEN

	SELECT generate_id('BILL_CLAIM_ADJ_DEFAULT') INTO seq_no;

	INSERT INTO bill_claim_adjustment (bill_claim_adj_id,bill_no, mod_time, approval_amount, insurance_deduction, primary_total_claim,
			secondary_total_claim, primary_approval_amount, secondary_approval_amount)
	VALUES (seq_no, NEW.bill_no, current_timestamp, NEW.approval_amount - OLD.approval_amount, NEW.insurance_deduction - OLD.insurance_deduction,
		NEW.primary_total_claim - OLD.primary_total_claim, NEW.secondary_total_claim - OLD.secondary_total_claim,
		NEW.primary_approval_amount - OLD.primary_approval_amount, NEW.secondary_approval_amount - OLD.secondary_approval_amount);
	END IF;

	RETURN NULL;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS bill_claim_adjustment_trigger_on_bill_update ON bill CASCADE;
CREATE TRIGGER bill_claim_adjustment_trigger_on_bill_update
	AFTER UPDATE ON bill
	FOR EACH ROW EXECUTE PROCEDURE bill_claim_adjustment_trigger_on_bill_update();


--- Trigger to insert Bill Charge Adjustments ON bill charge insert---
DROP FUNCTION IF EXISTS bill_charge_adjustment_trigger() CASCADE;
CREATE OR REPLACE FUNCTION bill_charge_adjustment_trigger() RETURNS trigger AS $BODY$
DECLARE
	seq_no varchar;
	patCategoryId int;
	priSponsorId varchar;
	secSponsorId varchar;
BEGIN
	patCategoryId := null;
	priSponsorId := null;
	secSponsorId := null;

	SELECT pr.patient_category_id INTO patCategoryId
		FROM bill b
		JOIN patient_registration pr ON(pr.patient_id = b.visit_id)
	WHERE b.bill_no = NEW.bill_no;

	SELECT bcl.sponsor_id INTO priSponsorId
		FROM bill b
		JOIN bill_claim bcl ON(bcl.bill_no = b.bill_no)
		WHERE bcl.priority = 1 AND b.bill_no = NEW.bill_no;

	SELECT bcl.sponsor_id INTO secSponsorId
		FROM bill b
		JOIN bill_claim bcl ON(bcl.bill_no = b.bill_no)
		WHERE bcl.priority = 2 AND b.bill_no = NEW.bill_no;

	SELECT generate_id('BILL_CHARGE_ADJ_DEFAULT') INTO seq_no;
	INSERT INTO bill_charge_adjustment (bill_charge_adjustment_id, charge_id, bill_no,act_rate,act_quantity,amount,
			discount,paid_amount,posted_date,status,username,mod_time,orig_rate,package_unit,doctor_amount,
			insurance_claim_amount,referal_amount,out_house_amount,prescribing_dr_amount,overall_discount_amt,
			dr_discount_amt, pres_dr_discount_amt,ref_discount_amt,hosp_discount_amt,claim_recd_total,
			return_insurance_claim_amt,return_amt,return_qty,redeemed_points,amount_included,qty_included,
			orig_insurance_claim_amount, charge_head, charge_group,patient_category_id,primary_sponsor_id,
			secondary_sponsor_id,service_sub_group_id,tax_amt,sponsor_tax_amt)
	VALUES (seq_no, NEW.charge_id, NEW.bill_no,NEW.act_rate,NEW.act_quantity,NEW.amount,
			NEW.discount,NEW.paid_amount,NEW.posted_date,NEW.status,NEW.username,current_timestamp,NEW.orig_rate,
			NEW.package_unit,NEW.doctor_amount,NEW.insurance_claim_amount,NEW.referal_amount,NEW.out_house_amount,
			NEW.prescribing_dr_amount,NEW.overall_discount_amt,NEW.dr_discount_amt, NEW.pres_dr_discount_amt,
			NEW.ref_discount_amt,NEW.hosp_discount_amt,NEW.claim_recd_total,NEW.return_insurance_claim_amt,
			NEW.return_amt,NEW.return_qty,NEW.redeemed_points,NEW.amount_included,NEW.qty_included,
			NEW.orig_insurance_claim_amount,NEW.charge_head,NEW.charge_group,patCategoryId,priSponsorId,
			secSponsorId,NEW.service_sub_group_id,NEW.tax_amt,NEW.sponsor_tax_amt);
	RETURN NULL;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS bill_charge_adjustment_trigger ON bill_charge CASCADE;
CREATE CONSTRAINT TRIGGER bill_charge_adjustment_trigger
	AFTER INSERT ON bill_charge
	DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
	EXECUTE PROCEDURE bill_charge_adjustment_trigger();

DROP FUNCTION IF EXISTS set_sponsor_writeoff() CASCADE;
CREATE OR REPLACE FUNCTION set_sponsor_writeoff() RETURNS TRIGGER AS $BODY$
DECLARE
	openClaimsCnt  integer;
	writennOffClaimsCnt  integer;
	billNo text;
	sponsorDue numeric;
	amountDiff numeric;
	writeOffStatus text;
BEGIN
	IF (NEW.closure_type = 'F' OR NEW.closure_type = 'D' OR NEW.closure_type = 'W') THEN

		billNo := null;
		FOR billNo IN
			(SELECT bcl.bill_no FROM bill_claim bcl
				JOIN insurance_claim icl on(bcl.claim_id = icl.claim_id)
				JOIN bill b on(b.bill_no = bcl.bill_no)
				where icl.claim_id = NEW.claim_id)
		LOOP
			openClaimsCnt := 0;

			SELECT 1 INTO openClaimsCnt  WHERE EXISTS (
				SELECT b.bill_no
				FROM bill b
				JOIN bill_claim bcl ON(b.bill_no = bcl.bill_no AND bcl.claim_id=NEW.claim_id)
				JOIN bill_charge bc ON(bcl.bill_no = bc.bill_no)
				WHERE b.bill_no=billNo AND bcl.claim_status != 'C' AND
				bc.charge_head NOT IN('PHRET','PHCRET')
			 );

			IF (openClaimsCnt IS NULL OR openClaimsCnt = 0) THEN

				writennOffClaimsCnt := 0;

				SELECT primary_total_claim+secondary_total_claim-primary_total_sponsor_receipts-secondary_total_sponsor_receipts-claim_recd_amount into sponsorDue
				FROM bill WHERE bill_no=billNo;

				SELECT auto_close_claims_with_difference into amountDiff FROM generic_preferences;

				SELECT sponsor_writeoff into writeOffStatus FROM bill WHERE bill_no=billNo;

				SELECT 1 into writennOffClaimsCnt where exists (
					select bill_no
					FROM bill_claim bcl
					JOIN insurance_claim icl on(bcl.claim_id = icl.claim_id)
					where bcl.bill_no= billNo and closure_type in('F','D','W')
				);


				IF (writennOffClaimsCnt > 0 AND writeOffStatus != 'A' AND sponsorDue != 0) THEN

					IF (abs(sponsorDue) <= abs(amountDiff)) THEN
						UPDATE bill SET sponsor_writeoff = 'A' Where bill_no = billNo;
					ELSE
						UPDATE bill SET sponsor_writeoff='M' Where bill_no=billNo;
					END IF;

				END IF;
			END IF;
		END LOOP;
	END IF;

	RETURN NEW;
END;
$BODY$ language plpgsql;

DROP FUNCTION IF EXISTS bill_charge_adjustment_trigger_on_bill_status_update() CASCADE;
CREATE OR REPLACE FUNCTION bill_charge_adjustment_trigger_on_bill_status_update() RETURNS trigger AS $BODY$
DECLARE
	seq_no varchar;
BEGIN
	IF (NEW.status != OLD.status AND NEW.status = 'X') THEN
		INSERT INTO bill_charge_adjustment (bill_charge_adjustment_id, charge_id, bill_no,act_rate,act_quantity,amount,
				discount,paid_amount,posted_date,status,username,mod_time,orig_rate,package_unit,doctor_amount,
				insurance_claim_amount,referal_amount,out_house_amount,prescribing_dr_amount,overall_discount_amt,
				dr_discount_amt, pres_dr_discount_amt,ref_discount_amt,hosp_discount_amt,claim_recd_total,
				return_insurance_claim_amt,return_amt,return_qty,redeemed_points,amount_included,qty_included,
				orig_insurance_claim_amount,charge_head,charge_group,patient_category_id,primary_sponsor_id,
				secondary_sponsor_id,service_sub_group_id,tax_amt,sponsor_tax_amt)
		(SELECT generate_id('BILL_CHARGE_ADJ_DEFAULT'), bc.charge_id, bc.bill_no, 0-act_rate,0-act_quantity,
				0-amount,
				0-discount,0-paid_amount,posted_date,'X',bc.username,current_timestamp,
				0-orig_rate,package_unit,0-doctor_amount,0-insurance_claim_amount,
				0-referal_amount,0-coalesce(out_house_amount,0.00),
				0-prescribing_dr_amount,0-overall_discount_amt,
				0-dr_discount_amt, 0-pres_dr_discount_amt,
				0-ref_discount_amt,0-hosp_discount_amt,0-claim_recd_total,
				0-return_insurance_claim_amt,
				0-return_amt,0-return_qty,redeemed_points,0-amount_included,
				0-qty_included,0-orig_insurance_claim_amount,charge_head,charge_group,pr.patient_category_id,
				pbcl.sponsor_id,sbcl.sponsor_id,bc.service_sub_group_id,0-tax_amt,0-sponsor_tax_amt
		FROM bill_charge bc
		JOIN bill b ON(b.bill_no = bc.bill_no)
		LEFT JOIN patient_registration pr ON(pr.patient_id = b.visit_id)
		LEFT JOIN bill_claim pbcl ON(pbcl.bill_no = b.bill_no AND pbcl.priority = 1)
		LEFT JOIN bill_claim sbcl ON(sbcl.bill_no = b.bill_no AND sbcl.priority = 2)
		WHERE bc.status != 'X' AND bc.bill_no=NEW.bill_no);

	ELSEIF (NEW.status != OLD.status AND OLD.status ='X' AND NEW.status = 'A') THEN
		INSERT INTO bill_charge_adjustment (bill_charge_adjustment_id, charge_id, bill_no,act_rate,act_quantity,amount,
				discount,paid_amount,posted_date,status,username,mod_time,orig_rate,package_unit,doctor_amount,
				insurance_claim_amount,referal_amount,out_house_amount,prescribing_dr_amount,overall_discount_amt,
				dr_discount_amt, pres_dr_discount_amt,ref_discount_amt,hosp_discount_amt,claim_recd_total,
				return_insurance_claim_amt,return_amt,return_qty,redeemed_points,amount_included,qty_included,
				orig_insurance_claim_amount,charge_head,charge_group,patient_category_id,primary_sponsor_id,
				secondary_sponsor_id,service_sub_group_id,tax_amt,sponsor_tax_amt)
		(SELECT generate_id('BILL_CHARGE_ADJ_DEFAULT'), bc.charge_id, bc.bill_no, act_rate,act_quantity,
				amount,discount,paid_amount,posted_date,bc.status,bc.username,current_timestamp,
				orig_rate,package_unit,doctor_amount,insurance_claim_amount, referal_amount,
				coalesce(out_house_amount,0.00),prescribing_dr_amount,overall_discount_amt,
				dr_discount_amt, pres_dr_discount_amt,ref_discount_amt,hosp_discount_amt,claim_recd_total,
				return_insurance_claim_amt,return_amt,return_qty,redeemed_points,amount_included,
				qty_included,orig_insurance_claim_amount,charge_head,charge_group,pr.patient_category_id,
				pbcl.sponsor_id,sbcl.sponsor_id,bc.service_sub_group_id,tax_amt,sponsor_tax_amt
		FROM bill_charge bc
		JOIN bill b ON(b.bill_no = bc.bill_no)
		LEFT JOIN patient_registration pr ON(pr.patient_id = b.visit_id)
		LEFT JOIN bill_claim pbcl ON(pbcl.bill_no = b.bill_no AND pbcl.priority = 1)
		LEFT JOIN bill_claim sbcl ON(sbcl.bill_no = b.bill_no AND sbcl.priority = 2)
		WHERE bc.status != 'X' AND bc.bill_no=NEW.bill_no);

	END IF;

	RETURN NULL;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS bill_charge_adjustment_trigger_on_bill_status_update ON bill CASCADE;
CREATE CONSTRAINT TRIGGER bill_charge_adjustment_trigger_on_bill_status_update
	AFTER UPDATE ON bill
	DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
	EXECUTE PROCEDURE bill_charge_adjustment_trigger_on_bill_status_update();

DROP FUNCTION IF EXISTS package_margin_lock_trigger() CASCADE;
CREATE OR REPLACE FUNCTION package_margin_lock_trigger() RETURNS trigger AS $BODY$
BEGIN
	IF (NEW.charge_head = 'MARPKG' AND NOT NEW.is_claim_locked ) THEN
		NEW.is_claim_locked = true;
	END IF;

	RETURN NEW;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS package_margin_lock_trigger ON bill_charge CASCADE;
CREATE TRIGGER package_margin_lock_trigger
	BEFORE UPDATE ON bill_charge
	FOR EACH ROW
	EXECUTE PROCEDURE package_margin_lock_trigger();

DROP TRIGGER IF EXISTS set_sponsor_writeoff_trigger ON insurance_claim CASCADE;
CREATE TRIGGER set_sponsor_writeoff_trigger
AFTER UPDATE
ON insurance_claim
FOR EACH ROW
EXECUTE PROCEDURE set_sponsor_writeoff();

DROP FUNCTION IF EXISTS y_reset_bill_cancellation_status_trigger() CASCADE;
CREATE OR REPLACE FUNCTION y_reset_bill_cancellation_status_trigger() RETURNS TRIGGER AS $BODY$
DECLARE
	billNo text;
	cancelAppStatus text;
BEGIN
	billNo = NEW.bill_no;

	SELECT cancellation_approval_status into cancelAppStatus FROM bill WHERE bill_no=billNo;

	IF (cancelAppStatus = 'A') THEN

	IF (TG_OP = 'INSERT') THEN

		UPDATE bill set cancellation_approval_status='N', cancellation_approved_by=null,cancellation_approved_date=null,
		cancel_approve_amount = 0.00
		WHERE bill_no=NEW.bill_no;

	ELSIF (TG_OP = 'UPDATE') THEN

		IF (NEW.status != OLD.status AND NEW.status = 'X' AND NEW.order_number IS NOT NULL) THEN

		ELSEIF ((NEW.amount != OLD.amount OR NEW.insurance_claim_amount != OLD.insurance_claim_amount)
			AND NEW.status = OLD.status) THEN

		UPDATE bill set cancellation_approval_status='N', cancellation_approved_by=null,cancellation_approved_date=null,
		cancel_approve_amount = 0.00
		WHERE bill_no=NEW.bill_no;

		END IF;
	END IF;

	END IF;

	RETURN NEW;
END;
$BODY$ language plpgsql;

DROP TRIGGER IF EXISTS y_reset_bill_cancellation_status_trigger ON bill_charge CASCADE;
CREATE TRIGGER y_reset_bill_cancellation_status_trigger
	AFTER INSERT OR UPDATE
	ON bill_charge
	FOR EACH ROW
	EXECUTE PROCEDURE y_reset_bill_cancellation_status_trigger();

DROP VIEW IF EXISTS patientdetails CASCADE;
CREATE OR REPLACE VIEW patientdetails AS
SELECT get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS patname,
	pr.mr_no, pr.patient_id, dep.dept_name, doc.doctor_name, od.org_name, pr.visit_type AS patienttype,
	pd.name_local_language, pr.reg_date, tpa.tpa_name
FROM patient_details pd
   	JOIN patient_registration pr ON (pr.mr_no = pd.mr_no)
   	LEFT JOIN department dep ON (dep.dept_id = pr.dept_name)
	LEFT JOIN doctors doc ON (doc.doctor_id = pr.doctor)
	LEFT JOIN organization_details od ON (od.org_id = pr.org_id)
	LEFT JOIN tpa_master tpa ON (tpa.tpa_id = pr.primary_sponsor_id)
	LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id)
WHERE ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )
;

DROP VIEW IF EXISTS all_patient_reports_view;
CREATE OR REPLACE VIEW all_patient_reports_view AS
SELECT form_id AS id, form_caption AS caption, form_title AS title, form_type AS type,
	'F' AS format, status
FROM form_header
UNION
SELECT format_id AS id, template_caption AS caption, template_title AS title, template_type AS type,
	'T' AS format, status
FROM discharge_format
UNION
SELECT template_id||'' AS id, template_name AS capton, '' AS title, 'Discharge Summary' AS type,
'P' AS format, status FROM doc_pdf_form_templates WHERE specialized=true and doc_type='SYS_DS'
;

DROP VIEW IF EXISTS bedcharges_view;
CREATE VIEW bedcharges_view AS
SELECT  bed_type AS bedtype, bed_charge, nursing_charge, initial_payment,
	duty_charge, maintainance_charge, luxary_tax, organization, bed_status,hourly_charge,
	bed_charge_discount,nursing_charge_discount,duty_charge_discount,maintainance_charge_discount,
	hourly_charge_discount,initial_payment_discount,billing_bed_type,
	daycare_slab_1_charge,daycare_slab_1_charge_discount,daycare_slab_2_charge,daycare_slab_2_charge_discount,
	daycare_slab_3_charge,daycare_slab_3_charge_discount,is_override
FROM bed_details
JOIN bed_types on(bed_type_name = bed_type)
UNION
SELECT intensive_bed_type AS bedtype, bed_charge  ,nursing_charge,initial_payment,
	duty_charge,maintainance_charge,luxary_tax,organization,bed_status,hourly_charge,
	bed_charge_discount,nursing_charge_discount,duty_charge_discount,maintainance_charge_discount,
	hourly_charge_discount,initial_payment_discount,billing_bed_type,
	daycare_slab_1_charge,daycare_slab_1_charge_discount,daycare_slab_2_charge,daycare_slab_2_charge_discount,
	daycare_slab_3_charge,daycare_slab_3_charge_discount,is_override
FROM icu_bed_charges icb
JOIN bed_types ON (bed_type_name = intensive_bed_type)
WHERE icb.bed_type='GENERAL'
;

DROP VIEW IF EXISTS missing_bed_charges_view;
CREATE VIEW missing_bed_charges_view AS
SELECT org_id, bed_type
FROM organization_details o
	CROSS JOIN (SELECT DISTINCT bed_type_name AS bed_type FROM bed_types WHERE is_icu = 'N') AS b
WHERE NOT EXISTS (SELECT bed_type FROM bed_details bd WHERE bd.bed_type=b.bed_type
	AND bd.organization=o.org_id)
;

DROP VIEW IF EXISTS missing_icu_bed_charges_view;
CREATE OR REPLACE VIEW missing_icu_bed_charges_view AS
SELECT org_id, bed_type, intensive_bed_type
FROM organization_details o
	CROSS JOIN (SELECT DISTINCT bed_type_name AS bed_type FROM bed_types WHERE billing_bed_type='Y') AS b
	CROSS JOIN (SELECT DISTINCT intensive_bed_type FROM icu_bed_charges) AS ib
WHERE NOT EXISTS (SELECT intensive_bed_type FROM icu_bed_charges ibc WHERE
	ib.intensive_bed_type=ibc.intensive_bed_type AND ibc.bed_type=b.bed_type AND ibc.organization=o.org_id)
;

DROP VIEW IF EXISTS all_beds_orgs_view CASCADE;
CREATE VIEW all_beds_orgs_view AS
SELECT bed_type_name AS bed_type, org_id
FROM bed_types CROSS JOIN organization_details
WHERE billing_bed_type = 'Y';

DROP VIEW IF EXISTS all_active_beds_orgs_view CASCADE;
CREATE VIEW all_active_beds_orgs_view AS
SELECT bed_type_name AS bed_type, org_id
FROM bed_types bt CROSS JOIN organization_details
WHERE billing_bed_type = 'Y' and bt.status='A';

DROP VIEW IF EXISTS missing_test_charges_view;
CREATE VIEW missing_test_charges_view AS
SELECT org_id, bed_type, test_id, priority
FROM all_beds_orgs_view ob
	CROSS JOIN (SELECT test_id, priority FROM diagnostics d
	CROSS JOIN (values ('R')) AS p(priority)) AS tp
WHERE NOT EXISTS (
	SELECT test_id
	FROM diagnostic_charges dc
	WHERE dc.test_id = tp.test_id AND dc.priority = tp.priority
		AND dc.org_name = ob.org_id AND dc.bed_type = ob.bed_type
)
;

DROP VIEW IF EXISTS missing_service_charges_view;
CREATE VIEW missing_service_charges_view AS
SELECT org_id, bed_type, service_id
FROM all_beds_orgs_view ob
	CROSS JOIN services s
WHERE NOT EXISTS (
	SELECT service_id from service_master_charges smc
 	WHERE smc.service_id=s.service_id AND smc.org_id=ob.org_id AND smc.bed_type=ob.bed_type
)
;

DROP VIEW IF EXISTS missing_consultation_charges_view;
CREATE VIEW missing_consultation_charges_view AS
SELECT org_id, bed_type, consultation_type_id
FROM all_beds_orgs_view ob
	CROSS JOIN consultation_types c
WHERE NOT EXISTS (
	SELECT consultation_type_id from consultation_charges cc
 	WHERE cc.consultation_type_id=c.consultation_type_id AND cc.org_id=ob.org_id AND cc.bed_type=ob.bed_type
)
;

DROP VIEW IF EXISTS missing_equipment_charges_view;
CREATE VIEW missing_equipment_charges_view AS
SELECT org_id, bed_type, eq_id
FROM all_beds_orgs_view ob
	CROSS JOIN equipment_master e
WHERE NOT EXISTS (
	SELECT equip_id FROM equipement_charges ec
	WHERE ec.equip_id=e.eq_id AND ec.org_id=ob.org_id AND ec.bed_type=ob.bed_type
)
;

DROP VIEW IF EXISTS missing_doctor_charges_view;
CREATE VIEW missing_doctor_charges_view AS
SELECT org_id, bed_type, doctor_id
FROM all_beds_orgs_view ob
	CROSS JOIN doctors d
WHERE NOT EXISTS (
	SELECT doctor_name FROM doctor_consultation_charge dc
  	WHERE dc.doctor_name=d.doctor_id AND dc.organization=ob.org_id AND dc.bed_type=ob.bed_type
)
;


DROP VIEW IF EXISTS missing_doctor_org_view;
CREATE VIEW missing_doctor_org_view AS
SELECT org_id, bed_type, doctor_id
FROM all_beds_orgs_view ob
	CROSS JOIN doctors d
WHERE NOT EXISTS (
	SELECT doctor_id FROM doctor_org_details dod
  	WHERE dod.doctor_id=d.doctor_id AND dod.org_id=ob.org_id
)
;

DROP VIEW IF EXISTS missing_operation_charges_view;
CREATE VIEW missing_operation_charges_view AS
SELECT org_id, bed_type, op_id
FROM all_beds_orgs_view ob
	CROSS JOIN operation_master o
WHERE NOT EXISTS (
	SELECT op_id FROM operation_charges oc
  	WHERE oc.op_id=o.op_id AND oc.org_id=ob.org_id AND oc.bed_type=ob.bed_type
)
;

DROP VIEW IF EXISTS missing_theatre_charges_view;
CREATE VIEW missing_theatre_charges_view AS
SELECT org_id, bed_type, theatre_id
FROM all_beds_orgs_view ob
	CROSS JOIN theatre_master t
WHERE NOT EXISTS (
	SELECT theatre_id FROM theatre_charges tc
	WHERE tc.theatre_id=t.theatre_id AND tc.org_id=ob.org_id AND tc.bed_type=ob.bed_type
)
;

DROP VIEW IF EXISTS missing_package_charges_view CASCADE;
CREATE OR REPLACE VIEW missing_package_charges_view AS
 	SELECT pk.package_id,ob.org_id, ob.bed_type
    FROM all_beds_orgs_view ob
    CROSS JOIN ( SELECT p.package_id FROM packages p) pk
    WHERE NOT (EXISTS ( SELECT pc.package_id
    FROM package_charges pc
    WHERE pc.package_id::text = pk.package_id::text AND  pc.org_id::text = ob.org_id::text AND pc.bed_type::text = ob.bed_type::text));

DROP VIEW IF EXISTS missing_dyna_package_charges_view CASCADE;
CREATE OR REPLACE VIEW missing_dyna_package_charges_view AS
SELECT dp.dyna_package_id, ob.org_id, ob.bed_type
FROM all_beds_orgs_view ob
    CROSS JOIN dyna_packages dp
WHERE NOT EXISTS (
	SELECT dyna_package_id from dyna_package_charges dpc
 	WHERE dpc.dyna_package_id=dp.dyna_package_id AND dpc.org_id=ob.org_id AND dpc.bed_type=ob.bed_type
)
;

DROP VIEW IF EXISTS missing_per_diem_codes_charges_view CASCADE;
CREATE OR REPLACE VIEW missing_per_diem_codes_charges_view AS
SELECT pm.per_diem_code, ob.org_id, ob.bed_type
FROM all_beds_orgs_view ob
    CROSS JOIN per_diem_codes_master pm
WHERE NOT EXISTS (
	SELECT per_diem_code from per_diem_codes_charges pc
 	WHERE pc.per_diem_code=pm.per_diem_code AND pc.org_id=ob.org_id AND pc.bed_type=ob.bed_type
)
;

DROP VIEW IF EXISTS missing_dyna_package_limits_view CASCADE;
CREATE OR REPLACE VIEW missing_dyna_package_limits_view AS
SELECT dp.dyna_package_id, ob.org_id, ob.bed_type, dpc.dyna_pkg_cat_id
FROM all_beds_orgs_view ob
	CROSS JOIN dyna_package_category dpc
    CROSS JOIN dyna_packages dp
WHERE NOT EXISTS (
	SELECT dyna_package_id FROM dyna_package_category_limits dpcl
 	WHERE dpcl.dyna_package_id=dp.dyna_package_id AND dpcl.org_id=ob.org_id AND dpcl.bed_type=ob.bed_type
	  AND dpcl.dyna_pkg_cat_id = dpc.dyna_pkg_cat_id
)
;

DROP VIEW IF EXISTS missing_anesthesia_type_charges_view;
CREATE VIEW missing_anesthesia_type_charges_view AS
SELECT org_id, bed_type, anesthesia_type_id
FROM all_beds_orgs_view ob
	CROSS JOIN anesthesia_type_master an
WHERE NOT EXISTS (
	SELECT anesthesia_type_id FROM anesthesia_type_charges ac
	WHERE ac.anesthesia_type_id=an.anesthesia_type_id AND ac.org_id=ob.org_id AND ac.bed_type=ob.bed_type
)
;

DROP VIEW IF EXISTS missing_registration_charges_view;
CREATE VIEW missing_registration_charges_view AS
SELECT org_id, bed_type
FROM all_beds_orgs_view ob
WHERE NOT EXISTS (
	SELECT * FROM registration_charges r WHERE r.org_id = ob.org_id and r.bed_type = ob.bed_type
);

DROP VIEW IF EXISTS missing_diet_charges_view;
CREATE VIEW missing_diet_charges_view AS
SELECT org_id, bed_type, diet_id
FROM all_beds_orgs_view ob
	CROSS JOIN diet_master d
WHERE NOT EXISTS (
	SELECT diet_id FROM diet_charges dc
	WHERE dc.diet_id=d.diet_id AND dc.org_id=ob.org_id AND dc.bed_type=ob.bed_type
);

DROP VIEW IF EXISTS missing_test_org_view;
CREATE VIEW missing_test_org_view AS
SELECT org_id, test_id
FROM organization_details ob
	CROSS JOIN diagnostics d
WHERE NOT EXISTS (
	SELECT test_id from test_org_details tod WHERE tod.test_id = d.test_id
	AND tod.org_id = ob.org_id
)
;

DROP VIEW IF EXISTS missing_service_org_view;
CREATE VIEW missing_service_org_view AS
SELECT org_id, service_id
FROM organization_details ob
	CROSS JOIN services s
WHERE NOT EXISTS (
	SELECT service_id FROM service_org_details sod
	WHERE sod.service_id=s.service_id AND sod.org_id=ob.org_id
);

DROP VIEW IF EXISTS missing_dyna_package_org_view;
CREATE VIEW missing_dyna_package_org_view AS
SELECT org_id, dyna_package_id
FROM organization_details ob
	CROSS JOIN dyna_packages d
WHERE NOT EXISTS (
	SELECT dyna_package_id FROM dyna_package_org_details dod
	WHERE dod.dyna_package_id=d.dyna_package_id AND dod.org_id=ob.org_id
);

DROP VIEW IF EXISTS missing_operation_org_view;
CREATE VIEW missing_operation_org_view AS
SELECT org_id, op_id
FROM organization_details ob
	CROSS JOIN operation_master o
WHERE NOT EXISTS (
	SELECT op_id FROM operation_org_details ood
	WHERE ood.operation_id=o.op_id AND ood.org_id=ob.org_id
);

DROP VIEW IF EXISTS missing_consultation_org_view;
CREATE VIEW missing_consultation_org_view AS
SELECT org_id, consultation_type_id
FROM organization_details ob
	CROSS JOIN consultation_types c
WHERE NOT EXISTS (
	SELECT * FROM consultation_org_details cod
	WHERE cod.consultation_type_id=c.consultation_type_id AND cod.org_id=ob.org_id
);

DROP VIEW IF EXISTS missing_anesthesia_type_org_view;
CREATE VIEW missing_anesthesia_type_org_view AS
SELECT org_id, anesthesia_type_id
FROM organization_details ob
	CROSS JOIN anesthesia_type_master a
WHERE NOT EXISTS (
	SELECT * FROM anesthesia_type_org_details aod
	WHERE aod.anesthesia_type_id=a.anesthesia_type_id AND aod.org_id=ob.org_id
);

--
-- No diet_org_details table.
--

DROP VIEW IF EXISTS missing_doctor_op_charges_view;
CREATE VIEW missing_doctor_op_charges_view AS
SELECT org_id, doctor_id
FROM organization_details ob
	CROSS JOIN doctors d
WHERE NOT EXISTS (
	SELECT doctor_id FROM doctor_op_consultation_charge doc
	WHERE doc.doctor_id=d.doctor_id AND doc.org_id=ob.org_id
);

DROP VIEW IF EXISTS missing_action_rights_view;
CREATE VIEW missing_action_rights_view AS
SELECT role_id, action
FROM (VALUES ('bed_close'), ('bill_reopen'), ('cancel_charges'), ('dishcharge_close'), ('discount'),
	('edit_charges'), ('insurance_close')) AS actions(action)
 	CROSS JOIN (
		SELECT DISTINCT role_id FROM u_role
		WHERE role_id NOT IN(1,2)
	) AS roles
EXCEPT
SELECT role_id, action FROM action_rights
;

DROP VIEW IF EXISTS missing_plan_details_view ;
CREATE VIEW missing_plan_details_view AS
SELECT plan_id, pat_type, insurance_category_id
FROM insurance_plan_main ipm
	CROSS JOIN (SELECT insurance_category_id FROM item_insurance_categories WHERE insurance_payable = 'Y')
		AS allcats
	CROSS JOIN (VALUES ('o'), ('i') ) AS pat_types(pat_type)
EXCEPT
SELECT plan_id, patient_type, insurance_category_id FROM insurance_plan_details
;

DROP VIEW IF EXISTS treating_departments_view CASCADE;
CREATE VIEW treating_departments_view AS
SELECT serv_dept_id::text AS dept_id, department AS dept_name, 'Service' AS dept_category FROM services_departments
UNION ALL
SELECT ddept_id AS dept_id, ddept_name AS dept_name,
CASE WHEN category = 'DEP_LAB' THEN 'Laboratory' ELSE 'Radiology' END AS dept_category
 FROM diagnostics_departments
UNION ALL
SELECT dept_id AS dept_id, dept_name AS dept_name, 'Others' AS dept_category FROM department;


DROP VIEW IF EXISTS revenue_settlements;
CREATE VIEW revenue_settlements AS
SELECT date(b.finalized_date), sum(bc.amount)
FROM bill_charge bc
	JOIN bill b USING (bill_no)
	JOIN patient_registration pr ON (b.visit_id=pr.patient_id)
WHERE date(b.finalized_date) BETWEEN current_date-6 AND current_date
	AND pr.status = 'I' AND b.status NOT IN ('X','A') AND bc.status != 'X'
GROUP BY date(b.finalized_date)
ORDER BY date(b.finalized_date) ;


DROP VIEW IF EXISTS usage_yesterday;
CREATE VIEW  usage_yesterday AS
 SELECT * FROM
  (SELECT current_date-1 AS "Log for Date") AS date,
  (SELECT COUNT(pr.patient_id) AS regd_count FROM patient_registration pr
	WHERE date(reg_date) =  current_date-1 ) AS regd_count,
  (SELECT COUNT(b.finalized_date) AS bills_finalized FROM bill b
	WHERE date(finalized_date)  =  current_date-1) AS bills_finalized,
  (SELECT COUNT(*) AS bill_claims FROM bill b
	WHERE date(finalized_date)  =  current_date-1 and primary_claim_status != '' ) AS claims,
  (SELECT COUNT(tr.report_id) AS test_reports FROM test_visit_reports tr
	WHERE date(report_date)  =  current_date-1 ) AS test_reports,
  (SELECT COUNT (*) AS dis_summary FROM patient_registration pd
	WHERE date(discharge_date)  =  current_date-1 and discharge_format != '')AS discharge_summary,
  (SELECT COUNT (*) AS pharmacy_bills FROM store_sales_main ps
	WHERE date(sale_date)  =  current_date-1 )AS pharmacy_sales,
  (SELECT COUNT (*) AS admission_count FROM admission a
	WHERE date(admit_date)  =  current_date-1 )AS admission_num,
  (SELECT COUNT (*) AS insurance_count FROM insurance_case ic
	WHERE date(case_added_date)  =  current_date-1 )AS insurance_num,
  (SELECT COUNT (*) AS voucher_count FROM payments p
	WHERE date(date)  =  current_date-1 )AS voucher_num,
  (SELECT COUNT (*) AS issue_count FROM stock_issue_main i
	WHERE date(date_time)  =  current_date-1 )AS num_issues,
  (SELECT COUNT (*) AS compl_logged FROM complaintslog l
	WHERE date(logged_date)  =  current_date-1 )AS new_complaints,
  (SELECT COUNT (*) AS compl_open FROM complaintslog l
	WHERE complaint_status = 'Open'  )AS open_complaints

;

DROP VIEW IF EXISTS usage_today;
CREATE VIEW usage_today AS
 SELECT * FROM
  (SELECT current_date AS "Log for Date") AS date,
  (SELECT COUNT(pr.patient_id) AS regd_count FROM patient_registration pr
	WHERE date(reg_date) =  current_date ) AS regd_count,
  (SELECT COUNT(b.finalized_date) AS bills_finalized FROM bill b
	WHERE date(finalized_date)  =  current_date) AS bills_finalized,
  (SELECT COUNT(*) AS bill_claims FROM bill b
	WHERE date(finalized_date)  =  current_date and primary_claim_status != '' ) AS claims,
  (SELECT COUNT(tr.report_id) AS test_reports FROM test_visit_reports tr
	WHERE date(report_date)  =  current_date ) AS test_reports,
  (SELECT COUNT (*) AS dis_summary FROM patient_registration pd
	WHERE date(discharge_date) =  current_date and discharge_format != '')AS discharge_summary,
  (SELECT COUNT (*) AS pharmacy_bills FROM store_sales_main ps
	WHERE date(sale_date)  =  current_date )AS pharmacy_sales,
  (SELECT COUNT (*) AS admission_count FROM admission a
        WHERE date(admit_date)  =  current_date )AS admission_num,
  (SELECT COUNT (*) AS insurance_count FROM insurance_case ic
        WHERE date(case_added_date)  =  current_date )AS insurance_num,
  (SELECT COUNT (*) AS voucher_count FROM payments p
        WHERE date(date)  =  current_date )AS voucher_num,
  (SELECT COUNT (*) AS issue_count FROM stock_issue_main i
	WHERE date(date_time)  =  current_date )AS num_issues,
  (SELECT COUNT (*) AS compl_logged FROM complaintslog l
	WHERE date(logged_date)  =  current_date )AS new_complaints,
  (SELECT COUNT (*) AS compl_open FROM complaintslog l
	WHERE complaint_status = 'Open'  )AS open_complaints
;

DROP VIEW IF EXISTS usage_check;
CREATE VIEW usage_check AS
SELECT date(open_date), count(open_date) AS bills_count
FROM bill
WHERE date(open_date)  BETWEEN  current_date-6 AND current_date
GROUP BY date(open_date);

-- obsolete.
DROP VIEW IF EXISTS bill_collections_report_view CASCADE;

DROP VIEW IF EXISTS diag_schedules_view;
CREATE VIEW diag_schedules_view AS
SELECT tp.mr_no, tp.pat_id AS patient_id, tp.prescribed_id, tp.labno, tp.pres_date, tp.common_order_id,
	om.oh_name, tp.conducted, tp.report_id, tvr.report_name, tvr.report_date,
	CASE WHEN d.conduction_format='C' THEN cim.short_impression
	ELSE him.short_impression END AS short_impression,isr.patient_other_info,
	CASE WHEN tvr.report_date IS NOT NULL THEN 'Y' ELSE 'N' END AS report_has_data,
	CASE WHEN d.sample_needed = 'n' THEN 'U' ELSE tp.sflag END AS sample_status,
	get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS patient_full_name,
	isr.patient_name AS inc_patient_name, ih.hospital_name AS ih_name,
	CASE WHEN isr.incoming_visit_id IS NOT NULL THEN 't' ELSE pr.visit_type END AS visit_type,pr.op_type,otn.op_type_name,
	d.ddept_id, d.test_name, CASE WHEN is_outhouse_test(d.test_id,coalesce(pr.center_id, isr.center_id)::integer) THEN 'O' ELSE 'I'
	END AS house_status, d.conduction_applicable, dd.category,b.payment_status AS payment_status, b.bill_type,
	CASE WHEN tp.re_conduction = true THEN 'Y' ELSE 'N' END AS re_conduction,
	coalesce(pr.center_id, isr.center_id) AS center_id,tp.sample_no,tvr.report_results_severity_status,d.conduction_format,
	pr.collection_center_id,tp.priority,
	CASE WHEN pr.patient_id IS NOT NULL THEN 'hospital' ELSE 'incoming' END AS hospital
FROM tests_prescribed tp
	LEFT JOIN test_visit_reports tvr USING (report_id)
	JOIN diagnostics d USING (test_id)
	JOIN diagnostics_departments dd USING (ddept_id)
	LEFT JOIN test_histopathology_results thr ON(thr.prescribed_id = tp.prescribed_id)
	LEFT JOIN histo_impression_master him ON(him.impression_id = thr.impression_id)
	LEFT JOIN test_cytology_results tcr ON(tcr.prescribed_id = tp.prescribed_id)
	LEFT JOIN histo_impression_master cim ON(tcr.impression_id = cim.impression_id)
	LEFT JOIN outsource_sample_details oh ON (oh.prescribed_id = tp.prescribed_id)
	LEFT JOIN diag_outsource_master dom ON (dom.outsource_dest_id = oh.outsource_dest_id)
	LEFT JOIN outhouse_master om ON (om.oh_id = dom.outsource_dest)
	LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id)
	LEFT JOIN op_type_names otn ON (otn.op_type = pr.op_type)
	LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
	LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
	LEFT JOIN incoming_sample_registration isr ON (tp.pat_id = isr.incoming_visit_id)
	LEFT JOIN incoming_hospitals ih ON (ih.hospital_id = isr.orig_lab_name)
	LEFT JOIN bill_activity_charge bac ON (bac.activity_id = tp.prescribed_id::varchar
		AND bac.activity_code='DIA')
	LEFT JOIN bill_charge bc ON  bc.charge_id=bac.charge_id
	LEFT JOIN bill b ON b.bill_no=bc.bill_no
WHERE tp.conducted NOT IN ('X','U','RBS','RAS','NRN','CRN') AND ( tp.report_id = 0 OR tp.report_id IS NULL OR tvr.signed_off != 'Y') 
AND ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) )
;


DROP VIEW IF EXISTS diag_lab_schedules_view;
CREATE VIEW diag_lab_schedules_view AS
SELECT tp.mr_no, tp.exp_rep_ready_time, tp.pat_id AS patient_id, tp.prescribed_id, tp.labno, CAST(tp.pres_date AS date) as pres_date, tp.common_order_id, tp.pres_doctor,
	coalesce(om.oh_name,hcm.center_name) AS oh_name, tp.conducted, tp.report_id, tvr.report_name, tvr.report_date,sc.sample_status AS sample_collection_status,
	CASE WHEN d.conduction_format='C' THEN cim.short_impression
	ELSE him.short_impression END AS short_impression,isr.patient_other_info,
	CASE WHEN tvr.report_date IS NOT NULL THEN 'Y' ELSE 'N' END AS report_has_data,
	CASE WHEN d.sample_needed = 'n' THEN 'U' ELSE tp.sflag END AS sample_status,
	get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS patient_full_name,
	isr.patient_name AS inc_patient_name, ih.hospital_name AS ih_name,
	CASE WHEN isr.incoming_visit_id IS NOT NULL THEN 't' ELSE pr.visit_type END AS visit_type,pr.op_type,otn.op_type_name,
	d.ddept_id, d.test_name, CASE WHEN is_outhouse_test(d.test_id,coalesce(pr.center_id, isr.center_id)::integer) THEN 'O' ELSE 'I'
	END AS house_status, d.conduction_applicable, dd.category,b.payment_status AS payment_status, b.bill_type,
	CASE WHEN tp.re_conduction = true THEN 'Y' ELSE 'N' END AS re_conduction,
	coalesce(pr.center_id, isr.center_id) AS center_id,COALESCE(sc.coll_sample_no, sc.sample_sno) AS sample_no,isrd.orig_sample_no,tvr.report_results_severity_status,d.conduction_format,
	pr.collection_center_id,tp.priority,COALESCE(pr.reference_docto_id,isr.referring_doctor) AS reference_docto_id, 
	CASE WHEN pr.patient_id IS NOT NULL THEN 'hospital' ELSE 'incoming' END AS hospital, dom.outsource_dest_type,
	dom.outsource_dest, isr.incoming_source_type, doc.doctor_name, doc.doctor_id, sc.sample_receive_status,
	case WHEN coalesce(tpa.sponsor_type, itpa.sponsor_type, 'R')='R' THEN 'R' ELSE 'S' END AS patient_sponsor_type, d.isconfidential,
	tp.remarks
FROM tests_prescribed tp
	LEFT JOIN test_visit_reports tvr USING (report_id)
	JOIN diagnostics d USING (test_id)
	JOIN diagnostics_departments dd on(dd.ddept_id=d.ddept_id AND dd.category='DEP_LAB')
	LEFT JOIN test_histopathology_results thr ON(thr.prescribed_id = tp.prescribed_id)
	LEFT JOIN histo_impression_master him ON(him.impression_id = thr.impression_id)
	LEFT JOIN test_cytology_results tcr ON(tcr.prescribed_id = tp.prescribed_id)
	LEFT JOIN histo_impression_master cim ON(tcr.impression_id = cim.impression_id)
	LEFT JOIN outsource_sample_details oh ON (oh.prescribed_id = tp.prescribed_id)
	LEFT JOIN diag_outsource_master dom ON (dom.outsource_dest_id = oh.outsource_dest_id)
	LEFT JOIN outhouse_master om ON (om.oh_id = dom.outsource_dest)
	LEFT JOIN hospital_center_master hcm ON(hcm.center_id::text = dom.outsource_dest)
	LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id)
	LEFT JOIN tpa_master tpa ON (pr.primary_sponsor_id=tpa.tpa_id)
	LEFT JOIN op_type_names otn ON (otn.op_type = pr.op_type)
	LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
	LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
	LEFT JOIN incoming_sample_registration isr ON (tp.pat_id = isr.incoming_visit_id)
	LEFT JOIN incoming_sample_registration_details isrd ON (isrd.prescribed_id = tp.prescribed_id AND isr.incoming_visit_id = isrd.incoming_visit_id)
	-- these are used to get the sponsor type of the original visit for internal lab patient.
	LEFT JOIN tests_prescribed itp ON (itp.prescribed_id=tp.coll_prescribed_id)
	LEFT JOIN patient_registration ipr ON (ipr.patient_id=itp.pat_id)
	LEFT JOIN tpa_master itpa ON (itpa.tpa_id=ipr.primary_sponsor_id)

	LEFT JOIN incoming_hospitals ih ON (ih.hospital_id = isr.orig_lab_name)
	LEFT JOIN sample_collection sc ON (sc.sample_collection_id = tp.sample_collection_id)
	LEFT JOIN bill_activity_charge bac ON (bac.activity_id = tp.prescribed_id::varchar
		AND bac.activity_code='DIA')
	LEFT JOIN bill_charge bc ON  bc.charge_id=bac.charge_id
	LEFT JOIN bill b ON b.bill_no=bc.bill_no
	-- if a test from a package, THEN retreive the doctor id from bill activity charge only, do not retreive from the bill charge table, this is
	-- because for a package which has operation, THEN payee_doctor_id gets updated with operation surgeon name. Refer BUG : 47755, 47757.
	LEFT JOIN doctors doc ON (doc.doctor_id = COALESCE(bac.doctor_id, (case WHEN bc.charge_group='PKG' THEN null ELSE bc.payee_doctor_id end)))
WHERE tp.conducted IN ('P','N','C','V','RC','RV','RP','MA','TS','CC','CR') AND ( tp.report_id = 0 OR tp.report_id IS NULL OR tvr.signed_off != 'Y') 
AND ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) )
;


DROP VIEW IF EXISTS diag_rad_schedules_view;
CREATE VIEW diag_rad_schedules_view AS
SELECT tp.mr_no,tp.exp_rep_ready_time, tp.pat_id AS patient_id, tp.prescribed_id, tp.labno, CAST(tp.pres_date AS date) as pres_date, tp.common_order_id, tp.pres_doctor,
	om.oh_name, tp.conducted, tp.report_id, tvr.report_name, tvr.report_date,sc.sample_status AS sample_collection_status,
	CASE WHEN d.conduction_format='C' THEN cim.short_impression
	ELSE him.short_impression END AS short_impression,isr.patient_other_info,
	CASE WHEN tvr.report_date IS NOT NULL THEN 'Y' ELSE 'N' END AS report_has_data,
	CASE WHEN d.sample_needed = 'n' THEN 'U' ELSE tp.sflag END AS sample_status,
	get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS patient_full_name,
	isr.patient_name AS inc_patient_name, ih.hospital_name AS ih_name,
	CASE WHEN isr.incoming_visit_id IS NOT NULL THEN 't' ELSE pr.visit_type END AS visit_type,pr.op_type,otn.op_type_name,
	d.ddept_id, d.test_name, CASE WHEN is_outhouse_test(d.test_id,coalesce(pr.center_id, isr.center_id)::integer) THEN 'O' ELSE 'I'
	END AS house_status, d.conduction_applicable, dd.category,b.payment_status AS payment_status, b.bill_type,
	CASE WHEN tp.re_conduction = true THEN 'Y' ELSE 'N' END AS re_conduction,
	coalesce(pr.center_id, isr.center_id) AS center_id,tp.sample_no,tvr.report_results_severity_status,d.conduction_format,
	pr.collection_center_id,tp.priority, doc.doctor_name, doc.doctor_id, CASE WHEN pr.patient_id IS NOT NULL THEN 'hospital' ELSE 'incoming' END AS hospital,
	case WHEN coalesce(tpa.sponsor_type, itpa.sponsor_type, 'R')='R' THEN 'R' ELSE 'S' END AS patient_sponsor_type, sc.sample_receive_status, d.isconfidential,
	tp.remarks
FROM tests_prescribed tp
	LEFT JOIN test_visit_reports tvr USING (report_id)
	JOIN diagnostics d USING (test_id)
	JOIN diagnostics_departments dd ON (dd.ddept_id=d.ddept_id AND dd.category='DEP_RAD')
	LEFT JOIN test_histopathology_results thr ON(thr.prescribed_id = tp.prescribed_id)
	LEFT JOIN histo_impression_master him ON(him.impression_id = thr.impression_id)
	LEFT JOIN test_cytology_results tcr ON(tcr.prescribed_id = tp.prescribed_id)
	LEFT JOIN histo_impression_master cim ON(tcr.impression_id = cim.impression_id)
	LEFT JOIN outsource_sample_details oh ON (oh.prescribed_id = tp.prescribed_id)
	LEFT JOIN diag_outsource_master dom ON (dom.outsource_dest_id = oh.outsource_dest_id)
	LEFT JOIN outhouse_master om ON (om.oh_id = dom.outsource_dest)
	LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id)
	LEFT JOIN tpa_master tpa ON (pr.primary_sponsor_id=tpa.tpa_id)
	LEFT JOIN op_type_names otn ON (otn.op_type = pr.op_type)
	LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
	LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
	LEFT JOIN incoming_sample_registration isr ON (tp.pat_id = isr.incoming_visit_id)
	LEFT JOIN incoming_sample_registration_details isrd ON (isrd.prescribed_id = tp.prescribed_id AND isr.incoming_visit_id = isrd.incoming_visit_id)
	-- these are used to get the sponsor type of the original visit for internal lab patient.
	LEFT JOIN tests_prescribed itp ON (itp.prescribed_id=isrd.source_test_prescribed)
	LEFT JOIN patient_registration ipr ON (ipr.patient_id=itp.pat_id)
	LEFT JOIN tpa_master itpa ON (itpa.tpa_id=ipr.primary_sponsor_id)

	LEFT JOIN incoming_hospitals ih ON (ih.hospital_id = isr.orig_lab_name)
	LEFT JOIN sample_collection sc ON (sc.sample_collection_id = tp.sample_collection_id)
	LEFT JOIN bill_activity_charge bac ON (bac.activity_id = tp.prescribed_id::varchar
		AND bac.activity_code='DIA')
	LEFT JOIN bill_charge bc ON  bc.charge_id=bac.charge_id
	LEFT JOIN bill b ON b.bill_no=bc.bill_no
	LEFT JOIN doctors doc ON (doc.doctor_id = COALESCE(bac.doctor_id, (case WHEN bc.charge_group='PKG' THEN null ELSE bc.payee_doctor_id end)))
WHERE tp.conducted IN ('P','N','C','V','RC','RV','RP','MA','TS','CC','CR') 
AND ( tp.report_id = 0 OR tp.report_id IS NULL OR tvr.signed_off != 'Y') 
AND ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) );

---
-- Summary view of the above, so that we can do the initial query without all the required joins
-- to make the retrieval of the initial list of patients much faster. All filterable fields should
-- be available, though. The previous view gets all details that are required for displaying the
-- screen (ie, includes fields used only for display and not required for filtering like bill status)--

DROP VIEW IF EXISTS diag_schedules_summary_view;
CREATE VIEW diag_schedules_summary_view AS
SELECT tp.mr_no,tp.exp_rep_ready_time, tp.pat_id AS patient_id, tp.prescribed_id, tp.labno, tp.pres_date,tp.common_order_id,
	om.oh_name, tp.conducted,COALESCE(itp.sample_no, tp.sample_no) AS sample_no, isrd.orig_sample_no, tp.pres_doctor,
	CASE WHEN d.conduction_format='C' THEN cim.short_impression
	ELSE him.short_impression END AS short_impression,
	CASE WHEN d.sample_needed = 'n' THEN 'U' ELSE tp.sflag END AS sample_status,
	isr.patient_name AS inc_patient_name, ih.hospital_name AS ih_name,
	CASE WHEN isr.incoming_visit_id IS NOT NULL THEN 't' ELSE pr.visit_type END AS visit_type,
	d.ddept_id, d.test_name, CASE WHEN is_outhouse_test(d.test_id,coalesce(pr.center_id, isr.center_id)::integer) THEN 'O' ELSE 'I'
	END AS house_status, dd.category, tvr.report_date,
	coalesce(pr.center_id, isr.center_id) AS center_id,tvr.report_results_severity_status,
	coalesce(pr.reg_date, isr.date) AS reg_date,d.conduction_format,isr.patient_other_info,
	tp.token_number, st.status AS sample_type_status,
	case WHEN d.results_entry_applicable='t' THEN 'Yes' ELSE 'No' END AS results_entry_applicable,
	pr.collection_center_id,tp.priority,
	CASE WHEN pr.patient_id IS NOT NULL THEN 'hospital' ELSE 'incoming' END AS hospital,
	case WHEN coalesce(tpa.sponsor_type, itpa.sponsor_type, 'R')='R' THEN 'R' ELSE 'S' END AS patient_sponsor_type
FROM tests_prescribed tp
	LEFT JOIN test_visit_reports tvr USING (report_id)
	JOIN diagnostics d USING (test_id)
	JOIN diagnostics_departments dd USING (ddept_id)
	LEFT JOIN sample_type st ON (st.sample_type_id = d.sample_type_id)
	LEFT JOIN test_histopathology_results thr ON(thr.prescribed_id = tp.prescribed_id)
	LEFT JOIN histo_impression_master him ON(him.impression_id = thr.impression_id)
	LEFT JOIN test_cytology_results tcr ON(tcr.prescribed_id = tp.prescribed_id)
	LEFT JOIN histo_impression_master cim ON(tcr.impression_id = cim.impression_id)
	LEFT JOIN outsource_sample_details oh ON (oh.prescribed_id = tp.prescribed_id)
	LEFT JOIN diag_outsource_master dom ON (dom.outsource_dest_id = oh.outsource_dest_id)
	LEFT JOIN outhouse_master om ON (om.oh_id = dom.outsource_dest)
	LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id)
	LEFT JOIN tpa_master tpa ON (pr.primary_sponsor_id=tpa.tpa_id)
	LEFT JOIN incoming_sample_registration isr ON (tp.pat_id = isr.incoming_visit_id)
	LEFT JOIN incoming_sample_registration_details isrd ON (isrd.prescribed_id = tp.prescribed_id AND isr.incoming_visit_id = isrd.incoming_visit_id)
	-- these are used to get the sponsor type of the original visit for internal lab patient.
	LEFT JOIN tests_prescribed itp ON (itp.prescribed_id=tp.coll_prescribed_id)
	LEFT JOIN patient_registration ipr ON (ipr.patient_id=itp.pat_id)
	LEFT JOIN tpa_master itpa ON (itpa.tpa_id=ipr.primary_sponsor_id)

	LEFT JOIN incoming_hospitals ih ON (ih.hospital_id = isr.orig_lab_name)
	LEFT JOIN patient_details pd ON (pd.mr_no = COALESCE(ipr.mr_no, pr.mr_no))
WHERE tp.conducted NOT IN ( 'X','U','RBS','RAS','S') AND (tp.report_id = 0 OR tp.report_id IS NULL OR tvr.signed_off = 'N')
AND ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) );

DROP VIEW IF EXISTS diag_schedules_lab_summary_view;
CREATE VIEW diag_schedules_lab_summary_view AS
SELECT tp.mr_no,tp.exp_rep_ready_time, tp.pat_id AS patient_id, tp.prescribed_id, tp.labno, CAST(tp.pres_date as date) as pres_date,tp.common_order_id,
    om.oh_name, tp.conducted,COALESCE(itp.sample_no, tp.sample_no) AS sample_no, isrd.orig_sample_no, tp.pres_doctor,
    CASE WHEN d.conduction_format='C' THEN cim.short_impression
    ELSE him.short_impression END AS short_impression,
    CASE WHEN d.sample_needed = 'n' THEN 'U' ELSE tp.sflag END AS sample_status,
    isr.patient_name AS inc_patient_name, ih.hospital_name AS ih_name,
    CASE WHEN isr.incoming_visit_id IS NOT NULL THEN 't' ELSE pr.visit_type END AS visit_type,
    d.ddept_id, d.test_name, dd.category, tvr.report_date,
    coalesce(pr.center_id, isr.center_id) AS center_id,tvr.report_results_severity_status,
    coalesce(pr.reg_date, isr.date) AS reg_date,d.conduction_format,isr.patient_other_info,
    tp.token_number, st.status AS sample_type_status,
    CASE WHEN d.results_entry_applicable='t' THEN 'Yes' ELSE 'No' END AS results_entry_applicable,
    pr.collection_center_id,tp.priority,COALESCE(pr.reference_docto_id,isr.referring_doctor) AS reference_docto_id, 
    CASE WHEN pr.patient_id IS NOT NULL THEN 'hospital' ELSE 'incoming' END AS hospital, sc.sample_receive_status,
    CASE WHEN coalesce(tpa.sponsor_type, itpa.sponsor_type, 'R')='R' THEN 'R' ELSE 'S' END AS patient_sponsor_type
FROM tests_prescribed tp
    JOIN diagnostics d ON (d.test_id = tp.test_id AND tp.conducted IN('N','P','C','V','RC','RV','RP','MA','TS','CC','CR'))
    JOIN diagnostics_departments dd on(d.ddept_id=dd.ddept_id AND dd.category='DEP_LAB')
    LEFT JOIN sample_type st ON (st.sample_type_id = d.sample_type_id)
    LEFT JOIN test_histopathology_results thr ON(thr.prescribed_id = tp.prescribed_id)
    LEFT JOIN histo_impression_master him ON(him.impression_id = thr.impression_id)
    LEFT JOIN test_cytology_results tcr ON(tcr.prescribed_id = tp.prescribed_id)
    LEFT JOIN histo_impression_master cim ON(tcr.impression_id = cim.impression_id)
    LEFT JOIN outsource_sample_details oh ON (oh.prescribed_id = tp.prescribed_id)
    LEFT JOIN diag_outsource_master dom ON (dom.outsource_dest_id = oh.outsource_dest_id)
    LEFT JOIN outhouse_master om ON (om.oh_id = dom.outsource_dest)
    LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id)
    LEFT JOIN tpa_master tpa ON (pr.primary_sponsor_id=tpa.tpa_id)
    LEFT JOIN incoming_sample_registration isr ON (tp.pat_id = isr.incoming_visit_id)
    LEFT JOIN incoming_sample_registration_details isrd ON (isrd.prescribed_id = tp.prescribed_id AND isr.incoming_visit_id = isrd.incoming_visit_id)
    -- these are used to get the sponsor type of the original visit for internal lab patient.
    LEFT JOIN tests_prescribed itp ON (itp.prescribed_id=tp.coll_prescribed_id)
    LEFT JOIN patient_registration ipr ON (ipr.patient_id=itp.pat_id)
    LEFT JOIN tpa_master itpa ON (itpa.tpa_id=ipr.primary_sponsor_id)

    LEFT JOIN incoming_hospitals ih ON (ih.hospital_id = isr.orig_lab_name)
    LEFT JOIN sample_collection sc ON (sc.sample_collection_id = tp.sample_collection_id)
    LEFT JOIN test_visit_reports tvr ON (tvr.report_id = tp.report_id)
    LEFT JOIN patient_details pd ON (pd.mr_no = COALESCE(ipr.mr_no, pr.mr_no))
WHERE (tp.report_id = 0 OR tp.report_id IS NULL OR tvr.signed_off = 'N') 
AND ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) );

DROP VIEW IF EXISTS diag_schedules_rad_summary_view;
CREATE VIEW diag_schedules_rad_summary_view AS
SELECT tp.mr_no,tp.exp_rep_ready_time, tp.pat_id AS patient_id, tp.prescribed_id, tp.labno, CAST(tp.pres_date as date) as pres_date, tp.common_order_id,
	om.oh_name, tp.conducted,tp.sample_no, tp.pres_doctor,
	CASE WHEN d.conduction_format='C' THEN cim.short_impression
	ELSE him.short_impression END AS short_impression,
	CASE WHEN d.sample_needed = 'n' THEN 'U' ELSE tp.sflag END AS sample_status,
	isr.patient_name AS inc_patient_name, ih.hospital_name AS ih_name,
	CASE WHEN isr.incoming_visit_id IS NOT NULL THEN 't' ELSE pr.visit_type END AS visit_type,
	d.ddept_id, d.test_name, CASE WHEN is_outhouse_test(d.test_id,coalesce(pr.center_id, isr.center_id)::integer) THEN 'O' ELSE 'I'
	END AS house_status, dd.category, tvr.report_date,
	coalesce(pr.center_id, isr.center_id) AS center_id,tvr.report_results_severity_status,
	coalesce(pr.reg_date, isr.date) AS reg_date,d.conduction_format,isr.patient_other_info,
	tp.token_number, st.status AS sample_type_status,
	case WHEN d.results_entry_applicable='t' THEN 'Yes' ELSE 'No' END AS results_entry_applicable,
	pr.collection_center_id,tp.priority,CASE WHEN pr.patient_id IS NOT NULL THEN 'hospital' ELSE 'incoming' END AS hospital,
	case WHEN coalesce(tpa.sponsor_type, itpa.sponsor_type, 'R')='R' THEN 'R' ELSE 'S' END AS patient_sponsor_type
FROM tests_prescribed tp
	LEFT JOIN test_visit_reports tvr USING (report_id)
	JOIN diagnostics d USING (test_id)
	JOIN diagnostics_departments dd on(dd.ddept_id=d.ddept_id AND dd.category='DEP_RAD')
	LEFT JOIN sample_type st ON (st.sample_type_id = d.sample_type_id)
	LEFT JOIN test_histopathology_results thr ON(thr.prescribed_id = tp.prescribed_id)
	LEFT JOIN histo_impression_master him ON(him.impression_id = thr.impression_id)
	LEFT JOIN test_cytology_results tcr ON(tcr.prescribed_id = tp.prescribed_id)
	LEFT JOIN histo_impression_master cim ON(tcr.impression_id = cim.impression_id)
	LEFT JOIN outsource_sample_details oh ON (oh.prescribed_id = tp.prescribed_id)
	LEFT JOIN diag_outsource_master dom ON (dom.outsource_dest_id = oh.outsource_dest_id)
	LEFT JOIN outhouse_master om ON (om.oh_id = dom.outsource_dest)
	LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id)
	LEFT JOIN tpa_master tpa ON (pr.primary_sponsor_id=tpa.tpa_id)
	LEFT JOIN incoming_sample_registration isr ON (tp.pat_id = isr.incoming_visit_id)
	LEFT JOIN incoming_sample_registration_details isrd ON (isrd.prescribed_id = tp.prescribed_id AND isr.incoming_visit_id = isrd.incoming_visit_id)
	-- these are used to get the sponsor type of the original visit for internal lab patient.
	LEFT JOIN tests_prescribed itp ON (itp.prescribed_id=isrd.source_test_prescribed)
	LEFT JOIN patient_registration ipr ON (ipr.patient_id=itp.pat_id)
	LEFT JOIN tpa_master itpa ON (itpa.tpa_id=ipr.primary_sponsor_id)

	LEFT JOIN incoming_hospitals ih ON (ih.hospital_id = isr.orig_lab_name)
	LEFT JOIN patient_details pd ON (pd.mr_no = COALESCE(ipr.mr_no, pr.mr_no))
WHERE  tp.conducted IN('N','P','C','V','RC','RV','NRN','CRN','RP','MA','TS','CC','CR')  AND (tp.report_id = 0 OR tp.report_id IS NULL OR tvr.signed_off = 'N')
AND ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) );

DROP VIEW IF EXISTS doc_all_templates_view CASCADE;
CREATE VIEW doc_all_templates_view AS
SELECT 'doc_hvf_templates' AS doc_format,
	template_id, template_name, title, doc_type, specialized, status, access_rights, dept_name FROM doc_hvf_templates
UNION SELECT 'doc_rich_templates' AS doc_format,
	template_id, template_name, title, doc_type, specialized, status, access_rights, dept_name FROM doc_rich_templates
UNION SELECT 'doc_pdf_form_templates' AS doc_format,
	template_id, template_name, '', doc_type, specialized, status, access_rights, dept_name FROM doc_pdf_form_templates
UNION SELECT 'doc_rtf_templates' AS doc_format,
	template_id, template_name, '', doc_type, specialized, status, access_rights, dept_name FROM doc_rtf_templates
;

DROP VIEW IF EXISTS store_sale_item_totals_view CASCADE;
CREATE VIEW store_sale_item_totals_view AS
SELECT sale_id, sum(tax) AS total_item_tax, sum(amount) AS total_item_amount, sum(disc) AS total_item_disc
FROM store_sales_details
GROUP BY sale_id
;

DROP VIEW IF EXISTS claim_totals_view CASCADE;
CREATE VIEW claim_totals_view AS
SELECT bcl.claim_id,bcl.bill_no,sum(insurance_claim_amt) AS tot_claim_amt,sum(claim_recd_total) AS tot_claim_recd,
sum(bcc.tax_amt) AS tot_claim_tax_amt
FROM bill_claim bcl
JOIN bill_charge_claim bcc on(bcl.bill_no = bcc.bill_no AND bcl.claim_id = bcc.claim_id)
GROUP BY bcl.claim_id,bcl.bill_no;

DROP VIEW IF EXISTS claim_bill_totals_view CASCADE;
CREATE VIEW claim_bill_totals_view AS
SELECT	count(b.bill_no) AS bills_count,
	sum(COALESCE(tot_claim_recd,0)+COALESCE(primary_total_sponsor_receipts,0)+COALESCE(secondary_total_sponsor_receipts,0)) AS bills_claim_recd_amount,
	sum(total_amount) AS bills_total_amount,
	sum(total_tax) AS bills_total_tax,
	sum(total_discount) AS bills_total_discount,
	sum(total_amount+total_discount) AS bills_amount,
	sum(total_receipts) AS bills_total_receipts,
	sum(deposit_set_off) AS bills_deposit_set_off,
	sum(insurance_deduction) AS bills_insurance_deduction,
	sum(tot_claim_amt) AS bills_insurance_claim,
	sum(tot_claim_tax_amt) AS tot_claim_tax_amt,
	sum(total_amount + total_tax + insurance_deduction - total_receipts - total_claim - total_claim_tax - deposit_set_off )
		AS bills_due_amount,
	sum(claim_recd_unalloc_amount) AS bills_claim_recd_unalloc_amount,
	sum(total_amount-COALESCE(approval_amount,0)) AS bills_excess,
	sum(total_claim-COALESCE(approval_amount,0)) AS bills_claim_excess,
	ctv.claim_id
FROM bill b
JOIN claim_totals_view ctv ON (b.bill_no = ctv.bill_no)
WHERE b.status!='X' AND b.is_tpa AND ctv.claim_id IS NOT NULL GROUP BY ctv.claim_id;

DROP VIEW IF EXISTS ratevariation_view;
CREATE VIEW ratevariation_view AS
SELECT
	b.visit_type, cgc.chargegroup_name, b.finalized_date, b.bill_no,
	coalesce(org.org_name,'GENERAL') AS org_name,
	bc.act_rate, bc.orig_rate, bc.act_quantity, bc.discount, bc.amount, bc.paid_amount, bc.username
FROM bill_charge bc
	JOIN bill b USING(bill_no)
	LEFT JOIN patient_registration pod ON (pod.patient_id=b.visit_id)
	JOIN organization_details org ON (org.org_id=pod.org_id)
	JOIN chargegroup_constants cgc ON cgc.chargegroup_id=bc.charge_group
;

DROP VIEW IF EXISTS pharmacy_sales_report_view;
CREATE VIEW pharmacy_sales_report_view AS
SELECT
	m.sale_id, m.bill_no,
	CASE WHEN b.bill_type='C' THEN 'C'
		 ELSE 'P' END
	  AS bill_type,
	CASE WHEN rc.is_credit='Y' and visit_type='r' THEN 'Retail Credit'
		 WHEN rc.is_credit='N' and visit_type='r' THEN 'Retail'
		 WHEN visit_type='o' THEN 'Out Patient'
		 ELSE 'In Patient' END
	  AS patient_type,
	m.sale_date AS date, m.date_time::time AS time, b.visit_type, d.dept_name AS store_name,
	m.type AS saletype, m.username,
	(coalesce(it.total_item_amount,0) - coalesce(m.discount,0) + coalesce(m.round_off,0)) AS final_amount,
	(coalesce(it.total_item_disc,0) + m.discount) AS discount,
	coalesce(it.total_item_tax,0) AS tax
FROM store_sales_main m
	JOIN store_sale_item_totals_view it using(sale_id)
	JOIN bill b ON (m.bill_no = b.bill_no)
	JOIN stores d ON (m.store_id = d.dept_id)
	LEFT JOIN store_retail_customers rc ON rc.customer_id=b.visit_id
;

DROP VIEW IF EXISTS pharmacy_retail_credit_report_view;
CREATE VIEW pharmacy_retail_credit_report_view AS
SELECT m.sale_id, m.bill_no, m.type AS saletype, m.sale_date AS date, rc.customer_name, prs.sponsor_name,
	d.dept_name AS store_name, m.username,
	(coalesce(it.total_item_amount,0) - coalesce(m.discount,0) + coalesce(m.round_off,0)) AS final_amount,
	(coalesce(it.total_item_disc,0) + m.discount) AS discount,
	coalesce(it.total_item_tax,0) AS tax
FROM store_sales_main m
	JOIN store_sale_item_totals_view it using(sale_id)
	JOIN bill b ON (m.bill_no = b.bill_no)
	JOIN stores d ON (m.store_id = d.dept_id)
	LEFT JOIN store_retail_customers rc ON rc.customer_id=b.visit_id
	LEFT JOIN store_retail_sponsors prs ON prs.sponsor_id=rc.sponsor_name
WHERE rc.is_credit='Y'
;

DROP VIEW IF EXISTS pharmacy_stock_level_report_view;
DROP VIEW IF EXISTS store_item_level_stock_view CASCADE;


DROP VIEW IF EXISTS store_invoice_totals_view CASCADE;
CREATE VIEW store_invoice_totals_view AS
SELECT i.supplier_invoice_id,
	sum(g.cost_price*g.billed_qty/g.grn_pkg_size - g.discount-g.scheme_discount)::numeric(18,2) AS grn_amt,
	sum(g.tax) AS grn_tax,
	sum(g.item_ced) AS ced_tax,
	max(gm.grn_date) AS grn_date, textcat_commacat(DISTINCT gm.grn_no) AS grn_nos,
	textcat_commacat(DISTINCT gd.dept_name) AS store,gm.form_8h
FROM store_invoice i
	JOIN store_grn_main gm USING (supplier_invoice_id)
	JOIN store_grn_details g USING (grn_no)
	JOIN stores gd ON (gm.store_id= gd.dept_id)
GROUP BY i.supplier_invoice_id,gm.form_8h
;

--
-- combined view of all referrals: all doctors + all referrals
--

DROP VIEW IF EXISTS all_referrers_view;
CREATE VIEW all_referrers_view AS
SELECT referal_no AS id, referal_name AS referrer, referal_mobileno AS referrer_phone,
	payment_category, payment_eligible, status
FROM referral
UNION
SELECT doctor_id AS id, doctor_name AS referrer, doctor_mobile AS referrer_phone,
	payment_category, payment_eligible, status
FROM doctors;

-------- FUNCTIONS ---------------------
--
-- Generates an ID from a pattern
-- Input: name of the pattern AS in hosp_id_patterns
--
DROP FUNCTION IF EXISTS generate_id(patternId text) CASCADE;

CREATE OR REPLACE FUNCTION generate_id(patternId text) RETURNS text AS $BODY$
DECLARE
	rec RECORD;
	sequence_details RECORD;
	transactional_sequence_rec RECORD;
BEGIN
	SELECT sequence_name, is_transactional_sequence 
	FROM hosp_id_patterns 
	INTO sequence_details 
	WHERE pattern_id = patternId;

	IF (sequence_details.is_transactional_sequence IS FALSE)
	THEN
		SELECT std_prefix || date_prefix ||
			trim(to_char(nextval(sequence_name), num_pattern)) AS id
		INTO rec
		FROM hosp_id_patterns
		WHERE pattern_id = patternId;
	ELSE
		SELECT value FROM transactional_sequence 
		INTO transactional_sequence_rec 
		WHERE sequence_name=sequence_details.sequence_name FOR UPDATE;
		
		SELECT std_prefix || date_prefix ||
				trim(to_char(transactional_sequence_rec.value, num_pattern)) AS id
		INTO rec
		FROM hosp_id_patterns
		WHERE pattern_id = patternId;

		UPDATE transactional_sequence 
		SET value = value+1 
		WHERE sequence_name = sequence_details.sequence_name;
	END IF;
	return rec.id;
END;
$BODY$ LANGUAGE 'plpgsql';


--
-- Similar to above function, but takes in the date and number AS parameters,
-- useful WHEN generating an ID for a previous date.
--
DROP FUNCTION IF EXISTS generate_id(patternId text, curdt timestamp, num integer) CASCADE;
CREATE OR REPLACE FUNCTION generate_id(patternId text, curdt timestamp, num integer) RETURNS text AS $BODY$
DECLARE
	rec RECORD;
BEGIN
	SELECT std_prefix ||
		coalesce(to_char(curdt, date_prefix_pattern),'') ||
		trim(to_char(num, num_pattern)) AS id
	INTO rec
	FROM hosp_id_patterns
	WHERE pattern_id = patternId;

	return rec.id;
END;
$BODY$ LANGUAGE 'plpgsql';

-------- FUNCTIONS ---------------------
--
-- Outputs the recently generated ID from a pattern
-- Input: name of the pattern as in hosp_id_patterns
--
DROP FUNCTION IF EXISTS get_generated_id(patternId text) CASCADE;
CREATE OR REPLACE FUNCTION get_generated_id(patternId text) RETURNS text AS $BODY$
DECLARE
	rec RECORD;
	sequenceName TEXT;
	lastGeneratedValue INTEGER;
BEGIN
	SELECT std_prefix || date_prefix ||
		trim(to_char(currval(sequence_name), num_pattern)) as id
	INTO rec
	FROM hosp_id_patterns
	WHERE pattern_id = patternId;
	return rec.id;
	EXCEPTION WHEN OTHERS THEN
	SELECT sequence_name
             FROM hosp_id_patterns
               WHERE pattern_id = patternId INTO sequenceName;

    EXECUTE format('SELECT last_value from %s',quote_ident(sequenceName))
               INTO lastGeneratedValue;

    SELECT std_prefix || date_prefix ||
               trim(to_char(lastGeneratedValue,num_pattern)) as id
       INTO rec
       FROM hosp_id_patterns
       WHERE pattern_id = patternId;
	return rec.id;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP FUNCTION IF EXISTS generate_next_sample_id(sampleTypeId integer, center integer) CASCADE;
CREATE OR REPLACE FUNCTION generate_next_sample_id(sampleTypeId integer, center integer) RETURNS text AS $BODY$
DECLARE
	rec RECORD;
	centerID integer;
BEGIN
	IF EXISTS (SELECT sample_prefix||start_number
		FROM sample_type_number_prefs
		WHERE sample_type_id = sampleTypeId AND center_id = center) THEN

		centerID := center;
	ELSE
		centerID := 0;

	END IF;

	UPDATE sample_type_number_prefs SET start_number = start_number+1
	WHERE sample_type_id = sampleTypeId AND center_id = centerID;

	SELECT COALESCE(sample_prefix,'') || COALESCE(date_prefix,'') ||
		trim(to_char(start_number, num_pattern)) AS id
	INTO rec
	FROM sample_type_number_prefs
	WHERE sample_type_id = sampleTypeId AND center_id = centerID;

	return rec.id;
END;
$BODY$ LANGUAGE 'plpgsql';



DROP FUNCTION IF EXISTS generate_next_sample_id(sampleTypeId integer, curdt timestamp, num integer) CASCADE;
CREATE OR REPLACE FUNCTION generate_next_sample_id(sampleTypeId integer, curdt timestamp, num integer) RETURNS text AS $BODY$
DECLARE
	rec RECORD;
BEGIN
	SELECT COALESCE(sample_prefix,'') ||
		coalesce(to_char(curdt, date_prefix_pattern),'') ||
		trim(to_char(num, num_pattern)) AS id
	INTO rec
	FROM sample_type
	WHERE sample_type_id = sampleTypeId;

	UPDATE sample_type SET start_number = num+1
	WHERE sample_type_id = sampleTypeId AND start_number<=num;

	return rec.id;
END;
$BODY$ LANGUAGE 'plpgsql';

--
-- Convenience function (used in master upload scripts) to generate an
-- id based ON existing data in table (equivalent to getNewIncrId)
--
DROP FUNCTION IF EXISTS generate_max_id(tbl text, col text, prefix text, len integer);
CREATE OR REPLACE FUNCTION generate_max_id(tbl text, col text, prefix text, len integer) RETURNS TEXT
AS $BODY$
DECLARE
	format text;
	maxid text;
	maxval text;
	newval integer;
BEGIN
	format := 'FM' || repeat('0',len);								-- 0000

	EXECUTE 'SELECT max(' || col || ') FROM ' || tbl ||
		' WHERE strpos(' || col || ', $$_$$) = 0' INTO maxid;		-- DOC0123

	IF maxid IS NULL THEN
		newval := 1;
	ELSE
		maxval := substring(maxid, length(prefix)+1);				-- 0123
		newval := to_number(maxval, '9999999999') + 1;				-- 124
	END IF;
	RETURN prefix || to_char(newval, format);						-- DOC0124
END;
$BODY$ LANGUAGE 'plpgsql';

--
-- calculates the age in days / months / years for the actual_date_of_birth or
-- estimated_date_of_birth with current_date --
--
DROP FUNCTION IF EXISTS get_patient_age(actual_date_of_birth date, estimated_date_of_birth date) CASCADE;
CREATE OR REPLACE FUNCTION get_patient_age(actual_date_of_birth date, estimated_date_of_birth date)
	RETURNS integer AS $BODY$
DECLARE
	age integer;
	days double precision;
BEGIN
	IF actual_date_of_birth is not null
		THEN days := (current_date - actual_date_of_birth)::double precision;
		ELSE days := (current_date - estimated_date_of_birth)::double precision;
	END IF;
	IF days < 31 THEN
		age := floor(days);
	ELSIF days < 730 THEN
    	age := floor(days / 30.43);
	ELSE
    	age := floor(days / 365.25);
	END IF;

	RETURN age;
END;
$BODY$ LANGUAGE 'plpgsql';


--
-- calculates the age in following text format
-- if age is greater than 5 years show  only years - 5 years 3 months ==> 5Y
-- if age is greater than 1 year  show  years and months - 2 years 3 months 5 days ==> 2Y+3M
-- if age is greater than 1 month show  only months - 3 months 5 days ==> 3M
-- other wise show days - 5 days => 5D
--

DROP FUNCTION IF EXISTS get_age_text(timestamp) CASCADE;
CREATE OR REPLACE FUNCTION get_age_text(timestamp) RETURNS text AS $$
DECLARE
	age interval;
	age_text text;
BEGIN
	age := AGE($1);
	IF (age >= interval '5 year') THEN 
		age_text := to_char(age, 'FMYYYY"Y"');
	ELSEIF (age >= interval '1 year') THEN 
		age_text := to_char(age, 'FMYYYY"Y"+FMMM"M"');
	ELSEIF (age >= interval '1 month') THEN 
		age_text := to_char(age, 'FMMM"M"');
	ELSE 
		age_text := to_char(age, 'FMDD"D"');
	END IF;
	RETURN age_text;
END
$$ LANGUAGE plpgsql;


--
-- returns the ageIn AS D, M and Y (if age in days, months and Years) for the
-- actual_date_of_birth or estimated_date_of_birth with current_date --
--
CREATE OR REPLACE FUNCTION get_patient_age_in(actual_date_of_birth date, estimated_date_of_birth date)
  RETURNS character varying AS $BODY$

DECLARE
	ageIn character varying;
	days double precision;
BEGIN
	IF actual_date_of_birth is not null
		THEN days := round((current_date - actual_date_of_birth)::double precision);
		ELSE days := round((current_date - estimated_date_of_birth)::double precision);
	END IF;
	IF days < 31 THEN
		ageIn := 'D';
	ELSIF days < 730 THEN
    	ageIn := 'M';
	ELSE
    	ageIn := 'Y';
	END IF;

	RETURN ageIn;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP FUNCTION IF EXISTS get_patient_age_in_months_as_of(actual_date_of_birth date, estimated_date_of_birth date, as_of_date date) CASCADE;
CREATE OR REPLACE FUNCTION get_patient_age_in_months_as_of(actual_date_of_birth date, estimated_date_of_birth date, as_of_date date)
	RETURNS integer AS $BODY$
DECLARE
	age integer;
	days double precision;
BEGIN
	IF actual_date_of_birth is not null
		THEN days := (as_of_date - actual_date_of_birth)::double precision;
		ELSE days := (as_of_date - estimated_date_of_birth)::double precision;
	END IF;

    	age := floor(days / 30.43);

	RETURN age;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP FUNCTION IF EXISTS get_patient_age_in_months_with_precision_as_of(actual_date_of_birth date, estimated_date_of_birth date, as_of_date date) CASCADE;
CREATE OR REPLACE FUNCTION get_patient_age_in_months_with_precision_as_of(actual_date_of_birth date, estimated_date_of_birth date, as_of_date date)
	RETURNS numeric AS $BODY$
DECLARE
	age numeric;
	days numeric;
BEGIN
	IF actual_date_of_birth is not null
		THEN days := (as_of_date - actual_date_of_birth)::double precision;
		ELSE days := (as_of_date - estimated_date_of_birth)::double precision;
	END IF;

    	age := ROUND(days/30.43/25,2)*25;

	RETURN age;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP FUNCTION IF EXISTS get_patient_age(actual_date_of_birth date, estimated_date_of_birth date, incoming_sample_date_of_birth date,isr_patient_age integer) CASCADE;
CREATE OR REPLACE FUNCTION get_patient_age(actual_date_of_birth date, estimated_date_of_birth date,incoming_sample_date_of_birth date, isr_patient_age integer)
	RETURNS integer AS $BODY$
DECLARE
	age integer;
	days double precision;
BEGIN
	IF actual_date_of_birth is not null
		THEN days := (current_date - actual_date_of_birth)::double precision;
    ELSIF estimated_date_of_birth is not null
        THEN days := (current_date - estimated_date_of_birth)::double precision;
	ELSIF incoming_sample_date_of_birth is not null
		THEN days := (current_date - incoming_sample_date_of_birth)::double precision;
	ELSE
		RETURN isr_patient_age;
	END IF;
	IF days < 31 THEN
		age := floor(days);
	ELSIF days < 730 THEN
    	age := floor(days / 30.43);
	ELSE
    	age := floor(days / 365.25);
	END IF;

	RETURN age;
END;
$BODY$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION get_patient_age_in(actual_date_of_birth date, estimated_date_of_birth date, incoming_sample_date_of_birth date, age_unit character varying)
  RETURNS character varying AS $BODY$

DECLARE
	ageIn character varying;
	days double precision;
BEGIN
	IF actual_date_of_birth is not null
		THEN days := round((current_date - actual_date_of_birth)::double precision);
	ELSIF estimated_date_of_birth is not null
		THEN  days := round((current_date - estimated_date_of_birth)::double precision);
    ELSIF incoming_sample_date_of_birth is not null
    	THEN days := round(current_date - incoming_sample_date_of_birth)::double precision;
    ELSE
    	RETURN age_unit;
	END IF;
	IF days < 31 THEN
		ageIn := 'D';
	ELSIF days < 730 THEN
    	ageIn := 'M';
	ELSE
    	ageIn := 'Y';
	END IF;

	RETURN ageIn;
END;
$BODY$ LANGUAGE 'plpgsql';

--
-- returns the age group of the patient AS per
-- 0 - 30 days
-- 31 - 1 yrs
-- 0 - 14 yrs
-- 15 - 80 yrs
-- > 80 yrs
-- actual_date_of_birth or estimated_date_of_birth with current_date --
--
CREATE OR REPLACE FUNCTION get_patient_age_group(actual_date_of_birth date, estimated_date_of_birth date)
  RETURNS character varying AS $BODY$

DECLARE
	ageGroup character varying;
	days double precision;
BEGIN
	IF actual_date_of_birth is not null
		THEN days := round((current_date - actual_date_of_birth)::double precision);
		ELSE days := round((current_date - estimated_date_of_birth)::double precision);
	END IF;
	IF days < 31 THEN
		ageGroup := '0 - 30 Days - Neo Natal';
	ELSE
    	-- ageIn := 'Y';
    IF days < (1*365) THEN
    	ageGroup := '31 Days - 1 Year: Infant';
	ELSIF days < (14*365) THEN
		ageGroup := '1 - 14 Yrs - Paediatric';
	ELSIF days > (80*365) THEN
		ageGroup := '80+ Yrs - Geriatric';
	ELSE
		ageGroup := '15 - 80 Yrs - General';
	END IF;
	END IF;

	RETURN ageGroup;
END;
$BODY$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION bill_item_account(item_id character varying, chargehead_id character varying)
	RETURNS character varying AS $BODY$
DECLARE
	account character varying;
BEGIN
	SELECT bah.account_head_name INTO account
	  FROM bill_charge_item_account bcia
	  JOIN bill_account_heads bah USING(account_head_id)
	  WHERE bcia.item_id=item_id AND bcia.chargehead_id=chargehead_id;

	IF NOT FOUND THEN
		SELECT bah.account_head_name INTO account FROM chargehead_constants cc
		  JOIN bill_account_heads bah USING(account_head_id)
		  WHERE cc.chargehead_id=chargehead_id;
	END IF;
	RETURN account;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP FUNCTION IF EXISTS deactivate_op_diag_patients();
DROP FUNCTION IF EXISTS close_no_charge_bills();

--
-- utility to grant read-only rights to an entire schema
--
CREATE OR REPLACE FUNCTION grant_readonly(role text, schema text) RETURNS INTEGER AS $BODY$
DECLARE
	obj record;
	num integer;
BEGIN
	num:=0;
	FOR obj IN SELECT relname FROM pg_class c JOIN pg_namespace ns ON (c.relnamespace = ns.oid)
			WHERE relkind IN ('r','v','S') AND nspname = schema
	LOOP
		EXECUTE 'GRANT SELECT ON ' || schema ||'.'|| obj.relname || ' TO ' || role;
		num := num + 1;
	END LOOP;
	RETURN num;
END;
$BODY$ LANGUAGE plpgsql;

--
-- utility to grant read-only rights to a pattern of tables
--
CREATE OR REPLACE FUNCTION grant_readonly_pattern(role text, schema text, pattern text)
	RETURNS INTEGER AS $BODY$
DECLARE
	obj record;
	num integer;
BEGIN
	num:=0;
	FOR obj IN SELECT relname FROM pg_class c JOIN pg_namespace ns ON (c.relnamespace = ns.oid)
			WHERE relkind IN ('r','v','S') AND nspname = schema
				AND relname like pattern
	LOOP
		EXECUTE 'GRANT SELECT ON ' || schema ||'.'|| obj.relname || ' TO ' || role;
		num := num + 1;
	END LOOP;
	RETURN num;
END;
$BODY$ LANGUAGE plpgsql;

-- todo: obsolete check
CREATE OR REPLACE FUNCTION getdoctornames(docids character varying) RETURNS character varying
    AS $$
DECLARE
docNames character varying;
id character varying;
str character varying;
buff character varying;
BEGIN
   docNames:='';
   buff:=docIds;
   WHILE INSTR(buff,docNames)>0 LOOP
     id:=SUBSTR(buff,1,(INSTR(buff,'','')-1));
     buff:=SUBSTR(buff,(INSTR(buff,'','')+1));
     select doctor_name INTO str from doctors where doctor_id=id;
     docNames:=docNames||str;
     docNames:=docNames||'','' ;
   END LOOP;
id:=SUBSTR(buff,(INSTR(buff,'','')+1));
select doctor_name INTO str from doctors where doctor_id=id;
docNames:=docNames||str;
return docNames;
END $$
    LANGUAGE plpgsql;


--
-- Round to nearest 5,10 etc.
--
CREATE OR REPLACE FUNCTION doroundvarying(unitcharge numeric,varianceby numeric,nearstround numeric)
  RETURNS numeric AS $$
   DECLARE
    charge numeric := 0;
   BEGIN

      charge := unitcharge + (unitcharge*varianceby)/100;

     IF nearstround = 10 THEN
       charge := round(charge,-1);
       -- RAISE NOTICE 'NEARST ROUND TO 10 %',charge ;
     ELSE IF nearstround = 5 THEN
     	charge := round(round(charge*2,-1)/2);
     ELSE IF nearstround = 25 THEN
        charge := round(round(charge*4,-2)/4);
     ELSE IF nearstround = 50 THEN
        charge := round(round(charge*2,-2)/2);
     ELSE IF nearstround = 100 THEN
        charge := round(charge,-2);
     ELSE IF nearstround = 1 THEN
        charge := round(charge);
     ELSE
     	charge := charge;
     END IF;
     END IF;
     END IF;
     END IF;
     END IF;
     END IF;

     RETURN charge;
  END;
$$LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION commonmedicine_trigger()
  RETURNS trigger AS $BODY$
  BEGIN
    UPDATE commonmedicine_timestamp set medicine_timestamp=medicine_timestamp+1;
    RETURN NEW;
  END;
$BODY$ LANGUAGE 'plpgsql';

--
-- Useful function: remove_dups -- removes all duplicate rows from a table
-- Example: select remove_dups('diagnostic_charges') will remove all duplicate
-- rows from diagnostic_charges table. A duplicate row is where ALL column values match.
--
CREATE OR REPLACE FUNCTION remove_dups (tbl TEXT) RETURNS VOID AS $$
BEGIN
 EXECUTE 'CREATE TEMP TABLE tmp_for_dups AS SELECT DISTINCT * FROM ' || quote_ident(tbl);
 EXECUTE 'DELETE FROM ' || quote_ident(tbl);
 EXECUTE 'INSERT INTO ' || quote_ident(tbl) || ' (SELECT * FROM tmp_for_dups)';
 DROP TABLE tmp_for_dups;
END;
$$ LANGUAGE plpgsql;

--
-- remove_dups_on will remove duplicates found ON one or more columns.
-- Eg, if you want to remove tests with duplicate names:
--   SELECT remove_dups_on('diagnostics', 'test_name');
-- If the distinct has multiple columns, eg, medicine_id+batch_no, then, specify
-- all the unique keys in the on_column field:
--   SELECT remove_dups_on('store_stock_details', 'medicine_id,batch_no');
--
CREATE OR REPLACE FUNCTION remove_dups_on (tbl TEXT, on_column TEXT) RETURNS VOID AS $$
BEGIN
 EXECUTE 'CREATE TEMP TABLE tmp_for_dups AS SELECT DISTINCT ON('
	|| on_column || ') * FROM ' || quote_ident(tbl);
 EXECUTE 'DELETE FROM ' || quote_ident(tbl);
 EXECUTE 'INSERT INTO ' || quote_ident(tbl) || ' (SELECT * FROM tmp_for_dups)';
 DROP TABLE tmp_for_dups;
END;
$$ LANGUAGE plpgsql;


--
-- Given a string, like Yes, YES, No, NO, N etc, return Y/N. Useful for master uploads.
--
CREATE OR REPLACE FUNCTION clean_yn(to_clean text, default_val text) RETURNS text AS $BODY$
BEGIN
	IF to_clean IS NULL OR to_clean = '' THEN
		RETURN default_val;
	END IF;
	RETURN upper(substring(trim(to_clean),1,1));
END;
$BODY$ LANGUAGE 'plpgsql';

--
-- Pharmacy stock checkpoint function
--
--
DROP FUNCTION IF EXISTS store_stock_checkpoint(chkptname character varying,remark character varying) CASCADE;
CREATE OR REPLACE FUNCTION store_stock_checkpoint(chkptname character varying,remark character varying)
  RETURNS void AS
$BODY$DECLARE
seqname character varying;
name character varying;
 BEGIN
seqname = 'Pharmacy_checkpoint_sequence';
name = chkptname || '(' ||localtimestamp(0)|| ')';
INSERT INTO store_checkpoint_main (CHECKPOINT_ID,CHECKPOINT_NAME,USER_NAME,CHECKPOINT_DATE,REMARKS)
       VALUES((select nextval(seqname)),name,'auto_update',localtimestamp(0),remark);
INSERT INTO store_checkpoint_details (checkpoint_id,store_id,medicine_id,batch_no,qty,mrp,cp)
  select (select currval(seqname)),dept_id,ssd.medicine_id,sibd.batch_no,qty,round(mrp/stock_pkg_size,2) AS mrp,round(package_cp/stock_pkg_size,2) AS package_cp
  from store_stock_details ssd
  JOIN store_item_batch_details sibd USING(item_batch_id) ;
RETURN ;
END;$BODY$
  LANGUAGE 'plpgsql' VOLATILE
  COST 100;
ALTER FUNCTION store_stock_checkpoint(character varying,character varying) OWNER TO postgres;

-- Triggers for detecting WHEN the list of available medicines in stock changes.
-- We could check more rigorously that the medicine list really changes due
-- and update the timestamp even WHEN not required, rather than complicate the
-- logic and make it more error prone.
--
CREATE OR REPLACE FUNCTION pharmacy_stock_change_timestamp() RETURNS TRIGGER AS $BODY$
DECLARE
	changed boolean;
BEGIN
	changed := false;
	IF (TG_OP = 'INSERT') THEN
		-- New stock: the mecidine list can change. Not optimized for the
		-- CASE where same medicine, different batch already exists with non-zero stock.
		changed := true;
	ELSIF (TG_OP = 'UPDATE') THEN
		-- Update: if the stock availability (qty being > 0) changes, the list can change
		-- Or, if the expiry date is changed, the item may need to be included/excluded
		-- (Not optimized for same medicine other batches already exists)
		IF ((NEW.qty > 0) != (OLD.qty > 0)) THEN
			changed := true;
		ELSIF (NEW.asset_approved != OLD.asset_approved) THEN
			changed := true;
		END IF;
	END IF;

	IF changed THEN
		UPDATE store_main_stock_timestamp SET medicine_timestamp = medicine_timestamp+1;
		UPDATE stores SET stock_timestamp = stock_timestamp+1 WHERE dept_id = NEW.dept_id;
	END IF;
	RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

--DROP TRIGGER IF EXISTS pharmacy_stock_change_timestamp ON store_stock_details;
--CREATE TRIGGER pharmacy_stock_change_timestamp
--AFTER INSERT OR UPDATE ON store_stock_details
--	FOR EACH ROW EXECUTE PROCEDURE pharmacy_stock_change_timestamp();


--
-- Trigger ON medicine details: update timestamp WHEN medicine details changes
--
CREATE OR REPLACE FUNCTION pharmacy_medicine_change_timestamp() RETURNS TRIGGER AS $BODY$
DECLARE
	changed boolean;
BEGIN
	changed := false;
	IF (TG_OP = 'INSERT') THEN
		-- New item: the mecidine list has definitely changed
		changed := true;
	ELSIF (TG_OP = 'UPDATE') THEN
		-- The medicine name, status, and item_barcode_id are the only change we need to track.
		IF (OLD.medicine_name != NEW.medicine_name) THEN
			changed:= true;
		END IF;
		IF (OLD.status != NEW.status) THEN
			changed:= true;
		END IF;
		IF (OLD.item_barcode_id != NEW.item_barcode_id) THEN
			changed:= true;
		END IF;
		IF (OLD.max_cost_price != NEW.max_cost_price) THEN
			changed:= true;
		END IF;
	END IF;

	IF changed THEN
		-- update the stock timestamp
		UPDATE store_main_stock_timestamp SET medicine_timestamp = medicine_timestamp+1;
		-- also update the item master timestamp
		UPDATE store_item_timestamp SET item_timestamp = item_timestamp + 1;
	END IF;
	RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS pharmacy_medicine_change_timestamp ON store_item_details;
CREATE TRIGGER pharmacy_medicine_change_timestamp
AFTER INSERT OR UPDATE ON store_item_details
	FOR EACH ROW EXECUTE PROCEDURE pharmacy_medicine_change_timestamp();


--
-- Trigger ON item code: update timestamp WHEN item code changes
--
CREATE OR REPLACE FUNCTION pharmacy_item_code_change_timestamp() RETURNS TRIGGER AS $BODY$
DECLARE
	changed boolean;
BEGIN
	changed := false;
	IF (TG_OP = 'INSERT') THEN
		-- New item code: the item code list has definitely changed
		changed := true;
	ELSIF (TG_OP = 'UPDATE') THEN
		-- The item code change we need to track.
		IF (OLD.item_code != NEW.item_code) THEN
			changed:= true;
		END IF;
	END IF;

	IF changed THEN
		-- update the item master timestamp
		UPDATE store_item_timestamp SET item_timestamp = item_timestamp + 1;
	END IF;
	RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS pharmacy_item_code_change_timestamp ON store_item_codes;
CREATE TRIGGER pharmacy_item_code_change_timestamp
AFTER INSERT OR UPDATE ON store_item_codes
	FOR EACH ROW EXECUTE PROCEDURE pharmacy_item_code_change_timestamp();



--
-- Convenience view for all details of a patient's visit by joining respective tables.
-- Note that retail and incoming customers are not included here. You will need to UNION
-- those tables and required fields if you need those also.
--
DROP VIEW IF EXISTS patient_visit_details_ext_view CASCADE;
CREATE OR REPLACE VIEW patient_visit_details_ext_view AS
SELECT
	pr.visit_type, pr.op_type, otn.op_type_name,pr.mr_no, pr.patient_id, pr.reg_date, pr.reg_date AS start_date, pr.reg_time,
	pr.revisit, pr.status, vtn.visit_type_name, s.salutation, pd.patient_name, pd.last_name,
	get_patient_full_name(s.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS patient_full_name,
	pd.patient_gender, get_patient_age(pd.dateofbirth, pd.expected_dob) AS patient_age,
	get_patient_age_in(pd.dateofbirth, pd.expected_dob) AS patient_age_in,pd.vip_status,
	pd.patient_area AS area, pr.mr_no AS cust_id, pr.ready_to_discharge,
	bn.bed_id, bn.bed_type, bn.bed_name, wn.ward_name,wn.ward_no,
	wnr.ward_name AS reg_ward_name, pr.bed_type AS bill_bed_type,
	(case WHEN pr.visit_type ='i' then
		(case WHEN (select activation_status from modules_activated where module_id='mod_ipservices')='Y' then
				(case WHEN coalesce(bn.bed_name,'')='' THEN '(Not allocated)'
					else wn.ward_name||'/'||bn.bed_name end)
		else pr.ward_name||'/'||pr.bed_type end)
	else '' end) AS ward_and_bed,
	pr.discharge_date, dtm.discharge_type, pr.discharge_time, pr.discharge_doc_id, pr.discharge_format,
	discharge_finalized_date, discharge_finalized_time, discharge_finalized_user,
	(case WHEN  coalesce(pr.discharge_finalized_user,'')!='' THEN 'F'
		when coalesce(pr.discharge_doc_id, 0) != '0' THEN 'O'
		else 'ND' end) AS discharge_doc_status,
	dep.dept_id, dep.dept_name, pr.doctor AS doctor_id, doc.doctor_name,
	coalesce(rdoc.doctor_name, ref.referal_name) AS referer,
	od.org_id, od.org_name,pr.unit_id,dep.dept_name||'-'||dum.unit_name AS unit_name,
	CASE WHEN ibn.bed_name IS NOT NULL THEN 'Y' ELSE 'N' END AS is_icu_type,tpa_name,ibd.bed_state,bt.is_icu,
	pr.center_id,wn.center_id AS admitted_ward_center_id,wnr.center_id AS reg_ward_center_id
FROM patient_registration pr
	LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
	JOIN visit_type_names vtn ON (vtn.visit_type = pr.visit_type)
	LEFT JOIN op_type_names otn ON (otn.op_type = pr.op_type)
	LEFT JOIN salutation_master s ON (s.salutation_id = pd.salutation)
	LEFT JOIN department dep ON (dep.dept_id = pr.dept_name)
	LEFT JOIN doctors doc ON (doc.doctor_id = pr.doctor)
	LEFT JOIN doctors rdoc ON (rdoc.doctor_id = pr.reference_docto_id )
	LEFT JOIN referral ref ON (ref.referal_no = pr.reference_docto_id )
	LEFT JOIN admission adm ON (adm.patient_id = pr.patient_id)
	LEFT JOIN ip_bed_details ibd ON(ibd.patient_id = pr.patient_id AND ibd.status IN ('C','A'))
	LEFT JOIN bed_names bn ON (bn.bed_id = adm.bed_id)
	LEFT JOIN bed_types bt ON(bn.bed_type = bt.bed_type_name)
	LEFT JOIN ward_names wn ON (wn.ward_no = bn.ward_no)
	LEFT JOIN ward_names wnr ON (wnr.ward_no = pr.ward_id)
	LEFT JOIN organization_details od ON (od.org_id = pr.org_id)
	LEFT JOIN dept_unit_master dum ON (dum.unit_id = pr.unit_id)
	LEFT JOIN discharge_type_master dtm ON(dtm.discharge_type_id = pr.discharge_type_id)
	LEFT JOIN (SELECT * FROM bed_names  WHERE bed_type IN (SELECT intensive_bed_type FROM icu_bed_charges ))
	AS ibn ON (ibn.bed_id = bn.bed_id AND  ibn.occupancy = 'Y' AND ibn.status = 'A' )
	LEFT JOIN tpa_master tp ON pr.primary_sponsor_id= tp.tpa_id
WHERE ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )
;

-- View for IP patients.
-- For a patient to be discharged WHEN a bed is allocated this view depicts the patient's all bills statuses
-- and payment amounts are ok or not for the patient discharge.

DROP VIEW IF EXISTS adt_bill_and_discharge_status_view;
CREATE OR REPLACE view adt_bill_and_discharge_status_view AS
SELECT
	visit_id, bool_or(credit_exists) AS credit_bill_exists, bool_and(bill_status_ok) AS bill_status_ok,
	bool_and(payment_ok) AS payment_ok, SUM(total_amount) AS total_amount,
	SUM(total_receipts) AS total_receipts,
	SUM(approval_amount) AS approval_amount
FROM
	(SELECT bill_no, b.visit_id, bill_type, b.status,
		(CASE WHEN (b.bill_type='C') THEN true ELSE false END) AS credit_exists,
		(CASE
			WHEN (b.bill_type='C' AND b.status IN ('F','C') AND b.discharge_status = 'Y') THEN true
			WHEN (b.is_tpa AND b.status IN ('F','C')) THEN true
			WHEN (b.bill_type='P' AND b.status IN ('F','C')) THEN true
			ELSE false END ) AS bill_status_ok,
		total_amount,
		total_receipts,
		(CASE WHEN b.is_tpa THEN coalesce(approval_amount,0.00) ELSE 0.00 END) AS approval_amount,
		(CASE
			WHEN b.is_tpa AND b.payment_status = 'P' THEN true
			WHEN total_amount <= total_receipts THEN true
			WHEN b.bill_type='P' THEN true
		ELSE false END) AS payment_ok
	FROM bill b
		JOIN patient_registration pr ON (pr.patient_id = b.visit_id )
		JOIN patient_details pd ON (pd.mr_no = pr.mr_no AND patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no))
	WHERE b.status !='X' AND pr.visit_type='i' AND pr.status = 'A'
) AS adtbills
GROUP BY adtbills.visit_id ;

-- View for OP patients -- In order to close visit, bill status should be F or S or C
DROP VIEW IF EXISTS op_bill_and_discharge_status_view;
CREATE OR REPLACE view op_bill_and_discharge_status_view AS
SELECT
	visit_id, bool_or(credit_exists) AS credit_bill_exists, bool_and(bill_status_ok) AS bill_status_ok,
	bool_and(payment_ok) AS payment_ok, SUM(total_amount) AS total_amount,
	SUM(total_receipts) AS total_receipts,
	SUM(approval_amount) AS approval_amount
FROM
	(SELECT bill_no, b.visit_id, bill_type, b.status,
		(CASE WHEN (b.bill_type='C') THEN true ELSE false END) AS credit_exists,
		(CASE
			WHEN (b.bill_type='C' AND b.status IN ('F','C')) THEN true
			WHEN (b.is_tpa AND b.status IN ('F','C')) THEN true
	 		WHEN (b.bill_type='P' AND b.status IN ('F','C')) THEN true
			ELSE false END) AS bill_status_ok,
		total_amount,
		total_receipts,
		(CASE WHEN b.is_tpa THEN coalesce(approval_amount,0.00) ELSE 0.00 END) AS approval_amount,
		(CASE
			WHEN b.is_tpa AND b.payment_status = 'P' THEN true
			WHEN total_amount <= total_receipts THEN true
			WHEN b.bill_type='P' THEN true
		ELSE false END) AS payment_ok
	FROM bill b
		JOIN patient_registration pr ON (pr.patient_id = b.visit_id )
		JOIN patient_details pd ON (pd.mr_no = pr.mr_no AND patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no))
	WHERE b.status !='X' AND pr.visit_type = 'o' AND pr.status = 'A'
) AS adtbills
GROUP BY adtbills.visit_id ;



DROP VIEW IF EXISTS all_payments_due_view CASCADE;
CREATE OR REPLACE VIEW all_payments_due_view as
	SELECT bc.bill_no, voucher_no, pd.amount, pd.payee_name, pd.payment_type, doctor_name AS name,
		pd.posted_date, pd.payment_id, pd.account_group, null AS account_head_name,
		hcm.center_code, dept.cost_center_code AS dept_center_code, b.visit_type, pr.op_type,
		pd.expense_center_id, pd.mod_time,
		sm.salutation, pdet.patient_name, pdet.middle_name, pdet.last_name,
		sm.salutation || ' ' || patient_name
        || CASE WHEN coalesce(middle_name, '') = '' THEN '' ELSE (' ' || middle_name) end
        || CASE WHEN coalesce(last_name, '') = '' THEN '' ELSE (' ' || last_name) end
		as full_name, chargehead_name, bc.charge_head
	FROM payments_details pd
		JOIN bill_charge bc using(charge_id)
		JOIN bill b using (bill_no)
		JOIN chargehead_constants c ON (bc.charge_head = c.chargehead_id)
		LEFT JOIN patient_registration pr ON (b.visit_id=pr.patient_id)
		LEFT JOIN patient_details pdet ON (pr.mr_no=pdet.mr_no)
		LEFT JOIN salutation_master sm ON (sm.salutation_id=pdet.salutation)
		LEFT JOIN department dept ON (pr.dept_name=dept.dept_id)
		JOIN hospital_center_master hcm ON (hcm.center_id=pd.expense_center_id)
		JOIN doctors d on pd.payee_name=d.doctor_id
	WHERE pd.payment_type in ('D', 'R', 'P') 
	AND (patient_confidentiality_check(COALESCE(pdet.patient_group, 0), pdet.mr_no))
	UNION
	SELECT bc.bill_no, voucher_no, pd.amount, pd.payee_name, pd.payment_type, oh_name AS name,
		pd.posted_date, pd.payment_id, pd.account_group, null AS account_head_name,
		hcm.center_code, dept.cost_center_code AS dept_center_code, b.visit_type, pr.op_type,
		pd.expense_center_id, pd.mod_time,
		sm.salutation, pdet.patient_name, pdet.middle_name, pdet.last_name,
		sm.salutation || ' ' || patient_name
        || CASE WHEN coalesce(middle_name, '') = '' THEN '' ELSE (' ' || middle_name) end
        || CASE WHEN coalesce(last_name, '') = '' THEN '' ELSE (' ' || last_name) end
		as full_name, chargehead_name, bc.charge_head
	FROM payments_details pd
		JOIN bill_charge bc using(charge_id)
		JOIN bill b using (bill_no)
		JOIN chargehead_constants c ON (bc.charge_head = c.chargehead_id)
		LEFT JOIN patient_registration pr ON (b.visit_id=pr.patient_id)
		LEFT JOIN patient_details pdet ON (pr.mr_no=pdet.mr_no)
		LEFT JOIN salutation_master sm ON (sm.salutation_id=pdet.salutation)
		LEFT JOIN department dept ON (pr.dept_name=dept.dept_id)
		JOIN hospital_center_master hcm ON (hcm.center_id=pd.expense_center_id)
		JOIN outhouse_master ohm ON pd.payee_name=ohm.oh_id
	WHERE pd.payment_type='O' 
	AND (patient_confidentiality_check(COALESCE(pdet.patient_group, 0), pdet.mr_no))
	UNION
	SELECT bc.bill_no, voucher_no, pd.amount, pd.payee_name, pd.payment_type, referal_name AS name,
		pd.posted_date, pd.payment_id, pd.account_group, null AS account_head_name,
		hcm.center_code, dept.cost_center_code AS dept_center_code, b.visit_type, pr.op_type,
		pd.expense_center_id, pd.mod_time,
		sm.salutation, pdet.patient_name, pdet.middle_name, pdet.last_name,
		sm.salutation || ' ' || patient_name
        || CASE WHEN coalesce(middle_name, '') = '' THEN '' ELSE (' ' || middle_name) end
        || CASE WHEN coalesce(last_name, '') = '' THEN '' ELSE (' ' || last_name) end
		as full_name, chargehead_name, bc.charge_head
	FROM payments_details pd
		JOIN bill_charge bc  using(charge_id)
		JOIN bill b using (bill_no)
		JOIN chargehead_constants c ON (bc.charge_head = c.chargehead_id)
		LEFT JOIN patient_registration pr ON (b.visit_id=pr.patient_id)
		LEFT JOIN patient_details pdet ON (pr.mr_no=pdet.mr_no)
		LEFT JOIN salutation_master sm ON (sm.salutation_id=pdet.salutation)
		LEFT JOIN department dept ON (pr.dept_name=dept.dept_id)
		JOIN hospital_center_master hcm ON (hcm.center_id=pd.expense_center_id)
		JOIN referral r  on pd.payee_name=r.referal_no
	WHERE pd.payment_type='F' 
	AND (patient_confidentiality_check(COALESCE(pdet.patient_group, 0), pdet.mr_no))
	UNION
	SELECT bc.bill_no, voucher_no, pd.amount, pd.payee_name, pd.payment_type, pd.payee_name AS name,
		pd.posted_date, pd.payment_id, pd.account_group, account_head_name,
		hcm.center_code, '' AS dept_center_code, '' AS visit_type, '' AS op_type,
		pd.expense_center_id, pd.mod_time,
		'' AS salutation, '' AS patient_name, '' AS middle_name, '' AS last_name, '' AS full_name,
		coalesce(chargehead_name, '') AS chargehead_name, coalesce(bc.charge_head, '') AS charge_head
	FROM payments_details pd
		LEFT OUTER JOIN bill_charge bc using(charge_id)
		LEFT OUTER JOIN bill_account_heads bac ON pd.account_head = bac.account_head_id
		LEFT OUTER JOIN chargehead_constants c ON (bc.charge_head = c.chargehead_id)
		JOIN hospital_center_master hcm ON (hcm.center_id=pd.expense_center_id)
	WHERE pd.payment_type='C'
;

DROP VIEW IF EXISTS counter_associated_accountgroup_view CASCADE;
CREATE OR REPLACE VIEW counter_associated_accountgroup_view AS
	SELECT account_group_id, account_group_name, c.counter_id, c.center_id FROM counters c
	LEFT OUTER JOIN stores gd ON c.counter_id=gd.counter_id
	LEFT OUTER JOIN account_group_master gm ON gm.account_group_id=gd.account_group
	GROUP BY c.counter_id, account_group_id, account_group_name, c.center_id
;


DROP VIEW IF EXISTS all_payment_vouchers_view CASCADE;
CREATE OR REPLACE VIEW all_payment_vouchers_view as
SELECT voucher_no, p.amount, p.tds_amount, p.payee_name, p.reference_no, pd.payment_type,
p.payment_mode_id, pm.payment_mode, p.card_type_id, ctm.card_type,
doctor_name AS name, p.mod_time, p.date, voucher_category, c.account_group_id, p.counter, p.bank,
pm.spl_account_name, pm.bank_required, pm.ref_required, hcm.center_code, c.center_id,
p.tax_amount, p.round_off,d.doctor_name AS payment_receiver,
CASE WHEN p.payment_type ='C' THEN 'Miscellaneous Payments'
WHEN p.payment_type ='O' THEN 'Outsource Payments'
    WHEN p.payment_type ='P' THEN 'Prescribing Doctor Payment'
  WHEN p.payment_type ='D' AND pd.category ilike '%Consultation' THEN 'Doctors Payment'
    WHEN p.payment_type ='D' AND pd.category ilike '%Tests' THEN 'Conducting Doctor Payment'
    WHEN p.payment_type ='F' THEN 'Other Referral Doctor Payment'
    WHEN p.payment_type ='R' THEN 'Referral Doctor Payment'
    WHEN p.payment_type ='S' THEN 'Supplier Payments'
    ELSE ''::text END as voucher_sub_type
FROM payments p
JOIN payments_details pd  using(voucher_no)
JOIN doctors d ON p.payee_name=d.doctor_id
JOIN counter_associated_accountgroup_view c ON c.counter_id=p.counter
JOIN payment_mode_master pm ON (p.payment_mode_id = pm.mode_id)
JOIN hospital_center_master hcm ON (c.center_id=hcm.center_id)
LEFT JOIN card_type_master ctm ON (p.card_type_id = ctm.card_type_id)
WHERE pd.payment_type in ('D', 'R', 'P')
UNION
SELECT voucher_no, p.amount, p.tds_amount, p.payee_name,p.reference_no, pd.payment_type,
p.payment_mode_id, pm.payment_mode, p.card_type_id, ctm.card_type,
oh_name AS name, p.mod_time, p.date, voucher_category, c.account_group_id, p.counter, p.bank,
pm.spl_account_name, pm.bank_required, pm.ref_required, hcm.center_code, c.center_id,
p.tax_amount, p.round_off,ohm.oh_name AS payment_receiver,
CASE WHEN p.payment_type ='C' THEN 'Miscellaneous Payments'
WHEN p.payment_type ='O' THEN 'Outsource Payments'
    WHEN p.payment_type ='P' THEN 'Prescribing Doctor Payment'
  WHEN p.payment_type ='D' AND pd.category ilike '%Consultation' THEN 'Doctors Payment'
    WHEN p.payment_type ='D' AND pd.category ilike '%Tests' THEN 'Conducting Doctor Payment'
    WHEN p.payment_type ='F' THEN 'Other Referral Doctor Payment'
    WHEN p.payment_type ='R' THEN 'Referral Doctor Payment'
    WHEN p.payment_type ='S' THEN 'Supplier Payments'
    ELSE ''::text END as voucher_sub_type
FROM payments p
JOIN payments_details pd using(voucher_no)
JOIN outhouse_master ohm ON p.payee_name=ohm.oh_id
JOIN counter_associated_accountgroup_view c ON c.counter_id=p.counter
JOIN payment_mode_master pm ON (p.payment_mode_id = pm.mode_id)
JOIN hospital_center_master hcm ON (c.center_id=hcm.center_id)
LEFT JOIN card_type_master ctm ON (p.card_type_id = ctm.card_type_id)
WHERE pd.payment_type='O'
UNION
SELECT voucher_no, p.amount, p.tds_amount, p.payee_name, p.reference_no, pd.payment_type,
p.payment_mode_id, pm.payment_mode, p.card_type_id, ctm.card_type,
referal_name AS name, p.mod_time,p.date, voucher_category, c.account_group_id, p.counter, p.bank,
pm.spl_account_name, pm.bank_required, pm.ref_required, hcm.center_code, c.center_id,
p.tax_amount, p.round_off,r.referal_name AS payment_receiver,
CASE WHEN p.payment_type ='C' THEN 'Miscellaneous Payments'
WHEN p.payment_type ='O' THEN 'Outsource Payments'
    WHEN p.payment_type ='P' THEN 'Prescribing Doctor Payment'
  WHEN p.payment_type ='D' AND pd.category ilike '%Consultation' THEN 'Doctors Payment'
    WHEN p.payment_type ='D' AND pd.category ilike '%Tests' THEN 'Conducting Doctor Payment'
    WHEN p.payment_type ='F' THEN 'Other Referral Doctor Payment'
    WHEN p.payment_type ='R' THEN 'Referral Doctor Payment'
    WHEN p.payment_type ='S' THEN 'Supplier Payments'
    ELSE ''::text END as voucher_sub_type
FROM payments p
JOIN payments_details pd  using(voucher_no)
JOIN referral r  ON p.payee_name=r.referal_no
JOIN counter_associated_accountgroup_view c ON c.counter_id=p.counter
JOIN payment_mode_master pm ON (p.payment_mode_id = pm.mode_id)
JOIN hospital_center_master hcm ON (c.center_id=hcm.center_id)
LEFT JOIN card_type_master ctm ON (p.card_type_id = ctm.card_type_id)
WHERE pd.payment_type='F'
UNION
SELECT voucher_no, p.amount, p.tds_amount, p.payee_name, p.reference_no, pd.payment_type,
p.payment_mode_id, pm.payment_mode, p.card_type_id, ctm.card_type,
p.payee_name AS name, p.mod_time, p.date, voucher_category, c.account_group_id, p.counter, p.bank,
pm.spl_account_name, pm.bank_required, pm.ref_required, hcm.center_code, c.center_id,
p.tax_amount, p.round_off,p.payee_name AS payment_receiver,
CASE WHEN p.payment_type ='C' THEN 'Miscellaneous Payments'
WHEN p.payment_type ='O' THEN 'Outsource Payments'
    WHEN p.payment_type ='P' THEN 'Prescribing Doctor Payment'
  WHEN p.payment_type ='D' AND pd.category ilike '%Consultation' THEN 'Doctors Payment'
    WHEN p.payment_type ='D' AND pd.category ilike '%Tests' THEN 'Conducting Doctor Payment'
    WHEN p.payment_type ='F' THEN 'Other Referral Doctor Payment'
    WHEN p.payment_type ='R' THEN 'Referral Doctor Payment'
    WHEN p.payment_type ='S' THEN 'Supplier Payments'
    ELSE ''::text END as voucher_sub_type
FROM payments p
JOIN payments_details pd using(voucher_no)
JOIN counter_associated_accountgroup_view c ON c.counter_id=p.counter
JOIN payment_mode_master pm ON (p.payment_mode_id = pm.mode_id)
JOIN hospital_center_master hcm ON (c.center_id=hcm.center_id)
LEFT JOIN card_type_master ctm ON (p.card_type_id = ctm.card_type_id)
WHERE pd.payment_type='C'
UNION
SELECT voucher_no, p.amount, p.tds_amount, p.payee_name, p.reference_no, pd.payment_type,
p.payment_mode_id, pm.payment_mode, p.card_type_id, ctm.card_type,
sm.supplier_name AS name, p.mod_time, p.date, voucher_category, c.account_group_id, p.counter, p.bank,
pm.spl_account_name, pm.bank_required, pm.ref_required, hcm.center_code, c.center_id,
p.tax_amount, p.round_off,sm.supplier_name AS payment_receiver,
CASE WHEN p.payment_type ='C' THEN 'Miscellaneous Payments'
WHEN p.payment_type ='O' THEN 'Outsource Payments'
    WHEN p.payment_type ='P' THEN 'Prescribing Doctor Payment'
  WHEN p.payment_type ='D' AND pd.category ilike '%Consultation' THEN 'Doctors Payment'
    WHEN p.payment_type ='D' AND pd.category ilike '%Tests' THEN 'Conducting Doctor Payment'
    WHEN p.payment_type ='F' THEN 'Other Referral Doctor Payment'
    WHEN p.payment_type ='R' THEN 'Referral Doctor Payment'
    WHEN p.payment_type ='S' THEN 'Supplier Payments'
    ELSE ''::text END as voucher_sub_type
FROM payments p
JOIN payments_details pd using(voucher_no)
JOIN supplier_master sm ON sm.supplier_code=p.payee_name
JOIN counter_associated_accountgroup_view c ON c.counter_id=p.counter
JOIN payment_mode_master pm ON (p.payment_mode_id = pm.mode_id)
JOIN hospital_center_master hcm ON (c.center_id=hcm.center_id)
LEFT JOIN card_type_master ctm ON (p.card_type_id = ctm.card_type_id)
WHERE pd.payment_type='S'
;

--
-- View for Custom report for VGH -a bug 9005
--
DROP VIEW IF EXISTS doc_op_cons_report_view CASCADE;
CREATE  OR REPLACE VIEW doc_op_cons_report_view AS
SELECT doc.doctor_name, pr.mr_no, b.visit_id,
	b.bill_no, date(b.finalized_date), bc.amount, bc.discount, bc.paid_amount,
	get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS patient_full_name
FROM bill_charge bc
	JOIN bill b ON (b.bill_no = bc.bill_no)
	JOIN patient_registration pr ON (b.visit_id = pr.patient_id)
	JOIN patient_details pd ON (pr.mr_no = pd.mr_no)
	LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
	LEFT JOIN doctors doc ON (doc.doctor_id = pr.doctor)
WHERE b.visit_type ='o' AND bc.charge_head ='OPDOC' 
AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )
GROUP BY doctor_name,  pr.mr_no, b.visit_id, patient_full_name, b.bill_no, b.finalized_date, bc.amount,
	bc.discount, bc.paid_amount
ORDER BY doctor_name, date(finalized_date)
;

--
-- View for Custom report for BGSAIMS - done in 2 steps to avoid divide by zero error
--
DROP VIEW IF EXISTS pharmacy_margin_view CASCADE;
CREATE VIEW pharmacy_margin_view AS
SELECT m.medicine_name, s.batch_no, sum(s.quantity) AS qty_sold, sum(s.amount) AS sale_value,
        sum((pkg_cp)*s.quantity/package_unit) AS pur_cost,
        sm.date_time::timestamp(0) AS date,
        (sum(s.amount) - sum((pkg_cp)*s.quantity/package_unit))
                AS margin
FROM store_sales_details s
        JOIN store_sales_main sm USING(sale_id)
        JOIN store_item_details m ON (s.medicine_id=m.medicine_id)
GROUP by m.medicine_name, s.batch_no, date ;

DROP VIEW IF EXISTS pharmacy_margin_report_view CASCADE;
CREATE VIEW pharmacy_margin_report_view AS
SELECT m.medicine_name AS item_name, m.batch_no, m.date, m.qty_sold, m.sale_value, m.pur_cost, m.margin, coalesce(m.margin/nullif(m.pur_cost,0)*100,0)::numeric(10,2) AS margin_percent
FROM pharmacy_margin_view m
ORDER BY margin_percent DESC
;
--
-- View for custom Report for outgoing tests
--
DROP VIEW IF EXISTS outgoing_test_report_view CASCADE;
CREATE OR REPLACE VIEW outgoing_test_report_view AS
SELECT CASE WHEN outsource_dest_type IN('O','IO') THEN om.oh_name ELSE hcm.center_name END AS outhouse ,
outsource_dest AS oh_id, tp.mr_no, tp.pat_id,
CASE WHEN pd.patient_name is null THEN isr.patient_name ELSE
get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) END AS patient_full_name , d.test_name,
tp.conducted,dd.category, date(tp.pres_date) AS date, bc.amount AS billed_amount,COALESCE(ohd.charge, 0) AS oh_amount,tp.prescribed_id,
COALESCE(pr.center_id, isr.center_id) AS center_id
 FROM diagnostics d
JOIN tests_prescribed tp ON (tp.test_id=d.test_id and tp.prescription_type in ('o', 'i'))
JOIN outsource_sample_details osd USING (prescribed_id)
JOIN diagnostics_departments dd ON (dd.ddept_id = d.ddept_id)
JOIN diag_outsource_master dom ON(dom.outsource_dest_id = osd.outsource_dest_id)
LEFT JOIN outhouse_master om ON (dom.outsource_dest=om.oh_id)
LEFT JOIN hospital_center_master hcm ON (hcm.center_id::text = dom.outsource_dest)
LEFT JOIN patient_details pd USING (mr_no)
LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
JOIN bill_activity_charge bac  ON (tp.prescribed_id::text = bac.activity_id AND activity_code = 'DIA')
JOIN bill_charge bc ON (bac.charge_id = bc.charge_id)
LEFT JOIN patient_registration pr ON ( pr.patient_id = tp.pat_id )
LEFT JOIN incoming_sample_registration isr ON(isr.incoming_visit_id = tp.pat_id)
LEFT JOIN diag_outsource_detail ohd ON (osd.outsource_dest_id = ohd.outsource_dest_id
AND tp.test_id = ohd.test_id AND ohd.source_center_id = (COALESCE(pr.center_id, isr.center_id)))
WHERE ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) )
;

--
-- View for custom Report for incoming tests
--
DROP VIEW IF EXISTS incoming_test_report_view CASCADE;
CREATE or REPLACE VIEW incoming_test_report_view AS
SELECT ih.hospital_name, isr.incoming_visit_id, isr.patient_name, d.test_name, tp.conducted AS status,
date(tp.pres_date) AS date, bc.amount AS billed_amount FROM incoming_sample_registration isr
LEFT JOIN incoming_sample_registration_details isrd USING (incoming_visit_id)
LEFT JOIN incoming_hospitals ih ON (isr.orig_lab_name=ih.hospital_id)
LEFT JOIN tests_prescribed tp ON (tp.prescribed_id=isrd.prescribed_id)
LEFT JOIN diagnostics d ON (tp.test_id=d.test_id)
LEFT JOIN bill b  ON (tp.pat_id=b.visit_id)
LEFT JOIN bill_charge bc  USING (bill_no)
;

--
-- View for custom report for ajhrc: name change based ON audit log
--
DROP VIEW IF EXISTS patient_name_update CASCADE;
CREATE or REPLACE VIEW patient_name_update AS
SELECT user_name, mr_no, operation, field_name, old_value, new_value, mod_time AS date
FROM patient_details_audit_log
WHERE field_name IN ('patient_name', 'middle_name') AND operation = 'UPDATE'
ORDER BY mod_time;

--
-- Custom report for llhm: no longer required
--
DROP VIEW IF EXISTS sample_no_view;

/*
 * Triggers for automatically setting the visit_id and previous_visit ID in
 * patient_details based ON changes in patient_registratin
 */
DROP TRIGGER IF EXISTS update_active_visit_id ON patient_registration;
DROP FUNCTION IF EXISTS update_active_visit_id();

CREATE OR REPLACE FUNCTION update_active_visit_id() RETURNS trigger AS $BODY$
BEGIN

	/*
	 * If deactivated, (A -> I), THEN do the following:
	 *   the old visit id AS this one
	 *   current visit id is NULL (ie, no current visit ID)
	 */
	IF (OLD.status = 'A' AND NEW.status = 'I') THEN
		UPDATE patient_details SET previous_visit_id = NEW.patient_id,mod_time = current_timestamp,
			visit_id = (
				SELECT patient_id FROM patient_registration
				WHERE mr_no = OLD.mr_no AND patient_id != NEW.patient_id AND status = 'A'
				ORDER BY reg_date + reg_time DESC LIMIT 1
			)
			WHERE mr_no = NEW.mr_no;
	END IF;

	/*
	 * If activated (it's a readmit), then, set the following:
	 *  current visit_id = new visit_id
	 *  previous_visit_id = latest visit ID in patient_registration for this mr_no, other than
	 *    this visit
	 */
	IF (OLD.status = 'I' AND NEW.status = 'A') THEN

		UPDATE patient_details SET
			visit_id = OLD.patient_id,mod_time = current_timestamp,
			previous_visit_id = (
    			SELECT patient_id FROM patient_registration 
				WHERE mr_no = OLD.mr_no AND patient_id != NEW.patient_id AND status = 'I'
				ORDER BY reg_date + reg_time DESC LIMIT 1
			)
		WHERE mr_no=OLD.mr_no;

	END IF;

	RETURN NEW;
END;
$BODY$ LANGUAGE 'plpgsql';


DROP TRIGGER IF EXISTS update_active_visit_id ON patient_registration;

CREATE TRIGGER update_active_visit_id
	AFTER UPDATE ON patient_registration
	FOR EACH ROW EXECUTE PROCEDURE update_active_visit_id();

DROP TRIGGER  IF EXISTS insert_active_visit_id ON patient_registration;
DROP FUNCTION  IF EXISTS insert_active_visit_id();

CREATE OR REPLACE FUNCTION insert_active_visit_id() RETURNS trigger AS $BODY$
BEGIN
	/*
	 * Insert is usually AS active, but can be AS inactive WHEN coming in
	 * from bulk upload.
	 */
	IF (NEW.status = 'A') THEN
		UPDATE patient_details SET visit_id = NEW.patient_id WHERE mr_no=NEW.mr_no;
	ELSE
		UPDATE patient_details SET visit_id=NULL, previous_visit_id=NEW.patient_id WHERE mr_no=NEW.mr_no;
	END IF;

	RETURN NULL;
END;
$BODY$ LANGUAGE 'plpgsql';


DROP TRIGGER IF EXISTS insert_active_visit_id ON patient_registration;
CREATE TRIGGER insert_active_visit_id
	AFTER INSERT ON patient_registration
	FOR EACH ROW EXECUTE PROCEDURE insert_active_visit_id();

-- Visit Type Trends
DROP VIEW IF EXISTS visit_type_7days_view CASCADE ;
CREATE VIEW  visit_type_7days_view AS
SELECT   date(date_trunc('day',reg_date)) AS period,  visit_type, count(patient_id) AS count
FROM patient_registration
WHERE date(reg_date) BETWEEN current_date-7 AND current_date
GROUP BY period, visit_type
ORDER BY period DESC
;
DROP VIEW IF EXISTS visit_type_4weeks_view CASCADE ;
CREATE VIEW  visit_type_4weeks_view AS
SELECT  'Week '|| date(date_trunc('week',reg_date)) AS period,  visit_type, count(patient_id) AS count
FROM patient_registration
WHERE date(reg_date) BETWEEN current_date-30 AND current_date
GROUP BY period, visit_type
ORDER BY period DESC
;
DROP VIEW IF EXISTS visit_type_12months_view CASCADE;
CREATE VIEW  visit_type_12months_view AS
SELECT  'Month '|| date(date_trunc('month',reg_date)) AS period,  visit_type, count(patient_id) AS count
FROM patient_registration
WHERE date(reg_date) BETWEEN current_date-365 AND current_date
GROUP BY period, visit_type
ORDER BY period DESC
;
-- Bill Type Trends
DROP VIEW IF EXISTS bill_type_7days_view CASCADE ;
CREATE VIEW  bill_type_7days_view AS
SELECT   date(date_trunc('day',finalized_date)) AS period,  bill_type, count(bill_no) AS count
FROM bill
WHERE date(finalized_date) BETWEEN current_date-7 AND current_date
GROUP BY period, bill_type
ORDER BY period DESC
;
DROP VIEW IF EXISTS bill_type_4weeks_view CASCADE ;
CREATE VIEW  bill_type_4weeks_view AS
SELECT  'Week '|| date(date_trunc('week',finalized_date)) AS period,  bill_type, count(bill_no) AS count
FROM bill
WHERE date(finalized_date) BETWEEN current_date-30 AND current_date
GROUP BY period, bill_type
ORDER BY period DESC
;
DROP VIEW IF EXISTS bill_type_12months_view CASCADE;
CREATE VIEW  bill_type_12months_view AS
SELECT  'Month '|| date(date_trunc('month',finalized_date)) AS period,  bill_type, count(bill_no) AS count
FROM bill
WHERE date(finalized_date) BETWEEN current_date-365 AND current_date
GROUP BY period, bill_type
ORDER BY period DESC
;
-- Insurance claim (from bill) Trends
DROP VIEW IF EXISTS insurance_claims_7days_view CASCADE ;
CREATE VIEW  insurance_claims_7days_view AS
SELECT   date(date_trunc('day',finalized_date)) AS period,  primary_claim_status, count(bill_no) AS count
FROM bill
WHERE date(finalized_date) BETWEEN current_date-7 AND current_date
GROUP BY period, primary_claim_status
ORDER BY period DESC
;
DROP VIEW IF EXISTS insurance_claim_4weeks_view CASCADE ;
CREATE VIEW  insurance_claim_4weeks_view AS
SELECT  'Week '|| date(date_trunc('week',finalized_date)) AS period,  primary_claim_status, count(bill_no) AS count
FROM bill
WHERE date(finalized_date) BETWEEN current_date-30 AND current_date
GROUP BY period, primary_claim_status
ORDER BY period DESC
;
DROP VIEW IF EXISTS insurance_claim_12months_view CASCADE;
CREATE VIEW  insurance_claim_12months_view AS
SELECT  'Month '|| date(date_trunc('month',finalized_date)) AS period,  primary_claim_status, count(bill_no) AS count
FROM bill
WHERE date(finalized_date) BETWEEN current_date-365 AND current_date
GROUP BY period, primary_claim_status
ORDER BY period DESC
;
-- Test Report Trends
DROP VIEW IF EXISTS test_reports_7days_view CASCADE ;
CREATE VIEW  test_reports_7days_view AS
SELECT   date(date_trunc('day',report_date)) AS period,  category, count(*) AS count
FROM test_visit_reports
WHERE date(report_date) BETWEEN current_date-7 AND current_date
GROUP BY period, category
ORDER BY period DESC
;
DROP VIEW IF EXISTS test_reports_4weeks_view CASCADE ;
CREATE VIEW  test_reports_4weeks_view AS
SELECT  'Week ' || date(date_trunc('week',report_date)) AS period,  category, count(*) AS count
FROM test_visit_reports
WHERE date(report_date) BETWEEN current_date-30 AND current_date
GROUP BY period, category
ORDER BY period DESC
;
DROP VIEW IF EXISTS test_reports_12months_view CASCADE ;
CREATE VIEW  test_reports_12months_view AS
SELECT   'Month ' || date(date_trunc('month',report_date)) AS period,  category, count(*) AS count
FROM test_visit_reports
WHERE date(report_date) BETWEEN current_date-365 AND current_date
GROUP BY period, category
ORDER BY period DESC
;
-- Discharge Summary Trends
DROP VIEW IF EXISTS discharge_summary_7days_view CASCADE ;
CREATE VIEW  discharge_summary_7days_view AS
SELECT   date(date_trunc('day',discharge_date)) AS period,  discharge_format, count(*) AS count
FROM patient_registration
WHERE date(discharge_date) BETWEEN current_date-7 AND current_date
GROUP BY period, discharge_format
ORDER BY period DESC
;
DROP VIEW IF EXISTS discharge_summary_4weeks_view CASCADE ;
CREATE VIEW discharge_summary_4weeks_view AS
SELECT  'Week ' || date(date_trunc('week',discharge_date)) AS period,  discharge_format, count(*) AS count
FROM patient_registration
WHERE date(discharge_date) BETWEEN current_date-30 AND current_date
GROUP BY period, discharge_format
ORDER BY period DESC
;
DROP VIEW IF EXISTS discharge_summary_12months_view CASCADE ;
CREATE VIEW  discharge_summary_12months_view AS
SELECT   'Month ' || date(date_trunc('month',discharge_date)) AS period,  discharge_format, count(*) AS count
FROM patient_registration
WHERE date(discharge_date) BETWEEN current_date-365 AND current_date
GROUP BY period, discharge_format
ORDER BY period DESC
;
-- Pharmacy Sales Trends
DROP VIEW IF EXISTS pharmacy_sales_7days_view CASCADE ;
CREATE VIEW  pharmacy_sales_7days_view AS
SELECT   date(date_trunc('day',sale_date)) AS period,  type, count(*) AS count
FROM store_sales_main
WHERE date(sale_date) BETWEEN current_date-7 AND current_date
GROUP BY period, type
ORDER BY period DESC
;
DROP VIEW IF EXISTS pharmacy_sales_4weeks_view CASCADE ;
CREATE VIEW pharmacy_sales_4weeks_view AS
SELECT  'Week ' || date(date_trunc('week',sale_date)) AS period,  type, count(*) AS count
FROM store_sales_main
WHERE date(sale_date) BETWEEN current_date-30 AND current_date
GROUP BY period, type
ORDER BY period DESC
;
DROP VIEW IF EXISTS pharmacy_sales_12months_view CASCADE ;
CREATE VIEW  pharmacy_sales_12months_view AS
SELECT   'Month ' || date(date_trunc('month',sale_date)) AS period,  type, count(*) AS count
FROM store_sales_main
WHERE date(sale_date) BETWEEN current_date-365 AND current_date
GROUP BY period, type
ORDER BY period DESC
;
-- Bed Admission Trends
DROP VIEW IF EXISTS admission_7days_view CASCADE ;
CREATE VIEW  admission_7days_view AS
SELECT   date(date_trunc('day',admit_date)) AS period,  b.bed_type, count(*) AS count
FROM admission a
JOIN bed_names b using (bed_id)
WHERE date(admit_date) BETWEEN current_date-7 AND current_date
GROUP BY period, b.bed_type
ORDER BY period DESC
;
DROP VIEW IF EXISTS admission_4weeks_view CASCADE ;
CREATE VIEW  admission_4weeks_view AS
SELECT  'Week ' || date(date_trunc('week',admit_date)) AS period,  b.bed_type, count(*) AS count
FROM admission a
JOIN bed_names b using (bed_id)
WHERE date(admit_date) BETWEEN current_date-30 AND current_date
GROUP BY period, b.bed_type
ORDER BY period DESC
;
DROP VIEW IF EXISTS admission_12months_view CASCADE ;
CREATE VIEW  admission_12months_view AS
SELECT  'Month ' || date(date_trunc('month',admit_date)) AS period,  b.bed_type, count(*) AS count
FROM admission a
JOIN bed_names b using (bed_id)
WHERE date(admit_date) BETWEEN current_date-365 AND current_date
GROUP BY period, b.bed_type
ORDER BY period DESC
;

-- Insurance CASE Trends
DROP VIEW IF EXISTS insurance_case_7days_view CASCADE ;
CREATE VIEW  insurance_case_7days_view AS
SELECT   date(date_trunc('day',case_added_date)) AS period,   count(*) AS count
FROM insurance_case
WHERE date(case_added_date) BETWEEN current_date-7 AND current_date
GROUP BY period
ORDER BY period DESC
;
DROP VIEW IF EXISTS insurance_case_4weeks_view CASCADE ;
CREATE VIEW insurance_case_4weeks_view AS
SELECT  'Week ' || date(date_trunc('week',case_added_date)) AS period, count(*) AS count
FROM insurance_case
WHERE date(case_added_date) BETWEEN current_date-30 AND current_date
GROUP BY period
ORDER BY period DESC
;
DROP VIEW IF EXISTS insurance_case_12months_view CASCADE ;
CREATE VIEW  insurance_case_12months_view AS
SELECT   'Month ' || date(date_trunc('month',case_added_date)) AS period, count(*) AS count
FROM insurance_case
WHERE date(case_added_date) BETWEEN current_date-365 AND current_date
GROUP BY period
ORDER BY period DESC
;
-- Payment voucher Trends
DROP VIEW IF EXISTS payments_7days_view CASCADE ;
CREATE VIEW  payments_7days_view AS
SELECT   date(date_trunc('day',date)) AS period,   count(*) AS count
FROM payments
WHERE date(date) BETWEEN current_date-7 AND current_date
GROUP BY period
ORDER BY period DESC
;
DROP VIEW IF EXISTS payments_4weeks_view CASCADE ;
CREATE VIEW payments_4weeks_view AS
SELECT  'Week ' || date(date_trunc('week',date)) AS period, count(*) AS count
FROM payments
WHERE date(date) BETWEEN current_date-30 AND current_date
GROUP BY period
ORDER BY period DESC
;
DROP VIEW IF EXISTS payments_12months_view CASCADE ;
CREATE VIEW  payments_12months_view AS
SELECT   'Month ' || date(date_trunc('month',date)) AS period, count(*) AS count
FROM payments
WHERE date(date) BETWEEN current_date-365 AND current_date
GROUP BY period
ORDER BY period DESC
;

-- Store User Issue Trends
DROP VIEW IF EXISTS store_issues_7days_view CASCADE ;
CREATE VIEW  store_issues_7days_view AS
SELECT   date(date_trunc('day',date_time)) AS period, user_type,  count(*) AS count
FROM stock_issue_main
WHERE date(date_time) BETWEEN current_date-7 AND current_date
GROUP BY period, user_type
ORDER BY period DESC
;
DROP VIEW IF EXISTS store_issues_4weeks_view CASCADE ;
CREATE VIEW store_issues_4weeks_view AS
SELECT  'Week ' || date(date_trunc('week',date_time)) AS period, user_type, count(*) AS count
FROM stock_issue_main
WHERE date(date_time) BETWEEN current_date-30 AND current_date
GROUP BY period, user_type
ORDER BY period DESC
;
DROP VIEW IF EXISTS store_issues_12months_view CASCADE ;
CREATE VIEW  store_issues_12months_view AS
SELECT   'Month ' || date(date_trunc('month',date_time)) AS period, user_type, count(*) AS count
FROM stock_issue_main
WHERE date(date_time) BETWEEN current_date-365 AND current_date
GROUP BY period, user_type
ORDER BY period DESC
;


--View for medicine sales

DROP VIEW IF EXISTS medicine_sales_view CASCADE;
CREATE OR REPLACE VIEW medicine_sales_view AS
SELECT m.medicine_name, m.medicine_id, s.batch_no, sum(s.quantity) AS qty_sold,date(s.expiry_date) AS date,
pmsd.qty,pmsd.received_date,sm.sale_date
   FROM store_sales_details s
   JOIN store_stock_details pmsd ON (pmsd.medicine_id = s.medicine_id AND pmsd.batch_no = s.batch_no)
   JOIN store_sales_main sm ON (s.sale_id = sm.sale_id)
   JOIN store_item_details m ON s.medicine_id = m.medicine_id
   GROUP BY m.medicine_name, m.medicine_id, s.batch_no, date,pmsd.qty,pmsd.received_date,sm.sale_date
  ORDER BY m.medicine_id
  ;

-- view for
-- 1) All medicines from store_item_details.
-- 2) All medicines from common_medicine_charge_master which are not in store_item_details.
-- 3) All medicines from prescribed_medicines_master which are not in store_item_details and common_medicine_charge_master.

DROP VIEW IF EXISTS active_pharmacy_medicines_view CASCADE;
CREATE OR REPLACE VIEW active_pharmacy_medicines_view AS
	SELECT pmd.medicine_name, pmd.medicine_id, pmd.generic_name, COALESCE(sum(pmsd.qty), 0) AS qty,
	cum.cons_uom_id,cum.consumption_uom
   	FROM store_item_details pmd
   		JOIN store_category_master scm ON (pmd.med_category_id=scm.category_id)
   		LEFT JOIN store_stock_details pmsd ON pmsd.medicine_id = pmd.medicine_id
		LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = pmd.cons_uom_id)
   	WHERE pmd.status='A'
   	GROUP BY medicine_name, pmd.medicine_id, pmd.generic_name, cum.cons_uom_id;

DROP VIEW IF EXISTS active_prescribed_medicines_view CASCADE;
CREATE OR REPLACE VIEW active_prescribed_medicines_view AS
	SELECT medicine_name, generic_name, ''::text AS consumption_uom
	FROM prescribed_medicines_master
	WHERE status='A'
		AND TRIM(medicine_name) NOT IN (SELECT TRIM(medicine_name)  FROM active_pharmacy_medicines_view);

DROP VIEW IF EXISTS all_active_medicines_view CASCADE;
CREATE OR REPLACE VIEW all_active_medicines_view AS
	SELECT medicine_name, medicine_id, trunc(qty, 2) AS qty, generic_name, 'item_master' AS master,
		consumption_uom FROM  active_pharmacy_medicines_view
	UNION
	SELECT medicine_name, 0 AS medicine_id, 0 AS qty, generic_name, 'op' AS master,
		consumption_uom FROM active_prescribed_medicines_view;

DROP VIEW IF EXISTS all_services_view CASCADE;
CREATE OR REPLACE VIEW all_services_view AS
	SELECT s.service_name, s.service_id, 'item_master' AS master FROM services s
	UNION
	SELECT service_name, '' AS service_id, 'op' AS master FROM prescribed_services_master WHERE
	status='A' AND TRIM(service_name) NOT IN (SELECT TRIM(service_name) FROM services WHERE status='A');


DROP VIEW IF EXISTS all_tests_pkgs_view CASCADE;
CREATE OR REPLACE VIEW all_tests_pkgs_view AS
	SELECT test_name, test_id, false AS ispkg, 'item_master' AS master, status, diag_code AS order_code,
		prior_auth_required, ddept_id, '' AS pack_type, 'A' AS approval_status, insurance_category_id FROM diagnostics
	UNION
	SELECT package_name, package_id::text, true AS ispkg, 'item_master' AS master, status, package_code AS order_code,
		prior_auth_required, '' AS ddept_id, type AS pack_type, approval_status, insurance_category_id 
	FROM packages WHERE type in ('d', 'P') AND package_category_id in (-3, -2);


DROP VIEW IF EXISTS store_item_in_stock_view CASCADE;
CREATE OR REPLACE VIEW store_item_in_stock_view AS
 SELECT pmsd.medicine_id, pms.medicine_name,pms.cust_item_code,sibd.batch_no, sum(pmsd.qty) AS stock_qty,
 gd.dept_id, pmsd.package_cp, sibd.mrp, round(pmsd.package_cp / pms.issue_base_unit, 2)
 AS unit_cp, round(sibd.mrp / pms.issue_base_unit, 2) AS unit_mrp
   FROM store_stock_details pmsd
   JOIN store_item_batch_details sibd USING(item_batch_id)
   JOIN store_item_details pms ON (pms.medicine_id = pmsd.medicine_id)
   JOIN stores gd USING (dept_id)
  GROUP BY pmsd.medicine_id, pms.medicine_name,pms.cust_item_code, sibd.batch_no, gd.dept_id,
  pmsd.package_cp, pms.issue_base_unit, pmsd.qty, sibd.mrp
  ORDER BY pmsd.medicine_id;

DROP VIEW IF EXISTS store_sales_details_view CASCADE;
CREATE OR REPLACE VIEW store_sales_details_view AS
SELECT s.medicine_id, s.batch_no, sum(s.quantity) AS sold_qty, date(pmsm.sale_date) AS sales_date, gd.dept_id
FROM store_sales_details s
   JOIN store_sales_main pmsm USING (sale_id)
   JOIN stores gd ON gd.dept_id = pmsm.store_id
  GROUP BY s.medicine_id, s.batch_no, date(pmsm.sale_date), gd.dept_id
  ORDER BY s.medicine_id;


drop view if exists service_open_prescription_visits_view CASCADE;
CREATE OR REPLACE VIEW service_open_prescription_visits_view AS
	(SELECT sp.patient_id, max(sp.prescription_id) AS pres_id
	   FROM services_prescribed sp
	   JOIN service_reports sr ON sp.report_id = sr.report_id AND sr.signed_off IS FALSE
	  GROUP BY sp.patient_id
	UNION
	 SELECT sp.patient_id, max(sp.prescription_id) AS pres_id
	   FROM services_prescribed sp
	  WHERE sp.conducted::text <> 'X'::text AND NOT (sp.patient_id::text IN (
	      SELECT service_reports.visit_id
	           FROM service_reports
	          WHERE service_reports.signed_off IS TRUE))
	  GROUP BY sp.patient_id)
	UNION
	 SELECT sp.patient_id, max(sp.prescription_id) AS pres_id
	   FROM services_prescribed sp
	   join services s using(service_id)
	  WHERE sp.report_id IS NULL AND sp.conducted::text <> 'X'::text AND s.conduction_applicable='t'
	  GROUP BY sp.patient_id
	  ORDER BY 2 DESC;

DROP VIEW IF EXISTS bill_receipts_view CASCADE;
CREATE OR REPLACE VIEW bill_receipts_view AS
		SELECT CASE WHEN r.tpa_id IS NOT NULL THEN 'S' ELSE r.receipt_type END AS payment_type, 
		CASE WHEN r.is_settlement THEN 'S' ELSE 'A' END AS recpt_type, br.sponsor_index, 
		br.receipt_no AS receipt_no, br.bill_no AS bill_no,
		CASE WHEN (r.is_deposit OR (rc.reconciliation_id IS NOT NULL)) THEN br.allocated_amount ELSE r.amount END AS amount,
		r.tds_amount AS tds_amt,r.display_date AS display_date, r.mob_number AS mob_number, 
		r.totp AS totp,
		br.mod_time, r.counter AS counter, r.payment_mode_id,r.card_type_id,pm.payment_mode,
		ctm.card_type,
		r.bank_name AS bank_name, r.reference_no AS reference_no, r.created_by AS username,
		r.remarks AS remarks,
		r.bank_batch_no, r.card_auth_code, r.card_holder_name, r.currency_id, r.exchange_rate,
		r.exchange_date, r.currency_amt, r.card_exp_date AS card_expdate, r.card_number, 
		fc.currency,
		b.visit_id AS visit_id, b.visit_type AS visit_type, b.bill_type AS bill_type,
		b.restriction_type AS restriction_type,
		b.status AS status,  p.mr_no AS mr_no, s.salutation, p.patient_name, p.middle_name, 
		p.last_name,
		s.salutation || ' ' || p.patient_name || CASE WHEN coalesce(p.middle_name, '') = '' THEN '' 
		  ELSE (' ' || p.middle_name) END || CASE WHEN coalesce(p.last_name, '') = '' THEN '' 
		  ELSE (' ' || p.last_name) END AS patient_full_name,
		coalesce(p.dateofbirth, expected_dob) AS dob, p.patient_gender,
		phc.customer_name AS customer_name , isr.patient_name AS incoming_patient_name,counter_type,
		c.counter_no, spl_account_name AS payment_mode_account, r.paid_by,
		c.center_id, pm.ref_required, pm.bank_required, 
		CASE WHEN r.is_deposit THEN 'true' ELSE 'false' END AS is_deposit,
		-- bank_name referenced AS bank in tally(which is replaced with template matcher)
		r.bank_name AS bank,pr.collection_center_id,
		CASE WHEN bcn.credit_note_bill_no IS NOT NULL THEN true ELSE false END AS is_credit_note, 
		r.is_settlement AS is_settlement, r.tpa_id AS tpa_id, r.deposit_available_for, r.package_id,
		br.allocated_amount AS allocated_amount
	FROM receipt_usage_view r
    JOIN bill_receipts br ON (br.receipt_no = r.receipt_id)
    JOIN bill b ON (b.bill_no = br.bill_no)
	JOIN payment_mode_master pm ON (r.payment_mode_id = pm.mode_id)
	LEFT JOIN reconciliation rc ON (r.receipt_id = rc.receipt_id)
	LEFT JOIN counters c on r.counter=counter_id
	LEFT JOIN patient_registration pr on (b.visit_id = pr.patient_id)
	LEFT JOIN patient_details p on (pr.mr_no = p.mr_no)
	LEFT JOIN bill_credit_notes bcn ON(bcn.credit_note_bill_no = br.bill_no)
	LEFT JOIN card_type_master ctm ON (ctm.card_type_id = r.card_type_id)
	LEFT JOIN foreign_currency fc ON (fc.currency_id = r.currency_id)
	LEFT JOIN store_retail_customers phc on (b.visit_id = phc.customer_id)
	LEFT JOIN salutation_master s on (s.salutation_id = p.salutation)
	LEFT JOIN incoming_sample_registration isr on (isr.billno=b.bill_no)
WHERE (patient_confidentiality_check(COALESCE(p.patient_group, 0), p.mr_no))
;


DROP VIEW IF EXISTS deposits_receipts_view CASCADE;
CREATE OR REPLACE VIEW deposits_receipts_view AS
SELECT CASE WHEN pdepo.receipt_type = 'R' THEN 'DR' WHEN pdepo.receipt_type = 'F' THEN 'DF'
	else ''  END AS payment_type, pdepo.receipt_id AS receipt_no, pdepo.amount AS amount, 
	0.00 AS tds_amt, pdepo.display_date AS display_date,
	pdepo.modified_at AS mod_time, pdepo.counter AS counter, pdepo.payment_mode_id, pdepo.card_type_id, pm.payment_mode,
	ctm.card_type, pdepo.bank_name AS bank_name, pdepo.reference_no AS reference_no, pdepo.mob_number AS mob_number, pdepo.totp AS totp,
	pdepo.created_by AS username, pdepo.remarks AS remarks,
	pdepo.bank_batch_no, pdepo.card_auth_code, pdepo.card_holder_name, pdepo.currency_id, pdepo.exchange_rate,
	pdepo.exchange_date, pdepo.currency_amt, pdepo.card_exp_date AS card_expdate, pdepo.card_number, fc.currency,
	case WHEN pdepo.receipt_type = 'R' THEN 'DR'  WHEN pdepo.receipt_type = 'F' THEN 'DF'  ELSE ''  END AS status,
	p.mr_no AS mr_no, s.salutation, p.patient_name, p.middle_name, p.last_name,
	get_patient_full_name(s.salutation, p.patient_name, p.middle_name, p.last_name) AS patient_full_name,
	COALESCE(p.dateofbirth, p.expected_dob) AS dob, p.patient_gender, counter_type, c.counter_no,
	spl_account_name AS payment_mode_account,pdepo.paid_by, c.center_id, pm.ref_required, pm.bank_required,
	-- bank_name referenced AS bank in tally(which is replaced with template matcher)
	pdepo.bank_name AS bank, fc.conversion_rate, pdepo.deposit_available_for, pdepo.package_id,
	hcm.center_name,pdepo.center_id as receipt_center_id
FROM patient_deposits_view pdepo
	LEFT JOIN counters c ON pdepo.counter=counter_id
	LEFT JOIN patient_details p ON pdepo.mr_no::text = p.mr_no::text
	LEFT JOIN salutation_master s ON s.salutation_id::text = p.salutation::text
	JOIN payment_mode_master pm ON (pm.mode_id = pdepo.payment_mode_id)
	LEFT JOIN foreign_currency fc ON (fc.currency_id = pdepo.currency_id)
	LEFT JOIN card_type_master ctm ON (ctm.card_type_id = pdepo.card_type_id)
	LEFT JOIN hospital_center_master hcm ON hcm.center_id=pdepo.center_id
WHERE is_deposit and (patient_confidentiality_check(COALESCE(p.patient_group, 0), p.mr_no))
;


-- diagnostics audit logs

DROP VIEW IF EXISTS  diag_all_prescription_visits_view CASCADE;
CREATE OR REPLACE VIEW  diag_all_prescription_visits_view AS
( SELECT tp.pat_id, max(tp.prescribed_id) AS pres_id
   FROM  tests_prescribed tp
   JOIN  test_visit_reports tvr ON tvr.report_id = tp.report_id AND tvr.category::text = 'DEP_LAB'::text
  GROUP BY tp.pat_id
UNION
 SELECT tp.pat_id, max(tp.prescribed_id) AS pres_id
   FROM  tests_prescribed tp
   JOIN  diagnostics d ON tp.test_id::text = d.test_id::text
   JOIN  diagnostics_departments dd ON d.ddept_id::text = dd.ddept_id::text
  WHERE NOT (tp.pat_id::text IN ( SELECT test_visit_reports.patient_id
   FROM  test_visit_reports
  WHERE test_visit_reports.category::text = 'DEP_LAB'::text)) AND dd.category::text = 'DEP_LAB'::text
  GROUP BY tp.pat_id)
UNION
 SELECT tp.pat_id, max(tp.prescribed_id) AS pres_id
   FROM  tests_prescribed tp
   JOIN  diagnostics d ON d.test_id::text = tp.test_id::text
   JOIN  diagnostics_departments dd ON d.ddept_id::text = dd.ddept_id::text
  WHERE tp.report_id IS NULL AND dd.category::text = 'DEP_LAB'::text
  GROUP BY tp.pat_id
  ORDER BY 2 DESC;

DROP VIEW IF EXISTS  radiology_all_prescription_visits_view CASCADE;
CREATE OR REPLACE VIEW  radiology_all_prescription_visits_view AS
( SELECT tp.pat_id, max(tp.prescribed_id) AS pres_id
   FROM  tests_prescribed tp
   JOIN  test_visit_reports tvr ON tvr.report_id = tp.report_id AND tvr.category::text = 'DEP_RAD'::text
  GROUP BY tp.pat_id
UNION
 SELECT tp.pat_id, max(tp.prescribed_id) AS pres_id
   FROM  tests_prescribed tp
   JOIN  diagnostics d ON tp.test_id::text = d.test_id::text
   JOIN  diagnostics_departments dd ON d.ddept_id::text = dd.ddept_id::text
  WHERE NOT (tp.pat_id::text IN ( SELECT test_visit_reports.patient_id
   FROM  test_visit_reports
  WHERE test_visit_reports.category::text = 'DEP_RAD'::text)) AND dd.category::text = 'DEP_RAD'::text
  GROUP BY tp.pat_id)
UNION
 SELECT tp.pat_id, max(tp.prescribed_id) AS pres_id
   FROM  tests_prescribed tp
   JOIN  diagnostics d ON d.test_id::text = tp.test_id::text
   JOIN  diagnostics_departments dd ON d.ddept_id::text = dd.ddept_id::text
  WHERE tp.report_id IS NULL AND dd.category::text = 'DEP_RAD'::text
  GROUP BY tp.pat_id
  ORDER BY 2 DESC;

DROP VIEW IF EXISTS store_reorder_levels CASCADE;
CREATE VIEW store_reorder_levels AS
SELECT min_level, max_level, reorder_level, danger_level, danger_uom, medicine_id, dept_id 
FROM item_store_level_details where danger_level IS NOT NULL;

DROP VIEW IF EXISTS current_stock_details_view CASCADE;
CREATE VIEW current_stock_details_view AS
select pmd.medicine_name,pmd.medicine_id,sibd.batch_no,pmc.category_name,pmc.category_id,
g.generic_name,g.generic_code,sic.control_type_name,sibd.exp_dt,pmd.issue_base_unit,
pmd.package_type,pmsd.qty,pmd.tax_type,sibd.mrp,pmsd.package_cp,
pds.reorder_level,gd.dept_name,pmsd.dept_id,mm.manf_code,mm.manf_name,date_trunc('day',stock_time)as stock_date,
'dummy column'::text AS checkpoint_name,tax_rate,pmd.bin
from store_item_details pmd
join store_stock_details pmsd using(medicine_id)
JOIN store_item_batch_details sibd USING(item_batch_id)
join pharmacy_medicine_category pmc ON (pmc.category_id=pmd.med_category_id)
join stores gd using(dept_id)
join manf_master mm ON (mm.manf_code=pmd.manf_name)
left join store_reorder_levels pds ON (pds.dept_id=gd.dept_id) and (pds.medicine_id=pmd.medicine_id)and (pmsd.dept_id=pds.dept_id)
and (pds.medicine_id=pmsd.medicine_id)
left join generic_name g ON (g.generic_code=pmd.generic_name)
left join store_item_controltype sic ON (sic.control_Type_id = pmd.control_type_id)
where qty>0;

DROP VIEW IF EXISTS formulary_report_view CASCADE;
CREATE OR REPLACE VIEW formulary_report_view AS
SELECT c.classification_name AS "Class", sc.sub_classification_name AS "Sub-Class",
	g.generic_name AS "Generic/Composition", m.medicine_name AS "Brand", cat.category_name AS "Type of Drug",
	mfr.manf_name AS "Company Name"
FROM store_item_details m
	JOIN manf_master mfr ON (mfr.manf_code = m.manf_name)
	JOIN generic_name g ON (g.generic_code = m.generic_name)
	JOIN pharmacy_medicine_category cat ON (cat.category_id = m.med_category_id)
	JOIN generic_classification_master c ON (c.classification_id = g.classification_id)
	JOIN generic_sub_classification_master sc ON (sc.sub_classification_id = g.sub_classification_id)
ORDER BY classification_name, sub_classification_name;


DROP VIEW IF EXISTS check_point_stock_details_view CASCADE;
CREATE VIEW check_point_stock_details_view AS
select pmd.medicine_name,pmd.medicine_id,pcd.batch_no,pmc.category_name,pmc.category_id,
g.generic_name,g.generic_code,sic.control_type_name,sibd.exp_dt,pmd.issue_base_unit,
pmd.package_type,(pcd.qty)as qty,pmd.tax_type,sibd.mrp,pmsd.package_cp,
pds.reorder_level,gd.dept_name,pcd.store_id,mm.manf_code,mm.manf_name,date_trunc('day',stock_time)as stock_date,pcm.checkpoint_name,pmd.tax_rate,
pcd.checkpoint_id,pmd.bin
from store_item_details pmd
join store_checkpoint_details pcd using(medicine_id)
join store_stock_details pmsd ON (pmsd.batch_no=pcd.batch_no)and (pmsd.medicine_id=pcd.medicine_id)and
(pmsd.dept_id=pcd.store_id)
JOIN store_item_batch_details sibd USING(item_batch_id)
join store_checkpoint_main pcm using(checkpoint_id)
join pharmacy_medicine_category pmc ON (pmc.category_id=pmd.med_category_id)
join stores gd using(dept_id)
join manf_master mm ON (mm.manf_code=pmd.manf_name)
left join store_reorder_levels pds ON (pds.dept_id=gd.dept_id) and (pds.medicine_id=pmd.medicine_id)and (pmsd.dept_id=pds.dept_id)
left join generic_name g ON (g.generic_code=pmd.generic_name)
left join store_item_controltype sic ON (sic.control_type_id = pmd.control_type_id)
where pcd.qty>0;

--
-- Details of a patient (mr_no), useful for display purposes.
-- Also includes visit status (A,I,N) for active, inactive, never active
-- and current AS well AS previous visit patient IDs.
--
DROP VIEW IF EXISTS patient_details_display_view CASCADE;
CREATE OR REPLACE VIEW  patient_details_display_view AS
SELECT pd.mr_no, pd.salutation AS salutation_id, sm.salutation, pd.patient_name, pd.middle_name, pd.last_name, pd.name_local_language,
	sm.salutation || ' ' || patient_name
        || CASE WHEN coalesce(middle_name, '') = '' THEN '' ELSE (' ' || middle_name) end
        || CASE WHEN coalesce(last_name, '') = '' THEN '' ELSE (' ' || last_name) end
	as full_name,
	pd.patient_gender, pd.dateofbirth, COALESCE(pd.dateofbirth, pd.expected_dob) AS expected_dob, pd.first_visit_reg_date AS reg_date,
	pr.reg_date AS current_visit_reg_date, pr.visit_type, pr.reference_docto_id,
	CASE
		WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 31
			THEN (floor(current_date - COALESCE(pd.dateofbirth, pd.expected_dob)))::integer
		WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 730
			THEN (floor((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/30.43))::integer
		ELSE (floor((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/365.25))::integer
	END AS age,
	CASE
		WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 31 THEN 'D'
 		WHEN (current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 730) THEN 'M'
		ELSE 'Y'
	END AS agein,
	''::text AS age_text,
	to_char(age(coalesce(dateofbirth, expected_dob)), 'FMYYY"Y"FMMM"M"') as raw_age_text,
 	pd.patient_phone,pd.patient_phone_country_code, pd.patient_care_oftext_country_code, pd.patient_phone2 AS addnl_phone, pd.patient_address, pd.patient_area,
	pd.patient_city, ci.city_name, pd.patient_state,st.state_name, ci.city_name AS cityname, st.state_name AS statename, dm.district_name, dm.district_id,
	pd.country, c.country_name, nc.country_name AS nationality_name,
	pd.custom_list5_value, custom_list6_value,
	pd.oldmrno, pd.casefile_no, custom_list4_value, pd.remarks,
	pd.patient_care_oftext AS patcontactperson, pd.relation AS patrelation,
 	pd.patient_careof_address AS pataddress, pd.next_of_kin_relation,
	pd.death_date, pd.death_time,drm.reason AS death_reason,pd.dead_on_arrival,
	pd.custom_field1, pd.custom_field2, pd.custom_field3, pd.custom_field4, pd.custom_field5,
	pd.custom_field6, pd.custom_field7, pd.custom_field8, pd.custom_field9, pd.custom_field10, pd.custom_field11,
	pd.custom_field12, pd.custom_field13,pd.custom_field14,pd.custom_field15,pd.custom_field16,pd.custom_field17,
	pd.custom_field18,pd.custom_field19, pd.sms_for_vaccination,
	pd.nationality_id, pd.patient_group, cgm.name AS patient_group_name, pd.is_unidentified_patient,
	pd.race_id,pd.religion_id, pd.blood_group_id, pd.marital_status_id,
	pd.resource_captured_from,

	-- these variable are for legacy support --
	pd.custom_field6 AS conditional_custom_field1, pd.custom_field7 AS conditional_custom_field2,
	pd.custom_field8 AS conditional_custom_field3, pd.custom_field9 AS conditional_custom_field4,
	pd.custom_field10 AS conditional_custom_field5,
	pd.custom_field11 AS clinical_field1, pd.custom_field12 AS clinical_field2, pd.custom_field13 AS clinical_field3,
	-- END of legacy variables --

	pd.other_identification_doc_id,pd.other_identification_doc_value,
	pd.custom_list1_value, pd.custom_list2_value, pd.custom_list3_value,pd.custom_list7_value,pd.custom_list8_value,pd.custom_list9_value,
	pd.custom_field5 AS patient_source_category, pd.custom_field4 AS patient_category,
	pd.custom_field4 AS patient_category_custom_field,
	pd.patient_category_id, pd.category_expiry_date, pcm.category_name AS patient_category_name,
	pd.patient_consultation_info,pr.primary_sponsor_id,
	CASE WHEN pd.patient_photo IS NULL THEN 'N' ELSE 'Y' END AS patient_photo_available, length(patient_photo) AS photo_size,
	pd.previous_visit_id, pd.visit_id, pd.no_allergies, pd.med_allergies, pd.food_allergies, pd.other_allergies,
	pd.vip_status, pd.passport_no, pd.passport_validity, pd.passport_issue_country, pd.visa_validity, pd.family_id,
	pd.government_identifier, pd.identifier_id, pd.portal_access, pd.email_id,
	mca.file_status, mca.indented,COALESCE(coalesce(dep.dept_name,mcu.file_user_name),'MRD Dept') AS issued_to,hcm.center_name,
	CASE WHEN (cpref.receive_communication in ('S','B') OR cpref.receive_communication is null) then 'Y' else 'N' end as send_sms, 
    CASE WHEN (cpref.receive_communication in ('E','B') OR cpref.receive_communication is null) then 'Y' else 'N' end as send_email,
    cpref.lang_code, 
	CASE
        WHEN visit_id IS NOT NULL THEN 'A'::text
        WHEN previous_visit_id IS NOT NULL THEN 'I'::text
        ELSE 'N'::text END AS visit_status, pd.mod_time as patient_mod_time, pd.original_mr_no,pd.mobile_password,pd.mobile_access, pr.center_id, cgm.emr_access AS mandate_emr_comments,doc.doctor_name
FROM  patient_details pd
	JOIN confidentiality_grp_master cgm ON (cgm.confidentiality_grp_id = pd.patient_group)
	LEFT JOIN patient_registration pr ON(pd.visit_id = pr.patient_id)
	LEFT JOIN contact_preferences cpref on (pd.mr_no = cpref.mr_no)
	LEFT JOIN salutation_master sm ON pd.salutation= sm.salutation_id
	LEFT JOIN country_master c ON pd.country = c.country_id
	LEFT JOIN country_master nc ON pd.nationality_id = nc.country_id
	LEFT JOIN city ci ON pd.patient_city= ci.city_id
	LEFT JOIN state_master st ON pd.patient_state= st.state_id
	LEFT JOIN district_master dm ON (dm.district_id = ci.district_id AND dm.state_id = st.state_id)
	LEFT JOIN patient_category_master pcm ON (pcm.category_id = pd.patient_category_id)
	LEFT OUTER JOIN mrd_casefile_attributes mca ON mca.mr_no = pd.mr_no
	LEFT OUTER JOIN department dep ON (dep.dept_id= mca.issued_to_dept)
	LEFT OUTER JOIN mrd_casefile_users mcu ON (mcu.file_user_id = mca.issued_to_user)
	LEFT JOIN death_reason_master drm ON (drm.reason_id=pd.death_reason_id)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id=pr.center_id)
	LEFT JOIN doctors doc ON (doc.doctor_id=pr.doctor)
WHERE ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )
;


DROP VIEW IF EXISTS deposits_totals_view CASCADE;
DROP VIEW IF EXISTS deposits_set_off_totals_view CASCADE;

DROP VIEW IF EXISTS deposit_dates_view CASCADE;
CREATE VIEW deposit_dates_view AS
SELECT mr_no, date(min(display_date)) AS first_deposit_date, date(max(display_date)) AS last_deposit_date, SUM(COALESCE(credit_card_commission_amount,0)) AS credit_card_commission_amount, 
SUM(COALESCE(credit_card_commission_percentage,0)) AS credit_card_commission_percentage
FROM receipts LEFT JOIN card_type_master using (card_type_id) WHERE is_deposit
GROUP BY mr_no;

--
-- Since deposit_setoff_total table has only realized deposits (commonly used)
-- use this view to get the unrealized totals.
--
DROP VIEW IF EXISTS deposits_unrealized_totals_view CASCADE;
CREATE VIEW deposits_unrealized_totals_view AS
SELECT mr_no, sum(amount) AS unrealized_amount
FROM receipts
WHERE realized != 'Y' AND is_deposit 
GROUP BY mr_no;

-- Clean up unused view to run precision_3.sql
DROP VIEW IF EXISTS deposits_realized_totals_view CASCADE;

DROP VIEW IF EXISTS patient_deposit_details_view CASCADE;
CREATE VIEW patient_deposit_details_view AS
SELECT pd.mr_no, pd.salutation, pd.salutation_id, pd.patient_name, pd.last_name, pd.patient_gender,
	pd.patient_phone, pd.full_name,
	pd.patient_city, pd.city_name, pd.patient_state, pd.state_name, pd.country, pd.country_name,
	pd.patient_area, pd.dateofbirth, pd.expected_dob, pd.age, pd.agein, pd.visit_status,
	COALESCE(dst.hosp_total_deposits,0) AS hosp_total_deposits,
	COALESCE(dut.unrealized_amount,0) AS hosp_unrealized_amount,
	COALESCE(dst.hosp_total_setoffs,0) AS hosp_total_setoffs,
	COALESCE(dst.hosp_total_balance,0) AS hosp_total_balance
FROM patient_details_display_view pd
	LEFT JOIN deposit_setoff_total dst ON (pd.mr_no=dst.mr_no)
	LEFT JOIN LATERAL (SELECT mr_no, sum(amount) AS unrealized_amount
FROM receipts
WHERE realized != 'Y' AND is_deposit AND receipts.mr_no=pd.mr_no
GROUP BY mr_no )dut ON (pd.mr_no = dut.mr_no)
;

--
-- Deposit details per bill: bill set off, total deposits, and total set off
--
DROP VIEW IF EXISTS bill_deposit_details_view CASCADE;
CREATE VIEW bill_deposit_details_view AS
SELECT b.bill_no, b.visit_id, pr.mr_no,  b.bill_type,b.restriction_type, b.status, b.deposit_set_off, b.ip_deposit_set_off,
	COALESCE(dst.hosp_total_deposits,0) AS hosp_total_deposits,
	COALESCE(dst.hosp_total_setoffs,0) AS hosp_total_setoffs
FROM bill b
	LEFT JOIN patient_registration pr ON b.visit_id = pr.patient_id
	LEFT JOIN deposit_setoff_total dst ON (dst.mr_no = pr.mr_no);

DROP VIEW IF EXISTS ip_deposits_view CASCADE;
CREATE OR REPLACE VIEW ip_deposits_view AS
SELECT
	ipdeposits.*,
	COALESCE(
		setoffs.total_ip_set_offs, 0) AS total_ip_set_offs,
	COALESCE(
		non_ip_bill_setoffs.total_ip_set_offs_non_ip_bills, 0) AS total_ip_set_offs_non_ip_bill
FROM (
	SELECT
		pd.mr_no,
		COALESCE(total_ip_deposits, 0) AS total_ip_deposits,
		COALESCE(ip_unrealized_amount, 0) AS ip_unrealized_amount
	FROM
		patient_ip_deposits_view pd
	LEFT JOIN LATERAL (
		SELECT
			sum(COALESCE(pds.amount, 0)) AS ip_unrealized_amount
		FROM
			patient_ip_deposits_view pds
		WHERE
			pds.realized <> 'Y'::bpchar
			AND pds.mr_no = pd.mr_no) AS ip_unra ON TRUE
	LEFT JOIN LATERAL (
		SELECT
			sum(COALESCE(pas.amount, 0)) AS total_ip_deposits
		FROM
			patient_ip_deposits_view pas
		WHERE
			pas.realized = 'Y'::bpchar
			AND pas.mr_no = pd.mr_no) AS total_ip_dep ON TRUE
	GROUP BY
		pd.mr_no,
		total_ip_deposits,
		ip_unrealized_amount) AS ipdeposits
	LEFT JOIN LATERAL (
		SELECT
			pr.mr_no,
			sum(b.ip_deposit_set_off) AS total_ip_set_offs
		FROM
			bill b
			JOIN patient_registration pr ON (b.visit_id = pr.patient_id)
		WHERE
			pr.visit_type = 'i'
			AND b.ip_deposit_set_off > 0
			AND pr.mr_no = ipdeposits.mr_no
		GROUP BY
			pr.mr_no) AS setoffs ON TRUE
	LEFT JOIN LATERAL (
		SELECT
			pr.mr_no,
			sum(b.ip_deposit_set_off) AS total_ip_set_offs_non_ip_bills
		FROM
			bill b
			JOIN patient_registration pr ON (b.visit_id = pr.patient_id)
		WHERE
			pr.visit_type != 'i'
			AND b.ip_deposit_set_off > 0
			AND pr.mr_no = ipdeposits.mr_no
		GROUP BY
			pr.mr_no) AS non_ip_bill_setoffs ON TRUE
ORDER BY
	mr_no;


--
-- day book view: daybook = all money inflow/outflow put together.
--
DROP VIEW IF EXISTS daybook_view CASCADE;
CREATE OR REPLACE VIEW daybook_view AS
SELECT CASE WHEN NOT is_deposit THEN 
			CASE WHEN r.receipt_type='R' AND r.tpa_id IS NULL THEN 'Bill Receipts' 
			WHEN r.receipt_type='F' AND r.tpa_id IS NULL THEN 'Bill Refunds'
			WHEN r.receipt_type='R' AND r.tpa_id IS NOT NULL THEN 'Sponsor Receipts' ELSE 'Sponsor Refunds' END ELSE 'Deposits' END AS main_type, CASE WHEN r.receipt_type='R'
	AND r.tpa_id IS NOT NULL THEN 'S' ELSE r.receipt_type END AS payment_type, CASE WHEN r.is_settlement THEN 'S' ELSE 'A' END AS sub_type,
	r.payment_mode_id ,r.card_type_id,
	pm.payment_mode ,ctm.card_type, r.bank_name, r.reference_no,
	r.receipt_id, br.receipt_no, b.bill_no, b.bill_type, b.visit_type AS visittype,
	pd.mr_no, pr.patient_id, pr.op_type,
	COALESCE (get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name),
		prc.customer_name, isr.patient_name) AS patname, br.username AS username,
	r.counter, c.counter_no,CASE WHEN c.counter_type='B' THEN 'Billing' ELSE 'Pharmacy' END AS counter_type,
	CASE WHEN c.collection_counter='Y' THEN 'Yes' ELSE 'No' END AS collection_counter,
	r.amount AS amt, r.display_date AS date,
	r.tds_amount AS tds_amt,
	r.bank_batch_no, r.card_auth_code, r.card_holder_name, r.currency_id, r.exchange_rate,
	r.exchange_date, r.currency_amt, r.card_exp_date AS card_expdate, r.card_number, fc.currency, hcm.center_name,
	CASE WHEN br.sponsor_index = 'P' THEN 'Primary' WHEN br.sponsor_index = 'S' THEN 'Secondary' END AS sponsor_type,category_name AS patient_category 
	FROM receipts r
    JOIN bill_receipts br  ON br.receipt_no=r.receipt_id
	JOIN bill b ON b.bill_no = br.bill_no
	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
	LEFT JOIN patient_details pd ON pr.mr_no = pd.mr_no
	LEFT JOIN patient_category_master pcm ON(pcm.category_id = pr.patient_category_id)
	LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
	LEFT JOIN store_retail_customers prc ON prc.customer_id = b.visit_id
	LEFT JOIN incoming_sample_registration isr ON isr.incoming_visit_id = b.visit_id
	JOIN counters c ON c.counter_id = r.counter
	JOIN payment_mode_master pm ON (r.payment_mode_id = pm.mode_id)
	JOIN hospital_center_master hcm ON (c.center_id=hcm.center_id)
	LEFT JOIN card_type_master ctm ON (r.card_type_id = ctm.card_type_id)
	LEFT JOIN foreign_currency fc ON (fc.currency_id = r.currency_id)
	WHERE ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) )

UNION ALL
SELECT 'Payments' AS main_type, '' AS payment_type, payment_type AS sub_type,
	p.payment_mode_id,p.card_type_id,pm.payment_mode, ctm.card_type, p.bank AS bank_name, p.reference_no,
	voucher_no,'' AS receipt_no, '' AS bill_no, '' AS bill_type, '' AS visit_type, '' AS mr_no, '' AS patient_id,'' AS op_type,
	CASE
		WHEN payment_type='D' THEN (SELECT doctor_name FROM doctors WHERE  doctor_id=payee_name)
		WHEN payment_type='R' THEN (SELECT doctor_name FROM  doctors WHERE doctor_id=payee_name)
		WHEN payment_type='F' THEN (SELECT referal_name FROM  referral where referal_no=payee_name)
		WHEN payment_type='O' THEN (SELECT oh_name FROM  outhouse_master where oh_id=payee_name)
		WHEN payment_type='P' THEN (SELECT doctor_name FROM doctors WHERE  doctor_id=payee_name)
		WHEN payment_type='S' THEN (SELECT supplier_name FROM supplier_master WHERE  supplier_code=payee_name)
		ELSE payee_name END AS patname, username,
	counter, c.counter_no,
	CASE WHEN c.counter_type='B' THEN 'Billing' ELSE 'Pharmacy' END AS counter_type,
	CASE WHEN c.collection_counter='Y' THEN 'Yes' ELSE 'No' END AS collection_counter,
	(0-amount) AS amt, date, 0.00 AS tds_amt,
	null, null, null, null, null,
	null, null, null, null, null, hcm.center_name AS collection_center_name, null,null AS patient_category
FROM payments p
	JOIN counters c ON c.counter_id = p.counter
	JOIN payment_mode_master pm ON (p.payment_mode_id = pm.mode_id)
	JOIN hospital_center_master hcm ON (c.center_id=hcm.center_id)
	LEFT JOIN card_type_master ctm ON (p.card_type_id = ctm.card_type_id)

UNION ALL
	SELECT 'Consolidated Claim Receipts' AS main_type, 'S' AS payment_type,
	'Consolidated Claim Receipts' AS sub_type, cs.payment_mode_id,cs.card_type_id,
	pm.payment_mode,  ctm.card_type, cs.bank_name, cs.reference_no,
	receipt_no,null, '' AS sponsor_bill_no, null AS bill_type, '' AS visit_type, '' AS mr_no,
	'' AS patient_id, '' AS op_type, tpa_name||','||insurance_co_name AS pat_name, cs.username,
	cs.counter, c.counter_no,
	CASE WHEN c.counter_type='B' THEN 'Billing' ELSE 'Pharmacy' END AS counter_type,
	CASE WHEN c.collection_counter='Y' THEN 'Yes' ELSE 'No' END AS collection_counter,
	amount , display_date, 0,
	cs.bank_batch_no, cs.card_auth_code, cs.card_holder_name, cs.currency_id, cs.exchange_rate,
	cs.exchange_date, cs.currency_amt, cs.card_expdate, cs.card_number, fc.currency,
	hcm.center_name AS collection_center_name, null,null AS patient_category
	FROM insurance_claim_receipt cs
	LEFT JOIN tpa_master tp ON (tp.tpa_id = cs.tpa_id)
	LEFT JOIN insurance_company_master ic ON (ic.insurance_co_id = cs.insurance_co_id)
	JOIN counters c ON c.counter_id = cs.counter
	JOIN payment_mode_master pm ON (cs.payment_mode_id = pm.mode_id)
	JOIN hospital_center_master hcm ON (c.center_id=hcm.center_id)
	LEFT JOIN card_type_master ctm ON (cs.card_type_id = ctm.card_type_id)
	LEFT JOIN foreign_currency fc ON (fc.currency_id = cs.currency_id)
;

DROP VIEW IF EXISTS patient_details_area_view CASCADE;
CREATE VIEW  patient_details_area_view AS
SELECT pd.mr_no,
	get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS patient_full_name,
    pd.patient_address, pd.patient_area AS area, ci.city_name, st.state_name, c.country_name,
	pd.patient_phone, pd.patient_gender,
	get_patient_age(pd.dateofbirth, pd.expected_dob) AS age,
	get_patient_age_in(pd.dateofbirth, pd.expected_dob) AS agein,
	pra.reg_date AS date
FROM patient_details pd
	LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
	LEFT JOIN patient_registration pra ON pra.patient_id= pd.visit_id
	LEFT JOIN country_master c ON pd.country = c.country_id
	LEFT JOIN city ci ON pd.patient_city= ci.city_id
	LEFT JOIN state_master st ON pd.patient_state= st.state_id
WHERE ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )
;

-- Store Stock Ledger View

DROP VIEW IF EXISTS stores_stock_ledger CASCADE;
CREATE VIEW stores_stock_ledger AS
(((((( SELECT pgm.store_id, pgm.grn_date AS txn_date, pgm.grn_no AS txn_ref, pg.batch_no, pgm.user_name,
pg.medicine_id, 'Purchase' AS txn_type,sum(pg.bonus_qty) AS bonus_qty, sum(pg.billed_qty) AS qty,sum(pg.billed_qty+pg.bonus_qty)AS total_qty,
s.supplier_name AS details, sibd.mrp, issue_base_unit,package_uom
   FROM store_grn_main pgm
   JOIN store_grn_details pg USING (grn_no)
   JOIN store_invoice pi USING (supplier_invoice_id)
   JOIN supplier_master s ON s.supplier_code::text = pi.supplier_id::text
   JOIN store_item_details USING (medicine_id)
   JOIN store_item_batch_details sibd ON(sibd.item_batch_id = pg.item_batch_id)
  WHERE pgm.debit_note_no IS NULL
  GROUP BY pgm.store_id, s.supplier_name, pgm.grn_date, pgm.grn_no, pgm.user_name, pg.batch_no, pg.medicine_id, sibd.mrp,issue_base_unit,package_uom
UNION
 SELECT pgm.store_id, pgm.grn_date AS txn_date, pgm.debit_note_no AS txn_ref, pg.batch_no, pgm.user_name,
 pg.medicine_id, 'Debit' AS txn_type,sum(pg.bonus_qty) AS bonus_qty, sum(pg.billed_qty) AS qty,sum(pg.billed_qty+pg.bonus_qty)AS total_qty,
 s.supplier_name AS details, sibd.mrp,issue_base_unit,package_uom
   FROM store_grn_main pgm
   JOIN store_grn_details pg USING (grn_no)
   JOIN store_debit_note pdn USING (debit_note_no)
   JOIN supplier_master s ON s.supplier_code::text = pdn.supplier_id::text
   JOIN store_item_details USING (medicine_id)
   JOIN store_item_batch_details sibd ON(sibd.item_batch_id = pg.item_batch_id)
  WHERE pgm.debit_note_no IS NOT NULL
  GROUP BY pgm.store_id, s.supplier_name, pgm.grn_date, pgm.debit_note_no, pgm.user_name, pg.batch_no, pg.medicine_id, sibd.mrp,issue_base_unit,package_uom)
UNION
 SELECT psrm.store_id, psrm.date_time AS txn_date, psrm.return_no::text AS txn_ref, sibd.batch_no, psrm.user_name, psr.medicine_id,
 	'Supplier Returns' AS txn_type, 0 AS bonus_qty, - sum(psr.qty) AS qty, - sum(psr.qty) AS total_qty, s.supplier_name AS details,
 	sibd.mrp, issue_base_unit,sid.package_uom
   FROM store_supplier_returns_main psrm
   JOIN store_supplier_returns psr USING (return_no)
   JOIN supplier_master s ON s.supplier_code::text = psrm.supplier_id::text
   JOIN store_item_details sid USING (medicine_id)
   JOIN store_item_batch_details sibd ON(sibd.item_batch_id = psr.item_batch_id)
  WHERE psrm.orig_return_no IS NULL
  GROUP BY psrm.store_id, s.supplier_name, psrm.date_time, psrm.return_no, psrm.user_name, sibd.batch_no, psr.medicine_id, sibd.mrp, issue_base_unit, sid.package_uom)
UNION
 SELECT psrm.store_id, psrm.date_time AS txn_date, psrm.return_no::text AS txn_ref, sibd.batch_no, psrm.user_name, psr.medicine_id,
 	'Replace' AS txn_type, 0 AS bonus_qty, - sum(psr.qty) AS qty, - sum(psr.qty) AS total_qty, s.supplier_name AS details,
 	sibd.mrp, issue_base_unit,sid.package_uom
   FROM store_supplier_returns_main psrm
   JOIN store_supplier_returns psr USING (return_no)
   JOIN supplier_master s ON s.supplier_code::text = psrm.supplier_id::text
   JOIN store_item_details sid USING (medicine_id)
   JOIN store_item_batch_details sibd ON(sibd.item_batch_id = psr.item_batch_id)
  WHERE psrm.orig_return_no IS NOT NULL
  GROUP BY psrm.store_id, s.supplier_name, psrm.date_time, psrm.return_no, psrm.user_name, sibd.batch_no, psr.medicine_id, sibd.mrp,issue_base_unit,sid.package_uom)
UNION
 SELECT psm.store_id, psm.date_time AS txn_date, psm.sale_id::text AS txn_ref, sibd.batch_no, psm.username AS user_name, ps.medicine_id,
        CASE
            WHEN psm.type = 'S'::bpchar THEN 'Sales'::text
            ELSE 'Returns'::text
        END AS txn_type, 0 AS bonus_qty, 0::numeric - sum(ps.quantity) AS qty, 0::numeric - sum(ps.quantity) AS total_qty, COALESCE(patient_details.patient_name::text ||
        CASE
            WHEN b.visit_type = 'o'::bpchar THEN ' (OP)'::text
            ELSE ' (IP)'::text
        END, store_retail_customers.customer_name::text || ' (Retail)'::text) AS details, sibd.mrp, issue_base_unit,package_uom
   FROM store_sales_main psm
   JOIN store_sales_details ps USING (sale_id)
   JOIN bill b USING (bill_no)
   JOIN store_item_details USING (medicine_id)
   JOIN store_item_batch_details sibd ON(sibd.item_batch_id = ps.item_batch_id)
   LEFT JOIN patient_registration pr ON b.visit_id::text = pr.patient_id::text
   LEFT JOIN patient_details USING (mr_no)
   LEFT JOIN store_retail_customers ON b.visit_id::text = store_retail_customers.customer_id::text
 WHERE (patient_confidentiality_check(COALESCE(patient_details.patient_group, 0), patient_details.mr_no))
  GROUP BY psm.store_id, psm.date_time, psm.sale_id, psm.type, psm.username, patient_details.patient_name, b.visit_type, store_retail_customers.customer_name, sibd.batch_no, ps.medicine_id, sibd.mrp, issue_base_unit,package_uom)
UNION
 SELECT isum.dept_from AS store_id, isum.date_time AS txn_date, isum.user_issue_no::text AS txn_ref, sibd.batch_no, isum.username AS user_name, isu.medicine_id,
 		CASE
 			WHEN isum.user_type = 'Hospital' THEN 'User Issues'
 			ELSE 'Patient Issues'
 		END AS txn_type, 0 AS bonus_qty, - sum(isu.qty) AS qty, - sum(isu.qty) AS total_qty, isum.issued_to AS details, sibd.mrp, issue_base_unit,sid.package_uom
   FROM stock_issue_main isum
   JOIN stock_issue_details isu USING (user_issue_no)
   JOIN store_item_details sid USING (medicine_id)
   JOIN store_item_batch_details sibd ON(sibd.item_batch_id = isu.item_batch_id)
  GROUP BY isum.dept_from, isum.date_time, isum.user_issue_no, isum.username, isum.issued_to, sibd.batch_no, isu.medicine_id, sibd.mrp, isum.user_type, issue_base_unit,sid.package_uom)
UNION
 SELECT isurm.dept_to AS store_id, isurm.date_time AS txn_date, isurm.user_return_no::text AS txn_ref, sibd.batch_no, isurm.username AS user_name, isur.medicine_id,
 		CASE
 			WHEN sim.user_type = 'Hospital' THEN 'User Returns'
 			ELSE 'Patient Returns'
 		END AS txn_type, 0 AS bonus_qty, sum(isur.qty) AS qty, sum(isur.qty) AS total_qty, isurm.returned_by AS details,
 		sibd.mrp,issue_base_unit,package_uom
   FROM store_issue_returns_main isurm
   JOIN store_issue_returns_details isur USING (user_return_no)
   JOIN store_item_details USING (medicine_id)
   JOIN store_item_batch_details sibd ON(sibd.item_batch_id = isur.item_batch_id)
   LEFT JOIN stock_issue_main sim ON (sim.user_issue_no = isurm.user_issue_no)
  GROUP BY isurm.dept_to, isurm.date_time, isurm.user_return_no, isurm.username,isurm.returned_by, sibd.batch_no, isur.medicine_id, sibd.mrp,sim.user_type,issue_base_unit,package_uom)
UNION
 SELECT psm.store_id, psm.date_time AS txn_date, psm.adj_no::text AS txn_ref, sibd.batch_no, psm.username AS user_name, ps.medicine_id, 'Adjustment' AS txn_type, 0 AS bonus_qty, sum(ps.qty) AS qty,
        CASE
            WHEN ps.type = 'A'::bpchar THEN sum(ps.qty)
            WHEN ps.type = 'R'::bpchar THEN - sum(ps.qty)
            ELSE sum(ps.qty)::numeric
        END AS total_qty, description AS details, sibd.mrp,issue_base_unit,package_uom
   FROM store_adj_main psm
   JOIN store_adj_details ps USING (adj_no)
   JOIN store_item_details USING (medicine_id)
   JOIN store_item_batch_details sibd ON(sibd.item_batch_id = ps.item_batch_id)
  GROUP BY psm.store_id, psm.date_time, psm.adj_no, psm.username, description, ps.type, sibd.batch_no, ps.medicine_id, sibd.mrp, issue_base_unit,package_uom
UNION
 SELECT psm.store_to AS store_id, psm.date_time AS txn_date, psm.transfer_no::text AS txn_ref, sibd.batch_no, psm.username AS user_name,
 ps.medicine_id, 'Stock Transfer' AS txn_type, 0 AS bonus_qty, sum(COALESCE(NULLIF(ps.qty_recd,0),(ps.qty-ps.qty_rejected))) AS qty, 
 sum(COALESCE(NULLIF(ps.qty_recd,0),(ps.qty-ps.qty_rejected))) AS total_qty, gd.dept_name AS details,
 sibd.mrp, issue_base_unit,package_uom
   FROM store_transfer_main psm
   JOIN store_transfer_details ps USING (transfer_no)
   JOIN stores gd ON gd.dept_id = psm.store_from
   JOIN store_item_details USING (medicine_id)
   JOIN store_item_batch_details sibd ON(sibd.item_batch_id = ps.item_batch_id)
  GROUP BY psm.store_to, gd.dept_name, psm.date_time, psm.transfer_no, psm.username, 
  sibd.batch_no, ps.medicine_id, sibd.mrp, issue_base_unit,package_uom
UNION
 SELECT psm.store_from AS store_id, psm.date_time AS txn_date, psm.transfer_no::text AS txn_ref, 
 sibd.batch_no, psm.username AS user_name,
 ps.medicine_id, 'Stock Transfer' AS txn_type, 0 AS bonus_qty, sum(ps.qty-ps.qty_rejected) AS qty, 
 -sum(ps.qty-ps.qty_rejected) AS total_qty, gd.dept_name AS details,
 sibd.mrp, issue_base_unit,package_uom
   FROM store_transfer_main psm
   JOIN store_transfer_details ps USING (transfer_no)
   JOIN store_item_details USING (medicine_id)
   JOIN stores gd ON gd.dept_id = psm.store_to
   JOIN store_item_batch_details sibd ON (sibd.item_batch_id = ps.item_batch_id)
  GROUP BY psm.store_from, gd.dept_name, psm.date_time, psm.transfer_no, psm.username, 
  sibd.batch_no, ps.medicine_id, sibd.mrp, issue_base_unit,package_uom
UNION
	SELECT srum.store_id,srum.date_time AS txn_date,'' AS txn_ref,sibd.batch_no,srum.user_name,sibd.medicine_id,'Consumable' AS txn_type,
		0 AS bonus_qty,sum(srud.qty) AS qty, -sum(srud.qty) AS total_qty,s.service_name AS details, sibd.mrp, issue_base_unit,sid.package_uom
		FROM store_reagent_usage_main srum
		JOIN store_reagent_usage_details  srud USING (reagent_usage_seq)
		JOIN services s ON (s.service_id = srum.consumer_id)
		JOIN store_item_batch_details sibd ON (sibd.item_batch_id = srud.item_batch_id)
		JOIN store_item_details sid ON (sid.medicine_id = sibd.medicine_id)
	GROUP BY srum.store_id,srum.date_time,sibd.batch_no,srum.user_name,sibd.medicine_id,s.service_name, sibd.mrp, issue_base_unit,sid.package_uom
UNION
	SELECT srum.store_id,srum.date_time AS txn_date,'' AS txn_ref,sibd.batch_no,srum.user_name,sibd.medicine_id,'Reagent' AS txn_type,
		0 AS bonus_qty,sum(srud.qty) AS qty, -sum(srud.qty) AS total_qty,d.test_name AS details, sibd.mrp, issue_base_unit,sid.package_uom
		FROM store_reagent_usage_main srum
		JOIN store_reagent_usage_details  srud USING (reagent_usage_seq)
		JOIN diagnostics d ON (d.test_id = srum.consumer_id)
		JOIN store_item_batch_details sibd ON (sibd.item_batch_id = srud.item_batch_id)
		JOIN store_item_details sid ON (sid.medicine_id = sibd.medicine_id)
	GROUP BY srum.store_id,srum.date_time,sibd.batch_no,srum.user_name,sibd.medicine_id,d.test_name, sibd.mrp, issue_base_unit,sid.package_uom
UNION
	SELECT srum.store_id,srum.date_time AS txn_date,'' AS txn_ref,sibd.batch_no,srum.user_name,sibd.medicine_id,'Reagent' AS txn_type,
		0 AS bonus_qty,sum(srud.qty) AS qty, -sum(srud.qty) AS total_qty,ope.operation_name AS details, sibd.mrp, issue_base_unit,sid.package_uom
		FROM store_reagent_usage_main srum
		JOIN store_reagent_usage_details  srud USING (reagent_usage_seq)
		JOIN operation_master ope ON (ope.op_id = srum.consumer_id)
		JOIN store_item_batch_details sibd ON (sibd.item_batch_id = srud.item_batch_id)
		JOIN store_item_details sid ON (sid.medicine_id = sibd.medicine_id)
	GROUP BY srum.store_id,srum.date_time,sibd.batch_no,srum.user_name,sibd.medicine_id,ope.operation_name, sibd.mrp, issue_base_unit,sid.package_uom
UNION
	SELECT grum.store_id,grum.open_date AS txn_date,'' AS txn_ref,sibd.batch_no,grum.user_name,
		sibd.medicine_id, 'Stock Consumption' AS txn_type,0 AS bonus_qty, sum(grud.qty) AS qty,
		-sum(grud.qty) AS total_qty, sid.medicine_name AS details, sibd.mrp, issue_base_unit,
		sid.package_uom
		FROM general_reagent_usage_main grum
		JOIN general_reagent_usage_details grud USING (consumption_id)
		JOIN store_item_batch_details sibd ON (sibd.item_batch_id = grud.item_batch_id)
		JOIN store_item_details sid ON (sid.medicine_id = sibd.medicine_id)
	GROUP BY grum.store_id,grum.open_date,sibd.batch_no,grum.user_name,sibd.medicine_id,sid.medicine_name,
	sibd.mrp, issue_base_unit,sid.package_uom;

CREATE OR REPLACE FUNCTION misc_payees_trigger()  RETURNS trigger AS $BODY$
BEGIN

	IF NEW.payment_type = 'C' THEN
		INSERT INTO misc_payees (SELECT NEW.payee_name WHERE NOT EXISTS (
				SELECT misc_payee_name FROM misc_payees WHERE misc_payee_name = NEW.payee_name));
	END IF;
	RETURN NEW;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS misc_payees_trigger ON payments_details;
CREATE TRIGGER misc_payees_trigger AFTER INSERT ON payments_details
	FOR EACH ROW EXECUTE PROCEDURE misc_payees_trigger();

DROP VIEW IF EXISTS payeenames_view CASCADE;
CREATE VIEW payeenames_view AS
		SELECT misc_payee_name AS payee_name, misc_payee_name AS payee_id, 'A' AS status, (misc_payee_name || '(Misc)') AS payee_for_payment,0 AS center_id FROM misc_payees
	UNION
		SELECT doctor_name AS payee_name, d.doctor_id AS payee_id, d.status, doctor_name AS payee_for_payment,center_id FROM doctors d
			LEFT JOIN doctor_center_master dcm ON (d.doctor_id = dcm.doctor_id)
			 WHERE d.status='A' AND dcm.status='A'
	UNION
		SELECT referal_name AS payee_name, referal_no AS payee_id, status, referal_name AS payee_for_payment,0 AS center_id FROM referral WHERE status='A'
	UNION
		SELECT COALESCE(om.oh_name, hcm.center_name) AS payee_name, COALESCE(om.oh_id, hcm.center_id::text) AS payee_id, dom.status,
		COALESCE(om.oh_name, hcm.center_name) AS payee_for_payment,0 AS center_id
		FROM diag_outsource_master dom
		LEFT JOIN outhouse_master om ON (om.oh_id = dom.outsource_dest)
		LEFT JOIN hospital_center_master hcm ON (hcm.center_id::text = dom.outsource_dest)
		WHERE dom.status='A'
	UNION
		SELECT supplier_name AS payee_name, supplier_code AS payee_id, status::character, supplier_name AS payee_for_payment,0 AS center_id FROM supplier_master WHERE status='A'
;

DROP VIEW IF EXISTS package_uoms_view CASCADE;
CREATE VIEW package_uoms_view AS
SELECT DISTINCT package_uom FROM package_issue_uom ORDER BY package_uom;

DROP VIEW IF EXISTS issue_uoms_view CASCADE;
CREATE VIEW issue_uoms_view AS
SELECT DISTINCT issue_uom FROM package_issue_uom ORDER BY issue_uom;

DROP VIEW IF EXISTS mrd_diagnosis_view CASCADE;
CREATE OR REPLACE VIEW mrd_diagnosis_view AS
SELECT pfoo.visit_id , primary_diagnosis, primary_diag_icd_code, primary_code_type,
			secondary_diagnosis,secondary_diag_icd_code,secondary_code_type FROM
  (SELECT visit_id, description AS primary_diagnosis,
  					icd_code AS primary_diag_icd_code,
  					code_type AS primary_code_type FROM mrd_diagnosis WHERE diag_type = 'P') AS pfoo
  JOIN (SELECT visit_id, textcat_linecat( description ) AS secondary_diagnosis,
  						 textcat_linecat( icd_code ) AS secondary_diag_icd_code,
						 textcat_linecat( code_type ) AS secondary_code_type
  			FROM mrd_diagnosis WHERE diag_type = 'S' GROUP BY visit_id) AS sfoo ON (sfoo.visit_id = pfoo.visit_id)
;

-- Simple view to get primary and secondary sponsor type of a visit
DROP VIEW IF EXISTS patient_sponsor_details_view CASCADE;
CREATE OR REPLACE VIEW patient_sponsor_details_view AS
SELECT pra.patient_id, pra.plan_id, pra.primary_sponsor_id, pra.secondary_sponsor_id,
	tpa.tpa_name, stpa.tpa_name AS sec_tpa_name,
	tpa.sponsor_type, stpa.sponsor_type AS sec_sponsor_type
FROM patient_registration pra
	LEFT JOIN tpa_master tpa ON tpa.tpa_id = pra.primary_sponsor_id
    LEFT JOIN tpa_master stpa ON stpa.tpa_id = pra.secondary_sponsor_id
;

--
-- View to get the patient details and visit details. Contains almost all the visit
-- related values of the patient, with all appropriate master joins.
--
DROP VIEW IF EXISTS patient_details_ext_view CASCADE;
CREATE OR REPLACE VIEW patient_details_ext_view AS
SELECT
	/* Patient fields */
	pd.mr_no, pd.salutation AS salutation_id, sm.salutation, pd.patient_name, pd.middle_name, pd.last_name,pd.name_local_language,
	sm.salutation || ' ' || patient_name
        || case when coalesce(middle_name, '') = '' then '' else (' ' || middle_name) end
        || case when coalesce(last_name, '') = '' then '' else (' ' || last_name) end
	as full_name, pd.mobile_access,
	pd.patient_gender, pd.dateofbirth, COALESCE(pd.dateofbirth, pd.expected_dob) AS expected_dob,
	CASE
		WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 31
			THEN (floor(current_date - COALESCE(pd.dateofbirth, pd.expected_dob)))::integer
		WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 730
			THEN (floor((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/30.43))::integer
		ELSE (floor((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/365.25))::integer
	END AS age,
	CASE
		WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 31 THEN 'D'
 		WHEN (current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 730) THEN 'M'
		ELSE 'Y'
	END AS agein, ''::text AS age_text,
	to_char(age(coalesce(dateofbirth, expected_dob)), 'FMYYY"Y"FMMM"M"') as raw_age_text,
	pd.patient_phone, pd.patient_phone2 AS addnl_phone, pd.patient_address, pd.patient_area,
	pd.patient_city, ci.city_name AS cityname, pd.patient_state,st.state_name AS statename,
	pd.country, cnm.country_name, ci.city_name, st.state_name, dm.district_name, 
	nc.country_name AS nationality_name,
	pd.oldmrno, pd.casefile_no, pd.remarks, pd.resource_captured_from,
	pd.patient_care_oftext, pd.patient_careof_address, pd.relation, pd.next_of_kin_relation,
	pd.death_date, pd.death_time,drm.reason AS death_reason,pd.dead_on_arrival,
	pd.custom_field1, pd.custom_field2, pd.custom_field3, pd.custom_field4, pd.custom_field5, pd.custom_field6,
	pd.custom_field7, pd.custom_field8, pd.custom_field9, pd.custom_field10, pd.custom_field11, pd.custom_field12,
	pd.custom_field13,custom_field14,pd.custom_field15,pd.custom_field16,pd.custom_field17,
	pd.custom_field18,pd.custom_field19, pd.original_mr_no, pd.patient_group, cgm.name as patient_group_name, 
	pra.is_er_visit,
	pd.race_id,pd.religion_id, pd.blood_group_id, pd.marital_status_id,

	-- these variables provided for legacy support(which are used in customizable print). so dont remove them
	pd.custom_field5 AS patient_source_category, pd.custom_field4 AS patient_category_custom_field,
	pd.custom_field6 AS conditional_custom_field1, pd.custom_field7 AS conditional_custom_field2,
	pd.custom_field8 AS conditional_custom_field3, pd.custom_field9 AS conditional_custom_field4,
	pd.custom_field10 AS conditional_custom_field5,	pd.custom_field11 AS clinical_field1,
	pd.custom_field12 AS clinical_field2, pd.custom_field13 AS clinical_field3,
	-- END of legacy variables --

	pd.custom_list1_value, pd.custom_list2_value, pd.custom_list3_value,pd.custom_list4_value,
	custom_list5_value,custom_list6_value,custom_list7_value,custom_list8_value,custom_list9_value,

	-- visit custom field values
	pra.visit_custom_list1, pra.visit_custom_list2,

	-- visit custom list values
	pra.visit_custom_field1, pra.visit_custom_field2, pra.visit_custom_field3,pra.visit_custom_field4,pra.visit_custom_field5,pra.visit_custom_field6,
	pra.visit_custom_field7,pra.visit_custom_field8,pra.visit_custom_field9,

	pd.patient_category_id, pd.category_expiry_date, pcm.category_name, pd_pcm.category_name AS patient_category_name,
	pd.patient_consultation_info,
	CASE WHEN pd.patient_photo IS NULL THEN 'N' ELSE 'Y' END AS patient_photo_available,
	pd.previous_visit_id, pd.visit_id, pd.no_allergies, pd.med_allergies, pd.food_allergies, pd.other_allergies, pd.vip_status,
	pd.government_identifier, pd.identifier_id, pd.portal_access, pd.email_id,
	pd.passport_no, pd.passport_validity, pd.passport_issue_country, pd.visa_validity, pd.family_id, pd.nationality_id, 
	mrd.file_status, mrd.indented, coalesce(depc.dept_name,mcu.file_user_name) AS issued_to, pd.mod_time AS patient_mod_time,
	gim.remarks AS govt_type_label, 
	/* Visit related fields */
	pra.patient_id, pra.status AS visit_status, pra.visit_type, pra.revisit, pra.reg_date, pra.reg_time,
	pra.op_type, otn.op_type_name, pra.main_visit_id, pra.use_drg, pra.drg_code, pra.use_perdiem, pra.per_diem_code,
	pra.mlc_status, pra.patient_category_id AS patient_category, hcm.center_name,hcm.center_id, hcm.health_authority, hcm.center_code,
	hcm.center_address , hcm.center_contact_phone , hcm.tin_number AS center_tin_number,
	pra.encounter_type, pra.patient_care_oftext AS patcontactperson, pra.relation AS patrelation,
	pra.patient_careof_address AS pataddress,
	pra.complaint, pra.analysis_of_complaint,
	pra.doctor, dr.doctor_name, dr.specialization, dr.doctor_type, dr.doctor_address,
	dr.doctor_mobile, dr.doctor_mail_id, dr.qualification, dr.registration_no, dr.res_phone,
	dr.clinic_phone, dr.doctor_license_number,
	pra.admitted_dept, admdep.dept_name AS admitted_dept_name, /* original dept admitted to */
	pra.dept_name AS dept_id, dep.dept_name,  pra.unit_id, dum.unit_name,	/* current dept */
	pra.org_id, od.org_name, od.store_rate_plan_id, od.pharmacy_discount_percentage,
	od.pharmacy_discount_type,
	pra.bed_type AS bill_bed_type, bn.bed_type AS alloc_bed_type, bn.bed_name AS alloc_bed_name,
	pra.ward_id AS reg_ward_id, wnr.ward_name AS reg_ward_name, wn.ward_name AS alloc_ward_name,
	ad.admit_date AS bed_start_date, date(ad.finalized_time) AS bed_end_date,
	pra.discharge_doc_id AS dis_doc_id, pra.discharge_format AS dis_format,
	pra.discharge_flag, pra.classification, pra.discharge_doctor_id, pra.discharge_date, pra.discharge_time,
	pra.discharge_finalized_date AS dis_finalized_date, pra.discharge_finalized_time AS dis_finalized_time,
	pra.discharge_finalized_user AS dis_finalized_user, dtm.discharge_type, pra.discharged_by, pra.discharge_remarks, pra.user_name AS admitted_by,
	pra.codification_status,pra.established_type, pra.disch_date_for_disch_summary, pra.disch_time_for_disch_summary,
	pra.reference_docto_id, COALESCE(drs.doctor_name, rd.referal_name) AS refdoctorname,
	pra.reg_charge_accepted,pra.ip_credit_limit_amount,
	pra.mlc_no, pra.mlc_type, pra.accident_place, pra.police_stn, pra.mlc_remarks, pra.certificate_status,
	pmd.icd_code AS primary_diagnosis_code, pmd.description AS primary_diagnosis_description,
	amd.icd_code AS admitting_diagnosis_code, amd.description AS admitting_diagnosis_description,
	(select textcat_commacat(description) from mrd_diagnosis md where (md.visit_id=pra.patient_id and diag_type='S'))
	as secondary_diagnosis_description, prmain.primary_insurance_approval AS main_visit_primary_insurance_approval,
	prmain.secondary_insurance_approval AS main_visit_secondary_insurance_approval,
	pra.primary_insurance_approval,pra.secondary_insurance_approval,
	pra.primary_sponsor_id, pra.secondary_sponsor_id,
	tpa.tpa_name, stpa.tpa_name AS sec_tpa_name,tpa.tin_number AS tpa_tin_number, stpa.tin_number AS sec_tpa_tin_number,
	tpa.sponsor_type AS sponsor_type, stpa.sponsor_type AS sec_sponsor_type,
	icm.insurance_co_name, icm.insurance_co_address,
	sicm.insurance_co_name AS sec_insurance_co_name, sicm.insurance_co_address AS sec_insurance_co_address,
	icm.tin_number AS insurance_co_tin_number, sicm.tin_number AS sec_insurance_co_tin_number,
	tpa.state AS tpa_state, tpa.city AS tpa_city, tpa.country AS tpa_country, tpa.pincode AS tpa_pincode,
	tpa.phone_no AS tpa_phone_no, tpa.mobile_no AS tpa_mobile_no, tpa.address AS tpa_address,
	pra.insurance_id, pra.category_id AS insurance_category,
	pra.plan_id, pra.prior_auth_id, pra.prior_auth_mode_id, pam.prior_auth_mode_name,
	pra.doc_id, ins.prior_auth_id AS ins_prior_auth_id,
	pra.primary_insurance_co, pra.secondary_insurance_co,
	icam.category_name AS plan_type_name, ipm.plan_exclusions, ipm.plan_notes, pipm.plan_name,sipm.plan_name AS sec_plan_name,
	pra.patient_policy_id,pra.docs_download_passcode,
	(case WHEN modact.activation_status = 'Y' THEN ppd.member_id ELSE ins.policy_no end) AS member_id,
	(case WHEN modact.activation_status = 'Y' THEN ppd.policy_number ELSE ins.insurance_no end) AS policy_number,
	(case WHEN modact.activation_status = 'Y' THEN ppd.policy_validity_start ELSE ins.policy_validity_start end) AS policy_validity_start,
	(case WHEN modact.activation_status = 'Y' THEN ppd.policy_validity_end ELSE ins.policy_validity_end end) AS policy_validity_end,
	(case WHEN modact.activation_status = 'Y' THEN ppd.policy_holder_name ELSE ins.policy_holder_name end) AS policy_holder_name,
	(case WHEN modact.activation_status = 'Y' THEN ppd.patient_relationship ELSE ins.patient_relationship end) AS patient_relationship,

	(case WHEN modact.activation_status = 'Y' THEN sppd.member_id ELSE ins.policy_no end) AS sec_member_id,
	(case WHEN modact.activation_status = 'Y' THEN sppd.policy_number ELSE ins.insurance_no end) AS sec_policy_number,
	(case WHEN modact.activation_status = 'Y' THEN sppd.policy_validity_start ELSE ins.policy_validity_start end) AS sec_policy_validity_start,
	(case WHEN modact.activation_status = 'Y' THEN sppd.policy_validity_end ELSE ins.policy_validity_end end) AS sec_policy_validity_end,
	(case WHEN modact.activation_status = 'Y' THEN sppd.policy_holder_name ELSE ins.policy_holder_name end) AS sec_policy_holder_name,
	(case WHEN modact.activation_status = 'Y' THEN sppd.patient_relationship ELSE ins.patient_relationship end) AS sec_patient_relationship,
	pcd.patient_relationship AS patient_corporate_relation, pcd.sponsor_id AS corporate_sponsor_id,
	pcd.employee_id, pcd.employee_name, pnd.sponsor_id AS national_sponsor_id,
	pnd.national_id, pnd.citizen_name, pnd.patient_relationship AS patient_national_relation,
	spcd.patient_relationship AS sec_patient_corporate_relation, spcd.sponsor_id AS sec_corporate_sponsor_id,
	spcd.employee_id AS sec_employee_id, spcd.employee_name AS sec_employee_name, spnd.sponsor_id AS sec_national_sponsor_id,
	spnd.national_id AS sec_national_id, spnd.citizen_name AS sec_citizen_name ,spnd.patient_relationship AS sec_patient_national_relation
	, pra.signatory_username,pra.collection_center_id,scc.collection_center, coalesce(ipm.require_pbm_authorization, 'N') AS require_pbm_authorization,
	pst.member_id_label AS primary_member_id_label,sst.member_id_label AS secondary_member_id_label,
	CASE WHEN pipm.limits_include_followup IS NOT NULL AND pipm.limits_include_followup = 'Y' THEN ppip.episode_limit 
		ELSE ppip.visit_limit END AS primary_approval_limit,
	CASE WHEN sipm.limits_include_followup IS NOT NULL AND sipm.limits_include_followup = 'Y' THEN spip.episode_limit 
		ELSE spip.visit_limit END AS secondary_approval_limit,
	pst.plan_type_label as primary_plan_type_label, sst.plan_type_label as secondary_plan_type_label,
	pra.primary_case_rate_id, pra.secondary_case_rate_id, pcrm.code as primary_caserate_code, 
	pcrm.code_description as primary_caserate_code_desc, scrm.code as secondary_caserate_code,
	scrm.code_description as secondary_caserate_code_desc,
	to_char(pra.reg_date, 'mm/dd/yyyy') as admission_date,
	to_char(pra.reg_time, 'HH12:MI AM') as admission_time,
	to_char(pra.discharge_date, 'mm/dd/yyyy') as disch_date,
	to_char(pra.discharge_time, 'HH12:MI AM') as disch_time
FROM patient_registration pra
   JOIN patient_details pd ON pra.mr_no = pd.mr_no
   LEFT JOIN govt_identifier_master gim ON (pd.identifier_id=gim.identifier_id)
   LEFT JOIN op_type_names otn ON (otn.op_type = pra.op_type)
   LEFT JOIN ward_names wnr ON wnr.ward_no = pra.ward_id
   LEFT JOIN salutation_master sm ON pd.salutation = sm.salutation_id
   LEFT JOIN city ci ON pd.patient_city = ci.city_id
   LEFT JOIN state_master st ON pd.patient_state = st.state_id
	LEFT JOIN district_master dm ON (dm.district_id = ci.district_id AND dm.state_id = st.state_id)
   LEFT JOIN country_master cnm ON pd.country = cnm.country_id
   LEFT JOIN country_master nc ON pd.nationality_id = nc.country_id
   LEFT JOIN department dep ON pra.dept_name = dep.dept_id
   LEFT JOIN department admdep ON pra.admitted_dept = admdep.dept_id
   LEFT JOIN dept_unit_master dum ON dum.unit_id = pra.unit_id
   LEFT JOIN discharge_type_master dtm ON(dtm.discharge_type_id = pra.discharge_type_id)
   LEFT JOIN doctors dr ON dr.doctor_id = pra.doctor
   LEFT JOIN admission ad ON ad.patient_id = pra.patient_id
   LEFT JOIN bed_names bn ON bn.bed_id = ad.bed_id
   LEFT JOIN ward_names wn ON wn.ward_no = bn.ward_no
   LEFT JOIN organization_details od ON pra.org_id = od.org_id
   LEFT JOIN doctors drs ON pra.reference_docto_id = drs.doctor_id
   LEFT JOIN referral rd ON pra.reference_docto_id = rd.referal_no
   LEFT JOIN patient_category_master pcm ON pcm.category_id = pra.patient_category_id
   LEFT JOIN patient_category_master pd_pcm ON pd_pcm.category_id=pd.patient_category_id
   LEFT JOIN tpa_master tpa ON tpa.tpa_id = pra.primary_sponsor_id
   LEFT JOIN tpa_master stpa ON stpa.tpa_id = pra.secondary_sponsor_id
   LEFT JOIN insurance_case ins ON pra.insurance_id = ins.insurance_id
   LEFT JOIN insurance_company_master icm ON icm.insurance_co_id = pra.primary_insurance_co
   LEFT JOIN insurance_company_master sicm ON sicm.insurance_co_id = pra.secondary_insurance_co
   LEFT JOIN insurance_plan_main ipm ON (pra.plan_id = ipm.plan_id)
   LEFT JOIN insurance_category_master icam  ON icam.category_id=ipm.category_id
   LEFT JOIN mrd_diagnosis pmd ON (pmd.visit_id = pra.patient_id AND pmd.diag_type = 'P')
   LEFT JOIN mrd_diagnosis amd ON (amd.visit_id = pra.patient_id AND amd.diag_type = 'A')
   LEFT JOIN patient_registration prmain ON (prmain.patient_id = pra.main_visit_id AND prmain.op_type = 'M')
   LEFT JOIN patient_insurance_plans ppip ON( ppip.patient_id = pra.patient_id AND ppip.priority = 1)
   LEFT JOIN patient_insurance_plans spip ON( spip.patient_id = pra.patient_id AND spip.priority = 2)
   LEFT JOIN insurance_plan_main pipm ON (ppip.plan_id = pipm.plan_id)
   LEFT JOIN insurance_plan_main sipm ON (spip.plan_id = sipm.plan_id)
   LEFT JOIN patient_policy_details ppd ON (ppd.mr_no = ppip.mr_no and ppd.status = 'A' AND ppip.patient_policy_id = ppd.patient_policy_id and ppip.plan_id = ppd.plan_id)
   LEFT JOIN patient_policy_details sppd ON (sppd.mr_no = ppip.mr_no and sppd.status = 'A' AND sppd.patient_policy_id = spip.patient_policy_id and spip.plan_id = sppd.plan_id)
   LEFT JOIN patient_corporate_details pcd ON (pcd.patient_corporate_id = pra.patient_corporate_id)
   LEFT JOIN patient_national_sponsor_details pnd ON (pnd.patient_national_sponsor_id = pra.patient_national_sponsor_id)
   LEFT JOIN patient_corporate_details spcd ON (spcd.patient_corporate_id = pra.secondary_patient_corporate_id)
   LEFT JOIN patient_national_sponsor_details spnd ON (spnd.patient_national_sponsor_id = pra.secondary_patient_national_sponsor_id)
   LEFT JOIN modules_activated modact ON (modact.module_id = 'mod_adv_ins')
   LEFT JOIN mrd_casefile_attributes mrd ON (mrd.mr_no = pd.mr_no)
   LEFT JOIN mrd_casefile_users mcu ON (mrd.issued_to_user = mcu.file_user_id)
   LEFT JOIN department depc ON (depc.dept_id = mrd.issued_to_dept)
   LEFT JOIN prior_auth_modes pam ON ( pra.prior_auth_mode_id = pam.prior_auth_mode_id)
   LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pra.center_id)
   LEFT JOIN death_reason_master drm ON (drm.reason_id=pd.death_reason_id)
   LEFT JOIN sample_collection_centers scc ON (scc.collection_center_id = pra.collection_center_id)
   LEFT JOIN sponsor_type pst ON pst.sponsor_type_id = tpa.sponsor_type_id
   LEFT JOIN sponsor_type sst ON sst.sponsor_type_id = stpa.sponsor_type_id
   LEFT JOIN case_rate_main pcrm ON(pcrm.case_rate_id = pra.primary_case_rate_id)
   LEFT JOIN case_rate_main scrm ON(scrm.case_rate_id = pra.secondary_case_rate_id)
   JOIN confidentiality_grp_master cgm ON (cgm.confidentiality_grp_id = pd.patient_group)  
WHERE ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )
;

--
-- View to list all the orders (excluding operation, since that has slightly
-- different set of parameters.) The fields that are listed for every order are:
--   patient_id, order_id, common_order_id,
--   type, sub_type, sub_type_name, activity_code             (sub-type is like Night IP for Doctor)
--   item_id, item_name, item_code
--   pres_doctor_id, pres_doctor_name, pres_timestamp, remarks
--   quantity, from_timestamp, to_timestamp		(where applicable, or 1, null, null)
--   operation_ref, package_ref
--   bill_no, bill_status,
--   status (U/N/P/C/X): Unnecessary, Not-conducted, Partially-conducted, Conducted, Xcancelled
--   sample_collected (Y/N): applicable only for tests
--   finalization_status (U/N/F): Unnecessary, Not Finalized, Finalized
--   prior_auth_id (exists for insurance patients)
--   cond_doctor_name,cond_doctor_id
--
DROP VIEW IF EXISTS patient_orders_view CASCADE;
CREATE VIEW patient_orders_view AS
SELECT
 sp.patient_id, sp.prescription_id AS order_id, sp.common_order_id,
 'Service' AS type, '' AS sub_type, '' AS sub_type_name, 'SER' AS activity_code,
 sp.service_id AS item_id, s.service_name AS item_name, s.service_code AS item_code,
 sp.doctor_id AS pres_doctor_id, pd.doctor_name AS pres_doctor_name, presc_date AS pres_timestamp,
 sp.remarks AS remarks,TO_CHAR(presc_date,'dd-mm-yyyy') AS pres_date,
 sp.quantity AS quantity, null::timestamp AS from_timestamp, null::timestamp AS to_timestamp,
 (sp.quantity::integer)::text || ' No(s)' AS details,
 operation_ref, package_ref, b.bill_no, b.status AS bill_status,bc.amount,
 (CASE WHEN conducted = 'X' THEN 'X' WHEN NOT s.conduction_applicable THEN 'U'
  ELSE sp.conducted END) AS status, 'N' AS sample_collected, 'U' AS finalization_status,
 bc.prior_auth_id, bc.prior_auth_mode_id,bc.first_of_category,d.doctor_name AS cond_doctor_name,
 d.doctor_id AS cond_doctor_id,null AS labno,true AS canclebill, sp.specialization AS isdialysis,ds.completion_status,
 coalesce(ds.status, 'O') AS dialysis_status,'' AS urgent,
 (case WHEN coalesce(sp.tooth_unv_number,'')='' THEN sp.tooth_fdi_number ELSE sp.tooth_unv_number end) AS tooth_number,
 bc.insurance_category_id,bc.charge_id, null AS outsource_dest_prescribed_id, 'N' AS mandate_additional_info, '' AS additional_info_reqts,
   NULL::integer AS package_category_id
FROM services_prescribed sp
 JOIN services s ON (s.service_id = sp.service_id)
 LEFT JOIN doctors pd ON pd.doctor_id = sp.doctor_id
 LEFT JOIN bill_activity_charge bac ON bac.activity_id=sp.prescription_id::text AND bac.activity_code='SER'
 LEFT JOIN bill_charge bc USING (charge_id)
 LEFT JOIN doctors d ON d.doctor_id = bc.payee_doctor_id
 LEFT JOIN bill b USING (bill_no)
 LEFT JOIN dialysis_session ds ON (ds.order_id = sp.prescription_id)
UNION ALL
SELECT
 tp.pat_id AS patient_id, tp.prescribed_id AS order_id, tp.common_order_id,
 CASE WHEN ddept.category = 'DEP_LAB' THEN 'Laboratory' ELSE 'Radiology' END AS type,
 ddept.ddept_id AS sub_type, ddept.ddept_name AS sub_type_name, 'DIA',
 tp.test_id AS item_id, d.test_name AS item_name, d.diag_code AS item_code,
 tp.pres_doctor AS pres_doctor_id, pd.doctor_name AS pres_doctor_name, pres_date AS pres_timestamp,
 tp.remarks,TO_CHAR(pres_date,'dd-mm-yyyy') AS pres_date,
 1 AS quantity, null::timestamp AS from_timestamp, null::timestamp AS to_timestamp,
 '' AS details,
 null AS operation_ref, tp.package_ref, b.bill_no, b.status AS bill_status,bc.amount,
 tp.conducted AS status,
 (CASE WHEN tp.sflag = '1' THEN 'Y' ELSE 'N' END ) AS sample_collected,'U' AS finalization_status,
 bc.prior_auth_id, bc.prior_auth_mode_id, bc.first_of_category,dc.doctor_name AS cond_doctor_name,
 dc.doctor_id AS cond_doctor_id,tp.labno,NOT tp.re_conduction AS canclebill, '' AS isdialysis, '' AS dialysis_status, '' AS completion_status,
 tp.priority AS urgent,null AS tooth_number, bc.insurance_category_id, bc.charge_id, tp.outsource_dest_prescribed_id, d.mandate_additional_info,
 d.additional_info_reqts,  NULL::integer AS package_category_id
FROM tests_prescribed tp
 JOIN diagnostics d ON d.test_id = tp.test_id
 JOIN diagnostics_departments ddept ON ddept.ddept_id = d.ddept_id
 LEFT JOIN doctors pd ON tp.pres_doctor = pd.doctor_id
 LEFT JOIN bill_activity_charge bac ON bac.activity_id=tp.prescribed_id::text AND bac.activity_code='DIA'
 LEFT JOIN bill_charge bc USING (charge_id)
 LEFT JOIN doctors dc ON dc.doctor_id = bc.payee_doctor_id
 LEFT JOIN bill b USING (bill_no)
UNION ALL
SELECT
 dc.patient_id, dc.consultation_id AS order_id, dc.common_order_id,
 'Doctor' AS type, dc.head AS sub_type,
 CASE WHEN ot_doc_role IS NULL OR ot_doc_role = '' THEN ct.consultation_type ELSE chc.chargehead_name
 END AS sub_type_name,
 'DOC',
 dc.doctor_name AS item_id, dcd.doctor_name AS item_name, null AS item_code,
 pd.doctor_id AS pres_doctor_id, pd.doctor_name AS pres_doctor_name, presc_date AS pres_timestamp,
 dc.remarks AS remarks,TO_CHAR(presc_date,'dd-mm-yyyy') AS pres_date,
 1 AS qty, visited_date AS from_timestamp, null AS to_timestamp,
 to_char(visited_date , 'DD-MM-YYYY HH24:MI') AS details,
 operation_ref, package_ref, b.bill_no, b.status AS bill_status,bc.amount,
 (CASE WHEN cancel_status='C' THEN 'X' WHEN dc.status='A' THEN 'N' ELSE dc.status END) AS status,
 'N' AS sample_collected, 'U' AS finalization_status,bc.prior_auth_id, bc.prior_auth_mode_id,  bc.first_of_category
 ,'' AS cond_doctor_name,null AS cond_doctor_id,null AS labno,true AS canclebill, '' AS isdialysis, '' AS dialysis_status, '' AS completion_status,
 '' AS urgent,null AS tooth_number, bc.insurance_category_id, bc.charge_id, null AS outsource_dest_prescribed_id, 'N' AS mandate_additional_info, '' AS additional_info_reqts,
 NULL::integer AS package_category_id
FROM doctor_consultation dc
 JOIN doctors dcd ON dcd.doctor_id = dc.doctor_name
 LEFT JOIN doctors pd ON pd.doctor_id = dc.presc_doctor_id
 LEFT JOIN consultation_types ct ON (ct.consultation_type_id::text = dc.head)
 LEFT JOIN chargehead_constants chc ON (chc.chargehead_id = dc.ot_doc_role)
 LEFT JOIN bill_activity_charge bac ON bac.activity_id=dc.consultation_id::text AND bac.activity_code='DOC'
 LEFT JOIN bill_charge bc USING (charge_id)
 LEFT JOIN bill b USING (bill_no)
UNION ALL
SELECT
 ep.patient_id, ep.prescribed_id AS order_id, ep.common_order_id,
 'Equipment' AS type, '' AS sub_type, '' AS sub_type_name, 'EQU',
 ep.eq_id AS item_id, e.equipment_name AS item_name, e.equipment_code AS item_code,
 ep.doctor_id AS pres_doctor_id, pd.doctor_name AS pres_doctor_name, ep.date AS pres_timestamp,
 ep.remarks AS remarks,TO_CHAR(date,'dd-mm-yyyy') AS pres_date,
 duration AS qty, used_from AS from_timestamp, used_till AS to_timestamp,
 (CASE WHEN used_from::date = used_till::date THEN
  to_char(used_from, 'DD-MM-YYYY HH24:MI - ') || to_char(used_till, 'HH24:MI')
 ELSE
  to_char(used_from, 'DD-MM-YYYY HH24:MI - ') || to_char(used_till, 'DD-MM-YYYY HH24:MI')
 END) AS details,
 operation_ref, package_ref, b.bill_no, b.status AS bill_status,bc.amount,
 (CASE WHEN cancel_status = 'C' THEN 'X' ELSE 'U' END) AS status,'N' AS sample_collected,
 finalization_status,bc.prior_auth_id, bc.prior_auth_mode_id, bc.first_of_category
 ,'' AS cond_doctor_name,null AS cond_doctor_id,null AS labno,true AS canclebill, '' AS isdialysis, '' AS dialysis_status, '' AS completion_status,
 '' AS urgent,null AS tooth_number, bc.insurance_category_id, bc.charge_id, null AS outsource_dest_prescribed_id, 'N' AS mandate_additional_info, '' AS additional_info_reqts,
 NULL::integer AS package_category_id
FROM equipment_prescribed ep
 JOIN equipment_master e ON e.eq_id = ep.eq_id
 LEFT OUTER JOIN doctors pd ON pd.doctor_id = ep.doctor_id
 LEFT JOIN bill_activity_charge bac ON bac.activity_id=ep.prescribed_id::text AND bac.activity_code='EQU'
 LEFT JOIN bill_charge bc USING (charge_id)
 LEFT JOIN bill b USING (bill_no)
UNION ALL
SELECT
 osp.patient_id, osp.prescribed_id AS order_id, osp.common_order_id,
 'Other Charge' AS type, service_group AS sub_type, chc.chargehead_name AS sub_type_name, 'OTC',
 service_name AS item_id, service_name AS item_name, NULL AS item_code,
 osp.doctor_id AS pres_doctor_id, pd.doctor_name AS pres_doctor_name, pres_time AS pres_timestamp,
 osp.remarks AS remarks,TO_CHAR(pres_time,'dd-mm-yyyy') AS pres_date,
 osp.quantity AS qty, null, null,
 osp.quantity::text || ' No(s)' AS details,
 operation_ref, package_ref, b.bill_no, b.status AS bill_status,bc.amount,
 (CASE WHEN cancel_status = 'C' THEN 'X' ELSE 'U' END) AS status,'N' AS sample_collected,
 'U' AS finalization_status,bc.prior_auth_id, bc.prior_auth_mode_id,  bc.first_of_category
 ,'' AS cond_doctor_name,null AS cond_doctor_id,null AS labno,true AS canclebill, '' AS isdialysis, '' AS dialysis_status, '' AS completion_status,
 '' AS urgent,null AS tooth_number, bc.insurance_category_id, bc.charge_id, null AS outsource_dest_prescribed_id, 'N' AS mandate_additional_info, '' AS additional_info_reqts,
 NULL::integer AS package_category_id
FROM other_services_prescribed osp
 LEFT OUTER JOIN doctors pd ON pd.doctor_id = osp.doctor_id
 JOIN chargehead_constants chc ON chargehead_id=osp.service_group
 LEFT JOIN bill_activity_charge bac ON bac.activity_id=osp.prescribed_id::text AND bac.activity_code='OTC'
 LEFT JOIN bill_charge bc USING (charge_id)
 LEFT JOIN bill b USING (bill_no)
UNION ALL
SELECT
 dp.visit_id AS patient_id, dp.ordered_id AS order_id, dp.common_order_id,
 'Meal' AS type, '' AS sub_type, '' AS sub_type_name, 'DIE',
 dp.diet_id::text AS item_id, dm.meal_name AS item_name, NULL AS item_code,
 dp.ordered_by AS pres_doctor_id, pd.doctor_name AS pres_doctor_name, ordered_time AS pres_timestamp,
 special_instructions AS remarks,TO_CHAR(ordered_time,'dd-mm-yyyy') AS pres_date,
 1 AS qty, meal_date + meal_time AS from_timestamp, null AS to_timestamp,
 (CASE WHEN meal_timing = 'Spl' THEN to_char(meal_date + meal_time,'DD-MM-YYYY HH:MI')
  ELSE to_char(meal_date, 'DD-MM-YYYY ') || meal_timing END) AS details,
 null AS operation_ref, package_ref,  b.bill_no, b.status AS bill_status,bc.amount,
 (CASE WHEN dp.status = 'Y' THEN 'C' ELSE dp.status END) AS status,'N' AS sample_collected,
 'U' AS finalization_status,bc.prior_auth_id,  bc.prior_auth_mode_id, bc.first_of_category
 ,'' AS cond_doctor_name,null AS cond_doctor_id,null AS labno,true AS canclebill, '' AS isdialysis, '' AS dialysis_status, '' AS completion_status,
 '' AS urgent,null AS tooth_number, bc.insurance_category_id, bc.charge_id, null AS outsource_dest_prescribed_id, 'N' AS mandate_additional_info, '' AS additional_info_reqts,
 NULL::integer AS package_category_id
FROM diet_prescribed dp
 JOIN diet_master dm ON dm.diet_id = dp.diet_id
 LEFT JOIN doctors pd ON pd.doctor_id = dp.ordered_by
 LEFT JOIN bill_activity_charge bac ON bac.activity_id=dp.ordered_id::text AND bac.activity_code='DIE'
 LEFT JOIN bill_charge bc USING (charge_id)
 LEFT JOIN bill b USING (bill_no)
UNION ALL
SELECT
 pps.patient_id, pps.prescription_id AS order_id, pps.common_order_id,
 /* sub-type is required for filtering by diag packages, to show in lab orders */
 'Package' AS type, pm.type AS sub_type, '' AS sub_type_name, 'PKG',
 pps.package_id::text AS item_id, CASE WHEN pac.is_customized_package THEN pac.package_name 
 ELSE pm.package_name END AS name, NULL AS item_code,
 pd.doctor_id AS pres_doctor_id, pd.doctor_name AS pres_doctor_name, presc_date AS pres_timestamp,
 pps.remarks AS remarks,TO_CHAR(presc_date,'dd-mm-yyyy') AS pres_date,
 1 AS qty, null AS from_timestamp, null AS to_timestamp,
 '' AS details,
 null AS operation_ref, null AS package_ref, b.bill_no, b.status AS bill_status,bc.amount,
 (SELECT (
  CASE
   WHEN foo.cancled > 0 THEN 'X'
    WHEN foo.actual = foo.notconducted  THEN 'N'
      WHEN foo.actual = foo.conducted  THEN 'C'
         WHEN  foo.notconducted < foo.actual THEN 'P'
      END  ) FROM (
      SELECT
    (SELECT COUNT(*) FROM tests_prescribed tp
     WHERE tp.package_ref = pp.prescription_id and conducted NOT IN ( 'U','X' ) ) +
    (SELECT COUNT(*) FROM services_prescribed sp
     where sp.package_ref = pp.prescription_id and conducted NOT IN ( 'U','X' ) ) +
    (SELECT COUNT(*) FROM bed_operation_schedule op
     where op.package_ref = pp.prescription_id and status NOT IN ( 'U','X' )) +
    (SELECT COUNT(*) FROM doctor_consultation dc
     where dc.package_ref = pp.prescription_id and status NOT IN ( 'U','X' )) +
    (SELECT COUNT(*) FROM services_prescribed spp
     JOIN bed_operation_schedule opp ON (opp.prescribed_id=spp.operation_ref)
     WHERE opp.package_ref = pp.prescription_id and conducted NOT IN ( 'U','X' ) ) AS actual,
    (SELECT COUNT(*) FROM tests_prescribed tp
     WHERE tp.package_ref = pp.prescription_id and conducted IN ('N','NRN')
      and tp.outsource_dest_prescribed_id IS NULL) +
    (SELECT COUNT(*) FROM services_prescribed sp
     where sp.package_ref = pp.prescription_id and conducted IN ('N')) +
    (SELECT COUNT(*) FROM bed_operation_schedule op
     where op.package_ref = pp.prescription_id and status IN ('N')) +
    (SELECT COUNT(*) FROM doctor_consultation dc
     where dc.package_ref = pp.prescription_id and status IN ('A')) +
    (SELECT COUNT(*) FROM services_prescribed spp
     JOIN bed_operation_schedule opp ON (opp.prescribed_id=spp.operation_ref)
     WHERE opp.package_ref = pp.prescription_id and spp.conducted IN ('N')) AS notconducted,
    (SELECT COUNT(*) FROM tests_prescribed tp
     WHERE tp.package_ref = pp.prescription_id and conducted IN ('P','RP')) +
     (SELECT COUNT(*) FROM tests_prescribed tp1
     WHERE tp1.package_ref = pp.prescription_id and conducted IN ('N', 'NRN')
      and tp1.outsource_dest_prescribed_id IS NOT NULL) +
    (SELECT COUNT(*) FROM services_prescribed sp
     where sp.package_ref = pp.prescription_id and conducted IN ('P')) +
    (SELECT COUNT(*) FROM bed_operation_schedule op
     where op.package_ref = pp.prescription_id and status IN ('P')) +
    (SELECT COUNT(*) FROM doctor_consultation dc
     where dc.package_ref = pp.prescription_id and status IN ('P')) +
    (SELECT COUNT(*) FROM services_prescribed spp
     JOIN bed_operation_schedule opp ON (opp.prescribed_id=spp.operation_ref)
     WHERE opp.package_ref = pp.prescription_id and spp.conducted IN ('P')) AS partially,
    (SELECT COUNT(*) FROM tests_prescribed tp
     WHERE tp.package_ref = pp.prescription_id and conducted IN ('C','V','S','RC','RV','CRN')) +
    (SELECT COUNT(*) FROM services_prescribed sp
     where sp.package_ref = pp.prescription_id and conducted IN ('C')) +
    (SELECT COUNT(*) FROM bed_operation_schedule op
     where op.package_ref = pp.prescription_id and status IN ('C')) +
    (SELECT COUNT(*) FROM doctor_consultation dc
     where dc.package_ref = pp.prescription_id and status IN ('C')) +
    (SELECT COUNT(*) FROM services_prescribed spp
     JOIN bed_operation_schedule opp ON (opp.prescribed_id=spp.operation_ref)
     WHERE opp.package_ref = pp.prescription_id and spp.conducted IN ('C')) AS conducted,
    (SELECT COUNT(*) FROM package_prescribed ppp
     where ppp.prescription_id = pp.prescription_id and status IN ('X') ) AS cancled
 FROM package_prescribed pp
 LEFT JOIN tests_prescribed tp ON (tp.package_ref = pp.prescription_id)
 LEFT JOIN services_prescribed sp ON (sp.package_ref = pp.prescription_id )
 LEFT JOIN bed_operation_schedule op ON (op.package_ref = pp.prescription_id)
 WHERE pp.prescription_id = pps.prescription_id
 group by pp.prescription_id) AS foo ) AS status
 ,'N' AS sample_collected,'U' AS finalization_status,bc.prior_auth_id,  bc.prior_auth_mode_id, bc.first_of_category
 ,'' AS cond_doctor_name,null AS cond_doctor_id,null AS labno,true AS canclebill, '' AS isdialysis, '' AS dialysis_status, '' AS completion_status,
 '' AS urgent,null AS tooth_number, bc.insurance_category_id, bc.charge_id, null AS outsource_dest_prescribed_id, 'N' AS mandate_additional_info, '' AS additional_info_reqts,
 pm.package_category_id
FROM package_prescribed pps
 JOIN packages pm ON pm.package_id = pps.package_id
 LEFT JOIN patient_customised_package_details pac ON pac.patient_package_id = pps.pat_package_id 
 LEFT OUTER JOIN doctors pd ON pd.doctor_id = pps.doctor_id
 LEFT JOIN bill_activity_charge bac ON bac.activity_id=pps.prescription_id::text AND bac.activity_code='PKG'
 LEFT JOIN bill_charge bc USING(charge_id)
 LEFT JOIN bill b USING (bill_no)
;

-- View to list all multi visit orders excluding opeartion ---

DROP VIEW IF EXISTS patient_multivisit_orders_view CASCADE;
CREATE OR REPLACE VIEW patient_multivisit_orders_view AS
      Select * FROM (
	      SELECT sp.service_id AS item_id,sp.quantity AS quantity,sp.package_ref, sp.conducted AS status,mr_no, sp.presc_date AS ordered_on
	      FROM services_prescribed sp
	UNION ALL
     	 SELECT tp.test_id AS item_id,1 AS quantity,tp.package_ref, tp.conducted AS status,mr_no, tp.pres_date AS ordered_on
      		FROM tests_prescribed tp
	UNION ALL
      	SELECT bc.consultation_type_id::text AS item_id,1 AS quantity,dc.package_ref,
      	(CASE WHEN dc.cancel_status='C' THEN 'X' WHEN dc.status='A' THEN 'N' ELSE dc.status END) AS status,mr_no, bc.posted_date AS ordered_on
      	FROM doctor_consultation dc
     	 LEFT JOIN bill_activity_charge bac ON bac.activity_id=dc.consultation_id::text AND bac.activity_code='DOC'
      	 LEFT JOIN bill_charge bc USING (charge_id)
	UNION ALL
       		SELECT osp.service_name AS item_id,osp.quantity AS quantity,osp.package_ref,
		coalesce(CASE WHEN osp.cancel_status = 'C' THEN 'X' ELSE osp.cancel_status end,'') AS status,mr_no, osp.pres_time AS ordered_on
      		FROM other_services_prescribed osp
	) AS foo
	WHERE package_ref is not null;

DROP VIEW IF EXISTS operation_prescriptions_view CASCADE;
CREATE OR REPLACE VIEW operation_prescriptions_view AS
	SELECT mr_no, patient_id, doctor_name AS doctor, TO_CHAR(prescribed_time,'DD-MM-YYYY')||' / '||TO_CHAR(prescribed_time,'HH:mi')  AS prescribed_time,
		(((((medicine_name::text || ' '::text) || dosage::text) || ' '::text) ||  doctor_prescription.no_days) || ' '::text) || 'day(s)'|| ' '::text AS details,doctor_prescription.remarks AS remarks,chargehead_name AS pgroup,med_prescription_id AS id,coalesce((case WHEN cancel_status = 'C' THEN 'X' ELSE cancel_status end), '') AS status,
		(case WHEN cancel_status ='C' THEN 'checked' ELSE '' end) AS check,(case WHEN cancel_status ='C' THEN 'disabled' end) AS dis,
		coalesce((case WHEN cancel_status='C' THEN '1' ELSE '1' end), '') AS role,operation_ref,chargehead_id AS head,prescribed_qty AS qty,chargegroup_id, 0 AS amount, common_order_id
	FROM chargehead_constants, doctor_prescription
		LEFT OUTER JOIN doctors ON  doctors.doctor_id = doctor_prescription.doctor_id
	WHERE chargehead_id='MEMED' and operation_ref is not null
	UNION ALL
	SELECT mr_no, patient_id , doctor_name AS  doctor,TO_CHAR(presc_date,'DD-MM-YYYY')||' / '||TO_CHAR(presc_date,'HH:mi') AS prescribed_time,
		s.service_name AS details, sp.remarks AS remarks, chargehead_name AS pgroup,prescription_id AS id,coalesce((case WHEN Conducted in ('C','P') THEN 'C'  ELSE Conducted end),'') AS  status,
		(case WHEN conducted ='X' THEN 'checked' ELSE '' end) AS check,(case WHEN conducted in ('C','X') THEN 'disabled' end) AS dis,
		coalesce((case WHEN conducted='X' THEN '1' ELSE '1' end), '') AS role,operation_ref,chargehead_id AS head,quantity AS qty,chargegroup_id, bc.amount, common_order_id
	FROM chargehead_constants, services_prescribed sp
		LEFT OUTER JOIN doctors ON  doctors.doctor_id = sp.doctor_id
		JOIN services s using(service_id)
		 JOIN bill_activity_charge bac ON bac.activity_id=sp.prescription_id::text AND bac.activity_code = 'SER'
 		 JOIN bill_charge bc using(charge_id)
	WHERE chargehead_id='SERSNP' and operation_ref is not null
	UNION ALL
	SELECT mr_no, patient_id,dc.doctor_name AS doctor,TO_CHAR(visited_date,'DD-MM-YYYY')||' / '||TO_CHAR(visited_date,'HH:mi')   AS prescribed_time,''  AS details,remarks AS remarks, chargehead_name as
		  pgroup, consultation_id AS id,coalesce((case WHEN cancel_status = 'C' THEN 'X' ELSE cancel_status end),'') AS status ,(case WHEN cancel_status ='C' THEN 'checked'  ELSE '' end) AS check,
		  (case WHEN cancel_status ='C' THEN 'disabled' end) AS dis,coalesce(ot_doc_role,'') AS role,operation_ref,head AS head,1 AS qty,chargegroup_id, bc.amount, common_order_id
	FROM chargehead_constants,  doctor_consultation  dcc
		LEFT OUTER JOIN doctors dc  ON dc.doctor_id = dcc.doctor_name
		  JOIN bill_activity_charge bac ON bac.activity_id=dcc.consultation_id::text AND bac.activity_code = 'DOC'
		   JOIN bill_charge bc USING(charge_id)
	WHERE chargehead_id=dcc.head and operation_ref is not null
	UNION ALL
	SELECT mr_no,patient_id ,doctor_name AS doctor,TO_CHAR(date,'DD-MM-YYYY')||' / '||TO_CHAR(date,'HH:mi')  AS prescribed_time,  equipment_name AS details,
		remarks AS remarks, chargehead_name AS pgroup,prescribed_id AS id,
		coalesce((case WHEN cancel_status = 'C' THEN 'X' ELSE 'N' end),'') AS status,(case WHEN cancel_status ='C' THEN 'checked'  ELSE '' end) AS check,
		(case WHEN cancel_status ='C' THEN 'disabled' end) AS dis,coalesce((case WHEN cancel_status='C' THEN '1' ELSE '1' end),'') AS role,operation_ref, chargehead_id AS head,duration AS qty,chargegroup_id, bc.amount, common_order_id
	FROM
	 equipment_prescribed pbe
		LEFT OUTER JOIN doctors doc  ON  doc.doctor_id = pbe.doctor_id JOIN equipment_master em ON em.eq_id = pbe.eq_id
		JOIN chargehead_constants ON chargehead_id = 'EQUOTC'
		  JOIN bill_activity_charge bac ON bac.activity_id=pbe.prescribed_id::text AND bac.activity_code = 'EQU'
		 JOIN bill_charge bc USING(charge_id)
	WHERE operation_ref is not null
	UNION ALL
	SELECT mr_no,patient_id, doctor_name AS doctor,TO_CHAR(pres_time,'DD-MM-YYYY HH:mi') AS prescribed_time,service_name AS details, remarks AS remarks,chargehead_name AS pgroup,
		prescribed_id AS id,coalesce((case WHEN cancel_status = 'C' THEN 'X' ELSE cancel_status end),'') AS status,(case WHEN cancel_status ='C' THEN 'checked' ELSE '' end) AS check,
		(case WHEN cancel_status ='C' THEN 'disabled' end) AS dis,coalesce((case WHEN cancel_status='C' THEN '1' ELSE '1' end),'') AS role,operation_ref,chargehead_id AS head,quantity AS qty,chargegroup_id, bc.amount, common_order_id
	FROM chargehead_constants JOIN other_services_prescribed osp ON  chargehead_id=osp.service_group
	left outer join
		doctors doc ON (doc.doctor_id = osp.doctor_id)
		  JOIN bill_activity_charge bac ON bac.activity_id=osp.prescribed_id::text AND bac.activity_code = 'OTC'
		 JOIN bill_charge bc USING(charge_id)
	WHERE   operation_ref is not null
;


DROP VIEW IF EXISTS store_stock_view CASCADE;
DROP VIEW IF EXISTS store_purchase_item_report_view CASCADE;
CREATE OR REPLACE VIEW store_purchase_item_report_view AS
SELECT
  CASE WHEN pngm.debit_note_no IS NULL THEN 'Purchase' ELSE 'Debit Note' END AS purchase_type,
  sm.supplier_name,sm.cust_supplier_code, COALESCE(pi.tax_name,pdn.tax_name) AS tax_name,  CASE WHEN supplier_invoice_id IS NOT NULL
  THEN pi.cst_rate ELSE pdn.cst_rate END  AS cst_rate,
  pmd.medicine_name, png.medicine_id, png.batch_no, png.exp_dt,
  pmd.generic_name AS generic_id, gn.generic_name, pmd.med_category_id, pmc.category AS category_name,
  control_type_name,
  pmd.manf_name AS manf_id, mm.manf_name,mm.manf_mnemonic,
  png.billed_qty, png.bonus_qty, pmd.issue_base_unit,
  png.cost_price, png.tax AS itemwise_tax_amount, png.tax_rate AS itemwise_tax_per, png.adj_mrp,
  png.discount AS itemwise_discount,
  png.grn_no, pngm.grn_date, pngm.store_id, gd.dept_name AS store_name, CASE WHEN supplier_invoice_id IS NOT NULL THEN
  CASE WHEN pi.tax_name='CST' THEN
  'CST:'||round(pi.cst_rate,0)  ELSE
  'VAT:'||round(png.tax_rate,0)  END
  ELSE
  CASE WHEN pdn.tax_name='CST' THEN
  'CST:'||round(pdn.cst_rate,0)  ELSE
  'VAT:'||round(png.tax_rate,0)  END
  END AS tax_name_and_tax_per
FROM store_grn_details png
  JOIN store_grn_main pngm USING (grn_no)
  JOIN store_item_details pmd USING (medicine_id)
  JOIN manf_master mm ON (mm.manf_code=pmd.manf_name)
  JOIN stores gd ON (gd.dept_id=pngm.store_id)
  JOIN store_category_master pmc ON (pmc.category_id=pmd.med_category_id)
  LEFT JOIN store_invoice pi USING (supplier_invoice_id)
  LEFT JOIN store_debit_note pdn ON (pdn.debit_note_no=pngm.debit_note_no)
  LEFT JOIN generic_name gn ON (gn.generic_code=pmd.generic_name)
  LEFT JOIN supplier_master sm ON (sm.supplier_code=pi.supplier_id OR sm.supplier_code = pdn.supplier_id)
  LEFT JOIN store_item_controltype sic ON (sic.control_type_id = pmd.control_type_id)
;

--
-- View used by Store purchase invoice reports (Invoice-wise, summary, trend and CSV)
-- We need one row per Invoice, but including totals from grn. So, we join the grn and sum over
-- the grn amounts.
--
DROP VIEW IF EXISTS store_purchase_invoice_report_view CASCADE;
CREATE OR REPLACE VIEW store_purchase_invoice_report_view AS
SELECT 'Purchase' :: text AS purchase_type, sm.supplier_name, sm.cust_supplier_code, i.invoice_no, i.invoice_date, i.po_no, 
CASE WHEN i.status = 'O' THEN 'Open' WHEN i.status = 'F' THEN 'Finalized' ELSE 'Closed' END AS invoice_status, i.due_date, 
i.paid_date, i.tax_name, 
CASE WHEN i.cash_purchase = 'Y' THEN 'Cash' ELSE 'Credit' END AS purchasetype, i.tcs_amount,
CASE WHEN i.discount_type = 'A' THEN i.discount WHEN i.discount_type = 'P' 
THEN gm.discount ELSE 0 END AS discount, i.round_off, i.other_charges, gm.cess, gm.grn_nos,
gm.grn_date, gm.store_name, gm.store_type_name, gm.item_amount, gm.item_discount, 
gm.item_scheme_discount , gm.item_tax, 0 AS received_debit_amt, 0 AS raised_amt, gm.ced_amt, 
spm.po_date, gm.consignment_stock, gm.user_name, i.date_time AS mod_datetime, gm.center_id, 
gm.center_name, gm.form_8h, sm.supplier_state, sm.supplier_tin_no, gm.pharmacy_tin_no, 
sm.drug_license_no, sm.pan_no, sm.cin_no, i.transportation_charges,
i.remarks,gm.purpose_of_purchase,NULL AS return_type,NULL AS return_reason,NULL AS return_remarks 
FROM store_invoice i 
JOIN LATERAL( 
SELECT gm.consignment_stock, gm.user_name, gm.form_8h, ( i.discount_per / 100 
	* ( SUM ( g.billed_qty / pmd.issue_base_unit * g.cost_price) - ( SUM(g.discount) + SUM(g.scheme_discount) ) + 
    ( SUM(g.tax) + ( SUM(g.tax) * i.cess_tax_rate ) / 100 ) + SUM(g.item_ced) + i.other_charges )) AS discount,
    ( SUM(g.tax) * i.cess_tax_rate ) / 100 AS cess, Textcat_commacat(DISTINCT gm.grn_no) AS grn_nos, 
    Max(gm.grn_date) AS grn_date, Max(s.dept_name) AS store_name, store_type_name,
    SUM(g.billed_qty / g.grn_pkg_size * g.cost_price) AS item_amount, SUM(g.discount) AS item_discount, 
    SUM(g.scheme_discount) AS item_scheme_discount , SUM(g.tax) AS item_tax, SUM(g.item_ced) AS ced_amt,
    s.center_id, hcm.center_name, s.pharmacy_tin_no, gm.purpose_of_purchase
    FROM     store_grn_main gm 
    JOIN     store_grn_details g USING(grn_no) 
    JOIN store_item_details pmd USING (medicine_id) 
    JOIN stores s ON ( gm.store_id = s.dept_id )
    LEFT JOIN hospital_center_master hcm ON ( s.center_id = hcm.center_id ) 
    LEFT JOIN store_type_master stm ON ( s.store_type_id = stm.store_type_id )
    WHERE i.supplier_invoice_id = gm.supplier_invoice_id
    GROUP BY gm.grn_no,store_type_name, s.center_id, hcm.center_name, s.pharmacy_tin_no, gm.purpose_of_purchase ) gm ON true  
JOIN supplier_master sm ON ( sm.supplier_code = i.supplier_id ) 
LEFT JOIN store_po_main spm ON spm.po_no = i.po_no 
UNION ALL
SELECT 'Debit Note'::text AS purchase_type, sm.supplier_name, sm.cust_supplier_code, 
d.debit_note_no, d.debit_note_date, '', CASE WHEN d.status = 'O' THEN 'Open' ELSE 'Closed' 
END AS invoice_status, NULL AS due_date, NULL AS paid_date, d.tax_name, '' AS purchasetype, 0 as tcs_amount,
CASE WHEN discount_type = 'A' THEN (0 - d.discount) ELSE gm.discount END AS discount, 
( 0 - d.round_off ) AS round_off, ( 0 - d.other_charges ) AS other_charges, 
0 AS cess, gm.grn_nos, gm.grn_date, gm.store_name, gm.store_type_name, 
gm.item_amount, gm.item_discount, gm.scheme_item_discount, 
gm.item_tax, COALESCE(received_debit_amt, 0) AS received_debit_amt, 
COALESCE(( - gm.raised_rate - CASE WHEN d.discount_type = 'A' THEN d.discount ELSE gm.discount 
END + d.other_charges + d.round_off), 0) AS raised_amt,
gm.ced_amt, NULL AS po_date, 
gm.consignment_stock, gm.user_name, d.date_time AS mod_datetime, gm.center_id, gm.center_name, 
gm.form_8h, sm.supplier_state, sm.supplier_tin_no, gm.pharmacy_tin_no, 
sm.drug_license_no, sm.pan_no, sm.cin_no, 0 AS transportation_charges,
NULL AS remarks,NULL AS purpose_of_purchase,CASE WHEN d.return_type='O' THEN 'Others'
WHEN d.return_type='E' THEN 'Expiry' WHEN d.return_type='D' THEN 'Damage' ELSE NULL END as return_type,
CASE WHEN d.return_type='O' THEN d.other_reason ELSE NULL END as return_reason,
d.remarks as return_remarks 
FROM store_debit_note d 
LEFT JOIN LATERAL( 
SELECT d.discount_per * (0 - sum((g.billed_qty / g.grn_pkg_size)*g.orig_debit_rate 
- (g.orig_discount + g.orig_scheme_discount) + g.orig_tax)) / 100 
AS discount, textcat_commacat(DISTINCT gm.grn_no) AS grn_nos, max(gm.grn_date) AS grn_date, 
max(s.dept_name) AS store_name, store_type_name, sum(g.billed_qty / g.grn_pkg_size*g.cost_price) 
AS item_amount, sum(g.discount) AS item_discount, sum(g.scheme_discount) AS scheme_item_discount, 
sum(g.tax) AS item_tax, (sum(g.billed_qty / g.grn_pkg_size*g.orig_debit_rate) - 
(sum(g.orig_discount) + sum(g.orig_scheme_discount)) + sum(g.orig_tax) ) as raised_rate,
sum(g.item_ced) AS ced_amt, gm.consignment_stock, gm.user_name, s.center_id, 
hcm.center_name, gm.form_8h, s.pharmacy_tin_no
    FROM store_grn_main gm 
    JOIN store_grn_details g using (grn_no) 
	JOIN store_item_details pmd using (medicine_id) 
	JOIN stores s ON ( gm.store_id = s.dept_id) 
	LEFT JOIN hospital_center_master hcm ON ( s.center_id = hcm.center_id) 
	LEFT JOIN store_type_master stm ON ( s.store_type_id = stm.store_type_id) 
    WHERE d.debit_note_no = gm.debit_note_no
    GROUP BY gm.grn_no, store_type_name, gm.consignment_stock, gm.user_name, s.center_id, 
    hcm.center_name, gm.form_8h, s.pharmacy_tin_no ) gm ON true 
JOIN supplier_master sm ON ( sm.supplier_code = d.supplier_id) ;

--
-- View used by Store Purchase Invoice Detailed report: select every row in grn
-- The report will group by Invoice and add up the totals. The view is similar to the one above,
-- but without the group by and sum.
--
DROP VIEW IF EXISTS store_purchase_detail_report_view CASCADE;
CREATE OR REPLACE VIEW store_purchase_detail_report_view AS
SELECT 'Purchase'::text AS purchase_type, sm.supplier_name,sm.cust_supplier_code, i.invoice_no, i.invoice_date,
  CASE WHEN i.status='O' THEN 'Open' WHEN i.status='F' THEN 'Finalized' ELSE 'Closed' END AS invoice_status,
  i.due_date, i.paid_date, i.tax_name, i.discount, i.round_off, i.other_charges, i.cess_tax_amt AS cess,
  g.billed_qty, g.cost_price, g.discount AS item_discount, g.tax AS item_tax
FROM store_invoice i
  JOIN store_grn_main gm USING (supplier_invoice_id)
  JOIN store_grn_details g USING (grn_no)
  JOIN supplier_master sm ON (sm.supplier_code = i.supplier_id)
UNION ALL
SELECT 'Debit Note'::text AS purchase_type, sm.supplier_name,sm.cust_supplier_code, d.debit_note_no, d.debit_note_date,
  CASE WHEN d.status = 'O' THEN 'Open' ELSE 'Closed' END AS invoice_status,
  date('1970-01-01') AS due_date, null AS paid_date, 'VAT' AS tax_name, (0-d.discount) AS discount,
  (0-d.round_off) AS round_off, (0-d.other_charges) AS other_charges, 0 AS cess,
  g.billed_qty, g.cost_price, g.discount AS item_discount, g.tax AS item_tax
FROM store_debit_note d
  JOIN store_grn_main gm USING (debit_note_no)
  JOIN store_grn_details g USING (grn_no)
  JOIN supplier_master sm ON (sm.supplier_code = d.supplier_id)
;


-- todo: cleanup patient reg. join, union should not be required in a view.

DROP VIEW IF EXISTS bed_status_report CASCADE;
CREATE OR REPLACE VIEW bed_status_report AS
 SELECT NULL::unknown AS status,ibd.is_bystander::text as bystander,bn.bed_name, substring( bn.bed_name  from  '[0-9]+') AS s_bed_name,
 wn.ward_name, wn.ward_no, bn.bed_id, NULL::unknown AS mr_no, NULL::unknown AS patient_name,
 NULL::unknown AS patient_gender, NULL::unknown AS age, NULL::unknown AS patient_id, NULL::unknown AS reg_date, NULL::unknown AS reg_time,
  NULL::unknown AS discharge_date, NULL::unknown AS discharge_time, NULL::unknown AS doctor_name,
  NULL::unknown AS doctor_id, bn.bed_type, bn.occupancy,
 NULL::unknown AS mlc_status,  NULL::unknown AS refdoctorname, bn.bed_status,bn.avilable_date
 ,NULL::unknown AS primary_tpa_name, NULL::unknown AS secondary_tpa_name, NULL::unknown AS plan_name,
 NULL::unknown AS primary_insurance_co_name,NULL::unknown AS secondary_insurance_co_name,
 NULL::unknown AS plan_type,NULL::unknown AS billing_status,null AS org_id,wn.center_id
  FROM bed_names bn
  JOIN ip_bed_details ibd ON bn.bed_id = ibd.bed_id
  JOIN ward_names wn ON bn.ward_no::text = wn.ward_no::text
  WHERE bn.occupancy::text = 'N'::text AND bn.status = 'A'::bpchar AND wn.status = 'A'::bpchar
UNION
 SELECT ibd.status,ibd.is_bystander::text as bystander, bn.bed_name,substring( bn.bed_name  from  '[0-9]+') AS s_bed_name,
 wn.ward_name, wn.ward_no, bn.bed_id, pd.mr_no,
 get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name)::text AS patient_name,
 pd.patient_gender, get_patient_age(pd.dateofbirth, pd.expected_dob) AS age,
 pra.patient_id, pra.reg_date, pra.reg_time, pra.discharge_date, pra.discharge_time, dr.doctor_name,dr.doctor_id, bn.bed_type, bn.occupancy,
 pra.mlc_status,  COALESCE(drs.doctor_name, rd.referal_name)  AS refdoctorname,
 bn.bed_status,bn.avilable_date
 ,COALESCE(ptpa.tpa_name, 'GENERAL'::character varying) AS primary_tpa_name,
 stpa.tpa_name AS secondary_tpa_name,
 ipm.plan_name,picm.insurance_co_name AS primary_insurance_co_name,
 sicm.insurance_co_name AS secondary_insurance_co_name,in_cat.category_name AS plan_type,
 ibd.bed_state AS billing_status,pra.org_id,wn.center_id
   FROM bed_names bn
   JOIN ip_bed_details ibd ON bn.bed_id = ibd.bed_id
   JOIN patient_registration pra ON ibd.patient_id::text = pra.patient_id::text AND (ibd.status = 'A'::"char" OR ibd.status = 'C'::"char"
   OR ibd.status = 'R'::"char") AND pra.status::text = 'A'::text AND pra.discharge_flag::text <> 'D'::text
   JOIN patient_details pd ON pra.mr_no::text = pd.mr_no::text
  LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
   JOIN doctors dr ON dr.doctor_id::text = pra.doctor::text
   JOIN ward_names wn ON wn.ward_no::text = bn.ward_no::text
   LEFT JOIN tpa_master ptpa ON ptpa.tpa_id::text = pra.primary_sponsor_id::text
   LEFT JOIN tpa_master stpa ON stpa.tpa_id::text = pra.secondary_sponsor_id::text
   LEFT JOIN insurance_plan_main ipm ON ipm.plan_id::text = pra.plan_id::text
   LEFT JOIN insurance_company_master picm ON picm.insurance_co_id = pra.primary_insurance_co
   LEFT JOIN insurance_company_master sicm ON sicm.insurance_co_id = pra.secondary_insurance_co
   LEFT JOIN insurance_category_master in_cat ON in_cat.category_id = ipm.category_id
   LEFT JOIN doctors drs ON pra.reference_docto_id::text = drs.doctor_id::text
   LEFT JOIN referral rd ON pra.reference_docto_id::text = rd.referal_no::text
   WHERE bn.occupancy::text = 'Y'::text AND bn.status = 'A'::bpchar 
UNION
  SELECT 'B' AS status,ibd.is_bystander::text as bystander,bed_name, substring(bed_name  from  '[0-9]+') AS s_bed_name,
  ward_name, wn.ward_no, bnn.bed_id,  null AS mr_no,null AS patient_name,
  null AS patient_gender, null AS age, null AS patient_id, null AS reg_date,
  null AS  reg_time, null AS discharge_date, null AS discharge_time, null AS doctor_name,null AS doctor_id, bed_type,
  occupancy,null AS mlc_status,null AS refdoctorname,bed_status,avilable_date
  ,null AS primary_tpa_name, null AS secondary_tpa_name, null AS plan_name,
  null AS primary_insurance_co_name,null AS secondary_insurance_co_name,
  null AS plan_type,NULL::unknown AS billing_status,null AS org_id,wn.center_id
   FROM bed_names bnn
   JOIN ip_bed_details ibd ON bnn.bed_id = ibd.bed_id
   JOIN ward_names wn using(ward_no)
   WHERE bnn.bed_id NOT IN(SELECT bn.bed_id   FROM bed_names bn
   JOIN ip_bed_details ibd ON bn.bed_id = ibd.bed_id
   JOIN patient_registration pra ON ibd.patient_id::text = pra.patient_id::text
   AND (ibd.status = 'A'::"char" OR ibd.status = 'C'::"char" OR ibd.status = 'R'::"char")
   AND pra.status::text = 'A'::text WHERE bn.occupancy::text = 'Y'::text AND bn.status = 'A'::bpchar)
   AND occupancy ='Y';

DROP VIEW IF EXISTS store_reorder_levels_view CASCADE;
CREATE OR REPLACE VIEW store_reorder_levels_view AS
   SELECT sub1.availableqty,  sub1.store_name, sub1.store_id, sub1.medicine_name,
     sub1.medicine_id, sub1.category, sub1.pkg_size, sub1.poqty, COALESCE(pds.min_level, 0::numeric) AS min_level, COALESCE(pds.max_level, 0::numeric) AS max_level,
     COALESCE(pds.reorder_level, 0::numeric) AS reorder_level, COALESCE(pds.danger_level, 0::numeric) AS danger_level
     FROM ( SELECT COALESCE(sum(pmsd.qty), 0::numeric) AS availableqty,
		gs.dept_name AS store_name, gs.dept_id AS store_id, pmd.medicine_name, pmd.medicine_id, pmc.category, pmd.issue_base_unit AS pkg_size,
		COALESCE(( SELECT sum(round((po.qty_req - po.qty_received) * pmd.issue_base_unit, 2)) AS sum  FROM store_po_main pom
	JOIN store_po po USING (po_no)
         WHERE gs.dept_id = pom.store_id AND pom.status='A' and po.medicine_id = pmd.medicine_id), 0::numeric) AS poqty
        FROM store_item_details pmd
	LEFT JOIN store_stock_details pmsd USING (medicine_id)
	LEFT JOIN store_reorder_levels pds ON (pds.medicine_id = pmsd.medicine_id AND pds.dept_id = pmsd.dept_id)
	LEFT JOIN stores gs ON gs.dept_id = pmsd.dept_id
	LEFT JOIN store_category_master pmc ON category_id=med_category_id
	where asset_approved='Y'
	GROUP BY gs.dept_name, gs.dept_id, pmd.medicine_name, pmd.medicine_id, pmc.category, pmd.issue_base_unit) sub1
	LEFT JOIN store_reorder_levels pds ON sub1.store_id = pds.dept_id AND sub1.medicine_id = pds.medicine_id
	where availableqty <= reorder_level
	ORDER BY sub1.store_name, sub1.medicine_name;

-- todo : yet to be remove
 DROP VIEW IF EXISTS store_indent_details_view CASCADE;
CREATE OR REPLACE VIEW store_indent_details_view AS
select store_id::integer,medicine_id,sum(qty)as qty,date_time,status,itemstatus,purchase_flag,store_name,medicine_name,
category,pkg_size,availableqty,po_no,poqty,purchasebleqty from (

 SELECT pndm.indent_store::text AS store_id, pmd.medicine_id, sum(pnd.qty - pnd.qty_fullfilled) AS qty, pndm.date_time,
 pndm.status, pnd.status AS itemstatus, pnd.purchase_flag,
gs.dept_name AS store_name,
 pmd.medicine_name, pmc.category, pmd.issue_base_unit AS pkg_size, COALESCE(( SELECT sum(pmsd.qty) AS sum
           FROM store_stock_details pmsd
          WHERE pmsd.dept_id = pndm.indent_store AND pmsd.medicine_id = pnd.medicine_id AND
          pmsd.asset_approved = 'Y'::bpchar), 0::numeric) AS availableqty, pnd.po_no,
 COALESCE(( SELECT sum(round((po.qty_req - po.qty_received) * pmd.issue_base_unit, 2)) AS sum
                   FROM store_po_main pom
              JOIN store_po po USING (po_no)
             WHERE gs.dept_id = pom.store_id AND pom.status::text = 'A'::text AND po.medicine_id = pmd.medicine_id), 0::numeric) AS poqty
   ,coalesce((select sum(pnd.qty - pnd.qty_fullfilled) where purchase_flag='Y'),0)as purchasebleqty
   FROM store_indent_main pndm
   JOIN store_indent_details pnd USING (indent_no)
   JOIN store_item_details pmd USING (medicine_id)
   LEFT JOIN stores gs ON gs.dept_id = pndm.indent_store
   LEFT JOIN store_category_master pmc ON pmc.category_id = pmd.med_category_id
  WHERE pnd.status::text <> 'F'::text
  GROUP BY pndm.indent_store,pndm.dept_from,indent_type, pmd.medicine_id, pnd.medicine_id, pndm.date_time, pndm.status,
  pnd.status, gs.dept_id,gs.dept_name, pmd.medicine_name, pmc.category,
  pmd.issue_base_unit, pnd.purchase_flag, pnd.po_no) AS indent
group by store_id,medicine_id,date_time,status,itemstatus,purchase_flag,store_name,medicine_name,
category,pkg_size,availableqty,po_no,poqty,purchasebleqty;


DROP TRIGGER IF EXISTS update_indent_medicine_id ON store_item_details;
DROP FUNCTION IF EXISTS update_indent_medicine_id() CASCADE;

--
-- Trigger to set the package size (required mainly for insertions from
-- spreadsheet using bulk upload, where package_size is not supplied)
--
DROP FUNCTION IF EXISTS update_package_size() CASCADE;
CREATE OR REPLACE FUNCTION update_package_size() RETURNS trigger AS $BODY$
BEGIN
	IF NEW.issue_base_unit IS NULL THEN
		NEW.issue_base_unit = (SELECT package_size FROM package_issue_uom
			WHERE package_uom = NEW.package_uom AND issue_uom = NEW.issue_units LIMIT 1);
	END IF;

	return NEW;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS update_package_size ON store_item_details;
CREATE TRIGGER update_package_size
	BEFORE INSERT ON store_item_details
	FOR EACH ROW EXECUTE PROCEDURE update_package_size();


--
-- Function used to fetch the unit from existing set of units of a department
-- WHEN the unit setting in dept_units_settings.registration preferences is set to rules
-- based i.e 'R'. This function gets the unit based ON current date
--
DROP FUNCTION IF EXISTS getUnit(deptId TEXT) CASCADE;
CREATE OR REPLACE FUNCTION getUnit(deptId TEXT) RETURNS INTEGER AS $BODY$
BEGIN
	RETURN getUnit(deptId, current_date);
END;
$BODY$ LANGUAGE 'plpgsql';

--
-- This function gets the unit based ON the passed in date
--
DROP FUNCTION IF EXISTS getUnit(deptId TEXT, curdt DATE) CASCADE;
CREATE OR REPLACE FUNCTION getUnit(deptId TEXT, curdt DATE) RETURNS INTEGER AS $BODY$
DECLARE
	weekNum INTEGER;
	weekNumdayOne INTEGER;
	dayOfWeek INTEGER;
	numUnits INTEGER;
	unitNum INTEGER;
	unitId INTEGER;
	dayOfYear INTEGER;
BEGIN
	SELECT extract(DOW FROM curdt) INTO dayOfWeek; 		/* 0: sunday, 6: saturday */
	SELECT extract(WEEK FROM curdt) INTO weekNum;		/* 1: first week, 53: last week */
	SELECT extract(DOY FROM curdt) INTO dayOfYear;		/* 1: first week, 53: last week */
	SELECT extract(WEEK FROM (extract(YEAR from curdt) || '-01-01')::date) INTO weekNumDayOne;
	SELECT count(*) FROM dept_unit_master WHERE dept_id = deptId AND status = 'A' INTO numUnits;

	/* First week of year starting in mid week return 52 or 53 AS weeknum fix it */
	IF (weekNumDayOne != 1) THEN
		IF (weekNum = weekNumDayOne) THEN
			weekNum := 1;
		ELSE
			weekNum := weekNum + 1;
		END IF;
	ELSE
	/* if its sunday and not jan 1 increase weeknum by 1 to fix week numbering of 
         * postgres which follows mon-sun AS week and use sun-sat AS week instead
	 */
	IF (dayOfWeek = 0 and dayOfYear > 1) THEN
		weekNum := weekNum + 1;
	END IF;

	END IF;
	/* Ensure that number of units is one of the supported ones: 1,2,3,4,6 */
	IF (numUnits = 5) THEN
		numUnits := 4;
	END IF;

	IF (numUnits > 6) THEN
		numUnits := 6;
	END IF;

	IF (numUnits = 0) THEN
		return null;
	END IF;

	IF (dayOfWeek = 0) THEN		/* Sunday */
		/* If Sunday unit id is returned by week number mod number of units */
		unitNum := weekNum % numUnits;
	ELSIF (numUnits < 4) THEN
			/* We need:
			 *  For 1 unit:  0,0,0,0,0,0
			 *  For 2 units: 0,1,0,1,0,1
			 *  For 3 units: 0,1,2,0,1,2 */
			unitNum := (dayOfWeek-1) % numUnits; 	/* eg, for numUnits=2: 0,1,0,1,0,1 */
	ELSIF (numUnits = 4) THEN
			/* We need: 0,1,2,3,0,1 (odd weekNum) or 0,1,2,3,2,3 (even weekNum) */
			IF ( ((weekNum%2) = 1) OR (dayOfWeek < 5) ) THEN
				unitNum := (dayOfWeek-1) % 4;
			ELSE
				unitNum := (dayOfWeek+1) % 4;
			END IF;
	ELSIF (numUnits = 6) THEN
			/* We need 0,1,2,3,4,5 */
			unitNum := (dayOfWeek -1);
	END IF;

	/*
	 * Return an indext unit id based ON the unit_id ordering
	 */
	SELECT unit_id FROM dept_unit_master WHERE dept_id = deptId AND status = 'A'
		ORDER BY unit_name LIMIT 1 OFFSET unitNum
		INTO unitId;

	RETURN unitId;

END;
$BODY$ LANGUAGE 'plpgsql';

DROP VIEW IF EXISTS doctor_wise_prescribe_sales_view;
CREATE OR REPLACE VIEW doctor_wise_prescribe_sales_view AS
SELECT d.doctor_name,dc.mr_no,get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name)
	AS patient_full_name, coalesce(std.medicine_name, g.generic_name) AS medicine_name, d.doctor_id,std.cust_item_code,
	date(prescribed_date) AS prescribed_date,medicine_quantity AS prescribedqty,issued_qty AS purchasedqty, pr.center_id
FROM patient_prescription pp
	JOIN patient_medicine_prescriptions pmp ON (pp.patient_presc_id=pmp.op_medicine_pres_id)
	LEFT JOIN store_item_details std ON (std.medicine_id = pmp.medicine_id)
	LEFT JOIN generic_name g ON (pmp.generic_code=g.generic_code)
	JOIN doctor_consultation dc ON (dc.consultation_id = pp.consultation_id)
	JOIN doctors d ON (d.doctor_id=dc.doctor_name)
	JOIN patient_registration pr ON (pr.patient_id = dc.patient_id)
	JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
	LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
	WHERE ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )
ORDER BY doctor_name,medicine_name,cust_item_code, prescribed_date;

-- GG Custom Report
DROP VIEW IF EXISTS paymentsdue_view CASCADE;
CREATE OR REPLACE VIEW paymentsdue_view AS
SELECT
CASE
WHEN pd.payment_type = ANY (ARRAY['D'::bpchar, 'P'::bpchar, 'R'::bpchar]) THEN ( SELECT doctors.doctor_name
	FROM doctors
	WHERE doctors.doctor_id::text = pd.payee_name::text)
WHEN pd.payment_type = 'F'::bpchar THEN ( SELECT referral.referal_name
	FROM referral
	WHERE referral.referal_no::text = pd.payee_name::text)
WHEN pd.payment_type = 'O'::bpchar THEN ( SELECT outhouse_master.oh_name
	FROM outhouse_master
	WHERE outhouse_master.oh_id::text = pd.payee_name::text)
WHEN pd.payment_type = 'S'::bpchar THEN ( SELECT supplier_master.supplier_name
	FROM supplier_master
	WHERE supplier_master.supplier_code::text = pd.payee_name::text)
																																										ELSE pd.payee_name
END AS payee_name, pd.payee_name AS payeeid, pd.payment_type, pd.amount,
CASE
WHEN pd.payment_type = 'C'::bpchar THEN 'Cash Voucher'::text
WHEN pd.payment_type = 'D'::bpchar THEN 'Doctor'::text
WHEN pd.payment_type = 'P'::bpchar THEN 'Prescribing Doctor'::text
WHEN pd.payment_type = ANY (ARRAY['R'::bpchar, 'F'::bpchar]) THEN 'Referral Doctor'::text
WHEN pd.payment_type = 'O'::bpchar THEN 'Outgoing Tests'::text
WHEN pd.payment_type = 'S'::bpchar THEN 'Supplier Payments'::text
ELSE NULL::text
END AS paymenttype, pd.posted_date AS date, pd.username, pd.account_head, chc.chargehead_name, pd.category, pd.description,
CASE WHEN payment_type='D' THEN payment_id END AS doc_payment_id,
CASE WHEN payment_type in ('R','F') THEN payment_id END AS ref_payment_id,
CASE WHEN payment_type ='P' THEN payment_id END AS prescribing_dr_payment_id, charge_id	, payment_id,voucher_no,activity_id

FROM payments_details pd
LEFT JOIN chargehead_constants chc ON chc.account_head_id = pd.account_head;

DROP VIEW IF EXISTS payments_view CASCADE;
CREATE VIEW  payments_view AS
SELECT bc.charge_id, charge_group, doctor_amount, doc.doctor_id, date(b.finalized_date) AS finalized_date ,
b.bill_type, b.status AS bill_status, doc.doctor_name,b.visit_id, bc.amount, bc.discount,
bc.paid_amount, b.bill_no,pdoc.doctor_name AS prescribing_doctor_name,bc.prescribing_dr_id, prescribing_dr_amount, pr.reference_docto_id,
coalesce(rdoc.doctor_name, ref.referal_name) AS referal_doctor, bc.referal_amount, doc_payment_id, ref_payment_id,  prescribing_dr_payment_id,
tpa.tpa_name,pr.primary_sponsor_id,'' AS activity_id,bc.activity_conducted
FROM bill_charge bc
JOIN bill b using(bill_no)
LEFT JOIN doctors doc ON  doc.doctor_id =bc.payee_doctor_id
LEFT JOIN doctors pdoc ON pdoc.doctor_id =bc.prescribing_dr_id
LEFT JOIN patient_registration pr  ON pr.patient_id=b.visit_id
LEFT JOIN doctors rdoc ON rdoc.doctor_id = pr.reference_docto_id
LEFT JOIN referral ref ON (ref.referal_no = pr.reference_docto_id )
LEFT JOIN tpa_master tpa ON (tpa.tpa_id = pr.primary_sponsor_id)
WHERE  b.status in ('F','C','S')
AND bc.status!='X'

UNION ALL

SELECT bc.charge_id, COALESCE(bac.charge_group,bc.charge_group) AS charge_group, bac.doctor_amount, doc.doctor_id, date(b.finalized_date) AS finalized_date ,
b.bill_type, b.status AS bill_status, doc.doctor_name,b.visit_id, bc.amount, bc.discount,
bc.paid_amount, b.bill_no,NULL AS prescribing_doctor_name,NULL AS prescribing_dr_id, 0 AS prescribing_dr_amount, NULL AS reference_docto_id,
NULL  AS referal_doctor, 0 AS referal_amount, (bac.doctor_payment_id) AS doc_payment_id, ref_payment_id,  prescribing_dr_payment_id,
tpa.tpa_name,pr.primary_sponsor_id,bac.activity_id,bac.activity_conducted
FROM bill_activity_charge bac
JOIN bill_charge bc ON (bc.charge_id=bac.charge_id )
JOIN bill b using(bill_no)
LEFT JOIN doctors doc ON  doc.doctor_id =bac.doctor_id
LEFT JOIN patient_registration pr  ON pr.patient_id=b.visit_id
LEFT JOIN tpa_master tpa ON (tpa.tpa_id = pr.primary_sponsor_id)
WHERE  b.status in ('F','C','S')
AND bc.status!='X' ;


-- GG Custom report
DROP VIEW IF EXISTS pending_payments_view;
CREATE VIEW pending_payments_view AS
    SELECT foo.payment_type, foo.doctor_id, foo.prescribing_dr_id,
    foo.reference_docto_id, foo.tpa_name, foo.mr_no, foo.patient_id,
    foo.patname, foo.bill_no, foo.billamount, foo.discount,
    foo.paid_amount, foo.doctoramount, foo.payee_name,
    foo.date, foo.chargegroup_name, foo.tpaname, foo.primary_sponsor_id,
    foo.payment_id, foo.conducted
    FROM ((SELECT 'Conducting Doctor Payments' AS payment_type,
    		pv.doctor_id, NULL::unknown AS prescribing_dr_id,
    		NULL::unknown AS reference_docto_id, pv.tpa_name,
    		pd.mr_no, pd.patient_id, pd.patname, pv.bill_no,
    		pv.amount AS billamount, pv.discount, pv.paid_amount,
    		pv.doctor_amount AS doctoramount, pv.doctor_name AS payee_name,
    		pv.finalized_date AS date, cgc.chargegroup_name, pv.tpa_name AS tpaname,
    		pv.primary_sponsor_id, pv.doc_payment_id AS payment_id, pv.activity_conducted AS conducted
    		FROM ((payments_view pv
    				JOIN patientdetails pd ON (((pd.patient_id)::text = (pv.visit_id)::text)))
    				JOIN chargegroup_constants cgc ON (((cgc.chargegroup_id)::text = (pv.charge_group)::text)))
 	UNION ALL SELECT 'Prescribing Doctor Payments' AS payment_type,
		NULL::unknown AS doctor_id, pv.prescribing_dr_id,
		NULL::unknown AS reference_docto_id, pv.tpa_name, pd.mr_no,
		pd.patient_id, pd.patname, pv.bill_no, pv.amount AS billamount,
		pv.discount, pv.paid_amount, pv.prescribing_dr_amount AS doctoramount,
		pv.prescribing_doctor_name AS payee_name, pv.finalized_date AS date,
		cgc.chargegroup_name, pv.tpa_name AS tpaname, pv.primary_sponsor_id,
		pv.prescribing_dr_payment_id AS payment_id,
		pv.activity_conducted AS conducted
		FROM ((payments_view pv
			JOIN patientdetails pd ON (((pd.patient_id)::text = (pv.visit_id)::text)))
			JOIN chargegroup_constants cgc ON (((cgc.chargegroup_id)::text = (pv.charge_group)::text))))
	UNION ALL SELECT 'Referal Doctor Payments' AS payment_type,
		NULL::unknown AS doctor_id, NULL::unknown AS prescribing_dr_id,
		pv.reference_docto_id, pv.tpa_name, pd.mr_no, pd.patient_id,
		pd.patname, pv.bill_no, pv.amount AS billamount, pv.discount,
		pv.paid_amount, pv.referal_amount AS doctoramount,
		pv.referal_doctor AS payee_name, pv.finalized_date AS date,
		cgc.chargegroup_name, pv.tpa_name AS tpaname, pv.primary_sponsor_id,
		pv.ref_payment_id AS payment_id, pv.activity_conducted AS conducted
		FROM ((payments_view pv JOIN patientdetails pd ON (((pd.patient_id)::text = (pv.visit_id)::text)))
		JOIN chargegroup_constants cgc ON (((cgc.chargegroup_id)::text = (pv.charge_group)::text)))) foo
		WHERE (((foo.conducted = 'Y'::bpchar)
			AND (foo.payment_id IS NULL)) AND (foo.doctoramount > (0)::numeric));



DROP FUNCTION IF EXISTS releasebed_trigger() CASCADE;
CREATE OR REPLACE FUNCTION releasebed_trigger() RETURNS trigger AS $BODY$
BEGIN
	IF (new.status = 'P' AND old.status != 'P') THEN
	    UPDATE bed_names bn SET occupancy='N'
	    WHERE bed_id=new.bed_id
		OR bed_ref_id=new.bed_id
		OR (bed_id=(SELECT bed_ref_id FROM bed_names WHERE bed_id=new.bed_id)
		AND NOT EXISTS (
		SELECT bed_id FROM bed_names WHERE bed_ref_id=bn.bed_id AND occupancy = 'Y' AND bed_id!=new.bed_id)) ;
	END IF;
	IF(new.status = 'X' AND (old.status != 'P')) THEN
        UPDATE bed_names bn SET occupancy='N'
	    WHERE bed_id=new.bed_id
		OR bed_ref_id=new.bed_id
		OR (bed_id=(SELECT bed_ref_id FROM bed_names WHERE bed_id=new.bed_id)
		AND NOT EXISTS (
		SELECT bed_id FROM bed_names WHERE bed_ref_id=bn.bed_id AND occupancy = 'Y' AND bed_id!=new.bed_id)) ;
	END IF;

 RETURN NULL;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP FUNCTION IF EXISTS occupybed_trigger() CASCADE;
CREATE OR REPLACE FUNCTION occupybed_trigger() RETURNS trigger AS $BODY$
BEGIN
	UPDATE bed_names SET occupancy = 'Y' where bed_id = new.bed_id;
	RETURN NULL;
END;
$BODY$ LANGUAGE 'plpgsql';
DROP TRIGGER IF EXISTS bed_occupancy_trigger ON ip_bed_details;

CREATE TRIGGER bed_occupancy_trigger
    AFTER INSERT ON ip_bed_details
    FOR EACH ROW
    EXECUTE PROCEDURE occupybed_trigger();

DROP TRIGGER IF EXISTS bed_release_trigger ON ip_bed_details;
CREATE TRIGGER bed_release_trigger
    AFTER UPDATE ON ip_bed_details
    FOR EACH ROW
    EXECUTE PROCEDURE releasebed_trigger();

--
-- Insert a new row into dialysis_machine_status whenever a new row is added
-- in dialysis_machine_master.
--
DROP FUNCTION IF EXISTS dialysis_machine_added_trigger() CASCADE;
CREATE OR REPLACE FUNCTION dialysis_machine_added_trigger() RETURNS trigger AS $BODY$
BEGIN
	INSERT INTO dialysis_machine_status VALUES (NEW.machine_id);
	return NULL;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS dialysis_machine_master_trigger ON dialysis_machine_master;
CREATE TRIGGER dialysis_machine_master_trigger
	AFTER INSERT ON dialysis_machine_master
	FOR EACH ROW
	EXECUTE PROCEDURE dialysis_machine_added_trigger();

--
-- Trigger WHEN the details of a dialysis machine are updated. The status params
-- are copied into the dialysis_session_parameters table ON certain conditions. Aggregate
-- values are updated always.
--
DROP FUNCTION IF EXISTS dialysis_machine_status_update() CASCADE;
CREATE OR REPLACE FUNCTION dialysis_machine_status_update() RETURNS trigger AS $BODY$
DECLARE
	doCopy boolean;
	orderId integer;
	sessionStatus text;
	lastSaved timestamp;
	startBpHigh integer;
	startBpLow integer;
	timediff text;
	abnormalBP text;
	numAlerts integer;
BEGIN
	doCopy := false;
	lastSaved := NULL;
	timediff := '30 MINUTES';
	abnormalBP := 'N';
	numAlerts := 0;

	-- Get the session (order_id) that is associated with this machine.
	SELECT order_id, status FROM dialysis_session WHERE order_id = NEW.assigned_order_id
		INTO orderId, sessionStatus;

	-- if not associated to a session, ignore the results.
	IF orderId IS NULL THEN
		return NEW;
	END IF;

	-- if the session is closed, ignore the results (assigned_order_id should have been null
	-- in this case, but just to safeguard against UI bugs WHEN the user changes the status.
	IF sessionStatus = 'C' THEN
		return NEW;
	END IF;

	-- If status has changed from dialyzing to not, need to copy the previous set of parameters
	-- into the session AS the final observation.
	IF OLD.polled_status = 'D' AND NEW.polled_status = 'N' THEN
		INSERT INTO dialysis_session_parameters (observation_id, order_id, obs_time,
			polled_status, uf_goal, uf_removed, uf_rate, blood_pump_rate,
			heparin_rate, dialysate_temp, dialysate_cond, venous_pressure,
			dialysate_pressure, tmp, dialysis_time, dialysate_rate, tmp_alarm,
			air_alarm, blood_leak_alarm, other_alarm, treatment_mode, subst_goal,
			subst_transit_val, subst_temp, bp_time, bp_high, bp_low, pulse_rate,
			bp_alarm, temperature_alarm, conductivity_alarm, venous_pressure_alarm,
			dialysate_pressure_alarm, subst_rate)
		VALUES (nextval('dialysis_session_parameters_seq'), orderId, OLD.last_polled_time,
			OLD.polled_status, OLD.uf_goal, OLD.uf_removed, OLD.uf_rate, OLD.blood_pump_rate,
			OLD.heparin_rate, OLD.dialysate_temp, OLD.dialysate_cond, OLD.venous_pressure,
			OLD.dialysate_pressure, OLD.tmp, OLD.dialysis_time, OLD.dialysate_rate, OLD.tmp_alarm,
			OLD.air_alarm, OLD.blood_leak_alarm, OLD.other_alarm, OLD.treatment_mode, OLD.subst_goal,
			OLD.subst_transit_val, OLD.subst_temp, OLD.bp_time, OLD.bp_high, OLD.bp_low, OLD.pulse_rate,
			OLD.bp_alarm, OLD.temperature_alarm, OLD.conductivity_alarm, OLD.venous_pressure_alarm,
			OLD.dialysate_pressure_alarm, OLD.subst_rate);

		IF sessionStatus = 'I' THEN
			UPDATE dialysis_session SET status = 'F', end_time = current_timestamp WHERE order_id = orderId;
		END IF;
		NEW.assigned_status = 'U';
		NEW.assigned_order_id = NULL;
	END IF;

	-- If not dialyzing, nothing more to do
	IF NEW.polled_status != 'D' THEN
		return NEW;
	END IF;

	-- If original session status = prepared, auto convert the session into In-Progress.
	IF sessionStatus = 'P' THEN
		UPDATE dialysis_session SET status = 'I', start_time = current_timestamp WHERE order_id = orderId;
	END IF;

	-- If last saved set of parameters is older than 30 minutes (or 15 if BP variation is high) minutes,
	-- trigger a copy.
	SELECT bp_high, bp_low FROM dialysis_session_parameters WHERE order_id = orderId and obs_type='F'
 		INTO startBpHigh, startBpLow;

	IF startBpHigh IS NOT NULL AND NEW.bp_high IS NOT NULL AND NEW.bp_low IS NOT NULL THEN
 		IF NEW.bp_high >= (startBpHigh + 40) OR NEW.bp_high <= (startBpHigh - 40) OR
			NEW.bp_low >= (startBpLow + 20) OR NEW.bp_low <= (startBpLow - 20) THEN
			timediff = '15 MINUTES';
			abnormalBP := 'Y';
		END IF;
	END IF;

	SELECT max(obs_time) FROM dialysis_session_parameters WHERE order_id = orderId
		INTO lastSaved;
	IF lastSaved IS NULL THEN
		doCopy := true;
	ELSIF lastSaved + timediff::interval <= current_timestamp THEN
		doCopy := true;
	END IF;

	--
	-- If a new BP measurement has been taken, trigger a copy. Also record an incident if
	-- BP is abnormal. That this will trigger multiple incidents if BP continues to
	-- be abnormal. Let's hope this is what is required.
	--
	IF (NEW.bp_time != OLD.bp_time) THEN
		doCopy := true;
		IF abnormalBP = 'Y' THEN
			INSERT INTO dialysis_session_incidents (incident_id, order_id, incident_time, username,
				description)
			VALUES (nextval('dialysis_session_incidents_seq'), orderId, current_timestamp, 'auto',
				'Abnormal BP Detected');
		END IF;
	END IF;

	--
	-- If any alarm is newly set, THEN trigger a copy, also record an incident.
	--
	IF (OLD.tmp_alarm = '0' AND NEW.tmp_alarm = '1') THEN
		doCopy := true;
		INSERT INTO dialysis_session_incidents (incident_id, order_id, incident_time, username, description)
		VALUES (nextval('dialysis_session_incidents_seq'), orderId, current_timestamp, 'machine',
				'TMP Alarm generated by dialysis machine');
		numAlerts := numAlerts +1;
	END IF;
	IF (OLD.air_alarm = '0' AND NEW.air_alarm = '1') THEN
		doCopy := true;
		INSERT INTO dialysis_session_incidents (incident_id, order_id, incident_time, username, description)
		VALUES (nextval('dialysis_session_incidents_seq'), orderId, current_timestamp, 'machine',
				'Air Alarm generated by dialysis machine');
		numAlerts := numAlerts +1;
	END IF;
	IF (OLD.blood_leak_alarm = '0' AND NEW.blood_leak_alarm = '1') THEN
		doCopy := true;
		INSERT INTO dialysis_session_incidents (incident_id, order_id, incident_time, username, description)
		VALUES (nextval('dialysis_session_incidents_seq'), orderId, current_timestamp, 'machine',
				'Blood Leak Alarm generated by dialysis machine');
		numAlerts := numAlerts +1;
	END IF;
	IF (OLD.other_alarm = '0' AND NEW.other_alarm = '1') THEN
		doCopy := true;
		INSERT INTO dialysis_session_incidents (incident_id, order_id, incident_time, username, description)
		VALUES (nextval('dialysis_session_incidents_seq'), orderId, current_timestamp, 'machine',
				'Other Alarm generated by dialysis machine');
		numAlerts := numAlerts +1;
	END IF;
	IF (OLD.temperature_alarm = '0' AND NEW.temperature_alarm = '1') THEN
		doCopy := true;
		INSERT INTO dialysis_session_incidents (incident_id, order_id, incident_time, username, description)
		VALUES (nextval('dialysis_session_incidents_seq'), orderId, current_timestamp, 'machine',
				'Temperature Alarm generated by dialysis machine');
		numAlerts := numAlerts +1;
	END IF;
	IF (OLD.conductivity_alarm = '0' AND NEW.conductivity_alarm = '1') THEN
		doCopy := true;
		INSERT INTO dialysis_session_incidents (incident_id, order_id, incident_time, username, description)
		VALUES (nextval('dialysis_session_incidents_seq'), orderId, current_timestamp, 'machine',
				'Conductivity Alarm generated by dialysis machine');
		numAlerts := numAlerts +1;
	END IF;
	IF (OLD.venous_pressure_alarm = '0' AND NEW.venous_pressure_alarm = '1') THEN
		doCopy := true;
		INSERT INTO dialysis_session_incidents (incident_id, order_id, incident_time, username, description)
		VALUES (nextval('dialysis_session_incidents_seq'), orderId, current_timestamp, 'machine',
				'Venous Pressure Alarm generated by dialysis machine');
		numAlerts := numAlerts +1;
	END IF;
	IF (OLD.dialysate_pressure_alarm = '0' AND NEW.dialysate_pressure_alarm = '1') THEN
		doCopy := true;
		INSERT INTO dialysis_session_incidents (incident_id, order_id, incident_time, username, description)
		VALUES (nextval('dialysis_session_incidents_seq'), orderId, current_timestamp, 'machine',
				'Dialysate Pressure Alarm generated by dialysis machine');
		numAlerts := numAlerts +1;
	END IF;
	IF (OLD.bp_alarm = '0' AND NEW.bp_alarm = '1') THEN
		doCopy := true;
		INSERT INTO dialysis_session_incidents (incident_id, order_id, incident_time, username, description)
		VALUES (nextval('dialysis_session_incidents_seq'), orderId, current_timestamp, 'machine',
				'BP Alarm generated by dialysis machine');
		numAlerts := numAlerts +1;
	END IF;

	--
	-- Save the session parameters AS a new set based ON above criteria.
	--
	IF doCopy THEN
		INSERT INTO dialysis_session_parameters (observation_id, order_id, obs_time,
			polled_status, uf_goal, uf_removed, uf_rate, blood_pump_rate,
			heparin_rate, dialysate_temp, dialysate_cond, venous_pressure,
			dialysate_pressure, tmp, dialysis_time, dialysate_rate, tmp_alarm,
			air_alarm, blood_leak_alarm, other_alarm, treatment_mode, subst_goal,
			subst_transit_val, subst_temp, bp_time, bp_high, bp_low, pulse_rate,
			bp_alarm, temperature_alarm, conductivity_alarm, venous_pressure_alarm,
			dialysate_pressure_alarm, subst_rate)
		VALUES (nextval('dialysis_session_parameters_seq'), orderId, NEW.last_polled_time,
			NEW.polled_status, NEW.uf_goal, NEW.uf_removed, NEW.uf_rate, NEW.blood_pump_rate,
			NEW.heparin_rate, NEW.dialysate_temp, NEW.dialysate_cond, NEW.venous_pressure,
			NEW.dialysate_pressure, NEW.tmp, NEW.dialysis_time, NEW.dialysate_rate, NEW.tmp_alarm,
			NEW.air_alarm, NEW.blood_leak_alarm, NEW.other_alarm, NEW.treatment_mode, NEW.subst_goal,
			NEW.subst_transit_val, NEW.subst_temp, NEW.bp_time, NEW.bp_high, NEW.bp_low, NEW.pulse_rate,
			NEW.bp_alarm, NEW.temperature_alarm, NEW.conductivity_alarm, NEW.venous_pressure_alarm,
			NEW.dialysate_pressure_alarm, NEW.subst_rate);
	END IF;

	--
	-- update the aggregates stored in the session anyway: whether or not we are doing a copy.
	--
	UPDATE dialysis_session SET last_polled_time = current_timestamp, poll_count=coalesce(poll_count,0)+1,
		total_venous_pressure = coalesce(total_venous_pressure,0) + NEW.venous_pressure,
		total_dialysate_pressure = coalesce(total_dialysate_pressure,0) + NEW.dialysate_pressure,
		total_pulse = coalesce(total_pulse,0) + NEW.pulse_rate,
		alerts = coalesce(alerts,0) + numAlerts
	WHERE order_id = orderId;

	--
	-- Set the min bp and corresponding time if bp is less than the last saved min bp
	--
	UPDATE dialysis_session SET intra_min_bp_high = NEW.bp_high, intra_min_bp_low = NEW.bp_low,
		min_bp_time = NEW.bp_time
	WHERE order_id = orderId AND (intra_min_bp_high IS NULL OR NEW.bp_high < intra_min_bp_high);

	return NEW;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS dialysis_machine_status_trigger ON dialysis_machine_status;
CREATE TRIGGER dialysis_machine_status_trigger
	BEFORE UPDATE ON dialysis_machine_status
	FOR EACH ROW
	EXECUTE PROCEDURE dialysis_machine_status_update();

--
-- Function/trigger to update the obs_type to F/L for first and last observations in
-- a dialysis session. To be called WHEN inserting/deleting a row into dialysis_session_parameters
--
DROP FUNCTION IF EXISTS dialysis_session_parameter_trigger() CASCADE;
CREATE OR REPLACE FUNCTION dialysis_session_parameter_trigger() RETURNS TRIGGER AS $BODY$
DECLARE
	orderId integer;
	maxObs integer;
	minObs integer;
BEGIN
	IF (TG_OP = 'INSERT') THEN
		orderId := NEW.order_id;
	ELSIF (TG_OP = 'DELETE') THEN
		orderId := OLD.order_id;
	END IF;

	SELECT max(observation_id) FROM dialysis_session_parameters WHERE order_id = orderId INTO maxObs;
	SELECT min(observation_id) FROM dialysis_session_parameters WHERE order_id = orderId INTO minObs;

	-- order is important, since there could be just 1 or 2 observations.
	UPDATE dialysis_session_parameters SET obs_type = 'M' WHERE order_id = orderId
		AND observation_id NOT IN (maxObs, minObs);
	UPDATE dialysis_session_parameters SET obs_type = 'L' WHERE observation_id = maxObs;
	UPDATE dialysis_session_parameters SET obs_type = 'F' WHERE observation_id = minObs;
	RETURN NULL;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS dialysis_session_parameter_trigger ON dialysis_session_parameters;
CREATE TRIGGER dialysis_session_parameter_trigger
	AFTER INSERT OR DELETE ON dialysis_session_parameters
	FOR EACH ROW
	EXECUTE PROCEDURE dialysis_session_parameter_trigger();



DROP VIEW IF EXISTS conducting_doctor_payment_status_view CASCADE ;
CREATE OR REPLACE VIEW conducting_doctor_payment_status_view AS
SELECT bill_no, date(finalized_date) AS date, b.visit_type, chargehead_name,
                get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS patient_full_name,
                doctor_name, sum(doctor_amount) AS doctor_amount,
                (select voucher_no from payments_details pd where pd.payment_id=bc.doc_payment_id) AS payment_voucher
        FROM bill_charge bc
        JOIN doctors d ON (bc.payee_doctor_id=d.doctor_id)
        JOIN bill b USING(bill_no)
        JOIN chargehead_constants c ON (bc.charge_head = c.chargehead_id)
        JOIN patient_registration pr ON (b.visit_id=pr.patient_id)
        JOIN patient_details pd using (mr_no)
		LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
        WHERE b.status IN ('F','S','C') AND bc.status != 'X' AND doctor_amount>0 AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )
        GROUP BY bill_no, finalized_date, b.visit_type, payee_doctor_id, doctor_name, b.mod_time, bc.doc_payment_id, c.chargehead_name, patient_full_name
;

DROP VIEW IF EXISTS bill_amounts_account_head_view;
CREATE OR REPLACE VIEW bill_amounts_account_head_view AS
 SELECT b.bill_no, b.finalized_date, b.is_tpa, tm.tpa_name AS primary_sponsor_name, stpa.tpa_name AS secondary_sponsor_name,
 	-- coalesce(sbah.account_head_name, bah.account_head_name) is not grouping if the service sub group and charge head are connected with same account head name.
 	-- hence used CASE WHEN statement. ref: bug 34813.
 	case WHEN sbah.account_head_name is null THEN bah.account_head_name ELSE sbah.account_head_name END AS account_head_name,
 	max(b.insurance_deduction) AS insurance_deduction,
 	sum(bc.amount) AS amount, sum(bc.discount) AS item_discount, b.primary_total_claim, b.secondary_total_claim, b.total_receipts,
	coalesce(b.deposit_set_off, 0) AS deposit_set_off, coalesce(b.points_redeemed_amt, 0) AS points_redeemed_amt,
	b.bill_type, min(b.visit_type) AS visit_type, min(b.restriction_type) AS restriction_type,
	COALESCE(pd.patient_name, isr.patient_name) AS patient_name, pd.last_name, sm.salutation,
	get_patient_full_name(sm.salutation,
		COALESCE(pd.patient_name, isr.patient_name), pd.middle_name, pd.last_name) AS patient_full_name,
	b.no_of_receipts, r.remarks AS receipt_remarks,
	r.payment_mode_id,pm.payment_mode AS receipt_payment_mode,ctm.card_type,r.card_type_id,
	r.display_date AS display_date, 0 AS final_tax, 0 AS discount, 0 AS round_off,
	'HOSP' AS sale_id, -1 AS vat_rate, 'H' AS type, b.mod_time, b.account_group,
	'HOSP' AS inter_comp_acc_group, -1 AS inter_comp_account_group_id,
	'Hospital Item' AS charge_item_type, 0 AS med_category_id,
	'' AS sales_cat_vat_account_prefix, '' AS sales_store_vat_account_prefix,
	spl_account_name AS payment_mode_account,
	r.bank_batch_no, r.card_auth_code, r.card_holder_name, r.currency_id, r.exchange_rate,
	r.exchange_date, r.currency_amt, r.card_exp_date AS card_expdate, r.card_number, fc.currency, hcm.center_code, r.bank_name AS bank,
	pm.ref_required, pm.bank_required, min(rhcm.center_code) AS receipt_center_code, min(pr.op_type) AS op_type,
	min(d.cost_center_code) AS dept_center_code, min(c.collection_counter) AS collection_counter, hcm.center_id AS visit_center_id,
	min(pr.mr_no) AS mr_no
 FROM bill_charge bc
    JOIN chargehead_constants cc ON (cc.chargehead_id=bc.charge_head)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
 	JOIN bill b USING (bill_no)
 	-- left join required ON sub groups, for ex: round off's there will be no service sub group.
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=bc.service_sub_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	LEFT JOIN bill_receipts br ON (b.last_receipt_no=br.receipt_no)
	JOIN receipts r ON (r.receipt_id=br.receipt_no)
	LEFT JOIN counters c ON (r.counter=c.counter_id)
	LEFT JOIN hospital_center_master rhcm ON (rhcm.center_id=c.center_id)
	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
	LEFT JOIN patient_details pd ON pd.mr_no = pr.mr_no
	LEFT JOIN incoming_sample_registration isr ON isr.incoming_visit_id = b.visit_id
	LEFT JOIN salutation_master sm ON sm.salutation_id = pd.salutation
	LEFT JOIN tpa_master tm ON tm.tpa_id = pr.primary_sponsor_id
	LEFT JOIN tpa_master stpa ON stpa.tpa_id::text = pr.secondary_sponsor_id::text
	LEFT JOIN payment_mode_master pm ON (r.payment_mode_id = pm.mode_id)
	LEFT JOIN card_type_master ctm ON (ctm.card_type_id = r.card_type_id)
	LEFT JOIN foreign_currency fc ON (fc.currency_id = r.currency_id)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id or hcm.center_id=isr.center_id) -- this has to be left join for retail credit bills we can add round offs.
 WHERE b.status in ('S', 'C', 'F') AND bc.status != 'X' AND bc.charge_head not in ('PHMED','PHRET','PHCMED','PHCRET','INVITE', 'INVRET') AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )
 GROUP BY b.bill_no, b.account_group, b.is_tpa, tm.tpa_name, stpa.tpa_name, b.finalized_date,
	case WHEN sbah.account_head_name is null THEN bah.account_head_name ELSE sbah.account_head_name end,
	b.deposit_set_off, b.points_redeemed_amt,
 	b.bill_type, pd.patient_name, isr.patient_name, pd.last_name, sm.salutation, pd.middle_name,
 	b.no_of_receipts, r.remarks, r.payment_mode_id,pm.payment_mode,r.card_type_id,ctm.card_type,
 	r.display_date, b.mod_time, b.primary_total_claim, b.secondary_total_claim, b.total_receipts, spl_account_name,
 	r.bank_batch_no, r.card_auth_code, r.card_holder_name, r.currency_id, r.exchange_rate,
	r.exchange_date, r.currency_amt, r.card_exp_date, r.card_number, fc.currency, hcm.center_code, r.bank_name, pm.ref_required,
	pm.bank_required, hcm.center_id

 UNION ALL
	-- sales and sales returns
 SELECT b.bill_no, b.finalized_date, b.is_tpa, tm.tpa_name AS primary_sponsor_name, stpa.tpa_name AS secondary_sponsor_name,
 	case WHEN sbah.account_head_name is null THEN bah.account_head_name ELSE sbah.account_head_name END AS account_head_name,
 	max(b.insurance_deduction) AS insurance_deduction,
 	sum(pms.amount) AS amount, sum(pms.disc) AS item_discount, b.primary_total_claim, b.secondary_total_claim,
 	b.total_receipts, coalesce(b.deposit_set_off, 0) AS deposit_set_off, coalesce(b.points_redeemed_amt, 0) AS points_redeemed_amt,
 	b.bill_type, min(b.visit_type) AS visit_type, min(b.restriction_type) AS restriction_type,
	COALESCE(pd.patient_name, prc.customer_name) AS patient_name, pd.last_name, sm.salutation,
	get_patient_full_name(sm.salutation, COALESCE(pd.patient_name, prc.customer_name), pd.middle_name, pd.last_name) AS patient_full_name,
	b.no_of_receipts, r.remarks AS receipt_remarks,
	r.payment_mode_id ,pm.payment_mode AS receipt_payment_mode,ctm.card_type,r.card_type_id,
	r.display_date AS display_date, sum(pms.tax) AS final_tax, pmsm.discount, pmsm.round_off,
	pmsm.sale_id, pms.tax_rate AS vat_rate, pmsm.type, b.mod_time, b.account_group, gm.inter_comp_acc_name,
	gm.account_group_id AS inter_comp_account_group_id,
	'Pharmacy Credit Item' AS charge_item_type, scm.category_id, scm.sales_cat_vat_account_prefix, gd.sales_store_vat_account_prefix,
	spl_account_name AS payment_mode_account,
	r.bank_batch_no, r.card_auth_code, r.card_holder_name, r.currency_id, r.exchange_rate,
	r.exchange_date, r.currency_amt, r.card_exp_date AS card_expdate, r.card_number, fc.currency,
	vhcm.center_code, r.bank_name AS bank, pm.ref_required, pm.bank_required, min(rhcm.center_code) AS receipt_center_code,
	min(pr.op_type) AS op_type, min(d.cost_center_code) AS dept_center_code, min(c.collection_counter) AS collection_counter,
	vhcm.center_id AS visit_center_id, min(pr.mr_no) AS mr_no
 FROM store_sales_main pmsm
 	JOIN bill b ON b.bill_no = pmsm.bill_no
    JOIN chargehead_constants cc ON (cc.chargehead_id=(case WHEN b.bill_type='P' and pmsm.type='S' THEN 'PHMED'
		when b.bill_type='P' and pmsm.type='R' THEN 'PHRET'
		when b.bill_type='C' and pmsm.type='S' THEN 'PHCMED'
		when b.bill_type='C' and pmsm.type='R' THEN 'PHCRET'
		else 'HOSPITAL_OR_ISSUE_ITEM' end))
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN store_sales_details pms ON pmsm.sale_id = pms.sale_id
	JOIN store_item_details sid ON pms.medicine_id=sid.medicine_id
	JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=sid.service_sub_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	JOIN store_category_master scm ON scm.category_id=sid.med_category_id
	JOIN stores gd ON pmsm.store_id=gd.dept_id
	LEFT JOIN bill_receipts br ON (b.last_receipt_no=br.receipt_no)
	JOIN receipts r ON (r.receipt_id=br.receipt_no)
	LEFT JOIN counters c ON (r.counter=c.counter_id)
	LEFT JOIN hospital_center_master rhcm ON (c.center_id=rhcm.center_id)
	LEFT JOIN patient_registration pr ON pr.patient_id = b.visit_id
	LEFT JOIN patient_details pd ON pd.mr_no = pr.mr_no
	LEFT JOIN salutation_master sm ON sm.salutation_id = pd.salutation
	LEFT JOIN store_retail_customers prc ON prc.customer_id = b.visit_id
	LEFT JOIN tpa_master tm ON tm.tpa_id = pr.primary_sponsor_id
	LEFT JOIN tpa_master stpa ON stpa.tpa_id::text = pr.secondary_sponsor_id::text
	LEFT JOIN payment_mode_master pm ON (r.payment_mode_id = pm.mode_id)
	LEFT JOIN card_type_master ctm ON (r.card_type_id = ctm.card_type_id)
	LEFT JOIN foreign_currency fc ON (fc.currency_id = r.currency_id)
	LEFT JOIN hospital_center_master vhcm ON (vhcm.center_id=pr.center_id or vhcm.center_id=prc.center_id)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)
	JOIN account_group_master gm ON gm.account_group_id=gd.account_group -- (dont move this to the top joins: performance issue)
 WHERE b.status in ('S', 'C', 'F') AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )

 GROUP BY b.bill_no, b.account_group, b.is_tpa, tm.tpa_name, stpa.tpa_name, b.finalized_date,
 	case WHEN sbah.account_head_name is null THEN bah.account_head_name ELSE sbah.account_head_name end,
 	pms.tax_rate, pmsm.discount, b.deposit_set_off, b.points_redeemed_amt,
 	pmsm.round_off, pmsm.sale_id, gm.inter_comp_acc_name, inter_comp_account_group_id, pmsm.type,
 	pd.patient_name, prc.customer_name, pd.last_name, sm.salutation, pd.middle_name, r.remarks, r.payment_mode_id,
 	pm.payment_mode,r.card_type_id,ctm.card_type,b.no_of_receipts, b.bill_type, b.mod_time, r.display_date,
 	b.primary_total_claim, b.secondary_total_claim, b.total_receipts, scm.category_id,
 	scm.sales_cat_vat_account_prefix, gd.sales_store_vat_account_prefix, spl_account_name,
 	r.bank_batch_no, r.card_auth_code, r.card_holder_name, r.currency_id, r.exchange_rate,
	r.exchange_date, r.currency_amt, r.card_exp_date, r.card_number, fc.currency,
	vhcm.center_code, r.bank_name, pm.ref_required,	pm.bank_required, vhcm.center_id

 UNION ALL
	-- issues
	-- issues should be added to hospital bills hence grouped by visit center code
 SELECT b.bill_no, b.finalized_date, b.is_tpa, tm.tpa_name AS primary_sponsor_name, stpa.tpa_name AS secondary_sponsor_name,
 	case WHEN sbah.account_head_name is null THEN bah.account_head_name ELSE sbah.account_head_name END AS account_head_name,
 	max(b.insurance_deduction) AS insurance_deduction,
 	sum(bc.amount) AS amount, sum(bc.discount) AS item_discount, b.primary_total_claim, b.secondary_total_claim,
 	b.total_receipts, coalesce(b.deposit_set_off, 0) AS deposit_set_off, coalesce(b.points_redeemed_amt, 0) AS points_redeemed_amt,
 	b.bill_type, min(b.visit_type) AS visit_type, min(b.restriction_type) AS restriction_type,
	pd.patient_name AS patient_name, pd.last_name, sm.salutation,
	get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS patient_full_name,
	b.no_of_receipts, r.remarks AS receipt_remarks,
	r.payment_mode_id ,pm.payment_mode AS receipt_payment_mode,ctm.card_type,r.card_type_id,
	r.display_date AS display_date, round(sum(((isu.amount*isu.qty*isu.vat)/100)),2) AS final_tax, 0 AS discount, 0 AS round_off,
	isum.user_issue_no::text, isu.vat AS vat_rate, 'ISSUE' AS type, b.mod_time, b.account_group, gm.inter_comp_acc_name,
	gm.account_group_id AS inter_comp_account_group_id,
	'Store Issue Credit Item' AS charge_item_type, scm.category_id, scm.sales_cat_vat_account_prefix, s.sales_store_vat_account_prefix,
	spl_account_name AS payment_mode_account,
	r.bank_batch_no, r.card_auth_code, r.card_holder_name, r.currency_id, r.exchange_rate,
	r.exchange_date, r.currency_amt, r.card_exp_date AS card_expdate, r.card_number, fc.currency, hcm.center_code, r.bank_name AS bank,
	pm.ref_required, pm.bank_required, min(rhcm.center_code) AS receipt_center_code, min(pr.op_type) AS op_type,
	min(d.cost_center_code) AS dept_center_code, min(c.collection_counter) AS collection_counter, hcm.center_id AS visit_center_id,
	min(pr.mr_no) AS mr_no
 FROM stock_issue_details isu
 	JOIN bill_activity_charge bac ON isu.item_issue_no::text = bac.activity_id AND bac.activity_code = ('PHI')
	JOIN bill_charge bc ON bc.charge_id = bac.charge_id
    JOIN chargehead_constants cc ON (cc.chargehead_id=bc.charge_head)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN bill b ON b.bill_no = bc.bill_no
	JOIN stock_issue_main isum ON isu.user_issue_no = isum.user_issue_no
	JOIN stores s ON s.dept_id=isum.dept_from
	JOIN account_group_master gm ON gm.account_group_id=bc.account_group
	JOIN store_item_details sid ON sid.medicine_id=isu.medicine_id
	JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=sid.service_sub_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	JOIN store_category_master scm ON scm.category_id=sid.med_category_id
	JOIN patient_registration pr ON pr.patient_id = b.visit_id
	JOIN patient_details pd ON pd.mr_no = pr.mr_no
	JOIN hospital_center_master hcm ON (hcm.center_id=pr.center_id)
	LEFT JOIN bill_receipts br ON (b.last_receipt_no=br.receipt_no)
	JOIN receipts r ON (r.receipt_id=br.receipt_no)
	LEFT JOIN counters c ON (r.counter=c.counter_id)
	LEFT JOIN hospital_center_master rhcm ON (rhcm.center_id=c.center_id)
	LEFT JOIN salutation_master sm ON sm.salutation_id = pd.salutation
	LEFT JOIN tpa_master tm ON tm.tpa_id = pr.primary_sponsor_id
	LEFT JOIN tpa_master stpa ON stpa.tpa_id::text = pr.secondary_sponsor_id::text
	LEFT JOIN payment_mode_master pm ON (r.payment_mode_id = pm.mode_id)
	LEFT JOIN card_type_master ctm ON (r.card_type_id = ctm.card_type_id)
	LEFT JOIN foreign_currency fc ON (fc.currency_id = r.currency_id)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)

 WHERE b.status in ('S', 'C', 'F') AND bc.status != 'X' and bc.charge_head = 'INVITE' AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )
 GROUP BY b.bill_no, b.account_group, b.is_tpa, tm.tpa_name, stpa.tpa_name, b.finalized_date,
 	case WHEN sbah.account_head_name is null THEN bah.account_head_name ELSE sbah.account_head_name end,
 	isu.vat, b.deposit_set_off, b.points_redeemed_amt,
 	isum.user_issue_no, gm.inter_comp_acc_name, inter_comp_account_group_id,
 	pd.patient_name,pd.last_name, sm.salutation, pd.middle_name, r.remarks, r.payment_mode_id,
 	pm.payment_mode,r.card_type_id,ctm.card_type, b.no_of_receipts, b.bill_type, b.mod_time, r.display_date,
 	b.primary_total_claim, b.secondary_total_claim, b.total_receipts,
 	scm.category_id, scm.sales_cat_vat_account_prefix, s.sales_store_vat_account_prefix, spl_account_name,
 	r.bank_batch_no, r.card_auth_code, r.card_holder_name, r.currency_id, r.exchange_rate,
	r.exchange_date, r.currency_amt, r.card_exp_date, r.card_number, fc.currency, hcm.center_code, r.bank_name, pm.ref_required,
	pm.bank_required, hcm.center_id

UNION ALL
	-- issue returns
	-- issue returns should be added to hospital bills hence grouped by visit center code
SELECT b.bill_no, b.finalized_date, b.is_tpa, tm.tpa_name AS primary_sponsor_name, stpa.tpa_name AS secondary_sponsor_name,
	case WHEN sbah.account_head_name is null THEN bah.account_head_name ELSE sbah.account_head_name END AS account_head_name,
	max(b.insurance_deduction) AS insurance_deduction,
 	sum(bc.amount) AS amount, sum(bc.discount) AS item_discount, b.primary_total_claim, b.secondary_total_claim,
 	b.total_receipts, coalesce(b.deposit_set_off, 0) AS deposit_set_off, coalesce(b.points_redeemed_amt, 0) AS points_redeemed_amt,
 	b.bill_type, min(b.visit_type) AS visit_type, min(b.restriction_type) AS restriction_type,
	pd.patient_name AS patient_name, pd.last_name, sm.salutation,
	get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS patient_full_name,
	b.no_of_receipts, r.remarks AS receipt_remarks,
	r.payment_mode_id ,pm.payment_mode AS receipt_payment_mode,ctm.card_type,r.card_type_id,
	r.display_date AS display_date, 0 AS final_tax, 0 AS discount, 0 AS round_off,
	sirm.user_return_no::text, 0 AS vat_rate, 'ISSUE_RETURN' AS type, b.mod_time, b.account_group, gm.inter_comp_acc_name,
	gm.account_group_id AS inter_comp_account_group_id,
	'Store Return Credit Item' AS charge_item_type, scm.category_id, scm.sales_cat_vat_account_prefix, s.sales_store_vat_account_prefix,
	spl_account_name AS payment_mode_account,
	r.bank_batch_no, r.card_auth_code, r.card_holder_name, r.currency_id, r.exchange_rate,
	r.exchange_date, r.currency_amt, r.card_exp_date AS card_exp_date, r.card_number, fc.currency, hcm.center_code,
	r.bank_name AS bank, pm.ref_required, pm.bank_required, min(rhcm.center_code) AS receipt_center_code, min(pr.op_type) AS op_type,
	min(d.cost_center_code) AS dept_center_code, min(c.collection_counter) AS collection_counter, hcm.center_id AS visit_center_id,
	min(pr.mr_no) AS mr_no
 FROM store_issue_returns_details sird
 	JOIN bill_activity_charge bac ON sird.item_return_no::text = bac.activity_id AND bac.activity_code = ('PHI')
	JOIN bill_charge bc ON bc.charge_id = bac.charge_id
    JOIN chargehead_constants cc ON (cc.chargehead_id=bc.charge_head)
	JOIN bill_account_heads bah ON (cc.account_head_id = bah.account_head_id)
	JOIN bill b ON b.bill_no = bc.bill_no
	JOIN store_issue_returns_main sirm ON sirm.user_return_no = sird.user_return_no
	JOIN stores s ON s.dept_id=sirm.dept_to
	JOIN account_group_master gm ON gm.account_group_id=bc.account_group
	JOIN store_item_details sid ON sid.medicine_id=sird.medicine_id
	JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=sid.service_sub_group_id)
	LEFT JOIN bill_account_heads sbah ON (ssg.account_head_id=sbah.account_head_id)
	JOIN store_category_master scm ON scm.category_id=sid.med_category_id
	JOIN patient_registration pr ON pr.patient_id = b.visit_id
	JOIN patient_details pd ON pd.mr_no = pr.mr_no
	JOIN hospital_center_master hcm ON pr.center_id=hcm.center_id
	LEFT JOIN bill_receipts br ON (b.last_receipt_no=br.receipt_no)
	JOIN receipts r ON (r.receipt_id=br.receipt_no)
	LEFT JOIN counters c ON (r.counter=c.counter_id)
	LEFT JOIN hospital_center_master rhcm ON (c.center_id=rhcm.center_id)
	LEFT JOIN salutation_master sm ON sm.salutation_id = pd.salutation
	LEFT JOIN tpa_master tm ON tm.tpa_id = pr.primary_sponsor_id
	LEFT JOIN tpa_master stpa ON stpa.tpa_id::text = pr.secondary_sponsor_id::text
	LEFT JOIN payment_mode_master pm ON (r.payment_mode_id = pm.mode_id)
	LEFT JOIN card_type_master ctm ON (r.card_type_id = ctm.card_type_id)
	LEFT JOIN foreign_currency fc ON (fc.currency_id = r.currency_id)
	LEFT JOIN department d ON (d.dept_id=pr.dept_name)

 WHERE b.status in ('S', 'C', 'F') AND bc.status != 'X' and bc.charge_head = 'INVRET' 
 AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )
 GROUP BY b.bill_no, b.account_group, b.is_tpa, tm.tpa_name, stpa.tpa_name, b.finalized_date,
 	case WHEN sbah.account_head_name is null THEN bah.account_head_name ELSE sbah.account_head_name end,
 	b.deposit_set_off, b.points_redeemed_amt,
 	sirm.user_return_no, gm.inter_comp_acc_name, inter_comp_account_group_id,
 	pd.patient_name,pd.last_name, sm.salutation, pd.middle_name, r.remarks, r.payment_mode_id,
 	pm.payment_mode,r.card_type_id,ctm.card_type, b.no_of_receipts, b.bill_type, b.mod_time, r.display_date,
 	b.primary_total_claim, b.secondary_total_claim, b.total_receipts,
 	scm.category_id, scm.sales_cat_vat_account_prefix, s.sales_store_vat_account_prefix, spl_account_name,
 	r.bank_batch_no, r.card_auth_code, r.card_holder_name, r.currency_id, r.exchange_rate,
	r.exchange_date, r.currency_amt, r.card_exp_date, r.card_number, fc.currency, hcm.center_code, r.bank_name,
	pm.ref_required, pm.bank_required, hcm.center_id;


/*
 * TODO: drop this view and add the full query in InPatientReportBuilder.srxml
 * See bug 16072
 */
DROP VIEW IF EXISTS in_patient_chart_view CASCADE;
CREATE OR REPLACE VIEW in_patient_chart_view AS
 SELECT pd.mr_no, sm.salutation, sm.salutation_id, pd.patient_name, pd.last_name, pd.patient_gender,
 pd.patient_phone, pd.oldmrno, pd.casefile_no, pd.patient_address, ci.city_name AS cityname, pd.patient_city, pd.country,
 st.state_name AS statename, pd.patient_state, pd.patient_area, pd.dateofbirth,
 pd.custom_list4_value, pd.custom_list5_value, pd.patient_care_oftext, pd.custom_list6_value, pd.remarks,
 pd.custom_field5, pd.custom_field4, COALESCE(pd.dateofbirth,
 pd.expected_dob) AS expected_dob, get_patient_age(pd.dateofbirth, pd.expected_dob) AS age,
 get_patient_age_in(pd.dateofbirth, pd.expected_dob) AS agein, pd.patient_careof_address, pd.relation,pd.family_id,
 pd.custom_field1, pd.custom_field2, pd.custom_field3, pd.patient_category_id, pd.category_expiry_date,
 pd.previous_visit_id, pd.visit_id, pra.visit_type, pra.op_type, otn.op_type_name,pra.patient_id, pra.revisit, pra.reg_date, pra.reg_time,
  pra.primary_sponsor_id, pra.insurance_id, pra.patient_care_oftext AS patcontactperson, pra.relation AS patrelation,
  pra.patient_careof_address AS pataddress, pra.complaint, pra.doctor, dr.doctor_name, dr.specialization, dr.doctor_type, dr.doctor_address,
  dr.doctor_mobile, dr.doctor_mail_id, dr.qualification, dr.registration_no, dr.res_phone, dr.clinic_phone, dr.doctor_license_number,
  pra.dept_name AS dept_id,
  dep.dept_name, pra.org_id, od.org_name, pra.bed_type AS bill_bed_type, wnr.ward_name AS reg_ward_name,
  pra.ward_id AS reg_ward_id, bn.bed_name AS alloc_bed_name, bn.bed_type AS alloc_bed_type,
  wn.ward_name AS alloc_ward_name, ptpa.tpa_name AS primary_tpa_name, stpa.tpa_name AS secondary_tpa_name, ipm.plan_name,
  in_cat.category_name AS plan_type, picm.insurance_co_name AS primary_insurance_co_name,
  sicm.insurance_co_name AS secondary_insurance_co_name,
  pra.discharge_doc_id AS dis_doc_id, pra.discharge_format AS dis_format,
  pra.discharge_flag, pra.discharge_doctor_id, pra.discharge_date, pra.discharge_time, pra.mlc_status,
  pra.discharge_finalized_date AS dis_finalized_date, pra.discharge_finalized_time AS dis_finalized_time,
  pra.discharge_finalized_user AS dis_finalized_user, b.bill_no, b.status, b.approval_amount, pra.reference_docto_id,
  dtm.discharge_type, COALESCE(drs.doctor_name, rd.referal_name) AS refdoctorname, pra.status AS visit_status,
  a.admit_date AS bed_start_date,
  a.finalized_time AS bed_end_date,pra.unit_id,dum.unit_name,pcm.category_name,pd.name_local_language,

  pd.custom_field6, pd.custom_field7, pd.custom_field8, pd.custom_field9, custom_field10, pra.center_id
   FROM patient_registration pra
   JOIN patient_details pd ON pra.mr_no::text = pd.mr_no::text
   LEFT JOIN op_type_names otn ON otn.op_type = pra.op_type
   LEFT JOIN ward_names wnr ON wnr.ward_no::text = pra.ward_id::text
   LEFT JOIN salutation_master sm ON pd.salutation::text = sm.salutation_id::text
   LEFT JOIN city ci ON pd.patient_city::text = ci.city_id::text
   LEFT JOIN state_master st ON pd.patient_state::text = st.state_id::text
   LEFT JOIN department dep ON pra.dept_name::text = dep.dept_id::text
   LEFT JOIN dept_unit_master dum ON dum.unit_id::text = pra.unit_id::text
   LEFT JOIN doctors dr ON dr.doctor_id::text = pra.doctor::text
   LEFT JOIN admission a ON a.patient_id::text = pra.patient_id::text
   LEFT JOIN bed_names bn ON bn.bed_id = a.bed_id
   LEFT JOIN discharge_type_master dtm ON(dtm.discharge_type_id = pra.discharge_type_id)
   LEFT JOIN ward_names wn ON wn.ward_no::text = bn.ward_no::text
   LEFT JOIN organization_details od ON pra.org_id::text = od.org_id::text
   LEFT JOIN doctors drs ON pra.reference_docto_id::text = drs.doctor_id::text
   LEFT JOIN referral rd ON pra.reference_docto_id::text = rd.referal_no::text
   LEFT JOIN bill b ON b.visit_id::text = pra.patient_id::text AND b.bill_type = 'C'::bpchar AND b.visit_type='i'
   LEFT JOIN patient_category_master pcm ON pcm.category_id = pd.patient_category_id
   LEFT JOIN tpa_master ptpa ON ptpa.tpa_id::text = pra.primary_sponsor_id::text
   LEFT JOIN tpa_master stpa ON stpa.tpa_id::text = pra.secondary_sponsor_id::text
   LEFT JOIN insurance_plan_main ipm ON (ipm.plan_id = pra.plan_id)
   LEFT JOIN insurance_category_master in_cat ON (in_cat.category_id = ipm.category_id)
   LEFT JOIN insurance_company_master picm ON (picm.insurance_co_id = pra.primary_insurance_co)
   LEFT JOIN insurance_company_master sicm ON (sicm.insurance_co_id = pra.secondary_insurance_co)
   AND b.status <> 'C'::bpchar AND b.status <> 'X'::bpchar AND b.restriction_type='N' AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) );

DROP VIEW IF EXISTS miscellaneous_payments_view;
CREATE OR REPLACE VIEW miscellaneous_payments_view AS
	SELECT p.voucher_no, date, pd.amount, account_head, account_head_name, p.voucher_category ,p.payee_name
		FROM  payments p
		JOIN payments_details pd ON (p.voucher_no=pd.voucher_no) and p.payment_type='C'
		LEFT JOIN bill_account_heads bac ON bac.account_head_id = pd.account_head
;

-- Store Purchase Details Screen view

DROP VIEW IF EXISTS store_purchase_details_view CASCADE ;
CREATE OR REPLACE VIEW  store_purchase_details_view AS
 SELECT supplier_master.supplier_name,supplier_master.cust_supplier_code, supplier_master.supplier_address, supplier_master.
 contact_person_name, supplier_master.contact_person_mobile_number, png.exp_dt AS exp,
 png.batch_no, pngm.grn_date, png.billed_qty AS qty, png.cost_price AS rate, png.discount AS disc,
 png.tax AS vat, pmd.medicine_name, c.identification,png.item_batch_id,
        CASE
            WHEN pngm.debit_note_no IS NULL THEN pngm.grn_no
            ELSE pngm.debit_note_no
        END AS txn_no, COALESCE(pi.supplier_id, pdn.supplier_id) AS supplier_id,
        CASE
            WHEN pngm.debit_note_no IS NULL THEN 'P'::text
            ELSE 'D'::text
        END AS type, png.medicine_id, pngm.store_id AS store, png.bonus_qty, png.mrp, gn.generic_name,invoice_no, pmd.issue_base_unit, png.grn_pkg_size
   FROM store_grn_main pngm
   JOIN store_grn_details png USING (grn_no)
   JOIN store_item_details pmd USING (medicine_id)
   JOIN store_category_master c ON pmd.med_category_id = c.category_id
   LEFT JOIN store_invoice pi USING (supplier_invoice_id)
   LEFT JOIN store_debit_note pdn ON pdn.debit_note_no::text = pngm.debit_note_no::text
   JOIN supplier_master ON supplier_master.supplier_code::text = pi.supplier_id::text OR supplier_master.supplier_code::text = pdn.supplier_id::text
   LEFT OUTER JOIN generic_name gn ON pmd.generic_name::text = gn.generic_code::text
  ORDER BY store,medicine_name,batch_no;

--- GG custom report
DROP VIEW IF EXISTS test_payments_with_hosp_share_view CASCADE;
CREATE OR REPLACE VIEW test_payments_with_hosp_share_view AS
SELECT b.bill_no,date(b.finalized_date) AS date , bc.amount AS bill_amount, d.test_name AS test_name ,bc.activity_conducted,doc.doctor_name, doctor_amount, pdoc.doctor_name AS prescribing_doctor_name, prescribing_dr_amount,
coalesce(rdoc.doctor_name, ref.referal_name) AS referal_doctor, bc.referal_amount,
tpa.tpa_name,
(coalesce(bc.amount,0)-coalesce(bc.doctor_amount,0)-coalesce(bc.prescribing_dr_amount,0)-coalesce(bc.referal_amount,0)) AS hospital_amt
FROM bill_charge bc
JOIN bill b using(bill_no)
LEFT JOIN doctors doc ON  doc.doctor_id =bc.payee_doctor_id
LEFT JOIN doctors pdoc ON pdoc.doctor_id =bc.prescribing_dr_id
LEFT JOIN patient_registration pr  ON pr.patient_id=b.visit_id
LEFT JOIN doctors rdoc ON rdoc.doctor_id = pr.reference_docto_id
LEFT JOIN referral ref ON (ref.referal_no = pr.reference_docto_id )
LEFT JOIN tpa_master tpa ON (tpa.tpa_id = pr.primary_sponsor_id)
JOIN diagnostics d ON (bc.act_description_id =d.test_id)
WHERE  b.status in ('F','C','S') AND bc.status!='X' AND bc.charge_group ='DIA' AND activity_conducted ='Y' AND
(doctor_amount >0 OR prescribing_dr_amount > 0 OR referal_amount >0 )
;


-- GG Payments Consolidated
DROP VIEW IF EXISTS payments_consolidated_view CASCADE;
CREATE OR REPLACE VIEW payments_consolidated_view AS
SELECT b.bill_no,date(b.finalized_date) AS date , bc.charge_id, cgc.chargegroup_name,  d.test_name AS test_name ,s.service_name AS service_name,
bc.activity_conducted, bc.amount AS bill_amount,
doc.doctor_name, bc.doctor_amount, pdoc.doctor_name AS prescribing_doctor_name, prescribing_dr_amount,
coalesce(rdoc.doctor_name, ref.referal_name) AS referal_doctor, bc.referal_amount,
tpa.tpa_name,
(coalesce(bc.doctor_amount,0)+coalesce(bc.prescribing_dr_amount,0)+coalesce(bc.referal_amount,0)) AS total_to_pay_amt,
(coalesce(bc.amount,0)-coalesce(bc.doctor_amount,0)-coalesce(bc.prescribing_dr_amount,0)-coalesce(bc.referal_amount,0)) AS hospital_amt,
bc.doc_payment_id, bc.prescribing_dr_payment_id, bc.ref_payment_id,
pd1.amount AS doc_paid_amt, pd2.amount AS pres_dr_paid_amt, pd3.amount AS ref_paid_amt,
pd1.voucher_no AS doc_voucher, pd2.voucher_no AS pres_voucher, pd3.voucher_no AS ref_voucher,
(coalesce(pd1.amount,0)+coalesce(pd2.amount,0)+coalesce(pd3.amount,0)) AS total_paid_amt,
(
   (coalesce(bc.doctor_amount,0)+coalesce(bc.prescribing_dr_amount,0)+coalesce(bc.referal_amount,0)) -
   (coalesce(pd1.amount,0)+coalesce(pd2.amount,0)+coalesce(pd3.amount,0))
) AS yet_to_pay_amt
FROM bill_charge bc
JOIN bill b USING(bill_no)
LEFT JOIN doctors doc ON  doc.doctor_id =bc.payee_doctor_id
LEFT JOIN doctors pdoc ON pdoc.doctor_id =bc.prescribing_dr_id
LEFT JOIN patient_registration pr  ON pr.patient_id=b.visit_id
LEFT JOIN doctors rdoc ON rdoc.doctor_id = pr.reference_docto_id
LEFT JOIN referral ref ON (ref.referal_no = pr.reference_docto_id )
LEFT JOIN tpa_master tpa ON (tpa.tpa_id = pr.primary_sponsor_id)
JOIN chargegroup_constants cgc ON cgc.chargegroup_id=bc.charge_group
LEFT JOIN diagnostics d ON (bc.act_description_id =d.test_id)
LEFT JOIN services s ON (bc.act_description_id =s.service_id)
LEFT JOIN payments_details pd1 ON (pd1.charge_id =bc.charge_id AND pd1.payment_id=bc.doc_payment_id AND pd1.voucher_no IS NOT NULL )
LEFT JOIN payments_details pd2 ON (pd2.charge_id =bc.charge_id AND pd2.payment_id=bc.prescribing_dr_payment_id AND pd2.voucher_no IS NOT NULL )
LEFT JOIN payments_details pd3 ON (pd3.charge_id =bc.charge_id AND pd3.payment_id=bc.ref_payment_id AND pd3.voucher_no IS NOT NULL )
WHERE  b.status IN ('F','C','S') AND bc.status!='X'
UNION ALL
SELECT b.bill_no,date(b.finalized_date) AS date,  bc.charge_id, cgc.chargegroup_name,  d.test_name AS test_name ,s.service_name AS service_name,
bac.activity_conducted, bc.amount AS bill_amount,
doc.doctor_name, bac.doctor_amount, NULL AS prescribing_doctor_name, 0 AS prescribing_dr_amount,NULL AS referal_doctor, 0 AS referal_amount,
tpa.tpa_name,
coalesce(bac.doctor_amount,0) AS total_to_pay_amt,
(coalesce(bc.amount,0)-coalesce(bac.doctor_amount,0)) AS hospital_amt,
bac.doctor_payment_id, NULL, NULL,
pd.amount AS doc_paid_amt, 0,0,
pd.voucher_no AS doc_voucher, NULL, NULL,
coalesce(pd.amount,0) AS total_paid_amt,
(coalesce(bac.doctor_amount,0) - coalesce(pd.amount,0)) AS yet_to_pay_amt
FROM bill_activity_charge bac
	JOIN bill_charge bc ON (bc.charge_id=bac.charge_id )
        JOIN bill b USING(bill_no)
        LEFT JOIN doctors doc ON  doc.doctor_id =bac.doctor_id
	LEFT JOIN patient_registration pr  ON pr.patient_id=b.visit_id
	LEFT JOIN tpa_master tpa ON (tpa.tpa_id = pr.primary_sponsor_id)
	JOIN chargegroup_constants cgc ON cgc.chargegroup_id=bac.charge_group
	LEFT JOIN diagnostics d ON (bac.act_description_id =d.test_id)
	LEFT JOIN services s ON (bac.act_description_id =s.service_id)
	LEFT JOIN payments_details pd ON (pd.charge_id =bac.charge_id AND pd.payment_id=bac.doctor_payment_id AND pd.voucher_no IS NOT NULL )
              WHERE  b.status IN ('F','C','S')
                AND bc.status!='X'
;


DROP FUNCTION IF EXISTS update_master_timestamp_trigger() CASCADE;
CREATE OR REPLACE FUNCTION update_master_timestamp_trigger() RETURNS trigger AS $BODY$
BEGIN
		update master_timestamp set master_count = master_count + 1;
RETURN NULL;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS doctor_update_timestamp ON doctors;
CREATE TRIGGER doctor_update_timestamp AFTER INSERT OR UPDATE ON doctors
  FOR EACH STATEMENT EXECUTE PROCEDURE update_master_timestamp_trigger();

DROP TRIGGER IF EXISTS diagnostics_update_timestamp ON diagnostics;
CREATE TRIGGER diagnostics_update_timestamp AFTER INSERT OR UPDATE ON diagnostics
  FOR EACH STATEMENT EXECUTE PROCEDURE update_master_timestamp_trigger();

DROP TRIGGER IF EXISTS operation_update_timestamp ON operation_master;
CREATE TRIGGER operation_update_timestamp AFTER INSERT OR UPDATE ON operation_master
  FOR EACH STATEMENT EXECUTE PROCEDURE update_master_timestamp_trigger();

DROP TRIGGER IF EXISTS equipment_update_timestamp ON equipment_master;
CREATE TRIGGER equipment_update_timestamp AFTER INSERT OR UPDATE ON equipment_master
  FOR EACH STATEMENT EXECUTE PROCEDURE update_master_timestamp_trigger();

DROP TRIGGER IF EXISTS services_update_timestamp ON services;
CREATE TRIGGER services_update_timestamp AFTER INSERT OR UPDATE ON services
  FOR EACH STATEMENT EXECUTE PROCEDURE update_master_timestamp_trigger();

DROP TRIGGER IF EXISTS test_org_update_timestamp ON test_org_details;
CREATE TRIGGER test_org_update_timestamp AFTER INSERT OR UPDATE ON test_org_details
  FOR EACH STATEMENT EXECUTE PROCEDURE update_master_timestamp_trigger();

DROP TRIGGER IF EXISTS service_org_update_timestamp ON service_org_details;
CREATE TRIGGER service_org_update_timestamp AFTER INSERT OR UPDATE ON service_org_details
  FOR EACH STATEMENT EXECUTE PROCEDURE update_master_timestamp_trigger();

DROP TRIGGER IF EXISTS operation_org_update_timestamp ON operation_org_details;
CREATE TRIGGER operation_org_update_timestamp AFTER INSERT OR UPDATE ON operation_org_details
  FOR EACH STATEMENT EXECUTE PROCEDURE update_master_timestamp_trigger();

DROP TRIGGER IF EXISTS diet_master_timestamp ON diet_master;
CREATE TRIGGER diet_master_timestamp AFTER INSERT OR UPDATE ON diet_master
  FOR EACH STATEMENT EXECUTE PROCEDURE update_master_timestamp_trigger();

DROP TRIGGER IF EXISTS pack_master_timestamp ON pack_master;

-- Trigger : packages_master_timestamp on packages
-- Packages table to hold both order sets and Package master details

DROP TRIGGER IF EXISTS packages_master_timestamp ON packages;
CREATE TRIGGER packages_master_timestamp AFTER INSERT OR UPDATE ON packages
  FOR EACH STATEMENT EXECUTE PROCEDURE update_master_timestamp_trigger();

DROP TRIGGER IF EXISTS pack_org_details_timestamp ON pack_org_details;
CREATE TRIGGER pack_org_details_timestamp AFTER INSERT OR UPDATE ON pack_org_details
  FOR EACH STATEMENT EXECUTE PROCEDURE update_master_timestamp_trigger();

DROP TRIGGER IF EXISTS bed_names_timestamp ON bed_names;
CREATE TRIGGER bed_names_timestamp AFTER INSERT OR UPDATE ON bed_names
  FOR EACH STATEMENT EXECUTE PROCEDURE update_master_timestamp_trigger();

-- Trigger: dyna_package_update_timestamp ON dyna_packages

DROP TRIGGER IF EXISTS dyna_package_update_timestamp ON dyna_packages;
CREATE TRIGGER dyna_package_update_timestamp AFTER INSERT OR UPDATE ON dyna_packages
  FOR EACH STATEMENT EXECUTE PROCEDURE update_master_timestamp_trigger();

-- Trigger: dyna_package_org_update_timestamp ON dyna_package_org_details

DROP TRIGGER IF EXISTS dyna_package_org_update_timestamp ON dyna_package_org_details;
CREATE TRIGGER dyna_package_org_update_timestamp AFTER INSERT OR UPDATE ON dyna_package_org_details
  FOR EACH STATEMENT EXECUTE PROCEDURE update_master_timestamp_trigger();

-- Trigger: per_diem_codes_update_timestamp ON per_diem_codes_master

DROP TRIGGER IF EXISTS per_diem_codes_update_timestamp ON per_diem_codes_master;
CREATE TRIGGER per_diem_codes_update_timestamp AFTER INSERT OR UPDATE ON per_diem_codes_master
  FOR EACH STATEMENT EXECUTE PROCEDURE update_master_timestamp_trigger();

DROP VIEW IF EXISTS operations_prescribed_view;
CREATE OR REPLACE VIEW operations_prescribed_view AS
 SELECT op.patient_id, max(op.PRESCRIBED_ID) AS pres_id
   FROM bed_operation_schedule op
   JOIN operation_reports opr ON opr.signed_off IS FALSE
  GROUP BY op.patient_id
UNION
 SELECT op.patient_id, max(op.PRESCRIBED_ID) AS pres_id
   FROM bed_operation_schedule op
  WHERE op.status::text <> 'X'::text AND NOT (op.patient_id IN ( SELECT operation_reports.visit_id
           FROM operation_reports
          WHERE operation_reports.signed_off IS TRUE))
  GROUP BY op.patient_id
UNION
 SELECT op.patient_id, max(op.PRESCRIBED_ID) AS pres_id
   FROM bed_operation_schedule op
   JOIN operation_master opm ON (opm.op_id = op.operation_name)
  WHERE  op.status::text <> 'X'::text
  GROUP BY op.patient_id
  ORDER BY 2 DESC;


-- NOT using get_patient_name, AS it is slow, and makes the
-- patient search slow. Also, for patient search, we don't need to search within the salutation
DROP VIEW IF EXISTS all_mrnos_view;
CREATE OR REPLACE VIEW all_mrnos_view AS
SELECT pd.*,coalesce(pd.mr_no,pr.mr_no),pd.first_visit_reg_date AS gen_reg_date, sal.salutation AS salutation_name, pr.reg_date AS visit_reg_date,
	pd.patient_name || ' ' || coalesce(pd.middle_name, '') || CASE WHEN coalesce(pd.middle_name, '')!='' THEN ' ' ELSE '' end
	|| coalesce(pd.last_name, '') AS patient_full_name,
	get_patient_age(dateofbirth, expected_dob) AS age,
	get_patient_age_in(dateofbirth, expected_dob) AS age_in,
	(CASE WHEN pd.visit_id is null and previous_visit_id is null THEN 'N'
	when pd.visit_id is not null THEN 'A' WHEN pd.visit_id is null THEN 'I' end) AS status,
	CASE WHEN  pr.primary_sponsor_id= '' THEN null ELSE pr.primary_sponsor_id END AS primary_sponsor_id,
	CASE WHEN pr.secondary_sponsor_id::text = ''::text THEN NULL::character varying ELSE pr.secondary_sponsor_id END AS secondary_sponsor_id,
	ppdp.member_id AS member_id,
	CASE WHEN pr.org_id= '' THEN null ELSE pr.org_id END AS org_id,
	pr.discharge_flag, pr.discharge_date, ci.city_name, pr.mlc_status,
	pr.reg_date AS last_visited_date,
	pr.center_id AS last_visited_center,
	pr.center_id AS active_visit_center, pr.collection_center_id, cgm.abbreviation, cgm.confidentiality_grp_id
FROM patient_details pd
	--LEFT JOIN patient_registration pr ON (pd.visit_id = pr.patient_id)
    LEFT JOIN patient_registration pr ON pd.mr_no::text = pr.mr_no::text
	AND pr.patient_id =
    CASE WHEN pd.visit_id != '' THEN
	pd.visit_id --For Main Visit
    ELSE
	pd.previous_visit_id --For Follow up Visit
    END
	LEFT JOIN patient_registration prvs_pr ON (pd.previous_visit_id=prvs_pr.patient_id)
	LEFT JOIN salutation_master sal ON (sal.salutation_id = pd.salutation)
	JOIN confidentiality_grp_master cgm ON (cgm.confidentiality_grp_id = pd.patient_group)
	LEFT JOIN city ci ON pd.patient_city= ci.city_id
    LEFT JOIN patient_insurance_plans pipp ON pipp.patient_id::text = pr.patient_id::text 
    LEFT JOIN patient_policy_details ppdp ON ppdp.patient_policy_id = pipp.patient_policy_id 
WHERE (pd.mr_no in (SELECT mr_no from user_mrno_association
 where emp_username = current_setting('application.username') OR current_setting('application.username') = '_system') or pd.patient_group in 
 (SELECT ufa.confidentiality_grp_id as patient_group from user_confidentiality_association ufa
 JOIN confidentiality_grp_master cgm
 ON (cgm.confidentiality_grp_id = ufa.confidentiality_grp_id)
where emp_username = current_setting('application.username')
 AND ufa.status = 'A' and cgm.status = 'A' UNION SELECT 0));


-- this view used for searching visits using visit autocomplete.
-- removed unnecessary joins because it is slowing down the query.
DROP VIEW IF EXISTS visit_search_view;
CREATE OR REPLACE VIEW visit_search_view AS
SELECT pd.mr_no, pd.patient_name, pd.middle_name, pd.last_name, pd.patient_gender,
	pd.patient_name || ' ' || coalesce(pd.middle_name, '') || ' ' || coalesce(pd.last_name, '')
		AS patient_full_name, (pr.reg_date + pr.reg_time) AS visit_reg_date,
	sal.salutation AS salutation_name, pd.dateofbirth, pd.expected_dob,
	CASE
		WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 31
			THEN (floor(current_date - COALESCE(pd.dateofbirth, pd.expected_dob)))::integer
		WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 730
			THEN (round((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/30.43))::integer
		ELSE (round((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/365.25))::integer
	END AS age,
	CASE
		WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 31 THEN 'D'
 		WHEN (current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 730) THEN 'M'
		ELSE 'Y'
	END AS age_in, pd.patient_phone,pd.patient_phone_country_code, pd.visit_id, pd.previous_visit_id, pd.government_identifier,
	dr.doctor_name, dep.dept_name,
	pr.status, pr.op_type, pr.patient_id, pr.center_id, pr.visit_type, pd.oldmrno, pr.collection_center_id
FROM patient_details pd
	JOIN patient_registration pr ON (pd.mr_no = pr.mr_no)
	LEFT JOIN op_type_names otn ON (otn.op_type = pr.op_type)
	LEFT JOIN department dep ON (pr.dept_name = dep.dept_id)
	LEFT JOIN doctors dr ON (dr.doctor_id = pr.doctor)
	LEFT JOIN salutation_master sal ON (sal.salutation_id = pd.salutation)
WHERE (pd.mr_no in (SELECT mr_no from user_mrno_association
 where emp_username = current_setting('application.username') OR current_setting('application.username') = '_system') or pd.patient_group in 
 (SELECT ufa.confidentiality_grp_id as patient_group from user_confidentiality_association ufa
 JOIN confidentiality_grp_master cgm
 ON (cgm.confidentiality_grp_id = ufa.confidentiality_grp_id)
where emp_username = current_setting('application.username')
 AND ufa.status = 'A' and cgm.status = 'A' UNION SELECT 0));



-- NOT using get_patient_name, AS it is slow, and makes the
-- patient search slow. Also, for patient search, we don't need to search within the salutation

DROP VIEW IF EXISTS all_visits_view;
CREATE OR REPLACE VIEW all_visits_view AS
SELECT pd.mr_no, pd.original_mr_no, pd.patient_name, pd.patient_gender, pd.patient_care_oftext, pd.patient_address,
	pd.patient_city, pd.patient_state, pd.patient_phone, pd.salutation, pd.last_name, pd.dateofbirth,
	pd.country, pd.cflag, pd.patient_careof_address, pd.relation,
	pd.user_name, pd.oldmrno, pd.emr_access, pd.remarks,
	pd.custom_field1, pd.custom_field2, pd.custom_field3, pd.custom_field4, pd.custom_field5,
	pd.custom_field6, pd.custom_field7, pd.custom_field8, pd.custom_field9, pd.custom_field10,
	pd.custom_field11, pd.custom_field12, pd.custom_field13,custom_field14,pd.custom_field15,pd.custom_field16,pd.custom_field17,
	pd.custom_field18,pd.custom_field19,

	pd.expected_dob, pd.timeofbirth, pd.portal_access, pd.email_id, pd.patient_area,
	pd.patient_photo, pd.category_expiry_date, pd.visit_id, pd.previous_visit_id, pd.casefile_no,
	pd.death_date, pd.death_time, drm.reason AS death_reason, pd.patient_phone2,
	pd.next_of_kin_relation, pd.middle_name,
	pd.custom_list1_value, pd.custom_list2_value, pd.custom_list3_value,custom_list4_value,
	custom_list5_value,custom_list6_value,custom_list7_value,custom_list8_value,custom_list9_value,
	pd.patient_consultation_info, pd.vip_status, pd.first_mlc_visitid,
	pd.no_allergies, pd.med_allergies, pd.food_allergies, pd.other_allergies,
	pd.first_visit_reg_date, pr.reg_date,
	pd.government_identifier, pd.identifier_id, pd.dead_on_arrival,
	pd.passport_no, pd.passport_validity, passport_issue_country, pd.visa_validity, pd.family_id,
	pr.patient_id, (pr.reg_date + pr.reg_time) AS visit_reg_date, pr.status, pr.op_type,  otn.op_type_name,
	sal.salutation AS salutation_name, pr.visit_type,
	pd.patient_name || ' ' || coalesce(pd.middle_name, '') || CASE WHEN coalesce(pd.middle_name, '')!='' THEN ' ' ELSE '' end
	|| coalesce(pd.last_name, '')
		AS patient_full_name,
	get_patient_age(dateofbirth, expected_dob) AS age,
	get_patient_age_in(dateofbirth, expected_dob) AS age_in,
	dr.doctor_name, dep.dept_name,
	dep.dept_id, pr.plan_id, pr.insurance_id, ic.policy_no, ipmp.plan_name AS primary_plan_name,ipms.plan_name AS secondry_plan_name, icm.category_name,
	pr.mlc_type, pr.mlc_no, pr.accident_place, pr.police_stn,
	pr.doctor, pr.complaint, pr.bed_type, pr.ward_id,
	(case WHEN  coalesce(pr.discharge_finalized_user,'')!='' THEN 'F'
		when coalesce(pr.discharge_doc_id, 0) != '0' THEN 'O'
		else 'ND' end) AS discharge_doc_status,
	pr.discharge_date, dtm.discharge_type, pr.discharge_time, pr.discharge_doc_id, pr.discharge_format,
	pr.discharge_finalized_date, pr.discharge_finalized_time, pr.discharge_finalized_user, pr.discharge_flag,
	CASE WHEN pr.org_id= '' THEN null ELSE pr.org_id END AS org_id,
	CASE WHEN  pr.primary_sponsor_id= '' THEN null ELSE pr.primary_sponsor_id END AS primary_sponsor_id,
	CASE WHEN  pr.secondary_sponsor_id= '' THEN null ELSE pr.secondary_sponsor_id END AS secondary_sponsor_id,
	 pr.mlc_status,pdc.doc_id, pdc.doc_format, pdc.template_id,dv.doc_type, dv.specialized, pr.patient_category_id,
	 pr.center_id, ppdp.member_id AS primary_member_id,ppds.member_id AS secondry_member_id,pd.mobile_password,mobile_access,
	 (CASE WHEN mac.activation_status='Y' THEN ppdp.policy_holder_name ELSE ic.policy_holder_name END) AS primary_policy_holder_name,
	 (CASE WHEN mac.activation_status='Y' THEN ppds.policy_holder_name ELSE ic.policy_holder_name END) AS secondry_policy_holder_name
FROM patient_details pd
	JOIN patient_registration pr ON (pd.mr_no = pr.mr_no)
	LEFT JOIN op_type_names otn ON (otn.op_type = pr.op_type)
	LEFT JOIN department dep ON (pr.dept_name = dep.dept_id)
	LEFT JOIN doctors dr ON (dr.doctor_id = pr.doctor)
	LEFT JOIN discharge_type_master dtm ON(dtm.discharge_type_id = pr.discharge_type_id)
	LEFT JOIN salutation_master sal ON (sal.salutation_id = pd.salutation)
	LEFT JOIN patient_documents pdc ON (pr.doc_id = pdc.doc_id and pr.mlc_status = 'Y')
	LEFT JOIN doc_all_templates_view dv ON (pdc.template_id = dv.template_id and pdc.doc_format = dv.doc_format)
	LEFT JOIN insurance_case ic ON (pr.insurance_id=ic.insurance_id)
	LEFT JOIN death_reason_master drm ON (drm.reason_id=pd.death_reason_id)
	LEFT JOIN patient_insurance_plans pipp ON (pipp.patient_id=pr.patient_id AND pipp.priority = 1)
	LEFT JOIN patient_insurance_plans pips ON (pips.patient_id=pr.patient_id AND pips.priority = 2)
	LEFT JOIN patient_policy_details ppdp ON (ppdp.patient_policy_id=pipp.patient_policy_id)
	LEFT JOIN patient_policy_details ppds ON (ppds.patient_policy_id=pips.patient_policy_id)
	LEFT JOIN insurance_plan_main ipmp ON (ipmp.plan_id=pipp.plan_id)
	LEFT JOIN insurance_plan_main ipms ON (ipms.plan_id=pips.plan_id)
	LEFT JOIN insurance_category_master icm ON (icm.category_id=ipmp.category_id AND icm.insurance_co_id=ipmp.insurance_co_id)
	LEFT JOIN modules_activated mac ON (mac.module_id = 'mod_adv_ins')
WHERE (pd.mr_no in (SELECT mr_no from user_mrno_association
 where emp_username = current_setting('application.username') OR current_setting('application.username') = '_system') or pd.patient_group in 
 (SELECT ufa.confidentiality_grp_id as patient_group from user_confidentiality_association ufa
 JOIN confidentiality_grp_master cgm
 ON (cgm.confidentiality_grp_id = ufa.confidentiality_grp_id)
where emp_username = current_setting('application.username')
 AND ufa.status = 'A' and cgm.status = 'A' UNION SELECT 0));


--
-- View for looking at conducting doctor payments: we go from bill_charge and join with bill_activity_charge
-- (which means multiple rows for conducting doctors), and filter out ON the following:
--   bill status must not be open or cancelled
--   bill_charge status must not be cancelled
--
-- Note: if activity is not conducted, the charge will not be posted. So, no need to check for
-- conduction of activity.
--
-- Note: this should NOT be used for prescribing/referral doctor amounts, since there can be multiple
-- rows due to join with bill_activity_charge.
--
DROP VIEW IF EXISTS conducting_doctor_payments_view CASCADE;
CREATE OR REPLACE VIEW conducting_doctor_payments_view AS
SELECT coalesce(pr.center_id, isr.center_id)as center_id, bc.charge_id, bc.bill_no, bc.charge_group, bc.charge_head, bc.act_department_id,
	bc.act_description, bc.act_remarks, bc.act_rate, bc.act_unit, bc.act_quantity, bc.amount, bc.discount,
	bc.discount_reason, bc.charge_ref, bc.paid_amount, bc.posted_date AS bc_posted_date, bc.status,
	bc.approval_id, bc.orig_rate, bc.package_unit, bc.hasactivity, bc.act_description_id,
	bc.insurance_claim_amount,
	bc.overall_discount_auth, bc.overall_discount_amt, bc.discount_auth_dr, bc.dr_discount_amt,
	bc.discount_auth_pres_dr, bc.pres_dr_discount_amt, bc.discount_auth_ref, bc.ref_discount_amt,
	bc.discount_auth_hosp, bc.hosp_discount_amt,
	bc.payee_doctor_id AS payee_doctor_id,
	bc.doctor_amount  AS doctor_amount,
	bc.doc_payment_id AS doctor_payment_id,
	'Y' AS activity_conducted,
	NULL AS bac_activity_id, NULL AS bac_activity_code,
	bc.prescribing_dr_id, bc.prescribing_dr_amount, bc.prescribing_dr_payment_id,
	bc.referal_amount, bc.ref_payment_id,
	bc.out_house_amount, bc.oh_payment_id,
	'N' AS package_charge,
	b.visit_id, b.visit_type, b.bill_type, b.open_date, b.status AS bill_status, b.closed_date,
	b.discharge_status, b.app_modified, b.username AS bill_username,
	b.remarks, coalesce(b.closed_by,b.username) AS closed_by, b.opened_by, b.mod_time,
	b.claim_recd_amount, coalesce(b.finalized_date, isr.date) AS finalized_date ,b. total_amount, b.primary_claim_status,
	coalesce(pr.mr_no,isr.incoming_visit_id) AS mr_no, pr.primary_sponsor_id,
	coalesce(pdet.patient_name,isr.patient_name) AS patient_name,  pdet.last_name,
	b.claim_recd_amount AS actual_claim_amt, chc.chargehead_name, chc.chargehead_id, chc.chargegroup_id, tpa.tpa_name,
	(CASE WHEN b.bill_type = 'C' THEN 'Bill Later' ELSE 'Bill Now' END) AS billtype,
	(CASE WHEN b.status = 'A' THEN 'Open' WHEN b.status = 'F' THEN 'Finalized'
		WHEN b.status = 'S' THEN 'Settled' WHEN b.status = 'C' THEN 'Closed' ELSE NULL END) AS billstatus,
	vtn.visit_type_long_name AS visittype,
	dac.disc_auth_name AS discount_auth_dr_name, dap.disc_auth_name AS discount_auth_pres_dr_name,
	daov.disc_auth_name AS overall_discount_auth_name, dar.disc_auth_name AS discount_auth_ref_name,
	(CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END) AS insurancestatus
FROM bill_charge bc
	JOIN bill b ON (bc.bill_no = b.bill_no)
	JOIN visit_type_names vtn ON (vtn.visit_type = b.visit_type)
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN patient_details pdet ON (pdet.mr_no=pr.mr_no)
	JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head)
	LEFT JOIN discount_authorizer dac ON bc.discount_auth_dr = dac.disc_auth_id
	LEFT JOIN discount_authorizer dap ON bc.discount_auth_pres_dr = dap.disc_auth_id
	LEFT JOIN discount_authorizer dar ON bc.discount_auth_ref = dar.disc_auth_id
	LEFT JOIN discount_authorizer dah ON bc.discount_auth_hosp = dah.disc_auth_id
	LEFT JOIN discount_authorizer daov ON bc.overall_discount_auth = daov.disc_auth_id
	LEFT JOIN insurance_case ic ON ic.insurance_id = pr.insurance_id
	LEFT JOIN tpa_master tpa on tpa.tpa_id=ic.tpa_id
	LEFT JOIN incoming_sample_registration isr on isr.billno=b.bill_no
WHERE b.status IN ('F','S','C') AND bc.status <> 'X' 
AND (patient_confidentiality_check(COALESCE(pdet.patient_group, 0), pdet.mr_no))

UNION ALL

SELECT coalesce(pr.center_id, isr.center_id) AS center_id, bc.charge_id, bc.bill_no, bc.charge_group, bc.charge_head, bc.act_department_id,
	bc.act_description, bc.act_remarks, bc.act_rate, bc.act_unit, bc.act_quantity, bc.amount, bc.discount,
	bc.discount_reason, bc.charge_ref, bc.paid_amount, bc.posted_date AS bc_posted_date, bc.status,
	bc.approval_id, bc.orig_rate, bc.package_unit, bc.hasactivity, bac.act_description_id,
	bc.insurance_claim_amount,
	bc.overall_discount_auth, bc.overall_discount_amt, bc.discount_auth_dr, bc.dr_discount_amt,
	bc.discount_auth_pres_dr, bc.pres_dr_discount_amt, bc.discount_auth_ref, bc.ref_discount_amt,
	bc.discount_auth_hosp, bc.hosp_discount_amt,
	bac.doctor_id AS payee_doctor_id,
	coalesce(bac.doctor_amount,0) AS doctor_amount,
	bac.doctor_payment_id AS doctor_payment_id,
	COALESCE (bac.activity_conducted, 'Y') AS activity_conducted,
	bac.activity_id AS bac_activity_id, bac.activity_code AS bac_activit_code,
	bc.prescribing_dr_id, bc.prescribing_dr_amount, bc.prescribing_dr_payment_id,
	bc.referal_amount, bc.ref_payment_id,
	bc.out_house_amount, bc.oh_payment_id,
	'Y'  AS package_charge,
	b.visit_id, b.visit_type, b.bill_type, b.open_date, b.status AS bill_status, b.closed_date,
	b.discharge_status, b.app_modified, b.username AS bill_username,
	b.remarks, coalesce(b.closed_by,b.username) AS closed_by, b.opened_by, b.mod_time,
	b.claim_recd_amount, b.finalized_date,b. total_amount, b.primary_claim_status,
	coalesce(pr.mr_no,isr.incoming_visit_id) AS mr_no, pr.primary_sponsor_id, coalesce(pdet.patient_name,isr.patient_name)
	as patient_name, pdet.last_name,
	b.claim_recd_amount AS actual_claim_amt, chc.chargehead_name, chc.chargehead_id, chc.chargegroup_id, tpa.tpa_name,
	(CASE WHEN b.bill_type = 'C' THEN 'Bill Later' ELSE 'Bill Now' END) AS billtype,
	(CASE WHEN b.status = 'A' THEN 'Open' WHEN b.status = 'F' THEN 'Finalized'
		WHEN b.status = 'S' THEN 'Settled' WHEN b.status = 'C' THEN 'Closed' ELSE NULL END) AS billstatus,
	vtn.visit_type_long_name AS visittype,
	dac.disc_auth_name AS discount_auth_dr_name, dap.disc_auth_name AS discount_auth_pres_dr_name,
	daov.disc_auth_name AS overall_discount_auth_name, dar.disc_auth_name AS discount_auth_ref_name,
	(CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END) AS insurancestatus
FROM bill_charge bc
	JOIN bill b ON (bc.bill_no = b.bill_no)
	JOIN visit_type_names vtn ON (vtn.visit_type = b.visit_type)
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN bill_activity_charge bac ON (bac.charge_id = bc.charge_id)
	LEFT JOIN patient_details pdet ON (pdet.mr_no=pr.mr_no)
	JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head)
	LEFT JOIN discount_authorizer dac ON bc.discount_auth_dr = dac.disc_auth_id
	LEFT JOIN discount_authorizer dap ON bc.discount_auth_pres_dr = dap.disc_auth_id
	LEFT JOIN discount_authorizer dar ON bc.discount_auth_ref = dar.disc_auth_id
	LEFT JOIN discount_authorizer dah ON bc.discount_auth_hosp = dah.disc_auth_id
	LEFT JOIN discount_authorizer daov ON bc.overall_discount_auth = daov.disc_auth_id
	LEFT JOIN insurance_case ic ON ic.insurance_id = pr.insurance_id
	LEFT JOIN tpa_master tpa on tpa.tpa_id=ic.tpa_id
	LEFT JOIN  incoming_sample_registration isr on (isr.billno=b.bill_no)
WHERE b.status IN ('F','S','C') AND bc.status <> 'X' and bc.charge_group = 'PKG' 
AND (patient_confidentiality_check(COALESCE(pdet.patient_group, 0), pdet.mr_no))
;
--
-- View for looking at prescribing doctor payments: we go from bill_charge
--   bill status must not be open or cancelled
--   bill_charge status must not be cancelled
--
-- Note: bill_activity_charge is not used in the joins. Doctor amount can be incorrect
-- in CASE of packages, and therefore, the warning about doctor amounts vs. bill amount will be wrong.
--
DROP VIEW IF EXISTS pres_doctor_payments_view CASCADE;
CREATE OR REPLACE VIEW pres_doctor_payments_view AS
SELECT pr.center_id, bc.charge_id, bc.bill_no, bc.charge_group, bc.charge_head, bc.act_department_id,
	bc.act_description, bc.act_remarks, bc.act_rate, bc.act_unit, bc.act_quantity, bc.amount, bc.discount,
	bc.discount_reason, bc.charge_ref, bc.paid_amount, bc.posted_date AS bc_posted_date, bc.status,
	bc.approval_id, bc.orig_rate, bc.package_unit, bc.hasactivity, bc.act_description_id,
	bc.insurance_claim_amount,
	bc.overall_discount_auth, bc.overall_discount_amt, bc.discount_auth_dr, bc.dr_discount_amt,
	bc.discount_auth_pres_dr, bc.pres_dr_discount_amt, bc.discount_auth_ref, bc.ref_discount_amt,
	bc.discount_auth_hosp, bc.hosp_discount_amt,
	bc.payee_doctor_id, bc.doctor_amount, bc.doc_payment_id AS doctor_payment_id,
	bc.prescribing_dr_id, bc.prescribing_dr_amount, bc.prescribing_dr_payment_id,
	bc.referal_amount, bc.ref_payment_id,
	bc.out_house_amount, bc.oh_payment_id,
	CASE WHEN bc.charge_group='PKG' THEN 'Y' ELSE 'N' END AS package_charge,
	b.visit_id, b.visit_type, b.bill_type, b.open_date, b.status AS bill_status, b.closed_date,
	b.discharge_status, b.app_modified, b.username AS bill_username,
	b.remarks, coalesce(b.closed_by,b.username) AS closed_by, b.opened_by, b.mod_time,
	b.claim_recd_amount, b.finalized_date,b. total_amount, b.primary_claim_status,
	pr.mr_no, pr.primary_sponsor_id, pdet.patient_name, pdet.last_name,
	b.claim_recd_amount AS actual_claim_amt, chc.chargehead_name, chc.chargehead_id, chc.chargegroup_id, tpa.tpa_name,
	(CASE WHEN b.bill_type = 'C' THEN 'Bill Later' ELSE 'Bill Now' END) AS billtype,
	(CASE WHEN b.status = 'A' THEN 'Open' WHEN b.status = 'F' THEN 'Finalized'
		WHEN b.status = 'S' THEN 'Settled' WHEN b.status = 'C' THEN 'Closed' ELSE NULL END) AS billstatus,
	vtn.visit_type_long_name AS visittype,
	dac.disc_auth_name AS discount_auth_dr_name, dap.disc_auth_name AS discount_auth_pres_dr_name,
	daov.disc_auth_name AS overall_discount_auth_name, dar.disc_auth_name AS discount_auth_ref_name,
	(CASE WHEN b.is_tpa THEN 'Y' ELSE 'N' END) AS insurancestatus
FROM bill_charge bc
	JOIN bill b ON (bc.bill_no = b.bill_no)
	JOIN visit_type_names vtn ON (vtn.visit_type = b.visit_type)
	JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	JOIN patient_details pdet ON (pdet.mr_no=pr.mr_no)
	JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head)
	LEFT JOIN discount_authorizer dac ON bc.discount_auth_dr = dac.disc_auth_id
	LEFT JOIN discount_authorizer dap ON bc.discount_auth_pres_dr = dap.disc_auth_id
	LEFT JOIN discount_authorizer dar ON bc.discount_auth_ref = dar.disc_auth_id
	LEFT JOIN discount_authorizer dah ON bc.discount_auth_hosp = dah.disc_auth_id
	LEFT JOIN discount_authorizer daov ON bc.overall_discount_auth = daov.disc_auth_id
	LEFT JOIN insurance_case ic ON ic.insurance_id = pr.insurance_id
	LEFT JOIN tpa_master tpa ON tpa.tpa_id=ic.tpa_id
WHERE b.status IN ('F','S','C') AND bc.status <> 'X' 
AND (patient_confidentiality_check(COALESCE(pdet.patient_group, 0), pdet.mr_no));


DROP VIEW IF EXISTS store_stock_movement_view;
CREATE VIEW store_stock_movement_view AS
SELECT dept_name, medicine_name, (0::NUMERIC-sum(sale_qty)) AS sales, (0::NUMERIC-sum(sale_return_qty)) AS returns,
  sum(purchase_qty) AS purchase, sum(return_debit_qty) AS return_debit_qty,
  (0::NUMERIC-sum(transfer_out_qty)) AS transfer_out, sum(transfer_in_qty) AS transfer_in,
  (0::NUMERIC-sum(adj_dec_qty)) AS adj_dec, sum(adj_incr_qty) AS adj_inc,
  sum(supplier_return) AS supplier_return, sum(supplier_replacement) AS supplier_replacement, sum(issued_qty) AS issued_qty, sum(returned_qty) AS returned_qty, sum(transit_qty) AS transit_qty, m_date::DATE,
  (sum(purchase_qty)-sum(sale_qty)-sum(sale_return_qty)-sum(transfer_out_qty)+sum(transfer_in_qty)-sum(adj_dec_qty)+ sum(adj_incr_qty)+ sum(supplier_return)+sum(supplier_replacement) +sum(return_debit_qty) -sum(issued_qty) +sum(returned_qty)) AS sum_total
FROM (
		  SELECT
		  	d.dept_name, m.medicine_name,
		    CASE
		    	WHEN sm.type = 'S'
		    	THEN s.quantity
		    	ELSE 0
		    END AS sale_qty,
		    CASE
		    	WHEN sm.type = 'R'
		    	THEN s.quantity
		    	ELSE 0
		    END AS sale_return_qty,
		    0 AS purchase_qty, 0 AS transfer_out_qty,
		    0 AS transfer_in_qty, 0 AS adj_dec_qty,
		    0 AS adj_incr_qty, 0 AS return_debit_qty,
		    0 AS supplier_return, 0 AS supplier_replacement,
		    0 AS issued_qty, 0 AS returned_qty, 0 AS transit_qty,
		    date_trunc('day',sm.sale_date) AS m_date
		  FROM store_sales_details s
		  JOIN store_sales_main sm USING (sale_id)
		  JOIN store_item_details m USING(medicine_id)
		  JOIN stores d ON (d.dept_id = sm.store_id)

		UNION ALL

		  SELECT
		  	d.dept_name, m.medicine_name, 0, 0,
		  	((g.billed_qty + g.bonus_qty) * m.issue_base_unit) AS purchase_qty,
		 	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, date_trunc('day', gm.grn_date) AS m_date
		  FROM store_grn_details g
		  JOIN store_grn_main gm USING (grn_no)
		  JOIN store_item_details m USING (medicine_id)
		  JOIN stores d ON (d.dept_id = gm.store_id)
		  WHERE g.billed_qty>0

		UNION ALL

		  SELECT d.dept_name, m.medicine_name, 0, 0, 0, s.qty AS transfer_out_qty, 0,
		  	0, 0, 0, 0, 0, 0, 0, 0, date_trunc('day', sm.date_time) AS m_date
		  FROM store_transfer_details  s
		  JOIN store_transfer_main sm USING (transfer_no)
		  JOIN store_item_details m USING (medicine_id)
		  JOIN stores d ON (d.dept_id = sm.store_from)

		UNION ALL

		  SELECT d.dept_name, m.medicine_name, 0, 0, 0, 0, s.qty AS transfer_in_qty,
		  	0, 0, 0, 0, 0, 0, 0, 0, date_trunc('day', sm.date_time) AS m_date
		  FROM store_transfer_details s
		  JOIN store_transfer_main sm USING (transfer_no)
		  JOIN store_item_details m USING (medicine_id)
		  JOIN stores d ON (d.dept_id = sm.store_to)

		UNION ALL

		  SELECT d.dept_name, m.medicine_name, 0, 0, 0, 0, 0,
		    CASE
		    	WHEN a.type = 'R'
		    	THEN a.qty
		    	ELSE 0
		    END AS adj_dec_qty,
		    CASE
		    	WHEN a.type = 'A'
		    	THEN a.qty
		    	ELSE 0
		    END AS adj_incr_qty, 0, 0, 0, 0, 0, 0, date_trunc('day', am.date_time) AS m_date
		  FROM store_adj_details a
		  JOIN store_adj_main am USING(adj_no)
		  JOIN store_item_details m USING(medicine_id)
		  JOIN stores d ON (d.dept_id = am.store_id)

		 UNION ALL

		  SELECT d.dept_name, m.medicine_name, 0, 0, 0, 0,
		  	0, 0, 0, (round((g.billed_qty),0) * m.issue_base_unit) AS return_debit_qty,
		  	0, 0, 0, 0, 0, date_trunc('day', gm.grn_date) AS m_date
		  FROM store_grn_details g
		  JOIN store_grn_main gm USING(grn_no)
		  JOIN store_item_details m USING(medicine_id)
		  JOIN stores d ON(d.dept_id = gm.store_id)
		  WHERE g.billed_qty<0

		  UNION ALL

		  SELECT d.dept_name, m.medicine_name, 0, 0, 0, 0,
		  	0, 0, 0, 0, (round((qty*-1),0) * m.issue_base_unit) AS supplier_return,
		  	0, 0, 0, 0, date_trunc('day', psrm.date_time) AS m_date
		  FROM store_supplier_returns psr
		  JOIN store_supplier_returns_main psrm ON (psr.return_no=psrm.return_no)
		  JOIN store_item_details m USING(medicine_id)
		  JOIN stores d ON(d.dept_id = psrm.store_id)
		  WHERE qty>0

		  UNION ALL

		  SELECT d.dept_name, m.medicine_name, 0, 0, 0, 0,
		  	0, 0, 0, 0, 0, (round((replaced_qty),0) * m.issue_base_unit) AS supplier_replacement,
		  	0, 0, 0, date_trunc('day', psrm.date_time) AS m_date
		  FROM store_supplier_returns psr
		  JOIN store_supplier_returns_main psrm USING(return_no)
		  JOIN store_item_details m USING(medicine_id)
		  JOIN stores d ON(d.dept_id = psrm.store_id)
		  WHERE qty>0

		  UNION ALL

		  SELECT d.dept_name, m.medicine_name, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0,sid.qty AS issued_qty, 0, 0,
            date_trunc('day', sim.date_time) AS m_date
          FROM stock_issue_details sid
          JOIN stock_issue_main sim USING(user_issue_no)
          JOIN store_item_details m USING(medicine_id)
          JOIN stores d ON(d.dept_id = sim.dept_from)
          WHERE qty>0

          UNION ALL

           SELECT d.dept_name, m.medicine_name, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, sird.qty AS returned_qty, 0,
                date_trunc('day', sirm.date_time) AS m_date
          FROM store_issue_returns_details sird
          JOIN store_issue_returns_main sirm USING(user_return_no)
          JOIN store_item_details m USING(medicine_id)
          JOIN stores d ON(d.dept_id = sirm.dept_to)
          WHERE qty>0

          UNION ALL

          SELECT d.dept_name, m.medicine_name, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, ssd.qty_in_transit AS transit_qty,
			date_trunc('day', ssd.stock_time) AS m_date
		  FROM store_stock_details ssd
		  JOIN store_item_details m USING(medicine_id)
		  JOIN stores d ON(d.dept_id = ssd.dept_id)
		  WHERE qty_in_transit>0

) AS all_stock_changes
GROUP BY dept_name,medicine_name, m_date
ORDER BY dept_name, medicine_name;


DROP FUNCTION IF EXISTS custom_srxml_trigger() CASCADE;
CREATE OR REPLACE FUNCTION custom_srxml_trigger()
  RETURNS trigger AS $BODY$
DECLARE
	srxmlFile varchar(255);
	repType varchar(10);
	repId integer;
BEGIN
	IF ( TG_OP = 'DELETE' ) THEN
		repType := OLD.report_type ;
		srxmlFile := OLD.report_name||'.srjs';
		repId := OLD.report_id;

		IF (repType = 'srjs') THEN
			DELETE FROM favourite_reports where custom_report_name = srxmlFile;
		END IF;
	END IF;
	RETURN OLD;
END;
$BODY$
  LANGUAGE 'plpgsql' ;

DROP TRIGGER IF EXISTS custom_srxml_trigger ON custom_reports CASCADE;
CREATE TRIGGER custom_srxml_trigger
  BEFORE DELETE
  ON custom_reports
  FOR EACH ROW
  EXECUTE PROCEDURE custom_srxml_trigger();



DROP FUNCTION IF EXISTS favourites_deleted_trigger() CASCADE;
CREATE OR REPLACE FUNCTION favourites_deleted_trigger()
  RETURNS TRIGGER AS $BODY$
BEGIN
	IF ( TG_OP = 'DELETE' ) THEN
		DELETE FROM emailable_reports_config where report_id =OLD.report_title ;
	END IF;
	RETURN OLD;
END;
$BODY$ LANGUAGE 'plpgsql' ;

DROP TRIGGER IF EXISTS favourites_deleted_trigger ON favourite_reports CASCADE;
CREATE TRIGGER favourites_deleted_trigger
  BEFORE DELETE
  ON favourite_reports
  FOR EACH ROW
  EXECUTE PROCEDURE favourites_deleted_trigger();

DROP VIEW IF EXISTS store_itemqty_view CASCADE;
CREATE OR REPLACE VIEW store_itemqty_view AS
	SELECT medicine_id,COALESCE(availableqty,0) AS availableqty,issue_type,identification,dept_id FROM
		(SELECT medicine_id,issue_type,identification FROM store_item_details
		LEFT JOIN store_category_master ON category_id= med_category_id) AS iv
	LEFT JOIN
		(SELECT medicine_id,SUM(qty) AS availableqty,dept_id FROM store_stock_details
		WHERE asset_approved='Y' group by medicine_id,dept_id ) AS im USING(medicine_id)
	GROUP BY medicine_id,availableqty,issue_type,identification,dept_id ;


DROP VIEW IF EXISTS all_tests_ordered_view;
CREATE OR REPLACE VIEW all_tests_ordered_view AS
	SELECT tp.mr_no, tp.pat_id,tp.exp_rep_ready_time,tp.test_id, tp.labno, tp.common_order_id,
		d.test_name, tp.conducted, tp.sflag, tp.prescribed_id, tp.is_outhouse_selected, tp.remarks,
		coalesce(isr.patient_name, get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name)) AS patient_full_name,
		pd.patient_name, isr.patient_name AS inc_patient_name, isr.incoming_visit_id,
		pd.last_name, s.salutation, d.sample_needed, d.ddept_id,
		CASE WHEN is_outhouse_test(d.test_id,coalesce(pr.center_id, isr.center_id)::integer) THEN 'O' ELSE 'I' END AS house_status,
		isr.patient_other_info,tp.priority, b.bill_type, b.payment_status, b.visit_type, pr.op_type, otn.op_type_name,
		coalesce(tp.report_id, 0) AS report_id,
		coalesce(tvr.report_name, 'No Report') AS report_name, tvr.signed_off,
		case WHEN tvr.report_date IS NOT NULL THEN 'Y' ELSE 'N' END AS report_data,
		tvr.report_date, tp.re_conduction, bc.charge_group, tp.pres_date AS pres_date,
		ih.hospital_name AS ih_name, om.oh_name,
		coalesce(pr.reg_date, isr.date) AS reg_date, bac.payment_charge_head AS charge_head, dd.category
		,st.sample_type,COALESCE(sc.coll_sample_no, sc.sample_sno) AS sample_sno,sc.sample_status AS sample_collection_status,st.sample_container, CASE WHEN d.sample_needed = 'n' THEN 'U' ELSE tp.sflag END AS sample_status,
		coalesce(pr.center_id, isr.center_id) AS center_id,sc.sample_date,tp.sample_no,isrd.orig_sample_no,tp.pres_doctor,presdoc.doctor_name,
		(select count(prescribed_id) from test_documents td where td.prescribed_id=tp.prescribed_id) AS doc_count,
		tp.token_number, COALESCE(st.status, 'A') AS sample_type_status,
		case WHEN d.results_entry_applicable='t' THEN 'Yes' ELSE 'No' END AS results_entry_applicable,
		CASE WHEN pr.patient_id IS NOT NULL THEN 'hospital' ELSE 'incoming' END AS hospital,dom.outsource_dest_id,
		dom.outsource_dest, dom.outsource_dest_type, isr.incoming_source_type, condoc.doctor_name AS conducting_doctor_name,
		condoc.doctor_id, sc.sample_receive_status, sc.sample_transfer_status, d.conducting_doc_mandatory,
		case WHEN coalesce(tpa.sponsor_type, itpa.sponsor_type, 'R')='R' THEN 'R' ELSE 'S' END AS patient_sponsor_type,
		COALESCE(itp.pat_id, tp.pat_id) AS collection_center_visit_id
	FROM tests_prescribed tp
	JOIN diagnostics d ON d.test_id = tp.test_id
	JOIN diagnostics_departments dd ON dd.ddept_id = d.ddept_id
	LEFT JOIN sample_type st ON (st.sample_type_id=d.sample_type_id)
	LEFT JOIN patient_registration pr  ON (pr.patient_id=tp.pat_id)
	LEFT JOIN patient_details pd  ON (pr.mr_no=pd.mr_no)
	LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
	LEFT JOIN tpa_master tpa ON (pr.primary_sponsor_id=tpa.tpa_id)
	LEFT JOIN op_type_names otn ON (otn.op_type = pr.op_type)
	LEFT JOIN incoming_sample_registration isr ON tp.pat_id = isr.incoming_visit_id
	LEFT JOIN incoming_sample_registration_details isrd ON (isrd.prescribed_id = tp.prescribed_id AND isr.incoming_visit_id = isrd.incoming_visit_id)
	-- these are used to get the sponsor type of the original visit for internal lab patient.
	LEFT JOIN tests_prescribed itp ON (itp.prescribed_id=tp.coll_prescribed_id)
	LEFT JOIN patient_registration ipr ON (ipr.patient_id=itp.pat_id)
	LEFT JOIN tpa_master itpa ON (itpa.tpa_id=ipr.primary_sponsor_id)

	LEFT JOIN salutation_master s ON (s.salutation_id = pd.salutation)
	LEFT JOIN bill_activity_charge bac on
		(bac.activity_id=tp.prescribed_id::varchar) AND bac.activity_code = 'DIA'
	LEFT JOIN bill_charge bc ON  bc.charge_id=bac.charge_id
	LEFT JOIN bill b ON b.bill_no=bc.bill_no
	LEFT JOIN test_visit_reports tvr ON ( tvr.report_id = tp.report_id)
	LEFT JOIN incoming_hospitals ih ON (ih.hospital_id = isr.orig_lab_name)
	LEFT JOIN outsource_sample_details oh ON (oh.prescribed_id = tp.prescribed_id)
	LEFT JOIN diag_outsource_master dom ON (dom.outsource_dest_id = oh.outsource_dest_id)
	LEFT JOIN outhouse_master om ON (om.oh_id = dom.outsource_dest)
	left join sample_collection sc ON (tp.sample_no = sc.sample_sno)
	LEFT JOIN doctors presdoc ON(presdoc.doctor_id = tp.pres_doctor)
	LEFT JOIN doctors condoc ON (condoc.doctor_id = COALESCE(bac.doctor_id, (case WHEN bc.charge_group='PKG' THEN null ELSE bc.payee_doctor_id end)))
	WHERE ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) )
;

DROP FUNCTION IF EXISTS insert_or_update_deposits_totals() CASCADE;

--
-- Update deposit set offs whenever the deposit is used up in a bill.
-- Called ON inserts or updates to a bill -- A bill can be created fresh with a deposit
-- set off inside it, if called from the pharmacy sales screen. Otherwise, it is only
-- update from the bill screen.
--
CREATE OR REPLACE FUNCTION update_deposit_setoffs() RETURNS trigger AS $BODY$
DECLARE
	extraAmt numeric(15,2);
	oldAmt numeric(15,2);
	newAmt numeric(15,2);
	mrNo character varying;
BEGIN
	IF (TG_OP = 'INSERT') THEN
		extraAmt = NEW.deposit_set_off;
	ELSE  -- UPDATE
		-- handle bill cancel AS well AS change in deposit amount.
		IF OLD.status != 'X' THEN oldAmt := OLD.deposit_set_off; ELSE oldAmt := 0; END IF;
		IF NEW.status != 'X' THEN newAmt := NEW.deposit_set_off; ELSE newAmt := 0; END IF;
		extraAmt = newAmt - oldAmt;
	END IF;

	IF extraAmt = 0 THEN
		-- no change: just return.
		RETURN NEW;
	END IF;

	SELECT mr_no FROM patient_registration WHERE patient_id=NEW.visit_id INTO mrNo;

	UPDATE deposit_setoff_total SET
		hosp_total_setoffs = hosp_total_setoffs + extraAmt,
		hosp_total_balance = hosp_total_balance - extraAmt
	WHERE mr_no=mrNo;

	RETURN NEW;
END;
$BODY$ LANGUAGE 'plpgsql';


DROP TRIGGER IF EXISTS update_deposits_totals ON bill;
DROP TRIGGER IF EXISTS update_deposit_setoffs ON bill;
CREATE TRIGGER update_deposit_setoffs
  AFTER INSERT OR UPDATE ON bill
  FOR EACH ROW EXECUTE PROCEDURE update_deposit_setoffs();

  -- validation function for purchase details
CREATE OR REPLACE FUNCTION validateName(character varying,int,int,character varying,character varying)
  RETURNS character varying AS
  $BODY$
DECLARE
    sampleval ALIAS FOR $5;
    row_data character varying;
    cond character varying;
    exists boolean;
BEGIN
cond:= E'\\*`';
exists:= false;
FOR row_data IN EXECUTE 'select regexp_split_to_table('|| $1 || ' ,'|| quote_literal(cond) ||' ) FROM store_stock_details
	where dept_id='|| $2 || ' and medicine_id='|| $3 || ' and batch_no='|| quote_literal($4) || '' loop
if (row_data = sampleval) then
    return true;
end if;
end loop;
RETURN exists;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE
  COST 100;

DROP VIEW IF EXISTS store_stock_details_view CASCADE ;
CREATE OR REPLACE VIEW store_stock_details_view AS
 SELECT g.generic_name, pmd.medicine_name, sibd.batch_no, pmsd.dept_id, sibd.mrp,
 pmd.issue_base_unit AS pkg_size, pmd.tax_rate, pmsd.package_cp, pmsd.qty,
 pmsd.qty_in_use + pmsd.qty_maint + pmsd.qty_retired + pmsd.qty_lost + pmsd.qty_unknown + pmsd.qty_kit + pmsd.qty_in_transit
 AS qty_not_avbl, pmd.medicine_id, m.manf_name, m.manf_mnemonic, pmd.bin, sibd.exp_dt,
 icm.category AS category_name, icm.billable, icm.issue_type, pmsd.item_ced_amt,
 replace(pmsd.item_supplier_name, '*`'::text, ','::text) AS supplier_name,
 replace(pmsd.item_grn_no, '*`'::text, ','::text) AS grn_no,
 replace(pmsd.item_invoice_no, '*`'::text, ','::text) AS invoice_no, pmd.med_category_id,
 replace(pmsd.item_supplier_code, '*`'::text, ','::text) AS supplier_code, pmsd.asset_approved,
 pmsd.consignment_stock, icm.expiry_date_val,
        CASE
            WHEN pmsd.qty > 0::numeric THEN 'g'::text
            WHEN pmsd.qty < 0::numeric THEN 'l'::text
            ELSE 'e'::text
        END AS qtycond, icm.identification, pmsd.stock_pkg_size,
        CASE
            WHEN sibd.exp_dt < current_date::date THEN 'Y'::text
            ELSE 'N'::text
        END AS expired, pmd.issue_units,pmd.item_barcode_id, pmsd.package_uom
   FROM store_stock_details pmsd
   JOIN store_item_batch_details sibd USING(item_batch_id)
   JOIN store_item_details pmd ON pmsd.medicine_id = pmd.medicine_id
   JOIN manf_master m ON m.manf_code::text = pmd.manf_name::text
   LEFT JOIN generic_name g ON g.generic_code::text = pmd.generic_name::text
   JOIN store_category_master icm ON icm.category_id = pmd.med_category_id
  GROUP BY g.generic_name, pmd.medicine_name, sibd.batch_no, pmsd.dept_id, sibd.mrp,
  pmd.issue_base_unit, pmd.tax_rate, pmsd.package_cp, pmsd.qty, pmsd.qty_in_use, pmsd.qty_maint,
  pmsd.qty_retired, pmsd.qty_lost, pmsd.qty_unknown, pmsd.qty_in_transit, pmsd.qty_kit,
  pmd.medicine_id, pmd.bin, m.manf_name, m.manf_mnemonic, sibd.exp_dt, icm.category, icm.billable,
  icm.issue_type, pmd.med_category_id, pmsd.asset_approved, pmsd.consignment_stock, pmsd.item_ced_amt,
  pmsd.item_supplier_name, pmsd.item_grn_no, pmsd.item_invoice_no, pmsd.item_supplier_code,
  icm.expiry_date_val, icm.identification, pmsd.stock_pkg_size, pmd.issue_units,pmd.item_barcode_id, pmsd.package_uom;


DROP VIEW IF EXISTS store_cons_invoice_totals_view_by_vatrate;
CREATE VIEW store_cons_invoice_totals_view_by_vatrate AS
SELECT si.supplier_id, pmd.med_category_id,sci.issue_id, date(con_invoice_date) AS con_invoice_date,
	si.supplier_invoice_id, sum((g.cost_price/g.grn_pkg_size*sid.qty) - (g.discount/g.total_qty*sid.qty)) AS grn_amt,
	sum((sid.qty * g.tax)/g.billed_qty) AS grn_tax,
	 CASE WHEN si.tax_name = 'CST' THEN si.cst_rate ELSE g.tax_rate END AS vat_rate, hcm.center_code, s.center_id,
	 CASE WHEN sim.user_type = 'Patient' THEN  pdept.cost_center_code ELSE dept.cost_center_code END AS dept_center_code,
	 CASE WHEN si.tax_name = 'CST' THEN s.purchases_store_cst_account_prefix ELSE s.purchases_store_vat_account_prefix END AS purchase_store_account_prefix,
	 sci.consignment_invoice_no
FROM store_consignment_invoice sci
	JOIN store_invoice si USING (supplier_invoice_id)
	JOIN store_grn_main gm USING (grn_no)
	JOIN store_grn_details g ON (gm.grn_no=g.grn_no and sci.medicine_id=g.medicine_id and sci.batch_no=g.batch_no)
	JOIN store_item_details pmd ON (g.medicine_id=pmd.medicine_id)
	JOIN stores s ON (gm.store_id=s.dept_id)
	JOIN stock_issue_details sid ON (sid.user_issue_no=sci.issue_id
		and sid.medicine_id=sci.medicine_id and sid.batch_no=sci.batch_no)
	JOIN stock_issue_main sim ON (sim.user_issue_no=sci.issue_id)
	LEFT JOIN patient_registration pr ON (pr.patient_id=sim.issued_to)
	LEFT JOIN department pdept ON (pdept.dept_id=pr.dept_name)
	LEFT JOIN department dept ON (dept.dept_id=sim.issued_to)
	LEFT JOIN hospital_center_master dhcm ON (dhcm.center_id=pr.center_id)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id=s.center_id)
where si.consignment_stock=true
GROUP BY sci.consignment_invoice_no, si.supplier_invoice_id, si.supplier_id, sci.issue_id, pmd.med_category_id,
	g.tax_rate, si.tax_name, si.cst_rate, date(con_invoice_date), hcm.center_code, s.center_id,
	pdept.cost_center_code, dept.cost_center_code, sim.user_type,
	(CASE WHEN si.tax_name = 'CST' THEN s.purchases_store_cst_account_prefix ELSE s.purchases_store_vat_account_prefix end)

;

DROP VIEW IF EXISTS payments_consolidated_ext_view CASCADE;
CREATE OR REPLACE VIEW payments_consolidated_ext_view AS
SELECT
	b.bill_no,date(b.finalized_date) AS date , bc.charge_id, cgc.chargegroup_name,  d.test_name AS test_name ,s.service_name AS service_name,
	bc.activity_conducted, bc.amount AS bill_amount,
	doc.doctor_name, bc.doctor_amount, pdoc.doctor_name AS prescribing_doctor_name, prescribing_dr_amount,
	coalesce(rdoc.doctor_name, ref.referal_name) AS referal_doctor, bc.referal_amount,
	ptpa.tpa_name AS primary_tpa_name, stpa.tpa_name AS secondary_tpa_name,
	ipm.plan_name, in_cat.category_name AS plan_type,
	picm.insurance_co_name AS primary_insurance_co_name, sicm.insurance_co_name AS secondary_insurance_co_name,
	(coalesce(bc.doctor_amount,0)+coalesce(bc.prescribing_dr_amount,0)+coalesce(bc.referal_amount,0)) AS total_to_pay_amt,
	(coalesce(bc.amount,0)-coalesce(bc.doctor_amount,0)-coalesce(bc.prescribing_dr_amount,0)-coalesce(bc.referal_amount,0)) AS hospital_amt,
	bc.doc_payment_id, bc.prescribing_dr_payment_id, bc.ref_payment_id,
	pd1.amount AS doc_paid_amt, pd2.amount AS pres_dr_paid_amt, pd3.amount AS ref_paid_amt,
	pd1.voucher_no AS doc_voucher, pd2.voucher_no AS pres_voucher, pd3.voucher_no AS ref_voucher,
	(coalesce(pd1.amount,0)+coalesce(pd2.amount,0)+coalesce(pd3.amount,0)) AS total_paid_amt,
	(
	   (coalesce(bc.doctor_amount,0)+coalesce(bc.prescribing_dr_amount,0)+coalesce(bc.referal_amount,0)) -
	   (coalesce(pd1.amount,0)+coalesce(pd2.amount,0)+coalesce(pd3.amount,0))
	) AS yet_to_pay_amt,
	 COALESCE(
	        (SELECT bahi.account_head_name FROM bill_charge_item_account bcia
	            JOIN bill_account_heads bahi ON (bahi.account_head_id = bcia.account_head_id)
	        WHERE bcia.chargehead_id = bc.charge_head AND bcia.item_id = bc.act_description_id
	        ), bahc.account_head_name
	 ) AS ac_head, agm.account_group_name, chc.chargehead_name,bc.act_description, bc.amount, bc.discount,
	 b.visit_id AS visit_id, b.bill_type, vtn.visit_type_name,pr.mr_no, pr.op_type, otn.op_type_name,
	 COALESCE (get_patient_full_name(sm.salutation, pdd.patient_name, pdd.middle_name, pdd.last_name), prc.customer_name, isr.patient_name)
	 AS patient_full_name,pdd.patient_gender,pcm.category_name, dep.dept_name, gd.dept_name AS store_name,
	 od.org_name, b.status as bill_status, b.payment_status, b.primary_claim_status,pr.center_id,pdd.name_local_language
FROM bill_charge bc
JOIN bill b USING(bill_no)
LEFT JOIN doctors doc ON  doc.doctor_id =bc.payee_doctor_id
LEFT JOIN doctors pdoc ON pdoc.doctor_id =bc.prescribing_dr_id
LEFT JOIN patient_registration pr  ON pr.patient_id=b.visit_id
LEFT JOIN op_type_names otn ON (otn.op_type = pr.op_type)
LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
LEFT JOIN patient_details pdd ON (pdd.mr_no = pr.mr_no)
LEFT JOIN salutation_master sm ON (sm.salutation_id = pdd.salutation)
LEFT JOIN doctors rdoc ON rdoc.doctor_id = pr.reference_docto_id
LEFT JOIN referral ref ON (ref.referal_no = pr.reference_docto_id )
LEFT JOIN tpa_master ptpa ON (ptpa.tpa_id = pr.primary_sponsor_id)
LEFT JOIN tpa_master stpa ON (stpa.tpa_id = pr.secondary_sponsor_id)
LEFT JOIN insurance_plan_main ipm ON (ipm.plan_id = pr.plan_id)
LEFT JOIN insurance_category_master in_cat ON (in_cat.category_id = ipm.category_id)
LEFT JOIN insurance_company_master picm ON (picm.insurance_co_id = pr.primary_insurance_co)
LEFT JOIN insurance_company_master sicm ON (sicm.insurance_co_id = pr.secondary_insurance_co)
JOIN chargegroup_constants cgc ON cgc.chargegroup_id=bc.charge_group
LEFT JOIN diagnostics d ON (bc.act_description_id =d.test_id)
LEFT JOIN services s ON (bc.act_description_id =s.service_id)
LEFT JOIN payments_details pd1 ON (pd1.charge_id =bc.charge_id AND pd1.payment_id=bc.doc_payment_id AND pd1.voucher_no IS NOT NULL )
LEFT JOIN payments_details pd2 ON (pd2.charge_id =bc.charge_id AND pd2.payment_id=bc.prescribing_dr_payment_id AND pd2.voucher_no IS NOT NULL )
LEFT JOIN payments_details pd3 ON (pd3.charge_id =bc.charge_id AND pd3.payment_id=bc.ref_payment_id AND pd3.voucher_no IS NOT NULL )
LEFT JOIN account_group_master agm  ON (agm.account_group_id = bc.account_group)
LEFT JOIN patient_category_master pcm ON (pcm.category_id = pr.patient_category_id)
LEFT JOIN department dep ON (dep.dept_id = pr.dept_name)
LEFT JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head)
LEFT JOIN visit_type_names vtn ON (vtn.visit_type = b.visit_type)
LEFT JOIN store_sales_main pmsm ON (pmsm.charge_id = bc.charge_id)
LEFT JOIN stores gd ON (gd.dept_id = pmsm.store_id)
LEFT JOIN organization_details od ON (od.org_id = pr.org_id)
LEFT JOIN bill_account_heads bahc ON (bahc.account_head_id = chc.account_head_id)
WHERE  b.status IN ('F','C','S') AND bc.status!='X' 
AND (patient_confidentiality_check(COALESCE(pdd.patient_group, 0), pdd.mr_no))

UNION ALL
SELECT
	b.bill_no,date(b.finalized_date) AS date,  bc.charge_id, cgc.chargegroup_name,  d.test_name AS test_name ,s.service_name AS service_name,
	bac.activity_conducted, bc.amount AS bill_amount,
	doc.doctor_name, bac.doctor_amount, NULL AS prescribing_doctor_name, 0 AS prescribing_dr_amount,NULL AS referal_doctor, 0 AS referal_amount,
	ptpa.tpa_name AS primary_tpa, stpa.tpa_name AS secondary_tpa,
	ipm.plan_name, in_cat.category_name AS plan_type,
	picm.insurance_co_name AS primary_insurance_co_name, sicm.insurance_co_name AS secondary_insurance_co_name,
	coalesce(bac.doctor_amount,0) AS total_to_pay_amt,
	(coalesce(bc.amount,0)-coalesce(bac.doctor_amount,0)) AS hospital_amt,
	bac.doctor_payment_id, NULL, NULL,
	pd.amount AS doc_paid_amt, 0,0,
	pd.voucher_no AS doc_voucher, NULL, NULL,
	coalesce(pd.amount,0) AS total_paid_amt,
	(coalesce(bac.doctor_amount,0) - coalesce(pd.amount,0)) AS yet_to_pay_amt,
	COALESCE(
	(SELECT bahi.account_head_name FROM bill_charge_item_account bcia
	    JOIN bill_account_heads bahi ON (bahi.account_head_id = bcia.account_head_id)
	WHERE bcia.chargehead_id = bc.charge_head AND bcia.item_id = bc.act_description_id
	), bahc.account_head_name
	) AS ac_head,agm.account_group_name,chc.chargehead_name,bc.act_description, bc.amount, bc.discount,
	 b.visit_id AS visit_id, b.bill_type, vtn.visit_type_name,pr.mr_no,pr.op_type, otn.op_type_name,
	 COALESCE (get_patient_full_name(sm.salutation, pdd.patient_name, pdd.middle_name, pdd.last_name), prc.customer_name, isr.patient_name)
	AS patient_full_name,pdd.patient_gender,pcm.category_name, dep.dept_name,
	gd.dept_name AS store_name,od.org_name, b.status as bill_status, b.payment_status, b.primary_claim_status,pr.center_id,pdd.name_local_language
FROM bill_activity_charge bac
JOIN bill_charge bc ON (bc.charge_id=bac.charge_id )
JOIN bill b USING(bill_no)
LEFT JOIN doctors doc ON  doc.doctor_id =bac.doctor_id
LEFT JOIN patient_registration pr  ON pr.patient_id=b.visit_id
LEFT JOIN op_type_names otn ON (otn.op_type = pr.op_type)
LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
LEFT JOIN patient_details pdd ON (pdd.mr_no = pr.mr_no)
LEFT JOIN salutation_master sm ON (sm.salutation_id = pdd.salutation)
LEFT JOIN tpa_master ptpa ON (ptpa.tpa_id = pr.primary_sponsor_id)
LEFT JOIN tpa_master stpa ON (stpa.tpa_id = pr.secondary_sponsor_id)
LEFT JOIN insurance_plan_main ipm ON (ipm.plan_id = pr.plan_id)
LEFT JOIN insurance_category_master in_cat ON (in_cat.category_id = ipm.category_id)
LEFT JOIN insurance_company_master picm ON (picm.insurance_co_id = pr.primary_insurance_co)
LEFT JOIN insurance_company_master sicm ON (sicm.insurance_co_id = pr.secondary_insurance_co)
JOIN chargegroup_constants cgc ON cgc.chargegroup_id=bac.charge_group
LEFT JOIN diagnostics d ON (bac.act_description_id =d.test_id)
LEFT JOIN services s ON (bac.act_description_id =s.service_id)
LEFT JOIN payments_details pd ON (pd.charge_id =bac.charge_id AND pd.payment_id=bac.doctor_payment_id AND pd.voucher_no IS NOT NULL )
LEFT JOIN account_group_master agm  ON (agm.account_group_id = bc.account_group)
LEFT JOIN patient_category_master pcm ON (pcm.category_id = pr.patient_category_id)
LEFT JOIN department dep ON (dep.dept_id = pr.dept_name)
LEFT JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head)
LEFT JOIN visit_type_names vtn ON (vtn.visit_type = b.visit_type)
LEFT JOIN store_sales_main pmsm ON (pmsm.charge_id = bc.charge_id)
LEFT JOIN stores gd ON (gd.dept_id = pmsm.store_id)
LEFT JOIN organization_details od ON (od.org_id = pr.org_id)
LEFT JOIN bill_account_heads bahc ON (bahc.account_head_id = chc.account_head_id)
WHERE  b.status IN ('F','C','S') AND bc.status!='X'
AND (patient_confidentiality_check(COALESCE(pdd.patient_group, 0), pdd.mr_no))
;

-- TN Purchase Tax Report View
DROP VIEW IF EXISTS purchase_details_view_by_vatrate CASCADE;
CREATE VIEW purchase_details_view_by_vatrate AS
SELECT sm.supplier_name,sm.cust_supplier_code,coalesce(supplier_tin_no,'') AS supplier_tin_no,invoice_no,i.invoice_date,
	sum(g.cost_price*g.billed_qty/g.grn_pkg_size) AS grn_amt, sum(g.tax) AS grn_tax,
	gm.grn_date,
	CASE WHEN i.tax_name = 'CST' THEN i.cst_rate ELSE g.tax_rate END AS vat_rate,gd.dept_id AS store_id,
	gd.store_type_id
FROM store_grn_details g
	JOIN store_grn_main gm USING (grn_no)
	left JOIN store_invoice i USING (supplier_invoice_id)
	JOIN store_item_details pmd using (medicine_id)
	JOIN stores gd ON (gm.store_id= gd.dept_id)
        join supplier_master sm ON (sm.supplier_code = i.supplier_id)
        join store_category_master ON med_Category_id=category_id
GROUP BY sm.supplier_name,sm.cust_supplier_code,supplier_tin_no, invoice_no,i.invoice_date,gm.grn_date, pmd.med_category_id,
g.tax_rate, i.tax_name, i.cst_rate,gd.dept_id,gd.store_type_id;


-- TN Sales Tax Report View
DROP VIEW IF EXISTS sale_details_view_by_vatrate CASCADE;
CREATE VIEW sale_details_view_by_vatrate AS
select tax_rate,sum(amount-tax) AS bill_amount,sum(tax)as tax_amount,sale_date,type,gd.dept_id AS store_id,
gd.store_type_id
from  store_sales_details pms
join  store_sales_main pmsm on(pms.sale_id=pmsm.sale_id)
join stores gd ON (gd.dept_id = pmsm.store_id)
group by tax_rate,sale_date,type,gd.dept_id,gd.store_type_id
order by type desc,tax_rate;

-- TN Supplier Returns Tax Report View
DROP VIEW IF EXISTS purchase_returns_view_by_vatrate CASCADE;
CREATE VIEW purchase_returns_view_by_vatrate AS
SELECT sum(g.cost_price*(0-g.billed_qty)/g.grn_pkg_size) AS grn_amt, sum(g.tax) AS grn_tax,
	CASE WHEN sdn.tax_name = 'CST' THEN sdn.cst_rate ELSE g.tax_rate END AS vat_rate,gd.dept_id AS store_id,
	gd.store_type_id,debit_note_date
FROM store_grn_details g
	JOIN store_grn_main gm USING (grn_no)
	join store_debit_note sdn ON sdn.debit_note_no = gm.debit_note_no
	JOIN stores gd ON (gm.store_id= gd.dept_id)
GROUP BY tax_rate, sdn.tax_name, sdn.cst_rate,gd.dept_id,gd.store_type_id,debit_note_date;

-- Store Supplier Return View

DROP VIEW IF EXISTS store_supplier_returns_view CASCADE;
CREATE VIEW store_supplier_returns_view AS
SELECT s.supplier_name,s.cust_supplier_code,srm.supplier_id,medicine_name,sr.batch_no,sr.qty,user_name,return_no,srm.date_time AS date,
		case WHEN return_type='D' THEN 'Damage' WHEN return_type='E' THEN 'Expiry' ELSE 'Others' END AS returntype,
		round((ssd.package_cp/sitd.issue_base_unit)*sr.qty,2) AS amt,orig_return_no,
		case WHEN srm.status='O' THEN 'Open' WHEN srm.status='C' THEN 'Closed' WHEN srm.status='P' THEN 'Partially Received' ELSE 'Received' END AS retstatus,
		case WHEN orig_return_no is null THEN 'R' ELSE 'X' END AS origstatus
	FROM store_supplier_returns sr
	JOIN store_supplier_returns_main srm using (return_no)
	JOIN supplier_master s ON (s.supplier_code = supplier_id)
	JOIN store_item_details sitd using(medicine_id)
	JOIN store_stock_details ssd ON dept_id=srm.store_id and ssd.medicine_id=sr.medicine_id and sr.batch_no=ssd.batch_no
	GROUP BY s.supplier_name,s.cust_supplier_code,medicine_name,sr.batch_no,sr.qty,user_name,srm.status,returntype,return_no,date,ssd.package_cp,sitd.issue_base_unit,orig_return_no,srm.supplier_id
	ORDER BY s.supplier_name,return_no
;
-- Stores stock transfer view
DROP VIEW IF EXISTS store_stock_transfer_view CASCADE ;
CREATE OR REPLACE VIEW  store_stock_transfer_view AS
select stm.transfer_no, stm.date_time, stm.store_from, stm.store_to, stm.username,st.medicine_id,
st.batch_no, st.qty,st.indent_no, st.qty_rejected, st.qty_recd,st.item_batch_id
from store_transfer_main stm join store_transfer_details st using (transfer_no);

DROP TRIGGER IF EXISTS common_charges_update_timestamp ON common_charges_master;
CREATE TRIGGER common_charges_update_timestamp AFTER INSERT OR UPDATE ON common_charges_master
  FOR EACH STATEMENT EXECUTE PROCEDURE update_master_timestamp_trigger();




DROP VIEW IF EXISTS all_services_ordered_view;
CREATE OR REPLACE VIEW all_services_ordered_view AS
	SELECT sp.mr_no, sp.patient_id, sp.service_id,
		s.service_name, sp.conducted, sp.prescription_id, sp.remarks,
		get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS patient_full_name,
		pd.patient_name, pd.last_name, sm.salutation, s.conduction_applicable,
		b.bill_type, b.payment_status, b.visit_type, pr.op_type, otn.op_type_name,
		bc.charge_group, sp.presc_date AS pres_date,
		pr.reg_date, bac.payment_charge_head AS charge_head, sd.doc_name AS report_name,
		sd.username AS report_generated_user, sd.doc_id, coalesce(sd.signed_off, false) AS signed_off, sdept.serv_dept_id AS service_department,
		sp.doctor_id AS pres_doctor_id, sp.conductedby AS cond_doctor_id,
		coalesce(cd.doctor_name, cht.tech_name) AS cond_doctor_name,
		coalesce(presdoc.doctor_name, pht.tech_name) AS pres_doctor_name, sd.username AS report_user_name,
		(CASE WHEN dat.doc_format IS NULL THEN 'doc_fileupload' ELSE dat.doc_format END) AS doc_format,
		sd.doc_date, dat.access_rights, sp.conducteddate, pr.center_id

	FROM services_prescribed sp
	JOIN patient_registration pr  ON (sp.patient_id=pr.patient_id)
	JOIN patient_details pd  ON (pr.mr_no=pd.mr_no)
	JOIN bill_activity_charge bac on
		(bac.activity_id=sp.prescription_id::varchar) AND bac.activity_code = 'SER'
	JOIN bill_charge bc ON  bc.charge_id=bac.charge_id
	JOIN bill b ON b.bill_no=bc.bill_no
	JOIN services s ON sp.service_id = s.service_id
	JOIN services_departments sdept ON (sdept.serv_dept_id=s.serv_dept_id)
	LEFT JOIN service_documents sd ON (sp.prescription_id=sd.prescription_id)
	LEFT JOIN patient_documents pdoc ON sd.doc_id=pdoc.doc_id
	LEFT OUTER JOIN doc_all_templates_view dat ON (pdoc.template_id=dat.template_id AND pdoc.doc_format=dat.doc_format)
	LEFT JOIN op_type_names otn ON (otn.op_type = pr.op_type)
	JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
	LEFT JOIN doctors cd ON (sp.conductedby=cd.doctor_id)
	LEFT JOIN hospital_technical cht ON (sp.conductedby=cht.tech_id)
	LEFT JOIN doctors presdoc ON (sp.doctor_id=presdoc.doctor_id)
	LEFT JOIN hospital_technical pht ON (sp.doctor_id=pht.tech_id)
	WHERE ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )
;


DROP VIEW IF EXISTS supplier_payments_view;
CREATE OR REPLACE VIEW supplier_payments_view AS
(
 SELECT inv.center_id, si.invoice_no, si.supplier_id, inv.supplier_invoice_id,
 si.invoice_date, si.po_no, inv.payment_id, pom.po_total, inv.status, ''
 AS debit_note_status, si.due_date, inv.account_group, ROUND (COALESCE(inv.amt +
  si.round_off + si.other_charges + si.cess_tax_amt + si.tcs_amount - si.discount - si.debit_amt,
 0::NUMERIC),2) AS final_amt, 'P' AS invoice_type, inv.consignment_status,
 inv.issue_id, si.cash_purchase
 FROM (
       SELECT s.center_id, si.supplier_invoice_id, si.status,
       si.consignment_stock, si.cash_purchase, s.account_group,
       SUM(sgd.billed_qty /sgd.grn_pkg_size * sgd.cost_price - sgd.discount + sgd.tax + sgd.item_ced - sgd.scheme_discount)::NUMERIC(15, 2) AS amt, si.status AS consignment_status,
       si.payment_id, 0 AS issue_id
       FROM store_invoice si
       JOIN store_grn_main sgm USING (supplier_invoice_id)
       JOIN store_grn_details sgd ON sgm.grn_no::text = sgd.grn_no::text
       JOIN stores s ON s.dept_id = sgm.store_id
       WHERE si.consignment_stock = FALSE
       GROUP BY si.supplier_invoice_id, si.status, si.consignment_stock,
       s.account_group, si.payment_id, si.supplier_id, si.cash_purchase, s.center_id

       UNION

       SELECT s.center_id, si.supplier_invoice_id, 'F' AS status,
       si.consignment_stock, si.cash_purchase, si.account_group,
       sci.amount AS amt,
       CASE
       WHEN si.consignment_stock = TRUE AND
       sci.status IS NOT NULL
       THEN sci.status
       ELSE 'F'::bpchar
       END AS consignment_status,
       sci.payment_id, sci.issue_id
       FROM store_consignment_invoice sci
       JOIN store_invoice si USING (supplier_invoice_id)
       JOIN store_grn_main sgm ON (sgm.supplier_invoice_id = si.supplier_invoice_id)
       JOIN stores s ON (s.dept_id = sgm.store_id)
       WHERE si.consignment_stock = TRUE AND sci.status IS
       NOT NULL) inv
 JOIN store_invoice si ON si.supplier_invoice_id::text =
 inv.supplier_invoice_id::text
 LEFT JOIN store_po_main pom ON pom.po_no::text = si.po_no::text
 AND pom.supplier_id::text = si.supplier_id::text
 WHERE inv.status = 'F'::bpchar AND inv.amt > 0::NUMERIC
 UNION

 SELECT debitamt.center_id, sdt.debit_note_no AS invoice_no, debitamt.supplier_id, 0
 AS supplier_invoice_id, sdt.debit_note_date AS invoice_date, '' AS
 po_no, sdt.payment_id, NULL AS po_total, sdt.status, sdt.status AS
 debit_note_status, NULL::unknown AS due_date,
 debitamt.account_group,
 debitamt.amount AS final_amt, 'PD' AS invoice_type,
 '' AS consignment_status, 0 AS issue_id, '' AS cash_purchase
 FROM
 (SELECT gdept.center_id, pdn.debit_note_no, pdn.payment_id, pdn.status,
  gdept.account_group,
  (0 - pdn.received_debit_amt) AS amount, sm.supplier_code AS
  supplier_id, pdn.status AS debit_note_status
  FROM store_grn_main pngm
  JOIN store_debit_note pdn ON pdn.debit_note_no::text =
  pngm.debit_note_no::text
  JOIN supplier_master sm ON sm.supplier_code::text =
  pdn.supplier_id::text
  JOIN stores gdept ON gdept.dept_id = pngm.store_id
  GROUP BY sm.supplier_code, pdn.debit_note_no,
  pdn.payment_id, pdn.received_debit_amt,
  pdn.status, gdept.account_group, gdept.center_id
  )debitamt
 JOIN store_debit_note sdt ON sdt.debit_note_no::text =
 debitamt.debit_note_no::text
 );

DROP VIEW IF EXISTS payee_names_view_for_voucher CASCADE;
CREATE VIEW payee_names_view_for_voucher AS

		SELECT misc_payee_name AS payee_name, misc_payee_name AS payee_id, 'A' AS status FROM misc_payees
	UNION
		SELECT doctor_name AS payee_name, doctor_id AS payee_id, status FROM doctors
	UNION
		SELECT referal_name AS payee_name, referal_no AS payee_id, status FROM referral
	UNION
		SELECT oh_name AS payee_name, oh_id AS payee_id, status FROM outhouse_master
	UNION
		SELECT supplier_name AS payee_name, supplier_code AS payee_id, status::character FROM supplier_master
;

DROP VIEW IF EXISTS payment_voucher_details_view;
CREATE OR REPLACE VIEW payment_voucher_details_view AS (SELECT b.visit_id, b.bill_no,
	coalesce(pr.mr_no,isr.incoming_visit_id) AS mr_no,
	chc.chargehead_name, pay.voucher_no, pay.posted_date, pay.description, pay.payment_type, pay.payee_name AS payee_id,
	coalesce(get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name), isr.patient_name) AS full_name, pay.amount, p.tax_amount,
	p.tds_amount, pnv.payee_name AS name,p.date,
	CASE WHEN cgc.chargegroup_id::text = 'DIA'::text THEN d.test_name
	WHEN cgc.chargegroup_id::text = 'SNP'::text THEN svr.service_name
	ELSE ''::character varying 	END AS activity_name, p.remarks,
		pm.payment_mode, p.payment_mode_id, ctm.card_type, p.card_type_id,
		p.bank, p.reference_no, p.amount AS voucher_amount, dac.disc_auth_name AS
		discount_auth_dr_name, dap.disc_auth_name AS discount_auth_pres_dr_name, daov.disc_auth_name AS
		overall_discount_auth_name, dar.disc_auth_name AS discount_auth_ref_name, dah.disc_auth_name AS
		discount_auth_hosp_name, bc.dr_discount_amt, bc.pres_dr_discount_amt, bc.ref_discount_amt,
		bc.hosp_discount_amt, bc.overall_discount_amt,
		COALESCE( (CASE WHEN pay.payment_type='D' and bc.charge_head='PKGPKG' THEN
			(select bac.conducted_datetime from bill_activity_charge bac where bac.doctor_payment_id=pay.payment_id)
		ELSE
			bc.conducted_datetime
		END) ,bc.posted_date) AS conducted_date, p.username
		FROM payments_details pay
		LEFT JOIN bill_charge bc USING (charge_id)
		LEFT JOIN bill b USING (bill_no)
		JOIN payments p USING (voucher_no)
		JOIN chargehead_constants chc ON chc.chargehead_id::text = bc.charge_head::text
		JOIN chargegroup_constants cgc ON chc.chargegroup_id::text = cgc.chargegroup_id::text
		JOIN payee_names_view_for_voucher pnv ON pnv.payee_id::text = pay.payee_name::text
		LEFT JOIN patient_registration pr ON b.visit_id::text = pr.patient_id::text
		JOIN payment_mode_master pm ON (p.payment_mode_id = pm.mode_id)
		LEFT JOIN card_type_master ctm ON (p.card_type_id = ctm.card_type_id)
		LEFT JOIN incoming_sample_registration isr ON (isr.billno=b.bill_no)
		LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
		LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
		LEFT JOIN diagnostics d ON d.test_id::text = bc.act_description_id::text
		LEFT JOIN services svr ON svr.service_id::text = bc.act_description_id::text
		LEFT JOIN discount_authorizer dac ON bc.discount_auth_dr = dac.disc_auth_id
		LEFT JOIN discount_authorizer dap ON bc.discount_auth_pres_dr = dap.disc_auth_id
		LEFT JOIN discount_authorizer dar ON bc.discount_auth_ref = dar.disc_auth_id
		LEFT JOIN discount_authorizer dah ON bc.discount_auth_hosp = dah.disc_auth_id
		LEFT JOIN discount_authorizer daov ON bc.overall_discount_auth = daov.disc_auth_id
		WHERE ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )
);


	 DROP VIEW IF EXISTS template_searching_view;

     CREATE OR REPLACE VIEW template_searching_view as
		(SELECT format_name, testformat_id, format_description,
		  (SELECT test_name FROM test_template_master
		    JOIN diagnostics USING(test_id) WHERE format_name = f.testformat_id LIMIT 1
		  ) AS used_in_test_name,
		  (SELECT test_id FROM test_template_master WHERE format_name = f.testformat_id LIMIT 1
		  ) AS used_in_test_id,
		 (select ddept_name from test_template_master join
		 diagnostics  d using(test_id)
		 join diagnostics_departments dd using (ddept_id) WHERE format_name = f.testformat_id LIMIT 1) AS dept_name
		  from test_format f
);


DROP VIEW IF EXISTS fixed_asset_report_view;
CREATE OR REPLACE VIEW fixed_asset_report_view AS
			(SELECT  fam.asset_id,
				medicine_name AS asset_name,
				dept_name,
				fam.installation_date,
				fam.asset_purchase_val,
				fam.asset_make,
				fam.asset_model,
				fam.asset_serial_no,
				cat.category AS asset_category,
				(sum(coalesce(amai.labor_cost,0))+sum(coalesce(amai.part_cost,0))) AS maint_cost,
				ams.next_maint_date,
				ama.maint_date,
				acm.complaint_type,
				acm.complaint_desc,
				ams.maint_id,
				ams.maint_frequency,
				ams.department AS maint_dept,
				(CASE WHEN acm.complaint_status =0 THEN 'Recorded'
					  WHEN acm.complaint_status =1 THEN 'Assigned'
					  WHEN acm.complaint_status =2 THEN 'Resolved'
				 	  WHEN acm.complaint_status =3 THEN 'Closed'
				 	  ELSE 'NONE'
				 END)  AS comp_status,
				 CASE WHEN ama.maint_date is null and (ams.next_maint_date < CURRENT_DATE) THEN 'overdue'
					  WHEN ama.maint_date is null and (ams.next_maint_date > CURRENT_DATE) THEN 'upcoming'
				 END AS schedule_status, hcm.center_name
			 FROM fixed_asset_master fam
			 LEFT JOIN  asset_maintenance_master ams ON (fam.asset_id = ams.asset_id and fam.asset_serial_no = ams.batch_no)
			 LEFT JOIN  asset_maintenance_activity ama ON (ama.asset_id = ams.asset_id and ama.batch_no = fam.asset_serial_no)
			 LEFT JOIN  assert_complaint_master acm ON (acm.asset_id = fam.asset_id and acm.batch_no = fam.asset_serial_no)
			 LEFT JOIN  asset_maintenance_activity_item amai USING (maint_activity_id)
			 LEFT JOIN  store_item_details sitd ON (fam.asset_id = sitd.medicine_id)
			 LEFT JOIN  stores s ON fam.asset_dept = s.dept_id
			 LEFT JOIN hospital_center_master hcm ON hcm.center_id = s.center_id
			 LEFT JOIN store_category_master cat ON (cat.category_id = sitd.med_category_id)
			 GROUP BY fam.asset_id, medicine_name, dept_name, fam.installation_date,
			 fam.asset_purchase_val,fam.asset_make, fam.asset_model,fam.asset_serial_no,cat.category,
			 amai.labor_cost, amai.part_cost, ams.next_maint_date, ama.maint_date,
			 acm.complaint_type, acm.complaint_desc, acm.complaint_status,ams.maint_id, ams.maint_frequency,
				ams.department,hcm.center_name );


DROP VIEW IF EXISTS prescription_lead_time_view cascade;
CREATE OR REPLACE VIEW prescription_lead_time_view AS
(SELECT  pat.patname,pat.name_local_language, dc.patient_id,
					dc.mr_no,
            		pmp.op_medicine_pres_id,
            		doc.doctor_name,
            		std.medicine_name,std.cust_item_code,
            		g.generic_name,
					pmp.medicine_quantity,
					pmp.issued_qty,
				   (CASE WHEN pmp.issued_qty = 0
				   		 THEN 'Not Sold'
				   		 WHEN pmp.issued_qty < pmp.medicine_quantity
                         THEN 'Partially Sold'
                         WHEN pmp.issued_qty >= pmp.medicine_quantity
                         THEN 'Fully Sold'
                    END) AS sold_status,
                    date(pp.prescribed_date) AS prescribed_date,
                    pp.prescribed_date AS prescribed_date_str,
					(SELECT s1.date_time
					FROM store_sales_main s1
                    WHERE sale_id = pmp.initial_sale_id)
                    AS initial_sale_date,
                    (SELECT s2.date_time
                    FROM store_sales_main s2
                    WHERE sale_id = pmp.final_sale_id)
                    AS final_sale_date,
                     CASE WHEN pp.status = 'P' AND pmp.expiry_date IS NOT NULL AND pmp.expiry_date < current_timestamp THEN pmp.expiry_date ELSE null END AS expiry_date
                    FROM patient_prescription pp
                    	JOIN doctor_consultation dc USING (consultation_id)
                    	JOIN patient_medicine_prescriptions pmp ON (pp.presc_type='Medicine' AND pp.store_item=true AND pp.patient_presc_id=pmp.op_medicine_pres_id)
						JOIN patientdetails pat ON (pat.patient_id = dc.patient_id)
						JOIN doctors doc ON dc.doctor_name = doc.doctor_id
						LEFT JOIN store_item_details std ON (pmp.medicine_id=std.medicine_id)
						LEFT JOIN generic_name g ON (pmp.generic_code=g.generic_code) );


DROP VIEW IF EXISTS operations_ordered_view cascade;
CREATE OR REPLACE VIEW operations_ordered_view AS
SELECT op.mr_no, op.patient_id, op.consultant_doctor, op.theatre_name, op.status,
	op.start_datetime, op.package_name, op.finalization_status, op.po_schedule_status, op.remarks,
	op.prescribed_id AS prescription_id, op.end_datetime, op.hrly, op.surgeon, op.anaesthetist, op.frompackage,
	op.common_order_id, op.prescribed_date, op.stock_reduced,  op.package_ref,
	op.department AS dept_id, op.operation_name AS operation_id,
	b.bill_type, b.status AS bill_status, b.payment_status,
	opm.operation_name AS operation,
	get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS patient_name, b.visit_type
FROM bed_operation_schedule op
	JOIN operation_master opm ON (opm.op_id = op.operation_name)
	JOIN department d USING(dept_id)
	JOIN patient_details pd ON op.mr_no = pd.mr_no
	LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
	LEFT JOIN bill_activity_charge bac ON
		(bac.activity_id=op.prescribed_id::varchar) AND bac.activity_code = 'OPE'
	LEFT JOIN bill_charge bc ON  bc.charge_id=bac.charge_id
	LEFT JOIN bill b ON b.bill_no=bc.bill_no
WHERE op.status != 'U' and op.status!='X' AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) );


-- Only one insert entry from the bill_charge_audit_log is included in the
-- view to avoid data overload. field_name == charge_id is used AS the filter
-- criteria just so that, it is always there. Any other field that is sure to
-- be captured AS part of the insert is AS good. (refer bug #19256)

DROP VIEW IF EXISTS bill_audit_view;
CREATE OR REPLACE VIEW bill_audit_view AS
((SELECT bal.log_id, 'bill_audit_log' AS base_table, bal.bill_no, NULL AS charge_id, NULL AS charge_head,
	NULL AS act_description,
	bal.user_name, bal.mod_time, bal.operation,
	'bill_'::text || bal.field_name::text AS field_name,
	bal.old_value, bal.new_value
	FROM bill_audit_log bal)
UNION ALL
(SELECT bcal.log_id, 'bill_charge_audit_log' AS base_table, bcal.bill_no, bcal.charge_id AS charge_id,
	bcal.charge_head AS charge_id, bc.act_description AS act_description,
	bcal.user_name, bcal.mod_time, bcal.operation,
	'bill_charge_'::text || bcal.field_name::text AS field_name,
	bcal.old_value, bcal.new_value
FROM bill_charge_audit_log bcal LEFT JOIN bill_charge bc ON bcal.charge_id = bc.charge_id
WHERE field_name = 'charge_id'));

DROP VIEW IF EXISTS contracts_view;
CREATE OR REPLACE VIEW contracts_view AS
	(SELECT  c.contract_id,
				c.contract_company,
				c.contract_start_date,
				c.contract_end_date,
				c.contract_renewal_date,
				'Expired Contract' AS status,
				c.contract_file_name,
				c.contract_value,
				c.contract_note,
				cm.contractor_name
		FROM contracts c
		LEFT JOIN contractor_master cm ON c.contractor_id = cm.contractor_id
		WHERE CURRENT_DATE > c.contract_renewal_date and c.contract_status='A'

  		UNION ALL

   		SELECT  c.contract_id,
   				c.contract_company,
   				c.contract_start_date,
    			c.contract_end_date,
    			c.contract_renewal_date,
    			'Coming up for Renewal' AS status,
    			c.contract_file_name,
				c.contract_value,
				c.contract_note,
				cm.contractor_name
    	FROM contracts c
    	LEFT JOIN contractor_master cm ON c.contractor_id = cm.contractor_id
    	WHERE  CURRENT_DATE <= c.contract_renewal_date and c.contract_status='A');


DROP VIEW IF EXISTS license_view ;
CREATE OR REPLACE VIEW license_view AS
	(SELECT
			l.license_id,
			l.license_desc,
			l.license_start_date,
    			l.license_end_date,
    	 		l.license_renewal_date,
    	 		'Expired License' AS status,
    	 		l.license_value,
    	 		l.license_note,
    	 		cm.contractor_name
    	FROM licenses l
    	LEFT JOIN contractor_master cm ON (l.contractor_id = cm.contractor_id)
    	WHERE CURRENT_DATE > l.license_renewal_date AND l.license_status='A'

    	UNION  ALL

        SELECT
		    l.license_id,
		    l.license_desc,
		    l.license_start_date,
		    l.license_end_date,
		    l.license_renewal_date,
		    'Coming up for Renewal' AS status,
		    l.license_value,
		l.license_note,
		cm.contractor_name
       FROM licenses l
       LEFT JOIN contractor_master cm ON (l.contractor_id = cm.contractor_id)
       WHERE CURRENT_DATE <= l.license_renewal_date AND l.license_status='A');


CREATE OR REPLACE VIEW mrd_counts AS
SELECT * FROM (
SELECT
   CASE WHEN requesting_dept like 'DEP%' THEN ( select dept_name from department where dept_id=requesting_dept )
   ELSE (select file_user_name from mrd_casefile_users where file_user_id::text=requesting_dept)
   END AS dept_or_user,
  date(request_date) AS date,
  count(*) AS pending_indents,0 AS  issued_files_count , 0 AS  returned_files_count
FROM mrd_casefile_attributes m1
WHERE indented ='Y'
GROUP BY  requesting_dept, date(request_date)
UNION ALL
SELECT
   CASE WHEN issued_to_dept is not null THEN (select dept_name from department where dept_id=issued_to_dept)
   ELSE (select file_user_name from mrd_casefile_users where file_user_id=issued_to_user)
   END AS dept_or_user,
  date(issued_on) AS date,
  0 AS pending_indents,count(*) AS  issued_files_count , 0 AS  returned_files_count
FROM mrd_casefile_issuelog m1
WHERE issued_on IS NOT NULL
GROUP BY issued_to_dept, issued_to_user, date(issued_on)
UNION ALL
SELECT
   CASE WHEN issued_to_dept is not null THEN (select dept_name from department where dept_id=issued_to_dept)
   ELSE (select file_user_name from mrd_casefile_users where file_user_id=issued_to_user)
   END AS dept_or_user,
   date(returned_on) AS date,
  0 AS pending_indents,0 AS  issued_files_count , count(*) AS returned_files_count
FROM  mrd_casefile_issuelog m1
WHERE returned_on IS NOT NULL
GROUP BY issued_to_dept, issued_to_user, date(returned_on)
) AS foo ;


DROP FUNCTION IF EXISTS addMissingPatient(varchar) CASCADE;
CREATE OR REPLACE FUNCTION addMissingPatient(mrno varchar) RETURNS void AS $$
BEGIN
 INSERT INTO patient_details (
        mr_no, patient_name, patient_gender,  patient_phone,salutation,last_name, expected_dob,
        patient_address, patient_city, patient_state,country,
        user_name, emr_access, portal_access, patient_category_id
)
VALUES ( mrno, 'Missing Data', 'F',
        '', 'SALU0001', '.',
        (substr(mrno,0,5)::numeric-22::numeric||'-01-01')::date,
        ' ',
        'CT0017',
        'ST0017', 'CM0001',
        'InstaAdmin','N','N','1'
);

update  mrd_casefile_attributes set created_date =(substr(mrno,0,5)||'-01-01')::date where mr_no =mrno ;
END;
$$ LANGUAGE plpgsql;


DROP FUNCTION IF EXISTS addMissingPatient_nocasefile(varchar) CASCADE;
CREATE OR REPLACE FUNCTION addMissingPatient_nocasefile(mrno varchar) RETURNS void AS $$
BEGIN
 INSERT INTO patient_details (
        mr_no, patient_name, patient_gender,  patient_phone,salutation,last_name, expected_dob,
        patient_address, patient_city, patient_state,country,
        user_name, emr_access, portal_access, patient_category_id
)
VALUES ( mrno, 'Missing Data', 'F',
        '', 'SALU0001', '.',
        ('2011-01-01')::date,
        ' ',
        'CT0017',
        'ST0017', 'CM0001',
        'InstaAdmin','N','N','1'
);

END;
$$ LANGUAGE plpgsql;


DROP VIEW IF EXISTS masters_deprtmentwise_counts ;
CREATE OR REPLACE VIEW masters_deprtmentwise_counts AS
	SELECT COUNT(test_id),ddept_id AS dept_id,'Diag' AS type from diagnostics
	GROUP BY ddept_id
	UNION
	SELECT COUNT(service_id),serv_dept_id::text AS dept_id,'Service' AS type from services
	GROUP BY serv_dept_id
	UNION
	SELECT COUNT(eq_id),dept_id AS dept_id,'Equipment' from equipment_master
	GROUP BY dept_id
	UNION
	SELECT COUNT(op_id),dept_id AS dept_id,'Operation' AS type from operation_master
	GROUP BY dept_id
	UNION
	SELECT COUNT(charge_name),'Other Charges' AS dept_id,'Other Charges' AS type from common_charges_master
	GROUP BY dept_id
	UNION
	SELECT COUNT(package_name),'Package' AS dept_id,'Package' AS type from packages
	GROUP BY dept_id;

-- store consumption view used in Stock Reorder screen

DROP VIEW IF EXISTS store_consumption_view CASCADE;
CREATE OR REPLACE VIEW store_consumption_view AS
SELECT dept_id,medicine_id,sum(qty) AS qty,date_time
FROM (
		  SELECT
		  	store_id AS dept_id,medicine_id,(s.quantity) AS qty,
		    date_trunc('day',sm.sale_date) AS date_time
		  FROM store_sales_details s
		  JOIN store_sales_main sm USING (sale_id)

		  UNION ALL

		  SELECT dept_from AS dept_id,medicine_id,
            (sid.qty-sid.return_qty) AS qty,
            date_trunc('day', sim.date_time) AS date_time
          FROM stock_issue_details sid
          JOIN stock_issue_main sim USING(user_issue_no)
          WHERE qty>0

	 	  UNION ALL

	       SELECT store_from AS dept_id, medicine_id, SUM(s.qty-s.qty_rejected) AS transfer_out_qty,
			date_trunc('day',sm.date_time) AS date_time
		   FROM store_transfer_details s
		   JOIN store_transfer_main sm USING(transfer_no)
		   GROUP BY  store_from, medicine_id,date_time

		  UNION ALL

			SELECT store_to AS dept_id, medicine_id, (0::numeric-SUM(s.qty-s.qty_rejected)) AS transfer_in_qty,
			date_trunc('day',sm.date_time) AS date_time
			FROM store_transfer_details s
			JOIN store_transfer_main sm USING(transfer_no)
			GROUP BY  store_to, medicine_id,date_time
) AS all_stock_changes
GROUP BY dept_id,medicine_id, date_time;


DROP VIEW IF EXISTS ESI_bill_charge_view CASCADE;

CREATE OR REPLACE VIEW ESI_bill_charge_view AS
(SELECT bc.charge_id, bc.username, bc.bill_no,
			CASE WHEN b.bill_type = 'C' THEN 'Bill Later'
				WHEN (b.bill_type = 'M' OR b.bill_type = 'R') THEN 'Pharmacy'
				ELSE 'Bill Now'
			END AS bill_type,
			CASE WHEN b.status = 'A' THEN 'Open' WHEN b.status = 'F' THEN 'Finalized'
				WHEN b.status = 'S' THEN 'Settled' WHEN b.status = 'C' THEN 'Closed'
				ELSE 'Cancelled'
			END AS bill_status,
			CASE WHEN activity_conducted ='Y' THEN 'Yes'
				WHEN activity_conducted ='N' THEN 'No'
				ELSE 'Not Applicable'
			END AS activity_conducted,
			DATE(b.finalized_date) AS finalized_date, DATE(b.finalized_date) AS fromDate,
			DATE(b.finalized_date) AS toDate, DATE(b.finalized_date) AS date,
			DATE(b.open_date) AS open_date,
			DATE(b.closed_date) AS closed_date, DATE(bc.posted_date) AS posted_date,
			COALESCE (pr.mr_no, prc.customer_id, isr.incoming_visit_id)
				AS cust_id,
			COALESCE (get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name), prc.customer_name, isr.patient_name)
				AS patient_full_name,
			CASE WHEN pd.patient_gender='M' THEN 'Male' WHEN pd.patient_gender='F' THEN 'Female' ELSE 'Other' END
				AS patient_gender,
			b.visit_id, vtn.visit_type_name AS visit_type, DATE(pr.reg_date) AS reg_date, pcm.category_name,
			pd.mr_no, c.city_name, cm.country_name, stm.state_name,
			CASE WHEN pd.patient_area = '' OR pd.patient_area IS NULL THEN NULL ELSE pd.patient_area
			END AS patient_area,
			CASE WHEN pr.status='A' THEN 'Active' WHEN pr.status='I' THEN 'Inactive' ELSE null
			END AS patient_status, pr.op_type, otn.op_type_name,

			dep.dept_name, tdep.dept_name AS treating_dept, dum.unit_name AS unit_name,
			doc.doctor_name, cdoc.doctor_name AS conducting_doctor,
			coalesce(rdoc.doctor_name, ref.referal_name) AS referer,
			wn.ward_name, od.org_name AS rate_plan, tm.tpa_name AS tpaname,
			cgc.chargegroup_name, chc.chargehead_name, bahc.account_head_name AS ac_head, agm.account_group_name,

			bc.act_description, bc.act_unit AS units,
			bc.act_rate::NUMERIC AS rate, bc.act_quantity::NUMERIC AS quantity, bc.orig_rate::NUMERIC,
			orig_rate*act_quantity AS orig_amount,
			(act_rate-orig_rate)*act_quantity AS diff,
			(act_rate-orig_rate) AS rate_diff,
			bc.discount::NUMERIC, (bc.discount+bc.amount)::NUMERIC AS amount,
			bc.amount::NUMERIC AS net_amount,
			bc.doctor_amount::NUMERIC, bc.referal_amount, bc.prescribing_dr_amount,
			bc.insurance_claim_amount::NUMERIC,

 			bc.dr_discount_amt,
 			bc.pres_dr_discount_amt,
 			bc.ref_discount_amt,
 			bc.hosp_discount_amt ,
			dac.disc_auth_name AS discount_auth_dr_name,
			dap.disc_auth_name AS discount_auth_pres_dr_name,
			daov.disc_auth_name AS overall_discount_auth_name,
			dar.disc_auth_name AS discount_auth_ref_name,
			dah.disc_auth_name AS discount_auth_hosp_name,

			pd.patient_address, pd.family_id,
			pd.custom_field1, pd.custom_field2, pd.custom_field3, pd.custom_field6, pd.custom_field7,
			pd.custom_field8, pd.custom_field9, pd.custom_field10

		FROM bill_charge bc
			JOIN bill b USING (bill_no)
			JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head)
			JOIN chargegroup_constants cgc ON (cgc.chargegroup_id = chc.chargegroup_id)
			JOIN bill_account_heads bahc ON (bahc.account_head_id = chc.account_head_id)
			JOIN visit_type_names vtn ON (vtn.visit_type = b.visit_type)
			LEFT JOIN account_group_master agm  ON (agm.account_group_id = bc.account_group)
			LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
			LEFT JOIN op_type_names otn ON (otn.op_type = pr.op_type)
			LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
			LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
			LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
			LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation)
			LEFT JOIN city c ON (c.city_id = pd.patient_city)
			LEFT JOIN country_master cm ON (pd.country = cm.country_id)
			LEFT JOIN state_master stm ON (pd.patient_state = stm.state_id)
			LEFT JOIN patient_category_master pcm ON (pcm.category_id = pr.patient_category_id)
			LEFT JOIN department dep ON (dep.dept_id = pr.dept_name)
			LEFT JOIN treating_departments_view tdep ON (tdep.dept_id = bc.act_department_id)
			LEFT JOIN doctors doc ON (doc.doctor_id = pr.doctor)
			LEFT JOIN doctors cdoc ON(cdoc.doctor_id=bc.payee_doctor_id)
			LEFT JOIN doctors rdoc ON (rdoc.doctor_id = pr.reference_docto_id )
			LEFT JOIN referral ref ON (ref.referal_no = pr.reference_docto_id )
			LEFT JOIN admission adm ON (adm.patient_id = pr.patient_id)
			LEFT JOIN bed_names bn ON (bn.bed_id = adm.bed_id)
			LEFT JOIN ward_names wn ON (wn.ward_no = bn.ward_no)
			LEFT JOIN organization_details od ON (od.org_id = pr.org_id)
			LEFT JOIN tpa_master tm ON (tm.tpa_id = pr.primary_sponsor_id)
			LEFT JOIN dept_unit_master dum ON (dum.unit_id = pr.unit_id)
			LEFT OUTER JOIN discount_authorizer dac ON (bc.discount_auth_dr = dac.disc_auth_id)
			LEFT OUTER JOIN discount_authorizer dap ON (bc.discount_auth_pres_dr = dap.disc_auth_id)
			LEFT OUTER JOIN discount_authorizer dar ON (bc.discount_auth_ref = dar.disc_auth_id)
			LEFT OUTER JOIN discount_authorizer dah ON (bc.discount_auth_hosp = dah.disc_auth_id)
			LEFT OUTER JOIN discount_authorizer daov ON (bc.overall_discount_auth = daov.disc_auth_id)

		WHERE b.status in ('F','S') AND bc.status != 'X' and b.bill_type!='P' AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )
order by tm.tpa_name,  pd.mr_no, date(b.finalized_date), b.bill_no);


DROP FUNCTION IF EXISTS update_diag_observation() CASCADE;
CREATE OR REPLACE function update_diag_observation() RETURNS TRIGGER AS $BODY$
DECLARE
	obsId integer;
	editObsId integer;
	rec Record;
BEGIN
	IF ((SELECT true from modules_activated WHERE module_id = 'mod_mrd' AND activation_status = 'Y') 
	AND COALESCE(NEW.code_type, '') != '' AND COALESCE(NEW.result_code, '') != '' AND NEW.test_detail_status != 'A') THEN
		-- check if a row for the observation already exists
		SELECT observation_id FROM mrd_observations ob
			JOIN bill_charge bc USING (charge_id)
			JOIN bill_activity_charge bac ON (bc.charge_id = bac.charge_id AND bac.activity_code = 'DIA')
		WHERE activity_id = NEW.prescribed_id::text
			AND ob.code = NEW.result_code LIMIT 1
		INTO obsId;

		IF obsId IS NULL THEN
			-- insert new observation (observation_id is auto generated by a sequence)
			INSERT INTO mrd_observations (charge_id, observation_type, code, value, value_type,value_editable)
			SELECT bac.charge_id, NEW.code_type, NEW.result_code, NEW.report_value, NEW.units, 'Y'
			FROM bill_activity_charge bac
			WHERE bac.activity_id = NEW.prescribed_id::text AND bac.activity_code = 'DIA';
		ELSE
			-- update the existing observation, maybe the value has changed.
			UPDATE mrd_observations SET observation_type=NEW.code_type, code=NEW.result_code,
				value=NEW.report_value, value_type=NEW.units, value_editable='Y'
			WHERE observation_id = obsId;
		END IF;
	END IF;
	RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;


DROP TRIGGER IF EXISTS update_diag_observation ON test_details CASCADE;
CREATE TRIGGER update_diag_observation
  AFTER INSERT OR UPDATE
  ON test_details
  FOR EACH ROW
  EXECUTE PROCEDURE update_diag_observation();

DROP FUNCTION IF EXISTS insert_diag_observation() CASCADE;
CREATE OR REPLACE function insert_diag_observation() RETURNS TRIGGER AS $BODY$
BEGIN
-- insert observations from test_results_master for the prescribed test
-- (observation_id is auto generated by a sequence)
IF ((SELECT true from modules_activated WHERE module_id = 'mod_mrd' AND activation_status = 'Y') AND NEW.activity_code = 'DIA') THEN
	INSERT INTO mrd_observations (charge_id, observation_type, code, value, value_type,value_editable)
		SELECT bac.charge_id, trm.code_type, trm.result_code, '', trm.units, 'Y'
		FROM test_results_master trm
			JOIN tests_prescribed tp ON (trm.test_id=tp.test_id)
			JOIN bill_activity_charge bac ON (bac.activity_id=tp.prescribed_id::text)
			LEFT JOIN patient_registration pr ON (tp.pat_id=pr.patient_id)
			LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id=tp.pat_id)
			LEFT JOIN test_results_center trc ON (trm.resultlabel_id = trc.resultlabel_id)
		WHERE tp.prescribed_id = NEW.activity_id::integer AND bac.activity_code = 'DIA'
		 	AND coalesce(trm.code_type, '') != ''
		 	AND coalesce(trm.result_code, '') != ''
		 	AND (trc.center_id = 0 OR trc.center_id = coalesce(pr.center_id, isr.center_id)) ORDER BY display_order;
END IF;
RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS insert_diag_observation ON bill_activity_charge CASCADE;
CREATE TRIGGER insert_diag_observation
  AFTER INSERT
  ON bill_activity_charge
  FOR EACH ROW
  EXECUTE PROCEDURE insert_diag_observation();

DROP FUNCTION IF EXISTS insert_service_observation() CASCADE;
CREATE OR REPLACE function insert_service_observation() RETURNS TRIGGER AS $BODY$
	DECLARE
	tooth_num_system character(1);
	tooth_number_required character(1);
BEGIN
-- (observation_id is auto generated by a sequence)
   SELECT tooth_numbering_system INTO tooth_num_system FROM generic_preferences;
   SELECT tooth_num_required into tooth_number_required
   FROM services s
   	JOIN services_prescribed sp ON (sp.service_id=s.service_id)
   WHERE NEW.activity_code = 'SER' AND prescription_id::text=NEW.activity_id;

IF NEW.activity_code = 'SER' AND tooth_num_system = 'U' AND tooth_number_required = 'Y' THEN
	INSERT INTO mrd_observations (charge_id, observation_type, code, value, value_type, value_editable)
		SELECT bac.charge_id, 'Universal Dental', regexp_split_to_table(sp.tooth_unv_number, E','), '', '', 'Y'
		FROM services s
			JOIN bill_activity_charge bac ON (bac.act_description_id = s.service_id)
			JOIN bill_charge bc ON (bac.charge_id=bc.charge_id)
			JOIN services_prescribed sp ON (bac.activity_id=sp.prescription_id::text)
		WHERE sp.prescription_id::text = NEW.activity_id AND bac.activity_code = 'SER';
END IF;
RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS insert_service_observation ON bill_activity_charge CASCADE;
CREATE TRIGGER insert_service_observation
  AFTER INSERT
  ON bill_activity_charge
  FOR EACH ROW
  EXECUTE PROCEDURE insert_service_observation();

DROP VIEW IF EXISTS store_stk_trnsfr_view;
CREATE VIEW store_stk_trnsfr_view AS
 SELECT s.medicine_id, s.batch_no, sum(s.qty) AS qty_consumed, date(sm.date_time) AS txndate, sm.store_from, sm.store_to
   FROM store_transfer_details s
   JOIN store_transfer_main sm USING (transfer_no)
  GROUP BY s.medicine_id, s.batch_no, date(sm.date_time), sm.store_from, sm.store_to;

 DROP VIEW IF EXISTS store_stk_issue_view;
CREATE VIEW store_stk_issue_view AS
SELECT sim.dept_from AS dept_id, sid.medicine_id, sid.batch_no, sum(sid.qty) AS qty, date(sim.date_time) AS txndate
   FROM stock_issue_details sid
   JOIN stock_issue_main sim USING (user_issue_no)
  GROUP BY sid.medicine_id, sid.batch_no, date(sim.date_time), sim.dept_from;


-- View to fetch claim submission batch (latest)
DROP VIEW IF EXISTS claim_submission_batch_view CASCADE;
CREATE VIEW claim_submission_batch_view AS
SELECT ic.claim_id, isb.*
   FROM insurance_claim ic
   JOIN insurance_submission_batch isb ON (isb.submission_batch_id = ic.last_submission_batch_id AND isb.is_resubmission = 'N')
UNION ALL
SELECT cs.claim_id, isb.*
	FROM insurance_claim ic
   JOIN (SELECT claim_id, max(submission_batch_id) AS resubmission_batch_id,count(submission_batch_id) as resubmission_count
	FROM claim_submissions GROUP by claim_id) cs ON (ic.claim_id=cs.claim_id)
   JOIN insurance_submission_batch isb ON (isb.submission_batch_id = cs.resubmission_batch_id)
;

--View to fetch all remittance received dates and payment references of insured bills.
DROP VIEW IF EXISTS all_insurance_remittance_details_view CASCADE;
CREATE OR REPLACE VIEW all_insurance_remittance_details_view AS
SELECT
   distinct(bill_no), payment_reference, remittance_id, received_date AS payment_recd_date, ir.is_recovery,ir.reference_no
   FROM insurance_remittance ir
   JOIN insurance_payment_allocation USING (remittance_id)
   JOIN bill_charge USING(charge_id)
UNION ALL
SELECT
	bill_no, payment_reference, remittance_id, received_date AS payment_recd_date, ir.is_recovery,ir.reference_no
   FROM insurance_remittance ir
   JOIN insurance_remittance_details USING (remittance_id)
   JOIN bill_claim USING(claim_id)
;

-- View to fetch the latest remittance received date and payment reference of insured bills.
DROP VIEW IF EXISTS insurance_remittance_details_view CASCADE;
CREATE OR REPLACE VIEW insurance_remittance_details_view AS
SELECT bill_no, payment_reference, max(payment_recd_date) AS payment_recd_date,reference_no
FROM all_insurance_remittance_details_view GROUP BY bill_no, payment_reference,reference_no;


--Usage :
-- getItemCodesForCodeType('code_type_name') returns all codes of Code Type - 'code_type_name'
-- getItemCodesForCodeType('code_type_name', 'patient_type') returns
--   all codes of Code Type - 'code_type_name' and applicable for OP/IP Patients - 'patient_type' (Used for drg_codes_master)
-- getItemCodesForCodeType('*') returns codes of all supported Code Types in the mrd_supported_code_types table

DROP FUNCTION IF EXISTS getItemCodesForCodeType (varchar) CASCADE;
DROP FUNCTION IF EXISTS getItemCodesForCodeType (varchar, varchar) CASCADE;
DROP TYPE IF EXISTS item_code_res_type CASCADE;
CREATE TYPE item_code_res_type AS (code varchar, code_desc varchar, code_type varchar , status varchar, mrd_code_id varchar);

CREATE OR REPLACE FUNCTION getItemCodesForCodeType (varchar, varchar)
RETURNS SETOF item_code_res_type AS
$BODY$
DECLARE
	r  item_code_res_type ;
	tabName varchar;
	codeName varchar;
    descName varchar;
    tempCodeType varchar;
    tempall item_code_res_type;
    patientType varchar;
    status varchar;
    mrdCodeId varchar;
BEGIN
	IF $1 = '*' THEN
	   FOR tempCodeType IN
			(SELECT DISTINCT code_type FROM
				mrd_supported_code_types )
		LOOP
			FOR tempall IN
			 SELECT * FROM getItemCodesForCodeType(tempCodeType)
			LOOP
			 RETURN NEXT tempall;
			END LOOP;
		END LOOP;
		return;
	END IF;

	SELECT code_master_table INTO tabName  FROM mrd_supported_code_types  WHERE CODE_TYPE = $1;
	IF $1 = 'Drug' OR $1 = 'Drug HAAD' OR $1 = 'Drug DHA'
        THEN
            codeName := 'item_code';
            descName := 'null';
            patientType := ' ';
            status := quote_literal('A');
            mrdCodeId := 'null';
        ELSIF $1 = 'Encounter Type'
		THEN
            codeName := 'encounter_type_id';
            descName := 'encounter_type_desc';
            patientType := ' ';
            status := 'status';
            mrdCodeId := 'null';
        ELSIF $1 = 'IR-DRG'
		THEN
            codeName := 'drg_code';
            descName := 'drg_description';
            patientType := ' AND upper(patient_type) = ' || quote_literal(upper($2)) ;
            status := 'status';
            mrdCodeId := 'null';
        ELSIF $1 = 'ICD' OR $1 = 'ICD9' OR $1 = 'ICD10' OR $1 = 'CPT'
        THEN
			codeName := 'code';
            descName := 'code_desc';
            patientType := ' ';
            status := 'status';
            mrdCodeId := 'mrd_code_id';
        ELSE
            codeName := 'code';
            descName := 'code_desc';
            patientType := ' ';
            status := 'status';
            mrdCodeId := 'null';
	END IF;

	IF tabName  IS NULL
		THEN
		return ;
	END IF;
	FOR r IN
	 EXECUTE 'SELECT '||  codeName || ' , ' ||  descName || ' , ' || quote_literal($1)|| ',' || status || ' , ' || mrdCodeId ||
	  '  FROM ' || tabName || ' WHERE code_type = ' || quote_literal($1) || '' || patientType || ''
	        LOOP
	                RETURN NEXT r;
	        END LOOP;
	return;

END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE
COST 100;
;

CREATE OR REPLACE FUNCTION getItemCodesForCodeType (varchar)
RETURNS SETOF item_code_res_type AS $$
DECLARE
	codesdata item_code_res_type;
	patientType varchar;
BEGIN
   patientType := '';
   FOR codesdata IN
	SELECT * FROM getItemCodesForCodeType($1, patientType)
   LOOP
   RETURN NEXT codesdata;
   END LOOP;
END;
$$ LANGUAGE plpgsql;

DROP VIEW IF EXISTS diagnostics_audit_log_view;
CREATE OR REPLACE VIEW diagnostics_audit_log_view AS
	(SELECT d.test_name,dal.*

		FROM diagnostics_audit_log dal
		JOIN diagnostics d using(test_id)
	);

DROP VIEW IF EXISTS diagnostic_charges_audit_log_view;
CREATE OR REPLACE VIEW diagnostic_charges_audit_log_view AS
	(SELECT d.test_name,dcal.*

		FROM diagnostic_charges_audit_log dcal
		JOIN diagnostics d using(test_id)
	);

DROP VIEW IF EXISTS services_audit_log_view;
CREATE OR REPLACE VIEW services_audit_log_view AS
	(SELECT s.service_name,sal.*
		FROM services_audit_log sal
		JOIN services s using(service_id)
	);

DROP VIEW IF EXISTS service_master_charges_audit_log_view;
CREATE OR REPLACE VIEW service_master_charges_audit_log_view AS
	(SELECT s.service_name,scal.*

		FROM service_master_charges_audit_log scal
		JOIN services s using(service_id)
	);

DROP VIEW IF EXISTS operation_master_audit_log_view;
CREATE OR REPLACE VIEW operation_master_audit_log_view AS
	(SELECT o.operation_name,oal.*
		FROM operation_master_audit_log oal
		JOIN operation_master o using(op_id)
	);

DROP VIEW IF EXISTS operation_charges_audit_log_view;
CREATE OR REPLACE VIEW operation_charges_audit_log_view AS
	(SELECT o.operation_name,ocal.*

		FROM operation_charges_audit_log ocal
		JOIN operation_master o using(op_id)
	);

DROP VIEW IF EXISTS all_receipts_audit_view;
CREATE OR REPLACE VIEW all_receipts_audit_view AS
SELECT  log_id, 'receipts_audit_log'::text AS base_table, user_name, mod_time, operation,
  	field_name, old_value, new_value, receipt_id AS receipt_no, receipt_id AS deposit_no
FROM receipts_audit_log;

--
-- Total of all bill and related amounts for the encounter.
-- Encounter => One main visit, and all its children Followup visits
-- TODO: bill_receipts max mod time? Let's leave this alone, the co-pay is not going to really
-- change once it is set. If we update mod_time in bill, it will interfere with Tally.
-- bill_charge max mod_time is not really necessary, AS once the bill is finalized, it cannot
-- be modified, and the bill mod_time will be updated ON finalization.
--
DROP VIEW IF EXISTS export_patient_details_bill_view CASCADE;
DROP VIEW IF EXISTS encounter_bill_totals_view CASCADE;
CREATE OR REPLACE VIEW encounter_bill_totals_view AS
SELECT pr.main_visit_id AS visit_id,
	max(b.bill_no) AS bill_no, max(b.finalized_date) AS finalized_date, max(open_date) AS open_date,
	max(b.mod_time) AS mod_time,
	max(b.username) AS opened_by,
	sum(total_amount) AS total_amount, sum(total_discount) AS total_discount, sum(total_claim) AS total_claim,
	sum(total_receipts) AS total_receipts, sum(primary_total_sponsor_receipts) AS primary_total_sponsor_receipts
FROM bill b
	JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
GROUP BY pr.main_visit_id;

--
-- This is for XML export of all encounters
--
DROP VIEW IF EXISTS export_patient_details_view CASCADE;
CREATE OR REPLACE VIEW export_patient_details_view AS
SELECT pr.mr_no, pr.patient_id, pr.visit_type, d.dept_name,
	sm.salutation, pd.patient_name AS first_name, pd.middle_name, pd.last_name,
	coalesce(pd.expected_dob,dateofbirth) AS dob, get_patient_age(pd.dateofbirth, pd.expected_dob) AS age,
	get_patient_age_in(pd.dateofbirth, pd.expected_dob) AS agein, pd.patient_gender,
	pd.custom_list1_value AS nationality,
	ci.city_name, st.state_name, cm.country_name, pd.patient_area, pd.patient_address, pd.patient_phone,
	pd.patient_phone2, pd.email_id, pd.next_of_kin_relation,
	pcm.category_name, pr.relation, pd.government_identifier,
	pr.patient_careof_address, pr.patient_care_oftext,
	pr.reg_date+pr.reg_time AS reg_date, pr.discharge_date+pr.discharge_time AS discharge_date,
	ipm.plan_id, ipm.plan_name, icm.category_id, icm.category_name AS plan_type, org_name, bed_type,
	pr.primary_insurance_approval, tpa_name, pr.prior_auth_id,pr.prior_auth_mode_id,
	ct.consultation_type, dc.visited_date AS consultation_date,
	ppd.policy_holder_name, ppd.patient_relationship, ppd.member_id, ppd.policy_validity_start,
	ppd.policy_validity_end, hic.insurance_co_code,
	GREATEST(ebv.mod_time, pd.mod_time, pr.mod_time) AS max_mod_time,
	ebv.*
FROM patient_registration pr
	JOIN patient_details pd USING (mr_no)
	LEFT JOIN salutation_master sm ON sm.salutation_id=pd.salutation
	LEFT JOIN country_master cm ON cm.country_id=pd.country
	LEFT JOIN city ci ON pd.patient_city::text = ci.city_id::text
	LEFT JOIN state_master st ON pd.patient_state::text = st.state_id::text
	LEFT JOIN patient_category_master pcm ON pcm.category_id = pr.patient_category_id
	LEFT JOIN insurance_plan_main ipm using(plan_id)
	LEFT JOIN insurance_category_master icm ON icm.category_id =ipm.category_id
	LEFT JOIN organization_details odet ON odet.org_id=pr.org_id
	LEFT JOIN tpa_master tpa ON tpa.tpa_id=pr.primary_sponsor_id
	LEFT JOIN insurance_company_master ic ON (ic.insurance_co_id = icm.insurance_co_id)
	LEFT JOIN hospital_center_master hcm ON(hcm.center_id=pr.center_id)
	LEFT JOIN ha_ins_company_code hic ON(hic.insurance_co_id=ic.insurance_co_id AND hcm.health_authority=hic.health_authority)
	LEFT JOIN department d ON d.dept_id=pr.dept_name
	LEFT JOIN doctor_consultation dc ON (pr.doctor = dc.doctor_name AND pr.patient_id = dc.patient_id)
	LEFT JOIN consultation_types ct ON (ct.consultation_type_id::text = dc.head)
	LEFT JOIN patient_policy_details ppd USING (patient_policy_id)
	JOIN encounter_bill_totals_view ebv ON pr.patient_id = ebv.visit_id
WHERE pr.op_type != 'F' OR pr.visit_type = 'i' AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) );

DROP FUNCTION IF EXISTS set_mod_time() CASCADE;
CREATE OR REPLACE FUNCTION set_mod_time() RETURNS TRIGGER AS $BODY$
BEGIN
	NEW.mod_time = current_timestamp;
	return NEW;
END;
$BODY$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS set_patient_registration_mod_time ON patient_registration CASCADE;
CREATE TRIGGER set_patient_registration_mod_time
  BEFORE UPDATE ON patient_registration
  FOR EACH ROW
EXECUTE PROCEDURE set_mod_time();

DROP TRIGGER IF EXISTS set_patient_details_mod_time ON patient_details CASCADE;
CREATE TRIGGER set_patient_details_mod_time
  BEFORE UPDATE ON patient_details
  FOR EACH ROW
EXECUTE PROCEDURE set_mod_time();



--
-- Convenience views for bill summary for a patient
--
DROP VIEW IF EXISTS bill_summary_view;
CREATE VIEW bill_summary_view AS
SELECT pr.mr_no, b.visit_id, pr.main_visit_id AS main, pr.reg_date, op_type AS ot,
	pr.doctor, b.bill_no, b.account_group AS ag, date(b.open_date) AS open_date, b.status AS st
FROM bill b
	JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
WHERE b.status != 'X'
ORDER BY mr_no, reg_date, bill_no;

--
-- Convenience views for charge summary for a patient
--
DROP VIEW IF EXISTS charges_summary_view;
CREATE VIEW charges_summary_view AS
SELECT pr.mr_no, b.visit_id, b.bill_no, b.status AS sts, b.is_tpa AS tpa, bc.charge_head AS head,
	date(posted_date) AS date, substr(bc.act_description,1,25) AS description,
	bc.status AS ch_sts, bc.amount
FROM bill_charge bc
	JOIN bill b USING (bill_no)
	JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
WHERE b.status != 'X' AND bc.status != 'X'
ORDER BY pr.reg_date, b.bill_no;


DROP VIEW IF EXISTS patient_section_field_values_view CASCADE;
CREATE OR REPLACE VIEW patient_section_field_values_view AS
SELECT psd.mr_no, psd.patient_id, psd.section_item_id, psd.generic_form_id, psd.section_status, psd.section_id, psf.form_id,
    psd.item_type, psf.form_type, psd.section_detail_id, psd.finalized, sm.linked_to,
    sfd.field_id, po.option_id, o.option_value, po.option_remarks, v.date_time, v.date,
    o.display_order AS option_display_order, sfd.field_name, field_type,
    allow_others, allow_normal, normal_text, section_title,
    sfd.display_order AS field_display_order, 
    CASE WHEN sfd.field_type in ('dropdown', 'checkbox') THEN coalesce(po.available, 'N') 
    	else 'Y' END AS available, 
    img.coordinate_x, img.coordinate_y, img.marker_id, notes, 
    v.field_detail_id, v.field_remarks, v.image_id, img.marker_detail_id
     
FROM patient_section_details psd
    JOIN patient_section_forms psf USING  (section_detail_id)
    JOIN section_master sm USING (section_id)
    JOIN section_field_desc sfd ON (sm.section_id=sfd.section_id)
    JOIN patient_section_fields v ON (psd.section_detail_id=v.section_detail_id and v.field_id=sfd.field_id)
    LEFT JOIN patient_section_options po ON (v.field_detail_id=po.field_detail_id)
    LEFT JOIN patient_section_image_details img ON (v.field_detail_id=img.field_detail_id)
    LEFT JOIN section_field_options o USING (option_id)
;

DROP VIEW IF EXISTS patient_section_field_values_for_print CASCADE;
CREATE OR REPLACE VIEW patient_section_field_values_for_print AS
SELECT psd.mr_no, psd.patient_id, psd.section_item_id, psd.generic_form_id, psd.section_status, psd.section_id, psf.form_id,
    psd.item_type, psf.form_type, psd.section_detail_id, psd.finalized, sm.linked_to,
    sfd.field_id, po.option_id, o.option_value, po.option_remarks, v.date_time, v.date,
    o.display_order AS option_display_order, sfd.field_name, field_type,
    allow_others, allow_normal, normal_text, section_title,
    sfd.display_order AS field_display_order, 
    CASE WHEN sfd.field_type in ('dropdown', 'checkbox') THEN coalesce(po.available, 'N') 
    	else 'Y' END AS available, 
    img.coordinate_x, img.coordinate_y, img.marker_id, notes, 
    v.field_detail_id, v.field_remarks, v.image_id, img.marker_detail_id, 
    CASE WHEN field_type in ('text', 'wide text') THEN coalesce(v.field_remarks, '') != ''
    	when field_type in ('dropdown', 'checkbox') THEN coalesce(po.available, 'N') = 'Y'
    	when field_type = 'date' THEN v.date is not null
    	when field_type = 'datetime' THEN v.date_time is not null
    	when field_type = 'image' THEN img.marker_id is not null END value_found

FROM  patient_section_details psd
    JOIN patient_section_forms psf USING  (section_detail_id)
    JOIN section_master sm USING (section_id)
    JOIN section_field_desc sfd ON (sm.section_id=sfd.section_id)
    JOIN patient_section_fields v ON (psd.section_detail_id=v.section_detail_id and v.field_id=sfd.field_id)
    LEFT JOIN patient_section_options po ON (v.field_detail_id=po.field_detail_id)
    LEFT JOIN patient_section_image_details img ON (v.field_detail_id=img.field_detail_id)
    LEFT JOIN section_field_options o USING (option_id)
;

-- trigger for inserting/updating the vitals readings in mrd_observation table
DROP FUNCTION IF EXISTS update_diag_observation_for_vitals() CASCADE;
CREATE OR REPLACE function update_diag_observation_for_vitals() RETURNS TRIGGER AS $BODY$
DECLARE
	record_found boolean;
	patientid character varying;
	obs_type character varying;
	obs_code character varying;
	param_uom_value character varying;
	charges integer;
BEGIN
	IF TG_OP = 'DELETE' THEN
		SELECT patient_id, true FROM visit_vitals vv
			WHERE vv.vital_reading_id=OLD.vital_reading_id ORDER BY date_time desc limit 1 INTO patientid, record_found;

		SELECT observation_type, observation_code, param_uom FROM vital_parameter_master
			WHERE param_id=OLD.param_id INTO obs_type, obs_code, param_uom_value;

	ELSE
		SELECT patient_id, true FROM visit_vitals vv
			WHERE vv.vital_reading_id=NEW.vital_reading_id ORDER BY date_time desc limit 1 INTO patientid, record_found;

		SELECT observation_type, observation_code, param_uom FROM vital_parameter_master
			WHERE param_id=NEW.param_id INTO obs_type, obs_code, param_uom_value;
	END IF;
	
	IF record_found AND COALESCE(obs_type, '') != '' AND COALESCE(obs_code, '') != ''
		THEN
			IF TG_OP = 'INSERT' THEN
				-- delete already existing observations for this charge id and observation code
				DELETE FROM mrd_observations ob
				WHERE ob.charge_id IN (select charge_id from bill_activity_charge bac join doctor_consultation dc
					ON (bac.activity_id=dc.consultation_id::text and bac.activity_code='DOC') where dc.patient_id=patientid
				) AND ob.observation_type=obs_type AND ob.code=obs_code;

				INSERT INTO mrd_observations (charge_id, observation_type, code, value, value_type, value_editable)
				SELECT bac.charge_id, obs_type, obs_code, NEW.param_value,
					param_uom_value, 'N'
				FROM bill_activity_charge bac
					JOIN doctor_consultation dc ON (bac.activity_id=dc.consultation_id::text AND bac.activity_code='DOC')
				WHERE dc.patient_id=patientid;

			ELSEIF TG_OP = 'UPDATE' THEN
				UPDATE mrd_observations ob SET observation_type=obs_type, code=obs_code,
					value=NEW.param_value, value_type=param_uom_value
				WHERE ob.charge_id IN (select charge_id from bill_activity_charge bac JOIN doctor_consultation dc ON
					(bac.activity_id=dc.consultation_id::text AND bac.activity_code='DOC') where dc.patient_id=patientid)
				AND ob.observation_type=obs_type AND ob.code=obs_code;
			ELSEIF TG_OP = 'DELETE' THEN
				DELETE FROM mrd_observations ob
				WHERE ob.charge_id IN (select charge_id from bill_activity_charge bac join doctor_consultation dc
					ON (bac.activity_id=dc.consultation_id::text and bac.activity_code='DOC') where dc.patient_id=patientid
				) AND ob.observation_type=obs_type AND ob.code=obs_code;
			END IF;
	END IF;
	RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS update_diag_observation_for_vitals ON vital_reading CASCADE;
CREATE TRIGGER update_diag_observation_for_vitals
  AFTER INSERT OR UPDATE OR DELETE
  ON vital_reading
  FOR EACH ROW
  EXECUTE PROCEDURE update_diag_observation_for_vitals();

--
-- All days from the beginning of the installation till date
--
DROP VIEW IF EXISTS all_days_view CASCADE;
CREATE VIEW all_days_view AS
SELECT generate_series(0,current_date-(SELECT min(date(start_date)) FROM ip_bed_details))
	+ (SELECT min(date(start_date)) from ip_bed_details) AS ref_date;

--
-- All beds all days cross join
--
DROP VIEW IF EXISTS all_beds_days_view CASCADE;
CREATE VIEW all_beds_days_view AS
SELECT (SELECT min(date(start_date)) from ip_bed_details)+g.a AS ref_date, bed_id
FROM generate_series(0,current_date-(SELECT min(date(start_date)) from ip_bed_details)) AS g(a)
    CROSS JOIN bed_names bn;

--
-- bed occupied days view: list one row per day per bed that is occupied in a day.
-- Occupied for a day is defined AS occupied at midnight ON that day. Thus, if a bed's END date
-- is today, it will not be counted AS occupied today. This also means that if a patient
-- is admitted and discharged ON the same day, this view will not show that bed-day at all.
--
-- We use generate_series for N number of days starting from the start date till end_date -1.
-- THEN we add the start date.
--
DROP VIEW IF EXISTS bed_occupied_days_view CASCADE;
CREATE VIEW bed_occupied_days_view AS
SELECT bed_id, start_date::date + generate_series(0,
  ((CASE WHEN bed_state = 'F' THEN date(end_date) ELSE current_date END) - date(start_date) - 1)) AS occ_date,
  1 AS occ_days,center_name
FROM ip_bed_details ipb
JOIN patient_registration pr USING(patient_id)
LEFT JOIN hospital_center_master hcm USING (center_id)
WHERE ipb.status != 'X';


--
-- Grouping ON date of the above, so that the total beds occupied per day is obtained
-- Note that no row is returned for a day where no beds were occupied.
--
DROP VIEW IF EXISTS total_bed_occupied_days_view CASCADE;
CREATE VIEW total_bed_occupied_days_view AS
SELECT occ_date, sum(occ_days) AS occ_days,center_name
FROM bed_occupied_days_view GROUP by occ_date,center_name;

--
-- Grouping ON date for the Bed occupancy, so that the total beds occupied per day is obtained
-- Note that no row is returned for a day where no beds were occupied.
--
DROP VIEW IF EXISTS total_bed_occupied_days_view_minimal CASCADE;
CREATE VIEW total_bed_occupied_days_view_minimal AS
SELECT occ_date, sum(occ_days) AS occ_days
FROM bed_occupied_days_view GROUP by occ_date;

--
-- bed_occupied_days_inc_view: one row per patient-bed-day, inclusive.
-- this is different from bed_occupied_days_view, this gives one row per bed-day if
-- it was occupied during any part of that day (whereas the other report will show
-- only if it was occupied at midnight). This report will included even when
-- discharge date is today.
--
DROP VIEW IF EXISTS bed_occupied_days_inc_view CASCADE;
CREATE VIEW bed_occupied_days_inc_view AS
SELECT bed_id, patient_id, admit_id, start_date::date + generate_series(0,
  ((CASE WHEN bed_state = 'F' THEN date(end_date) ELSE current_date END) - date(start_date))) AS occ_date
FROM ip_bed_details ipb
WHERE ipb.status != 'X';

--
-- View to give the number of hours occupied by a patient in one bed within a day.
-- One row per patient-bed-day is returned, with number of hours in that day.
--
DROP VIEW IF EXISTS patient_bed_day_hours_view CASCADE;
CREATE VIEW patient_bed_day_hours_view AS
SELECT bodv.occ_date, ipb.bed_id, ipb.patient_id, ipb.mrno AS mr_no, ipb.is_bystander,
	EXTRACT(EPOCH FROM
		LEAST(CASE WHEN bed_state = 'F' THEN end_date ELSE current_timestamp END, occ_date + 1)
		-
		GREATEST(start_date, occ_date)
	)/60/60 AS hours
FROM bed_occupied_days_inc_view AS bodv
	JOIN ip_bed_details ipb USING (admit_id)	/* for additional ipbed details info, esp start/end */
;

--
-- Convenience view for getting all threshold fields AS integer from IP Preferences, so we
-- don't have to deal with "-" AS one of the inputs
--
DROP VIEW IF EXISTS ip_pref_int_view CASCADE;
CREATE VIEW ip_pref_int_view AS
SELECT fd_threshold, hd_threshold,
	CASE WHEN hrly_charge_threshold = '-' THEN hd_threshold ELSE hrly_charge_threshold::integer
	END AS h_threshold
FROM (
	SELECT fd_threshold,
		CASE WHEN halfday_charge_threshold = '-' THEN fd_threshold ELSE halfday_charge_threshold::integer
		END AS hd_threshold,
		hrly_charge_threshold
	FROM (
		SELECT
			CASE WHEN fullday_charge_threshold = '-' THEN 24 ELSE fullday_charge_threshold::integer
			END AS fd_threshold,
			halfday_charge_threshold, hrly_charge_threshold
		FROM ip_preferences
	) AS f
) AS hf
;

--
-- Convenience view for viewing locks ON the db
--
DROP VIEW IF EXISTS all_locks_status;
CREATE VIEW all_locks_status AS
SELECT t.relname, l.locktype, page, virtualtransaction, pid, mode
FROM pg_locks l, pg_stat_all_tables t
WHERE l.relation=t.relid
ORDER BY relation ASC
;


DROP VIEW IF EXISTS patient_discharges CASCADE;
CREATE OR REPLACE VIEW patient_discharges AS
SELECT discharge_date,
sum(coalesce(discharge,0)) AS discharge,
sum(coalesce(normal_count,0)) AS normal_count,
sum(coalesce(absconded_count)) AS absconded_count,
sum(coalesce(dama_count,0)) AS dama_count,
sum(coalesce(death_count,0)) AS death_count,
sum(coalesce(ref_count,0)) AS ref_count,
sum(coalesce(admn_cancelled_count, 0)) AS admn_cancelled_count,
sum(coalesce(other_discharges_count, 0)) AS other_discharges_count  from(
	select LEAST(COALESCE(discharge_date,reg_date),current_date) AS discharge_date,
	 CASE WHEN coalesce(discharge_type, '') != 'Admission Cancelled' THEN 1 ELSE 0 END AS discharge,
	 CASE WHEN discharge_type ='Normal' THEN 1 ELSE 0 END AS normal_count,
	 CASE WHEN discharge_type='Absconded' THEN 1 ELSE 0 END AS absconded_count,
	 CASE WHEN discharge_type ='DAMA' THEN 1 ELSE 0 END AS dama_count,
	 CASE WHEN discharge_type='Death' THEN 1 ELSE 0 END AS death_count,
	 CASE WHEN discharge_type='Referred To' THEN 1 ELSE 0 END AS ref_count,
	 CASE WHEN discharge_type='Admission Cancelled' THEN 1 ELSE 0 END AS admn_cancelled_count,
	 CASE WHEN pr.discharge_type_id not in(1,2,3,4,5,6)
	 AND pr.discharge_type_id IS NOT NULL THEN 1 ELSE 0 END AS other_discharges_count
	 from patient_registration pr
	 LEFT JOIN discharge_type_master dtm ON(dtm.discharge_type_id = pr.discharge_type_id)
	 where pr.status='I' and visit_type='i'
	 ) AS foo
GROUP BY discharge_date;


DROP VIEW IF EXISTS patient_admissions CASCADE;
CREATE OR REPLACE VIEW patient_admissions AS
SELECT reg_date,count(reg_date) from patient_registration pr
LEFT JOIN discharge_type_master dtm ON(dtm.discharge_type_id = pr.discharge_type_id)
where visit_type='i' and coalesce(discharge_type, '')!='Admission Cancelled'
GROUP BY reg_date;

--
-- Number of times each bed was allocated every day
--
DROP VIEW IF EXISTS bed_start_dates CASCADE;
CREATE OR REPLACE VIEW bed_start_dates AS
SELECT bed_id, start_date::date, count(*) AS count
FROM ip_bed_details
WHERE status != 'X'
GROUP BY bed_id, start_date::date;

DROP VIEW IF EXISTS bed_end_dates CASCADE;
CREATE OR REPLACE VIEW bed_end_dates AS
SELECT bed_id, end_date::date AS end_date, count(*) AS count
FROM ip_bed_details
WHERE ip_bed_details.status != 'X' AND bed_state = 'F'
GROUP BY bed_id, end_date::date;


--
-- Total number of allocations in a day
--
DROP VIEW IF EXISTS total_bed_start_dates CASCADE;
CREATE OR REPLACE VIEW total_bed_start_dates AS
SELECT start_date::date, count(*) AS count
FROM ip_bed_details
WHERE status != 'X'
GROUP BY start_date::date;

DROP VIEW IF EXISTS total_bed_end_dates CASCADE;
CREATE OR REPLACE VIEW total_bed_end_dates AS
SELECT end_date::date AS end_date, count(*) AS count
FROM ip_bed_details
WHERE ip_bed_details.status != 'X' AND bed_state = 'F'
GROUP BY end_date::date;

DROP VIEW IF EXISTS total_bed_occupancy_dashboard CASCADE;
CREATE VIEW total_bed_occupancy_dashboard AS
SELECT ab.ref_date, coalesce(occ.occ_days,0)+coalesce(allocs.count,0)-coalesce(deallocs.count,0) AS closing_count
FROM all_days_view ab
LEFT JOIN total_bed_occupied_days_view_minimal occ ON (occ.occ_date = ab.ref_date-1)
LEFT JOIN total_bed_start_dates allocs on(allocs.start_date = ab.ref_date)
LEFT JOIN total_bed_end_dates deallocs ON(deallocs.end_date = ab.ref_date);


DROP VIEW IF EXISTS in_patient_reg_days_view CASCADE;
CREATE VIEW in_patient_reg_days_view AS
SELECT reg_date::date + generate_series(0,
  ((CASE WHEN pr.status = 'I' THEN LEAST(COALESCE(discharge_date,reg_date),current_date) ELSE current_date END)
  - date(reg_date) - 1)) AS reg_occ_date,
  count(patient_id) AS reg_occ_days
FROM patient_registration pr
WHERE pr.visit_type='i'
GROUP BY reg_occ_date;

DROP VIEW IF EXISTS stock_transfer_supplier_view  CASCADE ;

DROP VIEW IF EXISTS all_bill_available_templates_view CASCADE;
CREATE VIEW all_bill_available_templates_view AS
SELECT * from bill_available_templates
UNION ALL
SELECT 'CUSTOM-'||template_name, template_name, '*','*',0,'A'
FROM bill_print_template bpt
WHERE NOT EXISTS(SELECT * FROM bill_available_templates WHERE template_name = bpt.template_name)
UNION ALL
SELECT 'CUSTOMEXP-'||template_name, template_name, '*','*',1,'A'
FROM patient_visit_bills_template bvt
WHERE NOT EXISTS(SELECT * FROM bill_available_templates WHERE template_name = bvt.template_name)
ORDER BY display_order, template_name;

CREATE OR REPLACE FUNCTION update_original_result() RETURNS trigger AS $BODY$
BEGIN
IF new.original_test_details_id IS NOT NULL THEN
 UPDATE test_details SET revised_test_details_id = new.test_details_id, test_detail_status = 'A'
 WHERE test_details_id = new.original_test_details_id;
END IF;
RETURN NEW;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS update_original_test_detail_id_trigger ON test_details;
CREATE TRIGGER update_original_test_detail_id_trigger
  AFTER INSERT
  ON test_details
  FOR EACH ROW
  EXECUTE PROCEDURE update_original_result();

 CREATE OR REPLACE FUNCTION revrtto_original_result() RETURNS trigger AS $BODY$

BEGIN
IF old.original_test_details_id IS NOT NULL THEN
 UPDATE test_details SET revised_test_details_id = null,test_detail_status = 'S',amendment_reason = null
 WHERE test_details_id = old.original_test_details_id;
END IF;

IF ((SELECT count(*) from test_details where prescribed_id = old.prescribed_id
 AND test_detail_status IN('RP','RC','RV'))::int = 0) THEN
 UPDATE tests_prescribed set conducted = 'S' where prescribed_id = old.prescribed_id;
END IF;

RETURN NEW;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS revertto_original_test_detail_id_trigger ON test_details;
CREATE TRIGGER revertto_original_test_detail_id_trigger
  AFTER DELETE
  ON test_details
  FOR EACH ROW
  EXECUTE PROCEDURE revrtto_original_result();

-- patient_general_docs_audit_view

DROP VIEW IF EXISTS patient_general_docs_audit_view;
CREATE OR REPLACE VIEW patient_general_docs_audit_view AS
(SELECT pdl.log_id, 'patient_general_docs_audit_log' AS base_table,
	pdl.mr_no, null AS doc_id,
	pdl.user_name, pdl.mod_time, pdl.operation,
	'patient_general_docs_'::text || pdl.field_name::text AS field_name,
	pdl.old_value, pdl.new_value
	FROM patient_general_docs_audit_log pdl
UNION ALL
(SELECT foo.log_id, 'patient_pdf_form_doc_values_audit_log' AS base_table,
	foo.mr_no, foo.doc_id,
	foo.user_name, foo.mod_time, foo.operation,
	'patient_pdf_form_doc_values_'::text || foo.field_name::text AS field_name,
	'' AS old_value, foo.new_value
FROM (SELECT min(log_id) AS log_id,ppfl.doc_id, pgd.mr_no, ppfl.user_name,mod_time,operation,field_name,old_value,new_value
  FROM patient_pdf_form_doc_values_audit_log ppfl
   JOIN patient_general_docs pgd ON pgd.doc_id = ppfl.doc_id
   GROUP BY ppfl.doc_id,ppfl.user_name,mod_time,operation,field_name,old_value,new_value,pgd.mr_no) AS foo
WHERE foo.operation = 'INSERT' AND foo.field_name = 'doc_id'));



DROP FUNCTION IF EXISTS convert_to_numeric(text) CASCADE;
CREATE OR REPLACE FUNCTION convert_to_numeric(v_input text)
  RETURNS NUMERIC AS
$BODY$
DECLARE v_numeric_value NUMERIC DEFAULT NULL;
BEGIN
    BEGIN
        v_numeric_value := v_input::NUMERIC;
    EXCEPTION WHEN OTHERS THEN
        RETURN NULL;
    END;
RETURN v_numeric_value;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE
  COST 100;



DROP FUNCTION IF EXISTS add_center_dept_token_trigger() CASCADE;
CREATE OR REPLACE FUNCTION add_center_dept_token_trigger() RETURNS TRIGGER AS $BODY$
DECLARE

BEGIN

	INSERT INTO test_dept_tokens (SELECT d.ddept_id, hcm.center_id, 0 FROM diagnostics_departments d CROSS JOIN hospital_center_master hcm
		WHERE ddept_id||center_id not in (SELECT dept_id||center_id from test_dept_tokens));
RETURN NEW;
END;
$BODY$ language plpgsql;

DROP TRIGGER IF EXISTS add_center_dept_token_trigger ON diagnostics_departments CASCADE;
CREATE TRIGGER add_center_dept_token_trigger AFTER INSERT ON diagnostics_departments
    FOR EACH ROW EXECUTE PROCEDURE add_center_dept_token_trigger();

DROP TRIGGER IF EXISTS add_center_dept_token_trigger ON hospital_center_master CASCADE;
CREATE TRIGGER add_center_dept_token_trigger AFTER INSERT ON hospital_center_master
    FOR EACH ROW EXECUTE PROCEDURE add_center_dept_token_trigger();

DROP VIEW IF EXISTS deposit_ledger_view CASCADE ;
CREATE OR REPLACE VIEW deposit_ledger_view AS
SELECT deposit_no, deposit_date, payment_mode, credit, debit, mr_no, credit-debit AS balance, deposit_available_for 
FROM ( SELECT pdepo.receipt_id AS deposit_no, pdepo.display_date AS deposit_date, pm.payment_mode,
	CASE WHEN pdepo.amount>= 0 THEN pdepo.amount ELSE 0 END AS credit,
	CASE WHEN pdepo.amount< 0 THEN (-pdepo.amount) ELSE 0 END AS debit,
	mr_no, pdepo.deposit_available_for
	FROM patient_deposits_view pdepo
	JOIN payment_mode_master pm ON (pdepo.payment_mode_id = pm.mode_id) WHERE pdepo.is_deposit
	UNION
	SELECT bddv.bill_no, COALESCE (b.finalized_date,b.mod_time) AS set_off_date, '--' AS payment_mode, 
	CASE WHEN bddv.deposit_set_off< 0 THEN -bddv.deposit_set_off ELSE 0 END AS credit,
	CASE WHEN bddv.deposit_set_off>= 0 THEN bddv.deposit_set_off ELSE 0 END AS debit, bddv.mr_no,''
	FROM bill_deposit_details_view bddv
	LEFT JOIN bill b using(bill_no)
	where bddv.deposit_set_off <>0
	) AS foo
GROUP BY deposit_no, deposit_date, payment_mode, credit, debit, mr_no, deposit_available_for
ORDER BY deposit_date;


DROP VIEW IF EXISTS incoming_sample_ext_view CASCADE;
CREATE VIEW incoming_sample_ext_view AS
SELECT incoming_visit_id AS patient_id, isr.patient_name AS full_name,
	get_patient_age(null,null,isr.isr_dateofbirth,isr.patient_age) AS age, get_patient_age_in(null,null,isr.isr_dateofbirth,isr.age_unit) AS age_unit,
	address AS patient_address, billno AS bill_no,
	hcm.center_name,hcm.center_id, referal_name AS doctor_name,'GENERAL'::text AS org_name,
	'ORG0001'::text AS org_id, date AS reg_date,
	ih.hospital_name AS incoming_hospital_name,
	COALESCE(pd.dateofbirth, pd.expected_dob) AS expected_dob,
	get_patient_age(null,null,isr.isr_dateofbirth,isr.patient_age)::text AS age_text, pd.dateofbirth,
	CASE WHEN COALESCE(pd.patient_gender, isr.patient_gender) = 'M' THEN 'Male'
	WHEN COALESCE(pd.patient_gender, isr.patient_gender) = 'F' THEN 'Female'
	ELSE 'Couple' END AS gender
FROM incoming_sample_registration isr
LEFT JOIN referral rd ON isr.referring_doctor = rd.referal_no
LEFT JOIN hospital_center_master hcm ON (hcm.center_id = isr.center_id)
LEFT JOIN patient_details pd ON (pd.mr_no = isr.mr_no)
JOIN incoming_hospitals ih ON (ih.hospital_id = isr.orig_lab_name)
WHERE ( patient_confidentiality_check(COALESCE(pd.patient_group,0),pd.mr_no) )
;

DROP VIEW IF EXISTS clinical_lab_values_ext_view CASCADE;
CREATE OR REPLACE VIEW clinical_lab_values_ext_view AS
SELECT clinical_lab_recorded_id,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'Hb'
                    OR trm.resultlabel ILIKE 'Hemoglobin' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS hemoglobin,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'Hb'
                    OR trm.resultlabel ILIKE 'Hemoglobin' THEN clv.value_date
               ELSE NULL
           END) AS hemoglobin_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'TLC'
                    OR trm.resultlabel ILIKE 'Total Leucocyte Count' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END )AS tlc,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'TLC'
                    OR trm.resultlabel ILIKE 'Total Leucocyte Count' THEN clv.value_date
               ELSE NULL
           END) AS tlc_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'DLC'
                    OR trm.resultlabel ILIKE 'Differential Leucocyte Count' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS dlc,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'DLC'
                    OR trm.resultlabel ILIKE 'Differential Leucocyte Count' THEN clv.value_date
               ELSE NULL
           END) AS dlc_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'PLT'
                    OR trm.resultlabel ILIKE 'Platelet Count' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS plt,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'PLT'
                    OR trm.resultlabel ILIKE 'Platelet Count' THEN clv.value_date
               ELSE NULL
           END) AS plt_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'RBS'
                    OR trm.resultlabel ILIKE 'Random Blood Sugar' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS rbs,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'RBS'
                    OR trm.resultlabel ILIKE 'Random Blood Sugar' THEN clv.value_date
               ELSE NULL
           END) AS rbs_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'MCV'
                    OR trm.resultlabel ILIKE 'Mean Corpuscular Volume' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS mcv,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'MCV'
                    OR trm.resultlabel ILIKE 'Mean Corpuscular Volume' THEN clv.value_date
               ELSE NULL
           END) AS mcv_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'PRB'
                    OR trm.resultlabel ILIKE 'pre% BUN' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS prebun,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'PRB'
                    OR trm.resultlabel ILIKE 'pre% BUN' THEN clv.value_date
               ELSE NULL
           END) AS prebun_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'POB'
                    OR trm.resultlabel ILIKE 'post% BUN' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS postbun,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'POB'
                    OR trm.resultlabel ILIKE 'post% BUN' THEN clv.value_date
               ELSE NULL
           END) AS postbun_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'Creat.'
                    OR trm.resultlabel ILIKE 'Creatinine' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS creatinine,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'Creat.'
                    OR trm.resultlabel ILIKE 'Creatinine' THEN clv.value_date
               ELSE NULL
           END) AS creatinine_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'Na'
                    OR trm.resultlabel ILIKE 'Sodium' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS sodium,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'Na'
                    OR trm.resultlabel ILIKE 'Sodium' THEN clv.value_date
               ELSE NULL
           END) AS sodium_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'K'
                    OR trm.resultlabel ILIKE 'Pottasium' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS potassium,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'K'
                    OR trm.resultlabel ILIKE 'Pottasium' THEN clv.value_date
               ELSE NULL
           END) AS potassium_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'Bicarb.'
                    OR trm.resultlabel ILIKE 'Bicarbonate' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS bicarbonate,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'Bicarb.'
                    OR trm.resultlabel ILIKE 'Bicarbonate' THEN clv.value_date
               ELSE NULL
           END) AS bicarbonate_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'UA'
                    OR trm.resultlabel ILIKE 'Uric Acid' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS ua,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'UA'
                    OR trm.resultlabel ILIKE 'Uric Acid' THEN clv.value_date
               ELSE NULL
           END) AS ua_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'ca'
                    OR trm.resultlabel_short ILIKE 'cca'
                    OR trm.resultlabel ILIKE 'Calcium' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS ca,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'ca'
                    OR trm.resultlabel_short ILIKE 'cca'
                    OR trm.resultlabel ILIKE 'Calcium' THEN clv.value_date
               ELSE NULL
           END) AS ca_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'Phos'
                    OR trm.resultlabel ILIKE 'Phosphorus' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS phos,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'Phos'
                    OR trm.resultlabel ILIKE 'Phosphorus' THEN clv.value_date
               ELSE NULL
           END) AS phos_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'T.Bilirubin'
                    OR trm.resultlabel ILIKE 'Total Bilirubin' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS bilirubin,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'T.Bilirubin'
                    OR trm.resultlabel ILIKE 'Total Bilirubin' THEN clv.value_date
               ELSE NULL
           END) AS bilirubin_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'T.Prot'
                    OR trm.resultlabel ILIKE 'Total Protein' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS total_protein,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'T.Prot'
                    OR trm.resultlabel ILIKE 'Total Protein' THEN clv.value_date
               ELSE NULL
           END) AS total_protein_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'albumin'
                    OR trm.resultlabel ILIKE 'Albumin' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS albumin,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'albumin'
                    OR trm.resultlabel ILIKE 'Albumin' THEN clv.value_date
               ELSE NULL
           END) AS albumin_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'ALT'
                    OR trm.resultlabel ILIKE 'Alanine Aminotransferase' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS alt,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'ALT'
                    OR trm.resultlabel ILIKE 'Alanine Aminotransferase' THEN clv.value_date
               ELSE NULL
           END) AS alt_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'AST'
                    OR trm.resultlabel ILIKE 'Aspartate Aminotransferase' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS ast,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'AST'
                    OR trm.resultlabel ILIKE 'Aspartate Aminotransferase' THEN clv.value_date
               ELSE NULL
           END) AS ast_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'Alk Phos'
                    OR trm.resultlabel ILIKE 'Alkaline Phosphatase' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS alk_phos,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'Alk Phos'
                    OR trm.resultlabel ILIKE 'Alkaline Phosphatase' THEN clv.value_date
               ELSE NULL
           END) AS alk_phos_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'HIV'
                    OR trm.resultlabel ILIKE 'Human Immuno Deficiency Virus' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS hiv,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'HIV'
                    OR trm.resultlabel ILIKE 'Human Immuno Deficiency Virus' THEN clv.value_date
               ELSE NULL
           END) AS hiv_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'HBsAg'
                    OR trm.resultlabel ILIKE 'Hepatitis B Surface Antigen' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS hbsg,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'HBsAg'
                    OR trm.resultlabel ILIKE 'Hepatitis B Surface Antigen' THEN clv.value_date
               ELSE NULL
           END) AS hbsg_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'HCV'
                    OR trm.resultlabel ILIKE 'Hepatitis C Virus' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS hcv,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'HCV'
                    OR trm.resultlabel ILIKE 'Hepatitis C Virus' THEN clv.value_date
               ELSE NULL
           END) AS hcv_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'Anti-HBS titres'
                    OR trm.resultlabel ILIKE 'Anti-Hepatitis B Surface Antigen Titres' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS anti_hbs,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'Anti-HBS titres'
                    OR trm.resultlabel ILIKE 'Anti-Hepatitis B Surface Antigen Titres' THEN clv.value_date
               ELSE NULL
           END) AS anti_hbs_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'PTH'
                    OR trm.resultlabel ILIKE 'ParaThyroid Hormone' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS pth,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'PTH'
                    OR trm.resultlabel ILIKE 'ParaThyroid Hormone' THEN clv.value_date
               ELSE NULL
           END) AS pth_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'iron'
                    OR trm.resultlabel ILIKE 'Iron' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS iron,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'iron'
                    OR trm.resultlabel ILIKE 'Iron' THEN clv.value_date
               ELSE NULL
           END) AS iron_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'TIBC'
                    OR trm.resultlabel ILIKE 'Total Iron Binding Capacity' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS tibc,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'TIBC'
                    OR trm.resultlabel ILIKE 'Total Iron Binding Capacity' THEN clv.value_date
               ELSE NULL
           END) AS tibc_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'Ferritin'
                    OR trm.resultlabel ILIKE 'Ferritin' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS ferritin,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'Ferritin'
                    OR trm.resultlabel ILIKE 'Ferritin' THEN clv.value_date
               ELSE NULL
           END) AS ferritin_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'Tsat'
                    OR trm.resultlabel ILIKE 'Total Saturation' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS t_sat,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'Tsat'
                    OR trm.resultlabel ILIKE 'Total Saturation' THEN clv.value_date
               ELSE NULL
           END) AS t_sat_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'T.chol'
                    OR trm.resultlabel ILIKE 'Total Cholesterol' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS t_chol,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'T.chol'
                    OR trm.resultlabel ILIKE 'Total Cholesterol' THEN clv.value_date
               ELSE NULL
           END) AS t_chol_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'TG'
                    OR trm.resultlabel ILIKE 'Triacylglycerol' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS tg,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'TG'
                    OR trm.resultlabel ILIKE 'Triacylglycerol' THEN clv.value_date
               ELSE NULL
           END) AS tg_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'LDL'
                    OR trm.resultlabel ILIKE 'Low-Density Lipoprotein' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS ldl,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'LDL'
                    OR trm.resultlabel ILIKE 'Low-Density Lipoprotein' THEN clv.value_date
               ELSE NULL
           END) AS ldl_value_as_of_date,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'HDL'
                    OR trm.resultlabel ILIKE 'High-Density Lipoprotein' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS hdl,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'HDL'
                    OR trm.resultlabel ILIKE 'High-Density Lipoprotein' THEN clv.value_date
               ELSE NULL
           END) AS hdl_value_as_of_date,
            MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'ktv'
                    OR trm.resultlabel ILIKE 'KT/V' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS ktv,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'ktv'
                    OR trm.resultlabel ILIKE 'KT/V' THEN clv.value_date
               ELSE NULL
           END) AS ktv_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'urr'
                    OR trm.resultlabel ILIKE 'URR' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS urr,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'urr'
                    OR trm.resultlabel ILIKE 'URR' THEN clv.value_date
               ELSE NULL
           END) AS urr_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'capo'
                    OR trm.resultlabel ILIKE 'Ca PO4' THEN ROUND(convert_to_numeric(clv.test_value),2)
               ELSE NULL
           END) AS capo,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'capo'
                    OR trm.resultlabel ILIKE 'Ca PO4' THEN clv.value_date
               ELSE NULL
           END) AS capo_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'asite'
                    OR trm.resultlabel ILIKE 'Access Site' THEN clv.test_value
               ELSE NULL
           END) AS asite,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'asite'
                    OR trm.resultlabel ILIKE 'Access Site' THEN clv.value_date
               ELSE NULL
           END) AS asite_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'asiteinf'
                    OR trm.resultlabel ILIKE 'Access Site Infection' THEN clv.test_value
               ELSE NULL
           END) AS asiteinf,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'asiteinf'
                    OR trm.resultlabel ILIKE 'Access Site Infection' THEN clv.value_date
               ELSE NULL
           END) AS asiteinf_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'atypeprim'
                    OR trm.resultlabel ILIKE 'Access Type (Primary)' THEN clv.test_value
               ELSE NULL
           END) AS atypeprim,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'atypeprim'
                    OR trm.resultlabel ILIKE 'Access Type (Primary)' THEN clv.value_date
               ELSE NULL
           END) AS atypeprim_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'atypesec'
                    OR trm.resultlabel ILIKE 'Access type (Secondary)' THEN clv.test_value
               ELSE NULL
           END) AS atypesec,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'atypesec'
                    OR trm.resultlabel ILIKE 'Access type (Secondary)' THEN clv.value_date
               ELSE NULL
           END) AS atypesec_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'inf'
                    OR trm.resultlabel ILIKE 'Infection' THEN clv.test_value
               ELSE NULL
           END) AS inf,
          MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'inf'
                    OR trm.resultlabel ILIKE 'Infection' THEN clv.value_date
               ELSE NULL
           END) AS inf_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'org'
                    OR trm.resultlabel ILIKE 'Organism' THEN clv.test_value
               ELSE NULL
           END) AS org,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'org'
                    OR trm.resultlabel ILIKE 'Organism' THEN clv.value_date
               ELSE NULL
           END) AS org_value_as_of_date,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'locsite'
                    OR trm.resultlabel ILIKE 'Location/Site' THEN clv.test_value
               ELSE NULL
           END) AS locsite,
           MAX(CASE
               WHEN trm.resultlabel_short ILIKE 'locsite'
                    OR trm.resultlabel ILIKE 'Location/Site' THEN clv.value_date
               ELSE NULL
           END) AS locsite_value_as_of_date
   FROM clinical_lab_values clv
   LEFT JOIN clinical_lab_result clres ON clres.resultlabel_id = clv.resultlabel_id
   LEFT JOIN test_results_master trm ON trm.resultlabel_id = clres.resultlabel_id
GROUP BY clinical_lab_recorded_id;



DROP FUNCTION IF EXISTS clear_doctor_medicine_favourites() CASCADE;
CREATE OR REPLACE FUNCTION clear_doctor_medicine_favourites() RETURNS TRIGGER AS $BODY$
DECLARE
delDocMedFavForDoctors RECORD;
doctorId text;

BEGIN
	IF NEW.prescriptions_by_generics != OLD.prescriptions_by_generics THEN
		FOR delDocMedFavForDoctors IN
			SELECT doctor_id FROM doctors JOIN doctor_center_master USING (doctor_id) JOIN hospital_center_master USING (center_id)
			WHERE health_authority = NEW.health_authority
			LOOP
				doctorId := delDocMedFavForDoctors.doctor_id;
				IF doctorId IS NOT NULL AND doctorId != '' THEN
					delete from doctor_medicine_favourites where doctor_id = doctorId;
				END IF;
			END LOOP;
	END IF;
RETURN NEW;
END;
$BODY$ language plpgsql;


DROP TRIGGER IF EXISTS clear_doctor_medicine_favourites ON health_authority_preferences;
CREATE TRIGGER clear_doctor_medicine_favourites
    AFTER UPDATE ON health_authority_preferences
    FOR EACH ROW
    EXECUTE PROCEDURE clear_doctor_medicine_favourites();

--
-- Trigger for updating the national claim number. The following conditions
-- will trigger the update of the number:
-- * Bill is being finalized
-- * Number does not exist already
-- * Either of the patient's insurance is type national
-- * The national sponsor is set up for per-day reimbursement
--
DROP FUNCTION IF EXISTS bill_generate_national_claim_no() CASCADE;
CREATE OR REPLACE FUNCTION bill_generate_national_claim_no() RETURNS TRIGGER AS $BODY$
DECLARE
	newClaimNoReqd boolean;
BEGIN
	IF OLD.is_tpa AND OLD.status = 'A' AND NEW.status = 'F' AND OLD.national_claim_no IS NULL THEN
-- TODO: sponsor type migration
		SELECT tm.per_day_rate != 0 FROM patient_registration pr
			LEFT JOIN tpa_master tm ON (pr.primary_sponsor_id = tm.tpa_id)
		WHERE pr.patient_id = NEW.visit_id
		INTO newClaimNoReqd;

		IF newClaimNoReqd IS NULL OR NOT newClaimNoReqd THEN
			-- check secondary sponsor: it may require it
			SELECT tm.per_day_rate != 0 FROM patient_registration pr
				LEFT JOIN tpa_master tm ON (pr.secondary_sponsor_id = tm.tpa_id)
			WHERE pr.patient_id = NEW.visit_id
			INTO newClaimNoReqd;
		END IF;
	END IF;

	IF newClaimNoReqd THEN
		NEW.national_claim_no = nextval('bill_national_claim_seq');
	END IF;

	RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS bill_generate_national_claim_no_trigger ON bill CASCADE;
CREATE TRIGGER bill_generate_national_claim_no_trigger
BEFORE UPDATE ON bill
FOR EACH ROW
EXECUTE PROCEDURE bill_generate_national_claim_no();

--
-- Whenever a test is prescribed, check if it needs to be exported via HL7
-- and do the needful.
--

DROP FUNCTION IF EXISTS hl7_save_test_export_items() CASCADE;
CREATE FUNCTION hl7_save_test_export_items() RETURNS TRIGGER AS $BODY$
DECLARE

	rec RECORD;
	testInterface RECORD;
--	is_item_exported BOOLEAN := FALSE;
	bill_paid_status character(1);
	orderType character varying;
	modifiedOrderType character varying;
	resultType character varying;
	is_out_house boolean := false;
	outInterface RECORD;
	patientCenterId integer;
BEGIN

-- Note : We get the appointment id and thereby the equipment code for a scheduled test.
-- If the test is not scheduled, THEN we check if there is at least one equipment with a
-- hl7 export code for the concerned diagnostic department. If there are multiple
-- equipments, satisfying the condition THEN we assume that any one is good enough, since
-- in such a CASE our basic assumption is that all the equipments will have the
-- same hl7 export code. Hence the group by ON the query.
	SELECT hli.interface_name, hli.hl7_lab_interface_id INTO outInterface
					  FROM outhouse_master om
					  JOIN hl7_lab_interfaces hli ON(hli.hl7_lab_interface_id = om.hl7_lab_interface_id)
					  JOIN diag_outsource_master  dom ON (dom.outsource_dest = om.oh_id)
					  WHERE dom.outsource_dest_id = NEW.outsource_dest_id AND om.protocol ='hl7' AND hli.status='A';
	IF (outInterface is NULL OR outInterface.hl7_lab_interface_id IS NULL) THEN
		FOR rec IN
			SELECT hli.equipment_code_required, hli.interface_name, tem.hl7_export_code, hli.hl7_lab_interface_id,
			COALESCE(pr.center_id, isr.center_id) AS centerid, d.conduction_format, dei.item_type
			FROM hl7_lab_interfaces hli
			JOIN diagnostics_export_interface dei USING(hl7_lab_interface_id)
			JOIN diagnostics d USING(test_id)
			LEFT JOIN patient_registration pr ON (pr.patient_id = NEW.pat_id)
			LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = NEW.pat_id)
			JOIN hl7_center_interfaces hci ON (hci.hl7_lab_interface_id = hli.hl7_lab_interface_id AND 
					((COALESCE(pr.center_id, isr.center_id) = hci.center_id) OR (0 = hci.center_id)))		
			LEFT JOIN (SELECT em.ddept_id, em.center_id, min(em.hl7_export_code) AS hl7_export_code
			FROM test_equipment_master em WHERE coalesce(em.hl7_export_code, '') != '' group by em.ddept_id, em.center_id) AS tem
			ON (tem.center_id = COALESCE(pr.center_id, isr.center_id) AND tem.ddept_id = d.ddept_id)
			WHERE test_id = NEW.test_id AND hli.status = 'A'
		LOOP
			IF rec.equipment_code_required = 'N' OR NEW.appointment_id != 0 OR rec.hl7_export_code != '' THEN
				orderType:= 'TEST';
				resultType:= 'TESTRESULT';
				modifiedOrderType := 'TESTRESULTMODIFIED';
				IF (rec.conduction_format = 'T') THEN
					orderType:= 'TESTTEMPLATE';
					modifiedOrderType := 'TESTTEMPLATEMODIFIED';
					resultType:= 'TESTTEMPLATERESULT';
				END IF;
				IF (TG_OP = 'INSERT') THEN
					IF (NEW.conduction_type = 'i' AND rec.item_type = 'TEST' ) THEN
						INSERT INTO hl7_export_items (item_type, item_id, inserted_ts, interface_name, hl7_lab_interface_id, bill_paid, op_code, center_id)
						VALUES (orderType, NEW.prescribed_id, current_timestamp, rec.interface_name, rec.hl7_lab_interface_id, 'N', 'N', rec.centerid);
					END IF;
				ELSIF (TG_OP = 'UPDATE') THEN
					-- check if the order has not been sent yet
					SELECT 'Y' INTO bill_paid_status FROM hl7_export_items WHERE item_id = NEW.prescribed_id::text 
					AND op_code = 'N' AND export_status = 'S' AND item_type = orderType LIMIT 1;
					bill_paid_status := COALESCE(bill_paid_status, 'N');
					IF (rec.item_type = 'TEST' AND (OLD.conducted != 'X' AND NEW.conducted = 'X')) THEN
						INSERT INTO hl7_export_items (item_type, item_id, inserted_ts, interface_name, hl7_lab_interface_id, bill_paid, op_code, center_id)
						VALUES (orderType, NEW.prescribed_id, current_timestamp, rec.interface_name, rec.hl7_lab_interface_id, bill_paid_status, 'C', rec.centerid);	
					ELSIF (OLD.conduction_type = 'i' AND NEW.conduction_type = 'o' AND rec.item_type = 'TEST') THEN
						INSERT INTO hl7_export_items (item_type, item_id, inserted_ts, interface_name, hl7_lab_interface_id, bill_paid, op_code, center_id)
						VALUES (orderType, NEW.prescribed_id, current_timestamp, rec.interface_name, rec.hl7_lab_interface_id, 'Y', 'C', rec.centerid);
					ELSIF (OLD.conduction_type = 'o' AND NEW.conduction_type = 'i' AND rec.item_type = 'TEST') THEN
						INSERT INTO hl7_export_items (item_type, item_id, inserted_ts, interface_name, hl7_lab_interface_id, bill_paid, op_code, center_id)
						VALUES (orderType, NEW.prescribed_id, current_timestamp, rec.interface_name, rec.hl7_lab_interface_id, 'N', 'N', rec.centerid);				
					ELSIF (OLD.conducted = 'S' AND NEW.conducted = 'RAS' AND rec.item_type = 'TESTMODIFIED') THEN
						INSERT INTO hl7_export_items (item_type, item_id, inserted_ts, interface_name, hl7_lab_interface_id, bill_paid, op_code, center_id)
						VALUES (modifiedOrderType, NEW.prescribed_id, current_timestamp, rec.interface_name, rec.hl7_lab_interface_id, 'N', 'RC', rec.centerid);					
					ELSIF (OLD.conducted = 'S' AND rec.item_type = 'TESTMODIFIED' AND (NEW.conducted = 'RP' OR NEW.conducted = 'RC' OR NEW.conducted = 'RV')) THEN
						INSERT INTO hl7_export_items (item_type, item_id, inserted_ts, interface_name, hl7_lab_interface_id, bill_paid, op_code, center_id)
						VALUES (modifiedOrderType, NEW.prescribed_id, current_timestamp, rec.interface_name, rec.hl7_lab_interface_id, 'N', 'AM', rec.centerid);
					ELSIF ((rec.item_type = 'TESTRESULT') AND (OLD.conducted IN('N', 'P', 'C', 'V', 'RP', 'RC', 'RV')) AND (NEW.conducted = 'S')) THEN
						INSERT INTO hl7_export_items (item_type, item_id, inserted_ts, interface_name, hl7_lab_interface_id, bill_paid, op_code, center_id)
						VALUES (resultType, NEW.prescribed_id, current_timestamp, rec.interface_name, rec.hl7_lab_interface_id, bill_paid_status, 'N', rec.centerid);
					ELSIF (rec.item_type = 'TEST' AND NEW.conduction_type = 'i' 
							AND OLD.sample_collection_id IS NOT NULL AND OLD.sample_collection_id != NEW.sample_collection_id) THEN
						INSERT INTO hl7_export_items (item_type, item_id, inserted_ts, interface_name, hl7_lab_interface_id, bill_paid, op_code, center_id)
						VALUES (orderType, NEW.prescribed_id, current_timestamp, rec.interface_name, rec.hl7_lab_interface_id, 'N', 'C', rec.centerid);
						INSERT INTO hl7_export_items (item_type, item_id, inserted_ts, interface_name, hl7_lab_interface_id, bill_paid, op_code, center_id)
						VALUES (orderType, NEW.prescribed_id, current_timestamp, rec.interface_name, rec.hl7_lab_interface_id, 'N', 'N', rec.centerid);
					END IF;
				END IF;
			END IF;
		END LOOP;
	ELSE 
		IF (TG_OP = 'UPDATE') THEN
			    patientCenterId := (SELECT center_id FROM patient_registration WHERE patient_id = NEW.pat_id);
			    IF (patientCenterId is NULL) THEN
			    		patientCenterId := (SELECT center_id FROM incoming_sample_registration WHERE incoming_visit_id = NEW.pat_id);
			    END IF;			    
				SELECT 'Y' INTO bill_paid_status FROM hl7_export_items WHERE item_id = NEW.prescribed_id::text
					AND op_code = 'N' AND export_status = 'S' LIMIT 1;
				bill_paid_status := COALESCE(bill_paid_status, 'N');
				
				IF (OLD.conducted != 'X' AND NEW.conducted = 'X') THEN
					INSERT INTO hl7_export_items (item_type, item_id, inserted_ts, interface_name, hl7_lab_interface_id, bill_paid, op_code, center_id, out_house_sample)
					VALUES ('TEST', NEW.prescribed_id, current_timestamp, outInterface.interface_name, outInterface.hl7_lab_interface_id, bill_paid_status, 'C', patientCenterId, TRUE);
				ELSIF (OLD.conduction_type = 'o' AND NEW.conduction_type = 'i') THEN
					INSERT INTO hl7_export_items (item_type, item_id, inserted_ts, interface_name, hl7_lab_interface_id, bill_paid, op_code, center_id, out_house_sample)
					VALUES ('TEST', NEW.prescribed_id, current_timestamp, outInterface.interface_name, outInterface.hl7_lab_interface_id, 'N', 'N', patientCenterId, TRUE);	
				ELSIF (OLD.conduction_type = 'i' AND NEW.conduction_type = 'i' AND OLD.outsource_dest_id is NULL AND NEW.outsource_dest_id IS NOT NULL ) THEN
					FOR testInterface IN 
						SELECT hli.interface_name, hli.hl7_lab_interface_id
								FROM hl7_lab_interfaces hli
								JOIN diagnostics_export_interface dei USING(hl7_lab_interface_id)
								WHERE dei.test_id = NEW.test_id AND hli.status ='A'
						LOOP
								INSERT INTO hl7_export_items (item_type, item_id, inserted_ts, interface_name, hl7_lab_interface_id, bill_paid, op_code, center_id)
								VALUES ('TEST', NEW.prescribed_id, current_timestamp, testInterface.interface_name, testInterface.hl7_lab_interface_id, 'Y', 'C', patientCenterId);
					END LOOP;
					INSERT INTO hl7_export_items (item_type, item_id, inserted_ts, interface_name, hl7_lab_interface_id, bill_paid, op_code, center_id, out_house_sample)
					VALUES ('TEST', NEW.prescribed_id, current_timestamp, outInterface.interface_name, outInterface.hl7_lab_interface_id, 'N', 'N', patientCenterId, TRUE);		
				END IF;
		END IF;
	END IF;
	RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;


DROP TRIGGER IF EXISTS hl7_save_test_export_items_trigger ON tests_prescribed;
CREATE TRIGGER hl7_save_test_export_items_trigger
AFTER INSERT OR UPDATE ON tests_prescribed
FOR EACH ROW
EXECUTE PROCEDURE hl7_save_test_export_items();

--
-- Views used by Navision/SAP. aev stands for Accounting Export View
--
DROP VIEW IF EXISTS aev_bill CASCADE;
CREATE VIEW aev_bill AS
SELECT b.bill_no,
	COALESCE (pr.mr_no, prc.customer_id, isr.incoming_visit_id) AS cust_id,
	COALESCE (smb.salutation || ' ' || pd.patient_name
		|| CASE WHEN coalesce(pd.middle_name, '') = '' THEN '' ELSE (' ' || pd.middle_name) end
		|| CASE WHEN coalesce(pd.last_name, '') = '' THEN '' ELSE (' ' || pd.last_name) end,
    prc.customer_name, isr.patient_name)::character varying AS bill_patient_full_name,
	date(finalized_date) AS finalized_date, COALESCE(pr.center_id, prc.center_id) AS center_id,
	CASE WHEN b.is_tpa THEN pr.primary_sponsor_id ELSE NULL END AS primary_sponsor_id,
	CASE WHEN b.is_tpa THEN pr.secondary_sponsor_id ELSE NULL END AS secondary_sponsor_id,
	CASE WHEN b.is_tpa THEN ptpa.tpa_name ELSE NULL END AS primary_sponsor_name,
	CASE WHEN b.is_tpa THEN stpa.tpa_name ELSE NULL END AS secondary_sponsor_name,
	CASE WHEN b.is_tpa THEN COALESCE(ppd.member_id,pcd.employee_id,pnd.national_id) ELSE NULL END
		AS primary_member_id,
	CASE WHEN b.is_tpa THEN COALESCE(spd.member_id,scd.employee_id,snd.national_id) ELSE NULL END
		AS secondary_member_id,
	ipm.plan_name,
	(CASE WHEN b.visit_type = 'i' THEN 'IP' WHEN b.visit_type = 'o' THEN 'OP'
		WHEN b.visit_type = 'r' THEN 'Retail' WHEN b.visit_type = 't' THEN 'Test' END)::character varying
			AS visit_type,
	b.mod_time
FROM bill b
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
	LEFT JOIN salutation_master smb ON (smb.salutation_id = pd.salutation)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
	LEFT JOIN tpa_master ptpa ON (ptpa.tpa_id = pr.primary_sponsor_id)
	LEFT JOIN tpa_master stpa ON (stpa.tpa_id = pr.secondary_sponsor_id)
	LEFT JOIN patient_policy_details ppd ON (ppd.patient_policy_id = pr.patient_policy_id)
	LEFT JOIN patient_corporate_details pcd ON (pcd.patient_corporate_id = pr.patient_corporate_id)
	LEFT JOIN patient_national_sponsor_details pnd ON
		(pnd.patient_national_sponsor_id = pr.patient_national_sponsor_id)
	LEFT JOIN patient_policy_details spd ON (spd.patient_policy_id = pr.patient_policy_id)
	LEFT JOIN patient_corporate_details scd ON (scd.patient_corporate_id = pr.secondary_patient_corporate_id)
	LEFT JOIN patient_national_sponsor_details snd ON
		(snd.patient_national_sponsor_id = pr.secondary_patient_national_sponsor_id)
	LEFT JOIN insurance_plan_main ipm ON (ipm.plan_id = pr.plan_id)
WHERE b.status NOT IN ('A','X') AND ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) );

DROP VIEW IF EXISTS aev_bill_lines CASCADE;
CREATE VIEW aev_bill_lines AS
-- hospital items
SELECT b.bill_no, bc.charge_id, pr.center_id, hcm.center_name,
	COALESCE (pr.mr_no, prc.customer_id, isr.incoming_visit_id) AS cust_id,
	COALESCE (smb.salutation || ' ' || pd.patient_name
		|| CASE WHEN coalesce(pd.middle_name, '') = '' THEN '' ELSE (' ' || pd.middle_name) end
		|| CASE WHEN coalesce(pd.last_name, '') = '' THEN '' ELSE (' ' || pd.last_name) end,
    prc.customer_name, isr.patient_name)::character varying AS bill_patient_full_name,
	bc.charge_head, chc.chargehead_name, bc.charge_group, cgc.chargegroup_name,
	chc.account_head_id, bahc.account_head_name AS ac_head,
	NULL AS store_id, NULL AS store_name,
	bc.act_description_id::character varying AS item_id, act_description AS description,
	sid.issue_units AS uom, tdep.dept_name AS department,
	act_quantity AS quantity, act_rate AS rate, bc.discount, bc.amount,
	bc.doctor_amount, bc.payee_doctor_id AS doctor_id, doc.doctor_name,
	bc.act_remarks AS activity_details, bc.user_remarks,
	ssg.service_group_id, sg.service_group_name,
	bc.service_sub_group_id, ssg.service_sub_group_name,
	CASE WHEN bc.charge_head IN ('INVITE','INVRET') THEN 'Y'::character varying ELSE 'N'::character varying
		END AS is_inventory_item,
	coalesce(id.cost_value, rd.cost_value, 0) AS cost_value,
	coalesce(sid.issue_base_unit, 1) AS package_size,
	sid.med_category_id AS item_category_id, scm.category AS item_category_name,
	b.mod_time
FROM bill_charge bc
	JOIN bill b USING (bill_no)
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
	LEFT JOIN salutation_master smb ON (smb.salutation_id = pd.salutation)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
	LEFT JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head)
	LEFT JOIN chargegroup_constants cgc ON (cgc.chargegroup_id = chc.chargegroup_id)
	LEFT JOIN bill_account_heads bahc ON (bahc.account_head_id = chc.account_head_id)
	LEFT JOIN treating_departments_view tdep ON (tdep.dept_id = bc.act_department_id)
	LEFT JOIN doctors doc ON (doc.doctor_id = bc.payee_doctor_id)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id = bc.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id = ssg.service_group_id)

	LEFT JOIN bill_activity_charge ibac ON (ibac.activity_code = 'PHI' AND ibac.charge_id = bc.charge_id
		AND bc.charge_head = 'INVITE')
	LEFT JOIN stock_issue_details id ON (ibac.activity_id::integer = id.item_issue_no)
	LEFT JOIN stock_issue_main im ON (id.user_issue_no = im.user_issue_no)

	LEFT JOIN bill_activity_charge rbac ON (rbac.activity_code = 'PHI' AND rbac.charge_id = bc.charge_id
		AND bc.charge_head = 'INVRET')
	LEFT JOIN store_issue_returns_details rd ON (rbac.activity_id::integer = rd.item_return_no)
	LEFT JOIN store_issue_returns_main rm ON (rd.user_return_no = rm.user_return_no)

	LEFT JOIN store_item_details sid ON (sid.medicine_id::text = bc.act_description_id
		AND bc.charge_head IN ('INVITE','INVRET'))
	LEFT JOIN store_category_master scm ON (sid.med_category_id = scm.category_id)
WHERE bc.charge_head NOT IN ('PHMED','PHCMED','PHRET','PHCRET')
	AND b.status NOT IN ('A','X') AND bc.status != 'X' AND ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) )

UNION ALL
-- pharmacy items
SELECT sm.bill_no, 'PI' || s.sale_item_id, st.center_id, hcm.center_name,
	COALESCE (pr.mr_no, prc.customer_id, isr.incoming_visit_id) AS cust_id,
	COALESCE (smb.salutation || ' ' || pd.patient_name
		|| CASE WHEN coalesce(pd.middle_name, '') = '' THEN '' ELSE (' ' || pd.middle_name) end
		|| CASE WHEN coalesce(pd.last_name, '') = '' THEN '' ELSE (' ' || pd.last_name) end,
    prc.customer_name, isr.patient_name) AS bill_patient_full_name,
	bc.charge_head, chc.chargehead_name, bc.charge_group, cgc.chargegroup_name,
	chc.account_head_id, bahc.account_head_name AS ac_head,
	sm.store_id, st.dept_name AS store_name,
	s.medicine_id::character varying AS item_id, m.medicine_name AS description,
	m.issue_units AS uom, 'PHARMACY' AS department,
	s.quantity, s.rate, s.disc AS discount, s.amount,
	0 AS doctor_amount, NULL AS doctor_id, NULL AS doctor_name,
	NULL AS activity_details, NULL AS user_remarks,
	ssg.service_group_id, sg.service_group_name,
	bc.service_sub_group_id, ssg.service_sub_group_name,
	'Y' AS is_inventory_item,
	s.cost_value, m.issue_base_unit AS package_size,
	m.med_category_id AS item_category_id, scm.category AS item_category_name,
	sm.date_time AS mod_time
FROM store_sales_details s
	JOIN store_sales_main sm ON (s.sale_id = sm.sale_id)
	JOIN stores st ON (st.dept_id = sm.store_id)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = st.center_id)
	JOIN bill b USING (bill_no)
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
	LEFT JOIN salutation_master smb ON (smb.salutation_id = pd.salutation)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
	JOIN store_item_details m ON (s.medicine_id = m.medicine_id)
	JOIN store_category_master scm ON (m.med_category_id = scm.category_id)
	JOIN bill_charge bc ON (bc.charge_id= sm.charge_id)
	JOIN chargehead_constants chc ON (chc.chargehead_id = bc.charge_head)
	JOIN chargegroup_constants cgc ON (cgc.chargegroup_id = chc.chargegroup_id)
	JOIN bill_account_heads bahc ON (bahc.account_head_id = chc.account_head_id)
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id = m.service_sub_group_id)
	LEFT JOIN service_groups sg ON (sg.service_group_id = ssg.service_group_id)
WHERE ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) )

UNION ALL
-- pharmacy discounts
SELECT sm.bill_no, 'PD' || sm.sale_id, pr.center_id, hcm.center_name,
	COALESCE (pr.mr_no, prc.customer_id, isr.incoming_visit_id) AS cust_id,
	COALESCE (smb.salutation || ' ' || pd.patient_name
		|| CASE WHEN coalesce(pd.middle_name, '') = '' THEN '' ELSE (' ' || pd.middle_name) end
		|| CASE WHEN coalesce(pd.last_name, '') = '' THEN '' ELSE (' ' || pd.last_name) end,
    prc.customer_name, isr.patient_name) AS bill_patient_full_name,
	'BIDIS' AS charge_head, chc.chargehead_name, 'DIS' AS charge_group, cgc.chargegroup_name,
	chc.account_head_id, bahc.account_head_name AS ac_head,
	sm.store_id, st.dept_name AS store_name,
	sm.sale_id AS item_id, 'Pharmacy Bill Level Discounts' AS description, NULL AS uom,
	'PHARMACY' AS department,
	1 AS quantity, sm.discount AS rate, sm.discount AS discount, (0-sm.discount) AS amount,
	0 AS doctor_amount, NULL AS doctor_id, NULL AS doctor_name,
	NULL AS activity_details, NULL AS user_remarks,
	ssg.service_group_id, sg.service_group_name,
	0 AS service_sub_group_id, ssg.service_sub_group_name,
	'N' AS is_inventory_item,
	0 AS cost_value, 1 AS package_size,
	NULL AS item_category_id, NULL AS item_category_name,
	sm.date_time AS mod_time
FROM store_sales_main sm
	JOIN stores st ON (st.dept_id = sm.store_id)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = st.center_id)
	JOIN bill b USING (bill_no)
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
	LEFT JOIN salutation_master smb ON (smb.salutation_id = pd.salutation)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
	JOIN chargehead_constants chc ON (chc.chargehead_id = 'BIDIS')
	JOIN chargegroup_constants cgc ON (cgc.chargegroup_id = 'DIS')
	JOIN bill_account_heads bahc ON (bahc.account_head_id = chc.account_head_id)
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id = 0)
	LEFT JOIN service_groups sg ON (sg.service_group_id = ssg.service_group_id)
WHERE sm.discount != 0 AND ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) )

UNION ALL
-- pharmacy round offs
SELECT sm.bill_no, 'RO' || sm.sale_id, pr.center_id, hcm.center_name,
	COALESCE (pr.mr_no, prc.customer_id, isr.incoming_visit_id) AS cust_id,
	COALESCE (smb.salutation || ' ' || pd.patient_name
		|| CASE WHEN coalesce(pd.middle_name, '') = '' THEN '' ELSE (' ' || pd.middle_name) end
		|| CASE WHEN coalesce(pd.last_name, '') = '' THEN '' ELSE (' ' || pd.last_name) end,
    prc.customer_name, isr.patient_name) AS bill_patient_full_name,
	'ROF' AS charge_head, chc.chargehead_name, 'DIS' AS charge_group, cgc.chargegroup_name,
	chc.account_head_id, bahc.account_head_name AS ac_head,
	sm.store_id, st.dept_name AS store_name,
	sm.sale_id AS item_id, 'Pharmacy Round Offs' AS description, NULL AS uom,
	'PHARMACY' AS department,
	1 AS quantity, sm.round_off AS rate, 0 AS discount, sm.round_off AS amount,
	0 AS doctor_amount, NULL AS doctor_id, NULL AS doctor_name,
	NULL AS activity_details, NULL AS user_remarks,
	ssg.service_group_id, sg.service_group_name,
	0 AS service_sub_group_id, ssg.service_sub_group_name,
	'N' AS is_inventory_item,
	0 AS cost_value, 1 AS package_size,
	NULL AS item_category_id, NULL AS item_category_name,
	sm.date_time AS mod_time
FROM store_sales_main sm
	JOIN stores st ON (st.dept_id = sm.store_id)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = st.center_id)
	JOIN bill b USING (bill_no)
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
	LEFT JOIN salutation_master smb ON (smb.salutation_id = pd.salutation)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
	JOIN chargehead_constants chc ON (chc.chargehead_id = 'ROF')
	JOIN chargegroup_constants cgc ON (cgc.chargegroup_id = 'DIS')
	JOIN bill_account_heads bahc ON (bahc.account_head_id = chc.account_head_id)
	LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id = 0)
	LEFT JOIN service_groups sg ON (sg.service_group_id = ssg.service_group_id)
WHERE sm.round_off != 0 AND ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) )
;

DROP VIEW IF EXISTS aev_receipts CASCADE;
CREATE VIEW aev_receipts AS
SELECT r.receipt_id, r.display_date AS receipt_date, r.remarks AS description,
	br.bill_no, COALESCE (pr.mr_no, prc.customer_id, isr.incoming_visit_id) AS cust_id,
	r.amount, r.payment_mode_id, pmm.payment_mode AS payment_mode_name, r.reference_no,
	r.currency_id, fc.currency AS currency_name,
	r.counter AS counter_id, counter_no AS counter_name, c.center_id, center_name,
	br.mod_time
FROM receipts r
	JOIN bill_receipts br ON (br.receipt_no=r.receipt_id)
	JOIN bill b ON (b.bill_no = br.bill_no)
	LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id)
	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = b.visit_id)
	LEFT JOIN counters c ON (c.counter_id = r.counter)
	LEFT JOIN payment_mode_master pmm ON (pmm.mode_id = r.payment_mode_id)
	LEFT JOIN foreign_currency fc ON (fc.currency_id = r.currency_id)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = c.center_id) WHERE NOT is_deposit
;

DROP VIEW IF EXISTS aev_deposit_receipts CASCADE;
CREATE VIEW aev_deposit_receipts AS
SELECT drv.receipt_no, drv.display_date AS receipt_date, drv.remarks AS description,
	NULL::varchar AS bill_no, drv.mr_no AS cust_id,
	drv.amount, drv.payment_mode_id, drv.payment_mode AS payment_mode_name, drv.reference_no,
	drv.currency_id, drv.currency AS currency_name,
	drv.counter AS counter_id, drv.counter_no AS counter_name, drv.center_id, hcm.center_name,
	drv.mod_time
FROM deposits_receipts_view drv
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = drv.center_id);

DROP VIEW IF EXISTS aev_store_purchases CASCADE;
CREATE VIEW aev_store_purchases AS
SELECT gm.grn_no, gm.grn_date,
	gm.store_id, s.dept_name AS store_name, s.center_id, hcm.center_name,
	sm.supplier_name,sm.cust_supplier_code,
	(CASE WHEN gm.debit_note_no IS NOT NULL THEN 'Debit Note' ELSE 'Purchase' END)::character varying AS type,
	coalesce(gm.supplier_invoice_id::character varying, dn.debit_note_no) AS invoice_id,
	coalesce(si.supplier_id, dn.supplier_id) AS supplier_id,
	coalesce(si.invoice_date, dn.debit_note_date) AS invoice_date,
	si.invoice_no, si.po_no, si.po_reference AS invoice_reference,
	coalesce(si.other_charges,dn.other_charges) AS other_charges,
	coalesce(si.discount,dn.discount) AS discount, coalesce(si.round_off,dn.round_off) AS round_off,
	coalesce(si.status,dn.status) AS status, coalesce(si.date_time,dn.date_time) AS mod_time
FROM store_grn_main gm
	LEFT JOIN stores s ON (s.dept_id = gm.store_id)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = s.center_id)
	LEFT JOIN store_invoice si ON (si.supplier_invoice_id = gm.supplier_invoice_id)
	LEFT JOIN store_debit_note dn ON (dn.debit_note_no = gm.debit_note_no)
	LEFT JOIN supplier_master sm ON (sm.supplier_code = coalesce(si.supplier_id,dn.supplier_id))
WHERE coalesce(si.status,dn.status) != 'O'
;

DROP VIEW IF EXISTS aev_store_purchase_lines CASCADE;
CREATE VIEW aev_store_purchase_lines AS
SELECT gm.grn_no, gm.grn_date,
	gm.store_id, s.dept_name AS store_name, s.center_id, hcm.center_name,
	sm.supplier_name,sm.cust_supplier_code,
	(CASE WHEN gm.debit_note_no IS NOT NULL THEN 'Debit Note' ELSE 'Purchase' END)::character varying AS type,
	coalesce(gm.supplier_invoice_id::character varying, dn.debit_note_no) AS invoice_id,
	coalesce(si.supplier_id, dn.supplier_id) AS supplier_id,
	g.medicine_id AS item_id, sid.medicine_name AS item_name, g.batch_no,
	(g.billed_qty/g.grn_pkg_size) AS billed_qty, (g.bonus_qty/g.grn_pkg_size) AS bonus_qty,
	sid.issue_units AS uom, sid.package_uom, g.cost_price AS cost_price, g.discount AS discount, g.tax AS tax,
	g.item_ced AS ced,
	(g.billed_qty/g.grn_pkg_size*g.cost_price - g.discount + g.tax + g.item_ced) AS net_amount,
	sid.med_category_id AS item_category_id, scm.category AS item_category_name,
	coalesce(si.date_time,dn.date_time) AS mod_time
FROM store_grn_details g
	JOIN store_item_details sid ON (sid.medicine_id = g.medicine_id)
	JOIN store_category_master scm ON (sid.med_category_id = scm.category_id)
	JOIN store_grn_main gm ON (gm.grn_no = g.grn_no)
	LEFT JOIN stores s ON (s.dept_id = gm.store_id)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = s.center_id)
	LEFT JOIN store_invoice si ON (si.supplier_invoice_id = gm.supplier_invoice_id)
	LEFT JOIN store_debit_note dn ON (dn.debit_note_no = gm.debit_note_no)
	LEFT JOIN supplier_master sm ON (sm.supplier_code = coalesce(si.supplier_id,dn.supplier_id))
WHERE coalesce(si.status,dn.status) != 'O'
;

DROP VIEW IF EXISTS aev_sponsor_claims CASCADE;
CREATE VIEW aev_sponsor_claims AS
SELECT b.bill_no, pr.mr_no, b.finalized_date, b.primary_total_claim, b.secondary_total_claim,
	pr.primary_sponsor_id, pr.secondary_sponsor_id,
	prp.tpa_name AS primary_sponsor_name, prs.tpa_name AS secondary_sponsor_name,
	primary_insurance_co, secondary_insurance_co,
	icp.insurance_co_name AS primary_insurance_co_name, icp.insurance_co_name AS secondary_insurance_co_name,
	pr.center_id, hcm.center_name,
	b.mod_time
FROM bill b
	JOIN patient_registration pr ON (pr.patient_id = b.visit_id)
	LEFT JOIN tpa_master prp ON (prp.tpa_id = pr.primary_sponsor_id)
	LEFT JOIN tpa_master prs ON (prs.tpa_id = pr.secondary_sponsor_id)
	LEFT JOIN insurance_company_master icp ON (icp.insurance_co_id = pr.primary_insurance_co)
	LEFT JOIN insurance_company_master ics ON (ics.insurance_co_id = pr.secondary_insurance_co)
	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
WHERE b.is_tpa AND b.status NOT IN ('A','X');

DROP VIEW IF EXISTS aev_store_transfers CASCADE;
CREATE VIEW aev_store_transfers AS
SELECT std.medicine_id AS item_id, stm.date_time AS transaction_date, transfer_no,
	sid.medicine_name AS item_name,
	stm.store_from AS from_store_id, sf.dept_name AS from_store_name,
	stm.store_to AS to_store_id, st.dept_name AS to_store_name,
	std.qty AS quantity, sid.issue_units AS uom, std.cost_value/std.qty AS unit_cost,
	std.cost_value AS net_amount,
	sf.center_id AS from_center_id, sfc.center_name AS from_center_name,
	st.center_id AS to_center_id, stc.center_name AS to_center_name,
	stm.date_time AS mod_time
FROM store_transfer_details std
	JOIN store_transfer_main stm USING (transfer_no)
	JOIN store_item_details sid USING (medicine_id)
	JOIN stores sf ON (sf.dept_id = stm.store_from)
	JOIN hospital_center_master sfc ON (sfc.center_id = sf.center_id)
	JOIN stores st ON (st.dept_id = stm.store_to)
	JOIN hospital_center_master stc ON (stc.center_id = st.center_id)
;

DROP VIEW IF EXISTS aev_store_issues_returns CASCADE;
CREATE VIEW aev_store_issues_returns AS
SELECT id.medicine_id AS item_id, im.date_time AS transaction_date, 'Issue'::character varying AS type,
	im.user_issue_no AS issue_no, sid.medicine_name AS item_name,
	dept_from AS from_store_id, sf.dept_name AS from_store_name,
	sf.center_id AS from_center_id, sfc.center_name AS from_center_name,
	id.qty AS quantity, sid.issue_units AS uom, id.cost_value/id.qty AS unit_cost,
	id.cost_value AS net_amount,
	issued_to,
	im.date_time AS mod_time
FROM stock_issue_details id
	JOIN stock_issue_main im USING (user_issue_no)
	JOIN store_item_details sid USING (medicine_id)
	JOIN stores sf ON (sf.dept_id = im.dept_from)
	JOIN hospital_center_master sfc ON (sfc.center_id = sf.center_id)
WHERE im.user_type != 'Patient'

UNION ALL
SELECT ird.medicine_id AS item_id, im.date_time AS transaction_date, 'Return'::character varying AS type,
	irm.user_return_no AS issue_no, sid.medicine_name AS item_name,
	dept_to AS from_store_id, sf.dept_name AS from_store_name,
	sf.center_id AS from_center_id, sfc.center_name AS from_center_name,
	ird.qty AS quantity, sid.issue_units AS uom, ird.cost_value/ird.qty AS unit_cost,
	ird.cost_value AS net_amount,
	returned_by,
	irm.date_time AS mod_time
FROM store_issue_returns_details ird
	JOIN store_issue_returns_main irm USING (user_return_no)
	JOIN store_item_details sid USING (medicine_id)
	JOIN stores sf ON (sf.dept_id = irm.dept_to)
	JOIN hospital_center_master sfc ON (sfc.center_id = sf.center_id)
	JOIN stock_issue_main im ON (im.user_issue_no = irm.user_issue_no)
WHERE im.user_type != 'Patient'
;

--
-- Accounting views END here.
--

DROP VIEW IF EXISTS all_store_rate_plans_view  CASCADE;
CREATE VIEW all_store_rate_plans_view AS
SELECT store_rate_plan_id
FROM store_rate_plans;

DROP VIEW IF EXISTS missing_store_item_charges_view;
CREATE VIEW missing_store_item_charges_view AS
SELECT store_rate_plan_id,medicine_id
FROM all_store_rate_plans_view osir
CROSS JOIN store_item_details
WHERE NOT EXISTS (
	SELECT * FROM store_item_rates isir WHERE isir.store_rate_plan_id = osir.store_rate_plan_id
);

DROP VIEW IF EXISTS patient_return_indentable_items CASCADE;
CREATE VIEW patient_return_indentable_items AS
SELECT medicine_id,medicine_name,manf_name,category,package_uom,issue_base_unit,
		issue_units,visit_id,sum(qty) AS qty_avbl,package_type,store_id,process_type
FROM (
	SELECT sid.medicine_id,sid.medicine_name,m.manf_name,scm.category,sid.package_uom,sid.issue_base_unit,
		sid.issue_units,b.visit_id,quantity AS qty,package_type,store_id,'S' AS process_type
		FROM store_sales_main ssm
		JOIN bill b USING(bill_no)
		JOIN store_sales_details ssd USING(sale_id)
		JOIN store_item_details sid USING(medicine_id)
		JOIN manf_master m ON ( sid.manf_name = m.manf_code )
		JOIN store_category_master scm ON med_category_id=category_id
	UNION ALL
	SELECT sid.medicine_id,sid.medicine_name,m.manf_name,scm.category,
		sid.package_uom,sid.issue_base_unit,sid.issue_units,issued_to AS visit_id,
		qty-return_qty AS qty,package_type,dept_from AS store_id,'I' AS process_type
		FROM stock_issue_main ssm
		JOIN stock_issue_details ssd USING(user_issue_no)
		JOIN store_item_details sid USING(medicine_id)
		JOIN manf_master m ON ( sid.manf_name = m.manf_code )
		JOIN store_category_master scm ON med_category_id=category_id
	) AS foo
	GROUP BY medicine_id,medicine_name,manf_name,category,package_uom,issue_base_unit,
		issue_units,visit_id,package_type,store_id,process_type;

DROP VIEW IF EXISTS  insurance_estimate_view;
CREATE OR REPLACE  VIEW insurance_estimate_view as
SELECT sum(EST.AMT) AS AMT,estimate_id,insurance_id
FROM insurance_estimate est
 GROUP BY estimate_id,insurance_id;

--- Dyna package audit log view

DROP VIEW IF EXISTS dyna_package_charges_audit_log_view CASCADE;
CREATE OR REPLACE VIEW dyna_package_charges_audit_log_view AS
	(SELECT dpcal.*,dp.dyna_package_name
		FROM dyna_package_charges_audit_log dpcal
		LEFT JOIN dyna_packages dp using(dyna_package_id)
	);


DROP VIEW IF EXISTS dyna_package_category_limits_audit_log_view CASCADE;
CREATE OR REPLACE VIEW dyna_package_category_limits_audit_log_view AS
	(SELECT dpclal.*,dp.dyna_package_name
		FROM dyna_package_category_limits_audit_log dpclal
		LEFT JOIN dyna_packages dp using(dyna_package_id)
	);


-- this view contains only the joins which are used to get the fields for displaying in patient demography.
-- do not add extra joins to get the other fields not used in patient demography, instead get separately from database.
DROP VIEW IF EXISTS patient_details_header_tag_view CASCADE;
CREATE OR REPLACE VIEW patient_details_header_tag_view AS
SELECT
	/* Patient fields  */
	pd.mr_no, pd.salutation AS salutation_id, sm.salutation, pd.patient_name, pd.middle_name, pd.last_name,
	sm.salutation || ' ' || patient_name
        || CASE WHEN coalesce(middle_name, '') = '' THEN '' ELSE (' ' || middle_name) end
        || CASE WHEN coalesce(last_name, '') = '' THEN '' ELSE (' ' || last_name) END as full_name,
	pd.patient_gender, pd.dateofbirth, COALESCE(pd.dateofbirth, pd.expected_dob) AS expected_dob,
	''::text AS age_text,
	pd.patient_phone, pd.patient_phone2 AS addnl_phone, pd.patient_address, pd.patient_area,
	pd.oldmrno, pd.casefile_no, pd.remarks,
	pd.patient_care_oftext, pd.patient_careof_address, pd.relation, pd.next_of_kin_relation,
	pd.death_date, pd.death_time,pd.dead_on_arrival,
	pd.custom_field1, pd.custom_field2, pd.custom_field3, pd.custom_field4, pd.custom_field5, pd.custom_field6,
	pd.custom_field7, pd.custom_field8, pd.custom_field9, pd.custom_field10, pd.custom_field11, pd.custom_field12,
	pd.custom_field13,custom_field14,pd.custom_field15,pd.custom_field16,pd.custom_field17,
	pd.custom_field18,pd.custom_field19, pd.original_mr_no,ad.isbaby,

	pd.custom_list1_value, pd.custom_list2_value, pd.custom_list3_value,pd.custom_list4_value,
	custom_list5_value,custom_list6_value,custom_list7_value,custom_list8_value,custom_list9_value,

	-- visit custom field values
	pra.visit_custom_list1, pra.visit_custom_list2,

	-- visit custom list values
	pra.visit_custom_field1, pra.visit_custom_field2, pra.visit_custom_field3,pra.visit_custom_field4,pra.visit_custom_field5,pra.visit_custom_field6,
	pra.visit_custom_field7,pra.visit_custom_field8,pra.visit_custom_field9,

	pd.patient_category_id, pd.category_expiry_date, pcm.category_name, pd_pcm.category_name AS patient_category_name,
	pd.patient_consultation_info,
	CASE WHEN pd.patient_photo IS NULL THEN 'N' ELSE 'Y' END AS patient_photo_available,
	pd.previous_visit_id, pd.visit_id, pd.no_allergies, pd.med_allergies, pd.food_allergies, pd.other_allergies, pd.vip_status,
	pd.government_identifier, pd.identifier_id, pd.portal_access, pd.email_id,
	pd.passport_no, pd.passport_validity, pd.passport_issue_country, pd.visa_validity, pd.family_id,
	pd.mod_time AS patient_mod_time,
	/* Visit related fields */
	pra.patient_id, pra.status AS visit_status, pra.visit_type, pra.revisit, pra.reg_date, pra.reg_time,
	pra.op_type, otn.op_type_name, pra.main_visit_id,
	pra.mlc_status, pra.patient_category_id AS patient_category,
	pra.patient_care_oftext AS patcontactperson, pra.relation AS patrelation,
	pra.patient_careof_address AS pataddress,
	pra.complaint, pra.analysis_of_complaint,
	pra.doctor, dr.doctor_name,
	pra.admitted_dept, admdep.dept_name AS admitted_dept_name, /* original dept admitted to */
	pra.dept_name AS dept_id, dep.dept_name,  pra.unit_id, dum.unit_name,	/* current dept */
	pra.org_id, od.org_name, od.store_rate_plan_id,
	pra.bed_type AS bill_bed_type, bn.bed_type AS alloc_bed_type, bn.bed_name AS alloc_bed_name,
	pra.ward_id AS reg_ward_id, wnr.ward_name AS reg_ward_name, wn.ward_name AS alloc_ward_name,
	ad.admit_date AS bed_start_date, date(ad.finalized_time) AS bed_end_date,
	pra.discharge_doc_id AS dis_doc_id, pra.discharge_format AS dis_format,
	pra.discharge_flag, pra.discharge_doctor_id, pra.discharge_date, pra.discharge_time,
	pra.discharge_finalized_date AS dis_finalized_date, pra.discharge_finalized_time AS dis_finalized_time,
	pra.discharge_finalized_user AS dis_finalized_user, pra.discharged_by, pra.user_name AS admitted_by,
	pra.codification_status,pra.established_type, pra.disch_date_for_disch_summary, pra.disch_time_for_disch_summary,
	pra.reference_docto_id, COALESCE(drs.doctor_name, rd.referal_name) AS refdoctorname,
	pra.reg_charge_accepted,pra.ip_credit_limit_amount,pra.use_drg, pra.drg_code, pra.use_perdiem, pra.per_diem_code,
	pra.mlc_no, pra.mlc_type, pra.accident_place, pra.police_stn, pra.mlc_remarks, pra.certificate_status,
	pmd.icd_code AS primary_diagnosis_code, pmd.description AS primary_diagnosis_description,
	(select textcat_commacat(description) from mrd_diagnosis md where (md.visit_id=pra.patient_id and diag_type='S'))
	as secondary_diagnosis_description,
	pra.primary_insurance_approval,pra.secondary_insurance_approval,
	pra.primary_sponsor_id, pra.secondary_sponsor_id,
	tpa.tpa_name, stpa.tpa_name AS sec_tpa_name,
	pst.sponsor_type_name AS sponsor_type , sst.sponsor_type_name AS sec_sponsor_type,
	icm.insurance_co_name, icm.insurance_co_address,
	sicm.insurance_co_name AS sec_insurance_co_name, sicm.insurance_co_address AS sec_insurance_co_address,
	pra.insurance_id, pra.category_id AS insurance_category,
	pra.plan_id, pra.prior_auth_id, pra.prior_auth_mode_id,
	pra.doc_id, pra.primary_insurance_co, pra.secondary_insurance_co,
	icam.category_name AS plan_type_name, ipm.plan_exclusions, ipm.plan_notes, ipm.plan_name,
	pra.patient_policy_id,pra.docs_download_passcode,
	ppd.member_id AS member_id,
	ppd.policy_number AS policy_number,
	ppd.policy_validity_start AS policy_validity_start,
	ppd.policy_validity_end AS policy_validity_end,
	ppd.policy_holder_name AS policy_holder_name,
	ppd.patient_relationship AS patient_relationship,
	pcd.patient_relationship AS patient_corporate_relation, pcd.sponsor_id AS corporate_sponsor_id,
	pcd.employee_id, pcd.employee_name, pnd.sponsor_id AS national_sponsor_id,
	pnd.national_id, pnd.citizen_name, pnd.patient_relationship AS patient_national_relation,
	spcd.patient_relationship AS sec_patient_corporate_relation, spcd.sponsor_id AS sec_corporate_sponsor_id,
	spcd.employee_id AS sec_employee_id, spcd.employee_name AS sec_employee_name, spnd.sponsor_id AS sec_national_sponsor_id,
	spnd.national_id AS sec_national_id, spnd.citizen_name AS sec_citizen_name ,spnd.patient_relationship AS sec_patient_national_relation
	, pra.signatory_username,pra.collection_center_id, coalesce(require_pbm_authorization, 'N') AS require_pbm_authorization,
	   pst.member_id_label AS primary_member_id_label, sst.member_id_label AS secondary_member_id_label,
	   pst.plan_type_label AS primary_plan_type_label, sst.plan_type_label AS secondary_plan_type_label, ds.discharge_state_name,
	case WHEN cpref.lang_code is not NULL THEN cpref.lang_code ELSE (select contact_pref_lang_code from generic_preferences) END AS contact_pref_lang_code,
    pd.nationality_id,nc.country_name AS nationality_name
FROM patient_registration pra
   JOIN patient_details pd ON pra.mr_no = pd.mr_no
   LEFT JOIN op_type_names otn ON (otn.op_type = pra.op_type)
   LEFT JOIN ward_names wnr ON wnr.ward_no = pra.ward_id
   LEFT JOIN discharge_state_names ds ON (ds.discharge_state = pra.patient_discharge_status)
   LEFT JOIN salutation_master sm ON pd.salutation = sm.salutation_id
   LEFT JOIN country_master nc ON pd.nationality_id = nc.country_id
   LEFT JOIN department dep ON pra.dept_name = dep.dept_id
   LEFT JOIN department admdep ON pra.admitted_dept = admdep.dept_id
   LEFT JOIN dept_unit_master dum ON dum.unit_id = pra.unit_id
   LEFT JOIN doctors dr ON dr.doctor_id = pra.doctor
   LEFT JOIN admission ad ON ad.patient_id = pra.patient_id
   LEFT JOIN bed_names bn ON bn.bed_id = ad.bed_id
   LEFT JOIN ward_names wn ON wn.ward_no = bn.ward_no
   LEFT JOIN organization_details od ON pra.org_id = od.org_id
   LEFT JOIN doctors drs ON pra.reference_docto_id = drs.doctor_id
   LEFT JOIN referral rd ON pra.reference_docto_id = rd.referal_no
   LEFT JOIN patient_category_master pcm ON pcm.category_id = pra.patient_category_id
   LEFT JOIN patient_category_master pd_pcm ON pd_pcm.category_id=pd.patient_category_id
   LEFT JOIN tpa_master tpa ON tpa.tpa_id = pra.primary_sponsor_id
   LEFT JOIN tpa_master stpa ON stpa.tpa_id = pra.secondary_sponsor_id
   LEFT JOIN insurance_company_master icm ON icm.insurance_co_id = pra.primary_insurance_co
   LEFT JOIN insurance_company_master sicm ON sicm.insurance_co_id = pra.secondary_insurance_co
   LEFT JOIN insurance_plan_main ipm ON (pra.plan_id = ipm.plan_id)
   LEFT JOIN insurance_category_master icam  ON icam.category_id=ipm.category_id
   LEFT JOIN mrd_diagnosis pmd ON (pmd.visit_id = pra.patient_id AND pmd.diag_type = 'P')
   LEFT JOIN patient_insurance_plans ppip ON(ppip.patient_id=pra.patient_id AND ppip.priority = 1)
   LEFT JOIN patient_policy_details ppd ON (ppd.patient_policy_id = ppip.patient_policy_id)
   LEFT JOIN patient_corporate_details pcd ON (pcd.patient_corporate_id = pra.patient_corporate_id)
   LEFT JOIN patient_national_sponsor_details pnd ON (pnd.patient_national_sponsor_id = pra.patient_national_sponsor_id)
   LEFT JOIN patient_corporate_details spcd ON (spcd.patient_corporate_id = pra.secondary_patient_corporate_id)
   LEFT JOIN patient_national_sponsor_details spnd ON (spnd.patient_national_sponsor_id = pra.secondary_patient_national_sponsor_id)
   LEFT JOIN sponsor_type pst ON pst.sponsor_type_id = tpa.sponsor_type_id
   LEFT JOIN sponsor_type sst ON sst.sponsor_type_id = stpa.sponsor_type_id
   LEFT JOIN contact_preferences cpref ON cpref.mr_no = pd.mr_no
WHERE ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )
;

-- this view is used patient activities audit log to get the prescribed item names.
DROP VIEW IF EXISTS doctor_order_item_names_view CASCADE;
CREATE OR REPLACE VIEW doctor_order_item_names_view AS
SELECT pa.prescription_id, CASE WHEN activity_type = 'G' THEN gen_activity_details WHEN presc_type = 'NonBillable' THEN item_name
	WHEN presc_type = 'Medicine' THEN sid.medicine_name WHEN presc_type = 'Inv.' THEN atp.test_name
	WHEN presc_type = 'Doctor' THEN cdoc.doctor_name WHEN presc_type = 'Service' THEN service_name END item_name
FROM patient_activities pa
	LEFT JOIN patient_prescription pp on (pp.patient_presc_id=pa.prescription_id)
	LEFT JOIN patient_medicine_prescriptions pmp on (pp.presc_type='Medicine' and pmp.op_medicine_pres_id=pp.patient_presc_id)
	LEFT JOIN patient_other_medicine_prescriptions pomp on (pp.presc_type='Medicine' and pomp.prescription_id=pp.patient_presc_id)
	LEFT JOIN patient_test_prescriptions ptp on (pp.presc_type='Inv.' and ptp.op_test_pres_id=pp.patient_presc_id)
	LEFT JOIN patient_consultation_prescriptions pcp on (pp.presc_type='Doctor' and pcp.prescription_id=pp.patient_presc_id)
	LEFT JOIN patient_service_prescriptions psp on (pp.presc_type='Service' and pp.patient_presc_id=psp.op_service_pres_id)
	LEFT JOIN patient_other_prescriptions potp on ((pp.presc_type='NonBillable' and pp.patient_presc_id=potp.prescription_id))
	LEFT JOIN store_item_details sid ON (pp.presc_type = 'Medicine' AND pmp.medicine_id=sid.medicine_id)
	LEFT JOIN all_tests_pkgs_view atp ON (pp.presc_type = 'Inv.' AND ptp.test_id=atp.test_id)
	LEFT JOIN services s ON (pp.presc_type = 'Service' AND psp.service_id=s.service_id)
	LEFT JOIN doctors cdoc ON (pp.presc_type = 'Doctor' AND pcp.doctor_id=cdoc.doctor_id)
;

--- vital and intake-output audit logs views

DROP VIEW IF EXISTS vital_audit_log_view CASCADE;
CREATE OR REPLACE VIEW vital_audit_log_view AS
(SELECT vval.log_id,vval.patient_id,vval.vital_reading_id,vral.param_id,vval.user_name,vval.mod_time,vval.operation,vval.field_name,vval.old_value,vval.new_value
FROM visit_vitals_audit_log vval
JOIN vital_reading_audit_log vral USING (vital_reading_id)
JOIN vital_parameter_master vpm USING (param_id)
WHERE vpm.param_container = 'V'
UNION
SELECT vral.log_id,vval.patient_id,vral.vital_reading_id,vral.param_id,vral.user_name,vral.mod_time,vral.operation,vral.field_name,vral.old_value,vral.new_value
FROM vital_reading_audit_log vral
JOIN visit_vitals_audit_log vval USING (vital_reading_id)
JOIN vital_parameter_master vpm USING (param_id)
WHERE vpm.param_container = 'V'
);

DROP VIEW IF EXISTS intake_output_audit_log_view CASCADE;
CREATE OR REPLACE VIEW intake_output_audit_log_view AS
(SELECT vval.log_id,vval.patient_id,vval.vital_reading_id,vral.param_id,vval.user_name,vval.mod_time,vval.operation,vval.field_name,vval.old_value,vval.new_value
FROM visit_vitals_audit_log vval
JOIN vital_reading_audit_log vral USING (vital_reading_id)
JOIN vital_parameter_master vpm USING (param_id)
WHERE vpm.param_container IN ('I','O')
UNION
SELECT vral.log_id,vval.patient_id,vral.vital_reading_id,vral.param_id,vral.user_name,vral.mod_time,vral.operation,vral.field_name,vral.old_value,vral.new_value
FROM vital_reading_audit_log vral
JOIN visit_vitals_audit_log vval USING (vital_reading_id)
JOIN vital_parameter_master vpm USING (param_id)
WHERE vpm.param_container IN ('I','O')
);


DROP VIEW IF EXISTS vital_reading_fields_options_view CASCADE;
CREATE OR REPLACE VIEW vital_reading_fields_options_view AS
	(SELECT DISTINCT(vral.vital_reading_id), vral.log_id , vral.param_id, vpm.param_label,
		vval.patient_id ,vral.user_name ,
		vral.mod_time ,vral.operation , vral.field_name ,
		vral.old_value ,vral.new_value ,pr.mr_no
		FROM vital_reading_audit_log vral
		LEFT JOIN visit_vitals_audit_log vval ON (vral.vital_reading_id=vval.vital_reading_id)
		LEFT JOIN vital_reading vr ON   vr.vital_reading_id = vral.vital_reading_id
		JOIN vital_parameter_master vpm ON vpm.param_id = vral.param_id
		LEFT JOIN patient_registration pr ON vval.patient_id = pr.patient_id
	);

DROP VIEW IF EXISTS patient_vitals_audit_log_view CASCADE;
CREATE OR REPLACE VIEW patient_vitals_audit_log_view AS
SELECT  null::integer AS vital_reading_id, vval.log_id, 'visit_vitals_audit_log'::text AS base_table,
	vval.patient_id, vval.user_name,vval.mod_time,vval.operation,vval.field_name,
	vval.old_value,vval.new_value,pr.mr_no,vval.vital_reading_id AS v_reading_id
FROM visit_vitals_audit_log vval
	LEFT JOIN (  SELECT DISTINCT vital_reading_id
	FROM vital_reading
	JOIN vital_parameter_master vpm USING(param_id)
        WHERE vpm.param_container = 'V') AS foo
	on foo.vital_reading_id=vval.vital_reading_id
LEFT JOIN patient_registration pr ON vval.patient_id = pr.patient_id
UNION ALL
SELECT * FROM (
SELECT vral.vital_reading_id, min(vral.log_id) AS log_id, 'vital_reading_fields_options_view'::text AS base_table,
	min(vval.patient_id) AS patient_id,min(vral.user_name) AS user_name,
	min(vral.mod_time) AS mod_time,
	min(vral.operation) AS operation, min(vral.field_name) AS field_name,
	min(vral.old_value) AS old_value,min(vral.new_value) AS new_value,pr.mr_no,
	vral.vital_reading_id AS v_reading_id
	FROM vital_reading_audit_log vral
LEFT JOIN visit_vitals_audit_log vval ON (vral.vital_reading_id=vval.vital_reading_id)
-- to get the patient id we can use visit vitals but incase WHEN a reading is deleted, this doesnt has that row.
-- hence used visit_vitals_audit_log.
LEFT JOIN vital_reading vr ON   vr.vital_reading_id = vral.vital_reading_id
JOIN vital_parameter_master vpm ON vpm.param_id = vral.param_id
LEFT JOIN patient_registration pr ON vval.patient_id = pr.patient_id
WHERE  vpm.param_container = 'V' and vral.field_name = 'vital_reading_id'
GROUP BY vral.vital_reading_id ,pr.mr_no
 )  AS foo;

----Bug#34621-- related changes ----

CREATE OR REPLACE FUNCTION hospital_configuration_master_change_trigger() RETURNS TRIGGER AS $BODY$
DECLARE
	changed boolean;
BEGIN
	changed := false;
	IF (TG_OP = 'INSERT') THEN

		IF (NEW.print_type = 'Discharge') THEN
			insert into doc_print_configuration(document_type,center_id,printer_settings,page_settings,template_name) VALUES
			('discharge',NEW.center_id,NEW.printer_id,NEW.print_type,
			 coalesce((select template_type from print_templates
			where template_type ='D' AND (print_template_content != '')),'BuiltinDischargeSummary'));

			insert into doc_print_configuration(document_type,center_id,printer_settings,page_settings,template_name)(
			select 'reg_'||dht.template_name AS document_type,NEW.center_id,NEW.printer_id,NEW.print_type,dht.print_template_name
			FROM doc_hvf_templates dht
			WHERE dht.doc_type='SYS_RG' AND(dht.print_template_name is not null OR dht.print_template_name != ''));

			insert into doc_print_configuration(document_type,center_id,printer_settings,page_settings,template_name)(
			select 'reg_'||drt.template_name AS document_type,NEW.center_id,NEW.printer_id,NEW.print_type,'' AS print_template_name
			FROM doc_rich_templates drt
			WHERE drt.doc_type='SYS_RG');

			INSERT INTO doc_print_configuration (document_type, center_id, printer_settings, page_settings, template_name) (
				select 'prescription_'||ppt.template_name AS document_type, NEW.center_id, NEW.printer_id, NEW.print_type,
				'' AS print_template_name
				FROM prescription_print_template ppt
				WHERE ppt.prescription_template_content != '');

			INSERT INTO doc_print_configuration (document_type, center_id, printer_settings, page_settings, template_name) VALUES
				('prescription_BUILTIN_HTML', NEW.center_id, NEW.printer_id, NEW.print_type,'');

			INSERT INTO doc_print_configuration (document_type, center_id, printer_settings, page_settings, template_name) VALUES
				('prescription_BUILTIN_TEXT' , NEW.center_id, NEW.printer_id, NEW.print_type,'');

		END IF;

	ELSIF (TG_OP = 'UPDATE') THEN
		IF (OLD.printer_id != NEW.printer_id) THEN
			changed:= true;
		END IF;
	END IF;

	IF changed THEN
		-- update the doc_print_configuration table's printer_id
		UPDATE doc_print_configuration SET printer_settings = NEW.printer_id WHERE center_id = NEW.center_id
		AND (document_type = 'discharge' OR document_type ilike 'reg_%' OR document_type ilike 'prescription_%');
	END IF;
	RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS hospital_configuration_master_change_trigger ON hosp_print_master;
CREATE TRIGGER hospital_configuration_master_change_trigger
AFTER INSERT OR UPDATE ON hosp_print_master
	FOR EACH ROW EXECUTE PROCEDURE hospital_configuration_master_change_trigger();

-- prescription_print_template trigger

CREATE OR REPLACE FUNCTION prescription_print_template_trigger() RETURNS TRIGGER AS $BODY$
BEGIN
	IF (TG_OP = 'INSERT') THEN
		IF (NEW.template_name IS NOT NULL AND NEW.template_name != '' AND
			NEW.prescription_template_content IS NOT NULL AND NEW.prescription_template_content != '') THEN
			INSERT into doc_print_configuration(document_type, center_id, printer_settings, page_settings, template_name) (
			SELECT 'prescription_'||NEW.template_name, dpc.center_id, dpc.printer_settings, dpc.page_settings, dpc.template_name
				FROM doc_print_configuration dpc, generic_preferences gp
				WHERE dpc.document_type = 'prescription_' || gp.default_prescription_print_template);
		END IF;
	END IF;
	RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS prescription_print_template_trigger ON prescription_print_template;
CREATE TRIGGER prescription_print_template_trigger
AFTER INSERT ON prescription_print_template
	FOR EACH ROW EXECUTE PROCEDURE prescription_print_template_trigger();

-- doc_hvf_templates trigger

CREATE OR REPLACE FUNCTION doc_hvf_print_template_trigger() RETURNS TRIGGER AS $BODY$
BEGIN
	IF (TG_OP = 'INSERT') THEN
		IF (NEW.template_name IS NOT NULL AND NEW.template_name != '' AND
			NEW.doc_type = 'SYS_RG') THEN
			INSERT into doc_print_configuration(document_type, center_id, printer_settings, page_settings, template_name) (
			SELECT 'reg_'||NEW.template_name, hpm.center_id, hpm.printer_id, hpm.print_type, NEW.print_template_name
				FROM hosp_print_master hpm, generic_preferences gp
				WHERE hpm.print_type = 'Discharge');
		END IF;
	END IF;
	RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS doc_hvf_print_template_trigger ON doc_hvf_templates;
CREATE TRIGGER doc_hvf_print_template_trigger
AFTER INSERT ON doc_hvf_templates
	FOR EACH ROW EXECUTE PROCEDURE doc_hvf_print_template_trigger();

-- doc_rich_templates trigger

CREATE OR REPLACE FUNCTION doc_rich_print_template_trigger() RETURNS TRIGGER AS $BODY$
BEGIN
	IF (TG_OP = 'INSERT') THEN
		IF (NEW.template_name IS NOT NULL AND NEW.template_name != '' AND
			NEW.doc_type = 'SYS_RG') THEN
			INSERT into doc_print_configuration(document_type, center_id, printer_settings, page_settings, template_name) (
			SELECT 'reg_'||NEW.template_name, hpm.center_id, hpm.printer_id, hpm.print_type, ''
				FROM hosp_print_master hpm WHERE hpm.print_type = 'Discharge');
		END IF;
	END IF;
	RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS doc_rich_print_template_trigger ON doc_rich_templates;
CREATE TRIGGER doc_rich_print_template_trigger
AFTER INSERT ON doc_rich_templates
	FOR EACH ROW EXECUTE PROCEDURE doc_rich_print_template_trigger();

-- generic print template delete trigger, can be attached to any table with a prefix parameter

CREATE OR REPLACE FUNCTION print_template_delete_trigger() RETURNS TRIGGER AS $BODY$
DECLARE prefix text;
BEGIN
	prefix := TG_ARGV[0];
	IF (TG_OP = 'DELETE') THEN
		IF (OLD.template_name IS NOT NULL AND OLD.template_name != '') THEN
			DELETE FROM doc_print_configuration where document_type = prefix || '_' || OLD.template_name;
		END IF;
	END IF;
	RETURN OLD;
END;
$BODY$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS doc_hvf_print_template_del_trigger ON doc_hvf_templates;
CREATE TRIGGER doc_hvf_print_template_del_trigger
BEFORE DELETE ON doc_hvf_templates
	FOR EACH ROW EXECUTE PROCEDURE print_template_delete_trigger('reg');

DROP TRIGGER IF EXISTS doc_rich_print_template_del_trigger ON doc_rich_templates;
CREATE TRIGGER doc_rich_print_template_del_trigger
BEFORE DELETE ON doc_rich_templates
	FOR EACH ROW EXECUTE PROCEDURE print_template_delete_trigger('reg');

DROP TRIGGER IF EXISTS presc_print_template_del_trigger ON prescription_print_template;
CREATE TRIGGER presc_print_template_del_trigger
BEFORE DELETE ON prescription_print_template
	FOR EACH ROW EXECUTE PROCEDURE print_template_delete_trigger('prescription');

--- Per diem code charges audit log view

DROP VIEW IF EXISTS per_diem_codes_charges_audit_log_view CASCADE;
CREATE OR REPLACE VIEW per_diem_codes_charges_audit_log_view AS
(SELECT pcal.*,pc.per_diem_description,pc.service_groups_names
	FROM per_diem_codes_charges_audit_log pcal
	LEFT JOIN per_diem_codes_master pc using(per_diem_code)
);
DROP VIEW IF EXISTS patient_package_status_view CASCADE;
CREATE OR REPLACE VIEW patient_package_status_view AS
SELECT
	pps.patient_id, pps.prescription_id, pps.common_order_id,
	pps.package_id::text AS item_id, pm.package_name AS name,
	pd.doctor_id AS pres_doctor_id, pd.doctor_name AS pres_doctor_name, presc_date AS pres_timestamp,
	pps.remarks AS remarks,TO_CHAR(presc_date,'dd-mm-yyyy') AS pres_date,
	b.bill_no, b.status AS bill_status,bc.amount,
	(SELECT (
		CASE
			WHEN foo.cancled > 0 THEN 'X'
	 		WHEN foo.actual = foo.notconducted  THEN 'P'
		    WHEN foo.actual = foo.done  THEN 'C'
	        WHEN  foo.notconducted < foo.actual THEN 'P'
	     END  ) FROM (
	    	SELECT
				(SELECT COUNT(*) FROM tests_prescribed tp
					WHERE tp.package_ref = pp.prescription_id and conducted NOT IN ( 'U','X' ) ) +
				(SELECT COUNT(*) FROM services_prescribed sp
					where sp.package_ref = pp.prescription_id and conducted NOT IN ( 'U','X' ) ) +
				(SELECT COUNT(*) FROM bed_operation_schedule op
					where op.package_ref = pp.prescription_id and status NOT IN ( 'U','X' )) +
				(SELECT COUNT(*) FROM services_prescribed spp
					JOIN bed_operation_schedule opp ON (opp.prescribed_id=spp.operation_ref)
					WHERE opp.package_ref = pp.prescription_id and conducted NOT IN ( 'U','X' ) ) AS actual,
				(SELECT COUNT(*) FROM tests_prescribed tp
					WHERE tp.package_ref = pp.prescription_id and conducted IN ('N','NRN')) +
				(SELECT COUNT(*) FROM services_prescribed sp
					where sp.package_ref = pp.prescription_id and conducted IN ('N')) +
				(SELECT COUNT(*) FROM bed_operation_schedule op
					where op.package_ref = pp.prescription_id and status IN ('N')) +
				(SELECT COUNT(*) FROM services_prescribed spp
					JOIN bed_operation_schedule opp ON (opp.prescribed_id=spp.operation_ref)
					WHERE opp.package_ref = pp.prescription_id and spp.conducted IN ('N')) AS notconducted,
				(SELECT COUNT(*) FROM tests_prescribed tp
					WHERE tp.package_ref = pp.prescription_id and conducted IN ('P','RP')) +
				(SELECT COUNT(*) FROM services_prescribed sp
					where sp.package_ref = pp.prescription_id and conducted IN ('P')) +
				(SELECT COUNT(*) FROM bed_operation_schedule op
					where op.package_ref = pp.prescription_id and status IN ('P')) +
				(SELECT COUNT(*) FROM services_prescribed spp
					JOIN bed_operation_schedule opp ON (opp.prescribed_id=spp.operation_ref)
					WHERE opp.package_ref = pp.prescription_id and spp.conducted IN ('P')) AS partially,
				(SELECT COUNT(*) FROM tests_prescribed tp
					JOIN diagnostics USING(test_id)
					WHERE tp.package_ref = pp.prescription_id and (
					CASE WHEN results_entry_applicable THEN conducted = 'S' ELSE conducted = 'CRN' END )) +
				(SELECT COUNT(*) FROM services_prescribed sp
					where sp.package_ref = pp.prescription_id and conducted IN ('C')) +
				(SELECT COUNT(*) FROM bed_operation_schedule op
					where op.package_ref = pp.prescription_id and status IN ('C')) +
				(SELECT COUNT(*) FROM services_prescribed spp
					JOIN bed_operation_schedule opp ON (opp.prescribed_id=spp.operation_ref)
					WHERE opp.package_ref = pp.prescription_id and spp.conducted IN ('C')) AS done,
				(SELECT COUNT(*) FROM package_prescribed ppp
					where ppp.prescription_id = pp.prescription_id and status IN ('X') ) AS cancled
	FROM package_prescribed pp
	LEFT JOIN tests_prescribed tp ON (tp.package_ref = pp.prescription_id)
	LEFT JOIN services_prescribed sp ON (sp.package_ref = pp.prescription_id )
	LEFT JOIN bed_operation_schedule op ON (op.package_ref = pp.prescription_id)
	WHERE pp.prescription_id = pps.prescription_id
	group by pp.prescription_id) AS foo ) AS status
FROM package_prescribed pps
	JOIN packages pm ON pm.package_id = pps.package_id
	LEFT OUTER JOIN doctors pd ON pd.doctor_id = pps.doctor_id
	LEFT JOIN bill_activity_charge bac ON bac.activity_id=pps.prescription_id::text AND bac.activity_code='PKG'
	LEFT JOIN bill_charge bc USING(charge_id)
	LEFT JOIN bill b USING (bill_no)
;

DROP VIEW IF EXISTS priority_rate_sheet_parameters_view CASCADE;
CREATE OR REPLACE VIEW priority_rate_sheet_parameters_view AS
SELECT rpp.* from rate_plan_parameters rpp
JOIN (SELECT org_id, min(priority) AS priority from rate_plan_parameters GROUP BY org_id) AS x
ON (rpp.org_id = x.org_id and rpp.priority = x.priority)
;

--- function is used to update allergies in patient_details table from patient_allergies table

DROP FUNCTION IF EXISTS update_patient_details_allergies() CASCADE;
CREATE OR REPLACE FUNCTION update_patient_details_allergies() RETURNS TRIGGER AS $BODY$
DECLARE
	allergies character varying;
	currentallergy character varying;
	mrno character varying(15);
	allergytypeid integer;
	allergyid integer;
	allergencodeid integer;
	final_allergies character varying;
BEGIN
	IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN
		select mr_no from patient_section_details where section_detail_id=NEW.section_detail_id into mrno;
		allergytypeid := NEW.allergy_type_id;
		allergyid := NEW.allergy_id;
		allergencodeid := NEW.allergen_code_id;
	ELSEIF (TG_OP = 'DELETE') THEN
		select mr_no from patient_section_details where section_detail_id=OLD.section_detail_id into mrno;
		allergytypeid := OLD.allergy_type_id;
		allergyid := OLD.allergy_id;
		allergencodeid := OLD.allergen_code_id;
	END IF;

	SELECT textcat_commacat(COALESCE(am.allergen_description,gn.generic_name)) AS allergy INTO allergies
	FROM patient_allergies pa
    LEFT JOIN allergy_type_master atm ON (atm.allergy_type_id = pa.allergy_type_id)
    LEFT JOIN allergen_master am ON (pa.allergen_code_id=am.allergen_code_id)
	LEFT JOIN generic_name gn ON (pa.allergen_code_id = gn.allergen_code_id)
	JOIN patient_section_details psd using (section_detail_id)
	WHERE mr_no = mrno AND pa.allergy_type_id = allergytypeid AND pa.allergy_id NOT IN (allergyid) AND psd.section_status = 'A' AND pa.status = 'A';

	IF ((TG_OP = 'INSERT' OR TG_OP = 'UPDATE') AND allergyid IS NOT NULL) THEN
		SELECT COALESCE(am.allergen_description,gn.generic_name) as allergy INTO currentallergy FROM patient_allergies pa
		LEFT JOIN allergen_master am ON (pa.allergen_code_id=am.allergen_code_id)
		LEFT JOIN generic_name gn ON (pa.allergen_code_id = gn.allergen_code_id)
        where pa.allergy_id = allergyid AND pa.status = 'A';
		allergies := currentallergy || ',' || allergies;
	END IF;

	final_allergies := substring(textcat_commacat(allergies),1,1995) ||'....';
	IF (allergytypeid = null) THEN
	    UPDATE patient_details pd SET no_allergies = final_allergies
	    WHERE pd.mr_no = mrno;
	ELSEIF (allergytypeid = 1) THEN
	    UPDATE patient_details pd SET med_allergies = final_allergies
	    WHERE pd.mr_no = mrno;
	ELSEIF (allergytypeid = 3) THEN
	    UPDATE patient_details pd SET food_allergies = final_allergies
	    WHERE pd.mr_no = mrno;
	ELSEIF (allergytypeid = 2) THEN
	    UPDATE patient_details pd SET other_allergies = final_allergies
	    WHERE pd.mr_no = mrno;
	END IF;

	RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS update_patient_details_allergies_trigger ON patient_allergies;
CREATE TRIGGER update_patient_details_allergies_trigger
  AFTER INSERT OR UPDATE OR DELETE ON patient_allergies
  FOR EACH ROW EXECUTE PROCEDURE update_patient_details_allergies();


DROP VIEW IF EXISTS outsource_names CASCADE;
CREATE OR REPLACE VIEW outsource_names AS
SELECT outsource_dest_id,outsource_dest,
CASE WHEN outsource_dest_type IN ('O', 'IO') THEN oh_name ELSE hcm.center_name END AS outsource_name,
outsource_dest_type
FROM diag_outsource_master dom
LEFT JOIN outhouse_master om on(dom.outsource_dest = om.oh_id)
LEFT JOIN hospital_center_master hcm on(hcm.center_id::text = dom.outsource_dest);

DROP VIEW IF EXISTS multivisit_bills_view CASCADE;
CREATE OR REPLACE VIEW multivisit_bills_view AS
	SELECT bc.*, orders.package_id as pack_id, orders.pat_package_id from bill_charge bc
	JOIN (
		SELECT sp.common_order_id AS common_order_id, p.package_id, pp.pat_package_id
		FROM services_prescribed sp JOIN package_prescribed pp ON (sp.package_ref = pp.prescription_id)
		JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true)
		UNION ALL 
		SELECT tp.common_order_id as common_order_id, p.package_id, pp.pat_package_id
		FROM tests_prescribed tp JOIN package_prescribed pp ON (tp.package_ref = pp.prescription_id)
		JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true)
		UNION ALL
		SELECT dc.common_order_id as common_order_id, p.package_id, pp.pat_package_id
		FROM doctor_consultation dc
		JOIN package_prescribed pp ON (dc.package_ref = pp.prescription_id)
		JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true)
		LEFT JOIN bill_activity_charge bac ON bac.activity_id=dc.consultation_id::text AND bac.activity_code='DOC'
		LEFT JOIN bill_charge bc ON (bc.charge_id = bac.charge_id)
		UNION ALL
		SELECT osp.common_order_id as common_order_id, p.package_id, pp.pat_package_id
		FROM other_services_prescribed osp JOIN package_prescribed pp ON (osp.package_ref = pp.prescription_id)
		JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true)
		UNION ALL
		SELECT bos.common_order_id as common_order_id, p.package_id, pp.pat_package_id
		FROM bed_operation_schedule bos JOIN package_prescribed pp ON (bos.common_order_id = pp.common_order_id)
		JOIN packages p ON (pp.package_id = p.package_id AND p.multi_visit_package = true)
	) AS orders ON bc.order_number = orders.common_order_id;


DROP VIEW IF EXISTS patient_section_mini_window_view CASCADE;
CREATE OR REPLACE VIEW patient_section_mini_window_view AS
SELECT psd.section_id, om.operation_name,ops.operation_proc_id,ods.operation_details_id,psd.patient_id
	FROM operation_details ods
	JOIN patient_details pd on (pd.mr_no = ods.mr_no AND
	     patient_confidentiality_check(pd.patient_group,pd.mr_no))
	JOIN operation_procedures ops ON (ods.operation_details_id=ops.operation_details_id)
	JOIN operation_master om ON (om.op_id=ops.operation_id)
	JOIN patient_section_details psd ON (psd.section_item_id = ops.operation_proc_id AND psd.item_type='SUR')
	JOIN patient_section_fields psv ON (psd.section_detail_id=psv.section_detail_id)
	JOIN section_field_desc field ON (field.field_id=psv.field_id)
	LEFT JOIN patient_section_options pso ON (pso.field_detail_id=psv.field_detail_id)
	LEFT JOIN patient_section_image_details img ON (psv.field_detail_id=img.field_detail_id)
WHERE CASE 
		WHEN field_type in ('text', 'wide text') THEN coalesce(field_remarks, '') != ''
		WHEN field_type in ('dropdown', 'checkbox') THEN coalesce(pso.available, 'N') = 'Y'
		WHEN field_type in ('date') THEN date is not null
		WHEN field_type in ('datetime') THEN date_time is not null
		WHEN field_type in ('image') THEN marker_id is not null
		ELSE false END 
	GROUP BY psd.section_id,om.operation_name,ops.operation_proc_id,ods.operation_details_id,psd.patient_id;

DROP VIEW IF EXISTS multivisit_deposits_view CASCADE;
CREATE OR REPLACE VIEW multivisit_deposits_view AS
SELECT deposits.mr_no,
    deposits.package_id,
    COALESCE(deposits.total_deposits,0::numeric) AS total_deposits,
    COALESCE(setoffs.total_set_offs,0::numeric) AS total_set_offs,
    deposits.pat_package_id
   FROM ( SELECT pd.mr_no,
            pd.package_id::integer AS package_id, pd.pat_package_id::integer,
            sum(pd.amount) AS total_deposits
           FROM patient_package_deposits_view pd
          GROUP BY pd.mr_no,pd.package_id, pd.pat_package_id) deposits
     LEFT JOIN LATERAL ( SELECT r.mr_no,
            package_bills.package_id,
            sum(br.allocated_amount) AS total_set_offs,
            ru.entity_id AS pat_package_id
            FROM bill_receipts br
            JOIN receipts rcp ON br.receipt_no=rcp.receipt_id AND is_deposit=true
            join receipt_usage ru ON rcp.receipt_id = ru.receipt_id and ru.entity_type = 'pat_package_id'
            JOIN bill b ON br.bill_no=b.bill_no
             JOIN patient_registration r ON b.visit_id::text = r.patient_id::text
             JOIN patient_details p ON p.mr_no::text = r.mr_no::text
             JOIN LATERAL ( SELECT DISTINCT bc.bill_no,
                    orders.package_id
                   FROM bill_charge bc
                     JOIN ( SELECT sp.common_order_id,
                            p_1.package_id
                           FROM services_prescribed sp
                             JOIN package_prescribed pp ON sp.package_ref = pp.prescription_id AND pp.mr_no::text = deposits.mr_no::text
                             JOIN packages p_1 ON pp.package_id = p_1.package_id AND p_1.multi_visit_package = true
                        UNION ALL
                         SELECT tp.common_order_id,
                            p_1.package_id
                           FROM tests_prescribed tp
                             JOIN package_prescribed pp ON tp.package_ref = pp.prescription_id AND pp.mr_no::text = deposits.mr_no::text
                             JOIN packages p_1 ON pp.package_id = p_1.package_id AND p_1.multi_visit_package = true
                        UNION ALL
                         SELECT dc.common_order_id,
                            p_1.package_id
                         FROM doctor_consultation dc
                             JOIN package_prescribed pp ON dc.package_ref = pp.prescription_id AND pp.mr_no::text = deposits.mr_no::text
                             JOIN packages p_1 ON pp.package_id = p_1.package_id AND p_1.multi_visit_package = true
                             LEFT JOIN bill_activity_charge bac ON bac.activity_id::text = dc.consultation_id::text AND bac.activity_code::text = 'DOC'::text
                             LEFT JOIN bill_charge bc_1 ON bc_1.charge_id::text = bac.charge_id::text
                        UNION ALL
                         SELECT osp.common_order_id,
                            p_1.package_id
                           FROM other_services_prescribed osp
                             JOIN package_prescribed pp ON osp.package_ref = pp.prescription_id AND pp.mr_no::text = deposits.mr_no::text
                             JOIN packages p_1 ON pp.package_id = p_1.package_id AND p_1.multi_visit_package = true) orders ON bc.order_number = orders.common_order_id WHERE bc.bill_no = b.bill_no)
                              package_bills ON TRUE 
          WHERE patient_confidentiality_check(COALESCE(p.patient_group, 0), p.mr_no) AND deposits.mr_no::text = r.mr_no::text
          GROUP BY ru.entity_id,r.mr_no, package_bills.package_id) setoffs ON deposits.mr_no::text = setoffs.mr_no::text AND deposits.pat_package_id::text = setoffs.pat_package_id::text and deposits.package_id = setoffs.package_id;


DROP VIEW IF EXISTS multivisit_patient_package_view CASCADE;
CREATE OR REPLACE VIEW multivisit_patient_package_view AS
SELECT patp.mr_no, pp.patient_id, mbv.bill_no, pp.package_id, pp.pat_package_id, patp.status, p.package_name, p.description, p.package_code
FROM multivisit_bills_view mbv
JOIN package_prescribed pp ON (mbv.order_number = pp.common_order_id and mbv.package_id = pp.package_id)
LEFT JOIN patient_packages patp ON (pp.pat_package_id = patp.pat_package_id)
JOIN packages p ON (pp.package_id = p.package_id);


DROP VIEW IF EXISTS patient_allergies_audit_log_view CASCADE;
CREATE OR REPLACE VIEW patient_allergies_audit_log_view AS
SELECT paal.*,mr_no from patient_allergies_audit_log paal
JOIN patient_section_details psd ON (paal.section_detail_id = psd.section_detail_id);


DROP VIEW IF EXISTS store_item_codes_view CASCADE;
CREATE VIEW store_item_codes_view AS
SELECT sid.*,sic.code_type,sic.code_id,sic.item_code FROM store_item_details sid
LEFT JOIN store_item_codes sic ON(sic.medicine_id = sid.medicine_id) ORDER BY medicine_name;

-- Accounting view.

DROP VIEW IF EXISTS visit_sponsor_details CASCADE;
CREATE OR REPLACE VIEW visit_sponsor_details AS
SELECT pr.mr_no,pr.patient_id,ptm.tpa_name AS pri_tpa_name,ppip.sponsor_id AS primary_sponsor_id,
       pipm.plan_name AS pipm,ppip.plan_id AS pri_plan_id,ppip.insurance_co AS primary_insurance_co,
       ppip.patient_policy_id AS pri_patient_policy_id,ppip.prior_auth_id AS pri_prior_auth_id,
       ppip.prior_auth_mode_id AS pri_prior_auth_mode_id,
       stm.tpa_name AS sec_tpa_name,spip.sponsor_id AS secondary_sponsor_id,	
       sipm.plan_name AS sec_plan_name,sipm.plan_id AS sec_plan_id,spip.insurance_co AS secondary_insurance_co,
       spip.patient_policy_id AS sec_patient_policy_id,spip.prior_auth_id AS sec_prior_auth_id,     
       spip.prior_auth_mode_id AS sec_prior_auth_mode_id	
FROM patient_registration pr
LEFT JOIN patient_insurance_plans ppip ON(ppip.patient_id=pr.patient_id AND ppip.priority = 1)
LEFT JOIN patient_insurance_plans spip ON(spip.patient_id=pr.patient_id AND spip.priority = 2)
LEFT JOIN tpa_master ptm ON(ptm.tpa_id = ppip.sponsor_id)
LEFT JOIN tpa_master stm ON(stm.tpa_id = spip.sponsor_id)
LEFT JOIN insurance_plan_main pipm ON(pipm.plan_id = ppip.plan_id)
LEFT JOIN insurance_plan_main sipm ON(sipm.plan_id = spip.plan_id);


DROP VIEW IF EXISTS visit_bill_charge_claim_details CASCADE;
CREATE OR REPLACE VIEW visit_bill_charge_claim_details AS
SELECT * FROM (
	SELECT bcc.charge_id,bcc.insurance_claim_amt AS pri_insurance_claim_amt,bcc.claim_recd_total AS pri_claim_recd_total,
	 bcc.claim_status AS pri_claim_status,bcc.prior_auth_mode_id AS pri_prior_auth_mode_id,
	 bcc.denial_code AS pri_denial_code,bcc.prior_auth_id AS pri_prior_auth_id,
	 sec_bill_charge_claim.insurance_claim_amt AS sec_insurance_claim_amt,
	 sec_bill_charge_claim.claim_recd_total AS sec_claim_recd_total,sec_bill_charge_claim.claim_status AS sec_claim_status,
	 sec_bill_charge_claim.prior_auth_mode_id AS sec_prior_auth_mode_id,sec_bill_charge_claim.denial_code AS sec_denial_code,
	 sec_bill_charge_claim.prior_auth_id AS sec_prior_auth_id
	 FROM bill_charge_claim bcc
	 JOIN bill_charge bc  USING(charge_id)
	 JOIN bill_claim bcl ON(bcl.bill_no = bc.bill_no)
	 JOIN visit_sponsor_details vsd ON(bcl.visit_id = vsd.patient_id AND vsd.pri_plan_id = bcl.plan_id )
	 LEFT JOIN (
	 SELECT bcc.charge_id,bcc.insurance_claim_amt,bcc.claim_recd_total,bcc.claim_status,bcc.prior_auth_mode_id,
	 bcc.denial_code,bcc.prior_auth_id FROM
	 bill_charge_claim bcc
	 JOIN bill_charge bc  USING(charge_id)
	 JOIN bill_claim bcl ON(bcl.bill_no = bc.bill_no)
	 JOIN visit_sponsor_details vsd ON(bcl.visit_id = vsd.patient_id AND vsd.pri_plan_id = bcl.plan_id )
	 ) AS sec_bill_charge_claim ON(sec_bill_charge_claim.charge_id = bcc.charge_id)
 ) AS bill_charge_claims;

DROP VIEW IF EXISTS store_item_health_authority_code_type_view CASCADE;
CREATE VIEW store_item_health_authority_code_type_view AS
SELECT health_authority,null AS ha_code_type_id,medicine_id,medicine_name,'' AS code_type
FROM store_item_details,health_authority_master WHERE medicine_id NOT IN(select medicine_id FROM ha_item_code_type)
UNION
SELECT health_authority,ha_code_type_id,sid.medicine_id,medicine_name,code_type
FROM ha_item_code_type hict
JOIN store_item_details sid ON(sid.medicine_id=hict.medicine_id)
ORDER BY medicine_name;

DROP VIEW IF EXISTS store_item_ha_code_types_view CASCADE;
CREATE VIEW store_item_ha_code_types_view AS
SELECT sid.*,hict.code_type,sic.code_id,sic.item_code
FROM (select distinct code_type,medicine_id FROM ha_item_code_type) AS hict
JOIN store_item_details sid ON(hict.medicine_id = sid.medicine_id)
LEFT JOIN store_item_codes sic ON(sic.medicine_id = sid.medicine_id AND hict.code_type=sic.code_type)
ORDER BY medicine_name;

DROP FUNCTION IF EXISTS update_item_issue_no_for_indent() CASCADE;
CREATE OR REPLACE FUNCTION update_item_issue_no_for_indent() RETURNS TRIGGER AS $BODY$

BEGIN
	IF ((OLD.item_issue_no IS NULL AND NEW.item_issue_no IS NOT NULL) OR NEW.item_issue_no != OLD.item_issue_no) THEN
		INSERT INTO store_patient_indent_item_issue_no_details values(NEW.patient_indent_no,NEW.item_issue_no);
	END IF;
RETURN NEW;
END;
$BODY$ language plpgsql;

DROP TRIGGER IF EXISTS update_item_issue_no_for_indent_tigger ON store_patient_indent_details;
CREATE TRIGGER update_item_issue_no_for_indent_tigger
  AFTER UPDATE
  ON store_patient_indent_details
  FOR EACH ROW
  EXECUTE PROCEDURE update_item_issue_no_for_indent();

DROP FUNCTION IF EXISTS bill_charge_claim_totals_trigger() CASCADE;
CREATE OR REPLACE FUNCTION bill_charge_claim_totals_trigger() RETURNS TRIGGER AS $BODY$
DECLARE

	changed boolean;
	status_changed boolean;
	oldChargeId text;
	newChargeId text;
	newClaimId text;
	oldClaimId text;
	new_claim_status character(1);
	check_closure boolean;

BEGIN
--	raise notice 'bclm totals trigger %', TG_OP;

	changed := false; oldChargeId := null; newChargeId := null;
	status_changed := false; check_closure :=false; new_claim_status = null;
	oldClaimId := null; newClaimId := null;

--	IF ((TG_OP = 'INSERT') OR (TG_OP = 'UPDATE')) THEN
--		IF ((NEW.charge_head = 'MARDRG') OR (NEW.charge_head = 'OUTDRG')) THEN
--			-- do not run any updates because we came from bill_charge to bill_charge_claim and back here
--			RETURN NEW;
--		END IF;
--	END IF;
	IF (TG_OP = 'INSERT') THEN

		changed := true;
		status_changed := true;
		new_claim_status := NEW.claim_status;
		newChargeId = NEW.charge_id;
		newClaimId = NEW.claim_id;
		IF NEW.claim_status = 'C' THEN
			check_closure := true;
		END IF;

	ELSIF (TG_OP = 'UPDATE') THEN

		oldChargeId := OLD.charge_id;
		newChargeId := NEW.charge_id;

		oldClaimId := OLD.claim_id;
		newClaimId := NEW.claim_id;

		IF (NEW.charge_id != OLD.charge_id) THEN
			changed := true;
		ELSE
			IF (COALESCE(NEW.insurance_claim_amt,0) != COALESCE(OLD.insurance_claim_amt,0))
				THEN changed := true; END IF;
			IF (COALESCE(NEW.claim_recd_total,0) != COALESCE(OLD.claim_recd_total,0))
				THEN changed := true; END IF;
			IF (COALESCE(NEW.return_insurance_claim_amt,0) != COALESCE(OLD.return_insurance_claim_amt,0))
				THEN changed := true; END IF;
			IF (COALESCE(NEW.tax_amt,0) != COALESCE(OLD.tax_amt,0))
				THEN changed := true; END IF;
		END IF;
		IF (COALESCE(NEW.claim_status,'') != COALESCE(OLD.claim_status,'')) THEN
			status_changed := true;
			new_claim_status := NEW.claim_status;
		END IF;
		IF (COALESCE(NEW.claim_status, '') = 'C') THEN
			check_closure := true;
		END IF;

	ELSIF (TG_OP = 'DELETE') THEN

		oldChargeId := OLD.charge_id;
		oldClaimId := OLD.claim_id;
		changed := true;
		IF (COALESCE(OLD.claim_status, '') != 'C') THEN
			check_closure := true;
			status_changed := true;
		END IF;
	END IF;

	IF changed THEN

		UPDATE bill_charge bc SET
		insurance_claim_amount =
			coalesce((SELECT sum(insurance_claim_amt) FROM bill_charge_claim bcc WHERE bcc.charge_id = bc.charge_id),0),
		claim_recd_total =
			coalesce((SELECT sum(claim_recd_total) FROM bill_charge_claim bcc WHERE bcc.charge_id = bc.charge_id),0),
		sponsor_tax_amt =
			coalesce((SELECT sum(tax_amt) FROM bill_charge_claim bcc WHERE bcc.charge_id = bc.charge_id),0)
		WHERE charge_id IN (newChargeId, oldChargeId);

	END IF;

	IF status_changed THEN
		IF (check_closure) THEN
			UPDATE bill_charge bc SET
			claim_status = 'C'
			FROM bill_charge_claim bcc
			WHERE bcc.charge_id = bc.charge_id AND bcc.charge_id IN (newChargeId, oldChargeId) AND
			NOT EXISTS (SELECT 1 from bill_charge_claim where coalesce(claim_status, '') != 'C' and charge_id IN (newChargeId, oldChargeId));

			UPDATE bill_claim bc SET
			claim_status = 'C'
			FROM bill_charge_claim bcc
			WHERE bcc.claim_id = bc.claim_id AND bcc.claim_id IN (newClaimId, oldClaimId) AND
			NOT EXISTS (SELECT 1 from bill_charge_claim bcc JOIN bill_charge bch ON (bch.charge_id = bcc.charge_id AND bch.status != 'X')
						WHERE coalesce(bcc.claim_status, '') != 'C' and bcc.claim_id IN (newClaimId, oldClaimId));

		ELSE
			UPDATE bill_charge bc SET
			claim_status = new_claim_status WHERE
			charge_id IN (newChargeId, oldChargeId);
		END IF;
	END IF;

	RETURN NEW;
END;
$BODY$ language plpgsql;

DROP TRIGGER IF EXISTS bill_charge_claim_totals_trigger ON bill_charge_claim CASCADE;
CREATE CONSTRAINT TRIGGER bill_charge_claim_totals_trigger
	AFTER INSERT OR UPDATE OR DELETE
	ON bill_charge_claim
	DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
	EXECUTE PROCEDURE bill_charge_claim_totals_trigger();

DROP FUNCTION IF EXISTS bill_charge_claim_defaults_trigger() CASCADE;
CREATE OR REPLACE FUNCTION bill_charge_claim_defaults_trigger() RETURNS TRIGGER AS $BODY$
DECLARE
	chead text;
BEGIN
	chead := '';
--	raise notice 'bclm defaults trigger';
	IF (TG_OP = 'INSERT') THEN
		SELECT coalesce(charge_head, '') from bill_charge where charge_id = NEW.charge_id INTO chead;
		IF (coalesce(NEW.return_insurance_claim_amt, 0.00) = 0.00) THEN
			NEW.return_insurance_claim_amt := 0.00;
		END IF;
		NEW.charge_head := chead;
	END IF;

	RETURN NEW;
END;
$BODY$ language plpgsql;

DROP TRIGGER IF EXISTS bill_charge_claim_defaults_trigger ON bill_charge_claim CASCADE;
CREATE TRIGGER bill_charge_claim_defaults_trigger
	BEFORE INSERT
	ON bill_charge_claim
  	FOR EACH ROW
	EXECUTE PROCEDURE bill_charge_claim_defaults_trigger();

DROP FUNCTION IF EXISTS bill_claim_status_trigger() CASCADE;
CREATE OR REPLACE FUNCTION bill_claim_status_trigger() RETURNS TRIGGER AS $BODY$
DECLARE

	changed boolean;
	billId text;
	new_claim_status character(1);
	priority integer;

BEGIN

	changed := false; billId := null; new_claim_status = null;

	IF (TG_OP = 'INSERT') THEN

		changed := true;
		new_claim_status := NEW.claim_status;
		billId := NEW.bill_no;
		priority := NEW.priority;

	ELSIF (TG_OP = 'UPDATE') THEN

		billId := NEW.bill_no;
		priority := NEW.priority;

		IF (COALESCE(NEW.claim_status, '') != COALESCE(OLD.claim_status,'')) THEN
			changed := true;
			IF coalesce(NEW.claim_status, '') = 'D' THEN
				new_claim_status := 'S';
			ELSE
				new_claim_status := NEW.claim_status;
			END IF;
		END IF;

	ELSIF (TG_OP = 'DELETE') THEN

		billId := OLD.bill_no;
		priority := OLD.priority;
		changed := true;
		new_claim_status := 'O';
	END IF;

	IF new_claim_status = 'C' THEN
		new_claim_status := 'R';
	END IF;
   -- raise notice 'bill claim status trigger % priority % claim status % bill no %', changed, priority, new_claim_status, billId;
	IF changed THEN

		IF (priority = 1) THEN
			UPDATE bill b SET
			primary_claim_status = new_claim_status WHERE b.bill_no = billId;
		ELSIF (priority = 2) THEN
			UPDATE bill b SET
			secondary_claim_status = new_claim_status WHERE b.bill_no = billId;
		END IF;

	END IF;

	RETURN NEW;
END;
$BODY$ language plpgsql;

DROP TRIGGER IF EXISTS bill_claim_status_trigger ON bill_claim CASCADE;
CREATE CONSTRAINT TRIGGER bill_claim_status_trigger
	AFTER INSERT OR DELETE OR UPDATE
	ON bill_claim
	DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
	EXECUTE PROCEDURE bill_claim_status_trigger();

-- disabling bill_claim_status_trigger ON bill_claim. Will be dropped if no complications arise by 11.8
ALTER TABLE bill_claim DISABLE TRIGGER bill_claim_status_trigger;

DROP FUNCTION IF EXISTS bill_sponsor_totals_trigger() CASCADE;
CREATE OR REPLACE FUNCTION bill_sponsor_totals_trigger() RETURNS TRIGGER AS $BODY$
DECLARE
	changed boolean;
	oldBillNo text;
	newBillNo text;
BEGIN
	changed := false;
	oldBillNo := null;
	newBillNo := null;

	IF (TG_OP = 'INSERT') THEN
		changed := true;
		newBillNo := NEW.bill_no;

	ELSIF (TG_OP = 'UPDATE') THEN
		oldBillNo := OLD.bill_no;
		newBillNo := NEW.bill_no;
		IF (NEW.bill_no != OLD.bill_no) THEN
			changed := true;
		ELSE
			IF (COALESCE(NEW.insurance_claim_amt,0) != COALESCE(OLD.insurance_claim_amt,0))
				THEN changed := true; END IF;

		END IF;
	ELSIF (TG_OP = 'DELETE') THEN
		oldBillNo := OLD.bill_no;
		changed := true;
	END IF;

	IF changed THEN
		UPDATE bill b SET
		primary_total_claim =  coalesce((SELECT sum(insurance_claim_amt) FROM bill_charge_claim bcc
					JOIN bill_claim bcl ON(bcc.claim_id = bcl.claim_id AND bcl.priority=1)
					JOIN bill_charge bc ON(bc.charge_id = bcc.charge_id)
					WHERE bc.status != 'X' AND bcc.bill_no = b.bill_no),0),
		secondary_total_claim = coalesce((SELECT sum(insurance_claim_amt) FROM bill_charge_claim bcc
					JOIN bill_claim bcl ON(bcc.claim_id = bcl.claim_id AND bcl.priority=2)
					JOIN bill_charge bc ON(bc.charge_id = bcc.charge_id)
					WHERE bc.status != 'X' AND bcc.bill_no = b.bill_no),0)
		WHERE bill_no IN (newBillNo, oldBillNo);
	END IF;

	RETURN NEW;
END;
$BODY$ language plpgsql;

DROP TRIGGER IF EXISTS bill_sponsor_totals_trigger ON bill_charge_claim CASCADE;
CREATE CONSTRAINT TRIGGER bill_sponsor_totals_trigger
	AFTER INSERT OR UPDATE OR DELETE
	ON bill_charge_claim
	DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
	EXECUTE PROCEDURE bill_sponsor_totals_trigger();

DROP VIEW IF EXISTS bill_charge_claim_details_view CASCADE;
CREATE OR REPLACE VIEW bill_charge_claim_details_view AS
SELECT * FROM (
SELECT bcc.charge_id,pri_ins_clm.claim_id AS pri_claim_id,pri_ins_clm.last_submission_batch_id AS pri_submission_batch_id,pri_ins_clm.main_visit_id AS pri_main_visit_id,
	pri_ins_clm.status AS pri_status,pcsx.resubmission_count AS pri_resubmission_count,pri_ins_clm.closure_type AS pri_closure_type,
	pri_ins_clm.action_remarks AS pri_action_remarks,pri_ins_clm.attachment AS pri_attachment,
	pri_ins_clm.attachment_content_type AS pri_attachment_content_type,pri_ins_clm.resubmission_type AS pri_resubmission_type,
	pri_ins_clm.comments AS pri_comments,pri_ins_clm.account_group AS pri_account_group,pri_ins_clm.patient_id,
	pri_ins_clm.denial_remarks AS pri_denial_remarks,
	pri_ins_clm.payers_reference_no AS pri_payers_reference_no,pri_ins_clm.denial_code AS pri_denial_code,
	sec_ins_clm.claim_id AS sec_claim_id,sec_ins_clm.last_submission_batch_id AS sec_submission_batch_id,sec_ins_clm.main_visit_id AS sec_main_visit_id,
	sec_ins_clm.status AS sec_status,scsx.resubmission_count AS sec_resubmission_count,sec_ins_clm.closure_type AS sec_closure_type,
	sec_ins_clm.action_remarks AS sec_action_remarks,sec_ins_clm.attachment AS sec_attachment,
	sec_ins_clm.attachment_content_type AS sec_attachment_content_type,
	sec_ins_clm.resubmission_type AS sec_resubmission_type,sec_ins_clm.comments AS sec_comments,sec_ins_clm.account_group AS sec_account_group,
	sec_ins_clm.denial_remarks AS sec_denial_remarks,
	sec_ins_clm.payers_reference_no AS sec_payers_reference_no,sec_ins_clm.denial_code AS sec_denial_code,
	pri_ins_clm.prior_auth_id AS pri_prior_auth_id, pri_ins_clm.prior_auth_mode_id AS pri_prior_auth_mode_id,
	sec_ins_clm.prior_auth_id AS sec_prior_auth_id, sec_ins_clm.prior_auth_mode_id AS sec_prior_auth_mode_id
FROM bill_charge_claim bcc
LEFT JOIN ( SELECT bcc.charge_id,bcc.claim_id,last_submission_batch_id,main_visit_id,status,sic.closure_type,
	action_remarks,attachment,attachment_content_type,resubmission_type,comments,account_group,sip.patient_id,
	op_type,sic.denial_remarks,priority,sip.plan_id,sic.payers_reference_no,bcc.denial_code,
	bcc.prior_auth_id,bcc.prior_auth_mode_id
FROM insurance_claim sic
JOIN bill_charge_claim bcc ON(bcc.claim_id = sic.claim_id)
JOIN patient_insurance_plans sip ON(sip.patient_id = sic.patient_id AND sip.plan_id = sic.plan_id)
WHERE sip.priority = 1 ) AS pri_ins_clm ON(pri_ins_clm.charge_id = bcc.charge_id)
LEFT JOIN ( SELECT count(cs.submission_batch_id) AS resubmission_count, cs.claim_id FROM insurance_claim ic 
	JOIN claim_submissions cs ON (cs.claim_id = ic.claim_id) 
	JOIN insurance_submission_batch isb ON (cs.submission_batch_id = isb.submission_batch_id) 
	WHERE isb.is_resubmission = 'Y' GROUP BY cs.claim_id) pcsx ON (pcsx.claim_id = pri_ins_clm.claim_id )  
LEFT JOIN ( SELECT bcc.charge_id,bcc.claim_id,last_submission_batch_id,main_visit_id,status,sic.closure_type,
	action_remarks,attachment,attachment_content_type,resubmission_type,comments,account_group,sip.patient_id,
	op_type,sic.denial_remarks,priority,sip.plan_id,sic.payers_reference_no,bcc.denial_code,
	bcc.prior_auth_id,bcc.prior_auth_mode_id
FROM insurance_claim sic
JOIN bill_charge_claim bcc ON(bcc.claim_id = sic.claim_id)
JOIN patient_insurance_plans sip ON(sip.patient_id = sic.patient_id AND sip.plan_id = sic.plan_id)
WHERE sip.priority = 2 ) AS sec_ins_clm ON(sec_ins_clm.charge_id = bcc.charge_id) 
LEFT JOIN ( SELECT count(cs.submission_batch_id) AS resubmission_count, cs.claim_id FROM insurance_claim ic 
	JOIN claim_submissions cs ON (cs.claim_id = ic.claim_id) 
	JOIN insurance_submission_batch isb ON (cs.submission_batch_id = isb.submission_batch_id) 
	WHERE isb.is_resubmission = 'Y' GROUP BY cs.claim_id) scsx ON (scsx.claim_id = sec_ins_clm.claim_id )
 ) AS foo
 GROUP BY charge_id,pri_claim_id,pri_submission_batch_id,pri_main_visit_id,
	pri_status,pri_resubmission_count,pri_closure_type,
	pri_action_remarks,pri_attachment,pri_attachment_content_type,pri_resubmission_type,pri_comments,pri_account_group,patient_id,
	pri_denial_remarks,pri_payers_reference_no,
	sec_claim_id,sec_submission_batch_id,sec_main_visit_id,
	sec_status,sec_resubmission_count,sec_closure_type,
	sec_action_remarks,sec_attachment,sec_attachment_content_type,
	sec_resubmission_type,sec_comments,sec_account_group,
	sec_denial_remarks,sec_payers_reference_no,
	sec_denial_code,pri_denial_code, pri_prior_auth_id, pri_prior_auth_mode_id, sec_prior_auth_id, sec_prior_auth_mode_id;
	
DROP VIEW IF EXISTS bill_claim_details_view CASCADE;

DROP VIEW IF EXISTS bill_total_claim_dues_view CASCADE;
CREATE OR REPLACE VIEW bill_total_claim_dues_view AS
SELECT pri_claim.claim_recd_total AS pri_claim_recd_total,sec_claim.claim_recd_total AS sec_claim_recd_total,
	pri_claim.total_claim AS pri_claim_amt, pri_claim.total_claim_tax AS pri_claim_amt_tax, sec_claim.total_claim AS sec_claim_amt,sec_claim.total_claim_tax AS sec_claim_amt_tax,
	b.bill_no
	FROM bill b
LEFT JOIN LATERAL (SELECT sum(claim_recd_total) AS claim_recd_total,sum(bcc.insurance_claim_amt) AS total_claim,sum(bcc.tax_amt) AS total_claim_tax, bc.bill_no
	FROM bill_charge_claim bcc
	JOIN bill_claim bc ON(bc.claim_id=bcc.claim_id AND bc.bill_no=bcc.bill_no) WHERE bcc.bill_no =  b.bill_no AND bc.priority = 1 GROUP BY bc.bill_no) AS pri_claim ON TRUE
LEFT JOIN LATERAL (SELECT sum(claim_recd_total) AS claim_recd_total,sum(bcc.insurance_claim_amt) AS total_claim, sum(bcc.tax_amt) AS total_claim_tax,  bc.bill_no
	FROM bill_charge_claim bcc
	JOIN bill_claim bc ON(bc.claim_id=bcc.claim_id AND bc.bill_no=bcc.bill_no) WHERE bcc.bill_no =  b.bill_no AND bc.priority = 2 GROUP BY bc.bill_no) AS sec_claim ON TRUE;

DROP VIEW IF EXISTS accountgrp_and_center_view CASCADE;
CREATE OR REPLACE VIEW accountgrp_and_center_view AS
SELECT center_id AS ac_id,('C'||center_id)::text AS id,center_name AS ac_name,accounting_company_name,
	hospital_center_service_reg_no AS ser_reg_no, 'C' AS type,null AS store_center_id
FROM hospital_center_master hcm
JOIN hosp_accounting_prefs ap ON (ap.all_centers_same_comp_name != 'Y')
WHERE center_id != 0
UNION ALL
SELECT account_group_id AS ac_id,('A'||account_group_id)::text AS id, account_group_name AS ac_name,accounting_company_name,
	account_group_service_reg_no AS ser_reg_no, 'A' AS type,
	case WHEN account_group_id != 1 THEN sto.center_id ELSE null END AS store_center_id
FROM account_group_master agm
JOIN hosp_accounting_prefs ap ON (ap.all_centers_same_comp_name = 'Y' OR account_group_id != 1)
JOIN stores sto ON (sto.account_group=agm.account_group_id or agm.account_group_id=1)
GROUP BY account_group_name, account_group_id, accounting_company_name, account_group_service_reg_no,store_center_id
order by ac_name;

DROP VIEW IF EXISTS charge_alternate_codes_view CASCADE;
CREATE OR REPLACE VIEW charge_alternate_codes_view AS
SELECT b.bill_no,bc.charge_id,ac.item_id,ac.item_code,ac.alternate_code,b.status AS bill_status,
	icl.status AS claim_status,b.finalized_date,icl.last_submission_batch_id,ac.sponsor_id
FROM alternate_activity_codes ac
JOIN bill_charge bc ON(bc.act_description_id = ac.item_id AND
	bc.act_rate_plan_item_code = ac.item_code AND bc.code_type = ac.code_type)
JOIN bill b ON(b.bill_no = bc.bill_no)
JOIN patient_registration pr ON(pr.patient_id = b.visit_id)
JOIN hospital_center_master hcm ON(pr.center_id = hcm.center_id AND
	hcm.health_authority=ac.health_authority)
JOIN bill_claim bcl ON(bcl.bill_no = b.bill_no AND bcl.sponsor_id = ac.sponsor_id)
JOIN insurance_claim icl ON(icl.claim_id = bcl.claim_id)
WHERE bc.charge_id NOT IN(SELECT mo.charge_id FROM
mrd_observations mo WHERE mo.code ilike 'Non-Standard-Code' and mo.sponsor_id=ac.sponsor_id);

DROP VIEW IF EXISTS bill_adjustment_view CASCADE ;
CREATE VIEW  bill_adjustment_view AS
SELECT
	ba.bill_adjustment_id AS adjustment_id, ba.adjustment_type, ba.bill_no, b.visit_id, b.bill_type, b.open_date,
	b.status, b.finalized_date,
	b.closed_date, ba.username, b.discharge_status, b.app_modified, b.visit_type, b.remarks, b.closed_by, b.opened_by,
	b.primary_claim_status,ba.claim_recd_amount,b.cancel_reason, ba.deposit_set_off, b.discount_auth, b.discount_category_id,
	b.restriction_type, b.account_group, ba.total_amount, ba.total_discount, ba.total_claim,
	ba.total_receipts, b.no_of_receipts, b.last_receipt_no, b.procedure_no, b.sponsor_bill_no, ba.primary_total_sponsor_receipts,
	b.primary_no_of_sponsor_receipts, b.last_sponsor_receipt_no, b.dyna_package_id,
	ba.dyna_package_charge, b.obsolete_claim_id, b.is_tpa, b.is_primary_bill, b.payment_status, ba.total_claim_return ,
	b.finalized_by,  b.bill_rate_plan_id, ba.claim_recd_unalloc_amount, b.dyna_pkg_processed, b.bill_printed,
	ba.secondary_total_sponsor_receipts,
	b.secondary_no_of_sponsor, b.secondary_claim_status, b.national_claim_no, b.bill_label_id,b.reopen_reason, b.points_earned,
	b.points_redeemed, ba.points_redeemed_amt, b.audit_control_number, ba.adjustment_remarks,
	null AS mod_time, 0.00 AS approval_amount, 0.00 AS insurance_deduction, 0.00 AS primary_total_claim,
	0.00 AS secondary_total_claim, 0.00 AS primary_approval_amount, 0.00 AS secondary_approval_amount,
	b.cancellation_approved_by,b.cancellation_approval_status, b.total_tax
FROM bill_adjustment ba
JOIN bill b ON(b.bill_no = ba.bill_no)

UNION ALL

SELECT
	bca.bill_claim_adj_id AS adjustment_id, bca.adjustment_type, bca.bill_no, b.visit_id, b.bill_type, b.open_date,
	b.status, b.finalized_date, b.closed_date, null AS username, b.discharge_status,
	b.app_modified, b.visit_type, b.remarks, b.closed_by, b.opened_by,
	b.primary_claim_status, 0.00 AS claim_recd_amount, b.cancel_reason, 0.00 AS deposit_set_off, b.discount_auth, b.discount_category_id,
	b.restriction_type, b.account_group, 0.00 AS total_amount, 0.00 AS total_discount, 0.00 AS total_claim,
	0.00 AS total_receipts, b.no_of_receipts, b.last_receipt_no, b.procedure_no, b.sponsor_bill_no,
	0.00 AS primary_total_sponsor_receipts, b.primary_no_of_sponsor_receipts, b.last_sponsor_receipt_no, b.dyna_package_id,
	0.00 AS dyna_package_charge, b.obsolete_claim_id, b.is_tpa, b.is_primary_bill, b.payment_status,
	0.00 AS total_claim_return , b.finalized_by, b.bill_rate_plan_id, 0.00 AS claim_recd_unalloc_amount, b.dyna_pkg_processed,
	b.bill_printed, 0.00 AS secondary_total_sponsor_receipts, b.secondary_no_of_sponsor, b.secondary_claim_status,
	b.national_claim_no, b.bill_label_id, b.reopen_reason, b.points_earned,
	b.points_redeemed, 0.00 AS points_redeemed_amt, b.audit_control_number, null AS adjustment_remarks,
	bca.mod_time, bca.approval_amount, bca.insurance_deduction, bca.primary_total_claim,bca.secondary_total_claim,
	bca.primary_approval_amount, bca.secondary_approval_amount,b.cancellation_approved_by,b.cancellation_approval_status, b.total_tax
FROM bill_claim_adjustment bca
JOIN bill b ON(b.bill_no = bca.bill_no);

DROP VIEW IF EXISTS bill_charge_adjustment_view CASCADE ;
CREATE VIEW  bill_charge_adjustment_view AS
SELECT
	bca.bill_charge_adjustment_id, bca.adjustment_type, bca.charge_id, bca.bill_no, bca.charge_group, bca.charge_head,
	bc.act_department_id, bc.act_description, bc.act_remarks, bca.act_rate, bc.act_unit, bca.act_quantity, bca.amount,
	bca.discount, bc.discount_reason, bc.charge_ref, bca.paid_amount,bca.posted_date, bca.status, bca.username, bca.mod_time,
	bc.approval_id, bca.orig_rate, bca.package_unit, bca.doctor_amount,bc.doc_payment_id, bc.ref_payment_id,
	bc.oh_payment_id, bc.act_description_id, bc.hasactivity, bca.insurance_claim_amount, bc.payee_doctor_id,
	bca.referal_amount, bca.out_house_amount, bc.prescribing_dr_id, bca.prescribing_dr_amount,bc.prescribing_dr_payment_id,
	bc.overall_discount_auth, bca.overall_discount_amt, bc.discount_auth_dr,bca.dr_discount_amt, bc.discount_auth_pres_dr,
	bca.pres_dr_discount_amt, bc.discount_auth_ref, bca.ref_discount_amt,bc.discount_auth_hosp, bca.hosp_discount_amt,
	bc.activity_conducted, bc.account_group, bc.act_item_code, bc.act_rate_plan_item_code, bc.order_number, bc.allow_discount,
	bc.conducted_datetime, bc.code_type, bca.service_sub_group_id, bc.conducting_doc_mandatory, bc.charge_excluded,
	bc.consultation_type_id, bc.user_remarks, bc.insurance_category_id, bc.claim_status, bca.claim_recd_total,
	bc.denial_code, bc.prior_auth_id, bc.first_of_category, bca.return_insurance_claim_amt, bca.return_amt,
	bca.return_qty, bc.op_id, bc.from_date, bc.to_date, bc.prior_auth_mode_id, bc.allow_rate_increase,
	bc.allow_rate_decrease, bc.item_remarks, bc.denial_remarks, bca.redeemed_points, bca.amount_included, bca.qty_included,
	bc.package_finalized, bca.orig_insurance_claim_amount, bc.status AS bill_charge_status,
	bca.patient_category_id, bca.primary_sponsor_id, bca.secondary_sponsor_id,bca.tax_amt, bca.sponsor_tax_amt
FROM bill_charge_adjustment bca
JOIN bill_charge bc ON (bc.charge_id = bca.charge_id AND bc.bill_no = bca.bill_no) order by bca.bill_charge_adjustment_id;

DROP VIEW IF EXISTS bill_charge_adjustment_unsorted_view CASCADE ;
CREATE VIEW  bill_charge_adjustment_unsorted_view AS
SELECT
	bca.bill_charge_adjustment_id, bca.adjustment_type, bca.charge_id, bca.bill_no, bca.charge_group, bca.charge_head,
	bc.act_department_id, bc.act_description, bc.act_remarks, bca.act_rate, bc.act_unit, bca.act_quantity, bca.amount,
	bca.discount, bc.discount_reason, bc.charge_ref, bca.paid_amount,bca.posted_date, bca.status, bca.username, bca.mod_time,
	bc.approval_id, bca.orig_rate, bca.package_unit, bca.doctor_amount,bc.doc_payment_id, bc.ref_payment_id,
	bc.oh_payment_id, bc.act_description_id, bc.hasactivity, bca.insurance_claim_amount, bc.payee_doctor_id,
	bca.referal_amount, bca.out_house_amount, bc.prescribing_dr_id, bca.prescribing_dr_amount,bc.prescribing_dr_payment_id,
	bc.overall_discount_auth, bca.overall_discount_amt, bc.discount_auth_dr,bca.dr_discount_amt, bc.discount_auth_pres_dr,
	bca.pres_dr_discount_amt, bc.discount_auth_ref, bca.ref_discount_amt,bc.discount_auth_hosp, bca.hosp_discount_amt,
	bc.activity_conducted, bc.account_group, bc.act_item_code, bc.act_rate_plan_item_code, bc.order_number, bc.allow_discount,
	bc.conducted_datetime, bc.code_type, bca.service_sub_group_id, bc.conducting_doc_mandatory, bc.charge_excluded,
	bc.consultation_type_id, bc.user_remarks, bc.insurance_category_id, bc.claim_status, bca.claim_recd_total,
	bc.denial_code, bc.prior_auth_id, bc.first_of_category, bca.return_insurance_claim_amt, bca.return_amt,
	bca.return_qty, bc.op_id, bc.from_date, bc.to_date, bc.prior_auth_mode_id, bc.allow_rate_increase,
	bc.allow_rate_decrease, bc.item_remarks, bc.denial_remarks, bca.redeemed_points, bca.amount_included, bca.qty_included,
	bc.package_finalized, bca.orig_insurance_claim_amount, bc.status as bill_charge_status,
	bca.patient_category_id, bca.primary_sponsor_id, bca.secondary_sponsor_id,bca.tax_amt, bca.sponsor_tax_amt
FROM bill_charge_adjustment bca
JOIN bill_charge bc ON (bc.charge_id = bca.charge_id AND bc.bill_no = bca.bill_no);

DROP VIEW IF EXISTS diag_outsource_view CASCADE;
CREATE OR REPLACE VIEW diag_outsource_view AS
SELECT CASE WHEN outsource_dest_type='C' THEN hcm.center_name ELSE om.oh_name END AS oh_name,
	outsource_dest AS oh_id
FROM diag_outsource_master dom
LEFT JOIN outhouse_master om ON (dom.outsource_dest=om.oh_id)
LEFT JOIN hospital_center_master hcm ON (hcm.center_id::text = dom.outsource_dest);

DROP FUNCTION IF EXISTS modify_patient_header_pref() CASCADE;
CREATE OR REPLACE FUNCTION modify_patient_header_pref() RETURNS TRIGGER AS $$
DECLARE
    i integer;
    pcolName character varying;
    regFieldName character varying;
    regColName character varying;
    defaultValue character varying;
BEGIN
   i:=1;
   while(i<20)
    loop
         regColName := 'custom_field' || i::text || '_label';
         pcolName := 'custom_field'|| i::text;
         regFieldName := '(' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').' || regColName;
         defaultValue := 'Custom Field ' || i::text;

          EXECUTE $SomeTag$UPDATE patient_header_preferences SET field_desc=CASE WHEN ($SomeTag$ || regFieldName || $SomeTag$ !='') THEN $SomeTag$ || regFieldName || $SomeTag$ ELSE '$SomeTag$|| defaultValue || $SomeTag$' END WHERE field_name = '$SomeTag$ || pcolName || $SomeTag$'$SomeTag$;
         i:=i+1;
   END loop;

   i := 1;
   while(i<10)
     loop
        regColName := 'custom_list' || i::text || '_name';
        pcolName := 'custom_list' || i::text ||'_value';
        regFieldName := '(' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').' || regColName;
        defaultValue := 'Custom List ' || i::text;

          EXECUTE $SomeTag$UPDATE patient_header_preferences SET field_desc=CASE WHEN ($SomeTag$ || regFieldName || $SomeTag$ !='') THEN $SomeTag$ || regFieldName || $SomeTag$ ELSE '$SomeTag$|| defaultValue || $SomeTag$' END WHERE field_name = '$SomeTag$ || pcolName || $SomeTag$'$SomeTag$;
        i:=i+1;
     END loop;

     i := 1;
     while(i<3)
      loop
        regColName := 'visit_custom_list' || i::text || '_name';
        pcolName := 'visit_custom_list' || i::text ;
        regFieldName := '(' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').' || regColName;
        defaultValue := 'Visit Custom List ' || i::text;

          EXECUTE $SomeTag$UPDATE patient_header_preferences SET field_desc=CASE WHEN ($SomeTag$ || regFieldName || $SomeTag$ !='') THEN $SomeTag$ || regFieldName || $SomeTag$ ELSE '$SomeTag$|| defaultValue || $SomeTag$' END WHERE field_name = '$SomeTag$ || pcolName || $SomeTag$'$SomeTag$;
          i:=i+1;
      END loop;

      i := 1;
      while(i<10)
      loop
        regColName := 'visit_custom_field' || i::text || '_name';
        pcolName := 'visit_custom_field' || i::text ;
        regFieldName := '(' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').' || regColName;
        defaultValue := 'Visit Custom Field ' || i::text;

        EXECUTE $SomeTag$UPDATE patient_header_preferences SET field_desc=CASE WHEN ($SomeTag$ || regFieldName || $SomeTag$ !='') THEN $SomeTag$ || regFieldName || $SomeTag$ ELSE '$SomeTag$|| defaultValue || $SomeTag$' END WHERE field_name = '$SomeTag$ || pcolName || $SomeTag$'$SomeTag$;
        i:=i+1;
     END loop;

     regColName := 'family_id';
     pcolName := 'family_id';
     regFieldName := '(' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').' || regColName;
     defaultValue := 'Family ID';

      EXECUTE $SomeTag$UPDATE patient_header_preferences SET field_desc=CASE WHEN ($SomeTag$ || regFieldName || $SomeTag$ !='') THEN $SomeTag$ || regFieldName || $SomeTag$ ELSE '$SomeTag$|| defaultValue || $SomeTag$' END WHERE field_name = '$SomeTag$ || pcolName || $SomeTag$'$SomeTag$;

     regColName := 'nationality';
     pcolName := 'nationality_name';
     regFieldName := '(' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').' || regColName;
     defaultValue := 'Nationality';

      EXECUTE $SomeTag$UPDATE patient_header_preferences SET field_desc=CASE WHEN ($SomeTag$ || regFieldName || $SomeTag$ !='') THEN $SomeTag$ || regFieldName || $SomeTag$ ELSE '$SomeTag$|| defaultValue || $SomeTag$' END WHERE field_name = '$SomeTag$ || pcolName || $SomeTag$'$SomeTag$;

      
     regColName := 'government_identifier_label';
     pcolName := 'government_identifier';
     regFieldName := '(' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').' || regColName;
     defaultValue := 'Government Identifier';

      EXECUTE $SomeTag$UPDATE patient_header_preferences SET field_desc=CASE WHEN ($SomeTag$ || regFieldName || $SomeTag$ !='') THEN $SomeTag$ || regFieldName || $SomeTag$ ELSE '$SomeTag$|| defaultValue || $SomeTag$' END WHERE field_name = '$SomeTag$ || pcolName || $SomeTag$'$SomeTag$;

     regColName := 'passport_no';
     pcolName := 'passport_no';
     regFieldName := '(' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').' || regColName;
     defaultValue := 'Passport No';

      EXECUTE $SomeTag$UPDATE patient_header_preferences SET field_desc=CASE WHEN ($SomeTag$ || regFieldName || $SomeTag$ !='') THEN $SomeTag$ || regFieldName || $SomeTag$ ELSE '$SomeTag$|| defaultValue || $SomeTag$' END WHERE field_name = '$SomeTag$ || pcolName || $SomeTag$'$SomeTag$;

     regColName := 'passport_no';
     pcolName := 'passport_no';
     regFieldName := '(' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').' || regColName;
     defaultValue := 'Passport No';

      EXECUTE $SomeTag$UPDATE patient_header_preferences SET field_desc=CASE WHEN ($SomeTag$ || regFieldName || $SomeTag$ !='') THEN $SomeTag$ || regFieldName || $SomeTag$ ELSE '$SomeTag$|| defaultValue || $SomeTag$' END WHERE field_name = '$SomeTag$ || pcolName || $SomeTag$'$SomeTag$;

      regColName := 'passport_validity';
      pcolName := 'passport_validity';
      regFieldName := '(' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').' || regColName;
      defaultValue := 'Passport Validity';

      EXECUTE $SomeTag$UPDATE patient_header_preferences SET field_desc=CASE WHEN ($SomeTag$ || regFieldName || $SomeTag$ !='') THEN $SomeTag$ || regFieldName || $SomeTag$ ELSE '$SomeTag$|| defaultValue || $SomeTag$' END WHERE field_name = '$SomeTag$ || pcolName || $SomeTag$'$SomeTag$;

      regColName := 'visa_validity';
      pcolName := 'visa_validity';
      regFieldName := '(' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').' || regColName;
      defaultValue := 'Visa Validity';

      EXECUTE $SomeTag$UPDATE patient_header_preferences SET field_desc=CASE WHEN ($SomeTag$ || regFieldName || $SomeTag$ !='') THEN $SomeTag$ || regFieldName || $SomeTag$ ELSE '$SomeTag$|| defaultValue || $SomeTag$' END WHERE field_name = '$SomeTag$ || pcolName || $SomeTag$'$SomeTag$;

      regColName := 'member_id_label';
      pcolName := 'member_id';
      regFieldName := '(' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').' || regColName;
      defaultValue := 'Member Id';

      EXECUTE $SomeTag$UPDATE patient_header_preferences SET field_desc=CASE WHEN ($SomeTag$ || regFieldName || $SomeTag$ !='') THEN $SomeTag$ || regFieldName || $SomeTag$ ELSE '$SomeTag$|| defaultValue || $SomeTag$' END WHERE field_name = '$SomeTag$ || pcolName || $SomeTag$'$SomeTag$;

      regColName := 'old_reg_field_label';
      pcolName := 'oldmrno';
      regFieldName := '(' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').' || regColName;
      defaultValue := 'Old MR No';

      EXECUTE $SomeTag$UPDATE patient_header_preferences SET field_desc=CASE WHEN ($SomeTag$ || regFieldName || $SomeTag$ !='') THEN $SomeTag$ || regFieldName || $SomeTag$ ELSE '$SomeTag$|| defaultValue || $SomeTag$' END WHERE field_name = '$SomeTag$ || pcolName || $SomeTag$'$SomeTag$;

RETURN NEW;
END;
$$ language plpgsql;

DROP TRIGGER IF EXISTS update_patient_header_preferences ON registration_preferences;
CREATE TRIGGER update_patient_header_preferences after UPDATE ON registration_preferences FOR each row EXECUTE PROCEDURE modify_patient_header_pref();

DROP VIEW IF EXISTS indent_dispense_type_view CASCADE;
CREATE OR REPLACE VIEW indent_dispense_type_view AS
SELECT patient_indent_no, salecount, issuecount,
CASE
 WHEN (salecount > 0 AND issuecount > 0) THEN 'Sold/Issued'
 WHEN (salecount > 0 AND issuecount <= 0) THEN 'Sold'
 WHEN (salecount <= 0 AND issuecount > 0) THEN 'Issued'
ELSE NULL END AS dispense_type
FROM (
	SELECT patient_indent_no, COUNT(sale_item_id) AS salecount,
	COUNT(item_issue_no) AS issuecount
	FROM store_patient_indent_details
	WHERE sale_item_id IS NOT NULL OR item_issue_no IS NOT NULL GROUP BY
	patient_indent_no ORDER BY 1 ) AS foo;

DROP VIEW IF EXISTS patient_growth_chart_data_view CASCADE;
CREATE VIEW  patient_growth_chart_data_view AS
SELECT gcdata.*, calculate_bmi(gcdata.height, gcdata.weight) as bmi FROM (
SELECT pd.mr_no,pd.patient_gender,vr.vital_reading_id,
get_patient_age_in_months_with_precision_as_of(pd.dateofbirth, pd.expected_dob, date(vv.date_time))::numeric AS age_in_months,
(get_patient_age_in_months_as_of(pd.dateofbirth, pd.expected_dob, date(vv.date_time))/12)::numeric AS age_in_years,
max(case WHEN param_label = 'Weight' THEN cast_to_numeric(param_value) end)::numeric AS weight,
max(case WHEN param_label = 'Head Circumference' THEN cast_to_numeric(param_value) end)::numeric AS head_circumference,
max(case WHEN param_label = 'Height' THEN cast_to_numeric(param_value) end)::numeric AS height
    FROM vital_reading vr
    JOIN visit_vitals vv USING (vital_reading_id)
    JOIN vital_parameter_master vpm USING (param_id)
    JOIN patient_registration pr USING (patient_id)
    JOIN patient_details pd using (mr_no)
WHERE vpm.param_container='V' AND vpm.system_vital = 'Y' AND vv.status = 'A' AND vr.status='A' AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )
GROUP BY vr.vital_reading_id,pd.mr_no,pd.patient_gender,pd.dateofbirth, pd.expected_dob, vv.date_time) gcdata
ORDER BY gcdata.vital_reading_id DESC ;



DROP FUNCTION IF EXISTS receive_stock_transfer_log() CASCADE;
CREATE OR REPLACE FUNCTION receive_stock_transfer_log() RETURNS TRIGGER AS $BODY$

BEGIN
	IF (OLD.qty_recd != NEW.qty_recd OR OLD.qty_rejected != NEW.qty_rejected) THEN
		INSERT INTO store_transfer_receive_indent (transfer_detail_no, qty_recd, qty_rejected, cost_value, receive_datetime)
		VALUES (NEW.transfer_detail_no, NEW.qty_recd - OLD.qty_recd, NEW.qty_rejected - OLD.qty_rejected, NEW.cost_value - OLD.cost_value, current_timestamp);
	END IF;
RETURN NEW;
END;
$BODY$ language plpgsql;

DROP TRIGGER IF EXISTS receive_stock_transfer_log_tigger ON store_transfer_details;
CREATE TRIGGER receive_stock_transfer_log_tigger
  AFTER UPDATE
  ON store_transfer_details
  FOR EACH ROW
  EXECUTE PROCEDURE receive_stock_transfer_log();

DROP VIEW IF EXISTS emr_access_u_role_view;
CREATE VIEW emr_access_u_role_view AS
SELECT * FROM u_role WHERE role_id NOT IN(1,2) ORDER BY role_name;

DROP VIEW IF EXISTS emr_access_u_user_view;
CREATE VIEW emr_access_u_user_view AS
SELECT * FROM u_user WHERE role_id NOT IN(1,2) ORDER BY emp_username;

DROP VIEW IF EXISTS message_group_dispatcher_view;
CREATE VIEW message_group_dispatcher_view AS
SELECT mg.message_group, mdc.* FROM message_dispatcher_config mdc
JOIN (SELECT 'general'::text AS message_group) AS mg ON (mdc.message_mode in ('SMS', 'EMAIL', 'NOTIFICATION'));

DROP VIEW IF EXISTS advanced_operations_view cascade;
CREATE OR REPLACE VIEW advanced_operations_view AS
select * FROM (select mr_no,bo.prescribed_id AS prescription_id,bo.operation_name AS operation_id,bo.status
FROM bed_operation_schedule bo
LEFT JOIN operation_procedures op ON(op.prescribed_id=bo.prescribed_id AND op.oper_priority='P')
UNION ALL
select mr_no,bos.sec_prescribed_id AS prescription_id,bos.operation_id,bo.status
FROM bed_operation_schedule bo
LEFT JOIN bed_operation_secondary bos ON(bo.prescribed_id=bos.prescribed_id)
JOIN operation_procedures op ON(op.prescribed_id=bos.sec_prescribed_id AND op.oper_priority='S')) AS foo
WHERE status != 'U' and status!='X';

DROP VIEW IF EXISTS bill_charge_claim_activity_view;
CREATE VIEW bill_charge_claim_activity_view AS
SELECT ic.last_submission_batch_id,bcc.claim_id,min(bcc.charge_id) AS min_charge_id,act_rate_plan_item_code,
	posted_date::date AS posted_date, count(*) AS no_of_items,bc.code_type,
	coalesce(bcc.prior_auth_id, pip.prior_auth_id, '') AS prior_auth_id,
	aac.alternate_code, mccg.code_group
FROM bill_charge_claim bcc
JOIN bill_charge bc ON(bc.charge_id=bcc.charge_id)
JOIN insurance_claim ic ON(bcc.claim_id = ic.claim_id)
JOIN patient_insurance_plans pip ON(pip.patient_id = ic.patient_id and pip.plan_id = ic.plan_id
	and pip.sponsor_id = bcc.sponsor_id)
LEFT JOIN mrd_codes_master mcm ON(mcm.code = bc.act_rate_plan_item_code AND mcm.code_type = bc.code_type)
LEFT JOIN mrd_code_claim_groups mccg ON(mccg.mrd_code_id = mcm.mrd_code_id)
LEFT JOIN alternate_activity_codes  aac ON(aac.item_id = bc.act_description_id and aac.item_code = bc.act_rate_plan_item_code
	and aac.code_type = bc.code_type and aac.sponsor_id = bcc.sponsor_id)
GROUP BY bcc.claim_id,act_rate_plan_item_code,posted_date::date,ic.last_submission_batch_id,
	bc.code_type, coalesce(bcc.prior_auth_id, pip.prior_auth_id, ''), aac.alternate_code, mccg.code_group;
	
DROP VIEW IF EXISTS unlisted_bill_charge_claim_activity_view;
CREATE VIEW unlisted_bill_charge_claim_activity_view AS
SELECT ic.last_submission_batch_id,bcc.claim_id,min(bcc.charge_id) AS min_charge_id,act_rate_plan_item_code,
	posted_date::date AS posted_date, count(*) AS no_of_items,bc.code_type,
	coalesce(bcc.prior_auth_id, pip.prior_auth_id, '') AS prior_auth_id,
	aac.alternate_code, mccg.code_group, bc.act_description_id, bc.act_description
FROM bill_charge_claim bcc
JOIN bill_charge bc ON(bc.charge_id=bcc.charge_id)
JOIN insurance_claim ic ON(bcc.claim_id = ic.claim_id)
JOIN patient_insurance_plans pip ON(pip.patient_id = ic.patient_id and pip.plan_id = ic.plan_id
	and pip.sponsor_id = bcc.sponsor_id)
JOIN mrd_codes_master mcm ON(mcm.code = bc.act_rate_plan_item_code AND mcm.code_type = bc.code_type)
JOIN mrd_code_claim_groups mccg ON(mccg.mrd_code_id = mcm.mrd_code_id AND mccg.code_group = 'UG')
LEFT JOIN alternate_activity_codes  aac ON(aac.item_id = bc.act_description_id and aac.item_code = bc.act_rate_plan_item_code
	and aac.code_type = bc.code_type and aac.sponsor_id = bcc.sponsor_id)
GROUP BY bcc.claim_id,act_rate_plan_item_code,posted_date::date,ic.last_submission_batch_id,
	bc.code_type, coalesce(bcc.prior_auth_id, pip.prior_auth_id, ''), aac.alternate_code, mccg.code_group, 
	bc.act_description_id, bc.act_description;

DROP VIEW IF EXISTS sales_claim_activity_view;
CREATE VIEW sales_claim_activity_view AS
SELECT ic.last_submission_batch_id,scd.claim_id,min(scd.sale_item_id) AS min_sale_item_id,item_code,sale_date::date AS posted_date,
count(*) AS no_of_items, sd.code_type, min(sm.charge_id) AS min_charge_id,
coalesce(scd.prior_auth_id, pip.prior_auth_id, '') AS prior_auth_id,m.issue_base_unit,mccg.code_group
FROM sales_claim_details scd
JOIN store_sales_details sd ON(scd.sale_item_id=sd.sale_item_id)
JOIN store_sales_main sm ON(sd.sale_id = sm.sale_id)
JOIN store_item_details m ON (sd.medicine_id = m.medicine_id)
JOIN insurance_claim ic ON(scd.claim_id = ic.claim_id)
JOIN patient_insurance_plans pip ON(pip.patient_id = ic.patient_id and pip.plan_id = ic.plan_id
	and pip.sponsor_id = scd.sponsor_id)
LEFT JOIN mrd_codes_master mcm ON(mcm.code = sd.item_code AND mcm.code_type = sd.code_type)
LEFT JOIN mrd_code_claim_groups mccg ON(mccg.mrd_code_id = mcm.mrd_code_id)
GROUP BY scd.claim_id,item_code,sale_date::date,ic.last_submission_batch_id,sd.code_type,issue_base_unit,
	coalesce(scd.prior_auth_id, pip.prior_auth_id, ''), mccg.code_group;
	
DROP VIEW IF EXISTS sales_unlisted_items_claim_activity_view;
CREATE VIEW sales_unlisted_items_claim_activity_view AS
SELECT ic.last_submission_batch_id,scd.claim_id,min(scd.sale_item_id) AS min_sale_item_id,item_code,sale_date::date AS posted_date,
count(*) AS no_of_items, sd.code_type, min(sm.charge_id) AS min_charge_id,
coalesce(scd.prior_auth_id, pip.prior_auth_id, '') AS prior_auth_id,m.issue_base_unit,mccg.code_group, sd.medicine_id
FROM sales_claim_details scd
JOIN store_sales_details sd ON(scd.sale_item_id=sd.sale_item_id)
JOIN store_sales_main sm ON(sd.sale_id = sm.sale_id)
JOIN store_item_details m ON (sd.medicine_id = m.medicine_id)
JOIN insurance_claim ic ON(scd.claim_id = ic.claim_id)
JOIN patient_insurance_plans pip ON(pip.patient_id = ic.patient_id and pip.plan_id = ic.plan_id
	and pip.sponsor_id = scd.sponsor_id)
JOIN mrd_codes_master mcm ON(mcm.code = sd.item_code AND mcm.code_type = sd.code_type)
JOIN mrd_code_claim_groups mccg ON(mccg.mrd_code_id = mcm.mrd_code_id AND mccg.code_group='UG')
GROUP BY scd.claim_id,item_code,sale_date::date,ic.last_submission_batch_id,sd.code_type,issue_base_unit,
	coalesce(scd.prior_auth_id, pip.prior_auth_id, ''), mccg.code_group, sd.medicine_id;

--VIEWS FOR BLOOD BANK--

-- DONOR _GROUPING VIEW --
DROP VIEW IF EXISTS donor_grouping_view CASCADE;

 -- DONOR REGISTRATION VIEW --
DROP VIEW IF EXISTS donor_details_view CASCADE;

-- DONOR COMPONENT VIEW --
DROP VIEW IF EXISTS donor_comp_view CASCADE;

DROP VIEW IF EXISTS patient_return_indentable_batch_items CASCADE;
CREATE VIEW patient_return_indentable_batch_items AS
SELECT medicine_id,medicine_name,batch_no,manf_name,category,package_uom,issue_base_unit,
		issue_units,visit_id,sum(qty) AS qty_avbl,package_type,store_id,process_type,item_batch_id,exp_dt
FROM (
	SELECT sid.medicine_id,sid.medicine_name,m.manf_name,scm.category,sid.package_uom,sid.issue_base_unit,
		sid.issue_units,b.visit_id,quantity AS qty,package_type,store_id,'S' AS process_type, ssd.batch_no, ssd.item_batch_id,ssd.expiry_date AS exp_dt
		FROM store_sales_main ssm
		JOIN bill b USING(bill_no)
		JOIN store_sales_details ssd USING(sale_id)
		JOIN store_item_details sid USING(medicine_id)
		JOIN manf_master m ON ( sid.manf_name = m.manf_code )
		JOIN store_category_master scm ON med_category_id=category_id
	UNION ALL
	SELECT sid.medicine_id,sid.medicine_name,m.manf_name,scm.category,
		sid.package_uom,sid.issue_base_unit,sid.issue_units,issued_to AS visit_id,
		qty-return_qty AS qty,package_type,dept_from AS store_id,'I' AS process_type, ssd.batch_no, ssd.item_batch_id,sibd.exp_dt
		FROM stock_issue_main ssm
		JOIN stock_issue_details ssd USING(user_issue_no)
		JOIN store_item_details sid USING(medicine_id)
		JOIN manf_master m ON ( sid.manf_name = m.manf_code )
		JOIN store_category_master scm ON med_category_id=category_id
		JOIN store_item_batch_details sibd on(ssd.item_batch_id=sibd.item_batch_id)
	) AS foo
	GROUP BY medicine_id,medicine_name,batch_no,manf_name,category,package_uom,issue_base_unit,
		issue_units,visit_id,package_type,store_id,process_type,item_batch_id,exp_dt;

DROP VIEW IF EXISTS centerwise_doctors_view;
DROP VIEW IF EXISTS distinct_doctors_view;

-- Sets the revision number WHEN there is an insert or update in tests_prescribed table.

DROP FUNCTION IF EXISTS set_revision_number() CASCADE;
CREATE OR REPLACE FUNCTION set_revision_number() RETURNS TRIGGER AS $BODY$

	BEGIN
		NEW.revision_number = (select extract(epoch from current_timestamp));

	RETURN NEW;
	END;

$BODY$ language plpgsql;

DROP TRIGGER IF EXISTS set_revision_number_trigger ON tests_prescribed CASCADE;
CREATE TRIGGER set_revision_number_trigger
	BEFORE INSERT OR UPDATE ON tests_prescribed
	FOR EACH ROW
	EXECUTE PROCEDURE set_revision_number();


DROP FUNCTION IF EXISTS patient_demographics_mod_trigger() CASCADE;
CREATE OR REPLACE function patient_demographics_mod_trigger() RETURNS TRIGGER AS $BODY$
DECLARE
	modified character(1);
	patient RECORD;
	record_exists character varying(15);
BEGIN
	IF TG_OP = 'INSERT' THEN
		INSERT INTO patient_demographics_mod values(NEW.mr_no, localtimestamp(0));
	ELSEIF TG_OP = 'UPDATE' THEN
		modified := 'N';
		select * into patient from patient_details where mr_no=NEW.mr_no;

		IF (coalesce(patient.patient_name, '') != coalesce(NEW.patient_name, '')) THEN

			modified := 'Y';
		ELSEIF (coalesce(patient.patient_gender, '') != coalesce(NEW.patient_gender, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.patient_care_oftext, '') != coalesce(NEW.patient_care_oftext, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.patient_address, '') != coalesce(NEW.patient_address, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.patient_city, '') != coalesce(NEW.patient_city, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.patient_state, '') != coalesce(NEW.patient_state, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.patient_phone, '') != coalesce(NEW.patient_phone, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.salutation, '') != coalesce(NEW.salutation, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.last_name, '') != coalesce(NEW.last_name, '')) THEN
			modified := 'Y';
		ELSEIF (case WHEN patient.dateofbirth is null and NEW.dateofbirth is not null THEN true
					when patient.dateofbirth is not null and NEW.dateofbirth is null THEN true
					when patient.dateofbirth != NEW.dateofbirth THEN true ELSE false end) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.country, '') != coalesce(NEW.country, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.cflag, 0) != coalesce(NEW.cflag, 0)) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.patient_careof_address, '') != coalesce(NEW.patient_careof_address, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.relation, '') != coalesce(NEW.relation, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.custom_list5_value, '') != coalesce(NEW.custom_list5_value, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.custom_list6_value, '') != coalesce(NEW.custom_list6_value, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.user_name, '') != coalesce(NEW.user_name, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.oldmrno, '') != coalesce(NEW.oldmrno, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.custom_list4_value, '') != coalesce(NEW.custom_list4_value, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.emr_access, '') != coalesce(NEW.emr_access, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.remarks, '') != coalesce(NEW.remarks, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.custom_field1, '') != coalesce(NEW.custom_field1, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.custom_field2, '') != coalesce(NEW.custom_field2, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.custom_field3, '') != coalesce(NEW.custom_field3, '')) THEN
			modified := 'Y';
		ELSEIF (case WHEN patient.expected_dob is null and NEW.expected_dob is not null THEN true
					when patient.expected_dob is not null and NEW.expected_dob is null THEN true
					when patient.expected_dob != NEW.expected_dob THEN true ELSE false end) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.timeofbirth, '') != coalesce(NEW.timeofbirth, '')) THEN
			modified := 'Y';
		ELSEIF (case WHEN patient.portal_access is null and NEW.portal_access is not null THEN true
					when patient.portal_access is not null and NEW.portal_access is null THEN true
					when patient.portal_access != NEW.portal_access THEN true ELSE false end) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.email_id, '') != coalesce(NEW.email_id, '')) THEN
			modified := 'Y';

		ELSEIF (coalesce(patient.patient_area, '') != coalesce(NEW.patient_area, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.custom_field5, '') != coalesce(NEW.custom_field5, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.custom_field4, '') != coalesce(NEW.custom_field4, '')) THEN
			modified := 'Y';
		ELSEIF (case WHEN patient.patient_photo is null and NEW.patient_photo is not null THEN true
					when patient.patient_photo is not null and NEW.patient_photo is null THEN true
					when patient.patient_photo != NEW.patient_photo THEN true ELSE false end) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.patient_category_id, 0) != coalesce(NEW.patient_category_id, 0)) THEN
			modified := 'Y';
		ELSEIF (case WHEN patient.category_expiry_date is null and NEW.category_expiry_date is not null THEN true
					when patient.category_expiry_date is not null and NEW.category_expiry_date is null THEN true
					when patient.category_expiry_date != NEW.category_expiry_date THEN true ELSE false end) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.casefile_no, '') != coalesce(NEW.casefile_no, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.custom_field6, '') != coalesce(NEW.custom_field6, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.custom_field7, '') != coalesce(NEW.custom_field7, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.custom_field8, '') != coalesce(NEW.custom_field8, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.custom_field9, '') != coalesce(NEW.custom_field9, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.custom_field10, '') != coalesce(NEW.custom_field10, '')) THEN
			modified := 'Y';
		ELSEIF (case WHEN patient.death_date is null and NEW.death_date is not null THEN true
					when patient.death_date is not null and NEW.death_date is null THEN true
					when patient.death_date != NEW.death_date THEN true ELSE false end) THEN
			modified := 'Y';
		ELSEIF (case WHEN patient.death_time is null and NEW.death_time is not null THEN true
					when patient.death_time is not null and NEW.death_time is null THEN true
					when patient.death_time != NEW.death_time THEN true ELSE false end) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.patient_phone2, '') != coalesce(NEW.patient_phone2, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.custom_field11, '') != coalesce(NEW.custom_field11, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.custom_field12, '') != coalesce(NEW.custom_field12, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.custom_field13, '') != coalesce(NEW.custom_field13, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.next_of_kin_relation, '') != coalesce(NEW.next_of_kin_relation, '')) THEN
			modified := 'Y';
		ELSEIF (trim(coalesce(patient.middle_name, '')) != trim(coalesce(NEW.middle_name, ''))) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.custom_list1_value, '') != coalesce(NEW.custom_list1_value, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.custom_list2_value, '') != coalesce(NEW.custom_list2_value, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.custom_list3_value, '') != coalesce(NEW.custom_list3_value, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.patient_consultation_info, '') != coalesce(NEW.patient_consultation_info, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.vip_status, 'N') != coalesce(NEW.vip_status, 'N')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.med_allergies, '') != coalesce(NEW.med_allergies, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.food_allergies, '') != coalesce(NEW.food_allergies, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.other_allergies, '') != coalesce(NEW.other_allergies, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.government_identifier, '') != coalesce(NEW.government_identifier, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.identifier_id, 0) != coalesce(NEW.identifier_id, 0)) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.dead_on_arrival, '') != coalesce(NEW.dead_on_arrival, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.passport_no, '') != coalesce(NEW.passport_no, '')) THEN
			modified := 'Y';
		ELSEIF (case WHEN patient.passport_validity is null and NEW.passport_validity is not null THEN true
					when patient.passport_validity is not null and NEW.passport_validity is null THEN true
					when patient.passport_validity != NEW.passport_validity THEN true ELSE false end) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.passport_issue_country, '') != coalesce(NEW.passport_issue_country, '')) THEN
			modified := 'Y';
		ELSEIF (case WHEN patient.visa_validity is null and NEW.visa_validity is not null THEN true
					when patient.visa_validity is not null and NEW.visa_validity is null THEN true
					when patient.visa_validity != NEW.visa_validity THEN true ELSE false end) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.family_id, '') != coalesce(NEW.family_id, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.death_reason_id, 0) != coalesce(NEW.death_reason_id, 0)) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.custom_list7_value, '') != coalesce(NEW.custom_list7_value, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.custom_list8_value, '') != coalesce(NEW.custom_list8_value, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.custom_list9_value, '') != coalesce(NEW.custom_list9_value, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.sms_for_vaccination, '') != coalesce(NEW.sms_for_vaccination, '')) THEN
			modified := 'Y';
		ELSEIF (case WHEN patient.custom_field14 is null and NEW.custom_field14 is not null THEN true
					when patient.custom_field14 is not null and NEW.custom_field14 is null THEN true
					when patient.custom_field14 != NEW.custom_field14 THEN true ELSE false end) THEN
			modified := 'Y';
		ELSEIF (case WHEN patient.custom_field15 is null and NEW.custom_field15 is not null THEN true
					when patient.custom_field15 is not null and NEW.custom_field15 is null THEN true
					when patient.custom_field15 != NEW.custom_field15 THEN true ELSE false end) THEN
			modified := 'Y';
		ELSEIF (case WHEN patient.custom_field16 is null and NEW.custom_field16 is not null THEN true
					when patient.custom_field16 is not null and NEW.custom_field16 is null THEN true
					when patient.custom_field16 != NEW.custom_field16 THEN true ELSE false end) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.custom_field17, 0) != coalesce(NEW.custom_field17, 0)) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.custom_field18, 0) != coalesce(NEW.custom_field18, 0)) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.custom_field19, 0) != coalesce(NEW.custom_field19, 0)) THEN
			modified := 'Y';
		ELSEIF (case WHEN patient.mobile_access is null and NEW.mobile_access is not null THEN true
					when patient.mobile_access is not null and NEW.mobile_access is null THEN true
					when patient.mobile_access != NEW.mobile_access THEN true ELSE false end) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.mobile_password, '') != coalesce(NEW.mobile_password, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.no_allergies, '') != coalesce(NEW.no_allergies, '')) THEN
			modified := 'Y';
		ELSEIF (coalesce(patient.name_local_language, '') != coalesce(NEW.name_local_language, '')) THEN
			modified := 'Y';

		END IF;

		IF modified = 'Y' THEN
			select mr_no into record_exists from patient_demographics_mod where mr_no=NEW.mr_no;
			IF record_exists is not null THEN
				UPDATE patient_demographics_mod SET mod_time=localtimestamp(0) where mr_no=NEW.mr_no;
			ELSE
				INSERT INTO patient_demographics_mod values(NEW.mr_no, localtimestamp(0));
			END IF;
		END IF;

	END IF;

	RETURN NEW;
END;
$BODY$ language plpgsql;

DROP TRIGGER IF EXISTS patient_demographics_mod_trigger ON patient_details CASCADE;
CREATE TRIGGER patient_demographics_mod_trigger
	BEFORE INSERT OR UPDATE ON patient_details
	FOR EACH ROW
	EXECUTE PROCEDURE patient_demographics_mod_trigger();


DROP VIEW IF EXISTS insurance_payable_bill_charges_view CASCADE;
CREATE VIEW insurance_payable_bill_charges_view AS
SELECT bc.*,0.00 AS pri_insurance_claim_amount, 0.00 AS sec_insurance_claim_amount,0.00 AS pri_claim_tax_amt,0.00 AS sec_claim_tax_amt,
CASE WHEN chc.insurance_payable='Y' THEN true ELSE false END AS is_charge_head_payable,
true AS store_item_category_payable, true AS pri_include_in_claim_calc, true AS sec_include_in_claim_calc, 'N'::bpchar AS claim_amount_includes_tax, 'N'::bpchar AS limit_includes_tax
FROM bill_charge bc JOIN chargehead_constants chc ON (bc.charge_head = chc.chargehead_id);

DROP VIEW IF EXISTS visit_insurance_details_view CASCADE;
CREATE VIEW visit_insurance_details_view AS
SELECT pipd.visit_id, pipd.plan_id, pipd.insurance_category_id,
 	pipd.patient_amount, pipd.patient_percent,  pipd.patient_amount_cap,  pipd.per_treatment_limit, pipd.patient_type,
 	pipd.patient_amount_per_category, ipm.is_copay_pc_on_post_discnt_amt,  pip.priority, pip.sponsor_id, 
 	CASE WHEN iic.insurance_payable='Y' THEN true ELSE false END AS is_category_payable,
	pip.plan_limit,
	CASE WHEN (ipm.limits_include_followup='Y' AND pr.visit_type='o') THEN pip.episode_limit ELSE pip.visit_limit END AS visit_limit ,
	CASE WHEN (ipm.limits_include_followup='Y' AND pr.visit_type='o') THEN pip.episode_deductible ELSE pip.visit_deductible END AS visit_deductible ,
	CASE WHEN (ipm.limits_include_followup='Y' AND pr.visit_type='o') THEN pip.episode_copay_percentage ELSE pip.visit_copay_percentage END AS visit_copay_percentage ,
	CASE WHEN (ipm.limits_include_followup='Y' AND pr.visit_type='o') THEN pip.episode_max_copay_percentage ELSE pip.visit_max_copay_percentage END AS visit_max_copay_percentage ,
	pip.visit_per_day_limit, ipm.limits_include_followup, pr.reg_date, pr.discharge_date, ipd.category_payable AS plan_category_payable,
	ipm.limit_type
 FROM patient_insurance_plan_details pipd
 JOIN insurance_plan_main ipm ON(ipm.plan_id = pipd.plan_id)
 JOIN patient_insurance_plans pip ON(pip.patient_id = pipd.visit_id and pip.plan_id = pipd.plan_id)
 JOIN patient_registration pr ON(pip.patient_id = pr.patient_id)
 JOIN insurance_plan_details ipd ON(ipd.patient_type = pr.visit_type AND ipd.insurance_category_id = pipd.insurance_category_id)
 JOIN item_insurance_categories iic ON(iic.insurance_category_id = pipd.insurance_category_id) ;

DROP VIEW IF EXISTS op_to_ip_coverted_visits_view CASCADE;
CREATE OR REPLACE VIEW op_to_ip_coverted_visits_view AS
SELECT ipr.patient_id AS ip_visit_id, ipr.reg_date AS ip_reg_date, ipr.reg_time AS ip_reg_time, opr.patient_id AS op_visit_id, opr.reg_date AS op_reg_date, opr.reg_time AS op_reg_time
   FROM patient_registration ipr
   JOIN patient_registration opr ON ipr.original_visit_id::text = opr.patient_id::text
  WHERE ipr.visit_type = 'i'::bpchar AND ipr.original_visit_id IS NOT NULL AND opr.visit_type = 'o'::bpchar
  ORDER BY ipr.patient_id;

DROP FUNCTION IF EXISTS insurance_company_association_trigger() CASCADE;
CREATE OR REPLACE function insurance_company_association_trigger() RETURNS TRIGGER AS $BODY$
DECLARE
	categoryId integer;
	planId integer;
	advInsActive character(1);
	category RECORD;
BEGIN
	IF TG_OP = 'INSERT' THEN

		SELECT activation_status FROM modules_activated
		WHERE module_id = 'mod_adv_ins' INTO advInsActive;

		IF (advInsActive IS NULL OR advInsActive = 'N') THEN
			SELECT nextval('insurance_category_master_seq') INTO categoryId;
			SELECT nextval('insurance_plan_main_seq') INTO planId;

			-- Insert a plan type
			INSERT INTO insurance_category_master (category_id, insurance_co_id, category_name, status)
				VALUES (categoryId, NEW.insurance_co_id, substring(NEW.insurance_co_name, 1, 90) || '-Plans', 'A');

			INSERT INTO insurance_category_center_master (inscat_center_id, category_id, center_id, status)
				VALUES (nextval('insurance_category_center_master_seq'), categoryId, 0, 'A');

			-- Insert a plan
			INSERT INTO insurance_plan_main (plan_id, plan_name, category_id, overall_treatment_limit,
				insurance_co_id, ip_applicable, op_applicable, status,
				op_plan_limit, op_episode_limit, op_visit_limit, ip_plan_limit, ip_visit_limit, ip_per_day_limit,
				op_visit_deductible, ip_visit_deductible, op_copay_percent, ip_copay_percent, limits_include_followup)
			VALUES (planId, substring(NEW.insurance_co_name, 1, 90) || ' Plan', categoryId, 0.00,
				NEW.insurance_co_id, 'Y', 'Y', 'A',
				0.00, 0.00, 0.00, 0.00, 0.00, 0.00,
				0.00, 0.00, 0.00, 0.00, 'N');

			-- Insert insurance plan details for all insurance categories for the new mapping

			FOR category IN
				SELECT * FROM item_insurance_categories
			LOOP
				INSERT INTO insurance_plan_details (plan_id, insurance_category_id,
					patient_amount_cap, per_treatment_limit, patient_type)
				VALUES (planId, category.insurance_category_id,  0.00, 0.00, 'o');

				INSERT INTO insurance_plan_details (plan_id, insurance_category_id,
					patient_amount_cap, per_treatment_limit, patient_type)
				VALUES (planId, category.insurance_category_id,  0.00, 0.00, 'i');

			END LOOP;

		END IF;
	END IF;
	RETURN NEW;
END;
$BODY$ language plpgsql;

DROP TRIGGER IF EXISTS insurance_company_association_trigger ON insurance_company_master CASCADE;
CREATE TRIGGER insurance_company_association_trigger
	AFTER INSERT ON insurance_company_master
	FOR EACH ROW
	EXECUTE PROCEDURE insurance_company_association_trigger();

DROP FUNCTION IF EXISTS hl7_save_result_export_items() CASCADE;
CREATE OR REPLACE function hl7_save_result_export_items() RETURNS TRIGGER AS $BODY$
DECLARE
	rec record;
	orderType character varying;
	opCode character varying;
	modifiedOrderType character varying;
BEGIN
		FOR rec IN
			SELECT DISTINCT hli.hl7_lab_interface_id, hli.interface_name, tp.report_id, tp.prescribed_id, COALESCE(pr.center_id, isr.center_id) AS centerid,
			dei.item_type, d.conduction_format, tp.conducted
			FROM tests_prescribed tp
			JOIN diagnostics_export_interface dei ON (tp.test_id = dei.test_id AND dei.item_type != 'TEST')
			JOIN diagnostics d ON (d.test_id = dei.test_id)
			JOIN hl7_lab_interfaces hli ON (hli.hl7_lab_interface_id = dei.hl7_lab_interface_id)
			LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id)
			LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id)
			JOIN hl7_center_interfaces hci ON (hci.hl7_lab_interface_id = hli.hl7_lab_interface_id AND 
				(hci.center_id = COALESCE(pr.center_id, isr.center_id) OR (0 = hci.center_id)))
			WHERE hli.status = 'A' AND tp.report_id = COALESCE(NEW.revised_report_id, NEW.report_id) AND tp.pat_id = NEW.patient_id
		LOOP
			orderType:= 'TESTRESULT';
			opCode:= 'N';
			modifiedOrderType := 'TESTRESULTMODIFIED';
			IF (rec.conduction_format = 'T') THEN
				orderType:= 'TESTTEMPLATERESULT';
				modifiedOrderType := 'TESTTEMPLATEMODIFIED';
			END IF;
			IF (((OLD.addendum_signed_off = 'N' AND NEW.addendum_signed_off = 'Y')) 
				AND rec.item_type = 'TESTRESULT' ) THEN	
				opCode:= 'AD';
				INSERT INTO hl7_export_items (item_type, item_id, inserted_ts, interface_name, hl7_lab_interface_id, bill_paid, op_code, center_id)
				VALUES (orderType, rec.prescribed_id, current_timestamp, rec.interface_name, rec.hl7_lab_interface_id, 'N', opCode, rec.centerid);
			ELSIF (OLD.signed_off = 'Y' AND NEW.signed_off = 'N' AND rec.item_type = 'TESTMODIFIED') THEN
				INSERT INTO hl7_export_items (item_type, item_id, inserted_ts, interface_name, hl7_lab_interface_id, bill_paid, op_code, center_id)
				VALUES (modifiedOrderType, rec.prescribed_id, current_timestamp, rec.interface_name, rec.hl7_lab_interface_id, 'N', 'RS', rec.centerid);
			END IF;
		END LOOP;
	RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS hl7_save_result_export_items ON test_visit_reports CASCADE;
CREATE CONSTRAINT TRIGGER hl7_save_result_export_items
  AFTER UPDATE
  ON test_visit_reports
  DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
  EXECUTE PROCEDURE hl7_save_result_export_items();


DROP VIEW IF EXISTS hl7_result_export_view CASCADE;
CREATE OR REPLACE VIEW hl7_result_export_view AS
SELECT hei.op_code, hei.bill_paid, hei.interface_name, hei.export_status, hei.export_msg_id, 
	hei.export_id, hei.export_failure_msg, hei.exported_ts, hei.inserted_ts, 
	hei.item_type, item_id, tvr.report_id, tvr.patient_id, tvr.report_name, tvr.category, to_char(tvr.report_date, 'YYYYMMDDhh24miss') AS report_date, 
	tvr.signed_off, tvr.report_mode, tvr.user_name, tvr.pheader_template_id, tvr.report_addendum, tvr.addendum_signed_off, tvr.handed_over, 
	tvr.handed_over_to, tvr.hand_over_time, tvr.num_prints, tvr.report_state, tvr.revised_report_id, tvr.report_results_severity_status, 
	tvr.signoff_center, tvr.transferred, tvr.signedoff_by, tvr.notification_sent, COALESCE(isr.center_id, pr.center_id) AS center_id, hcm.center_name,
	COALESCE(pr.mr_no, isr.incoming_visit_id) AS mr_no, pd.oldmrno,
	COALESCE(pd.last_name, isr.patient_name) AS last_name,
	COALESCE(pd.patient_name,'') AS patient_name, sm.salutation,
	COALESCE(ci.city_name,'') AS cityname, COALESCE(substring(ci.city_id, 4, 3),'') AS city_code,
	COALESCE(st.state_name,'') AS statename, COALESCE(substring(st.state_id, 4, 3),'') AS state_code,
	COALESCE(cnm.country_name,'') AS country_name, COALESCE(cnm.country_code,'') AS country_code,
	COALESCE(pd.patient_phone, isr.phone_no) AS patient_phone,
	COALESCE(pd.patient_gender,isr.patient_gender) AS patient_gender,
	to_char(coalesce(pd.dateofbirth, pd.expected_dob, isr.isr_dateofbirth,
	(current_date-(CASE WHEN isr.age_unit = 'Y' THEN (isr.patient_age*365.25)::integer
	WHEN isr.age_unit = 'M' THEN (isr.patient_age*30.43)::integer ELSE isr.patient_age*1 END) )),'YYYYMMDD') AS expected_dob,
	COALESCE(pd.patient_address,isr.address,'') AS patient_address, pd.email_id AS patient_email_id,
	to_char(COALESCE(pr.reg_date, isr.date), 'YYYYMMDD') AS reg_date,
	pr.doctor, substring(dr.doctor_name,1,50) AS doctor_name, pr.bed_type, wn.ward_name,
	pr.reference_docto_id, pr.visit_type,
	substring(COALESCE(drs.doctor_name, rd.referal_name),1,50) AS refdoctorname,
	doctors.doctor_id AS conducting_doctor_id,doctors.doctor_name AS conducting_doctor_name,
	tm.resultlabel_id, tm.hl7_export_code, tm.resultlabel, tm.code_type, tm.result_code, td.amendment_reason,
	td.report_value, td.units, td.reference_range, td.test_detail_status, td.original_test_details_id,
	td.patient_report_file, td.format_name, td.conducted_in_reportformat, td.withinnormal,
	COALESCE(tp.his_prescribed_id, tp.prescribed_id) AS prescribed_id, tp.re_conduction, tp.reference_pres, hei.hl7_lab_interface_id 
	
FROM hl7_export_items hei
JOIN tests_prescribed tp ON (hei.item_id = tp.prescribed_id::text AND hei.item_type='TESTRESULT')
JOIN test_visit_reports tvr ON (tp.report_id = tvr.report_id)
LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id)
LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id)
LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id)
LEFT JOIN city ci ON pd.patient_city = ci.city_id
LEFT JOIN state_master st ON pd.patient_state = st.state_id
LEFT JOIN country_master cnm ON pd.country = cnm.country_id
LEFT JOIN hospital_center_master hcm ON (hcm.center_id = COALESCE(isr.center_id, pr.center_id))
LEFT JOIN ward_names wn ON (wn.ward_no = pr.ward_id)
LEFT JOIN doctors drs ON pr.reference_docto_id = drs.doctor_id
LEFT JOIN referral rd ON pr.reference_docto_id = rd.referal_no
LEFT JOIN doctors dr ON dr.doctor_id = pr.doctor
LEFT JOIN bill_activity_charge bac ON (bac.activity_id = tp.prescribed_id::text AND activity_code = 'DIA')
LEFT JOIN doctors ON (doctors.doctor_id = bac.doctor_id)
LEFT JOIN test_results_master tm ON (tm.test_id = tp.test_id)
LEFT JOIN test_results_center trc ON (tm.resultlabel_id = trc.resultlabel_id)
LEFT JOIN test_details td ON (td.test_id = tm.test_id AND td.resultlabel_id = tm.resultlabel_id
	AND td.patient_id = tp.pat_id AND td.prescribed_id = tp.prescribed_id)
WHERE hei.export_status IN ('N', 'F') AND td.test_detail_status IN ('S', 'A') AND
(COALESCE(trc.center_id, 0)= COALESCE(pr.center_id, isr.center_id, 0) OR COALESCE(trc.center_id, 0) = 0)
AND ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) )
UNION ALL

SELECT hei.op_code, hei.bill_paid, hei.interface_name, hei.export_status, hei.export_msg_id, 
	hei.export_id, hei.export_failure_msg, hei.exported_ts, hei.inserted_ts, 
	hei.item_type, item_id, tvr.report_id, tvr.patient_id, tvr.report_name, tvr.category, to_char(tvr.report_date, 'YYYYMMDDhh24miss') AS report_date, 
	tvr.signed_off, tvr.report_mode, tvr.user_name, tvr.pheader_template_id, tvr.report_addendum, tvr.addendum_signed_off, tvr.handed_over, 
	tvr.handed_over_to, tvr.hand_over_time, tvr.num_prints, tvr.report_state, tvr.revised_report_id, tvr.report_results_severity_status, 
	tvr.signoff_center, tvr.transferred, tvr.signedoff_by, tvr.notification_sent ,COALESCE(isr.center_id, pr.center_id) AS center_id, hcm.center_name,
	COALESCE(pr.mr_no, isr.incoming_visit_id) AS mr_no, pd.oldmrno,
	COALESCE(pd.last_name, isr.patient_name) AS last_name,
	COALESCE(pd.patient_name,'') AS patient_name, sm.salutation,
	COALESCE(ci.city_name,'') AS cityname, COALESCE(substring(ci.city_id, 4, 3),'') AS city_code,
	COALESCE(st.state_name,'') AS statename, COALESCE(substring(st.state_id, 4, 3),'') AS state_code,
	COALESCE(cnm.country_name,'') AS country_name, COALESCE(cnm.country_code,'') AS country_code,
	COALESCE(pd.patient_phone, isr.phone_no) AS patient_phone,
	COALESCE(pd.patient_gender,isr.patient_gender) AS patient_gender,
	to_char(coalesce(pd.dateofbirth, pd.expected_dob, isr.isr_dateofbirth,
	(current_date-(CASE WHEN isr.age_unit = 'Y' THEN (isr.patient_age*365.25)::integer
	WHEN isr.age_unit = 'M' THEN (isr.patient_age*30.43)::integer ELSE isr.patient_age*1 END) )),'YYYYMMDD') AS expected_dob,
	COALESCE(pd.patient_address,isr.address,'') AS patient_address, pd.email_id AS patient_email_id,
	to_char(COALESCE(pr.reg_date, isr.date), 'YYYYMMDD') AS reg_date,
	pr.doctor, substring(dr.doctor_name,1,50) AS doctor_name, pr.bed_type, wn.ward_name,
	pr.reference_docto_id, pr.visit_type,
	substring(COALESCE(drs.doctor_name, rd.referal_name),1,50) AS refdoctorname,
	doctors.doctor_id AS conducting_doctor_id,doctors.doctor_name AS conducting_doctor_name,
	null AS resultlabel_id, '' AS hl7_export_code, '' AS resultlabel, '' AS code_type, '' AS result_code, '' AS amendment_reason,
	'' AS report_value, '' AS units, '' AS reference_range, '' AS test_detail_status, null AS original_test_details_id,
	'' AS patient_report_file, '' AS format_name, '' AS conducted_in_reportformat, '' AS withinnormal,
	COALESCE(tp.his_prescribed_id, tp.prescribed_id) AS prescribed_id, tp.re_conduction, tp.reference_pres, hei.hl7_lab_interface_id
	
FROM hl7_export_items hei
JOIN tests_prescribed tp ON (hei.item_id = tp.prescribed_id::text AND hei.item_type='TESTRESULTMODIFIED')
JOIN test_visit_reports tvr ON (tp.report_id = tvr.report_id)
LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id)
LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id)
LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id)
LEFT JOIN city ci ON pd.patient_city = ci.city_id
LEFT JOIN state_master st ON pd.patient_state = st.state_id
LEFT JOIN country_master cnm ON pd.country = cnm.country_id
LEFT JOIN hospital_center_master hcm ON (hcm.center_id = COALESCE(isr.center_id, pr.center_id))
LEFT JOIN ward_names wn ON (wn.ward_no = pr.ward_id)
LEFT JOIN doctors drs ON pr.reference_docto_id = drs.doctor_id
LEFT JOIN referral rd ON pr.reference_docto_id = rd.referal_no
LEFT JOIN doctors dr ON dr.doctor_id = pr.doctor
LEFT JOIN bill_activity_charge bac ON (bac.activity_id = tp.prescribed_id::text AND activity_code = 'DIA')
LEFT JOIN doctors ON (doctors.doctor_id = bac.doctor_id)
WHERE hei.export_status IN ('N', 'F') AND ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) );
  
DROP VIEW IF EXISTS hl7_template_result_export_view CASCADE;
CREATE OR REPLACE VIEW hl7_template_result_export_view AS
SELECT hei.op_code, hei.bill_paid, hei.interface_name, hei.export_status, hei.export_msg_id, 
	hei.export_id, hei.export_failure_msg, hei.exported_ts, hei.inserted_ts, 
	hei.item_type, item_id, tvr.report_id, tvr.patient_id, tvr.report_name, tvr.category, to_char(tvr.report_date, 'YYYYMMDDhh24miss') AS report_date, 
	tvr.signed_off, tvr.report_mode, tvr.user_name, tvr.pheader_template_id, tvr.report_addendum, tvr.addendum_signed_off, tvr.handed_over, 
	tvr.handed_over_to, tvr.hand_over_time, tvr.num_prints, tvr.report_state, tvr.revised_report_id, tvr.report_results_severity_status, 
	tvr.signoff_center, tvr.transferred, tvr.signedoff_by, tvr.notification_sent, COALESCE(isr.center_id, pr.center_id) AS center_id, hcm.center_name,
	COALESCE(pr.mr_no, isr.incoming_visit_id) AS mr_no, pd.oldmrno,
	COALESCE(pd.last_name, isr.patient_name) AS last_name,
	COALESCE(pd.patient_name,'') AS patient_name, sm.salutation,
	COALESCE(ci.city_name,'') AS cityname, COALESCE(substring(ci.city_id, 4, 3),'') AS city_code,
	COALESCE(st.state_name,'') AS statename, COALESCE(substring(st.state_id, 4, 3),'') AS state_code,
	COALESCE(cnm.country_name,'') AS country_name, COALESCE(cnm.country_code,'') AS country_code,
	COALESCE(pd.patient_phone, isr.phone_no) AS patient_phone,
	COALESCE(pd.patient_gender,isr.patient_gender) AS patient_gender,
	to_char(coalesce(pd.dateofbirth, pd.expected_dob, isr.isr_dateofbirth,
	(current_date-(CASE WHEN isr.age_unit = 'Y' THEN (isr.patient_age*365.25)::integer
	WHEN isr.age_unit = 'M' THEN (isr.patient_age*30.43)::integer ELSE isr.patient_age*1 END) )),'YYYYMMDD') AS expected_dob,
	COALESCE(pd.patient_address,isr.address,'') AS patient_address, pd.email_id AS patient_email_id,
	to_char(COALESCE(pr.reg_date, isr.date), 'YYYYMMDD') AS reg_date,
	pr.doctor, substring(dr.doctor_name,1,50) AS doctor_name, pr.bed_type, wn.ward_name,
	pr.reference_docto_id, pr.visit_type,
	substring(COALESCE(drs.doctor_name, rd.referal_name),1,50) AS refdoctorname,
	doctors.doctor_id AS conducting_doctor_id,doctors.doctor_name AS conducting_doctor_name,
	'' AS resultlabel_id, '' AS hl7_export_code, '' AS resultlabel, '' AS code_type, '' AS result_code, td.amendment_reason,
	td.report_value, td.units, td.reference_range, td.test_detail_status, td.original_test_details_id,
	td.patient_report_file, td.format_name, td.conducted_in_reportformat, td.withinnormal,
	COALESCE(tp.his_prescribed_id, tp.prescribed_id) AS prescribed_id, tp.re_conduction, tp.reference_pres, hei.hl7_lab_interface_id
	
FROM hl7_export_items hei
JOIN tests_prescribed tp ON (hei.item_id = tp.prescribed_id::text AND hei.item_type='TESTTEMPLATERESULT')
JOIN test_visit_reports tvr ON (tp.report_id = tvr.report_id)
LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id)
LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id)
LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id)
LEFT JOIN city ci ON pd.patient_city = ci.city_id
LEFT JOIN state_master st ON pd.patient_state = st.state_id
LEFT JOIN country_master cnm ON pd.country = cnm.country_id
LEFT JOIN hospital_center_master hcm ON (hcm.center_id = COALESCE(isr.center_id, pr.center_id))
LEFT JOIN ward_names wn ON (wn.ward_no = pr.ward_id)
LEFT JOIN doctors drs ON pr.reference_docto_id = drs.doctor_id
LEFT JOIN referral rd ON pr.reference_docto_id = rd.referal_no
LEFT JOIN doctors dr ON dr.doctor_id = pr.doctor
LEFT JOIN bill_activity_charge bac ON (bac.activity_id = tp.prescribed_id::text AND activity_code = 'DIA')
LEFT JOIN doctors ON (doctors.doctor_id = bac.doctor_id)
LEFT JOIN test_details td ON (td.patient_id = tp.pat_id AND td.prescribed_id = tp.prescribed_id)
WHERE hei.export_status IN ('N', 'F') AND td.test_detail_status IN ('S', 'A') AND ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no))

UNION ALL

SELECT hei.op_code, hei.bill_paid, hei.interface_name, hei.export_status, hei.export_msg_id, 
	hei.export_id, hei.export_failure_msg, hei.exported_ts, hei.inserted_ts, 
	hei.item_type, item_id, tvr.report_id, tvr.patient_id, tvr.report_name, tvr.category, to_char(tvr.report_date, 'YYYYMMDDhh24miss') AS report_date, 
	tvr.signed_off, tvr.report_mode, tvr.user_name, tvr.pheader_template_id, tvr.report_addendum, tvr.addendum_signed_off, tvr.handed_over, 
	tvr.handed_over_to, tvr.hand_over_time, tvr.num_prints, tvr.report_state, tvr.revised_report_id, tvr.report_results_severity_status, 
	tvr.signoff_center, tvr.transferred, tvr.signedoff_by, tvr.notification_sent, COALESCE(isr.center_id, pr.center_id) AS center_id, hcm.center_name,
	COALESCE(pr.mr_no, isr.incoming_visit_id) AS mr_no, pd.oldmrno,
	COALESCE(pd.last_name, isr.patient_name) AS last_name,
	COALESCE(pd.patient_name,'') AS patient_name, sm.salutation,
	COALESCE(ci.city_name,'') AS cityname, COALESCE(substring(ci.city_id, 4, 3),'') AS city_code,
	COALESCE(st.state_name,'') AS statename, COALESCE(substring(st.state_id, 4, 3),'') AS state_code,
	COALESCE(cnm.country_name,'') AS country_name, COALESCE(cnm.country_code,'') AS country_code,
	COALESCE(pd.patient_phone, isr.phone_no) AS patient_phone,
	COALESCE(pd.patient_gender,isr.patient_gender) AS patient_gender,
	to_char(coalesce(pd.dateofbirth, pd.expected_dob, isr.isr_dateofbirth,
	(current_date-(CASE WHEN isr.age_unit = 'Y' THEN (isr.patient_age*365.25)::integer
	WHEN isr.age_unit = 'M' THEN (isr.patient_age*30.43)::integer ELSE isr.patient_age*1 END) )),'YYYYMMDD') AS expected_dob,
	COALESCE(pd.patient_address,isr.address,'') AS patient_address, pd.email_id AS patient_email_id,
	to_char(COALESCE(pr.reg_date, isr.date), 'YYYYMMDD') AS reg_date,
	pr.doctor, substring(dr.doctor_name,1,50) AS doctor_name, pr.bed_type, wn.ward_name,
	pr.reference_docto_id, pr.visit_type,
	substring(COALESCE(drs.doctor_name, rd.referal_name),1,50) AS refdoctorname,
	doctors.doctor_id AS conducting_doctor_id,doctors.doctor_name AS conducting_doctor_name,
	'' AS resultlabel_id, '' AS hl7_export_code, '' AS resultlabel, '' AS code_type, '' AS result_code, '' AS amendment_reason,
	'' AS report_value, '' AS units, '' AS reference_range, '' AS test_detail_status, null AS original_test_details_id, '' AS patient_report_file, 
	'' AS format_name, '' AS conducted_in_reportformat, '' AS withinnormal,
	COALESCE(tp.his_prescribed_id, tp.prescribed_id) AS prescribed_id, tp.re_conduction, tp.reference_pres, hei.hl7_lab_interface_id 
	
FROM hl7_export_items hei
JOIN tests_prescribed tp ON (hei.item_id = tp.prescribed_id::text AND hei.item_type='TESTTEMPLATEMODIFIED')
JOIN test_visit_reports tvr ON (tp.report_id = tvr.report_id)
LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id)
LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id)
LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id)
LEFT JOIN city ci ON pd.patient_city = ci.city_id
LEFT JOIN state_master st ON pd.patient_state = st.state_id
LEFT JOIN country_master cnm ON pd.country = cnm.country_id
LEFT JOIN hospital_center_master hcm ON (hcm.center_id = COALESCE(isr.center_id, pr.center_id))
LEFT JOIN ward_names wn ON (wn.ward_no = pr.ward_id)
LEFT JOIN doctors drs ON pr.reference_docto_id = drs.doctor_id
LEFT JOIN referral rd ON pr.reference_docto_id = rd.referal_no
LEFT JOIN doctors dr ON dr.doctor_id = pr.doctor
LEFT JOIN bill_activity_charge bac ON (bac.activity_id = tp.prescribed_id::text AND activity_code = 'DIA')
LEFT JOIN doctors ON (doctors.doctor_id = bac.doctor_id)
WHERE hei.export_status IN ('N', 'F') AND ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) );

DROP VIEW IF EXISTS hl7_report_export_view CASCADE;
CREATE OR REPLACE VIEW hl7_report_export_view AS
SELECT hei.interface_name, hei.bill_paid, hei.op_code, TEXTCAT_COMMACAT(hei.item_id) AS item_id_1,
	TEXTCAT_COMMACAT(hei.export_id::text) AS export_id, tvr.patient_id,
	tvr.report_id AS item_id, to_char(tvr.report_date, 'YYYYMMDDhh24miss') AS report_date, tvr.report_name, COALESCE(isr.center_id, pr.center_id) AS center_id, hcm.center_name,
	COALESCE(pr.mr_no, isr.incoming_visit_id) AS mr_no, pd.oldmrno, tvr.report_id,
	COALESCE(pd.last_name, isr.patient_name) AS last_name,
	COALESCE(pd.patient_name,'') AS patient_name, sm.salutation,
	COALESCE(ci.city_name,'') AS cityname, COALESCE(substring(ci.city_id, 4, 3),'') AS city_code,
	COALESCE(st.state_name,'') AS statename, COALESCE(substring(st.state_id, 4, 3),'') AS state_code,
	COALESCE(cnm.country_name,'') AS country_name, COALESCE(cnm.country_code,'') AS country_code,
	COALESCE(pd.patient_phone, isr.phone_no) AS patient_phone,
	COALESCE(pd.patient_gender,isr.patient_gender) AS patient_gender,
	to_char(coalesce(pd.dateofbirth, pd.expected_dob, isr.isr_dateofbirth,
	(current_date-(CASE WHEN isr.age_unit = 'Y' THEN (isr.patient_age*365.25)::integer
	WHEN isr.age_unit = 'M' THEN (isr.patient_age*30.43)::integer ELSE isr.patient_age*1 END) )),'YYYYMMDD') AS expected_dob,
	COALESCE(pd.patient_address,isr.address,'') AS patient_address, pd.email_id AS patient_email_id,
	to_char(COALESCE(pr.reg_date, isr.date), 'YYYYMMDD') AS reg_date,
	pr.doctor, substring(dr.doctor_name,1,50) AS doctor_name, pr.bed_type, wn.ward_name,
	pr.reference_docto_id, pr.visit_type,
	substring(COALESCE(drs.doctor_name, rd.referal_name),1,50) AS refdoctorname,
	TEXTCAT_COMMACAT(doctors.doctor_id) AS conducting_doctor_id,TEXTCAT_COMMACAT(doctors.doctor_name) AS conducting_doctor_name,
	TEXTCAT_COMMACAT(COALESCE(tp.his_prescribed_id::text, tp.prescribed_id::text)) AS prescribed_id,
	TEXTCAT_COMMACAT(d.test_name) AS test_names, hei.hl7_lab_interface_id
	
FROM hl7_export_items hei
JOIN tests_prescribed tp ON (hei.item_id = tp.prescribed_id::text AND hei.item_type IN ('TESTRESULT', 'TESTTEMPLATERESULT'))
JOIN test_visit_reports tvr ON (tp.report_id = tvr.report_id AND tvr.patient_id = tp.pat_id AND tvr.signed_off = 'Y')
JOIN diagnostics d ON (d.test_id = tp.test_id)
LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id)
LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id)
LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id)
LEFT JOIN city ci ON pd.patient_city = ci.city_id
LEFT JOIN state_master st ON pd.patient_state = st.state_id
LEFT JOIN country_master cnm ON pd.country = cnm.country_id
LEFT JOIN hospital_center_master hcm ON (hcm.center_id = COALESCE(isr.center_id, pr.center_id))
LEFT JOIN ward_names wn ON (wn.ward_no = pr.ward_id)
LEFT JOIN doctors drs ON pr.reference_docto_id = drs.doctor_id
LEFT JOIN referral rd ON pr.reference_docto_id = rd.referal_no
LEFT JOIN doctors dr ON dr.doctor_id = pr.doctor
LEFT JOIN bill_activity_charge bac ON (bac.activity_id = tp.prescribed_id::text AND activity_code = 'DIA')
LEFT JOIN doctors ON (doctors.doctor_id = bac.doctor_id)
WHERE hei.export_status IN ('N', 'F') AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )
GROUP BY hei.interface_name, hei.bill_paid, tvr.report_id, tvr.report_name, tvr.patient_id,
isr.center_id, pr.center_id, hcm.center_name, hei.op_code, tvr.report_date,
pr.mr_no, isr.incoming_visit_id, pd.oldmrno,
pd.last_name, isr.patient_name, pd.patient_name, sm.salutation,
ci.city_name, ci.city_id, st.state_name, st.state_id, cnm.country_name, cnm.country_code,
pd.patient_phone, isr.phone_no, pd.patient_gender,isr.patient_gender, pd.expected_dob,
pd.dateofbirth, isr.patient_age, pd.patient_address,isr.address, pd.email_id ,
pr.reg_date, isr.date, pr.doctor, dr.doctor_name, pr.bed_type, wn.ward_name,
pr.reference_docto_id, pr.visit_type, drs.doctor_name, rd.referal_name, hei.hl7_lab_interface_id;

	
-- View: test_tat_view

-- DROP VIEW test_tat_view;
DROP VIEW IF EXISTS test_tat_view CASCADE;
CREATE OR REPLACE VIEW test_tat_view AS
 SELECT hcm.center_name,
    hcm.center_id AS source_center_id,
    hcm.city_id,
    c.city_name,
    hcm.state_id,
    sm.state_name,
    om.outsource_name,
    dtcm.tat_center_id,
    dtcm.test_id,
    dtcm.conduction_tat_hours,
    dtcm.center_id,
    dtcm.logistics_tat_hours,
    dtcm.conduction_start_time,
    dtcm.processing_days
   FROM hospital_center_master hcm
     LEFT JOIN diag_tat_center_master dtcm ON dtcm.center_id = hcm.center_id
     LEFT JOIN city c ON c.city_id::text = hcm.city_id::text
     LEFT JOIN state_master sm ON sm.state_id::text = hcm.state_id::text
     LEFT JOIN diag_outsource_detail dod ON dod.source_center_id = dtcm.center_id AND dod.test_id::text = dtcm.test_id::text
     LEFT JOIN diag_outsource_master dom ON dod.outsource_dest_id = dom.outsource_dest_id
     LEFT JOIN outsource_names om ON dom.outsource_dest_id = om.outsource_dest_id
  WHERE hcm.status = 'A'::bpchar AND hcm.center_id <> 0
  ORDER BY hcm.center_name;

  -- Function: verify_cyclic_path()

-- DROP FUNCTION verify_cyclic_path();

CREATE OR REPLACE FUNCTION verify_cyclic_path()
  RETURNS trigger AS
$BODY$
DECLARE
var_source varchar(100);
var_outsource varchar(100);
BEGIN
var_source :=(select center_name from hospital_center_master where center_id=NEW.source_center_id);
var_outsource:=(select outsource_name from outsource_names where outsource_dest_id =NEW.outsource_dest_id);
if exists (with recursive outsource_chain(source_center_id,test_id,outsource_dest)
as
(select source_center_id,test_id,outsource_dest from
diag_outsource_detail dod
join diag_outsource_master dom ON (dom.outsource_dest_id =
dod.outsource_dest_id)
where dod.test_id = NEW.test_id and dod.outsource_dest_id =
(select outsource_dest_id from diag_outsource_master where outsource_dest=NEW.source_center_id::text)
union all
select foo.source_center_id,foo.test_id,foo.outsource_dest from (select
source_center_id,test_id,outsource_dest from diag_outsource_detail dod
join diag_outsource_master dom ON (dom.outsource_dest_id =
dod.outsource_dest_id) and dom.outsource_dest_type = 'C') as
foo,outsource_chain oc where (oc.source_center_id =
cast(foo.outsource_dest AS int)) and (oc.test_id = foo.test_id)
)
select source_center_id from outsource_chain where source_center_id:: text=(select outsource_dest from diag_outsource_master where outsource_dest_id=NEW.outsource_dest_id))
THEN
raise EXCEPTION 'This can lead to a cyclic path..at  (%) -> (%)',var_source,var_outsource;
END IF;

RETURN NEW;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION verify_cyclic_path()
  OWNER TO postgres;

  DROP TRIGGER IF EXISTS verify_cyclic_path_trigger ON diag_outsource_detail CASCADE;
  CREATE TRIGGER verify_cyclic_path_trigger
  BEFORE INSERT OR UPDATE
  ON diag_outsource_detail
  FOR EACH ROW
  EXECUTE PROCEDURE verify_cyclic_path();


-- Create view for consolidated_bill_receipts_view ;

DROP VIEW IF EXISTS consolidated_bill_receipts_view CASCADE;

CREATE OR REPLACE VIEW consolidated_bill_receipts_view AS

SELECT cpb.consolidated_bill_no, b.bill_no,pip.sponsor_id, coalesce(sum(r.amount),0) AS sponsor_received_amt, r.is_settlement
FROM consolidated_patient_bill  cpb
JOIN bill b ON (b.bill_no = cpb.bill_no)
JOIN patient_registration pr ON (b.visit_id = pr.patient_id)
LEFT JOIN patient_insurance_plans pip ON (pr.patient_id = pip.patient_id)
LEFT JOIN bill_receipts br ON (br.bill_no = b.bill_no)
JOIN receipts r ON (r.receipt_id = br.receipt_no and r.tpa_id = pip.sponsor_id) where b.status NOT IN('X','C')
GROUP BY cpb.consolidated_bill_no,b.bill_no,pip.sponsor_id, r.is_settlement;


-- function and trigger to make the editability of messages AS All WHEN the mod_practo_sms is off and editability AS Instaadmin  WHEN the mod_practo_sms is ON --
create or replace function message_editability()
RETURNS trigger AS $BODY$
BEGIN
IF(NEW.activation_status = 'Y') THEN
    update message_types set editability='I' where message_mode ilike 'SMS';
    return NEW;
ELSE
    update message_types set editability='A' where message_mode ilike 'SMS';
    return NEW;
END IF;
return NEW;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS practo_mode_message_editability ON modules_activated CASCADE;
CREATE TRIGGER  practo_mode_message_editability
    BEFORE INSERT OR UPDATE ON modules_activated
    FOR EACH ROW
    WHEN (NEW.module_id='mod_practo_sms')
    EXECUTE PROCEDURE message_editability();

-- function and trigger to make the editability of salucro AS All WHEN the mod_salucro is on/off and editability AS Instaadmin  WHEN the mod_salucro is ON --
create or replace function salucro_editability()
RETURNS trigger AS $BODY$
BEGIN
IF(NEW.activation_status = 'Y') THEN
    update payment_mode_master set status='A' where mode_id = -10;
    return NEW;
ELSE
    update payment_mode_master set status='I' where mode_id = -10;
    return NEW;
END IF;
return NEW;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS practo_mode_salucro_editability ON modules_activated CASCADE;
CREATE TRIGGER  practo_mode_salucro_editability
    BEFORE INSERT OR UPDATE ON modules_activated
    FOR EACH ROW
    WHEN (NEW.module_id='mod_salucro')
    EXECUTE PROCEDURE salucro_editability();

-- function and trigger to make the mode_id=-10 as inactive AS All WHEN the mod_salucro is deleted from the table --
create or replace function salucro_editability_delete()
RETURNS trigger AS $BODY$
BEGIN
    update payment_mode_master set status='I' where mode_id = -10;
    return OLD;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS practo_mode_salucro_delete ON modules_activated CASCADE;
CREATE TRIGGER  practo_mode_salucro_delete
    BEFORE DELETE ON modules_activated
    FOR EACH ROW
    WHEN (OLD.module_id='mod_salucro')
    EXECUTE PROCEDURE salucro_editability_delete();

-- function and trigger to make the editability of messages AS All WHEN the mod_practo_sms is deleted from the table --
create or replace function message_editability_delete()
RETURNS trigger AS $BODY$
BEGIN
    update message_types set editability='A' where message_mode ilike 'SMS';
    return NEW;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS practo_mode_message_delete ON modules_activated CASCADE;
CREATE TRIGGER  practo_mode_message_delete
    BEFORE DELETE ON modules_activated
    FOR EACH ROW
    WHEN (OLD.module_id='mod_practo_sms')
    EXECUTE PROCEDURE message_editability_delete();	
-- function and trigger to make the editability of practo share diag email messages AS None and status AS Active WHEN the mod_phr is off and editability AS None  and status AS Inactive WHEN the mod_phr is ON --
DROP FUNCTION IF EXISTS message_phr_diag_editability() CASCADE;

DROP TRIGGER IF EXISTS practo_mode_phr_diag_message_editability ON modules_activated CASCADE;

-- function and trigger to make the editability of practo share diag email messages AS None and status AS Inactive WHEN the mod_phr is deleted from the table --
DROP FUNCTION IF EXISTS message_phr_diag_editability_delete() CASCADE;

DROP TRIGGER IF EXISTS practo_mode_phr_diag_message_delete ON modules_activated CASCADE;
    
DROP VIEW IF EXISTS diag_report_sharing_on_bill_payment CASCADE;    
    
CREATE VIEW diag_report_sharing_on_bill_payment AS
select * from (select distinct b.bill_no, tvr.report_id, b.visit_id, b.bill_type, b.visit_type, (select b.total_amount-b.primary_total_claim-b.secondary_total_claim-b.total_receipts-b.deposit_set_off-b.points_redeemed_amt) AS patient_due_amnt,
(SELECT CASE WHEN creditNote.bill_no IS NOT NULL AND creditNote.bill_no != '' THEN b.total_amount-b.primary_total_claim-b.secondary_total_claim-b.total_receipts-b.deposit_set_off-b.points_redeemed_amt + creditNote.total_credit_amount - creditNote.total_credit_claim ELSE b.total_amount-b.primary_total_claim-b.secondary_total_claim-b.total_receipts-b.deposit_set_off-b.points_redeemed_amt END )AS net_patient_due 
from test_visit_reports tvr
JOIN tests_prescribed tp on(tp.report_id = tvr.report_id and tp.coll_prescribed_id is null and tp.mr_no is not null)
JOIN bill_activity_charge bac on(bac.activity_id = tp.prescribed_id::varchar and activity_code='DIA')
JOIN bill_charge bc on(bc.charge_id=bac.charge_id) 
JOIN bill b on(b.bill_no = bc.bill_no)
LEFT JOIN LATERAL ( 
SELECT bcn.bill_no , sum(cn.total_amount) AS total_credit_amount, sum(cn.total_claim) AS total_credit_claim 
FROM bill_credit_notes bcn 
LEFT JOIN bill cn ON (bcn.credit_note_bill_no = cn.bill_no ) 
WHERE bcn.bill_no = b.bill_no
GROUP BY bcn.bill_no ) AS creditNote ON true
) as foo where foo.visit_type='o' and foo.patient_due_amnt > 0 and foo.net_patient_due > 0;



DROP FUNCTION IF EXISTS vital_reading_delete_trigger_func() CASCADE;
CREATE OR REPLACE FUNCTION vital_reading_delete_trigger_func() RETURNS TRIGGER AS $BODY$
DECLARE

BEGIN

	insert into vital_reading_audit_log(vital_reading_id, param_id, user_name, operation, field_name, old_value, new_value)
	values(OLD.vital_reading_id, OLD.param_id, OLD.username, 'DELETE', 'param_value', OLD.param_value, 'Record Deleted');

    RETURN OLD;
END;
$BODY$ LANGUAGE plpgsql;

-- function and trigger to change template of messages WHEN practo sms mode is turned on
create or replace function message_templates()
RETURNS trigger AS $BODY$
BEGIN
IF(NEW.activation_status = 'Y') THEN
		update message_types set message_body ='Dear ${patient_name}, Your appointment at ${center_name} scheduled for ${appointment_date} at ${appointment_time} has been cancelled. Please call hospital for any query.' where message_type_id= 'sms_appointment_cancellation' ;

		update message_types set message_body ='Dear ${receipient_name}, Your appointment at ${center_name} scheduled for ${appointment_date} at ${appointment_time} has been confirmed. Please call hospital for any query.' where message_type_id= 'sms_appointment_confirmation' ;

		update message_types set message_body ='Dear ${recipient_name}, the details of your appointment at ${center_name} has been changed to ${appointment_date} at ${appointment_time}. Please call hospital for any query.' where message_type_id= 'sms_appointment_details_change' ;

		update message_types set message_body ='Dear ${receipient_name}, We are sorry to inform you that the doctor is unavailable for your appointment at ${center_name} ON ${appointment_date} at ${appointment_time}. Please call hospital for any query.' where message_type_id= 'sms_appointment_reschedule' ;

		update message_types set message_body ='Dear ${receipient_name}, you have ${total_appointments} appointments at ${center_name} ON ${appointment_date} and your schedule is AS follows ${_appointment_details}. Please call hospital for any query.' where message_type_id= 'sms_doctor_appointments' ;

		update message_types set message_body ='Dear ${receipient_name}, your visit is due with Dr. ${followup_doctor} at ${center_name} ON ${followup_date}. Please call hospital for any query.' where message_type_id= 'sms_followup_reminder' ;

		update message_types set message_body ='Dear doctor, ${patient_name} has been admitted at ${center_name}.Please call hospital for any query.' where message_type_id= 'sms_patient_admitted' ;

		update message_types set message_body ='Dear ${receipient_name}, this is to remind you that you have an appointment at ${center_name} ON ${appointment_date} at ${appointment_time}. Please call hospital for any query.' where message_type_id= 'sms_next_day_appointment_reminder' ;

		update message_types set message_body ='Dear ${receipient_name}, this is to remind you that you have upcoming appointments at ${center_name}. Please call hospital for any query.' where message_type_id= 'sms_appointment_reminder' ;

		update message_types set message_body ='Dear ${receipient_name}, your ${diag_report_name} is ready. Please collect it from ${center_name}. Please call hospital for any query.' where message_type_id= 'sms_report_ready' ;

		update message_types set message_body ='Dear ${receipient_name}, your vaccination, ${vaccine_name}, is due ON ${vaccine_due_date}. Please call hospital to book an appointment.' where message_type_id= 'sms_vaccine_reminder' ;

		update message_types set message_body ='Dear ${patient_name} , your patient details have been successfully updated. Please call hospital for any query.' where message_type_id= 'sms_edit_patient_access' ;
END IF;
return NEW;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS practo_mode_message_templates ON modules_activated CASCADE;
CREATE TRIGGER  practo_mode_message_templates
    BEFORE INSERT OR UPDATE ON modules_activated
    FOR EACH ROW
    WHEN (NEW.module_id='mod_practo_sms')
    EXECUTE PROCEDURE message_templates();

DROP FUNCTION IF EXISTS update_reg_custom_fields() CASCADE;
CREATE OR REPLACE FUNCTION update_reg_custom_fields() RETURNS TRIGGER AS $BODY$
DECLARE
	custom_field text;
	label_new text;
	label_old text;
	show_new text;
	show_old text;
	validate_new text;
	validate_old text;
BEGIN
	
	-- Patient Custom List
	FOR i in 1 .. 9
	LOOP
		custom_field := 'custom_list' || cast(i AS text);
		EXECUTE format('SELECT $1.%I , $2.%I, $1.%I , $2.%I, $1.%I ,$2.%I', custom_field || '_name',custom_field || '_name',custom_field || '_show',custom_field || '_show',custom_field || '_validate',custom_field || '_validate')
	    INTO label_new,label_old,show_new,show_old,validate_new,validate_old
	    USING NEW,OLD;

	    IF label_new != label_old or show_new != show_old or validate_new != validate_old
	    THEN
	    	EXECUTE format('UPDATE reg_custom_fields set label = %L, show_group = %L, mandatory = %L where name = %L ',label_new,show_new,validate_new,custom_field || '_value');
	    END IF;
	END LOOP;

	-- Patient Custom Field
	FOR i in 1 .. 19
	LOOP
		custom_field := 'custom_field' || cast(i AS text);
		EXECUTE format('SELECT $1.%I , $2.%I, $1.%I , $2.%I, $1.%I ,$2.%I', custom_field || '_label',custom_field || '_label',custom_field || '_show',custom_field || '_show',custom_field || '_validate',custom_field || '_validate')
	    INTO label_new,label_old,show_new,show_old,validate_new,validate_old
	    USING NEW,OLD;

	    IF label_new != label_old or show_new != show_old or validate_new != validate_old
	    THEN
	    	EXECUTE format('UPDATE reg_custom_fields set label = %L, show_group = %L, mandatory = %L where name = %L ',label_new,show_new,validate_new,custom_field) USING NEW;
	    END IF;
	END LOOP;

	-- Visit Custom List
	FOR i in 1 .. 2
	LOOP
		custom_field := 'visit_custom_list' || cast(i AS text);
		EXECUTE format('SELECT $1.%I , $2.%I, $1.%I , $2.%I, $1.%I ,$2.%I', custom_field || '_name',custom_field || '_name',custom_field || '_show',custom_field || '_show',custom_field || '_validate',custom_field || '_validate')
	    INTO label_new,label_old,show_new,show_old,validate_new,validate_old
	    USING NEW,OLD;

	    IF label_new != label_old or show_new != show_old or validate_new != validate_old
	    THEN
	    	EXECUTE format('UPDATE reg_custom_fields set label = %L, show_group = %L, mandatory = %L where name = %L ',label_new,show_new,validate_new,custom_field );
	    END IF;
	END LOOP;

	-- Visit Custom Field
	FOR i in 1 .. 9
	LOOP
		custom_field := 'visit_custom_field' || cast(i AS text);
		EXECUTE format('SELECT $1.%I , $2.%I, $1.%I , $2.%I, $1.%I ,$2.%I', custom_field || '_name',custom_field || '_name',custom_field || '_show',custom_field || '_show',custom_field || '_validate',custom_field || '_validate')
	    INTO label_new,label_old,show_new,show_old,validate_new,validate_old
	    USING NEW,OLD;

	    IF label_new != label_old or show_new != show_old or validate_new != validate_old
	    THEN
	    	EXECUTE format('UPDATE reg_custom_fields set label = %L, show_group = %L, mandatory = %L where name = %L ',label_new,show_new,validate_new,custom_field) USING NEW;
	    END IF;
	END LOOP;

	RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS update_reg_custom_fields_trigger ON registration_preferences CASCADE;
CREATE TRIGGER update_reg_custom_fields_trigger
AFTER UPDATE ON registration_preferences
FOR EACH ROW EXECUTE PROCEDURE update_reg_custom_fields();

DROP VIEW IF EXISTS custom_list_values_view CASCADE;
CREATE OR REPLACE VIEW custom_list_values_view AS
SELECT 'custom_list1_value' AS field_name, * from custom_list1_master
UNION ALL 
SELECT 'custom_list2_value' AS field_name, * from custom_list2_master
UNION ALL 
SELECT 'custom_list3_value' AS field_name, * from custom_list3_master
UNION ALL 
SELECT 'custom_list4_value' AS field_name, * from custom_list4_master
UNION ALL 
SELECT 'custom_list5_value' AS field_name, * from custom_list5_master
UNION ALL 
SELECT 'custom_list6_value' AS field_name, * from custom_list6_master
UNION ALL 
SELECT 'custom_list7_value' AS field_name, * from custom_list7_master
UNION ALL 
SELECT 'custom_list8_value' AS field_name, * from custom_list8_master
UNION ALL 
SELECT 'custom_list9_value' AS field_name, * from custom_list9_master
UNION ALL 
SELECT 'visit_custom_list1' AS field_name, * from custom_visit_list1_master
UNION ALL 
SELECT 'visit_custom_list2' AS field_name, * from custom_visit_list2_master;

-- Function and trigger to update highest precedence of discharge status
    
CREATE OR REPLACE FUNCTION set_discharge_status() RETURNS TRIGGER AS $BODY$
	DECLARE
		details record;
		dischargeStatus character varying;
	BEGIN
		
		dischargeStatus := 'N';
		SELECT initiate_discharge_status, clinical_discharge_flag, financial_discharge_status, pr.discharge_flag, pr.patient_discharge_status
			FROM patient_discharge pdis
			JOIN patient_registration pr ON (pr.patient_id = pdis.patient_id)
			WHERE pr.patient_id = NEW.patient_id INTO details;

			IF (details.discharge_flag = 'D') THEN
				dischargeStatus = 'D';
			ELSEIF (details.financial_discharge_status) THEN
				dischargeStatus = 'F';
			ELSEIF (details.clinical_discharge_flag) THEN
				dischargeStatus = 'C';
			ELSEIF (details.initiate_discharge_status) THEN
				dischargeStatus = 'I';
			END IF;

			IF (dischargeStatus != details.patient_discharge_status) THEN
				UPDATE patient_registration SET patient_discharge_status = dischargeStatus WHERE patient_id = NEW.patient_id;
			END IF;
				
	RETURN NEW;
	END;
$BODY$ LANGUAGE plpgsql;
    
DROP TRIGGER IF EXISTS update_discharge_status_trigger ON patient_discharge CASCADE;
CREATE TRIGGER update_discharge_status_trigger
AFTER INSERT OR UPDATE ON patient_discharge
FOR EACH ROW EXECUTE PROCEDURE set_discharge_status();

DROP VIEW IF EXISTS display_fields_options_view CASCADE;
CREATE OR REPLACE VIEW display_fields_options_view AS
    (SELECT psval.log_id,psval.section_detail_id, psval.field_id, psval.field_name, null AS option_id,
        null AS option_value,
        sdf.field_name AS section_field_name,
		psval.user_name, psval.operation, psval.mod_time, sdf.section_id,
		old_value, new_value
    FROM patient_section_fields_audit_log psval
    	JOIN section_field_desc sdf ON (sdf.field_id = psval.field_id)
    WHERE field_type in ('text', 'wide text', 'date', 'datetime', 'date_time')

    UNION ALL
    
    SELECT optval.log_id, psf.section_detail_id, psf.field_id, optval.field_name, optval.option_id,
        CASE WHEN optval.option_id = 0 THEN 'Normal' WHEN optval.option_id = -1 THEN 'Others' ELSE sfo.option_value END AS option_value,
        sdf.field_name AS section_field_name,
		optval.user_name, optval.operation, optval.mod_time, sdf.section_id,
		case WHEN old_value = 'Y' THEN 'Yes' WHEN old_value = 'N' THEN 'No' ELSE old_value end,
		case WHEN new_value = 'Y' THEN 'Yes' WHEN new_value = 'N' THEN 'No' ELSE new_value end
    FROM patient_section_options_audit_log optval
    	JOIN patient_section_fields psf using (field_detail_id)
    	JOIN section_field_desc sdf ON (sdf.field_id = psf.field_id)
    	LEFT JOIN section_field_options sfo ON (sfo.option_id=optval.option_id)	
    WHERE field_type in ('dropdown', 'checkbox')
    );

DROP VIEW IF EXISTS cons_audit_view CASCADE;
CREATE OR REPLACE VIEW cons_audit_view AS
    SELECT NULL::Integer AS section_detail_id, psdal.log_id, 'patient_section_details_audit_log'::Text AS base_table,
           psdal.mr_no, psdal.patient_id, psdal.section_item_id, NULL::Text AS section_title, psdal.section_id, psdal.user_name,
           psdal.mod_time, psdal.operation, 'psd_'::Text || psdal.field_name::Text AS field_name, psdal.old_value, psdal.new_value, 'integer' AS "section_detail_id@type"
        FROM patient_section_details_audit_log psdal
        JOIN patient_section_details psd ON (psdal.section_detail_id=psd.section_detail_id)
        JOIN patient_section_forms psf ON (psd.section_detail_id=psf.section_detail_id)
    WHERE psf.form_type IN ('Form_CONS','Form_OP_FOLLOW_UP_CONS') AND psd.item_type='CONS' AND field_name!='section_detail_id' AND psdal.section_id > 0
	
    UNION ALL
        SELECT psdal.section_detail_id, psdal.log_id, 'display_fields_options_view'::Text AS base_table,
               psdal.mr_no, psdal.patient_id, psdal.section_item_id, sm.section_title, psdal.section_id,
               psdal.user_name,
               psdal.mod_time, psdal.operation, 'psv_'::Text || psdal.field_name::Text AS field_name,
               psdal.old_value, psdal.new_value, 'integer' AS "section_detail_id@type"
        FROM patient_section_details_audit_log psdal
        JOIN patient_section_forms psf ON (psdal.section_detail_id=psf.section_detail_id)
        JOIN section_master sm ON sm.section_id = psdal.section_id
        WHERE psf.form_type IN ('Form_CONS','Form_OP_FOLLOW_UP_CONS') AND psdal.operation::Text = 'INSERT'::Text
        AND psdal.field_name::Text = 'section_detail_id'::Text
       ;
	

DROP VIEW IF EXISTS triage_audit_view CASCADE;
CREATE OR REPLACE VIEW triage_audit_view AS
    SELECT NULL::Integer AS section_detail_id, psdal.log_id, 'patient_section_details_audit_log'::Text AS base_table,
           psdal.mr_no, psdal.patient_id, psdal.section_item_id, NULL::Text AS section_title, psdal.section_id, psdal.user_name,
           psdal.mod_time, psdal.operation, 'psd_'::Text || psdal.field_name::Text AS field_name, psdal.old_value, psdal.new_value, 'integer' AS "section_detail_id@type"
        FROM patient_section_details_audit_log psdal
        JOIN patient_section_details psd ON (psdal.section_detail_id=psd.section_detail_id)
        JOIN patient_section_forms psf ON (psd.section_detail_id=psf.section_detail_id)
    WHERE psf.form_type='Form_TRI' AND field_name!='section_detail_id' AND psdal.section_id > 0

    UNION ALL
        SELECT psdal.section_detail_id, psdal.log_id, 'display_fields_options_view'::Text AS base_table,
               psdal.mr_no, psdal.patient_id, psdal.section_item_id, sm.section_title, psdal.section_id, psdal.user_name,
               psdal.mod_time, psdal.operation, 'psv_'::Text || psdal.field_name::Text AS field_name,
               psdal.old_value, psdal.new_value, 'integer' AS "section_detail_id@type"
        FROM patient_section_details_audit_log psdal
        JOIN patient_section_forms psf ON (psdal.section_detail_id=psf.section_detail_id)
        JOIN section_master sm ON sm.section_id = psdal.section_id
        WHERE psf.form_type='Form_TRI' AND psdal.operation::Text = 'INSERT'::Text
        AND psdal.field_name::Text = 'section_detail_id'::Text
       ; 



DROP VIEW IF EXISTS ia_audit_view CASCADE;
CREATE OR REPLACE VIEW ia_audit_view AS
    SELECT NULL::Integer AS section_detail_id, psdal.log_id, 'patient_section_details_audit_log'::Text AS base_table,
           psdal.mr_no, psdal.patient_id, psdal.section_item_id, NULL::Text AS section_title, psdal.section_id, psdal.user_name,
           psdal.mod_time, psdal.operation, 'psd_'::Text || psdal.field_name::Text AS field_name, psdal.old_value, psdal.new_value, 'integer' AS "section_detail_id@type"
        FROM patient_section_details_audit_log psdal
        JOIN patient_section_details psd ON (psdal.section_detail_id=psd.section_detail_id)
        JOIN patient_section_forms psf ON (psd.section_detail_id=psf.section_detail_id)
    WHERE psf.form_type='Form_IA' AND field_name!='section_detail_id' AND psdal.section_id > 0

    UNION ALL
        SELECT psdal.section_detail_id, psdal.log_id, 'display_fields_options_view'::Text AS base_table,
               psdal.mr_no, psdal.patient_id, psdal.section_item_id, sm.section_title, psdal.section_id, psdal.user_name,
               psdal.mod_time, psdal.operation, 'psv_'::Text || psdal.field_name::Text AS field_name,
               psdal.old_value, psdal.new_value, 'integer' AS "section_detail_id@type"
        FROM patient_section_details_audit_log psdal
        JOIN patient_section_forms psf ON (psdal.section_detail_id=psf.section_detail_id)
        JOIN section_master sm ON sm.section_id = psdal.section_id
        WHERE psf.form_type='Form_IA' AND psdal.operation::Text = 'INSERT'::Text
        AND psdal.field_name::Text = 'section_detail_id'::Text
       ; 


DROP VIEW IF EXISTS ip_audit_view CASCADE;
CREATE OR REPLACE VIEW ip_audit_view AS
    SELECT NULL::Integer AS section_detail_id, psdal.log_id, 'patient_section_details_audit_log'::Text AS base_table,
           psdal.mr_no, psdal.patient_id, psdal.section_item_id, NULL::Text AS section_title, psdal.section_id, psdal.user_name,
           psdal.mod_time, psdal.operation, 'psd_'::Text || psdal.field_name::Text AS field_name, psdal.old_value, psdal.new_value, 'integer' AS "section_detail_id@type"
        FROM patient_section_details_audit_log psdal
        JOIN patient_section_details psd ON (psdal.section_detail_id=psd.section_detail_id)
        JOIN patient_section_forms psf ON (psd.section_detail_id=psf.section_detail_id)
    WHERE psf.form_type='Form_IP' AND field_name!='section_detail_id' AND psdal.section_id > 0

    UNION ALL
        SELECT psdal.section_detail_id, psdal.log_id, 'display_fields_options_view'::Text AS base_table,
               psdal.mr_no, psdal.patient_id, psdal.section_item_id, sm.section_title, psdal.section_id, psdal.user_name,
               psdal.mod_time, psdal.operation, 'psv_'::Text || psdal.field_name::Text AS field_name,
               psdal.old_value, psdal.new_value, 'integer' AS "section_detail_id@type"
        FROM patient_section_details_audit_log psdal
        JOIN patient_section_forms psf ON (psdal.section_detail_id=psf.section_detail_id)
        JOIN section_master sm ON sm.section_id = psdal.section_id
        WHERE psf.form_type='Form_IP' AND psdal.operation::Text = 'INSERT'::Text
        AND psdal.field_name::Text = 'section_detail_id'::Text
       ; 


DROP VIEW IF EXISTS ot_audit_view CASCADE;
CREATE OR REPLACE VIEW ot_audit_view AS
    SELECT NULL::Integer AS section_detail_id, psdal.log_id, 'patient_section_details_audit_log'::Text AS base_table,
           psdal.mr_no, psdal.patient_id, psdal.section_item_id, NULL::Text AS section_title, psdal.section_id, psdal.user_name,
           psdal.mod_time, psdal.operation, 'psd_'::Text || psdal.field_name::Text AS field_name, psdal.old_value, psdal.new_value, 'integer' AS "section_detail_id@type"
        FROM patient_section_details_audit_log psdal
        JOIN patient_section_details psd ON (psdal.section_detail_id=psd.section_detail_id)
        JOIN patient_section_forms psf ON (psd.section_detail_id=psf.section_detail_id)
    WHERE psf.form_type='Form_OT' AND field_name!='section_detail_id' AND psdal.section_id > 0

    UNION ALL
        SELECT psdal.section_detail_id, psdal.log_id, 'display_fields_options_view'::Text AS base_table,
               psdal.mr_no, psdal.patient_id, psdal.section_item_id, sm.section_title, psdal.section_id, psdal.user_name,
               psdal.mod_time, psdal.operation, 'psv_'::Text || psdal.field_name::Text AS field_name,
               psdal.old_value, psdal.new_value, 'integer' AS "section_detail_id@type"
        FROM patient_section_details_audit_log psdal
        JOIN patient_section_forms psf ON (psdal.section_detail_id=psf.section_detail_id)
        JOIN section_master sm ON sm.section_id = psdal.section_id
        WHERE psf.form_type='Form_OT' AND psdal.operation::Text = 'INSERT'::Text
        AND psdal.field_name::Text = 'section_detail_id'::Text
       ; 

DROP VIEW IF EXISTS serv_audit_view CASCADE;
CREATE OR REPLACE VIEW serv_audit_view AS
    SELECT NULL::Integer AS section_detail_id, psdal.log_id, 'patient_section_details_audit_log'::Text AS base_table,
           psdal.mr_no, psdal.patient_id, psdal.section_item_id, NULL::Text AS section_title, psdal.section_id, psdal.user_name,
           psdal.mod_time, psdal.operation, 'psd_'::Text || psdal.field_name::Text AS field_name, psdal.old_value, psdal.new_value, 'integer' AS "section_detail_id@type"
        FROM patient_section_details_audit_log psdal
        JOIN patient_section_details psd ON (psdal.section_detail_id=psd.section_detail_id)
        JOIN patient_section_forms psf ON (psd.section_detail_id=psf.section_detail_id)
    WHERE psf.form_type='Form_Serv' AND field_name!='section_detail_id' AND psdal.section_id > 0

    UNION ALL
        SELECT psdal.section_detail_id, psdal.log_id, 'display_fields_options_view'::Text AS base_table,
               psdal.mr_no, psdal.patient_id, psdal.section_item_id, sm.section_title, psdal.section_id, psdal.user_name,
               psdal.mod_time, psdal.operation, 'psv_'::Text || psdal.field_name::Text AS field_name,
               psdal.old_value, psdal.new_value, 'integer' AS "section_detail_id@type"
        FROM patient_section_details_audit_log psdal
        JOIN patient_section_forms psf ON (psdal.section_detail_id=psf.section_detail_id)
        JOIN section_master sm ON sm.section_id = psdal.section_id
        WHERE psf.form_type='Form_Serv' AND psdal.operation::Text = 'INSERT'::Text
        AND psdal.field_name::Text = 'section_detail_id'::Text
       ; 

DROP VIEW IF EXISTS gen_audit_view CASCADE;
CREATE OR REPLACE VIEW gen_audit_view AS
	SELECT NULL::Integer AS section_detail_id, psdal.log_id, 'patient_section_details_audit_log'::Text AS base_table,
	       psd.mr_no, psdal.patient_id, psdal.section_item_id, psdal.generic_form_id, NULL::Text AS section_title, psdal.section_id, psdal.user_name,
	       psdal.mod_time, psdal.operation, 'psd_'::Text || psdal.field_name::Text AS field_name, psdal.old_value, psdal.new_value, 'integer' AS "section_detail_id@type"
        FROM patient_section_details_audit_log psdal
        JOIN patient_section_details psd ON (psdal.section_detail_id=psd.section_detail_id)
        JOIN patient_section_forms psf ON (psd.section_detail_id=psf.section_detail_id)
	WHERE psf.form_type='Form_Gen' AND psd.item_type='GEN' AND field_name!='section_detail_id' AND psd.section_id > 0

    UNION ALL
        SELECT psdal.section_detail_id, psdal.log_id, 'display_fields_options_view'::Text AS base_table,
	           psdal.mr_no, psdal.patient_id, psdal.section_item_id, psdal.generic_form_id, sm.section_title, psdal.section_id, psdal.user_name,
	           psdal.mod_time, psdal.operation, 'psv_'::Text || psdal.field_name::Text AS field_name,
	           psdal.old_value, psdal.new_value, 'integer' AS "section_detail_id@type"
	    FROM patient_section_details_audit_log psdal
        JOIN patient_section_forms psf ON (psdal.section_detail_id=psf.section_detail_id)
        JOIN section_master sm ON sm.section_id = psdal.section_id
	    WHERE psf.form_type='Form_Gen' AND psdal.operation::Text = 'INSERT'::Text
	    AND psdal.field_name::Text = 'section_detail_id'::Text
	    ;

DROP VIEW IF EXISTS patient_allergies_audit_log_triage_view CASCADE;
CREATE OR REPLACE VIEW patient_allergies_audit_log_triage_view AS
		SELECT psdal.log_id, psdal.user_name, psdal.mod_time, psdal.operation,
		    psdal.field_name, psdal.old_value, psdal.new_value, psdal.mr_no, psdal.patient_id, psdal.section_item_id, item_type,
		    'patient_section_detail_audit_log' ::text AS base_table, null::integer AS allergy_id ,null::text AS allergy, null::text AS allergy_type
		FROM patient_section_details_audit_log psdal
		JOIN patient_section_details psd ON (psd.section_detail_id=psdal.section_detail_id)
		JOIN patient_section_forms psf ON (psdal.section_detail_id=psf.section_detail_id)
		WHERE psf.form_type='Form_TRI' AND psd.section_id=-2

	UNION ALL
		SELECT paal.log_id, paal.user_name, paal.mod_time, paal.operation,
		    paal.field_name, paal.old_value, paal.new_value, mr_no, patient_id, section_item_id, item_type,
		    'patient_allergies_audit_log' ::text AS base_table, paal.allergy_id, COALESCE(am.allergen_description,gn.generic_name) as allergy, COALESCE(atm.allergy_type_name,'No Known Allergies') as allergy_type
		FROM patient_allergies_audit_log paal
		JOIN patient_section_details psd ON (paal.section_detail_id = psd.section_detail_id)
		JOIN patient_section_forms psf ON (paal.section_detail_id=psf.section_detail_id)
		LEFT JOIN patient_allergies pa ON (pa.allergy_id=paal.allergy_id)
		LEFT JOIN allergen_master am ON (pa.allergen_code_id=am.allergen_code_id)
		LEFT JOIN generic_name gn ON (pa.allergen_code_id = gn.allergen_code_id)
		LEFT JOIN allergy_type_master atm ON (atm.allergy_type_id = pa.allergy_type_id)
		WHERE psf.form_type='Form_TRI' and field_name = 'allergy_id';

DROP VIEW IF EXISTS patient_allergies_audit_log_cons_view CASCADE;
CREATE OR REPLACE VIEW patient_allergies_audit_log_cons_view AS
		SELECT psdal.log_id, psdal.user_name, psdal.mod_time, psdal.operation,
		    psdal.field_name, psdal.old_value, psdal.new_value, psdal.mr_no, psdal.patient_id, psdal.section_item_id, item_type,
		    'patient_section_detail_audit_log' ::text AS base_table, null::integer AS allergy_id, null::text AS allergy, null::text AS allergy_type
		FROM patient_section_details_audit_log psdal
		JOIN patient_section_details psd ON (psd.section_detail_id=psdal.section_detail_id)
		JOIN patient_section_forms psf ON (psdal.section_detail_id=psf.section_detail_id)
		WHERE psf.form_type='Form_CONS' AND psd.section_id=-2

	UNION ALL
		SELECT paal.log_id, paal.user_name, paal.mod_time, paal.operation,
		    paal.field_name, paal.old_value, paal.new_value, mr_no, patient_id, section_item_id, item_type,
		    'patient_allergies_audit_log' ::text AS base_table, paal.allergy_id, COALESCE(am.allergen_description,gn.generic_name) as allergy, COALESCE(atm.allergy_type_name,'No Known Allergies') as allergy_type
		FROM patient_allergies_audit_log paal
		JOIN patient_section_details psd ON (paal.section_detail_id = psd.section_detail_id)
		JOIN patient_section_forms psf ON (paal.section_detail_id=psf.section_detail_id)
		LEFT JOIN patient_allergies pa ON (pa.allergy_id=paal.allergy_id)
		LEFT JOIN allergen_master am ON (pa.allergen_code_id=am.allergen_code_id)
		LEFT JOIN generic_name gn ON (pa.allergen_code_id = gn.allergen_code_id)
		LEFT JOIN allergy_type_master atm ON (atm.allergy_type_id = pa.allergy_type_id)
		WHERE psf.form_type='Form_CONS' and field_name = 'allergy_id';

DROP VIEW IF EXISTS patient_allergies_audit_log_ipf_view CASCADE;
CREATE OR REPLACE VIEW patient_allergies_audit_log_ipf_view AS
		SELECT psdal.log_id, psdal.user_name, psdal.mod_time, psdal.operation,
		    psdal.field_name, psdal.old_value, psdal.new_value, psdal.mr_no, psdal.patient_id, psdal.section_item_id, item_type,
		    'patient_section_detail_audit_log' ::text AS base_table, null::integer AS allergy_id, null::text AS allergy, null::text AS allergy_type
		FROM patient_section_details_audit_log psdal
		JOIN patient_section_details psd ON (psd.section_detail_id=psdal.section_detail_id)
		JOIN patient_section_forms psf ON (psdal.section_detail_id=psf.section_detail_id)
		WHERE psf.form_type='Form_IP' AND psd.section_id=-2

	UNION ALL
		SELECT paal.log_id, paal.user_name, paal.mod_time, paal.operation,
		    paal.field_name, paal.old_value, paal.new_value, mr_no, patient_id, section_item_id, item_type,
		    'patient_allergies_audit_log' ::text AS base_table, paal.allergy_id, COALESCE(am.allergen_description,gn.generic_name) as allergy, COALESCE(atm.allergy_type_name,'No Known Allergies') as allergy_type
		FROM patient_allergies_audit_log paal
		JOIN patient_section_details psd ON (paal.section_detail_id = psd.section_detail_id)
		JOIN patient_section_forms psf ON (paal.section_detail_id=psf.section_detail_id)
		LEFT JOIN patient_allergies pa ON (pa.allergy_id=paal.allergy_id)
		LEFT JOIN allergen_master am ON (pa.allergen_code_id=am.allergen_code_id)
		LEFT JOIN generic_name gn ON (pa.allergen_code_id = gn.allergen_code_id)
		LEFT JOIN allergy_type_master atm ON (atm.allergy_type_id = pa.allergy_type_id)
		WHERE psf.form_type='Form_IP' and field_name = 'allergy_id';


DROP VIEW IF EXISTS patient_allergies_audit_log_genf_view CASCADE;
CREATE OR REPLACE VIEW patient_allergies_audit_log_genf_view AS
		SELECT psdal.log_id, psdal.user_name, psdal.mod_time, psdal.operation,
		    psdal.field_name, psdal.old_value, psdal.new_value, psd.generic_form_id,psdal.mr_no, psdal.patient_id, psdal.section_item_id, item_type,
		    'patient_section_detail_audit_log' ::text AS base_table, null::integer AS allergy_id, null::text AS allergy, null::text AS allergy_type
		FROM patient_section_details_audit_log psdal
		JOIN patient_section_details psd ON (psd.section_detail_id=psdal.section_detail_id)
		JOIN patient_section_forms psf ON (psdal.section_detail_id=psf.section_detail_id)
		WHERE psf.form_type='Form_Gen' AND psd.section_id=-2

	UNION ALL
		SELECT paal.log_id, paal.user_name, paal.mod_time, paal.operation,
		    paal.field_name, paal.old_value, paal.new_value,psd.generic_form_id,mr_no, patient_id, section_item_id, item_type,
		    'patient_allergies_audit_log' ::text AS base_table, paal.allergy_id, COALESCE(am.allergen_description,gn.generic_name) as allergy, COALESCE(atm.allergy_type_name,'No Known Allergies') as allergy_type
		FROM patient_allergies_audit_log paal
		JOIN patient_section_details psd ON (paal.section_detail_id = psd.section_detail_id)
		JOIN patient_section_forms psf ON (paal.section_detail_id=psf.section_detail_id)
		LEFT JOIN patient_allergies pa ON (pa.allergy_id=paal.allergy_id)
		LEFT JOIN allergen_master am ON (pa.allergen_code_id=am.allergen_code_id)
		LEFT JOIN generic_name gn ON (pa.allergen_code_id = gn.allergen_code_id)
		LEFT JOIN allergy_type_master atm ON (atm.allergy_type_id = pa.allergy_type_id)
		WHERE psf.form_type='Form_Gen' and field_name = 'allergy_id';


DROP VIEW IF EXISTS patient_complaints_audit_log_view CASCADE;
CREATE OR REPLACE VIEW patient_complaints_audit_log_view AS
		SELECT pral.log_id,pr.mr_no,pral.patient_id,pral.user_name,pral.mod_time,pral.operation,
		       pral.field_name,pral.old_value,pral.new_value
		FROM patient_registration_audit_log pral
		JOIN patient_registration pr ON pr.patient_id = pral.patient_id
		WHERE pral.field_name IN ('complaint')
	UNION ALL
		SELECT scal.log_id,pr.mr_no,scal.visit_id AS patient_id,scal.user_name,scal.mod_time,scal.operation,
		       CASE WHEN scal.field_name = 'complaint' THEN 'secondary_complaint'
		       ELSE scal.field_name END,scal.old_value,scal.new_value
		FROM secondary_complaints_audit_log scal
		JOIN patient_registration pr ON pr.patient_id = scal.visit_id;

DROP VIEW IF EXISTS patient_diagnosis_details_audit_log_view CASCADE;
CREATE OR REPLACE VIEW patient_diagnosis_details_audit_log_view AS
		SELECT pral.log_id, pral.user_name, pral.mod_time, pral.operation,
		    pral.field_name, pral.old_value, pral.new_value, pr.mr_no, pral.patient_id ,
		    'patient_registration_audit_log' ::text AS base_table, null::numeric AS id
		FROM patient_registration_audit_log pral
		LEFT JOIN patient_registration pr ON pral.patient_id = pr.patient_id
		WHERE  field_name = 'patient_id'
	UNION ALL
		SELECT mdal.log_id, mdal.user_name, mdal.mod_time, mdal.operation,
		    mdal.field_name, mdal.old_value, mdal.new_value, pr.mr_no, mdal.visit_id ,
		    'mrd_diagnosis_audit_log' ::text AS base_table, mdal.id
		FROM mrd_diagnosis_audit_log mdal
		LEFT JOIN mrd_diagnosis md ON (md.id=mdal.id)
		LEFT JOIN patient_registration pr ON mdal.visit_id = pr.patient_id
		WHERE field_name = 'id';

DROP VIEW IF EXISTS admission_request_diagnosis_audit_log_view CASCADE;
CREATE OR REPLACE VIEW admission_request_diagnosis_audit_log_view AS
		SELECT parl.log_id, parl.user_name, parl.mod_time, parl.operation,
		    parl.field_name, parl.old_value, parl.new_value, par.mr_no, parl.adm_request_id ,
		    'patient_admission_request_audit_log' ::text AS base_table, null::numeric AS id
		FROM patient_admission_request_audit_log parl
		JOIN patient_admission_request par ON  parl.adm_request_id  = par.adm_request_id
	UNION ALL
		SELECT mdal.log_id, mdal.user_name, mdal.mod_time, mdal.operation,
		    mdal.field_name, mdal.old_value, mdal.new_value, par.mr_no, mdal.adm_request_id ,
		    'mrd_diagnosis_audit_log' ::text AS base_table, mdal.id
		FROM mrd_diagnosis_audit_log mdal
		JOIN patient_admission_request par ON  mdal.adm_request_id = par.adm_request_id
		WHERE field_name = 'id';

DROP FUNCTION IF EXISTS patient_allergies_delete_trigger_func() CASCADE;
CREATE OR REPLACE FUNCTION patient_allergies_delete_trigger_func() RETURNS TRIGGER AS $BODY$
DECLARE

BEGIN
	insert into patient_allergies_audit_log(section_detail_id, allergy_id, user_name, operation, field_name, old_value, new_value)
	values(OLD.section_detail_id, OLD.allergy_id, OLD.username, 'DELETE', 'allergy_id', OLD.allergy_id, 'Record Deleted');

    RETURN OLD;
END;
$BODY$ LANGUAGE plpgsql;

DROP FUNCTION IF EXISTS mrd_diagnosis_delete_trigger_func() CASCADE;
CREATE OR REPLACE FUNCTION mrd_diagnosis_delete_trigger_func() RETURNS TRIGGER AS $BODY$
DECLARE

BEGIN

	insert into mrd_diagnosis_audit_log(visit_id, id, adm_request_id, user_name, operation, field_name, old_value, new_value)
	values(OLD.visit_id, OLD.id, OLD.adm_request_id, OLD.username, 'DELETE', 'id', OLD.id, 'Record Deleted');

    RETURN OLD;
END;
$BODY$ LANGUAGE plpgsql;

DROP FUNCTION IF EXISTS secondary_complaints_delete_trigger_func() CASCADE;
CREATE OR REPLACE FUNCTION secondary_complaints_delete_trigger_func() RETURNS TRIGGER AS $BODY$
DECLARE

BEGIN

	insert into secondary_complaints_audit_log(visit_id, user_name, operation, field_name, old_value, new_value)
	values(OLD.visit_id, OLD.username, 'DELETE', 'complaint', OLD.complaint, 'Record Deleted');

    RETURN OLD;
END;
$BODY$ LANGUAGE plpgsql;

DROP VIEW IF EXISTS op_patient_list cascade;
CREATE VIEW op_patient_list AS 
SELECT sa.mr_no, coalesce(pd.patient_name, sa.patient_name) AS patient_name, pd.middle_name, pd.last_name,
		case WHEN coalesce(sm.salutation, '') = '' THEN '' ELSE (sm.salutation || ' ') END 
		|| coalesce(pd.patient_name, sa.patient_name)
        || CASE WHEN coalesce(middle_name, '') = '' THEN '' ELSE (' ' || middle_name) end
        || CASE WHEN coalesce(last_name, '') = '' THEN '' ELSE (' ' || last_name) end
	as full_name, pd.patient_gender,
	CASE
		WHEN pd.patient_gender = 'M' THEN 'Male'
		WHEN pd.patient_gender = 'F' THEN 'Female'
		WHEN pd.patient_gender = 'O' THEN 'Others'
		WHEN pd.patient_gender = 'N' THEN 'Not Applicable'
		ELSE pd.patient_gender
	END AS patient_gender_text,

	CASE
		WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 30.43
		THEN (current_date - COALESCE(pd.dateofbirth, pd.expected_dob))::text || 'D'
		WHEN (current_date - COALESCE(pd.dateofbirth, pd.expected_dob)) < 365.25
		THEN floor((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/30.43)::text || 'M'
		WHEN (current_date - COALESCE(pd.dateofbirth, pd.expected_dob)) < (365.25*5)
		THEN floor((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/365.25)::text || 'Y' || '+' || floor(((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))%365)/30.43)::text || 'M'
		ELSE floor((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/365.25)::text || 'Y'
		END AS age_text,

	coalesce(sa.patient_contact, pd.patient_phone) AS patient_phone, pd.government_identifier, pd.dateofbirth, pd.expected_dob, 
	pd.oldmrno, coalesce(sa.patient_contact_country_code, pd.patient_phone_country_code) AS patient_phone_country_code,

	pr.reg_date AS visit_date, pr.reg_time AS visit_time, pr.reg_date+pr.reg_time AS visit_date_time, 
	coalesce(pr.visit_type, 'o') AS visit_type,
	pr.status AS visit_status, b.status AS bill_status, b.payment_status, b.open_date AS bill_open_date, doc.doctor_id AS doctor, doc.doctor_name, 
	coalesce(dept.dept_id, test.ddept_id, s.serv_dept_id||'') AS department, 
	coalesce(dept.dept_name, test_dept.ddept_name, sd.department) AS dept_name, 
	p_ins.insurance_co AS primary_insurance, picm.insurance_co_name AS primary_insurance_co_name,
	s_ins.insurance_co AS secondary_insurance, sicm.insurance_co_name AS secondary_insurance_co_name, 
	case WHEN appointment_time::date = current_date or coalesce(sa.mr_no, '') = '' THEN appointment_time ELSE pr.reg_date+pr.reg_time END AS date_time, 
	srt.primary_resource, srt.resource_type, srt.category, coalesce(doc.doctor_name, s.service_name, test.test_name) AS resource_name,
   
	null AS order_date, sa.appointment_time, sa.appointment_id, sa.appt_token, coalesce(pr.center_id, sa.center_id) AS center_id,

	sa.visit_id AS patient_id, b.bill_no, 0 AS prescribed_id, b.bill_type, 'appointment' AS type  
FROM scheduler_appointments sa
	JOIN scheduler_appointment_items sai USING (appointment_id)
	JOIN scheduler_master sch_m ON (sa.res_sch_id=sch_m.res_sch_id)
	JOIN scheduler_resource_types srt ON (srt.category=sch_m.res_sch_category and sai.resource_type=srt.resource_type)
	LEFT JOIN patient_details pd ON (pd.mr_no=sa.mr_no)
	LEFT JOIN salutation_master sm ON (sm.salutation_id=pd.salutation)
	LEFT JOIN patient_registration pr ON (pr.patient_id=sa.visit_id)
	LEFT JOIN patient_insurance_plans p_ins ON (p_ins.patient_id=pr.patient_id and p_ins.priority=1)
	LEFT JOIN patient_insurance_plans s_ins ON (s_ins.patient_id=pr.patient_id and s_ins.priority=2)
	LEFT JOIN insurance_company_master picm ON 
		(p_ins.insurance_co=picm.insurance_co_id)
	LEFT JOIN insurance_company_master sicm ON 
		(s_ins.insurance_co=sicm.insurance_co_id)

	LEFT JOIN bill b ON (b.visit_id=pr.patient_id and b.status!='X' and b.restriction_type!='P')
	LEFT JOIN doctors doc ON (sai.resource_type in ('DOC', 'OPDOC', 'LABTECH') and sai.resource_id=doc.doctor_id)
	LEFT JOIN services s ON (s.service_id=sa.res_sch_name)
	LEFT JOIN services_departments sd ON (s.serv_dept_id=sd.serv_dept_id)
	LEFT JOIN diagnostics test ON (test.test_id=sa.res_sch_name)
	LEFT JOIN diagnostics_departments test_dept ON (test_dept.ddept_id=test.ddept_id)
	LEFT JOIN department dept ON (doc.dept_id=dept.dept_id)
WHERE sa.appointment_status in ('Booked', 'confirmed', 'Confirmed', 'Arrived', 'Completed')
	and srt.category in ('DIA', 'SNP', 'DOC') AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )
	

UNION ALL

SELECT pd.mr_no, pd.patient_name, pd.middle_name, pd.last_name, 
	case WHEN coalesce(sm.salutation, '') = '' THEN '' ELSE (sm.salutation || ' ') END ||  coalesce(pd.patient_name, '')
        || CASE WHEN coalesce(middle_name, '') = '' THEN '' ELSE (' ' || middle_name) end
        || CASE WHEN coalesce(last_name, '') = '' THEN '' ELSE (' ' || last_name) end
	as full_name, pd.patient_gender,
	CASE
		WHEN pd.patient_gender = 'M' THEN 'Male'
		WHEN pd.patient_gender = 'F' THEN 'Female'
		WHEN pd.patient_gender = 'O' THEN 'Others'
		WHEN pd.patient_gender = 'N' THEN 'Not Applicable'
		ELSE pd.patient_gender
	END AS patient_gender_text,

	CASE
		WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 30.43
		THEN (current_date - COALESCE(pd.dateofbirth, pd.expected_dob))::text || 'D'
		WHEN (current_date - COALESCE(pd.dateofbirth, pd.expected_dob)) < 365.25
		THEN floor((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/30.43)::text || 'M'
		WHEN (current_date - COALESCE(pd.dateofbirth, pd.expected_dob)) < (365.25*5)
		THEN floor((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/365.25)::text || 'Y' || '+' || floor(((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))%365)/30.43)::text || 'M'
		ELSE floor((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/365.25)::text || 'Y'
		END AS age_text, 

	pd.patient_phone, pd.government_identifier, pd.dateofbirth, pd.expected_dob, pd.oldmrno, pd.patient_phone_country_code,
	
	pr.reg_date AS visit_date, pr.reg_time AS visit_time, pr.reg_date+reg_time AS visit_date_time, pr.visit_type,
	coalesce(pr.status, 'N') AS visit_status, b.status AS bill_status, b.payment_status, b.open_date AS bill_open_date, 
	doc.doctor_id AS doctor, 	doc.doctor_name, dept.dept_id AS department, 
	dept.dept_name AS dept_name, p_ins.insurance_co AS primary_insurance, picm.insurance_co_name AS primary_insurance_co_name,
	s_ins.insurance_co AS secondary_insurance, sicm.insurance_co_name AS secondary_insurance_co_name, pr.reg_date+pr.reg_time AS date_time, 
	false AS primary_resource, '' AS resource_type, '' AS category, '' AS resource_name,
   
	dc.visited_date AS order_date, null AS appointment_time, null AS appointment_id, null AS appt_token, pr.center_id,
	pr.patient_id, b.bill_no, coalesce(doc_bill.activity_id::integer, 0) AS prescribed_id, b.bill_type, 'patient' AS type 
FROM patient_details pd 
	LEFT JOIN salutation_master sm ON (sm.salutation_id=pd.salutation)
	LEFT JOIN patient_registration pr ON (pd.mr_no=pr.mr_no)
	LEFT JOIN department dept ON (pr.dept_name=dept.dept_id)
	LEFT JOIN doctor_consultation dc ON (dc.patient_id=pr.patient_id AND dc.status!='U' and dc.cancel_status is null)
	LEFT JOIN doctors doc ON (doc.doctor_id=dc.doctor_name)
	LEFT JOIN bill b ON (pr.patient_id=b.visit_id and b.status!='X' and b.restriction_type!='P')
	LEFT JOIN (
		SELECT bc.bill_no, bac.activity_id, bac.activity_code
		FROM bill_activity_charge bac
			JOIN bill_charge bc ON (bac.charge_id=bc.charge_id)
		WHERE activity_code = 'DOC'
	) AS doc_bill ON (doc_bill.bill_no=b.bill_no and doc_bill.activity_id=dc.consultation_id::text)
	LEFT JOIN patient_insurance_plans p_ins ON (p_ins.patient_id=pr.patient_id and p_ins.priority=1)
	LEFT JOIN insurance_company_master picm ON 
		(p_ins.insurance_co=picm.insurance_co_id)	
	LEFT JOIN patient_insurance_plans s_ins ON (s_ins.patient_id=pr.patient_id and s_ins.priority=2)
	LEFT JOIN insurance_company_master sicm ON 
		(s_ins.insurance_co=sicm.insurance_co_id)
WHERE ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )
;

DROP VIEW IF EXISTS patient_appointment_or_visit_count CASCADE;
CREATE VIEW  patient_appointment_or_visit_count AS
	SELECT sa.mr_no, sa.visit_id, d.doctor_id, d.doctor_name, sa.appointment_id, 
		coalesce(dept.dept_id, test_dept.ddept_id, sd.serv_dept_id||'') AS dept_id, 
		coalesce(dept.dept_name, test_dept.ddept_name, sd.department) AS dept_name, 'future_appointment' AS type,
		appointment_time AS date_time, sa.center_id, coalesce(d.doctor_name, s.service_name, test.test_name) AS resource_name,
		sa.res_sch_name AS resource_id, srt.category 
	FROM scheduler_appointments sa
		JOIN scheduler_appointment_items sai USING (appointment_id)
		JOIN scheduler_master sm ON (sa.res_sch_id=sm.res_sch_id)
		JOIN scheduler_resource_types srt ON (srt.category=sm.res_sch_category and sai.resource_type=srt.resource_type)
		LEFT JOIN doctors d ON (sai.resource_type in ('DOC', 'OPDOC', 'LABTECH') and sai.resource_id=d.doctor_id)
		LEFT JOIN department dept ON (dept.dept_id=d.dept_id)
		LEFT JOIN services s ON (s.service_id=sa.res_sch_name)
		LEFT JOIN services_departments sd ON (s.serv_dept_id=sd.serv_dept_id)
		LEFT JOIN diagnostics test ON (test.test_id=sa.res_sch_name)
		LEFT JOIN diagnostics_departments test_dept ON (test_dept.ddept_id=test.ddept_id)
	WHERE sa.appointment_status in ('Booked', 'confirmed', 'Confirmed')
		AND appointment_time::date > current_date and srt.category in ('DIA', 'SNP', 'DOC') 
	
	UNION ALL 

	SELECT sa.mr_no, sa.visit_id, d.doctor_id, d.doctor_name, sa.appointment_id,
		coalesce(dept.dept_id, test_dept.ddept_id, sd.serv_dept_id||'') AS dept_id, 
		coalesce(dept.dept_name, test_dept.ddept_name, sd.department) AS dept_name, 'today_appointment' AS type,
		appointment_time AS date_time, sa.center_id, coalesce(d.doctor_name, s.service_name, test.test_name) AS resource_name,
		sa.res_sch_name AS resource_id, srt.category
	FROM scheduler_appointments sa
		JOIN scheduler_appointment_items sai USING (appointment_id)
		JOIN scheduler_master sm ON (sa.res_sch_id=sm.res_sch_id)
		JOIN scheduler_resource_types srt ON (srt.category=sm.res_sch_category and sai.resource_type=srt.resource_type)
		LEFT JOIN doctors d ON (sai.resource_type in ('DOC', 'OPDOC', 'LABTECH') and sai.resource_id=d.doctor_id)
		LEFT JOIN department dept ON (dept.dept_id=d.dept_id)
		LEFT JOIN services s ON (s.service_id=sa.res_sch_name)
		LEFT JOIN services_departments sd ON (s.serv_dept_id=sd.serv_dept_id)
		LEFT JOIN diagnostics test ON (test.test_id=sa.res_sch_name)
		LEFT JOIN diagnostics_departments test_dept ON (test_dept.ddept_id=test.ddept_id)
	WHERE sa.appointment_status IN ('Booked', 'confirmed', 'Confirmed')
		AND appointment_time::date = current_date and srt.category in ('DIA', 'SNP', 'DOC')

	UNION ALL 

	SELECT sa.mr_no, sa.visit_id, d.doctor_id, d.doctor_name, sa.appointment_id,
		coalesce(dept.dept_id, test_dept.ddept_id, sd.serv_dept_id||'') AS dept_id, 
		coalesce(dept.dept_name, test_dept.ddept_name, sd.department) AS dept_name, 'past_appointment' AS type,
		appointment_time AS date_time, sa.center_id, coalesce(d.doctor_name, s.service_name, test.test_name) AS resource_name,
		sa.res_sch_name AS resource_id, srt.category
	FROM scheduler_appointments sa
		JOIN scheduler_appointment_items sai USING (appointment_id)
		JOIN scheduler_master sm ON (sa.res_sch_id=sm.res_sch_id)
		JOIN scheduler_resource_types srt ON (srt.category=sm.res_sch_category and sai.resource_type=srt.resource_type)
		LEFT JOIN doctors d ON (sai.resource_type in ('DOC', 'OPDOC', 'LABTECH') and sai.resource_id=d.doctor_id)
		LEFT JOIN department dept ON (dept.dept_id=d.dept_id)
		LEFT JOIN services s ON (s.service_id=sa.res_sch_name)
		LEFT JOIN services_departments sd ON (s.serv_dept_id=sd.serv_dept_id)
		LEFT JOIN diagnostics test ON (test.test_id=sa.res_sch_name)
		LEFT JOIN diagnostics_departments test_dept ON (test_dept.ddept_id=test.ddept_id)
	WHERE sa.appointment_status IN ('Arrived', 'Completed')
		AND appointment_time::date < current_date and srt.category in ('DIA', 'SNP', 'DOC')
	
	UNION ALL 

	SELECT pd.mr_no, pr.patient_id, pr.doctor AS doctor_id, null AS doctor_name, 0 AS appointment_id,
		dept.dept_id, dept.dept_name, 'today_visit' AS type,
		reg_date+reg_time AS date_time, pr.center_id, '' AS resource_name, '' AS resource_id, '' AS category 
	FROM patient_details pd 
		LEFT JOIN patient_registration pr USING (mr_no)
		LEFT JOIN department dept ON (dept.dept_id=pr.dept_name)
	WHERE pr.reg_date = current_date
	
	UNION ALL
	
	SELECT pd.mr_no, pr.patient_id, d.doctor_id, d.doctor_name, 0 AS appointment_id,
		dept.dept_id, dept.dept_name, 'today_doctor_order' AS type,
		reg_date+reg_time AS date_time, pr.center_id, '' AS resource_name, '' AS resource_id, '' AS category 
	FROM patient_details pd 
		JOIN patient_registration pr USING (mr_no)
		JOIN doctor_consultation dc ON (pr.patient_id=dc.patient_id AND dc.status!='U')
		JOIN doctors d ON (d.doctor_id=dc.doctor_name)
		JOIN department dept ON (dept.dept_id=coalesce(d.dept_id, pr.dept_name))
	WHERE pr.reg_date = current_date and dc.cancel_status is null
	
	UNION ALL 

	SELECT pd.mr_no, pr.patient_id, d.doctor_id, d.doctor_name, 0 AS appointment_id,
		dept.dept_id, dept.dept_name, 'past_visit' AS type,
		reg_date+reg_time AS date_time, pr.center_id, '' AS resource_name, '' AS resource_id, '' AS category
	FROM patient_details pd 
		JOIN patient_registration pr USING (mr_no)
		LEFT JOIN doctor_consultation dc ON (pr.patient_id=dc.patient_id AND dc.status!='U' and dc.cancel_status is null)
		LEFT JOIN doctors d ON (d.doctor_id=dc.doctor_name)
		LEFT JOIN department dept ON (dept.dept_id=coalesce(d.dept_id, pr.dept_name))
	WHERE pr.reg_date < current_date 
;



DROP VIEW IF EXISTS patient_or_appointment_details_view cascade;
create view patient_or_appointment_details_view AS 
SELECT pd.mr_no, null::integer AS appointment_id, pd.patient_name, pd.middle_name, pd.last_name, pd.email_id, 
	sm.salutation_id, sm.salutation,
	case WHEN coalesce(sm.salutation, '') = '' THEN '' ELSE (sm.salutation || ' ') END 
		|| patient_name
        || CASE WHEN coalesce(middle_name, '') = '' THEN '' ELSE (' ' || middle_name) end
        || CASE WHEN coalesce(last_name, '') = '' THEN '' ELSE (' ' || last_name) end
	as full_name,
	pd.dateofbirth,
	CASE 
		WHEN pd.dateofbirth != null THEN null
		ELSE pd.expected_dob
	END AS expected_dob,
	pd.patient_gender,
	CASE
		WHEN pd.patient_gender = 'M' THEN 'Male'
		WHEN pd.patient_gender = 'F' THEN 'Female'
		WHEN pd.patient_gender = 'O' THEN 'Others'
		WHEN pd.patient_gender = 'N' THEN 'Not Applicable'
		ELSE pd.patient_gender
	END AS patient_gender_text,
	CASE
		WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 30.43
		THEN (current_date - COALESCE(pd.dateofbirth, pd.expected_dob))::text || 'D'
		WHEN (current_date - COALESCE(pd.dateofbirth, pd.expected_dob)) < 365.25
		THEN floor((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/30.43)::text || 'M'
		WHEN (current_date - COALESCE(pd.dateofbirth, pd.expected_dob)) < (365.25*5)
		THEN floor((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/365.25)::text || 'Y' || '+' || floor(((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))%365)/30.43)::text || 'M'
		ELSE floor((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/365.25)::text || 'Y'
		END AS age_text,
	pd.patient_phone, pd.government_identifier, pd.patient_phone_country_code
FROM patient_details pd
	LEFT JOIN salutation_master sm ON (pd.salutation=sm.salutation_id) AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )

UNION 

SELECT null AS mr_no, sa.appointment_id, sa.patient_name, null AS middle_name, null AS last_name, null AS email_id,
	null AS salutation_id, null AS salutation,
	sa.patient_name AS full_name, 
	null AS dateofbirth, null AS expected_dob,
	null AS patient_gender, null AS patient_gender_text,  '' AS age_text, sa.patient_contact AS patient_phone, null AS government_identifier,
	sa.patient_contact_country_code
FROM scheduler_appointments sa
	where coalesce(mr_no, '')='';

DROP VIEW IF EXISTS patient_header_advanced_details_view CASCADE;
CREATE OR REPLACE VIEW patient_header_advanced_details_view AS
SELECT
	/* Patient fields  */
	pd.mr_no, pd.salutation AS salutation_id, sm.salutation, pd.patient_name, pd.middle_name, pd.last_name,
	sm.salutation || ' ' || patient_name
        || CASE WHEN coalesce(middle_name, '') = '' THEN '' ELSE (' ' || middle_name) end
        || CASE WHEN coalesce(last_name, '') = '' THEN '' ELSE (' ' || last_name) end
	as full_name,
	pd.patient_gender,pd.other_identification_doc_id,pd.other_identification_doc_value,
	CASE
		WHEN oidt.other_identification_doc_name is not NULL THEN oidt.other_identification_doc_name
		ELSE 'Other Identification Doc Value'
	END AS other_identification_doc_value_text,
	CASE
		WHEN pd.patient_gender = 'M' THEN 'Male'
		WHEN pd.patient_gender = 'F' THEN 'Female'
		WHEN pd.patient_gender = 'O' THEN 'Others'
		WHEN pd.patient_gender = 'N' THEN 'Not Applicable'
		ELSE pd.patient_gender
	END AS patient_gender_text,
	pd.dateofbirth,
	CASE 
		WHEN pd.dateofbirth != null THEN null
		ELSE pd.expected_dob
	END AS expected_dob,
	CASE
		WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 30.43
		THEN (current_date - COALESCE(pd.dateofbirth, pd.expected_dob))::text || 'D'
		WHEN (current_date - COALESCE(pd.dateofbirth, pd.expected_dob)) < 365.25
		THEN floor((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/30.43)::text || 'M'
		WHEN (current_date - COALESCE(pd.dateofbirth, pd.expected_dob)) < (365.25*5)
		THEN floor((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/365.25)::text || 'Y' || '+' || floor(((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))%365)/30.43)::text || 'M'
		ELSE floor((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/365.25)::text || 'Y'
		END AS age_text,
	pd.patient_phone, pd.patient_phone2 AS addnl_phone, pd.patient_address, pd.patient_area,
	pd.oldmrno, pd.casefile_no, pd.remarks,
	pd.patient_care_oftext, pd.patient_careof_address, pd.relation, pd.next_of_kin_relation,
	pd.death_date, pd.death_time,pd.dead_on_arrival,
	pd.custom_field1, pd.custom_field2, pd.custom_field3, pd.custom_field4, pd.custom_field5, pd.custom_field6,
	pd.custom_field7, pd.custom_field8, pd.custom_field9, pd.custom_field10, pd.custom_field11, pd.custom_field12,
	pd.custom_field13,custom_field14,pd.custom_field15,pd.custom_field16,pd.custom_field17,
	pd.custom_field18,pd.custom_field19, pd.original_mr_no,ad.isbaby,

	pd.custom_list1_value, pd.custom_list2_value, pd.custom_list3_value,pd.custom_list4_value,
	custom_list5_value,custom_list6_value,custom_list7_value,custom_list8_value,custom_list9_value,

	-- visit custom field values
	pra.visit_custom_list1, pra.visit_custom_list2,

	-- visit custom list values
	pra.visit_custom_field1, pra.visit_custom_field2, pra.visit_custom_field3,pra.visit_custom_field4,pra.visit_custom_field5,pra.visit_custom_field6,
	pra.visit_custom_field7,pra.visit_custom_field8,pra.visit_custom_field9,

	pd.patient_category_id, pd.category_expiry_date, pcm.category_name, pd_pcm.category_name AS patient_category_name,
	pd.patient_consultation_info,
	CASE WHEN pd.patient_photo IS NULL THEN 'N' ELSE 'Y' END AS patient_photo_available,
	pd.previous_visit_id, pd.visit_id, pd.no_allergies, pd.med_allergies, pd.food_allergies, pd.other_allergies, pd.vip_status,
	pd.government_identifier, pd.identifier_id, pd.portal_access, pd.email_id,
	pd.passport_no, pd.passport_validity, pd.passport_issue_country, pd.visa_validity, pd.family_id,
	pd.mod_time AS patient_mod_time,
	/* Visit related fields */
	pra.patient_id, pra.status AS visit_status, pra.visit_type, pra.revisit, pra.reg_date, pra.reg_time,
	pra.op_type, otn.op_type_name, pra.main_visit_id,
	pra.mlc_status, pra.patient_category_id AS patient_category,
	pra.patient_care_oftext AS patcontactperson, pra.relation AS patrelation,
	pra.patient_careof_address AS pataddress,
	pra.complaint, pra.analysis_of_complaint,
	pra.doctor, dr.doctor_name,
	pra.admitted_dept, admdep.dept_name AS admitted_dept_name, /* original dept admitted to */
	pra.dept_name AS dept_id, dep.dept_name,  pra.unit_id, dum.unit_name,	/* current dept */
	pra.org_id, od.org_name, od.store_rate_plan_id,
	pra.bed_type AS bill_bed_type, bn.bed_type AS alloc_bed_type, bn.bed_name AS alloc_bed_name,
	pra.ward_id AS reg_ward_id, wnr.ward_name AS reg_ward_name, wn.ward_name AS alloc_ward_name,
	ad.admit_date AS bed_start_date, date(ad.finalized_time) AS bed_end_date,
	pra.discharge_doc_id AS dis_doc_id, pra.discharge_format AS dis_format,
	pra.discharge_flag, pra.discharge_doctor_id, pra.discharge_date, pra.discharge_time,
	pra.discharge_finalized_date AS dis_finalized_date, pra.discharge_finalized_time AS dis_finalized_time,
	pra.discharge_finalized_user AS dis_finalized_user, pra.discharged_by, pra.user_name AS admitted_by,
	pra.codification_status,pra.established_type, pra.disch_date_for_disch_summary, pra.disch_time_for_disch_summary,
	pra.reference_docto_id, COALESCE(drs.doctor_name, rd.referal_name) AS refdoctorname,
	pra.reg_charge_accepted,pra.use_drg, pra.drg_code, pra.use_perdiem, pra.per_diem_code,
	pra.mlc_no, pra.mlc_type, pra.accident_place, pra.police_stn, pra.mlc_remarks, pra.certificate_status,
	pmd.icd_code AS primary_diagnosis_code, pmd.description AS primary_diagnosis_description,
	(select textcat_commacat(description) from mrd_diagnosis md where (md.visit_id=pra.main_visit_id and diag_type='S'))
	as secondary_diagnosis_description,
	pra.primary_insurance_approval,pra.secondary_insurance_approval,
	pra.primary_sponsor_id, pra.secondary_sponsor_id,
	tpa.tpa_name, stpa.tpa_name AS sec_tpa_name,
	pst.sponsor_type_name AS sponsor_type , sst.sponsor_type_name AS sec_sponsor_type,
	icm.insurance_co_name, icm.insurance_co_address,
	sicm.insurance_co_name AS sec_insurance_co_name, sicm.insurance_co_address AS sec_insurance_co_address,
	pra.insurance_id, pra.category_id AS insurance_category,
	pra.plan_id, pra.prior_auth_id, pra.prior_auth_mode_id,
	pra.doc_id, pra.primary_insurance_co, pra.secondary_insurance_co,
	icam.category_name AS plan_type_name, ipm.plan_exclusions, ipm.plan_notes, ipm.plan_name,
	pra.patient_policy_id,pra.docs_download_passcode,
	ppd.member_id AS member_id,
	ppd.policy_number AS policy_number,
	ppd.policy_validity_start AS policy_validity_start,
	ppd.policy_validity_end AS policy_validity_end,
	ppd.policy_holder_name AS policy_holder_name,
	ppd.patient_relationship AS patient_relationship,
	pcd.patient_relationship AS patient_corporate_relation, pcd.sponsor_id AS corporate_sponsor_id,
	pcd.employee_id, pcd.employee_name, pnd.sponsor_id AS national_sponsor_id,
	pnd.national_id, pnd.citizen_name, pnd.patient_relationship AS patient_national_relation,
	spcd.patient_relationship AS sec_patient_corporate_relation, spcd.sponsor_id AS sec_corporate_sponsor_id,
	spcd.employee_id AS sec_employee_id, spcd.employee_name AS sec_employee_name, spnd.sponsor_id AS sec_national_sponsor_id,
	spnd.national_id AS sec_national_id, spnd.citizen_name AS sec_citizen_name ,spnd.patient_relationship AS sec_patient_national_relation
	, pra.signatory_username,pra.collection_center_id, coalesce(require_pbm_authorization, 'N') AS require_pbm_authorization,
	   pst.member_id_label AS primary_member_id_label, sst.member_id_label AS secondary_member_id_label,
	   pst.plan_type_label AS primary_plan_type_label, sst.plan_type_label AS secondary_plan_type_label, pra.patient_discharge_status, ds.discharge_state_name,
	case WHEN cpref.lang_code is not NULL THEN cpref.lang_code ELSE (select contact_pref_lang_code from generic_preferences) END AS contact_pref_lang_code,
   pd.nationality_id,nc.country_name AS nationality_name
FROM patient_details pd
   LEFT JOIN patient_registration pra ON pra.mr_no = pd.mr_no
   LEFT JOIN op_type_names otn ON (otn.op_type = pra.op_type)
   LEFT JOIN ward_names wnr ON wnr.ward_no = pra.ward_id
   LEFT JOIN discharge_state_names ds ON (ds.discharge_state = pra.patient_discharge_status)
   LEFT JOIN salutation_master sm ON pd.salutation = sm.salutation_id
   LEFT JOIN country_master nc ON pd.nationality_id = nc.country_id
   LEFT JOIN department dep ON pra.dept_name = dep.dept_id
   LEFT JOIN department admdep ON pra.admitted_dept = admdep.dept_id
   LEFT JOIN dept_unit_master dum ON dum.unit_id = pra.unit_id
   LEFT JOIN doctors dr ON dr.doctor_id = pra.doctor
   LEFT JOIN admission ad ON ad.patient_id = pra.patient_id
   LEFT JOIN bed_names bn ON bn.bed_id = ad.bed_id
   LEFT JOIN ward_names wn ON wn.ward_no = bn.ward_no
   LEFT JOIN organization_details od ON pra.org_id = od.org_id
   LEFT JOIN doctors drs ON pra.reference_docto_id = drs.doctor_id
   LEFT JOIN referral rd ON pra.reference_docto_id = rd.referal_no
   LEFT JOIN patient_category_master pcm ON pcm.category_id = pra.patient_category_id
   LEFT JOIN patient_category_master pd_pcm ON pd_pcm.category_id=pd.patient_category_id
   LEFT JOIN tpa_master tpa ON tpa.tpa_id = pra.primary_sponsor_id
   LEFT JOIN tpa_master stpa ON stpa.tpa_id = pra.secondary_sponsor_id
   LEFT JOIN insurance_company_master icm ON icm.insurance_co_id = pra.primary_insurance_co
   LEFT JOIN insurance_company_master sicm ON sicm.insurance_co_id = pra.secondary_insurance_co
   LEFT JOIN insurance_plan_main ipm ON (pra.plan_id = ipm.plan_id)
   LEFT JOIN insurance_category_master icam  ON icam.category_id=ipm.category_id
   LEFT JOIN mrd_diagnosis pmd ON (pmd.visit_id = pra.main_visit_id AND pmd.diag_type = 'P')
   LEFT JOIN patient_insurance_plans ppip ON(ppip.patient_id=pra.patient_id AND ppip.priority = 1)
   LEFT JOIN patient_policy_details ppd ON (ppd.patient_policy_id = ppip.patient_policy_id)
   LEFT JOIN patient_corporate_details pcd ON (pcd.patient_corporate_id = pra.patient_corporate_id)
   LEFT JOIN patient_national_sponsor_details pnd ON (pnd.patient_national_sponsor_id = pra.patient_national_sponsor_id)
   LEFT JOIN patient_corporate_details spcd ON (spcd.patient_corporate_id = pra.secondary_patient_corporate_id)
   LEFT JOIN patient_national_sponsor_details spnd ON (spnd.patient_national_sponsor_id = pra.secondary_patient_national_sponsor_id)
   LEFT JOIN sponsor_type pst ON pst.sponsor_type_id = tpa.sponsor_type_id
   LEFT JOIN sponsor_type sst ON sst.sponsor_type_id = stpa.sponsor_type_id
   LEFT JOIN contact_preferences cpref ON cpref.mr_no = pd.mr_no
   LEFT JOIN other_identification_document_types oidt ON oidt.other_identification_doc_id = pd.other_identification_doc_id
WHERE ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )
;
	
DROP VIEW IF EXISTS patient_prescriptions_details_audit_log_view CASCADE;
CREATE OR REPLACE VIEW patient_prescriptions_details_audit_log_view AS
        SELECT log_id, user_name, mod_time, field_name, old_value, new_value, op_service_pres_id AS pres_id,
            operation, 'Service' AS type
        FROM patient_service_prescriptions_audit_log

    UNION ALL
        SELECT log_id, user_name, mod_time, field_name, old_value, new_value, op_test_pres_id AS pres_id,
            operation, 'Inv.' AS type
        FROM patient_test_prescriptions_audit_log

    UNION ALL
        SELECT log_id, user_name, mod_time, field_name, old_value, new_value, prescription_id AS pres_id,
            operation, 'Doctor' AS type
        FROM patient_consultation_prescriptions_audit_log

    UNION ALL
        SELECT log_id, user_name, mod_time, field_name, old_value, new_value, prescription_id AS pres_id,
            operation, 'Operation' AS type
        FROM patient_operation_prescriptions_audit_log

    UNION ALL
        SELECT popal.log_id, popal.user_name, popal.mod_time, popal.field_name, popal.old_value,
            popal.new_value, popal.prescription_id AS pres_id,
            operation,
            CASE WHEN pp.presc_type = 'NonBillable' THEN 'Others' ELSE pp.presc_type END AS type
        FROM patient_other_prescriptions_audit_log popal
        JOIN patient_prescription pp ON (popal.prescription_id = pp.patient_presc_id)

    UNION ALL
        SELECT log_id, user_name, mod_time, field_name, old_value, new_value, prescription_id AS pres_id,
            operation, 'Medicine' AS type
        FROM patient_other_medicine_prescriptions_audit_log

    UNION ALL
        SELECT log_id, user_name, mod_time, field_name, old_value, new_value, op_medicine_pres_id AS pres_id,
            operation, 'Medicine' AS type
        FROM patient_medicine_prescriptions_audit_log

    UNION ALL
        SELECT log_id, user_name, mod_time, field_name, old_value, new_value, patient_presc_id AS pres_id,
            operation, presc_type AS type
        FROM patient_prescription_audit_log;

DROP VIEW IF EXISTS patient_prescription_audit_log_view CASCADE;
CREATE OR REPLACE VIEW patient_prescription_audit_log_view AS
        SELECT psdal.log_id, psdal.user_name, psdal.mod_time, psdal.operation,
            psdal.field_name, psdal.old_value, psdal.new_value, psdal.mr_no, psdal.patient_id AS patient_id,
            psdal.section_item_id AS consultation_id, 'patient_section_detail_audit_log' ::text
            AS base_table, null::integer AS pres_id, null::text AS type
        FROM patient_section_details_audit_log psdal
        JOIN patient_section_details psd ON (psd.section_detail_id=psdal.section_detail_id)
        WHERE psd.section_id=-7

    UNION ALL
        SELECT ptpal.log_id, ptpal.user_name, ptpal.mod_time, ptpal.operation,
            ptpal.field_name, ptpal.old_value, ptpal.new_value, dc.mr_no, dc.patient_id, ppal.consultation_id,
            'patient_prescriptions_details_audit_log_view' ::text AS base_table,
            ptpal.op_test_pres_id AS pres_id, 'Inv.' AS type
        FROM patient_test_prescriptions_audit_log ptpal
        JOIN patient_prescription_audit_log ppal ON (ptpal.op_test_pres_id = ppal.patient_presc_id
            AND ppal.field_name = 'consultation_id')
        JOIN doctor_consultation dc ON (dc.consultation_id = ppal.consultation_id)
        WHERE ptpal.field_name = 'op_test_pres_id'

    UNION ALL
        SELECT pspal.log_id, pspal.user_name, pspal.mod_time, pspal.operation,
            pspal.field_name, pspal.old_value, pspal.new_value, dc.mr_no, dc.patient_id, ppal.consultation_id,
            'patient_prescriptions_details_audit_log_view' ::text AS base_table,
            pspal.op_service_pres_id AS pres_id, 'Service' AS type
        FROM patient_service_prescriptions_audit_log pspal
        JOIN patient_prescription_audit_log ppal ON (pspal.op_service_pres_id = ppal.patient_presc_id
            AND ppal.field_name = 'consultation_id')
        JOIN doctor_consultation dc ON (dc.consultation_id = ppal.consultation_id)
        WHERE pspal.field_name = 'op_service_pres_id'

    UNION ALL
        SELECT pcpal.log_id, pcpal.user_name, pcpal.mod_time, pcpal.operation,
            pcpal.field_name, pcpal.old_value, pcpal.new_value, dc.mr_no, dc.patient_id, ppal.consultation_id,
            'patient_prescriptions_details_audit_log_view' ::text AS base_table,
            pcpal.prescription_id AS pres_id, 'Doctor' AS type
        FROM patient_consultation_prescriptions_audit_log pcpal
        JOIN patient_prescription_audit_log ppal ON (pcpal.prescription_id = ppal.patient_presc_id
            AND ppal.field_name = 'consultation_id')
        JOIN doctor_consultation dc ON (dc.consultation_id = ppal.consultation_id)
        WHERE pcpal.field_name = 'prescription_id'

    UNION ALL
        SELECT popal.log_id, popal.user_name, popal.mod_time, popal.operation,
            popal.field_name, popal.old_value, popal.new_value, dc.mr_no, dc.patient_id, ppal.consultation_id,
            'patient_prescriptions_details_audit_log_view' ::text AS base_table,
            popal.prescription_id AS pres_id, 'Operation' AS type
        FROM patient_operation_prescriptions_audit_log popal
        JOIN patient_prescription_audit_log ppal ON (popal.prescription_id = ppal.patient_presc_id
            AND ppal.field_name = 'consultation_id')
        JOIN doctor_consultation dc ON (dc.consultation_id = ppal.consultation_id)
        WHERE popal.field_name = 'prescription_id'

    UNION ALL
        SELECT popal.log_id, popal.user_name, popal.mod_time, popal.operation,
            popal.field_name, popal.old_value, popal.new_value, dc.mr_no, dc.patient_id, ppal.consultation_id,
            'patient_prescriptions_details_audit_log_view' ::text AS base_table,
            popal.prescription_id AS pres_id,
            CASE WHEN pop.non_hosp_medicine THEN 'Medicine' ELSE 'NonHospital' END AS type
        FROM patient_other_prescriptions_audit_log popal
        JOIN (SELECT prescription_id from patient_other_prescriptions_audit_log WHERE field_name = 'is_discharge_medication' AND new_value = 'false') AS foo 
        ON (popal.prescription_id = foo.prescription_id)
        JOIN patient_prescription_audit_log ppal ON (popal.prescription_id = ppal.patient_presc_id
            AND ppal.field_name = 'consultation_id')
        JOIN doctor_consultation dc ON (dc.consultation_id = ppal.consultation_id)
        JOIN patient_other_prescriptions pop ON (popal.prescription_id = pop.prescription_id)
        WHERE popal.field_name = 'prescription_id'

    UNION ALL
        SELECT pompal.log_id, pompal.user_name, pompal.mod_time, pompal.operation,
            pompal.field_name, pompal.old_value, pompal.new_value, dc.mr_no, dc.patient_id, ppal.consultation_id,
            'patient_prescriptions_details_audit_log_view' ::text AS base_table,
            pompal.prescription_id AS pres_id, 'Medicine' AS type
        FROM patient_other_medicine_prescriptions_audit_log pompal
        JOIN (SELECT prescription_id from patient_other_medicine_prescriptions_audit_log WHERE field_name = 'is_discharge_medication' AND new_value = 'false') AS foo 
        ON (pompal.prescription_id = foo.prescription_id)
        JOIN patient_prescription_audit_log ppal ON (pompal.prescription_id = ppal.patient_presc_id
            AND ppal.field_name = 'consultation_id')
        JOIN doctor_consultation dc ON (dc.consultation_id = ppal.consultation_id)
        WHERE pompal.field_name = 'prescription_id'

    UNION ALL
        SELECT pmpal.log_id, pmpal.user_name, pmpal.mod_time, pmpal.operation,
            pmpal.field_name, pmpal.old_value, pmpal.new_value, dc.mr_no, dc.patient_id, ppal.consultation_id,
            'patient_prescriptions_details_audit_log_view' ::text AS base_table,
            pmpal.op_medicine_pres_id AS pres_id, 'Medicine' AS type
        FROM patient_medicine_prescriptions_audit_log pmpal
        JOIN (SELECT op_medicine_pres_id from patient_medicine_prescriptions_audit_log WHERE field_name = 'is_discharge_medication' AND new_value = 'false') AS foo 
        ON (pmpal.op_medicine_pres_id = foo.op_medicine_pres_id)
        JOIN patient_prescription_audit_log ppal ON (pmpal.op_medicine_pres_id = ppal.patient_presc_id
            AND ppal.field_name = 'consultation_id')
        JOIN doctor_consultation dc ON (dc.consultation_id = ppal.consultation_id)
        WHERE pmpal.field_name = 'op_medicine_pres_id';

DROP VIEW IF EXISTS patient_discharge_medication_cons_audit_log_view CASCADE;
CREATE OR REPLACE VIEW patient_discharge_medication_cons_audit_log_view AS
        SELECT psdal.log_id, psdal.user_name, psdal.mod_time, psdal.operation,
            psdal.field_name, psdal.old_value, psdal.new_value, psdal.mr_no, psdal.patient_id AS patient_id,
            psdal.section_item_id AS consultation_id, 'patient_section_detail_audit_log' ::text
            AS base_table, null::integer AS pres_id, null::text AS type
        FROM patient_section_details_audit_log psdal
        JOIN patient_section_details psd ON (psd.section_detail_id=psdal.section_detail_id)
        WHERE psd.section_id=-22

    UNION ALL
        SELECT popal.log_id, popal.user_name, popal.mod_time, popal.operation,
            popal.field_name, popal.old_value, popal.new_value, dc.mr_no, dc.patient_id, ppal.consultation_id,
            'patient_prescriptions_details_audit_log_view' ::text AS base_table,
            popal.prescription_id AS pres_id,
            CASE WHEN pop.non_hosp_medicine THEN 'Medicine' ELSE 'NonHospital' END AS type
        FROM patient_other_prescriptions_audit_log popal
        JOIN (SELECT prescription_id from patient_other_prescriptions_audit_log WHERE field_name = 'is_discharge_medication' AND new_value = 'true') AS foo 
        ON (popal.prescription_id = foo.prescription_id)
        JOIN patient_prescription_audit_log ppal ON (popal.prescription_id = ppal.patient_presc_id
            AND ppal.field_name = 'consultation_id')
        JOIN doctor_consultation dc ON (dc.consultation_id = ppal.consultation_id)
        JOIN patient_other_prescriptions pop ON (popal.prescription_id = pop.prescription_id)
        WHERE popal.field_name = 'prescription_id'

    UNION ALL
        SELECT pompal.log_id, pompal.user_name, pompal.mod_time, pompal.operation,
            pompal.field_name, pompal.old_value, pompal.new_value, dc.mr_no, dc.patient_id, ppal.consultation_id,
            'patient_prescriptions_details_audit_log_view' ::text AS base_table,
            pompal.prescription_id AS pres_id, 'Medicine' AS type
        FROM patient_other_medicine_prescriptions_audit_log pompal
        JOIN (SELECT prescription_id from patient_other_medicine_prescriptions_audit_log WHERE field_name = 'is_discharge_medication' AND new_value = 'true') AS foo 
        ON (pompal.prescription_id = foo.prescription_id)
        JOIN patient_prescription_audit_log ppal ON (pompal.prescription_id = ppal.patient_presc_id
            AND ppal.field_name = 'consultation_id')
        JOIN doctor_consultation dc ON (dc.consultation_id = ppal.consultation_id)
        WHERE pompal.field_name = 'prescription_id'

    UNION ALL
        SELECT pmpal.log_id, pmpal.user_name, pmpal.mod_time, pmpal.operation,
            pmpal.field_name, pmpal.old_value, pmpal.new_value, dc.mr_no, dc.patient_id, ppal.consultation_id,
            'patient_prescriptions_details_audit_log_view' ::text AS base_table,
            pmpal.op_medicine_pres_id AS pres_id, 'Medicine' AS type
        FROM patient_medicine_prescriptions_audit_log pmpal
        JOIN (SELECT op_medicine_pres_id from patient_medicine_prescriptions_audit_log WHERE field_name = 'is_discharge_medication' AND new_value = 'true') AS foo 
        ON (pmpal.op_medicine_pres_id = foo.op_medicine_pres_id)
        JOIN patient_prescription_audit_log ppal ON (pmpal.op_medicine_pres_id = ppal.patient_presc_id
            AND ppal.field_name = 'consultation_id')
        JOIN doctor_consultation dc ON (dc.consultation_id = ppal.consultation_id)
        WHERE pmpal.field_name = 'op_medicine_pres_id';

DROP FUNCTION IF EXISTS patient_prescription_delete_trigger_func() CASCADE;
CREATE OR REPLACE FUNCTION patient_prescription_delete_trigger_func() RETURNS TRIGGER AS $BODY$
DECLARE

BEGIN
    insert into patient_prescription_audit_log(consultation_id, patient_presc_id, user_name, operation,
        field_name, old_value, new_value, presc_type)
    values(OLD.consultation_id, OLD.patient_presc_id, OLD.username, 'DELETE', 'patient_presc_id',
        OLD.patient_presc_id, 'Record Deleted', OLD.presc_type);

    RETURN OLD;
END;
$BODY$ LANGUAGE plpgsql;

DROP VIEW IF EXISTS eclaim_credential CASCADE;
CREATE OR REPLACE VIEW eclaim_credential AS
select center_id, health_authority, shafafiya_preauth_user_id AS eclaim_user_id, shafafiya_preauth_password AS eclaim_password,eclaim_active
from hospital_center_master where health_authority='HAAD'
UNION ALL
select center_id, health_authority, dhpo_facility_user_id AS eclaim_user_id, dhpo_facility_password AS eclaim_password,eclaim_active
from hospital_center_master where health_authority='DHA';

DROP VIEW IF EXISTS ceo_dashbaord_data_view CASCADE;
  
CREATE OR REPLACE VIEW ceo_dashbaord_data_view AS
SELECT to_char(foo.date,'YYYY-MM-DD') AS date,
       sum(foo.bed_occupancy)::integer AS bed_occupancy,
       sum(foo.surgery)::integer AS surgery,
       sum(foo.discharge)::integer AS discharge,
       sum(foo.op_visit)::integer AS op_visit,
       sum(foo.ip_visit)::integer AS ip_visit,
       sum(foo.radiology_count)::integer AS radiology_count,
       sum(foo.lab_order_count)::integer AS lab_order_count,
       sum(foo.revenue) AS revenue,
       to_char(now() at time zone 'utc', 'YYYY-MM-DD"T"HH24:MI:SS"Z"') AS timestamp
FROM
  -- Number of beds occupied
  ( SELECT ref_date AS date,
       closing_count AS bed_occupancy,
       0::integer AS surgery, 0::integer AS discharge, 0::integer AS op_visit, 0::integer AS radiology_count,
        0::integer AS lab_order_count, 
       0::numeric AS revenue, 0::integer AS ip_visit
   FROM total_bed_occupancy_dashboard
  
   -- Number of surgeries
   UNION ALL SELECT date(prescribed_date) AS date,
        0::numeric AS bed_occupancy, count(*)::integer AS surgery, 0::integer AS discharge,
        0::integer AS op_visit, 0::integer AS radiology_count, 
        0::integer AS lab_order_count, 
        0::numeric AS revenue, 0::integer AS ip_visit
   FROM bed_operation_schedule WHERE status !='X' GROUP BY date(prescribed_date)
   
   -- Number of Discharges
   UNION ALL SELECT date(discharge_date) AS date,
        0::numeric AS bed_occupancy, 0::integer AS surgery, count(patient_id)::integer AS discharge,
        0::integer AS op_visit, 0::integer AS radiology_count, 
        0::integer AS lab_order_count,  0::numeric AS revenue, 0::integer AS ip_visit
   FROM patient_registration WHERE visit_type='i' AND discharge_flag='D' GROUP BY date(discharge_date)

   -- Count of Radiology orders
   UNION ALL SELECT date(posted_date) AS date,
        0::numeric AS bed_occupancy,0::integer AS surgery,0::integer AS discharge,0::integer AS op_visit,
        count(*)::integer AS radiology_count,0::integer AS lab_order_count,
        0::numeric AS revenue,0::integer AS ip_visit
   FROM bill_charge bc JOIN bill b ON (b.bill_no = bc.bill_no) 
   WHERE bc.charge_head='RTDIA' AND bc.status!='X' AND (bc.order_number IS NOT NULL OR b.visit_type = 't' ) GROUP BY date(posted_date)
  
   -- Count of Laboratory orders
   UNION ALL SELECT date(posted_date) AS date,
        0::numeric AS bed_occupancy,0::integer AS surgery,0::integer AS discharge,0::integer AS op_visit,
        0::integer AS radiology_count,
        count(*)::integer AS lab_order_count,0::numeric AS revenue,
        0::integer AS ip_visit
   FROM bill_charge bc JOIN bill b ON (b.bill_no = bc.bill_no) 
   WHERE bc.charge_head='LTDIA' AND bc.status!='X' AND (bc.order_number IS NOT NULL OR b.visit_type = 't' ) GROUP BY date(posted_date)

   -- Total Revenue
   UNION ALL SELECT date(mod_time) AS date,
        0::numeric AS bed_occupancy,0::integer AS surgery,0::integer AS discharge,0::integer AS op_visit,
        0::integer AS radiology_count,0::integer AS lab_order_count,
        sum(amount)::numeric AS revenue,
        0::integer AS ip_visit
   FROM bill_charge_adjustment  GROUP BY date(mod_time)
  
   -- Number of OP Visits
   UNION ALL SELECT reg_date AS date,
        0::numeric AS bed_occupancy,0::integer AS surgery,0::integer AS discharge,count(*)::integer AS op_visit,
        0::integer AS radiology_count,0::integer AS lab_order_count,
        0::numeric AS revenue,0::integer AS ip_visit
   FROM patient_registration WHERE visit_type='o' GROUP BY reg_date
  
   -- Number of IP Visits
   UNION ALL SELECT reg_date AS date,
        0::numeric AS bed_occupancy,0::integer AS surgery,0::integer AS discharge,0::integer AS op_visit,0::integer AS radiology_count,0::integer AS lab_order_count,
        0::numeric AS revenue,count(*)::integer AS ip_visit
   FROM patient_registration WHERE visit_type='i' GROUP BY reg_date
   ) AS foo

GROUP BY date ;

	
DROP FUNCTION IF EXISTS bill_claim_sponsor_adjustment_trigger_on_bill_claim_update() CASCADE;
CREATE OR REPLACE FUNCTION bill_claim_sponsor_adjustment_trigger_on_bill_claim_update() RETURNS trigger AS $BODY$
DECLARE
	seq_no varchar;
	patCategoryId int;
	priSponsorId varchar;
	secSponsorId varchar;
	
BEGIN
	patCategoryId := null; 
	priSponsorId := null; 
	secSponsorId := null; 

	SELECT pr.patient_category_id INTO patCategoryId
		FROM bill b
		JOIN patient_registration pr ON(pr.patient_id = b.visit_id)
	WHERE b.bill_no=OLD.bill_no;

	SELECT bcl.sponsor_id INTO priSponsorId
		FROM bill b
		JOIN bill_claim bcl ON(b.bill_no = bcl.bill_no)
	WHERE bcl.priority = 1 AND b.bill_no = OLD.bill_no ;

	SELECT bcl.sponsor_id INTO secSponsorId
		FROM bill b
		JOIN bill_claim bcl ON(b.bill_no = bcl.bill_no)
	WHERE bcl.priority = 2 AND b.bill_no = OLD.bill_no ;

	
	IF (TG_OP = 'DELETE') THEN
		SELECT generate_id('BILL_CHARGE_ADJ_DEFAULT') INTO seq_no;
		INSERT INTO bill_charge_adjustment (bill_charge_adjustment_id, charge_id, bill_no,act_rate,amount,
				discount,paid_amount,posted_date,status,mod_time,orig_rate,doctor_amount,
				insurance_claim_amount,referal_amount,out_house_amount,prescribing_dr_amount,overall_discount_amt,
				dr_discount_amt, pres_dr_discount_amt,ref_discount_amt,hosp_discount_amt,claim_recd_total,
				return_insurance_claim_amt,return_amt,amount_included,
				orig_insurance_claim_amount,patient_category_id,primary_sponsor_id,
				secondary_sponsor_id,sponsor_switch,tax_amt,sponsor_tax_amt)
		VALUES (seq_no, OLD.charge_id, OLD.bill_no, 0,0,
				0,0,current_timestamp,'A',current_timestamp,
				0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0,
				patCategoryId,priSponsorId,secSponsorId,true,0,0);
	ELSEIF (TG_OP = 'INSERT') THEN
		SELECT generate_id('BILL_CHARGE_ADJ_DEFAULT') INTO seq_no;
		INSERT INTO bill_charge_adjustment (bill_charge_adjustment_id, charge_id, bill_no,act_rate,amount,
				discount,paid_amount,posted_date,status,mod_time,orig_rate,doctor_amount,
				insurance_claim_amount,referal_amount,out_house_amount,prescribing_dr_amount,overall_discount_amt,
				dr_discount_amt, pres_dr_discount_amt,ref_discount_amt,hosp_discount_amt,claim_recd_total,
				return_insurance_claim_amt,return_amt,amount_included,
				orig_insurance_claim_amount,patient_category_id,primary_sponsor_id,
				secondary_sponsor_id,sponsor_switch,tax_amt,sponsor_tax_amt)
		VALUES (seq_no, NEW.charge_id, NEW.bill_no, 0,0,
				0,0,current_timestamp,'A',current_timestamp,
				0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0,
				patCategoryId,priSponsorId,secSponsorId,true,0,0);
				
	ELSEIF (OLD.sponsor_id != NEW.sponsor_id) THEN
	
		SELECT generate_id('BILL_CHARGE_ADJ_DEFAULT') INTO seq_no;
		INSERT INTO bill_charge_adjustment (bill_charge_adjustment_id, charge_id, bill_no,act_rate,amount,
				discount,paid_amount,posted_date,status,mod_time,orig_rate,doctor_amount,
				insurance_claim_amount,referal_amount,out_house_amount,prescribing_dr_amount,overall_discount_amt,
				dr_discount_amt, pres_dr_discount_amt,ref_discount_amt,hosp_discount_amt,claim_recd_total,
				return_insurance_claim_amt,return_amt,amount_included,
				orig_insurance_claim_amount,patient_category_id,primary_sponsor_id,
				secondary_sponsor_id,sponsor_switch,tax_amt,sponsor_tax_amt)
		VALUES (seq_no, NEW.charge_id, NEW.bill_no, 0,0,
				0,0,current_timestamp,'A',current_timestamp,
				0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0,
				patCategoryId,priSponsorId,secSponsorId,true,0,0);

	END IF;
	RETURN NULL;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS bill_claim_sponsor_adjustment_trigger_on_bill_claim_update ON bill_charge_claim CASCADE;
CREATE CONSTRAINT TRIGGER bill_claim_sponsor_adjustment_trigger_on_bill_claim_update
	AFTER UPDATE OR DELETE
	ON bill_charge_claim
	DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
	EXECUTE PROCEDURE bill_claim_sponsor_adjustment_trigger_on_bill_claim_update();
	
DROP VIEW IF EXISTS patient_orders_visit_report_view CASCADE;
CREATE VIEW patient_orders_visit_report_view AS
select count(patient_id) AS pending_tests_services_count, patient_id from(SELECT  sp.patient_id
FROM services_prescribed sp
 JOIN services s ON (s.service_id = sp.service_id)
WHERE sp.conducted IN ('N', 'P')
UNION ALL
SELECT tp.pat_id AS patient_id
FROM tests_prescribed tp
JOIN diagnostics d ON d.test_id = tp.test_id
JOIN diagnostics_departments ddept ON ddept.ddept_id = d.ddept_id
WHERE tp.conducted IN ('N', 'R', 'RP', 'NRN'))as foo group by foo.patient_id;
	

DROP VIEW IF EXISTS pending_operation_view CASCADE;
CREATE VIEW pending_operation_view AS
SELECT COUNT(bos.patient_id) AS pending_operation_count, bos.patient_id
    FROM bed_operation_schedule bos
    JOIN operation_procedures op ON bos.prescribed_id = op.prescribed_id
    WHERE bos.status = 'N' AND op.oper_priority = 'P' and bos.patient_id='IP000819'
    GROUP BY bos.patient_id ;

DROP VIEW IF EXISTS pending_ward_activities_view CASCADE;
CREATE VIEW pending_ward_activities_view AS
SELECT count(patient_id) AS pending_ward_activity_count, patient_id
  FROM patient_activities
  WHERE activity_status = 'P'
  GROUP BY patient_id; 

DROP VIEW IF EXISTS pending_patient_indent_view CASCADE;
CREATE VIEW pending_patient_indent_view AS
SELECT count(visit_id) AS pending_patient_indent_count, visit_id 
   FROM store_patient_indent_main
   WHERE status = 'O'
   GROUP BY visit_id;   
	
DROP VIEW IF EXISTS pbm_medicine_prescription_view CASCADE;
CREATE OR REPLACE VIEW pbm_medicine_prescription_view AS
SELECT pbm_presc_id, consultation_id, visit_id, sum(amount)+sum(discount) AS total_amount,sum(discount) AS total_discount, 
sum(amount) AS total_gross_amount,sum(amount)-sum(claim_net_amount) AS total_patient_amount,sum(claim_net_amount) AS total_claim_net_amount,
sum(claim_net_approved_amount) AS total_approved_net_amount,
avg(CASE WHEN issued IN ('Y', 'C') THEN 3 WHEN issued = 'P' THEN 2 ELSE 1 END) AS sts  
FROM pbm_medicine_prescriptions  
GROUP by pbm_presc_id, consultation_id, visit_id 
;

--- Trigger to insert Bill Charge tax Adjustments ON bill_charge_tax insert or update---
DROP FUNCTION IF EXISTS bill_charge_details_adjustment_tax_trigger() CASCADE;
CREATE OR REPLACE FUNCTION bill_charge_details_adjustment_tax_trigger() RETURNS trigger AS $BODY$
DECLARE
	seq_no varchar;
BEGIN
	IF (TG_OP = 'INSERT') THEN
		SELECT generate_id('BILL_CHARGE_DETAILS_ADJ_DEFAULT') INTO seq_no;
		INSERT INTO bill_charge_details_adjustment (
				charge_adjustment_detail_id,charge_id,claim_id,txn_id,txn_type,mod_time,tax_sub_group_id,tax_rate,tax_amt,sponsor_tax_amount)
		VALUES (seq_no, NEW.charge_id,null,txid_current(),'I',now(),NEW.tax_sub_group_id,NEW.tax_rate,NEW.tax_amount,0.00 );
	END IF;
	
	IF (TG_OP = 'UPDATE') THEN
		--IF (NEW.tax_amount != OLD.tax_amount OR NEW.tax_rate != OLD.tax_rate) THEN
			SELECT generate_id('BILL_CHARGE_DETAILS_ADJ_DEFAULT') INTO seq_no;
			INSERT INTO bill_charge_details_adjustment (
					charge_adjustment_detail_id,charge_id,claim_id,txn_id,txn_type,mod_time,tax_sub_group_id,tax_rate,tax_amt,sponsor_tax_amount,
					old_claim_id,old_tax_sub_group_id,old_tax_rate,old_tax_amt,old_sponsor_tax_amount)
			VALUES (seq_no, NEW.charge_id,null,txid_current(),'I',now(),NEW.tax_sub_group_id,NEW.tax_rate,NEW.tax_amount,0.00,
					null,OLD.tax_sub_group_id,OLD.tax_rate,OLD.tax_amount,0.00);
		--END IF;
	END IF;
	IF (TG_OP = 'DELETE') THEN
		SELECT generate_id('BILL_CHARGE_DETAILS_ADJ_DEFAULT') INTO seq_no;
		INSERT INTO bill_charge_details_adjustment (
				charge_adjustment_detail_id,charge_id,claim_id,txn_id,txn_type,mod_time,tax_sub_group_id,tax_rate,tax_amt,sponsor_tax_amount,
				old_claim_id,old_tax_sub_group_id,old_tax_rate,old_tax_amt,old_sponsor_tax_amount)
		VALUES (seq_no, OLD.charge_id,null,txid_current(),'I',now(),OLD.tax_sub_group_id,0.00,0.00,0.00,
				null,OLD.tax_sub_group_id,OLD.tax_rate,OLD.tax_amount,0.00);
	END IF;
	RETURN NULL;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS bill_charge_details_adjustment_tax_trigger ON bill_charge_tax CASCADE;
CREATE CONSTRAINT TRIGGER bill_charge_details_adjustment_tax_trigger
	AFTER INSERT OR UPDATE OR DELETE ON bill_charge_tax
	DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
	EXECUTE PROCEDURE bill_charge_details_adjustment_tax_trigger();


--- Trigger to insert Bill Charge tax Adjustments ON bill_charge_claim_tax insert or update---
DROP FUNCTION IF EXISTS bill_charge_details_claim_tax_adjustment_trigger() CASCADE;
CREATE OR REPLACE FUNCTION bill_charge_details_claim_tax_adjustment_trigger() RETURNS trigger AS $BODY$
DECLARE
	seq_no varchar;
	b_tid bigint;
	oldrec RECORD;
	
BEGIN
	IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN
		SELECT txn_id from bill_charge_details_adjustment
		WHERE charge_id = NEW.charge_id 
		AND tax_sub_group_id = NEW.tax_sub_group_id AND txn_type = 'I'
		ORDER BY charge_adjustment_detail_id DESC LIMIT 1 INTO b_tid;
	END IF;
	
	IF (TG_OP = 'INSERT') THEN
		SELECT bcda.tax_sub_group_id, bcda.old_tax_sub_group_id, bcct.claim_id, bcct.tax_rate, bcct.sponsor_tax_amount, bcct.sponsor_id 
		FROM bill_charge_details_adjustment bcda 
		JOIN bill_charge_claim_tax bcct 
		ON (bcda.charge_id = bcct.charge_id AND bcda.txn_id = b_tid 
			AND bcda.old_tax_sub_group_id = bcct.tax_sub_group_id
			AND bcda.tax_sub_group_id = NEW.tax_sub_group_id)
		WHERE bcda.old_tax_sub_group_id != bcda.tax_sub_group_id INTO oldrec;

		SELECT generate_id('BILL_CHARGE_DETAILS_ADJ_DEFAULT') INTO seq_no;
		IF oldrec.tax_sub_group_id IS NULL THEN
			INSERT INTO bill_charge_details_adjustment (
					charge_adjustment_detail_id,charge_id,claim_id,txn_id,txn_type,mod_time,tax_sub_group_id,tax_rate,tax_amt,sponsor_tax_amount,sponsor_id)
			VALUES (seq_no, NEW.charge_id,NEW.claim_id,COALESCE(b_tid, txid_current()),'S',now(),NEW.tax_sub_group_id,NEW.tax_rate,0.00,NEW.sponsor_tax_amount,NEW.sponsor_id );
		ELSE
			INSERT INTO bill_charge_details_adjustment (
					charge_adjustment_detail_id,charge_id,claim_id,txn_id,txn_type,mod_time,tax_sub_group_id,tax_rate,tax_amt,sponsor_tax_amount,
					old_claim_id, old_tax_sub_group_id, old_tax_rate, old_tax_amt, old_sponsor_tax_amount, sponsor_id, old_sponsor_id)
			VALUES (seq_no, NEW.charge_id,NEW.claim_id,COALESCE(b_tid, txid_current()),'S',now(),NEW.tax_sub_group_id,NEW.tax_rate,0.00,NEW.sponsor_tax_amount,
					oldrec.claim_id, oldrec.old_tax_sub_group_id, oldrec.tax_rate, 0.00, oldrec.sponsor_tax_amount,NEW.sponsor_id,oldrec.sponsor_id );
		END IF;		
		
	END IF;

	IF (TG_OP = 'UPDATE') THEN
		--IF (NEW.sponsor_tax_amount != OLD.sponsor_tax_amount OR NEW.tax_rate != OLD.tax_rate) THEN
			SELECT generate_id('BILL_CHARGE_DETAILS_ADJ_DEFAULT') INTO seq_no;
			INSERT INTO bill_charge_details_adjustment (
					charge_adjustment_detail_id,charge_id,claim_id,txn_id,txn_type,mod_time,tax_sub_group_id,tax_rate,tax_amt,sponsor_tax_amount,
					old_claim_id,old_tax_sub_group_id,old_tax_rate,old_tax_amt,old_sponsor_tax_amount,sponsor_id, old_sponsor_id)
			VALUES (seq_no, NEW.charge_id,NEW.claim_id,COALESCE(b_tid, txid_current()),'S',now(),NEW.tax_sub_group_id,NEW.tax_rate,0.00,NEW.sponsor_tax_amount,
					OLD.claim_id,OLD.tax_sub_group_id,OLD.tax_rate,0.00,OLD.sponsor_tax_amount,NEW.sponsor_id,OLD.sponsor_id);
		--END IF;
	END IF;
	
	IF (TG_OP = 'DELETE') THEN
		--IF (NEW.sponsor_tax_amount != OLD.sponsor_tax_amount OR NEW.tax_rate != OLD.tax_rate) THEN
			SELECT generate_id('BILL_CHARGE_DETAILS_ADJ_DEFAULT') INTO seq_no;
			INSERT INTO bill_charge_details_adjustment (
					charge_adjustment_detail_id,charge_id,claim_id,txn_id,txn_type,mod_time,tax_sub_group_id,tax_rate,tax_amt,sponsor_tax_amount,
					old_claim_id,old_tax_sub_group_id,old_tax_rate,old_tax_amt,old_sponsor_tax_amount,sponsor_id, old_sponsor_id)
			VALUES (seq_no, OLD.charge_id,NULL,COALESCE(b_tid, txid_current()),'S',now(),0,0.00,0.00,0.00,
					OLD.claim_id,OLD.tax_sub_group_id,OLD.tax_rate,0.00,OLD.sponsor_tax_amount,NULL,OLD.sponsor_id);
		--END IF;
	END IF;
	
	RETURN NULL;
END;
$BODY$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS bill_charge_details_claim_tax_adjustment_trigger ON bill_charge_claim_tax CASCADE;
CREATE CONSTRAINT TRIGGER bill_charge_details_claim_tax_adjustment_trigger
	AFTER INSERT OR UPDATE OR DELETE ON bill_charge_claim_tax
	DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
	EXECUTE PROCEDURE bill_charge_details_claim_tax_adjustment_trigger();

DROP FUNCTION IF EXISTS doctor_orderable_item_procedure() CASCADE;
CREATE OR REPLACE FUNCTION doctor_orderable_item_procedure()
   RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO orderable_item(entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, visit_type, direct_billing, service_group_id, status) 
            SELECT DISTINCT d.doctor_id AS entity_id, 'Doctor' AS entity, lower(d.doctor_name) AS item_name, 
            'mod_basic' AS module_id, 'Y' AS orderable, d.ot_doctor_flag AS operation_applicable, 
            'N' AS package_applicable, 'Y' AS is_multi_visit_package, d.service_sub_group_id, '*' AS visit_type, 
            (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Doctor') AS direct_billing, 
            ssg.service_group_id AS service_group_id, d.status AS status 
            FROM doctors d 
            JOIN service_sub_groups ssg using(service_sub_group_id) 
            WHERE d.doctor_id = NEW.doctor_id;
    ELSIF (TG_OP = 'UPDATE') THEN
        UPDATE orderable_item SET item_name = lower(NEW.doctor_name), operation_applicable = NEW.ot_doctor_flag, status = NEW.status
        WHERE entity_id = NEW.doctor_id;
    END IF;

    RETURN NEW;
END;
$$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION doctor_center_master_procedure()
    RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO mapping_center_id (orderable_item_id, center_id, status)
            SELECT DISTINCT oi.orderable_item_id, NEW.center_id, NEW.status 
            FROM orderable_item oi 
            WHERE oi.entity = 'Doctor' AND oi.entity_id = NEW.doctor_id; 
        RETURN NEW;
    ELSIF (TG_OP = 'UPDATE') THEN
        UPDATE mapping_center_id mci SET status = NEW.status
        FROM orderable_item oi
        WHERE oi.entity_id = NEW.doctor_id AND center_id = NEW.center_id AND oi.orderable_item_id = mci.orderable_item_id;
        RETURN NEW;
    ELSIF (TG_OP = 'DELETE') THEN
        DELETE FROM mapping_center_id mci
        USING orderable_item oi 
        WHERE oi.entity_id = OLD.doctor_id AND mci.center_id = OLD.center_id AND oi.orderable_item_id = mci.orderable_item_id;
        RETURN OLD;
    END IF;
END;
$$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS doctor_orderable_item_trigger ON doctors CASCADE;
CREATE TRIGGER doctor_orderable_item_trigger
    AFTER INSERT OR UPDATE ON doctors
    FOR EACH ROW
    EXECUTE PROCEDURE doctor_orderable_item_procedure();

DROP TRIGGER IF EXISTS doctors_center_mapping_trigger ON doctor_center_master CASCADE;
CREATE TRIGGER doctors_center_mapping_trigger
    AFTER INSERT OR UPDATE OR DELETE ON doctor_center_master
    FOR EACH ROW
    EXECUTE PROCEDURE doctor_center_master_procedure();

CREATE OR REPLACE FUNCTION other_charges_orderable_item_procedure()
   RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO orderable_item(entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, item_codes, status) 
            SELECT DISTINCT NEW.charge_name AS entity_id, 'Other Charge' AS entity, lower(NEW.charge_name) AS item_name, 
            'mod_basic' AS module_id, 'Y' AS orderable, 'Y' AS operation_applicable, 
            'Y' AS package_applicable, 'Y' AS is_multi_visit_package, NEW.service_sub_group_id AS service_sub_group_id, NEW.insurance_category_id, '*' AS visit_type, 
            (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Other Charge') AS direct_billing, 
            ssg.service_group_id AS service_group_id, lower(NEW.othercharge_code) AS item_codes, NEW.status AS status  
            FROM common_charges_master  ccm
            JOIN service_sub_groups ssg using(service_sub_group_id)
            WHERE ccm.charge_name = NEW.charge_name;
    ELSIF (TG_OP = 'UPDATE') THEN
        UPDATE orderable_item SET item_name = lower(NEW.charge_name), status = NEW.status, item_codes = lower(NEW.othercharge_code), insurance_category_id = NEW.insurance_category_id, service_sub_group_id = NEW.service_sub_group_id, service_group_id = ssg.service_group_id
        FROM service_sub_groups ssg
        WHERE ssg.service_sub_group_id = NEW.service_sub_group_id AND entity_id = NEW.charge_name;
    END IF;

    RETURN NEW;
END;
$$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS other_charge_orderable_item_trigger ON common_charges_master CASCADE;
CREATE TRIGGER other_charge_orderable_item_trigger
    AFTER INSERT OR UPDATE ON common_charges_master
    FOR EACH ROW
    EXECUTE PROCEDURE other_charges_orderable_item_procedure();


CREATE OR REPLACE FUNCTION meal_orderable_item_procedure()
   RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, status) 
        SELECT DISTINCT NEW.diet_id AS entity_id, 'Meal' AS entity, lower(NEW.meal_name) AS item_name, 
        'mod_basic' AS module_id, 'Y' AS orderable, 'Y' AS operation_applicable, 
        'Y' AS package_applicable, 'N' AS is_multi_visit_package, NEW.service_sub_group_id, NEW.insurance_category_id, 'i' AS visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Meal') AS direct_billing,
        ssg.service_group_id AS service_group_id, NEW.status
        FROM diet_master dm
        JOIN service_sub_groups ssg using(service_sub_group_id)
        WHERE dm.diet_id = NEW.diet_id;
     ELSIF (TG_OP = 'UPDATE') THEN
        UPDATE orderable_item SET item_name = lower(NEW.meal_name), status = NEW.status, insurance_category_id = NEW.insurance_category_id, service_sub_group_id = NEW.service_sub_group_id, service_group_id = ssg.service_group_id
        FROM service_sub_groups ssg
        WHERE ssg.service_sub_group_id = NEW.service_sub_group_id
        AND entity_id = OLD.diet_id::text AND entity = 'Meal';
    END IF;

    RETURN NEW;
END;
$$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS meal_orderable_item_trigger ON diet_master CASCADE; 
CREATE TRIGGER meal_orderable_item_trigger
    AFTER INSERT OR UPDATE ON diet_master
    FOR EACH ROW
    EXECUTE PROCEDURE meal_orderable_item_procedure();


CREATE OR REPLACE FUNCTION equipment_orderable_item_procedure()
   RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, item_codes, status) 
        SELECT DISTINCT NEW.eq_id AS entity_id, 'Equipment' AS entity, lower(NEW.equipment_name) AS item_name, 
        'mod_basic' AS module_id, 'Y' AS orderable, 'Y' AS operation_applicable, 
        'Y' AS package_applicable, 'N' AS is_multi_visit_package, NEW.service_sub_group_id, NEW.insurance_category_id, '*' AS visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Equipment') AS direct_billing,
        ssg.service_group_id AS service_group_id, lower(NEW.equipment_code) AS item_codes, NEW.status AS status  
        FROM equipment_master em
        JOIN service_sub_groups ssg using(service_sub_group_id)
        WHERE em.eq_id = NEW.eq_id;
     ELSIF (TG_OP = 'UPDATE') THEN
        UPDATE orderable_item SET item_name = lower(NEW.equipment_name), status = NEW.status, insurance_category_id = NEW.insurance_category_id, service_sub_group_id = NEW.service_sub_group_id, service_group_id = ssg.service_group_id, item_codes = lower(NEW.equipment_code) 
        FROM service_sub_groups ssg
        WHERE ssg.service_sub_group_id = NEW.service_sub_group_id AND entity_id = NEW.eq_id;
    END IF;

    RETURN NEW;
END;
$$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS equipment_orderable_item_trigger ON equipment_master CASCADE;
CREATE TRIGGER equipment_orderable_item_trigger
    AFTER INSERT OR UPDATE ON equipment_master
    FOR EACH ROW
    EXECUTE PROCEDURE equipment_orderable_item_procedure();


CREATE OR REPLACE FUNCTION bed_orderable_item_procedure()
   RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT' AND NEW.is_icu = 'N') THEN
       INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, status) 
        SELECT DISTINCT NEW.bed_type_name AS entity_id, 'Bed' AS entity, lower(NEW.bed_type_name) AS item_name, 
        'mod_adt' AS module_id, 'I' AS orderable, 'N' AS operation_applicable, 
        'Y' AS package_applicable, 'N' AS is_multi_visit_package, 
        cc.service_sub_group_id, NEW.insurance_category_id, 'i' AS visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Bed') AS direct_billing,
        ssg.service_group_id AS service_group_id, NEW.status AS status
        FROM bed_types bt
        JOIN chargehead_constants cc ON ( chargehead_id = 'BBED' )
        JOIN service_sub_groups ssg using(service_sub_group_id);
    ELSIF (TG_OP = 'INSERT' AND NEW.is_icu = 'Y') THEN
        INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, status) 
        SELECT DISTINCT NEW.bed_type_name AS entity_id, 'ICU' AS entity, lower(NEW.bed_type_name) AS item_name, 
        'mod_adt' AS module_id, 'I' AS orderable, 'N' AS operation_applicable, 
        'Y' AS package_applicable, 'N' AS is_multi_visit_package, 
        cc.service_sub_group_id, NEW.insurance_category_id, 'i' AS visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'ICU') AS direct_billing,
        ssg.service_group_id AS service_group_id, NEW.status AS status
        FROM bed_types bt
        JOIN chargehead_constants cc ON ( chargehead_id = 'BICU' )
        JOIN service_sub_groups ssg using(service_sub_group_id);
    ELSEIF (TG_OP = 'UPDATE' AND NEW.is_icu = 'N') THEN
        UPDATE orderable_item SET item_name = lower(NEW.bed_type_name), status = NEW.status, insurance_category_id = NEW.insurance_category_id, service_sub_group_id = cc.service_sub_group_id, service_group_id = ssg.service_group_id
        FROM service_sub_groups ssg
        JOIN chargehead_constants cc ON ( chargehead_id = 'BBED' ) 
        WHERE ssg.service_sub_group_id = cc.service_sub_group_id AND entity_id = OLD.bed_type_name AND entity = 'Bed';
    ELSEIF (TG_OP = 'UPDATE' AND NEW.is_icu = 'Y') THEN
        UPDATE orderable_item SET item_name = lower(NEW.bed_type_name), status = NEW.status, insurance_category_id = NEW.insurance_category_id, service_sub_group_id = cc.service_sub_group_id, service_group_id = ssg.service_group_id
        FROM service_sub_groups ssg
        JOIN chargehead_constants cc ON ( chargehead_id = 'BICU' ) 
        WHERE ssg.service_sub_group_id = cc.service_sub_group_id AND entity_id = OLD.bed_type_name AND entity = 'ICU';
    END IF;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS bed_orderable_item_trigger ON bed_types CASCADE;
CREATE TRIGGER bed_orderable_item_trigger
    AFTER INSERT OR UPDATE ON bed_types
    FOR EACH ROW
    EXECUTE PROCEDURE bed_orderable_item_procedure();



CREATE OR REPLACE FUNCTION direct_charge_orderable_item_procedure()
   RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT' AND NEW.associated_module = 'mod_billing' AND NEW.ip_applicable != '' AND NEW.op_applicable != '') THEN
       INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, status) 
        SELECT DISTINCT NEW.chargehead_id AS entity_id, 'Direct Charge' AS entity, lower(NEW.chargehead_name) AS item_name, 
        'mod_basic' AS module_id, 'N' AS orderable, 'N' AS operation_applicable, 
        'Y' AS package_applicable, 'N' AS is_multi_visit_package, NEW.service_sub_group_id, NEW.insurance_category_id,
        CASE WHEN (NEW.ip_applicable = 'Y' AND NEW.op_applicable = 'Y') THEN '*'
            WHEN (NEW.ip_applicable = 'Y' AND NEW.op_applicable = 'N') THEN 'i'
            WHEN (NEW.ip_applicable = 'N' AND NEW.op_applicable = 'Y') THEN 'o'
        END AS visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Direct Charge') AS direct_billing,
        ssg.service_group_id AS service_group_id, 'A' AS status   
        FROM chargehead_constants cc
        JOIN service_sub_groups ssg using(service_sub_group_id);
     ELSIF (TG_OP = 'UPDATE') THEN
        UPDATE orderable_item SET item_name = lower(NEW.chargehead_name), insurance_category_id = NEW.insurance_category_id, service_sub_group_id = NEW.service_sub_group_id, service_group_id = ssg.service_group_id
        FROM service_sub_groups ssg
        WHERE ssg.service_sub_group_id = NEW.service_sub_group_id AND entity_id = NEW.chargehead_id;
    END IF;

    RETURN NEW;
END;
$$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS direct_charge_orderable_item_trigger ON chargehead_constants CASCADE;
CREATE TRIGGER direct_charge_orderable_item_trigger
    AFTER INSERT OR UPDATE ON chargehead_constants
    FOR EACH ROW
    EXECUTE PROCEDURE direct_charge_orderable_item_procedure();


CREATE OR REPLACE FUNCTION lab_orderable_item_procedure()
   RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
    INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, item_codes, status) 
        SELECT DISTINCT NEW.test_id AS entity_id,
        CASE WHEN  ddept.category='DEP_LAB' THEN 'Laboratory'
             WHEN ddept.category='DEP_RAD'  THEN 'Radiology' END AS entity, lower(NEW.test_name) AS item_name, 
        'mod_basic' AS module_id, (CASE WHEN NEW.is_prescribable THEN 'Y' ELSE 'N' END) AS orderable, 'N' AS operation_applicable, 
        'Y' AS package_applicable, 'Y' AS is_multi_visit_package, NEW.service_sub_group_id, NEW.insurance_category_id, '*' AS visit_type, 
        CASE WHEN  ddept.category='DEP_LAB' THEN (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Laboratory')
             WHEN ddept.category='DEP_RAD'  THEN (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Radiology') END AS direct_billing,
        ssg.service_group_id AS service_group_id, lower(NEW.diag_code) AS item_codes, NEW.status AS status 
        FROM diagnostics d
        JOIN diagnostics_departments ddept USING (ddept_id)
        JOIN service_sub_groups ssg using(service_sub_group_id)
        WHERE (ddept.category='DEP_LAB' OR ddept.category='DEP_RAD') AND d.test_id = NEW.test_id AND d.ddept_id = NEW.ddept_id;

     ELSIF (TG_OP = 'UPDATE') THEN
        UPDATE orderable_item SET item_name = lower(NEW.test_name), insurance_category_id = NEW.insurance_category_id, service_sub_group_id = NEW.service_sub_group_id, service_group_id = ssg.service_group_id, status = NEW.status, item_codes = lower(NEW.diag_code), orderable = CASE WHEN NEW.is_prescribable THEN 'Y' ELSE 'N' END 
        FROM service_sub_groups ssg
        WHERE ssg.service_sub_group_id = NEW.service_sub_group_id AND entity_id = NEW.test_id;
    END IF;

    RETURN NEW;
END;
$$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS lab_orderable_item_trigger ON diagnostics CASCADE;
CREATE TRIGGER lab_orderable_item_trigger
    AFTER INSERT OR UPDATE ON diagnostics
    FOR EACH ROW
    EXECUTE PROCEDURE lab_orderable_item_procedure();


DROP FUNCTION IF EXISTS lab_org_trigger_procedure() CASCADE;
CREATE OR REPLACE FUNCTION lab_org_trigger_procedure()
    RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO mapping_org_id (orderable_item_id, org_id, status)
            SELECT oi.orderable_item_id, NEW.org_id,
            CASE WHEN (NEW.applicable = 't') THEN 'A' ELSE 'I' END AS status
            FROM orderable_item oi 
            WHERE oi.entity_id = NEW.test_id;
     ELSIF (TG_OP = 'UPDATE' AND NEW.applicable = 't' AND OLD.applicable = 'f') THEN
        UPDATE mapping_org_id  SET status = 'A' 
        WHERE orderable_item_id = (SELECT orderable_item_id FROM orderable_item WHERE entity_id = OLD.test_id AND (entity = 'Laboratory' OR entity = 'Radiology')) 
        AND org_id = NEW.org_id; 
        
    ELSIF (TG_OP = 'UPDATE' AND NEW.applicable = 'f' AND OLD.applicable = 't') THEN
        UPDATE mapping_org_id  SET status = 'I' 
        WHERE orderable_item_id = (SELECT orderable_item_id FROM orderable_item WHERE entity_id = OLD.test_id AND (entity = 'Laboratory' OR entity = 'Radiology')) 
        AND org_id = NEW.org_id;
    END IF;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;    

DROP TRIGGER IF EXISTS lab_org_trigger ON test_org_details CASCADE; 
CREATE TRIGGER lab_org_trigger
    AFTER INSERT OR UPDATE ON test_org_details
    FOR EACH ROW
    EXECUTE PROCEDURE  lab_org_trigger_procedure();


CREATE OR REPLACE FUNCTION services_orderable_item_procedure()
   RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, item_codes, status) 
            SELECT DISTINCT NEW.service_id AS entity_id, 'Service' AS entity, lower(NEW.service_name) AS item_name, 
            'mod_basic' AS module_id, 'Y' AS orderable, 'N' AS operation_applicable, 
            'Y' AS package_applicable, 'Y' AS is_multi_visit_package, NEW.service_sub_group_id, NEW.insurance_category_id, '*' AS visit_type, 
            (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Service') AS direct_billing,
            ssg.service_group_id AS service_group_id, lower(NEW.service_code) AS item_codes, NEW.status AS status 
            FROM services s
            JOIN service_sub_groups ssg using(service_sub_group_id) WHERE s.service_id = NEW.service_id;

     ELSIF (TG_OP = 'UPDATE') THEN
        UPDATE orderable_item SET item_name = lower(NEW.service_name), insurance_category_id = NEW.insurance_category_id, service_sub_group_id = NEW.service_sub_group_id, service_group_id = ssg.service_group_id, status = NEW.status, item_codes = lower(NEW.service_code) 
        FROM service_sub_groups ssg
        WHERE ssg.service_sub_group_id = NEW.service_sub_group_id AND entity_id = NEW.service_id;
    END IF;

    RETURN NEW;
END;
$$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS services_orderable_item_trigger ON services CASCADE;
CREATE TRIGGER services_orderable_item_trigger
    AFTER INSERT OR UPDATE ON services
    FOR EACH ROW
    EXECUTE PROCEDURE services_orderable_item_procedure();


DROP FUNCTION IF EXISTS services_org_trigger_procedure() CASCADE;
CREATE OR REPLACE FUNCTION services_org_trigger_procedure()
    RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO mapping_org_id (orderable_item_id, org_id, status)
            SELECT oi.orderable_item_id, NEW.org_id,
            CASE WHEN (NEW.applicable = 't') THEN 'A' ELSE 'I' END AS status
            FROM orderable_item oi 
            WHERE oi.entity_id = NEW.service_id;
            
    ELSIF (TG_OP = 'UPDATE' AND NEW.applicable = 't' AND OLD.applicable = 'f') THEN
        UPDATE mapping_org_id SET status = 'A' 
        WHERE orderable_item_id = (SELECT orderable_item_id FROM orderable_item WHERE entity_id = OLD.service_id AND entity = 'Service') 
        AND org_id = NEW.org_id;
        
    ELSIF (TG_OP = 'UPDATE' AND NEW.applicable = 'f' AND OLD.applicable = 't') THEN
        UPDATE mapping_org_id SET status = 'I' 
        WHERE orderable_item_id = (SELECT orderable_item_id FROM orderable_item WHERE entity_id = OLD.service_id AND entity = 'Service')
        AND org_id = NEW.org_id;
        
    END IF;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;    

DROP TRIGGER IF EXISTS services_org_trigger ON service_org_details CASCADE;
CREATE TRIGGER services_org_trigger
    AFTER INSERT OR UPDATE ON service_org_details
    FOR EACH ROW
    EXECUTE PROCEDURE  services_org_trigger_procedure();


CREATE OR REPLACE FUNCTION operation_orderable_item_procedure()
   RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, item_codes, status) 
            SELECT DISTINCT NEW.op_id AS entity_id, 'Operation' AS entity, lower(NEW.operation_name) AS item_name, 
            'mod_basic' AS module_id, 'Y' AS orderable, 'N' AS operation_applicable, 
            'N' AS package_applicable, 'N' AS is_multi_visit_package, NEW.service_sub_group_id, NEW.insurance_category_id, '*' AS visit_type, 
            (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Operation') AS direct_billing,
            ssg.service_group_id AS service_group_id, lower(NEW.operation_code) AS item_codes, NEW.status AS status 
            FROM operation_master om
            JOIN service_sub_groups ssg using(service_sub_group_id)
            WHERE om.op_id = NEW.op_id;

     ELSIF (TG_OP = 'UPDATE') THEN
        UPDATE orderable_item SET item_name = lower(NEW.operation_name), insurance_category_id = NEW.insurance_category_id, service_sub_group_id = NEW.service_sub_group_id, service_group_id = ssg.service_group_id, status = NEW.status, item_codes = lower(NEW.operation_code) 
        FROM service_sub_groups ssg
        WHERE ssg.service_sub_group_id = NEW.service_sub_group_id AND entity_id = NEW.op_id;
    END IF;

    RETURN NEW;
END;
$$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS operation_orderable_item_trigger ON operation_master CASCADE;
CREATE TRIGGER operation_orderable_item_trigger
    AFTER INSERT OR UPDATE ON operation_master
    FOR EACH ROW
    EXECUTE PROCEDURE operation_orderable_item_procedure();


DROP FUNCTION IF EXISTS operation_org_trigger_procedure() CASCADE;
CREATE OR REPLACE FUNCTION operation_org_trigger_procedure()
    RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO mapping_org_id (orderable_item_id, org_id, status)
            SELECT oi.orderable_item_id, NEW.org_id,
            CASE WHEN (NEW.applicable = 't') THEN 'A' ELSE 'I' END AS status
            FROM orderable_item oi 
            WHERE oi.entity_id = NEW.operation_id;
   ELSIF (TG_OP = 'UPDATE' AND NEW.applicable = 't' AND OLD.applicable = 'f') THEN
        UPDATE mapping_org_id SET status = 'A' 
        WHERE orderable_item_id = (SELECT orderable_item_id FROM orderable_item WHERE entity_id = OLD.operation_id AND entity = 'Operation')
        AND org_id = NEW.org_id;
        
    ELSIF (TG_OP = 'UPDATE' AND NEW.applicable = 'f' AND OLD.applicable = 't') THEN
        UPDATE mapping_org_id SET status = 'I' 
        WHERE orderable_item_id = (SELECT orderable_item_id FROM orderable_item WHERE entity_id = OLD.operation_id AND entity = 'Operation')
        AND org_id = NEW.org_id;
    END IF;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;    

DROP TRIGGER IF EXISTS operation_org_trigger ON operation_org_details CASCADE;
CREATE TRIGGER operation_org_trigger
    AFTER INSERT OR UPDATE ON operation_org_details
    FOR EACH ROW
    EXECUTE PROCEDURE  operation_org_trigger_procedure();

-- this insert/update the any changes made to packages
-- Used by order flat structure
CREATE OR REPLACE FUNCTION packages_orderable_item_procedure()
   RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT' AND (NEW.visit_applicability = 'o' OR NEW.visit_applicability = 'i') AND NEW.multi_visit_package = false) THEN
       INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, item_codes, status, valid_from_date, valid_to_date) 
        SELECT DISTINCT NEW.package_id::text as entity_id, 'Package' as entity, lower(NEW.package_name) as item_name, 
        'mod_basic' as module_id, 'Y' as orderable, 'N' as operation_applicable, 
        'Y' as package_applicable, 'N' as is_multi_visit_package, NEW.service_sub_group_id, NEW.insurance_category_id,
        CASE WHEN (NEW.visit_applicability = 'i') THEN 'i'
             WHEN (NEW.visit_applicability = 'o') THEN 'o' END as visit_type,
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Package') as direct_billing,
        ssg.service_group_id as service_group_id, lower(concat(NEW.package_code, ' ', NEW.order_code)) as item_codes,
        CASE WHEN (NEW.status = 'A' AND  NEW.approval_status='A' ) THEN 'A' ELSE 'I' END as status,
        pm.valid_from as valid_from_date, pm.valid_till as valid_to_date
        FROM packages pm
        JOIN service_sub_groups ssg using(service_sub_group_id)
        WHERE pm.package_id = NEW.package_id;

    ELSEIF (TG_OP = 'INSERT' AND NEW.visit_applicability = 'o' AND NEW.multi_visit_package = true) THEN
      INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, item_codes, status, valid_from_date, valid_to_date)  
        SELECT DISTINCT NEW.package_id::text as entity_id, 'MultiVisitPackage' as entity, lower(NEW.package_name) as item_name, 
        'mod_adv_packages' as module_id, 'Y' as orderable, 'N' as operation_applicable, 
        'N' as package_applicable, 'N' as is_multi_visit_package, NEW.service_sub_group_id, NEW.insurance_category_id, 'o' as visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'MultiVisitPackage') as direct_billing,
        ssg.service_group_id as service_group_id, lower(concat(NEW.package_code, ' ', NEW.order_code)) as item_codes,
        CASE WHEN (NEW.status = 'A' AND  NEW.approval_status='A' ) THEN 'A' ELSE 'I' END as status,
        pm.valid_from as valid_from_date, pm.valid_till as valid_to_date
        FROM packages pm
        JOIN service_sub_groups ssg using(service_sub_group_id)
        WHERE pm.package_id = NEW.package_id;

    ELSEIF (TG_OP = 'INSERT' AND (NEW.type = 'd') AND NEW.type != 'T' AND NEW.multi_visit_package = false) THEN
       INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, item_codes, status, valid_from_date, valid_to_date) 
        SELECT DISTINCT NEW.package_id::text as entity_id, 'DiagPackage' as entity, lower(NEW.package_name) as item_name, 
        'mod_basic' as module_id, 'Y' as orderable, 'N' as operation_applicable, 
        'Y' as package_applicable, 'N' as is_multi_visit_package, NEW.service_sub_group_id, NEW.insurance_category_id,
        '*' as visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Package') as direct_billing,
        ssg.service_group_id as service_group_id, lower(concat(NEW.package_code, ' ', NEW.order_code)) as item_codes,
        CASE WHEN (NEW.status = 'A' AND  NEW.approval_status='A' ) THEN 'A' ELSE 'I' END as status,
        pm.valid_from as valid_from_date, pm.valid_till as valid_to_date
        FROM packages pm
        JOIN service_sub_groups ssg using(service_sub_group_id)
        WHERE pm.package_id = NEW.package_id;
    
    ELSIF (TG_OP = 'UPDATE') THEN
        UPDATE orderable_item SET item_name = lower(NEW.package_name), insurance_category_id = NEW.insurance_category_id, service_sub_group_id = NEW.service_sub_group_id, service_group_id = ssg.service_group_id,
        item_codes = lower(concat(NEW.package_code, ' ', NEW.order_code)), status = (CASE WHEN (NEW.status = 'A' AND  NEW.approval_status='A' ) THEN 'A' ELSE 'I' END),
        valid_from_date = NEW.valid_from, valid_to_date = NEW.valid_till
        FROM service_sub_groups ssg
        WHERE ssg.service_sub_group_id = NEW.service_sub_group_id AND entity_id = NEW.package_id::text;
    END IF;

    RETURN NEW;
END;
$$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS packages_orderable_item_trigger ON pack_master CASCADE;
CREATE TRIGGER packages_orderable_item_trigger
    AFTER INSERT OR UPDATE ON pack_master
    FOR EACH ROW
    EXECUTE PROCEDURE packages_orderable_item_procedure();


-- this update/delete/insert the package rate plan mapping used by order flat structure
DROP FUNCTION IF EXISTS pack_org_trigger_procedure() CASCADE;
CREATE OR REPLACE FUNCTION pack_org_trigger_procedure()
    RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO mapping_org_id (orderable_item_id, org_id, status)
            SELECT oi.orderable_item_id, NEW.org_id,
            CASE WHEN (NEW.applicable = 't') THEN 'A' ELSE 'I' END AS status
            FROM orderable_item oi 
            WHERE oi.entity_id = NEW.package_id::text AND (entity = 'Package' OR entity = 'MultiVisitPackage' OR entity = 'DiagPackage');
    ELSIF (TG_OP = 'UPDATE' AND NEW.applicable = 't' AND OLD.applicable = 'f') THEN
        UPDATE mapping_org_id SET status = 'A' 
        WHERE orderable_item_id = (SELECT orderable_item_id FROM orderable_item WHERE entity_id = OLD.package_id::character varying AND (entity = 'Package' OR entity = 'MultiVisitPackage' OR entity = 'DiagPackage'))
        AND org_id = NEW.org_id;
        
    ELSIF (TG_OP = 'UPDATE' AND NEW.applicable = 'f' AND OLD.applicable = 't') THEN
        UPDATE mapping_org_id SET status = 'I' 
        WHERE orderable_item_id = (SELECT orderable_item_id FROM orderable_item WHERE entity_id = OLD.package_id::character varying AND (entity = 'Package' OR entity = 'MultiVisitPackage' OR entity = 'DiagPackage'))
        AND org_id = NEW.org_id;
    ELSIF (TG_OP = 'DELETE') THEN
        DELETE from mapping_org_id
        WHERE orderable_item_id = (SELECT orderable_item_id FROM orderable_item WHERE entity_id = OLD.package_id::character varying AND (entity = 'Package' OR entity = 'MultiVisitPackage' OR entity = 'DiagPackage'))
        AND org_id = OLD.org_id;
    END IF;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;    

DROP TRIGGER IF EXISTS package_org_trigger ON pack_org_details CASCADE;
CREATE TRIGGER package_org_trigger
    AFTER INSERT OR UPDATE OR DELETE ON pack_org_details
    FOR EACH ROW
    EXECUTE PROCEDURE  pack_org_trigger_procedure();


-- this update/delete/insert the package center mapping used by order flat structure
CREATE OR REPLACE FUNCTION package_center_master_procedure()
    RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO mapping_center_id (orderable_item_id, center_id, status)
            SELECT DISTINCT oi.orderable_item_id, NEW.center_id, NEW.status 
            FROM orderable_item oi 
            WHERE oi.entity_id = NEW.pack_id::text; 
        RETURN NEW;
    ELSIF (TG_OP = 'UPDATE') THEN
        UPDATE mapping_center_id mci SET status = NEW.status
        FROM orderable_item oi
        WHERE oi.entity_id = NEW.pack_id::text AND center_id = NEW.center_id AND oi.orderable_item_id = mci.orderable_item_id;
        RETURN NEW;
    ELSIF (TG_OP = 'DELETE') THEN
        DELETE FROM mapping_center_id mci
        USING orderable_item oi 
        WHERE oi.entity_id = OLD.pack_id::text AND mci.center_id = OLD.center_id AND oi.orderable_item_id = mci.orderable_item_id;
        RETURN OLD;
    END IF;
END;
$$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS package_center_mapping_trigger ON package_center_master CASCADE;
CREATE TRIGGER package_center_mapping_trigger
    AFTER INSERT OR UPDATE OR DELETE ON package_center_master
    FOR EACH ROW
    EXECUTE PROCEDURE package_center_master_procedure();


-- this updates/deletes/insert the package tpa mapping used for order flat structure. 
CREATE OR REPLACE FUNCTION package_sponsor_master_procedure()
    RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO mapping_tpa_id (orderable_item_id, tpa_id, status)
            SELECT DISTINCT oi.orderable_item_id, NEW.tpa_id, NEW.status 
            FROM orderable_item oi 
            WHERE oi.entity_id = NEW.pack_id::text; 
        RETURN NEW;
    ELSIF (TG_OP = 'UPDATE') THEN
        UPDATE mapping_tpa_id mti SET status = NEW.status
        FROM orderable_item oi
        WHERE oi.entity_id = NEW.pack_id::text AND tpa_id = NEW.tpa_id AND oi.orderable_item_id = mti.orderable_item_id;
        RETURN NEW;
    ELSIF (TG_OP = 'DELETE') THEN
        DELETE FROM mapping_tpa_id mti
        USING orderable_item oi 
        WHERE oi.entity_id = OLD.pack_id::text AND mti.tpa_id = OLD.tpa_id AND oi.orderable_item_id = mti.orderable_item_id;
        RETURN OLD;
    END IF;
END;
$$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS package_sponsor_master_trigger ON package_sponsor_master CASCADE;
CREATE TRIGGER package_sponsor_master_trigger
    AFTER INSERT OR UPDATE OR DELETE ON package_sponsor_master
    FOR EACH ROW
    EXECUTE PROCEDURE package_sponsor_master_procedure();

CREATE OR REPLACE FUNCTION package_plan_master_procedure()
    RETURNS TRIGGER as $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO mapping_plan_id (orderable_item_id, plan_id, status)
            SELECT DISTINCT oi.orderable_item_id, NEW.plan_id, NEW.status
            FROM orderable_item oi
            WHERE oi.entity_id = NEW.pack_id::text;
        RETURN NEW;
    ELSIF (TG_OP = 'UPDATE') THEN
        UPDATE mapping_plan_id mti SET status = NEW.status
        FROM orderable_item oi
        WHERE oi.entity_id = NEW.pack_id::text AND plan_id = NEW.plan_id AND oi.orderable_item_id = mti.orderable_item_id;
        RETURN NEW;
    ELSIF (TG_OP = 'DELETE') THEN
        DELETE FROM mapping_plan_id mti
        USING orderable_item oi
        WHERE oi.entity_id = OLD.pack_id::text AND mti.plan_id = OLD.plan_id AND oi.orderable_item_id = mti.orderable_item_id;
        RETURN OLD;
    END IF;
END;
$$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS package_plan_master_trigger ON package_plan_master CASCADE;
CREATE TRIGGER package_plan_master_trigger
    AFTER INSERT OR UPDATE OR DELETE ON package_plan_master
    FOR EACH ROW
    EXECUTE PROCEDURE package_plan_master_procedure();


CREATE OR REPLACE FUNCTION set_sample_status() returns TRIGGER AS $BODY$
	
	BEGIN
		UPDATE sample_collection sc SET sample_conduction_status = CASE WHEN (SELECT true FROM tests_prescribed tp WHERE tp.sample_collection_id = NEW.sample_collection_id AND tp.conducted NOT IN ('N', 'NRN', 'X') LIMIT 1) THEN 'P' ELSE 'N' END 
		WHERE sc.sample_collection_id = NEW.sample_collection_id;

		RETURN NEW;
	END;

$BODY$ language plpgsql;

DROP TRIGGER IF EXISTS set_sample_status_trigger ON tests_prescribed CASCADE;
CREATE TRIGGER set_sample_status_trigger 
AFTER INSERT OR UPDATE ON tests_prescribed
FOR EACH ROW
WHEN (NEW.sample_collection_id IS NOT NULL)
EXECUTE PROCEDURE set_sample_status();

DROP VIEW IF EXISTS clinical_lab_values_ext_view_crosstab CASCADE;
CREATE OR REPLACE VIEW clinical_lab_values_ext_view_crosstab AS (
SELECT clinicalreport.*
   FROM extensions.crosstab('SELECT clinical_lab_recorded_id,trm.resultlabel AS bucket,
                    max(clv.test_value || ''$_|_$'' || clv.value_date::text) AS bucketvalue
FROM clinical_lab_values clv
LEFT JOIN clinical_lab_result clres ON clres.resultlabel_id =
clv.resultlabel_id
  LEFT JOIN test_results_master trm ON trm.resultlabel_id =
clres.resultlabel_id
GROUP BY clinical_lab_recorded_id,trm.resultlabel'::text, 'select * from (values (''Hemoglobin''),
(''Total Leucocyte Count''), (''Differential Leucocyte Count''), (''Platelet Count''), (''Random Blood Sugar''),
(''Mean Corpuscular Volume''), (''pre% BUN''), (''post% BUN''), (''Creatinine''), (''Sodium''), (''Pottasium''), (''Bicarbonate''),
(''Uric Acid''), (''Calcium''), (''Phosphorus''), (''Total Bilirubin''), (''Total Protein''), (''Albumin''), (''Alanine Aminotransferase''),
(''Aspartate Aminotransferase''), (''Alkaline Phosphatase''), (''Human Immuno Deficiency Virus''), (''Hepatitis B Surface Antigen''),
(''Hepatitis C Virus''), (''Anti-Hepatitis B Surface Antigen Titres''), (''ParaThyroid Hormone''), (''Iron''), (''Total Iron Binding Capacity''),
(''Ferritin''), (''Total Saturation''), (''Total Cholesterol''), (''Triacylglycerol''), (''Low-Density Lipoprotein''), (''High-Density Lipoprotein''),
(''KT/V''), (''URR''), (''Ca PO4''), (''Access Site''), (''Access Site Infection''), (''Access Type (Primary)''),
(''Access type (Secondary)''), (''Infection''), (''Organism''), (''Location/Site'')) AS foo (label)'::text) clinicalreport(clinical_lab_recorded_id integer,
hemoglobin text, tlc text, dlc text, plt text, rbs text, mcv text, prebun text, postbun text, creatinine text, sodium text, potassium text, bicarbonate text,
ua text, ca text, phos text, bilirubin text, total_protein text, albumin text, alt text, ast text, alk_phos text,
hiv text, hbsg text, hcv text, anti_hbs text, pth text, iron text, tibc text, ferritin text, t_sat text, t_chol text,
tg text, ldl text, hdl text, ktv text, urr text, capo text, asite text, asiteinf text, atypeprim text, atypesec text,
inf text, org text, locsite text));


DROP FUNCTION IF EXISTS orderable_items_token_trigger() CASCADE;
CREATE FUNCTION orderable_items_token_trigger() RETURNS TRIGGER AS $BODY$
BEGIN

    IF (TG_OP = 'DELETE' OR (TG_OP = 'UPDATE' AND (NEW.item_name != OLD.item_name OR NEW.item_codes != OLD.item_codes))) THEN
        DELETE FROM orderable_items_tokens WHERE orderable_item_id = OLD.orderable_item_id;
    END IF;
    
    IF ((TG_OP = 'UPDATE' AND (NEW.item_name != OLD.item_name OR NEW.item_codes != OLD.item_codes)) OR TG_OP = 'INSERT') THEN
        INSERT INTO orderable_items_tokens (SELECT orderable_item_id, s.token FROM  orderable_item oi, unnest(string_to_array(regexp_replace(trim(both ' ' from oi.item_name), '\s+', ' '), ' ')) s(token) WHERE orderable_item_id = NEW.orderable_item_id);
        
        INSERT INTO orderable_items_tokens (SELECT orderable_item_id, s.token FROM  orderable_item oi, unnest(string_to_array(regexp_replace(trim(both ' ' from oi.item_codes), '\s+', ' '), ' ')) s(token) WHERE orderable_item_id = NEW.orderable_item_id);
    END IF;

    RETURN NEW;

END;
$BODY$ language plpgsql;

DROP TRIGGER IF EXISTS orderable_items_token_trigger ON orderable_item CASCADE;
CREATE TRIGGER orderable_items_token_trigger
    AFTER INSERT OR UPDATE OR DELETE ON orderable_item
    FOR EACH ROW
    EXECUTE PROCEDURE orderable_items_token_trigger();
    
DROP FUNCTION IF EXISTS order_sets_orderable_item_procedure() CASCADE;    
CREATE FUNCTION order_sets_orderable_item_procedure() RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT' and NEW.type = 'O') THEN
       INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, visit_type, direct_billing, service_group_id, item_codes, status) 
        SELECT DISTINCT NEW.package_id::text AS entity_id, 'Order Sets' AS entity, lower(NEW.package_name) AS item_name, 
        'mod_basic' AS module_id, 'Y' AS orderable, 'N' AS operation_applicable, 
        'Y' AS package_applicable, 'N' AS is_multi_visit_package, NEW.service_sub_group_id, NEW.visit_applicability AS visit_type, 
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Package') AS direct_billing,
        ssg.service_group_id AS service_group_id, NEW.package_code AS item_codes,
        NEW.status AS status
        FROM packages p
        JOIN service_sub_groups ssg using(service_sub_group_id)
        WHERE p.package_id = NEW.package_id;
    
    ELSIF (TG_OP = 'UPDATE') THEN
        UPDATE orderable_item SET item_name = lower(NEW.package_name), service_sub_group_id = NEW.service_sub_group_id, service_group_id = ssg.service_group_id,
        item_codes = NEW.package_code, status = NEW.status, visit_type = NEW.visit_applicability
        FROM service_sub_groups ssg
        WHERE ssg.service_sub_group_id = NEW.service_sub_group_id AND entity_id = NEW.package_id::text;
    END IF;

    RETURN NEW;
END;
$$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS order_sets_orderable_item_trigger ON packages CASCADE;
CREATE TRIGGER order_sets_orderable_item_trigger
    AFTER INSERT OR UPDATE ON packages
    FOR EACH ROW
    EXECUTE PROCEDURE order_sets_orderable_item_procedure();

CREATE OR REPLACE FUNCTION auto_close_consultation(noOfCons int)
	RETURNS integer AS $BODY$
DECLARE
	consultation RECORD;
	close_after_hours int;
	hours_text character varying;
	count int;
	consultations int[];
	stn_detail_id int;
BEGIN
	count := 0;
	if noOfCons is null or noOfCons = 0 then
		return 0;
	end if;
	SELECT op_consultation_auto_closure_period FROM clinical_preferences WHERE op_consultation_auto_closure = 'Y' into close_after_hours;
	hours_text := close_after_hours || ' hours';
	FOR consultation IN 
		SELECT dc.consultation_id, pr.mr_no, pr.patient_id, d.doctor_id, d.dept_id, pr.center_id
		FROM doctor_consultation dc
			JOIN patient_registration pr ON (dc.patient_id=pr.patient_id)
			JOIN doctors d ON (d.doctor_id=dc.doctor_name) 
			JOIN bill_activity_charge bac ON (bac.activity_id=dc.consultation_id::text and bac.activity_code='DOC') 
			JOIN bill_charge bc ON (bac.charge_id=bc.charge_id) 
			JOIN bill b ON (b.bill_no = bc.bill_no) 
		WHERE dc.status='A' 
			AND (b.bill_type = 'C' or (b.bill_type='P' AND b.payment_status = 'P')) 
			AND dc.presc_date::timestamp(0) <= localtimestamp(0) - cast(hours_text AS interval) 
		limit noOfCons 

	LOOP
		count := count + 1;
		consultations := array_append(consultations, consultation.consultation_id);
		SELECT nextval('patient_section_details_seq') into stn_detail_id;

		insert into patient_section_details(section_detail_id, mr_no, patient_id, section_item_id, 
			generic_form_id, item_type, section_id, section_status, finalized, finalized_user, 
			user_name, mod_time) 
		values (stn_detail_id, consultation.mr_no, consultation.patient_id, 
			consultation.consultation_id, 0, 'CONS', '-6', 'I', 'N', null, 'InstaAdmin', 
			localtimestamp(0));

		insert into patient_section_forms(section_detail_id, form_id, form_type, display_order) 
			(select stn_detail_id, '-1', 'Form_CONS', 1);

	END LOOP;
 	UPDATE doctor_consultation set status='C', consultation_complete_time=now(),
 			consultation_mod_time=localtimestamp(3), username='InstaAdmin'
			where consultation_id in (select unnest(consultations));
 	RETURN count;
END;
$BODY$ LANGUAGE 'plpgsql';

-- this update the value of schedule column base on schedulable_by for doctors table
DROP FUNCTION IF EXISTS change_schedule_with_scheduleable_by_procedure() CASCADE;
CREATE OR REPLACE FUNCTION change_schedule_with_scheduleable_by_procedure()
    RETURNS TRIGGER as $$
BEGIN
IF (TG_OP = 'INSERT') THEN
        update doctors set schedule = CASE WHEN (NEW.scheduleable_by = 'N') THEN false ELSE true END where doctor_id = NEW.doctor_id ;
        RETURN NEW;
 ELSIF (TG_OP = 'UPDATE') THEN
        IF OLD.scheduleable_by!=NEW.scheduleable_by THEN 
        update doctors set schedule = CASE WHEN (NEW.scheduleable_by = 'N') THEN false ELSE true END where doctor_id = NEW.doctor_id ;
        END IF;
        RETURN NEW;
    END IF;
END;
$$
LANGUAGE plpgsql; 

DROP TRIGGER IF EXISTS change_schedule_with_scheduleable_by_trigger ON doctors CASCADE;
CREATE TRIGGER change_schedule_with_scheduleable_by_trigger
    AFTER INSERT OR UPDATE ON doctors
    FOR EACH ROW
    EXECUTE PROCEDURE change_schedule_with_scheduleable_by_procedure();
    
 --------------------------- Services -----------------------------------------------------
DROP FUNCTION IF EXISTS service_insurance_category_mapping_trigger() CASCADE;
CREATE OR REPLACE FUNCTION service_insurance_category_mapping_trigger()
    RETURNS TRIGGER as $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        UPDATE services SET insurance_category_id = (select NEW.insurance_Category_id 
														from item_insurance_categories iic
														JOIN service_insurance_category_mapping sicm 
														ON NEW.insurance_category_id =iic.insurance_category_id
														WHERE service_id = NEW.service_id 
														order by priority limit 1)
		WHERE service_id = NEW.service_id;												
        
    END IF;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;  

DROP TRIGGER IF EXISTS service_insurance_category_mapping_trigger ON service_insurance_category_mapping CASCADE;
CREATE CONSTRAINT TRIGGER service_insurance_category_mapping_trigger
	AFTER INSERT
	ON service_insurance_category_mapping
	DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
	EXECUTE PROCEDURE service_insurance_category_mapping_trigger();

------------------------------ Stores ----------------------------------
DROP FUNCTION IF EXISTS store_items_insurance_category_mapping_trigger() CASCADE;
CREATE OR REPLACE FUNCTION store_items_insurance_category_mapping_trigger()
    RETURNS TRIGGER as $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        UPDATE store_item_details SET insurance_category_id = (select NEW.insurance_Category_id 
														from item_insurance_categories iic
														JOIN store_items_insurance_category_mapping sicm 
														ON NEW.insurance_category_id =iic.insurance_category_id
														WHERE medicine_id = NEW.medicine_id 
														order by priority limit 1)
		WHERE medicine_id = NEW.medicine_id;												
        
    END IF;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;  

DROP TRIGGER IF EXISTS store_items_insurance_category_mapping_trigger ON store_items_insurance_category_mapping CASCADE;
CREATE CONSTRAINT TRIGGER store_items_insurance_category_mapping_trigger
	AFTER INSERT
	ON store_items_insurance_category_mapping
	DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
	EXECUTE PROCEDURE store_items_insurance_category_mapping_trigger();

----------------------------- Bed Types ------------------------------------------
DROP FUNCTION IF EXISTS bed_types_insurance_category_mapping_trigger() CASCADE;
CREATE OR REPLACE FUNCTION bed_types_insurance_category_mapping_trigger()
    RETURNS TRIGGER as $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        UPDATE bed_types SET insurance_category_id = (select NEW.insurance_Category_id 
														from item_insurance_categories iic
														JOIN bed_types_insurance_category_mapping sicm 
														ON NEW.insurance_category_id =iic.insurance_category_id
														WHERE bed_type_name = NEW.bed_type_name 
														order by priority limit 1)
		WHERE bed_type_name = NEW.bed_type_name;												
        
    END IF;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;  

DROP TRIGGER IF EXISTS bed_types_insurance_category_mapping_trigger ON bed_types_insurance_category_mapping CASCADE;
CREATE CONSTRAINT TRIGGER bed_types_insurance_category_mapping_trigger
	AFTER INSERT
	ON bed_types_insurance_category_mapping
	DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
	EXECUTE PROCEDURE bed_types_insurance_category_mapping_trigger();

----------------------------- Consultation Types -----------------------------------
DROP FUNCTION IF EXISTS consultation_types_insurance_category_mapping_trigger() CASCADE;
CREATE OR REPLACE FUNCTION consultation_types_insurance_category_mapping_trigger()
    RETURNS TRIGGER as $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        UPDATE consultation_types SET insurance_category_id = (select NEW.insurance_Category_id 
														from item_insurance_categories iic
														JOIN consultation_types_insurance_category_mapping sicm 
														ON NEW.insurance_category_id =iic.insurance_category_id
														WHERE consultation_type_id = NEW.consultation_type_id 
														order by priority limit 1)
		WHERE consultation_type_id = NEW.consultation_type_id;												
        
    END IF;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;  

DROP TRIGGER IF EXISTS consultation_types_insurance_category_mapping_trigger ON consultation_types_insurance_category_mapping CASCADE;
CREATE CONSTRAINT TRIGGER consultation_types_insurance_category_mapping_trigger
	AFTER INSERT
	ON consultation_types_insurance_category_mapping
	DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
	EXECUTE PROCEDURE consultation_types_insurance_category_mapping_trigger();

-------------------------------- Anesthesia Type ----------------------------------
DROP FUNCTION IF EXISTS anesthesia_types_insurance_category_mapping_trigger() CASCADE;
CREATE OR REPLACE FUNCTION anesthesia_types_insurance_category_mapping_trigger()
    RETURNS TRIGGER as $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        UPDATE anesthesia_type_master SET insurance_category_id = (select NEW.insurance_Category_id 
														from item_insurance_categories iic
														JOIN anesthesia_types_insurance_category_mapping sicm 
														ON NEW.insurance_category_id =iic.insurance_category_id
														WHERE anesthesia_type_id = NEW.anesthesia_type_id 
														order by priority limit 1)
		WHERE anesthesia_type_id = NEW.anesthesia_type_id;												
        
    END IF;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;  

DROP TRIGGER IF EXISTS anesthesia_types_insurance_category_mapping_trigger ON anesthesia_types_insurance_category_mapping CASCADE;
CREATE CONSTRAINT TRIGGER anesthesia_types_insurance_category_mapping_trigger
	AFTER INSERT
	ON anesthesia_types_insurance_category_mapping
	DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
	EXECUTE PROCEDURE anesthesia_types_insurance_category_mapping_trigger();

-------------------------------- Packages -------------------------------------------------
DROP FUNCTION IF EXISTS packages_insurance_category_mapping_trigger() CASCADE;
CREATE OR REPLACE FUNCTION packages_insurance_category_mapping_trigger()
    RETURNS TRIGGER as $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        UPDATE packages SET insurance_category_id = (select NEW.insurance_Category_id
														from item_insurance_categories iic
														JOIN packages_insurance_category_mapping sicm 
														ON NEW.insurance_category_id =iic.insurance_category_id
														WHERE package_id = NEW.package_id 
														order by priority limit 1)
		WHERE package_id = NEW.package_id;												
        
    END IF;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;  

DROP TRIGGER IF EXISTS packages_insurance_category_mapping_trigger ON packages_insurance_category_mapping CASCADE;
CREATE CONSTRAINT TRIGGER packages_insurance_category_mapping_trigger
	AFTER INSERT
	ON packages_insurance_category_mapping
	DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
	EXECUTE PROCEDURE packages_insurance_category_mapping_trigger();	

------------------------------ Common Charges -------------------------------------------------
DROP FUNCTION IF EXISTS common_charges_insurance_category_mapping_trigger() CASCADE;
CREATE OR REPLACE FUNCTION common_charges_insurance_category_mapping_trigger()
    RETURNS TRIGGER as $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        UPDATE common_charges_master SET insurance_category_id = (select NEW.insurance_Category_id 
														from item_insurance_categories iic
														JOIN common_charges_insurance_category_mapping sicm 
														ON NEW.insurance_category_id =iic.insurance_category_id
														WHERE charge_name = NEW.charge_name 
														order by priority limit 1)
		WHERE charge_name = NEW.charge_name;												
        
    END IF;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;  

DROP TRIGGER IF EXISTS common_charges_insurance_category_mapping_trigger ON common_charges_insurance_category_mapping CASCADE;
CREATE CONSTRAINT TRIGGER common_charges_insurance_category_mapping_trigger
	AFTER INSERT
	ON common_charges_insurance_category_mapping
	DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
	EXECUTE PROCEDURE common_charges_insurance_category_mapping_trigger();	

------------------------------------- Meals -----------------------------------
DROP FUNCTION IF EXISTS diet_insurance_category_mapping_trigger() CASCADE;
CREATE OR REPLACE FUNCTION diet_insurance_category_mapping_trigger()
    RETURNS TRIGGER as $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        UPDATE diet_master SET insurance_category_id = (select NEW.insurance_Category_id 
														from item_insurance_categories iic
														JOIN diet_insurance_category_mapping sicm 
														ON NEW.insurance_category_id =iic.insurance_category_id
														WHERE diet_id = NEW.diet_id 
														order by priority limit 1)
		WHERE diet_id = NEW.diet_id;												
        
    END IF;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;  

DROP TRIGGER IF EXISTS diet_insurance_category_mapping_trigger ON diet_insurance_category_mapping CASCADE;
CREATE CONSTRAINT TRIGGER diet_insurance_category_mapping_trigger
	AFTER INSERT
	ON diet_insurance_category_mapping
	DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
	EXECUTE PROCEDURE diet_insurance_category_mapping_trigger();		

----------------------- Equipment ---------------------------------------
DROP FUNCTION IF EXISTS equipment_insurance_category_mapping_trigger() CASCADE;
CREATE OR REPLACE FUNCTION equipment_insurance_category_mapping_trigger()
    RETURNS TRIGGER as $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        UPDATE equipment_master SET insurance_category_id = (select NEW.insurance_Category_id 
														from item_insurance_categories iic
														JOIN equipment_insurance_category_mapping sicm 
														ON NEW.insurance_category_id =iic.insurance_category_id
														WHERE equipment_id = NEW.equipment_id 
														order by priority limit 1)
		WHERE eq_id = NEW.equipment_id;												
        
    END IF;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;  

DROP TRIGGER IF EXISTS equipment_insurance_category_mapping_trigger ON equipment_insurance_category_mapping CASCADE;
CREATE CONSTRAINT TRIGGER equipment_insurance_category_mapping_trigger
	AFTER INSERT
	ON equipment_insurance_category_mapping
	DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
	EXECUTE PROCEDURE equipment_insurance_category_mapping_trigger();	

----------------------- Operations ------------------------------------
DROP FUNCTION IF EXISTS operation_insurance_category_mapping_trigger() CASCADE;
CREATE OR REPLACE FUNCTION operation_insurance_category_mapping_trigger()
    RETURNS TRIGGER as $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        UPDATE operation_master SET insurance_category_id = (select NEW.insurance_Category_id 
														from item_insurance_categories iic
														JOIN operation_insurance_category_mapping sicm 
														ON NEW.insurance_category_id =iic.insurance_category_id
														WHERE operation_id = NEW.operation_id 
														order by priority limit 1)
		WHERE op_id = NEW.operation_id;												
        
    END IF;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;  

DROP TRIGGER IF EXISTS operation_insurance_category_mapping_trigger ON operation_insurance_category_mapping CASCADE;
CREATE CONSTRAINT TRIGGER operation_insurance_category_mapping_trigger
	AFTER INSERT
	ON operation_insurance_category_mapping
	DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
	EXECUTE PROCEDURE operation_insurance_category_mapping_trigger();

----------------------------- Diagnostics ---------------------------------
DROP FUNCTION IF EXISTS diagnostic_test_insurance_category_mapping_trigger() CASCADE;
CREATE OR REPLACE FUNCTION diagnostic_test_insurance_category_mapping_trigger()
    RETURNS TRIGGER as $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        UPDATE diagnostics SET insurance_category_id = (select NEW.insurance_Category_id 
														from item_insurance_categories iic
														JOIN diagnostic_test_insurance_category_mapping sicm 
														ON NEW.insurance_category_id =iic.insurance_category_id
														WHERE diagnostic_test_id = NEW.diagnostic_test_id 
														order by priority limit 1)
		WHERE test_id = NEW.diagnostic_test_id;												
        
    END IF;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;  

DROP TRIGGER IF EXISTS diagnostic_test_insurance_category_mapping_trigger ON diagnostic_test_insurance_category_mapping CASCADE;
CREATE CONSTRAINT TRIGGER diagnostic_test_insurance_category_mapping_trigger
	AFTER INSERT
	ON diagnostic_test_insurance_category_mapping
	DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
	EXECUTE PROCEDURE diagnostic_test_insurance_category_mapping_trigger();

DROP VIEW IF EXISTS ipemr_form_audit_log_view CASCADE;
CREATE OR REPLACE VIEW ipemr_form_audit_log_view AS
       SELECT  pral.log_id, pr.mr_no, pral.patient_id, pral.user_name, pral.mod_time, pral.operation,
		       pral.field_name, pral.old_value, pral.new_value
        FROM patient_registration_audit_log pral
        JOIN patient_registration pr ON pr.patient_id = pral.patient_id
		WHERE pral.field_name IN ('ipemr_reopen_remarks', 'ipemr_status', 'ipemr_complete_time');


DROP VIEW IF EXISTS patient_prescription_ip_audit_log_view CASCADE;
CREATE OR REPLACE VIEW patient_prescription_ip_audit_log_view AS
        SELECT psdal.log_id, psdal.user_name, psdal.mod_time, psdal.operation,
            psdal.field_name, psdal.old_value, psdal.new_value, psdal.mr_no, psdal.patient_id as patient_id,
            'patient_section_detail_audit_log' ::text
            AS base_table, null::integer as pres_id, null::text as type
        FROM patient_section_details_audit_log psdal
        JOIN patient_section_details psd on (psd.section_detail_id=psdal.section_detail_id)
        WHERE psd.section_id=-7

    UNION ALL
        SELECT ptpal.log_id, ptpal.user_name, ptpal.mod_time, ptpal.operation,
            ptpal.field_name, ptpal.old_value, ptpal.new_value, pr.mr_no, pr.patient_id, 
            'patient_prescriptions_details_audit_log_view' ::text AS base_table,
            ptpal.op_test_pres_id AS pres_id, 'Inv.' AS type
        FROM patient_test_prescriptions_audit_log ptpal
        JOIN patient_prescription_audit_log ppal ON (ptpal.op_test_pres_id = ppal.patient_presc_id
            AND ppal.field_name = 'visit_id')
        JOIN patient_registration pr ON (pr.patient_id = ppal.visit_id)
        WHERE ptpal.field_name = 'op_test_pres_id'

    UNION ALL
        SELECT pspal.log_id, pspal.user_name, pspal.mod_time, pspal.operation,
            pspal.field_name, pspal.old_value, pspal.new_value, pr.mr_no, pr.patient_id, 
            'patient_prescriptions_details_audit_log_view' ::text AS base_table,
            pspal.op_service_pres_id AS pres_id, 'Service' as type
        FROM patient_service_prescriptions_audit_log pspal
        JOIN patient_prescription_audit_log ppal ON (pspal.op_service_pres_id = ppal.patient_presc_id
            AND ppal.field_name = 'visit_id')
        JOIN patient_registration pr ON (pr.patient_id = ppal.visit_id)
        WHERE pspal.field_name = 'op_service_pres_id'

    UNION ALL
        SELECT pcpal.log_id, pcpal.user_name, pcpal.mod_time, pcpal.operation,
            pcpal.field_name, pcpal.old_value, pcpal.new_value, pr.mr_no, pr.patient_id, 
            'patient_prescriptions_details_audit_log_view' ::text AS base_table,
            pcpal.prescription_id AS pres_id, 'Doctor' as type
        FROM patient_consultation_prescriptions_audit_log pcpal
        JOIN patient_prescription_audit_log ppal ON (pcpal.prescription_id = ppal.patient_presc_id
            AND ppal.field_name = 'visit_id')
        JOIN patient_registration pr ON (pr.patient_id = ppal.visit_id)
        WHERE pcpal.field_name = 'prescription_id'

    UNION ALL
        SELECT popal.log_id, popal.user_name, popal.mod_time, popal.operation,
            popal.field_name, popal.old_value, popal.new_value, pr.mr_no, pr.patient_id, 
            'patient_prescriptions_details_audit_log_view' ::text AS base_table,
            popal.prescription_id AS pres_id, 'Operation' as type
        FROM patient_operation_prescriptions_audit_log popal
        JOIN patient_prescription_audit_log ppal ON (popal.prescription_id = ppal.patient_presc_id
            AND ppal.field_name = 'visit_id')
        JOIN patient_registration pr ON (pr.patient_id = ppal.visit_id)
        WHERE popal.field_name = 'prescription_id'

    UNION ALL
        SELECT popal.log_id, popal.user_name, popal.mod_time, popal.operation,
            popal.field_name, popal.old_value, popal.new_value, pr.mr_no, pr.patient_id, 
            'patient_prescriptions_details_audit_log_view' ::text AS base_table,
            popal.prescription_id AS pres_id,'Others' as type
        FROM patient_other_prescriptions_audit_log popal
        JOIN (SELECT prescription_id from patient_other_prescriptions_audit_log WHERE field_name = 'is_discharge_medication' AND new_value = 'false') AS foo 
        ON (popal.prescription_id = foo.prescription_id)
        JOIN patient_prescription_audit_log ppal ON (popal.prescription_id = ppal.patient_presc_id
            AND ppal.field_name = 'visit_id')
        JOIN patient_registration pr ON (pr.patient_id = ppal.visit_id)
        JOIN patient_prescription pp ON (popal.prescription_id = pp.patient_presc_id)
        WHERE popal.field_name = 'prescription_id'

    UNION ALL
        SELECT pompal.log_id, pompal.user_name, pompal.mod_time, pompal.operation,
            pompal.field_name, pompal.old_value, pompal.new_value, pr.mr_no, pr.patient_id, 
            'patient_prescriptions_details_audit_log_view' ::text AS base_table,
            pompal.prescription_id AS pres_id, 'Medicine' as type
        FROM patient_other_medicine_prescriptions_audit_log pompal
        JOIN (SELECT prescription_id from patient_other_medicine_prescriptions_audit_log WHERE field_name = 'is_discharge_medication' AND new_value = 'false') AS foo 
        ON (pompal.prescription_id = foo.prescription_id)
        JOIN patient_prescription_audit_log ppal ON (pompal.prescription_id = ppal.patient_presc_id
            AND ppal.field_name = 'visit_id')
        JOIN patient_registration pr ON (pr.patient_id = ppal.visit_id)
        WHERE pompal.field_name = 'prescription_id'

    UNION ALL
        SELECT pmpal.log_id, pmpal.user_name, pmpal.mod_time, pmpal.operation,
            pmpal.field_name, pmpal.old_value, pmpal.new_value, pr.mr_no, pr.patient_id, 
            'patient_prescriptions_details_audit_log_view' ::text AS base_table,
            pmpal.op_medicine_pres_id AS pres_id, 'Medicine' as type
        FROM patient_medicine_prescriptions_audit_log pmpal
        JOIN (SELECT op_medicine_pres_id from patient_medicine_prescriptions_audit_log WHERE field_name = 'is_discharge_medication' AND new_value = 'false') AS foo 
        ON (pmpal.op_medicine_pres_id = foo.op_medicine_pres_id)
        JOIN patient_prescription_audit_log ppal ON (pmpal.op_medicine_pres_id = ppal.patient_presc_id
            AND ppal.field_name = 'visit_id')
        JOIN patient_registration pr ON (pr.patient_id = ppal.visit_id)
        WHERE pmpal.field_name = 'op_medicine_pres_id';

DROP VIEW IF EXISTS patient_discharge_medication_ip_audit_log_view CASCADE;
CREATE OR REPLACE VIEW patient_discharge_medication_ip_audit_log_view AS
        SELECT psdal.log_id, psdal.user_name, psdal.mod_time, psdal.operation,
            psdal.field_name, psdal.old_value, psdal.new_value, psdal.mr_no, psdal.patient_id as patient_id,
            'patient_section_detail_audit_log' ::text
            AS base_table, null::integer as pres_id, null::text as type
        FROM patient_section_details_audit_log psdal
        JOIN patient_section_details psd on (psd.section_detail_id=psdal.section_detail_id)
        WHERE psd.section_id=-22 

    UNION ALL
        SELECT popal.log_id, popal.user_name, popal.mod_time, popal.operation,
            popal.field_name, popal.old_value, popal.new_value, pr.mr_no, pr.patient_id, 
            'patient_prescriptions_details_audit_log_view' ::text AS base_table,
            popal.prescription_id AS pres_id, pp.presc_type as type
        FROM patient_other_prescriptions_audit_log popal
        JOIN (SELECT prescription_id from patient_other_prescriptions_audit_log WHERE field_name = 'is_discharge_medication' AND new_value = 'true') AS foo 
        ON (popal.prescription_id = foo.prescription_id)
        JOIN patient_prescription_audit_log ppal ON (popal.prescription_id = ppal.patient_presc_id
            AND ppal.field_name = 'visit_id')
        JOIN patient_registration pr ON (pr.patient_id = ppal.visit_id)
        JOIN patient_prescription pp ON (popal.prescription_id = pp.patient_presc_id)
        WHERE popal.field_name = 'prescription_id'

    UNION ALL
        SELECT pompal.log_id, pompal.user_name, pompal.mod_time, pompal.operation,
            pompal.field_name, pompal.old_value, pompal.new_value, pr.mr_no, pr.patient_id, 
            'patient_prescriptions_details_audit_log_view' ::text AS base_table,
            pompal.prescription_id AS pres_id, 'Medicine' as type
        FROM patient_other_medicine_prescriptions_audit_log pompal
        JOIN (SELECT prescription_id from patient_other_medicine_prescriptions_audit_log WHERE field_name = 'is_discharge_medication' AND new_value = 'true') AS foo 
        ON (pompal.prescription_id = foo.prescription_id)
        JOIN patient_prescription_audit_log ppal ON (pompal.prescription_id = ppal.patient_presc_id
            AND ppal.field_name = 'visit_id')
        JOIN patient_registration pr ON (pr.patient_id = ppal.visit_id)
        WHERE pompal.field_name = 'prescription_id'

    UNION ALL
        SELECT pmpal.log_id, pmpal.user_name, pmpal.mod_time, pmpal.operation,
            pmpal.field_name, pmpal.old_value, pmpal.new_value, pr.mr_no, pr.patient_id, 
            'patient_prescriptions_details_audit_log_view' ::text AS base_table,
            pmpal.op_medicine_pres_id AS pres_id, 'Medicine' as type
        FROM patient_medicine_prescriptions_audit_log pmpal
        JOIN (SELECT op_medicine_pres_id from patient_medicine_prescriptions_audit_log WHERE field_name = 'is_discharge_medication' AND new_value = 'true') AS foo 
        ON (pmpal.op_medicine_pres_id = foo.op_medicine_pres_id)
        JOIN patient_prescription_audit_log ppal ON (pmpal.op_medicine_pres_id = ppal.patient_presc_id
            AND ppal.field_name = 'visit_id')
        JOIN patient_registration pr ON (pr.patient_id = ppal.visit_id)
        WHERE pmpal.field_name = 'op_medicine_pres_id';           

-- Accounting function for posting the reversals for existing/migrated data
-- This function will checks for any existing data(data posted from cron) and posts the reversals for that
-- If job_transaction has
--		 NULL = indicates data is came from cron job and it is older data
--		 -1 = indicates entries created via cron and these entries have corresponding reversal entries posted
--		 -2 = indicates reversal entries posted while migration for cron related data i.e. the the entries with -1

DROP FUNCTION IF EXISTS post_reversals_for_migrated_bill(varchar) CASCADE;
CREATE OR REPLACE FUNCTION post_reversals_for_migrated_bill(billNo varchar) RETURNS integer AS $$
BEGIN
	INSERT INTO hms_accounting_info
	 (center_id, center_name, visit_type, mr_no, visit_id, charge_group, charge_head,
	 account_group, service_group, service_sub_group, bill_no, audit_control_number,
	 voucher_no, voucher_type, voucher_date, item_code, item_name, receipt_store, issue_store,
	 currency, currency_conversion_rate, quantity, unit, unit_rate, gross_amount,
	 round_off_amount, discount_amount, points_redeemed, points_redeemed_rate,
	 points_redeemed_amount, item_category_id, purchase_vat_amount,
	 purchase_vat_percent, sales_vat_amount, sales_vat_percent, debit_account,
	 credit_account, tax_amount, net_amount, admitting_doctor, prescribing_doctor,
	 conductiong_doctor, referral_doctor, payee_doctor, outhouse_name, incoimng_hospital,
	 admitting_department, conducting_department, cost_amount, supplier_name, invoice_no,
	 invoice_date, voucher_ref, remarks, mod_time, counter_no, bill_open_date,
	 bill_finalized_date, is_tpa, insurance_co, old_mr_no, issue_store_center,
	 receipt_store_center, po_number, po_date, transaction_type, custom_1,
	 custom_2, custom_3, custom_4, cust_supplier_code, grn_date, cust_item_code,
	 prescribing_doctor_dept_name, custom_8, custom_9, custom_10, custom_11, guid,
	 update_status, created_at, sale_bill_no, job_transaction, patient_name,voucher_sub_type,
	 ha_item_code,ha_code_type)
	 SELECT center_id, center_name, hai.visit_type, mr_no,
	 hai.visit_id, charge_group, charge_head, hai.account_group, service_group, service_sub_group,
	 hai.bill_no, hai.audit_control_number, voucher_no, voucher_type, voucher_date, item_code,
	 item_name, receipt_store, issue_store, currency, currency_conversion_rate, quantity,
	 unit, unit_rate, gross_amount, round_off_amount, discount_amount, hai.points_redeemed,
	 points_redeemed_rate, points_redeemed_amount, item_category_id, purchase_vat_amount,
	 purchase_vat_percent, sales_vat_amount, sales_vat_percent, credit_account, debit_account,
	 tax_amount, net_amount, admitting_doctor, prescribing_doctor, conductiong_doctor,
	 referral_doctor, payee_doctor, outhouse_name, incoimng_hospital, admitting_department,
	 conducting_department, cost_amount, supplier_name, invoice_no, invoice_date, voucher_ref,
	 hai.remarks, hai.mod_time, counter_no, bill_open_date, bill_finalized_date, hai.is_tpa, insurance_co,
	 old_mr_no, issue_store_center, receipt_store_center, po_number, po_date,
	 CASE WHEN transaction_type='R' THEN 'N' ELSE 'R' END as transaction_type,
	 custom_1, custom_2, custom_3, custom_4, cust_supplier_code,
	 grn_date, cust_item_code, prescribing_doctor_dept_name, custom_8, custom_9, custom_10,
	 custom_11, generate_id('ACCOUNTING_VOUCHER') as guid, update_status,
	 now(), sale_bill_no, -2 AS job_transaction, patient_name,voucher_sub_type,
	 ha_item_code,ha_code_type   
	 FROM hms_accounting_info hai
	 LEFT JOIN bill b ON (b.bill_no=hai.bill_no)
	 WHERE b.status IN ('F','C') AND voucher_type IN ('HOSPBILLS', 'PHBILLS')
	 AND hai.charge_head NOT IN('INVITE','INVRET') AND job_transaction IS NULL
	 AND b.bill_no=billNo;

	 UPDATE hms_accounting_info hai set job_transaction = -1
	 FROM bill b WHERE b.bill_no=hai.bill_no
	 AND b.status IN ('F','C') AND voucher_type IN ('HOSPBILLS', 'PHBILLS')
	 AND hai.charge_head NOT IN('INVITE','INVRET') AND job_transaction IS NULL
	 AND b.bill_no=billNo;

	 return 1;
END;
$$ LANGUAGE plpgsql;


DROP FUNCTION IF EXISTS insert_iv_administered_details() CASCADE;
CREATE OR REPLACE FUNCTION insert_iv_administered_details() RETURNS trigger AS $$
BEGIN
	INSERT INTO patient_iv_administered_details(activity_id, state, username, mod_time, remarks)
	VALUES(OLD.activity_id, NEW.iv_status, NEW.username, NEW.mod_time, NEW.activity_remarks);
    RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS iv_administered_details_trigger on patient_activities;
CREATE TRIGGER iv_administered_details_trigger
  AFTER UPDATE
  ON patient_activities
  FOR EACH ROW
  WHEN (OLD.iv_status <> NEW.iv_status or (NEW.iv_status is not NULL and OLD.iv_status is NULL))
  EXECUTE PROCEDURE insert_iv_administered_details();

DROP VIEW IF EXISTS stock_take_audit_view CASCADE;

CREATE OR REPLACE VIEW stock_take_audit_view AS
  SELECT pal.log_id, 'physical_stock_take_audit_log'::text as base_table,
    pal.stock_take_id, NULL::numeric AS stock_take_detail_id,
    NULL::numeric AS item_batch_id, NULL::character varying AS medicine_name,
    pal.user_name, pal.mod_time, pal.operation,
    'stock_take_'::text || pal.field_name::text AS field_name,
    pal.old_value, pal.new_value
    FROM physical_stock_take_audit_log pal
  
  UNION ALL
  
  SELECT pdal.log_id, 'stock_take_detail_audit_log'::text AS base_table,
    pdal.stock_take_id, pdal.stock_take_detail_id,
    pdal.item_batch_id, sid.medicine_name, pdal.user_name,
    pdal.mod_time, pdal.operation,
    'stock_take_detail_'::text || pdal.field_name::text AS field_name,
    pdal.old_value, pdal.new_value
  FROM physical_stock_take_detail_audit_log pdal 
    LEFT JOIN store_item_batch_details sibd ON (pdal.item_batch_id = sibd.item_batch_id)
    LEFT JOIN store_item_details sid ON (sibd.medicine_id = sid.medicine_id)
  WHERE pdal.operation::text = 'INSERT'::text 
    AND pdal.field_name::text = 'stock_take_detail_id'::text;

-- New mapping trigger to update orderable item for each inserted package in packages entry.
-- Not considered Template Packages, with type='T' since they are migrated to order_sets already.
-- NOTE:Trigger to be created after migration is successful since the existing packages are already inserted into orderable_items.


CREATE OR REPLACE FUNCTION packages_orderable_item_mapping_procedure()
   RETURNS TRIGGER as $$
BEGIN
    IF (TG_OP = 'INSERT' AND NEW.type='P' AND (NEW.package_category_id=-2 OR NEW.package_category_id=-3) AND NEW.multi_visit_package = false) THEN
       INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, item_codes, status, valid_from_date, valid_to_date,gender_applicability,min_age,max_age,age_unit)
        SELECT DISTINCT NEW.package_id::text as entity_id, 'DiagPackage' as entity, lower(NEW.package_name) as item_name,
        'mod_basic' as module_id, 'Y' as orderable, 'N' as operation_applicable,
        'Y' as package_applicable, 'N' as is_multi_visit_package, NEW.service_sub_group_id, NEW.insurance_category_id,
        '*' as visit_type,
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Package') as direct_billing,
        ssg.service_group_id as service_group_id, lower(concat(NEW.package_code, ' ', NEW.order_code)) as item_codes,
        CASE WHEN (NEW.status = 'A' AND  NEW.approval_status='A' ) THEN 'A' ELSE 'I' END as status,
        pm.valid_from as valid_from_date, pm.valid_till as valid_to_date,
        pm.gender_applicability,pm.min_age,pm.max_age,pm.age_unit
        FROM packages pm
        JOIN service_sub_groups ssg using(service_sub_group_id)
        WHERE pm.package_id = NEW.package_id;

    ELSEIF (TG_OP = 'INSERT' AND NEW.type='P' AND (NEW.visit_applicability = 'o' OR NEW.visit_applicability = 'i' OR NEW.visit_applicability = '*') AND NEW.multi_visit_package = false) THEN
       INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, item_codes, status, valid_from_date, valid_to_date,gender_applicability,min_age,max_age,age_unit)
        SELECT DISTINCT NEW.package_id::text as entity_id, 'Package' as entity, lower(NEW.package_name) as item_name,
        'mod_basic' as module_id, 'Y' as orderable, 'N' as operation_applicable,
        'Y' as package_applicable, 'N' as is_multi_visit_package, NEW.service_sub_group_id, NEW.insurance_category_id,
        CASE WHEN (NEW.visit_applicability = 'i') THEN 'i'
             WHEN (NEW.visit_applicability = 'o') THEN 'o' END as visit_type,
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'Package') as direct_billing,
        ssg.service_group_id as service_group_id, lower(concat(NEW.package_code, ' ', NEW.order_code)) as item_codes,
        CASE WHEN (NEW.status = 'A' AND  NEW.approval_status='A' ) THEN 'A' ELSE 'I' END as status,
        pm.valid_from as valid_from_date, pm.valid_till as valid_to_date,
        pm.gender_applicability,pm.min_age,pm.max_age,pm.age_unit
        FROM packages pm
        JOIN service_sub_groups ssg using(service_sub_group_id)
        WHERE pm.package_id = NEW.package_id;

    ELSEIF (TG_OP = 'INSERT' AND NEW.type='P' AND NEW.multi_visit_package = true) THEN
      INSERT INTO orderable_item( entity_id, entity, item_name, module_id, orderable, operation_applicable, package_applicable, is_multi_visit_package, service_sub_group_id, insurance_category_id, visit_type, direct_billing, service_group_id, item_codes, status, valid_from_date, valid_to_date,gender_applicability,min_age,max_age,age_unit)
        SELECT DISTINCT NEW.package_id::text as entity_id, 'MultiVisitPackage' as entity, lower(NEW.package_name) as item_name,
        'mod_adv_packages' as module_id, 'Y' as orderable, 'N' as operation_applicable,
        'N' as package_applicable, 'N' as is_multi_visit_package, NEW.service_sub_group_id, NEW.insurance_category_id, 'o' as visit_type,
        (SELECT orderable from hosp_direct_bill_prefs WHERE item_type = 'MultiVisitPackage') as direct_billing,
        ssg.service_group_id as service_group_id, lower(concat(NEW.package_code, ' ', NEW.order_code)) as item_codes,
        CASE WHEN (NEW.status = 'A' AND  NEW.approval_status='A' ) THEN 'A' ELSE 'I' END as status,
        pm.valid_from as valid_from_date, pm.valid_till as valid_to_date,
        pm.gender_applicability,pm.min_age,pm.max_age,pm.age_unit
        FROM packages pm
        JOIN service_sub_groups ssg using(service_sub_group_id)
        WHERE pm.package_id = NEW.package_id;

    ELSIF (TG_OP = 'UPDATE' AND NEW.type='P') THEN
        UPDATE orderable_item SET item_name = lower(NEW.package_name), insurance_category_id = NEW.insurance_category_id, service_sub_group_id = NEW.service_sub_group_id, service_group_id = ssg.service_group_id,
        item_codes = lower(concat(NEW.package_code, ' ', NEW.order_code)), status = (CASE WHEN (NEW.status = 'A' AND  NEW.approval_status='A' ) THEN 'A' ELSE 'I' END),
        valid_from_date = NEW.valid_from, valid_to_date = NEW.valid_till,gender_applicability = NEW.gender_applicability,
        min_age = NEW.min_age, max_age = NEW.max_age, age_unit = NEW.age_unit
        FROM service_sub_groups ssg
        WHERE ssg.service_sub_group_id = NEW.service_sub_group_id AND entity_id = NEW.package_id::text;
    END IF;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;


DROP TRIGGER IF EXISTS packages_orderable_item_mapping_trigger ON packages CASCADE;
CREATE TRIGGER packages_orderable_item_mapping_trigger
    AFTER INSERT OR UPDATE ON packages
    FOR EACH ROW
    EXECUTE PROCEDURE packages_orderable_item_mapping_procedure();


CREATE OR REPLACE FUNCTION center_package_applicability_procedure()
    RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO mapping_center_id (orderable_item_id, center_id, status)
            SELECT DISTINCT oi.orderable_item_id, NEW.center_id, NEW.status
            FROM orderable_item oi
            WHERE oi.entity_id = NEW.package_id::text;
        RETURN NEW;
    ELSIF (TG_OP = 'UPDATE') THEN
        UPDATE mapping_center_id mci SET status = NEW.status
        FROM orderable_item oi
        WHERE oi.entity_id = NEW.package_id::text AND center_id = NEW.center_id AND oi.orderable_item_id = mci.orderable_item_id;
        RETURN NEW;
    ELSIF (TG_OP = 'DELETE') THEN
        DELETE FROM mapping_center_id mci
        USING orderable_item oi
        WHERE oi.entity_id = OLD.package_id::text AND mci.center_id = OLD.center_id AND oi.orderable_item_id = mci.orderable_item_id;
        RETURN OLD;
    END IF;
END;
$$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS center_package_applicability_mapping_trigger ON center_package_applicability CASCADE;
CREATE TRIGGER center_package_applicability_mapping_trigger
    AFTER INSERT OR UPDATE OR DELETE ON center_package_applicability
    FOR EACH ROW
    EXECUTE PROCEDURE center_package_applicability_procedure();

--Multivist Deposit View Center Wise
DROP VIEW IF EXISTS multivisit_deposits_center_wise_view CASCADE;
CREATE OR REPLACE VIEW  multivisit_deposits_center_wise_view AS SELECT deposits.mr_no,
    deposits.package_id,
    COALESCE(deposits.total_deposits,0::numeric) AS total_deposits,
    COALESCE(setoffs.total_set_offs,0::numeric) AS total_set_offs,
    deposits.center_id, deposits.pat_package_id, package_unallocated_amount
   FROM ( SELECT pd.mr_no,pd.center_id,
            pd.package_id::integer AS package_id, pd.pat_package_id::integer,
            sum(pd.amount) AS total_deposits,
            SUM(CASE WHEN pd.unallocated_amount>0 THEN pd.unallocated_amount ELSE 0 END)AS package_unallocated_amount 
           FROM patient_package_deposits_view pd
          GROUP BY pd.mr_no, pd.center_id,pd.package_id, pd.pat_package_id) deposits
     LEFT JOIN LATERAL ( SELECT r.mr_no,rcp.center_id,
            package_bills.package_id,
            sum(br.allocated_amount) AS total_set_offs,
            ru.entity_id AS pat_package_id
            FROM bill_receipts br
            JOIN receipts rcp ON br.receipt_no=rcp.receipt_id AND is_deposit=true
            join receipt_usage ru ON rcp.receipt_id = ru.receipt_id and ru.entity_type = 'pat_package_id'
            JOIN bill b ON br.bill_no=b.bill_no
             JOIN patient_registration r ON b.visit_id::text = r.patient_id::text
             JOIN patient_details p ON p.mr_no::text = r.mr_no::text
             JOIN LATERAL ( SELECT DISTINCT bc.bill_no,
                    orders.package_id
                   FROM bill_charge bc
                     JOIN ( SELECT sp.common_order_id,
                            p_1.package_id
                           FROM services_prescribed sp
                             JOIN package_prescribed pp ON sp.package_ref = pp.prescription_id AND pp.mr_no::text = deposits.mr_no::text
                             JOIN packages p_1 ON pp.package_id = p_1.package_id AND p_1.multi_visit_package = true
                        UNION ALL
                         SELECT tp.common_order_id,
                            p_1.package_id
                           FROM tests_prescribed tp
                             JOIN package_prescribed pp ON tp.package_ref = pp.prescription_id AND pp.mr_no::text = deposits.mr_no::text
                             JOIN packages p_1 ON pp.package_id = p_1.package_id AND p_1.multi_visit_package = true
                        UNION ALL
                         SELECT dc.common_order_id,
                            p_1.package_id
                         FROM doctor_consultation dc
                             JOIN package_prescribed pp ON dc.package_ref = pp.prescription_id AND pp.mr_no::text = deposits.mr_no::text
                             JOIN packages p_1 ON pp.package_id = p_1.package_id AND p_1.multi_visit_package = true
                             LEFT JOIN bill_activity_charge bac ON bac.activity_id::text = dc.consultation_id::text AND bac.activity_code::text = 'DOC'::text
                             LEFT JOIN bill_charge bc_1 ON bc_1.charge_id::text = bac.charge_id::text
                        UNION ALL
                         SELECT osp.common_order_id,
                            p_1.package_id
                           FROM other_services_prescribed osp
                             JOIN package_prescribed pp ON osp.package_ref = pp.prescription_id AND pp.mr_no::text = deposits.mr_no::text
                             JOIN packages p_1 ON pp.package_id = p_1.package_id AND p_1.multi_visit_package = true) orders ON bc.order_number = orders.common_order_id WHERE bc.bill_no = b.bill_no)
                              package_bills ON TRUE 
          WHERE patient_confidentiality_check(COALESCE(p.patient_group, 0), p.mr_no) AND deposits.mr_no::text = r.mr_no::text
          GROUP BY ru.entity_id,r.mr_no, package_bills.package_id,rcp.center_id) setoffs ON deposits.mr_no::text = setoffs.mr_no::text AND deposits.pat_package_id::text = setoffs.pat_package_id::text and deposits.package_id = setoffs.package_id
          AND setoffs.center_id=deposits.center_id;

          
--Refund Allocation Helper - Refund Reference amount and Allocated amount view      
DROP VIEW IF EXISTS refund_reference_allocation_view ;
CREATE OR REPLACE VIEW refund_reference_allocation_view AS
	SELECT
		br.bill_no,
		br.bill_receipt_id,
		rrr.id,
		rrr.refund_receipt_id,
		rrr.receipt_id,
		rrr.amount + COALESCE(
			rrr.tax_amount, 0) AS amount,
		COALESCE(bcra.allocated_amount,0) AS allocated_amount
	FROM
		receipt_refund_reference rrr
		JOIN bill_receipts br ON rrr.receipt_id = br.receipt_no
		LEFT OUTER JOIN LATERAL (
			SELECT
				br.bill_no,
				br.bill_receipt_id,
				COALESCE(SUM(bcra.allocated_amount) * - 1, 0) AS allocated_amount
			FROM
				bill_receipts br
				JOIN bill_charge_receipt_allocation bcra ON bcra.bill_receipt_id = br.bill_receipt_id
			WHERE
				rrr.receipt_id = br.receipt_no
				AND bcra.allocated_amount < 0
				AND bcra.refund_reference_id = rrr.id
			GROUP BY
				br.bill_receipt_id) AS bcra ON TRUE
	ORDER BY rrr.id; 
	
	
DROP FUNCTION IF EXISTS patient_search_token_regenerate_trigger() CASCADE;
CREATE FUNCTION patient_search_token_regenerate_trigger() RETURNS TRIGGER AS $BODY$
DECLARE
	token_changed boolean;
BEGIN
	token_changed := TG_OP = 'UPDATE' AND (
			NEW.patient_name != OLD.patient_name 
			OR NEW.middle_name != OLD.middle_name 
			OR NEW.last_name != OLD.last_name 
			OR NEW.government_identifier != OLD.government_identifier 
			OR NEW.patient_phone_country_code != OLD.patient_phone_country_code 
			OR NEW.patient_phone != OLD.patient_phone 
			OR NEW.oldmrno != OLD.oldmrno 
	);
    IF (TG_OP = 'DELETE' OR token_changed) THEN
        DELETE FROM patient_search_tokens WHERE entity_id = OLD.mr_no 
			AND entity = 'patient_details';
    END IF;
    IF (token_changed OR TG_OP = 'INSERT') THEN
		INSERT INTO patient_search_tokens (SELECT NEW.mr_no,'patient_details', s.token, false FROM unnest(string_to_array(regexp_replace(btrim(lower(concat_ws(' ', NEW.patient_name, NEW.middle_name, NEW.last_name))), '(\s|\t)+', ' ','g'), ' ')) s(token));
        INSERT INTO patient_search_tokens values (NEW.mr_no,'patient_details', lower(NEW.mr_no), false);
        INSERT INTO patient_search_tokens values (NEW.mr_no,'patient_details', reverse(lower(NEW.mr_no)), true);
		IF NEW.oldmrno IS NOT NULL AND NEW.oldmrno != '' THEN
			INSERT INTO patient_search_tokens values (NEW.mr_no,'patient_details', lower(NEW.oldmrno), false);
			INSERT INTO patient_search_tokens values (NEW.mr_no,'patient_details', reverse(lower(NEW.oldmrno)), true);
		END IF;
		IF NEW.government_identifier IS NOT NULL AND NEW.government_identifier != '' THEN
			INSERT INTO patient_search_tokens values (NEW.mr_no,'patient_details', lower(NEW.government_identifier), false);
		END IF;
		IF NEW.patient_phone IS NOT NULL AND NEW.patient_phone != '' THEN
	        INSERT INTO patient_search_tokens values (NEW.mr_no,'patient_details', NEW.patient_phone, false);
		END IF;
		IF NEW.patient_phone_country_code IS NOT NULL AND NEW.patient_phone_country_code != '' THEN
	        INSERT INTO patient_search_tokens values (NEW.mr_no,'patient_details', replace(NEW.patient_phone, NEW.patient_phone_country_code, ''), false);
		END IF;
    END IF;

    RETURN NEW;

END;
$BODY$ language plpgsql;

DROP TRIGGER IF EXISTS patient_details_token_trigger ON patient_details CASCADE;
CREATE TRIGGER patient_details_token_trigger
    AFTER INSERT OR UPDATE OR DELETE ON patient_details
    FOR EACH ROW
    EXECUTE PROCEDURE patient_search_token_regenerate_trigger();


DROP FUNCTION IF EXISTS contact_search_token_regenerate_trigger() CASCADE;
CREATE FUNCTION contact_search_token_regenerate_trigger() RETURNS TRIGGER AS $BODY$
DECLARE
	token_changed boolean;
BEGIN
	token_changed := TG_OP = 'UPDATE' AND (
			NEW.patient_name != OLD.patient_name 
			OR NEW.middle_name != OLD.middle_name 
			OR NEW.last_name != OLD.last_name 
			OR NEW.patient_contact != OLD.patient_contact
	);
    IF (TG_OP = 'DELETE' OR token_changed) THEN
        DELETE FROM patient_search_tokens 
			WHERE entity_id = OLD.contact_id::varchar 
			AND entity='contact_details';
    END IF;
    IF (token_changed OR TG_OP = 'INSERT') THEN
		INSERT INTO patient_search_tokens (SELECT NEW.contact_id::varchar,'contact_details', s.token, false FROM unnest(string_to_array(regexp_replace(btrim(lower(concat_ws(' ', NEW.patient_name, NEW.middle_name, NEW.last_name))), '(\s|\t)+', ' ','g'), ' ')) s(token));
		IF NEW.patient_contact IS NOT NULL AND NEW.patient_contact != '' THEN
	        INSERT INTO patient_search_tokens values (NEW.contact_id::varchar,'contact_details', NEW.patient_contact, false);
		END IF;
    END IF;

    RETURN NEW;

END;
$BODY$ language plpgsql;

DROP TRIGGER IF EXISTS contact_details_token_trigger ON contact_details CASCADE;
CREATE TRIGGER contact_details_token_trigger
    AFTER INSERT OR UPDATE OR DELETE ON contact_details
    FOR EACH ROW
    EXECUTE PROCEDURE contact_search_token_regenerate_trigger();
