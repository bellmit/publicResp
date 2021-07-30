#!/usr/bin/perl -w
package Insta::Util;
use Config::Properties;
use POSIX qw(strftime);
use File::Basename;

use lib dirname($0) . "/../lib";

#use Insta::Order::LabOrder;
#use Insta::Order::ConsultationOrder;

##################################################
sub new {
##################################################

    my $class  = shift;
    my $params = shift;
    my $self   = {};
    foreach my $key ( keys(%$params) ) {
        $self->{$key} = $params->{$key};
    }

    bless $self, $class;
}
##################################################
#This method will return the value from the properties
#@Param = key
#@Return = value
##################################################
sub getProperty {
##################################################

    my $self       = shift;
    my $key        = shift;
    my $properties = getProperties();
    return $properties->getProperty($key);
}

##################################################
sub getProperties {
##################################################
    my $file = dirname($0) . "/../lib/Insta/Config.properties";
    open my $fh, "<", "$file"
      or die "Unable to open [../lib/Insta/Config.properties] file \n";
    my $properties = Config::Properties->new();
    $properties->load($fh);
    return $properties;
}

##################################################
sub getDateTimeInYmdHMS {
##################################################
    return strftime( '%Y-%m-%d %H:%M:%S', localtime ) . ' ';
}

sub getModuleLocation {
    my $class = shift;
    my $key   = shift;

    $href = {
        "LIS"          => Insta::HL7Module::LIS,
        "RIS"          => Insta::HL7Module::LIS,
        "CONSULTATION" => Insta::HL7Module::Consultation,
        "SERVICE"      => Insta::HL7Module::Service
    };

    if ( not defined $href->{$key} ) {
        print $class->getDateTimeInYmdHMS(), __PACKAGE__,
          " ERROR Requested module $key doesn't support\n";
        exit(0);
    }

    return $href->{$key};
}

sub getOrderModules {
    return ( "LabOrder", "ConsultationOrder" );
}

sub getImportModules {

    #DIAGImport is for Laboratory
    #RADImport is for Radiology
    return ( "DIAGImport", "RADImport" );

}

=pod

=head1 

	isProcess is a method which will check whether process running or not.

	This method required process file as an argument, it will open that file in read mode if exits, if not then it will open in
	
	write mode and add the process id in file.


=cut

sub isProcess {
    my $self    = shift;
    my $pidfile = shift;

    my $exists = open( PIDCHECK, $pidfile );

    if ($exists) {
        print $self->getDateTimeInYmdHMS(), __PACKAGE__,
          "INFO PID file exists, file loc : $pidfile\n";
        my $pidcheck = <PIDCHECK>;
        chomp $pidcheck;
        close PIDCHECK;

        if ( -e "/proc/$pidcheck" ) {
            print $self->getDateTimeInYmdHMS(), __PACKAGE__,
              " ERROR ::  \tProcess Already running [ PID : $pidcheck ]\n";
            exit 0;
        }
        else {
            print $self->getDateTimeInYmdHMS(), __PACKAGE__,
" WARNING :: PID file exists but no process for pid : $pidcheck Current pid : $$ \n";
        }
    }

#	print $self->getDateTimeInYmdHMS(), __PACKAGE__ ," INFO :: Creating process file location : $pidfile , PID : $$\n";
    print $self->getDateTimeInYmdHMS(), __PACKAGE__,
      " INFO :: Creating pid file, current PID : $$\n";
    open( T, "> $pidfile" );
    print T $$;
    close T;
}

sub pidFileCleanUp {

    my $self    = shift;
    my $pidfile = shift;

#	print $self->getDateTimeInYmdHMS(), __PACKAGE__ ," INFO ::  Deleting pid file : $pidfile\n";
    if ( defined $pidfile and -e $pidfile ) {
        unlink($pidfile);

        print $self->getDateTimeInYmdHMS(), __PACKAGE__,
          " INFO :: Processing DONE , Deleted PID file, current PID : $$\n";

    }
    else {
        print $self->getDateTimeInYmdHMS(), __PACKAGE__,
          " INFO :: Processing DONE , Deleting PID file",
          " PID file doesn't exists : $$\n";
    }
    exit 0;
}

sub pidFileCleanWithoutExit {

    my $self    = shift;
    my $pidfile = shift;

#	print $self->getDateTimeInYmdHMS(), __PACKAGE__ ," INFO ::  Deleting pid file : $pidfile\n";
    if ( defined $pidfile and -e $pidfile ) {
        unlink($pidfile);

        print $self->getDateTimeInYmdHMS(), __PACKAGE__,
          " INFO :: Processing DONE , Deleted PID file, current PID : $$\n";

    }
    else {
        print $self->getDateTimeInYmdHMS(), __PACKAGE__,
          " INFO :: Processing DONE , Deleting PID file",
          " PID file doesn't exists : $$\n";
    }
}

1;
