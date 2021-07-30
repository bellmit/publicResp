package com.insta.hms.master.SupplierRateContract;

import com.insta.hms.master.CenterAssociationAction;
import com.insta.hms.master.CenterAssociationDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SupplierRateContractCenterApplicability extends CenterAssociationAction {

	public ActionForward getScreen(ActionMapping mapping, ActionForm form, HttpServletRequest request, 
			HttpServletResponse response) throws SQLException, IOException {
		
		getAssociationData(mapping, form, request, response,Integer.valueOf(request.getParameter("supplier_rate_contract_id")),"supplier_rate_contract_name");
		BasicDynaBean supplierContractBean = SupplierRateContractDAO.getSupplierNameFromContract(request.getParameter("supplier_rate_contract_id")!=null?request.getParameter("supplier_rate_contract_id"):"-1");
		ArrayList centerList = SupplierRateContractDAO.getEditSupplierRateContractCenters(request.getParameter("supplier_rate_contract_id")!=null?request.getParameter("supplier_rate_contract_id"):"-1",(String)supplierContractBean.get("supplier_code"));
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("status", supplierContractBean.get("status"));
		request.setAttribute("centerList", js.serialize(centerList));
		return mapping.findForward("centerAssociation");
	}

	@Override
	public CenterAssociationDAO getCenterAssociationDAO() {
		return new SupplierRateCenterDAO();
	}
}
