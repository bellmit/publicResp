package com.insta.hms.master.PrescriptionInstructionsMaster;

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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author nikunj.s
 *
 */
public class PrescriptionInstructionsMasterAction extends DispatchAction {

	static Logger logger = LoggerFactory.getLogger(PrescriptionInstructionsMasterAction.class);

	PrescriptionInstructionsMasterDAO dao = new PrescriptionInstructionsMasterDAO();

	public ActionForward list(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		Map map= request.getParameterMap();
		List prescriptionInstructionsList = PrescriptionInstructionsMasterDAO.getAllPrescriptionInstructions();
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("prescriptionInstructionsList", js.serialize(ConversionUtils.copyListDynaBeansToMap(prescriptionInstructionsList)));
		PagedList pagedList = dao.getPrescriptionInstructionsMasterDetails(map,ConversionUtils.getListingParameter(request.getParameterMap()));
		request.setAttribute("pagedList", pagedList);

		return m.findForward("list");
	}


	public ActionForward add(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		List prescriptionInstructionsList = PrescriptionInstructionsMasterDAO.getAllPrescriptionInstructions();
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("prescriptionInstructionsList", js.serialize(ConversionUtils.copyListDynaBeansToMap(prescriptionInstructionsList)));
		return m.findForward("addshow");
	}

	public ActionForward create (ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

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
				boolean exists = dao.exist("instruction_desc", ((String)(bean.get("instruction_desc"))).trim());
				if (exists) {
					error = "Prescription Instruction is already exists.....";
				} else {
					bean.set("instruction_id", dao.getNextSequence());
					success = dao.insert(con, bean);
					if (!success) {
						error = "Fail to add prescription instruction to the master....";
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
			redirect = new ActionRedirect(m.findForward("addRedirect"));
			flash.error(error);

		}else {
			redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("instruction_id", bean.get("instruction_id"));
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		BasicDynaBean bean = dao.findByKey("instruction_id", Integer.parseInt(req.getParameter("instruction_id")));
		req.setAttribute("bean", bean);

		List prescriptionInstructionsList = PrescriptionInstructionsMasterDAO.getAllPrescriptionInstructions();
		JSONSerializer js = new JSONSerializer().exclude("class");
		req.setAttribute("prescriptionInstructionsList", js.serialize(ConversionUtils.copyListDynaBeansToMap(prescriptionInstructionsList)));

		return m.findForward("addshow");
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

			Integer key = Integer.parseInt(req.getParameter("instruction_id"));
			Map<String, Integer> keys = new HashMap<String, Integer>();
			keys.put("instruction_id", key);
			FlashScope flash = FlashScope.getScope(req);

			if (errors.isEmpty()) {
				int success = dao.update(con, bean.getMap(), keys);
				if (success > 0) {
					con.commit();
					flash.success("Prescription Instruction master details updated successfully..");
				} else {
					con.rollback();
					flash.error("Failed to update Prescription Instruction master details..");
				}
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
			ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("instruction_id", key.toString());
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {
		String[] names = request.getParameterValues("checked");
		String presInstructionName = request.getParameter("presInstructionName");

		String error = null;
		String msg = null;
		FlashScope flash = FlashScope.getScope(request);

		if (names == null) {
			error = "No data supplied for delete..";
			flash.put("warning", error);
			ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

			return redirect;
		}

		Boolean success = true;
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		int deleteCount = 0;
		try {
			for (String value: names) {
				if (!dao.delete(con, "instruction_id", Integer.parseInt(value))) break;
				deleteCount++;
			}
			if (deleteCount == names.length) success = true;
			else success = false;
		} catch (SQLException se) {
			success = false;
			logger.error("", se);
			throw se;
		} finally {
			DataBaseUtil.commitClose(con, success);
		}

		if (success) msg = (names.length == 1?"Prescription Instruction":"Prescription Instruction") + " deleted successfully..";
		else error = "Failed to delete " + (names.length == 1?"Prescription Instruction":"Prescription Instruction");


		flash.put("success", msg);
		flash.put("error", error);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("instruction_desc", presInstructionName);
		redirect.addParameter("instruction_desc@op", "ico");

		return redirect;
	}

}