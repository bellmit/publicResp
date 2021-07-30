package com.insta.hms.master.DietaryMaster;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.beanutils.BasicDynaBean;
import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.diagnosticsmasters.addtest.TestCharge;
import com.insta.hms.emr.EMRDoc;
import com.insta.hms.emr.EMRInterface;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import flexjson.JSONSerializer;


public class DietaryMasterDAO extends GenericDAO{


	public DietaryMasterDAO(){
		super("diet_master");
	}

	private String DIET_MASTER_FIELDS = " SELECT *   ";
	private String COUNT = " SELECT count(*) ";
	private String FROM_TABLE = " FROM diet_master dm " ;

	private static final String GET_CHARGE = "SELECT charge FROM  diet_charges WHERE "
		+ "diet_id=? AND bed_type=? AND org_id= ? ";

	private static final String GET_CHARGES_FOR_DIET = "SELECT meal_name,bed_type , charge FROM  diet_charges  " +
			" JOIN diet_master dm USING (diet_id) "  +
			" WHERE diet_id=?  AND org_id= ? order by bed_type ";


	public PagedList getAllDietary(Map<LISTING, Object> paginingParams,String searchName,
			String dietCat,List statusList,String orgID,HttpServletRequest request,String serviceSubGroupId) throws Exception{


		ArrayList allDietList = new ArrayList();


		LinkedHashMap<String, ArrayList> map = new LinkedHashMap<String, ArrayList>();

		int pageNum = (Integer) paginingParams.get(LISTING.PAGENUM);
		Connection con = DataBaseUtil.getReadOnlyConnection();
		SearchQueryBuilder qb =
				new	SearchQueryBuilder(con,DIET_MASTER_FIELDS,COUNT,FROM_TABLE,null,null,"diet_id",false,25,pageNum);
		qb.addFilter(SearchQueryBuilder.STRING, "meal_name", "ILIKE", searchName);
		qb.addFilter(SearchQueryBuilder.STRING, "diet_category", "ILIKE", dietCat);
		qb.addFilter(SearchQueryBuilder.STRING, "status", "IN", statusList);
		if (serviceSubGroupId!=null && !serviceSubGroupId.equals(""))
			qb.addFilter(SearchQueryBuilder.INTEGER, "service_sub_group_id", "=", Integer.parseInt(serviceSubGroupId));
		qb.build();
		PreparedStatement psData = qb.getDataStatement();
		PreparedStatement psCount = qb.getCountStatement();

		int count = Integer.parseInt(DataBaseUtil.getStringValueFromDb(psCount));
		List dietlist = DataBaseUtil.queryToDynaList(psData);


		DataBaseUtil.closeConnections(null, psCount);
		DataBaseUtil.closeConnections(con, psData);

		return new PagedList(dietlist,count,25,pageNum);

	}


