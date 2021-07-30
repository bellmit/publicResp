<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions"  prefix="fn"%>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<script type="text/javascript">
	var category = '${ifn:cleanJavaScript(category)}'
	</script>
	<title><insta:ltext key="laboratory.pendingtests.list.pending"/> ${screenId eq 'lab_pending_samples' or screenId eq 'lab_pending_samples_search'?'Samples':'Tests'}
	 ${screenId eq 'lab_unfinished_tests_search' or screenId eq 'lab_pending_samples_search'?'Search':''} <insta:ltext key="laboratory.pendingtests.list.instahms"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css" />
	<insta:link type="js" file="diagnostics/unfinishedtests.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
	<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
	<insta:js-bundle prefix="laboratory.radiology.batchconduction"/>
	<insta:js-bundle prefix="laboratory.radiology.pendingtests"/>
	<insta:js-bundle prefix="diagnostics.diagdashboards"/>
	<script src="<%=request.getContextPath()%>/gettests.do?${test_timestamp}&${ifn:cleanJavaScript(sesHospitalId)}&module=${ifn:cleanJavaScript(module)}"></script>
	<script>
		var toolbarOptions = getToolbarBundle("js.laboratory.radiology.batchconduction.toolbar");
		var allTestNames = deptWiseTestsjson;
		var outHouses = ${outHouses};
		var inHouses = ${inHouses};
		var cpath = '${cpath}';
		var extraDetails = [];
		var centerId = ${centerId};
		var max_centers_inc_default = ${max_centers_inc_default};
		var sampleCollectionCenterId = ${sampleCollectionCenterId};
		var collectSampleUrl = '${urlRightsMap.lab_pending_samples_search == 'A' ? 'PendingSamplesSearch' : 'PendingSamples'}'; 

		var cancelUrl = '';
		var patientToolbar = null;

		if (category == "DEP_RAD") {
			baseUrl = 'pages/DiagnosticModule/radiology.do';
		}
		var baseModule = category == 'DEP_LAB' ? 'Laboratory' : 'Radiology';
		var module = category == 'DEP_LAB'? 'Lab' : 'Radiology';

		if (category == 'DEP_LAB') {
			if('${screenId}' != 'lab_pending_samples' && '${screenId}' != 'lab_pending_samples_search'){
				patientToolbar = {};
					patientToolbar.Edit = { title: toolbarOptions["vieweditresults"]["name"],
						imageSrc: 'icons/Edit.png', href: baseModule + '/editresults.do?_method=getBatchConductionScreen',
						onclick : 'changeURL',
						show: ${urlRightsMap.lab_edit_results == 'A'}
					};


					patientToolbar.AssignOuthouse = {title: toolbarOptions["outhouse"]["name"], imageSrc: 'icons/Edit.png',

						href: 'Laboratory/selectouthouse.do?_method=getOuthouseScreen',
						show: ${urlRightsMap.lab_select_outhouse == 'A'}
					};
					patientToolbar.EditReagents = {title: toolbarOptions["editreagents"]["name"], imageSrc: 'icons/Edit.png',
						href: 'Laboratory/editresults.do?_method=modifyReagents'
					};
					patientToolbar.Manage={ title: toolbarOptions["managereports"]["name"], imageSrc: 'icons/Edit.png',
						href: 'Laboratory/managereports.do?_method=getLabReport',
						show: ${urlRightsMap.lab_manage_reports == 'A'}
					};
					patientToolbar.Print = {title: toolbarOptions["print"]["name"], imageSrc: 'icons/Print.png',
						href: 'pages/DiagnosticModule/DiagReportPrint.do?_method=printReport', target: '_blank'
					};
					patientToolbar.PrintVisitReports =  {title: toolbarOptions["printreports.visitwise.in.brackets"]["name"], imageSrc: 'icons/Print.png',
						href: 'pages/DiagnosticModule/DiagReportPrint.do?_method=printSelectedReports&using=visitId', target: '_blank'
					};
					patientToolbar.PrintSampleWorkSheet =  {title: toolbarOptions["printsampleworksheet"]["name"], imageSrc: 'icons/Print.png',
						href: 'Laboratory/SampleWorkSheetPrint.do?_method=printSampleWorkSheet', target: '_blank'
					};
					patientToolbar.collectsample = { title: toolbarOptions["collectsample"]["name"],	imageSrc: 'icons/Edit.png',
						href: 'Laboratory/'+collectSampleUrl+'.do?_method=getSampleCollectionScreen&title=Collect Sample&default_sample_status=C',
						show: ${urlRightsMap.lab_pending_samples == 'A' || urlRightsMap.lab_pending_samples_search == 'A'}
					};
					patientToolbar.AddDoc= { title: toolbarOptions["viewadddocument"]["name"], imageSrc: 'icons/Edit.png',
						href: 'Laboratory/TestDocumentsList.do?_method=searchTestDocuments',
						show: ${urlRightsMap.lab_test_documents == 'A'}
					};
				patientToolbar.ReconductTest ={
						title: toolbarOptions["reconduct"]["name"],
						imageSrc: 'icons/Report.png',
						onclick : 'changeURL',
						href:'DiagnosticLabModule/'+module+'ReconductTestList.do?_method=getReconductTestListScreen',
						target:'_blank'
					};
				patientToolbar.EditIncomigPatient = {
						title: toolbarOptions["edit.incoming.patient.details"]["name"],
						imageSrc: 'icons/Edit.png',
						onclick : 'changeURL',
						href:'incomingsampleregistration/index.htm#/incomingVisitId/%visitId?',
				};
				
				cancelUrl = 'Laboratory/canceltest.do?_method=cancelPrescription';
			} else {
				patientToolbar = {};
					patientToolbar.collectsample = { title: toolbarOptions["collectsample"]["name"],	imageSrc: 'icons/Edit.png',
						href: 'Laboratory/'+collectSampleUrl+'.do?_method=getSampleCollectionScreen&title=Collect Sample&default_sample_status=C',
						show: ${urlRightsMap.lab_pending_samples == 'A' || urlRightsMap.lab_pending_samples_search == 'A'}
					}

			}
		} else {

			patientToolbar = {};
				patientToolbar.Edit = { title: toolbarOptions["vieweditresults"]["name"],
						imageSrc: 'icons/Edit.png', href: baseModule + '/editresults.do?_method=getBatchConductionScreen',
						onclick : 'changeURL',
						show: ${category == 'DEP_LAB' ? urlRightsMap.lab_edit_results == 'A' : urlRightsMap.rad_edit_results == 'A'}

				};
				patientToolbar.AssignOuthouse = {title: toolbarOptions["outhouse"]["name"], imageSrc: 'icons/Edit.png',
					href: 'Radiology/selectouthouse.do?_method=getOuthouseScreen',
					show: ${urlRightsMap.rad_select_outhouse == 'A'}
				};

				patientToolbar.EditReagents = {title: toolbarOptions["editreagents"]["name"], imageSrc: 'icons/Edit.png',
						href: 'Radiology/editresults.do?_method=modifyReagents'
				};
				patientToolbar.Manage= { title: toolbarOptions["managereports"]["name"], imageSrc: 'icons/Edit.png',
					href: 'Radiology/managereports.do?_method=getLabReport',
					show: ${urlRightsMap.rad_manage_reports == 'A'}
				};
				patientToolbar.Print = {title: toolbarOptions["print"]["name"], imageSrc: 'icons/Print.png', href: 'pages/DiagnosticModule/DiagReportPrint.do?_method=printReport', target: '_blank'};
				patientToolbar.PrintVisitReports ={title: toolbarOptions["printreports.visitwise.in.brackets"]["name"], imageSrc: 'icons/Print.png', href: 'pages/DiagnosticModule/DiagReportPrint.do?_method=printSelectedReports&using=visitId', target: '_blank'};
				patientToolbar.AddDoc= { title: toolbarOptions["viewadddocument"]["name"], imageSrc: 'icons/Edit.png',
						href: 'Radiology/TestDocumentsList.do?_method=searchTestDocuments',
						show: ${urlRightsMap.rad_test_documents == 'A'}
					};
			patientToolbar.ReconductTest ={
					title: toolbarOptions["reconduct"]["name"],
					imageSrc: 'icons/Report.png',
					onclick : 'changeURL',
					href:'DiagnosticLabModule/'+module+'ReconductTestList.do?_method=getReconductTestListScreen',
					target:'_blank'
				};


			cancelUrl = 'Radiology/canceltest.do?_method=cancelPrescription';
		}

		patientToolbar.Cancel = { title: toolbarOptions["cancel"]["name"], imageSrc: 'icons/Edit.png', href: cancelUrl,
		 	show: (${screenId != 'lab_pending_samples' && screenId != 'lab_pending_samples_search'})
		};

		function init() {
			createToolbar(patientToolbar);
			 autoCompleteTest();
			 autoCompleteOutHouse();
			 autoCompleteInHouse();
			 initMrNoAutoComplete('${cpath}');
			 if (category == 'DEP_LAB')
	 			document.getElementById('sample_no').focus();
			 if ( document.getElementById('resultTable') != null)
					initTooltip('resultTable', extraDetails);
		}
		function validateSearchForm() {
			document.testsForm._method.value = 'unfinishedTestsList';
			var errd=document.getElementById("_exp_rep_ready_date");
			var errt=document.getElementById("_exp_rep_ready_time");
			if(errd!=undefined && errt!=undefined && 
					errd.value!=undefined && errt.value!=undefined){
				if(empty(errd.value) && !empty(errt.value)){
					alert("Please Enter the value for date field in Expected Report Ready Time");
					return false;
				}
				if(!empty(errd.value) && !empty(errt.value)){
					var x=getDatePart(getDateFromField(errd));
					var curDate =getDatePart( new Date());
					if (x < curDate) {
						alert("Date can not be in the Past");
						return false;
					}
					return validateTime(errt);
				}
			}	
			return true;
		}
		var samplePrintType = '${genPrefs.sampleCollectionPrintType}';
		var form = document.testsForm;

		function doSave(){
			var screenId = document.getElementsByName('screenId');
			var checkBox = document.getElementsByName("completeCheck");
			var count = 0;
			for(var i=0; i<checkBox.length; i++) {
				if(checkBox[i].checked) count++;
			}
			if(count==0) {
				showMessage("js.laboratory.radiology.pendingtests.selecttest")
				return false;
			}else{
				var dourl = '${(urlRightsMap.lab_unfinished_tests_search == 'A' && category == 'DEP_LAB') ? 'unfinishedTestsSearch' : 'unfinishedTests'}';
				document.resultsForm.action = cpath +"/"+baseModule+"/"+dourl+".do?_method=saveResultsNotApplicableTests";
				document.resultsForm.submit();
			}
		}
	</script>
