<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><insta:ltext key="patient.hospitalroles.list.pagetitle"/></title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<c:set var="userList" value="${pagedList.dtoList}" />
	<c:set var="results" value="${not empty userList}"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/HospitalRolesMaster.do?_method=show',
				onclick: null,
				description: "View and/or Edit Hospital Roles details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
			autoRegionMaster();
		}
		var hospitalRole = <%= request.getAttribute("hospitalRolesList") %>;
		var rAutoComp;
		function autoRegionMaster() {
			var datasource = new YAHOO.util.LocalDataSource({result: hospitalRole});
			datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
			datasource.responseSchema = {
				resultsList : "result",
				fields : [  {key : "HOSP_ROLE_NAME"},{key : "HOSP_ROLE_ID"} ]
			};
			var rAutoComp = new YAHOO.widget.AutoComplete('hosp_role_name','hospitalrolecontainer', datasource);
			rAutoComp.minQueryLength = 0;
		 	rAutoComp.maxResultsDisplayed = 20;
		 	rAutoComp.forceSelection = false ;
		 	rAutoComp.animVert = false;
		 	rAutoComp.resultTypeList = false;
		 	rAutoComp.typeAhead = false;
		 	rAutoComp.allowBroserAutocomplete = false;
		 	rAutoComp.prehighlightClassname = "yui-ac-prehighlight";
			rAutoComp.autoHighlight = true;
			rAutoComp.useShadow = false;
		 	if (rAutoComp._elTextbox.value != '') {
					rAutoComp._bItemSelected = true;
					rAutoComp._sInitInputValue = rAutoComp._elTextbox.value;
			}
		}
	</script>

</head>
<body onload="init()">
	<h1><insta:ltext key="patient.hospitalroles.list.title.hospitalrolesmasterlist"/></h1>
	<insta:feedback-panel/>

	<form method="GET" name="HospitalRolesMasterSearchForm">
		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="HospitalRolesMasterSearchForm">
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField" style="height: 70px">
						<div class="sboField" style="height:69px">
							<div class="sboFieldLabel"><insta:ltext key="patient.hospitalroles.list.hospitalrolename"/></div>
								<div class="sboFieldInput">
									<input type="text" name="hosp_role_name" id="hosp_role_name" value="${ifn:cleanHtmlAttribute(param.hosp_role_name)}" style = "width:32em" >
									<input type="hidden" name="hosp_role_name@op" value="ico" />
									<div id="hospitalrolecontainer" style = "width:32em"></div>
								</div>
						</div>
		 			</td>
		 			<td></td>
					<td class="sboField" style="height: 70px">
					<div class="sboField">
						<div class="sboFieldLabel"><insta:ltext key="patient.hospitalroles.list.status"/>: </div>
							<div class="sboFieldInput">
								<insta:checkgroup name="status" optexts="Active,Inactive" opvalues="A,I" selValues="${paramValues.status}"/>
						</div>
					</td>
					<td></td>
				</tr>
	 	 	</table>
		</insta:search-lessoptions>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="" cellpadding="" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<th></th>
					<insta:sortablecolumn name="hosp_role_name" title="Hospital Role Name"/>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{hosp_role_id: '${record.map.hosp_role_id}'},[!${record.map.hosp_role_id < 0}]);" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td>
							<c:if test="${record.map.status == 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						</td>
						<td>
							<insta:truncLabel value="${record.map.hosp_role_name}" length="30"/>
						</td>
					</tr>
				</c:forEach>
			</table>
		</div>
	<div class="screenActions" style="float: left">
		<a href="HospitalRolesMaster.do?_method=add"><insta:ltext key="patient.hospitalroles.list.addnewhospitalrole"/></a>
	</div>
	<div class="legend" style="display: ${results?'block':'none'}" >
			<div class="flag"><img src="${cpath}/images/grey_flag.gif"/></div>
			<div class="flagText"><insta:ltext key="patient.hospitalroles.list.inactiveroles"/></div>
		</div>

	</form>
</body>
</html>