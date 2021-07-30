#!/usr/bin/perl
package Insta::HL7Exporter;
use Insta::Util;
use Hl7::Message;
use strict;
use REST::Client;
use JSON;
use File::Basename;

my $client = REST::Client->new();

##################################################
sub new {
##################################################
	my $class = shift;
	my $params = shift;
	
	my $self = {};
	foreach my $key (keys(%$params)) {
	   $self->{$key} = $params->{$key};
	}
	
	$class->init($self);
	bless $self, $class;
}

##################################################
sub init {
##################################################

}

##################################################
sub export {
##################################################
	my $self = shift;
	my $api_params = shift;
	
	my $interfaces = $self->getExportInterface();
	foreach my $ifName (keys %$interfaces)  {
		my $interface = $interfaces->{$ifName};
		if ($interface->{'send_result_as'} eq 'V') {
			$self->process($interface);
		} elsif ($interface->{'send_result_as'} eq 'R') {
			$self->processReport($interface, $api_params);
		}
	}
}

##################################################
sub process {
##################################################
	my $self = shift;
	my $ifDes = shift;
	my $modifiedTestExportItems = undef;

	$self->{'log'}->info("Processing Order : interface_name : $ifDes->{interface_name}, Item Type : $ifDes->{item_type}");
	my $exportItems = $self->{'extractor'}->getExportItems($ifDes);
	
	$self->{'log'}->info("Extracted data from database");
	
	foreach my $exportItem (@$exportItems) {
		$self->{'log'}->info("Processing item :".$exportItem);
		my $msg = $self->{'formatter'}->getMessage($exportItem, $ifDes);
		my $status = $self->{'dispatcher'}->dispatchMessage($exportItem, $msg, $ifDes);
	}

}

sub processReport {
	my $self = shift;
	my $ifDesc = shift;
	my $api_params = shift;
	
	my $res_l =  decode_json($self->loginReq($api_params));
	if(!defined $res_l->{request_handler_key}) {
		$self->{'log'}->error("Please enter correct username and password for API login");
		exit 1;
	}
	$self->getDiagVisitReport($api_params, $res_l, $ifDesc);
		
	$self->{'log'}->info("Login Response : $res_l->{return_message}");
	$self->{'log'}->debug("Login response : " . encode_json($res_l));
}

##################################################
sub loginReq {
##################################################
# RC : Login request should not be hard coded  
# Correct It should not hard code how it will take. Will it option based or it will take from properties file?
	my $self = shift;
	my $api_params = shift;
	my $apiurl = "http://$api_params->{'apihost'}:$api_params->{'apiport'}/$api_params->{'apiname'}";
	my $login = "$apiurl/Customer/Login.do";
	my $loginQueryStr = "_method=login&hospital_name=$api_params->{'schema'}&customer_user_id=$api_params->{'apiuser'}&customer_user_password=$api_params->{'apipwd'}";
	my $url = $login.'?'.$loginQueryStr;
	$self->{'log'}->debug("Login Request : $url");
	$client->GET($url);
	return $client->responseContent();
}

sub getDiagVisitReport {
	my $self = shift;
	my $api_params = shift;
	my $res_l = shift;
	my $ifDes = shift;
	my $apiurl = "http://$api_params->{'apihost'}:$api_params->{'apiport'}/$api_params->{'apiname'}/Customer/DiagnosticModule/DiagPrint.do";
	my $exportReports = $self->{'extractor'}->getExportReports($ifDes);
	
	foreach my $report (@$exportReports) {
		my $loginQueryStr = "_method=getDiagReportsForVisit&visitId=$report->{patient_id}&request_handler_key=$res_l->{request_handler_key}&logoHeader=Y";
		my $url = $apiurl.'?'.$loginQueryStr;
		$client->GET($url);
		my $doc = $client->responseContent();
		my $artistdir=dirname($ifDes->{orders_export_dir});
		my $fileName = $report->{patient_id}.'_'.$report->{report_id}.'.pdf';
		my $dir1 = $artistdir.'/files';
		unless (open(FH, "> $dir1/$fileName")) {
			my $failureMsg = "Folder is not configured properly. Could not create file $ifDes->{orders_export_dir}/me";
			$self->{'log'}->error($failureMsg);
			return 0;
		}
		print FH $doc;
		close FH;
		my $msg = $self->{'formatter'}->getMessage($report, $ifDes);
		my $status = $self->{'dispatcher'}->dispatchMessage($report, $msg, $ifDes);
	}
}

##################################################
sub getExportInterface {
##################################################
	my $self = shift;

	my $interfaceSql = $self->getInterfaceSql();
	
	$self->{'log'}->info("Getting list of module interface sql :");
	$self->{'log'}->debug("\n\n\t\t\t $interfaceSql"."\n");
	
	my $dbh = $self->{"db"};
    
    # get all interfaces configured
    my $interfaces = $dbh->selectall_hashref($interfaceSql,'interface_center');

	if(!%$interfaces){
		$self->{'log'}->info("No any interface configured");
		$self->{'log'}->info("Pls Configure The interface in hl7_lab_interface and hl7_center_interface correctly");
        return undef;
    }
	return $interfaces;
}


##################################################
sub getInterfaceSql {
##################################################
	my $class = shift;
	my $export_type = $class->exportType;
	my $interface_type = $class->name;
	my $sql = qq{SELECT hei.item_type, hli.interface_name, hli.sending_app, hli.interface_type, hci.export_type,
					hli.sending_facility, hli.append_doctor_signature, hli.consolidate_multiple_obx,
					hli.send_result_as, hci.orders_export_dir, hci.orders_export_ip_addr, hci.orders_export_port,
					hci.center_id, (hli.interface_name||'-'||hei.item_type||'-'||hci.center_id)::character varying as interface_center, hei.hl7_lab_interface_id
					FROM hl7_export_items hei 
					JOIN hl7_lab_interfaces hli ON (hei.export_status IN ('N', 'F') AND hei.item_type != 'VISIT' 
						AND hli.interface_type IN ($interface_type) AND hli.status = 'A' AND hli.hl7_lab_interface_id = hei.hl7_lab_interface_id) 
					JOIN hl7_center_interfaces hci ON (hci.export_type IN ($export_type) AND (hci.hl7_lab_interface_id = hli.hl7_lab_interface_id)
						AND hci.center_id = hei.center_id)
				GROUP BY hei.item_type, hli.interface_name, hli.sending_app, hli.interface_type, hci.export_type,
				hli.sending_facility, hli.append_doctor_signature, hli.consolidate_multiple_obx,
				hli.send_result_as, hci.orders_export_dir, hci.orders_export_ip_addr, hci.orders_export_port, hei.hl7_lab_interface_id,
				hci.center_id ORDER BY hli.interface_name, hci.center_id, 
				CASE hei.item_type WHEN  'TESTRESULT' THEN 1 WHEN 'TESTTEMPLATERESULT' THEN 2 WHEN 'TESTRESULTMODIFIED' THEN 3 
				WHEN 'TESTTEMPLATEMODIFIED' THEN 4 END};
	return $sql;		
}

1

