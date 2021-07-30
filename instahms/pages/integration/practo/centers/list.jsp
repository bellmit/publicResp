<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="dtoList" value="${pagedList.dtoList}"/>

<c:set var="hasResults"
	value="${not empty pagedList.dtoList ? 'true' : 'false'}" />

	
<html>
<head>	
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Centers List - Insta HMS</title>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="integration/centers.js" />
	<script>
	
	var cityStateCountryJSON = ${ifn:cleanJavaScript(cityStateCountryList)};
	function init() {
		var listSize = ${pagedList.totalRecords};
		for(i=0;i<listSize;i++){
			initAutoCity("_city_name_"+i, document.getElementById("_locality_id_"+i), "city_state_country_dropdown_"+i, "_city_id_"+i);	
		}
		
	}

	</script>
</head>
<body onload="init();" class="yui-skin-sam">
	<c:set var="results" value="${not empty pagedList.dtoList}"/>
	<h1>Centers</h1>
	<insta:feedback-panel/>
	<form name="searchform" action="CenterProfileMapping.do">
		<input type="hidden" name="_method" value="showDashboard">
		<insta:search-lessoptions form="searchform" >
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField">
						<div class="sboFieldLabel">Center Name: </div>
						<div class="sboFieldInput">
							<input type="text" name="center_name" value="${ifn:cleanHtmlAttribute(param.center_name)}"/>
						</div>
					</td>
				</tr>
			  </table>
		</insta:search-lessoptions>
	</form>
	
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	
	<c:url var="publishUrl" value="CenterProfileMapping.do">
	<c:param name="_method" value="publish"/>
	<%-- add all the request parameters except sort params as parameters to the search URL --%>
	<c:forEach var="p" items="${param}">
		<c:forEach items="${paramValues[p.key]}" var="value">	<%-- handle multival params --%>
			<c:param name="${p.key}" value="${value}"/>
		</c:forEach>
	</c:forEach>
	</c:url>
	
	<form name="publishForm" action="${publishUrl}" method="POST">
	<div class="resultList" >
		<table width="100%" class="dataTable" cellspacing="0" cellpadding="0" width="100%" id="resultTable">
			<tr>
				<th style="padding-top: 0px; padding-bottom: 0px;"><input
						type="checkbox" name="allPageOperations"
						onclick="return checkOrUncheckAll('_selectCenter', this)" /></th>
				<th>#</th>
				<insta:sortablecolumn name="center_name" title="Center Name"/>
				<th>City</th>
				<th>Locality</th>
				<th>Published Status</th>
			</tr>
			
			<c:forEach items="${dtoList}" var="bean" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}">
					<td><input type="checkbox" name="_selectCenter" 
							value="${bean.map.center_id}" 
							<c:if test="${bean.map.status == 'Y'}">disabled</c:if>></td>
					<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1}</td>
					<td>${bean.map.center_name}</td>
					<td>
						<c:out value="${cityMap[bean.map.city_id]}"/>
						<div id="city_state_country_wrapper" class="autoComplete">
							<input type="text" name="_city_name_${bean.map.center_id}" id="_city_name_${st.index}"/>
							<input type="hidden" name="_city_id_${bean.map.center_id}" id="_city_id_${st.index}" value=""/>
						<div id="city_state_country_dropdown_${st.index}" style="width:250px"></div>
						</div>
						</td>
					<td>
						<select name="_locality_id_${bean.map.center_id}" id="_locality_id_${st.index}" class="dropdown" >
							<option value="">${dummyvalue}</option>
						</select>
					</td>
					<td>${bean.map.status}</td>
				</tr>
			</c:forEach>
		</table>
		<insta:noresults hasResults="${results}"/>
	</div>
	
	<div class="fltL"
			style="width: 50%; margin-top: 5px; display: ${hasResults?'block':'none'}">
			<button type="button" name="Add" accesskey="C" class="button"
				onclick="return checkIsCenterSelected()">Publish</button>
		</div>
		
	</form>
</body>
</html>