<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>

<c:set var="testCenterAssociation" value="${requestScope.testCenterAssociation}" />
<c:set var="childSamplesList" value="${requestScope.childSamplesList}" />

<c:forEach items="${testCenterAssociation}" var="testCenterMap" varStatus="listIter">
	<script type="text/javascript">
		var testAssociation = {};
		var testID = "${testCenterMap.map.test_id}";
		var outCenters = "${testCenterMap.map.out_centers}";
		var testCategory = "";
		var outsourceDestinationID = null;
		var destination = null;
		if (outCenters.length === 0) { // Unnecessary step, won't occur.
			testCategory = "inhouse";
		} else {
			outCenters = outCenters.split(", ");
			var numOfDestinations = parseInt(outCenters[0], 10);
			if (numOfDestinations === 1) {
				testCategory = "splitable";
				destination = outCenters[1];
				outsourceDestinationID = parseInt(("${testCenterMap.map.outsource_dest_ids}".split(", "))[0], 10);
			} else {
				testCategory = "unsplitable";
			}
		}
		
		testAssociation.testCategory = testCategory;
		testAssociation.destination = destination;
		testAssociation.outsourceDestinationID = outsourceDestinationID;
		testCenterMap[testID] = testAssociation;
	</script>
</c:forEach>
<c:forEach items="${childSamplesList}" var="childSamples" varStatus="sample">
	<script type="text/javascript">
		var childSample = {};
		childSample.sampleNo = "${childSamples.map.child_sample_no}";
		childSample.testName = "${childSamples.map.test_name}".split("|");
		childSample.deptName = "${childSamples.map.dept_name}".split(", ");
		childSample.testID = "${childSamples.map.test_id}".split(", ");
		childSample.destination = "";
		childSample.outsourceDestinationID = null;
		if ((childSample.testID)[0] in testCenterMap) {
			var testID = (childSample.testID)[0];
			if (testCenterMap[testID].testCategory === "splitable") {
				childSample.destination = testCenterMap[testID].destination;
				childSample.outsourceDestinationID = testCenterMap[testID].outsourceDestinationID;
			} else {
				childSample.destination = "UNDECIDED";
			}
		} else {
			childSample.destination = "INHOUSE";
		}
		childSample.sampleCollectionID = "${childSamples.map.sample_collection_id}".split(", ");
		var parentSampleNo = "${childSamples.map.parent_sample_no}";
		if (!(parentSampleNo in childSamples)) {
			childSamples[parentSampleNo] = [];
		}
		childSamples[parentSampleNo].push(childSample);
	</script>
</c:forEach>