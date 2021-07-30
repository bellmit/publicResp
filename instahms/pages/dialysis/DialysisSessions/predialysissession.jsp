<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<title><insta:ltext key="patient.dialysis.sessions.predialysissession.title"/></title>
	<insta:js-bundle prefix="dialysismodule.commonvalidations"/>
	<script>
    	var toolbarOptions = getToolbarBundle("js.dialysismodule.commonvalidations.toolbar");
		var jsVaccinations = ${jsVaccinations};
		var jsLabResults = ${jsLabResults};
		var jscompletionStatus = '${preSesDetails.completion_status}';
	</script>
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="script" file="dialysis/dialysissessions.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
	<style>
		#dialog1_mask.mask {
		    z-index: 1;
		    display:none;
		    position:absolute;
		    top:0;
		    left:0;
		    -moz-opacity: 0.0001;
		    opacity:0.0001;
		    filter: alpha(opacity=50);
		    background-color:#CCC;
		}
		#dialog2_mask.mask {
		    z-index: 1;
		    display:none;
		    position:absolute;
		    top:0;
		    left:0;
		    -moz-opacity: 0.0001;
		    opacity:0.0001;
		    filter: alpha(opacity=50);
		    background-color:#CCC;
		}
		#dialog3_mask.mask {
		    z-index: 1;
		    display:none;
		    position:absolute;
		    top:0;
		    left:0;
		    -moz-opacity: 0.0001;
		    opacity:0.0001;
		    filter: alpha(opacity=50);
		    background-color:#CCC;
		}
		#dialog4_mask.mask {
		    z-index: 1;
		    display:none;
		    position:absolute;
		    top:0;
		    left:0;
		    -moz-opacity: 0.0001;
		    opacity:0.0001;
		    filter: alpha(opacity=50);
		    background-color:#CCC;
		}
	</style>


	<insta:js-bundle prefix="clinicaldata.commonvalidations"/>

</head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>

<c:set var="SelectOrdered">
 <insta:ltext key="patient.dialysis.sessions.predialysissession.Ordered"/>,
 <insta:ltext key="patient.dialysis.sessions.predialysissession.prepared"/>,
 <insta:ltext key="patient.dialysis.sessions.predialysissession.inprogress"/>,
 <insta:ltext key="patient.dialysis.sessions.predialysissession.completed"/>,
 <insta:ltext key="patient.dialysis.sessions.predialysissession.closed"/>
 </c:set>
<c:set var="saveButton">
 <insta:ltext key="patient.dialysis.prescriptions.save"/>
</c:set>
<c:set var="discontinuedOptions">
 <insta:ltext key="patient.dialysis.sessions.predialysissession.discontinued"/>,
 <insta:ltext key="patient.dialysis.sessions.predialysissession.cancelled"/>
</c:set>
<c:set var="isoText">
 <insta:ltext key="patient.dialysis.sessions.predialysissession.yes"/>,
 <insta:ltext key="patient.dialysis.sessions.predialysissession.no"/>
</c:set>

<c:set var="noproblemOptions">
 <insta:ltext key="patient.dialysis.sessions.predialysissession.noproblem"/>,
<insta:ltext key="patient.dialysis.sessions.predialysissession.reattempted"/>
</c:set>

<c:set var="heparinOptions">
 <insta:ltext key="patient.dialysis.sessions.predialysissession.heparin"/>,
<insta:ltext key="patient.dialysis.sessions.predialysissession.heparinfree"/>
</c:set>

<c:set var="select">
 <insta:ltext key="patient.dialysis.sessions.predialysissession.select"/>,
 <insta:ltext key="patient.dialysis.sessions.predialysissession.15min"/>,
 <insta:ltext key="patient.dialysis.sessions.predialysissession.30min"/>,
 <insta:ltext key="patient.dialysis.sessions.predialysissession.60min"/>,
 <insta:ltext key="patient.dialysis.sessions.predialysissession.50ml"/>,
 <insta:ltext key="patient.dialysis.sessions.predialysissession.100ml"/>
</c:set>

<c:set var="selectfew">
 <insta:ltext key="patient.dialysis.sessions.predialysissession.select"/>,
  <insta:ltext key="patient.dialysis.sessions.predialysissession.50ml"/>,
 <insta:ltext key="patient.dialysis.sessions.predialysissession.100ml"/>
</c:set>

<c:set var="needletype">
 <insta:ltext key="patient.dialysis.sessions.predialysissession.14g"/>,
 <insta:ltext key="patient.dialysis.sessions.predialysissession.15g"/>,
 <insta:ltext key="patient.dialysis.sessions.predialysissession.16g"/>,
 <insta:ltext key="patient.dialysis.sessions.predialysissession.17g"/>,
 <insta:ltext key="patient.dialysis.sessions.predialysissession.18g"/>,
 <insta:ltext key="patient.dialysis.sessions.predialysissession.ijvc"/>,
 <insta:ltext key="patient.dialysis.sessions.predialysissession.permcathete"/>,
 <insta:ltext key="patient.dialysis.sessions.predialysissession.others"/>,
 <insta:ltext key="patient.dialysis.sessions.predialysissession.notapplicable"/>
