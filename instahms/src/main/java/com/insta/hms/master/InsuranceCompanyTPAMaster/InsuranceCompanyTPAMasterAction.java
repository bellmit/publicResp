/**
 *
 */
package com.insta.hms.master.InsuranceCompanyTPAMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.InsuranceCompMaster.InsuCompMasterDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author lakshmi.p
 *
 */
public class InsuranceCompanyTPAMasterAction extends BaseAction {

	static Logger logger = LoggerFactory.getLogger(InsuranceCompanyTPAMasterAction.class);
    private static final GenericDAO tpaMasterDAO = new GenericDAO("tpa_master");

	InsuranceCompanyTPAMasterDAO dao = new InsuranceCompanyTPAMasterDAO();

	public ActionForward list(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res)  throws Exception {

		Map requestParams = req.getParameterMap();
		Map listingParams = ConversionUtils.getListingParameter(requestParams);
		JSONSerializer js = new JSONSerializer().exclude("class");
		PagedList pagedList = dao.searchCompanyTPAList(requestParams, listingParams);
		req.setAttribute("pagedList", pagedList);
		req.setAttribute("insCompList", js.serialize(ConversionUtils.listBeanToListMap(new InsuCompMasterDAO().listAll())));
		req.setAttribute("tpaList",js.serialize(ConversionUtils.listBeanToListMap(tpaMasterDAO.listAll())));

		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws Exception {
		JSONSerializer js = new JSONSerializer().exclude("class");
		req.setAttribute("insCompList", js.serialize(ConversionUtils.listBeanToListMap(new InsuCompMasterDAO().listAll())));
		req.setAttribute("tpaList",js.serialize(ConversionUtils.listBeanToListMap(tpaMasterDAO.listAll())));
		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
		throws IOException, ServletException, Exception {

		Map params = getParameterMap(req);
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = null;

		if (errors.isEmpty()) {

			BasicDynaBean exists = dao.findByCompanyTPA(con, bean);
			if(exists == null) {
				boolean success = dao.insert(con, bean);
				if (success) {
					con.commit();
					redirect = new ActionRedirect(m.findForward("listRedirect"));
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					redirect.addParameter("insurance_co_id", bean.get("insurance_co_id"));
					DataBaseUtil.closeConnections(con, null);
					return redirect;
				} else {
					con.rollback();
					flash.error("Failed to add Company TPA...");
				}
			} else {
				flash.error("Company With TPA already exists...");
			}
		} else {
			flash.error("Incorrectly formatted values supplied");
		}
		redirect = new ActionRedirect(m.findForward("listRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}

	public ActionForward delete(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
		throws IOException, ServletException, Exception {

		FlashScope flash = FlashScope.getScope(req);
		String[] insuranceCoIds  = req.getParameterValues("_insCompany");
		String[] tpaIds = req.getParameterValues("_tpa");

		String error = null;
		if (tpaIds.length > 0) {
			error = dao.deleteTPA(insuranceCoIds, tpaIds);
		}

		String rediectStr = req.getHeader("Referer").
        replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
		ActionRedirect redirect = new ActionRedirect(rediectStr);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		if (error != null) {
			flash.put("error", error);
		} else {
			flash.put("success", "TPA deleted successfully... ");
		}
		return redirect;

	}
}
