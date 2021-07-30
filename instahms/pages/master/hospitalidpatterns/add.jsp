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
			window.location.href = "${cpath}/${pagePath}/list.htm?sortOrder=pattern_id" +
						"&sortReverse=false";
		}
		
       function setDatePrefixPattern() {
    	   var seqResetFreq = document.hospitalidpatternsform.sequence_reset_freq.value;
    	   var datePrefixPatternList = [];
    	   if (seqResetFreq == 'D') {
    		   datePrefixPatternList = ['DDMMYY','DDYYMM','MMDDYY','MMYYDD','YYMMDD','YYDDMM'];
    	   } else if (seqResetFreq == 'M') {
    		   datePrefixPatternList = ['MMYY','MMYYYY','YYMM','YYYYMM'];
    	   } else if (seqResetFreq == 'Y' || seqResetFreq == 'F') {
    		   datePrefixPatternList = ['YY','YYYY'];
    	   }
		   loadSelectBox(document.hospitalidpatternsform.date_prefix_pattern, datePrefixPatternList, null, null, 'Select');
		   document.getElementById("lbl_date_prefix").innerHTML = '';
		   document.getElementById("date_prefix").value = '';
		   setMandotoryspan();
       }
       
	   function allowOnlyAlphNUnderscore(e) {
			var c = getEventChar(e);
			return (isCharControl(c) || isCharAlpha(c) || (charunderScore == c) );
	   }
	   
   	</script>
</head>

<body>
 <h1><insta:ltext key="patient.sequences.hospitalidpattern.addhospitalidpattern"/></h1>
 <c:set var="actionUrl" value="${cpath}/${pagePath}/create.htm"/>
 
<form action="${actionUrl}"  name="hospitalidpatternsform" method="POST">
	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
	<table class="formtable" >
		<tr>
			<td class="formlabel"><insta:ltext key="patient.sequences.hospitalidpattern.patternid"/>:</td>
			<td>
				<input type="text" id="pattern_id" name="pattern_id" value=""  class="required validate-length"
					onkeypress="return allowOnlyAlphNUnderscore(event)" onblur="upperCase(this)"/>
				<span class="star">*</span>
			</td>
			<td class="formlabel"><insta:ltext key="patient.sequences.hospitalidpattern.stdprefix"/>:</td>
			<td>
				<input type="text" id="std_prefix" name="std_prefix" value=""  />
			</td>
			<td class="formlabel"><insta:ltext key="patient.sequences.hospitalidpattern.sequencename"/>:</td>
			<td>
				<input type="text" id="sequence_name" name="sequence_name" value="" onkeypress="return allowOnlyAlphNUnderscore(event)" />
				<span class="star">*</span>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.sequences.hospitalidpattern.seqresetfrequency"/>:</td>
			<td>
				<insta:selectoptions id="sequence_reset_freq" name="sequence_reset_freq" value="" opvalues="D,M,Y,F" 
					optexts="Daily,Monthly,Calender Year,Financial Year" dummyvalue="Select" dummyvalueId='' onchange="setDatePrefixPattern()"/>			
			</td>
			<td class="formlabel"><insta:ltext key="patient.sequences.hospitalidpattern.dateprefixpattern"/>:</td>
			<td>
				<select name="date_prefix_pattern" id="date_prefix_pattern" class="dropdown" onchange="setDatePrefix()" >
					<option value="">Select</option>
				</select>
				<span class="star" id="date_prefix_pattern_mandatory" style= display:none >&nbsp;*</span>
			</td>
			<td class="formlabel"><insta:ltext key="patient.sequences.hospitalidpattern.numpattern"/>:</td>
			<td>
				<input type="text" id="num_pattern" name="num_pattern" value="" onkeypress="return allowOnlyZeroAndNine(event)" style="width: 130px" />
				<span class="star">*</span>
				<img class="imgHelpText" 
					title="<insta:ltext key='patient.sequences.hospitalidpattern.numpatternhelptext'/>"
					src="${cpath}/images/help.png"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.sequences.hospitalidpattern.transactiontype"/>:</td>
			<td>

				<insta:selectoptions id="transaction_type" name="transaction_type" value="" opvalues="MRN,BLN,VID,PHB,REP,ACN,CID,GRN,PON,PVN,LAB,RAD" 
					optexts="MR No,Bill No,Visit Id,Pharmacy Sales Bill No,Receipt No,Audit Control No,Claim Id,GRN No,Purchase Order No,Voucher No,
							Lab Number, Radiology Number"
					 dummyvalue="Select" dummyvalueId=''/><span class="star">*</span>			
			</td>
			<td class="formlabel"><insta:ltext key="patient.sequences.hospitalidpattern.dateprefix"/>:</td>
			<td>
				<label id="lbl_date_prefix"></label>
				<input type="hidden" name="date_prefix" id ="date_prefix" value="">
			</td>
		</tr>
	</table>
	</fieldset>
	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validateHospitalIdPatternForm();"><b><u>S</u></b>ave</button>
		|
		<a href="javascript:void(0)" onclick="doClose();"><insta:ltext key="patient.sequences.hospitalidpattern.hospitalidpatternlist"/></a>
	</div>
</form>
</body>
</html>
