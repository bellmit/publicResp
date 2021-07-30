package com.insta.hms.stores;

import au.com.bytecode.opencsv.CSVWriter;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DirectStockEntryDAO {

static Logger logger =LoggerFactory.getLogger(DirectStockEntryDAO.class);

/*
 * Constants used for sort order
 */
public static final int FIELD_NONE = 0;
public static final int FIELD_GRNNO = 1;
public static final int FIELD_SUPPLIER = 2;
public static final int FIELD_INVOICE = 3;
public static final int FIELD_GDATE = 4;
public static final int FIELD_STORE = 5;

public static final int FIELD_ISSUENO = 1;
public static final int FIELD_ISSUE_DATE = 2;

public static final int FIELD_RETURNNO = 1;
public static final int FIELD_RETURN_DATE = 2;

public static final int FIELD_ADJUSTNO = 1;
public static final int FIELD_ADJUST_DATE = 2;

	Connection con = null;
	public DirectStockEntryDAO(Connection con) {
		this.con = con;
	}

	public DirectStockEntryDAO () {}

	private static final String GET_MEDICINES_IN_MASTER = " SELECT DISTINCT pmd.medicine_name,pmd.medicine_id," +
		" m.manf_name,m.manf_code,m.manf_mnemonic,COALESCE(pmd.package_type,'') AS package_type, " +
		" COALESCE(g.generic_name,'') AS generic_name,g.generic_code,pmd.issue_base_unit, " +
		" COALESCE((SELECT cost_price FROM store_grn_details JOIN store_grn_main USING (grn_no) " +
			"WHERE medicine_id=pmd.medicine_id ORDER BY grn_date desc LIMIT 1),0) AS costprice, " +
		" COALESCE((SELECT mrp FROM store_stock_details " +
			"WHERE medicine_id=pmd.medicine_id ORDER BY exp_dt DESC LIMIT 1),0) AS mrp, " +
		" sic.control_type_name, mc.category, pmd.issue_units, m.status AS manf_status, pmd.status AS med_status, " +
		" mc.status AS cat_status, g.status AS gen_status,pmd.cust_item_code " +
		"FROM store_item_details pmd " +
		" RIGHT OUTER JOIN manf_master m ON pmd.manf_name=m.manf_code " +
		" RIGHT OUTER JOIN store_category_master mc ON pmd.med_category_id=mc.category_id" +
		" LEFT OUTER JOIN generic_name g ON pmd.generic_name=g.generic_code " +
		" LEFT JOIN store_item_controltype sic ON sic.control_type_id = pmd.control_type_id  ORDER BY medicine_name";

	public static ArrayList getMedicineNamesInMaster() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_MEDICINES_IN_MASTER);
			return DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_GENERICS_IN_MASTER =
		"SELECT DISTINCT GENERIC_NAME " +
		" FROM GENERIC_NAME where status='A' order by GENERIC_NAME";

	public static ArrayList getGenNamesInMaster() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_GENERICS_IN_MASTER);
			return DataBaseUtil.queryToArrayList1(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static String getGenericNames() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		ArrayList<String> GenericNames = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_GENERICS_IN_MASTER);
			JSONSerializer js = new JSONSerializer().exclude("class");
			GenericNames = DataBaseUtil.queryToArrayList1(ps);
			return js.serialize(GenericNames);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	private static final String GET_MANUFACTURES_IN_MASTER =
		"SELECT * " +
		" FROM MANF_MASTER  order by MANF_NAME";

	public static ArrayList getmanfNamesInMaster() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_MANUFACTURES_IN_MASTER);
			return DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_MED_CATAGEROY_IN_MASTER =
		"SELECT CATEGORY as CATEGORY_NAME " +
		" FROM STORE_CATEGORY_MASTER WHERE STATUS='A' ORDER BY CATEGORY";

	public static ArrayList getMedCategoryInMaster() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_MED_CATAGEROY_IN_MASTER);
			return DataBaseUtil.queryToArrayList1(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String MANUFACTURER_NAME_TO_ID =
		"SELECT manf_code FROM manf_master WHERE manf_name=?";

	public static String manfNameToId(String medicineName) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(MANUFACTURER_NAME_TO_ID);
			ps.setString(1, medicineName);

			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GENERIC_NAME_TO_ID =
		"SELECT generic_code FROM generic_name WHERE generic_name=?";

	public static String genericNameToId(String genericName) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GENERIC_NAME_TO_ID);
			ps.setString(1, genericName);

			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String MEDICINE_NAME_TO_ID =
		"SELECT medicine_id FROM store_item_details WHERE medicine_name=?";

	public static String medNameToId(String medName) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(MEDICINE_NAME_TO_ID);
			ps.setString(1, medName);

			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String SUPPLIER_NAME_TO_ID =
		"SELECT supplier_code FROM supplier_master WHERE supplier_name=?";

	public static String suppNameToId(String suppName) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(SUPPLIER_NAME_TO_ID);
			ps.setString(1, suppName);

			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}



	private static final String GET_BATCHNOS =
		"SELECT  DISTINCT BATCH_NO FROM store_stock_details WHERE MEDICINE_ID=?";

	public static ArrayList getMedicineId(String medId) throws SQLException {
		PreparedStatement ps = null;
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
		    ps = con.prepareStatement(GET_BATCHNOS);
		    ps.setString(1, medId);
		    return DataBaseUtil.queryToArrayList1(ps);
	    }finally {
		  DataBaseUtil.closeConnections(con, ps);
	    }
    }

	private static final String GET_SEL_MED_DET = "SELECT pmsd.medicine_id,pmsd.exp_dt,pmsd.mrp,COALESCE(pmd.package_type,'') AS package_type, "
		  +" pmsd.package_cp,m.manf_name,m.manf_code,m.manf_mnemonic,COALESCE(g.generic_name,'') AS generic_name,pmd.issue_base_unit, "
          +" COALESCE(isld.bin,pmd.bin)  as bin,sic.control_type_name,mc.category as category_name,pmd.tax_type, "
          +" (SELECT cost_price FROM store_grn_details"
          +" JOIN store_grn_main USING (grn_no) WHERE medicine_id=? and batch_no=? and debit_note_no is null "
          +" ORDER BY grn_date DESC LIMIT 1) AS originalcp, pmd.issue_units"
          +" FROM store_stock_details pmsd JOIN"
          +" store_item_details pmd USING(medicine_id)"
          +" LEFT JOIN item_store_level_details  isld ON isld.medicine_id = pmsd.medicine_id AND isld.dept_id = pmsd.dept_id "
          +" JOIN manf_master m ON pmd.manf_name=m.manf_code"
          +" JOIN store_categort_master mc ON pmd.med_category_id=mc.category_id"
          +" LEFT OUTER JOIN  generic_name g ON pmd.generic_name=g.generic_code"
          +" LEFT JOIN store_item_controltype sic ON sic.control_type_id = pmd.control_type_id"
          +" WHERE pmsd.medicine_id = ? AND pmsd.batch_no = ? LIMIT 1";

	public static ArrayList getSelMedicineDetails(String medId,String batchNo) throws SQLException {
		PreparedStatement ps = null;
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
		    ps = con.prepareStatement(GET_SEL_MED_DET);
		    ps.setString(1, medId);
		    ps.setString(2, batchNo);
		    ps.setString(3, medId);
		    ps.setString(4, batchNo);
		    return DataBaseUtil.queryToArrayList(ps);
	    } finally {
		  DataBaseUtil.closeConnections(con, ps);
	    }
    }

	private static final String INSERT_STOCK = "INSERT INTO store_stock_details "
		   +" (MEDICINE_ID,BATCH_NO,QTY,EXP_DT,MRP,TAX,RECEIVED_DATE,DEPT_ID,STOCK_TIME,PACKAGE_SP,PACKAGE_CP,USERNAME,CHANGE_SOURCE)"
		   + " VALUES(?,?,?,?,?,?,current_date,?,localtimestamp(0),?,?,?,?)";

	public boolean insertDirectStock(StockEntry dse) throws SQLException{
		PreparedStatement ps;
		ps = con.prepareStatement(INSERT_STOCK);
		ps.setString(1, dse.getHmedicineId());
		ps.setString(2,dse.getHbatchNo());
		ps.setFloat(3,dse.getHqty());
		ps.setDate(4,dse.getHexpiry());
		ps.setBigDecimal(5, dse.getHmrp());
		ps.setBigDecimal(6,dse.getHtax());
		ps.setString(7,dse.getDeptId());
		ps.setBigDecimal(8,dse.getHamrp());
		ps.setBigDecimal(9, dse.getHrate());
		ps.setString(10, dse.getUsername());
		ps.setString(11, "StockEntry");
        int count = ps.executeUpdate();
        ps.close();
		return count == 1;
	}

	public static boolean chkMed (String medId) throws SQLException{
		Connection con = null;
		boolean status = false;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			DirectStockEntryDAO dao = new DirectStockEntryDAO(con);
			status = dao.checkMedicineName(medId);
		}finally{
      if (con != null && !con.isClosed()) {
        con.close();
      }
		}
		return status;
	}

	private static final String CHECK_MEDICINE_NAME="SELECT medicine_id FROM store_stock_details WHERE medicine_id=?";

	public boolean checkMedicineName(String medicineID) throws SQLException {

		PreparedStatement ps=null;
		boolean target=true;
		String medicineStatus=null;
		try{
			ps = con.prepareStatement(CHECK_MEDICINE_NAME) ;
			ps.setString(1, medicineID);
			medicineStatus=DataBaseUtil.getStringValueFromDb(ps);
			if(medicineStatus==null)
				target=false;
		}finally{
			if(ps!=null)ps.close();
		}
		return target;
	}

	private static final  String CHECK_MEDICINE_NAME_IN_STORE="SELECT medicine_id FROM store_stock_details WHERE dept_id=? and medicine_id=?";

	public static boolean checkMedicineName(String medicineID,String store) throws SQLException {

		PreparedStatement ps=null;
		boolean target=true;
		String medicineStatus=null;
		Connection con = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(CHECK_MEDICINE_NAME_IN_STORE) ;
			ps.setString(1, store);
			ps.setString(2, medicineID);
			medicineStatus=DataBaseUtil.getStringValueFromDb(ps);
			if(medicineStatus==null)
				target=false;
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return target;
	}


	private static final String UPDATE_STOCK = "UPDATE store_stock_details SET QTY=coalesce(QTY,0)+?,PACKAGE_CP=?,TAX=?, "
		+" PACKAGE_SP=?,MRP=?,EXP_DT=?,USERNAME=?,CHANGE_SOURCE=? WHERE MEDICINE_ID=? AND BATCH_NO=? AND DEPT_ID=?";

	public boolean updateStock(StockEntry dse) throws SQLException{
		PreparedStatement ps;
		ps = con.prepareStatement(UPDATE_STOCK);
		ps.setFloat(1,dse.getHqty());
		ps.setBigDecimal(2,dse.getHrate());
		ps.setBigDecimal(3,dse.getHtax());
		ps.setBigDecimal(4, dse.getHamrp());
		ps.setBigDecimal(5, dse.getHmrp());
		ps.setDate(6, dse.getHexpiry());
		ps.setString(7, dse.getUsername());
		ps.setString(8,"StockEntry");
		ps.setString(9, dse.getHmedicineId());
		ps.setString(10,dse.getHbatchNo());
		ps.setString(11, dse.getDeptId());
		int count = ps.executeUpdate();
        ps.close();
        return count == 1;
	}


	/**
	 * 	Method to edit items of stock from GRN edit screen.
	 */

	private static final String EDIT_STOCK_FROM_GRN = "UPDATE store_stock_details SET PACKAGE_CP=?,TAX=?, "
		+" USERNAME=?,CHANGE_SOURCE=? WHERE MEDICINE_ID=? AND BATCH_NO=? AND DEPT_ID=?";
	public boolean editStockFromGrn(StockEntry dse) throws SQLException{
		PreparedStatement ps;
		ps = con.prepareStatement(EDIT_STOCK_FROM_GRN);

		ps.setBigDecimal(1,dse.getHrate());
		ps.setBigDecimal(2,dse.getHtax());
		ps.setString(3, dse.getUsername());
		ps.setString(4,"StockEntry");
		ps.setString(5, dse.getHmedicineId());
		ps.setString(6,dse.getHbatchNo());
		ps.setString(7, dse.getDeptId());
		int count = ps.executeUpdate();
        ps.close();
        return count == 1;
	}
	

	private static final String GET_ALL_ACTIVE_SUPPLIERS = "SELECT supplier_code, supplier_name,cust_supplier_code " +
			" FROM supplier_master where status='A' ORDER BY supplier_name";
	
	public static final String CENTER_SUPPLIER_DETAILS = "select sm.supplier_code,supplier_name,sm.cust_supplier_code,coalesce(scm.center_id,0) as center_id "
			+ "		from supplier_master sm  "
			+ "		left join supplier_center_master scm on(scm.supplier_code=sm.supplier_code) "
			+ " 	where scm.status='A' and sm.status='A' and scm.center_id IN(?,0) order by sm.supplier_name ";

	public static ArrayList getSuuplierNamesInMaster(Integer centerId) throws SQLException {
		Connection con = null;
		PreparedStatement pstmt = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			if (centerId != 0 && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) { 
				pstmt = con.prepareStatement(CENTER_SUPPLIER_DETAILS);
				pstmt.setInt(1, centerId);
			} else {
				pstmt = con.prepareStatement(GET_ALL_ACTIVE_SUPPLIERS);
			}
			return DataBaseUtil.queryToArrayList(pstmt);

		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}
	
	public static ArrayList getSuuplierNamesInMaster() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_ACTIVE_SUPPLIERS);
			return DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String INSERT_GRN_MAIN ="INSERT INTO store_grn_main(GRN_NO,GRN_DATE,SUPPLIER_INVOICE_ID,PO_NO,USER_NAME,store_id) "
		  +" VALUES(?,localtimestamp(0),?,?,?,?)";

	private static final String INSERT_GRN = "INSERT INTO store_grn_details "
		 +" (GRN_NO,MEDICINE_ID,BATCH_NO,EXP_DT,MRP,BILLED_QTY,BONUS_QTY,COST_PRICE,DISCOUNT,TAX_RATE,TAX,ADJ_MRP,TAX_TYPE, OUTGOING_TAX_RATE)"
		 +" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	public String insertGrnMain(StockEntry stockdto) throws SQLException {
		PreparedStatement ps=null;
		String grnNo = StockEntryDAO.getNextId(stockdto.getDeptId(),null);
		BigDecimal supplierInvoiceId = DirectStockEntryDAO.getSupplierInvoice(stockdto.getSupplier(), stockdto.getInvno());
		ps = con.prepareStatement(INSERT_GRN_MAIN);
		ps.setString(1, grnNo);
		ps.setBigDecimal(2,supplierInvoiceId);
		ps.setString(3, stockdto.getPonum());
		ps.setString(4, stockdto.getUsername());
		ps.setString(5, stockdto.getDeptId());
		int count = ps.executeUpdate();
		ps.close();
		if (count > 0) {
			return grnNo;
		}
		return null;
	}

	public boolean insertGrn(StockEntry stockdto, String vatORcst, BigDecimal cstRate) throws SQLException {
		PreparedStatement ps=null;
		ps = con.prepareStatement(INSERT_GRN);
		ps.setString(1, stockdto.getGrnNo());
		ps.setString(2, stockdto.getHmedicineId());
		ps.setString(3, stockdto.getHbatchNo());
		ps.setDate(4, stockdto.getHexpiry());
		ps.setBigDecimal(5, stockdto.getHmrp());
		ps.setFloat(6, stockdto.getHqty());
		ps.setFloat(7, stockdto.getBqty());
		ps.setBigDecimal(8, stockdto.getHrate());
		ps.setBigDecimal(9, stockdto.getHdisc());
		ps.setBigDecimal(10,  stockdto.getHtaxrate());
		ps.setBigDecimal(11, stockdto.getHtax());
		ps.setBigDecimal(12, stockdto.getHamrp());
		ps.setString(13, vatORcst.equals("CST") ? "C" :stockdto.getVatType());
		ps.setBigDecimal(14, vatORcst.equals("CST") ? cstRate : stockdto.getHtaxrate() );
		int count = ps.executeUpdate();
		ps.close();
		return count > 0;
	}

	/**
	 * Method to edit existing items of a GRN
	 *
	 */

	private static final String UPDATE_GRN = "UPDATE store_grn_details "
		 +" SET cost_price = ?,discount = ?,tax_rate = ?,tax = ?, tax_type = ?, outgoing_tax_rate = ?,EXP_DT = ?"
		 +" WHERE grn_no = ? AND MEDICINE_ID = ? AND batch_no = ?";

	public boolean updateGrn(StockEntry stockdto, String vatORcst, BigDecimal cstRate) throws SQLException {
		PreparedStatement ps=null;
		ps = con.prepareStatement(UPDATE_GRN);
		ps.setBigDecimal(1, stockdto.getHrate());
		ps.setBigDecimal(2, stockdto.getHdisc());
		ps.setBigDecimal(3, stockdto.getHtaxrate());
		ps.setBigDecimal(4, stockdto.getHtax());
		ps.setString(5, vatORcst.equals("CST") ? "C" :stockdto.getVatType() );
		ps.setBigDecimal(6, vatORcst.equals("CST") ? cstRate : stockdto.getHtaxrate());
		ps.setDate(7, stockdto.getHexpiry());
		ps.setString(8, stockdto.getGrnNo());
		ps.setString(9, stockdto.getHmedicineId());
		ps.setString(10, stockdto.getHbatchNo());
		int count = ps.executeUpdate();
		ps.close();
		return count > 0;
	}

	public static final String UPDATE_GRN_PAYMENTS = "UPDATE store_invoice set payment_id =? where invoice_no=? AND  supplier_id = ?  AND status='F' AND invoice_date = ? ";


	public static final String UPDATE_STORE_CONSINGMENT =
	" UPDATE store_consignment_invoice "+
	" SET payment_id =? , "+
	" amount_payable=(SELECT amount from payments_details where payment_id=?) "+
	" WHERE (grn_no, issue_id) IN ( "+
	"  SELECT grn_no, issue_id FROM 	 "+
	" store_consignment_invoice "+
	" ci  "+
	"  JOIN store_invoice si ON si.supplier_invoice_id::text= ci.supplier_invoice_id::text "+
	"  where issue_id=? AND invoice_no=? AND si.supplier_id = ? ) ";

	public static final String UPDATE_STORE_CONSINGMENT_DELETE =
		" UPDATE store_consignment_invoice "+
		" set payment_id = ?, amount_payable= 0.00 "+
		" WHERE (grn_no, issue_id) IN ( "+
		"  SELECT grn_no, issue_id FROM 	 "+
		" store_consignment_invoice "+
		" ci  "+
		"  JOIN store_invoice si ON si.supplier_invoice_id::text= ci.supplier_invoice_id::text "+
		"  where issue_id=? AND invoice_no=? AND si.supplier_id = ? ) ";


	public static final String UPDATE_STORE_DEBIT_NOTE = "UPDATE store_debit_note set payment_id = ? where debit_note_no=? AND  supplier_id = ? ";

	public  boolean updateGrnPaymentId(Connection con,ArrayList<StockEntry> stockDTO)throws SQLException{
		boolean target=false;
		if (stockDTO.isEmpty()){
			return true;
		}else{
			PreparedStatement ps = null;
			Iterator<StockEntry> it = stockDTO.iterator();
			while(it.hasNext()){
				int i =1;
				StockEntry stockDto = it.next();
				if (stockDto.getInvoiceType().equals("P")){
					if (stockDto.getConsignmentStatus().equals("F")){
						ps = con.prepareStatement(UPDATE_GRN_PAYMENTS);
						ps.setString(1,stockDto.getPaymentId());
						ps.setString(2,stockDto.getGrnNo());
						ps.setString(3, stockDto.getSupplier());
						ps.setDate(4, stockDto.getInvoiceDate());
					}else if (stockDto.getConsignmentStatus().equals("O")){
						if (stockDto.getPaymentId() == null){
							ps = con.prepareStatement(UPDATE_STORE_CONSINGMENT_DELETE);
							ps.setString(1, stockDto.getPaymentId());
							ps.setInt(2, stockDto.getIssueId());
							ps.setString(3, stockDto.getGrnNo());
							ps.setString(4 ,stockDto.getSupplier());
						}else{
							ps = con.prepareStatement(UPDATE_STORE_CONSINGMENT);
							ps.setString(1, stockDto.getPaymentId());
							ps.setString(2, stockDto.getPaymentId());
							ps.setInt(3, stockDto.getIssueId());
							ps.setString(4, stockDto.getGrnNo());
							ps.setString(5, stockDto.getSupplier());
						}
					}
				}else if (stockDto.getInvoiceType().equals("PD")){
					ps = con.prepareStatement(UPDATE_STORE_DEBIT_NOTE);
					ps.setString(1, stockDto.getPaymentId());
					ps.setString(2, stockDto.getGrnNo());
					ps.setString(3, stockDto.getSupplier());
				}else{
				}
			}
			int update = ps.executeUpdate();
			if(update >0) {
				target=true;
			}
			return target;
		}//end else
	}//end method


