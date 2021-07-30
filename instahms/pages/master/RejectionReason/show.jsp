<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Add/Edit Rejection Reason Category- Insta HMS</title>
</head>
<body>
	<h1>Edit Rejection Reason Category</h1>
	<insta:feedback-panel/>
	<form action="update.htm" method="POST">
		<%-- <input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}"/> --%>
		<input type="hidden" name="rejection_reason_category_id" value="${bean.rejection_reason_category_id}">
		<fieldset class="fieldSetBorder">
			<table class="formtable">
				<tr>
					<td style="width: 380px" class="formlabel">Rejection Reason Category:</td>
					<td><input type="text" name="rejection_reason_category_name" value="${bean.rejection_reason_category_name}" 
						class="required" title="Rejection reason Category is mandatory." maxlength="250"></td>
					<td class="formlabel"></td>
					<td></td>
					<td class="formlabel"></td>
					<td></td>
					<td class="formlabel">Status: </td>
					<td><Select name="status" class="dropdown validate-not-empty" title="Status is mandatory.">
							<option value="">-- Select --</option>
							<option value="A" ${bean.status == 'A' ? 'selected' : ''}>Active</option>
							<option value="I" ${bean.status == 'I' ? 'selected' : ''}>Inactive</option>
						</Select>
					</td>
					<td class="formlabel"></td>
					<td></td>
					<td class="formlabel"></td>
					<td></td>
				</tr>
			</table>
			<table style="margin-top: 10px">
				<tr>
					<td>
						<input type="submit" name="Save" value="Save"/>					
						| <a href="add.htm?">Add</a>						
						| <a href="list.htm?status=A&sortOrder=rejection_reason_category_name&sortReverse=false">List</a>
					</td>
				</tr>
			</table>
		</fieldset>
	</form>
</body>
</html>