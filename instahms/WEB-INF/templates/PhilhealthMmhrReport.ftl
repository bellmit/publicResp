<#setting number_format="0">
<#assign centerQuery="select * from hospital_center_master where center_id=?">
<#assign censusQuery="SELECT occ_date, SUM(CASE WHEN nhip THEN occupied ELSE 0 END) AS census_nhip, SUM(CASE WHEN NOT nhip THEN occupied ELSE 0 END) AS census_non_nhip, SUM(CASE WHEN nhip THEN admission ELSE 0 END) AS admissions_nhip, SUM(CASE WHEN NOT nhip THEN admission ELSE 0 END) AS admissions_non_nhip, SUM(CASE WHEN nhip THEN discharge ELSE 0 END) AS discharges_nhip, SUM(CASE WHEN NOT nhip THEN discharge ELSE 0 END) AS discharges_non_nhip, SUM(CASE WHEN nhip THEN intra_day_discharge ELSE 0 END) AS intra_day_discharges_nhip, SUM(CASE WHEN NOT nhip THEN intra_day_discharge ELSE 0 END) AS intra_day_discharges_non_nhip FROM ( SELECT   ?::date + generate_series(0, ?::date - ?::date) AS occ_date, false as nhip, 0 as discharge, 0 as admission, 0 as intra_day_discharge, 0 as occupied UNION   ALL SELECT   occ_temp_table.occ_date, occ_temp_table.nhip,    CASE WHEN occ_date = discharge_date AND reg_date != discharge_date THEN 1 ELSE 0 END as discharge,    CASE WHEN reg_date = occ_date AND (discharge_date is null OR  reg_date != discharge_date) THEN 1 ELSE 0 END as admission,    CASE WHEN discharge_date is not null AND reg_date = discharge_date THEN 1 ELSE 0 END as intra_day_discharge,    CASE WHEN discharge_date is null OR (discharge_date IS NOT NULL AND occ_date != discharge_date AND reg_date != discharge_date) THEN 1 ELSE 0 END as occupied  FROM (   SELECT      pr.patient_id,      pr.reg_date,      pr.discharge_date,     pr.start_date,     pr.end_date,      pip.sponsor_id is not null AND pip.sponsor_id = ? as nhip,      pr.start_date::date + generate_series(0, end_date::date - start_date::date) AS occ_date    FROM (      SELECT         patient_id,        reg_date,        (CASE WHEN reg_date <= ? THEN ? ELSE reg_date END) AS start_date,        (CASE WHEN (discharge_date is NULL OR discharge_date >= ?) THEN ? ELSE discharge_date END) AS end_date,        discharge_date,        discharge_type_id,        center_id       FROM         patient_registration       WHERE         visit_type='i'         AND center_id = ?         AND (reg_date <= ? OR (reg_date BETWEEN ? AND ?))        AND (discharge_date is null OR discharge_date >= ? OR (discharge_date BETWEEN ? AND ?))        AND (discharge_type_id IS NULL OR discharge_type_id != 6)       ORDER BY         patient_id    ) pr    JOIN hospital_center_master hcm ON hcm.center_id = pr.center_id   LEFT JOIN patient_insurance_plans pip ON pip.patient_id = pr.patient_id AND pip.sponsor_id = ?  ) AS occ_temp_table) AS occ_final group by occ_date order by occ_date;">

<#assign newBornQuery="select COALESCE(SUM(CASE WHEN pip.sponsor_id is not null AND pip.sponsor_id = ? THEN 1 ELSE 0 END),0) nhip, COALESCE(SUM(CASE WHEN pip.sponsor_id is not null AND pip.sponsor_id = ? THEN 0 ELSE 1 END),0) non_nhip, COALESCE(SUM(CASE WHEN pip.sponsor_id is not null AND pip.sponsor_id = ? THEN (CASE WHEN pd.death_date is null THEN 1 ELSE 0 END) ELSE 0 END),0) well_nhip, COALESCE(SUM(CASE WHEN pip.sponsor_id is not null AND pip.sponsor_id = ? THEN 0 ELSE (CASE WHEN pd.death_date is null THEN 1 ELSE 0 END) END),0) well_non_nhip, COALESCE(SUM(CASE WHEN pip.sponsor_id is not null AND pip.sponsor_id = ? THEN (CASE WHEN pd.delivery_type = 'C' THEN 1 ELSE 0 END) ELSE 0 END),0) caesarean_nhip, COALESCE(SUM(CASE WHEN pip.sponsor_id is not null AND pip.sponsor_id = ? THEN 0 ELSE (CASE WHEN pd.delivery_type = 'C' THEN 1 ELSE 0 END) END),0) caesarean_non_nhip from admission ad join patient_registration pr on pr.patient_id = ad.parent_id and pr.visit_type='i' AND center_id = ? join patient_details pd on pd.mr_no = ad.mr_no LEFT JOIN patient_insurance_plans pip on pip.patient_id = pr.patient_id and pip.sponsor_id = ? where ad.isbaby = 'Y' AND admit_date between ? AND ?;">

<#assign mortalityQuery="select dm.reason, COALESCE(SUM(CASE WHEN pip.sponsor_id is not null AND pip.sponsor_id = ? THEN 1 ELSE 0 END),0) nhip, COALESCE(SUM(CASE WHEN pip.sponsor_id is not null AND pip.sponsor_id = ? THEN 0 ELSE 1 END),0) non_nhip from patient_details pd join patient_registration pr on pr.mr_no = pd.mr_no and pr.discharge_date >= pd.death_date AND pr.center_id = ?  LEFT JOIN patient_insurance_plans pip on pip.patient_id = pr.patient_id and pip.sponsor_id = ?  join death_reason_master dm on dm.reason_id = pd.death_reason_id WHERE pd.death_date between ? AND ? group by dm.reason;">

