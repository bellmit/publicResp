package com.insta.hms.master.PrintConfigurationMaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;


public class PrintConfigurationsDAO extends GenericDAO {

	static Logger log = LoggerFactory.getLogger(PrintConfigurationsDAO.class);

	public static final String PRINT_TYPE_DIAG = "Lab";
	public static final String PRINT_TYPE_DIAG_RAD = "Rad";
	public static final String PRINT_TYPE_BILL = "Bill";
	public static final String PRINT_TYPE_PHARMACY = "Pharmacy";
	public static final String PRINT_TYPE_PATIENT = "Discharge";
	public static final String PRINT_TYPE_DISCHARGE = "Discharge";
	public static final String PRINT_TYPE_SERVICE = "Service";
	public static final String PRINT_TYPE_INSURENCE = "Insurance";
	public static final String PRINT_TYPE_STORE = "Store";
	public static final String PRINT_TYPE_APPOINTMENT = "Appointment";
	public static final String PRINT_TYPE_PATIENT_SURVEY_RESPONSE = "SurveyResponse";
	public static final String PRINT_TYPE_SAMPLE_COLLECTION = "Sample";
	public static final String PRINT_TYPE_PRESCRIPTION_LABEL = "PrescLabel";
	public static final String PRINT_TYPE_SAMPLE_WORK_SHEET = "SampleWorkSheet";
	public static final String PRINT_TYPE_WEB_DIAG = "Web Diag";

	public PrintConfigurationsDAO(String table){
		super(table);
	}

