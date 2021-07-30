<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>

<head>
<title><insta:ltext key="laboratory.signedoffreportslist.search.title"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta name="i18nSupport" content="true"/>
<insta:link type="css" file="widgets.css" />
<insta:link type="script" file="ajax.js" />
<insta:link type="script" file="widgets.js" />
<insta:link type="script" file="dashboardColors.js" />
<insta:link type="script" file="referaldoctorautocomplete.js" />
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="js" file="diagnostics/changeURLS.js"/>
<insta:link type="js" file="hmsvalidation.js"/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<jsp:include page="/pages/Common/MrnoPrefix.jsp" />
<script src="<%=request.getContextPath()%>/gettests.do?${test_timestamp}&${ifn:cleanURL(sesHospitalId)}&module=${ifn:cleanURL(module)}"></script>
<style type="text/css">
   .reportStatus{
   	 border:1px solid red;
   	 border-radius:6px;
   	 padding:2px 4px 2px 4px;
   	 font-size:9px;
   }
</style>
<c:choose>
<c:when test="${module == 'DEP_LAB'}">
	<c:set var="url" value="Laboratory/SignedOffReportList.do" />
</c:when>
<c:otherwise>
	<c:set var="url" value="Laboratory/RadSignedOffReportList.do" />
</c:otherwise>
</c:choose>
<c:set var="revertSignOffRts" value="${(roleId le 2) || actionRightsMap['revert_signoff'] eq 'A'}"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<insta:js-bundle prefix="laboratory.radiology.reportlist"/>
<script>
	var toolbarOptions = getToolbarBundle("js.laboratory.radiology.reportlist.toolbar");
	var allTestNames = deptWiseTestsjson;
	var outHouses = ${outHouses};
	var inHouses = ${inHouses};
	var HistoCytoNames = <%= request.getAttribute("HistoCytoNames") %>;
	var category = '${ifn:cleanJavaScript(module)}';
	var centerId = ${centerId};
	var max_centers_inc_default = ${max_centers_inc_default};
	var sampleCollectionCenterId = ${sampleCollectionCenterId};
	var printerType = null;
	var prescribedDocJSON = ${prescribedDoctors};
	var optimizedLabReportPrint = '${diagGenericPref.map.optimized_lab_report_print}';

function doSearch() {
	var theForm = document.signedOffForm;
	var signOffFromDate = theForm.rfdate.value;
	var signOffToDate = theForm.rtdate.value;
	
	if(theForm.mrno!=undefined)
	var mr_no = theForm.mrno.value;
	
	if(theForm.sampleNo!=undefined)
	var samp_no = theForm.sampleNo.value;
	
	if(theForm.labno!=undefined)
	var lab_no = theForm.labno.value;
	
	if(theForm.phoneNo!=undefined)
	var ph_no = theForm.phoneNo.value;
	
	var signoffDtReqd = (mr_no == undefined || (mr_no!=undefined && mr_no == "")) 
						&& 	(samp_no == undefined || (samp_no != undefined && samp_no == "")) 
						&&	(lab_no == undefined  || (lab_no!=undefined && lab_no == "")) 
						&&	(ph_no == undefined   || (ph_no != undefined && ph_no == "")) 
						&&	(signOffFromDate!=undefined && signOffFromDate == "");
	
	if (signoffDtReqd) {
		showMessage("js.laboratory.radiology.reportlist.signedoff.fromdaterequired");
		theForm.rfdate.focus();
		return false;
	}
	// if to date is empty then take today's date
	if (signOffToDate == "") {
		signOffToDate = formatDate(new Date(), 'ddmmyyyy', '-');
	}		
		
	if (!doValidateDateField(theForm.rfdate))
		return false;
	if (!doValidateDateField(theForm.rtdate))
		return false;

	if (parseDateStr(signOffFromDate) > parseDateStr(signOffToDate)) {
		showMessage("js.laboratory.radiology.reportlist.signedoff.tofromdate");
		theForm.rfdate.focus();
		return false;
	}
	// Difference of Signed-off to and from date in days should not be greater than 31 
	if (daysDiff(parseDateStr(signOffFromDate), parseDateStr(signOffToDate)) > 31) {
		showMessage("js.laboratory.radiology.reportlist.signedoff.daterange");
		theForm.rfdate.focus();
		return false;
	}
	var referName = document.getElementById("_referaldoctorName");
	var referId = document.getElementById("reference_docto_id")
	if(referName!=undefined && referId!=undefined){
		if(empty(referName.value) && !empty(referId.value))
			referId.value = "";
		if(empty(referId.value) && !empty(referName))
			referName.value = "";
	}
	
	return true;
}

function onChangeMrno() {
	var theForm = document.signedOffForm;
	var mrnoBox = theForm.mrno;

	var valid = addPrefix(mrnoBox, gMrNoPrefix, gMrNoDigits);

	if (!valid) {
		showMessage("js.laboratory.radiology.reportlist.invalidformat");
		theForm.mrno.value = ""
		theForm.mrno.focus();
		return false;
	}
}

