<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>

<html>
<head>
	<title>${ifn:cleanHtml(screen)} Exclusion - Insta HMS</title>
	<insta:link type="script" file="hmsvalidation.js" />
	<insta:link type="css" file="widgets.css" />
	<insta:link type="script" file="widgets.js" />
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="js" file="masters/addAnaesthesiatype.js" />
	<insta:link type="js" file="masters/charges_common.js" />


	<script>
		function validateAll(){
			document.anesthesiaForm.submit();
		}

		function setApplicable(index) {
			var checkbox = document.getElementById("selectAnesthesia"+index);
			var applicable = document.getElementById("applicable"+index);
			if(checkbox.checked)
				applicable.value = 'false';
			else
				applicable.value = 'true';
		}
	</script>

</head>

<c:set var="hasResults" value="${not empty pagedList.dtoList}" />
<c:set var="anaesthesiaList" value="${pagedList.dtoList}" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<body onload="initAnaesthesiaAc();"class="yui-skin-sam">

<div class="pageHeader">${ifn:cleanHtml(screen)} Exclusion - ${ifn:cleanHtml(org_name)}</div>
<insta:feedback-panel/>

<form action="${cpath}/pages/masters/ratePlan.do" method="GET" name="searchform">
	<input type="hidden" name="_method" value="getExcludeChargesScreen" />
	<input type="hidden" name="_searchMethod" value="getExcludeChargesScreen" />
	<input type="hidden" name="chargeCategory" value="anesthesia"/>
	<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}"/>
	<input type="hidden" name="org_name" value="${ifn:cleanHtmlAttribute(org_name)}"/>

	<insta:search form="searchform" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts">
			<div class="sboField">
				<div class="sboFieldLabel">Anaesthesia Type Name</div>
				<div class="sboFieldInput">
					<input type="text" id="anesthesia_type_name" name="anesthesia_type_name" value="${ifn:cleanHtmlAttribute(param.anesthesia_type_name)}" style="width: 140px"/>
					<input type="hidden" name="anesthesia_type_name@op" value="ilike" />
						<div id="anaesthesiaContainer"></div>
				</div>
			</div>
			<div class="sboField">
				<div class="sboFieldLabel">Service Sub Group</div>
				<div class="sboFieldInput">
						<insta:selectdb id="service_sub_group_id" name="service_sub_group_id" value=""
						table="service_sub_groups" class="dropdown"   dummyvalue="-- Select --"
						valuecol="service_sub_group_id"  displaycol="service_sub_group_name" />
				</div>
			</div>
			<input type="hidden" name="service_sub_group_id@type" value="integer" />

		</div>
		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table  class="searchFormTable">
					<tr>
						<td>
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

<form name="anesthesiaForm" action="${cpath}/pages/masters/ratePlan.do">
	<input type="hidden" name="_method" value="excludeCharges">
	<input type="hidden" name="chargeCategory" value="anesthesia"/>
	<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}">
	<input type="hidden" name="org_name" value="${ifn:cleanHtmlAttribute(org_name)}"/>
	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');" >
			<tr onmouseover="hideToolBar();">
				<th style="padding-top: 0px;padding-bottom: 0px;">
					<input type="checkbox" name="allPageAnaesthesiaTypes" onclick="selectAllItems('selectAnesthesia',this)"/>
				</th>
				<insta:sortablecolumn name="anesthesia_type_name" title="Anaesthesia Type Name"/>
				<th>Service Sub Group</th>
			</tr>
			<c:forEach var="anesthesia" items="${anaesthesiaList}" varStatus="st">

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}">
					<td>
						<c:set var="selected" value="${anesthesia.applicable=='false'?'checked':''}"/>
						<input type="checkbox" id="selectAnesthesia${st.index}" name="selectAnesthesia"
							value="${anesthesia.anesthesia_type_id}" ${selected} onclick="setApplicable(${st.index});">
						<input type="hidden" name="applicable" id="applicable${st.index}" value="${anesthesia.applicable}"/>
						<input type="hidden" name="category_id" id="category_id${st.index}" value="${anesthesia.anesthesia_type_id}"/>
					</td>
					<td>
						<c:if test="${anesthesia.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${anesthesia.status eq 'A' && anesthesia.applicable eq true}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						<c:if test="${anesthesia.status eq 'A' && anesthesia.applicable eq false}"><img src='${cpath}/images/purple_flag.gif'></c:if>
						${anesthesia.anesthesia_type_name}
					</td>
					<td>${anesthesia.service_sub_group_name}</td>
				</tr>
			</c:forEach>
		</table>
	</div>
	<div class="screenActions" align="left">
		 <input type="button" name="exclude" id="exclude" value="Exclude" onclick="validateAll();"/>|
		<a href="${cpath}${screenURL}${ifn:cleanURL(org_id)}">Edit ${ifn:cleanHtml(screen)}</a>
		<div class="legend" style="display: ${hasResults ? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/purple_flag.gif'></div>
			<div class="flagText">Excluded</div>
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>
	</div>
</form>

<insta:noresults hasResults="${hasResults}"/>
<script>
	var anaesthesiaNames = ${namesJSON};
	var cpath = '${cpath}';
</script>
</body>
</html>

