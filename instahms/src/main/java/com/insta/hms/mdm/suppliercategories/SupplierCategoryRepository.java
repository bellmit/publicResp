package com.insta.hms.mdm.suppliercategories;

import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.springframework.stereotype.Repository;

/**
 * The Class SupplierCategoryRepository.
 */
@Repository
public class SupplierCategoryRepository extends MasterRepository<Integer> {

  // RC Anupama : This query is being used to get the count of stores
  // for enabling / disabling the delete option. If we rework the
  // delete option in the master repository itself, then this may not
  // be necessary. Revisit this when we do delete.

  /** The Constant SUPP_CAT_EXT_QUERY_TABLES. */
  private static final String SUPP_CAT_EXT_QUERY_TABLES = " from (SELECT * , (select count(*) from "
      + " supplier_master where supp_category_id=suppcat.supp_category_id) as count"
      + " from supplier_category_master suppcat)as foo";

  /**
   * Instantiates a new supplier category repository.
   */
  public SupplierCategoryRepository() {
    super("supplier_category_master", "supp_category_id", "supp_category_name");
  }

  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(SUPP_CAT_EXT_QUERY_TABLES);
  }

}
