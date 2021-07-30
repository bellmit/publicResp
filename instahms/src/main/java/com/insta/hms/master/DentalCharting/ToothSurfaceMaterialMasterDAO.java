package com.insta.hms.master.DentalCharting;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

public class ToothSurfaceMaterialMasterDAO extends GenericDAO {

	public ToothSurfaceMaterialMasterDAO() {
		super("tooth_surface_material_master");
	}

	private static final String TOOTH_SURFACE_MATERIAL_FIELDS = "SELECT *";
	private static final String TOOTH_SURFACE_MATERIAL_COUNT = "SELECT count(material_id)";
	private static final String TOOTH_SURFACE_MATERIAL_TABLE = "FROM tooth_surface_material_master";

	public PagedList getMaterialList(Map filter, Map listing)throws SQLException,ParseException {
		Connection con = DataBaseUtil.getReadOnlyConnection();
		SearchQueryBuilder qb = null;
		try {
			qb = new SearchQueryBuilder(con,TOOTH_SURFACE_MATERIAL_FIELDS, TOOTH_SURFACE_MATERIAL_COUNT,
						TOOTH_SURFACE_MATERIAL_TABLE,listing);
			qb.addFilterFromParamMap(filter);
			qb.build();
			return qb.getMappedPagedList();
		}finally {
			DataBaseUtil.closeConnections(con, null);
			if(qb!=null) qb.close();
		}
	}

	public Boolean exists(String materialName, int materailID) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement("SELECT material_name FROM tooth_surface_material_master " +
					"	where material_id!=? and upper(trim(material_name))=upper(trim(?))");
			ps.setInt(1, materailID);
			ps.setString(2, materialName);
			rs = ps.executeQuery();
			if(rs.next()) return true;
		}finally {
			DataBaseUtil.closeConnections(con, ps);
			rs.close();
		}
		return false;
	}
}