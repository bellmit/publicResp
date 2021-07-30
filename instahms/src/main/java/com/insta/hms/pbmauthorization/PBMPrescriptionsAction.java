/**
 *
 */
package com.insta.hms.pbmauthorization;

import com.bob.hms.common.CenterHelper;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.Accounting.AccountingGroupMasterDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.InsuranceCategoryMaster.InsuranceCategoryMasterDAO;
import com.insta.hms.master.InsuranceCompMaster.InsuCompMasterDAO;
import com.insta.hms.master.PBMObservations.PBMObservationsMasterDAO;
import com.insta.hms.master.PlanMaster.PlanMasterDAO;
import com.insta.hms.master.PrescriptionsMaster.PrescriptionsMasterDAO;
import com.insta.hms.master.RecurrenceDailyMaster.RecurrenceDailyMasterDAO;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.stores.PharmacymasterDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author lakshmi
 *
 */
public class PBMPrescriptionsAction extends BaseAction {

	static Logger log = LoggerFactory.getLogger(PBMPrescriptionsAction.class);

	private static PBMPrescriptionsDAO pbmdao;
	private static GenericDAO pbmReqDAO;
	private static GenericDAO pbmPrescReqDAO;
	private static PrescriptionsMasterDAO mmDao;
	private static GenericDAO medDosageDao;
	private static GenericDAO pbmMedPrescDAO;
	private static PharmacymasterDAO storeItemDAO;
	private static GenericDAO genericNameDAO;
	private static MRDDiagnosisDAO mrdDao;
	private static GenericDAO planDAO;

	public PBMPrescriptionsAction() {
		pbmdao = new PBMPrescriptionsDAO();
		pbmReqDAO = new GenericDAO("pbm_request_approval_details");
		pbmPrescReqDAO = new GenericDAO("pbm_prescription_request");
		mmDao = new PrescriptionsMasterDAO();
		medDosageDao = new GenericDAO("medicine_dosage_master");
		pbmMedPrescDAO = new GenericDAO("pbm_medicine_prescriptions");
		storeItemDAO = new PharmacymasterDAO();
		genericNameDAO = new GenericDAO("generic_name");
		mrdDao = new MRDDiagnosisDAO();
		planDAO = new GenericDAO("insurance_plan_main");
	}
	
    private static final GenericDAO storesDAO = new GenericDAO("stores");

