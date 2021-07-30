<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<html>

<head>
	<title>Send Message - Insta HMS</title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="messaging/messaging.js"/>
	<insta:link type="css" file="widgets.css"/>
	<script type="text/javascript">
		function autoCompleteDoctors() {
			var datasource = new YAHOO.widget.DS_JSArray(doctorNames);
			var autoComp = new YAHOO.widget.AutoComplete('doctor_name','doctorContainer', datasource);
			autoComp.prehighlightClassName = "yui-ac-prehighlight";
			autoComp.typeAhead = true;
			autoComp.allowBrowserAutocomplete = false;
			autoComp.minQueryLength = 1;
			autoComp.maxResultsDisplayed = 20;
			autoComp.autoHighlight = false;
			autoComp.forceSelection = false;
		}
		var doctorNames = ${doctorNames};
	</script>
	<script>
	  contextPath = "${pageContext.request.contextPath}";
	  publicPath = contextPath + "/ui/";
	</script>
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="hasResult" value="${not empty messageDataList}"/>
<c:set var="urlPrefix" value="${not empty messageGroup ? messageGroup : 'master'}"/>

<body onload="autoCompleteDoctors();onLoadRecipientList();">
<div class="pageHeader">Send Message</div>
<div class="fieldSetLabel">
	Select Recipients :
	<c:forEach var="provider" items="${providerList}" varStatus="pstatus">
		<c:choose>
			<c:when test="${provider ne _currentProvider}">
			<a href="#" onclick="selectProvider('${provider}');">${provider}</a>
			</c:when>
			<c:otherwise>
			${provider}
			</c:otherwise>
		</c:choose>
		&nbsp;&nbsp;
	</c:forEach>
</div>
<div>&nbsp;</div>
	<form action="Message.do" method="GET" name="doctorSearchform">
		<input type="hidden" name="_method" value="searchRecipients"/>
		<input type="hidden" name="_searchMethod" value="searchRecipients"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
		<input type="hidden" name="_currentProvider" value="${_currentProvider}"/>
		<input type="hidden" name="_nextProvider" value=""/>
		<input type="hidden" name="_removed_selections" value=""/>

			<insta:search form="searchform" optionsId="optionalFilter" closed="${hasResults}">
				<div class="searchBasicOpts">
					<div class="sboField">
						<div class="sboFieldLabel">Doctor Name</div>
						<div class="sboFieldInput">
							<input type="text" id="doctor_name" name="recipient_name" value="${ifn:cleanHtmlAttribute(param.recipient_name)}"
								style="width:140px;"/>
								<input type="hidden" name="recipient_name@op" value="ilike"/>
								<div id="doctorContainer"></div>
						</div>
					</div>
					<div class="sboField">
						<c:set var="emailchecked" value="${(param.recipient_email == 'n')?'checked':''}"/>
						<div class="sboFieldLabel">&nbsp;&nbsp;</div>
 						<div class="sboFieldInput">
							<input type="checkbox" name="recipient_email" value="n" ${emailchecked}/>With Email Only
							<input type="hidden" name="recipient_email@op" value="null"/>
 						</div>
					</div>
					<div class="sboField">
						<c:set var="mobilechecked" value="${(param.recipient_mobile == 'n')?'checked':''}"/>
						<div class="sboFieldLabel">&nbsp;&nbsp;</div>
						<div class="sboFieldInput">
							<input type="checkbox" name="recipient_mobile" value="n" ${mobilechecked}/>With Mobile Only
							<input type="hidden" name="recipient_mobile@op" value="null"/>
						</div>
					</div>
				</div>

				<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
					<table class="searchFormTable">
						<tr>
							<td>
								<div class="sfLabel">Department</div>
								<div class="sfField">
									<insta:selectdb name="dept_id__" multiple="true" table="department" valuecol="dept_id" displaycol="dept_name"
											orderby="dept_name" values="${paramValues.dept_id__}" />
									<input type="hidden" name="dept_id__@op" value="in"/>
								</div>
							</td>
							<td>
								<div class="sfLabel">Doctor Type</div>
								<div class="sfField">
									<insta:selectoptions name="doctor_type" value="${param.doctor_type}"
											opvalues="HOSPITAL,CONSULTANT"  optexts="HOSPITAL,CONSULTANT"
											dummyvalue="---Select---" dummyvalueId=""/>
								</div>
							</td>
							<td class="last">
								<div class="sfLabel">Status</div>
								<div class="sfField">
									<insta:checkgroup name="doctor_status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.doctor_status}"/>
										<input type="hidden" name="doctor_status@op" value="in" />
								</div>
							</td>
							<td class="last">&nbsp;</td>
						</tr>
					</table>
				</div>
			</insta:search>
	</form>
<form method="POST" action="Message.do" name="MessageForm">

<input type="hidden" name="_method" value="saveRecipients" id="_method">
<input type="hidden" name="_currentProvider" value="${_currentProvider}"/>
<input type="hidden" name="_nextProvider" value=""/>
<input type="hidden" name="_removed_selections" value=""/>
<!-- new css and new list pattern-->
	<c:if test ="${not empty messageDataList}" >
	<div class="resultList">
		<insta:paginate curPage="${pagingInfo['pageNumber']}" numPages="${pagingInfo['numPages']}" totalRecords="${pagingInfo['totalRecords']}"/>
		<table class="resultList dialog_displayColumns" cellpadding="0" cellspacing="0" align="center" width="100%" id="resultTable" >
				<tr>
					<c:set var="all_checked" value=""/>

					<c:if test="${_select_all}">
						<c:set var="all_checked" value="checked"/>
					</c:if>
					<th><input type="checkbox" name="_select_all"
						value="true" onclick="allRecipients();" ${all_checked}/></th>
					<th>Doctor Name</th>
					<th>Email</th>
					<th>Mobile</th>
					<th>Department</th>
				</tr>
				<c:forEach var="recipientData" items="${messageDataList}" varStatus="status">
					<c:set var="checked" value=""/>
					<tr class="${status.index == 0 ?'firstRow': ''} ${status.index % 2 == 0? 'even':'odd' }">
						<c:forEach var="selection" items="${currentSelections}" varStatus="selStatus">
							<c:if test="${selection eq recipientData.key}">
							<c:set var="checked" value="checked"/>
							</c:if>
						</c:forEach>
						<td><input type="checkbox" name="_selected_recipients"
								value="${recipientData.key}" ${checked} onclick="clickRecipient(this, '${recipientData.key}');"/></td>
						<td>
						<c:if test="${messageLog.map.message_mode eq 'SMS' && empty recipientData.recipient_mobile}"><img src='${cpath}/images/red_flag.gif'></c:if>
						<c:if test="${messageLog.map.message_mode eq 'EMAIL' && empty recipientData.recipient_email}"><img src='${cpath}/images/red_flag.gif'></c:if>
						${recipientData.recipient_name}</td>
						<td>${recipientData.recipient_email}</td>
						<td>${recipientData.recipient_mobile}</td>
						<td>${recipientData.doctor_dept}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</c:if>
	<insta:noresults hasResults="${hasResult}"/>
	<div class="screenActions">
	<button type="button"  class="button" accesskey="N"  onclick="return saveRecipients('${_currentProvider}');"><b><u>N</u></b>ext</button>
	<div class="legend" style="display: ${hasResult? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText">Recipient without contact information </div>
	</div>
	</div>
	</form>
</body>
</html>
