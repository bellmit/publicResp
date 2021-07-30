#!/usr/bin/perl -w

use strict;
use DBI;
use POSIX qw(strftime);
use File::Basename;
use IO::Socket::INET;
use Getopt::Long;
use Sys::Hostname;
use POSIX ":sys_wait_h";
use IO::Handle;
use lib dirname($0)."/../lib";	# find our Hl7 module. Needed for the next two linesvice
use Hl7::Message;
use Hl7::Segment;
use HTML::Strip;
use Insta::Util;
use Insta::Logger;


# Exports observation results as an ORU message to PowerScribe and iSite interfaces.
# This program needs to be run as a cron-job periodically.

our $opt_db = 'hms';
our $opt_port = 5432;
our $opt_schema ='' ;
our $opt_host = 'localhost';
our $opt_username = 'postgres';
our $opt_password ='';
our $opt_foreground = 0;
our $opt_module ='';


sub usage {
	return "Usage: $0 [OPTIONS] <schema>\n" .
	" OPTIONS: \n" .
	"  -d|--db <database>: which database to connect to (hms)\n" .
	"  -s|--schema <schema>: which schema to operate on (hostname)\n" .
	"  -h|--host <host> : datbase host\n".
	"  -U|--username <username> : database username\n".
	"  -W|--password <password> : database password\n".
	"  -o|--port <port_no> : database port no".
	"  -n|--foreground: print to console instead of logfile\n" ;
}

GetOptions( "port|o=i", "db=s", "schema=s", "host=s", "username|U=s", "password|W=s", "foreground|n!") or die usage();

$opt_db = 'nmc' unless($opt_db);
$opt_host = 'localhost' unless($opt_host);

unless ($opt_schema) {
	$opt_schema = hostname;
	$opt_schema =~ s/instahms-//;
}

my $pidfile = "/var/run/hl7_rad_rpt.$opt_db.$opt_schema.pid";

#
# Standard message header attributes for all ORU exports
#
my %ORM_MSH = (
	sendApp=>'InstaHMS', sendFac=>'Diag',
	msgType=>['ORU','R01'], procId=>'P', ver=>'2.4'
);

#
# Read the config from the database: Do this before daemonizing so that we don't need
# to run if we cannot read the config, or if the config says there's nothing to do.
#
my $interfaces;

my $util = Insta::Util->new();
my $logfile = Insta::Logger->new({level=>$util->getProperty('log.level'),base_name=> (basename($0)),db=>$opt_db,
								schema=>$opt_schema,isConsilePrint=>$opt_foreground});

$logfile->rotateLog(10*1024*1024);

eval {
	readConfig();
};
if ($@) {
	$logfile->info("ERROR reading config: ", $@, "\n");
	exit 1;
}

if (keys(%$interfaces) < 1) {
	$logfile->info("No interfaces configured for exporting results, exiting.\n");
	exit 2;
}

my $exists = open(PIDCHECK, $pidfile);
if ($exists) {
	my $pidcheck = <PIDCHECK>;
	chomp $pidcheck;
	close PIDCHECK;
	if (-e "/proc/$pidcheck") {
		$logfile->info("Already running: $pidcheck\n");
		exit 0;
	} else {
		$logfile->info("Warning: PID file exists but no process: $pidcheck\n");
	}
}

my $nowString = localtime;
updateExportableOrders();

