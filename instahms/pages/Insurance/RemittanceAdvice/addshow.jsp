<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<jsp:useBean id="detailDisplay" class="java.util.HashMap"/>
<c:set target="${detailDisplay}" property="I" value="Item"/>
<c:set target="${detailDisplay}" property="B" value="Bill"/>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="billing/claimsCommon.js"/>
<insta:link type="script" file="billing/remittance.js"/>
<insta:link type="script" file="billing/remittanceadvicexl.js"/>

<script type="text/javascript">
	var eClaimModule	= '${preferences.modulesActivatedMap['mod_eclaim']}';
	var companyList		= ${insCompList};
	var companyTpaList	= ${insCompTpaList};
	var xlTpaListJSON	= ${xlTpaListJSON};
	var tpaList			= ${tpaList};
	var tpaCenterList   = ${tpaCenterList};
	var paramMethod		= '${ifn:cleanJavaScript(param._method)}';
	var detailLevel 	= '${ifn:cleanJavaScript(param.detail_level)}';
	var itemIdentity	= '${ifn:cleanJavaScript(param.item_identification)}';
	var claimFormat 	= '${claimFormat}';
</script>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<title>Remittance Upload</title>
</head>
<body onload="init();">
<form name="RemittanceForm" action="${cpath}/Insurance/RemittanceUpload.do" method="POST"  enctype="multipart/form-data">
<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}" />
<input type="hidden" name="remittance_id" value="${ifn:cleanHtmlAttribute(param.remittance_id)}" />
<c:set var="corpInsurance" value='<%=GenericPreferencesDAO.getAllPrefs().get("corporate_insurance")%>'/>

<c:set var="readOnly" value="readOnly"/>
<c:if test="${param._method == 'add'}">
	<c:set var="readOnly" value=""/>
</c:if>

<div class="pageHeader">XL Remittance Upload</div>