</head>
<body onload="init()">
<c:set var="selecthouse">
 <insta:ltext key="laboratory.pendingtests.list.inhouse"/>,
 <insta:ltext key="laboratory.pendingtests.list.outsource"/>
</c:set>
<c:set var="priority">
 <insta:ltext key="laboratory.signedoffreportslist.report.star"/>,
 <insta:ltext key="laboratory.signedoffreportslist.report.regular"/>
</c:set>
<c:set var="conducted">
 <insta:ltext key="laboratory.testauditlog.search.new.results"/>,
 <insta:ltext key="laboratory.testauditlog.search.new.noresults"/>,
 <insta:ltext key="laboratory.testauditlog.search.inprogress"/>,
 <insta:ltext key="laboratory.testauditlog.search.completed.results"/>,
 <insta:ltext key="laboratory.testauditlog.search.completed.noresults"/>,
 <insta:ltext key="ui.label.patient.arrived"/>,
 <insta:ltext key="laboratory.testauditlog.search.conductioncompleted"/>,
 <insta:ltext key="laboratory.testauditlog.search.transcriptionistscheduled"/>,
 <insta:ltext key="laboratory.testauditlog.search.changerequired"/>
</c:set>
<c:set var="conductedRad">
 <insta:ltext key="laboratory.testauditlog.search.new.results"/>,
 <insta:ltext key="laboratory.testauditlog.search.new.noresults"/>,
 <insta:ltext key="ui.label.patient.arrived"/>,
 <insta:ltext key="laboratory.testauditlog.search.conductioncompleted"/>,
 <insta:ltext key="laboratory.testauditlog.search.transcriptionistscheduled"/>,
 <insta:ltext key="laboratory.rad.testauditlog.search.inprogress"/>,
 <insta:ltext key="laboratory.rad.testauditlog.search.completed"/>,
 <insta:ltext key="laboratory.testauditlog.search.completed.noresults"/>,
 <insta:ltext key="laboratory.testauditlog.search.changerequired"/>
