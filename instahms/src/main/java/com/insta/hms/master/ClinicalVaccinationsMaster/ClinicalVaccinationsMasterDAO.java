package com.insta.hms.master.ClinicalVaccinationsMaster;

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

public class ClinicalVaccinationsMasterDAO extends GenericDAO{
	public ClinicalVaccinationsMasterDAO() {
		super("clinical_vaccinations_master");
	}

	private static String CLINICAL_VACCINATION_FIELDS = " SELECT *  ";

	private static String CLINICAL_VACCINATION_COUNT = " SELECT count(*) ";

	private static String CLINICAL_VACCINATION_TABLES = " FROM (SELECT * FROM clinical_vaccinations_master "+
			 ") as foo ";

	public PagedList getVaccinationDetails(Map map, Map pagingParams)
		throws Exception, ParseException {
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder qb = new SearchQueryBuilder(con, CLINICAL_VACCINATION_FIELDS,
					CLINICAL_VACCINATION_COUNT, CLINICAL_VACCINATION_TABLES, pagingParams);

			qb.addFilterFromParamMap(map);
			qb.build();

			PagedList l = qb.getMappedPagedList();
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String GET_VACCINATION_DETAILS = "SELECT vaccination_type,vaccination_type_id," +
			" frequency_in_months,status" +
			" FROM clinical_vaccinations_master " +
			" WHERE vaccination_type_id = ?";

	public BasicDynaBean getVaccinationBean(int vaccinationId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_VACCINATION_DETAILS);
			ps.setInt(1, vaccinationId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

}
