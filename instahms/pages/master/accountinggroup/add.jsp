<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page import="com.insta.hms.master.URLRoute" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.ACCOUNTING_GROUP_PATH %>"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add Account Group - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>

		var eClaimModule = '${preferences.modulesActivatedMap['mod_eclaim']}';

		function doClose() {
			window.location.href = "${cpath}/${pagePath}/list.htm?method=list&sortReverse=false";
		}
		function focus(){
			document.forms[0].account_group_name.focus();
			if ('${bean.account_group_id}' == '1') document.forms[0].status.disabled = true;
		}

		var allCentersSameCompName = '${acc_prefs[0].all_centers_same_comp_name}';

		function doSubmit() {
			document.getElementById('accounting_company_name').value = trim(document.getElementById('accounting_company_name').value);
			document.getElementById('account_group_service_reg_no').value = trim(document.getElementById('account_group_service_reg_no').value);
			var accountingCompName = document.getElementById('accounting_company_name').value;
			var serviceRegNo = document.getElementById('account_group_service_reg_no').value;

			if ('${bean.account_group_id}' == '1') {
				if (allCentersSameCompName == 'Y') {
					if (accountingCompName == '') {
						alert('Please enter the Accounting Company Name.');
						document.getElementById('accounting_company_name').focus();
						return false;
					}
					if (!empty(eClaimModule) && eClaimModule == "Y" && serviceRegNo == "") {
						alert("Please enter the Service Reg No.");
						document.getElementById('account_group_service_reg_no').focus();
						return false;
					}
				}
			} else {
				if (accountingCompName == '') {
					alert("Please enter the Accounting Company Name.");
					document.getElementById('accounting_company_name').focus();
					return false;
				}
				if (!empty(eClaimModule) && eClaimModule == "Y" && serviceRegNo == "") {
					alert("Please enter the Service Reg No.");
					document.getElementById('account_group_service_reg_no').focus();
					return false;
				}
			}
			/*if (allCentersSameCompName == 'N' && '${bean.account_group_id}' != '1') {
				if (accountingCompName == '') {
					alert("Accounting Preferences: All Centers Same Comp. preference is No. \nHence Accounting Company Name is required.");
					document.getElementById('accounting_company_name').focus();
					return false;
				}
				if (!empty(eClaimModule) && eClaimModule == "Y" && serviceRegNo == "") {
					alert("Accounting Preferences: All Centers Same Comp. preference is No. \nHence Service Reg No. is required.");
					document.getElementById('account_group_service_reg_no').focus();
					return false;
				}
			}*/
		}
	</script>
</head>
<body onload="focus()">
 <c:set var="actionUrl" value="${cpath}/${pagePath}/create.htm"/>
	

	<form action="${actionUrl}" method="POST">

		<input type="hidden" name="method" value="create">

		<h1>Add Account Group</h1>

		<insta:feedback-panel/>

		<fieldset class="fieldSetBorder">
			<table class="formtable">
				<tr>
					<td class="formlabel">Account Group: </td>
					<td>
						 <input type="text" name="account_group_name" value="${bean.account_group_name}"
						 	onblur="capWords(account_group_name)" class="required validate-length" length="200"
						 	title="Account Group Name is required and max length of name can be 200" />
					</td>
					<td class="formlabel">Service Reg No:</td>
					<td><input type="text" name="account_group_service_reg_no" id="account_group_service_reg_no"
							value="${bean.account_group_service_reg_no}"
							class="validate-length"
							length="200" title="Service reg no. max length is 200" ></td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td class="formlabel" style="white-space:nowrap">Inter-company Account Name: </td>
					<td><input type="text" name="inter_comp_acc_name" value="${bean.inter_comp_acc_name}" onblur="capWords(inter_comp_acc_name)" class="required validate-length"
							length="50" title="Account Name is required and max length of name can be 50" ></td>
					<td class="formlabel">Accounting Company Name: </td>
					<td><input type="text" name="accounting_company_name" id="accounting_company_name" value="${bean.accounting_company_name}" class="validate-length"
						onblur="capWords(accounting_company_name)" length="200" title="max length of Accounting Comp. Name can be 200 chars"/></td>
				</tr>
				<tr>
					<td class="formlabel">Status: </td>
					<td><insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
				</tr>

				<c:if test="${bean.account_group_id == 1}">
					<tr>
					 <td colspan="3" style="white-space:nowrap">***  '${bean.account_group_name}' is default Account Group you can't update status. ***</td>
					</tr>
				</c:if>
			</table>
		</fieldset>

		<table class="screenActions">
			<tr>
				<td >
					<button type="submit" accesskey="S" onclick="return doSubmit();"><b><u>S</u></b>ave</button>
				</td>
				<td>&nbsp;|&nbsp;</td>
				<td>
					<a href="javascript:void(0)" onclick="doClose();">Accounting Group List</a>
				</td>
			</tr>
		</table>

	</form>
</body>
</html>
