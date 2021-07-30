<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<c:set var="allowDecimalsForQty" value="<%= GenericPreferencesDAO.getGenericPreferences().getAllowdecimalsforqty()%>" />
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="storemgmt.pharmacyindentlist.view.title"/></title>
<insta:link type="js" file="hmsvalidation.js"/>
<insta:link type="js" file="ajax.js"/>
<insta:link type="js" file="stores/indent.js"/>
<script type="text/javascript">
	var allowDecimalsForQty = '${allowDecimalsForQty}';
</script>
<insta:js-bundle prefix="stores.mgmt.indents"/>
</head>

<jsp:useBean id="indentStatus" class="java.util.HashMap"/>
<c:set target="${indentStatus}" property="O" value="Open"/>
<c:set target="${indentStatus}" property="A" value="Approved"/>
<c:set target="${indentStatus}" property="R" value="Rejected"/>
<c:set target="${indentStatus}" property="X" value="Cancelled"/>
<c:set target="${indentStatus}" property="P" value="Processed"/>
<c:set target="${indentStatus}" property="C" value="Closed"/>

<jsp:useBean id="indentType" class="java.util.HashMap"/>
<c:set target="${indentType}" property="S" value="Stock Transfer Indent"/>
<c:set target="${indentType}" property="U" value="Dept / Ward Issue"/>

