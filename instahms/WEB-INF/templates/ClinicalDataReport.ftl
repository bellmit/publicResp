<#--
   * Clinical outcomes for international trending
	 *  Parameters : fromDate, toDate
 -->

 <#--Configurable list of lab result names being used to fetch various counts

Eg: if the resultlabel to fetch the haemoglobin value in the test results table
is not "HB" as assumed and is instead "HBg" then the haemoglobin counts can be
fetched specifying "HB":"HBg" in the following map.
-->
<#assign labStrings = {"HB":"Hb", "FERRITIN":"Ferritin", "TRANS SATURATION":"Trans Saturation"
, "URR":"URR", "KT/V": "Kt/V", "ALBUMIN":"Albumin","PHOSPHORUS":"Phos", "CALCIUM":"Ca","IPTH" : "ipth", "CAXPO4":"CAXPO4","POTASSIUM":"K"}>

<#--Configurable list of vaccine names -->
<#assign vaccineNames = {"INFLUENZA":"INFLUENZA", "PNEUMOVAX":"PNEUMOVAX", "HEPATITIS B":"HEPATITIS B"}>

<#macro outputRow title BEANMAP='{count:0}' format='#'>
	<tr>
		<th  style="padding-left:14px;align:right;">${title}</th>
		<#if BEANMAP??>
			<#assign value= BEANMAP.count!0>
		<#else>
			<#assign value= "0">
		</#if>
		<td>${value?string(format)}</td>
	</tr>
</#macro>

<#macro outputPercentRow title BEANMAP='{count:0}' BEANMAP2='{count:0}'format='#'>
	<tr>
		<th  style="padding-left:14px;align:right;">${title}</th>
		<#if BEANMAP?? && BEANMAP2??>
			<#assign divisor = 0>
			<#if ((BEANMAP2.count)!1)?int == 0 >
				<#assign divisor = 1>
			<#else>
				<#assign divisor = (BEANMAP2.count)!1>
			</#if>
			<#assign value= (( ((BEANMAP.count)!0)?int / divisor) * 100)>
		<#else>
			<#assign value= "0">
		</#if>
		<td>${value?string(format)}%</td>
	</tr>
</#macro>

<#macro outputPercentCol BEANMAP='{count:0}' BEANMAP2='{count:0}'format='#'>
		<#if BEANMAP?? && BEANMAP2??>
			<#assign divisor = 0>
			<#if ((BEANMAP2.count)!1)?int == 0 >
				<#assign divisor = 1>
			<#else>
				<#assign divisor = (BEANMAP2.count)!1>
			</#if>
			<#assign value= (( ((BEANMAP.count)!0)?int / divisor) * 100)>
		<#else>
			<#assign value= "0">
		</#if>
		<td>${value?string(format)}%</td>
</#macro>

<#macro outputCol BEANMAP format='#'>
		<#if BEANMAP??>
			<#assign value= BEANMAP.count!0>
		<#else>
			<#assign value= "0">
		</#if>
		<td>${value?string(format)}</td>
</#macro>

<#macro outputHeading title sub="N">
	<tr>
		<th colspan="1" style="font-style:italic;
		<#if sub?? && sub == 'Y'>
			padding: 8px 8px 8px 8px;font-size:13;color:#A78D84;
		<#else>
			border-width: 1.5px 0px 1.5px 1.5px;border-right-width: 0px;padding: 8px 8px 2px 3px;font-size:15;color:#A3A3A3;
		</#if>
		"
		>
			${title}
		</th>
		<th style="font-style:italic;<#if sub?? && sub == 'N'> border-width: 1.5px 1.5px 1.5px 0px;</#if>"></th>
	</tr>
</#macro>

<#macro outputHeadingRow title >
	<tr>
		<th colspan="5" style="font-style:italic;border-width: 1.5px 1.5px 1.5px 1.5px;padding: 8px 8px 2px 3px;font-size:15;color:#A3A3A3;">
			${title}
		</th>
	</tr>
</#macro>

<#assign dateRange = getDatesInRange(fromDate, toDate, 'month')>
<#assign empty= {}>

<#-- Anaemia counts -->

		<#assign hbLessQuery>
			SELECT count(*) AS count
			FROM test_results_master trm
			JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
			JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE values_as_of_date::date BETWEEN ? AND ? AND ( center_id = ?::integer OR ?::integer = 0)
			AND (resultlabel_short ilike '${labStrings["HB"]}' OR resultlabel ilike '${labStrings["HB"]}')
			AND convert_to_numeric(test_value) < 10 AND convert_to_numeric(test_value)>0
		</#assign>
		<#assign hbLess = queryToDynaBean(hbLessQuery, fromDate, toDate, centerId, centerId)>

		<#assign hbMoreQuery>
			SELECT count(*) AS count
			FROM test_results_master trm
			JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
			JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE values_as_of_date::date BETWEEN ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
			AND (resultlabel_short ilike '${labStrings["HB"]}' OR resultlabel ilike '${labStrings["HB"]}')
			AND convert_to_numeric(test_value) > 10 AND convert_to_numeric(test_value) >0
		</#assign>
		<#assign hbMore = queryToDynaBean(hbMoreQuery, fromDate, toDate, centerId, centerId)>

		<#assign hbBetweenQuery>
			SELECT count(*) AS count
			FROM test_results_master trm
			JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
			JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE values_as_of_date::date BETWEEN ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
			AND ( resultlabel_short ilike '${labStrings["HB"]}' OR resultlabel ilike '${labStrings["HB"]}' )
			AND convert_to_numeric(test_value)>= 10 AND convert_to_numeric(test_value)<= 12 AND convert_to_numeric(test_value) >0
		</#assign>
		<#assign hbBetween = queryToDynaBean(hbBetweenQuery, fromDate, toDate, centerId, centerId)>

		<#assign hbInRangeQuery>
			SELECT count(*) AS count
			FROM test_results_master trm
			JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
			JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE values_as_of_date::date BETWEEN ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
			AND ( resultlabel_short ilike '${labStrings["HB"]}' OR resultlabel ilike '${labStrings["HB"]}' )
			AND convert_to_numeric(test_value) > 0
		</#assign>
		<#assign hbInRange = queryToDynaBean(hbInRangeQuery, fromDate, toDate, centerId, centerId)>

		<#assign ferritinMoreQuery>
			SELECT count(*) AS count
			FROM test_results_master trm
			JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
			JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE values_as_of_date::date BETWEEN ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
			AND (trim(resultlabel_short) ilike '${labStrings["FERRITIN"]}' OR trim(resultlabel) ilike '${labStrings["FERRITIN"]}')
			AND convert_to_numeric(test_value) > 100
		</#assign>
		<#assign ferritinMore = queryToDynaBean(ferritinMoreQuery, fromDate, toDate, centerId, centerId)>


		<#assign ferritinLessQuery>
			SELECT count(*) AS count
			FROM test_results_master trm
			JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
			JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE values_as_of_date::date BETWEEN ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
			AND (trim(resultlabel_short) ilike '${labStrings["FERRITIN"]}' OR trim(resultlabel) ilike '${labStrings["FERRITIN"]}')
			AND convert_to_numeric(test_value) <= 100 AND convert_to_numeric(test_value) >0
		</#assign>
		<#assign ferritinLess = queryToDynaBean(ferritinLessQuery, fromDate, toDate, centerId, centerId)>

		<#assign ferritinInRangeQuery>
			SELECT count(*) AS count
			FROM test_results_master trm
			JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
			JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE values_as_of_date::date BETWEEN ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
			AND (trim(resultlabel_short) ilike '${labStrings["FERRITIN"]}' OR trim(resultlabel) ilike '${labStrings["FERRITIN"]}')
			AND convert_to_numeric(test_value) > 0
		</#assign>
		<#assign ferritinInRange = queryToDynaBean(ferritinInRangeQuery, fromDate, toDate, centerId, centerId)>



		<#assign transSatMoreQuery>
			SELECT count(*) AS count
			FROM test_results_master trm
			JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
			JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE values_as_of_date::date BETWEEN ? AND ?  AND ( center_id = ?::integer OR  ?::integer= 0)
			AND (resultlabel_short ilike  '${labStrings["TRANS SATURATION"]}' OR  resultlabel ilike  '${labStrings["TRANS SATURATION"]}')
			AND convert_to_numeric(test_value) > 20
		</#assign>
		<#assign transSatMore = queryToDynaBean(transSatMoreQuery, fromDate, toDate, centerId, centerId)>

		<#assign transSatLessQuery>
			SELECT count(*) AS count
			FROM test_results_master trm
			JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
			JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE values_as_of_date::date BETWEEN ? AND ?  AND ( center_id = ?::integer OR  ?::integer= 0)
			AND (resultlabel_short ilike  '${labStrings["TRANS SATURATION"]}' OR  resultlabel ilike  '${labStrings["TRANS SATURATION"]}')
			AND convert_to_numeric(test_value) <= 20 AND convert_to_numeric(test_value) >0
		</#assign>
		<#assign transSatLess = queryToDynaBean(transSatLessQuery, fromDate, toDate, centerId, centerId)>

		<#assign transSatInRangeQuery>
			SELECT count(*) AS count
			FROM test_results_master trm
			JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
			JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE values_as_of_date::date BETWEEN ? AND ?  AND ( center_id = ?::integer OR  ?::integer= 0)
			AND (resultlabel_short ilike  '${labStrings["TRANS SATURATION"]}' OR  resultlabel ilike  '${labStrings["TRANS SATURATION"]}')
			AND convert_to_numeric(test_value) > 0
		</#assign>
		<#assign transSatInRange = queryToDynaBean(transSatInRangeQuery, fromDate, toDate, centerId, centerId)>

