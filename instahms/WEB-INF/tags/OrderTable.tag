<%@tag body-content="empty" dynamic-attributes="dynatr" pageEncoding="UTF-8"%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<%@attribute name="orderList" required="false" type="java.util.List" %>
<%@attribute name="onlyNew" required="false" type="java.lang.Boolean"%>	<%-- eg, for registration --%>
<%@attribute name="operItems" required="false" type="java.lang.Boolean"%>
<%@attribute name="instanceId" required="false"%> <%-- ID of the table, to pass to handlers like start. --%>
<%@attribute name="allowAdd" required="false" %>
<%@attribute name="itemId" required="false" %>
<%@attribute name="insured" required="false" type="java.lang.Boolean"%>
<%@attribute name="allowCancle" required="false" %>
<%@attribute name="test_info_view" required="false" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>

<c:set var="operItems" value="${empty operItems ? false : operItems}"/>
<c:set var="onlyNew" value="${empty onlyNew ? false : onlyNew}"/>
<c:set var="allowAdd" value="${empty allowAdd ? true : allowAdd}"/>
<c:set var="insured" value="${empty insured ? false : insured}"/>
<c:set var="multiPlanExists" value="${empty multiPlanExists ? false : multiPlanExists}"/>
<c:set var="allowCancle" value="${empty allowCancle ? true : allowCancle }"/>
<c:set var="test_info_view" value="${empty test_info_view ? false : test_info_view}"/>

<c:set var="cancelWhenSample" value="${roleId == 1 || roleId == 2 ? 'A' : actionRightsMap.allow_test_cancel}"/>

<script type="text/javascript">
	var showChargesAllRatePlan = ('${roleId}' == '1' || '${roleId}' == '2') ? 'A' : '${actionRightsMap.view_all_rates}';
</script>

<%--
For inclusion in a page to show a list of orders, also works directly with AddOrderDialog.jsp.
Scripts for this page are in scripts/ordertable.js you need to include that in your jsp.
Note that this represents a true order, not suitable for pure billing items.
--%>

<c:set var="titleKey">
<insta:ltext key="common.order.titlekey"/>
</c:set>
<c:set var="additem">
<insta:ltext key="common.order.add.newitem.patientdischarged"/>
</c:set>
<c:set var="cancelitem">
<insta:ltext key="common.order.cancelitem"/>
</c:set>

<c:set var="edititem">
<insta:ltext key="common.order.edititemdetails"/>
</c:set>
<c:set var="addnewoperation">
<insta:ltext key="common.order.addnewoperation"/>
</c:set>


