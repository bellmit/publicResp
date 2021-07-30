package com.insta.hms.master;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.CityMaster.CityMasterDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class CenterAssociationAction extends BaseAction {

	protected void getAssociationData(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response,Object entityId,String entityName) throws SQLException, IOException {


		CenterAssociationDAO dao = getCenterAssociationDAO();
		BasicDynaBean entityBean = dao.getAssociatedBean(entityId);
		Map<String, Object> map = new HashMap<String, Object>(entityBean.getMap());
		map.put("entity_name", entityBean.get(entityName));

		request.setAttribute("bean",map);
		request.setAttribute("applicable_centers", dao.getAssociatedCenters(entityId));

		List centers = CenterMasterDAO.getCentersList();
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("cities_json", js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(
				new CityMasterDAO().listAll("city_name"))));
		request.setAttribute("centers_json", js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(centers)));

	}

	public abstract CenterAssociationDAO getCenterAssociationDAO();

	public ActionForward updateAssociation(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException  {

		CenterAssociationDAO dao = getCenterAssociationDAO();
		String assocKey = dao.getAssociationKey();
		int entityId = Integer.parseInt((String) request.getParameter("entity_id"));
		String entityIdCol = (String)request.getParameter("entity_id_column_name");;
		String error = null;
		Connection con = null;
		try {
			txn : {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				String app_for_centers = request.getParameter("applicable_for_centers");
				if (app_for_centers != null) {
					if (app_for_centers.equals("all")) {
						BasicDynaBean bean = dao.getBean();

						if (!dao.deleteAssociation(con, entityId)) {
							error = "Failed to delete doctor center association of few centers..";
							break txn;
						}

						if (!dao.insertAssocation(con, 0, entityId)) {
							error = "Failed to update doctor center association for all centers..";
							break txn;
						}

					} else {
						if (!dao.deleteAssociation(con, 0, entityId)) {
							error = "Failed to delete doctor center association for all centers..";
							break txn;
						}
						String[] centerIds = request.getParameterValues("center_id");
						String[] assocIds = request.getParameterValues(assocKey);

						String[] assocDeleted = request.getParameterValues("cntr_delete");
						String[] assocEdited = request.getParameterValues("cntr_edited");
						String[] assocStatus = request.getParameterValues("center_status");
						if (!dao.updateAssociations(con, entityId, centerIds, assocIds, assocStatus, assocDeleted, assocEdited)) {
							error = "Failed to insert the  doctor center association for selected centers..";
							break txn;
						};
					}
				}

			}
		} finally {
			DataBaseUtil.commitClose(con, error == null);
		}
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		FlashScope flash = FlashScope.getScope(request);

		flash.put("error", error);
		redirect.addParameter(entityIdCol, entityId);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}
}
