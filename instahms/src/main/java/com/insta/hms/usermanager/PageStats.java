package com.insta.hms.usermanager;

import org.apache.commons.beanutils.BasicDynaBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * The Enum PageStats.
 *
 * @author deepasri.prasad
 */

public enum PageStats {

  /** The todays collection. */
  TODAYS_COLLECTION(
      "TODAYS_COLLECTION",
      "rep_collection_detailed_builder",
      "Today's Collection",
      "SELECT COALESCE (SUM(amount),0) AS COUNT FROM ( SELECT amount FROM receipts r "
          + " JOIN counters c ON (r.counter = c.counter_id) WHERE c.collection_counter='Y' AND "
          + " date(display_date) = current_date ) as query",
      "/billing/CollectionReports.do?screenId=rep_collection_detailed_builder&reportName="
          + "Collections+Report&method=getReport&outputMode=pdf&_searchMethod=getScreen&"
          + "_parent_report_name=Collections+Report+Builder&selDateRange=td&_myreport=nosearch&"
          + "reportType=list&dateFieldSelection=receipt_date&_sel=on&fromDate=${td}&toDate=${td}&"
          + "istFields=payment_mode_name&listFields=main_type&listFields=detailed_type&"
          + "listFields=receipt_id&listFields=receipt_date&listFields=counter&listFields=remarks&"
          + "listFields=amt&baseFontSize=10&filter.1=collection_counter&filterOp.1=eq&"
          + "filterVal.1=Yes"),

  /** The todays pharmacy coll. */
  TODAYS_PHARMACY_COLL(
      "TODAYS_PHARMACY_COLL",
      "rep_collection_detailed_builder",
      "Today's Pharmacy Collection",
      "SELECT COALESCE (SUM(amount),0) AS COUNT FROM ( SELECT amount FROM receipts r "
          + " JOIN counters c ON (r.counter = c.counter_id) WHERE c.collection_counter='Y' AND "
          + " counter_type='P' AND date(display_date)=current_date "
          + " ) as query",
      "/billing/CollectionReports.do?screenId=rep_collection_detailed_builder&reportName="
          + "Collections+Report&method=getReport&outputMode=pdf&_searchMethod=getScreen&"
          + "_parent_report_name=Collections+Report+Builder&selDateRange=td&srjsFile=null&"
          + "reptDescFile=CollectionDetails.srjs&_myreport=nosearch&reportType=list&"
          + "dateFieldSelection=receipt_date&_sel=on&fromDate=26-10-2010&toDate=26-10-2010&"
          + "listFields=payment_mode_name&listFields=main_type&listFields=detailed_type&"
          + "listFields=receipt_id&listFields=receipt_date&listFields=counter&listFields=remarks"
          + "&listFields=amt&baseFontSize=10&userNameNeeded=Y&listGroups=&listGroups=&listGroups=&"
          + "sumGroupHoriz=&sumGroupVert=&sumGroupVertSub=&trendType=month&trendGroupVert=&"
          + "trendGroupVertSub=&customOrder1=&customOrder2=&filter.1=counter_type&filterOp.1=eq&"
          + "filterVal.1=Pharmacy&filter.2=collection_counter&filterOp.2=eq&filterVal.2=Yes&"
          + "_report_name=&_actionId=rep_collection_detailed_builder"),

  /** The todays purchases. */
  TODAYS_PURCHASES("TODAYS_PURCHASES",
      "ph_purchase_rep_builder",
      "Today's Purchases",
      " SELECT TRUNC(COALESCE(SUM(g.billed_qty/g.grn_pkg_size*g.cost_price - g.discount+ "
          + " i.round_off + g.tax + g.item_ced + i.other_charges),0), 2) AS COUNT FROM "
          + " store_grn_details g JOIN store_grn_main gm USING (grn_no) JOIN  store_invoice i "
          + " USING (supplier_invoice_id) WHERE gm.grn_date::DATE BETWEEN current_date AND "
          + " current_date ",
      "/stores/PurchaseInvoiceReportBuilder.do?screenId=ph_invoice_rep_builder&reportName="
          + "Purchase+Invoice+Report&method=getReport&outputMode=pdf&_searchMethod=getScreen&"
          + "_parent_report_name=Purchase+Invoice+Report+Builder&selDateRange=td&srjsFile=null&"
          + "reptDescFile=PharmacyInvoice.srjs&reportGroup=Procurement+Reports&current_user="
          + "ArunTest&print_title=Purchase+Invoice+Report&_myreport=nosearch&reportType=list&"
          + "dateFieldSelection=grn_date&_sel=on&fromDate=${td}&toDate=${td}&listFields="
          + "store_name&listFields=supplier_name&listFields=invoice_no&listFields=invoice_date&"
          + "listFields=grn_nos&listFields=received_debit_amt&listFields=raised_amt&listFields="
          + "difference_amt&listFields=invoice_amount&baseFontSize=10&listGroups=&listGroups=&"
          + "listGroups=&sumGroupHoriz=&sumGroupVert=&sumGroupVertSub=&trendType=month&"
          + "trendGroupVert=&trendGroupVertSub=&vtrendGroupHoriz=&vtrendType=month&"
          + "vtrendGroupVertSub=&customOrder1=&customOrder2=&filter.1=purchase_type&filterOp.1=eq&"
          + "filterVal.1=Purchase&_report_name=&_actionId=ph_invoice_rep_builder&pdfcstm_option="
          + "un_needed&userNameNeeded=Y&pdfcstm_option=dt_needed&dt_needed=true&pdfcstm_option="
          + "hsp_needed&hsp_needed=true&hsp_needed_h=false&pdfcstm_option=pgn_needed&pgn_needed="
          + "true&grpn_needed=false&pdfcstm_option=filterDesc_needed&filterDesc_needed=true"),

  /** The open bill now. */
  OPEN_BILL_NOW("OPEN_BILL_NOW",
      "search_bills",
      "Open Bill Now bills",
      "SELECT COALESCE(count(*),0) AS COUNT from bill WHERE  STATUS ='A' AND bill_type!='C'",
      "/pages/BillDischarge/BillList.do?_method=getBills&_searchMethod=getBills&sortOrder="
      + "open_date&sortReverse=true&_mysearch=nosearch&mr_no=&mr_no%40op=ilike&bill_no=&"
      + "bill_no%40op=ilike&open_date=&open_date=&open_date%40op=ge%2Cle&finalized_date=&"
      + "finalized_date%40type=date&finalized_date%40op=ge&finalized_date=&finalized_date%40op=le&"
      + "bill_type=P&restriction_type=&status=A&visit_type=&_search_name=&_actionId=search_bills"),

  /** The old open bill now. */
  OLD_OPEN_BILL_NOW("OLD_OPEN_BILL_NOW",
      "search_bills",
      "Open Bill Now bills > 1 day",
      "SELECT COALESCE(COUNT(*),0) AS COUNT from bill WHERE STATUS ='A' AND bill_type!='C' AND "
          + " open_date::date NOT BETWEEN current_date AND current_date",
      "/pages/BillDischarge/BillList.do?_method=getBills&_searchMethod=getBills&"
          + "sortOrder=open_date&sortReverse=true&_mysearch=nosearch&mr_no=&mr_no%40op=ilike&"
          + "bill_no=&bill_no%40op=ilike&open_date=&open_date=${yd}&open_date%40op=ge%2Cle&"
          + "open_date%40cast=y&finalized_date=&finalized_date%40type=date&finalized_date%40op=ge&"
          + "finalized_date=&finalized_date%40op=le&bill_type=P&restriction_type=&status=A&"
          + "visit_type=&_search_name=&_actionId=search_bills"),

  /** The old phar retail bills. */
  OLD_PHAR_RETAIL_BILLS("OLD_PHAR_RETAIL_BILLS",
      "search_bills",
      "Pharmacy Retail bills open > 1 day",
      "SELECT COALESCE(count(*),0) AS  COUNT from bill WHERE STATUS ='A' AND visit_type='r' AND "
          + " open_date::date NOT BETWEEN current_date AND current_date",
      "/pages/BillDischarge/BillList.do?_method=getBills&_searchMethod=getBills&"
          + "sortOrder=open_date&sortReverse=true&_mysearch=nosearch&mr_no=&mr_no%40op=ilike&"
          + "bill_no=&bill_no%40op=ilike&open_date=&open_date=${yd}&open_date%40op=ge%2Cle&"
          + "finalized_date=&finalized_date%40type=date&finalized_date%40op=ge&finalized_date=&"
          + "finalized_date%40op=le&bill_type=&restriction_type=&status=A&visit_type=r&"
          + "_search_name=&_actionId=search_bills"),

