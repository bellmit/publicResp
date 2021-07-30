<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:useBean id="currentDate" class="java.util.Date"/>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="clinicaldata.vaccinations.addoredit.addoreditvaccinationdetails"/></title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>
<insta:link type="script" file="ajax.js"/>
<insta:link type="script" file="date_go.js"/>
<insta:link type="script" file="Vaccinations/vaccinations.js"/>

<script>
	var reasonListJson = <%= request.getAttribute("reasonListJson")%>
	var vaccinationTypeListJson = <%= request.getAttribute("vaccinationTypeListJson")%>
</script>

	<insta:js-bundle prefix="clinicaldata.vaccinations"/>
	<insta:js-bundle prefix="clinicaldata.commonvalidations"/>
	<insta:js-bundle prefix="clinicaldata.common"/>

</head>
<body onload="init();">
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:choose>
<c:when test="${param._method=='add' && empty param.mr_no}">
	<h1 style="float: left"><insta:ltext key="clinicaldata.vaccinations.addoredit.addvaccinationdetails"/></h1>
	<c:url var="url" value="/clinical/Vaccinations.do"/>
	<insta:patientsearch fieldName="mr_no" searchUrl="${url}" searchMethod="show" searchType="mrNo" />
	<insta:feedback-panel/>
	<insta:patientgeneraldetails mrno="${param.mr_no}" addExtraFields="true" showClinicalInfo="true"/>
	<form name="VaccinationForm" action="${cpath}/clinical/Vaccinations.do" method="post">
	<input type="hidden" name="_method" value="saveVaccinationDetails">
	<input type="hidden" name="_searchMethod" value="show"/>
	<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}">
</c:when>
<c:otherwise>
<c:set var="addText"><insta:ltext key="clinicaldata.common.addoredit.add"/></c:set>
		<c:set var="editText"><insta:ltext key="clinicaldata.common.addoredit.edit"/></c:set>
	<h1>${empty vaccinationInformationList ? addText : editText} <insta:ltext key="clinicaldata.vaccinations.addoredit.vaccinationdetails"/></h1>
	<insta:feedback-panel/>
	<insta:patientgeneraldetails mrno="${param.mr_no}" addExtraFields="true" showClinicalInfo="true"/>
	<form name="VaccinationForm" action="${cpath}/clinical/Vaccinations.do" method="post">
	<input type="hidden" name="_method" value="saveVaccinationDetails">
	<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}">
