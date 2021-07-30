<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta name="i18nSupport" content="true"/>
	<title><insta:ltext key="clinicaldata.scorecard.show.scorecard"/></title>
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="js" file="dialysis/prescriptions.js" />
	<insta:link type="js" file="instaautocomplete.js" />
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
<style type="text/css">
	.scrolForContainer .yui-ac-content {
		max-height:18em;
		overflow:auto;
		overflow-x:auto;
		_height:18em;
		max-width:30em;
		width:30em;
		}

		.diagnosis_overlay {
		border:1px dotted #000;
		background-color:FFCC99;
		}

		div#container {
		width:500px;
		margin:0 auto;
		}

		p {
		margin:0 0 1.7em;
		}

		div.resultTable {
		overflow:auto;
		width:953px;
		}

		table.resultTable {
		border:1px #e6e6e6 solid;
		empty-cells:show;
		font-weight:600;
		width:100%;
		}

		table.resultTable input[type="text"] {
		font-weight:600;
		width:65px;
		}

		table.resultTable td {
		border-top:1px #e6e6e6 solid;
		border-bottom:none;
		border-right:1px #e6e6e6 solid;
		text-align:left;
		height:50px;
		cursor:pointer;
		padding:0 5px;
		color: #000000;
		}

		table.resultTable input{
			color: #000000;
		}

		table.resultTable td.blue {
		border-top:1px #e6e6e6 solid;
		border-bottom:none;
		border-right:1px #e6e6e6 solid;
		text-align:left;
		height:50px;
		background:#4592e4;
		font-weight:600;
		cursor:auto;
		padding:0 5px;
		}

		input.blue {
		background:#4592e4;
		}

		table.resultTable td.lightblue {
		border-top:1px #e6e6e6 solid;
		border-bottom:none;
		border-right:1px #e6e6e6 solid;
		text-align:left;
		height:50px;
		background:#bedce5;
		font-weight:700;
		cursor:auto;
		padding:0 5px;
		}

		input.lightblue {
		background:#bedce5;
		}

		table.resultTable td.darkgreen {
		border-top:1px #e6e6e6 solid;
		border-bottom:none;
		border-right:1px #e6e6e6 solid;
		text-align:center;
		height:50px;
		background:#00b456;
		font-weight:700;
		cursor:auto;
		padding:0 5px;
		}

		input.darkgreen {
		background:#00b456;
		}

		table.resultTable td.green {
		border-top:1px #e6e6e6 solid;
		border-bottom:none;
		border-right:1px #e6e6e6 solid;
		text-align:left;
		height:50px;
		background:#c3f94a;
		font-weight:700;
		padding:0 5px;
		}

		input.green {
		background:#c3f94a;
		}

		table.resultTable td.yellow {
		border-top:1px #e6e6e6 solid;
		border-bottom:none;
		border-right:1px #e6e6e6 solid;
		text-align:left;
		height:50px;
		background:#ff0;
		font-weight:700;
		padding:0 5px;
		}

		input.yellow {
		background:#ff0;
		}

		table.resultTable td.red {
		border-top:1px #e6e6e6 solid;
		border-bottom:none;
		border-right:1px #e6e6e6 solid;
		text-align:left;
		height:50px;
		background:red;
		font-weight:700;
		padding:0 5px;
		}

		input.red {
		background:red;
		}

		table.resultTable th {
		text-align:left;
		margin-top:1px;
		color:#333;
		background-image:url(../images/clmn_hd_bg.jpg);
		border-bottom:none;
		border-right:none;
		height:26px;
		font-weight:400;
		padding:0 5px;
		}

		table.resultTable tr:hover td.noHilight {
		background-color:#f2f5f9;
		}

		#grid th {
		background:#565656;
		color:#fff;
		}


		#grid tr:nth-child(2n) {
		background:#F7F7F7;
		}

		#status {
		background:#565656;
		border-top:1px solid #CECECE;
		box-shadow:0 0 8px rgba(0,0,0,.2);
		color:#fff;
		font-size:2em;
		position:fixed;
		left:0;
		bottom:0;
		width:100%;
		padding:10px;
		}

		#contextMenu {
		background:#F9F9F9;
		box-shadow:0 0 12px rgba(0,0,0,.3);
		border:1px solid #ccc;
		display:none;
		position:absolute;
		top:0;
		left:0;
		list-style:none;
		min-width:100px;
		margin:0;
		padding:0;
		}

		#contextMenu li {
		position:relative;
		}

		#contextMenu li:hover {
		background-color:#E4EBF3;
		color:#333;
		}

		#contextMenu a {
		color:#444;
		display:inline-block;
		text-decoration:none;
		width:85%;
		padding:8px;
		}

		#contextMenu li:hover a {
		color:#f9f9f9;
		background:#666;
		opacity:0.2;
		}

		table.resultTable td.indent,table.resultTable tr#toolbarRow0 td {
		border-top:none;
		}

		table.resultTable td.number,table.resultTable th.number {
		text-align:right;
		}

