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

public class ClinicalInfectionMasterDAO extends GenericDAO{
	public ClinicalInfectionMasterDAO() {
		super("clinical_infections_master");
	}

	private static String CLINICAL_INFECTIONS_FIELDS = " SELECT *  ";

	private static String CLINICAL_INFECTIONS_COUNT = " SELECT count(*) ";

	private static String CLINICAL_INFECTIONS_TABLES = " FROM (SELECT * FROM clinical_infections_master "+
			 ") as foo ";

	public PagedList getInfectionsDetails(Map map, Map pagingParams)
		throws Exception, ParseException {
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder qb = new SearchQueryBuilder(con, CLINICAL_INFECTIONS_FIELDS,
					CLINICAL_INFECTIONS_COUNT, CLINICAL_INFECTIONS_TABLES, pagingParams);

			qb.addFilterFromParamMap(map);
			qb.build();

			PagedList l = qb.getMappedPagedList();
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String GET_INFECTION_DETAILS = "SELECT infection_type,infection_type_id,status" +
			" FROM clinical_infections_master " +
			" WHERE infection_type_id = ?";

	public BasicDynaBean getInfectionBean(int infectionId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_INFECTION_DETAILS);
			ps.setInt(1, infectionId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
}
