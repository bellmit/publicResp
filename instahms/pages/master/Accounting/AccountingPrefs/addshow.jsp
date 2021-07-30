<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Accounting Preferences-Insta HMS</title>

	<c:set var="cpath" value="${pageContext.request.contextPath}" />
	<script>

		var eClaimModule = '${preferences.modulesActivatedMap['mod_eclaim']}';

		function enableDeptIncomeAccounts(obj) {
			var disable = obj.options[obj.options.selectedIndex].value !== 'Dept Based';
			document.accounting_prefs.pharmacy_income_dept.disabled = disable;
			document.accounting_prefs.incoming_test_income_dept.disabled = disable;
			document.accounting_prefs.outside_pat_income_dept.disabled = disable;
			document.accounting_prefs.phar_sales_to_hosp_patient.disabled = disable;
		}

		function getCentersWithoutCompNames() {
			var centersList = null;
			var ajaxobj = newXMLHttpRequest();
			var url = cpath + '/master/CenterMasterCompNames.do?_method=getCentersWithoutCompNames' ;
			ajaxobj.open("POST", url.toString(), false);
			ajaxobj.send(null);
			if (ajaxobj) {
				if (ajaxobj.readyState == 4) {
					if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
						eval("var cenlist =" + ajaxobj.responseText);
						if (!empty(cenlist))
							centersList = cenlist;
					}
				}
			}
			return centersList;
		}

		function getAccountGroupsWithoutCompNames() {
			var accGrpList = null;
			var ajaxobj = newXMLHttpRequest();
			var url = cpath + '/master/Accounting/AccountingGroupServiceReg.do?method=getAccountGroupsWithoutCompNames' ;
			ajaxobj.open("POST", url.toString(), false);
			ajaxobj.send(null);
			if (ajaxobj) {
				if (ajaxobj.readyState == 4) {
					if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
						eval("var acclist =" + ajaxobj.responseText);
						if (!empty(acclist))
							accGrpList = acclist;
					}
				}
			}
			return accGrpList;
		}

		function getCentersWithoutServiceReg() {
			var centersList = null;
			var ajaxobj = newXMLHttpRequest();
			var url = cpath + '/master/CenterMasterCompNames.do?_method=getCentersWithoutServiceReg' ;
			ajaxobj.open("POST", url.toString(), false);
			ajaxobj.send(null);
			if (ajaxobj) {
				if (ajaxobj.readyState == 4) {
					if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
						eval("var cenlist =" + ajaxobj.responseText);
						if (!empty(cenlist))
							centersList = cenlist;
					}
				}
			}
			return centersList;
		}

		function getAccountGroupsWithoutServiceReg() {
			var accGrpList = null;
			var ajaxobj = newXMLHttpRequest();
			var url = cpath + '/master/Accounting/AccountingGroupServiceReg.do?method=getAccountGroupsWithoutServiceReg' ;
			ajaxobj.open("POST", url.toString(), false);
			ajaxobj.send(null);
			if (ajaxobj) {
				if (ajaxobj.readyState == 4) {
					if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
						eval("var acclist =" + ajaxobj.responseText);
						if (!empty(acclist))
							accGrpList = acclist;
					}
				}
			}
			return accGrpList;
		}

		function doSubmit() {
			var newValue = 'Y';
			if (document.getElementsByName('all_centers_same_comp_name')[0].checked) {
				newValue = document.getElementsByName('all_centers_same_comp_name')[0].value;
			} else if (document.getElementsByName('all_centers_same_comp_name')[1].checked)  {
				newValue = document.getElementsByName('all_centers_same_comp_name')[1].value;
			}
			if ('${accounting_prefs.map.all_centers_same_comp_name}' == 'Y' && newValue == 'N') {

				var centers_without_comp_names = getCentersWithoutCompNames();
				var centers_without_service_reg = getCentersWithoutServiceReg();
				var account_groups_without_comp_names = getAccountGroupsWithoutCompNames();
				var account_groups_without_service_reg = getAccountGroupsWithoutServiceReg();

				if (!empty(centers_without_comp_names) && centers_without_comp_names.length > 0) {
					alert("All/Some of the Accounting Company names for centers are null. "+
						  "\nPlease enter the Comp. names then change the preference.");
					return false;
				}
				if (!empty(account_groups_without_comp_names) && account_groups_without_comp_names.length > 0) {
					alert("All/Some of the Accounting Company names for Accounting Groups are null. "+
						  "\nPlease enter the Accounting Company names then change the preference.");
					return false;
				}

				if (!empty(eClaimModule) && eClaimModule == "Y") {
					if (!empty(centers_without_service_reg) && centers_without_service_reg.length > 0) {
						alert("All/Some of the Service Reg No. for centers are null. "+
							  "\nPlease enter the Service Reg No. then change the preference.");
						return false;
					}
					if (!empty(account_groups_without_service_reg) && account_groups_without_service_reg.length > 0) {
						alert("All/Some of the Service Reg No. for Accounting Groups are null. "+
							  "\nPlease enter the Service Reg No. then change the preference.");
						return false;
					}
				}
			}
			return true;
		}
	</script>

