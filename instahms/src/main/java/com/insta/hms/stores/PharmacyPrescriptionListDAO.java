package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

/**
 * The Class PharmacyPrescriptionListDAO.
 */
public class PharmacyPrescriptionListDAO {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(PharmacyPrescriptionListDAO.class);

  /** The Constant LIST_EXT_QUERY_FIELDS. */
  private static final String LIST_EXT_QUERY_FIELDS = "SELECT * ";

  /** The Constant LIST_EXT_QUERY_COUNT. */
  private static final String LIST_EXT_QUERY_COUNT = " SELECT count(consultation_id) ";

  /** The Constant LIST_EXT_QUERY_TABLES_first. */
  private static final String LIST_EXT_QUERY_TABLES_first = " FROM (SELECT pr.mod_time,"
      + "pr.center_id, grp.patient_id, consultation_id, "
      + " CASE WHEN sts = 1 THEN 'P' WHEN sts = '3' THEN 'O' ELSE 'PA' END as status, d.doctor_name"
      + " as doctor,d.doctor_id, pd.mr_no, grp.type_of_prescription , "
      + " get_patient_name(pd.salutation, pd.patient_name, pd.middle_name, pd.last_name)"
      + " as patname, bed_name, w.ward_name, w.ward_no, pr.visit_type, pr.status as patstatus,"
      + " grp.visited_date, pr.patient_discharge_status,grp.pbm_presc_id "
      + " FROM ("
      + " SELECT (CASE WHEN pmp.is_discharge_medication = false THEN  'P' ELSE 'DM' END) as type_of_prescription,"
      + " dc.patient_id, dc.consultation_id, dc.visited_date,"
      + " pmp.pbm_presc_id,  avg(CASE WHEN pp.status IN ('O') THEN 3 WHEN pp.status = 'PA' THEN"
      + " 2 ELSE 1 END) as sts, dc.doctor_name as doctor_id "
      + " FROM patient_prescription pp "
      + " JOIN patient_medicine_prescriptions pmp ON (pp.patient_presc_id=pmp.op_medicine_pres_id) "
      + " JOIN doctor_consultation dc ON (pp.consultation_id=dc.consultation_id) "
      + " JOIN patient_registration pr ON pr.patient_id = dc.patient_id ";

  /** The Constant LIST_EXT_QUERY_TABLES_second. */
  private static final String LIST_EXT_QUERY_TABLES_second = " GROUP by dc.patient_id,"
      + " dc.consultation_id, dc.visited_date, dc.doctor_name, pmp.pbm_presc_id, is_discharge_medication"
      + " UNION ALL "
      + " SELECT (CASE WHEN pmp.is_discharge_medication = false THEN  'P' ELSE 'DM' END) as type_of_prescription,"
      + " pr.patient_id, null as consultation_id, pr.reg_date as visited_date,"
      + " pmp.pbm_presc_id,  avg(CASE WHEN pp.status IN ('O') THEN 3 WHEN pp.status = 'PA' THEN"
      + " 2 ELSE 1 END) as sts, pr.doctor as doctor_id "
      + " FROM patient_prescription pp "
      + " JOIN patient_medicine_prescriptions pmp ON (pp.patient_presc_id=pmp.op_medicine_pres_id and pmp.is_discharge_medication = true) "
      + " JOIN patient_registration pr ON (pr.patient_id = pp.visit_id) ";
  
  /** The Constant LIST_EXT_QUERY_TABLES_third. */
  private static final String LIST_EXT_QUERY_TABLES_third = " GROUP by pr.patient_id,"
      + " pr.reg_date, pr.doctor, pmp.pbm_presc_id, is_discharge_medication "
      + " ) as grp" + " JOIN patient_registration pr ON (pr.patient_id = grp.patient_id)"
      + " JOIN doctors d ON (grp.doctor_id=d.doctor_id)  "
      + " JOIN patient_details pd ON (pd.mr_no=pr.mr_no) "
      + " LEFT JOIN admission ad on ad.patient_id=grp.patient_id"
      + " LEFT JOIN bed_names using(bed_id)" + " LEFT JOIN ward_names w using(ward_no) "
      + " WHERE ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )) as list ";

