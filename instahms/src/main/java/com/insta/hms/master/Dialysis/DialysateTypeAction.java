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

public class DialysateTypeAction extends DispatchAction {

	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response)throws IOException, ServletException, Exception {

		Map map = request.getParameterMap();

		DialysateTypeDAO dao = new DialysateTypeDAO();
		PagedList pagedList = dao.search(map,ConversionUtils.getListingParameter(request.getParameterMap()),
					"dialysate_type_id");
		request.setAttribute("pagedList", pagedList);

		return mapping.findForward("list");
	}

	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response)throws IOException, ServletException, Exception {

		DialysateTypeDAO dao = new DialysateTypeDAO();
		FlashScope flash = FlashScope.getScope(request);
		Connection con = DataBaseUtil.getConnection();
		BasicDynaBean bean = dao.getBean();

		ActionRedirect redirect = new ActionRedirect(mapping.findForwardConfig("addRedirect"));
		List errors = new ArrayList();
		Map map = request.getParameterMap();
		con.setAutoCommit(false);

		ConversionUtils.copyToDynaBean(map, bean, errors);
		if(errors.isEmpty()) {
			BasicDynaBean exists = dao.findByKey("dialysate_type_name", request.getParameter("dialysate_type_name"));
			if(exists == null) {
				bean.set("dialysate_type_id", dao.getNextSequence());
				boolean success = dao.insert(con, bean);
				if(success) {

					con.commit();
					flash.success("Details Inserted successfully");
					redirect = new ActionRedirect(mapping.findForwardConfig("showRedirect"));
					redirect.addParameter("dialysate_type_id",bean.get("dialysate_type_id"));
				}else {

					flash.error("Failed to insert Dialysate Type");
				}
			}else {

				flash.error("Dialysate Type already exists");
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
		DialysateTypeDAO dao = new DialysateTypeDAO();
		BasicDynaBean bean = dao.getBean();
		List errors = new ArrayList();

		Object key = request.getParameter("dialysate_type_id");
		Map<String, Integer> keys = new HashMap<String, Integer>();
		keys.put("dialysate_type_id", Integer.parseInt(key.toString()));

		ConversionUtils.copyToDynaBean(map, bean, errors);
		if(errors.isEmpty()) {
			BasicDynaBean exists = dao.findByKey("dialysate_type_name", bean.get("dialysate_type_name"));
		if(exists != null && !key.equals(exists.get("dialysate_type_id").toString())) {
				flash.error("Dialysate name already exists");
			}
			else {
				int success = dao.update(con, bean.getMap(), keys);
				if(success == 1) {
					con.commit();
					flash.success("Details Updated successfully");
				}
				else {
					con.rollback();
					flash.error("Failed to update Dialysate Type details..");
				}
			}
		}
		else {
			flash.error("Incorrectly formatted details supplied");
		}

		con.close();
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("dialysate_type_id",request.getParameter("dialysate_type_id"));
		return redirect;
	}

	public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)throws IOException, ServletException, Exception {


		return mapping.findForward("addshow");
	}

	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response)throws IOException, ServletException, Exception {

		DialysateTypeDAO dao = new DialysateTypeDAO();
		BasicDynaBean bean = dao.findByKey("dialysate_type_id",
				Integer.parseInt(request.getParameter("dialysate_type_id")));
		request.setAttribute("bean", bean);

		return mapping.findForward("addshow");
	}


}


