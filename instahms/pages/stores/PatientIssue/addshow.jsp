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
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<c:set var="showCharges" value="${roleId == 1 || roleId == 2 ? 'A' : actionRightsMap.view_all_rates}"/>
<c:set var="allowRateIncrease" value="${roleId == 1 || roleId == 2 ? 'A' : actionRightsMap.allow_rateincrease}"/>
<c:set var="allowRateDecrease" value="${roleId == 1 || roleId == 2 ? 'A' : actionRightsMap.allow_ratedecrease}"/>
<c:set var="allowRateChange" value="${(allowRateIncrease == 'A' || allowRateDecrease == 'A') ? 'A' : 'N'}"/>
<c:set var="allowDiscount" value="${roleId == 1 || roleId == 2 ? 'A' : actionRightsMap.allow_discount}" />
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<c:set var="prefbarcode" value="<%= GenericPreferencesDAO.getGenericPreferences().getBarcodeForItem() %>" />
<c:set var="allowDecimalsForQty" value='<%=GenericPreferencesDAO.getAllPrefs().get("allow_decimals_in_qty_for_issues")%>' />
<c:set var="ip_credit_limit_rule" value='<%=GenericPreferencesDAO.getAllPrefs().get("ip_credit_limit_rule") %>' />
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

</style>
<c:set var="orgDetailsJSON" value="${ifn:JSONlist('organization_details')}"/>
<c:set var="storesJSONList" value="${ifn:JSONlist('stores')}"/>
<script type="text/javascript">
		var showCharges = '${showCharges}';
		var gAllowRateIncrease = '${allowRateIncrease}';
		var gAllowRateDecrease = '${allowRateDecrease}';
		var allowDiscount = '${allowDiscount}';
		var prefBarCode = '${prefbarcode}';
		var allowDecimalsForQty = '${allowDecimalsForQty}';
		var orgDetails = ${orgDetailsJSON};
		var storesJSON = ${storesJSONList};
		var gExpiryWarnDays = '${prefs.warn_expiry}';
		var gAllowExpiredSale = '${prefs.sale_expiry}';
		var gStockNegative = '${prefs.stock_negative_sale}';
		var hdrugAlertNeeded = '${prefs.pharmacy_schedule_h_alert}';
		var corporateInsurance = '${prefs.corporate_insurance}';
		var discountPlansJSON = '${discountPlansJSON}';
		var orderKitJSON = ${orderkitJSON};
		var gStoreId = ${store_id};
		var ip_credit_limit_rule = <insta:jsString value="${ip_credit_limit_rule}"/>;
</script>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="i18nSupport" content="true"/>
	<insta:link type="js" file="ajax.js" />
	<insta:link type="script" file="hmsvalidation.js"/>
  	<insta:link type="script" file="stores/patientissue.js" />
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
function toggleBox(szDivID, iState) // 1 visible, 0 hidden
{
    if(document.layers)	   //NN4+
    {
       document.layers[szDivID].visibility = iState ? "show" : "hide";
    }
    else if(document.getElementById)	  //gecko(NN6) + IE 5+
    {
        var obj = document.getElementById(szDivID);
        obj.style.visibility = iState ? "visible" : "hidden";
    }
    else if(document.all)	// IE 4
    {
        document.all[szDivID].style.visibility = iState ? "visible" : "hidden";
    }
}
var popurl = '${pageContext.request.contextPath}';
var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
var isSharedLogIn = '${ifn:cleanJavaScript(isSharedLogIn)}';
var actionId = '${actionId}';
var indentStore = ${param.indentStore == null ? (defStoreVal == '' ? 0 : defStoreVal) : param.indentStore};
var noAccess = false;
</script>
<insta:js-bundle prefix="sales.issues.storesuserissues"/>
<insta:js-bundle prefix="sales.issues"/>
</head>



<body onload="initpatient();AddGroupItemDetails('Patient'); enable(document.forms[0].mrno,'Patient');getReport('${ifn:cleanJavaScript(message)}','${ifn:cleanJavaScript(gtpass)}','${ifn:cleanJavaScript(hospital)}','Patient','${ifn:cleanJavaScript(billNo)}'); checkstoreallocation(); getPatientFromBill();" class="yui-skin-sam" >

