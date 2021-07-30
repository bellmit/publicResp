<%@tag body-content="empty" pageEncoding="UTF-8"%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@attribute name="sponsorIndex" required="true" %>
<%@attribute name="visitType" required="false" %>
<%@attribute name="screenid" required="false" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<script>
	function insuViewDoc(sponsorIndex, obj) {
		if (null != sponsorIndex) {
			if ('primary' == sponsorIndex) {
				return insuPrimaryViewDoc(obj);
			} else if ('secondary' == sponsorIndex) {
				return insuSecondaryViewDoc(obj);
			}
		}
	}
</script>

<c:set var="sponsorPrefix" value="${sponsorIndex == 'P' ? 'primary' : 'secondary' }"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="screenid" value="${screenid}"/>

<tr id="${sponsorPrefix}SponsorGroup" style="">
<td>
<fieldset class="fieldSetBorder" style="width: 935px;">
	<legend class="fieldSetLabel"><insta:ltext key="registration.patient.${sponsorPrefix}.sponsor"/></legend>
		<table class="formtable" id="${sponsorPrefix}InsuranceTab" style="">
		<tr>
			<c:if test="${preferences.modulesActivatedMap['mod_basic'] eq 'Y' ||
						preferences.modulesActivatedMap['mod_insurance'] eq 'Y' ||
						preferences.modulesActivatedMap['mod_adv_ins'] eq 'Y'}">
			<td class="formlabel">
				<insta:ltext key="registration.patient.payment.sponser"/>:
			</td>
			<td>
				<div id="${sponsorPrefix}_tpa_wrapper" class="autoComplete">
				<c:choose>
				<c:when test="${(screenId eq 'reg_registration') || (screenId eq 'ip_registration') || (screenId eq 'out_pat_reg')}">
				<input type="text" id="${sponsorPrefix}_sponsor_name" name="${sponsorPrefix}_sponsor_name"
               onchange="enableRegistrationOtherInsuranceDetailsTab('${sponsorIndex}');onTpaChange('${sponsorIndex}');"></input>
        </c:when>
        <c:otherwise>
				<input type="text" id="${sponsorPrefix}_sponsor_name" name="${sponsorPrefix}_sponsor_name"
						onchange="enableOtherInsuranceDetailsTab('${sponsorIndex}');onTpaChange('${sponsorIndex}');"></input>
		</c:otherwise>
        </c:choose>
				<input type="hidden" id="${sponsorPrefix}_sponsor_id" name="${sponsorPrefix}_sponsor_id" value=""></input>
				<div id="${sponsorPrefix}_tpa_dropdown" class="scrolForContainer" style="width:250px"></div></div>
			</td>
			<td class="formlabel">
				<insta:ltext key="registration.patient.payment.insuranceCo"/>:
			</td>
			<td>
			<c:choose>
      	<c:when test="${(screenId eq 'reg_registration') || (screenId eq 'ip_registration') || (screenId eq 'out_pat_reg')}">
				<select id="${sponsorPrefix}_insurance_co" name="${sponsorPrefix}_insurance_co" onchange="onInsuranceCompanyChange('${sponsorIndex}');insuViewDoc('${sponsorPrefix}',this);" class="dropdown">
        		<option selected="selected" value="">
        			  <insta:ltext key="common.selectbox.defaultText"/>
        		</option>
        	</select>
        </c:when>
        <c:otherwise>
        <select id="${sponsorPrefix}_insurance_co" name="${sponsorPrefix}_insurance_co" onchange="onLoadTpaList('${sponsorIndex}');insuViewDoc('${sponsorPrefix}',this);" class="dropdown">
                		<option selected="selected" value="">
                			  <insta:ltext key="common.selectbox.defaultText"/>
                		</option>
              </select>
				 	<!--<insta:selectdb name="${sponsorPrefix}_insurance_co" id="${sponsorPrefix}_insurance_co" onchange="onLoadTpaList('${sponsorIndex}');insuViewDoc('${sponsorPrefix}',this);"
         				 value="" table="insurance_company_master" style="width:137px;" dummyvalue="-- Select --" valuecol="insurance_co_id" displaycol="insurance_co_name" orderby="insurance_co_name" /> -->
         </c:otherwise>
         </c:choose>
         <div id="viewinsurance${sponsorPrefix}ruledocs"></div>
			</td>
			<td></td>
			<td></td>
			</c:if>
		</tr>
		</table>
		<%--
		<c:if test="${preferences.modulesActivatedMap['mod_adv_ins'] eq 'Y'}">
		--%>
		<table class="formtable" id="${sponsorPrefix}InsuranceOtherDetailsTab" style="display:none">
		<tr>
			<td class="formlabel" id="${sponsorPrefix}networkPlanType">
				<%-- Ins30: Corporate Insurance --%>
				<!--  style="${corpInsurancehid}">  -->
				<insta:ltext key="registration.patient.payment.networkPlanType"/>:
			</td>
			<td>
			<%-- Ins30: Corporate Insurance --%>
			<!-- style="${corpInsurancehid}"> -->
				<select id="${sponsorPrefix}_plan_type" name="${sponsorPrefix}_plan_type" onchange="onInsuCatChange('${sponsorIndex}')"
					class="dropdown">
					<option selected="selected" value="">
						<insta:ltext key="common.selectbox.defaultText"/>
					</option>
				</select>
			</td>
			<td class="formlabel">
				<insta:ltext key="registration.patient.payment.planName"/>:
			</td>
			<td id="${sponsorPrefix}PlanTD">
				<select id="${sponsorPrefix}_plan_id" name="${sponsorPrefix}_plan_id" onchange="onPolicyChange('${sponsorIndex}');"
					class="dropdown">
					<option selected="selected" value="">
					<insta:ltext key="common.selectbox.defaultText"/>-</option>
				</select>
				<input type="hidden" id="${sponsorPrefix}_patient_insurance_plans_id" name="${sponsorPrefix}_patient_insurance_plans_id" value=""/>
			</td>
			<c:if test="${(screenId eq 'reg_registration') || (screenId eq 'ip_registration') || (screenId eq 'out_pat_reg')}">
				<td class="formlabel"><insta:ltext key="registration.patient.payment.plan.details"/>:</td>
				<td class="forminfo">
					<div title="" id="${sponsorPrefix}_plan_div">
						<button id="pd_${sponsorPrefix}_planButton" title="<insta:ltext key='registration.patient.additional.plan.info.dot.dot.dot'/>" style="cursor:pointer;"
							onclick="javascript:initPatientRegPlanDetailsDialog('pd_${sponsorPrefix}_planButton');showPatientRegPlanDetailsDialog('${sponsorPrefix}');" type="button"><insta:ltext key="registration.patient.button.dot.dot"/></button>
					</div>
				</td>
			</c:if>
		</tr>
		<tr id="${sponsorPrefix}_planLimitRow" style="visibility:hidden">
			<input type="hidden" name="${sponsorPrefix}_limits_include_followUps" value="" id="${sponsorPrefix}_limits_include_followUps">
			<td class="formlabel">
				<insta:ltext key ="registration.patient.payment.planLimit"/>:
			</td>
			<td>
				<input type="text" name="${sponsorPrefix}_plan_limit" id="${sponsorPrefix}_plan_limit"size="5" value="" disabled="disabled"  maxlength="13"
					onkeypress="return enterNumOnly(event)" onchange="onPlanLimitChange(this,'${sponsorIndex}')";/>
			</td>
			<td class="formlabel">
				<insta:ltext key ="registration.patient.payment.planUtilization"/>:
			</td>
			<td>
				<input type="text" name="${sponsorPrefix}_plan_utilization" id="${sponsorPrefix}_plan_utilization"size="5" value="" disabled="disabled"  maxlength="13"
				onkeypress="return enterNumOnly(event)" onchange="onChangeutilizationLimit(this,'${sponsorIndex}')"/>
			</td>
			<td class="formlabel">
				<insta:ltext key ="registration.patient.payment.availableLimit"/>:
			</td>
			<td class="forminfo">
				<label id="${sponsorPrefix}_available_limit"> </label>
				<!--<label name="${sponsorPrefix}_available_limit" id="${sponsorPrefix}_available_limit" size="5"
				onkeypress="return enterNumOnly(event)"  onchange="onChangeAvaliableLimit(this,'${sponsorIndex}')"/>
			--></td>
		</tr>
		<tr>
			<td class="formlabel">
				<insta:ltext key ="registration.patient.payment.visitLimit"/>:
			</td>
			<td>
				<input type="text" name="${sponsorPrefix}_visit_limit" id="${sponsorPrefix}_visit_limit"size="5" value="" disabled="disabled"  maxlength="13"
					onkeypress="return enterNumOnly(event)" onchange="onVisitLimitChange(this,'${sponsorIndex}')"/>
					<input type="hidden" name="${sponsorPrefix}_visit_sponser_limit_hidden" id="${sponsorPrefix}_visit_sponser_limit_hidden"/>
			</td>
			<td class="formlabel" id="${sponsorPrefix}_visitDeductibleCellLbl" style="visibility:hidden">
				<insta:ltext key ="registration.patient.payment.visitDeductible"/>:
			</td>
			<td id="${sponsorPrefix}_visitDeductibleCellInfo" style="visibility:hidden">
				<input type="text" name="${sponsorPrefix}_visit_deductible" id="${sponsorPrefix}_visit_deductible"size="5" value="" disabled="disabled"  maxlength="13"
				onkeypress="return enterNumOnly(event)" onchange="onVisitdeductibleChange(this,'${sponsorIndex}')"/>
			</td>
			<td class="formlabel" id="${sponsorPrefix}_visitCopayCellLbl" style="visibility:hidden">
				<insta:ltext key ="registration.patient.payment.visitCopay"/>:
			</td>
			<td id="${sponsorPrefix}_visitCopayCellInfo" style="visibility:hidden">
				<input type="text" name="${sponsorPrefix}_visit_copay" id="${sponsorPrefix}_visit_copay"size="5" value="" disabled="disabled"  maxlength="13"
				onkeypress="return enterNumOnly(event)" onchange="onVisitCopayPercChange(this, '${sponsorIndex}')"/>
			</td>
		</tr>
		<tr id="${sponsorPrefix}_maxCopayRow" style="visibility:hidden">
			<td class="formlabel">
				<insta:ltext key ="registration.patient.payment.maxCopay"/>:
			</td>
			<td>
				<input type="text" name="${sponsorPrefix}_max_copay" id="${sponsorPrefix}_max_copay"size="5" value="" disabled="disabled"  maxlength="13"
					onkeypress="return enterNumOnly(event)" onchange="onMaxCopayChange(this,'${sponsorIndex}')"/>
			</td>
			<c:if test="${(screenId eq 'ip_registration' || (screenid eq 'Edit_Insurance' && visitType eq 'i'))}">
				<td class="formlabel">
					<insta:ltext key ="registration.patient.payment.perDayLimit"/>:
				</td>
				<td>
					<input type="text" name="${sponsorPrefix}_perday_limit" id="${sponsorPrefix}_perday_limit"size="5" value="" disabled="disabled"  maxlength="13"
					onkeypress="return enterNumOnly(event)" onchange="onPerDayLimitChange(this,'${sponsorIndex}')"/>
				</td>
			</c:if>
			<c:set var="moreBtnStyle" value="${preferences.modulesActivatedMap['mod_adv_ins'] eq 'Y' ? 'visible' : 'hidden'}"/>
			<td class="formlabel" id="${sponsorPrefix}_moreButtonLbl" "visibility:${moreBtnStyle};"><insta:ltext key="registration.patient.payment.plan.More"/>:</td>
			<td class="forminfo" style="visibility:${moreBtnStyle};">
				<div title="" id="${sponsorPrefix}_insurance_div">
					<button type="button" id="pd_${sponsorPrefix}_insuranceButton" name="pd_${sponsorPrefix}_insuranceButton"
						title="<insta:ltext key='registration.patient.additional.insurance.plan.info.dot.dot.dot'/>" style="cursor:pointer;"
						onclick="showInsurancePlanDetailsDialog('${sponsorPrefix}');"
						accesskey="I" class="button" value=".." disabled="disabled">
						<insta:ltext key="registration.patient.button.dot.dot"/> </button>
				</div>
			</td>
		</tr>
		</table>
		<table class="formtable" id="${sponsorPrefix}MememberShipValidityTab" style="display:none">
		<tr>
			<td class="formlabel" id="${sponsorPrefix}_member_id_label">${ifn:cleanHtml(regPref.member_id_label)}:</td>
			<td >
				<div id="${sponsorPrefix}MemberIdAutoComplete" class="autoComplete" style="padding-bottom: 20px;">
					<input type="text" name="${sponsorPrefix}_member_id" id="${sponsorPrefix}_member_id" onblur="checkForMemberID('${sponsorIndex}')"
						class="field" maxlength="39"/>
					<input type="hidden" name="${sponsorPrefix}_member_id_hidden" id="${sponsorPrefix}_member_id_hidden"/>
					<div id="${sponsorPrefix}MemberIdContainer" style="width:240px;"></div>
				</div>
				<span class="star" id="${sponsorPrefix}_member_id_star" style="visibility:hidden">*</span>
				<img class="imgHelpText"
