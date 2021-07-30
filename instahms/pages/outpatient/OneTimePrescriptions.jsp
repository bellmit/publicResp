<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<jsp:useBean id="eRxStatusDisplay" class="java.util.HashMap"/>
<c:set target="${eRxStatusDisplay}" property="O" value="Open"/>
<c:set target="${eRxStatusDisplay}" property="D" value="Denied"/>
<c:set target="${eRxStatusDisplay}" property="C" value="Closed"/>

<c:set var="requireERxAuthorization" value="${mod_eclaim_erx && visitType =='o'}"/>
<script>
    var showChargesAllRatePlan = ('${roleId}' == '1' || '${roleId}' == '2') ? 'A' : '${actionRightsMap.view_all_rates}';
    var itemFormList = <%= request.getAttribute("itemFormList") %>;
</script>
<style>
.ceedcircle {
     -webkit-border-radius:8px;
     -moz-border-radius:8px;
     border-radius:8px;
     border:1px solid #ccc;
     width:8px;
     height:8px;
     display: inline-block;
}

.yellow {
    background-color: yellow;
}

.black {
    background-color: #000000;
}

.grey {
    background-color: #A9A9A9;
}

.red {
    background-color: red;
}

.green {
    background-color: green;
}

.orange {
    background-color: #FFA500;
}

