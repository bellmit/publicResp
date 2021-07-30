<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<c:set var="max_centers_inc_default" value='<%= GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default") %>' scope="request"/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Payment Rules - Insta HMS</title>

	<insta:link type="css" file="widgets.css" />
	<insta:link type="script" file="widgets.js" />
	<insta:link type="js" file="PaymentRule.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

		<style>
	.yui-ac {
			width: 10em;
			padding-bottom: 2em;
		}
	</style>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/PaymentRule.do?_method=addShow',
				onclick: null,
				description: "Edit Payment Rule"
				}
		};
		function init()
		{
			createToolbar(toolbar);
			showFilterActive(document.searchForm);
		}
	</script>

</head>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<body class="yui-skin-sam" onload="init();">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1>Payment Rules</h1>

	<insta:feedback-panel/>

	<form name="searchForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search form="searchForm" optionsId="optionalFilter" closed="${hasResults}">

			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Rate Plan:</div>
					<div class="sboFieldInput">
						<insta:selectdb  name="rate_plan"  table="organization_details"
						valuecol="org_id" displaycol="org_name" orderby="org_name" dummyvalue="...Select..."
						 value="${param.rate_plan}"/>
						<input type="hidden" name="rate_plan@type" value="text" />
					</div>
				</div>
				<c:if test="${max_centers_inc_default > 1}">
					<div class="sboField">
						<div class="sboFieldLabel">Center:</div>
						<div class="sboFieldInput">
							<insta:selectdb  name="center_id"  table="hospital_center_master"
							valuecol="center_id" displaycol="center_name" orderby="center_name" dummyvalue="...Select..."
							 value="${param.center_id}"/>
							<input type="hidden" name="center_id@type" value="text" />
						</div>
					</div>
				</c:if>
			</div>

			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table  class="searchFormTable">
					<tr>
						 <td>
							<div class="sfLabel">Category:</div>
							<div class="sfField">
								<div class="sfLabelSub" >Conducting Doctor:</div>
								<div class="sfField">
									<select name="doctor_category" class="dropdown">
										<option value="" >..Select..</option>
										<option value="*" ${param.doctor_category == '*' ? 'selected' : ''}>All</option>
										<c:forEach var="details" items="${categoryDetails}">
											<c:set var="paramCategoryId" value="${param.doctor_category == '*' ? -1 : param.doctor_category}"/>
											<option value="${details.map.cat_id}"
												${details.map.cat_id == paramCategoryId ? 'selected' : ''} >${details.map.cat_name}</option>
										</c:forEach>
									</select>
									<input type="hidden" name="doctor_category@op" value="in" />
								</div>
								</div>
								<div class="sfField">
									<div class="sfLabelSub">Referrer Doctor:</div>
									<div class="sfField">
										<select name="referrer_category" class="dropdown">
											<option value="">..Select..</option>
											<option value="R" ${param.referrer_category == 'R' ? 'selected' : ''}>No Referrer Doctor</option>
											<option value="*" ${param.referrer_category == '*' ? 'selected' : ''}>All</option>
											<c:set var="paramRefCategoryId"
											value="${param.referrer_category == '*'  || param.referrer_category == 'R' ? -1 : param.referrer_category}"/>
											<c:forEach var="details" items="${categoryDetails}">
												<option value="${details.map.cat_id}" ${paramRefCategoryId == details.map.cat_id ? 'selected' : ''}>${details.map.cat_name}</option>
											</c:forEach>
										</select>
										<input type="hidden" name="referrer_category@op" value="in" />
									</div>
								</div>
								<div class="sfField">
									<div class="sfLabelSub">Prescribing Doctor:</div>
									<div class="sfField">
										<select name="prescribed_category" class="dropdown">
											<option value="">..Select..</option>
											<option value="P" ${param.prescribed_category == 'P' ? 'selected' : ''}>No Prescribing Doctor</option>
											<option value="*" ${param.prescribed_category == '*' ? 'selected' : ''}>All</option>
											<c:set var="paramPreCategoryId"
											value="${param.prescribed_category == '*' || param.prescribed_category == 'P' ? -1 : param.prescribed_category}"/>
											<c:forEach var="details" items="${categoryDetails}">
												<option value="${details.map.cat_id}" ${paramPreCategoryId == details.map.cat_id ? 'selected' : ''}>${details.map.cat_name}</option>
											</c:forEach>
										</select>
										<input type="hidden" name="prescribed_category@op" value="in" />
									</div>
								</div>
						</td>
						<td>
							<div class="sfLabel">Charges</div>
							<div class="sfLabelSub">Charge Group:</div>
							<div class="sfField">
								<insta:selectdb  name="chargegroup_id" value="${param.chargegroup_id}"
								table="chargegroup_constants" valuecol="chargegroup_id" displaycol="chargegroup_name"
								filtered="false" dummyvalue="..Select.."/>
								<input type="hidden" name="chargegroup_id@op" value="in" />
							</div>
							<div class="sfLabelSub">Charge Head:</div>
							<div class="sfField">
								<insta:selectdb  id="chargeHeadId" name="charge_head" value="${param.charge_head}"
								table="chargehead_constants" onchange="autoCompleteForActivity();"
								valuecol="chargehead_id" displaycol="chargehead_name" filtered="false"
								dummyvalue="..Select.."/>
								<input type="hidden" name="charge_head@op" value="in" />
							</div>
							<div class="sfLabelSub">Activity:</div>
							<div class="sfField">
							<div id="activity_wrapper" style="width:15em" class="autocomplete">
								<input type="text" name="_activityid" id="activityId" value="${ifn:cleanHtmlAttribute(param._activityid)}"/>
								<input type="hidden" name="activity_id@op" value="ico" />
								<div id="activity_dropdown"></div>
							</div>
							</div>
								<input type="hidden" name="activity_id" id="activity_id" value="${ifn:cleanHtmlAttribute(param.activity_id)}"/>
						</td>
						<td class="last">
							<div class="sfLabel">Status</div>
							<div class="sfField">
								<insta:checkgroup name="payment_eligible" opvalues="Y,N" optexts="Active,Inactive" selValues="${paramValues.payment_eligible}"/>
								<input type="hidden" name="paymetn_eligible@op" value="in" />
							</div>
						</td>
						<td class="last">&nbsp;</td>
						<td class="last">&nbsp;</td>

					</tr>
				</table>
			</div>
		</insta:search>
	</form>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<form name="paymentRuleform" />
		<input type="hidden" name="_method" value="reorderPrecedenceValues">
		<input type="hidden" name="priorityValue">
		<c:if test="${roleId == 1 && userId == 'InstaAdmin'}">
			<%-- as per Murali, this can cause havoc to any organized arrangement.
			So, make it available only to InstaAdmin user --%>
			<div class="screenActions"><input type="button" name="reorder" value="Renumber Priority" onclick="reorderPrecedence();"/></div>
		</c:if>

			<div class="resultList">
				<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
					<tr onmouseover="hideToolBar();">
						<th>Delete</th>
						<insta:sortablecolumn name="precedance" title="Pri."/>
						<insta:sortablecolumn name="doctor_category" title="Cond Doc"/>
						<insta:sortablecolumn name="referrer_category" title="Referer"/>
						<insta:sortablecolumn name="prescribed_category" title="Presc."/>
						<insta:sortablecolumn name="rate_plan" title="Rate Plan"/>
						<c:if test="${max_centers_inc_default > 1}">
							<insta:sortablecolumn name="center_id" title="Center Name"/>
						</c:if>
						<insta:sortablecolumn name="charge_head" title="Charge Head"/>
						<th>Activity Name</th>
						<th style="text-align:right">Cond Doctor</th>
						<th style="text-align:right">Referer</th>
						<th style="text-align:right">Presc.</th>
						<th style="text-align:right">Package</th>
					</tr>

					<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
						<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
							onclick="showToolbar(${st.index}, event, 'resultTable',
							{payment_id: '${record.payment_id}'}, '');" id="toolbarRow${st.index}">
							<td><input type="checkbox" id="checkDelBox${st.index}" name="checkDelBox" onclick="validateDeletion(${st.index}, ${record.precedance});"/>

							<c:set var="flagColor" value=""/>
							<c:choose>
								<c:when test="${record.payment_eligible =='Y'}">
									<c:set var="flagColor" value="empty"/>
								</c:when>
								<c:otherwise>
									<c:set var="flagColor" value="grey"/>
								</c:otherwise>
							</c:choose>

							<td>${record.precedance}</td>
							<td>
								<c:choose>
									<c:when test="${record.doctor_category_name eq null}">(All)</c:when>
									<c:otherwise>${record.doctor_category_name}</c:otherwise>
								</c:choose>
							</td>
							<td>
								<c:choose>
									<c:when test="${record.referrer_category eq '*'}">(All)</c:when>
									<c:when test="${record.referrer_category eq 'R'}">(No Referer)</c:when>
									<c:otherwise>${record.referer_category_name}</c:otherwise>
								</c:choose>
							</td>
							<td>
								<c:choose>
									<c:when test="${record.prescribed_category eq '*'}">(All)</c:when>
									<c:when test="${record.prescribed_category eq 'P'}">(No Presc.)</c:when>
									<c:otherwise>${record.prescribed_category_name}</c:otherwise>
								</c:choose>
							</td>
							<td>
								<c:choose>
									<c:when test="${record.org_name eq null}">(All)</c:when>
									<c:otherwise>${record.org_name}</c:otherwise>
								</c:choose>
							</td>
							<c:if test="${max_centers_inc_default > 1}">
								<td>
									<c:choose>
										<c:when test="${record.center_id eq '*'}">(All)</c:when>
										<c:otherwise>${record.center_name}</c:otherwise>
									</c:choose>
								</td>
							</c:if>

							<td>
								<img src="${cpath}/images/${flagColor}_flag.gif"/>
								<insta:truncLabel value="${record.chargehead_name}" length="15"/>
							</td>
							<td>${record.test_name} ${record.service_name} ${record.package_name }</td>

							<td style="text-align:right">
								<c:choose>
									<c:when test="${record.dr_payment_option eq '1' && record.dr_payment_value gt 0}">
										<fmt:formatNumber value="${record.dr_payment_value}" />%
									</c:when>
									<c:when test="${record.dr_payment_option eq '3' && record.dr_payment_value gt 0}">
										${record.dr_payment_value}
									</c:when>
									<c:when test="${record.dr_payment_option eq '4' && record.dr_payment_value gt 0}">
										-${record.dr_payment_value}
									</c:when>
									<c:when test="${record.dr_payment_option eq '5'}">
										<insta:truncLabel length="10" value="${record.dr_payment_expr}"/>
									</c:when>
									<c:otherwise>-</c:otherwise>
								</c:choose>
							</td>
							<td style="text-align:right">
								<c:choose>
									<c:when test="${record.ref_payment_option eq '1' && record.ref_payment_value gt 0}">
										<fmt:formatNumber value="${record.ref_payment_value}" />%
									</c:when>
									<c:when test="${record.ref_payment_option eq '3' && record.ref_payment_value gt 0}">
										${record.ref_payment_value}
									</c:when>
									<c:when test="${record.ref_payment_option eq '4' && record.ref_payment_value gt 0}">
										-${record.ref_payment_value}
									</c:when>
									<c:when test="${record.ref_payment_option eq '5'}">
										<insta:truncLabel length="10" value="${record.ref_payment_expr}"/>
									</c:when>
									<c:otherwise>-</c:otherwise>
								</c:choose>
							</td>
							<td style="text-align:right">
								<c:choose>
									<c:when test="${record.presc_payment_option eq '1' && record.presc_payment_value gt 0}">
										<fmt:formatNumber value="${record.presc_payment_value}" />%
									</c:when>
									<c:when test="${record.presc_payment_option eq '3' && record.presc_payment_value gt 0}">
										${record.presc_payment_value}
									</c:when>
									<c:when test="${record.presc_payment_option eq '4' && record.presc_payment_value gt 0}">
										-${record.presc_payment_value}
									</c:when>
									<c:when test="${record.presc_payment_option eq '5'}">
										<insta:truncLabel length="10" value="${record.presc_payment_expr}"/>
									</c:when>
									<c:otherwise>-</c:otherwise>
								</c:choose>
							</td>
							<td style="text-align:right">${record.dr_pkg_amt}</td>
						</tr>
					</c:forEach>
				</table>

				<c:if test="${param._method == 'list'}">
					<insta:noresults hasResults="${hasResults}"/>
				</c:if>

			</div>

		<c:url var="add" value="PaymentRule.do">
			<c:param name="_method" value="addShow"></c:param>
		</c:url>
		<div class="screenActions" style="float:left">
			<a href="${add}">Add Payment Rule</a>
			<c:if test="${urlRightsMap.paymentrule_audit_log != 'N'}"> |
				<a href="${cpath}/paymentrule/auditlog/AuditLogSearch.do?_method=getSearchScreen">Audit Log</a>
			</c:if>
		</div>
	</form>

	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText">Inactive Rule</div>
	</div>
<script>
var labTestDetails=<%= request.getAttribute("laboratoryTestDetails") %>;
var radTestDetails=<%= request.getAttribute("radiologyTestDetails") %>;
var servicesDetails=<%= request.getAttribute("serviceDetails") %>;
var empt=[];
</script>

</body>
</html>
