<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<div id="previousPrescDiv" style="display: none">
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Previous Prescriptions</legend>
			<div style="margin-top: 10px;">
				<table width="100%">
					<tr>
						<td width="30%">Consultation Date : <label id="consultationDate" style="font-weight: bold;"></label></td>
						<td width="70%">Doctor Name : <label id="doctorName" style="font-weight: bold;"></label></td>
					</tr>
					<tr>
						<td colspan="2" style="padding-top: 10px">Primary Diagnosis : <label id="primaryDiagnosis" style="font-weight: bold;"></label></td>
					</tr>
				</table>
			</div>
			<div id="prevProgressbar" style="margin-top: 10px; font-weight: bold">
				Loading.. please wait..
			</div>
			<div style="float:right; margin-top: 5px; ">
				<table width="100%">
					<tr>
						<td><label style="margin-right: 30px" id="noofrecords"></label> </td>
						<td><label style="margin-right: 10px" id="prevPaginationDiv"></label></td>
					</tr>
				</table>
			</div>
			<div style="clear:both"></div>
			<div class="resultList" style="width: 780px" >
				<table class="resultList" id="previousResultsTable" cellspacing="0" cellpadding="0" style="margin: top: 10px;width: 780px">
					<tr>
						<th width="20px">Select</th>
						<th>Item Type</th>
						<th>Name</th>
						<th>Form</th>
						<th>Strength</th>
						<th>Admin Strength</th>
						<th>Details</th>
						<th>Route</th>
						<th>Instructions</th>
						<th>Special Instructions</th>
						<th>Qty</th>
					</tr>
					<tr style="display:none">
						<td></td>
						<td><label></label></td>
						<td><label></label></td>
						<td><label></label></td>
						<td><label></label></td>
						<td><label></label></td>
						<td><label></label></td>
						<td><label></label></td>
						<td><label></label></td>
						<td><label></label></td>
						<td>
							<label></label>

							<input type="hidden" name="prev_item_name" value=""/>
							<input type="hidden" name="prev_item_type" value=""/>
							<input type="hidden" name="prev_item_id" value=""/>
							<input type="hidden" name="prev_strength" value=""/>
							<input type="hidden" name="prev_admin_strength" value=""/>
							<input type="hidden" name="prev_granular_units" value=""/>
							<input type="hidden" name="prev_drug_code" value=""/>
							<input type="hidden" name="prev_frequency" value=""/>
							<input type="hidden" name="prev_duration" value=""/>
							<input type="hidden" name="prev_duration_units" value=""/>
							<input type="hidden" name="prev_medicine_quantity" value=""/>
							<input type="hidden" name="prev_item_remarks" value=""/>
							<input type="hidden" name="prev_special_instr" value=""/>
							<input type="hidden" name="prev_item_master" value=""/>
							<input type="hidden" name="prev_ispackage" id="ispackage" value=""/>
							<input type="hidden" name="prev_generic_code" value=""/>
							<input type="hidden" name="prev_generic_name" value=""/>
							<input type="hidden" name="prev_edited" value='false'/>
							<input type="hidden" name="prev_delItem" id="delItem" value="false" />
							<input type="hidden" name="prev_route_id" value=""/>
							<input type="hidden" name="prev_route_name" value=""/>
							<input type="hidden" name="prev_consumption_uom" value=""/>
							<input type="hidden" name="prev_item_form_id" value=""/>
							<input type="hidden" name="prev_item_form_name" value=""/>
							<input type="hidden" name="prev_item_strength" value=""/>
							<input type="hidden" name="prev_item_strength_units" value=""/>
							<input type="hidden" name="prev_item_strength_unit_name" value=""/>
							<input type="hidden" name="prev_display_order" value=""/>
							<input type="hidden" name="prev_tooth_num_required" value=""/>
							<input type="hidden" name="prev_priorAuth" value=""/>
							<input type="hidden" name="prev_charge" value=""/>
							<input type="hidden" name="prev_discount" value=""/>
							<input type="hidden" name="prev_non_hosp_medicine" value=""/>
							<input type="hidden" name="prev_insurance_category_id" value=""/>
							<input type="hidden" name="prev_insurance_category_name" value=""/>
						</td>
					</tr>
					<tr style="display: none; background-color:#FFC">
						<td colspan="10"><img id="npId" src="${cpath}/images/alert.png"/> No prescription available.</td>
					</tr>
				</table>
			</div>
		</fieldset>
		<table >
			<tr>
				<td><input type="button" id="presc_Ok" name="presc_Ok" value="Ok"></td>
				<td><input type="button" name="previousResults_btn" id="previousResults_btn" value="Close"/></td>
			</tr>
		</table>
	</div>
</div>