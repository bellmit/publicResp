/**
 *
 */
package com.insta.hms.diagnosticmodule.common;

/**
 * @author hanumanth
 *
 */
public final class  DiagnosticsPostgresQueryHandler {

	/* Queries related to TEST_FORMAT(Report Template Screen) Table */
	public static final String insertReportFormat = "INSERT INTO TEST_FORMAT(TESTFORMAT_ID,FORMAT_NAME,FORMAT_DESCRIPTION,REPORT_FILE) VALUES(?,?,?,?)";
	public static final String updateReportFormat = "UPDATE TEST_FORMAT SET FORMAT_NAME=?,FORMAT_DESCRIPTION=?,REPORT_FILE=? WHERE TESTFORMAT_ID=?";
	public static final String getReportFormat = "SELECT FORMAT_NAME,FORMAT_DESCRIPTION,REPORT_FILE FROM TEST_FORMAT WHERE TESTFORMAT_ID=? FOR UPDATE";
	public static final String getAllReportFormats = "SELECT TESTFORMAT_ID,FORMAT_NAME,FORMAT_DESCRIPTION,REPORT_FILE FROM TEST_FORMAT FOR UPDATE ORDER BY FORMAT_NAME";
	public static final String getReportContent = "SELECT REPORT_FILE FROM TEST_FORMAT WHERE TESTFORMAT_ID=?";
	public static final String getAllTemplateDetailsOtherThanTemplateContent = "SELECT TESTFORMAT_ID,FORMAT_NAME,FORMAT_DESCRIPTION FROM TEST_FORMAT ORDER BY FORMAT_NAME";

	/*Queries related to TEST_Details(Diagnostics Conducting Screen) Table */
	public static final String getTestReportFormat ="SELECT PATIENT_REPORT_FILE FROM TEST_DETAILS WHERE MR_NO=? AND PATIENT_ID=? AND FORMAT_NAME=? AND PRESCRIBED_ID=?";

	/*Queries related to diagnostic master screen */
	public static final String diaglist="SELECT DDEPT_ID, DDEPT_NAME FROM DIAGNOSTICS_DEPARTMENTS WHERE STATUS='A' ORDER BY DDEPT_NAME";
	public static final String testFormatList="SELECT TESTFORMAT_ID,FORMAT_NAME from TEST_FORMAT order by FORMAT_NAME";
	public static final String specialSymbollist="SELECT * FROM SPECIAL_SYMBOLS";
	public static final String testquery="select test_id,test_name,dg.Ddept_id,dgd.DDept_Name from diagnostics dg,Diagnostics_Departments dgd where dg.DDept_ID=dgd.DDept_ID ORDER BY TEST_NAME ASC";
	public static final String bedquery="select distinct od.org_name,bd.organization,bd.bed_type from bed_details bd,ORGANIZATION_DETAILS od where bd.organization=od.org_id order by bd.organization";
	
	public static final String insertDiagnosticTest = "INSERT INTO DIAGNOSTICS(TEST_ID,TEST_NAME,DDEPT_ID,UNITS,NVM_LOW,NVM_HIGH,NVF_LOW,NVF_HIGH,INSTRUCTION,REPORT_INFO,SAMPLE_NEEDED,SAMPLE_INFO,HOUSE_STATUS,NEW_BORN_1_TO_30_DAYS_LOW,NEW_BORN_1_TO_30_DAYS_HIGH,INFANTS_1_TO_3_MON_LOW,INFANTS_1_TO_3_MON_HIGH,INFANTS_3_TO_6_MON_LOW,INFANTS_3_TO_6_MON_HIGH,INFANTS_6_TO_12_MON_LOW,INFANTS_6_TO_12_MON_HIGH,CHILD_1_TO_6_YEAR_LOW,CHILD_1_TO_6_YEAR_HIGH,CHILD_6_TO_12_YEAR_LOW,CHILD_6_TO_12_YEAR_HIGH,TURN_AROUND_TIME,TYPE_OF_SPECIMEN,GENERAL_REF_VALUE,DIAG_CODE,FORMAT_NAME)VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";


	public static final String updateDiagnosticTest="UPDATE DIAGNOSTICS SET UNITS=?,NVM_LOW=?,NVM_HIGH=?,NVF_LOW=?,NVF_HIGH=?,INSTRUCTION=?,REPORT_INFO=?,SAMPLE_NEEDED=?,SAMPLE_INFO=?,HOUSE_STATUS=?,NEW_BORN_1_TO_30_DAYS_LOW=?,NEW_BORN_1_TO_30_DAYS_HIGH=?,INFANTS_1_TO_3_MON_LOW=?,INFANTS_1_TO_3_MON_HIGH=?,INFANTS_3_TO_6_MON_LOW=?,INFANTS_3_TO_6_MON_HIGH=?,INFANTS_6_TO_12_MON_LOW=?,INFANTS_6_TO_12_MON_HIGH=?,CHILD_1_TO_6_YEAR_LOW=?,CHILD_1_TO_6_YEAR_HIGH=?,CHILD_6_TO_12_YEAR_LOW=?,CHILD_6_TO_12_YEAR_HIGH=?,TURN_AROUND_TIME=?,TYPE_OF_SPECIMEN=?,GENERAL_REF_VALUE=?,DIAG_CODE=?,FORMAT_NAME=? WHERE TEST_ID=? AND  DDEPT_ID=?";

