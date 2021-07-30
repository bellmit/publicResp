	<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.stores.StoresDBTablesUtil"%>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<%@page import="com.insta.hms.master.ServiceSubGroup.ServiceSubGroupDAO"%>

<html>
<head>
	<title> <insta:ltext key="storeprocurement.stockreorder.reordercriteria.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
    <insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="ajax.js"/>
	<insta:link type="script" file="widgets.js"/>
	<script type="text/javascript">
		var deptId = '${empty dept_id? param.dept_id: dept_id}';
		var deptName = '${ifn:cleanJavaScript(dept_name)}';
	    var gRoleId = '${roleId}';
	    var isSuperStore = '${isSuperStore}';
		var default_store = '${default_store}';
		var vatPref = '${prefVat}';
		var allStoresJSON = ${allStoresJSON};
		var decimalsAllowed = '${ifn:cleanJavaScript(decimalsAllowed)}';
		var subGroups = <%= new flexjson.JSONSerializer().serialize(
		ServiceSubGroupDAO.getAllActiveServiceSubGroups()) %>
		var paramSerSubGroupValue = '${ifn:cleanJavaScript(param.service_sub_group_id)}';
		var paramCriteria = '${ifn:cleanJavaScript(param.criteria)}';
		var jSuppPoList = <%= StoresDBTablesUtil.getTableDataInJSON("select supplier_id,po_no,status,store_id from store_po_main where status='O'") %>;

		function sigFigs(n, sig) {
		    var mult = Math.pow(10, sig - Math.floor(Math.log(n) / Math.LN10) - 1);
		    return Math.round(n * mult) / mult;
		}
	</script>
	<style>
		#table3 td {
			padding: 0px 10px 10px 10px;
			font-weight: bold;
		}
		#disabler  tr {
		  background-color: #8888ff;
		}

		table.formtable td.disabler{
			  background-color: #CCCCCC;
			  opacity: 0.5;
			  -webkit-user-select: none;
			  -khtml-user-select: none;
			  -moz-user-select: none;
			  -o-user-select: none;
			  user-select: none;
		}

		table.formtable tr.disabler{
			  background-color: #CCCCCC;
			  opacity: 0.5;
			  -webkit-user-select: none;
			  -khtml-user-select: none;
			  -moz-user-select: none;
			  -o-user-select: none;
			  user-select: none;
		}

	</style>
	<script>

	</script>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<body class="yui-skin-sam" onload="init('${ifn:cleanJavaScript(param.criteria)}');" >
<c:set var="all">
	<insta:ltext key="storeprocurement.poapprovaldashboard.list.all.in.brackets"/>
</c:set>
<form name="reorderCriteriaForm" method="GET" action="StockReorder.do" >
<input type="hidden" name="method" value="list"/>
<input type="hidden" name="dept_id" id="dept_id" value="${empty param.dept_id? dept_id: param.dept_id}"/>
<input type="hidden" name="dept_name" id="dept_name" value="${ifn:cleanHtmlAttribute(dept_name)}" />

