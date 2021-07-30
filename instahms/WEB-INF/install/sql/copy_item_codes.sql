--
-- Script to copy item code and code type from GENERAL rateplan
-- to wherever the code or code_type is not set in for the items.
--

UPDATE service_org_details s SET item_code = sg.item_code, code_type = sg.code_type
FROM service_org_details sg
WHERE (sg.org_id = 'ORG0001' and sg.service_id = s.service_id)
	AND (s.item_code is null or s.code_type is null);

UPDATE test_org_details t SET item_code = tg.item_code, code_type = tg.code_type
FROM test_org_details tg
WHERE (tg.org_id = 'ORG0001' and tg.test_id = t.test_id)
	AND (t.item_code is null or t.code_type is null);

UPDATE operation_org_details o SET item_code = og.item_code, code_type = og.code_type
FROM operation_org_details og
WHERE (og.org_id = 'ORG0001' and og.operation_id = o.operation_id)
	AND (o.item_code is null or o.code_type is null);

UPDATE anesthesia_type_org_details a SET item_code = ag.item_code, code_type = ag.code_type
FROM anesthesia_type_org_details ag
WHERE (ag.org_id = 'ORG0001' and ag.anesthesia_type_id = a.anesthesia_type_id)
	AND (a.item_code is null or a.code_type is null);

UPDATE consultation_org_details a SET item_code = ag.item_code, code_type = ag.code_type
FROM consultation_org_details ag
WHERE (ag.org_id = 'ORG0001' and ag.consultation_type_id = a.consultation_type_id)
	AND (a.item_code is null or a.code_type is null);

UPDATE dyna_package_org_details a SET item_code = ag.item_code, code_type = ag.code_type
FROM dyna_package_org_details ag
WHERE (ag.org_id = 'ORG0001' and ag.dyna_package_id = a.dyna_package_id)
	AND (a.item_code is null or a.code_type is null);

UPDATE pack_org_details a SET item_code = ag.item_code
FROM pack_org_details ag
WHERE (ag.org_id = 'ORG0001' and ag.package_id = a.package_id)
	AND (a.item_code is null);


