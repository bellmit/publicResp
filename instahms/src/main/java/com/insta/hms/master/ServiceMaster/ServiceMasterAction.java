package com.insta.hms.master.ServiceMaster;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.adminmasters.organization.RateMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.HsSfWorkbookUtils;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.CommonUtils;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.jobs.MasterChargesCronSchedulerDetailsRepository;
import com.insta.hms.master.ServiceSubGroup.ServiceSubGroupDAO;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupRepository;
import com.insta.hms.resourcescheduler.ResourceDAO;
import com.insta.hms.xls.exportimport.ChargesImportExporter;
import com.insta.hms.xls.exportimport.DetailsImportExporter;

import flexjson.JSONSerializer;

/*
 * Tables involved:
 *   services: all service definitions (service_id, name, etc.)
 *     -> ServiceMasterDAO
 *   service_org_details: item_code and applicable flag per service-org_id
 *     -> ServiceOrgDetailsDAO
 *   service_master_charges: charges on a service_id,org_id,bed_type basis
 *     -> ServiceChargeDAO
 *   services_departments: single column list of service department names (no IDs)
 *     -> ServiceDepartmentDAO
 *
 *  Forms:
 *   We use ServiceUploadForm only for the CSV upload. All other submits use req.getParameter
 *   directly. For this reason, the action mapping for the upload alone is different.
 *   The reason is that ActionForm is the most convenient way to deal with multipart/form-data
 */

public class ServiceMasterAction extends DispatchAction {

	static Logger logger = LoggerFactory.getLogger(ServiceMasterAction.class);
	
	private static final GenericDAO serviceSubTasksDAO = new GenericDAO("service_sub_tasks");

	/*
	 * Lists all the services as a filtered search result. (pages/master/ServiceMaster/list.jsp)
	 */
	private static Map<String, String> aliasMap;
	static {
		aliasMap = new HashMap<String, String>();
			aliasMap.put("department", "serv_dept_id");
			aliasMap.put("specialization_name", "specialization");
	}

	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, Exception {

		ServiceMasterDAO dao = new ServiceMasterDAO();
		ServiceChargeDAO cdao = new ServiceChargeDAO();

		Map requestParams = new HashMap();
		requestParams.putAll(req.getParameterMap());
		String orgId = req.getParameter("org_id");
		if ( (orgId == null) || orgId.equals("")) {
			String[] org_id = {"ORG0001"};
			requestParams.put("org_id", org_id);
			orgId = "ORG0001";
		}

		PagedList list = dao.search(requestParams,
					ConversionUtils.getListingParameter(req.getParameterMap()));

		List<String> ids = new ArrayList<String>();
		for (Map obj : (List<Map>) list.getDtoList()) {
			ids.add((String) obj.get("service_id"));
		}

		List<String> bedTypes = new BedMasterDAO().getUnionOfBedTypes();
		List chargeList = cdao.getAllChargesForBedTypes(orgId, bedTypes, ids);
		Map chargesMap = ConversionUtils.listBeanToMapMap(chargeList, "service_id");

		JSONSerializer js = new JSONSerializer().exclude("class");

		req.setAttribute("pagedList", list);
		req.setAttribute("bedTypes", bedTypes);
		req.setAttribute("charges", chargesMap);
		req.setAttribute("namesJSON", js.serialize(dao.getAllNames()));
		req.setAttribute("org_id", orgId);
		MasterChargesCronSchedulerDetailsRepository masterChargesCronSchedulerDetailsRepository =
	            ApplicationContextProvider.getBean(MasterChargesCronSchedulerDetailsRepository.class);
        Map<String, Object> cronJobKeys = new HashMap<String, Object>();
        cronJobKeys.put("entity", "SERVICE");
        ArrayList<String> status = new ArrayList<String>();
        status.add("F");
        status.add("P");
        cronJobKeys.put("status", status);
        List<BasicDynaBean> masterCronJobDetails =
            masterChargesCronSchedulerDetailsRepository.findByCriteria(cronJobKeys);
        req.setAttribute("masterCronJobDetails", ConversionUtils.listBeanToListMap(masterCronJobDetails));

		return mapping.findForward("list");
	}

	/*
	 * add: returns the "add" screen for adding a new item
	 */
	public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws SQLException {
		return addShow(mapping, form, req, res);
	}

	/*
	 * show: returns the "edit" screen for showing/editing an existing item.
	 */
	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws SQLException {
		String serviceId = req.getParameter("service_id");
		req.setAttribute("servicesubtaskList" ,new ServiceMasterDAO().getServiceSubTasksDetails(serviceId));
		return addShow(mapping, form, req, res);
	}

