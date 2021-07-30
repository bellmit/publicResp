/**
 *
 */
package com.insta.hms.master.InsuranceCompanyTPAMaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class InsuranceCompanyTPAMasterDAO extends GenericDAO {

	static Logger logger = LoggerFactory.getLogger(InsuranceCompanyTPAMasterDAO.class);

	public InsuranceCompanyTPAMasterDAO() {
		super("insurance_company_tpa_master");
	}

	public static final String FIND_COMPANY_TPA = "SELECT insurance_co_id, tpa_id FROM insurance_company_tpa_master WHERE " +
			"	insurance_co_id = ? AND tpa_id = ? ";

	public BasicDynaBean findByCompanyTPA(Connection con, BasicDynaBean bean) throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(FIND_COMPANY_TPA);) {
		  ps.setString(1, (String)bean.get("insurance_co_id"));
	    ps.setString(2, (String)bean.get("tpa_id"));

	    return DataBaseUtil.queryToDynaBean(ps);
		}
	}


	private static final String INSURANCE_COMPANY_TPA_FIELDS = " SELECT *";

	private static final String INSURANCE_COMPANY_TPA_COUNT = " SELECT count(insurance_co_id) ";

	private static final String INSURANCE_COMPANY_TPA_TABLES = " from (SELECT ictm.insurance_co_id, ictm.tpa_id, icm.insurance_co_name, tm.tpa_name " +
			"  FROM insurance_company_tpa_master ictm " +
			"  LEFT JOIN tpa_master tm ON (tm.tpa_id = ictm.tpa_id) " +
			"  LEFT JOIN insurance_company_master  icm ON (icm.insurance_co_id = ictm.insurance_co_id) )AS foo ";

	public PagedList searchCompanyTPAList(Map map, Map pagingParams)throws ParseException, SQLException {

		Connection con = null;
		SearchQueryBuilder qb = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			qb = new SearchQueryBuilder(con, INSURANCE_COMPANY_TPA_FIELDS, INSURANCE_COMPANY_TPA_COUNT, INSURANCE_COMPANY_TPA_TABLES, pagingParams);
			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("insurance_co_id");
			qb.build();

			return qb.getMappedPagedList();
		}finally {
      if (null != qb) {
        qb.close();
      }
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String DELETE_TPA = "DELETE FROM insurance_company_tpa_master WHERE insurance_co_id = ? AND tpa_id = ? ";

	public static String deleteTPA(String[] insuranceCoIds, String[] tpaIds) throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			ps = con.prepareStatement(DELETE_TPA);

			for (int i=0; i<tpaIds.length;i++) {
				ps.setString(1, insuranceCoIds[i]);
				ps.setString(2, tpaIds[i]);
				ps.addBatch();
			}
			ps.executeBatch();

		} finally {
		  if(null != ps) {
		    ps.close();
		  }
			DataBaseUtil.commitClose(con, true);
		}
		return null;
	}

	private static final String GET_COMPANY_TPA_LIST = "SELECT ictm.insurance_co_id, ictm.tpa_id, " +
			" icm.insurance_co_name, tm.tpa_name, icm.status AS ins_co_status, tm.status AS tpa_status" +
			" FROM insurance_company_tpa_master ictm " +
			" LEFT JOIN tpa_master tm ON (tm.tpa_id = ictm.tpa_id) " +
			" LEFT JOIN insurance_company_master  icm ON (icm.insurance_co_id = ictm.insurance_co_id) " +
			" WHERE icm.status = 'A'  AND tm.status='A' " +
			" ORDER BY icm.insurance_co_name, tm.tpa_name ";
	
	private static final String GET_COMPANY_TPA_LIST_FOR_INSCO = "SELECT ictm.insurance_co_id, ictm.tpa_id, " +
      " icm.insurance_co_name, tm.tpa_name, icm.status AS ins_co_status, tm.status AS tpa_status" +
      " FROM insurance_company_tpa_master ictm " +
      " LEFT JOIN tpa_master tm ON (tm.tpa_id = ictm.tpa_id) " +
      " LEFT JOIN insurance_company_master  icm ON (icm.insurance_co_id = ictm.insurance_co_id) " +
      " WHERE icm.status = 'A'  AND tm.status='A' AND ictm.insurance_co_id = ? " +
      " ORDER BY icm.insurance_co_name, tm.tpa_name ";

	public List getCompanyTpaList() throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_COMPANY_TPA_LIST);
	}
	
  public List getCompanyTpaList(String insuranceCoId) throws SQLException {
    if (StringUtils.isNotEmpty(insuranceCoId)) {

      try (Connection con = DataBaseUtil.getConnection();
          PreparedStatement ps = con.prepareStatement(GET_COMPANY_TPA_LIST_FOR_INSCO)) {
        ps.setString(1, insuranceCoId);
        return DataBaseUtil.queryToDynaList(ps);
      }
    }
    return Collections.emptyList();
  }

	private static final String GET_COMPANY_TPA_XL_LIST = "SELECT ictm.insurance_co_id, ictm.tpa_id, " +
			" icm.insurance_co_name, tm.tpa_name, icm.status AS ins_co_status, tm.status AS tpa_status" +
			" FROM insurance_company_tpa_master ictm " +
			" LEFT JOIN tpa_master tm ON (tm.tpa_id = ictm.tpa_id) " +
			" LEFT JOIN insurance_company_master  icm ON (icm.insurance_co_id = ictm.insurance_co_id) " +
			" LEFT JOIN tpa_center_master  tcm ON (tcm.tpa_id = ictm.tpa_id) ";
	
	private static final String WHERE_COND_FOR_CENTER_XL_TPA_LIST	= " WHERE icm.status = 'A'  AND tm.status='A' " +
			" AND CASE WHEN tcm.center_id=-1 THEN tm.claim_format='XL' ELSE tcm.claim_format='XL' END "
			+ " AND (tcm.center_id=? OR tcm.center_id=-1) "
			+ " GROUP BY ictm.insurance_co_id, ictm.tpa_id, icm.insurance_co_name, tm.tpa_name, icm.status, tm.status "
			+ " ORDER BY icm.insurance_co_name, tm.tpa_name ";
	
	private static final String WHERE_COND_FOR_DEFAULT_CENTER_XL_TPA_LIST	= " WHERE icm.status = 'A'  AND tm.status='A' " +
			" AND CASE WHEN tcm.center_id=-1 THEN tm.claim_format='XL' ELSE tcm.claim_format='XL' END "
			+ " GROUP BY ictm.insurance_co_id, ictm.tpa_id, icm.insurance_co_name, tm.tpa_name, icm.status, tm.status " +
			" ORDER BY icm.insurance_co_name, tm.tpa_name ";
	
	private static final String WHERE_COND_FOR_NON_CENTER_XL_TPA_LIST	= " WHERE icm.status = 'A'  AND tm.status='A' " +
			" AND tm.claim_format='XL' "
			+ " GROUP BY ictm.insurance_co_id, ictm.tpa_id, icm.insurance_co_name, tm.tpa_name, icm.status, tm.status " +
			" ORDER BY icm.insurance_co_name, tm.tpa_name ";

	public List getCompanyTpaXLList() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null; 
		try {
			con = DataBaseUtil.getConnection();
			BasicDynaBean genericPref = GenericPreferencesDAO.getAllPrefs();
			Integer maxCenters = (Integer)genericPref.get("max_centers_inc_default");
			int centerId = RequestContext.getCenterId();
			if(maxCenters > 1) {
				if(centerId > 0) {
					ps = con.prepareStatement(GET_COMPANY_TPA_XL_LIST+WHERE_COND_FOR_CENTER_XL_TPA_LIST);
					ps.setInt(1, RequestContext.getCenterId());
					return DataBaseUtil.queryToDynaList(ps);	
				} else {
					ps = con.prepareStatement(GET_COMPANY_TPA_XL_LIST+WHERE_COND_FOR_DEFAULT_CENTER_XL_TPA_LIST);
					return DataBaseUtil.queryToDynaList(ps);	
				}
				
			} else {
				ps = con.prepareStatement(GET_COMPANY_TPA_XL_LIST+WHERE_COND_FOR_NON_CENTER_XL_TPA_LIST);
				return DataBaseUtil.queryToDynaList(ps);
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private static final String GET_ALL_TPA_XL_LIST = " SELECT  tm.tpa_id, tm.tpa_name, tm.status AS tpa_status "
			+ " FROM tpa_master tm "
			+ " LEFT JOIN tpa_center_master tcm ON (tcm.tpa_id = tm.tpa_id) ";
			//+ " LEFT JOIN insurance_company_tpa_master ictm ON (tm.tpa_id = ictm.tpa_id) "
			//+ " LEFT JOIN insurance_company_master icm on (icm.insurance_co_id = ictm.insurance_co_id AND icm.status = 'A') ";
	
	private static final String WHERE_COND_FOR_ALL_TPA_CENTER = " WHERE tm.status='A' "
			+ " AND CASE WHEN tcm.center_id=-1 THEN tm.claim_format='XL' ELSE tcm.claim_format='XL' END "
			+ " AND (tcm.center_id=? OR tcm.center_id=-1) "
			+ " GROUP BY tm.tpa_id "
			+ " ORDER BY tm.tpa_name ";
	
	private static final String WHERE_COND_FOR_ALL_TPA_DEFAULT_CENTER = " WHERE tm.status='A' "
			+ " AND CASE WHEN tcm.center_id=-1 THEN tm.claim_format='XL' ELSE tcm.claim_format='XL' END "
			+ " GROUP BY tm.tpa_id "
			+ " ORDER BY tm.tpa_name ";
	
	private static final String WHERE_COND_FOR_ALL_TPA_NON_CENTER = " WHERE tm.status='A' AND  tm.claim_format='XL' "
			+ " GROUP BY tm.tpa_id "
			+ " ORDER BY tm.tpa_name ";
	
	public List<BasicDynaBean> getAllXLTpaList() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null; 
		try {
			con = DataBaseUtil.getConnection();
			BasicDynaBean genericPref = GenericPreferencesDAO.getAllPrefs();
			Integer maxCenters = (Integer)genericPref.get("max_centers_inc_default");
			int centerId = RequestContext.getCenterId();
			if(maxCenters > 1) {
				if(centerId > 0) {
					ps = con.prepareStatement(GET_ALL_TPA_XL_LIST+WHERE_COND_FOR_ALL_TPA_CENTER);
					ps.setInt(1, RequestContext.getCenterId());
					return DataBaseUtil.queryToDynaList(ps);	
				} else {
					ps = con.prepareStatement(GET_ALL_TPA_XL_LIST+WHERE_COND_FOR_ALL_TPA_DEFAULT_CENTER);
					return DataBaseUtil.queryToDynaList(ps);
				}
			} else {
				ps = con.prepareStatement(GET_ALL_TPA_XL_LIST+WHERE_COND_FOR_ALL_TPA_NON_CENTER);
				return DataBaseUtil.queryToDynaList(ps);
			}
			
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private static final String GET_COMPANY_TPA_LIST_CENTER_WISE = " SELECT ictm.insurance_co_id, ictm.tpa_id, " +
			 "  icm.insurance_co_name, tm.tpa_name, icm.status AS ins_co_status, tm.status AS tpa_status" +
			 " FROM insurance_company_tpa_master ictm " +
			 " LEFT JOIN tpa_master tm ON (tm.tpa_id = ictm.tpa_id) " +
			 " LEFT JOIN insurance_company_master icm ON (icm.insurance_co_id = ictm.insurance_co_id) " +
			 " WHERE icm.status = 'A' AND tm.status='A' " +
			 " and icm.insurance_co_id in " +
			 " (select insurance_co_id from insurance_category_master where category_id in " +
			 " (select category_id from insurance_category_center_master where center_id in (?,0))) " +
			 " ORDER BY icm.insurance_co_name, tm.tpa_name ";

		public List getCompanyTpaList(int centerId) throws SQLException {
			return DataBaseUtil.queryToDynaList(GET_COMPANY_TPA_LIST_CENTER_WISE,centerId);
		}

		
	private static final String GET_IP_CATEGORY_COMPANY_TPA_LIST = "SELECT ictm.insurance_co_id, ictm.tpa_id, " +
		" icm.insurance_co_name, tm.tpa_name, icm.status AS ins_co_status, tm.status AS tpa_status" +
		" FROM insurance_company_tpa_master ictm " +
		" JOIN (SELECT regexp_split_to_table(foo.arp, E',') AS insurance_co_id "+
				" FROM ( "+
				" SELECT  "+
				" 	CASE  "+
				" 		WHEN ip_allowed_insurance_co_ids ='*' "+
				" 		THEN ( "+
				" 			SELECT array_to_string ( "+
				" 				ARRAY(  "+
				" 					SELECT insurance_co_id::text FROM insurance_company_master "+
				" 					WHERE status='A'  "+
				" 				),  "+
				" 			',') "+
				" 		     ) "+
				" 		ELSE ip_allowed_insurance_co_ids  "+
				" 	END AS arp  "+
				" 	FROM patient_category_master  "+
				" 	WHERE category_id = ? ) AS foo) AS catins ON (catins.insurance_co_id = ictm.insurance_co_id)" +
		" LEFT JOIN tpa_master tm ON (tm.tpa_id = ictm.tpa_id) " +
		" LEFT JOIN insurance_company_master icm ON (icm.insurance_co_id = ictm.insurance_co_id) " +
		" WHERE icm.status = 'A'  AND tm.status='A' " +
		" ORDER BY icm.insurance_co_name, tm.tpa_name ";

	private static final String GET_OP_CATEGORY_COMPANY_TPA_LIST =  "SELECT ictm.insurance_co_id, ictm.tpa_id, " +
		" icm.insurance_co_name, tm.tpa_name, icm.status AS ins_co_status, tm.status AS tpa_status" +
		" FROM insurance_company_tpa_master ictm " +
		" JOIN (SELECT regexp_split_to_table(foo.arp, E',') AS insurance_co_id "+
				" FROM ( "+
				" SELECT  "+
				" 	CASE  "+
				" 		WHEN op_allowed_insurance_co_ids ='*' "+
				" 		THEN ( "+
				" 			SELECT array_to_string ( "+
				" 				ARRAY(  "+
				" 					SELECT insurance_co_id::text FROM insurance_company_master "+
				" 					WHERE status='A'  "+
				" 				),  "+
				" 			',') "+
				" 		     ) "+
				" 		ELSE op_allowed_insurance_co_ids  "+
				" 	END AS arp  "+
				" 	FROM patient_category_master  "+
				" 	WHERE category_id = ? ) AS foo) AS catins ON (catins.insurance_co_id = ictm.insurance_co_id)" +
		" LEFT JOIN tpa_master tm ON (tm.tpa_id = ictm.tpa_id) " +
		" LEFT JOIN insurance_company_master icm ON (icm.insurance_co_id = ictm.insurance_co_id) " +
		" WHERE icm.status = 'A' AND tm.status='A' " +
		" ORDER BY icm.insurance_co_name, tm.tpa_name ";

	public List<BasicDynaBean> getCategoryCompanyTpaList(int patient_category_id, String visitType) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			if(visitType.equals("i"))
				ps = con.prepareStatement(GET_IP_CATEGORY_COMPANY_TPA_LIST);
			else
				ps = con.prepareStatement(GET_OP_CATEGORY_COMPANY_TPA_LIST);
			ps.setInt(1, patient_category_id);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
}
