package com.insta.hms.master.Hl7Master;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Hl7ConfigDAO extends GenericDAO{

	public Hl7ConfigDAO(){
		super("hl7_lab_interfaces");
	}

	String INTERFACE_FILED = "SELECT * ";
	String INTERFACE_COUNT = "SELECT count(*) ";
	//String INTERFACE_TABLES = "FROM (SELECT interface_name, sending_app, sending_facility, status, results_import_dir, export_type, set_completed_status, equipment_code_required, conducting_doctor_mandatory, append_doctor_signature, ack_type, consolidate_multiple_obx from hl7_lab_interfaces) as foo";
	String INTERFACE_TABLES = "FROM (SELECT * from hl7_lab_interfaces) as foo";
	
	@SuppressWarnings("rawtypes")
	public PagedList getInterfaceDetails(Map map,Map<LISTING, Object> pagingParams) 
			throws ParseException, SQLException {

		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder qb = new SearchQueryBuilder(con, INTERFACE_FILED,
					INTERFACE_COUNT, INTERFACE_TABLES, pagingParams);

			qb.addFilterFromParamMap(map);
			qb.build();

			PagedList l = qb.getMappedPagedList();
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}

	}
	
	public static final String GET_ALL_INTERFACE = "SELECT interface_type,interface_name FROM hl7_lab_interfaces " ;

	@SuppressWarnings("rawtypes")
	public static List getAvalInterfaces() {
		PreparedStatement ps = null;
		ArrayList cityNames = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ALL_INTERFACE);
			cityNames = DataBaseUtil.queryToArrayList(ps);
		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return cityNames;
	}
	
	public static final String GET_INTERFACE_CENTER_DETAILS = "SELECT hci.*, hcm.center_name "
			+ " FROM hl7_center_interfaces hci "
			+ " JOIN hospital_center_master hcm ON (hcm.center_id = hci.center_id) "
			+ " WHERE hci.hl7_lab_interface_id = ? " ;

	@SuppressWarnings("rawtypes")
	public static List<BasicDynaBean> getInterfaceCenterDetails(String hl7_lab_interface_id_str) {
		PreparedStatement ps = null;
		List list = null;
		Connection con = null;
		Integer hl7_lab_interface_id = (null != hl7_lab_interface_id_str && !hl7_lab_interface_id_str.equals("")) 
				? Integer.parseInt(hl7_lab_interface_id_str) : null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_INTERFACE_CENTER_DETAILS);
			ps.setInt(1, hl7_lab_interface_id);
			list = DataBaseUtil.queryToDynaList(ps);
		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return list;
	}
	
}