# iterate through the interfaces
foreach my $ifName (keys %$interfaces) {

	my $if = $interfaces->{$ifName};

	# Get the orders to be exported meant for this interface as HL7 messages
	my $orders = getInterfaceResults($if->{hl7_lab_interface_id});
	my $numOrders = @$orders;

	print "Found $numOrders orders for interface = $ifName \n";

	my $conn = IO::Socket::INET->new (
			Proto    => "tcp",
			PeerAddr =>$if->{orders_export_ip_addr},
			PeerPort => $if->{orders_export_port},
			timeout  => 10
		);

	if ($conn || ($if->{export_type} eq 'F') || ($if->{export_type} eq 'B') ) {

		foreach my $order (@$orders) {
			# construct a HL7 message for this order
			if(($order->{center_id} eq $if->{center_id}) || ($if->{center_id} eq 0)){
				print "\n\n Sending message ... ";
        		my $msg = getOrderMessage($order, $if);
        		# Send the HL7 message according to interface preference (and also update
        		# hl7_export_items to indicate done/failed status)
        		if (($if->{export_type} eq 'F') || ($if->{export_type} eq 'B')) {
        			exportMessage($order, $msg, $if->{orders_export_dir}, $if->{export_type} eq 'F');
        		}
        		if (($if->{export_type} eq 'S') || ($if->{export_type} eq 'B')) {
        			# this could fork and do the job in a different process, so don't rely on return value
            		sendMessage($order, $msg,$if->{orders_export_ip_addr},$if->{orders_export_port},$conn);
            		$conn->flush();
        		}
        		# otherwise, the interface wouldn't have been selected
        		print "Done processing order: ", $order->{prescribed_id}, "\n";
			}
		}
       	if (($if->{export_type} eq 'S') || ($if->{export_type} eq 'B')){
       		$conn->close();
       	}
	} else {
       print " Unable to connect ............ \n";
	}
}

sub readConfig {
	my $this = shift;
	my $dbh = getConnection();
	$logfile->info("Reading Hl7 configuration..");
	# get all interfaces configured
	$interfaces = $dbh->selectall_hashref(
				"SELECT hei.item_type, hli.interface_name, hli.sending_app, hli.interface_type, hci.export_type,
					hli.sending_facility, hli.append_doctor_signature, hli.consolidate_multiple_obx,
					hli.send_result_as, hci.orders_export_dir, hci.orders_export_ip_addr, hci.orders_export_port,
					hci.center_id, (hli.interface_name||'-'||'-'||hei.item_type||'-'||hci.center_id)::character varying as interface_center, hei.hl7_lab_interface_id 
					FROM hl7_export_items hei 
					JOIN hl7_lab_interfaces hli ON (hei.export_status IN ('N', 'F') AND hei.item_type = 'TESTTEMPLATERESULT' 
						AND hli.status = 'A' AND hli.hl7_lab_interface_id = hei.hl7_lab_interface_id) 
					JOIN hl7_center_interfaces hci ON (hci.export_type IN ('S') AND (hci.hl7_lab_interface_id = hli.hl7_lab_interface_id)
						AND (hci.center_id = hei.center_id OR 0 = hci.center_id))
				GROUP BY hei.item_type, hli.interface_name, hli.sending_app, hli.interface_type, hci.export_type,
				hli.sending_facility, hli.append_doctor_signature, hli.consolidate_multiple_obx,
				hli.send_result_as, hci.orders_export_dir, hci.orders_export_ip_addr, hci.orders_export_port,
				hci.center_id, hei.hl7_lab_interface_id ORDER BY hli.interface_name, hci.center_id",
		'interface_center');
	$dbh->disconnect();
}

