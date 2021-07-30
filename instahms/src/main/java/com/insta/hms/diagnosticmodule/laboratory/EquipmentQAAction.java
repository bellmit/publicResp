package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.testequipmentmasters.EquipmentResultsDAO;
import com.insta.hms.testequipmentmasters.TestEquipmentMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import flexjson.JSONSerializer;

public class EquipmentQAAction extends BaseAction {

	static EquipmentTestConductedDAO eqConductedDAO = new EquipmentTestConductedDAO();
	static TestEquipmentMasterDAO testEqMasterDAO  = new TestEquipmentMasterDAO();
	static EquipmentResultsDAO eqresultsDAO = new EquipmentResultsDAO();
	static GenericDAO eqTestValuesDAO = new GenericDAO("equipment_test_values");

	@IgnoreConfidentialFilters
	public ActionForward list(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws SQLException,ParseException,IOException{

		Map filterParams = new HashMap(request.getParameterMap());
		request.setAttribute("pagedList",new EquipmentQABO().getEquipmentsDetails(filterParams));
		request.setAttribute("category", mapping.getProperty("category"));
		request.setAttribute("existingEquipments", new JSONSerializer().exclude("class").serialize(
				ConversionUtils.listBeanToListMap(testEqMasterDAO.listAll(null, "status", "A"))));
		return mapping.findForward("list");
	}

	@IgnoreConfidentialFilters
	public ActionForward add(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws SQLException,ParseException,IOException{

		String equipmentId = request.getParameter("equipmentId");
		List<BasicDynaBean> eqCponductionDet = eqConductedDAO.newEquipmentConductionDetails(equipmentId);

		request.setAttribute("equipmentCondDetails",null);

		request.setAttribute("equipmentDetails", testEqMasterDAO.findByKey("eq_id", Integer.parseInt(equipmentId)));

		request.setAttribute("equipmentResultDetails",eqCponductionDet);


		request.setAttribute("category", mapping.getProperty("category"));
		return mapping.findForward("add");

	}

	@IgnoreConfidentialFilters
	public ActionForward recordQuality(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws SQLException,IOException{
		request.setAttribute("category", mapping.getProperty("category"));
		request.setAttribute("equipmentId", request.getParameter("equipmentId"));
		Map params = request.getParameterMap();
		String[] resultlabelId = (String[]) params.get("resultlabel_id");
		String[] testRecordComplete = (String[]) params.get("test_record_complete");

		Connection con = null;
		boolean success = true;
		int eqConductedId = 0;
		try{

			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			//transaction
			BasicDynaBean eqCondutBean  = eqConductedDAO.getBean();
			ActionForward errForward = copyToDynaBean(request, response, eqCondutBean);
			if (errForward != null) return errForward;
			eqConductedId = eqConductedDAO.getNextSequence();
			eqCondutBean.set("equipment_conducted_id", eqConductedId);
			eqCondutBean.set("test_record_complete", testRecordComplete != null ? "Y" : "N");

			success = eqConductedDAO.insert(con, eqCondutBean);
			List errorList = new ArrayList();
			List<BasicDynaBean> eqResultValList = new ArrayList<BasicDynaBean>();
			if ( !success )
				return errorResponse(request, response, "Failed to record values");

			for(int i = 0;i<resultlabelId.length;i++){
				BasicDynaBean eqResultBean = eqTestValuesDAO.getBean();
				ConversionUtils.copyIndexToDynaBean( params, i, eqResultBean, errorList );
				eqResultBean.set("equipment_conducted_id", eqConductedId);
				eqResultValList.add(eqResultBean);
			}

			success = eqTestValuesDAO.insertAll(con, eqResultValList);
			if ( !success )
				return errorResponse(request, response, "Failed to record values");

		}finally{
			DataBaseUtil.commitClose(con, success);
		}

		 ActionRedirect redirect = new ActionRedirect(mapping.findForward("showredirect"));
		 FlashScope flash = FlashScope.getScope(request);
		 redirect.addParameter("eqConductedId", eqConductedId);
		 redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward editQuality(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws SQLException,IOException{
		request.setAttribute("category", mapping.getProperty("category"));
		request.setAttribute("equipmentId", request.getParameter("equipmentId"));
		Map params = request.getParameterMap();
		String[] resultlabelId = (String[]) params.get("resultlabel_id");
		String[] testRecordComplete = (String[]) params.get("test_record_complete");

		Connection con = null;
		boolean success = true;
		BasicDynaBean eqCondutBean = null;
		try{

			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			//transaction
			eqCondutBean  = eqConductedDAO.getBean();
			ActionForward errForward = copyToDynaBean(request, response, eqCondutBean);
			if (errForward != null) return errForward;
			eqCondutBean.set("test_record_complete", testRecordComplete != null ? "Y" : "N");

			success = eqConductedDAO.update(con, eqCondutBean.getMap(),
					"equipment_conducted_id",eqCondutBean.get("equipment_conducted_id")) > 0;
			List errorList = new ArrayList();
			if ( !success )
				return errorResponse(request, response, "Failed to record values");

			Map<String ,Object> keys = new HashMap<String ,Object>();
			for(int i = 0;i<resultlabelId.length;i++){
				BasicDynaBean eqResultBean = eqTestValuesDAO.getBean();
				ConversionUtils.copyIndexToDynaBean( params, i, eqResultBean, errorList );
				keys.put("equipment_conducted_id", (Integer)eqCondutBean.get("equipment_conducted_id"));
				keys.put("resultlabel_id", Integer.parseInt(resultlabelId[i]));
				keys.put("equipment_id", (Integer)eqCondutBean.get("equipment_id"));

				success &= eqTestValuesDAO.update(con, eqResultBean.getMap(),keys) > 0;
			}

			if ( !success )
				return errorResponse(request, response, "Failed to record values");

		}finally{
			DataBaseUtil.commitClose(con, success);
		}

		 ActionRedirect redirect = new ActionRedirect(mapping.findForward("showredirect"));
		 FlashScope flash = FlashScope.getScope(request);
		 redirect.addParameter("eqConductedId", eqCondutBean.get("equipment_conducted_id"));
		 redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward show(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws SQLException,IOException{

		String eqConductedId = request.getParameter("eqConductedId");
		request.setAttribute("category", mapping.getProperty("category"));

		BasicDynaBean equpCondDetails =
			eqConductedDAO.findByKey("equipment_conducted_id", Integer.parseInt(eqConductedId));
		request.setAttribute("equipmentCondDetails",equpCondDetails);

		request.setAttribute("equipmentDetails",
			testEqMasterDAO.findByKey("eq_id", equpCondDetails.get("equipment_id")));

		List<BasicDynaBean> eqCponductionDet =
			eqConductedDAO.equipmentConductionDetails(
					equpCondDetails.get("equipment_id").toString(),eqConductedId );
		request.setAttribute("equipmentResultDetails",eqCponductionDet);
		return mapping.findForward("show");
	}
}
