package com.insta.hms.mdm.ordersets;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class CenterPackageApplicabilityRepository extends MasterRepository<Integer> {

  public CenterPackageApplicabilityRepository() {
    super("center_package_applicability", "center_package_id");
  }

  private static final String LIST_ALL_QUERY = " SELECT  cpa.*, hcm.center_name, hcm.city_id, "
      + " hcm.state_id, ct.city_name, sm.state_name FROM center_package_applicability cpa "
      + " LEFT JOIN hospital_center_master hcm on (hcm.center_id = cpa.center_id)"
      + " LEFT JOIN city ct ON (ct.city_id = hcm.city_id) "
      + " LEFT JOIN state_master sm ON (sm.state_id = hcm.state_id) ";

  @Override
  public List<BasicDynaBean> listAll(List<String> columns, Map<String, Object> filterMap,
      String sortColumn) {
    boolean shouldFilter = filterMap != null && !filterMap.isEmpty();
    StringBuilder query = new StringBuilder("SELECT ");
    List<Object> filterValues = new ArrayList<>();
    if (columns == null || columns.isEmpty()) {
      query.append("*");
    } else {
      boolean first = true;
      for (String column : columns) {
        column = DatabaseHelper.quoteIdent(column);
        if (!first) {
          query.append(", ");
        }
        first = false;
        query.append(column);
      }
    }
    query.append(" FROM ( ").append(LIST_ALL_QUERY);
    if (shouldFilter) {

      query.append(" WHERE ");
      Integer filterIndex = 1;
      for (Map.Entry<String, Object> entry : filterMap.entrySet()) {
        if (filterIndex == filterMap.size()) {
          query.append(DatabaseHelper.quoteIdent((String) entry.getKey())).append("=?");
        } else {
          query.append(DatabaseHelper.quoteIdent((String) entry.getKey())).append("=?")
              .append(" AND ");
        }
        filterIndex++;
        filterValues.add(entry.getValue());
      }
    }

    if ((sortColumn != null) && !sortColumn.equals("")) {
      query.append(" ORDER BY " + DatabaseHelper.quoteIdent(sortColumn));
    }

    query.append(" ) as foo ");

    if (shouldFilter) {
      return DatabaseHelper.queryToDynaList(query.toString(), filterValues.toArray());
    } else {
      return DatabaseHelper.queryToDynaList(query.toString());
    }
  }

}
