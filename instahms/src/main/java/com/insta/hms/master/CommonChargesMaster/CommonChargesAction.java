package com.insta.hms.master.CommonChargesMaster;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
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
import com.insta.hms.common.AbstractDataHandlerAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.csvutils.TableDataHandler;
import com.insta.hms.master.ServiceSubGroup.ServiceSubGroupDAO;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupRepository;

import flexjson.JSONSerializer;

public class CommonChargesAction extends AbstractDataHandlerAction {
	static Logger logger = LoggerFactory.getLogger("CommonChargesAction");
	
	private static final GenericDAO chargeHeadConstantsDAO = new GenericDAO("chargehead_constants");
    private static final GenericDAO itemGroupTypeDAO = new GenericDAO("item_group_type");
    private static final GenericDAO itemGroupsDAO = new GenericDAO("item_groups");
    private static final GenericDAO commonItemSubGroupsDAO = new GenericDAO("common_item_sub_groups");
    
	public ActionForward list(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse responce)throws Exception {

		Map listingParams = ConversionUtils.getListingParameter(request.getParameterMap());
		PagedList commoncharges = new CommonChargesDAO().getAllCommonCharges(
				request.getParameterMap(), listingParams);
		request.setAttribute("pagedList", commoncharges);
		return mapping.findForward("list");
	}

