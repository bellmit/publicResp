package com.insta.hms.stores;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SupplierMasterDAO extends GenericDAO{

	static Logger logger = LoggerFactory.getLogger(ManufacturermasterDAO.class);

	Connection con=null;

	public SupplierMasterDAO() {
		super("supplier_master");
	}

	public static BasicDynaBean getSupplierDetails(Connection con, String supplierName) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("SELECT * FROM supplier_master WHERE upper(supplier_name)=upper(?);");
			ps.setObject(1, supplierName);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String GET_SUPP_DET = "SELECT SUPPLIER_NAME,SUPPLIER_ADDRESS,SUPPLIER_CITY,"
		          + " SUPPLIER_STATE,SUPPLIER_COUNTRY,SUPPLIER_PIN,SUPPLIER_PHONE1,SUPPLIER_PHONE2,"
		          + " SUPPLIER_FAX,SUPPLIER_MAILID,SUPPLIER_WEBSITE,SUPPLIER_CODE,CONTACT_PERSON_NAME,"
		          + " CONTACT_PERSON_MAILID,CONTACT_PERSON_MOBILE_NUMBER,SUPPLIER_TIN_NO,STATUS,"
		          + " SUPP_CATEGORY_ID,credit_period,CITY_ID,STATE_ID,IS_REGISTERED,CUST_SUPPLIER_CODE,"
		          + " DRUG_LICENSE_NO, PAN_NO, CIN_NO, TCS_APPLICABLE"
		          + " FROM SUPPLIER_MASTER WHERE SUPPLIER_CODE=?";

    public static BasicDynaBean getSelectedSuppDetails (String suppId) throws SQLException{

    	PreparedStatement ps = null;Connection con = null;
    	try{
    		con = DataBaseUtil.getReadOnlyConnection();
            ps = con.prepareStatement(GET_SUPP_DET);
            ps.setString(1, suppId);
            List list = DataBaseUtil.queryToDynaList(ps);
			if (list.size() > 0) {
				return (BasicDynaBean) list.get(0);
			} else {
				return null;
			}
    	}finally{
    		DataBaseUtil.closeConnections(con, ps);
    	}
    }

    private static final String INSERT_SUPPLIER = "INSERT INTO SUPPLIER_MASTER(SUPPLIER_NAME,SUPPLIER_ADDRESS,"
    	    + " SUPPLIER_CITY,SUPPLIER_STATE,SUPPLIER_COUNTRY,SUPPLIER_PIN,SUPPLIER_PHONE1,SUPPLIER_PHONE2,"
    	    + " SUPPLIER_FAX,SUPPLIER_MAILID,SUPPLIER_WEBSITE,SUPPLIER_CODE,CONTACT_PERSON_NAME,"
    	    + " CONTACT_PERSON_MAILID,CONTACT_PERSON_MOBILE_NUMBER,SUPPLIER_TIN_NO,STATUS,credit_period,cust_supplier_code)"
    	    + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public boolean insertSupplier (SupplierDTO dto) throws SQLException{
    	PreparedStatement ps = null;boolean status = false;
    	int count = 0;
    	try {
    		con = DataBaseUtil.getConnection();
    		String supplierCode = AutoIncrementId.getNewIncrUniqueId("SUPPLIER_CODE", "SUPPLIER_MASTER", "Supplier Code");
    		ps = con.prepareStatement(INSERT_SUPPLIER);
    		ps.setString(1, dto.getSuppmast_suppname());
    		ps.setString(2, dto.getSuppmast_address());
    		ps.setString(3, dto.getSuppmast_city());
    		ps.setString(4, dto.getSuppmast_state());
    		ps.setString(5, dto.getSuppmast_country());
    		ps.setString(6, dto.getSuppmast_pinCode());
    		ps.setString(7, dto.getSuppmast_phone1());
    		ps.setString(8, dto.getSuppmast_phone2());
    		ps.setString(9, dto.getSuppmast_fax());
    		ps.setString(10, dto.getSuppmast_email());
    		ps.setString(11, dto.getSuppmast_webSite());
    		ps.setString(12, supplierCode);
    		ps.setString(13, dto.getContact_person_name());
    		ps.setString(14, dto.getContact_person_mailid());
    		ps.setString(15, dto.getContact_person_mobile());
    		ps.setString(16, dto.getSupplier_tin_number());
    		ps.setString(17, "A");
    		ps.setInt(18, dto.getCreditPeriod() == null || dto.getCreditPeriod().isEmpty() ? 0 : Integer.parseInt(dto.getCreditPeriod()));
    		ps.setString(19, dto.getCust_supplier_code());
    		count = ps.executeUpdate();

	    	if (count > 0) {
	    		status = true;
	    	}
	    	return status;
    	}finally {
    		DataBaseUtil.closeConnections(con, ps);
    	}
    }
    
    private static final String UPDATE_SUPPLIER = "UPDATE SUPPLIER_MASTER SET SUPPLIER_NAME=?,SUPPLIER_ADDRESS=?,"
    	    + " SUPPLIER_CITY=?,SUPPLIER_STATE=?,SUPPLIER_COUNTRY=?,SUPPLIER_PIN=?,SUPPLIER_PHONE1=?,"
    	    + " SUPPLIER_PHONE2=?,SUPPLIER_FAX=?,SUPPLIER_MAILID=?,SUPPLIER_WEBSITE=?,"
    	    + " CONTACT_PERSON_NAME=?,CONTACT_PERSON_MAILID=?,CONTACT_PERSON_MOBILE_NUMBER=?,"
    	    + " SUPPLIER_TIN_NO=?,STATUS=?,credit_period = ?,cust_supplier_code=? WHERE SUPPLIER_CODE=?";

    public boolean updateSupplier (SupplierDTO dto) throws SQLException{
    	PreparedStatement ps = null;boolean status = false;
    	int count = 0;
    	try {
    		boolean pharmacy = false;
    		boolean inventory = false;
    		ps = con.prepareStatement(UPDATE_SUPPLIER);
    		ps.setString(1, dto.getSuppmast_suppname());
    		ps.setString(2, dto.getSuppmast_address());
    		ps.setString(3, dto.getSuppmast_city());
    		ps.setString(4, dto.getSuppmast_state());
    		ps.setString(5, dto.getSuppmast_country());
    		ps.setString(6, dto.getSuppmast_pinCode());
    		ps.setString(7, dto.getSuppmast_phone1());
    		ps.setString(8, dto.getSuppmast_phone2());
    		ps.setString(9, dto.getSuppmast_fax());
    		ps.setString(10, dto.getSuppmast_email());
    		ps.setString(11, dto.getSuppmast_webSite());
    		ps.setString(12, dto.getContact_person_name());
    		ps.setString(13, dto.getContact_person_mailid());
    		ps.setString(14, dto.getContact_person_mobile());
    		ps.setString(15, dto.getSupplier_tin_number());
    		ps.setString(16, dto.getStatus());
    		ps.setInt(17, dto.getCreditPeriod().isEmpty() ? 0 :Integer.parseInt(dto.getCreditPeriod()));
    		ps.setString(18, dto.getCust_supplier_code());

    		ps.setString(19, dto.getSuppCode());
    		count = ps.executeUpdate();

	    	if (count > 0) {
	    		status = true;
	    	}
	    	return status;
    	}finally {
    		DataBaseUtil.closeConnections(con, ps);
    	}
    }

    private static final String GETSUPPLIERS = "SELECT SUPPLIER_CODE,SUPPLIER_NAME,CUST_SUPPLIER_CODE FROM SUPPLIER_MASTER  ORDER BY SUPPLIER_NAME";

	public static ArrayList getSupplierNamesInMaster() throws SQLException {
		Connection con = null; PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GETSUPPLIERS);
			return DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String SUPPLIER_EXT_QUERY_FIELDS = " SELECT *";

	private static final String SUPPLIER_EXT_QUERY_COUNT =
		" SELECT count(supplier_code) ";

	private static final String SUPPLIER_EXT_QUERY_TABLES =
		  " FROM supplier_master";

	public static PagedList searchSuppliers(Map filter, Map listing)
		throws SQLException, ParseException {

		Connection con = DataBaseUtil.getReadOnlyConnection();

		SearchQueryBuilder qb = new SearchQueryBuilder(con,
				SUPPLIER_EXT_QUERY_FIELDS, SUPPLIER_EXT_QUERY_COUNT, SUPPLIER_EXT_QUERY_TABLES, listing);

		qb.addFilterFromParamMap(filter);
		qb.addSecondarySort("supplier_code");
		qb.build();

		PagedList l = qb.getMappedPagedList();

		qb.close();
		con.close();

		return l;
	}

	//private static final String SUPPLIERS_NAMESAND_iDS="SELECT supplier_name,supplier_code,cust_supplier_code FROM  supplier_master";
	private static final String SUPPLIERS_NAMESAND_iDS="SELECT supplier_name,supplier_code,cust_supplier_code, "+
			"	CASE WHEN cust_supplier_code IS NOT NULL AND  TRIM(cust_supplier_code) != ''  THEN supplier_name||' - '||cust_supplier_code ELSE supplier_name END as cust_supplier_code_with_name FROM supplier_master  ORDER BY supplier_name ";

    public static List getSuppliersNamesAndIds() throws SQLException{

	  return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(SUPPLIERS_NAMESAND_iDS));
}
    private static final String GET_SUPPLIER_DETAILS="SELECT supplier_code, supplier_name,status, supplier_address, supplier_city, supplier_state, "+
    	"supplier_country, supplier_pin, supplier_phone1,supplier_phone2,supplier_fax,supplier_mailid,supplier_website, "+
	    "contact_person_name,contact_person_mobile_number,contact_person_mailid, supplier_tin_no, credit_period, cust_supplier_code "+
	    "FROM  supplier_master WHERE status='A' ";



    public static List<BasicDynaBean> getSupplierDetails() throws SQLException{

		List supplierList=null;
		PreparedStatement ps=null;
		Connection con=null;
		con=DataBaseUtil.getReadOnlyConnection();
		ps=con.prepareStatement(GET_SUPPLIER_DETAILS);
		supplierList = DataBaseUtil.queryToDynaList(ps);
		DataBaseUtil.closeConnections(con, ps);

		return supplierList;
	}
    private static final String GET_SUPPLIER_DETAIL=" SELECT supplier_code, supplier_name, cust_supplier_code, status, supplier_address, supplier_city, supplier_state, "+
        	" supplier_country, supplier_pin, supplier_phone1,supplier_phone2,supplier_fax,supplier_mailid,supplier_website, "+
    	    " contact_person_name,contact_person_mobile_number,contact_person_mailid, supplier_tin_no, credit_period, scm.supp_category_name,is_registered, "+
        	" drug_license_no, pan_no, cin_no, tcs_applicable "+
    	    " FROM  supplier_master sm"+
    	    " JOIN supplier_category_master scm on(scm.supp_category_id=sm.supp_category_id) "+
    	    " ORDER BY supplier_code ";


    public static List<BasicDynaBean> getSupplierDetail() throws SQLException{

		List supplierList=null;
		PreparedStatement ps=null;
		Connection con=null;
		con=DataBaseUtil.getReadOnlyConnection();
		ps=con.prepareStatement(GET_SUPPLIER_DETAIL);
		supplierList = DataBaseUtil.queryToDynaList(ps);
		DataBaseUtil.closeConnections(con, ps);

		return supplierList;
	}
        
        
       /* //R.C move it to SupplierCenterDAO as this is respective DAO.
        private static final String DELETE_DEFAULT_CENTER="delete from supplier_center_master where "
          		+ "supplier_code in  (select supplier_code from supplier_center_master  "
          		+ "GROUP BY supplier_code having count(center_id) > 1) and center_id=0 ";
          
        public boolean deleteDefaultCenter() throws SQLException {
        	Connection con = DataBaseUtil.getReadOnlyConnection();
    		PreparedStatement ps = null;
    		try {
				ps = con.prepareStatement(DELETE_DEFAULT_CENTER);
				int rowsDeleted = ps.executeUpdate();
				return (rowsDeleted != 0);
    		} finally {
    			DataBaseUtil.closeConnections(con, ps);
    		}
    		
        }*/

}