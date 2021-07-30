<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.stores.StoresDBTablesUtil"%>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<jsp:useBean id="pbmStatusDisplay" class="java.util.HashMap"/>
<c:set target="${pbmStatusDisplay}" property="O" value="Open"/>
<c:set target="${pbmStatusDisplay}" property="S" value="Sent"/>
<c:set target="${pbmStatusDisplay}" property="D" value="Denied"/>
<c:set target="${pbmStatusDisplay}" property="R" value="ForResub"/>
<c:set target="${pbmStatusDisplay}" property="C" value="Closed"/>

<jsp:useBean id="prescStatus" class="java.util.HashMap"/>
<c:set target="${prescStatus}" property="Y" value="Fully Dispensed"/>
<c:set target="${prescStatus}" property="P" value="Partially Dispensed"/>
<c:set target="${prescStatus}" property="N" value="Not Dispensed"/>

<jsp:useBean id="erxApprovalStatus" class="java.util.HashMap"/>
<c:set target="${erxApprovalStatus}" property="F" value="Fully Approved"/>
<c:set target="${erxApprovalStatus}" property="R" value="Fully Rejected"/>
<c:set target="${erxApprovalStatus}" property="P" value="Partially Approved"/>

<c:set var="hasPbmFinalizeRights" value="${((actionRightsMap.pbm_prescription_finalize == 'A')||(roleId==1)||(roleId==2))}"/>
<c:set var="hasERxConsAccessRights" value="${((actionRightsMap.erx_consultation_access == 'A')||(roleId==1)||(roleId==2))}"/>

<c:set var="excludePBM" value="${mod_eclaim_erx && !mod_eclaim_pbm}"/>

<html>
<head>
	<title>${excludePBM?'ERx':'PBM'} Prescriptions List - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<script>
		var docList = <%= StoresDBTablesUtil.getNamesInJSON(StoresDBTablesUtil.DOCTORS) %>;
		var hasPbmFinalizeRights = '${hasPbmFinalizeRights}';
		var hasERxConsAccessRights = '${hasERxConsAccessRights}';
		var mod_elaim_pbm_enabled = '${mod_eclaim_pbm}' == 'true' ? 'Y' : 'N';
		var mod_elaim_erx_enabled = '${mod_eclaim_erx}' == 'true' ? 'Y' : 'N';
	</script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="billing/claimsCommon.js"/>
	<insta:link type="script" file="PBMAuthorization/pbmPrescriptions.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<script>
		var companyList    = ${insCompList};
		var categoryList   = ${insCategoryList};
		var tpaList        = ${tpaList};
		var planList       = ${planList};
		var extraDetails   = [];
	</script>
</head>

<c:set var="prescList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty prescList}"/>

<body onload="init(); showFilterActive(document.pbmListSearchForm);">
<h1>${excludePBM?'ERx':'PBM'} Prescriptions List</h1>

