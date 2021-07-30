/**
 *
 */
package com.insta.hms.master.DiscountAuthorizerMaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;

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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author lakshmi.p
 *
 */
public class DiscountAuthorizerMasterAction extends DispatchAction {


	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		DiscountAuthorizerMasterDAO dao = new DiscountAuthorizerMasterDAO();
		Map requestParams = req.getParameterMap();
		PagedList pagedList = dao.search(requestParams, ConversionUtils.getListingParameter(requestParams),
					"disc_auth_id");
		req.setAttribute("pagedList", pagedList);

		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		List<String>  authorizers = DiscountAuthorizerMasterDAO.getDiscountAuthorizerNames();
		req.setAttribute("authorizersList", new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(authorizers)));
		List<BasicDynaBean> centerLists = CenterMasterDAO.getAllCentersExceptSuper();
		req.setAttribute("centerLists",centerLists);
		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Map params = req.getParameterMap();
		String allcenters = req.getParameter("applicable_for_centers");
		String[] centerid = req.getParameterValues("multicenterid");

		String centers = "";
		if(null != centerid){
				for (String s : centerid){
					centers = centers + s + ",";
			}
			centers = centers.substring(0, centers.length()-1);
		}
		if(null != allcenters){
			centers = allcenters;
		}

		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		DiscountAuthorizerMasterDAO dao = new DiscountAuthorizerMasterDAO();
		BasicDynaBean bean = dao.getBean();
		bean.set("created_timestamp",DateUtil.getCurrentTimestamp());
		bean.set("center_id", centers);
		ConversionUtils.copyToDynaBean(params, bean, errors);
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = null;

		if (errors.isEmpty()) {
			BasicDynaBean exists = dao.findByKey("disc_auth_name", bean.get("disc_auth_name"));
			if (exists == null) {
				bean.set("disc_auth_id", dao.getNextSequence());
				boolean success = dao.insert(con, bean);
				if (success) {
					con.commit();
					flash.success("Discount Authorizer details inserted successfully..");
					redirect = new ActionRedirect(m.findForward("showRedirect"));
					redirect.addParameter("disc_auth_id", bean.get("disc_auth_id"));
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					DataBaseUtil.closeConnections(con,null);
					return redirect;
				} else {
					con.rollback();
					flash.error("Failed to add Authorizer..");
				}
			} else {
				flash.error("Discount Authorizer name already exists..");
			}
		} else {
			flash.error("Incorrectly formatted values supplied");
		}
		redirect = new ActionRedirect(m.findForward("addRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		DiscountAuthorizerMasterDAO dao = new DiscountAuthorizerMasterDAO();
		BasicDynaBean bean = dao.findByKey("disc_auth_id", new Integer(req.getParameter("disc_auth_id")));
		req.setAttribute("bean", bean);
		String centerIds = (String)bean.get("center_id");
		req.setAttribute("centerIds", centerIds);
		List  authorizers = DiscountAuthorizerMasterDAO.getDiscountAuthorizerNames();
		req.setAttribute("authorizersList", new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(authorizers)));
		req.setAttribute("discountsLists", js.serialize(dao.getDiscountsNamesAndIds()));
		List<BasicDynaBean> centerLists = CenterMasterDAO.getAllCentersExceptSuper();
		req.setAttribute("centerLists",centerLists);

		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		Map params = req.getParameterMap();
        String allcenters = req.getParameter("applicable_for_centers");
		String[] centerid = req.getParameterValues("multicenterid");

		String centers = "";
		if(null != centerid){
			for (String s : centerid){
				centers = centers + s + ",";
			}
			centers = centers.substring(0, centers.length()-1);
		}
		if(null != allcenters){
			centers = allcenters;
		}

		List errors = new ArrayList();

		DiscountAuthorizerMasterDAO dao = new DiscountAuthorizerMasterDAO();
		BasicDynaBean bean = dao.getBean();
		bean.set("updated_timestamp",DateUtil.getCurrentTimestamp());
		bean.set("center_id", centers);
		ConversionUtils.copyToDynaBean(params, bean, errors);

		Object key = req.getParameter("disc_auth_id");
		Map keys = new HashMap();
		keys.put("disc_auth_id", Integer.parseInt(key.toString()));
		FlashScope flash = FlashScope.getScope(req);

		if (errors.isEmpty()) {
			BasicDynaBean exists = dao.findByKey("disc_auth_name",bean.get("disc_auth_name"));
			if (exists != null && !key.equals(exists.get("disc_auth_id").toString())) {
				flash.error("Discount Authorizer name already exists..");
			}
			else {
				int success = dao.update(con, bean.getMap(), keys);

				if (success > 0) {
					con.commit();
					flash.success("Discount Authorizer details updated successfully..");
				} else {
					con.rollback();
					flash.error("Failed to update Discount Authorizer details..");
				}
			}
		}
		else {
			flash.error("Incorrectly formatted values supplied");
		}
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter("disc_auth_id" , bean.get("disc_auth_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}
	public static List<BasicDynaBean> getDiscountAuthorizers(int centerId) throws SQLException{
		List<BasicDynaBean>  authorizers = DiscountAuthorizerMasterDAO.getActiveDiscountAuthorizerNames();
		List<BasicDynaBean> discAuths = new ArrayList<BasicDynaBean>();
		for (BasicDynaBean discAuth : authorizers)
		{
			String centerIdsStr = (String)discAuth.get("center_id");
			String[] centerIdsStrArr =  centerIdsStr.split(",");
               for(int i=0; i<centerIdsStrArr.length; i++){
            	   int cenId = Integer.parseInt(centerIdsStrArr[i]);
                   if(cenId == centerId || cenId == 0)
                	   discAuths.add(discAuth);
				}
		}
		if(centerId == 0) discAuths = authorizers;
		return discAuths;
	}

}
