<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page import="com.insta.hms.stores.StoresDBTablesUtil"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="method" value="${param._method}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Service Consumables Master - Insta HMS</title>
<insta:link type="script" file="ajax.js" />
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="masters/serviceConsumable.js"/>
<script>
	var cpath = '${cpath}';
	var method = '${ifn:cleanJavaScript(param._method)}';
	var service_id = '${ifn:cleanJavaScript(param.service_id)}';
	var itList = <%=StoresDBTablesUtil.getTableDataInJSON(StoresDBTablesUtil.CONSUMABLE_ITEMS) %>;

	<c:if test="${param._method != 'add'}">
	     Insta.masterData=${serviceconsumablesLists};
	</c:if>
</script>
</head>
<body onload="fillReagents();">

<c:choose>
    <c:when test="${param._method != 'add'}">
        <h1 style="float:left">Edit Service Consumable</h1>
        <c:url var="searchUrl" value="/master/ServiceConsumableMaster.do"/>
        <insta:findbykey keys="service_name,service_id" fieldName="service_id" method="show" url="${searchUrl}"/>
    </c:when>
    <c:otherwise>
         <h1>Add Service Consumable</h1>
    </c:otherwise>
</c:choose>
<form action="ServiceConsumableMaster.do" name="serviceConsumableMasterForm"  method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:set var="update" value=""/>
	<c:set var="active" value=""/>
	<c:set var="status" value="hidden"/>
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="consumable_name" value=""/>
		<input type="hidden" name="service_name" value="${bean[0].map.service_id}"/>
		<c:set var="update" value="disabled"/>
		<c:set var="status" value="visible"/>
	</c:if>
	<insta:feedback-panel/>

	<fieldset class="fieldSetBorder">
	<table class="formtable">
		<tr>
			<td >
				<table width="75%">
					<tr><td class="formlabel">Service Name:</td>
						<td class="formlabel">
							<div id="testDiv" align="left">
								<select name="service_id" id="service_id" class="dropdown">
									<option value="">...Service Name...</option>
									<c:forEach var="serv" items="${services_list}">
										<option value="${serv.SERVICE_ID}">${serv.SERVICE_NAME}</option>
									</c:forEach>
								</select>
							</div>

							<div align="left" id="testLabel" style="display: none;">
								<label><b>${bean[0].map.service_name} </b></label>
							</div>
						</td>
						<td width="370" style="visibility: ${ifn:cleanHtmlAttribute(status)}" align="right">Inactive:</td>
						<td style="visibility: ${ifn:cleanHtmlAttribute(status)}"><input type="checkbox" name="status" id="status" ${active} /></td>
					</tr>
				</table>
		</tr>
		<tr><td><fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Add Consumables</legend>
			<table>
			<tr>(only items of issue type consumable can be added here)</tr>
			<tr><td class="formlabel">Category: </td>
			<td><insta:selectdb name="category_id" table="store_category_master" valuecol="category_id" displaycol="category"
					dummyvalue="....Category...." onchange="getConsumableItems();" filtered="true"  filtercol="status,issue_type" filtervalue="A,C"
					orderby="category"/></td>
			<td class="formlabel">Consumable Name:</td>
			<td>
			<select name="consumable_id" id="consumable_id" class="dropdown" onchange="setIsuueDetails(this.value);" >
				<option>...Consumable...</option>
			</select>
			</td>
			<td class="formlabel">Unit UOM:</td>
			<td id="issueqty" style="font-weight: bold"></td>

			<td class="formlabel">Quantity Needed:</td>
			<td>
				<input type="text" class="number" name="quantity_needed" id="quantity_needed" maxlength="100" />
			</td>
			<td>
				<input type="button" name="add" id="name" value="Add" onclick="return addReagents(service_id,consumable_id,quantity_needed)" />
			</td></tr></table></fieldset></td>
		</tr>
	</table>
	</fieldset>

	<fieldset class="fieldSetBorder">
	<table  class="delActionTable" border="0" cellspacing="0" cellpadding="0" id="reagentstable" width="40%">
		<tr class="header">
			<td class="first">Consumable Name</td>
			<td>Qty</td>
			<td class="last">&nbsp;</td>
		</tr>
	</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return checkduplicate();"><b><u>S</u></b>ave</button>
		|
		<c:if test="${param._method != 'add'}">
			<a href="javascript:void(0)" onclick="window.location.href='${cpath}/master/ServiceConsumableMaster.do?_method=add'">Add</a>
		|
		</c:if>
		<a href="javascript:void(0)" onclick="doClose();return true;">Consumable List</a>
	</div>

</form>

<c:if test="${param._method != 'add'}">
	<script type="text/javascript">
		var consumables = ${consumables};
	</script>
</c:if>

</body>
</html>
