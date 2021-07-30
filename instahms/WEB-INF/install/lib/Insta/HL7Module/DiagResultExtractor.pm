#!/usr/bin/perl -w
package Insta::HL7Module::DiagResultExtractor;
use strict;
use parent ("Insta::DBExtractor");

##################################################
sub getExportSql {
##################################################
	my $self = shift;
	my $sql = qq{
		SELECT v.* from hl7_result_export_view v
		WHERE v.export_status IN ('N','F')
				AND v.hl7_lab_interface_id=? ORDER BY v.export_id ASC};
	
	$self->{'log'}->debug("Getting export SQL: ".$sql);
	
	return $sql;
}

##################################################
sub getExportTemplateSql {
##################################################
	my $self = shift;
	my $sql = qq{
		SELECT v.* from hl7_template_result_export_view v
		WHERE v.hl7_lab_interface_id=? ORDER BY v.export_id ASC};
	
	$self->{'log'}->debug("Getting export Template SQL: ".$sql);
	
	return $sql;
}

##################################################
sub getExportReportsSql {
##################################################
	my $self = shift;
	my $sql = qq{
		SELECT v.* from hl7_report_export_view v
		WHERE v.hl7_lab_interface_id=?};
	
	$self->{'log'}->debug("Getting export tests SQL: ".$sql);
	return $sql;
}

return 1;
