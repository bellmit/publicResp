<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page import="com.insta.hms.master.URLRoute"%>
<%@page import="com.insta.hms.stores.StoresDBTablesUtil"%>
<%@page import="com.insta.hms.master.ServiceSubGroup.ServiceSubGroupDAO"%>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.STOCK_ADJUSTMENT_REASON_MASTER_PATH %>"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add Stock Adjustment Reason - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

<insta:link type="script" file="ajax.js" />

<style>
.grid{margin:5px 7px 5px 7px;padding:5px 7px 5px 7px;}
input.num {text-align: right; width: 6em;}

</style>
<script>
	var StockAdjust = ${ifn:convertListToJson(StockAdjust)};

	function validate() {
		if (trim(document.forms[0].adjustment_reason.value) == '' ) {
			alert('Enter Stock Adjustment Reason ');
			return false;
		}

		stockadjustreason=document.stockadjustmentreasonform;
		stockadjustment=stockadjustreason.adjustment_reason.value;

       if (stockadjustment.length > 1000){
           alert("The field cannot contain more than 1000 characters!")
             return false
        }else {
             return true
       }

	 }

</script>
</head>

<body  class="yui-skin-sam">
<h1>Add Stock Adjustment Reason</h1>
<insta:feedback-panel/>

<form action="create.htm" name="stockadjustmentreasonform" method="POST">
	
<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Stock Adjustment Reason Details</legend>
	<table class="formtable" style="margin-left:0px;">
		<tr>
			<td class="formlabel">Adjustment Reason:</td>
			<td colspan="3">
				<textarea name="adjustment_reason" id="adjustment_reason" maxlength="1000" rows="4" cols="52" title="Reason is required - max length 1000" >${bean.map.adjustment_reason}</textarea>
                    <span class="star">*</span>

			</td>
		</tr>
		<tr>

            <td class="formlabel">Status:</td>
				<td>
					<insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I"
					optexts="Active,Inactive" />
				</td>
		</tr>


	</table>
</fieldset>


<div class="screenActions">
	<input type="submit" name="save" value="Save" class="button" onclick="return validate();">
	|
	<a href="${cpath}/${pagePath}/list.htm?sortOrder=adjustment_reason&sortReverse=false&status=A">Back To DashBoard
</div>

</form>
</body>
</html>