<%-- onsubmit="javascript:void(0); return false;"--%>
	<table id="masterTable" style="width:100%;">
		<tr>
			<td>
				<h1><insta:ltext key="storeprocurement.stockreorder.reordercriteria.stockreorder"/></h1>
			</td>
		</tr>
		<tr>
			<td>
				<div id="criteriaDiv" style="display: block;width:100%;">
					<dl class="accordion" style="margin-bottom: 10px;">
						<dt>
							<span><h2><insta:ltext key="storeprocurement.stockreorder.reordercriteria.reordercriteria"/></h2></span>
							<div class="clrboth"></div>
						</dt>
						<dd class="open">
							<div class="bd">
								<table id="criteriaAndFilterTable" class="formTable" style="width:98%;text-align:center;" align="center">
									<tr>
										<td class="formLabel">
											<%--Criteria information goes here --%>
											<fieldset class="fieldSetBorder" >
												<legend class="fieldSetLabel"><insta:ltext key="storeprocurement.stockreorder.reordercriteria.choosecriteria"/></legend>
												<table style="width:100%;">
													<tr >
														<td style="padding-top:0px;padding-bottom:0px;">
															<input type="radio" name="criteria" id="criteria_consumption" value="cons" onclick="handleCriteriaSelect(this);" tabIndex="10">
															<i><b><insta:ltext key="storeprocurement.stockreorder.reordercriteria.consumptionbased"/></b></i>
														</td>
													</tr>
													<tr id="consumptionCriteriaRow" class="disabler">
														<td style="padding-top:0px;padding-bottom:0px;" >
															<table style="width:100%;padding-left:40px;">
																<tr>
																	<td style="padding-top:0px;padding-bottom:0px;">
																		<div>
																			<insta:ltext key="storeprocurement.stockreorder.reordercriteria.basedonconsumptionfor"/> <input type="text" id="cons_days" name="cons_days" value="${ifn:cleanHtmlAttribute(param.cons_days)}"  style="width:45px;" onKeyPress="return enterNumOnlyzeroToNine(event);" tabIndex="15"/> <insta:ltext key="storeprocurement.stockreorder.reordercriteria.days.orderfor"/> <input type="text" id="to_order_days" name="to_order_days" value="${ifn:cleanHtmlAttribute(param.to_order_days)}"  style="width:45px;" onKeyPress="return enterNumOnlyzeroToNine(event);" tabIndex="20"/> <insta:ltext key="storeprocurement.stockreorder.reordercriteria.days"/>
																		</div>
																	</td>
																</tr>
															</table>
														</td>
													</tr>
													<tr>
														<td style="border-top-width:1px;padding-top:0px;padding-bottom:0px;border-top-style:dashed;border-color:#808080;">
															<input type="radio" name="criteria" id="criteria_indent" value="indent" onclick="handleCriteriaSelect(this);" tabIndex="25">
															<i><b><insta:ltext key="storeprocurement.stockreorder.reordercriteria.indenteditems"/></b></i>
														</td>
													</tr>
													<tr id="indentCriteriaRow" class="disabler">
														<td style="padding-top:0px;padding-bottom:0px;">
															<table style="width:70%;padding-left:40px;padding-top:0px;padding-bottom:0px;">
																<tr>
																	<td style="padding-top:0px;padding-bottom:0px;">
																		<input type="radio" name="indent_type" id="indent_type_purchase" value="indent_purchase" style="margin-left:0px;"  tabIndex="30">Items flagged for purchase in Indent: <input type="text" id="purchase_indent" name="purchase_indent" value="${ifn:cleanHtmlAttribute(param.purchase_indent)}"  style="width:45px;" onKeyPress="return enterNumOnlyzeroToNine(event);" tabIndex="35"/>
																	</td>
																	<td style="padding-top:0px;padding-bottom:0px;" >
																		<input type="radio" name="indent_type" id="indent_type_all" value="indent_all"  tabIndex="40" ><insta:ltext key="storeprocurement.stockreorder.reordercriteria.allitemsinindent"/>: <input type="text" id="all_indent" name="all_indent" value="${ifn:cleanHtmlAttribute(param.all_indent)}"  style="width:45px;" onKeyPress="return enterNumOnlyzeroToNine(event);" tabIndex="45"/>
																	</td>
																</tr>
															</table>

														</td>
													</tr>
													<tr>
														<td style="border-top-width:1px;padding-top:0px;padding-bottom:0px;border-top-style:dashed;border-color:#808080;style="padding-top:3px;"">
															<input type="radio" name="criteria" id="criteria_pending_indent" value="pending_indent"  onclick="handleCriteriaSelect(this);" tabIndex="50">
															<i><b><insta:ltext key="storeprocurement.stockreorder.reordercriteria.pendingindentsbased"/></b></i>
														</td>
													</tr>
													<tr id="pendingIndentCriteriaRow" class="disabler">
														<td style="padding-top:0px;padding-bottom:0px;">
															<table style="width:70%;padding-left:40px;padding-top:0px;">
																<tr>
																	<td style="padding-top:0px;padding-bottom:0px;">
																		<insta:ltext key="storeprocurement.stockreorder.reordercriteria.pendingindentsage"/>: <input type="text" id="pending_indent_age" name="pending_indent_age" value="${ifn:cleanHtmlAttribute(param.pending_indent_age)}"  style="width:45px;" onKeyPress="return enterNumOnlyzeroToNine(event);" tabIndex="55"/>
																	</td>
																</tr>
															</table>
														</td>
													</tr>
													<tr>
														<td style="border-top-width:1px;border-top-style:dashed;border-color:#808080;">
															<input type="radio" name="criteria" id="criteria_reorder_level" value="reorder" onclick="handleCriteriaSelect(this);" tabIndex="60">
															<i><b><insta:ltext key="storeprocurement.stockreorder.reordercriteria.reorderlevelbased"/></b></i>
														</td>
													</tr>
													<tr id="reorderCriteriaRow" class="disabler">
														<td style="padding-top:0px;padding-top:0px;padding-bottom:0px;">
															<table style="width:70%;padding-left:40px;padding-top:0px;">
																<tr>
															    	<td style="padding-top:0px;padding-bottom:0px;">
															    		<input type="radio" name="reorder_level" id="rl" value="rl" checked tabIndex="65"><insta:ltext key="storeprocurement.stockreorder.reordercriteria.belowreorderlevel"/>
															    	</td>
															       	<td style="padding-top:0px;padding-bottom:0px;">
															       		<input type="radio" name="reorder_level" id="dl" value="dl" tabIndex="70"><insta:ltext key="storeprocurement.stockreorder.reordercriteria.belowdangerlevel"/>
															       	</td>
															       	<td style="padding-top:0px;padding-bottom:0px;">
															      		<input type="radio" name="reorder_level" id="ml" value="ml" tabIndex="75"><insta:ltext key="storeprocurement.stockreorder.reordercriteria.belowminimumlevel"/>
															       	</td>
																</tr>
															</table>
														</td>
													</tr>
												</table>
											</fieldset>
										</td>
									</tr>
									<tr>
										<td class="formLabel">
											<%--Filter Information goes here --%>
											<fieldset class="fieldSetBorder" >
												<legend class="fieldSetLabel"><insta:ltext key="storeprocurement.stockreorder.reordercriteria.applyfilter"/></legend>
												<table style="width:100%;">
													<tr>
														<td style="padding-top:0px;padding-bottom:0px;">
															<table style="width:80%;">
																<tr>
																	<td style="padding-top:0px;padding-bottom:0px;">
																		<insta:ltext key="storeprocurement.stockreorder.reordercriteria.salesexceedingquantity"/> <input type="text" id="sales_quantity" name="sales_quantity" value="${ifn:cleanHtmlAttribute(param.sales_quantity)}"  style="width:45px;" onKeyPress="return enterNumOnlyzeroToNine(event);"  tabIndex="90" /> <insta:ltext key="storeprocurement.stockreorder.reordercriteria.soldin"/> <input type="text" id="sale_days" name="sale_days" value="${ifn:cleanHtmlAttribute(param.sale_days)}"  style="width:45px;" onKeyPress="return enterNumOnlyzeroToNine(event);" tabIndex="95"/> <insta:ltext key="storeprocurement.stockreorder.reordercriteria.days"/>
																		<br/>
																	</td>
																	<td align="right" style="padding-top:0px;padding-bottom:0px;"><insta:ltext key="storeprocurement.stockreorder.reordercriteria.servicegroup"/>:
																		<insta:selectdb id="service_group_id" name="service_group_id" value="${param.service_group_id}"
																				table="service_groups" class="dropdown" dummyvalue="${all}"
																				valuecol="service_group_id"  displaycol="service_group_name" onchange="loadServiceSubGroup();" tabIndex="100"></insta:selectdb>
																		<br/>
																	</td>
																</tr>
																<tr>
																	<td style="padding-top:0px;padding-bottom:0px;">
																		<input type="checkbox" name="exclude_poitem" id="exclude_poitem" checked tabIndex="105"><insta:ltext key="storeprocurement.stockreorder.reordercriteria.excludeitemsraisedinpo"/>
																		<br/><br/>
																	</td>
																	<td align="right" style="padding-top:0px;padding-bottom:0px;">
																		<insta:ltext key="storeprocurement.stockreorder.reordercriteria.servicesubgroup"/>:
																		<select name="service_sub_group_id" id="service_sub_group_id" class="dropdown" tabIndex="110">
																			<option value="">${all}</option>
																		</select>
																		<br/><br/>
																	</td>
																</tr>
																<tr>
																	<td style="padding-top:0px;padding-bottom:0px;">
																		<insta:ltext key="storeprocurement.stockreorder.reordercriteria.preferredsupplier"/>:
																		<select name="preferred_supplier" id="preferred_supplier" class="dropdown" value="${ifn:cleanHtmlAttribute(param.preferred_supplier)}">
																		<option value="">${ifn:cleanHtml(all)}</option>
																			<c:forEach items="${listAllcentersforAPo}" var="supplier">
																				<option value="${supplier.map.supplier_code}">${supplier.map.supplier_name}</option>
																			</c:forEach>
																		</select>
																		<br/>
																	</td>
																	<td align="right" style="padding-top:0px;padding-bottom:0px;">
																		&nbsp;
																		<c:if test="${!(multiStoreAccess eq 'A' || roleId eq 1 || roleId eq 2 )}">
																			<insta:ltext key="storeprocurement.stockreorder.reordercriteria.selectedstore"/>: <b><insta:getStoreName store_id="${empty dept_id? param.dept_id : dept_id}" tabIndex="120"/></b>
																			<input type="hidden" name="selected_stores" id="selected_stores" value="${empty dept_id? param.dept_id : dept_id}"/>
																		</c:if>
																		<br/>
																	</td >
																</tr>
																<c:if test="${(multiStoreAccess eq 'A' || roleId eq 1 || roleId eq 2 )}">
																<tr >
																	<td colspan="2" align="left" style="padding-top:0px;padding-bottom:0px;">
																		<table id="storeSelectTable" style="width:100%;">
																			<tr>
																				<td align="left"  style="padding-left:0px;" >
																					<table>
																						<tr>
																							<td style="text-align:left;padding-left:0px;padding-top:0px;padding-bottom:0px;width:100px" align="right" >
																								<insta:ltext key="storeprocurement.stockreorder.reordercriteria.availablestores"/>:
																							</td>
																							<td style="text-align:left;padding-top:0px;padding-bottom:0px;" align="left" colspan="2" tabIndex="120">
																								<insta:userstores username="${userid}" elename="avlbl_stores" id="avlbl_stores"  multipleSelect="5" style="width:11.5em;padding-left:5;color:#666666;" onDblClick="moveSelectedOptions(this,this.form.selected_stores);"/>
																							</td>
																						</tr>
																					</table>
																				</td>
																				<td valign="top" align="center"  style="padding-top:15px;padding-bottom:0px;">
																					<input type="button" name="addLstFldsButton" value=">>"  title="Add" style="width:75px;" onclick="addSelected_stores();" tabIndex="125"/><br /><br />
																					<input type="button" onclick="removeSelected_stores();"  value="<<"  title="Remove" style="width:75px;" tabIndex="135" />
																				</td>
																				<td  align="right"  style="padding-left:0px;padding-top:0px;padding-bottom:0px;">
																					<table>
																						<tr>
																							<td style="text-align:right;padding-top:0px;padding-bottom:0px;" align="right">
																								<insta:ltext key="storeprocurement.stockreorder.reordercriteria.selectedstores"/>:
																							</td>
																							<td style="text-align:right;padding-top:0px;padding-bottom:0px;" align="right" colspan="2" tabIndex="130">
																								<select size="5" style="width:11.5em;padding-left:5; color:#666666;" name="selected_stores" id="selected_stores" multiple onDblClick="moveSelectedOptions(this,this.form.avlbl_stores);">
																								</select>
																							</td>
																						</tr>
																					</table>
																				</td>
																			</tr>
																		</table>
																	</td>
																</tr>
																</c:if>
															</table>
														</td>
													</tr>

												</table>
												<table style="width:240px;padding-left:10px;padding-top:0px;padding-bottom:0px;">
													<tr>
														<td><insta:ltext key="storeprocurement.stockreorder.reordercriteria.purchasedate"/>:</td>
														<td style="padding-top:0px;padding-bottom:0px;padding-left:0px;">
															<insta:datewidget name="purchase_flag_date_dt" id="purchase_flag_date_dt" btnPos="left"/>
														</td>

													</tr>
												</table>
											</fieldset>
										</td>
									</tr>
								</table>
							</div>
						</dd>
					</dl>
				</div>
			</td>
		</tr>
		<tr>
		</tr>
	</table>
	<div align="right" style="margin-right: 2em;" id="buttonDiv"><input type="button" value="Search" onclick="return validateCriteriaForm();" class="button" tabIndex="140">&nbsp;<input type="button" value="Clear" onclick="clearSearch();" class="button" tabIndex="145">
	<img class="imgHelpText" title="<insta:ltext key="storeprocurement.stockreorder.reordercriteria.help"/>"
	src="${cpath}/images/information.png" onclick="doHelp();" tabIndex="150"/>
	</div>