	public List  getChargeForMeal(int dietId,String orgId) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		List mealChargeList = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_CHARGES_FOR_DIET);
			ps.setInt(1, dietId);
			ps.setString(2, orgId);

			mealChargeList = DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return mealChargeList;
	}

	private String GET_ALL_DIET=" SELECT diet_id,meal_name FROM diet_master ";

	public ArrayList getAllDiet(){

		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		ArrayList dietList = null;
		try {
			  ps = con.prepareStatement(GET_ALL_DIET);
			  dietList = DataBaseUtil.queryToArrayList(ps);
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DataBaseUtil.closeConnections(con, ps, null);
		}
		return dietList;
	}

	private static final String GET_ROUTINE_CHARGE = "SELECT charge FROM  diet_charges WHERE "
		+ "diet_id=? AND bed_type=? AND org_id=?  ";

	private static final String GET_DISCOUNT = "SELECT discount FROM  diet_charges WHERE "
		+ "diet_id=? AND bed_type=? AND org_id=?  ";


	public Map editDietCharges(String orgId, int dietId) throws SQLException {

		Connection con = DataBaseUtil.getConnection();
		LinkedHashMap<String, ArrayList<String>> map = new LinkedHashMap<String, ArrayList<String>>();
		ArrayList<String> beds = new ArrayList<String>();
		ArrayList<String> regularCharge = new ArrayList<String>();
		ArrayList<String> discount = new ArrayList<String>();

		BedMasterDAO bddao = new BedMasterDAO();
		ArrayList<Hashtable<String, String>> bedTypes = bddao
				.getUnionOfAllBedTypes();
		Iterator<Hashtable<String, String>> it = bedTypes.iterator();

		PreparedStatement rps = con.prepareStatement(GET_ROUTINE_CHARGE);
		PreparedStatement dps = con.prepareStatement(GET_DISCOUNT);

		while (it.hasNext()) {
			Hashtable<String, String> ht = it.next();
			String bedType = ht.get("BED_TYPE");
			beds.add(bedType);

			rps.setInt(1, dietId);
			rps.setString(2, bedType);
			rps.setString(3, orgId);
			regularCharge.add(DataBaseUtil.getStringValueFromDb(rps));

			dps.setInt(1, dietId);
			dps.setString(2, bedType);
			dps.setString(3, orgId);
			discount.add(DataBaseUtil.getStringValueFromDb(dps));
		}

		map.put("CHARGES", beds);
		map.put("REGULARCHARGE", regularCharge);
		map.put("DISCOUNT", discount);
		rps.close();con.close();
		return map;
	}


	private String UPDATE_DIET_CHARGES = " UPDATE diet_charges SET charge = ?,discount=? WHERE diet_id = ? AND org_id = ? AND bed_type = ? ";

	private String CHECK_DIET_CHARGE = " SELECT count(*) FROM diet_charges WHERE diet_id = ? AND org_id = ?  AND  bed_type = ? ";

	private String INSERT_NEW_CHARGE = " INSERT INTO diet_charges (diet_id,org_id,bed_type,charge,discount) VALUES (?,?,?,?,?) ";

	public  boolean updateDietCharges(int dietID,String orgID,ArrayList bedlist,ArrayList chargelist,ArrayList discountList){

		Connection con = null;
		PreparedStatement ps = null;
		PreparedStatement psCheckDietCharge = null;
		PreparedStatement psNewDietCharge = null;
		boolean status = false;
		BedMasterDAO bedMasterDAO = new BedMasterDAO();
		try {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				ps = con.prepareStatement(UPDATE_DIET_CHARGES);
				psNewDietCharge = con.prepareStatement(INSERT_NEW_CHARGE);
				psCheckDietCharge = con.prepareStatement(CHECK_DIET_CHARGE);
				//ArrayList bedList = bedMasterDAO.getUnionOfAllBedTypes();
				Iterator it = bedlist.iterator();
				//Iterator chargeit = chargeList.get("charges").iterator();
				for (int i=0;i<bedlist.size();i++){
					String bedType = bedlist.get(i).toString();

					psCheckDietCharge.setInt(1, dietID);
					psCheckDietCharge.setString(2, orgID);
					psCheckDietCharge.setString(3, bedType);

					String count = DataBaseUtil.getStringValueFromDb(psCheckDietCharge);
					 if (count.equals("0")){

						 psNewDietCharge.setInt(1, dietID);
						 psNewDietCharge.setString(2, orgID);
						 psNewDietCharge.setString(3, bedType);
						 psNewDietCharge.setBigDecimal(4,  new  BigDecimal(chargelist.get(i).toString()));
						 psNewDietCharge.setBigDecimal(5,  new  BigDecimal(discountList.get(i).toString()));
						 psNewDietCharge.addBatch();

					 }else {
							ps.setBigDecimal(1,  new  BigDecimal(chargelist.get(i).toString()));
							ps.setBigDecimal(2,  new  BigDecimal(discountList.get(i).toString()));
							ps.setInt(3, dietID);
							ps.setString(4, orgID);
							ps.setString(5, bedType);

							ps.addBatch();
					 }
				}

				do {
					int a[] = ps.executeBatch();
					status = DataBaseUtil.checkBatchUpdates(a);
					if (!status) break;

					int b[] = psNewDietCharge.executeBatch();
					status = DataBaseUtil.checkBatchUpdates(b);


				}while (false);
				if (status){
					con.commit();
				}else{
					con.rollback();
				}

		} catch (SQLException e) {

		}finally{
			DataBaseUtil.closeConnections(null, psNewDietCharge);
			DataBaseUtil.closeConnections(null, psCheckDietCharge);
			DataBaseUtil.closeConnections(con, ps);
		}
		return status;
	}



	private String INSERT_INTO_DIET_CHARGES = " INSERT INTO diet_charges(diet_id,org_id,bed_type,charge) VALUES (?,?,?,?) ";

	public boolean insertIntoDietCharges(Connection con,int dietId,String orgID) throws SQLException{
		boolean status = false;
		PreparedStatement ps = con.prepareStatement(INSERT_INTO_DIET_CHARGES);
			OrgMasterDao orgDao = new OrgMasterDao();
			ArrayList<Hashtable<String,String>> al = orgDao.getAllOrgs();
			Iterator<Hashtable<String,String>> orgIt = al.iterator();
			TestCharge  tc = null;
			while(orgIt.hasNext()){
				Hashtable<String,String> ht = orgIt.next();
				String orgId = ht.get("ORG_ID");
				BedMasterDAO bddao= new BedMasterDAO();
				ArrayList<Hashtable<String,String>> bedTypes = bddao.getUnionOfAllBedTypes();
				Iterator<Hashtable<String,String>> bedIt = bedTypes.iterator();
				while(bedIt.hasNext()){
					String bedType = bedIt.next().get("BED_TYPE");

					ps.setInt(1, dietId);
					ps.setString(2, orgId);
					ps.setString(3, bedType);
					ps.setBigDecimal(4, BigDecimal.ZERO);

					ps.addBatch();
				}
			}
					int a[] = ps.executeBatch();
					status = DataBaseUtil.checkBatchUpdates(a);
					ps.close();
					return status;
	}


	private String GET_CHARGE_FOR_MEAL = " SELECT dm.diet_id,dm.meal_name,dc.org_id,dc.bed_type,dc.charge,dc.discount " +
										 " FROM diet_master dm " +
										 " JOIN diet_charges dc USING (diet_id)" +
										 " WHERE  dc.org_id = ? AND dc.bed_type = ? AND dm.status = 'A' ";

	public List getMealCharge (String bedType,String orgID) throws SQLException{

		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;

		ps = con.prepareStatement(GET_CHARGE_FOR_MEAL);


		ps.setString(1, orgID);
		ps.setString(2, bedType);

		List mealCahrgeList = DataBaseUtil.queryToArrayList(ps);

		DataBaseUtil.closeConnections(con, ps);
		return  mealCahrgeList;

	}

	private String GET_ALL_MEAL = " SELECT dm.diet_id,dm.meal_name,dm.status " +
			" FROM diet_master dm  " ;

	public String getAllMeal () throws SQLException{

		Connection con = DataBaseUtil.getReadOnlyConnection();
		JSONSerializer js = new JSONSerializer();
		PreparedStatement ps = null;

		ps = con.prepareStatement(GET_ALL_MEAL);

		String mealCahrgeList = js.exclude("class").serialize( DataBaseUtil.queryToArrayList(ps));

		DataBaseUtil.closeConnections(con, ps);
		return  mealCahrgeList;
	}


	private String GET_DIET_CATEGORY = " SELECT distinct  dm.diet_category "
			+ " FROM diet_master dm  ";

	public List getDietCategory() throws SQLException {

		Connection con = DataBaseUtil.getReadOnlyConnection();
		JSONSerializer js = new JSONSerializer();
		PreparedStatement ps = null;

		ps = con.prepareStatement(GET_DIET_CATEGORY);

		List categoryList = DataBaseUtil.queryToDynaList(ps);

		DataBaseUtil.closeConnections(con, ps);
		return categoryList;
	}

	private static final String GET_ROUTINE_CHARGE_DISCOUNT = "SELECT " +
		" dc.charge, dc.discount, dm.service_tax, dm.diet_id, dm.meal_name,"+
		" dm.service_sub_group_id, dm.insurance_category_id,dm.billing_group_id " +
		" FROM  diet_charges dc " +
		"  JOIN diet_master dm ON (dm.diet_id = dc.diet_id) " +
		" WHERE dc.diet_id=? AND bed_type=? AND org_id=? ";

	public BasicDynaBean getChargeForMeal(String orgId, String dietId, String bedType) throws SQLException{

		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = con.prepareStatement(GET_ROUTINE_CHARGE_DISCOUNT);
		ps.setInt(1, Integer.parseInt(dietId));
		ps.setString(2, bedType);
		ps.setString(3, orgId);
		BasicDynaBean chargebean = (BasicDynaBean)DataBaseUtil.queryToDynaList(ps).get(0);
		DataBaseUtil.closeConnections(con, ps);
		return chargebean;
	}


	private String  FIELDS = "SELECT pr.mr_no,coalesce ( bn.bed_name,'(Not allocated)') as bed_name,coalesce (wn.ward_name,'(Not allocated)') as ward_name, " +
	 						 " dm.meal_name,dp.meal_timing, get_patient_name(pd.salutation, pd.patient_name, pd.middle_name, pd.last_name) as patient_name,dp.ordered_id " ;


	private String FORM_TABLES = " FROM diet_prescribed dp " +
								 " LEFT JOIN diet_master dm ON (dm.diet_id = dp.diet_id)" +
								 " JOIN patient_registration pr ON (pr.patient_id = dp.visit_id)" +
								 " LEFT JOIN ip_bed_details ibd ON (ibd.patient_id = pr.patient_id AND ibd.status IN ('A','C'))" +
								 " LEFT JOIN bed_names bn ON (bn.bed_id = ibd.bed_id)" +
								 " LEFT JOIN ward_names wn ON (wn.ward_no = bn.ward_no)" +
								 " LEFT JOIN patient_diet_prescriptions pdp ON (dp.diet_pres_id = pdp.diet_pres_id)" +
								 " JOIN patient_details pd ON (pd.mr_no = pr.mr_no) ";



	public PagedList searchMealsToBeDelivered (Map<LISTING, Object> params,String date,String ward,String mealName,String mealTiming) throws SQLException, ParseException{
		Connection con = DataBaseUtil.getReadOnlyConnection();
		String sortField = (String)params.get(LISTING.SORTCOL);
		boolean sortReverse = (Boolean)params.get(LISTING.SORTASC);
		int pageSize = (Integer)params.get(LISTING.PAGESIZE);
		int pageNum = (Integer)params.get(LISTING.PAGENUM);
		SearchQueryBuilder qb = new
								SearchQueryBuilder(con,FIELDS,COUNT,FORM_TABLES,null,null,"pr.mr_no",false,25,pageNum);
		qb.addFilter(SearchQueryBuilder.DATE, "dp.meal_date", "=", DateUtil.parseDate(date));
		qb.addFilter(SearchQueryBuilder.STRING, "wn.ward_no", "=", ward);
		qb.addFilter(SearchQueryBuilder.STRING, "dm.meal_name", "=", mealName);
		qb.addFilter(SearchQueryBuilder.STRING, "pdp.meal_timing", "ILIKE", mealTiming);
		qb.addFilter(SearchQueryBuilder.STRING, "dp.status", "=", "N");

		qb.build();
		PreparedStatement psData = qb.getDataStatement();
		PreparedStatement psCount = qb.getCountStatement();

		int count = DataBaseUtil.getIntValueFromDb(psCount);

		List<BasicDynaBean> list = DataBaseUtil.queryToDynaList(psData);

		DataBaseUtil.closeConnections(null, psData);
		DataBaseUtil.closeConnections(con, psCount);
		return  new PagedList(list,count,25,pageNum);
	}



	private String  MEAL_FIELDS = "SELECT (SELECT string_agg(allergen_description, ', ') FROM patient_allergies pa LEFT JOIN allergen_master am ON am.allergen_code_id = pa.allergen_code_id LEFT JOIN allergy_type_master atm ON atm.allergy_type_id = pa.allergy_type_id WHERE atm.allergy_type_code='F' and pa.status = 'A' and pa.section_detail_id = psd.section_detail_id ) as food_allergies, pr.mr_no,coalesce ( bn.bed_name,'(Not allocated)') as bed_name,coalesce (wn.ward_name,'(Not allocated)') as ward_name, " +
	  							   " dm.meal_name,dp.meal_timing, get_patient_name(pd.salutation, pd.patient_name, pd.middle_name, pd.last_name) as patient_name,dp.ordered_id, dp.status, dp.ordered_time, dp.status_updated_time, dp.special_instructions, dt.doctor_name " ;

	private String MEAL_FORM_TABLES = " FROM diet_prescribed dp " +
									  " LEFT JOIN diet_master dm ON (dm.diet_id = dp.diet_id)" +
									  " JOIN patient_registration pr ON (pr.patient_id = dp.visit_id)" +
									  " LEFT JOIN ip_bed_details ibd ON (ibd.patient_id = pr.patient_id AND ibd.status IN ('A','C'))" +
									  " LEFT JOIN bed_names bn ON (bn.bed_id = ibd.bed_id)" +
									  " LEFT JOIN ward_names wn ON (wn.ward_no = bn.ward_no)" +
									  " JOIN patient_details pd ON (pd.mr_no = pr.mr_no AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )) "+
                    " LEFT JOIN patient_section_details psd ON (pd.mr_no = psd.mr_no AND psd.section_id = -2 and psd.section_status = 'A') " +
									  " LEFT JOIN doctors dt ON (dt.doctor_id = dp.ordered_by )";

	/**
	 *
	 *
	 * @param params is optional Ordered Date, Ward, Meal Name, Meal Time and Delivered
	 *
	 * @param listingParams
	 * @return  PagedList
	 * @throws SQLException
	 * @throws ParseException
	 */

	public PagedList searchMealsToBeDelivered(Map params,Map<LISTING, Object> listingParams, Boolean isNoFilter)
					throws SQLException, ParseException {

					Connection con = DataBaseUtil.getConnection();
					SearchQueryBuilder qb = null;
					int pageNum = (Integer) listingParams.get(LISTING.PAGENUM);
					String [] filterCenterIdStr = (String[]) params.get("pr.center_id");
					Integer userCenterId = RequestContext.getCenterId();
					try {
							qb = new SearchQueryBuilder(con, MEAL_FIELDS, COUNT,
							MEAL_FORM_TABLES, null, null, "pr.mr_no", false, 25, pageNum);

							qb.addFilterFromParamMap(params);
							if(filterCenterIdStr == null && !isNoFilter && userCenterId != 0){
							  qb.addFilter(qb.INTEGER, "pr.center_id", "=", userCenterId);
							 }
							qb.build();
							return qb.getMappedPagedList();
						} finally {
							if(qb!=null){
								qb.close();
								qb=null;
							}
							if(con!=null){
								con.close();
								con=null;
							}
						}
		}

	//For trend report

	private static String REVENUE_QUERY = "select  sum(amount) as total_amount,dp.diet_id,dm.meal_name, " +
			" case when dp.meal_timing like 'Spl %' then 'Spl' else dp.meal_timing end as time" +
			" from bill b " +
			" JOIN bill_charge bc on bc.bill_no = b.bill_no and b.status != 'X' and bc.status != 'X' and bc.charge_group = 'DIE' " +
			" JOIN diet_prescribed dp ON  (b.visit_id = dp.visit_id  AND dp.ordered_id::varchar = bc.act_description_id AND dp.status = 'Y') " +
			" JOIN diet_master dm ON (dm.diet_id = dp.diet_id) " +
			" WHERE date(dp.ordered_time) BETWEEN  ? AND ? " +
			" GROUP BY dp.diet_id,time,dm.meal_name  " +
			" ORDER BY dp.diet_id ";

	private static String GET_COUNT ="select count(meal_name),dm.meal_name,dm.diet_id," +
			" case when dp.meal_timing like 'Spl %' then 'Spl' else dp.meal_timing end as time  " +
			" from bill b " +
			" JOIN bill_charge bc on bc.bill_no = b.bill_no and b.status != 'X' and bc.status != 'X' and bc.charge_group = 'DIE' " +
			" JOIN diet_prescribed dp ON  (b.visit_id = dp.visit_id  AND dp.ordered_id::varchar = bc.act_description_id AND dp.status = 'Y') " +
			" JOIN diet_master dm ON (dm.diet_id = dp.diet_id) " +
			" WHERE date(dp.ordered_time) BETWEEN  ? AND ?  " +
			" GROUP BY dm.meal_name,dm.diet_id,time  " +
			" ORDER BY dm.diet_id ";

	public List<BasicDynaBean> getMealsRevenue(Date fromDate,Date toDate) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;

		con = DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(REVENUE_QUERY);
		ps.setDate(1, fromDate);
		ps.setDate(2, toDate);

		List<BasicDynaBean> list = DataBaseUtil.queryToDynaList(ps);
		DataBaseUtil.closeConnections(con, ps);
		return list;
	}

	public List<BasicDynaBean> getMealsCount(Date fromDate,Date toDate) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;

		con = DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(GET_COUNT);
		ps.setDate(1, fromDate);
		ps.setDate(2, toDate);

		List<BasicDynaBean> mealsList = DataBaseUtil.queryToDynaList(ps);
		DataBaseUtil.closeConnections(con, ps);
		return mealsList;
	}


	private String GET_DIET_PRES_FOR_VISIT = "  SELECT pdp.visit_id, pr.reg_date FROM patient_diet_prescriptions pdp " +
			"  JOIN patient_registration pr ON (pr.patient_id = pdp.visit_id) " +
			"  WHERE pdp.visit_id = ? " +
			"  GROUP BY pdp.visit_id,pr.reg_date ";

	private String GET_DIET_PRES_FOR_MRNO =
		"  SELECT pdp.visit_id, pr.reg_date FROM patient_diet_prescriptions pdp " +
		"  JOIN patient_registration pr ON (pr.patient_id = pdp.visit_id) " +
		"  WHERE pr.mr_no = ? " +
		"  GROUP BY pdp.visit_id,pr.reg_date ";
	public List<EMRDoc> getVisits(String visitId, String mrNo, boolean allVisitsDocs) throws SQLException, ParseException{
		Connection con = null;
		List<EMRDoc> list = new ArrayList<EMRDoc>();
		con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		if (allVisitsDocs) {
			ps = con.prepareStatement(GET_DIET_PRES_FOR_MRNO);
			ps.setString(1, mrNo);
		} else {
			ps = con.prepareStatement(GET_DIET_PRES_FOR_VISIT);
			ps.setString(1, visitId);
		}
		ResultSet rs = ps.executeQuery();
		BasicDynaBean printpref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
		while(rs.next()) {
			EMRDoc dtoObj = new EMRDoc();
			populateEMRPresc(dtoObj, rs, printpref);
			list.add(dtoObj);
		}
		DataBaseUtil.closeConnections(con, ps, rs);
		return list;
	}

	private static void populateEMRPresc(EMRDoc dtoObj, ResultSet rs, BasicDynaBean printpref)
	throws SQLException, ParseException {

		int printerId = (Integer) printpref.get("printer_id");
		dtoObj.setPrinterId(printerId);
		dtoObj.setVisitid(rs.getString("visit_id"));
		dtoObj.setProvider(EMRInterface.Provider.MealsPrescriptionsProvider);
		dtoObj.setDocid("");
		dtoObj.setTitle("MealPrescriptions");
		dtoObj.setType("SYS_RX");
		dtoObj.setDate(rs.getDate("reg_date"));
		dtoObj.setVisitDate(rs.getDate("reg_date"));
		dtoObj.setDoctor("");
		String displayUrl = "/dietary/DietaryMasterPrint.do?method=printPrescription&patient_id="
				+ rs.getString("visit_id") + "&printerId="+printerId ;

		dtoObj.setPdfSupported(true);
		dtoObj.setAuthorized(true);
		dtoObj.setDocid(rs.getString("visit_id"));

		dtoObj.setDisplayUrl(displayUrl);
	}



	 private static String DIET_CHART_EMR = "select dc.doc_id,dc.patient_id,dc.username," +
	 		" CASE WHEN (dat.title = '' or dat.title is null) THEN dat.template_name ELSE dat.title END AS title,dat.template_name, "
			+ " dat.access_rights,pr.reg_date FROM diet_chart_documents dc "
			+ " JOIN patient_registration pr USING (patient_id) "
			+ " JOIN patient_documents pd on pd.doc_id = dc.doc_id "
			+ " JOIN doc_all_templates_view dat ON (pd.doc_format=   dat.doc_format AND  dc.template_id=dat.template_id) ";

	public static List<EMRDoc> getDietaryChartForEMR(String patientId, String mrNo, boolean allVisitsDocs)
			throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;

		List<EMRDoc> docs = new ArrayList<EMRDoc>();
		List<BasicDynaBean> l = null;
		if (allVisitsDocs) {
			l = DataBaseUtil.queryToDynaList(DIET_CHART_EMR + " WHERE pr.mr_no=? ", mrNo);
		} else {
			l = DataBaseUtil.queryToDynaList(DIET_CHART_EMR + " WHERE dc.patient_id=? ", patientId);
		}
		BasicDynaBean printpref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
		int printerId = (Integer) printpref.get("printer_id");
		for (BasicDynaBean b : l) {
			EMRDoc doc = new EMRDoc();
			String accessRights = (String) b.get("access_rights");
			String userName = (String) b.get("username");

			doc.setPrinterId(printerId);
			String docId = b.get("doc_id").toString();
			doc.setDocid(docId);
			doc.setVisitid((String) b.get("patient_id"));
			doc.setProvider(EMRInterface.Provider.DietChartProvider);
			if (b.get("title") != null && !(b.get("title").equals("")))
				doc.setTitle((String) b.get("title"));
			else
				doc.setTitle((String) b.get("template_name"));
			doc.setDoctor("");
			String displayUrl = "/Dietary/DietaryGenericDocumentsPrint.do?_method=print&doc_id="
					+ docId + "&&forcePdf=true&printerId="+printerId + "&allFields=Y";
			doc.setDisplayUrl(displayUrl);
			doc.setPdfSupported(true);
			doc.setType("SYS_DIE");
			doc.setUpdatedBy(userName);
			doc.setAuthorized(EMRInterface.Helper.getAuthorized(userName, accessRights));
			doc.setAccessRights(accessRights);
			doc.setVisitDate((java.util.Date)b.get("reg_date"));
			docs.add(doc);
		}
		return docs;
	}

     private static final String DIET_NAMESANDMEAL_iDS="select meal_name,diet_id from diet_master";

     public static List getDietNamesAndMealIds() throws SQLException{
    	 return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(DIET_NAMESANDMEAL_iDS));
     }
     
     private static final String GET_DIETARY_ITEM_SUBGROUP_DETAILS = "select disg.item_subgroup_id,isg.item_subgroup_name,ig.item_group_id,item_group_name,igt.item_group_type_id,igt.item_group_type_name " +
 			" from dietary_item_sub_groups disg "+
 			" left join item_sub_groups isg on (isg.item_subgroup_id = disg.item_subgroup_id) "+
 			" left join diet_master dm on (dm.diet_id = disg.diet_id) "+
 			" left join item_groups ig on (ig.item_group_id = isg.item_group_id)"+
 			" left join item_group_type igt on (igt.item_group_type_id = ig.item_group_type_id)"+
 			" where disg.diet_id = ? ";

 		public static List<BasicDynaBean> getDietaryItemSubGroupDetails(int dietId) throws SQLException {
 			List list = null;
 			Connection con = null;
 		    PreparedStatement ps = null;
 		 try{
 			 con=DataBaseUtil.getReadOnlyConnection();
 			 ps=con.prepareStatement(GET_DIETARY_ITEM_SUBGROUP_DETAILS);
 			 ps.setInt(1, dietId);
 			 list = DataBaseUtil.queryToDynaList(ps);
 		 }finally {
 			 DataBaseUtil.closeConnections(con, ps);
 		 }
 		return list;
 		} 

     private static final String GET_DIETARY_ITEM_SUB_GROUP_TAX_DETAILS = "SELECT isg.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name, ig.group_code "+
				" FROM dietary_item_sub_groups disg "+
				" JOIN item_sub_groups isg ON(disg.item_subgroup_id = isg.item_subgroup_id) "+
				" JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) " +
				" WHERE disg.diet_id = ? ";
		
	 public List<BasicDynaBean> getDietaryItemSubGroupTaxDetails(String itemId) throws SQLException{
			Connection con = null;
			PreparedStatement ps = null;
			try {
				con = DataBaseUtil.getConnection();
				ps = con.prepareStatement(GET_DIETARY_ITEM_SUB_GROUP_TAX_DETAILS);
				ps.setInt(1, Integer.parseInt(itemId));
				return DataBaseUtil.queryToDynaList(ps);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
	}


  public List<BasicDynaBean> getActiveInsuranceCategories(int dietId) throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    try{
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(SELECT_INSURANCE_CATEGORY_IDS);
      ps.setInt(1, dietId);

      return DataBaseUtil.queryToDynaList(ps);
    } finally{
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String SELECT_INSURANCE_CATEGORY_IDS = "SELECT insurance_category_id "
      + "FROM diet_insurance_category_mapping WHERE diet_id =?";

}
