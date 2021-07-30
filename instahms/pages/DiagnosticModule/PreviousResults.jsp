<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<div id="previousResultsDiv" style="display: none">
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Previous Results</legend>
			<div style="margin-top: 10px;">
				Test Result : <label id="testResultLabel" style="font-weight: bold;"></label>
				<label style="padding-left: 12px">Methodology : </label><label id="testMethodLbl" style="font-weight: bold;"></label>
			</div>
			<div id="progressbar" style="margin-top: 10px; font-weight: bold">
				Loading.. please wait..
			</div>
			<div style="float:right; margin-top: 10px; margin-right: 10px" id="paginationDiv" name="paginationDiv">
			</div>
			<div style="clear:both"></div>
			<div class="resultList" style="width: 780px" >
				<table class="resultList" id="previousResultsTable" cellspacing="0" cellpadding="0" style="margin: top: 10px;width: 780px">
					<tr>
						<th>Visit ID</th>
						<th>Test Name</th>
						<th>Test Date</th>
						<th>Sample Date&Time</th>
						<th>Value</th>
						<th>Report</th>
					</tr>
					<tr style="display:none">
						<td><label></label></td>
						<td><label></label></td>
						<td><label></label></td>
						<td><label></label></td>
						<td><label></label></td>
						<td><a href="" title="Report Content"></a></td>
					</tr>
					<tr style="display: none; background-color:#FFC">
						<td colspan="6"><img src="${cpath}/images/alert.png"/> No previous history available for the Result Label.</td>
					</tr>
				</table>
			</div>
		</fieldset>
		<table >
			<tr>
				<td><input type="button" name="previousResults_btn" id="previousResults_btn" value="Close"/></td>
			</tr>
		</table>
	</div>
</div>