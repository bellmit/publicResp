DROP VIEW IF EXISTS scm_inbound_data_template CASCADE;
CREATE VIEW scm_inbound_data_template AS

SELECT psm.store_id, s.dept_name as store_name,psm.date_time AS txn_date, psm.sale_id::character varying AS txn_ref, sibd.batch_no, psm.username AS user_name, ps.medicine_id,
sibd.exp_dt,psm.bill_no as bill_number,
        CASE
            WHEN psm.type = 'S'::bpchar THEN 'Sales'::text
            ELSE 'Sales Returns'::text
        END AS txn_type, 0 AS bonus_qty, 0::numeric - (trunc(sum(ps.quantity)/issue_base_unit,4)) AS qty,
        0::numeric - (trunc(sum(ps.quantity)/issue_base_unit,4)) AS total_qty,
        COALESCE(patient_details.patient_name::text ||
        CASE
            WHEN b.visit_type = 'o'::bpchar THEN ' (OP)'::text
            ELSE ' (IP)'::text
        END, store_retail_customers.customer_name::text || ' (Retail)'::text) AS details, sibd.mrp, issue_base_unit,sid.package_uom,
        sic.item_code,sid.item_barcode_id,
 		sid.package_uom as item_unit,hcm.center_name,
 		'S'::character varying AS txn_type_prifix,ps.sale_item_id as txn_detail_ref,null as department
   FROM store_sales_main psm
   JOIN store_sales_details ps USING (sale_id)
   JOIN bill b USING (bill_no)
   JOIN store_item_details  sid USING (medicine_id)
   JOIN store_item_batch_details sibd ON(sibd.item_batch_id = ps.item_batch_id)
   JOIN stores s ON(s.dept_id = psm.store_id)
   LEFT JOIN patient_registration pr ON b.visit_id::text = pr.patient_id::text
   LEFT JOIN patient_details USING (mr_no)
   LEFT JOIN store_retail_customers ON b.visit_id::text = store_retail_customers.customer_id::text
   JOIN hospital_center_master hcm ON (hcm.center_id = s.center_id)
   LEFT JOIN ha_item_code_type hict ON (sid.medicine_id = hict.medicine_id AND hict.health_authority = hcm.health_authority)
   LEFT JOIN store_item_codes sic ON (hict.medicine_id = sic.medicine_id AND hict.code_type = sic.code_type)
   WHERE psm.type = 'S'
  GROUP BY psm.store_id, psm.date_time, psm.sale_id, psm.type, psm.username, patient_details.patient_name,
  		   b.visit_type, store_retail_customers.customer_name, sibd.batch_no, ps.medicine_id,
  		   sibd.mrp, issue_base_unit,package_uom,sic.item_code,sid.item_barcode_id,
 		sid.package_uom,hcm.center_name,ps.sale_item_id,sibd.exp_dt,psm.bill_no,s.dept_name