  /** The pending claims. */
  PENDING_CLAIMS("PENDING_CLAIMS",
      "search_bills",
      "Claims Pending",
      "SELECT COALESCE(count(bill_no),0) AS COUNT FROM bill b WHERE primary_claim_status='S' AND "
          + " b.status='F' AND payment_status='U' AND is_tpa",
      "/pages/BillDischarge/BillList.do?_method=getBills&_searchMethod=getBills&"
          + "sortOrder=open_date&sortReverse=true&_mysearch=nosearch&mr_no=&mr_no%40op=ilike&"
          + "bill_no=&bill_no%40op=ilike&open_date=&open_date=&open_date%40op=ge%2Cle&"
          + "finalized_date=&finalized_date%40type=date&finalized_date%40op=ge&finalized_date=&"
          + "finalized_date%40op=le&bill_type=&restriction_type=&status=F&visit_type=&"
          + "payment_status=U&primary_claim_status=S&_search_name=&_actionId=search_bills&"
          + "is_tpa=true&is_tpa%40cast=y"),

  /** The todays cancelled bills. */
  TODAYS_CANCELLED_BILLS("TODAYS_CANCELLED_BILLS",
      "search_bills",
      "Today's Cancelled Bills",
      "SELECT COALESCE(COUNT(*),0) AS COUNT FROM bill WHERE  mod_time::date between current_date "
          + " and current_date AND status='X' ",
      "/pages/BillDischarge/BillList.do?_method=getBills&_searchMethod=getBills&"
          + "sortOrder=open_date&sortReverse=true&_mysearch=nosearch&mr_no=&mr_no%40op=ilike&"
          + "bill_no=&bill_no%40op=ilike&cl_date%40cast=y&cl_date=${td}&cl_date=${td}&"
          + "cl_date%40op=ge%2Cle&cl_date%40type=date&finalized_date=&finalized_date%40type=date&"
          + "finalized_date%40op=ge&finalized_date=&finalized_date%40op=le&bill_type=&"
          + "restriction_type=&status=X&visit_type=&_search_name=&_actionId=search_bills"),

  /** The to be collected advance. */
  TO_BE_COLLECTED_ADVANCE("TO_BE_COLLECTED_ADVANCE",
      "search_bills",
      "Advance to be collected bills",
      "SELECT COALESCE(COUNT(*),0) AS COUNT FROM bill WHERE "
          + " (COALESCE(total_amount,0)-COALESCE(total_receipts,0))>0  AND status='A' AND "
          + " bill_type='C' ",
      "/pages/BillDischarge/BillList.do?_method=getBills&_searchMethod=getBills&"
          + "sortOrder=open_date&sortReverse=true&_mysearch=nosearch&mr_no=&mr_no%40op=ilike&"
          + "bill_no=&bill_no%40op=ilike&open_date=&open_date=&open_date%40op=ge%2Cle&"
          + "finalized_date=&finalized_date%40type=date&finalized_date%40op=ge&finalized_date=&"
          + "finalized_date%40op=le&bill_type=C&restriction_type=&status=A&visit_type=&"
          + "primary_sponsor_id=&primary_claim_status=&_search_name=&_actionId=search_bills&"
          + "adv_to_be_collected=0&adv_to_be_collected%40op=gt&"
          + "adv_to_be_collected%40type=numeric"),

  /** The active op patients. */
  ACTIVE_OP_PATIENTS("ACTIVE_OP_PATIENTS",
      "visit_details_search",
      "Active Out Patients",
      "SELECT COALESCE(COUNT(*),0) AS COUNT  FROM  all_visits_view WHERE status='A' AND "
          + " visit_type='o'",
      "/VisitDetailsSearch.do?_method=list&status=A&sortOrder=visit_reg_date&sortReverse=true&"
          + "visit_type=o"),

  /** The active ip patients. */
  ACTIVE_IP_PATIENTS("ACTIVE_IP_PATIENTS",
      "visit_details_search",
      "Active In Patients",
      "SELECT COALESCE(COUNT(*),0) AS COUNT FROM all_visits_view WHERE status='A' AND "
          + " visit_type='i'",
      "/VisitDetailsSearch.do?_method=list&status=A&sortOrder=visit_reg_date&sortReverse=true&"
          + "visit_type=i"),

  /** The todays discharges. */
  TODAYS_DISCHARGES("TODAYS_DISCHARGES",
      "visit_details_search",
      "Today's Discharges",
      "SELECT COALESCE(COUNT(*),0) AS COUNT FROM patient_registration JOIN discharge_type_master "
          + " dtm USING(discharge_type_id) WHERE discharge_flag='D' AND visit_type='i' AND "
          + " coalesce(dtm.discharge_type, '') != 'Admission Cancelled' AND discharge_date::DATE "
          + " BETWEEN current_date AND current_date",
      "/VisitDetailsSearch.do?_method=list&sortOrder=visit_reg_date&sortReverse=true&country=&"
          + "patient_state=&patient_city=&_mysearch=nosearch&mr_no=&mr_no%40op=ilike&_status=&"
          + "_reg_date=&_reg_time=&_reg_date=&_reg_time=&visit_type=i&exclude_in_qb_finalized=&"
          + "discharge_finalized_user=&discharge_finalized_date=&discharge_finalized_date=&"
          + "discharge_finalized_date%40op=ge%2Cle&_search_name=&_actionId=&visit_reg_date=&"
          + "visit_reg_date=&discharge_date%40cast=y&discharge_date=${td}&discharge_date=${td}&"
          + "discharge_date%40op=le,ge&discharge_date%40type=date&discharge_flag=D"),

  /** The todays admissions. */
  TODAYS_ADMISSIONS("TODAYS_ADMISSIONS",
      "visit_details_search",
      "Today's Admissions (IP)",
      "SELECT COALESCE(count(*),0) AS COUNT FROM  patient_registration LEFT JOIN "
          + " discharge_type_master dtm USING(discharge_type_id) WHERE visit_type='i' AND "
          + " coalesce(dtm.discharge_type, '') != 'Admission Cancelled' AND reg_date::date "
          + " BETWEEN current_date AND current_date",
      "/VisitDetailsSearch.do?_method=list&_searchMethod=list&sortOrder=visit_reg_date&"
          + "sortReverse=true&country=&patient_state=&patient_city=&alloc_ward_no=&"
          + "alloc_ward_no%40op=ico&alloc_bed_no=&alloc_bed_no%40op=ico&reg_ward_no=&"
          + "reg_ward_no%40op=ico&_mysearch=nosearch&mr_no=&mr_no%40op=ilike&_status=&"
          + "_reg_date%40cast=y&_reg_date=${td}&_reg_time=&_reg_date=${td}&_reg_time=&"
          + "visit_type=i&dept_id=&doctor=&_ward_name=&_bed_name=&_country=&_patientstate=&"
          + "_patientcity=&patient_area=&exclude_in_qb_finalized=&discharge_finalized_user=&"
          + "discharge_finalized_date=&discharge_finalized_date=&discharge_finalized_date%40op="
          + "ge%2Cle&_customRegFieldName=&_regFieldName=&_customRegFieldValue=&"
          + "_hiddenRegFieldValue=&_regFieldValue=&_search_name=&_actionId=visit_details_search&"
          + "visit_reg_date%40cast=y&visit_reg_date=${td}&visit_reg_date=${td}&"
          + "visit_reg_date%40type=date&visit_reg_date%40op=ge%2Cle"),

  /** The todays op registrations. */
  TODAYS_OP_REGISTRATIONS("TODAYS_OP_REGISTRATIONS",
      "visit_details_search",
      "Today's OP Registrations",
      "SELECT COALESCE(count(*),0) AS COUNT FROM  patient_registration  WHERE visit_type='o' AND "
          + " reg_date::date BETWEEN current_date AND current_date ",
      "/VisitDetailsSearch.do?_method=list&sortOrder=visit_reg_date&sortReverse=true&country=&"
          + "patient_state=&patient_city=&_mysearch=nosearch&mr_no=&mr_no%40op=ilike&_status=&"
          + "_reg_date%40cast=y&_reg_date=${td}&_reg_time=&_reg_date=${td}&_reg_time=&"
          + "visit_type=o&exclude_in_qb_finalized=&discharge_finalized_user=&"
          + "discharge_finalized_date=&discharge_finalized_date=&"
          + "discharge_finalized_date%40op=ge%2Cle&_search_name=&_actionId=&"
          + "visit_reg_date%40cast=y&visit_reg_date=${td}&visit_reg_date="
          + "${td}&visit_reg_date%40op=ge,le"),

  /** The pending lab tests. */
  PENDING_LAB_TESTS("PENDING_LAB_TESTS",
      "lab_unfinished_tests",
      "Pending Lab Tests",
      "SELECT count(*) FROM diag_schedules_summary_view   WHERE conducted in ('N', 'P') "
          + " AND (category::text = 'DEP_LAB')",
      "/Laboratory/unfinishedTests.do?_method=unfinishedTestsList&sortOrder=pres_date&"
          + "sortReverse=true&conducted=N&conducted=P"),