	public BasicDynaBean getRecord(String printType, int centerId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("SELECT * FROM hosp_print_master where print_type=? and center_id=?");
			ps.setString(1, printType);
			ps.setInt(2, centerId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String PRINT_MASTER_FIELDS = "SELECT print_type, pd.printer_definition_name, "+
		" header1, header2, header3, footer1, footer2, footer3 ,printer_id ";

	private static final String PRINT_MASTER_COUNT =" SELECT COUNT(hpm.*) ";

	private static final String PRINT_MASTER_TABLE =" FROM hosp_print_master hpm LEFT JOIN  "+
		" printer_definition pd using(printer_id)";

	public PagedList getPrintMaster(Map<LISTING, Object> pagingParams)throws SQLException{
		int pageNum = (Integer)pagingParams.get(LISTING.PAGENUM);
		Connection con = DataBaseUtil.getReadOnlyConnection();
		SearchQueryBuilder qb = null;
		try {
			qb =
				new SearchQueryBuilder(con,PRINT_MASTER_FIELDS,PRINT_MASTER_COUNT,PRINT_MASTER_TABLE,
						" WHERE center_id=0 ", null, "print_type", false, 25, pageNum);
			qb.build();

			return qb.getDynaPagedList();
		}
		finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
		}
	}

	public  final String GET_LOGO_SIZE = "SELECT length(logo) as logo_size "+
		" FROM hosp_print_master_files where center_id = ?";

	public  int getFileSizes(int center_id) throws SQLException {
		int size = 0;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_LOGO_SIZE);
			ps.setInt(1, center_id);
			rs = ps.executeQuery();
			while (rs.next()){
				size = rs.getInt(1);
			}
		}finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}

		return size;
	}

	private static final String GET_LOGO = "SELECT logo FROM hosp_print_master_files where center_id = ?";

	public static InputStream getLogo(int center_id) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_LOGO);
			ps.setInt(1, center_id);
			rs = ps.executeQuery();
			if (rs.next())
				return rs.getBinaryStream(1);
			else
				return null;
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
	}
	
	private static final String GET_CENTERS_LOGOS = "select center_id, center_name, length(hpmf.logo) as logo_size "
			+ "from hospital_center_master LEFT JOIN hosp_print_master_files hpmf using(center_id) where status = 'A' # order by center_name";
	
	public static List getCentersAndLogoSizes(int center_id) throws SQLException{

		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			 String query = new String(GET_CENTERS_LOGOS);
			if(center_id != -1) {
				//when max_centers_inc_default is < 1
				if(center_id == 0 ) {
				    query = query.replace("#", " AND center_id = ? ");
				    ps = con.prepareStatement(query);
				    ps.setInt(1, center_id);
				} else {
				    query = query.replace("#", " AND center_id in( ?,?) ");
				    ps = con.prepareStatement(query);
				    ps.setInt(1, center_id);					
				    ps.setInt(2, 0);
				}
			} else {
				query = query.replace("#", "");
				ps = con.prepareStatement(query);				
			}
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private static final String DELETE_LOGO = "UPDATE hosp_print_master_files set logo='' where center_id = ?";

	public static boolean deleteLogo(int center_id) throws SQLException {
		boolean success=false;
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(DELETE_LOGO);
			ps.setInt(1, center_id);
			int result = ps.executeUpdate();
			if (result > 0 ) success = true;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return success;
	}

	/*
	 * Returns the complete print settings using the given printer definition ID for
	 * the given print type. Print type can be one of PRINT_TYPE_XXX constants defined above.
	 * The return value includes:
	 *   Page options: page width, height etc (as in printer_definition)
	 *   Header/footer settings: header1, header2 etc (as in hosp_print_master)
	 * Note: if printId == 0, it falls back to the default printer for the given type.
	 */
	
	public static BasicDynaBean getPageOptions(String printType, int printId)throws SQLException{
		return getPageOptions(printType, printId, RequestContext.getCenterId());
	}
	
	private static  String PRINT_PAGE_DEFINITIONS = "SELECT pd.*,hpm.* FROM printer_definition pd "+
		" LEFT JOIN hosp_print_master hpm on pd.printer_id=? where  print_type=? and center_id=?";

	public static BasicDynaBean getPageOptions(String printType, int printId, int centerId)throws SQLException{

		if (printId == 0)
			return getCenterPageOptions(printType, centerId);

		Connection con = null;
		PreparedStatement ps = null;
		try  {
			con = DataBaseUtil.getReadOnlyConnection();

			ps = con.prepareStatement(PRINT_PAGE_DEFINITIONS);
			ps.setInt(1, printId);
			ps.setString(2,printType);
			ps.setInt(3, centerId);
			BasicDynaBean bean = DataBaseUtil.queryToDynaBean(ps);
			if (bean != null) {
				// found center specific print configurations.
				return bean;
			} else {
				// not found center specific print configuration so return the super center print configuration.
				if (centerId != 0) {
					ps.setInt(1, printId);
					ps.setString(2,printType);
					ps.setInt(3, 0);
					return DataBaseUtil.queryToDynaBean(ps);
				}
			}

		}
		finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return null;

	}

	/*
	 * Returns the complete print settings using the default printer defined for
	 * the given print type. Print type can be one of PRINT_TYPE_XXX constants defined above.
	 * The return value includes:
	 *   Page options: page width, height etc (as in printer_definition)
	 *   Header/footer settings: header1, header2 etc (as in hosp_print_master)
	 */
	
	public static BasicDynaBean getPageOptions(String printType)throws SQLException{
		return getCenterPageOptions(printType, RequestContext.getCenterId());
	}

	private static  String PRINT_PAGE_OPTIONS = "SELECT pd.*,hpm.* FROM printer_definition pd "+
		" LEFT JOIN hosp_print_master hpm USING(printer_id) ";

	public static BasicDynaBean getCenterPageOptions(String printType, int centerId)throws SQLException{

		String PRINT_TYPE = PRINT_PAGE_OPTIONS+" where print_type= ? and center_id=?" ;
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(PRINT_TYPE);
			ps.setString(1, printType);
			ps.setInt(2, centerId);
			BasicDynaBean bean = DataBaseUtil.queryToDynaBean(ps);
			if (bean != null) {
				// found center specific print configurations.
				return bean;
			} else {
				// not found center specific print configuration so return the super center print configuration.
				if (centerId != 0) {
					ps.setString(1, printType);
					ps.setInt(2, 0);
					bean = DataBaseUtil.queryToDynaBean(ps);
					return bean;
				}
			}
		}
		finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return null;
	}

	public static BasicDynaBean getPrintMode(String printType) throws SQLException {
		return getPageOptions(printType);
	}

	/*
	 * Convenience wrappers around the above
	 */
	public static BasicDynaBean getDiagDefaultPrintPrefs() throws SQLException {
		return getPageOptions(PRINT_TYPE_DIAG);
	}
	public static BasicDynaBean getDiagRadDefaultPrintPrefs() throws SQLException {
		return getPageOptions(PRINT_TYPE_DIAG_RAD);
	}
	public static BasicDynaBean getServiceDefaultPrintPrefs() throws SQLException {
		return getPageOptions(PRINT_TYPE_SERVICE);
	}

	public static BasicDynaBean getBillDefaultPrintPrefs() throws SQLException {
		return getPageOptions(PRINT_TYPE_BILL);
	}

	public static BasicDynaBean getDischargeDefaultPrintPrefs() throws SQLException {
		return getPageOptions(PRINT_TYPE_DISCHARGE);
	}

	public static BasicDynaBean getPharmacyDefaultPrintPrefs() throws SQLException {
		return getPageOptions(PRINT_TYPE_PHARMACY);
	}

	public static BasicDynaBean getPatientDefaultPrintPrefs() throws SQLException {
		return getPageOptions(PRINT_TYPE_PATIENT);
	}
	
	public static BasicDynaBean getPatientDefaultPrintPrefs(int centerId) throws SQLException {
		return getCenterPageOptions(PRINT_TYPE_PATIENT, centerId);
	}
	
	public static BasicDynaBean getSampleDefaultPrintPrefs() throws SQLException {
		return getPageOptions(PRINT_TYPE_SAMPLE_COLLECTION);
	}
	
	public static BasicDynaBean getWebDiagPrintPrefs() throws SQLException {
		return getPageOptions(PRINT_TYPE_WEB_DIAG);
	}

	private static final String PRINT_TYPES = "SELECT printer_definition_name, print_type "+
		"FROM hosp_print_master hpm join printer_definition pd using(printer_id)" ;

	public static List getPrinterTypes() throws SQLException{
		return DataBaseUtil.queryToDynaList(PRINT_TYPES);
	}
}
