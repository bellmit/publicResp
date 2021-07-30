<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/functions" prefix = "fn" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="allowDecimalsForQty"
	value="<%= GenericPreferencesDAO.getGenericPreferences().getAllowdecimalsforqty()%>" />

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title><insta:ltext key="storeprocurement.receivetransferedindent.addedit.receiveindent.instahms"/></title>
	<insta:link type="script" file="hmsvalidation.js" />
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="js" file="ajax.js" />
	<insta:link type="js" file="stores/receiveindent.js" />
	<insta:js-bundle prefix="stores.mgmt"/>
<script>
		var toolbarOptions = getToolbarBundle("js.sales.todayspatientappointments.toolbar");
		var deptId = '${ifn:cleanJavaScript(dept_id)}';
		var gRoleId = '${roleId}';
		var accessstores = '${multiStoreAccess}';
		var allowDecimalsForQty = '${allowDecimalsForQty}';		
		var receiveStatus = '${genPrefs.recTransIndent}';
</script>
</head>
<c:set var="receiveText">
<insta:ltext key="storeprocurement.receivetransferedindent.addedit.receive"/>
</c:set>
<c:set var="hasResults" value="${not empty indentlist}" />
<jsp:useBean id="indentStatus" class="java.util.HashMap" />
<c:set target="${indentStatus}" property="O" value="Open" />
<c:set target="${indentStatus}" property="C" value="Closed" />
<c:set target="${indentStatus}" property="A" value="Approved" />
<c:set target="${indentStatus}" property="R" value="Rejected" />
<c:set target="${indentStatus}" property="P" value="Processed" />
<c:set target="${indentStatus}" property="X" value="Cancelled" />
<jsp:useBean id="indentType" class="java.util.HashMap" />
<c:set target="${indentType}" property="R" value="Request for new Items" />
<c:set target="${indentType}" property="S" value="Stock Transfer" />
<c:set target="${indentType}" property="U" value="Dept / Ward Issue" />

<body onload="initDialog(); checkstoreallocation();" class="yui-skin-sam">

