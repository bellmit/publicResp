<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@page import="com.insta.hms.common.Encoder" %>

<html>
<head>
	<title><insta:ltext key="services.serviceconduction.list.title1"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="service/serviceconduction.js"/>
	<insta:link type="script" file="outpatient/insta_section.js"/>
	<insta:link type="js" file="outpatient/diagnosis_details.js"/>
	<insta:link type="script" file="outpatient/allergies.js"/>
	<insta:link type="script" file="outpatient/preAnaesthestheticCheckup.js"/>
	<insta:link type="script" file="outpatient/healthMaintenance.js"/>
	<insta:link type="script" file="vitalreadings/vitalreadings.js"/>
	<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />

	<style>
		.yui-ac {
			padding-bottom: 20px;
		}
		.complaintAc {
		    padding-bottom:2em;
		}
		.scrolForContainer .yui-ac-content {
			 max-height:11em;overflow:auto;overflow-x:auto; /* scrolling */
		}
	</style>

	<script>
		var collapsiblePanels = {};
		var insta_form_json = ${insta_form_json};
		var sys_generated_forms = ${sys_generated_forms};
		var roleId = '${roleId}';
		var doctors = <%= request.getAttribute("doctors_list") %>;
		var phrase_suggestions_json = ${phrase_suggestions_json};
		var phrase_suggestions_by_dept_json = ${phrase_suggestions_json};
		var consumablesJSON = <%= request.getAttribute("serviceConsumablesJSON") %>;
		var medicineStoreStockDetailsJSON = <%= request.getAttribute("medicineStockDetailsJSON") %>;
		var allowConsumableQtyIncrease = '${genPrefs.allowConstantConsumableQtyIncrease}';
		var consumableStockNegative = '${genPrefs.consumableStockNegative}';
		var serviceconsumablesjson = <%= request.getAttribute("serviceconsumablesjson") %>;
		var prescribedQty = '${serviceBean.map.quantity}';
		var group_patient_sections = '${group_patient_sections}';
		var insta_sections_json = ${insta_sections_json};

		var validate_diagnosis_codification = '${genericPrefs.validate_diagnosis_codification}';
		var mod_mrd_icd =  '${preferences.modulesActivatedMap.mod_mrd_icd}';
		var diagnosis_code_type = '<%=Encoder.cleanJavaScript((String)request.getAttribute("defaultDiagnosisCodeType")) %>';

		var conductedFlag = "${serviceBean.map.conducted}";
		var complaintForm = null;
		var paramType = "V";
		var mod_ceed_enabled = ${preferences.modulesActivatedMap.mod_ceed_integration == 'Y'};
		var editable_sections = ${ifn:convertListToJson(section_rights)};
		var all_section_edit_rights = ${roleId == 1 || roleId == 2};

		function validateConductionDate() {
		    var result = validateBlankDateCheck();
		    if (!result) {
		        return false;
		    } else {
            var d1 = document.getElementById("conducted_end_date").value;
            var d2 = document.getElementById("conducted_date").value;
            if(d1 != "" && d2 != "") {
            d1 = new Date(d1.replace( /(\d{2})-(\d{2})-(\d{4})/, "$2/$1/$3") );
            d2 = new Date(d2.replace( /(\d{2})-(\d{2})-(\d{4})/, "$2/$1/$3") );
                if (d1 < d2) {
                alert ("Conducted Date cannot be greater than Conducted End Date.");
                return false;
              } else if (d1.getTime() === d2.getTime()) {
                var t1 = document.getElementById("conducted_end_time").value;
                var t2 = document.getElementById("conducted_time").value;
                if(t2>t1) {
                alert("Conducted Time cannot be greater than Conducted End Time");
                return false;
                }
              }
            }
           }
           return true;
		}

		function validateBlankDateCheck(){
		    var d1 = document.getElementById("conducted_end_date");
		    var d2 = document.getElementById("conducted_date");
		    var t1 = document.getElementById("conducted_end_time");
		    var t2 = document.getElementById("conducted_time");
		    if (d1 != null && d2 != null) {
		        if (document.getElementById("conducted_end_date").value == ""
		        || document.getElementById("conducted_date").value == ""){
		            alert("Conduction End Date & Conducted Date Cannot be blank");
		            return false;
		        }
		    if (t1 != null && t2 != null) {
		        if (document.getElementById("conducted_end_time").value == ""
		        || document.getElementById("conducted_time").value == ""){
		            alert("Conduction End Time & Conducted Time Cannot be blank");
		            return false;
		        }
		    } else {
		        alert("Conduction End Date and Conducted Date " +
		        "or Conduction End Time & Conducted Time Cannot be blank");
		        return false;
		    }
		    return true;
		  }
		}

		function validate() {
			var conDocRequired = document.serviceConductionForm.conducting_doc_mandatory;
			var conDoctor = document.serviceConductionForm.conducting_doctor;
			if ((conDocRequired.value == 'O' || conDocRequired.value == 'C') && trim(conDoctor.value) == '') {
				showMessage("js.services.serviceconduction.conductingdoctor.required");
				conDoctor.focus();
				return false;
			}
			// validate mandatory fields in physician forms.
			if (!validateMandatoryFields()) return false;
			if (!validateSysGenForms()) return false;
			if (!validateEditedQuantity()) {
				return false;
			}

			if (!checkUserStore()) return false;

			var isConducted = document.getElementById("completed").checked;
			var conducted = document.getElementById("completed").disabled;

			if (isConducted && !conducted) {
				if (!validateAvailablityOfItemInStore()) return false;
				if (!validateConsumableAvailableQty()) return false;
			}

			if(allowConsumableQtyIncrease == 'N' && !checkConsumableQtyWithMasterQty()) {
				return false;
			}

		if (document.getElementById('conducted_time').value != '') {
			var valid = true;
			if (document.getElementById('conducted_date').value == '') {
				alert("Please enter the Conducted Date.");
				document.getElementById('conducted_date').focus();
				return false;
			}
			valid = valid && validateTime(document.getElementById('conducted_time'));
			if (!valid) return false;
		}
		if (document.getElementById('conducted_end_time').value != '') {
        	var valid = true;
        	valid = valid && validateTime(document.getElementById('conducted_end_time'));
        	if (!valid) return false;
        }
		
			return true;
		}
		function ValidatesaveAndAddDocument(addReport) {
	        var conductionResult = validateConductionDate();
			var result=validate();
			if(!result || !conductionResult) {
				return false ;
			} else {
				document.serviceConductionForm.addReport.value = addReport;
				document.serviceConductionForm.submit();
				return true;
			}
		}
		function ValidateSaveAndPrint(print) {
	        var conductionResult = validateConductionDate();
			var result=validate();
			if(!result || !conductionResult) {
				return false ;
			} else {
				document.serviceConductionForm.isPrint.value = print;
				document.serviceConductionForm.submit();
				return true;
			}
		}
		function showConductionEndDate() {
			var visibility = conductedFlag == 'R' || (document.getElementById('completed').checked && !document.getElementById('completed').disabled)? 'visible' : 'hidden';
				document.getElementById('conducted_end_time').style.visibility = visibility;
				document.getElementById('conducted_end_date').style.visibility = visibility;
				document.getElementById('datewidget').style.visibility = visibility;
				document.getElementById('enddateinput').style.visibility = visibility;
				document.getElementById('enddatelabel').style.visibility = visibility;
		}
		
	</script>
	<insta:link type="script" file="outpatient/complaint.js"/>
