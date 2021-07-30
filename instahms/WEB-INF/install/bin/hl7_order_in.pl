#!/usr/bin/perl -w
package hl7_order_out;
use strict;

use File::Basename;
use File::Copy;
use Getopt::Long;
use DBI;
use POSIX qw(strftime);
use IO::Socket::INET;
use Sys::Hostname;
use POSIX ":sys_wait_h";
use IO::Handle;
use REST::Client;
use JSON;
use URI::Escape;

use lib dirname($0)."/../lib";	# find our Hl7 module. Needs to be before the next two lines

use Insta::Util;
use Insta::Logger;
use Insta::HL7Module::LIS;
use Insta::HL7Module::Service;
use Insta::HL7Module::Consultation;
use constant {
	FAILURE => 0,
	SUCCESS => 1,
	DUPLICATE => 2,
	INVALID => 3
};

# Database configuration
our $opt_db = 'hms';
our $opt_port = 5432;
our $opt_schema ='' ;
our $opt_host = 'localhost';
our $opt_username = 'postgres';
our $opt_password ='';
our $opt_foreground = 0;
our $opt_module ='';
our $opt_apiuser = '';
our $opt_apipwd ='';
our $opt_apihost='localhost';
our $opt_apiport='80';
our $opt_apiname='instaapi';
my $client = REST::Client->new();

sub usage {
	return "Usage: $0 [OPTIONS] <schema>\n" .
	" OPTIONS: \n" .
	"  -d|--db <database>: which database to connect to (hms)\n" .
	"  -s|--schema <schema>: which schema to operate on (hostname)\n" .
	"  -h|--host <host> : datbase host\n".
	"  -U|--username <username> : database username\n".
	"  -W|--password <password> : database password\n".
	"  -o|--port <port_no> : database port no".
	"  -f|--foreground: print to console instead of logfile\n".
	"  -m|--module : hl7 module is required like(LIS, RIS, SERVICES, CONSULTATION)\n" .
	"  -au|--apiuser :  API application username\n".
	"  -ap|--apipwd : API application password \n" .
	"  -ah|--apihost : API application host \n" .
	"  -ap|--apiport : API application port \n" .
	"  -an|--apiname : API application name \n" ;

}

GetOptions( "port|o=i", "db=s", "schema=s", "host=s", "username|U=s", 'password|W=s', "foreground!", "module|m=s", "apiuser=s", "apipwd=s",
		"apihost=s", "apiport=s", "apiname=s") or die usage();
my $apiurl = "http://$opt_apihost:$opt_apiport/$opt_apiname";
my $util = Insta::Util->new();
my $log = Insta::Logger->new({level=>$util->getProperty('log.level'),base_name=> ($opt_module .'_'. basename($0)),db=>$opt_db,
								schema=>$opt_schema,isConsilePrint=>$opt_foreground});
$log->rotateLog(10*1024*1024);
$log->info("#####################>>>>>>>>>>>>>>>>>INIT<<<<<<<<<<<<<<<<<<<<<#################");

END { 
	$log->info("#####################<<<<<<<<<<<<<<<<<END>>>>>>>>>>>>>>>>>>>>>>#################\n");
}



##################################################
sub getConnection {
##################################################
	my $dbh = DBI->connect("dbi:Pg:dbname=$opt_db;host=$opt_host;port=$opt_port", "$opt_username", "$opt_password",
		{AutoCommit => 1, RaiseError =>1});
	$dbh->do("SET search_path TO $opt_schema");
	$dbh->do("SET application.username TO '_system'");
	return $dbh;
}


##################################################
sub readConfig {
##################################################
	my $dbh = getConnection();
	my $sql = "SELECT * FROM hl7_lab_interfaces  
                  JOIN hl7_center_interfaces hci USING(interface_name)
                  WHERE status='A' 
                  AND interface_type = 'LIS'
                  AND export_type IN ('F')";

	my $hash_ref = $dbh->selectall_hashref($sql,'interface_name');
	$dbh->disconnect();

	return $hash_ref;
}

##################################################
sub loginReq {
##################################################
# RC : Login request should not be hard coded  
# Correct It should not hard code how it will take. Will it option based or it will take from properties file?
	my $login = "$apiurl/Customer/Login.do";
	my $loginQueryStr = "_method=login&hospital_name=$opt_schema&customer_user_id=$opt_apiuser&customer_user_password=$opt_apipwd";
	my $url = $login.'?'.$loginQueryStr;
	$log->debug("Login Request : $url");
	$client->GET($url);
	return $client->responseContent();
}



