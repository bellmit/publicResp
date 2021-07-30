<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<c:set var="currType"><fmt:message key="currencyType"/> </c:set>
<c:set var="bedlist" value="${pagedList.dtoList}" />
<c:set var="hasResults" value="${not empty bedlist}"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Rate Plan Overrides - Insta HMS</title>
<insta:link type="js" file="ajax.js" />
<insta:link type="js" file="date_go.js" />
<insta:link type="js" file="hmsvalidation.js" />
<insta:link type="js" file="masters/bedmaster.js" />
<insta:link type="css" file="widgets.css" />
<insta:link type="script" file="widgets.js" />
<insta:link type="js" file="dashboardsearch.js"/>

<style type="text/css">
  .status_I {background-color: #E4C89C }
</style>
<script>
	pa = new Array();
	var toolbarA = {
		edit: {
			title: "Edit Charge",
			imageSrc: 'icons/Edit.png',
			href: 'pages/masters/insta/admin/newbedmaster.do?method=getChargesOverrideScreen&fromItemMaster=false',
			onclick: null,
			description: null
		}
	};
	function init() {
		createToolbar(toolbarA);
	}
</script>
</head>
<body onload="init()">

<html:form action="/pages/masters/insta/admin/newbedmaster.do?"	onsubmit="return doSearch()" method="GET">

	<html:hidden property="pageNum" />
	<html:hidden property="method" value="getChargesList" />
	<html:hidden property="orgId" value="${newbedmasterform.orgId}"/>

	<h1>Rate Plan Overrides - ${newbedmasterform.orgName}</h1>
	<insta:feedback-panel/>
	<insta:search-lessoptions form="newbedmasterform" >
		<div class="searchBasicOpts" >
			<div class="sboField" style="height:69">
				<div class="sboFieldLabel">View Charges For</div>
				<div class="sboFieldInput">
					<select name="chargeHead" class="dropdown">
					 	<c:if test="${newbedmasterform.chargeHead != null && not empty newbedmasterform.chargeHead }">
	 					 	<c:choose>
						 	 	<c:when test="${newbedmasterform.chargeHead eq 'BEDCHARGE'}">
									<option value="BEDCHARGE">BEDCHARGE</option>
							  	</c:when>
							  	<c:when test="${newbedmasterform.chargeHead eq 'NURSING'}">
									<option value="NURSING">NURSING CHARGE</option>
							 	 </c:when>
							  	<c:when test="${newbedmasterform.chargeHead eq 'INITIAL'}">
							  		<option value="INITIAL">INITIAL PAYMENT</option>
							  	</c:when>
							 	 <c:when test="${newbedmasterform.chargeHead eq 'DUTY'}">
							  		<option value="DUTY">DUTY DOCTOR CHARGE</option>
							 	 </c:when>
							  	<c:when test="${newbedmasterform.chargeHead eq 'HOURLY'}">
							  		<option value="HOURLY">HOURLY CHARGE</option>
							  	</c:when>
							    <c:when test="${newbedmasterform.chargeHead eq 'LUXARY'}">
						  			<option value="LUXARY">LUXURY CHARGE</option>
	    						</c:when>
						  		<c:when test="${newbedmasterform.chargeHead eq 'MAINTAINANCE'}">
									<option value="MAINTAINANCE">PROFESSIONAL CHARGE</option>
	 						    </c:when>
					 	 	</c:choose>
						</c:if>
						<c:if test="${newbedmasterform.chargeHead ne 'BEDCHARGE'}">
								<option value="BEDCHARGE">BEDCHARGE</option>
						</c:if>
						<c:if test="${newbedmasterform.chargeHead ne 'NURSING'}">
								<option value="NURSING">NURSING CHARGE</option>
						</c:if>
						<c:if test="${newbedmasterform.chargeHead ne 'INITIAL'}">
								<option value="INITIAL">INITIAL PAYMENT</option>
						</c:if>
						<c:if test="${newbedmasterform.chargeHead ne 'DUTY'}">
								<option value="DUTY">DUTY DOCTOR CHARGE</option>
						</c:if>
						<c:if test="${newbedmasterform.chargeHead ne 'HOURLY'}">
								<option value="HOURLY">HOURLY CHARGE</option>
						</c:if>
						<c:if test="${newbedmasterform.chargeHead ne 'LUXARY'}">
								<option value="LUXARY">LUXURY CHARGE</option>
						</c:if>
						<c:if test="${newbedmasterform.chargeHead ne 'MAINTAINANCE'}">
								<option value="MAINTAINANCE">PROFESSIONAL CHARGE</option>
						</c:if>
					</select>
				</div>
			</div>
			<div class="sboField">
				<div class="sboFieldLabel">Overrided</div>
				<div class="sboFieldInput">
					<insta:checkgroup name="is_override" opvalues="Y,N" optexts="Yes,No" selValues="${paramValues.is_override}"/>
						<input type="hidden" name="is_override@op" value="in" />
				</div>
			</div>
		</div>
	</insta:search-lessoptions>

	<table class="formtable" width="100%">

		<c:url var="EC" value="newbedmaster.do">
			<c:param name="method" value="getEditChargesScreen" />
		</c:url>

		<tr>
			<td>
			<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
			<div class="resultList">
			<table class="resultList" onmouseover="hideToolBar('')");" id="resultTable">
				<tr>
					<th>Bed Type</th>
					<th>
					  <c:choose>
						  <c:when test="${newbedmasterform.chargeHead eq 'BEDCHARGE'}">
								Bed Charge
						  </c:when>
						  <c:when test="${newbedmasterform.chargeHead eq 'NURSING'}">
								Nursing Charge
						  </c:when>
						  <c:when test="${newbedmasterform.chargeHead eq 'INITIAL'}">
						  		Intial Payment
						  </c:when>
						  <c:when test="${newbedmasterform.chargeHead eq 'DUTY'}">
						  		Duty Charge
						  </c:when>
						  <c:when test="${newbedmasterform.chargeHead eq 'HOURLY'}">
						  		Hourly Charge
						  </c:when>
						  <c:when test="${newbedmasterform.chargeHead eq 'LUXARY'}">
						  		Luxury Charge(%)
						  </c:when>
						  <c:when test="${newbedmasterform.chargeHead eq 'MAINTAINANCE'}">
								Professional Charge
						  </c:when>
					  </c:choose>
					</th>
				</tr>

				<c:choose>
					<c:when test="${chargeHead eq 'BEDCHARGE' }">
						<input type="hidden" name="groupUpdatComponent" value="BEDCHARGE" />
						<c:forEach var="record" items="${bedlist}" varStatus="st">
							<c:set var="flagColor">
								<c:if test="${record.BED_STATUS eq 'A'}">empty</c:if>
								<c:if test="${record.BED_STATUS ne 'A'}">grey</c:if>
							</c:set>
							<script>
								pa[${st.index}] = {};
								pa[${st.index}].bed_type = <insta:jsString value="${record.BEDTYPE}"/>;
							</script>
							<tr onclick="showToolbar(${st.index}, event, 'resultTable',
								{bedType: pa[${st.index}].bed_type, orgId: '${newbedmasterform.orgId}'},	[true]);"
								onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
								<td><img src="${cpath}/images/${flagColor}_flag.gif" />${record.BEDTYPE}</td>
								<td class="number" style="text-align: left;">${ifn:afmt(record.BED_CHARGE)}</td>
							</tr>
						</c:forEach>
					</c:when>
					<c:when test="${chargeHead eq 'NURSING'}">
					<input type="hidden" name="groupUpdatComponent" value="NURSING" />
						<c:forEach var="record" items="${bedlist}" varStatus="st">
							<c:set var="flagColor">
								<c:if test="${record.BED_STATUS eq 'A'}">empty</c:if>
								<c:if test="${record.BED_STATUS ne 'A'}">grey</c:if>
							</c:set>
							<script>
								pa[${st.index}] = {};
								pa[${st.index}].bed_type = <insta:jsString value="${record.BEDTYPE}"/>;
							</script>
							<tr onclick="showToolbar(${st.index}, event, 'resultTable',
								{bedType: pa[${st.index}].bed_type, orgId: '${newbedmasterform.orgId}'},	[true]);"
								onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
								<td><img src="${cpath}/images/${flagColor}_flag.gif" />${record.BEDTYPE}</td>
								<td class="number" style="text-align: left;">${ifn:afmt(record.NURSING_CHARGE)}</td>
							</tr>
						</c:forEach>
					</c:when>
					<c:when test="${chargeHead eq 'INITIAL'}">
					  <input type="hidden" name="groupUpdatComponent" value="INITIAL" />
						<c:forEach var="record" items="${bedlist}" varStatus="st">
							<c:set var="flagColor">
								<c:if test="${record.BED_STATUS eq 'A'}">empty</c:if>
								<c:if test="${record.BED_STATUS ne 'A'}">grey</c:if>
							</c:set>
							<script>
								pa[${st.index}] = {};
								pa[${st.index}].bed_type = <insta:jsString value="${record.BEDTYPE}"/>;
							</script>
							<tr onclick="showToolbar(${st.index}, event, 'resultTable',
								{bedType: pa[${st.index}].bed_type, orgId: '${newbedmasterform.orgId}'},	[true]);"
								onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
								<td><img src="${cpath}/images/${flagColor}_flag.gif" />${record.BEDTYPE}</td>
								<td class="number" style="text-align: left;">${ifn:afmt(record.INITIAL_PAYMENT)}</td>
							</tr>
						</c:forEach>
					</c:when>
					<c:when test="${chargeHead eq 'DUTY' }">
					  <input type="hidden" name="groupUpdatComponent" value="DUTY" />
						<c:forEach var="record" items="${bedlist}" varStatus="st">
							<c:set var="flagColor">
								<c:if test="${record.BED_STATUS eq 'A'}">empty</c:if>
								<c:if test="${record.BED_STATUS ne 'A'}">grey</c:if>
							</c:set>
							<script>
								pa[${st.index}] = {};
								pa[${st.index}].bed_type = <insta:jsString value="${record.BEDTYPE}"/>;
							</script>
							<tr onclick="showToolbar(${st.index}, event, 'resultTable',
								{bedType: pa[${st.index}].bed_type, orgId: '${newbedmasterform.orgId}'},	[true]);"
								onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
								<td><img src="${cpath}/images/${flagColor}_flag.gif" />${record.BEDTYPE}</td>
								<td class="number" style="text-align: left;">${ifn:afmt(record.DUTY_CHARGE)}</td>
							</tr>
						</c:forEach>
					</c:when>
					<c:when test="${chargeHead eq 'HOURLY' }">
					  <input type="hidden" name="groupUpdatComponent" value="HOURLY" />
						<c:forEach var="record" items="${bedlist}" varStatus="st">
							<c:set var="flagColor">
								<c:if test="${record.BED_STATUS eq 'A'}">empty</c:if>
								<c:if test="${record.BED_STATUS ne 'A'}">grey</c:if>
							</c:set>
							<script>
								pa[${st.index}] = {};
								pa[${st.index}].bed_type = <insta:jsString value="${record.BEDTYPE}"/>;
							</script>
							<tr onclick="showToolbar(${st.index}, event, 'resultTable',
								{bedType: pa[${st.index}].bed_type, orgId: '${newbedmasterform.orgId}'},	[true]);"
								onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
								<td><img src="${cpath}/images/${flagColor}_flag.gif" />${record.BEDTYPE}</td>
								<td class="number" style="text-align: left;">${ifn:afmt(record.HOURLY_CHARGE)}</td>
							</tr>
						</c:forEach>
					</c:when>
					<c:when test="${chargeHead eq 'LUXARY' }">
					  <input type="hidden" name="groupUpdatComponent" value="LUXARY" />
						<c:forEach var="record" items="${bedlist}" varStatus="st">
							<c:set var="flagColor">
								<c:if test="${record.BED_STATUS eq 'A'}">empty</c:if>
								<c:if test="${record.BED_STATUS ne 'A'}">grey</c:if>
							</c:set>
							<script>
								pa[${st.index}] = {};
								pa[${st.index}].bed_type = <insta:jsString value="${record.BEDTYPE}"/>;
							</script>
							<tr onclick="showToolbar(${st.index}, event, 'resultTable',
								{bedType: pa[${st.index}].bed_type, orgId: '${newbedmasterform.orgId}'},	[true]);"
								onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
								<td><img src="${cpath}/images/${flagColor}_flag.gif" />${record.BEDTYPE}</td>
								<td class="number" style="text-align: left;">${ifn:afmt(record.LUXARY_TAX)}</td>
							</tr>
						</c:forEach>
					</c:when>
					<c:when test="${chargeHead eq 'MAINTAINANCE'}">
					 <input type="hidden" name="groupUpdatComponent" value="MAINTAINANCE" />
						<c:forEach var="record" items="${bedlist}" varStatus="st">
							<c:set var="flagColor">
								<c:if test="${record.BED_STATUS eq 'A'}">empty</c:if>
								<c:if test="${record.BED_STATUS ne 'A'}">grey</c:if>
							</c:set>
							<script>
								pa[${st.index}] = {};
								pa[${st.index}].bed_type = <insta:jsString value="${record.BEDTYPE}"/>;
							</script>
							<tr onclick="showToolbar(${st.index}, event, 'resultTable',
								{bedType: pa[${st.index}].bed_type, orgId: '${newbedmasterform.orgId}'},	[true]);"
								onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
								<td><img src="${cpath}/images/${flagColor}_flag.gif" />${record.BEDTYPE}</td>
								<td class="number" style="text-align: left;">${ifn:afmt(record.MAINTAINANCE_CHARGE)}</td>
							</tr>
						</c:forEach>
					</c:when>
				</c:choose>
			</table>
			</div>
			</td>
		</tr>
	</table>

</html:form>
<table class="screenActions" width="100%">
	<tr>
		<td><a href="${cpath}/pages/masters/ratePlan.do?_method=showRatePlanDetails&org_id=${newbedmasterform.orgId}">Edit Rate Plan</a></td>
		<td align="right">
			<img src="${cpath}/images/grey_flag.gif">
			&nbsp;&nbsp;Inactive
		</td>
	</tr>
</table>

</body>
</html>