	/*Queries related to Sample Collection*/
	/*public static final String listOfPatientsforSampleCollection="SELECT   DET.MR_NO,DET.PATIENT_NAME,DECODE(DET.PATIENT_GENDER,'M','Male','F','Female'), DET.PATIENT_AGE,C.CITY_NAME,REG.PATIENT_ID FROM  PATIENT_DETAILS DET JOIN  CITY C ON DET.PATIENT_CITY=C.CITY_ID JOIN PATIENT_REGISTRATION REG   ON DET.MR_NO=REG.MR_NO  JOIN (TESTS_PRESCRIBED TESTS JOIN DIAGNOSTICS DIA ON TESTS.TEST_ID=DIA.TEST_ID)ON DET.MR_NO=TESTS.MR_NO " +
																 " WHERE DIA.SAMPLE_NEEDED='y' AND  TESTS.CONDUCTED='N' AND TESTS.BILL_RAISED='Y' AND TESTS.SFLAG='0' AND REG.CFLAG='0' UNION SELECT DET.MR_NO,DET.PATIENT_NAME,DECODE(DET.PATIENT_GENDER,'M','Male','F','Female'),DET.PATIENT_AGE,C.CITY_NAME, REG.PATIENT_ID   FROM  PATIENT_DETAILS DET JOIN CITY C ON DET.PATIENT_CITY=C.CITY_ID  JOIN PATIENT_REGISTRATION REG  " +
																 " ON DET.MR_NO=REG.MR_NO  JOIN (SAMPLE_COLLECTION TESTS JOIN DIAGNOSTICS DIA ON TESTS.TEST_ID=DIA.TEST_ID)ON DET.MR_NO=TESTS.MR_NO  WHERE DIA.SAMPLE_NEEDED='y' AND  TESTS.CONDUCTED='C'  AND REG.CFLAG='0' AND TESTS.SAMPLE_STATUS='A' UNION SELECT   DET.MR_NO,DET.PATIENT_NAME,DECODE(DET.PATIENT_GENDER,'F','Female','M','Male'),DET.PATIENT_AGE,C.CITY_NAME, REG.PATIENT_ID " +
																 " FROM  PATIENT_DETAILS DET JOIN CITY C ON DET.PATIENT_CITY=C.CITY_ID  JOIN PATIENT_REGISTRATION REG   ON DET.MR_NO=REG.MR_NO  JOIN (GROUPTESTS_PRESCRIBED TESTS JOIN DIAGNOSTICS DIA ON TESTS.TEST_ID=DIA.TEST_ID)ON DET.MR_NO=TESTS.MR_NO  WHERE DIA.SAMPLE_NEEDED='y' AND  TESTS.CONDUCTED='N' AND TESTS.BILL_RAISED='Y' AND TESTS.SFLAG='0' AND REG.CFLAG='0'";*/


	public static final String listOfPatientsforSampleCollection="SELECT   DET.MR_NO,DET.PATIENT_NAME,CASE WHEN DET.PATIENT_GENDER='M'THEN 'Male' ELSE 'Female' END , DET.PATIENT_AGE,C.CITY_NAME,REG.PATIENT_ID FROM  PATIENT_DETAILS DET JOIN  CITY C ON DET.PATIENT_CITY=C.CITY_ID JOIN PATIENT_REGISTRATION REG   ON DET.MR_NO=REG.MR_NO  JOIN (TESTS_PRESCRIBED TESTS JOIN DIAGNOSTICS DIA ON TESTS.TEST_ID=DIA.TEST_ID)ON DET.MR_NO=TESTS.MR_NO "+
	                                                               " WHERE DIA.SAMPLE_NEEDED='y' AND  TESTS.CONDUCTED='N' AND TESTS.BILL_RAISED='Y' AND TESTS.SFLAG='0' AND REG.CFLAG='0' UNION SELECT DET.MR_NO,DET.PATIENT_NAME,CASE WHEN DET.PATIENT_GENDER='M' THEN 'Male' ELSE 'Female' END ,DET.PATIENT_AGE,C.CITY_NAME, REG.PATIENT_ID   FROM  PATIENT_DETAILS DET JOIN CITY C ON DET.PATIENT_CITY=C.CITY_ID  JOIN PATIENT_REGISTRATION REG "+
	                                                               " ON DET.MR_NO=REG.MR_NO  JOIN (SAMPLE_COLLECTION TESTS JOIN DIAGNOSTICS DIA ON TESTS.TEST_ID=DIA.TEST_ID)ON DET.MR_NO=TESTS.MR_NO  WHERE DIA.SAMPLE_NEEDED='y' AND  TESTS.CONDUCTED='C'  AND REG.CFLAG='0' AND TESTS.SAMPLE_STATUS='A' UNION SELECT   DET.MR_NO,DET.PATIENT_NAME,CASE WHEN DET.PATIENT_GENDER='F'THEN'Female' ELSE 'Male'END ,DET.PATIENT_AGE,C.CITY_NAME, REG.PATIENT_ID "+
	                                                               " FROM  PATIENT_DETAILS DET JOIN CITY C ON DET.PATIENT_CITY=C.CITY_ID  JOIN PATIENT_REGISTRATION REG   ON DET.MR_NO=REG.MR_NO  JOIN (GROUPTESTS_PRESCRIBED TESTS JOIN DIAGNOSTICS DIA ON TESTS.TEST_ID=DIA.TEST_ID)ON DET.MR_NO=TESTS.MR_NO  WHERE DIA.SAMPLE_NEEDED='y' AND  TESTS.CONDUCTED='N' AND TESTS.BILL_RAISED='Y' AND TESTS.SFLAG='0' AND REG.CFLAG='0'";