sub getInterfaceResults {
	my ($ifName) = @_;
	my $dbh = getConnection();
	my $results = $dbh->selectall_arrayref(qq{
		SELECT distinct(ei.item_id), ei.item_type,ei.inserted_ts,ei.exported_ts,ei.export_id,ei.interface_name,ei.export_status,ei.export_msg_id,
		ei.export_failure_msg,ei.bill_paid,ei.op_code, tp.pat_id as patient_id, tp.prescribed_id, d.test_name, d.hl7_export_code,
		to_char(tvr.report_date, 'YYYYMMDDhh24miss') as signoff_date,
        d.test_id, to_char(tp.pres_date, 'YYYYMMDDhh24mi') as pres_date, tp.common_order_id,
    to_char(sched_equipment.appointment_time, 'YYYYMMDDhh24mi') as scheduled_time_start, 
    to_char(coalesce(sched_equipment.appointment_time, tp.pres_date), 'YYYYMMDDhh24miss') as start_date_time,
        to_char(sched_equipment.appointment_time + (sched_equipment.duration::text||'mins')::interval, 'YYYYMMDDhh24mi')
            as scheduled_time_end, COALESCE(sched_equipment.hl7_export_code, dept_equipment.hl7_export_code) as equipment_code,
      COALESCE(pd.mr_no,isr.incoming_visit_id) as mr_no, pd.oldmrno,
		COALESCE(pd.last_name, isr.patient_name) as last_name,
		COALESCE(pd.patient_name,'') as patient_name, sm.salutation,
		COALESCE(pd.patient_gender,isr.patient_gender) as patient_gender,
       	to_char(coalesce(pd.dateofbirth, pd.expected_dob, isr.isr_dateofbirth,
		(current_date-(CASE WHEN isr.age_unit = 'Y' THEN (isr.patient_age*365.25)::integer
		WHEN isr.age_unit = 'M' THEN (isr.patient_age*30.43)::integer ELSE isr.patient_age*1 END) )),'YYYYMMDD') as expected_dob,
        COALESCE(pd.patient_address,isr.address,'') as patient_address,
		COALESCE(ci.city_name,'') AS cityname, COALESCE(substring(ci.city_id, 4, 3),'') as city_code,
		COALESCE(st.state_name,'') AS statename, COALESCE(substring(st.state_id, 4, 3),'') as state_code,
		COALESCE(cnm.country_name,'') as country_name, COALESCE(cnm.country_code,'') as country_code,
		COALESCE(pd.patient_phone, isr.phone_no) AS patient_phone,
        to_char(COALESCE(pr.reg_date, isr.date), 'YYYYMMDD') as reg_date,
		pr.doctor, substring(dr.doctor_name,1,50) as doctor_name, pr.bed_type, wn.ward_name,
		pr.reference_docto_id, mrd.icd_code as diagnosis_code, mrd.description as diagnosis,pr.visit_type,
		substring(COALESCE(drs.doctor_name, rd.referal_name),1,50) AS refdoctorname,
        d.conduction_format,
		COALESCE(pr.center_id,isr.center_id)   AS center_id,
		COALESCE(hcm.center_name,hcm_isr.center_name) AS center_name,
		COALESCE(hcm.center_code,hcm_isr.center_code) AS center_code,
		cd.doctor_id as conducting_doctor_id,cd.doctor_name as conducting_doctor_name,
		presc_doc.doctor_id as prescription_doctor_id, presc_doc.doctor_name as prescription_doctor_name, ei.hl7_lab_interface_id,
		presc_doc_dept.dept_name as prescription_department,COALESCE(td.patient_report_file,'NA') as observations,
		COALESCE(tvr.report_addendum,'NA') as addendumreport
	FROM hl7_export_items ei
		JOIN tests_prescribed tp ON (tp.prescribed_id = ei.item_id::integer)
		JOIN diagnostics_export_interface dei ON (tp.test_id = dei.test_id)
		LEFT JOIN tests_conducted tc ON (tc.prescribed_id= tp.prescribed_id)
		JOIN diagnostics d ON (d.test_id = tp.test_id)
		LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id)
		LEFT JOIN mrd_diagnosis mrd ON (mrd.visit_id = tp.pat_id AND diag_type = 'P')
		LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id)
		LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
		LEFT JOIN hospital_center_master hcm_isr ON (hcm_isr.center_id = isr.center_id)
		LEFT JOIN ward_names wn ON (wn.ward_no = pr.ward_id)
		LEFT JOIN doctors drs ON pr.reference_docto_id = drs.doctor_id
		LEFT JOIN referral rd ON pr.reference_docto_id = rd.referal_no
		LEFT JOIN doctors dr ON dr.doctor_id = pr.doctor
		LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
		LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id)
		LEFT JOIN city ci ON pd.patient_city = ci.city_id
		LEFT JOIN state_master st ON pd.patient_state = st.state_id
		LEFT JOIN country_master cnm ON pd.country = cnm.country_id
		LEFT JOIN bill_activity_charge bac ON (bac.activity_id = tp.prescribed_id::text and activity_code = 'DIA')
		LEFT JOIN doctors cd ON (cd.doctor_id = bac.doctor_id)
		LEFT JOIN doctors presc_doc ON (presc_doc.doctor_id = tp.pres_doctor)
		LEFT JOIN doctors conducting_doc ON (conducting_doc.doctor_id = tc.conducted_by)		
		LEFT JOIN department presc_doc_dept ON (presc_doc_dept.dept_id = presc_doc.dept_id)
		LEFT JOIN test_details td ON(td.prescribed_id=tp.prescribed_id)
		JOIN test_visit_reports tvr ON(tp.report_id=tvr.report_id) 
		LEFT JOIN
			(SELECT sch.appointment_id, sch.appointment_time, sch.duration,
				schi.resource_id as eq_id, tem.hl7_export_code
			FROM scheduler_appointments sch
			LEFT JOIN scheduler_appointment_items schi
				ON (sch.appointment_id = schi.appointment_id AND schi.resource_type = 'EQID')
			LEFT JOIN test_equipment_master tem
				ON (tem.eq_id::text = schi.resource_id))
			AS sched_equipment ON (tp.appointment_id = sched_equipment.appointment_id)
		LEFT JOIN
			(SELECT DISTINCT ON (center_id, ddept_id) center_id, ddept_id, hl7_export_code
				FROM test_equipment_master WHERE hl7_export_code != '')
			AS dept_equipment ON (dept_equipment.ddept_id = d.ddept_id AND
                               dept_equipment.center_id = COALESCE(pr.center_id, isr.center_id))
		WHERE ei.bill_paid = 'Y' AND ei.export_status IN ('N')
		AND ei.item_type = 'TESTTEMPLATERESULT' AND ei.op_code in ('N','AD') 
		AND ei.hl7_lab_interface_id=?},
		{Slice=>{}}, $ifName);
	
	$dbh->disconnect();
	return $results;
}


