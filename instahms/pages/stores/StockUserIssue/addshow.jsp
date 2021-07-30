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
<html>
<c:set var="prefbarcode" value="<%= GenericPreferencesDAO.getGenericPreferences().getBarcodeForItem() %>" />
<c:set var="allowDecimalsForQty" value='<%=GenericPreferencesDAO.getAllPrefs().get("allow_decimals_in_qty_for_issues")%>' />
<head>
<c:set var="issuetodeptonly"
	value='<%= GenericPreferencesDAO.getAllPrefs().get("issue_to_dept_only") %>'
	scope="request" />
<c:if test="${issuetodeptonly == 'N'}">
<title><insta:ltext key="salesissues.stockuserissue.details.title"/></title>
</c:if>
<c:if test="${issuetodeptonly == 'Y'}">
<title><insta:ltext key="salesissues.stockuserissue.details.titledept"/></title>
</c:if>

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

</style>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="i18nSupport" content="true"/>
	<insta:link type="js" file="ajax.js" />
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="stores/storeshelper.js" />
	<insta:link type="script" file="stores/storescommon.js" />
	<insta:link type="script" file="stores/stockuserissue.js" />
	<insta:link type="js" file="stores/storeMedicinesAjax.js" />
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
</style>
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
var gAllowExpiredSale = '${prefs.saleOfExpiredItems}';
var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
var deptId = '${ifn:cleanJavaScript(store_id)}';
var gRoleId = '${roleId}';
var accessstores = '${multiStoreAccess}';
var prefBarCode = '${prefbarcode}';
var allowDecimalsForQty = '${allowDecimalsForQty}';
var hdrugAlertNeeded = '${prefs.pharmacy_schedule_h_alert}';
var issuetodept = '${issuetodeptonly}';
var gExpiryWarnDays = '${prefs.warn_expiry}';
</script>
<insta:js-bundle prefix="sales.issues"/>
</head>



<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:choose>
	<c:when test="${not empty groupStoreId}">
		<c:set var="defStoreVal" value="${groupStoreId}"/>
	</c:when>
	<c:otherwise>
		<c:set var="defStoreVal" value="${store_id}"/>
	</c:otherwise>
</c:choose>
<body onload=" init();AddGroupItemDetails('Hospital');enable(document.forms[0].hosp_user,'Hospital'); getReport('${ifn:cleanJavaScript(message)}','${ifn:cleanJavaScript(gtpass)}','${hospital}','User');checkstoreallocation();" class="yui-skin-sam" >


<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>

<div id="storecheck" style="display: block;" >
<div ID="kitdetails" class="kit">
<marquee><b><span style="background-color: #ADCFAD;"><insta:ltext key="salesissues.stockuserissue.details.kitdetails"/></span></b></marquee>
<fieldset class="fieldSetBorder" >
	<legend class="fieldSetLabel"><insta:ltext key="salesissues.stockuserissue.details.kititemslist"/></legend>
	<div style="overflow:auto;max-height:15em"  align="center">
	<table class="dashboard" width="100%" cellspacing="0" cellpadding="0" id="table1">
		<tr bgcolor="#8FBC8F">
        <th><insta:ltext key="salesissues.stockuserissue.details.category"/></th>
        <th><insta:ltext key="salesissues.stockuserissue.details.itemname"/></th>
        <th><insta:ltext key="salesissues.stockuserissue.details.billable"/></th>
        <th><insta:ltext key="salesissues.stockuserissue.details.qty"/></th>
        </tr>
        </table></div></fieldset>
   <span style="text-align: center"><a href="#" onClick="toggleBox('kitdetails',0); return false"  title='<insta:ltext key="salesissues.stockuserissue.details.hidethisbox"/>'><insta:ltext key="salesissues.stockuserissue.details.hidethisbox"/></a></span>

</div>
<div ID="nokit" class="nokit">
   <table>
   <tr><td>
   <insta:ltext key="salesissues.stockuserissue.details.nokitdetails"/></td></tr>
   <tr><td align="center">
   <a href="#" onClick="toggleBox('nokit',0); return false"  title='<insta:ltext key="salesissues.stockuserissue.details.hidethisbox"/>'><insta:ltext key="salesissues.stockuserissue.details.hidethisbox"/></a>
   </td></tr></table>
