package com.insta.hms.mdm.grnprinttemplates;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.springframework.stereotype.Repository;

/** The Class GrnPrintTemplatesRepository. */
@Repository
public class GrnPrintTemplatesRepository extends MasterRepository<Integer> {

  /** The Constant GRN_TEMPLATES_EXT_QUERY_TABLES. */
  private static final String GRN_TEMPLATES_EXT_QUERY_TABLES =
      " from (SELECT * , (select count(*) from grn_print_template where "
          + " template_id=suppcat.template_id) as count from grn_print_template suppcat)as foo";

  /** Instantiates a new grn print templates repository. */
  public GrnPrintTemplatesRepository() {
    super("grn_print_template", "template_id", "template_name");
  }

  /**
   * Build Search query.
   *
   * @return true, if successful
   */
  public SearchQuery getSearchQuery() {
    return new SearchQuery(GRN_TEMPLATES_EXT_QUERY_TABLES);
  }

  /**
   * Delete record.
   *
   * @param supplierCategoryId the supplier category Id
   * @return true, if successful
   */
  public boolean deleteRecord(int supplierCategoryId) {
    int noRowsModified =
        DatabaseHelper.delete(
            "delete from grn_print_template where template_id = ?",
            new Object[] {supplierCategoryId});
    if (noRowsModified > 0) {
      return true;
    }
    return false;
  }
}
