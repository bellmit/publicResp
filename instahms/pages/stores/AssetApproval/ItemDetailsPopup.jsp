<%@page import="org.apache.struts.Globals" %>
<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>

<html>
<head>
	<title>Item Purchase Details - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
    <insta:link type="script" file="hmsvalidation.js"/>
    <jsp:include page="/pages/yuiScripts.jsp"/>
    <c:set var="prefVat" value="<%= GenericPreferencesDAO.getGenericPreferences().getShowVAT() %>" />

</head>
<body>
<h1>Item Purchase Details</h1>
<form>
       <table class="formtable" cellpadding="0" cellspacing="0" width="70%" align="center">
        <tr>
        <td class="formlabel">Item Name :</td><td class="forminfo"> ${itemName}</td>
        <td class="formlabel">Batch/Serial No :</td><td  class="forminfo"> ${identifier}</td>
        <td class="formlabel">Stock Type :</td><td class="forminfo"> ${stock}</td>
        </tr>
        </table>
<fieldset class="fieldSetBorder" >
	<legend class="fieldSetLabel">Item Details</legend>
	<table class="detailList dialog_displayColumns" width="100%" cellspacing="0" cellpadding="0" id="table1">
		<tr >
        <th >Supplier</th>
        <th >Cost Price</th>
        <th >MRP</th>
        <c:if test="${prefVat eq 'Y'}">
        <th>VAT Rate</th>
        </c:if>
        <th >Invoice number</th>
        <th >Invoice Date</th>
        <th >GRN number</th>
        <th >GRN Date</th>
        <th >PO number</th>
        </tr>
        <c:forEach items="${itemList}" var="item" varStatus="status">
		<c:set var="i" value="${status.index + 1}"/>
		<tr>
		<td> ${item.map.supplier_name}</td>
		<td> ${item.map.cost_price}</td>
		<td> ${item.map.mrp}</td>
		<c:if test="${prefVat eq 'Y'}">
		<td> ${item.map.tax_rate} %</td>
		</c:if>
		<td> ${item.map.invoice_no}</td>
		<td> <c:forTokens items="${item.map.invoice_date}" var="idate" delims="-" varStatus="status">
				<c:choose>
					<c:when test="${status.index == 0}">
						<c:set var="yyyy" value="${idate}"/>
					</c:when>
					<c:when test="${status.index == 1}">
						<c:set var="mm" value="${idate}"/>
					</c:when>
					<c:when test="${status.index == 2}">
						<c:set var="dd" value="${idate}"/>
					</c:when>
				</c:choose>
			</c:forTokens><c:out value="${dd}"/>-<c:out value="${mm}"/>-<c:out value="${yyyy}"/></td>
		<td> ${item.map.grn_no}</td>
		<td> <c:forTokens items="${item.map.grn_date}" var="gdate" delims="-" varStatus="status">
				<c:choose>
					<c:when test="${status.index == 0}">
						<c:set var="yyyy" value="${gdate}"/>
					</c:when>
					<c:when test="${status.index == 1}">
						<c:set var="mm" value="${gdate}"/>
					</c:when>
					<c:when test="${status.index == 2}">
						<c:set var="dd" value="${gdate}"/>
					</c:when>
				</c:choose>
			</c:forTokens><c:out value="${dd}"/>-<c:out value="${mm}"/>-<c:out value="${yyyy}"/></td>
		<td> ${item.map.po_no}</td>
		</tr>
		</c:forEach>
	</table>
	</div>
	</fieldset>



</form>
</body>
</html>
