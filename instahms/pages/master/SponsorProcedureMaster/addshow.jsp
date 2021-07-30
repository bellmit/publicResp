<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/Edit Sponsor Procedure - Insta HMS</title>
<insta:link type="css" file="hmsNew.css"/>
<insta:link type="script" file="hmsvalidation.js"/>
<script>
	function doClose() {
		window.location.href = "${cpath}/master/SponsorProcedureMaster.do?_method=list&status=A&sortOrder=tpa_name&sortReverse=false";
	}

</script>
</head>
<body>

<form action="SponsorProcedureMaster.do" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="procedure_no" value="${bean.map.procedure_no}"/>
	</c:if>

	<h1>${param._method == 'add' ? 'Add' : 'Edit'} Sponsor Procedure</h1>

	<insta:feedback-panel/>

	<fieldset class="fieldsetborder">

		<table class="formtable" align="center">
			<tr>
				<td>Sponsor Name:</td>
				<td>
					<insta:selectdb name="tpa_id" table="tpa_master" orderby="tpa_name"
						valuecol="tpa_id" displaycol="tpa_name" filtered="false" value="${bean.map.tpa_id}"/>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td>Procedure Code:</td>
				<td>
					<input type="text" name="procedure_code" value="${bean.map.procedure_code}"
						 class="required validate-length" length="30" title="Procedure code is required and max length of code can be 30"  />
				</td>
			</tr>
			<tr>
				<td>Procedure Name:</td>
				<td>
					<input type="text" name="procedure_name" value="${bean.map.procedure_name}"
						 class="required validate-length" length="150" size="50" title="Procedure name is required and max length of name can be 150"  />
				</td>
			</tr>
			<tr>
				<td>Limit:</td>
				<td>
					<input type="text" name="procedure_limit" value="${bean.map.procedure_limit}"
						 class="number required validate-length" length="22" title="Procedure limit" onkeypress="return enterNumOnlyANDdot(event)"  />
				</td>
			</tr>
			<tr>
				<td>Remarks:</td>
				<td>
					<input type="text" name="remarks" value="${bean.map.remarks}" size="50" length="100"/>
				</td>
			</tr>
			<tr>
				<td>Status</td>
				<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>
		</table>
	</fieldset>
	<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S"><b><u>S</u></b>ave</button></td>
			<c:if test="${param._method=='show'}">
				<td>&nbsp;|&nbsp;</td>
				<td><a href="#" onclick="window.location.href='${cpath}/master/SponsorProcedureMaster.do?_method=add'">Add</a></td>
			</c:if>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();">Sponsor Procedure List</a></td>
		</tr>
	</table>

</form>
</body>
</html>
