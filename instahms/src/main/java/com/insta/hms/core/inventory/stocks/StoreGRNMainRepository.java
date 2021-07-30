package com.insta.hms.core.inventory.stocks;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class StoreGRNMainRepository extends GenericRepository {

	public StoreGRNMainRepository() {
		super("store_grn_main");
	}
}
