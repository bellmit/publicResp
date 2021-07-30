<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<div id="addSIDialog" style="display: none">
	<div class="bd">
		<div id="addSIDialogFieldsDiv">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Add Activity Details</legend>
				<fieldset>
						<table>
						<tr>
							<td class="fieldSetLabel">Type:</td>
							<td><input type="radio" name="s_d_itemType" value="DIA" checked onclick="onItemChange()"> Investigation</td>
							<td><input type="radio" name="s_d_itemType" value="SER" onclick="onItemChange()"> Service</td>
							<td><input type="radio" name="s_d_itemType" value="OPE" onclick="onItemChange()"> Operation</td>
							<td><input type="radio" name="s_d_itemType" value="DOC" onclick="onItemChange()"> Doctor</td>
							<td><input type="radio" name="s_d_itemType" value="ITE" onclick="onItemChange()"> Inventory</td>
						</tr>
					</table>
				</fieldset>
					<table class="formtable">
						<tr>
							<td class="formlabel">Date:</td>
							<td>
								<insta:datewidget name="s_d_prescribed_date" id="s_d_prescribed_date" value="today"/>
							</td>
						</tr>
						<tr>
							<td class="formlabel">Item: </td>
							<td colspan="3">
							<div id="s_d_itemAutocomplete" style="padding-bottom: 25px; width: 350px">
								<input type="text" id="s_d_itemName" name="s_d_itemName" style="width: 350px">
								<div id="s_d_itemContainer" style="width: 350px" class="scrolForContainer"></div>
								<input type="hidden" name="s_d_item_master" id="s_d_item_master" value=""/>
								<input type="hidden" name="s_d_ispackage" id="s_d_ispackage" value=""/>
								<input type="hidden" name="s_d_item_id" id="s_d_item_id" value=""/>
								<input type="hidden" name="s_d_price" id="s_d_price" value=""/>
								<input type="hidden" name="s_d_priorAuth" id="s_d_priorAuth" value=""/>
								<input type="hidden" name="s_d_tooth_num_required" id="s_d_tooth_num_required" value=""/>
								<input type="hidden" name="s_d_tooth_number" id="s_d_tooth_number" value=""/>
								<input type="hidden" name="s_d_doc_cons_type_old" id="s_d_doc_cons_type_old" value=""/>
							</div>
						</td>
						<td class="formlabel" id="s_d_toothLabelTd">Tooth Number: </td>
						<td id="s_d_toothValueTd">
							<div id="s_d_ToothNumberDiv" style="width: 120px; float: left;"></div>
							<div class="multiInfoEditBtn" style="float: left;margin-left: 10px" id="s_d_ToothNumBtnDiv">
								<a href="javascript:void(0);" onclick="return showToothNumberDialog('add', this);"
									title="Select Tooth Numbers">
									<img src="${cpath}/icons/Edit.png" class="button"/>
								</a>
							</div>
							<div id="s_d_ToothNumDsblBtnDiv" style="float: left;margin-left: 10px;">
								<img src="${cpath}/icons/Edit1.png" class="button"/>
							</div>
						</td>
						<td class="formlabel" id="s_d_docConsLabelTd" style="display: none">Consultation Type: </td>
						<td id="s_d_docConsValueTd" style="display: none">
							<select class="dropdown" name="s_d_doc_cons_type" id="s_d_doc_cons_type"
								onchange="setSIQtyFieldsEdited();onQtyChange('s_d');" >
								<option value=""><insta:ltext key="common.selectbox.defaultText"/></option>
								<c:forEach var="ch" items="${docCharges}">
									<option value="${ch.map.consultation_type_id}">${ch.map.consultation_type}</option>
								</c:forEach>
							</select>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Code Type: </td>
						<td class="forminfo">
							<select class="dropdown" name="s_d_code_type" id="s_d_code_type"
								onchange="initTrtCodesAutocomp('s_d_code', 's_d_trtDropDown', this.value, 's_d');"/>
								<option value=""><insta:ltext key="common.selectbox.defaultText"/></option>
								<c:forEach var="ct" items="${codeCategories}">
									<option value="${ct.code_type}">${ct.code_type}</option>
								</c:forEach>
							</select>
						</td>
						<td class="formlabel">Code/Desc:</td>
						<td class="forminfo">
							<div id="s_d_trtAuto" style="padding-bottom: 20px">
								<input type="text" name="s_d_code" id="s_d_code" />
								<div id="s_d_trtDropDown" class="scrolForContainer" style="width:350px;"></div>
							</div>
						</td>
						<td colspan="2" class="forminfo" style="white-space: nowrap;">
							<label id="s_d_code_desc"></label>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Prior Auth Id: </td>
						<td class="forminfo">
							<input type="text" name="s_d_preauth_id" id="s_d_preauth_id">
						</td>
						<td class="formlabel">Prior Auth Mode: </td>
						<td class="forminfo">
							<insta:selectdb  name="s_d_preauth_mode" id="s_d_preauth_mode" value="" table="prior_auth_modes"
								valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name" filtered="false"
								dummyvalue="-- Select --"/>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Prior Auth Status:</td>
						<td class="forminfo">
						<insta:selectoptions name="s_d_preauth_act_status" id="s_d_preauth_act_status"
							 value="" optexts="Open,Denied,Approved" opvalues="O,D,C" disabled="true"
							 onchange="setDefaultApprvdQty('s_d',this);"/>
						</td>
						<td class="formlabel"><label id="s_d_priorAuthLabelTd">Prior Auth: </label></td>
						<td ><b><label id="s_d_priorAuth_label"></label></b></td>
						<td class="formlabel"> <label id="s_d_markPriorAuthReqTd">Send For Prior Auth: </label></td>
						<td id="s_d_markPriorAuthCheckBox">
							<input type="checkbox" id="s_d_markPriorAuthReq" disabled>
						</td>
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
						<td class="formlabel">Qty:</td>
						<td class="forminfo" style="width:120px;white-space:nowrap">
							<input type="text" name="s_d_claim_item_qty" id="s_d_claim_item_qty"
								onchange="onQtyChange('s_d');" class="numeric"
								style="width:50px;"/>
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
					<tr>
						<td class="formlabel">Approved&nbsp;Claim&nbsp;Amount:</td>
						<td class="forminfo">
							<input 
								type="text"
								name="s_d_claim_item_approved_amount" 
								id="s_d_claim_item_approved_amount"
								class="numeric"
								style="width:80px;"/>
						</td>
						<td class="formlabel">Approved&nbsp;Qty:</td>
						<td class="forminfo" style="width:120px;white-space:nowrap">
							<input type="text" name="s_d_claim_approved_qty" id="s_d_claim_approved_qty"
								onchange="onQtyChange('s_d');" class="numeric"
								style="width:50px;"/>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Remarks: </td>
						<td colspan="5"><input type="text" name="s_d_item_remarks" id="s_d_item_remarks"
						value="" style="width: 443px"></td>
					</tr>
				</table>

				<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Add Observations</legend>
				 <input type="hidden" name="addobsIndex" id="addobsIndex" value="1"/>
				  <input type="hidden" name="addobsActItemId" id="addobsActItemId" value=""/>
				       <table>
				       	<tr>
				       		<td>
				        		<table class="formtable" id="addobsValueTable" style="display:none;">
				        			<tr>
				        				<th width="140px"> Observation Type </th>
				        				<th width="76px"> Observation Code </th>
				        				<th width="62px"> Code/Desc </th>
				        				<th width="140px"> Value </th>
				        				<th width="140px"> Value Type </th>
				        				<th> </th>
				        			</tr>
				        			<tr id="addobs.1">
				        				<!--Observation Row -->
				        				<td width="140px;">
				        					<insta:selectdb name="addobsCodeType.1" id="addobsCodeType.1" table="mrd_supported_codes"
											valuecol="code_type" displaycol="code_type" dummyvalue="--Select--"
											dummyvalueId="" filtercol="code_category" filtervalue="Observations"
											onchange="initObsCodesAutocomp('addobs', 1);"/>
				        				</td>
				        				<td width="76px">
				        					<div id="addobsAuto.1" style="width:70px;">
											<input type="text" name="addobsCode.1" id="addobsCode.1"  style="width:70px;" value=""/>
										<div id="addobsDropDown.1" class="scrolForContainer" style="width:300px;"></div>
										</div>
				        				</td>
				        				<td width="62px" style="text-align:right">
				        					<label id="addobsCodeDesc.1" class="addobsRowCount"></label>
				        					<input type="hidden" name="addobsMasterCodeDesc.1" id="addobsMasterCodeDesc.1" value=""/>
										<img class="imgHelpText" name="addobsHelpImg.1" id="addobsHelpImg.1" title="Code Description" src="${cpath}/images/help.png" onclick="showCodeDescription('addobs', 1);"/>
				        				</td>
				        				<td width="140px">
				        					<input type="text" name="addobsValue.1" id="addobsValue.1" value=""/>
				        				</td>
				        				<td width="140px">
				        					<input type="text" name="addobsValueType.1" id="addobsValueType.1" value=""/>
				        				</td>
				        				<td  id="addobsDel.1" align="right" width="17px;" style="padding-left: 5px; padding-right: 5px; padding-top: 9px; height: 18px; width: 17px;">
				        					&nbsp;
				        				</td>
				        			</tr>
				        		</table>
				       		</td>
				       	</tr>
				       	<tr>
				       		<!--Add more Observations Row -->
				       		<td colspan="7">&nbsp;</td>
							<td>
								<button type="button" name="btnAddItem" id="btnAddItem"
									title="Add Observations" onclick="addObservationElements('addobs');" class="imgButton">
								<img src="${cpath}/icons/Add.png"/>
								</button>
							</td>
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
				<legend class="fieldSetLabel">Edit Prescription Activity</legend>
				<table class="formtable">
					<tr>
						<td class="formlabel">Type: </td>
						<td>
							<label id="s_ed_itemTypeLabel" style="font-weight: bold"></label>
							<input type="hidden" id="s_ed_itemType" name="s_ed_itemType" value=""/>
						</td>
						<td class="formlabel">Date:</td>
						<td>
							<insta:datewidget name="s_ed_prescribed_date" id="s_ed_prescribed_date" value="today"/>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Item: </td>
						<td colspan="3">
							<div id="s_ed_itemAutocomplete" style="padding-bottom: 25px; width: 350px">
								<input type="text" id="s_ed_itemName" name="s_ed_itemName" style="width: 350px"
								disabled/>
								<div id="s_ed_itemContainer" style="width: 350px" class="scrolForContainer"></div>
								<input type="hidden" name="s_ed_item_master" id="s_ed_item_master" value=""/>
								<input type="hidden" name="s_ed_ispackage" id="s_ed_ispackage" value=""/>
								<input type="hidden" name="s_ed_item_id" id="s_ed_item_id" value=""/>
								<input type="hidden" name="s_ed_price" id="s_ed_price" value=""/>
								<input type="hidden" name="s_ed_priorAuth" id="s_ed_priorAuth" value=""/>
								<input type="hidden" name="s_ed_tooth_num_required" id="s_ed_tooth_num_required" value=""/>
								<input type="hidden" name="s_ed_doc_cons_type_old" id="s_ed_doc_cons_type_old" value=""/>
							</div>
						</td>
						<td class="formlabel" id="s_ed_toothLabelTd">Tooth Number: </td>
						<td id="s_ed_toothValueTd">
							<input type="hidden" name="s_ed_tooth_number" id="s_ed_tooth_number" value=""/>
							<div id="s_ed_ToothNumberDiv" style="width: 120px; float: left"></div>
							<div class="multiInfoEditBtn" style="float: left" id="s_ed_ToothNumBtnDiv">
								<a href="javascript:void(0);" onclick="return showToothNumberDialog('edit', this);"
									title="Select Tooth Numbers">
									<img src="${cpath}/icons/Edit.png" class="button"/>
								</a>
							</div>
							<div id="s_ed_ToothNumDsblBtnDiv" style="float: left;margin-left: 10px;">
								<img src="${cpath}/icons/Edit1.png" class="button"/>
							</div>
						</td>
						<td class="formlabel" id="s_ed_docConsLabelTd" style="display: none">Consultation Type: </td>
						<td id="s_ed_docConsValueTd" style="display: none">
							<select class="dropdown" name="s_ed_doc_cons_type" id="s_ed_doc_cons_type"
								onchange="setSIQtyFieldsEdited();onQtyChange('s_ed');" >
								<option value=""><insta:ltext key="common.selectbox.defaultText"/></option>
								<c:forEach var="ch" items="${docCharges}">
									<option value="${ch.map.consultation_type_id}">${ch.map.consultation_type}</option>
								</c:forEach>
							</select>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Code Type: </td>
						<td class="forminfo">
							<select class="dropdown" name="s_ed_code_type" id="s_ed_code_type"
								onchange="initTrtCodesAutocomp('s_ed_code', 's_ed_trtDropDown', this.value, 's_ed');setSIEdited();"/>
								<option value=""><insta:ltext key="common.selectbox.defaultText"/></option>
								<c:forEach var="ct" items="${codeCategories}">
									<option value="${ct.code_type}">${ct.code_type}</option>
								</c:forEach>
							</select>
						</td>
						<td class="formlabel">Code/Desc:</td>
						<td class="forminfo">
							<div id="s_ed_trtAuto" style="padding-bottom: 20px">
								<input type="text" name="s_ed_code" id="s_ed_code" onchange="setSIEdited();"/>
								<div id="s_ed_trtDropDown" class="scrolForContainer" style="width:350px;"></div>
							</div>
						</td>
						<td colspan="2" class="forminfo" style="white-space: nowrap;">
							<label id="s_ed_code_desc"></label>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Prior Auth Id: </td>
						<td class="forminfo">
							<input type="text" name="s_ed_preauth_id" id="s_ed_preauth_id" onchange="setSIEdited()" />
						</td>
						<td class="formlabel">Prior Auth Mode: </td>
						<td class="forminfo">
							<insta:selectdb  name="s_ed_preauth_mode" id="s_ed_preauth_mode" value="" table="prior_auth_modes"
								valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name" filtered="false"
								onchange="setSIEdited()" dummyvalue="-- Select --"/>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Prior Auth Status:</td>
						<td class="forminfo">
						<insta:selectoptions name="s_ed_preauth_act_status" id="s_ed_preauth_act_status"
							 value="" optexts="Open,Denied,Approved" opvalues="O,D,C" disabled="true"
							 onchange="setSIEdited();setDefaultApprovedAmount();setDefaultApprvdQty('s_ed',this);"/>
						</td>
						<td class="formlabel"><label id="s_ed_priorAuthLabelTd">Prior Auth: </label></td>
						<td ><b><label id="s_ed_priorAuth_label"></label></b></td>
						<td class="formlabel"> <label id="s_ed_markPriorAuthReqTd">Send For Prior Auth: </label></td>
						<td id="s_ed_markPriorAuthCheckBox">
							<input type="checkbox" id="s_ed_markPriorAuthReq" value="" onchange="setSIEdited();" >
						</td>
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
						<td class="formlabel">Qty:</td>
						<td class="forminfo" style="width:120px;white-space:nowrap">
							<input type="text" name="s_ed_claim_item_qty" id="s_ed_claim_item_qty"
								${preauthPrescBean.map.preauth_status == 'S' ? 'readOnly' : ''}
								onchange="setSIQtyFieldsEdited();onQtyChange('s_ed');" class="numeric"
								style="width:50px;"/>
              <input type="hidden" name="s_ed_claim_item_rem_qty" id="s_ed_claim_item_rem_qty" /> 

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
								${preauthPrescBean.map.preauth_status == 'S' ? 'readOnly' : ''}
								onchange="recalcItemAmount();" class="numeric"
								style="width:80px;"/>
						</td>
						<td class="formlabel">Orig&nbsp;Claim:</td>
						<td class="forminfo"> <label id="s_ed_claim_item_orig_net_amount"></label> </td>
					</tr>
					<tr>
						<td class="formlabel">Approved&nbsp;Claim&nbsp;Amount:</td>
						<td class="forminfo">
							<input type="text" 
							 name="s_ed_claim_approved_amt"
							 id="s_ed_claim_approved_amt"
							 onchange="recalcItemAmount();"
							 class="numeric"/>
						</td>
						<td class="formlabel">Approved&nbsp;Qty:</td>
						<td class="forminfo">
							<input type="text" name="s_ed_claim_approved_qty" id="s_ed_claim_approved_qty"
								onchange="setSIQtyFieldsEdited();" class="numeric"
								style="width:80px;"/>
						<input type="hidden" name="s_ed_claim_approved_rem_qty" id="s_ed_claim_approved_rem_qty" />
						</td>
					</tr>
					<tr>
						<td class="formlabel">Remarks: </td>
						<td colspan="5"><input type="text" name="s_ed_item_remarks" id="s_ed_item_remarks"
						value="" style="width: 443px;" onchange="setSIEdited();"></td>
					</tr>
				</table>
			</fieldset>

			<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">View/Edit Observations</legend>
			 <input type="hidden" name="obsIndex" id="obsIndex" value="1"/>
			  <input type="hidden" name="obsActItemId" id="obsActItemId" value=""/>
			       <table>
			       	<tr>
			       		<td>
			        		<table class="formtable" id="obsValueTable" style="display:none;">
			        			<tr>
			        				<th width="140px"> Observation Type </th>
			        				<th width="76px"> Observation Code </th>
			        				<th width="62px"> Code/Desc </th>
			        				<th width="140px"> Value </th>
			        				<th width="140px"> Value Type </th>
			        				<th> </th>
			        			</tr>
			        			<tr id="obs.1">
			        				<!--Observation Row -->
			        				<td width="140px;">
			        					<insta:selectdb name="obsCodeType.1" id="obsCodeType.1" table="mrd_supported_codes"
										valuecol="code_type" displaycol="code_type" dummyvalue="--Select--"
										dummyvalueId="" filtercol="code_category" filtervalue="Observations"
										onchange="initObsCodesAutocomp('obs', 1);"/>
			        				</td>
			        				<td width="76px">
			        					<div id="obsAuto.1" style="width:70px;">
										<input type="text" name="obsCode.1" id="obsCode.1"  style="width:70px;" value=""/>
									<div id="obsDropDown.1" class="scrolForContainer" style="width:300px;"></div>
									</div>
			        				</td>
			        				<td width="62px" style="text-align:right">
			        					<label id="obsCodeDesc.1" class="obsRowCount"></label>
			        					<input type="hidden" name="obsMasterCodeDesc.1" id="obsMasterCodeDesc.1" value=""/>
									<img class="imgHelpText" name="obsHelpImg.1" id="obsHelpImg.1" title="Code Description" src="${cpath}/images/help.png" onclick="showCodeDescription('obs', 1);"/>
			        				</td>
			        				<td width="140px">
			        					<input type="text" name="obsValue.1" id="obsValue.1" value=""/>
			        				</td>
			        				<td width="140px">
			        					<input type="text" name="obsValueType.1" id="obsValueType.1" value=""/>
			        				</td>
			        				<td  id="obsDel.1" align="right" width="17px;" style="padding-left: 5px; padding-right: 5px; padding-top: 9px; height: 18px; width: 17px;">
			        					&nbsp;
			        				</td>
			        			</tr>
			        		</table>
			       		</td>
			       	</tr>
			       	<tr>
			       		<!--Add more Observations Row -->
			       		<td colspan="7">&nbsp;</td>
						<td>
							<button type="button" name="btnAddItem" id="btnAddItem"
								title="Add Observations" onclick="addObservationElements('obs');" class="imgButton">
							<img src="${cpath}/icons/Add.png"/>
							</button>
						</td>
			      </tr>
			    </table>
			</fieldset>
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

