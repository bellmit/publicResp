/**
 *
 */
package com.insta.hms.master.DynaPackageRules;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.AbstractCachingDAO;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.QueryBuilder;
import com.insta.hms.common.SearchQueryBuilder;
import net.sf.ehcache.Cache;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lakshmi
 *
 */
public class DynaPackageRulesMasterDAO extends AbstractCachingDAO {

	static Logger logger = LoggerFactory.getLogger(DynaPackageRulesMasterDAO.class);

	public DynaPackageRulesMasterDAO() {
		super("dyna_package_rules");
	}

	public static final String DYNA_PKG_RULES_FIELDS = " SELECT * ";
	public static final String DYNA_PKG_RULES_COUNT  = " SELECT count(*)";
	public static final String DYNA_PKG_RULES_TABLES = " FROM  ("+
		" SELECT dpr.pkg_rule_id, dpr.dyna_pkg_cat_id," +
		" dpc.dyna_pkg_cat_name, dpr.center_id, hcm.center_name, " +
		" dpr.chargegroup_id, dpr.chargehead_id, dpr.activity_type, " +
		" scm.category as item_type, dpr.activity_id, "+
		" dpr.priority, cgc.chargegroup_name, chc.chargehead_name, " +
		" dpr.service_group_id, dpr.service_sub_group_id, "+
		" sg.service_group_name, ssg.service_sub_group_name,  "+
		" COALESCE(d.test_name, s.service_name, om.operation_name," +
		"	 dc.doctor_name, dt.meal_name, sid.medicine_name, " +
		"	 nbt.bed_type_name, ibt.bed_type_name, em.equipment_name, " +
		"	 pm.package_name, ccm.charge_name) AS activity_name "+
		" FROM dyna_package_rules dpr " +
		" LEFT JOIN dyna_package_category dpc ON(dpr.dyna_pkg_cat_id::text = dpc.dyna_pkg_cat_id::text) "+
		" LEFT OUTER JOIN chargegroup_constants cgc ON(dpr.chargegroup_id = cgc.chargegroup_id) "+
		" LEFT OUTER JOIN chargehead_constants chc ON(dpr.chargehead_id = chc.chargehead_id) "+
		" LEFT OUTER JOIN service_groups sg ON(dpr.service_group_id::text = sg.service_group_id::text) "+
		" LEFT OUTER JOIN service_sub_groups ssg ON(dpr.service_sub_group_id::text = ssg.service_sub_group_id::text) "+
		" LEFT OUTER JOIN diagnostics d ON(dpr.activity_id = d.test_id) " +
		" LEFT OUTER JOIN services s ON(dpr.activity_id = s.service_id) " +
		" LEFT OUTER JOIN operation_master om on (dpr.activity_id = om.op_id) "+
		" LEFT OUTER JOIN doctors dc on (dpr.activity_id = dc.doctor_id) "+
		" LEFT OUTER JOIN diet_master dt on (dpr.activity_id = dt.diet_id::text) "+
		" LEFT OUTER JOIN equipment_master em on (dpr.activity_id = em.eq_id::text) "+
		" LEFT OUTER JOIN packages pm on (dpr.activity_id = pm.package_id::text) "+
		" LEFT OUTER JOIN common_charges_master ccm on (dpr.activity_id = ccm.charge_name::text) "+
		" LEFT OUTER JOIN bed_types nbt on (dpr.activity_id = nbt.bed_type_name::text AND nbt.is_icu  = 'N') "+
		" LEFT OUTER JOIN bed_types ibt on (dpr.activity_id = ibt.bed_type_name::text AND ibt.is_icu  = 'Y') "+
		" LEFT OUTER JOIN store_item_details sid on (dpr.activity_id = sid.medicine_id::text) "+
		" LEFT OUTER JOIN store_category_master scm on (dpr.activity_type = scm.category_id::text) "+
		" LEFT OUTER JOIN hospital_center_master hcm ON(dpr.center_id = hcm.center_id::text)) as rules";