</c:set>

<c:set var="heparintype">
<insta:ltext key="patient.dialysis.sessions.predialysissession.heparin"/>,
<insta:ltext key="patient.dialysis.sessions.predialysissession.lowmolecularheparin"/>
</c:set>

<c:set var="preEquipOptions">
<insta:ltext key="patient.dialysis.sessions.predialysissession.discontinued"/>,
<insta:ltext key="patient.dialysis.sessions.predialysissession.cancelled"/>
</c:set>

<body onload="preInit(); showPostSesNotes();enabledisableHeparinType();"  class="yui-skin-sam">
<div class="pageHeader"><insta:ltext key="patient.dialysis.sessions.predialysissession.pageHeader"/></div>
<insta:feedback-panel/>
<c:if test="${fn:length(vaccinations) ne 0 || fn:length(labResults) ne 0}">
	<div class="helpPanel">
		<table id="infoTable">
		<c:if test="${fn:length(vaccinations) ne 0}">
			<tr>
				<td valign="top""><img src="${cpath}/images/information.png"/><label id="labelId" style="white-space: nowrap;"></label></td>
			</tr>
		</c:if>
		<c:if test="${fn:length(labResults) ne 0}">
			<tr>
				<td><img src="${cpath}/images/information.png"/><label id="labelId1" style="white-space: nowrap;"></label></td>
			</tr>
		</c:if>
		</table>
	</div>
