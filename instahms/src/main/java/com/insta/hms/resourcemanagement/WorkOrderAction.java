/**
 * 
 */
package com.insta.hms.resourcemanagement;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.stores.PurchaseOrderDAO;
import com.insta.hms.stores.StoresPOApprovalDAO;
import flexjson.JSONSerializer;
import freemarker.template.Template;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author irshad
 *
 */
public class WorkOrderAction extends BaseAction {
	static Logger log = LoggerFactory.getLogger(WorkOrderAction.class);
	static final StoresPOApprovalDAO dao = new StoresPOApprovalDAO ();
	static final WorkOrderDAO wodao = new WorkOrderDAO("work_order_main");

	@IgnoreConfidentialFilters
    public  ActionForward getWOs(ActionMapping mapping,ActionForm fm,HttpServletRequest request, HttpServletResponse response)
			throws SQLException, Exception {
   
		String searchMethod = request.getParameter("_searchMethod");
		if (searchMethod == null || searchMethod.isEmpty())
			searchMethod = "noSearch";
		String[] wono = request.getParameterValues("_wono");
		String[] close = request.getParameterValues("_hidclose");
		String[] cancel = request.getParameterValues("_hidcancel");
		String[] woStatus = request.getParameterValues("_woStatus");

		int centerId = RequestContext.getCenterId();
		request.setAttribute("listcentersforsuppliers",dao.listAllcentersforAPo(centerId));
		Map map = request.getParameterMap();
		
		List<String> closeList = new ArrayList<String>();
		List<String> cancelList = new ArrayList<String>();
		if (searchMethod.equals("noSearch") && wono != null) {

			for (int i = 0; i < wono.length; i++) {
				if (woStatus[i].equalsIgnoreCase("O") || woStatus[i].equalsIgnoreCase("A") || woStatus[i].equalsIgnoreCase("R")) {
					if (close[i].equalsIgnoreCase("Y")) {	
						closeList.add(wono[i]);
					}
					if (cancel[i].equalsIgnoreCase("Y")) {	
						cancelList.add(wono[i]);
					}
				}

			}
			
			wodao.updateWo(closeList,"C");
			wodao.updateWo(cancelList,"X");
			
		}

		PagedList pagedList = wodao.getWOList(map,ConversionUtils.getListingParameter(map));
		request.setAttribute("pagedList", pagedList);
		return mapping.findForward("list");
    }
 
	@IgnoreConfidentialFilters
    public ActionForward getWOScreen(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res)
		throws SQLException {
    	
    	 int centerId = (Integer)req.getSession().getAttribute("centerId");
		 JSONSerializer js = new JSONSerializer().exclude("class");
		 String woNO = req.getParameter("wo_no");
		 if (woNO != null && !woNO.equals("")) {
			 BasicDynaBean wobean = WorkOrderDAO.getWODetails(woNO);
			 req.setAttribute("wobean", wobean);
			 List<BasicDynaBean> woItems = WorkOrderDAO.getWOItems(woNO);
			 req.setAttribute("woItems", woItems);
			 req.setAttribute("op_mode", "edit");
		 } else {
			 req.setAttribute("op_mode", "add");
		 }
		 req.setAttribute("listAllcentersforAPo", js.deepSerialize(PurchaseOrderDAO.listAllcentersforAPo(centerId)));
		 
		 return m.findForward("addshow");
	}
 
