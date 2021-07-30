<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta name="i18nSupport" content="true"/>
	<title><insta:ltext key="registration.optoipconversion.details.optoipconversion"/></title>
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="js" file="ajax.js" />
	<insta:link type="js" file="registration/editPatientVisit.js"/>
	<insta:link type="js" file="registration/registrationCommon.js"/>
	<script>
	var modAdvInsurance ='${preferences.modulesActivatedMap['mod_adv_ins']}';
	var isModAdvanceIns = !empty(modAdvInsurance) && modAdvInsurance == 'Y';
	var allwdRatePlans = ${allowedRatePlansMap};
	var isTpaValidForIP = '${isTpaValidForIP}';
	var advancedOTModule = '${preferences.modulesActivatedMap['mod_advanced_ot']}';
	var isPlanExist = ${isPlanExist};
	advancedOTModule = (empty(advancedOTModule)) ? 'N' : advancedOTModule;


	function initOpIp() {
		initDoctorDept('');
		populateBedTypes();
		loadRatePlans();
	}

	function loadRatePlans(){
		var defltRatePlan = allwdRatePlans.defaultRatePlan;
		var errorMsg = allwdRatePlans.errorMsg;
		var ratePlanList = allwdRatePlans.ratePlanList;
		if(errorMsg == "") {
			loadSelectBox(document.getElementById('org_id'), ratePlanList, "ORG_NAME", "ORG_ID", "--Select--", "");
			setSelectedIndex(document.getElementById('org_id'), defltRatePlan);
		} else {
			alert(errorMsg);
			document.getElementById('org_id').length = 1;
		}
	}

	function onChangeDepartment() {
		var deptId = document.mainform.dept_name.value;
		document.mainform.dept_allowed_gender.value = '';
		setDeptAllowedGender(deptId);

		document.mainform.doctor.value = '';
		document.mainform.doctor_name.value = '';
		initDoctorDept(deptId);
	}
	function save() {
		var valid = true;
		var doctorObj = document.mainform.doctor_name;
		var bedtypeObj = document.mainform.bed_type;
		var wardObj = document.mainform.ward_id;
		var billNos = document.getElementsByName('bilNo');
		var billActions = document.getElementsByName('billAction');
		var primaryClaimStatus = document.getElementsByName('primaryClaimStatus');
		var secondaryClaimStatus = document.getElementsByName('secondaryClaimStatus');
		var isTpa = document.getElementsByName('isTpa');
		for (var i=0; i<billActions.length; i++) {
			if (billActions[i].value == '') {
				showMessage("js.registration.optoipconversion.selectbillaction");
				billActions[i].focus();
				return false;
			}

			if(isTpa[i].value == "true" && billActions[i].value == 'I' && (primaryClaimStatus[i].value == 'S' || secondaryClaimStatus[i].value == 'S')) {
				showMessage("js.registration.optoipconversion.notselectconnent.ipoption");
				return false;
			}

			if(isTpa[i].value == "true" && billActions[i].value == 'C' && isPlanExist) {
				var ok = confirm(" Warning: Copy charge item to IP bill later bill action will recalculate claim  \n " +
								"amounts according to IP visit plan rules in bill " + billNos[i].value +". \n " +
				 				"Do you want to proceed ? ");
				if (!ok) {
					return false;
				}
			}
		}

		valid = valid && validateRequired(doctorObj, getString("js.registration.optoipconversion.doctorrequired"));
		if(!valid) {
			doctorObj.focus();
			return false;
		}
		valid = valid && validateRequired(bedtypeObj, getString("js.registration.optoipconversion.bedtyperequired"));
		if(!valid) {
			bedtypeObj.focus();
			return false;
		}
		valid = valid && validateRequired(wardObj, getString("js.registration.optoipconversion.wardrequired"));
		if(!valid) {
			wardObj.focus();
			return false;
		}
		valid = valid && validateDeptAllowedGenderForPatient();
		if(!valid) {
			document.mainform.dept_name.focus();
			return false;
		}
		if (document.getElementById('ipApplicable').value=="N") {
			showMessage("js.registration.optoipconversion.plannotapplicableforip");
			return false;
		}
		if (!checkRatePlanValidity()) {
			document.mainform.org_id.focus();
			return false;
		}


		if ((document.mainform.appointId) && (document.mainform.appointId.value != '') && (document.mainform.appointId.value != '0')
			&& (document.mainform.category) && (document.mainform.category.value == 'OPE')) {
			if(advancedOTModule == "Y") {
			} else
				var msg=getString("js.registration.optoipconversion.patientsurgeryscheduled");
				msg+="\n";
				msg+=getString("js.registration.optoipconversion.surgerywillbeordered");
				alert(msg);
		}

		if(isTpaValidForIP == 'N') {
			showMessage('js.registration.optoipconversion.existingtpa.or.sponsor');
		}
		document.mainform.submit();
		return true;
	}
	</script>