</style>
<div class="resultList" style="margin-top: 10px">
    <legend class="fieldSetLabel">Management
    <c:forEach var="stn" items="${sys_generated_section}">
			<c:if test="${stn.section_mandatory && stn.section_id == param.section_id}">
				<span class="star">*</span>
			</c:if>
		</c:forEach>
    </legend>
    <table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="itemsTable" border="0" width="100%" style="margin-top: 8px">
        <tr>
            <th>Pres Date & Time</th>
            <th>Type</th>
            <th>Name</th>
            <c:choose>
                <c:when test="${preferences.modulesActivatedMap.mod_ceed_integration == 'Y'}" >
                     <th>Cross Code Status</th>
                </c:when>
            </c:choose>
            <th>Tooth No.</th>
            <th>Form</th>
            <th>Strength</th>
            <th>Admin Strength</th>
            <th>Details</th>
            <th>Route</th>
            <th>Instructions</th>
            <th>Special Instructions</th>
            <th>Qty</th>
            <th style="text-align: right">Pkg. Price</th>
            <th style="text-align: right">Unit Price</th>
            <th style="text-align: right">Pat Amt</th>
            <th>Insurance Category</th>
            <c:if test="${requireERxAuthorization}">
                <th>ERx Status</th>
                <th title="Denial Code">Denial Code</th>
                <th>Code Type</th>
                <th>Drug Code</th>
                <th>Description</th>
                <th>Example</th>
            </c:if>
            <th style="width: 16px;"></th>
            <th style="width: 16px"></th>
        </tr>
        <c:set var="numPrescriptions" value="${fn:length(prescriptions)}"/>
        <c:forEach begin="1" end="${numPrescriptions+1}" var="i" varStatus="loop">
            <c:set var="prescription" value="${prescriptions[i-1].map}"/>
            <%-- treat the partially prescribed items as fully prescribed items. --%>
            <c:set var="item_issued" value="${not empty prescription.issued ? prescription.issued : 'P'}"/>
            <c:set var="item_issued" value="${prescription.issued == 'PA' or prescription.issued == 'O' ? 'O' : prescription.issued}"/>
            <c:set var="priorAuth" value="${not empty patient.primary_sponsor_id ? prescription.prior_auth_required : ''}"/>
            <c:set var="flagColor">
                <c:choose>
                    <c:when test="${item_issued == 'O'}">grey</c:when>
                    <c:when test="${priorAuth == 'A'}">blue</c:when>
                    <c:when test="${priorAuth == 'S'}">green</c:when>
                    <c:otherwise>empty</c:otherwise>
                </c:choose>
            </c:set>
            <c:set var="erxFlagColor">
                <c:choose>
                    <c:when test="${prescription.erx_status == 'D'}">red</c:when>
                    <c:when test="${prescription.erx_status == 'C'}">grey</c:when>
                    <c:otherwise>empty</c:otherwise>
                </c:choose>
            </c:set>
            <c:if test="${empty prescription}">
                <c:set var="style" value='style="display:none"'/>
            </c:if>
            <tr ${style}>
                <td>
                    <input type="checkbox" name="chk_send_item_for_erx" onchange="markForErx(this);"
                        ${((not empty erxBean.map.erx_reference_no) || not (prescription.item_type == 'Medicine' && prescription.non_hosp_medicine == false)) ? 'disabled' : ''}
                        ${prescription.send_for_erx == 'Y' ? 'checked' : ''}
                        style="display: ${requireERxAuthorization ? 'inline' : 'none'}"/>
                    <img src="${cpath}/images/${flagColor}_flag.gif"/>
                    <fmt:formatDate value="${prescription.prescribed_date}" pattern="dd-MM-yyyy HH:mm"/>
                </td>
                <td >
                    <label>${prescription.item_type}
                        <c:if test="${prescription.item_type == 'Medicine' && prescription.non_hosp_medicine}">
                            [Non Hosp]
                        </c:if>
                    </label>
                    <input type="hidden" name="item_prescribed_id" value="${prescription.item_prescribed_id}"/>
                    <input type="hidden" name="itemType" value="${prescription.item_type}"/>
                    <c:choose>
                        <c:when test="${prescription.item_type == 'Medicine' && prescription.master != 'op' && prescription.non_hosp_medicine == false && empty prescription.item_id}">
                            <input type="hidden" name="item_name" value="<c:out value='${prescription.generic_name}'/>"/>
                            <input type="hidden" name="item_id" value="${prescription.generic_code}"/>
                        </c:when>
                        <c:otherwise>
                            <input type="hidden" name="item_name" value="<c:out value='${prescription.item_name}'/>"/>
                            <input type="hidden" name="item_id" value="${prescription.item_id}"/>
                        </c:otherwise>
                    </c:choose>
                    <input type="hidden" name="send_item_for_erx" value="${empty prescription.send_for_erx ? 'N' : prescription.send_for_erx}"/>
                    <input type="hidden" name="admin_strength" value="${prescription.admin_strength}"/>
                    <input type="hidden" name="strength" value="${prescription.strength}"/>
                    <input type="hidden" name="frequency" value="${ifn:cleanHtmlAttribute(prescription.frequency)}"/>
                    <input type="hidden" name="duration" value="${prescription.duration}"/>
                    <input type="hidden" name="duration_units" value="${prescription.duration_units}"/>
                    <input type="hidden" name="medicine_quantity" value="${prescription.medicine_quantity}"/>
                    <input type="hidden" name="item_remarks" value="${prescription.item_remarks}"/>
                    <input type="hidden" name="special_instr" value="${ifn:cleanHtmlAttribute(prescription.special_instr)}"/>
                    <input type="hidden" name="item_master" value="${prescription.master}"/>
                    <input type="hidden" name="delItem" id="delItem" value="false" />
                    <input type="hidden" name="ispackage" id="ispackage" value="${prescription.ispackage}"/>
                    <input type="hidden" name="issued" value="${item_issued}"/>
                    <input type="hidden" name="generic_code" value="${prescription.generic_code}"/>
                    <input type="hidden" name="generic_name" value="${prescription.generic_name}"/>
                    <input type="hidden" name="refills" value="${prescription.refills}"/>
                    <c:set var="activity_due_date" value=""/>
                    <c:if test="${not empty prescription.activity_due_date}">
                        <fmt:formatDate value="${prescription.activity_due_date}" pattern="dd-MM-yyyy HH:mm" var="activity_due_date"/>
                    </c:if>
                    <input type="hidden" name="granular_units" value="${prescription.granular_units}"/>
                    <input type="hidden" name="activity_due_date" value="${activity_due_date}"/>
                    <input type="hidden" name="addActivity" value="${not empty activity_due_date}"/>
                    <input type="hidden" name="edited" value='false'/>
                    <input type="hidden" name="pkg_size" value=""/>
                    <input type="hidden" name="drug_code" value="${prescription.drug_code}"/>
                    <input type="hidden" name="pkg_price" value=""/>
                    <input type="hidden" name="item_pkg_price" value=""/>
                    <input type="hidden" name="item_unit_price" value=""/>
                    <input type="hidden" name="route_id" value="${prescription.route_id}"/>
                    <input type="hidden" name="route_name" value="${prescription.route_name}"/>
                    <input type="hidden" name="consumption_uom" value="${prescription.consumption_uom}"/>
                    <input type="hidden" name="qty_in_stock" value=""/>
                    <input type="hidden" name="priorAuth" value="${priorAuth}"/>
                    <input type="hidden" name="requirePriorAuth" value="${prescription.preauth_required}"/>
                    <input type="hidden" name="item_form_id" value="${prescription.item_form_id == 0 ? '' : prescription.item_form_id}"/>
                    <input type="hidden" name="item_strength" value="${prescription.item_strength}"/>
                    <input type="hidden" name="item_strength_units" value="${prescription.item_strength_units}"/>
                    <input type="hidden" name="tooth_unv_number" value="${prescription.tooth_unv_number}"/>
                    <input type="hidden" name="tooth_fdi_number" value="${prescription.tooth_fdi_number}"/>
                    <input type="hidden" name="tooth_num_required" value="${prescription.tooth_num_required}"/>
                    <input type="hidden" name="non_hosp_medicine" value="${prescription.non_hosp_medicine}"/>
                    <input type="hidden" name="service_qty" value="${prescription.service_qty}"/>
                    <input type="hidden" name="addToFavourite" value="false"/>
                    
                    <input type="hidden" name="chargeId" value=""/>
                    <input type="hidden" name="chargeHeadId" value=""/>
                    <input type="hidden" name="amt" value=""/>
                    <input type="hidden" name="returnAmt" value=""/>
                    <input type="hidden" name="disc" value=""/>
                    <input type="hidden" name="insuranceCategoryId" value=""/>
                    <input type="hidden" name="delCharge" value=""/>
                    <input type="hidden" name="isClaimLocked" value="false"/>
                    <input type="hidden" name="priInsClaimAmt" value=""/>
                    <input type="hidden" name="secInsClaimAmt" value=""/>
                    <input type="hidden" name="chargeGroupId" value=""/>
                    <input type="hidden" name="priClaimAmt" value="0"/>
                    <input type="hidden" name="secClaimAmt" value="0"/>
                    <input type="hidden" name="orderPatientAmt" value=""/>
                    
                    <input type="hidden" name="temp_charge_id" value=""/>
                    <input type="hidden" name="itemCode" value=""/>
                    <input type="hidden" name="origRate" value=""/>
                    <input type="hidden" name="batchNo" value=""/>
                    <input type="hidden" name="qty" value=""/>
                    <input type="hidden" name="medDiscRS" value=""/>
                    <input type="hidden" name="medDiscType" value=""/>
                    <input type="hidden" name="primclaimAmt" value="0"/>
                    <input type="hidden" name="secclaimAmt" value="0"/>
                    <input type="hidden" name="itemBatchId" value=""/>
                    <input type="hidden" name="is_claim_locked" value="false"/>
                    <input type="hidden" name="category_payable" value=""/>
                    <input type="hidden" name="insuranceCategoryName" value=""/>
                    <input type="hidden" name="consultation_type_id" id="consultation_type_id" value="${consultation_bean.head}"/>
                    <input type="hidden" name="tax_amt" value="0"/>
                    <input type="hidden" name="tax" value="0"/>
                    <input type="hidden" name="op_id" value=""/>
                    <input type="hidden" name="returnAmt" value="0"/>
                    <input type="hidden" name="medicineId" value=""/>
                </td>
                <td>
                    <c:choose>
                        <c:when test="${prescription.item_type == 'Medicine' && prescription.master != 'op' && prescription.non_hosp_medicine == false && empty prescription.item_id}">
                            <insta:truncLabel value="${prescription.generic_name}" length="20"/>
                        </c:when>
                        <c:otherwise>
                            <c:set var="tooth_number" value=""/>
                            <c:if test="${not empty prescription.tooth_unv_number || not empty prescription.tooth_fdi_number}">
                                <c:set var="tooth_number" value="[${empty prescription.tooth_unv_number ? prescription.tooth_fdi_number : prescription.tooth_unv_number}]"/>
                            </c:if>
                            <insta:truncLabel value="${prescription.item_name} ${tooth_number}" length="20"/>
                        </c:otherwise>
                    </c:choose>
                </td>
                <c:choose>
                    <c:when test="${preferences.modulesActivatedMap.mod_ceed_integration == 'Y'}" >
                        <td>
                            <c:choose>
                                <c:when test="${!ceedstatus}">
                                    <div class="ceedcircle black"></div>&nbsp;Not Initiated
                                </c:when>
                                <c:otherwise>
                                    <c:choose>                
                                        <c:when test="${ceedResponseMap.containsKey(prescription.item_prescribed_id)}">
                                            <c:set var="rank" value="Normal"/>
                                            <c:set var="circle_color" value="green"/>
                                            <c:forEach items="${ceedResponseMap[prescription.item_prescribed_id]}" var="response">
                                                <c:choose>
                                                    <c:when test="${response.claim_edit_rank == 'A'}">
                                                        <c:set var="rank" value="Alert"/>
                                                        <c:set var="circle_color" value="red"/>
                                                    </c:when>
                                                    <c:when test="${response.claim_edit_rank == 'R' && rank != 'Alert'}">
                                                        <c:set var="rank" value="Review"/>
                                                        <c:set var="circle_color" value="yellow"/>
                                                    </c:when>
                                                    <c:when test="${response.claim_edit_rank == 'E'}">
                                                        <c:set var="rank" value="Error"/>
                                                        <c:set var="circle_color" value="orange"/>
                                                    </c:when>
                                                     <c:when test="${response.claim_edit_rank == 'NA'}">
                                                        <c:set var="rank" value="Not Applicable"/>
                                                        <c:set var="circle_color" value="grey"/>
                                                    </c:when>
                                                </c:choose>
                                            </c:forEach>
                                            <div class="ceedcircle ${circle_color}" ></div>&nbsp;${rank}
                                        </c:when>
                                        <c:when test="${prescription.item_type == 'Doctor' || prescription.item_type == 'NonHospital'
                                        	|| (prescription.item_type == 'Medicine' && prescription.non_hosp_medicine == true)
                                        	|| (prescription.item_type == 'Medicine' && (empty prescription.item_id or prescription.master == 'op'))}">
                                        	<div class="ceedcircle grey"></div>&nbsp;Not Applicable
                                        </c:when>
                                        <c:otherwise>
                                        	<div class="ceedcircle black"></div>&nbsp;Not Initiated
                                        </c:otherwise>
                                    </c:choose>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </c:when>
                </c:choose>
                <td>${tooth_number}</td>
                <td>
                    <insta:truncLabel value="${prescription.item_form_name}" length="15"/>
                </td>
                <td>
                    <insta:truncLabel value="${prescription.item_strength} ${prescription.unit_name}" length="15"/>
                </td>
                <td>
                    <insta:truncLabel value="${prescription.admin_strength}" length="15"/>
                </td>
                <td>
                    <c:if test="${(prescription.item_type == 'Medicine' || prescription.item_type == 'NonHospital') &&
                                    (not empty prescription.medicine_dosage or not empty prescription.duration)}">
                                <insta:truncLabel value="${prescription.medicine_dosage} / ${prescription.duration} ${prescription.duration_units} " length="20"/>
                    </c:if>
                </td>
                <td>
                    <label>
                        <c:if test="${prescription.item_type == 'Medicine'}">
                            ${prescription.route_name}
                        </c:if>
                    </label>
                </td>
                <td>
                    <insta:truncLabel value="${prescription.item_remarks}" length="20"/>
                </td>
                <td>
                    <insta:truncLabel value="${prescription.special_instr}" length="20"/>
                </td>
                <td>
                    <label>
                        <c:choose>
                            <c:when test="${prescription.item_type == 'Medicine' || prescription.item_type == 'NonHospital'}">
                                ${prescription.medicine_quantity}
                            </c:when>
                            <c:when test="${prescription.item_type == 'Service'}">
                                ${prescription.service_qty}
                            </c:when>
                        </c:choose>
                    </label>
                </td>
                    <!-- hide the unit price and pkg price when prescriptions done by generics -->
                <td class="number"><label></label></td>
                <td class="number"><label></label></td>
                <td class="number"><label></label></td>
                <td><label></label></td>
                <c:if test="${requireERxAuthorization}">
                <td>
                    <c:if test="${prescription.send_for_erx == 'Y' && prescription.item_type == 'Medicine' && prescription.non_hosp_medicine == false}">
                        <img src="${cpath}/images/${erxFlagColor}_flag.gif"/>
                        ${eRxStatusDisplay[prescription.erx_status]}
                    </c:if>
                </td>
                <td>${prescription.erx_denial_code}</td>
                <td>${prescription.denial_code_type}</td>
                <td>${prescription.drug_code}</td>
                <td><insta:truncLabel value="${empty prescription.denial_desc ? prescription.erx_denial_remarks : prescription.denial_desc}" length="15"/></td>
                <td><insta:truncLabel value="${prescription.example}" length="15"/></td>
                </c:if>
                <td style="text-align: center">
                    <c:choose>
                        <c:when test="${item_issued == 'O'}">
                            <img src="${cpath}/icons/delete_disabled.gif"" class="imgDelete button" />
                        </c:when>
                        <c:otherwise>
                            <a href="javascript:Cancel Item" onclick="return cancelItem(this);" title="Cancel Item" >
                                <img src="${cpath}/icons/delete.gif" class="imgDelete button" />
                            </a>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td style="text-align: center">
                    <input type="hidden" name="delPayment" id="delPayment" value="false" />
	                    <a name="_prescEditAnchor" href="javascript:Edit" onclick="return showEditItemDialog(this);"
	                        title="Edit One Time Prescription Details">
	                        <img src="${cpath}/icons/Edit.png" class="button" />
	                    </a>
                </td>
            </tr>
        </c:forEach>
    </table>
    <table class="addButton" style="height:25px;">
        <tr>
        	<td style="display: ${not empty ceedbean ? 'cell' : 'none'}">
        		<font >Cross-Code Check Last Run Date/Time: <b><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${ceedbean.map.response_datetime}"/></b></font>
        		<img class="imgHelpText" src="${cpath}/images/help.png" 
        			title='NOTE: If there have been any changes to the diagnosis / prescribed activities please make a fresh CEED check submission'/>
        	</td>
            <td class="number" style="text-align: right">
                Pkg. Price Total: <b><label id="estimatedPkgTotal"></label></b>
                &nbsp;&nbsp;Unit Price Total: <b><label id="estimatedUnitTotal"></label></b>
                &nbsp;&nbsp;Patient Portion: <b><label id="estimatedCopayTotal"></label></b>
                &nbsp;&nbsp;<insta:screenlink screenId="pres_audit_log" label="Management Audit Log" addPipe="false" target="_blank"
                extraParam="?_method=getAuditLogDetails&al_table=patient_prescription_audit_log_view&consultation_id=${param.consultation_id}&mr_no=${patient.mr_no}&patient_id=${patient.patient_id}"/>
            </td>
            <td style="width: 16px;text-align: right">
                <button type="button" name="btnAddItem" id="btnAddItem" title="Add One Time Prescription (Alt_Shift_+)"
                    onclick="showAddItemDialog(this); return false;"
                    accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
            </td>
            <td style="width: 16px;text-align: right">
                <button type="button" name="btnAddItem" id="btnAddItem" title="Prescribe using Previous Prescriptions"
                    onclick="getPreviousPrescriptions(this, '${patient.mr_no}', '${ifn:cleanJavaScript(param.consultation_id)}', '${consultation_bean.doctor_name}'); return false;"
                    class="imgButton"><img src="${cpath}/icons/Send.png"></button>
            </td>
            <td style="width: 16px;text-align: right">
                <button type="button" name="btnAddItem" id="btnAddItem" title="Prescribe using favourites"
                    onclick="showFavouritesDialog(this); return false;"
                    class="imgButton"><img src="${cpath}/icons/list-star.png"></button>
	        </td>
        </tr>
    </table>
    <fieldset class="fieldSetBorder" style="margin-top: 5px">
        <table class="formtable">
            <tr>
                <td class="formlabel" style="vertical-align: top">Patient Instructions:</td>
                <td colspan="5">
                    <textarea name="prescription_notes" style="width: 448px;"
                        id="prescription_notes" rows="2">${consultation_bean.prescription_notes}</textarea>
                </td>
            </tr>
        </table>
    </fieldset>
