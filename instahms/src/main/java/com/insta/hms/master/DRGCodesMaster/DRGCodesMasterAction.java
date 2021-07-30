/**
 *
 */
package com.insta.hms.master.DRGCodesMaster;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.HsSfWorkbookUtils;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupRepository;
import com.insta.hms.xls.exportimport.DetailsImportExporter;

import flexjson.JSONSerializer;

/**
 * @author lakshmi
 *
 */
public class DRGCodesMasterAction extends BaseAction {

	static Logger logger = LoggerFactory.getLogger(DRGCodesMasterAction.class);
	
	private static final GenericDAO itemGroupTypeDAO = new GenericDAO("item_group_type");
	private static final GenericDAO itemGroupsDAO = new GenericDAO("item_groups");
	private static final GenericDAO drugCodeItemSubGroupsDAO = new GenericDAO("drg_code_item_sub_groups");

	DRGCodesMasterDAO dao = new DRGCodesMasterDAO();

	public ActionForward list(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res)  throws Exception {

		Map filter = getParameterMap(req);
		PagedList pagedList = dao.search(filter, ConversionUtils.getListingParameter(filter), "drg_code");
		req.setAttribute("pagedList", pagedList);
		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws Exception {
		JSONSerializer js = new JSONSerializer().exclude("class");
		req.setAttribute("itemGroupTypeList", ConversionUtils.listBeanToListMap(itemGroupTypeDAO.findAllByKey("item_group_type_id","TAX")));
		req.setAttribute("itemGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(itemGroupsDAO.findAllByKey("status","A"))));
		//req.setAttribute("itemSubGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(new GenericDAO("item_sub_groups").findAllByKey("status","A"))));
		List <BasicDynaBean> itemSubGroupList = new TaxSubGroupRepository().getItemSubGroup(new java.sql.Date(new java.util.Date().getTime()));
		Iterator<BasicDynaBean> itemSubGroupListIterator = itemSubGroupList.iterator();
		List<BasicDynaBean> validateItemSubGrouList = new ArrayList<BasicDynaBean>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String currentDateStr = sdf.format(new java.util.Date());
		while(itemSubGroupListIterator.hasNext()) {
			BasicDynaBean itenSubGroupbean = itemSubGroupListIterator.next();
			if(itenSubGroupbean.get("validity_end") != null){
				Date endDate = (Date)itenSubGroupbean.get("validity_end");
				
				try {
					if(sdf.parse(currentDateStr).getTime() <= endDate.getTime()) {
						validateItemSubGrouList.add(itenSubGroupbean);
					}
				} catch (ParseException e) {
					continue;
				}
			} else {
				validateItemSubGrouList.add(itenSubGroupbean);
			}
		}
		req.setAttribute("itemSubGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(validateItemSubGrouList)));
		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
		throws IOException, ServletException, Exception {

		Map params = getParameterMap(req);
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = null;

		if (errors.isEmpty()) {
			BasicDynaBean exists = dao.findByKey("drg_code", bean.get("drg_code"));
			if(exists == null) {
				String username = (String)req.getSession(false).getAttribute("userid");
				bean.set("username", username);
				boolean success = dao.insert(con, bean);
				
				if(success) {
					String drg_code = (String) bean.get("drg_code");
					success = saveItemSubGroup(drg_code,con,req);
				}
				
				if (success) {
					con.commit();
					redirect = new ActionRedirect(m.findForward("showRedirect"));
					flash.success("DRG Code details inserted successfully..");
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					redirect.addParameter("drg_code", bean.get("drg_code"));
					DataBaseUtil.closeConnections(con, null);
					return redirect;
				} else {
					con.rollback();
					flash.error("Failed to add DRG Code...");
				}
			} else {
				flash.error("DRG Code already exists...");
			}
		} else {
			flash.error("Incorrectly formatted values supplied");
		}
		redirect = new ActionRedirect(m.findForward("addRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
		throws IOException, ServletException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		Map filterMap = new HashMap();
		filterMap.put("drg_code", req.getParameter("drg_code"));
		List beans = dao.listAll(null, filterMap, null);
		req.setAttribute("bean", beans.size() > 0 ? beans.get(0): null);
		req.setAttribute("DRGCodesList", js.serialize(ConversionUtils.copyListDynaBeansToMap(dao.listAll())));
		req.setAttribute("taxsubgroup", ConversionUtils.copyListDynaBeansToMap(dao.getDrgItemSubGroupDetails(req.getParameter("drg_code"))));
		req.setAttribute("itemGroupTypeList", ConversionUtils.listBeanToListMap(itemGroupTypeDAO.findAllByKey("item_group_type_id","TAX")));
		req.setAttribute("itemGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(itemGroupsDAO.findAllByKey("status","A"))));
		//req.setAttribute("itemSubGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(new GenericDAO("item_sub_groups").findAllByKey("status","A"))));
		List <BasicDynaBean> itemSubGroupList = new TaxSubGroupRepository().getItemSubGroup(new java.sql.Date(new java.util.Date().getTime()));
		Iterator<BasicDynaBean> itemSubGroupListIterator = itemSubGroupList.iterator();
		List<BasicDynaBean> validateItemSubGrouList = new ArrayList<BasicDynaBean>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String currentDateStr = sdf.format(new java.util.Date());
		while(itemSubGroupListIterator.hasNext()) {
			BasicDynaBean itenSubGroupbean = itemSubGroupListIterator.next();
			if(itenSubGroupbean.get("validity_end") != null){
				Date endDate = (Date)itenSubGroupbean.get("validity_end");
				
				try {
					if(sdf.parse(currentDateStr).getTime() <= endDate.getTime()) {
						validateItemSubGrouList.add(itenSubGroupbean);
					}
				} catch (ParseException e) {
					continue;
				}
			} else {
				validateItemSubGrouList.add(itenSubGroupbean);
			}
		}
		req.setAttribute("itemSubGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(validateItemSubGrouList)));
		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		Map params = req.getParameterMap();
		List errors = new ArrayList();

		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		Object key1 = req.getParameter("old_drg_code");
		Map<String, String> keys = new HashMap<String, String>();
		keys.put("drg_code", key1.toString());
		FlashScope flash = FlashScope.getScope(req);
		Map filterMap = new HashMap();
		filterMap.put("drg_code", bean.get("drg_code"));
		List beans = null;
		if (errors.isEmpty()) {
			beans = dao.listAll(null, filterMap, null);
			BasicDynaBean exists = beans.size() > 0 ? (BasicDynaBean) beans.get(0): null;

			if ( exists != null && (!key1.equals(exists.get("drg_code")))  ) {
				flash.error( "DRG Code '" + bean.get("drg_code") +" already exists..");
				filterMap.put("drg_code", key1);
				beans = dao.listAll(null, filterMap, null);
			}
			else {
				String username = (String)req.getSession(false).getAttribute("userid");
				bean.set("username", username);
				int success = dao.update(con, bean.getMap(), keys);
				
				if(success > 0) {
					String drg_code = (String) bean.get("drg_code");
					success = updateItemSubGroup(drg_code,con,req);
				}

				if (success > 0) {
					con.commit();
					flash.success("DRG Code details updated successfully..");
					beans = dao.listAll(null, filterMap, null);

				} else {
					con.rollback();
					flash.error("Failed to update DRG Code details..");
					filterMap.put("drg_code", key1);
					beans = dao.listAll(null, filterMap, null);
				}
			}
		}
		else {
			flash.error("Incorrectly formatted values supplied");
		}

		bean = beans.size() > 0 ? (BasicDynaBean)beans.get(0): null;
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter("drg_code", bean.get("drg_code"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}

	public ActionForward exportDRGCodeDetailsToXls(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, java.io.IOException {

		List<String> drgCodeTableColumns = Arrays.asList(new String[] {
				"DRG_Code_dup_id", "DRG Code", "DRG Description", "Patient Type", "Relative Weight", "Status", "Code Type", "HCPCS Portion %"});

		Map<String, List> columnNamesMap = new HashMap<String, List>();
		columnNamesMap.put("mainItems", drgCodeTableColumns);
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet drgWorkSheet = workbook.createSheet("DRGCODES");
		List<BasicDynaBean> drgCodesList=DRGCodesMasterDAO.getDRGCodeDetails();
		HsSfWorkbookUtils.createPhysicalCellsWithValues(drgCodesList, columnNamesMap, drgWorkSheet, true);

		res.setHeader("Content-type", "application/vnd.ms-excel");
		res.setHeader("Content-disposition","attachment; filename=DRGCodeDetails.xls");
		res.setHeader("Readonly", "true");
		java.io.OutputStream os = res.getOutputStream();
		workbook.write(os);
		os.flush();
		os.close();
		return null;
	}

	public static DetailsImportExporter detailsImporExp;

	static {
		detailsImporExp = new DetailsImportExporter("drg_codes_master", "", "");
	}

	public StringBuilder errors;

	public ActionForward importDRGCodeDetailsFromXls(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {

		String referer = request.getHeader("Referer");
		referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
		ActionRedirect redirect = new ActionRedirect(referer);
		FlashScope flash = FlashScope.getScope(request);

		DRGCodesUploadForm drgForm = (DRGCodesUploadForm) form;
		ByteArrayInputStream byteStream = new ByteArrayInputStream(drgForm.getXlsDRGCodesFile().getFileData());
		XSSFWorkbook workBook = null;
		try {
			workBook = new XSSFWorkbook(byteStream);
		} catch (Exception e) {
			logger.error("", e);
			flash.put("error", "File format could be wrong or file itself is corrupted. Please upload the .xls file.");
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}
		XSSFSheet sheet = workBook.getSheetAt(0);

		this.errors = new StringBuilder();
		Map<String, String> aliasMap = new HashMap<String, String>();

		aliasMap.put("drg code", "drg_code");
		aliasMap.put("drg description", "drg_description");
		aliasMap.put("patient type", "patient_type");
		aliasMap.put("relative weight", "relative_weight");
		aliasMap.put("status", "status");
		aliasMap.put("code type", "code_type");
		aliasMap.put("hcpcs portion %", "hcpcs_portion_per");

		List<String> mandatoryList = Arrays.asList("drg_code", "drg_description", "patient_type", "relative_weight", "status", "code_type");
		List<String> oddFields = Arrays.asList("");

		detailsImporExp.setTableDbName("drg_code");
		detailsImporExp.setAliasUnmsToDBnmsMap(aliasMap);
		detailsImporExp.setId("drg_code_dup_id");
		detailsImporExp.setMandatoryFields(mandatoryList);
		detailsImporExp.setIsDateRequired(false);
		detailsImporExp.setIsUserNameRequired(true);
		detailsImporExp.setColumnNameForUser("username");
		detailsImporExp.setIsUserNameRequired(true);
		detailsImporExp.setOddFields(oddFields);
		detailsImporExp.setNameAsIdPattern(true);
		detailsImporExp.setExemptFromNullCheck(Arrays.asList("drg_code_dup_id"));
		detailsImporExp.setDeptNotExist(true);

		detailsImporExp.importDetailsToXls(sheet, null, errors, (String)request.getSession(false).getAttribute("userid"));

		if (this.errors.length() > 0)
			flash.put("error", this.errors);
		else
			flash.put("info", "File successfully uploaded.");
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}
	
	private boolean saveItemSubGroup(String drg_code, Connection con,HttpServletRequest request)
			throws SQLException, IOException{
			Map params = request.getParameterMap();
			List errors = new ArrayList();
			
			boolean flag = true;
			String [] itemSubgroupId = request.getParameterValues("item_subgroup_id");
			String [] delete = request.getParameterValues("deleted");
			
			if (errors.isEmpty()) {
				if(itemSubgroupId != null && itemSubgroupId.length > 0 && !itemSubgroupId[0].equals("")){
					BasicDynaBean itemsubgroupbean = drugCodeItemSubGroupsDAO.getBean();
					ConversionUtils.copyToDynaBean(params, itemsubgroupbean, errors);
					List records = drugCodeItemSubGroupsDAO.findAllByKey("drg_code", drg_code);
					if (records.size() > 0)
						flag = drugCodeItemSubGroupsDAO.delete(con, "drg_code", drg_code);
					
					for(int i=0; i<itemSubgroupId.length; i++){
						if(itemSubgroupId[i] != null && !itemSubgroupId[i].isEmpty()) {
							if (delete[i].equalsIgnoreCase("false")) {
								itemsubgroupbean.set("drg_code", drg_code);
								itemsubgroupbean.set("item_subgroup_id", Integer.parseInt(itemSubgroupId[i]));
								flag = drugCodeItemSubGroupsDAO.insert(con, itemsubgroupbean);
							}
						}
					}
				}	
			}
			return flag;

		}
	
	private int updateItemSubGroup(String drg_code, Connection con,HttpServletRequest request)
			throws SQLException, IOException{
			Map params = request.getParameterMap();
			List errors = new ArrayList();
			
			int flag = 1;
			String [] itemSubgroupId = request.getParameterValues("item_subgroup_id");
			String [] delete = request.getParameterValues("deleted");
			
			if (errors.isEmpty()) {
				if(itemSubgroupId != null && itemSubgroupId.length > 0 && !itemSubgroupId[0].equals("")){
					BasicDynaBean itemsubgroupbean = drugCodeItemSubGroupsDAO.getBean();
					ConversionUtils.copyToDynaBean(params, itemsubgroupbean, errors);
					List records = drugCodeItemSubGroupsDAO.findAllByKey("drg_code", drg_code);
					if (records.size() > 0)
						flag = (drugCodeItemSubGroupsDAO.delete(con, "drg_code", drg_code)) ? 1: 0;
					
					for(int i=0; i<itemSubgroupId.length; i++){
						if(itemSubgroupId[i] != null && !itemSubgroupId[i].isEmpty()) {
							if (delete[i].equalsIgnoreCase("false")) {
								itemsubgroupbean.set("drg_code", drg_code);
								itemsubgroupbean.set("item_subgroup_id", Integer.parseInt(itemSubgroupId[i]));
								flag = drugCodeItemSubGroupsDAO.insert(con, itemsubgroupbean) ? 1:0;
							}
						}
					}
					
				}	
			}
			return flag;

		}

}