</style>

<insta:js-bundle prefix="clinicaldata.scorecard"/>
<insta:js-bundle prefix="clinicaldata.commonvalidations"/>
<insta:js-bundle prefix="clinicaldata.common"/>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="amendValues" value="${(roleId le 2) || actionRightsMap['amend_score_card_values'] eq 'A'}"/>

<body onload="init();" class="yui-skin-sam" style="text-align:center;">
<c:set var="getText"><insta:ltext key="clinicaldata.common.addoredit.get"/></c:set>
<c:set var="updateText"><insta:ltext key="clinicaldata.common.addoredit.update"/></c:set>
<div class="pageHeader">${param._method == 'add' ?getText:updateText} <insta:ltext key="clinicaldata.scorecard.show.dialysisscorecard"/></div>
<insta:feedback-panel/>
<form name="searchForm" method="GET" action="${cpath}/clinical/ScoreCard.do">

<input type="hidden" name="_method" value="show">
<div class="searchBasicOpts"  style="display:${param._method=='edit'|| param._method=='save'?'none':'block'}">
	<fieldset style="width:800px">
		<table class="search" style="padding-bottom: 12px">
			<tr>
				<td style="vertical-align:middle;"><insta:ltext key="clinicaldata.scorecard.show.mrno.name"/>:</td>
				<td style="padding-top:10px;">
					<div id="mrnoAutoComplete">
						<input type="text"  id="mr_no" name="mr_no" style="width: 8em" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
						<div id="mrnoAcDropdown" style="width: 34em"></div>
					</div>
				</td>
				<td style="padding-left:200px;vertical-align:middle;" ><insta:ltext key="clinicaldata.scorecard.show.month"/>:</td>
				<td>
					<div >
						<table cellpadding=0 cellspacing=0 border=0>
							<tr>
								<td colspan=7>
										<select name="month" id="month" class="dropDown">
										<option value="1"><insta:ltext key="clinicaldata.scorecard.show.jan"/></option>
										<option value="2"><insta:ltext key="clinicaldata.scorecard.show.feb"/></option>
										<option value="3"><insta:ltext key="clinicaldata.scorecard.show.mar"/></option>
										<option value="4"><insta:ltext key="clinicaldata.scorecard.show.apr"/></option>
										<option value="5"><insta:ltext key="clinicaldata.scorecard.show.may"/></option>
										<option value="6"><insta:ltext key="clinicaldata.scorecard.show.jun"/></option>
										<option value="7"><insta:ltext key="clinicaldata.scorecard.show.jul"/></option>
										<option value="8"><insta:ltext key="clinicaldata.scorecard.show.aug"/></option>
										<option value="9"><insta:ltext key="clinicaldata.scorecard.show.sep"/></option>
										<option value="10"><insta:ltext key="clinicaldata.scorecard.show.oct"/></option>
										<option value="11"><insta:ltext key="clinicaldata.scorecard.show.nov"/></option>
										<option value="12"><insta:ltext key="clinicaldata.scorecard.show.dec"/></option>
										</select>
									<input name="year" id="year" type=text size="20px" maxlength=4  style="width:50px;" onkeypress="return enterNumOnlyANDhypen(event);"/>
									<span id="popcal"><button type="button" name="getDetails" value="Get Details" id="getDetails" onClick="validateAndFetchMrnoDetails();"><insta:ltext key="clinicaldata.scorecard.show.getdetails"/></button></span>
								</td>
							</tr>
						</table>
					</div>
				</td>
			</tr>
		</table>
	</fieldset>
</div>


</form>
</font>

<insta:patientgeneraldetails  mrno="${param.mr_no}" addExtraFields="true" showClinicalInfo="true" />
<form name="detailsForm" method="GET" action="${cpath}/clinical/ScoreCard.do">
<jsp:useBean id="monthArray" class="java.util.HashMap"/>
<c:set target="${monthArray}" property="1" value="January"/>
<c:set target="${monthArray}" property="2" value="February"/>
<c:set target="${monthArray}" property="3" value="March"/>
<c:set target="${monthArray}" property="4" value="April"/>
<c:set target="${monthArray}" property="5" value="May"/>
<c:set target="${monthArray}" property="6" value="June"/>
<c:set target="${monthArray}" property="7" value="July"/>
<c:set target="${monthArray}" property="8" value="August"/>
<c:set target="${monthArray}" property="9" value="September"/>
<c:set target="${monthArray}" property="10" value="October"/>
<c:set target="${monthArray}" property="11" value="November"/>
<c:set target="${monthArray}" property="12" value="December"/>

