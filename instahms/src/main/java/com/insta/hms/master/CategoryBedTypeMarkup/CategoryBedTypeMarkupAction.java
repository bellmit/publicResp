package com.insta.hms.master.CategoryBedTypeMarkup;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

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

public class CategoryBedTypeMarkupAction extends BaseAction{

	public ActionForward list(ActionMapping m,ActionForm f,HttpServletRequest req,
			HttpServletResponse res) throws IOException ,SQLException, Exception{
		CategoryBedTypeMarkupDAO dao = new CategoryBedTypeMarkupDAO();

		Map map= getParameterMap(req);
		PagedList list = dao.list(map, ConversionUtils.getListingParameter(map));
		req.setAttribute("pagedList", list);
		ArrayList<String> cats = CategoryBedTypeMarkupDAO.getCats();
		req.setAttribute("cat", cats);
		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		ArrayList<String> cats = CategoryBedTypeMarkupDAO.getCats();
		req.setAttribute("cat", cats);

		return m.findForward("addshow");
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		CategoryBedTypeMarkupDAO dao = new CategoryBedTypeMarkupDAO();
		BasicDynaBean bean = null;
		Object catId = req.getAttribute("category_id");
		Object bed = req.getAttribute("bedtype");
		if (catId == null)  catId = req.getParameter("category_id");
		if (bed == null)  bed = req.getParameter("bed_type");
		if (bed.toString().equalsIgnoreCase("All")) {
			bean = dao.findByKey("category_id", Integer.parseInt(catId.toString()));
			bean.set("bed_type", "All");
		}
		else
			bean = dao.findByKeyColumn(catId.toString(),bed.toString());
		req.setAttribute("bean", bean);
		ArrayList<String> cats = CategoryBedTypeMarkupDAO.getCats();
		req.setAttribute("cat", cats);
		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		String bedType = null;
		boolean success = false;

		CategoryBedTypeMarkupDAO dao = new CategoryBedTypeMarkupDAO();
		BasicDynaBean bean = dao.getBean();
		Map<String, Object> keys = new HashMap<String, Object>();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = null;
		List<String> bedTypes = DataBaseUtil.queryToArrayList1("SELECT bedtype FROM bedcharges_view " +
				"WHERE organization ='ORG0001'");
		try {
			if (errors.isEmpty()) {
				bedType = (String)bean.get("bed_type");
				if (bedType.equalsIgnoreCase("All")) {
					keys.put("category_id", bean.get("category_id"));
					for (String bedName : bedTypes) {
						bean.set("bed_type", bedName);
						keys.put("bed_type", bedName);
						if (dao.findByKeyColumn(req.getParameter("category_id"), bedName) != null) {
							success = dao.update(con, bean.getMap(), keys) > 1;
						}
						else
							success = dao.insert(con, bean);
					}
				} else {
					if (dao.findByKeyColumn(req.getParameter("category_id"), req.getParameter("bed_type")) == null)
						success = dao.insert(con, bean);
				}
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
		} finally {
			if (success) {
				con.commit();
				flash.success("Markup details inserted successfully..");
				redirect = new ActionRedirect(m.findForward("showRedirect"));
				redirect.addParameter("category_id", req.getParameter("category_id"));
				redirect.addParameter("bed_type", req.getParameter("bed_type"));
			} else {
				con.rollback();
				if (errors.isEmpty()) flash.error("Category and Bedtype Markup Rate already exists..");
				redirect = new ActionRedirect(m.findForward("addRedirect"));
			}
			DataBaseUtil.closeConnections(con, null);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		}

		return redirect;

	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		Map params = req.getParameterMap();
		List errors = new ArrayList();
		String error = null;

		CategoryBedTypeMarkupDAO dao = new CategoryBedTypeMarkupDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		Object key = ((Object[])params.get("category_id"))[0];
		Object bed = ((Object[])params.get("bed_type"))[0];
		Map keys = new HashMap();
		keys.put("category_id", Integer.parseInt(key.toString()));
		keys.put("bed_type", bed.toString());
		FlashScope flash = FlashScope.getScope(req);


		if (errors.isEmpty()) {
			int success = dao.update(con, bean.getMap(), keys);

			if (success > 0) {
				con.commit();
				con.close();
				flash.success("Markup details updated successfully..");
			} else {
				con.rollback();
				con.close();
				flash.error("Failed to update markup details..");
			}
		}
		else {
             flash.error("Incorrectly formatted values supplied");
		}
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter("category_id", bean.get("category_id"));
		redirect.addParameter("bed_type", bean.get("bed_type"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward delete(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		Connection con = DataBaseUtil.getConnection();
		String error = "";

		boolean status = CategoryBedTypeMarkupDAO.deleteMarkupDetails(con, req.getParameter("category_id"), req.getParameter("bed_type"));
		if (status) error = "Markup details deleted successfully....";
		else error = "Transaction failure";
		FlashScope flash = FlashScope.getScope(req);
		flash.put("success", error);
		ActionRedirect redirect = new ActionRedirect(m.findForward("listRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}



}
