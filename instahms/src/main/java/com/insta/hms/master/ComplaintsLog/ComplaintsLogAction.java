package com.insta.hms.master.ComplaintsLog;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
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
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ComplaintsLogAction extends DispatchAction{

	static Logger logger = LoggerFactory.getLogger(ComplaintsLogAction.class);
	public static String[] SEARCH_BOOL_FIELDS = {"statusAll", "statusOpen","statusClarify","statusPending", "statusFixed", "statusWontFix","statusProdEnh"};

	public static enum COMP_STATUS { OPEN, CLARIFY, PENDING, FIXED, WONTFIX, PRODENH }

	public ActionForward list(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		ComplaintsLogDAO dao = new ComplaintsLogDAO();

		Map map = request.getParameterMap();

		PagedList pagedList = dao.search(map,ConversionUtils.getListingParameter(request.getParameterMap()),
				"complaint_id");

		request.setAttribute("pagedList", pagedList);

		return m.findForward("list");
	}


	public ActionForward add(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response){

		JSONSerializer json = new JSONSerializer().exclude("class");
		ArrayList complaintsLog = (ArrayList)ComplaintsLogDAO.getAllComplaintMasters();

		request.setAttribute("complaintsLog", json.serialize(complaintsLog));

		return m.findForward("addshow");
	}


	public ActionForward create (ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		Map params = request.getParameterMap();
		List errors = new ArrayList();
		Connection con = null;
		ActionRedirect redirect = new ActionRedirect(m.findForwardConfig("addRedirect"));
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

	 		HttpSession session = request.getSession(false);
                	String userid = (String)session.getAttribute("userid");
			ComplaintsLogDAO dao = new ComplaintsLogDAO();
			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);
			String comstatus = ((Object[])params.get("complaint_status"))[0].toString();

            java.sql.Timestamp date= new Timestamp((new java.util.Date()).getTime()) ;

			bean.set("logged_by", userid );
			bean.set("logged_date", date);
			bean.set("updated_by", userid );
			bean.set("updated_date", date);

			if (COMP_STATUS.OPEN.equals(comstatus)) {
				bean.set("complaint_status", "Open");
			}else if(COMP_STATUS.CLARIFY.equals(comstatus))  {
				bean.set("complaint_status", "Clarify");
			}else if(COMP_STATUS.PENDING.equals(comstatus))  {
				bean.set("complaint_status", "Pending");
			}else if(COMP_STATUS.FIXED.equals(comstatus))  {
				bean.set("complaint_status", "Fixed");
			}else if(COMP_STATUS.WONTFIX.equals(comstatus))  {
				bean.set("complaint_status", "Not In Scope");
			}else if(COMP_STATUS.PRODENH.equals(comstatus))  {
				bean.set("complaint_status", "Prod Enh");
			}

			if(errors.isEmpty()){
				bean.set("complaint_id", new BigDecimal(dao.getNextComplaintId()));
				boolean sucess = dao.insert(con, bean);
				if (sucess){
					con.commit();
					FlashScope fScope = FlashScope.getScope(request);
					fScope.success("Complaints Log details inserted successfully.");
					redirect = new ActionRedirect(m.findForwardConfig("showRedirect"));
					redirect.addParameter("complaint_id",bean.get("complaint_id"));
					redirect.addParameter(FlashScope.FLASH_KEY, fScope.key());
					return redirect;
				}else{
					con.rollback();
					request.setAttribute("error", "Fail to add complaints log details.");
				}

			}else{
				request.setAttribute("error", "Incorrectly formatted values supplied.");
			}
		}finally {
			if (con != null) con.close();
		}
		return redirect;
	}


	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		ComplaintsLogDAO dao = new ComplaintsLogDAO();
		BasicDynaBean bean = dao.findByKey("complaint_id", Integer.parseInt(req.getParameter("complaint_id")));
		req.setAttribute("bean", bean);

		ArrayList  complaintDetails =
			(ArrayList)ComplaintsLogDAO.getComplaintMasters(new BigDecimal(Integer.parseInt(req.getParameter("complaint_id"))));

		req.setAttribute("complaintDetails", complaintDetails);
		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = null;
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map params = req.getParameterMap();
			List errors = new ArrayList();

	 		HttpSession session = req.getSession(false);
                	String userid = (String)session.getAttribute("userid");
			ComplaintsLogDAO dao = new ComplaintsLogDAO();
			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);

			Object key = req.getParameter("complaint_id");

			Map<String, Integer> keys = new HashMap<String, Integer>();
			keys.put("complaint_id", Integer.parseInt(key.toString()));

			java.sql.Timestamp date= new Timestamp((new java.util.Date()).getTime()) ;
			bean.set("updated_by", userid );
			bean.set("updated_date", date);

			if (errors.isEmpty()) {

					int success = dao.update(con, bean.getMap(), keys);

					if (success > 0) {
						con.commit();
						FlashScope flash = FlashScope.getScope(req);
						flash.success("Complaints Log details updated successfully.");
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						redirect.addParameter("complaint_id", req.getParameter("complaint_id"));
						return redirect;
					} else {
						con.rollback();
						req.setAttribute("error", "Failed to update Complaints Log.");
					}

			}
			else {
				req.setAttribute("error", "Incorrectly formatted values supplied.");
			}

		}finally {
			if (con != null) con.close();
		}
		redirect.addParameter("complaint_id", req.getParameter("complaint_id"));
		return redirect;
	}
}


