package com.insta.hms.master.Microbiology;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class MicroAbstPanelMasterAction extends DispatchAction{

	GenericDAO dao = new GenericDAO("micro_abst_panel_master");
	JSONSerializer json = new JSONSerializer().exclude("class");

	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
					HttpServletResponse response)throws Exception {

		Map requestParams = request.getParameterMap();
		request.setAttribute("abstPanelNames", json.serialize(dao.getColumnList("abst_panel_name")));
		request.setAttribute("pagedList", MicroAbstPanelMasterDao.getOrganismDetails(requestParams,
				ConversionUtils.getListingParameter(requestParams)));

		return mapping.findForward("list");

	}


	public ActionForward add(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws Exception{

		request.setAttribute("abstPanelNamesAndIds", json.serialize(MicroAbstPanelMasterDao.getPanelmNamesAndIds()));
		return mapping.findForward("addShow");
	}

	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response)throws Exception {

		BasicDynaBean bean = dao.findByKey("abst_panel_id", Integer.parseInt(request.getParameter("abst_panel_id")));
		request.setAttribute("abstPanelNamesAndIds", json.serialize(MicroAbstPanelMasterDao.getPanelmNamesAndIds()));
		request.setAttribute("bean", bean);
		request.setAttribute("grpList", MicroAbstPanelMasterDao.getOrgGrpList(Integer.parseInt(request.getParameter("abst_panel_id"))));
		request.setAttribute("antibioticList", MicroAbstPanelMasterDao.getAntibioticList(Integer.parseInt(request.getParameter("abst_panel_id"))));

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
		ActionRedirect redirect = null;
		FlashScope flash = FlashScope.getScope(request);

		try {

			if (errors.isEmpty()) {
				BasicDynaBean exists = dao.findByKey("abst_panel_name", (String)bean.get("abst_panel_name"));
				if (exists != null) {
					error = "ABST panel name already exists.....";
				} else {
					bean.set("abst_panel_id", dao.getNextSequence());
					success = dao.insert(con, bean);
					if (!success) {
						error = "Fail to add Micro ABST panel master....";
					}
				}
			} else {
				error = "Incorrectly formatted values supplied..";
			}
			if (success) {
				success = saveOrganismGroupValues(request, con, bean.get("abst_panel_id").toString());
				if (!success) {
					error = "Failed to save Organism groups...";
					flash.error(error);
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					return redirect;
				}
			}

			if (success) {
				success = saveAntibioticValues(request, con, bean.get("abst_panel_id").toString());
				if (!success) {
					error = "Failed to save Antibiotics ...";
					flash.error(error);
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					return redirect;
				}
			}
		}finally {
			DataBaseUtil.commitClose(con, success);
		}

		if (error != null) {
			redirect = new ActionRedirect(mapping.findForward("addRedirect"));
			flash.error(error);

		}else {
			redirect = new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter("abst_panel_id", bean.get("abst_panel_id"));
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
			Object key = request.getParameter("abst_panel_id");

			Map<String, Integer> keys = new HashMap<String, Integer>();
			keys.put("abst_panel_id", Integer.parseInt(key.toString()));
			FlashScope flash = FlashScope.getScope(request);
			boolean success = false;

			ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter("abst_panel_id", key.toString());
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

			success = saveOrganismGroupValues(request, con, key.toString());
			if (!success) {
				flash.error("Failed to save Organism groups...");
				return redirect;
			}

			if (success && !saveAntibioticValues(request, con, key.toString())) {
				flash.error("Failed to save Antibiotics ...");
				return redirect;
			}

			if (errors.isEmpty()) {
				success &= dao.update(con, bean.getMap(), keys) > 0;
				if (success) {
					con.commit();
					flash.success("ABST panel master details updated successfully..");
				} else {
					con.rollback();
					flash.error("Failed to update Micro ABST panel master details..");
				}
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
			return redirect;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private boolean saveOrganismGroupValues(HttpServletRequest request, Connection con, String abstPanelID)throws SQLException, IOException {

		String[] abstOrgrIds = request.getParameterValues("micro_abst_orggr_id");
		String[] abstOrgrDeleted = request.getParameterValues("org_deleted");
		String[] orgGrpIds = request.getParameterValues("org_group_id");
		int abstPanelId = Integer.parseInt(abstPanelID);
		GenericDAO microAbstOrggrDAO = new GenericDAO("micro_abst_orggr");
		BasicDynaBean microAbstOrggrBean = microAbstOrggrDAO.getBean();

		boolean success = true;

		for (int i=0; i<abstOrgrIds.length-1; i++) {
			if (abstOrgrIds[i].equals("_") && abstOrgrDeleted[i].equals("false")) {
				microAbstOrggrBean.set("micro_abst_orggr_id", microAbstOrggrDAO.getNextSequence());
				microAbstOrggrBean.set("abst_panel_id", abstPanelId);
				microAbstOrggrBean.set("org_group_id", Integer.parseInt(orgGrpIds[i]));

				success = microAbstOrggrDAO.insert(con, microAbstOrggrBean);
				if (!success)
					return success;
			}

			if (!abstOrgrIds[i].equals("_") && abstOrgrDeleted[i].equals("true")) {

				success = microAbstOrggrDAO.delete(con, "micro_abst_orggr_id", Integer.parseInt(abstOrgrIds[i]));
				if (!success)
					return success;
			}

		}

		return success;
	}

	private boolean saveAntibioticValues(HttpServletRequest request, Connection con, String abstPanelID)throws SQLException, IOException {

		String[] abstAntIds = request.getParameterValues("antibiotic_id_check");
		String[] AntIds = request.getParameterValues("antibiotic_id");
		String[] abstAntDeleted = request.getParameterValues("ant_deleted");
		int abstPanelId = Integer.parseInt(abstPanelID);
		GenericDAO microAbstAntibioticDAO = new GenericDAO("micro_abst_antibiotic_master");
		BasicDynaBean microAbstAntBean = microAbstAntibioticDAO.getBean();

		boolean success = true;

		for (int i=0; i<abstAntIds.length-1; i++) {
			if (abstAntIds[i].equals("_") && abstAntDeleted[i].equals("false")) {
				microAbstAntBean.set("antibiotic_id", Integer.parseInt(AntIds[i]));
				microAbstAntBean.set("abst_panel_id", abstPanelId);

				success = microAbstAntibioticDAO.insert(con, microAbstAntBean);
				if (!success)
					return success;
			}

			if (!abstAntIds[i].equals("_") && abstAntDeleted[i].equals("true")) {

				success = microAbstAntibioticDAO.delete(con, "antibiotic_id", Integer.parseInt(AntIds[i]));
				if (!success)
					return success;
			}

		}

		return success;
	}

}