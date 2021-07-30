<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="billing.editvisitrateplan.bedtype.details.title"/></title>
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="js" file="instaautocomplete.js" />
<insta:link type="js" file="ajax.js" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<style>
	#myDialog_mask.mask {
		    z-index: 1;
		    display: none;
		    position: absolute;
		    top: 0;
		    left: 0;
		    -moz-opacity: 0.0001;
		    opacity: 0.0001;
		    filter: alpha(opacity=50);
		    background-color: #CCC;
	}

	#myDialog1_mask.mask {
		    z-index: 1;
		    display: none;
		    position: absolute;
		    top: 0;
		    left: 0;
		    -moz-opacity: 0.0001;
		    opacity: 0.0001;
		    filter: alpha(opacity=50);
		    background-color: #CCC;
	}
</style>

<script>
		var existingRatePlan = '${patient.org_id}';
		var existingBedType = '${patient.bill_bed_type}';
		var allwdRatePlans = ${param.visitId == null? 'null': allowedRatePlans};
		var bedTypesJSON = ${param.visitId == null? 'null':bedTypesListJSON}
		var categRatePlanWiseList =  ${param.visitId == null? 'null': categoryWiseRateplans};
		var allOpenBillsJSON = ${param.visitId == null? 'null': allOpenBillsJSON};
		var visitTpaId = '${patient.primary_sponsor_id}';

		function loadRatePlan() {
			if(allwdRatePlans!= null && allwdRatePlans!='null') {
				var defltRatePlan = allwdRatePlans.defaultRatePlan;
				var errorMsg = allwdRatePlans.errorMsg;
				var ratePlanList = allwdRatePlans.ratePlanList;
				if(errorMsg == "") {
					loadSelectBox(document.getElementById('updated_rate_plan'), ratePlanList, "ORG_NAME", "ORG_ID", "--Select--", "");
					setSelectedIndex(document.getElementById('updated_rate_plan'), defltRatePlan);
				} else {
					alert(errorMsg);
					document.getElementById('updated_rate_plan').length = 1;
					document.getElementById('updated_rate_plan').options[0].text = "General";
					document.getElementById('updated_rate_plan').options[0].value = "ORG0001";
				}
			}
		}

		function init() {
			var ratePlanSelect = document.getElementById("updated_rate_plan");
			loadRatePlan();
			setSelectedIndex(ratePlanSelect,existingRatePlan);
			var bedTypeSelect = document.getElementById("updated_bed_type");
			loadSelectBox(bedTypeSelect, bedTypesJSON, 'bed_type_name', 'bed_type_name', "--Select--", "");
			setSelectedIndex(bedTypeSelect,existingBedType);
			toggleDateDisable();
		}

		function toggleDateDisable() {
			if(document.getElementById("apply_charge").value != 'D') {
				document.getElementById("toDate").disabled = true;
				document.getElementById("fromDate").disabled = true;
				document.getElementById("toTime").disabled = true;
				document.getElementById("fromTime").disabled = true;
			} else {
				document.getElementById("toDate").removeAttribute("disabled");
				document.getElementById("fromDate").removeAttribute("disabled");
				document.getElementById("toTime").removeAttribute("disabled");
				document.getElementById("fromTime").removeAttribute("disabled");

			}
		}

		function validate(){
			if(!checkIfVisitSelected())
				return false;
			checkNonInsuredBills();
			return true;
		}

		function checkIfVisitSelected() {
			var visitId = document.changeRatePlanForm.visitId.value;
			if(visitId == null || visitId == '' || visitId == 'null') {
				showMessage("js.billing.changerateplan.selectpatientvisit");
				return false;
			}
			return true;
		}

		var nonInsuredBills = "";
		function  checkNonInsuredBills(){
			nonInsuredBills = "";
			if(visitTpaId != ''){//only inserance patient
				for(var i = 0;i<allOpenBillsJSON.length;i++){
					var bill = allOpenBillsJSON[i];
					if(!bill.is_tpa)
						nonInsuredBills = nonInsuredBills+bill.billNo+",";
				}
				if( trim(nonInsuredBills) != '' && document.getElementById('updated_rate_plan').value != '' )
				alert(getString("js.billing.changerateplan.visithas.noninsurancebills")+nonInsuredBills.substring(0,nonInsuredBills.length-1)+"\n"+getString("js.billing.changerateplan.changerateplan.notapplicable"));

			}
		}
</script>
<insta:js-bundle prefix="billing.changerateplan"/>
<insta:js-bundle prefix="registration.patient"/>
</head>

<body onload="init();" class="yui-skin-sam">
<form name="someForm" action="changeRatePlan.do" method="GET">
<table width="100%">
	<tr>
		<td width="100%">
		<div class="pageheader" style="float: left"><insta:ltext key="billing.editvisitrateplan.bedtype.details.editvisitrate"/>
		<insta:ltext key="billing.editvisitrateplan.bedtype.details.planorbedtype"/></div>
		<div><insta:patientsearch searchType="visit"
				searchUrl="/editVisit/changeRatePlan.do" activeOnly="true"
				buttonLabel="Find" searchMethod="updateRatePlan" fieldName="visitId" />
		</div>
		</td>
	</tr>
</table>
</form>