/**
 * for medicine details pop-up
 */
	private static final String GET_MED_DETAILS = "SELECT pngm.grn_no,TO_CHAR(pngm.grn_date,'YYYY-MM-DD') AS grn_date, "
			 + " COALESCE(pngm.po_no,'') AS po_no,COALESCE(pinv.invoice_no,'') AS invoice_no,"
			 + " pinv.invoice_date,sm.supplier_name,sm.cust_supplier_code,"
			 + " pmd.medicine_name,png.mrp,png.cost_price,png.tax_rate,pmd.cust_item_code "
			 + " FROM store_grn_main pngm JOIN store_grn_details png ON pngm.grn_no = png.grn_no"
			 + " JOIN store_item_details pmd ON pmd.medicine_id=png.medicine_id"
			 + " LEFT JOIN store_invoice pinv ON pinv.supplier_invoice_id = pngm.supplier_invoice_id "
			 + " JOIN supplier_master sm ON sm.supplier_code=pinv.supplier_id"
			 + " WHERE png.medicine_id=? AND png.batch_no=?  ";


	public static ArrayList getMedDetails(String medId,String batchNo)throws SQLException{

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs =  null;
		ArrayList medList = new ArrayList();
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_MED_DETAILS);
			ps.setString(1, medId);
			ps.setString(2, batchNo);
			rs = ps.executeQuery();
			StockEntry med = null;
			while(rs.next()){
				med = new StockEntry();
				med.setGrnNo(rs.getString("grn_no"));
				med.setGrnDate(rs.getDate("grn_date"));
				med.setPonum(rs.getString("po_no"));
				med.setInvno(rs.getString("invoice_no"));
				med.setInvoiceDate(rs.getDate("invoice_date"));
				med.setSupplier(rs.getString("supplier_name"));
				med.setHmedicineName(rs.getString("medicine_name"));
				med.setHmrp(rs.getBigDecimal("mrp"));
				med.setHrate(rs.getBigDecimal("cost_price"));
				med.setHtaxrate(rs.getBigDecimal("tax_rate"));
				medList.add(med);
			}
		}finally{
			DataBaseUtil.closeConnections(con, ps, rs);
		}
		return medList;
	}

	private static final String GET_PO_MED ="SELECT PO.MEDICINE_ID,PMD.MEDICINE_NAME,COALESCE(G.GENERIC_NAME,'') AS GENERIC_NAME,"
			+ " M.MANF_NAME,M.MANF_CODE,M.MANF_MNEMONIC,PO.VAT_RATE,PMD.ISSUE_BASE_UNIT,"
			+ " sic.control_type_name,COALESCE(PMD.PACKAGE_TYPE,'') "
			+ " AS PACKAGE_TYPE,PO.MRP,PO.ADJ_MRP,PO.COST_PRICE,(PO.QTY_REQ-PO.QTY_RECEIVED)AS QTY_REQ,PO.DISCOUNT,PO.VAT,PO.MED_TOTAL, "
			+ " CASE WHEN POM.VAT_TYPE='M' THEN 'MRP Based(with bonus)' ELSE 'Cost Price Based(without bonus)' END AS VATSTATUS,MC.CATEGORY AS CATEGORY_NAME,mc.claimable,PMD.CUST_ITEM_CODE "
			+ " FROM store_po PO JOIN store_item_details PMD USING(MEDICINE_ID) "
			+ " JOIN MANF_MASTER M ON PMD.MANF_NAME=M.MANF_CODE "
			+ " JOIN STORE_CATEGORY_MASTER MC ON PMD.MED_CATEGORY_ID=MC.CATEGORY_ID"
			+ " LEFT OUTER JOIN  GENERIC_NAME G ON PMD.GENERIC_NAME=G.GENERIC_CODE "
			+ " JOIN store_po_main POM USING(PO_NO) WHERE PO_NO=?"
			+ " LEFT JOIN store_item_controltype sic ON (sic.control_type_id = PMD.control_type_id)";

	public static ArrayList getPOMedicines(String poNo) throws SQLException{

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs =  null;
		ArrayList poMedList = new ArrayList();
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_PO_MED);
			ps.setString(1, poNo);
			rs = ps.executeQuery();
			StockEntry poMed = null;

			while(rs.next()){
				poMed = new StockEntry();
				poMed.setHmedicineId(rs.getString("medicine_id"));
				poMed.setHmedicineName(rs.getString("medicine_name"));
				poMed.setHgenName(rs.getString("generic_name"));
				poMed.setHmanufacturer(rs.getString("manf_name"));
				poMed.setHmanufCode(rs.getString("manf_mnemonic"));
				poMed.setVatrate(rs.getBigDecimal("vat_rate"));
				poMed.setHpackUnit(rs.getFloat("issue_base_unit"));
				poMed.setControlTypeName(rs.getString("control_type_name"));
				poMed.setHpackType(rs.getString("package_type"));
				poMed.setHmrp(rs.getBigDecimal("mrp"));
				poMed.setHamrp(rs.getBigDecimal("adj_mrp"));
				poMed.setHrate(rs.getBigDecimal("cost_price"));
				poMed.setHqty(rs.getFloat("qty_req"));
				poMed.setHdisc(rs.getBigDecimal("discount"));
				poMed.setHtax(rs.getBigDecimal("vat"));
				poMed.setHamt(rs.getBigDecimal("med_total"));
				poMed.setVatType(rs.getString("VATSTATUS"));
				poMed.setHmedcatname(rs.getString("category_name"));
				poMed.setClaimable(rs.getBoolean("claimable"));
				poMedList.add(poMed);
			}
		}finally{
		DataBaseUtil.closeConnections(con, ps, rs);
		}
		return poMedList;
	}

	private static final String GETPONOS =
		"SELECT PO_NO FROM store_po_main WHERE STATUS='O' ORDER BY PO_NO";

	public static ArrayList getpoNos() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GETPONOS);
			return DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String INSERT_INVOICE = "INSERT INTO store_invoice (supplier_id,invoice_no,invoice_date," +
			"due_date,po_no,po_reference, discount,round_off,status,discount_type,discount_per,cess_tax_rate,tax_name, cst_rate) " +
			"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public static boolean insertInvoice (StockEntry sdto, String vatORcst, String cstrate) throws SQLException{


    	Connection con = null;
    	PreparedStatement ps = null;
    	try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(INSERT_INVOICE);
			ps.setString(1, sdto.getSuppCode());
			ps.setString(2, sdto.getInvno());
			ps.setDate(3, sdto.getInvoiceDate());
			ps.setDate(4, sdto.getDueDate());
			ps.setString(5, sdto.getPonum());
			ps.setString(6, sdto.getReference());
			ps.setBigDecimal(7, sdto.getDisc());
			ps.setBigDecimal(8, sdto.getRoff());
			ps.setString(9, "O");
			ps.setString(10, sdto.getInvDisc());
			ps.setBigDecimal(11, sdto.getDiscPer());
			ps.setBigDecimal(12, sdto.getCessTaxRate());

			ps.setString(13, vatORcst);
			ps.setBigDecimal(14, new BigDecimal(cstrate));


			int count = ps.executeUpdate();
			if (count > 0) {
				return true;
			}
    	}finally{
    		DataBaseUtil.closeConnections(con, ps);
    	}
		return false;
	}

	private static final String GET_SUPP_WITH_INV = "SELECT sm.supplier_code,sm.supplier_name,sm.cust_supplier_code," +
			"TO_CHAR(pinv.invoice_date,'DD-MM-YYYY') AS invoice_date, pinv.po_reference,pinv.invoice_no," +
			"TO_CHAR(pinv.due_date,'DD-MM-YYYY') AS due_date, pinv.po_no,pinv.supplier_id,pinv.discount," +
			"pinv.round_off,pinv.status, pinv.discount_per,pinv.discount_type,credit_period, " +
			"pinv.cess_tax_rate, pinv.tax_name, pinv.cst_rate " +
			"FROM supplier_master sm  LEFT OUTER JOIN store_invoice pinv ON sm.supplier_code=supplier_id " +
			"WHERE SM.STATUS='A' AND pharmacy";

	public static ArrayList getSuppAndInvNo() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_SUPP_WITH_INV);
			return DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

   private static final String CHECK_SUPP_INV = "SELECT COUNT(*) FROM  store_invoice WHERE SUPPLIER_ID=? AND INVOICE_NO=?";

	public static int checkSuppInv(String suppId,String invNo) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int count = 0;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(CHECK_SUPP_INV);
			ps.setString(1, suppId);
			ps.setString(2, invNo);
			rs = ps.executeQuery();
			if (rs.next()){
				count = rs.getInt(1);
			}
		}finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
		return count;
	}

	private static final String GET_INV_DETAILS =
		" SELECT pinv.po_reference, pinv.invoice_no, to_char(pinv.due_date,'dd-mm-yyyy') as due_date, " +
		"  pinv.po_no, pinv.supplier_id, pinv.discount, pinv.round_off, pinv.status, pinv.discount_type, " +
		"  pinv.discount_per, to_char(pinv.invoice_date,'dd-mm-yyyy') AS invoice_date, sm.supplier_name, sm.cust_supplier_code, " +
		"  pinv.other_charges, pinv.other_charges_remarks, pinv.remarks, " +
		"  to_char(pinv.paid_date,'dd-mm-yyyy') AS paid_date, pinv.payment_remarks, pinv.cess_tax_rate " +
		" FROM store_invoice pinv " +
		"  JOIN supplier_master sm ON sm.supplier_code=supplier_id " +
		" WHERE invoice_no=? and pinv.supplier_id=?";

	public static  StockEntry getInvDetails (String invNo, String suppId) throws SQLException{
		StockEntry sdto = null;
		PreparedStatement ps = null;
		Connection con = null;
		ResultSet rs = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_INV_DETAILS);
			ps.setString(1, invNo);
			ps.setString(2, suppId);
			rs = ps.executeQuery();

			if (rs.next()){
				sdto = new StockEntry();
				sdto.setSupplier(rs.getString("supplier_name"));
				sdto.setInvno(rs.getString("invoice_no"));
				sdto.setInvDate(rs.getString("invoice_date"));
				sdto.setInvdueDate(rs.getString("due_date"));
				sdto.setPonum(rs.getString("po_no"));
				sdto.setReference(rs.getString("po_reference"));
				sdto.setDisc(rs.getBigDecimal("discount"));
				sdto.setRoff(rs.getBigDecimal("round_off"));
				sdto.setStatus(rs.getString("status"));
				sdto.setSuppCode(rs.getString("supplier_id"));
				sdto.setInvDisc(rs.getString("discount_type"));
				sdto.setDiscPer(rs.getBigDecimal("discount_per"));
				sdto.setOtherCharges(rs.getBigDecimal("other_charges"));
				sdto.setOtherDescription(rs.getString("other_charges_remarks"));
				sdto.setRemarks(rs.getString("remarks"));
				sdto.setStrPaidDate(rs.getString("paid_date"));
				sdto.setPaymentRemarks(rs.getString("payment_remarks"));
				sdto.setCessTaxRate(rs.getBigDecimal("cess_tax_rate"));
			}
			return sdto;
		}finally{
			DataBaseUtil.closeConnections(con, ps, rs);
		}
	}

	private static final String UPDATE_INVOICE =
		" UPDATE store_invoice SET supplier_id=?,invoice_no=?, invoice_date=?, due_date=?, po_no=?, po_reference=?, " +
		"  discount=?, round_off=?, status=?, date_time=current_timestamp, " +
		"  other_charges=?, other_charges_remarks=?, remarks=?, paid_date=?, payment_remarks=?, " +
		"  discount_type=?, discount_per=?, cess_tax_rate = ?, cess_tax_amt = ? " +
		" WHERE invoice_no=? AND supplier_id=?";

	public  String updateInvoice(StockEntry sdto) throws SQLException {
		PreparedStatement ps = null;
		Connection con = null;
		int count = 0;
		try {
			con = DataBaseUtil.getConnection();
			if ( sdto.getOldSupplier().equals( sdto.getSuppCode() ) && ( sdto.getOldInvoiceNo().equals( sdto.getInvno() ) ) ){
				count = editInvoice(con, sdto, sdto.getSuppCode(), sdto.getSuppCode(), sdto.getInvno(), sdto.getInvno());
			} else {
				ps = con.prepareStatement("SELECT COUNT(*) FROM store_invoice where invoice_no = ? AND supplier_id = ?");
				ps.setString(1, sdto.getInvno());
				ps.setString(2, sdto.getSuppCode());
				int res = DataBaseUtil.getIntValueFromDb(ps);
				DataBaseUtil.closeConnections(null, ps);
				if (res == 0){
					count = editInvoice(con,  sdto, sdto.getSuppCode(), sdto.getOldSupplier(), sdto.getInvno(), sdto.getOldInvoiceNo());
				} else {
					return "DuplicateInvoice";
				}
			}
			if (count > 0)
				return "success";
		    return "failure";
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private int editInvoice( Connection con, StockEntry sdto, String newSupp, String oldSupp, String newInv, String oldInv) throws SQLException{
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(UPDATE_INVOICE);
			ps.setString(1, newSupp);
			ps.setString(2, newInv);

			ps.setDate(3, sdto.getInvoiceDate());
			ps.setDate(4, sdto.getDueDate());
			ps.setString(5, sdto.getPonum());
			ps.setString(6, sdto.getReference());
			ps.setBigDecimal(7, sdto.getDisc());
			ps.setBigDecimal(8, sdto.getRoff());
			ps.setString(9, sdto.getStatus());
			ps.setBigDecimal(10, sdto.getOtherCharges());
			ps.setString(11, sdto.getOtherDescription());
			ps.setString(12, sdto.getRemarks());
			ps.setDate(13, sdto.getPaidDate());
			ps.setString(14, sdto.getPaymentRemarks());
			ps.setString(15, sdto.getInvDisc());
			ps.setBigDecimal(16, sdto.getDiscPer());
			ps.setBigDecimal(17, sdto.getCessTaxRate());
			ps.setBigDecimal(18, sdto.getCessAmt());

			ps.setString(19, sdto.getOldInvoiceNo());
			ps.setString(20, oldSupp);
			int count = ps.executeUpdate();
			return count;
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String GET_BATCHES = "SELECT BATCH_NO FROM store_stock_details WHERE MEDICINE_ID=? AND DEPT_ID=?";

	public static String[] getBatches (String medId, String store) throws SQLException{
		int i=0;
		String centralId = store;
		String countquery = "select count(*) from store_stock_details where medicine_id=? and dept_id=?";
		int count = DataBaseUtil.getIntValueFromDb(countquery, new Object[] {medId, centralId });
		String[] batchs = new String[count];
		PreparedStatement ps = null;
		Connection con = null;
		ResultSet rs = null;

		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_BATCHES);
            ps.setString(1, medId);
            ps.setString(2,centralId);
            rs = ps.executeQuery();

            while (rs.next()) {
            	batchs[i] = rs.getString("batch_no");
            	i++;
            }
		    return batchs;
	   }finally{
			DataBaseUtil.closeConnections(con, ps, rs);
		}
	}

	private static final String GET_GRNS_WITH_INV = "SELECT TO_CHAR(PNGM.GRN_DATE,'DD-MM-YYYY') AS GRNDATE,PNGM.GRN_NO, "
		                    + " SUM(((PNG.BILLED_QTY * PNG.COST_PRICE) - PNG.DISCOUNT + PNG.TAX)) AS AMT," +
		                    		"COUNT(png.grn_no) AS total_items,SUM(png.billed_qty) AS total_qty, " +
		                    		"SUM((png.billed_qty * png.cost_price) ) AS tot_amt, SUM(png.discount) AS discount ,sum(png.tax) AS tax"
			                + " FROM store_grn_main PNGM JOIN store_grn_details PNG USING(GRN_NO)"
			                + " WHERE pngm.supplier_invoice_id = ? GROUP BY PNGM.GRN_NO,PNGM.GRN_DATE,debit_note_no";

	public static ArrayList getGrnsAccInvNo(BigDecimal supplierInvoiceId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_GRNS_WITH_INV);
			ps.setBigDecimal(1, supplierInvoiceId);
		//	ps.setString(2, suppCode);
			return DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	private static final String GET_INV_AMOUNTS = "SELECT coalesce(other_charges,0) AS other_charges," +
			"SUM(((png.billed_qty * png.cost_price) - png.discount + png.tax)) AS other_grns_amt, " +
			"SUM(png.tax) AS vat_amt, cess_tax_rate " +
			"FROM store_grn_main pngm JOIN store_grn_details png USING(grn_no) " +
        	"JOIN store_invoice pinv on pinv.supplier_invoice_id=pngm.supplier_invoice_id " +
        	"WHERE pngm.supplier_invoice_id=? " +
        	"GROUP BY pinv.supplier_id, pinv.invoice_no, other_charges, cess_tax_rate";

	public static List<BasicDynaBean> getInvoiceGrnsAmounts(BigDecimal supplierInvoiceId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
			try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_INV_AMOUNTS);
			ps.setBigDecimal(1, supplierInvoiceId);
			return DataBaseUtil.queryToDynaList(ps);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
	}

