<div id="addSIDialog" style="display: none">
	<div class="bd">
		<div id="addSIDialogFieldsDiv">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Add Medicine Prescription</legend>
				<table class="formtable">
					<tr>
						<td class="formlabel">Type: </td>
						<td><select id="s_d_itemType" name="s_d_itemType" class="dropdown">
							<option value="Medicine">Medicine</option>
						</select></td>
						<td class="formlabel">Item: </td>
						<td colspan="3">
							<div id="s_d_itemAutocomplete" style="padding-bottom: 25px; width: 350px">
								<input type="text" id="s_d_itemName" name="s_d_itemName" style="width: 350px">
								<div id="s_d_itemContainer" style="width: 350px" class="scrolForContainer"></div>
								<input type="hidden" name="s_d_item_master" id="s_d_item_master" value=""/>
								<input type="hidden" name="s_d_generic_name" id="s_d_generic_name" value=""/>
								<input type="hidden" name="s_d_generic_code" id="s_d_generic_code" value=""/>
								<input type="hidden" name="s_d_ispackage" id="s_d_ispackage" value=""/>
								<input type="hidden" name="s_d_item_id" id="s_d_item_id" value=""/>
								<input type="hidden" name="s_d_package_size" id="s_d_package_size" value=""/>
								<input type="hidden" name="s_d_price" id="s_d_price" value=""/>
								<input type="hidden" name="s_d_qty_in_stock" id="s_d_qty_in_stock" value=""/>
								<input type="hidden" name="s_d_item_prescribed_id" id="s_d_item_prescribed_id" value=""/>
								<input type="hidden" name="s_d_granular_units" id="s_d_granular_units" value=""/>
							</div>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Date:</td>
						<td>
							<insta:datewidget name="s_d_prescribed_date" id="s_d_prescribed_date" value="today"/>
						</td>
						<td class="formlabel">Generic: </td>
						<td colspan="2"><a id="s_d_genericNameAnchor_dialog" style="display: none"></a></td>
					</tr>
					<tr>
						<td class="formlabel">Item Form: </td>
						<td><insta:selectdb name="s_d_item_form_id" id="s_d_item_form_id" table="item_form_master"
							displaycol="item_form_name" valuecol="item_form_id" filtercol="status" filtervalue="A" orderby="item_form_name"
							dummyvalue="-- Select --" dummyvalueId="" onchange="setGranularUnit(event, 's_d');"/>
						</td>
						<td class="formlabel" title="Dose/Strength">Strength</td>
						<td>
							<input type="text" name="s_d_item_strength" id="s_d_item_strength" value="" style="width: 60px">
							<insta:selectdb name="s_d_item_strength_units" id="s_d_item_strength_units" table="strength_units" displaycol="unit_name" valuecol="unit_id"
								dummyvalue="Select --" style="width: 60px"/>
						</td>
						<td class="formlabel">Route: </td>
						<td>
							<c:choose>
								<c:when test="${prescriptions_by_generics}">
									<insta:selectdb name="s_d_medicine_route" id="s_d_medicine_route" table="medicine_route" valuecol="route_id" dummyvalue="-- Select --"
										displaycol="route_name" />
								</c:when>
								<c:otherwise >
									<select id="s_d_medicine_route" class="dropdown">
										<option value="">-- Select --</option>
									</select>
								</c:otherwise>
							</c:choose>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Dosage: </td>
						<td>
							<input type="text" name="s_d_strength" id="s_d_strength" value="" />
							<label id="s_d_medicineUOM"></label>
							<input type="hidden" name="s_d_consumption_uom" id="s_d_consumption_uom" value=""/>
						</td>
						<td class="formlabel">Frequency: </td>
						<td>
							<div id="s_d_frequencyAutoComplete" style="padding-bottom: 25px; width: 138px">
								<input type="text" name="s_d_frequency" id="s_d_frequency" maxlength="150">
								<div id="s_d_frequencyContainer"></div>
								<input type="hidden" name="s_d_per_day_qty" id="s_d_per_day_qty" value=""/>
							</div>
						</td>
						<td class="formlabel"></td>
						<td></td>
					</tr>
					<tr>
						<td class="formlabel">Duration: </td>
						<td>
							<input type="text" name="s_d_duration" id="s_d_duration" value="" onkeypress="return enterNumOnlyzeroToNine(event);" onblur="return calcQty(event, 's_d');"/>
						</td>
						<td colspan="2">
							<input type="radio" name="s_d_duration_units" value="D" onchange="return calcQty(event, 's_d');">Days
							<input type="radio" name="s_d_duration_units" value="W" onchange="return calcQty(event, 's_d');">Weeks
							<input type="radio" name="s_d_duration_units" value="M" onchange="return calcQty(event, 's_d');">Months
						</td>
					</tr>
					<tr>
						<td class="formlabel">Remarks: </td>
						<td colspan="5"><input type="text" name="s_d_remarks" id="s_d_remarks" value="" style="width: 443px"></td>
					</tr>
				</table>
			</fieldset>
			<fieldset class="fieldSetBorder">
				<table class="compactform">
					<tr>
						<td class="formlabel">Pkg.&nbsp;Size: </td>
						<td class="forminfo"><label id="s_d_pkg_size_label"></label></td>
						<td class="formlabel">Code Type:</td>
						<td class="forminfo"><label id="s_d_code_type"></label></td>
						<td class="formlabel">Code: </td>
						<td class="forminfo"><label id="s_d_code"></label></td>
					</tr>
					<tr>
						<td class="formlabel">Rate:</td>
						<td class="forminfo"> <label id="s_d_price_label"></label> </td>
						<td class="formlabel">Amount:</td>
						<td class="forminfo"> <label id="s_d_claim_item_amount"></label> </td>
						<td class="formlabel">Orig&nbsp;Rate:</td>
						<td class="forminfo"> <label id="s_d_claim_item_orig_rate"></label> </td>
					</tr>
					<tr>
						<td class="formlabel">Qty(UOM):</td>
						<td class="forminfo" style="width:120px;white-space:nowrap">
							<input type="text" name="s_d_claim_item_qty" id="s_d_claim_item_qty"
								onchange="onQtyChange('s_d');" class="numeric"
								${pbmPrescBean.map.pbm_finalized == 'Y' ? 'readOnly' : ''}
								style="width:50px;"/>
								<select name="s_d_user_unit" id="s_d_user_unit" onchange="onQtyChange('s_d');"
									class="dropdown" style="width:80px;"
									${pbmPrescBean.map.pbm_finalized == 'Y' ? 'disabled' : ''}>
									<option value=""></option>
								</select>
						</td>
						<td class="formlabel">Patient&nbsp;Amt:</td>
						<td class="forminfo"> <label id="s_d_claim_item_pat_amount"></label> </td>
						<td class="formlabel">Orig&nbsp;Amt:</td>
						<td class="forminfo"> <label id="s_d_claim_item_orig_amount"></label> </td>
					</tr>
					<tr>
						<td class="formlabel">Discount:</td>
						<td class="forminfo"> <label id="s_d_claim_item_disc"></label> </td>
						<td class="formlabel">Claim&nbsp;Amt:</td>
						<td class="forminfo">
							<input type="text" name="s_d_claim_item_net_amount" id="s_d_claim_item_net_amount"
								class="numeric" style="width:80px;" readonly="readonly"/>
						</td>
						<td class="formlabel">Orig&nbsp;Claim:</td>
						<td class="forminfo"> <label id="s_d_claim_item_orig_net_amount"></label> </td>
					</tr>
				</table>
			</fieldset>
		</div>
		<table style="margin-top: 10">
			<tr>
				<td>
					<button type="button" name="SIAdd" id="SIAdd" >Add</button>
					<input type="button" name="SIClose" value="Close" id="SIClose"/>
				</td>
			</tr>
		</table>
	</div>
