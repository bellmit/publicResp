<%@page import="com.insta.hms.vitalForm.VisitVitalsDAO"%>
<%@page import="com.insta.hms.vitalparameter.VitalMasterDAO"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<%
DoctorConsultImagesDAO consultImageDao = new DoctorConsultImagesDAO();
DoctorConsultationDAO consultDao = new DoctorConsultationDAO();
VitalMasterDAO vmDAO = new VitalMasterDAO();
VisitVitalsDAO vvDAO = new VisitVitalsDAO();

String consIdStr = request.getParameter("consultation_id");
int consId = Integer.parseInt(consIdStr);
BasicDynaBean consultBean = consultDao.findConsultationExt(consId);
String visitType = (String) consultBean.get("visit_type");
UserDAO userdao = new UserDAO();
String userName = (String) request.getSession(false).getAttribute("userid");
String noteTakerPreferences = userdao.getPrescriptionNoteTaker(userName);
Map modulesActivatedMap = ((Preferences) request.getSession(false).getAttribute("preferences")).getModulesActivatedMap();
String mod_pharmacy = (String) (modulesActivatedMap.get("mod_pharmacy"));
mod_pharmacy = mod_pharmacy == null ? "" : mod_pharmacy;

int prevConsultationId = DoctorConsultationDAO.getPreviousConsultationId(
		(String) consultBean.get("mr_no"), consId, (String) consultBean.get("doctor_name"),
		visitType);
request.setAttribute("previousConsultationId", prevConsultationId);

List imageColumn = new ArrayList();
imageColumn.add("consultation_id");
imageColumn.add("content_type");
imageColumn.add("datetime");
imageColumn.add("image_id");

if (prevConsultationId > 0) {
	BasicDynaBean prvsConsultBean = consultDao.findConsultationExt(prevConsultationId);
	if (prvsConsultBean != null) {
		String prevPatientId = (String) prvsConsultBean.get("patient_id");

		request.setAttribute("prevConsult", prvsConsultBean.getMap());
		request.setAttribute("prev_diagnosis_details", MRDDiagnosisDAO.getPrimarySecondaryDiagnosis(prevPatientId));
		request.setAttribute("prev_all_fields", vmDAO.getActiveVitalParams("O"));
		request.setAttribute("prev_vital_readings", vvDAO.getVitals(prevPatientId,
				null, null, "V"));
		//request.setAttribute("prevGenericVitalFormDetails", genericVitalFormDAO.getVitalReadings((String) prvsConsultBean.get("patient_id")));
		request.setAttribute("prevPrescriptions", PrescriptionsMasterDAO.getAllPrescriptions(prevConsultationId, prevPatientId, mod_pharmacy, (Map) request.getAttribute("patient")));

        if (noteTakerPreferences.equalsIgnoreCase("Y")) {
        	request.setAttribute("previousConsultationImages", consultImageDao.listAll(imageColumn,
        			"consultation_id", prevConsultationId));
        } else {
        	request.setAttribute("previousConsultFieldValues",
    					ConsultationFieldValuesDAO.getConsultationFieldsValues(prevConsultationId, true, false));
        }
	}

}
int doctorTemplateId = (Integer) consultBean.get("doctor_template_id");
List list = DoctorConsultationDAO.getConsultationTemplate(doctorTemplateId);
request.setAttribute("consultFields", list);

%>
<script>
	var previousConsultationId = parseInt(${previousConsultationId});
