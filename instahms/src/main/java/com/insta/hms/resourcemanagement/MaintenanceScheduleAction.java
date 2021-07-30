package com.insta.hms.resourcemanagement;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class MaintenanceScheduleAction extends BaseAction{

	private static Logger logger = LoggerFactory.getLogger(MaintenanceScheduleAction.class);
	private static final String[] SEARCH_BOOL_FIELDS = 
	   {"statusAll", "statusActive", "statusRetired", "sortReverse"};
  private static final String GET_CONTRACTORS_QUERY = 
      "SELECT contractor_id, contractor_name FROM contractor_master";
  private static final MaintenanceScheduleDAO dao = new MaintenanceScheduleDAO();
	

	@IgnoreConfidentialFilters
	public ActionForward list(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		HttpSession session = request.getSession(false);
		ArrayList err = new ArrayList();
		HashMap params = new HashMap();
		ConversionUtils.copyBooleanFields(request.getParameterMap(), params, SEARCH_BOOL_FIELDS, err, false);

		int roleId = (Integer) session.getAttribute("roleId");
		String multiStoreAccess = (String) session.getAttribute("multiStoreAccess");
		String dept_id =  (String) session.getAttribute("pharmacyStoreId");

		Map listingParams = ConversionUtils.getListingParameter(request.getParameterMap());
		Map filter= getParameterMap(request);

		PagedList pagedList = null;

		if(dept_id != null  && !dept_id.equals("")) {
			if (!filter.containsKey("asset_dept")){
				filter.put("asset_dept", new String[]{dept_id});
				filter.put("asset_dept@type", new String[]{"integer"});
				filter.put("asset_dept@cast", new String[]{"y"});
			}
		}
		int centerId = (Integer) request.getSession(false).getAttribute("centerId");
		if (centerId != 0) {
			filter.put("center_id", new String[]{centerId+""});
			filter.put("center_id@type", new String[]{"integer"});
		}
		if(roleId == 1 || roleId == 2 || multiStoreAccess.equals("A") || (dept_id != null && !dept_id.equals("")))
		pagedList = dao.getMaintScheduleDetails(filter, listingParams);
		request.setAttribute("pagedList", pagedList);
		request.setAttribute("dept_id", dept_id);
		return m.findForward("list");
	}

	@IgnoreConfidentialFilters
	public ActionForward add(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{

		ArrayList maintSchedules = (ArrayList)MaintenanceScheduleDAO.getAllMaintSchedules();
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("maintSchedules", js.serialize(maintSchedules));
    request.setAttribute("contractors", js.serialize(ConversionUtils.copyListDynaBeansToMap(
        DataBaseUtil.queryToDynaList(GET_CONTRACTORS_QUERY))));

		return m.findForward("addshow");
	}

	@IgnoreConfidentialFilters
	public ActionForward create (ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		Map params = request.getParameterMap();
		List errors = new ArrayList();
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);
			bean.set("next_maint_date", DataBaseUtil.parseDate(((Object[])params.get("next_maint_date"))[0].toString()));
			if(errors.isEmpty()){
				BasicDynaBean exists = dao.findByKey("maint_id", bean.get("maint_id"));
				if (exists == null){
					bean.set("maint_id", new BigDecimal(dao.getNextMaintScheduleId()));

					boolean sucess = dao.insert(con, bean);
					if (sucess){
						con.commit();
						FlashScope fScope = FlashScope.getScope(request);
						fScope.success("Maintenance Schedule details inserted successfully.");
						ActionRedirect redirect = new ActionRedirect(m.findForwardConfig("listRedirect"));
						redirect.addParameter(FlashScope.FLASH_KEY, fScope.key());
						return redirect;
					}else{
						con.rollback();
						request.setAttribute("error", "Fail to add maintenance schedule.");
					}
				}else{
					request.setAttribute("error", "Schedule name already exists.");
				}
			}else{
				request.setAttribute("error", "Incorrectly formatted values supplied.");
			}
		}
		finally {
			if( con != null) con.close();
		}
		return m.findForward("list");
	}

	@IgnoreConfidentialFilters
	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest request, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		String userid = (String) request.getSession().getAttribute("userId");
		BasicDynaBean bean = dao.findByKey("maint_id", Integer.parseInt(request.getParameter("maint_id")));
		request.setAttribute("bean", bean);

		int asset_id = Integer.parseInt(bean.get("asset_id").toString());
		String batch_no = bean.get("batch_no").toString();
		java.util.Date  lastMaintDate = dao.getLastUpdatedMaintDate(asset_id, batch_no);
		request.setAttribute("lastMaintDate", lastMaintDate);

		if( bean.get("contractor_id") != null) {
		String contractor_name = new GenericDAO("contractor_master").findByKey("contractor_id", bean.get("contractor_id")).get("contractor_name").toString();
		request.setAttribute("contractor_name", contractor_name);
		}
		ArrayList<String>  maintSchedules = (ArrayList)MaintenanceScheduleDAO.getAllMaintSchedules();
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("maintSchedules", js.serialize(maintSchedules));
    request.setAttribute("contractors", js.serialize(ConversionUtils.copyListDynaBeansToMap(
        DataBaseUtil.queryToDynaList(GET_CONTRACTORS_QUERY))));
		request.setAttribute("userId", userid);
		return m.findForward("addshow");
	}

	@IgnoreConfidentialFilters
	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest request, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = null;
		FlashScope flash = FlashScope.getScope(request);
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map params = request.getParameterMap();
			List errors = new ArrayList();


			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);

			bean.set("next_maint_date", DataBaseUtil.parseDate(((Object[])params.get("next_maint_date"))[0].toString()));

			Object key = request.getParameter("maint_id");

			Map<String, Integer> keys = new HashMap<String, Integer>();
			keys.put("maint_id", Integer.parseInt(key.toString()));


			if (errors.isEmpty()) {

					int success = dao.update(con, bean.getMap(), keys);

					if (success > 0) {
						con.commit();

						flash.success("Maintenance Schedule details updated successfully..");
					} else {
						con.rollback();
						flash.error ("Failed to update schedule details..");
					}

			}
			else {
				flash.error("Incorrectly formatted values supplied");
			}

		} finally {
			if( con != null )
				con.close();
		}
		ActionRedirect redirect = new ActionRedirect(m.findForward("listRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;

	}
}