private static final String GET_GRN_DET="SELECT sm.supplier_name,sm.cust_supplier_code,pinv.supplier_id, pinv.round_off,pinv.invoice_no," +
		"TO_CHAR(pinv.due_date,'DD-MM-YYYY') AS due_date, TO_CHAR(pinv.invoice_date,'DD-MM-YYYY') AS invoice_date," +
		"pinv.discount,pngm.grn_no,pinv.discount_type,pinv.discount_per, pngm.store_id, pinv.cess_tax_rate, " +
		"pinv.tax_name, pinv.cst_rate " +
		" FROM  store_grn_main pngm JOIN store_invoice pinv USING ( supplier_invoice_id ) " +
		"JOIN supplier_master sm ON pinv.supplier_id=supplier_code WHERE grn_no = ?";

public static  StockEntry getGrnDetails (String grnNo) throws SQLException{
	StockEntry sdto = null;
	PreparedStatement ps = null;
	Connection con = null;
	ResultSet rs = null;
	try{
		con = DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(GET_GRN_DET);
		ps.setString(1, grnNo);
		rs = ps.executeQuery();

		if (rs.next()){
			sdto = new StockEntry();
			sdto.setSupplier(rs.getString("supplier_name"));
			sdto.setInvno(rs.getString("invoice_no"));
			sdto.setInvDate(rs.getString("invoice_date"));
			sdto.setInvdueDate(rs.getString("due_date"));
			sdto.setGrnNo(rs.getString("grn_no"));
			sdto.setDisc(rs.getBigDecimal("discount"));
			sdto.setRoff(rs.getBigDecimal("round_off"));
			sdto.setSuppCode(rs.getString("supplier_id"));
			sdto.setInvDisc(rs.getString("discount_type"));
			sdto.setDiscPer(rs.getBigDecimal("discount_per"));
			sdto.setDeptId(rs.getString("store_id"));
			sdto.setCessTaxRate(rs.getBigDecimal("cess_tax_rate"));
			sdto.setTaxName(rs.getString("tax_name"));
			sdto.setCstRate(rs.getBigDecimal("cst_rate"));
		}
		return sdto;
	}finally{
		DataBaseUtil.closeConnections(con, ps, rs);
	}
}

