<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Equipment Charges - Insta HMS</title>
		<insta:link type="script" file="hmsvalidation.js" />
		<insta:link type="js" file="masters/addEquipmet.js" />
		<insta:link type="js" file="masters/charges_common.js" />
		<insta:link type="js" file="masters/editCharges.js"/>
		<insta:link type="css" file="widgets.css" />
		<insta:link type="script" file="widgets.js" />
		<insta:link type="js" file="ajax.js" />
		<insta:link type="js" file="masters/orderCodes.js" />

		<c:set value="${pageContext.request.contextPath}" var="cpath" />
		<script>
		<%--	var namesList = ${ifn:convertListToJson(equipmentNames)}; --%>
		Insta.masterData=${ifn:convertListToJson(equipmentNames)};
			function fillRatePlanDetails(equipId){
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
						var doctorId = derivedRatePlanDetails[i].equip_id;
						var cell4 = row.insertCell(-1);
						var baseRateSheet = derivedRatePlanDetails[i].base_rate_sheet_id;
						var orgName = derivedRatePlanDetails[i].org_name;
						var url = cpath + '/pages/masters/ratePlan.do?_method=getOverideChargesScreen&org_id='+orgId+
								'&equip_id='+equipId+'&chargeCategory=equipment&fromItemMaster=true&baseRateSheet='+baseRateSheet+
								'&org_name='+orgName;
							cell4.innerHTML = '<a href="'+ url +'" title="Edit Charge" target="_blank">Edit Charge</a>';
					}
				}
			}
		</script>
	</head>
	
	<body class="yui-skin-sam" onload="fillRatePlanDetails('${bean.map.eq_id}');">
		<h1 style="float: left">Equipment Charges</h1>
	 	<c:url var="searchUrl" value="/master/equipment/editcharge.htm"/>
		<insta:findbykey keys="equipment_name,eq_id" fieldName="equip_id" method="" url="${searchUrl}"
 			extraParamKeys="org_id" extraParamValues="${bean.map.org_id}"/>

		<insta:feedback-panel/>
		<form action="${cpath}/master/equipment/editcharge.htm" name="showform" method="GET">		<%-- for rate plan change --%>
			<input type="hidden" name="equip_id" value="${bean.map.eq_id}"/>
			<input type="hidden" name="org_id" value=""/>
		</form>

		<form action="${cpath}/master/equipment/updatecharge.htm" name="chargesForm" method="POST">
			<input type="hidden" name="equip_id" value="${bean.map.eq_id}"/>
			<fieldset class="fieldsetBorder">
			<legend class="fieldsetLabel">Equipment Details</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Equipment Name:</td>
					<td class="forminfo">${bean.map.equipment_name}</td>
					<td class="formlabel">Rate Sheet:</td>
					<td>
					 <select name="org_id" id="org_id" class="dropdown" onchange="changeRatePlanAddShow();">
						<c:forEach items="${orgMasterData}" var="orgDetails">
							<option value="${orgDetails.get('org_id')}" ${orgDetails.get('org_id') eq bean.map.org_id ? 'selected' : ''}>${orgDetails.get('org_name')}</option>
						</c:forEach>
					</select>	
					</td>
				</tr>
				<tr>
					<td class="formlabel">Unit Size (Minutes):</td>
					<td class="forminfo">${bean.map.duration_unit_minutes}"</td>
					<td class="formlabel">Minimum Duration (units):</td>
					<td class="forminfo">${bean.map.min_duration}"</td>
					<td class="formlabel">Slab 1 Threshold (units):</td>
					<td class="forminfo">${bean.map.slab_1_threshold}"</td>
				</tr>
				<tr>
					<td class="formlabel">Incr. Duration (units):</td>
					<td class="forminfo">${bean.map.incr_duration}"</td>
				</tr>
			</table>
			</fieldset>

			<div class="resultList">
				<fieldset class="fieldsetBorder">
				<legend class="fieldsetLabel">Equipment Charges</legend>
				<table class="dataTable" id="equipmentCharges" align="left">
					<tr>
						<th>Bed Types</th>
						<c:forEach var="bed" items="${bedTypes}">
							<th style="width: 2em; overflow: hidden">${bed.get('bed_type')}</th>
							<c:set var="bed_t" value="${bed.get('bed_type')}"></c:set>
							<input type="hidden" name="bed_type" value="<c:out value='${bed_t}'/>"/>
						</c:forEach>
					</tr>

					<tr>
						<td style="text-align: right">Daily Charge</td>
						<c:forEach var="bed" items="${bedTypes}" varStatus="k">
							<c:set var="i" value="${k.index}"/>
							<td>
								<input type="text" name="daily_charge" id="daily_charge${i}"
								class="number validate-decimal"
								value="${ifn:afmt(charges[bed.get('bed_type')].daily_charge)}"
								onblur="validateDiscount('daily_charge','daily_charge_discount','${i}')"
								onkeypress="return nextFieldOnTab(event, this, 'equipmentCharges');"/>
							</td>
						</c:forEach>
						<input type="hidden" name="ids" value="${i+1}">
					</tr>
					<tr>
						<td style="text-align: right">Daily Charge Discount</td>
						<c:forEach var="bed" items="${bedTypes}" varStatus="k">
							<c:set var="i" value="${k.index}"/>
							<td>
								<input type="text" name="daily_charge_discount" id="daily_charge_discount${i}"
								class="number validate-decimal"
								value="${ifn:afmt(charges[bed.get('bed_type')].daily_charge_discount)}"
								onblur="validateDiscount('daily_charge','daily_charge_discount','${i}')"
								onkeypress="return nextFieldOnTab(event, this, 'equipmentCharges');"/>
							</td>
						</c:forEach>
					</tr>

					<tr>
						<td style="text-align: right">Min Charge</td>
						<c:forEach var="bed" items="${bedTypes}" varStatus="k">
							<c:set var="i" value="${k.index}"/>
							<td>
								<input type="text" name="min_charge" id="min_charge${i}"
								class="number validate-decimal"
								value="${ifn:afmt(charges[bed.get('bed_type')].min_charge)}"
								onblur="validateDiscount('min_charge','min_charge_discount','${i}')"
								onkeypress="return nextFieldOnTab(event, this, 'equipmentCharges');"/>
							</td>
						</c:forEach>
					</tr>
					<tr>
						<td style="text-align: right">Min Charge Discount</td>
						<c:forEach var="bed" items="${bedTypes}" varStatus="k">
							<c:set var="i" value="${k.index}"/>
							<td>
								<input type="text" name="min_charge_discount" id="min_charge_discount${i}"
								class="number validate-decimal"
								value="${ifn:afmt(charges[bed.get('bed_type')].min_charge_discount)}"
								onblur="validateDiscount('min_charge','min_charge_discount','${i}')"
								onkeypress="return nextFieldOnTab(event, this, 'equipmentCharges');"/>
							</td>
						</c:forEach>
					</tr>

					<tr>
						<td style="text-align: right">Slab 1 Charge</td>
						<c:forEach var="bed" items="${bedTypes}" varStatus="k">
							<c:set var="i" value="${k.index}"/>
							<td>
								<input type="text" name="slab_1_charge" id="slab_1_charge${i}"
								class="number validate-decimal"
								value="${ifn:afmt(charges[bed.get('bed_type')].slab_1_charge)}"
								onblur="validateDiscount('slab_1_charge','slab_1_charge_discount','${i}')"
								onkeypress="return nextFieldOnTab(event, this, 'equipmentCharges');"/>
							</td>
						</c:forEach>
					</tr>
					<tr>
						<td style="text-align: right">Slab 1 Charge Discount</td>
						<c:forEach var="bed" items="${bedTypes}" varStatus="k">
							<c:set var="i" value="${k.index}"/>
							<td>
								<input type="text" name="slab_1_charge_discount" id="slab_1_charge_discount${i}"
								class="number validate-decimal"
								value="${ifn:afmt(charges[bed.get('bed_type')].slab_1_charge_discount)}"
								onblur="validateDiscount('slab_1_charge','slab_1_charge_discount','${i}')"
								onkeypress="return nextFieldOnTab(event, this, 'equipmentCharges');"/>
							</td>
						</c:forEach>
					</tr>

					<tr>
						<td style="text-align: right">Incr Charge</td>
						<c:forEach var="bed" items="${bedTypes}" varStatus="k">
							<c:set var="i" value="${k.index}"/>
							<td>
								<input type="text" name="incr_charge" id="incr_charge${i}"
								class="number validate-decimal"
								value="${ifn:afmt(charges[bed.get('bed_type')].incr_charge)}"
								onblur="validateDiscount('incr_charge','incr_charge_discount','${i}')"
								onkeypress="return nextFieldOnTab(event, this, 'equipmentCharges');"/>
							</td>
						</c:forEach>
					</tr>
					<tr>
						<td style="text-align: right">Incr Charge Discount</td>
						<c:forEach var="bed" items="${bedTypes}" varStatus="k">
							<c:set var="i" value="${k.index}"/>
							<td>
								<input type="text" name="incr_charge_discount" id="incr_charge_discount${i}"
								class="number validate-decimal"
								value="${ifn:afmt(charges[bed.get('bed_type')].incr_charge_discount)}"
								onblur="validateDiscount('incr_charge','incr_charge_discount','${i}')"
								onkeypress="return nextFieldOnTab(event, this, 'equipmentCharges');"/>
							</td>
						</c:forEach>
					</tr>

					<tr>
						<td style="text-align: right">Tax(%)</td>
						   <c:forEach var="bed" items="${bedTypes}" varStatus="k">
						     <c:set var="i" value="${k.index}"/>
							 <td><input type="text" name="tax" class="number validate-decimal" id="tax${i}"
								value="${ifn:afmt(charges[bed.get('bed_type')].tax)}"
								onkeypress="return nextFieldOnTab(event, this, 'equipmentCharges');"/>
							 </td>
						  </c:forEach>


					</tr>
					<tr>
					   <c:if test="${not empty bedTypes}">
					     <td style="text-align: right">Copy charges to all Beds</td>
					     <td>
					       <input type="checkbox" name="checkbox" onclick="fillValues('equipmentCharges', this);"/>
					     </td>
					      <c:forEach begin="2" end="${fn:length (bedTypes)}">
					       <td>&nbsp;</td>
					     </c:forEach>
					   </c:if>
					</tr>
				</table>
				</fieldset>
			</div>

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

			<table class="screenActions">
				<tr>
					<td>
						<button type="button" name="update" accesskey="U" onclick="submitCharges();"><b><u>U</u></b>pdate</button>
					</td>
					<td>&nbsp;|&nbsp;</td>
					<td><a href="${cpath}/master/equipment/show.htm?equip_id=${bean.map.eq_id}&org_id=${bean.map.org_id}" >Equipment Details</a></td>
					<td>&nbsp;|&nbsp;</td>
					<td><a href="${cpath}/master/equipment.htm?status=A&org_id=ORG0001&sortOrder=equipment_name&sortReverse=false">Equipments List</a></td>
				</tr>
			</table>

			<script>
				var derivedRatePlanDetails = ${ifn:convertListToJson(derivedRatePlanDetails)};
			</script>
	</body>
</html>