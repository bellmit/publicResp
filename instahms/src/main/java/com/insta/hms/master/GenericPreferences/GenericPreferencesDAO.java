package com.insta.hms.master.GenericPreferences;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.utils.EnvironmentUtil;
import com.insta.hms.redis.RedisMessagePublisher;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author krishna.t
 *
 */
public class GenericPreferencesDAO {

	static Logger logger = LoggerFactory.getLogger(GenericPreferencesDAO.class);

	private static final String UPDATE_PREFS = "UPDATE generic_preferences SET "+
		"   stock_negative_sale=?, pharma_auto_roundoff=?, " +
		"   autogenerate_sampleid=?," +
		"   bill_later_print_default=?,bill_now_print_default=?," +
		"   hospital_name=?,sale_expiry=?,warn_expiry=?, " +
		"   user_name_in_bill_print=?," +
		"   luxary_tax_applicable_on=?,pharmacy_schedule_h_alert = ?,pharmacy_patient_type=?, " +
		"   deposit_cheque_realization_flow=?, receipt_refund_print_default=?, " +
		"   doctors_custom_field1=?, doctors_custom_field2=?, doctors_custom_field3=?, doctors_custom_field4=?, " +
		"   doctors_custom_field5=?, sample_collection_print_type=?, validate_cost_price=?, " +
		"   claim_service_tax=?, default_prescribe_doctor=?, se_with_po=?, "+
		"   hospital_tin=?, hospital_pan=?, hospital_service_regn_no=?, prescribing_doctor_required=?, show_hosp_amts_pharmasales=?, "+
		"   qty_default_to_issue_unit=?,allow_decimals_for_qty=? , currency_symbol=?, whole=?, decimal=?, currency_format=?," +
		"   force_remarks_po_item_reject= ?,returns_against_specific_supplier=?,fixed_ot_charges = ?,"+
		"   allow_only_indent_based_issue=?, force_sub_group_selection = ?, sales_returns_print_type=?, " +
		"   show_central_excise_duty=?, default_bill_later_creditsales=?, " +
		"   hospital_address=?,default_printer_for_bill_later=?,default_printer_for_bill_now=?,default_prescription_print_template=?," +
		"   default_prescription_web_template=?, default_prescription_web_printer=? , "+
		"   default_consultation_print_template=?,po_approval_reqd_more_than_amt=?, validate_diagnosis_codification=?, return_validity=?," +
		"   package_uom = ?, issue_uom=?, package_size=?," +
		"   default_po_print_template =?, show_tests_in_emr=?, independent_generation_of_sample_id=?, " +
		"	allow_decimals_in_qty_for_issues=?, service_name_required=?,normal_color_code=?,abnormal_color_code=?,critical_color_code=?,improbable_color_code=?, "+
		"   sample_no_generation = ?,surgery_name_required=?, use_smart_card=?,"  +
		"	gen_token_for_lab=?, gen_token_for_rad=?, auto_close_nocharge_op_bills = ?, " +
		"   auto_close_visit = ?, default_emr_print_template = ?," +
		" 	billing_bed_type_for_op = ?, deposit_receipt_print_default = ?, auto_close_claims_with_difference = ?," +
		"   points_earning_points = ?, points_earning_amt = ?, points_redemption_rate = ?, " +
		"   default_dental_cons_print_template=?, allow_cons_qty_incr=?, "+
		"   allow_consumable_stock_negative=?," +
		"	bill_service_charge_percent=? , po_to_be_validated=?, default_voucher_print=?, sample_assertion=?, fin_year_start_month=?, fin_year_end_month=?, diag_images=?, blood_exp=?," +
		"	op_one_presc_doc=?,emr_url_date=? ,procurement_tax_label=?, scheduler_generate_order = ?,hijricalendar = ?,expired_items_procurement=?,procurement_expiry_days=?, " +
		"   check_insu_card_exp_in_sales=? , auto_mail_po_to_sup=?, diag_report_print_center=?,pbm_price_threshold=?,mobile_number_validation=?,mobile_starting_pattern=?,mobile_length_pattern=?," +
		"	is_return_against_grnno=?, apply_supplier_tax_rules=?,no_of_credit_debit_card_digits=?," +
		"   email_bill_printer=?,email_bill_now_template=?,email_bill_later_template=?,mod_username=?";

