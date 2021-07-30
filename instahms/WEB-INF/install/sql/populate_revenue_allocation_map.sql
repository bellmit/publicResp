UPDATE revenue_allocation_map SET allocation_department = 'PDEPT' where charge_head
IN ('AANOPE','ANAOPE','ANATOPE','ASUOPE','BBED','BICU','BIDIS','BSTAX','BYBED','COSOPE','CSTAX','DDBED','DDICU',
'EMREG','EQOPE','EQUOTC','GREG','IMPOTC','IPDOC','IPREG','LTAX','LTDIA','MDIE', 'MISOTC','MLREG','NCBED','NCICU',
'OCOTC','OPDOC','OPREG','PCBED','PCICU','PKGPKG','RIPDOC','ROF','ROPDOC',
'RTDIA','SACOPE','SUOPE', 'SERSNP', 'STAX', 'TCAOPE','TCOPE');

UPDATE revenue_allocation_map SET allocation_department = 'ADEPT' where charge_head
IN ('CONMED', 'CONOPE', 'CONOTC', 'INVITE', 'INVRET', 'MARDRG', 'MARPDM', 'MARPKG', 'MEMED', 'OUTDRG', 'PHCMED',
'PHCRET', 'PHMED',  'PHRET');


-- AANOPE        | Asst. Anaesthetist Fees
-- ANAOPE        | Anaesthetist Fees
-- ANATOPE       | Anesthesia type Charge
-- ASUOPE        | Asst. Surgeon Fees
-- BBED          | Bed Charge
-- BICU          | Bed Charge (ICU)
-- BIDIS         | Bill Discounts
-- BSTAX         | Service Charge
-- BYBED         | Bystander Bed Charges
-- CONMED        | Consumables
-- CONOPE        | Consumables
-- CONOTC        | Consumables
-- COSOPE        | Co-op. Surgeon Fees
-- CSTAX         | Claim Service Tax
-- DDBED         | Duty Doctor Charge
-- DDICU         | Duty Doctor Charge (ICU)
-- EMREG         | EMR Charge
-- EQOPE         | Equipment
-- EQUOTC        | Equipment
-- GREG          | General Registration
-- IMPOTC        | Implant Charge
-- INVITE        | Inventory Item
-- INVRET        | Inventory Returns
-- IPDOC         | IP Doctor Visit
-- IPREG         | Admission Charge
-- LTAX          | Luxury Tax
-- LTDIA         | Lab Tests
-- MARDRG        | DRG Base Margin
-- MARPDM        | Per Diem Margin
-- MARPKG        | Package Margin
-- MDIE          | Dietary charges
-- MEMED         | Medicine
-- MISOTC        | Miscellaneous
-- MLREG         | MLC Charge
-- NCBED         | Nurse Charge
-- NCICU         | Nurse Charge (ICU)
-- OCOTC         | Other charge
-- OPDOC         | OP Consultation
-- OPREG         | OP Visit Registration
-- OUTDRG        | DRG Outlier Payment
-- PCBED         | Professional Charge
-- PCICU         | Professional Charge (ICU)
-- PHCMED        | Pharmacy Medicine
-- PHCRET        | Pharmacy Returns
-- PHMED         | Pharmacy Sales
-- PHRET         | Pharmacy Sales Returns
-- PKGPKG        | Package Charges
-- RIPDOC        | IP Follow Up Consultation
-- ROF           | Round Off
-- ROPDOC        | OP Revisit Consultation
-- RTDIA         | Radiology Tests
-- SACOPE        | Surgeon's Fees
-- SERSNP        | Service
-- STAX          | Service Tax
-- SUOPE         | Surgeon Fees
-- TCAOPE        | Additional Theatre Charge
-- TCOPE         | Theater Charge