	public PagedList getDynaPkgRules(Map filters, Map<LISTING, Object> listing)throws SQLException, ParseException {

		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			SearchQueryBuilder qb = new SearchQueryBuilder(con, DYNA_PKG_RULES_FIELDS,
					DYNA_PKG_RULES_COUNT, DYNA_PKG_RULES_TABLES, listing);

			qb.addFilterFromParamMap(filters);
			qb.addSecondarySort("pkg_rule_id");
			qb.build();
			PagedList l= qb.getMappedPagedList();
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String CHARGE_GROUP_HEAD_DETAILS =
		" SELECT ssg.service_sub_group_id, ssg.service_sub_group_name, " +
		" sg.service_group_id, sg.service_group_name," +
		" cgc.chargegroup_id, cgc.chargegroup_name, " +
		" chc.chargehead_id, chc.chargehead_name " +
		" 	FROM service_sub_groups ssg " +
		" LEFT OUTER JOIN service_groups sg ON (sg.service_group_id = ssg.service_group_id) " +
		" LEFT OUTER JOIN chargehead_constants chc ON (chc.service_sub_group_id = ssg.service_sub_group_id) " +
		" LEFT OUTER JOIN chargegroup_constants cgc ON (cgc.chargegroup_id = chc.chargegroup_id) ";

	public List<BasicDynaBean> getChargeGroupHeadDetails() throws SQLException {
		return DataBaseUtil.queryToDynaList(CHARGE_GROUP_HEAD_DETAILS);
	}

	private static final String STORE_ITEM_CATEGORY_DETAILS =
			" SELECT category,category_id::text FROM store_category_master WHERE status = 'A' ORDER BY category ";

	public List<BasicDynaBean> getStoreItemDetails() throws SQLException {
		return DataBaseUtil.queryToDynaList(STORE_ITEM_CATEGORY_DETAILS);
	}

	private static final String DUPLICATE_RULE =
		"SELECT * FROM dyna_package_rules " +
		" WHERE chargegroup_id=? AND chargehead_id=? AND service_group_id=? AND service_sub_group_id=? " +
		"  AND dyna_pkg_cat_id = ? AND activity_type=? AND activity_id=? ";

	public BasicDynaBean isDuplicateRule(BasicDynaBean bean) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(DUPLICATE_RULE);
			int i=1;
			ps.setString(i++, (String)bean.get("chargegroup_id"));
			ps.setString(i++, (String)bean.get("chargehead_id"));
			ps.setString(i++, (String)bean.get("service_group_id"));
			ps.setString(i++, (String)bean.get("service_sub_group_id"));
			ps.setInt(i++, (Integer)bean.get("dyna_pkg_cat_id"));
			ps.setString(i++, (String)bean.get("activity_type"));
			ps.setString(i++, (String)bean.get("activity_id"));

			List<BasicDynaBean> l = DataBaseUtil.queryToDynaList(ps);
			if (l != null && l.size() > 0)
				return l.get(0);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return null;
	}

	private BasicDynaBean getActivityBean() {
		DynaBeanBuilder builder = new DynaBeanBuilder();
		builder.add("activity_id");
		builder.add("activity_name");
		return builder.build();
	}

	private static final String ALL_LAB_TESTS =
		" SELECT * FROM " +
		"	(SELECT test_id::text as activity_id, test_name as activity_name," +
		"		service_sub_group_id::text, service_group_id::text " +
		"	FROM diagnostics " +
		" JOIN diagnostics_departments dd using(ddept_id) " +
		" JOIN service_sub_groups USING (service_sub_group_id) " +
		" JOIN service_groups USING (service_group_id) " +
		" WHERE dd.category='DEP_LAB' ) AS foo " ;

	private static final String ALL_RAD_TESTS =
		" SELECT * FROM " +
		"	(SELECT test_id::text as activity_id, test_name as activity_name," +
		"		service_sub_group_id::text, service_group_id::text " +
		"	FROM diagnostics " +
		" JOIN diagnostics_departments dd using(ddept_id) " +
		" JOIN service_sub_groups USING (service_sub_group_id) " +
		" JOIN service_groups USING (service_group_id) " +
		" WHERE dd.category='DEP_RAD' ) AS foo " ;

