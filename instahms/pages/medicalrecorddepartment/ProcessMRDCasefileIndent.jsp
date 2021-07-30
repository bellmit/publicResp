<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
	<head>
		<title>Process Indent Casefile - Insta HMS</title>
		<meta http-equiv="Content-type" content="text/html; charset=iso-8859-1"/>
		<insta:link type="css" file="widgets.css"/>
		<insta:link type="js" file="widgets.js"/>
		<insta:link type="js" file="dashboardsearch.js"/>
		<insta:link type="js" file="medicalrecorddepartment/processcasefileindent.js"/>
		<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	</head>	

	<body onload="init();" class="yui-skin-sam">
		<div class="pageheader">Process Case file Indent</div>
		<form method="GET" action="./ProcessMRDCasefileIndent.do" name="indentSearchForm">
			<input type="hidden"	name="method" value="processCasefileIndentSearch"/>
			<input type="hidden"	name="button"/>
			<input type="hidden" name="mrdscreen" value="issue"/>
			<c:set var="indList" value="${indentList.dtoList}"/>
			<c:set var="hasResult" value="${not empty indList }"/>
			<insta:search form="indentSearchForm" optionsId="optionalFilters" closed="${hasResult}"
			validateFunction="return validateIndent()">
			<div class="searchBasicOpts">
				<div class="sboField">
				<div class="sboFieldLabel">Indentor</div>
				<div class="sboFieldInput">
					<input type="text" name="indenter"/>
				</div>
				</div>
			</div>
			<div id="optionalFilters" style="clear: both; display : ${hasResult ? 'none': 'block'}">
				<table class="searchFormTable" width="100%">
					<tr>
						<td>
							<div class="sfLabel">Indent Date</div>
							<div class="sfField">
								<div class="sfFieldSub">From:</div>				
								<insta:datewidget name="request_date0" id="request_date0" value="${param.request_date0}"/>
								<input type="text" name="request_time0" id="request_time0" value="${ifn:cleanHtmlAttribute(param.request_time0)}"	
								class="timeField"/>
							</div>
							<div class="sfField">
								<div class="sfFieldSub">To:</div>
									<insta:datewidget name="request_date1" id="request_date1"	value="${param.request_date1}"/>
									<input type="text" name="request_time1" id="request_time1" value="${ifn:cleanHtmlAttribute(param.request_time1)}" 
										class="timeField"/>
							</div>
						</td>
						<td class="last">
							<div class="sfLabel">Indenting Dept - Units	</div>
							<div class="sfField">
								<select name="dep_unit_name" id="dep_unit_name" value="${ifn:cleanHtmlAttribute(param.dep_unit_name)}" 
									${paam.dep_unit_name ? selected : ''} 		class="dropdown">
									<option value="">...Select...</option>
									<c:forEach var="dep" items="${depUnitList}">
									<option value="${dep.map.dep_unit_name}" 
										${param.dep_unit_name == dep.map.dep_unit_name ? 'selected':''}>
												${dep.map.dep_unit_name}
									</option>
									</c:forEach>
								</select>
							</div>
						</td>
					</tr>
				</table>
			</div>
			</insta:search>
			<insta:paginate curPage="${indentList.pageNumber}" numPages="${indentList.numPages}"
			totalRecords="${indentList.totalRecords}"	/>
			<div class="resultList">
				<table class="resultList" width="100%">
					<tr>
						<th>Select</th>
						<insta:sortablecolumn name="mr_no" title="MR No / Case File No"/>
						<insta:sortablecolumn name="indent_no" title="Indent No"/>
						<insta:sortablecolumn name="requested_by" title="Raised By"/>
						<insta:sortablecolumn	name="request_date" title="Raised Date"/>
						<insta:sortablecolumn name="requesting_dept" title="Department - units"/>
					</tr>
						<c:forEach var="ind" items="${indList}" varStatus="st">
						<c:set var="i" value="${st.index+1}"/>
					<tr>
						<td>
							<input type="checkbox" name="fileCheck" value="${ind.mr_no}" 
							onclick="onFileCheck(this, 'hiddenMrno${i}')"/>
							<input type="hidden" name="hiddenMrno" id="hiddenMrno${i}" />
							<input type="hidden" name="indent_no" id="indent_no${i}" value="${ind.indent_no}"/>
						</td>
						<td>${ind.mr_no}</td>
						<td>${ind.indent_no}</td>
						<td>${ind.requested_by}</td>
						<td><fmt:formatDate value="${ind.request_date}" pattern="dd-MM-yyyy HH:MM a"/></td>
						<td>${ind.requesting_dept}</td>
					</tr>
						</c:forEach>
				</table>
			</div>

			<c:if test="${not empty indList}">
			<br/>
			<dl class="accordion">
				<dt><span class="clrboth">Process Indent</span></dt>
				<dd>
				<div class="bd">

					<table class="search" width="100%">
						<tr>
							<th>Select Files:</th>
							<th>Date of Issue:</th>
							<th>Issued To:</th>
							<th>Remarks:</th>
						</tr>
						<tr>
							<td>
								<input type="radio" name="selectFiles" id="singleFile"
								value="singleFile" checked="true" onclick="onCheckRadio(this.value)">Select Files</br>
								<input type="radio" name="selectFiles" id="pageFiles"
								value="pageFiles" onclick="onCheckRadio(this.value)">Select All Files in this Page</br>
								<input type="radio" name="selectFiles" id="allFiles"
								value="allFiles" onclick="onCheckRadio(this.value)">Select All Files
							</td>
							<jsp:useBean id="now" class="java.util.Date"/>
							<td>
								<c:set var="dt" value="${now}"/>
								<fmt:formatDate value="${dt}" pattern="HH:mm" var="time"/>
								<fmt:formatDate value="${dt}" pattern="dd-MM-yyyy" var="date"/>
								<table>
									<tr>
										<td>
											<insta:datewidget name="issued_date" valid="past" value="${date}"/>
										</td>
										<td>
											<input type="text" size="5" name="issued_time"  value="${time}" class="timeField"/>
										</td>
									</tr>
								</table>
							</td>
							<td>
								<div id="wrapper_issuedTo" style="width: 15em;padding-bottom:2em;">
									<input type="text" name="mrd_issued_to" id="mrd_issued_to"/>
									<div id="issuedToContainer"></div>
								</td>
								<td>
									<input name="remarks" id="remarks" type="textbox" size="27"/>
								</td>
							</tr>
							<tr>
								<td colspan="2">
									<br/>
									<input type="button" name="issue" value="Issue to User" onclick="return validate('issue')"/>
									<input type="button" name="close" value="Close without Issue" onclick="return validate('close')"/>
									<input type="button" name="printPatients" value="Print" onclick="return printPatientsList()"/>
								</td>
							</tr>
						</table>	
					</div>
				</dd>
			</dl>
				</c:if>	
		</form>
		<script>
			var contextPath = '${cpath}'
			var mrdUserNameList = ${mrdUserNameList};
		</script>
	</body>
</html>


