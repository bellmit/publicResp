<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<title>Payment Rule - Insta HMS</title>
		<insta:link type="js" file="PaymentRule.js"/>
		<insta:link type="script" file="widgets.js"/>
		<insta:link type="script" file="ajax.js"/>
		<c:set var="cpath" value="${pageContext.request.contextPath}"/>
</head>
<body onload="loadActivity();">

	<form action="PaymentRule.do" name="paymentForm">
		<input type="hidden" name="_method" value="create">
		<input type="hidden" name="precedenceId" value="${precedenceId}">
		<input type="hidden" name="firstPayee" value="dr_payment"/>
		<input type="hidden" name="secondPayee" value="ref_payment"/>
		<input type="hidden" name="thirdPayee" value="presc_payment"/>

		<h1>Add Payment Rule</h1>
		<insta:feedback-panel/>
		<fieldset class="fieldSetBorder">
			<table class="formtable" style="white-space:nowrap">

				<tr>
					<td class="formlabel">Priority:</td>
					<td><input type="text" name="precedance" id="precedance" class="required validate-number" title="Priority is required"></td>
					<td class="formlabel">Rate Plan:</td>
					<td><select name="rate_plan" id="rate_plan" class="dropdown">
							<option value="*">All</option>
							<c:forEach var="rateplan" items="${organizationDetails}">
							<option value="${rateplan.map.org_id}">${rateplan.map.org_name}</option>
							</c:forEach>
					</select></td>
					<td colspan="2">&nbsp;</td>
				</tr>

				<tr>
					<td class="formlabel">Conducting Doctor Category:</td>
					<td><select name="doctor_category" id="doctor_category" onchange="Ajax.get('./CategoryMaster.do?_method=getCategoryDetailsJSON&id='+this.options[this.selectedIndex].value,function(data, status) { var text = eval(data);document.getElementById('doc_cat_desc').innerHTML = text==null||text==''?'':text; })" class="dropdown">
							<option value="*">All</option>
							<c:forEach var="details" items="${categoryDetails}">
							<option value="${details.map.cat_id}">${details.map.cat_name}</option>
							</c:forEach>
					</select>
					<span id="doc_cat_desc"></span></td>

					<td class="formlabel">Referrer Doctor Category:</td>
					<td><select name="referrer_category" id="referrer_category" onchange="Ajax.get('./CategoryMaster.do?_method=getCategoryDetailsJSON&id='+this.options[this.selectedIndex].value,function(data, status) { var text = eval(data);document.getElementById('ref_cat_desc').innerHTML = text==null||text==''?'':text; })"class="dropdown">
							<option value="*">All</option>
							<option value="">No Referrer</option>
							<c:forEach var="details" items="${categoryDetails}">
							<option value="${details.map.cat_id}">${details.map.cat_name}</option>
							</c:forEach>
					</select>
					<span id="ref_cat_desc"></span></td>

					<td class="formlabel">Prescribing Doctor Category:</td>
					<td><select name="prescribed_category" id="prescribed_category" onchange="Ajax.get('./CategoryMaster.do?_method=getCategoryDetailsJSON&id='+this.options[this.selectedIndex].value,function(data, status) { var text = eval(data);document.getElementById('pres_cat_desc').innerHTML = text==null||text==''?'':text; })" class="dropdown">
							<option value="*">All</option>
							<option value="">No Prescribing Doctor</option>
							<c:forEach var="details" items="${categoryDetails}">
							<option value="${details.map.cat_id}">${details.map.cat_name}</option>
							</c:forEach>
					</select>
					<span id="pres_cat_desc"></span></td>
				</tr>

				<tr>
					<td class="formlabel">Charge Groups:</td>
					<td><select name="charge_group" onchange="loadChargeHeads();loadActivity();" class="dropdown" id="charge_group">
							<option value="">-- Select --</option>
							<c:forEach var="group" items="${chargeGroups}">
								<option value="${group.map.chargegroup_id}">${group.map.chargegroup_name}</option>
							</c:forEach>
							</select>
					</td>
					<td class="formlabel">Charge Head:</td>
					<td><select name="charge_head" onchange="loadActivity();" class="dropdown" id="charge_head">
							<option value="">-- Select --</option>
					</select><span class="star">*</span></td>

					<td class="formlabel">Activity:</td>
					<td><select name="activity_id" class="dropdown"></select></td>

				</tr>
				<tr>
					<td class="formlabel">Conducting Doctor Payment Type: </td>
					<td>
						<select name="dr_payment_option" id="doctorSelect" onchange="selectLessAmountOption()" class="dropdown">
							<option value="1">Percentage</option>
							<option value="3">Actual</option>
							<option value="4">Less than bill amount</option>
							<option value="5">Expression Builder</option>
						</select>

					</td>
					<td class="formlabel">Value: </td>
					<td>
						<input type="text" name="dr_payment_value" id="dr_payment_value" size="6" class="required validate-number"
						value="0" title="Doctor Payment Value is required">
					</td>
					<td style="display:none;" id="drExprLbl" class="formlabel">Expression: </td>
					<td style="display:none;" id="drExpr"><input type="text" name="dr_payment_expr" id="dr_payment_expr"></input></td>
			</tr>
				<tr>
					<td class="formlabel">Referrer Doctor Payment Type: </td>
					<td>
						<select name="ref_payment_option"  id="referalDoctorSelect" onchange="selectLessAmountOption()" class="dropdown">
							<option value="1">Percentage</option>
							<option value="3">Actual</option>
							<option value="4">Less than bill amount</option>
							<option value="5">Expression Builder</option>
						</select>

					</td>
					<td class="formlabel">Value: </td>
					<td><input type="text" name="ref_payment_value" id="ref_payment_value" size="6" class="required validate-number"
						value="0" title="Referral Payment Value is required" ></td>
					<td style="display:none;" id="refExprLbl" class="formlabel">Expression: </td>
					<td style="display:none;" id="refExpr"><input type="text" name="ref_payment_expr" id="ref_payment_expr"></input></td>
					<td></td>
				</tr>

				<tr>
					<td class="formlabel">Prescribing Doctor Payment Type: </td>
					<td>
						<select name="presc_payment_option"  id="prescribingDoctorSelect" onchange="selectLessAmountOption()" class="dropdown">
							<option value="1">Percentage</option>
							<option value="3">Actual</option>
							<option value="4">Less than bill amount</option>
							<option value="5">Expression Builder</option>
						</select>
					</td>
					<td class="formlabel">Value: </td>
					<td><input type="text" name="presc_payment_value" id="presc_payment_value" size="6" class="required validate-number"
						value="0" title="prescribed Payment Value is required"></td>
					<td id="prescExprLbl" style="display:none;" class="formlabel">Expression: </td>
					<td id="prescExpr" style="display:none;"><input type="text" name="presc_payment_expr" id="presc_payment_expr"></input></td>
			</tr>
				<tr>
					<td class="formlabel">Package Payments Value: </td>
					<td colspan="5">
						<input type="text" name="dr_pkg_amt" size="6" class="required validate-number"
						 value="0" title="Package Payment Value is required">
					</td>
				</tr>
			</table>
		</fieldset>

		<dl class="accordion" style="margin-bottom: 10px;">
			<dt>
				<span>Expression Help</span>
				<div class="clrboth"></div>
			</dt>
			<dd id="expr_tokens">
				<div class="bd">
					<table class="resultList">
						<tr>
							<td colspan="5"><b>Simple expression:</b> \${amount - discount - 10}</td>
						</tr>
						<tr>
							<td colspan="5"><b>Complex expression:</b> &lt;#if bill_type == 'C'&gt;\${amount}&lt;#else&gt;\${amount - 20}&lt;/#if&gt;</td>
						</tr>
						<tr>
							<td colspan="5"><b>Available Tokens:</b></td>
						</tr>

						<tr>
							<c:forEach items="${exprTokens}" var="token" varStatus="it">
							<c:if test="${(it.index % 5) == 0}">
							</tr><tr>
							</c:if>
							<td>${token}</td>
							</c:forEach>
						</tr>
					</table>
				</div>
			</dd>
		</dl>

		<div class="screenActions"><button type="button" accesskey="S" onclick="return validateScreen();"><b><u>S</u></b>ave</button>&nbsp;|
			<a href="javascript:void(0)" onclick="doCancel()">Payment Rule List</a></div>
	</form>

	<script>
			var laboratoryDetails=${laboratoryTestDetails};
			var radiologyTestDetails=${radiologyTestDetails};
			var serviceDetails=${serviceDetails};
			var chargeHeads = ${chargeHeads};
			var packageDetails = ${packageDetails};
			var cpath = '${cpath}';
			function doCancel()
				{
					window.location.href="${cpath}/master/PaymentRule.do?_method=list&sortOrder=precedance&sortReverse=false";
				}


	</script>
</body>
</html>