	public static final String sampleCollectionInsertionQuery="INSERT INTO SAMPLE_COLLECTION(MR_NO,PATIENT_ID,TEST_ID,TEST_NAME,SAMPLE_SNO,SAMPLE_SUBSNO,SAMPLE_COLLECTED,CONDUCTED,SAMPLE_STATUS,DEPT_ID,PPRESCRIBED_ID,SAMPLE_DATE,SAMPLE_TIME)VALUES(?,?,?,?,?,?,'Y','N','A',?,?,TO_DATE(?,'DD-MON-YYYY'),?::time without time zone)";
	public static final String testPrescribedUpdateQuery = "UPDATE TESTS_PRESCRIBED SET SFLAG='1' WHERE PRESCRIBED_ID=?";
	public static final String groupTestPrescribedUpdateQuery = "UPDATE GROUPTESTS_PRESCRIBED SET SFLAG='1' WHERE PRESCRIBED_ID=?";
	public static final String sysdateQuery ="SELECT  to_char('now'::date,'YYYY-MON-DD')";


	/*Queries related to testConducting screen*/
	public static final String testPrescribedMrnos="SELECT DISTINCT (TESTS.MR_NO) FROM (TESTS_PRESCRIBED)  TESTS  JOIN ( DIAGNOSTICS DIA JOIN DIAGNOSTICS_DEPARTMENTS DDIA ON  DIA.DDEPT_ID = DDIA.DDEPT_ID ) ON TESTS.TEST_ID=DIA.TEST_ID JOIN  PATIENT_REGISTRATION REG ON TESTS.MR_NO=REG.MR_NO LEFT OUTER JOIN SAMPLE_COLLECTION SAM ON TESTS.PRESCRIBED_ID=SAM.PPRESCRIBED_ID WHERE TESTS.BILL_RAISED='Y' " +
			" AND REG.CFLAG=0 AND (TESTS.CONDUCTED = 'N' OR SAM.CONDUCTED IN ('R','N') )AND (((DIA.SAMPLE_NEEDED='y')AND (TESTS.SFLAG='1')AND (SAM.SAMPLE_STATUS='A'  ))OR((DIA.SAMPLE_NEEDED='n')AND (TESTS.SFLAG='0'))) AND DDIA.DDEPT_ID=? AND DIA.HOUSE_STATUS='I' UNION SELECT DISTINCT (TESTS.MR_NO) FROM (GROUPTESTS_PRESCRIBED)  TESTS  JOIN ( DIAGNOSTICS DIA JOIN DIAGNOSTICS_DEPARTMENTS DDIA ON " +
			" DIA.DDEPT_ID = DDIA.DDEPT_ID ) ON TESTS.TEST_ID=DIA.TEST_ID JOIN  PATIENT_REGISTRATION REG ON TESTS.MR_NO=REG.MR_NO LEFT OUTER JOIN SAMPLE_COLLECTION SAM ON TESTS.PRESCRIBED_ID=SAM.PPRESCRIBED_ID WHERE TESTS.BILL_RAISED='Y' AND REG.CFLAG=0 AND (TESTS.CONDUCTED = 'N' OR SAM.CONDUCTED IN ('R','N') )AND (((DIA.SAMPLE_NEEDED='y')AND (TESTS.SFLAG='1')AND (SAM.SAMPLE_STATUS='A'  ))OR((DIA.SAMPLE_NEEDED='n')AND (TESTS.SFLAG='0')))" +
			" AND DDIA.DDEPT_ID=? AND DIA.HOUSE_STATUS='I'";