sub updateExportableOrders {
	my $dbh = getConnection();
	$logfile->info("Update bill paid status as Y for hl7_export_items\n");
	#
	# Update the bill_paid (actually ready_for_export) status for existing rows in hl7_export_items.
	# This will take effect in case the bill was paid, or sample collected after the order was placed,
	# for orders already present in hl7_export_items.
	#
	# todo: rename bill_paid to ready_for_export since this considers sample collection as well
	# todo: need updated_ts when we actually set it to ready for export.
	#
	my $sth = $dbh->prepare(qq{
		UPDATE hl7_export_items ei SET bill_paid = 'Y'
		FROM tests_prescribed tp, bill_activity_charge bac, bill_charge bc, bill b, diagnostics d,
		hl7_lab_interfaces hli
		WHERE bill_paid = 'N' AND item_type = 'TESTTEMPLATERESULT'
			AND (b.payment_status = 'P' OR b.bill_type = 'C') AND (d.sample_needed = 'n' OR tp.sflag = '1')
			AND (bac.activity_id = tp.prescribed_id::text AND bac.activity_code = 'DIA')
			AND (bc.charge_id = bac.charge_id)
			AND (b.bill_no = bc.bill_no)
			AND (d.test_id = tp.test_id)
			AND (tp.prescribed_id = ei.item_id::integer)
			AND (hli.hl7_lab_interface_id = ei.hl7_lab_interface_id)
			AND (hli.conducting_doctor_mandatory != 'Y' OR coalesce(bc.payee_doctor_id, bac.doctor_id ,'') != '')
	});

	$sth->execute();
	$dbh->disconnect();
}