<body class="yui-skin-sam">
<c:set var="requestingText">
<insta:ltext key="storemgmt.pharmacyindentlist.view.requesting"/>
</c:set>
<c:set var="indentText">
<insta:ltext key="storemgmt.pharmacyindentlist.view.indent"/>
</c:set>
<h1> <insta:ltext key="storemgmt.pharmacyindentlist.view.viewindent"/></h1>
<insta:feedback-panel/>
<form action="PharmacyIndent.do" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="indentNo" value="${indentdetails.map.indent_no}">

	<fieldset class="fieldSetBorder" >
	   <legend class="fieldSetLabel">${indentType[indentdetails.map.indent_type] }</legend>
		 <table   class="formtable" >
		 	<tr>
				<td class="formlabel"><insta:ltext key="storemgmt.pharmacyindentlist.view.indentno"/>:</td>
				<td class="forminfo">${indentdetails.map.indent_no}</td>
				<td class="formlabel"><insta:ltext key="storemgmt.pharmacyindentlist.view.user"/>:</td>
				<td class="forminfo" align="left">${indentdetails.map.requester_name}</td>
				<td class="formlabel">${indentdetails.map.indent_type eq 'S' ? requestingText : indentText} <insta:ltext key="storemgmt.pharmacyindentlist.view.store"/>:</td>
						<td class="forminfo"><insta:selectdb  name="indent_store" value="${indentdetails.map.indent_type eq 'S' ? indentdetails.map.dept_from : indentdetails.map.indent_store}"
					table="stores" valuecol="dept_id" displaycol="dept_name" disabled= "disabled"/></td>
				<td class="formlabel"><insta:ltext key="storemgmt.pharmacyindentlist.view.status"/>:</td>
				<td class="forminfo">${indentStatus[indentdetails.map.status]}</td>
			</tr>
			<tr>
				<td class="formlabel"></td>
				<td></td>
				<td colspan="2">
					<c:choose>
						<c:when test="${indentdetails.map.indent_type eq 'S'}">

							<table>
								<tr>
									<td class="formlabel"><insta:ltext key="storemgmt.pharmacyindentlist.view.indentstore"/>:</td>
									<td class="forminfo">${ifn:cleanHtmlAttribute(storeName)}</td>
								</tr>
							</table>
						</c:when>
						<c:otherwise>
							<table>
								<tr>
									<c:choose>
										<c:when test="${!empty indentdetails.map.dept_name}">
											<td class="formlabel"><insta:ltext key="storemgmt.pharmacyindentlist.view.dept"/>:</td>
											<td class="forminfo">${indentdetails.map.dept_name}</td>
										</c:when>
										<c:otherwise>
											<td class="formlabel"><insta:ltext key="storemgmt.pharmacyindentlist.view.ward"/>:</td>
											<td class="forminfo">${indentdetails.map.ward_name}</td>
										</c:otherwise>
									</c:choose>
								</tr>
							</table>
						</c:otherwise>
					</c:choose>
				</td>
				<c:choose>
					<c:when test="${indentdetails.map.status eq 'A' || indentdetails.map.status eq 'C'}">
						<td class="formlabel"><insta:ltext key="storemgmt.pharmacyindentlist.view.expecteddate"/>:</td>
						<td class="forminfo">
						<fmt:formatDate value="${indentdetails.map.expected_date}" pattern="dd-MM-yyyy" var="expecteddt"/>
						<fmt:formatDate value="${indentdetails.map.expected_date}" pattern="HH:mm" var="expectedtime"/>
						<c:out value="${expecteddt} ${expectedtime}"/></td>
						<td class="formlabel"><insta:ltext key="storemgmt.pharmacyindentlist.view.reason"/>:</td>
						<td class="forminfo">
							<c:if test="${indentdetails.map.status eq 'A'}">${indentdetails.map.approver_remarks}</c:if>
							<c:if test="${indentdetails.map.status eq 'C'}">${indentdetails.map.closure_reasons}</c:if>
						</td>
					</c:when>
					<c:otherwise>
						<td class="formlabel"><insta:ltext key="storemgmt.pharmacyindentlist.view.expecteddate"/>:</td>
						<td><fmt:formatDate value="${indentdetails.map.expected_date}" pattern="dd-MM-yyyy" var="expecteddt"/>
						<insta:datewidget name="expected_date" value="${expecteddt}"/></td>
						<td class="formlabel"><insta:ltext key="storemgmt.pharmacyindentlist.view.reason"/>:</td>
						<td><input type="text" size="30" name="remarks" value="${indentdetails.map.remarks}"></td>
					</c:otherwise>
				</c:choose>
			</tr>
		 </table>
	</fieldset>

   	 <fieldset class="fieldSetBorder" >
      <legend class="fieldSetLabel" ><insta:ltext key="storemgmt.pharmacyindentlist.view.itemslist"/></legend>
		<table  id="indentItemListTab" class="datatable"  width="100%">
			<tr>
				<th ><insta:ltext key="storemgmt.pharmacyindentlist.view.item"/></th>
				<th ><insta:ltext key="storemgmt.pharmacyindentlist.view.qty"/></th>
				<th ><insta:ltext key="storemgmt.pharmacyindentlist.view.qtyavbl.instore"/></th>
				<th><insta:ltext key="storemgmt.pharmacyindentlist.view.pkgsize"/></th>
				<th><insta:ltext key="storemgmt.pharmacyindentlist.view.unituom"/></th>
			</tr>
			<c:forEach var="indent" items="${indentlist}" varStatus="status">
				<c:set var="i" value="${status.index}"/>
				<tr id="row${i}">
					<td><label id="itemnamelbl${i}">${indent.map.medicine_name}</label>
						<input type="hidden" id="medidlbl${i}" value="${indent.map.medicine_id}"/>
						<input type="hidden" id="indentnolbl${i}" value="${indent.map.indent_no}"/></td>
					<td><label id="itemqtylbl${i}">${indent.map.qty}</label></td>
					<td><label id="availqtylbl${i}">${indent.map.availableqty}</label></td>
					<td>${indent.map.issue_base_unit}</td>
					<td>${indent.map.issue_units}</td>
				</tr>
			</c:forEach>
		</table>
	</fieldset>
	<div class="screenActions">
   		<a href="${cpath }/stores/storesIndent.do?_method=list&status=O&sortOrder=indent_no&sortReverse=true"><insta:ltext key="storemgmt.pharmacyindentlist.view.backtodashboard"/></a>
	</div>
</form>
</body>
</html>