  /** The pending radiology tests. */
  PENDING_RADIOLOGY_TESTS("PENDING_RADIOLOGY_TESTS",
      "rad_unfinished_tests",
      "Pending Radiology Tests",
      " SELECT count(*) FROM diag_schedules_summary_view   WHERE conducted in ('N', 'P') "
          + " AND (category::text = 'DEP_RAD')",
      "/Radiology/unfinishedTests.do?_method=unfinishedTestsList&sortOrder=pres_date&"
          + "sortReverse=true&conducted=N&conducted=P"),

  /** The pending services. */
  PENDING_SERVICES("PENDING_SERVICES",
      "service_order",
      "Pending Services",
      "SELECT count(prescription_id) AS COUNT  FROM all_services_ordered_view "
          + " WHERE conducted in ('N', 'P') and signed_off=false ",
      "/Service/Services.do?_method=pendingList&_searchMethod=pendingList&_mysearch=nosearch&"
          + "mr_no=&pres_date=&pres_date=&pres_date%40op=ge%2Cle&pres_date%40type=date&"
          + "service_department=&service_name=&service_name%40op=ico&visit_type=&_search_name=&"
          + "_actionId=pending_services"),

  /** The todays pending surgery. */
  TODAYS_PENDING_SURGERY("TODAYS_PENDING_SURGERY",
      "operations_pending_list",
      "Today's Pending Surgeries",
      "SELECT COALESCE(count(*),0) AS COUNT FROM bed_operation_schedule op WHERE "
          + " start_datetime::date BETWEEN current_date AND current_date AND status in ('N', 'P')",
      "/otservices/PendingOperations.do?_method=pendingList&pageNum=&_searchMethod=pendingList&"
          + "_mysearch=nosearch&mr_no=&mr_no%40op=ilike&start_datetime%40cast=y&start_datetime="
          + "${td}&start_datetime=${td}&start_datetime%40op=ge%2Cle&start_datetime%40type=date&"
          + "dept_id=&operation=&operation%40op=ilike&_search_name=&"
          + "_actionId=operations_pending_list"),

  /** The pending bed allocation. */
  PENDING_BED_ALLOCATION("PENDING_BED_ALLOCATION",
      "adt",
      "Pending Bed allocation",
      "SELECT COALESCE(COUNT(*),0) AS COUNT FROM  patient_visit_details_ext_view pvd LEFT JOIN "
          + " admission a USING (patient_id) WHERE pvd.bed_name IS NULL AND "
          + " pvd.visit_type_name = 'IP' and  pvd.status='A'",
      "/pages/ipservices/Ipservices.do?_method=getADTScreen&bed_name=Allocate+Bed"),

  /** The expired items. */
  EXPIRED_ITEMS("EXPIRED_ITEMS",
      "ph_indvl_stock_builder",
      "Expired Item Batches",
      "SELECT COALESCE(COUNT(*),0) AS COUNT FROM ( SELECT pmsd.medicine_id FROM "
          + " store_stock_details pmsd JOIN store_item_batch_details USING(item_batch_id) WHERE "
          + " qty>0 AND exp_dt::DATE < current_date ) AS foo",
      "/stores/IndvlStockReportBuilder.do?screenId=ph_indvl_stock_builder&reportName="
          + "Detailed+Stock+Report&method=getReport&outputMode=pdf&_searchMethod=getScreen&"
          + "_parent_report_name=Detailed+Stock+Report+Builder&selDateRange=cstm&srjsFile=null&"
          + "reptDescFile=IndividualStock.srxml&_myreport=nosearch&reportType=list&"
          + "dateFieldSelection=exp_dt&_sel=on&fromDate=&toDate=${td}&_report_name=&"
          + "_actionId=ph_indvl_stock_builder&&listFields=_sl&listFields=dept_name&"
          + "listFields=item_short_name&listFields=batch_no&listFields=category_name&"
          + "listFields=exp_dt&listFields=qty&listFields=reorder_level&listFields=mrp&"
          + "listFields=package_cp&baseFontSize=10&listGroups=item_short_name&listGroups=&"
          + "listGroups=&sumGroupHoriz=&sumGroupVert=&sumGroupVertSub=&trendType=month&"
          + "trendGroupVert=&trendGroupVertSub=&filter.1=qty&filterOp.1=gt&filterVal.1=0&"
          + "pdfcstm_option=grpn_needed&grpn_needed=true"),

  /** The monthwise collection. */
  MONTHWISE_COLLECTION("MONTHWISE_COLLECTION",
      "rep_collection_detailed_builder",
      "Collection (This Month)",
      "SELECT COALESCE (SUM(amount),0) AS COUNT FROM ( SELECT amount FROM receipts r JOIN "
          + " counters c ON (r.counter = c.counter_id) WHERE c.collection_counter='Y' AND "
          + " date(display_date) BETWEEN ${tm} AND current_date ) "
          + " as query",
      "/billing/CollectionReports.do?screenId=rep_collection_detailed_builder&reportName="
          + "Collections+Report&method=getReport&outputMode=pdf&_searchMethod=getScreen&"
          + "_parent_report_name=Collections+Report+Builder&selDateRange=cstm&srjsFile=null&"
          + "reptDescFile=CollectionDetails.srjs&_myreport=nosearch&reportType=list&"
          + "dateFieldSelection=receipt_date&_sel=on&fromDate=${tm}&toDate=${td}&listFields="
          + "payment_mode_name&listFields=main_type&listFields=detailed_type&listFields="
          + "receipt_id&listFields=receipt_date&listFields=counter&listFields=remarks&"
          + "listFields=amt&listGroups=&listGroups=&listGroups=&sumGroupHoriz=&sumGroupVert=&"
          + "sumGroupVertSub=&trendType=month&trendGroupVert=&trendGroupVertSub=&filter.1="
          + "collection_counter&filterOp.1=eq&filterVal.1=Yes&baseFontSize=10&_report_name=&"
          + "_actionId=rep_collection_detailed_builder"),

  /** The fin yearwise collection. */
  FIN_YEARWISE_COLLECTION("FIN_YEARWISE_COLLECTION",
      "rep_collection_detailed_builder",
      "Collection (Fin. Yr.)",
      "SELECT COALESCE (SUM(amount),0) AS COUNT FROM ( SELECT amount FROM receipts r JOIN "
          + " counters c ON (r.counter = c.counter_id) WHERE c.collection_counter='Y' AND "
          + " date(display_date) BETWEEN ${fy} AND current_date ) "
          + " as query",
      "/billing/CollectionReports.do?screenId=rep_collection_detailed_builder&reportName="
          + "Collections+Report&method=getReport&outputMode=pdf&_searchMethod=getScreen&"
          + "_parent_report_name=Collections+Report+Builder&selDateRange=cstm&srjsFile=null&"
          + "reptDescFile=CollectionDetails.srjs&_myreport=nosearch&reportType=list&"
          + "dateFieldSelection=receipt_date&_sel=on&fromDate=${fy}&toDate=${td}&listFields="
          + "payment_mode_name&listFields=main_type&listFields=detailed_type&listFields=receipt_id"
          + "&listFields=receipt_date&listFields=counter&listFields=remarks&listFields=amt&"
          + "listGroups=&listGroups=&listGroups=&sumGroupHoriz=&sumGroupVert=&sumGroupVertSub=&"
          + "trendType=month&trendGroupVert=&trendGroupVertSub=&filter.1=collection_counter&"
          + "filterOp.1=eq&filterVal.1=Yes&baseFontSize=10&_report_name=&"
          + "_actionId=rep_collection_detailed_builder"),

  /** The monthwise pharmacy coll. */
  MONTHWISE_PHARMACY_COLL("MONTHWISE_PHARMACY_COLL",
      "rep_collection_detailed_builder",
      "Pharmacy Collection (This Month)",
      "SELECT COALESCE (SUM(amount),0) AS COUNT FROM ( SELECT amount FROM receipts r JOIN "
          + " counters c ON (r.counter = c.counter_id) WHERE c.collection_counter='Y' AND "
          + " counter_type='P' AND date(display_date) BETWEEN ${tm} AND current_date  ) as query",
      "/billing/CollectionReports.do?screenId=rep_collection_detailed_builder&reportName="
          + "Collections+Report&method=getReport&outputMode=pdf&_searchMethod=getScreen&"
          + "_parent_report_name=Collections+Report+Builder&selDateRange=cstm&srjsFile=null&"
          + "reptDescFile=CollectionDetails.srjs&_myreport=nosearch&reportType=list&"
          + "dateFieldSelection=receipt_date&_sel=on&fromDate=${tm}&toDate=${td}&listFields="
          + "payment_mode_name&listFields=main_type&listFields=detailed_type&listFields="
          + "receipt_id&listFields=receipt_date&listFields=counter&listFields=remarks&listFields="
          + "amt&baseFontSize=10&userNameNeeded=Y&listGroups=&listGroups=&listGroups=&"
          + "sumGroupHoriz=&sumGroupVert=&sumGroupVertSub=&trendType=month&trendGroupVert=&"
          + "trendGroupVertSub=&customOrder1=&customOrder2=&filter.1=counter_type&filterOp.1=eq&"
          + "filterVal.1=Pharmacy&filter.2=collection_counter&filterOp.2=eq&filterVal.2=Yes&"
          + "_report_name=&_actionId=rep_collection_detailed_builder"),

