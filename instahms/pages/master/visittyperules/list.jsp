<%@page import="com.insta.hms.master.URLRoute"%>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="pagePath" value="<%=URLRoute.OP_VISIT_TYPE_RULES %>"/>
<c:set var="max_centers" value="${genPrefs[0].max_centers_inc_default}" scope="request"/>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Advanced Followup Rules - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<script type="text/javascript">
		var searchListData = {
				autoCompleteFields : ["rule_name"],
				autoCompleteData : <%=request.getAttribute("lookupListMap")%>,
				autoCompleteChooseFields : {
					"rule_name" : [{key:"rule_name"},{key:"rule_id"}]
				},
				toolbar : {
					Edit: {
							title: "View/Edit",
							imageSrc: "icons/Edit.png",
							href: 'master/visittyperules/show.htm?',
							onclick: null,
							description: "View and/or Edit Op Visit Type Rules details"
						}
				}
		}

		function initSearchListPage(searchListData)
		{
			var fields = (null != searchListData) ? searchListData.autoCompleteFields : [];
			var dataMap = (null != searchListData) ? searchListData.autoCompleteData : null;
			var toolbar = (null != searchListData) ? searchListData.toolbar : null;
			for (var i = 0; i < fields.length; i++) {
				var chooseFields = searchListData.autoCompleteChooseFields[fields[i]];
				var autoComplete = initLocalAutoComplete(fields[i], dataMap, chooseFields);
			}
			if (null != toolbar) {
				createToolbar(toolbar);
			}
		}

	</script>

</head>
<c:set var="entityName" value="Advanced Followup Rules"/>
<c:set var="keyField" value="rule_id"/>
<c:set var="searchFormName" value="SearchForm"/>
<c:set var="listColumns" value="rule_name:Rule Name"/>
<c:set var="sortableColumns" value="rule_name"/>

<body onload="initSearchListPage(searchListData);">
	<insta:list-dashboard displayName="${entityName}" searchFormName="${searchFormName}" >
		<insta:search-lessoptions form="${searchFormName}">
			<div class="searchBasicOpts" >
				<insta:search-basic-field fieldName="rule_name" type="a" displayName="Rule"/>
			</div>
		</insta:search-lessoptions>
	    <insta:search-result keyField="${keyField}" columns="${listColumns}" sortableColumns="${sortableColumns}" dataList="${pagedList}" showToolbar="Y"/>
	</insta:list-dashboard>
	
	<table style="margin-top: 10px;float: left">
		<tr>
		 <td><a href="${cpath}${pagePath}/add.htm">Add New Rule&nbsp;|</a>
			<a href="${cpath}/master/followuprulesapplicability/list.htm">Applicability</a></td>
		</tr>
	</table>
</body>
</html>