	public static boolean saveGenericPreferences(GenericPreferencesDTO dto)
			throws SQLException{

		PreparedStatement ps = null;
		Connection con = null;
		boolean flag = false;
		try {
			con = DataBaseUtil.getConnection();
			String query = null;
			ps = con.prepareStatement(UPDATE_PREFS);
			int i=1;
			ps.setString(i++, dto.getStockNegativeSale());
			ps.setString(i++, dto.getPharmaAutoRoundOff());
			ps.setString(i++, dto.getAutoSampleIdRequired());
			ps.setString(i++, dto.getBillLaterPrintDefault());
			ps.setString(i++, dto.getBillNowPrintDefault());
			ps.setString(i++, dto.getHospitalName());
			ps.setString(i++, dto.getSaleOfExpiredItems());
			ps.setInt(i++, Integer.parseInt(dto.getWarnForExpiry()));
			ps.setString(i++, dto.getUserNameInBillPrint());
			ps.setString(i++, dto.getLuxTax());
			ps.setString(i++, dto.getHdrugAlert());
			ps.setString(i++, dto.getPharmacyPatientType());
			ps.setString(i++, dto.getDepositChequeRealizationFlow());
			ps.setString(i++, dto.getReceiptRefundPrintDefault());
			ps.setString(i++, dto.getdoctorCustomField1());
			ps.setString(i++, dto.getdoctorCustomField2());
			ps.setString(i++, dto.getdoctorCustomField3());
			ps.setString(i++, dto.getdoctorCustomField4());
			ps.setString(i++, dto.getdoctorCustomField5());
			ps.setString(i++, dto.getSampleCollectionPrintType());
			ps.setString(i++, dto.getPharmacyValidateCostPrice());
			ps.setBigDecimal(i++, new BigDecimal(dto.getServiceTaxOnClaimAmount()));
			ps.setString(i++, dto.getDefault_prescribe_doctor());
			ps.setString(i++, dto.getSeWithPO());
			ps.setString(i++, dto.getHospitalTin());
			ps.setString(i++, dto.getHospitalPan());
			ps.setString(i++, dto.getHospitalServiceRegnNo());
			ps.setString(i++, dto.getPrescribingDoctorRequired());
			ps.setString(i++, dto.getShowHospAmtsOnPharmaSales());
			ps.setString(i++, dto.getQtyDefaultToIssueUnit());
			ps.setString(i++, dto.getAllowdecimalsforqty());
			ps.setString(i++, dto.getCurrencySymbol());
			ps.setString(i++, dto.getWhole());
			ps.setString(i++, dto.getDecimal());
			ps.setString(i++, dto.getCurrencyFormat());
			ps.setString(i++, dto.getForceRemarksForPoItemReject());
			ps.setString(i++, dto.getReturnAgainstSpecificSupplier());
			ps.setString(i++, dto.getFixedOtCharges());
			ps.setString(i++, dto.getAllowIndentBasedIssue());
			ps.setString(i++, dto.getForceSubGroupSelection());
			ps.setString(i++, dto.getSalesReturnsPrintType());
			ps.setString(i++, dto.getShowCED());
			ps.setString(i++, dto.getDefBillLaterCreditSales());
			ps.setString(i++, dto.getHospitalAddress());
			ps.setInt(i++, Integer.parseInt(dto.getDefault_printer_for_bill_later()));
			ps.setInt(i++, Integer.parseInt(dto.getDefault_printer_for_bill_now()));
			ps.setString(i++, dto.getDefault_prescription_print_template());
			ps.setString(i++, dto.getDefault_prescription_web_template());
			ps.setInt(i++, Integer.parseInt(dto.getDefault_prescription_web_printer()));
			ps.setString(i++, dto.getDefault_consultation_print_template());
			ps.setBigDecimal(i++, dto.getPoApprovalLimit());
			ps.setString(i++, dto.getValidateDiagnosisCodification());
			ps.setObject(i++, dto.getReturnValidDays() == null || dto.getReturnValidDays().equals("") ? null : Integer.parseInt(dto.getReturnValidDays()));
			ps.setString(i++, dto.getPackageUOM());
			ps.setString(i++, dto.getIssueUOM());
			ps.setBigDecimal(i++, dto.getPackageSize() == null || dto.getPackageSize().equals("") ? new BigDecimal(1) : dto.getPackageSize());
			ps.setString(i++, dto.getDefault_po_print_template());
			ps.setString(i++, dto.getShow_tests_in_emr());
			ps.setString(i++, dto.getIndependentGenerationOfSampleId());
			ps.setString(i++, dto.getAllowDecimalsInQtyForIssues());
			ps.setString(i++, dto.getServiceNameRequired());
			ps.setString(i++, dto.getTestNormalResult());
			ps.setString(i++, dto.getTestAbnormalResult());
			ps.setString(i++, dto.getTestCriticalResult());
			ps.setString(i++, dto.getTestImprobableResult());
			ps.setString(i++, dto.getSampleNoBase());
			ps.setString(i++, dto.getSurgeryNameRequired());
			ps.setString(i++, dto.getUse_smart_card());
			ps.setString(i++, dto.getGen_token_for_lab());
			ps.setString(i++, dto.getGen_token_for_rad());
			ps.setString(i++, dto.getAutoCloseNoChargeOpBills());
			ps.setString(i++, dto.getAutoCloseVisits());
			ps.setString(i++, dto.getDefault_emr_print_template());
			ps.setString(i++, dto.getBillingBedTypeForOP());
			ps.setString(i++, dto.getDepositReceiptRefundPrintDefault());
			ps.setBigDecimal(i++, dto.getAuto_close_claims_with_difference() == null ? BigDecimal.ZERO : dto.getAuto_close_claims_with_difference());
			ps.setInt(i++, dto.getPoints_earning_points());
			ps.setBigDecimal(i++, dto.getPoints_earning_amt());
			ps.setBigDecimal(i++, dto.getPoints_redemption_rate());
			ps.setString(i++, dto.getDefault_dental_cons_print_template());
			ps.setString(i++, dto.getAllowConstantConsumableQtyIncrease());
			ps.setString(i++, dto.getConsumableStockNegative());
			ps.setBigDecimal(i++, (dto.getServiceChargePercent() == null || dto.getServiceChargePercent().equals(""))
									? BigDecimal.ZERO : new BigDecimal(dto.getServiceChargePercent()));
			ps.setString(i++, dto.getPoToBeValidated());
			ps.setString(i++, dto.getDefault_voucher_print());
			ps.setString(i++, dto.getSampleassertion());
			ps.setInt(i++, dto.getFin_year_start_month());
			ps.setInt(i++, dto.getFinYearEndMonth());
			String[] diagImage = dto.getDiag_images();
			String diagImages = "";
			for (String s : diagImage){
				diagImages = diagImages + s + ",";
			}
			ps.setString(i++, diagImages.equals("") ? null :diagImages.substring(0, diagImages.length()-1));
			ps.setInt(i++, dto.getbloodExpiry());
			ps.setString(i++, dto.getOp_one_presc_doc());
			ps.setString(i++, dto.getEmr_url_date());
			ps.setString(i++, dto.getProcurement_tax_label());
			ps.setString(i++, dto.getSchedulerGenerateOrder());
			ps.setString(i++, dto.getHijriCalendar());
			ps.setString(i++, dto.getExpiredItemsProcurement());
			ps.setInt(i++, dto.getProcurementExpiryDays());
			ps.setString(i++, dto.getCheck_insu_card_exp_in_sales());
			ps.setBoolean(i++, !dto.getAuto_mail_po_to_sup().equals("f"));
			ps.setString(i++, dto.getDiag_report_print_center());
			ps.setInt(i++, dto.getPbmPriceThreshold());
			ps.setString(i++, dto.getIsMobileValidate());
			ps.setString(i++, dto.getMobileStartPattern());
			ps.setString(i++, dto.getMobileLengthPattern());
			ps.setBoolean(i++, !dto.getIs_return_against_grnno().equals("f"));
			ps.setBoolean(i++, !dto.getApply_supplier_tax_rules().equals("f"));
			ps.setInt(i++, dto.getNo_of_credit_debit_card_digits());
			ps.setInt(i++,dto.getEmailBillPrint());
			ps.setString(i++, dto.getEmailBillNowTemplate());
			ps.setString(i++, dto.getEmailBillLaterTemplate());
			ps.setString(i++, dto.getMod_username());

			if (ps.executeUpdate() == 1) {
				flag = true;
			}

			// since the prefs are changed, we need to get the new one, so clear the cache.
			GenericPreferencesCache.CACHEDPREFERENCESDTO.remove(RequestContext.getSchema());
			GenericPreferencesCache.CACHEDPREFERENCESBEAN.remove(RequestContext.getSchema());
			invalidateGenericPrefCache();
			
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return flag;
	}

	private static final String GET_PREFS = "SELECT protocol, host_name, port_no, auth_required, generic_preferences.username, "+
		" password, hospital_mail_id, stock_negative_sale, pharma_allow_cp_sale, pharma_auto_roundoff, " +
		" sampleflow_required,autogenerate_sampleid,bill_later_print_default,bill_now_print_default, " +
		" hospital_name,sale_expiry," +
		" warn_expiry,domain_name, pharma_return_restricted," +
		" user_name_in_bill_print, allow_bill_reopen, luxary_tax_applicable_on, pharmacy_schedule_h_alert,  " +
		" pharmacy_patient_type, seperate_pharmacy_credit_bill," +
		" deposit_cheque_realization_flow, receipt_refund_print_default, " +
		" doctors_custom_field1,doctors_custom_field2,doctors_custom_field3,doctors_custom_field4," +
		" doctors_custom_field5,sample_collection_print_type,validate_cost_price, pharmacy_sale_margin_in_per, claim_service_tax, " +
		" default_prescribe_doctor, deposit_avalibility,se_with_po, receive_transfer_indent, hospital_tin, " +
		" hospital_pan, hospital_service_regn_no, prescribing_doctor_required, show_hosp_amts_pharmasales,qty_default_to_issue_unit, "+
		" allow_decimals_for_qty, currency_symbol, whole, decimal,currency_format,force_remarks_po_item_reject," +
		" returns_against_specific_supplier,fixed_ot_charges," +
		" allow_only_indent_based_issue,force_sub_group_selection,sales_returns_print_type," +
		" show_central_excise_duty,operation_apllicable_for, go_live_date, " +
		" default_bill_later_creditsales, hospital_address,vat_applicable,cess_applicable," +
		" default_printer_for_bill_later,default_printer_for_bill_now,barcode_for_item, after_decimal_digits,default_prescription_print_template," +
		" default_prescription_web_template, default_prescription_web_printer, upload_limit_in_mb, sales_print_items, default_consultation_print_template,po_approval_reqd_more_than_amt, " +
		" validate_diagnosis_codification, allow_bill_now_insurance, return_validity, package_uom, issue_uom, package_size, " +
		" hosp_uses_dynamic_addresses, default_po_print_template, max_centers_inc_default," +
		" max_active_hosp_users, show_tests_in_emr,independent_generation_of_sample_id, allow_decimals_in_qty_for_issues, service_name_required, " +
		" normal_color_code,abnormal_color_code,critical_color_code,improbable_color_code," +
		" sample_no_generation,surgery_name_required, use_smart_card," +
		" gen_token_for_lab, gen_token_for_rad,auto_close_nocharge_op_bills, auto_close_visit, default_emr_print_template," +
		" billing_bed_type_for_op, " +
		" deposit_receipt_print_default, auto_close_claims_with_difference, " +
		" points_earning_points, points_earning_amt, points_redemption_rate, " +
		" default_dental_cons_print_template,allow_cons_qty_incr, indent_approval_by, corporate_insurance, " +
		" allow_zero_claim_amount_for_op, allow_zero_claim_amount_for_ip,allow_consumable_stock_negative," +
		" bill_service_charge_percent,po_to_be_validated, default_voucher_print, sample_assertion , issue_to_dept_only," +
		" allow_cross_center_indents,fin_year_start_month,fin_year_end_month,bill_cancellation_requires_approval," +
		" diag_images,blood_exp, op_one_presc_doc,aggregate_amt_on_remittance, " +
		" enable_force_selection_for_mrno_search,emr_url_date,procurement_tax_label,restrict_inactive_visits, " +
		" scheduler_generate_order,hijricalendar,expired_items_procurement,procurement_expiry_days,stock_entry_agnst_do,check_insu_card_exp_in_sales," +
		" auto_mail_po_to_sup, diag_report_print_center, pbm_price_threshold,mobile_number_validation,mobile_starting_pattern," +
		" mobile_length_pattern, is_return_against_grnno, apply_supplier_tax_rules, separator_type, bill_label_for_bill_later_bills, " +
		" currency_format,no_of_credit_debit_card_digits,email_bill_printer,email_bill_now_template,email_bill_later_template,po_round_off,income_tax_cash_limit_applicability, "+
		" enable_patient_deposit_availability,bill_pending_validation_activity_types,ra_auto_download_last_no_of_days,apply_cp_validation_for_po FROM generic_preferences ";

	/**
	 * getGenericPreferences()
	 * @return
	 * @throws SQLException
	 * 
	 * Do not use this method. Use getAllPrefs() ins struts and  in spring<hr> com.insta.hms.master.GenericPreferences.GenericPreferencesRepository.getAllPreferences() </hr>
	 * 
	 *  
	 */

	@Deprecated
	public static GenericPreferencesDTO getGenericPreferences() throws SQLException {
		String schema = RequestContext.getSchema();
		GenericPreferencesDTO dto = GenericPreferencesCache.CACHEDPREFERENCESDTO.get(schema);
		if (dto == null || GenericPreferencesCache.CACHEDPREFERENCESDTO.isEmpty()) {
			Connection con = DataBaseUtil.getConnection();
			try {
				return getGenericPreferences(con);
			} finally {
				DataBaseUtil.closeConnections(con, null);
			}
		} else {
			return dto;
		}
	}

	/**
	 * getGenericPreferences(Connection)
	 * @return
	 * @throws SQLException
	 * 
	 * Do not use this method. Use getAllPrefs() in struts and in spring use <hr> com.insta.hms.master.GenericPreferences.GenericPreferencesRepository.getAllPreferences() </hr>
	 * 
	 *  
	 */

	@Deprecated
	public static GenericPreferencesDTO getGenericPreferences(Connection con) throws SQLException {
		String schema = RequestContext.getSchema();
		GenericPreferencesDTO dto = GenericPreferencesCache.CACHEDPREFERENCESDTO.get(schema);
		if (dto == null || GenericPreferencesCache.CACHEDPREFERENCESDTO.isEmpty()) {
			dto = getGenericPreferencesFromDB(con);
			GenericPreferencesCache.CACHEDPREFERENCESDTO.put(schema, dto);
		}
		return dto;
	}

	private static GenericPreferencesDTO getGenericPreferencesFromDB() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		try {
			return getGenericPreferencesFromDB(con);
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public static GenericPreferencesDTO getGenericPreferencesFromDB(Connection con) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		GenericPreferencesDTO dto = null;
		try {
			ps = con.prepareStatement(GET_PREFS);
			rs = ps.executeQuery();
			if (rs.next()){
				dto = new GenericPreferencesDTO();
				dto.setProtocol(rs.getString("protocol"));
				dto.setHostName(rs.getString("host_name"));
				dto.setPortNo(rs.getString("port_no"));
				dto.setAuthRequired(rs.getString("auth_required"));
				dto.setMailUserName(rs.getString("username"));
				dto.setMailPassword(rs.getString("password"));
				dto.setHospMailID(rs.getString("hospital_mail_id"));
				dto.setStockNegativeSale(rs.getString("stock_negative_sale"));
				dto.setPharmaAllowCpSale(rs.getString("pharma_allow_cp_sale"));
				dto.setPharmaAutoRoundOff(rs.getString("pharma_auto_roundoff"));
				dto.setBillLaterPrintDefault(rs.getString("bill_later_print_default"));
				dto.setBillNowPrintDefault(rs.getString("bill_now_print_default"));
				dto.setSampleFlowRequired(rs.getString("sampleflow_required"));
				dto.setAutoSampleIdRequired(rs.getString("autogenerate_sampleid"));
				dto.setHospitalName(rs.getString("hospital_name"));
				dto.setSaleOfExpiredItems(rs.getString("sale_expiry"));
				dto.setWarnForExpiry(rs.getString("warn_expiry"));
				dto.setDomainName(rs.getString("domain_name"));
				dto.setPharmaReturnRestricted(rs.getString("pharma_return_restricted"));
				dto.setUserNameInBillPrint(rs.getString("user_name_in_bill_print"));
				dto.setAllowBillReopen(rs.getString("allow_bill_reopen"));
				dto.setLuxTax(rs.getString("luxary_tax_applicable_on"));
				dto.setHdrugAlert(rs.getString("pharmacy_schedule_h_alert"));
				dto.setPharmacyPatientType(rs.getString("pharmacy_patient_type"));
				dto.setPharmacySeperateCreditbill(rs.getString("seperate_pharmacy_credit_bill"));
				dto.setDepositChequeRealizationFlow(rs.getString("deposit_cheque_realization_flow"));
				dto.setReceiptRefundPrintDefault(rs.getString("receipt_refund_print_default"));
				dto.setdoctorCustomField1(rs.getString("doctors_custom_field1"));
				dto.setdoctorCustomField2(rs.getString("doctors_custom_field2"));
				dto.setdoctorCustomField3(rs.getString("doctors_custom_field3"));
				dto.setdoctorCustomField4(rs.getString("doctors_custom_field4"));
				dto.setdoctorCustomField5(rs.getString("doctors_custom_field5"));
				dto.setSampleCollectionPrintType(rs.getString("sample_collection_print_type"));
				dto.setPharmacyValidateCostPrice(rs.getString("validate_cost_price"));
				dto.setPharmacySaleMargin(rs.getString("pharmacy_sale_margin_in_per"));
				dto.setServiceTaxOnClaimAmount(rs.getString("claim_service_tax"));
				dto.setDefault_prescribe_doctor(rs.getString("default_prescribe_doctor"));
				dto.setDeposit_avalibility(rs.getString("deposit_avalibility"));
				dto.setSeWithPO(rs.getString("se_with_po"));
				dto.setRecTransIndent(rs.getString("receive_transfer_indent"));
				dto.setHospitalTin(rs.getString("hospital_tin"));
				dto.setHospitalPan(rs.getString("hospital_pan"));
				dto.setHospitalServiceRegnNo(rs.getString("hospital_service_regn_no"));
				dto.setPrescribingDoctorRequired(rs.getString("prescribing_doctor_required"));
				dto.setShowHospAmtsOnPharmaSales(rs.getString("show_hosp_amts_pharmasales"));
				dto.setQtyDefaultToIssueUnit(rs.getString("qty_default_to_issue_unit"));
				dto.setAllowdecimalsforqty(rs.getString("allow_decimals_for_qty"));
				dto.setCurrencySymbol(rs.getString("currency_symbol"));
				dto.setWhole(rs.getString("whole"));
				dto.setDecimal(rs.getString("decimal"));
				dto.setCurrencyFormat(rs.getString("currency_format"));
				dto.setForceRemarksForPoItemReject(rs.getString("force_remarks_po_item_reject"));
				dto.setReturnAgainstSpecificSupplier(rs.getString("returns_against_specific_supplier"));
				dto.setFixedOtCharges(rs.getString("fixed_ot_charges"));
				dto.setAllowIndentBasedIssue(rs.getString("allow_only_indent_based_issue"));
				dto.setForceSubGroupSelection(rs.getString("force_sub_group_selection"));
				dto.setSalesReturnsPrintType(rs.getString("sales_returns_print_type"));
				dto.setShowCED(rs.getString("show_central_excise_duty"));
				dto.setOperationApplicableFor(rs.getString("operation_apllicable_for"));
				dto.setGoLiveDate(rs.getDate("go_live_date"));
				dto.setDefBillLaterCreditSales(rs.getString("default_bill_later_creditsales"));
				dto.setHospitalAddress(rs.getString("hospital_address"));
				dto.setShowVAT(rs.getString("vat_applicable"));
				dto.setShowCESS(rs.getString("cess_applicable"));
				dto.setDefault_printer_for_bill_later(rs.getString("default_printer_for_bill_later"));
				dto.setDefault_printer_for_bill_now(rs.getString("default_printer_for_bill_now"));
				dto.setBarcodeForItem(rs.getString("barcode_for_item"));
				dto.setDecimalDigits(rs.getInt("after_decimal_digits"));
				dto.setDefault_prescription_print_template(rs.getString("default_prescription_print_template"));
				dto.setDefault_prescription_web_template(rs.getString("default_prescription_web_template"));
				dto.setDefault_prescription_web_printer(rs.getString("default_prescription_web_printer"));
				dto.setDefault_consultation_print_template(rs.getString("default_consultation_print_template"));
				dto.setUploadLimitInMB(rs.getInt("upload_limit_in_mb"));
				dto.setSalesPrintItems(rs.getString("sales_print_items"));
				dto.setPoApprovalLimit(rs.getBigDecimal("po_approval_reqd_more_than_amt"));
				dto.setValidateDiagnosisCodification(rs.getString("validate_diagnosis_codification"));
				dto.setAllowBillNowInsurance(rs.getString("allow_bill_now_insurance"));
				dto.setReturnValidDays(rs.getString("return_validity"));
				dto.setPackageUOM(rs.getString("package_uom"));
				dto.setIssueUOM(rs.getString("issue_uom"));
				dto.setPackageSize(rs.getBigDecimal("package_size"));
				dto.setHospUsesDynamicAddress(rs.getString("hosp_uses_dynamic_addresses"));
				dto.setDefault_po_print_template(rs.getString("default_po_print_template"));
				dto.setMax_centers_inc_default(rs.getInt("max_centers_inc_default"));
				dto.setMax_active_hosp_users(rs.getInt("max_active_hosp_users"));
				dto.setShow_tests_in_emr(rs.getString("show_tests_in_emr"));
				dto.setIndependentGenerationOfSampleId(rs.getString("independent_generation_of_sample_id"));
				dto.setAllowDecimalsInQtyForIssues(rs.getString("allow_decimals_in_qty_for_issues"));
				dto.setServiceNameRequired(rs.getString("service_name_required"));
				dto.setTestNormalResult(rs.getString("normal_color_code"));
				dto.setTestAbnormalResult(rs.getString("abnormal_color_code"));
				dto.setTestCriticalResult(rs.getString("critical_color_code"));
				dto.setTestImprobableResult(rs.getString("improbable_color_code"));
				dto.setSampleNoBase(rs.getString("sample_no_generation"));
				dto.setSurgeryNameRequired(rs.getString("surgery_name_required"));
				dto.setUse_smart_card(rs.getString("use_smart_card"));
				dto.setGen_token_for_lab(rs.getString("gen_token_for_lab"));
				dto.setGen_token_for_rad(rs.getString("gen_token_for_rad"));
				dto.setAutoCloseNoChargeOpBills(rs.getString("auto_close_nocharge_op_bills"));
				dto.setAutoCloseVisits(rs.getString("auto_close_visit"));
				dto.setDefault_emr_print_template(rs.getString("default_emr_print_template"));
				dto.setBillingBedTypeForOP(rs.getString("billing_bed_type_for_op"));
				dto.setDepositReceiptRefundPrintDefault(rs.getString("deposit_receipt_print_default"));
				dto.setAuto_close_claims_with_difference(rs.getBigDecimal("auto_close_claims_with_difference"));
				dto.setPoints_earning_points(rs.getInt("points_earning_points"));
				dto.setPoints_earning_amt(rs.getBigDecimal("points_earning_amt"));
				dto.setPoints_redemption_rate(rs.getBigDecimal("points_redemption_rate"));
				dto.setDefault_dental_cons_print_template(rs.getString("default_dental_cons_print_template"));
				dto.setAllowConstantConsumableQtyIncrease(rs.getString("allow_cons_qty_incr"));
				dto.setIndent_approval_by(rs.getString("indent_approval_by"));
				dto.setCorporate_insurance(rs.getString("corporate_insurance"));
				dto.setConsumableStockNegative(rs.getString("allow_consumable_stock_negative"));
				dto.setServiceChargePercent(rs.getString("bill_service_charge_percent"));
				dto.setPoToBeValidated(rs.getString("po_to_be_validated"));
				dto.setDefault_voucher_print(rs.getString("default_voucher_print"));
				dto.setSampleassertion(rs.getString("sample_assertion"));
				dto.setIssuetodeptonly(rs.getString("issue_to_dept_only"));
				dto.setAllow_cross_center_indents(rs.getString("allow_cross_center_indents"));
				dto.setFin_year_start_month(rs.getInt("fin_year_start_month"));
				dto.setFin_year_end_month(rs.getInt("fin_year_end_month"));
				dto.setFinYearEndMonth(rs.getInt("fin_year_end_month"));
				dto.setDiag_images(rs.getString("diag_images") != null ? rs.getString("diag_images").split(",") : null);
				dto.setBillcancellationrequiresapproval(rs.getString("bill_cancellation_requires_approval"));
				dto.setAggregate_amt_on_remittance(rs.getString("aggregate_amt_on_remittance"));
				dto.setbloodExpiry(rs.getInt("blood_exp"));
				dto.setOp_one_presc_doc(rs.getString("op_one_presc_doc"));
				dto.setEnable_force_selection_for_mrno_search("enable_force_selection_for_mrno_search");
				dto.setEmr_url_date(rs.getString("emr_url_date"));
				dto.setProcurement_tax_label(rs.getString("procurement_tax_label"));
				dto.setRestrictInactiveIpVisit(rs.getString("restrict_inactive_visits"));
				dto.setSchedulerGenerateOrder(rs.getString("scheduler_generate_order"));
				dto.setHijriCalendar(rs.getString("hijricalendar"));
				dto.setExpiredItemsProcurement(rs.getString("expired_items_procurement"));
				dto.setProcurementExpiryDays(rs.getInt("procurement_expiry_days"));
				dto.setStock_entry_agnst_do(rs.getString("stock_entry_agnst_do"));
				dto.setCheck_insu_card_exp_in_sales(rs.getString("check_insu_card_exp_in_sales"));
				dto.setAuto_mail_po_to_sup(rs.getString("auto_mail_po_to_sup"));
				dto.setDiag_report_print_center(rs.getString("diag_report_print_center"));
				dto.setPbmPriceThreshold(rs.getInt("pbm_price_threshold"));
				dto.setIsMobileValidate(rs.getString("mobile_number_validation"));
				dto.setMobileStartPattern(rs.getString("mobile_starting_pattern"));
				dto.setMobileLengthPattern(rs.getString("mobile_length_pattern"));
				dto.setIs_return_against_grnno(rs.getString("is_return_against_grnno"));
				dto.setApply_supplier_tax_rules(rs.getString("apply_supplier_tax_rules"));
				dto.setSeparator_type(rs.getString("separator_type"));
				dto.setCurrency_format(rs.getString("currency_format"));
				dto.setNo_of_credit_debit_card_digits(rs.getInt("no_of_credit_debit_card_digits"));
				dto.setEmailBillPrint(rs.getInt("email_bill_printer"));
				dto.setEmailBillNowTemplate(rs.getString("email_bill_now_template"));
				dto.setEmailBillLaterTemplate(rs.getString("email_bill_later_template"));
				dto.setBillLabelForBillLaterBills(rs.getString("bill_label_for_bill_later_bills"));;
				dto.setPoroundoff(rs.getString("po_round_off"));
				dto.setIncomeTaxCashLimitApplicability(rs.getString("income_tax_cash_limit_applicability"));
				dto.setEnablePatientDepositAvailability(rs.getString("enable_patient_deposit_availability"));
				dto.setBillPendingValidationActivityTypes(rs.getString("bill_pending_validation_activity_types"));
				dto.setRaAutoProcessLastNumberOfDays(rs.getInt("ra_auto_download_last_no_of_days"));
				dto.setApplyCpValidationForPo(rs.getString("apply_cp_validation_for_po"));
			}
		} finally {
			DataBaseUtil.closeConnections(null, ps, rs);
		}
		return dto;
	}

	/*
	 * Bean version of the above: useful for "backend-only" preferences.
	 */
	public static final String GET_ALL_PREFS = "SELECT * FROM generic_preferences";

    public static BasicDynaBean getAllPrefs() throws SQLException {
        
        
        String schema = RequestContext.getSchema();
        
        BasicDynaBean bean = GenericPreferencesCache.CACHEDPREFERENCESBEAN.get(schema);
        
        if (bean == null) {
            bean = DataBaseUtil.queryToDynaBean(GET_ALL_PREFS, new Object[]{});
            GenericPreferencesCache.CACHEDPREFERENCESBEAN.put(schema, bean);
        }
        return bean;
    }

	/**
	 * 
	 * @return
	 * @throws SQLException
	 * 
	 * Do not use this method. Ues getAllPrefs() ins struts and <hr> com.insta.hms.master.GenericPreferences.GenericPreferencesRepository.getAllPreferences() </hr>
	 * 
	 *  
	 */
    @Deprecated
	public static BasicDynaBean getPrefsBean() throws SQLException {
		return getAllPrefs();
	}

	private static final String GET_DIAG_GENERIC_PREF = "SELECT sampleflow_required,autogenerate_sampleid,sample_assertion," +
			"autogenerate_labno, sample_no_generation, gen_token_for_lab, gen_token_for_rad, diag_images," +
			" sample_collection_print_type, optimized_lab_report_print FROM generic_preferences ";
	public static BasicDynaBean  getdiagGenericPref() throws SQLException{
		List l =DataBaseUtil.queryToDynaList(GET_DIAG_GENERIC_PREF);
		if(l!=null && l.size() >0){
			return (BasicDynaBean) l.get(0);
		}
		return null;
	}

	private static final String GET_SCREEN_LOGO = "SELECT screen_logo FROM hosp_print_master_files where center_id = 0";

	public static InputStream getScreenLogo() throws SQLException {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			stmt = con.prepareStatement(GET_SCREEN_LOGO);
			rs = stmt.executeQuery();
			if (rs.next())
				return rs.getBinaryStream(1);
			else
				return null;
		} finally {
			DataBaseUtil.closeConnections(con, stmt, rs);
		}
	}


