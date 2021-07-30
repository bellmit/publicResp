package SCM::ADJTableMap;

use strict;
use warnings;


my %table_map = (

	XXNMC_HIS_ADJ_OUT_MAIN		=> 'store_adj_main',

	XXNMC_HIS_ADJ_OUT_DETAILS	=> 'store_adj_details',
);

my %table_field_hash = (

		 store_adj_main => {

					ADJ_NUM			=> 'external_adj_no',
					TRANSACTION_DATE	=> 'date_time',
					USER_NAME		=> 'username',
					REASON			=> 'reason',
					STORE_ID		=> 'store_id',
				},

		store_adj_details => {

					ADJ_NUM		=> 'external_adj_no',
					BATCH_NUM	=> 'batch_no',
					TYPE		=> 'type',
					QUANTITY	=> 'qty',
					MEDICINE_ID	=> 'medicine_id',
					DESCRIPTION	=> 'description',
					ITEM_BATCH_ID	=> 'item_batch_id',
					ADJ_DETAIL_NUM	=> 'adj_detail_no',
					ITEM_COST	=> 'cost_value',
				},
);


sub getTableFieldMap {


	my $table_name = $_[0];
	if(!exists  $table_field_hash{$table_name}) {

		print "Table field  map is not available, Please provide the correct table name : $table_name";
		return;
	}
	return %{$table_field_hash{$table_name}};
}

sub getPgTableName{

	my $ora_table_name = $_[0];
	if(!exists $table_map{$ora_table_name}){

		print "Oracle table is not available for postgres, Please provide the correct table name : $ora_table_name";
		return;
	}
	return $table_map{$ora_table_name};
}

