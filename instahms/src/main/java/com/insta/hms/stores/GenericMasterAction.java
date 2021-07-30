package com.insta.hms.stores;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.AbstractDataHandlerAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.MigratedTo;
import com.insta.hms.csvutils.TableDataHandler;
import com.insta.hms.mdm.stores.genericnames.GenericNamesController;
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

@MigratedTo(GenericNamesController.class) // Partially migrated migration
public class GenericMasterAction extends AbstractDataHandlerAction {

	GenericDAO scDao = new GenericDAO("generic_sub_classification_master");
	
    private static final GenericDAO genericName = new GenericDAO("generic_name");
    
	JSONSerializer js = new JSONSerializer();

	public ActionForward getGenericDashBoard(ActionMapping mapping,ActionForm form, HttpServletRequest request,HttpServletResponse response)
	throws Exception {
		Map map= getParameterMap(request);
		PagedList list = GenericMasterDAO.searchGenerics(map, ConversionUtils.getListingParameter(map));
		request.setAttribute("pagedList", list);
		return mapping.findForward("genericdashboard");
	}

	public ActionForward getGenericDetailsScreen(ActionMapping mapping,ActionForm form, HttpServletRequest request,HttpServletResponse response)
	throws ServletException, IOException,SQLException {

		JSONSerializer js = new JSONSerializer().exclude("class");
		String genericId = request.getParameter("generic_id");
		if (genericId!= null) {
			BasicDynaBean bean = genericName.findByKey("generic_code", genericId);
			request.setAttribute("gendto", bean);
			request.setAttribute("genericDetailsLists", js.serialize(GenericMasterDAO.getGenericDetailsNamesAndIds()));
		}
		ArrayList<String> generics = GenericMasterDAO.getGenericNamesInMaster();
		request.setAttribute("scList", js.serialize(ConversionUtils.copyListDynaBeansToMap(scDao.listAll())));
		request.setAttribute("generics", js.serialize(ConversionUtils.copyListDynaBeansToMap(genericName.listAll())));
//		request.setAttribute("generics", generics);
		return mapping.findForward("genericdetails");
	}

	public ActionForward saveGenericDetails(ActionMapping mapping,ActionForm form, HttpServletRequest request,HttpServletResponse response)
	throws SQLException,Exception {
		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect("StoresMastergendetails.do");
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("_method", "getGenericDetailsScreen");
		redirect.addParameter("sortOrder", "generic_name");
		redirect.addParameter("sortReverse", "false");
		BasicDynaBean bean = null;
		String operation = request.getParameter("operation");
		String generic_code = request.getParameter("generic_code");
		String generic_name = request.getParameter("generic_name");
		Map params = request.getParameterMap();
		List errors = new ArrayList();
		boolean flag = true;
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			if(operation.equalsIgnoreCase("insert")){
				bean = genericName.getBean();
				ConversionUtils.copyToDynaBean(params, bean, errors);
				generic_code = AutoIncrementId.getSequenceId("generic_sequence", "GENERICNAME");
				bean.set("generic_code", generic_code);
				if (flag) flag = genericName.insert(con, bean);
			}else{
				bean = genericName.getBean();
				ConversionUtils.copyToDynaBean(params, bean, errors);
				if (flag) flag = genericName.update(con, bean.getMap(), "generic_code", generic_code) > 0;
			}
			if (flag) {
				con.commit();
				if (operation.equalsIgnoreCase("insert")) flash.put("success","Generic :"+generic_name+" Details are Successfully Inserted");
				else flash.put("success","Generic :"+generic_name+" Details are Successfully Updated");
			}
		}catch (Exception e) {
			con.rollback();
			flash.put("error","Transaction Failure");
		}
		finally {
			DataBaseUtil.closeConnections(con, null);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		}
		redirect.addParameter("generic_id", bean.get("generic_code"));
        return redirect;
	}

	private static TableDataHandler masterHandler = null;

	protected TableDataHandler getDataHandler() {
		if (masterHandler == null) {
			masterHandler = new TableDataHandler(
					"generic_name",		// table name
					new String[]{"generic_code"},	// keys
					new String[]{"generic_name","status","standard_adult_dose","criticality",
						"classification_id","sub_classification_id"},
					new String[][]{
						{"classification_id", "generic_classification_master", 
							"classification_id", "classification_name" },
						{"sub_classification_id", "generic_sub_classification_master",
							"sub_classification_id", "sub_classification_name" }
					},
					null
			);
		}
		masterHandler.setSequenceName("generic_sequence");
		masterHandler.setAutoIncrName("GENERICNAME");
		return masterHandler;
	}
}
