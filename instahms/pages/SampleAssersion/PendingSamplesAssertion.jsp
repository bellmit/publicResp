<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<c:set var="samplesList" value="${pagedList.dtoList}" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<head>
	<title><insta:ltext key="laboratory.pendingsamplesassertion.list.pendingsamplesassertion"/></title>
	<insta:link type="css" file="widgets.css" />
	<insta:link type="script" file="widgets.js" />
	<insta:link type="script" file="dashboardColors.js" />
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="hmsvalidation.js" />
	<insta:link type="script" file="SampleAssersion/pendingsampleassersion.js" />
	<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
	<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
	<script>
	    var outHouses = ${outhouses};
		var centerId = ${centerId};
		var inHouses = ${inHouses};
		var sampleAssertionListJSON =  <%= request.getAttribute("sampleAssertionListJSON") %>;
		var max_centers_inc_default = ${max_centers_inc_default};
		var sampleCollectionCenterId = ${sampleCollectionCenterId};
		function autoCompleteOutHouse() {
			dataSource = new YAHOO.util.LocalDataSource(outHouses,{ queryMatchContains : true })
			dataSource.responseSchema = {fields : ["OUTSOURCE_NAME"]};
			oAutoComp1 = new YAHOO.widget.AutoComplete('outsource_name', 'outhouse_container', dataSource);
			oAutoComp1.maxResultsDisplayed = 15;
			oAutoComp1.allowBrowserAutocomplete = false;
			oAutoComp1.prehighlightClassName = "yui-ac-prehighlight";
			oAutoComp1.typeAhead = false;
			oAutoComp1.useShadow = false;
			oAutoComp1.minQueryLength = 0;
			oAutoComp1.animVert = false;
			oAutoComp1.forceSelection = true;


			oAutoComp1.textboxBlurEvent.subscribe(function() {
		var OUTSOURCE_NAME = YAHOO.util.Dom.get(outsource_name).value;
		if(OUTSOURCE_NAME == '') {
			YAHOO.util.Dom.get(outsource_dest_id).value = "";
		}
	});
	oAutoComp1.itemSelectEvent.subscribe(function() {
		var OUTSOURCE_NAME = YAHOO.util.Dom.get(outsource_name).value;
		if(OUTSOURCE_NAME != '') {
			for ( var i=0 ; i< outHouses.length; i++){
				if(OUTSOURCE_NAME == outHouses[i]["outsource_name"]){
					YAHOO.util.Dom.get(outsource_dest_id).value = outHouses[i]["outsource_dest_id"];
					break;
				}
			}
		}else{
			YAHOO.util.Dom.get(outsource_dest_id).value = "";
		}
	});

		}
		function smplnofocus(){
          document.getElementById('sampleSno').focus();
		}
		function init(){
		initMrNoAutoComplete('${cpath}');
		autoCompleteOutHouse();
		initsampleRejectDialog();
		smplnofocus();
		autoCompleteInHouse();
		initSampleAssertionDialog();
		}
	</script>
	<insta:js-bundle prefix="laboratory.radiology.pendingsampleassertion"/>
	<insta:js-bundle prefix="laboratory.radiology"/>
</head>
<body onload="init();">
<c:set var="samplestatus">
 <insta:ltext key="laboratory.pendingsamplesassertion.list.pending"/>,
 <insta:ltext key="laboratory.pendingsamplesassertion.list.asserted"/>,
 <insta:ltext key="laboratory.pendingsamplesassertion.list.rejected"/>
</c:set>
<c:set var="conductionType">
 <insta:ltext key="laboratory.pendingsamplesassertion.list.inhouse"/>,
 <insta:ltext key="laboratory.pendingsamplesassertion.list.outhouse"/>
</c:set>
<c:set var="patientType">
 <insta:ltext key="laboratory.pendingsamplesassertion.list.ip"/>,
  <insta:ltext key="laboratory.pendingsamplesassertion.list.op"/>,
 <insta:ltext key="laboratory.pendingsamplesassertion.list.incomingtest"/>
 </c:set>
 <c:set var="testPriority">
 <insta:ltext key="laboratory.pendingsamplesassertion.list.stat"/>,
 <insta:ltext key="laboratory.pendingsamplesassertion.list.regular"/>
