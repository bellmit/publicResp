package com.insta.hms.master.DiagTemplate;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class DiagTemplateDAO extends GenericDAO {

	public DiagTemplateDAO() {
		super("test_format");
	}

	public String getNextId() throws SQLException {
		return AutoIncrementId.getNewIncrUniqueId("TESTFORMAT_ID","TEST_FORMAT","TEXTFORMAT");
	}

	/*
	 * The bean is used only for insert/updates, as a substitute for DTOs.
	 */
	public BasicDynaBean getBean() {
		DynaBeanBuilder bldr = new DynaBeanBuilder();
		bldr.add("format_name")
			.add("testformat_id")
			.add("format_description")
			.add("report_file");
		return bldr.build();
	}

	/*
	 * To search, returns all fields except the template contents as a MapList inside a PagedList.
	 * Note: the format contents are not part of the regular listing. For every format, we also
	 * get the first test that uses this template, by name.
	 */
	private static final String SELECT_FIELD = "SELECT format_name,testformat_id,format_description," +
			"used_in_test_name,used_in_test_id,dept_name ";

	private static final String SELECT_COUNT = "SELECT count(*)";
	private static final String FROM_TABLE = "FROM template_searching_view";

	public PagedList list(Map requestParams, Map<LISTING, Object> pagingParams)
		throws SQLException, ParseException {

		Connection con = DataBaseUtil.getReadOnlyConnection();
		SearchQueryBuilder qb = new SearchQueryBuilder(con, SELECT_FIELD,
				SELECT_COUNT, FROM_TABLE, null, pagingParams);
		try {
			qb.addFilterFromParamMap(requestParams);
			qb.addSecondarySort("testformat_id");
			qb.build();
			return qb.getMappedPagedList();
		} finally {
			qb.close();
			DataBaseUtil.closeConnections(con, null);
		}
	}


  private static final String GET_TEST_NAMES="SELECT used_in_test_id, used_in_test_name FROM template_searching_view";

  public static List getTestsNames() throws SQLException{
		 return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(GET_TEST_NAMES));
	 }

  private static final String GET_DIAG_DEPARTMENT_NAMES="SELECT ddept_id,ddept_name FROM diagnostics_departments";

  public static List getDiagDepartmentNames() throws SQLException{
		 return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(GET_DIAG_DEPARTMENT_NAMES));
	 }
}