<insta:js-bundle prefix="services.serviceconduction"/>
<insta:js-bundle prefix="outpatient.consultation.mgmt"/>
<insta:js-bundle prefix="outpatient.vitalform"/>
<insta:js-bundle prefix="registration.patient"/>
</head>
<body onload="init();ajaxForPrintUrls();">
<c:set var="report">
 <insta:ltext key="services.serviceconduction.addedit.report"/>
</c:set>
<c:set var="save">
 <insta:ltext key="services.serviceconduction.addedit.saveand"/>
</c:set>
<c:set var="savereport">
 <insta:ltext key="services.serviceconduction.addedit.save.report"/>
</c:set>
<c:set var="cText">
 <insta:ltext key="services.serviceconduction.addedit.c"/>
</c:set>
<c:set var="disaledText">
 <insta:ltext key="services.serviceconduction.addedit.disaled"/>
</c:set>
<c:set var="pendingserviceslist">
 <insta:ltext key="services.serviceconduction.addedit.pendingserviceslist"/>
</c:set>
<c:set var="updateText">
 <insta:ltext key="services.serviceconduction.addedit.update"/>
</c:set>
<c:set var="addText">
 <insta:ltext key="services.serviceconduction.addedit.add"/>
</c:set>
<c:set var="r">
 <insta:ltext key="services.serviceconduction.addedit.r"/>
