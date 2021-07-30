package com.insta.hms.core.inventory.stocks;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class StoreTransactionLotDetailsRepository extends GenericRepository {

  StoreTransactionLotDetailsRepository() {
    super("store_transaction_lot_details");
  }
}
