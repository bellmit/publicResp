<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page import="com.insta.hms.master.URLRoute" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.SPONSOR_TYPE_PATH %>"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Sponsor Type Master- Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
 /*
  *R.C:Java script in seperate js file.
  */
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: '${pagePath}/show.htm?_method=show',
				onclick: null,
				description: "View and/or Edit Sponsor Type Details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
			autoSponsorMaster();
		}
		var sponsorList = ${ifn:convertListToJson(sponsortypeList)};
		console.log(sponsorList);
		var rAutoComp;
		function autoSponsorMaster() {
			var datasource = new YAHOO.util.LocalDataSource({result: sponsorList});
			datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
			datasource.responseSchema = {
				resultsList : "result",
				fields : [  {key : "sponsor_type_name"},{key : "sponsor_type_id"} ]
			};
			var rAutoComp = new YAHOO.widget.AutoComplete('sponsor_type_name','sponsorcontainer', datasource);
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
	<form  name="sponsorMasterSearchForm" action="" method="get">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<h1>Sponsor Type Master</h1>

		<insta:search-lessoptions form="sponsorMasterSearchForm">
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField" style="height: 70px">
						<div class="sboField" style="height:69px">
							<div class="sboFieldLabel">Sponsor Type Name: </div>
								<div class="sboFieldInput">
									<input type="text" name="sponsor_type_name" id="sponsor_type_name" value="${ifn:cleanHtmlAttribute(param.sponsor_type_name)}" style = "width:20em" >
									<input type="hidden" name="sponsor_type_name@op" value="ico" />
									<div id="sponsorcontainer" style = "width:20em"></div>
								</div>
						</div>
		 			</td>
		 			<td></td>
					<td class="sboField" style="height: 70px">
					<div class="sboField">
						<div class="sboFieldLabel">Status: </div>
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
					<insta:sortablecolumn name="sponsor_type_name" title="Sponsor Type"/>
					<th>Status</th>
				</tr>

				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{sponsor_type_id: '${record.sponsor_type_id}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td><insta:truncLabel value="${record.sponsor_type_name}" length="30"/></td>
						<td>${record.status == 'A' ? 'Active' : 'Inactive'}</td>
					</tr>
				</c:forEach>
			</table>
		</div>

		<c:url var="url" value="${pagePath}/add.htm">
				<c:param name="_method" value="add"/>
		</c:url>
		<div class="screenActions" style="float:left"><a href="<c:out value='${url}'/>">Add Sponsor Type</a></div>

	</form>
</body>
</html>