UNION
SELECT psm.store_id, s.dept_name as store_name,psm.date_time AS txn_date, psm.sale_id::character varying AS txn_ref, sibd.batch_no, psm.username AS user_name, ps.medicine_id,
sibd.exp_dt,psm.bill_no as bill_number,
        CASE
            WHEN psm.type = 'S'::bpchar THEN 'Sales'::text
            ELSE 'Sales Returns'::text
        END AS txn_type, 0 AS bonus_qty, 0::numeric -  (trunc(sum(ps.quantity)/issue_base_unit,4))  AS qty,
        0::numeric - (trunc(sum(ps.quantity)/issue_base_unit,4)) AS total_qty,
        COALESCE(patient_details.patient_name::text ||
        CASE
            WHEN b.visit_type = 'o'::bpchar THEN ' (OP)'::text
            ELSE ' (IP)'::text
        END, store_retail_customers.customer_name::text || ' (Retail)'::text) AS details, sibd.mrp, issue_base_unit,sid.package_uom,
        sic.item_code,sid.item_barcode_id,
 		sid.package_uom as item_unit,hcm.center_name,
 		'SR'::character varying AS txn_type_prifix,ps.sale_item_id as txn_detail_ref,null as department
   FROM store_sales_main psm
   JOIN store_sales_details ps USING (sale_id)
   JOIN bill b USING (bill_no)
   JOIN store_item_details  sid USING (medicine_id)
   JOIN store_item_batch_details sibd ON(sibd.item_batch_id = ps.item_batch_id)
   JOIN stores s ON(s.dept_id = psm.store_id)
   LEFT JOIN patient_registration pr ON b.visit_id::text = pr.patient_id::text
   LEFT JOIN patient_details USING (mr_no)
   LEFT JOIN store_retail_customers ON b.visit_id::text = store_retail_customers.customer_id::text
   JOIN hospital_center_master hcm ON (hcm.center_id = s.center_id)
   LEFT JOIN ha_item_code_type hict ON (sid.medicine_id = hict.medicine_id AND hict.health_authority = hcm.health_authority)
   LEFT JOIN store_item_codes sic ON (hict.medicine_id = sic.medicine_id AND hict.code_type = sic.code_type)
   WHERE psm.type = 'R'
  GROUP BY psm.store_id, psm.date_time, psm.sale_id, psm.type, psm.username, patient_details.patient_name,
  		   b.visit_type, store_retail_customers.customer_name, sibd.batch_no, ps.medicine_id,
  		   sibd.mrp, issue_base_unit,package_uom,sic.item_code,sid.item_barcode_id,
 		sid.package_uom,hcm.center_name,ps.sale_item_id,sibd.exp_dt,psm.bill_no,s.dept_name
UNION
 SELECT isum.dept_from AS store_id,s.dept_name as store_name, isum.date_time AS txn_date, isum.user_issue_no::character varying AS txn_ref,
 		sibd.batch_no, isum.username AS user_name, isu.medicine_id,sibd.exp_dt,bc.bill_no as bill_number,
 		CASE
 			WHEN isum.user_type = 'Hospital' THEN 'User Issues'
 			ELSE 'Patient Issues'
 		END AS txn_type, 0 AS bonus_qty, - (trunc(sum(isu.qty)/issue_base_unit,4)) AS qty,
 		- (trunc(sum(isu.qty)/issue_base_unit,4)) AS total_qty, isum.issued_to AS details,
 		sibd.mrp, issue_base_unit,sid.package_uom,sic.item_code,sid.item_barcode_id,
 		sid.package_uom as item_unit,hcm.center_name,
 		CASE
 			WHEN isum.user_type = 'Hospital' THEN 'UI'::character varying
 			ELSE 'PI'::character varying
 		END AS txn_type_prifix,item_issue_no as txn_detail_ref,
 		CASE
 			WHEN isum.user_type = 'Hospital' THEN isum.issued_to ELSE null END AS department
   FROM stock_issue_main isum
   JOIN stock_issue_details isu USING (user_issue_no)
   JOIN store_item_details sid USING (medicine_id)
   JOIN store_item_batch_details sibd ON(sibd.item_batch_id = isu.item_batch_id)
   JOIN stores s ON(s.dept_id = isum.dept_from)
   JOIN hospital_center_master hcm ON (hcm.center_id = s.center_id)
   LEFT JOIN ha_item_code_type hict ON (sid.medicine_id = hict.medicine_id AND hict.health_authority = hcm.health_authority)
   LEFT JOIN store_item_codes sic ON (hict.medicine_id = sic.medicine_id AND hict.code_type = sic.code_type)
   LEFT JOIN bill_activity_charge bac ON(isu.item_issue_no ::character varying = bac.activity_id AND payment_charge_head='INVITE')
   LEFT JOIN bill_charge bc ON(bc.charge_id = bac.charge_id)
  GROUP BY isum.dept_from, isum.date_time, isum.user_issue_no, isum.username, isum.issued_to, sibd.batch_no,
  		isu.medicine_id, sibd.mrp, isum.user_type, issue_base_unit,sid.package_uom,item_unit,bc.bill_no ,
  		sic.item_code,sid.item_barcode_id,sid.issue_units,package_uom,hcm.center_name,s.dept_name,sibd.exp_dt,item_issue_no
