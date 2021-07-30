<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page import="org.apache.struts.Globals"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%> 
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="defaultValue" value="${(prefDecimalDigits == 3) ? '0.000' : '0.00'}"/>
<c:set var="saleOrReturn" value="${resultMap.salesListMain.type}"/>
<html>
<head>
	<title><insta:ltext key="salesissues.medicinesaleedit.dashboard.editsales"/><c:if test="${saleOrReturn == 'R'}"> <insta:ltext key="salesissues.medicinesaleedit.dashboard.returns"/></c:if>- <insta:ltext key="salesissues.medicinesaleedit.dashboard.instahms"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="i18nSupport" content="true"/>
	<insta:link type="js" file="hmsvalidation.js"/>
	<insta:link type="js" file="ajax.js" />
	<insta:link type="js" file="instaautocomplete.js" />
	<insta:link type="js" file="stores/loginDialog.js" />
	<insta:link type="js" file="stores/storeshelper.js"/>
	<insta:js-bundle prefix="sales.issues"/>
	</head>

<body onload="init();" class="yui-skin-sam">

<c:choose>
	<c:when test="${saleOrReturn == 'R'}">
		<h1><insta:ltext key="salesissues.medicinesaleedit.dashboard.editsalereturns"/> </h1>
		<input type="hidden" name="salesReturn" value="true">
	</c:when>
	<c:otherwise>
		<h1><insta:ltext key="salesissues.medicinesaleedit.dashboard.editsales"/></h1>
		<input type="hidden" name="salesReturn" value="false">
 	</c:otherwise>
</c:choose>

<insta:feedback-panel/>
<c:set var="credit">
<insta:ltext key="salesissues.raisepatientindent.addshow.credit"/>
</c:set>
<c:choose>
<c:when test="${isRetail}" >
<div id="custRetailDetails" >
	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Retail ${resultMap.salesListMain.bill_type eq 'C'? credit:''}<insta:ltext key="salesissues.medicinesaleedit.dashboard.customerdetails"/></legend>
		<table class="formtable" cellpadding="0" cellspacing="0" width="100%" border="0">
			<tr>
				<td class="formlabel"><insta:ltext key="salesissues.medicinesaleedit.dashboard.name"/>:</td>
				<td class ="formInfo"> ${resultMap.retailDetails.customer_name} </td>
				<td class="formlabel"><insta:ltext key="salesissues.medicinesaleedit.dashboard.visitdate"/>:</td>
				<td class ="formInfo">
						<fmt:formatDate value="${resultMap.retailDetails.visit_date}" pattern="dd-MM-yyyy"/>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<c:if test="${resultMap.salesListMain.bill_type eq 'C'}" >
			<tr>
				<td class="formlabel"><insta:ltext key="salesissues.medicinesaleedit.dashboard.sponsor"/>:</td>
				<td class ="formInfo">
					${resultMap.retailDetails.sponsor_name}
				</td>

				<td class="formlabel"><insta:ltext key="salesissues.medicinesaleedit.dashboard.phoneno"/>:</td>
				<td class ="formInfo">
					${resultMap.retailDetails.sponsor_name}
				</td>
				<td class="formlabel"><insta:ltext key="salesissues.medicinesaleedit.dashboard.creditlimit"/>:</td>
				<td class ="formInfo">
					${resultMap.retailDetails.credit_limit}
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			</c:if>
		</table>
	</fieldset>
</div>
</c:when>
<c:otherwise>
<div id="patientDetails" >
	<insta:patientdetails visitid="${visitId}" />
</div>
</c:otherwise>
</c:choose>

<form name="editSalesForm" method="POST" action="editSales.do" >
<input type="hidden" name="_method" value="saveSalesClaimDetails" />
<input type="hidden" name="visitId" value="${ifn:cleanHtmlAttribute(visitId)}" />
<input type="hidden" name="saleId" value="${ifn:cleanHtmlAttribute(saleId)}" />
<input name="planId" id="planId" type="hidden" value="${resultMap.patientDetails.plan_id}"/>



<input name="isTpa" id="isTpa" type="hidden" value="${resultMap.patientDetails.primary_sponsor_id ne null && resultMap.isTpa eq true}">

<br />
<div id="salesDetailsMain" >
	<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">
		<c:choose>
			<c:when test="${saleOrReturn == 'R'}"><insta:ltext key="salesissues.medicinesaleedit.dashboard.return"/></c:when>
			<c:otherwise><insta:ltext key="salesissues.medicinesaleedit.dashboard.sale"/></c:otherwise>
		</c:choose> <insta:ltext key="salesissues.medicinesaleedit.dashboard.details"/>
	 </legend>
		<table class="formtable" cellpadding="0" cellspacing="0" width="100%" border="0">
		<tr>
			<td class="formLabel">
				<insta:ltext key="salesissues.medicinesaleedit.dashboard.salebillno"/>:
			</td>
			<td class="formInfo">
				${resultMap.salesListMain.sale_id}
			</td>
			<td class="formLabel">
				<insta:ltext key="salesissues.medicinesaleedit.dashboard.type"/>:
			</td>
			<td class="formInfo">
				${resultMap.salesListMain.type eq 'S'? 'Sales':'Returns'}
			</td>
			<td class="formLabel">
				<insta:ltext key="salesissues.medicinesaleedit.dashboard.hospitalbillno"/>.:
			</td>
			<td class="formInfo">
				${resultMap.salesListMain.bill_no}
			</td>
			<td class="formLabel">
				<insta:ltext key="salesissues.medicinesaleedit.dashboard.saledate"/>:
			</td>
			<td class="formInfo">
				<fmt:formatDate value="${resultMap.salesListMain.sale_date}" pattern="dd-MM-yyyy"/>
			</td>
		</tr>
		<tr>
			<td class="formLabel">
				<insta:ltext key="salesissues.medicinesaleedit.dashboard.store"/>:
			</td>
			<td class="formInfo">
				${resultMap.salesListMain.dept_name}
			</td>
			<td class="formLabel">
				<insta:ltext key="salesissues.medicinesaleedit.dashboard.username"/>:
			</td>
			<td class="formInfo">
				${resultMap.salesListMain.user_display_name}
			</td>
			<td class="formLabel">
				<insta:ltext key="salesissues.medicinesaleedit.dashboard.doctor"/>
			</td>
			<td class="formInfo">
				${resultMap.salesList[0].doctor_name}
			</td>
			<c:if test="${saleOrReturn == 'R' && resultMap.salesList[0].return_bill_no ne null}">
			<td class="formLabel">
				<insta:ltext key="salesissues.medicinesaleedit.dashboard.originalbillno"/>
			</td>
			<td class="formInfo">
				${resultMap.salesList[0].return_bill_no}
			</td>
			</c:if>
		</tr>
		</table>
	</fieldset>
</div>
<br />