	@SuppressWarnings("unchecked")
	@IgnoreConfidentialFilters
	public  ActionForward getList(ActionMapping mapping,ActionForm fm,
			HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception {

		Integer userCenterId = RequestContext.getCenterId();
		String errorMsg = CenterHelper.authenticateCenterUser(userCenterId);
		if(errorMsg != null) {
			request.setAttribute("error", errorMsg);
			return mapping.findForward("list");
		}

		Map<Object,Object> map= getParameterMap(request);
		JSONSerializer js = new JSONSerializer().exclude("class");
		PagedList list = PBMPrescriptionsDAO.searchPBMPrescriptionList(map, ConversionUtils.getListingParameter(map));
		request.setAttribute("pagedList", list);

		HttpSession session = request.getSession(false);
    	String dept_id = (String) session.getAttribute("pharmacyStoreId");

    	if (dept_id != null && !dept_id.equals("")) {
    		BasicDynaBean dept = storesDAO.findByKey("dept_id", Integer.parseInt(dept_id));
    		if (dept.get("store_rate_plan_id") != null) {
	    		String dept_name = dept.get("dept_name").toString();
	    		request.setAttribute("dept_id", dept_id);
	    		request.setAttribute("dept_name", dept_name);
    		}
		}

    	if (dept_id != null  &&  dept_id.equals("")) {
			request.setAttribute("dept_id", dept_id);
        }

    	List<String> columns = new ArrayList<>();
    	columns.add("insurance_co_id");
    	columns.add("insurance_co_name");
		request.setAttribute("insCompList", js.serialize(ConversionUtils.listBeanToListMap(
				new InsuCompMasterDAO().listAll(columns, "status", "A", "insurance_co_name"))));

		request.setAttribute("insCategoryList", js.serialize(ConversionUtils.listBeanToListMap(
				new GenericDAO("insurance_category_master").listAll(null, "status", "A", "category_name"))));

		columns.clear();
		columns.add("tpa_name");
		columns.add("tpa_id");
		request.setAttribute("tpaList",js.serialize(ConversionUtils.listBeanToListMap(
				new GenericDAO("tpa_master").listAll(columns, "status", "A", "tpa_name"))));

		columns.clear();
		columns.add("plan_id");
		columns.add("plan_name");
		columns.add("category_id");
		request.setAttribute("planList",js.serialize(ConversionUtils.listBeanToListMap(
				new GenericDAO("insurance_plan_main").listAll(columns, "status", "A", "plan_name"))));
		request.setAttribute("inscatName", InsuranceCategoryMasterDAO.getInsCatCenter(RequestContext.getCenterId()));
		return mapping.findForward("list");
	}

	public ActionForward getPBMPrescRateDetails(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		String store = request.getParameter("storeId");
		Integer storeId = (store != null && !store.equals("")) ? Integer.parseInt(store) : null;
		String pbmPresc = request.getParameter("pbmPrescId");
		int pbmPrescId = Integer.parseInt(pbmPresc);

		BasicDynaBean pbmbean = pbmdao.getPBMPresc(pbmPrescId);
		String visitId = (String)pbmbean.get("patient_id");
		String visitType = (String)pbmbean.get("visit_type");
		String orgId = (String)pbmbean.get("org_id");
		int planId = (Integer)pbmbean.get("plan_id");
		List<BasicDynaBean> pbmprescRates = new ArrayList<BasicDynaBean>();
		if (storeId != null) {
			Connection con = DataBaseUtil.getReadOnlyConnection();
			try {
				pbmprescRates = pbmdao.getPBMPrescRates(con, pbmPrescId, visitId, visitType, orgId, storeId, planId);
			} finally {
				con.close();
			}
		}

		JSONSerializer js = new JSONSerializer().exclude("class");
		response.setContentType("text/javascript");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

		response.getWriter().write(js.serialize(ConversionUtils.copyListDynaBeansToMap(pbmprescRates)));
		return null;
	}

	public ActionForward ajaxPBMPrescRate(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		String store = request.getParameter("store_id");
		Integer storeId = (store != null && !store.equals("")) ? Integer.parseInt(store) : null;
		String visitId = request.getParameter("patient_id");
		String visitType = request.getParameter("visit_type");
		String orgId = request.getParameter("org_id");
		String planIdStr = request.getParameter("plan_id");
		int planId = Integer.parseInt(planIdStr);
		String medicineIdStr = request.getParameter("medicine_id");
		int medicineId = Integer.parseInt(medicineIdStr);
		String qtyStr = request.getParameter("medicine_quantity");
		BigDecimal medQty = new BigDecimal(qtyStr);
		String itemUOM = request.getParameter("item_uom");
		String itemPrescIdStr = request.getParameter("item_prescribed_id");
		if("_".equals(itemPrescIdStr)){
			itemPrescIdStr="0";
		}
		int itemPrescId = Integer.parseInt(itemPrescIdStr);

		BasicDynaBean pbmPrescActBean =  pbmMedPrescDAO.findByKey("pbm_medicine_pres_id", itemPrescId);
		String status = pbmPrescActBean != null ? (String)pbmPrescActBean.get("pbm_status") : "O";
		boolean calcRate = (!status.equals("C"));

		BasicDynaBean pbmprescRate = null;
		if (storeId != null) {
			pbmprescRate = pbmdao.getPBMPrescItemRateBean(medicineId, medQty, itemUOM,
					visitId, visitType, orgId, storeId, planId, calcRate);

			// When a new item is added, store sale unit is defaulted as UOM.
			if (itemUOM == null || itemUOM.trim().equals("")) {
				pbmprescRate.set("user_unit", pbmprescRate.get("store_sale_unit"));
			}
		}

		JSONSerializer js = new JSONSerializer().exclude("class");
		response.setContentType("text/javascript");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

		response.getWriter().write(js.serialize(pbmprescRate != null ? pbmprescRate.getMap() : null));
		return null;
	}

	@IgnoreConfidentialFilters
	public ActionForward reopenPBMPresc(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws SQLException, Exception {

		String pbmPrescStr = request.getParameter("pbm_presc_id");
		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		int pbmPrescId = (pbmPrescStr != null && !pbmPrescStr.equals("")) ? Integer.parseInt(pbmPrescStr) : 0;

		boolean success = true;
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			BasicDynaBean pbmBean = pbmdao.getBean();
			pbmBean.set("pbm_presc_id", pbmPrescId);
			pbmBean.set("pbm_finalized", "N");
			int n = pbmdao.updateWithName(con, pbmBean.getMap(), "pbm_presc_id");
			success = (n > 0);

		} finally {
			DataBaseUtil.commitClose(con, success);
		}

		redirect.addParameter("pbm_presc_id", pbmPrescId);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward savePBMDetails(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws SQLException, Exception {

		HttpSession session = request.getSession(false);
		String userid = (String)session.getAttribute("userid");
		Connection con = null;

		String pbmPrescStr = request.getParameter("pbm_presc_id");
		String consIdStr = request.getParameter("consultation_id");
		String patientId = request.getParameter("patient_id");

		String comments = request.getParameter("_comments");
		String resubmission_type = request.getParameter("_resubmit_type");

		BasicDynaBean visitInsDet = VisitDetailsDAO.getVisitDetails(patientId);
		String visitType = (String)visitInsDet.get("visit_type");
		String orgId = (String)visitInsDet.get("org_id");
		int planId = (Integer)visitInsDet.get("plan_id");
		Integer centerId = (Integer)visitInsDet.get("center_id");

		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect("/PBMAuthorization/PBMPresc.do");
		BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
		String use_store_items = (String) genericPrefs.get("prescription_uses_stores");
		String prescByGenerics = (String) HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(
				CenterMasterDAO.getHealthAuthorityForCenter(centerId)).getPrescriptions_by_generics();
		Boolean prescriptionsByGenerics = use_store_items.equals("Y") && prescByGenerics.equals("Y");

		boolean mod_eclaim_pbm = (Boolean)session.getAttribute("mod_eclaim_pbm");
		boolean mod_eclaim_erx = (Boolean)session.getAttribute("mod_eclaim_erx");

		int consId = (consIdStr != null && !consIdStr.equals("")) ? Integer.parseInt(consIdStr) : 0;
		int pbmPrescId = (pbmPrescStr != null && !pbmPrescStr.equals("")) ? Integer.parseInt(pbmPrescStr) : 0;
		boolean priorAuthRequired = false;
		int drugCount = 0;

		boolean allSuccess = false;

		/* Prior Auth is required only when mod_eclaim_pbm module is enabled (mod_eclaim_erx is disabled)
		 * and visit type is 'o' (or)
		 * mod_eclaim_erx module is enabled and visit type is 'o'
		 */
		if (!mod_eclaim_erx && mod_eclaim_pbm && visitType.equals("o")) {
			BasicDynaBean planBean = planDAO.findByKey("plan_id", planId);
			if (planBean != null && ((String)planBean.get("require_pbm_authorization")).equals("Y")) {
				priorAuthRequired = true;
			}
		}else if (mod_eclaim_erx && visitType.equals("o") && planId != 0) {
			priorAuthRequired = true;
		}

		String store = request.getParameter("_phStore");
		Integer storeId = (store != null && !store.equals("")) ? Integer.parseInt(store) : null;

		try {
		  txn:{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			String[] prescribedIds = request.getParameterValues("s_item_prescribed_id");
			String[] prescribedDates = request.getParameterValues("s_prescribed_date");
			String[] itemNames = request.getParameterValues("s_item_name");
			String[] itemIds = request.getParameterValues("s_item_id");
			String[] medFrequencies = request.getParameterValues("s_frequency");
			String[] durations = request.getParameterValues("s_duration");
			String[] duration_units = request.getParameterValues("s_duration_units");
			String[] medQty = request.getParameterValues("s_medicine_quantity");
			String[] item_remarks = request.getParameterValues("s_remarks");
			String[] itemMaster = request.getParameterValues("s_item_master");
			String[] ispackage = request.getParameterValues("s_ispackage");
			String[] addActivities = request.getParameterValues("s_addActivity");
			String[] routeOfAdmin = request.getParameterValues("s_route_id");
			// strength is the dosage to the patient how much to take (1 tab or 1/2 tab or 5ml or 10ml)
			String[] medicine_strengths = request.getParameterValues("s_strength");
			String[] generic_code = request.getParameterValues("s_generic_code");
			String[] item_form_ids = request.getParameterValues("s_item_form_id");
			// item_strength is the medicine strength (100mg, 200mg, 500ml etc.,)
			String[] item_strengths = request.getParameterValues("s_item_strength");
			String[] item_strength_units = request.getParameterValues("s_item_strength_units");

			String[] rate = request.getParameterValues("s_item_rate");
			String[] qty = request.getParameterValues("s_item_qty");
			String[] discount = request.getParameterValues("s_item_disc");
			String[] amount = request.getParameterValues("s_item_amount");
			String[] claimNetAmount = request.getParameterValues("s_claim_net_amount");
			String[] itemPkgUnit = request.getParameterValues("s_item_package_unit");
			String[] itemUserUnit = request.getParameterValues("s_item_user_unit");

			String[] itemType = request.getParameterValues("s_itemType");
			String[] delItems = request.getParameterValues("s_delItem");
			String[] editItems = request.getParameterValues("s_edited");

			if (prescribedIds != null) {
				for (int i=0; i< prescribedIds.length-1; i++) {
					boolean deleteItem = new Boolean(delItems[i]);
					String prescribedId = prescribedIds[i];
					int itemPrescriptionId = 0;
					BasicDynaBean itemBean = pbmMedPrescDAO.getBean();

					itemBean.set("rate", new BigDecimal(rate[i]));
					itemBean.set("discount", new BigDecimal(discount[i]));
					itemBean.set("amount", new BigDecimal(amount[i]));
					itemBean.set("claim_net_amount", new BigDecimal(claimNetAmount[i]));
					itemBean.set("package_size", new BigDecimal(itemPkgUnit[i]));
					itemBean.set("user_unit", itemUserUnit[i]);

					itemBean.set("prescribed_date", DateUtil.parseTimestamp(prescribedDates[i]+" "+DateUtil.getCurrentTime()));

					String duration = durations[i];
					String medicineQuantity = medQty[i];
					if (!duration.equals("")) {
						itemBean.set("duration", Integer.parseInt(duration));
						itemBean.set("duration_units", duration_units[i]);
					} else {
						itemBean.set("duration", null);
						itemBean.set("duration_units", null);
					}
					if (!medicineQuantity.equals("")) {
						itemBean.set("medicine_quantity", new BigDecimal(medicineQuantity));
					} else {
						itemBean.set("medicine_quantity", null);
					}
					if (use_store_items.equals("Y")) {
						if (!prescriptionsByGenerics)
							itemBean.set("medicine_id", Integer.parseInt(itemIds[i]));

						if (itemIds[i] != null && !itemIds[i].equals(""))
							itemBean.set("medicine_id", Integer.parseInt(itemIds[i]));

						//	update the generic_code always when pharmacy module is enabled
						itemBean.set("generic_code", generic_code[i]);
					}
					itemBean.set("frequency", medFrequencies[i]);
					itemBean.set("medicine_remarks", item_remarks[i]);
					itemBean.set("consultation_id", consId);
					itemBean.set("strength", medicine_strengths[i]);
					itemBean.set("item_strength", item_strengths[i]);
					if (!item_strength_units[i].equals(""))
						itemBean.set("item_strength_units", Integer.parseInt(item_strength_units[i]));
					else
						itemBean.set("item_strength_units", null);
					if (!item_form_ids[i].equals(""))
						itemBean.set("item_form_id", Integer.parseInt(item_form_ids[i]));
					else
						itemBean.set("item_form_id", null);

					if (!routeOfAdmin[i].equals("")) {
						itemBean.set("route_of_admin", Integer.parseInt(routeOfAdmin[i]));
					} else{
						itemBean.set("route_of_admin", null);
					}

					itemBean.set("username", userid);
					itemBean.set("visit_id", patientId);
					// if this is not set, the edited values will be overriden by the trigger function on pbm_presctiption.
					itemBean.set("updated_in_pbm", "Y");

					if (prescribedId.equals("_")) {

						// New item pbm status is Open.
						itemBean.set("pbm_status", "O");

						String frequency = medFrequencies[i].trim();
						BigDecimal perdayqty =  null;
						if (!duration.equals("") && !medicineQuantity.equals("")) {
							BigDecimal Qty = new BigDecimal(medQty[i], new MathContext(2));
							BigDecimal days = null;
							if (duration_units[i].equals("D"))
								days = new BigDecimal(Integer.parseInt(duration), new MathContext(2));
							else if (duration_units[i].equals("W"))
								days = new BigDecimal(Integer.parseInt(duration)*7, new MathContext(2));
							else if (duration_units[i].equals("M"))
								days = new BigDecimal(Integer.parseInt(duration)*30, new MathContext(2));

							perdayqty = Qty.divide(days, 2, BigDecimal.ROUND_HALF_UP);
						}

						if (!use_store_items.equals("Y") && !PrescriptionsMasterDAO.medicineExisits(itemNames[i])) {
							BasicDynaBean presMedMasterBean = mmDao.getBean();
							presMedMasterBean.set("medicine_name", itemNames[i]);
							presMedMasterBean.set("status", "A");

							if (!mmDao.insert(con, presMedMasterBean))
								break txn;
						}

						itemPrescriptionId = pbmMedPrescDAO.getNextSequence();
						itemBean.set("pbm_medicine_pres_id", itemPrescriptionId);

						// Insert item into pbm_medicine_prescriptions
						if (!pbmMedPrescDAO.insert(con, itemBean))
							break txn;

						BasicDynaBean dbDosageBean = medDosageDao.findByKey(con, "dosage_name", frequency);

						if (dbDosageBean == null) {
							BasicDynaBean dosagebean = medDosageDao.getBean();
							dosagebean.set("dosage_name", frequency);
							dosagebean.set("per_day_qty", perdayqty);
							if (!medDosageDao.insert(con, dosagebean))
								break txn;
						}

					} else {
						itemPrescriptionId = Integer.parseInt(prescribedIds[i]);
						if (deleteItem) {
							if (!pbmMedPrescDAO.delete(con, "pbm_medicine_pres_id", itemPrescriptionId))
								break txn;
						} else {
							Map<String, Object> keys = new HashMap<String, Object>();
							keys.put("pbm_medicine_pres_id", itemPrescriptionId);
							itemBean.set("mod_time", new Timestamp(new java.util.Date().getTime()));
							if (pbmMedPrescDAO.update(con, itemBean.getMap(), keys) <=0 )
								break txn;
						}
					}

					// Update pbm_presc_id and medicine id in pbm_medicine_prescriptions
					if (!deleteItem && priorAuthRequired) {
						drugCount++;
						BasicDynaBean opMedPrescBean =
							pbmMedPrescDAO.findByKey(con, "pbm_medicine_pres_id", itemPrescriptionId);

						// Update medicine_id.
						if (opMedPrescBean.get("medicine_id") == null) {
							String genericCode = (String)opMedPrescBean.get("generic_code");
							BasicDynaBean genNameBean = genericNameDAO.findByKey("generic_code", genericCode);
							if (genNameBean != null) {
								String genericName = (String)genNameBean.get("generic_name");
								BasicDynaBean storeItemBean = storeItemDAO.findByKey("generic_name", genericName);
								if (storeItemBean != null) {
									opMedPrescBean.set("medicine_id", (Integer)storeItemBean.get("medicine_id"));
								}
							}
						}

						// Update pbm_presc_id.
						if (opMedPrescBean.get("pbm_presc_id") == null) {
							if (pbmPrescId == 0) {
								pbmPrescId = pbmdao.getNextSequence();
								BasicDynaBean pbmBean = pbmdao.getBean();
								pbmBean.set("pbm_presc_id", pbmPrescId);
								pbmBean.set("erx_consultation_id", consId);
								pbmBean.set("pbm_finalized", "N");
								pbmBean.set("status", "O");

								if (!pbmdao.insert(con, pbmBean)){
									break txn;
								}
							}
						}
						opMedPrescBean.set("pbm_presc_id", pbmPrescId);
						int k = pbmMedPrescDAO.updateWithName(con, opMedPrescBean.getMap(), "pbm_medicine_pres_id");
						if (k <= 0)
							break txn;
					}
				}

				// Update drug count in PBM Prescription.
				if (pbmPrescId != 0) {
					BasicDynaBean pbmBean = pbmdao.getBean();
					pbmBean.set("pbm_presc_id", pbmPrescId);
					pbmBean.set("drug_count", drugCount);

					if (resubmission_type != null) {

						int requestCount = DataBaseUtil.getIntValueFromDb(
								"SELECT count(*) FROM pbm_prescription_request WHERE pbm_presc_id = "+pbmPrescId);

						pbmBean.set("resubmit_type", resubmission_type);
			    		pbmBean.set("comments", comments);
						if (resubmission_type.equals("correction")) {
			    			pbmBean.set("resubmit_request_id_with_correction", ""+requestCount);
			    		}else {
			    			pbmBean.set("resubmit_request_id_with_correction", null);
			    		}
					}

					int n = pbmdao.updateWithName(con, pbmBean.getMap(), "pbm_presc_id");
					if (n <= 0)
						break txn;
				}
			}

			List<String> columns = new ArrayList<String>();
			columns.add("pbm_presc_id");
			columns.add("pbm_store_id");
			columns.add("pbm_finalized");
			columns.add("status");

			Map<String, Object> key = new HashMap<String, Object>();
			key.put("pbm_presc_id", pbmPrescId);
			BasicDynaBean pbmbean = pbmdao.findByKey(con, columns, key);

			Integer pbmStoreId = pbmbean != null && pbmbean.get("pbm_store_id") != null ? (Integer)pbmbean.get("pbm_store_id") : null;
			if ((pbmStoreId == null && storeId != null) ||
					(pbmStoreId != null && storeId != null && pbmStoreId.intValue() != storeId.intValue())) {

				boolean rateUpdate = pbmdao.savePBMPrescStore(con, pbmPrescId,
						patientId, visitType, orgId, planId, storeId, userid);
				if (!rateUpdate)
					break txn;
			}

			allSuccess = true;

		  }// txn
		} finally {
			DataBaseUtil.commitClose(con, allSuccess);
		}

		if (!allSuccess)
			flash.put("error", "Transaction failed");

		String method = "getPBMPrescription";
		if (allSuccess) {
			String finalize = request.getParameter("finalizeChk");
			if (finalize != null && finalize.equals("finalizePBMPresc")) {
				method = "finalizePBMPresc";
			}
			String markResubmit = request.getParameter("markResubmit");
			if (markResubmit != null && markResubmit.equals("markForResubmission")) {
				method = "markForResubmission";
				redirect.addParameter("_comments", comments);
				redirect.addParameter("_resubmit_type", resubmission_type);
			}
		}
		redirect.addParameter("_method", method);
		redirect.addParameter("pbm_presc_id", pbmPrescId);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}

	public ActionForward savePBMPrescriptionStore(ActionMapping mapping, ActionForm fm,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {

		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		String userid = (String)request.getSession(false).getAttribute("userid");

		String[] pbmPrescArr = request.getParameterValues("_pbm_presc_id");
		List<String> pbmPrescList = Arrays.asList(pbmPrescArr);

		String store = request.getParameter("_phStore");
		Integer storeId = (store != null && !store.equals("")) ? Integer.parseInt(store) : null;

		Connection con = null;
		boolean success = true;

		try {
			 con = DataBaseUtil.getConnection();
			 con.setAutoCommit(false);

			// Update store and store rates for the prescription activities
			// there is no store existing.
			for (String pbmPresc : pbmPrescList) {

				int pbmPrescId = Integer.parseInt(pbmPresc);

				BasicDynaBean pbmbean = pbmdao.getPBMPresc(pbmPrescId);

				String visitId = (String)pbmbean.get("patient_id");
				String visitType = (String)pbmbean.get("visit_type");
				String orgId = (String)pbmbean.get("org_id");
				int planId = (Integer)pbmbean.get("plan_id");

				Integer pbmStoreId = pbmbean.get("pbm_store_id") != null ? (Integer)pbmbean.get("pbm_store_id") : null;
				if (pbmStoreId != null)
					continue;

				success = pbmdao.savePBMPrescStore(con, pbmPrescId, visitId, visitType, orgId, planId, storeId, userid);
			}
		}finally {
			DataBaseUtil.commitClose(con, success);

			if (success) {
				flash.put("info", "PBM Prescriptions store and rates saved successfully.");
			}else {
				flash.put("error", "Error while saving PBM Prescriptions.");
			}
		}

		redirect.addParameter("pbm_finalized", "N");
		return redirect;
	}


	public ActionForward finalizePrescriptions(ActionMapping mapping, ActionForm fm,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {

		StringBuilder errStr = new StringBuilder("Error(s) while finalizing PBM Prescriptions. <br/>" +
								"Please correct (or) update the following and finalize again.<br/>");

		String[] pbmPrescArr = request.getParameterValues("_pbm_presc_id");
		List<String> pbmPrescList = Arrays.asList(pbmPrescArr);

		HttpSession session = request.getSession(false);
		String activeMode = (String)session.getAttribute("shafafiya_pbm_active");

		String path = request.getContextPath();
		Map<String, StringBuilder> errorsMap = new HashMap<String, StringBuilder>();
		List<PBMRequest> pbmRequestList = new ArrayList<PBMRequest>();
		pbmdao.validatePBMPrescriptions(errorsMap, path, activeMode, pbmPrescList, pbmRequestList);

		if (errorsMap != null && !errorsMap.isEmpty()) {
			Iterator keys = errorsMap.keySet().iterator();

			while (keys.hasNext()) {
				String key = (String)keys.next();
				StringBuilder errorString = (StringBuilder)errorsMap.get(key);
				errStr.append("<br/>"+errorString);
			}

			request.setAttribute("error", errStr.toString());
			request.setAttribute("referer", request.getHeader("Referer").
					replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
			return mapping.findForward("listReportErrors");

		}else {

			FlashScope flash = FlashScope.getScope(request);
	        ActionRedirect redirect = new ActionRedirect(request.getHeader("Referer").
					replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
	        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

	        String userid = (String)request.getSession().getAttribute("userid");
			boolean success = pbmdao.finalizePrescriptions(pbmPrescList, userid);
			if (!success)
				flash.error("Prescription(s) finalize unsuccessful.");

			return redirect;
		}
	}

	public ActionForward finalizePBMPresc(ActionMapping mapping, ActionForm fm,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {

		StringBuilder errStr = new StringBuilder("Error(s) while finalizing PBM Prescription. <br/>" +
								"Please correct (or) update the following and finalize again.<br/>");

		String[] pbmPrescArr = request.getParameterValues("pbm_presc_id");
		List<String> pbmPrescList = Arrays.asList(pbmPrescArr);

		HttpSession session = request.getSession(false);
		String activeMode = (String)session.getAttribute("shafafiya_pbm_active");

		String path = request.getContextPath();
		Map<String, StringBuilder> errorsMap = new HashMap<String, StringBuilder>();
		List<PBMRequest> pbmRequestList = new ArrayList<PBMRequest>();
		pbmdao.validatePBMPrescriptions(errorsMap, path, activeMode, pbmPrescList, pbmRequestList);

		if (errorsMap != null && !errorsMap.isEmpty()) {
			Iterator keys = errorsMap.keySet().iterator();

			while (keys.hasNext()) {
				String key = (String)keys.next();
				StringBuilder errorString = (StringBuilder)errorsMap.get(key);
				errStr.append("<br/>"+errorString);
			}

			FlashScope flash = FlashScope.getScope(request);
	        ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
	        redirect.addParameter("pbm_presc_id", pbmPrescArr[0]);
	        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
	        flash.error(errStr.toString());

			return redirect;

		}else {

			FlashScope flash = FlashScope.getScope(request);
	        ActionRedirect redirect = new ActionRedirect(request.getHeader("Referer").
					replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
	        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

	        String userid = (String)request.getSession().getAttribute("userid");
			boolean success = pbmdao.finalizePrescriptions(pbmPrescList, userid);
			if (!success)
				flash.error("Prescription(s) finalize unsuccessful.");

			return redirect;
		}
	}

	@IgnoreConfidentialFilters
	public ActionForward getPBMPrescriptionScreen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws SQLException, Exception {

		HttpSession session = request.getSession(false);
		boolean mod_eclaim_pbm = (Boolean)session.getAttribute("mod_eclaim_pbm");
		boolean mod_eclaim_erx = (Boolean)session.getAttribute("mod_eclaim_erx");

		String patient_id = request.getParameter("patient_id");
		if (patient_id != null) {
			BasicDynaBean pbmPrescBean = pbmdao.getPBMPatient(patient_id);
			if (pbmPrescBean == null) {
				FlashScope flash = FlashScope.getScope(request);
				flash.put("error", "Invalid Patient Id : "+patient_id);
				ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}

			request.setAttribute("pbmPrescBean", pbmPrescBean);

			String error = "This patient";
			String opType = (pbmPrescBean != null && pbmPrescBean.get("op_type") != null)
							? (String)pbmPrescBean.get("op_type") : null;
			if (!opType.equals("O")) {
				int consId = (pbmPrescBean != null && pbmPrescBean.get("consultation_id") != null)
								? (Integer)pbmPrescBean.get("consultation_id") : 0;
				if (consId == 0) {
					error += " has no doctor consultation,";
				}
			}
			int planId = (pbmPrescBean != null && pbmPrescBean.get("plan_id") != null)
							? (Integer)pbmPrescBean.get("plan_id") : 0;
			if (planId == 0) {
				error += " is not Insured with Plan,";
			}
			if (!mod_eclaim_erx && mod_eclaim_pbm) {
				BasicDynaBean planBean = planDAO.findByKey("plan_id", planId);
				if (planBean != null && !((String)planBean.get("require_pbm_authorization")).equals("Y")) {
					error += " plan do NOT require PBM Authorization.";
				}
			}
			request.setAttribute("error", (error.equals("This patient") ? "" : error));

			String service_reg_no = null;

			if (pbmPrescBean != null) {
				if (pbmPrescBean.get("account_group") != null && ((Integer)pbmPrescBean.get("account_group")).intValue() != 0) {
					BasicDynaBean accbean = new AccountingGroupMasterDAO().findByKey("account_group_id", (Integer)pbmPrescBean.get("account_group"));
					service_reg_no = accbean.get("account_group_service_reg_no") != null ? (String)accbean.get("account_group_service_reg_no") : "";
				}else if (pbmPrescBean.get("center_id") != null) {
					BasicDynaBean centerbean = new CenterMasterDAO().findByKey("center_id", (Integer)pbmPrescBean.get("center_id"));
					service_reg_no = centerbean.get("hospital_center_service_reg_no") != null ? (String)centerbean.get("hospital_center_service_reg_no") : "";
				}
			}
			request.setAttribute("service_reg_no", service_reg_no);

			boolean isInsuranceCardAvailable = PatientDetailsDAO.getCurrentPatientCardImage(patient_id, null) != null;
			request.setAttribute("isInsuranceCardAvailable", isInsuranceCardAvailable);

			List<BasicDynaBean> diagnosisList = pbmdao.findAllDiagnosis(patient_id);
			request.setAttribute("diagnosisList", diagnosisList);
		}

		setRequestAttributes(request, null);
		return mapping.findForward("show");
	}

	@IgnoreConfidentialFilters
	public ActionForward getPBMPrescription(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws SQLException, Exception {

		String pbmPresc = request.getParameter("pbm_presc_id");
		int pbmPrescId = (pbmPresc != null) ? Integer.parseInt(pbmPresc) : 0;
		BasicDynaBean pbmPrescBean = pbmdao.getPBMPresc(pbmPrescId);
		if (pbmPrescBean == null) {
			FlashScope flash = FlashScope.getScope(request);
			flash.put("error", "PBM prescription with id : "+pbmPrescId+" does not exists.");
			ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}

		HttpSession session = request.getSession(false);
		String userid = (String)session.getAttribute("userid");
		Integer centerId = RequestContext.getCenterId();
    	String store = (String) session.getAttribute("pharmacyStoreId");

    	// Selected store from PBM Presc List.
    	Integer storeId = (store != null && !store.equals("")) ? Integer.parseInt(store) : null;
    	Integer pbmStoreId = null;
    	Integer pbmCenterId = null;

		Connection con = null;
		boolean success = true;

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			BasicDynaBean pbmbean = pbmdao.getPBMPresc(pbmPrescId);

			String visitId = (String)pbmbean.get("patient_id");
			String visitType = (String)pbmbean.get("visit_type");
			String ratePlanId = (String)pbmbean.get("org_id");
			int planId = (Integer)pbmbean.get("plan_id");
			String pbmFinalized = (String)pbmbean.get("pbm_finalized");

			// Saved PBM Prescription store.
			pbmStoreId = pbmbean.get("pbm_store_id") != null ? (Integer)pbmbean.get("pbm_store_id") : null;
			pbmCenterId = pbmbean.get("center_id") != null ? (Integer)pbmbean.get("center_id") : centerId;

			if (pbmFinalized.equals("N")) {
				// Update store and store rates for the prescription activities
				// there is no store existing.
				if ((pbmStoreId == null && storeId != null) ||
						(pbmStoreId != null && storeId != null)) {
					success = pbmdao.savePBMPrescStore(con, pbmPrescId, visitId, visitType, ratePlanId, planId, storeId, userid);
				}
			}

		}finally {
			DataBaseUtil.commitClose(con, success);
		}

		String healthAuthority = HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(
				CenterMasterDAO.getHealthAuthorityForCenter(pbmCenterId)).getHealth_authority();
		List<BasicDynaBean> prescActivities = pbmdao.getPBMPrescriptionActivities(pbmPrescId, healthAuthority);

		List<Map> prescActivitiesList = new ArrayList<Map>();
		Map<String, Object> prescActivitiesMap = null;
		List<BasicDynaBean> diagnosisList = null;

		for (BasicDynaBean activity : prescActivities) {
			prescActivitiesMap = new HashMap<String, Object>();

			int pbm_medicine_pres_id = (Integer)activity.get("pbm_medicine_pres_id");
			List<BasicDynaBean> observations = pbmdao.findDrugObservations(pbm_medicine_pres_id);
			prescActivitiesMap.put("activity", activity.getMap());
			prescActivitiesMap.put("observations", ConversionUtils.listBeanToListMap(observations));

			prescActivitiesList.add(prescActivitiesMap);
		}

		request.setAttribute("prescActivitiesList", prescActivitiesList);
		request.setAttribute("pbmPrescBean", pbmPrescBean);

		String service_reg_no = null;

		if (pbmPrescBean != null) {
			if (pbmPrescBean.get("account_group") != null && ((Integer)pbmPrescBean.get("account_group")).intValue() != 0) {
				BasicDynaBean accbean = new AccountingGroupMasterDAO().findByKey("account_group_id", (Integer)pbmPrescBean.get("account_group"));
				service_reg_no = accbean.get("account_group_service_reg_no") != null ? (String)accbean.get("account_group_service_reg_no") : "";
			}else if (pbmPrescBean.get("center_id") != null) {
				BasicDynaBean centerbean = new CenterMasterDAO().findByKey("center_id", (Integer)pbmPrescBean.get("center_id"));
				service_reg_no = centerbean.get("hospital_center_service_reg_no") != null ? (String)centerbean.get("hospital_center_service_reg_no") : "";
			}
		}
		request.setAttribute("service_reg_no", service_reg_no);

		String patient_id = (String)pbmPrescBean.get("patient_id");
		String pbm_presc_status = (String)pbmPrescBean.get("pbm_presc_status");
		String pbm_request_type = pbmPrescBean.get("pbm_request_type") != null ? (String)pbmPrescBean.get("pbm_request_type") : null;
		String resubmit_type = pbmPrescBean.get("resubmit_type") != null ? ((String)pbmPrescBean.get("resubmit_type")).toString() :null;
	//	request.setAttribute("patient_id", patient_id);
		if (pbm_presc_status.equalsIgnoreCase("O") ||
				(pbm_presc_status.equalsIgnoreCase("R") && ("correction").equalsIgnoreCase(resubmit_type)) ||
				(pbm_request_type != null && pbm_request_type.equalsIgnoreCase("cancellation")))
			diagnosisList = pbmdao.findAllDiagnosis(patient_id);
		else
			diagnosisList = pbmdao.findFlaggedDiagnosis(patient_id);

		request.setAttribute("diagnosisList", diagnosisList);

		boolean isInsuranceCardAvailable = PatientDetailsDAO.getCurrentPatientCardImage(patient_id, null) != null;
		request.setAttribute("isInsuranceCardAvailable", isInsuranceCardAvailable);

		setRequestAttributes(request, pbmStoreId);

		return mapping.findForward("show");
	}

	public void setRequestAttributes(HttpServletRequest request, Integer pbmStoreId) throws SQLException {

		JSONSerializer js = new JSONSerializer().exclude("class");
		HttpSession session = request.getSession(false);
		Integer userCenterId =  (Integer) session.getAttribute("centerId");
    	String dept_id = (pbmStoreId == null) ? ((String) session.getAttribute("pharmacyStoreId")) : pbmStoreId.toString();

    	if (dept_id != null && !dept_id.equals("")) {
    		BasicDynaBean dept = storesDAO.findByKey("dept_id", Integer.parseInt(dept_id));
    		if (dept.get("store_rate_plan_id") != null) {
	    		String dept_name = dept.get("dept_name").toString();
	    		request.setAttribute("dept_id", dept_id);
	    		request.setAttribute("dept_name", dept_name);
    		}
		}

    	Integer storeCenterId = userCenterId;
		if (null != request.getParameter("patient_id") && !"".equals(request.getParameter("patient_id")))
			storeCenterId = (Integer) new GenericDAO("patient_registration").findByKey("patient_id",(String)request.getParameter("patient_id")).get("center_id");

		BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
		request.setAttribute("genericPrefs", genericPrefs.getMap());
		String prescription_uses_stores = (String) genericPrefs.get("prescription_uses_stores");
		String prescByGenerics = (String) HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(
				CenterMasterDAO.getHealthAuthorityForCenter(storeCenterId)).getPrescriptions_by_generics();
		request.setAttribute("prescriptions_by_generics",
				prescription_uses_stores.equals("Y") && prescByGenerics.equals("Y"));
    Map filterMap = new HashMap<>();
    filterMap.put("status", "A");
    filterMap.put("medication_type", "M");
		request.setAttribute("frequencies", new RecurrenceDailyMasterDAO().listAll(null, filterMap, null));

		List medDosages = medDosageDao.listAll();
		request.setAttribute("medDosages", js.serialize(ConversionUtils.copyListDynaBeansToMap(medDosages)));

		List pbmObservations = new PBMObservationsMasterDAO().listAll();
		request.setAttribute("pbmObservations", js.serialize(ConversionUtils.copyListDynaBeansToMap(pbmObservations)));

		List<String> columns = new ArrayList<String>();
		columns.add("dept_name");
		columns.add("dept_id");
		columns.add("sale_unit");
		columns.add("status");

		List stores = storesDAO.listAll(columns);
		request.setAttribute("storesJSON", js.serialize(ConversionUtils.copyListDynaBeansToMap(stores)));
		List itemFormList = new GenericDAO("item_form_master").listAll();
		request.setAttribute("itemFormList", js.serialize(ConversionUtils.copyListDynaBeansToMap(itemFormList)));

	}

	public ActionForward markForResubmission(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException {

		String pbmPresc = req.getParameter("pbm_presc_id");
		String comments = req.getParameter("_comments");
		String resubmission_type = req.getParameter("_resubmit_type");
		String patient_id = req.getParameter("patient_id");

		Connection con = null;
		boolean success = true;

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			if (pbmPresc != null && !pbmPresc.equals("")) {

				int pbmPrescId = Integer.parseInt(pbmPresc);

				List<String> columns = new ArrayList<String>();
				columns.add("pbm_presc_id");
				columns.add("pbm_request_id");
				columns.add("pbm_resubmit_request_id");

				Map<String, Object> key = new HashMap<String, Object>();
				key.put("pbm_presc_id", pbmPrescId);
				BasicDynaBean pbmPresBean = pbmdao.findByKey(columns, key);
				String pbmRequestId = (String)pbmPresBean.get("pbm_request_id");
				String pbmResubmitRequestId =
					pbmPresBean.get("pbm_resubmit_request_id") != null ? (String)pbmPresBean.get("pbm_resubmit_request_id") : null;

				int requestCount = DataBaseUtil.getIntValueFromDb("SELECT count(*) FROM pbm_prescription_request WHERE pbm_presc_id = "+pbmPrescId);
	    		String pbmPrescriptionRequestId =
	    			(pbmResubmitRequestId != null &&  !pbmResubmitRequestId.equals("")) ? pbmResubmitRequestId : pbmRequestId;

				BasicDynaBean reqApprovalBean = pbmReqDAO.findByKey("pbm_request_id", pbmRequestId);


	    		Map<String, Object> keys = new HashMap<String, Object>();
				keys.put("pbm_presc_id", pbmPrescId);
				keys.put("pbm_request_id", pbmPrescriptionRequestId);
				BasicDynaBean pbmPrescReq = pbmPrescReqDAO.findByKey(keys);

				if (pbmPrescReq != null) {
					pbmPrescReq = pbmPrescReqDAO.getBean();
					pbmPrescReq.set("pbm_presc_id", pbmPrescId);
					pbmPrescReq.set("pbm_request_id", pbmPrescriptionRequestId);

					pbmPrescReq.set("file_id", reqApprovalBean.get("file_id"));
					pbmPrescReq.set("approval_recd_date", reqApprovalBean.get("approval_recd_date"));
					pbmPrescReq.set("pbm_auth_id_payer", reqApprovalBean.get("pbm_auth_id_payer"));
					pbmPrescReq.set("approval_status", reqApprovalBean.get("approval_status"));
					pbmPrescReq.set("approval_comments", reqApprovalBean.get("approval_comments"));
					pbmPrescReq.set("approval_result", reqApprovalBean.get("approval_result"));
					pbmPrescReq.set("approval_limit", reqApprovalBean.get("approval_limit"));

					int i = pbmPrescReqDAO.update(con, pbmPrescReq.getMap(), keys);
					success = success && (i > 0);
				}

				reqApprovalBean.set("file_id", null);
				reqApprovalBean.set("approval_recd_date", null);
				reqApprovalBean.set("pbm_auth_id_payer", null);
				reqApprovalBean.set("approval_status", null);
				reqApprovalBean.set("approval_comments", null);
				reqApprovalBean.set("approval_result", null);
				reqApprovalBean.set("approval_limit", null);

				int i = pbmReqDAO.updateWithName(con, reqApprovalBean.getMap(), "pbm_request_id");
				success = success && (i > 0);

	    		BasicDynaBean pbmBean = pbmdao.getBean();
	    		pbmBean.set("pbm_presc_id", Integer.parseInt(pbmPresc));
	    		pbmBean.set("status", "R");
	    		pbmBean.set("resubmit_type", resubmission_type);
	    		pbmBean.set("comments", comments);

	    		pbmResubmitRequestId = pbmRequestId + "-RESUBMIT-" +requestCount;
	    		pbmBean.set("pbm_resubmit_request_id", pbmResubmitRequestId);

	    		if (resubmission_type.equals("correction")) {
	    			pbmBean.set("resubmit_request_id_with_correction", ""+requestCount);
	    		}else {
	    			pbmBean.set("resubmit_request_id_with_correction", null);
	    		}

	    		i = pbmdao.updateWithName(con, pbmBean.getMap(), "pbm_presc_id");
	    		success = success && (i > 0);

	    		if (success){
	    			if (resubmission_type.equalsIgnoreCase("correction")){
	    				// Check if there are any prescriptions for this visit that is in status 'C', or 'S' or 'R' type 'internal complaint'
	    				List<BasicDynaBean> sentClosedPBMPrescs = pbmdao.getSentClosedPBMPrescsForVisit(con, patient_id, pbmPrescId);
	    				if (sentClosedPBMPrescs == null || sentClosedPBMPrescs.size() == 0) { // no prescriptions exist
	    					// Reset the flags for the diagnosis codes when status is marked as "Marked for Resubmission", type Correction
	    					boolean setResetSuccess = mrdDao.setResetDiagFlags(con, patient_id, false);
	    					success = success && setResetSuccess;
	    				}
	    			}
	    		}
			}
		}finally {
			DataBaseUtil.commitClose(con, success);
		}

		FlashScope flash = FlashScope.getScope(req);
        ActionRedirect redirect = new ActionRedirect(req.getHeader("Referer").
				replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
 		return redirect;
	}


	public ActionForward addOrEditAttachment(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception,SQLException {

		String pbmPresc = req.getParameter("pbm_presc_id");
		int pbmPrescId = Integer.parseInt(pbmPresc);
		BasicDynaBean pbmPrescBean = pbmdao.getPBMPresc(pbmPrescId);
		req.setAttribute("pbmPrescBean", pbmPrescBean);

		String service_reg_no = null;

		if (pbmPrescBean != null) {
			if (pbmPrescBean.get("account_group") != null && ((Integer)pbmPrescBean.get("account_group")).intValue() != 0) {
				BasicDynaBean accbean = new AccountingGroupMasterDAO().findByKey("account_group_id", (Integer)pbmPrescBean.get("account_group"));
				service_reg_no = accbean.get("account_group_service_reg_no") != null ? (String)accbean.get("account_group_service_reg_no") : "";
			}else if (pbmPrescBean.get("center_id") != null) {
				BasicDynaBean centerbean = new CenterMasterDAO().findByKey("center_id", (Integer)pbmPrescBean.get("center_id"));
				service_reg_no = centerbean.get("hospital_center_service_reg_no") != null ? (String)centerbean.get("hospital_center_service_reg_no") : "";
			}
		}
		req.setAttribute("service_reg_no", service_reg_no);

		int size = pbmdao.getFileSize(pbmPrescId);
		req.setAttribute("fileSize", size);

		return mapping.findForward("addAttachment");

	}

	@IgnoreConfidentialFilters
	public ActionForward showAttachment(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception,SQLException {

		String pbmPresc = req.getParameter("pbm_presc_id");
		int pbmPrescId = Integer.parseInt(pbmPresc);
		Map attchMap = pbmdao.getAttachment(pbmPrescId);

		String type = (String)attchMap.get("Type");
		res.setContentType(type);

		OutputStream os = res.getOutputStream();
		InputStream file = (InputStream)attchMap.get("Content");

		byte[] bytes = new byte[4096];
		int len = 0;
		while ( (len = file.read(bytes)) > 0) {
			os.write(bytes, 0, len);
		}

		os.flush();
		file.close();
		return null;
	}

	@IgnoreConfidentialFilters
	public ActionForward deleteAttachment(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception {

		String pbmPresc = request.getParameter("pbm_presc_id");
		int pbmPrescId = Integer.parseInt(pbmPresc);
		boolean success = pbmdao.deleteAttachment(pbmPrescId);
		FlashScope flash = FlashScope.getScope(request);
		if(!success) {
			flash.put("error", "Attachment could not be deleted.");
		}

        ActionRedirect redirect = new ActionRedirect("PBMPresc.do");
        redirect.addParameter("_method", "addOrEditAttachment");
        redirect.addParameter("pbm_presc_id", pbmPrescId);
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
 		return redirect;
	}


	public Map<String ,Object> getAttachmentMap(HttpServletRequest request)
	 throws FileUploadException, IOException	{

		Map<String,Object> params = new HashMap<String, Object>();
		String contentType = null;
		if (request.getContentType().split("/")[0].equals("multipart")){

			DiskFileItemFactory factory = new DiskFileItemFactory();

			ServletFileUpload upload = new ServletFileUpload(factory);
			List<FileItem> items  = upload.parseRequest(request);
			Iterator it = items.iterator();
			while (it.hasNext()){
				FileItem item = (FileItem) it.next();
				if (item.isFormField()){
					String name = item.getFieldName();
					String value = item.getString();
					params.put(name, new Object[]{value});
				}else {
					String fieldName = item.getFieldName();
					String fileName = item.getName();
					contentType = item.getContentType();
					boolean isInMempry = item.isInMemory();
					long sizeInBytes = item.getSize();
					if (!fileName.equals("")){
						params.put(fieldName, new InputStream[]{item.getInputStream()});
						params.put("attachment_content_type", new String[]{contentType});
						params.put("attachment_size", new Integer[]{item.getInputStream().available()});
					}
				}
			}
		}else {
			params.putAll(request.getParameterMap());
		}
		return params;
	}

	@IgnoreConfidentialFilters
	public ActionForward saveAttachment(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException,Exception {

		FlashScope flash = FlashScope.getScope(request);
		String pbmPresc = request.getParameter("pbm_presc_id");
		int pbmPrescId = Integer.parseInt(pbmPresc);

		Map<String, Object> params = getAttachmentMap(request);
		boolean success = pbmdao.updateAttachment(params, pbmPrescId);
		if(!success)
			 flash.put("error", "Failed to update attachment");

		ActionRedirect redirect = new ActionRedirect("PBMPresc.do");
        redirect.addParameter("_method", "addOrEditAttachment");
        redirect.addParameter("pbm_presc_id", pbmPrescId);
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
 		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward clonePrescription(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws SQLException, Exception {
		HttpSession session = request.getSession(false);
		String userId = (String)session.getAttribute("userid");
		Connection con = null;
		boolean allSuccess = false;
		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect("/PBMAuthorization/PBMPresc.do");
		String pbmPrescStr = request.getParameter("pbm_presc_id");
		int pbmPrescId = (pbmPrescStr != null && !pbmPrescStr.equals("")) ? Integer.parseInt(pbmPrescStr) : 0;
		Integer newPrescId = 0;
		try {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				newPrescId = clonePBMPrescription(con, pbmPrescId, userId);
				allSuccess = (newPrescId != 0);
		} finally {
			DataBaseUtil.commitClose(con, allSuccess);
		}

		if (newPrescId == 0) {
			flash.put("error", "Transaction failed");
		}

		String method = "getPBMPrescription";
		redirect.addParameter("_method", method);
		redirect.addParameter("pbm_presc_id", newPrescId);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}


	private Integer clonePBMPrescription(Connection con, Integer pbmPrescId, String userId) throws SQLException, IOException {
		// Copy of the master entry
		Integer newPrescId = copyPBMPrescription(con, pbmPrescId);
		if (newPrescId == 0) {
			return 0;
		}
		int itemCount = 0;
		// Create all the prescription item entries
		List<BasicDynaBean> sourceItems = pbmMedPrescDAO.findAllByKey(con, "pbm_presc_id", pbmPrescId);
		for (BasicDynaBean sourceItem : sourceItems) {
			BasicDynaBean newPrescItem = copyPrescriptionItem(newPrescId, sourceItem, userId);
			BasicDynaBean savedPrescItem = savePrescriptionItem(con, newPrescItem);
			if (null == savedPrescItem) {
				break;
			}
			itemCount++;
		}

		if (itemCount < sourceItems.size()) {
			return 0;
		}
		// update the drug count in the master
		if (!updateDrugCount(con, newPrescId, itemCount)) {
			return 0;
		}
		// update store details
//		updateStorePresc(con, newPrescId, storeId, patientId, visitType, orgId, planId, userid);
		// we are done
		return newPrescId;
	}

	private Integer copyPBMPrescription(Connection con, int prescId) throws SQLException, IOException {

		List<String> columns = new ArrayList<String>();
		columns.add("pbm_presc_id");
		columns.add("pbm_store_id");
		columns.add("pbm_finalized");
		columns.add("status");
		columns.add("erx_consultation_id");

		Map<String, Object> key = new HashMap<String, Object>();
		key.put("pbm_presc_id", prescId);
		BasicDynaBean sourceBean = pbmdao.findByKey(con, columns, key);

		// BasicDynaBean sourceBean = pbmdao.findByKey(con, "pbm_presc_id", prescId);

		Integer pbmPrescId = pbmdao.getNextSequence();
		BasicDynaBean pbmBean = pbmdao.getBean();
		pbmBean.set("pbm_presc_id", pbmPrescId);
		pbmBean.set("erx_consultation_id", sourceBean.get("erx_consultation_id"));
		pbmBean.set("pbm_finalized", "N");
		pbmBean.set("status", "O");

		if (!pbmdao.insert(con, pbmBean)){
			return 0;
		}
		return pbmPrescId;
	}


	private BasicDynaBean savePrescriptionItem(Connection con, BasicDynaBean newPrescBean) throws SQLException, IOException {

		Integer newPrescriptionId = pbmMedPrescDAO.getNextSequence();
		newPrescBean.set("pbm_medicine_pres_id", newPrescriptionId);

		// Insert item into pbm_medicine_prescriptions
		if (!pbmMedPrescDAO.insert(con, newPrescBean))
			return null;

		return newPrescBean;
	}

	private BasicDynaBean copyPrescriptionItem(Integer prescId, BasicDynaBean sourceBean, String userid) throws SQLException {

		BasicDynaBean itemBean = pbmMedPrescDAO.getBean();
		itemBean.set("pbm_presc_id", prescId);
		itemBean.set("rate", sourceBean.get("rate"));
		itemBean.set("discount", sourceBean.get("discount"));
		itemBean.set("amount", sourceBean.get("amount"));
		itemBean.set("claim_net_amount", sourceBean.get("claim_net_amount"));
		itemBean.set("package_size", sourceBean.get("package_size"));
		itemBean.set("user_unit", sourceBean.get("user_unit"));
		itemBean.set("prescribed_date", sourceBean.get("prescribed_date"));
		itemBean.set("duration", sourceBean.get("duration"));
		itemBean.set("duration_units", sourceBean.get("duration_units"));
		itemBean.set("medicine_quantity", sourceBean.get("medicine_quantity"));
		itemBean.set("medicine_id", sourceBean.get("medicine_id"));
		itemBean.set("generic_code", sourceBean.get("generic_code"));

		itemBean.set("frequency", sourceBean.get("frequency"));
		itemBean.set("medicine_remarks", sourceBean.get("medicine_remarks"));
		itemBean.set("consultation_id", sourceBean.get("consultation_id"));
		itemBean.set("strength", sourceBean.get("strength"));
		itemBean.set("item_strength", sourceBean.get("item_strength"));
		itemBean.set("item_strength_units", sourceBean.get("item_strength_units"));
		itemBean.set("item_form_id", sourceBean.get("item_form_id"));

		itemBean.set("route_of_admin", sourceBean.get("route_of_admin"));
		itemBean.set("username", userid);
		itemBean.set("visit_id", sourceBean.get("visit_id"));
		return itemBean;
	}

	private boolean updateDrugCount(Connection con, Integer pbmPrescId, int drugCount) throws SQLException, IOException {

		if (pbmPrescId != 0) {
			BasicDynaBean pbmBean = pbmdao.getBean();
			pbmBean.set("pbm_presc_id", pbmPrescId);
			pbmBean.set("drug_count", drugCount);

			int n = pbmdao.updateWithName(con, pbmBean.getMap(), "pbm_presc_id");
			if (n <= 0)
				return false;
			else
				return true;
		}
		return true;
	}

	private boolean updateStorePresc(Connection con, Integer pbmPrescId, Integer storeId,
				String patientId, String visitType, String orgId, Integer planId, String userid) throws SQLException, IOException {

		List<String> columns = new ArrayList<String>();
		columns.add("pbm_presc_id");
		columns.add("pbm_store_id");
		columns.add("pbm_finalized");
		columns.add("status");

		Map<String, Object> key = new HashMap<String, Object>();
		key.put("pbm_presc_id", pbmPrescId);
		BasicDynaBean pbmbean = pbmdao.findByKey(con, columns, key);

		Integer pbmStoreId = pbmbean != null && pbmbean.get("pbm_store_id") != null ? (Integer)pbmbean.get("pbm_store_id") : null;
		if ((pbmStoreId == null && storeId != null) ||
				(pbmStoreId != null && storeId != null && pbmStoreId.intValue() != storeId.intValue())) {

			boolean rateUpdate = pbmdao.savePBMPrescStore(con, pbmPrescId,
					patientId, visitType, orgId, planId, storeId, userid);
			if (!rateUpdate)
				return false;
			else
				return true;
		}
		return true;
	}
}
