package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.integration.scm.inventory.ScmOutBoundInvService;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.modules.ModulesDAO;
import com.insta.hms.usermanager.Role;
import com.insta.hms.usermanager.UserDashBoardDAO;
import com.lowagie.text.DocumentException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
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
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import flexjson.JSONSerializer;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class StoresIndentProcessAction extends BaseAction{

	static final Logger log = LoggerFactory.getLogger(StoresIndentProcessAction.class);

	private static final GenericDAO mdao = new GenericDAO("store_indent_main");
	private static final StoresIndentDAO dao = new StoresIndentDAO();
  	private static final ModulesDAO modules = new ModulesDAO();
    private static final GenericDAO storeIndentDetailsDAO = new GenericDAO("store_indent_details");

  	private static ScmOutBoundInvService scmOutService = ApplicationContextProvider
    	.getBean(ScmOutBoundInvService.class);

	@IgnoreConfidentialFilters
	public ActionForward list(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException, Exception {

		Map<Object,Object> map= getParameterMap(req);
		PagedList pagedList = null;
		HttpSession session = req.getSession(false);
		String dept_id = (String) session.getAttribute("pharmacyStoreId");
		/** check if user has access right to view data of all stores*/
		HashMap actionRightsMap = new HashMap();
		Object roleID = null;
		Role role=new Role();
		actionRightsMap= (HashMap) req.getSession(false).getAttribute("actionRightsMap");
		roleID=  req.getSession(false).getAttribute("roleId");
		String actionRightStatus=(String) session.getAttribute("multiStoreAccess");
		if (actionRightStatus==null)
			actionRightStatus="N";
		int roleId = ((Integer)roleID).intValue();
		if ((actionRightStatus.equals("A")) || (roleId ==1 || (roleId==2))){
			/** User has access to all stores or he is in admin role..let him see all*/
			if ((roleId != 1) && (roleId != 2)){
				/** User has multistore access but not in Admin or InstaAdmin Role
				 *
				 */
				if (!map.containsKey("indent_store")){
					/** if not searching by specific store, show indents belonging to all assigned stores*/
					List loggedStores = StoresDBTablesUtil.getLoggedUserStoreIds((String)req.getSession(false).getAttribute("userId"));
					if(!loggedStores.isEmpty()) {
						String[] loggedStoresAsParamArray = (String[])loggedStores.toArray(new String[0]);
						map.put("indent_store",loggedStoresAsParamArray);
					}else if (dept_id != null) {
						String[] loggedStoresAsParamArray = new String[]{dept_id};
						map.put("indent_store",loggedStoresAsParamArray);
					}
					map.put("indent_store@type",new String[] {"integer"});
				}
			}
			pagedList = StoresIndentDAO.searchIndentList(map, ConversionUtils.getListingParameter(map),null);
		} else{
			/** user can see data only pertaining to his store*/
			 if(dept_id!=null && !dept_id.equals("")){
				map.put("indent_store", new String[]{dept_id});
				map.put("indent_store@type",new String[] {"integer"});
				pagedList = StoresIndentDAO.searchIndentList(map, ConversionUtils.getListingParameter(map),null);
			} else{
				pagedList = new PagedList();
			}
		}
		req.setAttribute("pagedList", pagedList);
		List allUserNames = UserDashBoardDAO.getAllUserNames();
		req.setAttribute("userNameList", new JSONSerializer().serialize(allUserNames));
		//req.setAttribute("itemNames", StoresDBTablesUtil.getTableDataInJSON(StoresDBTablesUtil.GET_MEDICINE_NAMES_IN_MASTER));

		int centerId = (Integer)req.getSession().getAttribute("centerId");
		boolean multiCentered = GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1;
		if(GenericPreferencesDAO.getGenericPreferences().getAllow_cross_center_indents().equals("N")) {
			HashMap filterMap = new HashMap();
			filterMap.put("status", "A");
			if(centerId !=0) {
				filterMap.put("center_id",centerId);
			}
			List columns = new ArrayList();
			columns.add("dept_name");
			columns.add("dept_id");
			List l = new GenericDAO("stores").listAll(columns, filterMap, "dept_name");
			req.setAttribute("storesList", l);
		}
		req.setAttribute("multiCentered", GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1 );
		req.setAttribute("centers", CenterMasterDAO.getAllCentersExceptSuper());
		req.setAttribute("wards",BedMasterDAO.getAllWardNames( centerId,multiCentered ));
		req.setAttribute("master_timestamp", PharmacymasterDAO.getItemMasterTimestamp());
		return am.findForward("list");
	}

	@IgnoreConfidentialFilters
	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		int indent_no = Integer.parseInt(req.getParameter("indent_no"));

		BasicDynaBean indentdetails = StoresIndentDAO.getIndentApprovalRejectDetails(indent_no);
		req.setAttribute("indentdetails", indentdetails);
		int store_id  = 0;
		List<BasicDynaBean> indentlist = null;

		store_id  = Integer.parseInt(indentdetails.get("indent_store").toString());
		String indenttype = (String)indentdetails.get("indent_type");
		BasicDynaBean indentBean = mdao.findByKey("indent_no", indent_no);
		String deptFrom = ( indentBean != null && ((String) indentBean.get("indent_type")).equals("S")) ? (String) indentBean.get("dept_from") : "-999";
		indentlist = StoresIndentDAO.getIndentItemDetailsForProcess(indent_no,store_id,"trx", Integer.parseInt(deptFrom));

		req.setAttribute("indentlist", indentlist);

		HttpSession session = req.getSession(false);

       	BasicDynaBean dept = new GenericDAO("stores").findByKey("dept_id", store_id);
       	String dept_name = dept.get("dept_name").toString();
       	req.setAttribute("dept_id", String.valueOf(store_id));
        req.setAttribute("dept_name", dept_name);

		List l = StoresIndentDAO.getItemIdentifierDetails(indent_no,store_id);
		req.setAttribute("identifierList", l);
		if (indenttype.equals("S")){
			String storeName  = StoresIndentDAO.getStoreName( Integer.parseInt(indentdetails.get("dept_from").toString()));
			req.setAttribute("storeName", storeName);
		}
		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
	throws IOException, ServletException, Exception {
		Connection con = null;
		ActionRedirect redirect = null;
		FlashScope flash = null;
		String msg = null;
		String issuemessage = null;
		String transfermessage = null;
		int indent_no = 0;
		boolean success = false;
    ArrayList<String> failedItems = new ArrayList<String>();
    List<Map<String, Object>> cacheTransferTxns = new ArrayList<>();

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			flash = FlashScope.getScope(req);
			redirect = new ActionRedirect(m.findForward("listRedirect"));
			
      BasicDynaBean module = modules.findByKey("module_id", "mod_scm");

			String status = req.getParameter("status_main");
			indent_no = Integer.parseInt(req.getParameter("indentNo"));
			HttpSession session = req.getSession(false);
			String userid = (String)session.getAttribute("userid");
			String remarks = req.getParameter("remarks");
			msg = "Proccessing failed for Indent No: "+indent_no;
			String[] purchase = req.getParameterValues("purchaseSelect");
			String[] oldPurchase = req.getParameterValues("oldPurchaseSelect");
			String[] medName = req.getParameterValues("medicine_name");
			String emptyrows = req.getParameter("emptyrows");
			String[] itemid = req.getParameterValues("itemid");
			int store_id = 0;
			int fromStore = 0;
			BasicDynaBean indentBean = mdao.findByKey("indent_no", indent_no);
			String issueType = (String) indentBean.get("indent_type");
			/** Update Stock Details based on general prefs.*/
			String receiveprocess = (String)new GenericDAO("generic_preferences").getRecord().getMap().get("receive_transfer_indent");
			if ((null == receiveprocess) || (receiveprocess.equals(""))){
				receiveprocess = "N";
			}

			if(status.equals("X")) {
				success = StoresIndentProcessDAO.updateIndentStatus(con,indent_no, userid, remarks, status, issueType);
				if(success) {
					msg = "Status Updated for Indent No: "+indent_no;
					for (int j = 0; j< medName.length; j++) {
						Map<String, Object> fields = new HashMap<String, Object>();
						Map<String, Object> keys = new HashMap<String, Object>();
						keys.put("indent_no", indent_no);
						keys.put("medicine_name", medName[j]);
						fields.put("status", "C");
						if(storeIndentDetailsDAO.update(con, fields, keys) > 0) {
							success = true;
						} else {
							success = false;
						}
					}
				}
				BasicDynaBean indentMainBean = mdao.findByKey("indent_no", indent_no);
				if(indentMainBean.get("status").equals("P") || indentMainBean.get("status").equals("C")){
				  success = false;
				}
				if(success){
				  con.commit();
				} else {
					msg = "Status Updation Failed for Indent No: "+indent_no;
					con.rollback();
				}

				flash.put("success", msg);
				redirect.addParameter("_success",msg);
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				redirect.addParameter("status","A");
				return redirect;

			} else if(status.equals("P")) {
				success = StoresIndentProcessDAO.updateIndentStatus(con,indent_no, userid, remarks, status, issueType);

				if(success && emptyrows.equalsIgnoreCase("N")) {
					String indentSelect[] = req.getParameterValues("indentSelect");
					List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();
					Map<String, Object> itemBatchQtyMap = null;
					Map<String, Object> qtyBatchMap = null;
					List<Map<String, Object>> l = null;
					if(medName.length > 0) {
						for (int i = 0; i < medName.length; i++) {
							String[] qty = req.getParameterValues("item_qty" + i);
							String[] itemBatchId = req.getParameterValues("item_batch_id" + i);
							//String[] identifier = req.getParameterValues("item_identifier" + i);
							String[] qtyavlbl = req.getParameterValues("item_qty_avlbl" + i);
							String[] qtyinuse = req.getParameterValues("item_qty_in_use" + i);
							l = new ArrayList<Map<String, Object>>();
							if(indentSelect[i].equals("true")) {
								if (qty != null && qty.length > 0) {
									for (int j = 0; j < qty.length; j++) {
										if (!qty[j].equals("0") && !qty[j].equals("")) {
											qtyBatchMap = new HashMap<String, Object>();
											qtyBatchMap.put("QTY", qty[j]);
											qtyBatchMap.put("IDENTIFIER", itemBatchId[j]);
											qtyBatchMap.put("AVBL_QTY", qtyavlbl[j]);
											qtyBatchMap.put("QTY_IN_USE", qtyinuse[j]);
											l.add(qtyBatchMap);
										}
									}
								}
								itemBatchQtyMap = new HashMap<String, Object>();
								String itemId = "";
								if ((null != itemid) && (!(itemid[i].equals("")))) {
									itemId = (String)itemid[i];
								}
								itemBatchQtyMap.put("MEDICINE_ID", itemId);
								itemBatchQtyMap.put("MEDICINE_NAME", medName[i]);
								itemBatchQtyMap.put("ITEM_LIST", l);
								itemList.add(itemBatchQtyMap);
							}
						}
					}
					
					if (issueType.equals("U")) {
						store_id = (Integer)indentBean.get("indent_store");
						BigDecimal userIssueid = StoresIndentProcessDAO.userIssue(con,indentBean, itemList, remarks, userid);
						issuemessage = userIssueid != null ? userIssueid.toString() : null;
						if(issuemessage != null && !issuemessage.equalsIgnoreCase("0") ) {
							success = true;
							msg = "Proccessing successful...";
						}else success = false;
					} else if (issueType.equals("S")) {
						store_id = (Integer)indentBean.get("indent_store");
						fromStore = Integer.parseInt((String)indentBean.get("dept_from"));
						int transferid = StoresIndentProcessDAO.stockTransfer(con,indentBean, itemList, remarks, userid,receiveprocess,failedItems, cacheTransferTxns);
						transfermessage = new Integer(transferid).toString();
						if(!transfermessage.equalsIgnoreCase("0") ) {
							success = true;
							msg = "Proccessing successful...";
						}else {
							success = false;
							transfermessage = null;
						}
					}
					for (int j = 0; j< medName.length; j++) {
						Map<String, Object> fields = new HashMap<String, Object>();
						Map<String, Object> keys = new HashMap<String, Object>();
						if(!oldPurchase[j].equals(purchase[j])) {
							fields.put("purchase_flag", purchase[j].equalsIgnoreCase("true") ? "Y" : "N");
							fields.put("purchase_flag_date", purchase[j].equalsIgnoreCase("true") ? DataBaseUtil.getDateandTime() : null);
							keys.put("indent_no", indent_no);
							keys.put("medicine_name", medName[j]);
							if (success) {
								success = storeIndentDetailsDAO.update(con, fields, keys) > 0;
								msg = "Processing successful...";
							}
						}
					}

				} else {
					for (int j = 0; j< medName.length; j++) {
						Map<String, Object> fields = new HashMap<String, Object>();
						Map<String, Object> keys = new HashMap<String, Object>();
						if(!oldPurchase[j].equals(purchase[j])) {
							fields.put("purchase_flag", purchase[j].equalsIgnoreCase("true") ? "Y" : "N");
							fields.put("purchase_flag_date", purchase[j].equalsIgnoreCase("true") ? DataBaseUtil.getDateandTime() : null);
							keys.put("indent_no", indent_no);
							keys.put("medicine_name", medName[j]);
							if (success) success = storeIndentDetailsDAO.update(con, fields, keys) > 0;
						}
					}
					if (success) msg = "Indent Items Purchase Status Updates Successful...";
				}
				BasicDynaBean indentMainBean = mdao.findByKey("indent_no", indent_no);
        if(indentMainBean.get("status").equals("X") || indentMainBean.get("status").equals("C")){
          success = false;
        }
			}
			if (success) {
				flash.put("report", "true");
				flash.put("indent_no", indent_no);
				con.commit();
		    redirect.addParameter("_transfermessage",transfermessage);
		    redirect.addParameter("_issuemessage",issuemessage);
		    
        if (!cacheTransferTxns.isEmpty() && module != null
            && ((String) module.get("activation_status")).equals("Y")) {
          scmOutService.scheduleStockTransferTxns(cacheTransferTxns);
        }
				//update stock timestamp
				StockFIFODAO stockFIFODAO = new StockFIFODAO();
				stockFIFODAO.updateStockTimeStamp();
				stockFIFODAO.updateStoresStockTimeStamp(store_id);
				stockFIFODAO.updateStoresStockTimeStamp(fromStore);
				//if (con!=null) con.close();
			} else {
				log.error("Error in transaction, rolling back");
				con.rollback();
				msg = "Status Updation Failed for Indent No: "+indent_no;
				//if (con!=null) con.close();
			}
			if(success) {
				msg = StoresIndentProcessDAO.updateIndentFullfilledStatus(indent_no, msg, userid, issueType, receiveprocess);
			}
	} catch (Exception e) {
		success = false;
		log.error("Caught exception, rolling back", e);
		con.rollback();
	} finally {
		redirect.addParameter("_success",msg);
		String insuffInfo = "";
		for(String failedItem:failedItems){
			insuffInfo = insuffInfo.concat(failedItem).concat("</br>");
		}
		flash.put("info", insuffInfo);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("status","A");
		redirect.addParameter("sortOrder","indent_no");
		redirect.addParameter("sortReverse",true);
		redirect.addParameter("_indentno",indent_no);

		if (con!=null) con.close();
	}
	return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward generateReport (ActionMapping mapping,ActionForm af,
			HttpServletRequest request,HttpServletResponse response) throws SQLException,IOException,TemplateException,
			XPathExpressionException, DocumentException, TransformerException{

		Connection con = null;
		Template t = null;
		HashMap params = new HashMap();
		int store_id = 0;
		int indent_no = Integer.parseInt(request.getParameter("indent_no"));
		BasicDynaBean indentdetails = StoresIndentDAO.getIndentApprovalRejectDetails(indent_no);
		List<BasicDynaBean> indentlist = null;
		store_id  = Integer.parseInt(indentdetails.get("indent_store").toString());
		BasicDynaBean indentBean = mdao.findByKey("indent_no", indent_no);
		String deptFrom = ( indentBean != null && ((String) indentBean.get("indent_type")).equals("S")) ? (String) indentBean.get("dept_from") : "-999";
		indentlist = StoresIndentDAO.getIndentItemDetailsForProcess(indent_no,store_id,"print", Integer.parseInt(deptFrom));


		request.setAttribute("indentdetails", indentdetails);
		//List l = StoresIndentDAO.getItemIdentifierDetails(indent_no,store_id);

		String indenttype = (String)indentdetails.get("indent_type");
		if (indenttype.equals("S")){
			String storeName  = StoresIndentDAO.getStoreName( Integer.parseInt(indentdetails.get("dept_from").toString()));
			params.put("storeName", storeName);
		}

		params.put("indentdetails", indentdetails);
		params.put("indentlist", indentlist);

		BasicDynaBean printprefs = PrintConfigurationsDAO.getPageOptions(
				PrintConfigurationsDAO.PRINT_TYPE_STORE);

		PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();
		String templateContent = null;
		//t = AppInit.getFmConfig().getTemplate("ProcessedIndentPrint.ftl");
		templateContent = printtemplatedao.getCustomizedTemplate(PrintTemplate.Process_indent);
		if (templateContent == null || templateContent.equals("")) {
			t = AppInit.getFmConfig().getTemplate(PrintTemplate.Process_indent.getFtlName() + ".ftl");
		} else
		{
			StringReader reader = new StringReader(templateContent);
			t = new Template("ProcessedIndentPrint.ftl", reader, AppInit.getFmConfig());
		}
		
		StringWriter writer = new StringWriter();
		t.process(params,writer);
		String printContent = writer.toString();
		HtmlConverter hc = new HtmlConverter();
		if (printprefs.get("print_mode").equals("P")) {
			OutputStream os = response.getOutputStream();
			response.setContentType("application/pdf");
			hc.writePdf(os, printContent, "processindent", printprefs, false, false, true, true, true, false);
			}else {
				String textReport = null;
				textReport = new String(hc.getText(printContent, "processindentText", printprefs, true, true));
				request.setAttribute("textReport", textReport);
				request.setAttribute("textColumns", printprefs.get("text_mode_column"));
				request.setAttribute("printerType", "DMP");
				return mapping.findForward("textPrintApplet");
			}

		return null;
	}
}