	/*
	 * Common method for add/show. Note: this is private scope.
	 */
	private ActionForward addShow(ActionMapping mapping, ActionForm form , HttpServletRequest req,
			HttpServletResponse res) throws SQLException {

		ServiceMasterDAO dao = new ServiceMasterDAO();
		ServiceChargeDAO cdao = new ServiceChargeDAO();

		String orgId = req.getParameter("org_id");
		int centerId = (Integer)req.getSession(false).getAttribute("centerId");
		if ( (orgId == null) || orgId.isEmpty() ) {
			orgId = "ORG0001";
		}

		String method = req.getParameter("_method");

		List<String> bedTypes = new BedMasterDAO().getUnionOfBedTypes();
		List<String> departments = new ServiceDepartmentDAO().getColumnList("department");

		JSONSerializer js = new JSONSerializer().exclude("class");
		req.setAttribute("bedTypes", bedTypes);
		req.setAttribute("departmentsJSON", js.serialize(departments));
		Map hl7Filtermap = new HashMap();
		hl7Filtermap.put("status", "A");
		hl7Filtermap.put("interface_type", "SERVICE");
		req.setAttribute("hl7Interfaces", new GenericDAO("hl7_lab_interfaces").
                listAll(null, hl7Filtermap,null));
		
		if (method.equals("show")) {
			
			String serviceId = req.getParameter("service_id");
			req.setAttribute("interfaceNames", new GenericDAO("services_export_interface").findAllByKey("service_id",serviceId ));
			BasicDynaBean bean = dao.getServiceDetails(serviceId, orgId);
			String groupId = new ServiceSubGroupDAO().findByKey("service_sub_group_id", bean.get("service_sub_group_id")).get("service_group_id").toString();
			req.setAttribute("groupId", groupId);
			List<BasicDynaBean> activeInsurance = dao.getActiveInsuranceCategories(serviceId);
			List<BasicDynaBean> mappedResourceList = dao.getMappedServiceResources(serviceId, centerId);
			StringBuilder activeInsuranceCategories = new StringBuilder();
			for (BasicDynaBean activeInsuranceCategory : activeInsurance) {
			activeInsuranceCategories.append(activeInsuranceCategory.get("insurance_category_id"));
			activeInsuranceCategories.append(",");
			}
			req.setAttribute("insurance_categories", activeInsuranceCategories.toString());
		    List<Integer> selectedServiceResources = new ArrayList<>();
		    for (BasicDynaBean serviceResource : mappedResourceList) {
		      selectedServiceResources.add((Integer) serviceResource.get("serv_res_id"));
		    }
		    req.setAttribute("selectedServiceResource", selectedServiceResources);

			req.setAttribute("centerId", centerId);
			List<String> idList = new ArrayList();
			idList.add(serviceId);
			List<BasicDynaBean> chargeList = cdao.getAllChargesForBedTypes(orgId, bedTypes, idList);
			List<BasicDynaBean> discountList = cdao.getAllDiscountsForBedTypes(orgId, bedTypes, idList);
			req.setAttribute("bean", bean);
			req.setAttribute("charges", chargeList.get(0).getMap());
			req.setAttribute("discounts", discountList.get(0).getMap());
			req.setAttribute("service_default_duration", -1);
			req.setAttribute("method", "update");
			req.setAttribute("serviceList", js.serialize(dao.getServicesNamesAndIds()));
			req.setAttribute("conductingRoleIds", CommonUtils.getStringArrayFromCommaSeparatedString((String)bean.get("conducting_role_id")));
			req.setAttribute("taxsubgroup", ConversionUtils.copyListDynaBeansToMap(dao.getServiceItemSubGroupDetails(serviceId)));
		} else {
			req.setAttribute("method", "create");
      req.setAttribute("service_default_duration", new ResourceDAO().getDefaultDurationService());
		}
		List<BasicDynaBean> serviceResourceList = dao.getServiceResourcesByCenter(centerId);
		req.setAttribute("service_resources", serviceResourceList);
		req.setAttribute("serviceSubGroupsList", js.serialize(ServiceSubGroupDAO.getAllActiveServiceSubGroups()));
		req.setAttribute("itemGroupTypeList", ConversionUtils.listBeanToListMap(new GenericDAO("item_group_type").findAllByKey("item_group_type_id","TAX")));
		req.setAttribute("itemGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(new GenericDAO("item_groups").findAllByKey("status","A"))));
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
		return mapping.findForward("addshow");
	}

	public ActionForward showCharges(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)throws SQLException {

		ServiceMasterDAO dao = new ServiceMasterDAO();
		ServiceChargeDAO cdao = new ServiceChargeDAO();

		String orgId = request.getParameter("org_id");
		if ( (orgId == null) || orgId.isEmpty() ) {
			orgId = "ORG0001";
		}

		String serviceId = request.getParameter("service_id");
		BasicDynaBean bean = dao.getServiceDetails(serviceId, orgId);
		List<String> bedTypes = new BedMasterDAO().getUnionOfBedTypes();
		List<String> idList = new ArrayList();

		JSONSerializer js = new JSONSerializer().exclude("class");
		idList.add(serviceId);
		List<BasicDynaBean> chargeList = cdao.getAllChargesForBedTypes(orgId, bedTypes, idList);
		List<BasicDynaBean> discountList = cdao.getAllDiscountsForBedTypes(orgId, bedTypes, idList);

		List<BasicDynaBean> derivedRatePlanDetails = cdao.getDerivedRatePlanDetails(orgId, serviceId);

		if(derivedRatePlanDetails.size()<0)
        	request.setAttribute("derivedRatePlanDetails", js.serialize(Collections.EMPTY_LIST));
        else
        	request.setAttribute("derivedRatePlanDetails", js.serialize(ConversionUtils.copyListDynaBeansToMap(derivedRatePlanDetails)));

		request.setAttribute("bean", bean);
		request.setAttribute("charges", chargeList.get(0).getMap());
		request.setAttribute("discounts", discountList.get(0).getMap());
		request.setAttribute("bedTypes", bedTypes);
		request.setAttribute("method", "updateCharges");
		request.setAttribute("serviceList", js.serialize(dao.getServicesNamesAndIds()));

		return mapping.findForward("showcharges");
	}

	/*
	 * create: POST method to create a new service
	 */
	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, IOException, Exception {
		Map params = req.getParameterMap();
		ServiceMasterDAO sdao = new ServiceMasterDAO();
		ServiceChargeDAO cdao = new ServiceChargeDAO();
		HttpSession session = req.getSession();
    	String userName = (String)session.getAttribute("userid");
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect=new ActionRedirect(mapping.findForward("addRedirect"));
		String[] conductingRoles = req.getParameterValues("conductingRoleId");
		String[] subtask_delete = (String[]) params.get("service_subtask_deleted");
		String[] subtask_edited = (String[]) params.get("service_subtask_edited");
		String[] subtask_short = (String[]) params.get("desc_short");
		String[] subtask_status = (String[]) params.get("subtask_status");

		Connection con = null;
		boolean success = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map keys = new HashMap();
			String error = null;
			List errors = new ArrayList();
			String [] subtaskIds = (String[])params.get("sub_task_id");

			BasicDynaBean servicesubtask = serviceSubTasksDAO.getBean();
			BasicDynaBean service = sdao.getBean();
			service.set("username", userName);
			
			if (null == req.getParameter("service_sub_group_id")) {
				flash.put("error", "Service Sub Group is required");
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}

			ConversionUtils.copyToDynaBean(req.getParameterMap(), service, errors, true);
			if(req.getParameter("specialization")!=null && !req.getParameter("specialization").equals("D") && !req.getParameter("specialization").equals("I"))
				service.set("specialization", null);

			String serviceId = sdao.getNextId();
			service.set("service_id", serviceId);
			service.set("conducting_role_id", CommonUtils.getCommaSeparatedString(conductingRoles));
			keys.put("service_id", service.get("service_id"));
			sdao.update(con, service.getMap(), keys);

			if (!errors.isEmpty()) {
				flash.put("error", "Incorrectly formatted values supplied");
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}

				BasicDynaBean serviceDeptBean  = new GenericDAO("services_departments").
					findByKey("serv_dept_id", service.get("serv_dept_id"));

			// 1. Insert the service
				success = sdao.insert(con, service);

				if(success) {
					success = saveOrUpdateInsuranceCategory(serviceId, con, req);
				}

				// update Service Resource Mapping
        if (success) {
          success = createOrUpdateServiceResourceMapping(serviceId, con, req);
        }

				// 1.save or update itemsubgroup(tax sub group) based on service
				if(success) {
					String service_id = (String) service.get("service_id");
					success = saveOrUpdateItemSubGroup(service_id,con,req);
				}
				
				if (!success) {
					flash.put("error", "Service with the same name already exists");
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						return redirect;
				}

				success &= cdao.initServiceItemCharges(con, serviceId, userName);

				if (subtaskIds != null) {
					for(int i=0; i<subtaskIds.length;i++) {
						if (!subtaskIds[i].equals("")&& subtaskIds[i].equals("_")) {
							servicesubtask = serviceSubTasksDAO.getBean();
							ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i, servicesubtask, errors);
							if (errors.isEmpty()) {
								servicesubtask.set("sub_task_id", serviceSubTasksDAO.getNextSequence());
								servicesubtask.set("display_order",servicesubtask.get("display_order")==null?0:servicesubtask.get("display_order"));
								servicesubtask.set("status",subtask_status[i]);
								if (service != null)
									servicesubtask.set("service_id", service.get("service_id"));
								serviceSubTasksDAO.insert(con, servicesubtask);
							}
						}
					}
				}


			if (success) {
				sdao.insertHl7Interfaces(con,serviceId, req.getParameterValues("interface_name"));
				flash.put("success", "Service inserted successfully");
			} else {
				flash.put("error", "Service failed to insert successfully");
			}


			redirect=new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter("service_id", serviceId);
			redirect.addParameter("org_id", req.getParameter("org_id"));

			} finally {
				DataBaseUtil.commitClose(con, success);
			}
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}

	/*
	 * update: POST method to update an existing service and charges.
	 */
	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, IOException {
		Map params =req.getParameterMap();
		ServiceMasterDAO sdao = new ServiceMasterDAO();
		ServiceOrgDetailsDAO odao = new ServiceOrgDetailsDAO();
		ServiceChargeDAO cdao = new ServiceChargeDAO();
		ServiceDepartmentDAO ddao = new ServiceDepartmentDAO();
		String[] subtaskIds = (String[])params.get("sub_task_id");
		String[] subtask_delete = (String[]) params.get("service_subtask_deleted");
		String[] subtask_edited = (String[]) params.get("service_subtask_edited");
		String[] subtask_short = (String[]) params.get("desc_short");
		String[] subtask_status = (String[]) params.get("subtask_status");

		HttpSession session = req.getSession();
    	String userName = (String)session.getAttribute("userid");
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect= new ActionRedirect(mapping.findForward("showRedirect"));
		String[] conductingRoles = req.getParameterValues("conductingRoleId");

		ArrayList errors = new ArrayList();
		BasicDynaBean service = sdao.getBean();
		BasicDynaBean orgDetails = odao.getBean();
		BasicDynaBean servicesubtask = null;
		service.set("username", userName);

		ConversionUtils.copyToDynaBean(req.getParameterMap(), service, errors, true);
		if(req.getParameter("specialization")!=null && !req.getParameter("specialization").equals("D") && !req.getParameter("specialization").equals("I"))
			service.set("specialization", null);
		service.set("conducting_role_id", CommonUtils.getCommaSeparatedString(conductingRoles));
		String serviceId = req.getParameter("service_id");
		ConversionUtils.copyToDynaBean(req.getParameterMap(), orgDetails, errors);

		if (!errors.isEmpty()) {
			flash.put("error", "Incorrectly formatted values supplied");
		}

		Connection con = null;
		boolean allSuccess = false;
		int serv_dept_id=0;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);


			if (subtask_delete != null) {
				for(int i=0; i<subtask_delete.length;i++) {
					if (subtask_delete[i].equals("false")) {
						if(subtaskIds[i] != null && subtaskIds[i].equals("_")){
							servicesubtask = serviceSubTasksDAO.getBean();
							ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i, servicesubtask, errors);
							if (errors.isEmpty()) {
								servicesubtask.set("sub_task_id", serviceSubTasksDAO.getNextSequence());
								servicesubtask.set("display_order",servicesubtask.get("display_order")==null?0:servicesubtask.get("display_order"));
								servicesubtask.set("service_id",service.get("service_id"));
								servicesubtask.set("status",subtask_status[i]);
								serviceSubTasksDAO.insert(con, servicesubtask);
							}
						} else if (subtaskIds != null && !subtaskIds[i].equals("")) {
							Map keys1 = new HashMap();
							keys1.put("sub_task_id", Integer.parseInt(subtaskIds[i]));
							servicesubtask = serviceSubTasksDAO.getBean();
							ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i, servicesubtask, errors);
							if (errors.isEmpty()) {
								servicesubtask.set("sub_task_id", Integer.parseInt(subtaskIds[i]));
								servicesubtask.set("service_id", service.get("service_id"));
								servicesubtask.set("display_order",servicesubtask.get("display_order")==null?0:servicesubtask.get("display_order"));
								servicesubtask.set("desc_short",subtask_short[i]);
								servicesubtask.set("status",subtask_status[i]);
							}	serviceSubTasksDAO.update(con, servicesubtask.getMap(), keys1);

						}
					} else if (subtask_delete[i].equals("true") && subtaskIds!= null && !subtaskIds[i].equals("")) {
						serviceSubTasksDAO.delete(con, "sub_task_id", Integer.parseInt(subtaskIds[i]));
					}
				}
			}

			// 1. Update the service

			boolean success = (1 == sdao.update(con, service.getMap(), "service_id", serviceId));

			if(success) {
			  success = saveOrUpdateInsuranceCategory(serviceId, con, req);
			}

			// update service resource mapping
	     if (success) {
	        success = createOrUpdateServiceResourceMapping(serviceId, con, req);
	      }

			if(success) {
				String service_id = (String) service.get("service_id");
				success = saveOrUpdateItemSubGroup(service_id,con,req);
			}
			
			if (!success) {
				flash.put("error", "Service with the same name already exists");
			}

			odao.updateWithNames(con, orgDetails.getMap(), new String[] {"service_id", "org_id"});

			allSuccess = true;
			sdao.updateHl7Interface(con, serviceId, req.getParameterValues("interface_name"));
			flash.put("success", "Service updated successfully");

		} finally {
			DataBaseUtil.commitClose(con, allSuccess);
		}
		redirect.addParameter("service_id", req.getParameter("service_id"));
		redirect.addParameter("org_id", req.getParameter("org_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward updateCharges(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)throws SQLException, IOException,Exception {

		ServiceChargeDAO cdao = new ServiceChargeDAO();
		String[] beds = request.getParameterValues("bed_type");
		HttpSession session = request.getSession();
		String userName = (String)session.getAttribute("userid");

		String orgId = request.getParameter("org_id");
        String serviceId = request.getParameter("service_id");

		String[] derivedRateplanIds = request.getParameterValues("ratePlanId");
        String[] ratePlanApplicable = request.getParameterValues("applicable");
        String[] unitChg = request.getParameterValues("unit_charge");
        String[] serDiscount = request.getParameterValues("discount");

        Double[] unitCharge = new Double[unitChg.length];
        Double[] discounts = new Double[serDiscount.length];

        ActionRedirect redirect= new ActionRedirect(mapping.findForward("showChargesRedirect"));
		FlashScope flash = FlashScope.getScope(request);

        for(int i=0; i<unitChg.length; i++) {
        	unitCharge[i] = new Double(unitChg[i]);
        	discounts[i] = new Double(serDiscount[i]);
        }

		ArrayList errors = new ArrayList();
		Connection con = null;
		boolean allSuccess = false;
		ServiceOrgDetailsDAO odao = new ServiceOrgDetailsDAO();
		BasicDynaBean orgDetails = odao.getBean();

		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			ConversionUtils.copyToDynaBean(request.getParameterMap(), orgDetails, errors);
			orgDetails.set("applicable", true);
			odao.updateWithNames(con, orgDetails.getMap(), new String[] {"service_id", "org_id"});

			List<BasicDynaBean> chargeList = new ArrayList();
			for (int i=0; i<beds.length; i++) {
				BasicDynaBean charge = cdao.getBean();
				ConversionUtils.copyToDynaBean(request.getParameterMap(), charge, errors);
				ConversionUtils.copyIndexToDynaBean(request.getParameterMap(), i, charge, errors);
				charge.set("username", userName);
				chargeList.add(charge);
			}

			if (!errors.isEmpty()) {
				flash.put("error", "Incorrectly formatted values supplied");
			}

			for (BasicDynaBean c: chargeList) {
				cdao.updateWithNames(con, c.getMap(), new String[] {"service_id", "org_id", "bed_type"});
			}

			 if(null != derivedRateplanIds && derivedRateplanIds.length > 0) {
				 allSuccess = cdao.updateOrgForDerivedRatePlans(con,derivedRateplanIds,ratePlanApplicable,serviceId);
				 allSuccess = cdao.updateChargesForDerivedRatePlans(con,orgId,derivedRateplanIds,beds,
						 unitCharge, serviceId, discounts,ratePlanApplicable);
		        }
			RateMasterDao rdao = new RateMasterDao();
			List<BasicDynaBean> allDerivedRatePlanIds = rdao.getDerivedRatePlanIds(orgId);
			if(null != allDerivedRatePlanIds) {
				cdao.updateApplicableflagForDerivedRatePlans(con, allDerivedRatePlanIds, "services", "service_id",
						serviceId, "service_org_details", orgId);
			}

			allSuccess = true;

		} finally {
			DataBaseUtil.commitClose(con, allSuccess);
		}

		redirect.addParameter("service_id", request.getParameter("service_id"));
		redirect.addParameter("org_id", request.getParameter("org_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	/*
	 * Group Update: called from the main list screen, updates the charges of all/selected
	 * services by a formula: +/- a certain amount or percentage,
	 */
	public ActionForward groupUpdate(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {

		FlashScope flash = FlashScope.getScope(req);

		ActionRedirect redirect = new ActionRedirect(req.getHeader("Referer").
				replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		String orgId = req.getParameter("org_id");
		String amtType = req.getParameter("amtType");
		String incType = req.getParameter("incType");
		String updateTable = req.getParameter("updateTable");
		String userName = (String)req.getSession().getAttribute("userid");

		String allServices = req.getParameter("allServices");
		String allBedTypes = req.getParameter("allBedTypes");

		List<String> services = null;
		if (!allServices.equals("yes"))
			services = ConversionUtils.getParamAsList(req.getParameterMap(), "selectService");

		List<String> bedTypes = null;
		if (allBedTypes == null)
			bedTypes = ConversionUtils.getParamAsList(req.getParameterMap(), "selectBedType");

		BigDecimal amount;
		BigDecimal round;
		try {
			amount = new BigDecimal(req.getParameter("amount"));
			round = new BigDecimal(req.getParameter("round"));
		} catch (NumberFormatException e) {
			flash.put("error", "Incorrectly formatted parameters");
			return redirect;
		}

		if (incType.equals("-"))
			amount = amount.negate();

		ServiceChargeDAO dao = new ServiceChargeDAO();

		Connection con = null;
		boolean success = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			dao.groupIncreaseCharges(con, orgId, bedTypes, services, amount, amtType.equals("%"),
					round, updateTable, (String)req.getSession(false).getAttribute("userid"));
			success = true;
			if(success)con.commit();
		} finally {
			DataBaseUtil.commitClose(con, success);
		}

		if(success)
			dao.updateChargesForDerivedRatePlans(orgId,userName,"services",false);

		if (success)
			flash.put("success", "Charges updated successfully");
		else
			flash.put("error", "Error updating charges");

		return redirect;
	}

	private static ChargesImportExporter importExporter;

	static {

		importExporter = new ChargesImportExporter("services", "service_org_details",
				"service_master_charges", "services_departments", "service_id", "serv_dept_id", "department",
				new String[] {"service_name", "status"}, new String[] {"Service Name", "Status"},
				new String[] {"applicable", "item_code","special_service_code","special_service_contract_name"}, new String[] {"Applicable", "Code","PackageID","Package Contract Name"},
				new String[] {"unit_charge", "discount"}, new String[] {"Charge", "Discount"});

		importExporter.setItemWhereFieldKeys(new String[] {"service_id"});
		importExporter.setOrgWhereFieldKeys(new String[] {"service_id", "org_id"});
		importExporter.setChargeWhereFieldKeys(new String[] {"service_id", "org_id"});
		importExporter.setMandatoryFields(new String[] {"service_name"});
		importExporter.setItemName("service_name");
	}


	public ActionForward exportChargesToXls(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {

		String orgId = request.getParameter("org_id");
		String orgName = (String)OrgMasterDao.getOrgdetailsDynaBean(orgId).get("org_name");
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet workSheet = workbook.createSheet("SERVICE CHARGES");
		importExporter.exportCharges(orgId, workSheet, null, "A");

		response.setHeader("Content-type", "application/vnd.ms-excel");
		response.setHeader("Content-disposition","attachment; filename="+"\"ServiceCharges_"+orgName+".xls\"");
		response.setHeader("Readonly", "true");
		java.io.OutputStream outputStream = response.getOutputStream();
		workbook.write(outputStream);
		outputStream.flush();
		outputStream.close();

		return null;
	}

	public ActionForward importChargesFromXls(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, Exception {

		ServiceUploadForm suForm = (ServiceUploadForm) form;
		String orgId = suForm.getOrg_id();
		XSSFWorkbook workBook = new XSSFWorkbook(suForm.getXlsServiceFile().getInputStream());
		XSSFSheet sheet = workBook.getSheetAt(0);
		this.errors = new StringBuilder();

		FlashScope flash = FlashScope.getScope(request);
		String referer = request.getHeader("Referer");
		referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
		ActionRedirect redirect = new ActionRedirect(referer);
		Connection con = DataBaseUtil.getConnection();
		ServiceChargeDAO cdao = new ServiceChargeDAO();
		String userName = (String) request.getSession().getAttribute("userid");
		/*
		 * Keep a backup of the rates for safety: TODO: be able to checkpoint and revert
		 * to a previous version if required.
		 */
		cdao.backupCharges(con, orgId, userName);
		if (con != null)
			con.close();

		importExporter.setUseAuditLogHint(true);
		importExporter.importCharges(true, orgId, sheet, userName, this.errors);

		cdao.updateChargesForDerivedRatePlans(orgId,userName,"services",true);

		if (this.errors.length() > 0)
			flash.put("error", this.errors);
		else
			flash.put("info", "File successfully uploaded");

		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	/*
	 * Export the set of charges for this rate plan, all bed types as a CSV. Called from the main
	 * list screen. Every bed type is a column.
	 */
	public ActionForward exportChargesCSV(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, java.io.IOException {

		String orgId = req.getParameter("org_id");
		BasicDynaBean orgBean = OrgMasterDao.getOrgdetailsDynaBean(orgId);
		String orgName = (String) orgBean.get("org_name");

		res.setHeader("Content-type","application/csv");
		res.setHeader("Content-disposition","attachment; filename=ServiceRates_" + orgName + ".csv");
		res.setHeader("Readonly","true");

		CSVWriter writer = new CSVWriter(res.getWriter(),CSVWriter.DEFAULT_SEPARATOR);

		List<String> allBedTypes = new BedMasterDAO().getUnionOfBedTypes();
		ServiceChargeDAO dao = new ServiceChargeDAO();
		dao.getAllChargesForBedTypesCSV(orgId, allBedTypes, writer);

		return null;
	}


	public ActionForward exportServiceDetailsToXls(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, java.io.IOException {

		List<String> serviceColumnTableColumns = Arrays.asList(new String[] {
									"service_id","Service Name","Units","Service Tax","Status",
									"Conduction Applicable","Reporting Activity Timing In E-claim","Service Sub Group","Service Group",
									"Specialization Name","Department","Alias","service_duration" });

		 Map<String, List> columnNamesMap = new HashMap<String, List>();
		 columnNamesMap.put("mainItems", serviceColumnTableColumns);
		 XSSFWorkbook workbook = new XSSFWorkbook();
		 XSSFSheet servicesWorkSheet = workbook.createSheet("SERVICES");
		 List<BasicDynaBean> serviceList=ServiceMasterDAO.getServiceDetails();
		 HsSfWorkbookUtils.createPhysicalCellsWithValues(serviceList, columnNamesMap, servicesWorkSheet, true);
		 res.setHeader("Content-type", "application/vnd.ms-excel");
		 res.setHeader("Content-disposition","attachment; filename=ServiceDefinationDetails.xls");
		 res.setHeader("Readonly", "true");
		 java.io.OutputStream os = res.getOutputStream();
		 workbook.write(os);
		 os.flush();
		 os.close();
		return null;
	}

	public static DetailsImportExporter detailsImporExp;

	static {
		detailsImporExp = new DetailsImportExporter("services", "service_org_details", "service_master_charges");

	}

	public ActionForward importServiceDetailsFromXls(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {


		ServiceUploadForm serviceForm = (ServiceUploadForm) form;
		ByteArrayInputStream byteStream = new ByteArrayInputStream(serviceForm.getXlsServiceFile().getFileData());
		XSSFWorkbook workBook = new XSSFWorkbook(byteStream);
		XSSFSheet sheet = workBook.getSheetAt(0);

		this.errors = new StringBuilder();
		HashMap servDeptHashMap=ServiceDepartmentDAO.getServiceDepartmentHashMap();

		Connection con = null;
		boolean success = false;
		Map<String, String> aliasMap = new HashMap<String, String>();

		aliasMap.put("service name", "service_name");
		aliasMap.put("units", "units");
		aliasMap.put("service tax", "service_tax");
		aliasMap.put("code", "service_code");
		aliasMap.put("status", "status");
		aliasMap.put("conduction applicable", "conduction_applicable");
		aliasMap.put("specialization name", "specialization");
		aliasMap.put("department", "serv_dept_id");
		aliasMap.put("service sub group", "service_sub_group");
		aliasMap.put("service group", "service_group");
		aliasMap.put("alias", "service_code");
		aliasMap.put("service duration", "service_duration");
		aliasMap.put("reporting activity timing in e-claim", "activity_timing_eclaim");

		List<String> charges = Arrays.asList("unit_charge");
		List<String> mandatoryList = Arrays.asList("service_name", "status", "serv_dept_id", "service_sub_group", "service_group","service_duration");
		List<String> exemptFromNullCheck = Arrays.asList("service_id");
		List<String> oddFields = Arrays.asList("service_code");

		detailsImporExp.setTableDbName("service_name");
		detailsImporExp.setAliasUnmsToDBnmsMap(aliasMap);
		detailsImporExp.setBed("bed_type");
		detailsImporExp.setCharges(charges);
		detailsImporExp.setOddFields(oddFields);
		detailsImporExp.setId("service_id");
		detailsImporExp.setIdForOrgTab("service_id");
		detailsImporExp.setOrgNameForChgTab("org_id");
		detailsImporExp.setMandatoryFields(mandatoryList);
		detailsImporExp.setOrgId("org_id");
		detailsImporExp.setType("Services");
		detailsImporExp.setDeptName("serv_dept_id");
		detailsImporExp.setDeptMap(servDeptHashMap);
		detailsImporExp.setSerSubGrpName("service_sub_group");
		detailsImporExp.setSerGrpName("service_group");
		detailsImporExp.setOrderType("Service");
		detailsImporExp.setDbCodeName("service_code");
		detailsImporExp.setIdForChgTab("service_id");
		detailsImporExp.setCodeAliasRequired(true);
		detailsImporExp.setDeptNotExist(false);
		detailsImporExp.setUsingHospIdPatterns(false);
		detailsImporExp.setUsingUniqueNumber(true);
		detailsImporExp.setUsingSequencePattern(false);
		detailsImporExp.setIsDateRequired(false);
		detailsImporExp.setIsUserNameRequired(false);
		detailsImporExp.setExemptFromNullCheck(exemptFromNullCheck);
		detailsImporExp.setColumnNameForUser("username");
		detailsImporExp.setIsUserNameRequired(true);
		detailsImporExp.setUseAuditLogHint(true);

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

	public StringBuilder errors;

	public ActionForward getorgName(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)throws ServletException, IOException, SQLException {

		String orgId = request.getParameter("org_id");
		BasicDynaBean orgBean = OrgMasterDao.getOrgdetailsDynaBean(orgId);
		String orgName = (String) orgBean.get("org_name");

		response.setContentType("text/plain");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		response.getWriter().write(orgName);
		return null;
	}
	
	private boolean saveOrUpdateItemSubGroup(String service_id, Connection con,HttpServletRequest request)
			throws SQLException, IOException{
			Map params = request.getParameterMap();
			List errors = new ArrayList();
			
			boolean flag = true;
			String [] itemSubgroupId = request.getParameterValues("item_subgroup_id");
			String [] delete = request.getParameterValues("deleted");
			
			if (errors.isEmpty()) {
				if(itemSubgroupId != null && itemSubgroupId.length > 0 && !itemSubgroupId[0].equals("")){
					GenericDAO itemsubgroupdao = new GenericDAO("service_item_sub_groups");
					BasicDynaBean itemsubgroupbean = itemsubgroupdao.getBean();
					ConversionUtils.copyToDynaBean(params, itemsubgroupbean, errors);
					List records = itemsubgroupdao.findAllByKey("service_id", service_id);
					if (records.size() > 0)
						flag = itemsubgroupdao.delete(con, "service_id", service_id);
					
					for(int i=0; i<itemSubgroupId.length; i++){
						if(itemSubgroupId[i] != null && !itemSubgroupId[i].isEmpty()) {
							if (delete[i].equalsIgnoreCase("false")) {
								itemsubgroupbean.set("service_id", service_id);
								itemsubgroupbean.set("item_subgroup_id", Integer.parseInt(itemSubgroupId[i]));
								flag = itemsubgroupdao.insert(con, itemsubgroupbean);
							}
						}
					}
				}	
			}
			return flag;

		}

/**
  * Save or update Insurance Category.
  *
  * @param serviceId
  *          the service id
  * @param request
  *          the request
  * @return true, if successful
  */
  private boolean saveOrUpdateInsuranceCategory(String serviceId,
      Connection con, HttpServletRequest request) throws SQLException, IOException {
    boolean flag = true;
    String[] insuranceCategories = request.getParameterValues("insurance_category_id");
    if (insuranceCategories != null && insuranceCategories.length > 0
        && !insuranceCategories[0].equals("")) {
      GenericDAO insuranceCategoryDAO =
          new GenericDAO("service_insurance_category_mapping");
      BasicDynaBean insuranceCategoryBean = insuranceCategoryDAO.getBean();
      List<BasicDynaBean> records = insuranceCategoryDAO.findAllByKey("service_id", serviceId);
      if (records != null && records.size() > 0) {
        flag = insuranceCategoryDAO.delete(con,"service_id", serviceId);
      }
      for (String insuranceCategory :  insuranceCategories) {
        insuranceCategoryBean.set("service_id", serviceId);
        insuranceCategoryBean.set("insurance_category_id", Integer.parseInt(insuranceCategory));
        flag = insuranceCategoryDAO.insert(con,insuranceCategoryBean);
      }
    }
    return flag;
  }
  
  private boolean createOrUpdateServiceResourceMapping(String serviceId,
      Connection con, HttpServletRequest request) throws SQLException, IOException {
    boolean flag = true;
    String[] serviceResourceIds = request.getParameterValues("serv_res_id");
    GenericDAO serviceResourceMappingDao =
            new GenericDAO("service_service_resources_mapping");
    BasicDynaBean serviceResourceMappingBean = serviceResourceMappingDao.getBean();
    if (serviceResourceIds != null && serviceResourceIds.length > 0
        && !serviceResourceIds[0].equals("")) {
      List<BasicDynaBean> existingMappedRecords = serviceResourceMappingDao.findAllByKey("service_id", serviceId);
      if (existingMappedRecords != null && existingMappedRecords.size() > 0) {
        flag = serviceResourceMappingDao.delete(con,"service_id", serviceId);
      }
      for (String serviceResourceId :  serviceResourceIds) {
        serviceResourceMappingBean.set("service_id", serviceId);
        serviceResourceMappingBean.set("serv_res_id", Integer.parseInt(serviceResourceId));
        flag = serviceResourceMappingDao.insert(con,serviceResourceMappingBean);
      }      
    } else { // delete the existing mapping if any
    	serviceResourceMappingDao.delete(con, "service_id", serviceId);
    }
    return flag;
  }
  
}