private static final String GETGRNNOS = "SELECT GRN_NO FROM store_grn_main  ORDER BY GRN_NO";

public static ArrayList getgrnNos() throws SQLException {
	Connection con = null;
	PreparedStatement ps = null;
	try {
		con = DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(GETGRNNOS);
		return DataBaseUtil.queryToArrayList(ps);
	} finally {
		DataBaseUtil.closeConnections(con, ps);
	}
}

private static final String GET_GRN_MED ="SELECT PNG.MEDICINE_ID,PMD.MEDICINE_NAME,COALESCE(G.GENERIC_NAME,'') AS GENERIC_NAME,"
	+ " M.MANF_NAME,M.MANF_CODE,M.MANF_MNEMONIC,PNG.TAX_RATE,PMD.ISSUE_BASE_UNIT,PNG.BATCH_NO,PNG.TAX_TYPE,"
	+ " sic.control_type_name,COALESCE(PMD.PACKAGE_TYPE,'') "
	+ " AS PACKAGE_TYPE,PNG.MRP,PNG.ADJ_MRP,PNG.COST_PRICE,(PNG.BILLED_QTY*PNG.COST_PRICE-PNG.DISCOUNT+PNG.TAX)AS MED_TOTAL, "
	+ " TO_CHAR(PNG.EXP_DT,'MM') AS MONTH,TO_CHAR(PNG.EXP_DT,'YY') AS YEAR,PNG.BILLED_QTY,PNG.DISCOUNT,PNG.TAX,PNG.BONUS_QTY,MC.CATEGORY AS CATEGORY_NAME,mc.claimable, PMD.CUST_ITEM_CODE "
	+ " FROM store_grn_details PNG JOIN store_item_details PMD USING(MEDICINE_ID) "
	+ " JOIN MANF_MASTER M ON PMD.MANF_NAME=M.MANF_CODE "
	+ " JOIN STORE_CATEGORY_MASTER MC ON PMD.MED_CATEGORY_ID=MC.CATEGORY_ID"
	+ " LEFT OUTER JOIN  GENERIC_NAME G ON PMD.GENERIC_NAME=G.GENERIC_CODE "
	+ " JOIN store_grn_main PNGM USING(GRN_NO) "
	+ " LEFT JOIN store_item_controltype sic ON (sic.control_type_id = pmd.control_type_id) WHERE GRN_NO=?";