	public static final String doctorlist = "Select Doctor_name from doctors order by Doctor_name";
	public static final String diagnosticFormats="Select * from Test_Format";
	public static final String isolatesList="Select distinct  ISO_ID,isolate_name from isolates";
	public static final String checkTestNameinDepartment="SELECT DISTINCT DIA.Test_ID FROM Diagnostics DIA JOIN DIAGNOSTICS_DEPARTMENTS DEPT ON DIA.DDEPT_ID=DEPT.DDEPT_ID WHERE  DEPT.DDEPT_NAME LIKE ? and DIA.Test_Name=?";
	public static final String insertTestDetails ="INSERT INTO Test_Details(MR_No,Patient_Id,Test_ID,Conducted_Date,Conducted_Time,Report_Value,Comments,prescribed_id,CONDUCTED_STATUS) values(?,?,?,TO_DATE(?,'DD-MON-YYYY'),TO_TIMESTAMP(?, 'HH:MI:SS AM'),?,?,?,'C')";

	public static final String updateTestPrescribed="UPDATE Tests_Prescribed SET Conducted='Y' WHERE bill_raised='Y' and test_id=? and MR_No=? and Pat_ID=? and prescribed_id=?";
	public static final String updateGrouptestPrescribed ="UPDATE GroupTests_Prescribed SET Conducted='Y' WHERE bill_raised='Y' and test_id=? and MR_No=? and PATIENT_ID=? and prescribed_id=?";
	public static final String checkSampleOrSubsample ="SELECT * FROM SAMPLE_COLLECTION WHERE SAMPLE_SUBSNO=?";
	public static final String updateSamplCollection="UPDATE SAMPLE_COLLECTION SET CONDUCTED='Y' WHERE PPRESCRIBED_ID=? AND SAMPLE_SNO=?";
	public static final String updateSampleCollectionWithSubNo="UPDATE SAMPLE_COLLECTION SET CONDUCTED='Y' WHERE PPRESCRIBED_ID=? AND SAMPLE_SUBSNO=?";
	public static final String getDoctorId = "SELECT Doctor_ID from Doctors WHERE Doctor_Name=?";
	public static final String getTestCharge="SELECT Charge FROM Diagnostic_Charges WHERE Test_ID=?";
	public static final String insertTestConduct ="INSERT INTO Tests_Conducted(MR_No,Patient_ID,Test_ID,Referred_By,Conducted_date,Conducted_By,Issued_By,Designation,Charge,conducted_time,SATISFACTORY_STATUS,prescribed_id,CONDUCTED_STATUS) values(?,?,?,?,TO_DATE(?,'DD-MON-YYYY'),?,?,?,?,TO_TIMESTAMP(?,'HH:MI:SS AM'),?,?,?)";



	/*Queries related to the diagnostic Department*/
	public static final String checkDuplicateDept = "SELECT * FROM DIAGNOSTICS_DEPARTMENTS WHERE DDEPT_NAME =? and status='A'";
	public static final String getNextPanrepId="SELECT coalesce(MAX(panrep_id),0)+1 FROM u_panrep";
	public static final String getPanrepIdforDiagnosticDept="SELECT panrep_id FROM u_panrep where panrep_name='Diagnostics' and sname='diagnostic'";
	public static final String insertNewDeptQuery="INSERT INTO DIAGNOSTICS_DEPARTMENTS(DDEPT_ID,DDEPT_NAME,STATUS) VALUES(?,?,?)";
	public static final String insertNewDeptIntoPanrep="INSERT INTO u_panrep(RECNO,PANREP_ID,PANREP_NAME,PANREP_TYPE,PANREP_URL,PANREP_REF,PANREP_ORDNO,PANREP_STATUS,PANREP_REMK,SNAME) " +
			" VALUES(?::numeric,?::numeric,?,'S',?,?::numeric,null,'A','inserted for new diag dept','diagnostic')";

	public static final String chechDuplicateDeptwhileUpdating="SELECT * FROM DIAGNOSTICS_DEPARTMENTS WHERE DDEPT_NAME=? AND DDEPT_ID!=?";
	public static final String updateDeptName = "UPDATE DIAGNOSTICS_DEPARTMENTS SET DDEPT_NAME =? WHERE DDEPT_ID=?";
	public static final String deleteDeptName = "DELETE FROM  DIAGNOSTICS_DEPARTMENTS WHERE DDEPT_ID=?";


	/*Queries related to Antibiotic Panes*/
	public static final String getAllAntibiotics ="SELECT ANTI_ID,ANTIBIOTIC_NAME FROM ANTIBIOTIC_NAMES ORDER BY ANTIBIOTIC_NAME ASC";
	public static final String antibioticSave ="INSERT INTO ANTIBIOTIC_NAMES VALUES (?,?)";
	public static final String antibioticUpdate="UPDATE ANTIBIOTIC_NAMES SET  ANTIBIOTIC_NAME=? WHERE ANTI_ID=?";
	public static final String validteAntibioticName="SELECT ANTIBIOTIC_NAME FROM ANTIBIOTIC_NAMES WHERE ANTIBIOTIC_NAME=? ORDER BY ANTIBIOTIC_NAME ";


