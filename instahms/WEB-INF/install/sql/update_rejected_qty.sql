--Query to update indent rejected to main qty

UPDATE store_stock_details ssd set qty = qty + foo.qty_rejected,qty_in_transit = qty_in_transit - foo.qty_rejected FROM(SELECT store_stock_id,
sum(CASE WHEN sd.qty_in_transit > pst.qty_rejected THEN pst.qty_rejected WHEN sd.qty_in_transit = pst.qty_rejected THEN sd.qty_in_transit  WHEN sd.qty_in_transit < pst.qty_rejected THEN sd.qty_in_transit END ) as qty_rejected
	FROM store_indent_main pim
	join store_indent_details pi using (indent_no)
	join store_stock_transfer_view pst using (indent_no,medicine_id)
	join (
		SELECT sum(qty) as qty,sum(qty_in_transit) as qty_in_transit,item_batch_id,dept_id,store_stock_id
			FROM store_stock_details GROUP BY item_batch_id,dept_id,store_stock_id) as sd on pst.item_batch_id = sd.item_batch_id and pst.store_from = sd.dept_id
	JOIN store_item_batch_details sibd ON(sd.item_batch_id = sibd.item_batch_id)
	join store_item_details pmd on sibd.medicine_id = pmd.medicine_id
	join stores g on pim.dept_from = g.dept_id::text
where (CASE WHEN sd.qty_in_transit > pst.qty_rejected THEN pst.qty_rejected WHEN sd.qty_in_transit = pst.qty_rejected THEN sd.qty_in_transit  WHEN sd.qty_in_transit < pst.qty_rejected THEN sd.qty_in_transit END) > 0 and pi.status != 'S' GROUP BY store_stock_id order by store_stock_id) as foo WHERE ssd.store_stock_id = foo.store_stock_id ;
