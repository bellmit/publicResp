<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Stock Upload-Insta HMS</title>
   <script>
function doUpload() {

   var form = document.stockUploadForm;
	if (form.stockXlsFile.value == "") {
		alert("Please browse and select a file to upload");
		return false;
	}
 }
   </script>
</head>
<c:set var="cpath">${pageContext.request.contextPath }</c:set>
<body>
	<h1>Stock Upload</h1>
	<insta:feedback-panel/>
<form action="StockUpload.do" method="GET" name="stockForm" >
	<input type="hidden" name="_method" value="getStockSampleXlsSheets"/>
		<table width="100%">
			<tr align="center">
				<td>Stock Sample Template:<button type="submit" name="download" id="download"  class="button" accesskey="E" ><label><u><b>D</b></u>ownload</label></button>
					<img class="imgHelpText" src="${cpath}/images/help.png" title="Note: This template will give you a idea how template must look while uploading."/>
				</td>
			</tr>
			   <tr>
			    <td>&nbsp;</td>
			</tr>
	  </form>
	<form name="stockUploadForm" action="StockUpload.do" method="POST" enctype="multipart/form-data" style="padding:0; margin:0">
	  <input type="hidden" name="_method" value="importStockDetailsFromXls"/>
			<tr align="center">
			   <td >Stock Data: <input type="file" name="stockXlsFile"  accept="<insta:ltext key="upload.accept.master"/>"/></td>
		   </tr>
		     <tr height="20"/>
		     <tr align="center">
			 <td>
				<button type="submit" name="save" id="save"  class="button" accesskey="F" onclick=" return doUpload();" ><label><u><b>U</b></u>pload</label></button>
			</td>
		 </tr>
      </form>
		<tr height="20"/>
		<tr align="left">
    		  <td class="label">Note:Please Upload Xls File only.While uploading data if any data is there in the corresponding tables then they will wiped out.
		   </td>
		 </tr>
	 </table>
  </body>
</html>