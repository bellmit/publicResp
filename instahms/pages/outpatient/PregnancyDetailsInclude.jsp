<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<fmt:parseNumber var="sectionId" type="number" value="${param.section_id}" />
<c:set var="finalized" value="${not empty section_finalize_status ? section_finalize_status[(sectionId).intValue()].finalized : 'N'}"/>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="stn_right_access"
	value="${((roleId == 1 || roleId == 2 || fn:contains(section_rights, param.section_id)) && finalized != 'Y')}"/>
<script>
	YAHOO.util.Event.onContentReady("content", initPregnancyDetails);
	var pregnancy_details_form = '${ifn:cleanJavaScript(param.form_name)}';
</script>
<fieldSet class="fieldSetBorder">
<legend class="fieldSetLabel" style="margin-top: 10px">Obstetric History
<c:forEach var="stn" items="${sys_generated_section}">
	<c:if test="${stn.section_mandatory && stn.section_id == param.section_id && stn_right_access}">
		<span class="star">*</span>
	</c:if>
</c:forEach>
<c:if test="${!(roleId == 1 || roleId == 2 || fn:contains(section_rights, param.section_id))}">
	<i>[Read-Only]</i>
</c:if>
<c:if test="${finalized == 'Y'}">
	<i>[Finalized]</i>