<c:if test="${mod_eclaim_erx && saleOrReturn != 'R'}">
	<div id="insuranceDetails">
		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="ui.label.insurance.details"/></legend>
			<table class="insuranceDetails" cellpadding="0" cellspacing="0" width="100%" border="0">
				<tr>
				<td class="formlabel" style="text-align:right; padding-right:5px; width: 14%;"><insta:ltext key="patient.discharge.medication.erx.request.info.erx.ref.no"/> :</td>
				<td style="width: 20%;">
					<input type="text" name="erxReferenceNo" id="erxReferenceNo" maxlength="50" value="${resultMap.salesListMain.erx_reference_no}" >
				</td>
				<td class="formlabel" style="text-align:right; padding-right:5px; width: 10%;"><insta:ltext key="ui.label.external.pbm"/> :</td>
				<td>
				<c:choose>
					<c:when test="${resultMap.salesListMain.is_external_pbm eq true}">
						<input type="checkbox" name="isExternalPbm" id="isExternalPbm" checked>
				</c:when>
					<c:otherwise>
						<input type="checkbox" name="isExternalPbm" id="isExternalPbm">
					</c:otherwise>
				</c:choose>
				</td>
				<td></td>
				<td></td>
				<tr>
					<td class="formlabel" style="text-align:right; padding-right:5px; width: 14%;"><insta:ltext key="salesissues.sales.details.primarypriorauthno"/> :</td>
					<td style="width: 20%;">
						<input type="text" name="priPriorAuthNo" id="priPriorAuthNo" maxlength="100" >
					</td>
					<td class="formlabel" style="text-align:right; padding-right:5px;"><insta:ltext key="salesissues.sales.details.primarypriorauthmode"/>:</td>
						<td class="forminfo">
							<insta:selectdb  id="priPriorAuthMode" name="priPriorAuthMode" value="" table="prior_auth_modes" valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name" filtered="false" dummyvalue="-- Select --"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel" style="text-align:right; padding-right:5px; width: 14%;"><insta:ltext key="salesissues.sales.details.secondarypriorauthno"/> :</td>
					<td style="width: 20%;">
						<input type="text" name="secPriorAuthNo" id="secPriorAuthNo" maxlength="100" >
					</td>
					<td class="formlabel" style="text-align:right;"><insta:ltext key="salesissues.sales.details.secondarypriorauthmode"/>:</td>
						<td class="forminfo">
							<insta:selectdb  id="secPriorAuthMode" name="secPriorAuthMode" value="" table="prior_auth_modes" valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name" filtered="false" dummyvalue="-- Select --"/>
					</td>
					<td style="width: 20%;">
						<button type="button" id="copyPriorAuthDetails" onClick="copyPriorAuthToItems();"><insta:ltext key="ui.label.copy.prior.auth.to.items"/></button>
					</td>
				</tr>
				</tr>
				
			</table>
		</fieldset>
	</div>
