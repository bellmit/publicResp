<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<jsp:useBean id="currentDate" class="java.util.Date"/>
<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="clinicaldata.infectionsdata.addoredit.addoreditinfectionsdetails"/></title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>
<insta:link type="script" file="ajax.js"/>
<insta:link type="script" file="date_go.js"/>
<insta:link type="script" file="Infections/infections.js"/>


<script>
	var infectionTypeListJson = <%= request.getAttribute("infectionTypeListJson")%>
	var infectionsSitesJson = <%= request.getAttribute("infectionsSitesJson")%>
</script>
<insta:js-bundle prefix="clinicaldata.infectionsdata"/>
<insta:js-bundle prefix="clinicaldata.commonvalidations"/>
<insta:js-bundle prefix="clinicaldata.common"/>
</head>
<body onload="init();">
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:choose>
<c:when test="${param._method=='add' && empty param.mr_no}">
	<h1 style="float: left"><insta:ltext key="clinicaldata.infectionsdata.addoredit.addinfectiondetails"/></h1>
	<c:url var="url" value="/clinical/Infections.do"/>
	<insta:patientsearch fieldName="mr_no" searchUrl="${url}" searchMethod="show" searchType="mrNo" />
	<form name="InfectionForm" action="${cpath}/clinical/Infections.do" method="post">
	<input type="hidden" name="_method" value="saveInfectionDetails">
	<input type="hidden" name="_searchMethod" value="show"/>
	<input type="hidden" name="mr_no" id="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}">
</c:when>
<c:otherwise>

		<c:set var="addText"><insta:ltext key="clinicaldata.common.addoredit.add"/></c:set>
		<c:set var="editText"><insta:ltext key="clinicaldata.common.addoredit.edit"/></c:set>

	<h1>${empty infectionsInformationList ? addText : editText} <insta:ltext key="clinicaldata.infectionsdata.addoredit.infectionsdetails"/></h1>
	<form name="InfectionForm" action="${cpath}/clinical/Infections.do" method="post">
	<input type="hidden" name="_method" value="saveInfectionDetails">
	<input type="hidden" name="mr_no" id="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}">
