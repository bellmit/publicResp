package com.insta.hms.common;

import com.insta.hms.common.StdReportDesc.Field;
import com.insta.hms.common.StdReportDesc.JoinTable;
import com.insta.hms.common.StdReportDesc.QueryUnit;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class InsClaimsReportDescProvider.
 */
public class InsClaimsReportDescProvider extends StdReportDescJsonProvider {

  /**
   * Adds the tax custom fields.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  public Map<String, Field> addTaxCustomFields() throws SQLException {
    Map<String, Field> fields = new HashMap<String, Field>();
    fields.putAll(ReportCustomFieldsHelper.addBillTaxAmtCustomFieldsByBill());
    fields.putAll(ReportCustomFieldsHelper.addPriSponsorTaxAmtCustomFieldsByBill());
    fields.putAll(ReportCustomFieldsHelper.addSecSponsorTaxAmtCustomFieldsByBill());
    fields.putAll(ReportCustomFieldsHelper.addPatientTaxAmtCustomFieldsByBill());
    return fields;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.StdReportDescJsonProvider#getReportDesc(java.lang.String)
   */
  @Override
  public StdReportDesc getReportDesc(String descName) throws Exception {
    StdReportDesc reportDesc = super.getReportDesc(descName);
    QueryUnit qu = reportDesc.getQueryUnits().get(0);
    List<JoinTable> jt = qu.getJoinTables();
    jt.addAll(ReportCustomFieldsHelper.addTaxCustomJoinTablesForBill());
    qu.setDynamicFieldsPartitionedOn("b.bill_no");
    qu.setJoinTables(jt);
    reportDesc.getFields().putAll(addTaxCustomFields());
    reportDesc.validate();
    return reportDesc;
  }

}
