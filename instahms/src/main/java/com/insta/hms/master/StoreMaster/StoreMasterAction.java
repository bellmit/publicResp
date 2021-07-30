package com.insta.hms.master.StoreMaster;

import static com.insta.hms.master.StoreMaster.StoreMasterDAO.getAccountId;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.AbstractDataHandlerAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.csvutils.TableDataHandler;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.CounterMaster.CounterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PharmacyPrintTemplate.PharmacyPrintTemplateDAO;
import com.insta.hms.master.PrescriptionsLabelPrintTemplates.PrescriptionsLabelPrintTemplateDAO;
import com.insta.hms.master.PrescriptionsPrintTemplates.PrescriptionsTemplateDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.commons.beanutils.DynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class StoreMasterAction extends AbstractDataHandlerAction {

	StoreMasterDAO dao = new StoreMasterDAO();
	CounterMasterDAO counterMasterDao = new CounterMasterDAO();
	JSONSerializer js = new JSONSerializer();

	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		CenterMasterDAO centerDao = new CenterMasterDAO();
		int userCenterId = (Integer)req.getSession(false).getAttribute("centerId");
		Map requestParams = req.getParameterMap();
		Map listingParams = ConversionUtils.getListingParameter(requestParams);

		PagedList pagedList = dao.getStoreDetailPages(requestParams, listingParams, userCenterId);
		req.setAttribute("pagedList", pagedList);
		req.setAttribute("centers", centerDao.getAllCentersExceptSuper());
		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
		throws IOException, ServletException, Exception {

		List<String> columns = new ArrayList();
		columns.add("template_name");
		List<BasicDynaBean> templates = new PharmacyPrintTemplateDAO().listAll(columns);
		req.setAttribute("templates", templates);

		req.setAttribute("pharmacy_counters", counterMasterDao.getPharmacyActiveCounters());

		List<BasicDynaBean> presc_lbl_templates = new PrescriptionsLabelPrintTemplateDAO().listAll(columns);
		req.setAttribute("presc_lbl_templates", presc_lbl_templates);
		req.setAttribute("presc_templates", PrescriptionsTemplateDAO.getTemplateNames());
		req.setAttribute("centers", new CenterMasterDAO().listAll(null, "status", "A", "center_name"));
		req.setAttribute("max_centers_inc_default", GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default"));
		req.setAttribute("genPrefs", GenericPreferencesDAO.getPrefsBean().getMap());

		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Map params = req.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		BasicDynaBean bean = dao.getBean();
		bean.set("created_timestamp",DateUtil.getCurrentTimestamp());
		ConversionUtils.copyToDynaBean(params, bean, errors);
		if(bean != null && (bean.get("allowed_raise_bill")== null || bean.get("allowed_raise_bill").equals(""))) {
			bean.set("allowed_raise_bill", "N");
		}
		if(req.getParameter("batch_selling_price_id") != null) {
			bean.set("use_batch_mrp", "Y");
		} else {
			bean.set("use_batch_mrp", "N");
		}
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = null;

		if (errors.isEmpty()) {
			BasicDynaBean exists = dao.findByKey("dept_name", bean.get("dept_name"));
			/*
			 *  selected counter is already associated with another account group for another store.
			 */
			String accountGroup = dao.validateCounter( (String) bean.get("counter_id"), con,
					(Integer) bean.get("account_group"), (Integer) bean.get("dept_id"));
			if (accountGroup != null) {
				flash.put("error", "Counter is already associated with \"" +accountGroup + "\" Account Group." +
						" \nEither select the different counter or select the \""+ accountGroup + "\" Account Group." +
						" Create new counter if one(not associated) is not available");
				redirect = new ActionRedirect(m.findForward("addRedirect"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
			if (exists == null) {
				bean.set("dept_id", dao.getNextSequence());
				boolean success = dao.insert(con, bean);
				if (success) {
					con.commit();
					redirect = new ActionRedirect(m.findForward("showRedirect"));
					flash.success("Store master details inserted successfully..");
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					redirect.addParameter("dept_id", bean.get("dept_id"));
					DataBaseUtil.closeConnections(con, null);
					return redirect;
				} else {
					con.rollback();
					flash.error("Failed to add  Store..");
				}
			} else {
				flash.error("Store name already exists..");
			}
		} else {
			flash.error("Incorrectly formatted values supplied");
		}
		redirect = new ActionRedirect(m.findForward("addRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		StoreMasterDAO dao = new StoreMasterDAO();
		BasicDynaBean bean = dao.getStoreDetails(Integer.parseInt(req.getParameter("dept_id")));

		req.setAttribute("bean", bean);
		req.setAttribute("storesLists", js.serialize(dao.getStoresNamesAndIds()));

		List<String> columns = new ArrayList();
		columns.add("template_name");
		List<BasicDynaBean> templates = new PharmacyPrintTemplateDAO().listAll(columns);
		req.setAttribute("templates", templates);


		List<BasicDynaBean> presc_lbl_templates = new PrescriptionsLabelPrintTemplateDAO().listAll(columns);
		req.setAttribute("presc_lbl_templates", presc_lbl_templates);
		req.setAttribute("presc_templates", PrescriptionsTemplateDAO.getTemplateNames());

		req.setAttribute("centers", new CenterMasterDAO().listAll(null, "status", "A", "center_name"));
		req.setAttribute("max_centers_inc_default", GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default"));
		req.setAttribute("pharmacy_counters", counterMasterDao.getPharmacyActiveCounters());
		req.setAttribute("genPrefs", GenericPreferencesDAO.getPrefsBean().getMap());
		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = null;
		Map params = req.getParameterMap();
		List errors = new ArrayList();

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			BasicDynaBean bean = dao.getBean();
			bean.set("updated_timestamp",DateUtil.getCurrentTimestamp());
			ConversionUtils.copyToDynaBean(params, bean, errors);
			if(bean != null && (bean.get("allowed_raise_bill")== null || bean.get("allowed_raise_bill").equals(""))) {
				bean.set("allowed_raise_bill", "N");
			}
			if(req.getParameter("batch_selling_price_id") != null) {
				bean.set("use_batch_mrp", "Y");
			} else {
				bean.set("use_batch_mrp", "N");
			}
			Integer key = Integer.parseInt(req.getParameter("dept_id"));
			Map<String, Integer> keys = new HashMap<String, Integer>();
			keys.put("dept_id", key);
			FlashScope flash = FlashScope.getScope(req);

			ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("dept_id", bean.get("dept_id"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

			if (errors.isEmpty()) {
				/*
				 *  selected counter is already associated with another account group for another store.
				 */
				String accountGroup = dao.validateCounter( (String) bean.get("counter_id"), con,
						(Integer) bean.get("account_group"), (Integer) bean.get("dept_id"));
				if (accountGroup != null) {
					flash.put("error", "Counter is already associated with \"" +accountGroup + "\" Account Group." +
							" \nEither select the different counter or select the \""+ accountGroup + "\" Account Group." +
							" Create new counter if one(not associated) is not available.");
					return redirect;
				}
				DynaBean exists = dao.findByKey("dept_name", bean.get("dept_name"));
				if (exists != null && !key.equals(exists.get("dept_id"))) {
					flash.error("Store name already exists..");
				}
				else {

					if (bean.get("status") != null && ((String)bean.get("status")).equals("I")) {
						List<BasicDynaBean> serviceStoreDependants = getServiceStoreDependants(key);
						if (serviceStoreDependants != null && serviceStoreDependants.size() > 0) {
							flash.error("Cannot mark the store as inactive. <br/>" +
									" One (or) more Service Department stores are linked with this store.");
							return redirect;
						}

						List<BasicDynaBean> diagStoreDependants = getDiagStoreDependants(key);
						if (diagStoreDependants != null && diagStoreDependants.size() > 0) {
							flash.error("Cannot mark the store as inactive. <br/>" +
									" One (or) more Diagnostic Department stores are linked with this store.");
							return redirect;
						}

						List<BasicDynaBean> userDefaultStoreDependants = getUserDefaultStoreDependants(req.getParameter("dept_id"));
						if (userDefaultStoreDependants != null && userDefaultStoreDependants.size() > 0) {
							flash.error("Cannot mark the store as inactive. <br/>" +
									" One (or) more User default store is linked with this store.");
							return redirect;
						}

						List<BasicDynaBean> userMultiStoreDependants = getUserMultiStoreDependants(req.getParameter("dept_id"));
						if (userMultiStoreDependants != null && userMultiStoreDependants.size() > 0) {
							flash.error("Cannot mark the store as inactive. <br/>" +
									" One (or) more User multi stores are linked with this store.");
							return redirect;
						}
					}

					int success = dao.update(con, bean.getMap(), keys);

					if (success > 0) {
						con.commit();
						flash.success("Store master details updated successfully..");
					} else {
						con.rollback();
						flash.error("Failed to update Store master details..");
					}
				}
			}
			else {
				flash.error("Incorrectly formatted values supplied");
			}

			return redirect;

		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public static final String GET_SERVICE_STORE_DEPENDANTS =
					" SELECT * FROM services_departments WHERE store_id = ? AND status = 'A' ";

	private List<BasicDynaBean> getServiceStoreDependants(int store_id) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_SERVICE_STORE_DEPENDANTS, new Object[]{store_id});
	}

	public static final String GET_DIAG_STORE_DEPENDANTS =
		" SELECT * FROM diagnostics_departments d " +
		" JOIN diagnostic_department_stores ds ON (d.ddept_id = ds.ddept_id) " +
		" WHERE ds.store_id = ?  AND d.status = 'A' ";

	private List<BasicDynaBean> getDiagStoreDependants(int store_id) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_DIAG_STORE_DEPENDANTS, new Object[]{store_id});
	}

	public static final String GET_USER_DEFAULT_STORE_DEPENDANTS =
		" SELECT * FROM u_user WHERE pharmacy_store_id = ? ";

	private List<BasicDynaBean> getUserDefaultStoreDependants(String store_id) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_USER_DEFAULT_STORE_DEPENDANTS, new Object[]{store_id});
	}

	public static final String GET_USER_MULTI_STORE_DEPENDANTS =
		"SELECT multi_store from u_user where multi_store IS NOT NULL AND multi_store != ''"
		+ "AND ? = any(string_to_array(replace(multi_store, ' ', ''), ','));";

	private List<BasicDynaBean> getUserMultiStoreDependants(String store_id) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_USER_MULTI_STORE_DEPENDANTS, new Object[]{store_id});
	}

	public ActionForward getAccountGrpId(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)throws ServletException, IOException, SQLException {
		String counterId = request.getParameter("counterId");
		Integer accountObj = getAccountId(counterId);
		response.setContentType("text/plain");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		response.getWriter().write(accountObj.toString());
		return null;
	}

	private static TableDataHandler masterHandler = null;

	@Override
	protected TableDataHandler getDataHandler() {
		if (masterHandler == null) {
			masterHandler = new TableDataHandler(
					"stores",		// table name
					new String[]{"dept_id"},	// keys
					new String[]{"dept_name", "counter_id", "status",
						"pharmacy_tin_no", "pharmacy_drug_license_no", "account_group",
						"store_type_id", "is_super_store", "is_sterile_store", "sale_unit", "center_id",
						"allowed_raise_bill", "is_sales_store", "auto_fill_indents",
						"auto_fill_prescriptions", "purchases_store_vat_account_prefix",
						"purchases_store_cst_account_prefix", "sales_store_vat_account_prefix",
						"store_rate_plan_id", "use_batch_mrp","auto_po_generation_frequency_in_days","allow_auto_po_generation",
						"auto_cancel_po_frequency_in_days","allow_auto_cancel_po"
					},
					new String[][]{
						// our field        ref table        ref table id field  ref table name field
						{"counter_id", "counters", "counter_id", "counter_no"},
						{"account_group", "account_group_master", "account_group_id", "account_group_name"},
						{"store_type_id", "store_type_master", "store_type_id", "store_type_name"},
						{"center_id", "hospital_center_master", "center_id", "center_name"},
						{"store_rate_plan_id", "store_rate_plans", "store_rate_plan_id", "store_rate_plan_name"},
					},
					null
			);
		}

		masterHandler.setSequenceName("stores_seq");
		masterHandler.setAlias("store_rate_plan_id", "store_tariff_name");
		return masterHandler;
	}

}
