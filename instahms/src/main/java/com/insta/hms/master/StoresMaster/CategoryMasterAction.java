package com.insta.hms.master.StoresMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.AbstractDataHandlerAction;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.inventory.stocks.StoreCategoryService;
import com.insta.hms.csvutils.TableDataHandler;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class CategoryMasterAction extends AbstractDataHandlerAction {
	static Logger logger = LoggerFactory.getLogger(CategoryMasterAction.class);
	private static JSONSerializer js = new JSONSerializer().exclude("class");
	private static StoreCategoryService storeCategoryService = ApplicationContextProvider
	      .getBean(StoreCategoryService.class);

	public ActionForward list(ActionMapping m,ActionForm f,HttpServletRequest req,
			HttpServletResponse res) throws IOException ,SQLException, Exception{


		CategoryMasterDAO dao = new CategoryMasterDAO();
		Map map= getParameterMap(req);
		PagedList list = dao.list1(map, ConversionUtils.getListingParameter(map));
		req.setAttribute("pagedList", list);
		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		return m.findForward("addshow");
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		JSONSerializer js = new JSONSerializer().exclude("class");
		CategoryMasterDAO dao = new CategoryMasterDAO();
		Integer catId = Integer.parseInt(req.getParameter("category_id"));
		BasicDynaBean bean = dao.findByKey("category_id", catId);
		boolean inDiscPlan = dao.isCategoryInDiscPlan(catId);
		req.setAttribute("bean", bean);
		req.setAttribute("inDiscPlan", inDiscPlan);
		req.setAttribute("categoriesLists", js.serialize(dao.getCategoriesNamesAndIds()));
		String c =  dao.testCategoryUsed(req.getParameter("category_id"));
		if (c == null || c.equals("")){
			c ="0";
		}
		if (Integer.parseInt(c) > 0) {
			req.setAttribute("dsabled","true");
		} else {
			req.setAttribute("dsabled", "false");
		}
		/** check if user has access right to edit category master*/
		Object roleID = null;
		roleID=  req.getSession(false).getAttribute("roleId");
		int roleId = ((Integer)roleID).intValue();
		req.setAttribute("role", roleId);
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

			CategoryMasterDAO dao = new CategoryMasterDAO();
			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);
			FlashScope flash = FlashScope.getScope(req);

			if (errors.isEmpty()) {
				BasicDynaBean exists = dao.findByKey("category", bean.get("category"));
				if (exists == null) {
					bean.set("category_id", dao.getNextSequence());
					boolean success = dao.insert(con, bean);
					if (success) {
						con.commit();
						flash.success("Category master details inserted successfully..");
						ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
						redirect.addParameter("category_id", bean.get("category_id"));
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						return redirect;
					} else {
						con.rollback();
						flash.error("Failed to add  Category..");
					}
				} else {
					flash.error("Category name already exists..");

				}
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
			ActionRedirect redirect = new ActionRedirect(m.findForward("addRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

			return redirect;
		}finally {
			DataBaseUtil.closeConnections(con, null);
		}
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

			CategoryMasterDAO dao = new CategoryMasterDAO();
			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);

			Object key = Integer.parseInt(req.getParameter("category_id"));
			Map<String, Integer> keys = new HashMap<String, Integer>();
			keys.put("category_id", (Integer)key);
			FlashScope flash = FlashScope.getScope(req);

			if (errors.isEmpty()) {
				BasicDynaBean exists = dao.findByKey("category", bean.get("category"));
				if (exists != null && !key.equals(exists.get("category_id"))) {
					flash.error("Category name already exists..");
				}
				else {
					int success = dao.update(con, bean.getMap(), keys);

					if (success > 0) {
						con.commit();
						flash.success("Inventory master details updated successfully..");

					} else {
						con.rollback();
						flash.error("Failed to update Inventory master details..");
					}
				}
			}
			else {
				flash.error("Incorrectly formatted values supplied");
			}
			ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("category_id", bean.get("category_id"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

			return redirect;
		}finally{
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static TableDataHandler masterHandler = null;

	protected TableDataHandler getDataHandler() {
		if (masterHandler == null) {
			masterHandler = new TableDataHandler(
					"store_category_master",		// table name
					new String[]{"category_id"},	// keys
					new String[]{"category", "status", "identification", "issue_type",
						"billable", "claimable", "retailable", "asset_tracking",
						"expiry_date_val", "discount", "prescribable"
					},
					new String[][]{		// masters
					},
					null
			);
		}

		masterHandler.setSequenceName("store_category_master_seq");
		return masterHandler;
	}

  @SuppressWarnings("unchecked")
  @IgnoreConfidentialFilters
  public ActionForward getCategorydetailsAJAX(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException {
    HttpSession session = (HttpSession) request.getSession(false);
    Map responseMap = new HashMap();
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    responseMap.put("isDrug",
        storeCategoryService
            .getCategoryDetailsById(Integer.parseInt(request.getParameter("category_id")))
            .get("is_drug"));
    response.getWriter().write(js.deepSerialize(responseMap));
    response.flushBuffer();
    return null;
  }

}
