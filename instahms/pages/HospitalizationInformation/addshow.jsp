<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:useBean id="currentDate" class="java.util.Date"/>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.addoreditclinicalhospitalizationinformationlist"/></title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>
<insta:link type="script" file="ajax.js"/>
<insta:link type="script" file="date_go.js"/>
<insta:link type="script" file="HospitalizationInformation/HospitalizationInformation.js"/>

<script>
	var reasonListJson = <%= request.getAttribute("reasonListJson")%>
</script>

<insta:js-bundle prefix="clinicaldata.hospitalizationinformation"/>
<insta:js-bundle prefix="clinicaldata.commonvalidations"/>
<insta:js-bundle prefix="clinicaldata.common"/>

</head>
<body onload="init();">
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>

<c:choose>
<c:when test="${param._method=='add' && empty param.mr_no}">
	<h1 style="float: left"><insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.addhospitalizationinformation"/></h1>
	<c:url var="url" value="/dialysis/HospitalizationInformation.do"/>
	<insta:patientsearch fieldName="mr_no" searchUrl="${url}" searchMethod="show" searchType="mrNo" />
	<insta:feedback-panel/>
	<insta:patientgeneraldetails mrno="${param.mr_no}" addExtraFields="true" showClinicalInfo="true"/>
	<form name="HospitalizationInformationForm" action="${cpath}/dialysis/HospitalizationInformation.do" method="post">
	<input type="hidden" name="_method" value="saveHospitalizationDetails">
	<input type="hidden" name="_searchMethod" value="show"/>
	<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}">
</c:when>
<c:otherwise>
<c:set var="addText"><insta:ltext key="clinicaldata.common.addoredit.add"/></c:set>
		<c:set var="editText"><insta:ltext key="clinicaldata.common.addoredit.edit"/></c:set>
	<h1>${empty hospitalizationInformationList ? addText: editText} <insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.hospitalizationinformation"/></h1>
	<insta:feedback-panel/>
	<insta:patientgeneraldetails mrno="${param.mr_no}" addExtraFields="true" showClinicalInfo="true"/>
	<form name="HospitalizationInformationForm" action="${cpath}/dialysis/HospitalizationInformation.do" method="post">
	<input type="hidden" name="_method" value="saveHospitalizationDetails">
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
						<td class="formlabel"><insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.dataasof"/></td>
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
					<th class="formlabel"><insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.hospitalname"/></th>
					<th class="formlabel"><insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.admissiondate"/></th>
					<th class="formlabel"><insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.dischargedate"/></th>
					<th class="formlabel"><insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.reason"/></th>
					<th class="formlabel"><insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.remarks"/></th>
					<th class="formlabel">&nbsp;</th>
					<th class="formlabel">&nbsp;</th>
					<th class="formlabel">&nbsp;</th>
				</tr>
				<c:set var="length" value="${fn:length(hospitalizationInformationList)}"/>
				<c:forEach var="i" begin="1" end="${length+1}" varStatus="loop">
					<c:set var="record" value="${hospitalizationInformationList[i-1]}"/>
					<c:if test="${empty record}">
						<c:set var="style" value='style="display:none"'/>
					</c:if>
					<tr ${style}>
						<td class="formlabel">
							<label>${record.map.hospital_name}</label>
							<input type="hidden" name="hospitalization_id" value="${ifn:cleanHtmlAttribute(param.hospitalization_id)}"/>
							<input type="hidden" name="hospitalization_details_id" value="${record.map.hospitalization_details_id}"/>
							<input type="hidden" name="hospital_name"  value="${record.map.hospital_name}">
						</td>
						<td>
							<label><fmt:formatDate pattern="dd-MM-yyyy" value="${record.map.admission_date}"/></label>
							<input type="hidden" name="admission_date"  value='<fmt:formatDate pattern="dd-MM-yyyy" value="${record.map.admission_date}"/>'>
						</td>
						<td>
							<label><fmt:formatDate pattern="dd-MM-yyyy" value="${record.map.discharge_date}"/></label>
							<input type="hidden" name="discharge_date"  value='<fmt:formatDate pattern="dd-MM-yyyy" value="${record.map.discharge_date}"/>'>
						</td>
						<td>
							<label>${record.map.reason}</label>
							<input type="hidden" name="reason"  value="${record.map.reason}">
							<input type="hidden" name="reason_id"  value="${record.map.reason_id}">
						</td>
						<td>
						   	<insta:truncLabel value="${record.map.remarks}" length="30"/>
							<input type="hidden" name="remarks"  value="${record.map.remarks}">
							 <input type="hidden" name="hdeleted" value="false">
						</td>
						<td>
							<a title='<insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.cancelhospitalizationinformation"></insta:ltext>'>
								<img src="${cpath}/icons/Delete.png" class="imgDelete button" onclick="deleteItem(this)"/>
							</a>
							</td>
						<td>
							<a title='<insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.edithospitalizationinformation"></insta:ltext>'>
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
							<button type="button" name="btnAddItem" id="btnAddItem" title='<insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.addnewhospitalizationinformation"></insta:ltext>'
									onclick="openDialogBox();"
									accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
						</td>
					</tr>
				</table>
		</fieldset>
		<table >
			<tr>
				<td style="text-align: left"><insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.lastupdated.user.date"/></td>
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
			<button type="submit" accesskey="A" onclick="return validateForm()"><insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.s"/><b><u><insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.a"/></u></b><insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.ve"/></button>
			| <a href="${cpath}/dialysis/HospitalizationInformation.do?_method=list"><insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.hospitalizationinformationlist"/></a>
			| <a href="${cpath}/dialysis/PreDialysisSessions.do?_method=showDialysis&mr_no=${ifn:cleanURL(param.mr_no)}"><insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.predialysis"/></a>
		</div>
	</c:if>
		<div id="hospitalizationDialog" style="visibility:hidden">
		<div class="hd" id="itemdialogheader"><insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.additem"/></div>
		<div class="bd">
			<table class="formtable" cellpadding="0" cellspacing="0">
				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.hospital"/>:
					</td>
					<td>
						<input type="hidden" name="dialogId">
						<input type="text" name="dialog_hospital_name" id="dialog_hospital_name" value="">
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.admissiondate"/>:</td>
					<td >
						<insta:datewidget name="dialog_admission_date" id="dialog_admission_date" value=""/>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.dischargedate"/>:</td>
					<td >
						<insta:datewidget name="dialog_discharge_date" id="dialog_discharge_date" value=""/>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.reason"/>:</td>
					<td>
						<insta:selectdb name="dialog_reason" id="dialog_reason" table="clinical_hospitalization_reasons" valuecol="reason_id"
						displaycol="reason" filtercol="status" filtervalue="A" orderby="reason" dummyvalue="${dummyvalue}" dummyvalueId=""
						onchange="" value=""/>
						<input type="hidden" name="dialog_reason_id" value="">
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.remarks"/></td>
					<td>
						<textarea rows="2" cols="11" id="dialog_remarks" onblur="checkLength(this,2000,'Remarks')"></textarea>
					</td>
				</tr>
				<tr>
					<td>
						<button type="button" name="Save" accesskey="S" onclick="addRecord();"><b><u><insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.adds"/></u></b><insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.ave"/></button>
						<button type="button" name="Cancel" accesskey="C" onclick="cancelDialog();"><b><u><insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.c"/></u></b><insta:ltext key="clinicaldata.hospitalizationinformation.addoredit.ancel"/></button>
					</td>
				</tr>
			</table>
		</div>
		</div>
</form>

</body>
</html>