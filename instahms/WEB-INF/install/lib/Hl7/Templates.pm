package Hl7::Templates;

use strict;
use warnings;

my %SEGMENT_TEMPLATES = (
	MSH => [qw{fieldSep encoding sendApp sendFac recvApp
			recvFac msgTimest security msgType controlId
			procId ver seq continuePtr ackType
			appAckType country charset lang}],

	MSA => [qw{code controlId msgText expectedSequence delayedAckType
			errorCondition}],

	PID => [qw{sid pid pidList altPid name
			momName dob sex alias race
			addr country phHome phBus lang
			maritalSts religion accNum ssnNum dlNum
			momId ethnicGrp pob multBirth birthOrder
			citizenship	veterans nationality deathTimest death}],

	PV1 => [qw{sid patientClass assgndLoc admType preadmitNum
			priorLoc attdDoctor refDoctor consDoctor hospService
			tempLoc preadmitTestInd readmitInd admitSrc ambulSts
			vipInd admitDoctor patientType visitNum financialClass
			chargePriceInd courtesyCode	creditRating contractCode contractEffDate
			contractAmt contractPeriod interestCode xferBaddCode xferBaddDate
			baddAgencyCode baddXferAmt baddRecovAmt delAccInd
			delAccDate dischDisp dischLoc dietType servFac
			bedSts accountSts pendingLoc priorTempLoc admitTimest
			dischTimest curBalance totalCharges totalAdjs totalPmts}],

	IN1 => [qw{sid insPlanId insCoId insCoName insCoAddr
			insCoContactPer insCoPhNum groupNum groupName groupEmpId
			groupEmpName planEffectiveDate planExpireDate authInfo planType
			insuredName insuredRelPatient insuredDob insuredAddr assignmentOfBenefits
			coordinationOfBenefits coordOfBenPriority noticeOfAdmFlag noticeOfAdmDate reportOfEligibilityFlag
			reportOfEligibilityDate releaseInformationCode preAdmitCert verificationDateTime verificationBy
			typeOfAgreementCode billingStatus lifetimeReserveDays delayBeforeLRDay companyPlanCode
			policyNumber policyDeductible policyLimitAmount policyLimitDays roomRateSemiPrivate
			roomRatePrivate insuredsEmploymentStatus insuredsAdministrativeSex insuredsEmployersAddress verificationStatus
			priorInsurancePlanID coverageType handicap insuredsIDNumber signatureCode
			signatureCodeDate insuredsBirthPlace vipIndicator}],
			
	IN2 => [qw{insuredEmpId insuredSocialSecurityNum insuredEmprNameandId emprInfoData mailClaimParty 
			medicareHealthInsCardNum medicaidCaseName medicaidCaseNumber militarySponsorName militaryIdNum 
			dependentOfMilitaryRecipient militaryOrganization militaryStation militarySer militaryRank 
			militaryStatus militaryRetireDate militaryNonAvailCertOnFile babyCoverage combineBabyBill
			bloodDeductible specialCoverageApprovalName specialCoverageApprovalTitle nonCoveredInsCode payorId 
			payorSubscriberId eligibilitySource roomCoverageAmt policyAmt dailyDeductible 
			livingDependency ambulatoryStatus citizenship primaryLanguage livingArrangement
			publicityCode protectionIndicator studentIndicator religion mothersMaidenName 
			nationality ethnicGroup maritalStatus insuredsEmploymentStartDate employmentStopDate 
			jobTitle jobCode jobStatus emprContactPersonName emprContactPersonPhnum 
			emprContactReason insuredsContactPersonName insuredsContactPersonPhNum insuredContactPersonReason relationshipToThePatientStartDate 
			relationshipToThePatientStopDate insCoContactReason insCoContactPhNum policyScope policySource 
			patientMemberNum guarantorsRelationshipToInsured insuredsHomePhNum insuredsEmprPhNum militaryHandicappedProgram 
			suspendFlag copayLimitFlag stopLossLimitFlag insuredOrgNameandId insuredEmprOrgNameandId 
			race cmsPatientsRelationshipToInsured}],

	DG1 => [qw{sid diagnosisCodingMethod diagnosisCode diagnosisDescription diagnosisDateTime 
			diagnosisType majorDiagnosisCategory diagnosticRelatedGrp DRGApprovalIndicator DRGGrouperReviewCode
			outlierType outlierDays outlierCost grouperVersionAndType diagnosisPriority 
			diagnosisClinician diagnosisClassification confidentialIndicator attestationDateTime diagnosisIdentifier 
			diagnosisActionCode}],

	ORC => [qw{ctrl placerOrderNum fillerOrderNum placerGrpNum orderSts
			respFlag qty parent timest enteredBy
			verifiedBy ordProvider entererLoc callbackPh effectiveTime
			ctrlCodeReason enteringOrg enteringDev actionBy advBenefNoticeCode}],

	OBR => [qw{sid placerOrderNum fillerOrderNum serviceId priority
			reqTime obsTime obsEndTime collVolume collId
			specimenAction danger clinicalInfo specimenTime specimenSource
			ordProvider callbackPh placer1 placer2 filler1
			filler2 resultTs chargeTo diagServId resultSts
			parentResult qtyTiming resultCcTo parent transMode
			reason principalIntrprtr asstIntrprtr technician transcriptionist scheduledDtTime}],

	OBX => [qw{sid valueType obsId obsSubId obsValue
			units refRanges abnormalFlags probability natureAbnTest
			obsResultSts dateLastNormal accessChecks obsTime producerId
			responsibleObserver obsMethod equipInstance analTime}],

	NTE => [qw{sid srcOfComment comment commentType}],

	ZTD	=> [qw{treatmentDate prescDate prescProfile shiftName treatType
			treatMethod access transToDialysis transFromDialysis billingRemarks
			remarks treatStartTime treatStopTime effectiveTime standStillTime
			prescribedTime ufTime avgUfRate totalUfVol avgBloodFlow
			totalBloodFlow avgArtPress avgVenPress avgTMP recirculation
			avgDiaFlow avgDiaTemp avgConductivity machineSodium preWeight
			postWeight targetWeight machineType machineNo machineId
			sysPre sysPost diaPre diaPost MAPPre
			MAPPost pulsePre pulsePost antiCoagType antiCoagRate
			antiCoagVol targetKtV measuredKtV dialyzer contentrate
			bicarbonate addnlConcentrate substitute arterialNeedle venousNeedle
			singleNeedle arterialBloodSys venousBloodSys}],

	RXO	=> [qw{giveCode giveAmtMin giveAmtMax giveUnits dosageForm
			treatInstructions admInstructions deliverLocation allowSubst dispCode
			dispAmt dispUnits noOfRefills providerDEA pharmacistId
			needsHumanReview givePerTimeUnit giveStrength giveStrengthUnits indication
			giveRateAmount giveRateUnits totalDailyDose}],
);

sub getSegmentTemplate {
	my $type = shift;
	return $SEGMENT_TEMPLATES{$type};
}

1;