<#-- Anaemia counts End -->
<#-- Adequacy counts -->
		<#assign urrMoreQuery>
			SELECT count(*) AS count FROM (
				SELECT cdav.mr_no
				FROM clinical_dial_adeq_values cdav
				JOIN patient_details pd ON(pd.mr_no = cdav.mr_no)
				LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
				WHERE urr>=65 AND urr>0 AND cdav.mr_no IS NOT NULL
				AND values_as_of_date::date BETWEEN ? AND ?
				AND ( center_id = ?::integer OR  ?::integer= 0)
			) AS foo
		</#assign>
		<#assign urrMore = queryToDynaBean(urrMoreQuery, fromDate, toDate, centerId, centerId)>

		<#assign urrLessQuery>
			SELECT count(*) AS count FROM (
				SELECT cdav.mr_no
				FROM clinical_dial_adeq_values cdav
				JOIN patient_details pd ON(pd.mr_no = cdav.mr_no)
				LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
				WHERE urr<65 AND cdav.mr_no IS NOT NULL
				AND values_as_of_date::date BETWEEN ? AND ?
				AND ( center_id = ?::integer OR  ?::integer= 0)
			) AS foo
		</#assign>
		<#assign urrLess = queryToDynaBean(urrLessQuery, fromDate, toDate, centerId, centerId)>

		<#assign urrInRangeQuery>
			SELECT count(*) AS count FROM (
				SELECT cdav.mr_no
				FROM clinical_dial_adeq_values cdav
				JOIN patient_details pd ON(pd.mr_no = cdav.mr_no)
				LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
				WHERE cdav.mr_no IS NOT NULL
				AND values_as_of_date::date BETWEEN ? AND ?
				AND ( center_id = ?::integer OR  ?::integer= 0)
			) AS foo
		</#assign>
		<#assign urrInRange = queryToDynaBean(urrInRangeQuery, fromDate, toDate, centerId, centerId)>



		<#assign ktvLessQuery>
		SELECT count(*) AS count FROM (
			SELECT cdav.mr_no
			FROM clinical_dial_adeq_values cdav
			JOIN patient_details pd ON(pd.mr_no = cdav.mr_no)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE ktv<=1.2 AND cdav.mr_no IS NOT NULL
			AND values_as_of_date::date BETWEEN ? AND ?
			AND ( center_id = ?::integer OR  ?::integer= 0)
		) AS foo
		</#assign>

		<#assign ktvLess = queryToDynaBean(ktvLessQuery, fromDate, toDate, centerId, centerId)>

		<#assign ktvMoreQuery>
		SELECT count(*) AS count FROM (
			SELECT cdav.mr_no
			FROM clinical_dial_adeq_values cdav
			JOIN patient_details pd ON(pd.mr_no = cdav.mr_no)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE ktv>1.2 AND ktv>0 AND cdav.mr_no IS NOT NULL
			AND values_as_of_date::date BETWEEN ? AND ?
			AND ( center_id = ?::integer OR  ?::integer= 0)
		) AS foo
		</#assign>
		<#assign ktvMore = queryToDynaBean(ktvMoreQuery, fromDate, toDate, centerId, centerId)>

		<#assign ktvInRangeQuery>
			SELECT count(*) AS count FROM (
				SELECT cdav.mr_no
				FROM clinical_dial_adeq_values cdav
				JOIN patient_details pd ON(pd.mr_no = cdav.mr_no)
				LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
				WHERE cdav.mr_no IS NOT NULL
				AND values_as_of_date::date BETWEEN ? AND ?
				AND ( center_id = ?::integer OR  ?::integer= 0)
			) AS foo
		</#assign>
		<#assign ktvInRange = queryToDynaBean(ktvInRangeQuery, fromDate, toDate, centerId, centerId)>



		<#assign netAdeqQuery>
			SELECT count(*) AS count FROM (
				SELECT cdav.mr_no
				FROM clinical_dial_adeq_values cdav
				JOIN patient_details pd ON(pd.mr_no = cdav.mr_no)
				LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
				WHERE  cdav.mr_no IS NOT NULL
				AND values_as_of_date::date BETWEEN ? AND ?
				AND ( center_id = ?::integer OR  ?::integer= 0)
			) AS foo
		</#assign>
		<#assign netAdeq = queryToDynaBean(netAdeqQuery, fromDate, toDate, centerId, centerId)>


<#-- Adequacy counts End -->
<#-- Access counts -->
		<#assign cvcPlaceQuery>
			SELECT count (distinct dp.mr_no) as count from dialysis_session ds
			JOIN dialysis_access_types dac ON ds.access_type_id = dac.access_type_id
			LEFT JOIN dialysis_prescriptions dp ON dp.dialysis_presc_id= ds.prescription_id
			JOIN patient_details pd ON(pd.mr_no = dp.mr_no)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE access_category='CVC' AND access_mode='P' AND dp.mr_no IS NOT NULL
			AND start_time::date between ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
		</#assign>
		<#assign cvcPlace = queryToDynaBean(cvcPlaceQuery, fromDate, toDate, centerId, centerId)>

		<#assign avfPlaceQuery>
			SELECT count (distinct dp.mr_no) as count from dialysis_session ds
			JOIN dialysis_access_types dac ON ds.access_type_id = dac.access_type_id
			LEFT JOIN dialysis_prescriptions dp ON dp.dialysis_presc_id= ds.prescription_id
			JOIN patient_details pd ON(pd.mr_no = dp.mr_no)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE access_category='AVF' AND access_mode='P' AND dp.mr_no IS NOT NULL
			AND start_time::date between ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
		</#assign>
		<#assign avfPlace = queryToDynaBean(avfPlaceQuery, fromDate, toDate, centerId, centerId)>

		<#assign avfUseQuery>
			SELECT count (distinct dp.mr_no) as count from dialysis_session ds
			JOIN dialysis_access_types dac ON ds.access_type_id = dac.access_type_id
			LEFT JOIN dialysis_prescriptions dp ON dp.dialysis_presc_id= ds.prescription_id
			JOIN patient_details pd ON(pd.mr_no = dp.mr_no)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE access_category='AVF'  AND dp.mr_no IS NOT NULL
			AND start_time::date between ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
		</#assign>
		<#assign avfUse = queryToDynaBean(avfUseQuery, fromDate, toDate, centerId, centerId)>

		<#assign dialTotQuery>
			SELECT count (distinct dp.mr_no) as count from dialysis_session ds
			JOIN dialysis_access_types dac ON ds.access_type_id = dac.access_type_id
			LEFT JOIN dialysis_prescriptions dp ON dp.dialysis_presc_id= ds.prescription_id
			JOIN patient_details pd ON(pd.mr_no = dp.mr_no)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE  dp.mr_no IS NOT NULL
			AND start_time::date between ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
		</#assign>
		<#assign dialTot = queryToDynaBean(dialTotQuery, fromDate, toDate, centerId, centerId)>

		<#assign noCVCFistulaQuery>
			SELECT count (distinct dp.mr_no) as count from dialysis_session ds
			JOIN dialysis_access_types dac ON ds.access_type_id = dac.access_type_id
			LEFT JOIN dialysis_prescriptions dp ON dp.dialysis_presc_id= ds.prescription_id
			JOIN patient_details pd ON(pd.mr_no = dp.mr_no)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE access_category != 'CVC' AND access_type ilike '%fistula%'   AND dp.mr_no IS NOT NULL
			AND start_time::date between ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
		</#assign>
		<#assign noCVCFistula = queryToDynaBean(noCVCFistulaQuery, fromDate, toDate, centerId, centerId)>

		<#assign noCVCGraftQuery>
			SELECT count (distinct dp.mr_no) as count from dialysis_session ds
			JOIN dialysis_access_types dac ON ds.access_type_id = dac.access_type_id
			LEFT JOIN dialysis_prescriptions dp ON dp.dialysis_presc_id= ds.prescription_id
			JOIN patient_details pd ON(pd.mr_no = dp.mr_no)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE access_category != 'CVC' AND access_type ilike '%graft%'   AND dp.mr_no IS NOT NULL
			AND start_time::date between ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
		</#assign>
		<#assign noCVCGraft = queryToDynaBean(noCVCGraftQuery, fromDate, toDate, centerId, centerId)>


		<#assign cvcUseQuery>
			SELECT count (distinct dp.mr_no) as count from dialysis_session ds
			JOIN dialysis_access_types dac ON ds.access_type_id = dac.access_type_id
			LEFT JOIN dialysis_prescriptions dp ON dp.dialysis_presc_id= ds.prescription_id
			JOIN patient_details pd ON(pd.mr_no = dp.mr_no)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE access_category='CVC'  AND dp.mr_no IS NOT NULL
			AND start_time::date between ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
		</#assign>
		<#assign cvcUse = queryToDynaBean(cvcUseQuery, fromDate, toDate, centerId, centerId)>

		<#assign cvcOnlyQuery>
			SELECT count (distinct dp.mr_no) as count from dialysis_session ds
			JOIN dialysis_access_types dac ON ds.access_type_id = dac.access_type_id
			LEFT JOIN dialysis_prescriptions dp ON dp.dialysis_presc_id= ds.prescription_id
			JOIN patient_details pd ON(pd.mr_no = dp.mr_no)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE access_category='CVC'  AND dp.mr_no IS NOT NULL
			AND dp.mr_no NOT IN (
			SELECT distinct dp.mr_no FROM
			dialysis_session ds
			JOIN dialysis_access_types dac ON ds.access_type_id = dac.access_type_id
			LEFT JOIN dialysis_prescriptions dp ON dp.dialysis_presc_id= ds.prescription_id
			JOIN patient_details pd ON(pd.mr_no = dp.mr_no)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE access_category !='CVC' AND start_time::date between ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0))
			AND start_time::date between ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
		</#assign>
		<#assign cvcOnly = queryToDynaBean(cvcOnlyQuery, fromDate, toDate, centerId, centerId,fromDate, toDate, centerId, centerId)>

