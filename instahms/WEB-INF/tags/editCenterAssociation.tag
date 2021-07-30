<%@tag body-content="empty" dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<div id="editCenterDialog" style="display: none">
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Edit Center</legend>
			<input type="hidden" id="center_edit_row_id" value=""/>
			<table class="formtable">
				<tr>
					<td class="formlabel">Center: </td>
					<td class="forminfo"><label id="ed_center_label"></label></td>
				</tr>
				<tr>
					<td class="formlabel">State: </td>
					<td class="forminfo"><label id="ed_state_label"></label></td>
				</tr>
				<tr>
					<td class="formlabel">City: </td>
					<td class="forminfo"><label id="ed_city_label"></label></td>
				</tr>
				<tr>
					<td class="formlabel">Status: </td>
					<td class="forminfo">
						<select id="ed_center_status" class="dropdown" onchange="setCenterFieldEdited();">
							<option value="A">Active</option>
							<option value="I">Inactive</option>
						</select>
					</td>
				</tr>
			</table>
		</fieldset>
		<table style="margin-top: 10px">
			<tr>
				<td>
					<input type="button" id="edit_center_ok" value="Ok"/>
					<input type="button" id="edit_center_cancel" value="Cancel"/>
					<input type="button" id="edit_center_previous" value="<<Previous" />
					<input type="button" id="edit_center_next" value="Next>>"/>
				</td>
			</tr>
		</table>
	</div>
</div>
