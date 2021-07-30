<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page import="com.insta.hms.master.URLRoute"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="pagePath" value="<%=URLRoute.HOSPITAL_ID_PATTERNS_PATH %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Hospital Id Patterns - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="master/sequences/hospitalIdPatterns.js"/>
	<script>
		
		function doClose() {
			window.location.href = "${cpath}/${pagePath}/list.htm?sortOrder=pattern_id"
					+ "&sortReverse=false";
		}

		function setDatePrefixPattern(callType) {
			var seqResetFreq = document.hospitalidpatternsform.sequence_reset_freq.value;
			var datePrefixPattern = '';
			if (callType != 'onchange') {
				datePrefixPattern = document.getElementById("date_prefix_pattern").value;
			}
			var datePrefixPatternList = [];
			if (seqResetFreq == 'D') {
				datePrefixPatternList = [ 'DDMMYY', 'DDYYMM', 'MMDDYY',
						'MMYYDD', 'YYMMDD', 'YYDDMM' ];
			} else if (seqResetFreq == 'M') {
				datePrefixPatternList = [ 'MMYY', 'MMYYYY', 'YYMM', 'YYYYMM' ];
			} else if (seqResetFreq == 'Y' || seqResetFreq == 'F') {
				datePrefixPatternList = [ 'YY', 'YYYY' ];
			}
			loadSelectBox(document.hospitalidpatternsform.date_prefix_pattern,
					datePrefixPatternList, null, null, 'Select');
			
			setSelectedIndex(document.hospitalidpatternsform.date_prefix_pattern, datePrefixPattern);
			
			if (callType == 'onchange') {
				document.getElementById("lbl_date_prefix").innerHTML = '';
				document.getElementById("date_prefix").value = '';
				setMandotoryspan();
			}
		}
		
	</script>
</head>

<body onload="setDatePrefixPattern();">
 <h1><insta:ltext key="patient.sequences.hospitalidpattern.edithospitalidpattern"/></h1>
 <c:set var="actionUrl" value="${cpath}/${pagePath}/update.htm"/>
 
<form action="${actionUrl}"  name="hospitalidpatternsform" method="POST">
	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
	<table class="formtable" >
		<tr>
			<td class="formlabel"><insta:ltext key="patient.sequences.hospitalidpattern.patternid"/>:</td>
			<td>
				<input type="text" id="pattern_id" name="pattern_id" value="${bean.pattern_id}" readonly/>
				<span class="star">*</span>
			</td>
			<td class="formlabel"><insta:ltext key="patient.sequences.hospitalidpattern.stdprefix"/>:</td>
			<td>
				<input type="text" id="std_prefix" name="std_prefix" value="${bean.std_prefix}" />
			</td>
			<td class="formlabel"><insta:ltext key="patient.sequences.hospitalidpattern.sequencename"/>:</td>
			<td>
				<input type="text" id="sequence_name" name="sequence_name" value="${bean.sequence_name}" readonly/>
				<span class="star">*</span>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.sequences.hospitalidpattern.seqresetfrequency"/>:</td>
			<td>
				<insta:selectoptions id="sequence_reset_freq" name="sequence_reset_freq" value="${bean.sequence_reset_freq}" opvalues="D,M,Y,F" 
					optexts="Daily,Monthly,Calender Year,Financial Year" dummyvalue="Select" dummyvalueId=''  onchange="setDatePrefixPattern('onchange')" />			
			</td>
			<td class="formlabel"><insta:ltext key="patient.sequences.hospitalidpattern.dateprefixpattern"/>:</td>
			<td>
				<select name="date_prefix_pattern" id="date_prefix_pattern" class="dropdown" onchange="setDatePrefix()" >
					<option value="${bean.date_prefix_pattern}" selected>${bean.date_prefix_pattern}</option>
				</select>
				<span class="star" id="date_prefix_pattern_mandatory" style= display:${bean.date_prefix_pattern != '' ? 'inline-block' :'none'}>&nbsp;*</span>
			</td>
			<td class="formlabel"><insta:ltext key="patient.sequences.hospitalidpattern.numpattern"/>:</td>
			<td>
				<input type="text" id="num_pattern" name="num_pattern" value="${bean.num_pattern}" onkeypress="return allowOnlyZeroAndNine(event)" style="width: 130px" />
				<span class="star">*</span>
				<img class="imgHelpText" 
					title="<insta:ltext key='patient.sequences.hospitalidpattern.numpatternhelptext'/>"
					src="${cpath}/images/help.png"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.sequences.hospitalidpattern.transactiontype"/>:</td>
			<td>
				<insta:selectoptions id="transaction_type" name="transaction_type" value="${bean.transaction_type}" opvalues="MRN,BLN,VID,PHB,REP,ACN,CID,GRN,PON,PVN,LAB,RAD" 
					optexts="MR No,Bill No,Visit Id,Pharmacy Sales Bill No,Receipt No,Audit Control No,Claim Id,GRN No,Purchase Order No,Voucher No,
							Lab Number, Radiology Number"
					 dummyvalue="Select" dummyvalueId='' onchange="setDatePrefixPattern()"/>
					 <span class="star">*</span>			
			</td>
			<td class="formlabel"><insta:ltext key="patient.sequences.hospitalidpattern.dateprefix"/>:</td>
			<td>
				<label id="lbl_date_prefix">${bean.date_prefix}</label>
				<input type="hidden" name="date_prefix" id ="date_prefix" value="${bean.date_prefix}">
			</td>
		</tr>
	</table>
	</fieldset>
	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validateHospitalIdPatternForm();"><b><u>S</u></b>ave</button>
		&nbsp;|&nbsp;
		<a href="${cpath}/${pagePath}/add.htm">Add</a>
		&nbsp;|&nbsp;
		<a href="javascript:void(0)" onclick="doClose();"><insta:ltext key="patient.sequences.hospitalidpattern.hospitalidpatternlist"/></a>
	</div>
</form>
</body>
</html>
