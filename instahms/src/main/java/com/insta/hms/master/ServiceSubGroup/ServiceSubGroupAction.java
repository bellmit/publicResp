package com.insta.hms.master.ServiceSubGroup;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.HsSfWorkbookUtils;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.ServiceMaster.ServiceDepartmentDAO;
import com.insta.hms.xls.exportimport.DetailsImportExporter;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

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

public class ServiceSubGroupAction extends BaseAction {
  
  private static final GenericDAO serviceGroupsDAO = new GenericDAO("service_groups");

	public ActionForward list(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		if (req.getParameter("ssg.service_group_id")!=null)
			req.setAttribute("service_group_id", req.getParameter("ssg.service_group_id"));
		if (req.getParameter("ssg.status")!=null)
			req.setAttribute("status", req.getParameter("ssg.status"));
		Map map= getParameterMap(req);
		PagedList list = ServiceSubGroupDAO.searchList(map, ConversionUtils.getListingParameter(map));
		req.setAttribute("pagedList", list);

		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		req.setAttribute("serviceSubGroupsList", js.serialize(ServiceSubGroupDAO.getAllServiceSubGroups()));
		req.setAttribute("serviceGroupsList", serviceGroupsDAO.listAll(null,"status","A",null));

		return m.findForward("addshow");
	}

