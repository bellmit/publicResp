<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>

<html>

<head>
	<title>Surgery / Procedure Add to Bill Screen - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="js" file="OTServices/OperationDetails/OperationDetails.js"/>
		<style type="text/css">
		.deletedRow{
			background-color:#EAEAEA; cursor:pointer;
			border-bottom:1px #666 solid;  border-right:1px #999 solid;
			padding:5px 10px 4px 10px;  color:#707070;
		}
	</style>
	<script>
		var isModAdvanceIns = ${mod_adv_ins};
		var fixedOtCharges = '${genPrefs.fixedOtCharges}';
	</script>

</head>
<body>

<h1>Surgery / Procedure Add to Bill Screen</h1>
	 <insta:feedback-panel/>
	 <insta:patientdetails visitid="${visit_id}" showClinicalInfo="true"/>

<form name="addToBillForm" method="POST" action="${cpath}/otservices/OtManagement/Operation.do">
<input type="hidden" name="_method" id="_method" value="saveOperationBillableResources"/>
<input type="hidden" name="operation_details_id" id="operation_details_id" value="${ifn:cleanHtmlAttribute(operation_details_id)}"/>
<input type="hidden" name="visit_id" id="visit_id" value="${ifn:cleanHtmlAttribute(visit_id)}"/>

<div class="resultList">
	<table class="resultList" width="100%" id="dashboardTable" style="empty-cells: show">
		<tr>
			<th>Surgery / Procedure</th>
			<th>Resource Name</th>
			<th>Billable</th>
			<c:if test="${mod_adv_ins}">
				<th>Prior Auth No</th>
				<th>Prior Auth Mode</th>
			</c:if>
		</tr>
			<c:set var="rowIndex" value="0"/>
			<c:forEach var="ope" items="${operations}" varStatus="st">
				<c:set var="billableChecked" value="false"/>
				<tr class="${st.first ? 'firstRow' : ''}">
					<td>
						<insta:truncLabel value="${ope.operation_name}" length="30"/> ${ope.oper_priority == 'P' ? '(Primary)' : '(Secondary)'}
						<input type="hidden" name="opeProcId"  value="${ope.operation_proc_id}"/>
					</td>
					<td colspan="2">&nbsp;</td>
					<c:if test="${mod_adv_ins && ope.prior_auth_required != 'N'}">
						<td>
							<input type="text" name="prior_auth_id" id="prior_auth_id${rowIndex}" maxlength="50" value="${ope.prior_auth_id}"/>
							<input type="hidden" name="prior_auth_required" id="prior_auth_required${rowIndex}" value="${ope.prior_auth_required}"/>
						</td>
						<td>
							<insta:selectdb  name="prior_auth_mode_id" id="prior_auth_mode_id${rowIndex}" table="prior_auth_modes"
								valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name"
									filtered="false" dummyvalue="-- Select --" value="${ope.prior_auth_mode_id}"/>
						</td>
					</c:if>
					<c:if test="${mod_adv_ins && ope.prior_auth_required == 'N'}">
						<td colspan="2">&nbsp;
						<input type="hidden" name="prior_auth_id" id="prior_auth_id${rowIndex}" value=""/>
						<input type="hidden" name="prior_auth_mode_id" id="prior_auth_mode_id${rowIndex}" value=""/>
						<input type="hidden" name="prior_auth_required" id="prior_auth_required${rowIndex}"  value=""/>
						</td>
					</c:if>
				</tr>
			<c:forEach var="billItem" items="${billItems}">
				<c:if test="${ope.operation_proc_id == billItem.operation_proc_id}">
					<tr>
						<td class="indent">&nbsp;</td>
						<td>
							<c:choose>
								<c:when test="${billItem.operation_speciality == 'SU'}">
									<insta:truncLabel value="${billItem.resource_name}" length="100"/> (Primary Surgeon/Doctor)
								</c:when>
								<c:when test="${billItem.operation_speciality == 'ASU'}">
									<insta:truncLabel value="${billItem.resource_name}" length="100"/> (Secondary Surgeon/Doctor)
								</c:when>
								<c:when test="${billItem.operation_speciality == 'AN'}">
									<insta:truncLabel value="${billItem.resource_name}" length="100"/> (Primary Anaesthetist)
								</c:when>
								<c:when test="${billItem.operation_speciality == 'ASAN'}">
									<insta:truncLabel value="${billItem.resource_name}" length="100"/> (Secondary Anaesthetist)
								</c:when>
								<c:when test="${billItem.operation_speciality == 'COSOPE'}">
									<insta:truncLabel value="${billItem.resource_name}" length="100"/> (Co-op. Surgeon/Doctor)
								</c:when>
								<c:when test="${billItem.operation_speciality == 'PAED'}">
									<insta:truncLabel value="${billItem.resource_name}" length="100"/> (Doctor)
								</c:when>
								<c:otherwise>
									<insta:truncLabel value="${billItem.resource_name}" length="100"/>
								</c:otherwise>
							</c:choose>
						</td>
						<c:if test="${ope.oper_priority == 'P'}">
							<c:choose>
								<c:when test="${!billableChecked && genPrefs.fixedOtCharges == 'N' && billItem.resource_type == 'THEAT'}">
									<c:set var="billableChecked" value="true"/>
								</c:when>
								<c:when test="${!billableChecked && genPrefs.fixedOtCharges == 'Y' && billItem.resource_type == 'TEAM' &&
									billItem.operation_speciality == 'SU'}">
									<c:set var="billableChecked" value="true"/>
								</c:when>
								<c:otherwise>
									<c:set var="billableChecked" value="false"/>
								</c:otherwise>
							</c:choose>
						</c:if>
						<c:if test="${ope.oper_priority == 'S'}">
							<c:choose>
								<c:when test="${billItem.resource_type == 'THEAT' }">
									<c:set var="billableChecked" value="true"/>
								</c:when>
								<c:otherwise>
									<c:set var="billableChecked" value="false"/>
								</c:otherwise>
							</c:choose>
						</c:if>
						<td>
							<input type="checkbox" name="billable" id="billable"
								value="${billItem.operation_proc_id}-${billItem.resource_id}-${billItem.resource_type}"
								${(billableChecked || billItem.billable == 'Y') ?'checked' :'' }/>
							 <%--<c:if test="${billableChecked}">
								<input type="hidden" name="billable" id="billable"
									value="${billItem.operation_proc_id}-${billItem.resource_id}-${billItem.resource_type}"/>
							</c:if> --%>
						</td>
						<c:if test="${mod_adv_ins}">
							<td colspan="2">&nbsp;</td>
						</c:if>
					</tr>
				</c:if>
			</c:forEach>
			<c:if test="${mod_adv_ins}">
				<c:set var="rowIndex" value="${rowIndex + 1}"/>
			</c:if>

			</c:forEach>
	</table>
