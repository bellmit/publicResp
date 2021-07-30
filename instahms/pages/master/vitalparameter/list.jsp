<%@page import="com.insta.hms.master.URLRoute"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="pagePath" value="<%=URLRoute.VITAL_PARAMETER_PATH %>"/>
<c:set var="pagePathForRange" value="<%=URLRoute.VITAL_PARAMETER_RANGE %>"/>
<html>

<head>
<title>Vital(I/O) Parameter Dashboard-Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css" />
	<insta:link type="script" file="widgets.js" />
	<insta:link type="script" file="hmsvalidation.js" />


	<style type="text/css">
		.InactiveColor{background-color: #F9966B}
	</style>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit Parameter",
				imageSrc: "icons/Edit.png",
				href: '${pagePath}/show.htm?',
				onclick: null,
				description: "View and/or Edit Vital(I/O) Parameter details"
				},
			EditResults:{
				title: "View/Edit Reference Ranges",
				imageSrc: "icons/Edit.png",
				href: "${pagePathForRange}/show.htm?",
				onclick: null,
				description: "View and/or Edit Reference Ranges"
			}
		};
		function init()
		{
			createToolbar(toolbar);
		}
	</script>

</head>

	<c:set var="cpath" value="${pageContext.request.contextPath}" />

	<c:set var="hasResults" value="${empty pagedList.dtoList ? false : true}"></c:set>

<body class="yui-skin-sam" onload="init()">

	<h1>Vital(I/O) Parameter Dashboard</h1>

	<insta:feedback-panel/>


	<form method="GET" name="VitalSearchForm">
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="VitalSearchForm"  >
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Label Name:</div>
					<div class="sboFieldInput">
						<input type="text" name="param_label" Id="labelName" value="${ifn:cleanHtmlAttribute(param.param_label)}"/>
						<input type="hidden" name="param_label@op" value="ico" />
					</div>
				</div>
				<div class="sboField" style="padding-bottom:50px">
					<div class="sboFieldLabel">Vital(I/O) Status:</div>
					<div class="sboFieldInput">
						<insta:checkgroup name="param_status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.param_status}"/>
						<input type="hidden" name="param_status@op" value="in" />
					</div>
				</div>
				<div class="sboField" style="padding-bottom:50px">
					<div class="sboFieldLabel">Visit Type:</div>
					<div class="sboFieldInput">
							<insta:checkgroup name="visit_type" opvalues="I,O" optexts="IP,OP"
									selValues="${paramValues.visit_type}"/>
					</div>
				</div>
			</div>
		</insta:search-lessoptions>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
				<th>#</th>
					<insta:sortablecolumn name="param_container" title="Vital(I/O) Category"/>
					<insta:sortablecolumn name="param_label" title="Label Name"/>
					<th>Reference Ranges</th>
					<th>UOM</th>
					<insta:sortablecolumn name="param_order" title="Order"/>
					<th>Visit Type</th>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
						{param_id: '${record.param_id}'},[true,${record.param_container eq 'V'}]);" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1}</td>
						<c:set var="isReferenceExist" value="0"/>
						<c:forEach items="${referenceRangeList}" var="referenceBean">
							<c:if test="${referenceBean.param_id eq record.param_id}">
								<c:set var="isReferenceExist" value="1"/>
							</c:if>
						</c:forEach>
						<c:if test="${record.param_container eq 'V'}">
							<td>
								<c:if test="${record.param_status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
								<c:if test="${record.param_status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
								Vital
							</td>
						</c:if>
						<c:if test="${record.param_container eq 'I'}">
							<td>
								<c:if test="${record.param_status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
								<c:if test="${record.param_status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
								Intake
							</td>
						</c:if>
						<c:if test="${record.param_container eq 'O'}">
							<td>
								<c:if test="${record.param_status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
								<c:if test="${record.param_status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
								Output
							</td>
						</c:if>
						<td>${record.param_label}</td>
						<td>
							<c:if test="${isReferenceExist eq 1}">
								<img src="${cpath}/images/check-mark-icon.png" />
							</c:if>
						</td>
						<td>${record.param_uom}</td>
						<td>${record.param_order}</td>
						<td>${record.visit_type}</td>
					</tr>
				</c:forEach>
			</table>

			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

		</div>

	<div class="screenActions" style="float:left"><a href="${cpath}/${pagePath}/add.htm">Add New Vital(I/O) Parameter</a></div>
	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
	</div>
</form>
</body>
</html>
