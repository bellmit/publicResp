<%@page import="org.apache.struts.Globals" %>
<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>

<html>
<head>
	<title>${ifn:cleanHtml(screen)} Exclusion-Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="js" file="ajax.js" />
	<insta:link type="css" file="widgets.css" />
	<insta:link type="script" file="widgets.js" />
	<insta:link type="js" file="masters/packmaster.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<style type="text/css">
	.status_A.type_P {background-color: #D9EABB;}
	.status_A.type_T {background-color: #C5D9A3;}
	.status_I.type_P {background-color: #D9EABB; color:grey;}
	.status_I.type_T {background-color: #C5D9A3; color:grey; }

    table.legend { border-collapse: collapse; margin-left: 6px; }
	table.legend td { border: 1px solid grey; padding: 2px 5px;}

	</style>

		<script type="text/javascript">
		function init()
		{
			initOperationsAutocomplete();
		}
		function initOperationsAutocomplete(){
			var datasource = new YAHOO.widget.DS_JSArray(${packages});
			var operationAC = new YAHOO.widget.AutoComplete('package_name','packagenameAcContainer', datasource);
			operationAC.maxResultsDisplayed = 15;
			operationAC.allowBrowserAutocomplete = false;
			operationAC.prehighlightClassName = "yui-ac-prehighlight";
			operationAC.typeAhead = false;
			operationAC.useShadow = false;
			operationAC.minQueryLength = 1;
			operationAC.animVert = false;
			operationAC.autoHighlight = false;
	  	}

	  	function validateAll(){
			document.pkgForm.submit();
		}
		function setApplicable(index) {
			var checkbox = document.getElementById("selectPackage"+index);
			var applicable = document.getElementById("applicable"+index);
			if(checkbox.checked)
				applicable.value = 'false';
			else
				applicable.value = 'true';
		}
	</script>

</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<body onload="init();" class="yui-skin-sam">

	<h1>${ifn:cleanHtml(screen)} Exclusion - ${ifn:cleanHtml(org_name)}</h1>

	<insta:feedback-panel/>

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"/>

	<form action="${cpath}/pages/masters/ratePlan.do" method="GET" name="searchform">
	<input type="hidden" name="_method" value="getExcludeChargesScreen" />
	<input type="hidden" name="_searchMethod" value="getExcludeChargesScreen" />
	<input type="hidden" name="chargeCategory" value="packages"/>
	<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}">
	<input type="hidden" name="org_name" value="${ifn:cleanHtmlAttribute(org_name)}"/>

		<insta:search form="searchform" optionsId="optionalFilter" closed="${hasResults}">

			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Package Name:</div>
					<div class="sboFieldInput">
							<input type="text" id="package_name" name="package_name" value="${ifn:cleanHtmlAttribute(param.package_name)}"/>
									<input type="hidden" name="package_name@op" value="ico" />
										<div id="packagenameAcContainer" style="width: 300px;"></div>
					</div>
				</div>
			</div>

			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table  class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">Service Sub Group:</div>
							<div class="sfField">
								<insta:selectdb id="service_sub_group_id" name="service_sub_group_id" value=""
								table="service_sub_groups" class="dropdown"   dummyvalue="-- Select --"
								valuecol="service_sub_group_id"  displaycol="service_sub_group_name" />
								<input type="hidden" name="service_sub_group_id@type" value="integer" />
							</div>
						</td>
						<td>
							<div class="sfLabel">Status:</div>
							<div class="sfField">
								<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
								<input type="hidden" name="status@op" value="in" />
							</div>
						</td>
						<td>
							<div class="sfLabel">Type:</div>
							<div class="sfField">
								<insta:checkgroup name="package_type" opvalues="i,o,d" optexts="IP,OP,Diag" selValues="${paramValues.package_type}"/>
								<input type="hidden" name="package_type@op" value="in" />
							</div>
						</td>
						<td>
						<div class="sfLabel">State:</div>
						<div class="sfField">
							<insta:checkgroup name="type" opvalues="P,T" optexts="Package,Template" selValues="${paramValues.type}"/>
							<input type="hidden" name="type@op" value="in" />
						</td>
						<td class="last">
							<div class="sfLabel">Charges</div>
							<div class="sfField">
								<insta:checkgroup name="applicable" opvalues="true,false" optexts="Included Only,Excluded Only"
								selValues="${paramValues.applicable}"/>
								<input type="hidden" name="applicable@op" value="in" />
								<input type="hidden" name="applicable@cast" value="y"/>
							</div>
						</td>
					</tr>
				</table>
			</div>
		</insta:search>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	</form>
	<form name="pkgForm" action="${cpath}/pages/masters/ratePlan.do">
		<input type="hidden" name="_method" value="excludeCharges">
		<input type="hidden" name="chargeCategory" value="packages"/>
		<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}">
		<input type="hidden" name="org_name" value="${ifn:cleanHtmlAttribute(org_name)}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr>
					<th style="padding-top: 0px;padding-bottom: 0px;">
					<input type="checkbox" name="allPagePackages"  onclick="selectAllItems('selectPackage',this)"/></th>
					<insta:sortablecolumn name="package_name" title="Package Name"/>
				</tr>
				<c:forEach var="pkg" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}">
						<td>
							<c:set var="selected" value="${pkg.applicable=='false'?'checked':''}"/>
							<input type="checkbox" id="selectPackage${st.index}" name="selectPackage"
								value="${pkg.package_id}" ${selected} onclick="setApplicable(${st.index});">
							<input type="hidden" name="applicable" id="applicable${st.index}" value="${pkg.applicable}"/>
							<input type="hidden" name="category_id" id="category_id${st.index}" value="${pkg.package_id}"/>
						</td>
						<td>
							<c:if test="${pkg.status=='A' && pkg.type=='T' && pkg.applicable eq true}"><img src="${cpath}/images/yellow_flag.gif"></c:if>
							<c:if test="${pkg.status=='A' && pkg.type=='P' && pkg.applicable eq true}"><img src="${cpath}/images/empty_flag.gif"></c:if>
							<c:if test="${pkg.status=='A' && pkg.type=='T' && pkg.applicable eq false}"><img src="${cpath}/images/purple_flag.gif"></c:if>
							<c:if test="${pkg.status=='A' && pkg.type=='P' && pkg.applicable eq false}"><img src="${cpath}/images/purple_flag.gif"></c:if>
							<c:if test="${pkg.status=='I' && pkg.type=='T'}"><img src="${cpath}/images/blue_flag.gif"></c:if>
							<c:if test="${pkg.status=='I' && pkg.type=='P'}"><img src="${cpath}/images/grey_flag.gif"></c:if>
							<c:out value="${pkg.package_name}"/>
						</td>
					</tr>
				</c:forEach>
			</table>
		</div>
		<div class="screenActions" align="left">
			 <input type="button" name="exclude" id="exclude" value="Exclude" onclick="validateAll();"/>|
			<a href="${cpath}${screenURL}${ifn:cleanURL(org_id)}">Edit ${ifn:cleanHtml(screen)}</a>
		</div>
		<div class="legend" style="${hasResults?'display':'none'}">
			<div class="flag"><img src="${cpath}/images/empty_flag.gif"></div>
			<div class="flagText">Active Package</div>
			<div class="flag"><img src="${cpath}/images/yellow_flag.gif"></div>
			<div class="flagText">Active Template</div>
			<div class="flag"><img src="${cpath}/images/grey_flag.gif"></div>
			<div class="flagText">Deactivated Package</div>
			<div class="flag"><img src="${cpath}/images/blue_flag.gif"></div>
			<div class="flagText">Deactivated Template</div>
			<div class="flag"><img src="${cpath}/images/purple_flag.gif"></div>
			<div class="flagText">Excluded</div>
		</div>

</form>
<insta:noresults hasResults="${hasResults}"/>
</body>
</html>
