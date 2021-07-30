<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Edit Purchase Account Names - Insta HMS</title>
	<insta:link type="js" file="master/accounting/accountingprefs.js"/>

</head>
<body>
	<h1>Edit Sales/Purchase Account Names</h1>
	<insta:feedback-panel/>
	<form action="PurchaseAccounts.do" name="categoryForm">
		<input type="hidden" name="_method" value="update">
		<table class="formtable">
			<tr>
				<td class="formlabel">Category: </td>
				<td><insta:selectdb name="category_id" id="category_id" table="store_category_master"
					value="${category.map.category_id}" displaycol="category" valuecol="category_id" class="validate-not-empty dropdown"/></td>
				<td></td>
				<td></td>
				<td></td>
				<td></td>
			<tr>
			<tr>
				<td class="formlabel">Purchases Category Account Prefix(VAT): </td>
				<td><input type="text" name="purchases_cat_vat_account_prefix" value="${category.map.purchases_cat_vat_account_prefix}"/>
			</tr>
			<tr>
				<td class="formlabel">Purchases Category Account Prefix(CST): </td>
				<td><input type="text" name="purchases_cat_cst_account_prefix" value="${category.map.purchases_cat_cst_account_prefix}"/>
			</tr>
			<tr>
				<td class="formlabel">Sales Category Account Prefix(VAT): </td>
				<td><input type="text" name="sales_cat_vat_account_prefix" value="${category.map.sales_cat_vat_account_prefix}"/>
			</tr>

		</table>
		<div class="screenActions">
			<c:url var="purAccountsUrl" value="PurchaseAccounts.do">
				<c:param name="_method" value="list"/>
			</c:url>
			<button type="submit" name="save" accesskey="S" onclick="return validateAccountHeads();">
			<b><u>S</u></b>ave</button>
			| <a href="${purAccountsUrl}" title="Add Account Prefixes for Category">Sales/Purchases A/c Prefixes</a>
		</div>
	</form>
</body>
</html>