<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page import="org.apache.struts.Globals"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<c:set var="showCharges" value="${roleId == 1 || roleId == 2 ? 'A' : actionRightsMap.view_all_rates}"/>
<c:set var="allowRateIncrease" value="${roleId == 1 || roleId == 2 ? 'A' : actionRightsMap.allow_rateincrease}"/>
<c:set var="allowRateDecrease" value="${roleId == 1 || roleId == 2 ? 'A' : actionRightsMap.allow_ratedecrease}"/>
<c:set var="allowRateChange" value="${(allowRateIncrease == 'A' || allowRateDecrease == 'A') ? 'A' : 'N'}"/>
<c:set var="allowDiscount" value="${roleId == 1 || roleId == 2 ? 'A' : actionRightsMap.allow_discount}" />
<c:set var="allowTaxEdit" value="${roleId == 1 || roleId == 2 ? 'A' : actionRightsMap.allow_tax_subgroup_edit}" />
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<c:set var="prefbarcode" value="${generic_prefs.barcode_for_item}" />
<c:set var="allowDecimalsForQty" value='${generic_prefs.allow_decimals_in_qty_for_issues}' />
<head>
<title><insta:ltext key="salesissues.patientissue.addshow.title"/></title>
<style>
	table.detailFormTable { font-family:Verdana,Arial,sans-serif; font-size:9pt; border-collapse: collapse;}
	table.detailFormTable td.label { padding: 0px 2px 0px 2px; overflow: hidden; }
	tr.deleted {background-color: #F2DCDC; color: gray; }
	.kit  {color:darkgreen; background-color:#C5D9A3; layer-background-color:#C5D9A3;
        position:absolute;  left:100px; width:480px; height:280px;border:1px solid darkgreen;
        z-index:99;  visibility:hidden;}
    .nokit  {color:darkgreen; background-color:#C5D9A3; layer-background-color:#C5D9A3;
        position:absolute; top:200px; left:100px; width:300px; height:100px;border:6px groove #545565;border:1px solid darkgreen;
        z-index:99;font-size:14px;font-family:Helvetica;  visibility:hidden;}
    .cstk{background-color:#C0CFAF;}
    table tbody {
	    display: table;
	    width: 100%;
	}
	table.detailList {
	    border: 1px #CCCCCC solid;
	    empty-cells: show;
	    width: 100%;
    }
    .orderkits {
	    background: white url("../images/search-icon.png") left no-repeat;
	    background-size: 20px 20px;
	    padding-left: 10px;
	    border: 1px #999 solid;
	    height:19px;
	    display:inline-block;
	    width:10px;
	    border-right: 0px;
	    border-bottom: 1px #ccc solid;
	    border-left: 1px #ccc solid;
	}
	#mrnoContainer {
    	width: 500px;
	}
	.scrollit {
	    overflow-x:scroll;
	    width:100px;
	}

</style>
<script type="text/javascript">
		var showCharges = '${showCharges}';
		var gAllowRateIncrease = '${allowRateIncrease}';
		var gAllowRateDecrease = '${allowRateDecrease}';
		var allowDiscount = '${allowDiscount}';
		var allowTaxEdit = '${allowTaxEdit}';
		var prefBarCode = '${prefbarcode}';
		var allowDecimalsForQty = '${allowDecimalsForQty}';
		var orgDetails = '${ifn:convertListToJson(org_details)}';
		var storesJSON = ${ifn:convertListToJson(stores)};
		var gExpiryWarnDays = '${generic_prefs.warn_expiry}';
		var gAllowExpiredSale = '${generic_prefs.sale_expiry}';
		var gStockNegative = '${generic_prefs.stock_negative_sale}';
		var hdrugAlertNeeded = '${generic_prefs.pharmacy_schedule_h_alert}';
		var corporateInsurance = '${generic_prefs.corporate_insurance}';
		var discountPlansJSON = ${ifn:convertListToJson(discount_plans)};
		var orderKitJSON = ${ifn:convertListToJson(order_kits)};
		var gStoreId = '${store_id}';
		var deptId = '${ifn:cleanJavaScript(store_id)}';
		var message = '${ifn:cleanJavaScript(message)}';
		var gtPass = '${ifn:cleanJavaScript(gtpass)}';
		var type = '${ifn:cleanJavaScript(hospital)}';
		var billNo = '${ifn:cleanJavaScript(billNo)}';
		var subgroupNamesList = JSON.parse('${ifn:cleanJavaScript(subGroupListJSON)}'); 
		var groupListJSON = JSON.parse('${ifn:cleanJavaScript(groupListJSON)}');
</script>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="i18nSupport" content="true"/>
	<insta:link type="js" file="ajax.js" />
	<insta:link type="script" file="hmsvalidation.js"/>
  	<insta:link type="script" file="stores/patientissue/patientissue.js" />
	<insta:link type="js" file="stores/storeMedicinesAjax.js" />
	<insta:link type="js" file="stores/storeshelper.js" />
	<insta:link type="js" file="instaautocomplete.js" />
	<insta:link type="js" file="stores/loginDialog.js" />
	<insta:link type="js" file="Insurance/insuranceCalculation.js" />
	<jsp:include page="/pages/Common/MrnoPrefix.jsp" />
<style>
	#dialog_mask.mask {
	    z-index: 1;
	    display:none;
	    position:absolute;
	    top:0;
	    left:0;
	    -moz-opacity: 0.0001;
	    opacity:0.0001;
	    filter: alpha(opacity=50);
	    background-color:#CCC;
	}
	.scrolForContainer .yui-ac-content{
	 max-height:18em;overflow:auto;overflow-x:auto; /* scrolling */
    _height:18em; max-width:35em; width:35em;/* ie6 */
    }
    .scrolOrderKitForContainer .yui-ac-content{
	 margin-left: -20px;
	 max-height:18em;overflow:auto;overflow-x:auto; /* scrolling */
    _height:18em; max-width:40em; width:40em;/* ie6 */
	}
