package com.insta.hms.master.ClinicalVaccinationNoReasonsMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.util.Map;

/**
 * @author mithun.saha
 *
 */

public class ClinicalVaccinationNoReasonsMasterDAO extends GenericDAO{

	public ClinicalVaccinationNoReasonsMasterDAO() {
		super("clinical_vacc_no_reason");
	}

	private static String CLINICAL_VACCINATION_NO_REASON_FIELDS = " SELECT *  ";

	private static String CLINICAL_VACCINATION_NO_REASON_COUNT = " SELECT count(*) ";

	private static String CLINICAL_VACCINATION_NO_REASON_TABLES = " FROM (SELECT * FROM clinical_vacc_no_reason "+
			 ") as foo ";

	public PagedList getVaccinationNoReasons(Map map, Map pagingParams)
		throws Exception, ParseException {
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder qb = new SearchQueryBuilder(con, CLINICAL_VACCINATION_NO_REASON_FIELDS,
					CLINICAL_VACCINATION_NO_REASON_COUNT, CLINICAL_VACCINATION_NO_REASON_TABLES, pagingParams);

			qb.addFilterFromParamMap(map);
			qb.build();

			PagedList l = qb.getMappedPagedList();
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String GET_VACCINATION_NO_REASON_DETAILS = "SELECT reason_id,reason_name,status" +
			" FROM clinical_vacc_no_reason " +
			" WHERE reason_id = ?";

	public BasicDynaBean getVaccinationNoReasonBean(int reasonId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_VACCINATION_NO_REASON_DETAILS);
			ps.setInt(1, reasonId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

}
