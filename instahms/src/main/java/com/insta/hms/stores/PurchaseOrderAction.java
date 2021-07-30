package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.NumberToWordFormat;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.core.inventory.StoresHelper;
import com.insta.hms.core.inventory.procurement.PurchaseOrderService;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.MiscellaneousSettings.MiscellaneousSettingsDAO;
import com.insta.hms.master.POPrintTemplate.POPrintTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.SupplierContractsItem.SupplierContractsItemRateDAO;
import com.insta.hms.messaging.MessageManager;
import com.insta.hms.usermanager.UserDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import flexjson.JSONSerializer;

public class PurchaseOrderAction extends BaseAction {

	static Logger log = LoggerFactory.getLogger(PurchaseOrderAction.class);
	private static final SupplierContractsItemRateDAO supContractItemDao = new SupplierContractsItemRateDAO();
  private static final GenericDAO storePOMainDAO = new GenericDAO("store_po_main");
  private static final GenericDAO storePOTaxDetailsDAO = new GenericDAO("store_po_tax_details");
  private static final GenericDAO storePODAO = new GenericDAO("store_po");
	private PurchaseOrderService purchaseOrderService = (PurchaseOrderService) ApplicationContextProvider
      .getApplicationContext().getBean("purchaseOrderService");

	@IgnoreConfidentialFilters
	public ActionForward getPOScreen(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res)
		throws SQLException, ParseException {

		req.setAttribute("medicine_timestamp", PharmacymasterDAO.getItemMasterTimestamp());

		String termsAndConditions = PurchaseOrderDAO.getAdditionalTermsAndConditions();
		req.setAttribute("hospitalterms", termsAndConditions);

		BasicDynaBean printPref=PrintConfigurationsDAO.getPrintMode(PrintConfigurationsDAO.PRINT_TYPE_STORE);
		req.setAttribute("printPref",printPref);

		req.setAttribute("templates", new POPrintTemplateDAO().getTemplateNames());
		GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
		req.setAttribute("default_po_print_template", dto.getDefault_po_print_template());
		
		  int centerId = (Integer)req.getSession().getAttribute("centerId");
		  JSONSerializer js = new JSONSerializer().exclude("class");
		  req.setAttribute("listAllcentersforAPo", js.deepSerialize(PurchaseOrderDAO.listAllcentersforAPo(centerId)));
		  String healthAuthorityForCenter = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
		  String healthAuthority = HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(healthAuthorityForCenter).getHealth_authority();
		
		String poNo = req.getParameter("poNo");
		if (poNo != null && !poNo.equals("")) {
			BasicDynaBean pobean = PurchaseOrderDAO.getPoDetails(poNo);
			req.setAttribute("pobean", pobean);
			List<BasicDynaBean> poItems = PurchaseOrderDAO.getPOItems(poNo,healthAuthority);
			List<Map <String, Object>> poItemMaps = new ArrayList<Map<String, Object>>();
			
			for(BasicDynaBean poItem : poItems){
			  Map<String, Object> poItemMap = new HashMap<String, Object>();
			  poItemMap.putAll(poItem.getMap());
			  List<BasicDynaBean> suppRateList = supContractItemDao.getSupplierItemRateValue(poItem.get("medicine_id"),(String) pobean.get("supplier_id"), centerId);
			  Map<String, Object> supplierRates = getSupplierRateDetails(poItem, suppRateList, centerId);
			  poItemMap.put("margin", supplierRates.get("margin"));
			  poItemMap.put("margin_type", supplierRates.get("margin_type"));
			  poItemMap.put("min_rate", supplierRates.get("min_rate"));
			  poItemMap.put("discounted_min_rate", supplierRates.get("discounted_min_rate"));
			  poItemMap.put("min_rate_suppliers", supplierRates.get("min_rate_suppliers"));
			  poItemMaps.add(poItemMap);
			}
			
			req.setAttribute("poItems", poItemMaps);
			BasicDynaBean poGrnDetBean = new GenericDAO("store_grn_main").findByKey("po_no",poNo);
			req.setAttribute("grn_exists", poGrnDetBean != null);
			List<BasicDynaBean> poTaxDetails = PurchaseOrderDAO.getPOTaxDetails(poNo);
			req.setAttribute("po_tax_details", poTaxDetails);
		}
		List<String> storeUserList = UserDAO.getStoreUsers();
		Iterator<String> storeUserIterator = storeUserList.iterator();
		StringBuilder storeUsers = new StringBuilder();
		while(storeUserIterator.hasNext()) {
			storeUsers.append(storeUserIterator.next()).append(",");
		}
		List<BasicDynaBean> sugGroupList = PurchaseOrderDAO.getAllSubGroups();
		List<BasicDynaBean> groupList = PurchaseOrderDAO.getAllGroups();
		
		req.setAttribute("subGroupListJSON", js.serialize(ConversionUtils.listBeanToListMap(sugGroupList)));
		req.setAttribute("groupList", ConversionUtils.listBeanToListMap(groupList));
		req.setAttribute("groupListJSON", js.serialize(ConversionUtils.listBeanToListMap(groupList)));
		req.setAttribute("grn_count",  poNo == null || poNo.equals("") ? 0 : new StockEntryDAO().getGrnCount(poNo) );
		req.setAttribute("screen_id", m.getProperty("action_id"));
		req.setAttribute("store_users", storeUsers.length() > 0 ? storeUsers.substring(0, storeUsers.length()-1) : "");
		return m.findForward("poscreen");
	}

