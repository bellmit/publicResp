package com.insta.hms.master.DocumentTypeMaster;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;

import java.util.Map;
import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DocumentTypeMasterDAO  extends GenericDAO{
	Connection con = null;

	public DocumentTypeMasterDAO() {
		super("doc_type");
	}

	public String getNextDoctypeId() throws SQLException {
		String DoctypeID = null;
		DoctypeID =	AutoIncrementId.getNewIncrUniqueId("DOC_TYPE_ID", "DOC_TYPE", "Document Number");

		return DoctypeID;
	}

	public ArrayList getdocTypeDetails() throws SQLException {
		PreparedStatement ps = null;
		ArrayList arrDocTypeDetails = new ArrayList();
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement("SELECT DOC_TYPE_ID,DOC_TYPE_NAME,CASE WHEN status = 'A' THEN 'ACTIVE' ELSE 'INACTIVE' END AS status,SYSTEM_TYPE,PREFIX FROM DOC_TYPE");
			arrDocTypeDetails = DataBaseUtil.queryToArrayList(ps);
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return arrDocTypeDetails;
	}

	/*
	 * Returns all document type IDs and their names: useful for translating
	 */
	private static final String GET_DOC_TYPE_NAMES = " SELECT * FROM doc_type ";

	public static List<BasicDynaBean> getDocTypeNames() throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_DOC_TYPE_NAMES);
	}
	private static final String GET_DOC_TYPE_LIST = "SELECT doc_type_id, doc_type_name FROM doc_type order by doc_type_name";

	private static final String GET_PATIENT_SHAREABLE_DOC_TYPES = "SELECT doc_type_id FROM doc_type where isshareabletopatient = 'Y'";

	public static List getDocTypeList() throws SQLException{
		return ConversionUtils.listBeanToListMap(DataBaseUtil.queryToDynaList(GET_DOC_TYPE_LIST));
	}

	public static Map getPatientShareableDocTypes() throws SQLException{
		return ConversionUtils.listBeanToMapBean(DataBaseUtil.queryToDynaList(GET_PATIENT_SHAREABLE_DOC_TYPES), "doc_type_id");
	}
}