function enableTestType(){
	var theForm = document.signedOffForm;
	var disabled =  theForm.testAll.checked;

	theForm.testIncoming.disabled = disabled;
	theForm.testOutgoing.disabled = disabled;
}

function enablePatientType() {

	var theForm = document.signedOffForm;
	var disabled = theForm.patientAll.checked;

	theForm.patientIp.disabled = disabled;
	theForm.patientOp.disabled = disabled;
	theForm.patientIn.disabled = disabled;
}

function showOnlyInhosueVisits(){
	var theForm = document.signedOffForm;
    var disabled = theForm.showOnlyInhouseTests.checked;

	theForm.patientIp.disabled = disabled;
	theForm.patientOp.disabled = disabled;
	theForm.patientAll.disabled = disabled;
}

var reportToolbar = {}
		reportToolbar.Print= {
			title: toolbarOptions["print"]["name"],
			imageSrc: 'icons/Print.png',
			href: 'pages/DiagnosticModule/DiagReportPrint.do?_method=printReport',
			target: '_blank'
		};
		reportToolbar.AmendTestResults = {
			title: toolbarOptions["amendreport"]["name"],
			imageSrc: 'icons/Report.png',
			href: '${module == 'DEP_LAB' ? 'Laboratory' : 'Radiology'}/editresults.do?_method=getBatchConductionScreen',
		};
		reportToolbar.Addendum= {
			title: toolbarOptions["addaddendum"]["name"],
			imageSrc: 'icons/Add.png',
			href: '${module == 'DEP_LAB' ? 'Laboratory' : 'Radiology'}/managereports.do?_method=getAddAddendumsScreen',
			show: ${(module == 'DEP_LAB' ? urlRightsMap.lab_manage_reports : urlRightsMap.rad_manage_reports) == 'A'}
		};
		reportToolbar.SignOff= {
			title: toolbarOptions["signoffaddendum"]["name"],
			imageSrc: 'icons/Signoff.png',
			href: '${ifn:cleanJavaScript(url)}?_method=signOffAddendum',
			description: null,
			show: ${(module == 'DEP_LAB' ? urlRightsMap.lab_manage_reports : urlRightsMap.rad_manage_reports) == 'A'}
		};
		reportToolbar.HandOver= {
			title: toolbarOptions["handover"]["name"],
			imageSrc: 'icons/HandOver.png',
			href: '${ifn:cleanJavaScript(url)}?_method=getHandOver'
		};
		reportToolbar.SendEmail= {
    			title: toolbarOptions["sendemail"]["name"],
    			imageSrc: 'icons/Send.png',
    			onclick: 'showEmailDialog',
    			show : ${(actionRightsMap.allow_send_diagnostics_reports == 'A')||(roleId==1)||(roleId==2)}
    };
		reportToolbar.ViewReport= {
			title: toolbarOptions["viewreport"]["name"],
			imageSrc: 'icons/Report.png',
			href:'${ifn:cleanJavaScript(url)}?_method=getSignedOffReportContent',
			target: '_blank'
		};
		if(optimizedLabReportPrint == 'Y') {
			reportToolbar.PrintVisistReports= {
					title: toolbarOptions["printreports.visitwise.in.brackets"]["name"],
					imageSrc: 'icons/Print.png',
					href: 'pages/DiagnosticModule/DiagReportPrint.do?_method=printOptimizedDiagReport&using=visitId&category=${ifn:cleanURL(module)}',
					target: '_blank'
				};	
		} else {
			reportToolbar.PrintVisistReports= {
					title: toolbarOptions["printreports.visitwise.in.brackets"]["name"],
					imageSrc: 'icons/Print.png',
					href: 'pages/DiagnosticModule/DiagReportPrint.do?_method=printSelectedReports&using=visitId&category=${ifn:cleanURL(module)}',
					target: '_blank'
				};
		}

var baseModule = category == 'DEP_LAB' ? 'Laboratory' : 'Radiology';
var module = category == 'DEP_LAB'? 'Lab' : 'Radiology';

	var testToolBar ={}
		testToolBar.ReconductTest ={
			title: toolbarOptions["reconduct"]["name"],
			imageSrc: 'icons/Redo.png',
			onclick : 'changeReconductURL',
			href:'DiagnosticLabModule/'+module+'ReconductTestList.do?_method=getReconductTestListScreen',
			target:'_blank'
	};

var eDialog = null;

function initEmailDialog() {
	var dialog = document.getElementById('sendEmailDialog');
	dialog.style.display = 'block';
	eDialog = new YAHOO.widget.Dialog("sendEmailDialog", {
		width: "300px",
		visible: false,
		modal: true,
		constraintoviewport: true
	});
  YAHOO.util.Event.addListener("sendEmailBtn", "click", ajaxSend, true);
	var escKey = new YAHOO.util.KeyListener(document, { keys:27 },
  	                                              { fn:cancelEmDialog,
  	                                                scope:eDialog,
  	                                                correctScope:true } );

  eDialog.cfg.setProperty("keylisteners", escKey);
  eDialog.cancelEvent.subscribe(cancelEmDialog);
	eDialog.render();
}

function cancelEmDialog(){
   document.getElementById('email_id').value ='';
   eDialog.hide();
}