<insta:feedback-panel/>
<c:if test="${empty error}">
<form name="pbmListSearchForm" method="GET">
	<input type="hidden" name="_method"  id ="_method" value="getList">
	<input type="hidden" name="_searchMethod" value="getList"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="pbmListSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
		<div class="searchBasicOpts" >
		  	<div class="sboField">
				<div class="sboFieldLabel">MR No/Patient Name:</div>
				<div class="sboFieldInput">
					<div id="mrnoAutoComplete">
						<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
						<div id="mrnoContainer" style="width: 300px"></div>
					</div>
				</div>
			</div>
			<div class="sboField" style="width: 300px">
				<div class="sboFieldLabel">Doctor:</div>
				<div class="sboFieldInput" style="width: 300px">
					<div id="doctorAutoComplete">
						<input type="text" name="doctor"  id="doctor" value="${ifn:cleanHtmlAttribute(param.doctor)}"/>
					    <div id="doc_dropdown" style="width: 300px"></div>
			      </div>
		      </div>
		   </div>
		   <c:if test="${mod_eclaim_pbm && hasPbmFinalizeRights}">
		   <div class="sboField">
				<div class="sboFieldLabel">Presc. Store:</div>
				<div class="sboFieldInput">
					<c:choose>
						<c:when test="${roleId ==1 || roleId==2 || (multiStoreAccess == 'A')}">
							<insta:userstores username="${userid}" elename="pbm_store_id"
								 showDefaultValueForNormalUsers='Y' defaultVal="-- Select --"
								 id="pbm_store_id" onlySalesStores="Y" storesWithTariff="Y" val="${param.pbm_store_id}"/>
						</c:when>
						<c:otherwise>
							<insta:userstores username="${userid}" elename="pbm_store_id"
							    showDefaultValueForNormalUsers='Y' defaultVal="-- Select --"
								 id="pbm_store_id" onlySalesStores="Y" storesWithTariff="Y" val="${param.pbm_store_id}"/>
						</c:otherwise>
					</c:choose>
					<input type="hidden" name="pbm_store_id@type" value="integer"/>
				</div>
		   </div>
		   </c:if>
	   </div>
	 	<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Consultation Date:</div>
						<div class="sfField">
							<div class="sfFieldSub">From: </div>
							<insta:datewidget name="visited_date" id="visited_date0" value="${paramValues.visited_date[0]}"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub">To: </div>
							<insta:datewidget name="visited_date" id="visited_date1" value="${paramValues.visited_date[1]}"/>
							<input type="hidden" name="visited_date@op" value="ge,le"/>
							<input type="hidden" name="visited_date@cast" value="y"/>
						</div>
						<div class="sfLabel">Patient Status:</div>
						<div class="sfField">
							<insta:checkgroup name="patstatus" selValues="${paramValues.patstatus}"
							opvalues="A,I" optexts="Active,Inactive"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Ins. Company Name:</div>
						<div class="sfField">
							<insta:selectdb displaycol="insurance_co_name" name="insurance_co_id" id="insurance_co_id" value="${param.insurance_co_id}"
							table="insurance_company_master" orderby="insurance_co_name"
							valuecol="insurance_co_id" dummyvalue="(All)" onchange="onChangeInsuranceCompany()"/>
						</div>
						<div class="sfLabel">Network/Plan Type:</div>
						<div class="sfField">
							<c:set var="inscatSelected" value="${fn:join(paramValues.category_id, ' ')}"/>
							<select name="category_id" id="category_id"  multiple="multiple" class="listbox" optionTitle="true"
										style="border-top:1px #999 solid; border-left:1px #999 solid; border-bottom:1px #ccc solid; border-right:1px #ccc solid;"
										onchange="onChangeInsuranceCategory()" >
								<option value="(All)">(All)</option>
								<c:forEach items="${inscatName}" var="inscat" >
									<c:set var="selected" value="${fn:contains(inscatSelected,inscat.map.category_id)?'selected':''}"/>
									<option value="${inscat.map.category_id}" ${selected}>${inscat.map.category_name}</option>
								</c:forEach>
									<input type="hidden" name="category_id@type" value="text"/>
							</select>
						</div>
					</td>
					<td>
						<div class="sfLabel">TPA Name:</div>
						<div class="sfField">
							<insta:selectdb displaycol="tpa_name" name="tpa_id" id="tpa_id" value="${param.tpa_id}"
							table="tpa_master" orderby="tpa_name"
							valuecol="tpa_id" dummyvalue="(All)" onchange="onChangeTPA()"/>
						</div>
						<div class="sfLabel">Plan Name:</div>
						<div class="sfField">
							<insta:selectdb displaycol="plan_name" name="plan_id" id="plan_id" value="${param.plan_id}"
							table="insurance_plan_main" orderby="plan_name"
							valuecol="plan_id" dummyvalue="(All)"/>
							<input type="hidden" name="plan_id@type" value="integer"/>
						</div>
						<c:if test="${mod_eclaim_pbm}">
							<div class="sfLabel">Finalized:</div>
							<div class="sfField">
								<insta:checkgroup name="pbm_finalized" selValues="${paramValues.pbm_finalized}"
								opvalues="Y,N" optexts="Yes,No"/>
							</div>
						</c:if>
					</td>
					<td>
						<c:if test="${mod_eclaim_pbm}">
							<div class="sfLabel">PBM Presc. Status:</div>
							<div class="sfField">
								<insta:checkgroup name="pbm_presc_status" selValues="${paramValues.pbm_presc_status}"
								opvalues="O,S,D,R,C" optexts="Open,Sent,Denied,ForResub,Closed"/>
							</div>
						</c:if>
						<c:if test="${mod_eclaim_erx}">
							<div class="sfLabel">ERx Reference No.</div>
							<div class="sfField">
								<input type="text" name="erx_reference_no" value="${param.erx_reference_no}">
								<input type="hidden" name="erx_reference_no@op" value="ico"/>
							</div>
						</c:if>
						<c:if test="${mod_eclaim_pbm}">
							<div class="sfLabel">PBM Presc. ID: </div>
							<div class="sfField">
								<input type="text" name="pbm_presc_id" value="${param.pbm_presc_id}">
								<input type="hidden" name="pbm_presc_id@type" value="integer"/>
							</div>
						</c:if>
					</td>
					<td>
						<div class="sfLabel">Prescription Status</div>
						<div class="sfField">
							<insta:checkgroup name="presc_status" selValues="${paramValues.presc_status}"
							opvalues="N,P,Y" optexts="Not Dispensed,Partially Dispensed,Fully Dispensed"/>
						</div>
						<c:if test="${mod_eclaim_erx}">
							<div class="sfLabel">ERx Approval Status</div>
							<div class="sfField">
								<insta:checkgroup name="erx_approval_status" selValues="${paramValues.erx_approval_status}"
								opvalues="F,R,P" optexts="Fully Approved,Fully Rejected,Partially Approved"/>
							</div>
							<div class="sfLabel">ERx Presc. ID</div>
							<div class="sfField">
								<input type="text" name="erx_presc_id" value="${param.erx_presc_id}">
								<input type="hidden" name="erx_presc_id@op" value="ico"/>
							</div>
						</c:if>
					</td>
				</tr>
			</table>
	   </div>
	</insta:search>
 </form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"  showTooltipButton="true"/>
	<form method="POST" name="pbmFinalizeForm" action="./PBMPrescList.do">
	<input type="hidden" name="_method" value=""/>
		
		<c:if test="${mod_eclaim_erx}">
			<button type="button" name="checkNewERxAuths" value="searchERxAuth"
					accessKey="R" onclick="return erxApprovalSearch();">
			<img class="flag" src="${cpath}/icons/Refresh.png"/>
			<b><u>R</u></b>efresh
			</button>
		</c:if>
		
	<c:if test="${mod_eclaim_pbm && hasPbmFinalizeRights}">
		<c:if test="${genericPrefs.prescription_uses_stores == 'Y' && !prescriptions_by_generics}">
		<div>
		<c:choose>
			<c:when test="${roleId ==1 || roleId==2 || (multiStoreAccess == 'A')}">
				<insta:userstores username="${userid}" elename="_phStore" val="${dept_id}"
					 id="_phStore" onlySalesStores="Y" storesWithTariff="Y"/>
			</c:when>
			<c:otherwise>
				<b>${ifn:cleanHtml(dept_name)}</b>
				<input type = "hidden" name="_phStore" id="_phStore" value="${ifn:cleanHtmlAttribute(dept_id)}" />
			</c:otherwise>
		</c:choose>
		<div id="storeErrorDiv" style="display:none;white-space:nowrap;color:red;">
		<div class="clrboth"></div>
		<div class="fltL" style="width: 15px; margin:0 0 0 0px;"> <img src="${cpath}/images/error.png" /></div>
			<div class="fltL" style="margin:5px 0 0 5px; width:160px;">
				User has No Store With Tariff
			</div>
		</div>
		&nbsp;<button type="submit" name="updateStore" accesskey="P" id="updateStoreBtn"
			class="button" onclick="return validateStore()">
		Save <b><u>P</u></b>BM Presc. Store</button>
		<img class="imgHelpText" title="Save PBM Presc. Store will update the store, rate, discount, claim and co-pay amounts
