package com.insta.hms.master.AllergyMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class AllergyMasterDAO extends GenericDAO {

	public AllergyMasterDAO(){
		super("allergy_master");
	}

	private static final String ALLERGY_FIELDS = "SELECT * ";
	private static final String ALLERGY_TABLES = "FROM allergy_master";
	private static final String ALLERGY_COUNT = "SELECT count(allergy_id)";

	public PagedList getAllergyDeatils(Map filter, Map listing)throws SQLException,
		ParseException {
		Connection con = DataBaseUtil.getConnection();
		SearchQueryBuilder qb = null;
		try {
			qb = new SearchQueryBuilder(con,ALLERGY_FIELDS,ALLERGY_COUNT,ALLERGY_TABLES,listing );
			qb.addFilterFromParamMap(filter);
			qb.build();

			return qb.getMappedPagedList();
		}finally {
			DataBaseUtil.closeConnections(con, null);
			if(qb!=null) qb.close();
		}

	}

	private static final String ALLERGY_NAMESAND_iDS="select allergy_name,allergy_id from allergy_master";

	public static List getAllergyNamesAndIds() throws SQLException{
		return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(ALLERGY_NAMESAND_iDS));
	}

	public boolean exists(int allergyId, String allergyName) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT allergy_name FROM allergy_master " +
					"	where allergy_id!=? and upper(trim(allergy_name))=upper(trim(?))");
			ps.setInt(1, allergyId);
			ps.setString(2, allergyName);
			rs = ps.executeQuery();
			if (rs.next()) return true;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return false;
	}

}