<insta:js-bundle prefix="registration.patient"/>
<insta:js-bundle prefix="registration.optoipconversion"/>
<insta:js-bundle prefix="common.commonselectbox"/>
<insta:js-bundle prefix="registration.editvisitdetails"/>
</head>
<body onload="initOpIp()">
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="patientgender">
<insta:ltext key="selectdb.dummy.value"/>,
<insta:ltext key="registration.optoipconversion.details.male"/>,
<insta:ltext key="registration.optoipconversion.details.female"/>,
<insta:ltext key="registration.optoipconversion.details.others"/>
</c:set>
<div class="pageHeader"><insta:ltext key="registration.optoipconversion.details.optoipconversion"/></div>
<insta:feedback-panel/>

<c:if test="${isTpaValidForIP eq 'N'}">
	<div class="helpPanel">
		<table>
			<tr>
				<td valign="top" style="width: 30px"><img src="${cpath}/images/information.png"/></td>
				<td style="padding-bottom: 5px">
						<insta:ltext key="registration.optoipconversion.details.template1"/> <insta:ltext key="registration.optoipconversion.details.template2"/><a href="${cpath}/editVisit/changeTPA.do?_method=changeTpa&visitId=${ifn:cleanURL(visitId)}" target="_blank">'<insta:ltext key="registration.optoipconversion.details.add.editinsurance"/>'</a> <insta:ltext key="registration.optoipconversion.details.screen"/>.
				</td>
			</tr>
		</table>
	</div>
</c:if>
<insta:patientdetails  visitid="${visitId}" />
<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel"><insta:ltext key="patient.header.fieldset.otherdetails"/></legend>
	<table class="patientdetails" style="width:100%">
		 <tr>
		 	 <fmt:formatDate var="rdate" value="${patient.reg_date}" pattern="dd-MM-yyyy"/>
 			 <fmt:formatDate var="rtime" value="${patient.reg_time}" pattern="HH:mm"/>
 			 <td class="formlabel"><insta:ltext key="registration.optoipconversion.details.reg"/>.&nbsp;<insta:ltext key="registration.optoipconversion.details.date"/>&<insta:ltext key="registration.optoipconversion.details.time"/>:</td>
 			 <td class="forminfo"> ${rdate} ${rtime}
				<c:set var="reg_date" value="${rdate}"/>
				<c:set var="reg_time" value="${rtime}"/>
			</td>
			<c:choose>
				<c:when test="${not empty regPrefFields.oldRegNumField}">
					<td class="formlabel">${ifn:cleanHtml(regPrefFields.oldRegNumField)}:</td>
				 	<td class="forminfo"><label id="oldmrnoLabel"></label></td>
				 	<td class="formlabel"></td>
				 	<td></td>
				</c:when>
				<c:otherwise>
					<td class="formlabel"></td>
				 	<td></td>
				 	<td class="formlabel"></td>
				 	<td></td>
				</c:otherwise>
       		</c:choose>
       </tr>
	</table>
</fieldset>


<form method="post" name="mainform" action="OPIPConversion.do">
<input type="hidden" name="method" value="saveOPIPConversion">
<input type="hidden" name="patient_id" value="${ifn:cleanHtmlAttribute(visitId)}">
<input type="hidden" name="patient_gender" value="${patient.patient_gender}">
<input type="hidden" id="gender" value="${ifn:cleanHtmlAttribute(gender)}">
<input type="hidden" name="prescription_id" value="">
<input type="hidden" id="ipApplicable" value="${ipApplicable}">
<input type="hidden" name="appointId" value="${ifn:cleanHtmlAttribute(appointId)}">
<input type="hidden" name="reg_date" value="${rdate}">
<input type="hidden" name="reg_time" value="${rtime}">
<input type="hidden" name="dept_allowed_gender" value="">
<c:if test="${not empty appointId}">
	<input type="hidden" name="category" value="${ifn:cleanHtmlAttribute(category)}">
	<input type="hidden" name="mrno" value="${ifn:cleanHtmlAttribute(mrno)}">
