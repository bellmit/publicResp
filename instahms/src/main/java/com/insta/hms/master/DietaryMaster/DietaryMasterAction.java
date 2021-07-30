package com.insta.hms.master.DietaryMaster;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.ipservices.DashBoardDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericDocumentTemplate.GenericDocumentTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.ServiceSubGroup.ServiceSubGroupDAO;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupRepository;
import com.lowagie.text.DocumentException;

import flexjson.JSONSerializer;
import freemarker.template.TemplateException;

public class DietaryMasterAction  extends DispatchAction{

    static Logger logger = LoggerFactory.getLogger(DietaryMasterAction.class);
    private static final GenericDAO patientDietPrescriptions =
        new GenericDAO("patient_diet_prescriptions");
    private static final GenericDAO itemGroupTypeDAO = new GenericDAO("item_group_type");
    private static final GenericDAO itemGroupsDAO = new GenericDAO("item_groups");

	public ActionForward list(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		DietaryMasterDAO dao = new DietaryMasterDAO();
		ArrayList<String> bedTypes = BedMasterDAO.getUnionOfBedTypes();
		Map listingParams = ConversionUtils.getListingParameter(request.getParameterMap());
		String searchName = request.getParameter("searchName");
		String dietCat = request.getParameter("dietCategory");
		List statusList = ConversionUtils.getParamAsList(request.getParameterMap(),"searchStatus");
		String orgID = request.getParameter("org_id");
		if (orgID == null || orgID.equals("")){
			orgID = "ORG0001";
		}
		String serviceSubGroupId = request.getParameter("service_sub_group_id");
		PagedList pagedList = dao.getAllDietary(listingParams, searchName,dietCat, statusList,orgID,request,serviceSubGroupId);
		List mainlist = new ArrayList();
			for (int i=0 ; i<pagedList.getDtoList().size();i++){
				BasicDynaBean bean = (BasicDynaBean)pagedList.getDtoList().get(i);
					List basicBean = dao.getChargeForMeal(Integer.parseInt(bean.get("diet_id").toString()), orgID);
					mainlist.add(ConversionUtils.listBeanToMapMapBean(basicBean,"meal_name","bed_type"));
			}

		String mealNameAndCharges  = dao.getAllMeal();
		List categoryList = dao.getDietCategory();
		request.setAttribute("mealNameAndCharges", mealNameAndCharges);
		request.setAttribute("categoryList", categoryList);
		request.setAttribute("pagedList", pagedList);
		request.setAttribute("bedTypes", bedTypes);
		request.setAttribute("bedTypeAndChargeList",mainlist);
		request.setAttribute("org_id", orgID);
		request.setAttribute("filterclosed", true);
		request.setAttribute("screentype", "searchScreen");
		return mapping.findForward("list");
	}

	public ActionForward add(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

      JSONSerializer js = new JSONSerializer().exclude("class");
      DietaryMasterDAO dao = new DietaryMasterDAO();
      ArrayList dietList = dao.getAllDiet();
      JSONSerializer jSerializer = new JSONSerializer().exclude("class");
      request.setAttribute("allMealList", dao.getAllMeal());
      request.setAttribute("dietList", jSerializer.serialize(dietList));
      request.setAttribute("org_id", request.getParameter("org_id"));
      request.setAttribute("serviceSubGroupsList",
          js.serialize(ServiceSubGroupDAO.getAllActiveServiceSubGroups()));
      request.setAttribute("itemGroupTypeList", ConversionUtils
          .listBeanToListMap(itemGroupTypeDAO.findAllByKey("item_group_type_id", "TAX")));
      request.setAttribute("itemGroupListJson", js.serialize(ConversionUtils
          .listBeanToListMap(itemGroupsDAO.findAllByKey("status", "A"))));
      // request.setAttribute("itemSubGroupListJson",
      // js.serialize(ConversionUtils.listBeanToListMap(new
      // GenericDAO("item_sub_groups").findAllByKey("status","A"))));
      List<BasicDynaBean> itemSubGroupList = new TaxSubGroupRepository()
          .getItemSubGroup(new java.sql.Date(new java.util.Date().getTime()));
      Iterator<BasicDynaBean> itemSubGroupListIterator = itemSubGroupList.iterator();
      List<BasicDynaBean> validateItemSubGrouList = new ArrayList<BasicDynaBean>();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      String currentDateStr = sdf.format(new java.util.Date());
      while (itemSubGroupListIterator.hasNext()) {
        BasicDynaBean itenSubGroupbean = itemSubGroupListIterator.next();
        if (itenSubGroupbean.get("validity_end") != null) {
          Date endDate = (Date) itenSubGroupbean.get("validity_end");

          try {
            if (sdf.parse(currentDateStr).getTime() <= endDate.getTime()) {
              validateItemSubGrouList.add(itenSubGroupbean);
            }
          } catch (ParseException e) {
            continue;
          }
        } else {
          validateItemSubGrouList.add(itenSubGroupbean);
        }
      }
      request.setAttribute("itemSubGroupListJson",
          js.serialize(ConversionUtils.listBeanToListMap(validateItemSubGrouList)));
      return mapping.findForward("addshow");
	}