	/*Queries related to the Isolates*/
	public static final String getAllIsolates ="Select DISTINCT ISOLATE_NAME,ISO_ID from ISOLATES ORDER BY ISOLATE_NAME";
	public static final String insertIsolates ="insert into ISOLATES(ISO_ID,ISOLATE_NAME,ANTIBIOTIC_NAME) values(?,?,?)";
	public static final String updateIsolateName="update ISOLATES set ISOLATE_NAME=? WHERE ISO_ID=?";
	public static final String checkdupIsolateName="select ISOLATE_NAME FROM ISOLATES WHERE ISO_ID= ?";

	/*Queries related to Department Confirmation*/
	public static final String updateTestConductQuery="UPDATE TESTS_CONDUCTED SET CONFIRM_STATUS=? WHERE MR_NO=? AND PATIENT_ID=? AND TEST_ID=? AND PRESCRIBED_ID=? AND CONDUCTED_STATUS='C'";


	/*Queries related to report*/
	public static final String mrnoList ="SELECT DISTINCT MR_NO FROM PATIENT_REGISTRATION";

	/*Queries related to the Diagnostic Cancellation*/
	/*public static final String getAllMrnoswithBillRaisedNo="SELECT DISTINCT (TP.MR_NO) MRNO  FROM TESTS_PRESCRIBED TP, PATIENT_REGISTRATION PR WHERE TP.MR_NO=PR.MR_NO AND TP.PAT_ID=PR.PATIENT_ID AND TP.BILL_RAISED='N' " +
			" AND PR.CFLAG=0 AND TP.CONDUCTED = 'N' UNION SELECT DISTINCT (GP.MR_NO) MRNO FROM  GROUPTESTS_PRESCRIBED GP,PATIENT_REGISTRATION PR WHERE GP.BILL_RAISED='N' AND  GP.CONDUCTED='N' AND GP.MR_NO=PR.MR_NO AND GP.PATIENT_ID=PR.PATIENT_ID";*/


	public static final String getAllMrnoswithBillRaisedNo="SELECT DISTINCT (TP.MR_NO) as MRNO  FROM TESTS_PRESCRIBED TP, PATIENT_REGISTRATION PR WHERE TP.MR_NO=PR.MR_NO AND TP.PAT_ID=PR.PATIENT_ID AND TP.BILL_RAISED='N'  " +
	" AND PR.CFLAG=0 AND TP.CONDUCTED = 'N' AND TP.PACKAGE_NAME='N' UNION SELECT DISTINCT (GP.MR_NO) as MRNO FROM  GROUPTESTS_PRESCRIBED GP,PATIENT_REGISTRATION PR WHERE GP.BILL_RAISED='N' AND  GP.CONDUCTED='N' AND GP.MR_NO=PR.MR_NO AND GP.PATIENT_ID=PR.PATIENT_ID";

	public static final String getCatagoryDetails="Select distinct Tests_Prescribed.Department,Diagnostics_Departments.DDept_Name,Tests_Prescribed.MR_No,Tests_Prescribed.Pat_ID " +
			"  from Tests_Prescribed,Diagnostics_Departments where Conducted like 'N' and Bill_Raised='N'and Diagnostics_Departments.DDept_ID=Tests_Prescribed.Department";

	/*public static final String getTestDetails="Select tp.test_id,tp.Test_Name,Department,MR_No,Pat_ID,tp.Pres_Doctor,dc.doctor_name from tests_prescribed tp ,diagnostics dg,Doctors dc " +
			" where tp.Test_ID=dg.Test_ID and tp.bill_raised='N' and Conducted like 'N'  and dc.Doctor_Id(+)=tp.Pres_Doctor";*/

	public static final String getTestDetails="Select tp.test_id,tp.Test_Name,Department,MR_No,Pat_ID,tp.Pres_Doctor,dc.doctor_name from diagnostics dg,Doctors dc left  outer join tests_prescribed tp " +
	" on  dc.Doctor_Id=tp.Pres_Doctor where tp.Test_ID=dg.Test_ID and tp.bill_raised='N' and Conducted like 'N'";


	/*public static final String fullpatDet="SELECT distinct Patient_Details.MR_No,Patient_Details.Patient_Name,Patient_Details.Patient_Age||decode(Patient_Details.Patient_Age_In,'Y','Years','M','Months','D','Days','NONE') Patient_Age," +
			" decode(Patient_Details.Patient_Gender,'M','Male','F','Female','NONE')Patient_Gender,Tests_Prescribed.Pat_ID, Tests_Prescribed.Pres_Doctor,Doctors.Doctor_Name,Patient_Details.PATIENT_ADDRESS FROM Patient_details,Tests_Prescribed,Doctors" +
			" where Doctors.Doctor_Id(+)=Tests_Prescribed.Pres_Doctor and Patient_details.mr_no=Tests_Prescribed.mr_no order by patient_details.mr_no";
	*/

