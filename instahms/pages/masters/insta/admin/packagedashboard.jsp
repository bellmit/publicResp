<%@page import="org.apache.struts.Globals" %>
<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>

<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default") %>'/>
<c:set var="mod_adv_packages" value="${preferences.modulesActivatedMap.mod_adv_packages}"/>

<html>
<head>
	<title>Package List-Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="js" file="ajax.js" />
	<insta:link type="css" file="widgets.css" />
	<insta:link type="script" file="widgets.js" />
	<insta:link type="js" file="masters/packmaster.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />

	<style type="text/css">
	.status_A.type_P {background-color: #D9EABB;}
	.status_A.type_T {background-color: #C5D9A3;}
	.status_I.type_P {background-color: #D9EABB; color:grey;}
	.status_I.type_T {background-color: #C5D9A3; color:grey; }

    table.legend { border-collapse: collapse; margin-left: 6px; }
	table.legend td { border: 1px solid grey; padding: 2px 5px;}

	</style>

		<script type="text/javascript">
		var max_centers_inc_default = ${max_centers_inc_default};
		var loggedInCenterId = ${centerId};
		var toolbar = {
			Edit_Pack: {
				title: "Edit Package",
				imageSrc: "icons/Edit.png",
				href: 'pages/masters/insta/admin/PackagesMasterAction.do?_method=show',
				onclick: null,
				description: "Edit Package details"
				},
			Edit_Charge: {
				title: "Edit Charges",
				imageSrc: "icons/Edit.png",
				href: 'pages/masters/insta/admin/PackagesMasterAction.do?_method=getEditPackageCharges',
				onclick: null,
				description: "Edit Charges"
				},
			pack_applicability: {
				title: "Package Applicability",
				imageSrc : "icons/Edit.png",
				href: 'master/PackageApplicabilityAction.do?_method=getScreen',
				onclick: null,
				description : 'Package Applicability'
			},
			ClonePackage : {
				title : "Clone Package",
				imageSrc : "icons/Edit.png",
				href : "pages/masters/insta/admin/PackagesMasterAction.do?_method=show&clone_package=true",
				description : "Clone Package"
			}
		};
		function init()
		{
			createToolbar(toolbar);
			showFilterActive(document.PackageSearchForm);
			initOperationsAutocomplete();
		}
		function initOperationsAutocomplete(){
			var datasource = new YAHOO.widget.DS_JSArray(${packages});
			var operationAC = new YAHOO.widget.AutoComplete('package_name','packagenameAcContainer', datasource);
			operationAC.maxResultsDisplayed = 15;
			operationAC.allowBrowserAutocomplete = false;
			operationAC.prehighlightClassName = "yui-ac-prehighlight";
			operationAC.typeAhead = false;
			operationAC.useShadow = false;
			operationAC.minQueryLength = 1;
			operationAC.animVert = false;
			operationAC.autoHighlight = false;
	  	}
	  	function validateForm() {
	  		var applicable_for_sponsor = document.getElementsByName('_applicable_for_sponsor');
	  		var sponsor = document.getElementById('_sponsor').value;
	  		for (var i=0; i<applicable_for_sponsor.length; i++) {
	  			if (applicable_for_sponsor[i].checked && applicable_for_sponsor[i].value == 'specific' && sponsor == '') {
	  				alert("Please select the sponsor");
	  				return false;
	  			}
	  		}
	  		if (max_centers_inc_default > 1 && loggedInCenterId == 0) {
		  		var applicable_for_center = document.getElementsByName('_applicable_for_center');
		  		var center_id = document.getElementById('_center_id').value;
		  		for (var i=0; i<applicable_for_center.length; i++) {
		  			if (applicable_for_center[i].checked && applicable_for_center[i].value == 'specific' && center_id == '') {
		  				alert("Please select the Center");
		  				return false;
		  			}
		  		}
	  		}
	  		return true;
	  	}

	  	function changeValue(obj) {
	  		if(obj.checked) {
	  			document.PackageSearchForm.multi_visit_package.value = true;
	  		} else {
	  			document.PackageSearchForm.multi_visit_package.value = false;
	  		}
	  	}

	  	function selectAllPagePackages() {
	  		var listform = document.PackageSearchForm;
			var checked = listform.allPagePackages.checked;
			var length = listform.selectPackage.length;

			if (length == undefined) {
				listform.selectPackage.checked = checked;
			} else {
				for (var i=0;i<length;i++) {
					listform.selectPackage[i].checked = checked;
				}
			}
		}


	  	function selectAllCenters(){
			var selectedCenters = document.updateform.allCenters.checked;
			var centersLen = document.updateform.selectCenter.length;

			for (i=centersLen-1;i>=0;i--) {
				document.updateform.selectCenter[i].selected = selectedCenters;
			}

			if(!selectedCenters)
				document.updateform.all_centers_checkbox.checked = true;
			else
				document.updateform.all_centers_checkbox.checked = false;

		}

		function deselectListBox(obj,lsitBoxName) {
			if(obj.checked) {
				if(lsitBoxName == "center") {
					document.updateform.allCenters.checked = false;
					selectAllCenters();
				} else {
					document.updateform.allSponsors.checked = false;
					selectAllSponsors();
				}
			} else {
				if(lsitBoxName == "center") {
					document.updateform.allCenters.checked = true;
					selectAllCenters();
				} else {
					document.updateform.allSponsors.checked = true;
					selectAllSponsors();
				}
			}
		}

		function deselectAllCenters(){
			document.updateform.allCenters.checked = false;
			document.updateform.all_centers_checkbox.checked = false;
		}

	  	function selectAllSponsors(){
			var selectedSponsors = document.updateform.allSponsors.checked;
			var sponsorsLen = document.updateform.selectSponsor.length;

			for (i=sponsorsLen-1;i>=0;i--) {
				document.updateform.selectSponsor[i].selected = selectedSponsors;
			}

			if(!selectedSponsors)
				document.updateform.all_sponsors_checkbox.checked = true;
			else
				document.updateform.all_sponsors_checkbox.checked = false;
		}

		function deselectAllSponsors(){
			document.updateform.allSponsors.checked = false;
			document.updateform.all_sponsors_checkbox.checked = false;
		}

		function onChangeAllPackages() {
			var val = getRadioSelection(document.updateform.allPackages);
			// if allPackages = yes, then disable the page selections
			var disabled = (val == 'yes');

			var listform = document.PackageSearchForm;
			listform.allPagePackages.disabled = disabled;
			listform.allPagePackages.checked = false;

			var length = listform.selectPackage.length;

			if (length == undefined) {
				listform.selectPackage.disabled = disabled;
				listform.selectPackage.checked  = false;
			} else {
				for (var i=0;i<length;i++) {
					listform.selectPackage[i].disabled = disabled;
					listform.selectPackage[i].checked = false;
				}
			}
		}

		function doGroupUpdate() {
			var isCenterSelected = true;
			var updateform = document.updateform;
			var listform = document.PackageSearchForm;
			var anyPackages = false;
			var allPackages = getRadioSelection(document.updateform.allPackages);
			if (allPackages == 'yes') {
				anyPackages = true;
			} else {
				var div = document.getElementById("PackageListInnerHtml");
				while (div.hasChildNodes()) {
					div.removeChild(div.firstChild);
				}

				var length = listform.selectPackage.length;
				if (length == undefined) {
					if (listform.selectPackage.checked ) {
						anyPackages = true;
						div.appendChild(makeHidden("selectPackage", "", listform.selectPackage.value));
					}
				} else {
					for (var i=0;i<length;i++) {
						if (listform.selectPackage[i].checked){
							anyPackages = true;
							div.appendChild(makeHidden("selectPackage", "", listform.selectPackage[i].value));
						}
					}
				}
			}

			if (!anyPackages) {
				alert('Select at least one Package for updation');
				return;
			}

			var anyCenters = false;
			var allCenters = false;
			if(max_centers_inc_default > 1) {
				allCenters = document.updateform.all_centers_checkbox.checked;
				if (updateform.allCenters.checked) {
					anyCenters = true;
				} else {
					var centersLength = updateform.selectCenter.length;

					for (var i=0; i<centersLength ; i++) {
						if(updateform.selectCenter.options[i].selected){
							anyCenters = true;
							break;
						}
					}
				}
			} else {
				anyCenters = true;
				allCenters = true;
			}

			var anySponsors = false;
			var allSponsors = document.updateform.all_sponsors_checkbox.checked;
			if (updateform.allSponsors.checked) {
				anySponsors = true;
			} else {
				var sponsorsLength = updateform.selectSponsor.length;

				for (var i=0; i<sponsorsLength ; i++) {
					if(updateform.selectSponsor.options[i].selected){
						anySponsors = true;
						break;
					}
				}
			}

			if (!allCenters && !anyCenters) {
				isCenterSelected = false;
				alert('Select at least one center for updation');
				return ;
			}

			if (isCenterSelected && !allSponsors && !anySponsors) {
				alert('Select at least one sponsor for updation');
				return ;
			}
			updateform.submit();
		}

	</script>

</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<body onload="init();" class="yui-skin-sam">

	<h1>Package List</h1>

	<insta:feedback-panel/>

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"/>

	<form name="PackageSearchForm" method="GET">

		<input type="hidden" name="_method" id="method" value="getPackageListScreen"/>
		<input type="hidden" name="_searchMethod" value="getPackageListScreen"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search form="PackageSearchForm" optionsId="optionalFilter" closed="${hasResults}" validateFunction="validateForm()">

			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Rate Sheet:</div>
					<div class="sboFieldInput">
						<c:set var="orgid" value="${empty param.org_id ? 'ORG0001' : param.org_id}"/>
						<insta:selectdb name="org_id" id="org_id" value="${orgid}"
							table="organization_details" valuecol="org_id" orderby="org_name"
							displaycol="org_name" onchange="changeRate();"
							filtered="true" filtercol="status,is_rate_sheet" filtervalue="A,Y"/>

					</div>
				</div>
			</div>

			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table  class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">Package Name</div>
							<div class="sfField" style="height: 25px;">
								<div id="AutoComp">
									<input type="text" id="package_name" name="package_name" value="${ifn:cleanHtmlAttribute(param.package_name)}"/>
									<input type="hidden" name="package_name@op" value="ico" />
									<div id="packagenameAcContainer" style="width: 300px;"></div>
								</div>
							</div>
							<div style="clear: both"></div>
							<c:choose>
								<c:when test="${mod_adv_packages == 'Y'}">
									<div class="sfLabel">Approval Status:</div>
									<div class="sfField">
										<insta:checkgroup name="approval_status" opvalues="P,A,R" optexts="Created,Approved,Rejected" selValues="${paramValues.approval_status}"/>
										<input type="hidden" name="approval_status@op" value="in" />
									</div>
								</c:when>
								<c:otherwise >
									<input type="hidden" name="approval_status" value="A"/>
								</c:otherwise>
							</c:choose>
							<c:if test="${mod_adv_packages eq 'Y'}">
								<div class="sfLabel">Advance Packages:</div>
								<div class="sfField">
									<input type="checkbox" name="multi_visit_package" value="${param.multi_visit_package}" ${param.multi_visit_package ? 'checked' : ''}  onchange="changeValue(this)"> Multi Visit Package
									<input type="hidden" name="multi_visit_package@type" value="boolean" />
									<input type="hidden" name="multi_visit_package@cast" value="Y" />
								</div>
							</c:if>
						</td>
						<td>
							<div class="sfLabel">Type:</div>
							<div class="sfField">
								<insta:checkgroup name="package_type" opvalues="i,o,d" optexts="IP,OP,Diag" selValues="${paramValues.package_type}"/>
								<input type="hidden" name="package_type@op" value="in" />
							</div>
						</td>
						<td>
							<div class="sfLabel">Items:</div>
							<div class="sfField">
								<insta:checkgroup name="applicable" opvalues="true,false" optexts="Included Only,Excluded Only" selValues="${paramValues.applicable}"/>
								<input type="hidden" name="applicable@op" value="in" />
								<input type="hidden" name="applicable@type" value="boolean" />
							</div>
							<div class="sfLabel">Sponsors:</div>
							<div class="sfField">
								<insta:checkgroup name="_applicable_for_sponsor" opvalues="-1,0,specific" optexts="Sponsor & No Sponsor,No Sponsor Tieup,Specific Sponsor" selValues="${paramValues._applicable_for_sponsor}"/>
								<insta:selectdb name="_sponsor" id="_sponsor" table="tpa_master" displaycol="tpa_name" filtered="true" valuecol="tpa_id" orderby="tpa_name" dummyvalue="-- Select --"
									value="${param._sponsor}"/>
							</div>
							<c:if test="${max_centers_inc_default > 1 && centerId == 0}">
								<div class="sfLabel">Centers:</div>
								<div class="sfField">
									<insta:checkgroup name="_applicable_for_center" opvalues="-1,specific" optexts="No Center Tieup,Specific Center" selValues="${paramValues._applicable_for_center}"/>
									<select name="_center_id" id="_center_id" class="dropdown">
										<option value="">-- Select --</option>
										<c:forEach var="center" items="${centers}">
											<option value="${center.map.center_id}" ${param._center_id == center.map.center_id ? 'selected' : ''}>${center.map.center_name}</option>
										</c:forEach>
									</select>
								</div>
							</c:if>
						</td>
						<td>
							<div class="sfLabel">Service Sub Group:</div>
							<div class="sfField">
								<insta:selectdb id="service_sub_group_id" name="service_sub_group_id" value=""
								table="service_sub_groups" class="dropdown"   dummyvalue="-- Select --"
								valuecol="service_sub_group_id"  displaycol="service_sub_group_name" />
								<input type="hidden" name="service_sub_group_id@type" value="integer" />
							</div>
						</td>
						<td class="last">
							<div class="sfLabel">Status:</div>
							<div class="sfField">
								<insta:checkgroup name="package_active" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.package_active}"/>
								<input type="hidden" name="package_active@op" value="in" />
							</div>
						</td>
					</tr>
				</table>
			</div>
		</insta:search>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<c:forEach var="charge" items="${pagedList.dtoList}" varStatus="st">
					<c:choose>
							<c:when test="${st.index eq 0}">
								<tr onmouseover="hideToolBar();">
									<c:forEach var="bed" items="${charge}">
										<c:choose>
										<c:when test="${bed == 'GENERAL'}">
											<th style="width: 2em; overflow: hidden">GENERAL/OP</th>
										</c:when>
										<c:when test="${bed == 'Package Name'}">
											<th style="padding-top: 0px;padding-bottom: 0px;width:0px;">
												<input type="checkbox" name="allPagePackages" disabled onclick="selectAllPagePackages()"/>
											</th>
											<insta:sortablecolumn name="package_name" title="${bed}"/>
										</c:when>
										<c:otherwise>
											<th style="width: 2em; overflow: hidden">${bed}</th>
										</c:otherwise>
									</c:choose>
									</c:forEach>
								</tr>
							</c:when>
							<c:otherwise>
							<c:set var="packId" value="${charge[1]}"/>
							<c:set var="temp_type" value="${charge[3]}"/>
							<c:set var="isMultiVisitPackage" value="false"/>
							<c:if test="${charge[5] == 't'}">
								<c:set var="isMultiVisitPackage" value="true"/>
							</c:if>
							<c:set var="editChargesEnabled" value="${ temp_type eq 'P'}" />
							<c:set var="clonePackageEnabled" value="${not isMultiVisitPackage}" />
							<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
								onclick="showToolbar(${st.index}, event, 'resultTable',
									{packId: '${packId}', org_id: '${ifn:cleanJavaScript(orgid)}', multi_visit_package: '${isMultiVisitPackage}'},[true, ${editChargesEnabled},true, ${clonePackageEnabled}]);" id="toolbarRow${st.index}">

								<c:set var="colCount" value="0" />
								<c:set var="stat" value="${charge[0]}"/>
								<c:set var="applicable" value="${charge[4]}"/>
								<c:forEach var="charges" items="${charge}">
									<c:choose>
										<c:when test="${colCount eq 0}">
											<c:set var="colCount" value="1" />
											<c:set var="status" value="${charges}" />
										</c:when>
										<c:when test="${colCount eq 1}">
											<c:set var="colCount" value="2" />
											<c:set var="packId" value="${charges}" />
										</c:when>
										<c:when test="${colCount eq 2}">
											<td>
												<input type="checkbox" name="selectPackage" disabled value="${packId}">
											</td>
											<td>
												<c:if test="${stat=='A' && temp_type=='T' && applicable=='t'}"><img src="${cpath}/images/yellow_flag.gif"></c:if>
												<c:if test="${stat=='A' && temp_type=='P' && applicable=='t'}"><img src="${cpath}/images/empty_flag.gif"></c:if>
												<c:if test="${stat=='I' && temp_type=='T'}"><img src="${cpath}/images/blue_flag.gif"></c:if>
												<c:if test="${stat=='I' && temp_type=='P'}"><img src="${cpath}/images/grey_flag.gif"></c:if>
												<c:if test="${stat=='A' && temp_type=='T' && applicable=='f'}"><img src="${cpath}/images/purple_flag.gif"></c:if>
												<c:if test="${stat=='A' && temp_type=='P' && applicable=='f'}"><img src="${cpath}/images/purple_flag.gif"></c:if>
												${charges}
											</td>
											<c:set var="colCount" value="3" />
											<c:set var="testName" value="${charges}" />
										</c:when>
										<c:when test="${(colCount eq 3)}">
											<c:set var="colCount" value="4" />
											<c:set var="type" value="${charges}"/>

										</c:when>
										<c:when test="${(colCount eq 4)}">
											<c:set var="colCount" value="5" />
											<c:set var="appl" value="${charges}"/>
										</c:when>
										<c:when test="${(colCount eq 5)}">
											<c:set var="colCount" value="6" />
											<c:set var="multiVisitPackage" value="${charges}"/>
										</c:when>
										<c:when test="${(colCount eq 6 ) or (colCount eq 7) or (colCount eq 8)}">
											<td class="number" align="right">${charges}</td>
											<c:set var="colCount" value="${colCount + 1}" />
										</c:when>
										<c:when test="${colCount eq 9}">
											<td class="number" align="right">${charges}</td>
											<c:set var="colCount" value="${colCount + 1}" />
										</c:when>
										<c:otherwise>
											<td class="number" align="right">${charges}</td>
										</c:otherwise>
									</c:choose>
								</c:forEach>
							</tr>
							</c:otherwise>
						</c:choose>
				</c:forEach>
			</table>

			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

		</div>
		<br/>
		<div id="content">
			<h1>
				Package Creation Job status
			</h1>
			<div class="resultList" >
				<table class="resultList" id="schedulerTable">
				<tr onmouseover="hideToolBar();">
					<th>ID</th>
					<th>Package ID</th>
					<th>Status</th>
					<th>Error</th>
					<th>Retry</th>
				</tr>
				<c:forEach var="record" items="${masterCronJobDeatils}">
					<tr>
						<td>${record.id}</td>
						<td>${record.entity_id}</td>
						<td id="entity_status_${record.entity_id}">${record.status == 'P'? 'Processing': record.status == 'S'? 'Success': 'Failed'}</td>
						<td id="error_status_${record.entity_id}">${record.error_message}</td>
						<td id="retry_job_${record.entity_id}"><c:if test="${record.status == 'F'}">
							<button type="button" onclick="retryJobSchedule('${record.entity_id}');">
							<b><u>R</u></b>etry</button>
							</c:if></td>
					</tr>
				</c:forEach>
				</table>
			</div>

			<c:if test="${empty masterCronJobDeatils}">
				<insta:noresults hasResults="${not empty masterCronJobDeatils}"/>
			</c:if>
		</div>
		<div class="screenActions" style="float:left">
			<a href="${cpath}/pages/masters/insta/admin/PackagesMasterAction.do?_method=add&package_type=o&multi_visit_package=false">Add OP Package</a>
			| <a href="${cpath}/pages/masters/insta/admin/PackagesMasterAction.do?_method=add&package_type=i&multi_visit_package=false">Add IP Package</a>
			| <a href="${cpath}/pages/masters/insta/admin/PackagesMasterAction.do?_method=add&package_type=d&multi_visit_package=false">Add Diag Package</a>
			<c:if test="${mod_adv_packages == 'Y'}">
				| <a href="${cpath}/pages/masters/insta/admin/PackagesMasterAction.do?_method=add&package_type=o&multi_visit_package=true">Add Multi Visit Package</a>
			</c:if>
		</div>

		<div class="legend" style="${hasResults?'display':'none'}">
			<div class="flag"><img src="${cpath}/images/empty_flag.gif"></div>
			<div class="flagText">Active Package</div>
			<div class="flag"><img src="${cpath}/images/grey_flag.gif"></div>
			<div class="flagText">Deactivated Package</div>
			<div class="flag"><img src="${cpath}/images/purple_flag.gif"></div>
			<div class="flagText">Excluded</div>
		</div>

</form>
<div class="resultList">
<div id="CollapsiblePanel1" class="CollapsiblePanel">
	    	<div class=" title CollapsiblePanelTab" tabindex="0" style=" border-left:none;">
	        	<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">Group Update</div>
				<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;"><img src="${cpath}/images/down.png" /></div>
				<div class="clrboth"></div>
			</div>

		<table>
			<tr>
				<td valign="top">
					<form name="updateform" action="PackagesMasterAction.do" method="POST">
						<input type="hidden" name="_method" value="updatePackgeCentersAndSponsors">

						<div style="display:none" id="PackageListInnerHtml">
							<%-- this holds the hidden inputs for the list of selected services --%>
						</div>

						<table class="search">
							<tr>
								<th>Select Packages</th>
								<c:if test="${max_centers_inc_default > 1}">
									<th colspan="2">Select Centers</th>
								</c:if>
								<th colspan="2">Select Sponsors</th>
							</tr>

							<tr>
								<td>
									<input type="radio" checked name="allPackages" onclick="onChangeAllPackages()" value="yes">
									All Packages <br>
									<input type="radio" name="allPackages" onclick="onChangeAllPackages()" value="no">
									Selected Packages
								</td>
								<c:if test="${max_centers_inc_default > 1}">
									<td>
										<input type="checkbox" name="all_centers_checkbox" checked align="top" onclick="deselectListBox(this,'center')"/>All
									</td>
									<td style="padding-left: 1em">
										<select multiple="true" size="10" name="selectCenter">
											<c:forEach items="${centers}" var="center">
												<option value="${center.map.center_id}" onclick="deselectAllCenters();">${center.map.center_name}</option>
											</c:forEach>
										</select>
										<br/>
										<input type="checkbox" name="allCenters" value="yes"
												onclick="selectAllCenters()"/>Select All
									</td>
								</c:if>
								<td>
									<input type="checkbox" name="all_sponsors_checkbox" checked align="top" onclick="deselectListBox(this,'sponsors');"/>All
								</td>
								<td style="padding-left: 1em">
										<select multiple="true" size="10" name="selectSponsor">
											<c:forEach items="${sponsors}" var="sponsor">
												<option value="${sponsor.map.tpa_id}" onclick="deselectAllSponsors();">${sponsor.map.tpa_name}</option>
											</c:forEach>
										</select>
									<br/>
									<input type="checkbox" name="allSponsors" value="yes"
											onclick="selectAllSponsors()"/>Select All
								</td>
							</tr>
						</table>
					</form>
				</td>
			</tr>
			<tr>
				<td colspan="3"  style="vertical-align: bottom;">
					<button type="button" accesskey="C" onclick="return doGroupUpdate()">
					Update <b><u>C</u></b>hanges</button>
				</td>
			</tr>
		</table>
	</div>
</div>
<script type="text/javascript">
	var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen: false});
</script>
</script>
</body>
</html>