  /** The yearwise pharmacy coll. */
  YEARWISE_PHARMACY_COLL("YEARWISE_PHARMACY_COLL",
      "rep_collection_detailed_builder",
      "Pharmacy Collection (Fin. Yr.)",
      "SELECT COALESCE (SUM(amount),0) AS COUNT FROM ( SELECT amount FROM receipts r JOIN "
          + " counters c ON (r.counter = c.counter_id) WHERE c.collection_counter='Y' AND "
          + " counter_type='P' AND date(display_date) BETWEEN ${fy} AND current_date ) as query",
      "/billing/CollectionReports.do?screenId=rep_collection_detailed_builder&reportName="
          + "Collections+Report&method=getReport&outputMode=pdf&_searchMethod=getScreen&"
          + "_parent_report_name=Collections+Report+Builder&selDateRange=cstm&srjsFile=null&"
          + "reptDescFile=CollectionDetails.srjs&_myreport=nosearch&reportType=list&"
          + "dateFieldSelection=receipt_date&_sel=on&fromDate=${fy}&toDate=${td}&listFields="
          + "payment_mode_name&listFields=main_type&listFields=detailed_type&listFields="
          + "receipt_id&listFields=receipt_date&listFields=counter&listFields=remarks&"
          + "listFields=amt&baseFontSize=10&userNameNeeded=Y&listGroups=&listGroups=&listGroups=&"
          + "sumGroupHoriz=&sumGroupVert=&sumGroupVertSub=&trendType=month&trendGroupVert=&"
          + "trendGroupVertSub=&customOrder1=&customOrder2=&filter.1=counter_type&filterOp.1=eq&"
          + "filterVal.1=Pharmacy&filter.2=collection_counter&filterOp.2=eq&filterVal.2=Yes&"
          + "_report_name=&_actionId=rep_collection_detailed_builder"),


  /** The monthwise purchases. */
  MONTHWISE_PURCHASES("MONTHWISE_PURCHASES",
      "ph_purchase_rep_builder",
      "Purchases (This Month)",
      " SELECT TRUNC(COALESCE(SUM(g.billed_qty/g.grn_pkg_size*g.cost_price - g.discount+ "
          + " i.round_off + g.tax + g.item_ced + i.other_charges),0), 2) AS COUNT "
          + " FROM store_grn_details g JOIN store_grn_main gm USING (grn_no) JOIN "
          + " store_invoice i USING (supplier_invoice_id) WHERE gm.grn_date::DATE "
          + " BETWEEN ${tm} AND current_date ",
      "/stores/PurchaseInvoiceReportBuilder.do?screenId=ph_invoice_rep_builder&reportName="
          + "Purchase+Invoice+Report&method=getReport&outputMode=pdf&_searchMethod=getScreen&"
          + "_parent_report_name=Purchase+Invoice+Report+Builder&selDateRange=cstm&srjsFile=null&"
          + "reptDescFile=PharmacyInvoice.srjs&reportGroup=Procurement+Reports&current_user="
          + "ArunTest&print_title=Purchase+Invoice+Report&_myreport=nosearch&reportType=list&"
          + "dateFieldSelection=grn_date&_sel=on&fromDate=${tm}&toDate=${td}&listFields="
          + "store_name&listFields=supplier_name&listFields=invoice_no&listFields=invoice_date&"
          + "listFields=grn_nos&listFields=received_debit_amt&listFields=raised_amt&listFields="
          + "difference_amt&listFields=invoice_amount&baseFontSize=10&listGroups=&listGroups=&"
          + "listGroups=&sumGroupHoriz=&sumGroupVert=&sumGroupVertSub=&trendType=month&"
          + "trendGroupVert=&trendGroupVertSub=&vtrendGroupHoriz=&vtrendType=month&"
          + "vtrendGroupVertSub=&customOrder1=&customOrder2=&filter.1=purchase_type&filterOp.1=eq&"
          + "filterVal.1=Purchase&_report_name=&_actionId=ph_invoice_rep_builder&pdfcstm_option="
          + "un_needed&userNameNeeded=Y&pdfcstm_option=dt_needed&dt_needed=true&pdfcstm_option="
          + "hsp_needed&hsp_needed=true&hsp_needed_h=false&pdfcstm_option=pgn_needed&"
          + "pgn_needed=true&grpn_needed=false&pdfcstm_option=filterDesc_needed&"
          + "filterDesc_needed=true"),

  /** The yearwise purchases. */
  YEARWISE_PURCHASES("YEARWISE_PURCHASES",
      "ph_purchase_rep_builder",
      "Purchases (Fin. Yr.)",
      " SELECT TRUNC(COALESCE(SUM(g.billed_qty/g.grn_pkg_size*g.cost_price - g.discount+ "
          + " i.round_off + g.tax + g.item_ced+ i.other_charges),0), 2) AS COUNT "
          + " FROM store_grn_details g JOIN store_grn_main gm USING (grn_no) JOIN "
          + " store_invoice i USING (supplier_invoice_id) WHERE gm.grn_date::DATE "
          + " BETWEEN ${fy} AND current_date ",
      "/stores/PurchaseInvoiceReportBuilder.do?screenId=ph_invoice_rep_builder&reportName="
          + "Purchase+Invoice+Report&method=getReport&outputMode=pdf&_searchMethod=getScreen&"
          + "_parent_report_name=Purchase+Invoice+Report+Builder&selDateRange=cstm&srjsFile=null&"
          + "reptDescFile=PharmacyInvoice.srjs&reportGroup=Procurement+Reports&current_user="
          + "ArunTest&print_title=Purchase+Invoice+Report&_myreport=nosearch&reportType=list&"
          + "dateFieldSelection=grn_date&_sel=on&fromDate=${fy}&toDate=${td}&listFields="
          + "store_name&listFields=supplier_name&listFields=invoice_no&listFields=invoice_date&"
          + "listFields=grn_nos&listFields=received_debit_amt&listFields=raised_amt&listFields="
          + "difference_amt&listFields=invoice_amount&baseFontSize=10&listGroups=&listGroups=&"
          + "listGroups=&sumGroupHoriz=&sumGroupVert=&sumGroupVertSub=&trendType=month&"
          + "trendGroupVert=&trendGroupVertSub=&vtrendGroupHoriz=&vtrendType=month&"
          + "vtrendGroupVertSub=&customOrder1=&customOrder2=&filter.1=purchase_type&filterOp.1=eq&"
          + "filterVal.1=Purchase&_report_name=&_actionId=ph_invoice_rep_builder&pdfcstm_option="
          + "un_needed&userNameNeeded=Y&pdfcstm_option=dt_needed&dt_needed=true&pdfcstm_option="
          + "hsp_needed&hsp_needed=true&hsp_needed_h=false&pdfcstm_option=pgn_needed&pgn_needed="
          + "true&grpn_needed=false&pdfcstm_option=filterDesc_needed&filterDesc_needed=true"),

  /** The open bill later. */
  OPEN_BILL_LATER("OPEN_BILL_LATER",
      "search_bills",
      "Open Bill Later bills",
      "SELECT COALESCE(count(*),0) AS COUNT from bill WHERE  STATUS ='A' AND bill_type='C'",
      "/pages/BillDischarge/BillList.do?_method=getBills&_searchMethod=getBills&"
          + "sortOrder=open_date&sortReverse=true&_mysearch=nosearch&mr_no=&mr_no%40op=ilike&"
          + "bill_no=&bill_no%40op=ilike&open_date=&open_date=&open_date%40op=ge%2Cle&"
          + "finalized_date=&finalized_date%40type=date&finalized_date%40op=ge&finalized_date=&"
          + "finalized_date%40op=le&bill_type=C&restriction_type=&status=A&visit_type=&"
          + "_search_name=&_actionId=search_bills"),

  /** The todays appointments. */
  TODAYS_APPOINTMENTS("TODAYS_APPOINTMENTS",
      "today_resource_scheduler",
      "Today's Pending Appointments",
      "SELECT COALESCE(count(*),0) AS COUNT FROM  scheduler_appointments sch WHERE "
          + " date(appointment_time)=current_date AND appointment_status IN('Booked','Confirmed')",
      "/pages/resourcescheduler/todaysappointments.do?_method=getTodaysPatientAppointments&amp;"
          + "startTime=today&amp;resFilter=ALL&amp;appoint_status=Booked&amp;"
          + "appoint_status=Confirmed"),

