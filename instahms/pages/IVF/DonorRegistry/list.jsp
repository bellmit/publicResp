<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Patient Donor Registry - Insta HMS</title>
	<insta:link type="css" file="hmsNew.css" />
	<insta:link type="js" file="ajax.js" />
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
	<script>
	var toolbar = {
		EditDonorDetails: {
			title: "Edit Donor Details",
			imageSrc: "icons/Edit.png",
			href: '/IVF/DonorRegistry.do?_method=show',
			onclick: null,
			description: "Edit Donor Details"
		},
		EditDonationDetails: {
			title: "Edit Donation Details",
			imageSrc: "icons/Edit.png",
			href: '/IVF/DonationDetails.do?_method=getDonationDetails',
			onclick: null,
			description: "Edit Donation Details"
		}

	};
	function init() {
		createToolbar(toolbar);
	}
	</script>
</head>
<c:set var="donorRegistrylist" value="${PagedList.dtoList}"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="hasResults" value="${not empty donorRegistrylist}"/>
<body  onload="init();" class="yui-skin-sam">
<div class="pageHeader">Patient Donor Registry</div>
<insta:feedback-panel/>
<form name="donorRegistryForm" method="get" >
<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search-lessoptions form="donorRegistryForm">
	<div class="searchBasicOpts" >
			<div class="sboField" style="height:69px">
				<div class="sfLabel">MR No/Patient Name:</div>
				<div class="sfField">
					<div id="mrnoAutoComplete">
						<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
						<div id="mrnoContainer" style="width: 300px"></div>
					</div>
				</div>
			</div>
			<div class="sboField">
				<div class="sfLabel">Donor Status:</div>
				<div class="sfField">
					<insta:checkgroup name="donor_status" selValues="${paramValues.donor_status}"
						opvalues="A,I" optexts="Active,InActive"/>
				</div>
			</div>
	</div>
	</insta:search-lessoptions>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<fieldSet class="fieldSetBorder"><legend class="fieldSetLabel">Donor List</legend>
	<table class="resultList" cellpadding="0" cellspacing="0" id="resultTable" onmouseover="hideToolBar('');" >
		<tr onmouseover="hideToolBar();">
			<th>MR No</th>
			<th>Donor Status</th>
		</tr>
		<c:forEach var="dr" items="${donorRegistrylist}" varStatus="st">
			<tr  class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{mr_no: '${dr.mr_no}'});"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
				<td>${dr.mr_no}</td>
				<td>${dr.donor_status}</td>
			</tr>
		</c:forEach>
	</table>
	</fieldSet>
	<div class="screenActions" align="left">
		<a href="${cpath}/IVF/DonorRegistry.do?_method=add&mr_no=${ifn:cleanURL(param.mr_no)}">Add New Donor</a>
	</div>
</form>
</body>
</html>