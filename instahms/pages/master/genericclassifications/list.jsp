<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.URLRoute"%>
<html>
<c:set var="pagePath" value="<%=URLRoute.GENERIC_CLASSIFICATION_PATH %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<head>
	<title>Generic Classification List - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp" />

<script>
	var pagePath = '${pagePath}';
	var cPath = '${cpath}';
	var toolbar = {
		View: {
			title: "View/Edit",
			imageSrc: "icons/Edit.png",
			href: '${pagePath}/show.htm?',
			onclick: null,
			description: "View/Edit Generic Classification Details"
		}
	};

	var theForm = document.genclassListSearchForm;
	
	function init() {
		theForm = document.genclassListSearchForm;
		theForm.classification_name.focus();
		createToolbar(toolbar);
	}
	
	function deleteClassification() {
		theForm = document.genclassListSearchForm;
		var check = false;
		var delEls = document.getElementsByName("del_classid");
	 	for (var i=0; i<delEls.length; i++)  {
			if (delEls[i].checked == true) {
	 			check = true;
	 			break;
	 		}
	 	}
		if (!check) {
			alert("Please select the classification names to delete");
			return false;
		}
		theForm.action = cPath+pagePath+"/delete.htm";
		theForm.submit();
	}
</script>
</head>
<c:set var="genclassList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty genclassList ? true : false}"/>
<body onload="init();">

<h1>Generic Classification List</h1>

<insta:feedback-panel/>

<form name="genclassListSearchForm" method="GET" action="">
	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search-lessoptions form="genclassListSearchForm" >
	  <div class="searchBasicOpts" >
	  	<div class="sboField">
			<div class="sboFieldLabel">Classification</div>
				<div class="sboFieldInput">
					<input type="text" name="classification_name" value="${ifn:cleanHtmlAttribute(param.classification_name)}"/>
					<input type="hidden" name="classification_name@op" value="ilike"/>
				</div>
	    	</div>
	  	</div>
	  </div>
	</insta:search-lessoptions>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
			    <th>Delete</th>
			    <insta:sortablecolumn name="classification_name" title="Classification"/>
			</tr>
            <c:forEach var="classi" items="${genclassList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{classification_id:'${classi.classification_id }'},
						[true]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<td><input type="checkbox" name="del_classid" id="del_classid" value="${classi.classification_id}" /></td>
					<td><c:out value="${classi.classification_name}"/></td>
				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'getGenericClassificationDashBoard'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div>
   <div class="screenActions">
     	<button type="button" accesskey="D" name="Delete" class="button" onclick="return deleteClassification()" >
     	<b><u>D</u></b>elete</button> |
  		<a href="${cpath}${pagePath}/add.htm">Add Generic Classification</a>
   </div>

</form>
</body>
</html>