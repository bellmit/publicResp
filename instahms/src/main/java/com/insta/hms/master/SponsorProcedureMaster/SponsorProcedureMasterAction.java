/**
 *
 */
package com.insta.hms.master.SponsorProcedureMaster;

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
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author lakshmi.p
 *
 */
public class SponsorProcedureMasterAction extends DispatchAction {

	static Logger logger = LoggerFactory.getLogger(SponsorProcedureMasterAction.class);

	public ActionForward list(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		SponsorProcedureMasterDAO dao = new SponsorProcedureMasterDAO();

		Map filter= request.getParameterMap();
		PagedList pagedList = dao.getSponsorProcedureList(filter,ConversionUtils.getListingParameter(request.getParameterMap()));

		request.setAttribute("pagedList", pagedList);
		List procedureNameList = SponsorProcedureMasterDAO.getAllProcedureNames();
		request.setAttribute("procedureNameList", new JSONSerializer().serialize(procedureNameList));

		return m.findForward("list");
	}


	public ActionForward add(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response){

		return m.findForward("addshow");
	}


	public ActionForward create (ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		Map params = request.getParameterMap();
		List errors = new ArrayList();
		Connection con = null;

		SponsorProcedureMasterDAO dao = new SponsorProcedureMasterDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(m.findForward("addRedirect"));

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			if(errors.isEmpty()){
				BasicDynaBean exists = dao.getExistingBean((String)bean.get("procedure_code"), (String)bean.get("tpa_id"));
				if (exists == null){
					boolean sucess = dao.insert(con, bean);
					if (sucess){
						con.commit();
						flash.success("Sponsor procedure details inserted successfully.....");
						redirect = new ActionRedirect(m.findForward("showRedirect"));
						redirect.addParameter("procedure_code",request.getParameter("procedure_code"));
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						return redirect;
					}else{
						con.rollback();
						flash.error("Fail to add sponsor procedure....");
					}
				}else{
					flash.error("Sponsor procedure already exists....");
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					return redirect;
				}
			}else{
				flash.error("Incorrectly formatted values supplied....");
			}
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		}finally {
			DataBaseUtil.closeConnections(con, null);
		}
		return redirect;
	}


	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		BasicDynaBean bean =null;

		SponsorProcedureMasterDAO dao = new SponsorProcedureMasterDAO();
		if(req.getParameter("procedure_code")!=null)
		{
			bean = dao.findByKey("procedure_code", req.getParameter("procedure_code"));
		}
		else
		{
			bean = dao.findByKey("procedure_no", new Integer(req.getParameter("procedure_no")));
		}
		req.setAttribute("bean", bean);
		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = null;
		Map params = req.getParameterMap();
		List errors = new ArrayList();

		SponsorProcedureMasterDAO dao = new SponsorProcedureMasterDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		Integer key = new Integer(req.getParameter("procedure_no"));

		Map<String, Integer> keys = new HashMap<String, Integer>();
		keys.put("procedure_no", key);
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			if (errors.isEmpty()) {
				BasicDynaBean exists = dao.getExistingBean((String)bean.get("procedure_code"), (String)bean.get("tpa_id"));
				if (exists != null) {
					if(((Integer)exists.get("procedure_no")).equals(key)){
						int success = dao.update(con, bean.getMap(), keys);

						if (success > 0) {
							con.commit();
							flash.success("Sponsor procedure details updated successfully..");
							redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
							redirect.addParameter("procedure_no", req.getParameter("procedure_no"));
							return redirect;
						} else {
							con.rollback();
							flash.error("Failed to update sponsor procedure details..");
						}
					}else {
						flash.error("Sponsor procedure already exists..");
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					}
				}
			}
			else {
				flash.error("Incorrectly formatted values supplied");
			}
			redirect.addParameter("procedure_no", req.getParameter("procedure_no"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		}finally {
			DataBaseUtil.closeConnections(con, null);
		}
		return redirect;
	}
}