</c:if>

	<h3><insta:ltext key="registration.optoipconversion.details.selectaction"/>: ${ifn:cleanHtml(visitId)}</h3>
	
<div class="resultList" style="margin: 10px 0px 5px 0px;">
	<table class="datatable" width="100%">
		<tr>
			<td><insta:ltext key="registration.optoipconversion.details.billtype"/></td>
			<td><insta:ltext key="registration.optoipconversion.details.billnumber"/></td>
			<td><insta:ltext key="registration.optoipconversion.details.billamount"/></td>
			<td><insta:ltext key="registration.optoipconversion.details.status"/></td>
			<td><insta:ltext key="registration.optoipconversion.details.action"/></td>
			<td></td>
		</tr>

		<c:set var="bill" value="${patientBills}"></c:set>
		<c:forEach var="billDetails" items="${bill}" varStatus="st">
			<c:set var="i" value="${(st.index)+1}" />
			<c:if test="${!(billDetails.restrictionType != 'P' && billDetails.totalAmount lt 0)}">
			<tr>
				<td>
					<input type="hidden" name="bilType${i}" value="${billDetails.billType}">
					<c:if test="${billDetails.billType eq 'P'}"><insta:ltext key="registration.optoipconversion.details.billnow"/></c:if>
					<c:if test="${billDetails.billType eq 'C' && billDetails.restrictionType eq 'N'}"><insta:ltext key="registration.optoipconversion.details.billlater"/></c:if>
					<c:if test="${billDetails.billType eq 'M' && billDetails.visitType eq 'o'}"><insta:ltext key="registration.optoipconversion.details.pharmacybillnow"/></c:if>
					<c:if test="${billDetails.billType eq 'C' && billDetails.restrictionType eq 'P'}"><insta:ltext key="registration.optoipconversion.details.pharmacybilllater"/></c:if>
					<c:if test="${billDetails.billType eq 'R' && billDetails.visitType eq 'o'}"><insta:ltext key="registration.optoipconversion.details.pharmacybillnow"/></c:if>
				</td>
				<td>${billDetails.billNo}</td>
				<td>${billDetails.totalAmount }</td>
				<td>
					<c:if test="${billDetails.status eq 'A'}"><insta:ltext key="registration.optoipconversion.details.open"/></c:if>
					<c:if test="${billDetails.status eq 'C'}"><insta:ltext key="registration.optoipconversion.details.closed"/></c:if>
					<c:if test="${billDetails.status eq 'X'}"><insta:ltext key="registration.optoipconversion.details.cancelled"/></c:if>
					<c:if test="${billDetails.status eq 'F'}"><insta:ltext key="registration.optoipconversion.details.finalized"/></c:if>
					<c:if test="${billDetails.status eq 'S'}"><insta:ltext key="registration.optoipconversion.details.settled"/></c:if>
					<input type="hidden" name="primaryClaimStatus" value="${billDetails.primaryClaimStatus}"/>
					<input type="hidden" name="secondaryClaimStatus" value="${billDetails.secondaryClaimStatus}"/>
					<input type="hidden" name="isTpa" value="${billDetails.is_tpa}"/>
				</td>
				<!-- Paitent Bill Now -->
				<c:if test="${billDetails.billType eq 'P'}">
					<td>
						<input type="hidden" name="bilNo" value="${billDetails.billNo}">
						<select name="billAction" class="dropdown">
							<option value="">${dummyvalue}</option>
							<option value="L"><insta:ltext key="registration.optoipconversion.details.leaveasop"/></option>
							<option value="I"><insta:ltext key="registration.optoipconversion.details.connecttoipvisit.leaveasbillnow"/></option>
							<c:if test="${billDetails.status eq 'A'}">
								<option value="C"><insta:ltext key="registration.optoipconversion.details.copychargeitems.ipbilllaterbill"/></option>
							</c:if>
						</select>
						<c:if test="${fn:length(creditNoteBillsMap[billDetails.billNo]) > 0}">
						<img class="imgHelpText" src="${cpath}/images/information.png"
							title='The action will applicable for credit notes also'/>
						</c:if>
					</td>
				</c:if>
				<!-- Patient Bill Later -->
				<c:if test="${billDetails.billType eq 'C' && billDetails.restrictionType eq 'N'}">
					<td>
						<c:if test="${billDetails.status ne 'X' && billDetails.status eq 'A'}">
							<input type="hidden" name="bilNo" value="${billDetails.billNo}">
							<select name="billAction" class="dropdown">
								<option value="">${dummyvalue}</option>
								<option value="L"><insta:ltext key="registration.optoipconversion.details.leaveasop"/></option>
								<option value="C"><insta:ltext key="registration.optoipconversion.details.copychargeitems.ipbilllaterbill"/></option>
							</select>
						</c:if>
						<c:if test="${billDetails.status ne 'X' && billDetails.status eq 'C'}">
							<input type="hidden" name="bilNo" value="${billDetails.billNo}">
							<select name="billAction" class="dropdown">
								<option value="">${dummyvalue}</option>
								<option value="L"><insta:ltext key="registration.optoipconversion.details.leaveasop"/></option>
								<option value="I"><insta:ltext key="registration.optoipconversion.details.connecttoipvisit.leaveasbilllater"/></option>
							</select>
						</c:if>
						<c:if test="${billDetails.status ne 'X' && billDetails.status eq 'F' || billDetails.status eq 'S'}">
							<input type="hidden" name="bilNo" value="${billDetails.billNo}">
							<select name="billAction" class="dropdown">
								<option value="">${dummyvalue}</option>
								<option value="L"><insta:ltext key="registration.optoipconversion.details.leaveasop"/></option>
								<option value="I"><insta:ltext key="registration.optoipconversion.details.connecttoipvisit.leaveasbilllater"/></option>
							</select>
						</c:if>
						<c:if test="${fn:length(creditNoteBillsMap[billDetails.billNo]) > 0}">
						<img class="imgHelpText" src="${cpath}/images/information.png"
							title='The action will applicable for credit notes also'/>
						</c:if>
					</td>
				</c:if>
				<!-- Pharmacy Bill Now -->
				<c:if test="${billDetails.billType eq 'M' && billDetails.visitType eq 'o'}">
					<td>
						<input type="hidden" name="bilNo" value="${billDetails.billNo}">
						<select name="billAction" class="dropdown">
							<option value="">${dummyvalue}</option>
							<option value="L"><insta:ltext key="registration.optoipconversion.details.leaveasop"/></option>
							<option value="I"><insta:ltext key="registration.optoipconversion.details.connecttoipvisit.leaveasbillnow"/></option>
						</select>
					</td>
					<c:if test="${fn:length(creditNoteBillsMap[billDetails.billNo]) > 0}">
					<img class="imgHelpText" src="${cpath}/images/information.png"
						title='The action will applicable for credit notes also'/>
					</c:if>
				</c:if>
				<c:if test="${billDetails.billType eq 'R' && billDetails.visitType eq 'o'}">
					<td>
						<input type="hidden" name="bilNo" value="${billDetails.billNo}">
						<select name="billAction" class="dropdown">
							<option value="">${dummyvalue}</option>
							<option value="L"><insta:ltext key="registration.optoipconversion.details.leaveasop"/></option>
							<option value="I"><insta:ltext key="registration.optoipconversion.details.connecttoipvisit.leaveasbillnow"/></option>
						</select>
					</td>
					<c:if test="${fn:length(creditNoteBillsMap[billDetails.billNo]) > 0}">
					<img class="imgHelpText" src="${cpath}/images/information.png"
						title='The action will applicable for credit notes also'/>
					</c:if>
				</c:if>
				<!-- Pharmacy Bill later -->
				<c:if test="${billDetails.billType eq 'C' && billDetails.restrictionType eq 'P'}">
					<td>
						<c:if test="${billDetails.status ne 'X' && billDetails.status ne 'S' && billDetails.status ne 'F'}">
							<input type="hidden" name="bilNo" value="${billDetails.billNo}">
							<select name="billAction" class="dropdown">
								<option value="">${dummyvalue}</option>
								<option value="L"><insta:ltext key="registration.optoipconversion.details.leaveasop"/></option>
								<option value="I"><insta:ltext key="registration.optoipconversion.details.connecttoipvisit.leaveaspharmacybilllater"/></option>
							</select>
						</c:if>
						<c:if test="${fn:length(creditNoteBillsMap[billDetails.billNo]) > 0}">
						<img class="imgHelpText" src="${cpath}/images/information.png"
							title='The action will applicable for credit notes also'/>
						</c:if>
					</td>
				</c:if>
				<td>					
					<c:set var="creditNoteBills" value="${creditNoteBillsMap[billDetails.billNo]}"></c:set>
					<c:set var="numpCreditNotes" value="${fn:length(creditNoteBills)}"/>
					<c:forEach var="creditNoteBill" items="${creditNoteBills}" varStatus="st">
						<c:set var="l" value="${(st.index)}" />
						<a href="${cpath}/billing/viewCreditNote.do?_method=viewCreditNote&billNo=${creditNoteBill.map.credit_note_bill_no}" 
							target="_blank">${creditNoteBill.map.credit_note_bill_no}</a>
						${(st.index+1) < numpCreditNotes ? ',' : ''}
					</c:forEach>
				</td>
			</tr>
		</c:if>
		</c:forEach>
	</table>