	@IgnoreConfidentialFilters
    public ActionForward saveWO(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws SQLException,Exception {
		ActionRedirect redirect = null;
		HttpSession session = req.getSession(false);
		String username = (String) session.getAttribute("userid");
		Connection con = null;
		boolean status = false;
		ArrayList<String> printUrls = null;
		Map<String, Object> itemsMap = new HashMap<String, Object>(req.getParameterMap());
		String[] item_name = (String[]) itemsMap.get("wo_item_name");
		int elelen = item_name.length -1;
		String[] deleted = (String[]) itemsMap.get("_deleted");
		String woNO = "";
		String[] itemStatus = (String[]) itemsMap.get("status_ar");
		if(((String[])itemsMap.get("wo_no"))[0] != null && !((String[])itemsMap.get("wo_no"))[0].equals("")) {
			woNO = ((String[])itemsMap.get("wo_no"))[0];
		}
		GenericDAO woMainDAO = new GenericDAO("work_order_main");
		BasicDynaBean woMainBean =null;
		FlashScope flash = FlashScope.getScope(req);
		Date date=new Date();
	    try {
	    	redirect = new ActionRedirect(mapping.findForward("listRedirect"));
	    	con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			log.debug("Number of items being entered in stock: " + elelen);
			
			woMainBean = woMainDAO.getBean();
			
			Object expectedReceivedDate = null; 
			if(req.getParameterMap().containsKey("expected_received_date")) {
				expectedReceivedDate = ((Object[])(req.getParameterMap().get("expected_received_date")))[0];
			}
			itemsMap.remove("expected_received_date");
			ConversionUtils.copyToDynaBean(itemsMap, woMainBean, null, true);
			String woStatus = String.valueOf(woMainBean.get("status"));
			if(!woNO.equals("")) {
				BasicDynaBean existWOMainDAO = woMainDAO.findByKey(con, "wo_no", woNO);
				if(existWOMainDAO != null) {
					woMainBean.set("wo_no", woNO);
					
					if(expectedReceivedDate != null) {
						SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
					    Date parsedDate = dateFormat.parse(String.valueOf(expectedReceivedDate));
						woMainBean.set("expected_received_date", new java.sql.Date(parsedDate.getTime()));
					}
					if(woStatus.equalsIgnoreCase("O")) {
						woMainBean.set("raised_by", username);
						//woMainBean.set("wo_date", new java.sql.Date(date.getTime()));
					} else if(woStatus.equalsIgnoreCase("A")) {
						woMainBean.set("raised_by", username);
						woMainBean.set("approved_by", username);
						woMainBean.set("approved_date", DataBaseUtil.getDateandTime());
					} else if(woStatus.equalsIgnoreCase("R")) {
						woMainBean.set("raised_by", username);
						woMainBean.set("rejected_by", username);
						woMainBean.set("rejected_date", DataBaseUtil.getDateandTime());
					}
					woMainDAO.update(con, woMainBean.getMap(), "wo_no", woNO);
					flash.info("Work Order "+woNO+" updated successfully");
				}
			} else {
				woNO = woMainDAO.getNextFormattedId();
				woMainBean.set("wo_no", woNO);
				if(expectedReceivedDate != null) {
					SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
				    Date parsedDate = dateFormat.parse(String.valueOf(expectedReceivedDate));
					woMainBean.set("expected_received_date", new java.sql.Date(parsedDate.getTime()));
				}
				if(woStatus.equalsIgnoreCase("O")) {
					woMainBean.set("raised_by", username);
					woMainBean.set("wo_date", new java.sql.Date(date.getTime()));
				} else if(woStatus.equalsIgnoreCase("A")) {
					woMainBean.set("raised_by", username);
					woMainBean.set("approved_by", username);
					woMainBean.set("wo_date", new java.sql.Date(date.getTime()));
					woMainBean.set("approved_date", DataBaseUtil.getDateandTime());
				} /*else if(woStatus.equalsIgnoreCase("R")) {
					woMainBean.set("raised_by", username);
					woMainBean.set("rejected_by", username);
					woMainBean.set("wo_date", new java.sql.Date(date.getTime()));
					woMainBean.set("rejected_date", DataBaseUtil.getDateandTime());
				}*/
				int centerId = (Integer)req.getSession().getAttribute("centerId");
				woMainBean.set("center_id", centerId);
				woMainDAO.insert(con, woMainBean);
				flash.info("Work Order "+woNO+" generated successfully");
			}
			
			GenericDAO woDetailsBean = new GenericDAO("work_order_details");
			GenericDAO woItemMasterDAO = new GenericDAO("work_order_items_master");
			
			// WO details
			for (int i=0; i < elelen; i++) {
				
				BasicDynaBean bean = woDetailsBean.getBean();		// details
				BasicDynaBean itemMasterBean = woItemMasterDAO.getBean();		// details
				
				ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i, bean, null, true);
				ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i, itemMasterBean, null, true);
				
				int woItemId = 0;
				
				String dbValue = null; 
				if(req.getParameterMap().containsKey("dbvalue")) {
					dbValue = (String)((Object[])(req.getParameterMap().get("dbvalue")))[i];
				}
				
				BasicDynaBean existingWOItemBean = woItemMasterDAO.findByKey(con, "wo_item_name", itemMasterBean.get("wo_item_name"));
				if(existingWOItemBean == null) {
					woItemId = woItemMasterDAO.getNextSequence();
					itemMasterBean.set("wo_item_id", woItemId);
					itemMasterBean.set("status", "A");

					woItemMasterDAO.insert(con, itemMasterBean);
				
				} else {
					woItemId = (Integer)existingWOItemBean.get("wo_item_id");
				}
				
				if (deleted[i].equalsIgnoreCase("false")) {
					bean.set("status", 
							((String)woMainBean.get("status")).equals("A") //if
							?  (!itemStatus[i].equals("R") ? (String)woMainBean.get("status") : itemStatus[i]) 
							: (String)woMainBean.get("status"));//else
					if(dbValue != null && dbValue.equals("true")) {
						Map<String, Object> whereKeyValues = new HashMap<String, Object>();
						whereKeyValues.put("wo_no", woNO);
						whereKeyValues.put("wo_item_id", woItemId);
						BasicDynaBean basicDynaBean = woDetailsBean.findByKey(whereKeyValues);
						
						if(basicDynaBean != null) {
							woDetailsBean.update(con, bean.getMap(), "wo_details_id", basicDynaBean.get("wo_details_id"));
						} else {
							bean.set("wo_no", woNO);
							bean.set("wo_details_id", woDetailsBean.getNextSequence());
							bean.set("wo_item_id", woItemId);
							woDetailsBean.insert(con, bean);
						}
						
					} else {
						bean.set("wo_no", woNO);
						bean.set("wo_details_id", woDetailsBean.getNextSequence());
						bean.set("wo_item_id", woItemId);
						woDetailsBean.insert(con, bean);
					}
					
				} else {
					LinkedHashMap<String, Object> whereMap = new LinkedHashMap<String, Object>();
					whereMap.put("wo_no", woNO);
					whereMap.put("wo_item_id", woItemId);
					woDetailsBean.delete(con, whereMap);
				}
				status = true;
				if (req.getParameter("_printAfterSave").equals("Y")) {
					ActionRedirect url = new ActionRedirect(mapping.findForward("woPrintRedirect"));
					url.addParameter("wo_no", woNO);
					printUrls = new ArrayList<String>();
					printUrls.add(req.getContextPath() + url.getPath());
					session.setAttribute("printURLs", printUrls);
				}
			}
			
	    } finally {
			DataBaseUtil.commitClose(con, status);
		}
		
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("wo_no", woNO);
		return redirect;
    }
 
	@IgnoreConfidentialFilters
    public  ActionForward generateWOprint(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response) throws Exception {
		Map params = new HashMap();
		String woNo=request.getParameter("wo_no");
		if (woNo != null) {
			GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
			List<BasicDynaBean> woOrderItemsList = WorkOrderDAO.getWOItems(woNo);
			BasicDynaBean woBean = WorkOrderDAO.getWODetails(woNo);
			BasicDynaBean suppBean = WorkOrderDAO.getSupplierDetails(woNo);
			
			params.put("items", woOrderItemsList);
			params.put("hospital_tin",dto.getHospitalTin());
			params.put("hospital_pan",dto.getHospitalPan());
			params.put("hospital_service_regn_no",dto.getHospitalServiceRegnNo());
			params.put("woBean", woBean);
			params.put("suppBean", suppBean);
			
			PrintTemplatesDAO printTemplateDAO = new PrintTemplatesDAO();
			BasicDynaBean printprefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_STORE);
			PrintTemplate template = PrintTemplate.WorkOrderPrintTemplate;
			String templateContent = printTemplateDAO.getCustomizedTemplate(template);
			
		    Template t = null;
		        if(templateContent == null || templateContent.equals("")){
		       		t = AppInit.getFmConfig().getTemplate(template.getFtlName() + ".ftl");
		        }else{
		        	StringReader reader = new StringReader(templateContent);
		        	t = new Template(null, reader, AppInit.getFmConfig());
		        }
		   
		    HtmlConverter htmlConverter = new HtmlConverter();
			StringWriter writer = new StringWriter();
			t.process(params, writer);
			String printContent = writer.toString();
			if (printprefs.get("print_mode").equals("P")) {
				OutputStream os = response.getOutputStream();
				response.setContentType("application/pdf");
				htmlConverter.writePdf(os, printContent, "WorkOrderPrintTemplate", printprefs, false, false, true, true, true, false);
			} else {
				String textReport = null;
				textReport = new String(htmlConverter.getText(printContent, "WorkOrderPrintTemplate", printprefs, true, true));
				request.setAttribute("textReport", textReport);
				request.setAttribute("textColumns", printprefs.get("text_mode_column"));
				request.setAttribute("printerType", "DMP");
				return mapping.findForward("textPrintApplet");
			}
		}
		return null;
	}
}
