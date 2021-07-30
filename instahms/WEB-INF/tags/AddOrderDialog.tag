<%@tag body-content="empty" pageEncoding="UTF-8"%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<%@attribute name="visitType" required="true" %>
<%@attribute name="includeOtDocCharges" required="false" %>  <%-- billing will pass true --%>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<%
if (includeOtDocCharges == null) includeOtDocCharges = "N";
java.util.List docCharges = null;
java.util.List otDocCharges = null;
String orgId = "ORG0001";

Integer centerId = com.bob.hms.common.RequestContext.getCenterId();
String healthAuthorityForCenter = com.insta.hms.master.CenterMaster.CenterMasterDAO.getHealthAuthorityForCenter(centerId);
String healthAuthority = com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(healthAuthorityForCenter).getHealth_authority();

java.util.Map patient = null;
String moduleId = request.getParameter("moduleId");
if (moduleId == null || (moduleId != null && !moduleId.equals("mod_insurance"))) {
	patient = (java.util.Map)request.getAttribute("patient");
}

if(patient == null)//possible for Quickestimate screen
	orgId = (String)request.getAttribute("rate_plan") == null?
			(String)request.getParameter("rate_plan"):(String)request.getAttribute("rate_plan");
if(patient != null && request.getAttribute("rate_plan") == null)
	orgId = (String)patient.get("org_id");

orgId = orgId == null?"ORG0001":orgId;

String operationApplicability =  com.insta.hms.master.GenericPreferences.GenericPreferencesDAO.getGenericPreferences().getOperationApplicableFor();
if (visitType.equals("o")) {
	if(( operationApplicability.equals("b") || operationApplicability.equals("o")) && includeOtDocCharges.equals("Y"))
		docCharges = com.insta.hms.orders.ConsultationTypesDAO.
					getConsultationTypes("o", "ot",orgId,healthAuthority);
	else
		docCharges = com.insta.hms.orders.ConsultationTypesDAO.getConsultationTypes("o",orgId,healthAuthority);
	otDocCharges = com.insta.hms.master.Accounting.ChargeHeadsDAO.getOtDoctorChargeHeads();
} else if (includeOtDocCharges.equals("Y")) {
	/* combined list of ip and ot types for billing */
	if( operationApplicability.equals("b") || operationApplicability.equals("i"))
		docCharges = com.insta.hms.orders.ConsultationTypesDAO.getConsultationTypes("i", "ot",orgId,healthAuthority);
	else
		docCharges = com.insta.hms.orders.ConsultationTypesDAO.getConsultationTypes("i",orgId,healthAuthority);
} else if(visitType.equals("i")){
	/* only ip types from consultation types */
	docCharges = com.insta.hms.orders.ConsultationTypesDAO.getConsultationTypes("i",orgId,healthAuthority);
	/* OT DOC types from charge heads */
	otDocCharges = com.insta.hms.master.Accounting.ChargeHeadsDAO.getOtDoctorChargeHeads();
}else{
	docCharges = com.insta.hms.orders.ConsultationTypesDAO.getConsultationTypes("i", "o",orgId,healthAuthority);
}
request.setAttribute("docCharges", docCharges);
request.setAttribute("otDocCharges", otDocCharges);
%>
<style type="text/css">
	.yui-ac {padding-bottom: 2em;}
	.scrollingDropDown .yui-ac-content{
		max-height:110px; overflow-y:auto; /* scrolling */
		_height:110px;	/* ie6 */
	}
</style>
<insta:js-bundle prefix="common.order"/>