UNION
 SELECT isurm.dept_to AS store_id,s.dept_name as store_name, isurm.date_time AS txn_date, isurm.user_return_no::character varying AS txn_ref,
 		sibd.batch_no, isurm.username AS user_name, isur.medicine_id,sibd.exp_dt,bc.bill_no as bill_number,
 		CASE
 			WHEN sim.user_type = 'Hospital' THEN 'User Returns'
 			ELSE 'Patient Returns'
 		END AS txn_type, 0 AS bonus_qty, trunc(sum(isur.qty)/issue_base_unit,4) AS qty,
 		trunc(sum(isur.qty)/issue_base_unit,4) AS total_qty, isurm.returned_by AS details,
 		sibd.mrp,issue_base_unit,package_uom,sic.item_code,sid.item_barcode_id,
 		sid.package_uom as item_unit
 		,hcm.center_name,CASE
 			WHEN sim.user_type = 'Hospital' THEN 'UR'::character varying
 			ELSE 'PR'::character varying
 		END AS txn_type_prifix,item_return_no as txn_detail_ref,
 		CASE
 			WHEN sim.user_type = 'Hospital' THEN isurm.returned_by ELSE null END AS department
   FROM store_issue_returns_main isurm
   JOIN store_issue_returns_details isur USING (user_return_no)
   JOIN store_item_details sid USING (medicine_id)
   JOIN store_item_batch_details sibd ON(sibd.item_batch_id = isur.item_batch_id)
   JOIN stores s ON(s.dept_id = isurm.dept_to)
   JOIN hospital_center_master hcm ON (hcm.center_id = s.center_id)
   LEFT JOIN ha_item_code_type hict ON (sid.medicine_id = hict.medicine_id AND hict.health_authority = hcm.health_authority)
   LEFT JOIN store_item_codes sic ON (hict.medicine_id = sic.medicine_id AND hict.code_type = sic.code_type)
   LEFT JOIN bill_activity_charge bac ON(isur.item_return_no ::character varying = bac.activity_id AND payment_charge_head='INVRET')
   LEFT JOIN bill_charge bc ON(bc.charge_id = bac.charge_id)
   LEFT JOIN stock_issue_main sim ON (sim.user_issue_no = isurm.user_issue_no)
  GROUP BY isurm.dept_to, isurm.date_time, isurm.user_return_no, isurm.username,isurm.returned_by,bc.bill_no,
  		sibd.batch_no, isur.medicine_id, sibd.mrp,sim.user_type,issue_base_unit,package_uom,sic.item_code,
  		sid.item_barcode_id,sid.issue_units,package_uom,hcm.center_name,s.dept_name,sibd.exp_dt,item_unit,item_return_no