	public ActionForward edit(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		req.setAttribute("serviceSubGroupsList", js.serialize(ServiceSubGroupDAO.getAllServiceSubGroups()));
		req.setAttribute("serviceGroupsList", serviceGroupsDAO.listAll(null,"status","A",null));

		if (req.getParameter("serviceSub_group_id")!=null) {
			ServiceSubGroupDAO dao = new ServiceSubGroupDAO();
			BasicDynaBean bean = dao.findByKey("service_sub_group_id", Integer.parseInt(req.getParameter("serviceSub_group_id")));
			req.setAttribute("bean", bean);
		}

		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException, Exception {

		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		HttpSession session = req.getSession();
		String userName = (String)session.getAttribute("userid");

		ServiceSubGroupDAO dao = new ServiceSubGroupDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		bean.set("username", userName);
		bean.set("mod_time", DateUtil.getCurrentTimestamp());

		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(m.findForward("addRedirect"));

		try {
			if (errors.isEmpty()) {
				BasicDynaBean exists = dao.getServiceSubGroup((String)bean.get("service_sub_group_name"), (Integer)bean.get("service_group_id"));
				if (exists == null) {
					bean.set("service_sub_group_id", dao.getNextSequence());
					boolean success = dao.insert(con, bean);
					if (success) {
						con.commit();
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						return redirect;
					} else {
						con.rollback();
						flash.error("Failed to add  Service Sub Group..");
					}
				} else {
					flash.error("Service Sub Group Name already exists..");
					redirect = new ActionRedirect(m.findForward("addRedirect"));
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					return redirect;
				}
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
		} finally {
			if (con!=null)
				con.close();
		}

		redirect = new ActionRedirect(m.findForward("addRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward update(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException, Exception {

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		Map params = req.getParameterMap();
		List errors = new ArrayList();
		HttpSession session = req.getSession();
		String userName = (String)session.getAttribute("userid");

		ServiceSubGroupDAO dao = new ServiceSubGroupDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		bean.set("username", userName);
		bean.set("mod_time", DateUtil.getCurrentTimestamp());
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));

		Object key = req.getParameter("service_sub_group_id");
		Map keys = new HashMap();
		keys.put("service_sub_group_id", Integer.parseInt(key.toString()));
		FlashScope flash = FlashScope.getScope(req);

		try {
			if (errors.isEmpty()) {
				int success = dao.update(con, bean.getMap(), keys);
				if (success > 0) {
					con.commit();
					redirect.addParameter("serviceSub_group_id", req.getParameter("service_sub_group_id"));
					return redirect;
				} else {
					con.rollback();
					flash.error("Failed to update Service Sub Group details..");
				}
			} else {
				flash.error("Incorrectly formatted values supplied..");
			}
		} finally {
			if (con!=null)
				con.close();
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward exportServiceSubGroupDetails(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)throws ServletException, IOException, SQLException {

		List<String> serviceSubGroupTableColumns = Arrays.asList(new String[] {
				"service_sub_group_id","service_sub_group_name", "service_group_name", "status", "display_order",
				"service_sub_group_code"});

		Map<String, List> columnNamesMap = new HashMap<String, List>();
		columnNamesMap.put("mainItems", serviceSubGroupTableColumns);
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet serviceGroupsWorkSheet = workbook.createSheet("SERVICES SUB GROUPS");
		List<BasicDynaBean> serviceSubGrpList = ServiceSubGroupDAO.getServiceSubGroupsDetails();
		HsSfWorkbookUtils.createPhysicalCellsWithValues(serviceSubGrpList, columnNamesMap, serviceGroupsWorkSheet, true);
		response.setHeader("Content-type", "application/vnd.ms-excel");
		response.setHeader("Content-disposition","attachment; filename=ServiceSubGroupDetails.xls");
		response.setHeader("Readonly", "true");
		java.io.OutputStream os = response.getOutputStream();
		workbook.write(os);
		os.flush();
		os.close();

		return null;
	}

	public StringBuilder errors;
	public static DetailsImportExporter detailsImporExp;

	static {
		detailsImporExp = new DetailsImportExporter("service_sub_groups", "", "");

	}

	public ActionForward importServiceSubGroupsDetails(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException, SQLException {
		ServiceSubGroupUploadForm serviceSubGroupForm = (ServiceSubGroupUploadForm) form;
		ByteArrayInputStream byteStream = new ByteArrayInputStream(serviceSubGroupForm.getXlsServiceSubGroupFile().getFileData());
		XSSFWorkbook workBook = new XSSFWorkbook(byteStream);
		XSSFSheet sheet = workBook.getSheetAt(0);

		this.errors = new StringBuilder();
		HashMap servDeptHashMap=ServiceDepartmentDAO.getServiceDepartmentHashMap();

		Map<String, String> aliasMap = new HashMap<String, String>();

		aliasMap.put("name", "service_sub_group_name");
		aliasMap.put("display order", "display_order");
		aliasMap.put("group code", "service_sub_group_code");
		aliasMap.put("service_group_name", "service_group_id");
		aliasMap.put("status", "status");

		List<String> charges = Arrays.asList("");
		List<String> mandatoryList = Arrays.asList("service_sub_group_name", "status", "display_order", "service_group_id");
		List<String> exemptFromNullCheck = Arrays.asList("service_sub_group_id");
		List<String> oddFields = Arrays.asList("service_sub_group_code");

		detailsImporExp.setTableDbName("service_sub_group_name");
		detailsImporExp.setAliasUnmsToDBnmsMap(aliasMap);
		detailsImporExp.setBed("");
		detailsImporExp.setCharges(charges);
		detailsImporExp.setOddFields(oddFields);
		detailsImporExp.setId("service_sub_group_id");
		detailsImporExp.setIdForOrgTab("");
		detailsImporExp.setOrgNameForChgTab("");
		detailsImporExp.setMandatoryFields(mandatoryList);
		detailsImporExp.setOrgId("");
		detailsImporExp.setIsDateRequired(true);
		detailsImporExp.setIsUserNameRequired(true);
		detailsImporExp.setDeptName("");
		detailsImporExp.setDeptMap(servDeptHashMap);
		detailsImporExp.setSerSubGrpName("");
		detailsImporExp.setSerGrpName("service_group_id");
		detailsImporExp.setOrderType("");
		detailsImporExp.setDbCodeName("");
		detailsImporExp.setIdForChgTab("");
		detailsImporExp.setColumnNameForDate("mod_time");
		detailsImporExp.setColumnNameForUser("username");
		detailsImporExp.setCodeAliasRequired(false);
		detailsImporExp.setDeptNotExist(true);
		detailsImporExp.setUsingHospIdPatterns(false);
		detailsImporExp.setUsingUniqueNumber(false);
		detailsImporExp.setExemptFromNullCheck(exemptFromNullCheck);

		String referer = request.getHeader("Referer");
		referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
		ActionRedirect redirect = new ActionRedirect(referer);
		FlashScope flash = FlashScope.getScope(request);

		detailsImporExp.importDetailsToXls(sheet, null, errors, (String)request.getSession(false).getAttribute("userid"));

		if (this.errors.length() > 0)
			flash.put("error", this.errors);
		else
			flash.put("info", "File successfully uploaded");
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;

	}

}