</form>

<%-- Criteria and Filter Form Portion Ends--%>

<%-- ResultList Form Portion Starts--%>

<form name="resultForm" method="POST" action="StockReorder.do">

	<table id="resultFormTopTable">
		<tr>
			<td>
				<div id="gridportion" style="display: block">
					<insta:paginate curPage="${empty param.pageNum? pagedList.pageNumber:param.pageNum}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}" />
				</div>
			</td>
		</tr>
		<tr id="resultListRow">
			<td>
				<fieldset  class="fieldSetBorder" style="width: 90%;">
			    <legend class="fieldSetLabel"> <insta:ltext key="storeprocurement.stockreorder.reordercriteria.itemlist"/></legend>
					<div class="resultList">
						<table class="detailList dialog_displayColumns" id="resultTable" cellpadding="0" cellspacing="0">
							<tr >
								<th>
								</th>
								<th>
									<insta:ltext key="storeprocurement.stockreorder.reordercriteria.item"/>
								</th>
								<th>
									<insta:ltext key="storeprocurement.stockreorder.reordercriteria.qtyavbl"/>
								</th>
								<th>
									<insta:ltext key="storeprocurement.stockreorder.reordercriteria.danger"/>
								</th>
								<th>
									<insta:ltext key="storeprocurement.stockreorder.reordercriteria.min"/>
								</th>
								<th>
									<insta:ltext key="storeprocurement.stockreorder.reordercriteria.max"/>
								</th>
								<th>
									<insta:ltext key="storeprocurement.stockreorder.reordercriteria.reorder"/>
								</th>
								<th>
									<insta:ltext key="storeprocurement.stockreorder.reordercriteria.indentpending"/>
								</th>
								<th>
									<insta:ltext key="storeprocurement.stockreorder.reordercriteria.flagged"/>
								</th>
								<th>
									<insta:ltext key="storeprocurement.stockreorder.reordercriteria.poraised"/>
								</th>
								<th>
									<insta:ltext key="storeprocurement.stockreorder.reordercriteria.consumption"/>
								</th>
								<th>
									<insta:ltext key="storeprocurement.stockreorder.reordercriteria.preferredsupplier"/>
								</th>
								<th>
									<insta:ltext key="storeprocurement.stockreorder.reordercriteria.orderunit.qty"/>
								</th>
								<th>
									<insta:ltext key="storeprocurement.stockreorder.reordercriteria.orderpkg"/>
								</th>
							</tr>
							<c:set var="i" value="1"/>
        					<c:forEach items="${pagedList.dtoList}" var="item" varStatus="st">
        						<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
									onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"
									onclick= "showToolbar(${st.index}, event, 'resultTable',
												{itemId:'${item.map.item_id }',storeId:'${ifn:cleanJavaScript(param.store_id)}' });"
									>
					        		<td class="noclass" onclick="stopToolBar(this);">
										<input type="checkbox" name="selItem" id="selItem${i}"
											onclick="selectItemRow(this, ${i})" />
									    <input type="hidden" name="hselected" id="hselected${i}" value="false"/>
									</td>
					        		<td style="width:10em;padding-left: 0.5em;">
					        			<input type="hidden" name="itemId" id="itemId${i }" value="${item.map.item_id}">
					        			<insta:truncLabel value="${item.map.medicine_name}" length="18"/>
					        		</td>
					        		<td align="right" style="padding-left: 1.0em;">
					        			${ifn:afmt(item.map.availableqty)}
					        			<span id="minLevelIndicator${i}" title="Qty below minimum level"  style="display: ${item.map.min_level> 0 && item.map.availableqty < item.map.min_level? 'inline': 'none'}">
					        				<img src="${cpath}/images/alert.png" class="flag" title="Qty below minimum level"/>
					        			</span>
					        		</td>
					        		<td align="right" style="padding-left: 1.0em;">
					        			${item.map.danger_level}
					        		</td>
					        		<td align="right" style="padding-left: 1.0em;">
					        			${item.map.min_level}
					        			<input type="hidden" id="maxlevel${i}" value="${item.map.max_level}" />
					        		</td>
					        		<td align="right" style="padding-left: 1.0em;">
					        			${item.map.max_level}
					        		</td>
					        		<td align="right" style="padding-left: 1.0em;" >
					        			${item.map.reorder_level}
					        		</td>
					        		<td align="right" style="padding-left: 1.0em;" >
					        			${item.map.indentqty}
					        		</td>
					        		<td align="right" style="padding-left: 1.0em;" >
					        			${item.map.flaggedqty}
					        		</td>
					        		<td align="right" style="padding-left: 1.0em;">
					        			${item.map.poqty}
									</td>
					        		<td align="right" style="padding-left: 1.0em;" title="${empty param.cons_days? 'Not Applicable': ''}">
	        							<c:choose>
	        								 <c:when test="${not empty param.cons_days }">${ifn:afmt(item.map.consumedqty)}</c:when>
	        		 						 <c:otherwise>NA</c:otherwise>
	        							</c:choose>
        							</td>
        							<td align="right" style="padding-left: 1.0em;" >
					        			<insta:truncLabel value="${item.map.pref_supplier_name}" length="20"/>
					        		</td>
					        		<c:set var="ord_qty" value="${item.map.ord_qty}"/>
					        		<td align="right" class="noclass" onclick="stopToolBar(this);">
					        			<c:set var="ordDays" value="${empty param.ord_days? 1: param.ord_days}"/>
					        			<input type="text" name="orderqty" id="orderqty${i }"
					        				value="${decimalsAllowed == 'Y'? ifn:afmt(ord_qty) : ifn:round(ord_qty, 'ROUND_CEILING', '0') == 0? 1: ifn:round(ord_qty, 'ROUND_CEILING', '0')}" class="number"
					        				readOnly onchange="onchangeQty('${i}', this);">
					        			<c:set var="ordQtyFmtd" value="${decimalsAllowed == 'Y'? ifn:afmt(ord_qty) : ifn:round(ord_qty, 'ROUND_CEILING', '0')== 0? 1: ifn:round(ord_qty, 'ROUND_CEILING', '0')}"/>
					        			<input type="hidden" name="pkgsize" id="pkgsize${i}" value="${item.map.pkg_size}">
					        			<span id="maxLevelIndicator${i}" title="Qty above maximum level" style="display: ${item.map.max_level> 0 && ordQtyFmtd > item.map.max_level? 'inline': 'none'}">
					        				<img src="${cpath}/images/error.png"/>
					        			</span>
					        		</td>
        							<td align="right" style="padding-left: 1.0em;" >
										<label id="baselbl${i }">
											${ifn:round(ordQtyFmtd/item.map.pkg_size,'ROUND_CEILING', '0') == 0?  1:ifn:round(ordQtyFmtd/item.map.pkg_size,'ROUND_CEILING', '0')}
					 					</label>
        							</td>
        						</tr>
        	 					<c:set var="i" value="${i+1}"/>
        					</c:forEach>
						</table>
					</div>
				</fieldset>
			</td>
		</tr>
		<tr>
			<td>
				<c:if test="${not empty pagedList.dtoList}">
			       <div class="screenActions">
			       		<input type="checkbox" value="selAllItems" onchange="selectAllItems()" name="_AllItems" tabIndex="160"/> <insta:ltext key="storeprocurement.stockreorder.reordercriteria.selectallonthispage"/>
						<input type="submit" name="" id="" value="Raise PO on" onclick="return validateResultForm('new');" tabIndex="165">
						<select name="store_id" id="store_id" class="dropdown" tabIndex="170">
							<option value=""><insta:ltext key="storeprocurement.stockreorder.reordercriteria.store.in.brackets"/></option>
						</select>
						<select name="supplier_id" id="supplier_id" class="dropdown" onchange="setPOs(this.value);" tabIndex="170">
							<option value="">${all}</option>
							 <c:forEach items="${listAllcentersforAPo}" var="supplierItems">
								<option value="${supplierItems.map.supplier_code}" >${supplierItems.map.supplier_name}</option>
							</c:forEach> 
						</select>
						<insta:ltext key="storeprocurement.stockreorder.reordercriteria.or"/>
						<input type="submit" name="" id="" value="Add To PO " onclick="return validateResultForm('old');">
						<select name="poNo" class="dropdown">
						 	<option value=""><insta:ltext key="storeprocurement.stockreorder.reordercriteria.pono.in.brackets"/></option>
						</select>
					</div>
				</c:if>
			</td>
		</tr>
		<c:if test="${not empty pagedList.dtoList}">
		<tr>
			<td>
				<div class="screenActions">
					<button type="button" onClick = "getCsv();" ><insta:ltext key="storeprocurement.stockreorder.reordercriteria.exporttocsv"/></button>
				</div>
			</td>
		</tr>
		</c:if>
	</table>
