/**
 *
 */
package com.insta.hms.master.PrescriptionsMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.master.MedicineRoute.MedicineRouteDAO;

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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna.t
 *
 */
public class PrescriptionsMasterAction extends DispatchAction{

	static Logger log = LoggerFactory.getLogger(PrescriptionsMasterAction.class);
	PrescriptionsMasterDAO dao = new PrescriptionsMasterDAO();
	MedicineRouteDAO mrDao = new MedicineRouteDAO();

	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException, ParseException {

		Map requestParams = request.getParameterMap();
		request.setAttribute("pagedList", dao.search(requestParams,
						ConversionUtils.getListingParameter(requestParams), "medicine_name"));
		return mapping.findForward("list");
	}

	public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {
		request.setAttribute("medicineRouteList", mrDao.listAll(null, "status", "A"));
		return mapping.findForward("addshow");
	}

	public ActionForward show(ActionMapping mapping, ActionForm form,
			HttpServletRequest req, HttpServletResponse res)throws
			ServletException,Exception,IOException{

			BasicDynaBean bean = dao.findByKey("medicine_name", req.getParameter("medicine_name"));
			req.setAttribute("bean", bean);
			req.setAttribute("medicineRouteList", mrDao.listAll(null, "status", "A"));

			return mapping.findForward("addshow");
	}


	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {
		Map params = request.getParameterMap();
		String type = mapping.getProperty("master_type"); //request.getParameter("type");

		BasicDynaBean bean = dao.getBean();
		List errorFields = new ArrayList();
		ConversionUtils.copyToDynaBean(params, bean, errorFields);
		String[] medicineRoutesList = request.getParameterValues("route_of_admin");
		String commaSepListOfRoutes = "";
		boolean isfirst = true;
		if (medicineRoutesList != null) {
			for (int i=0; i<medicineRoutesList.length; i++) {
				if (isfirst) {
					commaSepListOfRoutes += medicineRoutesList[i];
					isfirst = false;
				} else {
					commaSepListOfRoutes += ","+ medicineRoutesList[i];
				}
			}
		}
		bean.set("route_of_admin", commaSepListOfRoutes);
		if (bean.get("item_strength") == null || bean.get("item_strength").equals(""))
			bean.set("item_strength_units", null);

		String error = null;
		String msg = null;
		Boolean success = true;
		FlashScope flash = FlashScope.getScope(request);

		if (errorFields.isEmpty()) {
			Connection con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			try {

				String value = (String) bean.get("medicine_name");
				if (value != null)
					bean.set("medicine_name", value.trim());
				if (dao.exist("medicine_name", bean.get("medicine_name"))) {
					flash.put("warning", "Medicine " + value + " already exists..");
					ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

					return redirect;
				}
				if (dao.insert(con, bean)) {
					msg = "Medicine inserted successfully..";
				} else {
					success = false;
					error = "Failed to add Medicine";
				}
			} catch (SQLException se) {
				success = false;
				log.error("", se);
				throw se;
			} finally {
				DataBaseUtil.commitClose(con, success);
			}

		} else {
			success = false;
			error = "Incorrectly formatted details supplied..";
		}

		ActionRedirect redirect = null;
		if (success) {
			flash.put("success", msg);
			redirect = new ActionRedirect(mapping.findForward("listRedirect"));

		} else {
			flash.put("error", error);
			redirect = new ActionRedirect(mapping.findForward("addRedirect"));
		}
		redirect.addParameter("_type", type);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}

	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest req, HttpServletResponse res)throws
			ServletException,Exception,IOException,SQLException{
		Map params = req.getParameterMap();
		String dbName = req.getParameter("dbName");

		BasicDynaBean bean =dao.getBean();
		List errorFields = new ArrayList();
		ConversionUtils.copyToDynaBean(params, bean, errorFields);
		String[] medicineRoutesList = req.getParameterValues("route_of_admin");
		String commaSepListOfRoutes = "";
		boolean isfirst = true;
		if (medicineRoutesList != null) {
			for (int i=0; i<medicineRoutesList.length; i++) {
				if (isfirst) {
					commaSepListOfRoutes += medicineRoutesList[i];
					isfirst = false;
				} else {
					commaSepListOfRoutes += ","+ medicineRoutesList[i];
				}
			}
		}
		bean.set("route_of_admin", commaSepListOfRoutes);
		if (bean.get("item_strength") == null || bean.get("item_strength").equals(""))
			bean.set("item_strength_units", null);

		String error = null;
		String msg = null;
		Boolean success = true;
		FlashScope flash = FlashScope.getScope(req);

		if (errorFields.isEmpty()) {
			Connection con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			try{

				String value = (String) bean.get("medicine_name");
				if (value != null)
					bean.set("medicine_name", value.trim());
				if(dao.exist("medicine_name", bean.get("medicine_name")) && !dbName.equalsIgnoreCase(bean.get("medicine_name").toString())) {
					flash.put("warning", "Medicine " + value + " already exists..");
					ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
					redirect.addParameter("medicine_name", bean.get("medicine_name"));
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

					return redirect;
				}
				String key = req.getParameter("dbName");
				Map<String, String> keys = new HashMap<String, String>();
				keys.put("medicine_name", key );

				if((dao.update(con, bean.getMap(), keys))>0) {
					msg = "Medicine updated successfully..";
				} else {
					success = false;
					error = "Failed to update Medicine.";
				}
			} catch (SQLException se) {
				success = false;
				log.error("", se);
				throw se;
			} finally {
				DataBaseUtil.commitClose(con, success);
			}
		} else {
			success = false;
			error = "Incorrectly formatted details supplied..";
		}
		ActionRedirect redirect = null;
		if (success) {
			flash.put("success", msg);
			redirect = new ActionRedirect(mapping.findForward("showRedirect"));

		} else {
			flash.put("error", error);
		}
		redirect.addParameter("medicine_name", bean.get("medicine_name"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}

	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {
		String[] names = request.getParameterValues("checked");

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
				if (!dao.delete(con, "medicine_name", value)) break;
				deleteCount++;
			}
			if (deleteCount == names.length) success = true;
			else success = false;
		} catch (SQLException se) {
			success = false;
			log.error("", se);
			throw se;
		} finally {
			DataBaseUtil.commitClose(con, success);
		}

		if (success) msg = (names.length == 1?"Medicine":"Medicines") + " deleted successfully..";
		else error = "Failed to delete " + (names.length == 1?"Medicine":"Medicines");


		flash.put("success", msg);
		flash.put("error", error);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}


}
