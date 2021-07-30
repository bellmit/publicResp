package com.insta.hms.master.DiscountPlanMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.MigratedTo;
import com.insta.hms.master.MasterAction;
import com.insta.hms.master.MasterDAO;
import com.insta.hms.master.Accounting.ChargeHeadsDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.mdm.discountplans.DiscountPlanController;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

@MigratedTo(DiscountPlanController.class)
public class DiscountPlanMasterAction extends MasterAction {

    static Logger logger = LoggerFactory.getLogger(DiscountPlanMasterAction.class);

    DiscountPlanMasterDAO discountDao = new DiscountPlanMasterDAO();
    GenericDAO discountDetailsDao = new GenericDAO("discount_plan_details");
    
    private static final GenericDAO masterTimestampDAO = new GenericDAO("master_timestamp");
    private static final GenericDAO packOrgDetailsDAO = new GenericDAO("pack_org_details");
    
	@Override
	public MasterDAO getMasterDao() {
		return discountDao;
	}

	@Override
	public Map<String, List<BasicDynaBean>> getLookupLists() throws SQLException {
		Map<String, List<BasicDynaBean>> map = new HashMap<String, List<BasicDynaBean>>();
		List<BasicDynaBean> discountPlans = discountDao.listAll("discount_plan_name");
		map.put("discount_plan_name", discountPlans);
		return map;
	}

    @MigratedTo(method = "list", value = DiscountPlanController.class)
    public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		
    	Map map= req.getParameterMap();
		JSONSerializer js = new JSONSerializer().exclude("class");
		PagedList pagedList = discountDao.getDiscountPlanMainDetails(map,ConversionUtils.getListingParameter(req.getParameterMap()));
		req.setAttribute("pagedList", pagedList);
		req.setAttribute("centers", new CenterMasterDAO().getAllCentersAndSuperCenterAsFirst());
		getAutoLookupLists(req);
		
