<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<%@page import="com.insta.hms.stores.StoresDBTablesUtil"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Reagent Master - Insta HMS</title>
<insta:link type="script" file="ajax.js" />
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="masters/diagnosticReagentMaster.js" />

	<script>

		var cpath = '${cpath}';
		var method = '${ifn:cleanJavaScript(param._method)}';
		var test_id = '${ifn:cleanJavaScript(param._test_id)}';
		var itList = <%=StoresDBTablesUtil.getTableDataInJSON(StoresDBTablesUtil.CONSUMABLE_ITEMS) %>;

        <c:if test="${param._method != 'add'}">
		     Insta.masterData=${testsLists};
		</c:if>

	</script>

</head>
<body onload="fillReagents();">
<c:choose>
     <c:when test="${param._method != 'add'}">
         <h1 style="float:left">Edit Diagnostic Reagent</h1>
         <c:url var="searchUrl" value="/master/DiagnosticReagentMaster.do"/>
         <insta:findbykey keys="test_name,test_id" fieldName="test_id" method="show" url="${searchUrl}"/>
     </c:when>
     <c:otherwise>
         <h1>Add Diagnostic Reagent</h1>
     </c:otherwise>
</c:choose>

<form action="DiagnosticReagentMaster.do" name="diagnosticReagentMasterForm" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:set var="update" value=""/>
	<c:set var="active" value=""/>
	<c:set var="status" value="hidden"/>
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="reagent_name" value=""/>
		<input type="hidden" name="test_name" value="${bean[0].map.test_id}"/>
		<c:set var="update" value="disabled"/>
		<c:set var="status" value="visible"/>
	</c:if>
	<insta:feedback-panel/>

	<fieldset class="fieldSetBorder">
	<table class="formtable">
		<tr>
			<td class="formlabel"><table width="75%"><tr><td class="formlabel">Test Name:</td>
			<td class="formlabel">
				<div id="testDiv" align="left">
					<select name="test_id" id="test_id" class="dropdown">
						<option value="">...Test Name...</option>
						<c:forEach var="tests" items="${test_list}">
							<option value="${tests.TEST_ID}">${tests.TEST_NAME}</option>
						</c:forEach>
					</select>
				</div>

				<div align="left" id="testLabel" style="display: none;">
					<label><b>${bean[0].map.test_name}</b></label>
				</div>
			</td>
			<td width="370" style="visibility: ${ifn:cleanHtmlAttribute(status)}" align="right">Inactive:</td><td style="visibility: ${ifn:cleanHtmlAttribute(status)}"><input type="checkbox" name="status" id="status" ${active } /></td>
			</tr></table>
		</tr>
		<tr><td><fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Add Reagents</legend>
			<table>
			<tr>(only items of issue type consumable can be added here)</tr>
			<tr><td class="formlabel">Category: </td>
			<td><insta:selectdb name="category_id" table="store_category_master" valuecol="category_id" displaycol="category" orderby="category"
					dummyvalue="....Category...." onchange="getReagentItems();" filtered="true"  filtercol="issue_type" filtervalue="C"/></td>
					<td class="formlabel">Reagent Name:</td>
			<td>
			<select name="reagent_id" id="reagent_id" class="dropdown" onchange="setIsuueDetails(this.value);" >
				<option>...Reagent...</option>
			</select>
			</td>
			<td class="formlabel">Unit UOM:</td>
			<td id="issueqty" style="font-weight: bold"></td>

			<td class="formlabel">Quantity Needed:</td>
			<td>
				<input type="text"  class="number" name="quantity_needed" id="quantity_needed" maxlength="100" onkeypress="return enterNumAndDot(event);" onblur="return makeingDec(this.value,this)" />
			</td>
			<td>
				<input type="button" name="add" id="name" value="Add" onclick="return addReagents(test_id,reagent_id,quantity_needed)" />
			</td></tr></table></fieldset></td>
		</tr>
	</table>
	</fieldset>

	<fieldset class="fieldSetBorder">
	<table id="reagentstable" class="delActionTable" border="0" cellspacing="0" cellpadding="0" width="35%">
		<tr class="header">
			<td class="first">Reagent Name</td>
			<td>Qty</td>
			<td class="last">&nbsp;</td>
		</tr>
	</table>
	</fieldset>


	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return checkduplicate();"><b><u>S</u></b>ave</button>
		|
		<c:if test="${param._method != 'add'}">
			<a href="javascript:void(0)" onclick="window.location.href='${cpath}/master/DiagnosticReagentMaster.do?_method=add'">Add</a>
		|
		</c:if>
		<a href="javascript:void(0)" onclick="doClose();return true;">Diagnostic Reagent List</a>

	</div>

</form>

<c:if test="${param._method != 'add'}">
	<script type="text/javascript">

		var reagents = ${reagents};
	</script>
</c:if>

</body>
</html>