	public static final String fullpatDet="SELECT distinct Patient_Details.MR_No,Patient_Details.Patient_Name,Patient_Details.Patient_Age||case when Patient_Details.Patient_Age_In='Y'then 'Years' when Patient_Details.Patient_Age_In='M' then'Months'when Patient_Details.Patient_Age_In='D'then 'Days'else'NONE' end as Patient_Age," +
	" case when Patient_Details.Patient_Gender='M' then 'Male'when Patient_Details.Patient_Gender='F' then 'Female' else 'NONE'end as Patient_Gender,Tests_Prescribed.Pat_ID, Tests_Prescribed.Pres_Doctor,Doctors.Doctor_Name,Patient_Details.PATIENT_ADDRESS FROM Patient_details,Doctors left outer join Tests_Prescribed" +
	" on Doctors.Doctor_Id=Tests_Prescribed.Pres_Doctor where  Patient_details.mr_no=Tests_Prescribed.mr_no order by patient_details.mr_no";

	public static final String grouptestPrescriptiondiaglist="SELECT DISTINCT MR_NO FROM  GROUPTESTS_PRESCRIBED WHERE BILL_RAISED='N' AND CONDUCTED='N' order by MR_NO";

	/*public static final String grouptestpatDetQuery="SELECT DISTINCT PD.MR_NO,GP.GROUP_PRIORITY,GP.GROUP_ID,PM.PROFILE_NAME,GP.PATIENT_ID,PD.PATIENT_NAME,PD.PATIENT_AGE||DECODE (PD.PATIENT_AGE_IN,'Y','YEARS','M','MONTHS','D','DAYS','NONE')PATIENT_AGE," +
			" DECODE(PD.PATIENT_GENDER,'M','MALE','F','FEMALE','NONE')PATIENT_GENDER ,GP.PATIENT_ID,GP.PRES_DOCTOR,DC.DOCTOR_NAME,PD.PATIENT_ADDRESS FROM PATIENT_DETAILS PD,GROUPTESTS_PRESCRIBED GP,DOCTORS DC,PROFILE_MASTER PM  WHERE DC.DOCTOR_ID=GP.PRES_DOCTOR " +
			" AND PD.MR_NO=GP.MR_NO AND BILL_RAISED='N' AND CONDUCTED='N' and GP.GROUP_ID=PM.PROFILE_ID ORDER BY PD.MR_NO";*/

	public static final String grouptestpatDetQuery="SELECT DISTINCT PD.MR_NO,GP.GROUP_PRIORITY,GP.GROUP_ID,PM.PROFILE_NAME,GP.PATIENT_ID,PD.PATIENT_NAME,PD.PATIENT_AGE||CASE WHEN PD.PATIENT_AGE_IN='Y' THEN 'YEARS' WHEN PD.PATIENT_AGE_IN='M'THEN 'MONTHS' WHEN PD.PATIENT_AGE_IN='D' THEN 'DAYS' ELSE 'NONE'END AS PATIENT_AGE," +
	" CASE WHEN PD.PATIENT_GENDER='M' THEN 'MALE' WHEN PD.PATIENT_GENDER='F' THEN 'FEMALE' ELSE 'NONE'END AS PATIENT_GENDER ,GP.PATIENT_ID,GP.PRES_DOCTOR,DC.DOCTOR_NAME,C.CITY_NAME AS PATIENT_ADDRESS FROM PATIENT_DETAILS PD,GROUPTESTS_PRESCRIBED GP,DOCTORS DC,PROFILE_MASTER PM,CITY C  WHERE DC.DOCTOR_ID=GP.PRES_DOCTOR " +
	" AND PD.MR_NO=GP.MR_NO AND BILL_RAISED='N' AND CONDUCTED='N' and GP.GROUP_ID=PM.PROFILE_ID AND C.CITY_ID=PD.PATIENT_CITY ORDER BY PD.MR_NO";


   /*Queries related to the Diagnostic Cancellation*/
	public static final String getDeptid = "SELECT DDEPT_ID FROM Diagnostics_Departments where DDept_Name=?";
	public static final String getTestid = "SELECT TEST_ID FROM Diagnostics where TEST_NAME=? AND DDEPT_ID=?";
	public static final String cancelTestPrescribed="UPDATE Tests_Prescribed SET Conducted='Cancel',Cancelled_By=?,Cancel_Date=to_date(?,'DD-MON-YYYY'),BILL_RAISED='C' WHERE MR_No=? and Pat_ID=? and Department=? and TEST_ID=? and Conducted='N'and Bill_Raised='N'";
	public static final String getProfileid ="SELECT PROFILE_ID FROM PROFILE_MASTER where PROFILE_NAME=?";
	public static final String cancelGroupTestPres = "UPDATE groupTests_Prescribed SET Conducted='Cancel',Cancelled_By=?,Cancel_Date=to_date(?,'DD-MON-YYYY') WHERE MR_No=? and Patient_ID=? and group_id=? and  Conducted='N'and Bill_Raised='N'";


