<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="diagnosis_order_id" value="<%=SystemGeneratedSections.DiagnosisDetails.getSectionId() %>"/>
<c:set var="vitals_order_id" value="<%= SystemGeneratedSections.Vitals.getSectionId()%>"/>

<%@page import="com.insta.hms.master.outpatient.SystemGeneratedSections"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Edit Form - Insta HMS</title>
	<script>
		var avlbl_options = 'avlbl_sections';
		var selected_options = 'selected_sections';
		var selSection = ${ifn:convertListToJson(selectedSectionsJSON)};
		var deptJSON = ${ifn:convertListToJson(depts)};
		var opsJSON = ${ifn:convertListToJson(ops)};
		var serJSON = ${ifn:convertListToJson(serv)};
		var doctorsJSON = ${ifn:convertListToJson(doctors)};

		function init() {
			var patientSections = document.getElementById('group_patient_sections');
			var selectedSections = document.getElementById('selected_sections');
			if (!empty(patientSections)) {
				if (document.getElementById('form_type').value == 'Form_OT') {
					patientSections.value = 'Y';
					patientSections.checked = true;
				} else {
					if(patientSections.value == 'Y') {
						patientSections.checked = true;
						groupPatientSections(selectedSections);
					} else {
					 	patientSections.checked = false;
					}
				}
			}
		}

		function onchangeDepartment() {
			var formType = document.getElementById('form_type').value;
			if (formType == 'Form_OT') {
				var deptId = document.getElementById('dept_id').value;
				var opersDropDown = document.getElementById('operation_id');
				if (deptId == '') {
					alert("Please select the department");
					document.getElementById('dept_id').focus();
					loadSelectBox(opersDropDown, opsJSON, "operation_name", "op_id", "All", "-1");
					return false;
				}
				var list = filterList(opsJSON, 'dept_id', deptId);
				loadSelectBox(opersDropDown, list, "operation_name", "op_id", "All", "-1");
				
			} else if (formType == 'Form_Serv') {
				var servDropDown = document.getElementById('service_id');
				var serDeptId = document.getElementById('dept_id').value;
				if(serDeptId == '') {
					alert("Please select the department");
					document.getElementById('dept_id').focus();
					loadSelectBox(servDropDown, serJSON, "service_name", "service_id", "All", "-1");
					return false;
				}
				var list = filterList(serJSON, 'serv_dept_id', serDeptId);
				loadSelectBox(servDropDown, list, "service_name", "service_id", "All", "-1");
				
			} else if (formType == 'Form_CONS' || formType == 'Form_OP_FOLLOW_UP_CONS') {
				var doctorDropDown = document.getElementById('doctor_id');
				var docDeptId = document.getElementById('dept_id').value;
				if (docDeptId == '') {
					alert('Please select the department.');
					document.getElementById('dept_id').focus();
					loadSelectBox(doctorDropDown, doctorsJSON, "doctor_name", "doctor_id", "All", "-1");
					return false;
				}
				var list = filterList(doctorsJSON, "dept_id", docDeptId);
				loadSelectBox(doctorDropDown, list, "doctor_name", "doctor_id", "All", "-1");
			}
		}

		function saveComponents() {
			var form_name = document.getElementById('form_name').value;
			if (form_name == '') {
				alert("Please enter the form name.");
				document.getElementById('form_name').focus;
				return false;
			}
			if (form_name.length > 1000) {
				alert("Form Name should be less than 1000 chars.");
				document.getElementById('form_name').focus();
				return false;
			}
			var deptId = document.getElementById('dept_id').value;
			if (deptId == '') {
				alert("Please select the department");
				document.getElementById('dept_id').focus();
				return false;
			}
			if (document.getElementById('form_type').value == 'Form_Gen') {
				var template = document.getElementById('print_template_id').value;
				var doctype = document.getElementById('doc_type').value;
				if (template == '') {
					alert("Please select the Print Template.");
					document.getElementById('print_template_id').focus;
					return false;
				}
				if(doctype == ''){
					alert("Please select the Document Type.");
					document.getElementById('doc_type').focus;
					return false;
				}
			}
			var selectedSections = document.componentsform.selected_sections;
			var formType = document.getElementById('form_type').value;
			var isDiagnosisSelected = false;
			var vitalsSelected = false;
			for (var i=0; i<selectedSections.options.length; i++) {
				if (selectedSections.options[i].value == ${diagnosis_order_id}) {
					isDiagnosisSelected = true;
				}
			}
			if (selectedSections.options.length == 0) { // if we allow empty selected forms then NumberFormatException: For input string: "".
				alert("Minimum one section should be mandatory.");
				document.getElementById('avlbl_sections').focus();
				return false;
			}
			if ((formType == 'Form_CONS' || formType == 'Form_IP' || formType == 'Form_OP_FOLLOW_UP_CONS') && !isDiagnosisSelected ) {
				alert("Diagnosis Details (Sys) is mandatory.");
				return false;
			}

			var groPatSections = document.getElementById('group_patient_sections');
			if (!empty(groPatSections) && groPatSections.checked && formType != 'Form_OT') {
				alert('Grouping patient linked sections together at the top');
				groupPatientSections(selectedSections);
			}

			// Note: Any further changes should be above this.
			for (var i=0; i<selectedSections.options.length; i++) {
				selectedSections.options[i].selected = true;
			}
			document.componentsform.submit();
			return true;

		}

	</script>
	<insta:link type="script" file="shiftelements.js"/>
