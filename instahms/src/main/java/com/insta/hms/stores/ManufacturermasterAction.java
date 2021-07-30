package com.insta.hms.stores;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.AbstractDataHandlerAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.csvutils.TableDataHandler;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ManufacturermasterAction extends AbstractDataHandlerAction {
	ManufacturermasterDAO dao = new ManufacturermasterDAO();
	public ActionForward getManufacturerDetails(ActionMapping mapping,ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Map map= getParameterMap(request);
		PagedList list = ManufacturermasterDAO.searchManufacturers(map, ConversionUtils.getListingParameter(map));
		request.setAttribute("pagedList", list);
		return mapping.findForward("getManufacturerLists");
	}

	public ActionForward getManfDetailsScreen(ActionMapping mapping,ActionForm form, HttpServletRequest request,HttpServletResponse response)
	throws ServletException, IOException,SQLException {

		String manfId = request.getParameter("manufacturer_Id");
		JSONSerializer js = new JSONSerializer().exclude("class");
		if (manfId!= null) {
			BasicDynaBean manfdto = ManufacturermasterDAO.getSelectedManfDetails(manfId);
			request.setAttribute("fromDB", "Y");
			request.setAttribute("manfdto", manfdto);
			request.setAttribute("manufacturersLists", js.serialize(ManufacturermasterDAO.getManufacturersNamesAndIds()));
		}
		ArrayList<String> manfNames = ManufacturermasterDAO.getAllManfs();
		request.setAttribute("manfNamesJSON", js.serialize(manfNames));
		return mapping.findForward("populatemanuacturerdetails");
	}


  public ActionForward insertOrUpdatemanufacturerDetails(ActionMapping mapping,ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException, SQLException {

		FlashScope flash = FlashScope.getScope(request);
		BasicDynaBean bean = null;
		ActionRedirect redirect = new ActionRedirect("ManufacturerDetails.do");
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		String operation = request.getParameter("operation");
		String manf_code = request.getParameter("manf_code");
		String manf_name = request.getParameter("manf_name");
		String manufacturer = request.getParameter("manufacturer");
		Map params = request.getParameterMap();
		List errors = new ArrayList();
		boolean flag = true;
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			if(operation.equalsIgnoreCase("insert")){
				bean = dao.getBean();
				if (dao.exist("manf_name",manf_name)){
					 flash.put("error", "Duplicate Manfucturer Name: "+manf_name+ " already exists");
					 redirect.addParameter("manufacturer_Id", bean.get("manf_code"));
					 flag = false;
				} else{
				ConversionUtils.copyToDynaBean(params, bean, errors);
				manf_code = AutoIncrementId.getSequenceId("manufacturer_id_seq","Manufacturer");
				bean.set("manf_code", manf_code);
				if (flag) flag = dao.insert(con, bean);
				}
			}else{
					bean = dao.getBean();
					if(!manufacturer.equals(manf_name)) {
						if (dao.exist("manf_name",manf_name)){
							 flash.put("error", "Duplicate Manfucturer Name: "+manf_name+ " already exists");
							 redirect.addParameter("manufacturer_Id", bean.get("manf_code"));
							 flag = false;
						}
					}
					ConversionUtils.copyToDynaBean(params, bean, errors);
					bean.set("manf_code", manf_code);
					if (flag) flag = dao.update(con, bean.getMap(), "manf_code", manf_code) > 0;
			}
			if (flag) {
				con.commit();
				if (operation.equalsIgnoreCase("insert")) flash.put("success","Manufacturer :"+manf_name+" Details are Successfully Inserted");
				else flash.put("success","Manufacturer :"+manf_name+" Details are Successfully Updated");
				redirect.addParameter("_method", "getManufacturerDetails");
				redirect.addParameter("sortOrder", "manf_name");
				redirect.addParameter("sortReverse", "false");
			} else{
				redirect.addParameter("_method", "getManfDetailsScreen");
				redirect.addParameter("sortOrder", "manf_name");
				redirect.addParameter("sortReverse", "false");

			}
		}catch (Exception e) {
			con.rollback();
			flash.put("error","Transaction Failure");
		}
		finally {
			DataBaseUtil.closeConnections(con, null);
			//redirect.addParameter("manufacturer_Id", bean.get("manf_code"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		}
        return redirect;
	}

	private static TableDataHandler masterHandler = null;

	protected TableDataHandler getDataHandler() {
		if (masterHandler == null) {
			masterHandler = new TableDataHandler(
					"manf_master",		// table name
					new String[]{"manf_code"},	// keys
					new String[]{"manf_name", "manf_mnemonic", "status",
						"manf_address", "manf_city", "manf_state",
						"manf_country", "manf_pin", "manf_phone1", "manf_phone2",
				 		"manf_fax", "manf_mailid", "manf_website",
					},
					new String[][]{	/* masters */ },
					null
			);
		}

		masterHandler.setSequenceName("manufacturer_id_seq");
		masterHandler.setAutoIncrName("Manufacturer");
		return masterHandler;
	}

}
