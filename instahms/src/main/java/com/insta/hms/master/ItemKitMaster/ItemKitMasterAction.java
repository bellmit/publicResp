package com.insta.hms.master.ItemKitMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.stores.PharmacymasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ItemKitMasterAction extends BaseAction{

	ItemKitMasterDAO dao = new ItemKitMasterDAO();
    private static final GenericDAO storeKitDetailsDAO = new GenericDAO("store_kit_details");
	
	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, ServletException, ParseException {
		Map params = request.getParameterMap();
		PagedList list = dao.search(params, ConversionUtils.getListingParameter(params), "kit_id");
		request.setAttribute("pagedList", list);
		return mapping.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		req.setAttribute("master_timestamp", PharmacymasterDAO.getItemMasterTimestamp());
		return m.findForward("addshow");
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
		String kitId = req.getParameter("kit_id");
		req.setAttribute("kit", dao.findByKey("kit_id", Integer.parseInt(kitId)));
		req.setAttribute("kit_items", dao.getKitItemDetails(Integer.parseInt(kitId)));
		req.setAttribute("master_timestamp", PharmacymasterDAO.getItemMasterTimestamp());
		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ServletException {
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		FlashScope flash = FlashScope.getScope(request);
		Map params = request.getParameterMap();
		String[] kitItemId = (String[])params.get("kit_item_id");
		String[] kitId = (String[])params.get("kit_id");

		List errors = new ArrayList();
		BasicDynaBean bean = dao.getBean();

		ConversionUtils.copyToDynaBean(params, bean, errors);

		String error = null;
		boolean success = true;
		if (errors.isEmpty()) {
			if (dao.exist("kit_name", bean.get("kit_name"))) {
				error = "Kit Name "+ bean.get("kit_name")+" already exists";
			} else {
				Connection con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				bean.set("kit_id", dao.getNextSequence());

				try {
					if (dao.insert(con, bean)) {
						for(int i = 0;i<kitItemId.length-1;i++){
							BasicDynaBean itemBean = storeKitDetailsDAO.getBean();
							ConversionUtils.copyIndexToDynaBean(params, i, itemBean);

							itemBean.set("kit_id", (Integer)bean.get("kit_id"));
							success = storeKitDetailsDAO.insert(con, itemBean);
						}
					} else {
						error = "Failed to insert the kit";
					}
				} finally {
					DataBaseUtil.commitClose(con, success);
				}
			}
		} else {
			error = "Incorrectly formatted values supplied..";

		}
		if (error == null) {
			redirect.addParameter("kit_id", bean.get("kit_id"));
		} else {
			redirect = new ActionRedirect(mapping.findForward("addRedirect"));
			flash.put("error", error);
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ServletException {
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		Map params = request.getParameterMap();
		String[] kitItemId = (String[])params.get("kit_item_id");
		String[] kit_item_details_id = (String[])params.get("kit_item_detail_id");
		String[] deleted = (String[])params.get("deleted");

		BasicDynaBean bean = dao.getBean();

		List errorFields = new ArrayList();
		ConversionUtils.copyToDynaBean(params, bean, errorFields);

		Map keys = new HashMap();
		keys.put("kit_id", bean.get("kit_id"));
		FlashScope flash = FlashScope.getScope(request);
		String error = null;

		if (errorFields.isEmpty()) {
			Connection con = DataBaseUtil.getConnection();
			try {
				if (dao.update(con, bean.getMap(), keys) > 0) {
					for(int i = 0;i<kitItemId.length-1;i++){
						BasicDynaBean itemBean = storeKitDetailsDAO.getBean();
						ConversionUtils.copyIndexToDynaBean(params, i, itemBean);

						if ( kit_item_details_id[i] == null || kit_item_details_id[i].isEmpty()) {
							itemBean.set("kit_id", (Integer)bean.get("kit_id"));
							storeKitDetailsDAO.insert(con, itemBean);
						} else {
							Map itemkeys = new HashMap();
							itemkeys.put("kit_item_detail_id", Integer.parseInt(kit_item_details_id[i]));

							if ( deleted[i].equals("Y"))
							  storeKitDetailsDAO.delete(con, "kit_item_detail_id",Integer.parseInt(kit_item_details_id[i]));
							else
							  storeKitDetailsDAO.update(con, itemBean.getMap(), itemkeys);
						}
					}
				} else {
					error = "Failed to update Kit Details..";
				}
			} finally {
				DataBaseUtil.closeConnections(con, null);
			}
		} else {
			error = "Incorrectly formatted values supplied..";
		}
		if (error != null) {
			flash.put("error", error);
		}
		redirect.addParameter("kit_id", bean.get("kit_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

}
