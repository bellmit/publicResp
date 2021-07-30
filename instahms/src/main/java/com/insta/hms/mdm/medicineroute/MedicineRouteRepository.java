package com.insta.hms.mdm.medicineroute;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

/**
 * The Class MedicineRouteRepository.
 *
 * @author sonam
 */
@Repository
public class MedicineRouteRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new medicine route repository.
   */
  public MedicineRouteRepository() {
    super("medicine_route", "route_id", "route_name");
  }

  private static final String ROUTE_OF_ADMIN_FOR_ITEM = 
      " SELECT textcat_commacat(route_name) as route_name, "
      + "textcat_commacat(route_id||'') as route_id "
      + "FROM medicine_route WHERE route_id::text IN "
      + "(SELECT regexp_split_to_table(route_of_admin, ',') FROM store_item_details "
      + "WHERE medicine_id=?)";

  public BasicDynaBean getRouteOfAdminForItem(Integer medicineId) {
    return DatabaseHelper.queryToDynaBean(ROUTE_OF_ADMIN_FOR_ITEM, new Object[] {medicineId});
  }
}

