/**
 *
 */
package com.insta.hms.master.DynaPackageRules;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
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
import javax.servlet.http.HttpSession;

/**
 * @author lakshmi
 *
 */
public class DynaPackageRulesMasterAction extends BaseAction {

	static Logger logger = LoggerFactory.getLogger(DynaPackageRulesMasterAction.class);

	DynaPackageRulesMasterDAO dao = new DynaPackageRulesMasterDAO();

	public ActionForward list(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res)  throws Exception {

		PagedList pagedList = dao.getDynaPkgRules(req.getParameterMap(),
				ConversionUtils.getListingParameter(req.getParameterMap()));

		req.setAttribute("pagedList", pagedList);
		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		req.setAttribute("chargeGroupHeadListJSON", js.serialize(
				ConversionUtils.listBeanToListMap(dao.getChargeGroupHeadDetails())));
		req.setAttribute("storeItemCategories", dao.getStoreItemDetails());

		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {

		Map params = req.getParameterMap();

		List errors = new ArrayList();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(m.findForward("addRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		HttpSession session = req.getSession();
    	String userName = (String)session.getAttribute("userid");

		Connection con = null;
		boolean success = false;

		try {
			if (errors.isEmpty()) {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);

				int priority = (Integer)bean.get("priority");

				BasicDynaBean priortyBean = dao.findByKey("priority", priority);

				if (priortyBean != null) {
					redirect = new ActionRedirect(m.findForward("showRedirect"));
					flash.error("This rule has the same priority: " + priority);
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					redirect.addParameter("pkg_rule_id", priortyBean.get("pkg_rule_id"));
					return redirect;
				}

				BasicDynaBean exists = dao.isDuplicateRule(bean);

				if (exists != null) {
					priority = (Integer)exists.get("priority");
					flash.error(" A rule with the same condition exists with priority: " + priority);
					return redirect;
				}else {

					bean.set("pkg_rule_id", DataBaseUtil.getNextSequence("dyna_package_rules_seq"));
					bean.set("username", userName);
					success = dao.insert(con, bean);

					if (!success) {
						flash.error("Failed to insert dyna package rule Details.");
						return redirect;
					}else {
						redirect = new ActionRedirect(m.findForward("showRedirect"));
						flash.info("Dyna package rule inserted.");
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						redirect.addParameter("pkg_rule_id", bean.get("pkg_rule_id"));
						return redirect;
					}
				}
			} else {
				flash.error("Incorectly formated values supplied..");
				return redirect;
			}
		} finally {
			DataBaseUtil.commitClose(con, success);
		}
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		BasicDynaBean bean = dao.findByKey("pkg_rule_id", Integer.parseInt(req.getParameter("pkg_rule_id")));
		req.setAttribute("bean", bean);

		String activityType = (String)bean.get("activity_type");
		String activityId = (String)bean.get("activity_id");
		List<BasicDynaBean> activityList = dao.getActivityList(null, activityType, activityId);
		String activityName = "(All)";
		if (activityList != null && activityList.size() > 0)
			activityName = (String)((BasicDynaBean)activityList.get(0)).get("activity_name");
		req.setAttribute("activity_name", activityName);

		req.setAttribute("chargeGroupHeadListJSON", js.serialize(
				ConversionUtils.listBeanToListMap(dao.getChargeGroupHeadDetails())));
		req.setAttribute("storeItemCategories", dao.getStoreItemDetails());

		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {

		Map params = req.getParameterMap();

		List errors = new ArrayList();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		HttpSession session = req.getSession();
    	String userName = (String)session.getAttribute("userid");

		Connection con = null;
		boolean success = false;

		try {
			if (errors.isEmpty()) {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);

				int priority = (Integer)bean.get("priority");

				BasicDynaBean priortyBean = dao.findByKey("priority", priority);

				if (priortyBean != null && ((Integer)priortyBean.get("pkg_rule_id")).intValue()
							!=  ((Integer)(bean.get("pkg_rule_id"))).intValue()) {

					flash.error("This rule has the same priority: " + priority);
					redirect.addParameter("pkg_rule_id", priortyBean.get("pkg_rule_id"));
					return redirect;
				}

				BasicDynaBean exists = dao.isDuplicateRule(bean);

				if (exists != null && ((Integer)exists.get("pkg_rule_id")).intValue()
						!=  ((Integer)(bean.get("pkg_rule_id"))).intValue()) {

					priority = (Integer)exists.get("priority");
					flash.error(" A rule with the same condition exists with priority: " + priority);
					redirect.addParameter("pkg_rule_id", priortyBean.get("pkg_rule_id"));
					return redirect;
				}else {

					bean.set("username", userName);
					int i = dao.updateWithName(con, bean.getMap(), "pkg_rule_id");
					success = (i > 0);

					if (success)
						flash.info("Dyna package rule updated.");
					else
						flash.error("Failed to update dyna package rule Details..");
				}
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
		} finally {
			DataBaseUtil.commitClose(con, success);
		}
		redirect.addParameter("pkg_rule_id", bean.get("pkg_rule_id"));
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}

	public ActionForward reorderPriority(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {

		ActionRedirect redirect = new ActionRedirect(m.findForward("listRedirect"));
		FlashScope flash = FlashScope.getScope(req);
		boolean status = dao.updatePriorityValues();
		if (status) {
			flash.success("Priorities renumbered successfully.");
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		} else {
			flash.error("Failed to renumber priorities.");
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}
	}

	public ActionForward deleteRules(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException, Exception {

		Connection con = null;

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			ActionRedirect redirect = new ActionRedirect(m.findForward("listRedirect"));
			FlashScope flash = FlashScope.getScope(req);

			boolean status = false;
			String[] ruleDeleteChecks = req.getParameterValues("_ruleDelete");

			if (ruleDeleteChecks != null && ruleDeleteChecks.length > 0) {
				for (int i = 0; i < ruleDeleteChecks.length; i++) {
					int priority = new Integer(ruleDeleteChecks[i]);
					status = dao.delete(con, "priority", priority);
					if (!status) break;
				}
			}

			if (status) {
				con.commit();
				flash.success("Dyna package rule(s) deleted successfully.");
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			} else {
				con.rollback();
				flash.error("Failed to delete Dyna package rule(s).");
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	@SuppressWarnings("unchecked")
	public ActionForward getActivityList(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp) throws Exception {
		String searchStr = req.getParameter("query");
    	String activityType = req.getParameter("activityType");
    	List<BasicDynaBean> activityList = dao.getActivityList(searchStr, activityType, null);
    	Map resultMap = new HashMap();
    	resultMap.put("result",ConversionUtils.listBeanToListMap(activityList));
        String responseContent = new JSONSerializer().deepSerialize(resultMap);

        resp.setContentType("application/json");
        resp.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        resp.getWriter().write(responseContent);
        resp.flushBuffer();
        return null;

    }
}