</c:set>
<c:set var="samplestatus">
 <insta:ltext key="laboratory.pendingtests.list.pending"/>,
 <insta:ltext key="laboratory.pendingtests.list.collected"/>,
  <insta:ltext key="laboratory.pendingtests.list.notrequired"/>
</c:set>
<c:set var="visittype">
 <insta:ltext key="laboratory.pendingtests.list.ip"/>,
 <insta:ltext key="laboratory.pendingtests.list.op"/>,
  <insta:ltext key="laboratory.pendingtests.list.incomingtest"/>
</c:set>
<c:set var="sponsorType">
 <insta:ltext key="laboratory.pendingtests.list.sponsor"/>,
 <insta:ltext key="laboratory.pendingtests.list.retail"/>
</c:set>
<c:set var="mrno">
 <insta:ltext key="ui.label.mrno"/>
</c:set>
<c:set var="visitid">
 <insta:ltext key="laboratory.pendingtests.list.visitid"/>
</c:set>
<c:set var="headerSponsorType">
	<insta:ltext key="laboratory.reportsearch.search.sponsor_type"/>
</c:set>
<c:set var="presdate">
 <insta:ltext key="laboratory.pendingtests.list.presdate"/>
</c:set>
<c:set var="select">
 <insta:ltext key="selectdb.dummy.value"/>