		return m.findForward("list");
	}
    
    public ActionForward add(ActionMapping m,ActionForm f,
			HttpServletRequest req,HttpServletResponse resp) throws Exception{
    	JSONSerializer js = new JSONSerializer().exclude("class");
    
        req.setAttribute("insuranceCategoryList", js
            .serialize(ConversionUtils.listBeanToListMap(discountDao.getInsuranceCategoryList())));
        req.setAttribute("chargeHeadList", js.serialize(
            ConversionUtils.listBeanToListMap(ChargeHeadsDAO.getDiscountApplicableChargeHead())));
        req.setAttribute("discountPlanbean",
            js.serialize(ConversionUtils.listBeanToListMap(discountDao.listAll())));
        req.setAttribute("discountPlanDetailsbean", js.serialize(
            ConversionUtils.listBeanToListMap(discountDao.getDiscountPlanDetailsList("0"))));
        BasicDynaBean mst = (BasicDynaBean) masterTimestampDAO.getRecord();
        req.setAttribute("masterTimeStamp", mst.get("master_count"));
        BasicDynaBean packageOrgDetBean = packOrgDetailsDAO.getBean();
        req.setAttribute("org_id", packageOrgDetBean.get("org_id"));
    	
    	return m.findForward("addshow");
	}
    
    public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
    	
    	JSONSerializer js = new JSONSerializer().exclude("class");
    	
    	req.setAttribute("insuranceCategoryList", js.serialize(ConversionUtils.listBeanToListMap(discountDao.getInsuranceCategoryList())));
    	req.setAttribute("chargeHeadList", js.serialize(ConversionUtils.listBeanToListMap(ChargeHeadsDAO.getDiscountApplicableChargeHead())));
		String discount_plan_id = req.getParameter("discount_plan_id");
		
		if(null != discount_plan_id && !discount_plan_id.isEmpty()){
    	BasicDynaBean bean = discountDao.findByKey("discount_plan_id", Integer.parseInt(discount_plan_id));
		req.setAttribute("bean", bean);
		req.setAttribute("discountPlanbean", js.serialize(ConversionUtils.listBeanToListMap(discountDao.getDiscountPlanList(req.getParameter("discount_plan_name")))));
		req.setAttribute("discountPlanDetailsbean", js.serialize(ConversionUtils.listBeanToListMap(discountDao.getDiscountPlanDetailsList(discount_plan_id))));
		}
		BasicDynaBean mst = (BasicDynaBean) masterTimestampDAO.getRecord();
		req.setAttribute("masterTimeStamp", mst.get("master_count"));
		BasicDynaBean packageOrgDetBean = packOrgDetailsDAO.getBean();	
		req.setAttribute("org_id", packageOrgDetBean.get("org_id"));
		
		return m.findForward("addshow");
	}
	
	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		
	    List errors = new ArrayList();
		BasicDynaBean discountBean = discountDao.getBean();
		ConversionUtils.copyToDynaBean(req.getParameterMap(), discountBean, errors,true);
		
		boolean success = false;
		Connection con = null;
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		FlashScope flash = FlashScope.getScope(req);
		
		String[] applicable_type = req.getParameterValues("applicable_type");
		String[] deleted = req.getParameterValues("deleted");
		
		BasicDynaBean discountDetailsBean = discountDetailsDao.getBean();
		ConversionUtils.copyToDynaBean(req.getParameterMap(), discountDetailsBean, errors,true);
		
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			int plan_id = discountDao.getNextSequence();
			discountBean.set("discount_plan_id", plan_id);
			success = discountDao.insert(con, discountBean);	
			if(success){
				int typeNum =0;
				if(null != applicable_type){
					for(int i=0; i<applicable_type.length; i++) {
						if(deleted[i].equals("false")) {
							
							insertRow(con,req,discountDetailsBean,plan_id,i,typeNum);
							if(applicable_type[i].equalsIgnoreCase("I")){
								typeNum++;
							}	
						}
					}
			    }
			}	
			
			if(success){
				flash.info("Discount plan details added successfully...");
				redirect.addParameter("discount_plan_id", discountBean.get("discount_plan_id"));
			}
			else{
				flash.error("Failed to add discount plan details...");
			}
			
		} catch(Exception e){
			flash.error("Failed to add discount plan details...");
		}
		finally {
			DataBaseUtil.commitClose(con,success);
		}
		
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("discount_plan_name", req.getParameter("discount_plan_name"));
		return redirect;
	}
	
	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		
		
		String[] applicable_type = req.getParameterValues("applicable_type");
		String[] applicable_to_id = req.getParameterValues("applicable_to_id");
		String[] applicable_to_id_value = req.getParameterValues("applicable_to_id_value");
		String[] applicable_to_id_subgroup = req.getParameterValues("applicable_to_id_subgroup");
		String[] discount_value = req.getParameterValues("discount_value");
		String[] discount_type = req.getParameterValues("discount_type");
		String[] priority = req.getParameterValues("priority");
		String[] discount_plan_detail_id = req.getParameterValues("discount_plan_detail_id");
		String[] deleted = req.getParameterValues("deleted");
		
		List errors = new ArrayList();
		
		BasicDynaBean discountDetailsBean = discountDetailsDao.getBean();
		ConversionUtils.copyToDynaBean(req.getParameterMap(), discountDetailsBean, errors,true);
		
		int success = 0;
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map params = req.getParameterMap();
		
			BasicDynaBean bean = discountDao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);
			String str = req.getParameter("discount_plan_id");
			int key = Integer.parseInt(str);
			Map<String, Integer> keys = new HashMap<String, Integer>();
			keys.put("discount_plan_id", key);
			
			if (errors.isEmpty()) {
				 success = discountDao.update(con, bean.getMap(), keys);
				 
				Map<String, Integer> planDetailkey = new HashMap<String, Integer>();
				Map discountDetailsMap = new HashMap();
				int typeNum =0;
				if(null != applicable_type){
					for(int i=0; i<applicable_type.length; i++) {
						if(deleted[i].equals("false")) {
							
							if(null != discount_plan_detail_id && i < discount_plan_detail_id.length){
							
								planDetailkey.put("discount_plan_detail_id", Integer.parseInt(discount_plan_detail_id[i]));
							
								String applicable_type_value = applicable_type[i];
								discountDetailsMap.put("applicable_type", applicable_type_value);
								//discountDetailsMap.put("applicable_to_id", applicable_to_id[i]);
								discountDetailsMap.put("discount_value", BigDecimal.valueOf(Double.parseDouble(discount_value[i].isEmpty()?"0.00" :discount_value[i])));
								
							if(applicable_type_value.equalsIgnoreCase("I")){
								discountDetailsMap.put("discount_type", discount_type[typeNum]);
								discountDetailsMap.put("applicable_to_id", applicable_to_id_value[i]);
								discountDetailsMap.put("applicable_to_id_subgroup", applicable_to_id_subgroup[i]);
								typeNum++;
							}else{
								discountDetailsMap.put("discount_type", "P");
								discountDetailsMap.put("applicable_to_id", applicable_to_id[i]);
								discountDetailsMap.put("applicable_to_id_subgroup", "");
							}
							
							    discountDetailsMap.put("priority", Integer.parseInt(priority[i]));
							    success = discountDetailsDao.update(con, discountDetailsMap, planDetailkey);
							
							}else{
								if(deleted[i].equals("false")) {
									
									insertRow(con,req,discountDetailsBean,key,i,typeNum);
									if(applicable_type[i].equalsIgnoreCase("I")){
										typeNum++;
									}
								}
							}
							 	
						}else{
							
							if(null != discount_plan_detail_id && i < discount_plan_detail_id.length){
								discountDetailsDao.delete(con,"discount_plan_detail_id",Integer.parseInt(discount_plan_detail_id[i]));
							}
						}
					}
					
				}
			
				if (success > 0) {
					con.commit();
					flash.info("Discount plan details updated successfully..");
				} else {
					con.rollback();
					flash.error("Failed to update discount plan details..");
				}
				
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
			
			redirect.addParameter("discount_plan_id", req.getParameter("discount_plan_id"));
			redirect.addParameter("discount_plan_name", req.getParameter("discount_plan_name"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
	    }finally {
			DataBaseUtil.closeConnections(con, null);
		  }
	}
	


  public boolean insertRow(Connection con,HttpServletRequest req,BasicDynaBean bean,int key,int index,int typeNum)
		throws IOException, ServletException, Exception {
	
	  try{
		String[] applicable_type = req.getParameterValues("applicable_type");
		String[] applicable_to_id = req.getParameterValues("applicable_to_id");
		String[] discount_value = req.getParameterValues("discount_value");
		String[] discount_type = req.getParameterValues("discount_type");
		String[] priority = req.getParameterValues("priority");
		String[] discount_plan_detail_id = req.getParameterValues("discount_plan_detail_id");
		String[] deleted = req.getParameterValues("deleted");
		String[] applicable_to_id_value = req.getParameterValues("applicable_to_id_value");
		String[] applicable_to_id_subgroup = req.getParameterValues("applicable_to_id_subgroup");
		
		bean.set("discount_plan_detail_id", discountDetailsDao.getNextSequence());
		bean.set("discount_plan_id", key);
		String applicable_type_value = applicable_type[index];
		bean.set("applicable_type", applicable_type_value);
		bean.set("discount_value", BigDecimal.valueOf(Double.parseDouble(discount_value[index].isEmpty()?"0.00" :discount_value[index])));
		
		if(applicable_type_value.equalsIgnoreCase("I")){
			bean.set("discount_type", discount_type[typeNum]);
			bean.set("applicable_to_id", applicable_to_id_value[index]);
			bean.set("applicable_to_id_subgroup", applicable_to_id_subgroup[index]);
		}else{
			bean.set("discount_type", "P");
			bean.set("applicable_to_id", applicable_to_id[index]);
			bean.set("applicable_to_id_subgroup", "");
		}
		bean.set("priority", Integer.parseInt(priority[index]));
		
		return discountDetailsDao.insert(con, bean);
		
	  }finally{
		  DataBaseUtil.closeConnections(null, null);
	  }
	
   }		
  
}


