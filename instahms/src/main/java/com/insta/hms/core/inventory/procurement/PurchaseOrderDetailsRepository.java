package com.insta.hms.core.inventory.procurement;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * 
 * @author irshadmohammed
 *
 */
@Repository
public class PurchaseOrderDetailsRepository extends GenericRepository {
	
	public PurchaseOrderDetailsRepository() {
		super("store_po");
	}
	
	public String update_po_item_status = " UPDATE store_po SET status = ? WHERE po_no = ? ";

	public boolean updatePOItemsStatus(String po_no,String status) throws SQLException{
		return DatabaseHelper.update(update_po_item_status, new Object[]{status, po_no}) > 0;
	}
	
}