</form>
<div id="existingstock" style="visibility:hidden">
	<div class="bd">
		<table id="table3" cellpadding="5">
		</table>
		<fieldset class="fieldSetBorder" >
			<legend class="fieldSetLabel"><insta:ltext key="storeprocurement.stockreorder.reordercriteria.itempurchasedetails"/></legend>
			<table class="dashboard" width="100%" cellspacing="0" cellpadding="0" id="table2">
				<tr >
			        <th ><insta:ltext key="storeprocurement.stockreorder.reordercriteria.supplier"/></th>
			        <th ><insta:ltext key="storeprocurement.stockreorder.reordercriteria.costprice"/></th>
			        <th ><insta:ltext key="storeprocurement.stockreorder.reordercriteria.mrp"/></th>
		       		<c:if test="${prefVat eq 'Y'}"> <th><insta:ltext key="storeprocurement.stockreorder.reordercriteria.vatrate"/></th></c:if>
			        <th ><insta:ltext key="storeprocurement.stockreorder.reordercriteria.invoicenumber"/></th>
			        <th ><insta:ltext key="storeprocurement.stockreorder.reordercriteria.invoicedate"/></th>
			        <th ><insta:ltext key="storeprocurement.stockreorder.reordercriteria.grnnumber"/></th>
			        <th ><insta:ltext key="storeprocurement.stockreorder.reordercriteria.grndate"/></th>
			        <th ><insta:ltext key="storeprocurement.stockreorder.reordercriteria.ponumber"/></th>
			        <th><insta:ltext key="storeprocurement.stockreorder.reordercriteria.bonus"/></th>
			        <th><insta:ltext key="storeprocurement.stockreorder.reordercriteria.discount"/></th>
		        </tr>
			</table>
		 </fieldset>
	</div>
</div>

