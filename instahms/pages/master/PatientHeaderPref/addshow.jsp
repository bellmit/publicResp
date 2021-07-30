<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title><insta:ltext key="patient.patientheaderpreference.addshow.title"/></title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<script>
     function init()
		{
			autoFieldDesc();
		}
		var field = <%= request.getAttribute("fieldsLists") %>;
		var rAutoComp;
		function autoFieldDesc() {
			var datasource = new YAHOO.util.LocalDataSource({result: field});
			datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
			datasource.responseSchema = {
				resultsList : "result",
				fields : [
					{key : "field_desc"},
					{key : "field_name"},
					{key : "data_level"},
					{key : "visit_type"},
					{key : "data_category"},
					{key : "display"},
					{key : "display_order"}]
			};
			var rAutoComp = new YAHOO.widget.AutoComplete('field_desc','fieldcontainer', datasource);
			rAutoComp.minQueryLength = 0;
		 	rAutoComp.maxResultsDisplayed = 20;
		 	rAutoComp.forceSelection = false ;
		 	rAutoComp.animVert = false;
		 	rAutoComp.resultTypeList = false;
		 	rAutoComp.typeAhead = false;
		 	rAutoComp.allowBroserAutocomplete = false;
		 	rAutoComp.prehighlightClassname = "yui-ac-prehighlight";
			rAutoComp.autoHighlight = true;
			rAutoComp.useShadow = false;
		 	if (rAutoComp._elTextbox.value != '') {
					rAutoComp._bItemSelected = true;
					rAutoComp._sInitInputValue = rAutoComp._elTextbox.value;
			}
		}
     function validate(){
     var displayorder = document.getElementsByName("display_order");
     for ( var i=0; i< displayorder.length; i++ ){
             var displayordr = document.getElementById("display_order"+i).value
                   if( displayordr == "" ){
                       showMessage("js.patient.patientheaderpreference.addshow.displayorder.required");
                       document.getElementById("display_order"+i).focus();
                     return false;
                  }
                  if(displayordr.search(/^\d{1,}$/)==-1){
                     showMessage("js.patient.patientheaderpreference.addshow.displayorder.validate");
                     document.getElementById("display_order"+i).focus();
                     return false;
                  }
                }
    document.patientHeaderPrefForm1.submit();
    return true;
    }
</script>
<insta:js-bundle prefix="patient.patientheaderpreference.addshow"/>
</head>
  <c:set var="save">
        <insta:ltext key="btn.save"/>
   </c:set>
   <c:set var="fieldname">
   <insta:ltext key="patient.patientheaderpreference.addshow.field" />
   </c:set>
   <c:set var="patientlabel">
        <insta:ltext key="patient.patientheaderpreference.addshow.patient"/>
   </c:set>
   <c:set var="visit">
       <insta:ltext key="patient.patientheaderpreference.addshow.visit"/>
   </c:set>
   <c:set var="clinical">
       <insta:ltext key="patient.patientheaderpreference.addshow.clinical"/>
   </c:set>getAllPatientHeaderPrefFields
   <c:set var="nonclinical">
       <insta:ltext key="patient.patientheaderpreference.addshow.nonclinical"/>
   </c:set>
   <c:set var="both">
       <insta:ltext key="patient.patientheaderpreference.addshow.both"/>
   </c:set>
   <c:set var="none">
       <insta:ltext key="patient.patientheaderpreference.addshow.none"/>
   </c:set>
   <c:set var="o">
       <insta:ltext key="patient.patientheaderpreference.addshow.o"/>
   </c:set>
   <c:set var="i">
       <insta:ltext key="patient.patientheaderpreference.addshow.i"/>
   </c:set>
   <c:set var="b">
      <insta:ltext key="patient.patientheaderpreference.addshow.b"/>
   </c:set>
   <c:set var="yes">
      <insta:ltext key="patient.patientheaderpreference.addshow.yes"/>
   </c:set>
   <c:set var="no">
      <insta:ltext key="patient.patientheaderpreference.addshow.no"/>
   </c:set>
   <c:set var="displayorder">
      <insta:ltext key="patient.patientheaderpreference.addshow.displayorder"/>
   </c:set>

