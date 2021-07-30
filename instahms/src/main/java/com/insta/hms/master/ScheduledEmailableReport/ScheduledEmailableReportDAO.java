/**
 *
 */
package com.insta.hms.master.ScheduledEmailableReport;

import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.SQLException;
import java.util.List;


/**
 * @author krishna.t
 *
 */
public class ScheduledEmailableReportDAO extends GenericDAO {

	public ScheduledEmailableReportDAO() {
		super("emailable_reports_config");
	}

	public  List getReports() throws SQLException {
		return listAll("doc_id");
	}

	public List<BasicDynaBean> getReportsForTrigger(String trigger) throws SQLException {
		return listAll(null, "trigger_enum", trigger);
	}

}
