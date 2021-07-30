package com.insta.hms.master.StoreItemRates;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.StoresItemIssueRateMaster.StoresItemIssueRateMasterDAO;
import com.insta.hms.master.StoresRatePlanMaster.StoresRatePlanDAO;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupRepository;

import freemarker.core.InvalidReferenceException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import flexjson.JSONSerializer;

public class StoreItemRatesAction extends BaseAction {

	static Logger log = LoggerFactory.getLogger(StoresItemIssueRateMasterDAO.class);
	private static final GenericDAO itemGroupTypeDao = new GenericDAO("item_group_type");

	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception{


		StoreItemRatesDAO storeItRtDAO = new StoreItemRatesDAO();
		Map requestParams = new HashMap();
		requestParams.putAll(req.getParameterMap());
		String[] defaultRatePlanIdCast = {"y"};

		String storeRatePlan = req.getParameter("store_rate_plan_id");
		if ( (storeRatePlan == null) || storeRatePlan.equals("")) {
			BasicDynaBean storeRatePlanBean = new StoresRatePlanDAO().getRecord();
			String[] defaultRatePlanId = {storeRatePlanBean != null ? storeRatePlanBean.get("store_rate_plan_id").toString() : "0"};
			requestParams.put("store_rate_plan_id", defaultRatePlanId);
			requestParams.put("store_rate_plan_id@cast", defaultRatePlanIdCast);
		}

		if ( req.getParameter("store_rate_plan_id@cast") == null )
			requestParams.put("store_rate_plan_id@cast", defaultRatePlanIdCast);

		PagedList list = storeItRtDAO.list(requestParams, ConversionUtils.getListingParameter(req.getParameterMap()));

		req.setAttribute("list", list);
		return m.findForward("list");
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception{
		List<String> exprTokens = new ArrayList<String>();
		JSONSerializer js = new JSONSerializer().exclude("class");
		String storeTariff = req.getParameter("store_tariff");
		Integer storetariffId  = null;
		if(null != storeTariff && !"".equals(storeTariff)) {
		  storetariffId = Integer.parseInt(storeTariff);
		} else {
		  storetariffId = new StoreItemRatesDAO().getDefaultStoreRatePlanid();
		}
		exprTokens.add("average_cp");
		exprTokens.add("max_cp");
		exprTokens.add("mrp");
		exprTokens.add("center_id");
		exprTokens.add("store_id");
		exprTokens.add("item_selling_price");
		req.setAttribute("exprTokens", exprTokens);
		req.setAttribute("bean", new GenericDAO("store_item_details").findByKey("medicine_id", Integer.parseInt(req.getParameter("medicine_id"))));
		req.setAttribute("itemRates", new StoreItemRatesDAO().getItemRates(Integer.parseInt(req.getParameter("medicine_id")),storetariffId));
		req.setAttribute("store_tariff",storetariffId);
    req.setAttribute("itemGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(new GenericDAO("item_groups").findAllByKey("status","A"))));
    req.setAttribute("itemGroupTypeList", ConversionUtils.listBeanToListMap(itemGroupTypeDao.findAllByKey("item_group_type_id","TAX")));
    req.setAttribute("taxsubgroup", ConversionUtils.copyListDynaBeansToMap(new StoreItemRatesDAO().getStoreItemSubGroupDetails(Integer.parseInt(req.getParameter("medicine_id")),storetariffId)));
    List <BasicDynaBean> itemSubGroupList = new TaxSubGroupRepository().getItemSubGroup(new java.sql.Date(new java.util.Date().getTime()));
    Iterator<BasicDynaBean> itemSubGroupListIterator = itemSubGroupList.iterator();
    List<BasicDynaBean> validateItemSubGrouList = new ArrayList<BasicDynaBean>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String currentDateStr = sdf.format(new java.util.Date());
    while(itemSubGroupListIterator.hasNext()) {
      BasicDynaBean itenSubGroupbean = itemSubGroupListIterator.next();
      if(itenSubGroupbean.get("validity_end") != null){
        Date endDate = (Date)itenSubGroupbean.get("validity_end");
        
        try {
          if(sdf.parse(currentDateStr).getTime() <= endDate.getTime()) {
            validateItemSubGrouList.add(itenSubGroupbean);
          }
        } catch (ParseException e) {
          continue;
        }
      } else {
        validateItemSubGrouList.add(itenSubGroupbean);
      }
    }
    req.setAttribute("itemSubGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(validateItemSubGrouList)));
		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception{

		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect=new ActionRedirect(req.getContextPath()+"pages/master/StoresMaster/StoreItemRates.do?_method=show");

		StoreItemRatesDAO storeItemRatesDAO = new StoreItemRatesDAO();

		BasicDynaBean storesItemRateBean = new StoreItemRatesDAO().getBean();
		String[] ratePlanIds = req.getParameterValues("store_rate_plan_id");
		boolean success = true;
		Connection con = null;

		Map<String, String[]> paramMap = req.getParameterMap();
		List<String> errors = new ArrayList<String>();
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			StringBuffer invalidExpressionErrorMessage = new StringBuffer();
			invalidExpressionErrorMessage.append("Invalid expression is given : ");
			boolean errorFlag = false;

      storesItemRateBean = new StoreItemRatesDAO().getBean();
      ConversionUtils.copyToDynaBean(paramMap, storesItemRateBean, errors);
      boolean valid = false;
      try {
        valid = isValidExpression(paramMap.get("selling_price_expr")[0]);
      } catch(Exception e) {
        log.info("Invalid Expression", e);
      }
      if(valid) {
        if (!errors.isEmpty()) {
          flash.put("error", "Incorrectly formatted values supplied :  "+paramMap.get("store_rate_plan_name")[0]);
          redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
          return redirect;
        } else {
          Map<String, Object> keys = new HashMap<>();
          keys.put("medicine_id", Integer.parseInt(paramMap.get("medicine_id")[0]));
          keys.put("store_rate_plan_id", (Integer)storesItemRateBean.get("store_rate_plan_id"));

          success &= storeItemRatesDAO.update(con, storesItemRateBean.getMap(),keys) > 0;
          
          if (success) {
            int itemId = Integer.parseInt(paramMap.get("medicine_id")[0]);
            int storeTariffId = Integer.parseInt(paramMap.get("store_tariff")[0]);
            success = saveOrUpdateItemSubGroup(itemId, storeTariffId, con, req);
          }
        }
      } else {
        errorFlag = true;
        invalidExpressionErrorMessage.append(paramMap.get("store_rate_plan_name")[0]);
        invalidExpressionErrorMessage.append(", ");
      }
      
			if(errorFlag){
				flash.error(invalidExpressionErrorMessage.substring(0, invalidExpressionErrorMessage.length()-2));
			}
			
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		redirect = new ActionRedirect(m.findForward("showRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("medicine_id", (paramMap.get("medicine_id"))[0]);
		redirect.addParameter("store_tariff", (paramMap.get("store_tariff"))[0]);
		return redirect;
	}
	
	/*This method is used to validate issue rate expression given in issue rate master  */
	public boolean isValidExpression(String expression)throws Exception {
		boolean valid = false;
		Map results = new HashMap();
		results.put("average_cp", 1);
        results.put("max_cp",1);
        results.put("mrp", 1);
        results.put("center_id", 0);
        results.put("store_id", 0);
        results.put("item_selling_price", 0);
        StringWriter writer = new StringWriter();
        String expr = "<#setting number_format=\"##.##\">\n" + expression;
        try{
        	Template expressionTemplate = new Template("expression", new StringReader(expr),new Configuration());
        	expressionTemplate.process(results, writer);
        }catch (InvalidReferenceException ine) {
        	log.error("", ine);
        	return false;
        }catch (TemplateException e) {
        	log.error("", e);
        	return false;
        }catch (ArithmeticException e) {
        	log.error("", e);
        	return false;
        }catch(Exception e){
        	log.error("", e);
        	return false;
        }
        //it check non integer nos
        valid = !writer.toString().contains("[^.\\d]");

        try{
        	if(!writer.toString().trim().isEmpty()){
        		BigDecimal validNumber = new BigDecimal(writer.toString().trim());
        	}
        }catch(NumberFormatException ne){
        	log.error("", ne);
        	valid = false;
        }

        return valid ;
	}
	
	private boolean saveOrUpdateItemSubGroup(int itemId, int storetariffId, Connection con,HttpServletRequest request)
 throws SQLException, IOException {
    Map params = request.getParameterMap();
    List errors = new ArrayList();

    boolean flag = true;
    String[] itemSubgroupId = request.getParameterValues("item_subgroup_id");
    String[] delete = request.getParameterValues("deleted");

    if (errors.isEmpty()) {
      if (itemSubgroupId != null && itemSubgroupId.length > 0 && !itemSubgroupId[0].equals("")) {
        GenericDAO itemsubgroupdao = new GenericDAO("store_tariff_item_sub_groups");
        BasicDynaBean itemsubgroupbean = itemsubgroupdao.getBean();
        ConversionUtils.copyToDynaBean(params, itemsubgroupbean, errors);
        Map<String, Object> keys = new HashMap<>();
        keys.put("item_id", itemId);
        keys.put("store_rate_plan_id", storetariffId);
        List records = itemsubgroupdao.listAll(null, keys, null);
        if (!records.isEmpty()) {
          LinkedHashMap<String, Object> delKeys = new LinkedHashMap<String, Object>();
          delKeys.put("item_id", itemId);
          delKeys.put("store_rate_plan_id",storetariffId);
          flag = itemsubgroupdao.delete(con, delKeys);
        }

        for (int i = 0; i < itemSubgroupId.length; i++) {
          if (itemSubgroupId[i] != null && !itemSubgroupId[i].isEmpty()) {
            if (delete[i].equalsIgnoreCase("false")) {
              itemsubgroupbean.set("item_id", itemId);
              itemsubgroupbean.set("item_subgroup_id", Integer.parseInt(itemSubgroupId[i]));
              itemsubgroupbean.set("store_rate_plan_id", storetariffId);
              flag = itemsubgroupdao.insert(con, itemsubgroupbean);
            }
          }
        }
      }
    }
    return flag;

  }

}