<#-- Access counts End -->



<#-- Nutrition counts -->
		<#assign albuminLessQuery>
			SELECT count(*) AS count
			FROM test_results_master trm
			JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
			JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE values_as_of_date::date BETWEEN ? AND ?
			AND ( resultlabel_short ilike  '${labStrings["ALBUMIN"]}' OR resultlabel ilike  '${labStrings["ALBUMIN"]}')
			AND convert_to_numeric(test_value) <= 3 AND convert_to_numeric(test_value) >0 AND ( center_id = ?::integer OR  ?::integer= 0)
		</#assign>
		<#assign albuminLess = queryToDynaBean(albuminLessQuery, fromDate, toDate, centerId, centerId)>


		<#assign albuminMoreQuery>
			SELECT count(*) AS count
			FROM test_results_master trm
			JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
			JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE values_as_of_date::date BETWEEN ? AND ?
			AND ( resultlabel_short ilike  '${labStrings["ALBUMIN"]}' OR resultlabel ilike  '${labStrings["ALBUMIN"]}')
			AND convert_to_numeric(test_value) > 3 AND ( center_id = ?::integer OR  ?::integer= 0)
		</#assign>
		<#assign albuminMore = queryToDynaBean(albuminMoreQuery, fromDate, toDate, centerId, centerId)>


		<#assign albuminInRangeQuery>
			SELECT count(*) AS count
			FROM test_results_master trm
			JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
			JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE values_as_of_date::date BETWEEN ? AND ?
			AND ( resultlabel_short ilike  '${labStrings["ALBUMIN"]}' OR resultlabel ilike  '${labStrings["ALBUMIN"]}')
			AND convert_to_numeric(test_value) >0 AND ( center_id = ?::integer OR  ?::integer= 0)
		</#assign>
		<#assign albuminInRange = queryToDynaBean(albuminInRangeQuery, fromDate, toDate, centerId, centerId)>