	private static final String DELETE_SCREEN_LOGO = "UPDATE hosp_print_master_files set screen_logo=null where center_id = 0";

	public static boolean deleteScreenLogo() throws SQLException {
		PreparedStatement stmt = null;
		Connection con = null;
		boolean success=false;
		try {
			con = DataBaseUtil.getConnection();
			stmt = con.prepareStatement(DELETE_SCREEN_LOGO);
			int result = stmt.executeUpdate();
			if (result > 0 ) success = true;
		} finally {
			DataBaseUtil.closeConnections(con, stmt);
		}
		return success;
	}


	private static final String UPDATE_SCREEN_LOGO = "UPDATE hosp_print_master_files SET screen_logo=? where center_id = 0";

	public static boolean updateScreenLogo(InputStream l, int size) throws SQLException {
		PreparedStatement ps = null;
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(UPDATE_SCREEN_LOGO);
			ps.setBinaryStream(1, l, size);
			int result = ps.executeUpdate();
			return (result > 0 );
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	public static final String GET_FILE_SIZES = "SELECT length(screen_logo) as screen_logo_size " +
		" FROM hosp_print_master_files where center_id = 0";

	public static int getFileSizes() throws SQLException {
		int size = 0;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_FILE_SIZES);
			rs = ps.executeQuery();
			while (rs.next()){
				size = rs.getInt(1);
			}
		}finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}

		return size;
	}

	/**
	 * 
	 * @return
	 * @throws SQLException
	 * 
	 * Try to do not use this method. Use getAllPrefs, because its cashed. 
	 * In spring use <hr> com.insta.hms.master.GenericPreferences.GenericPreferencesRepository.getAllPreferences() </hr>
	 */
	@Deprecated
	public static boolean isBillNowTpaAllowed() throws SQLException {
		boolean allow = false;
		BasicDynaBean prefbean = getAllPrefs();
		if (prefbean != null && prefbean.get("allow_bill_now_insurance") != null)
			return ((String)prefbean.get("allow_bill_now_insurance")).equals("Y");
		return allow;
	}

