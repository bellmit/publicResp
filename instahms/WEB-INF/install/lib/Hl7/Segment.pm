package Hl7::Segment;

use strict;
use warnings;
use Hl7::Templates;

sub new {
	my $class = shift;
	my $self = {};
	bless $self, $class;

	my ($type, $fields, $msg) = @_;
	return $self->construct($type, $fields, $msg);
}

sub construct {
	my $self = shift;
	my ($type, $fields, $msg) = @_;

	$self->{_TYPE} = $type;
	$self->{_MSG} = $msg;
	# todo: clear other hash values of self

	if (defined($fields)) {
		if (!ref($fields)) {
			return $self->parse($fields);
		} elsif (ref($fields) eq 'ARRAY') {
			return $self->fromArray($fields);
		} elsif (ref($fields) eq 'HASH') {
			return $self->fromHash($fields);
		} else {
			die "Incorrect type of argument: can only handle scalar, array or hash";
		}
	} else {
		# else, leave it empty for addition of fields on the fly
		return $self;
	}
}

sub fromArray {
	my $self = shift;
	my ($fields) = @_;

	my $type = $self->{_TYPE};
	my $template = Hl7::Templates::getSegmentTemplate($self->{_TYPE});

	for (my $i=0; $i<@$fields; $i++) {
		if (defined($fields->[$i])) {
			# get the name of the field from the template. If not found, set it to type-num
			my $name = $template->[$i] || sprintf("%s-%d", $type, $i+1);

			# add the name-value to our self
			$self->{$name} = $fields->[$i];
		}
	}
	return $self;
}

sub fromHash {
	my $self = shift;
	my ($fields) = @_;

	# copy the hash contents to self
	foreach my $key (keys %$fields) {
		$self->{$key} = $fields->{$key};
	}
	return $self;
}

sub parse {
	my $self = shift;
	my ($segStr) = @_;

	die "Segment constructed without message: cannot parse" if not defined($self->{_MSG});

	my @fieldStrs = split('\\'.$self->{_MSG}{_FIELDSEP}, $segStr);
	my $type = shift @fieldStrs;

	# tolerate badly formatted input: using \r\n instead of \r, \n becomes part of type
	$type =~ s/^\n//;
	$self->{_TYPE} = $type;

	return undef unless ($type);

	# get components in each field
	my @fields;
	foreach my $fieldStr (@fieldStrs) {
		my $escCompSep = "\\".$self->{_MSG}{_COMPSEP};
		if ($fieldStr =~ /$escCompSep/) {
			my @components = split($escCompSep, $fieldStr);
			# todo: sub-comps using &. Also, how to support repeat?
			# todo: deal with escapes
			push (@fields, \@components);
		} elsif ($fieldStr eq '') {
			push (@fields, undef);
		} else {
			push (@fields, $fieldStr);
		}
	}

	if ($type eq 'MSH') {
		$fields[0] = $fieldStrs[0];
		unshift @fields, $self->{_MSG}{_FIELDSEP};
	}

	return $self->fromArray(\@fields);
}

#
# Although you can access a value just like $seg->{VAL}, this method is for a safer
# access: if you want single value, it will return the first value if the value is
# an array. If you want an indexed value, pass the index.
#
sub get {
	my $self = shift;
	my ($fieldName, $index) = @_;

	my $value = $self->{$fieldName};
	if (defined($index)) {
		if (ref($value) eq 'ARRAY') {
			return $value->[$index];
		} elsif ($index == 0) {
			return $value;
		} else {
			return undef;
		}
	} else {
		return (ref($value) eq 'ARRAY') ? $value->[0] : $value;
	}
}

sub getGenericTemplate {
	my $self = shift;

	# Go through the keys, find the max num, and generate names like (TYP-n)
	my @names;
	my $maxNum = 1;
	foreach my $name (keys(%$self)) {
		my ($type, $num) = split('-', $name);
		if (defined($num)) {
			$maxNum = $num if ($num > $maxNum);
		}
	}
	for (1..$maxNum) {
		push @names, sprintf("%s-%d", $self->{_TYPE}, $_);
	}
	return \@names;
}


#
# Converts to a string (for sending)
#
sub toString {
	my $self = shift;

	my $names = Hl7::Templates::getSegmentTemplate($self->{_TYPE}) || $self->getGenericTemplate();

	my $out = $self->{_TYPE} . $self->{_MSG}{_FIELDSEP};

	if ($self->{_TYPE} eq 'MSH') {
		# special processing: the first field needs to be skipped
		my @mshNames = @$names;
		shift @mshNames;
		$names = \@mshNames;
	}

	foreach my $name (@$names) {
		my $value = $self->{$name};
		if (defined($value)) {
			if (ref($value) eq 'ARRAY') {
				$out .= join($self->{_MSG}{_COMPSEP}, @$value); 
			} else {
				$out .= $value;
			}
		}

		# we may find repeated values using the name with a ~ suffix, eg, pidList~
		# stands for repeated values apart from the initial (or only) value.
		my $listValues = $self->{"$name~"};
		if (defined($listValues)) {
			# has to be an array
			foreach $value (@$listValues) {
				$out .= $self->{_MSG}{_REPEATSEP};
				if (ref($value) eq 'ARRAY') {
					$out .= join($self->{_MSG}{_COMPSEP}, @$value); 
				} else {
					$out .= $value;
				}
			}
		}
		$out .= $self->{_MSG}{_FIELDSEP};
		# todo: escape special chars
	}
	return $out;
}

sub dump {
	my $self = shift;
	my ($fh, $printNulls) = @_;

	my $names = Hl7::Templates::getSegmentTemplate($self->{_TYPE}) || $self->getGenericTemplate();

	if (!defined($names)) {
		# we don't have a template. Go through the keys, find the max num, and generate
		# generic names (TYP-n)
		$names = [];
		my $maxNum = 1;
		foreach my $name (keys(%$self)) {
			my ($type, $num) = split('-', $name);
			if (defined($num)) {
				$maxNum = $num if ($num > $maxNum);
			}
		}
		for (1..$maxNum) {
			push @$names, sprintf("%s-%d", $self->{_TYPE}, $_);
		}
	}

	if (defined $fh) {
		print $fh $self->{_TYPE}, "\n";
	} else {
		print $self->{_TYPE}, "\n";
	}

	my $i = 0;
	foreach my $name (@$names) {
		my $value = $self->{$name};
		$i++;
		next unless (defined($value) || $printNulls);

		if (defined $fh) {
			printf $fh " %15.15s (%02d): ", $name, $i;
		} else {
			printf " %15.15s (%02d): ", $name, $i;
		}
		if (defined($value)) {
			if (ref($value) eq 'ARRAY') {
				if (defined $fh) {
					print $fh "[", join(',', @$value),"]"; 
				} else {
					print "[", join(',', @$value),"]"; 
				}
			} else {
				if (defined $fh) {
					print $fh $value;
				} else {
					print $value;
				}
			}
		}
		if (defined $fh) {
			print $fh "\n";
		} else {
			print "\n";
		}
		# todo: subcomps
	}

}

1;

