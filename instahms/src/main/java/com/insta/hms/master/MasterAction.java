package com.insta.hms.master;

import com.insta.hms.common.AbstractMasterAction;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public abstract class MasterAction extends AbstractMasterAction {

	public ActionForward find(ActionMapping mapping, ActionForm form,
			HttpServletRequest req, HttpServletResponse resp) throws Exception {
		return find(mapping, req);
	}

	public ActionForward list(ActionMapping mapping, ActionForm form,
			HttpServletRequest req, HttpServletResponse resp)
			throws Exception {
		return list(mapping, req);
	}
	
	public ActionForward add(ActionMapping mapping,ActionForm form,
			HttpServletRequest req,HttpServletResponse resp) throws Exception{
		return addShow(mapping, req);
	}

	public ActionForward show(ActionMapping mapping,ActionForm form,
			HttpServletRequest req,HttpServletResponse resp) throws Exception{
		return addShow(mapping, req);
	}

	public ActionForward create(ActionMapping mapping, ActionForm form,
			HttpServletRequest req, HttpServletResponse resp)
			throws Exception {
		return doSave(mapping, req);
	}

	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest req, HttpServletResponse resp)
			throws Exception {
		return doSave(mapping, req);
	}

}
