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
use Insta::DiagDBExporter;
use Insta::Util;


#
# Exports orders that need to be exported (ie, those that have not been exported yet).
# Supports both socket interface and file export. The choice is made depending on the
# interface to which the export is to be done for each test.
#
# This program needs to be run as a cron-job periodically.
#
# SETUP
# ----
# (a) Insert into hl7_lab_interfaces a list of interfaces that will be accepting our orders
#     See \d+ comments on that table.
# (b) For every test that we need to export orders for, set its hl7_interface_name to one
#     of the interfaces in hl7_lab_interfaces (editable from UI)
# (c) If the interface understands our code, nothing to do. We will export our test_id as the
#     service ID in the export. If not, we'll need to set hl7_interface_code as non-blank. This
#     will be used as the service ID now.
# (d) Setup a cron-job to call this script with appropriate cmd line parameters periodically.
#


our $opt_db = 'hms';
our $opt_port = 5432;
our $opt_schema ='' ;
our $opt_host = 'localhost';
our $opt_username = 'postgres';
our $opt_password ='';
our $opt_foreground = 0;

our $opt_dest_db ="";
our $opt_dest_username = "";
our $opt_dest_password = "";
our $opt_dest_schema = "";


# TODO change this to production parameters


sub usage {
	return "Usage: $0 [OPTIONS] <schema>\n" .
	" OPTIONS: \n" .
	"  -d|--db <database>: which database to connect to (hms)\n" .
	"  -s|--schema <schema>: which schema to operate on (hostname)\n" .
	"  -h|--host <host> : database host\n".
	"  -U|--username <username> : database username\n".
	"  -W|--password <password> : database password\n".
	"  -o|--port <port_no> : database port no".
	"  -n|--foreground: print to console instead of logfile\n" .
	"-ddb|--dest_db : destination database".
	"-ddU|--dest_username : destination db username".
	" -dp|--dest_password : destination db password".
	" -ds|--dest_schema : destination schema";
}

GetOptions( "port|o=i", "db=s", "schema=s", "host=s", "username|U=s", "password|W=s", "foreground|n!", 
		"dest_db|ddb=s", "dest_username|ddU=s", "dest_password|dp=s","dest_schema|ds=s" ) or die usage();

$opt_db = 'hms' unless($opt_db);
$opt_host = '127.0.0.1' unless($opt_host);


unless ($opt_schema) {
	$opt_schema = hostname;
	$opt_schema =~ s/instahms-//;
}

$opt_dest_db = $opt_db unless($opt_dest_db) ;
$opt_dest_username = $opt_username unless($opt_dest_username);
$opt_dest_password = $opt_password unless($opt_dest_password);
$opt_dest_schema = $opt_schema unless($opt_dest_schema);


my $logfile = "/var/log/insta/$opt_db/hl7_db_lab_order.$opt_schema.log";
my $pidfile = "/tmp/hl7_db_lab_order.$opt_db.$opt_schema.pid";

#
# Read the config from the database: Do this before daemonizing so that we don't need
# to run if we cannot read the config, or if the config says there's nothing to do.
#
my $interfaces;

rotateLog(10*1024*1024);

my $util = Insta::Util->new();


$util->isProcess($pidfile);

eval {
	readConfig();
};
if ($@) {
	print "ERROR reading config: ", $@, "\n";
	exit 1;
}

if (keys(%$interfaces) < 1) {
	print "Either interface not configured OR There is no orders to export.\n";
	exit 2;
}

# get the set of orders to be exported into hl7_export_items
# (could also be a trigger ... later)

my $nowString = localtime;
print "\n", $nowString, ": === Starting to look for orders ===\n ";

updateExportableOrders();
# reportNonExportableOrders();

