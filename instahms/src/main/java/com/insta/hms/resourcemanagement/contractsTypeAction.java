package com.insta.hms.resourcemanagement;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class contractsTypeAction  extends DispatchAction {

  @IgnoreConfidentialFilters
	public ActionForward list(ActionMapping m,ActionForm f,
			HttpServletRequest req,HttpServletResponse res)throws IOException, ServletException, Exception {

		contractsTypeDAO dao = new contractsTypeDAO();
		Map map= req.getParameterMap();
		PagedList pagedList = dao.search(map, ConversionUtils.getListingParameter(req.getParameterMap()),
					"contract_type_id");
		req.setAttribute("pagedList", pagedList);

		return m.findForward("list");
	}

  @IgnoreConfidentialFilters
	public ActionForward add(ActionMapping m,ActionForm f,
			HttpServletRequest req,HttpServletResponse resp)throws IOException, ServletException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		ArrayList<String> avllist = (ArrayList)contractsTypeDAO.getAllcontracts();
		req.setAttribute("avllist", js.serialize(avllist));

		return m.findForward("addshow");
	}

	private static final String[] STRING_FIELDS = { "contract_type","status" };

	@IgnoreConfidentialFilters
	public ActionForward create(ActionMapping mapping, ActionForm af,
			HttpServletRequest request, HttpServletResponse response)
			throws SQLException, FileNotFoundException, IOException {

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		contractsTypeDAO dao = new contractsTypeDAO();

		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = null;

		HashMap fields = new HashMap();
		ConversionUtils.copyStringFields(request.getParameterMap(), fields,
				STRING_FIELDS, null);
		int contract_type_id = dao.getNextSequence();
		fields.put("contract_type_id ", contract_type_id);

		BasicDynaBean exists = dao.findByKey("contract_type", fields.get("contract_type"));

		if(exists == null){
			boolean status = true;
			status = dao.insertContractDetails(con, fields);

			if (status) {
				con.commit();
				flash.put("success", "ContractType details saved successfully..");
				redirect = new ActionRedirect(mapping.findForward("showRedirect"));
				redirect.addParameter("contract_type_id", new Integer(contract_type_id));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				con.close();
				return redirect;
			} else {
				con.rollback();
				flash.put("error", "Failed to add the details");
			}
		}else{
			flash.put("error", "ContractType Name already exists..");

		}
		redirect = new ActionRedirect(mapping.findForward("addRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException, Exception{

		contractsTypeDAO dao = new contractsTypeDAO();
		String contract_typeId = req.getParameter("contract_type_id");
		if (contract_typeId != null) {
			BasicDynaBean form = dao.getContractDetails(Integer.parseInt(contract_typeId));
			req.setAttribute("bean", form.getMap());
		}

		JSONSerializer js = new JSONSerializer().exclude("class");
		ArrayList<String> avllist = (ArrayList)contractsTypeDAO.getAllcontracts();
		req.setAttribute("avllist", js.serialize(avllist));

		return m.findForward("addshow");
	}

	@IgnoreConfidentialFilters
	public ActionForward update(ActionMapping m,ActionForm af,
			HttpServletRequest req, HttpServletResponse resp)
			throws ServletException,IOException, Exception {

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		contractsTypeDAO dao = new contractsTypeDAO();
		String contract_typeId = req.getParameter("contract_type_id");

		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));

		HashMap fields = new HashMap();
		ConversionUtils.copyStringFields(req.getParameterMap(), fields, STRING_FIELDS, null);

		boolean status = true;
		status = dao.updateFields(con,Integer.parseInt(contract_typeId),fields);

		if(status){
			con.commit();
			flash.put("success", "ContractType details updated successfully..");
			con.close();
		}else{
			con.rollback();
			req.setAttribute("error", "Failed to update ContractType details..");
		}
		redirect.addParameter("contract_type_id", req.getParameter("contract_type_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}


}