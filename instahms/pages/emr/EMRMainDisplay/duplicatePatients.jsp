<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<div id="duplicatePatientsDiv" style="display: none; margin: top: 10px;width: 400px">
	<div class="bd" >
		<fieldset class="fieldSetBorder" style="width: 400px">
			<legend class="fieldSetLabel">Duplicate Patient List</legend>
				<div style="float:right; margin-top: 10px; margin-right: 10px" id="paginationDiv" name="paginationDiv">
				</div>
				<table class="resultList" id="dupPatientTable" cellspacing="0" cellpadding="0" style="width: 400px">
					<tr>
						<th>Mr No</th>
						<th>Patient Name</th>
						<th>Age/Gender</th>
					</tr>
					<tr style="display:none">
						<td><label></label></td>
						<td><label></label></td>
						<td><label></label></td>
					</tr>
					<tr style="display: none; background-color:#FFC">
						<td colspan="4"><img src="${cpath}/images/alert.png"/> No duplicate patients exists.</td>
					</tr>
				</table>
		</fieldset>
		<table >
			<tr>
				<td><input type="button" name="dup_btn" id="dup_btn" value="Close"/></td>
			</tr>
		</table>
	</div>
</div>