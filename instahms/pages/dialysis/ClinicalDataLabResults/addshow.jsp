<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<jsp:useBean id="currentDate" class="java.util.Date"/>
<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="patient.clinicaldatalabresults.addshow.title"/></title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="js" file="masters/areamaster.js" />

<script>
	function validateForm(obj) {
		var valueDate = document.getElementById('values_as_of_date').value;
		if (empty(valueDate)) {
			showMessage("js.clinicaldata.commonvalidations.test.valuedate.required");
			document.getElementById('values_as_of_date').focus();
			return false;
		}
		if(obj.name == 'print'){
			document.getElementById('saveAndPrint').value = 'Y';
			document.getElementById("printerId").value = document.getElementById("printer").value;
		} else {
			document.getElementById('saveAndPrint').value = 'N';
		}

		var testValue = document.getElementsByName('test_value');
		var testValueDate = document.getElementsByName('value_date');
		for(var i=0;i<testValue.length;i++) {
			if(!empty(testValueDate[i].value) && !doValidateDateField(testValueDate[i]))
				return false;

			if(!empty(testValue[i].value) && empty(testValueDate[i].value)) {
				showMessage("js.clinicaldata.commonvalidations.date.required");
				testValueDate[i].focus();
				return false;
			}

			if(!empty(testValueDate[i].value) && empty(testValue[i].value)) {
				showMessage("js.clinicaldata.commonvalidations.test.value.required");
				testValue[i].focus();
				return false;
			}
		}
	}

	function isNumber(n) {
  		return !isNaN(parseFloat(n)) && isFinite(n);
	}

	function setEdited(index) {
		document.getElementById('edited'+index).value = "Y";
	}

	function getRowIndex(row) {
		return row.rowIndex - 1;
	}
	
	function getIndexedValue(name, index) {
		var obj = getIndexedFormElement(diagcenterform, name, index);
		if (obj)
			return obj.value;
		else
			return null;
	}
	
	function setIndexedValue(name, index, value) {
		var obj = getIndexedFormElement(diagcenterform, name, index);
		if (obj)
			obj.value = value;
		return obj;
	}
</script>

<insta:js-bundle prefix="clinicaldata.commonvalidations"/>
</head>
<body onload="ajaxForPrintUrls();">
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="addText"><insta:ltext key="patient.dialysis.common.add"/></c:set>
		<c:set var="editText"><insta:ltext key="patient.dialysis.common.edit"/></c:set>
