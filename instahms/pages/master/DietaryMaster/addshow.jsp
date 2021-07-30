<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/esapi.tld" prefix="esapi" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<html>
	<head>
		<title>Add New Diet - InstaHMS</title>
		<insta:link type="css" file="widgets.css"/>
		<insta:link type="script" file="widgets.js"/>
		<insta:link type="script" file="hmsvalidation.js"/>
		<insta:link type="script" file="masters/dietmaster.js"/>
		<c:set var="cpath" value="${pageContext.request.contextPath}"/>

		<script type="text/javascript">
			var orginalMealName = '${bean.map.meal_name}';
			var mealName = ${allMealList};

			//Bug#:	42425
			var dietPresFormName = null ;
			YAHOO.util.Event.onContentReady('content', setFormName);
			function setFormName() {
				dietPresFormName = document.dietaryMaster;
			}
			
		 var itemGroupList = ${itemGroupListJson};
		 var itemSubGroupList = ${itemSubGroupListJson};
		</script>
		<style>
			table.delActionTable td.first {
			border-left : 1px #cad6e3 solid;
			}
		</style>
	</head>
	<body onload="calculateTotalCalory();itemsubgroupinit();">
		<form action="DietaryMaster.do" method="get" name="dietaryMaster">
			<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}">
			<input type="hidden" name="_method" id="_method" value="${param._method == 'add'?'create' : 'update'}">
			<input type="hidden" id="serviceSubGroup" value="${bean.map.service_sub_group_id}">
			<c:choose>
				<c:when test="${param._method == 'add'}">
					<input type="hidden" name="recordLength" value="1">
				</c:when>
				<c:otherwise>
					<input type="hidden" name="recordLength" value="${requestScope.recordLength}">
				</c:otherwise>
			</c:choose>

			<input type="hidden" name="totalClorificValue" value="0">
			<h1>${param._method == 'add'?'Add ' : 'Edit  '}Meal</h1>
			<insta:feedback-panel/>
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Meal Details</legend>
				<table  class="formtable" width="100%">
					<tr>
						<td class="formlabel">Meal Name :</td>
						<td>
							<input type="text" name="meal_name" id="meal_name" value="${bean.map.meal_name}"
								class="required validate-length" length="100" onblur="capWords(meal_name)" title="Meal name is required">
							<input  type="hidden" name="diet_id" id="diet_id" value="${bean.map.diet_id}">
						</td>

						<td class="formlabel">Diet Category :</td>
						<td>
							<input type="text" name="diet_category" id="diet_category" value="${bean.map.diet_category}"
								class="required validate-length" length="100" onblur="capWords(diet_category)" title="Diet category name is required">
						</td>

						<td class="formlabel">Diet Type :</td>
						<td>
							<input type="text" name="diet_type" id="diet_type" value="${bean.map.diet_type}"
								class="required validate-length" length="100" onblur="capWords(this)" title="Diet type is required">
						</td>
					</tr>
					<tr>
						<td class="formlabel">Service Group:</td>
						<td>
							<insta:selectdb id="service_group_id" name="service_group_id" value="${groupId}"
								table="service_groups" class="dropdown"   dummyvalue="-- Select --"
								valuecol="service_group_id"  displaycol="service_group_name" onchange="loadServiceSubGroup()" />
						</td>
						<td class="formlabel">Service Sub Group:</td>
						<td>
							<select name="service_sub_group_id" id="service_sub_group_id" class="dropdown">
								<option value="">-- Select --</option>
							</select>
						</td>
						<td class="formlabel">Insurance Category:</td>
						<td>
							<insta:selectdb  name="insurance_category_id" id="insurance_category_id"  value="${insurance_categories}" table="item_insurance_categories" valuecol="insurance_category_id" displaycol="insurance_category_name" filtercol="system_category" filtervalue="N" multiple="true"/>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Remarks :</td>
						<td>

							<input type="text" name="remarks" id="remarks" class="validate-length" length="100" value="${bean.map.remarks}" maxlength="100">

						</td>
						<td class="formlabel">Service Tax(%)</td>
						<td>
							<input type="text" name="serviceTax" id="diet_type" class="number" value="${bean.map.service_tax}" onkeypress="return enterNumOnlyANDdot(event)">
						</td>
						<td class="formlabel">Status :</td>
						<td>
							<insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive"/>
						</td>
					<tr>
						<td class="formlabel">Calorific Value :</td>
						<td>
							<label id="totalCalorificValue" class="forminfo"></label>
							<input type="hidden" id="totalCalorificValue">
						</td>
						<td class="formlabel">Billing Group:</td>
						<td>
							<insta:selectdb  name="billing_group_id" id="billing_group_id"  value="${bean.map.billing_group_id}" table="item_groups" valuecol="item_group_id"
								displaycol="item_group_name" dummyvalue="-- Select --" filtercol="item_group_type_id,status" filtervalue="BILLGRP,A"/>
						</td>
				</tr>
				</table>
			</fieldset>
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Meal Constituents</legend>
				<table class="delActionTable">
					<tr>
						<td>
						<table id="formatTable" class="delActionTable" cellspacing="0" cellpadding="0">
							<tr class="header">
								<td class="first">Constituent</td>
								<td>Quantity</td>
								<td>Units</td>
								<td>Calorific Value</td>
								<td>&nbsp;</td>
							</tr>
							<c:choose>
								<c:when test="${not empty constuientList}">
									<c:forEach var="constituent" items="${constuientList}" varStatus="s">
										<c:set var="i" value="${s.index+1}"/>
										<tr>
											<td class="first">
												<input type="text" name="constituent_name${i}" id="constituent_name${i}"  value="${constituent.CONSTITUENT_NAME}"
													class="required validate-length ${s.last?'':'previousEl'}"
													length="100" onblur="capWords(constituent_name${i}),checkDuplicate(${i})">
												<input type="hidden" name="constituentName${i}" id="constituentName${i}"  value="${constituent.CONSTITUENT_NAME}">
											</td>
											<td>
												<input type="text" name="quantity${i}" id="quantity${i}" onkeypress="return enterNumOnlyzeroToNine(event)" value="${constituent.QUANTITY}"
												 class="${s.last?'':'previousEl'} ${s.first?'first':''}">
											</td>
											<td>
												<input type="text" name="units${i}" id="units${i}" value="${constituent.UNITS}"
												 class="${s.last?'':'previousEl'} ${s.first?'first':''}">
											</td>
											<td>
												<input type="text" name="calorific_value${i}" id="calorific_value${i}" onkeypress="return enterNumOnlyzeroToNine(event)"
												value="${constituent.CALORIFIC_VALUE}" onblur="calculateTotalCalory()"
												 class="${s.last?'':'previousEl'} ${s.first?'first':''}">
											</td>
											<td class="last">
											     <a onclick="changeElsColor(${i});disableRow(${i});" href="javascript:void(0)">
											     <img src="${cpath}/icons/Delete.png" name="delItem${i}" id="delItem${i}" class="imgDelete"/></a>
											     <input type='hidden' id='delete${i}' name="delete${i}" value='N'>
												 <input type="hidden" id="newAdded${i}" name="newAdded${i}" value="N">
											</td>
										</tr>
									</c:forEach>
								</c:when>
								<c:otherwise>
									<tr id="1">
										<td class="first" style="border-left:1px #cad6e3 solid">
											<input type="text" name="constituent_name1" id="constituent_name1"
												class="required validate-length" length="100" onblur = "capWords(constituent_name1)" title="Constituent name is required">
										</td>
										<td>
											<input type="text" name="quantity1" id="quantity1" onkeypress="return enterNumOnlyzeroToNine(event)" value="">
										</td>
										<td>
											<input type="text" name="units1" id="units1" value="">
										</td>
										<td>
											<input type="text" name="calorific_value1" id="calorific_value1" onkeypress="return enterNumOnlyzeroToNine(event)"
												onblur="calculateTotalCalory()">
										</td>
										<td class="last">
										     <a onclick="changeElsColor(1);disableRow(1);" href="javascript:void(0)">
										     <img src="${cpath}/icons/Delete.png" name="delItem1" id="delItem1" class="imgDelete"/></a>
										     <input type='hidden' id='delete1' name='delete1' value='N'>
											 <input type='hidden' id='newAdded1' name='newAdded1' value='Y'>
										</td>
									</tr>
								</c:otherwise>
							</c:choose>
						</table>
					</td>
					<td valign="bottom" style="padding-left:5px">

					<button type="button" name="addresults" Class="imgButton" Id="addresults" onclick="addresultlabels()" >
									<img src="${cpath}/icons/Add.png" align="right"/>
					</button>
					</td>
				</tr>
			</table>
		</fieldset>
	<insta:taxations/>
				<c:url var="dashBoardUrl" value="DietaryMaster.do">
					<c:param name="_method" value="list"></c:param>
					<c:param name="org_id" value="${org_id}"></c:param>
				</c:url>

				<c:url var="editChargeUrl" value="DietaryMaster.do">
					<c:param name="_method" value="editCharges"></c:param>
					<c:param name="organization" value="${org_id}"></c:param>
					<c:param name="diet_id" value="${bean.map.diet_id}"></c:param>
				</c:url>

			<table class="screenActions">
				<tr >
					<td >
						<button type="button" accesskey="S" onclick="return checkFormFields()"><b><u>S</u></b>ave</button>
						<c:if test="${param._method != 'add'}">
							|&nbsp;<a href="#" onclick="window.location.href='${cpath}/dietary/DietaryMaster.do?_method=add'">Add</a>
						</c:if>
						<c:if test="${param._method != 'add'}">
							|&nbsp;<a href="<c:out value='${editChargeUrl}' />" title="Edit the charges">Edit Charges</a>
						</c:if>
						|&nbsp;<a href="<c:out value='${dashBoardUrl}' />" title="Diet Dashboard">Diet Dashboard</a>
					</td>
				</tr>
			</table>
		</form>
		<script type="text/javascript">
			var serviceSubGroupsList = ${serviceSubGroupsList};
		</script>

	</body>
</html>