	private static final String ALL_SERVICES =
		" SELECT * FROM " +
		"	(SELECT service_id::text as activity_id, service_name as activity_name," +
		"		service_sub_group_id::text, service_group_id::text " +
		"	FROM services " +
		" JOIN service_sub_groups USING (service_sub_group_id) " +
		" JOIN service_groups USING (service_group_id)) AS foo " ;

	private static final String ALL_DOCTORS =
		" SELECT * FROM " +
		"	(SELECT doctor_id::text as activity_id, doctor_name as activity_name," +
		"		service_sub_group_id::text, service_group_id::text " +
		"	FROM doctors " +
		" JOIN service_sub_groups USING (service_sub_group_id) " +
		" JOIN service_groups USING (service_group_id)) AS foo " ;

	private static final String ALL_OPERATIONS =
		" SELECT * FROM " +
		"	(SELECT op_id::text as activity_id, operation_name as activity_name," +
		"		service_sub_group_id::text, service_group_id::text " +
		"	FROM operation_master " +
		" JOIN service_sub_groups USING (service_sub_group_id) " +
		" JOIN service_groups USING (service_group_id)) AS foo " ;

	private static final String ALL_EQUIPMENTS =
		" SELECT * FROM " +
		"	(SELECT eq_id::text as activity_id, equipment_name as activity_name," +
		"		service_sub_group_id::text, service_group_id::text " +
		"	FROM equipment_master " +
		" JOIN service_sub_groups USING (service_sub_group_id) " +
		" JOIN service_groups USING (service_group_id)) AS foo " ;

	private static final String ALL_PACKAGES =
		" SELECT * FROM " +
		"	(SELECT package_id::text as activity_id, package_name as activity_name," +
		"		service_sub_group_id::text, service_group_id::text " +
		"	FROM packages " +
		" JOIN service_sub_groups USING (service_sub_group_id) " +
		" JOIN service_groups USING (service_group_id)) AS foo " ;

	private static final String ALL_OTHER_CHARGES =
		" SELECT * FROM " +
		"	(SELECT charge_name::text as activity_id, charge_name as activity_name," +
		"		service_sub_group_id::text, service_group_id::text " +
		"	FROM common_charges_master " +
		" JOIN service_sub_groups USING (service_sub_group_id) " +
		" JOIN service_groups USING (service_group_id)) AS foo " ;

	private static final String ALL_DIET =
		" SELECT * FROM " +
		"	(SELECT diet_id::text as activity_id, meal_name as activity_name," +
		"		service_sub_group_id::text, service_group_id::text " +
		"	FROM diet_master " +
		" JOIN service_sub_groups USING (service_sub_group_id) " +
		" JOIN service_groups USING (service_group_id)) AS foo " ;

	private static final String ALL_STORE_ITEMS =
		" SELECT * FROM " +
		"	(SELECT medicine_id::text as activity_id, medicine_name as activity_name, med_category_id, " +
		"		service_sub_group_id::text, service_group_id::text " +
		"	FROM store_item_details " +
		" JOIN service_sub_groups USING (service_sub_group_id) " +
		" JOIN service_groups USING (service_group_id)) AS foo " ;

	private static final String ALL_NORMAL_BEDTYPES =
		" SELECT * FROM " +
		"	(SELECT bed_type_name::text as activity_id, bed_type_name as activity_name," +
		"	  service_sub_group_id::text, service_group_id::text " +
		"  FROM bed_types bt" +
		" JOIN chargehead_constants cc ON (cc.chargehead_id = 'BBED')" +
		" JOIN service_sub_groups USING (service_sub_group_id) " +
		" JOIN service_groups USING (service_group_id)" +
		" WHERE is_icu  = 'N') AS foo";