	public ActionForward create(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response) throws Exception{


		Map parameterMap = request.getParameterMap();
		List dieterrorFields = new ArrayList();
		List constituentErrors = new ArrayList();
		String orgID = request.getParameter("org_id");
		FlashScope flashScope = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
		Connection con = null;

		DietaryMasterDAO dietaryDAO = new DietaryMasterDAO();
		ConstituentMasterDAO constituentDAO  = new ConstituentMasterDAO();
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			BasicDynaBean bean = dietaryDAO.getBean();
			ConversionUtils.copyToDynaBean(parameterMap, bean, dieterrorFields);


			int totalRecords = Integer.parseInt(request.getParameter("recordLength"));
			List<BasicDynaBean> constituentList = new ArrayList<BasicDynaBean>();

			int id = dietaryDAO.getNextSequence();


			if (dieterrorFields.isEmpty() && constituentErrors.isEmpty()){

				BasicDynaBean exists = dietaryDAO.findByKey("meal_name", bean.get("meal_name"));

				if (exists == null){
					bean.set("diet_id", id);

					if (request.getParameter("serviceTax") == null || request.getParameter("serviceTax").isEmpty()){
						bean.set("service_tax", BigDecimal.ZERO);
					}else {
						bean.set("service_tax", new BigDecimal(request.getParameter("serviceTax")));
					}


					boolean success = dietaryDAO.insert(con, bean);
					
					if(success) {
						int dietId = (Integer) bean.get("diet_id");
						success = saveOrUpdateItemSubGroup(dietId,con,request);
					}

					if(success) {
						int dietId = (Integer) bean.get("diet_id");
						success = saveOrUpdateInsuranceCategory(dietId,con,request);
					}

					if (success){
						for (int i=1;i<=totalRecords;i++) {
							if (request.getParameter("constituent_name"+i) != null && !request.getParameter("constituent_name"+i).equals("")){
								BasicDynaBean constituentBean = constituentDAO.getBean();
								constituentBean.set("diet_id", id);
								constituentBean.set("constituent_name",request.getParameter("constituent_name"+i) );
								if ( request.getParameter("quantity"+i) != null  && !request.getParameter("quantity"+i).equals("")){
									constituentBean.set("quantity", new BigDecimal(request.getParameter("quantity"+i)));
								}else{
									constituentBean.set("quantity",BigDecimal.ZERO) ;
								}
								constituentBean.set("units", request.getParameter("units"+i));
								if (!request.getParameter("calorific_value"+i).equals("")){
									constituentBean.set("calorific_value", new BigDecimal(request.getParameter("calorific_value"+i)));
								}else {
									constituentBean.set("calorific_value", BigDecimal.ZERO);
								}
								if (request.getParameter("delete"+i).equals("N")){
									constituentList.add(constituentBean);
								}
							}
						}
						success = constituentDAO.insertAll(con, constituentList);
						if(success){
							success = dietaryDAO.insertIntoDietCharges(con,id, orgID);
							if (success){
								con.commit();
								flashScope.success("New diet details is added successfully....");
								redirect = new ActionRedirect(mapping.findForward("showRedirect"));
								redirect.addParameter("diet_id", bean.get("diet_id"));
								redirect.addParameter("org_id", orgID);
								redirect.addParameter(FlashScope.FLASH_KEY, flashScope.key());
								return redirect;

							}else{
								con.rollback();
								flashScope.error("Error in adding diet and orgnization details....");
							}
						}else{
							con.rollback();
							flashScope.error("Error in adding constituents....");
						}
					}else{
						con.rollback();
						flashScope.error("Fail to add the new diet details....");
					}
				}else{
					flashScope.error("Duplicate values are not allowed....");
				}

			}else{
				flashScope.error("Incorrectly formated values are supplied....");
			}
			redirect.addParameter("org_id", orgID);
			redirect.addParameter(FlashScope.FLASH_KEY, flashScope.key());
			return redirect;

		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public ActionForward show (ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		    JSONSerializer js = new JSONSerializer().exclude("class");
			int dietId = 0;
			if (request.getParameter("diet_id") != null && !request.getParameter("diet_id").equals(""))
				dietId = Integer.parseInt(request.getParameter("diet_id"));

			DietaryMasterDAO dietaryDAO = new DietaryMasterDAO();
			BasicDynaBean dietaryBean = dietaryDAO.findByKey("diet_id", dietId);
			String groupId = new ServiceSubGroupDAO().findByKey("service_sub_group_id", dietaryBean.get("service_sub_group_id")).get("service_group_id").toString();
			request.setAttribute("groupId", groupId);

			ConstituentMasterDAO dao = new ConstituentMasterDAO();
			ArrayList constuientList =  dao.getConstiuentForDiet(dietId);
			request.setAttribute("constuientList", constuientList);
			request.setAttribute("bean", dietaryBean);
			request.setAttribute("org_id", request.getParameter("org_id"));
			request.setAttribute("allMealList", dietaryDAO.getAllMeal());
			request.setAttribute("recordLength", constuientList.size());
			List<BasicDynaBean> activeInsurance = dietaryDAO.getActiveInsuranceCategories(dietId);
			StringBuilder activeInsuranceCategories = new StringBuilder();
			for (BasicDynaBean activeInsuranceCategory : activeInsurance) {
			  activeInsuranceCategories.append(activeInsuranceCategory.get("insurance_category_id"));
			  activeInsuranceCategories.append(",");
			}
			request.setAttribute("insurance_categories", activeInsuranceCategories.toString());
			request.setAttribute("serviceSubGroupsList", js.serialize(ServiceSubGroupDAO.getAllActiveServiceSubGroups()));
			request.setAttribute("taxsubgroup", ConversionUtils.copyListDynaBeansToMap(dietaryDAO.getDietaryItemSubGroupDetails(Integer.parseInt(request.getParameter("diet_id")))));
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

	public ActionForward update(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		boolean success = true;
		Map parameterMap = request.getParameterMap();
		List dieterrorFields = new ArrayList();
		List constituentErrors = new ArrayList();
		FlashScope flashScope = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		DietaryMasterDAO dietaryDAO = new DietaryMasterDAO();
		ConstituentMasterDAO constituentDAO  = new ConstituentMasterDAO();

		BasicDynaBean dietaryBean = dietaryDAO.getBean();
		ConversionUtils.copyToDynaBean(parameterMap, dietaryBean, dieterrorFields);

		int totalRecords = Integer.parseInt(request.getParameter("recordLength"));

		if (dieterrorFields.isEmpty() && constituentErrors.isEmpty()){

			if (request.getParameter("serviceTax") == null || request.getParameter("serviceTax").isEmpty()){
				dietaryBean.set("service_tax", BigDecimal.ZERO);
			}else {
				dietaryBean.set("service_tax", new BigDecimal(request.getParameter("serviceTax")));
			}


			int dietUpdateCount = dietaryDAO.update(con, dietaryBean.getMap(), "diet_id", dietaryBean.get("diet_id"));
			if (dietUpdateCount != 1){
				success = false;
			}
			if(success) {
				int dietId = (Integer) dietaryBean.get("diet_id");
				success = saveOrUpdateItemSubGroup(dietId,con,request);
			}

			if(success) {
				int dietId = (Integer) dietaryBean.get("diet_id");
				success = saveOrUpdateInsuranceCategory(dietId,con,request);
			}

			if (success){
				BasicDynaBean dietIdBean = dietaryDAO.findByKey("diet_id", dietaryBean.get("diet_id"));
				int dietID = Integer.parseInt(dietIdBean.get("diet_id").toString());

				List<BasicDynaBean> newConstituentList = new ArrayList<BasicDynaBean>();
				List<BasicDynaBean> updateConstituentList = new ArrayList<BasicDynaBean>();
				List<BasicDynaBean> deleteConstituentList = new ArrayList<BasicDynaBean>();

				List<BasicDynaBean> hiddenUpdateConstituentList = new ArrayList<BasicDynaBean>();
				List<BasicDynaBean> hiddenDeleteConstituentList = new ArrayList<BasicDynaBean>();

				for (int i=1;i<=totalRecords;i++){

					//To get the list of constituent names which are newly added

					if (request.getParameter("constituent_name"+i) != null	&& !request.getParameter("constituent_name"+i).equals("")
							&& request.getParameter("newAdded"+i).equals("Y")){

						BasicDynaBean constituentBean = constituentDAO.getBean();
						constituentBean.set("constituent_name",request.getParameter("constituent_name"+i) );
						if ( request.getParameter("quantity"+i) != null  && !request.getParameter("quantity"+i).equals("")){
							constituentBean.set("quantity", new BigDecimal(request.getParameter("quantity"+i)));
						}else{
							constituentBean.set("quantity",BigDecimal.ZERO) ;
						}
						constituentBean.set("units", request.getParameter("units"+i));
						if (!request.getParameter("calorific_value"+i).equals("")){
							constituentBean.set("calorific_value", new BigDecimal(request.getParameter("calorific_value"+i)));
						}else {
							constituentBean.set("calorific_value", BigDecimal.ZERO);
						}
						if (request.getParameter("delete"+i).equals("N")){
							newConstituentList.add(constituentBean);
						}
					}//end of if condition


					//To get the list  of  constituents which are to be updated and marked for deletion

					if ( request.getParameter("constituent_name"+i) != null && !request.getParameter("constituent_name"+i).equals("")
							&& request.getParameter("newAdded"+i).equals("N")){

						BasicDynaBean constituentBean = constituentDAO.getBean();
						constituentBean.set("constituent_name",request.getParameter("constituent_name"+i) );
						if ( request.getParameter("quantity"+i) != null  && !request.getParameter("quantity"+i).equals("")){
							constituentBean.set("quantity", new BigDecimal(request.getParameter("quantity"+i)));
						}else{
							constituentBean.set("quantity",BigDecimal.ZERO) ;
						}
						constituentBean.set("units", request.getParameter("units"+i));
						if (!request.getParameter("calorific_value"+i).equals("")){
							constituentBean.set("calorific_value", new BigDecimal(request.getParameter("calorific_value"+i)));
						}else {
							constituentBean.set("calorific_value", BigDecimal.ZERO);
						}
						if (request.getParameter("delete"+i).equals("N")){
							updateConstituentList.add(constituentBean);
						}else{
							deleteConstituentList.add(constituentBean);
						}
						//for hidden values
						BasicDynaBean hiddenBean = constituentDAO.getBean();
						hiddenBean.set("diet_id", dietID);
						hiddenBean.set("constituent_name",request.getParameter("constituentName"+i) );
						if (request.getParameter("delete"+i).equals("N")){
							hiddenUpdateConstituentList.add(hiddenBean);
						}else{
							hiddenDeleteConstituentList.add(hiddenBean);
						}
					}//end of if
				}


				int updateCount = 0;
				int i = 0,j=0;

				//Delete
				for (BasicDynaBean deleteBean : deleteConstituentList){
					BasicDynaBean keys = hiddenDeleteConstituentList.get(j);
					success =  success && new ConstituentMasterDAO().delete(con, "diet_id", dietIdBean.get("diet_id"), "constituent_name", keys.get("constituent_name"));
					 j++;
				}

				//insert

				for (BasicDynaBean newConstituentBean : newConstituentList){
					newConstituentBean.set("diet_id",dietID);
					success = success && new  ConstituentMasterDAO().insert(con, newConstituentBean);
				}

				//update

				for (BasicDynaBean b : updateConstituentList){
					BasicDynaBean keys = hiddenUpdateConstituentList.get(i);
					updateCount = updateCount + new ConstituentMasterDAO().update(con, b.getMap(),keys.getMap());
					i++;
				}

				if ((success == true)&& (updateCount == updateConstituentList.size())){
					con.commit();
					DataBaseUtil.closeConnections(con, null);
					flashScope.success("Diet and constituent details are updated successfully....");
				}else{
					con.rollback();
					DataBaseUtil.closeConnections(con, null);
					flashScope.error("Fail to update diet and constituent details....");
				}
			}else{
				DataBaseUtil.closeConnections(con, null);
				flashScope.error("Error in updating diet details....");
			}
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flashScope.key());
		redirect.addParameter("diet_id", dietaryBean.get("diet_id"));
		return redirect;
	}

	public  ActionForward editCharges(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response) throws Exception{
			String orgId = request.getParameter("organization");
			JSONSerializer js = new JSONSerializer().exclude("class");

			int dietId = Integer.parseInt(request.getParameter("diet_id"));
			DietaryMasterDAO dao = new DietaryMasterDAO();
			ConstituentMasterDAO consdao = new ConstituentMasterDAO();
			String totalCal = consdao.getTotalCalorificValue(dietId);
			BasicDynaBean dietBean = dao.findByKey("diet_id", dietId);

			Map  map = dao.editDietCharges(orgId, dietId);

			request.setAttribute("chargeMap", map);
			request.setAttribute("bean", dietBean);
			request.setAttribute("org_id", orgId);
			request.setAttribute("tot_cal", totalCal);
			request.setAttribute("chargesLists", js.serialize(dao.getDietNamesAndMealIds()));
			return mapping.findForward("edit");
	}

	public ActionForward updateDietCharges(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

			ActionRedirect redirect = new ActionRedirect (mapping.findForward("editRedirect"));
			FlashScope fScope = FlashScope.getScope(request);
			int dietID = Integer.parseInt(request.getParameter("diet_id"));
			String orgID = request.getParameter("org_id");

			ArrayList bedlist = new ArrayList();
			ArrayList chargeList = new ArrayList();
			ArrayList discountList = new ArrayList();

			for (int i = 0;i<(request.getParameterValues("bedTypes")).length;i++){
			bedlist.add(request.getParameterValues("bedTypes")[i]);
			}
			for (int i = 0 ;i<request.getParameterValues("regularCharges").length;i++){

				if (request.getParameterValues("regularCharges")[i] == null || (request.getParameterValues("regularCharges")[i]).equals("")){
					chargeList.add(BigDecimal.ZERO);
				}else {
					chargeList.add(request.getParameterValues("regularCharges")[i]);
				}

				if (request.getParameterValues("discount")[i] == null || (request.getParameterValues("discount")[i]).equals("")){
					discountList.add(BigDecimal.ZERO);
				}else {
					discountList.add(request.getParameterValues("discount")[i]);
				}

			}
			DietaryMasterDAO dao = new DietaryMasterDAO ();
			DietChargesDAO cdao = new DietChargesDAO();
			boolean status = dao.updateDietCharges(dietID, orgID,bedlist,chargeList,discountList);
			GenericDAO rateDao = new GenericDAO("priority_rate_sheet_parameters_view");
			List<BasicDynaBean> derivedRatePlanList = rateDao.findAllByKey("base_rate_sheet_id", orgID);
			String[] ratePlanIds = new String[derivedRatePlanList.size()];
			for(int i=0;i<derivedRatePlanList.size();i++) {
				BasicDynaBean bean = derivedRatePlanList.get(i);
				String ratePlanId = (String)bean.get("org_id");
				ratePlanIds[i] = ratePlanId;
			}
			if(derivedRatePlanList.size()>0)
				status = cdao.updateDietChargesForRatePlans(orgID, ratePlanIds, bedlist, chargeList, discountList, dietID);

			if (status) {
					fScope.success("Diet charges are updated successfully");
			}else{
					fScope.error("Fail to update diet charges");
			}
			redirect.addParameter("organization", orgID);
			redirect.addParameter("diet_id",request.getParameter("diet_id"));
			redirect.addParameter(FlashScope.FLASH_KEY, fScope.key());

			return redirect;
	}

	public ActionForward getPrescriptionScreen(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response) throws SQLException{
			JSONSerializer js = new JSONSerializer();
			DietaryMasterDAO dao = new DietaryMasterDAO();
			PatientDietPrescriptionsDAO prescriptionDAO = new PatientDietPrescriptionsDAO();
			List prescriptionList =  prescriptionDAO.getPrescribedMealsForPatient(request.getParameter("patient_id"));
			String mealNameAndCharges  = dao.getAllMeal();
			String docId = "";
			String templateId = "";
			String format = "";
			BasicDynaBean dietDocBean = new GenericDAO("diet_chart_documents").findByKey("patient_id", request.getParameter("patient_id"));
			if (dietDocBean != null) {
				docId = dietDocBean.get("doc_id").toString();
				templateId = dietDocBean.get("template_id").toString();
				GenericDAO patientDocDAO = new GenericDAO("patient_documents");
				BasicDynaBean patientDocBean = patientDocDAO.getBean();
				patientDocDAO.loadByteaRecords(patientDocBean, "doc_id", Integer.parseInt(docId));
				format = (String)patientDocBean.get("doc_format");

			}

			request.setAttribute("mealNameAndCharges", mealNameAndCharges);
			request.setAttribute("patientid", request.getParameter("patient_id"));
			request.setAttribute("prescriptionList", prescriptionList);
			request.setAttribute("screentype", "prescriptionScreen");
			request.setAttribute("doctorList", js.exclude("class").serialize(new DoctorMasterDAO().getAllDoctor()));
			request.setAttribute("dietaryTempletes", GenericDocumentTemplateDAO.getTemplates(true, "SYS_DIE", "A"));
			request.setAttribute("doc_id", docId);
			request.setAttribute("template_id", templateId);
			request.setAttribute("format", format);
			request.setAttribute("opencreditbills", js.serialize(new DashBoardDAO().getOpenCreditBills()));
			return mapping.findForward("prescriptionScreen");
	}


	public ActionForward saveMealPrescriptions (ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response) throws SQLException, IOException, ParseException{

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		boolean success = true;
		List errorFields = new ArrayList();
		String[] mealName = request.getParameterValues("meal_name");
		String[] meal_date = request.getParameterValues("meal_date");
		String[] meal_timing = request.getParameterValues("meal_timing");
		String[] special_instructions = request.getParameterValues("special_instructions");
		String[] newAdded = request.getParameterValues("newAdded");
		String [] delete = request.getParameterValues("delete");
		String [] dietpresID = request.getParameterValues("diet_pres_id");
		int totalRecords = Integer.parseInt(request.getParameter("recordLength"));
		FlashScope fScope = FlashScope.getScope(request);
		String redirect = "prescriptionScreen";


		List<BasicDynaBean> newPrescribedLists = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> deletePrescribedLists = new ArrayList<BasicDynaBean>();

 		if (mealName != null){

			for (int i = 0; i<mealName.length;i++){

				if ((newAdded[i].equals("Y")) && (delete[i].equals("N"))) {

					BasicDynaBean bean = patientDietPrescriptions.getBean();
					bean.set("diet_pres_id", patientDietPrescriptions.getNextSequence());
					bean.set("added_to_bill", Boolean.FALSE);
					bean.set("prescribed_time", DateUtil.getCurrentTimestamp());
					bean.set("visit_id", request.getParameter("visit_id"));
					bean.set("prescribed_by", request.getParameter("prescribed_by"));

					ConversionUtils.copyIndexToDynaBean(request.getParameterMap(), i, bean, errorFields);

					logger.debug("Date after setting: " + bean.get("prescribed_by"));
					logger.info("Date after setting: " + bean.get("meal_date"));
					newPrescribedLists.add(bean);
				}

				if ((newAdded[i].equals("N")) && (delete[i].equals("Y"))) {

					BasicDynaBean bean =  patientDietPrescriptions.getBean();
					bean.set("diet_pres_id", Integer.parseInt(dietpresID[i]));

					deletePrescribedLists.add(bean);
				}
			}
			//delete
			for (BasicDynaBean deletePrescribedList : deletePrescribedLists){

				success = success && patientDietPrescriptions.delete(con, "diet_pres_id", deletePrescribedList.get("diet_pres_id"));
			}

			//save
			for (BasicDynaBean newPrescribedList : newPrescribedLists){

				success = success && patientDietPrescriptions.insert(con, newPrescribedList);
			}
			if (success){
				DataBaseUtil.commitClose(con, success);
				fScope.success("Prescription details are saved successfully....");
				fScope.put("print", "MealPrescriptionPrint");
			}else {
				DataBaseUtil.commitClose(con, success);
				fScope.error("Fail to save the prescription details....");
			}
		}
		DietaryMasterDAO dao = new DietaryMasterDAO();

		String mealNameAndCharges  = dao.getAllMeal();
		request.setAttribute("mealNameAndCharges", mealNameAndCharges);
		request.setAttribute("patientid", request.getParameter("visit_id"));

		ActionRedirect rediActionRedirect = new ActionRedirect(mapping.findForward("prescRedirect"));
		rediActionRedirect.addParameter("patient_id", request.getParameter("visit_id"));
		rediActionRedirect.addParameter("showPrinter", request.getParameter("printerId"));
		rediActionRedirect.addParameter(FlashScope.FLASH_KEY, fScope.key());
		return (rediActionRedirect);
	}


/*
	public ActionForward getCanteenScheduleScreen(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response) {

			request.setAttribute("method", "getMealsToBeDelivered");
		return mapping.findForward("canteenScheduleScreen");
	}
*/

	public ActionForward getMealsToBeDelivered(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response) throws SQLException, ParseException{

			Map listingParams = ConversionUtils.getListingParameter(request.getParameterMap());
			String date = request.getParameter("date");
			String ward = request.getParameter("wardname");
			String mealName = request.getParameter("mealname");
			String mealTiming = request.getParameter("mealtime");

			if (date == null || date.equals("")){
				date = DateUtil.currentDate("dd-MM-yyyy");
			}
			DietaryMasterDAO dao = new DietaryMasterDAO();
			PagedList deliveredMealList = dao.searchMealsToBeDelivered(listingParams,date, ward, mealName, mealTiming);

			request.setAttribute("mealList", deliveredMealList);
			request.setAttribute("date", date);
			request.setAttribute("method", "updateMealDeliveredStatus");
			return mapping.findForward("canteenScheduleScreen");
	}


	public ActionForward updateMealDeliveredStatus (ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response) throws SQLException, IOException{
			int updateCount = 0;
			HttpSession session = request.getSession(false);
			FlashScope scope = FlashScope.getScope(request);
			String[] orderedId = request.getParameterValues("orderedId");
			String[] updateStatus = request.getParameterValues("updateStatus");
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
			return redirect;
	}



	public ActionForward groupUpdate(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response) throws SQLException{

		String strOrgid = request.getParameter("org_id");
		String strVarianceBy = request.getParameter("varianceBy");
		String strVarianceValue = request.getParameter("varianceValue");
		String strVarianceType = request.getParameter("varianceType");
		String arrDietIdStr[] = request.getParameterValues("dietID");
		String arrBedType[] = request.getParameterValues("groupBedType");
		String updateTable = request.getParameter("updateTable");
		String strAllbedTypes = request.getParameter("allBedTypes");

		FlashScope scope = FlashScope.getScope(request);

		BigDecimal amount = null;
		DietaryMasterBO bo = new DietaryMasterBO();

		boolean isPercentage = false;
		boolean success = true;

		if (strVarianceValue !=null && !strVarianceValue.equals("")){
			amount = new BigDecimal(strVarianceValue);

		}else if (strVarianceBy !=null && !strVarianceBy.equals("")){
			amount = new BigDecimal(strVarianceBy);
			isPercentage = true;
		}

		BigDecimal roundTo;
		try {
			roundTo = new BigDecimal(request.getParameter("round"));
		} catch (NumberFormatException e) {
			ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
			redirect.addParameter("org_id", strOrgid);
			redirect.addParameter(FlashScope.FLASH_KEY, scope.key());
			scope.put("error", "Incorrectly formatted parameters");
			return redirect;
		}

		if (strVarianceType.equals("-"))
			amount = amount.negate();

		List<Integer> selectDiets = null;
		if (arrDietIdStr != null && arrDietIdStr.length > 0 ) {

			Integer[] arrDietId = new Integer[arrDietIdStr.length];
			for (int i=0;i<arrDietIdStr.length;i++){
				arrDietId[i] = new Integer(arrDietIdStr[i]);
			}

			for (int i=0;i<arrDietId.length;i++){
				selectDiets = Arrays.asList(arrDietId);
			}
		}
		List<String> bedTypes = null;
		if ( arrBedType != null || !strAllbedTypes.equals("") ) {
			for (int i=0;i<arrBedType.length;i++){
				bedTypes = Arrays.asList(arrBedType);
			}
		}

		success = bo.groupUpdateCharges(strOrgid, bedTypes, selectDiets, amount,
				isPercentage, roundTo ,updateTable);

		if (success){
			scope.success("Diet charges are updated successfully....");
		}else{
			scope.success("Fail to update the diet charges....");
		}

		ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
		redirect.addParameter("org_id", strOrgid);
		redirect.addParameter(FlashScope.FLASH_KEY, scope.key());
		return redirect;
	}

	//For Prescribed meal print

	public ActionForward printPrescription(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException,
			DocumentException, TemplateException, XPathExpressionException, TransformerException {
		String visitIdStr = request.getParameter("patient_id");
		String printerIdStr = request.getParameter("printerId");
		BasicDynaBean prefs = null;
		int printerId = 0;
		if ( (printerIdStr !=null) && !printerIdStr.equals("")) {
			printerId = Integer.parseInt(printerIdStr);
		}

		prefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT,
				printerId);

		String printMode = "P";
		if (prefs.get("print_mode") != null) {
			printMode = (String) prefs.get("print_mode");
		}
		PrescribedMealFtlHealper ftlHelper = new PrescribedMealFtlHealper();

		if (printMode.equals("P")) {
			response.setContentType("application/pdf");
			OutputStream os = response.getOutputStream();
			ftlHelper.getPrescriptionFtlReport(visitIdStr, PrescribedMealFtlHealper.return_type.PDF,
					prefs, os);
			os.close();

		} else {
			String textReport = new String(ftlHelper.getPrescriptionFtlReport(visitIdStr,
					PrescribedMealFtlHealper.return_type.TEXT_BYTES, prefs, null));
			request.setAttribute("textReport", textReport);
			request.setAttribute("textColumns", prefs.get("text_mode_column"));
			request.setAttribute("printerType", "DMP");
			return mapping.findForward("textPrintApplet");

		}
		return null;
	}
	
	private boolean saveOrUpdateItemSubGroup(int dietId, Connection con,HttpServletRequest request)
			throws SQLException, IOException{
			Map params = request.getParameterMap();
			List errors = new ArrayList();
			
			boolean flag = true;
			String [] itemSubgroupId = request.getParameterValues("item_subgroup_id");
			String [] delete = request.getParameterValues("deleted");
			
			if (errors.isEmpty()) {
				if(itemSubgroupId != null && itemSubgroupId.length > 0 && !itemSubgroupId[0].equals("")){
					GenericDAO itemsubgroupdao = new GenericDAO("dietary_item_sub_groups");
					BasicDynaBean itemsubgroupbean = itemsubgroupdao.getBean();
					ConversionUtils.copyToDynaBean(params, itemsubgroupbean, errors);
					List records = itemsubgroupdao.findAllByKey("diet_id", dietId);
					if (records.size() > 0)
						flag = itemsubgroupdao.delete(con, "diet_id", dietId);
					
					for(int i=0; i<itemSubgroupId.length; i++){
						if(itemSubgroupId[i] != null && !itemSubgroupId[i].isEmpty()) {
							if (delete[i].equalsIgnoreCase("false")) {
								itemsubgroupbean.set("diet_id", dietId);
								itemsubgroupbean.set("item_subgroup_id", Integer.parseInt(itemSubgroupId[i]));
								flag = itemsubgroupdao.insert(con, itemsubgroupbean);
							}
						}
					}
				}	
			}
			return flag;

		}

	private boolean saveOrUpdateInsuranceCategory(int dietId,
	    Connection con, HttpServletRequest request) throws SQLException, IOException {
	    boolean flag = true;
	    String[] insuranceCategories = request.getParameterValues("insurance_category_id");
	    if (insuranceCategories != null && insuranceCategories.length > 0
	        && !insuranceCategories[0].equals("")) {
	      GenericDAO insuranceCategoryDAO =
	          new GenericDAO("diet_insurance_category_mapping");
	      BasicDynaBean insuranceCategoryBean = insuranceCategoryDAO.getBean();
	      List<BasicDynaBean> records = insuranceCategoryDAO.findAllByKey("diet_id", dietId);
	      if (records != null && records.size() > 0) {
	        flag = insuranceCategoryDAO.delete(con,"diet_id", dietId);
	      }
	      for (String insuranceCategory :  insuranceCategories) {
	        insuranceCategoryBean.set("diet_id", dietId);
	        insuranceCategoryBean.set("insurance_category_id", Integer.parseInt(insuranceCategory));
	        flag = insuranceCategoryDAO.insert(con,insuranceCategoryBean);
	      }
	    }
	    return flag;
	  }

}
