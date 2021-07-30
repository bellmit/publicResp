<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title><insta:ltext key="clinicaldata.infectionsdata.addorshow.addoreditantibioticdetails"/></title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>
<insta:link type="script" file="ajax.js"/>
<insta:link type="script" file="date_go.js"/>
<insta:link type="script" file="Infections/antibiotics.js"/>

<script>
	var medicineNamesAndIds = <%= request.getAttribute("medicineNamesAndIds")%>
</script>
<insta:js-bundle prefix="clinicaldata.infectionsdata"/>
<insta:js-bundle prefix="clinicaldata.common"/>
</head>
<body onload="init();">
	<h1> <insta:ltext key="clinicaldata.infectionsdata.addorshow.addoreditantibioticfor"/> ${ifn:cleanHtml(param.infection_type)}</h1>
	<insta:feedback-panel/>
	<insta:patientgeneraldetails mrno="${param.mr_no}" addExtraFields="true" showClinicalInfo="true"/>
	<form name="AntibioticForm" action="${cpath}/clinical/Infections.do" method="post" autocomplete="off">
	<input type="hidden" name="_method" value="saveAntibioticDetails">
	<input type="hidden" name="infection_id" value="${ifn:cleanHtmlAttribute(param.infection_id)}">
	<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}">


<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
	<tr>
		<th class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addorshow.antibiotic"/></th>
		<th class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addorshow.form"/></th>
		<th class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addorshow.strength"/></th>
		<th class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addorshow.dosage"/></th>
		<th class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addorshow.durationdays"/></th>
		<th><insta:ltext key="clinicaldata.infectionsdata.addorshow.frequency"/></th>
		<th><insta:ltext key="clinicaldata.infectionsdata.addorshow.antibioticintakestatus"/></th>
		<th class="formlabel">&nbsp;</th>
		<th class="formlabel">&nbsp;</th>
		<th class="formlabel">&nbsp;</th>
	</tr>
	<c:set var="length" value="${fn:length(antibioticsList)}"/>
	<c:forEach var="i" begin="1" end="${length+1}" varStatus="loop">
		<c:set var="record" value="${antibioticsList[i-1]}"/>
		<c:if test="${empty record}">
			<c:set var="style" value='style="display:none"'/>
		</c:if>
		<tr ${style}>
			<td class="formlabel">
				<label>${record.map.medicine_name}</label>
				<input type="hidden" name="op_medicine_pres_id" value="${record.map.op_medicine_pres_id}"/>
				<input type="hidden" name="antibiotic_log_id" value="${record.map.antibiotic_log_id}"/>
				<input type="hidden" name="medicine_name" value="${record.map.medicine_name}"/>
			</td>
			<td>
				<label>${record.map.infection_form_name}</label>
				<input type="hidden" name="infection_form_name" id="infection_form_name" value="${record.map.infection_form_name}">
			</td>
			<td>
				<label>${record.map.strength}</label>
				<input type="hidden" name="strength" id="strength" value="${record.map.strength}">
			</td>
			<td>
				<label>${record.map.dosage}</label>
				<input type="hidden" name="dosage" id="dosage" value="${record.map.dosage}">
				<input type="hidden" name="test_dosage" id="test_dosage" value="${record.map.test_dosage}">
			</td>
			<td>
				<label>${record.map.duration}</label>
				<input type="hidden" name="duration" id="duration" value="${record.map.duration}">
			</td>
			<td>
				<label>${record.map.frequency}</label>
				<input type="hidden" name="frequency" id="frequency" value="${record.map.frequency}">
			</td>
			<td>
			   	<insta:truncLabel value="${record.map.remarks}" length="30"/>
				<input type="hidden" name="remarks"  value="${record.map.remarks}">
				 <input type="hidden" name="hdeleted" value="false">
			</td>
			<td>
				<a title="Cancel Antibiotic Details Row">
					<img src="${cpath}/icons/Delete.png" class="imgDelete button" onclick="deleteItem(this)"/>
				</a>
			<td>
				<a title="Edit Antibiotic Details" >
					<img src="${cpath}/icons/Edit.png" class="button" id="editIcon${index}" name="editIcon" onclick="openEditDialogBox(this)"/>
				</a>
			</td>
			<td>&nbsp;</td>
		</tr>
	</c:forEach>