</head>

<body onload="enableDeptIncomeAccounts(document.accounting_prefs.cost_center_basis);">
	<div class="pageHeader">Accounting Preferences</div>

	<insta:feedback-panel/>

	<form action="AccountingPrefs.do" method="POST" name="accounting_prefs">

		<input type="hidden" name="method" value="update"/>

		<fieldset class="fieldSetBorder">
			<table class="formtable" >
				<tr>
					<td class="formlabel">Separate Pharmacy: </td>
					<td ><input type="radio" name="pharmacy_separate_entity" id="pharmacy_separate_entity" value="N"
							${accounting_prefs.map.pharmacy_separate_entity == 'N'?'checked':''} /> No
						<input type="radio" name="pharmacy_separate_entity" id="pharmacy_separate_entity" value="Y"
							${accounting_prefs.map.pharmacy_separate_entity == 'Y'?'checked':''} /> Yes
							<img class="imgHelpText" title="Separate Pharmacy" src="${cpath}/images/help.png"/>
					</td>
					<td class="formlabel">Create inter-company Vouchers: </td>
					<td style="padding-top: 0px;"><input type="radio" name="inter_co_vouchers" id="inter_co_vouchers" value="N"
							${accounting_prefs.map.inter_co_vouchers == 'N'?'checked':''} /> No
						<input type="radio" name="inter_co_vouchers" id="inter_co_vouchers" value="Y"
							${accounting_prefs.map.inter_co_vouchers == 'Y'?'checked':''} /> Yes
							<img class="imgHelpText" title="Create inter-company Vouchers" src="${cpath}/images/help.png"/>
					</td>
					<td class="formlabel">Separate Account for each Outgoing VAT%: </td>
					<td style="padding-top: 0px;"><input type="radio" name="separate_acc_for_out_vat" id="separate_acc_for_out_vat" value="N"
							${accounting_prefs.map.separate_acc_for_out_vat == 'N'?'checked':''}> No
						<input type="radio" name="separate_acc_for_out_vat" id="separate_acc_for_out_vat"
							value="Y" ${accounting_prefs.map.separate_acc_for_out_vat == 'Y'?'checked':''}> Yes
							<img class="imgHelpText" title="Separate Account for each Outgoing VAT%" src="${cpath}/images/help.png"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel" >Separate Purchase Account for each VAT%: </td>
					<td style="padding-top: 0px;"><input type="radio" name="separate_purcharse_acc_for_vat" id="separate_purcharse_acc_for_vat" value="N"
							${accounting_prefs.map.separate_purcharse_acc_for_vat == 'N'?'checked':''}> No
						<input type="radio" name="separate_purcharse_acc_for_vat" id="separate_purcharse_acc_for_vat"
							value="Y" ${accounting_prefs.map.separate_purcharse_acc_for_vat == 'Y'?'checked':''}> Yes
							<img class="imgHelpText" title="Separate Purchase Account for each VAT%" src="${cpath}/images/help.png"/>
					</td>
					<td class="formlabel">Separate Account for each Incoming VAT%: </td>
					<td style="padding-top: 0px;"><input type="radio" name="separate_acc_for_in_vat" id="separate_acc_for_in_vat" value="N"
							${accounting_prefs.map.separate_acc_for_in_vat == 'N'?'checked':''}> No
						<input type="radio" name="separate_acc_for_in_vat" id="separate_acc_for_in_vat"
							value="Y" ${accounting_prefs.map.separate_acc_for_in_vat == 'Y'?'checked':''}> Yes
							<img class="imgHelpText" title="Separate Account for each Incoming VAT%" src="${cpath}/images/help.png"/>
					</td>
					<td class="formlabel">Separate Sales Account for each VAT%: </td>
					<td style="padding-top: 0px;"><input type="radio" name="separate_sales_acc_for_vat" id="separate_sales_acc_for_vat" value="N"
							${accounting_prefs.map.separate_sales_acc_for_vat == 'N'?'checked':''}> No
						<input type="radio" name="separate_sales_acc_for_vat" id="separate_sales_acc_for_vat"
							value="Y" ${accounting_prefs.map.separate_sales_acc_for_vat == 'Y'?'checked':''}> Yes
							<img class="imgHelpText" title="Separate Sales Account for each VAT%" src="${cpath}/images/help.png"/>
					</td>
				</tr>


				<tr>
					<td class="formlabel">Pharmacy Sales Account Include VAT%: </td>
					<td><input type="radio" name="pharmacy_sales_acc_include_vat" id="pharmacy_sales_acc_include_vat" value="N"
							${accounting_prefs.map.pharmacy_sales_acc_include_vat == 'N'?'checked':''}> No
						<input type="radio" name="pharmacy_sales_acc_include_vat" id="pharmacy_sales_acc_include_vat"
							value="Y" ${accounting_prefs.map.pharmacy_sales_acc_include_vat == 'Y'?'checked':''}> Yes
							<img class="imgHelpText" title="Pharmacy Sales Account Include VAT%" src="${cpath}/images/help.png"/>
					</td>
				</tr>
				<tr>
					<td>IP Income Account Prefix </td>
					<td><input type="text" name="ip_income_acc_prefix" id="ip_income_acc_prefix" value="${accounting_prefs.map.ip_income_acc_prefix}"/></td>
					<td>IP Income Account Suffix </td>
					<td><input type="text" name="ip_income_acc_suffix" id="ip_income_acc_suffix" value="${accounting_prefs.map.ip_income_acc_suffix}"/></td>
					<td>OP Income Account Prefix </td>
					<td><input type="text" name="op_income_acc_prefix" id="op_income_acc_prefix" value="${accounting_prefs.map.op_income_acc_prefix}"/></td>
				</tr>
				<tr>
					<td>OP Income Account Suffix </td>
					<td><input type="text" name="op_income_acc_suffix" id="op_income_acc_suffix" value="${accounting_prefs.map.op_income_acc_suffix}"/></td>
					<td>Others Income Account Prefix </td>
					<td><input type="text" name="others_income_acc_prefix" id="others_income_acc_prefix" value="${accounting_prefs.map.others_income_acc_prefix}"/></td>
					<td>Others Income Account Suffix </td>
					<td><input type="text" name="others_income_acc_suffix" id="others_income_acc_suffix" value="${accounting_prefs.map.others_income_acc_suffix}"/></td>
				</tr>
				<tr>
					<td class="formlabel">Bill Reference in Receipts: </td>
					<td>
						<select name="bill_reference" class="dropdown">
							<option value="bill_no" ${accounting_prefs.map.bill_reference == 'bill_no' ? 'selected' : ''}>Bill No. & Patient Name</option>
							<option value="batch_no" ${accounting_prefs.map.bill_reference == 'batch_no' ? 'selected' : ''}>Counter Name & Batch No.</option>
						</select>
					</td>
					<td class="formlabel">Cost Center Basis: </td>
					<td>
						<select name="cost_center_basis" id="cost_center_basis" class="dropdown" onclick="enableDeptIncomeAccounts(this);">
							<option value="None" ${accounting_prefs.map.cost_center_basis == 'None' ? 'selected' : ''}>None</option>
							<option value="Center Based" ${accounting_prefs.map.cost_center_basis == 'Center Based' ? 'selected' : ''}>Center Based</option>
							<option value="Dept Based" ${accounting_prefs.map.cost_center_basis == 'Dept Based' ? 'selected' : ''}>Admitting Dept. Based</option>
						</select>
					</td>
					<td class="formlabel">Tally Unique ID Prefix: </td>
					<td ><input type="text" name="tally_guid_prefix" class="field" id="tally_guid_prefix" value="${accounting_prefs.map.tally_guid_prefix}"/>
				</tr>
				<tr>
					<td class="formlabel">Include Item level discounts: </td>
					<td><input type="radio" name="single_acc_for_item_and_bill_discounts" id="single_acc_for_item_and_bill_discounts" value="N"
							${accounting_prefs.map.single_acc_for_item_and_bill_discounts == 'N'?'checked':''}> No
						<input type="radio" name="single_acc_for_item_and_bill_discounts" id="single_acc_for_item_and_bill_discounts"
							value="Y" ${accounting_prefs.map.single_acc_for_item_and_bill_discounts == 'Y'?'checked':''}> Yes
							<img class="imgHelpText" title="if Include Item level discounts = Yes, it will post along with Bill
								discounts account head." src="${cpath}/images/help.png"/></td>
					<td class="formlabel">Pharmacy Income Dept.</td>
					<td>
						<insta:selectdb name="pharmacy_income_dept" table="department" displaycol="dept_name"
							valuecol="dept_id" value="${accounting_prefs.map.pharmacy_income_dept}"/>
					</td>
					<td class="formlabel">Incoming Tests Income Dept.</td>
					<td>
						<insta:selectdb name="incoming_test_income_dept" table="department" displaycol="dept_name"
							valuecol="dept_id" value="${accounting_prefs.map.incoming_test_income_dept}"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Outside Patients Income Dept.</td>
					<td>
						<insta:selectdb name="outside_pat_income_dept" table="department" displaycol="dept_name"
							valuecol="dept_id" value="${accounting_prefs.map.outside_pat_income_dept}"/>
					</td>
					<td class="formlabel">Pharmacy Sales to Hospital Patient.</td>
					<td><select name="phar_sales_to_hosp_patient" class="dropdown">
							<option value="Admitting Dept" ${accounting_prefs.map.phar_sales_to_hosp_patient == 'Admitting Dept' ? 'selected' : ''}>Admitting Department</option>
							<option value="Pharmacy Dept" ${accounting_prefs.map.phar_sales_to_hosp_patient == 'Pharmacy Dept' ? 'selected' : ''}>Pharmacy Department</option>
						</select>
					</td>
					<td class="formlabel">All Centers Same Company: </td>
					<td>
						<input type="radio" name="all_centers_same_comp_name" id="all_centers_same_comp_name" value="N"
							${accounting_prefs.map.all_centers_same_comp_name == 'N'?'checked':''}> No
						<input type="radio" name="all_centers_same_comp_name" id="all_centers_same_comp_name"
							value="Y" ${accounting_prefs.map.all_centers_same_comp_name == 'Y'?'checked':''}> Yes
							<img class="imgHelpText" title="If all centers same company is YES then allowing to export it by account group,
							else allowed centerwise AND accountgroup wise." src="${cpath}/images/help.png"/>
						</td>
				</tr>
			</table>
		</fieldset>

		<c:url var="voucherUrl" value="VoucherTypes.do">
			<c:param name="method" value="show"/>
		</c:url>
		<c:url var="sAccountUrl" value="SpecialAccountNames.do">
			<c:param name="method" value="show"/>
		</c:url>
		<c:url var="pAccountUrl" value="PartyAccountNames.do">
			<c:param name="method" value="show"/>
		</c:url>
		<c:url var="purAccountsUrl" value="PurchaseAccounts.do">
			<c:param name="_method" value="list"/>
		</c:url>

		<div class="screenActions" >
			<button type="submit" name="save" accesskey="S" onclick="return doSubmit();"><b><u>S</u></b>ave</button>
			| <a href="${voucherUrl}" title="Voucher Types">Voucher Types</a>
			| <a href="${sAccountUrl}" title="Special Account Names">Special Account Names</a>
			| <a href="${pAccountUrl}" title="Party Account Names">Party Account Names</a>
			| <a href="${purAccountsUrl}" title="Add Account Prefixes for Category">Sales/Purchases A/c Prefixes</a>
		</div>

	</form>

</body>
</html>
