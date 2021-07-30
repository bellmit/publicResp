package com.insta.hms.master.Microbiology;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class MicroOrgGroupMasterAction extends DispatchAction{

	GenericDAO dao = new GenericDAO("micro_org_group_master");
	JSONSerializer json = new JSONSerializer().exclude("class");

	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
					HttpServletResponse response)throws Exception {

		Map requestParams = request.getParameterMap();
		PagedList pagedList = dao.search(requestParams, ConversionUtils.getListingParameter(requestParams),
				"org_group_id");
	//	request.setAttribute("shortImpressionNames", json.serialize(dao.getColumnList("org_group_name")));
		request.setAttribute("pagedList", pagedList);

		return mapping.findForward("list");

	}


	public ActionForward add(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws Exception{

		request.setAttribute("orgGrpNamesAndIds", json.serialize(MicroOrgGroupMasterDao.getOrgGrpNamesIds()));
		return mapping.findForward("addShow");
	}

	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response)throws Exception {

		BasicDynaBean bean = dao.findByKey("org_group_id", Integer.parseInt(request.getParameter("org_group_id")));
		request.setAttribute("orgGrpNamesAndIds", json.serialize(MicroOrgGroupMasterDao.getOrgGrpNamesIds()));
		request.setAttribute("bean", bean);

		return mapping.findForward("addShow");
	}


	public ActionForward create (ActionMapping mapping, ActionForm form,
			HttpServletRequest request,HttpServletResponse response) throws Exception {

		Map params = request.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		String error = null;
		boolean success = false;
		try {
			if (errors.isEmpty()) {
				BasicDynaBean exists = dao.findByKey("org_group_name", (String)bean.get("org_group_name"));
				if (exists != null) {
					error = "Organism Group Name  already exists.....";
				} else {
					bean.set("org_group_id", dao.getNextSequence());
					success = dao.insert(con, bean);
					if (!success) {
						error = "Fail to add Organism Group master....";
					}
				}
			} else {
				error = "Incorrectly formatted values supplied..";
			}
		}finally {
			DataBaseUtil.commitClose(con, success);
		}
		ActionRedirect redirect = null;
		FlashScope flash = FlashScope.getScope(request);
		if (error != null) {
			redirect = new ActionRedirect(mapping.findForward("addRedirect"));
			flash.error(error);

		}else {
			redirect = new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter("org_group_id", bean.get("org_group_id"));
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;

	}


	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)throws Exception {

		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map params = request.getParameterMap();
			List errors = new ArrayList();

			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);
			Object key = request.getParameter("org_group_id");

			Map<String, Integer> keys = new HashMap<String, Integer>();
			keys.put("org_group_id", Integer.parseInt(key.toString()));
			FlashScope flash = FlashScope.getScope(request);

			if (errors.isEmpty()) {
				int success = dao.update(con, bean.getMap(), keys);
				if (success > 0) {
					con.commit();
					flash.success("Organism Group master details updated successfully..");
				} else {
					con.rollback();
					flash.error("Failed to update Organism Group master details..");
				}
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
			ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter("org_group_id", key.toString());
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

}