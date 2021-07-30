<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="pagePath" value="<%=URLRoute.ITEM_GROUP_PATH %>"/>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Tax Groups List - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	
<script type="text/javascript">
	
	var toolbar = {
		Edit: {
			title: "View/Edit",
			imageSrc: "icons/Edit.png",
			href: '${pagePath}/show.htm?',
			onclick: null,
			description: "View and/or Edit Tax Group details"
			}
	};

	function init()
	{
		createToolbar(toolbar);
	}

		function init()
		{
			createToolbar(toolbar);
		}
</script>

</head>
<c:set var="entityName" value="Tax Groups"/>
<c:set var="keyField" value="item_group_id,item_group_name"/>
<c:set var="searchFormName" value="ItemGroupSearchForm"/>
<c:set var="listColumns" value="item_group_type_name:Tax Group Type,item_group_name:Tax Group,group_code:Tax Group Code,item_group_display_order:Display Order"/>
<c:set var="mappingFields" value="GST:India GST,IGST:India IGST,VAT:GCC VAT,KSACTA:KSA VAT Citizen Taxable,KSACEX:KSA VAT Citizen Exempt" />
<c:set var="sortableColumns" value="item_group_name,item_group_display_order"/>
<c:set var="addEditUrl" value="ItemGroupMaster"/>

<body onload="init()">
	<insta:list-dashboard displayName="${entityName}" searchFormName="${searchFormName}" >
		<insta:search-lessoptions form="${searchFormName}">
		<div class="searchBasicOpts" >
				<div class="sboField" style="height:69">
					<div class="sboFieldInput">
						<insta:search-basic-field fieldName="item_group_name" type="t" displayName="Tax Group"/>
						<input type="hidden" name="item_group_name@op" value="ilike"/>
					</div>
				</div>
				
					<div class="sboField" style="height:90">
					<div class="sboFieldInput">
						<insta:search-basic-field fieldName="status" type="c" opvalues="A,I" optexts="Active, Inactive" displayName="Status" style="height:69px;"/>
					</div>
				</div>
				
			</div>
		</insta:search-lessoptions>
		<insta:tax-search-result keyField="${keyField}"
			columns="${listColumns}" sortableColumns="${sortableColumns}"
			dataList="${pagedList}" showToolbar="Y"
			mappingFieldValue="${mappingFields}"
			mappingFieldName="group_code" />
	</insta:list-dashboard>
	
	<table style="margin-top: 10px;float: left">
		<tr>
		 <td><a href="${cpath}${pagePath}/add.htm">Add Tax Group</a></td>
		 <div class="legend" style= "display: block" >
		  <div class="flag"><img alt="" src="${cpath}/images/empty_flag.gif"></div>
			 <div class="flagText">Active</div>
		 	<div class="flag"><img alt="" src="${cpath}/images/grey_flag.gif"></div>
		 	<div class="flagText">Inactive</div>
		 </div>
		</tr>
	</table>
</body>
</html>
