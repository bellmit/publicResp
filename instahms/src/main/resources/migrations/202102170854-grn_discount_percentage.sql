-- liquibase formatted sql
-- changeset anandpracto:adding-discount-column-for-grn-table
ALTER TABLE store_grn_details ADD COLUMN discount_per numeric(10,2) default 0.0;

UPDATE store_grn_details sgd1 SET discount_per = sgd2.calculated_disc_per
FROM (SELECT round(discount*100/(cost_price*billed_qty/grn_pkg_size),2) AS calculated_disc_per,grn_no,medicine_id,batch_no
	FROM store_grn_details
	) AS sgd2
WHERE sgd1.grn_no = sgd2.grn_no AND sgd1.medicine_id = sgd2.medicine_id AND sgd1.batch_no = sgd2.batch_no AND sgd1.discount >0;