UNION
 SELECT psm.store_id, s.dept_name as store_name,psm.date_time AS txn_date, psm.adj_no::character varying AS txn_ref,
 		sibd.batch_no, psm.username AS user_name, ps.medicine_id,sibd.exp_dt,null as bill_number,
 		 'Adjustment Decrease' AS txn_type, 0 AS bonus_qty, trunc(sum(ps.qty)/issue_base_unit,4) AS qty,
        CASE
            WHEN ps.type = 'A'::bpchar THEN trunc(sum(ps.qty)/issue_base_unit,4)
            WHEN ps.type = 'R'::bpchar THEN - trunc(sum(ps.qty)/issue_base_unit,4)
            ELSE trunc(sum(ps.qty)/issue_base_unit,4)::numeric
        END AS total_qty, description AS details, sibd.mrp,issue_base_unit,package_uom,sic.item_code,
        sid.item_barcode_id,sid.package_uom as item_unit,hcm.center_name,'AD'::character varying as txn_type_prifix,
        adj_detail_no as txn_detail_ref,null as department
   FROM store_adj_main psm
   JOIN store_adj_details ps USING (adj_no)
   JOIN store_item_details sid USING (medicine_id)
   JOIN store_item_batch_details sibd ON(sibd.item_batch_id = ps.item_batch_id)
   JOIN stores s ON(s.dept_id = psm.store_id)
   JOIN hospital_center_master hcm ON (hcm.center_id = s.center_id)
   LEFT JOIN ha_item_code_type hict ON (sid.medicine_id = hict.medicine_id AND hict.health_authority = hcm.health_authority)
   LEFT JOIN store_item_codes sic ON (hict.medicine_id = sic.medicine_id AND hict.code_type = sic.code_type)
   WHERE ps.type = 'R'
  GROUP BY psm.store_id, psm.date_time, psm.adj_no, psm.username, description, ps.type, sibd.batch_no,sid.issue_units,
  		ps.medicine_id, sibd.mrp, issue_base_unit,package_uom,sic.item_code,sid.item_barcode_id,hcm.center_name,s.dept_name,
  		sibd.exp_dt,adj_detail_no

 UNION
 SELECT psm.store_id,s.dept_name as store_name, psm.date_time AS txn_date, psm.adj_no::character varying AS txn_ref,
 		sibd.batch_no, psm.username AS user_name, ps.medicine_id,sibd.exp_dt,null as bill_number,
 		 'Adjustment Increase' AS txn_type, 0 AS bonus_qty, trunc(sum(ps.qty)/issue_base_unit,4) AS qty,
        CASE
            WHEN ps.type = 'A'::bpchar THEN trunc(sum(ps.qty)/issue_base_unit,4)
            WHEN ps.type = 'R'::bpchar THEN - (trunc(sum(ps.qty)/issue_base_unit,4))
            ELSE trunc(sum(ps.qty)/issue_base_unit,4)::numeric
        END AS total_qty, description AS details, sibd.mrp,issue_base_unit,package_uom,sic.item_code,
        sid.item_barcode_id,sid.package_uom as item_unit,hcm.center_name,'AI'::character varying as txn_type_prifix,
        adj_detail_no as txn_detail_ref,null as department
   FROM store_adj_main psm
   JOIN store_adj_details ps USING (adj_no)
   JOIN store_item_details sid USING (medicine_id)
   JOIN store_item_batch_details sibd ON(sibd.item_batch_id = ps.item_batch_id)
   JOIN stores s ON(s.dept_id = psm.store_id)
   JOIN hospital_center_master hcm ON (hcm.center_id = s.center_id)
   LEFT JOIN ha_item_code_type hict ON (sid.medicine_id = hict.medicine_id AND hict.health_authority = hcm.health_authority)
   LEFT JOIN store_item_codes sic ON (hict.medicine_id = sic.medicine_id AND hict.code_type = sic.code_type)
   WHERE ps.type = 'A'
  GROUP BY psm.store_id, psm.date_time, psm.adj_no, psm.username, description, ps.type, sibd.batch_no,sid.issue_units,
  		ps.medicine_id, sibd.mrp, issue_base_unit,package_uom,sic.item_code,sid.item_barcode_id,hcm.center_name,s.dept_name,
  		sibd.exp_dt,adj_detail_no
;

DROP VIEW IF EXISTS scm_purchase_request_view CASCADE;
CREATE VIEW scm_purchase_request_view AS
SELECT center_name as HOSPITAL_NAME,sim.dept_from as STORE_ID,s.dept_name as store_name,
        CASE WHEN is_super_store = 'Y' THEN dept_name ELSE null END as ORGANIZATION_CODE,
        CASE WHEN is_super_store = 'N' THEN dept_name ELSE null END as SUBINV_CODE,
	sim.indent_no as indent_number,date_time as indent_date,requester_name as REQUESTED_BY,
	approved_by as APPROVED_BY,sic.item_code,sidm.medicine_id as item_id,package_uom as TRANSACTION_UOM,trunc(qty/issue_base_unit,4) as quantity,
	expected_date as NEED_BY_DATE,'N'::character varying as CONSIGNMENT_STOCK,null as SUPPLIER,
	('PR'||sim.indent_no||sidm.medicine_id)::character varying as transaction_id,issue_base_unit as PACKAGE_UOM_SIZE
