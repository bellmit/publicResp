<%@tag body-content="empty" dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<div id="addCenterDialog" style="display: none">
	<div class="bd" style="width: 430px;" >
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Add Center</legend>
			<table class="add_center_table">
				<tr>
					<td >State: </td>
					<td><insta:selectdb name="d_state" id="d_state" table="state_master" valuecol="state_id" displaycol="state_name"
							filtered="true" dummyvalue="-- Select --" dummyvalueid="" onchange="return populateCities();" orderby="state_name"/></td>
					<td rowspan="2" valign="middle">
						<div style="float: left; padding-top: 5px; margin-left: 10px">(OR) Show All Centers</div>
						<div style="float: left">
							<input type="checkbox" id="show_all_centers_chkbox" onclick="showAllCenters(this)"/>
						</div>
					</td>
				</tr>
				<tr>
					<td >City: </td>
					<td><select name="d_city" id="d_city" class="dropdown">
							<option value="">-- Select --</option>
						</select>
					</td>
				</tr>
				<tr>
					<td colspan="3">
						<div style="float: right">
							<input type="button" id="d_search_centers" value="Search" />
						</div>

						<div style="float: right; padding-top: 5px" id="paginationDiv">
						</div>
					</td>
				</tr>
			</table>
			<table id="avlbl_centers_table" class="detailList" style="margin-top: 10px">
				<tr>
					<th style="padding-top: 0px;padding-bottom: 0px">
						<input type="checkbox" id="d_checkAllCenters" onclick="return checkOrUncheckAll('d_center_chkbox', this)"/>
					</th>
					<th>State</th>
					<th>City</th>
					<th>Center</th>
				</tr>
				<tr style="display: none">
					<td>
						<input type="checkbox" name="d_center_chkbox" value=""/>
						<input type="hidden" name="d_state_name" value=""/>
						<input type="hidden" name="d_city_name" value=""/>
						<input type="hidden" name="d_center_name" value=""/>
					</td>
					<td></td>
					<td></td>
					<td></td>
				</tr>
			</table>
		</fieldset>
		<table style="margin-top: 10px">
			<tr>
				<td>
					<input type="button" id="d_center_ok" value="Ok"/>
					<input type="button" id="d_center_cancel" value="Cancel"/>
				</td>
			</tr>
		</table>
	</div>
</div>
