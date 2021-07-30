package com.insta.hms.master.OTConsumablesMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OTConsumablesMasterAction extends BaseAction {
  
  private static final GenericDAO operationMasterDAO =  new GenericDAO("operation_master");
  
	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Map requestParams = req.getParameterMap();
		PagedList pagedList = null;
		OTConsumablesMasterDAO dao=new OTConsumablesMasterDAO();
		pagedList = dao.getOtConsumabels(requestParams,
					ConversionUtils.getListingParameter(requestParams));
		JSONSerializer js = new JSONSerializer();
		req.setAttribute("pagedList", pagedList);
		req.setAttribute("operations" ,
				js.serialize((dao.getOperationNames())));


		return m.findForward("list");
	}
	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
			req.setAttribute("operations", new OTConsumablesMasterDAO().getOperationsListForNewConsumable());
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
		String op_id = req.getParameter("op_id");
		String[] hdeleted =(String[])params.get("hdeleted");
		String[] status = (String[])params.get("status");
		int deletedCount = 0;
		try {
			OTConsumablesMasterDAO dao = new OTConsumablesMasterDAO();
			BasicDynaBean bean = dao.getBean();

			FlashScope flash = FlashScope.getScope(req);

			for(int i =0 ;i<consumables.length;i++){
				if(hdeleted[i].equalsIgnoreCase("false")){
				BasicDynaBean exists = dao.findOTByConsumable(Integer.parseInt(consumables[i]), op_id);
				if(exists == null){
					bean.set("consumable_id", Integer.parseInt(consumables[i]));
					bean.set("operation_id", op_id);
					bean.set("qty_needed", new BigDecimal(qty[i]));
					if(status != null)bean.set("status", "I");
					else bean.set("status", "A");
					boolean success = dao.insert(con, bean);
					if (success) {
						con.commit();

					} else {
						con.rollback();
						flash.error("Failed to add  Service Consumable..");
						ActionRedirect addredirect = new ActionRedirect(m.findForward("addRedirect"));
						addredirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						return addredirect;
					}
				}else{

					flash.error("Consumable with same operation name already exists....");
					ActionRedirect addredirect = new ActionRedirect(m.findForward("addRedirect"));
					addredirect.addParameter("consumable_id", consumables[i]);
					addredirect.addParameter("operation_id", op_id);
					addredirect.addParameter("qty_needed", req.getParameter("qty_needed"));
					addredirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					return addredirect;
				}
			} else {

				deletedCount ++;
			}
			}
			if (consumables.length == deletedCount){
				ActionRedirect listRedirect = new ActionRedirect(m.findForwardConfig("listRedirect"));
				flash.success("Successfully deleted");
				listRedirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return listRedirect;
			} else {
				ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
				flash.success("OT Consumable master details inserted successfully..");
				redirect.addParameter("operation_id", bean.get("operation_id"));
                redirect.addParameter("operation_name", operationMasterDAO
                    .findByKey(con, "op_id", bean.get("operation_id")).get("operation_name"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}

		} finally {
			DataBaseUtil.closeConnections(con, null);
		}

	}
	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		JSONSerializer js = new JSONSerializer().exclude("class");
		OTConsumablesMasterDAO dao = new OTConsumablesMasterDAO();
		List<BasicDynaBean> bean = new OTConsumablesMasterDAO().getOperationConsumables(req.getParameter("operation_id"));
		req.setAttribute("consumables", bean);
		req.setAttribute("otConsumablesLists", js.serialize(dao.getOpConsumablesNamesAndIds()));
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
			String operation_id = ((String[])params.get("op_id"))[0];
			String[] hdeleted =(String[])params.get("hdeleted");
			String[] status = (String[])params.get("status");
			int success = 1;
	        boolean flag = true;
	        int deletedCount = 0;
			OTConsumablesMasterDAO dao = new OTConsumablesMasterDAO();
			BasicDynaBean bean = dao.getBean();

			String operationName = (String)(operationMasterDAO.findByKey("op_id", operation_id)).get("operation_name");
			Map<String, Object> keys = new HashMap<String, Object>();

			for(int i =0 ;i<consumables.length;i++){
				keys.put("consumable_id", new BigDecimal(consumables[i]));
				keys.put("operation_id", operation_id.toString());

				BasicDynaBean exists = dao.findOTByConsumable(Integer.parseInt(consumables[i]), operation_id);
				if (exists != null) {
					if(hdeleted[i].equalsIgnoreCase("false")) {
						bean.set("consumable_id", Integer.parseInt(consumables[i]));
						bean.set("operation_id", operation_id);
						bean.set("qty_needed", new BigDecimal(qty[i]));
						if(status != null)bean.set("status", "I");
						else bean.set("status", "A");
						if (success > 0) success = dao.update(con, bean.getMap(), keys);
					} else {
						if (flag){
							flag = dao.delete(con, "operation_id", operation_id, "consumable_id", Integer.parseInt(consumables[i]));
							deletedCount ++;
						}
					}
				}else {
					if(hdeleted[i].equalsIgnoreCase("false")){
						bean.set("consumable_id", Integer.parseInt(consumables[i]));
						bean.set("operation_id", operation_id);
						bean.set("qty_needed", new BigDecimal(qty[i]));
						if(status != null)bean.set("status", "I");
						else bean.set("status", "A");
						if(success > 0){
							flag = dao.insert(con, bean);
							if (flag) success = 1;
							else success = 0;
						}
						else success = 0;
					} else {
						deletedCount ++;
					}
				}
			}
			if (success > 0 || flag) {
				con.commit();
				flash.success("OT Consumable master details updated successfully..");
			} else {
				con.rollback();
				flash.error("Failed to update OT Consumable master details..");
			}

			if (consumables.length == deletedCount) {
				redirect = new ActionRedirect(m.findForward("listRedirect"));
				flash.info("The Operation : <b>"+ operationName +"</b> is removed from OT consumables master.");
			}else {
				redirect = new ActionRedirect(m.findForward("showRedirect"));
				redirect.addParameter("operation_id", bean.get("operation_id"));
				redirect.addParameter("operation_name", operationName);
			}
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}finally {
			if (con !=null) con.close();
		}

	}
}
