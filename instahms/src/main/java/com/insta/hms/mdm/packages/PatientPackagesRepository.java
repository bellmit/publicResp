package com.insta.hms.mdm.packages;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PatientPackagesRepository extends MasterRepository<Integer> {

  public PatientPackagesRepository() {
    super("patient_packages", "pat_package_id");
  }

  private static final String GET_MULTI_VISIT_PACKAGE_IDS =
      "SELECT ppq.pat_package_id, ppq.package_id, ppq.package_name, ppq.package_code, "
          + " ppq.package_charge, ppq.created_at, pdvq.total_deposit, pdvq.unallocated_deposit "
          + " FROM ( SELECT pp.pat_package_id, "
      + " pp.package_id, ppd.package_name, ppd.package_code, ppd.created_at, "
          + " sum(ppcc.charge) as package_charge "
          + " FROM patient_packages as pp "
          + " LEFT JOIN patient_customised_package_details ppd "
          + " ON (pp.pat_package_id = ppd.patient_package_id ) "
          + " JOIN patient_package_contents ppc ON pp.pat_package_id = ppc.patient_package_id "
          + " JOIN patient_package_content_charges ppcc "
          + " ON ppcc.patient_package_content_id = ppc.patient_package_content_id "
          + " WHERE pp.mr_no = :mrNo "
          + " AND pp.status IN('P') AND ppd.multi_visit_package = true AND pp.is_discontinued "
          + " IS NOT true AND CASE WHEN ppd.consumption_validity_value IS NOT NULL "
          + " THEN ppd.created_at >= (select current_timestamp - "
          + "   ((ppd.consumption_validity_value || CASE WHEN ppd.consumption_validity_unit = 'M' "
          + "   THEN ' months ' ELSE ' days ' END)::interval)) ELSE true END "
          + " group by ppd.created_at, "
          + " pp.pat_package_id, pp.package_id, ppd.package_name, ppd.package_code"
      + " ) ppq "
      + "INNER JOIN ("
      + " SELECT pp.pat_package_id, coalesce(sum(ppdv.amount), 0) AS total_deposit, "
          + " sum(CASE WHEN ppdv.unallocated_amount>0 THEN ppdv.unallocated_amount "
          + " ELSE 0 END) AS unallocated_deposit"
          + " FROM patient_packages AS pp "
          + " LEFT JOIN patient_customised_package_details ppd "
          + " ON (pp.pat_package_id = ppd.patient_package_id) "
          + " LEFT JOIN patient_package_deposits_view ppdv "
          + " ON ppdv.pat_package_id = pp.pat_package_id::text "
          + " WHERE pp.mr_no = :mrNo "
          + " AND pp.status IN('P')"
          + " AND ppd.multi_visit_package = true AND pp.is_discontinued IS NOT true "
          + " GROUP BY pp.pat_package_id"
      + ") pdvq ON ppq.pat_package_id = pdvq.pat_package_id "
      + " ORDER BY ppq.created_at DESC ";

  /**
   * get patient package details.
   *
   * @param mrNo the mrNo
   * @return list of Patient Package details
   */
  public List<BasicDynaBean> getMultiVisitPackageIds(String mrNo) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("mrNo", mrNo);
    return DatabaseHelper.queryToDynaList(GET_MULTI_VISIT_PACKAGE_IDS, params);
  }

  private static final String GET_MULTI_VISIT_PACKAGES =
      "SELECT pp.package_id, ppd.package_name, pm.insurance_category_id, pp.pat_package_id,"
      + " to_char(ppd.created_at,'YYYY-MM-DD HH24:MI:SS') AS presc_date FROM patient_packages "
      + " as pp JOIN packages pm ON (pp.package_id = pm.package_id ) JOIN "
      + " patient_customised_package_details ppd ON  (ppd.patient_package_id = "
      + " pp.pat_package_id)  WHERE mr_no = ? AND pp.status IN('P') AND "
      + " ppd.multi_visit_package = true "
      + " AND EXISTS( "
      + " SELECT ppc.patient_package_content_id, ppc.activity_qty as content_qty,"
      + " sum(COALESCE(ppcc.quantity,0)) as consumed_qty"
      + " FROM patient_package_contents  ppc"
      + " LEFT JOIN patient_package_content_consumed ppcc ON("
      + " ppc.patient_package_content_id = ppcc.patient_package_content_id)"
      + " WHERE ppc.patient_package_id=pp.pat_package_id and charge_head ='INVITE'"
      + " GROUP BY 1,2 having (ppc.activity_qty-sum(COALESCE(ppcc.quantity,0)))>0)";

  public List<BasicDynaBean> getMultiVisitPackages(String mrNo) {
    return DatabaseHelper.queryToDynaList(GET_MULTI_VISIT_PACKAGES, mrNo);
  }


  private static final String GET_MULTI_VISIT_PACKAGE_ID =
      "SELECT ppq.pat_package_id, ppq.package_id, ppq.package_name, ppq.package_code, "
      + " 0 AS package_charge, ppq.created_at ,pdvq.total_deposit, pdvq.unallocated_deposit FROM "
      + " ( SELECT pp.pat_package_id, "
      + " pp.package_id, pm.package_name, pm.package_code, min(pap.presc_date) AS created_at "
      + " FROM patient_packages  pp "
      + " LEFT JOIN packages pm ON (pp.package_id = pm.package_id ) "
      + " JOIN package_prescribed pap ON pap.pat_package_id = pp.pat_package_id "
      + " WHERE pp.mr_no = :mrNo AND pp.status IN('P') AND pm.multi_visit_package = true "
      + " group by pp.pat_package_id,pp.package_id, pm.package_name, pm.package_code)  ppq"
      + " INNER JOIN ("
      + " SELECT pp.pat_package_id, coalesce(sum(ppdv.amount), 0) AS total_deposit, "
      + " sum(CASE WHEN ppdv.unallocated_amount>0 THEN ppdv.unallocated_amount "
      + " ELSE 0 END) AS unallocated_deposit"
      + " FROM patient_packages AS pp "
      + " LEFT JOIN packages pm ON (pp.package_id = pm.package_id ) "
      + " LEFT JOIN patient_package_deposits_view ppdv "
      + " ON ppdv.pat_package_id = pp.pat_package_id::text "
      + " WHERE pp.mr_no = :mrNo "
      + " AND pp.status IN('P')"
      + " AND pm.multi_visit_package = true "
      + " AND pp.is_discontinued IS NOT true "
      + " GROUP BY pp.pat_package_id"
      + ") pdvq ON ppq.pat_package_id = pdvq.pat_package_id ";

  /**
   * get the multi-visit package details of patients.
   *
   * @param mrNo the mrNo
   * @return list of multi-visit package details
  */
  public List<BasicDynaBean> getMultiVisitPackageId(String mrNo) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("mrNo", mrNo);
    return DatabaseHelper.queryToDynaList(GET_MULTI_VISIT_PACKAGE_ID, params);
  }

}

  