<#-- Nutrition counts End -->
<#-- Bone Mineral Management Counts -->
		<#assign phosphorusMoreQuery>
			SELECT count(*) AS count
			FROM test_results_master trm
			JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
			JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE values_as_of_date::date BETWEEN ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
			AND ( resultlabel_short ilike  '${labStrings["PHOSPHORUS"]}' OR  resultlabel ilike  '${labStrings["PHOSPHORUS"]}')
			AND convert_to_numeric(test_value) > 5.5
		</#assign>
		<#assign phosphorusMore = queryToDynaBean(phosphorusMoreQuery, fromDate, toDate, centerId, centerId)>

		<#assign phosphorusLessQuery>
			SELECT count(*) AS count
			FROM test_results_master trm
			JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
			JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE values_as_of_date::date BETWEEN ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
			AND ( resultlabel_short ilike  '${labStrings["PHOSPHORUS"]}' OR  resultlabel ilike  '${labStrings["PHOSPHORUS"]}')
			AND convert_to_numeric(test_value) <= 5.5 AND convert_to_numeric(test_value) >0
		</#assign>
		<#assign phosphorusLess = queryToDynaBean(phosphorusLessQuery, fromDate, toDate, centerId, centerId)>

		<#assign phosphorusInRangeQuery>
			SELECT count(*) AS count
			FROM test_results_master trm
			JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
			JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE values_as_of_date::date BETWEEN ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
			AND ( resultlabel_short ilike  '${labStrings["PHOSPHORUS"]}' OR  resultlabel ilike  '${labStrings["PHOSPHORUS"]}')
			AND convert_to_numeric(test_value) > 0
		</#assign>
		<#assign phosphorusInRange = queryToDynaBean(phosphorusInRangeQuery, fromDate, toDate, centerId, centerId)>


		<#assign calciumMoreQuery>
			SELECT count(*) AS count
			FROM test_results_master trm
			JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
			JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE values_as_of_date::date BETWEEN ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
			AND ( resultlabel_short ilike  '${labStrings["CALCIUM"]}' OR  resultlabel ilike  '${labStrings["CALCIUM"]}')
			AND convert_to_numeric(test_value) > 10.2
		</#assign>
		<#assign calciumMore = queryToDynaBean(calciumMoreQuery, fromDate, toDate, centerId, centerId)>

		<#assign calciumLessQuery>
			SELECT count(*) AS count
			FROM test_results_master trm
			JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
			JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE values_as_of_date::date BETWEEN ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
			AND ( resultlabel_short ilike  '${labStrings["CALCIUM"]}' OR  resultlabel ilike  '${labStrings["CALCIUM"]}')
			AND convert_to_numeric(test_value) <= 10.2 AND convert_to_numeric(test_value) >0
		</#assign>
		<#assign calciumLess = queryToDynaBean(calciumLessQuery, fromDate, toDate, centerId, centerId)>

		<#assign calciumInRangeQuery>
			SELECT count(*) AS count
			FROM test_results_master trm
			JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
			JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE values_as_of_date::date BETWEEN ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
			AND ( resultlabel_short ilike  '${labStrings["CALCIUM"]}' OR  resultlabel ilike  '${labStrings["CALCIUM"]}')
			AND convert_to_numeric(test_value) > 0
		</#assign>
		<#assign calciumInRange = queryToDynaBean(calciumInRangeQuery, fromDate, toDate, centerId, centerId)>



		<#assign ipthLessQuery>
			SELECT count(*) AS count
			FROM test_results_master trm
			JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
			JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE values_as_of_date::date BETWEEN ? AND ?
			AND ( resultlabel_short ilike  '${labStrings["IPTH"]}' OR  resultlabel ilike  '${labStrings["IPTH"]}')
			AND convert_to_numeric(test_value) < 150 AND convert_to_numeric(test_value) >0
			AND ( center_id = ?::integer OR  ?::integer= 0)
		</#assign>
		<#assign ipthLess = queryToDynaBean(ipthLessQuery, fromDate, toDate, centerId, centerId)>

	<#assign ipthMoreQuery>
			SELECT count(*) AS count
			FROM test_results_master trm
			JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
			JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE values_as_of_date::date BETWEEN ? AND ?
			AND ( resultlabel_short ilike  '${labStrings["IPTH"]}' OR  resultlabel ilike  '${labStrings["IPTH"]}')
			AND convert_to_numeric(test_value) > 300
			AND ( center_id = ?::integer OR  ?::integer= 0)
	</#assign>
	<#assign ipthMore = queryToDynaBean(ipthMoreQuery, fromDate, toDate, centerId, centerId)>

	<#assign ipthInRangeQuery>
		SELECT count(*) AS count
		FROM test_results_master trm
		JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
		LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
		LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
		JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
		WHERE values_as_of_date::date BETWEEN ? AND ?
		AND ( resultlabel_short ilike  '${labStrings["IPTH"]}' OR  resultlabel ilike  '${labStrings["IPTH"]}')
		AND convert_to_numeric(test_value) > 0
		AND ( center_id = ?::integer OR  ?::integer= 0)
	</#assign>
	<#assign ipthInRange = queryToDynaBean(ipthInRangeQuery, fromDate, toDate, centerId, centerId)>



	<#assign potassiumLessQuery>
		SELECT count(*) AS count
		FROM test_results_master trm
		JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
		LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
		LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
		JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
		LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
		WHERE values_as_of_date::date BETWEEN ? AND ?
		AND (resultlabel_short ilike  '${labStrings["POTASSIUM"]}' OR  resultlabel ilike  '${labStrings["POTASSIUM"]}')
		AND convert_to_numeric(test_value) < 5 AND convert_to_numeric(test_value) >0
		AND ( center_id = ?::integer OR  ?::integer= 0)
	</#assign>
	<#assign potassiumLess = queryToDynaBean(potassiumLessQuery, fromDate, toDate, centerId, centerId)>

	<#assign potassiumMoreQuery>
			SELECT count(*) AS count
			FROM test_results_master trm
			JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
			LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
			JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE values_as_of_date::date BETWEEN ? AND ?
			AND (resultlabel_short ilike  '${labStrings["POTASSIUM"]}' OR  resultlabel  ilike  '${labStrings["POTASSIUM"]}')
			AND convert_to_numeric(test_value) > 5
			AND ( center_id = ?::integer OR  ?::integer= 0)
	</#assign>
	<#assign potassiumMore = queryToDynaBean(potassiumMoreQuery, fromDate, toDate, centerId, centerId)>

	<#assign potassiumInRangeQuery>
		SELECT count(*) AS count
		FROM test_results_master trm
		JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
		LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
		LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
		JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
		LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
		WHERE values_as_of_date::date BETWEEN ? AND ?
		AND (resultlabel_short ilike  '${labStrings["POTASSIUM"]}' OR  resultlabel ilike  '${labStrings["POTASSIUM"]}')
		AND convert_to_numeric(test_value) > 0
		AND ( center_id = ?::integer OR  ?::integer= 0)
	</#assign>
	<#assign potassiumInRange = queryToDynaBean(potassiumInRangeQuery, fromDate, toDate, centerId, centerId)>



	<#assign caxpo4LessQuery>

	 		SELECT count(*) AS count FROM  ( SELECT ROUND((SUM(calcium)*SUM(phosphorus)),2) AS caxpo4 FROM (
		  (  SELECT COALESCE(ROUND(convert_to_numeric(test_value),2),0) AS calcium,  0 AS phosphorus, clrd.clinical_lab_recorded_id
		 FROM test_results_master trm
		 JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
		 LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
		 LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
		 JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
		 LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
		  WHERE values_as_of_date::date BETWEEN ? AND ?
		 AND  (resultlabel_short ilike 'ca' OR resultlabel ilike 'calcium')
		 AND ( center_id = ?::integer OR  ?::integer= 0) AND convert_to_numeric(test_value) >0
		 UNION
		  ( SELECT 0 AS calicum, ROUND(convert_to_numeric(test_value),2) AS phosphorus, clrd.clinical_lab_recorded_id
		 FROM test_results_master trm
		 JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
		 LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
		 LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
		 JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
		LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
		  WHERE values_as_of_date::date BETWEEN ? AND ?
		  AND (resultlabel_short ilike 'phos' OR resultlabel ilike 'Phosphorus') AND convert_to_numeric(test_value) >0
		  AND ( center_id = ?::integer OR  ?::integer= 0)

                 ))) AS foo
                 GROUP BY clinical_lab_recorded_id
                 HAVING (ROUND((SUM(calcium)*SUM(phosphorus)),2)>0 AND ROUND((SUM(calcium)*SUM(phosphorus)),2)<=55)
	) AS foo2
	</#assign>
	<#assign caxpo4Less = queryToDynaBean(caxpo4LessQuery, fromDate, toDate, centerId, centerId, fromDate, toDate, centerId, centerId)>

	<#assign caxpo4MoreQuery>
					SELECT count(*) AS count FROM  ( SELECT ROUND((SUM(calcium)*SUM(phosphorus)),2) AS caxpo4 FROM (
		  (  SELECT COALESCE(ROUND(convert_to_numeric(test_value),2),0) AS calcium,  0 AS phosphorus, clrd.clinical_lab_recorded_id
		 FROM test_results_master trm
		 JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
		 LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
		 LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
		 JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
		  WHERE values_as_of_date::date BETWEEN ? AND ?
		 AND  (resultlabel_short ilike 'ca' OR resultlabel ilike 'calcium') AND convert_to_numeric(test_value) >0
		 AND ( center_id = ?::integer OR  ?::integer= 0)
		 UNION
		  ( SELECT 0 AS calicum, ROUND(convert_to_numeric(test_value),2) AS phosphorus, clrd.clinical_lab_recorded_id
		 FROM test_results_master trm
		 JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
		 LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
		 LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
		 JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
		 LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
		  WHERE values_as_of_date::date BETWEEN ? AND ?
		  AND (resultlabel_short ilike 'phos' OR resultlabel ilike 'Phosphorus') AND convert_to_numeric(test_value) >0
		  AND ( center_id = ?::integer OR  ?::integer= 0)

                 ))) AS foo
                 GROUP BY clinical_lab_recorded_id
                 HAVING (ROUND((SUM(calcium)*SUM(phosphorus)),2)>0 AND ROUND((SUM(calcium)*SUM(phosphorus)),2)> 55)
	) AS foo2
		</#assign>
		<#assign caxpo4More = queryToDynaBean(caxpo4MoreQuery, fromDate, toDate, centerId, centerId, fromDate, toDate, centerId, centerId)>

	<#assign caxpo4InRangeQuery>
 				SELECT count(*) AS count FROM  ( SELECT ROUND((SUM(calcium)*SUM(phosphorus)),2) AS caxpo4 FROM (
		  (  SELECT COALESCE(ROUND(convert_to_numeric(test_value),2),0) AS calcium,  0 AS phosphorus, clrd.clinical_lab_recorded_id
		 FROM test_results_master trm
		 JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
		 LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
		 LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
		 JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
		 LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
		  WHERE values_as_of_date::date BETWEEN ? AND ?
		 AND  (resultlabel_short ilike 'ca' OR resultlabel ilike 'calcium') AND convert_to_numeric(test_value) >0
		 AND ( center_id = ?::integer OR  ?::integer= 0)
		 UNION
		  ( SELECT 0 AS calicum, ROUND(convert_to_numeric(test_value),2) AS phosphorus, clrd.clinical_lab_recorded_id
		 FROM test_results_master trm
		 JOIN clinical_lab_result clr ON (trm.resultlabel_id = clr.resultlabel_id)
		 LEFT JOIN clinical_lab_values clv ON (clv.resultlabel_id = clr.resultlabel_id)
		 LEFT JOIN clinical_lab_recorded clrd ON (clrd.clinical_lab_recorded_id  = clv.clinical_lab_recorded_id)
		 JOIN patient_details pd ON(pd.mr_no = clrd.mrno)
		 LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
		  WHERE values_as_of_date::date BETWEEN ? AND ?
		  AND (resultlabel_short ilike 'phos' OR resultlabel ilike 'Phosphorus') AND convert_to_numeric(test_value) >0
		  AND ( center_id = ?::integer OR  ?::integer= 0)

                 ))) AS foo
                 GROUP BY clinical_lab_recorded_id
                 HAVING ROUND((SUM(calcium)*SUM(phosphorus)),2)>0
	) AS foo2
	</#assign>
	<#assign caxpo4InRange = queryToDynaBean(caxpo4InRangeQuery, fromDate, toDate, centerId, centerId, fromDate, toDate, centerId, centerId)>





<#-- Bone Mineral Management Counts End -->
<#-- Hospitilization cause in the month of initial hospitilization -->

		<#assign hosplnReasonQuery =
		"SELECT  count(reason) AS count ,reason
					FROM clinical_hospitalization_details chd
					JOIN clinical_hospitalization_reasons  chr ON (chr.reason_id = chd.reason_id)
					JOIN clinical_hospitalization ch ON ch.hospitalization_id = chd.hospitalization_id
					JOIN patient_details pd ON(pd.mr_no = ch.mr_no)
					LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
					WHERE admission_date::date BETWEEN ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
					GROUP BY reason
		">
		<#assign hosplnReason = queryToDynaList(hosplnReasonQuery, fromDate, toDate, centerId, centerId)>
		<#assign hosplnReasonCount = listBeanToMapNumeric(hosplnReason,'reason','count')>

