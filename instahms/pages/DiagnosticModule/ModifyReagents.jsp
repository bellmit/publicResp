<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="laboratory.testconduction.list.title1"/></title>
<insta:link type="css" file="widgets.css" />
<insta:link type="script" file="widgets.js" />
<insta:link type="script" file="hmsvalidation.js"/>
<style type="text/css">
		.status_A.type_P { background-color: #EAD6BB }
		.status_C { background-color: #C5D9A3 }
		.status_X { color: grey }
	</style>
<script>
var addedRegaents = [];
var cpath = '<%=request.getContextPath()%>';
  function fillReagents(){

			for(var i =0;i<reagents.length;i++){
					var reagentTable = document.getElementById("reagentstable");
					var numRows = reagentTable.rows.length;

					var id = numRows;
				   	var tbody = document.getElementById("reagentstable");
				   	var len = tbody.rows.length;

                    if(reagents[i].status == 'true')
                   {
                    var	row = reagentTable.insertRow(id);
				   	row.id=len;
				    var cell1 = document.createElement("TD");
					cell1.setAttribute("class", "label");
					cell1.setAttribute("title", reagents[i].reagent_id);

					cell1.setAttribute("style", "max-width: 15em");
					var text4 = document.createTextNode(reagents[i].item_name);
					cell1.appendChild(text4);

					var inp11 = document.createElement("INPUT");
				    inp11.setAttribute("type","hidden");
				    inp11.setAttribute("name","item_id");
				    inp11.setAttribute("id","item_id"+id);
				    inp11.setAttribute("value",reagents[i].reagent_id);
				    cell1.appendChild(inp11);
				    addedRegaents.length = i;
				    addedRegaents[reagents[i].item_name] = reagents[i].item_name;

				    var hidden = makeHidden('ref_no','ref_no'+id,reagents[i].prescription_id);
				    cell1.appendChild(hidden);

				    hidden = makeHidden('reagent_usage_seq','reagent_usage_seq'+id,reagents[i].usage_no);
				    cell1.appendChild(hidden);
					if(reagents[i].usage_no == 0){
						 hidden = makeHidden('old_qty','old_qty'+id,'0');
				    	cell1.appendChild(hidden);
					}else{
					    if(reagents[i].qty == 0)
					    	hidden = makeHidden('old_qty','old_qty'+id,'0');
					    else
					    	hidden = makeHidden('old_qty','old_qty'+id,reagents[i].qty);
					    cell1.appendChild(hidden);
				    }

				    var cell2 = document.createElement("TD");
					var inp21 = document.createElement("INPUT");
				    inp21.setAttribute("type","text");
				    inp21.setAttribute("name","qty");
				    inp21.setAttribute("size","3");
				    inp21.setAttribute("id","qty"+id);
				    inp21.setAttribute("value",reagents[i].qty);
	    			inp21.setAttribute("onkeypress","return enterNumAndDot(event)");
	    			inp21.setAttribute("onblur","return makeingDecValidate(this.value,this,'"+id+"')");


				    cell2.appendChild(inp21);

					row.appendChild(cell1);
					row.appendChild(cell2);
					// document.getElementById("reagentstable").insertRow(len);
				}
			}

			if(document.getElementById("reagentstable").rows.length == 1) {
				var row = reagentTable.insertRow(1);
				var cell1 = document.createElement("TD");
				cell1.setAttribute("class", "label");
				cell1.setAttribute("align", "center");
				cell1.setAttribute("title", "No Active Reagents");
				var text = document.createTextNode("No Active Reagents");
				cell1.appendChild(text);
				row.appendChild(cell1);
			}
      }

      function saveReagents(conducted,patient_id,visit_id)
      {
        var visitId;
        if(patient_id == '')
        {
           visitId = visit_id;
        }
        else
        {
           visitId = patient_id;
        }
        var form = document.update;
        form.action = cpath+'/'+'${param.category == 'DEP_LAB' ? 'Laboratory' :'Radiology'}'+'/editresults.do?_method=updateReagents&conducted='+conducted+'&visitId='+visitId;
	    form._method.value = "updateReagents";
	    form.submit();
      }

       function makeingDecValidate(objValue,obj,id){
	    if (objValue!= '' && objValue!= '.') {
			document.getElementById(obj.name+id).value = parseFloat(objValue).toFixed(2);
		}
		if (objValue == '.' || objValue == '') document.getElementById(obj.name+id).value = 0.00;
	}
</script>
</head>
<c:set var="labreportList">
 <insta:ltext key="laboratory.reconductiontests.test.labreportslist"/>
</c:set>
<c:set var="radreportList">
 <insta:ltext key="laboratory.reconductiontests.test.radreportslist"/>
</c:set>
<c:set var="pendingTestList">
 <insta:ltext key="laboratory.reconductiontests.test.pendingtestlist"/>
</c:set>
<c:set var="editTestResults">
 <insta:ltext key="laboratory.reconductiontests.test.edittestresult"/>
</c:set>
<c:set var="saveBtn">
 <insta:ltext key="laboratory.reconductiontests.test.edittestresult"/>
</c:set>
<body onload="fillReagents();">
	<h1><insta:ltext key="laboratory.testconduction.list.editdiagnosisdetails"/></h1>
	<c:choose>
		<c:when test="${not empty patientvisitdetails }">
			<insta:patientdetails  visitid="${patientvisitdetails.map.patient_id}"/>
			<input type="hidden" name='visitid' value="${patientvisitdetails.map.patient_id}">
		</c:when>
		<c:otherwise>
			<input type="hidden" name='visitid' value="${custmer.map.incoming_visit_id}">
			<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="laboratory.testconduction.list.patientdetails"/></legend>
			<table class="formtable" cellpadding="0" cellspacing="0" width="100%">
				<tr>
					<td class="formlabel"><insta:ltext key="ui.label.patient.name"/>:</td>
					<td class="forminfo">${custmer.map.patient_name}</td>
					<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.fromlab"/>:</td>
					<td class="forminfo">${custmer.map.hospital_name}</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.patientvisit"/>:</td>
					<td class="forminfo">${custmer.map.incoming_visit_id}</td>
					<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.age.gender"/>:</td>
					<td class="forminfo">${custmer.map.age_text}${fn:toLowerCase(custmer.map.age_unit)} / ${custmer.map.gender}</td>
				</tr>
			</table>
			</fieldset>
		</c:otherwise>
	</c:choose>
	<input type="hidden" name="visitId" value="${ifn:cleanHtmlAttribute(param.visitId)}" >

	<form action="editresults.do" method="POST" name="update">
		<input type="hidden" name="_method" id="method" value="updateReagents"/>
		<input type="hidden" name="testId" value="${ifn:cleanHtmlAttribute(param.testId)}"/>
		<input type="hidden" name="visitId" value="${ifn:cleanHtmlAttribute(param.visitId)}"/>
		<input type="hidden" name="category" value="${ifn:cleanHtmlAttribute(param.category)}"/>
		<input type="hidden" name="pageNum" value="${ifn:cleanHtmlAttribute(param.pageNum)}">
		<input type="hidden" name="prescribed_id" id="prescribed_id" value="${ifn:cleanHtmlAttribute(param.prescribedId)}"/>
		<input type="hidden" name="testName" value="${ifn:cleanHtmlAttribute(param.testName)}"/>

		<fieldset class="fieldSetBorder" style="margin-left: 3px">
				<h2 style="margin-top: 10px"><insta:ltext key="laboratory.testconduction.list.testname"/>: ${ifn:cleanHtml(testName)}</h2>
				<table id="reagentstable" class="dataTable" cellpadding="0" cellspacing="0" width="100%">
					<tr id="reagentRow0">
					<th><insta:ltext key="laboratory.testconduction.list.reagentname"/></th>
					<th><insta:ltext key="laboratory.testconduction.list.qty"/></th>
				</tr>
			</table>
		</fieldset>
		<div class="screenActions">
			<input type="button" value="Save"
				onclick="saveReagents('${ifn:cleanJavaScript(param.conducted)}','${patientvisitdetails.map.patient_id}','${custmer.map.incoming_visit_id}')" />
			<c:choose>
				<c:when test="${category == 'DEP_LAB'}">
					<c:set var="url" value="Laboratory"/>
					<c:set var="reportListLink" value="${labreportList}"/>
				</c:when>
				<c:otherwise>
					<c:set var="url" value="Radiology"/>
					<c:set var="reportListLink" value="${radreportList}"/>
				</c:otherwise>
			</c:choose>
			<c:if test="${param.conducted != 'NRN' && param.conducted != 'CRN'}">
				| <a href="<c:out value="${cpath}/${url}/schedules.do?_method=getScheduleList&category=${ifn:cleanURL(category)}&mr_no=${ifn:cleanURL(patientvisitdetails.map.mr_no)}"/>">${reportListLink}</a>
				<insta:screenlink screenId="${category == 'DEP_LAB' ? 'lab_edit_results' : 'rad_edit_results'}"
				addPipe="true" label="${editTestResults}" extraParam="?_method=getBatchConductionScreen
				&prescId=${param.prescribedId}&visitid=${param.visitId}&category=${category}"/>
			</c:if>
			<insta:screenlink screenId="${category == 'DEP_LAB' ? 'lab_unfinished_tests' : 'rad_unfinished_tests'}"
			addPipe="true" label="${pendingTestList}" extraParam="?_method=unfinishedTestsList&conducted=N&conducted=P&conducted=NRN&sortOrder=pres_date&patient_id=${param.visitId}"/>
		</div>
	</form>
<script type="text/javascript">
var reagents = ${reagents};
</script>
</body>
</html>


