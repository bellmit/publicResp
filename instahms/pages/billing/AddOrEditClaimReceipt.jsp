<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<title>Claim Receipt ${bean.map.receipt_no}</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<insta:link type="script" file="billing/claim_receipts.js"/>
	<insta:link type="script" file="billing/claimsCommon.js"/>
	<insta:link type="script" file="paymentCommon.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
<script>
	var jPaymentModes = <%= request.getAttribute("paymentModesJSON") %>;
	var companyList = ${insCompList};
	var categoryList = ${insCategoryList};
	var tpaList = ${tpaList};

	function validate() {
		var amountObj  = mainform.amount;
		var insCompObj = mainform.insurance_co_id;
		var tpaObj     = mainform.tpa_id;
		var dateObj    = mainform.paymentDate;
		var timeObj    = mainform.paymentTime;
		var bankObj    = mainform.bank_name;
		var refNoObj   = mainform.reference_no;
		var payRefObj  = mainform.payment_reference;
		var modeObj    = mainform.payment_mode_id;
		var cardObj    = mainform.card_type_id;

		if (tpaObj.value == '') {
			alert("Select TPA name");
			tpaObj.focus();
			return false;
		}
		if (amountObj.value == '') {
			alert("Enter amount");
			amountObj.focus();
			return false;
		}
		if (getPaise(amountObj.value) == 0) {
			alert("Enter the amount");
			amountObj.focus();
			return false;
		}
		if (!validateAmount(amountObj, "Amount is not valid"))
			return false;

		if (dateObj != null) {
			if (dateObj.value == '') {
				alert("Enter date");
				dateObj.focus();
				return false;
			}
			if (timeObj.value == '') {
				alert("Enter time");
				timeObj.focus();
				return false;
			}
			if (!doValidateDateField(dateObj, 'past')) {
				dateObj.focus();
				return false;
			}

			if (!validateTime(timeObj)) {
				timeObj.focus();
				return false;
			}
		}
		if (payRefObj.value == '') {
			alert("Enter payment reference");
			payRefObj.focus();
			return false;
		}
		if (modeObj.value != -1) {
			/*if (!cardObj.disabled) {
				if (cardObj.value == '') {
					alert("Select card type");
					cardObj.focus();
					return false;
				}
			}*/
			if (!bankObj.disabled) {
				if (bankObj.value == '') {
					alert("Enter bank name");
					bankObj.focus();
					return false;
				}
				if (bankObj.value.length > 50) {
					alert("Enter short name for the bank name field as it cross the field size (50)");
					bankObj.focus();
					return false;
				}
			}
			if (!refNoObj.disabled) {
				if (refNoObj.value == '') {
					alert("Enter reference number");
					refNoObj.focus();
					return false;
				}
				if (refNoObj.value.length >50) {
					 alert("Enter short reference number for ref num field as it cross the field size (50)");
					 refNoObj.focus();
  	                 return false;
				}
			}
		}
		return true;
	}

	function setEnableDisable() {
		var disable = '${empty bean || bean.map.payment_mode_id == -1}';
		if (disable == 'true')
			document.getElementById("cardTypeId").setAttribute("disabled","disabled");
	}
</script>
</head>

<body onload="init();setEnableDisable();hidePaymentModeForPaymentVoucher();">

<form name="mainform" method="POST" action="claimReceipts.do">
<jsp:useBean id="currentDate" class="java.util.Date"/>

<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
<c:if test="${param._method == 'show'}">
	<input type="hidden" name="receipt_no" value="${bean.map.receipt_no}"/>
</c:if>

<div class="pageHeader">${param._method == 'add' ? 'New Receipt' : 'Edit Receipt'}</div>
<insta:feedback-panel/>

<c:set var="disable"> ${not empty bean && bean.map.payment_mode_id != -1 ? '' : 'disabled'}</c:set>