<script>
	function getCsv() {
		var getCsv = true;
		validateCriteriaForm(getCsv);
	}

	function selectItemRow(checkBox, rowId) {
		var itemListTable = document.getElementById("resultTable");
		var row = itemListTable.rows[rowId];
		var deletedInput = document.getElementById('hselected'+rowId);
		if (checkBox.checked) {
			deletedInput.value = 'true';
			document.getElementById('orderqty'+rowId).readOnly = false;
			row.setAttribute("class", "rowbgToolBar");
		} else {
			deletedInput.value = 'false';
			document.getElementById('orderqty'+rowId).readOnly = true;
			row.setAttribute("class", "");
	    }
	}

	function setPOs(val){
		if (val == ''){
			document.resultForm.poNo.length = 0;
		}else{
			var store = document.getElementById('store_id').value;
			if(store == ''){
				alert("Please select a store");
				setSelectedIndex(document.getElementById("supplier_id"),'');
			}
			var supplierPOs = filterList (filterList (filterList(jSuppPoList, "SUPPLIER_ID", val),"STORE_ID",store), "STATUS","O");
			loadSelectBox(document.resultForm.poNo, supplierPOs, "PO_NO", "PO_NO", "------ (PO No) -----", "");
		}
	}

	function selectAllItems(){
		var form = document.resultForm;
		if(form._AllItems) {
			var checkStatus =   form._AllItems.checked
			var length = form.selItem.length;
			if(length == 'undefined' || form.selItem.type== 'checkbox') {
		 		if (checkStatus) {
					form.hselected.value = 'true';
					form.selItem.checked = true;
				}
				else {
					form.hselected.value = 'false';
					form.selItem.checked = false;
				}
				selectItemRow(form.selItem,1);
			} else {
				var count = 0;
				for(var i=0;i<length;i++){
					if (checkStatus) {
						form.hselected[i].value = 'true';
						form.selItem[i].checked = true;
					}
					else {
						form.hselected[i].value = 'false';
						form.selItem[i].checked = false;
					}
					var k = i;
					var obj = document.getElementById('selItem'+(k+1));
					selectItemRow(obj,k+1);
			   }
	       }
	    }
    }


    function validateResultForm(val) {
   		var itemListTable = document.getElementById("resultTable");
	   	var actualrows = itemListTable.rows.length;
		var numRows = itemListTable.rows.length-1;
		var nothingChecked = true;
		if (document.resultForm.supplier_id.value == '') {
			alert("Please select a Supplier");
			document.getElementById('supplier_id').focus();
			return false;
		}
		if (document.resultForm.store_id.value == '') {
			alert("Please select a Store");
			document.getElementById('store_id').focus();
			return false;
		}
		if (val == 'old' && document.resultForm.poNo.value == '') {
			alert("Please select the PO No");
			return false;
		}
		if (numRows >= 1) {
			for(var k=1;k<=numRows;k++){
				if ((document.getElementById("selItem"+k).checked)) {
					nothingChecked = false;
				}
			}
	 		if (nothingChecked) {
		    	alert("No items have been selected...\nPlease select an item to save");
	 		    return false;
		    }
		} else {
		    alert("Save cannot be processed...\nThere are no items to save");
				return false;
		}
	    for(var j=1;j<=numRows;j++){
	    	if (document.getElementById("selItem"+j).checked) {
	    		if (!isValidNumber(document.getElementById("orderqty"+j), decimalsAllowed)) return false;

		 	    if (document.getElementById("orderqty"+j).value <= 0) {
	                alert("Order Qty (Issue units) should be greater than zero");
	             	document.getElementById("orderqty"+j).focus();
	             	return false;
		        }
		        if (document.getElementById("baselbl"+j).textContent <= 0) {
	                alert("Order Qty (Pkg) should be greater than zero");
	             	document.getElementById("orderqty"+j).focus();
	             	return false;
		        }
	    	}
       }

       if(!validateIfOnlyNumbersPresent(document.resultForm))
       		return false;
      	document.resultForm.action = '${cpath}/pages/stores/poscreen.do?_method=goPOScreenWithExistingItems';
       	document.resultForm.submit();
    }

	function onchangeQty (rowId) {
		if (!isValidNumber(document.getElementById("orderqty"+rowId), decimalsAllowed)) return false;

		var orderedQty = parseFloat(document.getElementById("orderqty"+rowId).value);
		var maxLevel = document.getElementById("maxlevel"+rowId).value;

		if(maxLevel > 0 && orderedQty> maxLevel) {
			document.getElementById('maxLevelIndicator'+rowId).style.display = 'inline';
		} else {
			document.getElementById('maxLevelIndicator'+rowId).style.display = 'none';
		}
		var pkgsize = parseFloat(document.getElementById("pkgsize"+rowId).value);
		var baseQty = Math.ceil(orderedQty/pkgsize);
		document.getElementById("baselbl"+rowId).textContent = parseFloat(baseQty).toFixed(0);
	}

	function stopToolBar(chk) {
			var e=window.event||arguments.callee.caller.arguments[0];
		if (e.stopPropagation) e.stopPropagation();
		}

		function doHelp(a) {
			var helpWin=window.open("${cpath}/stores/StockReorder.do?method=getHelpPage", 'Help on Stock Reorder screen',
					'height=700,width=800,resizable=yes,scrollbars=yes,status=no');
			helpWin.focus();
			return false;
	}

	function handleCriteriaSelect(criteriaObj) {
		var value = criteriaObj.value;
		if(value == 'cons') {
			disableAllCriteriaActions();
			enableCriteriaAction(document.getElementById('consumptionCriteriaRow'));
			if(paramCriteria == '') {
				document.getElementById('cons_days').value  = 30;
				document.getElementById('to_order_days').value = 30;
			}
		} else if( value ==  'indent') {
			disableAllCriteriaActions();
			enableCriteriaAction(document.getElementById('indentCriteriaRow'));
			if(document.getElementById('indent_type_purchase').checked) {
				removePreviousSpans(document.getElementById('all_indent').parentNode);
				removePreviousSpans(document.getElementById('purchase_indent').parentNode);
			} else {
				removePreviousSpans(document.getElementById('purchase_indent').parentNode);
				removePreviousSpans(document.getElementById('all_indent').parentNode);
			}
		} else if ( value == 'pending_indent') {
			disableAllCriteriaActions();
			enableCriteriaAction(document.getElementById('pendingIndentCriteriaRow'));
		} else if ( value == 'reorder' ){
			disableAllCriteriaActions();
			enableCriteriaAction(document.getElementById('reorderCriteriaRow'));
		}
	}

	function disableAllCriteriaActions() {
		document.getElementById('consumptionCriteriaRow').className = "disabler";
		disableFormFields(document.getElementById('consumptionCriteriaRow'), true);

		document.getElementById('indentCriteriaRow').className = "disabler";
		disableFormFields(document.getElementById('indentCriteriaRow'), true);

		document.getElementById('pendingIndentCriteriaRow').className = "disabler";
		disableFormFields(document.getElementById('pendingIndentCriteriaRow'), true);

		document.getElementById('reorderCriteriaRow').className = "disabler";
		disableFormFields(document.getElementById('reorderCriteriaRow'), true);
	}

	function enableCriteriaAction(parent) {
		parent.className = '';
		disableFormFields(parent, false);
	}

	function isValidAmount(text) {
	    var str = text.toString();
		var re = (decDigits == 3) ? /^\d{0,10}(\.\d{1,3})?$/ : /^\d{0,10}(\.\d{1,2})?$/;
	    return re.test(str);
	}

	function validateIfOnlyNumbersPresent(formObj){
		var elems = formObj.getElementsByTagName("INPUT");
		for (var j = 0; j < elems.length; j++) {
			if(elems[j].type == 'text') {
				var value =  (elems[j].value.trim()).replace(/\,/g,'');
				if ( elems[j].name == 'purchase_flag_date_dt' ) {
					return doValidateDateField(elems[j]);
				} else {
					if(value.length > 13){
						alert("Numeric value too large");
						elems[j].focus();
						return false;
					} else if(!isValidAmount(value.trim())) {
						alert("Please enter a valid number");
						elems[j].focus();
						return false;
					}
				}
			}
		}
		return true;
	}

	function disableFormFields(parent, isDisabled) {
		var tagNames = ["INPUT", "SELECT", "TEXTAREA"];
		for (var i = 0; i < tagNames.length; i++) {
		    var elems = parent.getElementsByTagName(tagNames[i]);
		    for (var j = 0; j < elems.length; j++) {
		      elems[j].disabled = isDisabled;
		      if(!isDisabled && elems[j].type == "text"){
		      		appendStarSpan(elems[j].parentNode);
		      }
		    }
		}
	}

	function appendStarSpan(parent){
		var star = document.createElement("span");
		star.setAttribute("class", "star");
		star.innerHTML = "*";
		removePreviousSpans(parent);
		parent.appendChild(star);
	}

	function removePreviousSpans(parent) {
		var children= parent.childNodes;
		var spanChildren = new Array();
		if(children!= null){
			var j=0;
			for(var i=0; i<children.length; i++) {
				if(children[i].tagName == "SPAN"){
					spanChildren[j++] = children[i];
				}
			}
		}
		if(spanChildren != null) {
			for(var k=0; k<spanChildren.length; k++){
				parent.removeChild(spanChildren[k]);
			}
		}
	}

	function loadServiceSubGroup() {
		var groupId = document.getElementById("service_group_id").value;
		var filteredList = filterList(subGroups, 'SERVICE_GROUP_ID', groupId);
		loadSelectBox(document.getElementById("service_sub_group_id"),
				filteredList, 'SERVICE_SUB_GROUP_NAME', 'SERVICE_SUB_GROUP_ID', '-- All --', '');
		if (document.getElementById("service_sub_group_id")!= null || document.getElementById("service_sub_group_id")!= '') {
			setSelectedIndex(document.getElementById("service_sub_group_id"), paramSerSubGroupValue);
		}
	}

	function clearSearch() {
		clearForm(document.reorderCriteriaForm);
		var form = document.reorderCriteriaForm;
		if (document.reorderCriteriaForm) {
			for (var i=0; i<document.reorderCriteriaForm.elements.length; i++) {
				if (document.reorderCriteriaForm.elements[i].nodeName == 'FIELDSET')
					continue;
				var type = form.elements[i].type;
				if (type)
					type = type.toLowerCase();
				// to select the first radio or checkbox
				switch (type) {
				 	case "radio":
				 	case "checkbox":
				 	var radioElements = document.getElementsByName(form.elements[i].name);
				 	if(radioElements!= null && radioElements.length >0){
				 		radioElements[0].checked = true;
				 		radioElements[0].click();
				 	}

				}
			}
		}
	}
	// function to move between selects
	var avlFlds;
	var selFlds;

	function swapOptions(obj, i, j) {
		var o = obj.options;
		var i_selected = o[i].selected;
		var j_selected = o[j].selected;
		var temp = new Option(o[i].text, o[i].value, o[i].title, o[i].defaultSelected, o[i].selected);
		temp.setAttribute("title", o[i].title);
		var temp2 = new Option(o[j].text, o[j].value, o[j].title, o[j].defaultSelected, o[j].selected);
		temp2.setAttribute("title", o[j].title);

		o[i] = temp2;
		o[j] = temp;
		o[i].selected = j_selected;
		o[j].selected = i_selected;
	}

	function moveOptionUp(obj) {
		if (!hasOptions(obj)) {
			return;
		}
		for (i = 0; i < obj.options.length; i++) {
			if (obj.options[i].selected) {
				if (i != 0 && !obj.options[i - 1].selected) {
					swapOptions(obj, i, i - 1);
					obj.options[i - 1].selected = true;
				}
			}
		}
	}

	function moveOptionDown(obj) {
		if (!hasOptions(obj)) {
			return;
		}
		for (i = obj.options.length - 1; i >= 0; i--) {
			if (obj.options[i].selected) {
				if (i != (obj.options.length - 1) && !obj.options[i + 1].selected) {
					swapOptions(obj, i, i + 1);
					obj.options[i + 1].selected = true;
				}
			}
		}
	}

	function sortSelect(obj) {
		var o = new Array();
		if (!hasOptions(obj)) {
			return;
		}
		for (var i = 0; i < obj.options.length; i++) {
			o[o.length] = new Option(obj.options[i].text, obj.options[i].value, obj.options[i].defaultSelected, obj.options[i].selected);
			(o[i]).title = obj.options[i].title;
			(o[i]).value = obj.options[i].value;

		}
		if (o.length == 0) {
			return;
		}
		o = o.sort(function(val1, val2) {

			if ((val1.text + "") < (val2.text + "")) {
				return - 1;
			}
			if ((val1.text + "") > (val2.text + "")) {
				return 1;
			}
			return 0;
		});

		for (var i = 0; i < o.length; i++) {
			obj.options[i] = new Option(o[i].text, o[i].defaultSelected, o[i].selected);
			obj.options[i].title = o[i].title;
			obj.options[i].value = o[i].value;
			obj.options[i].setAttribute("onmouseover", 'this.title="'+o[i].text+'"');
		}
	}

	function createListElements(from, to) {
		avlFlds = document.getElementById(from);
		selFlds = document.getElementById(to);
	}

	function hasOptions(obj) {
		if (obj != null && obj.options != null) {
			return true;
		}
		return false;
	}

	/*
	 * Move a single named field (no need to mark as selected)
	 * from one list to another
	 */
	function moveSelectedOption(fromList, toList, fieldName) {
		if (!hasOptions(fromList)) {
			return;
		}
		for (var i = 0; i < fromList.options.length; i++) {
			var o = fromList.options[i];
			if (o.value == fieldName) {
				if (!hasOptions(toList)) {
					var index = 0;
				} else {
					var index = toList.options.length;
				}
				toList.options[index] = new Option(o.text, o.value, o.title, false, false);
				toList.options[index].setAttribute("title", o.title);

				// Delete the selected options from  the available list.
				fromList.options[i] = null;
				break;
			}
		}

		// Only the 'toList' may need sorting
		if (toList.id=='avlbl_stores') {
			sortSelect(toList);
		}

		fromList.selectedIndex = -1;
		toList.selectedIndex = -1;
	}

	/*
	 * Move all fields in the from list marked as selected to the to list
	 */
	function moveSelectedOptions(from, to, sort) {
		addToStoresDropDown();
		if (!hasOptions(from)) {
			return;
		}
		for (var i = 0; i < from.options.length; i++) {
			var o = from.options[i];
			if (o.selected) {
				if (!hasOptions(to)) {
					var index = 0;
				} else {
					var index = to.options.length;
				}
				to.options[index] = new Option(o.text, o.value, o.title, false, false);
				to.options[index].setAttribute("title", o.title);
				to.options[index].setAttribute("onmouseover", 'this.title="'+o.text+'"');
			}
		}
		// Delete the selected options from  the available list.
		for (var i = (from.options.length - 1); i >= 0; i--) {
			var o = from.options[i];
			if (o.selected) {
				from.options[i] = null;
			}
		}
		if(from.id=='avlbl_stores' ){
			sortSelect(from);
		}else if(to.id=='avlbl_stores' ){
			sortSelect(to);
		}
		from.selectedIndex = -1;
		to.selectedIndex = -1;
		addToStoresDropDown();
	}

	function addSelected_stores() {
		createListElements('avlbl_stores', 'selected_stores');
		moveSelectedOptions(avlFlds, selFlds, 'from');
		addToStoresDropDown();
	}

	function addToStoresDropDown() {
		var storesObj = document.getElementById('store_id');
		var selectedStoresObj = document.getElementById('selected_stores');
		if(storesObj != null) {
			for(var k=1; k< storesObj.options.length; k++){
				storesObj.options[k] = null;
			}
			storesObj.length = 1;
			if(selectedStoresObj.type == 'hidden') {
				storesObj.length = 2;
				storesObj.options[1].text = deptName;
				storesObj.options[1].value = deptId;
			} else {
				var j=0;
				for(var i=0; i< selectedStoresObj.options.length;i++){
					var superstr = "";
					var superstr = findInList2(allStoresJSON, "dept_id", selectedStoresObj.options[i].value, "is_super_store", "Y")
					if(superstr!= null &&  superstr != "") {
						storesObj.length = (j+1)+1;
						storesObj.options[j+1].text = selectedStoresObj.options[i].text;
						storesObj.options[j+1].value = selectedStoresObj.options[i].value;
						j++;
					}
				}
			}
		}
	}


	function removeSelected_stores() {
		createListElements('avlbl_stores', 'selected_stores');
		moveSelectedOptions(selFlds, avlFlds);
		addToStoresDropDown();
	}

	function getCheckedValue(radioObj) {
		if(!radioObj)
			return "";
		var radioLength = radioObj.length;
		if(radioLength == undefined)
			if(radioObj.checked)
				return radioObj.value;
			else
				return "";
		for(var i = 0; i < radioLength; i++) {
			if(radioObj[i].checked) {
				return radioObj[i].value;
			}
		}
		return "";
	}


	function validateCriteriaForm(getCsv) {
		var criteria = getCheckedValue(document.reorderCriteriaForm.criteria);
		if( criteria == 'cons') {
			if(!validateConsumptionCriteria())
				return false;
		} else if ( criteria == 'indent' ) {
			if(!validateIndentCriteria())
				return false;
		} else if ( criteria == 'pending_indent' ) {
			if(!validatePendingIndentCriteria())
				return false;
		} else if ( criteria == 'reorder' ) {
			if(!validateReorderCriteria())
				return false;
		}
		setStoresSelected();
		if(!validateFilters()){
			return false;
		}

		if(!validateIfOnlyNumbersPresent(document.reorderCriteriaForm))
      			return false;


		if(getCsv== null || getCsv=='' || getCsv== 'undefined') {
			document.reorderCriteriaForm.method.value = "list";
		} else {
			document.reorderCriteriaForm.method.value = "exportReorderDetailsInCSV";
		}

		document.reorderCriteriaForm.submit();
		return true;
	}

	function validateConsumptionCriteria() {
		var cons_days = document.getElementById('cons_days').value;
		var to_order_days = document.getElementById('to_order_days').value;

		if(cons_days == null || cons_days.trim() == '' || isNaN(parseInt(cons_days))|| parseInt(cons_days)==0) {
			alert("Please enter the consumption days");
			document.getElementById('cons_days').focus();
			return false;
		}

		if(to_order_days == null || to_order_days.trim()== ''|| isNaN(parseInt(to_order_days)) || parseInt(to_order_days)==0) {
			alert("Please enter the number of days to order");
			document.getElementById('to_order_days').focus();
			return false;
		}

		return true;
	}

	function validateIndentCriteria() {
		var indent_type = getCheckedValue(document.reorderCriteriaForm.indent_type);
		if(indent_type == null || indent_type == ''){
			alert("Please select atleast one indent criteria");
			return false;
		} else {
			if(indent_type == 'indent_purchase') {
				var purchase_indent = document.getElementById('purchase_indent').value;
				if (purchase_indent == null || purchase_indent.trim() == '') {
					//alert("Please enter the Indent Number");
					//document.getElementById('purchase_indent').focus();
					//return false;
				}
			} else if(indent_type == 'indent_all') {
				var all_indent = document.getElementById('all_indent').value;
				if( all_indent == null || all_indent.trim() == '') {
					//alert("Please enter the Indent Number");
					//document.getElementById('all_indent').focus();
					//return false;
				}
			}
		}
		return true;
	}


	function validatePendingIndentCriteria (){
		var pending_indent_age = document.getElementById('pending_indent_age').value;
		if(pending_indent_age == null || pending_indent_age.trim() == ''){
			alert("Please enter the pending indents age");
			document.getElementById('pending_indent_age').focus();
			return false;
		}
		return true;
	}


	function validateReorderCriteria() {
		var reorder_level = getCheckedValue(document.reorderCriteriaForm.reorder_level);
		if(reorder_level == null || reorder_level.trim() == '') {
			alert("Please select atleast one reorder level criterion");
			return false;
		}
		return true;
	}


	function validateFilters() {
		if(document.getElementById('selected_stores').value == null
			|| document.getElementById('selected_stores').value.trim() == '') {
			alert("Please select atleast one store");
			if(document.getElementById('selected_stores').type != 'text') {
				document.getElementById('avlbl_stores').focus();
				if(document.getElementById('avlbl_stores').options[0].length > 0)
					document.getElementById('avlbl_stores').options[0].selected = true;
			}
			return false;
		}
		return true;
	}


	function setStoresSelected() {
		var storesSelectObj = document.getElementById("selected_stores");
		if(storesSelectObj && storesSelectObj.type != 'hidden' && storesSelectObj.options.length >0) {
			for(var i = 0 ; i<storesSelectObj.options.length; i++) {
				storesSelectObj.options[i].selected = true;
			}
		}
	}


	function uRLDecode(encodedString) {
		var output = decodeURIComponent(encodedString);
		output = output.replace('+',' ','g');
		return output;
	}

	var toolbar = {
	 	Edit : {
	 		title : "Item Purchase Details",
	 		imageSrc : "icons/View.png",
			onclick: 'openDialog',
			description: "View Purchase Details for this Item"

	 	}
	};

	function openDialog(anchor, params, id, toolbar) {
		var itemId ='';
		var storeId='';
		var identifier='';
		var stock='';
		var item_name='';
		var storesSelectObj = document.getElementById("selected_stores");
		var storeIds = [];
		if(storesSelectObj && storesSelectObj.type == 'hidden') {
			storeIds.push(storesSelectObj.value);
		} else if(storesSelectObj.type != 'hidden' && storesSelectObj.options.length >0) {
			for(var i = 0 ; i<storesSelectObj.options.length; i++) {
				storeIds.push(storesSelectObj.options[i].value);
			}
		}
		for (var paramname in params) {
			var paramvalue = params[paramname]
			if (paramname == 'itemId')
				itemId = paramvalue;
			if (paramname == 'storeId')
				storeId = paramvalue;
			if (paramname == 'identifier')
				identifier = paramvalue;
			if (paramname == 'stock')
				stock = paramvalue;
		}
		Ajax.get('./StockReorder.do?method=getItemDetails&itemId='+itemId+'&storeId='+storeIds+'&identifier='+identifier+'&stock='+stock,
			function(data, status) {
				var text = eval(data);
				var pd_table = document.getElementById("table2");
				removeRows(pd_table);
				for(var i=0;i<text.length;i++) {
					item_name=text[i].medicine_name;
					stock = text[i].stock;
					var numRows = pd_table.rows.length;
					var id = numRows ;
					var row = pd_table.insertRow(id);
					var cell;
					cell = row.insertCell(-1);
					cell.innerHTML=text[i].supplier_name;
					cell.setAttribute('style','white-space:normal');
					cell = row.insertCell(-1);
					cell.innerHTML=text[i].cost_price;
					cell = row.insertCell(-1);
					cell.innerHTML=text[i].mrp;
					if (vatPref == 'Y') {
						cell = row.insertCell(-1);
						cell.innerHTML=text[i].tax_rate;
					}
					cell = row.insertCell(-1);
					cell.innerHTML=text[i].invoice_no;
					cell = row.insertCell(-1);
					cell.innerHTML=text[i].invoice_date;
					cell = row.insertCell(-1);
					cell.innerHTML=text[i].grn_no;
					cell = row.insertCell(-1);
					cell.innerHTML=text[i].grn_date;
					cell = row.insertCell(-1);
					cell.innerHTML=text[i].po_no;
					cell = row.insertCell(-1);
					cell.innerHTML=text[i].bonus_qty;
					cell = row.insertCell(-1);
					cell.innerHTML=text[i].discount;
			 	}
				 var item_details_table = document.getElementById("table3");
				 var numRows = item_details_table.rows.length;
				 if(numRows == 1)
				   item_details_table.deleteRow(-1);
				 numRows = item_details_table.rows.length;
				 var id = numRows ;
				 var row = item_details_table.insertRow(id);
				 var cell;
				 cell = row.insertCell(-1);
				 cell.innerHTML="Item Name:";
				 cell = row.insertCell(-1);
				 cell.innerHTML=item_name;

				 cell = row.insertCell(-1);
				 cell.innerHTML="Stock Type :";
				 cell = row.insertCell(-1);
				 if(stock == 'true')
				 cell.innerHTML="Consignment";
				 else
				 cell.innerHTML="Normal";
				}
			);

		positionDialogAt(anchor);
	}

	function removeRows(table){
		var innerTableObj = table;
		var length = innerTableObj.rows.length ;
		for (var i=1; i<length; i++) {
			innerTableObj.deleteRow(1);
		}
	}

	function positionDialogAt(obj) {
		var row = getThisRow(obj);
		existingStockDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
		existingStockDialog.show();
		return false;
	}

	function subscribeKeyListeners(dialog) {
		var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
				{ fn:dialog.hide, scope:dialog, correctScope:true } );
		dialog.cfg.setProperty("keylisteners", [escKeyListener]);
	}

	var existingStockDialog = null;
	function initDialog() {
		var dialog = document.getElementById('existingstock');
		dialog.style.display = 'block';
		existingStockDialog = new YAHOO.widget.Dialog("existingstock", {
					width:"970px",
					context : ["", "tr", "br"],
					visible: false,
					modal: true,
					constraintoviewport: true
		});
		subscribeKeyListeners(existingStockDialog);
		existingStockDialog.render();
	}

	function userHasRights() {
 		if(gRoleId != 1 && gRoleId != 2) {
 			if(deptId == "" && default_store == "No") {
 				alert("There is no assigned store, hence you dont have any access to this screen");
 				document.getElementById("masterTable").style.display = 'none';
 				document.getElementById("resultFormTopTable").style.display = 'none';
				document.getElementById("buttonDiv").style.display = 'none';
 				return false;
 			}
 		}
 		if(document.getElementById('avlbl_stores') && !(document.getElementById('avlbl_stores').options.length >0 && document.getElementById('avlbl_stores').options[0].text!='')) {
			alert("There is no assigned super store, hence you dont have any access to this screen");
			document.getElementById("masterTable").style.display = 'none';
			document.getElementById("resultFormTopTable").style.display = 'none';
			document.getElementById("buttonDiv").style.display = 'none';
			return false;
		}
		if(document.getElementById('avlbl_stores') &&  default_store == 'Yes' && isSuperStore == 'N' && !(document.getElementById('avlbl_stores').options.length >0 && document.getElementById('avlbl_stores').options[0].text!='')) {
			alert("There is no assigned super store, hence you dont have any access to this screen");
			document.getElementById("masterTable").style.display = 'none';
			document.getElementById("resultFormTopTable").style.display = 'none';
			document.getElementById("buttonDiv").style.display = 'none';
			return false;
		}

		if(!document.getElementById('avlbl_stores') && deptId != "" &&  default_store == 'Yes' && isSuperStore == 'N'){
			alert("There is no assigned super store, hence you dont have any access to this screen");
			document.getElementById("masterTable").style.display = 'none';
			document.getElementById("resultFormTopTable").style.display = 'none';
			document.getElementById("buttonDiv").style.display = 'none';
			return false;
		}
		return true;
	}

	function init( criteria ) {
		if(!userHasRights()){
			return false;
		} else {
			loadServiceSubGroup();
			createToolbar(toolbar);
			initDialog();
		}
		if(criteria == null || criteria == '') {
			clearSearch();
			document.getElementById('exclude_poitem').checked = true;
			document.getElementById('criteria_consumption').click();
			document.getElementById('cons_days').value  = 30;
			document.getElementById('to_order_days').value = 30;
			if(document.getElementById('dept_id').value != null && document.getElementById('dept_id').value != ''
				&& document.getElementById('selected_stores').type != 'hidden') {
				var listFld = document.getElementById('selected_stores');
				var avbFld =  document.getElementById('avlbl_stores');
				for (var k = 0; k < avbFld.length; k++) {
					if(avbFld.options[k].value == document.getElementById('dept_id').value) {
						avbFld[k].selected = true;
					}
				}
				moveSelectedOptions(avbFld, listFld, 'from');
				addToStoresDropDown();
				setPreferredSupplier();
			}
			addToStoresDropDown();
		} else {
			if( criteria == 'cons') {
				document.getElementById('criteria_consumption').click();
			} else if ( criteria == 'indent' ) {
				document.getElementById('criteria_indent').click();
			} else if ( criteria == 'pending_indent' ) {
				document.getElementById('criteria_pending_indent').click();
			} else if ( criteria == 'reorder' ) {
				document.getElementById('criteria_reorder_level').click();
			}
			var argName = new Array();
			var argVal = new Array();
			if (window.location != null && window.location.search.length > 1) {
			//parse the URL
				var urlParameters = window.location.search.substring(1);
				var parameterPair = urlParameters.split('&');
				//get the key-value pairs.
				for (var i = 0; i < parameterPair.length; i++) {
					var pos = parameterPair[i].indexOf('=');
					argName[i] = parameterPair[i].substring(0, pos);
					argVal[i] = uRLDecode(parameterPair[i].substring(pos + 1));
				}
			}
		    document.getElementById('exclude_poitem').checked = false;
			for (var i = 0; i < argName.length; i++) {
				if (argName[i] == 'indent_type') {
					var purchaseType = argVal[i].toString();
					if(purchaseType == 'indent_purchase') {
						document.getElementById('indent_type_purchase').click();
					} else if( purchaseType = 'indent_all') {
						document.getElementById('indent_type_all').click();
					}
				} else if(argName[i] == 'purchase_indent') {
					document.getElementById('purchase_indent').value = argVal[i].toString();
				} else if( argName[i] == 'all_indent' ) {
					document.getElementById('all_indent').value = argVal[i].toString();
				} else if(argName[i] == 'cons_days') {
					document.getElementById('cons_days').value = argVal[i].toString();
				} else if(argName[i] == 'to_order_days') {
					document.getElementById('to_order_days').value = argVal[i].toString();
				} else if(argName[i] == 'pending_indent_age') {
					document.getElementById('pending_indent_age').value = argVal[i].toString();
				} else if(argName[i] == 'reorder_level') {
					document.getElementById(argVal[i].toString()).click();
				} else if(argName[i] == 'sales_quantity') {
					document.getElementById('sales_quantity').value = argVal[i].toString();
				} else if(argName[i] == 'sale_days') {
					document.getElementById('sale_days').value = argVal[i].toString();
				} else if(argName[i] == 'exclude_poitem') {
					if( argVal[i].toString() == 'on') {
						document.getElementById('exclude_poitem').checked = true;
					}
				} else if(argName[i] == 'preferred_supplier') {
					setSelectedIndex(document.getElementById('preferred_supplier'), argVal[i].toString());
				} else if(argName[i] == 'service_group_id') {
					setSelectedIndex(document.getElementById('service_group_id'), argVal[i].toString());
					loadServiceSubGroup();
					for (var k = 0; k < argName.length; k++) {
						if (argName[k] == 'service_sub_group_id') {
							setSelectedIndex(document.getElementById('service_sub_group_id'), argVal[k].toString());
						}
					}
				} else if(argName[i] == 'service_sub_group_id') {
					setSelectedIndex(document.getElementById('service_sub_group_id'), argVal[i].toString());
				} else if (argName[i] == 'purchase_flag_date_dt') {
					document.getElementById("purchase_flag_date_dt").value = argVal[i].toString();
				}else if(argName[i] == 'selected_stores') {
					var fCount = 0;
					var gCount = 0;
					var fldArray = new Array();
					var grpArray = new Array();
					for (var k = 0; k < argName.length; k++) {
						if (argName[k] == 'selected_stores') {
							fldArray[fCount++] = argVal[k];
						}
					}
					var listFld = document.getElementById('selected_stores');
					var avbFld =  document.getElementById('avlbl_stores');
					if(document.getElementById('selected_stores').type != 'hidden') {
						for (var k = 0; k < listFld.length; k++) {
							listFld[k].selected = true;
						}
						moveSelectedOptions(listFld, avbFld, 'from');
						for (var h = 0; h < fldArray.length; h++) {
							for (var k = 0; k < avbFld.length; k++) {
								if (fldArray[h].toString() == avbFld[k].value) {
									avbFld[k].selected = true;

								}
							}
						}
						moveSelectedOptions(avbFld, listFld, 'from');
						addToStoresDropDown();
					} else {
						addToStoresDropDown();
					}
				}
			}
			setPreferredSupplier();
			selectAllItems();
		}
	}

	function setPreferredSupplier(){
		if(document.getElementById('supplier_id')!= null && document.getElementById('supplier_id')!= '') {
			setSelectedIndex(document.getElementById('supplier_id'), document.getElementById('preferred_supplier').value);
		}
	}

</script>
</body>

</html>