	private static final String ALL_ICU_BEDTYPES =
		" SELECT * FROM " +
		"	(SELECT bed_type_name::text as activity_id, bed_type_name as activity_name," +
		"	  service_sub_group_id::text, service_group_id::text " +
		"  FROM bed_types bt" +
		" JOIN chargehead_constants cc ON (cc.chargehead_id = 'BICU')" +
		" JOIN service_sub_groups USING (service_sub_group_id) " +
		" JOIN service_groups USING (service_group_id)" +
		" WHERE is_icu  = 'Y') AS foo";

	@SuppressWarnings("unchecked")
	public List<BasicDynaBean> getActivityList(String searchStr,
			String activityType, String activityId) throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DataBaseUtil.getConnection();

			if (activityType != null && !activityType.equals("*")) {

				boolean medicines = false;
				boolean append = false;
				int i = 1;

				StringBuilder query = new StringBuilder();

				if (activityType.equals("_ALL_LABTESTS"))
					query.append(ALL_LAB_TESTS);

				else if (activityType.equals("_ALL_RADTESTS"))
					query.append(ALL_RAD_TESTS);

				else if (activityType.equals("_ALL_DOCTORS"))
					query.append(ALL_DOCTORS);

				else if (activityType.equals("_ALL_SERVICES"))
					query.append(ALL_SERVICES);

				else if (activityType.equals("_ALL_OPERATIONS"))
					query.append(ALL_OPERATIONS);

				else if (activityType.equals("_ALL_DIET"))
					query.append(ALL_DIET);

				else if (activityType.equals("_ALL_EQUIPMENTS"))
					query.append(ALL_EQUIPMENTS);

				else if (activityType.equals("_ALL_PACKAGES"))
					query.append(ALL_PACKAGES);

				else if (activityType.equals("_ALL_OTHER_CHARGES"))
					query.append(ALL_OTHER_CHARGES);

				else if (activityType.equals("_ALL_NORMAL_BEDTYPES"))
					query.append(ALL_NORMAL_BEDTYPES);

				else if (activityType.equals("_ALL_ICU_BEDTYPES"))
					query.append(ALL_ICU_BEDTYPES);

				else {
					medicines = true;
					query.append(ALL_STORE_ITEMS);
					append = QueryBuilder.addWhereFieldOpValue(append, query, "med_category_id", "=", activityType);
				}

				if (searchStr != null && !searchStr.trim().equals(""))
					append = QueryBuilder.addWhereFieldOpValue(append, query, "activity_name", "ilike", searchStr);

				if (activityId != null && !activityId.trim().equals(""))
					append = QueryBuilder.addWhereFieldOpValue(append, query, "activity_id", "=", activityId);

				ps = con.prepareStatement(query.toString());

				if (medicines) { // For store medicines append item category if selected.
					ps.setInt(i++, Integer.parseInt(activityType));
				}

				if (searchStr != null && !searchStr.trim().equals(""))
					ps.setString(i++, "%" + searchStr + "%");

				if (activityId != null && !activityId.trim().equals(""))
					ps.setString(i++, activityId);

				return DataBaseUtil.queryToDynaList(ps);
			}
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}

		List<BasicDynaBean> list = new ArrayList<BasicDynaBean>();
		BasicDynaBean activityBean = getActivityBean();
		activityBean.set("activity_id", "*");
		activityBean.set("activity_name", "(All)");
		list.add(activityBean);
		return list;
	}

	private static final String GET_ALL_RULES = " SELECT * FROM dyna_package_rules ORDER BY priority ";

	@SuppressWarnings("unchecked")
	public boolean updatePriorityValues() throws SQLException, IOException {
		boolean status = true;

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		List<BasicDynaBean> list = DataBaseUtil.queryToDynaList(GET_ALL_RULES);
		int priority = 10;
		for (Object object : list) {
			BasicDynaBean bean = (BasicDynaBean)object;
			bean.set("priority", priority);
			priority +=10;
		}

		try {
			deleteAll(con);
			insertAll(con, list);
			con.commit();
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}

		return status;
	}

	@Override
	protected Cache newCache(String region) {
		return new Cache(region, 500, MemoryStoreEvictionPolicy.LRU, false, "/tmp", true, 0, 0, false, 0,
				null);
	}
}
