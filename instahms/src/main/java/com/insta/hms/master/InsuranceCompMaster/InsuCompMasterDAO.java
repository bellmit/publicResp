package com.insta.hms.master.InsuranceCompMaster;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.MimeTypeDetector;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.PlanMaster.PlanMasterCacheDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.upload.FormFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsuCompMasterDAO extends GenericDAO {

	public  InsuCompMasterDAO() {
		super("insurance_company_master");
	}

	public String getNextICId() throws SQLException {
		String insuComp_Id = null;
		insuComp_Id = AutoIncrementId.getNewIncrId("insurance_co_id", "insurance_company_master", "ICM");

		return insuComp_Id;
	}

	private static final String INSURANCECOMPANIES_NAMESAND_iDS =
		"SELECT insurance_co_id,insurance_co_name,status,insurance_rules_doc_name from insurance_company_master";

	public static List getInsuranceCompaniesNamesAndIds() throws SQLException {
		   return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(
					   INSURANCECOMPANIES_NAMESAND_iDS+ " ORDER BY insurance_co_name "));
	}

	public static Map getInsuranceCompaniesNamesAndIdsMap() throws SQLException {
		List<BasicDynaBean> lst = DataBaseUtil.queryToDynaList(INSURANCECOMPANIES_NAMESAND_iDS+ " ORDER BY insurance_co_name ");
		Map h = new HashMap();
		for(BasicDynaBean b : lst) {
			h.put(b.get("insurance_co_name"), b.get("insurance_co_id"));
		}
		return h;
	}

	private static final String SEARCH_FIELDS = " SELECT ic.insurance_co_id,ic.insurance_co_name,ic.insurance_co_address, " +
			"ic.insurance_co_city,ic.insurance_co_state,ic.insurance_co_country,ic.insurance_co_phone,ic.insurance_co_email," +
			"ic.status,ic.default_rate_plan";
	private static final String SEARCH_COUNT = " SELECT count(*) ";
	private static final String SEARCH_TABLE =
		" FROM insurance_company_master ic ";

	public PagedList search(Map requestParams, Map listing)
			throws SQLException, ParseException {

		Connection con = DataBaseUtil.getReadOnlyConnection();

		SearchQueryBuilder qb = new SearchQueryBuilder(con, SEARCH_FIELDS,
				SEARCH_COUNT, SEARCH_TABLE, listing);

		qb.addFilterFromParamMap(requestParams);
		qb.addSecondarySort("insurance_co_id");

		qb.build();
		PagedList l = qb.getMappedPagedList();
		qb.close();
		DataBaseUtil.closeConnections(con, null);
		return l;
	}

	public static List getActiveCompanyNames() throws SQLException{
		   return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(INSURANCECOMPANIES_NAMESAND_iDS+" WHERE status='A' ORDER BY insurance_co_name "));
	}

	private static final String COMPANY_CODE =
		" SELECT CASE WHEN hic.insurance_co_code IS NULL OR trim(hic.insurance_co_code) = '' THEN '@'||insurance_co_name " +
		" ELSE hic.insurance_co_code END FROM " +
		" insurance_company_master icm " +
		" LEFT JOIN ha_ins_company_code hic ON(hic.insurance_co_id=icm.insurance_co_id AND hic.health_authority = ?)" +
		" WHERE icm.insurance_co_id = ? ";

	public static String getCompanyCode(String insuranceCoId,Integer centerId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
		try {
			ps = con.prepareStatement(COMPANY_CODE);
			ps.setString(1, healthAuthority);
			ps.setString(2, insuranceCoId);
			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	public boolean insertInsuRuleDocUploadFiles(InsuCompMasterDTO dto, Connection con, String insComId)
	throws SQLException, FileNotFoundException, IOException {

	String msg = "";
	PreparedStatement ps = null;
	int count = 0;
	boolean flag = false;
	InsuCompMasterDAO dao = new InsuCompMasterDAO();

	try {

		FormFile formFile = dto.getInsuruledoc();


		String fileName = formFile.getFileName();
		String contentType = MimeTypeDetector.getMimeTypes(formFile.getInputStream()).toString();

		String extension = "";

		if(fileName.contains(".")) {
			extension = fileName.substring(fileName.indexOf(".")+1);
	        if(extension.equals("odt") || extension.equals("ods") )
	        	contentType = "application/vnd.oasis.opendocument.text";
		}

			ps = con.prepareStatement("INSERT INTO insurance_company_master (insurance_co_id,insurance_co_name,insurance_co_address, " +
					" insurance_co_city,insurance_co_state,insurance_co_country,insurance_co_phone,insurance_co_email,status, " +
					" default_rate_plan,insurance_rules_doc_name, insurance_rules_doc_type, " +
					" insurance_rules_doc_bytea,tin_number, interface_code) VALUES (?, ?, ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ?, ?)");

			ps.setString(1, insComId);
			ps.setString(2, dto.getInsurance_co_name());
			ps.setString(3, dto.getInsurance_co_address());
			ps.setString(4, dto.getInsurance_co_city());
			ps.setString(5, dto.getInsurance_co_state() );
			ps.setString(6, dto.getInsurance_co_country());
			ps.setString(7, dto.getInsurance_co_phone());
			ps.setString(8, dto.getInsurance_co_email());
			ps.setString(9, dto.getStatus());
			ps.setString(10,dto.getDefault_rate_plan());


			ps.setString(11, fileName);
			ps.setString(12, contentType);
			ps.setBinaryStream(13, formFile.getInputStream(), (int)formFile.getFileSize());
			ps.setString(14,dto.getTin_number());
			ps.setString(15, dto.getInterface_code());
			PlanMasterCacheDAO cachedao= new PlanMasterCacheDAO();
			cachedao.invalidateCacheRegion(); 
		    count = ps.executeUpdate();
	  if(count > 0){
			return true;
	  }else {
			return false;
		}
	 }finally {
		DataBaseUtil.closeConnections(null, ps);
	  }

}

	private static final String GET_INS_DETAILS= "SELECT  insurance_co_id ,insurance_co_name,insurance_co_address,insurance_co_city, "+
		" insurance_co_state, insurance_co_country, insurance_co_phone, insurance_co_email, "+
		" status, default_rate_plan, insurance_co_code_obsolete,  insurance_rules_doc_name , "+
		" insurance_rules_doc_type, tin_number, interface_code FROM insurance_company_master where insurance_co_id = ? ";

	public BasicDynaBean getInsuranceCompanyDetails(String insComId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_INS_DETAILS);
			ps.setString(1, insComId);
			return DataBaseUtil.queryToDynaBean(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_UPLOADED_DOC =
		" SELECT insurance_rules_doc_name, insurance_rules_doc_bytea, insurance_rules_doc_type " +
		" FROM insurance_company_master " +
		" WHERE insurance_co_id=? " ;

	public static Map getUploadedDocInfo(String insucoId) throws SQLException {
    	Map<String,Object> upload = new HashMap<String,Object>();
    	Connection con = null;
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	try {
    		con = DataBaseUtil.getReadOnlyConnection();
    		ps = con.prepareStatement(GET_UPLOADED_DOC);
    		ps.setString(1, insucoId);
    		rs = ps.executeQuery();
    		if (rs.next()) {
    			String filename = rs.getString(1);
    			upload.put("filename", filename);
    			InputStream uploadfile = rs.getBinaryStream(2);
    			upload.put("uploadfile", uploadfile);
    			String contenttype = rs.getString(3);
    			upload.put("contenttype", contenttype);
    		}
    	 }finally {
    		DataBaseUtil.closeConnections(con, ps, rs);
    	   }
    	   return upload;
    }

	private static final String GET_INSURANCE_COMPANY_LIST = "SELECT icm.insurance_co_id, icm.insurance_co_name, icm.status, icm.insurance_rules_doc_name " +
	" FROM insurance_company_master icm " +
	" WHERE icm.status = 'A' ";
	private static final int String = 0;
	private static final Object Object = null;

	public List getinsuCompanyDetailsList() throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_INSURANCE_COMPANY_LIST);
	}


	public List listAll() throws SQLException {
		List columns = new ArrayList();
		columns.add("insurance_co_id");
		columns.add("insurance_co_name");
		columns.add("insurance_co_address");
		columns.add("insurance_co_city");
		columns.add("insurance_co_state");
		columns.add("insurance_co_country");
		columns.add("insurance_co_phone");
		columns.add("insurance_co_email");
		columns.add("default_rate_plan");
		columns.add("insurance_co_code_obsolete");
		columns.add("insurance_rules_doc_name");
		columns.add("status");
		return super.listAll(columns);
	}

	@Override
	public List listAll(List<String> columns, String filterBy, Object filterValue, String sortColumn) throws SQLException {
		if (null == columns) {
			columns = new ArrayList();
			columns.add("insurance_co_id");
			columns.add("insurance_co_name");
			columns.add("insurance_co_address");
			columns.add("insurance_co_city");
			columns.add("insurance_co_state");
			columns.add("insurance_co_country");
			columns.add("insurance_co_phone");
			columns.add("insurance_co_email");
			columns.add("default_rate_plan");
			columns.add("insurance_co_code_obsolete");
			columns.add("insurance_rules_doc_name");
			columns.add("status");
		}
		return super.listAll(columns, filterBy, filterValue, sortColumn);
	}

@Override
public BasicDynaBean findByKey(String keycolumn, Object identifier) throws SQLException {
	List columns = new ArrayList();
	columns.add("insurance_co_id");
	columns.add("insurance_co_name");
	columns.add("insurance_co_address");
	columns.add("insurance_co_city");
	columns.add("insurance_co_state");
	columns.add("insurance_co_country");
	columns.add("insurance_co_phone");
	columns.add("insurance_co_email");
	columns.add("default_rate_plan");
	columns.add("insurance_co_code_obsolete");
	columns.add("insurance_rules_doc_name");
	Map keymap = new HashMap();
	keymap.put(keycolumn, identifier);
	return super.findByKey(columns, keymap);
}


}

