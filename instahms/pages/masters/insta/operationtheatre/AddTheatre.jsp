<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="script" file="masters/charges_common.js"/>
<insta:link type="css" file="widgets.css" />
<insta:link type="script" file="widgets.js" />

<insta:link type="js" file="masters/addTheatre.js" />
<insta:link type="js" file="ajax.js" />
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<title>Insta HMS</title>
 <script>
   <c:if test="${param._method != 'getNewTheatreScreen'}">
    Insta.masterData=${theatresLists};
   </c:if>
   var multiCenters = ${multiCenters};
   var storesJson = <%= request.getAttribute("storesJson") %>;
   var itemGroupList = ${itemGroupListJson};
   var itemSubGroupList = ${itemSubGroupListJson};
 </script>
</head>
<body onload="keepbackupValue();itemsubgroupinit();">
<c:choose>
   <c:when test="${param._method != 'getNewTheatreScreen'}">
     <h1 style="float:left">Theatre/Room Details </h1>
     <c:url var="searchUrl" value="/pages/masters/insta/operationtheatre/TheatMast.do"/>
     <insta:findbykey keys="theatre_name,theatre_id"  fieldName="theatreId" method="geteditChargeScreen" url="${searchUrl}"
	 extraParamKeys="orgId" extraParamValues="ORG0001"/>
  </c:when>
  <c:otherwise>
     <h1>Theatre/Room Details</h1>
  </c:otherwise>