</c:if>
<c:set var="numColumns" value="14"/>
<c:set var="hasMoreThanOneClaim" value="${fn:length(resultMap.visit_plans) > 1}"/>
<div class="resultList" style="margin: 10px 0px 5px 0px;">
<table class="detailList dialog_displayColumns" width="100%" cellspacing="0" cellpadding="0" id="medList" border="0">
	<tr>
		<th style="width: 1em;">#</th>
		<th style="width: 20em;"><insta:ltext key="salesissues.medicinesaleedit.dashboard.item"/></th>
		<th><insta:ltext key="salesissues.medicinesaleedit.dashboard.drugcode"/></th>
		<th><insta:ltext key="salesissues.medicinesaleedit.dashboard.batch"/></th>
		<th><insta:ltext key="salesissues.medicinesaleedit.dashboard.expiry"/></th>
		<th><insta:ltext key="salesissues.medicinesaleedit.dashboard.mfr"/></th>
		<th><insta:ltext key="salesissues.medicinesaleedit.dashboard.pkgsize"/></th>
		<th><insta:ltext key="salesissues.medicinesaleedit.dashboard.unitrate"/></th>
		<th><insta:ltext key="salesissues.medicinesaleedit.dashboard.packagerate"/></th>
		<th><insta:ltext key="salesissues.medicinesaleedit.dashboard.qty"/></th>
		<th><insta:ltext key="salesissues.medicinesaleedit.dashboard.discount"/></th>
		<th><insta:ltext key="salesissues.medicinesaleedit.dashboard.amount"/></th>
		<th><insta:ltext key="salesissues.medicinesaleedit.dashboard.tax"/></th>
		<th><insta:ltext key="salesissues.medicinesaleedit.dashboard.patamt"/></th>
		<th><insta:ltext key="salesissues.medicinesaleedit.dashboard.pattaxamt"/></th>
		<c:if test="${resultMap.patientDetails.tpa_name ne null && resultMap.patientDetails.tpa_name ne '' && resultMap.isTpa eq true}">
			<th><insta:ltext key="salesissues.medicinesaleedit.dashboard.pri.claimamt"/></th>
			<th><insta:ltext key="salesissues.medicinesaleedit.dashboard.pri.claimtaxamt"/></th>
			<th><insta:ltext key="salesissues.medicinesaleedit.dashboard.pri.priorauth"/></th>
			<c:set var="numColumns" value="18"/>
			<c:if test="${hasMoreThanOneClaim}">
				<th><insta:ltext key="salesissues.medicinesaleedit.dashboard.sec.claim"/></th>
				<th><insta:ltext key="salesissues.medicinesaleedit.dashboard.sec.claimtaxamt"/></th>
				<th><insta:ltext key="salesissues.medicinesaleedit.dashboard.sec.priorauth"/></th>
				<c:set var="numColumns" value="21"/>
			</c:if>
		</c:if>
		<th><insta:ltext key="patient.discharge.medication.erx.request.info.erx.activity.id"/></th>
		<th></th>
	</tr>
	<input type="hidden" name="_itemCount" id="_itemCount" value="${fn:length(resultMap.saleItemDetails)}"/>
	<c:forEach var="record" items="${resultMap['saleItemDetails']}" varStatus="st">
	<tr id="row${record.sale_item_id}" class="medRow">
		<td>
	 		${st.index+1}
	 	</td>
		<td>
			<insta:truncLabel value="${record.medicine_name}" length="25"/>
			<input type="hidden" name="sale_item_id" id="sale_item_id${st.index}" value="${record.sale_item_id}"/>
			<input type="hidden" name="medicine_id" id="medicine_id${record.sale_item_id}" value="${record.medicine_id}"/>
			<input type="hidden" name="_billStatus${record.sale_item_id}" id="_billStatus${record.sale_item_id}" value="${record.billstatus}"/>
			<input type="hidden" name="isEdited" id="_isEdited${record.sale_item_id}" value="f"/>
			<input type="hidden" name="qty_hid" id="qty_hid${record.sale_item_id}" value="${record.quantity}"/>
			<input type="hidden" name="pkgSize_hid" id="pkgSize_hid${record.sale_item_id}" value="${record.package_unit}"/>
			<input type="hidden" name="discount_hid" id="discount_hid${record.sale_item_id}" value="${record.discount_per}"/>
			<input type="hidden" name="discount_type_hid" id="discount_type_hid${record.sale_item_id}" value="${record.discount_type}"/>
		</td>
		<td>
			<label id="itemCodeCol${record.sale_item_id}">
				${record.item_code}
			</label>
			<input type="hidden" name="itemCode${record.sale_item_id}" id="itemCode${record.sale_item_id}" value="${record.item_code}"/>
			<input type="hidden" name="itemCodeType${record.sale_item_id}" id="itemCodeType${record.sale_item_id}" value="${record.code_type}"/>
			<input type="hidden" name="erxActivityId" id="erxActivityId${record.sale_item_id}" value="${record.erx_activity_id}" />
		</td>
		<td>
			${record.batch_no}
		</td>
		<td>
			${record.exp_dt_str}
		</td>
		<td>
			${record.manf_mnemonic}
		</td>
		<td>
			${record.package_unit}
		</td>
		<td class="number">
			<fmt:formatNumber type="number" groupingUsed="false" minFractionDigits="${after_decimal_digits}" maxFractionDigits="${after_decimal_digits}" value="${record.unit_mrp_wrf}" />
		</td>
		<td class="number">
			${record.pkg_mrp}
		</td>
		<td class="number">
			${record.quantity}
		</td>
		<td class="number">
			${record.itemwise_discount}
		</td>
		<td class="number">
			<label id="itemAmtCol${record.sale_item_id}">
			<c:set var="roundedAmt"><fmt:formatNumber groupingUsed="false" type="number" minFractionDigits="${after_decimal_digits}" maxFractionDigits="${after_decimal_digits}" value="${record.post_discount_amt_wrf+record.return_amt}" /></c:set>
			${roundedAmt}
			</label>
			<input type="hidden" name="itemAmt" id="itemAmt${record.sale_item_id}" value ="${roundedAmt}" />
		</td>
		<!-- Tax Amount -->
		<td class="number">
			<label id="itemTaxAmtCol${record.sale_item_id}">
				${record.tax+record.return_tax_amt}
			</label>
			<input type="hidden" name="itemTaxAmt${record.sale_item_id}" id="itemTaxAmt${record.sale_item_id}" value ="${record.tax+record.return_tax_amt}" />
		</td>
		<td class="number">
			<c:set var="saleClaimsDetails" value="${resultMap.sales_claim_details}"/>
			<c:set var="saleClaims" value="${saleClaimsDetails[record.sale_item_id]}"/>
			<c:set var="numsalesClaims" value="${fn:length(saleClaimsDetails[record.sale_item_id])}"/>
			<c:set var="recordInsuAmt" value="0"/>
			<c:set var="recordTotalSponsorTaxAmt" value="0"/>
			<c:set var="recordPatientTaxAmt" value="0"/>
			<c:forEach begin="1" end="${numsalesClaims}" var="c" varStatus="loop">
					<c:set var="salesClaims_cal" value="${saleClaims[c-1]}"/>
					<c:set var="recordInsuAmt" value="${resultMap.salesListMain.type eq 'S' ? recordInsuAmt+salesClaims_cal.insurance_claim_amt + salesClaims_cal.return_insurance_claim_amt : 0}"/>
					<c:set var="recordTotalSponsorTaxAmt" value="${resultMap.salesListMain.type eq 'S' ? recordTotalSponsorTaxAmt+salesClaims_cal.tax_amt : 0}"/>
			</c:forEach>
			<!-- National/Corporate will not have sales claim amounts so bring claim amt from sales details table -->
			<!--<c:if test="${numsalesClaims == 0}">
				<c:set var="recordInsuAmt" value="${recordInsuAmt+record.insurance_claim_amt + record.return_insurance_claim_amt}"/>
			</c:if>-->
			<c:set var="recordPatientTaxAmt" value="${resultMap.salesListMain.type eq 'S' ? ((record.tax+record.return_tax_amt)-recordTotalSponsorTaxAmt) : 0}"/>
			<label id="itemPatCol${record.sale_item_id}">
				${resultMap.salesListMain.type eq 'S' ? (roundedAmt - recordInsuAmt - recordTotalSponsorTaxAmt) : 0}
			</label>
			<input type="hidden" name="itemPat${record.sale_item_id}"  id="itemPat${record.sale_item_id}" value ="${roundedAmt - recordInsuAmt - (record.tax+record.return_tax_amt)}" />
			<input type="hidden" name="itemInsu${record.sale_item_id}"  id="itemInsu${record.sale_item_id}" value ="${recordInsuAmt+recordTotalSponsorTaxAmt}" />
			<input type="hidden" name="itemPreAuthNo${record.sale_item_id}"  id="itemPreAuthNo${record.sale_item_id}" value ="${record.prior_auth_id}" />
			<input type="hidden" name="itemPreAuthModeNo${record.sale_item_id}"  id="itemPreAuthModeNo${record.sale_item_id}" value ="${record.prior_auth_mode_id}" />
		</td>
		<!-- Patient Tax Amount -->
		<td class="number">
			<label id="itemPatTaxCol${record.sale_item_id}">${resultMap.salesListMain.type eq 'S' ? ((record.tax+record.return_tax_amt) - recordTotalSponsorTaxAmt) : 0}</label>
		</td>
		<c:if test="${resultMap.patientDetails.tpa_name ne null && resultMap.patientDetails.tpa_name ne '' && resultMap.isTpa eq true }">

			<!-- display claim amounts of all sponsors -->
			<c:forEach begin="1" end="${numsalesClaims}" var="j" varStatus="loop">
				<c:set var="salesClaims" value="${saleClaims[j-1]}"/>

				<c:if test="${j == 1}">
					<td class="number">
						<label id="pri_itemInsuCol${record.sale_item_id}">
							${resultMap.salesListMain.type eq 'S' ? (salesClaims.insurance_claim_amt + salesClaims.return_insurance_claim_amt + salesClaims.tax_amt) : 0}
						</label>
					</td>
					<!-- Primary Tax Amount -->
					<td class="number">
						<label id="itemPrimTaxCol${record.sale_item_id}">${resultMap.salesListMain.type eq 'S' ? (salesClaims.tax_amt) : 0}</label>
						<input type="hidden" name="priInsClaimTaxAmt" id="priInsClaimTaxAmt${record.sale_item_id}" value="${salesClaims.tax_amt}" />
					</td>
					<td>
						<label id="pri_itemPreAuthCol${record.sale_item_id}">
							${salesClaims.prior_auth_id}
						</label>
					</td>
					<input type="hidden" name="pri_insClaimAmt" id="pri_insClaimAmt${record.sale_item_id}" value="${salesClaims.insurance_claim_amt + salesClaims.return_insurance_claim_amt}" />
					<input type="hidden" name="orig_pri_insClaimAmt" id="orig_pri_insClaimAmt${record.sale_item_id}" value="${salesClaims.insurance_claim_amt + salesClaims.return_insurance_claim_amt}" />
					<input type="hidden" name="pri_itemPreAuthId" id="pri_itemPreAuthId${record.sale_item_id}" value="${salesClaims.prior_auth_id}" />
					<input type="hidden" name="pri_itemPreAuthMode" id="pri_itemPreAuthMode${record.sale_item_id}" value="${salesClaims.prior_auth_mode_id}" />
					<input type="hidden" name="pri_claim_id" id="pri_claim_id${record.sale_item_id}" value="${salesClaims.claim_id}" />
					<c:forEach items="${groupList}" var="group">
						<input type="hidden" name="pritaxrate${group.item_group_id}" id="pritaxrate${group.item_group_id}_${record.sale_item_id}" value="" />
						<input type="hidden" name="pritaxamount${group.item_group_id}" id="pritaxamount${group.item_group_id}_${record.sale_item_id}" value="" />
						<input type="hidden" name="pritaxsubgroupid${group.item_group_id}" id="pritaxsubgroupid${group.item_group_id}_${record.sale_item_id}" value="" />
					</c:forEach>
				</c:if>

				<c:if test="${j != 1}">
					<td class="number">
						<label id="sec_itemInsuCol${record.sale_item_id}">
							${resultMap.salesListMain.type eq 'S' ? (salesClaims.insurance_claim_amt + salesClaims.return_insurance_claim_amt + salesClaims.tax_amt) : 0}
						</label>
					</td>
					<!-- Scendory Tax Amount -->
					<td class="number">
						<label id="itemSecTaxCol${record.sale_item_id}">${resultMap.salesListMain.type eq 'S' ? (salesClaims.tax_amt) : 0}</label>
						<input type="hidden" name="secInsClaimTaxAmt" id="secInsClaimTaxAmt${record.sale_item_id}" value="${salesClaims.tax_amt}" />
					</td>
					<td>
						<label id="sec_itemPreAuthCol${record.sale_item_id}">
							${salesClaims.prior_auth_id}
						</label>
					</td>
					<input type="hidden" name="sec_insClaimAmt" id="sec_insClaimAmt${record.sale_item_id}" value="${salesClaims.insurance_claim_amt + salesClaims.return_insurance_claim_amt}" />
					<input type="hidden" name="orig_sec_insClaimAmt" id="orig_sec_insClaimAmt${record.sale_item_id}" value="${salesClaims.insurance_claim_amt + salesClaims.return_insurance_claim_amt}" />
					<input type="hidden" name="sec_itemPreAuthId" id="sec_itemPreAuthId${record.sale_item_id}" value="${salesClaims.prior_auth_id}" />
					<input type="hidden" name="sec_itemPreAuthMode" id="sec_itemPreAuthMode${record.sale_item_id}" value="${salesClaims.prior_auth_mode_id}" />
					<input type="hidden" name="sec_claim_id" id="sec_claim_id${record.sale_item_id}" value="${salesClaims.claim_id}" />
					<c:forEach items="${groupList}" var="group">
						<input type="hidden" name="sectaxrate${group.item_group_id}" id="sectaxrate${group.item_group_id}_${record.sale_item_id}" value="" />
						<input type="hidden" name="sectaxamount${group.item_group_id}" id="sectaxamount${group.item_group_id}_${record.sale_item_id}" value="" />
						<input type="hidden" name="sectaxsubgroupid${group.item_group_id}" id="sectaxsubgroupid${group.item_group_id}_${record.sale_item_id}" value="" />
					</c:forEach>
				</c:if>
			</c:forEach>
			<%-- <c:if test="${numsalesClaims == 0}">
				<input type="hidden" name="pri_insClaimAmt" id="pri_insClaimAmt${record.sale_item_id}" value="${record.insurance_claim_amt + record.return_insurance_claim_amt}" />
				<input type="hidden" name="orig_pri_insClaimAmt" id="orig_pri_insClaimAmt${record.sale_item_id}" value="${record.insurance_claim_amt + record.return_insurance_claim_amt}" />
				<input type="hidden" name="pri_itemPreAuthId" id="pri_itemPreAuthId${record.sale_item_id}" value="${record.prior_auth_id}" />
				<input type="hidden" name="pri_itemPreAuthMode" id="pri_itemPreAuthMode${record.sale_item_id}" value="${record.prior_auth_mode_id}" />

				<td class="number">
					<label id="pri_itemInsuCol${record.sale_item_id}">
					${record.insurance_claim_amt}
					</label>
				</td>
				<td >
					<label id="pri_itemPreAuthCol${record.sale_item_id}">
						${record.prior_auth_id}
					</label>
				</td>
			</c:if> --%>
		</c:if>
		<td>
			<label id="erx_activity_id${record.sale_item_id}">
				${record.erx_activity_id}
			</label>
		</td>
		<td>
		 <img src="${cpath}/icons/Edit.png" onclick="showEditDialog('${record.sale_item_id}');">
		</td>
	</tr>
	</c:forEach>
