<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta http-equiv="Cache-Control" content="no-cache"/>
		<title>Rate Plan Overrides - Insta HMS</title>
		<insta:link type="script" file="hmsvalidation.js"/>
		<insta:link type="js" file="masters/charges_common.js" />
		<insta:link type="js" file="masters/charges_common.js" />

	<script>
		var cpth  =  '${cpath}';
		function funSaveValues(){
			if(!validateAllDiscounts()) return false;
			document.regChargeForm.submit();
		}

		function validateAllDiscounts() {
			var charges = 5;
			var len = document.regChargeForm.ids.value;
			var valid = true;
			for(var j=0;j<charges;j++) {
				for(var i=0;i<len;i++) {
					valid = valid && validateDiscount('ip_reg_charge','ip_reg_charge_discount',i);
					valid = valid && validateDiscount('op_reg_charge','op_reg_charge_discount',i);
					valid = valid && validateDiscount('gen_reg_charge','gen_reg_charge_discount',i);
					valid = valid && validateDiscount('reg_renewal_charge','reg_renewal_charge_discount',i);
					valid = valid && validateDiscount('mrcharge','mrcharge_discount',i);
					valid = valid && validateDiscount('ip_mlccharge','ip_mlccharge_discount',i);
					valid = valid && validateDiscount('op_mlccharge','op_mlccharge_discount',i);
				}
			}
			if(!valid) return false;
			else return true;
		}
	</script>
</head>

