/**
 *
 */
package com.insta.hms.master.packages;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.adminmaster.packagemaster.PackageDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.CityMaster.CityMasterDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import com.insta.hms.master.PlanMaster.PlanMasterDAO;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * @author krishna
 *
 */
public class PackageApplicabilityAction extends DispatchAction {

	CenterDAO packCenterDAO = new CenterDAO();
	SponsorDAO packSponsorDAO = new SponsorDAO();
	CenterMasterDAO centerDAO = new CenterMasterDAO();
	PlanMasterDAO planMasterDAO = new PlanMasterDAO();
	PlanDAO packPlanDAO = new PlanDAO();

	public ActionForward getScreen(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {
		Integer packageId = Integer.parseInt(request.getParameter("packId"));
		request.setAttribute("pack_bean", PackageDAO.getPackageDetails(packageId));
		request.setAttribute("applicable_sponsors", packSponsorDAO.getSponsors(packageId));
		request.setAttribute("applicable_centers", packCenterDAO.getCenters(packageId));
		request.setAttribute("applicable_plans", packPlanDAO.getPlans(packageId));
				
		List centers = CenterMasterDAO.getCentersList();
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("cities_json", js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(
				new CityMasterDAO().listAll("city_name"))));
		request.setAttribute("centers_json", js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(centers)));
		return mapping.findForward("package_applicability");
	}
	
	public ActionForward getEligiblePlans(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {
		response.setContentType("text/plain");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

		JSONSerializer js = new JSONSerializer().exclude("class");
		Integer packageId = Integer.parseInt(request.getParameter("packId"));
		String tpaId = request.getParameter("tpaId");
		String tpaIdArray[] = tpaId.split(",");
        List eligiblePlansList = packPlanDAO.getEligiblePlans(packageId, tpaIdArray, null);
		js.serialize(eligiblePlansList == null ? null : ConversionUtils.listBeanToListMap(eligiblePlansList), response.getWriter());
		response.flushBuffer();
				return null;
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException  {

		int packageId = Integer.parseInt(request.getParameter("package_id"));
		String error = null;
		Connection con = null;
		try {
			txn : {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				String app_for_centers = request.getParameter("applicable_for_centers");
				if (app_for_centers != null) {
					if (app_for_centers.equals("all")) {
						BasicDynaBean bean = packCenterDAO.getBean();

						if (!packCenterDAO.delete(con, packageId)) {
							error = "Failed to delete package applicability of few centers..";
							break txn;
						}

						if (packCenterDAO.findByKey(con, "pack_id", packageId) == null) {
							bean.set("package_center_id", packCenterDAO.getNextSequence());
							bean.set("pack_id", packageId);
							bean.set("center_id", -1);
							bean.set("status", "A");
							if (!packCenterDAO.insert(con, bean)) {
								error = "Failed to update package applicability for all centers..";
								break txn;
							}
						}

					}
					else if ( app_for_centers.equals("none") ) {
						BasicDynaBean bean = packCenterDAO.getBean();

						if (!packCenterDAO.delete(con, packageId)) {
							error = "Failed to delete package applicability of few centers..";
							break txn;
						}

						if (packCenterDAO.findByKey(con, "pack_id", packageId) == null) {
							bean.set("package_center_id", packCenterDAO.getNextSequence());
							bean.set("pack_id", packageId);
							bean.set("center_id", 0);
							bean.set("status", "A");
							if (!packCenterDAO.insert(con, bean)) {
								error = "Failed to update package applicability for none centers..";
								break txn;
							}
						}

					} else {
						if (!packCenterDAO.delete(con, packageId, -1)) {
							error = "Failed to delete package applicability for all centers..";
							break txn;
						}
						String[] centerIds = request.getParameterValues("center_id");
						String[] pack_center_id = request.getParameterValues("package_center_id");
						String[] pack_center_delete = request.getParameterValues("cntr_delete");
						String[] pack_center_edited = request.getParameterValues("cntr_edited");
						String[] center_statuses = request.getParameterValues("center_status");
						for (int i=0; i<centerIds.length-1; i++) {
							BasicDynaBean bean = packCenterDAO.getBean();
							bean.set("pack_id", packageId);
							bean.set("center_id", Integer.parseInt(centerIds[i]));
							bean.set("status", center_statuses[i]);

							if (pack_center_id[i].equals("_")) {
								bean.set("package_center_id", packCenterDAO.getNextSequence());
								if (!packCenterDAO.insert(con, bean)) {
									error = "Failed to insert the package applicability for selected centers..";
									break txn;
								}
							} else if (new Boolean(pack_center_delete[i])) {
								if (!packCenterDAO.delete(con, "package_center_id", Integer.parseInt(pack_center_id[i]))) {
									error = "Failed to delete the package applicability for selected center..";
									break txn;
								}
							} else if (new Boolean(pack_center_edited[i])) {
								if (packCenterDAO.update(con, bean.getMap(), "package_center_id",
										Integer.parseInt(pack_center_id[i])) != 1) {
									error = "Failed to update the package applicability for selected center..";
									break txn;
								}
							}
						}
					}
				}

				String app_for_sponsors = request.getParameter("applicable_for_sponsors");
				if (app_for_sponsors.equals("all")) {
					BasicDynaBean bean = packSponsorDAO.getBean();
					if (!packSponsorDAO.delete(con, packageId)) {
						error = "Failed to delete package applicability of few sponsors..";
						break txn;
					}

					if (packSponsorDAO.findByKey(con, "pack_id", packageId) == null) {
						// insert only if (applicable to all sponsors) row is not found.
						bean.set("package_sponsor_id", packSponsorDAO.getNextSequence());
						bean.set("pack_id", packageId);
						bean.set("tpa_id", "-1");
						bean.set("status", "A");
						if (!packSponsorDAO.insert(con, bean)) {
							error = "Failed to update package applicability for all sponsors..";
							break txn;
						}
					}

				} else if (app_for_sponsors.equals("none")) {
					BasicDynaBean bean = packSponsorDAO.getBean();
					if (!packSponsorDAO.delete(con, packageId, "-1")) {
						error = "Failed to delete package applicability for all sponsors..";
						break txn;
					}
					if (!packSponsorDAO.delete(con, packageId, "0")) {
						error = "Failed to delete package applicability for all sponsors..";
						break txn;
					}

					if (!packSponsorDAO.delete(con, packageId)) {
						error = "Failed to delete package applicability of few sponsors..";
						break txn;
					}

					if (packSponsorDAO.findByKey(con, "pack_id", packageId) == null) {
						// insert only if (applicable to all sponsors) row is not found.
						bean.set("package_sponsor_id", packSponsorDAO.getNextSequence());
						bean.set("pack_id", packageId);
						bean.set("tpa_id", "0");
						bean.set("status", "A");
						if (!packSponsorDAO.insert(con, bean)) {
							error = "Failed to update package applicability for none sponsors..";
							break txn;
						}
					}

				}else {
					if (!packSponsorDAO.delete(con, packageId, "-1")) {
						error = "Failed to delete package applicability for all sponsors..";
						break txn;
					}
					if (!packSponsorDAO.delete(con, packageId, "0")) {
						error = "Failed to delete package applicability for all sponsors..";
						break txn;
					}
					String[] tpaIds = request.getParameterValues("tpa_id");
					String[] pack_sponsor_id = request.getParameterValues("package_sponsor_id");
					String[] pack_sponsor_delete = request.getParameterValues("tpa_delete");
					String[] pack_sponsor_edited = request.getParameterValues("tpa_edited");
					String[] tpa_statuses = request.getParameterValues("tpa_status");
					for (int i=0; i<tpaIds.length-1; i++) {
						BasicDynaBean bean = packSponsorDAO.getBean();
						bean.set("pack_id", packageId);
						bean.set("tpa_id", tpaIds[i]);
						bean.set("status", tpa_statuses[i]);

						if (pack_sponsor_id[i].equals("_")) {
							bean.set("package_sponsor_id", packSponsorDAO.getNextSequence());
							if (!packSponsorDAO.insert(con, bean)) {
								error = "Failed to insert the package applicability for selected sponsor(s)..";
								break txn;
							}
						} else if (new Boolean(pack_sponsor_delete[i])) {
							if (!packSponsorDAO.delete(con, "package_sponsor_id", Integer.parseInt(pack_sponsor_id[i]))) {
								error = "Failed to delete the package applicability for selected sponsor(s)..";
								break txn;
							}
						} else if (new Boolean(pack_sponsor_edited[i])) {
							if (packSponsorDAO.update(con, bean.getMap(), "package_sponsor_id",
									Integer.parseInt(pack_sponsor_id[i])) != 1) {
								error = "Failed to update the package applicability for selected sponsor(s)..";
								break txn;
							}
						}
					}
				}
				
				String app_for_plans = request.getParameter("applicable_for_plans");
				if (app_for_plans.equals("all") && !(app_for_sponsors.equals("none"))) {
					BasicDynaBean bean = packPlanDAO.getBean();
					if (!packPlanDAO.delete(con, packageId)) {
						error = "Failed to delete package applicability of few Plans..";
						break txn;
					}

					if (packPlanDAO.findByKey(con, "pack_id", packageId) == null) {
						// insert only if (applicable to all Plans) row is not found.
						bean.set("package_plan_id", packPlanDAO.getNextSequence());
						bean.set("pack_id", packageId);
						bean.set("plan_id", -1);
						bean.set("status", "A");
						if (!packPlanDAO.insert(con, bean)) {
							error = "Failed to update package applicability for all Plans..";
							break txn;
						}
					}

				} else if (app_for_plans.equals("none") || app_for_sponsors.equals("none")) {
					BasicDynaBean bean = packPlanDAO.getBean();
					if (!packPlanDAO.delete(con, packageId, -1)) {
						error = "Failed to delete package applicability for all Plans..";
						break txn;
					}
					if (!packPlanDAO.delete(con, packageId, 0)) {
						error = "Failed to delete package applicability for all Plans..";
						break txn;
					}

					if (!packPlanDAO.delete(con, packageId)) {
						error = "Failed to delete package applicability of few Plans..";
						break txn;
					}

					if (packPlanDAO.findByKey(con, "pack_id", packageId) == null) {
						// insert only if (applicable to all Plans) row is not found.
						bean.set("package_plan_id", packPlanDAO.getNextSequence());
						bean.set("pack_id", packageId);
						bean.set("plan_id", 0);
						bean.set("status", "A");
						if (!packPlanDAO.insert(con, bean)) {
							error = "Failed to update package applicability for none Plans..";
							break txn;
						}
					}

				}else {
					if (!packPlanDAO.delete(con, packageId, -1)) {
						error = "Failed to delete package applicability for all Plans..";
						break txn;
					}
					if (!packPlanDAO.delete(con, packageId, 0)) {
						error = "Failed to delete package applicability for all Plans..";
						break txn;
					}
					String[] planIds = request.getParameterValues("plan_id");
					String[] pack_plan_id = request.getParameterValues("package_plan_id");
					String[] pack_plan_delete = request.getParameterValues("plan_delete");
					String[] pack_plan_edited = request.getParameterValues("plan_edited");
					String[] plan_statuses = request.getParameterValues("plan_status");
					for (int i=0; i<planIds.length-1; i++) {
						BasicDynaBean bean = packPlanDAO.getBean();
						bean.set("pack_id", packageId);
						bean.set("plan_id", Integer.parseInt(planIds[i]));
						bean.set("status", plan_statuses[i]);

						if (pack_plan_id[i].equals("_")) {
							bean.set("package_plan_id", packPlanDAO.getNextSequence());
							if (!packPlanDAO.insert(con, bean)) {
								error = "Failed to insert the package applicability for selected plan(s)..";
								break txn;
							}
						} else if (new Boolean(pack_plan_delete[i])) {
							if (!packPlanDAO.delete(con, "package_plan_id", Integer.parseInt(pack_plan_id[i]))) {
								error = "Failed to delete the package applicability for selected plan(s)..";
								break txn;
							}
						} else if (new Boolean(pack_plan_edited[i])) {
							if (packPlanDAO.update(con, bean.getMap(), "package_plan_id",
									Integer.parseInt(pack_plan_id[i])) != 1) {
								error = "Failed to update the package applicability for selected plan(s)..";
								break txn;
							}
						}
					}
				}
			}
		} finally {
			DataBaseUtil.commitClose(con, error == null);
		}
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		FlashScope flash = FlashScope.getScope(request);

		flash.put("error", error);
		redirect.addParameter("packId", packageId);
		redirect.addParameter("multi_visit_package",request.getParameter("multi_visit_package"));
		redirect.addParameter("org_id", request.getParameter("org_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

}
