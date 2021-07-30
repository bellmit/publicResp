<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Modify Surgery/Procedure Consumables - Insta HMS</title>
<insta:link type="css" file="hmsNew.css"/>
<insta:link type="js" file="hmsvalidation.js"/>
<script type="text/javascript">
</script>
</head>
<body>
	<h1>Modify Surgery/Procedure Consumables</h1>
	<div  class="error">${ifn:cleanHtml(error)}</div>
	<insta:patientdetails visitid="${param.patient_id}" showClinicalInfo="true"/>
	<form name="modifyotconsumablesform" method="POST" action="OTConsumables.do">
	<input type="hidden" name="_method" value="modifyOtConsumables">
	 	<div style="height:10px;">&nbsp;</div>
		<table width="100%">
			<tr>
				<td>Surgery/Procedure Name:
				<input type="hidden" name="operation_id" id="operation_id" value="${ifn:cleanHtmlAttribute(param.operation_id)}"/>
				<input type="hidden" name="operation_name" id="operation_name" value="${ifn:cleanHtmlAttribute(param.operation_name)}"/>
				<input type="hidden" name="operation_id" id="operation_id" value="${ifn:cleanHtmlAttribute(param.operation_id)}"/>
				<input type="hidden" name="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}">
				<input type="hidden" name="prescribed_id" value="${ifn:cleanHtmlAttribute(param.prescribedId)}">
				<input type="hidden" name="operation_type" value="${ifn:cleanHtmlAttribute(param.operation_type)}">
				<c:if test="${preferences.modulesActivatedMap['mod_consumables_flow'] == 'Y'}">
					<input type="hidden" name="operation_details_id" value="${ifn:cleanHtmlAttribute(param.operation_details_id)}">
				</c:if>
				<label><b>${ifn:cleanHtml(param.operation_name)}</b></label>
				</td>
			</tr>

		<tr>
			<td>
				<table id="reagentstable" class="dataTable" width="40%">
					<tr id="reagentRow0">
						<th>Consumable Name</th>
						<th>Qty</th>
					</tr>
					<c:choose>
						<c:when test="${consumables[0].map.status}">
							<c:forEach items="${consumables }" var="otconsumable"  varStatus="loop">
								<tr>
									<td>${otconsumable.map.item_name }
										<input type="hidden" name="item_id" id="item_id${loop.index }" value="${otconsumable.map.item_id}"/>
									</td>
									<td>
										<input type="text" name="qty" id="qty" class="number" value="${otconsumable.map.qty }" size="3" onkeypress="return enterNumAndDot(event);"/>
										<input type="hidden" name="ref_no" id="ref_no${loop.index}" value="${otconsumable.map.ref_no }"/>
										<input type="hidden" name="reagent_usage_seq" id="reagent_usage_seq${loop.index}" value="${otconsumable.map.usage_no}"/>
										<c:if test="${otconsumable.map.usage_no != '' }">
											<input type="hidden" name="old_qty" id="old_qty${loop.index}" value="${otconsumable.map.qty }"/>
										</c:if>
										<c:if test="${otconsumable.map.usage_no == '' }">
											<input type="hidden" name="old_qty" id="old_qty${loop.index}" value="0"/>
										</c:if>
									</td>
								</tr>
							</c:forEach>
						</c:when>
						<c:otherwise>
							<tr><td>No Active Consumables</td></tr>
						</c:otherwise>
					</c:choose>
				</table>
			</td>
		</tr>
		<tr>
			<td colspan="2">
				<input type="submit" value="Save"/>
				<insta:screenlink screenId="conduct_operation"
					extraParam="?_method=getOperationsConductionScreen&prescription_id=${param.prescribedId}&visitId=${param.patient_id}"
					label="Conduct Surgery/Procedure" addPipe="true"/>
				<insta:screenlink screenId="operation_detailed_screen" extraParam="?_method=getOperationDetailedScreen&visitId=${patient.patient_id}&prescribed_id=${param.prescribedId}&mr_no=${patient.mr_no}&operation_details_id=${param.operation_details_id}"
					label="Surgery/Procedure Details" addPipe="true" />
			</td>
		</tr>
		</table>
	</form>
</body>
</html>