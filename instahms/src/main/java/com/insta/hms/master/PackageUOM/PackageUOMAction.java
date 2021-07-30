package com.insta.hms.master.PackageUOM;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.AbstractDataHandlerAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.csvutils.TableDataHandler;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class PackageUOMAction extends AbstractDataHandlerAction {

	static Logger logger = LoggerFactory.getLogger(PackageUOMAction.class);

	public ActionForward list(ActionMapping m,ActionForm f,HttpServletRequest req,
			HttpServletResponse res) throws IOException ,SQLException, Exception {
		Map map= getParameterMap(req);
		PagedList list = PackageUOMDAO.searchPackageUOMs(map, ConversionUtils.getListingParameter(map));
		req.setAttribute("pagedList", list);
		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		req.setAttribute("pkgUOMS", js.serialize(ConversionUtils.copyListDynaBeansToMap(new PackageUOMDAO().listAll())));
		req.setAttribute("isuuePackageList", js.serialize(ConversionUtils.listBeanToListMap(PackageUOMDAO.getPackIssueUOMs())));
		return m.findForward("addshow");
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		req.setAttribute("pkgUOMS", js.serialize(ConversionUtils.copyListDynaBeansToMap(new PackageUOMDAO().listAll())));
		req.setAttribute("isuuePackageList", js.serialize(ConversionUtils.listBeanToListMap(PackageUOMDAO.getPackIssueUOMs())));

		List<BasicDynaBean> packageUomList = new PackageUOMDAO().findAllByKey("package_uom", req.getParameter("package_uom"));
		BasicDynaBean bean = null;
		for (int i=0;i<packageUomList.size();i++) {
			if (((String)packageUomList.get(i).get("issue_uom")).equals(req.getParameter("issue_uom"))) {
				bean = packageUomList.get(i);
			}
		}
		req.setAttribute("bean", bean);
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
			PackageUOMDAO masterDAO = new PackageUOMDAO();

			BasicDynaBean bean = masterDAO.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);
			boolean success = false;

			FlashScope flash = FlashScope.getScope(req);

			if (errors.isEmpty()) {
				success = new PackageUOMDAO().insert(con, bean);
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
			if (success)
				flash.info("Package UOM created succesfully.");
			ActionRedirect redirect = new ActionRedirect(m.findForward("addRedirect"));
			redirect.addParameter("issue_uom", bean.get("issue_uom"));
			redirect.addParameter("package_uom", bean.get("package_uom"));
			redirect.addParameter("integration_uom_id", bean.get("integration_uom_id"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;

		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = null;
		Map params = req.getParameterMap();
		List errors = new ArrayList();
		PackageUOMDAO masterDAO = new PackageUOMDAO();
		GenericDAO gDAO = new GenericDAO("store_item_details");

		BasicDynaBean bean = masterDAO.getBean();
		BasicDynaBean itembean = gDAO.getBean();
		itembean.set("issue_units", req.getParameter("originalissueUOM"));
		itembean.set("package_uom", req.getParameter("originalpkgUOM"));
		itembean.set("issue_base_unit", new BigDecimal(req.getParameter("originalPkgSize")));

		ConversionUtils.copyToDynaBean(params, bean, errors);
		FlashScope flash = FlashScope.getScope(req);
		try {
			con = DataBaseUtil.getConnection();
			if (errors.isEmpty()) {
				Map keys = new HashMap();
				Map keys1 = new HashMap();
				keys.put("package_uom", req.getParameter("originalpkgUOM"));
				keys.put("issue_uom", req.getParameter("originalissueUOM"));
				keys1.put("package_uom", req.getParameter("originalpkgUOM"));
				keys1.put("issue_units", req.getParameter("originalissueUOM"));
				int i = masterDAO.update(con, bean.getMap(), keys);
				if (i > 0)
					flash.info("Package UOM updated successfully.");
				else
					flash.error("Failed to update");
				List<BasicDynaBean> itemsOfUoM = gDAO.listAll(null,keys1,null);

				if(itemsOfUoM != null && !itemsOfUoM.isEmpty() && itemsOfUoM.size()>0 ){
					int j = gDAO.update(con, itembean.getMap(), keys1);
					if (j > 0)
						flash.info("Package UOM updated successfully...<br/>Store Item details also updated successfully.");
					else
						flash.error("Failed to update Store Item details.");
				}

			}else {
				flash.error("Incorrectly formatted values supplied");
			}
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter("package_uom", itembean.get("package_uom"));
		redirect.addParameter("issue_uom", itembean.get("issue_units"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}
	/*
	public ActionForward deleteUOMs( ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, Exception {
		Map map =  request.getParameterMap();
		String[] delItems = (String[]) map.get("_delete");
		String[] deleteFlag = (String[]) map.get("_deleteflag");
		String[] deleteOnPacakageUOM = (String[]) map.get("_deleteOnPacakageUOM");
		String[] delItemsOnIssueUOM = (String[]) map.get("_deleteOnIsssueUOM");
		FlashScope flash = FlashScope.getScope(request);
		Connection con = null;
		boolean success = true;
		PackageUOMDAO masterDAO = new PackageUOMDAO();
		try {
			con = DataBaseUtil.getConnection();
			if (deleteFlag != null && deleteOnPacakageUOM != null && delItemsOnIssueUOM != null) {
				for (int i=0;i<deleteFlag.length;i++) {
						if (deleteFlag[i].equals("true")) {
							success = masterDAO.delete(con, "package_uom", deleteOnPacakageUOM[i], "issue_uom", delItemsOnIssueUOM[i]);
						}

					if (!success)
						break;
				}
			} else {
				success = false;
			}
		} finally{
			DataBaseUtil.commitClose(con, success);
		}
		if (success)
			flash.info("UOM(s) deleted successfully");
		else
			flash.error("Failed to delete");

		ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}*/

	private static TableDataHandler masterHandler = null;

	protected TableDataHandler getDataHandler() {
		if (masterHandler == null) {
			masterHandler = new TableDataHandler(
					"package_issue_uom",		// table name
					new String[]{"package_uom", "issue_uom"},	// keys
					new String[]{"package_size","integration_uom_id"},
					new String[][]{	/* masters */ },
					null
			);
		}
		return masterHandler;
	}


}
