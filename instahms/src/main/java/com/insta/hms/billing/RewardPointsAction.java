/**
 *
 */
package com.insta.hms.billing;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.AreaMaster.AreaMasterDAO;
import com.insta.hms.master.CityMaster.CityMasterDAO;
import com.insta.hms.master.CountryMaster.CountryMasterDAO;
import com.insta.hms.master.StateMaster.StateMasterDAO;

import flexjson.JSONSerializer;

/**
 * @author lakshmi
 *
 */
public class RewardPointsAction extends BaseAction {


	static Logger log = LoggerFactory.getLogger(RewardPointsAction.class);

	/*
	 * Get reward points search screen
	 */
	@IgnoreConfidentialFilters
	public ActionForward getRewardPointsScreen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		setRequestAttributes(request);
		return mapping.findForward("RewardPointsList");
	}

	/*
	 * Filtered list of patients and their reward points details
	 */
	@IgnoreConfidentialFilters
	public ActionForward getRewardPoints(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception {

		Map map = getParameterMap(request);
		PagedList list = RewardPointsDAO.searchPatients(map, ConversionUtils.getListingParameter(map));
		request.setAttribute("pagedList",list);
		setRequestAttributes(request);
		return mapping.findForward("RewardPointsList");
	}

	public void setRequestAttributes(HttpServletRequest request) throws SQLException  {
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("countryList",js.serialize(CountryMasterDAO.getCountryList(false)));
		request.setAttribute("areaList", js.serialize(AreaMasterDAO.getPatientAreaList()));
		request.setAttribute("cityList", js.serialize(CityMasterDAO.getPatientCityList(false)));
		request.setAttribute("stateList", js.serialize(StateMasterDAO.getStateIdName()));
	}
	
	@IgnoreConfidentialFilters
	public ActionForward addRewardPoints(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException
	{
		String mrNo = request.getParameter("mr_no");
		if ((mrNo != null) && !mrNo.equals("")) {
			Map patmap = PatientDetailsDAO.getPatientGeneralDetailsMap(mrNo);
			if (patmap == null) {
				FlashScope flash = FlashScope.getScope(request);
				flash.put("error", mrNo+" doesn't exists.");
				ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRewardPointsRedirect"));
				redirect.addParameter("_method", "addRewardPoints");
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
		}
		
		BigDecimal totalPointsAvailable = BigDecimal.ZERO;
		request.setAttribute("totalPointsAvailable", BigDecimal.ZERO);
		BigDecimal flag = BigDecimal.ZERO;
		if ((mrNo != null) && !mrNo.equals("")) {
			flag = new RewardPointsDAO().getTotalPointsAvailable(mrNo);
			totalPointsAvailable = (flag==null) ? BigDecimal.ZERO : flag ;
			//BasicDynaBean rewardPointStatusBean = new GenericDAO("reward_points_status").findByKey("mr_no", mrNo);
			//if(rewardPointStatusBean!=null)
				request.setAttribute("totalPointsAvailable", totalPointsAvailable);
		}
		return mapping.findForward("addRewardPoints");
	}
	
	public ActionForward saveRewardPoints(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException,IOException
	{
		
		GenericDAO rewardPointEarningDAO = new GenericDAO("reward_points_earnings");
		String mr_no = request.getParameter("mr_no");
		String points = request.getParameter("points");
		String remarks = request.getParameter("remarks");
		BasicDynaBean rewardPointEarningBean = rewardPointEarningDAO.getBean(); 
		GenericDAO rewardPointStatusDAO = new GenericDAO("reward_points_status");

		Connection con = null;
		boolean success = false;
		try{
		   con = DataBaseUtil.getConnection();
		   con.setAutoCommit(false);
		   rewardPointEarningBean.set("mr_no", mr_no);   		//insert data into dynabean here
		   rewardPointEarningBean.set("points", Integer.parseInt(points));   		//insert data into dynabean here
		   rewardPointEarningBean.set("remarks", remarks);   		//insert data into dynabean here
		   rewardPointEarningBean.set("date", DateUtil.getCurrentTimestamp());   		//insert data into dynabean here
		   rewardPointEarningBean.set("bill_no", "");   		//insert data into dynabean here
		   rewardPointEarningBean.set("eligible_value", BigDecimal.ZERO);   		//insert data into dynabean here
		   rewardPointEarningBean.set("entry_type", "M");   		//insert data into dynabean here
		   rewardPointEarningBean.set("entry_id", rewardPointEarningDAO.getNextSequence() );   		//insert data into dynabean here
		   success = rewardPointEarningDAO.insert(con,rewardPointEarningBean);
		   
		   if(rewardPointStatusDAO.exist("mr_no",mr_no, false))
		   {
			   BasicDynaBean bean =  rewardPointStatusDAO.findByKey("mr_no", mr_no);
			   
			   int totalpointsEarned = new RewardPointsDAO().getTotalpointsEarned(con,mr_no); 
			   bean.set("points_earned",totalpointsEarned);
			   success = rewardPointStatusDAO.update(con, bean.getMap(), "mr_no", mr_no) >= 0; 
		   }
		   else
		   {
			   BasicDynaBean rewardPointStatusBean = rewardPointStatusDAO.getBean(); 
			   rewardPointStatusBean.set("mr_no", mr_no);
			   rewardPointStatusBean.set("points_earned", Integer.parseInt(points));
			   rewardPointStatusBean.set("points_redeemed", 0);
			   rewardPointStatusBean.set("open_points_redeemed", 0);
			   success = success & (rewardPointStatusDAO.insert(con,rewardPointStatusBean));
		   }   
		}finally{
		 DataBaseUtil.commitClose(con,success);
		}
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRewardPointsRedirect"));
		redirect.addParameter("mr_no", mr_no);
		return redirect;
	}
}
