<%@page import="com.insta.hms.common.Encoder"%>
<%@ page contentType="text/html;charset=windows-1252"%>
<%@ page import="java.util.ArrayList"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-template.tld" prefix="template"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>


<html >
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>File Upload - Insta HMS</title>
<link rel="File-List" href="main_files/filelist.xml">

<hms:link type="css" file="style.css" />
<insta:link type="script" file="aw.js" />
<insta:link type="script" file="date_go.js" />
<insta:link type="script" file="multifile_alltypesupload.js" />
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="script" file="DatePick.js"/>
<insta:link type="script" file="tableSearch.js"/>
<insta:link type="script" file="ajax.js"/>
<insta:link type="css" file="hms.css"/>

<jsp:include page="/pages/sessionCheck.jsp" />
<jsp:include page="/pages/Common/MrnoPrefix.jsp" />

<script src="../../scripts/aw.js"></script>
<link href="../../css/aw.css" rel="stylesheet"></link>
<link href="../../css/style.css" rel="stylesheet" type="text/css"></link>
<link rel="StyleSheet" href="../../../css/dtree.css" type="text/css" />
<script type="text/javascript" src="../../../scripts/dtree.js"></script>
<script type="text/javascript">
var mrnoFromAction = '<%=request.getAttribute("mrno")!=null?Encoder.cleanJavaScript((String)request.getAttribute("mrno")):""%>';
var patNameFromAction = '<%=request.getAttribute("patName")!=null?Encoder.cleanJavaScript((String)request.getAttribute("patName")):""%>';
var oldMrnoFromAction = '<%=request.getAttribute("oldMrno")!=null?Encoder.cleanJavaScript((String)request.getAttribute("oldMrno")):""%>';

var oldMrNos = <%=request.getAttribute("oldMrnos")%>;
var selectedPatientId = "";

var pageAction = '<%=request.getAttribute("pageAction")%>';

