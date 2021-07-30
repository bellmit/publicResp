package com.insta.hms.core.inventory.patientindent;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class PatientIndentRepository extends GenericRepository {
	
	public PatientIndentRepository() {
		super("store_patient_indent_main");
	}

  static Logger logger = LoggerFactory.getLogger(PatientIndentRepository.class);
	
	private static final String GET_EQUI_MEDICINES_FROM_STOCK =
		    " SELECT DISTINCT m.medicine_name,m.cust_item_code, " +
	   		" m.medicine_name || ' ('||SUM(qty)||')' AS display_name, SUM(qty) AS qty, SUM(qty)||'@'||m.medicine_name AS name_qty " +
	   		"	FROM store_stock_details msd " +
	   		"   JOIN store_item_batch_details sibd USING(item_batch_id) " +
	   		"	JOIN store_item_details m ON(m.medicine_id=msd.medicine_id) " +
	   		"	JOIN generic_name g ON g.generic_code = m.generic_name " +
	   		" 	JOIN store_category_master scm ON (scm.category_id = m.med_category_id) ";

		public static List<BasicDynaBean> getEquivalentMedicinesList(String medicineName, String genericName,
				String storeId, Boolean allStores, String saleType) throws SQLException {
			List<BasicDynaBean> l = null;
			String saleExpiredMedicine = GenericPreferencesDAO.getGenericPreferences().getSaleOfExpiredItems();
			//String saleExpiredMedicine = (String)GenericPreferencesDAO.getAllPrefs().get("sale_expiry");
			StringBuilder buildQuery = new StringBuilder(GET_EQUI_MEDICINES_FROM_STOCK);
			buildQuery.append(" WHERE ");
			buildQuery.append(" g.status = 'A' AND ");
			if (!allStores) {
				buildQuery.append(" msd.dept_id=?  AND ASSET_APPROVED = 'Y' ");
				buildQuery.append(" AND ");
			}
			buildQuery.append(" ( medicine_name = ? OR g.generic_name = ? )");
			//if ("sale".equals(saleType) || "return".equals(saleType))
				buildQuery.append(" AND scm.issue_type IN ('C','R') AND billable=true AND retailable=true ");
			if (!"Y".equalsIgnoreCase(saleExpiredMedicine)) buildQuery.append("AND (sibd.exp_dt is null or sibd.exp_dt >= current_date) ");
			buildQuery.append("GROUP BY medicine_name,cust_item_code ");
			l = DatabaseHelper.queryToDynaList(buildQuery.toString(), new Object[] {Integer.parseInt(storeId), medicineName, genericName });
			return l;
		}

         /**
	   * Gets the visit id.
	   *
	   * @param indentNo String
	   * @return the visit id
	   */
	  public String getVisitId(String indentNo) {
	    return DatabaseHelper.getString("SELECT visit_id FROM store_patient_indent_main WHERE "
	        + " patient_indent_no = ? ", indentNo);
	  }

	  private static final String GET_PATIENT_MEDICINE_BATCH_DETAILS = ""
	      + "Select distinct sid.medicine_id, sibd.batch_no, sibd.exp_dt, 'issue' as type "
	      + "from stock_issue_main sim "
	      + "join stock_issue_details sid ON (sim.user_issue_no=sid.user_issue_no AND qty - return_qty > 0) "
	      + "join store_item_batch_details sibd using (item_batch_id) "
	      + "where issued_to = :visit_id AND sid.medicine_id in (:medicine_ids) "
	      + "UNION ALL "
	      + "Select distinct ssd.medicine_id, sibd.batch_no, sibd.exp_dt, 'sales' as type "
	      + "from bill b "
	      + "join store_sales_main ssm using (bill_no) "
	      + "join store_sales_details ssd ON (ssd.sale_id=ssm.sale_id AND quantity - return_qty > 0) "
	      + "join store_item_batch_details sibd using (item_batch_id) "
	      + "where b.visit_id = :visit_id AND ssd.medicine_id in (:medicine_ids)";

	  public List<BasicDynaBean> getMedicineBatchDetailsForPatient(String patientId, List<Integer> medicineIds) {
	    MapSqlParameterSource parameters = new MapSqlParameterSource();
	    parameters.addValue("visit_id", patientId);
	    parameters.addValue("medicine_ids", medicineIds);
        return DatabaseHelper.queryToDynaList(GET_PATIENT_MEDICINE_BATCH_DETAILS, parameters);
      }

}
