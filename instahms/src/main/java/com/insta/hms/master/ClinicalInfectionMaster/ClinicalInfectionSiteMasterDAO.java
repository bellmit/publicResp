package com.insta.hms.master.ClinicalInfectionMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.util.Map;

public class ClinicalInfectionSiteMasterDAO extends GenericDAO{
	public ClinicalInfectionSiteMasterDAO() {
		super("clinical_infection_site_master");
	}

	private static String CLINICAL_INFECTIONS_SITE_FIELDS = " SELECT *  ";

	private static String CLINICAL_INFECTIONS_SITE_COUNT = " SELECT count(*) ";

	private static String CLINICAL_INFECTIONS_SITE_TABLES = " FROM (SELECT * FROM clinical_infection_site_master "+
			 ") as foo ";

	public PagedList getInfectionSiteDetails(Map map, Map pagingParams)
		throws Exception, ParseException {
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder qb = new SearchQueryBuilder(con, CLINICAL_INFECTIONS_SITE_FIELDS,
					CLINICAL_INFECTIONS_SITE_COUNT, CLINICAL_INFECTIONS_SITE_TABLES, pagingParams);

			qb.addFilterFromParamMap(map);
			qb.build();

			PagedList l = qb.getMappedPagedList();
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String GET_INFECTION_SITE_DETAILS = "SELECT *" +
			" FROM clinical_infection_site_master " +
			" WHERE infection_site_id = ?";

	public BasicDynaBean getInfectionSiteBean(int infectionSiteId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_INFECTION_SITE_DETAILS);
			ps.setInt(1, infectionSiteId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
}