</c:choose>
<html:form action="/pages/masters/insta/operationtheatre/TheatMast.do">
	<input type="hidden" name="_method" value="${requestScope._method}" />
	<html:hidden property="theatreId" />
	<html:hidden property="pageNum" />
	<html:hidden property="chargeType" />
	<html:hidden property="orgId" />
	<input type="hidden" name="theatreID" value="${ifn:cleanHtmlAttribute(param.theatreId)}"/>
	<input type="hidden" name="orgID" value="${ifn:cleanHtmlAttribute(param.orgId)}"/>
	<input type="hidden" name="chargeTyp" value="${ifn:cleanHtmlAttribute(param.chargeType)}"/>
	<input type="hidden" name="pageNo" value="${ifn:cleanHtmlAttribute(param.pageNum)}"/>
	<input type="hidden" name="contextPath" value="${pageContext.request.contextPath}" />
   <insta:feedback-panel/>

	<fieldset class="fieldSetBorder">
		<table class="formtable">
			<tr>
				<td class="formlabel">Theatre/Room Name:</td>
				<td class="forminfo"><html:text property="theatreName" styleId="theatreName"
					onblur="checkDuplicate();capWords(theatreName);" maxlength="100"/></td>
				<td class="formlabel">Status:</td>
				<td class="frominfo">
					<insta:selectoptions name="status" opvalues="A,I" optexts="Active,Inactive"
					value="${param.status}"/>
				</td>
				<c:if test="${multiCenters}">
					<td class="formlabel">Center:</td>
					<td>
						<c:choose>
							<c:when test="${empty param.theatreId }">
								<select name="centerId" class="dropdown" onchange="loadCenters(this)">
									<option value="">--Select--</option>
									<c:forEach items="${centers}" var="center">
										<option value="${center.map.center_id }"
										<c:if test="${center.map.center_id eq param.center_id }"></c:if>>
										${center.map.center_name }
										</option>
									</c:forEach>
								</select>
							</c:when>
							<c:otherwise>
								<insta:getCenterName center_id="${theaterDetails.map.center_id}"/>
								<input type="hidden" name="centerId" value="${theaterDetails.map.center_id}"/>
							</c:otherwise>
						</c:choose>
					</td>
				</c:if>
			</tr>

			<tr>
				<td class="formlabel">Store:</td>
				<td>
					<select name="storeId" id="storeId" class="dropdown">
						<option value="">--Select--</option>
						<c:forEach items="${stores}" var="store">
							<option value="${store.map.dept_id}" ${store.map.dept_id == (empty param.store_id ? theaterDetails.map.store_id : param.store_id) ? 'selected' : ''}>${store.map.dept_name }</option>
						</c:forEach>
					</select>

				</td>
				<td class="formlabel">Schedulable:</td>
				<td class="forminfo"><html:checkbox property="schedule" onclick="changeCheckboxValues()"></html:checkbox></td>
				<td class="formlabel">Overbook Limit:</td>
				<td class="forminfo"><html:text property="overbook_limit" 
				onkeypress="return enterNumOnlyzeroToNine(event)"></html:text>
				<img class="imgHelpText" title=" Zero - overbook not allowed.
 Empty - infinite.
 Specific number - that many overbooking allowed." src="${cpath}/images/help.png">
				</td>
			</tr>

			<tr>
				<td class="formlabel">Unit Size (minutes):</td>
				<td class="forminfo"><html:text property="unitSize"
					onkeypress="return enterNumOnlyzeroToNine(event)" /></td>
				<td class="formlabel">Min Duration (units):</td>
				<td class="forminfo"><html:text property="minDuration"
					onkeypress="return enterNumOnlyzeroToNine(event)" /></td>
				<td class="formlabel">Slab 1 Threshold (units):</td>
				<td class="forminfo"><html:text property="slab1Threshold"
					onkeypress="return enterNumOnlyzeroToNine(event)" /></td>
			</tr>
			<tr>
				<td class="formlabel">Incr Duration (units):</td>
				<td class="forminfo"><html:text property="incrDuration"
					onkeypress="return enterNumOnlyzeroToNine(event)" /></td>
				<td class="formlabel">Allow Zero Claim Amount:</td>
				<td>
					<insta:selectoptions name="allowZeroClaimAmount" value="${empty theaterDetails.map.allow_zero_claim_amount ? 'n' : theaterDetails.map.allow_zero_claim_amount}" opvalues="n,i,o,b" optexts="No,IP,OP,Both(IP & OP)"/>
				</td>
				<td class="formlabel">Insurance Category:</td>
			    <td>
			        <insta:selectdb  name="insurance_category_id" id="insurance_category_id"  value="${insurance_categories}" table="item_insurance_categories" valuecol="insurance_category_id" displaycol="insurance_category_name" filtercol="system_category" filtervalue="N" multiple="true"/>
			    </td>
			</tr>
			<tr>
				<td class="formlabel">Billing Group:</td>
				<td>
					<insta:selectdb  name="billingGroupId" id="billingGroupId"  value="${theaterDetails.map.billing_group_id}" table="item_groups" valuecol="item_group_id"
						displaycol="item_group_name" dummyvalue="-- Select --" filtercol="item_group_type_id,status" filtervalue="BILLGRP,A"/>
				</td>
			</tr>
		</table>
	</fieldset>
	<insta:taxations/>
	<div class="screenActions">
		<button type="button" name="Save" accesskey="S" onclick="validate();"><b><u>S</u></b>ave</button>
		<c:if test="${param._method=='geteditChargeScreen'}">
		|
		<a href="${cpath}/pages/masters/insta/operationtheatre/TheatMast.do?_method=getNewTheatreScreen">Add</a>
		|
		<a href="${cpath}/pages/masters/insta/operationtheatre/TheatMast.do?_method=showCharges&theatreId=${ifn:cleanURL(param.theatreId)}&orgId=${ifn:cleanURL(param.orgId)}&chargeType=${ifn:cleanURL(param.chargeType)}&store_id=${ifn:cleanURL(param.store_id)}">Theatre/Room Charges</a>
		</c:if>
		|
		<a href="${cpath}/pages/masters/insta/operationtheatre/TheatMast.do?_method=getTheatMast&status=A&sortReverse=false">Theatre/Room</a>
	</div>
</html:form>
</body>
</html>