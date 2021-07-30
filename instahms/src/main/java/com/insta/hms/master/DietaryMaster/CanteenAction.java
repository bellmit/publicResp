package com.insta.hms.master.DietaryMaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class CanteenAction extends DispatchAction{

	
	@IgnoreConfidentialFilters
	public ActionForward getMealsToBeDelivered(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response) throws SQLException, ParseException{

	        Map<String, Object[]> params = new HashMap();
	        params.putAll(request.getParameterMap());
			Map<LISTING,Object> listingParams = ConversionUtils.getListingParameter(params);
			int centerId = (Integer)request.getSession().getAttribute("centerId");
			String [] filterCenterIdStr = (String[]) params.get("pr.center_id");
			String date = request.getParameter("meal_date");
			Boolean isNoFilter = false;
			if ((filterCenterIdStr != null 
			    && ( filterCenterIdStr[0] != null && !filterCenterIdStr[0].isEmpty()) 
			    && Integer.parseInt(filterCenterIdStr[0]) == 0)) {
			  params.remove("pr.center_id");
			  isNoFilter = true;
			}
			if (date == null || date.equals("")){
				date = DateUtil.currentDate("dd-MM-yyyy");
			}

			DietaryMasterDAO dao = new DietaryMasterDAO();

			PagedList pagedList = dao.searchMealsToBeDelivered(params, listingParams, isNoFilter);

			request.setAttribute("pagedList", pagedList);

			request.setAttribute("date", date);

			request.setAttribute("method", "updateMealDeliveredStatus");
			boolean multiCentered = GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1;

			request.setAttribute("wards",BedMasterDAO.getAllWardNames( centerId,multiCentered ));
			request.setAttribute("genPrefs", GenericPreferencesDAO.getAllPrefs());
			request.setAttribute("centers",CenterMasterDAO.getAllCentersExceptSuper());
            int selectedUserCenterId =
                filterCenterIdStr == null || StringUtils.isEmpty(filterCenterIdStr[0]) ? centerId
                    : Integer.parseInt((filterCenterIdStr[0]));
            request.setAttribute("selectedUserCenterId", selectedUserCenterId);
			return mapping.findForward("canteenScheduleScreen");
	}


	public ActionForward updateMealDeliveredStatus (ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response) throws SQLException, IOException{
			int updateCount = 0;
			HttpSession session = request.getSession(false);
			FlashScope scope = FlashScope.getScope(request);
			String[] orderedId = request.getParameterValues("_orderedId");
			String[] updateStatus = request.getParameterValues("_updateStatus");
			String userName = (String)session.getAttribute("userid");
			Connection con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			for (int i=0;i<orderedId.length;i++) {
				if (updateStatus[i].equals("Y")) {
				DietPrescribedDAO pdao = new DietPrescribedDAO();
				BasicDynaBean bean = pdao.getBean();
				bean.set("status", "Y");
				bean.set("status_updated_time", DateUtil.getCurrentTimestamp());
				bean.set("status_updated_by", userName);
				updateCount = pdao.update(con, bean.getMap(), "ordered_id", Integer.parseInt(orderedId[i]));
				}
			}
			if (updateCount >0){
				con.commit();
				DataBaseUtil.closeConnections(con, null);
				scope.success("Meals delivered status are updated successfully....");
			}else{
				con.rollback();
				DataBaseUtil.closeConnections(con, null);
				scope.error("Fail to update the meals delivered status....");
			}
			ActionRedirect redirect = new ActionRedirect(mapping.findForward("canteenRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY, scope.key());
			redirect.addParameter("dp.meal_date", request.getParameter("dp.meal_date"));
			redirect.addParameter("dp.meal_timing", request.getParameter("dp.meal_timing"));
			redirect.addParameter("dm.meal_name", request.getParameter("dm.meal_name"));
			redirect.addParameter("wn.ward_name", request.getParameter("wn.ward_name"));
			redirect.addParameter("dp.status", request.getParameter("dp.status"));
			redirect.addParameter("pr.center_id", request.getParameter("pr.center_id"));
            redirect.addParameter("pr.center_id@type", request.getParameter("pr.center_id@type"));
			return redirect;
	}



}
