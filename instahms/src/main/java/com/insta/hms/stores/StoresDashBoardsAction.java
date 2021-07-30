package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.BillChargeClaimDAO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.inventory.StoresHelper;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.POPrintTemplate.POPrintTemplateDAO;
import com.insta.hms.master.PharmacyPrintTemplate.PharmacyPrintTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.usermanager.Role;
import com.insta.hms.usermanager.UserDAO;
import flexjson.JSONSerializer;
import freemarker.template.Template;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class StoresDashBoardsAction extends BaseAction {
    static Logger log = LoggerFactory.getLogger(StoresDashBoardsAction.class);
     static JSONSerializer js = new JSONSerializer().exclude("class");
     private static SalesClaimDetailsDAO salesClaimDAO = new SalesClaimDetailsDAO();
     private static PatientInsurancePlanDAO insPlanDAO = new PatientInsurancePlanDAO();
     private static StoresPOApprovalDAO poDetailsDAO = new StoresPOApprovalDAO();
     private static GenericDAO grnPrintTemplateDao = new GenericDAO("grn_print_template");
     private static GenericDAO storeSalesMainDao = new GenericDAO("store_sales_main");
     
     private static final GenericDAO genericPreferencesDAO = new GenericDAO("generic_preferences");
     private static final GenericDAO storesDAO = new GenericDAO("stores");
     private static final GenericDAO storeHospUserDAO = new GenericDAO("store_hosp_user");
     private static final GenericDAO uUserDAO = new GenericDAO("u_user");
     private static final GenericDAO storeSalesDetailsDAO = new GenericDAO("store_sales_details");
     private static final GenericDAO salesClaimTaxDetailsDAO = new GenericDAO("sales_claim_tax_details");

     
     
    /** Screen: View GRN, Method: getGrns, Action Id: pharma_view_grns, Path: /pages/stores/viewgrns.
     */
     @IgnoreConfidentialFilters
    public  ActionForward getGrns(ActionMapping mapping,ActionForm fm,
            HttpServletRequest request,HttpServletResponse response) throws Exception {

        Map<Object,Object> map = getParameterMap(request);
        HttpSession session = request.getSession(false);
        String storeFilter = request.getParameter("dept_id");
        String dept_id = (String)session.getAttribute("pharmacyStoreId");

        if (storeFilter == null || storeFilter.isEmpty()) {
            map.put("dept_id", new String[] {dept_id});
            map.put("dept_id@type", new String[] {"integer"});
        }
        
        request.setAttribute("default_store", dept_id);

        String user = (String)request.getSession(false).getAttribute("userid");
        PagedList list = StoresDashBoardsDAO.searchGRNS(map, ConversionUtils.getListingParameter(map));
        request.setAttribute("pagedList", list);
        BasicDynaBean hosp = genericPreferencesDAO.getRecord();
        if(hosp.get("hospital_tin") != null)
            request.setAttribute("hosp_tin",hosp.get("hospital_tin").toString());
        if(hosp.get("hospital_pan") != null)
            request.setAttribute("hosp_pan", hosp.get("hospital_pan").toString());
        if(hosp.get("hospital_service_regn_no") != null)
            request.setAttribute("hosp_ser_reg_no", hosp.get("hospital_service_regn_no").toString());
        if(hosp.get("stock_entry_agnst_do") != null)
            request.setAttribute("stock_entry_agnst_do", hosp.get("stock_entry_agnst_do").toString());
        
        request.setAttribute("grnPrintTemplates", ConversionUtils.listBeanToListMap(grnPrintTemplateDao
            .listAll(Arrays.asList(new String[] { "template_id", "template_name" }))));
        
        if(StringUtils.isNotEmpty(dept_id)){
          request.setAttribute("defaultGrnPrintTemplate", StoreDAO.findByStore(Integer.valueOf(dept_id)).get("grn_print_template"));          
        }else{
          request.setAttribute("defaultGrnPrintTemplate", "BUILTIN_HTML");
        }
        request.setAttribute("userCenterId",uUserDAO.findByKey("emp_username", user).get("center_id"));
        int centerId = (Integer) request.getSession(false).getAttribute("centerId");
        ArrayList deptids = (ArrayList)DataBaseUtil.queryToList("select regexp_split_to_table(multi_store,E'\\,') FROM u_user where emp_username=?", user);
        
        if (deptids.size() == 0)
            request.setAttribute("deptids", (ArrayList)DataBaseUtil.queryToList("select regexp_split_to_table(pharmacy_store_id,E'\\,') FROM u_user where emp_username=?", user));
        else
            request.setAttribute("deptids", deptids);
        
        BasicDynaBean printPrefBean = PrintConfigurationsDAO.getPrintMode(PrintConfigurationsDAO.PRINT_TYPE_STORE);
        request.setAttribute("printPref", printPrefBean);

        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DataBaseUtil.getConnection();
            request.setAttribute("suppliers", js.serialize(ConversionUtils.copyListDynaBeansToMap(new StoreDAO(con).getSupplierNames())));
        } finally {
            DataBaseUtil.closeConnections(con, ps);
        }
        return mapping.findForward("viewgrnscreen");
    }


    /** Screen: View POs, Method: getPOs, Action Id: pharma_view_po, Path: /pages/stores/viewpo.
     * @param mapping
     * @param fm
     * @param request
     * @param response
     * @return
     * @throws SQLException
     * @throws Exception
     */
     @IgnoreConfidentialFilters
    public  ActionForward getPOs(ActionMapping mapping,ActionForm fm,
            HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{
        GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
        Map map= getParameterMap(request);
        HttpSession session = request.getSession(false);
        String storeFilter = request.getParameter("store_id");
        String dept_id = (String)session.getAttribute("pharmacyStoreId");
        if (storeFilter == null) {
            map.put("store_id", new String[] {dept_id});
            map.put("store_id@type", new String[] {"integer"});
        }

        int centerId = (Integer) request.getSession(false).getAttribute("centerId");
        String[] selectedCenterId = (String[]) map.get("_center_id");
		if (centerId != 0) {
			map.put("center_id", new String[]{centerId+""});
			map.put("center_id@type", new String[]{"integer"});
		} else if ( selectedCenterId != null ) {
			map.put("center_id", selectedCenterId);
			map.put("center_id@type", new String[]{"integer"});
		}

        String userId = (String)session.getAttribute("userId");
        Integer roleId = (Integer)session.getAttribute("roleId");
        PagedList list = StoresDashBoardsDAO.searchPOs(map, ConversionUtils.getListingParameter(map));
        List dtoList = list.getDtoList();
        List loggedStores =  StoresDBTablesUtil.getLoggedUserStoreIds(userId);
        if(dtoList != null) {
            List entryRights = new ArrayList(dtoList.size());
            
            for (Map obj : (List<Map>) list.getDtoList()) {
                 
                if(roleId == 1 || roleId == 2) {
                    entryRights.add("Y");
                    continue;
                }
                if(obj != null && obj.get("store_id")!=null) {
                    if(loggedStores!= null && loggedStores.contains(obj.get("store_id").toString())) {
                        entryRights.add("Y");
                    } else {
                        entryRights.add("N");
                    }
                } else {
                    entryRights.add("N");
                }
               
            }
            List<String> storeUserList = UserDAO.getStoreUsers();
    		Iterator<String> storeUserIterator = storeUserList.iterator();
    		StringBuilder storeUsers = new StringBuilder();
    		while(storeUserIterator.hasNext()) {
    			storeUsers.append(storeUserIterator.next()).append(",");
    		}
    		List<String> storeUserWithAutoPOList = UserDAO.getStoreUsersWithAutoPO();
    		Iterator<String> storeUserWithAutoPOIterator = storeUserWithAutoPOList.iterator();
    		StringBuilder storeUserWithAutoPO = new StringBuilder();
    		while(storeUserWithAutoPOIterator.hasNext()) {
    			storeUserWithAutoPO.append(storeUserWithAutoPOIterator.next()).append(",");
    		}
    		
    		request.setAttribute("store_users", storeUsers.length() > 0 ? storeUsers.substring(0, storeUsers.length()-1) : "");
    		request.setAttribute("store_usersWithAutoPO", storeUserWithAutoPO.length() > 0 ? storeUserWithAutoPO.substring(0, storeUserWithAutoPO.length()-1) : "");
            request.setAttribute("entryRights", entryRights);
        }
        
        request.setAttribute("pagedList", list);
        BasicDynaBean hosp = genericPreferencesDAO.getRecord();
        if(hosp.get("hospital_tin") != null)
        request.setAttribute("hosp_tin",hosp.get("hospital_tin").toString());
        if(hosp.get("hospital_pan") != null)
        request.setAttribute("hosp_pan", hosp.get("hospital_pan").toString());
        if(hosp.get("hospital_service_regn_no") != null)
        request.setAttribute("hosp_ser_reg_no", hosp.get("hospital_service_regn_no").toString());
        List<String> columns = new ArrayList();
        columns.add("template_name");
        List<BasicDynaBean> templates = new POPrintTemplateDAO().listAll(columns);
        request.setAttribute("templates", templates);
        BasicDynaBean bean = PrintConfigurationsDAO.getPrintMode(PrintConfigurationsDAO.PRINT_TYPE_STORE);
        request.setAttribute("bean", bean);
        request.setAttribute("default_po_print_template", dto.getDefault_po_print_template());
        if(dept_id!=null && !dept_id.equals("")){
            BasicDynaBean dept = storesDAO.findByKey("dept_id", Integer.parseInt(dept_id));
            String dept_name = dept.get("dept_name").toString();
            request.setAttribute("dept_id", dept_id);
            request.setAttribute("dept_name", dept_name);
        }
        if(dept_id != null && dept_id.equals("")) {
            request.setAttribute("dept_id", dept_id);
        }
        
        ArrayList<String> SupplierNames = StockEntryDAO.getCenterMaterSupplierMaster(centerId);
        request.setAttribute("suppliers",SupplierNames);
        request.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
        request.setAttribute("centers", CenterMasterDAO.getAllCentersExceptSuper());
        return mapping.findForward("viewposcreen");
    }

    /** Screen: View POs, Method: savePOChanges, Action Id: pharma_view_po, Path: /pages/stores/viewpo.
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws SQLException
     * @throws Exception
     */
     @IgnoreConfidentialFilters
    public ActionForward savePOChanges(ActionMapping mapping ,ActionForm form,
            HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{

        List closePO = new ArrayList();
        List cancelPO = new ArrayList();
        List<ViewPO> updatePODetails = new ArrayList<ViewPO>();
        boolean status = true;
        String error = "Transaction failed";
        String success = "PO Details updated successfully";
        String[] pono = request.getParameterValues("_pono");
        String[] postatus = request.getParameterValues("_postatus");
        String[] close = request.getParameterValues("_hidclose");
        String[] cancel = request.getParameterValues("_hidcancel");
        String[] count = request.getParameterValues("_count");
        String user = null;
      
        Connection con = null;
        try {
            con = DataBaseUtil.getConnection();
            con.setAutoCommit(false);
            for (int i = 0; i < pono.length; i++) {
                // todo: should be handelled diff ,remove DTO.
                ViewPO po = new ViewPO();
                if (postatus[i].equalsIgnoreCase("O") || postatus[i].equalsIgnoreCase("AO")) {
                    if (close[i].equalsIgnoreCase("Y")) {
                        po.setPono(pono[i]);
                        po.setStatus("C");
                        po.setUserName("");
                        closePO.add(po);
                    }
                }
                if (postatus[i].equalsIgnoreCase("C") || postatus[i].equalsIgnoreCase("FC") || postatus[i].equalsIgnoreCase("X") ||Integer.parseInt(count[i]) > 0) {
                	if (close[i].equalsIgnoreCase("Y") && postatus[i].equalsIgnoreCase("A") && Integer.parseInt(count[i]) > 0) {
                        po.setPono(pono[i]);
                        po.setStatus("C");
                        po.setUserName("");
                        closePO.add(po);
                    }
                }else{
                    if (cancel[i].equalsIgnoreCase("Y")) {
                        po.setPono(pono[i]);
                        po.setStatus("X");
                        user = (String)request.getSession(false).getAttribute("userid");
                        po.setUserName(user);
                        cancelPO.add(po);
                    }
                }

                updatePODetails.add(po);
            }
            if (status) status = StoresDashBoardsDAO.updatePo(closePO,con);
            if (status) status = StoresDashBoardsDAO.updatePo(cancelPO,con);
            //update po details status to main status

            for(ViewPO podetail : updatePODetails ){
            	poDetailsDAO.updatePODetails(con,podetail);
            }
            if (status)	{
                con.commit();
                request.setAttribute("success", success);
            } else {
                con.rollback();
                request.setAttribute("error", error);
            }
        }catch (Exception e) {
            status = false;
            con.rollback();
            request.setAttribute("error", error);
        }
        finally {
            DataBaseUtil.closeConnections(con, null);
        }
        return mapping.findForward("savedpo");
    }

    /** Screen: View Stock Issues, Method: getStkIss, Action Id: stores_view_stock_issues,
     * Path: /pages/stores/viewstockissues.
     * @param mapping
     * @param fm
     * @param request
     * @param response
     * @return
     * @throws SQLException
     * @throws Exception
     */
    @IgnoreConfidentialFilters
    public  ActionForward getStkIss(ActionMapping mapping,ActionForm fm,
            HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{

        Map map= getParameterMap(request);
        PagedList list = StoresDashBoardsDAO.searchStkIss(map, ConversionUtils.getListingParameter(map));
        request.setAttribute("hospuserlist", js.serialize(ConversionUtils.copyListDynaBeansToMap(
            storeHospUserDAO.listAll(null, "status", "A", "hosp_user_name"))));
        request.setAttribute("pagedList", list);
        return mapping.findForward("viewstockissuescreen");
    }


    /** Screen: View Stock Issues for Patient, Method: getStkIssForPat, Action Id: stores_view_patient_issues,
     * Path: /pages/stores/viewpatientissues.
     * @param mapping
     * @param fm
     * @param request
     * @param response
     * @return
     * @throws SQLException
     * @throws Exception
     */
    public  ActionForward getStkIssForPatScreen(ActionMapping mapping,ActionForm fm,
            HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{
        return mapping.findForward("viewstockissuescreen");
    }

    @IgnoreConfidentialFilters
    public  ActionForward getStkIssForPat(ActionMapping mapping,ActionForm fm,
            HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{

        Map map= getParameterMap(request);
        PagedList list = StoresDashBoardsDAO.searchStkIssForPatient(map,
                ConversionUtils.getListingParameter(map));
        request.setAttribute("pagedList", list);
        return mapping.findForward("viewstockissuescreen");
    }

    /** Screen: View Stock Adjustments, Method: getStkAdj, Action Id: pharma_view_stock_adj,
     * Path: /pages/stores/viewstockadjustments.
     * @param mapping
     * @param fm
     * @param request
     * @param response
     * @return
     * @throws SQLException
     * @throws Exception
     */
    @IgnoreConfidentialFilters
    public  ActionForward getStkAdj(ActionMapping mapping,ActionForm fm,
            HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{

        Map map= getParameterMap(request);
        Connection con = null;
        PreparedStatement ps = null;
        try {
        con = DataBaseUtil.getConnection();
        HttpSession session = request.getSession(false);
        String dept_id = (String)session.getAttribute("pharmacyStoreId");
        if(dept_id!=null && !dept_id.equals("")){
            BasicDynaBean dept = storesDAO.findByKey("dept_id", Integer.parseInt(dept_id));
            String dept_name = dept.get("dept_name").toString();
            request.setAttribute("dept_id", dept_id);
            request.setAttribute("dept_name", dept_name);
        }
        if(dept_id!=null && dept_id.equals("")) {
            request.setAttribute("dept_id", dept_id);
        }

        PagedList list = StoresDashBoardsDAO.searchStkAdj(map, ConversionUtils.getListingParameter(map));
        //request.setAttribute("item_list", js.serialize(ConversionUtils.copyListDynaBeansToMap(new StoreDAO(con).getItemNames())));
        request.setAttribute("pagedList", list);
        }finally {
             DataBaseUtil.closeConnections(con, ps);
        }
        return mapping.findForward("viewstockadjustmentscreen");
    }

    /** Screen: Supplier Returns/Replacements, Method: getSupplierReturns, Action Id: pharma_view_supp_returns,
     * Path: /stores/StoresSupplierReturns.
     * @param mapping
     * @param fm
     * @param request
     * @param response
     * @return
     * @throws SQLException
     * @throws Exception
     */
    @IgnoreConfidentialFilters
    public  ActionForward getSupplierReturns(ActionMapping mapping,ActionForm fm,
            HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{

        Map map= getParameterMap(request);
        /** check if user has access right to view data of all stores*/
        HashMap actionRightsMap = new HashMap();
        HttpSession session = request.getSession(false);
        Object roleID = null;
        Role role=new Role();
        actionRightsMap= (HashMap) request.getSession(false).getAttribute("actionRightsMap");
        roleID=  request.getSession(false).getAttribute("roleId");
        String actionRightStatus=(String) session.getAttribute("multiStoreAccess");
        if (actionRightStatus==null)
            actionRightStatus="N";
        int roleId = ((Integer)roleID).intValue();
        int centerId = (Integer) request.getSession().getAttribute("centerId");
        PagedList list = null;
        String dept_id = (String) session.getAttribute("pharmacyStoreId");
        if ((actionRightStatus.equals("A")) || (roleId ==1 || (roleId==2))){
            /** User has access to all stores or he is in admin role..let him see all supplier returns*/
            list = StoresDashBoardsDAO.searchSuppReturns(map, ConversionUtils.getListingParameter(map));
        } else{
            /** user can see data only for his assigned store*/
            map.put("store_id", new String[]{dept_id});
            map.put("store_id@type", new String[]{"integer"});
            list = StoresDashBoardsDAO.searchSuppReturns(map, ConversionUtils.getListingParameter(map));
        }
        if(roleId == 1 || roleId == 2) {
            if(dept_id!=null && !dept_id.equals(""))
            request.setAttribute("store_id", dept_id);
            else
            request.setAttribute("store_id", 0);
        }else {
            request.setAttribute("store_id", dept_id);
        }
        request.setAttribute("pagedList", list);
        ArrayList<String> SupplierNames = StoresDBTablesUtil.getCenterMaterSupplierMaster();
        Connection con = null;
        PreparedStatement ps = null;
        try{
            con = DataBaseUtil.getConnection();
            if (GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
            	request.setAttribute("suppliers", js.serialize(ConversionUtils.copyListDynaBeansToMap(new StoreDAO(con).getCenterSupplierNames(centerId))));
            } else {
            	request.setAttribute("suppliers", js.serialize(ConversionUtils.copyListDynaBeansToMap(new StoreDAO(con).getCenterSupplierNames())));
            }
        }finally{
            DataBaseUtil.closeConnections(con, ps);
        }
        return mapping.findForward("viewsupplierreturnscreen");
    }

    /** Screen: Supplier Returns (With Debit Notes), Method: getSupplierReturnDebits, Action Id: pharma_view_supp_returns,
     * Path: /stores/StoresSupplierReturns.
     * @param mapping
     * @param fm
     * @param request
     * @param response
     * @return
     * @throws SQLException
     * @throws Exception
     */
    @IgnoreConfidentialFilters
    public  ActionForward getSupplierReturnDebits(ActionMapping mapping,ActionForm fm,
            HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{

        Map map= getParameterMap(request);
        PagedList list = StoresDashBoardsDAO.searchSuppReturnDebits(map, ConversionUtils.getListingParameter(map));
        request.setAttribute("pagedList", list);
        int centerId = (Integer) request.getSession().getAttribute("centerId");
        HttpSession session = request.getSession(false);
        int roleId = (Integer)session.getAttribute("roleId");
        String store_id = (String)session.getAttribute("pharmacyStoreId");
        if(roleId == 1 || roleId == 2) {
            if(store_id!=null && !store_id.equals(""))
                request.setAttribute("store_id", store_id);
            else
                request.setAttribute("store_id", 0);
        } else {
            request.setAttribute("store_id", store_id); }
        if (GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
        	ArrayList<String> SupplierNames = StoresDBTablesUtil.getCenterBasedSupplierMaster(centerId);
        	 request.setAttribute("suppliers",SupplierNames);
        } else {
        	 ArrayList<String> SupplierNames = StoresDBTablesUtil.getCenterBasedSupplierMaster();
        	 request.setAttribute("suppliers",SupplierNames);
        }
        return mapping.findForward("viewsupplierreturndebitscreen");
    }

    /** Screen: Purchase Details, Method: getPhPurchaseDetails, Action Id: pharma_view_supp_returns,
     * Path: /stores/StoresSupplierReturns.
     * @param mapping
     * @param fm
     * @param request
     * @param response
     * @return
     * @throws SQLException
     * @throws Exception
     */
    @IgnoreConfidentialFilters
    public  ActionForward getPhPurchaseDetails(ActionMapping mapping,ActionForm fm,
            HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{
        BasicDynaBean dept = null;
        String strDeptName = null;
        Map map= getParameterMap(request);
        if (request.getParameter("store") != null) {
            dept = storesDAO.findByKey("dept_id", Integer.parseInt(request.getParameter("store")));
            strDeptName = (String) dept.get("dept_name");
        }
        
        request.setAttribute("userCenterId",
            uUserDAO.findByKey("emp_username", request.getSession(false).getAttribute("userId"))
                .get("center_id"));
        PagedList list = StoresDashBoardsDAO.searchPhPurchaseDetails(map,
            ConversionUtils.getListingParameter(map));
        request.setAttribute("pagedList", list);
        BasicDynaBean hosp = genericPreferencesDAO.getRecord();
        if (hosp.get("hospital_tin") != null)
          request.setAttribute("hosp_tin", hosp.get("hospital_tin").toString());
        if (hosp.get("hospital_pan") != null)
          request.setAttribute("hosp_pan", hosp.get("hospital_pan").toString());
        if (hosp.get("hospital_service_regn_no") != null)
          request.setAttribute("hosp_ser_reg_no", hosp.get("hospital_service_regn_no").toString());
        request.setAttribute("dept_id", request.getParameter("store"));
        request.setAttribute("dept_name", strDeptName);
        request.setAttribute("genPrefs", GenericPreferencesDAO.getPrefsBean().getMap());
        return mapping.findForward("viewpurchasedetailsscreen");
    }

    @IgnoreConfidentialFilters
    public ActionForward getPhPurchaseDetailsScreen(ActionMapping mapping, ActionForm fm,
        HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {
      HttpSession session = request.getSession(false);
      String dept_id = (String) session.getAttribute("pharmacyStoreId");
      request.setAttribute("genPrefs", GenericPreferencesDAO.getPrefsBean().getMap());
      request.setAttribute("userCenterId",
          uUserDAO.findByKey("emp_username", request.getSession(false).getAttribute("userId"))
              .get("center_id"));

      int roleId = (Integer) session.getAttribute("roleId");
      if (dept_id != null && !dept_id.equals("")) {
        BasicDynaBean dept = storesDAO.findByKey("dept_id", Integer.parseInt(dept_id));
        String dept_name = dept.get("dept_name").toString();
        request.setAttribute("dept_id", dept_id);
        request.setAttribute("dept_name", dept_name);
      }
      if (dept_id != null && dept_id.equals("")) {
        request.setAttribute("dept_id", dept_id);
      }
      if (roleId == 1 || roleId == 2) {
        if (dept_id != null && !dept_id.equals(""))
          request.setAttribute("dept_id", dept_id);
        else
          request.setAttribute("dept_id", 0);
      }
      return mapping.findForward("viewpurchasedetailsscreen");
    }

    /** Screen: Duplicate Sales Bills, Method: getSaleBillsListScreen, Action Id: pharma_sale_duplicate_bill,
     * Path: /pages/stores/salebill.
     * @param mapping
     * @param fm
     * @param request
     * @param response
     * @return
     * @throws SQLException
     * @throws Exception
     */
    @IgnoreConfidentialFilters
    public  ActionForward getSaleBillsListScreen(ActionMapping mapping,ActionForm fm,
            HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{
        return mapping.findForward("getsalesLists");
    }

    @IgnoreConfidentialFilters
    public  ActionForward getSaleBillsList(ActionMapping mapping,ActionForm fm,
            HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{

        Map map= getParameterMap(request);
        PagedList list = StoresDashBoardsDAO.searchPhSalesDetails(map, ConversionUtils.getListingParameter(map));
        BasicDynaBean billPrintBean = PrintConfigurationsDAO.getPrintMode(PrintConfigurationsDAO.PRINT_TYPE_PHARMACY);
        BasicDynaBean prescLabelPrintBean = PrintConfigurationsDAO.getPrintMode(PrintConfigurationsDAO.PRINT_TYPE_PRESCRIPTION_LABEL);
        request.setAttribute("prescLabelPrintBean", prescLabelPrintBean);
		request.setAttribute("billPrintBean", billPrintBean);
        request.setAttribute("pagedList", list);
        GenericPreferencesDTO prescPrefs = GenericPreferencesDAO.getGenericPreferences();
        request.setAttribute("salePrintItems", prescPrefs.getSalesPrintItems());
        request.setAttribute("saleTemplates", PharmacyPrintTemplateDAO.getPharmacyTemplateList());
        return mapping.findForward("getsalesLists");
    }

    @IgnoreConfidentialFilters
    public ActionForward getScreen(ActionMapping m, ActionForm f,
            HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException {

        return m.findForward("getsalesLists");
    }


     public ActionForward getSaleDetails(ActionMapping am, ActionForm af, HttpServletRequest req,
                HttpServletResponse res) throws SQLException, IOException {

         Connection con = DataBaseUtil.getConnection();

         MedicineSalesDAO meddao = new MedicineSalesDAO(con);
         String saleId =  req.getParameter("saleId");
         String saleItemId =  req.getParameter("sale_item_id");
         Integer storeId = 0;
         Integer centerId = -1;

         if (saleItemId != null && !saleItemId.equals("")) {
             BasicDynaBean itembean = storeSalesDetailsDAO.findByKey("sale_item_id", Integer.parseInt(saleItemId));
             saleId = (itembean != null) ? (String)itembean.get("sale_id") : null;
             BasicDynaBean storeItemMainBean = storeSalesMainDao.findByKey("sale_id", saleId);
             if(storeItemMainBean != null) {
                 storeId = (Integer)storeItemMainBean.get("store_id");
             }
             BasicDynaBean storeBean = storesDAO.findByKey("dept_id", storeId);
             if(storeBean != null)
                 centerId = (Integer)storeBean.get("center_id");
         }
         List saleItemList = meddao.getSalesItemList(saleId);

         String billNo = null;
         String visitId = null;
         String bill_visit_type = null;
         boolean isTpa = false;
         BasicDynaBean billbean = null;

         if (saleItemList != null && saleItemList.size() > 0) {
             BasicDynaBean saleitembean = (BasicDynaBean)saleItemList.get(0);
             billNo = (saleitembean != null && saleitembean.get("bill_no") != null)
                             ? (String)saleitembean.get("bill_no") : null;
             if(saleitembean != null) {
            	 storeId = (Integer)saleitembean.get("store_id");
            	 BasicDynaBean storeBean = storesDAO.findByKey("dept_id", storeId);
                 if(storeBean != null)
                     centerId = (Integer)storeBean.get("center_id");
             }

             if (billNo != null) {
                 billbean = BillDAO.getBillBean(billNo);
                 visitId  = (billbean != null) ? (String)billbean.get("visit_id") : null;
                 isTpa  = (billbean != null) ? (Boolean)billbean.get("is_tpa") : false;
                 bill_visit_type = (billbean != null) ? (String)billbean.get("visit_type") : null;
             }
         }

         if (saleId == null || saleId.equals("") || billNo == null || billNo.equals("")) {
             req.setAttribute("error", "This sale id is invalid or sale bill does not exists."+saleId);
         }
         String healthAuthority = HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(CenterMasterDAO.getHealthAuthorityForCenter(centerId)).getHealth_authority();
         String[] drugCodeTypes = HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(healthAuthority).getDrug_code_type();

         Boolean isRetail = false ;
         if (bill_visit_type != null) {
           isRetail = bill_visit_type.equals("r");
         }
         Map resultMap = new HashMap();

         resultMap.put("isTpa", isTpa);
         BasicDynaBean details = null;

         if(bill_visit_type != null && !bill_visit_type.equals("r")) {
            details = VisitDetailsDAO.getPatientVisitDetailsBean(visitId);
            resultMap.put("patientDetails", details.getMap());
         } else {
            resultMap.put("retailDetails", new RetailCustomerDAO().getRetailCustomerEx(visitId).getMap());
         }

         Map salesListMain = MedicineSalesDAO.getSalesMain(saleId).getMap();
         List salesList = ConversionUtils.listBeanToListMap(MedicineSalesDAO.getSalesList1(saleId));
         List saleItemDetails = ConversionUtils.listBeanToListMap(MedicineSalesDAO.getSaleItemsDetails(saleId));

         resultMap.put("salesListMain", salesListMain);
         resultMap.put("salesList", salesList);
         resultMap.put("saleItemDetails", saleItemDetails);
         //	   multi-payer
         //1.set secondary sponsor
         setPlanDetails(resultMap,visitId);
         req.setAttribute("resultMap", resultMap);

         req.setAttribute("saleId", saleId);
         req.setAttribute("visitId", visitId);
         req.setAttribute("isRetail", isRetail);
         List<BasicDynaBean> sugGroupList = PurchaseOrderDAO.getAllSubGroups();
 		 List<BasicDynaBean> groupList = PurchaseOrderDAO.getAllGroups();
         req.setAttribute("subGroupListJSON", js.serialize(ConversionUtils.listBeanToListMap(sugGroupList)));
 		 req.setAttribute("groupList", ConversionUtils.listBeanToListMap(groupList));
 		 req.setAttribute("groupListJSON", js.serialize(ConversionUtils.listBeanToListMap(groupList)));
		 req.setAttribute("pharmaCodeTypesJSON", js.serialize(MedicineSalesDAO.getPharmaCodeTypes(healthAuthority)));
		 req.setAttribute("pharmaCodesJSON", js.serialize(MedicineSalesDAO.getPharmaCodes(drugCodeTypes)));
		 req.setAttribute("after_decimal_digits",GenericPreferencesDAO.getPrefsBean().get("after_decimal_digits")!=null?GenericPreferencesDAO.getPrefsBean().get("after_decimal_digits"):0);

         DataBaseUtil.closeConnections(con, null);

         return am.findForward("getSaleDetails");
    }

    public ActionForward saveSalesClaimDetails(ActionMapping am, ActionForm af, HttpServletRequest req,
            HttpServletResponse res) throws SQLException, IOException {
         Boolean success = true;

         String saleId =  req.getParameter("saleId");
         String billNo = null;

         Connection con = DataBaseUtil.getConnection();
         con.setAutoCommit(false);
         try{

             MedicineSalesDAO meddao = new MedicineSalesDAO(con);
             List saleItemList = meddao.getSalesItemList(saleId);

             String visitId = null;
             String chargeId = null;
             String billStatus = null;
             BasicDynaBean billbean = null;

             if (saleItemList != null && saleItemList.size() > 0) {
                 BasicDynaBean saleitembean = (BasicDynaBean)saleItemList.get(0);
                 if (saleitembean != null) {
                     billNo = (saleitembean.get("bill_no") != null)
                                     ? (String)saleitembean.get("bill_no") : null;
                     chargeId = (saleitembean.get("charge_id") != null)
                                      ? (String)saleitembean.get("charge_id") : null;
                 }
                 if (billNo != null) {
                     billbean = BillDAO.getBillBean(billNo);
                     visitId  = (billbean != null) ? (String)billbean.get("visit_id") : null;
                      billStatus = (billbean != null) ? (String)billbean.get("status") : null;
                 }
             }

             if (saleId == null || saleId.equals("") || billNo == null || billNo.equals("") || chargeId == null || chargeId.equals("")) {
                 FlashScope flash = FlashScope.getScope(req);
                 ActionRedirect redirect = new ActionRedirect("/pages/stores/editSales.do?_method=getSaleDetails");
                 flash.error("This sale id is invalid or sale bill does not exists."+saleId);
                 redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                 redirect.addParameter("visitId", visitId);
                 redirect.addParameter("saleId", saleId);
                 return redirect;
             }

             GenericDAO chgDAO = new GenericDAO("bill_charge");

             Map<Object, Object> columnData = new HashMap<Object, Object>();

             BigDecimal prevInsClaimAmts = BigDecimal.ZERO;
             BigDecimal insClaimAmts = BigDecimal.ZERO;
             BigDecimal insClaimTaxAmts = BigDecimal.ZERO;
             Map itemsMap = req.getParameterMap();

             String[] saleItemIds = (String[])itemsMap.get("sale_item_id");
             String[] pri_claimAmts = (String[])itemsMap.get("pri_insClaimAmt");
             String[] priInsClaimTaxAmts = (String[])itemsMap.get("priInsClaimTaxAmt");
             String[] sec_claimamts = (String[])itemsMap.get("sec_insClaimAmt");
             String[] secInsClaimTaxAmts = (String[])itemsMap.get("secInsClaimTaxAmt");
             String[] erxActivityIds = (String[])itemsMap.get("erxActivityId");
             String[] pri_priauthno = (String[])itemsMap.get("pri_itemPreAuthId");
             String[] pri_priauthmode = (String[])itemsMap.get("pri_itemPreAuthMode");
             String[] sec_priauthno = (String[])itemsMap.get("sec_itemPreAuthId");
             String[] sec_priauthmode = (String[])itemsMap.get("sec_itemPreAuthMode");
             String[] isEdited = (String[])itemsMap.get("isEdited");
             String[] priClaimId = (String[])itemsMap.get("pri_claim_id");
             String[] secClaimId = (String[])itemsMap.get("sec_claim_id");
             // sets external Pbm and erx Reference No in store_sales_main
             updateSalesMain(con, itemsMap, saleId);
             
             List<BasicDynaBean> planList = insPlanDAO.getPlanDetails(con, visitId);
             BigDecimal[] consolidatedClaimAmts = new BigDecimal[planList.size()];
             BigDecimal[] consolidatedClaimTaxAmts = new BigDecimal[planList.size()];
             List<BasicDynaBean> groupList = PurchaseOrderDAO.getAllGroups();
             StoresHelper storeHelper = new StoresHelper();
             String[] priFieldGetKeys = {"pritaxrate", "pritaxamount", "pritaxsubgroupid"};
             String[] secFieldGetKeys = {"sectaxrate", "sectaxamount", "sectaxsubgroupid"};
             String[] fieldSetKeys = {"taxrate", "tax_amt", "item_subgroup_id"};
             MedicineSalesBO medicineSalesBO = new MedicineSalesBO(); 

             for(int i = 0;i< saleItemIds.length ;i++ ){
            	 String sid = saleItemIds[i];
                 BasicDynaBean sidBean = storeSalesDetailsDAO.findByKey(con, "sale_item_id", Integer.parseInt(sid));
                 
                 columnData.clear();
                 //Primary insurance details
                 BigDecimal prevInsClaimAmt = sidBean == null || sidBean.get("insurance_claim_amt")==null || sidBean.get("insurance_claim_amt").equals("")? BigDecimal.ZERO : (BigDecimal)sidBean.get("insurance_claim_amt");
                 BigDecimal pri_insClaimAmt = pri_claimAmts == null || pri_claimAmts[i] == null
                         || pri_claimAmts[i].equals("")? BigDecimal.ZERO :
                             BigDecimal.valueOf(Double.parseDouble(pri_claimAmts[i]));
                 BigDecimal priInsClaimTaxAmt = priInsClaimTaxAmts == null || priInsClaimTaxAmts[i] == null
                         || priInsClaimTaxAmts[i].equals("")? BigDecimal.ZERO :
                             BigDecimal.valueOf(Double.parseDouble(priInsClaimTaxAmts[i]));
                 String itemCode = req.getParameter("itemCode"+sid);
                 String itemCodeType = req.getParameter("itemCodeType"+sid);
                 String erxActivityId = erxActivityIds[i];
                 String pri_itemPreAuthNo = pri_priauthno != null ? pri_priauthno[i] : null;
                 String itemPreAuthModeNoStr = pri_priauthmode != null ? pri_priauthmode[i] : null;
                 Integer pri_itemPreAuthModeNo = itemPreAuthModeNoStr == null || itemPreAuthModeNoStr.equals("")? null: Integer.parseInt(itemPreAuthModeNoStr);
                 billStatus= req.getParameter("_billStatus"+sid);
                 if(req.getParameter("_billStatus"+sid).equals("A")) {
                     columnData.put("insurance_claim_amt", pri_insClaimAmt);
                 }
                 columnData.put("code_type", itemCodeType);
                 columnData.put("item_code", itemCode);
                 columnData.put("erx_activity_id", erxActivityId);
                 columnData.put("prior_auth_id", pri_itemPreAuthNo);
                 columnData.put("prior_auth_mode_id", pri_itemPreAuthModeNo);
                 storeSalesDetailsDAO.update(con, columnData, "sale_item_id", Integer.parseInt(sid));
                 prevInsClaimAmts = prevInsClaimAmts.add(prevInsClaimAmt);
                 insClaimAmts = insClaimAmts.add(pri_insClaimAmt);
                 ArrayList<Map<String, Object>> priSponsorTaxMap = new ArrayList<Map<String, Object>>();
                 ArrayList<Map<String, Object>> secSponsorTaxMap = new ArrayList<Map<String, Object>>();
                 //If claim amounts are edited then insert new tax splits.
                 if(isEdited[i].equals("t")) {
                	 for(int j=0; j<groupList.size() ;j++) {
      					BasicDynaBean groupBean = groupList.get(j);
      					Map taxSubDetails = storeHelper.getTaxDetailsMap(itemsMap, i, (Integer)groupBean.get("item_group_id"), priFieldGetKeys, fieldSetKeys);
      					if(taxSubDetails.size() > 0)
      						priSponsorTaxMap.add(taxSubDetails);
      				 }
                      for(int j=0; j<groupList.size() ;j++) {
       					BasicDynaBean groupBean = groupList.get(j);
       					Map taxSubDetails = storeHelper.getTaxDetailsMap(itemsMap, i, (Integer)groupBean.get("item_group_id"), secFieldGetKeys, fieldSetKeys);
       					if(taxSubDetails.size() > 0)
       						secSponsorTaxMap.add(taxSubDetails);
       				 }
                     int saleItemId = Integer.parseInt(sid);
                     Map<String, Object> filterMap = new HashMap<String, Object>();
                     filterMap.put("sale_item_id", saleItemId);
                     filterMap.put("adj_amt", "Y");
                     
                     List<BasicDynaBean> salesTaxList = salesClaimTaxDetailsDAO.listAll(null, filterMap, null);
                     String adjAmt = "N";
                     if(salesTaxList.size() > 0) {
                    	 adjAmt = "Y";
                     } 
                     //salesTaxDao.delete(con, "sale_item_id", saleItemId);
                     BasicDynaBean taxBean = salesClaimTaxDetailsDAO.getBean();
                     Iterator<Map<String, Object>> priTaxMapIterator = priSponsorTaxMap.iterator();
                     while(priTaxMapIterator.hasNext()) {
						Map<String, Object> taxSubMap = priTaxMapIterator.next();
						if(taxSubMap.get("taxrate") != null) {
							Map<String, Object> keyMap = new HashMap<String, Object>();
							keyMap.put("sale_item_id", saleItemId);
							keyMap.put("claim_id", priClaimId[i]);
							keyMap.put("item_subgroup_id", taxSubMap.get("item_subgroup_id"));
							BasicDynaBean salesClaimTaxBean = salesClaimTaxDetailsDAO.findByKey(keyMap);
							
							if(salesClaimTaxBean != null) {
								salesClaimTaxBean.set("tax_rate", taxSubMap.get("taxrate"));
								salesClaimTaxBean.set("tax_amt", taxSubMap.get("tax_amt"));
								
								salesClaimTaxDetailsDAO.update(con, salesClaimTaxBean.getMap(), keyMap);
							} else {
								taxBean.set("sale_item_id", saleItemId);
								taxBean.set("claim_id", priClaimId[i]);
								taxBean.set("item_subgroup_id", taxSubMap.get("item_subgroup_id"));
								taxBean.set("tax_rate", taxSubMap.get("taxrate"));
								taxBean.set("tax_amt", taxSubMap.get("tax_amt"));
								taxBean.set("adj_amt", adjAmt);
								success &= salesClaimTaxDetailsDAO.insert(con, taxBean);
							}
						}
						
                     }
                     
                     Iterator<Map<String, Object>> secTaxMapIterator = secSponsorTaxMap.iterator();
                     while(secTaxMapIterator.hasNext()) {
						Map<String, Object> taxSubMap = secTaxMapIterator.next();
						if(taxSubMap.get("taxrate") != null) {
							Map<String, Object> keyMap = new HashMap<String, Object>();
							keyMap.put("sale_item_id", saleItemId);
							keyMap.put("claim_id", secClaimId[i]);
							keyMap.put("item_subgroup_id", taxSubMap.get("item_subgroup_id"));
							BasicDynaBean salesClaimTaxBean = salesClaimTaxDetailsDAO.findByKey(keyMap);
							
							if(salesClaimTaxBean != null) {
								salesClaimTaxBean.set("tax_rate", taxSubMap.get("taxrate"));
								salesClaimTaxBean.set("tax_amt", taxSubMap.get("tax_amt"));
								
								salesClaimTaxDetailsDAO.update(con, salesClaimTaxBean.getMap(), keyMap);
							} else {
								taxBean.set("sale_item_id", saleItemId);
								taxBean.set("claim_id", secClaimId[i]);
								taxBean.set("item_subgroup_id", taxSubMap.get("item_subgroup_id"));
								taxBean.set("tax_rate", taxSubMap.get("taxrate"));
								taxBean.set("tax_amt", taxSubMap.get("tax_amt"));
								taxBean.set("adj_amt", adjAmt);
								success &= salesClaimTaxDetailsDAO.insert(con, taxBean);
							}
						}
                     }
                  }
                 
                 
                 //Secondary insurance details
                 BigDecimal sec_insClaimAmt = sec_claimamts == null || sec_claimamts[i] == null
                         || sec_claimamts[i].equals("")? BigDecimal.ZERO :
                         BigDecimal.valueOf(Double.parseDouble(sec_claimamts[i]));
                 BigDecimal secInsClaimTaxAmt = secInsClaimTaxAmts == null || secInsClaimTaxAmts[i] == null
                         || secInsClaimTaxAmts[i].equals("")? BigDecimal.ZERO :
                             BigDecimal.valueOf(Double.parseDouble(secInsClaimTaxAmts[i]));
                 String sec_itemPreAuthNo = sec_priauthno != null ? sec_priauthno[i] : null;
                 String sec_itemPreAuthModeNoStr = sec_priauthmode != null ? sec_priauthmode[i] : null;
                 Integer sec_itemPreAuthModeNo = sec_itemPreAuthModeNoStr == null || sec_itemPreAuthModeNoStr.equals("")? null: Integer.parseInt(sec_itemPreAuthModeNoStr);

                BigDecimal[] claimAmts = new BigDecimal[planList.size()];
                BigDecimal[] claimTaxAmts = new BigDecimal[planList.size()];
                String[] priAuthIds = new String[planList.size()];
                Integer[] priAuthModes = new Integer[planList.size()];
                for(int j = 0 ;j<planList.size();j++){
                    if ( j == 0 ) {
                        claimAmts[j] = pri_insClaimAmt != null ? pri_insClaimAmt :BigDecimal.ZERO;
                        claimTaxAmts[j] = priInsClaimTaxAmt != null ? priInsClaimTaxAmt : BigDecimal.ZERO;
                        priAuthIds[j] = pri_itemPreAuthNo;
                        priAuthModes[j] = pri_itemPreAuthModeNo;
                    } else {
                        claimAmts[j] = sec_insClaimAmt != null ? sec_insClaimAmt :BigDecimal.ZERO;
                        claimTaxAmts[j] = secInsClaimTaxAmt != null ? secInsClaimTaxAmt : BigDecimal.ZERO;
                        priAuthIds[j] = sec_itemPreAuthNo;
                        priAuthModes[j] = sec_itemPreAuthModeNo;
                    }
                }
                 //multi-payer
                GenericDAO salesClaimDAO = new GenericDAO("sales_claim_details");
                BillChargeClaimDAO billClaimDAO = new BillChargeClaimDAO();
                PatientInsurancePlanDAO insPlanDAO = new PatientInsurancePlanDAO();

                Map keys = new HashMap();
                for(int j = 0;j<planList.size();j++){
                    String sponsorId = insPlanDAO.getSponsorId(con,visitId,(Integer)planList.get(j).get("plan_id")) ;
                    String claimId = billClaimDAO.getClaimId(con, (Integer)planList.get(j).get("plan_id"), billNo, visitId, sponsorId);

                    keys.put("sale_item_id", Integer.parseInt(sid));
                    keys.put("claim_id", claimId);
                    BasicDynaBean salesSponsorClaim = salesClaimDAO.findByKey(keys);
                    if(null != salesSponsorClaim) {
	                    salesSponsorClaim.set("insurance_claim_amt", claimAmts[j]);
	                    salesSponsorClaim.set("ref_insurance_claim_amount", claimAmts[j]);
	                    salesSponsorClaim.set("tax_amt", claimTaxAmts[j]);
	                    salesSponsorClaim.set("prior_auth_id", priAuthIds[j]);
	                    salesSponsorClaim.set("prior_auth_mode_id", priAuthModes[j]);

	                    success &= (salesClaimDAO.update(con, salesSponsorClaim.getMap(),
	                                "sales_item_plan_claim_id",salesSponsorClaim.get("sales_item_plan_claim_id")) > 0);
                    }

                    consolidatedClaimAmts[j] = consolidatedClaimAmts[j] != null ? consolidatedClaimAmts[j].add(claimAmts[j]) : claimAmts[j] ;
                    consolidatedClaimTaxAmts[j] = consolidatedClaimTaxAmts[j] != null ? consolidatedClaimTaxAmts[j].add(claimTaxAmts[j]) : claimTaxAmts[j] ;
                }
                success &= medicineSalesBO.updateSaleTaxDetails(con, sidBean);
             }

             ChargeDAO cdao = new ChargeDAO(con);
             List<ChargeDTO> actChargeList=new ArrayList<ChargeDTO>();

             ChargeDTO charge = cdao.getCharge(chargeId);
             charge.setSponsorTaxAmounts(consolidatedClaimTaxAmts);
             charge.setClaimAmounts(consolidatedClaimAmts);

             actChargeList.add(charge);
             success = updateSalesBillClaimDetails(con,planList,actChargeList,billNo,visitId);
             
             success &= medicineSalesBO.insertOrUpdateBillChargeTaxesForSales(con, saleId);

             /*if(billStatus != null && billStatus.equals("A")){
                 columnData.clear();
                 columnData.put("insurance_claim_amount", insClaimAmts);
                 chgDAO.update(con, columnData, "charge_id", chargeId);
             }*/

             FlashScope flash = FlashScope.getScope(req);
             ActionRedirect redirect = new ActionRedirect("/pages/stores/editSales.do?_method=getSaleDetails");
             redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
             redirect.addParameter("visitId", visitId);
             redirect.addParameter("saleId", saleId);
             return redirect;
         }finally{
             DataBaseUtil.commitClose(con, success);
             if (success) {
                if (billNo != null && !billNo.equals(""))
                    BillDAO.resetTotalsOrReProcess(billNo);
            }
         }
    }

    private void updateSalesMain(Connection con, Map itemsMap, String saleId) throws SQLException, IOException {
      // if external Pbm checkbox is set in edit sales screen, set it as true in store_sales_main.
      // is external pbm checkbox sends 'on' if checked, else sends null.
      HashMap updateColumnData = new HashMap<>();
      if (itemsMap.get("isExternalPbm") != null && ((String[])itemsMap.get("isExternalPbm")).length > 0 ) {
        updateColumnData.put("is_external_pbm", true);
      } else {
        updateColumnData.put("is_external_pbm", false);
      }
      
      // set erx reference no
      if (itemsMap.get("erxReferenceNo") != null && ((String[])itemsMap.get("erxReferenceNo")).length > 0 ) {
        updateColumnData.put("erx_reference_no", ((String[])itemsMap.get("erxReferenceNo"))[0]);
      }
      storeSalesMainDao.update(con, updateColumnData, "sale_id", saleId);
    }


    /** Screen: View Stock Transfer, Method: getStkTransfer, Action Id: stores_view_stock_trans,
     * Path: /stores/stockTransfer.
     * @param mapping
     * @param fm
     * @param request
     * @param response
     * @return
     * @throws SQLException
     * @throws Exception
     */
    @IgnoreConfidentialFilters
    public  ActionForward getStkTransfer(ActionMapping mapping,ActionForm fm,
            HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{

        Map map= getParameterMap(request);
        PagedList list = StoresDashBoardsDAO.searchStkTransfer(map, ConversionUtils.getListingParameter(map));
        HttpSession session = request.getSession(false);

        String dept_id = (String) session.getAttribute("pharmacyStoreId");
        if(dept_id!=null && !dept_id.equals(""))
        {
            BasicDynaBean dept = storesDAO.findByKey("dept_id", Integer.parseInt(dept_id));
            String dept_name = dept.get("dept_name").toString();
            request.setAttribute("dept_id", dept_id);
            request.setAttribute("dept_name", dept_name);
        }
        if(dept_id!=null && dept_id.equals("")) {
            request.setAttribute("dept_id", dept_id);
        }
        request.setAttribute("pagedList", list);
        return mapping.findForward("viewstocktransferscreen");
    }


    /** Screen: View Stock User Returns, Method: getStkRet, Action Id: inv_view_stock_issues,
     * Path: /inventory/stockUserReturns.
     * @param mapping
     * @param fm
     * @param request
     * @param response
     * @return
     * @throws SQLException
     * @throws Exception
     */
    @IgnoreConfidentialFilters
    public  ActionForward getStkRet(ActionMapping mapping,ActionForm fm,
            HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{

        Map map= getParameterMap(request);
        PagedList list = StoresDashBoardsDAO.searchStkRet(map, ConversionUtils.getListingParameter(map));
        request.setAttribute("hospuserlist", js.serialize(ConversionUtils.copyListDynaBeansToMap(
            storeHospUserDAO.listAll(null, "status", "A", "hosp_user_name"))));
        request.setAttribute("pagedList", list);
        return mapping.findForward("viewstockuserreturnscreen");
    }

    /** Screen: View Stock User Returns, Method: getStkRet, Action Id: inv_view_stock_issues,
     * Path: /inventory/stockUserReturns.
     * @param mapping
     * @param fm
     * @param request
     * @param response
     * @return
     * @throws SQLException
     * @throws Exception
     */
    @IgnoreConfidentialFilters
    public  ActionForward getPatientStkRetScreen(ActionMapping mapping,ActionForm fm,
            HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{
        return mapping.findForward("viewstockpatientreturnscreen");
    }

    @IgnoreConfidentialFilters
    public  ActionForward getPatientStkRet(ActionMapping mapping,ActionForm fm,
            HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{

        Map map= getParameterMap(request);
        PagedList list = StoresDashBoardsDAO.searchPatientStkRet(map, ConversionUtils.getListingParameter(map));
        request.setAttribute("pagedList", list);
        return mapping.findForward("viewstockpatientreturnscreen");
    }

    @IgnoreConfidentialFilters
    public ActionForward getValidatePoScreen(ActionMapping m, ActionForm f,
            HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException, Exception {
        String po_no = req.getParameter("poNo");
        BasicDynaBean podetails = StoresPOApprovalDAO.getPOApprovalRejectDetails(po_no);
        req.setAttribute("podetails", podetails);
        List<BasicDynaBean> polist = null;
        int store_id  = 0;
        store_id  = Integer.parseInt(podetails.get("store_id").toString());
        polist = StoresPOApprovalDAO.getPOItemDetails(po_no,store_id);
        req.setAttribute("polist", polist);
        String storeName  = StoresIndentDAO.getStoreName(store_id);
        req.setAttribute("storeName", storeName);

        HttpSession session = req.getSession(false);
        String dept_id = (String)session.getAttribute("pharmacyStoreId");
        if(dept_id!=null && !dept_id.equals("")){
            BasicDynaBean dept = storesDAO.findByKey("dept_id", Integer.parseInt(dept_id));
            String dept_name = dept.get("dept_name").toString();
            req.setAttribute("dept_id", dept_id);
            req.setAttribute("dept_name", dept_name);
        }
        if(dept_id != null && dept_id.equals("")) {
            req.setAttribute("dept_id", dept_id);
        }
        return m.findForward("addshow");
    }


    public  ActionForward validatePO(ActionMapping mapping,ActionForm fm,
            HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{

        HttpSession session = request.getSession();
        GenericDAO poMain = new GenericDAO("store_po_main");
        Connection con = null;
        boolean success = true;

        try{
            con = DataBaseUtil.getConnection();
            con.setAutoCommit(false);
            Map keys = new HashMap();

            String po_no = request.getParameter("poNo");

            BasicDynaBean pobean = poMain.findByKey("po_no",po_no);
            String poStatus = request.getParameter("status_main");
            pobean.set("status", poStatus);
            pobean.set("last_modified_by", session.getAttribute("userid"));
            
            if(poStatus.equals("V")){
	            pobean.set("validated_by", session.getAttribute("userid"));
	            pobean.set("validated_time", DataBaseUtil.getDateandTime());
	            pobean.set("validator_remarks", request.getParameter("remarks"));
            }else{
            	if(poStatus.equals("AV")){
            		pobean.set("amendment_validated_by", session.getAttribute("userid"));
    	            pobean.set("amendment_validated_time", DataBaseUtil.getDateandTime());
    	            pobean.set("amendment_validator_remarks", request.getParameter("remarks"));
            	}
            }
            
            keys.put("po_no",po_no);

            success = poMain.update(con, pobean.getMap(), keys) > 0;
        } finally{
            DataBaseUtil.commitClose(con, success);
        }


        FlashScope flash = FlashScope.getScope(request);
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("savedpo"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        redirect.addParameter("status","O");
        redirect.addParameter("status","A");
        redirect.addParameter("status","AO");
        redirect.addParameter("status","AA");
        redirect.addParameter("sortOrder","po_no");
        redirect.addParameter("sortReverse",true);
        return redirect;
    }

    /**
     * Sets insurance plan related details in the map
     * @param resultMap
     */
    private void setPlanDetails(Map resultMap,String visitId) throws SQLException{
        List<Map> saleItems  = (List<Map>)resultMap.get("saleItemDetails");
        Map salesClaimDetails = new HashMap();
        for( Map saleItem : saleItems){
            salesClaimDetails.put(saleItem.get("sale_item_id"), ConversionUtils.listBeanToListMap(
                    salesClaimDAO.listAll(null,"sale_item_id", saleItem.get("sale_item_id"),"sales_item_plan_claim_id")));
        }
        resultMap.put("sales_claim_details", salesClaimDetails);
        resultMap.put("visit_plans", new PatientInsurancePlanDAO().
                listAll(null, "patient_id",visitId,"priority"));

    }

    /**
	 * Inserts claim detaikls of issued items
	 * @param con
	 * @param visitId
	 * @param actChargeList
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	private static boolean updateSalesBillClaimDetails(Connection con,List<BasicDynaBean> plansList,List<ChargeDTO> chargeList,
			String billNo,String visitId)
	throws SQLException,IOException{

		BillChargeClaimDAO billChargeClaimDAO = new BillChargeClaimDAO();
		int[] planIds = new int[plansList.size()];
		for(int j = 0;j<plansList.size();j++){
			planIds[j] = (Integer)plansList.get(j).get("plan_id");
		}
		boolean sucess = true;
		if ( planIds.length > 0 )
			sucess = billChargeClaimDAO.updateBillChargeClaims(con, chargeList, visitId, billNo, planIds,false);
		return sucess;
	}

	@IgnoreConfidentialFilters
	public  ActionForward getPrint(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response) throws Exception {
		
		PrintTemplatesDAO printTemplateDAO = new PrintTemplatesDAO();
		
		String report = request.getParameter("report");
		Map ftlParams = new HashMap();
		String templateContent = "";
		PrintTemplate template = null;
		if(report.equals("StoreStockUserIssue")){
			String issueNo = request.getParameter("issNo");
			List<BasicDynaBean> UserIssueDetails = StoresDashBoardsDAO.getUserIssueDetails(Integer.valueOf(issueNo));
			ftlParams.put("UserIssueList", UserIssueDetails);
			template = PrintTemplate.UserIssuePrintTemplate;
			templateContent = printTemplateDAO.getCustomizedTemplate(template);
		}else{
			if(report.equals("StoreStockUserReturns")){
				String returnNo = request.getParameter("returnNo");
				List<BasicDynaBean> UserIssueReturnDetails = StoresDashBoardsDAO.getUserIssueReturnDetails(Integer.valueOf(returnNo));
				ftlParams.put("UserIssueReturnList", UserIssueReturnDetails);
				template = PrintTemplate.UserIssueReturnPrintTemplate;
				templateContent = printTemplateDAO.getCustomizedTemplate(template);
			}
		}
		
		BasicDynaBean printprefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_STORE);
	     templateContent = printTemplateDAO.getCustomizedTemplate(template);
		
	    Template t = null;
	        if(template != null && (templateContent == null || templateContent.equals(""))){
	       		t = AppInit.getFmConfig().getTemplate(template.getFtlName() + ".ftl");
	        }else{
	        	StringReader reader = new StringReader(templateContent);
	        	t = new Template(null, reader, AppInit.getFmConfig());
	        }
	   
	    HtmlConverter htmlConverter = new HtmlConverter();
		StringWriter writer = new StringWriter();
		t.process(ftlParams, writer);
		String printContent = writer.toString();
		if (printprefs.get("print_mode").equals("P")) {
			OutputStream os = response.getOutputStream();
			response.setContentType("application/pdf");
			if(report.equals("StoreStockUserIssue")){
				htmlConverter.writePdf(os, printContent, "UserIssuePrintTemplate", printprefs, false, false, true, true, true, false);
			}else{
				htmlConverter.writePdf(os, printContent, "UserIssueReturnPrintTemplate", printprefs, false, false, true, true, true, false);

			}
			os.close();
		} else {
			String textReport = null;
			if(report.equals("StoreStockUserIssue")){
				textReport = new String(htmlConverter.getText(printContent, "UserIssuePrintTemplate", printprefs, true, true));
			}else{
				textReport = new String(htmlConverter.getText(printContent, "UserIssueReturnPrintTemplate", printprefs, true, true));

			}
			request.setAttribute("textReport", textReport);
			request.setAttribute("textColumns", printprefs.get("text_mode_column"));
            request.setAttribute("printerType", "DMP");
			return mapping.findForward("textPrintApplet");
		}
		return null;
	}

}