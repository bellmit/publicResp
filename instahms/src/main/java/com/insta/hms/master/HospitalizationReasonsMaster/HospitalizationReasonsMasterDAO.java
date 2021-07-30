package com.insta.hms.master.HospitalizationReasonsMaster;

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

/**
 * @author mithun.saha
 *
 */

public class HospitalizationReasonsMasterDAO extends GenericDAO {
		static Logger logger = LoggerFactory.getLogger(HospitalizationReasonsMasterDAO.class);
		public HospitalizationReasonsMasterDAO() {
			super("clinical_hospitalization_reasons");
		}
		private static String CLINICAL_HOSPITALIZATION_REASONS_FIELDS = " SELECT *  ";

		private static String CLINICAL_HOSPITALIZATION_REASONS_COUNT = " SELECT count(*) ";

		private static String CLINICAL_HOSPITALIZATION_REASONS_TABLES = " FROM (SELECT * FROM clinical_hospitalization_reasons "+
				 ") as foo ";

		public PagedList getHospitalizationReasons(Map map, Map pagingParams)
			throws Exception, ParseException {
			Connection con = null;
			try {
				con = DataBaseUtil.getReadOnlyConnection();

				SearchQueryBuilder qb = new SearchQueryBuilder(con, CLINICAL_HOSPITALIZATION_REASONS_FIELDS,
						CLINICAL_HOSPITALIZATION_REASONS_COUNT, CLINICAL_HOSPITALIZATION_REASONS_TABLES, pagingParams);

				qb.addFilterFromParamMap(map);
				qb.addSecondarySort("reason", true);
				qb.build();

				PagedList l = qb.getMappedPagedList();
				return l;
			} finally {
				DataBaseUtil.closeConnections(con, null);
			}
		}

		private static final String GET_REASON_DETAILS = "SELECT reason,reason_id,status" +
				" FROM clinical_hospitalization_reasons " +
				" WHERE reason_id = ?";

		public BasicDynaBean getReasonBean(int reasonId) throws Exception{
			Connection con = null;
			PreparedStatement ps = null;
			try {
				con = DataBaseUtil.getConnection();
				ps = con.prepareStatement(GET_REASON_DETAILS);
				ps.setInt(1, reasonId);
				return DataBaseUtil.queryToDynaBean(ps);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
		}

		private static final String GET_REASON_IDS = "SELECT reason_id from clinical_hospitalization_reasons";

		public List<BasicDynaBean> getReasonIds() throws Exception{
			Connection con = null;
			PreparedStatement ps = null;
			try {
				con = DataBaseUtil.getConnection();
				ps = con.prepareStatement(GET_REASON_IDS);
				return DataBaseUtil.queryToDynaList(ps);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
		}

}