	public ActionForward add(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse responce)
			throws IOException, ServletException, Exception {
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("allCommonCharges", new JSONSerializer().exclude("class").
				serialize(ConversionUtils.listBeanToListMap(new CommonChargesDAO().listAll())));
		request.setAttribute("chargeHeadsJSON", new JSONSerializer().exclude("class").
				serialize(ConversionUtils.listBeanToListMap(chargeHeadConstantsDAO.listAll())));
		request.setAttribute("serviceSubGroupsList", js.serialize(ServiceSubGroupDAO.getAllActiveServiceSubGroups()));
		request.setAttribute("itemGroupTypeList", ConversionUtils.listBeanToListMap(itemGroupTypeDAO.findAllByKey("item_group_type_id","TAX")));
		request.setAttribute("itemGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(itemGroupsDAO.findAllByKey("status","A"))));
		//request.setAttribute("itemSubGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(new GenericDAO("item_sub_groups").findAllByKey("status","A"))));
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
		request.setAttribute("itemSubGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(validateItemSubGrouList)));
		return mapping.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = null;

		CommonChargesDAO dao = new CommonChargesDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(m.findForward("addRedirect"));

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			if (errors.isEmpty()) {

				boolean success = dao.insert(con, bean);
				if(success) {
					String charge_name = (String) bean.get("charge_name");
					success = saveItemSubGroup(charge_name,con,req);
				}
				if (success) {
				  String charge_name = (String) bean.get("charge_name");
				  success = saveOrUpdateInsuranceCategory(charge_name, con, req);
				}
				if (success) {
					con.commit();
					redirect = new ActionRedirect(m.findForward("showRedirect"));
					redirect.addParameter("charge_name", bean.get("charge_name"));
				} else {
					con.rollback();
					flash.error("Failed to add  Charges..");
				}
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
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
		BasicDynaBean bean = new CommonChargesDAO().findByKey("charge_name", req.getParameter("charge_name"));
		String groupId = new ServiceSubGroupDAO().findByKey("service_sub_group_id", bean.get("service_sub_group_id")).get("service_group_id").toString();
		req.setAttribute("groupId", groupId);
		req.setAttribute("bean", bean);
		List<BasicDynaBean> activeInsurance = CommonChargesDAO.getActiveInsuranceCategories(req.getParameter("charge_name"));
		StringBuilder activeInsuranceCategories = new StringBuilder();
		for (BasicDynaBean activeInsuranceCategory : activeInsurance) {
		  activeInsuranceCategories.append(activeInsuranceCategory.get("insurance_category_id"));
		  activeInsuranceCategories.append(",");
		}
		req.setAttribute("insurance_categories", activeInsuranceCategories.toString());
		req.setAttribute("allCommonCharges", new JSONSerializer().exclude(
				"class").serialize(
				ConversionUtils.listBeanToListMap(new CommonChargesDAO()
						.listAll())));
		req.setAttribute("chargeHeadsJSON", new JSONSerializer().exclude("class").
				serialize(ConversionUtils.listBeanToListMap(chargeHeadConstantsDAO.listAll())));
		req.setAttribute("serviceSubGroupsList", js.serialize(ServiceSubGroupDAO.getAllActiveServiceSubGroups()));
		req.setAttribute("taxsubgroup", ConversionUtils.copyListDynaBeansToMap(CommonChargesDAO.getOpItemSubGroupDetails(req.getParameter("charge_name"))));
		req.setAttribute("itemGroupTypeList", ConversionUtils.listBeanToListMap(itemGroupTypeDAO.findAllByKey("item_group_type_id","TAX")));
		req.setAttribute("itemGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(itemGroupsDAO.findAllByKey("status","A"))));
		//req.setAttribute("itemSubGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(new GenericDAO("item_sub_groups").findAllByKey("status","A"))));
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
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = null;
		Map params = req.getParameterMap();
		List errors = new ArrayList();

		CommonChargesDAO dao = new CommonChargesDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		Map<String, String> keys = new HashMap<String, String>();
		keys.put("charge_name", req.getParameter("dbCharge_name"));

		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			if (errors.isEmpty()) {
				int success = dao.update(con, bean.getMap(), keys);
				if(success > 0) {
					String charge_name = (String) bean.get("charge_name");
					success = updateItemSubGroup(charge_name,con,req);
				}
				if (success > 0) {
					String charge_name = (String) bean.get("charge_name");
					boolean result = saveOrUpdateInsuranceCategory(charge_name, con, req);
					if (!result) {
						success = 0;
					}
				}
				if (success > 0) {
					con.commit();
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					redirect.addParameter("charge_name", bean.get("charge_name"));
				} else {
					con.rollback();
					flash.error("Failed to update Charge master details..");
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					redirect.addParameter("charge_name", bean.get("charge_name"));
				}
			} else {
				flash.error("Incorrectly formatted values supplied");
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			}
			redirect.addParameter("charge_name", req.getParameter("charge_name"));
			redirect.addParameter("charge_type", req.getParameter("charge_type"));
		}finally {
			DataBaseUtil.closeConnections(con, null);
		}
		return redirect;
	}

	public ActionForward groupUpdate(ActionMapping m, ActionForm af,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		CommonChargesDAO dao = new CommonChargesDAO();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		Double variancePer = 0.0;
		Double varianceValue = 0.0;
		String[] editChargeValues = request
				.getParameterValues("_editChargeDetails");
		String varianceType = request.getParameter("_varianceType");

		if (!request.getParameter("_varianceBy").equals("")
				&& request.getParameter("_varianceBy") != null) {
			variancePer = Double.valueOf((request.getParameter("_varianceBy")));
		}

		if (!request.getParameter("_varianceValue").equals("")
				&& request.getParameter("_varianceValue") != null) {
			varianceValue = Double.valueOf((request
					.getParameter("_varianceValue")));
		}
		String allRecords = request.getParameter("_allRecords");
		boolean success = false;
		boolean useValue = true;
		boolean updateallRecords = false;

		if (allRecords != null && allRecords.equals("updateAllRecords")) {
			updateallRecords = true;
		}

		if (varianceValue == 0.0) {
			useValue = false;
		}

		if (!updateallRecords) {
			ArrayList commonChargeToUpdate = new ArrayList(Arrays
					.asList(editChargeValues));
			success = dao.groupUpdate(con, commonChargeToUpdate, variancePer,
					varianceValue, allRecords, varianceType, useValue);
		} else {
			success = dao.updateAllRecords(con, variancePer, varianceValue,
					varianceType, useValue);
		}
		FlashScope fScope = FlashScope.getScope(request);
		if (success) {
			con.commit();
			DataBaseUtil.closeConnections(con, null);
			ActionRedirect redirect = new ActionRedirect(m
					.findForward("listRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY, fScope.key());
			return redirect;
		} else {
			con.rollback();
			DataBaseUtil.closeConnections(con, null);
			request.setAttribute("method", "list");
			fScope.error("Failed to update other charges..");
		}
		return m.findForward("list");
	}

	private static TableDataHandler commonChargeHandler = null;

	protected TableDataHandler getDataHandler() {
		if (commonChargeHandler == null) {
			commonChargeHandler = new TableDataHandler(
					"common_charges_master",		// table name
					new String[]{"charge_name"},	// keys
					new String[]{"charge_group", "charge_type", "service_sub_group_id",
						"othercharge_code", "charge", "status","insurance_category_id",
						"allow_rate_increase", "allow_rate_decrease"
					},
					new String[][]{	/* masters */
						// our field        ref table        ref table id field  ref table name field
						{"charge_group", "chargegroup_constants", "chargegroup_id", "chargegroup_name"},
						{"charge_type", "chargehead_constants", "chargehead_id", "chargehead_name"},
						{"service_sub_group_id", "service_sub_groups",
							"service_sub_group_id", "service_sub_group_name"},
						{"insurance_category_id", "item_insurance_categories",
							"insurance_category_id", "insurance_category_name"},
					},
					null
			);
		}
		commonChargeHandler.setIdValAsString(true);
		return commonChargeHandler;
	}
	private boolean saveItemSubGroup(String charge_name, Connection con,HttpServletRequest request)
			throws SQLException, IOException{
			Map params = request.getParameterMap();
			List errors = new ArrayList();
			
			boolean flag = true;
			String [] itemSubgroupId = request.getParameterValues("item_subgroup_id");
			String [] delete = request.getParameterValues("deleted");
			
			if (errors.isEmpty()) {
				if(itemSubgroupId != null && itemSubgroupId.length > 0 && !itemSubgroupId[0].equals("")){
					BasicDynaBean itemsubgroupbean = commonItemSubGroupsDAO.getBean();
					ConversionUtils.copyToDynaBean(params, itemsubgroupbean, errors);
					List records = commonItemSubGroupsDAO.findAllByKey("charge_name", charge_name);
					if (records.size() > 0)
						flag = commonItemSubGroupsDAO.delete(con, "charge_name", charge_name);
					
					for(int i=0; i<itemSubgroupId.length; i++){
						if(itemSubgroupId[i] != null && !itemSubgroupId[i].isEmpty()) {
							if (delete[i].equalsIgnoreCase("false")) {
								itemsubgroupbean.set("charge_name", charge_name);
								itemsubgroupbean.set("item_subgroup_id", Integer.parseInt(itemSubgroupId[i]));
								flag = commonItemSubGroupsDAO.insert(con, itemsubgroupbean);
							}
						}
					}
				}	
			}
			return flag;

	}
	
	private int updateItemSubGroup(String charge_name, Connection con,HttpServletRequest request)
			throws SQLException, IOException{
			Map params = request.getParameterMap();
			List errors = new ArrayList();
			
			int flag = 1;
			String [] itemSubgroupId = request.getParameterValues("item_subgroup_id");
			String [] delete = request.getParameterValues("deleted");
			
			if (errors.isEmpty()) {
				if(itemSubgroupId != null && itemSubgroupId.length > 0 && !itemSubgroupId[0].equals("")){
					BasicDynaBean itemsubgroupbean = commonItemSubGroupsDAO.getBean();
					ConversionUtils.copyToDynaBean(params, itemsubgroupbean, errors);
					List records = commonItemSubGroupsDAO.findAllByKey("charge_name", charge_name);
					if (records.size() > 0)
						flag = (commonItemSubGroupsDAO.delete(con, "charge_name", charge_name)) ? 1: 0;
					
					for(int i=0; i<itemSubgroupId.length; i++){
						if(itemSubgroupId[i] != null && !itemSubgroupId[i].isEmpty()) {
							if (delete[i].equalsIgnoreCase("false")) {
								itemsubgroupbean.set("charge_name", charge_name);
								itemsubgroupbean.set("item_subgroup_id", Integer.parseInt(itemSubgroupId[i]));
								flag = commonItemSubGroupsDAO.insert(con, itemsubgroupbean) ? 1:0;
							}
						}
					}
					
				}	
			}
			return flag;

		}

  private boolean saveOrUpdateInsuranceCategory(String chargeName,
      Connection con, HttpServletRequest request) throws SQLException, IOException {
      boolean flag = true;
      String[] insuranceCategories = request.getParameterValues("insurance_category_id");
      if (insuranceCategories != null && insuranceCategories.length > 0
        && !insuranceCategories[0].equals("")) {
        GenericDAO insuranceCategoryDAO =
          new GenericDAO("common_charges_insurance_category_mapping");
        BasicDynaBean insuranceCategoryBean = insuranceCategoryDAO.getBean();
        List<BasicDynaBean> records = insuranceCategoryDAO.findAllByKey("charge_name", chargeName);
        if (records != null && records.size() > 0) {
          flag = insuranceCategoryDAO.delete(con,"charge_name", chargeName);
        }
        for (String insuranceCategory :  insuranceCategories) {
          insuranceCategoryBean.set("charge_name", chargeName);
          insuranceCategoryBean.set("insurance_category_id", Integer.parseInt(insuranceCategory));
          flag = insuranceCategoryDAO.insert(con,insuranceCategoryBean);
        }
      }
      return flag;
  }

}
