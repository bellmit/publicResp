package com.insta.hms.master.OrderKitMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.stores.PharmacymasterDAO;
import flexjson.JSONSerializer;

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

public class OrderKitMasterAction extends BaseAction{
	JSONSerializer js = new JSONSerializer().exclude("class");
	OrderKitMasterDAO dao = new OrderKitMasterDAO();
	
	private static final GenericDAO orderKitDetailsDAO = new GenericDAO("order_kit_details");
	
	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, ServletException, ParseException {
		Map params = request.getParameterMap();
		PagedList list = dao.search(params, ConversionUtils.getListingParameter(params), "order_kit_id");
		request.setAttribute("pagedList", list);
		return mapping.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException, Exception {
		req.setAttribute("master_timestamp", PharmacymasterDAO.getItemMasterTimestamp());
		req.setAttribute("orderkit_names", js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(dao.getOrderKitNames(null))));
		return m.findForward("addshow");
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
		String kitId = req.getParameter("order_kit_id");
		req.setAttribute("kit", dao.findByKey("order_kit_id", Integer.parseInt(kitId)));
		req.setAttribute("kit_items", dao.getKitItemDetails(Integer.parseInt(kitId)));
		req.setAttribute("orderkit_names", js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(dao.getOrderKitNames(kitId))));
		req.setAttribute("master_timestamp", PharmacymasterDAO.getItemMasterTimestamp());
		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ServletException {
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		FlashScope flash = FlashScope.getScope(request);
		Map params = request.getParameterMap();
		String[] kitItemId = (String[])params.get("medicine_id");
		String[] kitId = (String[])params.get("order_kit_id");
		String[] deleted = (String[])params.get("deleted");
		
		List errors = new ArrayList();
		BasicDynaBean bean = dao.getBean();

		ConversionUtils.copyToDynaBean(params, bean, errors);

		String error = null;
		boolean success = true;
		if (errors.isEmpty()) {
			if (dao.exist("order_kit_name", bean.get("order_kit_name"))) {
				flash.put("error", "Order Kit Name "+ bean.get("order_kit_name")+" already exists");
			} else {
				Connection con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				bean.set("order_kit_id", dao.getNextSequence());

				try {
					if (dao.insert(con, bean)) {
						for(int i = 0;i<kitItemId.length-1;i++){
							BasicDynaBean itemBean = orderKitDetailsDAO.getBean();
							ConversionUtils.copyIndexToDynaBean(params, i, itemBean);
							itemBean.set("status", "A");
							itemBean.set("order_kit_id", (Integer)bean.get("order_kit_id"));
							if(deleted[i].equals("N"))
							success = orderKitDetailsDAO.insert(con, itemBean);
						}
						flash.put("info", "Order Kit created successfully.");
					} else {
						flash.put("error", "Failed to insert the Order Kit.");
					}
				} finally {
					DataBaseUtil.commitClose(con, success);
				}
			}
		} else {
			flash.put("error", "Incorrectly formatted values supplied.");

		}
		if (error == null) {
			redirect.addParameter("order_kit_id", bean.get("order_kit_id"));
		} else {
			redirect = new ActionRedirect(mapping.findForward("addRedirect"));
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ServletException {
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		Map params = request.getParameterMap();
		String[] kitItemId = (String[])params.get("medicine_id");
		String orderKitId = request.getParameter("order_kit_id");
		String[] sqlquery = (String[])params.get("sqlquery");
		String[] deleted = (String[])params.get("deleted");

		BasicDynaBean bean = dao.getBean();

		List errorFields = new ArrayList();
		ConversionUtils.copyToDynaBean(params, bean, errorFields);

		Map keys = new HashMap();
		keys.put("order_kit_id", bean.get("order_kit_id"));
		FlashScope flash = FlashScope.getScope(request);
		String error = null;

		if (errorFields.isEmpty()) {
			Connection con = DataBaseUtil.getConnection();
			try {
				if (dao.update(con, bean.getMap(), keys) > 0) {
					for(int i = 0;i<kitItemId.length-1;i++){
						BasicDynaBean itemBean = orderKitDetailsDAO.getBean();
						ConversionUtils.copyIndexToDynaBean(params, i, itemBean);

						if ( sqlquery[i] != null && sqlquery[i].isEmpty() ) {
							if(deleted[i].equals("N")){
								itemBean.set("order_kit_id", (Integer)bean.get("order_kit_id"));
								itemBean.set("medicine_id", Integer.parseInt(kitItemId[i]));
								itemBean.set("status", "A");
								orderKitDetailsDAO.insert(con, itemBean);
							}
						} else {
							Map itemkeys = new HashMap();
							itemkeys.put("medicine_id", Integer.parseInt(kitItemId[i]));
							itemkeys.put("order_kit_id", Integer.parseInt(orderKitId));
							if ( deleted[i].equals("Y"))
							  orderKitDetailsDAO.delete(con, "medicine_id",Integer.parseInt(kitItemId[i]),"order_kit_id",Integer.parseInt(orderKitId));
							else
								if(sqlquery[i].equalsIgnoreCase("update"))
								  orderKitDetailsDAO.update(con, itemBean.getMap(), itemkeys);
						}
					}
					flash.put("info", "Updated Order Kit Details.");
				} else {
					flash.put("error",  "Failed to update Order Kit Details.");
				}
			} finally {
				DataBaseUtil.closeConnections(con, null);
			}
		} else {
			flash.put("error", "Incorrectly formatted values supplied.");
		}
		
		redirect.addParameter("order_kit_id", bean.get("order_kit_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

}