</c:if>
</legend>
<c:set var="numPregnancyDetails" value="${pregnancyhistoriesBean[0]}"/>
<input type="hidden" name="obstetric_record_id" value="${numPregnancyDetails.obstetric_record_id}"/>
	<div class="resultList">
	<table class="formtable">
		<tr>
			<td class="formlabel">G: </td>
			<td style="width:200px;" align="left">
				<input type="text" name="field_g" id="field_g" value="${numPregnancyDetails.field_g}" ${stn_right_access ? '' : 'Readonly' }/>
			</td>
			
			<td class="formlabel">P: </td>
			<td style="width:200px;" align="left">
				<input type="text" name="field_p" id="field_p" value="${numPregnancyDetails.field_p}" ${stn_right_access ? '' : 'Readonly' }/>
			</td>
			
			<td class="formlabel">L: </td>
			<td style="width:200px;" align="left"> 
				<input type="text" name="field_l" id="field_l" value="${numPregnancyDetails.field_l}" ${stn_right_access ? '' : 'Readonly' }/>
			</td>
			
			<td class="formlabel">A: </td>
			<td style="width:200px;" align="left">
				<input type="text" name="field_a" id="field_a" value="${numPregnancyDetails.field_a}" ${stn_right_access ? '' : 'Readonly' }/>
			</td>			
		</tr>		
	</table> 
	
	<table class="detailList dialog_displayColumns" id="pregnancyDetailsTable" cellspacing="0" cellpadding="0" border="0" width="100%" style="margin-top: 8px">
		<tr>
			<th>Date</th>
			<th>Weeks</th>
			<th>Place</th>
			<th>Method</th>
			<th>Weight</th>
			<th>Sex</th>
			<th>Complications</th>
			<th>Feeding</th>
			<th>Outcome</th>
			<th style="width: 16px;"></th>
			<th style="width: 16px;"></th>
		</tr>
		<c:set var="numPregnancyDetails" value="${fn:length(pregnancyhistories)}"/>
		<c:forEach begin="1" end="${numPregnancyDetails+1}" var="i" varStatus="loop">
			<c:set var="pregnancydetails" value="${pregnancyhistories[i-1].map}"/>
			<c:if test="${empty pregnancydetails}">
				<c:set var="style" value='style="display:none"'/>
			</c:if>
			<tr ${style}>
				<td>
					<img src="${cpath}/images/empty_flag.gif"/>
					<fmt:formatDate var="pregnancy_history_date" pattern="dd-MM-yyyy " value="${pregnancydetails.date}"/>
					<input type="hidden" name="pregnancy_history_id" value="${pregnancydetails.pregnancy_history_id}"/>
					<input type="hidden" name="pregnancy_date" value="${pregnancy_history_date}"/>
					<input type="hidden" name="mr_no" value="${pregnancydetails.mr_no}"/>
					<input type="hidden" name="pregnancy_weeks" value="<c:out value='${pregnancydetails.weeks}'/>"/>
					<input type="hidden" name="pregnancy_place" value="<c:out value='${pregnancydetails.place}'/>"/>
					<input type="hidden" name="pregnancy_method" value="<c:out value='${pregnancydetails.method}'/>"/>
					<input type="hidden" name="pregnancy_weight" value="<c:out value='${pregnancydetails.weight}'/>"/>
					<input type="hidden" name="pregnancy_sex" value="${ifn:cleanHtmlAttribute(pregnancydetails.sex)}"/>
					<input type="hidden" name="pregnancy_complications" value="<c:out value='${pregnancydetails.complications}'/>"/>
					<input type="hidden" name="pregnancy_feeding" value="<c:out value='${pregnancydetails.feeding}'/>"/>
					<input type="hidden" name="pregnancy_outcome" value="<c:out value='${pregnancydetails.outcome}'/>"/>
					<input type="hidden" name="pregnancy_deleted" value="false" />
					<input type="hidden" name="pregnancy_edited" value="false" />
					<label>${pregnancy_history_date}</label>
				</td>
				<td>
					<insta:truncLabel value="${pregnancydetails.weeks}" length="20"/>
				</td>
				<td>
					<insta:truncLabel value="${pregnancydetails.place}" length="20"/>
				</td>
				<td>
					<insta:truncLabel value="${pregnancydetails.method}" length="20"/>
				</td>
				<td>
					<insta:truncLabel value="${pregnancydetails.weight}" length="20"/>
				</td>
				<td>
					<c:choose>
						<c:when test="${pregnancydetails.sex == 'M'}"><label>Male</label></c:when>
						<c:when test="${pregnancydetails.sex == 'F'}"><label>Female</label></c:when>
						<c:when test="${pregnancydetails.sex == 'O'}"><label>Unknown</label></c:when>
					</c:choose>
				</td>
				<td>
					<insta:truncLabel value="${pregnancydetails.complications}" length="30"/>
				</td>
				<td>
					<insta:truncLabel value="${pregnancydetails.feeding}" length="30"/>
				</td>
				<td>
					<insta:truncLabel value="${pregnancydetails.outcome}" length="30"/>
				</td>
				<c:if test="${stn_right_access}">
					<td style="width: 16px; text-align: center">
						<a href="javascript:Cancel Obstetric" onclick="return cancelPregnancyDetails(this);" title="Cancel Obstetric" >
							<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
						</a>
					</td>
					<td style="width: 16px; text-align: center">
						<a name="pregnancyEditAnchor" href="javascript:Edit Obstetric Details" onclick="return showEditPregnancyDialog(this);"
							title="Edit Obstetric Details">
							<img src="${cpath}/icons/Edit.png" class="button" />
						</a>
					</td>
				</c:if>
			</tr>
		</c:forEach>
	</table>
	<table class="addButton" style="width:100%; white-space:nowrap;">
		<tr>
			<td style="width:100%;"></td>
			<c:if test="${stn_right_access}">
				<td>
					<button type="button" name="btnAddPregnancy" id="btnAddPregnancy" title="Add Obstetric Details"
						onclick="showAddPregnancyDialog(this); return false;"
						accessKey="A" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
				</td>
			</c:if>
			<td style="padding-right:5px;">Finalize</td>
			<td> <span><input class="finalize" id="${param.section_id}_finalized"  type="checkbox" value="true" ${ finalized == 'Y' ? "checked" : "" } onclick="changeFinalized(this,${param.section_id});"  
																									${finalized == 'Y' ? ((roleId == 1 || roleId == 2 || actionRightsMap.undo_section_finalization == 'A') ? '' : 'disabled') : 
																									((roleId == 1 || roleId == 2 || fn:contains(section_rights, param.section_id)) ? '' : 'disabled')}></input>
																									<input type="hidden" value="${finalized}" name="${param.section_id}_finalized" ></span> </td>

		</tr>
	</table>