var selectedParam = null;
function showEmailDialog(anchor, params, id, toolbar) {
	if (eDialog != null) {
	  document.getElementById('email_id').value = params.emailId;
	  selectedParam = params;
		eDialog.show();
	}
	return false;
}

function ajaxSend() {
  var emailId =  document.getElementById('email_id').value;
  if(!isEmail(emailId,"Enter the valid Mail Id")){
      	return false;
  }
	var ajaxobj = newXMLHttpRequest();
	var url = "${cpath}/Laboratory/SignedOffReportList.do?_method=updateAndSendEmail&newEmail="+emailId+"&patientMrno="+selectedParam.mrNo+"&reportId="+selectedParam.reportId;
	ajaxobj.open("POST", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj.readyState == 4) {
		if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
			eval("var responseData =" + ajaxobj.responseText);
       eDialog.hide();
       if(responseData.result && responseData.isUpdated){
          window.location.reload();
          alert("Message Sent Successfully");
       } else {
          alert("Message Sent Failed");
          return false;
       }
		}
	}
	return true;
}

function init(){
	createToolbar(reportToolbar, 'report');
	createToolbar(testToolBar, 'test');
 	autoCompleteTest();
 	autoCompleteHistoCytoShortImpression();
	autoCompleteOutHouse();
	autoCompleteInHouse();
	initEmailDialog();
	var theForm = document.signedOffForm;
	enablePatientType();

	var ip = theForm.patientIp.checked ;
	var op = theForm.patientOp.checked ;
	var inPat = theForm.patientIn.checked ;
	if (${param._method eq 'getReportList'})
		printerType = theForm._printerType.value;

	if( !ip && !op && !inPat){
		theForm.patientAll.checked = true;
		enablePatientType();
	}

	if (category == 'DEP_LAB'){
		document.getElementById('sampleNo').focus();
		refDocAutoComplete(cpath, '_referaldoctorName', 'reference_docto_id', 'referalNameContainer', '/Laboratory/SignedOffReportList.do');
	}

	loadPrescribedDoctors();
	prescribedDoAutoComplete('pres_doctor', '_doctor_name', 'prescribedDocContainer');

}

function setPrinterId(obj) {
	printerType = obj.value;
}

 function onKeyPressMrno(e) {
	if (isEventEnterOrTab(e)) {
		return onChangeMrno();
	} else {
		return true;
	}
}

function clearSearch() {
	var theForm = document.signedOffForm;
	theForm.fdate.value = "";
	theForm.tdate.value = "";
	theForm.rfdate.value = "";
	theForm.rtdate.value = "";
	theForm.department.value = "";
	theForm.diagname.value = "";
	theForm.patientAll.checked = true;
	theForm.mrno.value = "";
	theForm.patientName.value = "";
	theForm.labno.value = "";
	theForm.inhouse.value = "";
	theForm.outhouse.value = "";
	theForm.showOnlyInhouseTests.checked = false;
	theForm.showOnlyouthouseTests.checked = false;
	enablePatientType();

}




function autoCompleteTest() {
	dataSource = new YAHOO.util.LocalDataSource(allTestNames)
	dataSource.responseSchema = {fields : ["TEST"]};
	oAutoComp1 = new YAHOO.widget.AutoComplete('diagname', 'test_container', dataSource);
	oAutoComp1.maxResultsDisplayed = 15;
	oAutoComp1.allowBrowserAutocomplete = false;
	oAutoComp1.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp1.typeAhead = false;
	oAutoComp1.useShadow = false;
	oAutoComp1.minQueryLength = 0;
	oAutoComp1.animVert = false;
}

function autoCompleteHistoCytoShortImpression() {
	dataSource = new YAHOO.util.LocalDataSource(HistoCytoNames)
	dataSource.responseSchema = {fields : ["SHORT_IMPRESSION"]};
	oAutoComp1 = new YAHOO.widget.AutoComplete('shortImpression', 'shot_impression_container', dataSource);
	oAutoComp1.maxResultsDisplayed = 15;
	oAutoComp1.allowBrowserAutocomplete = false;
	oAutoComp1.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp1.typeAhead = false;
	oAutoComp1.useShadow = false;
	oAutoComp1.minQueryLength = 0;
	oAutoComp1.animVert = false;
}

function autoCompleteInHouse() {
	dataSource = new YAHOO.util.LocalDataSource(inHouses,{ queryMatchContains : true })
	dataSource.responseSchema = {fields : ["hospital_name"]};
	oAutoComp1 = new YAHOO.widget.AutoComplete('inhouse', 'inhouse_container', dataSource);
	oAutoComp1.maxResultsDisplayed = 15;
	oAutoComp1.allowBrowserAutocomplete = false;
	oAutoComp1.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp1.typeAhead = false;
	oAutoComp1.useShadow = false;
	oAutoComp1.minQueryLength = 0;
	oAutoComp1.animVert = false;
}

