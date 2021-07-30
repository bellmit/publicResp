package com.insta.hms.master.ICDCodesMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.MigratedTo;
import com.insta.hms.mdm.icdcodes.IcdCodesController;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@MigratedTo(value=IcdCodesController.class)
public class ICDCodesMasterAction extends BaseAction{
  
  private static final GenericDAO mrdCodesMasterDAO = new GenericDAO("mrd_codes_master");

	public ActionForward list(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res)  throws Exception {

		Map filter = getParameterMap(req);
		PagedList pagedList = mrdCodesMasterDAO.search(filter, ConversionUtils.getListingParameter(filter),
				"code");
		req.setAttribute("pagedList", pagedList);
		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws Exception {
		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
		throws IOException, ServletException, Exception {

		Map params = getParameterMap(req);
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		BasicDynaBean bean = mrdCodesMasterDAO.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = null;

		if (errors.isEmpty()) {
			BasicDynaBean exists = mrdCodesMasterDAO.findByKey("code", bean.get("code"));
			if(exists == null) {
				boolean success = mrdCodesMasterDAO.insert(con, bean);
				if (success) {
					con.commit();
					redirect = new ActionRedirect(m.findForward("showRedirect"));
					flash.success("Code details inserted successfully..");
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					redirect.addParameter("code", bean.get("code"));
					redirect.addParameter("codeType", bean.get("code_type"));
					DataBaseUtil.closeConnections(con, null);
					return redirect;
				} else {
					con.rollback();
					flash.error("Failed to add Code...");
				}
			} else {
				flash.error("Code already exists...");
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

		Map filterMap = new HashMap();
		filterMap.put("code", req.getParameter("code"));
		filterMap.put("code_type", req.getParameter("codeType"));
		List beans = mrdCodesMasterDAO.listAll(null, filterMap, null);
		req.setAttribute("bean", beans.size() > 0 ? beans.get(0): null);
		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		Map params = req.getParameterMap();
		List errors = new ArrayList();

		BasicDynaBean bean = mrdCodesMasterDAO.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		Object key1 = req.getParameter("old_code");
		Object key2 = req.getParameter("old_code_type");
		Map<String, String> keys = new HashMap<String, String>();
		keys.put("code", key1.toString());
		keys.put("code_type", key2.toString());
		FlashScope flash = FlashScope.getScope(req);
		Map filterMap = new HashMap();
		filterMap.put("code", bean.get("code"));
		filterMap.put("code_type", bean.get("code_type"));
		List beans = null;
		if (errors.isEmpty()) {
			beans = mrdCodesMasterDAO.listAll(null, filterMap, null);
			BasicDynaBean exists = beans.size() > 0 ? (BasicDynaBean) beans.get(0): null;

			if ( exists != null && (!key1.equals(exists.get("code")) || !key2.equals(exists.get("code_type")) )  ) {
				flash.error( "Code '" + bean.get("code") +"'  of Code Type '"+ bean.get("code_type") + "' already exists..");
				filterMap.put("code", key1);
				filterMap.put("code_type", key2);
				beans = mrdCodesMasterDAO.listAll(null, filterMap, null);
			}
			else {
				int success = mrdCodesMasterDAO.update(con, bean.getMap(), keys);

				if (success > 0) {
					con.commit();
					flash.success("Code details updated successfully..");
					beans = mrdCodesMasterDAO.listAll(null, filterMap, null);

				} else {
					con.rollback();
					flash.error("Failed to update Code details..");
					filterMap.put("code", key1);
					filterMap.put("code_type", key2);
					beans = mrdCodesMasterDAO.listAll(null, filterMap, null);
				}
			}
		}
		else {
			flash.error("Incorrectly formatted values supplied");
		}

		bean = beans.size() > 0 ? (BasicDynaBean)beans.get(0): null;
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter("code", bean.get("code"));
		redirect.addParameter("codeType", bean.get("code_type"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}
}
