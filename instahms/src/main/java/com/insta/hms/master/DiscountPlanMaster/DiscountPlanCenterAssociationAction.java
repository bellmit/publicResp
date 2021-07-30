package com.insta.hms.master.DiscountPlanMaster;

import com.insta.hms.master.CenterAssociationAction;
import com.insta.hms.master.CenterAssociationDAO;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DiscountPlanCenterAssociationAction extends
		CenterAssociationAction {

	public ActionForward getScreen(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {
		getAssociationData(mapping, form, request, response,Integer.parseInt(request.getParameter("discount_plan_id")),"discount_plan_name");
		return mapping.findForward("centerAssociation");
	}

	@Override
	public CenterAssociationDAO getCenterAssociationDAO() {
		// TODO Auto-generated method stub
		return new DiscountPlanCenterAssociationDAO();
	}

}