<insta:feedback-panel/>

	<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Remittance Advice Details</legend>
		<table class="formtable" width="100%">
			<tr>
				<td class="formlabel">Insurance Company Name:</td>
		  		<td class="forminfo">
		  			<insta:selectdb displaycol="insurance_co_name" name="insurance_co_id" id="insurance_co_id" value="${bean.map.insurance_co_id}"
					table="insurance_company_master" valuecol="insurance_co_id" dummyvalue="(All)" onchange="onChangeRAInsuranceCompany()"  orderby="insurance_co_name"/>
		  		</td>
				<td class="formlabel">Sponsor :</td>
				<%--
		  		<c:choose>
					<c:when test="${corpInsurance eq 'Y'}">
						<td class="formlabel">Sponsor :</td>
					</c:when>
					<c:otherwise>
						<td class="formlabel">TPA :</td>
					</c:otherwise>
				</c:choose>
 				--%>
 		  		<td class="forminfo">
 		  		<%--
		  			<insta:selectdb displaycol="tpa_name"  name="tpa_id" id="tpa_id" value="${bean.map.tpa_id}"
					table="tpa_master" valuecol="tpa_id" dummyvalue="(All)" onchange="onChangeTPA(); showClaimFormat();" orderby="tpa_name"/>
						--%>
					
		  			<select name="tpa_id" id="tpa_id" class="dropdown"  onchange="onChangeTPA(); showClaimFormat();">
		  				<option value="">(All)</option>
			  			<c:forEach items="${xlTpaList}" var="tpa">
							<option value="${tpa.tpa_id}">${tpa.tpa_name}</option>
						</c:forEach>
					</select>
					<input type="hidden" name="claimFormat" id="claimFormat" value="${claimFormat}"/>
		  		</td>
		  		<td class="formlabel">Center/Account Group:</td>
		  		<td class="forminfo" >

				<c:set var="remittanceCenterOrAccountGroup" value="${(not empty bean.map.account_group && bean.map.account_group != 0) ? 'A' : '' }${bean.map.account_group}"/>

				<c:choose>
					<c:when test="${fn:startsWith(center_or_account_group, 'A')}"></c:when>
					<c:otherwise>
						<c:set var="remittanceCenterOrAccountGroup" value="${(not empty bean.map.center_id) ? 'C' : '' }${bean.map.center_id}"/>
					</c:otherwise>
				</c:choose>

				<select name="center_or_account_group" id="center_or_account_group" class="dropdown"  onchange="showClaimFormat();">
					<c:forEach items="${accountGrpAndCenterList}" var="acc">
						<option value="${acc.map.id}" ${remittanceCenterOrAccountGroup == acc.map.id ? 'selected':''}>${acc.map.ac_name}-(${acc.map.accounting_company_name})</option>
					</c:forEach>
				</select>

		  		</td>
			</tr>

			<tr>
				<td class="formlabel">Received Date:</td>
				<td class="forminfo">
					<insta:datewidget name="received_date" valueDate="${bean.map.received_date}" id="received_date" btnPos="left"/>
					<c:if test="${param._method != 'add'}">
						<input type="hidden" name="detail_level" id="detail_level" value="${empty bean.map.detail_level ? 'I' : bean.map.detail_level}" />
					</c:if>
					<span class="star"> * </span>
				</td>
				<c:choose>
					<c:when test="${param._method == 'add'}">
						<td class="formlabel">Upload File:</td>
						<td class="forminfo" colspan="2"><input type="file" name="remittance_metadata" id="remittance_metadata" accept="<insta:ltext key="upload.accept.master"/>"/></td>
						<td class="formlabel" style="text-align:left" id="recoveryId" style="${claimFormat=='XML'?'':'display:none' }">
							<input type="hidden" name="is_recovery" id="is_recovery" value="N" />
							<input type="checkbox" name="recovery_check" id="recovery_check"
							onclick="setRecovery()" /><label for="recovery_check"> Is Recovery </label>
						</td>
					</c:when>
					<c:otherwise>
						<td class="formlabel">Uploaded File:</td>
						<td class="forminfo" colspan="2">
							<input type="text" style="width:455px;" title="${bean.map.file_name}" value="${bean.map.file_name}" ${readOnly}/>
						</td>
						<td class="formlabel" style="text-align:left" id="recoveryId" style="${claimFormat=='XML'?'':'display:none' }">
							<input type="hidden" name="is_recovery" id="is_recovery" value="${bean.map.is_recovery}" />
							<input type="checkbox" name="recovery_check" id="recovery_check" onclick="setRecovery()"
							 	${not empty bean.map.is_recovery && bean.map.is_recovery == 'Y'? 'checked':''}
							  	disabled/><label for="recovery_check"> Is Recovery</label>
						</td>
					</c:otherwise>
				</c:choose>
			</tr>
			<tr>
			    <td class="formlabel">Reference No:</td>
				<td colspan="3" ><input type="text" name="reference_no" id="reference_no" value="${bean.map.reference_no}" /></td>
		   </tr>
		</table>
	</fieldset>
	<div id="XLRow" style="${claimFormat=='XL'?'':'white-space:nowrap' }">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Remittance Excel Upload Details</legend>
		<table class="formtable" width="100%">
			<tr>
				<td class="formlabel">Detail Level:</td>
				<td class="forminfo">
				<c:choose>
					<c:when test="${param._method == 'add'}">
						<insta:selectoptions name="detail_level" id="detail_level"
						opvalues="I,B" optexts="Item,Bill" value="${bean.map.detail_level}"
						onchange="enableDisableDetailLevelFields();"/>
					</c:when>
					<c:otherwise>
						<input type="text" value="${detailDisplay[bean.map.detail_level]}" ${readOnly}/>
					</c:otherwise>
				</c:choose>

				</td>
				<td class="formlabel">Worksheet Index:</td>
				<td class="forminfo">
					<input type="text" name="worksheet_index" id="worksheet_index" value="${ifn:cleanHtmlAttribute(param.worksheet_index)}" ${readOnly}/>
				</td>
				<td class="formlabel">Item Identification:</td>
				<td class="forminfo">
				<c:choose>
					<c:when test="${param._method == 'add'}">
						<insta:selectoptions name="item_identification" id="item_identification"
						opvalues="ActivityId,BillNo" optexts="ActivityId,BillNo"
						value="${empty param.item_identification ? 'ActivityId' : param.item_identification}"
						onchange="enableDisableIdentificationFields();"/>
					</c:when>
					<c:otherwise>
						<input type="text" value="" ${readOnly}/>
					</c:otherwise>
				</c:choose>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Item ID Heading:</td>
				<td class="forminfo">
					<input type="text" name="item_id_heading" id="item_id_heading" value="${ifn:cleanHtmlAttribute(param.item_id_heading)}" ${readOnly}/>
					<input type="hidden" name="item_id_heading_lbl" id="item_id_heading_lbl" value="Item ID Heading"/>
				</td>
				<td class="formlabel">Bill No. Heading:</td>
				<td class="forminfo">
					<input type="text" name="bill_no_heading" id="bill_no_heading" value="${ifn:cleanHtmlAttribute(param.bill_no_heading)}" ${readOnly} disabled/>
					<input type="hidden" name="bill_no_heading_lbl" id="bill_no_heading_lbl" value="Bill No. Heading"/>
				</td>
				<td class="formlabel">Service Name Heading:</td>
				<td class="forminfo">
					<input type="text" name="service_name_heading" id="service_name_heading" value="${ifn:cleanHtmlAttribute(param.service_name_heading)}" ${readOnly} disabled/>
					<input type="hidden" name="service_name_heading_lbl" id="service_name_heading_lbl" value="Service Name Heading"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Service Insurance Claim Amt. Heading:</td>
				<td class="forminfo">
					<input type="text" name="charge_insurance_claim_amount_heading" id="charge_insurance_claim_amount_heading" value="${ifn:cleanHtmlAttribute(param.charge_insurance_claim_amount_heading)}" ${readOnly} disabled/>
					<input type="hidden" name="charge_insurance_claim_amount_heading_lbl" id="charge_insurance_claim_amount_heading_lbl" value="Service Insurance Claim Amt. Heading"/>
				</td>
				<td class="formlabel">Service Posted Date Heading:</td>
				<td class="forminfo">
					<input type="text" name="service_posted_date_heading" id="service_posted_date_heading" value="${ifn:cleanHtmlAttribute(param.service_posted_date_heading)}" ${readOnly} disabled/>
					<input type="hidden" name="service_posted_date_heading_lbl" id="service_posted_date_heading_lbl" value="Service Posted Date Heading"/>
				</td>
				<td class="formlabel">Payment Ref Type:</td>
				<td class="forminfo">
				<c:choose>
					<c:when test="${param._method == 'add'}">
						<insta:selectoptions name="payment_ref_type" id="payment_ref_type"
						opvalues="Single,PerItem" optexts="Single Reference,Per Item Reference"
						value="${empty param.payment_ref_type ? 'PerItem' : param.payment_ref_type}"
						onchange="enableDisablePaymentRefFields();"/>
					</c:when>
					<c:otherwise>
						<input type="text" value="" ${readOnly}/>
					</c:otherwise>
				</c:choose>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Payment Reference:</td>
				<td class="forminfo">
					<input type="text" name="payment_reference" id="payment_reference" maxlength="50" value="${ifn:cleanHtmlAttribute(param.payment_reference)}" ${readOnly} disabled/>
					<input type="hidden" name="payment_reference_lbl" id="payment_reference_lbl" value="Payment Reference"/>
				</td>
				<td class="formlabel">Payment Reference Heading:</td>
				<td class="forminfo">
					<input type="text" name="payment_reference_heading" id="payment_reference_heading" value="${ifn:cleanHtmlAttribute(param.payment_reference_heading)}" ${readOnly}/>
					<input type="hidden" name="payment_reference_heading_lbl" id="payment_reference_heading_lbl" value="Payment Reference Heading"/>
				</td>
				<td class="formlabel">Remittance Amount Heading:</td>
				<td class="forminfo">
					<input type="text" name="amount_heading" id="amount_heading" value="${ifn:cleanHtmlAttribute(param.amount_heading)}"  ${readOnly}/>
					<input type="hidden" name="amount_heading_lbl" id="amount_heading_lbl" value="Remittance Amount Heading"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Denial Remarks Heading:</td>
				<td class="forminfo">
					<input type="text" name="denial_remarks_heading" id="denial_remarks_heading" value="${ifn:cleanHtmlAttribute(param.denial_remarks_heading)}" ${readOnly}/>
					<input type="hidden" name="denial_remarks_heading_lbl" id="denial_remarks_heading_lbl" value="Denial Remarks Heading"/>
				</td>
				<td class="formlabel">Payer ID Heading:</td>
				<td class="forminfo">
					<input type="text" name="payer_id_heading" id="payer_id_heading" value="${ifn:cleanHtmlAttribute(param.payer_id_heading)}" ${readOnly}/>
					<input type="hidden" name="payer_id_heading_lbl" id="payer_id_heading_lbl" value="Payer ID Heading"/>
				</td>
			</tr>
		</table>
	</fieldset>
	</div>
<c:if test="${param._method == 'add'}">
<button name="Submit" value="Submit" onclick="return validateSubmit();">Submit</button>
</c:if>
<c:if test="${param._method == 'delete'}">
<button name="Delete" value="Delete" onclick="return validateDelete();">Delete</button>
</c:if>
<c:if test="${param._method != 'add'}">
<insta:screenlink screenId="ins_remittance_xl" extraParam="?_method=add" label="Upload New Remittance" addPipe="false"/>
</c:if>
<insta:screenlink  screenId="ins_remittance_xl" extraParam="?_method=list" label="Remittance Advice Log" addPipe="true"/>
</form>
</body>
</html>
