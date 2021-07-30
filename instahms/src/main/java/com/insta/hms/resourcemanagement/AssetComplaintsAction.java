package com.insta.hms.resourcemanagement;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AssetComplaintsAction extends BaseAction{

	static Logger logger = LoggerFactory.getLogger(MaintenanceScheduleAction.class);
	public static final String[] SEARCH_BOOL_FIELDS = {"statusAll", "statusRecorded", "statusAssigned", "statusResolved", "statusClosed"};

	public static enum COMP_STATUS { RECORDED, ASSIGNED, RESOLVED, CLOSED }

	@IgnoreConfidentialFilters
	public ActionForward list(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		HttpSession session = request.getSession(false);
		AssetComplaintsDAO dao = new AssetComplaintsDAO();
		ArrayList err = new ArrayList();
		HashMap params = new HashMap();
		ConversionUtils.copyBooleanFields(request.getParameterMap(), params, SEARCH_BOOL_FIELDS, err, false);
		ArrayList statusList = null;
		params.get("statusAll");
		Boolean statusAll = (Boolean) params.get("statusAll");

		int roleId = (Integer) session.getAttribute("roleId");
		String multiStoreAccess = (String) session.getAttribute("multiStoreAccess");
		String dept_id =  (String) session.getAttribute("pharmacyStoreId");

		if (!statusAll) {
			statusList = new ArrayList();
			if ((Boolean)params.get("statusRecorded")) {
				statusList.add(new BigDecimal(0));
			}

			if ((Boolean)params.get("statusAssigned")) {
				statusList.add(new BigDecimal(1));
			}

			if ((Boolean)params.get("statusResolved")) {
				statusList.add(new BigDecimal(2));
			}

			if ((Boolean)params.get("statusClosed")) {
				statusList.add(new BigDecimal(3));
			}
		}

		Map listingParams = ConversionUtils.getListingParameter(request.getParameterMap());
		Map filter= getParameterMap(request);

		PagedList pagedList = null;
		String[] complainttype = (String[])filter.get("complaint_type");
		if ((null == complainttype) || (complainttype[0].equalsIgnoreCase("empty"))){
			//remove the dummy value
			filter.remove("complaint_type");
		}

		if(dept_id != null && !dept_id.equals("")) {
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
		pagedList = dao.getComplaintMasterDetails(filter, listingParams);
		request.setAttribute("pagedList", pagedList);
		request.setAttribute("dept_id", dept_id);
		return m.findForward("list");
	}

	@IgnoreConfidentialFilters
	public ActionForward add(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{
		return m.findForward("addshow");
	}

	@IgnoreConfidentialFilters
	public ActionForward create (ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		Map params = request.getParameterMap();
		List errors = new ArrayList();
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			HttpSession session=request.getSession();
			String userid = (String)session.getAttribute("userid");
			AssetComplaintsDAO dao = new AssetComplaintsDAO();
			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);
			String comstatus = ((Object[])params.get("complaint_status"))[0].toString();

			if (COMP_STATUS.RECORDED.equals(comstatus)) {
				bean.set("complaint_status", new BigDecimal(0));
			}else if(COMP_STATUS.ASSIGNED.equals(comstatus))  {
				bean.set("complaint_status", new BigDecimal(1));
			}else if(COMP_STATUS.RESOLVED.equals(comstatus))  {
				bean.set("complaint_status", new BigDecimal(2));
			}else if(COMP_STATUS.CLOSED.equals(comstatus))  {
				bean.set("complaint_status", new BigDecimal(3));
			}
			bean.set("created_by", userid);
			if(errors.isEmpty()){
				bean.set("complaint_id", new BigDecimal(dao.getNextComplaintId()));
				boolean sucess = dao.insert(con, bean);
				if (sucess){
					con.commit();
					FlashScope fScope = FlashScope.getScope(request);
					fScope.success("Complaint Master details inserted successfully.");
					ActionRedirect redirect = new ActionRedirect(m.findForwardConfig("listRedirect"));
					redirect.addParameter(FlashScope.FLASH_KEY, fScope.key());
					return redirect;
				}else{
					con.rollback();
					request.setAttribute("error", "Fail to add complaint master details.");
				}

			}else{
				request.setAttribute("error", "Incorrectly formatted values supplied.");
			}
		}finally {
			if (con != null) con.close();
		}
		return m.findForward("list");
	}

	@IgnoreConfidentialFilters
	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest request, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		HttpSession session=request.getSession();
		String userid = (String)session.getAttribute("userid");
		AssetComplaintsDAO dao = new AssetComplaintsDAO();
		BasicDynaBean bean = dao.findByKey("complaint_id", Integer.parseInt(request.getParameter("complaint_id")));
		request.setAttribute("bean", bean);

		ArrayList  complaintDetails =
			(ArrayList)AssetComplaintsDAO.getComplaintMasters(new BigDecimal(Integer.parseInt(request.getParameter("complaint_id"))));

		request.setAttribute("complaintDetails", complaintDetails);
		request.setAttribute("user", userid);
		return m.findForward("addshow");
	}

	@IgnoreConfidentialFilters
	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest request, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map params = request.getParameterMap();
			List errors = new ArrayList();

			AssetComplaintsDAO dao = new AssetComplaintsDAO();
			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);

			Object key = request.getParameter("complaint_id");

			Map<String, Integer> keys = new HashMap<String, Integer>();
			keys.put("complaint_id", Integer.parseInt(key.toString()));


			if (errors.isEmpty()) {

					int success = dao.update(con, bean.getMap(), keys);

					if (success > 0) {
						con.commit();
						FlashScope flash = FlashScope.getScope(request);
						flash.success("Maintenance Schedule details updated successfully.");
						ActionRedirect redirect = new ActionRedirect(m.findForward("listRedirect"));
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						return redirect;
					} else {
						con.rollback();
						request.setAttribute("error", "Failed to update schedule details.");
					}

			}
			else {
				request.setAttribute("error", "Incorrectly formatted values supplied.");
			}

		}finally {
			if (con != null) con.close();
		}
		return m.findForward("show");
	}
}