</table>
</div>
<br />
<fieldset class="fieldSetBorder">
  <legend class="fieldSetLabel"><insta:ltext key="salesissues.medicinesaleedit.dashboard.totals"/></legend>
	<table width="440" align="right" class="infotable">
		<tr>
			<td class="formlabel"><insta:ltext key="salesissues.medicinesaleedit.dashboard.totalbillamount"/>:</td>
			<td class="forminfo">
				<label id="lblTotAmt">${resultMap.salesListMain.total_item_amount}</label>
			</td>
			<td class="formlabel"><insta:ltext key="salesissues.medicinesaleedit.dashboard.totaldiscount"/>:</td>
			<td class="forminfo">
				<label>${resultMap.salesListMain.discount + resultMap.salesListMain.total_item_discount}</label>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="salesissues.medicinesaleedit.dashboard.totaltax"/>:</td>
			<td class="forminfo">
				<label id="lblTotTaxAmt">${resultMap.salesListMain.total_item_tax}</label>
			</td>
			<td class="formlabel"><insta:ltext key="salesissues.medicinesaleedit.dashboard.roundoff"/>:</td>
			<td class="forminfo">
				<label>${resultMap.salesListMain.round_off}</label>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="salesissues.medicinesaleedit.dashboard.patamt"/>:</td>
			<td class="forminfo">
				<label id="lblTotPatAmt">0.00</label>
			</td>
			<td class="formlabel"><insta:ltext key="salesissues.medicinesaleedit.dashboard.pattaxamt"/>:</td>
			<td class="forminfo">
				<label id="lblTotPatTaxAmt">0.00</label>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="salesissues.medicinesaleedit.dashboard.sponsoramt"/>:</td>
			<td class="forminfo">
				<label id="lblTotClaim">0.00</label>
			</td>
			<td class="formlabel"><insta:ltext key="salesissues.medicinesaleedit.dashboard.sponsortaxamt"/>:</td>
			<td class="forminfo">
				<label id="lblTotClaimTax">0.00</label>
			</td>
		</tr>
		<tr>
			<td class="formlabel" colspan="2">&nbsp;</td>
			<td class="formlabel"><b><insta:ltext key="salesissues.medicinesaleedit.dashboard.grandtotal"/>:</b></td>
			<td class="forminfo">
			 	<label id="lblGrandTotal">${resultMap.salesListMain.total_item_amount-resultMap.salesListMain.discount+resultMap.salesListMain.round_off}</label>
			</td>
		</tr>
	</table>
</fieldset>


<button type="submit" accesskey="S" name="save" value="Save" >
<b><u><insta:ltext key="salesissues.medicinesaleedit.dashboard.s"/></u></b><insta:ltext key="salesissues.medicinesaleedit.dashboard.a"/><insta:ltext key="salesissues.medicinesaleedit.dashboard.ve"/></button>&nbsp;

<div id="myDialog" style="display:none;visibility:hidden;">
		<div class="bd" id="bd2">
			<table class="formTable" align="center">
				<tr>
					<td>
						<fieldset class="fieldSetBorder" style="width:460px;"><legend class="fieldSetLabel"><insta:ltext key="salesissues.medicinesaleedit.dashboard.editcodeandinsuranceclaimdetails"/></legend>
								<br/>
								<table class="formTable" align="center">
									<tr>
										<td class="formlabel"><insta:ltext key="salesissues.medicinesaleedit.dashboard.codetype"/>:</td>
										<td class="forminfo">
											<input type="hidden"  id="_currentId" class="numeric" />
											<input type="hidden" name="_medicineId"  id="medicineId" class="numeric" />
											<input type="hidden" id="itemPatTaxCol"/>
											<input type="hidden" id="priInsClaimTaxAmt"/>
											<input type="hidden" id="secInsClaimTaxAmt"/>
											<input type="hidden" id="qtyHid"/>
											<input type="hidden" id="pkgSizeHid"/>
											<input type="hidden" id="discountHid"/>
											<input type="hidden" id="discountTypeHid"/>
											
											<select  id="_dlg_code_type" class="dropDown">
												<option></option>
											</select>
										</td>
										<td class="formlabel"><insta:ltext key="salesissues.medicinesaleedit.dashboard.code"/>:</td>
										<td class="forminfo">
											<div id="drugCodesAutoComplete" class="autoComplete">
												<input type="text" name="_dlg_code" id="_dlg_code" class="field" maxlength="39" tabindex="185" />
												<div id="drugCodesContainer" style="width:240px;"></div>
											</div>
											<input type="hidden"  id="_code" class="numeric" />
										</td>
									</tr>
									<tr>
										<td  class="formlabel"><insta:ltext key="salesissues.medicinesaleedit.dashboard.amount"/>:</td>
										<td  class="forminfo">
											<label id="_dlg_amt"></label>
										</td>
										<td  class="formlabel"><insta:ltext key="salesissues.medicinesaleedit.dashboard.patientamount"/>:</td>
										<td  class="forminfo">
											<label id="_dlg_pat_amt"></label>
										</td>
									</tr>
									<tr id="pri_claim_row">
										<td class="formlabel"><insta:ltext key="salesissues.medicinesaleedit.dashboard.pri.claimamtwot"/>:</td>
										<td class="forminfo">
											<input type="text" name="_dlg_claim_amt"  class="numeric"  onBlur="recalculatePatAmt();" onkeypress="return enterNumAndDotAndMinus(event);"/>
											<input type="hidden" name="_pri_claim_id" id="pri_claim_id" />
											<c:forEach items="${groupList}" var="group">
												<input type="hidden" name="pritaxrate${group.item_group_id}" id="pritaxrate${group.item_group_id}" value="" />
												<input type="hidden" name="pritaxamount${group.item_group_id}" id="pritaxamount${group.item_group_id}" value="" />
												<input type="hidden" name="pritaxsubgroupid${group.item_group_id}" id="pritaxsubgroupid${group.item_group_id}" value="" />
											</c:forEach>
										</td>
										<td colspan="2"></td>
									</tr>
									<tr id="pri_priauth_row">
										<td class="formlabel"><insta:ltext key="salesissues.medicinesaleedit.dashboard.pri.priorauth"/>:</td>
										<td class="forminfo">
											<input type="text" name="_dlg_pre_auth_no"  class="numeric" /></td>
										<td class="formlabel"><insta:ltext key="salesissues.medicinesaleedit.dashboard.pri.priorauthmode"/>:</td>
										<td class="forminfo">
											<insta:selectdb name="_dlg_pre_auth_mode_no" id="_dlg_pre_auth_mode_no" value="" table="prior_auth_modes" valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name" filtered="false" dummyvalue="-- Select --"/>
										</td>
									</tr>
									<c:if test="${mod_eclaim_erx}">
										<tr id="ed_erxReferenceRow">
											<td class="formlabel"><insta:ltext key="patient.discharge.medication.erx.request.info.erx.activity.id"/>:</td>
											<td class="forminfo" colspan="2">
												<input type="text" name="_dlg_erx_activity_id" id="_dlg_erx_activity_id" maxlength="50" style="width: 15em;"/>
											</td>
										</tr>
									</c:if>
									<c:if test="${hasMoreThanOneClaim}">
										<tr id="sec_claim_row">
											<td class="formlabel"><insta:ltext key="salesissues.medicinesaleedit.dashboard.sec.claimamt"/>:</td>
											<td class="forminfo">
												<input type="text" name="_dlg_claim_amt"  class="numeric"  onBlur="recalculatePatAmt();" onkeypress="return enterNumAndDotAndMinus(event);"/>
												<input type="hidden" name="_sec_claim_id" id="sec_claim_id" />
												<c:forEach items="${groupList}" var="group">
													<input type="hidden" name="sectaxrate${group.item_group_id}" id="sectaxrate${group.item_group_id}" value="" />
													<input type="hidden" name="sectaxamount${group.item_group_id}" id="sectaxamount${group.item_group_id}" value="" />
													<input type="hidden" name="sectaxsubgroupid${group.item_group_id}" id="sectaxsubgroupid${group.item_group_id}" value="" />
												</c:forEach>
											</td>
											<td colspan="2"></td>
										</tr>
										<tr id="sec_priauth_row">
											<td class="formlabel"><insta:ltext key="salesissues.medicinesaleedit.dashboard.sec.priorauth"/>:</td>
											<td class="forminfo">
												<input type="text" name="_dlg_pre_auth_no"  class="numeric" /></td>
											<td class="formlabel"><insta:ltext key="salesissues.medicinesaleedit.dashboard.sec.priorauthmode"/>:</td>
											<td class="forminfo">
												<insta:selectdb name="_dlg_pre_auth_mode_no" id="_dlg_pre_auth_mode_no" value="" table="prior_auth_modes" valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name" filtered="false" dummyvalue="-- Select --"/>
											</td>
										</tr>
									</c:if>
								</table>
						 </fieldset>
					</td>
				</tr>
				<tr>
					<td align="left">
						<input type="button" id="_ok_button" value="OK" onclick="handleOk();"/>
						<input type="button" id="_cancel_button" value="Cancel" onclick="handleCancel();"/>
					</td>
				</tr>
			</table>
		</div>
	</div>