FROM store_indent_details sid
JOIN store_indent_main sim USING(indent_no)
JOIN stores s ON(s.dept_id::character varying = sim.dept_from)
LEFT JOIN hospital_center_master hcm ON (hcm.center_id = s.center_id)
JOIN store_item_details sidm USING(medicine_id)
LEFT JOIN ha_item_code_type hict ON (sid.medicine_id = hict.medicine_id AND hict.health_authority = hcm.health_authority)
LEFT JOIN store_item_codes sic ON (hict.medicine_id = sic.medicine_id AND hict.code_type = sic.code_type)
WHERE sid.purchase_flag='Y' AND indent_type ='S';


DROP VIEW IF EXISTS scm_txn_view CASCADE;
CREATE VIEW scm_txn_view AS
SELECT center_name as HOSPITAL_NAME,store_name as STORE_NAME,store_id as STORE_ID,txn_date as   TRANSACTION_DATE,
	   medicine_id as ITEM_ID,item_unit as TRANSACTION_UOM,qty as   QUANTITY ,batch_no as   LOT_NUMBER          ,
	   exp_dt as EXPIRY_DATE,item_barcode_id as BARCODE_NUMBER, txn_type::character varying as TRANSACTION_TYPE,false as AUTO_CON_FLAG,
	   'N'::character varying as CONS_FLAG,BILL_NUMBER,txn_ref as DOC_NUMBER,txn_date as DOC_DATE,USER_NAME,
	   issue_base_unit as PACKAGE_UOM_SIZE,(txn_type_prifix||txn_detail_ref)::character varying as transaction_id,
	   department::character varying
	   FROM scm_inbound_data_template;



DROP VIEW IF EXISTS scm_transfer_view CASCADE;
CREATE VIEW scm_transfer_view AS
SELECT ('-9' || hcm.center_id::character varying)::character varying AS TO_STORE_ID, (hcm.center_name || '-IN-TRANSIT'::character varying)::character varying as TO_STORE_NAME,
	   psm.store_from::character varying AS FROM_STORE_ID, sfrom.dept_name as FROM_STORE_NAME,psm.date_time AS DOC_DATE,
	   psm.date_time AS  TRANSACTION_DATE, psm.transfer_no::character varying AS DOC_NUMBER,
	   package_uom as TRANSACTION_UOM,
 	   sibd.batch_no as LOT_NUMBER, psm.username AS user_name,ps.medicine_id  as ITEM_ID,sibd.exp_dt as EXPIRY_DATE,
 	   'Stock Transfer'::character varying AS TRANSACTION_TYPE, 0 AS bonus_qty,
 	   trunc(sum(ps.qty)/issue_base_unit,4) AS QUANTITY, trunc(sum(ps.qty)/issue_base_unit,4) AS total_qty, gd.dept_name AS details,
       issue_base_unit as PACKAGE_UOM_SIZE,package_uom,sid.item_barcode_id as BARCODE_NUMBER,item_unit,hcm.center_name as HOSPITAL_NAME,
       'N'::character varying AS CONS_FLAG,
       ('D'||transfer_detail_no)::character varying as transaction_id

   FROM store_transfer_main psm
   JOIN store_transfer_details ps USING (transfer_no)
   JOIN stores gd ON gd.dept_id = psm.store_from
   JOIN store_item_details sid USING (medicine_id)
   JOIN store_category_master scm ON(scm.category_id = sid.med_category_id)
   JOIN store_item_batch_details sibd ON(sibd.item_batch_id = ps.item_batch_id)
   JOIN stores sto ON(sto.dept_id = psm.store_to)
   JOIN stores sfrom ON(sfrom.dept_id = psm.store_from)
   JOIN hospital_center_master hcm ON (hcm.center_id = sto.center_id)
   WHERE ps.indent_no IS NOT NULL
  GROUP BY psm.store_to, sto.dept_name,psm.store_from, sfrom.dept_name,psm.date_time,sid.issue_units,package_uom,
	   psm.date_time, psm.transfer_no,ps.item_unit,sibd.batch_no, psm.username,ps.medicine_id,sibd.exp_dt,
 	   gd.dept_name,issue_base_unit,package_uom,sid.item_barcode_id,item_unit,hcm.center_name,scm.issue_type,transfer_detail_no,
 	   hcm.center_name,hcm.center_id
