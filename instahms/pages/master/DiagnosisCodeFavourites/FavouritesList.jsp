<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<head>
  <title><insta:ltext key="patient.doctor.diagnosiscodefavourites.favouriteslist.title"/></title>
  <insta:link type="script" file="/DiagnosisCodeFavourites/diagnosisfavourites.js"/>
  <insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
  <insta:js-bundle prefix="patient.doctor.diagnosisfavourites"/>
  <insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
 <script>
   var doctor = '${ifn:cleanJavaScript(doctor_id)}';
   var codeSearch = <%= request.getAttribute("codeSearchList") %>;
 </script>
</script>
<style type="text/css">
 .myAutoComplete{
		 width:12em; /* set width here or else widget will expand to fit its container */
	     padding-bottom:2em;
	  }
</style>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="header1">
     <insta:ltext key="patient.doctor.diagnosiscodefavourites.favouriteslist.header1"/>
</c:set>
<c:set var="codecategory">
     <insta:ltext key="patient.doctor.diagnosiscodefavourites.favouriteslist.codecategory"/>
</c:set>
<c:set var="diagnosis">
     <insta:ltext key="patient.doctor.diagnosiscodefavourites.favouriteslist.diagnosis"/>
</c:set>
<c:set var="codetype">
     <insta:ltext key="patient.doctor.diagnosiscodefavourites.favouriteslist.codetype"/>
</c:set>
<c:set var="doctor">
     <insta:ltext key="patient.doctor.diagnosiscodefavourites.favouriteslist.doctor"/>
</c:set>
<c:set var="code">
     <insta:ltext key="patient.doctor.diagnosiscodefavourites.favouriteslist.code"/>
</c:set>
<c:set var="description">
     <insta:ltext key="patient.doctor.diagnosiscodefavourites.favouriteslist.code.description"/>
</c:set>
<c:set var="doctorlist">
     <insta:ltext key="patient.doctor.diagnosiscodefavourites.favouriteslist.doctors.list"/>
</c:set>
<c:set var="select">
     <insta:ltext key="patient.doctor.diagnosiscodefavourites.favouriteslist.select"/>
</c:set>
<c:set var="selectcode">
     <insta:ltext key="patient.doctor.diagnosiscodefavourites.favouriteslist.select.code"/>
</c:set>
<c:set var="exportimport">
     <insta:ltext key="patient.doctor.diagnosiscodefavourites.favouriteslist.export.import.label"/>
</c:set>
<c:set var="exceltitle">
     <insta:ltext key="patient.doctor.diagnosiscodefavourites.favouriteslist.excel.title" />
</c:set>
<c:set var="save">
     <insta:ltext key="btn.save"/>