</table>
<table class="addButton">
	<tr>
		<td align="right">
			<button type="button" name="btnAddItem" id="btnAddItem" title="Press (Alt+Shift+(+)) Key to Add New Antibiotic Details"
					onclick="openDialogBox();"
					accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
		</td>
	</tr>
</table>
<div class="screenActions">
	<button type="submit" accesskey="S"><b><u><insta:ltext key="clinicaldata.infectionsdata.addorshow.s"/></u></b><insta:ltext key="clinicaldata.infectionsdata.addorshow.ave"/></button>
	| <a href="${cpath}/clinical/Infections.do?_method=list"><insta:ltext key="clinicaldata.infectionsdata.addorshow.infectionslist"/></a>
	| <a href="${cpath}/clinical/Infections.do?_method=show&mr_no=${ifn:cleanURL(param.mr_no)}"><insta:ltext key="clinicaldata.infectionsdata.addorshow.showinfections"/></a>
</div>

<div id="antibioticDialog" style="visibility:hidden">
	<div class="hd" id="itemdialogheader"></div>
	<div class="bd">
		<fieldset class="fieldsetborder">
		<legend class="fieldsetlabel"><insta:ltext key="clinicaldata.infectionsdata.addorshow.antibioticdetails"/></legend>
		<table class="formtable">
			<tr>
				<td class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addorshow.medicine"/>:</td>
				<td colspan="3">
					<input type="hidden" name="dialogId">
					<div id="itemAutocomplete" style="padding-bottom: 20px; width: 443px">
						<input type="text" name="dialog_medicine_name" id="dialog_medicine_name" value="" >
						<div id="medicineContainer" style="width: 350px" class="scrolForContainer"></div>
						<input type="hidden" name="dialog_op_medicine_pres_id" id="dialog_op_medicine_pres_id" value="">
					</div>
				</td>
			</tr>

			<tr>
				<td class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addorshow.form"/>:</td>
				<td>
					<input type="text" name="dialog_form" id="dialog_form" value="">
					<input type="hidden" name="dialog_form_id" id="dialog_form_id" value="">
				</td>
				<td class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addorshow.strength"/>:</td>
				<td>
					<input type="text" name="dialog_strength" id="dialog_strength" value="" maxlength="100">
				</td>
			</tr>

			<tr>
				<td class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addorshow.dosage"/>:</td>
				<td>
					<input type="text" name="dialog_dosage" id="dialog_dosage" value="" maxlength="100">
				</td>
				<td class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addorshow.frequency"/>:</td>
				<td>
					<input type="text" name="dialog_frequency" id="dialog_frequency" value="" maxlength="50">
				</td>
			</tr>

			<tr>
				<td class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addorshow.duration"/>:</td>
				<td>
					<input type="text" name="dialog_duration" id="dialog_duration" value="" maxlength="100">
				</td>
				<td class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addorshow.testdosage"/>:</td>
				<td>
					<input type="text" name="dialog_test_dosage" id="dialog_test_dosage" value="" maxlength="100">
				</td>
			</tr>

			<tr>
				<td class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addorshow.antibioticintakestatus"/></td>
				<td colspan="3">
					<textarea rows="2" cols="50" id="dialog_remarks" onblur="checkLength(this,2000,'Remarks')"></textarea>
				</td>
			</tr>
		</table>
		</fieldset>
		<table>
			<tr>
				<td>
					<button type="button" name="Save" accesskey="S" onclick="addRecord();"><b><u><insta:ltext key="clinicaldata.infectionsdata.addorshow.s"/></u></b><insta:ltext key="clinicaldata.infectionsdata.addorshow.ave"/></button>
					<button type="button" name="Cancel" accesskey="C" onclick="cancelDialog();"><b><u><insta:ltext key="clinicaldata.infectionsdata.addorshow.c"/></u></b><insta:ltext key="clinicaldata.infectionsdata.addorshow.ancel"/></button>
				</td>
			</tr>
		</table>
	</div>
</div>

</form>

</body>
</html>