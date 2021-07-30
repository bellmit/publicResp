<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<html>
	<head>
		<insta:link type="css" file="widgets.css"/>
		<insta:link type="script" file="widgets.js"/>
		<insta:link type="script" file="hmsvalidation.js"/>
		<insta:link type="js" file="masters/charges_common.js" />
		<insta:link type="script" file="masters/dietmaster.js"/>

		<script>
		   <c:if test="${param._method != 'add'}">
		       Insta.masterData=${chargesLists};
		   </c:if>
		   //Bug#:	42425
		   var dietPresFormName = null ;
		   YAHOO.util.Event.onContentReady('content', setFormName);
			function setFormName() {
				dietPresFormName = document.dietryMasterForm;
			}
		</script>
	</head>
	<body>
 <c:choose>
     <c:when test="${param._method != 'add'}">
	   <h1 style="float:left">Edit Charges</h1>
	   <c:url var="searchUrl" value="/dietary/DietaryMaster.do"/>
	   <insta:findbykey keys="meal_name,diet_id" fieldName="diet_id" method="editCharges" url="${searchUrl}"
	   extraParamKeys="organization" extraParamValues="ORG0001"/>
	 </c:when>
	 <c:otherwise>
	    <h1>Edit Charges</h1>
	 </c:otherwise>
 </c:choose>
		<form action="DietaryMaster.do" name="dietryMasterForm" method="post" onsubmit="return validateAllDiscounts();">
			<input type="hidden" name="_method" id="_method" value="updateDietCharges">
			<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}">

			<insta:feedback-panel/>
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Meal Details</legend>
				<table class="formtable">
					<tr>
						<td class="formlabel">Meal Name :</td>
						<td>
							<label class="forminfo">
							${bean.map.meal_name}
							</label>
							<input  type="hidden" name="diet_id" id="diet_id" value="${bean.map.diet_id}">
						</td>

						<td class="formlabel">Diet Category :</td>
						<td>
							<label class="forminfo">
							${bean.map.diet_category}
							</label>
						</td>

						<td class="formlabel">Diet Type :</td>
						<td>
							<label class="forminfo">
								${bean.map.diet_type}
							</label>
						</td>

					</tr>

					<tr>
						<td class="formlabel">Remarks :</td>
						<td>
							<label class="forminfo">
								${bean.map.remarks}
							</label>
						</td>
						<td class="formlabel">Service Tax</td>
						<td>
							<label class="forminfo">${bean.map.service_tax}</label>
						</td>
						<td class="formlabel">Calorific Value :</td>
						<td>
							<label id="totalCalorificValue" class="forminfo">${ifn:cleanHtml(tot_cal)}</label>
							<input type="hidden" id="totalCalorificValue" value="${ifn:cleanHtmlAttribute(tot_cal)}">
						</td>

					</tr>

				</table>
			</fieldset>
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Charge Details</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Rate Sheet :</td>
					<td>
						<insta:selectdb  name="organization" table="organization_details"
							value ="${org_id}" valuecol="org_id" displaycol="org_name"
							orderby="org_name"  onchange="getRatePlanCharges(this,'edit')"
							filtered="true" filtercol="status,is_rate_sheet" filtervalue="A,Y"/>
					</td>
					<td class="formlabel"></td>
					<td></td>
					<td class="formlabel"></td>
					<td></td>
				</tr>
			</table>
			<table id="dietaryCharges" class="dataTable">
				<c:forEach var="entry" items="${requestScope.chargeMap}">

					<c:choose>
						<c:when test="${entry.key eq 'CHARGES'}">
							<tr>
								<th>Bed Type</th>
								<c:forEach var="item" items="${entry.value}">
									<th>${ifn:cleanHtml(item)}</th>
									<input type="hidden" name="bedTypes" value="<c:out value="${item}"/>">
								</c:forEach>
							</tr>
						</c:when>
						<c:when test="${entry.key eq 'REGULARCHARGE'}">
							<tr>
								<td>CHARGES</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"></c:set>
									<td><input type="text" value="${ifn:afmt(item)}"
										name="regularCharges"  id="regularCharges${i}" class="number validate-decimal"
										onkeypress="return nextFieldOnTab(event, this, 'dietaryCharges');"
										onblur="validateDiscount('regularCharges','discount','${i}')"/>
									</td>
								</c:forEach>
								<input type="hidden" name="ids" value="${i+1}">
							</tr>
						</c:when>
						<c:when test="${entry.key eq 'DISCOUNT'}">
							<tr>
								<td>DISCOUNT</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"></c:set>
									<td><input type="text" value="${ifn:afmt(item)}"
										name="discount"  id="discount${i}" class="number"
										onkeypress="return nextFieldOnTab(event, this, 'dietaryCharges');"
										onblur="validateDiscount('regularCharges','discount','${i}')"/>
									</td>
								</c:forEach>
							</tr>
						</c:when>
					</c:choose>
				</c:forEach>
				<c:if test="${not empty chargeMap}">
				<tr>
					<td>Apply Charges To All</td>
					<td><input type="checkbox" name="checkbox" onclick="fillValues('dietaryCharges', this);"></td>
					<c:forEach begin="2" end="${fn:length (chargeMap.CHARGES)}">
						<td>&nbsp;</td>
					</c:forEach>
				</tr>
				</c:if>
			</table>
		</fieldset>
		<table width="100%" class="formtable">
			<tr>
				<c:url var="dashBoardUrl" value="DietaryMaster.do">
					<c:param name="_method" value="list"></c:param>
					<c:param name="org_id" value="${org_id}"></c:param>
				</c:url>
				<td>
					<button type="submit" accesskey="U"><b><u>U</u></b>pdate Charges</button>
					<a href="<c:out value='${dashBoardUrl}' />" title="Diet Dashboard">|&nbsp;Diet Dashboard</a>
				</td>
			</tr>
		</table>
	</form>
	</body>
</html>