<table width="100%" class="detailList dialog_displayColumns" id="orderTable${instanceId}">
	<tr>
		<th></th>
		<c:if test="${!onlyNew}">
			<th><insta:ltext key="common.order.bill.no"/></th>
		</c:if>
		<th><insta:ltext key="common.order.order.date.time"/></th>
		<c:if test="${!onlyNew}">
			<th><insta:ltext key="common.order.ord"/></th>
		</c:if>
		<th><insta:ltext key="common.order.prescribed.by"/></th>
		<th><insta:ltext key="common.order.type"/></th>
		<th><insta:ltext key="common.order.item"/></th>
		<th><insta:ltext key="common.order.details"/></th>
		<th><insta:ltext key="common.order.remarks"/></th>
		<th style="text-align: right"><insta:ltext key="common.order.amount"/></th>
		<th style="text-align: right; ${insured ? '' : 'display: none'}" ><insta:ltext key="common.order.pat.amt"/></th>
		<th style="text-align: right; ${insured ? '' : 'display: none'}" >
			<div id="preAuthHeader" style="${insured && !multiPlanExists ? '' : 'display: none'}"><insta:ltext key="common.order.prior.auth"/></div>
			<div id="priPreAuthHeader" style="${insured &&  multiPlanExists ? '' : 'display: none'} "><insta:ltext key="common.order.pri.prior.auth"/></div>
		</th>
		<th style="text-align: right; ${insured ? '' : 'display: none'}">
			<div id="secPriorAuthHeader" style="${insured &&  multiPlanExists ? '' : 'display: none'}"><insta:ltext key="common.order.sec.prior.auth"/></div>
		</th>
		<th style="width: 20px"></th>		<%-- trash icon --%>
		<th style="width: 30px"></th>		<%-- edit icon, only for existing orders --%>
		<c:if test="${test_info_view}">
			<th style="width: 150px"></th>
		</c:if>
	</tr>

	<c:set var="numOrders" value="${fn:length(orderList)}"/>

	<c:forEach begin="0" end="${numOrders}" var="i" varStatus="loop">
		<c:set var="order" value="${orderList[i].map}"/>
		<c:set var="cancelEnabled" value="${allowCancle &&
		( empty order ||
			(order.isdialysis eq 'D'
				?
					( (order.dialysis_status eq 'O' ||
					( (order.dialysis_status eq 'F' || order.dialysis_status eq 'C') && (order.completion_status eq 'D' || order.completion_status eq 'X') )
					) && order.status ne 'X')
				:
					( (order.status=='U' && empty order.outsource_dest_prescribed_id)||
					( (order.status == 'N' || order.status == 'NRN') && (empty order.outsource_dest_prescribed_id) && (order.sample_collected == 'N' || cancelWhenSample == 'A' ) )
					)
			)
		)}"/>


		<c:set var="editEnabled"
			value="${(empty order || order.status=='N' || order.status == 'P' || order.status == 'U' || order.status == 'NRN')}"/>
		<c:set var="flagColor">
			<c:choose>
				<c:when test="${order.status == 'C' || order.status == 'RC' || order.status == 'S' || order.status == 'V' || order.status == 'RV' || order.status == 'CRN'}">green</c:when>
				<c:when test="${order.status == 'R' || order.status == 'RBS' || order.status == 'RAS' }">brown</c:when>
				<c:when test="${order.status == 'P' || order.status == 'RP'}">yellow</c:when>
				<c:when test="${order.status == 'X'}">red</c:when>
				<c:when test="${order.status != 'U' && order.sample_collected == 'Y'}">blue</c:when>
				<c:otherwise>empty</c:otherwise>
			</c:choose>
		</c:set>

		<tr style="${empty order ? 'display:none' : ''}">
		    	<td>
		    	<c:if test="${!operItems && order.multi_visit_package}">

			<%-- get the package name & status to show as tooltip --%>

			<c:set var="packTitle" value=""/>
			<c:if test="${not empty order.bill_no and not empty mvpackageList and not empty mvpackageList[order.bill_no]}">
			      <c:set var="packStatus">
			      	     <c:choose>
					<c:when test="${mvpackageList[order.bill_no].map.status == 'C'}">Completed</c:when>
			       	     	<c:when test="${mvpackageList[order.bill_no].map.status == 'P'}">In Progress</c:when>
			       	     	<c:when test="${mvpackageList[order.bill_no].map.status == 'X'}">Cancelled</c:when>
			       	     	<c:otherwise>Unknown</c:otherwise>
			       	     </c:choose>
			      </c:set>
			      <c:set var="packTitle" value="${mvpackageList[order.bill_no].map.package_name}:${packStatus}"/>
			</c:if>
			<c:if test="${not empty packTitle}"><c:set var="packTitle" value="title=\"${packTitle}\""/></c:if>

			<%-- display a package icon --%>
			<img class="flag" src="${cpath}/images/package.png" ${packTitle}/>
			</c:if>
			</td>

			<c:if test="${!onlyNew}">
				<td>${order.bill_no}</td>
			</c:if>
			<td class="label" align="left">
				<label id="lblDateTime${i}">
					<fmt:formatDate value="${order.pres_timestamp}" pattern="dd-MM-yyyy HH:mm"/>
				</label>
				<%-- the following are the only editable fields, so we include only these as hidden
					fields. For added rows, we use makeHidden to add the type specific fields --%>
				<input type="hidden" name="multi_visit_package_bill_no" value="${order.bill_no}">
				<input type="hidden" name="package_id" value="${operItems ? '' : order.package_id}">
				<input type="hidden" name="mv_pat_package_id" value="${operItems ? '' : order.pat_package_id}">
				<input type="hidden" name="multi_visit_package" value="${operItems ? '' : order.multi_visit_package}">
				<input type="hidden" name="pres_date" value="<fmt:formatDate value="${order.pres_timestamp}" pattern="dd-MM-yyyy"/>"/>
				<input type="hidden" name="firstOfCategory" value="${order.first_of_category}"/>
				<input type="hidden" name="insCategoryId" value="${order.insurance_category_id}"/>
				<input type="hidden" name="remarks" value="${order.remarks}"/>
				<input type="hidden" name="presDocId" value="${order.pres_doctor_id}"/>
				<input type="hidden" name="presDocName" value="${order.pres_doctor_name}"/>
				<input type="hidden" name="status" value="${order.status}"/>
				<input type="hidden" name="finStatus" value="${order.finalization_status}"/>
				<input type="hidden" name="newFinStatus" value="${order.finalization_status}"/>
				<input type="hidden" name="serviceGroupId" value=""/>
				<fmt:formatDate value="${order.from_timestamp}" var="fromDate" pattern="dd-MM-yyyy"/>
				<fmt:formatDate value="${order.from_timestamp}" var="fromTime" pattern="HH:mm"/>
				<fmt:formatDate value="${order.to_timestamp}" var="toDate" pattern="dd-MM-yyyy"/>
				<fmt:formatDate value="${order.to_timestamp}" var="toTime" pattern="HH:mm"/>
				<input type="hidden" name="fromDate" value="${fromDate}"/>
				<input type="hidden" name="fromTime" value="${fromTime}"/>
				<input type="hidden" name="toDate" value="${toDate}"/>
				<input type="hidden" name="toTime" value="${toTime}"/>
				<%-- we also need some identifiers for the order being edited --%>
				<input type="hidden" name="existingtype" value="${order.type}"/>
				<input type="hidden" name="type" value="${order.type}"/>
				<input type="hidden" name="prescribedId" value="${order.order_id}"/>
				<input type="hidden" name="sub_type" value="${order.sub_type }"/>
				<input type="hidden" name="quantity" value="${order.quantity }"/>
				<%-- and some statuses --%>
				<input type="hidden" name="bill_status" value="${order.bill_status}"/>
				<input type="hidden" name="cancleBill" value="${order.canclebill }"/>
				<input type="hidden" name="cancelled" value=""/>
				<input type="hidden" name="edited" value="N">
				<input type="hidden" name="new" value="N">  <%-- newly added items will be true --%>
				<input type="hidden" name="orderAmount" value="">  <%-- only for newly added items --%>
				<input type="hidden" name="orderPatientAmt" value="">  <%-- only for newly added items --%>
				<input type="hidden" name="orderCategory" value=""/>  <%-- only for newly added items --%>
				<input type="hidden" name="orderDiscount" value=""/>  <%-- only for newly added items --%>
				<input type="hidden" name="chargeHead" value=""/>  <%-- only for newly added items --%>
				<input type="hidden" name="orderTax" value=""/>
				<input type="hidden" name="priClaimAmt" value=""/>
				<input type="hidden" name="secClaimAmt" value=""/>
 				<input type="hidden" name="urgent" value="${order.urgent}"/>
				<input type="hidden" name="s_tooth_number" value="${order.tooth_number}"/>
			</td>
			<c:if test="${!onlyNew}">
				<td>
				<c:choose>
					<c:when test="${order.status != 'X'}">
						<a target="blank" href="order.do?_method=printOrder&patientId=${patient.patient_id}&orderid=${order.common_order_id}">
							${order.common_order_id}</a>
					</c:when>
					<c:otherwise>
						<label>${order.common_order_id}</label>
					</c:otherwise>
				</c:choose>
				</td>
			</c:if>
			<td>${order.pres_doctor_name}</td>
			<td>${order.type}
				<c:if test="${not empty order.sub_type_name}">(${order.sub_type_name})</c:if>
			</td>
			<td>
				<c:if test="${!onlyNew}">
					<img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>
				</c:if>
				<insta:truncLabel value="${order.item_name}" length="32"/>
				<input type="hidden" name="item_id" value="${order.item_id}"/>
			</td>
			<td><insta:truncLabel value="${order.details}" length="16"/></td>
			<td><insta:truncLabel value="${order.remarks}" length="10"/></td>
			<td class="number"></td>
			<c:set var="priPreAuthNo" value=""/>
			<c:set var="priPreAuthMode" value=""/>
			<c:set var="secPreAuthNo" value=""/>
			<c:set var="secPreAuthMode" value=""/>
			<td class="number" style="${insured ? '' : 'display: none'}">
				<c:forEach var="preAuths" items="${preAuthNoAndModeIdsList}">
					<c:if test="${preAuths.charge_id == order.charge_id}">
						<c:set var="priPreAuthNo" value="${preAuths.pri_prior_auth_id}"/>
						<c:set var="priPreAuthMode" value="${preAuths.pri_prior_auth_mode_id}"/>
						<c:set var="secPreAuthNo" value="${preAuths.sec_prior_auth_id}"/>
						<c:set var="secPreAuthMode" value="${preauths.sec_prior_auth_mode_id}"/>
					</c:if>
				</c:forEach>
			</td>

			<td style="${insured ? '' : 'display: none'}; text-align:center">
				${priPreAuthNo}
				<input type="hidden" name="prior_auth_id" value="${priPreAuthNo}">
				<input type="hidden" name="prior_auth_mode_id" value="${priPreAuthMode}">
			</td>

			<td style="${insured && multiPlanExists ? '' : 'display: none'}; text-align:center">
				${secPreAuthNo}
				<input type="hidden" name="sec_prior_auth_id" value="${secPreAuthNo}">
				<input type="hidden" name="sec_prior_auth_mode_id" value="${secPreAuthMode}">
			</td>

			<td style="text-align:right">
				<c:choose>
					<c:when test="${cancelEnabled}">
						<a href="javascript:void(0)" onclick="cancelOrder(this, '${instanceId}');"
							title="${cancelitem} " >
							<img src="${cpath}/icons/delete.gif" class="button" />
						</a>
					</c:when>
					<c:otherwise>
						<img class="imgDelete" src="${cpath}/icons/delete_disabled.gif" />
					</c:otherwise>
				</c:choose>
			</td>

			<td style="text-align: center">
				<c:choose>
					<c:when test="${editEnabled}">
						<a href="javascript:void(0)" name="btnEditCharges" id="btnEditCharges${i}"
							onclick="showEditDialog(this, '${instanceId}');"
							title="${edititem}" >
							<img src="${cpath}/icons/Edit.png" class="button" />
						</a>
					</c:when>
					<c:otherwise>
						<img src="${cpath}/icons/Edit1.png"/>
					</c:otherwise>
				</c:choose>
			</td>
			<c:if test="${test_info_view}">
				<td style="width: 150px">
					<c:if test="${order.status != 'X' && order.mandate_additional_info == 'O'}">
						<c:url value="/Diagnostics/TestInfoViewer.do" var="testDocUrl">
							<c:param name="_method" value="list"/>
							<c:param name="prescribed_id" value="${order.order_id}"/>
							<c:param name="patient_id" value="${patient.patient_id}"/>
						</c:url>
						<c:set var="test_info_viewer_title">
							<insta:ltext key="common.order.test_info_viewer_title"/>
						</c:set>
						<a href="${testDocUrl}" title="${test_info_viewer_title}" target="_blank"><insta:ltext key="common.order.test_info_viewer"/></a>
					</c:if>
				</td>
			</c:if>
		</tr>
	</c:forEach>