// get patient details from valid mrno

	function patientDetails(){
		var Mrno = document.forms[0].mrNo.value;
		var oldMrno = document.forms[0].oldMrNo.value;

		if(window.XMLHttpRequest){
			req = new XMLHttpRequest();
		}
		else if(window.ActiveXObject){
			req = new ActiveXObject("MSXML2.XMLHTTP");
		}
		req.onreadystatechange = onResponse1;
		var url="medicalfileuploadAction.do?method=getpatientDetails&Mrno="+Mrno+"&oldMrno="+oldMrno;
		req.open("POST",url.toString(),true);
		req.setRequestHeader("Content-Type","text/xml");
		req.send(null);
	}

	function checkReadyState1(obj1){
		if(obj1.readyState == 4){
			if(obj1.status == 200){
				return true;
			}
		}
	}

	var tstr1=null;
	var x1=null;
	function onResponse1(){
		if(checkReadyState1(req)){
			tstr1=req.responseXML;
			if(tstr1!=null){
				getXML1();
			}
		}
	}

    var doc1;
    var ar = null;
	function getXML1(){
	    doc1 = tstr1;
	    x1=doc1.documentElement;
	    var len1=x1.childNodes[0].childNodes.length;
		for(var n=0;n<len1;n++){
			document.getElementById("patName").value=x1.childNodes[0].childNodes[n].attributes.getNamedItem('class1').nodeValue;
			document.getElementById("oldMrNo").value=x1.childNodes[0].childNodes[n].attributes.getNamedItem('class2').nodeValue;
			document.getElementById("mrNo").value=x1.childNodes[0].childNodes[n].attributes.getNamedItem('class3').nodeValue;
        }
	}// end of patient details from mrno

	function checkMrno(){
		swapModes();
		document.getElementById('docid_test').value = '';
		var nodes = document.getElementById("MRNOCHECK").getElementsByTagName("MRNOCHECKs")[0].getElementsByTagName("mrnocheck");
		var len=nodes.length;
		var mrno=document.forms[0].mrNo.value;
		if(mrno == "")
			return false;
		var foundFlag=true;
		for(var i=0;i<len;i++){
			if(mrno == nodes[i].attributes.getNamedItem('class1').nodeValue){
				foundFlag=false;
				break;
			}
		}
		if(foundFlag){
			document.forms[0].mrNo.value="";
			document.forms[0].oldMrNo.value="";
			document.forms[0].patName.value="";
			alert("Invalid MR No.");
			return false;
			//foundFlag = true;
		}
	}   //end of check mrno

	var mrnoOfOldMrNo = "";
	function checkOldMrno(obj){
	if(document.getElementById(obj.name).value!=""){
	   swapModes();
	   document.forms[0].mrNo.value="";
	   document.getElementById('docid_test').value = '';
	   var flag = false;
	   oldmrno = document.getElementById(obj.name).value;
	   if(oldmrno!=""){
			for (var i=0; i<oldMrNos.length; i++) {
				var eOldMrNoRow = oldMrNos[i];
				if (eOldMrNoRow.OLDMRNO == oldmrno) {
					mrnoOfOldMrNo = eOldMrNoRow.MR_NO
					flag = true;
				}
			}
			if (flag == true) {
				return true;
			} else {
				mrnoOfOldMrNo = "";
				document.forms[0].mrNo.value="";
				document.forms[0].oldMrNo.value="";
				document.forms[0].patName.value="";
				alert("Invalid Old MR No.");
	  			return false;
			}
		}
		}
	}
	function checkEnterKey(e){
		if(e.keyCode == 13){
			return false;
		}
		return true;
	}

	function validateOnView(){
		swapModes();
		if(getMrnoSearchData()){
	        var Mrno = document.forms[0].mrNo.value;
	        var doctype = "";
	        var patid = "";

	        if(Mrno   == ""){
				Mrno = mrnoOfOldMrNo;
			}

	        xmlreq = newXMLHttpRequest();

			var url="medicalfileuploadAction.do?method=getAvailableDocuments&Mrno="+Mrno+"&doctype=" +doctype+"&patid="+patid;
		  	getResponseHandlerText(xmlreq, getXMLData, url);
		}
      }

	var xmlDataLen =0;
	function getXMLData(domDocObj){
		var jsonExpression = "(" + domDocObj + ")";
		var documentDetails = eval(jsonExpression);

		var mrno = document.forms[0].mrNo.value;
		if (mrno == "") {
			mrno = mrnoOfOldMrNo;
		}
		var tabId = document.getElementById("avail_doc_table");
		var len = tabId.rows.length;
		if (len > 1) {
			for (var i=1; i<len; i++) {
				tabId.deleteRow(-1);
			}
		}
		xmlDataLen = documentDetails.length;
		var visitIdObj = document.getElementById("selExVisitId");
		visitIdObj.length=1;
		var innerhtmltab = document.getElementById("avail_doc_table");

		var k = 1;
		for (var i=0;i<xmlDataLen;i++) {
			var documentAtrbs = documentDetails[i];

			var trObj = "",tdObj="";
			if (documentAtrbs.DOCID != undefined) {
				trObj = innerhtmltab.insertRow(-1);
				tdObj = trObj.insertCell(-1);
				tdObj.innerHTML = '<span class="tabletext">' + documentAtrbs.DOCID + '</span>';

				tdObj = trObj.insertCell(-1);
				var shortValue = documentAtrbs.DOC_NAME;
				if(shortValue.length > 15){
					shortValue = shortValue.substring(0,13);
					shortValue = shortValue+"...";
				}
				var docDate = documentAtrbs.UPLOAD_DATE;
				var patientId = documentAtrbs.PATIENT_ID;
				if (docDate ==  undefined) {
					docDate = '';
				}
				if (patientId == undefined) {
					patientId = '';
				}
				tdObj.innerHTML ='<span class="tabletext"><a href="#" id="idto" onclick="javascript:getValues(\''+mrno+'\',\''+documentAtrbs.DOCID+'\',\''+ documentAtrbs.DOC_NAME +'\',\'' +
								documentAtrbs.DOC_TYPE + '\',\'' + docDate +'\',\'' +
								patientId+'\',\''+ 'docName' + '\',\''+ documentAtrbs.MODULE_ID + '\')" title="'+documentAtrbs.DOC_NAME+'">' +shortValue+ '</a></span>';

				tdObj = trObj.insertCell(-1);
				tdObj.innerHTML = '<span class="tabletext">' + documentAtrbs.DOC_TYPE_NAME + '</span>';

				tdObj = trObj.insertCell(-1);
				tdObj.innerHTML = '<span class="tabletext">' + docDate + '</span>';

				tdObj = trObj.insertCell(-1);
				tdObj.innerHTML = '<span class="tabletext">' + patientId + '</span>';

				tdObj = trObj.insertCell(-1);
				tdObj.innerHTML = '<span class="tabletext"><a href="#" id="idto" onclick="javascript:displayImage(\''+mrno+'\',\''+documentAtrbs.DOCID+'\',\''+ documentAtrbs.DOC_NAME +'\',\'' +
								documentAtrbs.DOC_TYPE + '\',\'' + docDate +'\',\'' +
								patientId+'\',\''+ documentAtrbs.MODULE_ID + '\')" >' +'Preview'+ '</a></span>';
			}

			if (documentAtrbs.DOCID == undefined) {
				var visitIdExists = false;
				for (var j=0; j<visitIdObj.length; j++) {
					if (visitIdObj.options[j].value == documentAtrbs.PATIENT_ID) {
						visitIdExists = true;
						break;
					}
				}
				if (visitIdExists == false) {
					visitIdObj.length = k+1;
					visitIdObj.options[k].value = documentAtrbs.PATIENT_ID;
					visitIdObj.options[k].text = documentAtrbs.PATIENT_ID;
					k++;
				}
			}

		}
	}

    function getValues(mrno, docId, docname, doctype, docdate, patid, from, mod_id){
    	if (mod_id == "mod_discharge") {
    		alert("System uploaded files, cannot be edited.");
    		return false;
    	} else {
    	}
    	var updateFileDiv = document.getElementById("updateFileDiv");
		var singleUpdate = document.getElementById("singleUpdate");
		if (updateFileDiv.style.display == 'block') {

		} else {
			updateFileDiv.style.display = 'block';
			singleUpdate.style.display = 'none';
		}
		var radioBtns = document.getElementsByName('radioVistid');
		if (radioBtns[0].checked) {
			document.getElementById('selExVisitId').value = 'NULLx';
			document.getElementById("oldvisitid").style.display = 'none';
			radioBtns[0].checked = false;
		}
		if (radioBtns[1].checked) {
			document.getElementById('txtNewVisitId').value = '';
			document.getElementById('visitType').value = 'NULLx';
			document.getElementById('visitDate').value = '';
			document.getElementById("newvisitid").style.display = 'none';
			radioBtns[1].checked = false;
		}
		if (patid != '') {
			document.getElementById('selExVisitId').value = patid;
			document.getElementById("oldvisitid").style.display = 'block';
			radioBtns[0].checked = true;
		}
		document.getElementById('docTag').value = docname;
		document.getElementById('docType').value = doctype;
		document.getElementById('docDate').value = docdate;
		//document.getElementById("oldvisitid").style.display = 'block';
		document.getElementById('docid_test').value = docId;
		document.getElementById('selExVisitId').value = patid;

		if (from == 'docName') {
			var divObj = document.getElementById("showImage");
			if (divObj.style.display == 'block') {
	    		divObj.style.display = 'none';
	    	}
    	}
    	return true;
    }

    function swapModes(){
    	var updateFileDiv = document.getElementById("updateFileDiv");
		var singleUpdate = document.getElementById("singleUpdate");
		if (updateFileDiv.style.display == 'block') {
			var radioBtns = document.getElementsByName('radioVistid');
			if (radioBtns[0].checked) {
				document.getElementById('selExVisitId').value = 'NULLx';
				radioBtns[0].checked = false;
			}
			if (radioBtns[1].checked) {
				document.getElementById('txtNewVisitId').value = '';
				document.getElementById('visitType').value = 'NULLx';
				document.getElementById('visitDate').value = '';
				radioBtns[1].checked = false;
			}
			singleUpdate.style.display = 'block';
			updateFileDiv.style.display = 'none';
			if (document.getElementById("showImage").style.display == 'block') {
				document.getElementById("showImage").style.display = 'none';
			}
		} else {

		}
		var tabId = document.getElementById("avail_doc_table");
		var len = tabId.rows.length;
		if (len > 1) {
			for (var i=1; i<len; i++) {
				tabId.deleteRow(-1);
			}
		}
    }

    function displayImage(mrno, docid, docname, doctype, docdate, patid, mod_id){
    	var divObj = document.getElementById("showImage");
		if (mod_id == "mod_discharge") {
	    	alert("System uploaded files, cannot be edited.");
    		return false;
	    } else {
	    	y=document.getElementsByName('leftNavigation')[0];
			document.getElementById("showImage").removeChild(y);
	    	var iframe = document.createElement("IFRAME");
	    	iframe.setAttribute("name","leftNavigation");
	    	iframe.setAttribute("width","100%");
	    	iframe.setAttribute("height","100%");
	    	iframe.setAttribute("frameborder","0");
	    	iframe.setAttribute("src","");
			document.getElementById("showImage").appendChild(iframe);
	    	if (getValues(mrno, docid, docname, doctype, docdate, patid, 'preview', mod_id)){
		    	document.getElementsByName('leftNavigation')[0].src = 'medicalfileuploadAction.do?method=getImageContent&Mrno='+mrno+'&docid='+docid;
	    		divObj.style.display = 'block';
		    }
	    }
    }

    function getMrnoSearchData(){
		if(pageAction == "update"){
			return true;
		}
		var varMrno =document.forms[0].mrNo.value;
		var varOldMrno = document.forms[0].oldMrNo.value;

		var flag = false;
		if((varMrno == "") && (varOldMrno == "")){
			alert("Please Enter atleast one registration no");
			document.forms[0].mrNo.focus();
			return false;
		} else if ((varMrno != "") && (varOldMrno != "")) {
			for (var i=0; i<oldMrNos.length; i++) {
				var eOldMrNoRow = oldMrNos[i];
				if (eOldMrNoRow.OLDMRNO == varOldMrno) {
					if(eOldMrNoRow.MR_NO == varMrno) {
						flag = true;
					}
				}
			}
			if (flag) {
			} else {
				document.forms[0].mrNo.value = "";
				document.forms[0].oldMrNo.value = "";
				document.forms[0].patName.value = "";

				alert("Old Registration No. not related to Registration No.");
				return false;
			}
		}
		return true;
	 }

	function showVisit(radioValue) {
		if (radioValue == 'OLD') {
			document.getElementById('oldvisitid').style.display = 'block';
			document.getElementById('txtNewVisitId').value = '';
			document.getElementById('visitType').value = 'NULLx';
			document.getElementById('visitDate').value = '';
			document.getElementById('newvisitid').style.display = 'none';
		} else {
			document.getElementById('selExVisitId').value = 'NULLx';
			document.getElementById('oldvisitid').style.display = 'none';
			document.getElementById('newvisitid').style.display = 'block';
		}
	}

	function clearFields(){
			document.getElementById("mrNo").value="";
			document.getElementById("oldMrNo").value="";
			document.getElementById("patName").value="";
	}

	function validateOnSave(){
		if(getMrnoSearchData()){
			var mrno = document.getElementById("mrNo").value;
			if(mrno == ""){
				document.getElementById("mrNo").value = mrnoOfOldMrNo;
			}

			//var temp = document.getElementsByTagName("theFile[0]");
			//alert(temp.length);
			//if(temp.length == 0){
			//	alert("sdfsd");
			//	return false;
			//}


			if (document.getElementById("updateFileDiv").style.display == 'block') {
				var docTag = document.getElementById('docTag').value;
				var docType = document.getElementById('docType').value;
				var docDate = document.getElementById('docDate').value;
				var visitType = document.getElementById('visitType').value;
				var visitDate = document.getElementById('visitDate').value;
				var txtNewVisitId = document.getElementById('txtNewVisitId').value;
				if (docTag == 'NULLx') {
					alert('please enter the doument tag');
					document.getElementById('docTag').focus();
					return false;
				}
				if (docType == 'NULLx') {
					alert('please select the document type');
					document.getElementById('docType').focus();
					return false;
				}
				if (docDate == '') {

				} else {
					var msg = validateDateStr(docDate,"past");
					if (msg == null) {
					} else {
						alert(msg);
						document.getElementById('docDate').value="";
						document.getElementById('docDate').focus();
						return false
					}
				}
				if (document.getElementsByName('radioVistid')[1].checked) {
					if ((visitType == '') && (visitDate == '') && (txtNewVisitId == '')) {
					} else{
						if (txtNewVisitId == '') {
							alert('please enter the visitid');
							document.getElementById('txtNewVisitId').focus();
							return false;
						}
						if (visitType == '') {
							alert('please select visit type');
							document.getElementById('visitType').focus();
							return false;
						}
						if (visitDate == '') {
							alert('please enter the visit date');
							doucment.getElementById('visitDate').focus();
							return false;
						} else {
							var msg = validateDateStr(visitDate,"past");
							if (msg == null) {
							} else {
								alert(msg);
								document.getElementById('visitDate').value="";
								document.getElementById('visitDate').focus();
								return false
							}
						}
					}
				}
				if (confirm("do you want to Update")){
					document.forms[0].method.value="updatemedicalFile";
					document.forms[0].action="medicalfileuploadAction.do";
					document.forms[0].submit();
				} else {
					return false
				}
			} else {

				var td = document.getElementById('fileTD');
				if (td.childNodes.length == 2) {
					if (td.childNodes[1].value  == "") {
						alert("Please upload file(s) to save");
						return false;
					}
				}

				document.forms[0].method.value="MedicalFileInsert";
				document.forms[0].action="medicalfileuploadAction.do";
				document.forms[0].submit();
			}
		}else{
			return false;
		}
	}

	function selectMode(){
		if (pageAction == "update") {
			document.getElementById('mrNo').value = mrnoFromAction;
			document.getElementById('patName').value = trimAll(patNameFromAction);
			document.getElementById('oldMrNo').value = trimAll(oldMrnoFromAction);

			validateOnView();
			return true;
		} else {
			document.forms[0].mrNo.focus();
		}
		if('${ifn:cleanJavaScript(mrno)}' != null){
		document.getElementById('mrNo').value = '${ifn:cleanJavaScript(mrno)}';
		onChangeMrno();
		}
	}

	function checkForUniqueNo(visitId){
		var selExVisitIds = document.getElementById('selExVisitId');
		for (var i=0; i<selExVisitIds.length; i++) {
			if (selExVisitIds[i].value == visitId.value) {
				alert('Visit ID \''+ visitId.value +'\' already exists for this MrNo.');
				visitId.value = "";
				break;
			}
		}
		if (visitId.value == "") {
			document.getElementById('txtNewVisitId').focus();
			return false;
		}
	}
	function validateOnDelete(){
		if(getMrnoSearchData()){
			var docid = document.getElementById('docid_test').value;
			if(docid == ""){
				alert("Please select file to Delete");
				return false;
			}
			if (confirm("do you want to Delete")){
				document.forms[0].method.value="deletemedicafile";
				document.forms[0].action="medicalfileuploadAction.do";
				document.forms[0].submit();
			} else {
				return false
			}
		}else{
			return false;
		}
	}
	function onKeyPressMrno(e) {
	if (isEventEnterOrTab(e)) {
		return onChangeMrno();
	} else {
		return true;
	}
}

