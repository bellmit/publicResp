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
<%@page import="com.insta.hms.integration.URLRoute"%>
<c:set var="pagePath" value="<%=URLRoute.REMITTANCE_UPLOAD %>"/>

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
<insta:link type="script" file="billing/remittanceadvicexml.js"/>

<script type="text/javascript">
	var companyList     =  ${ifn:convertListToJson(insCompList)};
	var companyTpaList  =  ${ifn:convertListToJson(insCompTpaList)};
	var tpaList         =  ${ifn:convertListToJson(tpaList)};
	var tpaCenterList   =  ${ifn:convertListToJson(tpaCenterList)};
	var  xmlTpaListJSON  = ${ifn:convertListToJson(xmlTpaList)};

	var eClaimModule	= '${preferences.modulesActivatedMap['mod_eclaim']}';
	var paramMethod		= '${ifn:cleanJavaScript(param._method)}';
	var detailLevel 	= '${ifn:cleanJavaScript(param.detail_level)}';
	var itemIdentity	= '${ifn:cleanJavaScript(param.item_identification)}';
	var claimFormat 	= '${claimFormat}';
	
</script>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<title>Remittance Upload</title>
</head>
<body onload="init();">

<c:set var="actionUrl" value="${cpath}/${pagePath}/upload.htm"/>
<form name="RemittanceForm" action="${actionUrl}" method="POST"  enctype="multipart/form-data">
<%-- <input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}" /> --%>
<input type="hidden" name="remittance_id" value="${ifn:cleanHtmlAttribute(param.remittance_id)}" />
<c:set var="corpInsurance" value='<%=GenericPreferencesDAO.getAllPrefs().get("corporate_insurance")%>'/>

<div class="pageHeader">Remittance Upload</div>

<insta:feedback-panel />

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
 		  		
		  		<td class="forminfo">
		  			<select name="tpa_id" id="tpa_id" class="dropdown"  onchange="onChangeTPA(); showClaimFormat();">
		  				<option value="">(All)</option>
			  			<c:forEach items="${xmlTpaList}" var="tpa">
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
				
					<td class="formlabel">Upload File:</td>
					<td class="forminfo" colspan="2"><input type="file" name="remittance_metadata" id="remittance_metadata" accept="<insta:ltext key="upload.accept.claim"/>"/></td>
					<td class="formlabel" style="text-align:left" id="recoveryId" style="${claimFormat=='XML'?'':'display:none' }">
						<input type="hidden" name="is_recovery" id="is_recovery" value="N" />
						<input type="checkbox" name="recovery_check" id="recovery_check"
						onclick="setRecovery()" /><label for="recovery_check"> Is Recovery </label>
					</td>
					
			</tr>
			<tr>
			    <td class="formlabel">Reference No:</td>
				<td colspan="3" ><input type="text" name="reference_no" id="reference_no" value="${bean.map.reference_no}" /></td>
		   </tr>
		</table>
	</fieldset>
	

<button name="Submit" value="Submit" onclick="return validateSubmit();">Submit</button>

<c:if test="${param._method == 'delete'}">
<button name="Delete" value="Delete" onclick="return validateDelete();">Delete</button>
</c:if>
<insta:screenlink  screenId="ins_remittance" extraParam="/list.htm?sortReverse=false" label="Remittance Advice Log" addPipe="true"/>
</form>
</body>
</html>