</c:set>
<c:set var="sampleDate">
 <insta:ltext key="laboratory.pendingsamplesassertion.list.sampledate"/>
</c:set>
 <c:set var="mrno">
 <insta:ltext key="ui.label.mrno"/>
</c:set>

	<h1><insta:ltext key="laboratory.pendingsamplesassertion.list.pendingsamplesassertion"/></h1>
	<insta:feedback-panel/>
	<form action="PendingSamplesAssertion.do"  name="sampleAssersionSearchForm">
		<input type="hidden" name="_method" value="list">
		<input type="hidden" name="_searchMethod" value="list"/>
		<insta:search form="sampleAssersionSearchForm" optionsId="optionalFilter" closed="${hasResults}">
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="laboratory.pendingsamplesassertion.list.mrno.patientname"/></div>
					<div class="sboFieldInput">
						<div id="mrnoAutoComplete">
							<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
							<div id="mrnoContainer" style="width: 300px"></div>
						</div>
					</div>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="laboratory.pendingsamplesassertion.list.sampleid"/>:</div>
					<div class="sboFieldInput">
						<div id="sampleId">
							<input type="text" name="sampleSno" id="sampleSno" value="${ifn:cleanHtmlAttribute(param.sampleSno)}"/>
							<div id="deptContainer"></div>
						</div>
					</div>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="laboratory.pendingsamplesassertion.list.sampledate"/></div>
					<div class="sboFieldInput">
					<div class="sfFieldSub"><insta:ltext key="laboratory.pendingsamplesassertion.list.from"/></div>
						<div id="sampleDate">
							<insta:datewidget name="sample_date" valid="past"	id="sample_date0" value="${paramValues.sample_date[0]}" />
						</div>
						<input type="hidden" name="sample_date@cast" value="y"/>
						<input type="hidden" name="sample_date@type" value="date"/>
						<input type="hidden" name="sample_date@op" value="ge,le"/>
					</div>
					<div class="sboFieldInput">
						<div class="sfFieldSub"><insta:ltext key="laboratory.pendingsamplesassertion.list.to"/></div>
						<insta:datewidget name="sample_date" id="sample_date1" valid="past"	value="${paramValues.sample_date[1]}" />
					</div>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="laboratory.pendingsamplesassertion.list.department"/></div>
					<div class="sboFieldInput">
						<div id="deptAutoComplete">
							<insta:selectdb name="ddept_id" table="diagnostics_departments" valuecol="ddept_id" filtercol="category,status" filtervalue="DEP_LAB,A"
								displaycol="ddept_name"	value="${empty param.ddept_id ? userDept : param.ddept_id}"
								dummyvalue="-- All --"  dummyvalueId=""/>
							<input type="hidden" name="ddept_id@op" value="ilike"/>
							<div id="deptContainer"></div>
						</div>
					</div>
				</div>
			</div>

			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel"><insta:ltext key="laboratory.pendingsamplesassertion.list.sampletype"/></div>
							<div class="sfField">
								<insta:selectdb name="sample_type_id" table="sample_type" valuecol="sample_type_id"
									displaycol="sample_type" size="8" style="width: 11em" multiple="true"
									values="${paramValues.sample_type_id}" class="noClass" orderby="sample_type"/>
									<input type="hidden" name="sample_type_id@cast" value="y"/>
							</div>
							<div class="sfLabel"><insta:ltext key="laboratory.pendingsamplesassertion.list.orginalsampleno"/>:</div>
							<div class="sfField">
								<div id="sampleId">
									<input type="text" name="origSampleSno" id="origSampleSno" value="${ifn:cleanHtmlAttribute(param.origSampleSno)}"/>
									<div id="deptContainer"></div>
								</div>
						    </div>
						</td>

						<c:if test="${sampleCollectionCenterId == -1}">
							<td>
							 <div class="sfLabel"><insta:ltext key="laboratory.pendingsamplesassertion.list.conductiontype"/></div>
							  <div class="sfField">
								<insta:checkgroup name="house_status" selValues="${paramValues.house_status}"
									opvalues="I,O" optexts="${conductionType}"/>
							  </div>

								<div class="sfLabel"><insta:ltext key="laboratory.pendingsamplesassertion.list.collectioncenter"/>:</div>
								<div class="sfField">
									<c:choose>
										<c:when test="${max_centers_inc_default > 1 && centerId != 0}">
												<select name="collectionCenterId" id="collectionCenterId" class="dropdown">
														<option value="">--Select--</option>
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
										valuecol="collection_center_id" displaycol="collection_center" dummyvalue="-- Select --"/>
										</c:otherwise>
									</c:choose>
								</div>
								<div class="sfLabel"><insta:ltext key="laboratory.pendingsamplesassertion.list.outhouse"/></div>
								<div class="sfField">
									<div id="outhouse_wrapper">
									<input type="text" name="outsource_name" id="outsource_name" value="${param.outsource_name}" />
									<input type="hidden" name="outsource_dest_id" id="outsource_dest_id" value="${param.outsource_dest_id}" />
									<div id="outhouse_container"></div>
									</div>
						     	</div>
                               <div>&nbsp;</div>
							</td>
						</c:if>
						<td>
							<div class="sfLabel"><insta:ltext key="laboratory.pendingsamplesassertion.list.patienttype"/></div>
							<div class="sfField">
								<insta:checkgroup name="visit_type" selValues="${paramValues.visit_type}"
									opvalues="i,o,t" optexts="${patientType}"/>
							</div>
							<div class="sfLabel"><insta:ltext key="laboratory.pendingsamplesassertion.list.testpriority"/></div>
								<div class="sfField">
									<insta:checkgroup name="priority" selValues="${paramValues.priority}"
									opvalues="S,R" optexts="${testPriority}"/>
								</div>
						</td>
						<td class="last">
							<div class="sfLabel"><insta:ltext key="laboratory.pendingsamplesassertion.list.samplestatus"/></div>
							<div class="sfField">
								<insta:checkgroup name="sample_status" selValues="${paramValues.sample_status}"
								opvalues="C,A,R" optexts="${samplestatus}"/>
							</div>
							<div class="sfLabel"><insta:ltext key="laboratory.pendingsamplesassertion.list.incominghospital"/></div>
							<div class="sfField" style="height: 20px">
								<div id="inhouse_wrapper">
									<input type="text" name="ih_name" id="ih_name" value="${ifn:cleanHtmlAttribute(param.ih_name)}" />
									<div id="inhouse_container"></div>
								</div>
							</div>
							<div class="sfLabel"><insta:ltext key="laboratory.pendingsamplesassertion.list.incomingpatientname"/></div>
							<div class="sfField">
								<input type="text" name="inc_patient_name" value="${ifn:cleanHtmlAttribute(param.inc_patient_name)}"/>
								<input type="hidden" name="inc_patient_name@op" value="ilike"/>
							</div>
							<div class="sfLabel"><insta:ltext key="laboratory.pendingsamplesassertion.list.incomingpatient.otherinfo"/></div>
							<div class="sfField">
								<input type="text" name="patient_other_info" value="${ifn:cleanHtmlAttribute(param.patient_other_info)}"/>
							</div>

						</td>
					</tr>
				</table>
			</div>
		</insta:search>
	</form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<c:url var="urlWithoutSort" value="Ipservices.do">
		<c:forEach var="p" items="${param}">
			<c:if test="${p.key != 'sortOrder' && p.key != 'sortReverse'}">
				<c:param name="${p.key}" value="${p.value}" />
			</c:if>
		</c:forEach>
	</c:url>
	<form action="PendingSamplesAssertion.do" name="sampleAssersionForm" method="POST">
	<input type="hidden" name="_method" value="insert">
		<div class="resultList">
			<table class="resultList dialog_displayColumns" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
				    <th></th>
					<th><input type="checkbox" onclick="selectAllSamples()" id="assertAll" style="padding-bottom: 11px"/></th>
					<th></th>
					<insta:sortablecolumn name="mr_no" title="${mrno}" />
					<th><insta:ltext key="laboratory.pendingsamplesassertion.list.patientid"/></th>
					<th><insta:ltext key="laboratory.pendingsamplesassertion.list.sampletype"/></th>
					<th><insta:ltext key="laboratory.pendingsamplesassertion.list.sampleidheader"/></th>
					<th><insta:ltext key="laboratory.pendingsamplesassertion.list.orginalsampleno"/></th>
					<insta:sortablecolumn name="sample_date" title="Sample Date" />
					<th><insta:ltext key="laboratory.pendingsamplesassertion.list.qty"/> <insta:ltext key="laboratory.pendingsamplesassertion.list.delivered"/></th>
					<th><insta:ltext key="laboratory.pendingsamplesassertion.list.qty"/> <insta:ltext key="laboratory.pendingsamplesassertion.list.recd"/></th>
					<th><insta:ltext key="laboratory.pendingsamplesassertion.list.department"/></th>
					<th><insta:ltext key="laboratory.pendingsamplesassertion.list.outhouse"/></th>
					<th><insta:ltext key="laboratory.pendingsamplesassertion.list.testname"/></th>
					<th><insta:ltext key="laboratory.pendingsamplesassertion.list.collectioncenter"/></th>
					<th></th>

				</tr>
				<c:set var="idx" value="0"/>
				<c:forEach items="${samplesList}" var="sample" varStatus="st">
				<c:set var="hasResults" value="${not empty samplesList}"/>
				<c:set var="disableCheck" value="${sample.map.sample_status == 'C'}"/>
					<tr id="toolbarRow${st.index}">
						<td>
						<c:set var="flagColor">
								<c:choose>
								    <c:when test="${sample.map.sample_status == 'C' && (sample.map.visit_type == 't' && (sample.map.bill_status ne 'A'|| sample.map.charge_head eq 'PKGPKG'))}">
								    grey</c:when>
									<c:when test="${sample.map.sample_status == 'C'}">empty</c:when>
									<c:when test="${sample.map.sample_status == 'A'}">green</c:when>
									<c:when test="${sample.map.sample_status == 'R'}">red</c:when>
								</c:choose>
							</c:set>
							<img src="${cpath}/images/${flagColor}_flag.gif"/>
						  </td>
						  <td>
							<input type="checkbox" name="assert" onclick="return setAsserted(this);" ${!disableCheck  ? 'disabled' : ''} />
							<input type="hidden" name="sample_sno" value="${sample.map.sample_sno }"/>
							<input type="hidden" name="received_by" value="${ifn:cleanHtmlAttribute(userId) }"/>
							<input type="hidden" name="asserted" value="N"/>
							<input type="hidden" name="sample_collection_id" value="${sample.map.sample_collection_id}"/>
							<input type="hidden" name="rejected" value="N"/>
							<input type="hidden" name="rejection_remarks"  value="${rejection_remarks}"/>
						</td>
						<td>
						<c:choose>
									<c:when test="${sample.map.sample_status == 'R' || sample.map.sample_status == 'A' || (sample.map.visit_type == 't' && (sample.map.bill_status ne 'A' || sample.map.charge_head eq 'PKGPKG'))}">
									<div id="btndisable${idx}">
										<a
											title='<insta:ltext key="laboratory.pendingsamplesassertion.list.samplereject" />'>
											<img src="${cpath}/icons/delete_disabled.gif" class="button"/>
										</a>
									</div>
									</c:when>
									<c:otherwise>
									<div id="btndelete${idx}">
										<a href="javascript:void(0)"
											title='<insta:ltext key="laboratory.pendingsamplesassertion.list.samplereject"/>'
											onclick='return showsampleRejectDialog(this);' >
											<img src="${cpath}/icons/delete.gif" class="button"/>
										</a>
									</div>
									<div id="btnundo${idx}" style="display:none;">
										<a href="javascript:void(0)"
											title='<insta:ltext key="laboratory.pendingsamplesassertion.list.samplereject"/>'
											onclick='setDeleteUndo(this);' >
											<img src="${cpath}/icons/undo_delete.gif" class="button"/>
										</a>
									</div>
									</c:otherwise>
						</c:choose>
                          <input type="hidden" name="rejecticon" value ="0" />
						</td>
						<td>
							${sample.map.mr_no }
						</td>
						<td>
							${sample.map.patient_id }
						</td>
						<td>
							${sample.map.sample_type }
						</td>
						<td>
							${sample.map.coll_sample_no }
						</td>
						<td>
							<c:if test="${empty sample.map.mr_no}">
								${sample.map.orig_sample_no }
							</c:if>
						</td>
						<td>
							<fmt:formatDate value="${sample.map.sample_date}" pattern="dd-MM-yyyy HH:mm"/>
						</td>
						<td>
							${sample.map.sample_qty}
							<input type="hidden" name="qty_delivered" id="qty_delivered" value="${sample.map.sample_qty}" />
						</td>
						<td>
						    <c:set var="qtyrecd" value="${sample.map.sample_status == 'A' ? sample.map.sample_qty : ''}" />
							<input type="text" name="qty_recd" id="qty_recd" class="number" value="${qtyrecd}" onkeypress="return enterNumOnly(event);" ${!disableCheck  ? 'disabled' : ''} />
						</td>
						<td>
							<insta:truncLabel value="${sample.map.ddept_name }" length="25"/>
						</td>
						<td>
							<insta:truncLabel value="${sample.map.outsource_name }" length="25"/>
						</td>
						<td>
							<c:choose>
								<c:when test="${sample.map.priority == 'S'}">
									<b><font color="#444444"><insta:truncLabel value="${sample.map.test_name }" length="30"/></font></b>
								</c:when>
								<c:otherwise>
									<insta:truncLabel value="${sample.map.test_name }" length="30"/>
								</c:otherwise>
							</c:choose>

						</td>
						<td>
							${sample.map.collection_center }
						</td>
						<td>
						<button id="sample_assertButton" name="sample_assertButton" title="<insta:ltext key='laboratory.pendingsamplesassertion.list.additionalinformation'/>" style="cursor:pointer;"
								onclick="showSampleAssertionDetailsDialog(${sample.map.sample_collection_id}, this);" type="button">..</button>
						</td>
					</tr>
					<c:set var="idx" value="${idx+1}"/>
				</c:forEach>
			</table>
		</div>
