-- liquibase formatted sql
-- changeSET AdeshAtole:coder-claim-review-migration-for-new-masters

-- migration quieries to migrate message 
INSERT INTO review_categories (category_name, category_type, send_email,status, created_by) SELECT DISTINCT (review_category), CASE WHEN review_category='physician' THEN 'P' ELSE 'NP' END, 'Y', 'A', 'InstaAdmin' FROM review_types;
UPDATE review_types SET review_category_id = (SELECT category_id from review_categories WHERE category_name = review_category);

INSERT INTO review_category_role (category_id, role_id, created_by) 
SELECT DISTINCT rc.category_id, mtr.role_id, 'InstaAdmin' FROM review_categories rc, review_types rt, codification_message_type_role mtr
WHERE rc.category_id = rt.review_category_id AND mtr.message_type_id = rt.review_type_id;

INSERT INTO review_type_center_role  (review_type_id,center_id,role_id, created_by)  SELECT message_type_id, 0 AS center_id, role_id, 'InstaAdmin' AS created_by FROM codification_message_type_role;