=comment

patientname = Rajendran Arun  :   PID : name (05): [Arun,Rajendran]

agefield=30  	:	PID : dob (07): 19480101000000

gender=M  	:	PID : sex (08): M

sampleTypeId=9

testId = DGC0287 :	OBR :  serviceId (04): [4039,Basophils]

bill_rate_plan_id=ORG0001
	
orig_sample_no=123samp

outhouseId=28

centerId=7

=cut


##################################################
sub isrRequest {
##################################################
	my $isr = "$apiurl/Customer/isr.do";
	my $isrQueryStr = undef;
	$isrQueryStr = shift;
	my $queryStr = $isr."?".$isrQueryStr;
	$log->debug("ISR URL : ". $queryStr);	
	$log->info("Requesting ISR");
	$client->GET($queryStr);
	return $client->responseContent();
}

sub processCancellation {
	my $msg = shift;
	my ($prescId, $resultCode, $sampleId) = readPlacerOrderNum($msg);
	my $tpRowLIS = undef;
	eval {
		$tpRowLIS = getLISTestsPrescribedRow($prescId);
	};

	if ($tpRowLIS) {
		if ($tpRowLIS->{conducted} eq 'X') {
			return DUPLICATE;
		}
		my $updateCancellationInTestsPrescribedQuery = qq {
			UPDATE tests_prescribed 
			SET user_name = ?, conducted = 'X', cancelled_by = ?, cancel_date = current_date
			WHERE prescribed_id = ?
		};

		my $discardCancelledTestReportQuery = qq {
			UPDATE test_visit_reports
			SET report_state = 'D'
			WHERE report_id = ?
		};

		my $getChargeIDOfActivityQuery = qq {
			SELECT charge_id FROM bill_activity_charge 
			WHERE activity_code = 'DIA' AND activity_id = ? ORDER BY charge_id
		};

		my $updateHasActivityBillChargeQuery = qq {
			UPDATE bill_charge SET hasactivity = FALSE where charge_id = ?
		};

		my $deleteRecordFromBillActivityChargeQuery = qq {
			DELETE from bill_activity_charge WHERE activity_id=? AND activity_code='DIA'
		};

		my ($dbh, $sth);
		if ($tpRowLIS->{report_id}) {
			my $isSignedOff;
			eval {
				$isSignedOff = isReportSignedOff($tpRowLIS->{report_id});
			};
			if ($isSignedOff) {
				$log->info("Report already signed off. Cannot allow cancellation.\n");
				return INVALID;
			}
			$dbh = getConnection();
			$log->info("Discarding cancelled test report with reportId : $tpRowLIS->{report_id}\n");
			eval {
				$sth = $dbh->prepare($discardCancelledTestReportQuery);
				$sth->execute($tpRowLIS->{report_id});
				$sth->finish();
			};
		}
		$log->info("Updating cancellation in tests_prescribed with prescribed_id : $tpRowLIS->{prescribed_id}\n");
		$dbh = getConnection() unless defined($dbh);

		eval {
			$sth = $dbh->prepare($updateCancellationInTestsPrescribedQuery);
			$sth->execute($opt_apiuser, $opt_apiuser, $tpRowLIS->{prescribed_id});
			$sth->finish();
			my $billChargeResult = $dbh->selectrow_hashref($getChargeIDOfActivityQuery, undef, $tpRowLIS->{prescribed_id});
			$log->info("Bill charge id for prescribed_id: $tpRowLIS->{prescribed_id} = $billChargeResult->{charge_id}\n");
			$sth = $dbh->prepare($updateHasActivityBillChargeQuery);
			$sth->execute($billChargeResult->{charge_id});
			$sth->finish();
			$sth = $dbh->prepare($deleteRecordFromBillActivityChargeQuery);
			$sth->execute($tpRowLIS->{prescribed_id});
			$sth->finish();
		};

		$dbh->disconnect();
	}

	if ($@) {
		$log->error($@);
		return FAILURE;
	}

	return SUCCESS;
}

sub isReportSignedOff {
	my $reportId = shift;
	my $dbh = getConnection();
	my $signoffStatusQuery = qq {
		SELECT signed_off FROM test_visit_reports where report_id = ?
	};

	my $signoffStatusQueryResult = $dbh->selectrow_hashref($signoffStatusQuery, undef, $reportId);
	$dbh->disconnect();
	return ($signoffStatusQueryResult->{signed_off} eq 'Y');
}