<#-- Infection Counts -->
		<#assign avfOrAvgQuery>
			SELECT count(cir.mr_no) AS count FROM clinical_infections_recorded cir
		JOIN clinical_infections ci ON ci.clinical_infections_recorded_id = cir.clinical_infections_recorded_id
		JOIN clinical_infections_master cif ON cif.infection_type_id = ci.infection_type_id
		JOIN patient_details pd ON(pd.mr_no = cir.mr_no)
		LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
		WHERE infection_type IN ('AVF','AVG') AND infection_status='Y'
		AND infection_effective_date BETWEEN ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
		</#assign>
		<#assign avfOrAvg = queryToDynaBean(avfOrAvgQuery, fromDate, toDate, centerId, centerId)>

		<#assign cvcQuery>
			SELECT count(cir.mr_no) AS count FROM clinical_infections_recorded cir
		JOIN clinical_infections ci ON ci.clinical_infections_recorded_id = cir.clinical_infections_recorded_id
		JOIN clinical_infections_master cif ON cif.infection_type_id = ci.infection_type_id
		JOIN patient_details pd ON(pd.mr_no = cir.mr_no)
		LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
		WHERE infection_type ilike 'cvc' AND infection_status='Y'
		AND infection_effective_date BETWEEN ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
		</#assign>
		<#assign cvc = queryToDynaBean(cvcQuery, fromDate, toDate, centerId, centerId)>

		<#assign nonAccessQuery>
			SELECT count(cir.mr_no) AS count FROM clinical_infections_recorded cir
		JOIN clinical_infections ci ON ci.clinical_infections_recorded_id = cir.clinical_infections_recorded_id
		JOIN clinical_infections_master cif ON cif.infection_type_id = ci.infection_type_id
		JOIN patient_details pd ON(pd.mr_no = cir.mr_no)
		LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
		WHERE lower(infection_type) NOT IN ('avf','avg','cvc','hepatitis b','hepatitis c','mrsa','vre')  AND infection_status='Y'
		AND infection_effective_date BETWEEN ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
		</#assign>
		<#assign nonAccess = queryToDynaBean(nonAccessQuery, fromDate, toDate, centerId, centerId)>

		<#assign hepatitisBQuery>
			SELECT count(cir.mr_no) AS count FROM clinical_infections_recorded cir
		JOIN clinical_infections ci ON ci.clinical_infections_recorded_id = cir.clinical_infections_recorded_id
		JOIN clinical_infections_master cif ON cif.infection_type_id = ci.infection_type_id
		JOIN patient_details pd ON(pd.mr_no = cir.mr_no)
		LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
		WHERE infection_type ilike 'hepatitis b' AND infection_status='Y'
		AND infection_effective_date BETWEEN ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
		</#assign>
		<#assign hepatitisB = queryToDynaBean(hepatitisBQuery, fromDate, toDate, centerId, centerId)>

		<#assign allHepatitisBQuery>
			SELECT count(cir.mr_no) AS count  FROM clinical_infections_recorded cir
		JOIN clinical_infections ci ON ci.clinical_infections_recorded_id = cir.clinical_infections_recorded_id
		JOIN clinical_infections_master cif ON cif.infection_type_id = ci.infection_type_id
		JOIN patient_details pd ON(pd.mr_no = cir.mr_no)
		LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
		WHERE infection_type ilike 'hepatitis b' AND infection_status='Y' AND ( center_id = ?::integer OR  ?::integer= 0)
		</#assign>
		<#assign allHepatitisB = queryToDynaBean(allHepatitisBQuery, centerId, centerId)>

		<#assign hepatitisCQuery>
			SELECT count(cir.mr_no) AS count FROM clinical_infections_recorded cir
		JOIN clinical_infections ci ON ci.clinical_infections_recorded_id = cir.clinical_infections_recorded_id
		JOIN clinical_infections_master cif ON cif.infection_type_id = ci.infection_type_id
		JOIN patient_details pd ON(pd.mr_no = cir.mr_no)
		LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
		WHERE infection_type ilike 'hepatitis C' AND infection_status='Y' AND ( center_id = ?::integer OR  ?::integer= 0)
		AND infection_effective_date BETWEEN ? AND ?
		</#assign>
		<#assign hepatitisC = queryToDynaBean(hepatitisCQuery, centerId, centerId, fromDate, toDate)>

		<#assign allHepatitisCQuery>
			SELECT count(cir.mr_no) AS count FROM clinical_infections_recorded cir
		JOIN clinical_infections ci ON ci.clinical_infections_recorded_id = cir.clinical_infections_recorded_id
		JOIN clinical_infections_master cif ON cif.infection_type_id = ci.infection_type_id
		JOIN patient_details pd ON(pd.mr_no = cir.mr_no)
		LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
		WHERE infection_type ilike 'hepatitis C' AND infection_status='Y' AND ( center_id = ?::integer OR  ?::integer= 0)
		</#assign>
		<#assign allHepatitisC = queryToDynaBean(allHepatitisCQuery, centerId, centerId)>


		<#assign mRSAQuery>
			SELECT count(cir.mr_no) AS count FROM clinical_infections_recorded cir
		JOIN clinical_infections ci ON ci.clinical_infections_recorded_id = cir.clinical_infections_recorded_id
		JOIN clinical_infections_master cif ON cif.infection_type_id = ci.infection_type_id
		JOIN patient_details pd ON(pd.mr_no = cir.mr_no)
		LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
		WHERE infection_type ilike 'mRSA' AND infection_status='Y'
		AND infection_effective_date BETWEEN ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
		</#assign>
		<#assign mRSA = queryToDynaBean(mRSAQuery, fromDate, toDate, centerId, centerId)>

		<#assign vREQuery>
			SELECT count(cir.mr_no) AS count FROM clinical_infections_recorded cir
		JOIN clinical_infections ci ON ci.clinical_infections_recorded_id = cir.clinical_infections_recorded_id
		JOIN clinical_infections_master cif ON cif.infection_type_id = ci.infection_type_id
		JOIN patient_details pd ON(pd.mr_no = cir.mr_no)
		LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
		WHERE infection_type ilike 'vRE' AND infection_status='Y'
		AND infection_effective_date BETWEEN ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
		</#assign>
		<#assign vRE = queryToDynaBean(vREQuery, fromDate, toDate, centerId, centerId)>