UNION ALL
SELECT psm.store_to::character varying AS TO_STORE_ID, sto.dept_name::character varying as To_STORE_NAME,
	   '-9' || hcm.center_id::character varying AS FROM_STORE_ID, hcm.center_name || '-IN-TRANSIT'::character varying as FROM_STORE_NAME,psm.date_time AS DOC_DATE,
	   stri.receive_datetime AS  TRANSACTION_DATE, psm.transfer_no::character varying AS DOC_NUMBER,
	   package_uom as TRANSACTION_UOM,
 	   sibd.batch_no as LOT_NUMBER, psm.username AS user_name,ps.medicine_id  as ITEM_ID,sibd.exp_dt as EXPIRY_DATE,
 	   'Stock Transfer'::character varying AS TRANSACTION_TYPE, 0 AS bonus_qty,
 	   trunc(sum(stri.qty_recd)/issue_base_unit,4) AS QUANTITY, trunc(sum(stri.qty_recd)/issue_base_unit,4) AS total_qty, sto.dept_name AS details,
       issue_base_unit as PACKAGE_UOM_SIZE,package_uom,sid.item_barcode_id as BARCODE_NUMBER,item_unit,hcm.center_name as HOSPITAL_NAME,
       'N'::character varying AS CONS_FLAG,
       ('R'||stri.transfer_detail_no)::character varying as transaction_id

   FROM store_transfer_main psm
   JOIN store_transfer_details ps USING (transfer_no)
   JOIN store_transfer_receive_indent stri ON (stri.transfer_detail_no = ps.transfer_detail_no)
   JOIN stores gd ON gd.dept_id = psm.store_from
   JOIN store_item_details sid USING (medicine_id)
   JOIN store_category_master scm ON(scm.category_id = sid.med_category_id)
   JOIN store_item_batch_details sibd ON(sibd.item_batch_id = ps.item_batch_id)
   JOIN stores sto ON(sto.dept_id = psm.store_to)
   JOIN stores sfrom ON(sfrom.dept_id = psm.store_from)
   JOIN hospital_center_master hcm ON (hcm.center_id = sto.center_id)
   WHERE stri.qty_recd > 0.00 AND ps.indent_no IS NOT NULL
  GROUP BY psm.store_to, sto.dept_name,psm.store_from, sfrom.dept_name,psm.date_time,sid.issue_units,package_uom,
	   stri.receive_datetime, psm.transfer_no,ps.item_unit,sibd.batch_no, psm.username,ps.medicine_id,sibd.exp_dt,
 	   gd.dept_name,issue_base_unit,package_uom,sid.item_barcode_id,item_unit,hcm.center_name,scm.issue_type,stri.transfer_detail_no,
 	   hcm.center_name,hcm.center_id