</div>
<div id="addItemDialog" style="display: none">
    <div class="bd">
        <div id="addItemDialogFields">
            <fieldset class="fieldSetBorder">
                <legend class="fieldSetLabel">Add Item</legend>
                <fieldset>
                    <legend class="fieldSetLabel">Item Type</legend>
                    <table>
                        <tr>
                            <td ><input type="radio" name="d_itemType" value="Medicine" checked onclick="onItemChange()"> Medicine</td>
                            <td ><input type="radio" name="d_itemType" value="Inv." onclick="onItemChange()"> Investigation</td>
                            <td ><input type="radio" name="d_itemType" value="Service" onclick="onItemChange()"> Service</td>
                            <td ><input type="radio" name="d_itemType" value="Operation" onclick="onItemChange()"> Operation</td>
                        </tr>
                        <tr>
                            <td><input type="radio" name="d_itemType" value="Doctor" onclick="onItemChange()"> Doctor Consultation</td>
                            <td><input type="radio" name="d_itemType" value="NonHospital" onclick="onItemChange()"> Non Hospital Items</td>
                        </tr>
                    </table>
                </fieldset>
                <fieldset class="fieldSetBorder">
                    <legend class="fieldSetLabel">Item Details</legend>
                    <table class="formtable">
                        <tr>
                            <td class="formlabel">Show Favourites: </td>
                            <td width="13px"><input type="checkbox" name="d_doctor_favourite" id="d_doctor_favourite" onchange="clearFieldsWhenChanged();"/> </td>
                            <td id="d_non_hosp_medicine_div" class="formlabel" >Non Hospital: </td>
                            <td id="d_non_hosp_medicine_div1"><input type="checkbox" id="d_non_hosp_medicine" value="NM" onchange="clearFieldsWhenChanged();"/></td>
                        </tr>
                        <tr>
                            <td class="formlabel" >Item: </td>
                            <td colspan="3">
                                <div style="float: left;">
                                    <div id="itemAutocomplete" style="padding-bottom: 20px; width: 350px">
                                        <input type="text" id="d_itemName" name="d_itemName" >
                                        <div id="itemContainer" style="width: 550px" class="scrolForContainer"></div>
                                        <input type="hidden" name="d_item_master" id="d_item_master" value=""/>
                                        <input type="hidden" name="d_generic_name" id="d_generic_name" value=""/>
                                        <input type="hidden" name="d_generic_code" id="d_generic_code" value=""/>
                                        <input type="hidden" name="d_drug_code" id="d_drug_code" value=""/>
                                        <input type="hidden" name="d_ispackage" id="d_ispackage" value=""/>
                                        <input type="hidden" name="d_package_size" id="d_package_size" value=""/>
                                        <input type="hidden" name="d_price" id="d_price" value=""/>
                                        <input type="hidden" name="d_item_id" id="d_item_id" value=""/>
                                        <input type="hidden" name="d_package_type" id="d_package_type" value=""/>
                                        <input type="hidden" name="d_qty_in_stock" id="d_qty_in_stock" value=""/>
                                        <input type="hidden" name="d_priorAuth" id="d_priorAuth" value=""/>
                                        <input type="hidden" name="d_tooth_num_required" id="d_tooth_num_required" value=""/>
                                        <input type="hidden" name="d_tooth_number" id="d_tooth_number" value=""/>
                                        <input type="hidden" name="d_granular_units" id="d_granular_units" value=""/>
                                        
                                        <input type="hidden" name="d_amt" id="d_amt" value=""/>
                                        <input type="hidden" name="d_disc" id="d_disc" value=""/>
                                        <input type="hidden" name="d_test_category" id="d_test_category" value=""/>
                                        <input type="hidden" name="d_chargeHeadId" id="d_chargeHeadId" value=""/>
                                        <input type="hidden" name="d_chargeGroupId" id="d_chargeGroupId" value=""/>
                                        
                                        <input type="hidden" name="d_itemCode" id="d_itemCode" value=""/>
                                        <input type="hidden" name="d_origRate" id="d_origRate" value=""/>
                                        <input type="hidden" name="d_batchNo" id="d_batchNo" value=""/>
                                        <input type="hidden" name="d_medDiscRS" id="d_medDiscRS" value=""/>
                                        <input type="hidden" name="d_medDiscType" id="d_medDiscType" value=""/>
                                        <input type="hidden" name="d_itemBatchId" id="d_itemBatchId" value=""/>
                                        <input type="hidden" name="d_med_discount" id="d_med_discount" value=""/>
                                        <input type="hidden" name="d_med_disc_type" id="d_med_disc_type" value=""/>
                                        <input type="hidden" name="d_insurance_category_id" id="d_insurance_category_id" value=""/>
                                        <input type="hidden" name="d_insurance_category_name" id="d_insurance_category_name" value=""/>
                                        <input type="hidden" name="d_category_payable" id="d_category_payable" value=""/>
                                    </div>
                                </div>

                            </td>
                            <td class="forminfo" id="pkg_details_button" style="display: none;">
                                    <input type="hidden" name="pkgid" id="pkgid" value=""/>
                                            <input type="button"  name="btnValuePkg" id="btnValuePkg" style="margin-top: 5px;margin-right: 120px;" title="Additional Package Details.."
                                                onclick="getPackageDetails(this);" accesskey="O" class="button"  value="..."> </button>
                            </td>
                            <td class="formlabel" id="d_toothLabelTd">Tooth Number: </td>
                            <td id="d_toothValueTd">
                                <div id="dToothNumberDiv" style="width: 120px; float: left;"></div>
                                <div class="multiInfoEditBtn" style="float: left;margin-left: 10px" id="dToothNumBtnDiv">
                                    <a href="javascript:void(0);" onclick="return showToothNumberDialog('add', this);"
                                        title="Select Tooth Numbers">
                                        <img src="${cpath}/icons/Edit.png" class="button"/>
                                    </a>
                                </div>
                                <div id="dToothNumDsblBtnDiv" style="float: left;margin-left: 10px;">
                                    <img src="${cpath}/icons/Edit1.png" class="button"/>
                                </div>
                            </td>
                        </tr>
                        <tr >
                            <td class="formlabel" >Generic: </td>
                            <td colspan="5"><a id="genericNameAnchor_dialog" style="display: none"></a></td>
                        </tr>
                        <tr id="d_drug_code_show">
                            <td class="formlabel" >Drug Code: </td>
                            <td colspan="5"><b><label id="d_drug_code_label" ></label></b></td>
                        </tr>
                        <tr id="d_category_payable_show">
                            <td colspan="1" class="formlabel">Covered by Insurance: </td>
                            <td><b><label id="d_category_payable_label"></label></b></td>
                            <td class="formlabel">Insurance Category: </td>
							<td ><b><label id="d_insurance_category_name_label"></label></b></td>
                        </tr>
                        <tr>
                            <td class="formlabel" >Pkg. Size: </td>
                            <td><b><label id="d_pkg_size_label"></label></b></td>
                            <td class="formlabel">Price: </td>
                            <td ><b><label id="d_price_label"></label></b></td>
                        </tr>
                        <tr>
                            <td class="formlabel"><label id="d_priorAuthLabelTd">Prior Auth: </label></td>
                            <td ><b><label id="d_priorAuth_label"></label></b></td>
                            <c:if test="${mod_eclaim_preauth}">
                                <td class="formlabel"> <label id="d_markPriorAuthReqTd">Send For Prior Auth: </label></td>
                                <td id="d_markPriorAuthCheckBox">
                                    <input type="checkbox" id="d_markPriorAuthReq" value="">
                                </td>
                            </c:if>
                        </tr>
                        <tr id="d_itemFormRow">
                            <td class="formlabel" >Item Form: </td>
                            <td><insta:selectdb name="d_item_form_id" id="d_item_form_id" table="item_form_master"
                                displaycol="item_form_name" valuecol="item_form_id" filtercol="status" filtervalue="A" orderby="item_form_name"
                                dummyvalue="-- Select --" dummyvalueId="" onchange="setGranularUnit(event, 'd');"/>
                            </td>
                            <td class="formlabel">Strength</td>
                            <td >
                                <div style="width: 138px">
                                    <input type="text" name="d_item_strength" id="d_item_strength" value="" style="width: 60px;">
                                    <insta:selectdb name="d_item_strength_units" id="d_item_strength_units" table="strength_units" displaycol="unit_name" valuecol="unit_id"
                                            dummyvalue="Select --" style="width: 60px;"/>
                                </div>
                            </td>
                            <td class="formlabel"></td>
                            <td></td>
                        </tr>
                    </table>
                </fieldset>
                <fieldset class="fieldSetBorder">
                    <legend class="fieldSetLabel">Management</legend>
                    <table class="formtable">
                        <tr>
                            <td class="formlabel">Admin Strength: </td>
                            <td>
                                <input type="text" name="d_admin_strength" id="d_admin_strength" value="" maxlength="98"/>
                            </td>
                            <td></td>
                            <td></td>
                            <td class="formlabel">Route: </td>
                            <td>
                                <select id="d_medicine_route" class="dropdown">
                                    <option value="">-- Select --</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td class="formlabel">Number Of Units (Dosage): </td>
                            <td colspan="5">
                                <input type="text" name="d_strength" id="d_strength" value="" onchange="calcQty('d'); setAutoGeneratedInstruction('d');"/>
                                <input type="text" name="d_consumption_uom" id="d_consumption_uom" value="" onchange="modifyUOMLabel(this, 'd'); setAutoGeneratedInstruction('d');"/> Granular Units
                            </td>
                        </tr>
                        <tr>
                            <td class="formlabel">Duration: </td>
                            <td>
                                <input type="text" name="d_duration" id="d_duration" value="" onkeypress="return enterNumOnlyzeroToNine(event);"
                                    onchange="calcQty('d'); setAutoGeneratedInstruction('d');"/>
                            </td>
                            <td colspan="2" >
                                <div style="width: 190px">
                                    <input type="radio" name="d_duration_units" value="D" onchange="calcQty('d'); setAutoGeneratedInstruction('d');">Days
                                    <input type="radio" name="d_duration_units" value="W" onchange="calcQty('d'); setAutoGeneratedInstruction('d');">Weeks
                                    <input type="radio" name="d_duration_units" value="M" onchange="calcQty('d'); setAutoGeneratedInstruction('d');">Months
                                </div>
                            </td>
                            <td class="formlabel">Frequency: </td>
                            <td>
                                <div id="frequencyAutoComplete" style="width: 138px">
                                    <input type="text" name="d_frequency" id="d_frequency" onchange="setAutoGeneratedInstruction('d');" maxlength="150">
                                    <div id="frequencyContainer" style="width: 300px;"></div>
                                    <input type="hidden" name="d_per_day_qty" id="d_per_day_qty" value=""/>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td class="formlabel">Total Qty: </td>
                            <td>
                                <input type="text" name="d_qty" id="d_qty" value="" maxlength="5" onkeypress="return enterNumOnlyzeroToNine(event);"/>
                            </td>
                            <td colspan="2"><label id="d_consumption_uom_label"></label></td>
                            <td class="formlabel">Add To Favourites: </td>
                            <td><input type="checkbox" name="d_addToFavourite" id="d_addToFavourite"/></td>
                        </tr>
                        <tr>
							<td class="formlabel">Refills: </td>
							<td>
								<input type="text" id="d_refills" name="d_refills" />
							</td>
						</tr>
                        <tr>
                            <td class="formlabel" >Instructions: </td>
                            <td colspan="5">
                                <div id="remarksAutoComplete" style="width: 500px">
                                    <input type="text" name="d_remarks" id="d_remarks" value="" style="width: 500px">
                                    <div id="remarksContainer" class="scrolForContainer"></div>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td class="formlabel">Special Instructions: </td>
                            <td colspan="5"><textarea name="d_special_instruction" id="d_special_instruction" style="width: 500px;" cols="50" rows="2" ></textarea></td>
                        </tr>
                    </table>
            </fieldset>
        </div>
        <table style="margin-top: 10">
            <tr>
                <td>
                    <button type="button" name="Add" id="Add" accesskey="A" >
                        <b><u>A</u></b>dd
                    </button>
                    <input type="button" name="Close" value="Close" id="Close"/>
                </td>
            </tr>
        </table>
    </div>
