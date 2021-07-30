<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<title><insta:ltext key="storemgmt.stockadjustmentlist.viewstock.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="stores.mgmt"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.stores.mgmt.toolbar");
	</script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="stores/view_stk_adj.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="stkList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty stkList}"/>
<body onload="init(); showFilterActive(document.StkAdjSearchForm);checkstoreallocation();">
<c:set var="adjustmentno">
<insta:ltext key="storemgmt.stockadjustmentlist.viewstock.adjustmentno"/>
</c:set>
<c:set var="adjustmentdate">
<insta:ltext key="storemgmt.stockadjustmentlist.viewstock.adjustmentdate"/>
</c:set>

<insta:feedback-panel/>
<div id="storecheck" style="display: block;" >
<h1><insta:ltext key="storemgmt.stockadjustmentlist.viewstock.stockadjustmentslist"/></h1>
<form name="StkAdjSearchForm" method="GET">
	<input type="hidden" name="_method" value="getStkAdj">
	<input type="hidden" name="_searchMethod" value="getStkAdj"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="StkAdjSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
	  <div class="searchBasicOpts" >
	  	<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="storemgmt.stockadjustmentlist.viewstock.adjustmentno"/></div>
				<div class="sboFieldInput">
					<input type="text" name="adj_no" value="${ifn:cleanHtmlAttribute(param.adj_no)}" onkeypress="return enterNumOnlyzeroToNine(event);">
					<input type="hidden" name="adj_no@type" value="integer" />
				</div>
	    </div>
	  </div>
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.stockadjustmentlist.viewstock.fromdate"/></div>
						<div class="sfField">
							<insta:datewidget name="adjust_date" id="adjust_date0" value="${paramValues.adjust_date[0]}"/>
					    </div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.stockadjustmentlist.viewstock.todate"/></div>
						<div class="sfField">
							<insta:datewidget name="adjust_date" id="adjust_date1" value="${paramValues.adjust_date[1]}"/>
							<input type="hidden" name="adjust_date@op" value="ge,le"/>
							<input type="hidden" name="adjust_date@cast" value="y"/>
					    </div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="storemgmt.stockadjustmentlist.viewstock.store"/></div>
						<c:choose>
						<c:when test="${roleId == 1 || roleId == 2 || (multiStoreAccess == 'A')}">
						<div class="sfField">
							<c:choose>
								<c:when test="${(dept_id != null)}">
									<insta:selectdb name="store_id" table="stores" valuecol="dept_id" class="dropdown"
									displaycol="dept_name" value="${dept_id}" orderby="dept_name" />
								</c:when>
								<c:otherwise>
									<insta:selectdb name="store_id" table="stores" valuecol="dept_id" class="dropdown"
									displaycol="dept_name" value="${param.store_id}"  orderby="dept_name"/>
								</c:otherwise>
							</c:choose>
						</div>
						</c:when>
				       <c:otherwise>
				       <div class="sboFieldInput">
				        <b>${dept_name}</b>
				        <input type="hidden" name="store_id" id="store_id" value="${dept_id}" />
				      </div>
				      </c:otherwise>
				      </c:choose>
				       <input type="hidden" name="store_id@type"  value="integer" />
				       <input type="hidden" name="store_id@cast" value="y" />
					</td>

				</tr>
		</table>
	  </div>
	</insta:search>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
				<insta:sortablecolumn name="adj_no" title="${adjustmentno}"/>
				<insta:sortablecolumn name="adjust_date" title="${adjustmentdate}"/>
			    <th><insta:ltext key="storemgmt.stockadjustmentlist.viewstock.store"/></th>
			    <th><insta:ltext key="storemgmt.stockadjustmentlist.viewstock.user"/></th>
			</tr>

			<c:forEach var="adj" items="${stkList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{adjNo: '${adj.adj_no}'},
						[true]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>

					<td>${adj.adj_no}</td>
					<td><fmt:formatDate value="${adj.adjust_date}" pattern="dd-MM-yyyy HH:mm:ss"/></td>
					<td>${adj.dept_name}</td>
					<td>${adj.username}</td>
				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'stkList'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div>


</form>
</div>
<script>
//var item_list = ${item_list};
var deptId = '${ifn:cleanJavaScript(dept_id)}';
var gRoleId = '${ifn:cleanJavaScript(roleId)}';
</script>
</body>
</html>