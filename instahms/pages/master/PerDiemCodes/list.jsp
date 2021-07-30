<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Per Diem Codes</title>
	<insta:link type="js" file="PerDiemCodes/PerDiemCodes.js" />
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="masters/charges_common.js" />
	<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
</head>

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>
<c:set var="perdiemCodeList" value="${pagedList.dtoList}" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<body onload="createToolbar(toolBar); selectAllBedTypes();">

<form name="searchform">
	<h1>Per Diem Codes</h1>
	<insta:feedback-panel />
	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>

	<insta:search form="searchform" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">Per Diem Code</div>
				<div class="sboFieldInput">
				  <insta:selectdb name="per_diem_code" id="per_diem_code" value="${param.per_diem_code}" table="per_diem_codes_master" valuecol="per_diem_code"
							orderby="per_diem_description" displaycol="per_diem_code" dummyvalue="-- Select --" dummyvalueId="" />
				</div>
			</div>
			<div class="sboField">
				<div class="sboFieldLabel">Per Diem Desc.</div>
				<div class="sboFieldInput">
				  <insta:selectdb name="per_diem_description" id="per_diem_description" value="${param.per_diem_description}" table="per_diem_codes_master" valuecol="per_diem_description"
							orderby="per_diem_description" displaycol="per_diem_description" dummyvalue="-- Select --" dummyvalueId="" />
				</div>
			</div>
			<div class="sboField" style="padding-left: 65px">
				<div class="sboFieldLabel">Rate Plan</div>
				<div class="sboFieldInput">
					<insta:selectdb name="org_id" id="org_id" value="${param.org_id}" table="organization_details" valuecol="org_id"
							orderby="org_name" displaycol="org_name" />
				</div>
			</div>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td class="last">
						<div class="sfLabel">Status</div>
						<div class="sfField">
							<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive"
								selValues="${paramValues.status}"/>
								<input type="hidden" name="status@op" value="in" />
						</div>
					</td>
					<td class="last">&nbsp;</td>
					<td class="last">&nbsp;</td>
					<td class="last">&nbsp;</td>
				</tr>
			</table>
		</div>
	</insta:search>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
</form>

<form name="listform" action="dummy.do">
	<input type="hidden" name="_method" value="groupUpdate">
	<input type="hidden" name="orgId" value="">
	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');" >
			<tr>
				<th>Per Diem Code</th>
				<th>Per Diem Code Desc.</th>
				<c:forEach var="bed" items="${bedTypes}">
					<th style="width: 40px; overflow: hidden">${ifn:cleanHtml(fn:substring(bed,0,6))}</th>
				</c:forEach>
			</tr>

			<c:forEach var="perdiemCodeDetails" items="${perdiemCodeList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{per_diem_code: '${perdiemCodeDetails.per_diem_code}', org_id: '${ifn:cleanJavaScript(org_id)}'}, '');" >
					<td>
						${perdiemCodeDetails.per_diem_code}
					</td>
					<td>
						<c:if test="${perdiemCodeDetails.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${perdiemCodeDetails.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						${perdiemCodeDetails.per_diem_description}
					</td>
					<c:forEach var="bed" items="${bedTypes}">
						<td class="number" >${ifn:afmt(charges[perdiemCodeDetails.per_diem_code][bed].map['charge'])}</td>
					</c:forEach>
				</tr>
			</c:forEach>
		</table>
	</div>
</form>

<insta:noresults hasResults="${hasResults}"/>

<c:url var="addUrl" value="PerDiemCodes.do">
	<c:param name="_method" value="add"></c:param>
</c:url>
<table class="screenActions" width="100%">
	<tr>
		<td><a href="<c:out value='${addUrl}'/>">Add New Per Diem</a></td>
	   <td align="right">
			<img src='${cpath}/images/grey_flag.gif'> Inactive
	  	</td>
	</tr>
</table>

</body>
</html>