</style>
<c:choose>
	<c:when test="${not empty groupStoreId}">
		<c:set var="defStoreVal" value="${groupStoreId}"/>
	</c:when>
	<c:otherwise>
		<c:set var="defStoreVal" value="${store_id}"/>
	</c:otherwise>
</c:choose>


<script type="text/javascript">
	var popurl = '${pageContext.request.contextPath}';
	var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
	var isSharedLogIn = '${ifn:cleanJavaScript(isSharedLogIn)}';
	var actionId = '${actionId}';
	var noAccess = false;
</script>
<insta:js-bundle prefix="sales.issues.storesuserissues"/>
<insta:js-bundle prefix="sales.issues"/>
</head>
<body onload="init();" class="yui-skin-sam">
	<div id="patientPlanDetailsDialog" style="display:none;visibility:hidden;" ondblclick="handlePatientPlanDetailsDialogCancel();">
		<div class="bd" id="bd1" style="padding-top: 0px;">
			<table class="formTable" align="center" id="pd_planDialogTable" style="width:480px;">
				<tr>
					<td>
						<fieldset class="fieldSetBorder" style="width:480px;white-space: normal;">
						<legend class="fieldSetLabel" style="white-space: normal;"><insta:ltext key="patientdetails.common.tag.plansummary"/></legend>
								<table class="formTable" align="center" style="width:480px;">
									<tr>
										<td>
											<fieldset class="fieldSetBorder" style="width:450px;white-space: normal;">
												<legend class="fieldSetLabel"><insta:ltext key="patientdetails.common.tag.plan.exclusions"/></legend>
													<p id="plan_exclusions" style="width:450px;word-wrap:break-word;line-height: 14px;white-space: normal;">
														${fn:replace('<label id="plan_exclusions"></label>', newLineChar, "<br />")}
													</p>										
											 </fieldset>
										</td>
									</tr>
									<tr>
										<td>
										<fieldset class="fieldSetBorder" style="width:450px;white-space: normal;">
											<legend class="fieldSetLabel"><insta:ltext key="patientdetails.common.tag.plan.notes"/></legend>
												<p style="width:450px;word-wrap:break-word;line-height: 14px;white-space: normal;">
												 	${fn:replace('<label id="plan_notes"></label>', newLineChar, "<br />")}
												</p>									
										</fieldset>
										</td>
									</tr>
								</table>
						 </fieldset>
					</td>
				</tr>
				<tr>
					<td align="left">
						<input type="button" value="Close" style="cursor:pointer;" onclick="handlePatientPlanDetailsDialogCancel();"/>
					</td>
				</tr>
			</table>
		</div>
	</div>

	<div id="patientSecPlanDetailsDialog" style="display:none;visibility:hidden;" ondblclick="handlePatientSecPlanDetailsDialogCancel();">
		<div class="bd" id="bd1" style="padding-top: 0px;">
			<table class="formTable" align="center" id="pd_secplanDialogTable" style="width:480px;">
				<tr>
					<td>
						<fieldset class="fieldSetBorder" style="width:480px;white-space: normal;">
						<legend class="fieldSetLabel" style="white-space: normal;"><insta:ltext key="patientdetails.common.tag.plansummary"/></legend>
								<table class="formTable" align="center" style="width:480px;">
									<tr>
										<td>
											<fieldset class="fieldSetBorder" style="width:450px;white-space: normal;">
												<legend class="fieldSetLabel"><insta:ltext key="patientdetails.common.tag.plan.exclusions"/></legend>					
													<p style="width:450px;word-wrap:break-word;line-height: 14px;white-space: normal;">
														${fn:replace('<label id="sec_plan_exclusions"></label>', newLineChar, "<br />")}
													</p>										
											 </fieldset>
										</td>
									</tr>
									<tr>
										<td>
										<fieldset class="fieldSetBorder" style="width:450px;white-space: normal;">
											<legend class="fieldSetLabel"><insta:ltext key="patientdetails.common.tag.plan.notes"/></legend>
												<p style="width:450px;word-wrap:break-word;line-height: 14px;white-space: normal;"> 
													${fn:replace('<label id="sec_plan_notes"></label>', newLineChar, "<br />")}
												</p>	
										</fieldset>
										</td>
									</tr>
								</table>
						 </fieldset>
					</td>
				</tr>
				<tr>
					<td align="left">
						<input type="button" value="Close" style="cursor:pointer;" onclick="handlePatientSecPlanDetailsDialogCancel();"/>
					</td>
				</tr>
			</table>
		</div>
	</div>