	/*Queries related to the  Sample Cancellation*/
	public static final String getMarnosforSampleCancellation = "SELECT  DISTINCT  MR_NO FROM SAMPLE_COLLECTION  WHERE  CONDUCTED IN ('R','N') AND SAMPLE_STATUS='A'";
	public static final String getSampleCancellationTestDetails="SELECT  DISTINCT  MR_NO,TEST_ID,TEST_NAME,SAMPLE_SNO,SAMPLE_SUBSNO , PPRESCRIBED_ID FROM SAMPLE_COLLECTION " +
			" WHERE  CONDUCTED IN ('R','N')AND SAMPLE_STATUS='A'";
	public static final String updateSampleCollectionStatus ="UPDATE SAMPLE_COLLECTION SET SAMPLE_STATUS='I',CONDUCTED='C' WHERE  PPRESCRIBED_ID=?";
	public static final String updateSamplStatusInTestPrescribed ="UPDATE TESTS_PRESCRIBED SET SFLAG='0' WHERE PRESCRIBED_ID=?";
	public static final String updateSamplStatusInGroupPrescribed="UPDATE GROUPTESTS_PRESCRIBED SET SFLAG='0' WHERE PRESCRIBED_ID=?";
	public static final String getPrescriptionType ="SELECT PRESCRIBED_ID FROM TESTS_PRESCRIBED WHERE PRESCRIBED_ID=?";

	/*Queries related to the outhouse master*/
	public static final String outgoingtestdetails="SELECT TEST_ID,TEST_NAME,DDEPT_ID FROM DIAGNOSTICS WHERE HOUSE_STATUS='O' ORDER BY TEST_NAME ASC";
	public static final String outHouse="SELECT * FROM OUTHOUSE_MASTER ";
	public static final String ohMasterDetails="SELECT * FROM OHMASTER_DETAIL";
	public static final String getOutHouseId ="select OH_ID from OUTHOUSE_MASTER where OH_NAME=?";
	public static final String getOHCharge="select OH_CHARGE from OHMASTER_DETAIL where OH_DEPT=? AND  OH_TESTID=? AND OH_ID=?";
	public static final String insertOutHouseQuery ="insert into OUTHOUSE_MASTER(OH_ID,OH_NAME) values(?,?)";
	public static final String insertOHMasterDetails="insert into OHMASTER_DETAIL(OH_ID,OH_DEPT,OH_TESTID,OH_CHARGE) values(?,?,?,?)";
	public static final String updateOHMasterDetails ="update  OHMASTER_DETAIL  set OH_CHARGE=? WHERE OH_TESTID=?  AND OH_DEPT=? AND OH_ID=?";

	/*Queries related to Profile Master */
	public static final String checkquery = "SELECT  * FROM PROFILE_MASTER ";
	public static final String profileNameQuery="SELECT DISTINCT( PROFILE_ID), PROFILE_NAME FROM PROFILE_MASTER WHERE STATUS='A' ORDER BY PROFILE_NAME ";
	public static final String getGeneralchargesQuery="SELECT DISTINCT(ORG_ID) FROM ORGANIZATION_DETAILS WHERE ORG_NAME=?";
	public static final String insertProfileMaster ="INSERT INTO PROFILE_MASTER (PROFILE_ID,PROFILE_NAME,PROFILE_INSTRUCTION,PROFILE_TESTS,SERVICE_TAX)VALUES(?,?,?,?,?::numeric)";

	public static final String insertProfileMasterCharge="INSERT INTO PROFILE_MASTER_CHARGES(PROFILE_ID,ORG_NAME,BED_TYPE,PROFILE_STAT,PROFILE_ROUTINE,PROFILE_SCHEDULE ) VALUES(?,?,?,?::numeric,?::numeric,?::numeric)";
	public static final String updateProfileMaster ="UPDATE PROFILE_MASTER SET SERVICE_TAX=?::NUMERIC WHERE PROFILE_ID=?";
	public static final String updateProfileMasterCharges="UPDATE PROFILE_MASTER_CHARGES SET PROFILE_STAT=?::NUMERIC,PROFILE_ROUTINE=?::NUMERIC,PROFILE_SCHEDULE=?::NUMERIC WHERE PROFILE_ID=? AND ORG_NAME=? AND BED_TYPE=?";


	/*Queries related to the OP Conducting Services*/
	public static final String getServicePrescribedMrnos ="SELECT DISTINCT MR_NO FROM SERVICES_PRESCRIBED  WHERE CONDUCTED='N' AND  BILL_RAISED='Y' AND  PATIENT_ID LIKE 'OPNO%' ORDER BY MR_NO";
	public static final String updateServicePrescribed ="UPDATE SERVICES_PRESCRIBED SET CONDUCTED='Y', CONDUCTEDBY=?,CONDUCTEDDATE=localtimestamp(0),COMMENTS=? WHERE MR_NO=? AND PATIENT_ID=? AND SERVICE_ID=? AND CONDUCTED='N'";
	public static final String updateServiceBillingDetails="update  services_billing_detail set CONDUCTED_DATE=localtimestamp(0) where MR_NO=? AND PATIENT_ID=? AND SERVICE_ID=?";


