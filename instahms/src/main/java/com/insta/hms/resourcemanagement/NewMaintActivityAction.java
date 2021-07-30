package com.insta.hms.resourcemanagement;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.FixedAssetMaster.FixedAssetMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class NewMaintActivityAction extends BaseAction{

	static Logger logger = LoggerFactory.getLogger(MaintenanceScheduleAction.class);

	@IgnoreConfidentialFilters
	public ActionForward show(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		FixedAssetMasterDAO dao = new FixedAssetMasterDAO();
		HttpSession session = request.getSession(false);
		int roleId = (Integer) session.getAttribute("roleId");
		String multiStoreAccess = (String) session.getAttribute("multiStoreAccess");
		String dept_id =  (String) session.getAttribute("pharmacyStoreId");

		Map listingParams = ConversionUtils.getListingParameter(request.getParameterMap());
		Map filter= getParameterMap(request);
		PagedList pagedList = null;

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
			pagedList = dao.getMaintActivityDetails(filter, listingParams);

		request.setAttribute("pagedList", pagedList);
		request.setAttribute("dept_id", dept_id);
		return m.findForward("show");
	}


	@IgnoreConfidentialFilters
	public ActionForward list(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

			Map listingParams = ConversionUtils.getListingParameter(request.getParameterMap());
			if (null != request.getParameter("maint_activity_id")){
				List list = FixedAssetMasterDAO.getNewMaintAssetHistory(
						new BigDecimal(Integer.parseInt(request.getParameter("maint_activity_id"))));
				request.setAttribute("componentDetials",list);
				request.setAttribute("operation","update");
				request.setAttribute("maint_activity_id",request.getParameter("maint_activity_id"));

			}
		return m.findForward("list");
	}

	@IgnoreConfidentialFilters
	public ActionForward create (ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			BasicDynaBean userdynaBean = null;
			boolean success = false;

	        Map<String, String[]> componentMap = request.getParameterMap();
	        String[] component = componentMap.get("component");
	        String[] description = componentMap.get("description");
	        String[] labourCost =  componentMap.get("labourCost");
	        String[] cost =  componentMap.get("cost");
	        String[] deleted = componentMap.get("componentCheckBox");
	        int item_id ;
	        String asset_id = request.getParameter("asset_id");
	        String batchNo =  request.getParameter("batch_no");
	        String maint_date = request.getParameter("maint_date");
	        String sch_date =  request.getParameter("scheduled_date");
	       BigDecimal maint_activity_id = new BigDecimal(new GenericDAO("asset_maintenance_activity").getNextSequence());

	       userdynaBean = new GenericDAO("asset_maintenance_activity").getBean();
	       userdynaBean.set("batch_no", batchNo);
	  	   userdynaBean.set("maint_activity_id", maint_activity_id);
	       userdynaBean.set("maint_date",  DataBaseUtil.parseDate(maint_date));
	       userdynaBean.set("scheduled_date",  DataBaseUtil.parseDate(sch_date));
	       userdynaBean.set("maint_by" , request.getParameter("maint_by"));
	       userdynaBean.set("description", request.getParameter("maint_description"));

           if(asset_id!=null){
        	 userdynaBean.set("asset_id",   Integer.parseInt(asset_id));
	       if(new GenericDAO("asset_maintenance_activity").insert(con, userdynaBean)){
	    	   success = true;
	         }
           }
	       if(success){
	    	   GenericDAO gdao=  new GenericDAO("asset_maintenance_activity_item");
		        for(int i = 0;i<component.length-1;i++){
		        	 if (deleted[i].equals("false")){
			        	 userdynaBean = new GenericDAO("asset_maintenance_activity_item").getBean();
			        	 item_id =  gdao.getNextSequence();
			        	 userdynaBean.set("item_id", new BigDecimal(item_id));
			        	 userdynaBean.set("maint_activity_id", maint_activity_id);
			             userdynaBean.set("component",  URLDecoder.decode(component[i],"UTF-8"));
			             userdynaBean.set("description", URLDecoder.decode(description[i],"UTF-8"));

			             if (null != labourCost[i] && !labourCost[i].equalsIgnoreCase("")) {
			            	 userdynaBean.set("labor_cost",  BigDecimal.valueOf(Float.parseFloat(labourCost[i])));
			             }else {
			            	 userdynaBean.set("labor_cost",  new BigDecimal(0));
			             }

			             if (null != cost[i] && !cost[i].equalsIgnoreCase("")) {
			            	  userdynaBean.set("part_cost" ,BigDecimal.valueOf(Float.parseFloat(cost[i])));
			             }else {
			            	  userdynaBean.set("part_cost" ,new BigDecimal(0));
			             }


			             if(new GenericDAO("asset_maintenance_activity_item").insert(con, userdynaBean)){
			            	 success = true;
			            	 request.setAttribute("success", "New Maintenance Activity details inserted successfully");
			             }
			             if(!success) {
			            	 request.setAttribute("success", "Fail to add new maintenance activity details");
			            	 break;
			             }
		        	 }
		        }
	       }

	       	FixedAssetMasterDAO assetDao = new FixedAssetMasterDAO();
			MaintenanceScheduleDAO mdao = new MaintenanceScheduleDAO();
			String freq = mdao.getMaintFrequency(con, Integer.parseInt(asset_id), batchNo);

			int num_of_days=0;
			if(freq != null  && !freq.equals("")) {

				if(freq.equals("Weekly")) num_of_days = 7;

				else if(freq.equals("Every two weeks")) num_of_days = 14;

				else if(freq.equals("Monthly")) num_of_days = 30;

				else if(freq.equals("Quarterly")) num_of_days = 90;

				else if(freq.equals("Semi-annually")) num_of_days = 180;

				else if(freq.equals("Annually")) num_of_days = 365;
			}

			int success_next_maint_update = assetDao.UpdateNextMaintDate(con, Integer.parseInt(asset_id),batchNo, num_of_days);


	       if((!success)&&(null==asset_id)){
          	 request.setAttribute("success", "Fail to add new maintenance activity details");
	       }
			if (success) {
				con.commit();
			} else {
				con.rollback();
			}
		}finally {
			if (con != null) con.close();
		}
		ActionRedirect redirect = new ActionRedirect(m.findForward("listRedirect"));
		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest request, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = null;
		Map<String, String[]> componentMap = request.getParameterMap();
        String[] component =componentMap.get("component");
        String[] description =  componentMap.get("description");
        String[] labourCost = componentMap.get("labourCost");
        String[] cost = componentMap.get("cost");
        String[] itemid = componentMap.get("item_id");
        String[] deletedItem = componentMap.get("componentCheckBox");
        int item_id = 0;

		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map<String, String[]> params = request.getParameterMap();
		    String asset_id = request.getParameter("asset_id");
		    String batchNo = request.getParameter("batch_no");


			GenericDAO dao = new GenericDAO("asset_maintenance_activity");
			BasicDynaBean bean = dao.getBean();

			bean.set("maint_activity_id", new BigDecimal(Integer.parseInt((String)request.getParameter("maint_activity_id"))));
			bean.set("asset_id", Integer.parseInt(asset_id));
			bean.set("batch_no", batchNo);
			bean.set("maint_date", DataBaseUtil.parseDate((String)request.getParameter("maint_date")));
			bean.set("scheduled_date", DataBaseUtil.parseDate((String)request.getParameter("scheduled_date")));
			bean.set("maint_by", request.getParameter("maint_by"));
			bean.set("description", request.getParameter("maint_description"));

			Object key = request.getParameter("maint_activity_id");

			Map<String, Integer> keys = new HashMap<String, Integer>();
			keys.put("maint_activity_id", Integer.parseInt(key.toString()));
			int success_main = dao.update(con, bean.getMap(), keys);

			FixedAssetMasterDAO assetDao = new FixedAssetMasterDAO();
			MaintenanceScheduleDAO mdao = new MaintenanceScheduleDAO();
			String freq = mdao.getMaintFrequency(con, Integer.parseInt(asset_id), batchNo);

			int num_of_days=0;
			if(freq != null  && !freq.equals("")) {

				if(freq.equals("Weekly")) num_of_days = 7;

				else if(freq.equals("Every two weeks")) num_of_days = 14;

				else if(freq.equals("Monthly")) num_of_days = 30;

				else if(freq.equals("Quarterly")) num_of_days = 90;

				else if(freq.equals("Semi-annually")) num_of_days = 180;

				else if(freq.equals("Annually")) num_of_days = 365;
			}
			int success_next_maint_update = assetDao.UpdateNextMaintDate(con, Integer.parseInt(asset_id),batchNo, num_of_days);

			int success_item = 0;
			for(int i = 0;i<component.length-1;i++){
				if (deletedItem[i].equals("false")){
					GenericDAO dao1 = new GenericDAO("asset_maintenance_activity_item");
					BasicDynaBean bean1 = dao1.getBean();
					if ((null != itemid) && (itemid.length > -1)){
						if ((null != ((String)itemid[i])) && (!((String)itemid[i]).equals(""))){
							bean1.set("item_id", new BigDecimal(Integer.parseInt((String)itemid[i])));
							item_id = Integer.parseInt((String)itemid[i]);
							bean1.set("maint_activity_id", new BigDecimal(Integer.parseInt((String)request.getParameter("maint_activity_id"))));
							bean1.set("component", URLDecoder.decode(component[i],"UTF-8"));
							bean1.set("description",  URLDecoder.decode(description[i],"UTF-8"));

							 if (null != labourCost && !(labourCost[i].equalsIgnoreCase(""))) {
								 bean1.set("labor_cost",  BigDecimal.valueOf(Float.parseFloat(labourCost[i])));
					         }else {
					        	 bean1.set("labor_cost",  new BigDecimal(0));
					         }

					         if (null != cost && !cost[i].equalsIgnoreCase("")) {
					        	 bean1.set("part_cost" ,BigDecimal.valueOf(Float.parseFloat(cost[i])));
					         }else {
					        	 bean1.set("part_cost" ,new BigDecimal(0));
					         }
							Map<String, Integer> keys1 = new HashMap<String, Integer>();
							keys1.put("item_id",Integer.parseInt((String)itemid[i]));
							success_item += dao1.update(con, bean1.getMap(), keys1);
						} else{
							item_id =  new GenericDAO("asset_maintenance_activity_item").getNextSequence();
							bean1.set("item_id", new BigDecimal(item_id));
				        	bean1.set("maint_activity_id", new BigDecimal(Integer.parseInt((String)request.getParameter("maint_activity_id"))));
				        	bean1.set("component",  URLDecoder.decode(component[i],"UTF-8"));
				        	bean1.set("description", URLDecoder.decode(description[i],"UTF-8"));

				            if (null != labourCost[i] && !labourCost[i].equalsIgnoreCase("")) {
				            	bean1.set("labor_cost",  BigDecimal.valueOf(Float.parseFloat(labourCost[i])));
				            }else {
				                bean1.set("labor_cost",  new BigDecimal(0));
				             }

				             if (null != cost[i] && !cost[i].equalsIgnoreCase("")) {
				            	  bean1.set("part_cost" ,BigDecimal.valueOf(Float.parseFloat(cost[i])));
				             }else {
				            	  bean1.set("part_cost" ,new BigDecimal(0));
				             }


				             if(new GenericDAO("asset_maintenance_activity_item").insert(con, bean1)){
				            	 success_item++;
				             }
						}
					}
				}else{
					/** handle deleted items
					 *
					 */
					GenericDAO dao1 = new GenericDAO("asset_maintenance_activity_item");
					BasicDynaBean bean1 = dao1.getBean();
					if ((null != ((String)itemid[i])) && (!((String)itemid[i]).equals(""))){
						//item exists in db so we have to delete from there
						if ( dao1.delete(con, "item_id", Integer.parseInt((String)itemid[i]))){
							success_item++;
						}

					}//else
					 //this is a new entry on UI that was chosen to be deleted. No need to do anything in that case

				}
			}
			if (success_main > 0 && (success_item > 0 || component.length-1 == 0)) {
				con.commit();
				FlashScope flash = FlashScope.getScope(request);
				flash.success("Maintenance Activity details updated successfully.");
				ActionRedirect redirect = new ActionRedirect(m.findForward("listRedirect"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				redirect.addParameter("edit", "true");
				return redirect;
			} else {
				con.rollback();
				request.setAttribute("error", "Failed to update schedule details.");
			}
		}
		finally {
			if (con != null) con.close();
		}
		return m.findForward("listRedirect");
	}
}