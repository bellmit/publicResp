<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>SampleType Master</title>
	<insta:link type="script" file="hmsvalidation.js"/>
	<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>' />

<script>
	var backupName = '';
	var allSampleLists = '${allSampleLists}';
	var sampleID = '${ifn:cleanJavaScript(param.sample_type_id)}';
	var method = '${ifn:cleanJavaScript(param._method)}';
	var cpath = '${cpath}';
	function enterNineOrZeroOnly(e){
	   var key=0;
		if(window.event || !e.which)
		{
			key = e.keyCode;
	   	}
		else
		{
			key = e.which;
		}

	     if(((key==48)||(key==57))||key==8||key==9||key==45 || key==0)
	     {
	        key=key;
	        return true;
	     }
	     else
	     {
	       key=0;
	       return false;
	     }

	}


	function keepBackUp(){
		if(document.sampleForm._method.value == 'update'){
				backupName = document.sampleForm.sample_type.value;
		}
	}

	function validate() {
		var status = document.getElementById('status').value;
		var tableLength = document.getElementById('sampleNumPrefsTbl').rows.length;

		document.sampleForm.sample_type.value = trim(document.sampleForm.sample_type.value);
		if (document.sampleForm.sample_type.value=="") {
			alert("Sample Type Name is required");
			document.sampleForm.sample_type.focus();
			return false;
		}
		if (document.sampleForm.sample_type.value.length<4) {
			alert("Sample Type Name should be more than 3 characters");
			document.sampleForm.sample_type.focus();
			return false;
		}

		for (var i=0; i<tableLength-1; i++) {
			var numPattern = trim(document.getElementById('num_pattern'+i).value);
			var startNumber = trim(document.getElementById('start_number'+i).value);
			var centerName = document.getElementsByName('center_name')[i].value;

			document.getElementById('sample_prefix'+i).value = trim(document.getElementById('sample_prefix'+i).value);
			if (document.getElementById('sample_prefix'+i).value != '' || document.getElementById('num_pattern'+i).value != ''
					|| document.getElementById('start_number'+i).value != '' || document.getElementById('reset_freq'+i).value != ''
					|| document.getElementById('center_id'+i).value == 0) {
				if (document.getElementById('sample_prefix'+i).value=="") {
					alert("sample prefix is required for the center "+centerName);
					document.getElementById('sample_prefix'+i).focus();
					return false;
				}
				if (document.getElementById('num_pattern'+i).value=="") {
					alert("Please enter the number pattern for the center "+centerName);
					document.getElementById('num_pattern'+i).focus();
					return false;
				}
				if (document.getElementById('start_number'+i).value=="") {
					alert("start number is required for the center "+centerName);
					document.getElementById('start_number'+i).focus();
					return false;
				}
				if (document.getElementById('reset_freq'+i).value != '') {
					document.getElementById('date_prefix'+i).value = trim(document.getElementById('date_prefix'+i).value);
					if (document.getElementById('date_prefix'+i).value == '' ) {
						alert("Please enter the date prefix for the center "+centerName);
						document.getElementById('date_prefix'+i).focus();
						return false;
					}
					document.getElementById('date_prefix_pattern'+i).value = trim(document.getElementById('date_prefix_pattern'+i).value);
					if (document.getElementById('date_prefix_pattern'+i).value == '' ) {
						alert("Please enter the date prefix pattern for the center "+centerName);
						document.getElementById('date_prefix_pattern'+i).focus();
						return false;
					}
				}

				if (numPattern.length < startNumber.length) {
					alert('Number of digits for sample number pattern \n should not be less than the '+
					'number of digits of current start number for the center '+centerName);
					document.getElementById('num_pattern')[i].focus();
					return false;
				}

				if (!checkDuplicatePrefixes()) {
					return false;
				}
			}
		}

		if(method != 'add' && status == 'I') {
			var text = 	isTestNameexistsWithsampletype();
			if (text == 'true') {
				alert("Tests are exists with this sample type.\n Please update them before inactivating sample type");
				document.getElementById('status').focus();
				return false;
			}
		}
		document.sampleForm.submit();
	}

	function isTestNameexistsWithsampletype() {

		var url = cpath+"/master/SampleType.do?_method=isSampletypeExistsWithTest&sampleTypeId="+sampleID;

		if(window.XMLHttpRequest) {
			req = new XMLHttpRequest();
		}
		else if(window.ActiveXObject) {
			req = new ActiveXObject("MSXML2.XMLHTTP");
		}
			req.open("GET", url.toString(), false);
			req.setRequestHeader("Content-Type", "text/plain");
			req.send(null);
			if(req.readyState == 4 && req.status == 200) {
				return req.responseText;

			}
	}

	function loadlabelName(){
    	var tableLength = document.getElementById('sampleNumPrefsTbl').rows.length;
    	for (var i=0; i<tableLength-1; i++) {
		    if (document.sampleForm._method.value=="update" && document.getElementById('start_number'+i).value != ''){
		     //  document.getElementById('sNumber'+i).innerHTML="Current Start Number:";
		       document.getElementById('start_number'+i).readOnly=true;
		    } else {
		     //  document.getElementById('sNumber'+i).innerHTML="Start Number:";
		    }
		}
    }

    function doClose() {

    	window.location.href = "${cpath}/master/SampleType.do?_method=list&sortOrder=sample_type" +
    				"&sortReverse=false&status=A";
    }

    function checkDuplicatePrefixes() {
    	var inputSamplePrefix = '';
    	var sampleList = eval(allSampleLists);
    	var tableLength = document.getElementById('sampleNumPrefsTbl').rows.length;
		for (var j=0; j<tableLength-1; j++) {
			inputSamplePrefix = document.getElementById('sample_prefix'+j).value;
			sampleTypeNumPrefID = document.getElementById('sample_type_number_prefs_id'+j).value;
			var centerName = document.getElementsByName('center_name')[j].value;
	    	for(var i=0; i<sampleList.length; i++) {
	    		var record = sampleList[i];
	    		if(sampleTypeNumPrefID != record['sample_type_number_prefs_id']) {
	    			if(trim(inputSamplePrefix) == record['sample_prefix']) {
	    				alert('Duplicate prefix entered for the center '+centerName);
	    				document.getElementById('sample_prefix'+j).value = '';
	    				return false;
	    			}
	    		}
	    	}
	    }
    	return true;
    }

    <c:if test="${param._method != 'add'}">
		  Insta.masterData=${samplesLists};
	</c:if>

