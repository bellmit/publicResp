package com.insta.hms.core.inventory.stocks;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class StoreInvoiceRepository extends GenericRepository {

	public StoreInvoiceRepository() {
		super("store_invoice");
	}
	
	@Override
	public Integer getNextSequence() {
	  return DatabaseHelper.getNextSequence("invoice");
	}
}