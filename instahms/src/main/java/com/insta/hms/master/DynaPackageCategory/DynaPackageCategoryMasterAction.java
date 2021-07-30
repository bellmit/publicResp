/**
 *
 */
package com.insta.hms.master.DynaPackageCategory;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.DynaPackage.DynaPackageCategoryLimitsDAO;
import com.insta.hms.master.DynaPackage.DynaPackageChargesDAO;
import com.insta.hms.master.DynaPackage.DynaPackageDAO;
import com.insta.hms.master.DynaPackageRules.DynaPackageRulesMasterDAO;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author lakshmi
 *
 */
public class DynaPackageCategoryMasterAction extends BaseAction {

	static Logger logger = LoggerFactory.getLogger(DynaPackageCategoryMasterAction.class);

	DynaPackageDAO pkgdao = new DynaPackageDAO();
	DynaPackageChargesDAO cdao = new DynaPackageChargesDAO();
	DynaPackageCategoryMasterDAO dao = new DynaPackageCategoryMasterDAO();
	DynaPackageRulesMasterDAO ruledao = new DynaPackageRulesMasterDAO();
	DynaPackageCategoryLimitsDAO catlimitdao = new DynaPackageCategoryLimitsDAO();

	public ActionForward list(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res)  throws Exception {

		Map filter = getParameterMap(req);
		PagedList pagedList = dao.search(filter, ConversionUtils.getListingParameter(filter), "dyna_pkg_cat_name");
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
		Connection con= null;

		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		FlashScope flash = FlashScope.getScope(req);

		ActionRedirect redirect = null;
		boolean success = false;

		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			if (errors.isEmpty()) {
				BasicDynaBean exists = dao.findExistsByKey("dyna_pkg_cat_name", bean.get("dyna_pkg_cat_name"));
				if(exists == null) {
					int newCategoryId = dao.getNextSequence();
					String username = (String)req.getSession(false).getAttribute("userid");
					bean.set("username", username);
					bean.set("dyna_pkg_cat_id", newCategoryId);
					success = dao.insert(con, bean);

					BasicDynaBean useCategoryBean = dao.getACategory();
					BasicDynaBean usePackageBean = pkgdao.getAPackage();
					boolean haslimits = catlimitdao.hasCategoryLimits(useCategoryBean);
					boolean hascharges = cdao.hasPackageCharges(usePackageBean);

					if (success && hascharges) {
						if (haslimits) {
							int useCategoryId = (Integer)useCategoryBean.get("dyna_pkg_cat_id");
							// Add new category details into package category limits based on an existing category.
							success = catlimitdao.addCategoryForDynaPackages(con, useCategoryId, newCategoryId, username);

						}else {
							// Insert category details into package category limits.
							success = catlimitdao.insertCategoryForDynaPackages(con, newCategoryId, username);
						}
					}

					if (success) {
						redirect = new ActionRedirect(m.findForward("showRedirect"));
						if (hascharges)
							flash.info("Dyna package category inserted and saved category limits details in all Dyna Packages.");
						else
							flash.info("Dyna package category inserted. No Dyna packages to insert category limits.");
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						redirect.addParameter("dyna_pkg_cat_id", bean.get("dyna_pkg_cat_id"));
						return redirect;
					} else {
						flash.error("Failed to add Dyna package category.");
					}
				} else {
					flash.error( "Duplicate dyna package category: "+ bean.get("dyna_pkg_cat_name"));
				}
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		redirect = new ActionRedirect(m.findForward("addRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
		throws IOException, ServletException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		BasicDynaBean bean = dao.findByKey("dyna_pkg_cat_id", Integer.parseInt(req.getParameter("dyna_pkg_cat_id")));
		req.setAttribute("bean", bean);
		req.setAttribute("DynaPkgCategoryNamesList", js.serialize(ConversionUtils.copyListDynaBeansToMap(dao.listAll())));
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
		FlashScope flash = FlashScope.getScope(req);

		if (errors.isEmpty()) {
			BasicDynaBean exists = dao.findExistsByKey("dyna_pkg_cat_name", bean.get("dyna_pkg_cat_name"));

			if ( exists != null && !exists.get("dyna_pkg_cat_name").equals(bean.get("dyna_pkg_cat_name")) ) {
				flash.error( "Dyna package category: " + bean.get("dyna_pkg_cat_name") +" already exists.");
			}
			else {
				String username = (String)req.getSession(false).getAttribute("userid");
				bean.set("username", username);
				int success = dao.updateWithName(con, bean.getMap(), "dyna_pkg_cat_id");

				if (success > 0) {
					con.commit();
					flash.success("Dyna package category details updated successfully..");

				} else {
					con.rollback();
					flash.error("Failed to update Dyna package category details..");
				}
			}
		}
		else {
			flash.error("Incorrectly formatted values supplied");
		}

		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter("dyna_pkg_cat_id", bean.get("dyna_pkg_cat_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}

	public ActionForward delete(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = null;
		Map params = req.getParameterMap();
		List errors = new ArrayList();

		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter("dyna_pkg_cat_id", bean.get("dyna_pkg_cat_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		String categoryName = null;
		String err = null;

		if (errors.isEmpty()) {
			boolean success = false;
			categoryName = (String)bean.get("dyna_pkg_cat_name");

			List<BasicDynaBean> rulesWithCategory = ruledao.findAllByKey("dyna_pkg_cat_id", bean.get("dyna_pkg_cat_id"));
			if (rulesWithCategory != null && rulesWithCategory.size() > 0) {
				flash.error("Cannot delete the category. One or more rules exists with this category.");
				return redirect;
			}

			try {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);

				// Delete category details from package category limits if no rule exists.
				if (rulesWithCategory == null || rulesWithCategory.size() == 0) {
					boolean haslimits = catlimitdao.hasCategoryLimits(bean);
					if (haslimits)
						success = catlimitdao.delete(con, "dyna_pkg_cat_id", bean.get("dyna_pkg_cat_id"));
					else
						success = true;
				}

				if (success)
					success = dao.delete(con, "dyna_pkg_cat_id", bean.get("dyna_pkg_cat_id"));

				if (!success)
					err = "Failed to delete Dyna package category details..";

			}catch (SQLException sqle) {
				success = false;
				err = "Error while deleting category : " +sqle.getMessage();
			}catch (Exception e) {
				success = false;
				throw e;
			}finally {
				DataBaseUtil.commitClose(con, success);
			}

			if (success)
				flash.info("Deleted dyna package category : <b>"+categoryName
							+"</b> and category limits details from all Dyna Packages.");
		}
		else {
			err = "Incorrectly formatted values supplied";
		}

		if (err != null)
			flash.error(err);

		redirect = new ActionRedirect(m.findForward("listRedirect"));
		redirect.addParameter("sortOrder", "dyna_pkg_cat_name");
		redirect.addParameter("sortReverse", "false");
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}
}
