<%@page import="com.bob.hms.common.Constants" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<title>Insurance Patients List - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="Insurance/insurance.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="script" file="date_go.js"/>
	<insta:link type="script" file="datetest.js"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>

	<script>
		var cpath = '${cpath}';
		var printPath = cpath + "/Insurance/PreAuthorizationDocumentsPrint.do";
		var toolbarArray = {
			Edit : 		{	title: "Edit",
						imageSrc: "icons/Edit.png",
						href: 'Insurance/PreAuthorizationForms.do?_method=show',
						onclick: null,
				  	},
			Print : 	{	title: "Print",
						imageSrc: "icons/Print.png",
						href: 'Insurance/PreAuthorizationDocumentsPrint.do?_method=print',
						onclick: null,
						target : '_blank'
				  	}
		};

		function init() {
			createToolbar(toolbarArray);
		}
	</script>
</head>

<body onload="init();">
<c:set var="InsDocsList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty InsDocsList}"/>
<div class="pageHeader">Insurance Pre-Authorization</div>
<insta:feedback-panel/>
<div>
	<c:choose>
		<c:when test="${not empty param.patient_id}">
			<insta:patientdetails  visitid="${param.patient_id}" />
		</c:when>
		<c:otherwise>
			<insta:patientgeneraldetails  mrno="${param.mr_no}" />
		</c:otherwise>
	</c:choose>
</div>
<form name="addNewCase" method="POST" action="/Insurance/AddOrEditCase.do" onsubmit="getInsDetails(); return false;">
<input type="hidden" name="_method" id="_method" value="addshow">
<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}">
<input type="hidden" name="insurance_id" value="${ifn:cleanHtmlAttribute(param.insurance_id)}">
<input type="hidden" name="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}"/>

<div class="resultList">
	<table class="resultList" width="100%" id="resultTable" cellspacing="0" cellpadding="0" >
		<tr>
			<th>Document Name</th>
			<th>Date</th>
		</tr>
		<c:forEach var="insDoc" items="${InsDocsList}" varStatus="st">

			<c:set var="edit" value="${!(insDoc.map.status == I &&  dischargeCloseRights == 'A')}"/>
			<tr  class="${st.first?'firstRow':''}" onclick="showToolbar('${st.index}', event, 'resultTable',
							{mr_no: '${ifn:cleanJavaScript(param.mr_no)}', doc_id: ${insDoc.map.doc_id}, template_id: ${insDoc.map.template_id},
							format: '${insDoc.map.doc_format}',patient_id: '${insDoc.map.visit_id}',insurance_id:'${insDoc.map.insurance_id}' ,
							 printerId: '${printpreferences.map.printer_id}'},
							[${edit}, true]);" onmouseover="hideToolBar('${st.index}')" id="toolbarRow${st.index}">
				<td>${insDoc.map.doc_name}</td>
				<td><fmt:formatDate pattern="dd-MM-yyyy" value="${insDoc.map.doc_date}"/></td>
			</tr>
		</c:forEach>
	</table>
</div>
<insta:noresults hasResults="${hasResults}"/>
<div class="screenActions">
	<a href="javascript:void(0)" onclick="funClose();">Case List</a>
</div>
</form>
</body>
</html>