</c:otherwise>
</c:choose>
	<c:if test="${not empty param.mr_no}">
		<br>
		<c:choose>
			<c:when test="${empty dataAsOfDate}">
				<c:set var="valueDate">
					<fmt:formatDate pattern="dd-MM-yyyy" value="${currentDate}"/>
				</c:set>
			</c:when>
			<c:otherwise>
				<c:set var="valueDate">
					<fmt:formatDate pattern="dd-MM-yyyy" value="${dataAsOfDate}"/>
				</c:set>
			</c:otherwise>
		</c:choose>
		<fieldset class="fieldsetborder">
			<div>
				<table class="formtable">
					<tr>
						<td class="formlabel"><insta:ltext key="clinicaldata.vaccinations.addoredit.dataasof"/></td>
						<td class="forminput"><insta:datewidget name="data_as_of_date" id="data_as_of_date" value="${valueDate}"/></td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
					</tr>
				</table>
			</div>
			<br>
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr>
					<th class="formlabel"><insta:ltext key="clinicaldata.vaccinations.addoredit.vaccination"/></th>
					<th class="formlabel"><insta:ltext key="clinicaldata.vaccinations.addoredit.status"/></th>
					<th class="formlabel"><insta:ltext key="clinicaldata.vaccinations.addoredit.reason"/></th>
					<th class="formlabel"><insta:ltext key="clinicaldata.vaccinations.addoredit.dateadministered"/></th>
					<th class="formlabel"><insta:ltext key="clinicaldata.vaccinations.addoredit.remarks"/></th>
					<th class="formlabel">&nbsp;</th>
					<th class="formlabel">&nbsp;</th>
					<th class="formlabel">&nbsp;</th>
				</tr>
				<c:set var="length" value="${fn:length(vaccinationInformationList)}"/>
				<c:forEach var="i" begin="1" end="${length+1}" varStatus="loop">
					<c:set var="record" value="${vaccinationInformationList[i-1]}"/>
					<c:if test="${empty record}">
						<c:set var="style" value='style="display:none"'/>
					</c:if>
					<tr ${style}>
						<td class="formlabel">
							<label>${record.map.vaccination_type}</label>
							<input type="hidden" name="vaccination_id" value="${ifn:cleanHtmlAttribute(param.vaccination_id)}"/>
							<input type="hidden" name="vaccination_details_id" value="${record.map.vaccination_details_id}"/>
							<input type="hidden" name="vaccination_type_id" value="${record.map.vaccination_type_id}"/>
						</td>
						<td>

							<c:choose>
								<c:when test="${record.map.vaccination_status == 'Y'}">
									<label><insta:ltext key="clinicaldata.vaccinations.addoredit.yes"/></label>
								</c:when>
								<c:when test="${record.map.vaccination_status == 'N'}">
									<label><insta:ltext key="clinicaldata.vaccinations.addoredit.no"/></label>
								</c:when>
								<c:when test="${record.map.vaccination_status == 'R'}">
									<label><insta:ltext key="clinicaldata.vaccinations.addoredit.refused"/></label>
								</c:when>
								<c:otherwise>
									<label></label>
								</c:otherwise>
							</c:choose>
							<input type="hidden" name="vaccination_status"  value="${record.map.vaccination_status}">
						</td>
						<td>
							<label>${record.map.reason_name}</label>
							<input type="hidden" name="reason_name"  value="${record.map.reason_name}">
							<input type="hidden" name="no_reason_id"  value="${record.map.no_reason_id}">
						</td>
						<td>
							<label><fmt:formatDate pattern="dd-MM-yyyy" value="${record.map.vaccination_date}"/></label>
							<input type="hidden" name="vaccination_date" id="vaccination_date" value="<fmt:formatDate pattern="dd-MM-yyyy" value='${record.map.vaccination_date}'/>">
							<input type="hidden" name="next_due_date" id="next_due_date" value="<fmt:formatDate pattern="dd-MM-yyyy" value='${record.map.next_due_date}'/>">
						</td>
						<td>
						   	<insta:truncLabel value="${record.map.remarks}" length="30"/>
							<input type="hidden" name="remarks"  value="${record.map.remarks}">
							 <input type="hidden" name="hdeleted" value="false">
						</td>
						<td>
							<a title='<insta:ltext key="clinicaldata.vaccinations.addoredit.cancelvaccinationdetails"/>'>
								<img src="${cpath}/icons/Delete.png" class="imgDelete button" onclick="deleteItem(this)"/>
							</a>
						<td>
							<a title='<insta:ltext key="clinicaldata.vaccinations.addoredit.editvaccinationdetails"/>'>
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
							<button type="button" name="btnAddItem" id="btnAddItem" title='<insta:ltext key="clinicaldata.vaccinations.addoredit.pressnew"/>'
									onclick="openDialogBox();"
									accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
						</td>
					</tr>
				</table>
		</fieldset>
		<table >
			<tr>
				<td style="text-align: left"><insta:ltext key="clinicaldata.vaccinations.addoredit.lastupdateduserordate"/></td>
				<td>&nbsp;</td>
				<c:if test="${param._method == 'show'}">
					<td style="text-align: left;color:#444;font-weight: bold;">
						${ifn:cleanHtml(userName)}/
						<fmt:formatDate pattern="dd-MM-yyyy" value="${mod_time}"/>
						<fmt:formatDate pattern="HH:mm:ss" value="${mod_time}"/>
					</td>
				</c:if>
			</tr>
		</table>
		<div class="screenActions">
			<button type="submit" accesskey="A" onclick="return validateForm()"><insta:ltext key="clinicaldata.vaccinations.addoredit.s"/><b><u><insta:ltext key="clinicaldata.vaccinations.addoredit.a"/></u></b><insta:ltext key="clinicaldata.vaccinations.addoredit.ve"/></button>
			| <a href="${cpath}/clinical/Vaccinations.do?_method=list"><insta:ltext key="clinicaldata.vaccinations.addoredit.vaccinationlist"/></a>
			| <a href="${cpath}/dialysis/PreDialysisSessions.do?_method=showDialysis&mr_no=${ifn:cleanURL(param.mr_no)}"><insta:ltext key="clinicaldata.vaccinations.addoredit.predialysis"/></a>
		</div>
	</c:if>
		<div id="vaccinationDialog" style="visibility:hidden">
		<div class="hd" id="itemdialogheader"><insta:ltext key="clinicaldata.vaccinations.addoredit.vaccination"/>:</div>
		<div class="bd">
			<fieldset class="fieldsetborder">
			<legend class="fieldsetlabel"><insta:ltext key="clinicaldata.vaccinations.addoredit.vaccinationdetails"/></legend>
			<table class="formtable" cellpadding="0" cellspacing="0">
				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.vaccinations.addoredit.vaccination"/>:</td>
					<td colspan="2">
						<input type="hidden" name="dialogId">
						<insta:selectdb name="dialog_vaccination_type" id="dialog_vaccination_type" table="clinical_vaccinations_master" valuecol="vaccination_type_id"
							displaycol="vaccination_type" filtercol="status" filtervalue="A" orderby="vaccination_type" dummyvalue="${dummyvalue}" dummyvalueId=""
							onchange="checkFrequencyExists(this)" value=""/>
						<input type="hidden" name="dialog_vaccination_type_id" id="dialog_vaccination_type_id" value="">
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.vaccinations.addoredit.status"/>:
					</td>
					<td colspan="2">
						<select name="dialog_vaccination_status" id="dialog_vaccination_status" class="dropdown" onchange="showReason(this)">
							<option value="">${dummyvalue}</option>
							<option value="Y"><insta:ltext key="clinicaldata.vaccinations.addoredit.yes"/></option>
							<option value="N"><insta:ltext key="clinicaldata.vaccinations.addoredit.no"/></option>
							<option value="R"><insta:ltext key="clinicaldata.vaccinations.addoredit.refused"/></option>
						</select>
						<input type="hidden" name="dialog_vaccination_status_id" id="dialog_vaccination_status_id" value="">
					</td>
				</tr>
				<tr style="display:none" id="trId">
					<td class="formlabel"><insta:ltext key="clinicaldata.vaccinations.addoredit.reason"/>:</td>
					<td colspan="2">
						<insta:selectdb name="dialog_reason_name" id="dialog_reason_name" table="clinical_vacc_no_reason" valuecol="reason_id"
							displaycol="reason_name" filtered="false" orderby="reason_name" dummyvalue="${dummyvalue}" dummyvalueId=""
							onchange="" value=""/>
						<input type="hidden" name="dialog_reason_id" id="dialog_reason_id" value="">
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.vaccinations.addoredit.dateadministered"/>:</td>
					<td style="width:105px">
						<insta:datewidget name="dialog_administered_date" id="dialog_administered_date" value=""/>
					</td>
					<td>
						<button type="button" id="nextDueDate" title='<insta:ltext key="clinicaldata.vaccinations.addoredit.longtemplate"/>' name="calculate" onclick="calculateNextDueDate();" accesskey="N"><b><u><insta:ltext key="clinicaldata.vaccinations.addoredit.n"/></u></b><insta:ltext key="clinicaldata.vaccinations.addoredit.extdue"/></button>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.vaccinations.addoredit.nextduedate"/>:</td>
					<td colspan="2">
						<insta:datewidget name="dialog_due_date" id="dialog_due_date" value=""/>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.vaccinations.addoredit.remarks"/></td>
					<td colspan="2">
						<textarea rows="2" cols="11" id="dialog_remarks" onblur="checkLength(this,2000,'Remarks')"></textarea>
					</td>
				</tr>
			</table>
			</fieldset>
			<table>
				<tr>
					<td>
						<button type="button" name="Save" accesskey="S" onclick="addRecord();"><b><u><insta:ltext key="clinicaldata.vaccinations.addoredit.adds"/></u></b><insta:ltext key="clinicaldata.vaccinations.addoredit.ave"/></button>
						<button type="button" name="Cancel" accesskey="C" onclick="cancelDialog();"><b><u><insta:ltext key="clinicaldata.vaccinations.addoredit.c"/></u></b><insta:ltext key="clinicaldata.vaccinations.addoredit.ancel"/></button>
					</td>
				</tr>
			</table>
		</div>
		</div>
</form>

</body>
</html>