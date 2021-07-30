package com.insta.hms.master.ClinicalData;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.util.List;
import java.util.Map;


public class ClinicalDataLabResultsMasterDAO extends GenericDAO{
	public ClinicalDataLabResultsMasterDAO() {
		super("clinical_lab_result");
	}
	static Logger logger = LoggerFactory.getLogger(ClinicalDataLabResultsMasterDAO.class);

	private static String CLINICAL_LAB_FIELDS = " SELECT *  ";

	private static String CLINICAL_LAB_COUNT = " SELECT count(*) ";

	private static String CLINICAL_LAB_TABLES = " FROM (SELECT trm.resultlabel_id,trm.resultlabel,trm.units,clr.status," +
			" trm.resultlabel||'('||d.test_name||')' AS result_test_name, clr.display_order " +
			" FROM test_results_master trm " +
			" JOIN diagnostics d USING(test_id)" +
			" JOIN clinical_lab_result clr USING (resultlabel_id) ) as foo ";

	public PagedList getClinicalLabDetails(Map map, Map pagingParams)
		throws Exception, ParseException {
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder qb = new SearchQueryBuilder(con, CLINICAL_LAB_FIELDS,
					CLINICAL_LAB_COUNT, CLINICAL_LAB_TABLES, pagingParams);

			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("display_order");
			qb.addSecondarySort("resultlabel");
			qb.build();

			PagedList l = qb.getMappedPagedList();
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String GET_CLINICAL_DETAILS = "SELECT trm.resultlabel,trm.resultlabel_id,trm.units,clr.status, " +
			" CASE WHEN resultlabel_short != null OR resultlabel_short != '' THEN resultlabel_short||'/'||trm.resultlabel||'('||d.test_name||')'" +
			" ELSE trm.resultlabel||'('||d.test_name||')' END AS result_test_name, clr.display_order" +
			" FROM test_results_master trm " +
			" JOIN clinical_lab_result clr USING(resultlabel_id)" +
			" JOIN diagnostics d USING(test_id)"+
			" WHERE resultlabel_id = ?";

	public BasicDynaBean getClinicalBean(int resultlabelId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_CLINICAL_DETAILS);
			ps.setInt(1, resultlabelId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_CLINICAL_AND_TEST_DETAILS = "SELECT trm.resultlabel,trm.resultlabel_id,trm.units,d.test_name," +
			" trm.resultlabel||'('||d.test_name||')' AS result_test_name,resultlabel_short AS result_short_name " +
			" FROM test_results_master trm " +
			" JOIN diagnostics d USING(test_id) ";

	public List getClinicalAndTestDetails() throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_CLINICAL_AND_TEST_DETAILS);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_RESULT_DETAILS = "SELECT trm.resultlabel,trm.resultlabel_id,trm.units,d.test_name," +
			" CASE WHEN resultlabel_short != null OR resultlabel_short != '' THEN resultlabel_short||'/'||trm.resultlabel||'('||d.test_name||')'" +
			" ELSE trm.resultlabel||'('||d.test_name||')' END AS result_test_name" +
			" FROM test_results_master trm " +
			" JOIN diagnostics d USING(test_id)";
	public List getTestResultDetails() throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_RESULT_DETAILS);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

}
