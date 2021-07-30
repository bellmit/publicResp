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
<c:set  var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<c:set var="prefbarcode" value="<%= GenericPreferencesDAO.getGenericPreferences().getBarcodeForItem() %>" />
<c:set var="prefDecimalQty" value="<%= GenericPreferencesDAO.getGenericPreferences().getAllowDecimalsInQtyForIssues() %>"/>
<head>
<c:set var="stockTransfertext">
	<insta:ltext key="storemgmt.pharmacyindentlist.list.stocktransfer"/>
</c:set>
<title>${kit_details_json != null ? 'Kit Issue' :  ( param.is_sterile_store == 'N' ? 'Sterile Stock Transfer' : stockTransfertext )}-<insta:ltext key="storemgmt.stocktransfer.addshow.title"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="i18nSupport" content="true"/>
	<insta:link type="js" file="ajax.js" />
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="stores/storeMedicinesAjax.js" />
	<insta:link type="js" file="stores/stocktransfer.js"/>
	<insta:link type="js" file="stores/storescommon.js"/>
	<insta:link type="js" file="stores/kit_issue.js"/>

<style>
 .cstk{background-color:#C0CFAF;}

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
</style>
<script type="text/javascript">
	var allowBackDate = '${actionRightsMap.allow_backdate}';
	var prefBarCode = '${prefbarcode}';
	var qtyDecimal = '${prefDecimalQty}';
	var kitIssue = ${kit_details_json != null };
	var kitDetails = '';
	<c:if test="${kit_details_json != null }">
		kitDetails = ${kit_details_json};
	</c:if>
	var sterileTransfer = ${param.is_sterile_store != null && param.is_sterile_store == 'N'};
	var storeMedicineAjaxUrlParamQueryStr = '&hosp=${ifn:cleanURL(sesHospitalId)}&includeConsignmentStock=Y';
	var orderKitJSON = ${orderkitJSON};
</script>

<!-- <script src="${cpath}/pages/stores/getMedicinesInStock.do?ts=${stock_timestamp}&hosp=${ifn:cleanURL(sesHospitalId)}&includeConsignmentStock=Y"></script> -->
<insta:js-bundle prefix="stores.mgmt"/>
<insta:js-bundle prefix="sales.issues"/>
</head>

<c:choose>
	<c:when test="${not empty groupStoreId}">
		<c:set var="defStoreVal" value="${groupStoreId}"/>
	</c:when>
	<c:otherwise>
		<c:set var="defStoreVal" value="${dept_id}"/>
	</c:otherwise>
</c:choose>
<body onload="init();" class="yui-skin-sam">
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<div id="storecheck" style="display: block;" >
<div><insta:feedback-panel/></div>
<h1>${kit_details_json != null || param.kitissue ? 'Kit Issue' : ( param.is_sterile_store == 'N' ? 'Sterile Stock Transfer' :stockTransfertext ) }</h1>
 <form method="POST" action="stocktransfer.do" name="stocktransferform" autocomplete="off">
 		<input type="hidden" name="appointment_id" value="${ifn:cleanHtmlAttribute(param.appointment_id)}"/>
    	<input type="hidden" name="itemtype" id="itemtype" value=""/>
    	<input type="hidden" name="_method" value="create">
    	<input type="hidden" name="kitissue" value="${kit_details_json != null}"/>
    	<input type="hidden" name="stocktype" id="stocktype" value=""/>
    	<input type="hidden" id="dialogId" value=""/>
    	<input type="hidden" id="sterile_stock_transfer" value="${param.is_sterile_store == 'N' }"/>
			<div id="result_msg" style="display: block;">
			<c:choose>
		    	<c:when test="${message != 'show'}" >
				<span class="resultMessage"><insta:ltext key="storemgmt.stocktransfer.addshow.transferno"/> <a href="${pageContext.request.contextPath}/pages/stores/stocktransfer.do?_method=getStockTransferPrint&transfer_no=${ifn:cleanURL(message)}" target="_blank"> ${ifn:cleanHtml(message)} </a> <insta:ltext key="storemgmt.stocktransfer.addshow.generated.stocktransfer"/></span>
				</c:when>
			</c:choose>
			</div>
			<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="storemgmt.stocktransfer.addshow.storedetails"/></legend>
				<table class="formtable" cellpadding="0" cellspacing="0" width="100%" border="0">
					<tr><td class="formlabel"><insta:ltext key="storemgmt.stocktransfer.addshow.fromstore"/>:</td>
					   <c:choose>
						   <c:when test="${(multiStoreAccess eq 'A' || roleId eq 1 || roleId eq 2 )}">
								<td>
									<insta:userstores username="${userid}" elename="store" id="store" onchange="onChangeStore(this.value,'none');"
										val="${defStoreVal}" tabindex="5" sterileStores="${param.is_sterile_store}"/>
								</td>
							</c:when>
							<c:otherwise>
								<td>
									<b>
										<insta:getStoreName store_id="${pharmacyStoreId}"/>
									</b>
								</td>
								<input type="hidden" name="store" id="store" value="${pharmacyStoreId}" />
							</c:otherwise>
						</c:choose>
						<td class="formlabel"><insta:ltext key="storemgmt.stocktransfer.addshow.tostore"/>:</td>
						<td>
							<c:choose>
								<c:when test="${param.ot_store != null}">
									<insta:getStoreName store_id="${param.ot_store}"/>
									<input type="hidden" name="to_store" id="to_store" value="${ifn:cleanHtmlAttribute(param.ot_store)}" />
								</c:when>
								<c:otherwise>
								<c:choose>
									<c:when test="${to_sterile_store}">
										<insta:selectdb name="to_store" id="to_store" table="stores" valuecol="dept_id" filtercol="is_sterile_store" filtervalue="Y" displaycol="dept_name" dummyvalue="-- Select --" value=""
											orderby="dept_name" onchange="validateToStore(store.value,this.value)" tabindex="10"/><span class="star">*</span>
											<input type="hidden" name="to_sterile_store" value="true"/>
									</c:when>
									<c:otherwise>
										<insta:selectdb name="to_store" id="to_store" table="stores" valuecol="dept_id" displaycol="dept_name" dummyvalue="${dummyvalue}" value=""
											orderby="dept_name" onchange="validateToStore(store.value,this.value)" tabindex="10"/><span class="star">*</span>
									</c:otherwise>
								</c:choose>

								</c:otherwise>
							</c:choose>
						</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="storemgmt.stocktransfer.addshow.transferreason"/>:</td>
						<td><input type="text" name="reason" id="reason" maxlength="30" tabindex="15" ><span class="star">*</span></td>
						<td class="formlabel"><insta:ltext key="storemgmt.stocktransfer.addshow.transferdate"/>:</td>
						<td><insta:datewidget name="transferDate"  valid="past" value="today" btnPos="left" required="true" tabindex="20"
							calButton="${actionRightsMap.allow_backdate eq 'A' || empty actionRightsMap.allow_backdate}"/></td>
					</tr>
					<tr>
						<td class="formlabel">GRN :</td>
						<td colspan>
							<input type="text" name="grn_no" id="grn_no" size="8"/>
							<button type="button" id="get_grn" name="loadgrn" accesskey="g" style="display: inline;" class="button" onclick="getGrnDetails();"><label><b>Load GRN</b></label></button>
						</td>
						<td class="formlabel"><insta:ltext key="storemgmt.stocktransfer.addshow.disallowexpired"/>:</td>
						<td><input type="checkbox" name="disallow_expired" ${ifn:cleanHtmlAttribute(param.disallow_expired)} checked  onclick="onChangeDisallowExpired(this)"/></td>
					</tr>
				</table>
			</fieldset>


		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="storemgmt.stocktransfer.addshow.itemlist"/></legend>
			<table class="dataTable" width="100%" cellspacing="0" cellpadding="0" id="itemListtable" >
				<tr >
					<th><insta:ltext key="storemgmt.stocktransfer.addshow.itemname"/></th>
					<th><insta:ltext key="storemgmt.stocktransfer.addshow.batch.serialno"/></th>
					<th><insta:ltext key="storemgmt.stocktransfer.addshow.exp.date"/></th>
					<th><insta:ltext key="storemgmt.stocktransfer.addshow.packagetype"/></th>
					<th><insta:ltext key="storemgmt.stocktransfer.addshow.transferqty"/></th>
					<th><insta:ltext key="storemgmt.stocktransfer.addshow.transferuom"/></th>
					<th><insta:ltext key="storemgmt.stocktransfer.addshow.pkgsize"/></th>
					<th><insta:ltext key="storemgmt.stocktransfer.addshow.description"/></th>
					<th></th>
					<th></th>
				</tr>
				<tr id="tableRow1">
					<td>
						<label id="itemLabel1"></label>
						<input type="hidden" name="item_id" id="item_id1"/>
						<input type="hidden" name="item_name" id="item_name1"/>
						<input type="hidden" name="from_store" id="from_store1"/>
						<input type="hidden" name="tranfer_store" id="tranfer_store1"/>
					</td>
					<td>
						<label id="identifierLabel1"></label>
						<input type="hidden" name="itemidentifier" id="itemidentifier1"/>
						<input type="hidden" name="expdt" id="expdt1"/>
						<input type="hidden" name="hmrp" id="hmrp1"/>
						<input type="hidden" name="stk_type" id="stk_type1"/>
						<input type="hidden" name="trannsfer_qty" id="trannsfer_qty1"/>
						<input type="hidden" name="itemidentification" id="itemidentification1"/>
						<input type="hidden" name="itemUnit" id="itemUnit1"/>
						<input type="hidden" name="pkgSize" id="pkgSize1"/>
						<input type="hidden" name="item_batch_id" id="item_batch_id1"/>
						<input type="hidden" name="description" id="description1"/>
					</td>
					<td><label id="expdtLabel1"></label></td>
					<td><label id="packagetypeLabel1"></label></td>
					<td><label id="transferqtyLabel1"></label></td>
					<td><label id="itemUOM1"></label></td>
					<td><label id="pkgSizeLabel1"></label></td>
					<td><label id="descriptionLabel1"></label></td>
					<td>
						<label id="itemRow1"></label>
						<input type="hidden" name="hdeleted" id="hdeleted1"/>
					</td>

					<td>
						<button type="button" id="addBut1" class="imgButton" accesskey="+" title='<insta:ltext key="storemgmt.stocktransfer.addshow.addnewitem"/>' tabindex="25">
							<img class="button" name="add" id="add1" src="../../icons/Add.png"	style="cursor:pointer;" >
						</button>
						<input type="hidden" name="isEdit" id="isEdit1" value='N'/>
					</td>

				</tr>
			 </table>
		 </fieldset>
		 <div class="legend">
			 <div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			 <div class="flagText" ><insta:ltext key="storemgmt.stocktransfer.addshow.consignmentstock"/></div>
		</div>
		 <div id="dialog" style="visibility:hidden">
			<div class="bd">
				<!-- Start Added For order kit -->
				<table class="formtable">	
					<tr>
						<td align="left" style="width: 100px;">
							<input type="radio" name="item_type" id="itemname" value="itemname" onclick="onChangeItemType()"/>
							<label for="item">Items</label>
						</td>
						<td align="left">
							<input type="radio" name="item_type" id="order" value="order" onclick="onChangeItemType()"/>
							<label for="order">Order Kits</label>
						</td>
					</tr>
				</table>
				<table class="formtable" style="display:none" id="orderKitDetails">
					<tr>
						<td  class="formlabel" style="width: 95px;">Order Kit Name:</td>
						<td valign="top" >
							<div id="orderkit_wrapper" style="width: 20em; padding-bottom:0.2em; z-index: 9000;" >
								<input type="text" name="orderkits" id="orderkits"  tabindex="20" style="width: 38em;" value="" onkeypress="return onKeyPressAddQty(event);"/>
								<div id="orderkit_dropdown" style="width: 38em;"></div>
							</div>
						</td>
					</tr>
				</table>
				<br/>
				<div class="formlabel" id="itemInfo" style="font-size:14px;text-align:left;"></div>
				<table  cellpadding="0" cellspacing="0" width="100%" border="0" 
					style="display:none;word-wrap:break-word;table-layout: fixed;padding-top:5px;padding-bottom:5px;color:black;background-color:#F0F0F5;" id="orderKitMissedItemsHeader">	
					<tr>
						<th style="width:515px;font-family:Arial-BoldMT;text-align: left;padding-left: 13px;padding-top: 8px;color:#666666;font-size:11px;"><b>ITEM NAME</b></th>
						<th style="text-align: left;padding-left: 50px;padding-top: 8px;font-family:Arial-BoldMT;color:#666666;font-size:11px;"><b>QUANTITY</b></th>
					</tr>
				</table>
				<table class="detailList" cellpadding="0" cellspacing="0" border="0" 
					style="display:none;width:100%;height:150px;overflow:auto;table-layout: fixed;border-bottom: 1px #CCCCCC solid;" id="orderKitMissedItems">	
				</table>
				<!-- End orderkit -->
				<fieldset class="fieldSetBorder" id="itemFieldSet" style="display:none">
				<legend class="fieldSetLabel"><insta:ltext key="storemgmt.stocktransfer.addshow.additem"/></legend>
				 <table cellpadding="0" cellspacing="0" width="100px" style="display:none" class="formtable" id="itemDetails">
					<tr>
						<c:choose>
							<c:when test="${prefbarcode eq 'Y'}">
								<td class="formlabel"><insta:ltext key="storemgmt.stocktransfer.addshow.itembarcode"/>: </td>
								<td ><input type="text" name="barCodeId" id="barCodeId" onchange="getItemBarCodeDetails(this.value);" tabindex="25"></td>
						    </c:when>
						    <c:otherwise>
						    	<input type="hidden" name="barCodeId" id="barCodeId" >
						    </c:otherwise>
					    </c:choose>
					    <td class="formlabel"><insta:ltext key="storemgmt.stocktransfer.addshow.item"/>: </td>
						<td style="padding-bottom:20px;">
							<div id="itemsac" style="width: 20em; padding-bottom:0.2em;">
								<input type="text" name="items" id="items" tabindex="30" style="width: 20em">
								<div id="item_dropdown" class="scrolForContainer"></div>
							</div>
						</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="storemgmt.stocktransfer.addshow.batch.serialno"/>/<insta:ltext key="storemgmt.stocktransfer.addshow.expdate"/>: </td>
						<td>
							<select name="batch" id="batch" style="width:20em;" tabindex="35" onchange="changeIdentifiers(this.value);" class="dropdown">
								<option value="">${dummyvalue}</option>
							</select>
							<input type="hidden" name="inventory" id="inventory" value="transfer"/>
							<input type="hidden" name="issuetype" id="issuetype" value=""/>
						</td>
						<td class="formlabel"><insta:ltext key="storemgmt.stocktransfer.addshow.pkgsize"/>: </td>
						<td class="forminfo" >
							<b><label id="pkg_size" ></label></b>
							<input type="hidden" name="issue_base_unit"/>
							<input type="hidden" name="e_item_batch_id"/>
						</td>
						
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="storemgmt.stocktransfer.addshow.mrp"/>: </td>
						<td class="forminfo" style="word-wrap: break-word;word-break: break-all;white-space: normal;"><b><label id="mrp"></label></b></td>
						<td class="formlabel"><insta:ltext key="storemgmt.stocktransfer.addshow.currstock"/>: </td>
						<td class="forminfo"><b><label id="qty_avbl" ></label></b></td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="storemgmt.stocktransfer.addshow.transferqty"/>: </td>
						<td>
							<input type="text" name="qty" id="qty" class="number" maxlength="8" size="4"
									tabindex="45" onkeypress="return onKeyPressAddQty(event);" >
							<span id="issueUnits"></span>
						<select name="item_unit" id="item_unit" onchange="onChangeUOM(this);" tabindex="40"></select></td>
						<td class="formlabel"><insta:ltext key="storemgmt.stocktransfer.addshow.description"/>: </td>
						<td>
							<input type="text" name="e_item_transfer_description" id="e_item_transfer_description"
								onkeypress="return onKeyPressAddQty(event);" tabindex="46" maxlength="100">
						</td>
					</tr>
					<tr>
						<td class="formlabel">Unit Rate: </td>
						<td class="forminfo"><b><label id="unit_rate" ></label></b></td>
					</tr>
				</table>
				</fieldset>
				<table>
					<tr><td>&nbsp;</td></tr>
					<tr>
						<td>
							<button type="button" id="OK" name="OK" accesskey="A"  style="display: inline;" class="button" onclick="addItems();"tabindex="50"><label><b><insta:ltext key="storemgmt.stocktransfer.addshow.ok"/></b></label></button>
							<button type="button" id="Cancel" name="Cancel" accesskey="A"  style="display: inline;" class="button" onclick="handleCancel();"tabindex="55"><label><b><insta:ltext key="storemgmt.stocktransfer.addshow.cancel"/></b></label></button>
						</td>
					</tr>
				</table>
			</div>
		</div>
		<div class="screenActions">
			<button id="save" type="button" accesskey="T" class="button" tabindex="65" onclick="return validate();" >
			<b><u><insta:ltext key="storemgmt.stocktransfer.addshow.t"/></u></b><insta:ltext key="storemgmt.stocktransfer.addshow.ransfer"/></button>
			<button type="button" accesskey="R" class="button" tabindex="70" onclick="onChangeStore(store.value,'none')" >
			<b><u><insta:ltext key="storemgmt.stocktransfer.addshow.r"/></u></b><insta:ltext key="storemgmt.stocktransfer.addshow.eset"/></button>
		</div>
	</form>
	</div>
	<script type="text/javascript">
		var groupItemDetails = ${groupItemDetails};
		var groupStoreId  = '${ifn:cleanJavaScript(groupStoreId)}';
		var cpath = '${pageContext.request.contextPath}';
		var deptId ='${ifn:cleanJavaScript(dept_id)}';
		var gRoleId = '${roleId}';
		var popurl = '${pageContext.request.contextPath}';
	</script>
</body>
</html>

