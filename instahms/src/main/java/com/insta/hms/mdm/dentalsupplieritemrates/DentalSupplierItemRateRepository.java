package com.insta.hms.mdm.dentalsupplieritemrates;

import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.springframework.stereotype.Repository;


/**
 * The Class is {@link DentalSupplierItemRateRepository}.
 * @author krishna
 *
 */
@Repository
public class DentalSupplierItemRateRepository extends MasterRepository<Integer> {

  public DentalSupplierItemRateRepository() {
    super("dental_supplier_item_rate_master", "item_supplier_rate_id", null, new String[] {
        "supplier_id", "item_id", "status" });
  }

  public static final String SEARCH_TABLES =
      " FROM (SELECT dsir.*, dsir.status as item_rate_status, dsm.item_name, dsma.supplier_name "
          + " FROM dental_supplier_item_rate_master dsir "
          + " JOIN dental_supplies_master dsm ON(dsm.item_id = dsir.item_id) "
          + " JOIN dental_supplier_master dsma ON(dsma.supplier_id = dsir.supplier_id)) as foo ";

  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(SEARCH_TABLES);
  }
}