  /** The open pharmacy credit bills. */
  OPEN_PHARMACY_CREDIT_BILLS("OPEN_PHARMACY_CREDIT_BILLS",
      "search_bills",
      "Open Pharmacy Credit Bills ",
      "SELECT COALESCE(count(*),0) as COUNT from bill where restriction_type = 'P' "
          + " AND bill_type = 'C' AND status = 'A' ",
      "/pages/BillDischarge/BillList.do?_method=getBills&_searchMethod=getBills&"
          + "sortOrder=open_date&sortReverse=true&_mysearch=nosearch&mr_no=&mr_no%40op=ilike&"
          + "bill_no=&bill_no%40op=ilike&open_date=&open_date=&open_date%40op=ge%2Cle&"
          + "finalized_date=&finalized_date%40type=date&finalized_date%40op=ge&finalized_date=&"
          + "finalized_date%40op=le&bill_type=C&restriction_type=P&status=A&visit_type=&"
          + "_search_name=&_actionId=search_bills"),

  /** The todays lab tests. */
  TODAYS_LAB_TESTS("TODAYS_LAB_TESTS",
      "lab_diag_report_list",
      "Today's Lab Tests",
      "SELECT COALESCE(COUNT(*),0) AS count FROM tests_prescribed tp JOIN diagnostics diag ON "
          + " (tp.test_id=diag.test_id) JOIN diagnostics_departments ddep ON "
          + " (ddep.ddept_id = diag.ddept_id) WHERE pres_date::date BETWEEN current_date AND "
          + " current_date AND conducted!='X' AND category='DEP_LAB' ",
      "/pages/labTests.do?screenId=diag_builder&reportName=Diagnostics+Report&method=getReport&"
          + "outputMode=pdf&_searchMethod=getScreen&_parent_report_name=Diagnostics+Report+Builder"
          + "&selDateRange=td&srjsFile=null&reptDescFile=Diagnostics.srxml&reportGroup="
          + "Diagnostics+Reports&current_user=InstaAdmin&print_title=Today's+Lab+Tests&"
          + "_myreport=nosearch&reportType=list&dateFieldSelection=pres_date&_sel=td&"
          + "fromDate=16-02-2011&toDate=16-02-2011&listFields=dept_category&listFields=cust_id&"
          + "listFields=test_name&listFields=patient_name&listFields=pres_date&listFields="
          + "presc_dr&listFields=conducted&listFields=labno&listFields=conducted_date&"
          + "listFields=sample_type&baseFontSize=10&listGroups=conducted&listGroups=&listGroups=&"
          + "sumGroupHoriz=&sumGroupVert=&sumGroupVertSub=&trendType=month&trendGroupVert=&"
          + "trendGroupVertSub=&customOrder1=&customOrder2=&filter.1=dept_category&filterOp.1=eq&"
          + "filterVal.1=Lab&filter.2=conducted&filterOp.2=ne&filterVal.2=Cancelled&_report_name=&"
          + "_actionId=diag_builder&pdfcstm_option=un_needed&userNameNeeded=Y&"
          + "pdfcstm_option=dt_needed&dt_needed=true&pdfcstm_option=hsp_needed&hsp_needed=true&"
          + "pdfcstm_option=pgn_needed&pgn_needed=true&grpn_needed=false"),

  /** The todays rad tests. */
  TODAYS_RAD_TESTS("TODAYS_RAD_TESTS",
      "radio_diag_report_list",
      "Today's Radiology Tests",
      "SELECT COALESCE(COUNT(*),0) AS count FROM tests_prescribed tp JOIN diagnostics diag ON "
          + " (tp.test_id=diag.test_id) JOIN diagnostics_departments ddep ON "
          + " (ddep.ddept_id = diag.ddept_id) WHERE pres_date::date BETWEEN current_date AND "
          + " current_date AND conducted!='X' AND category='DEP_RAD' ",
      "/pages/labTests.do?screenId=diag_builder&reportName=Diagnostics+Report&method=getReport&"
          + "outputMode=pdf&_searchMethod=getScreen&_parent_report_name="
          + "Diagnostics+Report+Builder&selDateRange=td&srjsFile=null&reptDescFile="
          + "Diagnostics.srxml&reportGroup=Diagnostics+Reports&current_user=InstaAdmin&"
          + "print_title=Today's+Radiology+Tests&_myreport=nosearch&reportType=list&"
          + "dateFieldSelection=pres_date&_sel=td&fromDate=16-02-2011&toDate=16-02-2011&"
          + "listFields=dept_category&listFields=cust_id&listFields=test_name&listFields="
          + "patient_name&listFields=pres_date&listFields=presc_dr&listFields=conducted&"
          + "listFields=labno&listFields=conducted_date&listFields=sample_type&baseFontSize=10&"
          + "listGroups=conducted&listGroups=&listGroups=&sumGroupHoriz=&sumGroupVert=&"
          + "sumGroupVertSub=&trendType=month&trendGroupVert=&trendGroupVertSub=&customOrder1=&"
          + "customOrder2=&filter.1=dept_category&filterOp.1=eq&filterVal.1=Radiology&filter.2="
          + "conducted&filterOp.2=ne&filterVal.2=Cancelled&_report_name=&_actionId=diag_builder&"
          + "pdfcstm_option=un_needed&userNameNeeded=Y&pdfcstm_option=dt_needed&dt_needed=true&"
          + "pdfcstm_option=hsp_needed&hsp_needed=true&pdfcstm_option=pgn_needed&pgn_needed=true&"
          + "grpn_needed=false"),

  /** The todays services. */
  TODAYS_SERVICES("TODAYS_SERVICES",
      "serice_report",
      "Today's Services Conducted",
      "SELECT COUNT(*) AS COUNT  FROM all_services_ordered_view WHERE conducted IN ('P', 'C') "
      + " AND (conducteddate::date >= current_date) AND (conducteddate::date <= current_date) ",
      "/Service/ConductedServices.do?_method=conductedList&_searchMethod=conductedList&"
          + "_mysearch=nosearch&mr_no=&pres_date=&pres_date=&pres_date%40op=ge%2Cle&"
          + "pres_date%40type=date&service_department=&service_name=&service_name%40op=ico&"
          + "visit_type=&signed_off=&signed_off%40type=boolean&_search_name=&"
          + "_actionId=conducted_services&conducteddate%40cast=y&conducteddate=${td}&"
          + "conducteddate=${td}&conducteddate%40op=ge%2Cle&conducteddate%40type=date"),

  /** The todays meals tobe delivered. */
  TODAYS_MEALS_TOBE_DELIVERED("TODAYS_MEALS_TOBE_DELIVERED",
      "grp_canteen",
      "Today's Meals To be Delivered",
      "SELECT  COALESCE(count(*),0) AS COUNT FROM diet_prescribed dp  LEFT JOIN diet_master dm ON "
          + " (dm.diet_id = dp.diet_id) JOIN patient_registration pr ON "
          + " (pr.patient_id = dp.visit_id) LEFT JOIN ip_bed_details ibd ON "
          + " (ibd.patient_id = pr.patient_id AND ibd.status IN ('A','C')) LEFT JOIN bed_names bn "
          + " ON (bn.bed_id = ibd.bed_id) LEFT JOIN ward_names wn ON (wn.ward_no = bn.ward_no) "
          + " LEFT JOIN patient_diet_prescriptions pdp ON (dp.diet_pres_id = pdp.diet_pres_id) "
          + " JOIN patient_details pd ON (pd.mr_no = pr.mr_no)  WHERE "
          + " (dp.meal_date::DATE = current_date) AND (dp.status = 'N')",
      "/dietary/Canteen.do?_method=getMealsToBeDelivered"),

  /** The indents awaiting approval. */
  INDENTS_AWAITING_APPROVAL("INDENTS_AWAITING_APPROVAL",
      "stores_indent_approval",
      "Indents Awaiting Approval",
      "SELECT COALESCE(COUNT(indent_no),0) AS COUNT FROM store_indent_main pim  WHERE "
          + " (status::text = 'O')",
      "/stores/StoresIndentApproval.do?_method=list&status=O&sortOrder=indent_no&"
          + "sortReverse=true"),

  /** The user indents awaiting approval. */
  USER_INDENTS_AWAITING_APPROVAL("USER_INDENTS_AWAITING_APPROVAL",
      "stores_user_indent_approval",
      "User Indents Awaiting Approval",
      "SELECT COALESCE(COUNT(indent_no),0) AS COUNT FROM store_indent_main pim  WHERE "
          + " (status::text = 'O')",
      "/stores/StoresUserIndentApproval.do?_method=list&status=O&sortOrder=indent_no&"
          + "sortReverse=true"),

