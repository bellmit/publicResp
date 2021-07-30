#!/usr/bin/perl -w
package Insta::DBExtractor;
use strict;

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
sub getExportItems {
##################################################

	my $self = shift;
	my $ifDesc = shift;
	my $sql = undef;
	if (($ifDesc->{item_type} eq 'TESTRESULT') || ($ifDesc->{item_type} eq 'TESTRESULTMODIFIED')) {
		$sql = $self->getExportSql();
	} elsif (($ifDesc->{item_type} eq 'TESTTEMPLATERESULT') || ($ifDesc->{item_type} eq 'TESTTEMPLATEMODIFIED')) {
		$sql = $self->getExportTemplateSql();
	}
	my $dbh = $self->{'db'};

	$self->{'log'}->info("Getting Export Items from database for signedoff reports ");
	$self->{'log'}->debug($sql);
	$self->{'log'}->debug("Interface Name : $ifDesc->{interface_name}");
	my $items = undef;
	if ($dbh && $sql) {
		$items = $dbh->selectall_arrayref($sql, {Slice=>{}}, $ifDesc->{hl7_lab_interface_id});
	}
	return $items;

}

##################################################
sub getExportReports {
##################################################

	my $self = shift;
	my $ifDesc = shift;

	my $sql = $self->getExportReportsSql();
	my $dbh = $self->{'db'};

	$self->{'log'}->info("Getting Export tests from database for signedoff reports ");
	$self->{'log'}->debug($sql);
	$self->{'log'}->debug("Interface Name : $ifDesc->{interface_name}");
	
	my $items = undef;
	if ($dbh && $sql) {
		$items = $dbh->selectall_arrayref($sql, {Slice=>{}}, $ifDesc->{hl7_lab_interface_id});
	}
	return $items;

}

return 1;
