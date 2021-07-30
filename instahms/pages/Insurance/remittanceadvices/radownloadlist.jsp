<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.integration.URLRoute"%>
<c:set var="pagePath" value="<%=URLRoute.REMITTANCE_UPLOAD %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<jsp:useBean id="remittanceStatusDisplay" class="java.util.HashMap"/>
<c:set target="${remittanceStatusDisplay}" property="F" value="Failed"/>
<c:set target="${remittanceStatusDisplay}" property="I" value="Partially Completed"/>
<c:set target="${remittanceStatusDisplay}" property="S" value="Scheduled"/>
<c:set target="${remittanceStatusDisplay}" property="N" value="Not Processed"/>
<c:set target="${remittanceStatusDisplay}" property="C" value="Completed"/>
<c:set target="${remittanceStatusDisplay}" property="P" value="Processing"/>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Remittance download List - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:js-bundle prefix="insurance.remittance"/>
	
	<script type="text/javascript">		
		function DownloadProcess(rowId) {
		    var file_name = document.getElementById("h_file_name"+rowId).value;
		    var received_start_date = document.getElementById("received_start_date").value;
		    var received_end_date = document.getElementById("received_end_date").value;
		    var tpa_id = document.getElementById("h_tpa_id"+rowId).value;
		   	var fileId = document.getElementById("h_fileId"+rowId).value;
		   	var status = document.getElementById("h_is_downloaded"+rowId).value == "Y" ? "downloaded" : "new";
			var account_group_id = document.getElementsByName("account_group_id")[0].value;
			var processing_statuses = "";
			var checkboxes = document.getElementsByName("processing_status");
			for(var i=0; i<checkboxes.length; i++){
				if(checkboxes[i].checked){
					processing_statuses += "&processing_status=" + checkboxes[i].value;
				}
			}
			
			var processBtn = document.getElementById("processBtn"+rowId);
		     
		     processBtn.disabled=true;
		     processBtn.removeAttribute('href');    
		     processBtn.style.textDecoration = 'none';
		     processBtn.style.cursor = 'default';
		     
		     var processTd = document.getElementById("processTd"+rowId);
		     
		     processTd.innerHTML = "Process";
		     
		     window.open(document.remdownloadform.action+"?file_name="+file_name+"&tpa_id="+tpa_id
		    		+"&fileId="+fileId+"&received_start_date="+received_start_date
		    		+"&received_end_date="+received_end_date+"&status="+status
		    		+"&account_group_id="+account_group_id + processing_statuses,'_blank');
		     
		    return true;
		}
		
		function Download(rowId) {
			var file_name = document.getElementById("h_file_name"+rowId).value;
			var received_date = document.getElementById("h_received_date"+rowId).value;
			var tpa_id = document.getElementById("h_tpa_id"+rowId).value;
			var fileId = document.getElementById("h_fileId"+rowId).value;
			var isDownloaded = document.getElementById("h_is_downloaded"+rowId).value;
			var account_group_id = document.getElementById("h_account_group_id"+rowId).value;

			window.open("${cpath}${pagePath}/raDownload.htm"+"?file_name="+file_name+"&is_downloaded="+isDownloaded+"&tpa_id="+tpa_id
		    		+"&fileId="+fileId+"&account_group_id="+account_group_id,"_parent");
		    return true;
		}
		
		function checkIsRecovery(index){
			 if (document.getElementById('is_recovery'+index).checked) {
				 document.getElementById('h_is_recovery'+index).value = 'Y';
			 }else{
				 document.getElementById('h_is_recovery'+index).value = 'N';
			 }
			 return true
		}
		
		function validateSubmit(){
			var fromDate = document.getElementById("received_start_date").value;
			var toDate = document.getElementById("received_end_date").value;
			if(parseDateStr(fromDate) > parseDateStr(toDate)) {
				showMessage("js.insurance.remittance.download.date.tofromdate.validation");
				return false;
			}
			if (daysDiff(parseDateStr(fromDate), parseDateStr(toDate)) > 61) {
				showMessage("js.insurance.remittance.download.date.range.validation");
				return false;
			}
		}
	</script>
</head>
<body>
	<c:set var="activeStatus">
		<insta:ltext key="billing.billlist.list.newtransactionsonly"/>,
	</c:set>
	<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>
	<h1>Remittance Advice Download</h1>
	<c:set var="actionUrl" value="${cpath}/${pagePath}/remittanceDownloadList.htm"/>
	<c:set var="downloadProcessUrl" value="${cpath}/${pagePath}/raDownloadProcess.htm"/>
	<c:set var="downloadUrl" value="${cpath}/${pagePath}/raDownload.htm"/>
	<c:set var="status" value="${paramValues.status[0]}"/>
	<!--aaa ${status} -->
	<insta:feedback-panel />
	
