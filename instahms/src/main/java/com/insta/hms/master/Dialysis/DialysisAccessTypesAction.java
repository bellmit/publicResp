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

@Deprecated
public class DialysisAccessTypesAction extends DispatchAction {

	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response)throws IOException, ServletException, Exception {

		DialAccessTypesDAO dao = new DialAccessTypesDAO();
		Map map = request.getParameterMap();

		PagedList pagedList = dao.search(map, ConversionUtils.getListingParameter(request.getParameterMap()),
					"access_type_id");
		request.setAttribute("pagedList", pagedList);

		return mapping.findForward("list");
	}

	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response)throws IOException, ServletException, Exception {

		Map params = request.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		ActionRedirect redirect = new ActionRedirect(mapping.findForwardConfig("addRedirect"));
		FlashScope flash = FlashScope.getScope(request);
		DialAccessTypesDAO dao = new DialAccessTypesDAO();
		BasicDynaBean bean = dao.getBean();

		ConversionUtils.copyToDynaBean(params, bean, errors);
		if(errors.isEmpty()) {
			BasicDynaBean exists = dao.findByKey("access_type", request.getParameter("access_type"));
			if(exists == null) {
				bean.set("access_type_id", dao.getNextSequence());
				boolean success = dao.insert(con, bean);
				if(success) {
					con.commit();
					flash.success("Dialysis Access Type Inserted successfully");
					redirect = new ActionRedirect(mapping.findForwardConfig("showRedirect"));
					redirect.addParameter("access_type_id", bean.get("access_type_id"));

				}else {
					flash.error("Failed to add Dialysis Access Type..");

				}
			}else {
				flash.error("Dialysis Access Type already exists..");

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

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		FlashScope flash = FlashScope.getScope(request);
		Map map = request.getParameterMap();

		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		DialAccessTypesDAO dao = new DialAccessTypesDAO();
		BasicDynaBean bean = dao.getBean();
		List errors = new ArrayList();

		Object key = request.getParameter("access_type_id");
		Map<String, Integer> keys = new HashMap<String, Integer>();
		keys.put("access_type_id", Integer.parseInt(key.toString()));

		ConversionUtils.copyToDynaBean(map, bean, errors);
		if(errors.isEmpty()) {
			BasicDynaBean exists = dao.findByKey("access_type", bean.get("access_type"));
		if(exists != null && !key.equals(exists.get("access_type_id").toString())) {
				flash.error("Dialysis Access Type already exists");

			}
			else {
				int success = dao.update(con, bean.getMap(), keys);
				if(success == 1) {
					con.commit();
					flash.success("Details Updated successfully");
				}
				else {
					con.rollback();
					flash.error("Failed to update Dialysis Access Type details..");
				}
			}
		}
		else {
			flash.error("Incorrectly formatted details supplied");

		}

		con.close();
		redirect.addParameter("access_type_id", request.getParameter("access_type_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response)throws IOException, ServletException, Exception {


		return mapping.findForward("addshow");
	}

	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response)throws IOException, ServletException, Exception {

		DialAccessTypesDAO dao = new DialAccessTypesDAO();
		BasicDynaBean bean = dao.findByKey("access_type_id",
				Integer.parseInt(request.getParameter("access_type_id")));
		request.setAttribute("bean", bean);

		return mapping.findForward("addshow");
	}

}