function autoCompleteOutHouse() {
	dataSource = new YAHOO.util.LocalDataSource(outHouses,{ queryMatchContains : true })
	dataSource.responseSchema = {fields : ["OUTSOURCE_NAME"]};
	oAutoComp1 = new YAHOO.widget.AutoComplete('outhouse', 'outhouse_container', dataSource);
	oAutoComp1.maxResultsDisplayed = 15;
	oAutoComp1.allowBrowserAutocomplete = false;
	oAutoComp1.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp1.typeAhead = false;
	oAutoComp1.useShadow = false;
	oAutoComp1.minQueryLength = 0;
	oAutoComp1.animVert = false;
}


	function getPrint(){
		var reportId = document.signedOffForm.reportId.value;
		if(reportId !=""){
			window.open('./DiagReportPrint.do?_method=printReport&printerId=0&reportId='+reportId);
			return true;
		}
	}

	function create() {
		 initMrNoAutoComplete('${cpath}');
	}

	function revertSignOff(){
	    var revertedReports = document.getElementsByName("revert_signoff");
	    var revertNeeded = false;
	    var anyHandedOver = false;
	    var row ;
	    for(var i =0;i<revertedReports.length;i++){
	    	if( revertedReports[i].checked ){
	    		row = getThisRow(revertedReports[i]);
	    		if(getElementByName(row, 'handed_over').value == 'Y') {
	    			anyHandedOver = true;
	    			break;
	    		}
	    		revertNeeded = true;
	    		break;
	    	}
	    }

		if ( anyHandedOver ){
			showMessage("js.laboratory.radiology.reportlist.reports.handover");
			return false;
		}
	    if(!revertNeeded){
	    	showMessage("js.laboratory.radiology.reportlist.reports.revertsignoff");
	    	return false;
	    }

		document.signedOffForm._method.value = "revertSignOffReports";
		document.signedOffForm.submit();
	}


</script>

<c:set var="amendTestResultsRt" value="${(roleId le 2) || actionRightsMap['amend_test_results'] eq 'A'}"/>
</head>

<body onload="init();getPrint();create()" class="yui-skin-sam">
<c:set var="select">
 <insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="mrno">
 <insta:ltext key="ui.label.mrno"/>
</c:set>
<c:set var="signedoffdate">
 <insta:ltext key="laboratory.signedoffreportslist.search.signedoffdate"/>
</c:set>
<c:set var="priority">
 <insta:ltext key="laboratory.signedoffreportslist.report.star"/>,
 <insta:ltext key="laboratory.signedoffreportslist.report.regular"/>
</c:set>
<c:set var="sponsorType">
 <insta:ltext key="laboratory.signedoffreportslist.report.sponsor"/>,
 <insta:ltext key="laboratory.signedoffreportslist.report.retail"/>
</c:set>
<c:set var="severitystatus">
	<insta:ltext key="laboratory.testauditlog.search.allnormal"/>,
	<insta:ltext key="laboratory.testauditlog.search.hasabnormalresults"/>,
	<insta:ltext key="laboratory.testauditlog.search.hascriticalresults"/>,
	<insta:ltext key="laboratory.testauditlog.search.nonvaluebasedtests"/>
