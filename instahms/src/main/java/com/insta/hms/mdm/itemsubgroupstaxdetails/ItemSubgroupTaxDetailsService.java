package com.insta.hms.mdm.itemsubgroupstaxdetails;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/**
 * The Class ItemSubgroupTaxDetailsService.
 *
 * @author irshadmohammed
 */
@Service
public class ItemSubgroupTaxDetailsService extends MasterService {

  /** The item subgroup tax details repository. */
  @LazyAutowired
  ItemSubgroupTaxDetailsRepository itemSubgroupTaxDetailsRepository;

  /**
   * Instantiates a new item subgroup tax details service.
   *
   * @param repo the ItemSubgroupTaxDetailsRepository
   * @param validator the ItemSubgroupTaxDetailsValidator
   */
  public ItemSubgroupTaxDetailsService(ItemSubgroupTaxDetailsRepository repo,
      ItemSubgroupTaxDetailsValidator validator) {
    super(repo, validator);
  }
}
