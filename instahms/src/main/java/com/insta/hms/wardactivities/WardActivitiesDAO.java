package com.insta.hms.wardactivities;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The Class WardActivitiesDAO.
 *
 * @author mithun.saha
 */

public class WardActivitiesDAO {

  /** The Constant ACTIVITIES_FIELDS. */
  private static final String ACTIVITIES_FIELDS = "SELECT a.activity_id, a.activity_type,"
      + " a.activity_status, a.due_date, a.activity_num, a.order_no,"
      + " a.activity_remarks as activity_remarks, COALESCE(pmp.medicine_remarks,"
      + " pomp.medicine_remarks, ptp.test_remarks, psp.service_remarks, pcp.cons_remarks,"
      + " potp.item_remarks) as remarks, a.completed_date, a.prescription_type,"
      + " a.completed_by, a.ordered_by, a.prescription_id, "
      + " CASE WHEN activity_type = 'G' THEN gen_activity_details"
      + " WHEN presc_type = 'NonBillable' THEN item_name"
      + " WHEN presc_type = 'Medicine' THEN sid.medicine_name WHEN presc_type = 'Inv.'"
      + " THEN atp.test_name "
      + " WHEN presc_type = 'Doctor' THEN cdoc.doctor_name WHEN presc_type = 'Service'"
      + " THEN service_name END item_name, "
      + " item_form_name as med_form_name, rdm.display_name as recurrence_name,"
      + " pmp.item_strength AS med_strength, pp.freq_type, "
      + " pp.repeat_interval, pp.start_datetime, pp.end_datetime, pp.no_of_occurrences,"
      + " pp.end_on_discontinue, COALESCE(pmp.medicine_remarks, pomp.medicine_remarks,"
      + " ptp.test_remarks, psp.service_remarks, pcp.cons_remarks,"
      + " potp.item_remarks) as presc_remarks, pmp.strength AS med_dosage, sid.cons_uom_id,"
      + " cum.consumption_uom,"
      + " doc.doctor_name, med_batch, med_exp_date, pp.doctor_id, a.patient_id, sl.salutation,"
      + " pd.patient_name, pd.middle_name, pd.last_name, pr.mr_no, bn.bed_id, bn.bed_name,"
      + " tp.conducted as test_conducted, sp.conducted as service_conducted,"
      + " CASE WHEN test.sample_needed = 'n' THEN 'U' ELSE tp.sflag END AS sample_status,"
      + " pr.visit_type";
  
  /** The Constant COUNT_FIELD. */
  private static final String COUNT_FIELD = "SELECT count(*) ";
  
  /** The Constant TABLES. */
  private static final String TABLES = " FROM patient_activities a "
      + " LEFT JOIN patient_prescription pp on (pp.patient_presc_id=a.prescription_id)"
      + " LEFT JOIN patient_medicine_prescriptions pmp on (pp.presc_type='Medicine'"
      + " and pmp.op_medicine_pres_id=pp.patient_presc_id) "
      + " LEFT JOIN patient_other_medicine_prescriptions pomp"
      + " on (pp.presc_type='Medicine' and pomp.prescription_id=pp.patient_presc_id)"
      + " LEFT JOIN patient_test_prescriptions ptp on (pp.presc_type='Inv.' and"
      + " ptp.op_test_pres_id=pp.patient_presc_id) "
      + " LEFT JOIN patient_consultation_prescriptions pcp on (pp.presc_type='Doctor'"
      + " and pcp.prescription_id=pp.patient_presc_id) "
      + " LEFT JOIN patient_service_prescriptions psp on (pp.presc_type='Service' and"
      + " pp.patient_presc_id=psp.op_service_pres_id)"
      + " LEFT JOIN patient_other_prescriptions potp on ((pp.presc_type='NonBillable'"
      + " and pp.patient_presc_id=potp.prescription_id))"
      + " JOIN patient_registration pr ON (a.patient_id=pr.patient_id AND pr.discharge_flag !='D')"
      + " JOIN patient_details pd ON (pr.mr_no=pd.mr_no) AND"
      + " ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )"
      + " LEFT JOIN admission adm ON (pr.patient_id=adm.patient_id) "
      + " LEFT JOIN bed_names bn ON (bn.bed_id=adm.bed_id) "
      + " LEFT JOIN ward_names wn ON (wn.ward_no=bn.ward_no)"
      + " LEFT JOIN salutation_master sl ON (sl.salutation_id= pd.salutation)"
      + " LEFT JOIN doctors doc ON (pcp.doctor_id=doc.doctor_id) "
      + " LEFT JOIN store_item_details sid ON (pp.presc_type = 'Medicine' AND"
      + " pmp.medicine_id=sid.medicine_id) "
      + " LEFT JOIN generic_name gn ON (pp.presc_type='Medicine' AND"
      + " pmp.generic_code=gn.generic_code) "
      + " LEFT JOIN all_tests_pkgs_view atp ON (pp.presc_type = 'Inv.' AND"
      + " ptp.test_id=atp.test_id) "
      + " LEFT JOIN services s ON (pp.presc_type = 'Service' AND"
      + " psp.service_id=s.service_id) "
      + " LEFT JOIN doctors cdoc ON (pp.presc_type = 'Doctor' AND"
      + " pcp.doctor_id=cdoc.doctor_id) "
      + " LEFT JOIN recurrence_daily_master rdm"
      + " ON (pp.recurrence_daily_id=rdm.recurrence_daily_id) "
      + " LEFT JOIN item_form_master ifm ON (pmp.item_form_id=ifm.item_form_id) "
      + " LEFT JOIN diagnostics test ON (presc_type='Inv.' AND test.test_id=ptp.test_id)"
      + // this is joined to get the sample needed column of test
      " LEFT JOIN tests_prescribed tp ON (a.activity_type='P' AND a.prescription_type='I'"
      + " AND a.order_no=tp.prescribed_id) "
      + " LEFT JOIN services_prescribed sp ON (a.activity_type='P' AND"
      + " a.prescription_type='S' AND a.order_no=sp.prescription_id) "
      + " LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = sid.cons_uom_id)";
  
  /** The Constant WHERE. */
  private static final String WHERE = " WHERE a.activity_status in ('P', 'S') ";

  /**
   * Gets the pending activities.
   *
   * @param params the params
   * @param listingParams the listing params
   * @return the pending activities
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public static PagedList getPendingActivities(Map params, Map<LISTING, Object> listingParams)
      throws SQLException, ParseException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    SearchQueryBuilder qb = null;
    try {
      List<String> validColumns = Arrays.asList("bed_id", "due_date", "prescription_type");
      qb = new SearchQueryBuilder(con, ACTIVITIES_FIELDS, COUNT_FIELD, TABLES, WHERE, null,
          listingParams, validColumns);
      qb.addFilterFromParamMap(params);
      qb.build();

      return qb.getDynaPagedList();
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
