package com.insta.hms.master.ClinicalData;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author mithun.saha
 *
 */

public class ClinicalDataLabResultsMasterAction extends DispatchAction{
	static Logger logger = LoggerFactory.getLogger(ClinicalDataLabResultsMasterAction.class);
	JSONSerializer json = new JSONSerializer().exclude("class");
	ClinicalDataLabResultsMasterDAO dao = new ClinicalDataLabResultsMasterDAO();
	
    private static final GenericDAO clinicalLabResultsDAO = new GenericDAO("clinical_lab_result");

	public ActionForward list(ActionMapping mapping, ActionForm form,
			HttpServletRequest request,HttpServletResponse response)
	throws SQLException, Exception {
		PagedList pl = null;
		Map requestParams = request.getParameterMap();
		pl = new ClinicalDataLabResultsMasterDAO().getClinicalLabDetails(requestParams, ConversionUtils.getListingParameter(request.getParameterMap()));
		request.setAttribute("pagedList", pl);
		request.setAttribute("resultNamesAndIds", json.serialize(ConversionUtils.copyListDynaBeansToMap(dao.getClinicalAndTestDetails())));
		return mapping.findForward("list");
	}

    public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
        HttpServletResponse response) throws SQLException, Exception {
      request.setAttribute("resultlabelIds",
          json.serialize(ConversionUtils.copyListDynaBeansToMap(clinicalLabResultsDAO.listAll())));
      request.setAttribute("resultNamesAndIds",
          json.serialize(ConversionUtils.copyListDynaBeansToMap(dao.getClinicalAndTestDetails())));
      request.setAttribute("testResultsAutoComplete",
          json.serialize(ConversionUtils.copyListDynaBeansToMap(dao.getTestResultDetails())));
      return mapping.findForward("addshow");
    }

    public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
        HttpServletResponse response) throws SQLException, Exception {
      BasicDynaBean bean = null;
      String reultLabelId = request.getParameter("resultlabel_id");
      if (reultLabelId != null && !reultLabelId.equals("")) {
        bean =
            new ClinicalDataLabResultsMasterDAO().getClinicalBean(Integer.parseInt(reultLabelId));
      }
      request.setAttribute("bean", bean);

      request.setAttribute("resultNamesAndIds",
          json.serialize(ConversionUtils.copyListDynaBeansToMap(dao.getClinicalAndTestDetails())));
      request.setAttribute("resultlabelIds", json.serialize(
          ConversionUtils.copyListDynaBeansToMap(clinicalLabResultsDAO.listAll())));
      request.setAttribute("testResultsAutoComplete",
          json.serialize(ConversionUtils.copyListDynaBeansToMap(dao.getTestResultDetails())));
      return mapping.findForward("addshow");
    }

	public ActionForward create(ActionMapping mapping, ActionForm form,
			HttpServletRequest request,HttpServletResponse response)
	throws SQLException, Exception {
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
				boolean exists = dao.exist("resultlabel_id", (Integer)bean.get("resultlabel_id"));
				if (exists) {
					error = "Result name already exists.....";
				} else {
					success = dao.insert(con, bean);
					if (!success) {
						error = "Fail to add Clinical Data Lab Result Master....";
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
			redirect.addParameter("resultlabel_id", bean.get("resultlabel_id"));
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map params = req.getParameterMap();
			List errors = new ArrayList();

			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);

			String key = req.getParameter("resultlabel_id_update");

			Map<String, Integer> keys = new HashMap<String, Integer>();
			keys.put("resultlabel_id",  Integer.parseInt(key));
			FlashScope flash = FlashScope.getScope(req);

			if (errors.isEmpty()) {
				int success = dao.update(con, bean.getMap(), keys);
				if (success > 0) {
					con.commit();
					flash.success("Clinical Data Lab Result master details updated successfully..");
				} else {
					con.rollback();
					flash.error("Failed to update Clinical Data Lab Result..");
				}
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
			ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("resultlabel_id", bean.get("resultlabel_id"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public ActionForward deleteCheckedRecords(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		String[] recordsToDelete = req.getParameterValues("_deleteResult");
		Connection con = null;
		boolean success = false;
		try {
			con = DataBaseUtil.getConnection();
			if (recordsToDelete!= null) {
					for (int i=0;i<recordsToDelete.length;i++) {
						if (!recordsToDelete[i].equals("")) {
							success = dao.delete(con, "resultlabel_id", Integer.parseInt(recordsToDelete[i]));
						}
					}
			}
		} finally {
			DataBaseUtil.commitClose(con, success);
		}
		ActionRedirect redirect = new ActionRedirect(m.findForward("listRedirect"));
		return redirect;
	}
}
