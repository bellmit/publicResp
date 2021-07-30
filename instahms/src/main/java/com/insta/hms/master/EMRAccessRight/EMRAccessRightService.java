package com.insta.hms.master.EMRAccessRight;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.DepartmentMaster.DepartmentMasterDAO;
import com.insta.hms.master.DocumentTypeMaster.DocumentTypeMasterDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class EMRAccessRightService {
	static Logger log = LoggerFactory.getLogger(EMRAccessRightService.class);
    private static final GenericDAO emrAccessRuleDetailsDAO =
        new GenericDAO("emr_access_rule_details");
    private static final GenericDAO uRoleDAO = new GenericDAO("u_role");
    private static final GenericDAO emrAccessRuleDAO = new GenericDAO("emr_access_rule");
    private static final GenericDAO docTypeDAO = new GenericDAO("doc_type");
	
	public void list(ActionMapping actionmapping, ActionForm actionform, HttpServletRequest req, HttpServletResponse res) throws SQLException, ParseException {
		// TODO Auto-generated method stub
		DocumentTypeMasterDAO dao = new DocumentTypeMasterDAO();
		BasicDynaBean bean = dao.findByKey("doc_type_id", req.getParameter("doc_type_id"));
		req.setAttribute("bean", bean);

		Map map= req.getParameterMap();
		EMRAccessRightDAO emraccessrightdao = new EMRAccessRightDAO();
		PagedList pagedList = emraccessrightdao.search(map,ConversionUtils.getListingParameter(req.getParameterMap()),"rule_id");
		req.setAttribute("pagedList", pagedList);
	}

	public void add(ActionMapping actionmapping, ActionForm actionform, HttpServletRequest req, HttpServletResponse res) throws SQLException {
		// TODO Auto-generated method stub
		DocumentTypeMasterDAO dao = new DocumentTypeMasterDAO();
		BasicDynaBean bean = dao.findByKey("doc_type_id", req.getParameter("doc_type_id"));
		req.setAttribute("bean", bean);

		BasicDynaBean rolebean = uRoleDAO.findByKey("role_id",(req.getParameter("role_id")==null?req.getParameter("role_id") :Integer.parseInt(req.getParameter("role_id")) ));
		req.setAttribute("rolebean", rolebean);

		EMRAccessRightDAO emraccessrightdao = new EMRAccessRightDAO();
		BasicDynaBean emrbean = emraccessrightdao.findByKey("rule_id", req.getParameter("rule_id"));
		req.setAttribute("emrbean", emrbean);
	}

	public void show(ActionMapping actionmapping, ActionForm actionform, HttpServletRequest req, HttpServletResponse res) throws Exception {
		// TODO Auto-generated method stub
		Map activeMap = new HashMap();
		activeMap.put("status", "A");

		EMRAccessRightDAO emraccessrightdao = new EMRAccessRightDAO();

		JSONSerializer js = new JSONSerializer().exclude("class");
		List consultationDocRulesList = new ArrayList();
		List consultationDocRulesMap = new ArrayList(); // helps to serialize an empty array
		Map consultationRuleDetailsMap = new HashMap(); // helps serialize an empty map
		// get all the beans for the rule and the document subtype
		String docTypeId = req.getParameter("doc_type_id");
		String ruleId = req.getParameter("rule_id");
		//if (null != docTypeId && "SYS_CONSULT".equals(docTypeId)) {
		if (null != docTypeId) {
			// set extra data when the entity is a consultation document
			consultationDocRulesList = emraccessrightdao.findAllByKey("doc_type_id", docTypeId);
		}else{
			consultationDocRulesList = emraccessrightdao.findAllByKey("rule_id", ruleId);
		}
		if (null != consultationDocRulesList) {
			consultationDocRulesMap = ConversionUtils.listBeanToListMap(consultationDocRulesList);
		}
		consultationRuleDetailsMap = getConsultationRuleDetailsJson(ruleId);
		req.setAttribute("consultationDocRulesMap", js.serialize(consultationDocRulesMap));
		req.setAttribute("consultationRuleDetailsMap", js.deepSerialize(consultationRuleDetailsMap));

		BasicDynaBean emrbean = emraccessrightdao.findByKey("rule_id", req.getParameter("rule_id"));
		req.setAttribute("emrbean", emrbean);

		List<BasicDynaBean> emrDetailsList = emrAccessRuleDetailsDAO.findAllByKey("rule_id", req.getParameter("rule_id"));
		req.setAttribute("emrDetailsList", emrDetailsList);

		List centList = new CenterMasterDAO().listAll(null, activeMap, "center_name");
		req.setAttribute("centList", centList);

		List deptList =  new DepartmentMasterDAO().listAll(null, activeMap, "dept_name");
		req.setAttribute("deptList", deptList);

		DocumentTypeMasterDAO dao = new DocumentTypeMasterDAO();
		if("DOC".equalsIgnoreCase(req.getParameter("rule_type"))){
			Map activeRoleMap = new HashMap();
			activeRoleMap.put("role_status", "A");
			List roleList =  uRoleDAO.listAll(null, activeRoleMap, "role_name");
			req.setAttribute("roleList", roleList);

			BasicDynaBean bean = dao.findByKey("doc_type_id", req.getParameter("doc_type_id"));
			req.setAttribute("bean", bean);
			Map activeUserMap = new HashMap();
			activeUserMap.put("emp_status","A");
			List userList = new GenericDAO("u_user").listAll(null, activeUserMap, "emp_username");
			req.setAttribute("userList", userList);
		}else{
			BasicDynaBean rolebean = uRoleDAO.findByKey("role_id",(req.getParameter("role_id")==null?req.getParameter("role_id") :Integer.parseInt(req.getParameter("role_id")) ));
			req.setAttribute("rolebean", rolebean);

			List documentList = dao.listAll(null, activeMap, "doc_type_name");
			req.setAttribute("documentList", documentList);
		}

	}

	public static ActionRedirect create(ActionMapping actionmapping, ActionForm actionform, HttpServletRequest req, HttpServletResponse res, ActionRedirect redirect, String strProcess) throws SQLException, IOException {
		// TODO Auto-generated method stub
		Map params = req.getParameterMap();
		List errors = new ArrayList();
		FlashScope flash = FlashScope.getScope(req);
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		HttpSession session = req.getSession();
		int roleId = (Integer)session.getAttribute("roleId");

		try {

			EMRAccessRightDAO dao = new EMRAccessRightDAO();
			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);
			String ruleType = req.getParameter("rule_type");
			if("DOC".equalsIgnoreCase(ruleType)){
				if(bean.get("user_access").equals("1") && !bean.get("role_access").equals("4")){
					bean.set("user_access", "0");
				}else if(bean.get("user_access").equals("1") && bean.get("role_access").equals("4")){
					bean.set("role_access", "0");
					//bean.set("user_access", "1");
				}

			}else{
				// RC : Review this logic for correctness
				if(bean.get("doc_access").equals("1") && !bean.get("role_access").equals("4")){
					bean.set("doc_access", "0");
				}else if(bean.get("doc_access").equals("1") && bean.get("role_access").equals("4")){
					bean.set("role_access", "4");
					//bean.set("doc_access", "1");
				}
				bean.set("doc_type_id", "");
			}

			if (errors.isEmpty()) {
				BasicDynaBean exists = dao.findByKey("rule_id", bean.get("rule_id"));
				if (exists == null) {
					if("Save".equals(strProcess)){
						bean.set("rule_id", EMRAccessRightDAO.getAccessRightRuleId());
					}else{
						bean.set("rule_id", bean.get("rule_id"));
					}

					BasicDynaBean ruleDetails = emrAccessRuleDetailsDAO.getBean();
					ConversionUtils.copyToDynaBean(params, ruleDetails, errors);
					if("DOC".equalsIgnoreCase(ruleType)){
						if((!bean.get("user_access").equals(null)) && (bean.get("user_access").equals("3") || bean.get("user_access").equals("2"))){
							bean.set("center_access", "3");
							bean.set("dept_access", "3");
							bean.set("role_access", "3");
						}
						bean.set("role_id", String.valueOf(roleId));
					}else{
						if((!bean.get("doc_access").equals(null)) && (bean.get("doc_access").equals("3") || bean.get("doc_access").equals("2"))){
							bean.set("center_access", "3");
							bean.set("dept_access", "3");
							bean.set("role_access", "0");
						}
					}

					boolean success = dao.insert(con, bean);// Data insert in EMR_ACCESS_RULE table
					ruleDetails.set("rule_id", bean.get("rule_id"));

						if(bean.get("center_access").equals("1")){

							ruleDetails.set("entity_type", "C");
							String[] centerList = req.getParameterValues("center_id");

							if (centerList != null) {
								for (int i=0; i<centerList.length; i++) {
									ruleDetails.set("entity_id", centerList[i]);
									success = emrAccessRuleDetailsDAO.insert(con,ruleDetails);// Data insert in EMR_ACCESS_RULE_DETAILS table
								}
							}
						}
						if(bean.get("dept_access").equals("1")){
							ruleDetails.set("entity_type", "D");
							String[] deptList = req.getParameterValues("dept_id");

							if (deptList != null) {
								for (int i=0; i<deptList.length; i++) {
									ruleDetails.set("entity_id", deptList[i]);
									success = emrAccessRuleDetailsDAO.insert(con,ruleDetails);// Data insert in EMR_ACCESS_RULE_DETAILS table
								}
							}
						}
						if(bean.get("role_access").equals("1")){
							ruleDetails.set("entity_type", "R");
							String[] roleList = req.getParameterValues("role_id");

							if (roleList != null) {
								int j;
								if("DOC".equalsIgnoreCase(ruleType)){ j=0;}else{ j=1; } // This will remove one extra entry of role while coming from role screen.
								for (int i=j; i<roleList.length; i++) {
									ruleDetails.set("entity_id", roleList[i]);
									success = emrAccessRuleDetailsDAO.insert(con,ruleDetails);// Data insert in EMR_ACCESS_RULE_DETAILS table
								}
							}
						}
						if("DOC".equalsIgnoreCase(ruleType)){
							if(bean.get("user_access").equals("1")){
								ruleDetails.set("entity_type", "U");
								String[] userList = req.getParameterValues("emp_username");

								if (userList != null) {
									for (int i=0; i<userList.length; i++) {
										ruleDetails.set("entity_id", userList[i]);
										success = emrAccessRuleDetailsDAO.insert(con,ruleDetails);// Data insert in EMR_ACCESS_RULE_DETAILS table
									}
								}
							}
						}else{
							if(bean.get("doc_access").equals("1")){
								ruleDetails.set("entity_type", "T");
								String[] docList = req.getParameterValues("doc_type_id");

								if (docList != null) {
									for (int i=0; i<docList.length; i++) {
										ruleDetails.set("entity_id", docList[i]);
										success = emrAccessRuleDetailsDAO.insert(con,ruleDetails);// Data insert in EMR_ACCESS_RULE_DETAILS table
									}
								}
							}
						}

					if (success) {
						con.commit();
						flash.success("EMR Access Rule details inserted successfully..");
						redirect = new ActionRedirect(actionmapping.findForward("showRedirect"));
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						redirect.addParameter("rule_id", bean.get("rule_id"));
						redirect.addParameter("rule_type", bean.get("rule_type"));
						if("DOC".equalsIgnoreCase(ruleType)){
							redirect.addParameter("doc_type_id", bean.get("doc_type_id"));
						}else{
							redirect.addParameter("role_id", bean.get("role_id"));
						}
						return redirect;
					} else {
						log.info("Failed to add  EMR Access Rule..");
						con.rollback();
						flash.error("Failed to add  EMR Access Rule..");
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					}
				} else {
					flash.error("Rule type name already exists..");
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					return redirect;
				}
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
		return redirect;
	}

	/*public ActionRedirect update(ActionMapping actionmapping, ActionForm actionform, HttpServletRequest req, HttpServletResponse res, ActionRedirect redirect, String strProcess) throws SQLException, IOException {

		boolean success = EMRAccessRightService.delete(actionmapping,actionform,req,res);
		if(success){
			redirect = EMRAccessRightService.create(actionmapping,actionform,req,res,redirect,strProcess);
		}else{
			log.info("Failed to update the records ..");
		}
		return redirect;
	}*/

	public ActionRedirect update(ActionMapping actionmapping, ActionForm actionform, HttpServletRequest req, HttpServletResponse res, ActionRedirect redirect, String strProcess) throws SQLException, IOException {
		Map params = req.getParameterMap();
		List errors = new ArrayList();
		EMRAccessRightDAO dao = new EMRAccessRightDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		String docTypeId = (String) bean.get("doc_type_id");
		boolean success = false;
		String ruleType = req.getParameter("rule_type");
		if("DOC".equalsIgnoreCase(ruleType) && (null != docTypeId && "SYS_CONSULT".equalsIgnoreCase(docTypeId))){
			String docSubType = (String) bean.get("doc_sub_type");
			String ruleQuery ="SELECT rule_id FROM emr_access_rule where doc_sub_type=?";
			String ruleId = DataBaseUtil.getStringValueFromDb(ruleQuery, docSubType);

			BasicDynaBean exists = dao.findByKey("rule_id", ruleId);
			if (exists == null) {
				strProcess = "Save";
				redirect = EMRAccessRightService.createConsultantRule(actionmapping,actionform,req,res,redirect,strProcess,ruleId);
			}else{
				success = EMRAccessRightService.deleteConsultantRule(actionmapping,actionform,req,res,ruleId);
				if(success){
					redirect = EMRAccessRightService.createConsultantRule(actionmapping,actionform,req,res,redirect,strProcess,ruleId);
				}else{
					log.info("Failed to update the records ..");
				}
			}
		}else{
			success = EMRAccessRightService.delete(actionmapping,actionform,req,res);
			if(success){
				redirect = EMRAccessRightService.create(actionmapping,actionform,req,res,redirect,strProcess);
			}else{
				log.info("Failed to update the records ..");
			}
		}

		return redirect;
	}

	private static boolean delete(ActionMapping actionmapping, ActionForm actionform, HttpServletRequest req, HttpServletResponse res) throws SQLException {
		// TODO Auto-generated method stub
		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		boolean success = false;

		try{
			EMRAccessRightDAO dao = new EMRAccessRightDAO();
			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);
			String strRuleValue = (String) bean.get("rule_id");

			BasicDynaBean ruleDetails = emrAccessRuleDetailsDAO.getBean();
			ConversionUtils.copyToDynaBean(params, ruleDetails, errors);
			BasicDynaBean exists = dao.findByKey("rule_id", ruleDetails.get("rule_id"));

			if (exists == null) {
			success = emrAccessRuleDAO.delete(con, "rule_id", strRuleValue.trim());
			}else{
				success = emrAccessRuleDetailsDAO.delete(con, "rule_id", strRuleValue.trim());
				success = emrAccessRuleDAO.delete(con, "rule_id", strRuleValue.trim());
			}

			if(success){
				log.info("EMR Access Rule records deleted successfully..");
				con.commit();
			}else{
				log.error("Failed to delete EMR Access Rule records ..");
				con.rollback();
			}

		} catch (SQLException se) {
			success = false;
			log.error("", se);
			throw se;
		} finally {
			DataBaseUtil.commitClose(con, success);
		}

		return success;
	}

	public ActionRedirect findRules(ActionMapping actionmapping, ActionForm actionform, HttpServletRequest req, HttpServletResponse res) throws SQLException {
		// TODO Auto-generated method stub
		ActionRedirect redirect = new ActionRedirect(actionmapping.findForward("showRedirect"));
		FlashScope flash = FlashScope.getScope(req);
		EMRAccessRightDAO emraccessrightdao = new EMRAccessRightDAO();
		BasicDynaBean ebean = new BasicDynaBean(null);
		Map searchMap = new HashMap();
		if("DOC".equalsIgnoreCase(req.getParameter("rule_type"))){
			searchMap.put("doc_type_id", req.getParameter("doc_type_id"));
			searchMap.put("rule_type", "DOC");
			 ebean = emraccessrightdao.findByKey(searchMap);
//			 ebean = emraccessrightdao.findByKey("doc_type_id", req.getParameter("doc_type_id"));
		}else{
			searchMap.put("role_id", req.getParameter("role_id"));
			searchMap.put("rule_type", "ROLE");
			 ebean = emraccessrightdao.findByKey(searchMap);
//			 ebean = emraccessrightdao.findByKey("role_id", req.getParameter("role_id"));
		}
			if (ebean == null) {
				EMRAccessRightService service = new EMRAccessRightService();
				service.add(actionmapping, actionform, req, res);
				redirect = null;
			}else{
				BasicDynaBean emrbean = emraccessrightdao.findByKey("rule_id", (String) ebean.get("rule_id"));
				req.setAttribute("emrbean", emrbean);
				if("DOC".equalsIgnoreCase(req.getParameter("rule_type"))){
					BasicDynaBean emrDetailsBean = docTypeDAO.findByKey("doc_type_id", req.getParameter("doc_type_id"));
					req.setAttribute("emrDetailsBean", emrDetailsBean);
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					redirect.addParameter("rule_id", (String) ebean.get("rule_id"));
					redirect.addParameter("rule_type", req.getParameter("rule_type"));
					redirect.addParameter("doc_type_id", emrDetailsBean.get("doc_type_id"));
				}else{
					BasicDynaBean rolebean = uRoleDAO.findByKey("role_id",(req.getParameter("role_id")==null?req.getParameter("role_id") :Integer.parseInt(req.getParameter("role_id")) ));
					req.setAttribute("rolebean", rolebean);
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					redirect.addParameter("rule_id", (String) ebean.get("rule_id"));
					redirect.addParameter("rule_type", req.getParameter("rule_type"));
					redirect.addParameter("role_id", rolebean.get("role_id"));
				}
			}
		return redirect;
	}

	// Finds if at least one rule is there, otherwise returns null
	public ActionRedirect findConsultationRules(ActionMapping actionmapping, ActionForm actionform, HttpServletRequest req, HttpServletResponse res)
		throws Exception {

		ActionRedirect redirect = new ActionRedirect(actionmapping.findForward("showRedirect"));
		FlashScope flash = FlashScope.getScope(req);
		BasicDynaBean ebean = null;
		Map activeMap = new HashMap();
		activeMap.put("status", "A");
		EMRAccessRightDAO emraccessrightdao = new EMRAccessRightDAO();

		Map searchMap = new HashMap();
		if("DOC".equalsIgnoreCase(req.getParameter("rule_type"))){
			searchMap.put("doc_type_id", req.getParameter("doc_type_id"));
			searchMap.put("rule_type", "DOC");
			ebean = emraccessrightdao.findByKey(searchMap);
		}

		if (ebean == null) {
			EMRAccessRightService service = new EMRAccessRightService();
			redirect = null;
		} else {
			BasicDynaBean emrbean = emraccessrightdao.findByKey("rule_id", (String) ebean.get("rule_id"));
			req.setAttribute("emrbean", emrbean);
			if("DOC".equalsIgnoreCase(req.getParameter("rule_type"))){
				BasicDynaBean emrDetailsBean = docTypeDAO.findByKey("doc_type_id", req.getParameter("doc_type_id"));
				req.setAttribute("emrDetailsBean", emrDetailsBean);
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				redirect.addParameter("rule_id", (String) ebean.get("rule_id"));
				redirect.addParameter("rule_type", req.getParameter("rule_type"));
				redirect.addParameter("doc_type_id", emrDetailsBean.get("doc_type_id"));
			}
		}
		return redirect;
	}

	private Map getConsultationRuleDetailsJson(String ruleId) throws Exception {
		EMRAccessRightDAO emraccessrightdao = new EMRAccessRightDAO();

		List centerList = emraccessrightdao.getAllowedCenters(ruleId);
		List depList = emraccessrightdao.getAllowedDepartments(ruleId);
		List roleList = emraccessrightdao.getAllowedRoles(ruleId);
		List userList = emraccessrightdao.getAllowedUsers(ruleId);
		List docTypeList = emraccessrightdao.getAllowedDocumentTypes(ruleId);

		List centerMapList =  ConversionUtils.listBeanToListMap(centerList);
		List depMapList =  ConversionUtils.listBeanToListMap(depList);
		List roleMapList =  ConversionUtils.listBeanToListMap(roleList);
		List userMapList =  ConversionUtils.listBeanToListMap(userList);
		List docTypeMapList = ConversionUtils.listBeanToListMap(docTypeList);

		Map allowedEntityMap = new HashMap();
		allowedEntityMap.put("centers", new ArrayList());
		allowedEntityMap.put("departments", new ArrayList());
		allowedEntityMap.put("roles", new ArrayList());
		allowedEntityMap.put("users", new ArrayList());
		allowedEntityMap.put("documentType", new ArrayList());

		if (centerMapList.size() > 0) {
			allowedEntityMap.put("centers", centerMapList);
		}
		if (depMapList.size() > 0) {
			allowedEntityMap.put("departments", depMapList);
		}
		if (roleMapList.size() > 0) {
			allowedEntityMap.put("roles", roleMapList);
		}
		if (userMapList.size() > 0) {
			allowedEntityMap.put("users", userMapList);
		}
		if (docTypeMapList.size() > 0) {
			allowedEntityMap.put("documentType", docTypeMapList);
		}
		return allowedEntityMap;
	}

	public static ActionRedirect createConsultantRule(ActionMapping actionmapping, ActionForm actionform, HttpServletRequest req, HttpServletResponse res, ActionRedirect redirect, String strProcess,String ruleId) throws SQLException, IOException {
		// TODO Auto-generated method stub
		Map params = req.getParameterMap();
		List errors = new ArrayList();
		FlashScope flash = FlashScope.getScope(req);
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		HttpSession session = req.getSession();
		int roleId = (Integer)session.getAttribute("roleId");

		try {

			EMRAccessRightDAO dao = new EMRAccessRightDAO();
			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);
			String ruleType = req.getParameter("rule_type");
			String docTypeId = (String) bean.get("doc_type_id");

			if("DOC".equalsIgnoreCase(ruleType)){
				if(bean.get("user_access").equals("1") && !bean.get("role_access").equals("4")){
					bean.set("user_access", "0");
				}else if(bean.get("user_access").equals("1") && bean.get("role_access").equals("4")){
					bean.set("role_access", "0");
					//bean.set("user_access", "1");
				}

			}else{
				// RC : Review this logic for correctness
				if(bean.get("doc_access").equals("1") && !bean.get("role_access").equals("4")){
					bean.set("doc_access", "0");
				}else if(bean.get("doc_access").equals("1") && bean.get("role_access").equals("4")){
					bean.set("role_access", "4");
					//bean.set("doc_access", "1");
				}
				bean.set("doc_type_id", "");
			}

			if (errors.isEmpty()) {
				BasicDynaBean exists = null;

					if("Save".equals(strProcess)){
						bean.set("rule_id", EMRAccessRightDAO.getAccessRightRuleId());
					}else{
						bean.set("rule_id", ruleId);
					}

					BasicDynaBean ruleDetails = emrAccessRuleDetailsDAO.getBean();
					ConversionUtils.copyToDynaBean(params, ruleDetails, errors);
					if("DOC".equalsIgnoreCase(ruleType)){
						if((!bean.get("user_access").equals(null)) && (bean.get("user_access").equals("3") || bean.get("user_access").equals("2"))){
							bean.set("center_access", "3");
							bean.set("dept_access", "3");
							bean.set("role_access", "3");
						}
						bean.set("role_id", String.valueOf(roleId));
					}else{
						if((!bean.get("doc_access").equals(null)) && (bean.get("doc_access").equals("3") || bean.get("doc_access").equals("2"))){
							bean.set("center_access", "3");
							bean.set("dept_access", "3");
							bean.set("role_access", "0");
						}
					}

					boolean success = dao.insert(con, bean);// Data insert in EMR_ACCESS_RULE table
					ruleDetails.set("rule_id", bean.get("rule_id"));

						if(bean.get("center_access").equals("1")){

							ruleDetails.set("entity_type", "C");
							String[] centerList = req.getParameterValues("center_id");

							if (centerList != null) {
								for (int i=0; i<centerList.length; i++) {
									ruleDetails.set("entity_id", centerList[i]);
									success = emrAccessRuleDetailsDAO.insert(con,ruleDetails);// Data insert in EMR_ACCESS_RULE_DETAILS table
								}
							}
						}
						if(bean.get("dept_access").equals("1")){
							ruleDetails.set("entity_type", "D");
							String[] deptList = req.getParameterValues("dept_id");

							if (deptList != null) {
								for (int i=0; i<deptList.length; i++) {
									ruleDetails.set("entity_id", deptList[i]);
									success = emrAccessRuleDetailsDAO.insert(con,ruleDetails);// Data insert in EMR_ACCESS_RULE_DETAILS table
								}
							}
						}
						if(bean.get("role_access").equals("1")){
							ruleDetails.set("entity_type", "R");
							String[] roleList = req.getParameterValues("role_id");

							if (roleList != null) {
								int j;
								if("DOC".equalsIgnoreCase(ruleType)){ j=0;}else{ j=1; } // This will remove one extra entry of role while coming from role screen.
								for (int i=j; i<roleList.length; i++) {
									ruleDetails.set("entity_id", roleList[i]);
									success = emrAccessRuleDetailsDAO.insert(con,ruleDetails);// Data insert in EMR_ACCESS_RULE_DETAILS table
								}
							}
						}
						if("DOC".equalsIgnoreCase(ruleType)){
							if(bean.get("user_access").equals("1")){
								ruleDetails.set("entity_type", "U");
								String[] userList = req.getParameterValues("emp_username");

								if (userList != null) {
									for (int i=0; i<userList.length; i++) {
										ruleDetails.set("entity_id", userList[i]);
										success = emrAccessRuleDetailsDAO.insert(con,ruleDetails);// Data insert in EMR_ACCESS_RULE_DETAILS table
									}
								}
							}
						}else{
							if(bean.get("doc_access").equals("1")){
								ruleDetails.set("entity_type", "T");
								String[] docList = req.getParameterValues("doc_type_id");

								if (docList != null) {
									for (int i=0; i<docList.length; i++) {
										ruleDetails.set("entity_id", docList[i]);
										success = emrAccessRuleDetailsDAO.insert(con,ruleDetails);// Data insert in EMR_ACCESS_RULE_DETAILS table
									}
								}
							}
						}

					if (success) {
						con.commit();
						flash.success("EMR Access Rule details inserted successfully..");
						redirect = new ActionRedirect(actionmapping.findForward("showRedirect"));
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						redirect.addParameter("rule_id", bean.get("rule_id"));
						redirect.addParameter("rule_type", bean.get("rule_type"));
						if("DOC".equalsIgnoreCase(ruleType)){
							redirect.addParameter("doc_type_id", bean.get("doc_type_id"));
							if(null != docTypeId && "SYS_CONSULT".equalsIgnoreCase(docTypeId)){
								redirect.addParameter("doc_sub_type", bean.get("doc_sub_type"));
							}
						}else{
							redirect.addParameter("role_id", bean.get("role_id"));
						}
						return redirect;
					} else {
						log.info("Failed to add  EMR Access Rule..");
						con.rollback();
						flash.error("Failed to add  EMR Access Rule..");
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					}

			} else {
				flash.error("Incorrectly formatted values supplied");
			}
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
		return redirect;
	}

	private static boolean deleteConsultantRule(ActionMapping actionmapping, ActionForm actionform, HttpServletRequest req, HttpServletResponse res,String ruleId) throws SQLException {
		// TODO Auto-generated method stub
		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		boolean success = false;
		try{

			BasicDynaBean ruleDetails = emrAccessRuleDetailsDAO.getBean();
			ConversionUtils.copyToDynaBean(params, ruleDetails, errors);
			BasicDynaBean exists = emrAccessRuleDetailsDAO.findByKey("rule_id", ruleId);

			if (exists == null) {
			success = emrAccessRuleDAO.delete(con, "rule_id", ruleId.trim());
			}else{
				success = emrAccessRuleDetailsDAO.delete(con, "rule_id", ruleId.trim());
				success = emrAccessRuleDAO.delete(con, "rule_id", ruleId.trim());
			}

			if(success){
				log.info("EMR Access Rule records deleted successfully..");
				con.commit();
			}else{
				log.error("Failed to delete EMR Access Rule records ..");
				con.rollback();
			}

		} catch (SQLException se) {
			success = false;
			log.error("", se);
			throw se;
		} finally {
			DataBaseUtil.commitClose(con, success);
		}

		return success;
	}

	public String getRuleDetailsBasedonDocSubType(String docTypeId,String docSubType, String ruleType,HttpServletRequest req, HttpServletResponse res) throws Exception {
		// TODO Auto-generated method stub

		EMRAccessRightDAO emraccessrightdao = new EMRAccessRightDAO();
		String ruleId=null;
		JSONSerializer js = new JSONSerializer().exclude("class");
		List consultationDocRulesMap = new ArrayList(); // helps to serialize an empty array
		Map consultationRuleDetailsMap = new HashMap(); // helps serialize an empty map
		Map searchMap = new HashMap();
		searchMap.put("doc_type_id", docTypeId);
		searchMap.put("doc_sub_type", docSubType);
		BasicDynaBean bean = (BasicDynaBean) emraccessrightdao.findByKey(searchMap);
		if(null!=bean){
			ruleId = (String) bean.get("rule_id");
		}

		// get all the beans for the rule and the document subtype
		if("DOC".equalsIgnoreCase(ruleType) && (null != docTypeId && "SYS_CONSULT".equalsIgnoreCase(docTypeId))){
			// set extra data when the entity is a consultation document
			List consultationDocRulesList = emraccessrightdao.findAllByKey("rule_id", ruleId);
			if (null != consultationDocRulesList) {
				consultationDocRulesMap = ConversionUtils.listBeanToListMap(consultationDocRulesList);
			}

			consultationRuleDetailsMap = getConsultationRuleDetailsJson(ruleId);
		}
		req.setAttribute("consultationDocRulesMaps", js.serialize(consultationDocRulesMap));
		req.setAttribute("consultationRuleDetailsMaps", js.deepSerialize(consultationRuleDetailsMap));
		return ruleId;
	}

}

