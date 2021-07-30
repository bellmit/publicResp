-- liquibase formatted sql
-- changeset shilpanr:insert-category-copay-details-into-patient-insurance-plan-details-table
INSERT INTO patient_insurance_plan_details(visit_id,plan_id,insurance_category_id,patient_amount,patient_percent,
	patient_amount_cap,per_treatment_limit,patient_type,patient_amount_per_category,patient_insurance_plans_id)
(SELECT pip.patient_id,pip.plan_id,ipd.insurance_category_id,ipd.patient_amount,ipd.patient_percent,
	ipd.patient_amount_cap,ipd.per_treatment_limit,pr.visit_type,ipd.patient_amount_per_category,pip.patient_insurance_plans_id
FROM patient_insurance_plans pip
JOIN patient_registration pr ON(pip.patient_id = pr.patient_id)
JOIN insurance_plan_details ipd ON(ipd.plan_id = pip.plan_id AND pr.visit_type = ipd.patient_type)
WHERE NOT EXISTS(SELECT visit_id, plan_id, insurance_category_id, patient_type FROM patient_insurance_plan_details id
					WHERE id. visit_id = pip.patient_id AND id.plan_id = pip.plan_id 
					AND id.insurance_category_id = ipd.insurance_category_id
					AND id.patient_type = pr.visit_type)
AND pip.patient_id NOT IN(SELECT pip.patient_id FROM patient_insurance_plans pip 
							JOIN patient_registration pr using(patient_id)  
						  GROUP BY pip.plan_id,pip.patient_id HAVING COUNT(pip.plan_id)>1));