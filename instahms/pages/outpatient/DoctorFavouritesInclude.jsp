<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<div id="doctorFavouritesDialog" style="display: none">
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Doctor Favourites</legend>
			<span style="float:right">
				<span id="fav_prev_link" style="cursor:pointer;float:left"><a onclick="showFavouritesDialog(this, 'prev'); return false;" >&lt;&nbsp;Prev</a></span>
				<span id="fav_pipe" style="float:left">&nbsp;|&nbsp;</span>
				<span id="fav_next_link" style="cursor:pointer;float:right"><a onclick="showFavouritesDialog(this, 'next'); return false;" >Next&nbsp;&gt;</a></span>
			</span>
			<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" border="0" width="100%" id="doctorPrescriptionFavouritesTable">
				<tr>
					<th width="20px">Select</th>
					<th>Type</th>
					<th class="number" width="50px">Dis. Order</th>
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
				<c:set var="numfavourites" value="0"/>
				<c:forEach begin="1" end="${numfavourites+1}" var="i" varStatus="loop">
					<c:set var="favourite" value="<%= new java.util.HashMap()%>"/>
					<c:if test="${empty favourite}">
						<c:set var="style" value='style="display:none"'/>
					</c:if>
					<tr ${style}>
						<td><c:set var="item_prescriptions_by_generics" value="${favourite.master != 'op' && empty favourite.item_id ? 'true' : 'false'}"/>
							<c:choose>
								<c:when test="${favourite.item_type == 'Medicine' && not favourite.non_hosp_medicine && (not empty erxBean.map.erx_reference_no || (item_prescriptions_by_generics != prescriptions_by_generics))}">
									<!--  If the Erx request is already sent, disable the medicine items, do not allow medicine. -->
									<input type="checkbox" name="select_favourite" id="select_favourite" value="${favourite.favourite_id}" disabled/>
								</c:when>
								<c:otherwise>
									<input type="checkbox" name="select_favourite" id="select_favourite" value="${favourite.favourite_id}"/>
								</c:otherwise>
							</c:choose>
						</td>
						<td>
							<img src="${cpath}/images/empty_flag.gif"/>
							<label>${favourite.item_type}
								<c:if test="${favourite.item_type == 'Medicine' && favourite.non_hosp_medicine}">
									[Non Hosp]
								</c:if>
							</label>
							<input type="hidden" name="fav_favourite_id" value="${favourite.favourite_id}"/>
							<input type="hidden" name="fav_itemType" value="${favourite.item_type}"/>
							<c:choose>
								<c:when test="${favourite.item_type == 'Medicine' && empty favourite.item_id && not empty favourite.generic_name}">
									<input type="hidden" name="fav_item_name" value="<c:out value='${favourite.generic_name}'/>"/>
									<input type="hidden" name="fav_item_id" value="${favourite.generic_code}"/>
								</c:when>
								<c:otherwise>
									<input type="hidden" name="fav_item_name" value="<c:out value='${favourite.item_name}'/>"/>
									<input type="hidden" name="fav_item_id" value="${favourite.item_id}"/>
								</c:otherwise>
							</c:choose>
							<c:set var="priorAuth" value="${not empty patient.primary_sponsor_id ? favourite.prior_auth_required : ''}"/>
							<input type="hidden" name="fav_strength" value="${favourite.strength}"/>
							<input type="hidden" name="fav_admin_strength" value="${favourite.admin_strength}"/>
							<input type="hidden" name="fav_granular_units" value="${favourite.granular_units}"/>
							<input type="hidden" name="fav_frequency" value="${ifn:cleanHtmlAttribute(favourite.frequency)}"/>
							<input type="hidden" name="fav_duration" value="${favourite.duration}"/>
							<input type="hidden" name="fav_duration_units" value="${favourite.duration_units}"/>
							<input type="hidden" name="fav_medicine_quantity" value="${favourite.medicine_quantity}"/>
							<input type="hidden" name="fav_item_remarks" value="${favourite.item_remarks}"/>
							<input type="hidden" name="fav_special_instr" value="${ifn:cleanHtmlAttribute(favourite.special_instr)}"/>
							<input type="hidden" name="fav_item_master" value="${favourite.master}"/>
							<input type="hidden" name="fav_ispackage" id="ispackage" value="${favourite.ispackage}"/>
							<input type="hidden" name="fav_generic_code" value="${favourite.generic_code}"/>
							<input type="hidden" name="fav_drug_code" value="${favourite.drug_code}"/>
							<input type="hidden" name="fav_generic_name" value="${favourite.generic_name}"/>
							<input type="hidden" name="fav_edited" value='false'/>
							<input type="hidden" name="fav_delItem" id="delItem" value="false" />
							<input type="hidden" name="fav_route_id" value="${favourite.route_id}"/>
							<input type="hidden" name="fav_route_name" value="${favourite.route_name}"/>
							<input type="hidden" name="fav_consumption_uom" value="${favourite.consumption_uom}"/>
							<input type="hidden" name="fav_item_form_id" value="${favourite.item_form_id == 0 ? '' : favourite.item_form_id}"/>
							<input type="hidden" name="fav_item_form_name" value="${favourite.item_form_name}"/>
							<input type="hidden" name="fav_item_strength" value="${favourite.item_strength}"/>
							<input type="hidden" name="fav_item_strength_units" value="${favourite.item_strength_units}"/>
							<input type="hidden" name="fav_item_strength_unit_name" value="${favourite.unit_name}"/>
							<input type="hidden" name="fav_display_order" value="${favourite.display_order}"/>
							<input type="hidden" name="fav_tooth_num_required" value="${favourite.tooth_num_required}"/>
							<input type="hidden" name="fav_non_hosp_medicine" value="${favourite.non_hosp_medicine}"/>
							<input type="hidden" name="fav_priorAuth" value="${priorAuth}"/>
							<input type="hidden" name="fav_insurance_category_id" value="${favourite.insurance_category_id}"/>
							<input type="hidden" name="fav_insurance_category_name" value="${favourite.insurance_category_name}"/>
						</td>
						<td class="number">${favourite.display_order}</td>
						<td>
							<c:choose>
								<c:when test="${favourite.item_type == 'Medicine' && empty favourite.item_id && not empty favourite.generic_name}">
									<insta:truncLabel value="${favourite.generic_name}" length="20"/>
								</c:when>
								<c:otherwise>
									<insta:truncLabel value="${favourite.item_name}" length="20"/>
								</c:otherwise>
							</c:choose>
						</td>
						<td>
							<insta:truncLabel value="${favourite.item_form_name}" length="15"/>
						</td>
						<td>
							<insta:truncLabel value="${favourite.item_strength} ${favourite.unit_name}" length="15"/>
						</td>
						<td>
							<insta:truncLabel value="${favourite.admin_strength}" length="15"/>
						</td>
						<td>
							<c:if test="${(favourite.item_type == 'Medicine' || favourite.item_type == 'NonHospital') &&
											(not empty favourite.medicine_dosage or not empty favourite.duration)}">
										<insta:truncLabel value="${favourite.medicine_dosage} / ${favourite.duration} ${favourite.duration_units}" length="20"/>
							</c:if>
						</td>
						<td>
							<label>
								<c:if test="${favourite.item_type == 'Medicine'}">
									${favourite.route_name}
								</c:if>
							</label>
						</td>
						<td>
							<insta:truncLabel value="${favourite.item_remarks}" length="20"/>
						</td>
						<td>
							<insta:truncLabel value="${favourite.special_instr}" length="20"/>
						</td>
						<td>
							<label><c:if test="${favourite.item_type == 'Medicine'}">
									${favourite.medicine_quantity}
									</c:if>
							</label>
						</td>
					</tr>
				</c:forEach>
			</table>
		</fieldset>
		<table style="margin-top: 10px;">
			<tr>
				<td>
					<input type="button" id="fav_Ok" name="fav_Ok" value="Ok">
					<input type="button" name="fav_Close" id="fav_Close" value="Close"/>
				</td>
			</tr>
		</table>
	</div>
</div>
