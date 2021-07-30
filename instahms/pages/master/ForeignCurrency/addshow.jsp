<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Edit Currency</title>
<insta:link type="script" file="hmsvalidation.js"/>
<script>
	var currencyListJSON = <%= request.getAttribute("currencyList") %>;

	function doClose() {
		window.location.href = "${cpath}/master/ForeignCurrency.do?_method=list&sortOrder=currency&sortReverse=false&status=A";
	}

	var hiddenCurrencyId = '${bean.map.currency_id}';

	function checkDuplicate(){
		var newCurrency = trim(document.currencyform.currency.value);
		for (var i=0;i<currencyListJSON.length;i++) {
			item = currencyListJSON[i];
			if (hiddenCurrencyId!=item.currency_id) {
			   var origCurrency = item.currency;
			    if (newCurrency.toLowerCase() == origCurrency.toLowerCase()) {
			    	alert(document.currencyform.currency.value+" already exists.");
			    	document.currencyform.currency.value='';
			    	document.currencyform.currency.focus();
			    	return false;
			    }
		     }
		}
		return true;
 	 }

 	 function validate() {
 	 	if (!checkDuplicate())
 	 		return false;
		if (!validateDecimal(document.currencyform.conversion_rate, "Conversion rate is invalid."))
			return false;
		return true;
 	 }
</script>

</head>
<body>

<form action="ForeignCurrency.do" method="POST" name="currencyform">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="currency_id" value="${bean.map.currency_id}"/>
	</c:if>

	<div class="pageHeader">${param._method == 'add' ? 'Add' : 'Edit'} Currency</div>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">

	<table class="formtable">
		<tr>
			<td class="formlabel">Currency:</td>
			<td>
				<input type="text" name="currency" value="${bean.map.currency}"
					onblur="capWords(currency);checkDuplicate();" class="required validate-length"
					length="300" title="Currency is required and max length of currency can be 300" />
			</td>
			<td></td>
			<td></td>
			<td></td>
			<td></td>
		</tr>
		<tr>
			<td class="formlabel">Conversion Rate</td>
			<td>
				<input type="text" name="conversion_rate" value="${bean.map.conversion_rate}"
					class="number required" title="Conversion rate is required"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Status</td>
			<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
		</tr>

	</table>

	</fieldset>

		<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S" onclick="return validate()"><b><u>S</u></b>ave</button></td>
			<c:if test="${param._method=='show'}">
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="window.location.href='${cpath}/master/ForeignCurrency.do?_method=add'">Add</a>
			</c:if>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();">Currency List</a></td>
		</tr>
	</table>

</form>

</body>
</html>