<#-- Infection Counts End -->
<#-- Vaccination Counts Begin -->
		<#assign influenza8MonthsQuery>
			SELECT COUNT(DISTINCT cv.mr_no) AS count FROM clinical_vaccinations_details cvd
			JOIN clinical_vaccination cv ON cvd.vaccination_id =cv.vaccination_id
			JOIN clinical_vaccinations_master cvm ON (cvm.vaccination_type_id = cvd.vaccination_type_id)
			JOIN patient_details pd ON(pd.mr_no = cv.mr_no)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE vaccination_date BETWEEN
			?
			AND
			?
			AND vaccination_status ='Y' AND ( center_id = ?::integer OR  ?::integer= 0)
			AND vaccination_type ilike '${vaccineNames["INFLUENZA"]}'
		</#assign>
		<#assign influenza8Months = queryToDynaBean(influenza8MonthsQuery, fromDate, toDate, centerId, centerId)>

		<#assign influenzaNotReceivedQuery>
			SELECT count(DISTINCT cv.mr_no)  AS count,  reason_name AS reason FROM clinical_vaccinations_details cvd
			JOIN clinical_vaccination cv ON cvd.vaccination_id =cv.vaccination_id
			JOIN clinical_vaccinations_master cvm ON (cvm.vaccination_type_id = cvd.vaccination_type_id)
			LEFT JOIN clinical_vacc_no_reason cvnr ON cvnr.reason_id = cvd.no_reason_id
			JOIN patient_details pd ON(pd.mr_no = cv.mr_no)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE
			vaccination_date BETWEEN ? AND ?
			AND
			vaccination_status ='N' AND ( center_id = ?::integer OR  ?::integer= 0)
			AND vaccination_type ilike '${vaccineNames["INFLUENZA"]}'
			GROUP BY reason_name
		</#assign>
		<#assign influenzaNotReceivedList = queryToDynaList(influenzaNotReceivedQuery, fromDate, toDate, centerId, centerId)>
		<#assign influenzaNotReceived = listBeanToMapNumeric(influenzaNotReceivedList,'reason','count')>

		<#assign influenzaRefusedQuery>
			SELECT count(DISTINCT cv.mr_no) AS count FROM clinical_vaccinations_details cvd
			JOIN clinical_vaccination cv ON cvd.vaccination_id =cv.vaccination_id
			JOIN clinical_vaccinations_master cvm ON (cvm.vaccination_type_id = cvd.vaccination_type_id)
			LEFT JOIN clinical_vacc_no_reason cvnr ON cvnr.reason_id = cvd.no_reason_id
			JOIN patient_details pd ON(pd.mr_no = cv.mr_no)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE
			vaccination_date BETWEEN ? AND ?
			AND
			vaccination_status ='R'
			AND vaccination_type ilike '${vaccineNames["INFLUENZA"]}' AND ( center_id = ?::integer OR  ?::integer= 0)
		</#assign>
		<#assign influenzaRefused = queryToDynaBean(influenzaRefusedQuery, fromDate, toDate, centerId, centerId)>

		<#assign pneumovax5YrsQuery>
			SELECT count(DISTINCT cv.mr_no) AS count FROM clinical_vaccinations_details cvd
			JOIN clinical_vaccination cv ON cvd.vaccination_id =cv.vaccination_id
			JOIN clinical_vaccinations_master cvm ON (cvm.vaccination_type_id = cvd.vaccination_type_id)
			LEFT JOIN clinical_vacc_no_reason cvnr ON cvnr.reason_id = cvd.no_reason_id
			JOIN patient_details pd ON(pd.mr_no = cv.mr_no)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE
			vaccination_date::DATE BETWEEN (current_date - interval'5 years')::date AND current_date::DATE
			AND
			vaccination_status ='Y' AND ( center_id = ?::integer OR  ?::integer= 0)
			AND vaccination_type ilike '${vaccineNames["PNEUMOVAX"]}'
		</#assign>
		<#assign pneumovax5Yrs = queryToDynaBean(pneumovax5YrsQuery, centerId, centerId)>


		<#assign pneumovaxNotReceivedQuery>
			SELECT count(DISTINCT cv.mr_no) AS count, reason_name as reason  FROM clinical_vaccinations_details cvd
			JOIN clinical_vaccinations_master cvm ON (cvm.vaccination_type_id = cvd.vaccination_type_id)
			JOIN clinical_vaccination cv ON cvd.vaccination_id =cv.vaccination_id
			LEFT JOIN clinical_vacc_no_reason cvnr ON cvnr.reason_id = cvd.no_reason_id
			JOIN patient_details pd ON(pd.mr_no = cv.mr_no)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE
			vaccination_date BETWEEN ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
			AND
			vaccination_status ='N'
			AND vaccination_type ilike '${vaccineNames["PNEUMOVAX"]}'
			GROUP BY reason_name
		</#assign>
		<#assign pneumovaxNotReceivedList = queryToDynaList(pneumovaxNotReceivedQuery, fromDate, toDate, centerId, centerId)>
		<#assign pneumovaxNotReceived = listBeanToMapNumeric(pneumovaxNotReceivedList,'reason','count')>


		<#assign pneumovaxRefusedQuery>
			SELECT count(DISTINCT cv.mr_no) AS count FROM clinical_vaccinations_details cvd
			JOIN clinical_vaccinations_master cvm ON (cvm.vaccination_type_id = cvd.vaccination_type_id)
			JOIN clinical_vaccination cv ON cvd.vaccination_id =cv.vaccination_id
			LEFT JOIN clinical_vacc_no_reason cvnr ON cvnr.reason_id = cvd.no_reason_id
			JOIN patient_details pd ON(pd.mr_no = cv.mr_no)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE
			vaccination_date BETWEEN ? AND ?
			AND
			vaccination_status ='R'
			AND vaccination_type ilike '${vaccineNames["PNEUMOVAX"]}' AND ( center_id = ?::integer OR  ?::integer= 0)
		</#assign>
		<#assign pneumovaxRefused = queryToDynaBean(pneumovaxRefusedQuery, fromDate, toDate, centerId, centerId)>

<#--Configurable list of hepatitis reasons -->


		<#assign hepatitisInProcessQuery>
			SELECT count(DISTINCT cv.mr_no)  AS count FROM clinical_vaccinations_details cvd
			JOIN clinical_vaccinations_master cvm ON (cvm.vaccination_type_id = cvd.vaccination_type_id)
			JOIN clinical_vaccination cv ON cvd.vaccination_id =cv.vaccination_id
			LEFT JOIN clinical_vacc_no_reason cvnr ON cvnr.reason_id = cvd.no_reason_id
			JOIN patient_details pd ON(pd.mr_no = cv.mr_no)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE
			next_due_date BETWEEN ? AND ?
			AND
			( vaccination_status ='N' OR vaccination_status ='R')
			AND vaccination_type ilike '${vaccineNames["HEPATITIS B"]}'
			AND ( center_id = ?::integer OR  ?::integer= 0)
		</#assign>
		<#assign hepatitisInProcess = queryToDynaBean(hepatitisInProcessQuery, fromDate, toDate, centerId, centerId)>


		<#assign hepatitisOneFullQuery>
			SELECT count(DISTINCT cv.mr_no) AS count FROM clinical_vaccinations_details cvd
			JOIN clinical_vaccinations_master cvm ON (cvm.vaccination_type_id = cvd.vaccination_type_id)
			JOIN clinical_vaccination cv ON cvd.vaccination_id =cv.vaccination_id
			LEFT JOIN clinical_vacc_no_reason cvnr ON cvnr.reason_id = cvd.no_reason_id
			JOIN patient_details pd ON(pd.mr_no = cv.mr_no)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE
			vaccination_date BETWEEN ? AND ?
			AND
			( vaccination_status ='Y')
			AND vaccination_type ilike '${vaccineNames["HEPATITIS B"]}'
			AND ( center_id = ?::integer OR  ?::integer= 0)
		</#assign>
		<#assign hepatitisOneFull = queryToDynaBean(hepatitisOneFullQuery, fromDate, toDate, centerId, centerId)>

		<#assign hepatitisRefusedQuery>
			SELECT count(DISTINCT cv.mr_no) AS count FROM clinical_vaccinations_details cvd
			JOIN clinical_vaccinations_master cvm ON (cvm.vaccination_type_id = cvd.vaccination_type_id)
			JOIN clinical_vaccination cv ON cvd.vaccination_id =cv.vaccination_id
			LEFT JOIN clinical_vacc_no_reason cvnr ON cvnr.reason_id = cvd.no_reason_id
			JOIN patient_details pd ON(pd.mr_no = cv.mr_no)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE
			vaccination_date BETWEEN ? AND ?
			AND
			vaccination_status ='R'
			AND vaccination_type ilike '${vaccineNames["HEPATITIS B"]}'
			AND ( center_id = ?::integer OR  ?::integer= 0)
		</#assign>
		<#assign hepatitisRefused = queryToDynaBean(hepatitisRefusedQuery, fromDate, toDate, centerId, centerId)>

		<#assign hepatitisNotReceivedQuery>
			SELECT count(DISTINCT cv.mr_no) AS count, reason_name AS reason  FROM clinical_vaccinations_details cvd
			JOIN clinical_vaccinations_master cvm ON (cvm.vaccination_type_id = cvd.vaccination_type_id)
			JOIN clinical_vaccination cv ON cvd.vaccination_id =cv.vaccination_id
			LEFT JOIN clinical_vacc_no_reason cvnr ON cvnr.reason_id = cvd.no_reason_id
			JOIN patient_details pd ON(pd.mr_no = cv.mr_no)
			LEFT JOIN patient_registration pr ON(pr.patient_id = coalesce(pd.visit_id,previous_visit_id))
			WHERE
			vaccination_date BETWEEN ? AND ?
			AND
			vaccination_status ='N'
			AND vaccination_type ilike '${vaccineNames["HEPATITIS B"]}'
			AND ( center_id = ?::integer OR  ?::integer= 0)
			GROUP BY reason_name
		</#assign>

		<#assign hepatitisNotReceivedList = queryToDynaList(hepatitisNotReceivedQuery, fromDate, toDate, centerId, centerId)>
		<#assign hepatitisNotReceived = listBeanToMapNumeric(hepatitisNotReceivedList,'reason','count')>

