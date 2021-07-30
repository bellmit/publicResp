package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StoresPatientIndentDAO extends GenericDAO {

	static Logger log = LoggerFactory.getLogger(StoresPatientIndentDAO.class);
    private static final GenericDAO storePatientIndentDetailsDAO =
        new GenericDAO("store_patient_indent_details");
    private static final GenericDAO storePatientIndentMainDAO =
        new GenericDAO("store_patient_indent_main");

	public StoresPatientIndentDAO(){
		super("store_patient_indent_main");
	}

	public String getNextIndentNo(){
		return DataBaseUtil.getStringValueFromDb("SELECT generate_id('PATIENT_INDENT_NO')");
	}

	private static String PATIENT_INDENTS_SELECT =
		" SELECT * ";

	private static String PATIENT_INDENTS_COUNT =
		" SELECT count(patient_indent_no) ";

	private static String PATIENT_INDENTS_LIST =
		"  FROM ( SELECT spim.*,pd.mr_no,pr.status as visit_status,pr.visit_type, get_patient_name(pd.salutation, pd.patient_name, pd.middle_name, pd.last_name) as patient_full_name" +
		"         ,wn.ward_name,wn.ward_no,bn.bed_name,pr.center_id, idt.dispense_type, pr.patient_discharge_status " +
		"		 	FROM store_patient_indent_main spim " +
		" 			JOIN patient_registration pr ON ( patient_id = visit_id)" +
		"           JOIN patient_details pd USING(mr_no) " +
		" 			LEFT JOIN admission adm ON (adm.patient_id = spim.visit_id)" +
		"  			LEFT JOIN bed_names bn ON (bn.bed_id = adm.bed_id) "+
		"  			LEFT JOIN ward_names wn ON (wn.ward_no = bn.ward_no) " +
		"		LEFT JOIN LATERAL ( " +
    "   SELECT patient_indent_no, salecount, issuecount," +
    "   CASE WHEN (salecount > 0 AND issuecount > 0) THEN 'Sold/Issued'" +
    "   WHEN (salecount > 0 AND issuecount <= 0) THEN 'Sold'" +
    "   WHEN (salecount <= 0 AND issuecount > 0) THEN 'Issued'" +
    "   ELSE NULL END AS dispense_type" +
    "   FROM (SELECT patient_indent_no, COUNT(sale_item_id) AS salecount," +
    "   COUNT(item_issue_no) AS issuecount FROM store_patient_indent_details" +
    "   WHERE (sale_item_id IS NOT NULL OR item_issue_no IS NOT NULL) " +
    "   AND patient_indent_no = spim.patient_indent_no " +
    "   GROUP BY patient_indent_no ) AS pin ) " +
    "   idt ON (idt.patient_indent_no = spim.patient_indent_no) "+
    "   WHERE ( patient_confidentiality_check(pd.patient_group,pd.mr_no) ) " +
    "   ) AS foo  " ;


	public static PagedList searchPatientIndents(Map filter, Map listing) throws SQLException, ParseException {

		Connection con = null;

		try{
			con = DataBaseUtil.getConnection();
			SearchQueryBuilder qb = new SearchQueryBuilder(con,
					PATIENT_INDENTS_SELECT, PATIENT_INDENTS_COUNT, PATIENT_INDENTS_LIST, listing);

			qb.addFilterFromParamMap(filter);
			qb.addSecondarySort("patient_indent_no");
			if ( RequestContext.getCenterId() != 0 )
				qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", RequestContext.getCenterId());
			qb.build();

			PagedList l = qb.getMappedPagedList();

			qb.close();
			con.close();

			return l;
		}finally{
			DataBaseUtil.closeConnections(con, null);
		}

	}

	private static final String PATIENT_INDENT_ITEM_DETAILS = "SELECT *,spd.dispense_status as item_dispense_status ,"+
	"m.manf_name as orig_manf_name, sic.item_code, sic.code_type, sibd.batch_no, sibd.exp_dt, " +
	" CASE " +
	"	WHEN spm.indent_type = 'R' THEN " +
	"       spd.qty_required-spd.qty_received " +
	"   ELSE " +
	"		COALESCE((select sum(qty) from store_stock_details where dept_id = spm.indent_store and medicine_id= spd.medicine_id ),0) " +
	"   END as qty_avbl" +
	" FROM store_patient_indent_main spm " +
	" JOIN store_patient_indent_details spd USING(patient_indent_no) " +
	" JOIN store_item_details sid USING(medicine_id) " +
	" JOIN manf_master m ON ( sid.manf_name = m.manf_code )"+
	" JOIN store_category_master on med_category_id=category_id "+
	" LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = sid.medicine_id AND hict.health_authority=?) " +
	" LEFT JOIN store_item_codes sic ON (sic.medicine_id = sid.medicine_id AND sic.code_type = hict.code_type) " +
	" LEFT JOIN store_item_batch_details sibd ON (spd.item_batch_id=sibd.item_batch_id)"+
	" WHERE patient_indent_no = ?";

	public List<BasicDynaBean> getPatientIndentDetails(String patIndentNo, String healthAuthority)
		throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(PATIENT_INDENT_ITEM_DETAILS);

			ps.setString(1, healthAuthority);
			ps.setString(2, patIndentNo);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String PATIENT_RETURN_INDENT_ITEM_DETAILS = "SELECT *, " +
			" CASE WHEN sid.cust_item_code IS NOT NULL AND  TRIM(sid.cust_item_code) != ''  THEN sid.medicine_name||' - '||sid.cust_item_code ELSE sid.medicine_name END as cust_item_code_with_name "+
			" FROM patient_return_indentable_items prii" +
			" LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = prii.medicine_id AND hict.health_authority=?)" +
			" LEFT JOIN store_item_codes sic ON (sic.medicine_id = prii.medicine_id AND sic.code_type = hict.code_type) "+
			" LEFT JOIN store_item_details sid ON (sid.medicine_id = prii.medicine_id) "+
			" WHERE visit_id = ? AND qty_avbl > 0";

	public List<BasicDynaBean> getPatientReturnIndentableItems(String visitId, String healthAuthority)
		throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;

		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(PATIENT_RETURN_INDENT_ITEM_DETAILS);
			ps.setString(1, healthAuthority);
			ps.setString(2, visitId);

			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}

	}

	public BasicDynaBean getPatientReturnIndentableStoreId(String visitId)
		throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;

		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement("SELECT store_id FROM patient_return_indentable_items WHERE visit_id = ? AND qty_avbl > 0");
			ps.setString(1, visitId);

			return DataBaseUtil.queryToDynaBean(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String INDENT_ITEM_DETAILS =
		" SELECT " +
		"	CASE " +
		"		WHEN qty_unit = 'P' " +
		"			THEN (sum(qty_required)-sum(qty_received))/issue_base_unit " +
		"		ELSE sum(qty_required)-sum(qty_received) " +
		"		END  as qty, " +
		"  sid.medicine_id,medicine_name as indent_medicine_name,indent_store" +
		"  ,qty_unit,CASE WHEN qty_unit = 'I' THEN '' ELSE st.package_uom END as uom," +
		"  issue_base_unit,st.issue_units as uom_display "+
		" FROM store_patient_indent_main sim" +
		" JOIN store_patient_indent_details sid USING(patient_indent_no) " +
		" JOIN store_item_details st USING (medicine_id) " +
		" WHERE sim.visit_id = ? and sim.status IN (?) AND indent_type = ?" +
		"  AND sim.dispense_status != 'C' AND sid.dispense_status = 'O' #" +
		" GROUP BY sid.medicine_id, medicine_name," +
		"          st.issue_units,st.package_UOM" +
		"          ,indent_store,qty_unit,uom,issue_base_unit ";

	/**
	 * Item level details of an Indent
	 * @param indentNo
	 * @return
	 * @throws SQLException
	 */
	public static List<BasicDynaBean> getIndentDetailsForProcess(String visitId,String status,String indentType)
	throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;

		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(INDENT_ITEM_DETAILS.replace("#", ""));
			ps.setString(1, visitId);
			ps.setString(2, status);
			ps.setString(3, indentType);

			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}

	}

	private static final String INDENT_ITEM_DETAILS_FOR_RETURNS =
		" SELECT CASE " +
		"		WHEN qty_unit = 'P' " +
		"			THEN (sum(qty_required)-sum(qty_received))/issue_base_unit " +
		"		ELSE sum(qty_required)-sum(qty_received) " +
		"		END  as qty, " +
		"	CASE WHEN qty_unit = 'I' THEN '' ELSE st.package_UOM END AS uom, " +
		"  sid.medicine_id,qty_unit,medicine_name as indent_medicine_name," +
		"  indent_store,issue_base_unit,issue_units "+
		" FROM store_patient_indent_main sim" +
		" JOIN store_patient_indent_details sid USING(patient_indent_no) " +
		" JOIN store_item_details st USING (medicine_id) " +
		" WHERE sim.patient_indent_no = ? and sim.status IN (?) AND indent_type = 'R'" +
		"  AND sim.dispense_status != 'C' AND sid.dispense_status = 'O' AND process_type = ? " +
		" GROUP BY sid.medicine_id, medicine_name," +
		"          qty_unit,st.issue_units,st.package_UOM" +
		"          ,indent_store,issue_base_unit ";

	/**
	 * Item level details of an Indent
	 * @param indentNo
	 * @return
	 * @throws SQLException
	 */
	public static List<BasicDynaBean> getIndentDetailsForReturnProcess(String patientIndentNo,String status,String processType)
	throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;

		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(INDENT_ITEM_DETAILS_FOR_RETURNS);
			ps.setString(1, patientIndentNo);
			ps.setString(2, status);
			ps.setString(3, processType);

			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}

	}


	public static List<BasicDynaBean> getIndentDetailsForProcessOfIndentStore(String visitId,String status,String indentType,
			int indentStore,String patientIndentNo)
	throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;

		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(INDENT_ITEM_DETAILS.replace("#", " AND indent_store = ? " + (patientIndentNo != null ? " AND patient_indent_no = ? " : "")));
			ps.setString(1, visitId);
			ps.setString(2, status);
			ps.setString(3, indentType);
			ps.setInt(4, indentStore);
			if( patientIndentNo != null ){
				ps.setString(5, patientIndentNo);
			}

			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}

	}
	public boolean updateIndentDetailsDispenseStatus(Connection con,String visitId,
			Map<String,String> indentDisStatusMap,BigDecimal quantity,String medicineId,int processId,
			String processRefColName)
	throws SQLException,IOException{

	return updateIndentDetailsDispenseStatus(con,visitId,indentDisStatusMap,quantity,medicineId,processId,processRefColName, null,null);

	}

	/**
	 * On process of an indent status of the indents changes.
	 * @param con
	 * @param indentItemIds
	 * @param dispenseStatus
	 * @param medDispOpt
	 * @param saleId
	 * @param saleItems
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public boolean updateIndentDetailsDispenseStatus(Connection con,String visitId,
			Map<String,String> indentDisStatusMap,BigDecimal quantity,String medicineId,int processId,
			String processRefColName, MedicineSalesDTO sale ,String type)
	throws SQLException,IOException{

		boolean sucess = true;
		Map keys = new HashMap<String, Object>();
		BasicDynaBean medDetail = PharmacymasterDAO.getSelectedMedicineDetails(medicineId);
		try{

			List<BasicDynaBean> indentDetails = null;
			keys = new HashMap<String, Object>();
			keys.put("medicine_id", Integer.parseInt(medicineId));
			keys.put("visit_id", visitId);
			if(sale != null){
				indentDetails = getIndentsOfItem(con,visitId,Integer.parseInt(medicineId),type,sale.getItemBatchId());
			} else {
				indentDetails = getIndentsOfItem(con,visitId,Integer.parseInt(medicineId),type,null);
			}
			
			if ( indentDetails.isEmpty() ){//multi-user/tab dispensed by other session already
				return false;
			}
			for(BasicDynaBean indentDetailBean : indentDetails){
				
				if ( indentDisStatusMap.get((String)indentDetailBean.get("patient_indent_no")) == null ){
					continue;
				}

				BasicDynaBean indentItem = storePatientIndentDetailsDAO.findByKey(con,"indent_item_id",indentDetailBean.get("indent_item_id"));

				BigDecimal indentReqQty = medDetail.get("identification").equals("S") ? BigDecimal.ONE :
					(BigDecimal)indentDetailBean.get("qty_required");
				BigDecimal indentRecQty = (BigDecimal)indentDetailBean.get("qty_received");
				BigDecimal actIndentRecQty = (BigDecimal)indentDetailBean.get("qty_required");
				//if user qty is more that req qty of indent this indent is fulfilled
				BigDecimal qtyforThisIndent =  quantity.compareTo(actIndentRecQty.subtract(indentRecQty)) > 0
													? actIndentRecQty.subtract(indentRecQty): quantity ;
				quantity = quantity.subtract(qtyforThisIndent);

				BigDecimal reqQty = indentReqQty;

				sucess &= updateIndentDetails(con,(Integer)indentDetailBean.get("indent_item_id"),
						qtyforThisIndent,indentDisStatusMap.get((String)indentDetailBean.get("patient_indent_no")),
						processId,indentItem,processRefColName,reqQty,(BigDecimal)indentDetailBean.get("qty_required"));

				if ( quantity.compareTo(BigDecimal.ZERO) == 0 )//user qty is processed come ot of this indent
					break;

			}

		}catch(Exception e){
			sucess = false;
		}

		return sucess;
	}

	public static boolean updateIndentDetails(Connection con,int indentItemId,BigDecimal recvQty,
			String dispenseStatus,int processItemId,BasicDynaBean indetItem,String processIdColName ,
			BigDecimal reqQty,BigDecimal actReqQty)
	throws SQLException,IOException{

		boolean sucess = true;

		try{

			BigDecimal recQty = ((BigDecimal)indetItem.get("qty_received")).add(recvQty.abs());
			String itemDispenseStatus = dispenseStatus.equals("all") ? "C"
						: ( dispenseStatus.equals("partiall") && recQty.compareTo(BigDecimal.ZERO) > 0
									? "C" : (actReqQty.subtract(recQty).compareTo(BigDecimal.ZERO) == 0 ? "C" : "O"));

			indetItem.set("qty_received", recQty);
			indetItem.set("dispense_status", itemDispenseStatus);
			indetItem.set(processIdColName, processItemId);

			Map keys = new HashMap();
			keys.put("indent_item_id", indetItem.get("indent_item_id"));

			sucess = storePatientIndentDetailsDAO.update(con, indetItem.getMap(), keys) > 0;

		}catch (Exception e) {
			log.error("Error while updating Indent Process No"+processItemId);
		}

		return sucess;

	}


	private String INDENTS_FOR_PROCESS =
		" SELECT * FROM store_patient_indent_main " +
		"	WHERE visit_id = ? AND status = 'F' AND dispense_status != 'C' AND indent_type = ? " +
		"   # " +
		"	ORDER BY expected_date ";

	/**
	 * All Finalized indent of the visit are eligible to process.
	 * @param visitId
	 * @return List
	 * @throws SQLException
	 */
	public List<BasicDynaBean> getIndentsForProcess(String visitId,String indentType,int indentStore,String patientIndentNo)
	throws SQLException{

		Connection con = null;
		PreparedStatement ps = null;

		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(INDENTS_FOR_PROCESS.replace("#", " AND indent_store = ?" + (patientIndentNo != null ? " AND patient_indent_no = ? " : "")));
			ps.setString(1, visitId);
			ps.setString(2, indentType);
			ps.setInt(3, indentStore);
			if ( patientIndentNo != null )
				ps.setString(4, patientIndentNo);

			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private String INDENTS_FOR_RETURNS =
		" SELECT * FROM store_patient_indent_main " +
		"	WHERE patient_indent_no = ? AND status = 'F' AND dispense_status != 'C' AND indent_type = 'R' " +
		"   # " +
		"	ORDER BY expected_date ";

	/**
	 * Returns list of Return indents of process tyep issued / sold.
	 * */
	public List<BasicDynaBean> getIndentsForReturnProcess(String patientIndentNo,int indentStore,String processType)
	throws SQLException{

		Connection con = null;
		PreparedStatement ps = null;

		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(INDENTS_FOR_RETURNS.replace("#", " AND indent_store = ? AND process_type = ?"));
			ps.setString(1, patientIndentNo);
			ps.setInt(2, indentStore);
			ps.setString(3, processType);

			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	/**
	 * All Finalized indent of the visit are eligible to process.
	 * @param visitId
	 * @return List
	 * @throws SQLException
	 */
	public List<BasicDynaBean> getIndentsForProcess(String visitId,String indentType)
	throws SQLException{

		Connection con = null;
		PreparedStatement ps = null;

		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(INDENTS_FOR_PROCESS.replace("#", ""));
			ps.setString(1, visitId);
			ps.setString(2, indentType);

			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	/**
	 * All Finalized indent of the visit are eligible to process.
	 * @param visitId
	 * @return List
	 * @throws SQLException
	 */
	public List<BasicDynaBean> getIndentsForReturnProcess(String patientIndentno,String indentType,String processType)
	throws SQLException{

		Connection con = null;
		PreparedStatement ps = null;

		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(INDENTS_FOR_RETURNS.replace("#", " AND process_type = ? "));
			ps.setString(1, patientIndentno);
			ps.setString(2, processType);

			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String UPDATE_MAIN_DISPENSE_STAUS =
		"UPDATE store_patient_indent_main sm SET dispense_status = (" +
		" CASE " +
		"	   WHEN " +
		"  		(SELECT count(*) FROM store_patient_indent_details sd WHERE  sm.patient_indent_no = sd.patient_indent_no AND " +
		"			sd.dispense_status != 'C') = 0 " +
		"	   THEN 'C' " +
		"      WHEN " +
		"  		(SELECT count(*) FROM store_patient_indent_details sd WHERE sm.patient_indent_no = sd.patient_indent_no AND" +
		"			 sd.qty_received != 0 and sd.qty_received < qty_required) > 0 " +
		"		THEN 'P' " +
		" 		WHEN " +
		"		(SELECT count(*) FROM store_patient_indent_details sd WHERE sm.patient_indent_no = sd.patient_indent_no AND" +
		"			 sd.dispense_status = 'C' ) > 0 " +
		"       THEN 'P' " +
		"   	ELSE 'O' " +
		" END) WHERE patient_indent_no = ? AND dispense_status != 'C' ";

	public static boolean updateIndentDispenseStatus(Connection con,String visitId)
	throws SQLException {

		boolean status = true;
		List<BasicDynaBean> indents  = storePatientIndentMainDAO.findAllByKey("visit_id", visitId);
		for (BasicDynaBean indentMain : indents) {
		  try (PreparedStatement ps = con.prepareStatement(UPDATE_MAIN_DISPENSE_STAUS);) {
		    ps.setString(1, (String) indentMain.get("patient_indent_no"));
		    status &= ps.executeUpdate() >= 0;
		  }
		}
		return status;
	}

	private static final String UPDATE_MAIN_PROCESS_TYPE = " UPDATE store_patient_indent_main SET process_type = ? WHERE patient_indent_no = ? ";

	public static boolean updateProcessType(Connection con,String visitId,String processType)
	throws SQLException {

		boolean status = true;
		List<BasicDynaBean> indents  = storePatientIndentMainDAO.findAllByKey("visit_id", visitId);
    for (BasicDynaBean indentMain : indents) {
      try (PreparedStatement ps = con.prepareStatement(UPDATE_MAIN_PROCESS_TYPE);) {
        ps.setString(1, processType);
        ps.setString(2, (String) indentMain.get("patient_indent_no"));

        status &= ps.executeUpdate() > 0;
      }
    }
		return status;

	}


	private static final String visit_item_indents =
		" SELECT * FROM store_patient_indent_main sm " +
		" JOIN store_patient_indent_details USING(patient_indent_no)" +
		" WHERE visit_id = ? AND medicine_id = ? AND sm.dispense_status != 'C' AND sm.status = 'F' # ORDER BY expected_date";

	/**
	 * Returns list of non-closed indent details of filters on visit_id and medicine_id
	 * @param visitId
	 * @param medicineId
	 * @return
	 * @throws SQLException
	 */
	public List<BasicDynaBean> getIndentsOfItem(Connection con,String visitId,int medicineId,String returnType,Integer itemBatchId)
	throws SQLException{
		PreparedStatement ps = null;
		try{
				if(returnType != null && returnType.equals("R")) {
					ps = con.prepareStatement(visit_item_indents.replace("#", " AND item_batch_id = ?"));
					ps.setString(1, visitId);
					ps.setInt(2, medicineId);
					ps.setInt(3, itemBatchId);
				} else {
					ps = con.prepareStatement(visit_item_indents.replace("#", ""));
					ps.setString(1, visitId);
					ps.setInt(2, medicineId);
				}

			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}


	}

	private static final String IS_MEDICINE_PART_OF_INDENT =
			" SELECT count(*) as medicine_indent_count from "
			+ " store_patient_indent_details where medicine_id = ? AND patient_indent_no in (#)";
	/**
	 * Checks if sold item is part of indent open for dispensing
	 * @param medicineId
	 * @return
	 * @throws SQLException
	 */
	public Boolean isMedicinePartOfIndent(Connection con, int medicineId, Set<String> indentIds) {
    	String[] placeHolderArr = new String[indentIds.size()];
        Arrays.fill(placeHolderArr, "?");
        String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
		try (PreparedStatement ps = con.prepareStatement(IS_MEDICINE_PART_OF_INDENT.replace("#", placeHolders))) {
			Iterator<String> it = indentIds.iterator();
			ps.setInt(1, medicineId);
			int idx = 2;
			while (it.hasNext()) {
				ps.setString(idx, it.next());
				idx++;
			}
			Integer indentCount = DataBaseUtil.getIntValueFromDb(ps);
			return indentCount > 0;
		} catch (SQLException e) {
			log.error("Exception checking whether Medicine is part of indent assuming its not", e);
		} 
		return false;
	}

	private static final String GET_MEDICINES_FOR_INDENT = 
      "SELECT medicine_id from store_patient_indent_details where patient_indent_no = ?";

  public List<String> getMedicinesForIndent(Connection con, String indentNo) throws SQLException {
    PreparedStatement ps = null;
    try {

      ps = con.prepareStatement(GET_MEDICINES_FOR_INDENT);
      ps.setString(1, indentNo);

      return DataBaseUtil.queryToStringList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

	private static final String update_all_indents =
		" UPDATE store_patient_indent_details SET dispense_status = 'C' WHERE patient_indent_no = ? ";

	public boolean closeAllIndents(Connection con,String patientIndentNo)
	throws SQLException{
		PreparedStatement ps = null;

		try{
			ps = con.prepareStatement(update_all_indents);
			ps.setString(1, patientIndentNo);

			return ps.executeUpdate() > 0;
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}
	/*Get the batch_no for patient return indent  */

	private static final String GET_BATCH_DETAILS =
		"SELECT	sid.medicine_id,sid.batch_no,sid.qty,pkg_size,pkg_cp,"+
		"item_unit,issue_pkg_size,sid.item_batch_id,issued_to,exp_dt " +
		"FROM stock_issue_details sid "+
		"JOIN stock_issue_main sim USING (user_issue_no) "+
		"LEFT JOIN store_item_batch_details sibd ON (sid.medicine_id =sibd.medicine_id AND sid.item_batch_id=sibd.item_batch_id)"+
		"LEFT JOIN store_stock_details ssd ON (sid.medicine_id =ssd.medicine_id AND sid.item_batch_id=ssd.item_batch_id)"+
		"WHERE issued_to =? AND sid.medicine_id =? group by sid.batch_no,sid.medicine_id,"+
		"sim.issued_to,sid.qty,sid.pkg_size,sid.pkg_cp,sid.item_unit, "+
		"sid.issue_pkg_size,sid.item_batch_id,sibd.exp_dt" ;
	public List<BasicDynaBean> getBatchNo(String patientId,int medicineId)
	throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_BATCH_DETAILS);

			ps.setString(1, patientId);
			ps.setInt(2, medicineId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	/*
     * This method gets open patient indents for a patient 
     */
    public List<BasicDynaBean> getOpenPatientIndents(String patientId) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DataBaseUtil.getConnection();
            String GET_OPEN_PATIENT_INDENTS = 
                    " SELECT spim.*" +
                    " FROM store_patient_indent_main spim " +
                    " JOIN patient_registration pr ON (pr.patient_id = spim.visit_id)" +
                    " JOIN patient_details pd ON (pd.mr_no = pr.mr_no)" +
                    " WHERE spim.visit_id = ? AND (spim.status = ? OR spim.dispense_status IN ('O', 'P')) " +
                    " AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )";
            StringBuilder query = new StringBuilder(GET_OPEN_PATIENT_INDENTS); 
            ps = con.prepareStatement(query.toString());
            ps.setString(1, patientId);
            ps.setString(2, "O");
            return DataBaseUtil.queryToDynaList(ps);
        } finally {
            DataBaseUtil.closeConnections(con, ps);
        }
    }

}