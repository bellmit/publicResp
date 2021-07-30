package com.insta.hms.mdm.integration.medicineroute;

import com.insta.hms.mdm.bulk.BulkDataIntegrationRepository;

import org.springframework.stereotype.Repository;

@Repository
public class MedicineRouteIntegrationRepository extends BulkDataIntegrationRepository<Integer> {

  public MedicineRouteIntegrationRepository() {
    super("medicine_route", "route_id", "integration_medicine_route_id");
  }
  
}
