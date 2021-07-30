<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page import="com.insta.hms.master.URLRoute"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="pagePath" value="<%=URLRoute.ISSUE_USER_MASTER_PATH %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title><insta:ltext key="sales.issues.user.title"/></title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	
	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
 		
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: '${pagePath}/show.htm?',
				onclick: null,
				description: "View and/or Edit Issue User Details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
			autoIssueUserMaster();
		}
		var map = ${ifn:convertListToJson(lookupListMap)};
		var issueUserList = map['hosp_user_name']
		var rAutoComp;
		function autoIssueUserMaster() {
			var datasource = new YAHOO.util.LocalDataSource({result: issueUserList});
			datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
			datasource.responseSchema = {
				resultsList : "result",
				fields : [  {key : "hosp_user_name"} ]
			};
			var rAutoComp = new YAHOO.widget.AutoComplete('hosp_user_name','hosp_user_name_container', datasource);
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
	<c:set var="issueuser">
	<insta:ltext key="sales.issues.user.header"/>
	</c:set>
	<form  name="IssueUserMasterForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<h1><insta:ltext key="sales.issues.user.title"/></h1>

		<insta:search-lessoptions form="IssueUserMasterForm">
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField" style="height: 70px">
							<div class="sboFieldLabel"><insta:ltext key="sales.issues.user.issueusername"/>: </div>
								<div class="sboFieldInput">
									<input type="text" name="hosp_user_name" id="hosp_user_name" value="${ifn:cleanHtmlAttribute(param.hosp_user_name)}" style = "width:20em" >
									<input type="hidden" name="hosp_user_name@op" value="ico" />
									<div id="hosp_user_name_container" style = "width:20em"></div>		
								</div>
		 			</td>
		 			<td></td>
					<td class="sboField" style="height: 70px">
					<div class="sboField">
						<div class="sboFieldLabel"><insta:ltext key="sales.issues.user.status"/>: </div>
							<div class="sboFieldInput">
								<insta:checkgroup name="status" optexts="Active,Inactive" opvalues="A,I" selValues="${paramValues.status}"/>
						</div>
					</td>
					<td class="sboField"></td>
				</tr>
	 	 	</table>
		</insta:search-lessoptions>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable"  onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="hosp_user_name" title="${issueuser}"/>
					<th><insta:ltext key="sales.issues.user.status"/></th>
				</tr>

				 <c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					 <tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{hosp_user_id: '${record.hosp_user_id}'},'');" 
							 id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td><insta:truncLabel value="${record.hosp_user_name}" length="30"/></td> 
						<td>${record.status == 'I' ?  'Inactive' : 'Active'}</td>
					</tr> 
				</c:forEach> 
			</table>
		</div>

		<table class="screenActions">
	    	<tr>
				<td><c:url var="Url" value="${pagePath}/add.htm"/>
				<a href="${Url}"><insta:ltext key="sales.issues.user.adddetails"/></a>
			</tr>
		</table>

	</form>
	
</body>
</html>



 