  /** The pending indents. */
  PENDING_INDENTS("PENDING_INDENTS",
      "stores_process_indent",
      "Pending Indents",
      "SELECT COALESCE(count(indent_no),0) AS COUNT FROM store_indent_main pim  WHERE "
          + " (status::text = 'A')",
      "/stores/StoresIndentProcess.do?_method=list&status=A&sortOrder=indent_no&sortReverse=true"),

  /** The active insurance patients. */
  ACTIVE_INSURANCE_PATIENTS("ACTIVE_INSURANCE_PATIENTS",
      "visit_details_search",
      "Active Insurance Patients",
      "SELECT COALESCE(COUNT(mr_no),0) AS COUNT FROM all_visits_view WHERE (status::text = 'A') "
          + " AND primary_sponsor_id IS NOT NULL",
      "/VisitDetailsSearch.do?_method=list&_searchMethod=list&sortOrder=visit_reg_date&"
          + "sortReverse=true&country=&patient_state=&patient_city=&_mysearch=nosearch&mr_no=&"
          + "mr_no%40op=ilike&status=A&_reg_date=&_reg_time=&_reg_date=&_reg_time=&visit_type=&"
          + "exclude_in_qb_finalized=&discharge_finalized_user=&discharge_finalized_date=&"
          + "discharge_finalized_date=&discharge_finalized_date%40op=ge%2Cle&_search_name=&"
          + "_actionId=visit_details_search&visit_reg_date=&visit_reg_date=&"
          + "visit_reg_date%40type=date&visit_reg_date%40op=ge%2Cle&primary_sponsor_id=n&"
          + "primary_sponsor_id%40op=null"),

  /** The active insurance bills. */
  ACTIVE_INSURANCE_BILLS("ACTIVE_INSURANCE_BILLS",
      "search_bills",
      "Active Insurance Bills",
      " SELECT COALESCE(count(bill_no),0) AS COUNT FROM bill b LEFT JOIN patient_registration pr "
          + " on (b.visit_id = pr.patient_id) WHERE b.is_tpa AND pr.status='A' AND "
          + " pr.primary_sponsor_id IS NOT NULL AND pr.primary_sponsor_id != '' ",
      "/pages/BillDischarge/BillList.do?title=Open+Bills&visit_status=A&sortOrder=open_date&"
          + "is_tpa=true&is_tpa%40cast=y&_method=getBills&sortReverse=true&pageNum=1"),

  /** The todays supplier returns. */
  TODAYS_SUPPLIER_RETURNS("TODAYS_SUPPLIER_RETURNS",
      "pharma_view_supp_returns",
      "Today's Supplier Returns",
      " SELECT COALESCE(count(RETURN_NO),0) AS COUNT FROM (SELECT RETURN_NO,S.SUPPLIER_NAME,"
          + "PSRM.DATE_TIME AS RETURN_DATE, USER_NAME,case when orig_return_no is null then 'O' "
          + " else 'E' end as txn_type, RETURN_TYPE,PSRM.STATUS,ORIG_RETURN_NO,supplier_id,"
          + "psrm.store_id FROM STORE_SUPPLIER_RETURNS_MAIN PSRM JOIN SUPPLIER_MASTER S ON "
          + " SUPPLIER_CODE=PSRM.SUPPLIER_ID) as FOO WHERE return_date::date BETWEEN current_date "
          + " AND current_date",
      "/stores/StoresSupplierReturns.do?_method=getSupplierReturns&_searchMethod="
          + "getSupplierReturns&sortOrder=return_no&sortReverse=true&_mysearch=nosearch&"
          + "return_no=&supplier_name=&store_id=&store_id%40type=integer&return_date%40cast=y&"
          + "return_date=${td}&return_date=${td}&return_date%40op=ge%2Cle&txn_type=&return_type=&"
          + "status=&_search_name=&_actionId=pharma_view_supp_returns"),

  /** The todays reopened bills. */
  TODAYS_REOPENED_BILLS("TODAYS_REOPENED_BILLS",
      "rep_bill_status_change",
      "Today's Reopened Bills",
      " SELECT count(*) FROM bill b JOIN bill_audit_log al ON (b.bill_no = al.bill_no) WHERE "
          + " al.operation ='UPDATE' AND al.field_name ='status' AND al.old_value != 'A' AND "
          + " al.new_value = 'A' AND date(al.mod_time) = current_date ",
      "/pages/BillStatusChangeStdReport.do?print_title=Bills+Status+Change+Report&"
          + "method=getReport&outputMode=pdf&reportType=list&dateFieldSelection=mod_date&"
          + "selDateRange=td&listFields=bill_no&listFields=bill_patient_full_name&listFields="
          + "bill_old_status&listFields=bill_new_status&listFields=user_name&listFields=mod_time&"
          + "listFields=total_amount&baseFontSize=10&filter.1=bill_new_status&filterOp.1=eq&"
          + "filterVal.1=Open&filter.2=bill_old_status&filterOp.2=ne&filterVal.2=Open&"
          + "customOrder1=bill_no&sort1=DESC&baseFontSize=10&userNameNeeded=Y&"
          + "filterDesc_needed=true"),

  /** The todays bills discounted. */
  TODAYS_BILLS_DISCOUNTED("TODAYS_BILLS_DISCOUNTED",
      "rep_bill_builder",
      "Today's Bills Discounted",
      "SELECT COALESCE(count(*),0) AS COUNT FROM bill WHERE total_discount>0 AND "
          + " finalized_date::date BETWEEN current_date AND current_date AND status IN ('F','C') ",
      "/billing/BillStdReport.do?print_title=Bills+Discounted+Today&method=getReport&"
          + "outputMode=pdf&reportType=list&dateFieldSelection=finalized_date&selDateRange=td&"
          + "listFields=mr_no&listFields=cust_id&listFields=bill_patient_full_name&"
          + "listFields=bill_no&listFields=open_date&listFields=total_amount&listFields="
          + "insurance_deduction&listFields=deposit_set_off&listFields=total_receipts&"
          + "listFields=due_amount&listFields=total_discount&baseFontSize=10&userNameNeeded=Y&"
          + "listGroups=dept_name&customOrder1=total_discount&sort1=DESC&filter.1=total_discount&"
          + "filterOp.1=gt&filterVal.1=0&filter.2=bill_status&filterOp.2=in&"
          + "filterVal.2=Finalized%2CClosed"),

  /** The todays purchase orders. */
  TODAYS_PURCHASE_ORDERS("TODAYS_PURCHASE_ORDERS",
      "stores_view_po",
      "Today's Purchase Order's",
      "SELECT COALESCE(count(PO_NO),0) AS COUNT FROM ( SELECT POM.PO_NO,POM.PO_DATE,POM.STATUS, "
          + " POM.QUT_NO, ( SELECT COUNT(*) FROM store_grn_main WHERE PO_NO=POM.PO_NO) AS "
          + " grn_count, POM.PO_TOTAL,SM.SUPPLIER_NAME,POM.SUPPLIER_ID, pom.store_id FROM "
          + " store_po_main POM JOIN SUPPLIER_MASTER SM ON SM.SUPPLIER_CODE=POM.SUPPLIER_ID "
          + " ) AS FOO WHERE PO_DATE::DATE BETWEEN current_date AND current_date",
      "/pages/stores/viewpo.do?_method=getPOs&_searchMethod=getPOs&sortOrder=po_no&sortReverse="
          + "true&_mysearch=nosearch&po_no=&store_id=&store_id%40type=integer&po_date%40cast=y&"
          + "po_date=${td}&po_date=${td}&po_date%40op=ge%2Cle&qut_no=&qut_no%40op=like&status=&"
          + "_search_name=&_actionId=stores_view_po"),

  /** The current bed occupancy pc. */
  CURRENT_BED_OCCUPANCY_PC("CURRENT_BED_OCCUPANCY_PC",
      "adt",
      "Total Bed Occupancy Percentage",
      "SELECT ROUND(SUM (numerator)/sum(denominator)*100,2) AS COUNT FROM ( SELECT null AS "
          + " numerator, count(*) AS denominator FROM bed_names bn LEFT JOIN ward_names wn "
          + " USING (ward_no) WHERE wn.status='A' AND bn.status='A' UNION select count(*) AS "
          + " NUMERATOR , null  AS denominator FROM bed_names bn LEFT JOIN ward_names wn  USING "
          + " (ward_no) WHERE wn.status='A' AND bn.status='A' AND occupancy ='Y' ) AS foo",
      "/pages/ipservices/Ipservices.do?_method=getADTScreen&bed_name=Allocate+Bed&"
          + "bed_name%40op=ne"),

