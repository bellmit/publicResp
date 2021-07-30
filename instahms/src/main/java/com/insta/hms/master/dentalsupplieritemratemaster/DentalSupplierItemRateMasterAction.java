package com.insta.hms.master.dentalsupplieritemratemaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.dentalsupplier.DentalSupplierMasterDAO;
import com.insta.hms.master.dentalsupplies.DentalSuppliesMasterDAO;
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

public class DentalSupplierItemRateMasterAction extends DispatchAction{
	static Logger logger = LoggerFactory.getLogger(DentalSupplierItemRateMasterAction.class);

	DentalSupplierItemRateMasterDAO dao = new DentalSupplierItemRateMasterDAO();
	DentalSupplierMasterDAO sdao = new DentalSupplierMasterDAO();
	DentalSuppliesMasterDAO ddao = new DentalSuppliesMasterDAO();
	JSONSerializer js = new JSONSerializer().exclude("class");

	public ActionForward list(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		Map map= request.getParameterMap();
		List suppliersList = sdao.getAllSuppliers();
		List itemList = ddao.getAllSupplies();
		request.setAttribute("suppliersList", js.serialize(suppliersList));
		request.setAttribute("itemList", js.serialize(itemList));
		PagedList pagedList = dao.getDentalItemRateDetails(map,ConversionUtils.getListingParameter(request.getParameterMap()));
		request.setAttribute("pagedList", pagedList);

		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		List<BasicDynaBean> dentalSupplireItemRateList = dao.listAll();
		request.setAttribute("dentalSupplireItemRateList", js.serialize(ConversionUtils.copyListDynaBeansToMap(dentalSupplireItemRateList)));
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
		Map<String,Object> identifiers = new HashMap<String, Object>();
		identifiers.put("item_id", Integer.parseInt(request.getParameter("item_name")));
		identifiers.put("supplier_id", Integer.parseInt(request.getParameter("supplier_name")));
		try {
			if (errors.isEmpty()) {
				BasicDynaBean exists = dao.findByKey(identifiers);
				if (exists != null) {
					error = "association of item name and supplier name already exists.....";
				} else {
					bean.set("item_supplier_rate_id", dao.getNextSequence());
					bean.set("item_id", Integer.parseInt(request.getParameter("item_name")));
					bean.set("supplier_id", Integer.parseInt(request.getParameter("supplier_name")));
					success = dao.insert(con, bean);
					if (!success) {
						error = "Fail to add dental item supplier rate to the master....";
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
			redirect.addParameter("item_supplier_rate_id", bean.get("item_supplier_rate_id"));
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;

	}


	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		BasicDynaBean bean = dao.findByKey("item_supplier_rate_id", Integer.parseInt(req.getParameter("item_supplier_rate_id")));
		req.setAttribute("bean", bean);
		List<BasicDynaBean> dentalSupplireItemRateList = dao.listAll();
		req.setAttribute("dentalSupplireItemRateList", js.serialize(ConversionUtils.copyListDynaBeansToMap(dentalSupplireItemRateList)));
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
			String pItemId = req.getParameter("c_item_id");
			String pSupplierId = req.getParameter("c_supplier_id");

			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);
			bean.set("item_id", Integer.parseInt(req.getParameter("item_name")));
			bean.set("supplier_id", Integer.parseInt(req.getParameter("supplier_name")));

			Integer key = Integer.parseInt(req.getParameter("item_supplier_rate_id"));
			Map<String, Integer> keys = new HashMap<String, Integer>();
			keys.put("item_supplier_rate_id", key);
			FlashScope flash = FlashScope.getScope(req);

			if (errors.isEmpty()) {
				int success = dao.update(con, bean.getMap(), keys);
				if (success > 0) {
					con.commit();
					flash.success("Dental Item Supplier Rate master details updated successfully..");
				} else {
					con.rollback();
					flash.error("Failed to update Dental Item Supplier Rate master details..");
				}
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
			ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("item_supplier_rate_id", key.toString());
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}
}
