package com.insta.hms.master.DynaPackage;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BasicCachingDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class DynaPackageDAO extends BasicCachingDAO {

	public DynaPackageDAO() {
		super("dyna_packages");
	}

	private static final String SEARCH_FIELDS = "select *";

	private static final String SEARCH_COUNT =  " SELECT count(*) ";

	private static final String SEARCH_TABLES =
		" FROM (SELECT dod.dyna_package_id, dod.org_id, dod.applicable, dod.item_code, " +
		" dp.dyna_package_name, dp.status,  dod.code_type, od.org_name,'dynapackages'::text as chargeCategory, " +
		" dod.is_override "+
		" FROM dyna_package_org_details dod " +
		" JOIN dyna_packages dp ON (dp.dyna_package_id = dod.dyna_package_id)" +
		" JOIN organization_details od ON(od.org_id = dod.org_id))  AS foo " ;

	public PagedList search(Map requestParams, Map pagingParams) throws ParseException, SQLException {

		Connection con = null;
		SearchQueryBuilder qb = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			qb = new SearchQueryBuilder(con, SEARCH_FIELDS, SEARCH_COUNT, SEARCH_TABLES, pagingParams);
			qb.addFilterFromParamMap(requestParams);
			qb.addSecondarySort("dyna_package_id");
			qb.build();

			return qb.getMappedPagedList();
		} finally {
			qb.close();
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public static final String DYNA_PACKAGE_DETAILS ="SELECT dp.*, dod.org_id, dod.applicable, dod.item_code," +
			" o.org_name, dod.code_type " +
			" FROM dyna_packages dp " +
			" JOIN dyna_package_org_details dod ON (dod.dyna_package_id = dp.dyna_package_id) " +
			" JOIN organization_details o ON (o.org_id = dod.org_id) " +
			" WHERE dp.dyna_package_id=? AND dod.org_id=? ";

	public BasicDynaBean getDynaPackageDetailsBean(int dynaPackageId,String orgId)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(DYNA_PACKAGE_DETAILS);
			ps.setInt(1, dynaPackageId);
			ps.setString(2, orgId);
			return DataBaseUtil.queryToDynaBean(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	 private static final String GET_DYNA_PACKAGE_DETAILS="SELECT dp.dyna_package_id," +
	 		"dp.dyna_package_name,dp.status FROM dyna_packages dp WHERE status='A' ORDER BY dyna_package_id";

	public static List<BasicDynaBean> getDynaPackageDetails() throws SQLException{

		List packageList=null;
		PreparedStatement ps=null;
		Connection con=null;
		con=DataBaseUtil.getReadOnlyConnection();
		ps=con.prepareStatement(GET_DYNA_PACKAGE_DETAILS);
		packageList = DataBaseUtil.queryToDynaList(ps);
		DataBaseUtil.closeConnections(con, ps);

		return packageList;
	}

	public static final String GET_DYNAPKG_CHARGE =
		" SELECT dp.dyna_package_id, dp.status, dp.dyna_package_name, dpc.charge, dpo.item_code, dp.excluded_amt_claimable " +
		" FROM dyna_packages dp " +
		" JOIN dyna_package_charges dpc USING (dyna_package_id) " +
		" JOIN dyna_package_org_details dpo ON (dp.dyna_package_id = dpo.dyna_package_id  AND dpo.org_id=?  AND dpo.applicable = true)" +
		" WHERE dp.status='A' ";

	public static final String BILL_DYNAPKG_CHARGE =
		" SELECT dp.dyna_package_id, dp.status, dp.dyna_package_name, dpc.charge, dpo.item_code,  dp.excluded_amt_claimable  " +
		" FROM dyna_packages dp " +
		" JOIN dyna_package_charges dpc USING (dyna_package_id) " +
		" JOIN dyna_package_org_details dpo ON (dp.dyna_package_id = dpo.dyna_package_id  AND dpo.org_id=?  AND dpo.applicable = true)" +
		" WHERE dp.dyna_package_id = ? " ;

	public static final String DYNAPKG_CHARGES = GET_DYNAPKG_CHARGE + " AND dpc.bed_type=? AND dpc.org_id=? " +
		" UNION " +
		BILL_DYNAPKG_CHARGE + " AND dpc.bed_type=? AND dpc.org_id=? " +
		" UNION " +
		GET_DYNAPKG_CHARGE + " AND dpc.bed_type='GENERAL' AND dpc.org_id='ORG0001' " +
		" AND NOT EXISTS (SELECT dyna_package_id FROM dyna_package_charges WHERE dyna_package_id = dp.dyna_package_id " +
		"                 AND bed_type=? AND org_id=?) ";

	@SuppressWarnings("unchecked")
	public List getDynaPackages(String orgid, String bedType, int dynaPkgId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		List<BasicDynaBean> list = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(DYNAPKG_CHARGES);
			ps.setString(1, orgid);
			ps.setString(2, bedType);
			ps.setString(3, orgid);
			ps.setString(4, orgid);
			ps.setInt(5, dynaPkgId);
			ps.setString(6, bedType);
			ps.setString(7, orgid);
			ps.setString(8, orgid);
			ps.setString(9, bedType);
			ps.setString(10, orgid);
			list = DataBaseUtil.queryToDynaList(ps);
		} finally {
			if (ps != null) ps.close();
			if (con != null) con.close();
		}
		return list;
	}

	public List getDynaPackages(Connection con, String orgid, String bedType, int dynaPkgId) throws SQLException {
    PreparedStatement ps = null;
    List<BasicDynaBean> list = null;
    try {
      ps = con.prepareStatement(DYNAPKG_CHARGES);
      ps.setString(1, orgid);
      ps.setString(2, bedType);
      ps.setString(3, orgid);
      ps.setString(4, orgid);
      ps.setInt(5, dynaPkgId);
      ps.setString(6, bedType);
      ps.setString(7, orgid);
      ps.setString(8, orgid);
      ps.setString(9, bedType);
      ps.setString(10, orgid);
      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (ps != null) ps.close();
    }
    return list;
  }
	
	public static final String GET_DYNAPKG =
		" SELECT dp.dyna_package_id, dp.status, dp.dyna_package_name, dpc.charge, " +
		" dcl.dyna_pkg_cat_id, dcat.dyna_pkg_cat_name, dcl.pkg_included, dcl.amount_limit, " +
		" dcl.qty_limit, dpo.item_code, dcat.limit_type " +
		" FROM dyna_packages dp " +
		" JOIN dyna_package_charges dpc USING (dyna_package_id) " +
		" JOIN dyna_package_category_limits dcl ON (dcl.dyna_package_id = dp.dyna_package_id AND dcl.org_id = dpc.org_id AND dcl.bed_type = dpc.bed_type) " +
		" JOIN dyna_package_category dcat ON (dcat.dyna_pkg_cat_id = dcl.dyna_pkg_cat_id) " +
		" JOIN dyna_package_org_details dpo ON (dp.dyna_package_id = dpo.dyna_package_id  AND dpo.org_id=?  AND dpo.applicable = true)" +
		" WHERE dp.status='A' AND dcl.pkg_included = 'Y' ";

	public static final String BILL_DYNAPKG =
		" SELECT dp.dyna_package_id, dp.status, dp.dyna_package_name, dpc.charge, " +
		" dcl.dyna_pkg_cat_id, dcat.dyna_pkg_cat_name, dcl.pkg_included, dcl.amount_limit, " +
		" dcl.qty_limit, dpo.item_code, dcat.limit_type " +
		" FROM dyna_packages dp " +
		" JOIN dyna_package_charges dpc USING (dyna_package_id) " +
		" JOIN dyna_package_category_limits dcl ON (dcl.dyna_package_id = dp.dyna_package_id AND dcl.org_id = dpc.org_id AND dcl.bed_type = dpc.bed_type) " +
		" JOIN dyna_package_category dcat ON (dcat.dyna_pkg_cat_id = dcl.dyna_pkg_cat_id) " +
		" JOIN dyna_package_org_details dpo ON (dp.dyna_package_id = dpo.dyna_package_id  AND dpo.org_id=?  AND dpo.applicable = true)" +
		" WHERE dp.dyna_package_id = ? AND dcl.pkg_included = 'Y' " ;

	public static final String GET_DYNAPKG_CHARGES = GET_DYNAPKG + " AND dpc.bed_type=? AND dpc.org_id=? " +
		" UNION " +
		BILL_DYNAPKG + " AND dpc.bed_type=? AND dpc.org_id=? " +
		" UNION " +
		GET_DYNAPKG + " AND dpc.bed_type='GENERAL' AND dpc.org_id='ORG0001' " +
		" AND NOT EXISTS (SELECT dyna_package_id FROM dyna_package_charges WHERE dyna_package_id = dp.dyna_package_id " +
		"                 AND bed_type=? AND org_id=?) ";

	public List<BasicDynaBean> getDynaPackageCharges(String orgid, String bedType, int dynaPkgId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		List<BasicDynaBean> list = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_DYNAPKG_CHARGES);
			ps.setString(1, orgid);
			ps.setString(2, bedType);
			ps.setString(3, orgid);
			ps.setString(4, orgid);
			ps.setInt(5, dynaPkgId);
			ps.setString(6, bedType);
			ps.setString(7, orgid);
			ps.setString(8, orgid);
			ps.setString(9, bedType);
			ps.setString(10, orgid);
			list = DataBaseUtil.queryToDynaList(ps);
		} finally {
			if (ps != null) ps.close();
			if (con != null) con.close();
		}
		return list;
	}

	public static final String GET_BILL_DYNAPKG_CHARGES =
					BILL_DYNAPKG + " AND dpc.bed_type=? AND dpc.org_id=? " ;

	@SuppressWarnings("unchecked")
	public List<BasicDynaBean> getBillDynaPackageCharges(String orgid,
					String bedType, int dynaPkgId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_BILL_DYNAPKG_CHARGES);
			ps.setString(1, orgid);
			ps.setInt(2, dynaPkgId);
			ps.setString(3, bedType);
			ps.setString(4, orgid);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_A_PACKAGE = "SELECT * FROM dyna_packages ORDER BY dyna_package_id LIMIT 1 ";

	@SuppressWarnings("unchecked")
	public BasicDynaBean getAPackage() throws SQLException {
		List<BasicDynaBean> list = DataBaseUtil.queryToDynaList(GET_A_PACKAGE);
		if (list != null && list.size() > 0)
			return list.get(0);
		return null;
	}
	
	private static final String GET_DYNA_PACK_SUBGROUP_DETAILS = "select dpisg.item_subgroup_id,isg.item_subgroup_name,ig.item_group_id,item_group_name,igt.item_group_type_id,igt.item_group_type_name " +
			" from dyna_package_item_sub_groups dpisg "+
			" left join item_sub_groups isg on (isg.item_subgroup_id = dpisg.item_subgroup_id) "+
			" left join dyna_packages dp on (dp.dyna_package_id = dpisg.dyna_package_id) "+
			" left join item_groups ig on (ig.item_group_id = isg.item_group_id)"+
			" left join item_group_type igt on (igt.item_group_type_id = ig.item_group_type_id)"+
			" where dpisg.dyna_package_id = ? "; 

		public static List<BasicDynaBean> getDynaPackItemSubGroupDetails(int dynaPackId) throws SQLException {
				List list = null;
				Connection con = null;
			    PreparedStatement ps = null;
			 try{
				 con=DataBaseUtil.getReadOnlyConnection();
				 ps=con.prepareStatement(GET_DYNA_PACK_SUBGROUP_DETAILS);
				 ps.setInt(1, dynaPackId);
				 list = DataBaseUtil.queryToDynaList(ps);
			 }finally {
				 DataBaseUtil.closeConnections(con, ps);
			 }
			return list;
		}
		
	public static final String GET_DYNAPKG_ID_CHARGES = GET_DYNAPKG
			+ " AND dpc.bed_type=? AND dpc.org_id=? AND dp.dyna_package_id=? " + " UNION " + BILL_DYNAPKG
			+ " AND dpc.bed_type=? AND dpc.org_id=? " + " UNION " + GET_DYNAPKG
			+ " AND dpc.bed_type='GENERAL' AND dpc.org_id='ORG0001'  AND dp.dyna_package_id=? "
			+ " AND NOT EXISTS (SELECT dyna_package_id FROM dyna_package_charges WHERE dyna_package_id = dp.dyna_package_id "
			+ "                 AND bed_type=? AND org_id=?) ";

	public static List<BasicDynaBean> getDynaPackageIdCharges(String orgid, String bedType, int dynaPkgId)
			throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		List<BasicDynaBean> list = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_DYNAPKG_ID_CHARGES);
			ps.setString(1, orgid);
			ps.setString(2, bedType);
			ps.setString(3, orgid);
			ps.setInt(4, dynaPkgId);
			ps.setString(5, orgid);
			ps.setInt(6, dynaPkgId);
			ps.setString(7, bedType);
			ps.setString(8, orgid);
			ps.setString(9, orgid);
			ps.setInt(10, dynaPkgId);
			ps.setString(11, bedType);
			ps.setString(12, orgid);
			list = DataBaseUtil.queryToDynaList(ps);
		} finally {
			if (ps != null)
				ps.close();
			if (con != null)
				con.close();
		}
		return list;
	}
	
	public static final String GET_DYNA_PACKAGE_CHARGE = "SELECT dpc.charge"
			+ " FROM dyna_package_charges dpc WHERE dpc.dyna_package_id=?"
			+ " AND dpc.org_id=? AND dpc.bed_type=?";

	public static List<String> getDynaPackageCharge(int dynaPackageId, String ratePlanId, String bedType)
			throws SQLException {
		PreparedStatement ps = null;
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_DYNA_PACKAGE_CHARGE);
			ps.setInt(1, dynaPackageId);
			ps.setString(2, ratePlanId);
			ps.setString(3, bedType);
			return DataBaseUtil.queryToOnlyArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
}