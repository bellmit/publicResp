<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="currType"><fmt:message key="currencyType"/> </c:set>

<html>
	<head>
		<title>Dietary Master List - Insta HMS</title>
		<insta:link type="css" file="widgets.css"/>
		<insta:link type="script" file="widgets.js"/>
		<insta:link type="script" file="hmsvalidation.js"/>
		<insta:link type="script" file="masters/dietmaster.js"/>
		<insta:link type="js" file="dashboardsearch.js"/>
		<script type="text/javascript">
			var mealNameAndCharges = ${mealNameAndCharges};
			var screentype = '${screentype}';
			var totalRecords = ${pagedList.totalRecords};
			var dietPresFormName = null ;
			YAHOO.util.Event.onContentReady('content', setFormName);
			function setFormName() {
				dietPresFormName = document.searchform;
			}

		</script>
		<style type="text/css">
			.status_I{background-color: #E4C89C}
		</style>
	</head>
	<body onload="initMealAutoComplete();selectAllBedTypes();">
	<c:set var="filterclosed" value="${not empty pagedList.dtoList}"></c:set>
		<form  name="searchform" method="get">
			<h1>Dietary Master</h1>
			<insta:feedback-panel/>
			<input type="hidden" name="_method" value="list">
			<input type="hidden" name="_searchMethod" value="list">

			<input type="hidden" name="orgID" id="orgID">
			<input type="hidden" name="percentage">

			<insta:search form="searchform" optionsId="optionalFilter" closed="${filterclosed}">
				<div class="searchBasicOpts" >
					<div class="sboField">
						<div class="sboFieldLabel">Meal Name:</div>
						<div class="sboFieldInput">
							<div id="mealname" class="autocomplete" style="width: 15em;">
								<input type="text" name="searchName" id="searchName" class="text-input" style="width: 15em;">
								<input type="hidden" id="mealid" name="mealid">
								<div id="mealnamecontainer" class="scrolForContainer"></div>
							</div>
						</div>
					</div>
					<div class="sboField">
						<div class="sboFieldLabel" style="padding-left:20px;">Rate Sheet:</div>
						<div class="sboFieldInput" style="padding-left:20px;">
							<insta:selectdb  name="org_id" value="${org_id}"
								table="organization_details" valuecol="org_id"
								displaycol="org_name" orderby="org_name" onchange="getRatePlanCharges(this,'list')"
								filtered="true" filtercol="status,is_rate_sheet" filtervalue="A,Y"/>
						</div>
					</div>
				</div>

				<div id="optionalFilter" style="clear: both; display: ${filterclosed ? 'none' : 'block'}" >
					<table class="searchFormTable">
						<tr>
							<td>
								<div class="sfLabel">Diet Category:</div>
								<div class="sfField">
									<select name="dietCategory" class="dropdown">
										<option value="">....Select....</option>
										 	<c:forEach var="dietCategory" items="${categoryList}">
										 		<option value="${dietCategory.map.diet_category}">${dietCategory.map.diet_category}</option>
										 	</c:forEach>
									</select>
								</div>
							</td>
							<td>
								<div class="sfLabel">Service Sub Group:</div>
								<div class="sfField">
									<insta:selectdb id="service_sub_group_id" name="service_sub_group_id" value=""
										table="service_sub_groups" class="dropdown"   dummyvalue="-- Select --"
										valuecol="service_sub_group_id"  displaycol="service_sub_group_name" />
								</div>
							</td>
							<td class="last">
								<div class="sfLabel">Status:</div>
								<div class="sfField">
									<insta:checkgroup name="searchStatus" selValues="${paramValues.status}"
										opvalues="A,I" optexts="Active,Inactive"/>
								</div>
							</td>
							<td class="last">&nbsp;</td>
							<td class="last">&nbsp;</td>
							<td class="last">&nbsp;</td>
						</tr>
					</table>
				</div>
			</insta:search>
			<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
			<div class="resultList">
				<table  class="resultList" id="mealsTable" cellspacing="0" cellpadding="0" >
					<tr>
						<th>Select </th>
						<th>Meal Name</th>
						<th>Diet Category</th>
						<th>Diet Type</th>
						<c:forEach var="bedTypes" items="${bedTypes}">
							<th>${ifn:cleanHtml(bedTypes)}</th>
						</c:forEach>
					</tr>
					<c:forEach var="dietID" items="${pagedList.dtoList}" varStatus="status">
						<c:set var="index" value="${status.index+1}"/>
					<tr class="${status.index == 0 ? 'firstRow' : ''} ${status.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${status.index}, event, 'mealsTable',
							{diet_id: '${dietID.map.diet_id}', org_id: '${ifn:cleanJavaScript(org_id)}', organization: '${ifn:cleanJavaScript(org_id)}'});"
						onmouseover="hideToolBar(${status.index})" id="toolbarRow${status.index}">
					<td><input type="checkbox" name="dietID" id="dietID${index}" value="${dietID.map.diet_id}"></td>
					<td>${dietID.map.meal_name}</td>
					<td>${dietID.map.diet_category}</td>
					<td>${dietID.map.diet_type}</td>
						<c:forEach var="bedTypes" items="${bedTypes}">
							<c:forEach var="bedAndCharge" items="${bedTypeAndChargeList}">
								<c:if test="${not empty bedAndCharge[dietID.map.meal_name][bedTypes].map.charge}">
									<td class="number">${bedAndCharge[dietID.map.meal_name][bedTypes].map.charge}</td>
								</c:if>
							</c:forEach>
						</c:forEach>
						</tr>
					</c:forEach>
				</table>
			</div>
			<insta:noresults hasResults="${filterclosed}"/>
			<div class="screenActions">
				<c:url var="addDietUrl" value="DietaryMaster.do">
					<c:param name="_method" value="add"/>
					<c:param name="org_id" value="${org_id}"></c:param>
				</c:url>
				<a href="<c:out value='${addDietUrl}' />" title="Add New Diet">Add New Diet</a>
			</div>
			<table class="dataTable" width="100%">
				<tr>
					<th colspan="2" style="padding-top: 0px;padding-bottom: 0px">Group Update/  Selected Diets
						<input type="checkbox" name="updateSelectedRecords" id="updateSelectedRecords" onclick="selectAll();">
						&nbsp;&nbsp;
						Select all ${pagedList.totalRecords} meals
						<input type="checkbox" name="updateAllMeals" id="updateAllMeals"  onclick="selectAll();">
					</th>
				</tr>
				<tr>
					<td>
						Bed types
						<input type="checkbox" name="allBedTypes" id="allBedTypes" checked="checked" onclick="selectAllBedTypes()">All
					</td>
					<td>
						<input type="radio" name="updateTable" value="UPDATECHARGE">Update Charge <br/>
						<input type="radio" name="updateTable" value="UPDATEDISCOUNT">Update Discount <br/>
						<input type="radio" name="updateTable" value="DISCOUNTCHARGE">Apply Discount On Charge <br/>
					</td>
				</tr>
				<tr>
					<td>
						<select name="groupBedType" id="groupBedType" size="5" multiple="multiple" onclick="">
							<c:forEach items="${bedTypes}" var="bedType" >
								<option value="${ifn:cleanHtml(bedType)}">${ifn:cleanHtml(bedType)}</option>
							</c:forEach>
						</select>
					</td>
					<td>
						<insta:selectoptions name="varianceType" optexts="Increase By,Decrease By" opvalues="+,-" value="" style="width: 8em;"/>
						<input type="text" name=varianceBy id= "varianceBy" size="5"> %
						<input type="text" name="varianceValue" id="varianceValue" size="5"> ${currType}
						&nbsp;&nbsp;&nbsp;Round off:
						<select name="round" style="width: 5em" class="dropdown">
							<option value="0">None</option>
							<option value="1">1</option>
							<option value="5">5</option>
							<option value="10">10</option>
							<option value="25">25</option>
							<option value="50">50</option>
							<option value="100">100</option>
						</select>
					</td>
				</tr>
				<tr>
					<td colspan="2">
						<button type="button" name="groupUpdate" accesskey="U" onclick="ValidateGropUpdate()">
						 <b><u>U</u></b>pdate Charges</button>
					</td>
				</tr>
			</table>
		</form>
	</body>
</html>