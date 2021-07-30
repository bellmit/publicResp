package com.insta.hms.master.ServiceConsumableMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.ServiceMaster.ServiceMasterDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServiceConsumableMasterAction extends DispatchAction{
	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");

		ServiceConsumableMasterDAO dao = new ServiceConsumableMasterDAO();
		ServiceMasterDAO serviceMasterDao = new ServiceMasterDAO();
		Map requestParams = req.getParameterMap();
		PagedList pagedList = null;
		pagedList = dao.getServiceConsumbaleDetailPages(requestParams,
					ConversionUtils.getListingParameter(requestParams));
		req.setAttribute("pagedList", pagedList);
		req.setAttribute("namesJSON", js.serialize(serviceMasterDao.getAllNames()));
		req.setAttribute("filterclosed", true);

		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
			req.setAttribute("consumables",null);
			req.setAttribute("services_list", new ServiceConsumableMasterDAO().getServiceToMapConsumables());
		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		String[] consumables = (String[])params.get("consumable");
		String[] qty = (String[]) params.get("qty");
		String service_id = req.getParameter("service_id");
		String[] hdeleted =(String[])params.get("hdeleted");
		int delItemsCount =0;

		ServiceConsumableMasterDAO dao = new ServiceConsumableMasterDAO();
		BasicDynaBean bean = dao.getBean();
		try {
			FlashScope flash = FlashScope.getScope(req);
			ActionRedirect redirect = null;
			for(int i =0 ;i<consumables.length;i++){
				if(hdeleted[i].equalsIgnoreCase("false")){
				BasicDynaBean exists = dao.findTestBykey(Integer.parseInt(consumables[i]), service_id);
				if(exists == null){
					bean.set("consumable_id", Integer.parseInt(consumables[i]));
					bean.set("service_id", service_id);
					bean.set("quantity_needed", new BigDecimal(qty[i]));
					bean.set("status", "A");
					boolean success = dao.insert(con, bean);
					if (success) {
						con.commit();

					} else {
						con.rollback();
						redirect = new ActionRedirect(m.findForward("addRedirect"));
						flash.error("Failed to add  Service Consumable..");
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						return redirect;
					}
				}else{
					ActionRedirect addredirect = new ActionRedirect(m.findForward("addRedirect"));
					flash.error("Consumable with same service name already exists....");
					addredirect.addParameter("consumable_id", consumables[i]);
					addredirect.addParameter("service_id", service_id);
					addredirect.addParameter("quantity_needed", req.getParameter("quantity_needed"));
					addredirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					return addredirect;
				}
			} else {

				delItemsCount ++;
			}
			}
			if (consumables.length == delItemsCount) {

				redirect = new ActionRedirect(m.findForward("listRedirect"));
				flash.success("Successfully Deleted");
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			} else {
				redirect = new ActionRedirect(m.findForward("showRedirect"));
				flash.success("Service Consumable master details inserted successfully..");
				redirect.addParameter("service_id", bean.get("service_id"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			}
			return redirect;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		String serviceid = req.getParameter("service_id");
		JSONSerializer js = new JSONSerializer().exclude("class");
		ServiceConsumableMasterDAO dao = new ServiceConsumableMasterDAO();
		List service_list = new ServiceConsumableMasterDAO().getServiceToMapConsumables();
		String service_name = ServiceConsumableMasterDAO.getServiceName(serviceid);
		Hashtable<String, String> table = new Hashtable<String, String>();
		table.put("SERVICE_ID", serviceid);
		table.put("SERVICE_NAME",service_name);
		service_list.add(table);
		List bean = dao.findTestBykey(serviceid);

		req.setAttribute("bean", bean);
		req.setAttribute("serviceName", service_name);
		req.setAttribute("consumables", js.serialize(ConversionUtils.copyListDynaBeansToMap(bean)));
		req.setAttribute("service_list", service_list);
		req.setAttribute("serviceconsumablesLists", js.serialize(dao.getServiceConsumablesNamesAndIds()));

		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = null;
		Connection con = null;
		Map params = req.getParameterMap();

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			String[] consumables = (String[])params.get("consumable");
			String[] qty = (String[]) params.get("qty");
			String service_id = req.getParameter("service_name");
			String[] hdeleted =(String[])params.get("hdeleted");
			String[] status = (String[])params.get("status");
			int success = 1;
	        boolean flag = true;
	        int delItemsCount = 0 ;
			ServiceConsumableMasterDAO dao = new ServiceConsumableMasterDAO();
			BasicDynaBean bean = dao.getBean();

			String serviceName = (String)(new GenericDAO("services").findByKey("service_id", service_id)).get("service_name");
			Map<String, Object> keys = new HashMap<String, Object>();

			for (int i =0 ;i<consumables.length;i++) {
				keys.put("consumable_id", new BigDecimal(consumables[i]));
				keys.put("service_id", service_id.toString());

				BasicDynaBean exists = dao.findTestBykey(Integer.parseInt(consumables[i]), service_id);
				if (exists != null) {
					if(hdeleted[i].equalsIgnoreCase("false")) {
						bean.set("consumable_id", Integer.parseInt(consumables[i]));
						bean.set("service_id", service_id);
						bean.set("quantity_needed", new BigDecimal(qty[i]));
						if(status != null)bean.set("status", "I");
						else bean.set("status", "A");
						if (success > 0) success = dao.update(con, bean.getMap(), keys);
					} else {
						if (flag) flag = dao.delete(con, "service_id", service_id, "consumable_id", Integer.parseInt(consumables[i]));
						delItemsCount ++;
					}
				}else {
					if(hdeleted[i].equalsIgnoreCase("false")){
						bean.set("consumable_id", Integer.parseInt(consumables[i]));
						bean.set("service_id", service_id);
						bean.set("quantity_needed", new BigDecimal(qty[i]));
						if(status != null)bean.set("status", "I");
						else bean.set("status", "A");
						if(success > 0){
							flag = dao.insert(con, bean);
							if (flag) success = 1;
							else success = 0;
						}
						else success = 0;
					} else {
						delItemsCount ++;
					}
				}
			}

			if (success > 0 || flag) {
				con.commit();
				flash.success("Service Consumable master details updated successfully..");
			} else {
				con.rollback();
				flash.error("Failed to update Service Consumable master details..");
			}

			if (consumables.length == delItemsCount) {
				redirect = new ActionRedirect(m.findForward("listRedirect"));
				flash.info("The Service : <b>"+ serviceName +"</b> is removed from service consumables master.");
			}else {
				redirect = new ActionRedirect(m.findForward("showRedirect"));
				redirect.addParameter("service_id", bean.get("service_id"));
			}
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;

		}finally {
			if (con !=null) con.close();
		}
	}
}