/*	public static String getPreferenceBillRatePlan() throws SQLException {
		String ratePlan = null;
		BasicDynaBean prefbean = getAllPrefs();
		if (prefbean != null && prefbean.get("rate_plan_for_non_insured_bill") != null
					&& !((String)prefbean.get("rate_plan_for_non_insured_bill")).equals(""))
			return (String)prefbean.get("rate_plan_for_non_insured_bill");
		return ratePlan;
	}*/
	
	/**
	 * 
	 * @return
	 * @throws SQLException
	 * 
	 * Try to do not use this method. Use getAllPrefs, because its cashed. 
	 * In spring use <hr> com.insta.hms.master.GenericPreferences.GenericPreferencesRepository.getAllPreferences() </hr>
	 */
	
	public static final String GET_CALENDAR_START_DAY = "SELECT calendar_start_day from generic_preferences";
	@Deprecated
    public static int getCalendarStartDay() throws SQLException {
        int startday=0;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try{
            con = DataBaseUtil.getReadOnlyConnection();
            ps = con.prepareStatement(GET_CALENDAR_START_DAY);
            rs = ps.executeQuery();
            while (rs.next()){
                startday = rs.getInt(1);
            }
        }finally {
            DataBaseUtil.closeConnections(con, ps, rs);
        }

        return startday;

    }
	
    public static void invalidateGenericPrefCache() {
        String schema = RequestContext.getSchema();
        clearCache();
        if (EnvironmentUtil.isDistributed()) {
            ApplicationContextProvider.getApplicationContext().getBean(RedisMessagePublisher.class)
                    .notifyCacheInvalidation(schema + "@generic_preferences");
        }
    }
	 
	 public static void clearCache() {
		GenericPreferencesCache.CACHEDPREFERENCESDTO.remove(RequestContext.getSchema());
		GenericPreferencesCache.CACHEDPREFERENCESBEAN.remove(RequestContext.getSchema());
	}
}

