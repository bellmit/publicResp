package com.insta.hms.master.DiagnosticReagentMaster;


import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.diagnosticsmasters.addtest.AddTestDAOImpl;
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

public class DiagnosticReagentMasterAction extends DispatchAction{

	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		JSONSerializer js = new JSONSerializer().exclude("class");
		DiagnosticReagentMasterDAO dao = new DiagnosticReagentMasterDAO();
		Map requestParams = req.getParameterMap();
		PagedList pagedList = null;
		pagedList = dao.getDiagReagentDetailPages(requestParams,
					ConversionUtils.getListingParameter(requestParams));
		req.setAttribute("pagedList", pagedList);
		req.setAttribute("testnames", js.serialize(AddTestDAOImpl.getAllTestNames()));
		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
			req.setAttribute("reagents",null);
			req.setAttribute("test_list", new DiagnosticReagentMasterDAO().getTestToMapReagents());
		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = null;
		String[] reagents = (String[])params.get("reagent");
		String[] qty = (String[]) params.get("qty");
		String test_id = req.getParameter("test_id");
		String[] hdeleted =(String[])params.get("hdeleted");
		int delItemsCount = 0;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			DiagnosticReagentMasterDAO dao = new DiagnosticReagentMasterDAO();
			BasicDynaBean bean = dao.getBean();

			FlashScope flash = FlashScope.getScope(req);
			ActionRedirect redirect = null;
			for(int i =0 ;i<reagents.length;i++){
				if(hdeleted[i].equalsIgnoreCase("false")){
				BasicDynaBean exists = dao.findTestBykey(Integer.parseInt(reagents[i]), test_id);
				if(exists == null){
					bean.set("reagent_id", Integer.parseInt(reagents[i]));
					bean.set("test_id", test_id);
					bean.set("quantity_needed", new BigDecimal(qty[i]));
					bean.set("status", "A");
					boolean success = dao.insert(con, bean);
					if (success) {
						con.commit();
						flash.success("DiagnosticReagent master details inserted successfully..");
						redirect = new ActionRedirect(m.findForward("showRedirect"));
						redirect.addParameter("test_id", bean.get("test_id"));
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					} else {
						con.rollback();
						redirect = new ActionRedirect(m.findForward("addRedirect"));
						flash.error("Failed to add  DiagnosticReagent..");
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					}
				}else{
					redirect = new ActionRedirect(m.findForward("addRedirect"));
					flash.error("Reagent with same test name already exists....");
					redirect.addParameter("reagent_id", reagents[i]);
					redirect.addParameter("test_id", test_id);
					redirect.addParameter("quantity_needed", req.getParameter("quantity_needed"));
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					return redirect;
				}
			} else {

				delItemsCount ++;
			}

			}
			if (reagents.length == delItemsCount) {
				redirect = new ActionRedirect(m.findForward("listRedirect"));
				flash.success("Successfully Deleted");
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

		String testid = req.getParameter("test_id");
		JSONSerializer js = new JSONSerializer().exclude("class");
		DiagnosticReagentMasterDAO dao = new DiagnosticReagentMasterDAO();
		List test_list = new DiagnosticReagentMasterDAO().getTestToMapReagents();
		String test_name = AddTestDAOImpl.getTestName(testid);
		Hashtable<String, String> table = new Hashtable<String, String>();
		table.put("TEST_ID", testid);
		table.put("TEST_NAME",test_name);
		test_list.add(table);
		List bean = dao.findTestBykey(testid);

		req.setAttribute("bean", bean);
		req.setAttribute("reagents", js.serialize(ConversionUtils.copyListDynaBeansToMap(bean)));
		req.setAttribute("test_list", test_list);
		req.setAttribute("testName", test_name);
		req.setAttribute("testsLists", js.serialize(dao.getTestsNamesAndIds()));

		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = null;
		ActionRedirect redirect = null;
		BasicDynaBean bean = null;
		FlashScope flash = null;
		int deleteCount = 0;
		Map params = req.getParameterMap();
		String[] reagents = (String[])params.get("reagent");
		String[] qty = (String[]) params.get("qty");
		String test_id = req.getParameter("test_name");
		String[] hdeleted =(String[])params.get("hdeleted");
		String[] status = (String[])params.get("status");

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			int success = 1;
	        boolean flag = true;

			DiagnosticReagentMasterDAO dao = new DiagnosticReagentMasterDAO();
			bean = dao.getBean();


			Map<String, Object> keys = new HashMap<String, Object>();
			flash = FlashScope.getScope(req);

			for(int i =0 ;i<reagents.length;i++){
				keys.put("reagent_id", new BigDecimal(reagents[i]));
				keys.put("test_id", test_id.toString());

					BasicDynaBean exists = dao.findTestBykey(Integer.parseInt(reagents[i]), test_id);
					if (exists != null) {
						if(hdeleted[i].equalsIgnoreCase("false")) {
							bean.set("reagent_id", Integer.parseInt(reagents[i]));
							bean.set("test_id", test_id);
							bean.set("quantity_needed", new BigDecimal(qty[i]));
							if(status != null)bean.set("status", "I");
							else bean.set("status", "A");
							if (success > 0) success = dao.update(con, bean.getMap(), keys);
						} else {
							if (flag) {
								flag = dao.delete(con, "test_id", test_id, "reagent_id", Integer.parseInt(reagents[i]));
								deleteCount ++;
							}
						}
					}else {
						if(hdeleted[i].equalsIgnoreCase("false")){
							bean.set("reagent_id", Integer.parseInt(reagents[i]));
							bean.set("test_id", test_id);
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
							deleteCount ++;
						}

						}
			}

			if (success > 0) {
				con.commit();
				flash.success("DiagnosticReagent master details updated successfully..");

			} else {
				con.rollback();
				flash.error("Failed to update DiagnosticReagent master details..");
			}

		}finally {
			if (con !=null) con.close();
	    }

		if (reagents.length == deleteCount) {
			redirect = new ActionRedirect(m.findForward("listRedirect"));
			flash.success("Deleted successfully");
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}else{
			redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("test_id", bean.get("test_id"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

			return redirect;
		}
	}

}