  /** The current icu bed occupancy pc. */
  CURRENT_ICU_BED_OCCUPANCY_PC("CURRENT_ICU_BED_OCCUPANCY_PC",
      "adt",
      "ICU Bed Occupancy Percentage",
      " SELECT CASE WHEN sum(denominator) = 0 THEN 0 ELSE "
          + " round(sum(numerator)/sum(denominator)*100,2) END AS COUNT FROM ( SELECT null AS "
          + " denominator, count(*) AS numerator FROM bed_names bn LEFT JOIN ward_names wn "
          + " USING (ward_no) WHERE bn.bed_type IN (SELECT intensive_bed_type FROM "
          + " icu_bed_charges ) AND occupancy ='Y'  AND bn.status ='A' AND wn.status='A' "
          + " UNION SELECT count(*) AS denominator, null FROM bed_names bn LEFT JOIN ward_names "
          + " wn  USING (ward_no) WHERE bn.bed_type in (SELECT intensive_bed_type FROM "
          + " icu_bed_charges ) AND bn.status ='A' AND wn.status='A') AS foo",
      "/pages/ipservices/Ipservices.do?_method=getADTScreen&is_icu_type=Y"),

  /** The total mlc count. */
  TOTAL_MLC_COUNT("TOTAL_MLC_COUNT",
      "visit_details_search",
      "Total MLC Count",
      "SELECT COALESCE (count(*),0) AS COUNT FROM patient_registration WHERE mlc_status='Y'",
      "/VisitDetailsSearch.do?_method=list&_searchMethod=list&sortOrder=visit_reg_date&"
          + "sortReverse=true&mlc_status=Y"),

  /** The todays mlc count. */
  TODAYS_MLC_COUNT("TODAYS_MLC_COUNT",
      "visit_details_search",
      "Today's MLC Count",
      "SELECT COALESCE (count(*),0) AS COUNT FROM patient_registration WHERE mlc_status='Y' "
          + " AND reg_date::date BETWEEN current_date AND current_date",
      "/VisitDetailsSearch.do?_method=list&_searchMethod=list&sortOrder=visit_reg_date&"
          + "sortReverse=true&mlc_status=Y&visit_reg_date%40cast=y&visit_reg_date=${td}&"
          + "visit_reg_date=${td}&visit_reg_date%40type=date&visit_reg_date%40op=ge%2Cle&"
          + "_reg_date=${td}&_reg_date=${td}&_reg_date%40cast=y"),

  /** The todays insurance registrations. */
  TODAYS_INSURANCE_REGISTRATIONS("TODAYS_INSURANCE_REGISTRATIONS",
      "visit_details_search",
      "Today's TPA/Sponsor Regn.",
      " SELECT COALESCE(COUNT(mr_no),0) AS COUNT FROM all_visits_view WHERE primary_sponsor_id "
          + " IS NOT NULL AND visit_reg_date::DATE BETWEEN current_date AND current_date",
      "/VisitDetailsSearch.do?_method=list&_searchMethod=list&sortOrder=visit_reg_date&"
          + "sortReverse=true&primary_sponsor_id=n&primary_sponsor_id%40op=null&visit_reg_date="
          + "${td}&visit_reg_date=${td}&visit_reg_date%40type=date&visit_reg_date%40op=ge%2Cle&"
          + "_reg_date=${td}&_reg_date=${td}&visit_reg_date%40cast=y&_reg_date%40cast=y"),

  /** The pending op consultations. */
  PENDING_OP_CONSULTATIONS("PENDING_OP_CONSULTATIONS",
      "op_out_patient",
      "Pending OP Consultations",
      " SELECT COALESCE(COUNT(patient_id),0) AS COUNT FROM doctor_consultation dc JOIN "
          + " patient_registration pr USING (patient_id) WHERE dc.status in ('A','P') and "
          + "pr.status='A' and pr.visit_type='o' and coalesce(dc.cancel_status,'')='' ",
      "/outpatient/OpListAction.do?_method=list&sortReverse=false&status=A&status=P&"
          + "visit_status=A"),
  /** The vacant beds. */
  VACANT_BEDS("VACANT_BEDS",
      "bed_occu_rep_builder",
      "Vacant Beds Count",
      " SELECT count(*) FROM bed_status_report WHERE occupancy = 'N'",
      "/pages/BedOccuBuilder.do?method=getReport&reportType=list&reptDescFile="
          + "BedOccupancyBuilder.srjs&outputMode=pdf&dt_needed=true&hsp_needed=true&grpn_needed="
          + "false&userNameNeeded=Y&selDateRange=pd&screenId=bed_occu_rep_builder&current_user="
          + "InstaAdmin&listFields=_gsl&listFields=bed_name&pdfcstm_option=un_needed&"
          + "pdfcstm_option=dt_needed&pdfcstm_option=hsp_needed&pdfcstm_option=pgn_needed&"
          + "pdfcstm_option=filterDesc_needed&reportName=Current+Bed+Occupancy+Report&"
          + "pgn_needed=true&vtrendType=month&dateFieldSelection=None&baseFontSize=10&"
          + "trendType=month&srjsFile=null&hsp_needed_h=false&print_title="
          + "Current+Bed+Occupancy+Report&filter.1=bed_status&filterOp.1=eq&"
          + "filterDesc_needed=true&reportGroup=Bed+Util+Reports&filterVal.1=Vacant&"
          + "customOrder1=bed_name&listGroups=ward_name"),
  /** The todays billed amount. */
  TODAYS_BILLED_AMOUNT("TODAYS_BILLED_AMOUNT",
      "rep_bill_builder",
      "Today's Billed Amount",
      "SELECT COALESCE(SUM(b.total_amount),0) AS COUNT FROM bill b WHERE open_date::DATE BETWEEN "
          + " current_date AND current_date",
      "/billing/BillStdReport.do?screenId=rep_bill_builder&reportName=Bills+Report&method="
          + "getReport&outputMode=pdf&_searchMethod=getScreen&_parent_report_name="
          + "Bills+Report+Builder&selDateRange=cstm&srjsFile=null&reptDescFile=Bill.srjs&&"
          + "_myreport=nosearch&reportType=list&dateFieldSelection=open_date&_sel=td&fromDate="
          + "${td}&toDate=${td}&listFields=bill_no&listFields=bill_patient_full_name&listFields="
          + "bill_status&listFields=total_amount&listFields=total_receipts&baseFontSize=10&"
          + "listGroups=&listGroups=&listGroups=&sumGroupHoriz=&sumGroupVert=&sumGroupVertSub=&"
          + "trendType=month&trendGroupVert=&trendGroupVertSub=&vtrendGroupHoriz=&"
          + "vtrendType=month&vtrendGroupVertSub=&filter.1=&filterOp.1=eq&_report_name=&"
          + "_actionId=rep_bill_builder"),
  /** The monthwise billed amount. */
  MONTHWISE_BILLED_AMOUNT("MONTHWISE_BILLED_AMOUNT",
      "rep_bill_builder",
      "Billed Amount - This Month",
      "SELECT COALESCE(SUM(b.total_amount),0) AS COUNT FROM bill b WHERE open_date::DATE BETWEEN "
          + " ${tm} AND current_date ",
      "/billing/BillStdReport.do?screenId=rep_bill_builder&reportName=Bills+Report&method="
          + "getReport&outputMode=pdf&_searchMethod=getScreen&_parent_report_name="
          + "Bills+Report+Builder&selDateRange=cstm&srjsFile=null&reptDescFile=Bill.srjs&"
          + "_myreport=nosearch&reportType=list&dateFieldSelection=open_date&_sel=tm&fromDate="
          + "${tm}&toDate=${td}&listFields=bill_no&listFields=bill_patient_full_name&listFields="
          + "bill_status&listFields=total_amount&listFields=total_receipts&baseFontSize=10&"
          + "listGroups=&listGroups=&listGroups=&sumGroupHoriz=&sumGroupVert=&sumGroupVertSub=&"
          + "trendType=month&trendGroupVert=&trendGroupVertSub=&vtrendGroupHoriz=&vtrendType="
          + "month&vtrendGroupVertSub=&customOrder1=&customOrder2=&filter.1=&filterOp.1=eq&"
          + "_report_name=&_actionId=rep_bill_builder"),
  /** The yearwise billed amount. */
  YEARWISE_BILLED_AMOUNT("YEARWISE_BILLED_AMOUNT",
      "rep_bill_builder",
      "Billed Amount - Fin Year",
      "SELECT COALESCE(SUM(b.total_amount),0) AS COUNT FROM bill b WHERE open_date::DATE BETWEEN "
          + " ${fy} AND current_date",
      "/billing/BillStdReport.do?screenId=rep_bill_builder&reportName=Bills+Report&method="
          + "getReport&outputMode=pdf&_searchMethod=getScreen&_parent_report_name="
          + "Bills+Report+Builder&selDateRange=cstm&srjsFile=null&reptDescFile=Bill.srjs&"
          + "reportGroup=Billing+Reports&_myreport=nosearch&reportType=list&dateFieldSelection="
          + "open_date&_sel=tf&fromDate=${fy}&toDate=${td}&listFields=bill_no&listFields="
          + "bill_patient_full_name&listFields=bill_status&listFields=total_amount&listFields="
          + "total_receipts&baseFontSize=10&listGroups=&listGroups=&listGroups=&sumGroupHoriz=&"
          + "sumGroupVert=&sumGroupVertSub=&trendType=month&trendGroupVert=&trendGroupVertSub=&"
          + "vtrendGroupHoriz=&vtrendType=month&vtrendGroupVertSub=&customOrder1=&customOrder2=&"
          + "filter.1=&filterOp.1=eq&_report_name=&_actionId=rep_bill_builder"),;

