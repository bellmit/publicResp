package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.auditlog.AuditLogDao;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

public class TestAuditLogDAO extends AuditLogDao {

	static Logger logger = LoggerFactory.getLogger(TestAuditLogDAO.class);

	public TestAuditLogDAO(String auditType, String tableName) {
		super(auditType, tableName);
	}

	private static String AUDIT_LOG_QUERY_FIELDS = "SELECT *";
	private static String AUDIT_LOG_QUERY_COUNT = "SELECT COUNT(*)";

	private static String AUDIT_TYPE_LABORATORY = "laboratory";

	public PagedList getAuditLogList(Map filterMap, Map listParams) throws SQLException, ParseException {
		String auditLogTables = null;
		// TODO: This translation should be avoided if possible
		String category = AUDIT_TYPE_LABORATORY.equals(getType()) ? "DEP_LAB" : "DEP_RAD";
		Object prescId[] = (Object[]) filterMap.get("prescribed_id");

		if (!getTable().equalsIgnoreCase("test_visit_reports_audit_log") && !getTable().equalsIgnoreCase("sample_collection_audit_log")) {
			// all tables have a test_id which needs to be joined with other tables to get the category (laboratory / radiology)
			auditLogTables = " FROM " +
			"(SELECT al.*, dd.category FROM " + getTable() + " al"
			+ " join diagnostics d on al.test_id = d.test_id #"
			+ " join diagnostics_departments dd on d.ddept_id = dd.ddept_id ) AS foo";
			
			if (prescId != null && !"".equals(prescId) && prescId[0] != null) {
				auditLogTables = auditLogTables.replace("#", "AND al.prescribed_id='"+prescId[0].toString()+"'");
				filterMap.remove("prescribed_id");
			} else {
				auditLogTables = auditLogTables.replace("#", "");
			}
		} else {
			// in case of patient_visit_reports, category is part of the table itself
			auditLogTables = " FROM " + getTable();
		}

		Connection con = DataBaseUtil.getReadOnlyConnection();

		SearchQueryBuilder sqb = new SearchQueryBuilder(con, AUDIT_LOG_QUERY_FIELDS,
				AUDIT_LOG_QUERY_COUNT, auditLogTables, listParams);
		sqb.addFilterFromParamMap(filterMap);
		if(!getTable().equalsIgnoreCase("sample_collection_audit_log"))
			sqb.addFilter(SearchQueryBuilder.STRING, "category", "=", category);
		// TODO: Need to add a secondary sort on the entity key
		sqb.build();
		PagedList l = sqb.getMappedPagedList();

		sqb.close();
		con.close();
		return l;
	}
}