<fieldset class="fieldSetBorder">
<table class="formtable" align="center">
	<tr>
		<c:if test="${param._method != 'add'}">
			<td class="formlabel">Receipt No:</td>
			<td class="forminfo"> ${bean.map.receipt_no} </td>
		</c:if>
		<td class="formlabel">Ins. Company Name:</td>
		<td class="forminfo">
			<insta:selectdb displaycol="insurance_co_name" name="insurance_co_id" id="insurance_co_id" value="${bean.map.insurance_co_id}"
				table="insurance_company_master" valuecol="insurance_co_id" dummyvalue="(All)" onchange="onChangeInsuranceCompany()"/>
		</td>
		<td class="formlabel">TPA Name:</td>
		<td class="forminfo">
			<insta:selectdb displaycol="tpa_name" name="tpa_id" id="tpa_id" value="${bean.map.tpa_id}"
				table="tpa_master" valuecol="tpa_id" dummyvalue="(All)" onchange="onChangeTPA();"/>
		</td>
	</tr>
	<tr>
		<td class="formlabel">Pay:</td>
		<td class="forminfo">
			<input type="text" name="amount" value="${bean.map.amount}">
		</td>

		<td class="formlabel">Date/Time:</td>
		<fmt:formatDate var="dateVal" value="${currentDate}" pattern="dd-MM-yyyy"/>
		<fmt:formatDate var="timeVal" value="${currentDate}" pattern="HH:mm"/>
		<c:set var="displaydate"><fmt:formatDate value="${bean.map.display_date}" pattern="dd-MM-yyyy"/></c:set>
		<c:set var="displaytime"><fmt:formatDate value="${bean.map.display_date}" pattern="HH:mm"/></c:set>
		<c:choose>
			<c:when test="${actionRightsMap.allow_backdate == 'A' || roleId == 1 || roleId ==2}">
				<td>
					<insta:datewidget name="paymentDate" value="${empty displaydate ? dateVal : displaydate}" valid="past" btnPos="right"/>
					<input type="text" name="paymentTime" class="timefield" value="${empty displaytime ? timeVal : displaytime}"/>
				</td>
			</c:when>
			<c:otherwise>
				<td class="forminfo">${dateVal} ${timeVal}
			</c:otherwise>
		</c:choose>
		<td class="formlabel">Counter:</td>
		<td class="forminfo">
			<c:choose>
				<c:when test="${param._method == 'add'}">
					<select name="counter" id="counter" class="dropdown">
						<c:if test="${not empty billingcounterId && billingcounterId != ''}">
							<option value="${billingcounterId}" ${bean.map.counter == billingcounterId ? 'selected':''}>${billingcounterName}</option>
						</c:if>
						<c:if test="${not empty pharmacyCounterId && pharmacyCounterId != ''}">
							<option value="${pharmacyCounterId}" ${bean.map.counter == pharmacyCounterId ? 'selected':''}>${pharmacyCounterName}</option>
						</c:if>
					</select>
				</c:when>
				<c:otherwise>
					<b><c:out value="${counterName}"/></b>
					<input type="hidden" name="counter" id="counter" value="${bean.map.counter}"/>
				</c:otherwise>
			</c:choose>
		</td>
	</tr>
	<tr>
		<td class="formlabel">Mode:</td>
		<td>
			<insta:selectdb name="payment_mode_id" id="paymentModeId" onchange="onChangePaymentMode();"
			 table="payment_mode_master" displaycol="payment_mode" valuecol="mode_id" value="${empty bean ? -1 : bean.map.payment_mode_id}"/>
		</td>
		<td class="formlabel">Payment Reference:</td>
		<td class="forminfo"><input type="text" name="payment_reference" value="${bean.map.payment_reference}" maxlength="50"/></td>
		<td class="formlabel">Remarks:</td>
		<td colspan="3" class="forminfo">
			<input type="text" name="remarks" id="paymentRemarks" maxlength="50" value="${bean.map.remarks}" ${disable}/>
		</td>
	</tr>
	<tr>
		<td class="formlabel">Card Type:</td>
		<td>
			<insta:selectdb name="card_type_id" id="cardTypeId" dummyvalue="-- Select --"
			 table="card_type_master" displaycol="card_type" valuecol="card_type_id" value="${bean.map.card_type_id}"/>
		</td>
		<td class="formlabel">Bank:</td>
		<td class="forminfo"><input type="text" name="bank_name" id="paymentBank" value="${bean.map.bank_name}" ${disable}/></td>
		<td class="formlabel">Ref Num:</td>
		<td class="forminfo"><input type="text" name="reference_no" id="paymentRefNum" value="${bean.map.reference_no}" ${disable}/></td>
	</tr>
</table>
</fieldset>

<table class="screenActions">
	<tr>
		<td>
			<input type="submit" value="Save" onclick="return validate();"/>
			<label>|</label>
			<c:if test="${param._method != 'add' && (not empty billingcounterId || not empty pharmacyCounterId)}">
				<a href="./claimReceipts.do?_method=add">New Receipt</a>
				<label>|</label>
			</c:if>
			<a href="${cpath}/billing/claimReceipts.do?_method=getScreen">Back</a>
		</td>
	 </tr>
</table>

</form>
</body>
</html>