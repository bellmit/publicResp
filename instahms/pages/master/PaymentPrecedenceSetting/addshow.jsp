<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Payment Preferences - Insta HMS</title>
<insta:link type="js" file="masters/paymentprecedence.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<script>
</script>

<style>
 div.helpText{width:210px}
</style>

</head>
<body>
	<h1>Payment Preferences</h1>

	<insta:feedback-panel/>
<form action="PaymentPrecedenceSetting.do?method=${param.method == 'add'?'create' : 'update'}"
		method="POST" >

	<fieldset class="fieldSetBorder">
		<table class="formtable">
		<tr>
			<td class="formlabel">Discount Amount:</td>
			<td><insta:selectoptions name="discounted_amount" value="${bean.map.discounted_amount}" opvalues="1,0" optexts="Yes,No"/>
			</td>
			<td>
			<img class="imgHelpText" src="${cpath}/images/help.png" title="Specify whether the discounted amount or the pre discounted charge amount should be used to calculate the payout."/>
			</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
		</tr>
		</table>
	</fieldset>
	<div class="screenActions"><button type="submit" accesskey="S" name="submit" onclick="return validateForm();">
	<b><u>S</u></b>ave</button>
	</div>
</form>
</body>
</html>