<#-- Mortality-->
		<#assign mortalityQuery>
			SELECT  count(DISTINCT pd.mr_no) AS count, reason AS reason
			from patient_details pd
			JOIN patient_registration pr on (pr.patient_id = coalesce(pd.visit_id,pd.previous_visit_id))
			LEFT JOIN doctors d on (d.doctor_id = pr.doctor)
			LEFT JOIN death_reason_master drm ON (drm.reason_id=pd.death_reason_id)
			where death_date::DATE BETWEEN ? AND ? AND ( center_id = ?::integer OR  ?::integer= 0)
			GROUP BY reason
		</#assign>

		<#assign mortalityList = queryToDynaList(mortalityQuery, fromDate, toDate, centerId, centerId)>
		<#assign mortality = listBeanToMapNumeric(mortalityList,'reason','count')>


		<#assign prefquery = "SELECT hospital_name FROM generic_preferences">
		<#assign prefMap = queryToDynaBean(prefquery)>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Clinical Outcomes Report - Insta HMS</title>
	<style>
		@page {
			size: A4 portrait;
			margin: 36pt 36pt 36pt 36pt;
		}
	body {
		font-family: Arial, sans-serif;
	}
	table.report {
		empty-cells: show;
		font-size: 9pt;
		border-collapse: collapse;
		border: 1px solid black;
	}

	table.report th {
		border: 1px solid black;
		padding: 2px 8px 2px 3px;
	}

	table.report td {
		padding: 2px 4px 2px 4px;
		border: 1px solid black;
	}

	table.report td.heading {
		font-weight: bold;
	}
	table.report th {text-align: left}
	table.report td {text-align: right}
	</style>
</head>

<#setting date_format="dd-MM-yyyy">
<body>
<#escape x as x?html>
<div align="center">
	<span style="font-size: 14pt;font-weight: bold;margin-top:15px;">${prefMap.hospital_name!?upper_case}<br/></span>
	<span style="font-size: 12pt;font-weight: bold;margin-top:15px;">Clinical Outcomes Report</span>
	<p style="margin-top:2px;">(${fromDate} - ${toDate})
	<font size="1pt">Value as of ${currDateTime!}</font></p>
</div>

<div align="left">
<table class="report" width="95%" style="border-left-width: 0px; border-right-width: 0px;">
	<tr>
		<th>Outcome Indicator</th>
		<th>Total Samples</th>
		<th>Total In Range</th>
		<th>Percentage In Range</th>
	</tr>
	<@outputHeadingRow title='Adequacy'/>
	<tr>
		<th>
			Kt/V &lt; 1.2
		</th>
		<@outputCol  BEANMAP=ktvInRange />
		<@outputCol  BEANMAP=ktvLess />
		<@outputPercentCol  BEANMAP=ktvLess  BEANMAP2=ktvInRange/>

	</tr>
	<tr>
		<th>
			Kt/V &gt; 1.2
		</th>
		<@outputCol  BEANMAP=ktvInRange />
		<@outputCol  BEANMAP=ktvMore />
		<@outputPercentCol  BEANMAP=ktvMore  BEANMAP2=ktvInRange/>

	</tr>
	<tr>
		<th>
			URR &lt;65
		</th>
		<@outputCol  BEANMAP=urrInRange />
		<@outputCol  BEANMAP=urrLess />
		<@outputPercentCol  BEANMAP=urrLess  BEANMAP2=urrInRange/>

	</tr>
	<tr>
		<th>
			URR &gt; 65
		</th>
		<@outputCol  BEANMAP=urrInRange />
		<@outputCol  BEANMAP=urrMore />
		<@outputPercentCol  BEANMAP=urrMore  BEANMAP2=urrInRange/>

	</tr>
	<@outputHeadingRow title='Anemia'/>
	<tr>
		<th>
			Hgb &lt; 10
		</th>
		<@outputCol  BEANMAP=hbInRange />
		<@outputCol  BEANMAP=hbLess />
		<@outputPercentCol  BEANMAP=hbLess  BEANMAP2=hbInRange/>

	</tr>
	<tr>
		<th>
			Hgb &gt; 10
		</th>
		<@outputCol  BEANMAP=hbInRange />
		<@outputCol  BEANMAP=hbMore />
		<@outputPercentCol  BEANMAP=hbMore  BEANMAP2=hbInRange/>

	</tr>
	<tr>
		<th>
			Hgb 10-12
		</th>
		<@outputCol  BEANMAP=hbInRange />
		<@outputCol  BEANMAP=hbBetween />
		<@outputPercentCol  BEANMAP=hbBetween  BEANMAP2=hbInRange/>

	</tr>
	<tr>
		<th>
			Ferritin &gt; 100
		</th>
		<@outputCol  BEANMAP=ferritinInRange />
		<@outputCol  BEANMAP=ferritinMore />
		<@outputPercentCol  BEANMAP=ferritinMore  BEANMAP2=ferritinInRange/>

	</tr>
	<tr>
		<th>
			Ferritin &lt; 100
		</th>
		<@outputCol  BEANMAP=ferritinInRange />
		<@outputCol  BEANMAP=ferritinLess />
		<@outputPercentCol  BEANMAP=ferritinLess  BEANMAP2=ferritinInRange/>

	</tr>
	<tr>
		<th>
			Trans Saturation &gt; 20
		</th>
		<@outputCol  BEANMAP=transSatInRange />
		<@outputCol  BEANMAP=transSatMore />
		<@outputPercentCol  BEANMAP=transSatMore  BEANMAP2=transSatInRange/>

	</tr>
	<tr>
		<th>
			Trans Saturation &lt; 20
		</th>
		<@outputCol  BEANMAP=transSatInRange />
		<@outputCol  BEANMAP=transSatLess />
		<@outputPercentCol  BEANMAP=transSatLess  BEANMAP2=transSatInRange/>

	</tr>
	<@outputHeadingRow title='Nutrition'/>
	<tr>
		<th>
			Albumin &lt; 3.5
		</th>
		<@outputCol  BEANMAP=albuminInRange />
		<@outputCol  BEANMAP=albuminLess />
		<@outputPercentCol  BEANMAP=albuminLess  BEANMAP2=albuminInRange/>

	</tr>
	<tr>
		<th>
			Albumin &gt; 3.5
		</th>
		<@outputCol  BEANMAP=albuminInRange />
		<@outputCol  BEANMAP=albuminMore />
		<@outputPercentCol  BEANMAP=albuminMore  BEANMAP2=albuminInRange/>

	</tr>
	<tr>
		<th>
			Potassium &gt; 5.0
		</th>
		<@outputCol  BEANMAP=potassiumInRange />
		<@outputCol  BEANMAP=potassiumMore />
		<@outputPercentCol  BEANMAP=potassiumMore  BEANMAP2=potassiumInRange/>

	</tr>
	<tr>
		<th>
			Potassium &lt; 5.0
		</th>
		<@outputCol  BEANMAP=potassiumInRange />
		<@outputCol  BEANMAP=potassiumLess />
		<@outputPercentCol  BEANMAP=potassiumLess  BEANMAP2=potassiumInRange/>

	</tr>
	<@outputHeadingRow title='Bone Mineral Management'/>
	<tr>
		<th>
			Phos &lt; 5.5
		</th>
		<@outputCol  BEANMAP=phosphorusInRange />
		<@outputCol  BEANMAP=phosphorusLess />
		<@outputPercentCol  BEANMAP=phosphorusLess  BEANMAP2=phosphorusInRange/>

	</tr>
	<tr>
		<th>
			Phos &gt; 5.5
		</th>
		<@outputCol  BEANMAP=phosphorusInRange />
		<@outputCol  BEANMAP=phosphorusMore />
		<@outputPercentCol  BEANMAP=phosphorusMore  BEANMAP2=phosphorusInRange/>

	</tr>
	<tr>
		<th>
			Calcium &lt; 10.2
		</th>
		<@outputCol  BEANMAP=calciumInRange />
		<@outputCol  BEANMAP=calciumLess />
		<@outputPercentCol  BEANMAP=calciumLess  BEANMAP2=calciumInRange/>
	</tr>
	<tr>
		<th>
			Calcium &gt; 10.2
		</th>
		<@outputCol  BEANMAP=calciumInRange />
		<@outputCol  BEANMAP=calciumMore />
		<@outputPercentCol  BEANMAP=calciumMore  BEANMAP2=calciumInRange/>

	</tr>
	<tr>
		<th>
			Caxpo4 &gt; 55
		</th>
		<@outputCol  BEANMAP=caxpo4InRange />
		<@outputCol  BEANMAP=caxpo4More />
		<@outputPercentCol  BEANMAP=caxpo4More  BEANMAP2=caxpo4InRange/>
	</tr>
	<tr>
		<th>
			Caxpo4 &lt; 55
		</th>
		<@outputCol  BEANMAP=caxpo4InRange />
		<@outputCol  BEANMAP=caxpo4Less />
		<@outputPercentCol  BEANMAP=caxpo4Less  BEANMAP2=caxpo4InRange/>

	</tr>
	<tr>
		<th>
			 iPTH &gt; 150
		</th>
		<@outputCol  BEANMAP=ipthInRange />
		<@outputCol  BEANMAP=ipthMore />
		<@outputPercentCol  BEANMAP=ipthMore  BEANMAP2=ipthInRange/>

	</tr>
	<tr>
		<th>
			iPTH &lt; 300
		</th>
		<@outputCol  BEANMAP=ipthInRange />
		<@outputCol  BEANMAP=ipthLess />
		<@outputPercentCol  BEANMAP=ipthLess  BEANMAP2=ipthInRange/>

	</tr>
	<@outputHeadingRow title='Access'/>
	<tr>
		<th>
			# of patients with a Fistula with no CVC
		</th>
		<@outputCol  BEANMAP=dialTot/>
		<@outputCol  BEANMAP=noCVCFistula />
		<@outputPercentCol  BEANMAP=noCVCFistula  BEANMAP2=dialTot/>

	</tr>
	<tr>
		<th>
			# of patients with a graft with no CVC
		</th>
		<@outputCol  BEANMAP=dialTot/>
		<@outputCol  BEANMAP=noCVCGraft />
		<@outputPercentCol  BEANMAP=noCVCGraft  BEANMAP2=dialTot/>

	</tr>
	<tr>
		<th>
			# of All patients with CVC (regardless of additional access)
		</th>
		<@outputCol  BEANMAP=dialTot/>
		<@outputCol  BEANMAP=cvcUse />
		<@outputPercentCol  BEANMAP=cvcUse  BEANMAP2=dialTot/>

	</tr>
	<tr>
		<th>
			# of patients with CVC only
		</th>
		<@outputCol  BEANMAP=dialTot/>
		<@outputCol  BEANMAP=cvcOnly />
		<@outputPercentCol  BEANMAP=cvcOnly  BEANMAP2=dialTot/>

	</tr>

	<tr>
		<th>
			# of patients with patients with a CVC in place
		</th>
		<@outputCol  BEANMAP=dialTot />
		<@outputCol  BEANMAP=cvcPlace />
		<@outputPercentCol  BEANMAP=cvcPlace  BEANMAP2=dialTot/>

	</tr>
	<tr>
		<th>
			# of patients with an AVF in place
		</th>
		<@outputCol  BEANMAP=dialTot/>
		<@outputCol  BEANMAP=avfPlace />
		<@outputPercentCol  BEANMAP=avfPlace  BEANMAP2=dialTot/>

	</tr>
	<tr>
		<th>
			# of patients with an AVF in use
		</th>
		<@outputCol  BEANMAP=dialTot />
		<@outputCol  BEANMAP=avfUse />
		<@outputPercentCol  BEANMAP=avfUse  BEANMAP2=dialTot/>

	</tr>