<table class="formtable" width="100%">
		<tr>
			<td>
				<div style="float: left; margin-top: 10px" style="display: ${hasResults ? 'block' : 'none'}" id="assert">
					<button type="button" accesskey="S" name = "assertbtn" onclick="validate();">
						<label><b><u><insta:ltext key="laboratory.pendingsamplesassertion.list.a"/></u></b><insta:ltext key="laboratory.pendingsamplesassertion.list.ssert"/></label>
					</button>&nbsp;
				</div>
				<div style="float: left; margin-top: 10px" id="reject">
					<button type="button" accesskey="S" name = "rejectbtn" onclick="rvalidate();">
						<label><b><u><insta:ltext key="laboratory.pendingsamplesassertion.list.r"/></u></b><insta:ltext key="laboratory.pendingsamplesassertion.list.eject"/></label>
					</button>&nbsp;
				</div>
				<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
					<div class="flag"><img src='${cpath}/images/empty_flag.gif'></div>
					<div class="flagText"><insta:ltext key="laboratory.pendingsamplesassertion.list.pending"/></div>
					<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
					<div class="flagText"><insta:ltext key="laboratory.pendingsamplesassertion.list.asserted"/></div>
					<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
					<div class="flagText"><insta:ltext key="laboratory.pendingsamplesassertion.list.rejected"/></div>
					<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
					<div class="flagText"><insta:ltext key="laboratory.pendingsamplesassertion.list.closedBill"/></div>
				</div>
			</td>
		</tr>