</c:set>
<c:set var="eport">
 <insta:ltext key="services.serviceconduction.addedit.eport"/>
</c:set>
<c:set var="msg">
<insta:ltext key="services.serviceconduction.list.message"/>
</c:set>
	<h1><insta:ltext key="services.serviceconduction.addedit.title"/></h1>
	<insta:feedback-panel/>
	<insta:patientdetails  visitid="${serviceBean.map.patient_id}" showClinicalInfo="true"/>
	<form method="POST" action="Services.do" name="serviceConductionForm"  autocomplete="off">
		<jsp:include flush="true" page="/pages/outpatient/CommonInclude.jsp"/>

		<input type="hidden" name="_method" value="conduct"/>
		<input type="hidden" name="service_id" value="${serviceBean.map.service_id}"/>
		<input type="hidden" name="prescription_id" value="${serviceBean.map.prescription_id}"/>
		<%--  following fields are used for service report add/editing --%>
		<input type="hidden" name="addReport" value="false"/>
		<input type="hidden" name="conducting_doc_mandatory" value="${serviceBean.map.conducting_doc_mandatory}"/>
		<input type="hidden" name="reportId" value="${serviceBean.map.doc_id}"/>
		<input type="hidden" name="template_id" value="${serviceBean.map.template_id}"/>
		<input type="hidden" name="format" value="${serviceBean.map.doc_format}"/>
		<input type="hidden" name="patient_id" value="${serviceBean.map.patient_id}"/>
		<input type="hidden" name="mr_no" value="${serviceBean.map.mr_no}"/>
		<input type="hidden" name="insta_form_id" value="${form.map.form_id}"/>
		<c:set var="readOnly" value="${serviceBean.map.conducted == 'C' ? 'readOnly' : ''}" />
		<c:set var ="reopenedState" value="${serviceBean.map.conducted == 'C' || serviceBean.map.conducted == 'R' ? 'readOnly' : ''}" />
		<input type="hidden" name="saveRequired" value="${serviceBean.map.conducted ne 'C'}" />
		<input type="hidden" name="isPrint" id="isPrint" value="false"/>

		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="services.serviceconduction.addedit.conductiondetails"/></legend>
			<table class="formtable">
				<tr>
					<td class="formlabel"><insta:ltext key="services.serviceconduction.addedit.servicename"/> </td>
					<td class="forminfo">
						<c:if test="${not empty serviceBean.map.tooth_unv_number || not empty serviceBean.map.tooth_fdi_number}">
							<c:set var="tooth_number" value="[${empty serviceBean.map.tooth_unv_number ? serviceBean.map.tooth_fdi_number : serviceBean.map.tooth_unv_number}]"/>
						</c:if>
						${serviceBean.map.service_name} ${tooth_number}
					</td>
					<td class="formlabel"><insta:ltext key="services.serviceconduction.addedit.servicedepartment"/> </td>
					<td class="forminfo">${serviceBean.map.department}</td>
					<td class="formlabel"><insta:ltext key="services.serviceconduction.addedit.qtyordered"/> </td>
					<td class="forminfo"><fmt:formatNumber value="${serviceBean.map.quantity}" pattern="0"/></td>
				</tr>
				
				<jsp:useBean id="currentDate" class="java.util.Date"/>
				<fmt:formatDate var="dateVal" value="${currentDate}" pattern="dd-MM-yyyy"/>
				<fmt:formatDate var="timeVal" value="${currentDate}" pattern="HH:mm"/>
				<tr>
					<td class="formlabel"><insta:ltext key="services.serviceconduction.addedit.conducteddate"/> </td>
					<td class="forminfo">
						<c:choose>
							<c:when test="${serviceBean.map.conducted == 'C'}">
								<fmt:formatDate value="${serviceBean.map.conducteddate}" pattern="dd-MM-yyyy" />&nbsp;
								<fmt:formatDate value="${serviceBean.map.conducteddate}" pattern="HH:mm"/>
								<input type="hidden" id="conducted_time" value="<fmt:formatDate value="${serviceBean.map.conducteddate}" pattern="HH:mm"/>"/>
								<input type="hidden" id="conducted_date" value="<fmt:formatDate value="${serviceBean.map.conducteddate}" pattern="dd-MM-yyyy"/>"/>
							</c:when>
							<c:when test= "${serviceBean.map.conducteddate!=null && serviceBean.map.conducteddate!=null}">
                            <fmt:formatDate var="date1Val" value="${serviceBean.map.conducteddate}" pattern="dd-MM-yyyy"/>
                            <fmt:formatDate var="time1Val" value="${serviceBean.map.conducteddate}" pattern="HH:mm"/>
                            <insta:datewidget id="conducted_date" name="conducted_date" value="${date1Val}"/>
                            <input type="text" name="conducted_time" maxlength="5" id="conducted_time" value="${time1Val}" style="width: 40px;">
                            </c:when>
							<c:otherwise>
								<insta:datewidget id="conducted_date" name="conducted_date" value="today"/>
								<input type="text" name="conducted_time" maxlength="5" id="conducted_time" value="${timeVal}" style="width: 40px;">
							</c:otherwise>
						</c:choose>
					</td>

					<td class="formlabel"><insta:ltext key="services.serviceconduction.addedit.conductedby"/> </td>
					<td>
						<div id="autocomplete" style="padding-bottom: 20px;width:200px;">
							<input type="text" name="conducting_doctor" id="conducting_doctor"
								value="${serviceBean.map.conducting_doctor}" ${reopenedState}/>
							<div id="doctorContainer" style="width:300px;"></div>
						</div>
						<input type="hidden" name="conducting_doctor_id" id="conducting_doctor_id" value="${serviceBean.map.conductedby}">
					</td>
					<td class="formlabel"><insta:ltext key="services.serviceconduction.addedit.completed"/> </td>
					<td><input type="checkbox" name="completed" id="completed" value="C" onClick= "showConductionEndDate();"
					${serviceBean.map.conducted == 'C' ? 'checked ' : ''}
					 ${serviceBean.map.conducted == 'C' ? 'disabled' : ''}/></td>
				</tr>
				<tr>		
						<c:choose>
							<c:when test="${serviceBean.map.conducted == 'C'}">
							<td class="formlabel"><insta:ltext
							key="services.serviceconduction.addedit.conducted.enddate" /></td>
							<td class="forminfo" >
							<fmt:formatDate value="${serviceBean.map.conducted_end_date}" pattern="dd-MM-yyyy" />&nbsp;
								<fmt:formatDate value="${serviceBean.map.conducted_end_date}" pattern="HH:mm"/>
								<input type="hidden" id="conducted_end_time" value="<fmt:formatDate value="${serviceBean.map.conducted_end_date}" pattern="HH:mm"/>"/>
								<input type="hidden" id="conducted_end_date" value="<fmt:formatDate value= "${serviceBean.map.conducted_end_date}" pattern="dd-MM-yyyy"/>"/>
								</td>
								</c:when>
                            <c:when test="${serviceBean.map.conducted == 'R'}">
                            <td class="formlabel">
                                <insta:ltext key="services.serviceconduction.addedit.conducted.enddate" />
                            </td>
                            <td class="forminfo" >
                                <fmt:formatDate var="dateVal" value="${serviceBean.map.conducted_end_date}" pattern="dd-MM-yyyy"/>
                                <fmt:formatDate var="timeVal" value="${serviceBean.map.conducted_end_date}" pattern="HH:mm"/>
                                <insta:datewidget id="conducted_end_date" name="conducted_end_date" value="${dateVal}"/>
                                <input type="text" name="conducted_end_time" maxlength="5" id="conducted_end_time" value="${timeVal}" style="width: 40px;">
                            </td>
						    </c:when>
							<c:otherwise>
							<td class="formlabel" id="enddatelabel" style="visibility: hidden" ><insta:ltext
							key="services.serviceconduction.addedit.conducted.enddate"/></td>
							<td class="forminfo" id="enddateinput" style="visibility: hidden">
								<div id="datewidget" name="datewidget" style="visibility: hidden">
									<insta:datewidget
										name="conducted_end_date" value="today" />
									<input type="text" name="conducted_end_time" maxlength="5"
										id="conducted_end_time" value="${timeVal}"
										style="width: 40px; visibility: hidden">	
								</div>
								</td>
							</c:otherwise>
						</c:choose>

						
					<td class="formlabel"><insta:ltext key="services.serviceconduction.addedit.remarks"/> </td>
					<td><input type="text" name="remarks" value="${serviceBean.map.remarks}" ${readOnly}></td>
				</tr>
			</table>
		</fieldset>

	 <c:forTokens  items="${form.map.sections}" delims="," var="section_id">
		<c:set var="sectionsCount" value="${section_id > 0 ? sectionsCount+1 : sectionsCount}"/>
	</c:forTokens>


	<c:set var="fieldsetField" value="false" />
	<c:set var="pfIndex" value="0"/>
		<c:forTokens items="${form.map.sections}" delims="," var="section_id" varStatus="s_i" >
			<c:if test="${section_id > 0 && group_patient_sections == 'Y'}">
			<c:forEach items="${insta_sections}" var="patient_form_group" varStatus="j">
				<c:if test="${patient_form_group.map.section_id == section_id && patient_form_group.map.linked_to == 'patient'}">
					<c:if test="${pfIndex == 0}">
						<c:set var="pfIndex" value="${pfIndex+1}"/>
						<c:set var="fieldsetField" value="true" />
						<div id="CollapsiblePanel" class="CollapsiblePanel">
							<div class=" title CollapsiblePanelTab"  style=" border-left:none;">
								<div id="patient_history_label" class="fltL " style="width: 230px; margin:5px 0px 0px 10px;"><b><i>Patient History</i></b></div>
								<div class="fltR txtRT" style="width: 25px; margin: 10px 10px 0px;">
									<img src="${cpath}/images/down.png" />
								</div>
								<div class="clrboth"></div>
							</div>
						<fieldset class="fieldSetBorder" id="fieldset" style="background-color: #F8FCFF; border-style:none ">
					</c:if>
						<jsp:include page="/pages/outpatient/InstaSection.jsp">
							<jsp:param name="section_id" value="${section_id}"/>
							<jsp:param name="service_pres_id" value="${param.prescription_id}"/>
							<jsp:param name="form_name" value="serviceConductionForm"/>
							<jsp:param name="form_type" value="Form_Serv"/>
							<jsp:param name="displayExpandedSection" value="${sectionsCount <= 10 ? 'block': 'none'}"/>
							<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
							<jsp:param name="mr_no" value="${patient.mr_no}"/>
							<jsp:param name="patient_id" value="${patient.patient_id}"/>
						</jsp:include>
				</c:if>
			</c:forEach>
		</c:if>
		<c:if test="${fieldsetField == 'true' && s_i.last}" >
					</fieldset>
					</div>
				<script type="text/javascript">
					var mrNo = '${patient.mr_no}';
					collapsiblePanels[mrNo] = new Spry.Widget.CollapsiblePanel("CollapsiblePanel", {contentIsOpen:false});
				</script>
		</c:if>
	</c:forTokens>

	<c:forTokens items="${form.map.sections}" delims="," var="section_id">

		<c:choose>
			<c:when test="${section_id > 0 && group_patient_sections == 'N'}">
				<jsp:include page="/pages/outpatient/InstaSection.jsp">
					<jsp:param name="section_id" value="${section_id}"/>
					<jsp:param name="service_pres_id" value="${param.prescription_id}"/>
					<jsp:param name="form_name" value="serviceConductionForm"/>
					<jsp:param name="form_type" value="Form_Serv"/>
					<jsp:param name="displayExpandedSection" value="${sectionsCount <= 10 ? 'block': 'none'}"/>
					<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
					<jsp:param name="mr_no" value="${patient.mr_no}"/>
					<jsp:param name="patient_id" value="${patient.patient_id}"/>
				</jsp:include>
			</c:when>
			<c:otherwise>
				<c:forEach items="${insta_sections}" var="patient_form_group">
					<c:if test="${patient_form_group.map.section_id == section_id && patient_form_group.map.linked_to != 'patient'}">
						<jsp:include page="/pages/outpatient/InstaSection.jsp">
							<jsp:param name="section_id" value="${section_id}"/>
							<jsp:param name="service_pres_id" value="${param.prescription_id}"/>
							<jsp:param name="form_name" value="serviceConductionForm"/>
							<jsp:param name="form_type" value="Form_Serv"/>
							<jsp:param name="displayExpandedSection" value="${sectionsCount <= 10 ? 'block': 'none'}"/>
							<jsp:param name="insta_form_id" value="${form.map.form_id}"/>
							<jsp:param name="mr_no" value="${patient.mr_no}"/>
							<jsp:param name="patient_id" value="${patient.patient_id}"/>
						</jsp:include>
					</c:if>
				</c:forEach>
			</c:otherwise>
		</c:choose>
	</c:forTokens>
	<c:if test="${not empty consumables}">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="services.serviceconduction.addedit.consumablesstoredetails"/></legend>
			<table class="formtable">
				<tr>
					<td class="formlabel"><insta:ltext key="services.serviceconduction.addedit.storename"/> </td>
					<td colspan="3" class="forminfo">
						<c:choose>
							<c:when test="${serviceBean.map.conducted == 'C' || serviceBean.map.conducted == 'R'}">
								${ifn:cleanHtml(storeName)}
								<input type="hidden" name="store_id" id="store_id" value="${ifn:cleanHtmlAttribute(storeId)}"/>
							</c:when>
							<c:otherwise>
								<select name="store_id" id="store_id" class="dropdown" onchange="onStoreChange();">
								<option value=""><insta:ltext key="selectdb.dummy.value"/></option>
								<c:forEach items="${storesList}" var="st">
									<option value="${st.dept_id}" <c:if test="${storeId eq st.dept_id}"><insta:ltext key="services.serviceconduction.addedit.selected"/></c:if> >${st.dept_name}</option>
								</c:forEach>
								</select>
							</c:otherwise>
						</c:choose>
					</td>
					<td class="formlabel"></td>
					<td></td>
				</tr>
			</table>
		</fieldset>
		</c:if>
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="services.serviceconduction.addedit.serviceconsumable"/></legend>
			<table id="reagentstable" class="dataTable" style="margin-top: 10px" cellspacing="0"  cellpadding="0" >
				<tr id="reagentRow0" class="header">
					<td><insta:ltext key="services.serviceconduction.addedit.consumablename"/></td>
					<td><insta:ltext key="services.serviceconduction.addedit.qty"/></td>
					<td><insta:ltext key="services.serviceconduction.addedit.availableqty"/></td>
					<td><insta:ltext key="services.serviceconduction.addedit.unituom"/></td>
					<td><insta:ltext key="services.serviceconduction.addedit.pkgtype"/></td>
				</tr>
				<c:set var="prescribed_qty" value="${(empty serviceBean || empty serviceBean.map.quantity || serviceBean.map.quantity == 0) ? 1 : serviceBean.map.quantity}"/>
				<c:set var="checkboxVal" value="add"/>
				<c:forEach items="${consumables}" var="consumable" varStatus="status">
					<c:set value="${status.index}" var="index"/>
					<tr>
						<td>
							<label title="${consumable.map.medicine_name}">${consumable.map.medicine_name}</label>
						</td>
						<td>
							<c:choose>
								<c:when test="${not empty usageconsumables && fn:length(usageconsumables) > 0}">
									<input class="number" type="text" name="qty" id="qty${ifn:cleanHtmlAttribute(index)}"
										value="${ifn:afmt(consumable.map.qty)}" ${reopenedState}/>
								</c:when>
								<c:otherwise>
									<input class="number" type="text" name="qty" id="qty${ifn:cleanHtmlAttribute(index)}"
										value="${ifn:afmt(consumable.map.qty * prescribed_qty)}" ${reopenedState}/>
								</c:otherwise>
							</c:choose>
							<c:if test="${not empty serviceconsumables && fn:length(serviceconsumables) > 0}">
								<c:forEach items="${serviceconsumables}" var="sc">
									<c:if test="${sc.map.medicine_name == consumable.map.medicine_name}">
										(<insta:ltext key="services.serviceconduction.list.qtyperservice"/> : ${ifn:afmt(sc.map.qty)})
									</c:if>
								</c:forEach>
							</c:if>
							<input type="hidden" name="consumable_id" id="consumable_id${ifn:cleanHtmlAttribute(index)}" value="${consumable.map.consumable_id}"/>
							<c:set var="checkboxVal" value="${consumable.map.usage_no != 0 ? updateText : addText}"/>
							<input type="hidden" name="ref_no" id="ref_no" value="${consumable.map.usage_no}"/>
							<input type="hidden" name="reagent_usage_seq" id="reagent_usage_seq" value="${consumable.map.usage_no}"/>
							<input type="hidden" name="existentItemInStore" id="existentItemInStore${ifn:cleanHtmlAttribute(index)}" value="N"/>
						</td>
						<td style="text-align:center;">
							<label></label>
						</td>
						<td style="text-align:center;">
							<label></label>
						</td>
						<td style="text-align:center;">
							<label></label>
						</td>
					</tr>
				</c:forEach>
				<c:if test="${not empty consumables}">
					<tr>
						<td colspan="5">${checkboxVal}<insta:ltext
								key="services.serviceconduction.list.consumables" /> <input
							type="checkbox" value="${checkboxVal}" name="updateConsumables"
							${checkboxVal == 'Update' ? 'checked' : ''}
							${serviceBean.map.conducted == 'C' || serviceBean.map.conducted == 'R'? ' disabled' : ''} />
					</tr>
				</c:if>
			</table>
			<insta:noresults hasResults="${not empty consumables}" message="${msg}"/>
		</fieldset>
		<table style="margin-top: 5px">
			<tr>
				<td style="width: 63px">Finalize All:</td>
				<td style="width: 30px"><input id="finalizeAll" type="checkbox"
					value="true" onClick="finalizeAllInstaSections();"
					${serviceBean.map.conducted == 'C' || serviceBean.map.conducted == 'R'? ' disabled' : ''} /></td>
				<td></td>
			</tr>
		</table>
		<div class="screenActions">
			<c:url var="pendingServices" value="Services.do">
				<c:param name="_method" value="pendingList"/>
				<c:param name="sortReverse" value="false"/>
				<c:param name="date_range" value="week"/>
			</c:url>
			<button type="button" name="save" id="save" accessKey="S" onclick="return ValidatesaveAndAddDocument(false);" ${serviceBean.map.conducted == 'C' ? 'disabled' : ''}>
				<b><u><insta:ltext key="services.serviceconduction.addedit.s"/></u></b><insta:ltext key="services.serviceconduction.addedit.ave"/></button>

			<c:set var="btnLabel" value="${serviceBean.map.conducted == 'C' ? '<b><u>a</u></b>dd document' : 'Save & <b><u>A</u></b>dd document'}" />
			<button type="button" name="saveAndAddDocument" id="saveAndAddDocument" accessKey="A" onclick="return ValidatesaveAndAddDocument(true)">
				${btnLabel}</button>
			<c:set var="btnLabel" value="${serviceBean.map.conducted == 'C' ? '<b><u>p</u></b>rint' : 'Save & <b><u>P</u></b>rint'}" />
			<button type="button" name="saveAndPrint" id="saveAndPrint" accessKey="P" onclick="return ValidateSaveAndPrint(true)">
				${btnLabel}
			</button>
			| <a href="${pendingServices}" title='<insta:ltext key="services.serviceconduction.addedit.pendingserviceslist"/>'><insta:ltext key="services.serviceconduction.addedit.pendingserviceslist"/></a>
			<insta:screenlink screenId="serv_audit_log" label="Insta Sections Audit Log" addPipe="true" target="_blank"
				extraParam="?_method=getAuditLogDetails&al_table=serv_audit_view&mr_no=${patient.mr_no}&patient_id=${patient.patient_id}&section_item_id=${param.prescription_id}"/>
		</div>
		<jsp:include page="/pages/outpatient/InstaSectionDialogsInclude.jsp"/>
	</form>
</body>
</html>