	/*Queries related to Incoming Sample Registration*/
	public static final String getPatientIdsfromohsamplereg="SELECT distinct oh_patid  as oh_patid from OH_SAMPLEREGISTRATION";
	public static final String getOutHouseDetails ="SELECT OH_ID,OH_NAME FROM OUTHOUSE_MASTER";
	public static final String getIncomingTests ="SELECT TEST_ID,TEST_NAME,DDEPT_ID FROM DIAGNOSTICS WHERE HOUSE_STATUS='I' ORDER BY TEST_NAME ASC";
	public static final String insertOhSampleRegistration="INSERT INTO OH_SAMPLEREGISTRATION(OH_PATID,PATIENT_NAME,AGE,GENDER,CONTACT_PERSON,PHONE_NO,FROM_HOUSE) VALUES(?,?,?,?,?,?,?)";
	public static final String insertOhSampleRegDetails ="INSERT INTO OH_SAMPLEREG_DETAILS(OHSAMPLE_UNIQUE,SAMPLENO,PATIENT_ID,OH_DEPT,OH_TESTID) VALUES(?,?,?,?,?)";

	/*Queries related to Outgoing Tests*/
	/*public static final String getOutgoingPatients ="select sc.sample_sno,sc.patient_id,pd.patient_name,sc.tesT_id,sc.test_name,sc.pprescribed_id from sample_collection sc," +
			" patient_details pd, diagnostics dc where sc.mr_no=pd.mr_no and sample_collected='Y' and conducted='N' and sample_status='A' " +
			" and ((sc.PPRESCRIBED_ID not in(select id.IH_UNIQUENO from inhouse_details id))) and dc.test_id=sc.test_id and dc.house_status='O' order by sc.sample_sno";*/


	public static final String getOutgoingPatients ="select sc.mr_no,sc.sample_sno,sc.patient_id,pd.patient_name,sc.tesT_id,sc.test_name,sc.pprescribed_id from sample_collection sc," +
				" patient_details pd, diagnostics dc where sc.mr_no=pd.mr_no and sample_collected='Y' and conducted='N' and sample_status='A' " +
				" and ((sc.PPRESCRIBED_ID not in(select id.IH_UNIQUENO from inhouse_details id))) and dc.test_id=sc.test_id and dc.house_status='O' order by sc.sample_sno";

	public static final String inhouseDetails = "insert into inhouse_details(sample_no,patient_id,test_id,oh_id,ih_status,IH_UNIQUENO,mrno) values(?,?,?,?,'O',?,?)";

	/*Queries related to diagnostic Charges*/
	public static final String getdiagnewtestnamesdepts="SELECT DISTINCT (D.DDEPT_ID),DD.DDEPT_NAME FROM  DIAGNOSTICS_DEPARTMENTS DD,DIAGNOSTICS D WHERE D.TEST_ID NOT IN(SELECT DISTINCT( DC.TEST_ID) FROM DIAGNOSTIC_CHARGES DC) AND DD.DDEPT_ID=D.DDEPT_ID ORDER BY DD.DDEPT_NAME ASC";
	public static final String getDiagChargeTestDetails="select distinct (dg.test_id),dg.test_name,dg.Ddept_id,dgd.DDept_Name from diagnostics dg,Diagnostics_Departments dgd,diagnostic_charges dc where dg.DDept_ID=dgd.DDept_ID and dc.test_id=dg.test_id ORDER BY TEST_NAME ASC";
	public static final String getNettestNameDetails="SELECT DISTINCT (D.DDEPT_ID),DD.DDEPT_NAME,D.TEST_ID,D.TEST_NAME  FROM  DIAGNOSTICS_DEPARTMENTS DD,DIAGNOSTICS D WHERE D.TEST_ID NOT IN(SELECT DISTINCT( DC.TEST_ID) FROM DIAGNOSTIC_CHARGES DC) AND DD.DDEPT_ID=D.DDEPT_ID ORDER BY D.TEST_NAME ASC";
	//public static final String getBedDetails="select distinct od.org_name,bd.organization,bd.bed_type from bed_details bd,ORGANIZATION_DETAILS od where bd.organization=od.org_id order by bd.organization";
	public static final String getBedDetails = " select distinct  od.org_name,bd.organization,bd.bed_type from bed_details bd,ORGANIZATION_DETAILS od where od.status='A' and bd.organization=od.org_id union "+
											   " select ORG.ORG_NAME as org_name,ORG.ORG_ID as organization , IBC.INTENSIVE_BED_TYPE as bed_type from ORGANIZATION_DETAILS ORG,ICU_BED_CHARGES IBC WHERE ORG.STATUS='A' AND ORG.ORG_ID = IBC.ORGANIZATION ";


}
