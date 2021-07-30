<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<%@page import="com.insta.hms.stores.StoresDBTablesUtil"%>
<%@page import="com.insta.hms.master.ServiceSubGroup.ServiceSubGroupDAO"%>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add Issue Rate - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

<insta:link type="script" file="ajax.js" />

<style>
.grid{margin:5px 7px 5px 7px;padding:5px 7px 5px 7px;}
input.num {text-align: right; width: 6em;}

.scrolForContainer .yui-ac-content{
	 max-height:18em;overflow:auto;overflow-x:auto; /* scrolling */
    _height:18em; max-width:35em; width:35em;/* ie6 */
}

</style>
<c:set var="prefbarcode" value="<%= GenericPreferencesDAO.getGenericPreferences().getBarcodeForItem() %>"/>
	<script>

		var hidden_id = 'medicine_id';
		Insta.masterData = [{"item_code":null,"medicine_id":null,"medicine_name":null}];
		<c:if test="${param._method == 'add'}">
	 		Insta.masterData = ${itemList};
	 	</c:if>
	 	var parameters = ["medicine_name", "medicine_id"];

		function setIds(oself, elItem) {
			var record = elItem[2];
			document.getElementById(hidden_id).value = record[parameters[1]];
			var url = '${cpath}/master/StoreItemIssueRateMaster.do?_method=getItemMaxMRP&medicine_id='+record[parameters[1]];
			Ajax.get(url, setMaxMRP);
		}

	 	function init() {
	 		if ( '${ifn:cleanJavaScript(param._method)}' == 'add' )
		    	Insta.mastersAutocomplete('medicine_name', 'medicine_dropdown');
		}

		function setMaxMRP(responseText, status) {
			var map = (eval( "(" + responseText + ")"));
			document.getElementById('mrp').textContent = map.max_mrp;
			document.getElementById('cp').textContent = map.avg_cp;

		}

		function validate() {
			if ( document.forms[0].medicine_name.value == '' ) {
				alert('Enter item name');
				return false;
			}

			if ( document.forms[0].issue_rate_expr.value == '' ) {
				alert('Enter issue rate expression');
				return false;
			}
		}

	</script>
</head>

<body onload="init();" class="yui-skin-sam">
	<c:choose>
	    <c:when test="${param._method != 'add'}">
	        <h1>Edit Issue Rate</h1>
	    </c:when>
	    <c:otherwise>
	         <h1>Add Issue Rate </h1>
	    </c:otherwise>
	</c:choose>

<form action="StoreItemIssueRateMaster.do" name="storesratemasterform" method="POST">

	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">

	<insta:feedback-panel/>

<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel"> Item Details</legend>
	<table class="formtable">
		<tr>
			<td class="formlabel">Item Name:</td>
			<td>
				<c:if test="${param._method eq 'add'}">
					<div id="medicine_wrapper">
						<input type="text" name="medicine_name" id="medicine_name" style="width: 18em" value=""/>
						<div id="medicine_dropdown" class="scrolForContainer"></div>
						<span class="star">*</span>
					</div>
				</c:if>
				<c:if test="${param._method ne 'add'}">
					<input type="text" name="medicine_name" id="medicine_name" style="width: 18em"
							value="<c:out value='${bean.map.medicine_name}'/>" readonly/>
				</c:if>
				<input type="hidden" name="medicine_id" id="medicine_id" value="${bean.map.medicine_id}"/>
			</td>
			<td class="formlabel">Avg Cost Price:</td>
			<td class="forminfo" id="cp">${bean.map.avg_cp}</td>
			<td class="formlabel">Max MRP:</td>
			<td class="forminfo" id="mrp">${bean.map.max_mrp}</td>
		</tr>
		<tr>
			<td class="formlabel">Issue Rate Expr:<span class="star">*</span></td>
			<td colspan="4">
				<textarea cols="150" rows="5" name="issue_rate_expr" id="issue_rate_expr">${bean.map.issue_rate_expr}</textarea>
				
			</td>
			<td></td><td></td>
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
					<td colspan="7"><b> Sample expression:</b>
					<#if center_id == 1>\${package_cp+25*package_cp}<#else>\${package_cp+15*package_cp} &lt;/#if>
					</td>
				</tr>
				<tr>
					<td colspan="7"><b>Available Tokens:</b></td>
				</tr>
				<tr>
					<c:forEach items="${exprTokens}" var="token" varStatus="it">
					<td>${token}</td>
					</c:forEach>
				</tr>
			</table>
		</div>
	</dd>
</dl>


<div class="screenActions">
	<input type="submit" name="save" value="Save" class="button" onclick="return validate();">
	|
	<c:if test="${param._method != 'add'}">
	<a href="javascript:void(0);" onclick="window.location.href='${cpath}/master/StoreItemIssueRateMaster.do?_method=add'">Add</a>
	|
	</c:if>
	<a href="${cpath }/master/StoreItemIssueRateMaster.do?_method=list&sortOrder=medicine_name&sortReverse=false&status=A">Back To DashBoard</a>
</div>

</form>
</body>
</html>
