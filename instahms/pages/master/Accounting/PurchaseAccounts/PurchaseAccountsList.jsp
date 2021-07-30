<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Purchase Account Names - Insta HMS</title>
	<insta:link type="js" file="master/accounting/accountingprefs.js"/>
	<script>
		var toolbar = {
			Edit : { title: "Edit Account Names", imageSrc: "icons/Order.png",  href: 'master/Accounting/PurchaseAccounts.do?_method=show'}
		};
		function init() {
			createToolbar(toolbar);
		}
	</script>

</head>
<body onload="init();">
	<h1>Purchase Account Names</h1>
	<insta:feedback-panel/>
	<c:set var="categoryAccountNames" value="${pagedList.dtoList}"/>
	<form action="PurchaseAccounts.do" name="categoryForm">
		<input type="hidden" name="_method" value="list">
		<input type="hidden" name="_searchMethod" value="list">
		<insta:search-lessoptions  form="categoryForm" >
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField">
						<div class="sboFieldLabel">Category Name</div>
						<div class="sboFieldInput">
							<insta:selectdb name="category_id" id="category_id" table="store_category_master"
								value="${param.category_id}" dummyvalue="-- Select --" dummyvalueid="" displaycol="category" valuecol="category_id"/>
							<input type="hidden" name="category_id@cast" value="y"/>
						</div>
					</td>
					<td class="sboField">
						<div class="sboFieldLabel">Purchases Category Account Prefix(VAT)</div>
						<div class="sboFieldInput" style="height:50px">
							<input type="text" name="purchases_cat_vat_account_prefix" id="purchases_cat_vat_account_prefix"
							 value="${ifn:cleanHtmlAttribute(param.purchases_cat_vat_account_prefix)}"/>
						    <input type="hidden" name="purchases_cat_vat_account_prefix@op" value="ilike"/>
						</div>
					</td>
					<td class="sboField">
						<div class="sboFieldLabel">Purchases Category Account Prefix(CST)</div>
						<div class="sboFieldInput" style="height:70px">
							<input type="text" name="purchases_cat_cst_account_prefix" id="purchases_cat_cst_account_prefix"
							 value="${ifn:cleanHtmlAttribute(param.purchases_cat_cst_account_prefix)}" />
							<input type="hidden" name="purchases_cat_cst_account_prefix@op" value="ilike"/>
						</div>
					</td>
					<td class="sboField">
						<div class="sboFieldLabel">Sales Category Account Prefix(VAT)</div>
						<div class="sboFieldInput" style="height:70px">
							<input type="text" name="sales_cat_vat_account_prefix" id="sales_cat_vat_account_prefix"
							 value="${ifn:cleanHtmlAttribute(param.sales_cat_vat_account_prefix)}"/>
							<input type="hidden" name="sales_cat_vat_account_prefix@op" value="ilike"/>
						</div>
					</td>
				</tr>
			</table>
		</insta:search-lessoptions>
		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
		<div class="resultList" >
			<table class="resultList" id="resultTable" cellspacing="0" cellpadding="0" onmouseover="hideToolBar('');">
				<tr>
					<th>Category</th>
					<th>Purchases Category Account Prefix(VAT)</th>
					<th>Purchases Category Account Prefix(CST)</th>
					<th>Sales Category Account Prefix(VAT)</th>
				</tr>
				<c:forEach items="${categoryAccountNames}" var="category" varStatus="st">
					<tr onclick="showToolbar(${st.index}, event, 'resultTable',
						{category_id: '${category.category_id}'}, [true]);"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
						<td>${ifn:cleanHtml(category.category)}</td>
						<td>${category.purchases_cat_vat_account_prefix}</td>
						<td>${category.purchases_cat_cst_account_prefix}</td>
						<td>${category.sales_cat_vat_account_prefix}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
		<insta:noresults hasResults="${not empty categoryAccountNames}"/>
	</form>
</body>
</html>