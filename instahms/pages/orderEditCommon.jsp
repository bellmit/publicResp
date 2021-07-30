<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>

<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>
<c:set var="ok">
<insta:ltext key="common.ordereditcommon.ok"/>
</c:set>
<c:set var="cancel">
<insta:ltext key="common.ordereditcommon.cancel"/>
</c:set>

<form name="editForm">
<div id="editDialog" style="visibility: none; display:none">
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="common.ordereditcommon.title"/></legend>
			<table class="formtable" width="100%">
				<tr>
					<td class="formlabel"><insta:ltext key="common.ordereditcommon.prescribedby"/></td>
					<td class="yui-skin-sam">
						<div>
							<input type="text" name="ePrescribedBy" id="ePrescribedBy" style="width: 11.5em"  />
							<div id="ePrescribedByContainer"></div>
						</div>
						<input type="hidden" name="ePresDocId"/>
					</td>
					<td class="formlabel"><insta:ltext key="common.ordereditcommon.remarks"/></td>
					<td><input type="text" name="eRemarks" id="eRemarks"/></td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="common.ordereditcommon.finalized"/></td>
					<td><input type="checkbox" name="eFinalized"/></td>
					<td class="formlabel"><insta:ltext key="common.ordereditcommon.urgent"/></td>
					<td><input type="checkbox" name="eurgent"/></td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="common.ordereditcommon.start"/></td>
					<td>
						<insta:datewidget name="eFromDate" btnPos="left"/>
						<input type="text" name="eFromTime" id="eFromTime" class="timefield" maxlength="5" />
					</td>

					<td class="formlabel"><insta:ltext key="common.ordereditcommon.end"/></td>
					<td>
						<insta:datewidget name="eToDate" btnPos="left"/>
						<input type="text" name="eToTime" id="eToTime" class="timefield" maxlength="5"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel">
						<insta:ltext key="common.ordereditcommon.toothnumber"/>
					</td>
					<td colspan="2">
						<input type="hidden" name="ed_tooth_number" id="ed_tooth_number" value=""/>
						<input type="hidden" name="ed_tooth_num_required" id="ed_tooth_num_required" value=""/>
						<div id="edToothNumberDiv" style="width: 160px; float: left"></div>
						<div class="multiInfoEditBtn" style="float: left" id="edToothNumBtnDiv">
							<a href="javascript:void(0);" onclick="addOrderDialog.showToothNumberDialog('edit', this);"
								title="Select Tooth Numbers">
								<img src="${cpath}/icons/Edit.png" class="button"/>
							</a>
						</div>
						<div id="edToothNumDsblBtnDiv" style="float: left; display: none">
							<img src="${cpath}/icons/Edit1.png" class="button"/>
						</div>
					</td>
				</tr>
				<tr id="ePriAuthRowId">
					<td class="formlabel">
						<div id="ePriPreAuthLbl" style="display:none"><insta:ltext key="common.ordereditcommon.pri.prior.authid"/></div>
						<div id="ePreAuthLbl" style="display:none"><insta:ltext key="common.ordereditcommon.prior.authid"/></div>
					</td>
					<td>
						<input type="text" name="ePriorAuthId" id="ePriorAuthId" />
					</td>
					<td class="formlabel">
						<div id="ePriPreAuthModeLbl" style="display:none"><insta:ltext key="common.ordereditcommon.pri.prior.authmode"/></div>
						<div id="ePreAuthModeLbl" style="display:none"><insta:ltext key="common.ordereditcommon.prior.authmode"/></div>
					</td>
					<td>
						<insta:selectdb name="ePriorAuthMode" table="prior_auth_modes" value="" valuecol="prior_auth_mode_id"
						displaycol="prior_auth_mode_name" filtered="false" dummyvalue="-- Select --"/>
					</td>
				</tr>
				<tr id="eSecPriAuthRowId">
					<td class="formlabel"><insta:ltext key="common.ordereditcommon.sec.prior.authid"/></td>
					<td>
						<input type="text" name="eSecPriorAuthId" id="eSecPriorAuthId" />
					</td>
					<td class="formlabel"><insta:ltext key="common.ordereditcommon.sec.prior.authmode"/></td>
					<td>
						<insta:selectdb name="eSecPriorAuthMode" table="prior_auth_modes" value="" valuecol="prior_auth_mode_id"
						displaycol="prior_auth_mode_name" filtered="false" dummyvalue="-- Select --"/>
					</td>
				</tr>
				<tr id="eConductingDoc" style="display: none;">
					<td class="formlabel"><insta:ltext key="common.ordereditcommon.conductingdoc"/></td>
					<td class="yui-skin-sam" valign="top" >
						<div style="width: 138px">
							<input type="text" id="eConducting_doctor" name="eConducting_doctor" disabled/>
							<div id="eConducting_doctorAcDropdown" style="width: 250px"></div>
						</div>
						<input type="hidden" name="eConducting_doctorId"/>
					</td>
				</tr>
			<table class="detailList" id="condDoctorsTableED" style="display: none; margin-top: 10px;">
				<tr>
					<th><insta:ltext key="common.ordereditcommon.activitytype"/></th>
					<th><insta:ltext key="common.ordereditcommon.name"/></th>
					<th><insta:ltext key="common.ordereditcommon.conddoc"/></th>
				</tr>
			</table>
		</fieldset>

		<table>
			<tr>
				<td><input type="button" value="${ok}" onclick="return saveEdit()"/></td>
				<td><input type="button" value="${cancel}" onclick="cancelEdit()"/></td>
			</tr>
		</table>

	</div>
</div>
</form>