</head>
<body onload="init();">
	<h1>Edit Form</h1>
	<insta:feedback-panel/>
	<form action="update.htm" method="POST" name="componentsform">
		<input type="hidden" name="istemplate" value="${bean.istemplate}"/>
		<input type="hidden" name="id" value="${bean.id}"/>
		<input type="hidden" name="form_department_id" value="${bean.form_department_id}"/>
		<fieldset class="fieldSetBorder">
			<table class="formtable">
				<tr>
					<td class="formlabel">Form Name: </td>
					<td><input type="text" name="form_name" id="form_name" value="${bean.form_name}"/></td>
				</tr>
				<tr>
					<td class="formlabel">Department: </td>
					<td>
						<c:choose>
							<c:when test="${bean.dept_id == '-1'}">
								<b>All</b>
								<input type="hidden" name="dept_id" id="dept_id" value="${bean.dept_id}"/>
							</c:when>
							<c:when test="${param.form_type == 'Form_Serv' && bean.dept_id != -1}">
								<insta:selectdb name="dept_id"  id ="dept_id" table="services_departments" dummyvalue="-- Select --"
								valuecol="serv_dept_id"  displaycol="department"
								value="${bean.dept_id}" orderby="department" onchange="onchangeDepartment();"/>
								<input type="hidden" name="dept_id" id="dept_id"  value=""/>
							</c:when>
							<c:otherwise>
								<select name="dept_id" id="dept_id" class="dropdown" onchange="onchangeDepartment();">
									<option value="">-- Select --</option>
									<c:if test="${param.form_type == 'Form_Gen' || param.form_type eq 'Form_OP_FOLLOW_UP_CONS'}">
										<option value="-1">All</option>
									</c:if>
									<c:forEach items="${depts}" var="dept">
										<option value="${dept.dept_id}" ${bean.dept_id == dept.dept_id ? 'selected' : ''}>
											${dept.dept_name}</option>
									</c:forEach>
								</select>
							</c:otherwise>
						</c:choose>
					</td>
					<c:choose>
						<c:when test="${param.form_type == 'Form_OT'}">
							<td class="formlabel">Operation: </td>
							<td >
							<c:choose>
								<c:when test="${bean.dept_id == '-1' && bean.operation_id ==  '-1'}">
									<b>All</b>
									<input type="hidden" name="operation_id" id="operation_id" value="${bean.operation_id}"/>
								</c:when>
								<c:otherwise>
									<select name="operation_id" id="operation_id" class="dropdown" >
										<option value="-1" ${bean.operation_id ==  '-1' ? 'selected' : ''}>All</option>
										<c:forEach items="${ops}" var="op">
											<c:if test="${bean.dept_id == '-1' || bean.dept_id == op.dept_id}">
												<option value="${op.op_id}" ${bean.operation_id == op.op_id ? 'selected' : ''}>
													${op.operation_name}</option>
											</c:if>
										</c:forEach>
									</select>
								</c:otherwise>
							</c:choose>
							</td>
							<td class="formlabel">&nbsp;</td>
							<td >&nbsp;</td>
							</c:when>
						<c:when test="${param.form_type == 'Form_Serv'}">
							<td class="formlabel">Services: </td>
							<td>
							<c:choose>
								<c:when test="${bean.dept_id == '-1' && bean.service_id == '-1'}">
									<b>All</b>
									<input type="hidden" name="service_id" id="service_id" value="${bean.service_id}"/>
								</c:when>
								<c:otherwise>
									<select name="service_id" id="service_id" class="dropdown" >
										<option value="-1" ${bean.service_id ==  '-1' ? 'selected' : ''}>All</option>
										<c:forEach items="${serv}" var="ser">
											<c:if test="${bean.dept_id == '-1' || bean.dept_id == ser.serv_dept_id}">
												<option value="${ser.service_id}" ${ser.service_id == bean.service_id ? 'selected' : ''}>
													<c:out value="${ser.service_name}"/></option>
											</c:if>
										</c:forEach>
									</select>
								</c:otherwise>
							</c:choose>
							</td>
							<td class="formlabel">&nbsp;</td>
							<td >&nbsp;</td>
						</c:when>
						<c:when test="${param.form_type == 'Form_CONS' || param.form_type == 'Form_OP_FOLLOW_UP_CONS'}">
							<td class="formlabel">Doctors: </td>
							<td>
							<c:choose>
								<c:when test="${bean.dept_id == '-1' && bean.doctor_id == '-1'}">
									<b>All</b>
									<input type="hidden" name="doctor_id" id="doctor_id" value="${bean.doctor_id}"/>
								</c:when>
								<c:otherwise>
								<select name="doctor_id" id="doctor_id" class="dropdown" >
									<option value="-1" >All</option>
									<c:forEach items="${doctors}" var="doc">
										<c:if test="${bean.dept_id == '-1' || bean.dept_id == doc.dept_id}">
											<option value="${doc.doctor_id}" ${doc.doctor_id == bean.doctor_id ? 'selected' : ''}><c:out value="${doc.doctor_name}"/></option>
										</c:if>
									</c:forEach>
								</select>
								</c:otherwise>
							</c:choose>
							</td>
							<td class="formlabel">&nbsp;</td>
							<td >&nbsp;</td>
						</c:when>
						<c:otherwise>
							<td class="formlabel">&nbsp;</td>
							<td >&nbsp;<input type="hidden" name="operation_id" id="operation_id" value="-1"/></td>
							<td class="formlabel">&nbsp;</td>
							<td >&nbsp;</td>
						</c:otherwise>
					</c:choose>
				</tr>
				<c:if test="${param.form_type == 'Form_Gen'}">
					<tr>
						<td class="formlabel">Print Template:</td>
						<td>
							<select class="dropdown" name="print_template_id" id="print_template_id">
							<option value="">Select Template</option>
									<c:forEach items="${templateList}" var="printTemp">
										<option value="${printTemp.print_template_id}" ${bean.print_template_id == printTemp.print_template_id ? 'selected' : ''}>
											${printTemp.template_name}
										</option>
									</c:forEach>
							</select>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Status:</td>
						<td>
							<select class="dropdown" name="status" id="status">
								<option value="">--Select--</option>
								<option value="A" ${bean.status != 'I' ? 'selected' : ''}>Active</option>
								<option value="I" ${bean.status == 'I' ? 'selected' : ''}>Inactive</option>
							</select>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Document Type: </td>
						<td>
						<select class="dropdown" name="doc_type" id="doc_type">
							<option value="">Select DocType</option>
								<c:forEach items="${doctypelist}" var="docType">
									<option value="${docType.doc_type_id}" ${bean.doc_type == docType.doc_type_id ? 'selected' : ''}>
										${docType.doc_type_name}
									</option>
								</c:forEach>
						</select>
						</td>
					</tr>
				</c:if>
				<tr>
					<td class="formlabel">Form Type: </td>
					<td>
						<c:set var="form_type" value="${bean.form_type}"/>
						<input type="hidden" name="form_type" id="form_type" value="${ifn:cleanHtmlAttribute(form_type)}"/>
						<b>
							<c:choose>
								<c:when test="${form_type == 'Form_CONS'}">OP Consultation</c:when>
								<c:when test="${form_type == 'Form_IP'}">IP Record</c:when>
								<c:when test="${form_type == 'Form_Serv'}">Service</c:when>
								<c:when test="${form_type == 'Form_OT'}">Surgery/Operation Theatre Management</c:when>
								<c:when test="${form_type == 'Form_TRI'}">Triage</c:when>
								<c:when test="${form_type == 'Form_IA'}">Initial Assessment</c:when>
								<c:when test="${form_type == 'Form_Gen'}">Generic Form</c:when>
								<c:when test="${form_type == 'Form_OP_FOLLOW_UP_CONS'}">OP Follow Up Consultation</c:when>
							</c:choose>
						</b>
					</td>
				</tr>
				<tr style="display: ${form_type == 'Form_OT' ? 'none' : 'table-row'}">
					<td class="formlabel">Group Patient Linked Sections: </td>
					<td>
						<input type="checkbox" id="group_patient_sections" name="group_patient_sections" value="${bean.group_patient_sections}" onclick="groupPatientSections(this.form.selected_sections)" />
					</td>
				</tr>
				<tr style="display: ${form_type == 'Form_TRI' && preferences.modulesActivatedMap['mod_newcons'] != 'Y' ? 'table-row' : 'none'}">
					<td class="formlabel">Immunization: </td>
					<td><input type="checkbox" name="immunization" value="Y" ${bean.immunization == 'Y' ? 'checked' : ''}></td>
				</tr>
			</table>
			<table align="center" width="342" style="padding-right:5; padding-left:10px;border-width:0px; margin:0px;">
				<tr>
					<td align="center" style="padding-right: 4pt; border-width:0px; margin:0px; width:134px;">
						Available Sections
						<br />
						<select name="avlbl_sections" id="avlbl_sections" style="width:15em;padding-left:5; color:#666666;"
							multiple="true" size="15" onDblClick="moveSelectedOptions(this,this.form.selected_sections);">
							<c:forEach items="${availableSections}" var="psection">
								<c:set var="sectionNotSelected" value="true"/>
								<c:forEach items="${selectedSections}" var="ssection">
									<c:if test="${psection.section_id == ssection.section_id}">
										<c:set var="sectionNotSelected" value="false"/>
									</c:if>
								</c:forEach>
								<c:if test="${sectionNotSelected}">
									<option value="${psection.section_id}" title="${psection.section_title}">${psection.section_title}</option>
								</c:if>
							</c:forEach>
						</select>
					</td>
					<td valign="top" align="left" style="padding-right:0;">
						<br />
						<br />
						<input type="button" name="addLstFldsButton" value=">" onclick="addListFields();"/>

					</td>
					<td valign="top" align="center" style="width:134px;padding-left:4pt;">
						Selected Sections
						<br />
						<select  size="15" style="width:15em;padding-left:5; color:#666666;" multiple id="selected_sections" name="selected_sections" onDblClick="moveSelectedOptions(this,this.form.avlbl_sections);">
							<c:forEach items="${selectedSections}" var="psection">
								<option value="${psection.section_id}" title="${psection.section_title}">${psection.section_title}</option>
							</c:forEach>
						</select>
					</td>
					<td>
						<div align="center">
							<button type="button" style="border-width:thin;border-style:none; background-color:#FFFFFF;" onclick="moveOptionUp(selected_sections);"> <img src="${cpath}/icons/std_up.png" width=10 height=8/>  </button>
							<br />
							<br />
							<button type="button" style="border-width:thin;border-style:none; background-color:#FFFFFF;" onclick="moveOptionDown(selected_sections);"><img src="${cpath}/icons/std_down.png" width=10 height=8/> </button>
							<br />
							<br />
							<br /><br />
							<br /><br />
							<br /><br />
							<br /><br />
							<br/><br/>
						</div>
					</td>
				</tr>
			</table>
		</fieldset>
		<table class="screenActions">
			<tr>
				<td>
					<input type="button" name="save" value="Save" onclick="return saveComponents();">
					| <a href="list.htm">Forms List</a>
					<c:choose>
						<c:when test="${form_type == 'Form_CONS'}"><c:set var="title">OP</c:set></c:when>
						<c:when test="${form_type == 'Form_IP'}"><c:set var="title">IP</c:set></c:when>
						<c:when test="${form_type == 'Form_Serv'}"><c:set var="title">Service</c:set></c:when>
						<c:when test="${form_type == 'Form_OT'}"><c:set var="title">Surgery</c:set></c:when>
						<c:when test="${form_type == 'Form_TRI'}"><c:set var="title">Triage</c:set></c:when>
						<c:when test="${form_type == 'Form_IA'}"><c:set var="title">Initial Assessment</c:set></c:when>
						<c:when test="${form_type == 'Form_Gen'}"><c:set var="title">Generic</c:set></c:when>
						<c:when test="${form_type == 'Form_OP_FOLLOW_UP_CONS'}"><c:set var="title">OP Follow Up</c:set></c:when>
					</c:choose>
					<c:if test="${form_type == 's' && preferences.modulesActivatedMap['mod_advanced_ot'] eq 'Y'}">
						| <a href="add.htm?form_type=${form_type}" title="Add New ${title} Form">Add Surgery Form</a>
					</c:if>
					| <a href="add.htm?form_type=${form_type}" title="Add New ${title} Form">Add ${title} Form</a>
					<c:if test="${preferences.modulesActivatedMap['mod_newcons'] eq 'Y' && max_centers > 1 && (bean.form_type == 'Form_CONS' || bean.form_type == 'Form_OP_FOLLOW_UP_CONS')}">
						| <a href="showCenter.htm?id=${bean.id}&form_type=${bean.form_type}">Center Applicability</a>
					</c:if>
				</td>
			</tr>
		</table>
	</form>
</body>
</html>