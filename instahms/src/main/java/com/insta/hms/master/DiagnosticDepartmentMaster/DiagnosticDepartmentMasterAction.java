package com.insta.hms.master.DiagnosticDepartmentMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class DiagnosticDepartmentMasterAction extends DispatchAction {
  
  private static final GenericDAO hospitalCenterMasterDAO =
      new GenericDAO("hospital_center_master");
  private static final GenericDAO diagnosticDepartmentStoresDAO =
      new GenericDAO("diagnostic_department_stores");

	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		DiagnosticDepartmentMasterDAO dao = new DiagnosticDepartmentMasterDAO();
		Map requestParams = req.getParameterMap();
		PagedList pagedList = dao.search(requestParams, ConversionUtils.getListingParameter(requestParams),
					"ddept_id");
		req.setAttribute("pagedList", pagedList);

		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		Map<String, Object> filterMap = new HashMap<String, Object>();
		filterMap.put("status", "A");
		req.setAttribute("centerList", hospitalCenterMasterDAO.listAll(null,filterMap,"center_name"));
		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			DiagnosticDepartmentMasterDAO dao = new DiagnosticDepartmentMasterDAO();
			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);
			FlashScope flash = FlashScope.getScope(req);
			ActionRedirect redirect = null;
			int maxCenters = Integer.parseInt(new GenericPreferencesDAO().getAllPrefs().get("max_centers_inc_default").toString());

			if (errors.isEmpty()) {
				BasicDynaBean exists = dao.findByKey("ddept_name", bean.get("ddept_name"));
				if (exists == null) {
					bean.set("ddept_id", dao.getNextDeptId());
					String[] center_id = req.getParameterValues("center_id");
					String[] store_id = req.getParameterValues("store_id");
					String[] isNew = req.getParameterValues("added");
					String[] isDeleted = req.getParameterValues("selectedrow");
					BasicDynaBean deptbean = diagnosticDepartmentStoresDAO.getBean();
					Boolean suc = false;
					if(maxCenters == 1) {
						String dept = req.getParameter("deptid");
						deptbean.set("ddept_id", bean.get("ddept_id").toString());
						deptbean.set("center_id", 0);
						deptbean.set("store_id", Integer.parseInt(dept));
						suc = diagnosticDepartmentStoresDAO.insert(con, deptbean);
					} else {
						for(int i=0; i<center_id.length; i++) {
							if(!center_id[i].equals("")) {
								deptbean.set("ddept_id", bean.get("ddept_id").toString());
								deptbean.set("center_id", Integer.parseInt(center_id[i]));
								deptbean.set("store_id", Integer.parseInt(store_id[i]));
								if (isNew[i].equalsIgnoreCase("Y") && isDeleted[i].equalsIgnoreCase("false")) {
									if(!dao.isDiagDeptStoreExist(bean.get("ddept_id").toString(), Integer.parseInt(center_id[i])))
										suc = diagnosticDepartmentStoresDAO.insert(con, deptbean);
								} else if (isNew[i].equalsIgnoreCase("N") && isDeleted[i].equalsIgnoreCase("true")) {
									LinkedHashMap <String,Object> dkeys = new LinkedHashMap<String,Object>();
									dkeys.put("ddept_id", bean.get("ddept_id").toString());
									dkeys.put("center_id", Integer.parseInt(center_id[i]));
									suc = diagnosticDepartmentStoresDAO.delete(con, dkeys);
								} else if (isNew[i].equalsIgnoreCase("N")) {
									Map<String, Object> keys = new HashMap<String,Object>();
									keys.put("ddept_id", bean.get("ddept_id").toString());
									keys.put("center_id", Integer.parseInt(center_id[i]));
									int j = diagnosticDepartmentStoresDAO.update(con, deptbean.getMap(), keys);
									if(j>0) suc = true;
								}
							}
						}
					}

					suc = dao.insert(con, bean);
					if (suc) {
						con.commit();
						flash.success("Department master details inserted successfully..");
						redirect = new ActionRedirect(m.findForward("showRedirect"));
						redirect.addParameter("ddept_id", bean.get("ddept_id"));
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						DataBaseUtil.closeConnections(con, null);
						return redirect;
					} else {
						con.rollback();
						flash.error("Failed to add  Department..");
					}
				} else {
					flash.error("Department name already exists..");
				}
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
			redirect = new ActionRedirect(m.findForward("addRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;

		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		DiagnosticDepartmentMasterDAO dao = new DiagnosticDepartmentMasterDAO();
		BasicDynaBean bean = dao.findByKey("ddept_id", req.getParameter("ddept_id"));
		int maxCenters = Integer.parseInt(new GenericPreferencesDAO().getAllPrefs().get("max_centers_inc_default").toString());

		Map<String, Object> filterMap = new HashMap<String, Object>();
		filterMap.put("status", "A");
		req.setAttribute("bean", bean);
		req.setAttribute("dDeptsList", js.serialize(dao.getDdeptsNamesAndIds()));
		req.setAttribute("centerList", hospitalCenterMasterDAO.listAll(null, filterMap, "center_name"));
		req.setAttribute("diagdeptstores", ConversionUtils.copyListDynaBeansToMap(dao.getDiagDeptStoreDetails(req.getParameter("ddept_id"))));

		if(maxCenters == 1) {
			List <BasicDynaBean> list = dao.getDiagDeptStoreDetails(req.getParameter("ddept_id"));
			req.setAttribute("deptId", list.get(0).get("dept_id"));
		}
		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = null;
		Map params = req.getParameterMap();
		List errors = new ArrayList();
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			DiagnosticDepartmentMasterDAO dao = new DiagnosticDepartmentMasterDAO();
			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);

			int maxCenters = Integer.parseInt(new GenericPreferencesDAO().getAllPrefs().get("max_centers_inc_default").toString());
			String[] center_id = req.getParameterValues("center_id");
			String[] store_id = req.getParameterValues("store_id");
			String[] isNew = req.getParameterValues("added");
			String[] isDeleted = req.getParameterValues("selectedrow");
			Object key = req.getParameter("ddept_id");
			Boolean suc = false;
			BasicDynaBean deptbean = diagnosticDepartmentStoresDAO.getBean();


			if(maxCenters == 1) {
				String dept = req.getParameter("deptid");
				if(dao.isDiagDeptStoreExist(key.toString(), 0)) {
					deptbean.set("ddept_id", key.toString());
					deptbean.set("center_id", 0);
					deptbean.set("store_id", Integer.parseInt(dept));
					Map<String, Object> keys = new HashMap<String,Object>();
					keys.put("ddept_id", key);
					keys.put("center_id",  0);
					int j = diagnosticDepartmentStoresDAO.update(con, deptbean.getMap(), keys);
					if(j>0) suc = true;
				}else {
					deptbean.set("ddept_id", key.toString());
					deptbean.set("center_id", 0);
					deptbean.set("store_id", Integer.parseInt(dept));
					suc = diagnosticDepartmentStoresDAO.insert(con, deptbean);
				}
			}else {
				for(int i=0; i<center_id.length; i++) {
					if(!center_id[i].equals("")) {
						deptbean.set("ddept_id", key);
						deptbean.set("center_id", Integer.parseInt(center_id[i]));
						deptbean.set("store_id", Integer.parseInt(store_id[i]));
						if (isNew[i].equalsIgnoreCase("Y") && isDeleted[i].equalsIgnoreCase("false")) {
							if(!dao.isDiagDeptStoreExist(key.toString(), Integer.parseInt(center_id[i])))
								suc = diagnosticDepartmentStoresDAO.insert(con, deptbean);
						} else if (isNew[i].equalsIgnoreCase("N") && isDeleted[i].equalsIgnoreCase("true")) {
							LinkedHashMap <String,Object> dkeys = new LinkedHashMap<String,Object>();
							dkeys.put("ddept_id", key);
							dkeys.put("center_id",  Integer.parseInt(center_id[i]));
							suc = diagnosticDepartmentStoresDAO.delete(con, dkeys);
						} else if (isNew[i].equalsIgnoreCase("N")) {
							Map<String, Object> keys = new HashMap<String,Object>();
							keys.put("ddept_id", key);
							keys.put("center_id",  Integer.parseInt(center_id[i]));
							int j = diagnosticDepartmentStoresDAO.update(con, deptbean.getMap(), keys);
							if(j>0) suc = true;
						}
					}
				}
			}

			Map<String, String> keys = new HashMap<String, String>();
			keys.put("ddept_id", key.toString());
			FlashScope flash = FlashScope.getScope(req);
			boolean stat=true;
			String status=req.getParameter("status");
			System.out.println(req.getParameter("status"));
			if (errors.isEmpty()) {
				BasicDynaBean exists = dao.findByKey("ddept_name", bean.get("ddept_name"));
				if (exists != null && !key.equals(exists.get("ddept_id"))) {
					flash.error("Department name already exists..");

				} else {
					int k = dao.update(con, bean.getMap(), keys);
						if(k>0) suc = true;

					if (suc == true) {
						con.commit();
						if(status.equals("I")) {
							stat=dao.updateStatus(req.getParameter("ddept_id"));
						}
						if(stat)
							flash.success("Department master details updated successfully..");
						else
							flash.error("Error in updating diagnostics master");
					} else {
						con.rollback();
						flash.error("Failed to update Department master details..");
					}
				}
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
			ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("ddept_id", bean.get("ddept_id"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			DataBaseUtil.closeConnections(con, null);
			return redirect;

		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

}