<div id="storecheck" style="display: block;">
 <form method="POST" action="${cpath}/patientissues/saveissuedetails.htm" name="patientissueform" autocomplete="off" id="patientissueform">
    	<input type="hidden" name="_method" value="savePatientItemsIssued">
    	<input type="hidden" name="saleType" value="${ifn:cleanHtmlAttribute(saleType)}">
		<input type="hidden" name="is_tpa" id="is_tpa" value="" />
		<input type="hidden" name="mr_no_hid" id="mr_no_hid" value="" />
    	<input type="hidden" name="orgId" value="">
    	<input type="hidden" name="stocktype" id="stocktype" value=""/>
    	<input type="hidden" name="tran_type" id="tran_type" value="Patient" />
    	<input type="hidden" id="dialogId" value=""/>
    	<input type="hidden" name="isSharedLogIn" value="${ifn:cleanHtmlAttribute(isSharedLogIn)}"/>
		<input type="hidden" name="authUser" value=""/>
		<input type="hidden" name="patient_indent_no_param" value="${ifn:cleanHtmlAttribute(param.patient_indent_no)}"/>
		<input type="hidden" name="operation_details_id" value="${ifn:cleanHtmlAttribute(operation_details_id)}"/>
		<input type="hidden" name="fromOTScreen" value="${ifn:cleanHtmlAttribute(fromOTScreen)}"/>
		<input type="hidden" name="visitIdForOT" value="${ifn:cleanHtmlAttribute(visitIdForOT)}"/>
		<h1><insta:ltext key="salesissues.patientissue.addshow.patientissue"/></h1>

    	<div>
			<c:choose>
				<c:when test="${param.flag == false }">
					<span class="resultMessage"><insta:ltext key="${ifn:cleanHtml(msg)}"/></span>
				</c:when>
				<c:when test="${param.message == 0 }">
					<span class="resultMessage"></span>
				</c:when>
		    	<c:when test="${param.message != 0 && param.gtpass eq true}" >
					<span class="resultMessage"><insta:ltext key="salesissues.patientissue.addshow.itemsissued"/></span>
				</c:when>
				<c:when test="${param.message != null && param.message != 0 && param.gtpass eq false}" >
					<span class="resultMessage"><insta:ltext key="salesissues.patientissue.addshow.itemsissuedtotheuser"/></span>
				</c:when>
				<%-- <c:when test="${message == null }" >
					<span class="resultMessage"><insta:ltext key="salesissues.patientissue.addshow.transactionfailure"/></span>
				</c:when> --%>

			</c:choose>
	    </div></br>
	    <insta:feedback-panel/>
	    <!-- Patient Header -->
		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="salesissues.patientissue.addshow.patientissuedetails"/></legend>
			<table class="formtable" cellpadding="0" cellspacing="0" width="100%" border="0">
				<tr><td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.patient"/>:</td>
					<td valign="middle">
						<table>
					 		<tr>
					 			<td>
									<div id="mrnoAutocomplete" style="width: 100px;vertical-align:middle; ">
										<input type="text" class="field" name="mrno" tabindex="5" id="mrno" style="width: 115px;" />
										<div id="mrnoContainer"></div>
									</div>
								</td>
								<td>
									<span class="star" style="padding-left:0px;padding-right:80px;vertical-align:top;">*</span>
								</td>
							</tr>
						</table>
					</td>
					<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.store"/>:</td>
					<c:choose>
						<c:when test="${(roleId == 1) || (roleId == 2) || (multiStoreAccess == 'A')}">
							<td>
								<insta:dropdown username="${userid}" elename="store" onchange="onChangeStore(this.value);getPatientFromBill();" id="store" selected_value="${defStoreVal}" display_field="dept_name" display_value="dept_id" datalist="${stores}" tabindex="7"/>
							</td>
						</c:when>
						<c:otherwise>
							<td colspan="2">
								<b>${ifn:cleanHtml(stores[0].dept_name)}</b>
								<input type = "hidden" name="store" id="store" value="${ifn:cleanHtmlAttribute(store_id)}" />
							</td>
						</c:otherwise>
					</c:choose>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.issuereason"/>:</td>
					<td>
						<input type="text" name="reason" id="reason" maxlength="30" tabindex="10" onblur="upperCase(reason)" >
					</td>
					<c:choose>
						<c:when test="${(roleId == 1) || (roleId == 2) || (actionRightsMap.allow_backdate == 'A')}">
							<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.date"/>:</td>
							<td><insta:datewidget name="issueDate" valid="past" value="today" btnPos="left" tabindex="12" />
								<input type="text" name="issueTime" id = "issueTime" style="width:5em" value='<fmt:formatDate value="${serverNow}" pattern="HH:mm"/>'/>
							</td>
							<td>
								<input type="checkbox" name="gatepass" id="gatepass"  tabindex="13"><insta:ltext key="salesissues.patientissue.addshow.gatepass"/>
							</td>
						</c:when>
						<c:otherwise>
							<td></td>
							<td>
								<input type="checkbox" name="gatepass" id="gatepass"  tabindex="13"><insta:ltext key="salesissues.patientissue.addshow.gatepass"/>
							</td>
						</c:otherwise>
					</c:choose>
				</tr>
			</table>
			<!-- End Patient Header -->
		</fieldset>
		<!-- Patient Details -->
		<div id="patientDetails" style="display:none">
			<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="salesissues.patientissue.addshow.patientdetails"/></legend>
				<table class="patientdetails" cellpadding="0" cellspacing="0" width="100%" border="0">
					<tr>
						<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.mrno"/>:</td>
						<td class="forminfo"><div id="patientMrno"></div></td>
						<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.name"/>:</td>
						<td class="forminfo"><div id="patientName"></div></td>
						<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.age.or.gender"/>:</td>
						<td class="forminfo"><div id="patientAgeSex"></div></td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.visitno"/>:</td>
						<td class="forminfo">
							<div id="patientVisitNo"></div>
							<input name="visitId" type="hidden"><input name="visitType" id="visitType" type="hidden">
						</td>
						<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.dept"/>:</td>
						<td class="forminfo"><div id="patientDept"></div></td>
						<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.doctor"/>:</td>
						<td class="forminfo"><div id="patientDoctor"></div></td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.adm.or.regdate"/>:</td>
						<td class="forminfo"><div id="admitDate"><div></td>
						<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.referredby"/>:</td>
						<td class="forminfo"><div id="referredBy"></div></td>
						<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.bedtype"/>:</td>
						<td class="forminfo">
							<div id="patientBedType"></div>
							<input type="hidden" id="planId" name="planId" value="0"/>
						</td>
					</tr>
					<tr id="primarySponsorRow" style="display:none">
						<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.rateplan"/>:</td>
						<td class="forminfo"><div id="ratePlan"></div></td>

						<td class="formlabel"><label id="priSponsorType"></label></td>
						<td class="forminfo"><label id="priSponsorName"></label></td>

						<td class="formlabel"><label id="priIDName"></label></td>
						<td class="forminfo"><label id="priID"></label></td>
					</tr>
					<tr id="pritpaextrow" style="display:none">
						<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.membershipid"/>:</td>
						<td class="forminfo"><div id="priPolicyId"></div></td>
						<td class="formlabel" id="networkPlanTypeLblCell"><insta:ltext key="salesissues.patientissue.addshow.network.or.plantype"/>:</td>
						<td class="forminfo" id="networkPlanTypeValueCell"><div id="priPlanType"></div></td>
						<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.planname"/>:</td>
						<td class="forminfo">
							<div id="priPlanname" style="float:left;padding-right:5px;padding-top: 2%;width:80%;">
							</div>
							<button id="pd_planButton" title="${title2}" style="float:left; vertical-align:middle;cursor:pointer;width:15%;"
							onclick="javascript:initPatientPlanDetailsDialog();showPatientPlanDetailsDialog();" type="button" > .. </button>
						</td>
						<td class="forminfo"><div id="discountPlanId" style="display:none"></div></td> 
					</tr>
					<tr id="secSponsorRow" style="display:none">
						<td class="formlabel"></td>
						<td class="forminfo"></td>

						<td class="formlabel"><label id="secSponsorType"></label>:</td>
						<td class="forminfo"><label id="secSponsorName"></label></td>

						<td class="formlabel"><label id="secIDName"></label>:</td>
						<td class="forminfo"><label id="secID"></label></td>
					</tr>
					<tr id="sectpaextrow" style="display:none">
						<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.membershipid"/>:</td>
						<td class="forminfo"><div id="secPolicyId"></div></td>
						<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.network.or.plantype"/>:</td>
						<td class="forminfo"><div id="secPlanType"></div></td>
						<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.planname"/>:</td>
						<td class="forminfo">
							<div id="secPlanname" style="float:left;padding-right:5px;padding-top: 2%;width:80%;">
							</div>
							<button id="pd_planButton2" title="${title2}" style="float:left; text-align:right; vertical-align:middle;cursor:pointer;width:15%"
								onclick="javascript:initPatientSecPlanDetailsDialog();showPatientSecPlanDetailsDialog();" type="button" > .. </button>
						</td>
					</tr>
				</table>
			</fieldset>
		</div>
		<!-- End Patient Details -->
		<!-- Indent Details -->
		<fieldset class="fieldSetBorder" id="prescFieldSet">
			<legend class="fieldSetLabel"><insta:ltext key="salesissues.patientissue.addshow.indentpackagedetails"/></legend>
			<div id="prescDetailsDiv" style="display:none">
				<table id="prescInfo">
					<tr>
						<th align="left" style="padding-right: 5px"><insta:ltext key="salesissues.patientissue.addshow.indent"/></th>
						<th align="left" style="padding-right: 5px"><insta:ltext key="salesissues.patientissue.addshow.indent"/></th>
						<th align="left"><insta:ltext key="salesissues.patientissue.addshow.dispensestatus"/></th>
					</tr>
					<tr id="prescTemplateRow" style="display:none" >
						<td style="padding-right: 5px"></td>
						<td style="padding-right: 5px"><a target="_blank" href=""><insta:ltext key="salesissues.patientissue.addshow.view"/></a></td>
						<td>
							<select name="dispense_status" class="dropdown">
								<option value="all"><insta:ltext key="salesissues.patientissue.addshow.closeall"/></option>
								<option value="partiall"><insta:ltext key="salesissues.patientissue.addshow.closedispensed"/></option>
								<option value="full" selected><insta:ltext key="salesissues.patientissue.addshow.closefullydispensed"/></option>
							</select>
							<input type="hidden" name="patient_indent_no_ref"/>
						</td>
					</tr>
				</table>
			</div>
			<div id ="packageDetailsDiv">
				<table id ="packageInfo">
					<tr>
						<td class="formlabel" style="padding:5px;" > <insta:ltext key="salesissues.patientissue.addshow.activepackages"/>:</td>
						<td>
							<select name="activePackage" id="activePackage" style="width:150" class="dropdown" onchange="onChangePackage(this.value)">
								<option value="">--- Select ---</option>
							</select>
							<input type="hidden" name="package_id" id="package_id"/>
							<input type="hidden" name="pkg_charge_id_ref" id="pkg_charge_id_ref"/>
							<input type="hidden" name="pkg_ins_cat_id" id="pkg_ins_cat_id"/>
						</td>
					</tr>
				</table>
			</div>
			<table id="unavblMedicines"></table>
		</fieldset>
		<!-- End Indent Details -->
		<!-- Item grid -->
		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="salesissues.patientissue.addshow.itemlist"/></legend>
			<div class="resultList" style="margin: 10px 0px 5px 0px;">
			<table class="detailList scrollit" cellspacing="0" cellpadding="0" id="itemListtable" border="0">
				<tr>
					<th><insta:ltext key="salesissues.patientissue.addshow.itemname"/></th>
					<th><insta:ltext key="salesissues.patientissue.addshow.controltype"/></th>
					<th><insta:ltext key="salesissues.patientissue.addshow.batch.or.sno"/></th>
					<th><insta:ltext key="salesissues.patientissue.addshow.exp.date"/></th>
					<th><insta:ltext key="salesissues.patientissue.addshow.issueqty"/></th>
					<th><insta:ltext key="salesissues.patientissue.addshow.issueuom"/></th>
					<th><insta:ltext key="salesissues.patientissue.addshow.pkgsize"/></th>
					<c:if test="${showCharges == 'A'}">
						<th><insta:ltext key="salesissues.patientissue.addshow.rate"/></th>
						<th><insta:ltext key="salesissues.patientissue.addshow.unitrate"/></th>
						<th><insta:ltext key="salesissues.patientissue.addshow.pkgmrp"/></th>
						<th><insta:ltext key="salesissues.patientissue.addshow.discount"/></th>
						<th><insta:ltext key="salesissues.patientissue.addshow.issueamt"/></th>
						<th><insta:ltext key="salesissues.patientissue.addshow.tax"/></th>
						<th><insta:ltext key="salesissues.patientissue.addshow.pat.amt"/></th>
						<th><insta:ltext key="salesissues.patientissue.addshow.pat.tax"/></th>
						<th><insta:ltext key="salesissues.patientissue.addshow.pri.claim"/></th>
						<th><insta:ltext key="salesissues.patientissue.addshow.pri.tax"/></th>
						<th><insta:ltext key="salesissues.patientissue.addshow.sec.claim"/></th>
						<th><insta:ltext key="salesissues.patientissue.addshow.sec.tax"/></th>
					</c:if>
					<th></th>
					<th></th>
				</tr>
				<!-- Add proper column name that matches to db column names. -->
				<tr id="tableRow1">
						<td><label id="flagImg1"></label>
						<label id="itemLabel1"></label>
						<input type="hidden" name="temp_charge_id" id="temp_charge_id1" value=""/>
						<input type="hidden" name="item_name" id="item_name1" value=""/>
						<input type="hidden" name="item_bar_code_id" id="item_bar_code_id1" value=""/>
						<input type="hidden" name="pkg_mrp" id="pkg_mrp1" value="" />
						<input type="hidden" name="pkg_unit" id="pkg_unit1" value=""/>
						<input type="hidden" name="tax_per" id="tax_per1" value=""/>
						<input type="hidden" name="tax_type" id="tax_type1" value=""/>
						<input type="hidden" name="tax_amt" id="tax_amt1" value=""/>
						<input type="hidden" name="original_tax" id="original_tax1" value="" />
						<input type="hidden" name="amt" id="amt1" value="" />
						<input type="hidden" name="category" id="category1" value="" />
						<input type="hidden" name="insurancecategory" id="insurancecategory1" value="" />
						<input type="hidden" name="billinggroup" id="billinggroup1" value="" />
						<input type="hidden" name="pat_pkg_content_id" id="pat_pkg_content_id1" value="" />
						<input type="hidden" name="pri_ins_amt" id="pri_ins_amt1" value="" />
						<input type="hidden" name="sec_ins_amt" id="sec_ins_amt1" value="" />
						<input type="hidden" name="original_mrp" id="original_mrp1" value="" />
						<input type="hidden" name="item_unit" id="item_unit1" value="" />
						<input type="hidden" name="indent_item_id" id="indent_item_id1"/>
						<input type="hidden" name="patient_indent_no" id="patient_indent_no1"/>
						<input type="hidden" name="medicine_id" id="medicine_id1" value=""/>
						<input type="hidden" name="cat_payable" value="t"/>
						<input type="hidden" name="issue_base_unit" id="issue_base_unit1" value=""/>
						<input type="hidden" name="control_type_id" id="control_type_id1" value=""/>
						<input type="hidden" name="control_type_name" id="control_type_name1" value=""/>
						<input type="hidden" name="priCatPayable" id="priCatPayable1" value="t"/>
						<input type="hidden" name="pri_ins_tax" id="pri_ins_tax1" value=""/>
						<input type="hidden" name="sec_ins_tax" id="sec_ins_tax1" value=""/>
						<input type="hidden" name="tax_sub_group_ids" id="tax_sub_group_ids" value=""/>
					</td>
					<td><label id="controleTypeLabel1"></label></td>
					<td>
						<label id="identifierLabel1"></label>
						<input type="hidden" name="item_identifier" id="item_identifier1"/>
						<input type="hidden" name="item_batch_id" id="item_batch_id1"/>
						<input type="hidden" name="exp_dt" id="exp_dt1"/>
						<input type="hidden" name="stype" id="stype1"/>
					</td>
					<td><label id="expdtLabel1"></label></td>
					<td>
						<label id="issue_qtyLabel1" ></label>
						<input type="hidden" name="issue_qty" id="issue_qty1"/>
						<input type="hidden" name="pkg_issue_qty" id= "pkg_issue_qty1">
					</td>
					<td>
						<label id="uomLabel1" ></label>
					</td>
					<td>
						<label id="pkgSizeLabel1" ></label>
						<input  type="hidden" name="item_billable" id="item_billable_hidden1"/>
						<input  type="hidden" name="package_size" id="package_size1"/>
						<input  type="hidden" name="unit_mrp" id="unit_mrp1"/>
						<c:forEach items="${groupList}" var="group">
							<input type="hidden" name="taxname${group.item_group_id}" id="taxname${group.item_group_id}1" value="" />
							<input type="hidden" name="taxrate${group.item_group_id}" id="taxrate${group.item_group_id}1" value="" />
							<input type="hidden" name="taxamount${group.item_group_id}" id="taxamount${group.item_group_id}1" value="" />
							<input type="hidden" name="taxsubgroupid${group.item_group_id}" id="taxsubgroupid${group.item_group_id}1" value="" />
						</c:forEach>
					</td>
					<c:if test="${showCharges == 'A'}">
						<td>
							<label id="mrpLabel1"></label>
						</td>
						<td>
							<label id="unitmrpLabel1"></label>
						</td>
						<td>
							<label id="pkgMRPLabel1"></label>
						</td>
						<td>
							<label id="discountLabel1"></label>
						</td>
						<td>
							<label id="totamtLabel1"></label>
						</td>
						<td>
							<label id="taxamtLabel1"></label>
						</td>
						<td>
							<label id="totpatamtLabel1"></label>
						</td>
						<td>
							<label id="totpattaxLabel1"></label>
						</td>
						<td>
							<label id="pri_totinsamtLabel1"></label>
						</td>
						<td>
							<label id="pri_totinstaxLabel1"></label>
						</td>
						<td>
							<label id="sec_totinsamtLabel1"></label>
						</td>
						<td>
							<label id="sec_totinstaxLabel1"></label>
						</td>
					</c:if>
					<td><label id="itemRow1"></label>
						<input type="hidden" name="hdeleted" id="hdeleted1"/>
						<input type="hidden" name="mrpHid" id="mrpHid1" />
						<input type="hidden" name="unitMrpHid" id="unitMrpHid1"/>
						<input type="hidden" name="origUnitMrpHid" id="origUnitMrpHid1"/>
						<input type="hidden" name="discountHid" id="discountHid1" />
						<input type="hidden" name="discountAmtHid" id="discountAmtHid1"/>
						<input type="hidden" name="coverdbyinsuranceflag" id="coverdbyinsuranceflag1"/>
					</td>
					<td>
						<button name="addBut" id="addBut1" onclick="openDialogBox(1); return false;" class="imgButton" 
							accesskey="+" title='<insta:ltext key="salesissues.pharmacyestimateslist.list.addnewitem"/>' tabindex="15">
							<img class="button" name="add" id="add1" src='${cpath}/icons/Add.png' style="cursor:pointer;" >
						</button>
					</td>
				</tr>
			 </table>
			 </div>
			 <div class="legend" >
				<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
				<div class="flagText"><insta:ltext key="salesissues.patientissue.addshow.consignmentstock"/></div>
				<div class="flag"><img src='${cpath}/images/purple_flag.gif'></div>
				<div class="flagText"><insta:ltext key="salesissues.sales.details.notcoverdbyinsurance"/></div>
			</div>
		</fieldset>
		<!-- End item grid -->
		<!-- Totals -->
		<table id="table_b">
			<tr>
				<td>
					<div id="creditbill" style="display: none;">
						<label><insta:ltext key="salesissues.patientissue.addshow.bills"/>&nbsp;:</label>
						<select name="bill_no" id="bill_no" class="dropdown" onchange="onChangeBill()" tabindex="75">
							<option value="C"></option>
						</select>
						<c:if test="${showCharges == 'A'}">
							&nbsp;&nbsp;<label class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.totalamount"/>:&nbsp;&nbsp;</label>
							<label id="totAmt" class="forminfo">0.00</label>
							&nbsp;&nbsp;<label class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.totaltax"/>:&nbsp;&nbsp;</label>
							<label id="totTax" class="forminfo">0.00</label>
							&nbsp;&nbsp;<label class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.patientportion"/>:&nbsp;&nbsp;</label>
							<label id="totPatAmt" class="forminfo">0.00</label>
							&nbsp;&nbsp;<label class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.patienttax"/>:&nbsp;&nbsp;</label>
							<label id="totPatTax" class="forminfo">0.00</label>
							&nbsp;&nbsp;<label class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.claimamount"/>:&nbsp;&nbsp;</label>
							<label id="totClaimAmt" class="forminfo">0.00</label>
							&nbsp;&nbsp;<label class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.claimtax"/>:&nbsp;&nbsp;</label>
							<label id="totClaimTax" class="forminfo">0.00</label>
						</c:if>
					</div>
				</td>
			</tr>
			<tr></tr>
		</table>
		<!-- End Totals -->
		<!-- Save button -->
		<div class="screenActions">
			<input type="hidden" name="is-new-ux" id="is-new-ux" value="${isNewUX}"/>
			<button id="save" type="button" accesskey="S" class="button" tabindex="80" onclick="return submitForm(this,'Patient');" ><b><u><insta:ltext key="salesissues.patientissue.addshow.s"/></u></b><insta:ltext key="salesissues.patientissue.addshow.ave"/></button>
			<button type="reset" accesskey="R" class="button" tabindex="85" onclick="clearPatientDetails();onChangeStore(store.value);"><b><u><insta:ltext key="salesissues.patientissue.addshow.r"/></u></b><insta:ltext key="salesissues.patientissue.addshow.eset"/></button>
			<c:if test="${fromOTScreen == 'Y'}">
				|&nbsp;<a href="${cpath}/otservices/OtManagement.do?_method=getOtManagementScreen&operation_details_id=${ifn:cleanURL(operation_details_id)}&visit_id=${ifn:cleanURL(visitIdForOT)}">
					<insta:ltext key="salesissues.patientissue.addshow.otmanagement"/>
				</a>
			</c:if>
		</div>
		<!-- End Save button -->
	</form>
	<!-- Item Dialog -->
	<div id="dialog" style="visibility:hidden">
		<form name="patientissuedailog" id="patientissuedailog">
		<div class="bd">
		<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="salesissues.patientissue.addshow.add.or.edititem"/></legend>
			<table class="formtable">	
				<tr>
					<td align="left" style="width: 100px;">
						<input type="radio" name="itemtype" id="itemname" value="itemname" onclick="onChangeItemType()"/>
						<label for="item">Items</label>
					</td>
					<td align="left">
						<input type="radio" name="itemtype" id="order" value="order" onclick="onChangeItemType()"/>
						<label for="order">Order Kits</label>
					</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
			</table>
			<table class="formtable" style="display:none" id="orderKitDetails">
				<tr>
					<td  class="formlabel" style="width: 10px;">Order Kit Name:</td>
					<td valign="top" >
						<div id="orderkit_wrapper" style="width: 20em; padding-bottom:0.2em; z-index: 9000;" >
							<input type="text" name="orderkits" id="orderkits"  tabindex="20" style="width:422px;" value="" onkeypress="return onKeyPressAddQty(event)"/>
							<div id="orderkit_dropdown" style="width: 422px;"></div>
						</div>
					</td>
				</tr>
			</table>
			<br/>
			<div class="formlabel" id="itemInfo" style="font-size:14px;text-align:left;"></div>
			<table  cellpadding="0" cellspacing="0" width="100%" border="0" 
					style="display:none;word-wrap:break-word;table-layout: fixed;padding-top:5px;padding-bottom:5px;color:black;background-color:#F0F0F5;" 
					id="orderKitMissedItemsHeader">	
				<tr>
					<th style="width:465px;font-family:Arial-BoldMT;text-align: left;padding-left: 13px;padding-top: 8px;color:#666666;font-size:11px;"><b>ITEM NAME</b></th>
					<th style="text-align: left;padding-left: 50px;padding-top: 8px;font-family:Arial-BoldMT;color:#666666;font-size:11px;"><b>QUANTITY</b></th>
				</tr>
			</table>
			<table class="detailList" cellpadding="0" cellspacing="0" border="0" 
				style="display:none;width:100%;height:150px;overflow:auto;table-layout: fixed;" id="orderKitMissedItems">	
			</table>
			<table class="formtable" style="display:none" id="itemDetails">
				<tr>
					<c:if test="${prefbarcode eq 'Y'}">
						<td  class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.itembarcode"/>:</td>
						<td>
							<input type="text" name="barCodeId" id="barCodeId" onchange="getItemBarCodeDetails(this.value);" onkeypress="preventOpenDialogOnEnterKey(this.value);" tabindex="20">
						</td>
					</c:if>
					<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.items"/>:</td>
					<td valign="top" >
						<div id="item_wrapper" style="width: 16em; padding-bottom:0.2em; z-index: 9000;" >
							<input type="text" name="items" id="items" tabindex="25" style="width: 16em"  />
							<div id="item_dropdown" class="scrolForContainer"></div>
						</div>
						<c:if test="${prefbarcode ne 'Y'}">
							<input type="hidden" name="barCodeId" id="barCodeId" >
						</c:if>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.batch"/>&nbsp;<insta:ltext key="salesissues.patientissue.addshow.no.avblqty.exp.dt"/>:</td>
					<td>
						<select name="batch" id="batch" style="width: 200" tabindex="30" onchange="onChangeBatch(this.value);" class="dropdown">
							<option value=""><insta:ltext key="salesissues.patientissue.addshow.select"/></option>
						</select>
						<input type="hidden" id="item_batch_id" name="item_batch_id" value =""/>
						<input type="hidden" id="batch_no" name="batch_no" value =""/>
					</td>
					<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.availableqty"/>:</td>
					<td>
						<b><label id="avlqtylabel">0</label></b>
					</td>
				</tr>

				<c:choose>
					<c:when test="${showCharges == 'A'}">
						<tr>
							<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.issuerate"/>:</td>
							<td class="forminfo" style="width:7em">
								<c:choose>
									<c:when test="${allowRateChange == 'A'}">
										<input type="text" name="itemMrp" id="itemMrp" class="number" tabindex="50" onkeypress="return enterNumOnlyANDdot(event);" onchange="onChangeRate(this.value)"/>
									</c:when>
									<c:otherwise>
										<input type="text" name="itemMrp" id="itemMrp" class="number" readonly/>
									</c:otherwise>
								</c:choose>
							</td>
							<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.unit"/>&nbsp;<insta:ltext key="salesissues.patientissue.addshow.rate"/>:</td>
							<td class="forminfo" style="width:7em">
								<input type="text" name="unitMrp" id="unitMrp" class="number" readonly/>
								<input type="hidden" id="unit" name="unit" value=""/>
								<input type="hidden" id="unit_mrp" name="unit_mrp" value=""/>
								<input type="hidden" id="discount_amt" name="discount_amt" value=""/>
								<input type="hidden" id="discount_per" name="discount_per" value=""/>
							</td>
						</tr>
					</c:when>
					<c:otherwise>
							<input type="hidden" id="unit" name="unit" value=""/>
							<input type="hidden" id="unit_mrp" name="unit_mrp" value=""/>
							<input type="hidden" name="discount_amt" id="discount_amt" value="0"/>
							<input type="hidden" name="discount_per" id="discount_per" value="0"/>
					</c:otherwise>
				</c:choose>

				<tr>
					<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.issueqty"/>:</td>
					<td style="width:7em">
						<input type="text" name="issuQty" id="issuQty" style="text-align:right;width: 50" class="num" maxlength="6" tabindex="51" 
							onkeypress="return onKeyPressAddQty(event)" onchange="onChangeQty(this.value);" />
						<select name="item_unit" id="item_unit" onchange="onChangeQty(document.getElementById('issuQty').value);" tabindex="52"></select>
					</td>
					<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.pkgsize"/>:
					</td>
					<td>
						<input type="text" id="pkg_size" name="pkg_size" readonly="true"/>
						<input type="hidden" id="itemBillable" name="itemBillable" value=""/>
						<input type="hidden" id="mrp" name="mrp" value="" />
						<input type="hidden" id="tax" name="tax" value=""/>
						<input type="hidden" id="original_tax" name="original_tax" value=""/>
						<input type="hidden" id="tax_rate" name="tax_rate" value=""/>
						<input type="hidden" id="taxType" name="taxType" value=""/>
						<input type="hidden" id="patper" name="patper" value=""/>
						<input type="hidden" id="patcatamt" name="patcatamt" value="" />
						<input type="hidden" id="patcap" name="patcap" value=""/>

						<input type="hidden" id="expdt" name="expdt" value="" />
						<input type="hidden" id="categoryId" name="categoryId" value="" />
						<input type="hidden" id="insuranceCategoryId" name="insuranceCategoryId" value=""/>
						<input type="hidden" id="billingGroupId" name="billingGroupId" value=""/>
						<input type="hidden" id="isFirstOfCategory" name="isFirstOfCategory" value="true"/>

						<input type="hidden" id="origMRP" name="origMRP" value=""/>
						<input type="hidden" id="origUnitMrpHid" name="origUnitMrpHid" value=""/>
						<input type="hidden" id="issue_base_unit" name="issue_base_unit" value=""/>
						<input type="hidden" id="control_type_id" name="control_type_id" value=""/>
						<input type="hidden" id="control_type_name" name="control_type_name" value=""/>
						<input type="hidden" id="priCatPayable" name="priCatPayable" value=""/>
						<input type="hidden" id="oldMRP" name="oldMRP" value=""/>
						<input type="hidden" id="medicine_id" name="medicine_id" value=""/>
						<input type="hidden" id="amount" name="amount" value=""/>
						<input type="hidden" id="issue_rate_expr" name="issue_rate_expr" value =""/>
						<input type="hidden" id="visit_selling_expr" name="visit_selling_expr" value =""/>
						<input type="hidden" id="store_selling_expr" name="store_selling_expr" value =""/>

						<c:forEach items="${groupList}" var="group">
							<input type="hidden" name="taxname${group.item_group_id}" id="taxname${group.item_group_id}" value="" />
							<input type="hidden" name="taxrate${group.item_group_id}" id="taxrate${group.item_group_id}" value="" />
							<input type="hidden" name="taxamount${group.item_group_id}" id="taxamount${group.item_group_id}" value="" />
							<input type="hidden" name="taxsubgroupid${group.item_group_id}" id="taxsubgroupid${group.item_group_id}" value="" />
						</c:forEach>
					</td>
				</tr>
				<tr id="add_tax_groups"></tr>		
				<tr>
					<c:choose>
						<c:when test="${showCharges == 'A'}">
						<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.discount.percentage.in.brackets"/>:</td>
						<td class="forminfo" style="width:7em">
							<c:choose>
								<c:when test="${allowDiscount == 'A'}">
									<input type="text" name="discount" id="discount" class="number" value="0" tabindex="55" onkeypress="return onKeyPressDiscount(event);" onChange="onChangeDiscount(this);"/>
								</c:when>
								<c:otherwise>
									<input type="text" name="discount" id="discount" class="number" value="0" readonly />
								</c:otherwise>
							</c:choose>
						</td>
						<td colspan=2>
							<table>
								<tr id="coverdbyinsurancestatusid">
									<td class="formLabel">
										<insta:ltext key="salesissues.sales.details.coverdbyinsurance"/>:
									</td>
									<td class="formInfo" colspan="2">
										<b><label id="coverdbyinsurance" ></label></b>
										<input type="hidden" name="coverdbyinsuranceflag" value="" id="coverdbyinsuranceflag">
									</td>
								</tr>
							</table>
						</td>
						</c:when>
						<c:otherwise>
							<td colspan=2>
								<table>
									<tr id="coverdbyinsurancestatusid">
										<td class="formLabel">
											<insta:ltext key="salesissues.sales.details.coverdbyinsurance"/>:
										</td>
										<td class="formInfo" colspan="2">
											<b><label id="coverdbyinsurance" ></label></b>
											<input type="hidden" name="coverdbyinsuranceflag" value="" id="coverdbyinsuranceflag">
										</td>
									</tr>
								</table>
							</td>
						</c:otherwise>
					</c:choose>
				</tr>
				<tr><td colspan="6">&nbsp;</td></tr>
				</table>
				<table  style="padding-top:7px;">
				<tr>
					<td colspan="6">
						<button type="button" id="OK" name="OK" accesskey="A"  style="display: inline;" class="button" onclick="addItemsToTable();" tabindex="60"><label><b><insta:ltext key="salesissues.patientissue.addshow.ok"/></b></label></button>
						<button type="button" id="Cancel" name="Cancel" accesskey="A"  style="display: inline;" class="button" onclick="handleCancel();" tabindex="70"><label><b><insta:ltext key="salesissues.patientissue.addshow.cancel"/></b></label></button>
					</td>
				</tr>
			</table>
			<table id="deletedrow"></table>
			</fieldset>
		</div>
		</form>
	</div>
	<!-- End Item Dialog -->
	</div>
	<!-- Login dialog -->
	<div id="loginDiv" style="display: none">
		<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"><insta:ltext key="salesissues.patientissue.addshow.logindetails"/></legend>
				<table class="formtable">
					<tr>
						<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.userid"/>: </td>
						<td><input type="text" name="login_user" id="login_user"/></td>
						<td class="formlabel">&nbsp;</td>
						<td>&nbsp;</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.password"/>: </td>
						<td><input type="password" name="login_password" id="login_password" onkeypress="return submitOnEnter(event);"/></td>
						<td class="formlabel">&nbsp;</td>
						<td>&nbsp;</td>
					</tr>
				</table>
				<table style="margin-top: 10px">
					<tr>
						<td><input type="button" name="submitForm" id="submitForm" value="Submit" />
							<input type="button" name="cancelSubmit" id="cancelSubmit" value="Cancel" />
						</td>
					</tr>
				</table>
			</fieldset>
		</div>
	</div>
	<!-- End Login dialog -->
	
	<script type="text/javascript">
		var cpath = '${pageContext.request.contextPath}';
		//RC: This field is not in use.
		var groupItemDetails = '';//${groupItemDetails};
		var groupStoreId  = '${ifn:cleanJavaScript(groupStoreId)}';
		var grpStoreItem_unit = '${grpStoreItem_unit}';
		var type = '${type}';
		
		var gRoleId = '${roleId}';
		var accessstores = '${multiStoreAccess}';
		var orgDetails = <%= request.getAttribute("orgDiscounts")%>;
		var gMarginAmt = '${sale_margin}';
		var visitId = '${ifn:cleanJavaScript(visit_id)}';
		var billNo = '${ifn:cleanJavaScript(bill_no)}';
		var storeMedicineAjaxUrlParamQueryStr = '&hosp=${ifn:cleanURL(sesHospitalId)}&issueType=CR&includeConsignmentStock=Y&includeZeroStock=' + ((gStockNegative == "A" || gStockNegative == "W") ? "Y" : "N");
	</script>
	<c:if test="${not empty defStoreVal}">
		<script src="${cpath}/stockdetails/getstockinstore.json?ts=${stock_timestamp}&hosp=${ifn:cleanURL(sesHospitalId)}&issueType=CR&includeConsignmentStock=Y&storeId=${ifn:cleanURL(defStoreVal)}&includeZeroStock=${(generic_prefs.stock_negative_sale == 'A' || generic_prefs.stock_negative_sale == 'W') ? 'Y' : 'N'}">
		</script>
	</c:if>
</body>
</html>