<!-- <div ID="kitdetails" class="kit">
<marquee><b><span style="background-color: #ADCFAD;">Kit Details</span></b></marquee>
<fieldset class="fieldSetBorder" >
	<legend class="fieldSetLabel">Kit  Items list</legend>
	<div style="overflow:auto;max-height:15em"  align="center">
	<table class="dashboard" width="100%" cellspacing="0" cellpadding="0" id="table1">
		<tr bgcolor="#8FBC8F">
        <th>Category</th>
        <th>Item Name</th>
        <th>Billable</th>
        <th>Qty</th>
        </tr>
        </table></div></fieldset>
   <span style="text-align: center"><a href="#" onClick="toggleBox('kitdetails',0); return false"  title="Hide This Box">Hide This Box</a></span>

</div>
<div ID="nokit" class="nokit">
   <table>
   <tr><td>
   no Kit details.</td></tr>
   <tr><td align="center">
   <a href="#" onClick="toggleBox('nokit',0); return false"  title="Hide This Box">Hide This Box</a>
   </td></tr></table>
</div>

-->
 <div id="storecheck" style="display: block;" >
 <form method="POST" action="StockPatientIssue.do" name="patientissueform" autocomplete="off" id="patientissueform">
    	<input type="hidden" name="_method" value="saveItemsIssued">
    	<input type="hidden" name="saleType" value="${ifn:cleanHtmlAttribute(saleType)}">
    	<input type="hidden" name="orgId" value="">
    	<input type="hidden" name="stocktype" id="stocktype" value=""/>
    	<input type="hidden" id="dialogId" value=""/>
    	<input type="hidden" name="isSharedLogIn" value="${ifn:cleanHtmlAttribute(isSharedLogIn)}"/>
		<input type="hidden" name="authUser" value=""/>
		<input type="hidden" name="patient_indent_no_param" value="${ifn:cleanHtmlAttribute(param.patient_indent_no)}"/>
		<input type="hidden" name="operation_details_id" value="${ifn:cleanHtmlAttribute(operation_details_id)}"/>
		<input type="hidden" name="fromOTScreen" value="${ifn:cleanHtmlAttribute(fromOTScreen)}"/>
		<input type="hidden" name="visitIdForOT" value="${ifn:cleanHtmlAttribute(visitIdForOT)}"/>
		<h1><insta:ltext key="salesissues.patientissue.addshow.patientissue"/></h1>

    	<div >
			<c:choose>
				<c:when test="${flag == false }">
					<span class="resultMessage">${ifn:cleanHtml(msg)}</span>
				</c:when>
				<c:when test="${message == 0 }">
				<span class="resultMessage"></span>
				</c:when>
		    	<c:when test="${ message != 0 && gtpass eq true}" >
				<span class="resultMessage"><insta:ltext key="salesissues.patientissue.addshow.itemsissued"/></span>
				</c:when>
				<c:when test="${message == null }" >
				<span class="resultMessage"><insta:ltext key="salesissues.patientissue.addshow.transactionfailure"/></span>
				</c:when>

			</c:choose>
	    </div></br>
	    <insta:feedback-panel/>

			<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="salesissues.patientissue.addshow.patientissuedetails"/></legend>
				<table class="formtable" cellpadding="0" cellspacing="0" width="100%" border="0">
					<tr><td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.patient"/>:</td>
						 <td valign="middle">
						 	<table>
						 		<tr>
						 			<td>
									 	<div id="mrnoAutocomplete" style="width: 100px;vertical-align:middle; ">
									 		<input type="text" class="field" name="mrno" tabindex="5" id="mrno" style="width: 115px;" />
											<div id="mrnoContainer" ></div>
										 </div>
									</td>
									<td>
										<span class="star" style="padding-left:0px;padding-right:80px;vertical-align:top;">*</span>
									</td>
								</tr>
							</table>
						</td>
						<!-- <a href="" onclick="return popup_mrsearch();" id="search">Search</a>-->
						<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.store"/>:</td>
						<c:choose>
						<c:when test="${(roleId == 1) || (roleId == 2) || (multiStoreAccess == 'A')}">
						<td><insta:userstores username="${userid}" elename="store" onchange="onChangeStore(this.value,'Patient');getPatientFromBill();" id="store" val="${defStoreVal}" tabindex="7"/>
						</td>
						</c:when>
						<c:otherwise>
						<td><b>${ifn:cleanHtml(store_name)}</b>
						<input type = "hidden" name="store" id="store" value="${ifn:cleanHtmlAttribute(store_id)}" />
						</td>
						</c:otherwise>
						</c:choose>
						<td></td>
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
									<input type="text" name="issueTime" id = "issueTime" style="width:5em"
										value='<fmt:formatDate value="${serverNow}" pattern="HH:mm"/>'/>
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
			</fieldset>

		<div id="patientDetails" style="display:none">
			<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="salesissues.patientissue.addshow.patientdetails"/></legend>
				<table class="patientdetails" cellpadding="0" cellspacing="0" width="100%" border="0">
					<tr>
						<td class="formlabel"><insta:ltext key="ui.label.mrno"/>:</td>
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
							<div id="priPlanname"></div>
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
							<div id="secPlanname"></div>
						</td>
					</tr>


				</table>
			</fieldset>
		</div>
		<fieldset class="fieldSetBorder" id="prescFieldSet" style="display: none">
			<legend class="fieldSetLabel"><insta:ltext key="salesissues.patientissue.addshow.indentdetails"/></legend>
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
			<table id="unavblMedicines"></table>
		</fieldset>
		<table class="formtable">
			<tr>
				<td>
					<input type="radio" name="itemorkit" id="item" checked="checked" style="visibility:hidden; display: none;" value="item" onchange="changeContainer(this.id.'Patient')"/>
				</td>
			</tr>
		</table>
		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="salesissues.patientissue.addshow.itemlist"/></legend>
			<table class="detailList" width="100%" cellspacing="0" cellpadding="0" id="itemListtable" border="0">
				<tr >
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
						<th><insta:ltext key="salesissues.patientissue.addshow.pat.amt"/></th>
						<th><insta:ltext key="salesissues.patientissue.addshow.pri.claim"/></th>
						<th><insta:ltext key="salesissues.patientissue.addshow.sec.claim"/></th>
					</c:if>
					<th></th>
					<th></th>
				</tr>
				<tr id="tableRow1">
					<td><label id="flagImg1"></label>
						<label id="itemLabel1"></label>
						<input type="hidden" name="temp_charge_id" id="temp_charge_id1"/>
						<input type="hidden" name="item_name" id="item_name1"/>
						<input type="hidden" name="storeId" id="storeId1"/>
						<input type="hidden" id="pkgmrp1" name="pkgmrp" value="" />
						<input type="hidden" id="pkgcp1" name="pkgcp" value=""/>
						<input type="hidden" id="medDisc1" name="medDisc" value=""/>
						<input type="hidden" id="taxPer1" name="taxPer" value=""/>
						<input type="hidden" id="pkgUnit1" name="pkgUnit" value=""/>
						<input type="hidden" id="taxType1" name="taxType" value=""/>
						<input type="hidden" id="medDiscRS1" name="medDiscRS" value="" />
						<input type="hidden" id="amt1" name="amt" value="" />
						<input type="hidden" id="category1" name="category" value="" />
						<input type="hidden" id="insurancecategory1" name="insurancecategory" value="" />
						<input type="hidden" id="firstOfCategory1" name="firstOfCategory" value="true" />
						<input type="hidden" id="patIncClaimAmt1" name="patIncClaimAmt" value="" />
						<input type="hidden" id="pri_patIncClaimAmt1" name="patIncClaimAmt" value="" />
						<input type="hidden" id="sec_patIncClaimAmt1" name="sec_patIncClaimAmt" value="" />
						<input type="hidden" id="isMarkUpRate1" name="isMarkUpRate" value="" />
						<input type="hidden" id="originalMRP1" name="originalMRP" value="" />
						<input type="hidden" id="itemUnit1" name="itemUnit" value="" />
						<input type="hidden" name="indent_item_id" id="indent_item_id1"/>
						<input type="hidden" name="patient_indent_no" id="patient_indent_no1"/>
						<input type="hidden" name="issueRateExpr" id="issueRateExpr1" value=""/>
						<input type="hidden" name="visitSellingPriceExpr" id="visitSellingPriceExpr1" value=""/>
						<input type="hidden" name="storeSellingPriceExpr" id="storeSellingPriceExpr1" value=""/>
						<input type="hidden" name="medicineId" id="medicineId1" value=""/>
						<!-- added for insurance exclusions. -->
						<input type="hidden" name="cat_payable" value="t"/>
						<input type="hidden" id="medDiscWithoutInsurance1" name="medDiscWithoutInsurance1" value=""/>
                        <input type="hidden" id="medDiscWithInsurance1" name="medDiscWithInsurance1" value=""/>
					</td>
					<td><label id="controleTypeLabel1"></label></td>
					<td>
						<label id="identifierLabel1"></label>
						<input type="hidden" name="item_identifier" id="item_identifier1"/>
						<input type="hidden" name="exp_dt" id="exp_dt1"/>
						<input type="hidden" name="stype" id="stype1"/>
					</td>
					<td><label id="expdtLabel1"></label></td>
					<td>
						<label id="issue_qtyLabel1" ></label>
						 <input type="hidden" name="issue_qty" id="issue_qty1"/>
					</td>
					<td>
						<label id="uomLabel1" ></label>
					</td>
					<td>
						<label id="pkgSizeLabel1" ></label>
							<input  type="hidden" name="item_billable" id="item_billable_hidden1"/>
							<input  type="hidden" name="package_size" id="package_size1"/>
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
						<label id="totpatamtLabel1"></label>
					</td>
					<td>
						<label id="pri_totinsamtLabel1"></label>
					</td>
					<td>
						<label id="sec_totinsamtLabel1"></label>
					</td>
				</c:if>
					<td><label id="itemRow1"></label>
					<input type="hidden" name="hdeleted" id="hdeleted1"/>
					<input type="hidden" name="mrpHid" id="mrpHid1" />
					<input type="hidden" name="unitMrpHid" id="unitMrpHid1"/>
					<input type="hidden" name="origUnitMrpHid" id="origUnitMrpHid1"/>
					<input type="hidden" name="discountHid" id="discountHid1" />
					<input type="hidden" name="discountAmtHid" id="discountAmtHid1" />
					</td>

					<td>
						<button name="addBut" id="addBut1" onclick="openDialogBox(1); return false;" class="imgButton"
						accesskey="+" title='<insta:ltext key="salesissues.pharmacyestimateslist.list.addnewitem"/>' tabindex="15">
						<img class="button" name="add" id="add1" src="../icons/Add.png"
									style="cursor:pointer;" >
						</button>
					</td>
				</tr>
			 </table>
			 <div class="legend" >
				<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
				<div class="flagText"><insta:ltext key="salesissues.patientissue.addshow.consignmentstock"/></div>
				<div class="flag"><img src='${cpath}/images/purple_flag.gif'></div>
				<div class="flagText"><insta:ltext key="salesissues.sales.details.notcoverdbyinsurance"/></div>
			</div>
			</fieldset>
			 <p></p>
			 <div id="dialog" style="visibility:hidden">
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
						style="display:none;word-wrap:break-word;table-layout: fixed;padding-top:5px;padding-bottom:5px;color:black;background-color:#F0F0F5;" id="orderKitMissedItemsHeader">	
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
							<td  class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.items"/>:</td>
							<td valign="top" >
									<div id="item_wrapper" style="width: 16em; padding-bottom:0.2em; z-index: 9000;" >
										<input type="text" name="items" id="items" tabindex="25" style="width: 16em"  />
										<div id="item_dropdown" class="scrolForContainer"></div>
									</div>
									<!-- <span id="kithyper" style="display:none;margin-left: 17em;"><a href="#" onclick="kitDetailsList(); return false;"  title="Show Kit details">Kit Details.</a></span>-->
									<c:if test="${prefbarcode ne 'Y'}">
										<input type="hidden" name="barCodeId" id="barCodeId" >
									</c:if>
							</td>
							
						</tr>

						<tr>
							<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.batch"/>&nbsp;<insta:ltext key="salesissues.patientissue.addshow.no.avblqty.exp.dt"/>:</td>
							<td>
								<select name="batch" id="batch" style="width: 200" tabindex="30" onchange="changeItems(this.value,'Patient');" class="dropdown">
								<option value=""><insta:ltext key="salesissues.patientissue.addshow.select"/></option>
								</select>
							</td>
							<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.pkgsize"/>:
							</td>
							<td>
									<input type="text" name="pkg_size" id="pkg_size" disabled="disabled" tabindex="35"/>
									<input type="hidden" name="inventory" id="inventory" value="issue"/>
									<input type="hidden" name="itemBillable" id="itemBillable" value=""/>

									<input type="hidden" id="mrp" name="mrp" value="" />
									<input type="hidden" id="cp" name="cp" value=""/>
									<input type="hidden" id="Disc" name="Disc" value=""/>
									<input type="hidden" id="tax" name="tax" value=""/>
									<input type="hidden" id="Unit" name="Unit" value=""/>
									<input type="hidden" id="unit_mrp" name="unit_mrp" value=""/>
									<input type="hidden" id="txType" name="txType" value=""/>
									<input type="hidden" id="DiscRS" name="DiscRS" value="" />
									<input type="hidden" id="amot" name="amot" value="" />
									<input type="hidden" id="expdt" name="expdt" value="" />
									<input type="hidden" id="categoryId" name="categoryId" value="" />
									<input type="hidden" id="patamt" name="patamt" value="" />
									<input type="hidden" id="patcatamt" name="patcatamt" value="" />
									<input type="hidden" id="patper" name="patper" value=""/>
									<input type="hidden" id="patcap" name="patcap" value=""/>
									<input type="hidden" id="insuranceCategoryId" name="insuranceCategoryId" value=""/>
									<input type="hidden" id="isFirstOfCategory" name="isFirstOfCategory" value="true"/>
									<input type="hidden" id="isMarkUp" name="isMarkUp" value=""/>
									<input type="hidden" id="origMRP" name="origMRP" value=""/>
									<input type="hidden" name="issue_base_unit" id="issue_base_unit"/>
									<input type="hidden" id="control_type_id" name="control_type_id" value=""/>
									<input type="hidden" id="control_type_name" name="control_type_name" value=""/>
									<input type="hidden" id="medDiscWithoutInsuranceForm" name="medDiscWithoutInsuranceForm" value=""/>
                                    <input type="hidden" id="medDiscWithInsuranceForm" name="medDiscWithInsuranceForm" value=""/>
                                    <input type="hidden" id="priCatPayable" name="priCatPayable" value=""/>
                                    <input type="hidden" id="item_batch_id" name="item_batch_id" value =""/>
                                    <input type="hidden" id="issue_rate_expr" name="issue_rate_expr" value =""/>
                                    <input type="hidden" id="visit_selling_expr" name="visit_selling_expr" value =""/>
                                    <input type="hidden" id="store_selling_expr" name="store_selling_expr" value =""/>
                                    <input type="hidden" id="oldMRP" name="oldMRP" value=""/>
                                    <input type="hidden" id="medicine_id" name="medicine_id" value=""/>
							</td>
						</tr>
						<c:choose>
							<c:when test="${showCharges == 'A'}">
								<tr>
									<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.issuerate"/>:</td>
									<td class="forminfo" style="width:7em">
										<c:choose>
											<c:when test="${allowRateChange == 'A'}">
												<input type="text" name="itemMrp" id="itemMrp" class="number" tabindex="40" onChange = "calUnitMrp();" onkeypress="return enterNumOnlyANDdot(event);"/>
											</c:when>
											<c:otherwise>
												<input type="text" name="itemMrp" id="itemMrp" class="number" readonly/>
											</c:otherwise>
										</c:choose>
										<input type="hidden" name="origRate" id="origRate"/>
									</td>
									<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.unit"/>&nbsp;<insta:ltext key="salesissues.patientissue.addshow.rate"/>:</td>
									<td class="forminfo" style="width:7em">
										<input type="text" name="unitMrp" id="unitMrp" class="number" readonly/>
									</td>
								</tr>
							</c:when>
							<c:otherwise>
									<input type="hidden" name="itemMrp" id="itemMrp" />
									<input type="hidden" name="unitMrp" id="unitMrp"/>
									<input type="hidden" name="discount" id="discount" value="0"/>
									<input type="hidden" name="origRate" id="origRate"/>
							</c:otherwise>
						</c:choose>
						<tr>
							<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.issueqty"/>:</td>
							<td style="width:7em">
								<input type="text" name="issuQty" id="issuQty" style="text-align:right;width: 50" class="num" maxlength="6"
								tabindex="45"  onkeypress="return onKeyPressAddQty(event)" onchange="getIssueItemPriceUsingExpr(null)" />

								<select name="item_unit" id="item_unit" tabindex="50"></select>
							</td>
						</tr>
						<tr id="coverdbyinsurancestatusid">
							<td class="formLabel">
								<insta:ltext key="salesissues.sales.details.coverdbyinsurance"/>:
							</td>
							<td class="formInfo" colspan="2">
								<b><label id="coverdbyinsurance" ></label></b>
								<input type="hidden" name="coverdbyinsuranceflag" value="" id="coverdbyinsuranceflag">
							</td>
							
						</tr>
						<tr>
							<c:choose>
								<c:when test="${showCharges == 'A'}">
								<td class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.discount.percentage.in.brackets"/>:</td>
										<td class="forminfo" style="width:7em">
											<c:choose>
											<c:when test="${allowDiscount == 'A'}">
											<input type="text" name="discount" id="discount" class="number" value="0" tabindex="55" onkeypress="return enterNumOnlyANDdot(event);" onChange="onChangeDiscount(this);"/>
											</c:when>
											<c:otherwise>
											<input type="text" name="discount" id="discount" class="number" value="0" readonly />
											</c:otherwise>
											</c:choose>
								</td>
								</c:when>
								<c:otherwise>
									<td></td>
									<td></td>
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
			</div>


		<table id="table_b">
			<tr>
			<td >
				<div id="creditbill" style="display: none;" >
					<label ><insta:ltext key="salesissues.patientissue.addshow.bills"/> :</label>
					<select name="bill_no" id="bill_no" class="dropdown" onchange="onChangeBill()" tabindex="75">
					<option value="C"></option>
					</select>
					<c:if test="${showCharges == 'A'}">
					&nbsp;&nbsp;<label class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.totalamount"/>:&nbsp;&nbsp;</label>
					<label id="totAmt" class="forminfo">0.00</label>
					&nbsp;&nbsp;<label class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.patientportion"/>:&nbsp;&nbsp;</label>
					<label id="totPatAmt" class="forminfo">0.00</label>
					&nbsp;&nbsp;<label class="formlabel"><insta:ltext key="salesissues.patientissue.addshow.claimamount"/>:&nbsp;&nbsp;</label>
					<label id="totClaimAmt" class="forminfo">0.00</label>
					</c:if>
				</div>
			</td>
			</tr>
		</table>
		<div class="screenActions">
			<button id="save" type="button" accesskey="S" class="button" tabindex="80" onclick="return submitForm(this,'Patient');" ><b><u><insta:ltext key="salesissues.patientissue.addshow.s"/></u></b><insta:ltext key="salesissues.patientissue.addshow.ave"/></button>
			<button type="reset" accesskey="R" class="button" tabindex="85" onclick="clearPatientDetails();onChangeStore(store.value,'Patient');"><b><u><insta:ltext key="salesissues.patientissue.addshow.r"/></u></b><insta:ltext key="salesissues.patientissue.addshow.eset"/></button>
			<c:if test="${fromOTScreen == 'Y'}">
				| <a href="${cpath}/otservices/OtManagement.do?_method=getOtManagementScreen&operation_details_id=${ifn:cleanURL(operation_details_id)}&visit_id=${ifn:cleanURL(visitIdForOT)}">
				<insta:ltext key="salesissues.patientissue.addshow.otmanagement"/>
				</a>
			</c:if>
		</div>
	</form>
	</div>

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


	<script type="text/javascript">
	var hospuserlist = ${hospuserlist};
	//var kitlist = ${kitlist};
	var stock_negative_sale = '${stock_negative_sale}';
	var cpath = '${pageContext.request.contextPath}';
	var groupItemDetails = ${groupItemDetails};
	var groupStoreId  = '${ifn:cleanJavaScript(groupStoreId)}';
	var grpStoreItem_unit = '${grpStoreItem_unit}';
	var type = '${type}';
	var deptId = '${ifn:cleanJavaScript(store_id)}';
	var gRoleId = '${roleId}';
	var accessstores = '${multiStoreAccess}';
	var orgDetails = <%= request.getAttribute("orgDiscounts")%>;
	var gMarginAmt = '${sale_margin}';
	var visitIdFromBill = '${ifn:cleanJavaScript(visitIdFromBill)}';
	var billNo = '${ifn:cleanJavaScript(billNo)}';
	var storeMedicineAjaxUrlParamQueryStr = '&hosp=${ifn:cleanURL(sesHospitalId)}&issueType=CR&includeConsignmentStock=Y';
	</script>
	<c:if test="${not empty defStoreVal}">
		<script src="${cpath}/pages/stores/getMedicinesInStock.do?ts=${stock_timestamp}&hosp=${ifn:cleanURL(sesHospitalId)}&issueType=CR&includeConsignmentStock=Y&storeId=${ifn:cleanURL(defStoreVal)}">
		</script>
	</c:if>
</body>
</html>

