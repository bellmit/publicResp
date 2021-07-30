<%
	JSONSerializer js = new JSONSerializer().exclude("class");
	CrownStatusesDAO crownStatusDAO = new CrownStatusesDAO();
	RootStatusesDAO rootStatusesDAO = new RootStatusesDAO();
	PatientDentalConditionDao patDentlDAO = new PatientDentalConditionDao();
	SurfaceMaterialDAO materialDAO = new SurfaceMaterialDAO();

	request.setAttribute("crown_statuses", crownStatusDAO.listAll(null, "status", "A"));
	request.setAttribute("root_statuses", rootStatusesDAO.listAll(null, "status", "A"));
	List materials = materialDAO.listAll(null, "status", "A");
	request.setAttribute("surface_materials", materials);
	request.setAttribute("surface_materials_json", js.serialize(ConversionUtils.listBeanToListMap(materials)));

	request.setAttribute("dc_markers", patDentlDAO.getMarkerImageDetails(request.getParameter("mr_no")));
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>


<%@page import="flexjson.JSONSerializer"%>
<%@page import="com.insta.hms.outpatient.CrownStatusesDAO"%>
<%@page import="com.insta.hms.outpatient.RootStatusesDAO"%>
<%@page import="com.insta.hms.dentalconsultation.PatientDentalConditionDao"%>
<%@page import="com.insta.hms.outpatient.SurfaceMaterialDAO"%>
<%@page import="java.util.List"%>
<%@page import="com.insta.hms.common.ConversionUtils"%>
<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Dental Chart</legend>
	<div id="dental_chart_image" onclick="updateDentalChartXY(event);"
		style="width: 800px; height: 400px;
		background-image: url('${cpath}/DentalConsultation/Consultation.do?_method=getDentalChart');
		background-repeat:no-repeat;">
		<c:set var="numDCMarkers" value="${fn:length(dc_markers)}"/>
		<c:forEach begin="1" end="${numDCMarkers+1}" var="i" varStatus="loop">
			<c:set var="image" value="${dc_markers[i-1].map}"/>
			<div style="display: ${empty image ? 'none' : 'block'}; height: 0px" id="dental_chart_marker_div"
				name="dental_chart_marker_div">
				<c:url value="/DentalConsultation/Consultation.do" var="markerUrl">
					<c:param name="_method" value="getDentalChartMarkerImage"/>
					<c:param name="dc_material_id" value="${image.material_id}"/>
					<c:param name="dc_status_id" value="${image.status_id}"/>
					<c:param name="dc_tooth_part" value="${image.tooth_part}"/>
					<c:param name="dc_unv_number" value="${image.unv_number}"/>
					<c:param name="mr_no" value="${patient.mr_no}"/>
				</c:url>
				<c:set var="title" value=""/>
				<c:choose>
					<c:when test="${image.tooth_part == 'crown'}">
						<c:set var="title" value="${image.crown_status_desc}"/>
					</c:when>
					<c:when test="${image.tooth_part == 'root'}">
						<c:set var="title" value="${image.root_status_desc}"/>
					</c:when>
					<c:otherwise>
						<c:if test="${not empty image.tooth_part}">
							<c:set var="title" value="${image.option_name}"/>
							<c:if test="${not empty title && not empty image.material_name}">
								<c:set var="title" value="${title}/"/>
							</c:if>
							<c:set var="title" value="${title}${image.material_name}"/>
							<c:if test="${not empty title && not empty image.surface_status_name}">
								<c:set var="title" value="${title}/"/>
							</c:if>
							<c:set var="title" value="${title}${image.surface_status_name}"/>
						</c:if>
					</c:otherwise>
				</c:choose>
				<c:set var="dc_pos_x" value="0"/>
				<c:set var="dc_pos_y" value="0"/>
				<c:set var="tooth_number" value="${image.unv_number}"/>
				<c:if test="${fn:startsWith(image.unv_number, '[0-9]+')}">
				    <c:set var="tooth_number" value="${ifn:toString(image.unv_number)}"/>
				</c:if>
				<c:forEach var="xy" items="${(adult_tooth_image_details.teeth)[tooth_number].toothPart}">
					<c:if test="${xy.key == image.tooth_part}">
						<c:set var="dc_pos_x" value="${xy.value['pos_x']}"/>
						<c:set var="dc_pos_y" value="${xy.value['pos_y']}"/>
					</c:if>
				</c:forEach>
				<img src="${not empty image ? markerUrl : ''}" name="dc_marker_image" id="dc_marker_image" style="
					top: ${not empty dc_pos_y ? dc_pos_y : 0}px;
					left: ${not empty dc_pos_x ? dc_pos_x: 0}px; position:relative;display:block;z-index:2;" title="${title}"/>
				<input type="hidden" name="dc_tooth_part" value="${image.tooth_part}"/>
				<input type="hidden" name="dc_status_id" value="${image.status_id}"/>
				<input type="hidden" name="dc_option_id" value="${image.option_id}"/>
				<input type="hidden" name="dc_material_id" value="${image.material_id}"/>
				<input type="hidden" name="dc_unv_number" value="${image.unv_number}"/>
			</div>
		</c:forEach>

	</div>