</table>

<br/><br/>

<div align="left">
	<table class="report" width="63%" style="border-left-width: 0px; border-right-width: 0px;">
		<tr>
			<th width="80%">Trend</th>
			<th width="20%" style="text-align:right">Count</th>
		</tr>
		<@outputHeading title='Hospitalization Statistics'/>

		<#list hosplnReasonCount?keys as hospln_reasons>
			<#if hospln_reasons != '_total'>
				<tr>
					<th  style="padding-left:14px;align:right;">${hospln_reasons}</th>
					<#if hosplnReasonCount[hospln_reasons]??>
						<#assign value1= (hosplnReasonCount[hospln_reasons]!0)?string("#")>
					<#else>
						<#assign value1= "0">
					</#if>
					<td>${(value1)}</td>
				</tr>
			 </#if>
		</#list>

		<tr style="border:none;border:0px;border-collapse:collapse;"><td  style="border:none;border:0px;border-collapse:collapse;"><br/></td><td  style="border:none;border:0px;border-collapse:collapse;"><br/></td></tr>
		<@outputHeading title='Infection Statistics'/>

		<@outputHeading title='Access' sub="Y"/>
		<@outputRow title='AVF or AVG' BEANMAP=avfOrAvg />
		<@outputRow title='CVC' BEANMAP=cvc/>

		<@outputHeading title='Non Access' sub='Y'/>
		<@outputRow title='Non Access' BEANMAP=nonAccess />

		<@outputHeading title='Hepatitis' sub='Y'/>
		<@outputRow title='Count of new cases of Hepatitis B' BEANMAP=hepatitisB />
		<@outputRow title='Count of all patients with  Hepatitis B' BEANMAP=allHepatitisB />

		<@outputRow title='Count of new cases of Hepatitis C' BEANMAP=hepatitisC />

		<@outputRow title='Count of all patients with  Hepatitis C' BEANMAP=allHepatitisC />

		<@outputHeading title='MRSA' sub='Y'/>
		<@outputRow title='Count of new cases of MRSA' BEANMAP=mRSA />

		<@outputHeading title='VRE' sub='Y'/>
		<@outputRow title='Count of new cases of VRE' BEANMAP=vRE />


		<tr style="border:none;border:0px;border-collapse:collapse;"><td  style="border:none;border:0px;border-collapse:collapse;"><br/></td><td  style="border:none;border:0px;border-collapse:collapse;"><br/></td></tr>
		<@outputHeading title='Vaccination Statistics'/>

		<@outputHeading title='Influenza' sub='Y'/>
		<@outputRow title='Total number of patients who received influenza vaccine ' BEANMAP=influenza8Months />
		<@outputRow title='Total number of patients who refused' BEANMAP=influenzaRefused />
		<#if influenzaNotReceived?? && (influenzaNotReceived?keys)??>
			<#list influenzaNotReceived?keys as reasons>
				<#if reasons != '_total'>
					<tr>
						<th  style="padding-left:14px;align:right;">Total number of patients who did not receive the Influenza vaccine because of  ${reasons}</th>
						<#if influenzaNotReceived[reasons]??>
							<#assign value3= (influenzaNotReceived[reasons]!0)?string("#")>
						<#else>
							<#assign value3= "0">
						</#if>
						<td>${(value3)}</td>
					</tr>
				</#if>
			</#list>
		</#if>


		<@outputHeading title='Pneumovax' sub='Y'/>
		<@outputRow title='Total number of patients who received Pneumovax vaccine in the last 5 years' BEANMAP=pneumovax5Yrs />
		<@outputRow title='Total number of patients who refused' BEANMAP=pneumovaxRefused />
		<#if pneumovaxNotReceived?? && (pneumovaxNotReceived?keys)??>
			<#assign pneumovaxRefReasons = pneumovaxNotReceived?keys>
			<#list pneumovaxRefReasons as reasons>
					<#if reasons != '_total'>
					<tr>
						<th  style="padding-left:14px;align:right;">Total number of patients who did not receive Pneumovax vaccine due to ${reasons}</th>
						<#if pneumovaxNotReceived[reasons]??>
							<#assign value3= (pneumovaxNotReceived[reasons]!0)?string("#")>
						<#else>
							<#assign value3= "0">
						</#if>
						<td>${(value3)}</td>
					</tr>
					</#if>
			</#list>
		</#if>


		<@outputHeading title='Hepatitis B' sub='Y'/>

		<@outputRow title='Total number of patients who are in the process of receiving the INITIAL Hepatitis B vaccine series' BEANMAP=hepatitisInProcess />
		<@outputRow title='Total number of patients who received one full INTIAL Hepatitis B vaccine series' BEANMAP=hepatitisOneFull />
		<@outputRow title='Total number of patients who have refused the Hepatitis B series' BEANMAP=hepatitisRefused />
		<#if hepatitisNotReceived?? && (hepatitisNotReceived?keys)??>
		<#assign hepatitisRefReasons = hepatitisNotReceived?keys>
			<#list hepatitisRefReasons as hreasons>
					<#if hreasons != '_total'>
						<tr>
							<th  style="padding-left:14px;align:right;">Total number of patients who did not receive the Hep B vaccine because of  ${hreasons}</th>
							<#if hepatitisNotReceived[hreasons]??>
								<#assign value3= (hepatitisNotReceived[hreasons]!0)?string("#")>
							<#else>
								<#assign value3= "0">
							</#if>
							<td>${(value3)}</td>
						</tr>
					</#if>
			</#list>
		</#if>
		<tr style="border:none;border:0px;border-collapse:collapse;"><td  style="border:none;border:0px;border-collapse:collapse;"><br/></td><td  style="border:none;border:0px;border-collapse:collapse;"><br/></td></tr>
		<@outputHeading title='Mortality' sub='N'/>
		<#if mortality?? && (mortality?keys)??>
			<#assign mortalityReasons = mortality?keys>
			<#list mortalityReasons as reasons>
					<#if reasons?? && reasons != '_total'>
						<tr>
							<th  style="padding-left:14px;align:right;">${reasons}</th>
							<#if mortality[reasons]??>
								<#assign value3= (mortality[reasons]!0)?string("#")>
							<#else>
								<#assign value3= "0">
							</#if>
							<td>${(value3)}</td>
						</tr>
					</#if>
			</#list>
		<#else>
				<tr>
					<th   style="padding-left:14px;align:right;"> - </th>
					<td>0</td>
				</tr>
		</#if>

	</table>
</div>
</div>
</#escape>
</body>
</html>