  /**
   * Search prescription list.
   *
   * @param filter the filter
   * @param listing the listing
   * @return the paged list
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public static PagedList searchPrescriptionList(Map filter, Map listing)
      throws SQLException, ParseException {

    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      StringBuilder query = new StringBuilder(LIST_EXT_QUERY_TABLES_first);
      String[] visitFilter = (String[]) filter.get("visited_date");
      String[] mrNoFilter =  (String[]) filter.get("mr_no");
      int centerId = RequestContext.getCenterId();
      if ((null != visitFilter && ((visitFilter[0] != null && !visitFilter[0].isEmpty())
          || (visitFilter[1] != null && !visitFilter[1].isEmpty())))
          || (mrNoFilter != null && !mrNoFilter[0].isEmpty())  || centerId != 0) {
        query.append(" WHERE ");
      }
      if (mrNoFilter != null && !mrNoFilter[0].isEmpty()) {
          query.append(" pr.mr_no = '");
          query.append(mrNoFilter[0]);
          query.append("'");
      }
      if (centerId != 0) {
           if (mrNoFilter != null && !mrNoFilter[0].isEmpty()) {
             query.append(" AND ");
           }
           query.append(" pr.center_id = ");
           query.append(centerId);
      }
      if (visitFilter != null && (visitFilter[0] != null && !visitFilter[0].isEmpty())) {
        if ((mrNoFilter != null && !mrNoFilter[0].isEmpty())  || centerId != 0) {
          query.append(" AND ");
        }
        query.append("dc.visited_date >= '");
        query.append(DateUtil.parseDate(visitFilter[0]).toString());
        query.append("'");
      }

      if (visitFilter != null && (visitFilter[1] != null && !visitFilter[1].isEmpty())) {
        if ((mrNoFilter != null && !mrNoFilter[0].isEmpty()) || centerId != 0
           || (visitFilter != null && (visitFilter[0] != null && !visitFilter[0].isEmpty()))) {
          query.append(" AND ");
        }
        query.append("dc.visited_date <= '");
        query.append(DateUtil.parseDate(visitFilter[1]).toString());
        query.append("' ::date + 1");
      }

      query.append(LIST_EXT_QUERY_TABLES_second);
      if ((null != visitFilter && ((visitFilter[0] != null && !visitFilter[0].isEmpty())
              || (visitFilter[1] != null && !visitFilter[1].isEmpty())))
              || (mrNoFilter != null && !mrNoFilter[0].isEmpty())  || centerId != 0) {
            query.append(" WHERE ");
      }
      if (mrNoFilter != null && !mrNoFilter[0].isEmpty()) {
          query.append(" pr.mr_no = '");
          query.append(mrNoFilter[0]);
          query.append("'");
      }
      if (centerId != 0) {
           if (mrNoFilter != null && !mrNoFilter[0].isEmpty()) {
             query.append(" AND ");
           }
           query.append(" pr.center_id = ");
           query.append(centerId);
      }
      if (visitFilter != null && (visitFilter[0] != null && !visitFilter[0].isEmpty())) {
        if ((mrNoFilter != null && !mrNoFilter[0].isEmpty())  || centerId != 0) {
          query.append(" AND ");
        }
        query.append("pr.reg_date >= '");
        query.append(DateUtil.parseDate(visitFilter[0]).toString());
        query.append("'");
      }

      if (visitFilter != null && (visitFilter[1] != null && !visitFilter[1].isEmpty())) {
        if ((mrNoFilter != null && !mrNoFilter[0].isEmpty()) || centerId != 0
           || (visitFilter != null && (visitFilter[0] != null && !visitFilter[0].isEmpty()))) {
           query.append(" AND ");
        }
        query.append("pr.reg_date <= '");
        query.append(DateUtil.parseDate(visitFilter[1]).toString());
        query.append("' ::date + 1");
      }

      query.append(LIST_EXT_QUERY_TABLES_third);

      SearchQueryBuilder qb = new SearchQueryBuilder(con, LIST_EXT_QUERY_FIELDS,
          LIST_EXT_QUERY_COUNT, query.toString(), listing);

      qb.addFilterFromParamMap(filter);
      if (centerId != 0) {
        qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", centerId);
      }
      qb.addSecondarySort("consultation_id");
      qb.build();

      PagedList list = qb.getMappedPagedList();
      qb.close();

      return list;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }
}