public static ArrayList getGRNMedicines(String poNo) throws SQLException{
	Connection con = DataBaseUtil.getReadOnlyConnection();
	PreparedStatement ps = con.prepareStatement(GET_GRN_MED);
	ps.setString(1, poNo);
	ResultSet rs = ps.executeQuery();
	StockEntry grnMed = null;
	ArrayList grnMedList = new ArrayList();

	while(rs.next()){
		grnMed = new StockEntry();
		grnMed.setHmedicineId(rs.getString("medicine_id"));
		grnMed.setHmedicineName(rs.getString("medicine_name"));
		grnMed.setHgenName(rs.getString("generic_name"));
		grnMed.setHmanufacturer(rs.getString("manf_name"));
		grnMed.setHmanufCode(rs.getString("manf_mnemonic"));
		grnMed.setVatrate(rs.getBigDecimal("tax_rate"));
		grnMed.setHpackUnit(rs.getFloat("issue_base_unit"));
		grnMed.setControlTypeName(rs.getString("control_type_name"));
		grnMed.setHpackType(rs.getString("package_type"));
		grnMed.setHmrp(rs.getBigDecimal("mrp"));
		grnMed.setHamrp(rs.getBigDecimal("adj_mrp"));
		grnMed.setHrate(rs.getBigDecimal("cost_price"));
		grnMed.setHqty(rs.getFloat("billed_qty"));
		grnMed.setHdisc(rs.getBigDecimal("discount"));
		grnMed.setHtax(rs.getBigDecimal("tax"));
		grnMed.setHamt(rs.getBigDecimal("med_total"));
		grnMed.setMon(rs.getString("month"));
		grnMed.setYear(rs.getString("year"));
		grnMed.setBatchNo(rs.getString("batch_no"));
		grnMed.setBonus(rs.getString("bonus_qty"));
		grnMed.setVatType(rs.getString("tax_type"));
		grnMed.setHmedcatname(rs.getString("category_name"));
		grnMed.setClaimable(rs.getBoolean("claimable"));
		grnMedList.add(grnMed);

    }
	rs.close();
	ps.close();
	if (con!= null) con.close();
	return grnMedList;
}



private static final String GET_DEPARTMENTS = "SELECT DEPT_ID,DEPT_NAME FROM stores";

public static ArrayList getDeptNames() throws SQLException {
	Connection con = null;
	PreparedStatement ps = null;
	try {
		con = DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(GET_DEPARTMENTS);
		return DataBaseUtil.queryToArrayList(ps);
	} finally {
		DataBaseUtil.closeConnections(con, ps);
	}
}

private static final String GET_ACTIVE_DEPARTMENTS =
" SELECT d.dept_id, d.dept_name,d.auto_fill_prescriptions, c.counter_id, c.counter_no, d.account_group, " +
" 	d.sale_unit, d.allowed_raise_bill, d.is_sales_store,d.auto_fill_indents, d.use_batch_mrp,"+
"   d.center_id " +
" FROM stores d " +
" 	LEFT JOIN counters c ON (c.counter_id = d.counter_id AND c.center_id = d.center_id) " +
" 	WHERE d.status='A' AND d.center_id = ?";

public static ArrayList getActiveDeptNames(int centerId) throws SQLException {
	Connection con = null;
	PreparedStatement ps = null;
	try {
		con = DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(GET_ACTIVE_DEPARTMENTS);
		ps.setInt(1, centerId);
		return DataBaseUtil.queryToArrayList(ps);
	} finally {
		DataBaseUtil.closeConnections(con, ps);
	}
}

private static final String[] QUERY_FIELD_NAMES =
{ "", "GRN_NO", "SM.SUPPLIER_NAME", "INVOICE_NO", "GRN_DATE"};

private static final String GRN_EXT_QUERY_FIELDS = "SELECT GRN_NO,SM.SUPPLIER_NAME,SM.CUST_SUPPLIER_CODE,GRN_DATE,PI.INVOICE_NO,PI.STATUS,GD.DEPT_NAME";

private static final String GRN_EXT_QUERY_COUNT =
	" SELECT count(GRN_NO) ";

private static final String GRN_EXT_QUERY_INITWHERE =
	" where debit_note_no is null";

private static final String GRN_EXT_QUERY_TABLES =
	" FROM store_grn_main PNGM "
	+" JOIN store_invoice PI USING(SUPPLIER_INVOICE_ID) JOIN SUPPLIER_MASTER SM ON SM.SUPPLIER_CODE=PI.SUPPLIER_ID JOIN stores GD ON (GD.DEPT_ID=PNGM.STORE_ID)";


public static PagedList searchGRNS(String grnno,String suppId,String invNo,
		java.sql.Date fromDate, java.sql.Date toDate,
		int sortOrder, boolean sortReverse, int pageSize, int pageNum)
	throws SQLException {

	Connection con = DataBaseUtil.getReadOnlyConnection();
	String sortField = null;
	if ( (sortOrder != FIELD_NONE) && (sortOrder < QUERY_FIELD_NAMES.length) ) {
		sortField = QUERY_FIELD_NAMES[sortOrder];
	}

	SearchQueryBuilder qb = new SearchQueryBuilder(con,
			GRN_EXT_QUERY_FIELDS, GRN_EXT_QUERY_COUNT, GRN_EXT_QUERY_TABLES,GRN_EXT_QUERY_INITWHERE,
			sortField, sortReverse, pageSize, pageNum);

	qb.addFilter(qb.STRING, "GRN_NO", "=", grnno);
	if (!suppId.equalsIgnoreCase("All")) qb.addFilter(qb.STRING, "SUPPLIER_ID", "=", suppId);
	qb.addFilter(qb.STRING, "INVOICE_NO", "ILIKE", invNo);
	qb.addFilter(qb.DATE,   "date_trunc('day', GRN_DATE)", ">=", fromDate);
	qb.addFilter(qb.DATE,   "date_trunc('day', GRN_DATE)", "<=", toDate);

	qb.build();

	PreparedStatement psData = qb.getDataStatement();
	PreparedStatement psCount = qb.getCountStatement();

	ResultSet rsData = psData.executeQuery();

	ArrayList list = new ArrayList();
	while (rsData.next()) {
		ViewGRNDTO grn = new ViewGRNDTO();
		populateBill(grn, rsData);
		list.add(grn);
	}
	rsData.close();

	int totalCount = 0;
	ResultSet rsCount = psCount.executeQuery();
	if (rsCount.next()) {
		totalCount = rsCount.getInt(1);
	}
	rsCount.close();

	qb.close();
	con.close();

	return new PagedList(list, totalCount, pageSize, pageNum);
}

private static void populateBill(ViewGRNDTO grn, ResultSet rs) throws SQLException {
	grn.setGrnNo(rs.getString("grn_no"));
	grn.setSuppname(rs.getString("supplier_name"));
	grn.setInvNo(rs.getString("invoice_no"));
	grn.setGdate(rs.getDate("grn_date"));
	grn.setStoreName(rs.getString("dept_name"));
	grn.setStatus(rs.getString("status"));
}

private static final String[] QUERY_FIELD_NAMES1 =
	{ "", "ISSUE_NO", "DATE_TIME"};

private static final String ISSUE_EXT_QUERY_FIELDS =
	" SELECT ISSUE_NO,TO_CHAR(DATE_TIME,'DD-MM-YYYY HH:MI:SS') AS ISSUEDATE,"
	+ " GDF.DEPT_NAME AS FROMSTORE,GDT.DEPT_NAME AS TOSTORE,USERNAME";

private static final String ISSUE_EXT_QUERY_COUNT =
	" SELECT count(ISSUE_NO) ";

private static final String ISSUE_EXT_QUERY_TABLES =
	" FROM stock_issue_main PSIM JOIN  stores GDF ON "
	+ " PSIM.STORE_FROM= GDF.DEPT_ID ";

public static PagedList searchStockIssues(int issNo,String fStore,String tStore,
		java.sql.Date fromDate, java.sql.Date toDate,
		int sortOrder, boolean sortReverse, int pageSize, int pageNum)
	throws SQLException {

	Connection con = DataBaseUtil.getReadOnlyConnection();
	String sortField = null;
	if ( (sortOrder != FIELD_NONE) && (sortOrder < QUERY_FIELD_NAMES1.length) ) {
		sortField = QUERY_FIELD_NAMES1[sortOrder];
	}

	SearchQueryBuilder qb = new SearchQueryBuilder(con,
			ISSUE_EXT_QUERY_FIELDS, ISSUE_EXT_QUERY_COUNT, ISSUE_EXT_QUERY_TABLES, null,
			sortField, sortReverse, pageSize, pageNum);

    if (issNo > 0) qb.addFilter(qb.INTEGER, "ISSUE_NO", "=", issNo);
	qb.addFilter(qb.STRING, "STORE_FROM", "=", fStore);
	qb.addFilter(qb.STRING, "STORE_TO", "=", tStore);
	qb.addFilter(qb.DATE,   "date_trunc('day', DATE_TIME)", ">=", fromDate);
	qb.addFilter(qb.DATE,   "date_trunc('day', DATE_TIME)", "<=", toDate);

	qb.build();

	PreparedStatement psData = qb.getDataStatement();
	PreparedStatement psCount = qb.getCountStatement();

	ResultSet rsData = psData.executeQuery();

	ArrayList list = new ArrayList();
	while (rsData.next()) {
		ViewGRNDTO iss = new ViewGRNDTO();
		populateBill1(iss, rsData);
		list.add(iss);
	}
	rsData.close();

	int totalCount = 0;
	ResultSet rsCount = psCount.executeQuery();
	if (rsCount.next()) {
		totalCount = rsCount.getInt(1);
	}
	rsCount.close();

	qb.close();
	con.close();

	return new PagedList(list, totalCount, pageSize, pageNum);
}

