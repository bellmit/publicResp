<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@page import="com.insta.hms.master.URLRoute"%>
<c:set var="pagePath" value="<%=URLRoute.COUNTER_PATH %>"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Edit Counters - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

	<script>
		var backupName = '';

		function keepBackUp(){
			backupName = document.counterForm.counter_no.value;
		}

		function doClose() {
			window.location.href = "${cpath}${pagePath}/list.htm?sortOrder=counter_no" +
						"&sortReverse=false&status=A";
		}
		function focus(){
			document.counterForm.counter_no.focus();
		}

		function validate() {
			var centerId = document.getElementById('center_id').value;
			if (centerId == '') {
				alert("Please select the center.");
				document.getElementById('center_id').focus();
				return false;
			}
			return true;
		}
	</script>

</head>
<body onload="focus(); keepBackUp();">
  <h1>Add Counter</h1>
<form action="create.htm" name="counterForm" method="POST">
	
	
	<insta:feedback-panel/>

	<fieldset class="fieldSetBorder">
	<table class="formtable">
		<tr>
			<td class="formlabel">Counter:</td>
			<td>
				<input type="text" name="counter_no" value="${bean.counter_no}" onblur="capWords(counter_no)" class="required validate-length" length="100" title="Name is required and max length of name can be 100" />
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</td>
		</tr>

		<tr>
			<td class="formlabel">CounterType:</td>
			<td>
				<insta:selectoptions name="counter_type" value="${bean.counter_type}" opvalues="B,P" optexts="Billing counter,Pharmacy counter" />
			</td>
		</tr>

		<tr>
			<td class="formlabel">Collection Counter:</td>
			<td>
				<insta:selectoptions name="collection_counter" value="${bean.collection_counter}"
					opvalues="Y,N" optexts="Yes,No" />
			</td>
		</tr>

		<tr>
			<td class="formlabel">Status</td>
			<td><insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
		</tr>
		<tr>
			<c:choose>
				<c:when test="${max_centers_inc_default == 1}">
					<input type="hidden" name="center_id" id="center_id" value="0"/>
				</c:when>
				<c:otherwise>
					<td class="formlabel">Center: </td>
					<td class="forminfo">
						<select class="dropdown" name="center_id" id="center_id">
							<option value="">-- Select --</option>
							<c:forEach items="${centers}" var="center">
								<c:if test="${center.center_id != 0}">
									<option value="${center.center_id}">${center.center_name}</option>
								</c:if>
							</c:forEach>
						</select>
					</td>
				</c:otherwise>
			</c:choose>
		</tr>

	</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validate();"><b><u>S</u></b>ave</button>
		|
		<a href="javascript:void(0)" onclick="doClose();">Counter List</a>
	</div>

</form>

</body>
</html>
