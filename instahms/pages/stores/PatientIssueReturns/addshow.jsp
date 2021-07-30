<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="prefbarcode" value="<%= GenericPreferencesDAO.getGenericPreferences().getBarcodeForItem() %>" />
<c:set var="allowDecimalsForQty" value='<%=GenericPreferencesDAO.getAllPrefs().get("allow_decimals_in_qty_for_issues")%>' />
<c:set var="showCharges" value="${roleId == 1 || roleId == 2 ? 'A' : actionRightsMap.view_all_rates}"/>
<c:set var="allowRateChange" value="${roleId == 1 || roleId == 2 ? 'A' : actionRightsMap.allow_ratechange}"/>
<c:set var="storesList" value="${ifn:JSONlist('stores')}"/>
<head>
	<title><insta:ltext key="salesissues.patientissuereturns.addshow.title"/> </title>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="stores/patientissue_returns.js"/>
	<insta:link type="script" file="stores/storescommon.js" />
	<insta:link type="js" file="stores/loginDialog.js" />
	<insta:link type="js" file="stores/storeshelper.js" />

<script type="text/javascript">

	var dialog = '';
	var itemsListJSON = ${issuedItemsJSON};
	var visitissuedClaimJSON = ${visit_issued_claim_details};
	var patientPlanDetails = ${patient_plan_details};
	var patIndentDetailsJSON = '';
	var indentsListJSON = '';
	var returnIndentItemsJSON = '';
	<c:if test="${returnIndentItems != null}">
		returnIndentItemsJSON = ${returnIndentItems};
	</c:if>
	<c:if test="${patIndentDetailsJSON != null}">
		patIndentDetailsJSON = ${patIndentDetailsJSON};
		indentsListJSON = ${indentsListJSON};
	</c:if>
	var jStores = ${storesList};
	var stockNegative = '${prefs.stock_negative_sale}';
	var allowDecimalsForQty = '${allowDecimalsForQty}';
	var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
	var expiryWarnDays = '${prefs.warn_expiry}';
	var billsJSON = ${billsJSON};
	var oItemAutoComp ;
	var itemNamesArray = new Array();
	var visitSelected = ${param.patient_id != null};
	var gMedicineBatches = {};
	<c:if test="${allBatches != null}">
	var medBatches = ${allBatches};
	</c:if>
	var prefbarcode = '${prefbarcode}';
	var showCharges = '${showCharges}';
	var returnNo = 0;
	<c:if test="${not empty param.returnNo}">
		returnNo = ${ifn:cleanJavaScript(param.returnNo)};
	</c:if>
	var gAllowExpiredSale = '${prefs.sale_expiry}';
	var issuedItemsMedicineWiseMapJSON = ${issuedItemsMedicineWiseMapJSON};
	var deptId = '${pharmacyStoreId}';
	var gRoleId = '${roleId}';
	var isSharedLogIn = '${ifn:cleanJavaScript(isSharedLogIn)}';
	var itemTaxDetails = JSON.parse('${ifn:cleanJavaScript(itemTaxDetails)}');
	var subgroupNamesList = JSON.parse('${ifn:cleanJavaScript(subGroupListJSON)}'); 
	var groupListJSON = JSON.parse('${ifn:cleanJavaScript(groupListJSON)}');
	var packages =  ${packages};
