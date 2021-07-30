-- liquibase formatted sql
-- changeset rajendratalekar:handle-bmi-expression-evaluation

ALTER TABLE vital_parameter_master ALTER COLUMN expr_for_calc_result TYPE character varying(300);

UPDATE vital_parameter_master SET expr_for_calc_result = '${results["Height"]?has_content?then(results["Weight"]/(results["Height"]*results["Height"]/10000),"")}' WHERE expr_for_calc_result = '${results["Weight"]/((results["Height"]/100)*(results["Height"]/100))}';
UPDATE vital_parameter_master SET expr_for_calc_result = '${results["Height."]?has_content?then(results["Weight."]/(results["Height."]*results["Height."]/10000),"")}' WHERE expr_for_calc_result = '${results["Weight."]/((results["Height."]/100)*(results["Height."]/100))}';