sub readPlacerOrderNum {
	my $msg = shift;
	my $orderNum = undef;
	my ($prescId, $resultLabel, $sampleNo);
 	if(ref $msg->{ORC}->{placerOrderNum} eq "ARRAY") {
        $orderNum =  $msg->{ORC}->{placerOrderNum}[0];
  	} else {
        $orderNum =  $msg->{ORC}->{placerOrderNum}; 
  	}

	my @orderNumArr = split(/\./, $orderNum, 3);
 	my $orderNumSize = @orderNumArr;
 	if ($orderNumSize == 1) {
 		$prescId = $orderNumArr[0];
 	} elsif($orderNumSize == 2) {
 		$prescId = $orderNumArr[0];
 		$sampleNo = $orderNumArr[1];
 	} elsif($orderNumSize == 3) {
 		$prescId = $orderNumArr[0];
 		$resultLabel = $orderNumArr[1];
 		$sampleNo = $orderNumArr[2];
 	}
 	
   	return ($prescId, $resultLabel, $sampleNo);
}

sub processNewOrder {
	my $msg = shift;
###################Mandatory Fields#######################
    my @formField = ();
 	my ($prescId, $resultCode, $sampleId) = readPlacerOrderNum($msg);
    my $row = undef;
    my $test_id;
	my $sampleTypeId = $msg->{OBR}{placer2};
	my $colSampleDate = $msg->{OBR}{specimenTime};
	if (defined($resultCode) && $resultCode ne "") {
		$row = getResultTestDetails($resultCode);
		$test_id = $row->{test_id};
	} else {
		$test_id = $msg->{OBR}{serviceId}[0];
	}
	my $pv1 = $msg->{PV1};
	my $conductingDocId = $pv1->get('attdDoctor');
	print "conducting doc id : $conductingDocId\n";
	print "Sample Type Id : $sampleTypeId\n";		
	push(@formField, "testId=$test_id");
    $log->info("prescId : $prescId,  resultCode : $resultCode, sampleNo : $sampleId, sampleTypeId : $sampleTypeId");
	
	# Check if order is duplicate.
	if(isPrescribedIdExists($prescId, $test_id)) {
		return DUPLICATE;
	}

    push(@formField, "orig_sample_no=$sampleId");
    push(@formField, "sampleId=$sampleId");
    push(@formField, "hisToken=y");
    my $centerName = $msg->{MSH}{recvFac} || "Default Center";
	push(@formField, "centerName=$centerName");
	print "center name check : $centerName";
    my $orginalLabName = $msg->{MSH}{sendFac} || '';
	push(@formField, "orginalLabName=$orginalLabName");
	my $patientname = "";
	$patientname = $msg->{PID}{name}[4] || "";
	$patientname = $patientname . " " . $msg->{PID}{name}[1] || "";
	$patientname = $patientname . " " . $msg->{PID}{name}[2] || "";
	$patientname = $patientname . " ". $msg->{PID}{name}[0] || "";
	$log->info("Patient Name : $patientname");
	push(@formField, "patientname=$patientname");
	my $patient_phone = uri_escape($msg->{PID}{phHome}[0]);
	push(@formField, "phone_no=$patient_phone");
	push(@formField, "patient_other_info=$msg->{PID}{pidList}[0]");
    my $gender = $msg->{PID}{sex};
	push(@formField, "gender=$gender");
    my $agefield = substr($msg->{PID}{dob},0,8);
	$log->info("Age field = $agefield");
	my $age_unit=undef;my $age = undef; 
	$age_unit = get_age_unit($agefield);		
	$age = get_age($agefield);
	$log->info("Patient age : $age $age_unit");
	push(@formField, "agefield=$age");
  	push(@formField, "billType=BL");
	push(@formField, "category=DEP_LAB");
	push(@formField, "bill_rate_plan_id=ORG0001");
	push(@formField, "sampleTypeId=$sampleTypeId");	    	
	push(@formField, "conducting_doctor_id=$conductingDocId");	    	
	push(@formField, "colSampleDate=$colSampleDate");
	push(@formField, "his_patientID=$msg->{PV1}{visitNum}");

	##################### API LOGIN ######################
	my $res_l =  decode_json(loginReq());
	if(!defined $res_l->{request_handler_key}) {
		$log->error("Please enter correct username and password for API login");
		exit 1;
	}
	$log->info("Login Response : $res_l->{return_message}");
	$log->debug("Login response : " . encode_json($res_l));
	################# API LOGIN SUCCESSFUL ###############
	push(@formField, "request_handler_key=$res_l->{request_handler_key}");
	
	my $str = join("&",@formField);
	$log->debug("ISR FORM Fields = @formField");
	my $res = decode_json(isrRequest($str));
	$log->info("========================STARTING API LOG PRINT====================");
	foreach my $temp (@{$res->{apiLog}}) {
		$log->info($temp);
	}

	$log->info("========================END API LOG PRINT=========================");
	$log->info("ISR response message  : $res->{return_message}");
	$log->info("ISR response code : $res->{return_code}");
	$log->info("Patient VISIT_ID : $res->{INHOUSE_VISIT_ID}");

	if ($res->{return_code} eq "2001") {
		$log->info("Updating tests prescribed, Prescription Id : $prescId for Incoming Visit Id : $res->{INHOUSE_VISIT_ID},
		 testId : $test_id");
		my $rowCount = updateHisPrescribedId($prescId, $res->{INHOUSE_VISIT_ID}, $test_id);
		$log->info("Updated rows of test prescribed :$rowCount ");
		$log->info("Updating incoming sample registration for Incoming Visit Id : $res->{INHOUSE_VISIT_ID}");
		my $rowCountIsr = updateIsrDateofBirth($res->{INHOUSE_VISIT_ID}, $agefield);
		$log->info("Updated rows isr_dateofbirth of incoming sample registration  :$rowCountIsr ");
		if( $age_unit eq "M" or $age_unit eq "D" ) {
			my $u_age_unit = update_age_unit($age_unit,$res->{INHOUSE_VISIT_ID});
			$log->info("Updated age unit for visit_id : $res->{INHOUSE_VISIT_ID}, Updated Number of rows : $u_age_unit");
		}
		return SUCCESS;
	}

	return FAILURE;
}

##################################################
sub processInterface {
##################################################
	my $row_interface = shift;
	my $baseDir = $row_interface->{import_dir};
	my $inputDir = "$baseDir/in";		# location for input files
	my $doneDir  = "$baseDir/done";		# moved here after successful processing
	my $errorDir = "$baseDir/error";	# moved here if any errors. Move back to in to reprocess

	opendir(DIR, $inputDir) || die "Unable to opendir $inputDir: $!\n";
	my $file;

	while ($file = readdir(DIR)) {
		next if (($file eq ".") || ($file eq ".."));
		my ($error, $errMsg);
		eval {
			my $messageStr;
			$log->info(">>>>Processing $file");
			open (MSGF, "$inputDir/$file") || die "Unable to open $file: $!\n";
			{
				local $/;
				$messageStr = <MSGF>;
		  	}
			my $msg = Hl7::Message->new($messageStr);
			my $controlCode = $msg->{ORC}{ctrl};
			my $operationStatus = 0;
			if ($controlCode eq 'CA') {
				$operationStatus = processCancellation($msg);
			} elsif ($controlCode eq 'NW') {
				$operationStatus = processNewOrder($msg);
			}
			# All the processing done, file operation performed.
            if($operationStatus == SUCCESS) {
				$log->info("$file processed, moving to done >>>>>>\n");
				move("$inputDir/$file", "$doneDir/$file");
			} elsif ($operationStatus == FAILURE) {
				$log->error("$file has errors, moving to error >>>>>>\n");
				move("$inputDir/$file", "$errorDir/$file");
			} elsif ($operationStatus == DUPLICATE) {
				$log->warn("$file has already registered, moving to duplicate >>>>>>\n");
				move("$inputDir/$file", "$baseDir/duplicate/$file") || die "Unable to write $baseDir/duplicate/$file\n";
			} else {
				$log->info("moving $file to invalid >>>>>>\n");
				move("$inputDir/$file", "$baseDir/invalid/$file") || die "Unable to write $baseDir/invalid/$file\n";
			}

			close MSGF;
		};

		if ($@) {
			$log->error($@);
			$log->error("$file has errors, moving to error >>>>>>\n");
			move("$inputDir/$file", "$errorDir/$file");
		}
	}

	closedir(DIR);
}

sub isPrescribedIdExists {                                                       
    my $prescId = shift;                                                        
    $log->info(" Checking duplicate order for prescription_id : $prescId");
    my $row = getLISTestsPrescribedRow($prescId);                                                                                                       
    return defined($row);                                                                                                                                       
}

sub getLISTestsPrescribedRow {
	my $prescId = shift;
    my $sql = qq {
    		SELECT prescribed_id, report_id, conducted FROM tests_prescribed
    		WHERE his_prescribed_id = ?
    	};
    my $dbh = getConnection();
    my $tpRowLIS = $dbh->selectrow_hashref($sql, undef, $prescId);                                                                                                     
    $dbh->disconnect();
    return $tpRowLIS;
}

sub getResultTestDetails {
    my $resultlable_id = shift;
    my $rows = undef;
    $log->info("Searching test id for results_labelid : $resultlable_id");
    my $sql = "select * from test_results_master where resultlabel_id = ?";
    my $dbh = getConnection();
    my $sth = $dbh->prepare($sql);
    $sth->execute($resultlable_id);
    my $rowCount = $sth->rows();
    $log->info("Found number of test : $rowCount");
    $rows = $sth->fetchrow_hashref;
    $sth->finish();
    $dbh->disconnect();
    if ( $rowCount == 0 ) {
        $log->error("Test Id not found for the resultlable id : $resultlable_id");
        die ("Test not found for the resultlabel_id : $resultlable_id");
    }
    $log->info("Test_Id : $rows->{test_id}, Resultlable : $rows->{resultlabel}");
    return $rows;
}

sub updateHisPrescribedId {
    my $prescId = shift;
    my $visitId = shift;
    my $testId = shift;
    my $dbh = getConnection();
    my $sql = qq{ UPDATE tests_prescribed set his_prescribed_id = ? 
                                                        WHERE pat_id = ? AND test_id = ? };
    my $sth = $dbh->prepare($sql);
    $sth->execute($prescId, $visitId, $testId);
    my $count =  $sth->rows();
    $dbh->disconnect();
    return $count;
}

sub updateIsrDateofBirth {
    my $visitId = shift;
    my $isrDob = shift;    
    my $dbh = getConnection();
    my $sql = qq{ UPDATE incoming_sample_registration set isr_dateofbirth = to_date(?, 'YYYYMMDD') 
                                                        WHERE incoming_visit_id = ? };
    my $sth = $dbh->prepare($sql);
    $sth->execute($isrDob, $visitId);
    my $count =  $sth->rows();
    $dbh->disconnect();
    print "count : $count";
    return $count;
}


sub get_age_unit {                                                                                                                                         
    my $dob = shift;                                     ## In YYYYDDMM                                              
    my $sql = "select * from get_patient_age_in(to_date(?,'YYYYMMDD'),null)";   
    my $dbh = getConnection();                                                  
    my $rowhash = $dbh->selectrow_hashref($sql,{},$dob); ## o/p  Y or M         
    my $unit = $rowhash->{get_patient_age_in};                                  
    $dbh->disconnect();                                                         
    return $unit;                                                                                                                                          
}  

sub get_age {                                                                                                                                               
    my $dob = shift;                                     ## In YYYYDDMM                                              
    my $sql = "select * from get_patient_age(to_date(?,'YYYYMMDD'),null)";      
    my $dbh = getConnection();                                                  
    my $rowhash = $dbh->selectrow_hashref($sql,{},$dob); ## o/p  Y or M         
    my $age = $rowhash->{get_patient_age};                                      
    $dbh->disconnect();                                                         
    return $age;                                                                
}   


sub update_age_unit {                                                                                                                                     
    my $age_unit = shift;                                                       
    my $visit_id = shift;                                                       
    my $sql = "UPDATE incoming_sample_registration SET age_unit = ? where incoming_visit_id = ?";  
    my $dbh = getConnection();                                                  
    my $sth = $dbh->prepare($sql);                                              
    $sth->execute($age_unit,$visit_id);                                                   
    $sth->finish();                                                             
    my $rows = $sth->rows();                                                       
    $dbh->disconnect();                                                         
    return $rows;                                                               
}

sub main {
	$log->info("API URL = $apiurl");

	my $interface_ref = undef;

	eval {
		$interface_ref = readConfig();
	};

	if ($@) {
		$log->error("Reading config : Please check database connection ", $@);
		$log->info("$DBI::errstr");
		exit 1;
	}

	if(scalar (keys %$interface_ref) == 0) {
		$log->info("No interface configured for interface_type : $opt_module ");
	}

	foreach my $ifName (keys %$interface_ref) {
		my $row_interface = $interface_ref->{$ifName};
		$log->info("Processing interface name: $row_interface->{interface_name},       interface_type : $row_interface->{interface_type}");	
		processInterface($row_interface);
	}
}

main;