</div>
</fieldSet>
<div id="addPregnancyDialog" style="display: none">
	<div class="bd">
		<div id="addPregnancyDialogFields">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Add Obstetric History</legend>
					<table class="formtable">
						<tr>
							<td class="formlabel">Date: </td>
							<td>
								<input type="hidden" name="d_pregnancy_history_id" id="d_pregnancy_history_id" />
								<insta:datewidget name="d_pregnancy_date" valid="past" id="d_pregnancy_date" btnPos="left"/>
							</td>
							<td class="formlabel">Week: </td>
							<td>
								<input type="text" name="d_pregnancy_week" id="d_pregnancy_week" />
							</td>
						</tr>

						<tr>
							<td class="formlabel">Place: </td>
							<td>
								<input type="text" name="d_pregnancy_place" id="d_pregnancy_place" maxlength="100"/>
							</td>
							<td class="formlabel">Method: </td>
							<td>
								<input type="text" name="d_pregnancy_method" id="d_pregnancy_method" maxlength="100"/>
							</td>
						</tr>

						<tr>
						<td class="formlabel">Weight: </td>
						<td style="width:200px;" align="left">
							<input type="text" name="d_pregnancy_weight" id="d_pregnancy_weight" />&nbsp;Kg
						</td>
						<td class="formlabel">Sex: </td>
						<td>
							<select class="dropdown" id="d_pregnancy_sex">
								<option value="">-- Select --</option>
								<option value="M">Male</option>
								<option value="F">Female</option>
								<option value="O">Unknown</option>
							</select>
						</td>
					</tr>

					<tr>
						<td class="formlabel">Complications: </td>
						<td colspan="3">
							<textarea name="d_pregnancy_complication" id="d_pregnancy_complication" rows="4" cols="50"></textarea>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Feeding: </td>
						<td>
							<input type="text" name="d_pregnancy_feeding" id="d_pregnancy_feeding"/>
						</td>
					</tr>

				<tr>
					<td class="formlabel">Out Come: </td>
					<td>
						<input type="text" name="d_pregnancy_outcome" id="d_pregnancy_outcome"/>
					</td>
				</tr>
			</table>
		</fieldset>
	</div>
	<table style="margin-top: 10">
			<tr>
				<td>
					<button type="button" name="pregnancyDetails_add_btn" id="pregnancyDetails_add_btn" accesskey="A" >
						<b><u>A</u></b>dd
					</button>
					<input type="button" name="pregnancyDetails_cancel_btn" value="Close" id="pregnancyDetails_cancel_btn"/>
				</td>
			</tr>
		</table>
	</div>
</div>

<div id="editPregnancyDialog" style="display: none">
	<input type="hidden" name="editPregnancyRowId" id="editPregnancyRowId" value=""/>
		<div class="bd">
			<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Edit Obstetric History</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Date: </td>
					<td>
						<input type="hidden" name="ed_pregnancy_history_id" id="ed_pregnancy_history_id" value=""/>
						<insta:datewidget name="ed_pregnancy_date"  valid="past" id="ed_pregnancy_date" btnPos="left" extravalidation="setPregnancyDetailsEdited();doValidateDateField(this,'past')"/>
					</td>
					<td class="formlabel">Week: </td>
					<td>
						<input type="text" name="ed_pregnancy_week" id="ed_pregnancy_week" onchange="setPregnancyDetailsEdited();" />
					</td>
				</tr>

				<tr>
					<td class="formlabel">Place: </td>
					<td>
						<input type="text" name="ed_pregnancy_place" id="ed_pregnancy_place"  maxlength="100" onchange="setPregnancyDetailsEdited();"/>
					</td>
					<td class="formlabel">Method: </td>
					<td>
						<input type="text" name="ed_pregnancy_method" id="ed_pregnancy_method"  maxlength="100" onchange="setPregnancyDetailsEdited();"/>
					</td>
				</tr>

				<tr>
					<td class="formlabel">Weight: </td>
					<td style="width:200px;" align="left">
						<input type="text" name="ed_pregnancy_weight" id="ed_pregnancy_weight"  onchange="setPregnancyDetailsEdited();"/>&nbsp;Kg
					</td>
					<td class="formlabel">Sex: </td>
					<td>
							<select class="dropdown" id="ed_pregnancy_sex" onchange="setPregnancyDetailsEdited();">
								<option value="">-- Select --</option>
								<option value="M">Male</option>
								<option value="F">Female</option>
								<option value="O">Unknown</option>
							</select>
						</td>
				</tr>

				<tr>
						<td class="formlabel">Complications: </td>
						<td colspan="3">
							<textarea name="ed_pregnancy_complication" id="ed_pregnancy_complication" rows="2" cols="50" onchange="setPregnancyDetailsEdited();"></textarea>
						</td>
				</tr>
				<tr>
					<td class="formlabel">Feeding: </td>
					<td>
						<input type="text" name="ed_pregnancy_feeding" id="ed_pregnancy_feeding" onchange="setPregnancyDetailsEdited();"/>
					</td>
				</tr>

				<tr>
					<td class="formlabel">Out Come: </td>
					<td>
						<input type="text" name="ed_pregnancy_outcome" id="ed_pregnancy_outcome" onchange="setPregnancyDetailsEdited();"/>
					</td>
				</tr>
		</table>
		<table style="margin-top: 10">
				<tr>
					<td>
						<input type="button" id="edit_PregnancyDetails_Ok" name="editok" value="Ok">
						<input type="button" id="edit_PregnancyDetails_Cancel" name="cancel" value="Cancel" />
						<input type="button" id="edit_PregnancyDetails_Previous" name="previous" value="<<Previous" />
						<input type="button" id="edit_PregnancyDetails_Next" name="next" value="Next>>"/>
					</td>
				</tr>
		</table>
	</fieldset>
</div>
</div>