</script>
</head>
<body onload="loadlabelName();keepBackUp();">
<c:choose>
    <c:when test="${param._method != 'add'}">
         <h1 style="float:left">Edit Sample Type</h1>
         <c:url var="searchUrl" value="/master/SampleType.do"/>
         <insta:findbykey keys="sample_type,sample_type_id" fieldName="sample_type_id" method="show" url="${searchUrl}"/>
    </c:when>
    <c:otherwise>
         <h1>Add Sample Type</h1>
    </c:otherwise>
</c:choose>

<form action="SampleType.do" name="sampleForm" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="sample_type_id" id="sample_type_id" value="${bean.map.sample_type_id}"/>
		<input type="hidden" name="specimenName" value="${bean.map.sample_type}"/>
	</c:if>

	<insta:feedback-panel/>

	<fieldset class="fieldSetBorder">
	<table class="formtable" >
		<tr>
			<td class="formlabel">Sample Type Name:</td>
			<td>
				<input type="text" name="sample_type" value="${bean.map.sample_type}" >
			</td>
			<td class="formlabel">Status:</td>
			<td><insta:selectoptions name="status" id="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
			<td class="formlabel">Container:</td>
			<td>
				<input type="text" name="sample_container" value="${bean.map.sample_container}"	maxlength="50">
			</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td colspan="3" style="text-align: left">Sample Number Pattern:</td>
		</tr>
	</table>
	<div class="resultList">
	<table class="detailList"  id="sampleNumPrefsTbl">
		<tr>
			<th>Center</th>
			<th>Prefix</th>
			<th>Start Number</th>
			<th>Number Pattern</th>
			<th>Date Prefix</th>
			<th>Date Prefix Pattern</th>
			<th>Date Reset Frequency</th>
		</tr>
		<c:set var="sampleNumPrefsBeanForDefCenter" value="${centerWiseSampleNumPrefs[defaultCenterRec.map.center_id]}" />
		<tr>
			<td>
				${defaultCenterRec.map.center_name}
			</td>
			<td>
				<input type="text" name="sample_prefix" id="sample_prefix0" value="${sampleNumPrefsBeanForDefCenter.map.sample_prefix}" maxlength="5" >
			</td>
			<td>
				<input type="text" name="start_number" id="start_number0" value="${sampleNumPrefsBeanForDefCenter.map.start_number}"	onkeypress="return enterNumOnlyzeroToNine(event);" >
			</td>
			<td>
				<input type="text" name="num_pattern" id="num_pattern0" value="${sampleNumPrefsBeanForDefCenter.map.num_pattern}" onkeypress="return enterNineOrZeroOnly(event);" >
				<img class="imgHelpText" title="Please enter either zero or nine for the prefix pattern. No of digits should be equal or more than the current start number"
	 				src="${cpath}/images/help.png"/>
			</td>
			<td>
				<input type="text" name="date_prefix" id="date_prefix0" value="${sampleNumPrefsBeanForDefCenter.map.date_prefix}" >
			</td>
			<td>
				<input type="text" name="date_prefix_pattern" id="date_prefix_pattern0" value="${sampleNumPrefsBeanForDefCenter.map.date_prefix_pattern}" >
				<img class="imgHelpText" title="Valid patterns are DDMMYYYY, MMDDYYYY (or) YYYY"
	 				src="${cpath}/images/help.png"/>
			</td>
			<td>
				<insta:selectoptions name="reset_freq" id="reset_freq0" value="${sampleNumPrefsBeanForDefCenter.map.reset_freq}" opvalues="D,W,M,Y,F" optexts="Daily,Weekly,Monthly,Yearly,Fin. Yearly"
					dummyvalue="None" dummyvalueId=""/>
				<img class="imgHelpText" title="Frequency is used along with the date prefix." src="${cpath}/images/help.png"/>
			</td>
				<input type="hidden" name="sample_type_number_prefs_id" id="sample_type_number_prefs_id0" value="${sampleNumPrefsBeanForDefCenter.map.sample_type_number_prefs_id}" />
				<input type="hidden" name="center_id" id="center_id0" value="${defaultCenterRec.map.center_id}" />
				<input type="hidden" name="center_name" id="center_name0" value="${defaultCenterRec.map.center_name}" />
		</tr>
		<c:forEach items="${listOfCentersExcldDefault}" var="center" varStatus="idx">
			<c:set var="sampeNumPrefsForCenter" value="${centerWiseSampleNumPrefs[center.map.center_id]}" />
			<c:set var="index" value="${idx.index+1}" />
			<tr>
				<td>
					${center.map.center_name}
				</td>
				<td>
					<input type="text" name="sample_prefix" id="sample_prefix${index}" value="${sampeNumPrefsForCenter.map.sample_prefix}" maxlength="5" >
				</td>
				<td>
					<input type="text" name="start_number" id="start_number${index}" value="${sampeNumPrefsForCenter.map.start_number}"	onkeypress="return enterNumOnlyzeroToNine(event);" >
				</td>
				<td>
					<input type="text" name="num_pattern" id="num_pattern${index}" value="${sampeNumPrefsForCenter.map.num_pattern}" onkeypress="return enterNineOrZeroOnly(event);" >
					<img class="imgHelpText" title="Please enter either zero or nine for the prefix pattern. No of digits should be equal or more than the current start number"
		 				src="${cpath}/images/help.png"/>
				</td>
				<td>
					<input type="text" name="date_prefix" id="date_prefix${index}" value="${sampeNumPrefsForCenter.map.date_prefix}" >
				</td>
				<td>
					<input type="text" name="date_prefix_pattern" id="date_prefix_pattern${index}" value="${sampeNumPrefsForCenter.map.date_prefix_pattern}" >
					<img class="imgHelpText" title="Valid patterns are DDMMYYYY, MMDDYYYY (or) YYYY"
		 				src="${cpath}/images/help.png"/>
				</td>
				<td>
					<insta:selectoptions name="reset_freq" id="reset_freq${index}" value="${sampeNumPrefsForCenter.map.reset_freq}" opvalues="D,W,M,Y,F" optexts="Daily,Weekly,Monthly,Yearly,Fin. Yearly"
						dummyvalue="None" dummyvalueId=""/>
					<img class="imgHelpText" title="Frequency is used along with the date prefix." src="${cpath}/images/help.png"/>
				</td>
					<input type="hidden" name="sample_type_number_prefs_id" id="sample_type_number_prefs_id${index}" value="${sampeNumPrefsForCenter.map.sample_type_number_prefs_id}" />
					<input type="hidden" name="center_id" id="center_id${index}" value="${center.map.center_id}" />
					<input type="hidden" name="center_name" id="center_name${index}" value="${center.map.center_name}" />
			</tr>
		</c:forEach>
	</table>
	</div>
	</fieldset>

	<div class="screenActions" style="padding-top: 15px">
		<button type="button" accesskey="S" onclick="validate()"><b><u>S</u></b>ave</button>
		|
		<c:if test="${param._method != 'add'}">
			<a href="javascript:void(0)" onclick="window.location.href='${cpath}/master/SampleType.do?_method=add'">Add</a>
		|
		</c:if>
		<a href="javascript:void(0)" onclick="doClose();">Sample List</a>
	</div>

</form>

</body>
</html>
