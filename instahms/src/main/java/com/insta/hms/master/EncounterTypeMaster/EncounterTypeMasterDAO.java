package com.insta.hms.master.EncounterTypeMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.diagnosticsmasters.addtest.AddTestAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class EncounterTypeMasterDAO extends GenericDAO{
	public EncounterTypeMasterDAO() {
		super("encounter_type_codes");

	}

	static Logger logger = LoggerFactory.getLogger(AddTestAction.class);

	private static final String ALL_ENCOUNTER_TYPE_FIELDS = " SELECT * ";

	private static final String ALL_ENCOUNTER_TYPE_COUNT = " SELECT COUNT(encounter_type_codes) ";

	private static final String ALL_ENCOUNTER_TYPE_TABLES = " FROM  encounter_type_codes ";

	public PagedList getEncounterTypeList(Map map ,Map paginParams) throws SQLException,ParseException {
		Connection con = null;
		SearchQueryBuilder qb = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			qb = new SearchQueryBuilder(con, ALL_ENCOUNTER_TYPE_FIELDS, ALL_ENCOUNTER_TYPE_COUNT, ALL_ENCOUNTER_TYPE_TABLES,paginParams);
			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("encounter_type_id");
			qb.build();
			return qb.getMappedPagedList();
		}
		finally {
			qb.close();
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String ENCOUNTER_TYPE_DETAILS = "Select * from encounter_type_codes";

	public  List getEncounterTypeDetails() throws SQLException {
		return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(ENCOUNTER_TYPE_DETAILS));
	}

	public int updateUndefaultValues(Connection con , String type, int key, String value) throws SQLException {

		String UPDATE_DEFAULT_VALUES = "UPDATE encounter_type_codes SET @  WHERE encounter_type_id != ? ";

		if (type.equals("o")) {
			UPDATE_DEFAULT_VALUES = UPDATE_DEFAULT_VALUES.replace("@", " op_encounter_default=? ");
		}else if (type.equals("i")) {
			UPDATE_DEFAULT_VALUES = UPDATE_DEFAULT_VALUES.replace("@", " ip_encounter_default=? ");
		}else if (type.equals("d")) {
			UPDATE_DEFAULT_VALUES = UPDATE_DEFAULT_VALUES.replace("@", " daycare_encounter_default=? ");
		}

		PreparedStatement ps = con.prepareStatement(UPDATE_DEFAULT_VALUES);
		ps.setString(1, value);
		ps.setInt(2, key);
		int i = ps.executeUpdate();
		if (ps != null) ps.close();
		return i;
	}
}