# iterate through the interfaces
foreach my $ifName (keys %$interfaces) {
	eval {
		my $if = $interfaces->{$ifName};
		
		# get the exporter for the export type
		my $exporter = getExporter($if->{export_type});
		
		# Get the orders to be exported meant for this interface as data records
		print "Getting orders for interface_name $if->{interface_name}, Item Type : $if->{item_type} \n";
	
		my $orders = getInterfaceOrders($if->{hl7_lab_interface_id}, $if->{center_id});
		my $numOrders = @$orders;
		print "Total No of orders : $numOrders, interface-center : $ifName\n";
		my $handle = $exporter->open($if->{orders_export_ip_addr}, $if->{orders_export_port});
		my $target = $exporter->init($handle); 
		my $targetod = $exporter->initod($handle); 
		my $targetopd = $exporter->initvisitdetails($handle);
		my $presc_export_id={};
		my $col_presc_export_id={};
		foreach my $order (@$orders) {
			if(defined $order->{sample_no}) {
				# construct a HL7 message for this order
				print "\n******** START_POSTING ************\n";
				print dateTime()."Orders : $numOrders , interface-center : $ifName\n";
				print "Current patient center_id : $order->{center_id}, interface_center : $if->{center_id}, \n";
				if($order->{resultlabel_id}){
					print "prescribed_id : $order->{prescribed_id}, OP_CODE : $order->{op_code}, result_label : $order->{resultlabel_id} \n";
				} else {
					print "prescribed_id : $order->{prescribed_id}, OP_CODE : $order->{op_code} \n";
				}
				print "Posting message for $if->{interface_name}_$if->{center_id} \n";
				my $msgId = getMessageId();
				$order->{export_msg_id} = $msgId;
				# Send the HL7 message according to interface preference (and also update
				# hl7_export_items to indicate done/failed status)
					$exporter->export($target, $order);
					my $coll_prescribed_id = $order->{coll_prescribed_id};
					my $temp = $order->{export_id};
					if($coll_prescribed_id){
						 $col_presc_export_id->{$temp} = '';
					} else {
						 $presc_export_id->{$temp} = '';
					}

				updateExportedSuccess($order->{export_id}, $msgId) if ($if->{export_type} eq 'D');
				print dateTime(). "Done processing order for prescribed_id : ", $order->{prescribed_id}, "\n";
				print "********* END_POSTING ************\n\n";
			}
		}
		
		# inserting patient details	 
			  foreach my $presc_expid (keys %$presc_export_id) {
			  print "\n******** POSTING TO hl7_order_items_main TABLE************\n";	
			  print "Processing order details for export_id : ", $presc_expid,"\n";
			  	my $orderdetails = getpatientdetails($presc_expid);
			  	
			  		foreach my $orderd (@$orderdetails) {		  
			  			$exporter->exportd($targetod, $orderd);		  	  
			 		 }
			  print "\n*********** END_POSTING **************\n";
			  }
		# inserting internallab details
			  foreach my $coll_presc_expid (keys %$col_presc_export_id) {
			  print "\n******** POSTING TO hl7_order_items_main TABLE************\n";	
			  print "Processing order details for collection export_id : ", $coll_presc_expid,"\n";
			  	my $orderdetails = getcentrallabdetails($coll_presc_expid);
			  	
			  		foreach my $orderd (@$orderdetails) {		  
			  			$exporter->exportd($targetod, $orderd);		  	  
			 		 }
			  print "\n*********** END_POSTING **************\n";
			  }		  
			  
		# inserting visit details
		if($if->{item_type} eq 'VISIT'){
			my $demographicorders = getInterfaceVisitOrders($if->{hl7_lab_interface_id}, $if->{center_id});
			my $numVisitOrders = @$demographicorders;
			print "Total No of Visit orders : $numVisitOrders, interface-center : $ifName\n";
				  foreach my $dgorder (@$demographicorders) {
				  my $msgId = getMessageId();
					$dgorder->{export_msg_id} = $msgId;
				  	print "\n******** POSTING TO hl7_visit_details TABLE************\n";	
				  	print "Processing order details for visitid : ", $dgorder->{origin_patient_id},"\n";
				  		  
				  	$exporter->exportvisitdetails($targetopd, $dgorder);
				  			  	  
				 	updateExportedVisitSuccess($dgorder->{origin_patient_id}, $msgId) if ($if->{export_type} eq 'D');
				  	print "\n*********** END_POSTING **************\n";
				 }
		 }	  
		$exporter->close($handle);
	};
	if ($@) {
		print "ERROR while opening connection:\n", $@, "\n";
	}
}

#clean up

$util->pidFileCleanUp($pidfile);

####################
# Subs
####################