</div>

<div id="editSIDialog" style="display: none">
	<input type="hidden" name="s_ed_editRowId" id="s_ed_editRowId" value=""/>
	<div class="bd">
		<div id="editSIDialogFieldsDiv">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Edit Medicine Prescription</legend>
				<table class="formtable">
					<tr>
						<td class="formlabel">Type: </td>
						<td>
							<label id="s_ed_itemTypeLabel" style="font-weight: bold"></label>
							<input type="hidden" id="s_ed_itemType" name="s_ed_itemType" value="Medicine"/>
						</td>
						<td class="formlabel">Generic: </td>
						<td colspan="2"><a id="s_ed_genericNameAnchor_editdialog" style="display: none"></a></td>
					</tr>
					<tr>
						<td class="formlabel">Date:</td>
						<td style="width:230px">
							<insta:datewidget name="s_ed_prescribed_date" id="s_ed_prescribed_date" value="today"/>
						</td>
						<td class="formlabel">Item: </td>
						<td colspan="3">
							<div id="s_ed_itemAutocomplete" style="padding-bottom: 25px; width: 350px">
								<input type="text" id="s_ed_itemName" name="s_ed_itemName" style="width: 350px"
								${pbmPrescBean.map.pbm_finalized == 'Y' ? 'disabled' : ''}>
								<div id="s_ed_itemContainer" style="width: 350px" class="scrolForContainer"></div>
								<input type="hidden" name="s_ed_item_master" id="s_ed_item_master" value=""/>
								<input type="hidden" name="s_ed_generic_name" id="s_ed_generic_name" value=""/>
								<input type="hidden" name="s_ed_generic_code" id="s_ed_generic_code" value=""/>
								<input type="hidden" name="s_ed_ispackage" id="s_ed_ispackage" value=""/>
								<input type="hidden" name="s_ed_item_id" id="s_ed_item_id" value=""/>
								<input type="hidden" name="s_ed_package_size" id="s_ed_package_size" value=""/>
								<input type="hidden" name="s_ed_price" id="s_ed_price" value=""/>
								<input type="hidden" name="s_ed_qty_in_stock" id="s_ed_qty_in_stock" value=""/>
								<input type="hidden" name="s_ed_item_prescribed_id" id="s_ed_item_prescribed_id" value=""/>
								<input type="hidden" name="s_ed_granular_units" id="s_ed_granular_units" value=""/>
							</div>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Item Form: </td>
						<td><insta:selectdb name="s_ed_item_form_id" id="s_ed_item_form_id" table="item_form_master" onchange="setGranularUnit(event, 's_ed');setSIEdited();"
							displaycol="item_form_name" valuecol="item_form_id" filtercol="status" filtervalue="A" orderby="item_form_name"
							dummyvalue="-- Select --" dummyvalueId="" />
						</td>
						<td class="formlabel" title="Dose/Strength">Strength</td>
						<td>
							<input type="text" name="s_ed_item_strength" id="s_ed_item_strength" value="" style="width: 60px" onchange="setSIEdited();">
							<insta:selectdb name="s_ed_item_strength_units" id="s_ed_item_strength_units" table="strength_units" displaycol="unit_name" valuecol="unit_id"
								dummyvalue="Select --" style="width: 60px" onchange="setSIEdited();"/>
						</td>
						<td class="formlabel">Route: </td>
						<td>
							<c:choose>
								<c:when test="${prescriptions_by_generics}">
									<insta:selectdb name="s_ed_medicine_route" id="s_ed_medicine_route"
										table="medicine_route" valuecol="route_id" dummyvalue="-- Select --"
										displaycol="route_name" />
								</c:when>
								<c:otherwise >
									<select id="s_ed_medicine_route" class="dropdown">
										<option value="">-- Select --</option>
									</select>
								</c:otherwise>
							</c:choose>
						</td>
					</tr>

					<tr>
						<td class="formlabel">Dosage: </td>
						<td>
							<input type="text" name="s_ed_strength" id="s_ed_strength" value=""/>
							<label id="s_ed_medicineUOM"></label>
							<inpu typ="hidden" name="s_ed_consumption_uom" id="s_ed_consumption_uom" value=""/>
						</td>
						<td class="formlabel">Frequency: </td>
						<td>
							<div id="s_ed_frequencyAutoComplete" style="padding-bottom: 25px; width: 138px;">
								<input type="text" name="s_ed_frequency" id="s_ed_frequency"  maxlength="150" value=""
								${pbmPrescBean.map.pbm_finalized == 'Y' ? 'disabled' : ''}/>
								<input type="hidden" name="s_ed_frequency_hidden" id="s_ed_frequency_hidden" value=""/>
								<input type="hidden" name="s_ed_per_day_qty" id="s_ed_per_day_qty" value=""/>
								<div id="s_ed_frequencyContainer"></div>
							</div>
						</td>
						<td class="formlabel"></td>
						<td></td>
					</tr>
					<tr>
						<td class="formlabel">Duration: </td>
						<td><input type="text" name="s_ed_duration" id="s_ed_duration" value=""
							${pbmPrescBean.map.pbm_finalized == 'Y' ? 'readOnly' : ''}
						onkeypress="return enterNumOnlyzeroToNine(event);" onchange="setSIQtyFieldsEdited();setSIEdited();" onblur="return calcQty(event, 's_ed');"/></td>
						<td colspan="2">
							<c:choose>
								<c:when test="${pbmPrescBean.map.pbm_finalized == 'Y'}">
									<input type="radio" name="s_ed_duration_units" value="D" disabled="disabled">Days
									<input type="radio" name="s_ed_duration_units" value="W" disabled="disabled">Weeks
									<input type="radio" name="s_ed_duration_units" value="M" disabled="disabled">Months
								</c:when>
								<c:otherwise>
									<input type="radio" name="s_ed_duration_units" value="D" onchange="setSIQtyFieldsEdited();setSIEdited();return calcQty(event, 's_ed');">Days
									<input type="radio" name="s_ed_duration_units" value="W" onchange="setSIQtyFieldsEdited();setSIEdited();return calcQty(event, 's_ed');">Weeks
									<input type="radio" name="s_ed_duration_units" value="M" onchange="setSIQtyFieldsEdited();setSIEdited();return calcQty(event, 's_ed');">Months
								</c:otherwise>
							</c:choose>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Remarks: </td>
						<td colspan="5"><input type="text" name="s_ed_remarks" id="s_ed_remarks" value="" style="width: 443px;" onchange="setSIEdited();"></td>
					</tr>
				</table>
				</fieldset>
				<fieldset class="fieldSetBorder">
				<table class="compactform">
					<tr>
						<td class="formlabel">Pkg.&nbsp;Size: </td>
						<td class="forminfo"> <label id="s_ed_pkg_size_label"></label></td>
						<td class="formlabel">Code Type: </td>
						<td class="forminfo"> <label id="s_ed_code_type"></label></td>
						<td class="formlabel">Code: </td>
						<td class="forminfo"> <label id="s_ed_code"></label></td>
					</tr>
					<tr>
						<td class="formlabel">Rate:</td>
						<td class="forminfo"> <label id="s_ed_price_label"></label> </td>
						<td class="formlabel">Amount:</td>
						<td class="forminfo"> <label id="s_ed_claim_item_amount"></label> </td>
						<td class="formlabel">Orig&nbsp;Rate:</td>
						<td class="forminfo"> <label id="s_ed_claim_item_orig_rate"></label> </td>
					</tr>
					<tr>
						<td class="formlabel">Qty(UOM):</td>
						<td class="forminfo" style="width:120px;white-space:nowrap">
							<input type="text" name="s_ed_claim_item_qty" id="s_ed_claim_item_qty"
								onchange="setSIQtyFieldsEdited();onQtyChange('s_ed');" class="numeric"
								${pbmPrescBean.map.pbm_finalized == 'Y' ? 'readOnly' : ''}
								style="width:50px;"/>
								<select name="s_ed_user_unit" id="s_ed_user_unit" onchange="setSIQtyFieldsEdited();onQtyChange('s_ed');"
									class="dropdown" style="width:80px;"
									${pbmPrescBean.map.pbm_finalized == 'Y' ? 'disabled' : ''}>
									<option value=""></option>
								</select>
						</td>
						<td class="formlabel">Patient&nbsp;Amt:</td>
						<td class="forminfo"> <label id="s_ed_claim_item_pat_amount"></label> </td>
						<td class="formlabel">Orig&nbsp;Amt:</td>
						<td class="forminfo"> <label id="s_ed_claim_item_orig_amount"></label> </td>
					</tr>
					<tr>
						<td class="formlabel">Discount:</td>
						<td class="forminfo"> <label id="s_ed_claim_item_disc"></label> </td>
						<td class="formlabel">Claim&nbsp;Amt:</td>
						<td class="forminfo">
							<input type="text" name="s_ed_claim_item_net_amount" id="s_ed_claim_item_net_amount"
								onchange="recalcItemAmount()" class="numeric"
								style="width:80px;"/>
						</td>
						<td class="formlabel">Orig&nbsp;Claim:</td>
						<td class="forminfo"> <label id="s_ed_claim_item_orig_net_amount"></label> </td>
					</tr>
				</table>
			</fieldset>
			<div id="codeDetails" style="margin-bottom:10px;">
				<dl class="accordion">
				  <dt>
			    	<div style="float:left; margin-right: 5px; display:block;" id="codeDetailsImg">
						<img width="16" height="16" src="<%=request.getContextPath()%>/images/arrow_down.png">
					</div>
					<div style="float:left">Observation Code Type / Code Details</div>
				    <div class="clrboth"></div>
				  </dt>
				  <dd id="codeDetailsDD">
					<table class="formtable" width="100px">
						<tr>
							<td class="formlabel">Item Form:</td>
					    	<td class="forminfo">
					    		<label id="s_ed_item_form_code_type"> </label> /
								<label id="s_ed_item_form_code"> </label>
					    	</td>
					    	<td class="formlabel">Strength: </td>
							<td class="forminfo">
								<label id="s_ed_item_strength_code_type"> </label> /
								<label id="s_ed_item_strength_code"> </label>
							</td>
					    	<td class="formlabel">Route:</td>
					    	<td class="forminfo">
					    		<label id="s_ed_medicine_route_code_type"> </label> /
								<label id="s_ed_medicine_route_code"> </label>
					    	</td>
					    </tr>
					    <tr>
					    	<td class="formlabel">Dosage:</td>
					    	<td class="forminfo">
					    		<label id="s_ed_strength_code_type"> </label> /
								<label id="s_ed_strength_code"> </label>
					    	</td>
					    	<td class="formlabel">Frequency:</td>
					    	<td class="forminfo">
					    		<label id="s_ed_frequency_code_type"> </label> /
								<label id="s_ed_frequency_code"> </label>
					    	</td>
		    			</tr>
		    			<tr>
		    				<td class="formlabel">Duration:</td>
					    	<td class="forminfo">
					    		<label id="s_ed_duration_code_type"> </label> /
								<label id="s_ed_duration_code"> </label>
					    	</td>
		    				<td class="formlabel">Refills:</td>
							<td class="forminfo">
					    		<label id="s_ed_refills_code_type"> </label> /
								<label id="s_ed_refills_code"> </label>
					    	</td>
							<td class="formlabel">Instruction/Remarks:</td>
							<td class="forminfo">
					    		<label id="s_ed_remarks_code_type"> </label> /
								<label id="s_ed_remarks_code"> </label>
					    	</td>
						</tr>
					</table>
				</dd>
				</dl>
			</div>
		</div>
		<table style="margin-top: 10">
			<tr>
				<td>
					<input type="button" id="siOk" name="siok" value="Ok"/>
					<input type="button" id="siEditCancel" name="sicancel" value="Cancel" />
					<input type="button" id="siEditPrevious" name="siprevious" value="<<Previous" />
					<input type="button" id="siEditNext" name="sinext" value="Next>>" />
				</td>
			</tr>
		</table>
	</div>
