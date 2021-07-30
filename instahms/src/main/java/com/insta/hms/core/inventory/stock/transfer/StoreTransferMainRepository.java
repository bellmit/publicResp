package com.insta.hms.core.inventory.stock.transfer;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class StoreTransferMainRepository extends GenericRepository  {
  StoreTransferMainRepository() {
    super("store_transfer_main");
  }

}