<form name="orderDialogForm">
	<div id="orderDialogAddDialog" style="visibility: hidden; display:none">
		<div class="bd">
			<div id="orderDialogFormFields">
				<fieldset class="fieldSetBorder" id="addItemFieldSet">
					<legend class="fieldSetLabel"><insta:ltext key="common.addorder.header"/></legend>
					<table id="addorderstable" class="formtable">
						<tr>
							<td class="formlabel"><insta:ltext key="common.addorder.prescribing.doctor"/></td>
							<td class="yui-skin-sam" valign="top" >
								<div style="width: 138px">
									<input type="text" id="prescribing_doctor" name="prescribing_doctor"/>
									<div id="prescribing_doctorAcDropdown" style="width: 250px"></div>
								</div>
								<input type="hidden" name="prescribing_doctorId"/>
							</td>
							<td class="formlabel"><insta:ltext key="common.addorder.order.date"/>:</td>
							<td>
								<insta:datewidget name="presdate" btnPos="topleft" value="today" valid="past"/>
								<input type="text" name="prestime" class="timefield" maxlength="5" value="${curTimeStr}"/>
							</td>
						</tr>

						<tr>
							<td class="formlabel" ><insta:ltext key="common.addorder.service.group"/>:</td>
							<td>
								<select name="service_group_id" id="service_group_id" class="dropdown"
									onchange="return filterServiceSubGroup();">
									<option value=""><insta:ltext key="common.selectbox.defaultText.all"/></option>
									<c:forEach items="${serviceGroups}" var="group">
										<option value="${group.map.service_group_id}">${group.map.service_group_name}</option>
									</c:forEach>
								</select>
							</td>

							<td class="formlabel" class="dropdown"><insta:ltext key="common.addorder.service.sub.group"/>:</td>
							<td>
								<select name="service_sub_group_id" id="service_sub_group_id"
									class="dropdown" onchange="setAutoCompleteFilterValue()"/>
									<option value=""><insta:ltext key="common.selectbox.defaultText.all"/></option>
								</select>
							</td>
						</tr>

						<tr>
							<td class="formlabel"><insta:ltext key="common.addorder.item"/>:</td>
							<td colspan="3" class="yui-skin-sam" valign="top">
								<div style="display: block; float: left; width: 440px" id="item">
									<input type="text" id="orderDialogItems" name="orderDialogItems" />
									<div id="orderDialogItemsAcDropdown" class="scrollingDropDown" style="width: 500px;"></div>
								</div>
								<div style="display: none; float: left; width: 440px" id="oPItem">
									<input type="text" id="orderDialogOpItems" name="orderDialogOpItems" />
									<div id="orderDialogOpItemsAcDropdown" class="scrollingDropDown" style="width: 500px;"></div>
								</div>
								<c:set var="itemImgHelpText">
									<insta:ltext key="common.addorder.item.img.help.text"/>
								</c:set>
								<div style="float: left; margin-left: 5px">
									<img class="imgHelpText" src="${cpath}/images/help.png"
											title="${itemImgHelpText}"/>
								</div>
								<input type="hidden" name="amount" id="amount" readonly="readonly" size="5" value="0"/>
								<td class="forminfo" id="pkg_details_button" style="display: none;">
									<input type="hidden" name="pkgid" id="pkgid" value=""/>
											<input type="button"  name="btnValuePkg" id="btnValuePkg"  title="Additional Package Details.."
												onclick="getPackageDetails(this);" accesskey="O" class="button"  value="..."> </button>
								</td>
							</td>
						</tr>

						<tr>
							<td class="formlabel" style="height: 0px; padding: 0px"></td>
							<td colspan="2" style="height: 15px; padding-top: 0px" valign="top">
								<label id="orderDialogItemType">&nbsp;</label>
							</td>
							<td style="height: 15px; padding-top: 0px" valign="top">
								<label id="itemPrice" ></label>
							</td>
						</tr>

						<tr>
							<td class="formlabel" style="height: 0px; padding: 0px">
								<insta:ltext key="common.addorder.tooth.number"/>:
							</td>
							<td colspan="2" style="height: 15px; padding-top: 0px" valign="top">
								<input type="hidden" name="d_tooth_number" id="d_tooth_number" value=""/>

								<div id="dToothNumberDiv" style="width: 160px; float: left;"></div>
								<div style="float: left;margin-left: 10px; display: none" id="dToothNumBtnDiv">
									<a href="javascript:void(0);" onclick="addOrderDialog.showToothNumberDialog('add', this);"
										title="Select Tooth Numbers">
										<img src="${cpath}/icons/Edit.png" class="button"/>
									</a>
								</div>
								<div style="float: left;margin-left: 10px;" id="dToothNumDsblBtnDiv" >
									<img src="${cpath}/icons/Edit1.png" class="button"/>
								</div>
							</td>
							<td style="height: 15px; padding-top: 0px" valign="top">
								<label id="itemPrvInfo" class="dark bold"></label>
							</td>
						</tr>

						<tr>
							<%-- used for Consultation type / Charge Type / Meal Timing --%>
							<td class="formlabel"><label id="addInfoLabel"><insta:ltext key="common.addorder.charge.type"/>:</label></td>
							<td>
								<div id="addInfoDoc" style="display: none">
									<select class="dropdown" name="doctorCharge" >
										<option value=""><insta:ltext key="common.selectbox.defaultText"/></option>
										<c:forEach var="ch" items="${docCharges}">
											<option value="${ch.map.consultation_type_id}">${ch.map.consultation_type}</option>
										</c:forEach>
									</select>
								</div>
								<c:set var="surgeonAnaesthetistType">
									<insta:ltext key="common.addorder.surgeon.anaesthetist.Type"/>
								</c:set>
								<div id="addInfoOtDoc" style="display: none">	<%-- for adding doctors under operations --%>
									<select class="dropdown" name="otDoctorCharge" title="${surgeonAnaesthetistType}">
										<option value=""><insta:ltext key="common.selectbox.defaultText"/></option>
										<c:forEach var="ch" items="${otDocCharges}">
											<option value="${ch.map.chargehead_id}">${ch.map.chargehead_name}</option>
										</c:forEach>
									</select>
								</div>
								<c:set var="mealTiming">
									<insta:ltext key="common.addorder.meal.timing"/>
								</c:set>
								<div id="addInfoMeal" style="display: none">
									<select name="mealTiming" class="dropdown" title="${mealTiming}">
										<option value="" selected><insta:ltext key="common.selectbox.defaultText"/></option>
										<option value="BF"><insta:ltext key="common.addorder.meal.timing.option.bf"/></option>
										<option value="Lunch"><insta:ltext key="common.addorder.meal.timing.option.lunch"/></option>
										<option value="Dinner"><insta:ltext key="common.addorder.meal.timing.option.dinner"/></option>
										<option value="Spl"><insta:ltext key="common.addorder.meal.timing.option.spl"/></option>
									</select>
								</div>
								<c:set var="chargeType">
									<insta:ltext key="common.addorder.charge.type"/>
								</c:set>
								<div id="addInfoUnits">
									<select name="units" class="dropdown" title="${ifn:cleanHtmlAttribute(chargeType)}" disabled>
										<option value="" selected><insta:ltext key="common.selectbox.defaultText"/></option>
										<option value="D"><insta:ltext key="common.addorder.charge.type.option.daily"/></option>
										<option value="H"><insta:ltext key="common.addorder.charge.type.option.hourly"/></option>
									</select>
								</div>
							</td>
							<%-- Quantity / Finalized --%>
							<td class="formlabel"><label id="addInfo2Label"><insta:ltext key="common.addorder.label.quantity"/>:</label></td>
							<td>
								<div id="addInfoQty">
									<input type="text" name="quantity" title="Quantity" disabled/>
								</div>
								<div id="addInfoFinalized" style="display: none">
									<input type="checkbox" name="finalized" value="Y"/>
								</div>
							</td>
						</tr>

						<tr>
							<td class="formlabel"><insta:ltext key="common.addorder.start"/>:</td>
							<td>
								<insta:datewidget name="fromDate" value="today" btnPos="topleft"/>
								<input type="text" name="fromTime" id="fromTime" class="timefield" maxlength="5"
									value="${curTimeStr}"/>
							</td>

							<td class="formlabel"><insta:ltext key="common.addorder.end"/>:</td>
							<td>
								<insta:datewidget name="toDate" value="today" btnPos="topleft"/>
								<input type="text" name="toTime" id="toTime" class="timefield" maxlength="5"/>
							</td>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="common.addorder.remarks"/>:</td>
							<td>
								<input type="text" name="remarks" maxlength="2000"/>
							</td>
							<td class="formlabel">
								<insta:ltext key="common.addorder.urgent"/>:
							</td>
							<td>
								<input type="checkbox" name="urgent" id="urgent" disabled/>
							</td>
						</tr>
						<tr>
							<td class="formlabel" >
								<div id="preAuthLabel">
									<div id="preAuthNoLbl" style="display:none"><insta:ltext key="common.addorder.prior.auth.no"/>:</div>
									<div id="priPreAuthNoLbl" style="display:none"><insta:ltext key="common.addorder.pri.prior.auth.no"/>:</div>
								</div>
							</td>
							<td>
								<div  id="preAuthField">
									<input type="text" name="prior_auth_id"/>
								</div>
							</td>
							<td class="formlabel">
								<div id="preAuthModeLabel">
									<div id="priPreAuthModeLbl" style="display:none"><insta:ltext key="common.addorder.pri.prior.auth.mode"/>:</div>
									<div id="preAuthModeLbl" style="display:none"><insta:ltext key="common.addorder.prior.auth.mode"/>:
								</div>
							</td>
							<td>
								<div  id="preAuthModeField">
								 	<insta:selectdb  name="prior_auth_mode_id" value="" table="prior_auth_modes" valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name" filtered="false" dummyvalue="-- Select --"/>
								</div>
							</td>
						</tr>
						<tr>
							<td class="formlabel">
								<div id="secPreAuthLabel"><insta:ltext key="common.addorder.sec.prior.auth.no"/>:</div>
							</td>
							<td>
								<div id="secPreAuthField">
									<input type="text" name="sec_prior_auth_id"/>
								</div>
							</td>
							<td class="formlabel">
								<div id="secPreAuthModeLabel">
									<insta:ltext key="common.addorder.sec.prior.auth.mode"/>:
								</div>
							</td>
							<td>
								<div id="secPreAuthModeField">
									<insta:selectdb  name="sec_prior_auth_mode_id" value="" table="prior_auth_modes"
									valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name" filtered="false" dummyvalue="-- Select --"/>
								</div>
							</td>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="common.addorder.conducting.doctor"/>:</td>
							<td class="yui-skin-sam" valign="top" >
								<div style="width: 138px">
									<input type="text" id="conducting_doctor" name="conducting_doctor" disabled/>
									<div id="conducting_doctorAcDropdown" style="width: 250px"></div>
								</div>
								<input type="hidden" name="conducting_doctorId"/>
							</td>
						</tr>
					</table>
				</fieldset>

				<fieldset class="fieldSetBorder" id="addOperationFieldSet" style="display: none">
					<legend class="fieldSetLabel"><insta:ltext key="common.addorder.operation.details"/></legend>
					<table class="formtable">
						<tr>
							<td class="formlabel"><insta:ltext key="common.addorder.primary"/>&nbsp;<insta:ltext key="common.addorder.surgeon"/>:</td>
							<td class="yui-skin-sam" valign="top" >
								<div>
									<input type="text" id="surgeon" name="surgeon"/>
									<div id="surgeonAcDropdown" style="width: 200px"></div>
								</div>
								<input type="hidden" name="surgeonId"/>
							</td>
							<td colspan="2">&nbsp;</td>
							<td class="formlabel"><insta:ltext key="common.addorder.theatre"/>:</td>
							<td>
								<select name="theatre" id="theatre" class="dropdown">
									<option value="">-- Select --</option>
									<c:forEach items="${otlist_applicabletovisitcenter}" var="ot">
										<option value="${ot.map.theatre_id }">${ot.map.theatre_name }</option>
									</c:forEach>
								</select>
							</td>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="common.addorder.primary"/>&nbsp;<insta:ltext key="common.addorder.anaesthetist"/>:</td>
							<td class="yui-skin-sam" valign="top" >
								<div>
									<input type="text" id="anaesthetist" name="anaesthetist"/>
									<div id="anaesthetistAcDropdown" style="width: 200px"></div>
								</div>
								<input type="hidden" name="anaesthetistId"/>
							</td>
						</tr>
					</table>
				</fieldset>

				<fieldset id="addOpAnaesDetailsFieldSet" style="display: none">
					<legend class="fieldSetLabel"><insta:ltext key="common.addorder.label.anesthesia.details"/></legend>
					<table width="100%" id="anaestiatistTypeTable">
						<tr style="height:5px;">
							<td class="formlabel">&nbsp;</td>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="common.addorder.anesthesia.type"/>:</td>
							<td>
								<select class="dropdown" name="anesthesia_type" id="anesthesia_type0">
									<option value=""><insta:ltext key="common.selectbox.defaultText"/></option>
								</select>
								<input type="hidden" name="anaesthesiaTypeId" id="anaesthesiaTypeId0"/>
							</td>
							<td class="formlabel"><insta:ltext key="common.addorder.anesthesia.type.start.time"/>:</td>
							<td>
								<insta:datewidget name="anes_start_date" id="anes_start_date0"  value="today"/>
								<input type="text" name="anes_start_time" id="anes_start_time0" class="timefield" maxlength="5" value="${curTimeStr}"/>
							</td>
							<td class="formlabel"><insta:ltext key="common.addorder.anesthesia.type.end.time"/>:</td>
							<td>
								<insta:datewidget name="anes_end_date" id="anes_end_date0" value="today"/>
								<input type="text" name="anes_end_time" id="anes_end_time0" class="timefield" maxlength="5" value="${curTimeStr}"/>
							</td>
						</tr>
					</table>
					<div style="height:5px;">&nbsp;</div>
					<table>
						<tr class="footer">
							<td style="width:700px;">&nbsp;</td>
							<td align="left">
								<button id="btnAddItem" class="imgButton" title="Add Diagnosis Code" name="btnAddItem" type="button" onclick="addAnaestiatistTypeRow();">
									<img src="${cpath}/icons/Add.png">
								</button>
							</td>
						</tr>
					</table>
				</fieldset>

				<fieldset class="fieldSetBorder" id="addMultivisitPackageFieldSet" style="display: none">
					<legend class="fieldSetLabel"><insta:ltext key="common.addorder.multivisit.package.details"/></legend>
					<table class="resultList" cellspacing="0" cellpadding="0" id="multiVisitPackageDetailsTable" >
						<tr>
							<th>Select</th>
							<th>Item</th>
							<th>Total Quantity</th>
							<th>Available Quantity</th>
							<th>Order Quantity</th>
							<th>&nbsp;</th>

						</tr>
					</table>
				</fieldset>

				<fieldset class="fieldSetBorder" id="addDocVisitFieldSet" style="display: none">
					<legend class="fieldSetLabel"><insta:ltext key="common.addorder.doctor.visits"/></legend>
					<table class="formtable" id="docVisits">
					</table>
				</fieldset>

				<fieldset class="fieldSetBorder" id="addConductingDocFieldSet" style="display: none">
					<legend class="fieldSetLabel"><insta:ltext key="common.addorder.conducting.doctor"/></legend>
					<table class="detailList" id="conductingDoctors">
						<tr>
							<th>Activity Type</th>
							<th>Name</th>
							<th>Cond. Doctor</th>
						</tr>
					</table>
				</fieldset>

				<%-- Used only by packages to select the doctors within the package items --%>
				<fieldset class="fieldSetBorder" id="addDiscountsFieldSet" style="display: none">
					<legend class="fieldSetLabel"><insta:ltext key="common.addorder.discount.details"/></legend>
					<table class="formtable">
						<tr>
							<td class="formlabel"><insta:ltext key="common.addorder.discount.description"/>:</td>
							<td><input type="text" name="discDescription"></td>
							<td class="formlabel"><insta:ltext key="common.addorder.discount.auth"/>:</td>
							<td class="yui-skin-sam" valign="top" >
								<div>
									<input type="text" id="discauth" name="discauth" style="width: 138px"/>
									<div id="discauthAcDropdown" style="width: 138px"></div>
								</div>
								<input type="hidden" name="discauthId"/>
							</td>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="common.addorder.percentage"/>:</td>
							<td><input type="text" name="discPercent"></td>

							<td class="formlabel"><insta:ltext key="common.addorder.absolute.amount"/>:</td>
							<td><input type="text" name="discAmount"></td>
						</tr>
					</table>
				</fieldset>

				<fieldset class="fieldSetBorder" id="addMisFieldSet" style="display: none">
					<legend class="fieldSetLabel"><insta:ltext key="common.addorder.miscellaneous.charge.details"/></legend>
					<table class="formtable">
						<tr>
							<td class="formlabel"><insta:ltext key="common.addorder.miscellaneous.description"/>:</td>
							<td><input type="text" name="miscDescription" title="Description"></td>
						</tr>

						<tr>
							<td class="formlabel"><insta:ltext key="common.addorder.rate"/>:</td>
							<td><input type="text" name="miscRate" title="Rate"></td>
							<td class="formlabel"><insta:ltext key="common.addorder.misc.quantity"/>:</td>
							<td><input type="text" name="miscQty" title="Quantity"></td>
						</tr>
					</table>
				</fieldset>
				<fieldset class="fieldSetBorder" id="" style="display: none">
					<legend class="fieldSetLabel"><insta:ltext key="common.addorder.test.title.additional_notes"/></legend>
					<table class="formtable">
						<tr>
							<td class="formlabel"><insta:ltext key="common.addorder.test.label.additional_notes"/></td>
							<td>
								<textarea name="test_additional_notes" id="test_additional_notes" rows="3" cols="150">
								</textarea>
							</td>
						</tr>
					</table>
				</fieldset>
			</div>

			<table>
				<tr>
					<td>
						<button type="button" name="orderDialogAdd" id="orderDialogAdd" accesskey="A" disabled="disabled">
							<b><u><insta:ltext key="common.button.char.a"/></u></b><insta:ltext key="common.addorder.button.dd"/>
						</button>
					</td>
					<td>
						<c:set var="prevButton">
							<insta:ltext key="common.addorder.button.prev"/>
						</c:set>
						<input type="button" name="orderDialogPrevious" value="${prevButton}" disabled />
					</td>
					<td>
						<button type="button" name="orderDialogNext" id="orderDialogNext" accesskey="N" disabled>
							<b><u><insta:ltext key="common.button.char.n"/></u></b><insta:ltext key="common.addorder.button.ext"/>&gt;&gt;
					</td>
					<td>
						<c:set var="closeButton">
							<insta:ltext key="common.addorder.button.close"/>
						</c:set>
						<input type="button" name="orderDialogCancel" value="${closeButton}" />
					</td>
				</tr>
			</table>
		</div>
	</div>
	<div id="toothNumDialog" style="display: none">
		<div class="bd">
			<input type="hidden" name="dialog_type" id="dialog_type" value=""/>
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel" id="toothNumberDialogTitle"></legend>
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
						<td>
							<c:set var="tn_ok">
								<insta:ltext key="common.order.toothnum_ok_btn"/>
							</c:set>
							<c:set var="tn_close">
								<insta:ltext key="common.order.toothnum_close_btn"/>
							</c:set>
							<input type="button" name="toothNumDialog_ok" id="toothNumDialog_ok" value="${tn_ok}"/>
						</td>
						<td><input type="button" name="toothNumDialog_close" id="toothNumDialog_close" value="${tn_close}"></td>
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
			</table>
			</fieldset>
		</div>
	</div>
</form>

