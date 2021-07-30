<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<c:set var="prefbarcode" value="<%= GenericPreferencesDAO.getGenericPreferences().getBarcodeForItem() %>" />
<c:set var="prefDecimalQty" value="<%= GenericPreferencesDAO.getGenericPreferences().getAllowDecimalsInQtyForIssues()%>"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.title"/></title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="stores/storescommon.js"/>
<insta:link type="script" file="stores/adjuststock.js"/>
<insta:link type="script" file="ajax.js" />

<script src="${cpath}/pages/stores/getItemMaster.do?ts=${master_timestamp}&hosp=${ifn:cleanURL(sesHospitalId)}"></script>

<style>
.grid{margin:5px 7px 5px 7px;padding:5px 7px 5px 7px;}
.num {text-align: right; width: 6em;}
#autoitem, #autoidentifier {
		width:15em; /* set width here or else widget will expand to fit its container */

	}
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
}
</style>
<script>
function stopRKey(evt) {
  var evt = (evt) ? evt : ((event) ? event : null);
  var node = (evt.target) ? evt.target : ((evt.srcElement) ? evt.srcElement : null);
  if ((evt.keyCode == 13) && (node.type=="text"))  {return false;}
}

document.onkeypress = stopRKey;
var qtyDecimal = '${prefDecimalQty}';
var gItemMasterTimestamp = '${master_timestamp}';
</script>
<insta:js-bundle prefix="stores.mgmt"/>
</head>
<body onload="StockAdjustAutoComp();init();"  class="yui-skin-sam">
<c:set var="adjtype">
 <insta:ltext key="storemgmt.stockadjustmentscreen.addshow.increase"/>,