<body onload="init();showFilterActive(document.patientHeaderPrefForm);">
<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>
<c:set var="results" value="${not empty pagedList.dtoList}"/>
 <h1><insta:ltext key="patient.patientheaderpreference.addshow.header"/></h1><br/>

   <form name="patientHeaderPrefForm" method="GET">
        <input type="hidden" name="method" value="getPatientHeaderPref">
        <input type="hidden" name="_searchMethod" value="getPatientHeaderPref"/>
        <input type="hidden" name="_method" value="getPatientHeaderPref"/>
        <input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

   <insta:search form="patientHeaderPrefForm" optionsId="optionalFilter" closed="${results}">
			<div class="searchBasicOpts" >
					<div class="sboField">
						<div class="sboFieldLabel">${fieldname}</div>
						<div class="sboFieldInput">
							<input type="text" name="field_desc" id="field_desc" value="${ifn:cleanHtmlAttribute(param.field_desc)}" style = "width:15em" >
							<input type="hidden" name="region_name@op" value="ico" />
							<div id="fieldcontainer" style ="width:32em"></div>
						</div>
				   </div>
            </div>
            <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
					<table class="searchFormTable">
						<tr>
		 			        <td class="sboField" style="height: 120px;">
					          <div class="sboField">
						      <div class="sboFieldLabel"><insta:ltext key="patient.patientheaderpreference.addshow.level" /></div>
							  <div class="sboFieldInput">
									<insta:checkgroup name="data_level" opvalues="P,V" optexts="${patientlabel},${visit}" selValues="${paramValues.data_level}"/>
							 </div>
					       </div>
				         </td>
                         <td class="sboField" style="height: 120px;">
					          <div class="sboField">
						      <div class="sboFieldLabel"><insta:ltext key="patient.patientheaderpreference.addshow.visit.type" /></div>
							  <div class="sboFieldInput">
									<insta:checkgroup name="visit_type" opvalues="o,i,b" optexts="${o},${i},${b}" selValues="${paramValues.visit_type}"/>
							 </div>
					       </div>
				         </td>
				         <td class="sboField" style="height: 120px;">
					          <div class="sboField">
						      <div class="sboFieldLabel"><insta:ltext key="patient.patientheaderpreference.addshow.category" /></div>
							  <div class="sboFieldInput">
									<insta:checkgroup name="data_category" opvalues="None,Both,C,O" optexts="${none},${both},${clinical},${nonclinical}" selValues="${paramValues.data_category}"/>
							 </div>
					       </div>
				         </td>
				         <td class="last">
					          <div class="sboField">
						      <div class="sboFieldLabel"><insta:ltext key="patient.patientheaderpreference.addshow.display" /></div>
							  <div class="sboFieldInput">
									<insta:checkgroup name="display" opvalues="Y,N" optexts="${yes},${no}" selValues="${paramValues.display}"/>
							 </div>
					       </div>
				         </td>

				    </tr>
	 	 	   </table>
		</insta:search>
  </form>
  <form name="patientHeaderPrefForm1" action="PatientHeaderPreferences.do?method=update" method="post">
    <input type="hidden" name="pageNum" value="${pagedList.pageNumber}">
   <insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}" pageNumParam="pageNum"/>
    <div class="resultList" >
     <table class="resultList" cellspacing="0" cellpadding="0" border="0" >
        <tr>
            <insta:sortablecolumn name="field_desc" title="${fieldname}" />
            <th style="width: 90px"><insta:ltext key="patient.patientheaderpreference.addshow.level" /></th>
            <th style="width: 90px"><insta:ltext key="patient.patientheaderpreference.addshow.visit.type" /></th>
            <th style="width: 90px"><insta:ltext key="patient.patientheaderpreference.addshow.category" /></th>
            <th style="width: 90px"><insta:ltext key="patient.patientheaderpreference.addshow.display" /></th>
            <insta:sortablecolumn name="display_order" title="${displayorder}"/>
        </tr>
        <c:set var="idx" value="0"/>
        <c:forEach var="patient" items="${pagedList.dtoList}" varStatus="st">
        <tr>
            <input type="hidden" name="field_name" value="${patient.map.field_name }"  />
            <input type="hidden" name="field_desc" value="${patient.map.field_desc }" />

            <td>${patient.map.field_desc }</td>
            <td><insta:selectoptions name="data_level" optexts="${patientlabel},${visit}" style="width: 80px"
                         opvalues="P,V" value="${patient.map.data_level}" disabled="true"></insta:selectoptions>
            </td>
            <td><insta:selectoptions name="visit_type" optexts="${o},${i},${b}" style="width : 80px"
                         opvalues="o,i,b" value="${patient.map.visit_type}" ></insta:selectoptions>
            </td>
            <td><insta:selectoptions name="data_category" optexts="${none},${both},${clinical},${nonclinical}"
                         opvalues="None,Both,C,O" value="${patient.map.data_category}" ></insta:selectoptions>
            </td>
            <td><insta:selectoptions name="display" optexts="${yes},${no}" style="width : 50px"
                         opvalues="Y,N" value="${patient.map.display}" ></insta:selectoptions>
            </td>
            <td><input type="text" name="display_order" id = "display_order${idx}" maxlength="3" style="width : 35px;" value="${patient.map.display_order}" />
            </td>
            <c:set var="idx" value="${idx+1}"/>
        </tr>
        </c:forEach>
        <c:if test="${param.method == 'getPatientHeaderPref'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>
     </table>
  </div>
   <div class="screenActions">
    <input type="button" accesskey="S" value="${save}" onclick="validate();"/>
     </div>
    </form>
</body>
</html>