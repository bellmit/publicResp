<%@page contentType="text/html" isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="org.apache.struts.Globals"%>

<html>
<head>
	<title>Role - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="script" file="usermanager/role.js"/>

	<%-- please put all Javascript in scripts/usermanager/role.js --%>
<%--
<script>
		function doCancel()
		{
			window.location.href="${pageContext.request.contextPath}/pages/usermanager/UserDashBoard.do?_method=list";
		}
	</script>
--%>
	<style>
		.ygtv-checkbox {
			width: 50px;
		}
		.ygtvlabel {
			white-space: nowrap;
			padding-left: 5px;
		}
	</style>
</head>

<body onload="getRoleScreens();" class="setMargin"  class="yui-skin-sam">
	<html:form action="/pages/usermanager/RoleAction.do" method="post" onsubmit="return false;">
		<html:hidden property="method" value="createRole"></html:hidden>
		<html:hidden property="status" value="A"/>
		<html:hidden property="roleId" value="${hRoleId}"/>
		<html:hidden property="operation" value="create"/>
		<html:hidden property="rolename" value="${roleName}"/>


<table class="formtable" border="0" cellpadding="0" cellspacing="0" align="center" width="100%">
	<tr>
		<td class="pageHeader">Role</td>
	</tr>

	<tr>
		<td>
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Role Details</legend>
			<table class="formtable" border="0" cellpadding="0" cellspacing="0">

					<tr>
						<td class="formlabel">Application Role Name:</td>
						<td>
							<div id="createRole">    <%-- for show/hide --%>
								<html:text property="name" styleClass="text-input" maxlength="24" size="24"
									onkeypress="return enterAlphaNumeric(event)"/>
									<span class="star">*</span>
							</div>
						</td>
						<td class="formlabel">Remarks:</td>
						<td valign="bottom">
							<html:textarea property="remarks" rows="1" cols="40"/>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Last Modified By:</td>
						<td>
							<b><label id="mod_user"></label></b>  <b><label id="mod_date"></label></b>
						</td>
						<td></td>
						<td></td>
					</tr>
				</table>
			</fieldset>
		</td>
	</tr>

	<tr>
		<td>
			<table border="0" cellspacing="0" cellpadding="0">
				<tr>
				  <td valign="top" style="width: 35em">
				  <fieldset class="fieldSetBorder" id="actionRights">
				  	<legend class="fieldSetLabel">Screens</legend>
				  <!-- Div tag for YUI TREE Widget -->
						<div id="treeDiv" class="ygtv-checkbox"></div>
				  </fieldset>
				  <!-- Hidden form variables for all screens and ScreenGroups -->
					<c:forEach items="${screenConfig.screenGroupList}" var="group" varStatus="groupStatus">
						<c:forEach items="${group.screenList}" var="screen" varStatus="screenStatus">
							<input type="hidden" name="screenRights(${screen.id})" id="screenRights_${screen.id}_A" value="N">
						</c:forEach>
					</c:forEach>
				  </td>

					<td valign="top">
						<fieldset class="fieldSetBorder" id="actionRights" style="width: 438px;">
							<legend class="fieldSetLabel">Actions</legend>
							<table class="dashboard" border="0" cellspacing="0" cellpadding="0" width="100%">
								<tr >
									<td >Screen Activity</td>
									<td colspan="2" >Access Rights</td>
								</tr>

								<tr class="firstRow">
									<td>Reopen Bill</td>
									<td>
										<input type="radio" name="actionRights(bill_reopen)"
												id="actionRights_bill_reopen_A" value="A"/>
										<label for="actionRights_bill_reopen_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(bill_reopen)"
												id="actionRights_bill_reopen_N" value="N"/>
										<label for="actionRights_bill_reopen_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Direct Billing (without using Order)</td>
									<td>
										<input type="radio" name="actionRights(addtobill_charges)"
												id="actionRights_addtobill_charges_A" value="A"/>
										<label for="actionRights_addtobill_charges_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(addtobill_charges)"
												id="actionRights_addtobill_charges_N" value="N"/>
										<label for="actionRights_addtobill_charges_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Add Items to Bill/Edit Bill Amounts</td>
									<td>
										<input type="radio" name="actionRights(edit_bill)"
												id="actionRights_edit_bill_A" value="A"/>
										<label for="actionRights_edit_bill_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(edit_bill)"
												id="actionRights_edit_bill_N" value="N"/>
										<label for="actionRights_edit_bill_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Allow Tax Sub Group Editing</td>
									<td>
										<input type="radio" name="actionRights(allow_tax_subgroup_edit)"
												id="actionRights_allow_tax_subgroup_edit_A" value="A"/>
										<label for="actionRights_allow_tax_subgroup_edit_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(allow_tax_subgroup_edit)"
												id="actionRights_allow_tax_subgroup_edit_N" value="N"/>
										<label for="actionRights_allow_tax_subgroup_edit_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Create Bill for Closed Visit</td>
									<td>
										<input type="radio" name="actionRights(create_bill_for_closed_visit)"
												id="actionRights_create_bill_for_closed_visit_A" value="A"/>
										<label for="actionRights_create_bill_for_closed_visit_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(create_bill_for_closed_visit)"
												id="actionRights_create_bill_for_closed_visit_N" value="N"/>
										<label for="actionRights_create_bill_for_closed_visit_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Cancel Bill</td>
									<td>
										<input type="radio" name="actionRights(cancel_bill)"
												id="actionRights_cancel_bill_A" value="A"/>
										<label for="actionRights_cancel_bill_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(cancel_bill)"
												id="actionRights_cancel_bill_N" value="N"/>
										<label for="actionRights_cancel_bill_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Cancel Items in Bill</td>
									<td>
										<input type="radio" name="actionRights(cancel_elements_in_bill)"
												id="actionRights_cancel_elements_in_bill_A" value="A"/>
										<label for="actionRights_cancel_elements_in_bill_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(cancel_elements_in_bill)"
												id="actionRights_cancel_elements_in_bill_N" value="N"/>
										<label for="actionRights_cancel_elements_in_bill_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Allow Refunds</td>
									<td>
										<input type="radio" name="actionRights(allow_refund)"
												id="actionRights_allow_refund_A" value="A"/>
										<label for="actionRights_allow_refund_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(allow_refund)"
												id="actionRights_allow_refund_N" value="N"/>
										<label for="actionRights_allow_refund_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Allow Discounts</td>
									<td>
										<input type="radio" name="actionRights(allow_discount)"
												id="actionRights_allow_discount_A" value="A"/>
										<label for="actionRights_allow_discount_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(allow_discount)"
												id="actionRights_allow_discount_N" value="N"/>
										<label for="actionRights_allow_discount_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Allow All Rate Increase</td>
									<td>
										<input type="radio" name="actionRights(allow_rateincrease)"
												id="actionRights_allow_rateincrease_A" value="A"/>
										<label for="actionRights_allow_rateincrease_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(allow_rateincrease)"
												id="actionRights_allow_rateincrease_N" value="N"/>
										<label for="actionRights_allow_rateincrease_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Allow All Rate Decrease</td>
									<td>
										<input type="radio" name="actionRights(allow_ratedecrease)"
												id="actionRights_allow_ratedecrease_A" value="A"/>
										<label for="actionRights_allow_ratedecrease_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(allow_ratedecrease)"
												id="actionRights_allow_ratedecrease_N" value="N"/>
										<label for="actionRights_allow_ratedecrease_N">No</label>
									</td>
								</tr>
								<tr>
									<td>View All Rates in Order/Prescription</td>
									<td>
										<input type="radio" name="actionRights(view_all_rates)"
												id="actionRights_view_all_rates_A" value="A"/>
										<label for="actionRights_view_all_rates_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(view_all_rates)"
												id="actionRights_view_all_rates_N" value="N"/>
										<label for="actionRights_view_all_rates_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Allow Back Date</td>
									<td>
										<input type="radio" name="actionRights(allow_backdate)"
												id="actionRights_allow_backdate_A" value="A"/>
										<label for="actionRights_allow_backdate_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(allow_backdate)"
												id="actionRights_allow_backdate_N" value="N"/>
										<label for="actionRights_allow_backdate_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Edit Date</td>
									<td>
										<input type="radio" name="actionRights(edit_date)"
												id="actionRights_edit_date_A" value="A"/>
										<label for="actionRights_edit_date_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(edit_date)"
												id="actionRights_edit_date_N" value="N"/>
										<label for="actionRights_edit_date_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Allow Receipt Back Date</td>
									<td>
										<input type="radio" name="actionRights(allow_receipt_backdate)"
												id="actionRights_allow_receipt_backdate_A" value="A"/>
										<label for="actionRights_allow_receipt_backdate_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(allow_receipt_backdate)"
												id="actionRights_allow_receipt_backdate_N" value="N"/>
										<label for="actionRights_allow_receipt_backdate_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Allocate/Shift Bed</td>
									<td>
										<input type="radio" name="actionRights(bed_close)"
												id="actionRights_bed_close_A" value="A"/>
										<label for="actionRights_bed_close_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(bed_close)"
												id="actionRights_bed_close_N" value="N"/>
										<label for="actionRights_bed_close_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Allow Set Charged Bed Type</td>
									<td>
										<input type="radio" name="actionRights(set_charged_bed_type)"
												id="actionRights_set_charged_bed_type_A" value="A"/>
										<label for="actionRights_set_charged_bed_type_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(set_charged_bed_type)"
												id="actionRights_set_charged_bed_type_N" value="N"/>
										<label for="actionRights_set_charged_bed_type_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Edit Inactive Visit Discharge Summary</td>
									<td>
										<input type="radio" name="actionRights(dishcharge_close)"
												id="actionRights_dishcharge_close_A" value="A"/>
										<label for="actionRights_dishcharge_close_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(dishcharge_close)"
												id="actionRights_dishcharge_close_N" value="N"/>
										<label for="actionRights_dishcharge_close_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Edit Regn. Fields(First Name)</td>
									<td>
										<input type="radio" name="actionRights(edit_first_name)"
												id="actionRights_edit_first_name_A" value="A"/>
										<label for="actionRights_edit_first_name_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(edit_first_name)"
												id="actionRights_edit_first_name_N" value="N"/>
										<label for="actionRights_edit_first_name_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Edit Regn. Fields(Custom Fields)</td>
									<td>
										<input type="radio" name="actionRights(edit_custom_fields)"
												id="actionRights_edit_custom_fields_A" value="A"/>
										<label for="actionRights_edit_custom_fields_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(edit_custom_fields)"
												id="actionRights_edit_custom_fields_N" value="N"/>
										<label for="actionRights_edit_custom_fields_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Patient Category Change</td>
									<td>
										<input type="radio" name="actionRights(patient_category_change)"
												id="actionRights_patient_category_change_A" value="A"/>
										<label for="actionRights_patient_category_change_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(patient_category_change)"
												id="actionRights_patient_category_change_N" value="N"/>
										<label for="actionRights_patient_category_change_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Sign-off Lab / Service Reports</td>
									<td>
										<input type="radio" name="actionRights(sign_off_lab_reports)"
												id="actionRights_sign_off_lab_reports_A" value="A"/>
										<label for="actionRights_sign_off_lab_reports_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(sign_off_lab_reports)"
												id="actionRights_sign_off_lab_reports_N" value="N"/>
										<label for="actionRights_sign_off_lab_reports_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Revert Services Conduction Status</td>
									<td>
										<input type="radio" name="actionRights(revert_service_conduction)"
												id="actionRights_revert_service_conduction_A" value="A"/>
										<label for="actionRights_revert_service_conduction_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(revert_service_conduction)"
												id="actionRights_revert_service_conduction_N" value="N"/>
										<label for="actionRights_revert_service_conduction_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Revert Service Sign-off</td>
									<td>
										<input type="radio" name="actionRights(revert_service_signoff)"
												id="actionRights_revert_service_signoff_A" value="A"/>
										<label for="actionRights_revert_service_signoff_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(revert_service_signoff)"
												id="actionRights_revert_service_signoff_N" value="N"/>
										<label for="actionRights_revert_service_signoff_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Allow New Registration</td>
									<td>
										<input type="radio" name="actionRights(allow_new_registration)"
												id="actionRights_allow_new_registration_A" value="A">
										<label for="actionRights_allow_new_registration_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(allow_new_registration)"
												id="actionRights_allow_new_registration_N" value="N">
										<label for="actionRights_allow_new_registration_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Add Edit Contact</td>
									<td>
										<input type="radio" name="actionRights(add_edit_contact)"
												id="actionRights_add_edit_contact_A" value="A">
										<label for="actionRights_add_edit_contact_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(add_edit_contact)"
												id="actionRights_add_edit_contact_N" value="N">
										<label for="actionRights_add_edit_contact_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Cancel Test (After Sample Collection)</td>
									<td>
										<input type="radio" name="actionRights(allow_cancel_test)"
												id="actionRights_allow_cancel_test_A" value="A">
										<label for="actionRights_allow_cancel_test_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(allow_cancel_test)"
												id="actionRights_allow_cancel_test_N" value="N">
										<label for="actionRights_allow_cancel_test_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Cancel Test (At Any Time)</td>
									<td>
										<input type="radio" name="actionRights(cancel_test_any_time)"
												id="actionRights_cancel_test_any_time_A" value="A">
										<label for="actionRights_cancel_test_any_time_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(cancel_test_any_time)"
												id="actionRights_cancel_test_any_time_N" value="N">
										<label for="actionRights_cancel_test_any_time_N">No</label>
									</td>
								</tr>
								<tr>
								<tr>
									<td>Allow Retail Credit Sales</td>
									<td>
										<input type="radio" name="actionRights(allow_retail_credit_sales)"
												id="actionRights_allow_retail_credit_sales_A" value="A">
										<label for="actionRights_allow_retail_credit_sales_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(allow_retail_credit_sales)"
												id="actionRights_allow_retail_credit_sales_N" value="N">
										<label for="actionRights_allow_retail_credit_sales_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Allow Retail Sales</td>
									<td>
										<input type="radio" name="actionRights(allow_retail_sales)"
												id="actionRights_allow_retail_sales_A" value="A">
										<label for="actionRights_allow_retail_sales_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(allow_retail_sales)"
												id="actionRights_allow_retail_sales_N" value="N">
										<label for="actionRights_allow_retail_sales_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Allow Max Costprice Change</td>
									<td>
										<input type="radio" name="actionRights(change_max_costprice)"
												id="actionRights_change_max_costprice_A" value="A">
										<label for="actionRights_change_max_costprice_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(change_max_costprice)"
												id="actionRights_change_max_costprice_N" value="N">
										<label for="actionRights_change_max_costprice_N">No</label>
									</td>
								</tr>

								<tr>
									<td>Show Avbl Qty On Raise Indent</td>
									<td>
										<input type="radio" name="actionRights(show_avbl_qty)"
												id="actionRights_show_avbl_qty_A" value="A">
										<label for="actionRights_show_avbl_qty_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(show_avbl_qty)"
												id="actionRights_show_avbl_qty_N" value="N">
										<label for="actionRights_show_avbl_qty_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Reopen Codification</td>
									<td>
										<input type="radio" name="actionRights(reopen_codification)"
												id="actionRights_reopen_codification_A" value="A">
										<label for="actionRights_reopen_codification_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(reopen_codification)"
												id="actionRights_reopen_codification_N" value="N">
										<label for="actionRights_reopen_codification_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Access for All Coder Reviews</td>
									<td>
										<input type="radio" name="actionRights(access_for_all_coder_reviews)"
												id="actionRights_access_for_all_coder_reviews_A" value="A">
										<label for="actionRights_access_for_all_coder_reviews_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(access_for_all_coder_reviews)"
												id="actionRights_access_for_all_coder_reviews_N" value="N">
										<label for="actionRights_access_for_all_coder_reviews_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Allow Direct Stock Entry</td>
									<td>
										<input type="radio" name="actionRights(direct_stock_entry)"
												id="actionRights_direct_stock_entry_A" value="A">
										<label for="actionRights_direct_stock_entry_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(direct_stock_entry)"
												id="actionRights_direct_stock_entry_N" value="N">
										<label for="actionRights_direct_stock_entry_N">No</label>
									</td>
								</tr>
								<tr>
									<td>User / Counter Day Book Access</td>
									<td>
										<input type="radio" name="actionRights(usr_or_counter_day_book_access)"
											id="actionRights_usr_or_counter_day_book_access_A" value="A">
										<label for="actionRights_usr_or_counter_day_book_access_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(usr_or_counter_day_book_access)"
											id="actionRights_usr_or_counter_day_book_access_N" value="N">
										<label for="actionRights_usr_or_counter_day_book_access_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Edit Receipt Amounts</td>
									<td>
										<input type="radio" name="actionRights(edit_receipt_amounts)"
											id="actionRights_edit_receipt_amounts_A" value="A">
										<label for="actionRights_edit_receipt_amounts_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(edit_receipt_amounts)"
											id="actionRights_edit_receipt_amounts_N" value="N">
										<label for="actionRights_edit_receipt_amounts_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Cancel Appointment</td>
									<td>
										<input type="radio" checked name="actionRights(cancel_scheduler_appointment)"
											id="actionRights_cancel_scheduler_appointment_A" value="A">
										<label for="actionRights_cancel_scheduler_appointment_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(cancel_scheduler_appointment)"
											id="actionRights_cancel_scheduler_appointment_N" value="N">
										<label for="actionRights_cancel_scheduler_appointment_N">No</label>
									</td>
								</tr>
								<tr>
									<td>New Bill For Order Screen</td>
									<td>
										<input type="radio" checked name="actionRights(new_bill_for_order_screen)"
											id="actionRights_new_bill_for_order_screen_A" value="A">
										<label for="actionRights_new_bill_for_order_screen_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(new_bill_for_order_screen)"
											id="actionRights_new_bill_for_order_screen_N" value="N">
										<label for="actionRights_new_bill_for_order_screen_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Validate test results</td>
									<td>
										<input type="radio" checked name="actionRights(validate_test_results)"
											id="actionRights_validate_test_results_A" value="A">
										<label for="actionRights_validate_test_results_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(validate_test_results)"
											id="actionRights_validate_test_results_N" value="N">
										<label for="actionRights_validate_test_results_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Revert Signoff</td>
									<td>
										<input type="radio" checked name="actionRights(revert_signoff)"
											id="actionRights_revert_signoff_A" value="A">
										<label for="actionRights_revert_signoff_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(revert_signoff)"
											id="actionRights_revert_signoff_N" value="N">
										<label for="actionRights_revert_signoff_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Amend test results</td>
									<td>
										<input type="radio" checked name="actionRights(amend_test_results)"
											id="actionRights_amend_test_results_A" value="A">
										<label for="actionRights_amend_test_results_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(amend_test_results)"
											id="actionRights_amend_test_results_N" value="N">
										<label for="actionRights_amend_test_results_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Amend Score Card Values</td>
									<td>
										<input type="radio" checked name="actionRights(amend_score_card_values)"
											id="actionRights_amend_score_card_values_A" value="A">
										<label for="actionRights_amend_score_card_values_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(amend_score_card_values)"
											id="actionRights_amend_score_card_values_N" value="N">
										<label for="actionRights_amend_score_card_values_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Reopen Consultation After Time Limit</td>
									<td>
										<input type="radio" checked name="actionRights(reopen_consultation_after_time_limit)"
											id="actionRights_reopen_consultation_after_time_limit_A" value="A">
										<label for="actionRights_reopen_consultation_after_time_limit_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(reopen_consultation_after_time_limit)"
											id="actionRights_reopen_consultation_after_time_limit_N" value="N">
										<label for="actionRights_reopen_consultation_after_time_limit_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Return Pharmacy Items After Validity Days</td>
									<td>
										<input type="radio" checked name="actionRights(allow_pharmacy_item_return_after_validity_days)"
											id="actionRights_allow_pharmacy_item_return_after_validity_days_A" value="A">
										<label for="actionRights_allow_pharmacy_item_return_after_validity_days_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(allow_pharmacy_item_return_after_validity_days)"
											id="actionRights_allow_pharmacy_item_return_after_validity_days_N" value="N">
										<label for="actionRights_allow_pharmacy_item_return_after_validity_days_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Package Approval</td>
									<td>
										<input type="radio" checked name="actionRights(package_approval)"
											id="actionRights_package_approval_A" value="A">
										<label for="actionRights_package_approval_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(package_approval)"
											id="actionRights_package_approval_N" value="N">
										<label for="actionRights_package_approval_N">No</label>
									</td>
								</tr>
								<tr>
									<td>PBM Prescription Finalize</td>
									<td>
										<input type="radio" name="actionRights(pbm_prescription_finalize)"
												id="actionRights_pbm_prescription_finalize_A" value="A"/>
										<label for="actionRights_pbm_prescription_finalize_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(pbm_prescription_finalize)"
												id="actionRights_pbm_prescription_finalize_N" value="N"/>
										<label for="actionRights_pbm_prescription_finalize_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Add Vitals</td>
									<td>
										<input type="radio" name="actionRights(add_vitals)"
												id="actionRights_add_vitals_A" value="A"/>
										<label for="actionRights_add_vitals_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(add_vitals)"
												id="actionRights_add_vitals_N" value="N"/>
										<label for="actionRights_add_vitals_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Delete Vitals</td>
									<td>
										<input type="radio" name="actionRights(delete_vitals)"
												id="actionRights_delete_vitals_A" value="A"/>
										<label for="actionRights_delete_vitals_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(delete_vitals)"
												id="actionRights_delete_vitals_N" value="N"/>
										<label for="actionRights_delete_vitals_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Edit Vitals</td>
									<td>
										<input type="radio" name="actionRights(edit_vitals)"
												id="actionRights_edit_vitals_A" value="A"/>
										<label for="actionRights_edit_vitals_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(edit_vitals)"
												id="actionRights_edit_vitals_N" value="N"/>
										<label for="actionRights_edit_vitals_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Allow MAR Setup</td>
									<td>
										<input type="radio" checked name="actionRights(allow_mar_setup)"
												id="actionRights_allow_mar_setup_A" value="A"/>
										<label for="actionRights_allow_mar_setup_A">Yes</label>
									</td>
									<td>
										<input type="radio" name="actionRights(allow_mar_setup)"
												id="actionRights_allow_mar_setup_N" value="N"/>
										<label for="actionRights_allow_mar_setup_N">No</label>
									</td>
								</tr>
								<tr>
									<td>ERx Consultation Access</td>
									<td>
										<input type="radio" name="actionRights(erx_consultation_access)"
												id="actionRights_erx_consultation_access_A" value="A"/>
										<label for="actionRights_erx_consultation_access_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(erx_consultation_access)"
												id="actionRights_erx_consultation_access_N" value="N"/>
										<label for="actionRights_erx_consultation_access_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Allow Prior Auth Prescription Edit</td>
									<td>
										<input type="radio" name="actionRights(allow_preauth_prescription_edit)"
												id="actionRights_allow_preauth_prescription_edit_A" value="A"/>
										<label for="actionRights_allow_preauth_prescription_edit_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(allow_preauth_prescription_edit)"
												id="actionRights_allow_preauth_prescription_edit_N" value="N"/>
										<label for="actionRights_allow_preauth_prescription_edit_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Registration Charge Applicability</td>
									<td>
										<input type="radio" name="actionRights(reg_charges_app)"
												id="actionRights_reg_charges_app_A" value="A"/>
										<label for="actionRights_reg_charges_app_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(reg_charges_app)"
												id="actionRights_reg_charges_app_N" value="N"/>
										<label for="actionRights_reg_charges_app_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Patient Header Editability</td>
									<td>
										<input type="radio" name="actionRights(edit_patient_header)"
												id="actionRights_edit_patient_header_A" value="A"/>
										<label for="actionRights_edit_patient_header_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(edit_patient_header)"
												id="actionRights_edit_patient_header_N" value="N"/>
										<label for="actionRights_edit_patient_header_N">No</label>
									</td>
								</tr>
								<tr>
									<td>Edit Patient Communication</td>
									<td>
										<input type="radio" name="actionRights(edit_patient_communication)"
												id="actionRights_edit_patient_communication_A" value="A"/>
										<label for="actionRights_edit_patient_communication_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(edit_patient_communication)"
												id="actionRights_edit_patient_communication_N" value="N"/>
										<label for="actionRights_edit_patient_communication_N">No</label>
									</td>
								</tr>
								<tr>
								    <td>Allow Back Dated Appointments/Block Calendar</td>
									<td>
										<input type="radio" name="actionRights(allow_backdated_app)"
												id="actionRights_allow_backdated_app_A" value="A"/>
										<label for="actionRights_allow_backdated_app_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(allow_backdated_app)"
												id="actionRights_allow_backdated_app_N" value="N"/>
										<label for="actionRights_allow_backdated_app_N">No</label>
									</td>
								</tr>
								<tr>
								    <td>Delete Patient General Documents</td>
									<td>
										<input type="radio" name="actionRights(allow_delete_patient_doc)"
												id="actionRights_allow_delete_patient_doc_A" value="A"/>
										<label for="actionRights_allow_delete_patient_doc_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(allow_delete_patient_doc)"
												id="actionRights_allow_delete_patient_doc_N" value="N"/>
										<label for="actionRights_allow_delete_patient_doc_N">No</label>
									</td>
								</tr>
								<tr>
								    <td>Allow Denial Acceptance</td>
									<td>
										<input type="radio" name="actionRights(allow_denial_acceptance)"
												id="actionRights_allow_denial_acceptance_A" value="A"/>
										<label for="actionRights_allow_denial_acceptance_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(allow_denial_acceptance)"
												id="actionRights_allow_denial_acceptance_N" value="N"/>
										<label for="actionRights_allow_denial_acceptance_N">No</label>
									</td>
								</tr>
								<tr>
								    <td>Allow Edit Dynamic Packages in Bill</td>
									<td>
										<input type="radio" name="actionRights(allow_edit_dyna_package)"
												id="actionRights_allow_edit_dyna_package_A" value="A"/>
										<label for="actionRights_allow_edit_dyna_package_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(allow_edit_dyna_package)"
												id="actionRights_allow_edit_dyna_package_N" value="N"/>
										<label for="actionRights_allow_edit_dyna_package_N">No</label>
									</td>
								</tr>
								<tr>
								    <td>Show Other Center Patients in Reg. Screen</td>
									<td>
										<input type="radio" name="actionRights(show_other_center_patients)"
												id="actionRights_show_other_center_patients_A" value="A"/>
										<label for="actionRights_show_other_center_patients_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(show_other_center_patients)"
												id="actionRights_show_other_center_patients_N" value="N"/>
										<label for="actionRights_show_other_center_patients_N">No</label>
									</td>
								</tr>
								<tr>
								    <td>Create Credit(Bill Later)Bills</td>
									<td>
										<input type="radio" name="actionRights(allow_credit_bill_later)"
												id="actionRights_allow_credit_bill_later_A" value="A"/>
										<label for="actionRights_allow_credit_bill_later_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(allow_credit_bill_later)"
												id="actionRights_allow_credit_bill_later_N" value="N"/>
										<label for="actionRights_allow_credit_bill_later_N">No</label>
									</td>
								</tr>
								<tr>
								    <td>Allow Dyna Package Include/Exclude</td>
									<td>
										<input type="radio" name="actionRights(allow_dyna_package_include_exclude)"
												id="actionRights_allow_dyna_package_include_exclude_A" value="A"/>
										<label for="actionRights_allow_dyna_package_include_exclude_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(allow_dyna_package_include_exclude)"
												id="actionRights_allow_dyna_package_include_exclude_N" value="N"/>
										<label for="actionRights_allow_dyna_package_include_exclude_N">No</label>
									</td>
								</tr>
								<tr>
								    <td>Allow Edit Bill Open Date</td>
									<td>
										<input type="radio" name="actionRights(allow_edit_bill_open_date)"
												id="actionRights_allow_edit_bill_open_date_A" value="A"/>
										<label for="actionRights_allow_edit_bill_open_date_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(allow_edit_bill_open_date)"
												id="actionRights_allow_edit_bill_open_date_N" value="N"/>
										<label for="actionRights_allow_edit_bill_open_date_N">No</label>
									</td>
								</tr>
								<tr>
								    <td>Allow Dynamic CoPay Change</td>
									<td>
										<input type="radio" name="actionRights(allow_dynamic_copay_change)"
												id="actionRights_allow_dynamic_copay_change_A" value="A"/>
										<label for="actionRights_allow_dynamic_copay_change_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(allow_dynamic_copay_change)"
												id="actionRights_allow_dynamic_copay_change_N" value="N"/>
										<label for="actionRights_allow_dynamic_copay_change_N">No</label>
									</td>
								</tr>
								<tr>
								    <td>Allow Discount Plans</td>
									<td>
										<input type="radio" name="actionRights(allow_discount_plans_in_bill)"
												id="actionRights_allow_discount_plans_in_bill_A" value="A"/>
										<label for="actionRights_allow_discount_plans_in_bill_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(allow_discount_plans_in_bill)"
												id="actionRights_allow_discount_plans_in_bill_N" value="N"/>
										<label for="actionRights_allow_discount_plans_in_bill_N">No</label>
									</td>
								</tr>
								<tr>
								    <td>Allow WO Approve</td>
									<td>
										<input type="radio" name="actionRights(allow_wo_approve)"
												id="actionRights_allow_wo_approve_A" value="A"/>
										<label for="actionRights_allow_wo_approve_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(allow_wo_approve)"
												id="actionRights_allow_wo_approve_N" value="N"/>
										<label for="actionRights_allow_wo_approve_N">No</label>
									</td>
								</tr>
								<tr>
								    <td>Add/Edit Appointment</td>
									<td>
										<input type="radio" name="actionRights(add_edit_scheduler_rights)"
												id="actionRights_add_edit_scheduler_rights_A" value="A"/>
										<label for="actionRights_add_edit_scheduler_rights_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(add_edit_scheduler_rights)"
												id="actionRights_add_edit_scheduler_rights_N" value="N"/>
										<label for="actionRights_add_edit_scheduler_rights_N">No</label>
									</td>
								</tr>
								<tr>
                                    <td>Block/Unblock Calendar</td>
                                	<td>
                                		<input type="radio" name="actionRights(block_unblock_calendar)"
                                			    id="actionRights_block_unblock_calendar_A" value="A"/>
                                		<label for="actionRights_block_unblock_calendar_A">Yes</label>
                                	</td>
                                	<td>
                                		<input type="radio" checked name="actionRights(block_unblock_calendar)"
                                				id="actionRights_block_unblock_calendar_N" value="N"/>
                                		<label for="actionRights_block_unblock_calendar_N">No</label>
                                	</td>
                                </tr>
								<tr>
								    <td>Allow Print Reports (Visit Wise)</td>
									<td>
										<input type="radio" name="actionRights(allow_print_report_visit_wise)"
												id="actionRights_allow_print_report_visit_wise_A" value="A"/>
										<label for="actionRights_allow_print_report_visit_wise_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(allow_print_report_visit_wise)"
												id="actionRights_allow_print_report_visit_wise_N" value="N"/>
										<label for="actionRights_allow_print_report_visit_wise_N">No</label>
									</td>
								</tr>
								<tr>
                                    <td>View CEED Response Comments</td>
                                    <td>
                                        <input type="radio" name="actionRights(view_ceed_response_comments)"
                                                id="actionRights_view_ceed_response_comments_A" value="A"/>
                                        <label for="actionRights_view_ceed_response_comments_A">Yes</label>
                                    </td>
                                    <td>
                                        <input type="radio" checked name="actionRights(view_ceed_response_comments)"
                                                id="actionRights_view_ceed_response_comments_N" value="N"/>
                                        <label for="actionRights_view_ceed_response_comments_N">No</label>
                                    </td>
                                </tr>
                                <tr>
                                	<td>Allow Initiate Discharge</td>
                                    <td>
                                        <input type="radio" name="actionRights(initiate_discharge)"
                                                id="actionRights_initiate_discharge_A" value="A"/>
                                        <label for="actionRights_initiate_discharge_A">Yes</label>
                                    </td>
                                    <td>
                                        <input type="radio" checked name="actionRights(initiate_discharge)"
                                                id="actionRights_initiate_discharge_N" value="N"/>
                                        <label for="actionRights_initiate_discharge_N">No</label>
                                    </td>
                                </tr>
                                <tr>
                                	<td>Allow Clinical Discharge</td>
                                    <td>
                                        <input type="radio" name="actionRights(clinical_discharge)"
                                                id="actionRights_clinical_discharge_A" value="A"/>
                                        <label for="actionRights_clinical_discharge_A">Yes</label>
                                    </td>
                                    <td>
                                        <input type="radio" checked name="actionRights(clinical_discharge)"
                                                id="actionRights_clinical_discharge_N" value="N"/>
                                        <label for="actionRights_clinical_discharge_N">No</label>
                                    </td>
                                </tr>
                                <tr>
                                	<td>Undo Section/Form Finalization</td>
                                    <td>
                                        <input type="radio" name="actionRights(undo_section_finalization)"
                                                id="actionRights_undo_section_finalization_A" value="A"/>
                                        <label for="actionRights_undo_section_finalization_A">Yes</label>
                                    </td>
                                    <td>
                                        <input type="radio" checked name="actionRights(undo_section_finalization)"
                                                id="actionRights_undo_section_finalization_N" value="N"/>
                                        <label for="actionRights_undo_section_finalization_N">No</label>
                                    </td>
                                </tr>	
                                <tr>
                                	<td>Modify Bill Finalized Date</td>
                                    <td>
                                        <input type="radio" name="actionRights(modify_bill_finalized_date)"
                                                id="actionRights_modify_bill_finalized_date_A" value="A"/>
                                        <label for="actionRights_modify_bill_finalized_date_A">Yes</label>
                                    </td>
                                    <td>
                                        <input type="radio" checked name="actionRights(modify_bill_finalized_date)"
                                                id="actionRights_modify_bill_finalized_date_N" value="N"/>
                                        <label for="actionRights_modify_bill_finalized_date_N">No</label>
                                </tr>
                                <tr>
                                	<td>Allow Available Credit Limit Change</td>
                                    <td>
                                        <input type="radio" name="actionRights(allow_ip_patient_credit_limit_change)"
                                                id="actionRights_allow_ip_patient_credit_limit_change_A" value="A"/>
                                        <label for="actionRights_allow_ip_patient_credit_limit_change_A">Yes</label>
                                    </td>
                                    <td>
                                        <input type="radio" checked name="actionRights(allow_ip_patient_credit_limit_change)"
                                                id="actionRights_allow_ip_patient_credit_limit_change_N" value="N"/>
                                        <label for="actionRights_allow_ip_patient_credit_limit_change_N">No</label>
                                    </td>
                                </tr>
                                <tr>
                                	<td>Allow Back Date Bill Activities</td>
                                    <td>
                                        <input type="radio" name="actionRights(allow_back_date_bill_activities)"
                                                id="actionRights_allow_back_date_bill_activities_A" value="A"/>
                                        <label for="actionRights_allow_back_date_bill_activities_A">Yes</label>
                                    </td>
                                    <td>
                                        <input type="radio" checked name="actionRights(allow_back_date_bill_activities)"
                                                id="actionRights_allow_back_date_bill_activities_N" value="N"/>
                                        <label for="actionRights_allow_back_date_bill_activities_N">No</label>
                                    </td>
                                </tr>	
                                 <tr>
                                     <td>Add Appointment Planner</td>
                                     <td>
                                         <input type="radio" name="actionRights(add_edit_appointment_plan)"
                                                 id="actionRights_add_edit_appointment_plan_A" value="A"/>
                                         <label for="actionRights_add_edit_appointment_plan_A">Yes</label>
                                         </td>
                                     <td>
                                         <input type="radio" checked name="actionRights(add_edit_appointment_plan)"
                                                id="actionRights_add_edit_appointment_plan_N" value="N"/>
                                         <label for="actionRights_add_edit_appointment_plan_N">No</label>
                                     </td>
                                 </tr>
                                 <tr>
                                     <td>Edit Appointment Planner</td>
                                     <td>
                                         <input type="radio" name="actionRights(edit_appointment_plan)"
                                                 id="actionRights_edit_appointment_plan_A" value="A"/>
                                         <label for="actionRights_edit_appointment_plan_A">Yes</label>
                                         </td>
                                     <td>
                                         <input type="radio" checked name="actionRights(edit_appointment_plan)"
                                                id="actionRights_edit_appointment_plan_N" value="N"/>
                                         <label for="actionRights_edit_appointment_plan_N">No</label>
                                     </td>
                                 </tr>
                                 <tr>
                                     <td>Allow Customize Sections</td>
                                     <td>
                                         <input type="radio" name="actionRights(allow_customize_sections)"
                                                 id="actionRights_allow_customize_sections_A" value="A"/>
                                         <label for="actionRights_allow_customize_sections_A">Yes</label>
                                         </td>
                                     <td>
                                         <input type="radio" checked name="actionRights(allow_customize_sections)"
                                                id="actionRights_allow_customize_sections_N" value="N"/>
                                         <label for="actionRights_allow_customize_sections_N">No</label>
                                     </td>
                                 </tr>
								<tr>
                                     <td>Modify Care Team Doctors</td>
                                     <td>
                                         <input type="radio" name="actionRights(modify_care_team)"
                                                 id="actionRights_modify_care_team_A" value="A"/>
                                         <label for="actionRights_modify_care_team_A">Yes</label>
                                         </td>
                                     <td>
                                         <input type="radio" checked name="actionRights(modify_care_team)"
                                                id="actionRights_modify_care_team_N" value="N"/>
                                         <label for="actionRights_modify_care_team_N">No</label>
                                     </td>
                                 </tr>
                                </tr>
                                <tr>
                                    <td>Disable Modifying Fields (Stock Entry)</td>
                                    <td>
                                        <input type="radio" name="actionRights(apply_strict_po_controls)"
                                                id="actionRights_apply_strict_po_controls_A" value="A"/>
                                        <label for="actionRights_apply_strict_po_controls_A">Yes</label>
                                    </td>
                                    <td>
                                        <input type="radio" checked name="actionRights(apply_strict_po_controls)"
                                                id="actionRights_apply_strict_po_controls_N" value="N"/>
                                        <label for="actionRights_apply_strict_po_controls_N">No</label>
                                    </td>
                                </tr>
                                <tr>
                                    <td>Mark Patient Duplicate</td>
                                    <td>
                                        <input type="radio" name="actionRights(mark_patient_duplicate)"
                                                id="actionRights_mark_patient_duplicate_A" value="A"/>
                                        <label for="actionRights_mark_patient_duplicate_A">Yes</label>
                                    </td>
                                    <td>
                                        <input type="radio" checked name="actionRights(mark_patient_duplicate)"
                                                id="actionRights_mark_patient_duplicate_N" value="N"/>
                                        <label for="actionRights_mark_patient_duplicate_N">No</label>
                                    </td>
                                </tr>
                                <tr>
                                   <td>Modify Referred by details</td>
                                   <td>
                                       <input type="radio" name="actionRights(modify_referred_by_details)"
                                             id="actionRights_modify_referred_by_details_A" value="A"/>
                                       <label for="actionRights_modify_referred_by_details_A">Yes</label>
                                   </td>
                                   <td>
                                       <input type="radio" checked name="actionRights(modify_referred_by_details)"
                                               id="actionRights_modify_referred_by_details_N" value="N"/>
                                       <label for="actionRights_modify_referred_by_details_N">No</label>
                                   </td>
                              </tr>
								<tr>
									<td>Multi-Visit Package Discontinuation</td>
									<td>
										<input type="radio" name="actionRights(multi_visit_package_discontinuation)"
											   id="actionRights_multi_visit_package_discontinuation_A" value="A"/>
										<label for="actionRights_multi_visit_package_discontinuation_A">Yes</label>
									</td>
									<td>
										<input type="radio" checked name="actionRights(multi_visit_package_discontinuation)"
											   id="actionRights_multi_visit_package_discontinuation_N" value="N"/>
										<label for="actionRights_multi_visit_package_discontinuation_N">No</label>
									</td>
								</tr>
								<tr>
                                    <td>Allow Finalize/Close GRN</td>
                                    <td>
                                        <input type="radio" name="actionRights(allow_finalize_close_grn)"
                                                id="actionRights_allow_finalize_close_grn_A" value="A"/>
                                        <label for="actionRights_allow_finalize_close_grn_A">Yes</label>
                                    </td>
                                    <td>
                                        <input type="radio" checked name="actionRights(allow_finalize_close_grn)"
                                                id="actionRights_allow_finalize_close_grn_N" value="N"/>
                                        <label for="actionRights_allow_finalize_close_grn_N">No</label>
                                    </td>
                                </tr>
								<tr>
									<td>Send manual diagnostics reports by email</td>
									<td>
										<input type="radio" name="actionRights(allow_send_diagnostics_reports)"
												id="actionRights_allow_send_diagnostics_reports_A" value="A"/>
										<label for="actionRights_allow_send_diagnostics_reports_A">Yes</label>
									</td>

									<td>
										<input type="radio" checked name="actionRights(allow_send_diagnostics_reports)"
												id="actionRights_allow_send_diagnostics_reports_N" value="N"/>
										<label for="actionRights_allow_send_diagnostics_reports_N">No</label>
									</td>
								</tr>
                                <tr>
                                    <td>Override Online Prior Auth Status</td>
                                    <td>
                                        <input type="radio" name="actionRights(override_online_prior_auth_status)"
                                                id="actionRights_override_online_prior_auth_status_A" value="A"/>
                                        <label for="actionRights_override_online_prior_auth_status_A">Yes</label>
                                    </td>
                                    <td>
                                        <input type="radio" checked name="actionRights(override_online_prior_auth_status)"
                                                id="actionRights_override_online_prior_auth_status_N" value="N"/>
                                        <label for="actionRights_override_online_prior_auth_status_N">No</label>
                                    </td>
                               </tr>
                               <tr>
                                    <td>Allow appointment overbooking</td>
                                    <td>
                                        <input type="radio" name="actionRights(allow_appt_overbooking)"
                                               id="actionRights_allow_appt_overbooking_A" value="A"/>
                                        <label for="actionRights_allow_appt_overbooking_A">Yes</label>
                                    </td>
                                    <td>
                                        <input type="radio" checked name="actionRights(allow_appt_overbooking)"
                                               id="actionRights_allow_appt_overbooking_N" value="N"/>
                                        <label for="actionRights_allow_appt_overbooking_N">No</label>
                                    </td>
                               </tr>
                                <tr>
                                  <td>Assign Encounter Type at Registration</td>
                                  <td>
                                         <input type="radio" name="actionRights(allow_assigning_encounter_type)"
                                         id="actionRights_allow_assigning_encounter_type_A" value="A"/>
                                         <label for="actionRights_allow_assigning_encounter_type_A">Yes</label>
                                  </td>
                                  <td>
                                       <input type="radio" checked name="actionRights(allow_assigning_encounter_type)"
                                       id="actionRights_allow_assigning_encounter_type_N" value="N"/>
                                       <label for="actionRights_allow_assigning_encounter_type_N">No</label>
                                  </td>
                                </tr>
								 <tr id="easyRewardzActionTrId">
                                    <td>Allow EasyRewardz Coupon Redemption</td>
                                    <td>
                                        <input type="radio" name="actionRights(allow_easyrewardz_coupon_redemption)"
                                                id="actionRights_allow_easyrewardz_coupon_redemption_A" value="A"/>
                                        <label for="actionRights_allow_easyrewardz_coupon_redemption_A">Yes</label>
                                    </td>
                                    <td>
                                        <input type="radio" checked name="actionRights(allow_easyrewardz_coupon_redemption)"
                                                id="actionRights_allow_easyrewardz_coupon_redemption_N" value="N"/>
                                        <label for="actionRights_allow_easyrewardz_coupon_redemption_N">No</label>
                                    </td>
                                </tr>
							</table>
						</fieldset>
				</td>
			</tr>
		</table>
		</td>
	</tr>

	<tr>
		<td>
		<table  class="screenActions" style="white-space:nowrap">
			<tr>
				<td >
					<button type="button" accesskey="S" onclick="doSubmit(); return false;">
						<b><u>S</u></b>ubmit</button>
					| <a href="#" onclick="resetValues(); return false;"> Reset</a>
					| <a href="${cpath}/pages/usermanager/UserDashBoard.do?_method=list"> User/Role List</a>
					<c:if test="${param.creatingNewRole eq null}">
						| <a href="${pageContext.request.contextPath}/pages/usermanager/PageStatsAction.do?method=getPageStatsScreen&roleName=${ifn:cleanURL(param.roleName)}&roleId=${ifn:cleanURL(param.roleId)}">Edit Role Page Stats</a>
						| <a href="${pageContext.request.contextPath}/master/EMRAccessRight.do?_method=add&rule_type=ROLE&role_id=${ifn:cleanURL(param.roleId)}&role_name=${ifn:cleanURL(param.roleName)}"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.emraccessrule"/></a>
					</c:if>
				</td>
			</tr>
		</table>
		</td>
	</tr>

</table>

</html:form>


<script>
		var screensTree = [
			<c:set var="grpSeparator" value="0"/>
			<c:forEach items="${screenConfig.screenGroupList}" var="group" varStatus="groupStatus">
			<c:if test="${groupAvlblMap[group.id] == 'Y'}">
				<c:set var="scrSeparator" value="0"/>
				<c:if test="${grpSeparator eq 1}">,</c:if>{ type:"Text", label:"${group.name}", title:"${group.name}", data:"${group.id}",
				children: [
					<c:forEach items="${group.screenList}" var="screen" varStatus="screenStatus">
						<c:if test="${preferences.modulesActivatedMap[screen.module] == 'Y'}">
							<c:if test="${scrSeparator eq 1}">,</c:if>{type:"Text", label:"${screen.name}", title:"${screen.name}", data:"${screen.id}" }
							<c:set var="scrSeparator" value="1"/>
						</c:if>
					</c:forEach>
				]}
				<c:set var="grpSeparator" value="1"/>
			</c:if>
			</c:forEach>
		];
		var easyRewardzModule = '${preferences.modulesActivatedMap['mod_easy_rewards_coupon']}';
</script>

<div id="debug" style="display:none; font-size: 7pt;"></div>
</body>

</html>

