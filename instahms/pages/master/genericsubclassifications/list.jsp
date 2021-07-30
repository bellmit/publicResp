<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.URLRoute"%>
<c:set var="pagePath" value="<%=URLRoute.GENERIC_SUB_CLASSIFICATION_PATH %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<head>
	<title>Generic Classification List - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp" />
	<script>
		var toolbar = {
		View: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: '${pagePath}/show.htm?',
				onclick: null,
				description: "View/Edit Sub Classification Details"
			}
		};
		var theForm = document.subclassListSearchForm;
		function init() {
			theForm = document.subclassListSearchForm;
			theForm.sub_classification_name.focus();
			createToolbar(toolbar);
		}
	</script>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="subclassList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty subclassList ? true : false}"/>
<body onload="init(); ">
	<h1>Generic Sub Classification List</h1>
	<insta:feedback-panel/>
	<form name="subclassListSearchForm" method="GET">
		<input type="hidden" name="_method" value="list">
		<input type="hidden" name="_searchMethod" value="list"/>
	
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
	
		<insta:search-lessoptions form="subclassListSearchForm" >
		  <div class="searchBasicOpts" >
		    <div class="sboField">
				<div class="sboFieldLabel">Classification</div>
					<div class="sboFieldInput">
						<insta:selectdb name="classification_id" value="${param.classification_id}" table="generic_classification_master"
							valuecol="classification_id"	displaycol="classification_name" dummyvalue="All" filtered="false" />
						<input type="hidden" name="classification_id@cast" value="y"/>
					</div>
				</div>
				<div class="sboField">
				<div class="sboFieldLabel">Sub Classification</div>
					<div class="sboFieldInput">
						<input type="text" name="sub_classification_name" value="${ifn:cleanHtmlAttribute(param.sub_classification_name)}"/>
						<input type="hidden" name="sub_classification_name@op" value="ilike"/>
					</div>
		    	</div>
		  </div>
		</insta:search-lessoptions>
	
		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	
		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
				    <insta:sortablecolumn name="sub_classification_name" title="Sub Classification"/>
				</tr>
	            <c:forEach var="subclassi" items="${subclassList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{sub_classification_id:'${subclassi.sub_classification_id }',classification_id:'${subclassi.classification_id}'},
							[true]);"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
						<td><c:out value="${subclassi.sub_classification_name}"/></td>
					</tr>
				</c:forEach>
			</table>
			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>
	    </div>
	    <div class="screenActions">
			<a href="${cpath}${pagePath}/add.htm">Add New Sub Classification Name</a>
		</div>
	</form>
</body>
</html>