</script>
<%@page import="com.insta.hms.outpatient.DoctorConsultationDAO"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="org.apache.commons.beanutils.BasicDynaBean"%>
<%@page import="com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO"%>
<%@page import="com.insta.hms.master.PrescriptionsMaster.PrescriptionsMasterDAO"%>
<%@page import="com.insta.hms.outpatient.ConsultationFieldValuesDAO"%>
<%@page import="com.insta.hms.usermanager.UserDAO"%>
<%@page import="com.insta.hms.vitalForm.genericVitalFormDAO"%>
<%@page import="com.insta.hms.outpatient.DoctorConsultImagesDAO"%>
<%@page import="com.bob.hms.common.Preferences"%>
<%@page import="java.util.Map"%>
<div id="previousVisitSummaryDiv" style="display: none">
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Previous Visit Summary</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Date:</td>
					<td class="forminfo"><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${prevConsult.visited_date}"/></td>
					<td class="formlabel">Visit ID:</td>
					<td class="forminfo">${prevConsult.patient_id}</td>
				</tr>
			</table>
			<c:if test="${not empty prev_diagnosis_details}">
				<table class="formtable">
					<c:forEach items="${prev_diagnosis_details}" var="diagnosis" varStatus="st">
						<tr>
							<td class="formlabel">${diagnosis.map.diag_type == 'P' ? 'Principal' : (diagnosis.map.diag_type == 'V' ? 'Reason For Visit' : 'Secondary')} Diagnosis: </td>
							<td class="forminfo">${diagnosis.map.description}</td>
							<td class="formlabel">Code: </td>
							<td class="forminfo">${diagnosis.map.icd_code}</td>
						</tr>
					</c:forEach>
				</table>
			</c:if>
			<c:if test="${ visitType == 'o' && not empty prev_vital_readings}">
				<table class="detailList">
					<tr>
						<th>Date</th>
						<th>Time</th>
						<c:forEach var="columnBean" items="${prev_all_fields}">
							<th>${columnBean.map.param_label}<c:if test="${not empty columnBean.map.param_uom}"> (${columnBean.map.param_uom})</c:if>
							</th>
						</c:forEach>
					</tr>
					<c:set var="vitalReadingId" value="0"/>
					<c:forEach var="reading" items="${prev_vital_readings}">
						<c:set var="index" value="${i-1}"/>
						<fmt:formatDate pattern="dd-MM-yyyy" value="${reading.dateTime}" var="readingDate"/>
						<fmt:formatDate pattern="HH:mm" value="${reading.dateTime}" var="readingTime"/>
						<tr >
							<td>
								<label>${readingDate}</label>
							</td>
							<td>${readingTime}</td>
							<c:forEach var="columnBean" items="${all_fields}">
								<td>
									<c:set var="paramValue" value=""/>
									<c:set var="prefColorCode" value=""/>
									<c:forEach var="values" items="${reading.readings}">
										<c:if test="${columnBean.map.param_id == values.paramId}">
											<c:set var="paramValue" value="${values.paramValue}"/>
											<c:set var="prefColorCode" value="${values.colorCode}"/>
											<c:set var="isColor" value="${paramType eq 'V' and (not empty prefColorCode ? (prefColorCode ne prefColorCodes.map.normal_color_code) : false)}"/>
											<label style="color: ${not empty prefColorCode ? (prefColorCode eq prefColorCodes.map.normal_color_code ? 'grey' : prefColorCode) : 'grey'}; font-weight: ${isColor ? 'bold' : ''}">${values.paramValue}</label>
										</c:if>
									</c:forEach>
								</td>
							</c:forEach>
							
						</tr>
					</c:forEach>
				</table>
			</c:if>
			<c:choose>
				<c:when test="${prescriptionNoteTakerPreferences == 'Y'}">
					<fieldset class="feildSetBorder">
						<legend class="fieldSetLabel">Consultation Images</legend>
						<table class="dashboard">
							<tr>
							 	<th>Date</th>
							   	<th>Action</th>
							</tr>
							<c:forEach var="image" items="${previousConsultationImages}">
								<tr name="dialog_rowName" id="row${image.map.image_id}">
							   	 	<td ><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${image.map.datetime}"/></td>
							   	 	<td>
							   	 		<a name="dialog_viewImage" href="#" id="dialog_viewImage${image.map.image_id}" style="display: block;" onclick="displayNote('${image.map.image_id}', 'dialog_')">View</a>
							   	 		<label name="dialog_withoutView" id="withoutView${image.map.image_id}" style="display: none;">View</label>
							   	 	</td>
							   	 </tr>
							</c:forEach>
							<tr>
								<td style="display: none;" id="dialog_imageTd" colspan="2"></td>
							</tr>

						</table>
					</fieldset>
				</c:when>
				<c:otherwise>
					<c:set var="prevConsultfieldsMap" value="${empty previousConsultFieldValues ? consultFields : previousConsultFieldValues}"/>
					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel">Consultation Notes</legend>
						<table class="formtable" >
							<c:forEach items="${prevConsultfieldsMap}" var="field" varStatus="st">
								<tr>
									<td class="formlabel" style="width: 20%">${field.map.field_name}:</td>
									<td class="forminfo" style="width: 80%">${field.map.field_value}</td>
								</tr>
							</c:forEach>
						</table>
					</fieldset>
				</c:otherwise>
			</c:choose>
			<table id="previousVisitDetails">
				<c:if test="${not empty prevPrescriptions}">
					<tr>
						<td>
							<table width="100%" class="dataTable" style="margin-top: 4px;">
								<tr>
									<th>Type</th>
									<th>Name</th>
									<th>Details</th>
									<th>Qty</th>
									<th>Remarks</th>
								</tr>
								<c:forEach items="${prevPrescriptions}" var="prescription">
									<tr>
										<td>${prescription.map.item_type}</td>
										<td style="white-space: normal"><c:choose>
												<c:when test="${not empty prescription.map.item_name}">
													<insta:truncLabel value="${prescription.map.item_name}" length="20"/>
												</c:when>
												<c:otherwise>
													<insta:truncLabel value="${prescription.map.generic_name}" length="20"/>
												</c:otherwise>
											</c:choose>
										</td>
										<td>
											<c:if test="${prescription.map.item_type == 'Medicine'}">
												<insta:truncLabel value="${prescription.map.medicine_dosage} / ${prescription.map.duration} ${prescription.map.duration_units}" length="10"/>
											</c:if>
										</td>
										<td>
											<c:choose>
												<c:when test="${prescription.map.item_type == 'Medicine' || prescription.map.item_type == 'NonHospital'}">
													${prescription.map.medicine_quantity}
												</c:when>
												<c:when test="${prescription.map.item_type == 'Service'}">
													${prescription.map.service_qty}
												</c:when>
											</c:choose>
										</td>
										<td>
											<insta:truncLabel value="${prescription.map.item_remarks}" length="50"/>
										</td>
									</tr>
								</c:forEach>
							</table>
						</td>
					</tr>
				</c:if>
			</table>

			<table style="margin-top: 10px;">
				<tr>
					<td><input type="button" name="pvdCloseBtn" id="pvdCloseBtn" value="Close"/></td>
				</tr>
			</table>
		</fieldset>
	</div>
</div>
