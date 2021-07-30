package com.insta.hms.core.inventory.stock.transfer;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class StoreTransferDetailsRepository extends GenericRepository  {
  StoreTransferDetailsRepository() {
    super("store_transfer_details");
  }

}