<#assign commonCauseConfinementQuery="select md.code_type, md.icd_code, md.description, count(id) as allCount, COALESCE(SUM(CASE WHEN pip.sponsor_id is not null AND pip.sponsor_id = ? THEN 1 ELSE 0 END),0) nhip, COALESCE(SUM(CASE WHEN pip.sponsor_id is not null AND pip.sponsor_id = ? THEN 0 ELSE 1 END),0) non_nhip from mrd_diagnosis md join patient_registration pr on pr.patient_id = md.visit_id LEFT JOIN patient_insurance_plans pip on pip.patient_id = pr.patient_id and pip.sponsor_id = ? where pr.visit_type='i' and (pr.discharge_date is null or pr.discharge_date between ? and ?) AND pr.center_id = ?  group by md.code_type, md.icd_code, md.description order by count(id) desc limit 10;">

<#assign commonSurgicalProcedureQuery="select om.op_id, om.operation_name, count(op_id), COALESCE(SUM(CASE WHEN pip.sponsor_id is not null AND pip.sponsor_id = ? THEN 1 ELSE 0 END),0) nhip, COALESCE(SUM(CASE WHEN pip.sponsor_id is not null AND pip.sponsor_id = ? THEN 0 ELSE 1 END),0) non_nhip from operation_procedures op JOIN operation_details od ON od.operation_details_id=op.operation_details_id JOIN operation_master om on om.op_id=op.operation_id JOIN patient_registration pr on pr.patient_id = od.patient_id LEFT JOIN patient_insurance_plans pip on pip.patient_id = pr.patient_id and pip.sponsor_id = ? where pr.visit_type='i' AND od.operation_status='C' and od.surgery_end between ? and ? AND pr.center_id = ? group by om.op_id, om.operation_name order by count(op_id) desc limit 10;">

<#assign surgicalSterlizationQuery="select COALESCE(SUM(CASE WHEN pip.sponsor_id is not null AND pip.sponsor_id = ? THEN (CASE WHEN (crm1.code = '55250' AND crm1.code_type='RVS') OR (crm2.code = '55250' AND crm2.code_type='RVS') THEN 1 ELSE 0 END) ELSE 0 END),0) vasectomy_nhip, COALESCE(SUM(CASE WHEN pip.sponsor_id is not null AND pip.sponsor_id = ? THEN 0 ELSE (CASE WHEN (crm1.code = '55250' AND crm1.code_type='RVS') OR (crm2.code = '55250' AND crm2.code_type='RVS') THEN 1 ELSE 0 END) END),0) vasectomy_non_nhip, COALESCE(SUM(CASE WHEN pip.sponsor_id is not null AND pip.sponsor_id = ? THEN (CASE WHEN (crm1.code in ('58600','58605') AND crm1.code_type='RVS') OR (crm2.code in ('58600','58605') AND crm2.code_type='RVS') THEN 1 ELSE 0 END) ELSE 0 END),0) btl_nhip, COALESCE(SUM(CASE WHEN pip.sponsor_id is not null AND pip.sponsor_id = ? THEN 0 ELSE (CASE WHEN (crm1.code in ('58600','58605') AND crm1.code_type='RVS') OR (crm2.code in ('58600','58605') AND crm2.code_type='RVS') THEN 1 ELSE 0 END) END),0) btl_non_nhip from patient_registration pr LEFT JOIN (SELECT * FROM case_rate_main WHERE code_type='RVS' AND code in ('55250','58600','58605')) crm1 ON crm1.case_rate_id=pr.primary_case_rate_id LEFT JOIN (SELECT * FROM case_rate_main WHERE code_type='RVS' AND code in ('55250','58600','58605')) crm2 ON crm2.case_rate_id=pr.secondary_case_rate_id LEFT JOIN patient_insurance_plans pip on pip.patient_id = pr.patient_id and pip.sponsor_id = ? where pr.visit_type='i' AND pr.discharge_date between ? and ? AND pr.center_id = ? AND (crm1.case_rate_id is NOT NULL OR crm2.case_rate_id is NOT NULL);">

<#assign caesarianIndicationQuery="select cdim.indication, COALESCE(SUM(CASE WHEN pip.sponsor_id is not null AND pip.sponsor_id = ? THEN 1 ELSE 0 END),0) nhip, COALESCE(SUM(CASE WHEN pip.sponsor_id is not null AND pip.sponsor_id = ? THEN 0 ELSE 1 END),0) non_nhip from admission ad join patient_registration pr on pr.patient_id = ad.parent_id and pr.visit_type='i' AND pr.center_id = ? join patient_details pd on pd.mr_no = ad.mr_no AND pd.delivery_type = 'C' LEFT JOIN indication_for_caesarean_section cdim ON cdim.id = pd.caesarean_indication_id LEFT JOIN patient_insurance_plans pip on pip.patient_id = pr.patient_id and pip.sponsor_id = ? where ad.isbaby = 'Y' AND admit_date between ? AND ? AND pr.center_id = ? group by cdim.indication ORDER BY count(cdim.indication) desc limit 5;">