</script>
<insta:js-bundle prefix="sales.issues"/>
</head>
<c:set var="saveText">
<insta:ltext key="salesissues.patientissuereturns.addshow.saveText"/>
</c:set>
<body onload="init();">
	 <div id="storecheck" style="display: block;" >
	<h1 style="float: left; "><insta:ltext key="salesissues.patientissuereturns.addshow.patientissuereturns"/></h1>
	<insta:patientsearch searchType="visit" searchUrl="StockPatientReturn.do" buttonLabel="Find"
		searchMethod="show" fieldName="patient_id" activeOnly="true"/>

	<div><insta:feedback-panel/></div>
	<insta:patientdetails patient="${patient}" showClinicalInfo="false"/>
	<form name="patientIssueReturnForm" method="post" action="StockPatientReturn.do">
		<input type="hidden" name="_method" value="create"/>

		<input type="hidden" name="plan_id" id="plan_id" value="${patient.plan_id }"/>
		<input type="hidden" name="visit_type" id="visit_type" value="${patient.visit_type }"/>
		<input type="hidden" name="isSharedLogIn" value="${ifn:cleanHtmlAttribute(isSharedLogIn)}"/>
		<input type="hidden" name="authUser" value=""/>

		<fieldset class="fieldSetBorder" >
		<legend class="fieldSetLabel" ><insta:ltext key="salesissues.patientissuereturns.addshow.issuereturnsdetails"/></legend>
		<div >
			<table class="formtable">
				<tr>
					<td class="formlabel"><insta:ltext key="salesissues.patientissuereturns.addshow.store"/>:</td>
						<c:choose>
							<c:when test="${roleId == 1 || roleId == 2 || (multiStoreAccess == 'A')}">
							<td>
								<insta:userstores username="${userid}" elename="dept_to" onchange="onChangeStore(this.value);" id="dept_to" val="${indentStore}"/>
							</td>
							</c:when>
							<c:otherwise>
								<td><b>${ifn:cleanHtml(store_name)}</b>
								<input type = "hidden" name="dept_to" id ="dept_to" value="${ifn:cleanHtmlAttribute(store_id)}" />
							</td>
							</c:otherwise>
						</c:choose>
					<td class="formlabel"><insta:ltext key="salesissues.patientissuereturns.addshow.reason"/>:</td>
					<td>
						<input type="text" name="reference"/>
						<input type="hidden" name="returned_by" value="${patient.patient_id }"/>
						<input type="hidden" name="username" value="${ifn:cleanHtmlAttribute(userId)}"/>
					</td>
				</tr>
			</table>
		</div>
		</fieldset>
		<fieldset class="fieldSetBorder" id="indentFieldSet">
		<legend class="fieldSetLabel"><insta:ltext key="salesissues.patientissue.addshow.indentpackagedetails"/></legend>
			<div id="indentDetailsDiv" style="display:none">
				<table id="indentInfo">
					<tr>
						<th align="left" style="padding-right: 5px"><insta:ltext key="salesissues.patientissuereturns.addshow.indent"/></th>
						<th align="left" style="padding-right: 5px"><insta:ltext key="salesissues.patientissuereturns.addshow.indentdetails"/></th>
						<th align="left"><insta:ltext key="salesissues.patientissuereturns.addshow.dispensestatus"/></th>
					</tr>
						<tr id="prescTemplateRow" style="display:none" >
							<td style="padding-right: 5px"></td>
							<td style="padding-right: 5px"><a target="_blank" href=""><insta:ltext key="salesissues.patientissuereturns.addshow.view"/></a></td>
							<td>
								<select name="dispensedMedicine" class="dropdown">
									<option value="partiall"><insta:ltext key="salesissues.patientissuereturns.addshow.closedispensed"/></option>
									<option value="full" selected><insta:ltext key="salesissues.patientissuereturns.addshow.closefullydispensed"/></option>
								</select>
								<input type="hidden" name="patientIndentNoRef" value=""/>
							</td>
						</tr>
				</table>
			</div>
			<div id ="packageDetailsDiv" style="${patIndentDetailsJSON == null ? '' : 'display:none' }">
				<table id ="packageInfo">
					<tr>
						<td class="formlabel" style="padding:5px;" > <insta:ltext key="salesissues.patientissue.addshow.activepackages"/>:</td>
						<td>
							<select name="activePackage" id="activePackage" style="width:150" class="dropdown" onchange="onChangePackage(this.value)">
								<option value="">--- Select ---</option>
							</select>
							<input type="hidden" name="package_id" id="package_id"/>
							<input type="hidden" name="pkg_charge_id_ref"/>
						</td>
					</tr>
				</table>
			</div>
		</fieldset>

		<fieldset class="fieldSetBorder" >
		<legend class="fieldSetLabel" ><insta:ltext key="salesissues.patientissuereturns.addshow.itemslist"/></legend>
		<div class="resultlist">
		<table id="indentItemListTab" class="detailList"  cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<th><insta:ltext key="salesissues.patientissuereturns.addshow.itemname"/></th>
			<th><insta:ltext key="salesissues.patientissuereturns.addshow.controltype"/></th>
			<th><insta:ltext key="salesissues.patientissuereturns.addshow.batch.or.serial.no"/></th>
			<th><insta:ltext key="salesissues.patientissuereturns.addshow.exp.date"/></th>
			<th><insta:ltext key="salesissues.patientissuereturns.addshow.issueqty"/></th>
			<th><insta:ltext key="salesissues.patientissuereturns.addshow.returnqty"/></th>
			<th><insta:ltext key="salesissues.patientissuereturns.addshow.packageuom"/></th>
			<c:if test="${showCharges == 'A'}">
				<th><insta:ltext key="salesissues.patientissuereturns.addshow.rate"/></th>
				<th><insta:ltext key="salesissues.patientissuereturns.addshow.unitrate"/></th>
				<th><insta:ltext key="salesissues.patientissuereturns.addshow.returnamt"/></th>
				<th><insta:ltext key="salesissues.patientissuereturns.addshow.tax"/></th>
				<th><insta:ltext key="salesissues.patientissuereturns.addshow.pat.amt"/></th>
				<th><insta:ltext key="salesissues.patientissuereturns.addshow.pat.tax"/></th>
				<th><insta:ltext key="salesissues.patientissuereturns.addshow.pri.claim"/></th>
				<th><insta:ltext key="salesissues.patientissuereturns.addshow.sec.claim"/></th>
			</c:if>
			<th></th>
			<th></th>
		</tr>
		<tr style="display:none">
			<td class="formlabel"><label></label>
				<input type="hidden" name="medicine_id"/>
				<input type="hidden" name="medicine_name"/>
				<input type="hidden" name="batch_no"/>
				<input type="hidden" name="qty"/>
				<input type="hidden" name="rate"/>
				<input type="hidden" name="discount"/>
				<input type="hidden" name="item_unit"/>
				<input type="hidden" name="rtn_pkg_size"/>
				<input type="hidden" name="item_barcode_id"/>
				<input type="hidden" name="patient_indent_no"/>
				<input type="hidden" name="indent_item_id"/>
				<input type="hidden" name="patient_amount"/>
				<input type="hidden" name="patient_percent"/>
				<input type="hidden" name="patient_amount_cap"/>
				<input type="hidden" name="insurance_claim_amt"/>
				<input type="hidden" name="pri_insurance_claim_amt"/>
				<input type="hidden" name="sec_insurance_claim_amt"/>
				<input type="hidden" name="insurance_category_id"/>
				<input type="hidden" name="unit_rate"/>
				<input type="hidden" name="patient_amount_ref"/>
				<input type="hidden" name="patient_percent_ref"/>
				<input type="hidden" name="patient_amount_cap_ref"/>
				<input type="hidden" name="issue_units"/>
				<input type="hidden" name="package_uom"/>
				<input type="hidden" name="issue_base_unit"/>
				<input type="hidden" name="item_batch_id"/>
				<input type="hidden" name="issued_qty"/>
				<input type="hidden" name="tax_rate"/>
				<input type="hidden" name="tax_amount"/>
				<input type="hidden" name="original_tax_amount"/>
				<c:forEach items="${groupList}" var="group">
					<input type="hidden" name="taxname${group.item_group_id}" value="" />
					<input type="hidden" name="taxrate${group.item_group_id}" value="" />
					<input type="hidden" name="taxamount${group.item_group_id}" value="" />
					<input type="hidden" name="originaltaxamount${group.item_group_id}" value="" />
					<input type="hidden" name="taxsubgroupid${group.item_group_id}" value="" />
				</c:forEach>
			</td>
			<td class="formlabel"></td>
			<td class="formlabel"></td>
			<td class="formlabel"></td>
			<td class="formlabel"></td>
			<td class="formlabel"></td>
			<td class="formlabel"></td>
			<c:if test="${showCharges == 'A'}">
				<td class="formlabel"></td>
				<td class="formlabel"></td><!-- Added for total tax  -->
				<td class="formlabel"></td>
				<td class="formlabel"></td><!-- Added for patient tax -->
				<td class="formlabel"></td>
				<td class="formlabel"></td>
				<td class="formlabel"></td>
				<td class="formlabel"></td>
			</c:if>
			<td>
				<c:if test="${patIndentDetailsJSON == null}">
				<a href="javascript:void(0)" title='<insta:ltext key="salesissues.patientissuereturns.addshow.cancelissuereturn"/>' >
					<img src="${cpath}/icons/delete.gif" class="button" onclick="deleteReturn(this)"/>
				</a>
				</c:if>
			</td>
			<td>
				<c:if test="${patIndentDetailsJSON == null}">
				<a href="javascript:void(0)" name="btnEditReturns" id="btnEditReturns"
					title='<insta:ltext key="salesissues.patientissuereturns.addshow.editindentdetails"/>' onclick="getItemDialog(this);">
					<img src="${cpath}/icons/Edit.png" class="button" />
				</a>
				</c:if>
			</td>
		</tr>
		</table>
		<c:if test="${patIndentDetailsJSON == null}">
			<table width="100%" class="addButton">
				<tr>
					<td width="100%"></td>
					<td style="width: 20px">
						<button type="button" name="btnAddItem" id="btnAddItem" title='<insta:ltext key="salesissues.patientissuereturns.addshow.addindent"/>'
							onclick="getItemDialog(this);" accesskey="+" class="imgButton">
						<img src="${cpath}/icons/Add.png">
						</button>
					</td>
				</tr>
			</table>
		</c:if>
	</div>
	</fieldset>

	<div id="creditbill">
		<label ><insta:ltext key="salesissues.patientissuereturns.addshow.billlater"/></label>
		<select name="bill_no" id="bill_no" class="dropdown" onchange="reCalculateInsAmt();">
			<c:forEach items="${bills}" var="bill">
				<option value="${bill.bill_no}">${bill.bill_no}</option>
			</c:forEach>
		</select>
		<c:if test="${showCharges == 'A'}">
			&nbsp;&nbsp;<label class="formlabel"><insta:ltext key="salesissues.patientissuereturns.addshow.totalreturnamount"/>:&nbsp;&nbsp;</label>
			<label id="totAmt" class="forminfo" style="text-align:right;">0.00</label>
			&nbsp;&nbsp;<label class="formlabel"><insta:ltext key="salesissues.patientissuereturns.addshow.totaltax"/>:&nbsp;&nbsp;</label>
			<label id="totTax" class="forminfo" style="text-align:right;">0.00</label>
			&nbsp;&nbsp;<label class="formlabel"><insta:ltext key="salesissues.patientissuereturns.addshow.patientportion"/>:&nbsp;&nbsp;</label>
			<label id="totPatAmt" class="forminfo">0.00</label>
			&nbsp;&nbsp;<label class="formlabel"><insta:ltext key="salesissues.patientissuereturns.addshow.patienttax"/>:&nbsp;&nbsp;</label>
			<label id="totPatTax" class="forminfo">0.00</label>
			&nbsp;&nbsp;<label class="formlabel"><insta:ltext key="salesissues.patientissuereturns.addshow.claimamount"/>:&nbsp;&nbsp;</label>
			<label id="totClaimAmt" class="forminfo">0.00</label>
		</c:if>
	</div>

	 <div id="dialog" style="visibility:hidden">
				<div class="bd">
					<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel"><insta:ltext key="salesissues.patientissuereturns.addshow.additem"/></legend>
					<table class="formtable">

							<c:if test="${prefbarcode eq 'Y'}">
							<tr>
								<td class="formlabel"><insta:ltext key="salesissues.patientissuereturns.addshow.itembarcode"/>: </td>
								<td ><input type="text" name="eItemBarcode" onchange="getItemBarCodeDetails(this.value);" ></td>
							</tr>
							</c:if>
							<tr>
							<td class="formlabel"><insta:ltext key="salesissues.patientissuereturns.addshow.items"/>:</td>
							<td valign="top">
								<div id="item_wrapper" style="width: 17em; padding-bottom:0.2em; z-index: 9000;">
									<input type="text" name="items" id="items" style="width: 16em"  />
									<div id="item_dropdown"></div>
								</div>
								<span id="kithyper" style="display:none;margin-left: 17em;"><a href="#" onclick="kitDetailsList(); return false;"  title="Show Kit details"><insta:ltext key="salesissues.patientissuereturns.addshow.kitdetails"/></a></span>
								<c:if test="${prefbarcode ne 'Y'}">
									<input type="hidden" name="barCodeId" id="barCodeId" >
								</c:if>
							</td>
							</tr>

							<tr>
							<td class="formlabel"><insta:ltext key="salesissues.patientissuereturns.addshow.batch"/>&nbsp;<insta:ltext key="salesissues.patientissuereturns.addshow.no.or.remainingqty.or.expdt"/>:</td>
							<td>
								<select name="eBatch" id="eBatch" style="width: 200" class="dropdown">
								<option value=""><insta:ltext key="salesissues.patientissuereturns.addshow.select"/></option>
								</select>
							</td>
							</tr>
							<tr>
							<td class="formlabel"><insta:ltext key="salesissues.patientissuereturns.addshow.returnqty"/>:</td>
							<td>
								<input type="text" name="eReturnQty" id="eReturnQty" class="num" maxlength="6" />
								<input type="hidden" name="maxReturnQty"/>
								<select name="issue_unit" id="issue_unit" class="dropdown"></select>
							</td>
							</tr>
							<tr>
							<td class="formlabel"><insta:ltext key="salesissues.patientissuereturns.addshow.pkgsize"/>:</td>
							<td>
								<input type="text" name="ePackageSize" id="ePackageSize" disabled="disabled" />
							</td>
							</tr>

						<tr><td>&nbsp;</td></tr>
						<tr>
							<td>
								<button type="button" id="OK" name="OK" accesskey="A"  style="display: inline;" class="button" onclick="addItems(true);"><label><b><insta:ltext key="salesissues.patientissuereturns.addshow.ok"/></b></label></button>
								<button type="button" id="Cancel" name="Cancel" accesskey="A"  style="display: inline;" class="button" onclick="handleCancel();" ><label><b><insta:ltext key="salesissues.patientissuereturns.addshow.cancel"/></b></label></button>
							</td>
						</tr>
					</table>
					<table id="deletedrow"></table>
					</fieldset>
				</div>
			</div>
		<input type="button" name="save" id="save" value="${saveText }" onclick="return validate();"/>
	</form>
	</div>

	<div id="loginDiv" style="display: none">
			<div class="bd">
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel"><insta:ltext key="salesissues.patientissuereturns.addshow.logindetails"/></legend>
					<table class="formtable">
						<tr>
							<td class="formlabel"><insta:ltext key="salesissues.patientissuereturns.addshow.userid"/>: </td>
							<td><input type="text" name="login_user" id="login_user"/></td>
							<td class="formlabel">&nbsp;</td>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="salesissues.patientissuereturns.addshow.password"/>: </td>
							<td><input type="password" name="login_password" id="login_password" onkeypress="return submitOnEnter(event);"/></td>
							<td class="formlabel">&nbsp;</td>
							<td>&nbsp;</td>
						</tr>
					</table>
					<table style="margin-top: 10px">
						<tr>
							<td><input type="button" name="submitForm" id="submitForm" value="Submit" />
								<input type="button" name="cancelSubmit" id="cancelSubmit" value="Cancel" />
							</td>
						</tr>
					</table>
				</fieldset>
			</div>
		</div>

</body>
</html>
