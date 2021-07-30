<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<html>
	<head>
	<script type="text/javascript">
	//Bug#:	42425
		var dietPresFormName = null ;
		YAHOO.util.Event.onContentReady('content', setFormName);
			function setFormName() {
				dietPresFormName = document.canteenForm;
			}

	</script>
		<insta:link type="css" file="widgets.css"/>
		<insta:link type="script" file="widgets.js"/>
		<insta:link type="script" file="hmsvalidation.js"/>
		<insta:link type="script" file="masters/dietmaster.js"/>
		<insta:link type="js" file="dashboardsearch.js"/>

		<title>Meals Schedule for Canteen Visits- Insta HMS</title>
	</head>
	<body>
	<h1>Meals Schedule for Canteen</h1>
	<insta:feedback-panel/>
	<c:set var="filterclosed" value="${not empty pagedList.dtoList}"></c:set>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<form action="Canteen.do" name="canteenForm">
	<input type="hidden" name="_method" id="_method" value="getMealsToBeDelivered">
	<input type="hidden" name="_searchMethod" value="getMealsToBeDelivered">

	<insta:search-lessoptions form="canteenForm">
		<div class="resultList">
			<c:if test="${genPrefs.map.max_centers_inc_default > 1 }">
				<table class="formtable" style="height: 5em">
					<tr>
						<td class="last">
							<div class="sfLabel">Center:</div>
							<div class="sfField">
								<input type="hidden" name="pr.center_id@type" value="integer"/>
								<select class="dropdown" name="pr.center_id" id="pr.center_id" >
									<option value="0"> All</option>
									<c:forEach items="${centers}" var="center">
										<option value="${center.map.center_id}" ${center.map.center_id == selectedUserCenterId ?'selected':''}>${center.map.center_name}</option>
									</c:forEach>
								</select>
							</div>
						</td>
					</tr>
				</table>
			</c:if>
			<table class="searchFormTable">
				<tr>
					<td>
						<div class=sfLabel>Date:</div>
						<div class="sfField">
							<insta:datewidget  name="dp.meal_date" id="date" value="${param['dp.meal_date']}"/>
							<input type="hidden" name="dp.meal_date@type" = value="date"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Ward:</div>
						<div class="sfField">
							<select name="wn.ward_name" id="wardname" class="dropdown">
								<option value="">All</option>
								<c:forEach items="${wards }" var="ward">
									<option value="${ward.map.ward_no }" ${param['wn.ward_name'] == ward.map.ward_no ? 'selected' : ''}>${ward.map.ward_name }</option>
								</c:forEach>
							</select>
						</div>
					</td>
					<td>
						<div class="sfLabel">Meal Name:</div>
						<div class="sfField">
							<insta:selectdb name="dm.meal_name" dummyvalue="All" table="diet_master"
								valuecol="meal_name" displaycol="meal_name" orderby="meal_name" value="${param['dm.meal_name']}" />
						</div>
					</td>
					<td>
						<div class="sfLabel">Meal Time:</div>
						<div class="sfField">
							<select name="dp.meal_timing" id="mealtime" class="dropdown">
								<option value="">All</option>
								<option value="BF" ${param['dp.meal_timing'] == 'BF' ? 'selected' : ''}>BF</option>
								<option value="Lunch" ${param['dp.meal_timing'] == 'Lunch' ? 'selected' : ''}>Lunch</option>
								<option value="Dinner" ${param['dp.meal_timing'] == 'Dinner' ? 'selected' : ''}>Dinner</option>
								<option value="Spl" ${param['dp.meal_timing'] == 'Spl' ? 'selected' : ''}>Spl</option>
							</select>
							<input type="hidden" name="dp.meal_timing@op" value="ilike"/>
						</div>
					</td>

					<td class="last">
						<div class="sfLabel">Delivered</div>
						<div class="sfField">
							<select name="dp.status" class="dropdown">
								<option value="">All</option>
								<option value="Y" ${param['dp.status'] == 'Y' ? 'selected' : ''}>Yes</option>
								<option value="N" ${param['dp.status'] == 'N' ? 'selected' : ''}>No</option>
							</select>
						</div>
					</td>
				</tr>
			</table>
		</div>
	</insta:search-lessoptions>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<div class="resultList" style="width: auto;">
		<table id="resultTable" class="resultList" cellspacing="0" cellpadding="0" style="empty-cells: show">
			<tr>
				<th>Select</th>
				<th>Ward</th>
				<th>Bed</th>
				<th>Mr No</th>
				<th>Patient Name</th>
				<th>Food Allergies</th>
				<th>Meal</th>
				<th>Meal Date</th>
				<th>Meal Time</th>
				<th>Prescribed By</th>
				<th>Remarks</th>
				<th>Delivered</th>
			</tr>
			<c:forEach var="meal" items="${pagedList.dtoList}" varStatus="status">
				<c:set var="index" value="${status.index+1}"></c:set>
				<tr>
					<td>
						<input type="checkbox" name="_updateMealStatus" id="_updateMealStatus${index}"  ${meal.status =='Y'? 'disabled': '' }>
						<input type="hidden" name="_updateStatus"  id="_updateStatus${index}" value="">
						<input type="hidden" name="_orderedId" id="_orderedId${index}" value="${meal.ordered_id}">
					</td>
					<td>${meal.ward_name}</td>
					<td>${meal.bed_name}</td>
					<td>${meal.mr_no}</td>
					<td>${meal.patient_name}</td>
					<td style="color: red;">${meal.food_allergies}</td>
					<td>${meal.meal_name}</td>
					<td>
						<fmt:formatDate value="${meal.ordered_time}" pattern="dd-MM-yyyy"/>
					</td>
					<td>${meal.meal_timing}</td>
					<td>${meal.doctor_name}</td>
					<td>${meal.special_instructions }</td>
					<td>${meal.status =='Y' ? 'Yes' : 'No'}</td>

				</tr>
			</c:forEach>
		</table>
	</div>

	<insta:noresults hasResults="${filterclosed}"/>
	<table class="screenActions">
		<tr>
			<td>
				<button type="button" name="updateDeliveredTime" accesskey="U" onclick="return checkUpdateFields()">
					<b><u>U</u></b>pdate Delivered Time</button>
			</td>
		</tr>
	</table>

	</form>
</body>
</html>