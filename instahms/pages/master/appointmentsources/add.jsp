<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Channeling Appointment Source - Insta HMS</title>
<insta:link type="css" file="hmsNew.css"/>
<insta:link type="script" file="hmsvalidation.js"/>
<script>
		function doClose() {
			window.location.href = "${cpath}/master/appointmentsources/list.htm?status=A&sortOrder=appointment_source_name&sortReverse=false";
		}
</script>
</head>
<body>
    <c:set var="actionAttr" value="create.htm"/>

	<form action="${actionAttr}" method="POST">

		<h1>Add Channeling Appointment Source</h1>
		<insta:feedback-panel/>
		<input type="hidden" name="appointment_source_id" value="${bean.appointment_source_id}"/>

		<fieldset class="fieldsetborder">

			<table class="formTable">
				<tr>
					<td class="formlabel">Appointment Source Name:</td>
					<td>
						 <input type="text" name="appointment_source_name" value="${bean.appointment_source_name}"
						 	class="required" title='Channeling Source Name is required' maxlength="50"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Status:</td>
					<td>
						 <insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive"/>
					</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td class="formlabel">Paid at source:</td>
					<td>						 
						 <insta:radio name="paid_at_source" radioValues="Y,N" value="${empty(bean.paid_at_source) ? 'N' : bean.paid_at_source}" radioText="Yes,No" radioIds="Y,N" />
					</td>
				</tr>
			</table>

		</fieldset>

		<table class="screenActions">
			<tr>
				<td><button type="submit" accesskey="S"><b><u>S</u></b>ave</button></td>
				<td>&nbsp;|&nbsp;</td>
				<td><a href="javascript:void(0)" onclick="doClose();">Appointment Source List</a></td>
			</tr>
		</table>
	</form>
</body>
</html>
