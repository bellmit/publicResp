package com.insta.hms.mdm.ordersets;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class PackageContentsRepository extends MasterRepository<Integer> {

  public PackageContentsRepository() {
    super("package_contents", "package_content_id");
  }

  @Override
  public String getSortColumn() {
    return "display_order";
  }

  private static final String LIST_ALL_QUERY = " SELECT  pc.*, "
      + " coalesce(test.test_name, s.service_name, doc.doctor_name, "
      + "   d.dept_name, em.equipment_name, 'Doctor') as name" + " FROM package_contents pc "
      + " LEFT JOIN doctors doc ON (pc.doctor_id = doc.doctor_id AND pc.activity_type = 'Doctor')"
      + " LEFT JOIN services s ON (pc.activity_id=s.service_id AND pc.activity_type = 'Service') "
      + " LEFT JOIN diagnostics test ON (test.test_id=pc.activity_id AND "
      + "     (pc.activity_type = 'Laboratory' OR pc.activity_type = 'Radiology')) "
      + " LEFT JOIN equipment_master em ON (em.eq_id = pc.activity_id AND "
      + "     pc.activity_type = 'Equipment')"
      + " LEFT JOIN department d ON (pc.dept_id = d.dept_id AND pc.activity_type = 'Department')"
      + " LEFT JOIN chargehead_constants cc ON cc.chargehead_id = charge_head ";

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