</div>
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="registration.optoipconversion.details.visitinformation"/></legend>
	<table class="formtable" style="margin-top: 5px;">
		<tr>
			<td class="formlabel"><insta:ltext key="registration.optoipconversion.details.department"/>:</td>
			<td>
				<select name="dept_name" id="dept_name" class="dropdown" onchange="onChangeDepartment()">
					<option value=""><insta:ltext key="registration.optoipconversion.details.departmentoption"/></option>
					<c:forEach items="${arrdeptDetails}" var="deptDetails">
						<option value="${deptDetails.DEPT_ID}">${deptDetails.DEPT_NAME}</option>
					</c:forEach>
				</select>
			</td>
			<td class="formlabel"><insta:ltext key="registration.optoipconversion.details.doctor"/>:</td>
			<td colspan="2" valign="top">
				<%-- Doctor - Department Autocomplete --%>

				<div id="doc_dept_wrapper" style="width: 250px;">
					<input type="text" name="doctor_name" id="doctor_name"
						 style="width: 250px;"/>
				<div id="doc_dept_dropdown"></div>
				<input type="hidden" name="doctor" id="doctor">
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="registration.optoipconversion.details.bedtype"/>:</td>
			<td>
				<select name="bed_type" id="bed_type" class="dropdown" onchange="onChangeBedType(this, document.mainform.ward_id)">
					<option value="">${dummyvalue}</option>
				</select>
			</td>
			<td class="formlabel"><insta:ltext key="registration.optoipconversion.details.ward"/>:</td>
			<td>
				<select name="ward_id" id="ward_id" class="dropdown" onchange="onWardChang()">
					<option value="">${dummyvalue}</option>
				</select>
			</td>
		</tr>
	</table>
	</fieldset>
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="registration.optoipconversion.details.paymentinformation"/></legend>
		<table  class="formtable" style="margin-top: 5px;">
			<tr>
				<td class="formlabel" style="width:225px;"><insta:ltext key="registration.optoipconversion.details.rateplan"/>:</td>
				<c:set var="ratePlan" value="${not empty patient.org_id ? patient.org_id : ''}" />
				<td style="padding-right:0px;">
					<select name="org_id" id="org_id" class="dropdown">
						<option value="">${dummyvalue}</option>
					</select>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
		</table>
	</fieldset>



	<div class="screenActions">
		<button type="button" name="Save" accesskey="S" onclick="save()"><insta:ltext key="registration.optoipconversion.details.save"/></button>
		| <a href="${pageContext.request.contextPath}/pages/registration/ManageVisits/OPIPConversion.do?method=getOPIPConversion&patient_id=${ifn:cleanURL(visitId)}"><insta:ltext key="registration.optoipconversion.details.reset"/></a>
		| <a href="${pageContext.request.contextPath}/pages/registration/editvisitdetails.do?_method=getPatientVisitDetails&patient_id=${ifn:cleanURL(visitId)}&ps_status=active"><insta:ltext key="registration.optoipconversion.details.editvisitdetails"/></a>
	</div>
</form>
<script>
	var bedCharges = ${bedChargesJson};
	var jDocDeptNameList = <%= request.getAttribute("docDeptNameList") %>;
	var deptList = ${deptsList};
	var isTpaPatient = '${isTpa}';
</script>
</body>
</html>