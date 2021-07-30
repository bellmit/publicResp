package com.insta.hms.vitalForm;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.vitalparameter.VitalMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.RowSetDynaClass;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class genericVitalFormDAO extends GenericDAO {

	private String table = null;

	public genericVitalFormDAO(String table) {
		super(table);
		this.table = table;
	}

	public static final String GET_GENERIC_VITAL_FORM_FIELDS = " SELECT param_label, param_uom, param_id FROM vital_parameter_master ";

	public static final String GET_VITAL_READINGS_ = "SELECT vital_reading_id,to_char(date_time,'DD-MM-YYYY HH24:MI AM') AS date_time, "
			+ "to_char(date_time,'DD-MM-YYYY')as date,to_char(date_time,'HH24:MI')as time,consultation_id FROM visit_vitals "
			+ "WHERE patient_id=? and to_char(date_time,'DD-MM-YYYY') between ? and ?  ORDER BY date_time ";

	public static final String GET_PATIENT_VITAL_DETAILS = "SELECT  vpm.param_label, vpm.param_uom, " +
			"	vv.vital_reading_id, vr.param_id, vr.param_value, date_time "
			+ "FROM vital_reading vr "
			+ "JOIN visit_vitals vv USING(vital_reading_id) "
			+ "JOIN vital_parameter_master vpm USING(param_id) "
			+ "WHERE vpm.param_status='A' and vv.patient_id=? AND vr.vital_reading_id=? ";

	public static final String DELETE_VITAL_READINGS="DELETE  FROM vital_reading WHERE vital_reading_id=?";

	

	public static final String GET_VITAL_READINGS_FOR_VISIT = " SELECT  vpm.param_label, vpm.param_uom, " +
		"	vv.vital_reading_id, vr.param_id, vr.param_value, date_time, user_name " +
		" FROM vital_reading vr " +
		"	JOIN visit_vitals vv USING(vital_reading_id) " +
		"	JOIN vital_parameter_master vpm USING (param_id) " +
		" WHERE vv.patient_id=? ";
	
    private static final GenericDAO patientRegistrationDAO = new GenericDAO("patient_registration");
    
	public static List getVitalReadings(String patientId, String paramContainer) throws SQLException {
		paramContainer = paramContainer == null ? "" : paramContainer;
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			String query = GET_VITAL_READINGS_FOR_VISIT;
			if (!paramContainer.equals(""))
				query += " AND param_container=?";

			query += "order by vital_reading_id, param_id asc";
			ps = con.prepareStatement(query);
			ps.setString(1, patientId);
			if (!paramContainer.equals(""))
				ps.setString(2, paramContainer);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static List groupByReadingId(String patientId, String paramContainer) throws SQLException {
		// multiple rows(single row for each parameter) per single single reading id
		String visit = null;
		List<BasicDynaBean> paramWiseReadingList = getVitalReadings(patientId, paramContainer);
		BasicDynaBean patBean = patientRegistrationDAO.findByKey("patient_id",patientId);
		if(patBean != null)
			visit =  ((String)patBean.get("visit_type")).toUpperCase();
		List<BasicDynaBean> paramList = new VitalMasterDAO().listAll();
		ArrayList readingWiseList = new ArrayList();
		ArrayList records = new ArrayList();
		int readingId = 0;
		Map labelmap = null;
		for (BasicDynaBean bean: paramWiseReadingList) {
			if (readingId != (Integer) bean.get("vital_reading_id")) {
				labelmap = new LinkedHashMap();
				for (BasicDynaBean param: paramList) {
					labelmap.put(param.get("param_label"), "");
				}
				readingWiseList.add(labelmap);
			}
			if(bean.get("param_label") != null && !bean.get("param_label").equals("")){
			  labelmap.put(bean.get("param_label"), bean.get("param_value"));
			}
			labelmap.putAll(bean.getMap());
			readingId = (Integer) bean.get("vital_reading_id");

		}
		return readingWiseList;
	}

	public static final String VITAL_LABELS = "SELECT DISTINCT param_label " +
			" FROM vital_reading JOIN visit_vitals USING(vital_reading_id) " +
			"	JOIN vital_parameter_master vpm using (param_id)" +
			"	JOIN patient_registration pr using (patient_id)" +
			" WHERE pr.mr_no=? AND vpm.param_container='V' ";
	public static List getVitalLabels(String mrNo) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(VITAL_LABELS);
			ps.setString(1, mrNo);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	 public static final String GET_VITAL_VALUES =
			" SELECT param_label, vpm.param_uom, vr.param_value, date_time " +
			" FROM vital_reading vr " +
			" 	JOIN visit_vitals vv USING (vital_reading_id) " +
			" 	JOIN vital_parameter_master vpm USING (param_id) " +
			" 	JOIN patient_registration pr USING (patient_id) " +
			" WHERE pr.mr_no=? AND vpm.param_container = 'V' AND param_label IS NOT NULL AND param_label != ''";

	public static List getVitalValues(String mrno, String[] vitalLabels, Date fromDate, Date toDate, Integer limitOfRecords)
						throws SQLException, IOException {
			Connection con = null;
			PreparedStatement ps = null;
			StringBuffer query = new StringBuffer(GET_VITAL_VALUES);
			try {
				con = DataBaseUtil.getConnection();
				boolean first = true;
				if (vitalLabels != null && vitalLabels.length > 0) {
					query.append(" AND param_label in (");
					for (String value: vitalLabels) {
						if (first)
							query.append("?");
						else
							query.append(",?");
						first = false;
					}
					query.append(")");
				}
				if (fromDate != null){
					query.append(" AND date_time::date >= ?");
					query.append(" AND date_time::date <= ?");
				}
				query.append(" ORDER BY date_time");
				if(limitOfRecords != null)
					query.append(" limit "+limitOfRecords);
				int i = 1;
				ps = con.prepareStatement(query.toString());
				ps.setString(i++, mrno);

				if ( vitalLabels != null) {
					for (String value: vitalLabels) {
						ps.setString(i++, value);
					}
				}
				if (fromDate != null){
					ps.setDate(i++, fromDate);
					ps.setDate(i++, toDate);
				}

				return DataBaseUtil.queryToDynaList(ps);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
		}


	public static final String VITAL_HISTORY = "" +
			" SELECT  vv.patient_id, pr.reg_date, vpm.param_label, vpm.param_uom, " +
			"	vv.vital_reading_id, vr.param_id, vr.param_value, date_time " +
			" FROM vital_reading vr " +
			"	JOIN visit_vitals vv USING(vital_reading_id) " +
			"	JOIN vital_parameter_master vpm USING(param_id) " +
			" 	JOIN patient_registration pr ON (pr.patient_id=vv.patient_id) " +
			" WHERE vpm.param_status='A' and vv.patient_id IN " +
			" (SELECT dc.patient_id FROM doctor_consultation dc " +
			" 		JOIN patient_registration pr using (patient_id) " +
			"	WHERE dc.mr_no=? AND pr.visit_type=? AND doctor_name=? AND consultation_id < ? " +
			"	ORDER BY consultation_id desc) order by patient_id desc, vr.vital_reading_id, param_id";
	public static List getVitalsHistory(String mrNo, int consultationId, String doctorId,
			String visitType) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(VITAL_HISTORY);
			ps.setString(1, mrNo);
			ps.setString(2, visitType);
			ps.setString(3, doctorId);
			ps.setInt(4, consultationId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	private static final String READING_EXISTS = "SELECT * FROM vital_reading WHERE vital_reading_id=? and param_id=?";
	public static boolean readingExists(Connection con, int vitalReadingId, int paramId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(READING_EXISTS);
			ps.setInt(1, vitalReadingId);
			ps.setInt(2, paramId);
			rs = ps.executeQuery();
			if (rs.next()) return true;
		} finally {
			DataBaseUtil.closeConnections(null, ps, rs);
		}
		return false;
	}

	public List<Hashtable> getGenericVitalFormDetails() throws SQLException {

		PreparedStatement ps = null;
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con
					.prepareStatement(GET_GENERIC_VITAL_FORM_FIELDS
							+ "WHERE param_container='V' AND param_status='A' order by param_order ASC");
			List genericVitalFormFieldList = DataBaseUtil.queryToArrayList(ps);
			return genericVitalFormFieldList;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public List<Hashtable> getGenericIntakeFormDetails() throws SQLException {

		PreparedStatement ps = null;
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con
					.prepareStatement(GET_GENERIC_VITAL_FORM_FIELDS
							+ "WHERE param_container='I'AND param_status='A' order by param_order ASC ");
			List genericVitalFormFieldList = DataBaseUtil.queryToArrayList(ps);
			return genericVitalFormFieldList;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public List<Hashtable> getGenericOutputFormDetails() throws SQLException {

		PreparedStatement ps = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con
					.prepareStatement(GET_GENERIC_VITAL_FORM_FIELDS
							+ "WHERE param_container='O'AND param_status='A' order by param_order ASC ");
			List genericVitalFormFieldList = DataBaseUtil.queryToArrayList(ps);
			return genericVitalFormFieldList;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public boolean insertVisitVitalsDetails(Connection con, HashMap fields)
			throws SQLException {

		return DataBaseUtil.dynaInsert(con, "visit_vitals", fields);
	}

	public boolean insertVitalReadingDetails(Connection con, HashMap fields)
			throws SQLException {

		return DataBaseUtil.dynaInsert(con, "vital_reading", fields);
	}

	public ArrayList getTableColumns(String visitId) throws SQLException {

		PreparedStatement ps = null;
		Connection con = null;
		ArrayList<String> vitalContainer = null;
		ArrayList<String> vitalActiveColumns = null;
		vitalActiveColumns=new ArrayList<String>();
     	vitalContainer = new ArrayList<String>();
		vitalContainer.add("V");
		vitalContainer.add("I");
		vitalContainer.add("O");
		try {
			con = DataBaseUtil.getConnection();

			for (int i = 0; i < vitalContainer.size(); i++) {
				ps = con
						.prepareStatement(GET_GENERIC_VITAL_FORM_FIELDS
								+ "WHERE param_container=? AND param_status='A' order by param_order ASC ");
				ps.setString(1, vitalContainer.get(i));
				vitalActiveColumns.addAll(DataBaseUtil.queryToArrayList(ps));
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return vitalActiveColumns;
	}

	public List<Hashtable> getVisitVitaldetails(String visitId, String fromDate, String toDate)
			throws SQLException {

		PreparedStatement ps = null;
		int vitalReadinId = 0;
		int prevVReadingId = 0;
		String paramLabel = null;
		Hashtable hashtable = null;
		Connection con = null;
		List columnCount = getTableColumns(visitId);
		ArrayList arrResult = new ArrayList();
		try {
			con = DataBaseUtil.getConnection();
	     	ps = con.prepareStatement(GET_VITAL_READINGS_);
			ps.setString(1, visitId);
			ps.setString(2, fromDate);
			ps.setString(3, toDate);
      try (ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {

          vitalReadinId = rs.getInt("vital_reading_id");
          if (vitalReadinId == prevVReadingId)
            continue;

          prevVReadingId = vitalReadinId;
          try (PreparedStatement ps1 =
              con.prepareStatement(GET_PATIENT_VITAL_DETAILS
                  + "AND vr.param_id=? AND to_char(date_time,'DD-MM-YYYY') BETWEEN ? AND ?")) {

            Iterator<Hashtable> it = columnCount.iterator();

            while (it.hasNext()) {

              Hashtable _paramLabel = it.next();
              hashtable = new Hashtable();

              ps1.setString(1, visitId);
              ps1.setInt(2, vitalReadinId);
              ps1.setInt(3, Integer.parseInt((String) _paramLabel.get("PARAM_ID")));
              ps1.setString(4, fromDate);
              ps1.setString(5, toDate);

              try (ResultSet rs1 = ps1.executeQuery()) {

                if (rs1.next()) {
                  hashtable.put("vital_reading_id", rs1.getInt("vital_reading_id"));
                  hashtable.put("param_id", rs1.getInt("param_id"));
                  hashtable.put("param_value", rs1.getString("param_value"));
                  arrResult.add(hashtable);
                } else {
                  hashtable.put("vital_reading_id", vitalReadinId);
                  hashtable.put("param_id", _paramLabel.get("PARAM_ID"));
                  hashtable.put("param_value", "");

                  arrResult.add(hashtable);
                }
              }

            }
          }

        }
      }
      return arrResult;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

	public boolean deleteVitalReadings(Connection con, int vitalReadingId)
			throws SQLException {
		PreparedStatement ps = null;
		int i = 0;
		boolean target = false;
		try {
			ps = con.prepareStatement(DELETE_VITAL_READINGS);
			ps.setInt(1, vitalReadingId);
			i = ps.executeUpdate();
			if (i > 0)
				target = true;
			return target;
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public ArrayList getVisitVitalReadings(String visitId, String fromDate, String toDate) throws SQLException {

		PreparedStatement ps = null;
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_VITAL_READINGS_);
			ps.setString(1, visitId);
			ps.setString(2, fromDate);
			ps.setString(3, toDate);
			ArrayList visitVitalReadingList = DataBaseUtil.queryToArrayList(ps);
			return visitVitalReadingList;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_VITAL_PARAMETER_MASTER = "SELECT param_id,param_container,param_label,param_uom,param_order " +
			" FROM vital_parameter_master WHERE param_status='A' AND param_container = 'V' AND (visit_type = ? or visit_type is null) order by param_order ";
	public static List<BasicDynaBean> getVitalParameterMaster(String patientId) throws SQLException {

		PreparedStatement ps = null;
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_VITAL_PARAMETER_MASTER);
			BasicDynaBean patBean = patientRegistrationDAO.findByKey("patient_id",patientId);
			if(patBean != null)
				ps.setString(1, ((String)patBean.get("visit_type")).toUpperCase());
			else
				ps.setString(1, null);
			List vitalMasterList = DataBaseUtil.queryToDynaList(ps);
			return vitalMasterList;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_ALL_PARAMETER_MASTER = "SELECT param_id,param_container,param_label,param_uom,param_order " +
							" FROM vital_parameter_master WHERE (visit_type = ? or visit_type is null) ";
	public static List<BasicDynaBean> getVitalParameterMaster(String patientId,String paramType, String status) throws SQLException {

	PreparedStatement ps = null;
	Connection con = null;
	try {
		con = DataBaseUtil.getReadOnlyConnection();
		String query = GET_ALL_PARAMETER_MASTER;
		if (status != null) {
		  query += " AND param_status='" + status + "' ";
		}
		if(!paramType.equals("V"))
			query += " AND param_container IN ('I','O') ";
		else
			query += " AND param_container = 'V' ";
		query += "	order by param_order ";
		ps = con.prepareStatement(query);
		BasicDynaBean patBean = patientRegistrationDAO.findByKey("patient_id",patientId);
		if(patBean != null)
			ps.setString(1, ((String)patBean.get("visit_type")).toUpperCase());
		else
			ps.setString(1, null);
		List vitalMasterList = DataBaseUtil.queryToDynaList(ps);
		return vitalMasterList;
	} finally {
		DataBaseUtil.closeConnections(con, ps);
	}
}




	private static final String GET_VITAL_VISIT_READINGS = "select vv.vital_reading_id,vv.date_time,vm.param_label" +
			" ,vm.param_id,vr.param_value,'' as param_uom,vv.user_name from visit_vitals vv join vital_reading vr using(vital_reading_id)" +
			" left join vital_parameter_master vm using(param_id) where vv.patient_id = ?  ";
  public static List<BasicDynaBean> getVisitFormReadings(String visitId, String paramType) throws SQLException {
    return getVisitFormReadings(visitId, paramType, false);
  }
  public static List<BasicDynaBean> getVisitFormReadings(String visitId, String paramType, boolean discardEmpty) throws SQLException {

		PreparedStatement ps = null;
		Connection con = null ;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			String query = GET_VITAL_VISIT_READINGS ;
			if(!paramType.equals("V"))
				query += " AND param_container IN ('I','O') ";
			else
				query += " AND param_container = 'V' ";
      if (discardEmpty) {
        query += " AND vr.param_value != '' AND vr.param_value IS NOT NULL ";
      }
			query += " order by vv.date_time desc,vv.vital_reading_id,vr.param_id ";
			ps = con.prepareStatement(query);
			if(visitId != null) {
				ps.setString(1, visitId);
			}
			List vitalReadingList = DataBaseUtil.queryToDynaList(ps);
			return vitalReadingList;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String VITAL_READING_FIELDS ="select vital_reading_id,date_time " ;
	private static final String COUNT = "SELECT count(*) ";
	private static final String TABLES =
		"  FROM (select vv.vital_reading_id,vv.date_time, vpm.param_container,vv.patient_id from visit_vitals vv " +
	" JOIN vital_reading vr USING (vital_reading_id) "+
 " JOIN vital_parameter_master vpm ON (vr.param_id=vpm.param_id) " +
 "GROUP BY vv.vital_reading_id,vv.date_time, vpm.param_container,vv.patient_id ORDER BY vv.date_time,vv.vital_reading_id ) as readings ";
 private static final String GROUP_BY =" vital_reading_id,date_time ";

	public static PagedList getVisitFormReadings(String patientId, String vitalPageNumParam, String pageSizeParam ,String paramType) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		SearchQueryBuilder qb = null;
		int pageNum = 0,noOfRecord = 0;

		try {
			con = DataBaseUtil.getConnection();
			int pageSize = 20;
			if (pageSizeParam != null && !pageSizeParam.equals(""))
				pageSize = Integer.parseInt(pageSizeParam);

			String query =  "select vital_reading_id from visit_vitals vv join vital_reading vr using (vital_reading_id) " +
					"	join vital_parameter_master vpm ON (vpm.param_id=vr.param_id) " + " WHERE patient_id  = ? ";
			if(paramType.equals("V")) {
				query += " AND param_container='V' ";
			} else {
				query += " AND param_container IN ('I','O') ";
			}
			query += "GROUP BY vital_reading_id";
			ps = con.prepareStatement(query);
			ps.setString(1, patientId);
			List countList = DataBaseUtil.queryToDynaList(ps);
			noOfRecord = countList.size();
			if (vitalPageNumParam != null && !vitalPageNumParam.equals("")) {
				pageNum = Integer.parseInt(vitalPageNumParam);
			} else {

				int mod = noOfRecord % pageSize;
				if (mod == 0) {
					pageNum =  noOfRecord/pageSize;
				} else {
					pageNum =  noOfRecord/pageSize + 1;
				}
			}
			qb = new SearchQueryBuilder(con, VITAL_READING_FIELDS, null, TABLES, null,GROUP_BY , "date_time", false, pageSize, pageNum);
			qb.addSecondarySort("vital_reading_id");
			qb.addFilter(SearchQueryBuilder.STRING, "patient_id ", "=", patientId);
			if(paramType.equals("V")) {
				qb.appendToQuery(" param_container='V' ");
			} else {
				qb.appendToQuery(" param_container IN ('I','O') ");
			}
			qb.build();

			PreparedStatement psData = qb.getDataStatement();
			ResultSet rsData = psData.executeQuery();

			RowSetDynaClass rsd = new RowSetDynaClass(rsData);
			List dataList = rsd.getRows();
			rsData.close();

			return new PagedList(dataList, noOfRecord, pageSize, pageNum);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String VITAL_READING_FIELDS_VALUES = "SELECT vr.vital_reading_id, vpm.param_id, vpm.param_label, " +
			" vr.param_value,vpm.param_uom,vpm.param_container  FROM vital_reading vr "+
			" left join vital_parameter_master vpm using(param_id) " +
			" where vpm.param_status='A' AND (visit_type = 'I' or visit_type is null) ";
	@SuppressWarnings("unchecked")
	public static List<BasicDynaBean> getVitalFormDAOValues(PagedList pagedList,String paramType) throws SQLException{
		PreparedStatement ps = null;
		int index = 1;
		Connection con = null;
		List<BasicDynaBean> list = pagedList.getDtoList();
		if (list.isEmpty()) return Collections.EMPTY_LIST;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			String readingIds = " vital_reading_id IN (";
			boolean first = true;
			for (BasicDynaBean bean : list) {
				if (!first) {
					readingIds += ",";
				}
				readingIds += "?";
				first = false;
			}
			readingIds += ") ";
			String query = VITAL_READING_FIELDS_VALUES + " AND " + readingIds ;
			if(paramType.equals("V"))
				query += " AND vpm.param_container = 'V'  ORDER BY vital_reading_id  " ;
			else
				query += " AND vpm.param_container IN ('I','O')  ORDER BY vital_reading_id  ";
			ps = con.prepareStatement(query);

			int readingId = 0;
			for (BasicDynaBean bean : list) {
				readingId = (Integer) bean.get("vital_reading_id");
				ps.setInt(index++, readingId);
			}
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	private static final String GET_VITAL_VISIT_READINGSIDs =
			"select vv.vital_reading_id, vv.date_time, vv.user_name from visit_vitals vv " +
			"	JOIN vital_reading vr USING (vital_reading_id) " +
			"	JOIN vital_parameter_master vpm ON (vr.param_id=vpm.param_id) where patient_id= ? ";

	public static List<BasicDynaBean> getVisitVitalReadingIds(String patientId, String paramType)  throws SQLException {
		PreparedStatement ps = null;
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			String query = GET_VITAL_VISIT_READINGSIDs;
			if (paramType.equals("V"))
				query += " AND vpm.param_container='V'";
			else
				query += " AND vpm.param_container in ('I', 'O') ";
			query += " GROUP BY vv.vital_reading_id, vv.date_time, vv.user_name order by date_time ";

			ps = con.prepareStatement(query);
			ps.setString(1, patientId);
			List vitalReadingList = DataBaseUtil.queryToDynaList(ps);
			return vitalReadingList;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String UPDATE_VITAL_DATE_TIME="UPDATE visit_vitals SET date_time=? WHERE vital_reading_id=?";

	public boolean updateVitalDateTime(Timestamp datetime, int vitalreadingId, Connection con)
			throws SQLException {

		PreparedStatement ps = null;
		boolean target = false;
		try {
			ps = con.prepareStatement(UPDATE_VITAL_DATE_TIME);
			ps.setTimestamp(1, datetime);
			ps.setInt(2, vitalreadingId);
			int i = ps.executeUpdate();
			if (i > 0)
				target = true;

			return target;
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String INTAKEOUTPUTPARAM =
		" (select param_id, param_label, param_uom, param_container from  vital_parameter_master where param_container='I' AND param_status='A' " +
		"	limit 4-(select  count(*)-(count(*)-1) from vital_parameter_master where  param_container='O' limit 2)) " +
		" union all " +
		" (select param_id, param_label, param_uom, param_container from  vital_parameter_master where param_container='O' AND param_status='A' " +
		"	limit 4-(select  count(*)-(count(*)-1) from vital_parameter_master where  param_container='I' limit 2)) ";
	public static List getIntakeOutputParams() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(INTAKEOUTPUTPARAM);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String exactresultrange =
		" SELECT * FROM vital_reference_range_master where param_id = ? AND  " +
		" ( range_for_all = 'N'  AND " +
		" ( (min_patient_age IS NULL OR min_patient_age*(CASE WHEN age_unit = 'Y' THEN 365.25 ELSE 1 end) <= " +
		"		( SELECT (current_date - COALESCE(dateofbirth, expected_dob))::integer " +
		"			FROM patient_details WHERE mr_no = ? ) )" +
		"  AND" +
		"   (max_patient_age IS NULL OR (  SELECT (current_date - COALESCE(dateofbirth, expected_dob))::integer " +
		"			FROM patient_details WHERE mr_no = ? ) <= " +
		"			max_patient_age*(CASE WHEN age_unit = 'Y' THEN 365.25 ELSE 1 end) )) " +
		" AND (patient_gender = ? OR patient_gender = 'N')) ORDER BY priority LIMIT 1 ";

	private static String result_all = " SELECT * FROM vital_reference_range_master where param_id = ? AND  " +
		" range_for_all = 'Y'  ";

	private static String EMPTY_BEAN = "SELECT *, " +
			"'' as min_normal_value, '' as max_normal_value, '' as min_critical_value, '' as max_critical_value," +
			"'' as min_improbable_value, '' as max_improbable_value, '' as reference_range_txt" +
			" FROM vital_parameter_master vm" +
			" WHERE vm.param_id = ? ";

	public static List<BasicDynaBean> getReferenceRange(Map<String, Object> pd)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		String placeHolderValue =  (String)pd.get("mr_no") ;
		BasicDynaBean resultRangeBean = null;
		ArrayList<BasicDynaBean> list = new ArrayList<BasicDynaBean>();
		try{
			con = DataBaseUtil.getConnection();
			List<BasicDynaBean> vitalMasterList = new GenericDAO("vital_parameter_master").listAll();
			for (int i=0; i<vitalMasterList.size(); i++) {
				BasicDynaBean vitalMaster = vitalMasterList.get(i);
				ps = con.prepareStatement(exactresultrange );
				ps.setInt(1, (Integer)vitalMaster.get("param_id"));
				ps.setString(2,placeHolderValue);
				ps.setString(3, placeHolderValue);
				ps.setString(4, (String)pd.get("patient_gender"));
				resultRangeBean = DataBaseUtil.queryToDynaBean(ps);

				if( resultRangeBean == null ){
					ps = con.prepareStatement(result_all);
					ps.setInt(1, (Integer)vitalMaster.get("param_id"));

					resultRangeBean = DataBaseUtil.queryToDynaBean(ps);
				}

				if( resultRangeBean == null ){
					ps = con.prepareStatement(EMPTY_BEAN);
					ps.setInt(1, (Integer)vitalMaster.get("param_id"));

					resultRangeBean = DataBaseUtil.queryToDynaBean(ps);
				}

				list.add(resultRangeBean);
			}
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return list;
	}

}