UNION ALL
SELECT psm.store_from::character varying AS TO_STORE_ID, sfrom.dept_name::character varying as To_STORE_NAME,
	   '-9' || hcm.center_id::character varying AS FROM_STORE_ID, hcm.center_name || '-IN-TRANSIT'::character varying as FROM_STORE_NAME,psm.date_time AS DOC_DATE,
	   stri.receive_datetime AS  TRANSACTION_DATE, psm.transfer_no::character varying AS DOC_NUMBER,
	   package_uom as TRANSACTION_UOM,
 	   sibd.batch_no as LOT_NUMBER, psm.username AS user_name,ps.medicine_id  as ITEM_ID,sibd.exp_dt as EXPIRY_DATE,
 	   'Stock Transfer'::character varying AS TRANSACTION_TYPE, 0 AS bonus_qty,
 	   trunc(sum(stri.qty_rejected)/issue_base_unit,4) AS QUANTITY, trunc(sum(stri.qty_rejected)/issue_base_unit,4) AS total_qty, sfrom.dept_name AS details,
       issue_base_unit as PACKAGE_UOM_SIZE,package_uom,sid.item_barcode_id as BARCODE_NUMBER,item_unit,hcm.center_name as HOSPITAL_NAME,
       'N'::character varying AS CONS_FLAG,
       ('J'||stri.transfer_detail_no)::character varying as transaction_id

   FROM store_transfer_main psm
   JOIN store_transfer_details ps USING (transfer_no)
   JOIN store_transfer_receive_indent stri ON (stri.transfer_detail_no = ps.transfer_detail_no)
   JOIN stores gd ON gd.dept_id = psm.store_from
   JOIN store_item_details sid USING (medicine_id)
   JOIN store_category_master scm ON(scm.category_id = sid.med_category_id)
   JOIN store_item_batch_details sibd ON(sibd.item_batch_id = ps.item_batch_id)
   JOIN stores sto ON(sto.dept_id = psm.store_to)
   JOIN stores sfrom ON(sfrom.dept_id = psm.store_from)
   JOIN hospital_center_master hcm ON (hcm.center_id = sto.center_id)
   WHERE stri.qty_rejected > 0.00 AND ps.indent_no IS NOT NULL
  GROUP BY psm.store_to, sto.dept_name,psm.store_from, sfrom.dept_name,psm.date_time,sid.issue_units,package_uom,
	   stri.receive_datetime, psm.transfer_no,ps.item_unit,sibd.batch_no, psm.username,ps.medicine_id,sibd.exp_dt,
 	   gd.dept_name,issue_base_unit,package_uom,sid.item_barcode_id,item_unit,hcm.center_name,scm.issue_type,stri.transfer_detail_no,
 	   hcm.center_name,hcm.center_id
UNION ALL
SELECT psm.store_to::character varying AS TO_STORE_ID, sto.dept_name::character varying as To_STORE_NAME,
	   psm.store_from::character varying AS FROM_STORE_ID, sfrom.dept_name as FROM_STORE_NAME,psm.date_time AS DOC_DATE,
	   psm.date_time AS  TRANSACTION_DATE, psm.transfer_no::character varying AS DOC_NUMBER,
	   package_uom as TRANSACTION_UOM,
 	   sibd.batch_no as LOT_NUMBER, psm.username AS user_name,ps.medicine_id  as ITEM_ID,sibd.exp_dt as EXPIRY_DATE,
 	   'Stock Transfer'::character varying AS TRANSACTION_TYPE, 0 AS bonus_qty,
 	   trunc(sum(ps.qty)/issue_base_unit,4) AS QUANTITY, trunc(sum(ps.qty)/issue_base_unit,4) AS total_qty, gd.dept_name AS details,
       issue_base_unit as PACKAGE_UOM_SIZE,package_uom,sid.item_barcode_id as BARCODE_NUMBER,item_unit,hcm.center_name as HOSPITAL_NAME,
       'N'::character varying AS CONS_FLAG,
       ('D'||transfer_detail_no)::character varying as transaction_id

   FROM store_transfer_main psm
   JOIN store_transfer_details ps USING (transfer_no)
   JOIN stores gd ON gd.dept_id = psm.store_from
   JOIN store_item_details sid USING (medicine_id)
   JOIN store_category_master scm ON(scm.category_id = sid.med_category_id)
   JOIN store_item_batch_details sibd ON(sibd.item_batch_id = ps.item_batch_id)
   JOIN stores sto ON(sto.dept_id = psm.store_to)
   JOIN stores sfrom ON(sfrom.dept_id = psm.store_from)
   JOIN hospital_center_master hcm ON (hcm.center_id = sto.center_id)
   WHERE ps.indent_no IS NULL
  GROUP BY psm.store_to, sto.dept_name,psm.store_from, sfrom.dept_name,psm.date_time,sid.issue_units,package_uom,
	   psm.date_time, psm.transfer_no,ps.item_unit,sibd.batch_no, psm.username,ps.medicine_id,sibd.exp_dt,
 	   gd.dept_name,issue_base_unit,package_uom,sid.item_barcode_id,item_unit,hcm.center_name,scm.issue_type,transfer_detail_no,
 	   hcm.center_name,hcm.center_id;