sub readConfig {
	my $dbh = getConnection();
	print "Reading Hl7 configuration \n";
	# get all interfaces configured
	$interfaces = $dbh->selectall_hashref(
				"SELECT hei.item_type, hli.interface_name, hli.sending_app, hli.interface_type, hci.export_type,
					hli.sending_facility, hli.append_doctor_signature, hli.consolidate_multiple_obx,
					hli.send_result_as, hci.orders_export_dir, hci.orders_export_ip_addr, hci.orders_export_port,
					hci.center_id, (hli.interface_name||'-'||hei.item_type||'-'||hci.center_id)::character varying as interface_center, hei.hl7_lab_interface_id
					FROM hl7_export_items hei 
					JOIN hl7_lab_interfaces hli ON (hei.export_status IN ('N', 'F') AND hei.item_type IN ('TEST','VISIT') 
						AND hli.status = 'A' AND hli.hl7_lab_interface_id = hei.hl7_lab_interface_id) 
					JOIN hl7_center_interfaces hci ON (hci.export_type IN ('D') AND (hci.hl7_lab_interface_id = hli.hl7_lab_interface_id)
						AND hci.center_id = hei.center_id)
				GROUP BY hei.item_type, hli.interface_name, hli.sending_app, hli.interface_type, hci.export_type,
				hli.sending_facility, hli.append_doctor_signature, hli.consolidate_multiple_obx,
				hli.send_result_as, hci.orders_export_dir, hci.orders_export_ip_addr, hci.orders_export_port,
				hci.center_id, hei.hl7_lab_interface_id ORDER BY hli.interface_name, hci.center_id",
		'interface_center');
	$dbh->disconnect();
}

sub getExporter {
	my ($exportType) = @_;
	if ($exportType eq 'D') {
		my $dbExporter = Insta::DiagDBExporter->new( {
			db=>$opt_dest_db, schema=>$opt_dest_schema, db_username=>$opt_dest_username,db_password=>$opt_dest_password
		});
		return $dbExporter;
	}
}


sub updateExportableOrders {
	my $dbh = getConnection();
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
		WHERE bill_paid = 'N' AND item_type = 'TEST' AND ei.export_status = 'N'
			AND (b.payment_status = 'P' OR b.bill_type = 'C') AND (d.sample_needed = 'n' OR tp.sflag = '1')
			AND (bac.activity_id = tp.prescribed_id::text AND bac.activity_code = 'DIA')
			AND (bc.charge_id = bac.charge_id)
			AND (b.bill_no = bc.bill_no)
			AND (d.test_id = tp.test_id)
			AND (tp.prescribed_id = ei.item_id::integer)
			AND (hli.hl7_lab_interface_id = ei.hl7_lab_interface_id)
			AND ((COALESCE(bc.payee_doctor_id, '') = '' AND hli.conducting_doctor_mandatory = 'N')
				OR (COALESCE(bc.payee_doctor_id, '') <> '' AND hli.conducting_doctor_mandatory = 'Y')
				)
	});
	$sth->execute();
	$sth = $dbh->prepare(qq{
		UPDATE hl7_export_items ei SET bill_paid = 'N'
		FROM tests_prescribed tp, bill_activity_charge bac, bill_charge bc, bill b, diagnostics d,
		hl7_lab_interfaces hli
		WHERE bill_paid = 'Y' AND item_type = 'TEST' AND ei.export_status = 'N'
			AND (tp.sflag != '1' AND d.sample_needed = 'y')
			AND (bac.activity_id = tp.prescribed_id::text AND bac.activity_code = 'DIA')
			AND (bc.charge_id = bac.charge_id)
			AND (b.bill_no = bc.bill_no)
			AND (d.test_id = tp.test_id)
			AND (tp.prescribed_id = ei.item_id::integer)
			AND (hli.hl7_lab_interface_id = ei.hl7_lab_interface_id)
			AND ((COALESCE(bc.payee_doctor_id, '') = '' AND hli.conducting_doctor_mandatory = 'N')
				OR (COALESCE(bc.payee_doctor_id, '') <> '' AND hli.conducting_doctor_mandatory = 'Y')
				)
	});
	$sth->execute();
	$dbh->disconnect();
}