  /** The report id. */
  private String reportId;

  /** The screen id. */
  private String screenId;

  /** The display name. */
  private String displayName;

  /** The display query. */
  private String displayQuery;

  /** The url string. */
  private String urlString;

  /**
   * Gets the yesterdays date.
   *
   * @return the yesterdays date
   */
  public static String getYesterdaysDate() {
    Calendar today = new GregorianCalendar();
    today.add(Calendar.DATE, -1);
    String month = String.valueOf(today.get(GregorianCalendar.MONTH) + 1);
    String day = String.valueOf(today.get(GregorianCalendar.DAY_OF_MONTH));
    String year = String.valueOf(today.get(GregorianCalendar.YEAR));
    if (month.length() < 2) {
      month = "0" + month;
    }
    if (day.length() < 2) {
      day = "0" + day;
    }
    String dateStr = day + "-" + month + "-" + year;
    return dateStr;
  }

  /**
   * Gets the todays date in sql fmt.
   *
   * @return the todays date in sql fmt
   */
  public static String getTodaysDateInSqlFmt() {
    Calendar cal = new GregorianCalendar();
    String mnth = String.valueOf(cal.get(GregorianCalendar.MONTH) + 1);
    String dy = String.valueOf(cal.get(GregorianCalendar.DAY_OF_MONTH));
    String yr = String.valueOf(cal.get(GregorianCalendar.YEAR));
    if (mnth.length() < 2) {
      mnth = "0" + mnth;
    }
    if (dy.length() < 2) {
      dy = "0" + dy;
    }
    String dateStr = yr + "-" + mnth + "-" + dy;
    return dateStr;
  }

  /**
   * Gets the todays date.
   *
   * @return the todays date
   */
  public static String getTodaysDate() {
    Calendar cal = new GregorianCalendar();
    String mnth = String.valueOf(cal.get(GregorianCalendar.MONTH) + 1);
    String dy = String.valueOf(cal.get(GregorianCalendar.DAY_OF_MONTH));
    String yr = String.valueOf(cal.get(GregorianCalendar.YEAR));
    if (mnth.length() < 2) {
      mnth = "0" + mnth;
    }
    if (dy.length() < 2) {
      dy = "0" + dy;
    }
    String dateStr = dy + "-" + mnth + "-" + yr;
    return dateStr;
  }

  /**
   * Gets the this months start date.
   *
   * @return the this months start date
   * @throws ParseException
   *           the parse exception
   */
  public static String getThisMonthsStartDate() throws ParseException {
    Calendar cal = Calendar.getInstance();
    int minday = cal.getActualMinimum(cal.DAY_OF_MONTH);
    cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), minday);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    return simpleDateFormat.format(cal.getTime());
  }

  /**
   * Gets the this fin years start date.
   *
   * @param bean
   *          the bean
   * @return the this fin years start date
   * @throws ParseException
   *           the parse exception
   */
  public static String getThisFinYearsStartDate(BasicDynaBean bean) throws ParseException {

    Calendar cal = Calendar.getInstance();
    Integer finmon = (Integer) bean.get("fin_year_start_month"); // HMS-9103
    Integer finMon = finmon - 1;
    if (cal.get(cal.MONTH) >= finMon) {
      cal.set(cal.get(cal.YEAR), finMon, 1);
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
      return simpleDateFormat.format(cal.getTime());
    } else {
      // cal.set(cal.get(cal.YEAR), 2, 31);
      cal.add(Calendar.YEAR, -1);
      cal.set(cal.get(cal.YEAR), finMon, 1);
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
      return simpleDateFormat.format(cal.getTime());
    }
  }

  /**
   * Gets the this months start sql date.
   *
   * @return the this months start sql date
   * @throws ParseException
   *           the parse exception
   */
  public static String getThisMonthsStartSqlDate() throws ParseException {
    Calendar cal = Calendar.getInstance();
    int minday = cal.getActualMinimum(cal.DAY_OF_MONTH);
    cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), minday);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    return "'" + simpleDateFormat.format(cal.getTime()) + "'";
  }

  /**
   * Gets the this fin years start sql date.
   *
   * @param bean
   *          the bean
   * @return the this fin years start sql date
   * @throws ParseException
   *           the parse exception
   */
  public static String getThisFinYearsStartSqlDate(BasicDynaBean bean) throws ParseException {
    Calendar cal = Calendar.getInstance();
    Integer finmon = (Integer) bean.get("fin_year_start_month"); // HMS-9103
    Integer finMon = finmon - 1;
    if (cal.get(cal.MONTH) >= finMon) {
      cal.set(cal.get(cal.YEAR), finMon, 1);
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
      return "'" + simpleDateFormat.format(cal.getTime()) + "'";
    } else {
      cal.add(Calendar.YEAR, -1);
      cal.set(cal.get(cal.YEAR), finMon, 1);
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
      return "'" + simpleDateFormat.format(cal.getTime()) + "'";
    }
  }

  /**
   * Instantiates a new page stats.
   *
   * @param reportId
   *          the report id
   * @param screenId
   *          the screen id
   * @param displayName
   *          the display name
   * @param displayQuery
   *          the display query
   * @param urlString
   *          the url string
   */
  private PageStats(String reportId, String screenId, String displayName, String displayQuery,
      String urlString) {
    this.reportId = reportId;
    this.screenId = screenId;
    this.displayName = displayName;
    this.displayQuery = displayQuery;
    this.urlString = urlString;
  }

  /**
   * Gets the report id.
   *
   * @return the report id
   */
  public String getReportId() {
    return reportId;
  }

  /**
   * Gets the screen id.
   *
   * @return the screen id
   */
  public String getScreenId() {
    return screenId;
  }

  /**
   * Gets the display name.
   *
   * @return the display name
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Gets the display count query.
   *
   * @param bean
   *          the bean
   * @return the display count query
   * @throws ParseException
   *           the parse exception
   */
  public String getDisplayCountQuery(BasicDynaBean bean) throws ParseException {
    String newDisplayQuery = displayQuery;
    newDisplayQuery = newDisplayQuery.replaceAll("\\$\\{tm\\}", getThisMonthsStartSqlDate());
    newDisplayQuery = newDisplayQuery.replaceAll("\\$\\{fy\\}", getThisFinYearsStartSqlDate(bean));
    return newDisplayQuery;
  }

  /**
   * Gets the URL string.
   *
   * @param bean
   *          the bean
   * @return the URL string
   * @throws ParseException
   *           the parse exception
   */
  public String getURLString(BasicDynaBean bean) throws ParseException {
    String newString = urlString;
    newString = newString.replaceAll("\\$\\{td\\}", getTodaysDate());
    newString = newString.replaceAll("\\$\\{yd\\}", getYesterdaysDate());
    newString = newString.replaceAll("\\$\\{tm\\}", getThisMonthsStartDate());
    newString = newString.replaceAll("\\$\\{fy\\}", getThisFinYearsStartDate(bean));
    newString = newString.replaceAll("\\$\\{sqlTd\\}", getTodaysDateInSqlFmt());
    return newString;
  }

  /**
   * Gets the properties map.
   *
   * @param bean
   *          the bean
   * @return the properties map
   * @throws ParseException
   *           the parse exception
   */
  public Map getPropertiesMap(BasicDynaBean bean) throws ParseException {
    Map map = new HashMap();
    map.put("report_id", reportId);
    map.put("display_name", displayName);
    map.put("display_query", displayQuery);
    String newString = urlString;
    newString = newString.replaceAll("\\$\\{td\\}", getTodaysDate());
    newString = newString.replaceAll("\\$\\{yd\\}", getYesterdaysDate());
    newString = newString.replaceAll("\\$\\{tm\\}", getThisMonthsStartDate());
    newString = newString.replaceAll("\\$\\{fy\\}", getThisFinYearsStartDate(bean));
    newString = newString.replaceAll("\\$\\{sqlTd\\}", getTodaysDateInSqlFmt());
    map.put("url_string", newString);
    return map;
  }

  /**
   * Gets the display map.
   *
   * @return the display map
   */
  public Map getDisplayMap() {
    Map map = new HashMap();
    map.put("report_id", reportId);
    map.put("display_name", displayName);
    return map;
  }

  /**
   * Gets the screen rights map.
   *
   * @return the screen rights map
   */
  public Map getScreenRightsMap() {
    Map map = new HashMap();
    map.put("report_id", reportId);
    map.put("screen_id", screenId);
    return map;
  }

}