</table>

<table width="100%" class="addButton">
	<tr>
		<td width="100%"></td>
		<td style="width: 20px">
			<c:choose>
				<c:when test="${not allowAdd}">
					<c:set var="title" value="${additem}"/>
					<c:set var="img" value="Add1.png"/>
					<c:set var="onclick" value="return false;"/>
				</c:when>
				<c:when test="${operItems}">
					<c:set var="accessKey" value="O"/>
					<c:set var="title" value="${addnewoperation}"/>
					<c:set var="img" value="AddBlue.png"/>
					<c:set var="onclick" value="addOrderDialog.start(this, true, '${instanceId}','${itemId }');"/>
				</c:when>
				<c:otherwise>	<%-- normal add --%>
					<c:set var="accessKey" value="+"/>
					<c:set var="title" value="${titleKey}"/>
					<c:set var="img" value="Add.png"/>
					<c:set var="onclick" value="addOrderDialog.start(this, false, '${instanceId}','${itemId }');"/>
				</c:otherwise>
			</c:choose>

			<button type="button" name="btnAddItem" id="btnAddItem" title="${title}"
					onclick="${onclick}" accesskey="${accessKey}" class="imgButton">
				<img src="${cpath}/icons/${img}">
			</button>
		</td>
	</tr>
</table>

<%-- todo: ideally, Add and Cancel Options dialogs also belong in this file. But due to nesting problem of
forms (table has to appear within the main form), we have put the forms in the main page itself.
We could make them a separte tag file, though.
--%>

