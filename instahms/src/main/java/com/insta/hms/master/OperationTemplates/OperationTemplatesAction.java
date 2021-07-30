/**
 *
 */
package com.insta.hms.master.OperationTemplates;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.otmasters.opemaster.OperationMasterDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.GenericDocumentTemplate.GenericDocumentTemplateDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna
 *
 */
public class OperationTemplatesAction extends DispatchAction {

	OperationTemplatesDAO dao = new OperationTemplatesDAO();
	JSONSerializer js = new JSONSerializer();

	public ActionForward list(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws ServletException, IOException, SQLException, ParseException {
		req.setAttribute("pagedList", dao.search(req.getParameterMap()));
		req.setAttribute("templatesList", GenericDocumentTemplateDAO.getTemplates(true, "SYS_OT", "A"));
		req.setAttribute("operations", js.serialize(ConversionUtils.copyListDynaBeansToMap(OperationMasterDAO.getOperationsList())));
		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws ServletException, IOException, SQLException {
		req.setAttribute("templatesList", GenericDocumentTemplateDAO.getTemplates(true, "SYS_OT", "A"));
		req.setAttribute("operations", js.serialize(ConversionUtils.copyListDynaBeansToMap(OperationMasterDAO.getOperationsList())));
		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, IOException, ServletException,
		   FileUploadException {

		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		BasicDynaBean bean  = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = null;
		try {
			if (errors.isEmpty()){

				Map map = new HashMap();
				String[] templateIdAndFormat = req.getParameter("ot_template").split(",");
				String op_id = (String) bean.get("op_id");
				if (op_id.equals("")) {
					BasicDynaBean opBean = new GenericDAO("operation_master").findByKey("operation_name", req.getParameter("operation_name"));
					bean.set("op_id", opBean.get("op_id"));
				}
				bean.set("template_id", Integer.parseInt(templateIdAndFormat[0]));
				bean.set("format", templateIdAndFormat[1]);

				map.put("op_id", bean.get("op_id"));
				map.put("template_id", Integer.parseInt(templateIdAndFormat[0]));
				map.put("format", templateIdAndFormat[1]);
				BasicDynaBean exists = null;
				List<BasicDynaBean> beanList = dao.listAll(null, map, null);
				exists = beanList.size() > 0 ? beanList.get(0) : null;

				if (exists == null){
					boolean success = dao.insert(con, bean);
					if (success){
						con.commit();
						redirect = new ActionRedirect(m.findForward("showRedirect"));
						redirect.addParameter("op_id", bean.get("op_id"));
						redirect.addParameter("template_id", bean.get("template_id"));
						redirect.addParameter("format", bean.get("format"));
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						return redirect;
					}else{
						con.rollback();
						flash.error("Failed to add Template to the Operation...");
					}
				} else {
					flash.error("Association already exists");
				}
			}else{
				flash.error("Incorrectly formatted values supplied");
			}
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}

		redirect = new ActionRedirect(m.findForward("addRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}

	public ActionForward show(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, IOException, ServletException {

		String opId = req.getParameter("op_id");
		int templateId = Integer.parseInt(req.getParameter("template_id"));
		String format = req.getParameter("format");

		req.setAttribute("bean", dao.getRecord(opId, templateId, format));
		req.setAttribute("templatesList", GenericDocumentTemplateDAO.getTemplates(true, "SYS_OT", "A"));
		req.setAttribute("operations", js.serialize(ConversionUtils.copyListDynaBeansToMap(OperationMasterDAO.getOperationsList())));
		return m.findForward("addshow");

	}

	public ActionForward delete(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws SQLException {
		ActionRedirect redirect = null;
		FlashScope flash = FlashScope.getScope(req);
		Map params = req.getParameterMap();
		List errorFields = null;
		String[] deleted = (String[]) params.get("_deleted");
		Connection con = null;
		boolean success = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			for (int i=0; i<deleted.length; i++) {
				if (deleted[i].equals("Y")) {
					BasicDynaBean bean = dao.getBean();
					ConversionUtils.copyIndexToDynaBeanPrefixed(params, i, bean, errorFields, "_");
					LinkedHashMap<String, Object> identifiers = new LinkedHashMap<String, Object>();
					identifiers.put("op_id", bean.get("op_id"));
					identifiers.put("template_id", bean.get("template_id"));
					identifiers.put("format", bean.get("format"));
					success = dao.delete(con, identifiers);
					if (!success) {
						flash.error("Failed to delete");
						break;
					}
				}
			}
			if (success)
				flash.info("Deleted successfully");
		}finally{
			DataBaseUtil.commitClose(con, success);
		}

		redirect = new ActionRedirect(m.findForward("listRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}
}
