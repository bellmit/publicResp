	package com.insta.hms.master.PrinterSettingsMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class PrinterSettingsAction extends DispatchAction {

	static Logger log = LoggerFactory.getLogger(PrinterSettingsAction.class);

	public ActionForward list(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws IOException,
		   ServletException, SQLException {
		PrinterSettingsDAO dao  = new PrinterSettingsDAO();
		Map listingParams = ConversionUtils.getListingParameter(request.getParameterMap());

		PagedList pagedList = dao.getPrintDefinition(listingParams);
		request.setAttribute("pagedList",pagedList);

		return mapping.findForward("list");
	}

	public ActionForward add(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response)throws Exception{
		PrinterSettingsDAO dao = new PrinterSettingsDAO();
		BasicDynaBean bean = dao.getBean();
		if(bean.get("printer_definition_name")==null ){
			bean.set("print_mode", "P");
			bean.set("footer", "Y");
			bean.set("footer_vertical_position", "m");
			bean.set("continuous_feed", "N");
			bean.set("page_height", 842);
			bean.set("top_margin", 100);
			bean.set("bottom_margin", 20);
			bean.set("left_margin", 50);
			bean.set("font_name", "Sans-Serif");
			bean.set("font_size", 10);
			bean.set("page_width", 400);
			bean.set("text_mode_column", 80);
			bean.set("text_mode_extra_lines", 15);
			bean.set("right_margin", 15);
			bean.set("repeat_patient_info", "N");
			bean.set("page_number", "Y");
			bean.set("pg_no_position", "R");
			bean.set("pg_no_font_size", 12);
			bean.set("pg_no_vertical_position", "m");
		}
		request.setAttribute("bean", bean);
		return mapping.findForward("addshow");
	}

	public ActionForward create(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws Exception {

			Map params = request.getParameterMap();
			List errors  = new ArrayList();
			Connection con = null;
			ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
			boolean success = false;
			try{
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);

				PrinterSettingsDAO dao = new PrinterSettingsDAO();
				BasicDynaBean bean = dao.getBean();
				ConversionUtils.copyToDynaBean(params, bean, errors);

				if(errors.isEmpty()){
					BasicDynaBean exists = dao.findByKey("printer_definition_name",
							bean.get("printer_definition_name"));
					if(exists == null){
						bean.set("printer_id", dao.getNextSequence());
						success = dao.insert(con,bean);
						if (success) {
							FlashScope flash = FlashScope.getScope(request);
							redirect = new ActionRedirect(mapping.findForward("showRedirect"));
							flash.put("success", "Print Definition insertd successfully");
							redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
							redirect.addParameter("printer_id", bean.get("printer_id"));
							return redirect;
						}else {
							request.setAttribute("error", "Failed to add Print Definition");
						}
					} else {
						request.setAttribute("error", "Printer Definition already exists ");
					}
				}else {
					request.setAttribute("error", "Incorrectly formated values supplied ");
				}
			}finally{
				DataBaseUtil.commitClose(con, success);
			}
			return mapping.findForward("list");

	}

	public ActionForward show(ActionMapping mapping,ActionForm form,
		HttpServletRequest request, HttpServletResponse response)
		throws IOException,SQLException,Exception{
		PrinterSettingsDAO dao = new PrinterSettingsDAO();
		BasicDynaBean bean = dao.findByKey("printer_id", Integer.parseInt(request.getParameter("printer_id")));
		request.setAttribute("bean", bean);
		return mapping.findForward("addshow");
	}

	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException,SQLException,Exception{
		Connection con = null;
		boolean success = false;
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map params = request.getParameterMap();
			List errors = new ArrayList();
			PrinterSettingsDAO  dao = new PrinterSettingsDAO();
			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean,errors);

			Object key = Integer.parseInt(request.getParameter("printer_id"));
			Map<String, Object> keys = new HashMap<String,Object>();
			keys.put("printer_id", key);
			if(errors.isEmpty()){
				BasicDynaBean exists = dao.findByKey("printer_definition_name",
						bean.get("printer_definition_name"));
				if (exists ==null && (new Integer(0).equals(exists.get("printer_id")))) {
					request.setAttribute("error", "Printer Name already exists");
				}else {
					success = ( dao.update(con,bean.getMap(),keys) > 0);
					if(success){
						FlashScope flash = FlashScope.getScope(request);
						flash.put("success", "Printer definition updated successfully ");
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						redirect.addParameter("printer_id",request.getParameter("printer_id"));
						return redirect;
					}else {
						request.setAttribute("error", "Failed to update print definition detials ");
					}
				}
			}else{
				request.setAttribute("error" , "incorrectly formated values supplied ");
			}
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		redirect.addParameter("printer_id",request.getParameter("printer_id"));
		return redirect;
	}

	public ActionForward deletePrinterDefinition(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse respone)
		throws IOException, ServletException, Exception {
		Connection con = null;
		String[] printDefinition  = request.getParameterValues("deleteDefinition");
		PrinterSettingsDAO dao = new PrinterSettingsDAO();
		String msg = "";
		String error = "";
		boolean success = false;
		try{
		con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			if (printDefinition != null){
				for (String printer_id : printDefinition) {
					if(dao.delete(con, "printer_id", Integer.parseInt(printer_id))){
						success = true;
					}else{
						success = false;
						break;
					}
				}
				if (success){
					msg = ((printDefinition.length >1)?"Printer Definitons":"Printer Definiton") +
						" deleted Successfully ";
				}else {
					error = "Failed to delete printer definitions";
				}
			}//end if
		} catch(SQLException e){
			success= false;
			if(DataBaseUtil.isForeignKeyViolation(e))
				error = "Printer Definition Name is used in print master";
			else
				throw(e);
		}finally{
			DataBaseUtil.commitClose(con, success);
		}

		FlashScope flash = FlashScope.getScope(request);
		flash.put("error", error);
		flash.put("success", msg);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}
}

