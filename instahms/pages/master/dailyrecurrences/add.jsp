<%@page import="com.insta.hms.master.URLRoute"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Add Recurrence - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="js" file="master/recurrencedailymaster/recurrence_daily_master.js" />
<jsp:useBean id="presentDate" class="java.util.Date"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="pagepath" value="<%= URLRoute.RECURRENCE_DAILY_MASTER_PATH %>" />
<script>
	var cpath = '${cpath}';
	var diplayNames = ${ifn:convertListToJson(displaynames)};

</script>
</head>
<body onload="init();">
	<form name="recurrenceDailyMaster" action="create.htm" method="POST">
	<input type="hidden" name="num_activities" id="num_activities" value=""/>
	<c:set var="date"><fmt:formatDate value="${presentDate}" pattern="dd-MM-yyyy hh:mm"/></c:set>
	<input type="hidden" name="mod_time" id="mod_time" value="${ifn:cleanHtmlAttribute(date)}"/>
	<c:set var="readOnly" value="false"/>
	<c:set var="ReadOnly" value="${readOnly  ? 'disabled' : ''}"/>
	
	<h1>Add Recurrence Details</h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Display Name:</td>
				<td><div id="autocomplete" style="padding-bottom: 20px;">
						<input type="text" name="display_name" id="display_name" style="width:138px;"
							value="" ${ReadOnly}/>
						<div id="displaycontainer" style="width:240px;"></div>
					</div>
					<input type="hidden" name="recurrence_daily_id" id="recurrence_daily_id" value=""/>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Timings:</td>
				<td>
					<textarea name="timings" id="timings" style="width: 138px;" onblur="countNoOfActivities()"></textarea>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" value="" opvalues="A,I" optexts="Active,Inactive" /></td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Medication Type:</td>
				<td><insta:selectoptions name="medication_type" value="" opvalues="M,IV,A" optexts="All Medicines,Only IV,Only Additive" /></td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">No.Of Activities:</td>
				<td>
					<label class="forminfo" id="noofact"></label>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Display Order:</td>
				<td>
					<input type="text" name="display_order" id="display_order" value=""
						onkeypress="return enterNumOnlyzeroToNine(event)">
				</td>
			</tr>
		</table>
	</fieldset>
		<div class="screenActions">
		<button type="button" accesskey="S" onclick="return doSave()"><b><u>S</u></b>ave</button>
		| <a href="list.htm?sortOrder=display_name&sortReverse=false&status=A">Recurrence Daily List</a>
		</div>
	</form>
</body>
</html>