</div>
<div id="genericNameDisplayDialog"  style="visibility:hidden">
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Generic Name Details</legend>
			<table border="0" class="formtable">
				<tr height="10px"></tr>
				<tr>
					<td class="formlabel">Generic&nbsp;Name: </td>
					<td class="forminfo" style="width:8em"><b><label id="gen_generic_name"></label></b>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Classification: </td>
					<td class="forminfo" style="width:25em"><b><label id="classification_name"></label>	</td>
				</tr>
				<tr>
					<td class="formlabel">Sub-Classification:</td>
						<td class="forminfo" style="width:25em"><b><label id="sub_classification_name"></label>	</td>
				</tr>
				<tr>
					<td class="formlabel">Standard Adult Dose:</td>
						<td class="forminfo" style="width:25em"><b><label id="standard_adult_dose"></label>	</td>
				</tr>
				<tr>
					<td class="formlabel">Criticality:</td>
						<td class="forminfo" style="width:25em"><b><label id="criticality"></label>	</td>
				</tr>

				<tr height="10px"></tr>
			</table>
			<table>
				<tr>
			    	<td><input type="button" id="genericNameCloseBtn" value="Close"></td>
				</tr>
			</table>
		</fieldset>
	</div>
</div>


<div id="rejectionDetailsDialog" style="display:none;visibility:hidden;" ondblclick="handleRejectionDialogCancel();">
	<div class="bd" id="bd1" style="padding-top: 0px;">
		<table class="formTable" align="center" id="rejection_reason" style="width:480px;">
			<tr>
				<td>
					<fieldSet class="fieldSetBorder">
						<legend class="fieldSetLabel">Rejection Details</legend>
						<table width="100%">
							<tr>
								<td colspan="100">
									${pbmPrescBean.map.approval_comments}
								</td>
							</tr>
						</table>
					</fieldSet>
				</td>
			</tr>
			<tr>
				<td align="left">
					<input type="button" value="Close" style="cursor:pointer;" onclick="handleRejectionDialogCancel();"/>
				</td>
			</tr>
		</table>
		</fieldset>
	</div>
</div>