</table>

		<div style="float: right; margin-top: 10px">
			<table>
				<tr>
					<td><insta:ltext key="laboratory.pendingsamplesassertion.list.handoverby"/></td>
					<td>
						<insta:selectdb name="handover_by" table="u_user" valuecol="emp_username"
							displaycol="emp_username" style="width: 11em"
							values="${paramValues.hand_over_by}" class="noClass dropdown" filtered="true"
							filtercol="emp_status,hosp_user" filtervalue="A,Y" dummyvalue="${userId}" orderby="emp_username"/>
					</td>
				</tr>
			</table>
		</div>
	<div style="display:none" id="sampleRejectDialog">
<div class="bd">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="laboratory.pendingsamplesassertion.list.samplerejectionremarks"/></legend>
		<table >
		<tr><td>&nbsp;</td></tr>
			<tr>
				<td>
					<textarea name="srejection_remarks" id="srejection_remarks" rows="4" cols="25" ></textarea>
				</td>
			<tr><td>&nbsp;</td></tr>
			<tr>
				<td>
					<input type="button" name="btnNameOk" id="btnNameOk" value="Ok" onclick="setRejected(this);"/>
					<input type="button" name="btnNameX" value="Cancel" onclick="closesampleRejectDialog();"/>
					<input type="hidden" name="sampleRejectId" id="sampleRejectId"/>
				</td>
			</tr>
		</table>
	</fieldset>
