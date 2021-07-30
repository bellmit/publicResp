package com.insta.hms.master.GenericSchedulerMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GenericResourceTypeAction extends DispatchAction{
	static Logger logger = LoggerFactory.getLogger(GenericResourceTypeAction.class);
	JSONSerializer js = new JSONSerializer().exclude("class");
	GenericResourceTypeDAO dao = new GenericResourceTypeDAO();

	public ActionForward list(ActionMapping map,ActionForm fm,
			HttpServletRequest req,HttpServletResponse res)
				throws ServletException,Exception {

		Map params = req.getParameterMap();
		PagedList pagedlist =
		dao.getGenericResourceTypes(
			params,ConversionUtils.getListingParameter(
					req.getParameterMap()));

		req.setAttribute("pagedlist", pagedlist);

		return map.findForward("list");
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest request, HttpServletResponse responce)
	throws IOException, ServletException, Exception {

		BasicDynaBean bean = dao.getRecord(Integer.parseInt(request.getParameter("generic_resource_type_id")));
		request.setAttribute("bean", bean);
		ArrayList<String>  resourceNames = (ArrayList<String>)dao.getAllResourceNames();
		request.setAttribute("resourceNames", js.serialize(resourceNames));
		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest request, HttpServletResponse responce)
	throws IOException, ServletException, Exception {

		Connection con = null;
		Map params = request.getParameterMap();
		List errors = new ArrayList();
		boolean success = false;

		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		Object key = request.getParameter("generic_resource_type_id");

		Map<String, Integer> keys = new HashMap<String, Integer>();
		keys.put("generic_resource_type_id", Integer.parseInt(key.toString()));
		FlashScope flash = FlashScope.getScope(request);

		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);//begin transaction

			if (errors.isEmpty()) {
				success = dao.update(con, bean.getMap(), keys) > 0;

				if (!success)
					flash.error("Failed to update Generic Resources Types details..");
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter("generic_resource_type_id", Integer.parseInt(key.toString()));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}
}
