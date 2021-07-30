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
    Insta.masterData=${theatresLists};

    function fillRatePlanDetails(theatreId){
		if(derivedRatePlanDetails.length>0) {
			document.getElementById("ratePlanDiv").style.display = 'block' ;
			for (var i =0; i<derivedRatePlanDetails.length; i++) {
				var ratePlanTbl = document.getElementById("ratePlanTbl");
				var len = ratePlanTbl.rows.length;
				var templateRow = ratePlanTbl.rows[len-1];
			   	var row = '';
			   		row = templateRow.cloneNode(true);
			   		row.style.display = '';
			   		row.id = len-2;
			   		len = row.id;
			   	YAHOO.util.Dom.insertBefore(row, templateRow);

				var cell1 = row.insertCell(-1);
			    cell1.setAttribute("style", "width: 70px");
			    if(derivedRatePlanDetails[i].is_override=='Y')
		    		cell1.innerHTML = '<span class="label"><img src="'+cpath+'/images/blue_flag.gif"/>&nbsp;'+derivedRatePlanDetails[i].org_name;
		    	else
		    		cell1.innerHTML = '<span class="label"><img src="'+cpath+'/images/empty_flag.gif"/>&nbsp;'+derivedRatePlanDetails[i].org_name;
			    var inp2 = document.createElement("INPUT");
			    inp2.setAttribute("type", "hidden");
			    inp2.setAttribute("name", "ratePlanId");
			    inp2.setAttribute("id", "ratePlanId"+len);
			    inp2.setAttribute("value", derivedRatePlanDetails[i].org_id);
			    cell1.appendChild(inp2);

				var cell2 = row.insertCell(-1);
			    cell2.setAttribute("style", "width: 70px");
			    cell2.innerHTML = "<span class='label'>"+derivedRatePlanDetails[i].discormarkup;

			    var cell3 = row.insertCell(-1);
			    cell3.setAttribute("style", "width: 40px");
			    cell3.innerHTML = "<span class='label'>"+derivedRatePlanDetails[i].rate_variation_percent;

				var orgId = derivedRatePlanDetails[i].org_id;
				var doctorId = derivedRatePlanDetails[i].doctor_id;
				var cell4 = row.insertCell(-1);
				var baseRateSheet = derivedRatePlanDetails[i].base_rate_sheet_id;
				var orgName = derivedRatePlanDetails[i].org_name;
				var url = cpath + '/pages/masters/ratePlan.do?_method=getOverideChargesScreen&org_id='+orgId+
						'&theatre_id='+theatreId+'&chargeCategory=operationTheatre&fromItemMaster=true&baseRateSheet='+baseRateSheet+
						'&org_name='+orgName;
					cell4.innerHTML = '<a href="'+ url +'" title="Edit Charge" target="_blank">Edit Charge</a>';
			}
		}
	}

 </script>
