<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<%--
Anupama RC :There should be a @page directive at the top of the page
--%>
<head>
<insta:js-bundle prefix="dialysismodule.commonvalidations"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.dialysismodule.commonvalidations.toolbar");
		var contextPath = '<%=request.getContextPath()%>';
	</script>
	<title><insta:ltext key="patient.dialysis.prescriptions.screen"/></title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:link type="js" file="ajax.js" />
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="js" file="dialysis/prescriptions.js" />
	<insta:link type="js" file="dashboardsearch.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
<%--
Anupama RC : This  meta tag should appear soon after the title tag and before all javascript is included
--%>

</head>
<c:set var="prescList" value="${pagedList.dtoList}"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="hasResults" value="${not empty prescList}"/>
<%--
Anupama RC : You have repeated the below line, it is already there above. It is very important to make sure that
you do not make any changes to the code in any way, except the label / text changes.
It is VERY IMPORTANT to do a diff before you commit your changes and make sure that only intended changes are
made and no editing erros are committed.
--%>
<c:set var="prescList" value="${pagedList.dtoList}"/>
<c:set var="title">
 <insta:ltext key="patient.dialysis.prescriptions.prsec.date"/>
</c:set>
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="pendingOptions">
<insta:ltext key="patient.dialysis.prescriptions.pending"/>,
<insta:ltext key="patient.dialysis.prescriptions.active"/>,
<insta:ltext key="patient.dialysis.prescriptions.inactive"/>
</c:set>

<%--
Anupama RC : unused stuff should be removed
--%>
<%--
<insta:ltext key="patient.dialysis.prescriptions.pageHeader"/>,
<insta:ltext key="patient.dialysis.prescriptions.pageHeader"/>,
<insta:ltext key="patient.dialysis.prescriptions.pageHeader"/></c:set>--%>
<body  onload="initPresList();" class="yui-skin-sam">
<div class="pageHeader"><insta:ltext key="patient.dialysis.prescriptions.pageHeader"/></div>
<insta:feedback-panel/>
<form name="prescriptionsListForm" method="get" >
<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
	<insta:search form="prescriptionsListForm" optionsId="optionalFilter" closed="${hasResults}" >
		<div class="searchBasicOpts" >
			<div class="sboField">
				<%--
				Anupama RC : the key name should indicate its purpose. sboField is a style and not a field
				You should typically name this field as ptient.dialysis.prescription.mr_no and not sbofield
				--%>
				<div class="sboFieldLabel"><insta:ltext key="patient.dialysis.prescriptions.sbofield"/></div>
				<div class="sboFieldInput">
					<div id="mrnoAutoComplete">
						<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
						<div id="mrnoContainer" style="width: 300px"></div>
					</div>
				</div>
			</div>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable" >
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="patient.dialysis.prescriptions.doctor"/></div>
						<div class="sfField">
							<%-- Anupama RC : the dummyvalue has been removed.
							--%>
							<insta:selectdb name="doctor_id" table="doctors" valuecol="doctor_id"
									displaycol="doctor_name" filtered="true" value="${paramValues.doctor_id}" dummyvalue="${dummyvalue}"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="patient.dialysis.prescriptions.prescription.date"/></div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="patient.dialysis.prescriptions.from"/></div>
							<insta:datewidget name="presc_date" id="presc_date0" value="${paramValues.start_date[0]}"/>
							</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="patient.dialysis.prescriptions.to"/></div>
							<insta:datewidget name="presc_date" id="presc_date1" value="${paramValues.start_date[1]}"/>
							<%-- NOTE: tell the query-builder to use >= and <= operators for the dates --%>
							<input type="hidden" name="presc_date@op" value="ge,le"/>
						</div>
					</td>
					<td class="last">
						<div class="sfLabel"><insta:ltext key="patient.dialysis.prescriptions.status"/></div>
						<div class="sfField">
							<insta:checkgroup name="status" selValues="${paramValues.status}"
								opvalues="Active,Pending,Inactive" optexts="${pendingOptions}"/>
						</div>
					</td>
					<td class="last"></td>
					<td class="last"></td>
				</tr>
			</table>
		</div>
	</insta:search>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<div class="resultList">
		<table class="resultList" cellpadding="0" cellspacing="0" id="resultTable" onmouseover="hideToolBar('');" >
			<tr onmouseover="hideToolBar();">
				<th><insta:ltext key="ui.label.mrno"/></th>
				<th><insta:ltext key="ui.label.patient.name"/></th>
                 <insta:sortablecolumn name="presc_date" title="${title}"/>
				<th><insta:ltext key="patient.dialysis.prescriptions.doctor"/></th>
				<th><insta:ltext key="patient.dialysis.prescriptions.status"/></th>
				<th><insta:ltext key="patient.dialysis.prescriptions.startdate"/></th>
				<th><insta:ltext key="patient.dialysis.prescriptions.enddate"/></th>
				<th><insta:ltext key="patient.dialysis.prescriptions.targetweight"/></th>
				<th><insta:ltext key="patient.dialysis.prescriptions.dialyzer"/></th>

			</tr>

			<c:forEach var="presc" items="${prescList}" varStatus="st">
			<c:set var="flagColor">
				<c:choose>
					<c:when test="${presc.status == 'Pending'}">yellow</c:when>
					<c:when test="${presc.status == 'Inactive'}">grey</c:when>
					<c:otherwise>empty</c:otherwise>
				</c:choose>
			</c:set>
			<tr  class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{mr_no: '${presc.mr_no}', dialysis_presc_id: '${presc.dialysis_presc_id}'},
							[true,null,null,null]);"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
				<td>${presc.mr_no}</td>
				<td>${presc.patient_name}</td>
				<td><fmt:formatDate value="${presc.presc_date}" pattern="dd-MM-yyyy"/></td>
				<td>${presc.doctor_name}</td>
				<td>
					<img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>${presc.status}
				</td>
				<td><fmt:formatDate value="${presc.start_date}" pattern="dd-MM-yyyy"/></td>
				<td><fmt:formatDate value="${presc.end_date}" pattern="dd-MM-yyyy"/></td>
				<td>${presc.target_weight}</td>
				<td>${presc.dialyzer_type_name}</td>
			</tr>
			</c:forEach>
		</table>
		<c:if test="${param._method == 'list'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>
	</div>
	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
		<div class="flagText"> <insta:ltext key="patient.dialysis.prescriptions.pending"/></div>
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText"><insta:ltext key="patient.dialysis.prescriptions.inactive"/></div>
	</div>
	<c:url var="url" value="DialysisPrescriptions.do">
			<c:param name="_method" value="add"/>
			<c:param name="mr_no" value="${param.mr_no}" />
	</c:url>
	<table class="screenActions">
		<tr>
			<c:if test="${not empty param.mr_no}">
				<td ><a href='<c:out value="${url}"/>' ><b><insta:ltext key="patient.dialysis.prescriptions.addnewprescription"/></b></a>|&nbsp;</td>
			</c:if>
			<td><a href="${cpath}/dialysis/auditlog/AuditLogSearch.do?_method=getSearchScreen"><insta:ltext key="patient.dialysis.prescriptions.auditlog"/></a></td>
		</tr>
	</table>
</form>
</body>
</html>