<form name="remform" action="${actionUrl}" method="GET" >
			
		<insta:search-lessoptions form="remform" validateFunction="return validateSubmit()">
			<table class="searchFormTable">
					<tr>
                        <td>
                            <div><insta:ltext key="js.insurance.remittance.download.remittance.file.name.label"/>
                                <div class="sfField">
                                    <input type="text" name="rm_file_name" id="rm_file_name" value=""/>
                                </div>
                            </div>
                        </td>
						<td>
							<div class="sfLabeMR No.l"><b>Received Date :</b></div>
								<div class="sfField">
									<div class="sfFieldSub">From</div>
									<insta:datewidget name="received_start_date" id="received_start_date" value="${received_start_date}" />
								</div>
								
								<div class="sfField">
									<div class="sfFieldSub">To</div>
									<insta:datewidget name="received_end_date" id="received_end_date" value="${received_end_date}" />
								</div>
						</td>
						<td>
							<div class="sfLabel"><insta:ltext key="search.patient.visit.tpa.sponsor"/>
								<div class="sfField">
								<insta:selectdb name="primary_sponsor_id" table="tpa_master" valuecol="tpa_id" class="dropdown"
									displaycol="tpa_name" value="${param.primary_sponsor_id}" dummyvalue="(All)" orderby="tpa_name"/>
								</div>
							</div>
							
							<div class="sfLabel"><insta:ltext key="ui.label.account.group"/>
								<div class="sfField">
								<insta:selectdb name="account_group_id" table="account_group_master" valuecol="account_group_id" class="dropdown"
									displaycol="account_group_name" value="${param.account_group_id}" orderby="account_group_name"/>
								</div>
							</div>
						</td>
						<td>
								<div class="sboFieldLabel">Status:</div>
									<insta:checkgroup name="processing_status"
										optexts="Not Processed,Scheduled,Processing,Failed,Completed,Partially Completed"
										opvalues="N,S,P,F,C,I" selValues="${paramValues.processing_status}" />
						</td>
						<td>
							<div class="sfLabel">Remittance List Filter:</div>
							<div class="sfField">
							    <input type="radio" id="downloaded_radio"
							     name="status" value="downloaded" ${paramValues.status[0] eq 'downloaded' ? 'checked': ''}>
							    <label for="downloaded_radio" >Downloaded Transactions only </label>
							</div>
							<div class="sfField">
							    <input type="radio" id="new_radio"
							     name="status" value="new" ${paramValues.status[0] eq 'new' ? 'checked': ''} >
							    <label for="new_radio">New Transactions only</label>								
							</div>
						</td>
					</tr>
			</table>	
		</insta:search-lessoptions>
</form>
<form name="remdownloadform" method="GET" action="${downloadProcessUrl}" >
		<div class="detailList" >
			<table class="detailList"  id="resultTable">
				<tr onmouseover="hideToolBar();">
					<th></th>
					<th>Sl No.</th>
					<insta:sortablecolumn name="tpa_name" title="TPA/Sponsor Name"/>
					<insta:sortablecolumn name="file_name" title="File Name"/>
					<insta:sortablecolumn name="received_date" title="Received Date"/>
					<th><insta:ltext key="ui.label.status"/></th>
					<th></th>
					<th></th>
				</tr>

				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<c:if test="${empty param.primary_sponsor_id || param.primary_sponsor_id == record.tpaId}">
					
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}">
						<td>
							<input type="hidden" name="fileId" id="h_fileId${st.index}" value='${record.fileId}' />
							<input type="hidden" name="fileName" id="h_file_name${st.index}" value='${record.fileName}' />
							<input type="hidden" name="senderId" id="h_sender_id${st.index}" value='${record.senderId}' />
							<input type="hidden" name="tpaId" id="h_tpa_id${st.index}" value='${record.tpaId}' />
							<input type="hidden" name="accountGroupId" id="h_account_group_id${st.index}" value='${record.accountGroupId}' />
							<input type="hidden" name="processingStatus" id="h_processing_status${st.index}" value='${record.processingStatus}' />
							<input type="hidden" name="transactionDate" id="h_received_date${st.index}" value='${record.transactionDate}' />
							<input type="hidden" name="is_downloaded" id="h_is_downloaded${st.index}" value="${'new' eq paramValues.status[0]? 'N':'Y'}" />							
						</td>
						<td>${(st.index + 1)}</td>
						<td title="${record.tpaName}"> <insta:truncLabel value="${record.tpaName}" length="25"/> </td>
						<td title="${record.fileName}"> <insta:truncLabel value="${record.fileName}" length="55" /> </td>
						<td title ="${record.transactionDate}">${record.transactionDate}</td>
						<td title="${remittanceStatusDisplay[record.processingStatus]}"> <insta:truncLabel value="${remittanceStatusDisplay[record.processingStatus]}" length="25"/> </td>
						<td><a href="#" onclick="return Download(${st.index});"><insta:ltext key="ui.label.download"/></a></td>
						<td id="processTd${st.index}">
						<c:choose>
						<c:when test="${record.processingStatus == 'F' || record.processingStatus == 'N'}">
						<a id="processBtn${st.index}" href="#" onclick="return DownloadProcess(${st.index});"><insta:ltext key="ui.label.process"/></a>
						</c:when>
						<c:otherwise>
						<insta:ltext key="ui.label.process"/>
						</c:otherwise>
						</c:choose>
						</td>
					</tr>
					</c:if>
				</c:forEach>
			</table>

			<c:if test="${empty pagedList.dtoList}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>
		</div>
			<c:url var="Url" value="${pagePath}/add.htm"/>
			<div class="screenActions" style="float: left">
				<a href="${Url}">Upload New Remittance</a> | 
			</div>
			<c:url var="Url" value="${pagePath}/list.htm?sortReverse=false"/>
			<div class="screenActions" style="float: left">
				<a href="${Url}">Remittance Advice Log</a>
			</div>		
</form>

</body>
</html>