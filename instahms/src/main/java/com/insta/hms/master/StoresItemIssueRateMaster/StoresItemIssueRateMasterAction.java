package com.insta.hms.master.StoresItemIssueRateMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.stores.PharmacymasterDAO;
import com.insta.hms.xls.exportimport.DetailsImportExporter;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StoresItemIssueRateMasterAction extends BaseAction{

	static Logger logger = LoggerFactory.getLogger(StoresItemIssueRateMasterAction.class);

	public ActionForward list(ActionMapping m,ActionForm f,HttpServletRequest req,
			HttpServletResponse res) throws IOException ,SQLException, Exception{
		Map map= getParameterMap(req);
		PagedList list = PharmacymasterDAO.searchMedicineForIssueRates(map, ConversionUtils.getListingParameter(map));
		req.setAttribute("pagedList", list);
		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		List<String> exprTokens = new ArrayList<String>();
		StoresItemIssueRateMasterDAO.putissueRateExprParams(exprTokens);
		req.setAttribute("exprTokens", exprTokens);
		JSONSerializer js = new JSONSerializer().exclude("class");
		req.setAttribute("itemList", js.serialize(ConversionUtils.copyListDynaBeansToMap(StoresItemIssueRateMasterDAO.getItemsForRates())));
		return m.findForward("addshow");
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		List<String> exprTokens = new ArrayList<String>();
		StoresItemIssueRateMasterDAO.putissueRateExprParams(exprTokens);
		req.setAttribute("exprTokens", exprTokens);
		BasicDynaBean bean = StoresItemIssueRateMasterDAO.getMaxMRP(Integer.parseInt(req.getParameter("medicine_id")));
		req.setAttribute("bean", bean);
		return m.findForward("addshow");
	}

	public ActionForward getItemMaxMRP(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws IOException, SQLException {
		String medicineId = req.getParameter("medicine_id");
		BasicDynaBean b = null;
		res.setContentType("text/plain");
		res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		JSONSerializer js = new JSONSerializer().exclude("class");
		if ( medicineId != null && !medicineId.equals(""))
			b = StoresItemIssueRateMasterDAO.getMaxMRP(Integer.parseInt(medicineId));
		res.getWriter().write( js.serialize(b.getMap()));
		return null;
	}


	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			String issueRateExpr = req.getParameter("issue_rate_expr");
			StoresItemIssueRateMasterDAO masterDAO = new StoresItemIssueRateMasterDAO();
			boolean valid = masterDAO.isValidExpression(issueRateExpr);
			BasicDynaBean bean = masterDAO.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);
			boolean success = false;
			FlashScope flash = FlashScope.getScope(req);

			if(valid) {
				if (errors.isEmpty()) {
					success = new StoresItemIssueRateMasterDAO().insert(con, bean);
				} else {
					flash.error("Incorrectly formatted values supplied");
				}
			} else {
				flash.error("Invalid expression is given.");
			}
			if (success)
				flash.info("New Issue Rate created succesfully.");
			ActionRedirect redirect = new ActionRedirect(m.findForward("addRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;

		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = null;
		Map params = req.getParameterMap();
		List errors = new ArrayList();
		String issueRateExpr = req.getParameter("issue_rate_expr");
		StoresItemIssueRateMasterDAO masterDAO = new StoresItemIssueRateMasterDAO();
		boolean valid = masterDAO.isValidExpression(issueRateExpr);

		try {
			con = DataBaseUtil.getConnection();
			BasicDynaBean bean = masterDAO.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);
			FlashScope flash = FlashScope.getScope(req);
			if(valid){ 
				if (errors.isEmpty()) {
					int i = masterDAO.updateWithName(con, bean.getMap(), "medicine_id");
					if (i > 0)
						flash.info("Issue Rate updated successfully.");
					else
						flash.error("Failed to update");
				}
				else {
					flash.error("Incorrectly formatted values supplied");
				}
			} else {
				flash.error("Invalid expression is given.");
			}
			ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("medicine_id", bean.get("medicine_id"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

			return redirect;

		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}


	public ActionForward exportRatesToXls(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException, ParseException {

		XSSFWorkbook workbook = new XSSFWorkbook();
		request.getParameterMap();

		XSSFSheet workSheet = workbook.createSheet("ISSUE RATES");
		StoresItemIssueRateMasterDAO.exportRates(workSheet, null, "A", request.getParameterMap());

		response.setHeader("Content-type", "application/vnd.ms-excel");
		response.setHeader("Content-disposition","attachment; filename="+"\"IssueCharges_"+"IssueRates.xls\"");
		response.setHeader("Readonly", "true");
		java.io.OutputStream outputStream = response.getOutputStream();
		workbook.write(outputStream);
		outputStream.flush();
		outputStream.close();

		return null;
	}

	public static Map<String, String> aliasNamesToDbCOlNames = new HashMap<String, String>();
	static {
		aliasNamesToDbCOlNames.put("item id", "medicine_id");
		aliasNamesToDbCOlNames.put("item name", "medicine_name");
		aliasNamesToDbCOlNames.put("issue rate", "issue_rate_expr");
		aliasNamesToDbCOlNames.put("category name", "category_name");
	}
	public ActionForward importRatesFromXls(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, Exception {


		Map<String, Object[]> map = getParameterMap(request);
		List<String> extraFeilds = new ArrayList<String>();
		extraFeilds.add("medicine_name");
		extraFeilds.add("category_name");
		XSSFWorkbook workBook = new XSSFWorkbook( (InputStream) ((map.get("xlsRatesFile")))[0] );
		XSSFSheet sheet = workBook.getSheetAt(0);
		DetailsImportExporter importer = new DetailsImportExporter("store_item_issue_rates", "", "");
		importer.setAliasUnmsToDBnmsMap(aliasNamesToDbCOlNames);
		List<String> feilds = new ArrayList<String>();
		feilds.add("medicine_id");
		importer.setExemptFromNullCheck(feilds);
		List<String> mFeilds = new ArrayList<String>();
		mFeilds.add("issue_rate_expr");
		importer.setMandatoryFields(mFeilds);
		StringBuilder errors = new StringBuilder();
		importer.setId("medicine_id");
		importer.setOddFields(new ArrayList());
		importer.seTextraFields(extraFeilds);
		importer.setDeptNotExist(true);
		importer.setFlagForRefDupChk(true);
		importer.setRefTableForDupChk("store_item_details");
		importer.setColForRefDupChk("medicine_name");
		importer.setColNumForRefDupChk(1);
		importer.importDetailsToXls(sheet, null, errors, null);


		FlashScope flash = FlashScope.getScope(request);
		flash.error(errors.toString());
		String referer = request.getHeader("Referer");
		referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
		ActionRedirect redirect = new ActionRedirect(referer);

		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward deleteRates( ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, Exception {
		Map map =  request.getParameterMap();
		String[] delItems = (String[]) map.get("delete");
		boolean success = StoresItemIssueRateMasterDAO.deleteRates(delItems);
		FlashScope flash = FlashScope.getScope(request);
		if (success)
			flash.info("Issue Rates deleted successfully");
		else
			flash.error("Failed to delete");

		ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}


}
