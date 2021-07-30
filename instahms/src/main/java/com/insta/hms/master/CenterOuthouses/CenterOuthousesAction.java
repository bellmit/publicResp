/**
 *
 */
package com.insta.hms.master.CenterOuthouses;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.diagnosticsmasters.outhousemaster.OutHouseMasterDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.modules.ModulesDAO;

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
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna
 *
 */
public class CenterOuthousesAction extends DispatchAction {

	CenterOuthousesDAO dao = new CenterOuthousesDAO();
	CenterMasterDAO centerDao = new CenterMasterDAO();
	public ActionForward list(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws ServletException, IOException, SQLException, ParseException {
		req.setAttribute("pagedList", dao.search(req.getParameterMap()));
		req.setAttribute("outsourcedetail", OutHouseMasterDAO.getAllActiveOutSourceName());
		req.setAttribute("centers", centerDao.getAllCentersExceptSuper());
		boolean isInternalLabExists = dao.isIntLabExists();
		req.setAttribute("centralLabModule", new ModulesDAO().findByKey("module_id", "mod_central_lab"));
		req.setAttribute("isInternalLabExists", isInternalLabExists);
		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws ServletException, IOException, SQLException {
		Integer max_centers_inc_default = (Integer)GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default");
		if(max_centers_inc_default > 1){
			req.setAttribute("centers", centerDao.getAllCentersExceptSuper());
		}else {
			req.setAttribute("centers", centerDao.getAllCenters());
		}
		return m.findForward("addshow");
	}

	public ActionForward addCenterToInternalLab(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {
		request.setAttribute("internalLabDetails", dao.getInternalLabDetails());
		request.setAttribute("centers", centerDao.getAllCentersExceptSuper());
		return mapping.findForward("internalLabCenterAssociation");
	}

	public ActionForward showInternalLab(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, IOException, ServletException {

		Map map = new HashMap();
		map.put("center_id", Integer.parseInt(req.getParameter("center_id")));
		map.put("outsource_id", req.getParameter("outsource_id"));

		List<BasicDynaBean> beanList = dao.listAll(null, map, null);
		BasicDynaBean bean = beanList.size() > 0 ? beanList.get(0) : null;
		req.setAttribute("bean", bean);
		req.setAttribute("outSourceId", Integer.parseInt((String)bean.get("outsource_id")));
		req.setAttribute("internalLabDetails", dao.getInternalLabDetails());
		req.setAttribute("centers", centerDao.getAllCentersExceptSuper());
		return m.findForward("internalLabCenterAssociation");

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
		String outSourceDestType = (String)req.getParameter("outsource_dest_type");
		try {
			if (errors.isEmpty()){

				Map map = new HashMap();
				map.put("outsource_id", bean.get("outsource_id"));
				map.put("center_id", bean.get("center_id"));
				BasicDynaBean exists = null;
				List<BasicDynaBean> beanList = dao.listAll(null, map, null);
				exists = beanList.size() > 0 ? beanList.get(0) : null;

				if (exists == null){				
				    boolean success = dao.insert(con, bean);
				    if (success){
				    	con.commit();
				    	if(outSourceDestType.equalsIgnoreCase("C"))
				    		redirect = new ActionRedirect(m.findForward("showInternalLabRedirect"));
				    	else
				    		redirect = new ActionRedirect(m.findForward("showRedirect"));
				    	redirect.addParameter("outsource_id", bean.get("outsource_id"));
				    	redirect.addParameter("center_id", bean.get("center_id"));
				    	redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				    	return redirect;
				    } else {
				    	con.rollback();
				    	flash.error("Failed to add Outhouse Center Association..");
				    }
					  
				} else {
					flash.error("Association already exists");
					}
				} else{
					flash.error("Incorrectly formatted values supplied");
					}
			} finally {
				DataBaseUtil.closeConnections(con, null);
				}
			if(outSourceDestType.equalsIgnoreCase("C"))
				redirect = new ActionRedirect(m.findForward("internalLabRedirect"));
			else
				redirect = new ActionRedirect(m.findForward("addRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

			return redirect;
	}

	public ActionForward show(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, IOException, ServletException {

		Map map = new HashMap();
		map.put("center_id", Integer.parseInt(req.getParameter("center_id")));
		map.put("outsource_id", req.getParameter("outsource_id"));

		List<BasicDynaBean> beanList = dao.listAll(null, map, null);
		BasicDynaBean bean = beanList.size() > 0 ? beanList.get(0) : null;
		req.setAttribute("bean", bean);
		Integer max_centers_inc_default = (Integer)GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default");
		if(max_centers_inc_default > 1){
			req.setAttribute("centers", centerDao.getAllCentersExceptSuper());
		}else {
			req.setAttribute("centers", centerDao.getAllCenters());
		}
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
					if (!dao.isTestAssociatesWithCenterAndOutsource((Integer)bean.get("center_id"), (String)bean.get("outsource_id"))) {						
						success = dao.delete(con, "center_id", bean.get("center_id"), "outsource_id", bean.get("outsource_id"));
						if (!success) {
							flash.error("Failed to delete");
							break;
						}
					} else {
						success = false;
						flash.error("One or more tests associated with outsource "+
								((String[])params.get("_outsource_name"))[i]+ " in the center "+((String[])params.get("_center_name"))[i]);
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
