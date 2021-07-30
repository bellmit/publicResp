<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page pageEncoding="UTF-8"  isELIgnored="false"%>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<%@ page import="com.bob.hms.common.DataBaseUtil" %>
<%--

Steps to make the JSP i18n compliant

Standard checks to be done
===========================

1. 	Set the pageEncoding attribute to "UTF-8" in the @page directive

2. 	Make sure all the external javascript files included in the page use the <insta:link> tag.
	If <script src=....> must be used for some reason, then make sure you include the
	charset="UTF8" attribute. E.g

		<script type="text/javascript" src="${cpath}/scripts/myscript.js" charset="UTF-8"/>

Changes to the JSP file
===========================

3. Declare the page as i18n compliant by putting a <meta> tag in the header as shown below. This will let the
decorator switch to appropriate styles for the chosen language.

	<meta name="i18nSupport" content="true"/>

All the display strings will be replaced with a key from the application.properties file.

3. 	Pick a prefix to be used for the resource key, specific to this page.
	E.g. Say "search.patient.visit" for this page.

4. 	Make an entry for each String used in the JSP in application.properties file.
	The key should be of the format <page-prefix>.<anything-meaningful>. The value should be
	set to the text that is used in the page.

		E.g  :
		patient.visit.search.heading=Search Patient Visits

5. 	Change all labels / column headings and other literal strings to the respective
	resource keys using the insta:ltext tag.
		E.g :
		<h1>Search Patient Visits</h1> should be replaced with
		<h1><insta:ltext key="patient.visit.search.heading"/></h2>

Note: Common components like insta:search which involve text labels (like More Options, My
Searches), should only be set once, in the resource file. These should not be repeated in each JSP

Changing the Strings in js files
========================================

6. 	Review the page specific javascript files for any hard-coded strings. If
	there are any strings to be internationalized, then include the js-bundle
	at the end of the <head> block. Specify the prefix as a parameter to
	the tag

		E.g. :
		<insta:js-bundle prefix="patient.visit.search"/>

	This will import all the javascript strings for the page.

7. 	Create an entry for each of the strings in application.properties file.
	Each of these strings should have the following format
		js.<page-prefix>.<string-specific-suffix>

	E.g :
	js.patient.visit.search.admission.fromdate.required=Please enter the admission from date

	Here, js.patient.visit.search is mandatory.
	Rest of the key can be any meaningful name to indicate the purpose.

8.	Change all the alert() calls to showMessage(). Use the key in the resource
	file as a parameter to showMessage().

	E.g :
	alert('Please enter the admission from date'); will be replaced with
	showMessage("js.patient.visit.search.admission.fromdate.required");

9.	Other hardcoded strings (other than alerts) in the js
	file should be replaced with getString(<resource.key>)

	E.g :
		var currentBed = 'Current Bed'; should be replaced with
		getString("js.patient.visit.search.current.bed");

10. If the page uses a Toolbar and the toolbar strings should be localized, the following changes are necessary.

	(a) Create resource keys for the toolbar strings in the following format, for each toolbar option

	js.<page-prefix>.<toolbar-prefix>.<optionkey> = Display name, Description
	E.g.,
	js.search.patient.visit.toolbar.editvisit = Edit Visit, Edit Visit Details
	js.search.patient.visit.toolbar.order = Order

	(b) Create a script block in the page which will get the resource bundle for the toolbar.

	<script>
		var toolbarOptions = getToolbarBundle("js.search.patient.visit.toolbar");
	</script>
	This will return a js object of the following format
	{
		editvisit : // This is the key that follows the prefix
			{ 	name : "Edit Visit", // This is the display string in the toolbar
				description : "Edit Visit Details" // This is the description string for the toolbar
			},
		order :
			{
				name : "Order",
				description: "",
			}
	}

	(c) This structure should be used in initailizing the toolbar
	E.g. :
	toolbarOptions["editvisit"]["name"]

