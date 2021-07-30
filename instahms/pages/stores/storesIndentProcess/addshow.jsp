<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="storemgmt.processindentlist.addshow.title"/></title>
<c:set var="prefReceive" value="<%= GenericPreferencesDAO.getGenericPreferences().getRecTransIndent() %>"/>
<c:set var="allowDecimalsForQty" value="<%= GenericPreferencesDAO.getGenericPreferences().getAllowdecimalsforqty()%>" />

<style>
	.newDialogMask {
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

<insta:link type="js" file="hmsvalidation.js"/>
<insta:link type="js" file="ajax.js"/>
<insta:link type="js" file="stores/processindent.js"/>
<script>

	var deptId = '${ifn:cleanJavaScript(dept_id)}';
	var gRoleId = '${roleId}';
	var accessstores = '${multiStoreAccess}';
	var allowDecimalsForQty = '${allowDecimalsForQty}';

	function changeApprovalReject(value,id) {
		document.getElementById("approve_reject"+id).value = value;
	}

	function validateFields() {
		if(document.forms[0].status_main.value == 'P'){
			var val = false;
			if(checkEmptyRows()) {
				showMessage("js.stores.mgmt.indents.itempurchasestatus.update");
				val = true;
				document.forms[0].emptyrows.value = 'Y';
			}
		}
		document.forms[0].submit();
	}

	function checkstoreallocation() {
 		if(gRoleId != 1 && gRoleId != 2 && accessstores != 'A') {
 			if(deptId == "") {
 			showMessage("js.stores.mgmt.noassignedstore.notaccessthisscreen");
 			document.getElementById("storecheck").style.display = 'none';
 			}
 		}
	}

</script>
<insta:js-bundle prefix="stores.mgmt.indents"/>
</head>
<c:set var="hasResults" value="${not empty indentlist}"/>

<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="P" value="Permanent"/>
<c:set target="${statusDisplay}" property="C" value="Consumable"/>
<c:set target="${statusDisplay}" property="L" value="Loaneable"/>
<c:set target="${statusDisplay}" property="R" value="Retail Only"/>

<jsp:useBean id="indentStatus" class="java.util.HashMap"/>
<c:set target="${indentStatus}" property="O" value="Open"/>
<c:set target="${indentStatus}" property="A" value="Approved"/>
<c:set target="${indentStatus}" property="R" value="Rejected"/>
<c:set target="${indentStatus}" property="X" value="Cancelled"/>
<c:set target="${indentStatus}" property="P" value="Processed"/>
<c:set target="${indentStatus}" property="C" value="Closed"/>

<jsp:useBean id="indentType" class="java.util.HashMap"/>
<c:set target="${indentType}" property="S" value="Stock Transfer"/>
<c:set target="${indentType}" property="U" value="Dept / Ward Issue"/>

<body onload="initDialog(); checkstoreallocation();" class="yui-skin-sam">
<div id="storecheck" style="display: block;" >
<form action="StoresIndentProcess.do" method="POST">
	<input type="hidden" name="_method" value="update"/>
	<input type="hidden" name="indentNo" value="${indentdetails.map.indent_no}"/>
	<input type="hidden" id="dialogId" value=""/>
	<input type="hidden" id="emptyrows" name="emptyrows" value="N"/>
	<h1> <insta:ltext key="storemgmt.processindentlist.addshow.processindent"/> </h1>
	<insta:feedback-panel/>

	<fieldset class="fieldSetBorder" >
	   <legend class="fieldSetLabel">${indentType[indentdetails.map.indent_type] }</legend>
		 <table   class="formtable" >
		 	<tr>
				<td class="formlabel"><insta:ltext key="storemgmt.processindentlist.addshow.indentno"/>:</td>
				<td class="forminfo">${indentdetails.map.indent_no}</td>
				<td class="formlabel"><insta:ltext key="storemgmt.processindentlist.addshow.raisedby"/>:</td>
				<td class="forminfo" align="left">${indentdetails.map.requester_name}</td>
				<td class="formlabel"><insta:ltext key="storemgmt.processindentlist.addshow.indentstore"/>:</td>
				<td class="forminfo" align="left">${indentdetails.map.store_name}</td>
				<td class="formlabel"><insta:ltext key="storemgmt.processindentlist.addshow.status"/>:</td>
				<td class="forminfo">
					<c:choose>
						<c:when test="${indentdetails.map.status eq 'A'}">
							<select name="status_main" class="dropdown">
								<option value="P" ${indentdetails.map.status=='P'? 'selected' : ''}><insta:ltext key="storemgmt.processindentlist.addshow.processed"/></option>
								<option value="X" ${indentdetails.map.status=='X'? 'selected' : ''}><insta:ltext key="storemgmt.processindentlist.addshow.cancelled"/></option>
							</select>
						</c:when>
						<c:otherwise>
							${indentStatus[indentdetails.map.status]}
						</c:otherwise>
					</c:choose>
				</td>
			</tr>
			<tr>
				<td class="formlabel"></td>
				<td></td>
				<td colspan="2">
					<c:choose>
						<c:when test="${indentdetails.map.indent_type eq 'U'}">
							<c:choose>
								<c:when test="${indentdetails.map.location_type eq 'D'}">
									 <table>
										<tr>
											<td class="formlabel"><insta:ltext key="storemgmt.processindentlist.addshow.dept"/>:</td>
											<td class="forminfo">${indentdetails.map.dept_name}</td>
										</tr>
									 </table>
								</c:when>
								<c:otherwise>
									<table>
										<tr>
											<td class="formlabel"><insta:ltext key="storemgmt.processindentlist.addshow.ward"/>:</td>
											<td class="forminfo">${indentdetails.map.ward_name}</td>
										</tr>
									</table>
								</c:otherwise>
							</c:choose>
						</c:when>
						<c:otherwise>
							<table>
								<tr>
									<td class="formlabel" align="left"><insta:ltext key="storemgmt.processindentlist.addshow.requestingstore"/>:</td>
									<td class="forminfo">${ifn:cleanHtml(storeName)}</td>
								</tr>
							</table>
						</c:otherwise>
					</c:choose>
				</td>
				<c:choose>
					<c:when test="${indentdetails.map.status eq 'O' || indentdetails.map.status eq 'C'}">
						<td class="formlabel"><insta:ltext key="storemgmt.processindentlist.addshow.expecteddate"/>:</td>
						<td class="forminfo"><fmt:formatDate value="${indentdetails.map.expected_date}" pattern="dd-MM-yyyy" var="expecteddt"/>
						<fmt:formatDate value="${indentdetails.map.expected_date}" pattern="HH:mm" var="expectedtime"/>
						<c:out value="${expecteddt} ${expectedtime}"/></td>
						<td class="formlabel"><insta:ltext key="storemgmt.processindentlist.addshow.reason"/>:</td>
						<td class="forminfo">
							<c:if test="${indentdetails.map.status eq 'A'}">${indentdetails.map.approver_remarks}</c:if>
							<c:if test="${indentdetails.map.status eq 'C'}">${indentdetails.map.closure_reasons}</c:if>
						</td>
					</c:when>
					<c:otherwise>
						<td class="formlabel"><insta:ltext key="storemgmt.processindentlist.addshow.expecteddate"/>:</td>
						<td class="forminfo"><fmt:formatDate value="${indentdetails.map.expected_date}" pattern="dd-MM-yyyy" var="expecteddt"/>
						<fmt:formatDate value="${indentdetails.map.expected_date}" pattern="HH:mm" var="expectedtime"/>
							${expecteddt} ${expectedtime}</td>
						<td class="formlabel"><insta:ltext key="storemgmt.processindentlist.addshow.reason"/>:</td>
						<td><input type="text" size="30" name="remarks" maxlength="100" value='<c:out value="${indentdetails.map.remarks}"/>'></td>
					</c:otherwise>
				</c:choose>
			</tr>
		 </table>
	</fieldset>
	 <fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel"><insta:ltext key="storemgmt.processindentlist.addshow.itemslist"/></legend>
		<table id="indentItemListTab" class="dataTable" width="100%">
				<tr>
					<th width="45%"><insta:ltext key="storemgmt.processindentlist.addshow.item"/></th>
					<th width="10%"><insta:ltext key="storemgmt.processindentlist.addshow.required"/></th>
					<th width="10%"><insta:ltext key="storemgmt.processindentlist.addshow.pending"/></th>
					<th width="10%"><insta:ltext key="storemgmt.processindentlist.addshow.issued"/></th>
					<th width="10%"><insta:ltext key="storemgmt.processindentlist.addshow.avblinstore"/></th>
					<c:if test="${indentdetails.map.indent_type eq 'S'}">
						<th width="10%"><insta:ltext key="storemgmt.processindentlist.addshow.req.avblinstore"/></th>
					</c:if>
					<th width="10%"><insta:ltext key="storemgmt.processindentlist.addshow.poraised.notrecd"/></th>
					<th width="10%"><insta:ltext key="storemgmt.processindentlist.addshow.pkgsize"/></th>
					<th><insta:ltext key="storemgmt.processindentlist.addshow.unituom"/></th>
					<th><insta:ltext key="storemgmt.processindentlist.addshow.batchno.itemqty"/></th>
					<th width="5%" title="flag for Purchase"><insta:ltext key="storemgmt.processindentlist.addshow.purchase"/></th>
					<th width="5%"></th>
				</tr>
				<c:set var="i" value="0"/>
				<c:forEach var="indent" items="${indentlist}">
					<c:choose>
						<c:when test="${indentdetails.map.status eq 'A'}">
							<c:choose>
							<c:when test="${(indent.map.status eq 'A'  &&  indent.map.medicine_id != 0) }">
								<tr class=${indent.map.medicine_id == 0 ? 'NoItemIdClass':''}>
									<td>
										<div id="dialog${i}" style="display: none; max-height:400px; overflow:auto;overflow-x: hidden;">
											<div class="bd">
												<table class="detailFormTable" cellpadding="0" cellspacing="0" id="itemIdentifierTab${i}">
													<tr>
														<th width="50px"><insta:ltext key="storemgmt.processindentlist.addshow.itemqty"/></th>
														<th width="50px"><insta:ltext key="storemgmt.processindentlist.addshow.batchno"/></th>
														<th width="50px"><insta:ltext key="storemgmt.processindentlist.addshow.avblqty"/></th>
														<th width="70px"><insta:ltext key="storemgmt.processindentlist.addshow.expdt"/></th>
														<th width="50px"><insta:ltext key="storemgmt.processindentlist.addshow.pkgsize"/></th>
														<th width="50px">&nbsp;</th>
													</tr>
													<c:forEach items="${identifierList}" var="identity">
														<c:if test="${identity['MEDICINE_ID'] eq indent.map.medicine_id}">
															<tr><td><input type="hidden" id="identification${i}" value="${identity['ITEM_IDENTIFICATION']}"/></td></tr>
															<c:choose>
																<c:when test="${identity['ITEM_IDENTIFICATION'] eq 'S'}">
																	<c:choose>
																	<c:when test="${not empty identity['IDENTIFIER_LIST']}">
																	<tr><td>Select All<input type="checkbox" id="selectall${i}" onclick="selectAllItems(${i});setAllCheckBoxEdited(${i});"></td>
																		<td colspan="5"><insta:ltext key="storemgmt.processindentlist.addshow.itemcount"/>:<input type="text" id="itemcount${i}" value="0" class="number" readonly></td>
																	</tr>
																		<c:forEach items="${identity['IDENTIFIER_LIST']}" var="identifierList" varStatus="status">
																			<c:set var="j" value="${status.index}"/>
																			<tr>
																				<td>
																					<input type="checkbox" id="item_check${j}" name="item_check${i}" value="0" onclick="assignValue(this,item_qty${i}${j},itemcount${i});setItemCheckBoxEdited();"/>
																				</td>
																				<td><input type="text" name="batch_no${i}"
																					value="${identifierList.BATCH_NO}" disabled tabindex="-1" style="width: 50px;"/>
																				<td><input type="text" name="qty_aval${i}"
																					value="${identifierList.QTY}" disabled tabindex="0" style="width: 50px;"/>
																				</td>
																				<td><input type="text" name="exp_date${i}"
																					value="${identifierList.EXP_DT}" disabled tabindex="1" style="width: 70px;"/>
																				</td>
																				<td><input type="text" name="pkg_size${i}"
																					value="${identifierList.ISSUE_BASE_UNIT}" disabled tabindex="2" style="width: 50px;"/>
																					<input type="hidden" name="item_identifier${i}" value="${identifierList.BATCH_NO}"/>
																					<input type="hidden" name="item_batch_id${i}" value="${identifierList.ITEM_BATCH_ID}"/>
																					<input type="hidden" name="item_qty${i}" id="item_qty${i}${j}" value="0" onchange="setEdited()"/>
																					<input type="hidden" name="item_qty_avlbl${i}" value="${identifierList.QTY}"/>
																					<input type="hidden" name="item_qty_in_use${i}" value="${identifierList.QTY_IN_USE}"/>
																				</td>
																				<td>&nbsp;</td>
																			</tr>
																		</c:forEach>
																	<tr>
																		<td colspan="6" >
																			<input type="button" value="Ok" name="okButton${i}"/>
																			<input type="button" value="Cancel" name="cancelButton${i}"/>
																			<input type="button" value="&lt;&lt;Prev" name="prevButton${i}"/>
																			<input type="button" value="Next&gt;&gt;" name="nextButton${i}"/>
																		</td>

																	</tr>
																	</c:when>
																	<c:otherwise>
																		 <tr>
																		 	<td colspan="6" align="center"><b><insta:ltext key="storemgmt.processindentlist.addshow.nostock"/></b></td>
																		</tr>
																		<tr>
																			<td colspan="6">
																				<input type="button" value="Ok" name="okButton${i}"/>
																				<input type="button" value="Cancel" name="cancelButton${i}"/>
																				<input type="button" value="&lt;&lt;Prev" name="prevButton${i}"/>
																				<input type="button" value="Next&gt;&gt;" name="nextButton${i}"/>
																			</td>
																		</tr>
																	</c:otherwise>
																	</c:choose>
																</c:when>
																<c:otherwise>
																	<c:choose>
																	<c:when test="${not empty identity['IDENTIFIER_LIST']}">
																		<c:forEach items="${identity['IDENTIFIER_LIST']}" var="identifierList" varStatus="status">
																			<c:set var="j" value="${status.index}"/>
																			<tr>
																				<td>
																					<input type="text" name="item_qty${i}" id="item_qty${i}${j}" value="0" style="text-align:right;width: 60px;" onchange="setEdited()"/>
																				</td>
																				<td><input type="text" name="batch_no${i}"
																					value="${identifierList.BATCH_NO}" disabled tabindex="-1" style="width: 50px;"/>
																				</td>
																				<td><input type="text" name="qty_aval${i}"
																					value="${identifierList.QTY}" disabled tabindex="0" style="width: 50px;"/>
																				</td>
																				<td><input type="text" name="exp_date${i}"
																					value="${identifierList.EXP_DT}" disabled tabindex="1" style="width: 70px;"/>
																				</td>
																				<td>
																					<input type="text" name="pkg_size${i}"
																						value="${identifierList.ISSUE_BASE_UNIT}" disabled tabindex="2" style="width: 50px;"/>
																					<input type="hidden" name="item_identifier${i}" value="${identifierList.BATCH_NO}"/>
																					<input type="hidden" name="item_batch_id${i}" value="${identifierList.ITEM_BATCH_ID}"/>
																					<input type="hidden" name="item_qty_avlbl${i}" value="${identifierList.QTY}"/>
																					<input type="hidden" name="item_qty_in_use${i}" value="${identifierList.QTY_IN_USE}"/>
																				</td>
																				<td>&nbsp;</td>
																			</tr>
																		</c:forEach>
																		<tr>
																			<td colspan="6" >
																				<input type="button" value="Ok" name="okButton${i}"/>
																				<input type="button" value="Cancel" name="cancelButton${i}"/>
																				<input type="button" value="&lt;&lt;Prev" name="prevButton${i}"/>
																				<input type="button" value="Next&gt;&gt;" name="nextButton${i}"/>
																			</td>
																		</tr>
																	</c:when>
																	<c:otherwise>
																		 <tr>
																		 	<td colspan="6" align="center"><b><insta:ltext key="storemgmt.processindentlist.addshow.nostock"/></b></td>
																		 </tr>
																		 <tr>
																		 	<td colspan="6">
																			 	<input type="button" value="Ok" name="okButton${i}"/>
																				<input type="button" value="Cancel" name="cancelButton${i}"/>
																				<input type="button" value="&lt;&lt;Prev" name="prevButton${i}"/>
																				<input type="button" value="Next&gt;&gt;" name="nextButton${i}"/>
																			</td>
																		 </tr>
																	</c:otherwise>
																	</c:choose>
																</c:otherwise>
															</c:choose>
														</c:if>
													</c:forEach>
												</table>
											</div>
										</div>
									<label id="itemnamelbl${i}"><c:out value="${indent.map.medicine_name}"/></label>
										<input type="hidden" name="itemid" id="itemid${i}" value="${indent.map.medicine_id}"/>
									</td>
									<td align="right"><label id="itemrequiredqtylbl${i}">${indent.map.qty}</label></td>
									<td align="right"><label id="itempendingqtylbl${i}">${indent.map.qty-indent.map.qty_fullfilled}</label></td>
									<td align="right"><label id="itemissuedqtylbl${i}">${indent.map.qty_fullfilled}</label></td>
									<td align="right"><label id="itemavaliableqtylbl${i}">${indent.map.availableqty}</label></td>
									<c:if test="${indentdetails.map.indent_type eq 'S'}">
										<td align="right"><label id="itemreq_avaliableqtylbl${i}">${indent.map.req_availableqty}</label></td>
									</c:if>
									<td align="right"><label id="itemporaisedqtylbl${i}">${(indent.map.poqty + indent.map.pobqty)< 0 ?0 :(indent.map.poqty + indent.map.pobqty) }</label></td>
									<td>${indent.map.issue_base_unit}</td>
									<td>${indent.map.issue_units}</td>
									<td><label id="batchlbl${i}"></label></td>
									<td><input type="checkbox" name="purchase" id="purchase${i }" ${indent.map.purchase_flag eq 'Y' ? 'checked' : '' }
									    onclick="selectpur('${i }')"/>
									<input type="hidden" name="oldPurchaseSelect" id="oldPurchaseSelect${i}" value="${indent.map.purchase_flag eq 'Y'}"/>
									<input type="hidden" name="purchaseSelect" id="purchaseSelect${i}" value="${indent.map.purchase_flag eq 'Y'}"/>
									<input type="hidden" name="medicine_name" id="medicine_name${i }" value="${fn:escapeXml(indent.map.medicine_name)}">
									<input type="hidden" name="indentSelect" id="indentSelect${i}" value="false"/></td>
									<td>
										<img class="button" name="itemrow" id="itemrow${i}" src="${cpath}/icons/Edit.png" onclick="showIndentifierDialog(${i})">
										<input type="hidden" name="disableDialog" id="disableDialog${i}" value="false">
									</td>
								</tr>
							</c:when>
							<c:when test="${indent.map.status eq 'A' && indent.map.medicine_id == 0}">
								<tr>

									<td><c:out value="${indent.map.medicine_name}"/></td>
									<td align="right"><c:out value="${indent.map.qty}"></c:out></td>
									<td align="right">${indent.map.qty-indent.map.qty_fullfilled}</td>
									<td align="right">${indent.map.qty_fullfilled}</td>
									<td align="right">${ifn:afmt(indent.map.availableqty)}</td>
									<c:if test="${indentdetails.map.indent_type eq 'S'}">
										<td align="right">${ifn:afmt(indent.map.req_availableqty)}</td>
									</c:if>
									<td align="right">${(indent.map.poqty + indent.map.pobqty)< 0 ?0 :(indent.map.poqty + indent.map.pobqty) }</td>
									<td>${indent.map.issue_base_unit}</td>
									<td>${indent.map.issue_units}</td>
									<td><label id="batchlbl${i}"></label></td>
									<td><input type="checkbox" name="purchase" id="purchase${i }" ${indent.map.purchase_flag eq 'Y' ? 'checked' : '' }
											onclick="selectpur('${i }')">
									<input type="hidden" name="oldPurchaseSelect" id="oldPurchaseSelect${i}" value="${indent.map.purchase_flag eq 'Y'}"/>
									<input type="hidden" name="purchaseSelect" id="purchaseSelect${i}" value="${indent.map.purchase_flag eq 'Y'}"/>
									<input type="hidden" name="medicine_name" id="medicine_name${i }" value="${fn:escapeXml(indent.map.medicine_name)}">
									<input type="hidden" name="itemid" id="itemid${i}" value="${indent.map.medicine_id}"/>
									<input type="hidden" name="indentSelect" id="indentSelect${i}" value="false"/></td>
									<td>
										<img src="${cpath}/icons/Edit1.png" name="itemrow" id="itemrow${i}">
										<input type="hidden" name="disableDialog" id="disableDialog${i}" value="true">
									</td>
								</tr>
							</c:when>
							<c:otherwise>
								<tr >
									<td>
										<c:choose>
											<c:when test="${indent.map.status eq 'R'}">
												<img class="flag" src="${cpath}/images/red_flag.gif"/>
											</c:when>
											<c:when test="${indent.map.status eq 'C'}">
												<img class="flag" src="${cpath}/images/green_flag.gif"/>
											</c:when>
										</c:choose>
										<c:out value="${indent.map.medicine_name}"/>
									</td>
									<td align="right">${indent.map.qty}</td>
									<td align="right">${indent.map.qty-indent.map.qty_fullfilled}</td>
									<td align="right">${indent.map.qty_fullfilled}</td>
									<td align="right">${indent.map.availableqty}</td>
									<c:if test="${indentdetails.map.indent_type eq 'S'}">
										<td align="right">${indent.map.req_availableqty}</td>
									</c:if>
									<td align="right">${(indent.map.poqty + indent.map.pobqty)< 0 ?0 :(indent.map.poqty + indent.map.pobqty) }</td>
									<td>${indent.map.issue_base_unit}</td>
									<td>${indent.map.issue_units}</td>
									<td><label id="batchlbl${i}"></label></td>
									<td><input type="checkbox" name="purchase" id="purchase${i }" disabled ${indent.map.purchase_flag eq 'Y' ? 'checked' : '' }
											onclick="selectpur('${i }')">
									<input type="hidden" name="oldPurchaseSelect" id="oldPurchaseSelect${i}" value="${indent.map.purchase_flag eq 'Y'}"/>
									<input type="hidden" name="purchaseSelect" id="purchaseSelect${i}" value="${indent.map.purchase_flag eq 'Y'}"/>
									<input type="hidden" name="medicine_name" id="medicine_name${i }" value="${fn:escapeXml(indent.map.medicine_name)}">
									<input type="hidden" name="itemid" id="itemid${i}" value="${indent.map.medicine_id}"/>
									<input type="hidden" name="indentSelect" id="indentSelect${i}" value="false"/></td>
									<td>
										<img  src="${cpath}/icons/Edit1.png" name="itemrow" id="itemrow${i}">
										<input type="hidden" name="disableDialog" id="disableDialog${i}" value="true">
									</td>
								</tr>
							</c:otherwise>
						</c:choose>
					</c:when>
					<c:otherwise>
						<tr>
							<td><c:out value="${indent.map.medicine_name}"/></td>
							<td align="right">${indent.map.qty}</td>
							<td align="right">${indent.map.qty-indent.map.qty_fullfilled}</td>
							<td align="right">${indent.map.qty_fullfilled}</td>
							<td align="right"><c:out value="${ifn:afmt(indent.map.availableqty)}"/></td>
							<c:if test="${indentdetails.map.indent_type eq 'S'}">
								<td align="right"><c:out value="${ifn:afmt(indent.map.req_availableqty)}"/></td>
							</c:if>
							<td align="right">${(indent.map.poqty + indent.map.pobqty)< 0 ?0 :(indent.map.poqty + indent.map.pobqty) }</td>
							<td>${indent.map.issue_base_unit}</td>
							<td>${indent.map.issue_units}</td>
							<td><label id="batchlbl${i}"></label></td>
							<td><input type="checkbox" name="purchase" id="purchase${i }" disabled ${indent.map.purchase_flag eq 'Y' ? 'checked' : '' }
									onclick="selectpur('${i }')">
							<input type="hidden" name="oldPurchaseSelect" id="oldPurchaseSelect${i}" value="${indent.map.purchase_flag eq 'Y'}"/>
							<input type="hidden" name="purchaseSelect" id="purchaseSelect${i}" value="${indent.map.purchase_flag eq 'Y'}"/>
							<input type="hidden" name="medicine_name" id="medicine_name${i }" value="${fn:escapeXml(indent.map.medicine_name)}">
							<input type="hidden" name="itemid" id="itemid${i}" value="${indent.map.medicine_id}"/>
							<input type="hidden" name="indentSelect" id="indentSelect${i}" value="false"/></td>
							<td>
								<input type="button" value="#" disabled/>
								<input type="hidden" name="disableDialog" id="disableDialog${i}" value="true">
							</td>
						</tr>
					</c:otherwise>
					</c:choose>
					<c:set var="i" value="${i+1}"/>
				</c:forEach>
			</table>
	</fieldset>
	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText"><insta:ltext key="storemgmt.processindentlist.addshow.rejecteditem"/></div>
		<c:if test="${prefReceive eq 'Y'}">
			<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
			<div class="flagText"><insta:ltext key="storemgmt.processindentlist.addshow.receiveditem"/></div>
		</c:if>
  	</div>

	<div class="screenActions">
	 	<c:if test="${indentdetails.map.status eq 'A'}">
			<input type="button" value="Save" name="save" class="button" onclick="return validateFields()" >
		</c:if>
		<a href="${cpath }/stores/StoresIndentProcess.do?_method=list&status=A&sortOrder=indent_no&sortReverse=true"><insta:ltext key="storemgmt.processindentlist.addshow.backtodashboard"/></a>
	</div>

	<dl>
   		<dt style="padding-left: 2.5em;padding-bottom: 0.5em;font-weight: bold;" > <insta:ltext key="storemgmt.processindentlist.addshow.nostock.suspectreason"/></dt>
   		<dd><insta:ltext key="storemgmt.processindentlist.addshow.stocknotavailable"/></dd>
   		<dd><insta:ltext key="storemgmt.processindentlist.addshow.cannotprocess"/></dd>
    </dl>

</form>
</div>
</body>
</html>