<insta:feedback-panel />
<div><insta:patientdetails patient="${patient}" /></div>
<form name="changeRatePlanForm" action="changeRatePlan.do" method="GET">
	<input type="hidden" name="_method" value="setRatePlanDetails">
	<input type="hidden" name="visitId" value="${ifn:cleanHtmlAttribute(param.visitId)}"/>
	<input type="hidden" name="billNo" value="${ifn:cleanHtmlAttribute(param.billNo)}" />
	<input type="hidden" name="isNewUX" value="${ifn:cleanHtmlAttribute(param.isNewUX)}" />
	<input type="hidden" name="mrno" id="mrno" value="${patient.mr_no}" />
	<input type="hidden" name="rate_plan" id="rate_plan" value="" />
	<input type="hidden" name="existing_plan_id" id="existing_plan_id" value="" />

	<fieldset class="fieldsetborder" style="text-align:left;">
	<legend class="fieldSetLabel"><insta:ltext key="billing.editvisitrateplan.bedtype.details.editrateplaninformation"/></legend>
		<table class="formTable">
			<tr>
				<td class="formlabel"><insta:ltext key="billing.editvisitrateplan.bedtype.details.existingvisitrateplan"/>:</td>
				<td class="forminfo">${patient.org_name eq null || patient.org_name eq ''? (None) : patient.org_name}</td>
				<td class="formlabel"><insta:ltext key="billing.editvisitrateplan.bedtype.details.changevisitrateplanto"/>:</td>
				<td class="forminfo">
					<select id="updated_rate_plan" name="updated_rate_plan" size="1" class="dropdown">
						<option></option>
					</select>
				</td>
				<c:if test="${patient.primary_sponsor_id != '' &&  genPrefsRatePLan != null}">
					<td class="formlabel"><insta:ltext key="billing.editvisitrateplan.bedtype.details.noninsurancebillrateplan"/>: </td>
					<td class="forminfo">${genPrefsRatePLan.map.org_name} </td>
				</c:if>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="billing.editvisitrateplan.bedtype.details.existingbedtype"/>:</td>
				<td class="forminfo">${patient.bill_bed_type}</td>
				<td class="formlabel"><insta:ltext key="billing.editvisitrateplan.bedtype.details.changebedtypeto"/>:</td>
				<td>
					<select id="updated_bed_type" name="updated_bed_type" size="1" class="dropdown">
						<option></option>
					</select>
				</td>
				<td></td>
				<td></td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="billing.editvisitrateplan.bedtype.details.updatecharges"/>:</td>
				<td class="forminfo">
					<select id="apply_charge" name="apply_charge" class="dropDown"
						onChange="toggleDateDisable();" onBlur="toggleDateDisable();">
						<option value="N"><insta:ltext key="billing.editvisitrateplan.bedtype.details.none"/></option>
						<option value="Y"><insta:ltext key="billing.editvisitrateplan.bedtype.details.forallpreviouscharges"/></option>
						<option value="D"><insta:ltext key="billing.editvisitrateplan.bedtype.details.fordaterange"/></option>
					</select>
				</td>
				<td class="formlabel"><insta:ltext key="billing.editvisitrateplan.bedtype.details.from"/>:</td>
				<td class="forminfo">
					<div><insta:datewidget name="fromDate" id="fromDate" valueDate="${patient.reg_date}" />
					<input type="text" name="fromTime" id="fromTime" size="4"
							 value='<fmt:formatDate value="${patient.reg_time}" pattern="HH:mm"/>'
							 class="number" onBlur="doValidateTimeField(this);" />
					</div>
				</td>
				<td></td>
				<td></td>
			</tr>
			<tr>
				<td></td>
				<td></td>
				<td class="formlabel"><insta:ltext key="billing.editvisitrateplan.bedtype.details.to"/>:</td>
				<td class="forminfo">
					<div><insta:datewidget name="toDate" id="toDate" valueDate="<%= (new java.util.Date()) %>" />
					<input type="text" id="toTime" name="toTime" size="4"
							 value='<fmt:formatDate value="${serverNow}" pattern="HH:mm"/>'
							 class="number" onBlur="doValidateTimeField(this);" />
					</div>
				</td>
				<td></td>
				<td></td>
			</tr>
		</table>
	</fieldset>
	<table cellpadding="0" cellspacing="0" border="0" width="100%">
		<tr>
			<td align="left">
				<insta:accessbutton buttonkey="billing.editvisitrateplan.bedtype.details.save" type="submit" onclick="return validate();" />
				|&nbsp; <a href="${pageContext.request.contextPath}/editVisit/changeRatePlan.do?
							_method=updateRatePlan&amp;visitId=${patient.patient_id}&amp;billNo=${ifn:cleanURL(param.billNo)}"><insta:ltext key="billing.editvisitrateplan.bedtype.details.reset"/></a>
				| <a href="${pageContext.request.contextPath}/pages/registration/editvisitdetails.do?
							_method=getPatientVisitDetails&patient_id=${patient.patient_id}"><insta:ltext key="billing.editvisitrateplan.bedtype.details.editvisitdetails"/></a>
				<c:forEach var="bill" items="${bills}">
					| <insta:billLink mr_no="${mrNo}" bill_no="${bill.billNo}" visit_type="${visitType}" is_new_ux="${isNewUX}"/>
				</c:forEach>
			</td>
		</tr>
	</table>
</form>
</body>
</html>

