<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />


<html>
	<c:set var="visitList" value="${pagedList.dtoList}" />
	<c:set var="hasResults" value="${not empty visitList}" />
	
	<head>
		<meta name="i18nSupport" content="true"/>
		<title><insta:ltext key="patient.diag.status.title"/></title>
		<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
		<insta:link type="css" file="widgets.css" />
		<insta:link type="js" file="dashboardsearch.js"/>
		<insta:link type="js" file="visitdetailssearch/patientdiagstatus.js"/>	
		<insta:js-bundle prefix="patient.diag.status"/>
		<style type="text/css">
			 .reportStatus{
			   	 border:1px solid red;
			   	 border-radius:6px;
			   	 padding:2px 4px 2px 4px;
			   	 font-size:9px;
			   }
		</style>
		<script>
			var cpath = '${cpath}';
			var optimizedLabReportPrint = '${optimizedLabReportPrint}';
		</script>
	</head>

	<body onload="init();showPatientTestReportStatusFTUE()">
		<c:set var="select">
		 <insta:ltext key="selectdb.dummy.value"/>
		</c:set>	
		<c:set var="visitid">
			 <insta:ltext key="patient.diag.status.visitid"/>
		</c:set>		
		<c:set var="visitdate">
			 <insta:ltext key="patient.diag.status.visitdate"/>
		</c:set>		
		<c:set var="condstatus">
			<insta:ltext key="patient.diag.status.teststatus.new"/>,
			<insta:ltext key="patient.diag.status.teststatus.inprogress"/>,
			<insta:ltext key="patient.diag.status.teststatus.completed"/>,
			<insta:ltext key="patient.diag.status.teststatus.validated"/>,
			<insta:ltext key="patient.diag.status.teststatus.signedoff"/>,
			<insta:ltext key="patient.diag.status.teststatus.revisioncompleted"/>,
			<insta:ltext key="patient.diag.status.teststatus.revisionvalidated"/>,
			<insta:ltext key="patient.diag.status.teststatus.revisioninprogress"/>,
			<insta:ltext key="ui.label.patient.arrived"/>,
			<insta:ltext key="patient.diag.status.teststatus.scheduledfortranscriptionist"/>,
			<insta:ltext key="patient.diag.status.teststatus.conductioncompleted"/>,
			<insta:ltext key="patient.diag.status.teststatus.changerequired"/>,
			<insta:ltext key="patient.diag.status.teststatus.canceled"/>
		</c:set>
			<c:set var="samplestatus">
			<insta:ltext key="patient.diag.status.samplestatus.notcollected"/>,
			<insta:ltext key="patient.diag.status.samplestatus.pending"/>,
			<insta:ltext key="patient.diag.status.samplestatus.collected"/>,
			<insta:ltext key="patient.diag.status.samplestatus.asserted"/>,
			<insta:ltext key="patient.diag.status.samplestatus.rejected"/>,
			<insta:ltext key="patient.diag.status.samplestatus.transferred"/>,
			<insta:ltext key="patient.diag.status.samplestatus.received"/>,
			<insta:ltext key="patient.diag.status.samplestatus.notapplicable"/>
		</c:set>
	
		<h1><insta:ltext key="patient.diag.status.header"/></h1>
		<c:set var="visitList" value="${pagedList.dtoList}" />		
		<insta:feedback-panel/>
		<div style="display:none; margin-bottom:10px; padding:10px 0 10px 10px; height: 15px; background-color:#FFC;" class="brB brT brL brR" id="infoMsgDiv">
		<div class="fltL" style="width: 25px; margin:-1px 0 0 3px;" id="infoImg"> <img src="${cPath}/images/information.png" /></div>
		<div class="fltL"  style="margin:0px 0 0 5px ; width:865px;" id="infoDiv"></div>
		</div>
		<c:set var="method" value="list"/>
		<c:set var="actionURL" value="${cpath}/PatientDiagStatus.do"/>
		<c:set var="searchMethod" value="list"/>
		
		<form name="patientDiagStatusForm" action="${actionURL}">			
			<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(method)}"/>
			<input type="hidden" name="_searchMethod" value="${searchMethod }"/>
			<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
			<input type="hidden" name="_external_visit_id" value=""/>
			
			<insta:search form="patientDiagStatusForm" optionsId="optionalFilter" closed="${hasResults}" validateFunction="doSearch()">
				<div class="searchBasicOpts">
					<div class="sboField">
						<div class="sboFieldLabel"><insta:ltext key="patient.diag.status.mrno.patientname"/>:</div>
						<div class="sboFieldInput">
							<div id="mrnoAutoComplete">
								<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
								<div id="mrnoContainer" style="width: 300px"></div>
							</div>
						</div>
					</div>
				</div>	
				<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
					<table class="searchFormTable">
						<tr>
							<td>
								<div class="sfLabel"><insta:ltext key="patient.diag.status.department"/></div>
								<div class="sfField">			
									<insta:selectdb name="ddept_id" table="diagnostics_departments" valuecol="ddept_id"
										displaycol="ddept_name" value="${empty param.ddept_id ? userDept : param.ddept_id}"
									    dummyvalue="${select}" filtered="true" filtercol="status"
									    filtervalue="A"/>			
								</div>
								<div class="sfLabel"><insta:ltext key="patient.diag.status.sampleno"/></div>
								<div class="sfField">
									<input type="text" name="sample_no" value="${ifn:cleanHtmlAttribute(param.sample_no)}"/>
								</div>								
							</td>
							<td style="width: 40px">
								<div class="sfLabel"><insta:ltext key="patient.diag.status.visitdate"/></div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="patient.diag.status.visitdate.from"/>:</div>
									<insta:datewidget name="reg_date" id="reg_date0" valid="past"	value="${paramValues.reg_date[0]}" />
									<input type="hidden" name="reg_date@type" value="date"/>
									<input type="hidden" name="reg_date@op" value="ge,le"/>
									<input type="hidden" name="reg_date@cast" value="y"/>
								</div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="patient.diag.status.visitdate.to"/>:</div>
									<insta:datewidget name="reg_date" id="reg_date1" valid="past"	value="${paramValues.reg_date[1]}" />
								</div>								
							</td>
							<td style="width: 187px">
								<div class="sfLabel"><insta:ltext key="patient.diag.status.teststatus"/></div>
								<div class="sfField">
									<insta:checkgroup name="conducted" selValues="${paramValues.conducted}"
									opvalues="N,P,C,V,S,RC,RV,RP,MA,TS,CC,CR,X" optexts="${condstatus}"/>
								</div>
							</td>
							<td>
								<div class="sfLabel"><insta:ltext key="patient.diag.status.samplestatus"/></div>
								<div class="sfField">
									<insta:checkgroup name="sample_status" selValues="${paramValues.sample_status}"
									opvalues="NC,P,C,A,R,T,RC,NA" optexts="${samplestatus}"/>
								</div>								
							</td>					
						</tr>
					</table>
				</div>									
			</insta:search>			
		</form>
		
		<div style="margin-top: 10px;">
			<insta:patientgeneraldetails mrno="${param.mr_no}" addExtraFields="true"/>
		</div>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
		<form name="resultsForm" method="POST">
			<div class="resultList">
				<table class="resultList" width="100%" id="dashboardTable" style="empty-cells: show">
					<tr>
						<insta:sortablecolumn name="patient_id" title="${visitid}"/>
						<insta:sortablecolumn name="reg_date" title="${visitdate}"/>
						<th>
							<input type="checkbox" name="checkAllForReport"
									onclick="return checkOrUncheckAll('reportId', this)"/>
							<insta:ltext key="patient.diag.status.report"/>
						</th>
						<c:choose>
							<c:when test="${islabNoReq == 'Y'}">
								<th>
									<insta:ltext key="patient.diag.status.testname.prescribeddate.labno.order"/></br>
									<label style="font-size: 10px;" title='<insta:ltext key="patient.diag.status.package"/>'>
										(<insta:ltext key="patient.diag.status.package"/>)
									</label>								
								</th>
							</c:when>
							<c:otherwise>
								<th>
									<insta:ltext key="patient.diag.status.testname.prescribeddate.order"/></br>
									<label style="font-size: 10px;" title='<insta:ltext key="patient.diag.status.package"/>'>
										(<insta:ltext key="patient.diag.status.package"/>)
									</label>								
								</th>
							</c:otherwise>
						</c:choose>
						<th><insta:ltext key="patient.diag.status.teststatus"/></th>
						<th><insta:ltext key="patient.diag.status.samplestatus"/></th>	
						<th><insta:ltext key="patient.diag.status.collectioncurrentcenter"/></th>											
					</tr>

					<%--Schedules Begins --%>
			
					<c:forEach var="visitBean" items="${visitList}" varStatus="st">
						<c:set var="visitId" value="${visitBean.patient_id}"/>
						<c:set var="visitReports" value="${visitDetails[visitId]}"/>	<%-- is a list of list of tests --%>
						<c:set var="visit" value="${visitReports[0][0].map}"/>	<%-- use first first bean as test --%>
			
						<tr>
							<td>${ifn:cleanHtml(visit.patient_id)} (${ifn:cleanHtml(visit.patient_sponsor_type)})</td>
							<td><fmt:formatDate value="${visit.reg_date}" pattern="dd-MM-yyyy"/></td>
							<td colspan="5">&nbsp;</td>
						</tr>
			
						<c:forEach var="reportTestList" items="${visitReports}">  <%-- list of tests for this report --%>
							<c:set var="report" value="${reportTestList[0].map}"/>	<%-- use first test bean as report --%>
			
							<tr>
								<td class="indent" colspan="2">&nbsp;</td>
								<td class="subResult" valign="top" style="height:37px">
									<input type="checkbox" name="reportId" value="${report.report_id}"
											${(empty report.report_id || report.report_id == 0 || report.external_report_ready eq 'Y') ? 'disabled' : ''}/>
											
									<c:choose>
										<c:when test="${report.report_id == -1 && report.external_report_ready eq 'Y'}">										
                                            <!--  show no tag -->
											<a href="#" onclick="return getExternalReport('${report.org_patient_id}')">External report</a>
										</c:when>
										<c:otherwise>
										<insta:truncLabel value="${empty report.report_id || report.report_id == 0 ? 
										'(No Report)' : report.report_name}" length="12"/> 
										</c:otherwise> 
									</c:choose>
									
											<div style="margin-left:22px;margin-top:2px">
											<c:choose>
												<c:when test="${empty report.report_id || report.report_id == 0}">										
                                                     <!--  show no tag -->
												</c:when>
												<c:when test="${report.partial_patient_due  == 0 && report.signed_off == 'Y'}"> 
													 <span class="reportStatus" style="border-color:green;color:green">Ready for Handover</span>
												 </c:when>
												 <c:when test="${report.partial_patient_due  > 0 && report.signed_off == 'Y'}">
												     <span class="reportStatus" style="color:red">Bill Pending</span> 
												 </c:when>
												<c:otherwise> 
												</c:otherwise> 
											</c:choose>
											</div>
								</td>
								<td valign="top">
									<c:forEach var="testBean" items="${reportTestList}">
										<c:set var="test" value="${testBean.map}"/>
										<c:if test="${islabNoReq == 'Y'}">
											<c:set var="labnoText">-(${ifn:cleanHtml(test.labno)})</c:set>
										</c:if>
										<c:set var="sampleNoTxt">
											<c:choose>
												<c:when test="${not empty test.sample_no}">${test.sample_no}-</c:when>
											</c:choose>
										</c:set>
										<fmt:formatDate value="${test.pres_date}" pattern="dd-MM-yyyy" var="presDate"/>
										<div style="margin-top: 5px">
											${sampleNoTxt}<c:out value="${test.test_name}"/>-(${presDate})${labnoText}-[${test.common_order_id}]
										</div>
										<c:if test="${not empty test.package_name}">
											<div style="clear:both; "></div>
											<div style="valign: top">
												<label style="font-size: 12px;">${test.package_name}</label>
											</div>
										</c:if>
									</c:forEach>
								</td>
								<td valign="top">
									<c:forEach var="testBean" items="${reportTestList}">
										<c:set var="test" value="${testBean.map}"/>
										<c:set var="testStatus">
											<c:choose>
												<c:when test="${test.conducted eq 'N'}">
													<insta:ltext key="patient.diag.status.teststatus.new"/>
												</c:when>
												<c:when test="${test.conducted eq 'X'}">
													<insta:ltext key="patient.diag.status.teststatus.canceled"/>
												</c:when>
												<c:when test="${test.conducted eq 'P'}">
													<insta:ltext key="patient.diag.status.teststatus.inprogress"/>
												</c:when>
												<c:when test="${test.conducted eq 'CC'}">
													<insta:ltext key="patient.diag.status.teststatus.conductioncompleted"/>	
												</c:when>
												<c:when test="${test.conducted eq 'TS'}">
													<insta:ltext key="patient.diag.status.teststatus.scheduledfortranscriptionist"/>
												</c:when>
												<c:when test="${test.conducted eq 'CR'}">
													<insta:ltext key="patient.diag.status.teststatus.changerequired"/>
												</c:when>
												<c:when test="${test.conducted eq 'C'}">
													<insta:ltext key="patient.diag.status.teststatus.completed"/>
												</c:when>
												<c:when test="${test.conducted eq 'V'}">
													<insta:ltext key="patient.diag.status.teststatus.validated"/>
												</c:when>
												<c:when test="${test.conducted eq 'S'}">
													<insta:ltext key="patient.diag.status.teststatus.signedoff"/>
												</c:when>
												<c:when test="${test.conducted eq 'RP'}">
													<insta:ltext key="patient.diag.status.teststatus.revisioninprogress"/>
												</c:when>
												<c:when test="${test.conducted eq 'RC'}">
													<insta:ltext key="patient.diag.status.teststatus.revisioncompleted"/>
												</c:when>
												<c:when test="${test.conducted eq 'RV'}">
													<insta:ltext key="patient.diag.status.teststatus.revisionvalidated"/>
												</c:when>
												<c:when test="${test.conducted eq 'RAS'}">
													<insta:ltext key="patient.diag.status.teststatus.reconductedaftersignoff"/>
												</c:when>
												<c:when test="${test.conducted eq 'RBS'}">
													<insta:ltext key="patient.diag.status.teststatus.reconductedbeforesignoff"/>
												</c:when>
												<c:when test="${test.conducted eq 'NRN'}">
													<insta:ltext key="patient.diag.status.teststatus.new"/>	
												</c:when>
												<c:when test="${test.conducted eq 'CRN'}">
													<insta:ltext key="patient.diag.status.teststatus.completed"/>	
												</c:when>
												<c:when test="${test.conducted eq 'MA'}">
													<insta:ltext key="ui.label.patient.arrived"/>	
												</c:when>
												<c:otherwise>
													<insta:ltext key="patient.diag.status.teststatus.noconductionrequired"/>
												</c:otherwise>
											</c:choose>
										</c:set>
										<div style="margin-top: 5px"><c:out value="${testStatus}"/></div>
										<c:if test="${not empty test.package_name}">
											<br/>
										</c:if>
									</c:forEach>
								</td>																
								<td valign="top">
									<c:forEach var="testBean" items="${reportTestList}">
										<c:set var="test" value="${testBean.map}"/>
										<c:set var="sampleStatus">
											<c:choose>
												<c:when test="${test.sample_status eq 'C'}">
													<insta:ltext key="patient.diag.status.samplestatus.collected"/>
												</c:when>
												<c:when test="${test.sample_status eq 'P'}">
													<insta:ltext key="patient.diag.status.samplestatus.pending"/>
												</c:when>
												<c:when test="${test.sample_status eq 'A'}">
													<insta:ltext key="patient.diag.status.samplestatus.asserted"/>
												</c:when>
												<c:when test="${test.sample_status eq 'R'}">
													<insta:ltext key="patient.diag.status.samplestatus.rejected"/>
												</c:when>
												<c:when test="${test.sample_status eq 'T'}">
													<insta:ltext key="patient.diag.status.samplestatus.transferred"/>
												</c:when>
												<c:when test="${test.sample_status eq 'RC'}">
													<insta:ltext key="patient.diag.status.samplestatus.received"/>
												</c:when>
												<c:otherwise>
													<c:choose>
														<c:when test="${test.sample_needed eq 'y'}">
															<insta:ltext key="patient.diag.status.samplestatus.notcollected"/>
														</c:when>
														<c:otherwise>
															<insta:ltext key="patient.diag.status.samplestatus.notapplicable"/>
														</c:otherwise>
													</c:choose>												
												</c:otherwise>
											</c:choose>
										</c:set>										
										<div style="margin-top: 5px"><c:out value="${sampleStatus}"/></div>
										<c:if test="${not empty test.package_name}">
											<br/>
										</c:if>
									</c:forEach>
								</td>
								<td valign="top">
									<c:forEach var="testBean" items="${reportTestList}">
										<c:set var="test" value="${testBean.map}"/>
											<div style="margin-top: 5px">
												${test.coll_center_name}/<c:out value="${test.outsource_name}"/>
											</div>	
											<c:if test="${not empty test.package_name}">
												<br/>
											</c:if>
 									</c:forEach>								
								</td>
							</tr>
						</c:forEach> <%-- End of Report List --%>
					</c:forEach> <%--Schedules Ends --%>					
				</table>
			</div>
			<c:if test="${empty initialScreen}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>	
			<div style="float: left; margin-top: 20px" style="display: ${hasResults ? 'block' : 'none'}">
				<button type="button" accessKey="P" onclick="PrintAll();">
					<label><b><u><insta:ltext key="patient.diag.status.p"/></u></b><insta:ltext key="patient.diag.status.rintReports"/>
					</label>
				</button>
			</div>
			<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
				<div class="flagText"><insta:ltext key="patient.diag.status.legend.sponsor"/></div>
				<div class="flagText" style="padding-left: 10px"><insta:ltext key="patient.diag.status.legend.retail"/></div>
			</div>			
		</form>
		<!--  First time user Experience -->
		<a href="#" id="report-tour" style="display: none;"></a>
		<div style="display: none;">
		  <ul id="report-tour-steps">
		    <li data-id=".step-1" data-position="none">
		      <h2><insta:ltext key="laboratory.signedoffreportslist.search.newTokens"/></h2>
		      	<table class="ftue_Table" style="background: rgb(242,249,255)">
						<tr>
							<td style="width:38%;">
		      					<p><span class="reportStatus" style="border-color:green;color: green"><insta:ltext key="laboratory.signedoffreportslist.search.readyForHandoverStatusHead"/></span></p>
		      				</td>
		      				<td>	
		      					<p><insta:ltext key="laboratory.signedoffreportslist.search.readyForHandoverStatusBody"/></p>
		      				</td>
		      			</tr>
		      			<tr>
		      				<td style="width:38%;">
		      					<p><span class="reportStatus" style="color:red"><insta:ltext key="laboratory.signedoffreportslist.search.billPendingStatusHead"/></span></p>
		      				</td>
		      				<td>
		      					<p><insta:ltext key="laboratory.signedoffreportslist.search.billPendingStatusBody"/></p>
		    				</td>
		    			</tr>
		    	</table>
		    </li>
		  </ul>
		</div>
		<!--  FTUE end -->
	</body>
</html>

