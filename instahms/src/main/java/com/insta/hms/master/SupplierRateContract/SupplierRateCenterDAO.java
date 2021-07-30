package com.insta.hms.master.SupplierRateContract;

import com.insta.hms.master.CenterAssociationDAO;

public class SupplierRateCenterDAO extends CenterAssociationDAO {

	public SupplierRateCenterDAO(){
		super("store_supplier_contracts_center_applicability", "supplier_rate_contract_center_id",	"store_supplier_contracts","supplier_rate_contract_id","store_supplier_contracts_center_applicability_seq");
	}

}