##################################################
sub reportNonExportableOrders {
##################################################
	my $dbh = getConnection();
	my $nonExportableOrders = $dbh->selectall_arrayref(qq{SELECT item_id, interface_name FROM hl7_export_items WHERE bill_paid = 'N' AND item_type = 'TEST' });
	if (@$nonExportableOrders >0) {
		print ("Reporting non-exportable orders \n");
		print ("The following items are not exported because one of the following reasons: 
			Sample not collected, Bill not yet paid OR Conducting doctor setting mismatch between item and interface level.\n");
	}
	foreach my $nonExportableOrder (@$nonExportableOrders) {
		my ($item_id, $interface_name) = @$nonExportableOrder;
		print ("Item id $item_id For the interface $interface_name \n");
	}	

}


sub getInterfaceOrders {
	my ($ifName, $centerId) = @_;
	my $dbh = getConnection();
	my $orders = $dbh->selectall_arrayref(qq{
        SELECT * FROM (
            SELECT
                ei.*, tp.pat_id as patient_id, tp.prescribed_id, d.test_name, d.hl7_export_code, d.test_id,
                to_char(tp.pres_date, 'YYYYMMDDhh24mi') as pres_date, tp.common_order_id,
                to_char(sched_equipment.appointment_time, 'YYYYMMDDhh24mi') as scheduled_time_start, 
                to_char(coalesce(sched_equipment.appointment_time, tp.pres_date), 'YYYYMMDDhh24miss') as start_date_time,
                to_char(sched_equipment.appointment_time + (sched_equipment.duration::text||'mins')::interval, 'YYYYMMDDhh24mi')
                as scheduled_time_end, COALESCE(sched_equipment.hl7_export_code, dept_equipment.hl7_export_code) as equipment_code,
                COALESCE(sc.coll_sample_no, sc.sample_sno) as sample_sno, to_char(sc.sample_date, 'YYYYMMDDhh24missms') as sample_date,
                pd.mr_no, pd.oldmrno, COALESCE(pd.last_name,'') as last_name, pd.patient_name, sm.salutation,
                pd.patient_gender, to_char(coalesce(pd.dateofbirth,pd.expected_dob), 'YYYYMMDD') as expected_dob,
                COALESCE(pd.patient_address,'') as patient_address, COALESCE(ci.city_name,'') AS cityname,
                COALESCE(substring(ci.city_id, 4, 3),'') as city_code, COALESCE(st.state_name,'') AS statename,
                COALESCE(substring(st.state_id, 4, 3),'') as state_code, COALESCE(cnm.country_name,'') as country_name,
                COALESCE(cnm.country_code,'') as country_code, pd.patient_phone, to_char(pr.reg_date, 'YYYYMMDD') as reg_date,
                pr.doctor, substring(dr.doctor_name,1,50) as doctor_name, pr.bed_type, wn.ward_name, pr.reference_docto_id,
                mrd.icd_code as diagnosis_code, mrd.description as diagnosis, pr.visit_type, NULL AS refdoctorname,
                d.conduction_format, tm.resultlabel_id, tm.resultlabel, pr.center_id, hcm.center_name,
                doctors.doctor_id as conducting_doctor_id,doctors.doctor_name as conducting_doctor_name,
                presc_doc.doctor_id as prescription_doctor_id, presc_doc.doctor_name as prescription_doctor_name, 
                presc_doc_dept.dept_name as prescription_department, tp.coll_prescribed_id, ei.out_house_sample,
                COALESCE(pd.middle_name,'') as middle_name, rd.referal_mobileno, dr.doctor_mobile, 
                presc_doc.doctor_mobile as presc_doctor_mobile,tp.sample_no
            FROM hl7_export_items ei
            JOIN tests_prescribed tp ON (tp.prescribed_id = ei.item_id::integer)
            JOIN diagnostics d ON (d.test_id = tp.test_id)
            JOIN patient_registration pr ON (pr.patient_id = tp.pat_id)
            JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
            LEFT JOIN test_results_master tm ON (tm.test_id = tp.test_id)
            LEFT JOIN test_results_center trc on (tm.resultlabel_id = trc.resultlabel_id)
            LEFT JOIN sample_collection sc ON (sc.sample_collection_id = tp.sample_collection_id)
            LEFT JOIN mrd_diagnosis mrd ON (mrd.visit_id = tp.pat_id AND diag_type = 'P')
            LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
            LEFT JOIN hospital_center_master hcm_trc on (trc.center_id = hcm_trc.center_id)
            LEFT JOIN ward_names wn ON (wn.ward_no = pr.ward_id)
            LEFT JOIN doctors drs ON pr.reference_docto_id = drs.doctor_id
            LEFT JOIN referral rd ON pr.reference_docto_id = rd.referal_no
            LEFT JOIN doctors dr ON dr.doctor_id = pr.doctor
            LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id)
            LEFT JOIN city ci ON pd.patient_city = ci.city_id
            LEFT JOIN state_master st ON pd.patient_state = st.state_id
            LEFT JOIN country_master cnm ON pd.country = cnm.country_id
            LEFT JOIN bill_activity_charge bac ON (bac.activity_id = tp.prescribed_id::text and activity_code = 'DIA')
            LEFT JOIN doctors ON (doctors.doctor_id = bac.doctor_id)
            LEFT JOIN doctors presc_doc ON (presc_doc.doctor_id = tp.pres_doctor)
            LEFT JOIN department presc_doc_dept ON (presc_doc_dept.dept_id = presc_doc.dept_id)
            LEFT JOIN (
                SELECT sch.appointment_id, sch.appointment_time, sch.duration, schi.resource_id as eq_id,
                    tem.hl7_export_code
                FROM scheduler_appointments sch
                LEFT JOIN scheduler_appointment_items schi ON (sch.appointment_id = schi.appointment_id AND schi.resource_type = 'EQID')
                LEFT JOIN test_equipment_master tem ON (tem.eq_id::text = schi.resource_id))
                AS sched_equipment ON (tp.appointment_id = sched_equipment.appointment_id)
            LEFT JOIN (
                SELECT DISTINCT ON (center_id, ddept_id) center_id, ddept_id, hl7_export_code 
                FROM test_equipment_master WHERE hl7_export_code != '')
                AS dept_equipment ON (dept_equipment.ddept_id = d.ddept_id AND dept_equipment.center_id = pr.center_id)
            WHERE 
                ei.bill_paid = 'Y' AND ei.export_status IN ('N','F') AND ei.item_type = 'TEST' 
                AND (trc.center_id is null or trc.center_id='0' or trc.center_id = pr.center_id ) 
                AND (trc.status = 'A' or trc.status is null)
                AND (sc.sample_receive_status = 'R' OR sc.sample_receive_status IS NULL)
                AND ei.hl7_lab_interface_id=? AND (0 = ? OR pr.center_id = ?)
            UNION ALL
            SELECT
                ei.*, tp.pat_id as patient_id, tp.prescribed_id, d.test_name, d.hl7_export_code, d.test_id,
                to_char(tp.pres_date, 'YYYYMMDDhh24mi') as pres_date, tp.common_order_id,
                to_char(sched_equipment.appointment_time, 'YYYYMMDDhh24mi') as scheduled_time_start, 
                to_char(coalesce(sched_equipment.appointment_time, tp.pres_date), 'YYYYMMDDhh24miss') as start_date_time,
                to_char(sched_equipment.appointment_time + (sched_equipment.duration::text||'mins')::interval, 'YYYYMMDDhh24mi') as scheduled_time_end,
                COALESCE(sched_equipment.hl7_export_code, dept_equipment.hl7_export_code) as equipment_code,
                COALESCE(sc.coll_sample_no, sc.sample_sno) as sample_sno, to_char(sc.sample_date, 'YYYYMMDDhh24missms') as sample_date,
                isr.incoming_visit_id as mr_no, NULL as oldmrno, '' as last_name, isr.patient_name, NULL as salutation, isr.patient_gender,
                to_char(coalesce(isr.isr_dateofbirth, (current_date-(CASE WHEN isr.age_unit = 'Y' THEN (isr.patient_age*365.25)::integer 
                    WHEN isr.age_unit = 'M' THEN (isr.patient_age*30.43)::integer ELSE isr.patient_age*1 END) )), 'YYYYMMDD') as expected_dob,
                COALESCE(isr.address,'') as patient_address, '' AS cityname, '' as city_code, '' AS statename,
                '' as state_code, '' as country_name, '' as country_code, isr.phone_no AS patient_phone,
                to_char(isr.date, 'YYYYMMDD') as reg_date, NULL as doctor, NULL as doctor_name, NULL as bed_type,
                NULL as ward_name, NULL as reference_docto_id, mrd.icd_code as diagnosis_code,
                mrd.description as diagnosis, 't' as visit_type, NULL AS refdoctorname, d.conduction_format,
                tm.resultlabel_id, tm.resultlabel, isr.center_id, hcm_isr.center_name,
                doctors.doctor_id as conducting_doctor_id, doctors.doctor_name as conducting_doctor_name,
                presc_doc.doctor_id as prescription_doctor_id, presc_doc.doctor_name as prescription_doctor_name, 
                presc_doc_dept.dept_name as prescription_department, tp.coll_prescribed_id, ei.out_house_sample,
                '' as middle_name, NULL as referal_mobileno, NULL as doctor_mobile, presc_doc.doctor_mobile as presc_doctor_mobile,tp.sample_no
            FROM hl7_export_items ei
            JOIN tests_prescribed tp ON (tp.prescribed_id = ei.item_id::integer)
            JOIN diagnostics d ON (d.test_id = tp.test_id)
            JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id)
            LEFT JOIN test_results_master tm ON (tm.test_id = tp.test_id)
            LEFT JOIN test_results_center trc on (tm.resultlabel_id = trc.resultlabel_id)
            LEFT JOIN sample_collection sc ON (sc.sample_collection_id = tp.sample_collection_id)
            LEFT JOIN mrd_diagnosis mrd ON (mrd.visit_id = tp.pat_id AND diag_type = 'P')
            LEFT JOIN hospital_center_master hcm_isr ON (hcm_isr.center_id = isr.center_id)
            LEFT JOIN hospital_center_master hcm_trc on (trc.center_id = hcm_trc.center_id)
            LEFT JOIN bill_activity_charge bac ON (bac.activity_id = tp.prescribed_id::text and activity_code = 'DIA')
            LEFT JOIN doctors ON (doctors.doctor_id = bac.doctor_id)
            LEFT JOIN doctors presc_doc ON (presc_doc.doctor_id = tp.pres_doctor)
            LEFT JOIN department presc_doc_dept ON (presc_doc_dept.dept_id = presc_doc.dept_id)
            LEFT JOIN (
                SELECT sch.appointment_id, sch.appointment_time, sch.duration, schi.resource_id as eq_id, tem.hl7_export_code
                FROM scheduler_appointments sch
                LEFT JOIN scheduler_appointment_items schi ON (sch.appointment_id = schi.appointment_id AND schi.resource_type = 'EQID')
                LEFT JOIN test_equipment_master tem ON (tem.eq_id::text = schi.resource_id))
                AS sched_equipment ON (tp.appointment_id = sched_equipment.appointment_id)
            LEFT JOIN (
                SELECT DISTINCT ON (center_id, ddept_id) center_id, ddept_id, hl7_export_code
                FROM test_equipment_master WHERE hl7_export_code != '')
            AS dept_equipment ON (dept_equipment.ddept_id = d.ddept_id AND dept_equipment.center_id = isr.center_id)
            WHERE 
                ei.bill_paid = 'Y' AND ei.export_status IN ('N','F') AND ei.item_type = 'TEST' 
                AND (trc.center_id is null or trc.center_id='0' or trc.center_id = isr.center_id )
                AND (trc.status = 'A' or trc.status is null)
                AND (sc.sample_receive_status = 'R' OR sc.sample_receive_status IS NULL)  
                AND ei.hl7_lab_interface_id=? AND (0 = ? OR isr.center_id = ?)
        ) foo order by export_id},
		{Slice=>{}}, $ifName, $centerId, $centerId, $ifName, $centerId, $centerId);

	$dbh->disconnect();
	return $orders;
}

