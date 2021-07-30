package com.insta.hms.mdm.orderkit;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class OrderkitDetailsRepository.
 *
 * @author irshadmohammed
 */
@Repository
public class OrderkitDetailsRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new orderkit details repository.
   */
  public OrderkitDetailsRepository() {
    super("order_kit_details", "order_kit_details_id");
  }
}
