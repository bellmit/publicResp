package com.insta.hms.master.StoresItemIssueRateMaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.HsSfWorkbookUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.SearchQueryBuilder;
import freemarker.core.InvalidReferenceException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StoresItemIssueRateMasterDAO extends GenericDAO {

	static Logger log = LoggerFactory.getLogger(StoresItemIssueRateMasterDAO.class);
	public static Map<String, String> aliasNamesToDbCOlNames = new LinkedHashMap<String, String>();
	static {
		aliasNamesToDbCOlNames.put("Item ID", "medicine_id");
		aliasNamesToDbCOlNames.put("Item Name", "medicine_name");
		aliasNamesToDbCOlNames.put("Issue Rate", "issue_rate_expr");
		aliasNamesToDbCOlNames.put("Category Name", "category_name");

	}

	public StoresItemIssueRateMasterDAO( ) {
		super("store_item_issue_rates");
	}

	private static final String GET_MAX_MRP = "SELECT medicine_name, s.medicine_id, " +
				"MAX(sibd.mrp) AS max_mrp, MAX(package_cp) AS avg_cp, sir.issue_rate_expr " +
				"FROM store_item_details s   " +
				"LEFT JOIN store_stock_details ssd USING (medicine_id) " +
				"LEFT JOIN store_item_batch_details sibd USING(item_batch_id) " +
				"LEFT JOIN store_item_issue_rates  sir ON(s.medicine_id = sir.medicine_id) " +
				"WHERE s.medicine_id = ? GROUP BY  medicine_name, s.medicine_id, sir.issue_rate_expr";
	public static BasicDynaBean getMaxMRP (int medId) throws SQLException {

		 PreparedStatement ps = null;
		 Connection con = null;
			try {
				con = DataBaseUtil.getConnection();
				ps = con.prepareStatement(GET_MAX_MRP);
				ps.setInt(1, medId);
				return DataBaseUtil.queryToDynaBean(ps);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
	}


	public static void exportRates(XSSFSheet workSheet, String deptFilter, String statusFilter, Map map) throws SQLException, ParseException {
		String fields = "SELECT medicine_id, medicine_name, issue_rate_expr, category_name ";
		String fromTales = " FROM (SELECT medicine_id, medicine_name, issue_rate_expr, mc.category AS category_name, " +
				"mm.manf_name, gn.generic_name " +
				"FROM store_item_issue_rates iir " +
				"JOIN store_item_details pmd USING (medicine_id) " +
				"LEFT JOIN manf_master mm ON pmd.manf_name=mm.manf_code " +
				"LEFT JOIN generic_name gn ON pmd.generic_name=gn.generic_code " +
				"LEFT JOIN store_category_master mc ON pmd.med_category_id=mc.category_id ) AS foo ";
		Connection con = null;
		PreparedStatement pstmt = null;
		Map<String, List<String>> columnNamesMap = new HashMap<String, List<String>>();
		List<String> itemList = new ArrayList<String>();
		itemList.addAll(aliasNamesToDbCOlNames.keySet());
		columnNamesMap.put("mainItems", itemList);

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			SearchQueryBuilder sqb = new SearchQueryBuilder(con, fields, null, fromTales);
			sqb.addFilterFromParamMap(map);
			sqb.addSecondarySort("medicine_name");
			sqb.build();

			pstmt = sqb.getDataStatement();
			List list = DataBaseUtil.queryToDynaList(pstmt);
			HsSfWorkbookUtils.createPhysicalCellsWithValues(list, columnNamesMap, workSheet, true);

		} finally {
			DataBaseUtil.closeConnections(con, pstmt);

		}

	}

	private static String DELETE_ITEM_RATES = "DELETE FROM store_item_issue_rates WHERE medicine_id = ?";
	public static boolean deleteRates(String[] deleteItems) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(DELETE_ITEM_RATES);
			int i = 0;
			for (String item: deleteItems) {
				ps.setInt(1, Integer.parseInt(item));
				i = ps.executeUpdate();
			}
			return i > 0;
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_ITEMS_TO_BE = "SELECT medicine_name, medicine_id " +
			"FROM store_item_details s " +
			"JOIN store_category_master c ON (c.category_id = s.med_category_id) WHERE billable " +
			"AND s.medicine_id NOT IN (SELECT medicine_id FROM store_item_issue_rates)";
	public static List getItemsForRates() throws SQLException {

	 PreparedStatement ps = null;
	 Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ITEMS_TO_BE);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public static void putissueRateExprParams(List params) {
		params.add("package_cp");
		params.add("store_id");
		params.add("center_id");
		params.add("bed_type");
		params.add("mrp");
		params.add("discount (%)");
		params.add("max_cp");
	}
	
	/*This method is used to validate issue rate expression given in issue rate master  */
	public boolean isValidExpression(String expression)throws ArithmeticException,Exception {
		boolean valid = false;
		Map results = new HashMap();
		results.put("center_id", 1);
        results.put("store_id", 1);
        results.put("package_cp",1);
        results.put("bed_type", "GENERAL");
        results.put("mrp", 0);
        results.put("discount", 0);
        results.put("max_cp", 0);
        StringWriter writer = new StringWriter();
        String expr = "<#setting number_format=\"##.##\">\n" + expression;
        try{
        	Template expressionTemplate = new Template("expression", new StringReader(expr),new Configuration());
        	expressionTemplate.process(results, writer);
        }catch (InvalidReferenceException ine) {
        	log.error("", ine);
        	return false;
        }catch (TemplateException e) {
        	log.error("", e);
        	return false;
        }catch (ArithmeticException e) {
        	log.error("", e);
        	return false;
        }catch(Exception e){
        	log.error("", e);
        	return false;
        }
        //it check non integer nos
        valid = !writer.toString().contains("[^.\\d]");

        try{
        	if(!writer.toString().trim().isEmpty()){
        		BigDecimal validNumber = new BigDecimal(writer.toString().trim());
        	}
        }catch(NumberFormatException ne){
        	log.error("", ne);
        	valid = false;
        }

        return valid ;
	}
}