<insta:ltext key="storemgmt.stockadjustmentscreen.addshow.decrease"/>
</c:set>
<c:set var="longtemplate">
<insta:ltext key="storemgmt.stockadjustmentscreen.addshow.longtemplate"/>
</c:set>
<div id="storecheck" style="display: block;" >
<form action="StoresStockAdjust.do" method="POST" name="StockAdjustmentForm">
	<input type="hidden" name="itemId" value="" />
	<input type="hidden" name="_method" value="save" />
	<input type="hidden" name="stkyype" value="" />
	<input type="hidden" id="dialogId" value=""/>
	<h1><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.stockadjustmentscreen"/></h1>
	<insta:feedback-panel/>
	<c:if test="${not empty msg}" >
		<span class="resultMessage">
				<insta:ltext key="storemgmt.stockadjustmentscreen.addshow.adjustedno"/> <a href="${pageContext.request.contextPath}/DirectReport.do?report=StoreStockAdjustments&adjNo=${ifn:cleanURL(msg)}" target="_blank"> ${ifn:cleanHtml(msg)} </a> <insta:ltext key="storemgmt.stockadjustmentscreen.addshow.generatedforthisstockadjustment"/></span>
	</c:if>
	<table class="formTable"  width="100%">
		<tr>
			<td >
				<fieldset class="fieldSetBorder" >
				<legend class="fieldSetLabel"><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.stockadjustment"/></legend>
					<table class="formtable" cellpadding="0" cellspacing="0" border="0">
					<tr>
					    <td class="formlabel"><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.store"/>:</td>
					    <c:choose>
					   	<c:when test="${(roleId == 1) || (roleId == 2) || (multiStoreAccess == 'A') }">
							<td><insta:userstores username="${userid}" elename="store_id" onchange="getCategories();" id="store_id" val="${dept_id}" tabindex="1" /></td>
						</c:when>
						<c:otherwise>
							<td><input type="hidden" name="store_id" id='store_id' value="${ifn:cleanHtmlAttribute(dept_id)}"><b>${ifn:cleanHtmlAttribute(dept_name)}</b></td>
						</c:otherwise>
						</c:choose>

                      <td>
                          <div class="sboField">
                             <div class="sboFieldLabel"><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.reason"/>:</div>
			                <div id="stock_adjustment" class="autoComplete">
				                 <input type="text" name="reason" id="reason" value="" style="width:235px;" /><td><span class="star">*</span></td>
			                   <div id="adjustment_reason" class="scrolForContainer" style="width:250px"></div>
			                </div>
                         </div>
                      </td>


					</tr>
					</table>
				</fieldset>
			</td>
		</tr>
	</table>
	<fieldset class="fieldSetBorder" >
	<legend class="fieldSetLabel"><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.itemlist"/></legend>
		<table class="dataTable" width="100%" cellspacing="0" cellpadding="0" id="medtabel" >
			<tr>
				<th><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.category"/></th>
				<th><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.item"/></th>
				<th><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.batch.serialno"/></th>
				<th><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.exp.date"/></th>
				<th><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.description"/></th>
				<th><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.unituom"/></th>
				<th><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.remarks"/></th>
				<th></th>
				<th></th>
				<th></th>
			</tr>
			<tr id="tableRow1" style="display:none">
				<td class="forminfo" valign="middle">
		    		<label name="categoryLabel" id="categoryLabel1"></label>
		    		<input type="hidden" name="category_id" id='category_id1' >
				</td>
		    	<td class="forminfo" valign="middle">
		    		<label name=itemLabel" id="itemLabel1"></label>
					 <input type="hidden" name="item_id" id='item_id1' >
				</td>
				<td class="forminfo" valign="middle">
					<label id="identifierLabel1"></label>
		         	<input type="hidden" name="item_identifier" id='item_identifier1' >
		    	 </td>
		    	 <td class="forminfo" valign="middle">
		    	 	<label id="expiryLabel1"></label>
		    	 	<input type="hidden" name="expiry_date" id="expiry_date1">
		    	 </td>
		    	 <td class="forminfo" valign="middle">
					<label id="descriptionLabel1"></label>
		         	<input type="hidden" name="description" id='description1'>
		         	<input type="hidden" name="stype" id='stype1'>
		    	 </td>
		    	 <td class="forminfo" valign="middle">
					<label id="issueUnitsLabel1"></label>
		    	 </td>
		    	 <td class="forminfo" valign="middle">
		    	 	<label id="remarksLabel1"></label>
		         	<input type="hidden" name="remarks" id='remarks1'>
		    	 </td>
		    	 <td class="forminfo" valign="middle">
		         	<input type="hidden" name="incType" id='incType1'>
		         	<input type="hidden" name="stockStatus" id='stockStatus1'>
		         	<input type="hidden" name="qty" id="qty1">
		         	<input type="hidden" name="statusSrc" id='statusSrc1'>
		         	<input type="hidden" name="statusDst" id='statusDst1'>
	         		<input type="hidden" name="medicine_id"/>
					<input type="hidden" name="batch_no"/>
					<input type="hidden" name="item_batch_id"/>
					<input type="hidden" name="item_lot_id"/>
					<input type="hidden" name="lot_time"/>
					<input type="hidden" name="lot_source"/>
					<input type="hidden" name="purchase_type"/>
					<input type="hidden" name="qty"/>
					<input type="hidden" name="qty_maint"/>
					<input type="hidden" name="qty_retired"/>
					<input type="hidden" name="qty_lost"/>
					<input type="hidden" name="qty_kit"/>
					<input type="hidden" name="qty_unknown"/>
					<input type="hidden" name="qty_in_transit"/>
					<input type="hidden" name="qty_in_use"/>
					<input type="hidden" name="identification"/>
					<!-- Additional fields related to adj screen -->
					<input type="hidden" name="adjType"/>
					<input type="hidden" name="adjQty"/>
					<input type="hidden" name="adjStatus"/>
					<input type="hidden" name="adjRemarks"/>
		    	 </td>
		    	 <td>
					<label>
						<img class="imgDelete" src="${cpath}/icons/delete.gif"
						onclick="onDeleteRow(this)" style="cursor:pointer" />
					</label>
				</td>

				<td>
					<label>
						<img class="button" name="editicon" onclick="onEditRow(this)"
						src="${cpath }/icons/Edit.png">
					</label>
				</td>
			</tr>
		</table>

		<table class="addButton">
			<tr>
				<td align="right">
					<button type="button" name="addItem" id="addItem" title='<insta:ltext key="storemgmt.stockadjustmentscreen.addshow.addnewitem"/>'
						onclick="openAddDialog(); return false;" accesskey="+"
			 			class="imgButton" ><img src="${cpath}/icons/Add.png"></button>
				</td>
			</tr>
		</table>
	</fieldset>
	<div class="legend">
		 <div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		 <div class="flagText" ><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.consignmentstock"/></div>
	</div>
	<div class="screenActions">
		<button type="button" accesskey="S" onclick="return validateOnSave()" tabindex="60"><b><u><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.s"/></u></b><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.ave"/></button>
	</div>
