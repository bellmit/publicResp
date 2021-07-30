<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page import="java.util.Map"%>

<html>
<head>
    <title>System Preferences</title>
    <c:set var="cpath" value="${pageContext.request.contextPath}"/>
</head>

<body>
  <form name="systemPreferencesInsta" method="POST" action="${cpath}/systempreferences/update.htm">
      <div class="pageHeader">System Preferences</div>
      <insta:feedback-panel/>
      <div>
        <input type="hidden" name="mod_username" id="mod_username" value=""/>
        <div class="pannel">
          <fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Mail & SMS</legend>
            <table class="formtable">
              <tr>
                <td class="formlabel">Protocol : </td>
                <td>
                  <input type="text"  name="protocol" id="protocol" value='${bean.protocol}' />
                </td>
              </tr>
              <tr>
                <td class="formlabel">Port Number : </td>
                <td>
                  <input type="text" name="port_no" value='${bean.port_no }' id="port_number" onkeypress="return enterNumOnlyzeroToNine(event);" />
                </td>
              </tr>
              <tr>
                <td class="formlabel">Authentication Required : </td>
                <td>
                  <insta:selectoptions name="auth_required" value='${bean.auth_required }' opvalues="true,false" optexts="Yes,No"/>
                </td>
              </tr>
              <tr>
                <td class="formlabel">Password : </td>
                <td>
                  <input type="text" name="password" value='${bean.password }' />
                </td>
              </tr>
              <tr>
                <td class="formlabel">Host Name : </td>
                <td>
                  <input type="text" name="host_name" value='${bean.host_name }' />
                </td>
              </tr>
              <tr>
                <td class="formlabel">User Name : </td>
                <td>
                  <input type="text" name="username" value='${bean.username }' />
                </td>
              </tr>
              <tr>
                <td class="formlabel">Hospital Mail ID : </td>
                <td>
                  <input type="text" name="hospital_mail_id" value='${bean.hospital_mail_id }' />
                </td>
              </tr>
              <tr>
                <td class="formlabel">SMS UserName:  </td>
                <td>
                  <input type="text"  name="sms_username" value='${bean.sms_username }' />
                </td>
              </tr>
            </table>
          </fieldset>
          
          <fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Diagnostics</legend>
            <table class="formtable">
              <tr>
                <td class="formlabel">Sample Flow Required: </td>
                <td>
                  <insta:selectoptions name="sampleflow_required" value='${bean.sampleflow_required }' opvalues="Y,N" optexts="Yes,No"/>
                </td>
              </tr>
              <tr>
                <td class="formlabel">Autogenerate Lab No: </td>
                <td>
                  <insta:selectoptions name="autogenerate_labno" value='${bean.autogenerate_labno }' opvalues="Y,N" optexts="Yes,No"/>
                </td>
              </tr>
              <tr>
                <td class="formlabel">Max Collection Centers Count:  </td>
                <td>
                  <input type="text" name="max_collection_centers_count" value='${bean.max_collection_centers_count }' 
                      onkeypress="return enterNumOnlyzeroToNine(event);"/>
                </td>
              </tr>
            </table>
          </fieldset>

          <fieldset class="fieldSetBorder"><legend class="fieldSetLabel">System</legend>
            <table class="formtable">
              <tr>
                <td class="formlabel">Menu Background Color:  </td>
                <td>
                  <input type="text" name="menu_background_color" value='${bean.menu_background_color }' />
                </td>
              </tr>
              <tr>
                <td class="formlabel">Calendar Start Date:  </td>
                <td>
                  <input type="text" name="calendar_start_day" id="calendar_start_day" value='${bean.calendar_start_day }' onkeypress="return enterNumOnlyzeroToNine(event);" />
                </td>
              </tr>
              <tr>
                <td class="formlabel">Upload Limit in Mb:  </td>
                <td>
                  <input type="text" name="upload_limit_in_mb" id="upload_limit_in_mb" value='${bean.upload_limit_in_mb }' onkeypress="return enterNumOnlyzeroToNine(event);" />
                </td>
              </tr>
              <tr>
                <td class="formlabel">Enable Nestloop:  </td>
                <td>
                  <insta:selectoptions name="enable_nestloop" value='${bean.enable_nestloop }' opvalues="Y,N" optexts="Yes,No"/>
                  <input type="hidden" name="enable_nestloop" value='${bean.enable_nestloop }' />
                </td>
              </tr>
              <tr>
                <td class="formlabel">Separator Type:  </td>
                <td>
                  <input type="text" name="separator_type" value='${bean.separator_type }' />
                </td>
              </tr>
              <tr>
                <td class="formlabel">Patient Name Match Distance:  </td>
                <td>
                   <insta:selectoptions name="patient_name_match_distance" value='${bean.patient_name_match_distance }' opvalues="0,1,2" optexts="0,1,2"/>
                </td>
                </tr>
                <tr>
                <td class="formlabel">Show Stacktrace </td>
                <td>
                  <insta:selectoptions name="show_stacktrace" value='${bean.show_stacktrace }' opvalues="true,false" optexts="Yes,No"/>
                </td>
              </tr>
            </table>
          </fieldset>
        </div>
   
        <div class="pannel">
          <fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Pharmacy/Stores Preferences</legend>
            <table class="formtable">
              <tr>
                <td class="formlabel">Stock Entry Against Delivery Order:  </td>
                <td>
                  <insta:selectoptions name="stock_entry_agnst_do" value='${bean.stock_entry_agnst_do }' opvalues="Y,N" optexts="Yes,No"/>
                </td>
              </tr>
              <tr>
                <td class="formlabel">Allow Cross Center Indents:</td>
                <td>
                  <insta:selectoptions name="allow_cross_center_indents" value='${bean.allow_cross_center_indents }' opvalues="Y,N" optexts="Yes,No"/>
                </td>
              </tr>
          
              <tr>
                <td class="formlabel">Issue To Dept/Ward Only:  </td>
                <td>
                  <insta:selectoptions name="issue_to_dept_only" value='${bean.issue_to_dept_only }' opvalues="Y,N" optexts="Yes,No"/>
                </td>
              </tr>
          
              <tr>
                <td class="formlabel">Indent Approval Store:  </td>
                <td>
                  <insta:selectoptions name="indent_approval_by" value='${bean.indent_approval_by }' opvalues="I,R" 
                    optexts="Indent Store, Requesting Store" />
                </td>
              </tr>
          
              <tr>
                <td class="formlabel">Stock Inc:  </td>
                <td>
                  <insta:selectoptions name="stock_inc" value='${bean.stock_inc }' opvalues="M,L" 
                    optexts="Max_CP_GRN, Last_CP_GRN" />
                </td>
              </tr>
              <tr>
                <td class="formlabel">Sales Print Items:  </td>
                <td>
                  <insta:selectoptions name="sales_print_items" value='${bean.sales_print_items }' 
                    opvalues="BILLPRESCLABEL,BILLONLY,BILLPRESC,BILLLABEL" 
                    optexts="Bill Prescription and Label, Bill Only, Bill and Prescription, Bill and Label"/>
                </td>
              </tr>
              <tr>
                <td class="formlabel">Barcode for Item:  </td>
                <td>
                  <insta:selectoptions name="barcode_for_item" value='${bean.barcode_for_item }' opvalues="Y,N" optexts="Yes,No"/>
                </td>
              </tr>
              <tr>
                <td class="formlabel">Cess Applicable:  </td>
                <td>
                  <insta:selectoptions name="cess_applicable" value='${bean.cess_applicable }' opvalues="Y,N" optexts="Yes,No"/>
                </td>
              </tr>
              <tr>
                <td class="formlabel">Allow CP based Sales:  </td>
                <td>
                  <insta:selectoptions name="pharma_allow_cp_sale" value='${bean.pharma_allow_cp_sale }' opvalues="Y,N" optexts="Yes,No"/>
                </td>
              </tr>
              <tr>
                <td class="formlabel">Tax Applicable:  </td>
                <td>
                  <insta:selectoptions name="vat_applicable" value='${bean.vat_applicable }' opvalues="Y,N" optexts="Yes,No"/>
                </td>
              </tr>
              <tr>
                <td class="formlabel">Pharmacy Return Restricted:  </td>
                <td>
                  <insta:selectoptions name="pharma_return_restricted" value='${bean.pharma_return_restricted }' opvalues="Y,N" optexts="Yes,No"/>
                </td>
              </tr>
              <tr>
                <td class="formlabel">Separate Pharmacy Credit Bill:  </td>
                <td>
                  <insta:selectoptions name="seperate_pharmacy_credit_bill" value='${bean.seperate_pharmacy_credit_bill }' opvalues="Y,N" optexts="Yes,No"/>
                </td>
              </tr>
              <tr>
                <td class="formlabel">Pharmacy Sale Margin(%):  </td>
                <td>
                  <input type="text" name="pharmacy_sale_margin_in_per" id="pharmacy_sale_margin_in_per" value='${bean.pharmacy_sale_margin_in_per }' onkeypress="return enterNumOnlyANDdot(event);"/>
                </td>
              </tr>
              <tr>
                <td class="formlabel">Daily Checkpoint1:  </td>
                <td>
                  <input type="text" name="daily_checkpoint1" id="daily_checkpoint1" value='${bean.daily_checkpoint1 }' />
                </td>
              </tr>
              <tr>
                <td class="formlabel">Daily Checkpoint2:  </td>
                <td>
                  <input type="text" name="daily_checkpoint2" id="daily_checkpoint2" value='${bean.daily_checkpoint2 }' />
                </td>
              </tr>
              <tr>
                <td class="formlabel">Restrict Inactive Visits:  </td>
                <td>
                  <insta:selectoptions name="restrict_inactive_visits" value='${bean.restrict_inactive_visits }' opvalues="I,O,B,N" 
                      optexts="Inactive IP Patients, Inactive OP Patients, Active Patients, Disable Feature"/>
                </td>
              </tr>
            </table>
          </fieldset>
          
          <fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Registration</legend>
            <table class="formtable">
              <tr>      
                <td class="formlabel">Selection For Mrno Search: </td>
                <td>
                  <insta:selectoptions name="enable_force_selection_for_mrno_search" value='${bean.enable_force_selection_for_mrno_search }' opvalues="Y,N" optexts="Yes,No"/>
                </td>
              </tr>
            </table>
          </fieldset>
          <fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Accounting</legend>
            <table class="formtable">
              <tr>      
                <td class="formlabel">Scan for missing bills newer than </td>
                <td>
                  <input type="number" max="8784" min="2" name="accounting_missing_data_scan_rel_start" value='${bean.accounting_missing_data_scan_rel_start }'/> hour(s)
                </td>
              </tr>
              <tr>      
                <td class="formlabel">Scan for missing bills older than </td>
                <td>
                  <input type="number" max="8784" min="1" name="accounting_missing_data_scan_rel_end" value='${bean.accounting_missing_data_scan_rel_end }'/> hour(s)
                </td>
              </tr>
            </table>
          </fieldset>
        </div>
      
        <div class="pannel">
          <fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Billing & Insurance</legend>
            <table class="formtable">
              <tr>
                <td class="formlabel">CFD Max Count:  </td>
                <td>
                  <input type="text" name="cfd_max_count" id="cfd_max_count" value='${bean.cfd_max_count }' onkeypress="return enterNumOnlyzeroToNine(event);" />
                </td>
              </tr>
              <tr>
                <td class="formlabel">Deposit Avalibility:  </td>
                <td>
                	<insta:selectoptions name="deposit_avalibility" value='${bean.deposit_avalibility }' opvalues="B,H,P" optexts="Both,Hospital,Pharmacy"/>
                </td>
              </tr>
              <tr>
                <td class="formlabel">Allow Bill Now Insurance:  </td>
                <td>
                  <insta:selectoptions name="allow_bill_now_insurance" value='${bean.allow_bill_now_insurance }' opvalues="Y,N" optexts="Yes,No"/>
                </td>
              </tr>
              <tr>
                <td class="formlabel">Bill Cancel Require Approval:  </td>
                <td>
                  <insta:selectoptions name="bill_cancellation_requires_approval" value='${bean.bill_cancellation_requires_approval }' opvalues="Y,N" optexts="Yes,No"/>
                </td>
              </tr>
              <tr>
                <td class="formlabel">Apply Rateplan Discount:  </td>
                <td>
                  <insta:selectoptions name="apply_rateplan_discount" value='${bean.apply_rateplan_discount }' opvalues="Y,N" optexts="Yes,No"/>
                </td>
              </tr>
              <tr>
                <td class="formlabel">Aggregate Amt Remittance:  </td>
                <td>
                  <insta:selectoptions name="aggregate_amt_on_remittance" value='${bean.aggregate_amt_on_remittance }' opvalues="Y,N" optexts="Yes,No"/>
                </td>
              </tr>
              <tr>
              	<td class="formlabel">Show Prior Auth prescription List: </td>
              	<td>
              		<insta:selectoptions name="show_prior_auth_presc" value='${bean.show_prior_auth_presc }' opvalues="A,P" 
              			optexts="All prescriptions,Only Prior auth marked items"/>
              	</td>
              </tr>
              <tr>
                <td class="formlabel">Prior Auth Approval Amt as Claim Amt: </td>
                <td>
                  <insta:selectoptions name="set_preauth_approved_amt_as_claim_amt" value='${bean.set_preauth_approved_amt_as_claim_amt }' opvalues="Y,N" optexts="Yes,No"/>
                </td>
              </tr>
              <tr>
                <td class="formlabel">Override Claim Amt with Prior Auth Approval Amt: </td>
                <td>
                  <insta:selectoptions name="update_claim_of_ordered_item_on_preauth_approval" value='${bean.update_claim_of_ordered_item_on_preauth_approval }' opvalues="Y,N" optexts="Yes,No"/>
                </td>
              </tr>
            </table>
          </fieldset>
        
          <fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Clinical</legend>
            <table class="formtable">
              <tr>
                <td class="formlabel">Prescription Uses Stores:  </td>
                <td>
                  <insta:selectoptions name="prescription_uses_stores" value='${bean.prescription_uses_stores }' opvalues="Y,N" optexts="Yes,No"/>
                  </td>
              </tr>
              <tr>
                  <td class="formlabel">Tooth Numbering Systems:  </td>
                  <td>
                       <insta:selectoptions name="tooth_numbering_system" value='${bean.tooth_numbering_system }' opvalues="U,F" optexts="Universal, FDI"/>
                  </td>
              </tr>
              <tr>
                  <td class="formlabel">Allow All Cons Types Reg:  </td>
                  <td>
                      <insta:selectoptions name="allow_all_cons_types_in_reg" value='${bean.allow_all_cons_types_in_reg }' opvalues="Y,N" optexts="Yes,No"/>
                  </td>
              </tr>
              <tr>
              <td class="formlabel">Dental Chart:  </td>
                  <td>
                      <insta:selectoptions name="dental_chart" value='${bean.dental_chart }' opvalues="Y,N" optexts="Yes,No"/>
                  </td>
              </tr>
         
              <tr>
                  <td class="formlabel">Surgery/Procedure Applicable For:  </td>
                  <td>
                  	  <insta:selectoptions name="operation_apllicable_for" value='${bean.operation_apllicable_for }' opvalues="i,b" optexts="Inpatient, Inpatient & Outpatient"/>
                  </td>
              </tr>
            </table>
          </fieldset>
        
          <fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Others</legend>
            <table class="formtable">
              <tr>
                <td class="formlabel">Auto Close Indent CaseFiles:  </td>
                <td>
                  <insta:selectoptions name="auto_close_indented_casefiles" value='${bean.auto_close_indented_casefiles }' opvalues="Y,N" optexts="Yes,No"/>
                </td>
              </tr>
              <tr>
                <td class="formlabel">Domain Name:  </td>
                <td>
                  <input type="text" name="domain_name" value='${bean.domain_name }' />
                </td>
              </tr>
              <tr>
                <td class="formlabel">Pac Validity Days:  </td>
                <td>
                  <input type="text" name="pac_validity_days" id="pac_validity_days" value='${bean.pac_validity_days }' onkeypress="return enterNumOnlyzeroToNine(event);" />
                </td>
              </tr>
              <tr>
                <td class="formlabel">Hospital Dynamic Addresses:  </td>
                <td>
                  <insta:selectoptions name="hosp_uses_dynamic_addresses" value='${bean.hosp_uses_dynamic_addresses }' opvalues="Y,N" optexts="Yes,No"/>
                </td>
              </tr>
            </table>
          </fieldset>
          <fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Insta Support</legend>
            <table class="formtable">
              <tr>
                <td class="formlabel">Live Chat Support: </td>
                <td>
                  <insta:selectoptions name="live_chat_support" value='${bean.live_chat_support }' opvalues="true,false" optexts="Yes,No"/>
                </td>
              </tr>
            </table>
          </fieldset>
        </div>
      </div>
      
      <div class="clrboth"></div>
      <div class="fltL MrgTop10" >
        <button type="submit" property="Submit" accesskey="S" onclick="return update();"><b><u><insta:ltext key="patient.genericpreference.addshow.s"/></u></b><insta:ltext key="patient.genericpreference.addshow.ave"/></button>
      </div>
  </form>
<insta:link type="script" file="SystemPreferences/systemPreferences.js" />
<insta:link type="script" file="hmsvalidation.js"/>

</body>
</html>