</c:if>
<insta:patientgeneraldetails  mrno="${param.mr_no}" addExtraFields="true" showClinicalInfo="true"/>
<form name="preDialysis" method="post" action="${cpath}/dialysis/PreDialysisSessions.do" autocomplete="off">
<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(method)}">
<input type="hidden" name="prescription_id" value="${prescription_id}">
<input type="hidden" name="originalStatus" value="${preSesDetails.status}">
<input type="hidden" name="secondary_check_done" value="${preSesDetails.secondary_check_done}">
<input type="hidden" name="original_machine_id" value="${preSesDetails.machine_id}">
<input type="hidden" name="order_id" value="${ifn:cleanHtmlAttribute(order_id)}">
<c:set var="makeReadOnly" value="" />
	<c:if test="${method=='update' && (preSesDetails.status=='C')}">
		<c:set var="makeReadOnly" value="readonly" />
	</c:if>
	<c:set var="reprocessCount"  value=""/>
	<c:if test="${method=='create' && not empty preSesDetails.dialyzer_repr_count && preSesDetails.single_use_dialyzer eq 'N'}">
		<c:set var="reprocessCount"  value="${preSesDetails.dialyzer_repr_count+1}"/>
	</c:if>

	<fieldSet class="fieldSetBorder" ><legend class="fieldSetLabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.sessiondetails"/></legend>
	<table border="0"  width="100%" class="formtable">
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.formlabel"/></td>
			<td>
				<input type="button" name="prevSesNotes" id="prevSesNotes" value=".." title='<insta:ltext key="patient.dialysis.sessions.predialysissession.prev.session.details"/>'
							onclick="previousSessionNotes();"/>
				<div id="dialog4" style="visibility:hidden">
					<div class="bd">
					<table width="100%">
						<tr><td width="100%" id="prevSesDetails">${prev_session_notes}</td></tr>
					</table>
					</div>
				</div>
			</td>
		</tr>
		<tr>
			<td  class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.status"/></td>
			<td >
				<insta:selectoptions id="status" name="status" value="${preSesDetails.status}"
											opvalues="O,P,I,F,C" optexts="${SelectOrdered}" onchange="checkCompletionStatus();"/><span class="star">*</span>




			<td   class="formlabel" colspan="3"><insta:ltext key="patient.dialysis.sessions.predialysissession.preequip"/></td>
			<td align="left" style="border:none; padding: 0 2px">
				<input type="button" name="equipPre" id="equipPre" value=".." title='<insta:ltext key="patient.dialysis.sessions.predialysissession.preequipment.preparation"/>'
							onclick="preEquipmentPreparation();"/>
				<div id="dialog1" style="visibility:hidden">
					<div class="bd">
						<table width="100%">
							<c:forEach var="preDialysisRec" items="${prePrepDialysisList}" varStatus="loop">
								<tr>
									<td>
										<input type="checkbox" name="pre_prep_param_name" id="pre_prep_param_name${loop.index}" value="N" ${param._method == 'show' ? (preDialysisRec.map.prep_param_value == 'Y' ? 'checked' : '') : ''}>
										${preDialysisRec.map.prep_param}
										<input type="hidden" name="prep_param_id" id="prep_param_id${loop.index}" value="${preDialysisRec.map.prep_param_id}">
										<input type="hidden" name="prep_param_value" id="prep_param_value${loop.index}" value="N"/>
									</td>
								</tr>
							</c:forEach>
						</table>
					</div>
				</div>
			</td>
		</tr>
		<tr>
		     <td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.Physician"/></td>
			 <td>
				<insta:selectdb name="physician" table="doctors" valuecol="doctor_id"
								displaycol="doctor_name" filtered="true"  dummyvalue="${dummyvalue}" value="${preSesDetails.physician}"  /><span class="star">*</span>
			 </td>

            <td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.primarydialysistherapist"/></td>
			<td>
				<select name="start_attendant" class="dropdown">
					<c:choose>
						<c:when test="${not empty preSesDetails.start_attendant}">
							<option value="${preSesDetails.start_attendant}">${preSesDetails.start_attendant}</option>
						</c:when>
						<c:otherwise>
							<option value=""><insta:ltext key="patient.dialysis.sessions.postdialysissession.select"/></option>
							<c:forEach var="staffNames" items="${clinicalStaff}" varStatus="st">
								<option value="${staffNames.map.emp_username}">${staffNames.map.emp_username}</option>
							</c:forEach>
						</c:otherwise>
					</c:choose>
				</select><span class="star">*</span>
				</td>

		</tr>
       	<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.secondarycdialysistherapist"/></td>
			<td>
				<select name="dialyzer_check_user2" class="dropdown">
					<c:choose>
						<c:when test="${not empty preSesDetails.dialyzer_check_user2}">
							<option value="${preSesDetails.dialyzer_check_user2}">${preSesDetails.dialyzer_check_user2}</option>
						</c:when>
						<c:otherwise>
							<option value=""><insta:ltext key="patient.dialysis.sessions.postdialysissession.select"/></option>
							<c:forEach var="staffNames" items="${clinicalStaff}" varStatus="st">
								<option value="${staffNames.map['emp_username']}">${staffNames.map['emp_username']}</option>
							</c:forEach>
						</c:otherwise>
					</c:choose>
				</select><span class="star">*</span>
			</td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.password"/></td>
			<td>
				<input type="password"  name="emp_password" id ="emp_password"  ${makeReadOnly}/><span class="star">*</span>
			</td>

			</td>
          <td  class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.cannulationnurse"/></td>
		  <td>
				<select name="cannulation_nurse" class="dropdown">
					<c:choose>
						<c:when test="${not empty preSesDetails.cannulation_nurse}">
							<option value="${preSesDetails.cannulation_nurse}">${preSesDetails.cannulation_nurse}</option>
						</c:when>
						<c:otherwise>
							<option value=""><insta:ltext key="patient.dialysis.sessions.postdialysissession.select"/></option>
							<c:forEach var="staffNames" items="${clinicalStaff}" varStatus="st">
								<option value="${staffNames.map.emp_username}">${staffNames.map.emp_username}</option>
							</c:forEach>
						</c:otherwise>
					</c:choose>
				</select>
		  </td>
		</tr>
		<tr>
			<td  class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.location"/></td>
			<td >
				<select name="location_id" id="location_id" class="dropdown">
					<option value=""><insta:ltext key="patient.dialysis.sessions.postdialysissession.select"/></option>
					<c:forEach items="${locations}" var="location">
						<option value="${location.location_id}"
							${preSesDetails.location_id == location.location_id ? 'selected' : ''}>${location.location_name}</option>
					</c:forEach>
				</select>
				<span class="star">*</span>
			</td>
			<td  class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.machine"/></td>
			<td >
				<select name="machine_id" id="machine_id" class="dropdown">
					<option value=""><insta:ltext key="patient.dialysis.sessions.postdialysissession.select"/></option>
					<c:forEach items="${machines}" var="machine">
						<option value="${machine.map.machine_id}"
							${machine.map.machine_id == preSesDetails.machine_id ? 'selected' : ''}>${machine.map.machine_name}</option>
					</c:forEach>
				</select>
				<span class="star">*</span>


		</tr>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.accesstype"/></td>
			<td>
				<select name="access_type_id" class="dropdown" onchange="setPatencyCheckBoxValues(document.preDialysis);">
					<option value=""><insta:ltext key="patient.dialysis.sessions.postdialysissession.select"/></option>
					<c:forEach items="${accessTypes}" var="accessType">
					<option value="${accessType.map.access_type_id}" ${accessType.map.access_type_id == preSesDetails.access_type_id ? 'selected' : ''}>${accessType.map.access_type}</option>
					</c:forEach>
				</select><span class="star">*</span>
			</td>
			<td align="left" colspan="2">
				<input type="hidden" name="access_patency" id="access_patency" value="">
				<div style="display: none" id="access_patency_group1">
					<insta:ltext key="patient.dialysis.sessions.predialysissession.nf"/><input type="checkbox" name="patencyNf" value=""  ${preSesDetails.patency_nf == 'Y' ? 'checked' : '' }/>
				<insta:ltext key="patient.dialysis.sessions.predialysissession.rf"/><input type="checkbox" name="patencyRf" value=""  ${preSesDetails.patency_rf == 'Y' ? 'checked' : '' }/>
					<input type="hidden" name="patency_nf" id="patency_nf" value="">
					<input type="hidden" name="patency_rf" id="patency_rf" value="">
				</div>
				<div style="display: none" id="access_patency_group2">
					<label style="padding-left: 88px;"><insta:ltext key="patient.dialysis.sessions.predialysissession.bruit"/></label>
							<select name="patency_bruit_thrill" id="patency_bruit_thrill" class="dropdown">
								<option value=""><insta:ltext key="patient.dialysis.sessions.postdialysissession.select"/></option>
								<option value="Y" ${preSesDetails.patency_bruit_thrill eq 'Y' ? 'selected' : ''}><insta:ltext key="patient.dialysis.sessions.predialysissession.yes"/></option>
								<option value="O" ${preSesDetails.patency_bruit_thrill eq 'O' ? 'selected' : ''}><insta:ltext key="patient.dialysis.sessions.predialysissession.no"/></option>
								<option value="N" ${preSesDetails.patency_bruit_thrill eq 'N' ? 'selected' : ''}><insta:ltext key="patient.dialysis.sessions.predialysissession.notapplicable"/></option>
							</select><span class="star">*</span>
					<input type="hidden" name="patency_bruit" id="patency_bruit_thrill" value="">
				</div>
			</td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.cannulation"/></td>
			<td >
				<div style="float: left;" >
					<insta:selectoptions name="cannulation" value="${preSesDetails.cannulation}"
						  onchange="enableCannulationReAttemp()"   opvalues="N,R" optexts="${noproblemOptions}"/>
				</div>
				<div style="float: left;" >
					<input type="text" name="cannulation_reattempt" id="cannulation_reattempt" value="${preSesDetails.cannulation_reattempt}" class="number" ${makeReadOnly}/>
				</div>
			</td>
		</tr>
		<tr>
			<td  class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.accesssite"/></td>

			<td>
				<select name="access_site_id" class="dropdown">
				<option value=""><insta:ltext key="patient.dialysis.sessions.postdialysissession.select"/></option>
					<c:forEach items="${accessTypes}" var="accessSites">
						<option value="${accessSites.map.access_site_id}" ${accessSites.map.access_site_id == preSesDetails.access_site_id ? 'selected' : '' }>${accessSites.map.access_site}</option>
					</c:forEach>
				</select><span class="star">*</span>
			</td>
			<td  class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.accesssiteinfection"/></td>
			<td>
				<c:set var="accessSiteInfection" value="${empty preSesDetails.access_site_infection ? 'N' : preSesDetails.access_site_infection }" />
				<insta:radio name="access_site_infection" radioText="${isoText}"
						radioValues="Y,N" value="${accessSiteInfection}" radioIds="accessYes,accessNo" />
			</td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.isouf"/></td>
			<td >
				<c:set var="isoUf" value="${empty preSesDetails.iso_uf ? 'N' : preSesDetails.iso_uf }" />
				<c:set var="isoTime" ><fmt:formatDate value="${preSesDetails.iso_uf_time}" pattern="HH:mm"></fmt:formatDate></c:set>
					<insta:radio name="iso_uf" radioText="${isoText}"
						radioValues="Y,N" value="${isoUf}" radioIds="isoYes,isoNo" />
					<input type="text" name="iso_uf_time" id="iso_uf_time" value="${isoTime}" class="number"/>
			</td>
		</tr>
		<tr>
				<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.dialysate"/></td>
				<td>
					<insta:selectdb name="dialysate_type_id" table="dialysate_type" valuecol="dialysate_type_id"
						displaycol="dialysate_type_name" filtered="true" value="${preSesDetails.dialysate_type_id}" dummyvalue="${dummyvalue}"  /><span class="star">*</span>
				</td>
				<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.fromlabel1"/></td>
				<td><input type="text" name="other_staff" id="other_staff" maxlength="50" value="${preSesDetails.other_staff}"/></td>
				<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.fromlabel2"/></td>
				<td>
					<insta:selectoptions id="completion_status" name="completion_status" value="${preSesDetails.completion_status}"
												opvalues="D,X" optexts="${discontinuedOptions}" dummyvalue="${dummyvalue}" dummyvalueid="" disabled=""/><span class="star">*</span>
				</td>
		</tr>
		<tr>
		<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.needle"/>:</td>
								<td style="width: 50px;"><insta:selectoptions name="needle_type" dummyvalue="${dummyvalue}" dummyvalueid=""
											opvalues="14g,15g,16g,17g,18g,IJVC,PERM CATHETER,OTHERS,Not Applicable" optexts="${needletype}" value="${preSesDetails.needle_type}" /><span class="star">*</span></td>

		</tr>
	</table>
	</fieldSet>
	<fieldSet class="fieldSetBorder" ><legend class="fieldSetLabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.dialyzerreuse"/></legend>
	<table  class="formtable">
		<tr>
			<td  class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.dialyzer"/></td>
			<td >
				<insta:selectdb name="dialyzer_type_id" table="dialyzer_types" valuecol="dialyzer_type_id"
												displaycol="dialyzer_type_name" filtered="true" value="${preSesDetails.dialyzer_type_id}" dummyvalue="${dummyvalue}" /><span class="star">*</span>
			</td>
			<td  class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.bundlevolume"/></td>
			<td >
				<input type="text" name="dialyzer_lot_num"  id ="dialyzer_lot_num" value="${preSesDetails.dialyzer_lot_num}" ${makeReadOnly}/>
			</td>
			<td  class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.reprocess"/></td>
			<td>
				<input type="text" class="number" name="dialyzer_repr_count" id ="dialyzer_repr_count" onkeypress="return enterNumOnlyzeroToNine(event);"
						value="${reprocessCount==''? preSesDetails.dialyzer_repr_count:reprocessCount}" ${makeReadOnly}/>
				<span class="star">*</span>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.reprocessed"/></td>
			<td>
				<c:set var="reprocessedDate" ><fmt:formatDate pattern="dd-MM-yyyy" value="${preSesDetails.dialyzer_repr_date}"/></c:set>
				<div style="float: left"><insta:datewidget name="dialyzer_repr_date" valid="past" value="${reprocessedDate}" btnPos="left" autocomplete="off"/></div>
				<div style="float: left"> <span class="star"> * </span></div>
			</td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.rating"/></td>
			<td>
				<insta:selectdb name="in_dilayzer_rating_id" table="dialyzer_ratings" valuecol="dialyzer_rating_id"
												displaycol="dialyzer_rating" filtered="true" value="${preSesDetails.in_dilayzer_rating_id}" dummyvalue="${dummyvalue}" /><span class="star">*</span>
			</td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.singleuse"/></td>

			<td>
				<input type="checkbox" name="singleUseDialyzer" ${preSesDetails.single_use_dialyzer eq 'Y' ? 'checked' : ''}/>
				<input type="hidden" name="single_use_dialyzer" id="single_use_dialyzer" value="${preSesDetails.single_use_dialyzer}">
			</td>
		</tr>

	</table>
	</fieldSet>

	<fieldSet class="fieldSetBorder" ><legend class="fieldSetLabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.patientcondition"/></legend>
	<table class="formtable">
		<tr>
			<td  class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.bpsitting"/></td>
			<td  >
				<input type="text" name="in_bp_high_sit" id ="in_bp_high_sit" value="${preSesDetails.in_bp_high_sit}" class="number" ${makeReadOnly}/> / <input type="text" name="in_bp_low_sit" id ="in_bp_low_sit" value="${preSesDetails.in_bp_low_sit}" class="number" ${makeReadOnly}/><span class="star"> * </span>
			</td>
			<td  class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.bpstanding"/></td>
			<td >
				<input type="text" name="in_bp_high_stand" id ="in_bp_high_stand" value="${preSesDetails.in_bp_high_stand}" class="number" ${makeReadOnly}/> / <input type="text" name="in_bp_low_stand" id ="in_bp_low_stand" value="${preSesDetails.in_bp_low_stand}" class="number" ${makeReadOnly}/><span class="star"> * </span>
			</td>
			<td  class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.respiration"/></td>
			<td>
				<input type="text" name="in_respiration" id ="in_respiration"  value="${preSesDetails.in_respiration}" class="number" ${makeReadOnly}/>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.pulsesitting"/></td>
			<td>
				<input type="text" name="in_pulse_sit" id ="in_pulse_sit" value="${preSesDetails.in_pulse_sit}" class="number" ${makeReadOnly}/><span class="star"> * </span>
			</td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.pulsestanding"/></td>
			<td>
				<input type="text" name="in_pulse_stand" id ="in_pulse_stand" value="${preSesDetails.in_pulse_stand}" class="number" ${makeReadOnly}/><span class="star"> * </span>
			</td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.temperature"/></td>
			<td>
				<input type="text" name="in_temperature" id ="in_temperature" value="${preSesDetails.in_temperature}" class="number" ${makeReadOnly}/>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.measuredwt"/></td>
			<td>
				<input type="text" name="in_total_wt" id ="in_total_wt" value="${preSesDetails.in_total_wt}" class="number" ${makeReadOnly} onblur="setValues();"/>
			</td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.wheelchair"/></td>
			<td>
				<input type="text" name="in_wheelchair_wt" id ="in_wheelchair_wt" value="${preSesDetails.in_wheelchair_wt}" class="number" ${makeReadOnly} onblur="setValues();"/>
			</td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.prosthetic"/></td>
			<td>
				<input type="text" name="in_prosthetic_wt" id ="in_prosthetic_wt" value="${preSesDetails.in_prosthetic_wt}" class="number" ${makeReadOnly} onblur="setValues();"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.currentwt"/></td>
			<td><label id="currentWeight" ></label></td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.previouswt"/></td>
			<td>
				<input type="hidden" name="in_real_wt" value="">
				<label id="previousWeight" >${previousSesWT}</label>
			</td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.weightchange"/></td>
			<td >
				<label id="weightChange" ></label>
				<input type="hidden" name="weight_change" id="weight_change" value="">
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.condition"/></td>
			<td ><textarea name="in_patient_cond" ${makeReadOnly}>${preSesDetails.in_patient_cond}</textarea></td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.assessment"/></td>
			<td align="left" style="border:none; padding: 0 2px">
				<input type="button" name="preAssessment" id="preAssessment" value=".." title='<insta:ltext key="patient.dialysis.sessions.predialysissession.predialysis.assessment"/>'
							onclick="preAssessmentCheck();"/>
				<div id="dialog3" style="visibility:hidden">
					<div class="bd">
						<table width="100%">
							<tr>
								<td ><insta:ltext key="patient.dialysis.sessions.predialysissession.chestauscultation"/></td>
								<td>
									<input type="radio" name="chest_auscultation_clear" value="Y" ${preSesDetails.chest_auscultation_clear == 'Y' ? 'checked' : '' }/><insta:ltext key="patient.dialysis.sessions.predialysissession.yes"/>
									<input type="radio" name="chest_auscultation_clear" value="N" ${preSesDetails.chest_auscultation_clear == 'N' ? 'checked' : '' }/><insta:ltext key="patient.dialysis.sessions.predialysissession.no"/>
								</td>
							</tr>
							<tr>
								<td><insta:ltext key="patient.dialysis.sessions.predialysissession.peripheral"/></td>
								<td>
									<input type="radio" name="peripheral_edema" value="Y" ${preSesDetails.peripheral_edema == 'Y' ? 'checked' : '' }/><insta:ltext key="patient.dialysis.sessions.predialysissession.yes"/>
									<input type="radio" name="peripheral_edema" value="N" ${preSesDetails.peripheral_edema == 'N' ? 'checked' : '' }/><insta:ltext key="patient.dialysis.sessions.predialysissession.no"/>
								</td>
							</tr>
							<tr>
								<td><insta:ltext key="patient.dialysis.sessions.predialysissession.physical"/></td>
								<td>
									<input type="radio" name="chest_pain_discomfort" value="Y" ${preSesDetails.chest_pain_discomfort == 'Y' ? 'checked' : '' }/><insta:ltext key="patient.dialysis.sessions.predialysissession.yes"/>
									<input type="radio" name="chest_pain_discomfort" value="N" ${preSesDetails.chest_pain_discomfort == 'N' ? 'checked' : '' }/><insta:ltext key="patient.dialysis.sessions.predialysissession.no"/>
								</td>

							</tr>
							<tr>
								<td><insta:ltext key="patient.dialysis.sessions.predialysissession.recent"/></td>
								<td>
									<input type="radio" name="recent_surgery" value="Y" ${preSesDetails.recent_surgery == 'Y' ? 'checked' : '' }/><insta:ltext key="patient.dialysis.sessions.predialysissession.yes"/>
									<input type="radio" name="recent_surgery" value="N" ${preSesDetails.recent_surgery == 'N' ? 'checked' : '' }/><insta:ltext key="patient.dialysis.sessions.predialysissession.no"/>
								</td>
							</tr>
							<tr>
								<td><insta:ltext key="patient.dialysis.sessions.predialysissession.intradialysis"/></td>
								<td>
									<input type="radio" name="intradialysis_complaints" value="Y" ${preSesDetails.intradialysis_complaints == 'Y' ? 'checked' : '' }/><insta:ltext key="patient.dialysis.sessions.predialysissession.yes"/>
									<input type="radio" name="intradialysis_complaints" value="N" ${preSesDetails.intradialysis_complaints == 'N' ? 'checked' : '' }/><insta:ltext key="patient.dialysis.sessions.predialysissession.no"/>
								</td>
							</tr>
							<tr>
								<td><insta:ltext key="patient.dialysis.sessions.predialysissession.breakfast"/></td>
								<td>
									<input type="radio" name="breakfast_lunch_dinner" value="Y" ${preSesDetails.breakfast_lunch_dinner == 'Y' ? 'checked' : '' }/><insta:ltext key="patient.dialysis.sessions.predialysissession.yes"/>
									<input type="radio" name="breakfast_lunch_dinner" value="N" ${preSesDetails.breakfast_lunch_dinner == 'N' ? 'checked' : '' }/><insta:ltext key="patient.dialysis.sessions.predialysissession.no"/>
								</td>
							</tr>
						</table>
					</div>
				</div>
			</td>
		</tr>
	</table>
	</fieldSet>

	<fieldSet class="fieldSetBorder" ><legend class="fieldSetLabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.dialysisdetails"/></legend>
	<table   class="formtable">
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.drywt"/></td>
			<c:set var="dryWeightDate"><fmt:formatDate value="${preSesDetails.dry_wt_date}" pattern="dd-MM-yyyy"/></c:set>
			<td>${preSesDetails.dry_wt} / ${dryWeightDate}</td>
			<td  class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.targerwt"/></td>
			<td >
				<input type="text" name="target_wt" id ="target_wt" value="${preSesDetails.target_wt}" class="number" ${makeReadOnly} onblur="setExcessWeight();"/>
			</td>
			<td  class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.excesswt"/></td>
			<td ><label id="excessWt"></label></td>
	</tr>
	<tr>
		<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.duration"/></td>
			<td>
				<input type="text" name="estDuration" id ="estDuration" class="number" ${makeReadOnly} onblur="setExcessWeight();"/>
				<input type="hidden" name="est_duration" id ="est_duration" value="${preSesDetails.est_duration}"/>
			</td>

			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.targetuf"/></td>
			<td><label id="targetUF"></label></td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.targetufr"/></td>
			<td><label id="targetUFR"></label></td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.anticoagulation"/></td>
			<td >
				<insta:selectoptions name="anticoagulation" value="${preSesDetails.anticoagulation}"
						onchange="enableCoagulation()" 	opvalues="H,F" optexts="${heparinOptions}"/>
			</td>
		</tr>
		<tr id="heparintype" >
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.heparintype"/>:</td>
								<td style="width: 50px;"><insta:selectoptions name="heparin_type" id="heparin_type" dummyvalue="${dummyvalue}" dummyvalueid=""
								onchange="enabledisableHeparinType()"	opvalues="h,i" optexts="${heparintype}" value="${preSesDetails.heparin_type}" />
		    </td>


		   <td id="initial" class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.lowheparininitaldose"/>:</td>
           <td id="initialdose">
			  <input type="text" name="low_heparin_initial_dose" id ="low_heparin_initial_dose" value="${preSesDetails.low_heparin_initial_dose}"  ${makeReadOnly} class="number" onkeypress="return enterNumAndDot(event)" onblur="totalheparindose()"  />
		   </td>
		   <td id="intrim" class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.lowheparinintrimdose"/>:</td>
           <td id="intrimdose">
			  <input type="text" name="low_heparin_intrim_dose" id ="low_heparin_intrim_dose" value="${preSesDetails.low_heparin_intrim_dose}"   ${makeReadOnly} class="number" onkeypress="return enterNumAndDot(event)" onblur="totalheparindose()"  />
		   </td>
		</tr>
		<tr>
			<td colspan="4">
				<div  style="display: none" id="withHeparin">
					<table border="0" width="100%">
						<tr>
							<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.heparinbolus"/>:</td>
							<td class="formlabel" style="white-space: nowrap">
								<input type="text" name="heparin_bolus" id ="heparin_bolus" value="${preSesDetails.heparin_bolus}" class="number" ${makeReadOnly} onkeypress="return enterNumAndDot(event)" />
								<insta:ltext key="patient.dialysis.sessions.predialysissession.hourly"/><input type="text" name="heparin_rate" id ="heparin_rate" value="${preSesDetails.heparin_rate}" class="number" ${makeReadOnly}/>
							</td>
							<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.unitsinsyringe"/></td>
							<td >
								<input type="text" name="heparin_start" id ="heparin_start" value="${preSesDetails.heparin_start}" class="number" ${makeReadOnly}/>
							</td>
						</tr>

					</table>
					</div>
					<div  style="display: none" id="heparinFree">
					<table  border="0" width="100%">
						<tr>
							<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.frequency"/></td>
							<td >
								<insta:selectoptions name="frequency" value="${preSesDetails.frequency}"
										opvalues="null,15,30,60" optexts="${select}"/>
							</td>
							<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.volume"/></td>
							<td >
								<insta:selectoptions name="volume" value="${preSesDetails.volume}"
										opvalues="null,50,100" optexts="${selectfew}"/>
							</td>
						</tr>
					</table>
				</div>

			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.dialysisodometerstart"/></td>
			<td><input type="text" name="in_odometer_reading" value="${preSesDetails.in_odometer_reading}" onkeypress="return enterNumOnlyzeroToNine(event)" class="number"></td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.predialysis"/> </br> <insta:ltext key="patient.dialysis.sessions.predialysissession.assessment"/> </td>
			<td><textarea name="start_notes" ${makeReadOnly}>${preSesDetails.start_notes}</textarea></td>
			<td> <insta:ltext key="patient.dialysis.sessions.predialysissession.comment"/></td>
			<td><textarea name="post_session_notes" >${preSesDetails.post_session_notes}</textarea></td>
		</tr>
        <tr>
            <td class="formlabel"><insta:ltext key="patient.dialysis.sessions.predialysissession.filtrationfluid"/>:</td>
			<td><input type="text" name="filtration_replacement_fluid_volume" id ="filtrationFluid" class="number" value="${preSesDetails.filtration_replacement_fluid_volume}" onkeypress="return enterNumAndDot(event)" /></td>
        </tr>

	</table>
	</fieldSet>

	<c:url value="PostDialysisSessions.do" var="posturl">
			<c:param name="_method" value="show"/>
			<c:param name="mr_no" value="${mr_no}"/>
			<c:param name="order_id" value="${order_id}"/>
		</c:url>
	<c:url value="DialysisPrescriptions.do" var="prescrurl">
		<c:param name="_method" value="show"/>
		<c:param name="mr_no" value="${mr_no}"/>
		<c:param name="dialysis_presc_id" value="${preSesDetails.prescription_id}"/>
	</c:url>
	<c:url value="IntraDialysisSessions.do" var="intraurl">
		<c:param name="_method" value="show"/>
		<c:param name="mr_no" value="${mr_no}"/>
		<c:param name="order_id" value="${order_id}"/>
	</c:url>
	<div class="screenActions" align="left">
		<input type="submit" name="save" id="save" value="${saveButton}" onclick="return funValidateDetails();">
		| <a href="javascript:void(0)" onclick="funCancel();"><insta:ltext key="patient.dialysis.sessions.predialysissession.dialysissession"/></a> |
		<c:if test="${preSesDetails.status == 'I' || preSesDetails.status == 'F' || preSesDetails.status == 'C'}">
			 <a href='<c:out value="${intraurl}"/>'><insta:ltext key="patient.dialysis.sessions.predialysissession.intradialysis"/></a> |
		</c:if>
		<c:if test="${preSesDetails.status == 'F' || preSesDetails.status == 'C'}">
			<a href='<c:out value="${posturl}"/>'><insta:ltext key="patient.dialysis.sessions.predialysissession.postdialysis"/></a> |
		</c:if>
		<a href='<c:out value="${prescrurl}"/>'><insta:ltext key="patient.dialysis.sessions.predialysissession.prescription"/></a>
		<c:if test="${preferences.modulesActivatedMap['mod_clinical_data'] == 'Y'}">
			| <a href="${cpath}/dialysis/ClinicalDataLabResults.do?_method=list&mr_no=${ifn:cleanURL(param.mr_no)}"><insta:ltext key="patient.dialysis.sessions.predialysissession.clinicallab"/></a>
			| <a href="${cpath}/dialysis/HospitalizationInformation.do?_method=show&mr_no=${ifn:cleanURL(param.mr_no)}"><insta:ltext key="patient.dialysis.sessions.predialysissession.clinicalhospital"/></a>
			| <a href="${cpath}/clinical/Vaccinations.do?_method=show&mr_no=${ifn:cleanURL(param.mr_no)}"><insta:ltext key="patient.dialysis.sessions.predialysissession.clinalvacc"/></a>
			| <a href="${cpath}/clinical/DialysisAdequacy.do?_method=show&mr_no=${ifn:cleanURL(param.mr_no)}"><insta:ltext key="patient.dialysis.sessions.predialysissession.clinicaldialysis"/></a>
			| <a href="${cpath}/clinical/Infections.do?_method=show&mr_no=${ifn:cleanURL(param.mr_no)}"><insta:ltext key="patient.dialysis.sessions.predialysissession.clinicalinfections"/></a>

		</c:if>
	</div>
</form>
<script>
	var attendants = ${attendantJson};
	var originalStatus = '${preSesDetails.status}';
	var machineMasterDetails = ${machineMasterDetailsJson};
	var AccessTypeDetailsJson = ${AccessTypeDetailsJson};
	var originalDuration = '${preSesDetails.est_duration}';
	var isFinalized = '${isFinalized}';
	var prevSessionNotes = <insta:jsString value="${prev_session_notes}"/>;
</script>
</body>
</html>