private static void populateBill1(ViewGRNDTO iss, ResultSet rs) throws SQLException {
	iss.setIsNo(rs.getString("issue_no"));
	iss.setIdate(rs.getString("issuedate"));
	iss.setFrStore(rs.getString("fromstore"));
	iss.setToStore(rs.getString("tostore"));
	iss.setUser(rs.getString("username"));
}




 private static final String get_supplier_wise_invoice = " SELECT distinct supplier_id,invoice_no"
			+ " FROM  store_invoice ORDER BY supplier_id";

	public static String getInvoiceList() throws SQLException {

		Connection con = null;
		String arrInvoceXmlContent = null;
		con = DataBaseUtil.getReadOnlyConnection();
		arrInvoceXmlContent = DataBaseUtil.getXmlContentWithNoChild(
				get_supplier_wise_invoice, "INVOICE");
		con.close();
		return arrInvoceXmlContent;

	}

	private static final String[] QUERY_FIELD_NAMES2 =
	{ "", "RETURN_NO", "DATE_TIME"};

	private static final String RETURN_EXT_QUERY_FIELDS =
		" SELECT s.supplier_name,s.cust_supplier_code,TO_CHAR(psrm.date_time,'DD-MM-YYYY ') AS returndate,return_no,user_name,pi.invoice_no, "
		+" TO_CHAR(pi.invoice_date,'DD-MM-YYYY ') AS invoicedate,return_type,psrm.status ";

	private static final String RETURN_EXT_QUERY_COUNT =
		" SELECT count(RETURN_NO) ";

	private static final String RETURN_EXT_QUERY_TABLES =
		" FROM store_supplier_returns_main psrm "
		+" JOIN store_invoice pi USING ( supplier_invoice_id )" +
		" JOIN supplier_master s ON supplier_code=pi.supplier_id";


	public static PagedList searchSupplierReturns(int retNo,String supplier,
			java.sql.Date fromDate, java.sql.Date toDate,
			int sortOrder, boolean sortReverse, int pageSize, int pageNum)
		throws SQLException {

		Connection con = DataBaseUtil.getReadOnlyConnection();
		String sortField = null;
		if ( (sortOrder != FIELD_NONE) && (sortOrder < QUERY_FIELD_NAMES2.length) ) {
			sortField = QUERY_FIELD_NAMES2[sortOrder];
		}

		SearchQueryBuilder qb = new SearchQueryBuilder(con,
				RETURN_EXT_QUERY_FIELDS, RETURN_EXT_QUERY_COUNT, RETURN_EXT_QUERY_TABLES, null,
				sortField, sortReverse, pageSize, pageNum);

	    if (retNo > 0) qb.addFilter(qb.INTEGER, "RETURN_NO", "=", retNo);
		qb.addFilter(qb.STRING, "SUPPLIER_ID", "=", supplier);
		qb.addFilter(qb.DATE,   "date_trunc('day', DATE_TIME)", ">=", fromDate);
		qb.addFilter(qb.DATE,   "date_trunc('day', DATE_TIME)", "<=", toDate);

		qb.build();

		PreparedStatement psData = qb.getDataStatement();
		PreparedStatement psCount = qb.getCountStatement();

		ResultSet rsData = psData.executeQuery();

		ArrayList list = new ArrayList();
		while (rsData.next()) {
			ViewGRNDTO ret = new ViewGRNDTO();
			populateBill2(ret, rsData);
			list.add(ret);
		}
		rsData.close();

		int totalCount = 0;
		ResultSet rsCount = psCount.executeQuery();
		if (rsCount.next()) {
			totalCount = rsCount.getInt(1);
		}
		rsCount.close();

		qb.close();
		con.close();

		return new PagedList(list, totalCount, pageSize, pageNum);
	}

	private static void populateBill2(ViewGRNDTO ret, ResultSet rs) throws SQLException {
		ret.setSuppname(rs.getString("supplier_name"));
		ret.setRedate(rs.getString("returndate"));
		ret.setReNo(rs.getString("return_no"));
		ret.setUser(rs.getString("user_name"));
	}

	private static final String GETSUPPLIERS1 = "SELECT SUPPLIER_CODE,SUPPLIER_NAME,CUST_SUPPLIER_CODE FROM SUPPLIER_MASTER  ORDER BY SUPPLIER_NAME";

	public static ArrayList getAllSupplierNamesInMaster() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GETSUPPLIERS1);
			return DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String[] QUERY_FIELD_NAMES3 =
	{ "", "ADJ_NO", "DATE_TIME"};

	private static final String ADJ_EXT_QUERY_FIELDS =
		"SELECT GD.DEPT_NAME,TO_CHAR(DATE_TIME,'DD-MM-YYYY HH:MI:SS') AS ADJUSTDATE,ADJ_NO,USERNAME";

	private static final String ADJ_EXT_QUERY_COUNT =
		" SELECT count(ADJ_NO) ";

	private static final String ADJ_EXT_QUERY_TABLES =
		" FROM store_adj_main JOIN stores GD ON DEPT_ID=STORE_ID";




	public static PagedList searchStockAdjustments(int adjNo,String store,
			java.sql.Date fromDate, java.sql.Date toDate,
			int sortOrder, boolean sortReverse, int pageSize, int pageNum)
		throws SQLException {

		Connection con = DataBaseUtil.getReadOnlyConnection();
		String sortField = null;
		if ( (sortOrder != FIELD_NONE) && (sortOrder < QUERY_FIELD_NAMES3.length) ) {
			sortField = QUERY_FIELD_NAMES3[sortOrder];
		}

		SearchQueryBuilder qb = new SearchQueryBuilder(con,
				ADJ_EXT_QUERY_FIELDS, ADJ_EXT_QUERY_COUNT, ADJ_EXT_QUERY_TABLES, null,
				sortField, sortReverse, pageSize, pageNum);

	    if (adjNo > 0) qb.addFilter(qb.INTEGER, "ADJ_NO", "=", adjNo);
		qb.addFilter(qb.STRING, "STORE_ID", "=", store);
		qb.addFilter(qb.DATE,   "date_trunc('day', DATE_TIME)", ">=", fromDate);
		qb.addFilter(qb.DATE,   "date_trunc('day', DATE_TIME)", "<=", toDate);

		qb.build();

		PreparedStatement psData = qb.getDataStatement();
		PreparedStatement psCount = qb.getCountStatement();

		ResultSet rsData = psData.executeQuery();

		ArrayList list = new ArrayList();
		while (rsData.next()) {
			ViewGRNDTO adj = new ViewGRNDTO();
			populateBill3(adj, rsData);
			list.add(adj);
		}
		rsData.close();

		int totalCount = 0;
		ResultSet rsCount = psCount.executeQuery();
		if (rsCount.next()) {
			totalCount = rsCount.getInt(1);
		}
		rsCount.close();

		qb.close();
		con.close();

		return new PagedList(list, totalCount, pageSize, pageNum);
	}

	private static void populateBill3(ViewGRNDTO adj, ResultSet rs) throws SQLException {
		adj.setAdNo(rs.getString("adj_no"));
		adj.setAddate(rs.getString("adjustdate"));
		adj.setStoreName(rs.getString("dept_name"));
		adj.setUser(rs.getString("username"));
	}

	private static final String CATEGORY_NAME_TO_ID =
		"SELECT category_id FROM store_category_master WHERE category=?";

	public static String categoryNameToId(String catName) throws SQLException {
		Connection con = null; PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(CATEGORY_NAME_TO_ID);
			ps.setString(1, catName);

			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_USER_DEPARTMENTS = "SELECT gd.dept_id,gd.dept_name,gd.counter_id FROM stores gd,u_user u " +
			"WHERE (u.pharmacy_counter_id = gd.counter_id or role_id = 1 or role_id=2) AND emp_username = ? AND gd. status='A'";

	public static List getUserDeptNames(String user) throws SQLException {
		Connection con = null; PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_USER_DEPARTMENTS);
			ps.setString(1, user);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	private static final String MEDICINE_NAMES = "SELECT MEDICINE_NAME,CUST_ITEM_CODE FROM store_item_details ORDER BY MEDICINE_NAME ";

	public static String getMedicineNames() throws SQLException {
		Connection con = null; PreparedStatement ps = null;	ArrayList<String> SupplierNames = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(MEDICINE_NAMES);
			JSONSerializer js = new JSONSerializer().exclude("class");
			SupplierNames = DataBaseUtil.queryToArrayList1(ps);
			return js.serialize(SupplierNames);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_INVOICE = "SELECT supplier_invoice_id FROM store_invoice where supplier_id = ? AND invoice_no = ? ";

	public static BigDecimal getSupplierInvoice(String supplier, String invoice) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		List<BasicDynaBean> l = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_INVOICE);
			ps.setString(1, supplier);
			ps.setString(2, invoice);
			l  = DataBaseUtil.queryToDynaList(ps);
			if (l!= null && !l.isEmpty())
				return (BigDecimal)(l.get(0).get("supplier_invoice_id"));
			else return null;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String SUM_MRP_COST_GROUPED ="SELECT SUM(mrp_value)AS mrp_value,SUM(costprice_value)AS costprice_value,COALESCE(#::character varying,'None')AS group_by " +
    "FROM (SELECT (ROUND((qty*(mrp/issue_base_unit)),2))AS mrp_value," +
    "(ROUND((qty*(package_cp/issue_base_unit)),2))AS costprice_value,# FROM ## " +
    "WHERE ((@=?)OR(?='*')) AND exp_dt BETWEEN ? AND ? AND ! BETWEEN ? AND ? AND ((checkpoint_name::character varying=?)OR(?='*')) ) AS GROUP_MRP_COST " +
    "GROUP BY # ORDER BY #";


	public static List<BasicDynaBean> getCostandMrpSumGrouped(Connection con,
			Date fromDate, Date toDate, String groupBy, String primaryFilterBy,
			String primaryFilterValue, String secondaryFilterBy,
			int fromSecondaryFilterValInt, int toSecondaryFilterValInt,
			String checkPointName, String viewName) throws SQLException  {

		groupBy = DataBaseUtil.quoteIdent(groupBy);
		primaryFilterBy = DataBaseUtil.quoteIdent(primaryFilterBy);
		secondaryFilterBy = DataBaseUtil.quoteIdent(secondaryFilterBy);
		String query = SUM_MRP_COST_GROUPED;
		query = query.replace("##", viewName);
		query = query.replace("#", groupBy);
		query = query.replace("@", primaryFilterBy);
		query = query.replace("!", secondaryFilterBy);

		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(query);
			ps.setString(1, primaryFilterValue);
			ps.setString(2, primaryFilterValue);
			ps.setDate(3, fromDate);
			ps.setDate(4, toDate);
			ps.setInt(5, fromSecondaryFilterValInt);
			ps.setInt(6, toSecondaryFilterValInt);
			ps.setString(7, checkPointName);
			ps.setString(8, checkPointName);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			ps.close();
		}
	}


	private static final String STOCK_EXT_ALL ="SELECT medicine_name,batch_no,category_name,generic_name,control_type_name,to_char(exp_dt,'DD-MM-YYYY')as exp_dt," +
      "issue_base_unit,package_type,manf_name,qty,reorder_level,dept_name,tax_type,mrp,package_cp,tax_rate FROM ## " +
      "WHERE ((@=?)or (?='*')) AND # BETWEEN ? AND ? " +
      "AND((checkpoint_name:: character varying=?)or (?='*')) AND date(exp_dt) BETWEEN ? AND ?";

	public static void getStockDetailExtCSV(CSVWriter writer, Map params)
			throws SQLException, IOException {

		String format = (String) params.get("format");
		String groupBy = (String) params.get("groupBy");
		String primaryFilterBy = (String) params.get("primaryFilterBy");
		String primaryFilterValue = (String) params.get("primaryFilterValue");
		java.sql.Date fromDate = (java.sql.Date) params.get("fromDate");
		java.sql.Date toDate = (java.sql.Date) params.get("toDate");
		String secondaryFilterBy = (String) params.get("secondaryFilterBy");
		int fromSecondaryFilterValInt = (Integer) params.get("fromSecondaryFilterValInt");
		int toSecondaryFilterValInt = (Integer) params.get("toSecondaryFilterValInt");
		String filterBy2 = (String) params.get("filterBy2");
		String checkPointName = (String) params.get("checkPointName");
		String viewName = (String) params.get("viewName");
		if (viewName.isEmpty())
			viewName = "current_stock_details_view";
		viewName = DataBaseUtil.quoteIdent(viewName);
		primaryFilterBy = DataBaseUtil.quoteIdent(primaryFilterBy);
		secondaryFilterBy = DataBaseUtil.quoteIdent(secondaryFilterBy);
		String query = STOCK_EXT_ALL;
		query = query.replace("##", viewName);
		query = query.replace("@", primaryFilterBy);
		query = query.replace("#", secondaryFilterBy);


		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(query);
			ps.setString(1, primaryFilterValue);
			ps.setString(2, primaryFilterValue);
			ps.setInt(3, fromSecondaryFilterValInt);
			ps.setInt(4, toSecondaryFilterValInt);
			ps.setString(5, checkPointName);
			ps.setString(6, checkPointName);
			ps.setDate(7, fromDate);
			ps.setDate(8, toDate);
			rs = ps.executeQuery();
			writer.writeAll(rs, true);
			writer.flush();
		} finally {
			ps.close();
			rs.close();
			con.close();
		}

	}

	private static final String GET_PURCHASE_REPORT = "SELECT gd.dept_name,s.supplier_name,s.cust_supplier_code, pi.invoice_no, " +
			"gm.grn_date, gm.grn_no, " +
			"CASE WHEN discount_type = 'A' THEN  COALESCE((pi.discount-pi.round_off),0) " +
			"WHEN discount_type = 'P' THEN COALESCE(((" +
									"(SELECT SUM(g1.billed_qty*g1.cost_price - g1.discount + g1.tax) " +
									" FROM store_grn_details g1 JOIN store_grn_main gm1 on g1.grn_no = gm1.grn_no" +
									" JOIN stores gd ON (gd.dept_id=gm1.store_id) " +
									" WHERE gm1.supplier_invoice_id = pi.supplier_invoice_id ) + pi.cess_tax_amt)*pi.discount_per/100) - pi.round_off,0) END AS deductions, " +
			" SUM(g.cost_price*g.billed_qty) AS amount, SUM(g.tax) AS tax, SUM(g.discount) AS discount, " +
			" g.tax_rate||'%' as tax_rate,pi.cess_tax_rate, pi.cess_tax_amt, g.tax_rate AS tax_rate1" +
			" FROM store_grn_details g " +
			" JOIN store_grn_main gm USING (grn_no) " +
			" JOIN store_invoice pi USING (supplier_invoice_id) " +
			" JOIN supplier_master s on (s.supplier_code = pi.supplier_id) " +
			" JOIN stores gd ON (gd.dept_id=gm.store_id) " +
			" WHERE date_trunc('day',grn_date) between ? AND ?";
			private static final String GROUP_BY = " GROUP BY gd.dept_name,pi.supplier_invoice_id,s.supplier_name,pi.supplier_id,pi.invoice_no, " +
			" pi.cess_tax_rate, pi.cess_tax_amt,g.tax_rate, gm.grn_date, gm.grn_no,pi.discount,pi.round_off," +
			" pi.discount_type,pi.discount_per " +
			" ORDER BY gd.dept_name,s.supplier_name,pi.invoice_no,pi.cess_tax_rate, " +
			" pi.cess_tax_amt, gm.grn_date, tax_rate";

	public static List<BasicDynaBean> getReport(Connection con, Date from, Date to, String supp, String store)	throws SQLException {
			PreparedStatement ps = null;
			try{
				String query = GET_PURCHASE_REPORT;
				if (supp !=null && !supp.equals("")){
					query = query + " AND supplier_id = ? ";
				}
				if ( store !=null && !store.equals("")){
					query = query + " AND store_id = ? ";
				}
				ps = con.prepareStatement(query + GROUP_BY );
				ps.setDate(1, from);
				ps.setDate(2, to);
				if (supp !=null && !supp.equals("")){
					ps.setString(3, supp);
						if ( store !=null && !store.equals("")){
							ps.setString(4, store);
						}
				}else if ( store !=null && !store.equals("")){
					ps.setString(3, store);
				}
				//supplier_id store_id grn_date
				return DataBaseUtil.queryToDynaList(ps);
			}finally{
				if (ps != null) ps.close();
			}

		}
	private static final String GET_MANUFACTURES =
		"SELECT MANF_CODE,MANF_NAME,STATUS " +
		" FROM MANF_MASTER order by MANF_NAME";

	public static ArrayList getmanfNames() throws SQLException {
		Connection con = null; PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_MANUFACTURES);
			return DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_SUPPLIERS = "SELECT SUPPLIER_NAME FROM SUPPLIER_MASTER where status='A' order by SUPPLIER_NAME";

	public static ArrayList getSuppNames() throws SQLException {
		Connection con = null; PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_SUPPLIERS);
			return DataBaseUtil.queryToArrayList1(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	/*
	 * For Purchase Invoice Tabular Summary report
	 */
	private static final String TOBEREPLACED = "###";
	private static  final String GET_PURCHASE_INVOICE_SUMMARY=
		" SELECT coalesce(@g, 'None') as group_name, " +
		"  count(invoice_no) as count, sum(discount) as discount, sum(round_off) as round_off, " +
		"  sum(other_charges) as other_charges, sum(cess) as cess, " +
		"  sum(item_amount) as item_amount, sum(item_discount) as item_discount, sum(item_tax) as item_tax " +
		" FROM store_purchase_invoice_report_view " +
		" WHERE date(invoice_date) BETWEEN ? AND ? " +
		 TOBEREPLACED +
    	"  AND ( (? = '*') OR (@f = ?) OR (? = '' AND @f IS NULL) ) " +
		" GROUP BY group_name " +
		" ORDER BY group_name ";

	public static List<BasicDynaBean> getPurchaseInvoiceSummary (Date from, Date to,
			Date dueFromDate, Date dueToDate, String groupBy, String filterBy, String filterValue)
	throws SQLException {

	    filterBy = DataBaseUtil.quoteIdent(filterBy);
		groupBy = DataBaseUtil.quoteIdent(groupBy);
		String query = GET_PURCHASE_INVOICE_SUMMARY;

		if ( !( dueFromDate.toString()).equals("1970-01-01"))
			query = query.replace(TOBEREPLACED,"  AND ( DATE(due_date) BETWEEN ? AND ? )");
		else
			query = query.replace(TOBEREPLACED, "");

		query = query.replace("@g", groupBy);
		query = query.replace("@f", filterBy);

		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		ps = con.prepareStatement(query);
		int i = 1;
		ps.setDate(i++, from);
		ps.setDate(i++, to);
		if ( !( dueFromDate.toString()).equals("1970-01-01")) {
			ps.setDate(i++, dueFromDate);
			ps.setDate(i++, dueToDate);
		}
		ps.setString(i++, filterValue);
		ps.setString(i++, filterValue);
		ps.setString(i++, filterValue);
		List purchaseInvoiceSummaryList = DataBaseUtil.queryToDynaList(ps);
		DataBaseUtil.closeConnections(con, ps);
		return purchaseInvoiceSummaryList;
	}

	private static final String GET_ALL_PURCHASE_INVOICE_DUMP_IN_CSVEXT =
		" SELECT * " +
		" FROM store_purchase_invoice_report_view " +
		" WHERE date(invoice_date) BETWEEN ? AND ? " +
		  TOBEREPLACED +
    	"  AND ( (? = '*') OR (@f = ?) OR (? = '' AND @f IS NULL) ) ";

	public static void getPurchaseInvoiceExtCSV (CSVWriter writer, Date from, Date to,
			Date dueFromDate, Date dueToDate, String filterBy, String filterValue)
	throws SQLException, IOException {

	    filterBy = DataBaseUtil.quoteIdent(filterBy);
		String query = GET_ALL_PURCHASE_INVOICE_DUMP_IN_CSVEXT;

		if ( !( dueFromDate.toString()).equals("1970-01-01"))
			query = query.replace(TOBEREPLACED, "  AND ( (DATE(due_date) BETWEEN ? AND ?))");
		else
			query = query.replace(TOBEREPLACED, "");

		query = query.replace("@f", filterBy);
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(query);
			int i = 1;
			ps.setDate(i++, from);
			ps.setDate(i++, to);
			if ( !( dueFromDate.toString()).equals("1970-01-01")) {
				ps.setDate(i++, dueFromDate);
				ps.setDate(i++, dueToDate);
			}
			ps.setString(i++, filterValue);
			ps.setString(i++, filterValue);
			ps.setString(i++, filterValue);

			rs = ps.executeQuery();
			writer.writeAll(rs, true);
			writer.flush();
		} finally {
			ps.close();
			rs.close();
			con.close();
		}
	}

	private static final String GET_PURCHASE_INVOICE_TREND =
		" SELECT @g as group_name, " +
		"  TO_CHAR(date(DATE_TRUNC('@t',invoice_date)),'yyyy-MM-dd') AS period, " +
		"  count(invoice_no) as invoice_count, " +
		"  sum(item_amount-item_discount+item_tax+coalesce(other_charges,0)+round_off-discount+cess) as amount " +
		" FROM store_purchase_invoice_report_view " +
		" WHERE  date(invoice_date) BETWEEN ? AND ? " +
			TOBEREPLACED +
		"  AND ( (? = '*') OR (@f=?) OR (? = '' AND @f IS NULL) ) " +
		" GROUP BY period, group_name";

	public static List<BasicDynaBean> getPurchaseInvoiceTrend(Date fromDate, Date toDate,
			Date dueFromDate, Date dueToDate, String groupBy, String filterBy, String filterValue,
			String trend) throws SQLException {

	    filterBy = DataBaseUtil.quoteIdent(filterBy);
		groupBy = DataBaseUtil.quoteIdent(groupBy);

		if (!trend.equals("month") && !trend.equals("week")
				&& !trend.equals("day")) {
			logger.error("Invalid trend period: " + trend);
			return null;
		}

		String query = GET_PURCHASE_INVOICE_TREND;
		if ( !( dueFromDate.toString()).equals("1970-01-01"))
			query = query.replace(TOBEREPLACED, "  AND ( DATE(due_date) BETWEEN ? AND ? )" );
		else
			query = query.replace(TOBEREPLACED, "");

		query = query.replace("@g", groupBy);
		query = query.replace("@f", filterBy);
		query = query.replace("@t", trend);

		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		ps = con.prepareStatement(query);
		int i = 1;

		ps.setDate(i++, fromDate);
		ps.setDate(i++, toDate);
		if ( !( dueFromDate.toString()).equals("1970-01-01")) {
			ps.setDate(i++, dueFromDate);
			ps.setDate(i++, dueToDate);
		}
		ps.setString(i++, filterValue);
		ps.setString(i++, filterValue);
		ps.setString(i++, filterValue);
		List purchaseInvoiceTrendList = DataBaseUtil.queryToDynaList(ps);
		DataBaseUtil.closeConnections(con, ps);
		return purchaseInvoiceTrendList;
	}

	private static  final String GET_PURCHASE_ITEM_SUMMARY="SELECT COUNT(medicine_name)AS item_count,SUM(billed_qty)as total_billed_qty," +
	                                               "SUM(bonus_qty) as total_bonus_qty,SUM(itemwise_discount) AS total_itemwise_discount, " +
	                                               "SUM(itemwise_tax_amount) as total_itemwise_tax_amount, " +
	                                               "SUM(billed_qty*cost_price-itemwise_discount+itemwise_tax_amount) AS total_amount, " +
	                                               "COALESCE(@1,'None') AS group_name " +
	                                               "FROM store_purchase_item_report_view " +
	                                               "WHERE date(grn_date) BETWEEN ? AND ? AND " +
	                                               "( (? = '*') OR (@2=? ) OR (? = '' AND @2 IS NULL) ) " +
	                                               "GROUP BY group_name ";

	public static List<BasicDynaBean> getPurchaseItemSummary(Date fromDate, Date toDate,
			String groupBy, String filterBy, String filterValue) throws SQLException {

		 filterBy = DataBaseUtil.quoteIdent(filterBy);
		 groupBy = DataBaseUtil.quoteIdent(groupBy);
		 String query = GET_PURCHASE_ITEM_SUMMARY;
		 query = query.replace("@1", groupBy);
		 query = query.replace("@2", filterBy);
		 Connection con = DataBaseUtil.getReadOnlyConnection();
		 PreparedStatement ps = null;
		 ps = con.prepareStatement(query);
		 int i = 1;

		 ps.setDate(i++, fromDate);
		 ps.setDate(i++, toDate);
		 ps.setString(i++, filterValue);
		 ps.setString(i++, filterValue);
		 ps.setString(i++, filterValue);
		 List purchaseItemSummaryList = DataBaseUtil.queryToDynaList(ps);
		 DataBaseUtil.closeConnections(con, ps);
		return purchaseItemSummaryList;


	}

	private static final String GET_PURCHASE_ITEM_TREND="SELECT COUNT(medicine_name)as item_count," +
	                             "SUM(billed_qty*cost_price-itemwise_discount+itemwise_tax_amount) as total_amount, " +
	                             "COALESCE(@1,'None') AS group_name,TO_CHAR(date(DATE_TRUNC('%',grn_date)),'yyyy-MM-dd')as period " +
	                             "FROM  store_purchase_item_report_view " +
	                             "WHERE  date(grn_date) BETWEEN ? AND ? " +
	                             "AND ( (? = '*') OR (@2=? ) OR (? = '' AND @2 IS NULL) ) " +
	                             "GROUP BY group_name,period";

	public static List<BasicDynaBean> getPurchaseItemTrend(Date fromDate, Date toDate, String groupBy,
			String filterBy, String filterValue, String trendPeriod) throws SQLException {

		    filterBy = DataBaseUtil.quoteIdent(filterBy);
			groupBy = DataBaseUtil.quoteIdent(groupBy);
			trendPeriod = DataBaseUtil.quoteIdent(trendPeriod);
			String query = GET_PURCHASE_ITEM_TREND;
			query = query.replace("@1", groupBy);
			query = query.replace("@2", filterBy);
			query = query.replace("%", trendPeriod);
			Connection con = DataBaseUtil.getReadOnlyConnection();
			PreparedStatement ps = null;
			ps = con.prepareStatement(query);
			int i = 1;
			ps.setDate(i++, fromDate);
			ps.setDate(i++, toDate);
			ps.setString(i++, filterValue);
			ps.setString(i++, filterValue);
			ps.setString(i++, filterValue);
			List purchaseItemTrendList = DataBaseUtil.queryToDynaList(ps);
			DataBaseUtil.closeConnections(con, ps);
			return purchaseItemTrendList;
	}

	private static final String GET_ALL_PURCHASE_ITEM_DUMP_IN_CSVEXT="SELECT supplier_name,purchase_type,grn_no,medicine_name,manf_name," +
	                           "batch_no,tax_name,itemwise_tax_per,bonus_qty,billed_qty,cost_price,itemwise_discount,itemwise_tax_amount, " +
	                           "(billed_qty*cost_price-itemwise_discount+itemwise_tax_amount) as net_amount,store_name " +
	                           "FROM store_purchase_item_report_view WHERE date(grn_date) BETWEEN ? AND ? AND " +
	                           "( (? = '*')OR (@2=? ) OR (? = '' AND @2 IS NULL) ) " +
	                           "ORDER BY store_name";

	public static void getPurchaseItemExtCSV(CSVWriter writer,
			Date fromDate, Date toDate, String filterBy, String filterValue) throws SQLException, IOException {

		    filterBy = DataBaseUtil.quoteIdent(filterBy);
			String query = GET_ALL_PURCHASE_ITEM_DUMP_IN_CSVEXT;
			query = query.replace("@2", filterBy);
			Connection con = null;
			ResultSet rs = null;
			PreparedStatement ps = null;
			try {
				con = DataBaseUtil.getReadOnlyConnection();
				ps = con.prepareStatement(query);
				int i = 1;
				ps.setDate(i++, fromDate);
				ps.setDate(i++, toDate);
				ps.setString(i++, filterValue);
				ps.setString(i++, filterValue);
				ps.setString(i++, filterValue);
				rs = ps.executeQuery();
				writer.writeAll(rs, true);
				writer.flush();
			} finally {
				ps.close();
				rs.close();
				con.close();
			}
    	}

	private static final String GET_ALL_TAX_NAMES_WITH_TAX_PER="select DISTINCT(tax_name_and_tax_per) FROM " +
	                     "(SELECT  CASE WHEN pi.tax_name='CST' then  'CST:'||round(pi.cst_rate,0)  else " +
	                     "'VAT:'||round(png.tax_rate,0)  END as tax_name_and_tax_per FROM " +
	                     "store_grn_details png join store_grn_main pngm using(grn_no) " +
	                     "JOIN store_invoice pi using(supplier_invoice_id) " +
	                     "GROUP BY png.tax_rate,pi.cst_rate,pi.tax_name,png.tax_type " +
	                     "ORDER BY tax_rate,cst_rate,tax_type) as td";

	public static List getAllTaxNamesWithTaxPer() throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_ALL_TAX_NAMES_WITH_TAX_PER);
	}

	private static final String MEDICINE_NAMES_IN_MASTER = "SELECT MEDICINE_NAME,STATUS,CUST_ITEM_CODE FROM store_item_details ORDER BY MEDICINE_NAME ";

	public static String getMedicineNameMaster() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		ArrayList<String> medNames = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(MEDICINE_NAMES_IN_MASTER);
			JSONSerializer js = new JSONSerializer().exclude("class");
			medNames = DataBaseUtil.queryToArrayList(ps);
			return js.serialize(medNames);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String  store_stock_details_QUERY = "SELECT * FROM store_stock_details ";
	public BasicDynaBean getStock(Connection con,String  store,String med_id,String batch)throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		BasicDynaBean bean = null;
		GenericDAO storeStockDetialsDAO = new GenericDAO("store_stock_details");
		try{
			ps = con.prepareStatement(store_stock_details_QUERY+" WHERE DEPT_ID=? AND MEDICINE_ID=? AND BATCH_NO=?");
			ps.setString(1, store);
			ps.setString(2, med_id);
			ps.setString(3, batch);
			rs = ps.executeQuery();
			while(rs.next()){
				bean  = storeStockDetialsDAO.getBean();
				bean.set("dept_id", rs.getString("dept_id"));
				bean.set("medicine_id", rs.getString("medicine_id"));
				bean.set("batch_no", rs.getString("batch_no"));
				bean.set("mrp", rs.getBigDecimal("mrp"));
				bean.set("package_cp", rs.getBigDecimal("package_cp"));
				bean.set("package_sp",rs.getBigDecimal("package_sp"));
				bean.set("stock_time", rs.getTimestamp("stock_time"));
				bean.set("bin", rs.getString("bin"));
				bean.set("username", rs.getString("username"));
				bean.set("qty",rs.getBigDecimal("qty"));
				bean.set("tax_rate", rs.getBigDecimal("tax_rate"));
				bean.set("tax", rs.getBigDecimal("tax"));
				bean.set("exp_dt", rs.getDate("exp_dt"));
			}
			return bean;
		}finally{
			DataBaseUtil.closeConnections(null, ps,rs);
		}
	}

	private static final String PATIENT_NAMES = "SELECT DISTINCT(PATNAME) FROM ("
      +" SELECT DISTINCT(CUSTOMER_NAME) AS PATNAME FROM store_retail_customers"
      +" UNION ALL"
      +" SELECT DISTINCT(PATIENT_NAME) AS PATNAME FROM PATIENT_DETAILS) AS PAT"
      +" ORDER BY PATNAME ";

	public static String getPatientNames() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		ArrayList<String> patientNames = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(PATIENT_NAMES);
			JSONSerializer js = new JSONSerializer().exclude("class");
			patientNames = DataBaseUtil.queryToArrayList1(ps);
			return js.serialize(patientNames);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String MRNOS = "SELECT DISTINCT(MR_NO) FROM PATIENT_REGISTRATION ORDER BY MR_NO";

	public static String getMrnos() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		ArrayList<String> mrnos = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(MRNOS);
			JSONSerializer js = new JSONSerializer().exclude("class");
			mrnos = DataBaseUtil.queryToArrayList1(ps);
			return js.serialize(mrnos);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static boolean updateTable (Connection con,Map<String,Object> item,String query) throws SQLException{
		PreparedStatement ps;
		ps = con.prepareStatement(query);
		for (Map.Entry e : (Collection<Map.Entry<String, Object>>) item.entrySet()) {
				ps.setObject(Integer.parseInt((e.getKey()).toString()), e.getValue());
		}
		int count = ps.executeUpdate();
		if (ps != null) ps.close();
	 return count > 0;

	}

	private static final String GET_TAX_TYPES_PRESENT_IN_DB =
		" select tax_rate,(SELECT  CASE WHEN procurement_tax_label='V' then 'VAT' else 'GST' END as procurement_tax_label "
		+" FROM generic_preferences) as tax_name FROM store_grn_main pm JOIN store_grn_details p "
		+" USING (grn_no) JOIN store_invoice i USING (supplier_invoice_id)"
        +" WHERE i.tax_name in ('VAT','GST') GROUP BY tax_rate"
        +" union"
        +" SELECT cst_rate AS tax_rate,(SELECT  CASE WHEN procurement_tax_label='V' then 'CST' else 'iGST' END as procurement_tax_label "
		+" FROM generic_preferences) as tax_name  FROM store_invoice WHERE tax_name  in ('CST','iGST')"
        +" GROUP BY cst_rate"
        +" order by tax_name desc,tax_rate";

	public static ArrayList getTaxTypes() throws SQLException {
		Connection con = null; PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_TAX_TYPES_PRESENT_IN_DB);
			return DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static List getTaxTypesBean() throws SQLException {
		Connection con = null; PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_TAX_TYPES_PRESENT_IN_DB);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static List getCEDTypesBean() throws SQLException {
		Connection con = null; PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement("select distinct item_ced_per from store_grn_details order by item_ced_per");
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
}
