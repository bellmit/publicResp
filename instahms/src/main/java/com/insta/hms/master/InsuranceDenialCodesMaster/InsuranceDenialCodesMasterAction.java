/**
 *
 */
package com.insta.hms.master.InsuranceDenialCodesMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author lakshmi.p
 *
 */
public class InsuranceDenialCodesMasterAction extends BaseAction {

	static Logger logger = LoggerFactory.getLogger(InsuranceDenialCodesMasterAction.class);

	InsuranceDenialCodesMasterDAO dao = new InsuranceDenialCodesMasterDAO();

	public ActionForward list(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res)  throws Exception {

		Map filter = getParameterMap(req);
		PagedList pagedList = dao.search(filter, ConversionUtils.getListingParameter(filter), "denial_code");
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

		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = null;

		if (errors.isEmpty()) {
			BasicDynaBean exists = dao.findByKey("denial_code", bean.get("denial_code").toString().trim());
			if(exists == null) {
				bean.set("denial_code",bean.get("denial_code").toString().trim());
				boolean success = dao.insert(con, bean);
				if (success) {
					con.commit();
					redirect = new ActionRedirect(m.findForward("showRedirect"));
					flash.success("Denial Code details inserted successfully..");
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					redirect.addParameter("denial_code", bean.get("denial_code"));
					DataBaseUtil.closeConnections(con, null);
					return redirect;
				} else {
					con.rollback();
					flash.error("Failed to add Denial Code...");
				}
			} else {
				flash.error("Denial Code already exists...");
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
		filterMap.put("denial_code", req.getParameter("denial_code"));
		List beans = dao.listAll(null, filterMap, null);
		req.setAttribute("bean", beans.size() > 0 ? beans.get(0): null);
		req.setAttribute("DenialCodesList", js.serialize(ConversionUtils.copyListDynaBeansToMap(dao.listAll())));
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

		Object key1 = req.getParameter("old_denial_code");
		Map<String, String> keys = new HashMap<>();
		keys.put("denial_code", key1.toString());
		FlashScope flash = FlashScope.getScope(req);
		Map filterMap = new HashMap();
		filterMap.put("denial_code", bean.get("denial_code"));
		List beans = null;
		if (errors.isEmpty()) {
			beans = dao.listAll(null, filterMap, null);
			BasicDynaBean exists = (null != beans && !beans.isEmpty()) ? (BasicDynaBean) beans.get(0): null;

			if ( exists != null && (!key1.equals(exists.get("denial_code")))  ) {
				flash.error( "Denial Code '" + bean.get("denial_code") +" already exists..");
				filterMap.put("denial_code", key1);
				beans = dao.listAll(null, filterMap, null);
			}
			else {
				int success = dao.update(con, bean.getMap(), keys);

				if (success > 0) {
					con.commit();
					flash.success("Denial Code details updated successfully..");
					beans = dao.listAll(null, filterMap, null);

				} else {
					con.rollback();
					flash.error("Failed to update Denial Code details..");
					filterMap.put("denial_code", key1);
					beans = dao.listAll(null, filterMap, null);
				}
			}
		}
		else {
			flash.error("Incorrectly formatted values supplied");
		}

		bean = (null != beans && !beans.isEmpty()) ? (BasicDynaBean)beans.get(0): null;
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter("denial_code", null != bean ? bean.get("denial_code") : null);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}
}
