<%@ page pageEncoding="UTF-8"  isELIgnored="false"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page import="com.bob.hms.common.DataBaseUtil" %>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="max_centers_inc_default" value='<%= GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
	<head>
		<title>Patients Details Search - Insta HMS</title>
		<meta name="i18nSupport" content="true"/>
		<insta:link type="js" file="dashboardsearch.js"/>
		<insta:link type="js" file="patientdetailssearch/patientdetailssearch.js"/>
		<insta:link type="css" file="widgets.css"/>
		<style>
			.scrolForContainer .yui-ac-content{
				 width: 300px;
				 max-height:11em;
				 overflow:auto;
				 overflow-x:hidden; /* scrolling */
			    _height:11em; /* ie6 */
			}
			.autocomplete {
				padding-bottom: 20px;
				width: 138px;
			}
		</style>
		<script>
			var countryList = ${countryList};
			var stateList = ${stateList};
			var cityList = ${cityList};
		/*	var occupationList = ${occupationList};
			var bloodgroupList = ${bloodgroupList};
			var religionList = ${religionList};*/
			var categoryList = ${categoryList};
            var customList1 = ${customList1};
            var customList2 = ${customList2};
            var customList3 = ${customList3};
            var customList4 = ${customList4};
            var customList5 = ${customList5};
            var customList6 = ${customList6};
            var customList7 = ${customList7};
			var customList8 = ${customList8};
		    var customList9 = ${customList9};
			var searchForm = document.pdSearchForm;
			var regGeneral = '${urlRightsMap["reg_general"]}';
			var documentsList = '${urlRightsMap["generic_documents_list"]}';
			var documentsAdd = '${urlRightsMap["add_generic_documents"]}';
			var docket = '${urlRightsMap["patient_docket"]}'
			var emr = '${urlRightsMap["emr_screen"]}';
			var slida = '${urlRightsMap["slida_action"]}';
			
		</script>
		<insta:js-bundle prefix="patient.details.search"/>
		<insta:js-bundle prefix="patient.details.toolbar.option.common"/>		
		<script>
			var toolbarOptions = getToolbarBundle("js.patient.details.search.toolbar.option");
			var toolbarOptionsCommon = getToolbarBundle("js.patient.details.toolbar.option.common");			
		</script>
	</head>
	<body onload="init(document.pdSearchForm);initPatientToolbar(toolbarOptions);">
		<h1><insta:ltext key="patient.details.search.heading"/></h1>
		<c:set var="patientsList" value="${pagedList.dtoList}"/>
		<c:set var="useInfinitePagination" value='<%=DataBaseUtil.isLargeDataset("patient_details")%>'/>
 		<c:set var="hasResults" value="${(useInfinitePagination && pagedList.pageNumber > 0) || not empty patientsList}"/>

		<form action="" method="GET" name="pdSearchForm" autocomplete="off">
		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
		<input type="hidden" name="country" value="${ifn:cleanHtmlAttribute(param.country)}" />
		<input type="hidden" name="patient_state" value="${ifn:cleanHtmlAttribute(param.patient_state)}" />
		<input type="hidden" name="patient_city" value="${ifn:cleanHtmlAttribute(param.patient_city)}" />

			<insta:search form="pdSearchForm" optionsId="optionalFilter" closed="${hasResults}">
				<div class="searchBasicOpts" >
					<div class="sboField">
						<div class="sboFieldLabel"><insta:ltext key="patient.details.search.mr.no.patient.name"/>:</div>
						<div class="sboFieldInput">
							<div id="mrnoAutoComplete">
								<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
								<input type="hidden" name="mr_no@op" value="ilike" />
								<div id="mrnoContainer"></div>
							</div>
						</div>
					</div>
				</div>
							
				<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
					<table class="searchFormTable">
						<tr>
							<td>
								<div class="sfLabel"><insta:ltext key="patient.details.search.patient.details"/>:</div>
								<div class="sfField">
									<div class="sfFieldSub" style="white-space: nowrap"><insta:ltext key="patient.details.search.patient.full.name"/>:</div>
									<input type="text" name="patient_full_name" value="${ifn:cleanHtmlAttribute(param.patient_full_name)}"/>
									<input type="hidden" name="patient_full_name@type" value="text"/>
									<input type="hidden" name="patient_full_name@op" value="ico"/>
								</div>
								<div class="sfField">
									<div class="sfFieldSub" style="white-space: nowrap"><insta:ltext key="patient.details.search.phone"/>:</div>
									<input type="text" name="patient_phone" id="patient_phone" value="${ifn:cleanHtmlAttribute(param.patient_phone)}"/>
								</div>
								<div class="sfLabel"><insta:ltext key="registration.patient.commonlabel.patientdateOfBirth"/>:</div>
								<div class="sfField">
									<insta:datewidget name="dateofbirth" id="dateofbirth" value="${ifn:cleanHtmlAttribute(param.dateofbirth)}"/>
									<input type="hidden" name="dateofbirth@type" value="date"/>
								</div>
								<c:if test="${regPref.caseFileSetting != '' && regPref.caseFileSetting != null && regPref.caseFileSetting == 'Y'}">
									<div class="sfField">
										<div class="sfFieldSub" style="white-space: nowrap"><insta:ltext key="patient.details.search.case.file.no"/></div>
										<input type="text" name="casefile_no"/>
										<input type="hidden" name="casefile_no@op" value="ew"/>
									</div>
								</c:if>
								<c:if test="${customRegFieldsMap.family_id != null && customRegFieldsMap.family_id != ''}">
								<div class="sfField">
									<div class="sfFieldSub">${customRegFieldsMap.family_id}:</div>
									<input type="text" name="family_id" value="${param.family_id}"/>
								</div>
								</c:if>
							</td>
							<td>
								<div class="sfLabel"><insta:ltext key="patient.details.search.status"/></div>
								<div class="sfField">
									<c:set var="statusOptions"><insta:ltext key="patient.details.search.status.options"/></c:set>
									<insta:checkgroup name="status" opvalues="A,I,N" optexts="${statusOptions}" selValues="${paramValues.status}"/>
								</div>
								<div class="sfLabel"><insta:ltext key="patient.details.search.lastvisited.date"/>:</div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="patient.details.search.from"/>:</div>
									<insta:datewidget name="last_visited_date" id="last_visited_date0" value="${paramValues.last_visited_date[0]}"/>
								</div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="patient.details.search.to"/>:</div>
									<insta:datewidget name="last_visited_date" id="last_visited_date1" value="${paramValues.last_visited_date[1]}"/>
									<input type="hidden" name="last_visited_date@op" value="ge,le"/>
								</div>
								<c:if test="${max_centers_inc_default > 1}">
								<div class="sfLabel"><insta:ltext key="patient.details.search.lastvisited.center"/>:</div>
								<div class="sfField">
									<select name="last_visited_center" id="last_visited_center" class="dropdown">
										<option value="">-- All --</option>
										<c:forEach var="center" items="${centers}">
											<option value="${center.map.center_id}"
												${param.last_visited_center == center.map.center_id ? 'selected' : ''}>
												${center.map.center_name}
											</option>
										</c:forEach>
									</select>
									<input type="hidden" name="last_visited_center@cast" value="y"/>
								</div>
								</c:if>
							</td>
							<td>
								<div class="sfLabel"><insta:ltext key="patient.details.search.location"/></div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="patient.details.search.country"/>:</div>
									<div style="clear: both"/>
									<div id="autocountry" class="autocomplete">
										<input type="text" name="_country" id="_country" size="8" class="field" value="${ifn:cleanHtmlAttribute(param._country)}"/>
										<div id="countrycontainer" class="scrolForContainer"></div>
									</div>
								</div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="patient.details.search.state"/>:</div>
									<div style="clear: both"/>
									<div id="autostate" class="autocomplete">
										<input type="text"	name="_patientstate" id="_patientstate" size="8" class="field" value="${ifn:cleanHtmlAttribute(param._patientstate)}"/>
										<div id="statecontainer" class="scrolForContainer"></div>
									</div>
								</div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="patient.details.search.city"/>:</div>
									<div style="clear: both"/>
									 <div id="autocity" class="autocomplete">
										<input type="text" name="_patientcity" id="_patientcity" size="8" class="field" value="${ifn:cleanHtmlAttribute(param._patientcity)}"/>
										<div id="citycontainer" class="scrolForContainer"></div>
									</div>
								</div>
								<div class="sfField">
									<div class="sfFieldSub"><insta:ltext key="patient.details.search.area"/>:</div>
									<div style="clear: both"/>
									<div id="autoarea" class="autocomplete">
										<input type="text"	name="patient_area" id="patient_area" class="field" size="8" value="${ifn:cleanHtmlAttribute(param.patient_area)}"/>
										<div id="areacontainer" class="scrolForContainer"></div>
									</div>
								</div>
								 
							</td>
							
							<td class="last" >
								<div class="sfLabel"><insta:ltext key="patient.details.search.generic.search.options"/></div>
								<div class="sfField">
									<select name="_customRegFieldName" class="dropdown" onclick="showDateAndNumericInput(this)">
										<option value="">...<insta:ltext key="patient.details.search.custom.registration.field"/>...</option>
										<c:forEach var="c" items="${customRegFieldsMap}">
											<c:if test="${c.key != 'family_id'}">
											<option value="${c.key}" ${param._customRegFieldName == c.key ? 'selected' : ''}>${ifn:cleanHtml(c.value)}</option>
											</c:if>
										</c:forEach>
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
										<c:if test="${not empty regPref.government_identifier_label}">
										<option value="government_identifier" ${param._customRegFieldName == 'government_identifier' ? 'selected' : ''}>${regPref.government_identifier_label}</option>
										</c:if>
										
										<c:if test="${empty regPref.government_identifier_label}">
										<option value="government_identifier" ${param._customRegFieldName == 'government_identifier' ? 'selected' : ''}>Govt. Identifier</option>
										</c:if>
									</select>
								</div>
								<div class="sfField">
									<select name="_regFieldName" onchange="onChangeRegField()" class="dropdown">
										<option value="">...<insta:ltext key="patient.details.search.registration.field"/>...</option>
										<option value="patient_gender" ${param._regFieldName == 'patient_gender' ? 'selected' : ''}><insta:ltext key="patient.details.search.gender"/></option>
										<c:if test="${regPref.patientCategory != null}">
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
								<div class="sfField" id="customField">
									<input type="text" name="_customRegFieldValue" id="_customRegFieldValue"
										value="${ifn:cleanHtmlAttribute(param._customRegFieldValue)}" size="18" />
								</div>
								<div class="sfField" id="customDateField" style="display:none">
									<insta:datewidget name="_customRegFieldValue" id="_customRegDateFieldValue"
										value="${param._customRegFieldValue}" />
								</div>
								<div class="sfField" id="customNumericField" style="display:none">
									<input type="text" name="_customRegFieldValue" id="_customRegNumericFieldValue"
										value="${ifn:cleanHtmlAttribute(param._customRegFieldValue)}" class="number"/>
								</div>
								<div class="sfField">
									<input type="hidden" name="_hiddenRegFieldValue" value="${ifn:cleanHtmlAttribute(param._regFieldValue)}">
						    		<select name="_regFieldValue" class="dropdown">
						    			<option value=""><insta:ltext key="selectdb.dummy.value"/></option>
                                    </select>
								</div>
							</td>
						</tr>
					</table>
				</div>
			</insta:search>
		</form>
		<c:if test="${param._method == 'list'}">
		
		<c:choose>
 			<c:when test="${useInfinitePagination}">
 				<insta:paginateinfinite curPage="${pagedList.pageNumber}" isLastPage="${ fn:length(patientsList) < pagedList.pageSize }" showTooltipButton="true"/>	
 			</c:when>				
 			<c:otherwise>
 				<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}" showTooltipButton="true"/>
 			</c:otherwise>
		</c:choose>
		
		<div class="resultList">
			<table class="resultList dialog_displayColumns" cellspacing="0" cellpadding="0" id="resultTable">
				<tr>
					<th>#</th>
					<!-- This can actually be simplified, by moving the ltext into the sortablecolumn tag.
					But that will break all the other pages right now. If we have a fallback mechanism
					built into ltext tag, then it can be done -->

					<c:set var="columnMRNo"><insta:ltext key="ui.label.mrno"/></c:set>

					<insta:sortablecolumn name="mr_no" title="${columnMRNo}"/>
					<th><insta:ltext key="ui.label.patient.name"/></th>
					<th><insta:ltext key="patient.details.search.age.gender"/></th>
					<th><insta:ltext key="patient.details.search.phone"/></th>
					<th><insta:ltext key="patient.details.search.area"/></th>
					<th><insta:ltext key="patient.details.search.city"/></th>
					<th><insta:ltext key="patient.details.search.lastvisited"/></th>
				</tr>
				<c:forEach var="patientBean" items="${patientsList}" varStatus="st">
				<c:set var="patient" value="${patientBean.map}"/>
					<c:choose>
						<c:when test="${patient.status == 'A'}"><c:set var="flagColor" value="empty"/></c:when>
						<c:when test="${patient.status == 'I'}"><c:set var="flagColor" value="grey"/></c:when>
						<c:otherwise><c:set var="flagColor" value="green"/></c:otherwise>
					</c:choose>
					<tr class="${st.first ? 'firstRow' : ''} ${patient.mlc_status == 'Y'? 'mlcRow' : ''}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
						{mr_no: '${patient.mr_no}', mrno: '${patient.mr_no}'},null,'',null, patientSearchSetHrefs);"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">

						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1}</td>
						<td><img src="${cpath}/images/${flagColor}_flag.gif"/> ${patient.mr_no}</td>
						<td <c:if test="${patient.vip_status=='Y'}">class="vipIndicator" title="VIP"</c:if>>${ifn:cleanHtml(patient.patient_full_name)}</td>
						<td><bdo dir="${pageDirection}">${patient.age_text} </bdo>/ ${patient.patient_gender}</td>
						<td>${patient.patient_phone}</td>
						<td>${patient.patient_area}</td>
						<td>${patient.city_name}</td>
						<td>${patient.last_visited_date}</td>
					</tr>
				</c:forEach>
			</table>
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>
		</div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText"><insta:ltext key="patient.details.search.inactive.visits"/></div>
			<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
			<div class="flagText"><insta:ltext key="patient.details.search.no.visits"/></div>
		</div>
	</body>
</html>
