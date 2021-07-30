<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>


<html>
	<head>
		<script type="text/javascript">
		//Bug#:	42425
			var dietPresFormName = null;
			YAHOO.util.Event.onContentReady('content', setFormName);
			function setFormName() {
				dietPresFormName = document.presForm;
			}
		</script>

		<insta:link type="css" file="widgets.css"/>
		<insta:link type="script" file="widgets.js"/>
		<insta:link type="script" file="ajax.js"/>
		<insta:link type="script" file="hmsvalidation.js"/>
		<insta:link type="script" file="ipservices/ipservices.js" />
		<insta:link type="script" file="masters/dietmaster.js"/>
		<script type="text/javascript">
			var patient_id = '${ifn:cleanJavaScript(patientid)}';
			var cpath='${cpath}';
		</script>
		<insta:js-bundle prefix="registration.patient"/>
	</head>

	<body onload="init();printprescription('${ifn:cleanJavaScript(print)}', '${showPrinter}');ajaxForPrintUrls();">
	<div class="pageHeader">Prescribe Meal</div>
	<insta:feedback-panel/>
	<insta:patientdetails visitid="${patientid}" showClinicalInfo="true"/>
	<form action="dietPrescribe.do"  id="dietPrescForm" method="POST" name="presForm">
		<input type="hidden" name="recordLength" value="0">
		<input type="hidden" name="_method" value="saveMealPrescriptions">
		<input type="hidden" name="prescribed_by" id="prescribed_by" value="">
		<input type="hidden" id="serverTime" value="<fmt:formatDate value="<%=new java.util.Date()%>" type="time" pattern="HH:mm"/>">
		<input type="hidden" name="visit_id" id="visit_id" value="${patient.patient_id}">
		<input type="hidden" id="reg_date" value='<fmt:formatDate value="${patient.reg_date}" pattern="dd-MM-yyyy"/>'>
		<table class="formtable" align="center" width="80%">
			<tr>
				<td colspan="2">
					<table>
						<tr>
							<td valign="top">
								<label><b>Dietician :</b></label>
							</td>
							<td class="yui-skin-sam" valign="top" >
								<div id="doctorname" class="autocomplete" style="width: 25em;">
									<input type="text" name="doctorName" id="doctorName" class="text-input" style="width: 10em;" tabindex="5">
          								<div id="doctornamecontainer" class="scrolForContainer"></div>
								</div>
							</td>
						</tr>
					</table>
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<table class="dataTable" id="mealPrescTab" align="center" width="100%">
						<tr>
							<th>Date</th>
							<th>Time</th>
							<th>Meal</th>
							<th>Special Ins</th>
							<th>Delivered</th>
							<th>Delivered Time</th>
							<th>Delete</th>
    					</tr>
						<c:choose>
							<c:when test="${not empty prescriptionList}">
								<c:forEach var="prescription" items="${prescriptionList}" varStatus="status">
									<c:set var="index" value="${status.index+1}"></c:set>
									<tr id="tr${index}">
											<input type="hidden" name="newAdded" id="newAdded${index}" value="N">
											<input type="hidden" name="delete" id="delete${index}" value="N">
											<input type="hidden" name="meal_name" id="meal_name${index}" value="${prescription.map.meal_name}">
											<input type="hidden" name="meal_date" id = "meal_date${index}" value='<fmt:formatDate value="${prescription.map.meal_date}" pattern="dd-MM-yyyy"/>'>
											<input type="hidden" name="meal_timing" id="meal_timing${index}" value="${prescription.map.meal_timing}">
											<input type="hidden" name="diet_pres_id" id="diet_pres_id${index}" value="${prescription.map.diet_pres_id}">
											<input type="hidden" name="meal_time" id="meal_time${index}" value="${prescription.map.meal_time}">
											<input type="hidden" name="special_instructions" id="special_instructions${index}" value="${prescription.map.special_instructions}">
										<td>
											<fmt:formatDate value="${prescription.map.meal_date}" pattern="dd-MM-yyyy"/>
										</td>
										<td>
											${prescription.map.meal_timing}
										</td>
										<td>
											${prescription.map.meal_name}
										</td>
										<td style="white-space: normal;">
											${prescription.map.special_instructions}
										</td>
										<td>
											<c:choose>
												<c:when test="${not empty prescription.map.status}">
													${prescription.map.status}
												</c:when>
												<c:otherwise>
													N
												</c:otherwise>
											</c:choose>
										</td>
										<td>
											<fmt:formatDate value="${prescription.map.status_updated_time}" pattern="dd-MM-yyyy hh:mm"/>
										</td>
										<td>
										   <img class="imgDelete" name="image" id="image${index}" src="${cpath }/icons/Delete.png" onclick="onClickDelete(${index})">
										</td>
									</tr>
								</c:forEach>

							</c:when>
							<c:otherwise>
							</c:otherwise>
						</c:choose>
					</table>
				</td>
			</tr>
			<tr>
				<td colspan="2">

					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel">Meal Prescriptions</legend>
						<table class="formtable" align="center" width="100%">
							<tr>
								<td class="formlabel">From Date :</td>
								<td valign="top">
									<insta:datewidget  name="fromdate" id="fromdate"/>
								</td>
								<td class="formlabel" colspan="">Meal Name :</td>
								<td class="yui-skin-sam" valign="top" colspan="3">
									<div id="mealname" class="autocomplete" style="width: 20em;">
										<input type="text" name="prescribeMeal" id="prescribeMeal" class="text-input" style="width: 10em;" tabindex="25">
										<input type="hidden" id="mealid" name="mealid">
										<div id="mealnamecontainer" class="scrolForContainer"></div>
									</div>
								</td>
								<td>&nbsp;</td>
								<td>&nbsp;</td>
							</tr>
							<tr>
								<td class="formlabel">To Date:</td>
								<td>
									<insta:datewidget name="todate" id="todate"/>
									<script>
										document.getElementById('fromdate').setAttribute('tabindex', 10);
										document.getElementById('todate').setAttribute("tabindex", 15);
									</script>
								</td>
								<td class="formlabel" >Special Instructions :</td>
								<td>
									<input type="text" name="prescribeSplIns" id="prescribeSplIns" maxlength="2000" tabindex="30">
								</td>
								<td style="width: 7em;"></td>
							</tr>
							<tr>
								<td  class="formlabel" >Time :</td>
								<td>
									<insta:selectoptions name="prescribeTime" id = "prescribeTime" value =""
										opvalues="0,BF,Lunch,Dinner,Spl" optexts="..Select Time..,BF,Lunch,Dinner,Spl" onchange="showTime()"  tabindex="20"/>

								</td>
							</tr>
								<td>
									<div id="splTimeDiv" style="display: none">
											<input type="text" name="splMealTime" id="splMealTime" size="4"
												value='<fmt:formatDate value="${serverNow}" type="time" pattern="HH:mm"/>' />

											<input type="hidden" name="hiddensplMealTime" id="hiddensplMealTime" size="4"
												value='<fmt:formatDate value="${serverNow}" type="time" pattern="HH:mm"/>' />
									</div>
								</td>

							<tr>
								<td >
									<button type="button" name="add" id="add" onclick="handleAddDiet();" accesskey="A" tabindex="35"/><b><u>A</u></b>dd</button>
								</td>
							</tr>
						</table>
				</fieldset>
			</td>
			</tr>