<body >
	<form method="POST" action="${cpath}/pages/masters/ratePlan.do" name="regChargeForm" >
		<h1>Rate Plan Overrides - ${ifn:cleanHtml(org_name)}</h1>
		<input type="hidden" name="_method" value="overRideRegistrationCharges"/>
		<input type="hidden" name="org_id" id="org_id" value="${ifn:cleanHtmlAttribute(org_id)}"/>
		<input type="hidden" name="org_name" id="org_name" value="${ifn:cleanHtmlAttribute(org_name)}"/>

		<input type="hidden" name="fromItemMaster" value="${ifn:cleanHtmlAttribute(fromItemMaster)}"/>
		<input type="hidden" name="baseRateSheet" value="${ifn:cleanHtmlAttribute(baseRateSheet)}"/>

		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Registration Charges</legend>
		<div class="resultList">
			<table id="regCharges" cellpadding="0" cellspacing="0" class="dataTable">
				<tr>
					<th>BedType</th>
					<c:forEach items="${bedTypes}" var="bed" varStatus="k">
					<c:set var="j" value="${k.index}"/>
						<th>${bed}</th>
						<input type="hidden" name="beds" value="<c:out value='${bed}'/>"/>
					</c:forEach>
					<input type="hidden" name="ids" value="${j+1}">
				</tr>
				<tr>
					<td style="text-align: right">Ip Visit Charge:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="ip_reg_charge" class="number validate-decimal"
								id="ip_reg_charge${i.index}" value="${bean.map.ip_reg_charge}"
								onblur="validateDiscount('ip_reg_charge','ip_reg_charge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Ip Visit Discount:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="ip_reg_charge_discount" class="number validate-decimal"
								id="ip_reg_charge_discount${i.index}" value="${bean.map.ip_reg_charge_discount}"
								onblur="validateDiscount('ip_reg_charge','ip_reg_charge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Op Visit Charge:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="op_reg_charge" class="number validate-decimal"
								id="op_reg_charge${i.index}" value="${bean.map.op_reg_charge}"
								onblur="validateDiscount('op_reg_charge','op_reg_charge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Op Visit Discount:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="op_reg_charge_discount" class="number validate-decimal"
								id="op_reg_charge_discount${i.index}" value="${bean.map.op_reg_charge_discount}"
								onblur="validateDiscount('op_reg_charge','op_reg_charge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Registration Charge:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="gen_reg_charge" class="number validate-decimal"
								id="gen_reg_charge${i.index}" value="${bean.map.gen_reg_charge}"
								onblur="validateDiscount('gen_reg_charge','gen_reg_charge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Registration Discount:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="gen_reg_charge_discount" class="number validate-decimal"
								id="gen_reg_charge_discount${i.index}" value="${bean.map.gen_reg_charge_discount}"
								onblur="validateDiscount('gen_reg_charge','gen_reg_charge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Registration Renewal Charge:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="reg_renewal_charge" class="number validate-decimal"
								id="reg_renewal_charge${i.index}" value="${bean.map.reg_renewal_charge}"
								onblur="validateDiscount('reg_renewal_charge','reg_renewal_charge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Registration Renewal Discount:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="reg_renewal_charge_discount" class="number validate-decimal"
								id="reg_renewal_charge_discount${i.index}" value="${bean.map.reg_renewal_charge_discount}"
								onblur="validateDiscount('reg_renewal_charge','reg_renewal_charge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Medical Record Charge:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="mrcharge" class="number validate-decimal"
								id="mrcharge${i.index}" value="${bean.map.mrcharge}"
								onblur="validateDiscount('mrcharge','mrcharge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Medical Record Discount:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="mrcharge_discount" class="number validate-decimal"
								id="mrcharge_discount${i.index}" value="${bean.map.mrcharge_discount}"
								onblur="validateDiscount('mrcharge','mrcharge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Ip MLC Charge:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="ip_mlccharge" class="number validate-decimal"
								id="ip_mlccharge${i.index}" value="${bean.map.ip_mlccharge}"
								onblur="validateDiscount('ip_mlccharge','ip_mlccharge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Ip MLC Discount:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="ip_mlccharge_discount" class="number validate-decimal"
								id="ip_mlccharge_discount${i.index}" value="${bean.map.ip_mlccharge_discount}"
								onblur="validateDiscount('ip_mlccharge','ip_mlccharge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Op MLC Charge:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="op_mlccharge" class="number validate-decimal"
								id="op_mlccharge${i.index}" value="${bean.map.op_mlccharge}"
								onblur="validateDiscount('op_mlccharge','op_mlccharge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Op MLC Discount:</td>
					<c:forEach items="${bedTypes}" var="bed" varStatus="i">
						<c:forEach items="${beans}" var="bean">
						<c:if test="${bean.map.bed_type eq bed}">
							<td>
								<input type="text" name="op_mlccharge_discount" class="number validate-decimal"
								id="op_mlccharge_discount${i.index}" value="${bean.map.op_mlccharge_discount}"
								onblur="validateDiscount('op_mlccharge','op_mlccharge_discount','${i.index}')" onkeypress="return nextFieldOnTab(event, this, 'regCharges');"/>

							</td>
						</c:if>
						</c:forEach>
					</c:forEach>
				</tr>
				<c:if test="${not empty bedTypes}">
				<tr>
					<td style="text-align: right">Apply Charges To All</td>
					<td><input type="checkbox" name="checkbox" onclick="fillValues('regCharges', this);"/></td>
					<c:forEach begin="2" end="${fn:length (bedTypes)}" >
						<td>&nbsp;</td>
					</c:forEach>
				</tr>
				</c:if>
			</table>
		</div>
		</fieldset>

		<table class="screenActions">
		<tr>
			<td>
				<button type="button" accesskey="S" name="Save" onclick="funSaveValues();"><b><u>S</u></b>ave</button>
				<c:choose>
					<c:when test="${fromItemMaster eq 'false'}">
						| <a href="${cpath}/pages/masters/ratePlan.do?_method=showRatePlanDetails&org_id=${ifn:cleanURL(org_id)}">Edit Rate Plan</a>
					</c:when>
					<c:otherwise>
						<c:set var="url" value="${cpath}/master/RegistrationCharges.do?method=show"/>
						| <a href="<c:out value='${url}'/>">Registration Charges</a>
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
		</table>
	</form>

	<script>
		var cpath = '${cpath}';
	</script>
</body>
</html>