</head>
<body onload="fillRatePlanDetails('${ifn:cleanJavaScript(param.theatreId)}')">
     <h1 style="float:left">Theatre/Room Charges </h1>
     <c:url var="searchUrl" value="/pages/masters/insta/operationtheatre/TheatMast.do"/>
	     <insta:findbykey keys="theatre_name,theatre_id"  fieldName="theatreId" method="showCharges" url="${searchUrl}"
		 extraParamKeys="orgId" extraParamValues="${param.orgId}"/>

	<html:form action="/pages/masters/insta/operationtheatre/TheatMast.do">
		<input type="hidden" name="_method" value="${requestScope._method}" />
		<html:hidden property="theatreId" />
		<html:hidden property="pageNum" />
		<html:hidden property="chargeType" />
		<html:hidden property="orgId" />
		<input type="hidden" name="theatreID" value="${ifn:cleanHtmlAttribute(param.theatreId)}"/>
		<input type="hidden" name="orgID" value="${ifn:cleanHtmlAttribute(param.orgId)}"/>
		<input type="hidden" name="chargeType" value="${ifn:cleanHtmlAttribute(param.chargeType)}"/>
		<input type="hidden" name="pageNo" value="${ifn:cleanHtmlAttribute(param.pageNum)}"/>
		<input type="hidden" name="pageNum" value="${ifn:cleanHtmlAttribute(param.pageNum)}"/>
		<input type="hidden" name="store_id" value="${empty param.store_id ? theaterDetails.map.store_id : param.store_id}" />
		<input type="hidden" name="contextPath" value="${pageContext.request.contextPath}" />
	   <insta:feedback-panel/>

		<fieldset class="fieldsetborder">
		<legend class="fieldsetLabel">Theatre/Room Details</legend>
		<table class="formtable">
				<tr>
					<td class="formlabel">Theatre/Room Name:</td>
					<td>${theaterDetails.map.theatre_name}</td>
					<td class="formlabel">Rate Sheet:</td>
					<td>
						<insta:selectdb name="ratePlan" value="${param.orgId}"
							table="organization_details" valuecol="org_id" orderby="org_name"
							displaycol="org_name" onchange="changeRatePlan();"
							filtered="true" filtercol="status,is_rate_sheet" filtervalue="A,Y"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Unit Size (minutes):</td>
					<td class="forminfo">${theaterDetails.map.duration_unit_minutes}</td>
					<td class="formlabel">Min Duration (units):</td>
					<td class="forminfo">${theaterDetails.map.min_duration}</td>
				</tr>
				<tr>
					<td class="formlabel">Slab 1 Threshold (units):</td>
					<td class="forminfo">${theaterDetails.map.slab_1_threshold}</td>
					<td class="formlabel">Incr Duration (units):</td>
					<td class="forminfo">${theaterDetails.map.incr_duration}</td>
				</tr>
			</table>
			</fieldset>

			<div style="padding:5px 0 5px 0"></div>
			<fieldset class="fieldsetborder">
			<legend class="fieldsetLabel">Theatre/Room Charges</legend>
			<div class="resultList">
			<table class="dataTable" id="opTheatreCharges">
				<c:forEach var="entry" items="${requestScope.map}">
					<c:choose>
						<c:when test="${entry.key eq 'CHARGES'}">
							<tr>
								<th>CHARGES</th>
								<c:forEach var="item" items="${entry.value}">
									<th>${ifn:cleanHtml(item)}</th>
									<input type="hidden" name="bedTypes" value="<c:out value='${item}'/>"/>
								</c:forEach>
							</tr>
						</c:when>
						<c:when test="${entry.key eq 'dailyChgarge'}">
							<tr>
								<td>Daily Charge</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"/>
									<td><input type="text" value="${ifn:afmt(item)}"
									onkeypress="return nextFieldOnTab(event, this, 'opTheatreCharges');" name="dailyCharge" id="dailyCharge${i}"
									class="number validate-decimal" onblur="validateDiscount('dailyCharge','dailyChargeDiscount','${i}')"/>
									</td>
								</c:forEach>
								<input type="hidden" name="ids" value="${i+1}">
							</tr>
						</c:when>
						<c:when test="${entry.key eq 'dailyChargeDiscount'}">
							<tr>
								<td>Daily Charge Discount</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"/>
									<td><input type="text" value="${ifn:afmt(item)}"
									onkeypress="return nextFieldOnTab(event, this, 'opTheatreCharges');" name="dailyChargeDiscount" id="dailyChargeDiscount${i}"
									class="number validate-decimal" onblur="validateDiscount('dailyCharge','dailyChargeDiscount','${i}')"/>
									</td>
								</c:forEach>
							</tr>
						</c:when>

						<c:when test="${entry.key eq 'minCharge'}">
							<tr>
								<td>Min Charge</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"/>
									<td><input type="text" value="${ifn:afmt(item)}"
										onkeypress="return nextFieldOnTab(event, this, 'opTheatreCharges');" name="minCharge" id="minCharge${i}"
									class="number validate-decimal" onblur="validateDiscount('minCharge','minChargeDiscount','${i}')"/>
									</td>
								</c:forEach>
							</tr>
						</c:when>
						<c:when test="${entry.key eq 'minChargeDiscount'}">
							<tr>
								<td>Min Charge Discount</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"/>
									<td><input type="text" value="${ifn:afmt(item)}"
									onkeypress="return nextFieldOnTab(event, this, 'opTheatreCharges');" name="minChargeDiscount" id="minChargeDiscount${i}"
									class="number validate-decimal" onblur="validateDiscount('minCharge','minChargeDiscount','${i}')"/>
									</td>
								</c:forEach>
							</tr>
						</c:when>
						<c:when test="${entry.key eq 'slab1Charge'}">
							<tr>
								<td>Slab 1 Charge</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"/>
									<td><input type="text" value="${ifn:afmt(item)}"
										onkeypress="return nextFieldOnTab(event, this, 'opTheatreCharges');" name="slab1Charge" id="slab1Charge${i}"
									class="number validate-decimal" onblur="validateDiscount('slab1Charge','slab1ChargeDiscount','${i}')"/>
									</td>
								</c:forEach>
							</tr>
						</c:when>
						<c:when test="${entry.key eq 'slab1ChargeDiscount'}">
							<tr>
								<td>Slab 1 Charge Discount</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"/>
									<td><input type="text" value="${ifn:afmt(item)}"
										onkeypress="return nextFieldOnTab(event, this, 'opTheatreCharges');" name="slab1ChargeDiscount" id="slab1ChargeDiscount${i}"
									class="number validate-decimal" onblur="validateDiscount('slab1Charge','slab1ChargeDiscount','${i}')"/>
									</td>
								</c:forEach>
							</tr>
						</c:when>
						<c:when test="${entry.key eq 'incrCharge'}">
							<tr>
								<td>Incr Charge</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"/>
									<td><input type="text" value="${ifn:afmt(item)}"
										onkeypress="return nextFieldOnTab(event, this, 'opTheatreCharges');" name="incrCharge" id="incrCharge${i}"
									class="number validate-decimal" onblur="validateDiscount('incrCharge','incrChargeDiscount','${i}')"/>
									</td>
								</c:forEach>
							</tr>
						</c:when>
						<c:when test="${entry.key eq 'incrChargeDiscount'}">
							<tr>
								<td>Incr Charge Discount</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"/>
									<td><input type="text" value="${ifn:afmt(item)}"
									onkeypress="return nextFieldOnTab(event, this, 'opTheatreCharges');" name="incrChargeDiscount" id="incrChargeDiscount${i}"
									class="number validate-decimal" onblur="validateDiscount('incrCharge','incrChargeDiscount','${i}')"/>
									</td>
								</c:forEach>
							</tr>
						</c:when>
						<c:when test="${entry.key eq 'tax'}">
							<tr>
								<td>Tax(%)</td>
								<c:forEach var="item" items="${entry.value}">
									<td><input type="text" value="${ifn:afmt(item)}"
									onkeypress="return nextFieldOnTab(event, this, 'opTheatreCharges');" name="tax" class="number validate-decimal" /></td>
								</c:forEach>
							</tr>
						</c:when>
					</c:choose>
				</c:forEach>
				<tr>
				   <c:if test="${not empty map}">
				     <td style="text-align: right">Copy Charges to all Bed Types</td>
				     <td><input type="checkbox" name="checkbox" onclick="fillValues('opTheatreCharges', this)"></td>
				     <c:forEach begin="2" end="${fn:length (map.CHARGES)}">
				     	<td>&nbsp;</td>
				     </c:forEach>

				   </c:if>
				</tr>
			</table>
		</div>
		</fieldset>

		<div id="ratePlanDiv" style="display:none">
			<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Rate Plan List</legend>
				<table class="dashBoard" id="ratePlanTbl">
					<tr class="header">
						<td>Rate Plan</td>
						<td>Discount / Markup</td>
						<td>Variation %</td>
						<td>&nbsp;</td>
					</tr>
					<tr id="" style="display: none">
				</table>
				<table class="screenActions" width="100%">
					<tr>
						<td align="right">
							<img src='${cpath}/images/blue_flag.gif'>Overridden
						</td>
					</tr>
				</table>
			</fieldset>
		</div>

		<div class="screenActions">
			<button type="button" name="update" accesskey="U" onclick="validateCharges();"><b><u>U</u></b>pdate</button>
			|
			<a href="${cpath}/pages/masters/insta/operationtheatre/TheatMast.do?_method=geteditChargeScreen&theatreId=${theaterDetails.map.theatre_id}&orgId=${ifn:cleanURL(param.orgId)}&chargeType=${ifn:cleanURL(param.chargeType)}&pageNum=${ifn:cleanURL(param.pageNum)}&store_id=${empty param.store_id ? theaterDetails.map.store_id : param.store_id}">Theatre/Room</a>
			|
			<a href="${cpath}/pages/masters/insta/operationtheatre/TheatMast.do?_method=getTheatMast&status=A&sortReverse=false">Theatres/Rooms List</a>
		</div>
	</html:form>
	<script>
		var derivedRatePlanDetails = ${derivedRatePlanDetails};
	</script>
</body>
</html>