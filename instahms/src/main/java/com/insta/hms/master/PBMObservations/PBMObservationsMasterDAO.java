/**
 *
 */
package com.insta.hms.master.PBMObservations;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @author lakshmi
 *
 */
public class PBMObservationsMasterDAO extends GenericDAO {

	static Logger logger = LoggerFactory.getLogger(PBMObservationsMasterDAO.class);

	public PBMObservationsMasterDAO() {
		super("pbm_observations_master");
	}

	private static final String PBM_OBSERVATIONS_FIELDS = " SELECT * ";

	private static final String PBM_OBSERVATIONS_COUNT = " SELECT COUNT(id) ";

	private static final String PBM_OBSERVATIONS_TABLES = " FROM  pbm_observations_master ";

	public PagedList getPBMObservationsList(Map map ,Map paginParams) throws SQLException,ParseException {
		Connection con = null;
		SearchQueryBuilder qb = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			qb = new SearchQueryBuilder(con, PBM_OBSERVATIONS_FIELDS, PBM_OBSERVATIONS_COUNT, PBM_OBSERVATIONS_TABLES,paginParams);
			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("observation_name");
			qb.build();
			return qb.getMappedPagedList();
		}
		finally {
			qb.close();
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String PATIENT_PRESCRIPTIONS_COLUMNS =
		" SELECT column_name, data_type, ordinal_position, is_nullable " +
		" FROM information_schema.columns " +
		" WHERE table_schema=? AND table_name='pbm_medicine_prescriptions' ";

	public List<BasicDynaBean> getPatientPrescriptionColumns() throws SQLException {
		String schema = RequestContext.getSchema();
		return DataBaseUtil.queryToDynaList(PATIENT_PRESCRIPTIONS_COLUMNS, schema);
	}

	private static final String FIND_PBM_OBSERVATION =
		" SELECT * FROM pbm_observations_master " +
		" WHERE upper(observation_name)=upper(?) OR upper(patient_med_presc_value_column)=upper(?) ";

	public BasicDynaBean findPBMObservation(String observation_name,
							String patient_med_presc_value_column) throws SQLException {
		return DataBaseUtil.queryToDynaBean(FIND_PBM_OBSERVATION,
							new Object[]{observation_name, patient_med_presc_value_column});
	}
}
