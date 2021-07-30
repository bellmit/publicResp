<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>

<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default") %>'/>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Referral Doctor List - Insta HMS</title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<script type="text/javascript">
		var referaldoctorCenterapplicable = ${max_centers_inc_default > 1 ? 'true' : 'false'};
		var toolBar = {
			Edit : {
				title : "View/Edit",
				imageSrc : "icons/Edit.png",
				href : "/master/ReferalDoctor.do?_method=show",
				onclick : null,
				description : "View and/or Edit the contents of this Referral"
				},
			Center_Applicability: {
				title: "Center Applicability",
				imageSrc : "icons/Edit.png",
				href: '/master/ReferalDoctorApplicability.do?_method=getScreen',
				onclick: null,
				description : 'Center Applicability of this Referral Doctor',
				show : referaldoctorCenterapplicable
			}
		};

		function init() {
			createToolbar(toolBar);
		}
	</script>
</head>
<body onload="init();">

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>

<h1>Referral Doctor</h1>

<insta:feedback-panel/>

<form name="DepartmentForm" method="GET">

	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search-lessoptions form="DepartmentForm" >
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Referral Doctor's Name</div>
					<div class="sboFieldInput">
						<input type="text" name="referal_name" value="${ifn:cleanHtmlAttribute(param.referal_name)}" />
						<input type="hidden" name="referal_name@op" value="ico"/>
					</div>
				</div>
				<div class="sboField" style="height:69">
					<div class="sboFieldLabel">Status</div>
					<div class="sboFieldInput">
						<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
							<input type="hidden" name="status@op" value="in"/>
					</div>
				</div>
			</div>
		</insta:search-lessoptions>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList" >
		<table class="resultList" cellspacing="" cellpadding="" id="resultTable" onmouseover="hideToolBar();">
			<tr onmouseover="hideToolBar();">
				<th>#</th>
				<insta:sortablecolumn name="referal_name" title="Referral Doctor"/>
				<th>Payment Category</th>
				<th>Payment Eligible</th>
			</tr>
			<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
					onclick="showToolbar(${st.index}, event, 'resultTable', {referal_no: '${record.referal_no}'},'');">

					<td>${(pagedList.pageNumber -1 )* pagedList.pageSize + st.index + 1}</td>
					<td>
						<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						${record.referal_name}
					</td>
					<td>${record.cat_name}</td>
					<td>
						<c:if test="${record.payment_eligible eq 'Y'}">Yes</font></c:if>
						<c:if test="${record.payment_eligible eq 'N'}">No</c:if>
					</td>
				</tr>
			</c:forEach>
		</table>
	</div>

		<c:if test="${empty pagedList.dtoList}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

		<c:url var="Url" value="ReferalDoctor.do">
			<c:param name="_method" value="add"/>
		</c:url>

		<div class="screenActions" style="float: left;">
			<a href="${Url}">Add New Referral Doctor</a>
		</div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>

</form>
	<insta:CsvDataHandler divid="upload1" action="ReferalDoctor.do"/>
</body>
</html>