</fieldset>


<input type="hidden" name="hid_dc_tooth_number" id="hid_dc_tooth_number" value=""/>
<input type="hidden" name="hid_dc_tooth_part" id="hid_dc_tooth_part" value=""/>
<input type="hidden" name="hid_dc_pos_x" id="hid_dc_pos_x" value=""/>
<input type="hidden" name="hid_dc_pos_y" id="hid_dc_pos_y" value=""/>

<script>
	var surface_materials_json = ${surface_materials_json};
</script>
<div id="crown_status_div" style="display: none; visibility: hidden">
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel" id="crownStatusLabel">Crown Statuses</legend>
			<table width="100%" >
				<tr>
					<td>
						<c:forEach items="${crown_statuses}" var="status">
							<div style="float: left; height: 15px; width: 15px;background-color: ${status.map.color_code}; border: 1px solid">&nbsp;</div>
							<div style="float: left; ">&nbsp;
								<input type="radio" name="crown_status" value="${status.map.crown_status_id}"/>
								<input type="hidden" id="crown_${status.map.crown_status_id}" value="${status.map.crown_status_desc}"/>
								<span style="vertical-align: top; height: 20px">${status.map.crown_status_desc}</span>
							</div>
							<div style="clear: both"></div>
						</c:forEach>
					</td>
				</tr>
				<tr>
					<td>
						<input type="button" id="crown_status_ok" value="Update Status"/>
						<input type="button" id="crown_status_close" value="Close"/>
						<input type="button" id="crown_status_delete" value="Delete"/>
					</td>
				</tr>
			</table>
		</fieldset>
	</div>
</div>

<div id="root_status_div" style="display: none; visibility: hidden">
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel" id="rootStatusLabel">Root Statuses</legend>
			<table width="100%" >
				<tr>
					<td>
						<c:forEach items="${root_statuses}" var="status">
							<div style="float: left; height: 15px; width: 15px;background-color: ${status.map.color_code}; border: 1px solid">&nbsp;</div>
							<div style="float: left; ">&nbsp;
								<input type="radio" name="root_status" value="${status.map.root_status_id}"/>
								<input type="hidden" id="root_${status.map.root_status_id}" value="${status.map.root_status_desc}"/>
								<span style="vertical-align: top; height: 20px">${status.map.root_status_desc}</span>
							</div>
							<div style="clear: both"></div>
						</c:forEach>
					</td>
				</tr>
				<tr>
					<td>
						<input type="button" id="root_status_ok" value="Update Status"/>
						<input type="button" id="root_status_close" value="Close"/>
						<input type="button" id="root_status_delete" value="Delete"/>
					</td>
				</tr>
			</table>
		</fieldset>
	</div>
</div>

<div id="surface_status_div" style="display: none; visibility: hidden">
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel" id="surfaceLabel"></legend>
			<table width="100%" class="smallformtable">
				<tr>
					<td class="formlabel">Option</td>
					<td><insta:selectdb table="tooth_surface_option_master" displaycol="option_name" valuecol="option_id"
						orderby="option_name" name="tooth_surface_option" dummyvalue="-- Select --" id="tooth_surface_option"/></td>
				</tr>
				<tr>
					<td class="formlabel">Material</td>
					<td style="width: 270px">
						<div style="float: left">
							<select class="dropdown" name="tooth_surface_material" id="tooth_surface_material" onchange="displayColor();">
								<option value="">-- Select --</option>
								<c:forEach items="${surface_materials}" var="material">
									<option value="${material.map.material_id}">
										${material.map.material_name}
									</option>
								</c:forEach>
							</select>&nbsp;&nbsp;&nbsp;
						</div>
						<div id="material_color_div" style="display: none; float: left; height: 19px; width: 19px; border: 1px solid">&nbsp;</div>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Condition</td>
					<td><insta:selectdb table="tooth_surface_status_master" displaycol="surface_status_name" valuecol="surface_status_id"
						orderby="surface_status_name" name="tooth_surface_condition" dummyvalue="-- Select --" id="tooth_surface_condition"/></td>
				</tr>
				<tr>
					<td colspan="2">
						<input type="button" id="surface_status_ok" value="Update"/>
						<input type="button" id="surface_status_close" value="Close"/>
						<input type="button" id="surface_status_delete" value="Delete"/>
					</td>
				</tr>
			</table>
		</fieldset>
	</div>
</div>