</c:set>
	<c:set var="testsList" value="${pagedList.dtoList}"/>
	<c:set var="hasResults" value="${not empty testsList}"/>
	<c:set var="hasSampleFlow" value="${diagGenericPref.map.sampleflow_required == 'Y'}" />
	<input type="hidden" name="printType" value="${genPrefs.sampleCollectionPrintType}"/>
	<c:set var="displayToken" value="false"/>


	<jsp:useBean id="recondutableStatusBean" class="java.util.HashSet" scope="request">
	    <%
	    recondutableStatusBean.add("P");
	    recondutableStatusBean.add("C");
	    recondutableStatusBean.add("V");
	    recondutableStatusBean.add("S");
	    recondutableStatusBean.add("MA");
	    recondutableStatusBean.add("CC");
	    recondutableStatusBean.add("TS");
	    recondutableStatusBean.add("CR");
	    %>
	</jsp:useBean>

	<c:choose>
		<c:when test="${screenId != 'lab_pending_samples' and screenId != 'lab_pending_samples_search'}">
			<h1 >${category == 'DEP_LAB' ? 'Laboratory ' : 'Radiology '} <insta:ltext key="laboratory.pendingtests.list.pendingtests"/>${screenId == 'lab_unfinished_tests_search'?' Search':''}</h1>
			<c:set var="actionURL" value="${cpath}/${category == 'DEP_LAB' ? 'Laboratory' : 'Radiology'}/${screenId == 'lab_unfinished_tests_search' and category == 'DEP_LAB' ? 'unfinishedTestsSearch.do' : 'unfinishedTests.do'}"/>
			<c:set var="searchMethod" value="unfinishedTestsList"/>
			<c:set var="method" value="unfinishedTestsList"/>
			<c:choose>
				<c:when test="${category == 'DEP_LAB'}">
					<c:set var="displayToken" value="${genPrefs.gen_token_for_lab == 'Y'}"/>
				</c:when>
				<c:otherwise>
					<c:set var="displayToken" value="${genPrefs.gen_token_for_rad == 'Y'}"/>
				</c:otherwise>
			</c:choose>
		</c:when>
		<c:otherwise>
			<h1 >${category == 'DEP_LAB' ? 'Laboratory ' : 'Radiology '} <insta:ltext key="laboratory.pendingtests.list.pendingsamples"/>${screenId == 'lab_pending_samples_search'?' Search':''}</h1>
			<c:set var="actionURL" value="${cpath}/${category == 'DEP_LAB' ? 'Laboratory' : 'Radiology'}/${screenId == 'lab_pending_samples_search' and category == 'DEP_LAB' ?'PendingSamplesSearch.do' : 'PendingSamples.do'}"/>
			<c:set var="searchMethod" value="pendingSamplesList"/>
			<c:set var="method" value="pendingSamplesList"/>
		</c:otherwise>
	</c:choose>
	<insta:feedback-panel/>
	<div style="display:none; margin-bottom:10px; padding:10px 0 10px 10px; height: 15px; background-color:#FFC;" class="brB brT brL brR" id="infoMsgDiv">
		<div class="fltL" style="width: 25px; margin:-1px 0 0 3px;" id="infoImg"> <img src="${cpath}/images/information.png" /></div>
		<div class="fltL"  style="margin:0px 0 0 5px ; width:865px;" id="infoDiv"></div>
	</div>
	
	<form name="testsForm" action="${actionURL}">
		<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(method)}"/>
		<input type="hidden" name="_searchMethod" value="${searchMethod }"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="screenId" value="${screenId}"/>

		<insta:search form="testsForm" optionsId="optionalFilter" closed="${hasResults}" validateFunction="validateSearchForm()">
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="laboratory.pendingtests.list.mrno.patientname"/></div>
					<div class="sboFieldInput">
						<div id="mrnoAutoComplete">
							<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
							<div id="mrnoContainer" style="width: 300px"></div>
						</div>
					</div>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="laboratory.pendingtests.list.labno"/>:</div>
					<div class="sboFieldInput">
						<input type="text" name="labno" size="10" value="${ifn:cleanHtmlAttribute(param.labno)}"/>
					</div>
				</div>
				<c:if test="${category == 'DEP_LAB'}">
					<div class="sboField">
						<div class="sboFieldLabel"><insta:ltext key="laboratory.pendingtests.list.sampleno"/>:</div>
						<div class="sboFieldInput">
							<input type="text" name="sample_no" id="sample_no" value="${ifn:cleanHtmlAttribute(param.sample_no)}"/>
						</div>
					</div>
				</c:if>
			</div>
			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.department"/></div>
							<div class="sfField">
								<insta:selectdb name="ddept_id" table="diagnostics_departments" valuecol="ddept_id" displaycol="ddept_name"
									value="${empty param.ddept_id ? userDept : param.ddept_id}"
									dummyvalue="${select}" filtered="true" filtercol="category,status"
									filtervalue="${category},A"/>

							</div>

							<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.originalsampleno"/>:</div>
							<div class="sfField">
								<input type="text" name="orig_sample_no" id="orig_sample_no"  value="${ifn:cleanHtmlAttribute(param.orig_sample_no)}"/>
							</div>

							<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.testname"/>:</div>
							<div class="sfField">
								<div id="test_wrapper">
									<input name="test_name" type="text" id="test_name" value="${ifn:cleanHtmlAttribute(param.test_name)}"/>
									<div id="test_container" style="width: 300px"></div>
								</div>
							</div>

						</td>
						<td>
							<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.conductiontype"/></div>
							<div class="sfField">
								<insta:checkgroup name="house_status" selValues="${paramValues.house_status}"
									opvalues="I,O" optexts="${selecthouse}"/>
							</div>
							<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.incominghospital"/></div>
							<div class="sfField" style="height: 20px">
								<div id="inhouse_wrapper">
									<input type="text" name="ih_name" id="ih_name" value="${ifn:cleanHtmlAttribute(param.ih_name)}" />
									<div id="inhouse_container"></div>
								</div>
							</div>

							<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.outsource"/>:</div>
							<div class="sfField" style="margin-bottom: 20px">
								<div id="outhouse_wrapper">
									<input type="text" name="oh_name" id="oh_name" value="${ifn:cleanHtmlAttribute(param.oh_name)}" />
									<div id="outhouse_container"></div>
								</div>
							</div>

							<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.testpriority"/></div>
							<div class="sfField">
								<insta:checkgroup name="priority" selValues="${paramValues.priority}"
								opvalues="S,R" optexts="${priority}"/>
							</div>
						</td>
						<td>
							<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.testprescribeddate"/></div>
							<div class="sfField">
								<div class="sfFieldSub"><insta:ltext key="laboratory.pendingtests.list.from"/></div>
									<insta:datewidget name="pres_date" id="pres_date0" valid="past"	value="${paramValues.pres_date[0]}" />
									<input type="hidden" name="pres_date@type" value="date"/>
									<input type="hidden" name="pres_date@op" value="ge,le"/>
									<input type="hidden" name="pres_date@cast" value="y"/>
								</div>
							<div class="sfField">
								<div class="sfFieldSub"><insta:ltext key="laboratory.pendingtests.list.to"/></div>
									<insta:datewidget name="pres_date" id="pres_date1" valid="past"	value="${paramValues.pres_date[1]}" />
							</div>

							<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.reportdate"/></div>
							<div class="sfField">
								<div class="sfFieldSub"><insta:ltext key="laboratory.pendingtests.list.from"/></div>
									<insta:datewidget name="report_date" id="report_date0" valid="past" value="${paramValues.report_date[0]}" />
									<input type="hidden" name="report_date@type" value="date"/>
									<input type="hidden" name="report_date@op" value="ge,le">
									<input type="hidden" name="report_date@cast" value="y"/>
								</div>
							<div class="sfField">
								<div class="sfFieldSub"><insta:ltext key="laboratory.pendingtests.list.to"/></div>
									<insta:datewidget name="report_date" id="report_date1" valid="past" value="${paramValues.report_date[1]}" />
							</div>
							<c:if test="${sampleCollectionCenterId == -1 && category == 'DEP_LAB'}">
								<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.collectioncenter"/></div>
								<div class="sfField">
									<c:choose>
										<c:when test="${max_centers_inc_default > 1 && centerId != 0}">
												<select name="collectionCenterId" id="collectionCenterId" class="dropdown">
														<option value="">${select}</option>
														<option value="-1" ${param.collectionCenterId == -1?'selected':''}>${defautlCollectionCenter}</option>
													<c:forEach items="${collectionCenters}" var="col_Centers">
														<option value="${col_Centers.map.collection_center_id}" ${col_Centers.map.collection_center_id == param.collectionCenterId?'selected':''}>
															${col_Centers.map.collection_center}
														</option>
													</c:forEach>
												</select>
										</c:when>
										<c:otherwise>
											<insta:selectdb id="collectionCenterId"  name="collectionCenterId"
										value="${param.collectionCenterId}" table="sample_collection_centers"
										valuecol="collection_center_id" displaycol="collection_center" dummyvalue="${select}"/>
										</c:otherwise>
									</c:choose>
								</div>
							</c:if>
							
						<div class="sfLabel"><insta:ltext key="laboratory.reportsearch.search.expRepReadyTime"/>:</div>
					<div class="sfField">
					<insta:datewidget name="_exp_rep_ready_date" id="_exp_rep_ready_date" valid="future" value="${param._exp_rep_ready_date}" />
					<input type="text" name="_exp_rep_ready_time" id="_exp_rep_ready_time" class="timefield" 
						value="${ifn:cleanHtmlAttribute(param._exp_rep_ready_time)}" maxlength="5"/>
					<input type="hidden" name="exp_rep_ready_time@type" value="timestamp"/>
					<input type="hidden" name="exp_rep_ready_time@op" value="le"/>
					</div>
						</td>

						<td>
							<c:if test="${screenId != 'lab_pending_samples' and screenId != 'lab_pending_samples_search'}">
								<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.conductionstatus"/></div>
								<c:choose>
            					<c:when test="${category == 'DEP_RAD'}">
								<div class="sfField">
									<insta:checkgroup name="conducted" selValues="${paramValues.conducted}"
									opvalues="N,NRN,MA,CC,TS,P,C,CRN,CR" optexts="${conductedRad}"/>
								</div>
								</c:when>
								<c:otherwise>
								<c:if test="${screenType != 'searchlist'}">
									<div class="sfField">
									<insta:checkgroup name="conducted" selValues="${paramValues.conducted}"
									opvalues="N,NRN,P,C,CRN" optexts="${conducted}"/>
								</div>
								</c:if>
								<c:if test="${screenType == 'searchlist'}">
								<div class="sfField">
									<insta:checkgroup name="conducted" selValues="${paramValues.conducted}"
									opvalues="N,NRN,P,C,CRN" optexts="${conducted}"/>
								</div>
								</c:if>
								</c:otherwise>
								</c:choose>
							</c:if>
							<c:if test="${category == 'DEP_LAB'}">
								<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.samplestatus"/></div>
								<div class="sfField">
									<insta:checkgroup name="sample_status" selValues="${paramValues.sample_status}"
									opvalues="0,1,U" optexts="${samplestatus}"/>
								</div>
							</c:if>
						</td>

						<td class="last">
							<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.patienttype"/></div>
							<div class="sfField">
								<insta:checkgroup name="visit_type" opvalues="i,o,t" optexts="${visittype}"
									selValues="${paramValues.visit_type}"/>
							</div>
							<div class="sfLabel"><insta:ltext key="laboratory.reportsearch.search.sponsortype"/></div>
							<div class="sfField">
								<insta:checkgroup name="patient_sponsor_type" selValues="${paramValues.patient_sponsor_type}"
								opvalues="S,R" optexts="${sponsorType}"/>
							</div>
							<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.incomingpatientname"/></div>
							<div class="sfField">
								<input type="text" name="inc_patient_name" value="${ifn:cleanHtmlAttribute(param.inc_patient_name)}"/>
								<input type="hidden" name="inc_patient_name@op" value="ilike"/>
							</div>

							<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.incomingpatient.otherinfo"/></div>
							<div class="sfField">
								<input type="text" name="patient_other_info" value="${ifn:cleanHtmlAttribute(param.patient_other_info)}"/>
							</div>
						</td>
					</tr>
				</table>
			</div>
		</insta:search>
	</form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}" showTooltipButton="true"/>
	<form name="resultsForm"  method="POST">
		<input type="hidden" name="_method" value="getBatchConductionScreen"/>
		<input type="hidden" name="visitid" value=""/>
		<input type="hidden" name="visitType" value=""/>
		<input type="hidden" name="category" value="${ifn:cleanHtmlAttribute(category)}"/>
		<input type="hidden" name="reportId" value=""/>
		<input type="hidden" name="prescId" id="prescribed_id" value=""/>
		<div class="resultList">
			<table class="resultList" id="resultTable" cellspacing="0" cellpadding="0" style="empty-cells: show">
				<tr>
					<th><input type="checkbox" onclick="return checkOrUncheckAll('completeCheck',this)" id="completeAll"/></th>
					<c:if test="${!displayToken}">
						<th>#</th>
					</c:if>
					<c:if test="${displayToken}">
						<insta:sortablecolumn name="token_number" title="Token No."/>
					</c:if>
					<insta:sortablecolumn name="mr_no" title="${mrno}"/>
					<insta:sortablecolumn name="patient_id" title="${visitid} (${headerSponsorType})"/>
					<th><insta:ltext key="ui.label.patient.name"/></th>
					<th><insta:ltext key="laboratory.pendingtests.list.ordersymbol"/></th>
					<th><insta:ltext key="laboratory.pendingtests.list.testname"/></th>
					<c:if test="${screenId != 'lab_pending_samples' and screenId != 'lab_pending_samples_search'}">
						<th><insta:ltext key="laboratory.reportsearch.search.conductingdoctor"/></th>
						<th><insta:ltext key="laboratory.pendingtests.list.prescribingdoctor"/></th>
					</c:if>
					<insta:sortablecolumn name="pres_date" title="${presdate}"/>
					<c:choose>
						<c:when test="${screenId == 'lab_pending_samples' or screenId == 'lab_pending_samples_search'}">
							<th><insta:ltext key="laboratory.pendingtests.list.sampletype"/></th>
							<th><insta:ltext key="laboratory.pendingtests.list.sampleno"/></th>
							<th><insta:ltext key="laboratory.pendingtests.list.container"/></th>
						</c:when>
						<c:otherwise>
							<c:if test="${islabNoReq eq 'Y'}">
								<th><insta:ltext key="laboratory.pendingtests.list.labno"/></th>
							</c:if>
							<th><insta:ltext key="laboratory.pendingtests.list.remarks"/></th>
							<th><insta:ltext key="laboratory.pendingtests.list.report"/></th>
							<th><insta:ltext key="laboratory.pendingtests.list.docs"/></th>
						</c:otherwise>
					</c:choose>
				</tr>
				<c:forEach items="${testsList}" var="test" varStatus="st">
					<c:set var="rowIndex" value="${rowIndex + st.index}"/>
					<c:set var="printItem" value="${(test.report_id != 0) and (test.hasData ne 'N')}"/>
					<c:set var="cancelTest" value="${test.conducted == 'N' || test.conducted == 'NRN' }"/>
					<c:set var="testingCenterTest" value="${(category eq 'DEP_LAB' && max_centers_inc_default > 1) ? (test.hospital eq 'incoming' && test.incoming_source_type eq 'C') : false}" />
					<c:set var="isLIS" value="${category eq 'DEP_LAB' ? (test.hospital eq 'incoming' && test.incoming_source_type eq 'IH') : false}"/>
					<c:set var="sample_status">
					<c:choose>
						<c:when test="${test.sample_needed == 'n'}">U</c:when>
						<c:otherwise>${test.sflag}</c:otherwise>
					</c:choose>
					</c:set>
					<c:set var="sampleRequired"
							value="${(test.house_status eq 'O' || hasSampleFlow) && sample_status eq '0'}"/>
					<c:set var="selectOutHouse"
							value="${sample_status eq 'U' && test.house_status eq 'O' && (empty test.outhouse_hospital_name)}"/>
					<c:set var="isInternalLab" value="${category eq 'DEP_LAB' and test.outsource_dest_type eq 'C'}"/>
					<c:set var="collectionCenterTest"
							value="${max_centers_inc_default > 1 && category eq 'DEP_LAB' && test.house_status eq 'O' && test.outsource_dest_type eq 'C'}"/>
					<c:set var="sampleAssertion" value="${diagGenericPref.map.sample_assertion == 'Y'}" />
					<c:set var="blockAsserted" value="${(sampleAssertion == true && test.sample_collection_status ne 'A') && test.sample_status ne 'U' }"/>
					<c:set var="sampleTransferred" value="${(test.sample_transfer_status == 'T' && test.outsource_dest_type eq 'C')}"/>
					<c:set var="sampleStatus" value="${test.sample_status eq '0' ? 'false' : 'true'}"/>					
					<c:set var="incomingPatient" value="${category == 'DEP_LAB' && test.hospital eq 'incoming'}"/>
					<c:set var="isChildCenter" value="${category == 'DEP_LAB' ? (incomingPatient && test.incoming_source_type eq 'C') : false}"/>									
					<c:set var="allowPrintReportVisitWise" value="false"/>
					<c:if test="${(actionRightsMap.allow_print_report_visit_wise == 'A')||(roleId==1)||(roleId==2)}">
						<c:set var="allowPrintReportVisitWise" value="true"/>
					</c:if>
					
					<c:set var="flagColor">
						<c:choose>
							<c:when test="${!test.billPaid}">grey</c:when>
							<c:when test="${test.conducted == 'C' || test.conducted == 'CRN'}">green</c:when>
							<c:when test="${test.collectSample}">blue</c:when>
							<c:when test="${test.assignOuthouse}">yellow</c:when>
							<c:otherwise>empty</c:otherwise>
						</c:choose>
					</c:set>
					<c:choose>
						<c:when test="${test.billPaid}">
							<c:set var="blockUnpaid" value="false"/>
						</c:when>
						<c:otherwise>
							<c:set var="blockUnpaid" value="${directBillingPrefs[category == 'DEP_LAB' ? 'Laboratory' : 'Radiology'].map.block_unpaid == 'Y'}"/>
						</c:otherwise>
					</c:choose>
					<c:choose>
						<c:when test="${test.conducted == 'N' || test.conducted == 'NRN'}">
							<c:set var="cancelTest" value="${test.conducted == 'N' || test.conducted == 'NRN'}"/>
								<c:if test="${sampleRequired && !test.collectSample}">
									<c:set var="cancelTest" value="${(roleId == 1 || roleId == 2
					 							|| (actionRightsMap.allow_cancel_test == 'A') || (actionRightsMap.cancel_test_any_time == 'A')) }"/>
								</c:if>
						</c:when>
						<c:otherwise>
								<c:set var="cancelTest" value="${test.conducted == 'N' || test.conducted == 'NRN' || roleId == 1 || roleId == 2 || (actionRightsMap.cancel_test_any_time == 'A')}"/>
						</c:otherwise>

					</c:choose>
					<c:set var="canEditPatientDetails" value="${empty test.mr_no && (test.conducted == 'N' || test.conducted == 'P' ) && !(max_centers_inc_default > 1 && centerId == 0)}" />
					<c:choose>
						<c:when test="${screenId == 'lab_pending_samples' or screenId == 'lab_pending_samples_search'}">
							<tr onclick="showToolbar(${rowIndex}, event, 'resultTable',
								{reportId: '${test.report_id}', category: '${ifn:cleanJavaScript(category)}',visitid: '${test.pat_id}',
								 visit_type: '${test.visit_type}',
								 presMrno: '${test.mr_no}', patVisitId: '${test.pat_id}', prescribed_id: '${test.prescribed_id}'},
								[<c:if test="${category == 'DEP_LAB'}">${test.collectSample and !blockUnpaid and !incomingPatient}</c:if>]);"
							onmouseover="hideToolBar(${rowIndex})" id="toolbarRow${rowIndex}">
						</c:when>
						<c:otherwise>
							<tr onclick="showToolbar(${rowIndex}, event, 'resultTable',
								{reportId: '${test.report_id}', category: '${ifn:cleanJavaScript(category)}',visitid: '${test.pat_id}',
								 visit_type: '${test.visit_type}',patientid: '${test.pat_id}', labno: '${test.labno}', common_order_id: '${test.common_order_id}', sample_no: '${test.sample_no}', prescId:'${test.prescribed_id}',
								 presMrno: '${test.mr_no}', patVisitId: '${test.pat_id}', prescribed_id: '${test.prescribed_id}',
								 testName: '${test.test_name}',testId: '${test.test_id}',visitId: '${test.pat_id}',patient_id: '${test.pat_id}',
								 prescribedId: '${test.prescribed_id}',conducted: '${test.conducted}',bulkWorkSheetPrint: 'N', '%visitId': '${test.pat_id}'},
								[${test.canEdit && !test.assignOuthouse && !blockUnpaid && !blockAsserted && test.resultEntryApplicable 
								&& (collectionCenterTest ? test.outsource_dest eq centerId : true) 
								&& (testingCenterTest ? centerId ne 0 : true) && (!sampleTransferred) && !(isInternalLab ? test.outsource_dest ne centerId : false)}, 
								${!blockUnpaid && test.assignOuthouse} && ${!isChildCenter},
								${test.reagentExists && test.storeExist && test.conducted!='C' && test.conducted!='CRN' && !test.collectSample && !collectionCenterTest},
								${!blockUnpaid && test.resultEntryApplicable},${printItem}, ${test.resultEntryApplicable && allowPrintReportVisitWise}
								<c:if test="${category == 'DEP_LAB'}">,${sampleStatus},${test.collectSample and !blockUnpaid and !incomingPatient}</c:if>
								,true,${ifn:contains(recondutableStatusBean,test.conducted)},${canEditPatientDetails},${cancelTest && !testingCenterTest && !isLIS}]);"
							onmouseover="hideToolBar(${rowIndex})" id="toolbarRow${rowIndex}">
						</c:otherwise>
					</c:choose>
						<td>
							<input type="checkbox" name="completeCheck" id="completeCheck" value="${test.prescribed_id}"
								${(test.resultEntryApplicable || test.conducted=='CRN' || test.collectSample  || blockAsserted || blockUnpaid  || test.assignOuthouse || collectionCenterTest) ? 'disabled':''}/>
						</td>
						<input type="hidden" name="_conducting_doc_mandatory" value="${ifn:cleanHtmlAttribute(test.conducting_doc_mandatory)}" />
						<input type="hidden" name="_results_entry_applicable" value="${test.results_entry_applicable}" />
						<input type="hidden" name="_test_name" value="${test.test_name}"/>
						<c:if test="${!displayToken}">
							<td>${((pagedList.pageNumber-1) * pagedList.pageSize) + st.index + 1}</td>
						</c:if>
						<c:if test="${displayToken}">
							<td>${test.token_number}</td>
						</c:if>
						<td>${test.mr_no}</td>
						<td>${test.pat_id} (${test.patient_sponsor_type})</td>
						<td><insta:truncLabel value="${test.patient_full_name}" length="30"/></td>
						<td>${test.common_order_id}</td>
						<td>
							<img src="${cpath}/images/${flagColor}_flag.gif"/>
							<c:choose>
							<c:when test="${test.priority=='S'}">
								<b><font color="#444444">
								<insta:truncLabel value="${test.test_name}" length="35"/>
								</font></b>
							</c:when>
							<c:otherwise>
								<insta:truncLabel value="${test.test_name}" length="35"/>
							</c:otherwise>
							</c:choose>
						</td>
						<c:if test="${screenId != 'lab_pending_samples' and screenId != 'lab_pending_samples_search'}">
							<td>${test.conducting_doctor_name}</td>
							<td>${test.pres_doctor_name }</td>
						</c:if>
						<td><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${test.pres_date}"/></td>

						<c:choose>
							<c:when test="${screenId == 'lab_pending_samples' or screenId == 'lab_pending_samples_search'}">
								<td>${test.sample_type}</td>
								<td>
								<a onclick="return appendTemplateName(this,'${test.sample_sno}','${test.collection_center_visit_id}','${test.mr_no}','${test.sample_date }','${test.sample_type }')"
									target="blank"  href="#" title='<insta:ltext key="laboratory.pendingtests.list.reprintsampleno"/>'>
									${test.sample_sno}</a>
								</td>
								<td>${test.sample_container}</td>
							</c:when>
							<c:otherwise>
								<c:if test="${islabNoReq eq 'Y'}">
									<td>${ifn:cleanHtml(test.labno)}</td>
								</c:if>
								<td style="white-space: normal;">
									<insta:truncLabel value="${test.remarks}" length="12"></insta:truncLabel>
								</td>
								<td>${test.report_name}</td>
								<td><c:if test="${test.doc_count > 0}"><img src="${cpath}/icons/filewithtick.png" width="16px" height="16px"/></c:if></td>
							</c:otherwise>
						</c:choose>
						<script>
									extraDetails['toolbarRow${rowIndex}'] = {'Expected Report Ready Time': '<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${test.exp_rep_ready_time}"/> '};
								</script>
					</tr>
				</c:forEach>
			</table>
		</div>
		<div id="results" style="white-space: normal">
		</div>
		<div align="right" style="display: ${genPrefs.sampleCollectionPrintType == 'SL'?'none':'block'}">
			<insta:selectdb name="sampleBardCodeTemplate" table="sample_bar_code_print_templates" valuecol="template_name"
					displaycol="template_name" filtered="false" />
		</div>
		<c:if test="${screenId != 'lab_pending_samples' and screenId != 'lab_pending_samples_search'}">
			<div style="padding-top: 7px">
				<label><insta:ltext key="laboratory.reportsearch.search.conductingdoctor"/></label>
				<select name="commonConductingDoctor" id="commonConductingDoctor" style="width:12em"
					 class="dropdown" title="Set conducting doctor for all test at once"  }>
					<option value="">${select}</option>
					<c:forEach var="doctor" items="${requestScope.doctors}">
							<option value="${doctor.DOCTOR_ID}">${doctor.DOCTOR_NAME}</option>
						</c:forEach>
				</select>
			</div>
		</c:if>
		<div class="screenActions">
			<c:if test="${screenId != 'lab_pending_samples' and screenId != 'lab_pending_samples_search'}">
				<button type="button" accesskey="C" onclick="setConductingDoc()">
				<label><insta:ltext key="laboratory.reportsearch.search.set"/>&nbsp;<b><u><insta:ltext key="laboratory.reportsearch.search.c"/></u></b><insta:ltext key="laboratory.reportsearch.search.onductingdoctor"/></label></button>&nbsp;|
			</c:if>
			<button type="button" id="saveButton" accessKey="S" onclick="return doSave();"><b><u><insta:ltext key="laboratory.pendingtests.list.m"/></u></b><insta:ltext key="laboratory.pendingtests.list.arkcompleted"/></button>
		</div>
	</form>
	<div class="legend" style="margin-top: 10px;display: ${hasResults? 'block' : 'none'}">
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'/></div>
		<div class="flagText"><insta:ltext key="laboratory.pendingtests.list.unpaidbills"/></div>
		<div class="flag"><img src='${cpath}/images/green_flag.gif'/></div>
		<div class="flagText"><insta:ltext key="laboratory.pendingtests.list.completed"/></div>
		<div class="flag"><img src='${cpath}/images/blue_flag.gif'/></div>
		<div class="flagText"><insta:ltext key="laboratory.pendingtests.list.collectsample"/></div>
		<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
		<div class="flagText"><insta:ltext key="laboratory.pendingtests.list.selectouthouse"/></div>
	</div>
	<div style="clear:both"/>
	<div class="legend" style="margin-top: 10px; display: ${hasResults? 'block' : 'none'}">
		<div class="flagText"><insta:ltext key="laboratory.pendingtests.list.legend.sponsor"/></div>
		<div class="flagText" style="padding-left: 10px"><insta:ltext key="laboratory.pendingtests.list.legend.retail"/></div>
	</div>

</body>
</html>