</form>
<form name="dialogForm">
	<div id="dialog" style="visibility:hidden" id="addEditDialog">
		<div class="bd">
		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.additem"/></legend>
			<table width="100%" id="resultTable">
				<tr>
					<td colspan="2">
						<table width="100%">
							<tr>
								<c:if test="${prefbarcode eq 'Y'}">
									<th style="text-align:left"><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.itembarcode"/> </th>
								</c:if>
								<th style="text-align:left"><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.item"/></th>
								<th style="text-align:left"><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.batch.serialno"/></th>
								<th style="width:24px"></th>		<%-- for info icon --%>
								<th style="text-align:left"><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.exp.date"/></th>
							</tr>
							<tr>
								<c:choose>
								<c:when test="${prefbarcode eq 'Y'}">
									<td valign="top"><input type="text" name="item_barcode_id" id="item_barcode_id" onchange="getItemBarCodeDetails(this.value);" tabindex="8" ></td>
							    </c:when>
							    <c:otherwise>
							    	<input type="hidden" name="item_barcode_id" id="item_barcode_id" >
							    </c:otherwise>
							    </c:choose>
								<td valign="top">
									<div id="autoitem" style="width: 17em; padding-bottom: 1.8em">
										<input	name="item" id="item"  type="text" style="width: 15em;" tabindex="10"/>
									<div id="itemcontainer" class="scrolForContainer"></div>
									</div>
									<input type="hidden" name="itemid" id="itemid" value="" />
									<input type="hidden" name="category" id="category" value="" />
									<input type="hidden" name="categoryName" id="categoryName" value="" />
									<input type="hidden" name="medicine_id"/>
									<input type="hidden" name="batch_no"/>
									<input type="hidden" name="item_batch_id"/>
									<input type="hidden" name="item_lot_id"/>
									<input type="hidden" name="lot_time"/>
									<input type="hidden" name="lot_source"/>
									<input type="hidden" name="purchase_type"/>
									<input type="hidden" name="qty"/>
									<input type="hidden" name="qty_maint"/>
									<input type="hidden" name="qty_retired"/>
									<input type="hidden" name="qty_lost"/>
									<input type="hidden" name="qty_kit"/>
									<input type="hidden" name="qty_unknown"/>
									<input type="hidden" name="qty_in_transit"/>
									<input type="hidden" name="qty_in_use"/>
									<input type="hidden" name="identification"/>
									<input type="hidden" name="adjType"/>
									<input type="hidden" name="adjQty"/>
									<input type="hidden" name="adjStatus"/>
									<input type="hidden" name="adjRemarks"/>
									<input type="hidden" name="issue_units"/>
								</td>
								<td valign="top" style="text-align:left;width: 100px">
									<div id="autoidentifier" style="width: 14em; padding-bottom: 1.8em">
									<input type="text" id="identifier" name="identifier" tabindex="20" />
									<div id="identifiercontainer" class="scrolForContainer"></div>
									</div>
								</td>
								<td valign="top" style="text-align:left;width: 70px">
									<img class="imgHelpText" title=<insta:jsString value="${longtemplate}"/> src="${cpath}/images/information.png"/>
								</td>
								<td valign="top">
									<input type="text"  id="expiry_dt" name="expiry_dt" tabindex="20" value="" readonly></label>
									<input type="hidden" name="e_item_batch_id" />
								</td>

							</tr>
						</table>
					</td>
				</tr>
				<tr>
					<td>
						<div id="stock" style="display:none">
							<fieldset class="fieldSetBorder" >
							<legend class="fieldSetLabel"><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.stockincrease.decrease"/></legend>
							<table class="dashboard" width="100%">
								<tr>
									<th><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.adjust"/></th>
									<th><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.quantity"/></th>
									<th><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.remarks"/></th>
								</tr>
								<tr>
									<td><insta:selectoptions name="adjType_fld" value="" opvalues="A,R"
												optexts="${adjtype}" tabindex="25"/></td>
									<td>
										<input type="text" name="adjQty_fld" id="adjQty_fld" class="num" style="text-align:right;"  onblur="return makeingDec(this.value,this);" tabindex="35" />
										<span id="stkIssueUnits"></span>
										<input type="hidden" name="issue_units" id="issue_units"/>
									</td>
									<td><input type="text" name="adjRemarks_fld" id="adjRemarks_fld" maxlength="150" tabindex="40" /></td>
								</tr>
							</table>
							</fieldset>
						</div>
					</td>
				</tr>
								<tr>
					<td>
						<div id="status" style="display:none">
							<fieldset class="fieldSetBorder" >
							<legend class="fieldSetLabel"><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.statuschange"/></legend>
							<table class="dashboard" width="100%">
								<tr>
									<th><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.sourcestatus"/></th>
									<th><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.destinationstatus"/></th>
									<th><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.quantity"/></th>
									<th><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.remarks"/></th>
									<th>&nbsp;</th>
								</tr>

							</table>
							</fieldset>
						</div>
					</td>
				</tr>
			</table>
			</fieldset>
			<table>
				<tr>
					<td>
						<button type="button" id="OK" name="OK" accesskey="A"  style="display: inline;" class="button" onclick="onDialogSave();" tabindex="45"><label><b><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.ok"/></b></label></button>
						<button type="button" id="Cancel" name="Cancel" accesskey="A"  style="display: inline;" class="button" onclick="handleCancel();" tabindex="50"><label><b><insta:ltext key="storemgmt.stockadjustmentscreen.addshow.cancel"/></b></label></button>
					</td>
				</tr>
			</table>
		</div>
	</div>
	</form>
</div>

<script>
    var StockadjustreasonList = <%= request.getAttribute("stockadjustreasonList") %>;
	var stockAdjustmentDetails = ${stockDetailsToAdjust};
	var deptId = '${ifn:cleanJavaScript(dept_id)}';
	var gRoleId = '${roleId}';
	var prefBarCode = '${prefbarcode}';

</script>
</body>
</html>