<c:set var="monthNum">${empty card_month? param.month: card_month}</c:set>
<c:set var="yearNum">${empty card_year? param.year: card_year}</c:set>

<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(param._method)}">
<input type="hidden" name="month" value="${monthNum}">
<input type="hidden" name="year" value="${yearNum}">
<input type="hidden" name="score_card_id" value="${ifn:cleanHtmlAttribute(param.score_card_id)}">
<input type="hidden" name="locn" id="locn" value="${cpath}">
	<div id="cardDiv" style="display:${param._method=='show' || param._method=='edit'|| param._method=='save'?'block':'none'}">
		<table id="grid" class="resultTable" style="width:816px">
			<tr id="1">
				<td class="blue" >
					<insta:ltext key="clinicaldata.scorecard.show.patientname"/>
				</td>
				<td class="blue">
					${patMap.full_name}
					<input type="hidden" name="patname" value="${patMap.full_name}" />
				</td>

				<td class="blue">
					${monthArray[monthNum]} ${yearNum}
					<input type="hidden" name="monthyear" value="${monthArray[monthNum]}  ${yearNum}" />
				</td>
				<td class="blue">
					<insta:ltext key="clinicaldata.scorecard.show.mrno"/>${patMap.mr_no}
				</td>

				<td class="blue">
					<insta:ltext key="clinicaldata.scorecard.show.age.gender"/>${patMap.age_text} /${patMap.patient_gender}
					<input type="hidden" name="ageGender" value="${patMap.age_text} /${patMap.patient_gender}" />
				</td>
			</tr>

			<tr id="2">
				<td class="blue">
					<insta:ltext key="clinicaldata.scorecard.show.dialysisadequacy"/>
				</td>
				<td id="ktvRow" class="${sevMap.ktvSeverity}" title="Click to change severity">
					<insta:ltext key="clinicaldata.scorecard.show.kt.v"/> = <input type="text"  ${amendValues? ' ': 'readOnly'}  class="${sevMap.ktvSeverity}" width="20px"  id="ktv" name="ktv"  value="${valMap.ktv}" />
					<input type="hidden" value="${sevMap.ktvSeverity}" name="ktvSeverity" id="ktvSeverity"/>
					<span id="ktvMarker" style="vertical-align: top;  position:relative; top:-10; right:0;">${sevMap.ktvSeverity eq 'red'?'**':sevMap.ktvSeverity eq 'yellow'?'*':''}</span>
				</td>

				<td  id="urrRow" class="${sevMap.urrSeverity}" title="Click to change severity">
					<insta:ltext key="clinicaldata.scorecard.show.urr"/> = <input type="text"  ${amendValues? ' ': 'readOnly'}  class="${sevMap.urrSeverity}" width="20px"  id="urr" name="urr" value="${valMap.urr}"/>
					<input type="hidden" value="${sevMap.urrSeverity}" name="urrSeverity" id="urrSeverity"/>
					<span id="urrMarker" style="vertical-align: top;  position:relative; top:-10; right:0;">${sevMap.urrSeverity eq 'red'?'**':sevMap.urrSeverity eq 'yellow'?'*':''}</span>
				</td>
				<td id="bloodRow" class="${sevMap.bloodSeverity}" title="Click to change severity">
					<insta:ltext key="clinicaldata.scorecard.show.bloodflowqb"/> = <input type="text"  ${amendValues? ' ': 'readOnly'}  name="blood" id="blood" class="${sevMap.bloodSeverity}" width="20px"   value="${valMap.blood}"/>ml/min
					<input type="hidden" value="${sevMap.bloodSeverity}" name="bloodSeverity" id="bloodSeverity" />
					<span id="bloodMarker" style="vertical-align: top;  position:relative; top:-20; right:0;">${sevMap.bloodSeverity eq 'red'?'**':sevMap.bloodSeverity eq 'yellow'?'*':''}</span>
				</td>

				<td class="blue">
				</td>
			</tr>

			<tr id="3">
				<td class="blue" style="border-top-width: 0px;">
					&nbsp;
				</td>
				<td class="darkgreen">
					<input type="text"  ${amendValues? ' ': 'readOnly'}  class="darkgreen" value="${empty rangeMap.ktvRange?'>1.2' : rangeMap.ktvRange}" style="width:95px;" name="ktvRange" />
				</td>

				<td class="darkgreen">
					<input type="text"  ${amendValues? ' ': 'readOnly'}  class="darkgreen" value="${empty rangeMap.urrRange? '>65%' : rangeMap.urrRange}" style="width:95px;" name="urrRange" />
				</td>
				<td class="darkgreen">
					<input type="text"  ${amendValues? ' ': 'readOnly'}  class="darkgreen" value="${empty rangeMap.bloodRange? '>250 ml/Min' : rangeMap.bloodRange}" style="width:95px;" name="bloodRange" />
				</td>
				<td class="darkgreen">
					<insta:ltext key="clinicaldata.scorecard.show.target"/>
				</td>
			</tr>

			<tr id="4">
				<td  class="blue">
				<insta:ltext key="clinicaldata.scorecard.show.anemiaandfluidmanagement"/>
				</td>
				<td  id="hbRow" class="${sevMap.hbSeverity}" title="Click to change severity">
					<insta:ltext key="clinicaldata.scorecard.show.hb"/> = <input type="text"  ${amendValues? ' ': 'readOnly'}  class="${sevMap.hbSeverity}" width="20px"  name="hb" id="hb" value="${valMap.hb}"/>
					<input type="hidden" value="${sevMap.hbSeverity}" name="hbSeverity" id="hbSeverity"/>
					<span id="hbMarker" style="vertical-align: top;  position:relative; top:-10; right:0;">${sevMap.hbSeverity eq 'red'?'**':sevMap.hbSeverity eq 'yellow'?'*':''}</span>
				</td>
				<td id="weightRow" class="${sevMap.weightSeverity}" title="Click to change severity">
					<insta:ltext key="clinicaldata.scorecard.show.weightgain"/> = <input type="text"  ${amendValues? ' ': 'readOnly'}  class="${sevMap.weightSeverity}" width="20px"  name="weight" id="weight" value="${valMap.weight}"/>
					<input type="hidden" value="${sevMap.weightSeverity}" name="weightSeverity" id="weightSeverity"/>
					<span id="weightMarker" style="vertical-align: top;  position:relative; top:-10; right:0;">${sevMap.weightSeverity eq 'red'?'**':sevMap.weightSeverity eq 'yellow'?'*':''}</span>
				</td>
				<td id="bpRow" class="${sevMap.bpSeverity}" title="Click to change severity">
					<insta:ltext key="clinicaldata.scorecard.show.postbloodpressure"/> = <input type="text"  ${amendValues? ' ': 'readOnly'}  class="${sevMap.bpSeverity}" width="20px"  name="bp" id="bp" value="${valMap.bp}"/>
					<input type="hidden" value="${sevMap.bpSeverity}" name="bpSeverity" id="bpSeverity" />
					<span id="bpMarker" style="vertical-align: top;  position:relative; top:-20; right:-70;">${sevMap.bpSeverity eq 'red'?'**':sevMap.bpSeverity eq 'yellow'?'*':''}</span>
				</td>
				<td class="blue">
				</td>
			</tr>

			<tr id="5">
				<td  class="blue" style="border-top-width: 0px;">
				</td>
				<td class="darkgreen">
					<input type="text"  ${amendValues? ' ': 'readOnly'}  class="darkgreen" value="${empty rangeMap.hbRange? '11 F, 12 M' : rangeMap.hbRange}" style="width:95px;" name="hbRange" />
				</td>

				<td class="darkgreen">
					<input type="text"  ${amendValues? ' ': 'readOnly'}  class="darkgreen" value="${empty rangeMap.weightRange? '< 5% Body Weight' : rangeMap.weightRange}" style="width:95px;" name="weightRange" />
				</td>
				<td class="darkgreen">
					<input type="text"  ${amendValues? ' ': 'readOnly'}  class="darkgreen" value="${empty rangeMap.bpRange? '' : rangeMap.bpRange}"  style="width:95px;" name="bpRange" />
				</td>
				<td class="darkgreen">
					<insta:ltext key="clinicaldata.scorecard.show.target"/>
				</td>
			</tr>

			<tr id="6">
				<td class="blue">
	 				<insta:ltext key="clinicaldata.scorecard.show.nutrition"/>
				</td>
				<td id="albuminRow" class="${sevMap.albuminSeverity}" title="Click to change severity">
					 <insta:ltext key="clinicaldata.scorecard.show.albumin"/> = <input type="text"  ${amendValues? ' ': 'readOnly'}  class="${sevMap.albuminSeverity}" width="20px"  name="albumin" id="albumin" value="${valMap.albumin}"/>
					 <input type="hidden" value="${sevMap.albuminSeverity}" name="albuminSeverity" id="albuminSeverity"/>
					 <span id="albuminMarker" style="vertical-align: top;  position:relative; top:-10; right:0;"> ${sevMap.albuminSeverity eq 'red'?'**':sevMap.albuminSeverity eq 'yellow'?'*':''}</span>
				</td>

				<td id="proteinRow" class="${sevMap.proteinSeverity}" title="Click to change severity">
					 <insta:ltext key="clinicaldata.scorecard.show.totalprotein"/> = <input type="text"  ${amendValues? ' ': 'readOnly'}  class="${sevMap.proteinSeverity}" width="20px"  name="protein"  id="protein" value="${valMap.protein}"/>
					  <input type="hidden" value="${sevMap.proteinSeverity}" name="proteinSeverity" id="proteinSeverity"/>
					  <span id="proteinMarker" style="vertical-align: top;  position:relative; top:-10; right:0;">${sevMap.proteinSeverity eq 'red'?'**':sevMap.proteinSeverity eq 'yellow'?'*':''}</span>
				</td>
				<td id="potassiumRow" class="${sevMap.potassiumSeverity}" title="Click to change severity">
					<insta:ltext key="clinicaldata.scorecard.show.potassium"/> = <input type="text"  ${amendValues? ' ': 'readOnly'}  class="${sevMap.potassiumSeverity}" width="20px"  name="potassium" id="potassium" value="${valMap.potassium}"/>
					<input type="hidden" value="${sevMap.potassiumSeverity}" name="potassiumSeverity" id="potassiumSeverity"/>
					<span id="potassiumMarker" style="vertical-align: top;  position:relative; top:-10; right:0;">${sevMap.potassiumSeverity eq 'red'?'**':sevMap.potassiumSeverity eq 'yellow'?'*':''}</span>
				</td>
				<td class="blue">
				</td>
			</tr>

			<tr id="7">
				<td  class="blue" style="border-top-width: 0px;">


				</td>
				<td class="darkgreen">
					 <input type="text"  ${amendValues? ' ': 'readOnly'}  class="darkgreen" value="${empty rangeMap.albuminRange? '>4.0g/dl' : rangeMap.albuminRange}" style="width:95px;" name="albuminRange" />
				</td>

				<td class="darkgreen">
					 <input type="text"  ${amendValues? ' ': 'readOnly'}  class="darkgreen" value="${empty rangeMap.proteinRange? '>6.0' : rangeMap.proteinRange}" style="width:95px;" name="proteinRange" />
				</td>

				<td class="darkgreen">
					 <input type="text"  ${amendValues? ' ': 'readOnly'}  class="darkgreen" value="${empty rangeMap.potassiumRange? '3.5-5.5 mEg/L' : rangeMap.potassiumRange}" style="width:95px;" name="potassiumRange" />
				</td>

				<td class="darkgreen">
					<insta:ltext key="clinicaldata.scorecard.show.target"/>
				</td>
			</tr>
			<tr id="8">
				<td   class="blue">
					<insta:ltext key="clinicaldata.scorecard.show.bonemanagement"/>
				</td>

				<td id="caxpoRow" class="${sevMap.caxpoSeverity}" title="Click to change severity">
					<insta:ltext key="clinicaldata.scorecard.show.caxpo4"/> = <input type="text"  ${amendValues? ' ': 'readOnly'}  class="${sevMap.caxpoSeverity}" width="20px"  name="caxpo" id="caxpo" value="${valMap.caxpo}"/>
					<input type="hidden" value="${sevMap.caxpoSeverity}" name="caxpoSeverity" id="caxpoSeverity"/>
					<span id="caxpoMarker" style="vertical-align: top;  position:relative; top:-10; right:0;">${sevMap.caxpoSeverity eq 'red'?'**':sevMap.caxpoSeverity eq 'yellow'?'*':''}</span>
				</td>

				<td id="pthRow" class="${sevMap.pthSeverity}" title="Click to change severity">
					<insta:ltext key="clinicaldata.scorecard.show.pth"/> = <input type="text"  ${amendValues? ' ': 'readOnly'}  class="${sevMap.pthSeverity}" width="20px"  name="pth" id="pth" value="${valMap.pth}"/>
					<input type="hidden" value="${sevMap.pthSeverity}" name="pthSeverity" id="pthSeverity"/>
					<span id="pthMarker" style="vertical-align: top;  position:relative; top:-10; right:0;">${sevMap.pthSeverity eq 'red'?'**':sevMap.pthSeverity eq 'yellow'?'*':''}</span>
				</td>
				<td id="caRow" class="${sevMap.caSeverity}" title="Click to change severity">
					<insta:ltext key="clinicaldata.scorecard.show.correctedca.plus"/> = <input type="text"  ${amendValues? ' ': 'readOnly'}  class="${sevMap.caSeverity}" width="20px"  name="ca" id="ca" value="${valMap.ca}"/>
					<input type="hidden" value="${sevMap.caSeverity}" name="caSeverity" id="caSeverity"/>
					<span id="caMarker" style="vertical-align: top;  position:relative; top:-30; right:-160;">${sevMap.caSeverity eq 'red'?'**':sevMap.caSeverity eq 'yellow'?'*':''}</span>
				</td>

				<td class="blue">
				</td>
			</tr>

			<tr id="9" >
				<td  class="blue" style="border-top-width: 0px;">
				 &nbsp;
				</td>

				<td  class="darkgreen">
					<input type="text"  ${amendValues? ' ': 'readOnly'}  class="darkgreen" value="${empty rangeMap.caxpoRange? '&lt; 55mg2/dL2' : rangeMap.caxpoRange}" style="width:95px;" name="caxpoRange" />
				</td>

				<td  class="darkgreen">
					<input type="text"  ${amendValues? ' ': 'readOnly'}  class="darkgreen" value="${empty rangeMap.pthRange? '150-300 pg/ml' : rangeMap.pthRange}" style="width:95px;" name="pthRange" />
				</td>

				<td  class="darkgreen">
					<input type="text"  ${amendValues? ' ': 'readOnly'}  class="darkgreen" value="${empty rangeMap.caRange? '8.8 - 9.5 mg/dl' : rangeMap.caRange}" style="width:95px;" name="caRange" />
				</td>

				<td  class="darkgreen">
					<insta:ltext key="clinicaldata.scorecard.show.target"/>
				</td>
			</tr>
			<tr>
				<td colspan="1" class="lightblue" style="text-align:right;border-right:none;">
				<insta:ltext key="clinicaldata.scorecard.show.nephrologist"/>:
				</td>
				<td class="lightblue" style="text-align:left;border-right:none;">
					<input type="text"  style="width: 100px; background: none repeat scroll 0% 0% #bedce5;" value="${empty nephrologist? param.nephrologist:nephrologist}" name="nephrologist" id="nephrologist"/>
				</td>
				<td colspan="1"  class="lightblue" style="text-align:right;border-right:none;">
					<insta:ltext key="clinicaldata.scorecard.show.patient.attender"/>:
				</td>
				<td class="lightblue" colspan="2">
				</td>
			</tr>
	</table>
	<ul id="contextMenu">
		<li style="height:24px;padding-top:5px;background:#E4EBF3;text-align:center;" disabled="disabled"><insta:ltext key="clinicaldata.scorecard.show.changeseverity"/></li>
		<li id="1g" style="text-align:center;background: #c3f94a;" onclick="setColor('Achieved');" ><a href="#" onclick="setColor('Achieved');"><insta:ltext key="clinicaldata.scorecard.show.achieved"/></a></li>
		<li id="2r" style="text-align:center;background: #ff0000;" onclick="setColor('Higher');" ><a href="#"  onclick="setColor('Higher');"><insta:ltext key="clinicaldata.scorecard.show.higher"/></a></li>
		<li id="3y" style="text-align:center;background: #ffff00;" onclick="setColor('BorderLine');"><a href="#"  onclick="setColor('BorderLine');"><insta:ltext key="clinicaldata.scorecard.show.borderline"/></a></li>
	</ul>
	<div class="screenActions">
		<table align="left">
			<tr>
				<td>
					<button type="button" value="save" name="saveButton" onclick="return validateAndSave()" accesskey="S"><b><insta:ltext key="clinicaldata.scorecard.show.s"/></b><insta:ltext key="clinicaldata.scorecard.show.ave"/></button>
				</td>
				<td>
					&nbsp;
					<button type="button" value="print" name="printButton" onclick="return validateAndPrint()" accesskey="P"><b><insta:ltext key="clinicaldata.scorecard.show.p"/></b><insta:ltext key="clinicaldata.scorecard.show.rint"/></button>
				</td>
				<td>
					|<a href="${cpath}/clinical/ScoreCardList.do?_method=list&mr_no=${ifn:cleanURL(param.mr_no)}"><insta:ltext key="clinicaldata.scorecard.show.scorecardlist"/></a>
				</td>
				<td style="display:${param._method=='edit'|| param._method=='save'?'none':''}">
					&nbsp;|<a href="${cpath}/clinical/ScoreCard.do?_method=add"><insta:ltext key="clinicaldata.scorecard.show.reset"/></a>
				</td>
			</tr>
		</table>
	</div>