<div id="storecheck" style="display: block;">
	<form name="mainform" action="StoresReceiveIndents.do" method="POST">
		<input type="hidden" name="_method" value="update" />
		<input type="hidden" name="indentNo" value="${indentdetails.map.indent_no}"/>
		<input type="hidden" id="dialogId" value="" />
		<h1><insta:ltext key="storeprocurement.receivetransferedindent.addedit.receivetransferedindent"/></h1>
		<insta:feedback-panel/>

		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">${indentType[indentdetails.map.indent_type] }</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel"><insta:ltext key="storeprocurement.receivetransferedindent.addedit.indentno"/>:</td>
					<td class="forminfo">${indentdetails.map.indent_no}</td>
					<td class="formlabel"><insta:ltext key="storeprocurement.receivetransferedindent.addedit.raisedby"/>:</td>
					<td class="forminfo" align="left">${indentdetails.map.requester_name}</td>
					<td class="formlabel"><insta:ltext key="storeprocurement.receivetransferedindent.addedit.requestingstore"/>:</td>
					<td class="forminfo">
						<input type="hidden" id="recv_store" name="recv_store" value="${indentdetails.map.dept_from}" />
						<c:choose>
							<c:when test="${indentdetails.map.indent_type eq 'S'}">${ifn:cleanHtml(deptName)}</c:when>
							<c:otherwise>${indentdetails.map.dept_name}</c:otherwise>
						</c:choose>
					</td>
					<td class="formlabel"><insta:ltext key="storeprocurement.receivetransferedindent.addedit.status"/>:</td>
					<td class="forminfo">${indentStatus[indentdetails.map.status]}</td>
				</tr>

				<tr>
					<td class="formlabel"></td>
					<td></td>
					<td colspan="2">
						<table>
							<tr>
								<td class="formlabel"><insta:ltext key="storeprocurement.receivetransferedindent.addedit.indentstore"/>:</td>
								<td class="forminfo">${ifn:cleanHtml(storeName)}
									<input type="hidden" name="send_store" value="${indentdetails.map.indent_store}"/>
								</td>
							</tr>
						</table>
					</td>
					<td class="formlabel"><insta:ltext key="storeprocurement.receivetransferedindent.addedit.expecteddate"/>:</td>
					<td class="forminfo">
						<fmt:formatDate value="${indentdetails.map.expected_date}" pattern="dd-MM-yyyy"
						var="expecteddt" />
						<fmt:formatDate value="${indentdetails.map.expected_date}" pattern="HH:mm" var="expectedtime" />
						<c:out value="${expecteddt} ${expectedtime}" />
					</td>
					<td class="formlabel"><insta:ltext key="storeprocurement.receivetransferedindent.addedit.reason"/>:</td>
					<td class="forminfo">
						<c:if test="${indentdetails.map.status eq 'A'}">${indentdetails.map.approver_remarks}</c:if>
						<c:if test="${indentdetails.map.status eq 'C'}">${indentdetails.map.closure_reasons}</c:if>
					</td>
				</tr>
			</table>
		</fieldset>

		<table id="indentItemListTab" class="detailList" width="100%">
			<tr>
				<th width="45%"><insta:ltext key="storeprocurement.receivetransferedindent.addedit.item"/></th>
				<th width="10%" style="text-align: right"><insta:ltext key="storeprocurement.receivetransferedindent.addedit.indentedqty"/></th>
				<th width="10%" style="text-align: right"><insta:ltext key="storeprocurement.receivetransferedindent.addedit.delivered"/></th>
				<th width="10%" style="text-align: right"><insta:ltext key="storeprocurement.receivetransferedindent.addedit.accepted"/></th>
				<th width="10%" style="text-align: right"><insta:ltext key="storeprocurement.receivetransferedindent.addedit.rejected"/></th>
				<th><insta:ltext key="storeprocurement.receivetransferedindent.addedit.issueuom"/></th>
				<th><insta:ltext key="storeprocurement.receivetransferedindent.addedit.pkgsize"/></th>
				<th width="10%"><insta:ltext key="storeprocurement.receivetransferedindent.addedit.status"/></th>
				<th><insta:ltext key="storeprocurement.receivetransferedindent.addedit.batchno.itemqty"/></th>
				<th width="5%"></th>
			</tr>

			<c:set var="i" value="0" />
			<c:forEach var="indent" items="${indentlist}">
				<c:set var="item" value="${indent.map}"/>
				<tr class="${item.medicine_id == 0 ? 'NoItemIdClass':''}">
					<td>
						<label id="itemnamelbl${i}">${item.medicine_name}</label>
						<input type="hidden" name="itemid" id="itemid${i}" value="${item.medicine_id}" />
					</td>

					<td style="text-align: right" align="right">
						<label>${ifn:afmts(item.qty)}</label>
						<input type="hidden" name="indent_qty" id="indent_qty${i}" value="${item.qty}" />
					</td>

					<td style="text-align: right">${ifn:afmts(item.qty_fullfilled)}</td>

					<td style="text-align: right">
					<label id="itemrecdqtylbl${i}">${ifn:afmts(item.qty_recd)}</label>
					<input type="hidden" name="total_item_qty_recd" id="total_item_qty_recd${i }" value="${ifn:afmts(item.qty_recd)}"/>
					<input type="hidden" name="total_item_qty_rej" id="total_item_qty_rej${i }" value="${ifn:afmts(item.qty_rej)}"/>
						
					</td>
					<td style="text-align: right">
						<label id="itemrejqtylbl${i}">${ifn:afmts(item.qty_rej)}</label>
					</td>
					<td>${item.issue_units}</td>
					<td>${item.issue_base_unit}</td>
					<td>
						<label id="statuslbl${i}">${item.status}</label>
					</td>
					<td style="width: 200px;word-break: break-all;white-space: normal;">
						<label id="batchlbl${i}"></label>
					</td>
					<c:set var="totalrecqty" value="${item.qty_recd+indent.map.qty_rej}" />
						<td>
							<c:if test="${genPrefs.recTransIndent ne 'N'}">
								<a href="javascript:void(0)" onclick="showIndentifierDialog(${i});">
									<img src="${cpath}/icons/Edit.png" class="button" />
								</a>
							</c:if>
							<input type="hidden" name="indentSelect" id="indentSelect${i}" value="false" />
							<div id="dialogShow${i}"></div>
						</td>
					<c:set var="i" value="${i+1}" />
				</tr>
			</c:forEach>		<%-- end for items in indent --%>
		</table>

		<%-- another loop for adding one dialog per item. Having it within the table interferes with hover --%>
		<c:set var="i" value="0" />
		<c:forEach var="indent" items="${indentlist}">
			<c:set var="item" value="${indent.map}"/>
			<div id="dialog${i}" style="visibility:hidden; display:none; max-height:475px;">
				<input type="hidden" name="identification${i}" value="${item.identification}" />
				<div class="bd">
					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel"><insta:ltext key="storeprocurement.receivetransferedindent.addedit.receive"/> ${item.medicine_name}</legend>
						<table class="gridFormTable" cellpadding="0" cellspacing="0" id="itemIdentifierTab${i}">
							<tr>
								<th></th>
								<th><insta:ltext key="storeprocurement.receivetransferedindent.addedit.transfer"/> #</th>
								<th><insta:ltext key="storeprocurement.receivetransferedindent.addedit.batchno"/></th>
								<th style="text-align: right"><insta:ltext key="storeprocurement.receivetransferedindent.addedit.delivered"/></th>
								<th style="text-align: right"><insta:ltext key="storeprocurement.receivetransferedindent.addedit.accept"/></th>
								<c:if test="${item.identification ne 'S'}">
									<th style="text-align: right"><insta:ltext key="storeprocurement.receivetransferedindent.addedit.reject"/></th>
								</c:if>
							</tr>

							<c:if test="${item.identification eq 'S'}">
								<tr>
									<td>
										<input type="checkbox" id="selectall${i}" onclick="onSerialSelectAll(this,${i});">
									</td>
									<td colspan="4">
										<insta:ltext key="storeprocurement.receivetransferedindent.addedit.acceptcount"/>:
										<input type="text" id="accept_count${i}" style="width:3em;" value="0" class="number" readonly>
										<insta:ltext key="storeprocurement.receivetransferedindent.addedit.rejectcount"/>:
										<input type="text" id="reject_count${i}" style="width:3em;" value="0" class="number" readonly>
									</td>
								</tr>
							</c:if>

							<c:set var="j" value="0" />
							<c:forEach items="${transferList[item.medicine_id]}" var="tfr" varStatus="status">
								<c:set var="enabled" value="${(tfr.qty_recd le 0) and (tfr.qty_rejected le 0)}"/>

								<tr>
									<td>
										<c:if test="${enabled}">
											<input type="checkbox" name="process_check${i}" onclick="onClickProcess(this,${i},${j})" />
											<input type="hidden" name="process${i}" value="N" />
											<input type="hidden" name="transfer_no${i}" value="${tfr.transfer_no}" />
											<input type="hidden" name="transfer_detail_no${i}" value="${tfr.transfer_detail_no}" />
											<input type="hidden" name="item_batch_id${i}" value="${tfr.item_batch_id}" />
											<input type="hidden" name="batch_no${i}" value="${tfr.batch_no}" />
											<input type="hidden" name="item_qty_del${i}" value="${tfr.qty}" />
										</c:if>
									</td>
									<td>${tfr.transfer_no}</td>
									<td>${tfr.batch_no}</td>
									<td style="text-align: right">${tfr.qty}</td>

									<c:choose>
										<c:when test="${item.identification eq 'S'}">
											<td style="text-align: right">
												<c:if test="${enabled}">
													<input type="checkbox" name="item_check${i}" disabled onclick="onSerialSelectItem(this, ${i},${j});" />
													<input type="hidden" name="item_qty_rec${i}" id="item_qty_rec${i}${j}" value="0"/>
													<input type="hidden" name="item_qty_rej${i}" id="item_qty_rej${i}${j}" value="0"/>
												</c:if>
											</td>
										</c:when>
										<c:otherwise>
											<td style="text-align: right">
												<c:choose>
													<c:when test="${enabled}">
														<input type="text" name="item_qty_rec${i}" id="item_qty_rec${i}${j}" readonly value="${ifn:afmts(tfr.qty_recd)}" 
																class="number"
																onchange="onChangeAcceptQty(this,${i},${j})" />
													</c:when>
													<c:otherwise>
														${ifn:afmts(tfr.qty_recd)}
													</c:otherwise>
												</c:choose>
											</td>
	
											<td style="text-align: right">
												<c:choose><c:when test="${enabled}">
													<input type="text" name="item_qty_rej${i}" id="item_qty_rej${i}${j}" readonly
													value="${ifn:afmts(tfr.qty_rejected)}" class="number"/>
												</c:when><c:otherwise>
													${ifn:afmts(tfr.qty_rejected)}
												</c:otherwise></c:choose>
											</td>
										</c:otherwise>
									</c:choose>
								</tr>
								<c:if test="${enabled}"><c:set var="j" value="${j+1}"/></c:if>
							</c:forEach>

							<tr>
								<td colspan="4">
									<input type="button" value="Ok" name="okButton${i}" />
									<input type="button" value="&lt;&lt;Previous" name="prevButton${i}" />
									<input type="button" value="Next&gt;&gt;" name="nextButton${i}" />
								</td>
							</tr>
						</table>
						</fieldset>
					</div>
				</div>

			<c:set var="i" value="${i+1}" />
		</c:forEach>		<%-- end for items in indent --%>

		<div class="screenActions">
			<c:if test="${indentdetails.map.status ne 'C'}">
				<input type="button" value="${receiveText}" name="receive" class="button" onclick="return saveForm()"> |
			</c:if>
			<a href="${cpath }/stores/StoresReceiveIndents.do?_method=list"><insta:ltext key="storeprocurement.receivetransferedindent.addedit.backtoindentlist"/></a>
		</div>

	</form>
</div>

</body>

</html>