<#assign ouboundReferralReasonQuery="select orrm.reason, COALESCE(SUM(CASE WHEN pip.sponsor_id is not null AND pip.sponsor_id = ? THEN 1 ELSE 0 END),0) nhip, COALESCE(SUM(CASE WHEN pip.sponsor_id is not null AND pip.sponsor_id = ? THEN 0 ELSE 1 END),0) non_nhip FROM patient_registration pr join patient_details pd on pd.mr_no = pr.mr_no LEFT JOIN reason_for_referral orrm ON orrm.id = pr.reason_for_referral_id LEFT JOIN patient_insurance_plans pip on pip.patient_id = pr.patient_id and pip.sponsor_id = ? where pr.visit_type='i' AND discharge_type_id = 5 AND pr.center_id = ? AND pr.discharge_date between ? AND ? group by orrm.reason ORDER BY count(orrm.reason) desc limit 5;">

<#assign additionalMortalityCasesPerPage = 55>
<#assign centerMeta = queryToDynaBean(centerQuery, loggedInCenterId)>
<#assign centerReportingMeta = centerMeta.reporting_meta?eval>
<#assign tpaId = centerReportingMeta.philhealth_tpaid_ohsrsdohgovph>
<#assign bedReportRows = queryToDynaList(censusQuery, fromDate, toDate, fromDate, tpaId, fromDate, fromDate, toDate, toDate, loggedInCenterId, fromDate, fromDate, toDate, toDate, fromDate, toDate, tpaId)>
<#assign top10Diagnosis = queryToDynaList(commonCauseConfinementQuery, tpaId, tpaId, tpaId, fromDate, toDate, loggedInCenterId)>
<#assign newBornData = queryToDynaBean(newBornQuery, tpaId, tpaId, tpaId, tpaId, tpaId, tpaId, loggedInCenterId, tpaId, fromDate, toDate)>
<#assign mortalityCases = queryToDynaList(mortalityQuery, tpaId, tpaId, loggedInCenterId, tpaId, fromDate, toDate)>
<#assign mortalityCount = mortalityCases?size>
<#assign top10Surgeries = queryToDynaList(commonSurgicalProcedureQuery, tpaId, tpaId, tpaId, fromDate, toDate, loggedInCenterId)>
<#assign surgicalSterlizationData = queryToDynaBean(surgicalSterlizationQuery, tpaId, tpaId, tpaId, tpaId, tpaId, fromDate, toDate, loggedInCenterId)>
<#assign caesarianIndications = queryToDynaList(caesarianIndicationQuery, tpaId, tpaId, loggedInCenterId, tpaId, fromDate, toDate, loggedInCenterId)>
<#assign top5Referrals = queryToDynaList(ouboundReferralReasonQuery, tpaId, tpaId, tpaId, loggedInCenterId, fromDate, toDate)>
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <title>Philhealth Monthly Mandatory Hospital Report</title>

        <style type="text/css">
            @page {
                size: A4;
                margin: 15pt 15pt 15pt 15pt;
            }
            
            body {
                font-family: "Arial Unicode MS", sans-serif;
                font-size: 7pt;
            }
            
            table.report {
                empty-cells: show;
            }
            
            table.report {
                border-collapse: collapse;
                border: 1px solid black;
            }
            
            table.report th {
                border: 1px solid black;
                padding: 2px 3px 2px 3px;
            }
            
            table.report td {
                padding: 1px 2px 1px 2px;
                border: 1px solid black;
            }
            
            table.report td.number {
                text-align: right;
            }
            
            table.report td.totnumber {
                text-align: right;
            }
            
            p.noresult {
                font-weight: bold;
            }
            
            p.heading {
                font-size: 9pt;
                font-weight: bold;
            }
        </style>

    </head>

    <body>
        <p style="font-size: 7pt; text-align: center; font-weight:bold;line-height:1.25; padding:0; margin:0; ">Republic of the Phillipines</p>
        <p style="font-size: 8pt; text-align: center; font-weight:bold;line-height:1.25; padding:0; margin:0; ">PHILIPPINE HEALTH INSURANCE CORPORATION</p>
        <p style="font-size: 8pt; text-align: center;line-height:1.25; padding:0; margin:0;">12/F City State Centre, 709 Shaw Blvd., Brgy. Oranbo, Pasig City</p>
        <p style="font-size: 8pt; text-align: center; font-weight:bold;line-height:1.25; padding:0; margin:0; ">MANDATORY MONTHLY HOSPITAL REPORT</p>
        <p style="font-size: 8pt; text-align: center; font-weight:bold;line-height:1.25; padding:0; margin:0; ">For the Month of ${fromDate?string["MMMM, yyyy"]}</p>
        <table align="center" width="100%">
            <tbody>
                <tr>
                    <td width="14%">Accreditation No. :</td>
                    <td width="53%" style="border-bottom: 0.5pt solid #000000;">${centerReportingMeta.accredition_no}</td>
                    <td width="1%"></td>
                    <td width="17%">Region :</td>
                    <td width="14%" style="border-bottom: 0.5pt solid #000000;">${centerReportingMeta.region}</td>
                </tr>
                <tr>
                    <td>Name of Hospital :</td>
                    <td style="border-bottom: 0.5pt solid #000000;">${centerReportingMeta.hospital_name}</td>
                    <td></td>
                    <td>Category :</td>
                    <td style="border-bottom: 0.5pt solid #000000;">${centerReportingMeta.category}</td>
                </tr>
                <tr>
                    <td>Address No./Street :</td>
                    <td style="border-bottom: 0.5pt solid #000000;">${centerReportingMeta.address_street}</td>
                    <td></td>
                    <td>PHIC Accredited beds :</td>
                    <td style="border-bottom: 0.5pt solid #000000;">${centerReportingMeta.phic_bed_count}</td>
                </tr>
                <tr>
                    <td>Municipality :</td>
                    <td style="border-bottom: 0.5pt solid #000000;">${centerReportingMeta.address_municipality}</td>
                    <td></td>
                    <td>DOH Authorized beds :</td>
                    <td style="border-bottom: 0.5pt solid #000000;">${centerReportingMeta.doh_bed_count}</td>
                </tr>
                <tr>
                    <td>Province :</td>
                    <td style="border-bottom: 0.5pt solid #000000;">${centerReportingMeta.address_province}</td>
                    <td colspan="3"></td>
                </tr>
                <tr>
                    <td>Zip Code :</td>
                    <td style="border-bottom: 0.5pt solid #000000;">${centerReportingMeta.address_zip}</td>
                    <td colspan="3"></td>
                </tr>
            </tbody>
        </table>
        <table style="border-collapse:collapse;" width="100%">
            <tbody>
                <tr>
                    <td style="border:0;font-weight:bold;padding-top:6pt;" colspan="4"><span style="color:#F00000;">A.1.</span> DAILY CENSUS OF NHIP PATIENTS (EVERY 12:00 MN.)</td>
                    <td style="border:0;font-weight:bold;font-size:6pt;padding-top:6pt;" colspan="5">CENSUS FOR THE DAY = (CENSUS OF THE PREVIOUS DAY plus ADMISSIONS OF THE DAY</td>
                </tr>
                <tr>
                    <td style="border:0;" colspan="6"></td>
                    <td style="border:0;font-weight:bold;font-size:6pt;" colspan="3">minus DISCHARGES OF THE DAY)</td>
                </tr>
                <tr>
                    <td style="border:1pt solid #000000; color: #F00000;text-decoration:underline; text-align: center;">1</td>
                    <td colspan="3" style="border:1pt solid #000000; color: #F00000;text-decoration:underline; text-align: center;">2</td>
                    <td style="border:0;border-top:0 !important;"></td>
                    <td style="border:1pt solid #000000; color: #F00000;text-decoration:underline; text-align: center;">3</td>
                    <td colspan="3" style="border:1pt solid #000000; color: #F00000;text-decoration:underline; text-align: center;">4</td>
                </tr>
                <tr>
                    <td rowspan="2" style="border:1pt solid #000000; color: #000000; text-align: center; font-weight: bold;">DATE</td>
                    <td colspan="3" style="border:1pt solid #000000; color: #000000; text-align: center; font-weight: bold;">CENSUS</td>
                    <td style="border:0;"></td>
                    <td rowspan="2" style="border:1pt solid #000000; color: #000000; text-align: center; font-weight: bold;">DATE</td>
                    <td colspan="3" style="border:1pt solid #000000; color: #000000; text-align: center; font-weight: bold;">DISCHARGES</td>
                </tr>
                <tr>
                    <td style="border:1pt solid #000000; color: #000000; text-align: center; font-weight: bold;"><span style="color:#F00000;">a.</span> NHIP</td>
                    <td style="border:1pt solid #000000; color: #000000; text-align: center; font-weight: bold;"><span style="color:#F00000;">b.</span> NON-NHIP</td>
                    <td style="border:1pt solid #000000; color: #000000; text-align: center; font-weight: bold;"><span style="color:#F00000;">c.</span> TOTAL</td>
                    <td style="border:0;"></td>
                    <td style="border:1pt solid #000000; color: #000000; text-align: center; font-weight: bold;"><span style="color:#F00000;">a.</span> NHIP</td>
                    <td style="border:1pt solid #000000; color: #000000; text-align: center; font-weight: bold;"><span style="color:#F00000;">b.</span> NON-NHIP</td>
                    <td style="border:1pt solid #000000; color: #000000; text-align: center; font-weight: bold;"><span style="color:#F00000;">c.</span> TOTAL</td>
                </tr>
                <#assign total_census_nhip = 0>
                <#assign total_census_non_nhip = 0>
                <#assign total_discharges_nhip = 0>
                <#assign total_discharges_non_nhip = 0>
                <#list bedReportRows as dataRow>
                <tr>
                    <td width="8%" style="border:1pt solid #000000; color: #000000; text-align: right;">${dataRow.occ_date?string["d"]}</td>
                    <td width="12%" style="border:1pt solid #000000; color: #000000; text-align: right;">${dataRow.census_nhip}</td>
                    <td width="12%" style="border:1pt solid #000000; color: #000000; text-align: right;">${dataRow.census_non_nhip}</td>
                    <td width="15%" style="border:1pt solid #000000; color: #000000; text-align: right;">${(dataRow.census_non_nhip + dataRow.census_nhip)}</td>
                    <td width="6%"  style="border:0;"></td>
                    <td width="8%" style="border:1pt solid #000000; color: #000000; text-align: right;">${dataRow.occ_date?string["d"]}</td>
                    <td width="12%" style="border:1pt solid #000000; color: #000000; text-align: right;">${dataRow.discharges_nhip}</td>
                    <td width="12%" style="border:1pt solid #000000; color: #000000; text-align: right;">${dataRow.discharges_non_nhip}</td>
                    <td width="15%" style="border:1pt solid #000000; color: #000000; text-align: right;">${(dataRow.discharges_non_nhip + dataRow.discharges_nhip)}</td>
                </tr>            
                <#assign total_census_nhip = total_census_nhip + dataRow.census_nhip>
                <#assign total_census_non_nhip = total_census_non_nhip + dataRow.census_non_nhip>
                <#assign total_discharges_nhip = total_discharges_nhip + dataRow.discharges_nhip>
                <#assign total_discharges_non_nhip = total_discharges_non_nhip + dataRow.discharges_non_nhip>
                </#list>
                <tr>
                    <td style="border:1pt solid #000000; color: #000000; font-weight: bold; text-align: center;">TOTAL</td>
                    <td style="border:1pt solid #000000; color: #000000; font-weight: bold; text-align: right;">${total_census_nhip}</td>
                    <td style="border:1pt solid #000000; color: #000000; font-weight: bold; text-align: right;">${total_census_non_nhip}</td>
                    <td style="border:1pt solid #000000; color: #000000; font-weight: bold; text-align: right;">${(total_census_non_nhip + total_census_nhip)}</td>
                    <td  style="border:0;"></td>
                    <td style="border:1pt solid #000000; color: #000000; font-weight: bold; text-align: center;">TOTAL</td>
                    <td style="border:1pt solid #000000; color: #000000; font-weight: bold; text-align: right;">${total_discharges_nhip}</td>
                    <td style="border:1pt solid #000000; color: #000000; font-weight: bold; text-align: right;">${total_discharges_non_nhip}</td>
                    <td style="border:1pt solid #000000; color: #000000; font-weight: bold; text-align: right;">${(total_discharges_non_nhip + total_discharges_nhip)}</td>
                </tr>            
            </tbody>
        </table>
        <table style="padding-top:6pt;">
            <tr>
                <td style="border:0;font-weight:bold;" colspan="3"><span style="color:#F00000;">B.</span> QUALITY ASSURANCE INDICATOR</td>
            </tr>
            <tr>
                <td style="border:0;font-weight:bold;">1. Monthly Bed Occupancy Rate ( MBOR ) = <span style="text-decoration:underline"><#if bedReportRows?size == 0>0<#else>${((total_census_non_nhip + total_census_nhip)*100/(bedReportRows?size * centerReportingMeta.doh_bed_count))?string["0.00"]}</#if></span></td>
                <td style="border:0;font-weight:bold;"></td>
                <td style="border:0;font-weight:bold;">3. Average Length of Stay per NHIP Patient (ALSP) = <span style="text-decoration:underline"><#if total_discharges_nhip == 0>0<#else>${(total_census_nhip/total_discharges_nhip)?string["0.00"]}</#if></span></td>
            </tr>
            <tr>
                <td style="border:0;" width="53%">
                    <p style="font-size:6pt;line-height:1;padding:0 0 0 60pt;margin:0">Total of NHIP CENSUS plus Total of NON-NHIP CENSUS</p>
                    <p style="font-size:7pt;line-height:1;padding:0 0 0 10pt;margin:0"><span style="font-weight:bold;">MBOR =</span> ----------------------------------------------------------------------------------------- X 100</p>
                    <p style="font-size:5.5pt;line-height:1;padding:0 0 0 40pt;margin:0">Number of Days per Month Indicated multiplied by Number of DOH Authorized Beds</p>
                </td>
                <td style="border:0;font-weight:bold;" width="5%"></td>
                <td style="border:0;" width="42%">
                    <p style="font-size:6pt;line-height:1;padding:0 0 0 40pt;margin:0">Total of NHIP CENSUS</p>
                    <p style="font-size:7pt;line-height:1;padding:0 0 0 10pt;margin:0"><span style="font-weight:bold;">ALSP = </span>------------------------------</p>
                    <p style="font-size:6pt;line-height:1;padding:0 0 0 38pt;margin:0">Total NHIP DISCHARGES</p>
                </td>
            </tr>
            <tr>
                <td style="border:0;font-weight:bold;padding-top:6pt;">2. Monthly NHIP Beneficiary Occupancy Rate ( MNHIBOR ) = <span style="text-decoration:underline"><#if bedReportRows?size == 0>0<#else>${((total_census_nhip)*100/(bedReportRows?size * centerReportingMeta.phic_bed_count))?string["0.00"]}</#if></span></td>
                <td style="border:0;font-weight:bold;"></td>
                <td style="border:0;font-weight:bold;"></td>
            </tr>
            <tr>
                <td style="border:0;" width="53%">
                    <p style="font-size:6pt;line-height:1;padding:0 0 0 120pt;margin:0">Total of NHIP CENSUS</p>
                    <p style="font-size:7pt;line-height:1;padding:0 0 0 10pt;margin:0"><span style="font-weight:bold;">MNHIBOR =</span> ----------------------------------------------------------------------------------------- X 100</p>
                    <p style="font-size:5.5pt;line-height:1;padding:0 0 0 52pt;margin:0">Number of Days per Month Indicated multiplied by Number of PHIC Accredited Beds</p>
                </td>
                <td style="border:0;font-weight:bold;" width="5%"></td>
                <td style="border:0;" width="42%"></td>
            </tr>
        </table>
        <table style="border-collapse:collapse;" width="100%">
            <tbody>
                <tr>
                    <td style="border:0;font-weight:bold;padding-top:6pt;" colspan="5"><span style="color:#F00000;">C.</span> NEWBORN CENSUS</td>
                </tr>
                <tr>
                    <td style="border:0;font-size:6pt;">(Well Babies Only)</td>
                    <td style="border:1pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;" colspan="3">PARENT</td>
                    <td style="border:0;border-top:0 !important;"></td>
                </tr>
                <tr>
                    <td style="border:0;font-size:6pt;" width="20%"></td>
                    <td style="border:1pt solid #000000; color: #000000; text-align: center; font-weight: bold;" width="10%">NHIP</td>
                    <td style="border:1pt solid #000000; color: #000000; text-align: center; font-weight: bold;" width="10%">NON-NHIP</td>
                    <td style="border:1pt solid #000000; color: #000000; text-align: center; font-weight: bold;" width="12%">TOTAL</td>
                    <td style="border:0;border-top:0 !important;" width="48%"></td>
                </tr>
                <tr>
                    <td style="border:1pt solid #000000; color: #000000; text-align: center; font-weight: bold;">TOTAL # OF NEWBORN</td>
                    <td style="border:1pt solid #000000; color: #000000; text-align: right;">${newBornData.well_nhip}</td>
                    <td style="border:1pt solid #000000; color: #000000; text-align: right;">${newBornData.well_non_nhip}</td>
                    <td style="border:1pt solid #000000; color: #000000; text-align: right;">${(newBornData.well_nhip + newBornData.well_non_nhip)}</td>
                    <td style="border:0;border-top:0 !important;"></td>
                </tr>
            </tbody>    
        </table>
        <p style="padding: 25pt 0 0 0; margin:0; font-size:7pt;">DATE OF RECEIPT : PRO/SO ____________________ RECORDS SECTION _______________________ ACCREDITATION ______________________</p>
        <p style="color:#F00000; font-weight: bold; font-size:7pt;">* Note : This is a mandatory hospital report to be submitted <span style="text-decoration: underline;">within the first ten (10) days</span> of he following month.</p>
        <p style="page-break-before: always;"></p>
        <table style="border-collapse:collapse;" width="100%">
            <tbody>
                <tr>
                    <td style="border:0;font-weight:bold;padding-top:8pt;" colspan="3"><span style="color:#F00000;">D.</span> MOST COMMON CAUSES OF CONFINEMENT</td>
                </tr>
                <tr>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;" rowspan="2">DIAGNOSIS</td>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;" colspan="2">TOTAL</td>
                </tr>
                <tr>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;">NHIP</td>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;">NON-NHIP</td>
                </tr>
                <#list top10Diagnosis as dataRow>
                <tr>
                    <td width="76%" style="border:0.5pt solid #000000; color: #000000; text-align: left;">${dataRow.code_type} / ${dataRow.icd_code} - ${dataRow.description}</td>
                    <td width="12%" style="border:0.5pt solid #000000; color: #000000; text-align: right;">${dataRow.nhip}</td>
                    <td width="12%" style="border:0.5pt solid #000000; color: #000000; text-align: right;">${dataRow.non_nhip}</td>
                </tr>            
                <#else>
                <tr>
                    <td width="76%" style="border:0.5pt solid #000000; color: #000000; text-align: left;"> -- </td>
                    <td width="12%" style="border:0.5pt solid #000000; color: #000000; text-align: right;">0</td>
                    <td width="12%" style="border:0.5pt solid #000000; color: #000000; text-align: right;">0</td>
                </tr>
                </#list>
                <tr>
                    <td style="border:0;font-weight:bold;padding-top:8pt;" colspan="3"><span style="color:#F00000;">E.</span> SURGICAL OUTPUT - Top 10 Procedures</td>
                </tr>
                <tr>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;" rowspan="2">SURGICAL PROCEDURES</td>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;" colspan="2">TOTAL</td>
                </tr>
                <tr>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;">NHIP</td>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;">NON-NHIP</td>
                </tr>
                <#list top10Surgeries as dataRow>
                <tr>
                    <td width="76%" style="border:0.5pt solid #000000; color: #000000; text-align: left;">${dataRow.operation_name}</td>
                    <td width="12%" style="border:0.5pt solid #000000; color: #000000; text-align: right;">${dataRow.nhip}</td>
                    <td width="12%" style="border:0.5pt solid #000000; color: #000000; text-align: right;">${dataRow.non_nhip}</td>
                </tr>            
                <#else>
                <tr>
                    <td width="76%" style="border:0.5pt solid #000000; color: #000000; text-align: left;"> NONE </td>
                    <td width="12%" style="border:0.5pt solid #000000; color: #000000; text-align: right;">0</td>
                    <td width="12%" style="border:0.5pt solid #000000; color: #000000; text-align: right;">0</td>
                </tr>
                </#list>
                <tr>
                    <td style="border:0;font-weight:bold;padding-top:8pt;" colspan="3"><span style="color:#F00000;">E.1.</span>TOTAL SURGICAL STERILIZATION</td>
                </tr>
                <tr>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;" rowspan="2">SURGICAL STERILIZATION PROCEDURE</td>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;" colspan="2">NO. OF PATIENTS</td>
                </tr>
                <tr>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;">NHIP</td>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;">NON-NHIP</td>
                </tr>
                <tr>
                    <td width="76%" style="border:0.5pt solid #000000; color: #000000; text-align: left;">1. BILATERAL TUBAL LIGATION</td>
                    <td width="12%" style="border:0.5pt solid #000000; color: #000000; text-align: right;">${surgicalSterlizationData.btl_nhip}</td>
                    <td width="12%" style="border:0.5pt solid #000000; color: #000000; text-align: right;">${surgicalSterlizationData.btl_non_nhip}</td>
                </tr>            
                <tr>
                    <td width="76%" style="border:0.5pt solid #000000; color: #000000; text-align: left;">2. VASECTOMY</td>
                    <td width="12%" style="border:0.5pt solid #000000; color: #000000; text-align: right;">${surgicalSterlizationData.vasectomy_nhip}</td>
                    <td width="12%" style="border:0.5pt solid #000000; color: #000000; text-align: right;">${surgicalSterlizationData.vasectomy_non_nhip}</td>
                </tr>            
                <tr>
                    <td width="76%" style="border:0.5pt solid #000000;color: #000000;font-weight:bold;text-align: left;">TOTAL</td>
                    <td width="12%" style="border:0.5pt solid #000000;color: #000000;font-weight:bold;text-align: right;">${(surgicalSterlizationData.btl_nhip + surgicalSterlizationData.vasectomy_nhip)}</td>
                    <td width="12%" style="border:0.5pt solid #000000;color: #000000;font-weight:bold;text-align: right;">${(surgicalSterlizationData.btl_non_nhip + surgicalSterlizationData.vasectomy_non_nhip)}</td>
                </tr>            
                <tr>
                    <td style="border:0;font-weight:bold;padding-top:8pt;" colspan="3"><span style="color:#F00000;">F.</span>OBSTETRICAL PROCEDURES</td>
                </tr>
                <tr>
                    <td style="border:0;"></td>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;">NHIP</td>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;">NON-NHIP</td>
                </tr>
                <tr>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: left;"><span style="color:#F00000;">F.1.</span> TOTAL NUMBER OF DELIVERIES (NSD plus CAESAREAN SECTION)</td>
                    <td style="border:0.5pt solid #000000;font-size:7pt;text-align: right;">${newBornData.nhip}</td>
                    <td style="border:0.5pt solid #000000;font-size:7pt;text-align: right;">${newBornData.non_nhip}</td>
                </tr>
                <tr>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: left;"><span style="color:#F00000;">F.2.</span> TOTAL NUMBER OF CAESAREAN CASES</td>
                    <td style="border:0.5pt solid #000000;font-size:7pt;text-align: right;">${newBornData.caesarean_nhip}</td>
                    <td style="border:0.5pt solid #000000;font-size:7pt;text-align: right;">${newBornData.caesarean_non_nhip}</td>
                </tr>
                <tr>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;">INDICATIONS FOR CS:</td>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;">NHIP</td>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;">NON-NHIP</td>
                </tr>
                <#list caesarianIndications as dataRow>
                <tr>
                    <td style="border:0.5pt solid #000000;font-size:7pt;text-align: left;">${(dataRow.indication)!"UNSPECIFIED"}</td>
                    <td style="border:0.5pt solid #000000;font-size:7pt;text-align: right;">${dataRow.nhip}</td>
                    <td style="border:0.5pt solid #000000;font-size:7pt;text-align: right;">${dataRow.non_nhip}</td>
                </tr>
                <#else>
                <tr>
                    <td width="76%" style="border:0.5pt solid #000000; color: #000000; text-align: left;"> NONE </td>
                    <td style="border:0.5pt solid #000000;font-size:7pt;text-align: right;">${newBornData.caesarean_nhip}</td>
                    <td style="border:0.5pt solid #000000;font-size:7pt;text-align: right;">${newBornData.caesarean_non_nhip}</td>
                </tr>
                </#list>
                <tr>
                    <td style="border:0;font-weight:bold;padding-top:8pt;" colspan="3"><span style="color:#F00000;">G.</span> MONTHLY MORTALITY CENSUS (All Cases)</td>
                </tr>
                <tr>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;" rowspan="2">DIAGNOSIS</td>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;" colspan="2">TOTAL</td>
                </tr>
                <tr>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;">NHIP</td>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;">NON-NHIP</td>
                </tr>
                <#list mortalityCases as dataRow>
                <tr>
                    <td width="76%" style="border:0.5pt solid #000000; color: #000000; text-align: left;">${(dataRow.reason)!"UNSPECIFIED"}</td>
                    <td width="12%" style="border:0.5pt solid #000000; color: #000000; text-align: right;">${dataRow.nhip}</td>
                    <td width="12%" style="border:0.5pt solid #000000; color: #000000; text-align: right;">${dataRow.non_nhip}</td>
                </tr>
                <#if dataRow?index == 4>
                    <#break>
                </#if>
                <#else>
                <tr>
                    <td width="76%" style="border:0.5pt solid #000000; color: #000000; text-align: left;"> NONE </td>
                    <td width="12%" style="border:0.5pt solid #000000; color: #000000; text-align: right;">0</td>
                    <td width="12%" style="border:0.5pt solid #000000; color: #000000; text-align: right;">0</td>
                </tr>
                </#list>
                <tr>
                    <td style="border:0;font-weight:bold;padding-top:8pt;" colspan="3"><span style="color:#F00000;">H.</span> REFERRALS</td>
                </tr>
                <tr>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;" rowspan="2">MOST COMMON REASONS FOR REFERRAL</td>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;" colspan="2">NO. OF PATIENT REFERRED</td>
                </tr>
                <tr>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;">NHIP</td>
                    <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;">NON-NHIP</td>
                </tr>
                <#list top5Referrals as dataRow>
                <tr>
                    <td width="76%" style="border:0.5pt solid #000000; color: #000000; text-align: left;">${(dataRow.reason)!"UNSPECIFIED"}</td>
                    <td width="12%" style="border:0.5pt solid #000000; color: #000000; text-align: right;">${dataRow.nhip}</td>
                    <td width="12%" style="border:0.5pt solid #000000; color: #000000; text-align: right;">${dataRow.non_nhip}</td>
                </tr>            
                <#else>
                <tr>
                    <td width="76%" style="border:0.5pt solid #000000; color: #000000; text-align: left;"> NONE </td>
                    <td width="12%" style="border:0.5pt solid #000000; color: #000000; text-align: right;">0</td>
                    <td width="12%" style="border:0.5pt solid #000000; color: #000000; text-align: right;">0</td>
                </tr>
                </#list>
            </tbody>
        </table>
        <table style="border-collapse:collapse;" width="100%">
            <tbody>
                <tr>
                    <td width="45%" style="padding:10pt 0 40pt 0;font-size:7pt;">PREPARED BY:</td>
                    <td width="10%"></td>
                    <td width="45%" style="padding:10pt 0 40pt 0;font-size:7pt;">CERTIFIED CORRECT:</td>
                </tr>
                <tr>
                    <td style="border-top:0.5pt solid #000000;font-size:6pt;text-align: center;line-height:1.5;padding:0;margin:0;">Name and Position of Person filling up the form</td>
                    <td></td>
                    <td style="border-top:0.5pt solid #000000;font-size:6pt;text-align: center;line-height:1.5;padding:0;margin:0;">Chief of Hospital/Medical Director</td>
                </tr>
                <tr>
                    <td style="font-size:6pt;text-align: center;line-height:1;padding:0;margin:0;">(signature over printed name)</td>
                    <td></td>
                    <td style="font-size:6pt;text-align: center;line-height:1;padding:0;margin:0;">(signature over printed name)</td>
                </tr>
            </tbody>
        </table>
        <#assign idx = -1>
        <#if mortalityCount gt 5>
            <#list mortalityCases as dataRow>
            <#assign idx = dataRow?index - 5> 
            <#if idx gte 0>
                <#if idx % additionalMortalityCasesPerPage == 0>
                <p style="page-break-before: always;"></p>
                <table style="border-collapse:collapse;" width="100%">
                    <tbody>
                        <tr>
                            <td style="border:0;font-weight:bold;padding-top:8pt;" colspan="3"><span style="color:#F00000;">G.</span> MONTHLY MORTALITY CENSUS (All Cases) [Additional reasons sheet]</td>
                        </tr>
                        <tr>
                            <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;" rowspan="2">DIAGNOSIS</td>
                            <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;" colspan="2">TOTAL</td>
                        </tr>
                        <tr>
                            <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;">NHIP</td>
                            <td style="border:0.5pt solid #000000;font-weight:bold;font-size:7pt;text-align: center;">NON-NHIP</td>
                        </tr>
                </#if>
                        <tr>
                            <td width="76%" style="border:0.5pt solid #000000; color: #000000; text-align: left;">${dataRow.reason}</td>
                            <td width="12%" style="border:0.5pt solid #000000; color: #000000; text-align: right;">${dataRow.nhip}</td>
                            <td width="12%" style="border:0.5pt solid #000000; color: #000000; text-align: right;">${dataRow.non_nhip}</td>
                        </tr>            
                <#if idx % additionalMortalityCasesPerPage == (additionalMortalityCasesPerPage - 1)>
                    </tbody>
                </table>
                <table style="border-collapse:collapse;" width="100%">
                    <tbody>
                        <tr>
                            <td width="45%" style="padding:10pt 0 40pt 0;font-size:7pt;">PREPARED BY:</td>
                            <td width="10%"></td>
                            <td width="45%" style="padding:10pt 0 40pt 0;font-size:7pt;">CERTIFIED CORRECT:</td>
                        </tr>
                        <tr>
                            <td style="border-top:0.5pt solid #000000;font-size:6pt;text-align: center;line-height:1.5;padding:0;margin:0;">Name and Position of Person filling up the form</td>
                            <td></td>
                            <td style="border-top:0.5pt solid #000000;font-size:6pt;text-align: center;line-height:1.5;padding:0;margin:0;">Chief of Hospital/Medical Director</td>
                        </tr>
                        <tr>
                            <td style="font-size:6pt;text-align: center;line-height:1;padding:0;margin:0;">(signature over printed name)</td>
                            <td></td>
                            <td style="font-size:6pt;text-align: center;line-height:1;padding:0;margin:0;">(signature over printed name)</td>
                        </tr>
                    </tbody>
                </table>
                </#if>
            </#if>
            </#list>
        </#if>
        <#if (idx gte 0) && (idx % additionalMortalityCasesPerPage != (additionalMortalityCasesPerPage - 1))>
                    </tbody>
                </table>
                <table style="border-collapse:collapse;" width="100%">
                    <tbody>
                        <tr>
                            <td width="45%" style="padding:10pt 0 40pt 0;font-size:7pt;">PREPARED BY:</td>
                            <td width="10%"></td>
                            <td width="45%" style="padding:10pt 0 40pt 0;font-size:7pt;">CERTIFIED CORRECT:</td>
                        </tr>
                        <tr>
                            <td style="border-top:0.5pt solid #000000;font-size:6pt;text-align: center;line-height:1.5;padding:0;margin:0;">Name and Position of Person filling up the form</td>
                            <td></td>
                            <td style="border-top:0.5pt solid #000000;font-size:6pt;text-align: center;line-height:1.5;padding:0;margin:0;">Chief of Hospital/Medical Director</td>
                        </tr>
                        <tr>
                            <td style="font-size:6pt;text-align: center;line-height:1;padding:0;margin:0;">(signature over printed name)</td>
                            <td></td>
                            <td style="font-size:6pt;text-align: center;line-height:1;padding:0;margin:0;">(signature over printed name)</td>
                        </tr>
                    </tbody>
                </table>
        </#if>
    </body>

</html>