sub getOrderMessage {
	my ($order, $interface) = @_;
	
	my $dbh = getConnection();
	my $msgId = ($dbh->selectrow_array(qq{SELECT nextval('hl7_msgid_sequence') AS hl7_msg_id}))[0];
	$dbh->disconnect();

	my $msg = new Hl7::Message(\%ORM_MSH);
	$msg->{MSH}{controlId} = $msgId;
	$msg->{MSH}{recvApp} = $interface->{sending_app};
	$msg->{MSH}{recvFac} = $interface->{sending_facility};

	if ($order->{center_id} != 0) {
		#$msg->{MSH}{sendFac} = $order->{center_name};
		$msg->{MSH}{sendFac} =[$order->{center_code},$order->{center_name}];
	}

	# Add PID
	my ($line1, $line2) = split('\r{0,1}\n', $order->{patient_address});
	my $pid = new Hl7::Segment('PID', {
			sid=> 1,
			pidList=>[$order->{mr_no},'InstaHMS'],
			name=> [$order->{last_name} || '-', $order->{patient_name}, '', '', $order->{salutation}],
			dob=> $order->{expected_dob}, sex=> $order->{patient_gender},
			addr=> [$line1 || '', $line2 || '', $order->{cityname}, $order->{state_code}, '',
				$order->{country_code} || $order->{country_name}],
			phHome => $order->{patient_phone} || ''
		});
	if ($order->{oldmrno}) {
		$pid->{'pidList~'} = [[$order->{oldmrno}, 'Alternate']];
	}
	$msg->addSegment($pid);

	# Add PV1
	my $pv = new Hl7::Segment('PV1', {
			sid=>1,  visitNum => $order->{patient_id},
			admitDate=>$order->{reg_date}, assgndLoc=> [$order->{prescription_department}||'', $order->{ward_name}||'', $order->{bed_type}]
		});

	if($order->{visit_type} eq 'o') {
		$pv->{patientClass} = 'O';
	}else {
		$pv->{patientClass} = 'I';
	}
	if ($order->{refdoctorname}) {
		$pv->{refDoctor} = [$order->{reference_docto_id}, $order->{refdoctorname}];
	}

	if ($order->{doctor_name}) {
		$pv->{admitDoctor} = [$order->{doctor}, $order->{doctor_name}];
	}
	
	if(defined($order->{conducting_doctor_id})) {
		$pv->{attdDoctor} = [$order->{conducting_doctor_id},$order->{conducting_doctor_name}];
	}
	
    $msg->addSegment($pv);

	# Add ORC - placerOrderNum should be separated by dot(.) instead of with ampersand(&).
	my $orderNum = $order->{prescribed_id};
	my $format = $order->{conduction_format};
	my $ctrlCode = 'RE';
	my $resultStatus = 'F';
	if($order->{op_code} eq 'AD') {
		$ctrlCode = 'SC';
		$resultStatus = 'A';
	}
	my $orc = new Hl7::Segment('ORC', {ctrl=>$ctrlCode, placerOrderNum=>[$orderNum, 'InstaHMS']});

	$msg->addSegment($orc);

	# Order cancellations do not have any OBR segments
		# Add OBR
	my $serviceId = $order->{hl7_export_code};
	if (!defined($serviceId) || $serviceId eq '') {
		$serviceId = $order->{test_id};
	}

	my $scheduledStart = $order->{scheduled_time_start} || $order->{pres_date};
	
	my $obr = new Hl7::Segment('OBR', {sid=>1,
			serviceId=>[$serviceId, $order->{test_name},
			'InstaHMS', '', $order->{equipment_code} || ''],
			placerOrderNum=>[$orderNum, 'InstaHMS'],fillerOrderNum=>$orderNum,
			reqTime=>$order->{pres_date}, specimenAction=>'O', specimenTime=>$order->{sample_date},
			reason=>$order->{diagnosis},
			diagServId=>$order->{equipment_code},
			resultSts=>$resultStatus
		});
	if (defined($order->{scheduled_time_start})) {
		$obr->{qtyTiming} = [1,'','',$scheduledStart, $order->{scheduled_time_end}];
		print "scheduled start time : $scheduledStart, $order->{scheduled_time_start}, $order->{pres_date}\n";
	}
	if (defined($order->{start_date_time})) {
		$obr->{scheduledDtTime} = $order->{start_date_time};
	}
	
	$obr->{placer1} = '01';
	$obr->{filler2} = '01';

	if(defined($order->{prescription_doctor_id})) {
		$obr->{ordProvider} = [$order->{prescription_doctor_id},$order->{prescription_doctor_name}];
	}
		
	if(defined($order->{conducting_doctor_id})) {
		$obr->{principalIntrprtr} = [$order->{conducting_doctor_id},$order->{conducting_doctor_name}];
	}
		
	$msg->addSegment($obr);
	
	my @lines="";
    my $rptValue="";
    if($order->{op_code} eq 'AD') {    	
    	$rptValue = $order->{addendumreport};     	
	} else {
		$rptValue = $order->{observations}; 
	}
	$obr->{resultTs} = $order->{signoff_date};
	
	my $strBR="<br/>|<br>|<br />|</br>";
		
    @lines = split /$strBR/,$rptValue;
    
    my $count="";
	$count=0;
	foreach my $line (@lines) { 
	$count++;
	my $str1="\r";
	my $str2=" ";
	my $str3="&nbsp;";
	my $str4="&ndash;";
	my $str5="-";
	my $str6="\n";
		
	my $hs = HTML::Strip->new();
	
	$line=~ s/$str3/$str2/g;
	
	$line=~ s/$str4/$str5/g;
	
	my $clean_text = $hs->parse($line);
	
	$clean_text=~ s/$str1/$str2/g;
	$clean_text=~ s/$str6/$str2/g;
		
	$line=$clean_text;
	$hs->eof;
	
	my $obx = new Hl7::Segment('OBX', {sid=>$count,valueType=>'TX',
			obsId=>[$serviceId, $order->{test_name}],obsSubId=>'',obsValue=>$line,
			units=>'', refRanges=>'', abnormalFlags=>'', probability=>'', natureAbnTest=>'',
			obsResultSts=>'', dateLastNormal=>'', accessChecks=>'', obsTime=>'', producerId=>'',
			responsibleObserver=>'', obsMethod=>'', equipInstance=>'', analTime=>$order->{start_date_time}
			});
	if (defined($order->{scheduled_time_start})) {
		$obx->{qtyTiming} = [1,'','',$scheduledStart, $order->{scheduled_time_end}];
		print "scheduled start time : $scheduledStart, $order->{scheduled_time_start}, $order->{pres_date}\n";
	}
	if (defined($order->{start_date_time})) {
		$obx->{scheduledDtTime} = $order->{start_date_time};
	}
	
	$obx->{placer1} = '01';
	$obx->{filler2} = '01';
	
	if(defined($order->{prescription_doctor_id})) {
		$obx->{ordProvider} = [$order->{prescription_doctor_id},$order->{prescription_doctor_name}];
	}

	$msg->addSegment($obx);
	}
	
	return $msg;
}

