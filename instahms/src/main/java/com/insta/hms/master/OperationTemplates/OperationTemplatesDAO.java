/**
 *
 */
package com.insta.hms.master.OperationTemplates;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

/**
 * @author krishna
 *
 */
public class OperationTemplatesDAO extends GenericDAO {

	public OperationTemplatesDAO() {
		super("operation_doc_templates_master");
	}

	private static final String FIELDS = "SELECT odtm.op_id, odtm.template_id, odtm.format, dtv.template_name, om.operation_name  ";
	private static final String TABLES = " FROM operation_doc_templates_master odtm " +
			"	JOIN doc_all_templates_view dtv ON (dtv.doc_format=odtm.format and dtv.template_id=odtm.template_id)" +
			"	JOIN operation_master om ON (odtm.op_id=om.op_id) ";
	private static final String COUNT = "SELECT count(*) ";

	public PagedList search(Map filters) throws SQLException, ParseException {
		Connection con = DataBaseUtil.getConnection();
		SearchQueryBuilder qb = null;
		try {
			qb = new SearchQueryBuilder(con, FIELDS, COUNT, TABLES, ConversionUtils.getListingParameter(filters));
			String[] templateIdAndFormat = (String[]) filters.get("_ot_template");
			if (templateIdAndFormat != null && templateIdAndFormat[0] != null && !templateIdAndFormat[0].equals("")) {
				qb.addFilter(SearchQueryBuilder.INTEGER, "odtm.template_id", "=", Integer.parseInt(templateIdAndFormat[0].split(",")[0]));
				qb.addFilter(SearchQueryBuilder.STRING, "odtm.format", "=", templateIdAndFormat[0].split(",")[1]);
			}
			qb.addFilterFromParamMap(filters);
			qb.build();

			return qb.getMappedPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
		}
	}

	public BasicDynaBean getRecord(String opId, int templateId, String format) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(FIELDS + TABLES + " WHERE odtm.op_id=? and odtm.template_id=? and odtm.format=?");
			ps.setString(1, opId);
			ps.setInt(2, templateId);
			ps.setString(3, format);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

}