	@IgnoreConfidentialFilters
	public ActionForward getMedDetails(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{
		JSONSerializer js = new JSONSerializer().exclude("class");
		String medicineId = request.getParameter("medicineId");
		String suppId = request.getParameter("suppId");
		int storeId = Integer.parseInt(request.getParameter("storeId"));
		BasicDynaBean centerIdBean = UserDAO.getCenterId(storeId);
		int centerId = (Integer)centerIdBean.get("center_id");
		List centerStores = new StockEntryAction().getAllStores(centerId);
		String supplier_Validation = request.getParameter("supplier_Validation");
		response.setContentType("text/plain");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		 
		BasicDynaBean item = null;
		item = PurchaseOrderDAO.getItemDetailsForPo(
				Integer.parseInt(medicineId),storeId, suppId ,centerStores,(Integer)request.getSession().getAttribute("centerId"));
		if(supplier_Validation.equalsIgnoreCase("true")){
			List<BasicDynaBean> suppRateList = supContractItemDao.getSupplierItemRateValue(Integer.parseInt(medicineId),suppId, centerId);
		    Map<String, Object> resultMap = null;
			
		    if(item != null){
		    	resultMap = getSupplierRateDetails(item, suppRateList, centerId);
			}
		    response.getWriter().write(js.deepSerialize(resultMap));
		}
		
		if(!supplier_Validation.equalsIgnoreCase("true") && item != null){
			response.getWriter().write(js.serialize(item.getMap()));
		}
		response.flushBuffer();
		return null;
	}

	@IgnoreConfidentialFilters
	public ActionForward getTemplateText(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{

		String templateCode = request.getParameter("templateCode");
		String text = PaymentTermsMasterDAO.getTemplateText(templateCode);

		response.setContentType("text/plain");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		response.getWriter().write(text);
		response.flushBuffer();
		return null;
	}

	@IgnoreConfidentialFilters
	public ActionForward getPOItemsList (ActionMapping mappinf,ActionForm form,
			HttpServletRequest req,HttpServletResponse res) throws SQLException,Exception {
		JSONSerializer js = new JSONSerializer().exclude("class");
		int centerId = (Integer)req.getSession().getAttribute("centerId");
		String healthAuthorityForCenter = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
		String healthAuthority = HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(healthAuthorityForCenter).getHealth_authority();

		ArrayList<String> items = PurchaseOrderDAO.getPOItemsList(req.getParameter("po_no"),healthAuthority);
		res.setContentType("application/x-json");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        res.getWriter().write(js.serialize(items));
        res.flushBuffer();
        return null;
	}

	@IgnoreConfidentialFilters
	public ActionForward savePO(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws SQLException,Exception {
		
		Map itemsMap = getParameterMap(req);
		ActionRedirect redirect = null;
		HttpSession session = req.getSession(false);
		String username = (String) session.getAttribute("userid");
		Connection con = null;
		boolean status = false;
		boolean newPo = false;
		ArrayList<String> printUrls = null;
		
		String poNo = getParameter(itemsMap, "po_no");
		String[] item_id = (String[]) itemsMap.get("medicine_id");
		int	elelen = item_id.length -1;
		

		String[] deleted = (String[]) itemsMap.get("_deleted");
		String[] itemStatus = (String[]) itemsMap.get("status_ar");
		String storeIdStr = getParameter(itemsMap,"store_id");
		int storeId = Integer.parseInt(storeIdStr);
		String maintaxType = getParameter(itemsMap, "main_vat_type");
		String mainCST = getParameter(itemsMap, "main_cst_rate");

		BigDecimal poapplimit = GenericPreferencesDAO.getGenericPreferences().getPoApprovalLimit();
		BigDecimal userPOAppLimit = null;
		if ( session.getAttribute("userpoApprovalLimit") != null )
			userPOAppLimit = (BigDecimal)session.getAttribute("userpoApprovalLimit");

		boolean automaticPOApprove = false;
		BasicDynaBean poMainBean =null;
		BasicDynaBean poTaxBean =null;
		boolean applySuppTaxRules = (Boolean)GenericPreferencesDAO.getAllPrefs().get("apply_supplier_tax_rules");
		if(applySuppTaxRules) {
			if(maintaxType!= null && maintaxType.equals("Not Applicable"))
				maintaxType = "NA";
		}

		try {
			redirect = new ActionRedirect(mapping.findForward("poscreenRedirect"));
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			log.debug("Number of items being entered in stock: " + elelen);

			poMainBean = storePOMainDAO.getBean();
			poTaxBean = storePOTaxDetailsDAO.getBean();
			ConversionUtils.copyToDynaBean(itemsMap, poMainBean, null, true);

			// some parameters are prefixed with po_ to avoid conflict with store_po details
			ConversionUtils.copyToDynaBean(itemsMap, poMainBean, "po_");
			BasicDynaBean exisitnPOMainBean = storePOMainDAO.findByKey(con, "po_no", poNo);
			String tcsPer = getParameter(itemsMap, "po_tcs_per");
    	    String tcsType = getParameter(itemsMap, "tcs_type");
	        if (null != tcsType && tcsType.equalsIgnoreCase("P")) {
	            poMainBean.set("tcs_per", new BigDecimal(tcsPer));
	        }
			if ( exisitnPOMainBean == null ){
				poMainBean.set("user_id", username.toString());
			}
			poMainBean.set("last_modified_by", username.toString());
			poMainBean.set("actual_po_date", DateUtil.getCurrentTimestamp());
			poMainBean.set("mrp_type", "I");
			poMainBean.set("vat_type", "");
			poMainBean.set("vat_rate", BigDecimal.ZERO );


			//even on an edit, the total may reduce to become less than approval limit
            if (  ((BigDecimal)poMainBean.get("po_total")).compareTo(poapplimit) <= 0
            		&& (userPOAppLimit == null || ((BigDecimal)poMainBean.get("po_total")).compareTo(userPOAppLimit) <= 0 )) {
            	if(!poMainBean.get("status").equals("FC") && !poMainBean.get("status").equals("A") 
            			&& !poMainBean.get("status").equals("AA")){//if user is not manually approving
            		poMainBean.set("approver_remarks", "Automatic PO Approval");
            	}
            	if(!poMainBean.get("status").equals("FC"))
            		poMainBean.set("status", poMainBean.get("amended_reason") != null ? "AA" : "A");
                poMainBean.set("approved_by", username);
                automaticPOApprove = true;
            }

            //setting validated details,approved details
            if ( !poMainBean.get("status").equals("O") && !poMainBean.get("status").equals("AO") ){//meaning validate/approve status
            	//we need to update some extra attributes
            	if ( poMainBean.get("status").equals("V")) {

            		poMainBean.set("validated_by", session.getAttribute("userid"));
            		poMainBean.set("validated_time", DataBaseUtil.getDateandTime());
            		poMainBean.set("validator_remarks", poMainBean.get("remarks"));
            	} else if ( poMainBean.get("status").equals("AV")) {

            		poMainBean.set("amendment_validated_by", session.getAttribute("userid"));
            		poMainBean.set("amendment_validated_time", DataBaseUtil.getDateandTime());
            		poMainBean.set("amendment_validator_remarks", poMainBean.get("remarks"));
            		poMainBean.set("amendment_approver_remarks", "");
            		poMainBean.set("amendment_approved_by", "");
                    poMainBean.set("amendment_approved_time", null);
                    if(exisitnPOMainBean.get("amendment_time") == null)
                    	poMainBean.set("amendment_time", DataBaseUtil.getDateandTime());
            	} else if ( poMainBean.get("status").equals("A")) {

            		poMainBean.set("approver_remarks", poMainBean.get("approver_remarks"));
            		String approver_remarks = poMainBean.get("approver_remarks") == null ? "" : (String)poMainBean.get("approver_remarks");
            		poMainBean.set("remarks",poMainBean.get("remarks") == null ? (approver_remarks) : ((String)poMainBean.get("remarks")).concat(approver_remarks));
                    poMainBean.set("approved_by", username);
                    poMainBean.set("approved_time", DataBaseUtil.getDateandTime()); 
            	} else if ( poMainBean.get("status").equals("AA")) {

            		poMainBean.set("amendment_approver_remarks", poMainBean.get("remarks"));
            		String approver_remarks = poMainBean.get("approver_remarks") == null ? "" : (String)poMainBean.get("approver_remarks");
            		poMainBean.set("remarks",poMainBean.get("remarks") == null ? approver_remarks :
            			((String)poMainBean.get("remarks")).contains("Automatic PO Approval")
            			?((String)poMainBean.get("remarks")):((String)poMainBean.get("remarks")).concat(approver_remarks));
            		//poMainBean.set("remarks",poMainBean.get("remarks") == null ? approver_remarks : ((String)poMainBean.get("remarks")).concat(approver_remarks));
                    poMainBean.set("amendment_approved_by", username);
                    poMainBean.set("amendment_approved_time", DataBaseUtil.getDateandTime());
                    if ( exisitnPOMainBean != null && exisitnPOMainBean.get("amendment_time") == null )
                    	poMainBean.set("amendment_time", DataBaseUtil.getDateandTime());
            	}
            }

            if ( (String)poMainBean.get("amended_reason") != null && !((String)poMainBean.get("amended_reason")).isEmpty() ) {
            	//poMainBean.set("amendment_time", DataBaseUtil.getDateandTime());
            	
            	if ( exisitnPOMainBean != null && exisitnPOMainBean.get("amended_by") == null ) {
            		poMainBean.set("amended_by", username);
            	}
            }
            
            //check status to "AO"
            if(poMainBean.get("status").equals("AO")){
            	eraseAmendDetailsForCopyPO(poMainBean);
            	poMainBean.set("amendment_time", DataBaseUtil.getDateandTime());
            	poMainBean.set("amended_by", username);
            }
            

			if (poNo.equals("")) {
				// new PO: create one
				newPo = true;
				poNo = PurchaseOrderDAO.getNextId(""+storeId);
				poMainBean.set("po_no", poNo);
				storePOMainDAO.insert(con, poMainBean);

			} else {
			  storePOMainDAO.update(con, poMainBean.getMap(), "po_no", poNo);
			}
			
			// quotation copy upload
 			String filename = getParameter(itemsMap, "fileName");
 			String del = getParameter(itemsMap, "deleteUploadedQuotation");

 			if ((filename != null && !filename.equals("")) || (del != null && del.equals("Y"))) {
 				HashMap map = new HashMap<String, Object>();

 				if (del != null && del.equals("Y")) {
 					map.put("quotation_file_name", null);
 					map.put("quotation_attachment", null);
 					map.put("quotation_contenttype", null);

 				} else {
 					// filename is not null
 					Object[] fileContent = (Object[]) itemsMap.get("quotationAttachment");
 					map.put("quotation_file_name", filename);
 					map.put("quotation_attachment", fileContent[0]);
 					map.put("quotation_contenttype", getParameter(itemsMap, "content_type"));
 				}

 				int c = storePOMainDAO.update(con, map, "po_no", poMainBean.get("po_no"));

 				assert (c ==1) : "Quotation file upload/delete failed with row count: " + c;
 			}

 			List<BasicDynaBean> groupList = PurchaseOrderDAO.getAllGroups();
 			StoresHelper storeHelper = new StoresHelper();
			// details
			for (int i=0; i<elelen; i++) {
				BasicDynaBean bean = storePODAO.getBean();		// details
				ConversionUtils.copyIndexToDynaBean(itemsMap, i, bean, null, true);

				if (deleted[i].equalsIgnoreCase("false")) {
					bean.set("po_no", poNo);
						
					bean.set("status", 
							((String)poMainBean.get("status")).equals("AA") || ((String)poMainBean.get("status")).equals("A") //if
							?  (!itemStatus[i].equals("R") ? (String)poMainBean.get("status") : itemStatus[i]) 
							: (String)poMainBean.get("status"));//else
					
					if ( automaticPOApprove  && !bean.get("status").equals("R"))
						bean.set("status", poMainBean.get("amended_reason") != null ? "AA" : "A");
					if ( !((String)poMainBean.get("status")).equals("A") &&  !((String)poMainBean.get("status")).equals("AA") )
						bean.set("item_remarks", null);

					if (bean.get("item_order") == null) {
						// insert: remove
						storePODAO.insert(con, bean);
						for(int j=0; j<groupList.size() ;j++) {
						 	BasicDynaBean groupBean = groupList.get(j);
						 	BasicDynaBean taxBean = storePOTaxDetailsDAO.getBean();
						 	taxBean.set("medicine_id", bean.get("medicine_id"));
						 	taxBean.set("po_no", bean.get("po_no"));
						 	storeHelper.setTaxDetails(itemsMap, i, (Integer)groupBean.get("item_group_id"), taxBean);
						 	Map<String, Object> taxMap = new HashMap<String, Object>();
						 	taxMap.put("medicine_id", bean.get("medicine_id"));
						 	taxMap.put("po_no", bean.get("po_no"));
						 	taxMap.put("item_subgroup_id", taxBean.get("item_subgroup_id"));
						 	if(storePOTaxDetailsDAO.findByKey(taxMap) != null) {
							 	if(taxBean.get("item_subgroup_id") != null && taxBean.get("tax_amt") != null) {
							 		Map keysMap = new HashMap();
							 		keysMap.put("medicine_id", bean.get("medicine_id"));
							 		keysMap.put("po_no", bean.get("po_no"));
							 		keysMap.put("item_subgroup_id", taxBean.get("item_subgroup_id"));
							 		storePOTaxDetailsDAO.update(con, taxBean.getMap(), keysMap);
							 	}
              } else {
                int oldTaxSubgroupId = storeHelper.getOldTaxSubgroup(itemsMap, i,
                    (Integer) groupBean.get("item_group_id"));
                LinkedHashMap<String, Object> identifiers = new LinkedHashMap<>();
                identifiers.put("medicine_id", bean.get("medicine_id"));
                identifiers.put("po_no", bean.get("po_no"));
                identifiers.put("item_subgroup_id", oldTaxSubgroupId);
                storePOTaxDetailsDAO.delete(con, identifiers);
                if (taxBean.get("item_subgroup_id") != null && taxBean.get("tax_amt") != null) {
                  storePOTaxDetailsDAO.insert(con, taxBean);
                }
              }
						 	
						 }
						
					} else {
						// update
						storePODAO.updateWithName(con, bean.getMap(), "item_order");
						for(int j=0; j<groupList.size() ;j++) {
							BasicDynaBean groupBean = groupList.get(j);
						 	BasicDynaBean taxBean = storePOTaxDetailsDAO.getBean();
						 	taxBean.set("medicine_id", bean.get("medicine_id"));
						 	taxBean.set("po_no", bean.get("po_no"));
						 	storeHelper.setTaxDetails(itemsMap, i, (Integer)groupBean.get("item_group_id"), taxBean);
						 	Map<String, Object> taxMap = new HashMap<String, Object>();
						 	taxMap.put("medicine_id", bean.get("medicine_id"));
						 	taxMap.put("po_no", bean.get("po_no"));
						 	taxMap.put("item_subgroup_id", taxBean.get("item_subgroup_id"));
						 	if(storePOTaxDetailsDAO.findByKey(taxMap) != null) {
							 	if(taxBean.get("item_subgroup_id") != null && taxBean.get("tax_amt") != null) {
							 		Map keysMap = new HashMap();
							 		keysMap.put("medicine_id", bean.get("medicine_id"));
							 		keysMap.put("po_no", bean.get("po_no"));
							 		keysMap.put("item_subgroup_id", taxBean.get("item_subgroup_id"));
							 		storePOTaxDetailsDAO.update(con, taxBean.getMap(), keysMap);
							 	}
              } else {
                int oldTaxSubgroupId = storeHelper.getOldTaxSubgroup(itemsMap, i,
                    (Integer) groupBean.get("item_group_id"));
                LinkedHashMap<String, Object> identifiers = new LinkedHashMap<>();
                identifiers.put("medicine_id", bean.get("medicine_id"));
                identifiers.put("po_no", bean.get("po_no"));
                identifiers.put("item_subgroup_id", oldTaxSubgroupId);
                storePOTaxDetailsDAO.delete(con, identifiers);
                if (taxBean.get("item_subgroup_id") != null && taxBean.get("tax_amt") != null) {
                  storePOTaxDetailsDAO.insert(con, taxBean);
                }
              }
						 }
					}

					//append po_no in store_indent_details table.
					// TODO: Move this to some DAO.
				    String query = "UPDATE store_indent_details SET po_no=coalesce(po_no||','||?,?) WHERE " +
				    			" medicine_id = ? AND  indent_no IN ( " +
				    			" SELECT indent_no FROM store_indent_main WHERE  status = 'A' ) ";
					Map<String, Object> keys = new HashMap<String, Object>();
					keys.put("1", poNo);
					keys.put("2",  poNo);
					keys.put("3", Integer.parseInt(item_id[i]));
					StoresDBTablesUtil.updateTable(con,keys,query);

				} else {
					storePODAO.delete(con, "item_order", bean.get("item_order"));
					LinkedHashMap<String, Object> identifiers = new LinkedHashMap<>();
					identifiers.put("medicine_id", bean.get("medicine_id"));
					identifiers.put("po_no", poNo);
					storePOTaxDetailsDAO.delete(con, identifiers);
				}
			}

			BasicDynaBean poDetails = storePODAO.findByKey(con,"po_no",poNo);
			if( poDetails == null ) {//in case of empty po details po status should be open

				poMainBean.set("status", "O");
				storePOMainDAO.update(con, poMainBean.getMap(), "po_no", poNo);
			}


			status = true;
			if (getParameter(itemsMap,"_printAfterSave").equals("Y")) {
				ActionRedirect url = new ActionRedirect(mapping.findForward("poPrintRedirect"));
				url.addParameter("poNo", poNo);
				url.addParameter("printType", getParameter(itemsMap,"printType"));
				url.addParameter("temp_name", getParameter(itemsMap,"template_name"));
				printUrls = new ArrayList<String>();
				printUrls.add(req.getContextPath() + url.getPath());
				session.setAttribute("printURLs", printUrls);
			}
			
			
		} finally {
			DataBaseUtil.commitClose(con, status);
		}
		//to do add a condition on po status ='A'.Only approved po to be mailed
		if ( ( ((String)poMainBean.get("status")).equals("A") || ((String)poMainBean.get("status")).equals("AA") ) && ((Boolean)(GenericPreferencesDAO.getAllPrefs()).get("auto_mail_po_to_sup"))) {
			
			//auto approved po email to the supplier
			MessageManager mgr = new MessageManager();
			Map reportData = new HashMap();
			reportData.put("po_no", new String[]{poNo});
			reportData.put("printType", getParameter(itemsMap,"printType"));
			reportData.put("template_name", getParameter(itemsMap,"template_name"));
			mgr.processEvent("purches_order_report", reportData);
		}
		
		redirect.addParameter("poNo", poNo);
		if (newPo) {
			FlashScope flash = FlashScope.getScope(req);
			flash.info("Purchase Order " + poNo + " generated successfully");
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		}
		return redirect;
	}

	@IgnoreConfidentialFilters
	public  ActionForward generatePOprint(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response) throws Exception {

		Connection con=null;
	//	Template t = null;
		FtlReportGenerator ftlGen = null;
		Map params = new HashMap();
		String poNo=request.getParameter("poNo");
		String indentNo="";
		String indentDate="";
		String indentCenter = "";
		Timestamp authDate=null;
		BigDecimal totalAmountWithTax = BigDecimal.ZERO;
		BigDecimal totalTaxAmount = BigDecimal.ZERO;

		try{
			if (poNo != null) {
				con = DataBaseUtil.getReadOnlyConnection();
				MiscellaneousSettingsDAO dao= new MiscellaneousSettingsDAO();
				GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
				List<BasicDynaBean>poOrderList=PurchaseOrderDAO.getPurchaseOrderList(con,poNo);
				List<BasicDynaBean> indentDetails=PurchaseOrderDAO.getIndentNo(poNo);
				/** Taxation Deatils */
				List<BasicDynaBean> poTaxDetails = PurchaseOrderDAO.getTaxDetails(con, poNo);
				params.put("poTaxDetails",  poTaxDetails);
				
				BasicDynaBean poBean = storePOMainDAO.findByKey(con, "po_no", poNo);
				BasicDynaBean suppBean = PurchaseOrderDAO.getSupplierDetails(con,poNo);
				boolean approved = poBean.get("status").equals("A");
				BasicDynaBean printprefs = null;
				String printerId = request.getParameter("printType");
				if ( printerId != null )
					printprefs = PrintConfigurationsDAO.getPageOptions(
								PrintConfigurationsDAO.PRINT_TYPE_STORE, Integer.parseInt(printerId));
				if ( printprefs == null )
					printprefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_STORE);
				params.put("items", poOrderList);
				params.put("hospital_tin",dto.getHospitalTin());
				params.put("hospital_pan",dto.getHospitalPan());
				params.put("hospital_service_regn_no",dto.getHospitalServiceRegnNo());
				params.put("poBean", poBean);
				params.put("suppBean", suppBean);
				BasicDynaBean deptBean = new GenericDAO("department").findByKey("dept_id", poBean.get("dept_id"));
				params.put("dept_name", deptBean != null ? deptBean.get("dept_name") : null);
				params.put("NumberToStringConversion", NumberToWordFormat.wordFormat());
				if(indentDetails != null &&  indentDetails.size()>0){
					Iterator it = indentDetails.iterator();
					while(it.hasNext()){
						BasicDynaBean bean = (BasicDynaBean) it.next();
						indentNo=indentNo+bean.get("indent_no").toString()+',';
						indentDate=indentDate+bean.get("indent_date").toString()+',';
						indentCenter = indentCenter+bean.get("indent_center_name")+',';
						//authDate=(Timestamp) bean.get("approved_time");
					}
					indentNo=indentNo.substring(0, indentNo.length()-1);
					indentDate=indentDate.substring(0, indentDate.length()-1);
					params.put("indentNo",indentNo);
					params.put("indentDate",indentDate);
					params.put("indentCenter", indentCenter);
					log.debug("indentCenter****"+indentCenter);
					//params.put("auth_date",authDate);

				}
				HashMap<String, BigDecimal> vatDetails = new HashMap<String, BigDecimal>();
				for (BasicDynaBean b: poOrderList) {
					String rate = b.get("vat_rate").toString();
					BigDecimal taxAmt = (BigDecimal) b.get("vat");
					BigDecimal totalTax = vatDetails.get(rate);
					if (totalTax == null) {
						vatDetails.put(rate, taxAmt);
					} else {
						vatDetails.put(rate, taxAmt.add(totalTax));
					}
					BigDecimal medTotal = (BigDecimal) b.get("med_total");
					totalAmountWithTax = totalAmountWithTax.add(medTotal);
					totalTaxAmount  = totalTaxAmount.add(taxAmt);
				}
				params.put("totalAmountInWords",NumberToWordFormat.wordFormat().toRupeesPaise(totalAmountWithTax.subtract(totalTaxAmount)) );
				params.put("vatDetails",  vatDetails);
				String templateName = request.getParameter("temp_name");
				String templateMode = null;
				String templateContent = null;
				StringWriter writer = new StringWriter();
				if (templateName == null || templateName.equals("") || templateName.equals("BUILTIN_HTML")) {
				//	t = AppInit.getFmConfig().getTemplate("PurchaseOrderPrint.ftl");
					ftlGen = new FtlReportGenerator("PurchaseOrderPrint");
					templateMode = "H";
				} else if (templateName.equals("BUILTIN_TEXT")) {
				//	t = AppInit.getFmConfig().getTemplate("PurchaseOrderTextPrint.ftl");
					ftlGen = new FtlReportGenerator("PurchaseOrderTextPrint");
					templateMode = "T";
				} else {
					String templateCodeQuery =
						"SELECT pharmacy_template_content, template_mode FROM po_print_template " +
						" WHERE template_name=?";
					List printTemplateList  = DataBaseUtil.queryToDynaList(templateCodeQuery,templateName);
					for (Object obj: printTemplateList){
						BasicDynaBean templateBean = (BasicDynaBean) obj;
						templateContent = (String)templateBean.get("pharmacy_template_content");
						log.debug("templateContent="+ templateContent);
						templateMode = (String) templateBean.get("template_mode");
					}

					StringReader reader = new StringReader(templateContent);
				//	t = new Template("CustomTemplate.ftl", reader, AppInit.getFmConfig());
					ftlGen = new FtlReportGenerator("CustomTemplate",reader);
				}
			//	t.process(params,writer);
				ftlGen.setReportParams(params);
				ftlGen.process(writer);
				String printContent = writer.toString();
				HtmlConverter hc = new HtmlConverter();
				boolean repeatPatHeader = ( printprefs != null ? ((String)printprefs.get("repeat_patient_info")).equals("Y") : false );
				if (printprefs != null && printprefs.get("print_mode") != null &&
				    printprefs.get("print_mode").equals("P")) {
					OutputStream os = response.getOutputStream();
					response.setContentType("application/pdf");
					try {
						if (templateMode != null && templateMode.equals("T")){
							hc.textToPDF(printContent, os, printprefs);
						}else{
							hc.writePdf(os, printContent, "Pharmacy Bill", printprefs, false, repeatPatHeader, true, true, true, false);
						}
					} catch (Exception e) {
						response.reset();
						log.error("Original Template:");
						log.error("Generated HTML content:");
						log.error(printContent);
						throw(e);
					}
					os.close();

					return null;
				} else {
					String textReport = null;
					//text mode
					if (templateMode != null && templateMode.equals("T")){
						textReport = printContent;
					}else{
						textReport = new String(hc.getText(printContent, "Pharmacy Bill", printprefs, true, true));
					}
					request.setAttribute("textReport", textReport);
					if (printprefs != null) {
					  request.setAttribute("textColumns", printprefs.get("text_mode_column"));
					}
					request.setAttribute("printerType", "DMP");
					return mapping.findForward("textPrintApplet");
				}

			} else {
				return null;
			}
		} finally {
			con.close();
		}
	}

	/*
	 * This is called on submit of the StockReorder screen. A list of items and order quantity
	 * is sent to us.
	 */
	@IgnoreConfidentialFilters
	public ActionForward goPOScreenWithExistingItems(ActionMapping m, ActionForm f,
			HttpServletRequest request, HttpServletResponse res) throws Exception {

		Map paramMap = request.getParameterMap();

		String[] itemIdStr = (String[]) paramMap.get("itemId");
		String[] quantities = (String[]) paramMap.get("orderqty");
		String[] selected = (String[]) paramMap.get("hselected");

		int storeId = Integer.parseInt(request.getParameter("store_id"));
		
		String poNo = request.getParameter("poNo");
		String supplierId = request.getParameter("supplier_id");

		// todo: required?
		String dept = request.getParameter("dept");
		String remarks = request.getParameter("remarks");

		BasicDynaBean centerIdBean = UserDAO.getCenterId(storeId);
		int centerId = (Integer)centerIdBean.get("center_id");
		List centerStores = new StockEntryAction().getAllStores(centerId);

		/*
		 * We create a flash scope and redirect to the PO screen. That screen will take
		 * care of populating items in the flash scope as additional items over and above
		 * what is there in the PO (if it is there), or create new rows for them in the
		 * PO. Here, we just supply a list of medicines and corresponding quantity to
		 * be added to the PO. Items have to be like "new" items as in getMedDetails.
		 */
		int elelen = itemIdStr.length;
		log.debug("num SR items: " + elelen);
		HashMap<Integer, BigDecimal> qtyMap = new HashMap<Integer, BigDecimal>();
		ArrayList<Map> itemsList = new ArrayList<Map>();

		for (int i=0;i<elelen;i++) {
			if (!selected[i].equalsIgnoreCase("true"))
				continue;

			int itemId = Integer.parseInt(itemIdStr[i]);
			qtyMap.put(itemId, new BigDecimal(quantities[i]));
			log.debug("qty for item " + itemId + ": " + quantities[i]);
			// we can't get all items in one query since the appropriate cost_price calcluation
			// is complicated to do in a single query for multiple items. So we do this also
			// inside the loop.
			BasicDynaBean item = PurchaseOrderDAO.getItemDetailsForPo(itemId, storeId, supplierId, centerStores,(Integer)request.getSession().getAttribute("centerId"));
			List<BasicDynaBean> suppRateList = supContractItemDao.getSupplierItemRateValue(itemId,supplierId, centerId);
		    Map<String, Object> resultMap = null;
		   
		    if(item != null){
		    	resultMap = getSupplierRateDetails(item, suppRateList, centerId);
			}
			itemsList.add(resultMap);
		}

		JSONSerializer js = new JSONSerializer().exclude("class");
		FlashScope flash = FlashScope.getScope(request);
		flash.put("additionalItemsJSON", js.deepSerialize(itemsList));
		flash.put("additionalQtysJSON", js.deepSerialize(qtyMap));
		// temp for testing
		/*
		HttpSession session = request.getSession();
		session.setAttribute("additionalItemsJSON", js.deepSerialize(itemsList));
		session.setAttribute("additionalQtysJSON", js.deepSerialize(qtyMap));
		*/

		ActionRedirect redirect = new ActionRedirect(m.findForward("poscreenRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("poNo", poNo);
		redirect.addParameter("supplier_id", supplierId);
		redirect.addParameter("store_id", storeId);

		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward getCopyPoScreen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException {

        HttpSession session = request.getSession(false);
        String storeId = (String) session.getAttribute("pharmacyStoreId");
        String userName = (String) session.getAttribute("userid");
        int centerId = (Integer)request.getSession().getAttribute("centerId");
        request.setAttribute("listAllcentersforAPo", PurchaseOrderDAO.listsuppliersforAPo(centerId));
		String po_no = request.getParameter("poNo");
		String storeName = null;
		
		List<BasicDynaBean> originalPODetails = new PurchaseOrderDAO().getItemsOfPO(po_no);
		Map convertedPOItems = ConversionUtils.listBeanToMapBean(originalPODetails,"item_status");
		
		if ( convertedPOItems.get("A") == null){
			 request.setAttribute("noactiveitem", true);
		} else {
			 request.setAttribute("noactiveitem", false);
		}
		
		 JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("listAllcentersforAPo", js.deepSerialize(PurchaseOrderDAO.listAllcentersforAPo(centerId)));

        if (storeId!=null && !storeId.equals("")) {
        	BasicDynaBean dept = new GenericDAO("stores").findByKey("dept_id", Integer.parseInt(storeId));
        	storeName = dept.get("dept_name").toString();
		}
		request.setAttribute("store_id", storeId);
		request.setAttribute("store_name", storeName);

        BasicDynaBean pobean = storePOMainDAO.findByKey("po_no", po_no);
        request.setAttribute("pobean", pobean);

        BasicDynaBean suppBean = new GenericDAO("supplier_master").findByKey("supplier_code",
				pobean.get("supplier_id"));
		request.setAttribute("supplier_address", suppBean != null ? suppBean.get("supplier_address") : null );
		return mapping.findForward("copyPoScreen");
	}

	public ActionForward copyPO(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, java.io.IOException {

		ActionRedirect redirect = new ActionRedirect(m.findForward("poscreenRedirect"));
		String username = (String) req.getSession(false).getAttribute("userid");
		int centerId = RequestContext.getCenterId();
		
		Connection con = null;
		boolean success = false;
		String origPoNo = req.getParameter("po_no");
		int storeId = Integer.parseInt(req.getParameter("store_id"));

		PurchaseOrderDAO poDAO = new PurchaseOrderDAO();
		BigDecimal poapplimit = GenericPreferencesDAO.getGenericPreferences().getPoApprovalLimit();

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			StringBuilder inactiveMedicines = new StringBuilder();
			boolean emptyCopyPO = true;
			
			FlashScope flash = FlashScope.getScope(req);

			BasicDynaBean poBean = storePOMainDAO.findByKey("po_no", origPoNo);
			List<BasicDynaBean> originalPODetails = new PurchaseOrderDAO().getItemsOfPO(origPoNo);
			
			if ( originalPODetails != null && originalPODetails.size() > 0 ){
				//few items must be inactive
				for (BasicDynaBean inactiveItems : originalPODetails){
					
					if ( inactiveItems.get("item_status").equals("A")){
						emptyCopyPO = false;
						continue;
					}
					inactiveMedicines.append("</br>");
					inactiveMedicines.append((String)inactiveItems.get("medicine_name"));
				}
			}
			
			if ( emptyCopyPO ){
				flash.info("All the items of Original PO " + origPoNo +" are inactive,failed to Copy PO");
				redirect = new ActionRedirect(m.findForward("viewPORedirect"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				success = false;
				return redirect;
			}
			
			//after copy po, PO is like a new PO.No need to carry forward Amend related info of existing PO.  
			eraseAmendDetailsForCopyPO(poBean);
			poBean.set("amended_reason","");
			poBean.set("approved_time", null);
			poBean.set("validated_by", "");
			poBean.set("validated_time", null);
			
			
			String newPoNo = PurchaseOrderDAO.getNextId("" + storeId);
			ConversionUtils.copyToDynaBean(req.getParameterMap(), poBean, null, true);
			// following not to be copied
			poBean.set("po_no", newPoNo);
			poBean.set("actual_po_date", DateUtil.getCurrentTimestamp());
			poBean.set("user_id", username);
			poBean.set("last_modified_by", username);
			storePOMainDAO.insert(con, poBean);
			PurchaseOrderDAO.copyPoDetail(con, origPoNo, newPoNo);
			/**
			 * In case selected store has different store tarriff
			 * vat,vat_rate,vat_type can be different hense recalculating po_total
			 */
			List<BasicDynaBean> copiedPODetails = storePODAO.findAllByKey(con, "po_no", newPoNo);
			BigDecimal poTotal = BigDecimal.ZERO;
			GenericDAO poTaxDAO = new GenericDAO("store_po_tax_details");
      for (BasicDynaBean poItemBean : copiedPODetails) {
        BigDecimal medTotal = BigDecimal.ZERO;
        try {
          List<Map<Integer, Object>> allTaxesList = (List<Map<Integer, Object>>) purchaseOrderService
              .getTaxDetails(poBean, poItemBean).get("tax_details");
          BigDecimal netTaxRate = BigDecimal.ZERO;
          BigDecimal netTaxAmt = BigDecimal.ZERO;
          List<BasicDynaBean> taxBeanList = new ArrayList<BasicDynaBean>();
          for (Map<Integer, Object> taxObj : allTaxesList) {
            BasicDynaBean taxBean = poTaxDAO.getBean();
            for (Object obj : taxObj.values()) {
              Map<String, String> taxMap = (Map<String, String>) obj;
              taxBean.set("po_no", newPoNo);
              taxBean.set("medicine_id", poItemBean.get("medicine_id"));
              taxBean.set("item_subgroup_id",
                  Integer.parseInt(taxMap.get("tax_sub_group_id")));
              BigDecimal taxRate = new BigDecimal(taxMap.get("rate"));
              taxBean.set("tax_rate", taxRate);
              netTaxRate = netTaxRate.add(taxRate);
              BigDecimal taxAmt = new BigDecimal(taxMap.get("amount"));
              netTaxAmt = netTaxAmt.add(taxAmt);
              taxBean.set("tax_amt", taxAmt);
              taxBeanList.add(taxBean);
            }
          }
          if(!taxBeanList.isEmpty()) {
            poTaxDAO.insertAll(con, taxBeanList);
          }
          medTotal = (((BigDecimal) poItemBean.get("cost_price"))
              .multiply((BigDecimal) poItemBean.get("qty_req"))).add(netTaxAmt);
          
          Map keys = new HashMap();
          
          keys.put("po_no", poItemBean.get("po_no"));
          keys.put("medicine_id", poItemBean.get("medicine_id"));
          keys.put("item_order", poItemBean.get("item_order"));
          poDAO.updatePOItemsVat(con,keys, medTotal, netTaxRate, netTaxAmt);
        } catch (Exception e) {
          log.error("Error happend while getting the tax details for medicine_id :"
              + poItemBean.get("medicine_id"));
          e.printStackTrace();
        }
        poTotal = poTotal.add(medTotal);
      }
			poBean.set("po_total", poTotal);

			//status to be set based on po approval limit
			if ( ((BigDecimal)poBean.get("po_total")).compareTo(poapplimit) > 0) {
				poBean.set("status", "O");
				poBean.set("approver_remarks", "");
				poBean.set("approved_by", null);//approved by is not valid for an open PO
			} else {
				poBean.set("status", "A");
				poBean.set("approver_remarks", "Automatic PO Approval");
			}
			storePOMainDAO.update(con, poBean.getMap(), "po_no",newPoNo);

			//update all po items status to main status
			poDAO.updatePOItemsStatus(con, newPoNo, (String)poBean.get("status"));

			redirect.addParameter("poNo", newPoNo);
			

			if ( originalPODetails != null && originalPODetails.size() > 0 && inactiveMedicines.length() > 1){
				//few items must be inactive
				flash.info("The following item/s are inactive in the item master and not copied in the new copied PO: "+inactiveMedicines);
			} else {
				flash.info("Purchase Order " + newPoNo + " generated successfully");
			}
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

			success = true;
			return redirect;

		} finally {
			DataBaseUtil.commitClose(con, success);
		}
	}
	
	public void eraseAmendDetailsForCopyPO(BasicDynaBean poBean){
		
		
		poBean.set("amendment_time", null);
		poBean.set("amendment_validated_time",null);
		poBean.set("amendment_approved_time", null);
		poBean.set("amended_by","");
		poBean.set("amendment_validated_by","");
		poBean.set("amendment_approved_by","");
		poBean.set("amendment_validator_remarks","");
		poBean.set("amendment_approver_remarks","");
		
	}
	
	public  ActionForward insertRemarks(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response) throws Exception {
		
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("poscreenRedirect"));
		Connection con = null;
		try {
			Map itemsMap = getParameterMap(request);
			String grn_count = getParameter(itemsMap, "grn_count");
			String poNO = getParameter(itemsMap, "po_no");
			String remarks =  getParameter(itemsMap, "remarks");
			String status[] =  (String[]) itemsMap.get("status_fld");
			
			con = DataBaseUtil.getReadOnlyConnection();
			Map<String, String> columndata = new HashMap<String, String>();
			columndata.put("remarks", remarks);
			if(status != null && poNO != null){
				columndata.put("status", status[0].trim());
				Map<String, String> keyData = new HashMap<String, String>();
				keyData.put("po_no", poNO);
				int updatedColumns = storePOMainDAO.update(con, columndata, keyData);
			}
			redirect.addParameter("poNo", poNO);
			redirect.addParameter("grn_count", grn_count);
			return redirect;

		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
		
	}
	
	@IgnoreConfidentialFilters
	public ActionForward getUploadedQuotationCopy(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp) throws Exception {

		String poNo = req.getParameter("purordNo");
		String fileName = "";
		String contentType = "";
		if (poNo == null) {
			return m.findForward("error");
		}

		Map<String,Object> uploadMap = PurchaseOrderDAO.getUploadedDocInfo(poNo);

		if (uploadMap.isEmpty()) {
			return m.findForward("error");
		}

		fileName = (String)uploadMap.get("filename");
		contentType = (String)uploadMap.get("contenttype");
		resp.setContentType(contentType);
		if (!fileName.equals("")) {
			resp.setHeader("Content-disposition", "attachment; filename=\""+fileName+"\"");
		}

		OutputStream os = resp.getOutputStream();
		InputStream s = (InputStream) uploadMap.get("uploadfile");
		if (s != null) {
			byte[] bytes = new byte[4096];
			int len = 0;
			while ( (len = s.read(bytes)) > 0) {
				os.write(bytes, 0, len);
			}
		}

		os.flush();
		if (s != null) {
		  s.close();
		}
		return null;
	}
	
	private Map getSupplierRateDetails(BasicDynaBean item, List<BasicDynaBean> suppRateList, Integer centerId) throws SQLException {

		Map<String, Object> resultMap = null;
		if (item != null) {
			resultMap = new HashMap<String, Object>();
			resultMap.putAll(item.getMap());
			if (suppRateList != null && suppRateList.size() > 0) {
				BasicDynaBean supplyRate =  suppRateList.get(0);
				resultMap.put("cost_price", supplyRate.get("supplier_rate"));
				for (BasicDynaBean supplierRate : suppRateList) {
					Object dis = supplierRate.get("discount");
					if (dis != null) {
						resultMap.put("discount", new BigDecimal(dis.toString()));
						break;
					} else
						continue;
				}
				for (BasicDynaBean supplierRate : suppRateList) {
					Object mrp = supplierRate.get("mrp");
					if (mrp != null) {
						resultMap.put("mrp", new BigDecimal(mrp.toString()));
						break;
					} else
						continue;
				}
				for (BasicDynaBean supplierRate : suppRateList) {
				  Object margin = supplierRate.get("margin");
				  Object marginType = supplierRate.get("margin_type");
				  if (margin != null) {
				    resultMap.put("margin", new BigDecimal(margin.toString()));
				    resultMap.put("margin_type", marginType);
				    break;
				  } else
				    continue;
				}
				resultMap.put("supplier_rate_validation", "true");
			}
		}
		
		List<BasicDynaBean> minRateContracts = supContractItemDao
        .getMinimumRateSupplierContracts((Integer)item.get("medicine_id"), centerId);
    if (minRateContracts != null && !minRateContracts.isEmpty()) {
      List<String> minRateSuppliers = new ArrayList<>();
      for (BasicDynaBean contract : minRateContracts) {
        minRateSuppliers
            .add((String) (StringUtils.isNotEmpty((String) contract.get("supplier_name"))
                ? contract.get("supplier_name") : "Default Supplier"));
      }
      resultMap.put("min_rate_suppliers", minRateSuppliers);
      resultMap.put("min_rate", minRateContracts.get(0).get("supplier_rate"));
      resultMap.put("discounted_min_rate", minRateContracts.get(0).get("discounted_supplier_rate"));
    }
		return resultMap;
	}
}