for the selected PBM prescriptions with this store. No changes are done if a prescription has a store."
		 src="${cpath}/images/help.png"/>
		</div>
		</c:if>
	</c:if>

		<div class="resultList">
			<table class="resultList dialog_displayColumns" cellspacing="0" cellpadding="0" id="resultTable"">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<c:if test="${mod_eclaim_pbm && hasPbmFinalizeRights}">
						<th><input type="checkbox" name="_allPrescriptions" onclick="checkAllPrescriptions();"/></th>
					</c:if>
					<insta:sortablecolumn name="patient_id" title="Visit ID"/>
					<insta:sortablecolumn name="pbm_presc_id" title="PBM presc. ID"/>
					<c:if test="${mod_eclaim_erx}">
						<th>ERx Presc. ID</th>
						<th>ERx Ref. No</th>
						<th>ERx Approval</th>
						<th>ERx Request Type</th>
					</c:if>
					<c:if test="${mod_eclaim_pbm && hasPbmFinalizeRights}">
						<th>Store</th>
						<th>Finalized</th>
						<th>PBM Presc. Status</th>
					</c:if>
					<th>Patient Name</th>
					<th>Visited Date</th>
					<insta:sortablecolumn name="tpa_name" title="TPA/Sponsor"/>
					<insta:sortablecolumn name="category_name" title="Network/Plan Type"/>
					<c:if test="${mod_eclaim_pbm && hasPbmFinalizeRights}">
						<th>Resubmit Type</th>
					</c:if>
					<th>Prescription Status</th>
				</tr>
				<c:forEach var="presc" items="${prescList}" varStatus="st">
				<c:set var="i" value="${st.index + 1}"/>
				<c:set var="flagColor">
					<c:choose>
						<c:when test="${presc.patstatus eq 'I'}">grey</c:when>
						<c:otherwise>empty</c:otherwise>
					</c:choose>
				</c:set>
				<c:set var="pbmFlagColor">
					<c:choose>
						<c:when test="${presc.pbm_presc_status eq 'O'}">empty</c:when>
						<c:when test="${presc.pbm_presc_status eq 'S'}">violet</c:when>
						<c:when test="${presc.pbm_presc_status eq 'D'}">red</c:when>
						<c:when test="${presc.pbm_presc_status eq 'R'}">green</c:when>
						<c:when test="${presc.pbm_presc_status eq 'C'}">grey</c:when>
					</c:choose>
				</c:set>
				<c:set var="erxFlagColor">
					<c:choose>
						<c:when test="${presc.erx_approval_status eq 'F'}">green</c:when>
						<c:when test="${presc.erx_approval_status eq 'R'}">red</c:when>
						<c:when test="${presc.erx_approval_status eq 'P'}">yellow</c:when>
					</c:choose>
				</c:set>

				<c:set var="receiveEnable" value="${not empty presc.erx_reference_no}"/>

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{pbm_presc_id: ${presc.pbm_presc_id}, consultation_id: ${presc.consultation_id},visit_id: '${presc.patient_id}' },
							[true, true, ${receiveEnable},true]);"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1}</td>
					<c:if test="${mod_eclaim_pbm && hasPbmFinalizeRights}">
						<td>
							<c:choose>
								<c:when test="${presc.pbm_finalized eq 'N'}">
									<input type="checkbox" name="_pbm_presc_id" value="${presc.pbm_presc_id}"/>
								</c:when>
								<c:otherwise>
									<input type="checkbox" name="_pbm_presc_id" value="" disabled/>
								</c:otherwise>
							</c:choose>
						</td>
					</c:if>
					<td><img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>${presc.patient_id}</td>
					<td>${presc.pbm_presc_id }</td>
					<c:if test="${mod_eclaim_erx}">
						<td><insta:truncLabel value="${presc.erx_presc_id}" length="15"/></td>
						<td>${presc.erx_reference_no}</td>
						<td>
							<c:choose>
							<c:when test="${not empty presc.erx_approval_status}">
								<img class="flag" src="${cpath}/images/${erxFlagColor}_flag.gif"/>${erxApprovalStatus[presc.erx_approval_status]}
							</c:when>
							<c:when test="${not empty presc.erx_presc_id && not empty presc.erx_reference_no}">
								Pending...
							</c:when>
							</c:choose>
						</td>
						<td>${presc.erx_request_type}</td>
					</c:if>
					<c:if test="${mod_eclaim_pbm && hasPbmFinalizeRights}">
						<td>${presc.pbm_store_name }</td>
						<td>${presc.pbm_finalized eq 'Y' ? 'Yes' : 'No'}</td>
						<td><img class="flag" src="${cpath}/images/${pbmFlagColor}_flag.gif"/>${pbmStatusDisplay[presc.pbm_presc_status]}</td>
					</c:if>
					<td>${presc.patname}</td>
				   <td><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${presc.visited_date}"/></td>
				   <td><insta:truncLabel value="${presc.tpa_name}" length="15"/></td>
					<td><insta:truncLabel value="${presc.category_name}" length="15"/></td>
					<c:if test="${mod_eclaim_pbm && hasPbmFinalizeRights}">
				  		<td>${presc.resubmit_type}</td>
				   </c:if>
				   <td>${prescStatus[presc.presc_status]}</td>
				</tr>
				<script>
					extraDetails['toolbarRow${st.index}'] = {
						'MR No': '${presc.mr_no}',
						'Patient Id': '${presc.patient_id}',
						'Age/Gender' : '${presc.age} ${presc.age_in}/${presc.patient_gender}',
						'Mobile No.': '${presc.patient_phone}',
						'Doctor' : <insta:jsString value="${presc.doctor}"/>
					};
				</script>
				</c:forEach>
			</table>
			<c:if test="${param._method == 'getList'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>
	    </div>
