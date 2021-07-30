package com.insta.hms.master.vaccine;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class VaccineMasterDao extends GenericDAO{

	public VaccineMasterDao() {
		super("vaccine_master");
	}

	private static String VACCINE_FIELDS = " SELECT *  ";

	private static String VACCINE_COUNT = " SELECT count(*) ";

	private static String VACCINE_TABLES = " FROM (SELECT vaccine_id, vaccine_name, single_dose, display_order, " +
			"status FROM vaccine_master vm ) AS foo";


	public PagedList getVaccineDetails(Map map, Map pagingParams)
		throws Exception, ParseException {
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder qb = new SearchQueryBuilder(con, VACCINE_FIELDS,
					VACCINE_COUNT, VACCINE_TABLES, pagingParams);

			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("display_order", false);
			qb.build();

			PagedList l = qb.getMappedPagedList();
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String VACCINE_LIST = "SELECT vaccine_name, vaccine_id FROM vaccine_master";

	public static ArrayList getVaccineList()throws SQLException {

		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(VACCINE_LIST);
			return DataBaseUtil.queryToArrayList(pstmt);
		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}

	private static String DOSAGE_LIST = "SELECT vm.vaccine_id, vm.vaccine_name, vm.single_dose, vdm.* FROM vaccine_master vm" +
			"	JOIN vaccine_dose_master vdm USING(vaccine_id)" +
			"	WHERE vm.vaccine_id=? ";

	public static List<Map> getDosageList(int vaccineId, String[] statuses)throws SQLException {

		StringBuilder query = new StringBuilder(DOSAGE_LIST);

		if (statuses == null) {
		  statuses = new String[] {"A"};
		} else if (statuses[0].isEmpty()) {
				statuses = new String[] {"A", "I"};
		}
    String[] placeHolderArr = new String[statuses.length];
    Arrays.fill(placeHolderArr, "?");
    String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
    query.append("AND vdm.status in ( " + placeHolders + ")").append("ORDER BY dose_num");
    int idx = 1;
    try (Connection con = DataBaseUtil.getReadOnlyConnection();
		  PreparedStatement pstmt = con.prepareStatement(query.toString())) {
			pstmt.setInt(idx++, vaccineId);
			for (String status : statuses) {
			  pstmt.setString(idx++, status);
			}
			return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(pstmt));
		}
	}

	private static final String INACTIVATE_VACCINES = "UPDATE vaccine_dose_master SET status='I' WHERE vaccine_id = ?";


	public boolean changeDosageStatus(Connection con, String vaccineID)throws SQLException {

		Integer vaccineIdInt = Integer.parseInt(vaccineID);
		return DataBaseUtil.executeQuery(con, INACTIVATE_VACCINES, vaccineIdInt) > 0;

	}

	private static final String INACTIVATED_LIST = "SELECT dose_num, vaccine_dose_id FROM vaccine_dose_master WHERE vaccine_id = ? AND status='I'";

	public List getInactiveVaccines(int vaccineID)throws SQLException {

		Connection con = null;
		PreparedStatement pstmt = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(INACTIVATED_LIST);
			pstmt.setInt(1, vaccineID);
			return DataBaseUtil.queryToDynaList(pstmt);
		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}

}