</c:set>
</head>
<body onload="init();">
 <h1><insta:ltext key="patient.doctor.diagnosiscodefavourites.favouriteslist.mainheader"/></h1>
      <form action="DiagnosisCodeFavourites.do" method="POST" name="diagnosiscodeform">
      <input type="hidden" name="_method" value="save">
      <input type="hidden" name="doctor_id" value="${ifn:cleanHtmlAttribute(param.doctor_id)}"/>
      <input type="hidden" name="pageNum" value="${pagedList.pageNumber}"/>
      <input type="hidden" name="doctor_name" value="${ doctor_bean.map.doctor_name}" />
      <input type="hidden" name="favourite" value="${ifn:cleanHtmlAttribute(param.fav)}" />
      <insta:feedback-panel/>
   <fieldset class="fieldSetBorder">
         <h2>${header1}</h2>
      <table class="formtable" id="hiddenFieldsContainer">
           <tr>
                <td class="formlabel">${codecategory}</td>
                <td colspan="2">${diagnosis}</td>
                <td colspan="2"></td>
          </tr>
          <tr>
                <td class="formlabel">${codetype}</td>
                <td colspan="2">
                   <select name="code_type" class="dropdown" id="code_type" onchange="onChange();">
                         <option value="">${select }</option>
                         <c:forEach var="type" items="${codeType}">
                              <option value="${type.map.code_type}" ${type.map.code_type == param.code ? 'selected' : '' }>${type.map.code_type}</option>
                         </c:forEach>
                  </select>
                  </td>
                  <td colspan="2">
                     <c:if test="${param.code !='' && param.code != null}">
                     <insta:checkgroup name="searchFav" id="searchFav" selValue="${param.fav}" opvalues="favourite" optexts="Favourites" onchange="showFavourites(this);"/>
                    </c:if>
               </td>
         </tr>
         <tr>
               <td class="formlabel">${doctor }</td>
               <td colspan="2">${doctor_bean.map.doctor_name}</td>
               <td colspan="2"></td>
         </tr>
         <tr id="codeSearch" style="display: none;" >
                <td class="formlabel">Code :</td>
                <td>
                  <div class="myAutoComplete"><input type="text" name="searchCode" id="searchCode" value="${ifn:cleanHtmlAttribute(param.searchCode)}" >
                    <div id="codescontainer" style ="width:32em" ></div></div>
                </td>
                <td><input type="button" name="Search" value="Search"
			onclick="return getCodeDetails();" /></td>
          </tr>
     </table>
     <insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}" pageNumParam="pageNum" />
     <div class="resultList">
				<table class="resultList" onmouseover="hideToolBar('');" id="resultTable">
					<tr>
							<th style="width: 90px;">${selectcode }</th>
							<th style="width: 200px;">${ifn:cleanHtml(code)}</th>
							<th style="width: 400px;">${description }</th>
					</tr>
					<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr>
					        <input type="hidden" name="avlcode" value="${record.map.code }">
					        <td><input type="checkbox" name="selcode" value="${record.map.code }" ${not empty codesMap[record.map.code] ? 'checked' : '' }></td>
					        <td>${record.map.code }</td>
					        <td>${record.map.code_desc }</td>
					</tr>
					</c:forEach>
				</table>
		</div>
    </fieldset>
    <table class="screenActions">
           <tr>
              <td>
                 <input type="submit" name="save" value="${save}" ${empty param.code ? 'disabled': ''} >
                 <c:url value="/master/DoctorMaster.do" var="doctorListUrl">
            <c:param name="_method" value="list"/>
            <c:param name="status" value="A"/>
            <c:param name="sortOrder" value="doctor_name"/>
            <c:param name="sortReverse" value="false"/>
            <c:param name="org_id" value="ORG0001"/>
          </c:url>
          | <a href="${doctorListUrl}" title="Doctors List">${doctorlist}</a>
              </td>
          </tr>
     </table>
   </form>
   <div id="CollapsiblePanel1" class="CollapsiblePanel">
	    	<div class=" title CollapsiblePanelTab" tabindex="0" style=" border-left:none;">
	        	<div class="fltL " style="width: 230px; margin:5px 0 0 10px;"><insta:ltext key="patient.doctor.diagnosiscodefavourites.favouriteslist.upload" /></div>
				<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;"><img src="${cpath}/images/down.png" /></div>
				<div class="clrboth"></div>
		</div>
		<table class="search" style="padding-left: 3em">
		   <tr>
		      <th>${exportimport}</th>
					</tr>
					<tr>
						<td>
							<table>
								<tr>
									<td><insta:ltext key="patient.doctor.diagnosiscodefavourites.favouriteslist.export"/></td>
									<td>
										<form name="exporticdfavform" action="DiagnosisCodeFavourites.do" method="GET"
												style="padding:0; margin:0">
											<div style="float: left">
												<input type="hidden" name="_method" value="exportICDFavDetails">
												<input type="hidden" name="doctor_id" value="${ifn:cleanHtmlAttribute(param.doctor_id)}">
												<button type="submit" accesskey="E">
												<b><u><insta:ltext key="patient.doctor.diagnosiscodefavourites.favouriteslist.d" /> </u></b>
												<insta:ltext key="patient.doctor.diagnosiscodefavourites.favouriteslist.ownload" /></button>
											</div>
											<div style="float: left;white-space: normal">
												<img class="imgHelpText"
													 src="${cpath}/images/help.png"
													 title="${exceltitle }"/>
												</div>
											</div>
										</form>
									</td>
								</tr>
								<tr>
									<td><insta:ltext key="patient.doctor.diagnosiscodefavourites.favouriteslist.import"/></td>
									<td>
										<form name="uploadICDFavform" action="DiagnosisCodeFavourites.do" method="POST"
												enctype="multipart/form-data" style="padding:0; margin:0">
											<input type="hidden" name="_method" value="importICDFavDetailsFromXls">
											<input type="file" name="xlsICDFile" accept="<insta:ltext key="upload.accept.master"/>"/>
											<button type="button" accesskey="F" onclick="return doUpload()" >
											<b><u><insta:ltext key="patient.doctor.diagnosiscodefavourites.favouriteslist.u" /></u></b>
											<insta:ltext key="patient.doctor.diagnosiscodefavourites.favouriteslist.pload" /></button>
										</form>
									</td>
								</tr>
							</table>
						</td>
				</tr>
	</table>
   </div>
<script>
	var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen:false});
</script>
</body>
</html>