</div>
</div>
	<div id="sampleAssertionDialog" style="display:none;">
			<div class="bd">
				<fieldset class="fieldSetBorder" >
					<legend class="fieldSetLabel"><insta:ltext key="laboratory.pendingsamplesassertion.list.pendingsamplesassertiondetails"/></legend>
					<table class="formtable" id="assertionDetailsTable">
						<tr>
							<td class="formlabel"><insta:ltext key="laboratory.pendingsamplesassertion.list.sampleno"/>:</td>
							<td>
								<label id="tdSampleno"></label>
							</td>
							<td class="formlabel" ><insta:ltext key="ui.label.mrno"/>:</td>
							<td>
								<label id="tdMrno"></label>
							</td>
						</tr>
						<tr>
							<td class="formlabel" ><insta:ltext key="ui.label.patient.name"/>:</td>
							<td>
								<label id="tdPatientName"></label>
							</td>
							<td class="formlabel" ><insta:ltext key="laboratory.pendingsamplesassertion.list.sampledate"/></td>
							<td>
								<label id="tdSampleDate"></label>
							</td>
						</tr>
						<tr>
							<td class="formlabel" ><insta:ltext key="laboratory.pendingsamplesassertion.list.orginalsampleno"/>:</td>
							<td>
								<label id="tdOrigSampleNo"></label>
							</td>
							<td class="formlabel" ><insta:ltext key="laboratory.pendingsamplesassertion.list.incominghospital"/></td>
							<td>
								<label id="tdIncomingHospital"></label>
							</td>
						</tr>
						<tr>
							<td class="formlabel" ><insta:ltext key="laboratory.pendingsamplesassertion.list.collectioncenter"/>:</td>
							<td>
								<label id="tdCollectionCenter"></label>
							</td>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="laboratory.pendingsamplesassertion.list.tests"/>:</td>
							<td colspan="3">
								<label id="tdtests"></label>
							</td>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="laboratory.pendingsamplesassertion.list.sampletransfertime"/>:</td>
							<td>
								<label id="tdTransferTime"></label>
							</td>
							<td class="formlabel"><insta:ltext key="laboratory.pendingsamplesassertion.list.samplereceivedtime"/>:</td>
							<td>
								<label id="tdReceiptTime"></label>
							</td>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="laboratory.pendingsamplesassertion.list.othertransferdetails"/>:</td>
							<td colspan="3">
								<label id="tdTransferDetails"></label>
							</td>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="laboratory.pendingsamplesassertion.list.otherreceiveddetails"/>:</td>
							<td colspan="3">
								<label id="tdReceiptDetails"></label>
							</td>
						</tr>
					</table>
				</fieldset>
			</div>
		</div>
	</form>
</body>
</html>
