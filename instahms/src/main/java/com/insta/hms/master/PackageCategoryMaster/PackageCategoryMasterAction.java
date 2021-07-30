package com.insta.hms.master.PackageCategoryMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

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
 * @author nikunj.s
 *
 */
public class PackageCategoryMasterAction extends DispatchAction {

	static Logger logger = LoggerFactory.getLogger(PackageCategoryMasterAction.class);

	public PackageCategoryMasterDAO dao = new PackageCategoryMasterDAO();

	public ActionForward list(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		Map map= request.getParameterMap();
		List packageCategoryList = PackageCategoryMasterDAO.getAllpackageCategoty();
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("packageCategoryList", js.serialize(packageCategoryList));
		PagedList pagedList = dao.getPackageCategotyMasterDetails(map,ConversionUtils.getListingParameter(request.getParameterMap()));
		request.setAttribute("pagedList", pagedList);

		return m.findForward("list");
	}


	public ActionForward add(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		List packageCategoryList = PackageCategoryMasterDAO.getAllpackageCategoty();
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("packageCategoryList", js.serialize(packageCategoryList));
		return m.findForward("addshow");
	}


	public ActionForward create (ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		Map params = request.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		String error = null;
		boolean success = false;
		try {
			if (errors.isEmpty()) {
				boolean exists = dao.exist("package_category", ((String)(bean.get("package_category"))).trim());
				if (exists) {
					error = "package category name already exists.....";
				} else {
					bean.set("package_category_id", dao.getNextSequence());
					success = dao.insert(con, bean);
					if (!success) {
						error = "Fail to add package category to the master....";
					}
				}
			} else {
				error = "Incorrectly formatted values supplied..";
			}
		}finally {
			DataBaseUtil.commitClose(con, success);
		}
		ActionRedirect redirect = null;
		FlashScope flash = FlashScope.getScope(request);
		if (error != null) {
			redirect = new ActionRedirect(m.findForward("addRedirect"));
			flash.error(error);

		}else {
			redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("package_category_id", bean.get("package_category_id"));
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;

	}


	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		BasicDynaBean bean = dao.findByKey("package_category_id", Integer.parseInt(req.getParameter("package_category_id")));
		req.setAttribute("bean", bean);

		List packageCategoryList = PackageCategoryMasterDAO.getAllpackageCategoty();
		JSONSerializer js = new JSONSerializer().exclude("class");
		req.setAttribute("packageCategoryList", js.serialize(packageCategoryList));
		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map params = req.getParameterMap();
			List errors = new ArrayList();

			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);

			Integer key = Integer.parseInt(req.getParameter("package_category_id"));
			Map<String, Integer> keys = new HashMap<String, Integer>();
			keys.put("package_category_id", key);
			FlashScope flash = FlashScope.getScope(req);

			if (errors.isEmpty()) {
				int success = dao.update(con, bean.getMap(), keys);
				if (success > 0) {
					con.commit();
					flash.success("package category master details updated successfully..");
				} else {
					con.rollback();
					flash.error("Failed to update package category master details..");
				}
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
			ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("package_category_id", key.toString());
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

}