</div>
</form>
<script>
    var cpath = '<%=request.getContextPath()%>';

    function init() {
        setToday(); // set month range to current month's...
        Insta.initMRNoAcSearch(cpath, "mr_no", "mrnoAcDropdown", "all",

        function (type, args) {
            fetchMrnoDetails();
        });
    }

    function fetchMrnoDetails() {
        var mrno = document.searchForm.mr_no.value;
        if (mrno != "") {
            document.searchForm.action.value = cpath + "/clinical/ScoreCard.do?_method=getPatientDetails";
            document.searchForm._method.value = 'getPatientDetails';
            document.searchForm.submit();
        } else {
            return false;
        }
    }

    var paramMonth = '${monthNum}';
    var paramYear = '${yearNum}';

    function setToday() {
        document.searchForm.year.value = '';
        var now = new Date();
        var day = now.getDate();
        var month = now.getMonth();
        var year = now.getFullYear();
        if (paramMonth == '' && paramYear == '') {
            document.searchForm.month.selectedIndex = month;
            document.searchForm.year.value = year;
        } else {
            document.searchForm.month.selectedIndex = paramMonth - 1;
            document.searchForm.year.value = paramYear;
        }

    }

    function mouseX(evt) {
        if (evt.pageX) return evt.pageX;
        else if (evt.clientX) return evt.clientX + (document.documentElement.scrollLeft ? document.documentElement.scrollLeft : document.body.scrollLeft);
        else return null;
    }

    function mouseY(evt) {
        if (evt.pageY) return evt.pageY;
        else if (evt.clientY) return evt.clientY + (document.documentElement.scrollTop ? document.documentElement.scrollTop : document.body.scrollTop);
        else return null;
    }


    var gCurrentColor = 'Achieved';

    function setColor(color) {
        gCurrentColor = color;
    }


    function changeColor(tparent, tcell) {
        var cellId = tcell.id;
        var textBoxId = cellId.split('Row')[0];
        var textBox = document.getElementById(textBoxId);

        var markerSpan = document.getElementById(textBoxId+"Marker");

        var colorHex = gCurrentColor == 'Achieved' ? '#c3f94a' : gCurrentColor == 'Higher' ? '#FF0000' : '#FFFF00';
        var severity = document.getElementById(textBoxId + "Severity");
        severity.value = gCurrentColor == 'Achieved' ? 'green' : gCurrentColor == 'Higher' ? 'red' : 'yellow';
        tcell.style.backgroundColor = colorHex;
        textBox.style.backgroundColor = colorHex;

        if(gCurrentColor == 'Higher'){
        	markerSpan.innerHTML = '**';
        } else if(gCurrentColor == 'BorderLine') {
        	markerSpan.innerHTML = '*';
        } else {
        	markerSpan.innerHTML = '';
        }

        document.getElementById('contextMenu').style.display = 'none';
    }


	// To set and remove various event handlers, also to get click targets

    var EventUtil = {

        DOMReady: function (f) {
            if (document.addEventListener) {
                document.addEventListener("DOMContentLoaded", f, false);
            } else {
                window.setTimeout(f, 0);
            }
        },

        addHandler: function (element, type, handler) {
            if (element.addEventListener) {
                element.addEventListener(type, handler, false);
            } else if (element.addEvent) {
                element.addEvent('on' + type, handler);
            } else {
                element['on' + type] = handler;
            }
        },

        removeHandler: function (element, type, handler) {
            if (element.removeEventListener) {
                element.removeEventListener(type, handler);
            } else if (element.detachEvent) {
                element.detachEvent('on' + type, handler);
            } else {
                element['on' + type] = null;
            }
        },

        getEventObj: function (e) {
            return e ? e : window.event;
        },

        getEventTarget: function (e) {
            return e.target ? e.target : e.srcElement;
        },

        preventDefault: function (e) {
            e.preventDefault ? e.preventDefault() : e.returnValue = false;
        }
    };

    //On DOM load setup context-menu
    EventUtil.DOMReady(
    <c:if test="${amendValues}">
	    function () {
	        var myContextMenu = new ContextMenu("grid", 'contextMenu', function (e) {
	            e = EventUtil.getEventObj(e);
	            var target = EventUtil.getEventTarget(e);
	            //if menu hyperlink has been clicked, change the color based on severity
	            if (target.nodeName == 'A') {
	                if (myContextMenu.clickTarget.nodeName == 'TD') {
	                    changeColor(myContextMenu.clickTarget.parentNode, myContextMenu.clickTarget);
	                }
	                return false;
	            }
	        });

	        var menu = document.getElementById('contextMenu');
	        EventUtil.addHandler(menu, 'click', function (e) {
	            e = EventUtil.getEventObj(e);
	            var target = EventUtil.getEventTarget(e);
	            //if menu hyperlink has been clicked, change the color based on severity
	            if (target.nodeName == 'A') {
	                if (myContextMenu.clickTarget.nodeName == 'TD') {
	                    changeColor(myContextMenu.clickTarget.parentNode, myContextMenu.clickTarget);
	                }
	                return false;
	            }

	        });
			// handler - such that upon clicking a text-box, all text within is selected.
	        var inputElements = new Array();
	        inputElements = document.getElementsByTagName('INPUT');
	        var j = 0;
	        for (var i = 0; i < inputElements.length; i++) {
	            if (inputElements[i].type == 'text' && inputElements[i].name != 'year' && inputElements[i].name != 'mr_no') {
	                EventUtil.addHandler(inputElements[i], 'click', SelectText);
	            }

	        }

	    }
    </c:if>
    );

    function SelectText(e) {
        e = EventUtil.getEventObj(e);
        var target = EventUtil.getEventTarget(e);
        target.focus();
        target.select();
    }


    function ContextMenu(element, menu, contextHandler) {
        var THIS = this,
            contextEnabled = true;

        THIS.clickTarget = null;
        THIS.target = document.getElementById(element);
        THIS.menu = document.getElementById(menu);
        if (THIS.target) {
            EventUtil.addHandler(THIS.target, 'click', function (e) {
                var classname = EventUtil.getEventTarget(e).className;
                if (contextEnabled && classname != 'blue' && classname != 'darkgreen' && classname != 'lightblue') {
                    e = EventUtil.getEventObj(e);
                    THIS.menu.style.top = mouseY(e) + "px";
                    THIS.menu.style.left = mouseX(e) + "px";
                    THIS.menu.style.display = "block";

                    THIS.clickTarget = e.target;

                    if (typeof contextHandler == 'function') contextHandler(e);
                } else {

                }

                if (e.target.type && e.target.type == 'text') {
                    EventUtil.preventDefault(e);
                    THIS.menu.style.display = 'none';
                }

            });

            EventUtil.addHandler(document.documentElement, 'mouseover', function (e) {
                if (contextEnabled) {
                    var targ = document.getElementById(element);
                    var classname = EventUtil.getEventTarget(e).className;
                    var evenObj = EventUtil.getEventObj(e);
                    evenObj = EventUtil.getEventTarget(e);
                    if ((classname == 'blue' || classname == 'darkgreen' || classname == 'lightblue')) THIS.menu.style.display = 'none';
                }
            });

        }

        THIS.EnableContextMenu = function (status) {
            contextEnabled = status;
        };
    }


    function validateAndPrint() {
        if (!validateMrNoandDate()) return false;
        var elems = document.detailsForm.elements;
        var poststr = '';
        for (var i = 0; i < elems.length; i++) {
            poststr += (i != 0) ? "&" : "";
            poststr += encodeURI(elems[i].name) + "=" + encodeURIComponent(elems[i].value);
        }
        window.open(cpath + "/clinical/ScoreCard.do?_method=printScoreCard&" + poststr);
    }

     function validateAndSave() {
        if (!validateMrNoandDate()) return false;
        document.detailsForm._method.value="save";
        document.detailsForm.month.value = document.searchForm.month.value;
        document.detailsForm.year.value = document.searchForm.year.value;
		document.detailsForm.submit();
    }

    function validateMrNoandDate() {
        var year = document.getElementById('year').value;
        if (year== null || year=='' ||!isInteger(year) || isNaN(year)) {
            alert("Year is not a number...\nPlease enter a valid year");
            return false;
        }

        if (year.length < 4) {
            year = convertTwoDigitYear(parseInt(year, 10), '');
            document.getElementById('year').value = year;
        }

        if (document.getElementById('mr_no').value == null || document.getElementById('mr_no').value == '') {
            alert("MR Number is not valid...\nPlease select a valid MR Number");
            return false;
        }
        return true;
    }


    function validateAndFetchMrnoDetails() {
        if (!validateMrNoandDate()) return false;
        document.searchForm.action = cpath + "/clinical/ScoreCard.do?_method=show";
        document.searchForm._method = "show";
        document.searchForm.submit();
    }
</script>
</body>
</html>