sub getMessageId {
	my $dbh = getConnection();
	my $msgId = ($dbh->selectrow_array(qq{SELECT nextval('hl7_msgid_sequence') AS hl7_msg_id}))[0];
	$dbh->disconnect();
	return $msgId;
}

sub updateExportedSuccess {
	my ($exportId, $msgId) = @_;

	my $dbh = getConnection();
	my $sth = $dbh->prepare(qq{
		UPDATE hl7_export_items set exported_ts=current_timestamp, export_status='S', export_msg_id=?
		WHERE export_id=?});
	$sth->execute($msgId, $exportId);
	$dbh->disconnect();
}
sub updateExportedVisitSuccess {
	my ($exportId, $msgId) = @_;

	my $dbh = getConnection();
	my $sth = $dbh->prepare(qq{
		UPDATE hl7_export_items set exported_ts=current_timestamp, export_status='S', export_msg_id=?
		WHERE export_status IN ('N','F') AND item_id=?});
	$sth->execute($msgId, $exportId);
	$dbh->disconnect();
}

sub updateExportedFailure {
	my ($exportId, $msgId, $failureMsg, $failType) = @_;

	my $dbh = getConnection();
	my $sth = $dbh->prepare(qq{
		UPDATE hl7_export_items set export_status=?, export_msg_id=?, export_failure_msg=?
		WHERE export_id=?});
	$sth->execute($failType, $msgId, $failureMsg, $exportId);
	$dbh->disconnect();
}