</table>
	<c:set var="formatType" value=""></c:set>
	<div class="screenActions" style="float: left">
		<button type="button" name="Save" accesskey="S" onclick="return validate()" tabindex="40"><b><u>S</u></b>ave and Print</button>
		<insta:screenlink screenId="ip_ipservices" extraParam="?_method=getIPDashBoard&sortOrder=mr_no&sortReverse=true"
					label="In Patient List" addPipe="true"/>
		<c:choose>
			<c:when test="${not empty doc_id}">
				<c:url var="editDocUrl" value="/Dietary/DietaryGenericDocuments.do">
					<c:param name="_method" value="show"/>
					<c:param name="doc_id" value="${doc_id}"/>
					<c:param name="template_id" value="${template_id}"/>
					<c:param name="format" value="${format}"/>
					<c:param name="patient_id" value="${patientid}"></c:param>
					<c:param name="mr_no" value="${patient.mr_no}"></c:param>
				</c:url>

				<c:url var="deleteDocUrl" value="/Dietary/DietaryGenericDocuments.do">
					<c:param name="_method" value="deleteDocuments"/>
					<c:param name="deleteDocument" value="${doc_id},${format}"/>
					<c:param name="patient_id" value="${patientid}"></c:param>
					<c:param name="mr_no" value="${patient.mr_no}"></c:param>
				</c:url>
				| <a href="<c:out value='${editDocUrl}' />" title="Edit the diet chart">Edit Diet Chart</a>
				| <a href="<c:out value='${deleteDocUrl}' />" title="Delete the diet chart" onclick="return confirmDelete();">Delete Diet Chart</a>
			</c:when>
			<c:otherwise>
				<c:url value="/Dietary/DietaryGenericDocuments.do" var="addUrl">
					<c:param name="_method" value="add"/>
					<c:param name="patient_id" value="${patient.patient_id}"/>
					<c:param name="mr_no" value="${patient.mr_no}"></c:param>
				</c:url>
				| <a id="addTemplateUrl" href="" onmousedown="" tabindex="45" onclick="return addTemplate(event,'${ifn:cleanJavaScript(addUrl)}')">Add Diet Chart</a>
			</c:otherwise>
		</c:choose>
	</div>
	<div style="float: right; display: ${empty doc_id ? 'block' : 'none'};" >
		<select name="dietFormat" id="dietFormat" class="dropdown" tabindex="50">
			<option value="">-----select format----</option>
			<c:forEach var="dietaryTemplate" items="${dietaryTempletes}">
				<option value="${dietaryTemplate.map.format}+${dietaryTemplate.map.template_id}" >${dietaryTemplate.map.template_name}</option>
			</c:forEach>
		</select>
	</div>
</form>
<script type="text/javascript">
	var mealNameAndCharges = ${mealNameAndCharges};
	var doctorList = ${doctorList};
	var opencreditbills = <%= request.getAttribute("opencreditbills") %>;
	var screentype = '${screentype}';
</script>
	</body>

</html>
