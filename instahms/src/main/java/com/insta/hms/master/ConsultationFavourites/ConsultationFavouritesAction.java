/**
 *
 */
package com.insta.hms.master.ConsultationFavourites;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.MedicineRoute.MedicineRouteDAO;
import com.insta.hms.master.PrescriptionsMaster.PrescriptionsMasterDAO;
import com.insta.hms.outpatient.DoctorConsultationDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author krishna
 *
 */
public class ConsultationFavouritesAction extends DispatchAction {

	ConsultationFavouritesDAO medDAO = new ConsultationFavouritesDAO("doctor_medicine_favourites");
	ConsultationFavouritesDAO otherMedDAO = new ConsultationFavouritesDAO("doctor_other_medicine_favourites");
	ConsultationFavouritesDAO testDAO = new ConsultationFavouritesDAO("doctor_test_favourites");
	ConsultationFavouritesDAO serviceDAO = new ConsultationFavouritesDAO("doctor_service_favourites");
	ConsultationFavouritesDAO crossConsultDAO = new ConsultationFavouritesDAO("doctor_consultation_favourites");
	ConsultationFavouritesDAO opeDAO = new ConsultationFavouritesDAO("doctor_operation_favourites");
	ConsultationFavouritesDAO nonHospDAO = new ConsultationFavouritesDAO("doctor_other_favourites");
	//	when pharmacy module is not there, use the medicines from the prescribed_medicine_master.
	// when new medicine is prescribed insert it into the prescribed_medicine_master as well.
	PrescriptionsMasterDAO pmDao = new PrescriptionsMasterDAO();
	// when new dosage is prescribed, insert it into the medicine_dosage_master.
	GenericDAO medDosageDao = new GenericDAO("medicine_dosage_master");
	GenericDAO presInstructionDao = new GenericDAO("presc_instr_master");
	DoctorMasterDAO dmDao = new DoctorMasterDAO();
	MedicineRouteDAO routeDAO = new MedicineRouteDAO();

	JSONSerializer js = new JSONSerializer().exclude("class");
	
