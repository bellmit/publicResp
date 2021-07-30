package com.insta.hms.stores;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import flexjson.JSONSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author irshad.mohamad
 *
 */

public class ItemStockLevelDetailsAction extends DispatchAction {
	
	static Logger logger = LoggerFactory.getLogger(StoresItemBatchDetailsAction.class);
	ItemStockLevelDetailsDAO idao = new ItemStockLevelDetailsDAO();
	JSONSerializer js = new JSONSerializer().exclude("class");

	@IgnoreConfidentialFilters
	public ActionForward list(ActionMapping map,ActionForm form,
			HttpServletRequest req,HttpServletResponse res) throws ServletException,Exception {
		return map.findForward("list");
	}

	@IgnoreConfidentialFilters
	public ActionForward searchList(ActionMapping map,ActionForm form,
			HttpServletRequest req,HttpServletResponse res) throws ServletException,Exception {
		PagedList pagedList = idao.getItemStockLevelDetailsList(req.getParameterMap(), ConversionUtils.getListingParameter(req.getParameterMap()), (Integer)req.getSession(false).getAttribute("centerId"));
		req.setAttribute("after_decimal_digits",GenericPreferencesDAO.getPrefsBean().get("after_decimal_digits")!=null?GenericPreferencesDAO.getPrefsBean().get("after_decimal_digits"):0);
		req.setAttribute("pagedList", pagedList);
		return map.findForward("list");
	}

}