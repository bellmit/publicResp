package com.insta.hms.master.StoresRatePlanMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.utils.JsonUtility;
import com.insta.hms.master.StoreItemRates.StoreItemRatesDAO;
import com.insta.hms.stores.StoreItemCodesDAO;
import com.insta.hms.xls.exportimport.DetailsImportExporter;

import org.apache.commons.beanutils.BasicDynaBean;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import flexjson.JSONSerializer;

public class StoresRatePlanMasterAction extends BaseAction {

	static Logger logger = LoggerFactory.getLogger(StoresRatePlanMasterAction.class);
	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception{

		StoresRatePlanDAO storeRPDAO = new StoresRatePlanDAO();
		Map requestParams = req.getParameterMap();
		PagedList list = storeRPDAO.list(requestParams, ConversionUtils.getListingParameter(req.getParameterMap()));

		req.setAttribute("list", list);
		req.setAttribute("storeRatePlans", new StoresRatePlanDAO().listAll(null,"status","A","store_rate_plan_name"));
		req.setAttribute("codeTypes", StoreItemCodesDAO.getItemCodeTypes("Drug"));
		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception{

		JSONSerializer json = new JSONSerializer().exclude("class");
		List storesRatePlans = new StoresRatePlanDAO().listAll();
		req.setAttribute("existingStoresRatePlans", storesRatePlans);
		req.setAttribute("existingStoresRatePlansJSON", JsonUtility.toJson(ConversionUtils.listBeanToMapMap(storesRatePlans,"store_rate_plan_name")));
		return m.findForward("addshow");
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception{
		JSONSerializer json = new JSONSerializer().exclude("class");
		req.setAttribute("bean", new StoresRatePlanDAO().findByKey("store_rate_plan_id", Integer.parseInt(req.getParameter("store_rate_plan_id"))));
		List storesRatePlans = new StoresRatePlanDAO().listAll();
		req.setAttribute("existingStoresRatePlans", storesRatePlans);

		req.setAttribute("existingStoresRatePlansJSON", JsonUtility.toJson(ConversionUtils.listBeanToMapMap(storesRatePlans,"store_rate_plan_name")));
		
		req.setAttribute("RPListJSON", json.serialize(
				ConversionUtils.listBeanToListMap(
						new GenericDAO("organization_details").findAllByKey(
								"store_rate_plan_id", Integer.parseInt(req.getParameter("store_rate_plan_id"))))));
		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception{

		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect=new ActionRedirect(m.findForward("addRedirect"));
		Map reqMap = req.getParameterMap();


		BasicDynaBean storesRatePlanBean = new StoresRatePlanDAO().getBean();
		StoresRatePlanDAO storeRPDAO = new StoresRatePlanDAO();
		List<String> errors = new ArrayList<String>();
		ConversionUtils.copyToDynaBean(req.getParameterMap(), storesRatePlanBean, errors);

		if (!errors.isEmpty()) {
			flash.put("error", "Incorrectly formatted values supplied");
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}

		boolean success = true;
		Connection con = null;
		int storeRatePlanId = 0;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			storeRatePlanId = storeRPDAO.getNextSequence();
			storesRatePlanBean.set("store_rate_plan_id", storeRatePlanId);
			success = storeRPDAO.insert(con, storesRatePlanBean) ;

			String[] cpRatePlan = (String[])reqMap.get("cp_rate_plan_id");
			int cpRatePlanId = cpRatePlan != null && cpRatePlan.length > 0 && !cpRatePlan[0].isEmpty() ? Integer.parseInt( cpRatePlan[0] ) : 0;

			success &= storeRPDAO.addSPForRatePlan(con,storeRatePlanId, cpRatePlanId);//adding charges to all items for this rateplan.
      if (cpRatePlanId != 0 && success) {
        success &= storeRPDAO.addTaxSubgroupForStoreTariff(con, storeRatePlanId, cpRatePlanId);
      }
		}finally{
			DataBaseUtil.commitClose(con, success);
		}

		redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter("store_rate_plan_id", storeRatePlanId);
		return redirect;
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception{

		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect=new ActionRedirect(req.getContextPath()+"pages/master/StoresMaster/StoresRatePlans.do?_method=show");

		BasicDynaBean storesRatePlanBean = new StoresRatePlanDAO().getBean();
		StoresRatePlanDAO storeRPDAO = new StoresRatePlanDAO();
		List<String> errors = new ArrayList<String>();
		ConversionUtils.copyToDynaBean(req.getParameterMap(), storesRatePlanBean, errors);

		if (!errors.isEmpty()) {
			flash.put("error", "Incorrectly formatted values supplied");
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}

		boolean success = true;
		Connection con = null;
		int storeRatePlanId = 0;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			storeRatePlanId = Integer.parseInt( req.getParameter("store_rate_plan_id") );
			storesRatePlanBean.set("store_rate_plan_id", storeRatePlanId);
			success = storeRPDAO.update(con, storesRatePlanBean.getMap(),"store_rate_plan_id", storeRatePlanId) > 0;

		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("store_rate_plan_id", storeRatePlanId);
		return redirect;
	}

	public ActionForward exportRatesToXls(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException, ParseException {

		XSSFWorkbook workbook = new XSSFWorkbook();
		request.getParameterMap();
		String codeType = request.getParameter("code_type");
		String storeRatePlanId = request.getParameter("store_rate_plan_id");
		if(storeRatePlanId.equals("all")){
			List<BasicDynaBean> storeRatePlans = new StoresRatePlanDAO().listAll(null,"status","A",null);
			for ( BasicDynaBean storeratePlan : storeRatePlans ) {
				XSSFSheet ratePlanWS = workbook.createSheet((String)storeratePlan.get("store_rate_plan_name"));
				StoreItemRatesDAO.exportRates(ratePlanWS, null, "A", (Integer)storeratePlan.get("store_rate_plan_id"), codeType);
			}
		} else {
			BasicDynaBean storeRatePlanDetails = new StoresRatePlanDAO().findByKey("store_rate_plan_id", Integer.parseInt(storeRatePlanId));
		 	XSSFSheet ratePlanWS = workbook.createSheet((String)storeRatePlanDetails.get("store_rate_plan_name"));
		 	StoreItemRatesDAO.exportRates(ratePlanWS, null, "A", (Integer)storeRatePlanDetails.get("store_rate_plan_id"), codeType);
		}
	
		response.setHeader("Content-type", "application/vnd.ms-excel");
		response.setHeader("Content-disposition","attachment; filename="+"\"StoreItemRates_"+"StoreItemRates.xls\"");
		response.setHeader("Readonly", "true");
		java.io.OutputStream outputStream = response.getOutputStream();
		workbook.write(outputStream);
		outputStream.flush();
		outputStream.close();

		return null;
	}


	public ActionForward importRatesFromXls(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, Exception {
		Map<String, String> aliasNamesToDbCOlNames = new LinkedHashMap<String, String>();
		aliasNamesToDbCOlNames.put("item id", "medicine_id");
		aliasNamesToDbCOlNames.put("store rate plan id", "store_rate_plan_id");
		aliasNamesToDbCOlNames.put("item name", "medicine_name");
		aliasNamesToDbCOlNames.put("category name", "category_name");
		aliasNamesToDbCOlNames.put("tax basis", "tax_type");
		aliasNamesToDbCOlNames.put("tax %", "tax_rate");
		aliasNamesToDbCOlNames.put("drug code", "item_code");
		aliasNamesToDbCOlNames.put("code type", "code_type");
		aliasNamesToDbCOlNames.put("selling price expression", "selling_price_expr");
		Map<String, Object[]> map = getParameterMap(request);

		if (map.get("fileSizeError") != null) {
			// if the file size is greater than 10 MB prompting the user with the failure message.
			return mapping.findForward("fileUploadSizeError");
		}

		FlashScope flash = FlashScope.getScope(request);
		String referer = request.getHeader("Referer");
		ActionRedirect redirect;
		if(referer != null) {
			referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
			redirect = new ActionRedirect(referer);
		} else {
			redirect = new ActionRedirect(mapping.findForward("listRedirect"));
		}
	
		List<String> extraFeilds = new ArrayList<String>();
		extraFeilds.add("medicine_name");
		extraFeilds.add("category_name");
		extraFeilds.add("item_code");
		extraFeilds.add("code_type");
		extraFeilds.add("selling_price_expr");
		XSSFWorkbook workBook = new XSSFWorkbook( (InputStream) ((map.get("xlsRatesFile")))[0] );
		XSSFSheet sheet = null;

		DetailsImportExporter importer = new DetailsImportExporter("store_item_rates", "", "");
		importer.setAliasUnmsToDBnmsMap(aliasNamesToDbCOlNames);
		List<String> feilds = new ArrayList<String>();
		feilds.add("medicine_id");
		importer.setExemptFromNullCheck(feilds);
		List<String> mFeilds = new ArrayList<String>();
		importer.setMandatoryFields(mFeilds);
		StringBuilder errors = new StringBuilder();
		importer.setId("medicine_id");
		importer.setOddFields(new ArrayList());
		importer.seTextraFields(extraFeilds);
		boolean flag = false;
		try	{
			if(workBook.getNumberOfSheets() > 1){
				List<BasicDynaBean> storeRatePlans = new StoresRatePlanDAO().listAll(null,"status","A",null);
				for ( BasicDynaBean storeRatePlan : storeRatePlans ) {
					sheet = workBook.getSheet((String)storeRatePlan.get("store_rate_plan_name"));
					logger.debug("StoresRatePlanMasterAction:importRatesFromXls::sheet("+(String)sheet.getSheetName()+") is "+sheet);
					if(sheet!=null){
						importer.setExtraId("store_rate_plan_id");
						importer.importDetailsToXls(sheet, null, errors, null);
						flag = true;
					}
				}
			} else {
				sheet = workBook.getSheetAt(0);
				if(sheet!=null){
					importer.setExtraId("store_rate_plan_id");
					importer.importDetailsToXls(sheet, null, errors, null);
					flag = true;
				}
			}
			if (errors.length() > 0){
				flash.put("error", errors);
			}else {
				if(flag){
					flash.put("info", "File successfully uploaded");
				}else{
					flash.put("error", "Unable to upload file.");
				}
			}
			
		} catch(Exception e){
			redirect = new ActionRedirect(request.getHeader("Referer").
					replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			flash.error(e.getMessage());
		}

		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

}
