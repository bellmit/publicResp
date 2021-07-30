<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.URLRoute"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="pagePath" value="<%=URLRoute.INSURANCE_PLANS_PATH %>"/>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Plan Master - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<script type="text/javascript">
		var toolBar = {
			Edit : {
				title : "View/Edit",
				imageSrc : "icons/Edit.png",
				href : "${pagePath}/show.htm?",
				onclick : null,
				description : "View and/or Edit the contents of the Plan"
				}
		};


		function init() {
			createToolbar(toolBar);
		}

		function doUpload(formType) {
		   if(formType == "uploadform"){
				var form = document.uploadform;
				if (form.csvFile.value == "") {
					alert("Please browse and select a file to upload");
					return false;
				}
			}
			form.submit();
		}

		function doExport() {
			var theform = document.forms[0];
			theform.action = "PlanMasterUpload.do";
			theform.method ="GET";
			document.getElementById('form0Method').value = "exportChargesCSV";
			window.open("../master/PlanMasterUpload.do?_method=exportChargesCSV");
		}

		function clearSearch(){
			clearForm(PolicyMasterForm);
		}

		function doSearch(){
			return true;
		}
	</script>

</head>

<body onload="init();">

	<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>

	<h1>Plan Master</h1>

	<insta:feedback-panel/>

		<form name="PolicyMasterForm" method="GET">

			<!-- <input type="hidden" name="_method" id="mainMethod" value="list"/>
			<input type="hidden" name="_searchMethod" value="list"/> -->
			<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
			<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
			<c:set var="ovrAllTrmntLimitOp" value="${param['overall_treatment_limit@op']}"/>
			<c:set var="detailTypeOp" value="${param['detailType@op']}"/>
		<insta:search form="PolicyMasterForm" optionsId="optionalFilter" closed="${hasResults}"
			 clearFunction="clearSearch" validateFunction="doSearch">
			<%-- Basic fields to contain only MR No (autocomplete) and Bill No for quick access --%>
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Plan Name:</div>
					<div class="sboFieldInput">
						<input type="text" name="plan_name" value="${ifn:cleanHtmlAttribute(param.plan_name)}">
						<input type="hidden" name="plan_name@op" value="ilike">
					</div>
				</div>
				<div class="sboField">
				<div class="sboFieldLabel">Network/Plan Type :</div>
				<div class="sboFieldInput">
					<input type="text" name="category_name" id="category_name" value="${ifn:cleanHtmlAttribute(param.category_name)}" />
					<input type="hidden" name="category_name@op" id="category_name@op" value="ilike" />
				</div>
				</div>
				<div class="sboField">
				<div class="sboFieldLabel">Insurance Comp Name :</div>
				<div class="sboFieldInput">
					<insta:selectdb name="insurance_co_id" table="insurance_company_master" valuecol="insurance_co_id"
							displaycol="insurance_co_name"  filtered="false"
								value="${param.insurance_co_id}" orderby="insurance_co_name" dummyvalue="(All)"/>
					</div>
				</div>
				
				<div class="sboField">
				<div class="sboFieldLabel">Sponsor Name :</div>
				<div class="sboFieldInput">
					<insta:selectdb name="sponsor_id" table="tpa_master" valuecol="tpa_id"
							displaycol="tpa_name"  filtered="false"
								value="${param.sponsor_id}" orderby="tpa_name" dummyvalue="(All)"/>
					</div>
				</div>
				</div>
				
				<div class="sboField">
				<div class="sboFieldLabel">Plan Status :</div>
				<div class="sboFieldInput">
					<insta:selectoptions name="status" value="${param.status}" opvalues="A,I"
					optexts="Active,Inactive" dummyvalue="(All)" />
					</div>
				</div>
				<div class="sboField">
				<div class="sboFieldLabel">Discount Plan :</div>
				<div class="sboFieldInput">
					<select class= "dropdown"  name="discount_plan_id" >
					 	<option value=''>-- Select --</option>
					 	<c:forEach var="defaultDiscountPlan" items="${defaultDiscountPlanList}"  varStatus="st" >
					 		 <option value='${defaultDiscountPlanList[st.index].discount_plan_id}'  
					 		 	${defaultDiscountPlanList[st.index].discount_plan_id == param.discount_plan_id ? 'selected' : ''} >
					 		 ${defaultDiscountPlanList[st.index].discount_plan_name}</option> 
					 	</c:forEach>
					</select>		 
				</div>
				</div>	
			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">OP-Applicable:</div>
							<div class="sfField">
								<select name="op_applicable" class="dropDown">
									<option value=''>(All)</option>
									<option value='Y' ${param.op_applicable eq 'Y' ?'selected':''} >Yes</option>
									<option value='N'  ${param.op_applicable eq 'N' ?'selected':''}>No</option>
							 	</select>
							</div>
						</td>
						<td>
							<div class="sfLabel">Visit Sponsor Limit:</div>
							<div class="sfField">
								<select name="overall_treatment_limit@op" class="dropDown" style="width:60px;">
										<option value='eq'  ${ovrAllTrmntLimitOp == 'eq'?'selected':''} >=</option>
										<option  value='ne' ${ovrAllTrmntLimitOp == 'ne'?'selected':''}> &ne;</option>
										<option  value="lt" ${ovrAllTrmntLimitOp == 'lt'?'selected':''}> &lt; </option>
										<option  value="gt" ${ovrAllTrmntLimitOp == 'gt'?'selected':''}> &gt; </option>
										<option value="le"  ${ovrAllTrmntLimitOp == 'le'?'selected':''}> &le; </option>
										<option value="ge" ${ovrAllTrmntLimitOp == 'ge'?'selected':''} > &ge; </option>
							 	</select>
							 	<input type="hidden" name="overall_treatment_limit@type" value="numeric">
							 	<input type="text"  class="numeric" name="overall_treatment_limit" value="${ifn:cleanHtmlAttribute(param.overall_treatment_limit)}" onkeypress="return enterNumAndDotAndMinus(event);">
							</div>
						</td>
						<td>
							<div class="sfLabel">Rate Plan:</div>
							<div class="sfField">
								<insta:selectdb name="default_rate_plan" table="organization_details" valuecol="org_id" class="dropdown"
								orderby="org_name" displaycol="org_name" value="${param.default_rate_plan}" dummyvalue="(All)"/>
							</div>
						</td>
						<c:if test="${mod_eclaim_pbm}">
						<td>
							<div class="sfLabel">Require PBM Authorization:</div>
							<div class="sfField">
								<select name="require_pbm_authorization" class="dropDown">
									<option value=''>(All)</option>
									<option  value='Y' ${param.require_pbm_authorization eq 'Y' ?'selected':''}>Yes</option>
									<option  value='N' ${param.require_pbm_authorization eq 'N' ?'selected':''}>No</option>
							 	</select>
							</div>
						</td>
						</c:if>
					</tr>
					<tr>
						<td>
							<div class="sfLabel">IP-Applicable:</div>
							<div class="sfField">
								<select name="ip_applicable" class="dropDown">
									<option value=''>(All)</option>
									<option  value='Y' ${param.ip_applicable eq 'Y' ?'selected':''}>Yes</option>
									<option  value='N' ${param.ip_applicable eq 'N' ?'selected':''}>No</option>
							 	</select>
							</div>
							<div class="sfLabel">Co-Pay% on Post-Discounted Amt:</div>
							<div class="sfField">
								<select name="is_copay_pc_on_post_discnt_amt" class="dropDown">
									<option value=''>(All)</option>
									<option  value='Y' ${param.is_copay_pc_on_post_discnt_amt eq 'Y' ?'selected':''}>Yes</option>
									<option  value='N' ${param.is_copay_pc_on_post_discnt_amt eq 'N' ?'selected':''}>No</option>
							 	</select>
							</div>
						</td>
						<td colspan="3">
						<div class="sfLabel">Insurance Item Category And Value Combo Filter:</div>
							<div class="sfField">
								<table class="formtable">
									<tr>
										<td style="border-right: 0 none;width:100px;">
											Insurance Item Category
										</td>
										<td style="border-right: 0 none;width:100px;">
											Charge Amt. Type
										</td>
										<td style="border-right: 0 none;width:65px;">
											Operator
										</td>
										<td style="border-right: 0 none;width:100px;">
											Value
										</td>
									</tr>
									<tr>
										<td style="border-right: 0 none;width:100px;">
											<insta:selectdb name="insurance_category_id" table="item_insurance_categories" valuecol="insurance_category_id"
											displaycol="insurance_category_name" value="${param.insurance_category_id}" filtercol="insurance_payable" filtervalue="Y" dummyvalue="(All)"/>
										</td>
										<td style="border-right: 0 none;width:100px;">
											<select name="detailType"  class="dropDown">
												<option value='patient_amount_per_category' ${param.detailType eq 'patient_amount' ?'selected':''} >Deductible (Cat)</option>
												<option value='patient_amount' ${param.detailType eq 'patient_amount' ?'selected':''} >Deductible (Item)</option>
												<option  value='patient_percent' ${param.detailType eq 'patient_percent'?'selected':''}>Copay %</option>
												<option  value='patient_amount_cap' ${param.detailType eq 'patient_amount_cap'?'selected':''}>Max Copay</option>
												<option  value='per_treatment_limit' ${param.detailType eq 'per_treatment_limit'?'selected':''}>Sponsor Limit</option>
										 	</select>
										 </td>
										 <td style="border-right: 0 none;width:65px;">
											<select name="detailType@op" class="dropDown" style="width:60px;">
												<option value='eq' ${detailTypeOp eq 'eq'?'selected':''}  >=</option>
												<option  value='ne' ${detailTypeOp eq 'ne'?'selected':''}> &ne;</option>
												<option  value="lt" ${detailTypeOp eq 'lt'?'selected':''}> &lt; </option>
												<option  value="gt" ${detailTypeOp eq 'gt'?'selected':''}> &gt; </option>
												<option  value="le" ${detailTypeOp eq 'le'?'selected':''}> &le; </option>
												<option value="ge" ${detailTypeOp eq 'ge' ?'selected':''} > &ge; </option>
										 	</select>
										</td>
										 <td style="border-right: 0 none;width:100px;">
										 	<input type="text"  class="numeric" name="detailTypeAmt" value="${ifn:cleanHtmlAttribute(param.detailTypeAmt)}" onkeypress="return enterNumAndDotAndMinus(event);">
										 	<input type="hidden" name="detailTypeAmt@type" value="numeric">
										 </td>
									</tr>
								</table>
							</div>
						</td>
					</tr>
				</table>
			</div>
		</insta:search>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList" >
			<table class="resultList" cellspacing="" cellpadding="" id="resultTable" onmouseover="hideToolBar();">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="plan_name" title="Plan Name"/>
					<insta:sortablecolumn name="category_name" title="Network/Plan Type"/>
					<insta:sortablecolumn name="insurance_co_name" title="Insurance Co. Name"/>
					<insta:sortablecolumn name="tpa_name" title="Sponsor Name"/>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
						onclick="showToolbar(${st.index}, event, 'resultTable', {plan_id: '${record.plan_id}'},'');">

						<td>${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}</td>
						<td>
							<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							${record.plan_name}
						</td>
						<td>
							${record.category_name}
						</td>
						<td>
							${record.insurance_co_name}
						</td>
						<td>
							${record.tpa_name}
						</td>
					</tr>
				</c:forEach>
			</table>


			<c:if test="${empty pagedList.dtoList}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>
		</div>

		<c:url var="Url" value="${pagePath}/add.htm">
		</c:url>

		<div class="screenActions" style="float: left">
			<a href="${Url}">Add New Plan</a>
		</div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>
	</form>
    <form name="dataForm" method="GET">
		<div  style="padding-top:3em;">
	    	<div class=" title CollapsiblePanelTab" tabindex="0" style="border-left:1px solid #E0E0E0;border-right:1px solid #E0E0E0 ">
	        	<div class="fltL " style="width: 230px; margin:5px 0 0 10px;"><b>Export/Import Policy Details</b></div>
				<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;"></div>
				<div class="clrboth"></div>
			</div>
			<table width="100%">
				<tr>
					<td>
						<table  class="search" width="100%">
							<tr>
								<td width="10%" style="border-left:1px solid #E0E0E0;text-align:right;vertical-align:middle;">Export:</td>
								<td style="border-right:1px solid #E0E0E0;">
									<form name="PlanUploadForm" action="PlanMasterUpload.do" method="GET" style="padding:0; margin:0">
										<input type="hidden" name="_method" id="form0Method" value="exportChargesCSV">
										<div style="float: left;">
											<button type="button" accesskey="D" onclick="doExport();"><b><u>D</u></b>ownload</button>
										</div>
										<div style="float: left;white-space: normal">
											<img class="imgHelpText"
												 src="${cpath}/images/help.png"
												 title="Note: The export gives a CSV file (comma separated values), which can be edited in a spreadsheet like MS Excel. After editing and saving, the file can be imported back, and the new charges will be updated."/>
										</div>
									</form>
								</td>
							</tr>
							<tr>
								<td  width="10%" style="border-left:1px solid #E0E0E0;border-bottom:1px solid #E0E0E0;text-align:right;vertical-align:middle;">Import: <br/></td>
								<td style="border-right:1px solid #E0E0E0;border-bottom:1px solid #E0E0E0;vertical-align:bottom;">&nbsp;&nbsp;
									<form name="uploadform" action="PlanMasterUpload.do" method="POST"
											enctype="multipart/form-data" style="padding:0; margin:0">
										<input type="hidden" name="_method" id="upMethod" value="importValuesFromCSV">
										<input type="file" name="csvFile" accept="<insta:ltext key="upload.accept.master"/>" />
										<button type="button" accesskey="U" onclick="return doUpload('uploadform')"><b><u>U</u></b>pload</button>
									</form>
									<br/>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
		</div>
	</form>
</body>
</html>




