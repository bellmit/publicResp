<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="pagePath" value="<%=URLRoute.ITEM_SUB_GROUP_PATH %>"/>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Tax Sub Groups List - Insta HMS</title>

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
			description: "View and/or Edit Tax sub Group details"
			}
	};

		function init()
		{
			createToolbar(toolbar);
		}

	</script>

</head>
<c:set var="entityName" value="Tax Sub Groups"/>
<c:set var="keyField" value="item_subgroup_id"/>
<c:set var="searchFormName" value="ItemSubGroupSearchForm"/>
<c:set var="listColumns" value="item_group_name: Tax Group,item_subgroup_name:Tax Sub Group ,validity_start:Validity Start,validity_end:Validity End,item_subgroup_display_order:Display Order,tax_rate:Tax Rate"/>
<c:set var="sortableColumns" value="item_group_name,item_subgroup_name,item_subgroup_display_order"/>
<%-- <c:set var="addEditUrl" value="ItemGroupMaster"/> --%>

<body onload="init()">
	<insta:list-dashboard displayName="${entityName}" searchFormName="${searchFormName}" >
		<insta:search-lessoptions form="${searchFormName}">
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Tax Group:</div>
					<div class="sboFieldInput">
						<insta:selectdb name="item_group_name"  table="item_groups" valuecol="item_group_name"
							displaycol="item_group_name" status="A"
							value="${param.item_group_name}"  orderby="item_group_name" dummyvalue="-- Select --" dummyvalueId="" />
					</div>
				</div>
				<div class="sboField" style="height:75">
					<div class="sboFieldInput">
						<insta:search-basic-field fieldName="item_subgroup_name" type="a" displayName="Tax Sub Group"/>
					</div>
				</div>
				<div class="sboField" style="height:90">
					<div class="sboFieldInput">
						<insta:search-basic-field fieldName="status" type="c" opvalues="A,I" optexts="Active, Inactive" displayName="Status" style="height:69px;"/>
					</div>
				</div>
				
			</div>
		
		</insta:search-lessoptions>
	    <insta:search-result keyField="${keyField}" columns="${listColumns}" sortableColumns="${sortableColumns}" dataList="${pagedList}" showToolbar="Y"/>
		<%-- <insta:list-footer addEditUrl="${addEditUrl}" displayName="${entityName}" legends = "grey_flag.gif:Inactive"/> --%>
	</insta:list-dashboard>
	
	<table style="margin-top: 10px;float: left">
		<tr>
		 <td><a href="${cpath}${pagePath}/add.htm">Add Tax Sub Group</a></td>
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