</c:set>
<div class="pageHeader"><insta:ltext key="laboratory.signedoffreportslist.search.signedoffreportslist"/></div>
<c:out value="${param.msg}" />
<html:form action="/${url}" method="GET">
	<input type="hidden" name="_method" value="getReportList"/>
	<input type="hidden" name="_searchMethod" value="getReportList"/>
	<input type="hidden" name="from" value="${from }">
	<input type="hidden" name="reportId" value="${ifn:cleanHtmlAttribute(reportId)}">
	<input type="hidden" name="category" value="${ifn:cleanHtmlAttribute(module)}">
	<c:set var="filterClosed" value="${not empty pagedList.dtoList}" />

	<insta:search form="signedOffForm" optionsId="optionalFilter" closed="${filterClosed}" validateFunction="doSearch()">
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="ui.label.mrno"/>:</div>
				<div class="sboFieldInput">
					<html:text property="mrno" size="10" value="${param.mrno}" styleId="mrno" />
					<div id="mrnoContainer" style="width: 300px"></div>
				</div>
			</div>
			<c:if test="${module eq 'DEP_LAB'}">
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="laboratory.signedoffreportslist.search.sampleno"/>:</div>
					<div class="sboFieldInput">
						<input type="text" name="sampleNo" id="sampleNo" value="${ifn:cleanHtmlAttribute(param.sampleNo)}"/>
					</div>
				</div>
			</c:if>
			<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="laboratory.signedoffreportslist.search.labno"/>:</div>
				<div class="sboFieldInput">
					<html:text property="labno" size="10" />
				</div>
			</div>
			</div>
			<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="laboratory.signedoffreportslist.search.phonenumber"/>:</div>
				<div class="sboFieldInput">
				<html:text property="phoneNo" size="10" style="width: 120px;" /><img class="imgHelpText" src="${cpath}/images/help.png"
								title='<insta:ltext key="laboratory.signedoffreportslist.report.incomingsampleregistration"/>'/>
				</div>
			</div>
		</div>


		<div id="optionalFilter" style="clear: both; display: ${filterClosed ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td >
						<div class="sfLabel"><insta:ltext key="laboratory.signedoffreportslist.search.department"/>:</div>
						<div class="sfField">
							<html:select property="department" value="${userDept}" styleClass="dropdown">
								<html:option value="">${select}</html:option>
								<c:forEach var="dept" items="${DiagArraylist}">
									<html:option value="${dept.DDEPT_ID}">${dept.DDEPT_NAME}</html:option>
								</c:forEach>
							</html:select>
						</div>
						<div class="sfLabel"><insta:ltext key="laboratory.signedoffreportslist.search.testname"/>:</div>
						<div class="sfField" style="height: 20px">
							<div id="test_wrapper">
								<html:text property="diagname" styleId="diagname" value="${param.diagname}"/>
							<div id="test_container" style="width: 300px"></div>
							</div>
						</div>
						<div class="sfLabel"><insta:ltext key="laboratory.signedoffreportslist.search.hist.cyto.shortimpression"/>:</div>
						<div class="sfField" style="height: 20px">
							<div id="short_impression_wrapper">
								<html:text property="shortImpression" styleId="shortImpression" value="${param.shortImpression}"/>
							<div id="shot_impression_container"></div>
							</div>
						</div>	
						<c:if test="${module == 'DEP_LAB'}">
							<div class="sfLabel"><insta:ltext key="laboratory.signedoffreportslist.search.referredby"/></div>
							<div class="sfField" style="height: 20px">
								<div id="referalAutoComplete">
									<input type="text" name="_referaldoctorName" id="_referaldoctorName" value="${ifn:cleanHtmlAttribute(param._referaldoctorName)}" />
									<input type="hidden" name="reference_docto_id" id="reference_docto_id" value="${param.reference_docto_id}"/>
									<div id="referalNameContainer"></div>
								</div>
							</div>
						</c:if>

                           <div class="sfLabel"><insta:ltext key="laboratory.reportsearch.search.prescribedby"/></div>
                           <div class="sfField" style="height: 20px">
                                <div id="referalAutoComplete">
                                     <input type="text" name="_doctor_name" id="_doctor_name" value="${ifn:cleanHtmlAttribute(param._doctor_name)}" />
                                     <input type="hidden" name="pres_doctor" id="pres_doctor" value="${param.pres_doctor}"/>
                                     <div id="prescribedDocContainer"></div>
                                 </div>
                           </div>

					</td>
					<td>
						<table>
							<tr>
								<td class="last" style="padding-top: 0px; padding-bottom: 0px"><div class="sfLabel"><insta:ltext key="laboratory.signedoffreportslist.search.hospitals"/>:</div></td>
							</tr>
							<tr>
								<td class="last"><html:checkbox property="showOnlyInhouseTests"	value="true"  /><insta:ltext key="laboratory.signedoffreportslist.search.incoming"/>:</td>
							</tr>
							<tr>
								<td class="last">
									<div id="inhouse_wrapper">
										<html:text property="inhouse"  styleId="inhouse" value="${param.inhouse}" />
									<div id="inhouse_container"></div>
								</td>
							</tr>
							<tr><td class="last">&nbsp;</td></tr>
							<tr>
								<td class="last"><html:checkbox property="showOnlyouthouseTests"	value="true"/><insta:ltext key="laboratory.signedoffreportslist.search.outsourcing"/>:</td>
							</tr>
							<tr>
								<td class="last">
									<div id="outhouse_wrapper">
										<html:text property="outhouse" styleId="outhouse" value="${param.outhouse}" />
										<div id="outhouse_container"></div>
									</div>
								</td>
							</tr>
						</table>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="laboratory.signedoffreportslist.search.testdate"/>:</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="laboratory.signedoffreportslist.search.from"/>:</div>
								<insta:datewidget name="fdate" valid="past"	value="${param.fdate}" />
							</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="laboratory.signedoffreportslist.search.to"/>:</div>
								<insta:datewidget name="tdate" valid="past"	value="${param.tdate}" />
						</div>
						<div class="sfLabel"><insta:ltext key="laboratory.signedoffreportslist.search.signedoffdate"/>:</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="laboratory.signedoffreportslist.search.from"/>:</div>
								<insta:datewidget name="rfdate" valid="past" value="${param.rfdate}" />
							</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="laboratory.signedoffreportslist.search.to"/>:</div>
								<insta:datewidget name="rtdate" valid="past" value="${param.rtdate}" />
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="laboratory.signedoffreportslist.search.patienttype"/>:</div>
						<div class="sfField">
							<html:checkbox property="patientAll"
								onclick="enablePatientType()"><insta:ltext key="laboratory.signedoffreportslist.search.all"/></html:checkbox><br />
							<html:checkbox property="patientIp"><insta:ltext key="laboratory.signedoffreportslist.search.ip"/></html:checkbox><br />
							<html:checkbox property="patientOp"><insta:ltext key="laboratory.signedoffreportslist.search.op"/></html:checkbox><br />
							<html:checkbox property="patientIn"><insta:ltext key="laboratory.signedoffreportslist.search.incoming"/></html:checkbox><br />
						</div>
						<div class="sfLabel"><insta:ltext key="laboratory.signedoffreportslist.report.sponsortype"/></div>
						<div class="sfField">
							<insta:checkgroup name="patient_sponsor_type" selValues="${paramValues.patient_sponsor_type}"
							opvalues="S,R" optexts="${sponsorType}"/>
						</div>
						<c:if test="${sampleCollectionCenterId == -1 && module == 'DEP_LAB'}">
								<div class="sfLabel"><insta:ltext key="laboratory.signedoffreportslist.search.collectioncenter"/>:</div>
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
					</td>
					<td class="last">
						<div class="sfLabel"><insta:ltext key="laboratory.signedoffreportslist.search.handoverstatus"/>:</div>
						<div class="sfField">
							<insta:checkgroup name="ready_for_handover" selValues="${paramValues.ready_for_handover}"
							opvalues="Y,N" optexts="Ready for Handover,Bill Pending"/>
						</div>
						<div class="sfLabel"><insta:ltext key="laboratory.signedoffreportslist.search.handedover"/>:</div>
						<div class="sfField">
							<html:select property="handed_over" styleClass="dropdown">
								<html:option value=""><insta:ltext key="laboratory.signedoffreportslist.search.allselect"/></html:option>
								<html:option value="Y"><insta:ltext key="laboratory.signedoffreportslist.search.yes"/></html:option>
								<html:option value="N"><insta:ltext key="laboratory.signedoffreportslist.search.no"/></html:option>
							</html:select>
						</div>
						<div class="sfLabel"><insta:ltext key="laboratory.signedoffreportslist.search.testpriority"/>:</div>
						<div class="sfField">
							<insta:checkgroup name="priority" selValues="${paramValues.priority}"
							opvalues="S,R" optexts="${priority}"/>
						</div>
						<div class="sfLabel"> <insta:ltext key="laboratory.reportsearch.search.reportswithtestresults"/></div>
							<div class="sfField">
							<insta:checkgroup name="report_results_severity_status" selValues="${paramValues.report_results_severity_status}"
							opvalues="A,H,C,T" optexts="${severitystatus}"/>
						</div>
					</td>
				</tr>
			</table>
		</div>
	</insta:search>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<c:set var="reportList" value="${pagedList.dtoList}" />
	<c:set var="labCenter" value="${not empty outsourceRecForUsercenter ?
				(outsourceRecForUsercenter.map.outsource_dest_type eq 'C') : false}" />
	<div class="resultList">
		<table class="resultList" align="center" width="100%" id="resultTable">
			<tr>
				<insta:sortablecolumn name="mrno" title="${mrno}"/>
				<th>
					<insta:ltext key="laboratory.signedoffreportslist.search.visitidheader"/>
					(<insta:ltext key="laboratory.signedoffreportslist.report.sponsor_type"/>)
				</th>
				<c:if test="${module == 'DEP_LAB' && max_centers_inc_default > 1 && labCenter}">
					<th><insta:ltext key="laboratory.signedoffreportslist.search.patientCenter" /></th>
				</c:if>
				<th><insta:ltext key="ui.label.patient.name"/></th>
				<th><insta:ltext key="laboratory.signedoffreportslist.search.reportname"/></th>
				<th><insta:ltext key="laboratory.testconduction.list.severity.status"/></th>
				<insta:sortablecolumn name="reportdate" title="${signedoffdate}"/>		
				<th><insta:ltext key="laboratory.signedoffreportslist.search.msgstatus"/><img title='Critical Lab Value Result Notification Message Status' src="${cpath}/images/help.png" class="imgHelpText"></th>		
				<th><insta:ltext key="laboratory.signedoffreportslist.search.printscount"/></th>
				<th><insta:ltext key="laboratory.signedoffreportslist.search.tests.package"/></th>
			</tr>
			<c:set var="rowIndex" value="0"/>
			<c:forEach var="report" items="${reportList }" varStatus="st">
			<c:set var="testBean" value=""/>
			<c:set var="outhouseType" value=""/>
			<c:forEach var="tests" items="${reportsMap[report.map.report_id]}">
				<c:if test="${tests.map.pat_id eq report.map.pat_id}">
					<c:set var="testBean" value="${tests}" />
					<c:if test="${outhouseType ne 'IO' }">
						<c:set var="outhouseType" value="${tests.map.outsource_dest_type}" />
					</c:if>
				</c:if>
			</c:forEach>
			<c:set var="addaddendum" value="${report.map.addendum_signed_off == 'N'}"/>
			<c:set var="signoffaddendum" value="${report.map.has_addendum eq 'Y' &&report.map.addendum_signed_off == 'N' }"/>
			<c:set var="flagColor" value="empty"/>
			<c:if test="${report.map.handed_over == 'Y'}">
				<c:set var="flagColor" value="green"/>
			</c:if>
			<c:choose>
				<c:when test="${report.map.has_addendum eq 'N'}">
					<c:set var="handOverDisabled" value="${report.map.handed_over eq 'N'}"/>
				</c:when>
				<c:otherwise>
					<c:set var="handOverDisabled" value="${report.map.addendum_signed_off == 'Y' && report.map.handed_over eq 'N'}"/>
				</c:otherwise>
			</c:choose>
			<c:set var="handedOverReport" value="${report.map.handed_over eq 'Y'}"/>
			<c:set var="rowIndex" value="${rowIndex+1}"/>

			<c:set var="isCenterAndLab" value="${module == 'DEP_LAB' && max_centers_inc_default > 1}" />

			<c:set var="allowParentcenterTo" value="${!isCenterAndLab || (isCenterAndLab && ((report.map.hospital eq 'hospital' && ((not empty testBean.map.outsource_dest_prescribed_id && centerId !=0) || empty testBean.map.outsource_dest_prescribed_id)) || (report.map.hospital ne 'hospital' && report.map.incoming_source_type ne 'C')))}" />

			<c:set var="allowAmmendment" value="${!isCenterAndLab || (isCenterAndLab  && (((not empty testBean.map.outsource_dest_prescribed_id || report.map.incoming_source_type eq 'C') && report.map.signoff_center eq centerId) || (empty testBean.map.outsource_dest_prescribed_id && report.map.incoming_source_type ne 'C')))}" />

			<c:set var="addendumForInternalLab" value="${isCenterAndLab ? ((not empty testBean.map.outsource_dest_prescribed_id || report.map.incoming_source_type eq 'C') ? report.map.signoff_center eq centerId : true) : true }" />

			<c:set var="notAllowChldTstToDefaultCen" value="${(isCenterAndLab && not empty report.map.incoming_source_type && report.map.incoming_source_type eq 'C') ? (report.map.signoff_center eq centerId) : true}" />

			<c:set var="allowPrintReportVisitWise" value="false"/>
			<c:if test="${(actionRightsMap.allow_print_report_visit_wise == 'A')||(roleId==1)||(roleId==2)}">
				<c:set var="allowPrintReportVisitWise" value="true"/>
			</c:if>
			<c:set var="isInstaOuthouse" value="${not empty outhouseType && outhouseType eq 'IO'}"/>
			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${rowIndex}, event, 'resultTable',
						{printerId: printerType, reportId: '${report.map.report_id}',amendresult :'Y',
						 emailId:'${report.map.email_id}',mrNo:'${report.map.mr_no}',
						 category: '${ifn:cleanJavaScript(param.category)}', visitid: '${report.map.pat_id}'},
						[true,${!handedOverReport && amendTestResultsRt && allowAmmendment && !isInstaOuthouse},${!handedOverReport && addaddendum && addendumForInternalLab},${signoffaddendum && addendumForInternalLab},${handOverDisabled && allowParentcenterTo},true, ${allowPrintReportVisitWise}],'report');"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${rowIndex}">
					<td>
						<img src="${cpath}/images/${flagColor}_flag.gif" class="flag"/>
						${report.map.mr_no }
					</td>
					<td>${report.map.pat_id} (${report.map.patient_sponsor_type})</td>
					<c:if test="${module == 'DEP_LAB' && max_centers_inc_default > 1 && labCenter}">
						<td>${isCenterAndLab && not empty outsourceRecForUsercenter ?
								(outsourceRecForUsercenter.map.outsource_dest_type eq 'C' ? hospCentersList[report.map.source_center_id].map.center_name: '') : ''}</td>
					</c:if>
					<td><insta:truncLabel value="${report.map.name } ${report.map.patient_name }" length="20"/>  </td>
					<td style="height:37px">
						<input type="checkbox" name="revert_signoff" value="${report.map.report_id }" ${(allowAmmendment && !isInstaOuthouse) ? '' : 'disabled'}/>
						<insta:truncLabel value="${report.map.report_name }" length="20"/>
						<div style="margin-left:22px;margin-top:1px">
							<c:choose>
								<c:when test="${report.map.partial_patient_due == 0}"> 
								     <span class="reportStatus" style="border-color:green;color: green">Ready for Handover</span>
								 </c:when>
								<c:otherwise>
									<span class="reportStatus" style="color:red">Bill Pending</span>  
								</c:otherwise>
							</c:choose>
						</div>
					</td>
					<td style="text-align: center">
					<c:choose>
						<c:when test="${report.map.report_results_severity_status == 'A' }">
							<insta:ltext key="laboratory.reportsearch.search.allnormal"/>
						</c:when>
						<c:when test="${report.map.report_results_severity_status == 'H' }">
							<insta:ltext key="laboratory.reportsearch.search.abnormalresultsLbl"/>                              
						</c:when>
						<c:when test="${report.map.report_results_severity_status == 'C' }">
							<insta:ltext key="laboratory.reportsearch.search.criticalresultsLbl"/>                         
						</c:when>
					</c:choose>
					</td>
					<td><fmt:formatDate value="${report.map.report_date }" pattern="dd-MM-yyyy HH:mm"/></td>
					<td>
						<c:choose>
							<c:when test="${report.map.notification_sent == 'Y' }">
								<insta:ltext key="laboratory.signedoffreportslist.search.sent"/>
							</c:when>
							<c:when test="${report.map.notification_sent == 'N' }">
								<insta:ltext key="laboratory.signedoffreportslist.search.notsent"/>                            
							</c:when>
							<c:when test="${report.map.notification_sent == '' }">
								<insta:ltext key="laboratory.signedoffreportslist.search.notapplicable"/>                        
							</c:when>
					</c:choose>
					</td>
					<td>${report.map.num_prints }
						<input type="hidden" name="handed_over" value="${report.map.handed_over}"/>
					</td>
					<td>&nbsp;</td>
				</tr>
				<c:set var="rowIndex" value="${rowIndex+1}"/>
				<tr onclick="showToolbar(${rowIndex}, event, 'resultTable',
						{printerId: '0',category: '${ifn:cleanJavaScript(param.category)}',
						 visitid: '${report.map.pat_id}',
						 patientid:'${report.map.pat_id}',
						 signedOffReport:${report.map.report_id},mrno:'${report.map.mr_no }',hospital:'${report.map.hospital}'},
						[${!handedOverReport}],'test');"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${rowIndex}">
					<c:set var="col" value="${(module == 'DEP_LAB' && max_centers_inc_default > 1 && labCenter) ? 7 : 6}"/>
					<td></td>
					<td></td>
					<td colspan="${col}" class="indent">&nbsp;</td>
					<td valign="top" class="subResult">
						<c:forEach varStatus="rst" var="test" items="${reportsMap[report.map.report_id]}" >
							<c:set var="blockParentTestToDefaultCen" value="${(isCenterAndLab && report.map.hospital eq 'hospital' && not empty test.map.outsource_dest_prescribed_id && centerId eq 0) ? 'disabled' : ''}" />
							<c:set var="blockChildTestToDefautCen" value="${notAllowChldTstToDefaultCen ? '' : 'disabled'}"/>
							<c:if test="${test.map.pat_id eq report.map.pat_id}" >
								<input type="checkbox" name="amendResults" value="${test.map['prescribed_id']}" ${(isCenterAndLab && report.map.hospital eq 'incoming') ? blockChildTestToDefautCen : blockParentTestToDefaultCen}/>
								<c:choose>
									<c:when test="${test.map['priority']=='S'}">
										<b><font color="#444444"><insta:truncLabel value="${test.map['test_name']}" length="20"/></font></b>
										${test.map['package_name'] }
									</c:when>
									<c:otherwise>
										<insta:truncLabel value="${test.map['test_name']}" length="20"/>
										${test.map['package_name'] }
									</c:otherwise>
								</c:choose>
								<br/>
							</c:if>
						</c:forEach>
					</td>
				</tr>
			</c:forEach>
		</table>
		<c:if test="${ param._method eq 'getReportList'  && empty reportList}">
			<insta:noresults hasResults="${filterClosed}"/>
		</c:if>
	</div>
	<div>
		<c:if test="${revertSignOffRts}">
			<button type="button" accesskey="R" onclick="revertSignOff()">
			<label><b><u><insta:ltext key="laboratory.signedoffreportslist.search.r"/></u></b><insta:ltext key="laboratory.signedoffreportslist.search.evertsignoff"/></label></button>&nbsp;
		</c:if>
	</div>
	<c:if test="${ param._method eq 'getReportList'}">
		<div class="legend">
		<insta:selectdb name="_printerType" table="printer_definition"
			valuecol="printer_id" displaycol="printer_definition_name" id="printerType"
			value="${printerBean.map.printer_id}" onchange="setPrinterId(this)"/>
		</div>
	</c:if>
	<div style="clear:both"></div>
	<div class="legend" style="margin-top: 10px;display: ${not empty reportList? 'block' : 'none'}">
		<div class="flag"><img src='${cpath}/images/green_flag.gif'/></div>
		<div class="flagText"><insta:ltext key="laboratory.signedoffreportslist.search.handedover"/></div>
	</div>
	<div style="clear:both"/>
	<div class="legend" style="margin-top: 10px;display: ${not empty reportList? 'block' : 'none'}">
		<div class="flagText"><insta:ltext key="laboratory.signedoffreportslist.search.legend.sponsor"/></div>
		<div class="flagText" style="padding-left: 10px"><insta:ltext key="laboratory.signedoffreportslist.search.legend.retail"/></div>
	</div>

 <div id="sendEmailDialog" style="display: none;">
  		<div class="hd"><insta:ltext key="js.laboratory.radiology.reportlist.sendpatientreportdialog.header"/></div>
  		<div class="bd">
  		<fieldset class="fieldSetBorder">
      			<legend class="fieldSetLabel"><insta:ltext key="js.laboratory.radiology.reportlist.sendpatientreportdialog.title"/></legend>
            <table class="formtable">
            <tr>
              <td class="formlabel">
                  To Registered Email:
              </td>
              <td>
               <input type="text" name="email_id" id="email_id"/>
              </td>
            </tr>
           </table>
     </fieldset>
        <table>
     				<tr>
     					<td><button type="button" id="sendEmailBtn" value="Save" ><insta:ltext key="js.laboratory.radiology.reportlist.saveandsend"/></button></td>
     				</tr>
     			</table>
  		</div>
  </div>

</html:form>



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

