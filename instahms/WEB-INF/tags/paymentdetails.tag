<%@tag body-content="empty" dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<%@attribute name="remarks" required="false" %>
<%
request.setAttribute("paymentModesJSON", new flexjson.JSONSerializer().serialize(
	com.insta.hms.common.ConversionUtils.listBeanToListMap(new com.insta.hms.master.PaymentModes.PaymentModeMasterDAO().listAll())));

%>
<script type="text/javascript">
	var jPaymentModes = <%= request.getAttribute("paymentModesJSON") %>;
	
	function checkPayMode(mode){
		var optionText = mode.options[mode.selectedIndex].text;
		var optionValue = mode.value;
		if(optionValue==-2 || optionValue==-3 || optionValue==-4 || optionValue==-5){
			alert(optionText+" can not be used as mode for such payments.")
			document.getElementById('paymentModeId').value=-1;
			onChangePaymentMode();
		}
		
	}
</script>


<c:if test="${empty remarks}"><c:set var="remarks" value="false"/></c:if>

<tr>
	<td id="payModeLabelTd" class="formlabel">Payment Mode:</td>
	<td id="payModeTd" class="forminfo">
		<insta:selectdb name="paymentModeId" id="paymentModeId" onchange="onChangePaymentMode();checkPayMode(this);" orderby="displayorder"
			 table="payment_mode_master" displaycol="payment_mode" valuecol="mode_id" value="-1"/>
	</td>
	<td class="formlabel">Narration:</td>
	<td class="forminfo">
		<input type="text" name="paymentRemarks" id="paymentRemarks"/>
	</td>
	<c:choose>
		<c:when test="${remarks}">
			<td class="formlabel">Remarks:</td>
			<td class="forminfo">
				<input type="text" name="userRemarks" id="userRemarks"/>
			</td>
		</c:when>
		<c:otherwise>
			<td></td>
			<td></td>
		</c:otherwise>
	</c:choose>
</tr>
<tr id="payRefsTr">
	<td class="formlabel">Card Type:</td>
	<td class="forminfo">
		<insta:selectdb name="cardTypeId" id="cardTypeId" dummyvalue="-- Select --" disabled="disabled"
			 table="card_type_master" displaycol="card_type" valuecol="card_type_id"/>
			 <span class="star" id="cardTypeMandatory" style="display: none;">*</span>
	</td>
	<td class="formlabel">Bank:</td>
	<td class="forminfo">
		<insta:selectdb name="paymentBank" id="paymentBank" dummyvalue="-- Select --" disabled="disabled"
			 table="bank_master" displaycol="bank_name" valuecol="bank_name" orderby="bank_name"/>
			 <span class="star" id="bankMandatory" style="display: none;">*</span>
	</td>
	<td class="formlabel">Ref Number:</td>
	<td class="forminfo">
		<input type="text" name="paymentRefNum" id="paymentRefNum" disabled/>
		<span class="star" id="refNumberMandatory" style="display: none;">*</span>
	</td>
</tr>
