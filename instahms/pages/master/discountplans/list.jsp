<%@page import="com.insta.hms.master.URLRoute"%>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="pagePath" value="<%=URLRoute.DISCOUNT_PLAN_PATH %>"/>
<c:set var="max_centers" value="${genPrefs[0].max_centers_inc_default}" scope="request"/>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Discount Plan Master List - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<script type="text/javascript">
		var searchListData = {
				autoCompleteFields : ["discount_plan_name"],
				autoCompleteData : <%=request.getAttribute("lookupListMap")%>,
				autoCompleteChooseFields : {
					"discount_plan_name" : [{key:"discount_plan_name"},{key:"discount_plan_id"}]
				},
				toolbar : {
					Edit: {
							title: "View/Edit",
							imageSrc: "icons/Edit.png",
							href: 'master/discountplans/show.htm?',
							onclick: null,
							description: "View and/or Edit Discount Plan Master details"
						},
					CenterApplicability: {
							title: "Center Applicability",
							imageSrc: "icons/Edit.png",
							href: 'master/DiscountPlanCenterAssociation.do?_method=getScreen',
							onclick: null,
							description: "View and/or Edit Center Applicability",
							show:${max_centers > 1}
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
<c:set var="entityName" value="Discount Plan"/>
<c:set var="keyField" value="discount_plan_id"/>
<c:set var="searchFormName" value="DiscountPlanSearchForm"/>
<c:set var="listColumns" value="discount_plan_name:Discount Plan,validity_start:Valid From,validity_end:Valid Upto"/>
<c:set var="sortableColumns" value="discount_plan_name"/>
<c:set var="addEditUrl" value="DiscountPlanMaster"/>

<body onload="initSearchListPage(searchListData);">
	<insta:list-dashboard displayName="${entityName}" searchFormName="${searchFormName}" >
		<insta:search-lessoptions form="${searchFormName}">
			<div class="searchBasicOpts" >
				<insta:search-basic-field fieldName="discount_plan_name" type="a" displayName="Discount Plan"/>
 				<insta:search-basic-field fieldName="status" type="c" opvalues="A,I" optexts="Active, Inactive" displayName="Status" style="height:69px;"/>
			</div>
			<div class="sboFieldLabel">Center:</div>
			<div class="sboFieldInput">
				<select class="dropdown" name="_center_id" id="_center_id">
					<option value="-1" param._center_id eq -1 ? 'selected' : ''> ----All---- </option>
					<c:forEach items="${centers}" var="center">
						<option value="${center.center_id}"
							${param._center_id eq center.center_id ? 'selected' : ''}>${center.center_name}</option>
					</c:forEach>
				</select>
			</div>
				
		</insta:search-lessoptions>
	    <insta:search-result keyField="${keyField}" columns="${listColumns}" sortableColumns="${sortableColumns}" dataList="${pagedList}" showToolbar="Y"/>
		<%-- <insta:list-footer addEditUrl="${addEditUrl}" displayName="${entityName}" legends = "grey_flag.gif:Inactive"/> --%>
	</insta:list-dashboard>
	
	<table style="margin-top: 10px;float: left">
		<tr>
		 <td><a href="${cpath}${pagePath}/add.htm">Add Discount Plan</a></td>
		 <div class="legend" style= "display: block" >
		 	<div class="flag"><img alt="" src="${cpath}/images/grey_flag.gif"></div>
		 	<div class="flagText">Inactive</div>
		 </div>
		</tr>
	</table>
</body>
</html>

<%--
This is required for a search panel that has more options

			<div id="optionalFilter" style="clear: both; display: block;">
				<table class="searchFormTable">
				<tbody>
				<tr>
				<insta:search-more-field fieldName="validity_start" type="dr" displayName="Validity Date"/>
				</tr>
				</tbody>
				</table>
			</div>
--%>
