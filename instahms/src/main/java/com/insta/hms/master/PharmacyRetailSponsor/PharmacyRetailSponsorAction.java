package com.insta.hms.master.PharmacyRetailSponsor;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PharmacyRetailSponsorAction extends DispatchAction{
  
  private static final GenericDAO storeRetailSponsorsDAO = new GenericDAO("store_retail_sponsors");

	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		PharmacyRetailSponsorDAO dao = new PharmacyRetailSponsorDAO();
		Map requestParams = req.getParameterMap();
		PagedList pagedList = dao.search(requestParams,
					ConversionUtils.getListingParameter(req.getParameterMap()), "sponsor_name");
		req.setAttribute("pagedList", pagedList);

		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = null;
		String error = null;

		BasicDynaBean bean = storeRetailSponsorsDAO.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = null;

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			if (errors.isEmpty()) {
				BasicDynaBean exists = storeRetailSponsorsDAO.findByKey("sponsor_name", bean.get("sponsor_name"));
				if (exists == null) {
					bean.set("sponsor_id", storeRetailSponsorsDAO.getNextSequence());
					boolean success = storeRetailSponsorsDAO.insert(con, bean);
					if (success) {
						con.commit();
						redirect = new ActionRedirect(m.findForward("showRedirect"));
						flash.success("Retail Sponsor master details inserted successfully..");
						redirect.addParameter("sponsor_id", bean.get("sponsor_id"));
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						DataBaseUtil.closeConnections(con, null);
						return redirect;
					} else {
						con.rollback();
						error =  "Failed to add  Sponsor..";
					}
				} else {
					error =  "Sponsor name already exists..";
				}
			} else {
				error = "Incorrectly formatted values supplied";
			}
			flash.error(error);
			redirect = new ActionRedirect(m.findForward("addRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		}finally {
			DataBaseUtil.closeConnections(con, null);
		}
		return redirect;
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		PharmacyRetailSponsorDAO cdao = new PharmacyRetailSponsorDAO();
		Object sponsorId = req.getAttribute("sponsor_id");
		if (sponsorId == null)  sponsorId = req.getParameter("sponsor_id");
		BasicDynaBean bean = storeRetailSponsorsDAO.findByKey("sponsor_id",Integer.parseInt(sponsorId.toString()));
		req.setAttribute("bean", bean);
		req.setAttribute("retailSponsorsLists", js.serialize(cdao.getPharmacyRetailSponsorsNamesAndIds()));

		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Map params = req.getParameterMap();
		List errors = new ArrayList();

		BasicDynaBean bean = storeRetailSponsorsDAO.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		Object key = ((Object[])params.get("sponsor_id"))[0];
		Map<String, Integer> keys = new HashMap<String, Integer>();
		keys.put("sponsor_id", Integer.parseInt(key.toString()));
		FlashScope flash = FlashScope.getScope(req);

		if (errors.isEmpty()) {
			DynaBean exists = storeRetailSponsorsDAO.findByKey("sponsor_name", bean.get("sponsor_name"));
			if (exists != null && Integer.parseInt(key.toString()) != ((Integer)exists.get("sponsor_id")).intValue()) {
				flash.error("Sponsor name already exists..");
			}
			else {
				Connection con = null;
				try {
					con = DataBaseUtil.getConnection();
					con.setAutoCommit(false);
					int success = storeRetailSponsorsDAO.update(con, bean.getMap(), keys);

					if (success > 0) {
						con.commit();
						flash.success("Sponsor master details updated successfully..");
					} else {
						con.rollback();
						flash.error("Failed to update Sponsor master details..");
					}
				}finally {
					DataBaseUtil.closeConnections(con, null);
				}
			}
		}
		else {
             flash.error("Incorrectly formatted values supplied");
		}
		flash.put("sponsor_id", key);
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter("sponsor_id", bean.get("sponsor_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}




}