</c:otherwise>
</c:choose>
<insta:feedback-panel/>
<insta:patientgeneraldetails mrno="${param.mr_no}" addExtraFields="true" showClinicalInfo="true"/>
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
						<td class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addoredit.dataasof"/></td>
						<td class="forminput"><insta:datewidget name="values_as_of_date" id="values_as_of_date" value="${valueDate}"/></td>
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
					<th class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addoredit.infection"/></th>
					<th class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addoredit.effectivedate"/></th>
					<th class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addoredit.site"/></th>
					<th class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addoredit.infectionorganism"/></th>
					<th class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addoredit.infectionstatus"/></th>
					<th><insta:ltext key="clinicaldata.infectionsdata.addoredit.antimicrobialsusceptibility"/></th>
					<th><insta:ltext key="clinicaldata.infectionsdata.addoredit.antibiotics"/></th>
					<th><insta:ltext key="clinicaldata.infectionsdata.addoredit.remarks"/></th>
					<th class="formlabel">&nbsp;</th>
					<th class="formlabel">&nbsp;</th>
					<th class="formlabel">&nbsp;</th>
				</tr>
				<c:set var="length" value="${fn:length(infectionsInformationList)}"/>
				<c:forEach var="i" begin="1" end="${length+1}" varStatus="loop">
					<c:set var="index" value="${loop.index}"/>
					<c:set var="record" value="${infectionsInformationList[i-1]}"/>
					<c:if test="${empty record}">
						<c:set var="style" value='style="display:none"'/>
					</c:if>
					<tr ${style}>
						<td class="formlabel">
							<label>${record.map.infection_type}</label>
							<input type="hidden" name="clinical_infections_recorded_id" value="${ifn:cleanHtmlAttribute(param.clinical_infections_recorded_id)}"/>
							<input type="hidden" name="infection_id" value="${record.map.infection_id}"/>
							<input type="hidden" name="infection_type_id" value="${record.map.infection_type_id}"/>
							<input type="hidden" name="old_infection_type_id" id="old_infection_type_id${index}" value="${record.map.infection_type_id}"/>
							<input type="hidden" name="infection_type" value="${record.map.infection_type}"/>
						</td>
						<td>
							<label><fmt:formatDate pattern="dd-MM-yyyy" value="${record.map.infection_effective_date }"/></label>
							<input type="hidden" name="infection_effective_date" id="infection_effective_date" value="<fmt:formatDate pattern="dd-MM-yyyy" value='${record.map.infection_effective_date}'/>">
						</td>
						<td>
							<label>${record.map.infection_site_name}</label>
							<input type="hidden" name="infection_site_id" id="infection_site_id" value="${record.map.infection_site_id}">
						</td>
						<td>
							<label>${record.map.infecting_organism}</label>
							<input type="hidden" name="infecting_organism" id="infecting_organism" value="${record.map.infecting_organism}">
						</td>
						<td>
							<c:choose>
								<c:when test="${record.map.infection_status == 'Y'}">
									<label><insta:ltext key="clinicaldata.infectionsdata.addoredit.yes"/></label>
								</c:when>
								<c:when test="${record.map.infection_status == 'N'}">
									<label><insta:ltext key="clinicaldata.infectionsdata.addoredit.no"/></label>
								</c:when>
								<c:otherwise>
									<label></label>
								</c:otherwise>
							</c:choose>
							<input type="hidden" name="infection_status"  value="${record.map.infection_status}">
						</td>

						<td>
							<label>${record.map.anti_microbial_susceptibility}</label>
							<input type="hidden" name="anti_microbial_susceptibility" id="anti_microbial_susceptibility" value="${record.map.anti_microbial_susceptibility}">
						</td>
						<td>
							<c:if test="${not empty record.map.infection_id}">
								<a href="javascript:void(0);" onclick="openAddAntibioticsTab('${ifn:cleanJavaScript(record.map.infection_id)}',${ifn:cleanJavaScript(index)},'${ifn:cleanJavaScript(record.map.infection_type_id)}','${ifn:cleanJavaScript(record.map.infection_type)}')" id="addedit${i}"><insta:ltext key="clinicaldata.infectionsdata.addoredit.addoreditantibiotics"/></a>
							</c:if>
						</td>
						<td>
						   	<insta:truncLabel value="${record.map.remarks}" length="30"/>
							<input type="hidden" name="remarks"  value="${record.map.remarks}">
							 <input type="hidden" name="hdeleted" value="false">
						</td>
						<td>
							<a title='<insta:ltext key="clinicaldata.infectionsdata.addoredit.cancelinfectiondetails"/>'>
								<img src="${cpath}/icons/Delete.png" class="imgDelete button" onclick="deleteItem(this)"/>
							</a>
						<td>
							<a title='<insta:ltext key="clinicaldata.infectionsdata.addoredit.editinfectiondetaildetailsimg"/>' >
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
							<button type="button" name="btnAddItem" id="btnAddItem" title='<insta:ltext key="clinicaldata.infectionsdata.addoredit.addnewinfectiondetails"/>'
									onclick="openDialogBox();"
									accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
						</td>
					</tr>
				</table>
		</fieldset>
		<table >
			<tr>
				<td style="text-align: left"><insta:ltext key="clinicaldata.infectionsdata.addoredit.lastupdateduserordate"/></td>
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
			<button type="submit" accesskey="A" onclick="return validateForm()"><insta:ltext key="clinicaldata.infectionsdata.addoredit.s"/><b><u><insta:ltext key="clinicaldata.infectionsdata.addoredit.a"/></u></b><insta:ltext key="clinicaldata.infectionsdata.addoredit.ve"/></button>
			| <a href="${cpath}/clinical/Infections.do?_method=list"><insta:ltext key="clinicaldata.infectionsdata.addoredit.infectionslist"/></a>
			| <a href="${cpath}/dialysis/PreDialysisSessions.do?_method=showDialysis&mr_no=${ifn:cleanURL(param.mr_no)}"><insta:ltext key="clinicaldata.infectionsdata.addoredit.predialysis"/></a>
		</div>
	</c:if>
		<input type="hidden" name="e_infection_type_id" id="e_infection_type_id" value="">
		<div id="infectionDialog" style="visibility:hidden">
		<div class="hd" id="itemdialogheader"></div>
		<div class="bd">
			<fieldset class="fieldsetborder">
			<legend class="fieldsetlabel"><insta:ltext key="clinicaldata.infectionsdata.addoredit.infectionsdetails"/></legend>
			<table class="formtable" cellpadding="0" cellspacing="0">
				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addoredit.infection"/>:</td>
					<td colspan="2">
						<input type="hidden" name="dialogId">
						<insta:selectdb name="dialog_infection_type" id="dialog_infection_type" table="clinical_infections_master" valuecol="infection_type_id"
							displaycol="infection_type" filtercol="status" filtervalue="A" orderby="infection_type" dummyvalue="${dummyvalue}" dummyvalueId=""
							onchange="" value=""/>
						<span class="star">*</span>
					</td>
				</tr>

				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addoredit.infectionstatus"/>:</td>
					<td>
						<select name="dialog_infection_status" id="dialog_infection_status" class="dropdown">
							<option value="">${dummyvalue}</option>
							<option value="Y"><insta:ltext key="clinicaldata.vaccinations.addoredit.yes"/></option>
							<option value="N"><insta:ltext key="clinicaldata.vaccinations.addoredit.no"/></option>
						</select>
						<span class="star">*</span>
					</td>
				</tr>

				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addoredit.effectivedate"/>:</td>
					<td>
						<insta:datewidget name="dialog_effective_date" id="dialog_effective_date" value=""/>
						<span class="star">*</span>
					</td>
				</tr>

				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addoredit.site"/>:</td>
					<td>
						<insta:selectdb name="dialog_infection_site" id="dialog_infection_site" table="clinical_infection_site_master" valuecol="infection_site_id"
							displaycol="infection_site_name" filtercol="status" filtervalue="A" orderby="infection_site_name" dummyvalue="${dummyvalue}" dummyvalueId=""
							onchange="" value=""/>
							<span class="star">*</span>
					</td>
				</tr>

				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addoredit.infectionorganism"/>:</td>
					<td>
						<input type="text" name="dialog_infecting_organism" id="dialog_infecting_organism" value="">
						<span class="star">*</span>
					</td>
				</tr>

				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addoredit.antimicrobialsusceptibility"/>:</td>
					<td>
						<input type="text" name="dialog_microbial_susceptibility" id="dialog_microbial_susceptibility" value="">
						<span class="star">*</span>
					</td>
				</tr>

				<tr>
					<td class="formlabel"><insta:ltext key="clinicaldata.infectionsdata.addoredit.remarks"/></td>
					<td colspan="2">
						<textarea rows="2" cols="14" id="dialog_remarks" onblur="checkLength(this,2000,'Remarks')"></textarea>
					</td>
				</tr>
			</table>
			</fieldset>
			<table>
				<tr>
					<td>
						<button type="button" name="Save" accesskey="S" onclick="addRecord();"><b><u><insta:ltext key="clinicaldata.infectionsdata.addoredit.adds"/></u></b><insta:ltext key="clinicaldata.infectionsdata.addoredit.ave"/></button>
						<button type="button" name="Cancel" accesskey="C" onclick="cancelDialog();"><b><u><insta:ltext key="clinicaldata.infectionsdata.addoredit.c"/></u></b><insta:ltext key="clinicaldata.infectionsdata.addoredit.ancel"/></button>
					</td>
				</tr>
			</table>
		</div>
		</div>
</form>

</body>
</html>