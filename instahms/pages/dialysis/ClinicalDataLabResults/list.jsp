<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
<title><insta:ltext key="patient.clinicaldatalabresults.list.title"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta name="i18nSupport" content="true"/>
<insta:link type="script" file="hmsvalidation.js"/>
<script language="javascript" type="text/javascript">
var cpath = '<%= request.getContextPath()%>';
</script>
<insta:js-bundle prefix="clinicaldata.commonvalidations"/>
</head>
<body  class="setMargin yui-skin-sam" onload="">
<h1 style="float: left"><insta:ltext key="patient.clinicaldatalabresults.list.h1"/></h1>
<c:url var="url" value="/dialysis/ClinicalDataLabResults.do"/>
<insta:patientsearch fieldName="mr_no" searchUrl="${url}" searchMethod="list" searchType="mrNo" />
<insta:feedback-panel/>
<insta:patientgeneraldetails mrno="${param.mr_no}" addExtraFields="true" showClinicalInfo="true"/>
<form name="ClinicalDataLabResultsForm" action="/dialysis/ClinicalDataLabResults.do">
<input type="hidden" name="_method" value="list">
<input type="hidden" name="_searchMethod" value="list"/>
<input type="hidden" name="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}">
<input type="hidden" name="VisitId" />
<c:set var="addurl" value="${cpath}/dialysis/ClinicalDataLabResults.do?_method=add&mr_no=${ifn:cleanURL(param.mr_no)}"/>

<c:if test="${not empty param.mr_no}">
	<c:choose>
	<c:when test="${not empty pagedList.dtoList}">
		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
		<table width="100%" height="100%" border="0" class="resultList">
			<tr>
				<th style="width:70px;">&nbsp;</th>
				<th><insta:ltext key="patient.clinicaldatalabresults.list.th"/></th>
				<c:forEach items="${pagedList.dtoList}" var="dateRecord">
					<th style="text-align: center">
						<a href="${cpath}/dialysis/ClinicalDataLabResults.do?_method=show&mr_no=${ifn:cleanURL(param.mr_no)}
							&values_as_of_date=${dateRecord.map.values_as_of_date}
							&clinical_lab_recorded_id=${dateRecord.map.clinical_lab_recorded_id}"
							title='<insta:ltext key="patient.clinicaldatalabresults.list.edit.labresults"/> <fmt:formatDate pattern="dd-MM-yyyy" value="${dateRecord.map.values_as_of_date}"/>'>
							<fmt:formatDate pattern="dd-MM" value="${dateRecord.map.values_as_of_date}"/>
						</a>
					</th>
				</c:forEach>
			</tr>
				<c:forEach items="${masterList}" var="record">
					<tr>
						<td>${record.map.resultlabel_short}</td>
						<td>${record.map.resultlabel}</td>
						<c:forEach var="rec" items="${dateList.dtoList}">
								<td style="text-align: center">${test_result_values[rec.clinical_lab_recorded_id_text][record.map.resultlabel_id].map.test_value}</td>
						</c:forEach>
					</tr>
			</c:forEach>
		</table>
	</c:when>
	<c:otherwise>
		<table border="0" width="100%" height="100%">
			<tr>
				<td align="center" valign="top" style="font-size: 15pt;"><insta:ltext key="patient.clinicaldatalabresults.list.norecords"/></td>
			</tr>
		</table>
	</c:otherwise>
	</c:choose>
	<div class="screenActions" style="float:left">
		<a href='<c:out value="${addurl}"/>'><insta:ltext key="patient.clinicaldatalabresults.list.addnew"/></a>
		<c:if test="${not empty param.mr_no}">
			| <a href="${cpath}/dialysis/PreDialysisSessions.do?_method=showDialysis&mr_no=${ifn:cleanURL(param.mr_no)}"><insta:ltext key="patient.clinicaldatalabresults.list.predialysis"/></a>
		</c:if>
	</div>
</c:if>
</form>
	<script>

	</script>
</body>
</html>
