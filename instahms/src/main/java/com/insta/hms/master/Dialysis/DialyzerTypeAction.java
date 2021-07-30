package com.insta.hms.master.Dialysis;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;

import org.apache.commons.beanutils.BasicDynaBean;
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



public class DialyzerTypeAction extends DispatchAction {


	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)throws IOException, ServletException, Exception {

		DialyzerTypeDAO dao = new DialyzerTypeDAO();
		Map map= request.getParameterMap();

		PagedList pagedList = dao.search(map,ConversionUtils.getListingParameter(request.getParameterMap()),
					"dialyzer_type_id");

		request.setAttribute("pagedList", pagedList);

		return mapping.findForward("list");
	}


	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response)throws IOException, ServletException, Exception {

		Map params = request.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		DialyzerTypeDAO dao = new DialyzerTypeDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
		FlashScope flash = FlashScope.getScope(request);

		if(errors.isEmpty()) {
			BasicDynaBean exists = dao.findByKey("dialyzer_type_name", bean.get("dialyzer_type_name"));
			if(exists == null) {
				bean.set("dialyzer_type_id", dao.getNextSequence());
				boolean success = dao.insert(con, bean);
				if(success) {
					con.commit();
					redirect = new ActionRedirect(mapping.findForward("showRedirect"));
					redirect.addParameter("dialyzer_type_id", bean.get("dialyzer_type_id"));
					flash.success("Dialyzer Type Inserted Successfully");

				}else {
					flash.error("Faild to add Dialyzer Type...");

				}
			}else {
				flash.error("Dialyzer Type already exists");

			}
		}else {
			flash.error("Incorrectly formatted values supplied");

		}
		con.close();
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}


	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)throws IOException, ServletException, Exception {

		Map params = request.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		DialyzerTypeDAO dao = new DialyzerTypeDAO();
		BasicDynaBean bean = dao.getBean();
		ActionRedirect redirect = new ActionRedirect(mapping.findForwardConfig("showRedirect"));
		ConversionUtils.copyToDynaBean(params, bean, errors);

		Object key = request.getParameter("dialyzer_type_id");
		Map<String, Integer> keys = new HashMap<String, Integer>();
		keys.put("dialyzer_type_id", Integer.parseInt(key.toString()));
		FlashScope flash = FlashScope.getScope(request);

		if(errors.isEmpty()) {
			BasicDynaBean exists = dao.findByKey("dialyzer_type_name", bean.get("dialyzer_type_name"));
			if(exists != null && !key.equals(exists.get("dialyzer_type_id").toString())) {
				flash.error("Dialyzer Type already exists");

			}
			else {
				int success = dao.update(con, bean.getMap(), keys);
				if(success > 0) {
					con.commit();
					flash.success("Details updated successfully");
				}
				else {
					con.rollback();
					flash.error("Failed to Update Dialyzer Type details");
				}
			}
		}
		else {
			flash.error("Incorrectly formatted values supplied");

		}

		con.close();
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("dialyzer_type_id", request.getParameter("dialyzer_type_id"));
		return redirect;
	}

	public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response)throws IOException, ServletException, Exception {


		return mapping.findForward("addshow");
	}

	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response)throws IOException, ServletException, Exception {

		DialyzerTypeDAO dao = new DialyzerTypeDAO();
		BasicDynaBean bean = dao.findByKey("dialyzer_type_id", Integer.parseInt(request.getParameter("dialyzer_type_id")));
		request.setAttribute("bean", bean);

		return mapping.findForward("addshow");
	}


}