sub getConnection {
	my $dbh = DBI->connect("dbi:Pg:dbname=$opt_db;host=$opt_host;port=$opt_port", "$opt_username", "$opt_password",
		{AutoCommit => 1, RaiseError =>1});
	$dbh->do("SET search_path TO $opt_schema");
	$dbh->do("SET application.username TO '_system'");
	return $dbh;
}

#
# close when user presses ctrl-C, also remove pidfile
#
sub interrupt {
	print "Received signal, exiting ...\n";
	unlink $pidfile;
	exit 0;
};

#
# Rotate log file if size is greater than specified size.
#
sub rotateLog {
	my $maxSize = shift;
	my $fileSize = 0;

	return if ($opt_foreground);
	print "logfile : $logfile \n";
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

sub dateTime {
	my $time_string = scalar localtime();
    my   ($day,$mon,$date,$time,$year) = split /\s+/, $time_string;
    my $date_log = "$year-$mon-$date  $time :: ";
    return $date_log;
}

sub getcentrallabdetails{
	my ($coll_presc_expid) = @_;
	my $dbh = getConnection();
	my $orderdetails = $dbh->selectall_arrayref(qq{
	SELECT tp.pat_id as patient_id, tp.prescribed_id,ei.export_id, ei.inserted_ts, pm.package_name,tpam.tpa_name,
		 ipm.plan_name, ppd.member_id as employee_id,user_remarks as order_remarks, tpcol.clinical_notes as additional_test_info,
		 coalesce(isr.center_id, pr.center_id) as orig_center_id, tpcol.priority, st.sample_type, pd.email_id,
		 coalesce(hcm.center_name, hcmi.center_name) as origin_center_name, coalesce(isr.incoming_visit_id, pr.patient_id) as origin_patient_id,
		 om.oh_name as out_house_name,pm.package_category_id,tpam.tpa_id,st.sample_type_id	
	FROM hl7_export_items ei
		JOIN tests_prescribed tp ON (tp.prescribed_id = ei.item_id::integer)
		JOIN tests_prescribed tpcol ON (tpcol.prescribed_id = tp.coll_prescribed_id)
		JOIN bill_activity_charge bac ON (bac.activity_id =tpcol.prescribed_id::text AND bac.activity_code = 'DIA')
		JOIN bill_charge bc ON (bc.charge_id = bac.charge_id)
		JOIN sample_collection sc ON (tp.sample_collection_id = sc.sample_collection_id)
		LEFT JOIN patient_details pd ON (pd.mr_no = tp.mr_no)
		LEFT JOIN package_prescribed pp ON (pp.prescription_id=tpcol.package_ref)
		LEFT JOIN packages pm ON (pm.package_id=pp.package_id)
		LEFT JOIN patient_insurance_plans ppip ON( ppip.patient_id = tpcol.pat_id AND ppip.priority = 1)
		LEFT JOIN insurance_plan_main ipm ON (ipm.plan_id = ppip.plan_id)
		LEFT JOIN tpa_master tpam ON (tpam.tpa_id = ppip.sponsor_id)
		LEFT JOIN patient_policy_details ppd ON (ppd.mr_no = ppip.mr_no and ppd.status = 'A' AND ppip.patient_policy_id = ppd.patient_policy_id and ppip.plan_id = ppd.plan_id)
		LEFT JOIN patient_registration pr ON (pr.patient_id = tpcol.pat_id)
		LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tpcol.pat_id)
		LEFT JOIN sample_type st on (sc.sample_type_id=st.sample_type_id)
		LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
		LEFT JOIN hospital_center_master hcmi ON (hcm.center_id = isr.center_id)
		LEFT JOIN diag_outsource_master dom ON (dom.outsource_dest_id = tp.outsource_dest_id)
		LEFT JOIN outhouse_master om ON(om.oh_id = dom.outsource_dest)
	Where  ei.item_type = 'TEST' AND ei.export_id = ?},{Slice=>{}}, $coll_presc_expid);

	$dbh->disconnect();
	return $orderdetails;
}
sub getpatientdetails{
	my ($presc_expid) = @_;
	my $dbh = getConnection();
	my $orderdetails = $dbh->selectall_arrayref(qq{
	SELECT  tp.pat_id as patient_id, tp.prescribed_id,ei.export_id, ei.inserted_ts, pm.package_name, tpam.tpa_name, ipm.plan_name,
        ppd.member_id as employee_id, user_remarks as order_remarks, tp.clinical_notes as additional_test_info,
        coalesce(isr.center_id, pr.center_id) as orig_center_id, tp.priority, st.sample_type, pd.email_id,
        coalesce(hcm.center_name, hcmi.center_name) as origin_center_name, tp.pat_id as origin_patient_id,
            om.oh_name as out_house_name,pm.package_category_id,tpam.tpa_id,st.sample_type_id

	  FROM hl7_export_items ei   
	    JOIN tests_prescribed tp ON (tp.prescribed_id = ei.item_id::integer)
	    JOIN bill_activity_charge bac ON (bac.activity_id =tp.prescribed_id::text AND bac.activity_code = 'DIA')
	    JOIN bill_charge bc ON (bc.charge_id = bac.charge_id)
	    JOIN sample_collection sc ON (tp.sample_collection_id = sc.sample_collection_id)
	    LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id)
	    LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
	    LEFT JOIN package_prescribed pp ON (pp.prescription_id=tp.package_ref)
	    LEFT JOIN packages pm ON (pm.package_id=pp.package_id)
	    LEFT JOIN sample_type st on (sc.sample_type_id=st.sample_type_id)
	    LEFT JOIN patient_insurance_plans ppip ON( ppip.patient_id = pr.patient_id AND ppip.priority = 1)
	    LEFT JOIN insurance_plan_main ipm ON (ipm.plan_id = ppip.plan_id)
	    LEFT JOIN tpa_master tpam ON (tpam.tpa_id = ppip.sponsor_id)
	    LEFT JOIN patient_policy_details ppd ON (ppd.mr_no = ppip.mr_no and ppd.status = 'A' AND ppip.patient_policy_id = ppd.patient_policy_id and ppip.plan_id = ppd.plan_id)
	    LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id)
	    LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
	    LEFT JOIN hospital_center_master hcmi ON (hcm.center_id = isr.center_id)
	    LEFT JOIN diag_outsource_master dom ON (dom.outsource_dest_id = tp.outsource_dest_id)
	    LEFT JOIN outhouse_master om ON(om.oh_id = dom.outsource_dest)
	  Where ei.bill_paid = 'Y' AND ei.item_type = 'TEST' AND ei.export_id = ?},{Slice=>{}}, $presc_expid);
	
	$dbh->disconnect();
	return $orderdetails;
}