</form>

	<table class="screenActions">
		<tr>
			<td>
				<c:if test="${mod_eclaim_pbm && hasPbmFinalizeRights}">
					<button type="submit" name="finalize" accesskey="Z" class="button" onclick="return validateFinalize()">
					Finali<b><u>Z</u></b>e</button>&nbsp;
					<label>|</label>
					<a href="javascript:void(0);" onclick="clearForm(document.pbmFinalizeForm);">Clear</a>
					<label>|</label>
				</c:if>
				<c:if test="${!mod_eclaim_erx && (mod_eclaim_pbm && hasPbmFinalizeRights)}">
					<a target="#" href="./PBMPresc.do?_method=getPBMPrescriptionScreen">Add New PBM Prescription</a>
				</c:if>
				<insta:screenlink addPipe="true" screenId="pbm_requests" label="PBM Requests"
						extraParam="?_method=getRequests&pbm_finalized=Y&pbm_presc_status=O&pbm_presc_status=R"/>
			</td>
		 </tr>
	</table>

	<c:choose>
	<c:when test="${mod_eclaim_pbm && hasPbmFinalizeRights}">
		<div class="legend" style="display:${hasResults ? 'block' : 'none'}">
			<div class="flag"><img src="${cpath}/images/empty_flag.gif"></img></div>
			<div class="flagText">Open</div>
			<div class="flag"><img src="${cpath}/images/violet_flag.gif"></img></div>
			<div class="flagText">Sent</div>
			<div class="flag"><img src="${cpath}/images/red_flag.gif"></img></div>
			<div class="flagText">Denied</div>
			<div class="flag"><img src="${cpath}/images/green_flag.gif"></img></div>
			<div class="flagText">ForResub.</div>
			<div class="flag"><img src="${cpath}/images/grey_flag.gif"></img></div>
			<div class="flagText">Closed</div>
		</div>
	</c:when>
	<c:when test="${mod_eclaim_erx}">
		<div class="legend" style="display:${hasResults ? 'block' : 'none'}">
			<div class="flag"><img src="${cpath}/images/green_flag.gif"></img></div>
			<div class="flagText">Approved</div>
			<div class="flag"><img src="${cpath}/images/red_flag.gif"></img></div>
			<div class="flagText">Rejected</div>
		</div>
	</c:when>
	</c:choose>
</c:if>
</body>
</html>
