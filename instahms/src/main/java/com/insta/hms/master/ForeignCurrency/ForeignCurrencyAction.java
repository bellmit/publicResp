/**
 *
 */
package com.insta.hms.master.ForeignCurrency;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.HsSfWorkbookUtils;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.xls.exportimport.DetailsImportExporter;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author lakshmi.p
 *
 */
public class ForeignCurrencyAction extends DispatchAction {

	ForeignCurrencyDAO dao = new ForeignCurrencyDAO();

	public ActionForward list(ActionMapping m, ActionForm f,
							HttpServletRequest req, HttpServletResponse res) throws Exception {
		Map params = req.getParameterMap();
		PagedList pagedList = dao.search(params,ConversionUtils.getListingParameter(params), "currency_id");
		req.setAttribute("pagedList", pagedList);
		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f,
							HttpServletRequest req, HttpServletResponse res) throws Exception {
		JSONSerializer js = new JSONSerializer();
		List currencyList = ConversionUtils.listBeanToListMap(dao.listAll());
        req.setAttribute("currencyList", js.serialize(currencyList));
		return m.findForward("addshow");
	}

	public ActionForward create (ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		HttpSession session=request.getSession();
		String userid = (String)session.getAttribute("userid");

		Map params = request.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		bean.set("username", userid);
		bean.set("mod_time", DateUtil.getCurrentTimestamp());

		String error = null;
		String success = null;

		int currencyId = bean.get("currency_id") != null ? (Integer) bean.get("currency_id") : 0;
		if (errors.isEmpty()) {
			boolean exists = dao.exist(currencyId, (String) bean.get("currency"));
			if (exists) {
				error = "Currency already exists.....";
			} else {
				bean.set("currency_id", dao.getNextCurrencyId());
				boolean sucess = dao.insert(con, bean);
				if (sucess) {
					con.commit();
					success = "Currency details inserted successfully...";
				} else {
					con.rollback();
					error = "Currency details insert unsuccessful...";
				}
				con.close();
			}
		} else {
			error = "Incorrectly formatted values supplied..";
		}
		ActionRedirect redirect = null;
		FlashScope flash = FlashScope.getScope(request);
		if (error != null) {
			redirect = new ActionRedirect(m.findForward("addRedirect"));
			flash.error(error);
		}
		if (success != null) {
			redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("currency_id", bean.get("currency_id"));
			flash.success(success);
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;

	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		BasicDynaBean bean = dao.findByKey("currency_id", new Integer(req.getParameter("currency_id")));
		req.setAttribute("bean", bean);

		JSONSerializer js = new JSONSerializer();
		List currencyList = ConversionUtils.listBeanToListMap(dao.listAll());
        req.setAttribute("currencyList", js.serialize(currencyList));
		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		HttpSession session=req.getSession();
		String userid = (String)session.getAttribute("userid");

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		Map params = req.getParameterMap();
		List errors = new ArrayList();

		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		bean.set("username", userid);
		bean.set("mod_time", DateUtil.getCurrentTimestamp());

		Object key = req.getParameter("currency_id");

		Map<String, Integer> keys = new HashMap<String, Integer>();
		keys.put("currency_id", new Integer(key.toString()));
		FlashScope flash = FlashScope.getScope(req);

		if (errors.isEmpty()) {
			int success = dao.update(con, bean.getMap(), keys);
			if (success > 0) {
				con.commit();
				flash.success("Currency details updated successfully..");
			} else {
				con.rollback();
				flash.error("Failed to update currency details..");
			}
			con.close();
		} else {
			flash.error("Incorrectly formatted values supplied");
		}
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter("currency_id", key.toString());
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}


	public ActionForward exportCurrencyDetailsToXls(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, java.io.IOException {

		List<String> currencyTableColumns = Arrays.asList(new String[] {
							"Currency Id", "Currency Name", "Conversion Rate", "Status"});

		Map<String, List> columnNamesMap = new HashMap<String, List>();
		columnNamesMap.put("mainItems", currencyTableColumns);
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet currencyWorkSheet = workbook.createSheet("CURRENCY");
		List<BasicDynaBean> currencyList=ForeignCurrencyDAO.getCurrencyDetails();
		HsSfWorkbookUtils.createPhysicalCellsWithValues(currencyList, columnNamesMap, currencyWorkSheet, true);

		res.setHeader("Content-type", "application/vnd.ms-excel");
		res.setHeader("Content-disposition","attachment; filename=CurrencyDetails.xls");
		res.setHeader("Readonly", "true");
		java.io.OutputStream os = res.getOutputStream();
		workbook.write(os);
		os.flush();
		os.close();
		return null;
	}

	public static DetailsImportExporter detailsImporExp;

	static {
		detailsImporExp = new DetailsImportExporter("foreign_currency", "", "");
	}

	public StringBuilder errors;

	public ActionForward importCurrencyDetailsFromXls(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {

		ForeignCurrencyUploadForm currencyForm = (ForeignCurrencyUploadForm) form;
		ByteArrayInputStream byteStream = new ByteArrayInputStream(currencyForm.getXlsCurrencyFile().getFileData());
		XSSFWorkbook workBook = new XSSFWorkbook(byteStream);
		XSSFSheet sheet = workBook.getSheetAt(0);

		this.errors = new StringBuilder();
		Map<String, String> aliasMap = new HashMap<String, String>();

		aliasMap.put("currency id", "currency_id");
		aliasMap.put("currency name", "currency");
		aliasMap.put("conversion rate", "conversion_rate");
		aliasMap.put("status", "status");

		List<String> mandatoryList = Arrays.asList("currency_id", "currency", "conversion_rate", "status");
		List<String> exemptFromNullCheck = Arrays.asList("currency_id");
		List<String> oddFields = Arrays.asList("");

		detailsImporExp.setTableDbName("currency");
		detailsImporExp.setAliasUnmsToDBnmsMap(aliasMap);
		detailsImporExp.setId("currency_id");
		detailsImporExp.setMandatoryFields(mandatoryList);
		detailsImporExp.setIsDateRequired(true);
		detailsImporExp.setIsUserNameRequired(true);
		detailsImporExp.setExemptFromNullCheck(exemptFromNullCheck);
		detailsImporExp.setColumnNameForUser("username");
		detailsImporExp.setIsUserNameRequired(true);
		detailsImporExp.setOddFields(oddFields);
		detailsImporExp.setColumnNameForDate("mod_time");

		String referer = request.getHeader("Referer");
		referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
		ActionRedirect redirect = new ActionRedirect(referer);
		FlashScope flash = FlashScope.getScope(request);

		detailsImporExp.importDetailsToXls(sheet, null, errors, (String)request.getSession(false).getAttribute("userid"));

		if (this.errors.length() > 0)
			flash.put("error", this.errors);
		else
			flash.put("info", "File successfully uploaded (New values or currencies added will be ignored)");
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}
}