sub getInterfaceVisitOrders {
	my ($ifName, $centerId) = @_;
	my $dbh = getConnection();
	my $demoGraphicorders = $dbh->selectall_arrayref(qq{
	SELECT  pr.mr_no,ei.item_id as origin_patient_id, max(ei.inserted_ts) as inserted_ts, tpam.tpa_name,
			ipm.plan_name, ppd.member_id as employee_id, ei.export_msg_id,
			ei.op_code as patient_details_status  	   		    

	FROM hl7_export_items ei
		JOIN patient_registration pr ON (pr.patient_id = ei.item_id)
		LEFT JOIN patient_insurance_plans pip ON (pip.patient_id = pr.patient_id) 
		LEFT JOIN insurance_plan_main ipm ON (ipm.plan_id = pip.plan_id)
		LEFT JOIN tpa_master tpam ON (tpam.tpa_id = pip.sponsor_id)
		LEFT JOIN patient_insurance_plans ppip ON( ppip.patient_id = pr.patient_id AND ppip.priority = 1)
		LEFT JOIN patient_policy_details ppd ON (ppd.mr_no = ppip.mr_no and ppd.status = 'A' AND ppip.patient_policy_id = ppd.patient_policy_id and ppip.plan_id = ppd.plan_id)
	Where ei.bill_paid = 'Y' AND export_status IN ('N','F') AND item_type = 'VISIT'
			AND hl7_lab_interface_id=? AND ei.center_id = ? GROUP BY pr.mr_no,ei.item_id, tpam.tpa_name,
			ipm.plan_name, ppd.member_id, ei.export_msg_id, ei.op_code },
		{Slice=>{}}, $ifName, $centerId);

	$dbh->disconnect();
	return $demoGraphicorders;
}
