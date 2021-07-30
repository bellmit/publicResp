package com.insta.hms.master.EMRAccessRight;

import com.insta.hms.emr.EMRDoc;
import com.insta.hms.emraccess.EMRAccessRuleProcessor;

import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class EMRAccessRightAction extends DispatchAction{
	static Logger log = LoggerFactory.getLogger(EMRAccessRightAction.class);
	public ActionForward list(ActionMapping actionmapping, ActionForm actionform, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException, Exception {

		EMRAccessRightService service = new EMRAccessRightService();
		service.list(actionmapping,actionform,req,res);
		return actionmapping.findForward("list");
	}

	public ActionForward add(ActionMapping actionmapping, ActionForm actionform, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException, Exception {

//		EMRAccessRightService service = new EMRAccessRightService();
//		ActionRedirect redirect = service.editMode(actionmapping, actionform, req, res);
		ActionRedirect redirect = null;
		EMRAccessRightService service = new EMRAccessRightService();
		if (null != req.getParameter("doc_type_id") && "SYS_CONSULT".equals(req.getParameter("doc_type_id"))) {
			redirect = service.findConsultationRules(actionmapping, actionform, req, res);
		} else {
			redirect = service.findRules(actionmapping, actionform, req, res);
		}

		if(redirect==null){
			//service.add(actionmapping,actionform,req,res);
			service.show(actionmapping,actionform,req,res);
			return actionmapping.findForward("addshow");
		}else{
				return redirect;
		}
	}

	public ActionForward show(ActionMapping actionmapping, ActionForm actionform, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException, Exception {

		EMRAccessRightService service = new EMRAccessRightService();
		service.show(actionmapping,actionform,req,res);
		return actionmapping.findForward("addshow");
	}

	public ActionForward create(ActionMapping actionmapping, ActionForm actionform,	HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException, Exception {

		ActionRedirect redirect = new ActionRedirect(actionmapping.findForward("addRedirect"));
		String strProcess = "Save";
		redirect = EMRAccessRightService.create(actionmapping,actionform,req,res,redirect,strProcess);
		return redirect;
	}

	public ActionForward update(ActionMapping actionmapping, ActionForm actionform,	HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException, Exception {

		ActionRedirect redirect = new ActionRedirect(actionmapping.findForward("showRedirect"));
		String strProcess = "Update";
		EMRAccessRightService service = new EMRAccessRightService();
		redirect = service.update(actionmapping,actionform,req,res,redirect,strProcess);
		return redirect;
	}

	public ActionForward getRuleDetailsBasedonDocSubType(ActionMapping actionmapping, ActionForm actionform,	HttpServletRequest req, HttpServletResponse res) throws Exception {

	    String doctypeId = req.getParameter("doc_type_id");
	    String docSubType = req.getParameter("doc_sub_type");
	    String ruleType = req.getParameter("rule_type");
	    EMRAccessRightService service = new EMRAccessRightService();
	    String ruleId = service.getRuleDetailsBasedonDocSubType(doctypeId,docSubType,ruleType,req,res);

	    res.setContentType("text/plain");
	    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
	    if(null!=ruleId){
	    res.getWriter().write(ruleId);
	    }else{
	    	res.getWriter().write("");
	    }
	    res.flushBuffer();

	    return null;
	}

	//This Method is no more in use, used only for testing purpose...
	public ActionForward emrAccessCheck(ActionMapping actionmapping, ActionForm actionform, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException, Exception {

		HttpSession session = req.getSession();
		String userId = (String) session.getAttribute("userid");
		int centerId = (Integer)session.getAttribute("centerId");
		int roleId = (Integer)session.getAttribute("roleId");
		EMRDoc doc = new EMRDoc();
		EMRAccessRuleProcessor accessRuleProcessor = new EMRAccessRuleProcessor();
		boolean checkCenterAccess =true;
		//checkCenterAccess = accessRuleProcessor.checkDocCentersAccess(doc, userId,centerId,roleId);
		log.info("emrAccessCheck:checkCenterAccess::"+checkCenterAccess);
		if(checkCenterAccess){
			req.setAttribute("result", "Success..");
			return actionmapping.findForward("displayResult");
		}else{
			req.setAttribute("result", "Failure..");
			return actionmapping.findForward("displayResult");
		}
	}

}
