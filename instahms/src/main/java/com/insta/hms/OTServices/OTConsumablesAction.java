package com.insta.hms.OTServices;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Preferences;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.FlashScope;

import org.apache.commons.beanutils.DynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

	public class OTConsumablesAction extends DispatchAction{
		static Logger log = LoggerFactory.getLogger(OTServicesDashboard.class);
		public ActionForward getModifyOtConsumablesScreen(ActionMapping mapping,ActionForm form,
				HttpServletRequest request,HttpServletResponse responce)throws Exception {

		String operationType = request.getParameter("operation_type");
		operationType = (operationType != null && !operationType.isEmpty()) ? operationType : "P";
		List consumables = OTServicesDAO.getOTConsumablesUsed(Integer.parseInt(request.getParameter("prescribedId")),request.getParameter("operation_id"), operationType);
		if(consumables.size() == 0){
			consumables = new OTServicesDAO().getOTConsumables(request.getParameter("operation_id"));
		}
		request.setAttribute("consumables", consumables);
		return mapping.findForward("modifyotconsumables");
	}
	/**
	 * Reduce the gived qty of consumable from inventory stock and keeps
	 * track of reduced qty in inventory.
	 * @param mapping
	 * @param form
	 * @param request
	 * @param responce
	 * @return
	 * @throws Exception
	 */
	public ActionForward modifyOtConsumables(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse responce)throws Exception{
		Connection con = null;
		boolean result = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map params = request.getParameterMap();
			String[] prescribed_id = (String[])params.get("prescribed_id");
			String[] reagent_usage_seq = (String[])params.get("reagent_usage_seq");
			String visitId = request.getParameter("patient_id");
			String operationType = request.getParameter("operation_type");
			boolean isPrimaryOperation = true;
			isPrimaryOperation = (operationType != null && !operationType.isEmpty()) ? operationType.equals("S") ? false : isPrimaryOperation : isPrimaryOperation;
			Preferences pref = (Preferences)request.getSession(false).getAttribute("preferences");
			String opId = request.getParameter("operation_id");
			String modConsumableActive = "Y";
			if ( (pref!=null) && (pref.getModulesActivatedMap() != null) ) {
				modConsumableActive = (String) pref.getModulesActivatedMap().get("mod_consumables_flow");
		        if (modConsumableActive == null || "".equals(modConsumableActive)) {
		        	modConsumableActive = "N";
		        }
		    }
			List<DynaBean> reagentsRegired = new ArrayList<DynaBean>();
			List errors = new ArrayList();
			DynaBeanBuilder builder = new DynaBeanBuilder();
			builder.add("item_id", Integer.class).add("qty",BigDecimal.class).add("redusing_qty",BigDecimal.class);
			DynaBean reagentsbean = builder.build();

			FlashScope flash = FlashScope.getScope(request);
			if (reagent_usage_seq != null) {

				for (int i =0 ; i<reagent_usage_seq.length; i++){
					reagentsbean = builder.build();
					ConversionUtils.copyIndexToDynaBean(params, i, reagentsbean, errors);
					reagentsRegired.add(reagentsbean);
				}

				result = OTServicesBO.saveOTConsumableUsage(con, opId, Integer.parseInt(prescribed_id[0]),
					reagent_usage_seq, reagentsRegired, pref, isPrimaryOperation);
			}

			if (result) {
				con.commit();
				flash.success("OT Consumable details updated successfully..");
			} else {
				con.rollback();
				flash.error("Failed to update OT Consumable details..");
			}
			ActionRedirect redirect = new ActionRedirect(mapping.findForward("consumablesRedirect"));
			redirect.addParameter("operation_id", opId);
			redirect.addParameter("operation_name", request.getParameter("operation_name"));
			redirect.addParameter("patient_id", visitId);
			redirect.addParameter("prescribedId", prescribed_id[0]);
			redirect.addParameter("operation_type", operationType);
			if(modConsumableActive.equals("Y"))
				redirect.addParameter("operation_details_id", request.getParameter("operation_details_id"));
			redirect.addParameter(FlashScope.FLASH_KEY,flash.key());
			return redirect;
		} finally {
			DataBaseUtil.commitClose(con, result);
		}
	}

}