title="${regPref.member_id_label} Pattern Validation Rules. The pattern will be in the combination of x,9,’ ', ',' , '-', '.'. Here x is for any alphabetical character and 9 is for any numeric digit. Other than x and 9 represents constant values."
						 		src="${cpath}/images/help.png"   style="float:right; margin-top:-1px"/>
			</td>
			<td class="formlabel" id="${sponsorPrefix}_validity_start_period_label">${ifn:cleanHtml(regPref.member_id_valid_from_label)}:</td>
			<td id="${sponsorPrefix}_validity_start_period_tab">
				<input type="hidden" name="${sponsorPrefix}_policy_validity_hidden" id="${sponsorPrefix}_policy_validity_hidden"/>
				<insta:datewidget name="${sponsorPrefix}_policy_validity_start" value="" id="${sponsorPrefix}_policy_validity_start"
					btnPos="left" title="Policy validity start date"/>
				<span class="star" id="${sponsorPrefix}_policy_validity_start_star" style="visibility:hidden">*</span>
				<%--
				<c:choose>
				<c:when test="${corpInsurance eq 'Y'}">
					<b><label name="${sponsorPrefix}_policy_validity_start" id="${sponsorPrefix}_policy_validity_start"/></b>
					<input type="hidden" name="${sponsorPrefix}_policy_validity_start" id="${sponsorPrefix}_policy_validity_start1" />
 					<span class="star" id="${sponsorPrefix}_policy_validity_start_star" style="visibility:hidden"></span>
				</c:when>
				<c:otherwise>
					<insta:datewidget name="${sponsorPrefix}_policy_validity_start" value="" id="${sponsorPrefix}_policy_validity_start"
						btnPos="left" title="Policy validity start date"/>
					<span class="star" id="${sponsorPrefix}_policy_validity_start_star" style="visibility:hidden">*</span>
				</c:otherwise>
				</c:choose>
				--%>
			</td>
			<td class="formlabel" id="${sponsorPrefix}_validity_end_period_label">${ifn:cleanHtml(regPref.member_id_valid_to_label)}:</td>
			<td id="${sponsorPrefix}_validity_end_period_tab">
			<insta:datewidget name="${sponsorPrefix}_policy_validity_end" value="" id="${sponsorPrefix}_policy_validity_end"
					 btnPos="left" title="Policy validity end date"/>
			<span class="star" id="${sponsorPrefix}_policy_validity_end_star" style="visibility:hidden">*</span>
			<%--
			<c:choose>
				<c:when test="${corpInsurance eq 'Y'}">
					<b><label name="${sponsorPrefix}_policy_validity_end" value="" id="${sponsorPrefix}_policy_validity_end"/></b>
					<input type="hidden" name="${sponsorPrefix}_policy_validity_end" id="${sponsorPrefix}_policy_validity_end1" />
					<span class="star" id="${sponsorPrefix}_policy_validity_end_star" style="visibility:hidden"></span>
				</c:when>
				<c:otherwise>
				<insta:datewidget name="${sponsorPrefix}_policy_validity_end" value="" id="${sponsorPrefix}_policy_validity_end"
					 btnPos="left" title="Policy validity end date"/>
				<span class="star" id="${sponsorPrefix}_policy_validity_end_star" style="visibility:hidden">*</span>
				</c:otherwise>
				</c:choose>
			--%>
			</td>
		</tr>
		</table>
		<table class="formtable" id="${sponsorPrefix}OnlyValidityTab" style="display:none">
		<tr>
			<td class="formlabel" id="${sponsorPrefix}_validity_only_start_period_label">${ifn:cleanHtml(regPref.member_id_valid_from_label)}:</td>
			<td id="${sponsorPrefix}_validity_only_start_period_tab">
				<input type="hidden" name="${sponsorPrefix}_policy_validity_only_hidden" id="${sponsorPrefix}_policy_validity_only_hidden"/>
				<input type="hidden" name="${sponsorPrefix}_policy_validity_only_hidden1" id="${sponsorPrefix}_policy_validity_only_hidden1"/>
				<insta:datewidget name="${sponsorPrefix}_policy_validity_only_start" value="" id="${sponsorPrefix}_policy_validity_only_start"
					btnPos="left" title="Policy validity start date"/>
				<span class="star" id="${sponsorPrefix}_policy_validity_only_start_star" style="visibility:hidden">*</span>
				<%--
				<c:choose>
				<c:when test="${corpInsurance eq 'Y'}">
					<b><label name="${sponsorPrefix}_policy_validity_only_start" id="${sponsorPrefix}_policy_validity_only_start"/></b>
					<input type="hidden" name="${sponsorPrefix}_policy_validity_only_start" id="${sponsorPrefix}_policy_validity_only_start1" />
 					<span class="star" id="${sponsorPrefix}_policy_validity_only_start_star" style="visibility:hidden"></span>
				</c:when>
				<c:otherwise>
					<insta:datewidget name="${sponsorPrefix}_policy_validity_only_start" value="" id="${sponsorPrefix}_policy_validity_only_start"
						btnPos="left" title="Policy validity start date"/>
					<span class="star" id="${sponsorPrefix}_policy_validity_only_start_star" style="visibility:hidden">*</span>
				</c:otherwise>
				</c:choose>
				--%>
			</td>
			<td class="formlabel" id="${sponsorPrefix}_validity_only_end_period_label">${ifn:cleanHtml(regPref.member_id_valid_to_label)}:</td>
			<td id="${sponsorPrefix}_validity_only_end_period_tab">
				<insta:datewidget name="${sponsorPrefix}_policy_validity_only_end" value="" id="${sponsorPrefix}_policy_validity_only_end"
					 btnPos="left" title="Policy validity end date"/>
				<span class="star" id="${sponsorPrefix}_policy_validity_only_end_star" style="visibility:hidden">*</span>
				<%--
				<c:choose>
				<c:when test="${corpInsurance eq 'Y'}">
					<b><label name="${sponsorPrefix}_policy_validity_only_end" value="" id="${sponsorPrefix}_policy_validity_only_end"/></b>
					<input type="hidden" name="${sponsorPrefix}_policy_validity_only_end" id="${sponsorPrefix}_policy_validity_only_end1" />
					<span class="star" id="${sponsorPrefix}_policy_validity_only_end_star" style="visibility:hidden"></span>
				</c:when>
				<c:otherwise>
				<insta:datewidget name="${sponsorPrefix}_policy_validity_only_end" value="" id="${sponsorPrefix}_policy_validity_only_end"
					 btnPos="left" title="Policy validity end date"/>
				<span class="star" id="${sponsorPrefix}_policy_validity_only_end_star" style="visibility:hidden">*</span>
				</c:otherwise>
				</c:choose>
				--%>
			</td>
		</tr>
		</table>
		<table class="formtable" id="${sponsorPrefix}PolicyDetailsTab" style="display:none">
		<tr>
			<td class="formlabel">
				<insta:ltext key="registration.patient.payment.policyNumber"/>:
			</td>
			<td>
				<input type="text" name="${sponsorPrefix}_policy_number" id="${sponsorPrefix}_policy_number" maxlength="50">
				<input type="hidden" name="${sponsorPrefix}_policy_Details_hidden" id="${sponsorPrefix}_policy_Details_hidden"/>
				<span class="star" id="${sponsorPrefix}_policy_number_star" style="visibility:hidden">*</span>
			</td>
			<td class="formlabel">
				<insta:ltext key="registration.patient.payment.policyHolder"/>:
			</td>
			<td>
				<input type="text" name="${sponsorPrefix}_policy_holder_name" id="${sponsorPrefix}_policy_holder_name" maxlength="50">
				<span class="star" id="${sponsorPrefix}_policy_holder_name_star" style="visibility:hidden">*</span>
			</td>
			<td class="formlabel">
				<insta:ltext key="registration.patient.payment.relationShip"/>:
			</td>
			<td><input type="text" name="${sponsorPrefix}_patient_relationship" id="${sponsorPrefix}_patient_relationship">
			<span class="star" id="${sponsorPrefix}_patient_relationship_star" style="visibility:hidden">*</span></td>
		</tr>
		</table>
		<table class="formtable" id="${sponsorPrefix}PriorAuthDetailsTab" style="display:none">
		<tr>
			<td class="formlabel">
				<insta:ltext key="registration.patient.payment.priorAuthNo"/>:
			</td>
			<td>
				<input type="text" name="${sponsorPrefix}_prior_auth_id" id="${sponsorPrefix}_prior_auth_id" maxlength="25">
				<c:if test="${preferences.modulesActivatedMap['mod_adv_ins'] eq 'Y'
							&& not empty regPref.prior_auth_required &&	(regPref.prior_auth_required eq 'A' ||
								(screenId eq 'reg_registration' && regPref.prior_auth_required eq 'O') ||
								(screenId eq 'ip_registration' && regPref.prior_auth_required eq 'I'))}">
				<span class="star">*</span>
				</c:if>
			</td>
			<td class="formlabel">
				<insta:ltext key="registration.patient.payment.priorAuthMode"/>:
			</td>
			<td>
				<insta:selectdb  name="${sponsorPrefix}_prior_auth_mode_id" id="${sponsorPrefix}_prior_auth_mode_id" value=""
					table="prior_auth_modes" valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name"
					filtered="false" dummyvalue="-- Select --"/>
				<c:if test="${preferences.modulesActivatedMap['mod_adv_ins'] eq 'Y'
							&& not empty regPref.prior_auth_required &&	(regPref.prior_auth_required eq 'A' ||
								(screenId eq 'reg_registration' && regPref.prior_auth_required eq 'O') ||
								(screenId eq 'ip_registration' && regPref.prior_auth_required eq 'I'))}">
				<span class="star">*</span>
				</c:if>
			</td>
		  </tr>
		</table>

		<table class="formtable" id="${sponsorPrefix}drgPerdiemTab" style="display:none">
			<tr>
				<c:if test="${regPref.allow_drg_perdiem eq 'Y'}">
					<td class="formlabel"  id="drgCell">
						<label for="use_drg"><insta:ltext key="registration.patient.payment.useDRG"/>:</label>
					<td>
						<input type="checkbox" name="${sponsorPrefix}_drg_check" id="${sponsorPrefix}_drg_check" onclick="checkUseDRG('${sponsorIndex}')" />
						<input type="hidden" name="${sponsorPrefix}_use_drg" id="${sponsorPrefix}_use_drg" value="N">
					</td>
					</td>
					<c:if test="${screenid eq 'Edit_Insurance' }">

						<td class="formlabel" id="perdiemCell">
							<label for="use_perdiem"><insta:ltext key="registration.patient.payment.usePerdiem" />:</label>
						</td>
						<td>
							<input type="checkbox" name="${sponsorPrefix}_perdiem_check" id="${sponsorPrefix}_perdiem_check"
										onclick="checkUsePerdiem('${sponsorIndex}')" />
							<input type="hidden" name="${sponsorPrefix}_use_perdiem" id="${sponsorPrefix}_use_perdiem" value="N">

						</td>

					</c:if>
				</c:if>
			</tr>
		</table>

		<table class="formtable" id="${sponsorPrefix}OthersDetailsTab" style="display:none">
		<tr id="${sponsorPrefix}InsFile">
			<td class="formlabel">
				<insta:ltext key="registration.patient.documentupload.header"/>:
			</td>
			<td colspan="2">
				<input type="hidden" name="${sponsorPrefix}_insurance_doc_type" value="SYS_RG">
				<fmt:formatDate var="cdate" value="${currentDate}" pattern="dd-MM-yyyy"/>
				<input type="hidden" name="${sponsorPrefix}_insurance_doc_date" value="${cdate}">
				<c:if test="${screenid eq 'Edit_Insurance' }">
					<input type="file" name="${sponsorPrefix}_insurance_doc_content_bytea1" id="${sponsorPrefix}_insurance_doc_content_bytea1"  onChange="setDocUpdated(this,'${sponsorIndex}')" accept="<insta:ltext key="upload.accept.image"/>,<insta:ltext key="upload.accept.pdf"/>">
				</c:if>
				<c:if test="${(screenId eq 'reg_registration') || (screenId eq 'ip_registration') || (screenId eq 'out_pat_reg')}">
					<input type="file" name="${sponsorPrefix}_insurance_doc_content_bytea1" id="${sponsorPrefix}_insurance_doc_content_bytea1" accept="<insta:ltext key="upload.accept.image"/>,<insta:ltext key="upload.accept.pdf"/>">
				</c:if>
				<input type="hidden" name="${sponsorPrefix}_insurance_doc_name" id="${sponsorPrefix}_insurance_doc_name1" value="Insurance Card">
				<input type="hidden" name="${sponsorPrefix}_insurance_format" id="${sponsorPrefix}_insurance_format1" value="doc_fileupload">
				<input type="hidden" name="${sponsorPrefix}_insurance_mandatory" id="${sponsorPrefix}_insurance_mandatory1" value="X">

				<input type="hidden" name="${sponsorPrefix}_sponsor_cardfileLocationI" id="${sponsorPrefix}_sponsor_cardfileLocationI" value="" />
				<input type="hidden" name="${sponsorPrefix}_sponsor_cardContentTypeI" id="${sponsorPrefix}_sponsor_cardContentTypeI" value="" />

			</td>
			<c:if test="${(screenId eq 'reg_registration' && regPref.copy_paste_option eq 'Y') ||
					(screenId eq 'ip_registration' && regPref.copy_paste_option eq 'Y')}">
			<td class="formlabel">
				<insta:ltext key="registration.patient.pasteImage.imageLabel"/>:
			</td>
			<td>
			<div style="border:1px dashed grey;height: 45px;width: 65px;font-size: 11px;" id="${sponsorPrefix}_sponsor_pastedPhoto">
								<div style="padding:1px;text-align:center"><insta:ltext key="js.common.copy.and.press.paste"/></div>
							</div>
			</td>
		</c:if>
		<c:if test="${screenid eq 'Edit_Insurance' }">
			<td class="formlabel"></td>
			<td class="formlabel"><insta:ltext key="billing.changetpa.add.editpatientinsurance.documentusage"/>:</td>
			<td class="forminfo" style="width:180px;">
			  <select name="${sponsorPrefix}_insurance_document_usage" id="${sponsorPrefix}_insurance_document_usage" class="dropdown">
				<option value="New"><insta:ltext key="billing.changetpa.add.editpatientinsurance.addnewdocument"/></option>
				<option value="Update"><insta:ltext key="billing.changetpa.add.editpatientinsurance.updatedocument"/></option>
			   </select>
				<span class="star">*</span>
				<img class="imgHelpText" src="${cpath}/images/help.png"
 				title='<insta:ltext key="billing.changetpa.add.editpatientinsurance.membershipdetailstemplate"/>'/>
			</td>
		</c:if>
		<c:if test="${screenid eq 'Edit_Insurance'}" >
			<c:if test="${(sponsorPrefix eq 'primary') && (isPrimaryInsuranceCardAvailable eq true)}">
				<tr>
					<td class="formlabel"><insta:ltext key="billing.changetpa.add.editpatientinsurance.viewcurrentuploadeddocument"/>:</td>
					<td id="_p_plan_card_td" class="forminfo">
					<button id="_p_plan_card" title='<insta:ltext key="billing.changetpa.add.editpatientinsurance.viewuploadeddocument"/>'
						style="cursor:pointer;"
						onclick="javascript:showPrimaryInsurancePhotoDialog();"
						type="button">..</button>
					</td>
				</tr>
			</c:if>
			<c:if test="${(sponsorPrefix eq 'secondary') && (isSecondaryInsuranceCardAvailable eq true)}">
				<tr>
					<td class="formlabel"><insta:ltext key="billing.changetpa.add.editpatientinsurance.viewcurrentuploadeddocument"/>:</td>
					<td id="_s_plan_card_td" class="forminfo">
					<button id="_s_plan_card" title='<insta:ltext key="billing.changetpa.add.editpatientinsurance.viewuploadeddocument"/>'
						style="cursor:pointer;"
						onclick="javascript:showSecondaryInsurancePhotoDialog();"
						type="button">..</button>
					</td>
				</tr>
			</c:if>
		</c:if>
	</tr>
	</table>
	<%-- One empty row : javascript uses drgPerdiumDetailsTab in a few places, but is not required.
	Not to disturb all the code that uses it, we leave it here as an empty row --%>
	<table class="formtable" id="${sponsorPrefix}drgPerdiumDetailsTab" style="display:none">
	<tr>
		<td class="formlabel"></td><td></td>
		<td class="formlabel"></td><td></td>
		<td class="formlabel"></td><td></td>
	</tr>
	</table>
	<%--
	</c:if>
	--%>
	<table class="formtable" id="${sponsorPrefix}CorporateTab" style="display:none">
		<tr>
			<td class="formlabel">
				<insta:ltext key="registration.patient.sponsor.corporate.name"/>:
			</td>
			<td>
				<select id="${sponsorPrefix}_corporate" name="${sponsorPrefix}_corporate" class="dropdown" onchange="onCorporateChange('${sponsorIndex}')">
					<option selected="selected" value=""><insta:ltext key="common.selectbox.defaultText"/></option>
				</select>
			</td>
			<td class="formlabel">
				<insta:ltext key="registration.patient.sponsor.limit"/>:
			</td>
			<td>
				<input type="text" name="${sponsorPrefix}_corporate_approval" id="${sponsorPrefix}_corporate_approval"
						size="5" value="" onkeypress="return enterNumOnly(event)"/>
				<span class="star" id="${sponsorPrefix}_corporate_approval_star" style="visibility:hidden">*</span>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="registration.patient.sponsor.employee.id"/>:</td>
			<td>
				<div id="${sponsorPrefix}CorporateIdAutoComplete" class="autoComplete" style="padding-bottom: 20px;">
					<input type="text" name="${sponsorPrefix}_employee_id" id="${sponsorPrefix}_employee_id" onblur="checkForCorporateMemberID('${sponsorIndex}')"
						class="field" maxlength="39"/>
					<div id="${sponsorPrefix}CorporateIdContainer" style="width:240px;"></div>
				</div>
			</td>

			<td class="formlabel"><insta:ltext key="registration.patient.sponsor.employee.name"/>:</td>
			<td><input type="text" name="${sponsorPrefix}_employee_name" id="${sponsorPrefix}_employee_name"></td>

			<td class="formlabel"><insta:ltext key="registration.patient.payment.relationShip"/>:</td>
			<td><input type="text" name="${sponsorPrefix}_employee_relation" id="${sponsorPrefix}_employee_relation"></td>
		</tr>

		<tr id="${sponsorPrefix}CorporateFile">
			<td class="formlabel">
				<insta:ltext key="registration.patient.documentupload.header"/>:
			</td>
			<td colspan="2">
				<input type="hidden" name="${sponsorPrefix}_corporate_doc_type" value="SYS_RG">
				<fmt:formatDate var="cdate" value="${currentDate}" pattern="dd-MM-yyyy"/>
				<input type="hidden" name="${sponsorPrefix}_corporate_doc_date" value="${cdate}">

				<input type="file" name="${sponsorPrefix}_corporate_doc_content_bytea1" id="${sponsorPrefix}_corporate_doc_content_bytea1" accept="<insta:ltext key="upload.accept.image"/>,<insta:ltext key="upload.accept.pdf"/>">
				<input type="hidden" name="${sponsorPrefix}_corporate_doc_name" id="${sponsorPrefix}_corporate_doc_name1" value="Corporate Card">
				<input type="hidden" name="${sponsorPrefix}_corporate_format" id="${sponsorPrefix}_corporate_format1" value="doc_fileupload">
				<input type="hidden" name="${sponsorPrefix}_corporate_mandatory" id="${sponsorPrefix}_corporate_mandatory1" value="X">

				<input type="hidden" name="${sponsorPrefix}_sponsor_cardfileLocationC" id="${sponsorPrefix}_sponsor_cardfileLocationC" value="" />
				<input type="hidden" name="${sponsorPrefix}_sponsor_cardContentTypeC" id="${sponsorPrefix}_sponsor_cardContentTypeC" value="" />

			</td>
			<c:if test="${(screenId eq 'reg_registration' && regPref.copy_paste_option eq 'Y') ||
					(screenId eq 'ip_registration' && regPref.copy_paste_option eq 'Y')}">
				<c:if test="${regPref.copy_paste_option eq 'Y'}">
				<td class="formlabel">
					<insta:ltext key="registration.patient.pasteImage.imageLabel"/>:
				</td>
				<td>
				</td>
				<td>
					<label id="view${sponsorPrefix}SponsorC" ><insta:ltext key='registration.patient.pasteImage.viewImageLabel'/></label>
				</td>
			</c:if>
		</c:if>
		<c:if test="${screenid eq 'Edit_Insurance' }">
			<td class="formlabel"></td>
			<td class="formlabel"><insta:ltext key="billing.changetpa.add.editpatientinsurance.documentusage"/>:</td>
			<td class="forminfo" style="width:180px;">
			  <select name="primary_insurance_document_usage" id="primary_insurance_document_usage" class="dropdown">
				<option value="New"><insta:ltext key="billing.changetpa.add.editpatientinsurance.addnewdocument"/></option>
				<option value="Update"><insta:ltext key="billing.changetpa.add.editpatientinsurance.updatedocument"/></option>
			   </select>
				<span class="star">*</span>
				<img class="imgHelpText" src="${cpath}/images/help.png"
 				title='<insta:ltext key="billing.changetpa.add.editpatientinsurance.membershipdetailstemplate"/>'/>
			</td>
		</c:if>
	</tr>
	</table>

	<table class="formtable" id="${sponsorPrefix}NationalTab" style="display:none">
		<tr>
			<td class="formlabel">
				<insta:ltext key="registration.patient.sponsor.sponsor.name"/>:</td>
			<td>
				<select id="${sponsorPrefix}_national_sponsor" name="${sponsorPrefix}_national_sponsor"  class="dropdown" onchange="onNationalSponsorChange('${sponsorIndex}')">
					<option selected="selected" value=""><insta:ltext key="common.selectbox.defaultText"/></option>
				</select>
			</td>
			<td class="formlabel">
				<insta:ltext key="registration.patient.sponsor.limit"/>:
			</td>
			<td>
				<input type="text" name="${sponsorPrefix}_national_approval" id="${sponsorPrefix}_national_approval"
						size="5" value="" onkeypress="return enterNumOnly(event)"/>
				<span class="star" id="${sponsorPrefix}_national_approval_star" style="visibility:hidden">*</span>
			</td>
		</tr>

		<tr>
			<td class="formlabel"><insta:ltext key="registration.patient.sponsor.member.id"/>:</td>
			<td>
				<div id="${sponsorPrefix}NationalIdAutoComplete" class="autoComplete" style="padding-bottom: 20px;">
					<input type="text" name="${sponsorPrefix}_national_member_id" id="${sponsorPrefix}_national_member_id"
						class="field" maxlength="39"/>
					<div id="${sponsorPrefix}NationalIdContainer" style="width:240px;"></div>
				</div>
			</td>

			<td class="formlabel"><insta:ltext key="registration.patient.sponsor.member.name"/>:</td>
			<td><input type="text" name="${sponsorPrefix}_national_member_name" id="${sponsorPrefix}_national_member_name"></td>

			<td class="formlabel"><insta:ltext key="registration.patient.payment.relationShip"/>:</td>
			<td><input type="text" name="${sponsorPrefix}_national_relation" id="${sponsorPrefix}_national_relation"></td>
		</tr>
		<tr id="${sponsorPrefix}NationalFile">
			<td class="formlabel">
				<insta:ltext key="registration.patient.documentupload.header"/>:
			</td>
			<td colspan="2">
				<input type="hidden" name="${sponsorPrefix}_national_doc_type" value="SYS_RG">
				<fmt:formatDate var="cdate" value="${currentDate}" pattern="dd-MM-yyyy"/>
				<input type="hidden" name="${sponsorPrefix}_national_doc_date" value="${cdate}">

				<input type="file" name="${sponsorPrefix}_national_doc_content_bytea1" id="${sponsorPrefix}_national_doc_content_bytea1" accept="<insta:ltext key="upload.accept.image"/>,<insta:ltext key="upload.accept.pdf"/>">
				<input type="hidden" name="${sponsorPrefix}_national_doc_name" id="${sponsorPrefix}_national_doc_name1" value="National Card">
				<input type="hidden" name="${sponsorPrefix}_national_format" id="${sponsorPrefix}_national_format1" value="doc_fileupload">
				<input type="hidden" name="${sponsorPrefix}_national_mandatory" id="${sponsorPrefix}_national_mandatory1" value="X">

				<input type="hidden" name="${sponsorPrefix}_sponsor_cardfileLocationN" id="${sponsorPrefix}_sponsor_cardfileLocationN" value="" />
				<input type="hidden" name="${sponsorPrefix}_sponsor_cardContentTypeN" id="${sponsorPrefix}_sponsor_cardContentTypeN" value="" />

			</td>
			<c:if test="${(screenId eq 'reg_registration' && regPref.copy_paste_option eq 'Y') ||
					(screenId eq 'ip_registration' && regPref.copy_paste_option eq 'Y')}">
			<c:if test="${regPref.copy_paste_option eq 'Y'}">
				<td class="formlabel">
					<insta:ltext key="registration.patient.pasteImage.imageLabel"/>:
				</td>
				<td>
				</td>
				<td>
					<label id="view${sponsorPrefix}SponsorN" ><insta:ltext key='registration.patient.pasteImage.viewImageLabel'/></label>
				</td>
				</c:if>
			</c:if>
			<c:if test="${screenid eq 'Edit_Insurance' }">
			<td class="formlabel"></td>
			<td class="formlabel"><insta:ltext key="billing.changetpa.add.editpatientinsurance.documentusage"/>:</td>
			<td class="forminfo" style="width:180px;">
			  <select name="primary_insurance_document_usage" id="primary_insurance_document_usage" class="dropdown">
				<option value="New"><insta:ltext key="billing.changetpa.add.editpatientinsurance.addnewdocument"/></option>
				<option value="Update"><insta:ltext key="billing.changetpa.add.editpatientinsurance.updatedocument"/></option>
			   </select>
				<span class="star">*</span>
				<img class="imgHelpText" src="${cpath}/images/help.png"
 				title='<insta:ltext key="billing.changetpa.add.editpatientinsurance.membershipdetailstemplate"/>'/>
			</td>
		</c:if>
		</tr>
	</table>
</fieldset>
</td>
</tr>