</div>
<div id="editItemDialog" style="display: none">
    <input type="hidden" name="editRowId" id="editRowId" value=""/>
    <div class="bd">
        <fieldset class="fieldSetBorder">
             <c:if test="${preferences.modulesActivatedMap.mod_ceed_integration == 'Y' && (roleId == 1 || roleId == 2 ||  actionRightsMap.view_ceed_response_comments == 'A')}" >
                <fieldset class="fieldSetBorder" id="ceedcommentsfieldset" style="display: block">
                    <legend class="fieldSetLabel">Cross Coding Comments</legend>
                    <ul class="numbers" id="ceed_response_comments">
                    </ul>
                </fieldset>
            </c:if>
            <legend class="fieldSetLabel">Edit Item</legend>
            <fieldset class="fieldSetBorder">
                <legend class="fieldSetLabel">Item Details</legend>
                <table class="formtable">
                    <tr>
                        <td class="formlabel">Type: </td>
                        <td>
                            <label id="ed_itemTypeLabel" style="font-weight: bold"></label>
                            <input type="hidden" id="ed_itemType" name="ed_itemType" value=""/>
                        </td>
                    </tr>
                    <tr>
                        <td class="formlabel">Item: </td>
                        <td colspan="3">
                            <label id="ed_itemNameLabel" style="font-weight: bold"></label>
                            <input type="hidden" id="ed_itemName" name="ed_itemName" value="">
                            <input type="hidden" id="ed_item_master" name="ed_item_master" value=""/>
                            <input type="hidden" id="ed_ispackage" name="ed_ispackage" value=""/>
                            <input type="hidden" name="ed_package_size" id="ed_package_size" value=""/>
                            <input type="hidden" name="ed_price" id="ed_price" value=""/>
                            <input type="hidden" name="ed_item_id" id="ed_item_id" value=""/>
                            <input type="hidden" name="ed_priorAuth" id="ed_priorAuth" value=""/>
                            <input type="hidden" name="ed_tooth_num_required" id="ed_tooth_num_required" value=""/>
                            <input type="hidden" name="ed_non_hosp_medicine" id="ed_non_hosp_medicine" value=""/>
                            <input type="hidden" name="ed_granular_units" id="ed_granular_units" value=""/>
                            <input type="hidden" name="ed_qty_edited" id="ed_qty_edited" value="false"/>
                            <input type="hidden" name="ed_category_payable" id="ed_category_payable" value=""/>
                            <input type="hidden" name="ed_insurance_category_name" id="ed_insurance_category_name" value=""/>
                        </td>
                        <td class="formlabel" id="ed_toothLabelTd">Tooth Number: </td>
                        <td id="ed_toothValueTd">
                            <input type="hidden" name="ed_tooth_number" id="ed_tooth_number" value=""/>
                            <div id="edToothNumberDiv" style="width: 120px; float: left"></div>
                            <div class="multiInfoEditBtn" style="float: left" id="edToothNumBtnDiv">
                                <a href="javascript:void(0);" onclick="return showToothNumberDialog('edit', this);"
                                    title="Select Tooth Numbers">
                                    <img src="${cpath}/icons/Edit.png" class="button"/>
                                </a>
                            </div>
                            <div id="edToothNumDsblBtnDiv" style="float: left; display: none">
                                <img src="${cpath}/icons/Edit1.png" class="button"/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="formlabel">Generic: </td>
                        <td colspan="5"><a id="genericNameAnchor_editdialog" style="display:none"></a></td>
                    </tr>
                    <tr>
                        <td class="formlabel">Drug Code: </td>
                        <td colspan="5"><b><label id="ed_drug_code_label" ></label></b></td>
                    </tr>
                    <tr id="ed_category_payable_show">
                        <td class="formlabel">Covered by Insurance: </td>
                        <td><b><label id="ed_category_payable_label"></label></b></td>
                        <td class="formlabel">Insurance Category: </td>
						<td ><b><label id="ed_insurance_category_name_label"></label></b></td>
                    </tr>
                    <tr>
                        <td class="formlabel">Pkg. Size: </td>
                        <td><b><label id="ed_pkg_size_label"></label></b></td>
                        <td class="formlabel">Price: </td>
                        <td><b><label id="ed_price_label"></label></b></td>
                    </tr>
                    <tr>
                        <td class="formlabel" ><label id="ed_priorAuthLabelTd">Prior Auth: </label></td>
                        <td ><b><label id="ed_priorAuth_label"></label></b></td>
                        <c:if test="${mod_eclaim_preauth}">
                            <td class="formlabel"> <label id="ed_markPriorAuthReqTd">Send For Prior Auth: </label></td>
                            <td id="ed_markPriorAuthCheckBox">
                                <input type="checkbox" id="ed_markPriorAuthReq" value="" onchange="setEdited();">
                            </td>
                        </c:if>
                    </tr>
                    <tr id="ed_itemFormRow">
                        <td class="formlabel">Item Form: </td>
                        <td><insta:selectdb name="ed_item_form_id" id="ed_item_form_id" table="item_form_master"
                            displaycol="item_form_name" valuecol="item_form_id" filtercol="status" filtervalue="A" orderby="item_form_name"
                            dummyvalue="-- Select --" dummyvalueId="" onchange="setGranularUnit(event, 'ed');"/>
                        </td>
                        <td class="formlabel">Strength</td>
                        <td>
                            <div style="width: 138px">
                                <input type="text" name="ed_item_strength" id="ed_item_strength" value="" style="width: 60px" onchange="setEdited();">
                                <insta:selectdb name="ed_item_strength_units" id="ed_item_strength_units" table="strength_units" displaycol="unit_name" valuecol="unit_id"
                                    dummyvalue="Select --" style="width: 60px" onchange="setEdited();"/>
                            </div>
                        </td>
                        <td class="formlabel"></td>
                        <td></td>
                    </tr>
                </table>
            </fieldset>
            <fieldset class="fieldSetBorder">
                <legend class="fieldSetLabel">Management</legend>
                <table class="formtable">
                    <tr>
                        <td class="formlabel">Admin Strength: </td>
                        <td>
                            <input type="text" name="ed_admin_strength" id="ed_admin_strength" value="" onchange="setEdited();" maxlength="98"/>
                        </td>
                        <td></td>
                        <td></td>
                        <td class="formlabel">Route: </td>
                        <td><b><label id="ed_medicine_route" title=""></label></b></td>
                    </tr>
                    <tr>
                        <td class="formlabel">Number Of Units (Dosage): </td>
                        <td colspan="5">
                            <input type="text" name="ed_strength" id="ed_strength" value="" onchange="setEdited(); calcQty('ed'); setAutoGeneratedInstruction('ed');"/>
                            <input type="text" name="ed_consumption_uom" id="ed_consumption_uom" value="" onchange="modifyUOMLabel(this, 'ed');setAutoGeneratedInstruction('ed');setEdited();"/> Granular Units
                        </td>
                    </tr>
                    <tr>
                        <td class="formlabel">Duration: </td>
                        <td><input type="text" name="ed_duration" id="ed_duration" value="" onkeypress="return enterNumOnlyzeroToNine(event);" onchange="setEdited(); calcQty('ed'); setAutoGeneratedInstruction('ed');" /></td>
                        <td colspan="2">
                            <div style="width: 190px">
                                <input type="radio" name="ed_duration_units" value="D" onchange="setEdited(); calcQty('ed'); setAutoGeneratedInstruction('ed');">Days
                                <input type="radio" name="ed_duration_units" value="W" onchange="setEdited(); calcQty('ed'); setAutoGeneratedInstruction('ed');">Weeks
                                <input type="radio" name="ed_duration_units" value="M" onchange="setEdited(); calcQty('ed'); setAutoGeneratedInstruction('ed');">Months
                            </div>
                        </td>
                        <td class="formlabel">Frequency: </td>
                        <td>
                            <div id="ed_frequencyAutoComplete" style="width: 138px;">
                                <input type="text" name="ed_frequecy" id="ed_frequency" value="" onchange="setEdited(); setAutoGeneratedInstruction('ed');" maxlength="150"/>
                                <input type="hidden" name="ed_frequency_hidden" id="ed_frequency_hidden" value=""/>
                                <input type="hidden" name="ed_per_day_qty" id="ed_per_day_qty" value=""/>
                                <div id="ed_frequencyContainer" style="width: 300px;"></div>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="formlabel">Total Qty: </td>
                        <td >
                            <input type="text" name="ed_qty" id="ed_qty" value="" maxlength="5" onkeypress="return enterNumOnlyzeroToNine(event);" onchange="setEdited();qtyEdited('ed');"/>
                        </td>
                        <td colspan="2"><label id="ed_consumption_uom_label"></label></td>
                        <td class="formlabel"></td>
                        <td></td>
                    </tr>
                    <tr>
						<td class="formlabel">Refills: </td>
						<td>
							<input type="text" id="ed_refills" name="ed_refills" />
						</td>
					</tr>
                    <tr>
                        <td class="formlabel">Instructions: </td>
                        <td colspan="5">
                            <div id="ed_remarksAutoComplete" style="width: 500px">
                                <input type="text" name="ed_remarks" id="ed_remarks" value="" style="width: 500px" onchange="setEdited();">
                                <div id="ed_remarksContainer" class="scrolForContainer"></div>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="formlabel">Special Instructions: </td>
                        <td colspan="5"><textarea name="ed_special_instruction" id="ed_special_instruction" style="width: 500px;" cols="50" rows="2" onchange="setEdited();"></textarea></td>
                    </tr>
                </table>
                <table style="margin-top: 10">
                    <tr>
                        <td>
                            <input type="button" id="editOk" name="editok" value="Ok">
                            <input type="button" id="editCancel" name="cancel" value="Cancel" />
                            <input type="button" id="editPrevious" name="previous" value="<<Previous" />
                            <input type="button" id="editNext" name="next" value="Next>>"/>
                        </td>
                    </tr>
                </table>
            </fieldset>
        </fieldset>
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

<div id="valuePkgDialog" style="display:none;">
        <div class="bd">
            <div style="margin-left: 280px" id="paginationDiv"></div>
            <fieldset class="fieldSetBorder" >
                <legend class="fieldSetLabel">Package Summary</legend>
                <table class="formtable" id="staticpackageDetailsTab">
                    <tr>
                        <td class="forminfo" style="width:10px;text-align:right;">DESCRIPTION</td>
                        <td class="forminfo" style="width:10px;text-align:center;">TYPE</td>
                        <td class="forminfo" style="width:10px;text-align:center;">QUANTITY</td>
                    </tr>
                    <tr>
                    </tr>
            </table>
            </fieldset>
        </div>
    </div>