</form>
<script>
		var cpath = '${cpath}';
		var drugCodeTypes = ${pharmaCodeTypesJSON};
		var drugCodesJson = ${pharmaCodesJSON};
		var isReturn  =  ${saleOrReturn == 'R'};
		var priClaimRow = document.getElementById("pri_claim_row");
		var pripriauthrow = document.getElementById("pri_priauth_row");
		var secClaimRow = document.getElementById("sec_claim_row");
		var secpriauthrow = document.getElementById("sec_priauth_row");
		var subgroupNamesList = JSON.parse('${ifn:cleanJavaScript(subGroupListJSON)}'); 
		var groupListJSON = JSON.parse('${ifn:cleanJavaScript(groupListJSON)}');
		var totaldiscounts = ${resultMap.salesListMain.discount};
		var totalRoundoff = ${resultMap.salesListMain.round_off};

		function handleOk() {

			if(!validatePriorAuthMode(getElementByName(pripriauthrow,'_dlg_pre_auth_no'),getElementByName(pripriauthrow,'_dlg_pre_auth_mode_no'),null,null)){
				return false;
			}

			if(!validatePriorAuthMode(getElementByName(secpriauthrow,'_dlg_pre_auth_no'),getElementByName(secpriauthrow,'_dlg_pre_auth_mode_no'),null,null)){
				return false;
			}
			recalculatePatAmt();
			var id = document.getElementById('_currentId').value;
			var pri_newInsuClaim = parseFloat(getElementByName(priClaimRow,'_dlg_claim_amt').value);
			var sec_newInsuClaim = parseFloat(getElementByName(secClaimRow,'_dlg_claim_amt').value);
			var taxAmt = parseFloat(getElementByName(secClaimRow,'_dlg_claim_amt').value);
			var amt = parseFloat(document.getElementById('itemAmtCol'+id).textContent.trim());
			var pri_oldInsuClaimEl = document.getElementById('pri_itemInsuCol'+id)?document.getElementById('pri_itemInsuCol'+id):null;
			var oldPatClaimEl = (isReturn) ? 0 : document.getElementById('itemPatCol'+id);
			var taxAmt = parseFloat(document.getElementById('itemTaxAmtCol'+id).textContent);
			var priInsTaxAmt = isNotNullValue('priInsClaimTaxAmt')?getFieldValue('priInsClaimTaxAmt'):0;
			console.log(taxAmt);

			if(isNaN(pri_newInsuClaim)) pri_newInsuClaim = 0;
			if(isNaN(amt)) amt = 0;

			if(isReturn) {
				pri_newInsuClaim = Math.abs(pri_newInsuClaim);
				amt = Math.abs(amt);
			}

			if(pri_newInsuClaim > amt) {
				showMessage("js.sales.issues.claimsamtnotgreater.netamt");
				return false;
			} else {
				if(isReturn) {
					pri_newInsuClaim = -pri_newInsuClaim;
					amt = -amt;
				}
				//Set primary sponsor amount display.
				if(pri_oldInsuClaimEl!= null)
					pri_oldInsuClaimEl.textContent = (parseFloat(formatAmountValue(pri_newInsuClaim))+parseFloat(priInsTaxAmt)).toFixed(decDigits);
				//Set primary sponsor amount hidden.
				if(document.getElementById('pri_insClaimAmt'+id))
					document.getElementById('pri_insClaimAmt'+id).value = formatAmountValue(pri_newInsuClaim);

				document.getElementById('itemCodeCol'+id).textContent =
				document.getElementById('itemCode'+id).value = document.getElementById('_dlg_code').value;
				document.getElementById('itemCodeType'+id).value = document.getElementById("_dlg_code_type").value;
				if (document.getElementById("_dlg_erx_activity_id")) {
					document.getElementById('erxActivityId'+id).value = document.getElementById("_dlg_erx_activity_id").value;
					document.getElementById('erx_activity_id'+id).innerText = document.getElementById("_dlg_erx_activity_id").value;
				}
				if ( document.getElementById('pri_itemPreAuthCol'+id) != null ) {
					document.getElementById('pri_itemPreAuthCol'+id).textContent = getElementByName(pripriauthrow,"_dlg_pre_auth_no").value;
					document.getElementById('pri_itemPreAuthId'+id).value = getElementByName(pripriauthrow,"_dlg_pre_auth_no").value;
					document.getElementById('pri_itemPreAuthMode'+id).value = getElementByName(pripriauthrow,"_dlg_pre_auth_mode_no").value;
				}
				if(secClaimRow)
					setSecClaimDialogDetails(id,amt);

				var sec_claim_amt = 0;
				if ( document.getElementById('sec_insClaimAmt'+id) != null )
					sec_claim_amt = parseFloat(document.getElementById('sec_insClaimAmt'+id).value);
				
				//Set patient amount display.
				oldPatClaimEl.textContent = (parseFloat(formatAmountValue(amt - (pri_newInsuClaim+sec_claim_amt) - taxAmt))+parseFloat(document.getElementById('itemPatTaxCol').value)).toFixed(decDigits);
				//Set patient amount hidden.
				document.getElementById('itemPat'+id).value = parseFloat(formatAmountValue(amt - (pri_newInsuClaim+sec_claim_amt) - taxAmt)).toFixed(decDigits);
				
				for(var j=0; j < groupListJSON.length; j++) {
					var itemGroupId = groupListJSON[j].item_group_id;
					if(document.getElementById('pritaxrate'+itemGroupId).value) {
						document.getElementById('pritaxrate'+itemGroupId+'_'+id).value = document.getElementById('pritaxrate'+itemGroupId).value;
					}
					if(document.getElementById('pritaxamount'+itemGroupId).value) {
						document.getElementById('pritaxamount'+itemGroupId+'_'+id).value = document.getElementById('pritaxamount'+itemGroupId).value;
					}
					if(document.getElementById('pritaxsubgroupid'+itemGroupId).value) {
						document.getElementById('pritaxsubgroupid'+itemGroupId+'_'+id).value = document.getElementById('pritaxsubgroupid'+itemGroupId).value;
					}
					
					if(secClaimRow) {
						if(document.getElementById('sectaxrate'+itemGroupId).value) {
							document.getElementById('sectaxrate'+itemGroupId+'_'+id).value = document.getElementById('sectaxrate'+itemGroupId).value;
						}
						if(document.getElementById('sectaxamount'+itemGroupId).value) {
							document.getElementById('sectaxamount'+itemGroupId+'_'+id).value = document.getElementById('sectaxamount'+itemGroupId).value;
						}
						if(document.getElementById('sectaxsubgroupid'+itemGroupId).value) {
							document.getElementById('sectaxsubgroupid'+itemGroupId+'_'+id).value = document.getElementById('sectaxsubgroupid'+itemGroupId).value;
						}
					}
				}
				document.getElementById('itemPatTaxCol'+id).textContent = parseFloat(document.getElementById('itemPatTaxCol').value).toFixed(decDigits);
				
				if(document.getElementById('priInsClaimTaxAmt'+id)) {
					document.getElementById('priInsClaimTaxAmt'+id).value = document.getElementById('priInsClaimTaxAmt').value;
					document.getElementById('itemPrimTaxCol'+id).textContent = document.getElementById('priInsClaimTaxAmt').value;
				}
				if(secClaimRow) {
					document.getElementById('secInsClaimTaxAmt'+id).value = document.getElementById('secInsClaimTaxAmt').value;
					document.getElementById('itemSecTaxCol'+id).textContent = document.getElementById('secInsClaimTaxAmt').value;	
				}
				setPatientAndClaimTotals();
				myDialog.cancel();

			}
		}

		function setSecClaimDialogDetails(id,amt){
			var sec_newInsuClaim = parseFloat(getElementByName(secClaimRow,'_dlg_claim_amt').value);
			var secInsTaxAmt = isNotNullValue('secInsClaimTaxAmt')?getFieldValue('secInsClaimTaxAmt'):0;
			
			if(isNaN(sec_newInsuClaim)) sec_newInsuClaim = 0;

			if(sec_newInsuClaim > amt) {
				showMessage("js.sales.issues.claimsamtnotgreater.netamt");
				return false;
			} else {
				if(isReturn) {
					sec_newInsuClaim = -sec_newInsuClaim;
					amt = -amt;
				}
			}

			var sec_oldInsuClaimEl = document.getElementById('sec_itemInsuCol'+id)?document.getElementById('sec_itemInsuCol'+id):null;
			//Set secondary sponsor amount display.
			if(sec_oldInsuClaimEl!= null)
					sec_oldInsuClaimEl.textContent = (parseFloat(formatAmountValue(sec_newInsuClaim))+parseFloat(formatAmountValue(secInsTaxAmt))).toFixed(decDigits);
			//Set secondary sponsor amount hidden.
			document.getElementById('sec_insClaimAmt'+id).value = formatAmountValue(sec_newInsuClaim);
			if ( document.getElementById('sec_itemPreAuthCol'+id) != null ) {
				document.getElementById('sec_itemPreAuthCol'+id).textContent = getElementByName(secpriauthrow,"_dlg_pre_auth_no").value;
				document.getElementById('sec_itemPreAuthId'+id).value = getElementByName(secpriauthrow,"_dlg_pre_auth_no").value;
				document.getElementById('sec_itemPreAuthMode'+id).value = getElementByName(secpriauthrow,"_dlg_pre_auth_mode_no").value;
			}
		}

		function recalculatePatAmt() {
			var id = document.getElementById('_currentId').value;
			var medicineId = document.getElementById('medicineId').value;
			var pri_newInsuClaim = parseFloat(getElementByName(priClaimRow,'_dlg_claim_amt').value);
			var priTaxAmt = parseFloat(isNotNullValue('priInsClaimTaxAmt', id)?document.getElementById('priInsClaimTaxAmt'+id).value:0);
			var sec_newInsuClaim = parseFloat(getElementByName(secClaimRow,'_dlg_claim_amt').value);
			var secTaxAmt = parseFloat(isNotNullValue('secInsClaimTaxAmt', id)?document.getElementById('secInsClaimTaxAmt'+id).value:0);
			var qty = document.getElementById('qtyHid').value;
			var package_unit = document.getElementById('pkgSizeHid').value;
			var discount = document.getElementById('discountHid').value;
			var discountType = document.getElementById('discountTypeHid').value;
			var priClaimId = document.getElementById('pri_claim_id').value;
			if(secClaimRow)
				var secClaimId = document.getElementById('sec_claim_id').value;
			var adjAmt = 'N';
			
			document.getElementById('_isEdited'+id).value = 't';
			var priClaimTaxSplit = {};
			var secClaimTaxSplit = {};
			var priClaimTaxItem = {
				sale_item_id:id,
				quantity : 1,
				amount : pri_newInsuClaim,
				package_unit : 1,
				medicine_id: medicineId,
				claim_id: priClaimId,
				disc:0
			};
			
			var priTaxDetails = setTaxDetails(priClaimTaxItem, priClaimTaxSplit);
			if(secClaimRow) {
				var secClaimTaxItem = {
					sale_item_id:id,
					quantity : 1,
					amount : sec_newInsuClaim,
					package_unit : 1,
					medicine_id: medicineId,
					claim_id: secClaimId,
					disc:0
				};
				var secTaxDetails = setTaxDetails(secClaimTaxItem, secClaimTaxSplit);
			}
				
				
			var totalClaimTaxAmt = priTaxDetails.vatAmt;
			adjAmt = priTaxDetails.adjAmt;
			if(secClaimRow) 
				totalClaimTaxAmt += secTaxDetails.vatAmt;
	
			var taxAmt = parseFloat(document.getElementById('itemTaxAmtCol'+id).textContent);
			var amt = parseFloat(document.getElementById('itemAmtCol'+id).textContent);
			
			if(adjAmt == 'Y') {
				document.getElementById('itemAmtCol'+id).textContent = (amt - taxAmt) + totalClaimTaxAmt;
				document.getElementById('itemAmt'+id).value = (amt - taxAmt) + totalClaimTaxAmt;
				
				document.getElementById('itemTaxAmtCol'+id).textContent = totalClaimTaxAmt;
				document.getElementById('itemTaxAmt'+id).value = totalClaimTaxAmt;
				
				taxAmt = totalClaimTaxAmt;
			}
			
			document.getElementById('itemPatTaxCol').value = taxAmt - totalClaimTaxAmt;
			
			document.getElementById('priInsClaimTaxAmt').value = priTaxDetails.vatAmt;
			if(secClaimRow) {
				document.getElementById('secInsClaimTaxAmt').value = secTaxDetails.vatAmt;
			}
			
			for(var j=0; j < groupListJSON.length; j++) {
				var itemGroupId = groupListJSON[j].item_group_id;
				if(priClaimTaxSplit['taxrate'+itemGroupId]) {
					document.getElementById('pritaxrate'+itemGroupId).value = priClaimTaxSplit['taxrate'+itemGroupId];
				}
				if(priClaimTaxSplit['taxamount'+itemGroupId]) {
					document.getElementById('pritaxamount'+itemGroupId).value = priClaimTaxSplit['taxamount'+itemGroupId];
				}
				if(priClaimTaxSplit['taxsubgroupid'+itemGroupId]) {
					document.getElementById('pritaxsubgroupid'+itemGroupId).value = priClaimTaxSplit['taxsubgroupid'+itemGroupId];
				}
				
				if(secClaimRow) {
					if(secClaimTaxSplit['taxrate'+itemGroupId]) {
						document.getElementById('sectaxrate'+itemGroupId).value = secClaimTaxSplit['taxrate'+itemGroupId];
					}
					if(secClaimTaxSplit['taxamount'+itemGroupId]) {
						document.getElementById('sectaxamount'+itemGroupId).value = secClaimTaxSplit['taxamount'+itemGroupId];
					}
					if(secClaimTaxSplit['taxsubgroupid'+itemGroupId]) {
						document.getElementById('sectaxsubgroupid'+itemGroupId).value = secClaimTaxSplit['taxsubgroupid'+itemGroupId];
					}
				}
			}
	
			var amt = parseFloat(document.getElementById('itemAmtCol'+id).textContent.trim());
			amt = amt - taxAmt;
			if(isNaN(pri_newInsuClaim)) pri_newInsuClaim = 0;
			if(isNaN(sec_newInsuClaim)) sec_newInsuClaim = 0;
			if(isNaN(amt)) amt = 0;
			var patAmt = 0;
			if(secClaimRow) 
				patAmt = amt-pri_newInsuClaim-sec_newInsuClaim;
			else
				patAmt = amt-pri_newInsuClaim;
			
			if(isReturn) {
				pri_newInsuClaim = Math.abs(pri_newInsuClaim);
				sec_newInsuClaim = Math.abs(sec_newInsuClaim);
				amt = Math.abs(amt);
				patAmt = amt-(pri_newInsuClaim+sec_newInsuClaim);
			}

			if(pri_newInsuClaim > amt || sec_newInsuClaim > amt) {
				showMessage("js.sales.issues.claimsamtnotgreater.netamt");
				return false;
			} else {
				if(isReturn) {
					pri_newInsuClaim = -pri_newInsuClaim;;
					sec_newInsuClaim = -sec_newInsuClaim
					amt = -amt;
					patAmt = -patAmt;
				}
				document.getElementById('_dlg_pat_amt').textContent = (isReturn) ? 0 : formatAmountValue(patAmt);
				return true;
			}
		}

		function init() {
			setPatientAndClaimTotals();
			initDialog();
			drugCodesAutoComplete();
			loadSelectBox(document.getElementById('_dlg_code_type'), drugCodeTypes, 'code_type', 'code_type', '--Select--', ''); 
		}

		function setPatientAndClaimTotals() {
			var itemCount = parseInt(document.getElementById("_itemCount").value);
			var totClaimAmt = 0;
			var totPatAmt = 0;
			var totAmt = 0;
			var totTaxAmt = 0;
			var totSponsorTaxAmt = 0;

			
			for(var i=0; i<itemCount ; i++){
				var saleItemId = document.getElementById("sale_item_id"+i).value;
				var pri_insuAmt = document.getElementById("pri_insClaimAmt"+saleItemId) ? parseFloat(document.getElementById("pri_insClaimAmt"+saleItemId).value) : 0;
				var sec_insuAmt = document.getElementById("sec_insClaimAmt"+saleItemId) ? parseFloat(document.getElementById("sec_insClaimAmt"+saleItemId).value) : 0;
				var amt = parseFloat(document.getElementById("itemAmt"+saleItemId).value);
				var tax = parseFloat(document.getElementById("itemTaxAmt"+saleItemId).value);
				var priSponsorTax = 0;
				if(document.getElementById("priInsClaimTaxAmt"+saleItemId))
					priSponsorTax = parseFloat(document.getElementById("priInsClaimTaxAmt"+saleItemId).value);
				var secSponsorTax = 0;
				if(secClaimRow)
					secSponsorTax = parseFloat(document.getElementById("secInsClaimTaxAmt"+saleItemId).value);
				if(isNaN(pri_insuAmt)) pri_insuAmt = 0;
				if(isNaN(sec_insuAmt)) sec_insuAmt = 0;
				if(isNaN(amt)) amt = 0;
				totClaimAmt += pri_insuAmt;
				totClaimAmt += sec_insuAmt;
				totAmt += amt;
				totTaxAmt += tax;
				if(secClaimRow)
					totSponsorTaxAmt += (priSponsorTax + secSponsorTax);
				else
					totSponsorTaxAmt += priSponsorTax;
			}
			document.getElementById("lblTotClaim").textContent = (isReturn) ? 0 : (parseFloat(formatAmountValue(totClaimAmt)) + parseFloat(formatAmountValue(totSponsorTaxAmt))).toFixed(decDigits);
			document.getElementById("lblTotClaimTax").textContent = (isReturn) ? 0 : formatAmountValue(totSponsorTaxAmt);
			document.getElementById("lblTotPatAmt").textContent = (isReturn) ? 0 : (parseFloat(formatAmountValue(totAmt-totClaimAmt-totTaxAmt)) + parseFloat(formatAmountValue(totTaxAmt-totSponsorTaxAmt))).toFixed(decDigits);
			document.getElementById("lblTotPatTaxAmt").textContent = (isReturn) ? 0 : parseFloat(formatAmountValue(totTaxAmt-totSponsorTaxAmt));
			document.getElementById("lblTotTaxAmt").textContent = parseFloat(formatAmountValue(totTaxAmt));
			document.getElementById("lblTotAmt").textContent = parseFloat(formatAmountValue(totAmt));
            document.getElementById("lblGrandTotal").textContent = parseFloat(formatAmountValue(totAmt))-parseFloat(formatAmountValue(totaldiscounts))+parseFloat(formatAmountValue(totalRoundoff));
			
			
		}

		function drugCodesAutoComplete() {
			var drugCodesJsonArray = new Array();
			for (i=0 ; i< drugCodesJson.length; i++) {
				var item = drugCodesJson[i];
				if(!empty(item.code)) {
					drugCodesJsonArray.push(item.code);
				}
			}
			YAHOO.example.ACJSAddArray = new function() {
				datasource = new YAHOO.widget.DS_JSArray(drugCodesJsonArray);
				var autoComp = new YAHOO.widget.AutoComplete('_dlg_code', 'drugCodesContainer' ,datasource);

				autoComp.formatResult = Insta.autoHighlight;
				autoComp.prehighlightClassName = "yui-ac-prehighlight";
				autoComp.typeAhead = false;
				autoComp.useShadow = false;
				autoComp.allowBrowserAutocomplete = false;
				autoComp.queryMatchContains = true;
				autoComp.minQueryLength = 0;
				autoComp.maxResultsDisplayed = 20;
				autoComp.forceSelection = false;
				autoComp.itemSelectEvent.subscribe(updateCodeDetails);
			}
		}

		var updateCodeDetails = function(sType, aArgs) {
			var oData = aArgs[2];
			for(var i=0; i<drugCodesJson.length; i++) {
				var item = drugCodesJson[i];
				if(item.code == oData){
					document.getElementById("_code").value = oData;
					setSelectedIndex(document.getElementById("_dlg_code_type"),item.code_type);
				}
			}
		}

		var myDialog;
		function initDialog() {
		    myDialog = new YAHOO.widget.Dialog('myDialog', {
		        width:"500px",
		        visible: false,
		        modal: true,
		        constraintoviewport: true,

		    });

		    var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleCancel,
	                                                scope:myDialog,
	                                                correctScope:true } );
	        var entKeyListener = new YAHOO.util.KeyListener(document, { keys:13 },
	                                              { fn:handleOk,
	                                                scope:myDialog,
	                                                correctScope:true } );
			myDialog.cfg.queueProperty("keylisteners", [escKeyListener,entKeyListener]);
		    myDialog.render();
		}

		function handleCancel() {
			myDialog.cancel();
		}
		
		/* Copies prior auth information from insurance details subsection in sales screen
		 * to all items in the medicine grid. Copies primary and secondary prior auth numbers and modes.
		 * */
		function copyPriorAuthToItems() {
			var priPriorAuthNo = document.getElementById("priPriorAuthNo").value;
			var secPriorAuthNo = document.getElementById("secPriorAuthNo").value;
			var priPriorAuthMode = document.getElementById("priPriorAuthMode").value;
			var secPriorAuthMode = document.getElementById("secPriorAuthMode").value;
			var medicineRows = document.getElementsByClassName("medRow");
			if ((priPriorAuthNo != "" && priPriorAuthMode == "") || (secPriorAuthNo != "" && secPriorAuthMode == "")) {
				showMessage("js.sales.issues.auth.mode.and.prior.auth.number.are.required");
			} else {
				for (var i=0; i<medicineRows.length; i++) {
					var row = medicineRows[i];
					var id = row.id.substring(3);//get the item id
					// copy primary sponsor prior auth information
					if ( document.getElementById('pri_itemPreAuthCol'+id) != null ) {
						document.getElementById('pri_itemPreAuthCol'+id).textContent = priPriorAuthNo;
						document.getElementById('pri_itemPreAuthId'+id).value = priPriorAuthNo;
						document.getElementById('pri_itemPreAuthMode'+id).value = priPriorAuthMode;
					}
					// copy secondary sponsor prior auth information
					if ( document.getElementById('sec_itemPreAuthCol'+id) != null ) {
						document.getElementById('sec_itemPreAuthCol'+id).textContent = secPriorAuthNo;
						document.getElementById('sec_itemPreAuthId'+id).value = secPriorAuthNo;
						document.getElementById('sec_itemPreAuthMode'+id).value = secPriorAuthMode;
					}
				}
			}
		}

		function showEditDialog(id) {

			document.getElementById('_currentId').value = id;
			document.getElementById('myDialog').style.display='block';
			var dRow = document.getElementById("row"+id);
		    myDialog.cfg.setProperty("context", [dRow, "tr", "br"], false);
		    var isNotTpa = (document.getElementById('isTpa') == null) || (document.getElementById('isTpa').value=="false");
		    

		    if(document.getElementById('_billStatus'+id).value != 'A' || isNotTpa || isReturn) {
		    	getElementByName(priClaimRow,'_dlg_claim_amt').setAttribute("disabled", "true");
		    }else {
		    	getElementByName(priClaimRow,'_dlg_claim_amt').removeAttribute("disabled");
		    }
			getElementByName(pripriauthrow,'_dlg_pre_auth_no').disabled = isNotTpa;
			getElementByName(pripriauthrow,'_dlg_pre_auth_mode_no').disabled = isNotTpa;
			
			document.getElementById('medicineId').value = document.getElementById('medicine_id'+id).value;
			document.getElementById('qtyHid').value = document.getElementById('qty_hid'+id).value;
			document.getElementById('pkgSizeHid').value = document.getElementById('pkgSize_hid'+id).value;
			document.getElementById('discountHid').value = document.getElementById('discount_hid'+id).value;
			document.getElementById('discountTypeHid').value = document.getElementById('discount_type_hid'+id).value;
			
			if(document.getElementById('priInsClaimTaxAmt'+id) && document.getElementById('priInsClaimTaxAmt'+id).value) {
				document.getElementById('priInsClaimTaxAmt').value = document.getElementById('priInsClaimTaxAmt'+id).value;	
			} 
			if(document.getElementById('secInsClaimTaxAmt'+id) && document.getElementById('secInsClaimTaxAmt'+id).value) {
				if(document.getElementById("sec_claim_row")) {
					document.getElementById('secInsClaimTaxAmt').value = document.getElementById('secInsClaimTaxAmt'+id).value;
				}
			}
			
			getElementByName(priClaimRow,'_dlg_claim_amt').value = document.getElementById('pri_insClaimAmt'+id)? document.getElementById('pri_insClaimAmt'+id).value.trim(): parseFloat(0);
		    getElementByName(pripriauthrow,'_dlg_pre_auth_no').value = document.getElementById('pri_itemPreAuthCol'+id) ? document.getElementById('pri_itemPreAuthCol'+id).textContent.trim(): '';
		    setSelectedIndex(getElementByName(pripriauthrow,'_dlg_pre_auth_mode_no'), document.getElementById('pri_itemPreAuthMode'+id) ? document.getElementById('pri_itemPreAuthMode'+id).value : 0);
		    getElementByName(priClaimRow,'_pri_claim_id').value = document.getElementById('pri_claim_id'+id)? document.getElementById('pri_claim_id'+id).value: parseFloat(0);
		    
		    document.getElementById('_dlg_code').value = document.getElementById('itemCodeCol'+id).textContent.trim();
		    setSelectedIndex(document.getElementById('_dlg_code_type'), document.getElementById('itemCodeType'+id).value);
		    if (document.getElementById('_dlg_erx_activity_id')) {
		    	document.getElementById('_dlg_erx_activity_id').value = document.getElementById('erxActivityId'+id).value;
		    }
		    document.getElementById('_dlg_amt').textContent = parseFloat(formatAmountValue(document.getElementById('itemAmt'+id).value) - formatAmountValue(document.getElementById('itemTaxAmt'+id).value)).toFixed(decDigits);
		    document.getElementById('_dlg_pat_amt').textContent = (isReturn) ? 0 : formatAmountValue(document.getElementById('itemPat'+id).value);
		    if(document.getElementById("sec_claim_row"))
		 	   setSecClaimDetails(id);
		    myDialog.show();
		}

		function setSecClaimDetails(id){

			getElementByName(secClaimRow,'_dlg_claim_amt').value = document.getElementById('sec_insClaimAmt'+id)? document.getElementById('sec_insClaimAmt'+id).value.trim(): parseFloat(0);
		    getElementByName(secpriauthrow,'_dlg_pre_auth_no').value = document.getElementById('sec_itemPreAuthCol'+id) ? document.getElementById('sec_itemPreAuthCol'+id).textContent.trim(): '';
		    setSelectedIndex(getElementByName(secpriauthrow,'_dlg_pre_auth_mode_no'), document.getElementById('sec_itemPreAuthMode'+id).value);
		    getElementByName(secClaimRow,'_sec_claim_id').value = document.getElementById('sec_claim_id'+id).value? document.getElementById('sec_claim_id'+id).value: parseFloat(0);
		}
		
		function setTaxDetails(taxItem, item) {
			var url = cpath + "/sales/getbasetaxdetails.json";
			var vatRate = 0;
			var vatAmt = 0;
			var adjAmt = 'N';
			var response = ajaxFormObj(taxItem, url, false);
			if (response != undefined && response.tax_details != undefined) {
				var taxMap = response.tax_details;
				adjAmt = response.adj_amt;
				for(var i=0; i<taxMap.length; i++) {
				    for(var j=0; j < subgroupNamesList.length; j++) {
				    	if(taxMap[i] && taxMap[i][subgroupNamesList[j].item_subgroup_id] && taxMap[i][subgroupNamesList[j].item_subgroup_id] != null) {
				    		var val = taxMap[i][subgroupNamesList[j].item_subgroup_id];
				    		var itemGroupId = subgroupNamesList[j].item_group_id;
				    		item['taxrate'+itemGroupId] = parseFloat(val.rate).toFixed(decDigits);
				    		item['taxamount'+itemGroupId] = parseFloat(val.amount).toFixed(decDigits);
				    		item['taxsubgroupid'+itemGroupId] = subgroupNamesList[j].item_subgroup_id;

				    		vatAmt += parseFloat(parseFloat(val.amount).toFixed(decDigits));
						    vatRate = vatRate + parseFloat(val.rate);
				    	}
					}
				    
				}
			}
			var taxDetails = {
					vatRate:vatRate,
					vatAmt:vatAmt,
					adjAmt:adjAmt
			};
			return taxDetails;
		}
</script>
</body>
</html>