</div>
<table class="formtable">
	<tr>
		<td class="formlabel">Order Remarks:</td>
		<td colspan="3">
			<textarea name="order_remarks" id="order_remarks" cols="80" rows="4"><c:out value="${operationDetails.map.order_remarks}"/></textarea>
		</td>
	</tr>
</table>

<div class="screenActions">
	<c:set var="disabledAddTObill" value="${(operationDetails.map.operation_status != 'C' || operationDetails.map.added_to_bill eq 'Y') ? 'disabled=' : ''}"/>
	<c:set var="disabledSave" value="${(operationDetails.map.operation_status == 'X' || operationDetails.map.added_to_bill eq 'Y') ? 'disabled=' : ''}"/>
	<button type="submit" name="save" id="save" accesskey="S" ${disabledSave} onclick="return validateSave();"><b><u>S</u></b>ave</button>
	<button type="submit" name="addToBill" id="addtoBill" accesskey="A" ${disabledAddTObill} onclick="return addResourcesToBill();"><b><u>A</u></b>dd To Bill</button>
	<insta:screenlink screenId="operation_detailed_screen" extraParam="?_method=getOperationDetailedScreen&visitId=${patient.patient_id}&prescribed_id=${param.prescription_id}&mr_no=${patient.mr_no}&operation_details_id=${param.operation_details_id}"
		label="Surgery/Procedure Details" addPipe="true" />
	<c:if test="${operationDetails.map.added_to_bill eq 'Y'}">
			<insta:screenlink screenId="search_bills" extraParam="?_method=getBills&mr_no=${patient.mr_no}"
				label="Patient Bills" addPipe="true" />
	</c:if>

</div>
</form>

</body>
</html>