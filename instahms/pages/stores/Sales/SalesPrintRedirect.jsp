<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.common.Encoder" %>
<script language="javascript" type="text/javascript">
	var popupUrl;
	var popupPrescriptionUrl;
	var consultIds = ${consultationIds};
	var salePrintItems = '${ifn:cleanJavaScript(salePrintItems)}';
	var templateName = '${ifn:cleanJavaScript(templateName)}';
	var lblTemplateName = '${lblTemplateName}';
	var issuedPrescMedicineList = ${issuedMedicineList};
	var saleId = '<%=Encoder.cleanJavaScript((String)request.getAttribute("saleId"))%>';
	if ('${transaction}' == 'estimate') {
		popupUrl = "../../pages/stores/MedicineSalesPrint.do?method=getEstimatePrint";
		popupUrl += "&estimateId=${ifn:cleanURL(saleId)}";
		popupUrl += "&visitType=${ifn:cleanURL(visitType)}";
		popupUrl += "&printerId=${ifn:cleanURL(printerId)}";
	}else {
		popupUrl = "../../pages/stores/MedicineSalesPrint.do?method=getSalesPrint";
		popupUrl += "&saleId=<%=Encoder.cleanURL((String)request.getAttribute("saleId"))%>";
		popupUrl += "&return=<%=request.getAttribute("return")%>";
		popupUrl += "&printerId=<%=Encoder.cleanURL((String)request.getAttribute("printerId"))%>";

		if ( (salePrintItems == 'BILLPRESC' || salePrintItems == 'BILLPRESCLABEL') && consultIds != null ) {
			for ( var i=0; i<consultIds.length; i++) {
				if ( consultIds[i] != '' ) {
					popupPrescriptionUrl = '../../print/printPresConsultation.json?consultation_id='+consultIds[i]+'&templateName=' + templateName;
					popupPrescriptionUrl += "&printerId=<%=Encoder.cleanURL((String)request.getAttribute("printerId"))%>";
					window.open(popupPrescriptionUrl);
				}
			}
		}

		if ( (salePrintItems == 'BILLLABEL' || salePrintItems == 'BILLPRESCLABEL') && issuedPrescMedicineList != null && issuedPrescMedicineList != '') {
			var consultId ;
			var medPrescId = '' ;var lblCount = '';
			for ( var i=0; i<issuedPrescMedicineList.length; i++) {
				var noOfLabels = 0;
				consultId = issuedPrescMedicineList[i].consultation_id;
				if ('${saleUnit}' == 'P') {
					var issQty = issuedPrescMedicineList[i].issed_qty;
					var pkgSize = issuedPrescMedicineList[i].pkgUnit;
					noOfLabels = issQty%pkgSize == 0 ? issQty/pkgSize : parseInt(issQty/pkgSize) + 1;
				}else {
					noOfLabels = 1;
				}
				medPrescId = medPrescId + '&medicinePrescId='+issuedPrescMedicineList[i].op_medicine_pres_id;
				lblCount = lblCount +'&labelcount='+noOfLabels;
			}
			popupLabelUrl = '../../pages/stores/MedicineSalesPrint.do?method=printPrescLabel&consultation_id='
			+consultId +'&templateName=' + lblTemplateName+medPrescId+lblCount;
			popupLabelUrl += "&printerId=<%=Encoder.cleanURL((String)request.getAttribute("printerId"))%>";
			window.open(popupLabelUrl);
		}
	}
  if (saleId != null && saleId != '' && !saleId.match('Bill'))
	  window.open(popupUrl);


	var thisUrl;
	if ('${transaction}' == 'estimate') {
		thisUrl = "../../pages/stores/StoresEstimate.do?method=getSalesScreen&phStore=0&ps_status=active";
		thisUrl += "&msg=<%=Encoder.cleanURL((String)request.getAttribute("message"))%>";
		thisUrl += "&return=<%=request.getAttribute("return")%>";
	}else {
		thisUrl = "../../pages/stores/MedicineSales.do?method=getSalesScreen&phStore=0&ps_status=active";
		thisUrl += "&msg=<%=Encoder.cleanURL((String)request.getAttribute("message"))%>";
		thisUrl += "&return=<%=request.getAttribute("return")%>";
	}

  window.location.href= thisUrl;
</script>