</div>
 <form method="POST" action="StockUserIssue.do" name="stockissueform" autocomplete="off">
    	<input type="hidden" name="_method" value="saveItemsIssued">
    	<input type="hidden" name="saleType" value="${ifn:cleanHtmlAttribute(saleType)}">
    	<input type="hidden" name="stocktype" id="stocktype" value=""/>
    	<input type="hidden" id="dialogId" value=""/>
    	<input type="hidden" name="issued_to" id="issued_to"/>
    	<c:if test="${issuetodeptonly == 'Y'}">
    	<h1><insta:ltext key="salesissues.stockuserissue.details.stockdepartmentissue"/></h1>
		</c:if>
		<c:if test="${issuetodeptonly == 'N'}">
		<h1><insta:ltext key="salesissues.stockuserissue.details.stockuserissue"/></h1>
		</c:if>
    	<div >
			<c:choose>
				<c:when test="${flag == false }">
					<span class="resultMessage">${ifn:cleanHtmlAttribute(msg)}</span>
				</c:when>
				<c:when test="${message == 0 }">
				<span class="resultMessage"></span>
				</c:when>
		    	<c:when test="${ message != 0 && gtpass eq true}" >
				<span class="resultMessage"><insta:ltext key="salesissues.stockuserissue.details.itemsissued"/></span>
				</c:when>
				<c:when test="${message == null }" >
				<span class="resultMessage"><insta:ltext key="salesissues.stockuserissue.details.transactionfailure"/></span>
				</c:when >

			</c:choose>
	    </div></br>
	    <insta:feedback-panel/>
			<c:if test="${issuetodeptonly == 'N'}">
				<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="salesissues.stockuserissue.details.userissuedetails"/></legend>
				</c:if>
				<c:if test="${issuetodeptonly == 'Y'}">
				<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="salesissues.stockuserissue.details.deptissuedetails"/></legend>
			</c:if>
				<table class="formtable" cellpadding="0" cellspacing="0" width="100%" border="0">
				<tr>
				
					<tr id="userRow">
					<c:choose>
							<c:when test="${issuetodeptonly == 'N'}">
								<td >
									<input type="radio" accesskey="U" name="issueType" id="issueType_user"
									value="u" onclick="onChangeIssueType(this)" checked='checked'/>
									<label for="issueType_user">
									<insta:ltext key="salesissues.stockuserissue.details.issue.to"/> <b><u><insta:ltext key="salesissues.stockuserissue.details.u"/></u></b><insta:ltext key="salesissues.stockuserissue.details.ser"/></label>:
								</td>
								<td class="yui-skin-sam" valign="top">
									<div  id="hosp_user_wrapper">
										<div id="psAutocomplete" style="display: block; float: left; width: 210px">
											<input type="text" name="hosp_user" id="hosp_user" style="width: 200px"/>
											<div id="hosp_user_dropdown" class="scrollingDropDown" style="width: 250px;"></div>
										</div>
										<span id="hosp_user_mand" style="display:block;" class="star">*</span>
									</div>
								</td>
							</c:when>
							<c:otherwise>
								<td >
								<input type="radio" name="issueType" accesskey="D" id="issueType_dept"
									checked='checked' value="d" onclick="onChangeIssueType(this)"/>
								<label for="issueType_dept"><insta:ltext key="salesissues.stockuserissue.details.issue.to"/> <b><u><insta:ltext key="salesissues.stockuserissue.details.d"/></u></b><insta:ltext key="salesissues.stockuserissue.details.dept"/></label>:
								</td>
								<td>
									<insta:selectdb id="issue_dept" name="issue_dept" table="department" displaycol="dept_name"
										valuecol="dept_name" value="${hosp_user}" dummyvalue="${dummyvalue}" style="color:black;" 
										maxlength="25"/><span id="issue_dept_mand" class="star">*</span>
								</td>
							
							</c:otherwise>
						</c:choose>
						<td class="formlabel"><insta:ltext key="salesissues.stockuserissue.details.store"/>:</td>
                        <c:choose>
							<c:when test="${(roleId == 1) || (roleId == 2) || (multiStoreAccess == 'A') }">
								<td>
								<insta:userstores username="${userid}" elename="store" onchange="onChangeStore(this.value,'Hospital');" id="store" val="${defStoreVal}"/>
								</td>
							</c:when>
							<c:otherwise>
								<td>
									<b>${ifn:cleanHtmlAttribute(store_name)}</b>
									<input type = "hidden" name="store" id="store" value="${ifn:cleanHtmlAttribute(store_id)}" />
								</td>
							</c:otherwise>
						</c:choose>
						<c:if test="${issuetodeptonly == 'N' }">
							<tr>
								<td >
									<input type="radio" name="issueType" accesskey="D" id="issueType_dept"
									value="d" onclick="onChangeIssueType(this)"/>
									<label for="issueType_dept"><insta:ltext key="salesissues.stockuserissue.details.issue.to"/> <b><u><insta:ltext key="salesissues.stockuserissue.details.d"/></u></b><insta:ltext key="salesissues.stockuserissue.details.dept"/></label>:
								</td>
								<td >
									<insta:selectdb id="issue_dept" name="issue_dept" table="department" displaycol="dept_name"
										valuecol="dept_name" value="${hosp_user}" dummyvalue="${dummyvalue}" disabled="disabled" style="color:black;" maxlength="25"/>
										<span id="issue_dept_mand"  style="visibility:hidden;" class="star">*</span>
								</td>
								<td class="formlabel" colspan="2">&nbsp;</td>
							</tr>
						</c:if>
						<tr>
							<td >
								<input type="radio" name="issueType" accesskey="W" id="issueType_ward"
								value="w" onclick="onChangeIssueType(this)"/>
								<label for="issueType_ward"><insta:ltext key="salesissues.stockuserissue.details.issue.to"/> <b><u><insta:ltext key="salesissues.stockuserissue.details.w"/></u></b><insta:ltext key="salesissues.stockuserissue.details.ard"/></label>:
							</td>
							<td>
								<insta:selectdb id="issue_ward" name="issue_ward" table="ward_names" displaycol="ward_name" disabled="disabled"
									valuecol="ward_name" value="" dummyvalue="--Select--"  style="color:black;" maxlength="25"/>
								<span id="issue_ward_mand" style="visibility:hidden" class="star">*</span>
							</td>
							<td class="formlabel"><insta:ltext key="salesissues.stockuserissue.details.issuereason"/>:</td>
							<td>
								<input type="text" name="reason" id="reason" maxlength="30"  onblur="upperCase(reason)">
							</td>
						</tr>
						<tr>
						<td colspan="2">&nbsp;</td>
						<c:choose>
							<c:when test="${(roleId == 1) || (roleId == 2) || (actionRightsMap.allow_backdate == 'A')}">
								<td class="formlabel"><insta:ltext key="salesissues.stockuserissue.details.date"/></td>
								<td><insta:datewidget name="issueDate" valid="past" value="today" btnPos="left" /></td>
							</c:when>
							<c:otherwise>
								<td></td>
							</c:otherwise>
						</c:choose>
						<td>
							<input type="checkbox" name="gatepass" id="gatepass" ><insta:ltext key="salesissues.stockuserissue.details.gatepass"/>
						</td>
					</tr>
					<tr>
					</tr>
				</table>
			</fieldset>
		<div style =  "display : none">
			<table class="formtable">
				<tr>
	 				<td>
						<input type="radio" name="itemorkit" id="item" checked="checked" value="item" onchange="changeContainer(this.id.'Hospital')"/><insta:ltext key="salesissues.stockuserissue.details.items"/>
					</td>

	<!-- 				<td>
						<input type="radio" name="itemorkit" id="kits" value="kit" onchange="changeContainer(this.id)"/>
					</td>
					<td>
						<label>Kits</label>
					</td>
	 -->
				</tr>
			</table>
		</div>
		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="salesissues.stockuserissue.details.itemlist"/></legend>
			<table class="dataTable" width="100%" cellspacing="0" cellpadding="0" id="itemListtable" border="0">
				<tr >
					<th><insta:ltext key="salesissues.stockuserissue.details.itemname"/></th>
					<th>&nbsp;</th>
					<th><insta:ltext key="salesissues.stockuserissue.details.batch.or.serial.no"/></th>
					<th><insta:ltext key="salesissues.stockuserissue.details.exp.date"/></th>
					<th><insta:ltext key="salesissues.stockuserissue.details.issueqty"/></th>
					<th><insta:ltext key="salesissues.stockuserissue.details.issueuom"/></th>
					<th><insta:ltext key="salesissues.stockuserissue.details.pkgsize"/></th>
					<th>&nbsp;</th>
					<th>&nbsp;</th>
				</tr>
				<tr id="tableRow1">
					<td>
						<label id="flagImg1"></label>
						<label id="itemLabel1"></label>
						<input type="hidden" name="item_name" id="item_name1"/>
						<input type="hidden" name="temp_charge_id" id="temp_charge_id1"/>
						<input type="hidden" name="storeId" id="storeId1"/>
						<input type="hidden" id="itemUnit1" name="itemUnit" value="" />
						<input type="hidden" id="pkgUnit1" name="pkgUnit" value=""/>
						<input type="hidden" id="itemBatchId1" name="itemBatchId"/>
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
						<label id="issuetypeLabel1"></label>
						<label id="pkgSizeLabel1" ></label>
						<input  type="hidden" name="package_size" id="package_size1"/>
					</td>
					<td>
						<label id="itemRow1"></label>
						<input type="hidden" name="hdeleted" id="hdeleted1"/>
					</td>
					<td>
						<button name="addBut" id="addBut1" onclick="openDialogBox(1); return false;" class="imgButton" accesskey="+" title='<insta:ltext key="salesissues.stockuserissue.details.addnewitem"/>'>
						<img class="button" name="add" id="add1" src="../icons/Add.png"
									style="cursor:pointer;" >
						</button>
					</td>
				</tr>
			 </table>
			 <div class="legend" >
				<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
				<div class="flagText"><insta:ltext key="salesissues.stockuserissue.details.consignmentstock"/></div>
			</div>
			</fieldset>
			 <p></p>
			 <div id="dialog" style="visibility:hidden">
				<div class="bd">
					<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel"><insta:ltext key="salesissues.stockuserissue.details.additem"/></legend>
					<table class="formtable">

							<c:if test="${prefbarcode eq 'Y'}">
							<tr>
								<td class="formlabel"><insta:ltext key="salesissues.stockuserissue.details.itembarcode"/>: </td>
								<td ><input type="text" name="barCodeId" id="barCodeId" onchange="getItemBarCodeDetails(this.value);" tabindex="3" ></td>
							</tr>
							</c:if>
							<tr>
							<td class="formlabel"><insta:ltext key="salesissues.stockuserissue.details.items"/>:</td>
							<td valign="top">
								<div id="item_wrapper" style="width: 17em; padding-bottom:0.2em; z-index: 9000;">
									<input type="text" name="items" id="items" tabindex="4" style="width: 16em"  />
									<div id="item_dropdown"style="width: 420px;"></div>
								</div>
								<span id="kithyper" style="display:none;margin-left: 17em;"><a href="#" onclick="kitDetailsList(); return false;"  title='<insta:ltext key="salesissues.stockuserissue.details.showkitdetails"/>'><insta:ltext key="salesissues.stockuserissue.details.kitdetails1"/></a></span>
								<c:if test="${prefbarcode ne 'Y'}">
									<input type="hidden" name="barCodeId" id="barCodeId" >
								</c:if>
							</td>
							</tr>

							<tr>
							<td class="formlabel"><insta:ltext key="salesissues.stockuserissue.details.batch"/>&nbsp;<insta:ltext key="salesissues.stockuserissue.details.no.or.avblqty.or.expdt"/>:</td>
							<td>
								<select name="batch" id="batch" style="width: 200" tabindex="5" onchange="changeItems(this.value,'Hospital');" class="dropdown">
								<option value=""><insta:ltext key="salesissues.stockuserissue.details.select"/></option>
								</select>
							</td>
							</tr>
							<tr>
							<td class="formlabel"><insta:ltext key="salesissues.stockuserissue.details.issueqty"/>:</td>
							<td>
								<input type="text" name="issuQty" id="issuQty" style="text-align:right;"class="num" maxlength="8" size="4" tabindex="6"  onkeypress="return onKeyPressAddQty(event)"/ >
								<select name="item_unit" id="item_unit" tabindex="7"></select>
							</td>
							</tr>
							<tr>
							<td class="formlabel"><insta:ltext key="salesissues.stockuserissue.details.pkgsize"/>:</td>
							<td>
								<input type="text" name="pkg_size" id="pkg_size" disabled="disabled" tabindex="8"/>
								<input type="hidden" name="inventory" id="inventory" value="issue"/>
								<input type="hidden" name="itemBillable" id="itemBillable" value=""/>
								<input type="hidden" id="expdt" name="expdt" value="" />
								<input type="hidden" id="Unit" name="Unit" value=""/>
								<input type="hidden" name="issue_base_unit"/>
								<input type="hidden" id="control_type_id" name="control_type_id" value=""/>
								<input type="hidden" id="control_type_name" name="control_type_name" value=""/>
							</td>
							</tr>

						<tr><td>&nbsp;</td></tr>
						<tr>
							<td>
								<button type="button" id="OK" name="OK" accesskey="A"  style="display: inline;" class="button" onclick="addItemsToTable();"tabindex="9"><label><b><insta:ltext key="salesissues.stockuserissue.details.ok"/></b></label></button>
								<button type="button" id="Cancel" name="Cancel" accesskey="A"  style="display: inline;" class="button" onclick="handleCancel();" tabindex="10" ><label><b><insta:ltext key="salesissues.stockuserissue.details.cancel"/></b></label></button>
							</td>
						</tr>
					</table>
					<table id="deletedrow"></table>
					</fieldset>
				</div>
			</div>

		<table><tr><td align="center">
			<div id="creditbill" style="display: none;" >
				<label ><insta:ltext key="salesissues.stockuserissue.details.billlater"/></label>
				<select name="bill_no" id="bill_no" class="dropdown">
				<option value="C"></option>
				</select>
			</div></td></tr>
		</table>
		<div class="screenActions">
			<button id="save" type="button" accesskey="S" class="button" tabindex="11" onclick="return submitForm(this,'Hospital');" ><b><u><insta:ltext key="salesissues.stockuserissue.details.s"/></u></b><insta:ltext key="salesissues.stockuserissue.details.ave"/></button>
			<button type="button" accesskey="R" name="refresh" id="refresh"  tabindex="12" onclick="refreshForm();"><b><u><insta:ltext key="salesissues.stockuserissue.details.r"/></u></b><insta:ltext key="salesissues.stockuserissue.details.eset"/></button>
		</div>
	</form>
</div>
	<script type="text/javascript">
	var hospuserlist = ${hospuserlist};
	var kitlist = null;
	var stock_negative_sale = '${stock_negative_sale}';
	var cpath = '${pageContext.request.contextPath}';
	var groupItemDetails = ${groupItemDetails};
	var groupStoreId  = '${ifn:cleanJavaScript(groupStoreId)}';
	var grpStoreItem_unit = '${grpStoreItem_unit}';
	var type = '${type}';
	var showCharges = 'N';
	var storeMedicineAjaxUrlParamQueryStr = '&hosp=${ifn:cleanURL(sesHospitalId)}&issueType=CPL';
	</script>
	<c:if test="${not empty defStoreVal}">
		<script src="${cpath}/pages/stores/getMedicinesInStock.do?ts=${stock_timestamp}&hosp=${ifn:cleanURL(sesHospitalId)}&issueType=CPL&storeId=${ifn:cleanURL(defStoreVal)}"></script>
	</c:if>
</body>
</html>

