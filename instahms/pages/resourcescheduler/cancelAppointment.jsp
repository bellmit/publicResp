<%@ page language="java" contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
	<head>		
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="i18nSupport" content="true"/>
		<c:set var="cpath" value="${pageContext.request.contextPath}" />
		<insta:js-bundle prefix="resourcescheduler.cancelappointments"/>
		<insta:js-bundle prefix="scheduler.doctorscheduler" />
		<title><insta:ltext key="patient.resourcescheduler.cancelappointments.title"/></title>
		
		<script>
			function setCancelStatus(obj, index) {
				if (obj.checked)
					document.getElementById('cancel'+index).value = 'Y';
				else
					document.getElementById('cancel'+index).value = 'N';
			}
			
			function validate() {				
				document.getElementById('cancel_reason').value = trim(document.getElementById('cancel_reason').value);
				if (empty(document.getElementById('cancel_reason').value)) {
					showMessage("js.scheduler.doctorscheduler.validreason.cancelappointment");
					document.getElementById('cancel_reason').focus();
					return false;
				}
				document.cancelAppointment.submit();				
			}
		</script>
	</head>
	
	<body>		
		<h1><insta:ltext key="patient.resourcescheduler.cancelappointments.title"/></h1>
		<insta:feedback-panel/>
		<insta:patientgeneraldetails mrno="${param.mrno}" addExtraFields="true" showClinicalInfo="true"/>		
		<form name="cancelAppointment" action="${cpath}/pages/cancel/docappointmentslist.do?method=cancelChannelingAppointment" method="post">
			<input type="hidden" name="_method" value="cancelChannelingAppointment" />
			<input type="hidden" name="appointmentId" value="${ifn:cleanHtmlAttribute(param.appointmentId)}"/>
			<input type="hidden" name="mrno" value="${ifn:cleanHtmlAttribute(param.mrno)}"/>
			<input type="hidden" name="doctor_id" value="${ifn:cleanHtmlAttribute(param.doctor_id)}"/>
			<input type="hidden" name="doctor_name" value="${ifn:cleanHtmlAttribute(param.doctor_name)}"/>
			<input type="hidden" name="department" value="${ifn:cleanHtmlAttribute(param.department)}"/>
			<fieldset class="fieldSetBorder" >
			<legend class="fieldSetLabel" ><insta:ltext key="patient.resourcescheduler.schedulerresources.appointmentdetails"/></legend>
			<table class="formtable" >
				<tr>					
					 <td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerresources.cancelreason"/></td>
					 <td>
						<input type="text" name="cancel_reason" id="cancel_reason" maxlength="200" style="width:200px;" value="${schedularBean.map.cancel_reason}" />
					</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>					
				</tr>
			</table>
			</fieldset>
			<div class="resultList">
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel"><insta:ltext key="patient.resourcescheduler.cancelappointments.legend" /></legend>
						<table class="detailList" width="100%">
							<tr>
								<th>#</th>
								<th>Item</th>
								<th>Amount</th>
							</tr>							
								<c:forEach items="${channelingItems}" var="channelingItem" varStatus="idx">
									<c:set var="cancelled" value="N" />
									<c:forEach items="${cancelledItemsList}" var="cancelledItem">
										<c:if test="${cancelledItem.map.act_description_id eq channelingItem.act_description_id}">
											<c:set var="cancelled" value="Y" />
										</c:if>
									</c:forEach>
									<tr>
										<td>
											<input type="checkbox" name="cancelChkbox" id="cancelChkbox${idx.index}" ${channelingItem.payment_status eq 'U' ? 'disabled' : (cancelled eq 'Y' ? 'disabled' : '') } 
														${cancelled eq 'Y' ? 'checked' : '' } onclick="setCancelStatus(this,${idx.index});"/>
											<input type="hidden" name="cancel" id="cancel${idx.index}" value="N"/>
											<input type="hidden" name="activity_id" value="${channelingItem.activity_id}"/>
											<input type="hidden" name="activity_code" value="${channelingItem.activity_code}"/>
											<input type="hidden" name="act_description_id" value="${channelingItem.act_description_id}" />
											<input type="hidden" name="package_id" value="${channelingItem.package_id}" />
											<input type="hidden" name="charge_id" value="${channelingItem.charge_id}"/>
											
										</td>
										<td>${channelingItem.act_description}</td>
										<td>${channelingItem.amount}</td>
									</tr>
								</c:forEach>
						</table>
				</fieldset>
			</div>
			<div class="screenActions">
				<button type="button" onclick="return validate()" accesskey="C" ${not empty param.bill_number ? 'disabled' : ''}><b><u>C</u></b>ancel Channeling</button>
				|
				<c:url var="appointmentsScreenURL" value="/pages/resourcescheduler/docappointmentslist.do">
					<c:param name="method" value="getDocAppointmentsList"/>
					<c:param name="doctor_id" value="${param.doctor_id}"/>
					<c:param name="doctor_name" value="${param.doctor_name}" />
					<c:param name="department" value="${param.department}" />
				</c:url>
				<a href="<c:out value='${appointmentsScreenURL}'/>" ><insta:ltext key="patient.resourcescheduler.doctorappointments.title" /></a>
				<c:if test="${not empty param.bill_number}">
					|<insta:screenlink screenId="credit_bill_collection" extraParam="?_method=getCreditBillingCollectScreen&billNo=${param.bill_number}"
						label="Bill ${param.bill_number}"/>
				</c:if>
				 
			</div>
		</form>	
	</body>

</html>
 