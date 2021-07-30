package com.insta.hms.core.inventory.stocks;

import com.insta.hms.mdm.bulk.BulkDataMasterEntity;
import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StockUploadCSVBulkDataEntity extends CsVBulkDataEntity {
	private static final String[] KEYS = { "store_stock_id" };

	private static final String[] FIELDS = { "batch_no", "qty",
			"package_sp", "package_cp","medicine_id", "consignment_stock", "dept_id"};

	private static final BulkDataMasterEntity[] MASTERS = {
			new BulkDataMasterEntity("medicine_id", "store_item_details",
					"medicine_id", "medicine_name"),
			new BulkDataMasterEntity("dept_id", "stores", "dept_id",
					"dept_name") };

	public StockUploadCSVBulkDataEntity() {
		super(KEYS, FIELDS, null, MASTERS);
		enforceType("qty", BigDecimal.class);
		enforceType("package_sp", BigDecimal.class);
		enforceType("package_cp", BigDecimal.class);
	}
}