function onChangeMrno() {
	var mrnoBox = medicalrecords.mrNo;

	// complete
	var valid = addPrefix(mrnoBox, gMrNoPrefix, gMrNoDigits);

	if (!valid) {
		alert("Invalid MR No. Format");
		medicalrecords.mrNo.value = ""
		medicalrecords.mrNo.focus();
		return false;
	}
	checkMrno();
	patientDetails();
}

</script>


</head>

<body class="setMargin yui-skin-sam"  onload="return selectMode();">
<%
request.getAttribute("doctypename");
%>
<form name="medicalrecords" action="medicalfileuploadAction.do"	method="POST" scope="session" enctype="multipart/form-data">
<input	type="hidden" name="method" value="MedicalFileInsert">
<input type="hidden" name="docid_test" id="docid_test" value=""/>
<table border="0" height="100%" width="100%" cellpadding="0" cellspacing="0" class="totalbg">
   <tr>
		<td valign="top">
		  <table border="0" height="100%" width="100%" cellpadding="0" cellspacing="0">

				   <tr>
					 <td class="totalBG" height="100%" valign="top" width="100%">
						<table  border="0" width="100%" cellpadding="0" cellspacing="0">
						  <tr>
						<!-- To maintain specific height from the outlet BEGIN -->
								<td colspan="2"></td>
						<!-- To maintain specific height from the outlet BEGIN -->
							</tr>
					<!-- To maintain specific width from the outlet BEGIN -->
								<td></td>
					<!-- To maintain specific width from the outlet BEGIN -->
								<td width="100%">
					<!--  Actual Design begins in below table -->
					<!--  DESIGN BEGIN  -->
							<table width="100%" border="0" cellpadding="0" cellspacing="0" class="form">
						        <!-- a spacer -->
						        <tr>
									<td width="100%" height="2%" class="topLineDownSpace">
									<span class="pageHeader">Scanned ReportsHello${output}</span>
								<tr>
									<td height="10"></td>
								</tr>
				               <tr><td align="center" class="resultMessage">
								<c:if test="${not empty requestScope.output}" >${output}&nbsp;</c:if>
								<c:if test="${not empty requestScope.filesgreaterthan8mb}">
									<c:if test="${not empty filesgreaterthan8mb}">
										<c:forEach var="filesgreaterthan8mb" items="${files}">
											<c:if test="${files.fileName != ''}">
												${files.fileName}-&nbsp;${files.fileSize},
											</c:if>
										</c:forEach>
										<br/>which exceed allowed limit of 10MB.
									</c:if>
								</c:if></td></tr>



								<tr height="10%" >
									<td>
										<table border="0">
											<tr>
												<td align="left" class="label" >Enter Registration No:</td><td><input type="text" name="mrNo" size="15" id="mrNo" class="text-input" onkeypress="onKeyPressMrno(event)" onblur="onChangeMrno()" >&nbsp;&nbsp;
												&nbsp;<a href="" class="label" onclick="javascript:window.open('../../pages/Common/PatientSearchPopup.do?mrnoForm=medicalrecords&mrnoField=mrNo&searchType=all','Search','width=655,scrollbars=yes, height=430, screenX=175,screenY=70,left=200,top=200,scrollbar=0,menubar=0');return false;">Search</a>
												</td>
											</tr>
											<tr height="15%"><td class="label" align=center>(or)</td></tr>

											<tr height="10%" >
												<td align="left" class="label" >Enter Old Registration No:</td><td><input type="text" name="oldMrNo" size="15" id="oldMrNo"  class="text-input" onkeyup="upperCase(oldMrNo);" onblur="checkOldMrno(this);patientDetails();"  onkeypress="return checkEnterKey(event);"  >&nbsp;&nbsp;
												&nbsp;<a href="" class="label"  onclick="javascript:window.open('<%= request.getContextPath()%>/pages/Common/MrnoSearchPopup.do?method=getMrnoSearchScreen&form=medicalrecords&field=oldMrNo&nextField=fromDate&index=1&screen=oldmrnoscreen','Search','width=655, height=430, screenX=175,screenY=70,left=200,top=200,scrollbar=0,menubar=0,resizeble=0');return false;">Search</a>
												</td>
											</tr>
											<tr height="15%"></tr>
											<tr height="10%" ><td align="left" class="label" align=center>Patient Name:</td><td><input type="text" name="patName" id="patName" readonly="readonly"/></td></tr>
										</table>
									</td>
								</tr>
								<tr><td>&nbsp;</td>
								</tr>
								<tr><td height="10"></td></tr>

								<tr>
								<td valign="top">
								<div id="singleUpdate" style="display: block">
								<fieldset class="forminput" style="width: 400; " >
	                              <legend class="fieldSetLabel"><b>
	                              	<span style="font-weight: 650">Scanned&nbsp;Reports
	                              	</span></b>
	                               </legend>
			 						<table border="0" width="100%" cellpadding="0" cellspacing="0" height="100">
										<tr>
											<td class="label">File Upload</td>
											<td id="fileTD">&nbsp;<input type="file" id="my_file_element" name="theFile[0]"  accept="<insta:ltext key="upload.accept.medical_image"/>,<insta:ltext key="upload.accept.document"/>"/></td>
										</tr>
										<tr>
											<td></td>
											<td class="fieldSetLabel"><b>Newly added Images:</b>
											<div id="files_list"></div>
											<script>
													<!-- Create an instance of the multiSelector class, pass it the output target and the max number of files -->
															var multi_selector = new MultiSelector(document.getElementById( 'files_list' ) );
														<!-- Pass in the file element -->
														multi_selector.addElement(document.getElementById( 'my_file_element' ) );
											</script></td>
										</tr>
										<tr>
											<td id="file_list_edit" colspan="4">&nbsp;</td>
										</tr>
									</table><!-- Innner table -->
							</fieldset>
							</div>
							<div id="updateFileDiv" style="display: none">
								<fieldset class="forminput" style="width: 400; " >
	                              <legend class="fieldSetLabel"><b>
	                              	<span style="font-weight: 650">Scanned&nbsp;Reports
	                              	</span></b>
	                               </legend>
			 						<table border="0" width="100%" cellspacing="0" height="100">
			 							<tr>
											<td class="label">File&nbsp;Upload&nbsp;:</td>
											<td ><input type="file" id="updateFile"
												name="updateFile" accept="<insta:ltext key="upload.accept.medical_image"/>,<insta:ltext key="upload.accept.document"/>"/><span class="star">*</span></td>
										</tr>
										<tr>
											<td class="label">Document&nbsp;Tags&nbsp;:</td>
											<td ><input type="text" name="docTag" id="docTag" maxlength="150"/><span class="star">*</span></td>
										</tr>
										<tr>
											<td class="label">Document&nbsp;Type&nbsp;:</td>
											<td ><select name="docType" id="docType" >
												<option value="NULLx">----select doctype---------</option>
												<c:forEach items="${documentlist}" var="documents" >
													<option value="${documents.DOC_TYPE_ID}">${documents.DOC_TYPE_NAME}</option>
												</c:forEach>
											</select><span class="star">*</span></td>
										</tr>
										<tr>
											<td class="label">Document&nbsp;Date&nbsp;:</td>
											<td ><insta:datewidget name="docDate" id="docDate" valid="past" calButton="true"/></td>
										</tr>
										<tr>
											<td colspan="2">
												<table id="visitIdTab" cellspacing="0">
													<tr>
														<td class="label"><input type="radio" name="radioVistid" value="OLD" onclick="return showVisit(this.value);">Visit&nbsp;ID&nbsp;:</td>
														<td><div id="oldvisitid" style="display: none"><select name="selExVisitId" id="selExVisitId">
															<option value="NULLx">-----select visit-----</option>
														</select></div></td>
													</tr>
													<tr>
														<td class="label" valign="top"><input type="radio" name="radioVistid" value="NEW" onclick="return showVisit(this.value);">New&nbsp;Visit&nbsp;ID&nbsp;:</td>
														<td><div id="newvisitid" style="display: none" valign="top">Visit&nbsp;ID&nbsp;<input type="text" name="txtNewVisitId" id="txtNewVisitId" onchange="checkForUniqueNo(this);"/><span class="star">*</span><br/>
															Visit&nbsp;Type&nbsp;<select name="visitType" id="visitType">
															<option value="NULLx">-----select visit type----</option>
															<option value="i">In Patient</option>
															<option value="o">Out Patient</option>
															</select><span class="star">*</span>&nbsp;&nbsp;<br/>
															Date&nbsp;<insta:datewidget name="visitDate" id="visitDate" valid="past" calButton="true"/><span class="star">*</span><br/>
														</div></td>
													</tr>
												</table>
											</td>
										</tr>
			 						</table>
			 					</fieldset>
							</div>
						</td>

						</tr>

						<!-- End Table for fields -->
						<!--  Menu option -->
						<tr height="">
							<td><br>
								<table >
									<tr align="center">
										<td width="100"></td>
										 <td><input type="button" value="Save"  id="save"  class="button" onclick="return validateOnSave();">&nbsp;&nbsp;&nbsp;</td>
										 <td><input type="button" value="View"   id="View"  class="button" onclick="return validateOnView();">&nbsp;&nbsp;&nbsp;<td>
										 <td><input type="button" value="Delete"  id="Delete"  class="button" onclick="return validateOnDelete();"></td>
									</tr>
								</table>
							</td>
						</tr>
						<tr>
						 <td height="20"></td>
						</tr>
						<tr >

							<td align="center">
						    <table  align="left" border='2' cellpadding='4' cellspacing='0' bordercolor='darkgreen'  style="border-collapse: collapse" id="avail_doc_table">
						    <tr>
						    	<th height="20" colspan="1" class="masterFrameLabel">Doc&nbsp;ID</th>
						    	<th height="20" colspan="1" class="masterFrameLabel">Doc&nbsp;Name</th>
						    	<th height="20" colspan="1" class="masterFrameLabel">Doc&nbsp;Type</th>
						    	<th height="20" colspan="1" class="masterFrameLabel">Doc&nbsp;Date</th>
						    	<th height="20" colspan="1" class="masterFrameLabel">Visit&nbsp;ID</th>
						    	<th height="20" colspan="1" class="masterFrameLabel">view&nbsp;Image</th>
						    </tr>
						    </table>
						</td>
						</tr>
						</table>
						<!--  DESIGN END  -->
					</td>
			</table>
			</td>
			</tr>

			<tr>
                <td  align="center" height="25" colspan="2" width="929" class="totalbg"><jsp:include page="../frame/footer.jsp" /></td>
            </tr>
			</table>
		  </td>
		  <td valign="middle">
	        	<div id="showImage" style="display: none; width: 500; height:500">
	        		<iframe name="leftNavigation" src="" height="100%" width="100%" frameborder="0"></iframe>
	        	</div>
	      </td>
	 </tr>
  </table>
  <%=request.getAttribute("arrmrnoXmlContent")%>
</form>
</body>
</html>