--%>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
	<head>
		<title>Search Patient Visits - Insta HMS</title>
		<meta name="i18nSupport" content="true"/>
		<insta:link type="js" file="dashboardsearch.js"/>
		<insta:link type="js" file="visitdetailssearch/visitdetailssearch.js"/>
		<insta:link type="js" file="patientdetailssearch/patientdetailssearch.js"/>
		<insta:link type="css" file="widgets.css"/>
		<insta:link type="js" file="ajax.js"/>
		<style>
			.scrolForContainer .yui-ac-content{
				 max-height:11em;overflow:auto;overflow-x:hidden; /* scrolling */
			    _height:11em; /* ie6 */
			}
			.autocomplete {
				padding-bottom: 20px;
				width: 138px;
			}
			table.resultList tr.secondary td {
				border-top: none;
			}
			table.resultList td {
				height: 20px;
			}
		</style>
		<script src="${cpath}/VisitDetailsSearch/tpasponsoraction.do?_method=getdetails"></script>
		<script>
			var countryList = ${countryList};
			var stateList = ${stateList};
			var cityList = ${cityList};
		/*	var occupationList = ${occupationList};
	   	    var bloodgroupList = ${bloodgroupList};
			var religionList = ${religionList};*/
			var categoryList = ${categoryList};
			var wardNames = ${ward_names};
			var bedNames = ${bed_names};
			var bedTypes = ${bed_types};
			var orgNameJSONList = ${orgNameJSONList};
            var customList1 = ${customList1};
            var customList2 = ${customList2};
            var customList3 = ${customList3};
            var customList4 = ${customList4};
            var customList5 = ${customList5};
            var customList6 = ${customList6};
			var customList7 = ${customList7};
			var customList8 = ${customList8};
		    var customList9 = ${customList9};
			var contextPath = '${cpath}';
			var searchForm = document.visitSearchForm;
			var visitDetails = '${urlRightsMap["edit_visit_details"]}';
			var regGeneral = '${urlRightsMap["reg_general"]}';
			var dischargeSummary = '${urlRightsMap["discharge_summary"]}';
			var opOrder = '${urlRightsMap["new_op_order"]}';
			var ipOrder = '${urlRightsMap["new_ip_order"]}';
			var readmit = '${urlRightsMap["reg_re_admit"]}';
			var printPath = contextPath + "/MLCDocuments/MLCDocumentPrint.do";
			var mod_adt = "${preferences.modulesActivatedMap['mod_adt']}";
		</script>
		<insta:js-bundle prefix="search.patient.visit"/>
		<insta:js-bundle prefix="patient.details.toolbar.option.common"/>		
		<script>
			var toolbarOptions = getToolbarBundle("js.search.patient.visit.toolbar.option");
			var toolbarOptionsCommon = getToolbarBundle("js.patient.details.toolbar.option.common");			
		</script>
	</head>
	<body onload="init(document.visitSearchForm);initVisitToolbar(toolbarOptions);ajaxForPrintUrls();changeDatevalues();">
		<h1><insta:ltext key="search.patient.visit.heading"/></h1>
		<c:set var="visitsList" value="${pagedList.dtoList}"/>
		<c:set var="useInfinitePagination" value='<%=DataBaseUtil.isLargeDataset("patient_registration")%>'/>
		<c:set var="hasResults" value="${(useInfinitePagination && pagedList.pageNumber > 0) || not empty visitsList}"/>
		<c:set var="corpInsurance" value='<%=GenericPreferencesDAO.getAllPrefs().get("corporate_insurance")%>'/>

		<form action="" method="GET" name="visitSearchForm">
		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
		<input type="hidden" name="country" value="${ifn:cleanHtmlAttribute(param.country)}" />
		<input type="hidden" name="patient_state" value="${ifn:cleanHtmlAttribute(param.patient_state)}" />
		<input type="hidden" name="patient_city" value="${ifn:cleanHtmlAttribute(param.patient_city)}" />
		<input type="hidden" name="visit_reg_date"  value="${paramValues.visit_reg_date[0]}">
		<input type="hidden" name="visit_reg_date"  value="${paramValues.visit_reg_date[1]}">
		<input type="hidden" name="visit_reg_date@op" id="visit_reg_date@op" value="${ifn:cleanHtmlAttribute(param['visit_reg_date@op'])}">
		<input type="hidden" name="visit_reg_date@type" id="visit_reg_date@type" value="${ifn:cleanHtmlAttribute(param['visit_reg_date@type'])}">
		<input type="hidden" name="visit_reg_date@cast" id="visit_reg_date@cast" value="y">

		<c:set var="selectPrompt">
		<insta:ltext key="selectdb.dummy.value"/>
		</c:set>
		<c:set var="dischargeDate">
		<insta:ltext key="search.patient.visit.discharge.date"/>
		</c:set>
		<c:choose>
			<c:when test="${preferences.modulesActivatedMap.mod_adt == 'Y'}">
				<input type="hidden" name="exclude_in_qb_alloc_ward_no" value="${ifn:cleanHtmlAttribute(param.exclude_in_qb_alloc_ward_no)}"/>
				<input type="hidden" name="exclude_in_qb_alloc_bed_no" value="${ifn:cleanHtmlAttribute(param.exclude_in_qb_alloc_bed_no)}"/>
			</c:when>
			<c:otherwise> <%-- use the registered ward name alone instead --%>
				<input type="hidden" name="exclude_in_qb_reg_ward_no" value="${ifn:cleanHtmlAttribute(param.exclude_in_qb_reg_ward_no)}">
			</c:otherwise>
		</c:choose>


			<insta:search form="visitSearchForm" optionsId="optionalFilter" closed="${hasResults}" validateFunction="validateSearchForm()">
				<div class="searchBasicOpts" >
					<div class="sboField">
						<div class="sboFieldLabel"><insta:ltext key="search.patient.visit.mr.no.patient.name"/>:</div>
						<div class="sboFieldInput">
							<div id="mrnoAutoComplete">
								<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
								<input type="hidden" name="mr_no@op" value="ilike" />
								<div id="mrnoContainer"></div>
							</div>
						</div>
					</div>

					<div class="sboField">
						<div class="sboFieldLabel">&nbsp;
							<div class="sboFieldInput">
								<input type="checkbox" name="_mr_no" id="_mr_no" onclick="changeStatus()"/><insta:ltext key="search.patient.visit.active.only"/>
							</div>
						</div>
					</div>
				</div>
				<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
					<table class="searchFormTable">
						<tr>
							<td>
								<div class="sfLabel"><insta:ltext key="search.patient.visit.patient.details"/>:</div>
								<div class="sfField">
									<div class="sfFieldSub" style="white-space: nowrap"><insta:ltext key="search.patient.visit.patient.full.name"/>:</div>
									<input type="text" name="patient_full_name" value="${ifn:cleanHtmlAttribute(param.patient_full_name)}"/>
									<input type="hidden" name="patient_full_name@type" value="text"/>
									<input type="hidden" name="patient_full_name@op" value="ico"/>
								</div>
								<div class="sfField">
									<div class="sfFieldSub" style="white-space: nowrap"><insta:ltext key="search.patient.visit.phone"/>:</div>
									<input type="text" name="patient_phone" value="${ifn:cleanHtmlAttribute(param.patient_phone)}"/>
								</div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="search.patient.visit.complaint"/>:</div>
										<input type="text" name="complaint" id="complaint" value="${ifn:cleanHtmlAttribute(param.complaint)}"/>
								</div>
								<div class="sfLabel"><insta:ltext key="registration.patient.commonlabel.patientdateOfBirth"/>:</div>
								<div class="sfField">
									<insta:datewidget name="dateofbirth" id="dateofbirth" value="${ifn:cleanHtmlAttribute(param.dateofbirth)}"/>
									<input type="hidden" name="dateofbirth@type" value="date"/>
								</div>
								<div class="sfLabel"><insta:ltext key="search.patient.visit.admission.date"/>:</div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="search.patient.visit.admission.date.from"/>:</div>
									<div style="clear:both"></div>
									<insta:datewidget name="_reg_date" id="reg_date0" value="${paramValues._reg_date[0]}"/>
									<input type="text" class="timefield" name="_reg_time" id="_reg_time0" value="${paramValues._reg_time[0]}"/>
								</div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="search.patient.visit.admission.date.to"/>:</div>
									<div style="clear:both"></div>
									<insta:datewidget name="_reg_date" id="reg_date1" value="${paramValues._reg_date[1]}"/>
									<input type="text" class="timefield" name="_reg_time" id="_reg_time1" value="${paramValues._reg_time[1] }">
								</div>
							</td>
							<td>
								<div class="sfLabel"><insta:ltext key="search.patient.visit.status"/></div>
								<c:set var="statusOptions">
									<insta:ltext key="search.patient.visit.status.options"/>
								</c:set>
								<div class="sfField">
									<insta:checkgroup name="status" opvalues="A,I" optexts="${statusOptions}" selValues="${paramValues['status']}"/>
								</div>
								<div class="sfLabel"><insta:ltext key="search.patient.visit.type"/></div>
								<c:set var="typeOptions">
									<insta:ltext key="search.patient.visit.type.options"/>
								</c:set>
								<div class="sfField">
									<insta:checkgroup name="visit_type" opvalues="i,o" optexts="${typeOptions}" selValues="${paramValues.visit_type}"/>
								</div>

								<div class="sfLabel"><insta:ltext key="search.patient.visit.op.type"/></div>
								<c:set var="optypeOptions">
									<insta:ltext key="search.patient.visit.op.type.options"/>
								</c:set>
								<div class="sfField">
									<insta:checkgroup name="op_type" opvalues="M,F,D,R,O" optexts="${optypeOptions}" selValues="${paramValues.op_type}"/>
								</div>

							</td>
							<td>
								<div class="sfLabel"><insta:ltext key="search.patient.visit.department"/></div>
								<div class="sfField">
									<insta:selectdb name="dept_id" table="department" valuecol="dept_id" displaycol="dept_name"
										dummyvalue="${selectPrompt}" values="${paramValues.dept_id}" orderby="dept_name"/>
								</div>
								<div class="sfLabel"><insta:ltext key="search.patient.visit.doctor"/></div>
								<div class="sfField">
									<insta:selectdb name="doctor" table="doctors" valuecol="doctor_id" displaycol="doctor_name"
										dummyvalue="${selectPrompt}" values="${paramValues.doctor}" orderby="doctor_name"/>
								</div>
								<div class="sfLabel"><insta:ltext key="search.patient.visit.ward"/></div>
								<div class="sfField">
									<div id="wardAutoComplete" class="autocomplete">
										<input type="text" name="exclude_in_qb_ward_name" id="exclude_in_qb_ward_name" value="${ifn:cleanHtmlAttribute(param.exclude_in_qb_ward_name)}">
										<div id="wardContainer" ></div>
									</div>
								</div>
								<c:choose>
									<c:when test="${preferences.modulesActivatedMap['mod_adt'] eq 'Y'}">
										<div class="sfLabel"><insta:ltext key="search.patient.visit.bed.name"/></div>
										<div class="sfField">
											<div id="bedNameAutoComplete" class="autocomplete">
												<input type="text" name="exclude_in_qb_bed_name" id="exclude_in_qb_bed_name" value="${ifn:cleanHtmlAttribute(param.exclude_in_qb_bed_name)}">
												<div id="bedNameContainer" style="width: 300px;"></div>
											</div>
										</div>
									</c:when>
									<c:otherwise>
										<div class="sfLabel"><insta:ltext key="search.patient.visit.bed.type"/></div>
										<div class="sfField">
											<div id="bedTypeAutoComplete" class="autocomplete">
												<input type="text" name="exclude_in_qb_reg_bed_type" id="exclude_in_qb_reg_bed_type" value="${ifn:cleanHtmlAttribute(param.exclude_in_qb_reg_bed_type)}">
												<div id="bedTypeContainer"></div>
											</div>
										</div>
									</c:otherwise>
								</c:choose>
							</td>
							<td>
								<div class="sfLabel"><insta:ltext key="search.patient.visit.location"/></div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="search.patient.visit.country"/>:</div>
									<div style="clear: both"/>
									<div id="autocountry" class="autocomplete">
										<input type="text" name="_country" id="_country" size="8" class="field" value="${ifn:cleanHtmlAttribute(param._country)}"/>
										<div id="countrycontainer" class="scrolForContainer"></div>
									</div>
								</div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="search.patient.visit.state"/>:</div>
									<div style="clear: both"/>
									<div id="autostate" class="autocomplete">
										<input type="text"	name="_patientstate" id="_patientstate" size="8" class="field" value="${ifn:cleanHtmlAttribute(param._patientstate)}"/>
										<div id="statecontainer" class="scrolForContainer"></div>
									</div>
								</div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="search.patient.visit.city"/>:</div>
									<div style="clear: both"/>
									 <div id="autocity" class="autocomplete">
										<input type="text" name="_patientcity" id="_patientcity" size="8" class="field" value="${ifn:cleanHtmlAttribute(param._patientcity)}"/>
										<div id="citycontainer" class="scrolForContainer"></div>
									</div>
								</div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="search.patient.visit.area"/>:</div>
									<div style="clear: both"/>
									<div id="autoarea" class="autocomplete">
										<input type="text"	name="patient_area" id="patient_area" class="field" size="8" value="${ifn:cleanHtmlAttribute(param.patient_area)}"/>
										<div id="areacontainer" class="scrolForContainer"></div>
									</div>
								</div>
								<div style="clear:both"/ >
								<div class="sfLabel" ><insta:ltext key="search.patient.visit.discharge.date"/></div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="search.patient.visit.discharge.date.from"/>:</div>
									<insta:datewidget name="discharge_date" id="discharge_date0"
										value="${paramValues.discharge_date[0]}"/>
								</div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="search.patient.visit.discharge.date.to"/>:</div>
									<insta:datewidget name="discharge_date" id="discharge_date1"
										value="${paramValues.discharge_date[1]}"/>
									<input type="hidden" name="discharge_date@op" value="ge,le"/>
								</div>
							</td>
							<td class="last">
								<div class="sfLabel"><insta:ltext key="search.patient.visit.discharge.summary.status"/></div>
								<c:set var="dssOptions">
								<insta:ltext key="search.patient.visit.discharge.summary.status.options"/>
								</c:set>
								<div class="sfField">
									<insta:checkgroup name="exclude_in_qb_finalized" opvalues="ND,O,F"
										optexts="${dssOptions}" selValues="${paramValues.exclude_in_qb_finalized}"/>
								</div>

								<div class="sfLabel"><insta:ltext key="search.patient.visit.finalized.by"/></div>
								<div class="sfField">
									<input type="text" name="discharge_finalized_user"
											value="${ifn:cleanHtmlAttribute(param.discharge_finalized_user)}"/>
								</div>
								<div class="sfLabel"><insta:ltext key="search.patient.visit.finalized.date"/></div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="search.patient.visit.finalized.date.from"/>:</div>
									<insta:datewidget name="discharge_finalized_date" id="f_date0"
										value="${paramValues.discharge_finalized_date[0]}"/>
								</div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="search.patient.visit.finalized.date.to"/>:</div>
									<insta:datewidget name="discharge_finalized_date" id="f_date1"
										value="${paramValues.discharge_finalized_date[1]}"/>
									<input type="hidden" name="discharge_finalized_date@op" value="ge,le"/>
								</div>

							</td>
						</tr>
						<tr>
							<td class="last" >
								<div class="sfLabel"><insta:ltext key="search.patient.visit.generic.search.options"/></div>
								<div class="sfField">
									<select name="_customRegFieldName" class="dropdown" onchange="showDateAndNumericInput(this)">
										<option value=""><insta:ltext key="search.patient.visit.custom.registration.field"/></option>
										<c:forEach var="c" items="${customRegFieldsMap}">
											<option value="${c.key}" ${param._customRegFieldName == c.key ? 'selected' : ''}>${ifn:cleanHtml(c.value)}</option>
										</c:forEach>
										<option value="category_name" ${param._customRegFieldName == 'category_name' ? 'selected' : ''}><insta:ltext key="search.patient.visit.insurance.category.name"/></option>
										<option value="plan_name" ${param._customRegFieldName == 'plan_name' ? 'selected' : ''}><insta:ltext key="search.patient.visit.policy.name"/></option>
										<option value="policy_holder_name" ${param._customRegFieldName == 'policy_holder_name' ? 'selected' : ''}><insta:ltext key="search.patient.visit.policy.holder.name"/></option>
										<option value="mlc_no" ${param._customRegFieldName == 'mlc_no' ? 'selected' : ''}><insta:ltext key="search.patient.visit.mlc.no"/></option>
										<option value="mlc_type" ${param._customRegFieldName == 'mlc_type' ? 'selected' : ''}><insta:ltext key="search.patient.visit.mlc.type"/></option>
										<option value="accident_place" ${param._customRegFieldName == 'accident_place' ? 'selected' : ''}><insta:ltext key="search.patient.visit.accident.place"/></option>
										<option value="police_stn" ${param._customRegFieldName == 'police_stn' ? 'selected' : ''}><insta:ltext key="search.patient.visit.police.station"/></option>
										<c:if test="${preferences.modulesActivatedMap['mod_adv_ins'] eq 'Y'}">
											<option value="member_id" ${param._customRegFieldName == 'member_id' ? 'selected' : ''}>
											<c:choose>
												<c:when test="${corpInsurance eq 'Y'}">
													<insta:ltext key="registration.patient.sponsor.employee.id"/>
												</c:when>
												<c:otherwise>
													<insta:ltext key="search.patient.visit.member.id"/>
												</c:otherwise>
											</c:choose>
											</option>
										</c:if>
									</select>
								</div>
								<div class="sfField">
									<select name="_regFieldName" onchange="onChangeRegField()" class="dropdown">
										<option value=""><insta:ltext key="search.patient.visit.registration.field"/></option>
										<option value="patient_gender" ${param._regFieldName == 'patient_gender' ? 'selected' : ''}><insta:ltext key="search.patient.visit.gender"/></option>
										<option value="org_id" ${param._regFieldName == 'org_id' ? 'selected' : ''}><insta:ltext key="search.patient.visit.rateplan"/></option>
										<option value="primary_sponsor_id" ${param._regFieldName == 'primary_sponsor_id' ? 'selected' : ''}><insta:ltext key="search.patient.visit.primary.sponsor"/></option>
									    <c:if test="${corpInsurance ne 'Y'}">
										<option value="secondary_sponsor_id" ${param._regFieldName == 'secondary_sponsor_id' ? 'selected' : ''}><insta:ltext key="search.patient.visit.secondary.sponsor"/></option>
										</c:if>
										<c:if test="${not empty regPref.patientCategory}">
										<option value="patient_category_id" ${param._regFieldName == 'patient_category_id' ? 'selected' : ''}>${ifn:cleanHtml(regPref.patientCategory)}</option>
										</c:if>
										<c:if test="${not empty regPref.custom_list1_name}">
										<option value="custom_list1_value" ${param._regFieldName == 'custom_list1_value' ? 'selected' : ''}>${ifn:cleanHtml(regPref.custom_list1_name)}</option>
										</c:if>
										<c:if test="${not empty regPref.custom_list2_name}">
										<option value="custom_list2_value" ${param._regFieldName == 'custom_list2_value' ? 'selected' : ''}>${ifn:cleanHtml(regPref.custom_list2_name)}</option>
										</c:if>
										<c:if test="${not empty regPref.custom_list3_name}">
										<option value="custom_list3_value" ${param._regFieldName == 'custom_list3_value' ? 'selected' : ''}>${ifn:cleanHtml(regPref.custom_list3_name)}</option>
										</c:if>
										<c:if test="${not empty regPref.custom_list4_name}">
										<option value="custom_list4_value" ${param._regFieldName == 'custom_list4_value' ? 'selected' : ''}>${ifn:cleanHtml(regPref.custom_list4_name)}</option>
										</c:if>
										<c:if test="${not empty regPref.custom_list5_name}">
										<option value="custom_list5_value" ${param._regFieldName == 'custom_list5_value' ? 'selected' : ''}>${ifn:cleanHtml(regPref.custom_list5_name)}</option>
										</c:if>
										<c:if test="${not empty regPref.custom_list6_name}">
										<option value="custom_list6_value" ${param._regFieldName == 'custom_list6_value' ? 'selected' : ''}>${ifn:cleanHtml(regPref.custom_list6_name)}</option>
										</c:if>
										<c:if test="${not empty regPref.custom_list7_name}">
										<option value="custom_list7_value" ${param._regFieldName == 'custom_list7_value' ? 'selected' : ''}>${ifn:cleanHtml(regPref.custom_list7_name)}</option>
										</c:if>
										<c:if test="${not empty regPref.custom_list8_name}">
										<option value="custom_list8_value" ${param._regFieldName == 'custom_list8_value' ? 'selected' : ''}>${ifn:cleanHtml(regPref.custom_list8_name)}</option>
										</c:if>
										<c:if test="${not empty regPref.custom_list9_name}">
										<option value="custom_list9_value" ${param._regFieldName == 'custom_list9_value' ? 'selected' : ''}>${ifn:cleanHtml(regPref.custom_list9_name)}</option>
										</c:if>
									</select>
								</div>
							</td>
							<td class="last">
								<div style="height: 26px;">&nbsp;</div>
								<div class="sfField" id="customField" style="display:none">
									<input type="text" name="_customRegFieldValue" id="_customRegFieldValue" value="${ifn:cleanHtmlAttribute(param._customRegFieldValue)}" size="18"/>
								</div>
								<div class="sfField" id="customDateField" style="display:none">
									<insta:datewidget name="_customRegFieldValue" id="_customRegDateFieldValue" value="${param._customRegFieldValue}" />
								</div>
								<div class="sfField" id="customNumericField" style="display:none">
									<input type="text" name="_customRegFieldValue" id="_customRegNumericFieldValue"
										value="${ifn:cleanHtmlAttribute(param._customRegFieldValue)}" class="number"/>
								</div>
								<div class="sfField">
									<input type="hidden" name="_hiddenRegFieldValue" value="${ifn:cleanHtmlAttribute(param._regFieldValue)}"/>
						    		<select name="_regFieldValue" class="dropdown">
						    			<option value="">${selectPrompt}</option>
						    		</select>
								</div>
							</td>
						</tr>
					</table>
				</div>
			</insta:search>
		</form>
		<c:choose>
			<c:when test="${useInfinitePagination}">
				<insta:paginateinfinite curPage="${pagedList.pageNumber}" isLastPage="${ fn:length(visitsList) < pagedList.pageSize }" showTooltipButton="true"/>	
			</c:when>				
			<c:otherwise>
				<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}" showTooltipButton="true"/>
			</c:otherwise>
		</c:choose>
		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable">
				<tr>
					<th>#</th>
					<c:set var="visitMRNo">
						<insta:ltext key="ui.label.mrno"/>
					</c:set>
					<c:set var="visitId">
						<insta:ltext key="search.patient.visit.visit.id"/>
					</c:set>
					<c:set var="visitAdmitted">
						<insta:ltext key="search.patient.visit.admitted"/>
					</c:set>
					<c:set var="deptArea">
						<insta:ltext key="search.patient.visit.dept.area"/>
					</c:set>
					<insta:sortablecolumn name="mr_no" title="${visitMRNo}"/>
					<insta:sortablecolumn name="patient_id" title="${visitId}"/>
					<th><insta:ltext key="ui.label.patient.name"/></th>
					<th><insta:ltext key="search.patient.visit.age.sex"/></th>
					<th><insta:ltext key="search.patient.visit.phone"/></th>
					<insta:sortablecolumn name="visit_reg_date" title="${visitAdmitted}"/>
					<insta:sortablecolumn name="dept_name" title="${deptArea}"/>
					<th><insta:ltext key="search.patient.visit.doctor.nok"/></th>
					<th><insta:ltext key="search.patient.visit.ward.bed.nok.ph"/></th>
					<th><insta:ltext key="search.patient.visit.discharge.date"/></th>
				</tr>
				<c:forEach var="visitBean" items="${visitsList}" varStatus="st">
					<c:set var="visit" value="${visitBean.map}"/>
					<c:set var="enableInactive" value="true"/>
					<c:choose>
						<c:when test="${visit.status == 'A'}"><c:set var="flagColor" value="empty"/></c:when>
						<c:when test="${visit.status == 'I'}">
							<c:set var="flagColor" value="grey"/>
							<%-- <c:set var="enableInactive" value="false"/>  --%>
						</c:when>
					</c:choose>

					<c:set var="readmitEnabled" value="${not empty visit.previous_visit_id && visit.status == 'I' && empty visit.visit_id}"/>
					<c:set var="orderEnabled" value="${visit.status == 'A'}"/>
					<c:set var="mlcEnabled" value="${visit.status == 'A' && visit.mlc_status == 'Y'}"/>
					<!-- HMS-11532:Application should not allow to edit custom fields if we set as NO in role access. -->
					<c:set var="editCustomfiledEnabled" value="${actionRightsMap.edit_custom_fields == 'A' || roleId == 1 || roleId == 2}"/>

					<c:set var="rowIndex" value="${rowIndex + st.index}"/>
					<tr class="${st.first ? 'firstRow' : ''} ${visit.mlc_status == 'Y'? 'mlcRow' : ''}"
						onclick="showToolbar(${rowIndex}, event, 'resultTable',
						{patient_id: '${ifn:cleanJavaScript(visit.patient_id)}', mr_no: '${visit.mr_no}',
						visit_type: '${visit.visit_type}', mrno: '${visit.mr_no}',
						visitId: '${ifn:cleanJavaScript(visit.patient_id)}', doc_id: '${visit.doc_id}',
						template_id : '${visit.template_id}',format : '${visit.doc_format}', orig_mr_no : '${visit.original_mr_no}',
						documentType: 'mlc'	},
						[${enableInactive},true,${readmitEnabled},true,true,${orderEnabled}, ${mlcEnabled}, true, ${editCustomfiledEnabled}], null, true);"
						onmouseover="hideToolBar(${rowIndex})" id="toolbarRow${rowIndex}">

						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1}</td>
						<td><img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>${visit.mr_no}</td>
						<td ${visit.mlc_status=='Y' ? 'class="mlcIndicator"' : ''}>${ifn:cleanHtml(visit.patient_id)}</td>
						<td <c:if test="${visit.vip_status=='Y'}">class="vipIndicator" title="VIP"</c:if>>
							<c:set var="patientFullName" value="${visit.patient_full_name}"/>
							<insta:truncLabel value="${patientFullName}" length="20"/></td>
						<td>${visit.age_text} / ${visit.patient_gender}</td>
						<td>${visit.patient_phone}</td>
						<td><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${visit.visit_reg_date}"/></td>
						<td><insta:truncLabel value="${visit.dept_name}" length="15"/></td>
						<td><insta:truncLabel value="${visit.doctor_name}" length="15"/></td>
						<td
							 <c:set var="icu_beds" value="${fn:split(visit.icu_beds,',')}"/>

							<c:if test="${not empty visit.current_bed and ifn:arrayFind(icu_beds, visit.current_bed) ne -1}"> class="remarkIndicator"</c:if>>
							<c:set var="wardBed" value=""/>
							<c:if test="${visit.visit_type == 'i'}">
								<c:if test="${empty visit.discharge_date}">
									<c:choose>
										<c:when test="${preferences.modulesActivatedMap['mod_adt'] eq 'Y'}">
										<%-- show the allocated bed, if not allocated, no need to show reg. bed type/ward --%>
											<c:choose>
												<c:when test="${visit.alloc_ward_bed == '-'}"><insta:ltext key="search.patient.visit.not.allocated"/></c:when>
												<c:otherwise><c:set var="wardBed" value="${visit.alloc_ward_bed}"/></c:otherwise>
											</c:choose>
										</c:when>
										<c:otherwise><c:set var="wardBed" value="${visit.reg_ward_bed}"/></c:otherwise>
									</c:choose>
								</c:if>
								<c:if test="${visit.status eq 'I'}">
									<c:set var="wardBed" value="${visit.current_bed}"/>
								</c:if>
								<script>
								var p_bystander_bed = <insta:jsString value="${visit.bystander_bed}"/>;
								var p_previous_bed = <insta:jsString value="${visit.previous_bed}"/>;
								var p_retain_bed = <insta:jsString value="${visit.retain_bed}"/>;
								var p_current_bed = <insta:jsString value="${visit.current_bed}"/>;
								if (p_bystander_bed != '' ||
										p_previous_bed != '' ||
										p_retain_bed != '' ||
										p_current_bed != '')
									setExtraDetails('${visit.icu_beds}', p_previous_bed, p_retain_bed, p_current_bed, p_bystander_bed, 'toolbarRow${rowIndex}',
										'<fmt:formatDate pattern="dd-MM-yyyy" value="${visit.discharge_date}"/> <fmt:formatDate pattern="HH:mm" value="${visit.discharge_time}"/>');
								else
									extraDetails['toolbarRow${rowIndex}'] = {'${dischargeDate}': '<fmt:formatDate pattern="dd-MM-yyyy" value="${visit.discharge_date}"/> <fmt:formatDate pattern="HH:mm" value="${visit.discharge_time}"/>'};
								</script>
							</c:if>
							<c:if test="${visit.visit_type ne 'i'}">
								<script>
									extraDetails['toolbarRow${rowIndex}'] = {'${dischargeDate}': '<fmt:formatDate pattern="dd-MM-yyyy" value="${visit.discharge_date}"/> <fmt:formatDate pattern="HH:mm" value="${visit.discharge_time}"/>'};
								</script>
							</c:if>
							<insta:truncLabel value="${wardBed}" length="15"/>

						</td>
						<td><fmt:formatDate pattern="dd-MM-yyyy" value="${visit.discharge_date}"/></td>
					</tr>

					<c:set var="rowIndex" value="${rowIndex + st.index + 1}"/>
					<tr class="secondary" onclick="showToolbar(${rowIndex}, event, 'resultTable',
						{patient_id: '${ifn:cleanJavaScript(visit.patient_id)}', mr_no: '${visit.mr_no}',
						visit_type: '${visit.visit_type}', mrno: '${visit.mr_no}',
						visitId: '${ifn:cleanJavaScript(visit.patient_id)}', doc_id: '${visit.doc_id}',
						template_id : '${visit.template_id}',format : '${visit.doc_format}',
						documentType: 'mlc'	},
						[${enableInactive},true,${readmitEnabled},true,true,${orderEnabled},${mlcEnabled}, true, ${editCustomfiledEnabled}]);"
						onmouseover="hideToolBar(${rowIndex})" id="toolbarRow${rowIndex}">
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>
							<c:choose>
								<c:when test="${visit.op_type == 'F'}">FollowUp</c:when>
								<c:when test="${visit.op_type == 'D'}">FollowUp (No Cons.)</c:when>
								<c:when test="${visit.op_type == 'R'}">Revisit</c:when>
								<c:when test="${visit.op_type == 'O'}">Outside</c:when>
								<c:otherwise></c:otherwise>
							</c:choose>
						</td>
						<td colspan="4"><insta:truncLabel value="${visit.patient_address}" length="70"/></td>
						<td><insta:truncLabel value="${visit.patient_area}" length="15"/></td>
						<td><insta:truncLabel value="${visit.relation}" length="15"/></td>
						<td>${visit.patient_care_oftext}</td>
					</tr>

				</c:forEach>
			</table>
			<c:if test="${param.method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>
		</div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText"><insta:ltext key="search.patient.visit.inactive.visits"/></div>
		</div>
	</body>
</html>
