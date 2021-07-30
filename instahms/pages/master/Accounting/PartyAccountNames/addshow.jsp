<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Insta HMS</title>
	<insta:link type="js" file="master/accounting/accountingprefs.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
		<style type="text/css">
			.field{width: 170px;}
		</style>

</head>
<body>
	<h1>Party Account Names</h1>

	<form action="PartyAccountNames.do" method="POST">

	<input type="hidden" name="method" value="update"/>

		<fieldset class="fieldSetBorder">
		<table class="formtable">
			<tr><td class="formlabel" >Individual Accounts for suppliers: </td>
				<td ><input type="radio" name="supplier_individual_accounts"
						id="supplier_individual_accounts" value="Y"
						${partyaccountnames.supplier_individual_accounts == 'Y'?'checked':''} > Yes
					<input type="radio" name="supplier_individual_accounts"
						id="supplier_individual_accounts" value="N"
						${partyaccountnames.supplier_individual_accounts == 'N'?'checked':''} > No
				</td>

			</tr>
			<tr><td class="formlabel" >Supplier Account Name: </td>
				<td ><input type="text" name="supplier_ac_name" id="supplier_ac_name" class="field" value="${partyaccountnames.supplier_ac_name}"/>
				</td>
				<td class="formlabel" >Supplier Account Prefix: </td>
				<td ><input type="text" name="supplier_ac_prefix" id="supplier_ac_prefix" class="field" value="${partyaccountnames.supplier_ac_prefix}"/>
				</td>
				<td class="formlabel" >Supplier Account Suffix: </td>
				<td ><input type="text" name="supplier_ac_suffix" id="supplier_ac_suffix" class="field" value="${partyaccountnames.supplier_ac_suffix}"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel" >Individual Accounts for TPAs: </td>
				<td ><input type="radio" name="tpa_individual_accounts"
						id="tpa_individual_accounts" value="Y"
						${partyaccountnames.tpa_individual_accounts == 'Y'?'checked':''} > Yes
					<input type="radio" name="tpa_individual_accounts"
						id="tpa_individual_accounts" value="N"
						${partyaccountnames.tpa_individual_accounts == 'N'?'checked':''} > No
				</td>
			</tr>
			<tr><td class="formlabel" >TPA Account Name: </td>
				<td ><input type="text" name="tpa_ac_name" id="tpa_ac_name" class="field" value="${partyaccountnames.tpa_ac_name}"/>
				</td>
				<td class="formlabel" >TPA Account Prefix: </td>
				<td ><input type="text" name="tpa_ac_prefix" id="tpa_ac_prefix" class="field" value="${partyaccountnames.tpa_ac_prefix}"/>
				</td>
				<td class="formlabel" >TPA Account Suffix: </td>
				<td ><input type="text" name="tpa_ac_suffix" id="tpa_ac_suffix" class="field" value="${partyaccountnames.tpa_ac_suffix}"/>
				</td>
			</tr>

			<tr><td class="formlabel" >Individual Accounts for Doctors: </td>
				<td ><input type="radio" name="doctor_individual_accounts"
						id="doctor_individual_accounts" value="Y"
						${partyaccountnames.doctor_individual_accounts == 'Y'?'checked':''} /> Yes
					<input type="radio" name="doctor_individual_accounts"
						id="doctor_individual_accounts" value="N"
						${partyaccountnames.doctor_individual_accounts == 'N'?'checked':''} /> No
				</td>
			</tr>
			<tr>
				<td class="formlabel" >Doctor Account Name: </td>
				<td >
					<input type="text" name="doctor_ac_name" id="doctor_ac_name" class="field" value="${partyaccountnames.doctor_ac_name}"/>
				</td>
				<td class="formlabel" >Doctor Account IP Prefix: </td>
				<td >
					<input type="text" name="doctor_ip_ac_prefix" id="doctor_ip_ac_prefix" class="field" value="${partyaccountnames.doctor_ip_ac_prefix}"/>
				</td>
				<td class="formlabel" >Doctor Account IP Suffix: </td>
				<td >
					<input type="text" name="doctor_ip_ac_suffix" id="doctor_ip_ac_suffix" class="field" value="${partyaccountnames.doctor_ip_ac_suffix}"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel" >Doctor Account OP Prefix: </td>
				<td >
					<input type="text" name="doctor_op_ac_prefix" id="doctor_op_ac_prefix" class="field" value="${partyaccountnames.doctor_op_ac_prefix}"/>
				</td>
				<td class="formlabel" >Doctor Account OP Suffix: </td>
				<td >
					<input type="text" name="doctor_op_ac_suffix" id="doctor_op_ac_suffix" class="field" value="${partyaccountnames.doctor_op_ac_suffix}"/>
				</td>
			</tr>

			<tr><td class="formlabel" >Individual Accounts for Prescribing Doctors: </td>
				<td ><input type="radio" name="prescribingdoctor_individual_accounts"
						id="prescribingdoctor_individual_accounts" value="Y"
						${partyaccountnames.prescribingdoctor_individual_accounts == 'Y'?'checked':''} /> Yes
					<input type="radio" name="prescribingdoctor_individual_accounts"
						id="prescribingdoctor_individual_accounts" value="N"
						${partyaccountnames.prescribingdoctor_individual_accounts == 'N'?'checked':''} /> No
				</td>
			</tr>
			<tr>
				<td class="formlabel" >Prescribing Doctor Account Name: </td>
				<td >
					<input type="text" name="prescribingdoctor_ac_name" id="prescribingdoctor_ac_name" class="field" value="${partyaccountnames.prescribingdoctor_ac_name}"/>
				</td>
				<td class="formlabel">Prescribing Doctor Account IP Prefix: </td>
				<td >
					<input type="text" name="prescribingdoctor_ip_ac_prefix" id="prescribingdoctor_ip_ac_prefix" class="field" value="${partyaccountnames.prescribingdoctor_ip_ac_prefix}"/>
				</td>
				<td class="formlabel" >Prescribing Doctor Account IP Suffix: </td>
				<td >
					<input type="text" name="prescribingdoctor_ip_ac_suffix" id="prescribingdoctor_ip_ac_suffix" class="field" value="${partyaccountnames.prescribingdoctor_ip_ac_suffix}"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Prescribing Doctor Account OP Prefix: </td>
				<td >
					<input type="text" name="prescribingdoctor_op_ac_prefix" id="prescribingdoctor_op_ac_prefix" class="field" value="${partyaccountnames.prescribingdoctor_op_ac_prefix}"/>
				</td>
				<td class="formlabel" >Prescribing Doctor Account OP Suffix: </td>
				<td >
					<input type="text" name="prescribingdoctor_op_ac_suffix" id="prescribingdoctor_op_ac_suffix" class="field" value="${partyaccountnames.prescribingdoctor_op_ac_suffix}"/>
				</td>
			</tr>
			<tr><td class="formlabel" >Individual Accounts for Referral: </td>
				<td ><input type="radio" name="referral_individual_accounts"
						id="referral_individual_accounts" value="Y"
						${partyaccountnames.referral_individual_accounts == 'Y'?'checked':''} /> Yes
					<input type="radio" name="referral_individual_accounts"
						id="referral_individual_accounts" value="N"
						${partyaccountnames.referral_individual_accounts == 'N'?'checked':''} /> No
				</td>
			</tr>
			<tr><td class="formlabel">Referral Account Name: </td>
				<td >
					<input type="text" name="referral_ac_name" id="referral_ac_name" class="field" value="${partyaccountnames.referral_ac_name}"/>
				</td>
				<td class="formlabel" >Referral Account IP Prefix: </td>
				<td >
					<input type="text" name="referral_ip_ac_prefix" id="referral_ip_ac_prefix" class="field" value="${partyaccountnames.referral_ip_ac_prefix}"/>
				</td>
				<td class="formlabel" >Referral Account IP Suffix: </td>
				<td >
					<input type="text" name="referral_ip_ac_suffix" id="referral_ip_ac_suffix" class="field" value="${partyaccountnames.referral_ip_ac_suffix}"/>
				</td>
			</tr>
				<td class="formlabel" >Referral Account OP Prefix: </td>
				<td >
					<input type="text" name="referral_op_ac_prefix" id="referral_op_ac_prefix" class="field" value="${partyaccountnames.referral_op_ac_prefix}"/>
				</td>
				<td class="formlabel" >Referral Account OP Suffix: </td>
				<td >
					<input type="text" name="referral_op_ac_suffix" id="referral_op_ac_suffix" class="field" value="${partyaccountnames.referral_op_ac_suffix}"/>
				</td>
			<tr>
			</tr>
			<tr><td class="formlabel" >Individual Accounts for OutHouses: </td>
				<td ><input type="radio" name="outhouse_individual_accounts"
						id="outhouse_individual_accounts" value="Y"
						${partyaccountnames.outhouse_individual_accounts == 'Y'?'checked':''} > Yes
					<input type="radio" name="outhouse_individual_accounts"
						id="outhouse_individual_accounts" value="N"
						${partyaccountnames.outhouse_individual_accounts == 'N'?'checked':''} > No
				</td>
			</tr>
			<tr><td class="formlabel" >Outhouse Account Name: </td>
				<td ><input type="text" name="outhouse_ac_name" id="outhouse_ac_name" class="field" value="${partyaccountnames.outhouse_ac_name}"/>
				</td>
				<td class="formlabel">Outhouse Account Prefix: </td>
				<td ><input type="text" name="outhouse_ac_prefix" id="outhouse_ac_prefix" class="field" value="${partyaccountnames.outhouse_ac_prefix}"/>
				</td>
				<td class="formlabel" >Outhouse Account Suffix: </td>
				<td ><input type="text" name="outhouse_ac_suffix" id="outhouse_ac_suffix" class="field" value="${partyaccountnames.outhouse_ac_suffix}"/>
				</td>
			</tr>

			<tr><td class="formlabel" >Individual Accounts for Miscellaneous: </td>
				<td ><input type="radio" name="misc_individual_accounts"
						id="misc_individual_accounts" value="Y"
						${partyaccountnames.misc_individual_accounts == 'Y'?'checked':''} > Yes
					<input type="radio" name="misc_individual_accounts"
						id="misc_individual_accounts" value="N"
						${partyaccountnames.misc_individual_accounts == 'N'?'checked':''} > No
				</td>
			</tr>
			<tr><td class="formlabel" >Miscellaneous Account Name: </td>
				<td ><input type="text" name="misc_ac_name" id="misc_ac_name" class="field" value="${partyaccountnames.misc_ac_name}"/>
				</td>
				<td class="formlabel" >Miscellaneous Account Prefix: </td>
				<td ><input type="text" name="misc_ac_prefix" id="misc_ac_prefix" class="field" value="${partyaccountnames.misc_ac_prefix}"/>
				</td>
				<td class="formlabel" >Miscellaneous Account Suffix: </td>
				<td ><input type="text" name="misc_ac_suffix" id="misc_ac_suffix" class="field" value="${partyaccountnames.misc_ac_suffix}"/>
				</td>
			</tr>
		</table>
	</fieldset>
	<div class="screenActions">
		<button type="submit" name="save" accesskey="S" onclick="return validatePartyAccounts();">
		<b><u>S</u></b>ave</button>
		 | <a href="javascript:void(0);" onclick="return dashboard('${pageContext.request.contextPath}');">Accounting Preferences</a>
	</div>
	</form>
</body>
</html>