sub sendMessage {
	my ($order, $msg,$ipAddr,$port,$conn) = @_;
	my $msgId = $msg->{MSH}{controlId};

	print "Sending message: $msgId\n".$msg->toString()."\n"; $msg->dump();
	
	unless ($conn) {
		my $failureMsg = "ERROR: Unable to connect to host $ipAddr at port $port: $!";
		print $failureMsg, "\n";
		updateExportedFailure($order->{export_id}, $msgId, $failureMsg, 'F');
		return;
	}

	print $conn "\x0B", $msg->toString(), "\x1C\x0D";
	$conn->flush();

	# Read the response:
	$/ = "\x1C\x0D";	# file separator, carriage return
	my $resp;
	my $respMsg;
	eval {
		local $SIG{ALRM} = sub { die "Timed Out" };
		alarm 10;
		$resp = <$conn>;
		alarm 0;
	};
	if ($@) {
		my $failureMsg;
		if ($@ =~ /Timed Out/) {
			$failureMsg = "ERROR: Reading response timed out";
		} else {
			$failureMsg = "ERROR: Failed to read response: $@";
		}
		print $failureMsg, "\n";
		updateExportedFailure($order->{export_id}, $msgId, $failureMsg, 'F');
		return 0;
	}

	eval {
		$respMsg = Hl7::Message->new($resp);
		print "Recd commit message acknowledgement:\n";
		print $respMsg->toString(1);
	};
	if ($@) {
		my $clean = $resp;
		$clean =~ tr/\x0B\x0D\x1C/\n\n./;
		my $failureMsg = "ERROR: Could not parse response as HL7: $clean";
		print $failureMsg, "\n";
		updateExportedFailure($order->{export_id}, $msgId, $failureMsg, 'F');
		return 0;
	}
	
	if ($respMsg->{MSA}{code} ne 'AA') {
		my $failureMsg = "ERROR: Receiver rejected the message due to: " . $respMsg->{MSA}{msgText};
		print $failureMsg, "\n";
		updateExportedFailure($order->{export_id}, $msgId, $failureMsg, 'F');
		return 0;
	}

	print "Sending message succeeded: ", $respMsg->{MSA}{code}, ", updating success.", "\n";
	updateExportedSuccess($order->{export_id}, $msgId);
	return 1;
}


