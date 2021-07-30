package com.insta.hms.master.CategoryBedTypeMarkup;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CategoryBedTypeMarkupDAO  extends GenericDAO{

	public CategoryBedTypeMarkupDAO() {
		super("category_bed_markups");
	}
	private static String CATEGORY_MARKUP_FIELDS = " SELECT *";
	private static String CATEGORY_MARKUP_COUNT = " SELECT count(*) ";
	private static String CATEGORY_MARKUP_TABLES = " FROM (select cbm.*,cm.category from category_bed_markups cbm join store_category_master cm using(category_id)) as foo  ";

	public static PagedList list(Map filter, Map listing) throws Exception {

		Connection con = DataBaseUtil.getReadOnlyConnection();

		SearchQueryBuilder qb = new SearchQueryBuilder(con,
				CATEGORY_MARKUP_FIELDS, CATEGORY_MARKUP_COUNT, CATEGORY_MARKUP_TABLES, listing);

		qb.addFilterFromParamMap(filter);
		qb.addSecondarySort("category_id");
		qb.build();

		PagedList l = qb.getMappedPagedList();

		qb.close();
		con.close();

		return l;
	}

	private static final String GET_CAT_TYPES = "SELECT category_id, category FROM store_category_master  where billable='t'";

	public static ArrayList getCats() throws SQLException {
		Connection con = null; PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_CAT_TYPES);
			return DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_BED_DETAILS = "SELECT * FROM category_bed_markups where category_id=? and bed_type=? ";

	public BasicDynaBean findByKeyColumn(String cat, String bed) throws SQLException {

	PreparedStatement ps = null;Connection con = null;
	try {
		con = DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(GET_BED_DETAILS);
		ps.setInt(1, Integer.parseInt(cat));
		ps.setString(2, bed);
		List list = DataBaseUtil.queryToDynaList(ps);
		if (list.size() > 0) {
			return (BasicDynaBean) list.get(0);
		} else {
			return null;
		}
	} finally {
		DataBaseUtil.closeConnections(con, ps);
	}
	}

	private static final String DELETE_BED_CAT_DETAILS = "DELETE FROM category_bed_markups where category_id=? and bed_type=? ";

	public static boolean deleteMarkupDetails (Connection con,String catid,String bedtype) throws SQLException{
		PreparedStatement ps = null;boolean status = false;int count = 0;
		try {
			ps = con.prepareStatement(DELETE_BED_CAT_DETAILS);
			ps.setInt(1, Integer.parseInt(catid));
			ps.setString(2, bedtype);
			count = ps.executeUpdate();
			if (count > 0) status = true;
			}catch (Exception e){
				status = false;
			}
			return status;
	}

}
