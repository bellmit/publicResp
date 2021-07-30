/**
 *
 */
package com.insta.hms.master.PerDiemCodes;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @author lakshmi
 *
 */
public class PerDiemCodesDAO extends GenericDAO {

	public PerDiemCodesDAO() {
		super("per_diem_codes_master");
	}

	private static final String SEARCH_FIELDS = "select *";

	private static final String SEARCH_COUNT =  " SELECT count(*) ";

	private static final String SEARCH_TABLES =
		" FROM (SELECT pm.per_diem_code, pm.per_diem_description, pm.status " +
		" 	FROM per_diem_codes_master pm )  AS foo " ;

	public PagedList search(Map requestParams, Map pagingParams) throws ParseException, SQLException {

		Connection con = null;
		SearchQueryBuilder qb = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			qb = new SearchQueryBuilder(con, SEARCH_FIELDS, SEARCH_COUNT, SEARCH_TABLES, pagingParams);
			qb.addFilterFromParamMap(requestParams);
			qb.addSecondarySort("per_diem_code");
			qb.build();

			return qb.getMappedPagedList();
		} finally {
			qb.close();
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public static final String GET_PERDIEM_CODES =
		" SELECT per_diem_description || ' (' || per_diem_code || ') ' AS per_diem_description, per_diem_code " +
		" FROM per_diem_codes_master WHERE status='A' ORDER BY per_diem_description ";

	@SuppressWarnings("unchecked")
	public static List<BasicDynaBean> getPerDiemCodes() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		List<BasicDynaBean> perdiemCodesList = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_PERDIEM_CODES);
			perdiemCodesList = DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return perdiemCodesList ;
	}

	@SuppressWarnings("unchecked")
	public static List<BasicDynaBean> getPerDiemCodes(Connection con) throws SQLException {
    PreparedStatement ps = null;
    List<BasicDynaBean> perdiemCodesList = null;
    try{
      ps = con.prepareStatement(GET_PERDIEM_CODES);
      perdiemCodesList = DataBaseUtil.queryToDynaList(ps);
    }finally{
      DataBaseUtil.closeConnections(null, ps);
    }
    return perdiemCodesList ;
  }
	
	public static final String GET_PERDIEM =
		" SELECT pm.per_diem_code, pm.code_type, pm.status, pm.per_diem_description, " +
		" pm.service_groups_incl, pm.service_groups_names, " +
		" pmc.charge " +
		" FROM per_diem_codes_master pm " +
		" JOIN per_diem_codes_charges pmc USING (per_diem_code) " +
		" WHERE pm.status='A' ";

	public static final String BILL_PERDIEM =
		" SELECT pm.per_diem_code, pm.code_type, pm.status, pm.per_diem_description, " +
		" pm.service_groups_incl, pm.service_groups_names, " +
		" pmc.charge " +
		" FROM per_diem_codes_master pm " +
		" JOIN per_diem_codes_charges pmc USING (per_diem_code) " +
		" WHERE pm.per_diem_code = ? " ;

	public static final String GET_PERDIEM_CHARGES = GET_PERDIEM + " AND pmc.bed_type=? AND pmc.org_id=? " +
		" UNION " +
		BILL_PERDIEM + " AND pmc.bed_type=? AND pmc.org_id=? " +
		" UNION " +
		GET_PERDIEM + " AND pmc.bed_type='GENERAL' AND pmc.org_id='ORG0001' " +
		" AND NOT EXISTS (SELECT per_diem_code FROM per_diem_codes_charges WHERE per_diem_code = pm.per_diem_code " +
		"                 AND bed_type=? AND org_id=?) ";

	@SuppressWarnings("unchecked")
	public List<BasicDynaBean> getPerdiemCharges(String orgid,
				String bedType, String perdiemCode) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		List<BasicDynaBean> list = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_PERDIEM_CHARGES);
			ps.setString(1, bedType);
			ps.setString(2, orgid);
			ps.setString(3, perdiemCode);
			ps.setString(4, bedType);
			ps.setString(5, orgid);
			ps.setString(6, bedType);
			ps.setString(7, orgid);
			list = DataBaseUtil.queryToDynaList(ps);
		} finally {
			if (ps != null) ps.close();
			if (con != null) con.close();
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
  public List<BasicDynaBean> getPerdiemCharges(Connection con, String orgid,
        String bedType, String perdiemCode) throws SQLException {
    PreparedStatement ps = null;
    List<BasicDynaBean> list = null;
    try {
      ps = con.prepareStatement(GET_PERDIEM_CHARGES);
      ps.setString(1, bedType);
      ps.setString(2, orgid);
      ps.setString(3, perdiemCode);
      ps.setString(4, bedType);
      ps.setString(5, orgid);
      ps.setString(6, bedType);
      ps.setString(7, orgid);
      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (ps != null) ps.close();
    }
    return list;
  }

	public static final String GET_BILL_PERDIEM_CHARGES =
		BILL_PERDIEM + " AND pmc.bed_type=? AND pmc.org_id=? ";

	public BasicDynaBean getBillPerdiemCharge(String orgid,
				String bedType, String perdiemCode) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_BILL_PERDIEM_CHARGES);
			ps.setString(1, perdiemCode);
			ps.setString(2, bedType);
			ps.setString(3, orgid);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_MRD_CODES_PERDIEM_CODES =
		" SELECT code_desc || ' (' || code || ') ' AS per_diem_description, " +
		" code AS per_diem_code, code_desc " +
		" FROM mrd_codes_master WHERE code_type='Service Code' AND status='A' ORDER BY code_desc ";

	@SuppressWarnings("unchecked")
	public static List<BasicDynaBean> getMrdCodesPerDiemCodes() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		List<BasicDynaBean> perdiemCodesList = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_MRD_CODES_PERDIEM_CODES);
			perdiemCodesList = DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return perdiemCodesList ;
	}
	
	private static final String GET_PERDIEM_ITEM_SUBGROUP_DETAILS = "select pcisg.item_subgroup_id,isg.item_subgroup_name,ig.item_group_id,item_group_name,igt.item_group_type_id,igt.item_group_type_name " +
			" from perdiem_code_item_sub_groups pcisg "+
			" left join item_sub_groups isg on (isg.item_subgroup_id = pcisg.item_subgroup_id) "+
			" left join per_diem_codes_master pdcm on (pdcm.per_diem_code = pcisg.per_diem_code) "+
			" left join item_groups ig on (ig.item_group_id = isg.item_group_id)"+
			" left join item_group_type igt on (igt.item_group_type_id = ig.item_group_type_id)"+
			" where pcisg.per_diem_code = ? "; 

		public static List<BasicDynaBean> getPerdiemSubGroupDetails(String perdiemCode) throws SQLException {
			List list = null;
			Connection con = null;
		    PreparedStatement ps = null;
		 try{
			 con=DataBaseUtil.getReadOnlyConnection();
			 ps=con.prepareStatement(GET_PERDIEM_ITEM_SUBGROUP_DETAILS);
			 ps.setString(1, perdiemCode);
			 list = DataBaseUtil.queryToDynaList(ps);
		 }finally {
			 DataBaseUtil.closeConnections(con, ps);
		 }
		return list;
		}

}
