<%@tag pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@attribute name="searchType" required="true"%>
<%@attribute name="fieldName" required="true"%>
<%@attribute name="showStatusField" required="false" type="java.lang.Boolean"%>
<%@attribute name="addStatusRight" required="false" type="java.lang.Boolean"%>
<%@attribute name="searchUrl" required="true"%>
<%@attribute name="searchMethod" required="true"%>
<%@attribute name="buttonLabel" required="false" %>
<%@attribute name="selectCallback" required="false" %>
<%@attribute name="invalidCallback" required="false" %>
<%@attribute name="showDuplicateMrNos" required="false" type="java.lang.Boolean"%>
<%@attribute name="activeOnly" required="false"%>
<%@attribute name="openNewWindow" required="false"%>
<%@attribute name="visitType" required="false" %>
<%@attribute name="depositType" required ="false"%>
<%@attribute name="screenID" required="false" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="activeOnly" value="${empty activeOnly ? false : activeOnly}"/>
<c:set var="addStatusRight" value="${empty addStatusRight ? false : addStatusRight}"/>
<c:set var="showDuplicateMrNos" value="${empty showDuplicateMrNos ? false : showDuplicateMrNos}"/>
<c:set var="openNewWindow" value="${empty openNewWindow ? false : openNewWindow}"/>
<c:set var="visitType" value="${empty visitType ? 'all' : visitType}"/>

<style>
	#psContainer .yui-ac-content{
		max-height:30em;overflow:auto;overflow-x:auto; /* scrolling */
	}

</style>
<c:set var="searchType" value="${empty searchType ? 'mrNo' : searchType}"/>
<form name="patientSearch" id="patientSearch" action="<c:out value='${searchUrl}' />" method="GET" target="${(openNewWindow) ? '_blank' : ''}">
	<input type="hidden" name="_method" value="${searchMethod}"/>
	<table style="float: right; margin-top: 10px">
		<tr>
			<c:if test="${!addStatusRight}">
				<td style="display: ${showStatusField == 'true' ? 'block' : 'none'}; ">
					<div style="margin-top: 2px; float: left">Active Only</div>
					<div style="margin-top: 0px; float: left">
						<input type="checkbox" name="ps_status" value="active" onChange="reInializeAc()" ${param.ps_status == 'active' ? 'checked' : ''}>
					</div>
				</td>
			</c:if>
			<td style="padding-left: 10px">
				<label title="Search Patient with mr_no, patient name, patient phone."> MR No/Patient Name</label>:&nbsp;
			</td>
			<td>
				<div id="psAutocomplete" style="width: 100px; padding-bottom: 20px">
					<input type="text" name="${fieldName}" id="ps_${searchType}" style="width: 100px"/>
					<div id="psContainer" style="right: 0px; width: 500px;" ></div>
				</div>
			</td>
			<c:if test="${addStatusRight}">
				<td style="display: ${showStatusField == 'true' ? 'block' : 'none'}; ">
					<div style="margin-top: 2px; float: left">Active Only</div>
					<div style="margin-top: 0px; float: left">
						<input type="checkbox" name="ps_status" value="active" onChange="reInializeAc()" ${empty param.ps_status? 'checked' : param.ps_status == 'active' ? 'checked' : ''}>
					</div>
				</td>
			</c:if>
			<td >
				<c:choose>
					<c:when test="${screenID eq 'emr_screen' || screenID eq 'visit_emr_screen'}">
						&nbsp;<input type="button" value="${empty buttonLabel ? 'Get Details' : buttonLabel}" onclick="return checkNotEmpty();">
					</c:when>
					<c:otherwise>
						&nbsp;<input type="submit" value="${empty buttonLabel ? 'Get Details' : buttonLabel}" onclick="return checkNotEmpty();">
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
	</table>

</form>
<div style="clear: both"></div>

<script>
	function checkNotEmpty() {
		var filterValue = document.getElementById('ps_${searchType}').value;
		if (filterValue == '') {
			alert("Please enter the search value");
			document.getElementById('ps_${searchType}').focus();
			return false;
		}
		if (${screenID eq 'emr_screen' || screenID eq 'visit_emr_screen'}) {
			addJustificationComments();
		}
	}
	function getStatus() {
		if (${activeOnly}) {
			return 'active';
		}
		var status = '';
		if (document.patientSearch.ps_status) {
			var els = document.getElementsByName('ps_status');
			for (var i in els) {
				if (els[i].checked && !els[i].disabled)
					status = els[i].value;
			}
		}
		status = (status == '' ? 'all' : status);
		return status;
	}
	var psAc = null;
	<c:choose>
		<c:when test="${searchType == 'mrNo'}">
			psAc = Insta.initMRNoAcSearch('${cpath}', 'ps_${searchType}', 'psContainer', getStatus(),
				${not empty selectCallback ? selectCallback : 'undefined'},
				${not empty invalidCallback ? invalidCallback : 'undefined'}, false, '', ${showDuplicateMrNos});
		</c:when>
		<c:when test="${searchType == 'main'}">
			psAc = Insta.initVisitAcSearch('${cpath}', 'ps_${searchType}', 'psContainer', getStatus(),'${ifn:cleanJavaScript(visitType)}',
				${not empty selectCallback ? selectCallback : 'undefined'},
				${not empty invalidCallback ? invalidCallback : 'undefined'}, false, 'main');
		</c:when>
		<c:when test="${searchType == 'visit'}">
			psAc = Insta.initVisitAcSearch('${cpath}', 'ps_${searchType}', 'psContainer', getStatus(),'${ifn:cleanJavaScript(visitType)}',
				${not empty selectCallback ? selectCallback : 'undefined'},
				${not empty invalidCallback ? invalidCallback : 'undefined'}, false, 'visit');
		</c:when>
	</c:choose>
	function reInializeAc() {
		var status = '';
		if (${activeOnly})
			status = 'active';
		else
		 	status = document.patientSearch.ps_status ? document.patientSearch.ps_status.value : '';
		status = (status == '' ? 'all' : status);
		if ('${searchType}' == 'mrNo') {
			psAc.destroy();
			psAc = Insta.initMRNoAcSearch('${cpath}', 'ps_${searchType}', 'psContainer', getStatus(),
				${not empty selectCallback ? selectCallback : 'undefined'},
				${not empty invalidCallback ? invalidCallback : 'undefined'}, false, '', ${showDuplicateMrNos});
		} if ('${searchType}' == 'visit' || '${searchType}' == 'main') {
			psAc.destroy();
			psAc = Insta.initVisitAcSearch('${cpath}', 'ps_${searchType}', 'psContainer', getStatus(),'${ifn:cleanJavaScript(visitType)}',
				${not empty selectCallback ? selectCallback : 'undefined'},
				${not empty invalidCallback ? invalidCallback : 'undefined'}, false);
		}
	}
</script>
