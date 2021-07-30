package com.insta.hms.emr;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EmrJustificationComments extends DispatchAction{

	public ActionForward saveComments(ActionMapping mapping, ActionForm form, HttpServletRequest request,
    		HttpServletResponse response)throws SQLException, IOException {
    	
		List errors = new ArrayList();
		Map parameterMap = request.getParameterMap();
		GenericDAO dao = new GenericDAO("emr_justification_comments");
		String mr_no = request.getParameter("mr_no");
		String visit_id = request.getParameter("visit_id");
		Connection con = null;
		try {
			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(parameterMap, bean, errors);
			if ((mr_no == null || mr_no.equals("")) 
					&& (visit_id != null && !visit_id.equals(""))) {
				bean.set("mr_no", VisitDetailsDAO.getMrno(visit_id));
			}
			bean.set("emp_username", request.getSession(false).getAttribute("userid"));
	    	con = DataBaseUtil.getConnection();
	    	boolean b = dao.insert(con, bean);
	    	response.setContentType("text/plain");
	        response.setHeader("Cache-Control", "no-cache");
	        JSONSerializer js = new JSONSerializer().exclude("class");
	        js.serialize(b, response.getWriter());
	        
	        response.flushBuffer();
	    	return null;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
    }
	
	public ActionForward getEmrAccess(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)throws SQLException, IOException {
		
		String mr_no = request.getParameter("mr_no");
		String visit_id = request.getParameter("visit_id");
		BasicDynaBean patientBean = null;
		if (visit_id != null && !visit_id.equals(""))
			patientBean  = new VisitDetailsDAO().getVisitWithEmrAccess(visit_id);
		if (mr_no != null && !mr_no.equals(""))
			patientBean  = new VisitDetailsDAO().getPatientWithEmrAccess(mr_no);
		Boolean mandate_emr_access = false;
		if (patientBean != null)
			mandate_emr_access = (boolean) patientBean.get("mandate_emr_comments");
		response.setContentType("text/plain");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		response.getWriter().write(mandate_emr_access.toString());
		return null;
	}
}
