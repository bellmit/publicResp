<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>${param._method == 'add' ? 'Add' : 'Edit'} Per Diem Code</title>
	<insta:link type="js" file="PerDiemCodes/PerDiemCodes.js" />
	<insta:link type="js" file="masters/charges_common.js" />
	<script type="text/javascript">
		var perdiemCodesListJSON = <%= request.getAttribute("perdiemCodesListJSON") %> ;
		var itemGroupList = ${itemGroupListJson};
		var itemSubGroupList = ${itemSubGroupListJson};
	</script>
</head>

<body onload="itemsubgroupinit();fillAllValuesForAdd();">
	<form method="POST" action="PerDiemCodes.do" name="perDiemCodesForm" >
		<c:if test="${param._method == 'add'}"><h1>Add New Per Diem Code</h1></c:if>
		<c:if test="${param._method == 'show'}"><h1>Edit Per Diem Code</h1></c:if>
		<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">

	<insta:feedback-panel/>

		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Per Diem Code Details</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Per Diem Code:</td>
					<td>
						<c:choose>
							<c:when test="${param._method == 'add'}">
								<select name="per_diem_code" id="per_diem_code" class="dropdown"
								   onchange="setPerdiemDescription();">
								<option value="" selected>-- Select --</option>
									<c:forEach items="${perdiemCodesList}" var="perdiemcode">
										<option value="${perdiemcode.per_diem_code}"
											style="width:300px;"
											onmouseover='this.title = "${fn:escapeXml(perdiemcode.per_diem_description)}"'>
										 ${perdiemcode.per_diem_description}</option>
									</c:forEach>
								</select>
							</c:when>
							<c:otherwise>
								<input type="text" name="per_diem_code" class="field" readOnly
									value="${bean.map.per_diem_code}" id="per_diem_code" maxlength="15">
							</c:otherwise>
						</c:choose>
					</td>
					<td class="formlabel">Status:</td>
					<td>
						<insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I"
							optexts="Active,Inactive"/>
					</td>

					<td class="formlabel">Included Service Groups:</td>
					<td rowspan="4">
						<insta:selectdb name="service_groups_incl" id="service_groups_incl" multiple="true"
							class="noClass" optionTitle="true" table="service_groups" values="${inclSerGrps}"
							valuecol="service_group_id" displaycol="service_group_name" orderby="service_group_name"
					 		style="width:15em;height:10em;padding-left:3px;color:#666666;"  />
					</td>
				</tr>
				<tr>
					<td class="formlabel">Code Description:</td>
					<td colspan="3" style="width:200px;">
						<input type="text" name="per_diem_description" id="per_diem_description"
						   style="width:455px;" readOnly
							value="${bean.map.per_diem_description}" title="${bean.map.per_diem_description}" >
					</td>
				</tr>
				<tr>
					<td class="formlabel">Last Updated User:</td>
					<td class="forminfo">${bean.map.username}</td>
				</tr>
				<tr>
					<td class="formlabel">Last Modified Date :</td>
					<td class="forminfo">
						<fmt:formatDate value="${bean.map.mod_time}" pattern="dd-MM-yyyy HH:mm"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Home Care Code:</td>
					<td>
					<insta:selectoptions name="is_home_care_code" value="${bean.map.is_home_care_code}" opvalues="N,Y"
							optexts="No,Yes"/>
					</td>		
				</tr>
			</table>
		</fieldset>

		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Rate Plan Details</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Rate Plan:</td>
					<td>
						<c:choose>
							<c:when test="${param._method eq 'show'}">
								<insta:selectdb name="org_id" value="${org_id}"
									table="organization_details" valuecol="org_id" orderby="org_name"
									displaycol="org_name" onchange="getChargesForNewRatePlan();"/>
							</c:when>
							<c:otherwise>
								<label class="forminfo">GENERAL</label>
								<input type="hidden" name="org_id" value="ORG0001">
							</c:otherwise>
						</c:choose>
					</td>
					<td></td>
					<td></td>
					<td></td>
					<td></td>
				</tr>
			</table>
		</fieldset>

		<div class="resultList">
			<table class="dataTable" id="perDiemCharges" cellpadding="0" cellspacing="0">
				<tr>
					<th>Bed Types</th>
					<c:forEach var="bed" items="${bedTypes}">
						<th style="width: 2em; overflow: hidden">${bed}</th>
						<input type="hidden" name="bed_type" value="<c:out value='${bed}'/>"/>
					</c:forEach>
				</tr>

				<tr>
					<td>Charge</td>
					<c:forEach var="bed" items="${bedTypes}" varStatus="k">
						<c:set var="i" value="${k.index}"/>
						<td>
							<input type="text" name="charge" id="charge${i}" class="number validate-decimal"
							value="${ifn:afmt(charges[bean.map.per_diem_code][bed].map['charge'])}" onkeypress="return nextFieldOnTab(event, this, 'perDiemCharges');">
						</td>
					</c:forEach>
					<input type="hidden" name="ids" value="${i+1}">
				</tr>

				<tr id="audit_log_row">
					<td>&nbsp;</td>
					<c:forEach var="bed" items="${bedTypes}">
						<td style="width: 2em; overflow: hidden">
							<insta:screenlink screenId="per_diem_codes_charges_audit_log"
								extraParam="?_method=getAuditLogDetails&per_diem_code=${bean.map.per_diem_code}
								&bed_type=${bed}&org_id=${org_id}&per_diem_description=${ifn:encodeUriComponent(bean.map.per_diem_description)}
								&service_groups_names=${ifn:encodeUriComponent(bean.map.service_groups_names)}
								&al_table=per_diem_codes_charges_audit_log_view" label="Ch Audit Log"/>
						</td>
					</c:forEach>
				</tr>
				<tr>
					<c:if test="${not empty bedTypes}">
					   <td style="text-align: right">Apply Charges To All</td>
					   <td><input type="checkbox" name="checkbox" onclick="fillValues('perDiemCharges', this);"/></td>
					   <c:forEach begin="2" end="${fn:length (bedTypes)}">
					     <td>&nbsp;</td>
					   </c:forEach>
					</c:if>
				</tr>
			</table>
		</div>
		
	<insta:taxations/>
	
		<c:url var="url" value="PerDiemCodes.do">
			<c:param name="_method" value="list"/>
			<c:param name="sortReverse" value="false"/>
			<c:param name="status" value="A"/>
		</c:url>
		<table class="screenActions">
			<tr>
				<td>
					<button type="button" name="Save" accesskey="S" onclick="return validate();"><b><u>S</u></b>ave</button> |
					<a href="<c:out value='${url}'/>">Per Diem Codes List</a>
				</td>
			</tr>
		</table>
	</form>
</body>

</html>