sub exportMessage {
	my ($order, $msg, $dir, $isMainTransport) = @_;
	my $msgId = $msg->{MSH}{controlId};

	my $interfaceName = $order->{interface_name} . "_" . $order->{center_id};
	my $orderNum = $order->{prescribed_id};
	my $format = $order->{conduction_format};

	print "Exporting message $msgId:\n" .$msg->toString()."\n"; $msg->dump();
	unless (open(FH, "> $dir/$interfaceName"."_$orderNum.$order->{op_code}.hl7")) {
		my $failureMsg = "ERROR: Could not open $dir/$interfaceName"."_$orderNum.out: $!";
		print $failureMsg, "\n";
		updateExportedFailure($order->{export_id}, $msgId, $failureMsg, 'F') if ($isMainTransport);
		return;
	}

	print FH $msg->toString(0);
	close FH;
	print " call  exported success\n";
	updateExportedSuccess($order->{export_id}, $msgId) if ($isMainTransport);
}

sub updateExportedSuccess {
	my ($exportId, $msgId) = @_;
    print " from updated exported success current_timestamp  $exportId\n";
	my $dbh = getConnection();
	my $sth = $dbh->prepare(qq{
		UPDATE hl7_export_items set exported_ts=current_timestamp, export_status='S', export_msg_id=?
		WHERE export_id=?});
	$sth->execute($msgId, $exportId);
	$dbh->disconnect();
}

sub updateExportedFailure {
	my ($exportId, $msgId, $failureMsg, $failType) = @_;
    print " from updated exported failure\n";
	my $dbh = getConnection();
	my $sth = $dbh->prepare(qq{
		UPDATE hl7_export_items set export_status=?, export_msg_id=?, export_failure_msg=?
		WHERE export_id=?});
	$sth->execute($failType, $msgId, $failureMsg, $exportId);
	$dbh->disconnect();
}

# Rotate log file if size is greater than specified size.
#
sub rotateLog {
	my $maxSize = shift;
	my $fileSize = 0;

	return if ($opt_foreground);

	if (-f $logfile) {
		$fileSize = (stat($logfile))[7];

		if ($fileSize > $maxSize) {
			# rotate old logs
			for (1..8) {
				my $index = 9 - $_;		# 8..1
				if (-f "$logfile.$index") {
					rename($logfile.".".$index, $logfile.".".($index+1));
				}
			}
			rename($logfile, "$logfile.1");
		}
	}

	# close the stdout (current log)
	close(STDIN); close(STDOUT); close(STDERR);

	# rename current log
	# start the new one
	open(STDIN,  "+>/dev/null");
	open(STDOUT, ">> $logfile");
	open(STDERR, "+>&STDOUT");
	STDOUT->autoflush(1);
}

#
# close when user presses ctrl-C, also remove pidfile
#
sub interrupt {
	print "Received signal, exiting ...\n";
	unlink $pidfile;
	exit 0;
};


sub getConnection {
	my $dbh = DBI->connect("dbi:Pg:dbname=$opt_db;host=$opt_host;port=$opt_port", "$opt_username", "$opt_password",
		{AutoCommit => 1, RaiseError =>1});
	$dbh->do("SET search_path TO $opt_schema");
	$dbh->do("SET application.username TO '_system'");
	return $dbh;
}

sub dateTime {
	my $time_string = scalar localtime();
    my   ($day,$mon,$date,$time,$year) = split /\s+/, $time_string;
    my $date_log = "$year-$mon-$date  $time :: ";
    return $date_log;
}
