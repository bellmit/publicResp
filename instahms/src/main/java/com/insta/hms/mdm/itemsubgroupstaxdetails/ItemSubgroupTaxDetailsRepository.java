package com.insta.hms.mdm.itemsubgroupstaxdetails;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class ItemSubgroupTaxDetailsRepository.
 *
 * @author irshadmohammed
 */
@Repository
public class ItemSubgroupTaxDetailsRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new item subgroup tax details repository.
   */
  public ItemSubgroupTaxDetailsRepository() {
    super("item_sub_groups_tax_details", "item_subgroup_id");
  }
}