<div id="infoDialogs" style="visibility: hidden; display: none; width:300px;">
	<div class="hd" style= "visibility: hidden; display: none; ">Observation info</div>
	<div class="bd">
		<table width="90%">
			<tbody>
			<tr>
				<td>
					<label id="infoDialogsText" style="float: left"></label>
				</td>
				<td valign="top" style="width: 12px;">
				</td>
			</tr>
			</tbody>
		</table>
	</div>
</div>

<div id="toothNumDialog" style="display: none">
	<div class="bd">
		<input type="hidden" name="dialog_type" id="dialog_type" value=""/>
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Tooth Numbers(${genericPrefs.tooth_numbering_system == 'U' ? 'UNV' : 'FDI'})</legend>
			<table >
				<tr>
					<td colspan="10" style="border-bottom: 1px solid">Pediatric: </td>
				</tr>
				<tr>
					<c:forEach items="${pediac_tooth_numbers}" var="entry" varStatus="st">
						<c:if test="${st.index%10 == 0}">
							</tr><tr>
						</c:if>
						<td style="width: 50px">
							<input type="checkbox" name="d_chk_tooth_number" value="${ifn:cleanHtmlAttribute(entry)}"/> ${ifn:cleanHtml(entry)}
						</td>
					</c:forEach>
				</tr>
			</table>
			<table >
				<tr>
					<td colspan="10" style="border-bottom: 1px solid">Adult: </td>
				</tr>
				<tr>
					<c:forEach items="${adult_tooth_numbers}" var="entry" varStatus="st">
						<c:if test="${st.index%10 == 0}">
							</tr><tr>
						</c:if>
						<td style="width: 50px">
							<input type="checkbox" name="d_chk_tooth_number" value="${ifn:cleanHtmlAttribute(entry)}"/> ${ifn:cleanHtml(entry)}
						</td>
					</c:forEach>
				</tr>
			</table>
			<table style="margin-top: 10px">
				<tr>
					<td><input type="button" name="toothNumDialog_ok" id="toothNumDialog_ok" value="Ok"></td>
					<td><input type="button" name="toothNumDialog_close" id="toothNumDialog_close" value="Close"></td>
				</tr>
			</table>
		</fieldset>
	</div>
</div>