<h1>${param._method == 'add' ? addText : editText} <insta:ltext key="patient.clinicaldatalabresults.addshow.h1"/></h1>
<insta:feedback-panel/>
<insta:patientgeneraldetails mrno="${param.mr_no}" addExtraFields="true" showClinicalInfo="true"/>
<form action="ClinicalDataLabResults.do" method="POST" name="ClinicalDataLabResultsForm">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}">
	<input type="hidden" name="saveAndPrint" id="saveAndPrint" value="N">
	<input type="hidden" name="printerId" id="printerId" value="">
	<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}">

	<c:if test="${param._method == 'add'}">
		<c:set var="valueDate">
			<fmt:formatDate pattern="dd-MM-yyyy" value="${currentDate}"/>
		</c:set>
	</c:if>
	<c:if test="${param._method == 'show'}">
		<c:set var="valueDate">
			<fmt:formatDate pattern="dd-MM-yyyy" value="${clinicalLabValues.map.values_as_of_date}"/>
		</c:set>
	</c:if>
	<c:set var="dueDate">
		<fmt:formatDate pattern="dd-MM-yyyy" value="${clinicalLabValues.map.next_due_date}"/>
	</c:set>

	<fieldset class="fieldsetborder">
		<br>
		<table class="formtable">
			<tr>
				<td class="formlabel" align="left"> <insta:ltext key="patient.clinicaldatalabresults.addshow.formlabel1"/></td>
				<td class="forminput">
					<insta:datewidget name="values_as_of_date" id="values_as_of_date" value="${valueDate}"/>
				</td>
				<td class="formlabel"><insta:ltext key="patient.clinicaldatalabresults.addshow.nextdue"/></td>
				<td class="forminput" ><insta:datewidget name="next_due_date" id="next_due_date" value="${dueDate}" /></td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
		</table>
		<br>
		<table class="resultList">
			<tr>
				<th style="width:70px">&nbsp;</th>
				<th><insta:ltext key="patient.clinicaldatalabresults.addshow.resultname"/></th>
				<th><insta:ltext key="patient.clinicaldatalabresults.addshow.units"/></th>
				<th><insta:ltext key="patient.clinicaldatalabresults.addshow.range"/></th>
				<th><insta:ltext key="patient.clinicaldatalabresults.addshow.value"/></th>
				<th><insta:ltext key="patient.clinicaldatalabresults.addshow.remarks"/></th>
				<th><insta:ltext key="patient.clinicaldatalabresults.addshow.valueasofdate"/></th>
			</tr>
			<c:if test="${param._method == 'add'}">
				<c:forEach var="record" items="${clinicalLabDetails}" varStatus="st">
				<c:set var="index" value="${st.index}"/>
						<tr>
							<td>${record.map.resultlabel_short}</td>
							<td>
								${record.map.resultlabel}
								<input type="hidden" name="resultlabel_id" value="${record.map.resultlabel_id}" >
							</td>
							<td>${record.map.units}</td>
							<td><insta:truncLabel value="${record.map.reference_range_txt}" length="30"/></td>
							<td>
								<c:choose>
									<c:when test="${record.map.data_allowed eq 'V'}">
										<input type="text" style="width: 60px;background-color:${backColor};" maxlength="450" name="test_value" id="test_value${ifn:cleanHtmlAttribute(index)}"
										value="" tabindex="1"/>
									</c:when>
									<c:otherwise>
										<insta:selectoptions name="test_value" style="width: 60px;background-color:${backColor};"
											id="test_value${index}"
											onkeypress="nextFieldOnEnter(${index},event);"
											opvalues="${record.map.source_if_list}" optexts="${record.map.source_if_list}" value=""
											dummyvalue="${dummyvalue}" dummyvalueId=""></insta:selectoptions>
									</c:otherwise>
								</c:choose>
							</td>
							<%-- <td>
									<select name="withinNormal" id="withinNormal" class="dropdown" style="width:8em"
											onchange="setSeviarity('${record.map.min_normal_value}','${record.map.max_normal_value}',
												'${record.map.min_critical_value}','${record.map.max_critical_value}',
												'${record.map.min_improbable_value}','${record.map.max_improbable_value}',
												this,withinNormal,'','${record.map.reference_range_txt}','')";>
										<option value="">-- Select --</option>
										<option value="Y">Normal</option>
										<option value="*" >Abnormal Low</option>
										<option value="#" >Abnormal High</option>
										<option value="**">Critical Low</option>
										<option value="##">Critical High</option>
										<option value="***">Improbable Low</option>
										<option value="###">Improbable High</option>
									</select>
								</td> --%>
							<td>
								<input type="text" name="remarks" value="">
							</td>
							<td><insta:datewidget name="value_date" id="value_date${index}" btnPos="left"/></td>
						</tr>
					</c:forEach>
				</c:if>
				<c:if test="${param._method == 'show'}">
					<c:forEach var="record" items="${clinicalLabDetails}" varStatus="st">
				<c:set var="index" value="${st.index}"/>
						<tr>
							<td>${record.map.resultlabel_short}</td>
							<td>
								${record.map.resultlabel}
								<input type="hidden" name="resultlabel_id" value="${record.map.resultlabel_id}">
							</td>
							<td>${record.map.units}</td>
							<td><insta:truncLabel value="${record.map.reference_range_txt}" length="30"/></td>
							<td>
								<c:choose>
									<c:when test="${(not empty record.map.test_value && ifn:isNumeric(record.map.test_value) && record.map.test_value > record.map.max_improbable_value)}">
										<c:set var="backColor" value="${jsonHtmlColorCodes.map.improbable_color_code}"/>
									</c:when>
									<c:when test="${(not empty record.map.test_value && ifn:isNumeric(record.map.test_value) && record.map.test_value < record.map.min_improbable_value)}">
										<c:set var="backColor" value="${jsonHtmlColorCodes.map.improbable_color_code}"/>
									</c:when>
									<c:when test="${(not empty record.map.test_value && ifn:isNumeric(record.map.test_value) && record.map.test_value > record.map.max_critical_value)}">
										<c:set var="backColor" value="${jsonHtmlColorCodes.map.critical_color_code}"/>
									</c:when>
									<c:when test="${(not empty record.map.test_value && ifn:isNumeric(record.map.test_value) && record.map.test_value < record.map.min_critical_value)}">
										<c:set var="backColor" value="${jsonHtmlColorCodes.map.critical_color_code}"/>
									</c:when>
									<c:when test="${(not empty record.map.test_value && ifn:isNumeric(record.map.test_value) && record.map.test_value > record.map.max_normal_value)}">
										<c:set var="backColor" value="${jsonHtmlColorCodes.map.abnormal_color_code}"/>
									</c:when>
									<c:when test="${(not empty record.map.test_value && ifn:isNumeric(record.map.test_value) && record.map.test_value < record.map.min_normal_value)}">
										<c:set var="backColor" value="${jsonHtmlColorCodes.map.abnormal_color_code}"/>
									</c:when>
									<c:otherwise>
										<c:set var="backColor" value="${jsonHtmlColorCodes.map.normal_color_code}"/>
									</c:otherwise>
								</c:choose>
								<c:choose>
									<c:when test="${record.map.data_allowed eq 'V'}">
										<input type="text" style="width: 60px;background-color:${backColor};" maxlength="450" name="test_value" id="test_value${ifn:cleanHtmlAttribute(index)}"
										value="<c:out value="${record.map.test_value}" />" tabindex="1"/>
										<input type="hidden"  name="resultvalue" value="${record.map.test_value }"/>
										<input type="hidden"  name="values_id" value="${record.map.values_id }"/>
									</c:when>
									<c:otherwise>
										<insta:selectoptions name="test_value" style="width: 60px;background-color:${backColor};"
											id="test_value${index}"
											onkeypress="nextFieldOnEnter(${index},event);"
											opvalues="${record.map.source_if_list}" optexts="${record.map.source_if_list}" value="${record.map.test_value}"
											dummyvalue="${dummyvalue}" dummyvalueId="" onchange="setEdited('${index}')"></insta:selectoptions>
										<input type="hidden"  name="resultvalue" value="${record.map.test_value}"/>
										<input type="hidden"  name="values_id" value="${record.map.values_id }"/>
									</c:otherwise>
								</c:choose>
							</td>
							<%-- <td>
								<select name="withinNormal" id="withinNormal" class="dropdown" style="width:8em"
										onchange="setSeviarity('${record.map.min_normal_value}','${record.map.max_normal_value}',
											'${record.map.min_critical_value}','${record.map.max_critical_value}',
											'${record.map.min_improbable_value}','${record.map.max_improbable_value}',
											this,withinNormal,'','','')";>
									<option value="">-- Select --</option>
									<option value="Y" ${record.map.withinnormal == 'Y' ? 'selected' : ''}>Normal</option>
									<option value="*"  ${record.map.withinnormal == '*' ? 'selected' : ''}>Abnormal Low</option>
									<option value="#"  ${record.map.withinnormal == '#' ? 'selected' : ''}>Abnormal High</option>
									<option value="**" ${record.map.withinnormal == '**' ? 'selected' : ''}>Critical Low</option>
									<option value="##" ${record.map.withinnormal == '##' ? 'selected' : ''}>Critical High</option>
									<option value="***" ${record.map.withinnormal == '***' ? 'selected' : ''}>Improbable Low</option>
									<option value="###" ${record.map.withinnormal == '###' ? 'selected' : ''}>Improbable High</option>
								</select>
							</td> --%>
							<td>
								<input type="text" name="remarks" value="${record.map.remarks}"/>
								<input type="hidden" name="edited" id="edited${ifn:cleanHtmlAttribute(index)}" value="N"/>
							</td>
							<c:set var="testValueDate" value=""/>
							<c:if test="${param._method == 'show'}">
								<c:set var="testValueDate">
									<fmt:formatDate pattern="dd-MM-yyyy" value="${record.map.value_date}"/>
								</c:set>
							</c:if>
							<td>
								<insta:datewidget name="value_date" id="value_date${index}" value="${testValueDate}" extravalidation="setEdited('${index}')" btnPos="left"/>
							</td>
						</tr>
					</c:forEach>
				</c:if>
			</table>
			<input type="hidden" name="clinical_lab_recorded_id" id="clinical_lab_recorded_id" value="${ifn:cleanHtmlAttribute(param.clinical_lab_recorded_id)}">
		<table>
			<tr>
				<td>&nbsp;</td>
			</tr>
		</table>
		<table>
			<tr>
				<td style="text-align: left"><insta:ltext key="patient.clinicaldatalabresults.addshow.notes"/></td>
			</tr>
			<tr>
				<td>
					<textarea rows="4" cols="50" name="notes" id="notes" >${clinicalLabValues.map.notes}</textarea>
				</td>
			</tr>
		</table>

	</fieldset>
	<div style="float: right; margin-top: 10px" >
				<insta:selectdb name="printer" id="printer" table="printer_definition" valuecol="printer_id"
			displaycol="printer_definition_name" value="${pref.map.printer_id}" />
	</div>
	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validateForm(this)"><b><u><insta:ltext key="patient.clinicaldatalabresults.addshow.s"/></u></b><insta:ltext key="patient.clinicaldatalabresults.addshow.ave"/></button>
		| <button type="submit" accesskey="P" name="print" onclick="return validateForm(this)"><insta:ltext key="patient.clinicaldatalabresults.addshow.save"/><b><u><insta:ltext key="patient.clinicaldatalabresults.addshow.p"/></u></b><insta:ltext key="patient.clinicaldatalabresults.addshow.rint"/></button>
		| <a href="${cpath}/dialysis/ClinicalDataLabResults.do?_method=list&mr_no=${not empty clinicalLabValues.map.mrno ? clinicalLabValues.map.mrno : param.mr_no}"><insta:ltext key="patient.clinicaldatalabresults.addshow.labresultslist"/></a>
		| <a href="${cpath}/dialysis/PreDialysisSessions.do?_method=showDialysis&mr_no=${ifn:cleanURL(param.mr_no)}"><insta:ltext key="patient.clinicaldatalabresults.addshow.predialysis"/></a>

	</div>
</form>

</body>
</html>
