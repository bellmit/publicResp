package com.insta.hms.master.InsuranceCategoryMaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.map.MultiValueMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsuranceCategoryMasterDAO extends GenericDAO {

	public  InsuranceCategoryMasterDAO() {
		super("insurance_category_master");
	}

	private static String CATEGORY_MASTER_QUERY_COUNT = " SELECT COUNT(*) ";

	private static String CATEGORY_MASTER_QUERY_SELECT = " SELECT icam.*, insurance_co_name ";

	private static String CATEGORY_MASTER_QUERY_TABLES = " FROM insurance_category_master icam LEFT JOIN (SELECT insurance_co_id, insurance_co_name FROM  insurance_company_master) AS icom USING (insurance_co_id) ";

	private static String CATEGORY_MASTER_QUERY_SELECT_CENTER = " SELECT icam.*, insurance_co_name ,iccm.center_id";

	private static String CATEGORY_MASTER_QUERY_TABLES_CENTER =" FROM insurance_category_master icam " +
													" LEFT JOIN (SELECT center_id,category_id from  insurance_category_center_master) AS iccm USING (category_id) "+
													" LEFT JOIN (SELECT insurance_co_id, insurance_co_name " +
													" FROM  insurance_company_master) AS icom USING (insurance_co_id) ";

	public static PagedList getRecords(Map requestParams, Map<LISTING, Object> pagingParams) throws SQLException,
			ParseException {
		Connection con = null;
		SearchQueryBuilder qb = null;
		List list = new ArrayList();
		int centerID = RequestContext.getCenterId();
		try {
			con = DataBaseUtil.getConnection();

			if(centerID != 0 && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
				qb = new SearchQueryBuilder(con, CATEGORY_MASTER_QUERY_SELECT_CENTER, CATEGORY_MASTER_QUERY_COUNT, CATEGORY_MASTER_QUERY_TABLES_CENTER, pagingParams);
				qb.addFilterFromParamMap(requestParams);
				list.add(RequestContext.getCenterId());
				list.add(0);
				qb.addFilter(qb.INTEGER, "center_id", "IN", list);
			} else {
				qb = new SearchQueryBuilder(con, CATEGORY_MASTER_QUERY_SELECT, CATEGORY_MASTER_QUERY_COUNT, CATEGORY_MASTER_QUERY_TABLES, pagingParams);
				qb.addFilterFromParamMap(requestParams);
			}

				qb.addSecondarySort("category_id");
				qb.build();

				return qb.getMappedPagedList();
		} finally {
		  if(null != qb) {
		    qb.close();
		  }
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public static final String CATEGORY_DETAILS= " SELECT icam.*, insurance_co_name FROM insurance_category_master icam  " +
	 " JOIN (SELECT insurance_co_id, insurance_co_name FROM  insurance_company_master) AS icom USING(insurance_co_id) WHERE category_id = ? ";

	public BasicDynaBean getCategoryDetails(int categoryId)
	throws SQLException {
		PreparedStatement ps = null;
		BasicDynaBean catDetail = null;
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(CATEGORY_DETAILS);
			ps.setInt(1, categoryId);
			catDetail = DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return catDetail;
	}


	private static final String INSURANCECOMPANIES_NAMESAND_iDS="select insurance_co_id,insurance_co_name from insurance_company_master";

	public static List getInsuranceCompaniesNamesAndIds() throws SQLException {
		   return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(INSURANCECOMPANIES_NAMESAND_iDS));
	}

	private static final String INSURANCECATEGORIES_NAMESAND_iDS="select category_id,category_name from insurance_category_master";

	public static MultiValueMap getInsuranceCategoriesNamesAndIdsMultiMap () throws SQLException {
		List<BasicDynaBean> lst = DataBaseUtil.queryToDynaList(INSURANCECATEGORIES_NAMESAND_iDS);
		MultiValueMap h = new MultiValueMap();
		for(BasicDynaBean b : lst) {
			h.put(b.get("category_name"), b.get("category_id"));
		}
		return h;
	}

	private static final String INSURANCE_NAMES_AND_CAT_iDS="select category_id,insurance_co_id from insurance_category_master";

	public static Map getInsuranceNamesAndCatIdsMap () throws SQLException {
		List<BasicDynaBean> lst = DataBaseUtil.queryToDynaList(INSURANCE_NAMES_AND_CAT_iDS);
		Map h = new HashMap();
		for(BasicDynaBean b : lst) {
			h.put(b.get("category_id"), b.get("insurance_co_id"));
		}
		return h;
	}


	private static final String INSURANCE_CATEGORIES_AUTOCOMPLETE_QUERY = " " +
			" SELECT category_name ||' ('|| insurance_co_name || ')' AS category_display, category_id , category_name " +
			" FROM insurance_category_master " +
			" LEFT JOIN insurance_company_master USING(insurance_co_id) ORDER BY category_name ";

	public static List getInsuranceCategoryAutoCmpltList() throws SQLException {
		return DataBaseUtil.queryToDynaList(INSURANCE_CATEGORIES_AUTOCOMPLETE_QUERY);
	}
	private static final String CENTERWISE_INSURANCE_CATEGORIES_ACTIVE_LISTQUERY = "SELECT distinct icm.category_id,icm.insurance_co_id,icm.category_name " +
									" from insurance_category_master icm" +
									" LEFT JOIN insurance_category_center_master iccm ON(icm.category_id=iccm.category_id)"+
									" WHERE icm.status='A' AND (iccm.center_id=? or iccm.center_id=0 or coalesce(iccm.center_id,0)=0 ) and " +
									" coalesce(iccm.status,'A')='A'  order by category_name";

	private static final String INSURANCE_CATEGORIES_ACTIVE_LISTQUERY = "SELECT distinct icm.category_id,icm.insurance_co_id,icm.category_name " +
							" from insurance_category_master icm" +
							" LEFT JOIN insurance_category_center_master iccm ON(icm.category_id=iccm.category_id)"+
							" WHERE icm.status='A' and coalesce(iccm.status,'A')='A'  order by category_name";

	public static List getInsuranceCategoryActiveList() throws SQLException {
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		List<BasicDynaBean> categoryList = null;
		int centerId = RequestContext.getCenterId();
		try {
			if(centerId != 0 && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1){
				ps = con.prepareStatement(CENTERWISE_INSURANCE_CATEGORIES_ACTIVE_LISTQUERY);
				ps.setInt(1, centerId);
			} else {
				ps = con.prepareStatement(INSURANCE_CATEGORIES_ACTIVE_LISTQUERY);
			}
			categoryList = DataBaseUtil.queryToDynaList(ps);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return categoryList;
	}

	private static final String GET_PLAN_TYPE = "SELECT * FROM insurance_category_master where category_id=?";

	public static BasicDynaBean getPlanTypeDetails(int categoryID) throws SQLException {
		PreparedStatement ps = null;
		Connection con = DataBaseUtil.getConnection();
		try {
			ps = con.prepareStatement(GET_PLAN_TYPE);
			ps.setInt(1, categoryID);
			return (BasicDynaBean)DataBaseUtil.queryToDynaList(ps).get(0);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_CATEGORY_MASTER__INC_SUPER_CENTER = "SELECT distinct icm.category_id,icm.insurance_co_id,icm.category_name,icm.status " +
											" from insurance_category_master icm" +
											" LEFT JOIN insurance_category_center_master iccm ON(icm.category_id=iccm.category_id)"+
											" WHERE icm.status='A' AND (iccm.center_id=? or iccm.center_id=0) and iccm.status='A'  order by category_name";

	private static final String GET_CATEGORY_MASTER__INC_SUPER = "SELECT distinct icm.category_id,icm.insurance_co_id,icm.category_name,icm.status" +
							" from insurance_category_master icm" +
							" LEFT JOIN insurance_category_center_master iccm ON(icm.category_id=iccm.category_id)"+
							" WHERE icm.status='A' AND iccm.status='A'  order by category_name";

	public static List<BasicDynaBean> getInsCatCenter(int centerId) throws SQLException {

		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		List<BasicDynaBean> categoryList = null;
		try {
			if(centerId != 0 && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1){
				ps = con.prepareStatement(GET_CATEGORY_MASTER__INC_SUPER_CENTER);
				ps.setInt(1, centerId);
			} else {
				ps = con.prepareStatement(GET_CATEGORY_MASTER__INC_SUPER);
			}
			categoryList = DataBaseUtil.queryToDynaList(ps);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

		return categoryList;

	}

	private static final String GET_EDIT_VISIT__INC_CAT= "SELECT icm.category_id,icm.insurance_co_id,icm.category_name,icm.status "+
					 " FROM insurance_category_master icm "+
					 " LEFT JOIN insurance_category_center_master iccm ON(icm.category_id=iccm.category_id)"+
					 " WHERE icm.status='A' AND (iccm.center_id=? or iccm.center_id=0) and iccm.status='A'"+
					 " UNION "+
					 " SELECT icm.category_id,icm.insurance_co_id,icm.category_name,icm.status  "+
					 " FROM patient_registration pr "+
					 " JOIN insurance_category_master icm ON (pr.category_id=icm.category_id) "+
					 "  WHERE patient_id  = ?  ORDER BY category_name";

	private static final String GET_EDIT_VISIT__INC_CAT_WITH_INSCOM= "SELECT icm.category_id,icm.insurance_co_id,icm.category_name,icm.status "+
			 " FROM insurance_category_master icm "+
			 " LEFT JOIN insurance_category_center_master iccm ON(icm.category_id=iccm.category_id)"+
			 " WHERE icm.status='A' AND (iccm.center_id=? or iccm.center_id=0) AND insurance_co_id =? and iccm.status='A'"+
			 " UNION "+
			 " SELECT icm.category_id,icm.insurance_co_id,icm.category_name,icm.status  "+
			 " FROM patient_registration pr "+
			 " JOIN insurance_category_master icm ON (pr.category_id=icm.category_id) "+
			 "  WHERE patient_id  = ?  ORDER BY category_name";

	public static List<BasicDynaBean> getEditInsCatCenter(String visitId, String insCompanyId) throws SQLException {
		int centerId = RequestContext.getCenterId();
		PreparedStatement ps = null;
		List<BasicDynaBean> editInscatList = null;
		Connection con = null;
		List<String> list = new ArrayList<String>();
		list.add("center_id");
		Map<String, Object> identifiers = new HashMap<String, Object>();
		identifiers.put("patient_id", visitId);

		try {
			con = DataBaseUtil.getConnection();
			BasicDynaBean patientRegBean = new GenericDAO("patient_registration").findByKey(con, list, identifiers);//It should come visit_id with corresponding center_id in patient_registration.
			if(centerId == 0 && patientRegBean != null && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1)
				centerId = (Integer)patientRegBean.get("center_id");
					if (insCompanyId != null) {
						ps = con.prepareStatement(GET_EDIT_VISIT__INC_CAT_WITH_INSCOM);
						ps.setInt(1, centerId);
						ps.setString(2, insCompanyId);
						ps.setString(3, visitId);
					} else {
						ps = con.prepareStatement(GET_EDIT_VISIT__INC_CAT);
						ps.setInt(1, centerId);
						ps.setString(2, visitId);
					}
					editInscatList = DataBaseUtil.queryToDynaList(ps);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
		}
		return editInscatList;
	}
	
	private static final String GET_CATEGORY_MASTER_WITH_CENTER_AND_INSCOMP = "SELECT distinct icm.category_id,icm.insurance_co_id,icm.category_name,icm.status " +
			" from insurance_category_master icm" +
			" LEFT JOIN insurance_category_center_master iccm ON(icm.category_id=iccm.category_id)"+
			" WHERE icm.status='A' AND (iccm.center_id=? or iccm.center_id=0)  AND icm.insurance_co_id =?"+
			" and iccm.status='A'  order by category_name";

	private static final String GET_CATEGORY_MASTER_WITH_INSCOMP = "SELECT distinct icm.category_id,icm.insurance_co_id,icm.category_name,icm.status" +
			" from insurance_category_master icm" +
			" WHERE icm.status='A' AND icm.insurance_co_id = ?  order by category_name";
	
	public static List<BasicDynaBean> getInsCategory(int centerId, String insCompanyId) throws SQLException {

		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		List<BasicDynaBean> categoryList = null;
		try {
			if(centerId != 0 && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1){
				ps = con.prepareStatement(GET_CATEGORY_MASTER_WITH_CENTER_AND_INSCOMP);
				ps.setInt(1, centerId);
				ps.setString(2, insCompanyId);
			} else {
				ps = con.prepareStatement(GET_CATEGORY_MASTER_WITH_INSCOMP);
				ps.setString(1, insCompanyId);
			}
			categoryList = DataBaseUtil.queryToDynaList(ps);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

		return categoryList;

	}

}

