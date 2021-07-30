/**
 *
 */
package com.insta.hms.master.PatientCategory;
import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.CenterPreferences.CenterPreferencesDAO;
import com.insta.hms.master.InsuranceCompMaster.InsuCompMasterDAO;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.util.MessageResources;

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
 * @author deepasri.prasad
 *
 */
public class PatientCategoryAction extends DispatchAction {

	CenterMasterDAO cendao = new CenterMasterDAO();
	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		PatientCategoryDAO dao = new PatientCategoryDAO();
		Map map = req.getParameterMap();
		PagedList pagedList = dao.search(map, ConversionUtils.getListingParameter(req.getParameterMap()));
		req.setAttribute("pagedList", pagedList);
		req.setAttribute("centers", cendao.getAllCentersExceptSuper());

		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
			JSONSerializer js = new JSONSerializer().exclude("class");
			PatientCategoryDAO dao = new PatientCategoryDAO();
			req.setAttribute("tpaJSON", js.serialize(TpaMasterDAO.getActiveTpasNamesAndIds()));
			req.setAttribute("orgJSON", js.serialize(OrgMasterDao.getActiveOrgIdNames()));
			req.setAttribute("companyJSON", js.serialize(InsuCompMasterDAO.getActiveCompanyNames()));
			req.setAttribute("categoryLists", js.serialize(ConversionUtils.copyListDynaBeansToMap(dao.listAll())));
			req.setAttribute("centers", cendao.getAllCentersExceptSuper());
			return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		PatientCategoryDAO dao = new PatientCategoryDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors, true);
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = null;

		if (errors.isEmpty()) {
			BasicDynaBean exists = dao.findByKey("category_name", bean.get("category_name"));
			if (exists == null) {
				bean.set("category_id", dao.getNextId());

				boolean success = dao.insert(con, bean);
				if (success) {
					con.commit();
					redirect = new ActionRedirect(m.findForward("showRedirect"));
					redirect.addParameter("category_id", bean.get("category_id"));
					flash.info("Category details inserted successfully..");
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					DataBaseUtil.closeConnections(con, null);
					return redirect;
				} else {
					con.rollback();
					flash.error("Failed to add Category Details..");
				}
			} else {
				flash.error("Patient Category name already exists..");
			}
		} else {
			flash.error("Incorrectly formatted values supplied");
		}
		DataBaseUtil.closeConnections(con, null);
		redirect = new ActionRedirect(m.findForward("addRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("category_id", bean.get("category_id"));
		return redirect;
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		PatientCategoryDAO dao = new PatientCategoryDAO();
		BasicDynaBean bean = dao.findByKey("category_id", Integer.parseInt(req.getParameter("category_id")));
		req.setAttribute("bean", bean);
		req.setAttribute("tpaJSON", js.serialize(TpaMasterDAO.getActiveTpasNamesAndIds()));
		req.setAttribute("orgJSON", js.serialize(OrgMasterDao.getActiveOrgIdNames()));
		req.setAttribute("companyJSON", js.serialize(InsuCompMasterDAO.getActiveCompanyNames()));
		req.setAttribute("categoryLists", js.serialize(ConversionUtils.copyListDynaBeansToMap(dao.listAll())));
		req.setAttribute("centers", cendao.getAllCentersExceptSuper());
		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = DataBaseUtil.getConnection();
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		FlashScope flash = FlashScope.getScope(req);
		try {
			con.setAutoCommit(false);
			Map params = req.getParameterMap();
			List errors = new ArrayList();

			PatientCategoryDAO dao = new PatientCategoryDAO();
			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors, true);

			if (bean.get("status").equals("I")) {
			  boolean isCategoryUsedInPreference = false;
			  List<BasicDynaBean> preferences = CenterPreferencesDAO.getAllCentersPreferences();
			  for (BasicDynaBean preference : preferences) {
			    if (bean.get("category_id").equals(preference.get("emergency_patient_category_id"))) {
			      isCategoryUsedInPreference = true;
			      break;
			    }
			  }

			  if (isCategoryUsedInPreference) {
			    String errorMessage = ((MessageResources)req.getAttribute(Globals.MESSAGES_KEY)).getMessage("exception.cannot.make.category.inactive.because.it.is.mapped.as.the.emergency.patient.default.patient.category.in.center.preferences");
	        flash.error(errorMessage);
			    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			    redirect.addParameter("category_id",req.getParameter("category_id"));
			    return redirect;
			  }
			}


			Object key = req.getParameter("category_id");
			Map<String, Integer> keys = new HashMap<String, Integer>();
			keys.put("category_id", Integer.parseInt(key.toString()));

			if (errors.isEmpty()) {
				int success = dao.update(con, bean.getMap(), keys);

				if (success > 0) {
					con.commit();
					flash.success("Category details updated successfully..");
				} else {
					con.rollback();
					flash.error("Failed to update Category details..");
				}
			}
			else {
				flash.error("Incorrectly formatted values supplied");
			}
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("category_id",req.getParameter("category_id"));
		return redirect;
	}

}