	@IgnoreConfidentialFilters
	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {
		HttpSession session = request.getSession(false);
		BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
		String doctorId = request.getParameter("doctor_id");
		//BasicDynaBean doctorBean = dmDao.findByKey("doctor_id", doctorId);
		BasicDynaBean doctorBean = DoctorMasterDAO.getDoctorFavouriteCen(doctorId);

		int centerId;
		if (doctorBean != null && !doctorBean.equals("") && ((Integer)doctorBean.get("center_id") != 0)) {
			centerId = (Integer) doctorBean.get("center_id");
		} else if ((Integer) session.getAttribute("centerId") != 0) {
			centerId = (Integer) session.getAttribute("centerId");
		} else {
			centerId = 0;
		}
		request.setAttribute("Max_centers_inc_default", (Integer)genericPrefs.get("max_centers_inc_default"));
		request.setAttribute("center_id_js",centerId);
		request.setAttribute("genericPrefs", genericPrefs.getMap());
		String prescByGenerics = (String) HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(
				CenterMasterDAO.getHealthAuthorityForCenter(centerId)).getPrescriptions_by_generics();
		String prescription_uses_stores = (String) genericPrefs.get("prescription_uses_stores");
		request.setAttribute("prescriptions_by_generics",
				prescription_uses_stores.equals("Y") && prescByGenerics.equals("Y"));

		List medDosages = medDosageDao.listAll();
		List itemFormList = new GenericDAO("item_form_master").listAll();
		List presInstructions = presInstructionDao.listAll();

		request.setAttribute("presInstructions", js.serialize(ConversionUtils.copyListDynaBeansToMap(presInstructions)));
		request.setAttribute("itemFormList", js.serialize(ConversionUtils.copyListDynaBeansToMap(itemFormList)));
		request.setAttribute("all_favourites", ConsultationFavouritesDAO.getAllFavourites(doctorId, prescription_uses_stores));
		request.setAttribute("medDosages", js.serialize(ConversionUtils.copyListDynaBeansToMap(medDosages)));
		request.setAttribute("doctor_bean", doctorBean);
		request.setAttribute("routes_list_json", js.serialize(ConversionUtils.copyListDynaBeansToMap(
				routeDAO.listAll(null, "status", "A"))));
		return mapping.findForward("list");
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {

		HttpSession session = request.getSession(false);
		String doctorId = request.getParameter("doctor_id");
		//BasicDynaBean doctorBean = dmDao.findByKey("doctor_id", doctorId);
		BasicDynaBean doctorBean = DoctorMasterDAO.getDoctorFavouriteCen(doctorId);

		int centerId;
		if (doctorBean != null && !doctorBean.equals("") && ((Integer)doctorBean.get("center_id") != 0)) {
			centerId = (Integer) doctorBean.get("center_id");
		} else if ((Integer) session.getAttribute("centerId") != 0) {
			centerId = (Integer) session.getAttribute("centerId");
		} else {
			centerId = 0;
		}
		String prescByGenerics = (String) HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(
				CenterMasterDAO.getHealthAuthorityForCenter(centerId)).getPrescriptions_by_generics();

		String[] favouriteIds = request.getParameterValues("favourite_id");
		String[] itemNames = request.getParameterValues("item_name");
		String[] itemIds = request.getParameterValues("item_id");
		String[] adminStrength = request.getParameterValues("admin_strength");
		String[] medFrequencies = request.getParameterValues("frequency");
		String[] durations = request.getParameterValues("duration");
		String[] duration_units = request.getParameterValues("duration_units");
		String[] medQty = request.getParameterValues("medicine_quantity");
		String[] item_remarks = request.getParameterValues("item_remarks");
		String[] special_instructions = request.getParameterValues("special_instr");
		String[] delItems = request.getParameterValues("delItem");
		String[] itemType = request.getParameterValues("itemType");
		String[] itemMaster = request.getParameterValues("item_master");
		String[] ispackage = request.getParameterValues("ispackage");
		String[] routeOfAdmin = request.getParameterValues("route_id");
//		 strength is the dosage to the patient how much to take (1 tab or 1/2 tab or 5ml or 10ml)
		String[] medicine_strengths = request.getParameterValues("strength");
		String[] generic_code = request.getParameterValues("generic_code");
		String[] item_form_ids = request.getParameterValues("item_form_id");
//		 item_strength is the medicine strength (100mg, 200mg, 500ml etc.,)
		String[] item_strengths = request.getParameterValues("item_strength");
		String[] item_strength_units = request.getParameterValues("item_strength_units");
		String[] display_order = request.getParameterValues("display_order");
		String[] non_hosp_medicine = request.getParameterValues("non_hosp_medicine");
		String[] cons_uom_id = request.getParameterValues("cons_uom_id");
		String[] presc_by_generics = request.getParameterValues("presc_by_generics");

		BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
		String use_store_items = (String) genericPrefs.get("prescription_uses_stores");
		Boolean prescriptionsByGenerics = use_store_items.equals("Y") && prescByGenerics.equals("Y");

		GenericDAO mDao =
			use_store_items.equals("Y") ? new ConsultationFavouritesDAO("doctor_medicine_favourites") : new ConsultationFavouritesDAO("doctor_other_medicine_favourites");

		boolean allSuccess = false;
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		String error = null;
		try {
			txn: {
				if (favouriteIds != null) {
					/*
					 * deleting the records done intentionally as first transaction, which will allow deleting the duplicates.
					 * if we do it record by record, it will fail, when we have any duplicates in the transaction,
					 * 	we get an duplicate error and that's end.
					 * so we stuck there, we can't add or update or delete further.
					 *
					 * Actually, we are not allowing duplicates while insert itself, but this done to be on the safe side.
					 */
					if (!deleteAllMarked(con, itemType, favouriteIds, delItems, non_hosp_medicine, genericPrefs)) {
						error = "Failed to delete the favourites";
						break txn;
					}
					for (int i=0; i< favouriteIds.length-1; i++) {
						boolean deleteItem = new Boolean(delItems[i]);
						String favouriteId = favouriteIds[i];
						int favouriteIdInt = 0;
						if (itemType[i].equals("Medicine") && !new Boolean(non_hosp_medicine[i])) {
							BasicDynaBean med = mDao.getBean();
							String duration = durations[i];
							String medicineQuantity = medQty[i];
							if (!duration.equals("")) {
								med.set("duration", Integer.parseInt(duration));
								med.set("duration_units", duration_units[i]);
							} else {
								med.set("duration", null);
								med.set("duration_units", null);
							}
							if (!medicineQuantity.equals("")) {
								med.set("medicine_quantity", Integer.parseInt(medicineQuantity));
							} else {
								med.set("medicine_quantity", null);
							}
							if (use_store_items.equals("Y")) {
								if (presc_by_generics[i].equals("false")) // instand of cheking preferance only check for itemId.
									med.set("medicine_id", Integer.parseInt(itemIds[i]));
								//	update the generic_code always when pharmacy module is enabled
								med.set("generic_code", generic_code[i]);
							} else {
								med.set("medicine_name", itemNames[i]);
							}
							med.set("admin_strength", adminStrength[i]);
							med.set("frequency", medFrequencies[i]);
							med.set("medicine_remarks", item_remarks[i]);
							med.set("special_instr", special_instructions[i]);
							med.set("doctor_id", doctorId);
							med.set("strength", medicine_strengths[i]);
							med.set("item_strength", item_strengths[i]);
							if (cons_uom_id[i] != null && !cons_uom_id[i].equals("")) {
							  med.set("cons_uom_id", Integer.parseInt(cons_uom_id[i]));
							} else {
							  med.set("cons_uom_id", null);
							}
							if (!item_strength_units[i].equals(""))
								med.set("item_strength_units", Integer.parseInt(item_strength_units[i]));
							else
								med.set("item_strength_units", null);

							if (!item_form_ids[i].equals(""))
								med.set("item_form_id", Integer.parseInt(item_form_ids[i]));
							else
								med.set("item_form_id", null);

							if (!routeOfAdmin[i].equals("")) {
								med.set("route_of_admin", Integer.parseInt(routeOfAdmin[i]));
							} else {
								med.set("route_of_admin", null);
							}
							med.set("display_order", Integer.parseInt(display_order[i]));

							if (!favouriteId.equals("_")) {
								favouriteIdInt = Integer.parseInt(favouriteId);
							}
							if (!deleteItem && DoctorConsultationDAO.isDuplicate(con, med, itemType[i], doctorId,
									use_store_items, favouriteIdInt, false, new Boolean(non_hosp_medicine[i]))) {
								error = "Found medicine duplicates..";
								break txn;
							}
							if (favouriteId.equals("_")) {
								String frequency = medFrequencies[i].trim();
								BigDecimal perdayqty =  null;
								if (!duration.equals("") && !medicineQuantity.equals("")) {
									BigDecimal Qty = new BigDecimal(Integer.parseInt(medQty[i]), new MathContext(2));
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
									BasicDynaBean presMedMasterBean = pmDao.getBean();
									presMedMasterBean.set("medicine_name", itemNames[i]);
									presMedMasterBean.set("status", "A");

									if (!pmDao.insert(con, presMedMasterBean))
										break txn;
								}

								favouriteIdInt = mDao.getNextSequence();
								med.set("favourite_id", favouriteIdInt);
								if (!mDao.insert(con, med))
									break txn;

							} else {
								favouriteIdInt = Integer.parseInt(favouriteIds[i]);
								if (!deleteItem) {
									Map keys = new HashMap();
									keys.put("favourite_id", favouriteIdInt);
									if (mDao.update(con, med.getMap(), keys) <=0 )
										break txn;
								}
							}
						} else if (itemType[i].equals("Inv.")) {
							BasicDynaBean test = testDAO.getBean();
							test.set("test_id", itemIds[i]);
							test.set("test_remarks", item_remarks[i]);
							test.set("special_instr", special_instructions[i]);
							test.set("doctor_id", doctorId);
							test.set("ispackage", new Boolean(ispackage[i]));
							test.set("display_order", Integer.parseInt(display_order[i]));

							if (!favouriteId.equals("_")) {
								favouriteIdInt = Integer.parseInt(favouriteId);
							}
							if (!deleteItem && DoctorConsultationDAO.isDuplicate(con, test, itemType[i], doctorId,
									use_store_items, favouriteIdInt, false, new Boolean(non_hosp_medicine[i]))) {
								error = "Found test duplicates..";
								break txn;
							}
							if (favouriteId.equals("_")) {
								favouriteIdInt = testDAO.getNextSequence();
								test.set("favourite_id", favouriteIdInt);
								if (!testDAO.insert(con, test))
									break txn;
							} else {
								favouriteIdInt = Integer.parseInt(favouriteIds[i]);
								if (!deleteItem) {
									Map keys = new HashMap();
									keys.put("favourite_id", favouriteIdInt);
									if (testDAO.update(con, test.getMap(), keys) <=0 )
										break txn;
								}
							}
						} else if (itemType[i].equals("Service")) {
							BasicDynaBean ser = serviceDAO.getBean();
							ser.set("service_id", itemIds[i]);
							ser.set("service_remarks", item_remarks[i]);
							ser.set("special_instr", special_instructions[i]);
							ser.set("doctor_id", doctorId);
							ser.set("display_order", Integer.parseInt(display_order[i]));

							if (!favouriteId.equals("_")) {
								favouriteIdInt = Integer.parseInt(favouriteId);
							}
							if (!deleteItem && DoctorConsultationDAO.isDuplicate(con, ser, itemType[i], doctorId,
									use_store_items, favouriteIdInt, false, new Boolean(non_hosp_medicine[i]))) {
								error = "Found service duplicates..";
								break txn;
							}
							if (favouriteId.equals("_")) {
								favouriteIdInt = serviceDAO.getNextSequence();
								ser.set("favourite_id", favouriteIdInt);
								if (!serviceDAO.insert(con, ser)) break txn;

							} else {
								favouriteIdInt = Integer.parseInt(favouriteIds[i]);
								if (!deleteItem) {
									Map keys = new HashMap();
									keys.put("favourite_id", favouriteIdInt);
									if (serviceDAO.update(con, ser.getMap(), keys) <= 0)
										break txn;
								}
							}
						} else if (itemType[i].equals("Doctor")) {
							BasicDynaBean doctor = crossConsultDAO.getBean();
							doctor.set("doctor_id", doctorId);
							doctor.set("cons_doctor_id", itemIds[i]);
							doctor.set("consultation_remarks", item_remarks[i]);
							doctor.set("special_instr", special_instructions[i]);
							doctor.set("doctor_id", doctorId);
							doctor.set("display_order", Integer.parseInt(display_order[i]));

							if (!favouriteId.equals("_")) {
								favouriteIdInt = Integer.parseInt(favouriteId);
							}
							if (!deleteItem && DoctorConsultationDAO.isDuplicate(con, doctor, itemType[i], doctorId,
									use_store_items, favouriteIdInt, false, new Boolean(non_hosp_medicine[i]))) {
								error = "Found Cross-Consultation duplicates..";
								break txn;
							}
							if (favouriteId.equals("_")) {
								favouriteIdInt = crossConsultDAO.getNextSequence();
								doctor.set("favourite_id", favouriteIdInt);
								if (!crossConsultDAO.insert(con, doctor)) break txn;

							} else {
								favouriteIdInt = Integer.parseInt(favouriteIds[i]);
								if (!deleteItem) {
									Map keys = new HashMap();
									keys.put("favourite_id", favouriteIdInt);
									if (crossConsultDAO.update(con, doctor.getMap(), keys) <= 0)
										break txn;
								}
							}
						} else if (itemType[i].equals("NonHospital") || (itemType[i].equals("Medicine") && new Boolean(non_hosp_medicine[i]))) {
							BasicDynaBean nhBean = nonHospDAO.getBean();
							nhBean.set("item_name", itemNames[i]);
							nhBean.set("doctor_id", doctorId);
							nhBean.set("item_remarks", item_remarks[i]);
							nhBean.set("special_instr", special_instructions[i]);
							nhBean.set("non_hosp_medicine", new Boolean(non_hosp_medicine[i]));
							if (cons_uom_id[i] != null && !cons_uom_id[i].equals("")) {
							  nhBean.set("cons_uom_id", Integer.parseInt(cons_uom_id[i]));
                            } else {
                              nhBean.set("cons_uom_id", null);
                            }
							String duration = durations[i];
							String medicineQuantity = medQty[i];
							if (!duration.equals("")) {
								nhBean.set("duration", Integer.parseInt(duration));
								nhBean.set("duration_units", duration_units[i]);
							} else {
								nhBean.set("duration", null);
								nhBean.set("duration_units", null);
							}
							if (!medicineQuantity.equals("")) {
								nhBean.set("medicine_quantity", Integer.parseInt(medicineQuantity));
							} else {
								nhBean.set("medicine_quantity", null);
							}
							nhBean.set("admin_strength", adminStrength[i]);
							nhBean.set("frequency", medFrequencies[i]);
							nhBean.set("strength", medicine_strengths[i]);
							nhBean.set("item_strength", item_strengths[i]);
							if (!item_strength_units[i].equals(""))
								nhBean.set("item_strength_units", Integer.parseInt(item_strength_units[i]));
							else
								nhBean.set("item_strength_units", null);

							if (!item_form_ids[i].equals(""))
								nhBean.set("item_form_id", Integer.parseInt(item_form_ids[i]));
							else
								nhBean.set("item_form_id", null);

							nhBean.set("display_order", Integer.parseInt(display_order[i]));
							if (!favouriteId.equals("_")) {
								favouriteIdInt = Integer.parseInt(favouriteId);
							}
							if (!deleteItem && DoctorConsultationDAO.isDuplicate(con, nhBean, itemType[i], doctorId,
									use_store_items, favouriteIdInt, false, new Boolean(non_hosp_medicine[i]))) {
								error = "Found Non Hospital item duplicates..";
								break txn;
							}
							if (favouriteId.equals("_")) {
								favouriteIdInt = nonHospDAO.getNextSequence();
								nhBean.set("favourite_id", favouriteIdInt);
								if (!nonHospDAO.insert(con, nhBean)) break txn;
							} else {
								favouriteIdInt = Integer.parseInt(favouriteIds[i]);
								if (!deleteItem) {
									Map keys = new HashMap();
									keys.put("favourite_id", favouriteIdInt);
									if (nonHospDAO.update(con, nhBean.getMap(), keys) <= 0) break txn;
								}
							}

						} else if (itemType[i].equals("Operation")) {
							BasicDynaBean opBean = opeDAO.getBean();
							opBean.set("operation_id", itemIds[i]);
							opBean.set("doctor_id", doctorId);
							opBean.set("remarks", item_remarks[i]);
							opBean.set("special_instr", special_instructions[i]);
							opBean.set("display_order", Integer.parseInt(display_order[i]));

							if (!favouriteId.equals("_")) {
								favouriteIdInt = Integer.parseInt(favouriteId);
							}
							if (!deleteItem && DoctorConsultationDAO.isDuplicate(con, opBean, itemType[i], doctorId,
									use_store_items, favouriteIdInt, false, new Boolean(non_hosp_medicine[i]))) {
								error = "Found Operation duplicates..";
								break txn;
							}
							if (favouriteId.equals("_")) {
								favouriteIdInt = opeDAO.getNextSequence();
								opBean.set("favourite_id", opeDAO.getNextSequence());
								if (!opeDAO.insert(con, opBean)) break txn;
							} else {
								favouriteIdInt = Integer.parseInt(favouriteIds[i]);
								if (!deleteItem) {
									Map keys = new HashMap();
									keys.put("favourite_id", favouriteIdInt);
									if (opeDAO.update(con, opBean.getMap(), keys) <= 0) break txn;
								}
							}
						}
					}
				}
				allSuccess = true;
			}
		} finally {
			DataBaseUtil.commitClose(con, allSuccess);
		}
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
		FlashScope flash = FlashScope.getScope(request);

		if (!allSuccess) {
			flash.error(error != null ? error : "Failed to insert/update/delete the favourites..");
		}
		redirect.addParameter("doctor_id", doctorId);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	private boolean deleteAllMarked(Connection con, String[] itemType, String[] favouriteIds, String[] delItems,
			String[] non_hosp_medicines, BasicDynaBean genPrefs) throws SQLException {
		String use_store_items = (String) genPrefs.get("prescription_uses_stores");

		GenericDAO mDao =
			use_store_items.equals("Y") ? new ConsultationFavouritesDAO("doctor_medicine_favourites") :
				new ConsultationFavouritesDAO("doctor_other_medicine_favourites");

		for (int i=0; i<itemType.length; i++) {
			boolean deleteItem = new Boolean(delItems[i]);
			if (!favouriteIds.equals("_") && deleteItem) {
				if (itemType[i].equals("Medicine") && !new Boolean(non_hosp_medicines[i])) {
					if (!mDao.delete(con, "favourite_id", Integer.parseInt(favouriteIds[i]))) return false;
				} else if (itemType[i].equals("Inv.")) {
					if (!testDAO.delete(con, "favourite_id", Integer.parseInt(favouriteIds[i]))) return false;
				} else if (itemType[i].equals("Service")) {
					if (!serviceDAO.delete(con, "favourite_id", Integer.parseInt(favouriteIds[i]))) return false;
				} else if (itemType[i].equals("Doctor")) {
					if (!crossConsultDAO.delete(con, "favourite_id", Integer.parseInt(favouriteIds[i]))) return false;
				} else if (itemType[i].equals("NonHospital") || (itemType[i].equals("Medicine") && new Boolean(non_hosp_medicines[i]))) {
					if (!nonHospDAO.delete(con, "favourite_id", Integer.parseInt(favouriteIds[i]))) return false;
				} else if (itemType[i].equals("Operation")) {
					if (!opeDAO.delete(con, "favourite_id", Integer.parseInt(favouriteIds[i]))) return false;
				}
			}
		}
		return true;
	}

}
