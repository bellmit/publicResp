-- liquibase formatted sql
-- changeset manasaparam:renaming-surgery

update scheduler_resource_types set resource_description = 'Surgeon/Doctor' where resource_description = 'Surgeon';
update scheduler_resource_types set resource_description = 'Doctor' where resource_description = 'Paediatrician';
update scheduler_resource_types set resource_description = 'Asst Surgeon/Doctor' where resource_description = 'Asst Surgeon';
update scheduler_resource_types set resource_description = 'Theatre/Room' where resource_description = 'Operation Theatre';
update scheduler_resource_types set resource_description = 'Surgery/Procedure' where resource_description = 'Surgery';

update scheduler_master set description = 'for all surgeries / procedures' where description = 'for all surgeries';
update scheduler_master set description = 'for all rooms / theatres' where description = 'for all theatres';

update chargehead_constants set chargehead_name='Surgeon/Doctor' where chargehead_id='SUOPE';
update chargehead_constants set chargehead_name='Anesthetist' where chargehead_id='ANAOPE';
update chargehead_constants set chargehead_name='Asst. Surgeon / Doctor' where chargehead_id='ASUOPE';
update chargehead_constants set	chargehead_name='Asst. Anesthetist' where chargehead_id='AANOPE';
update chargehead_constants set chargehead_name='Co-op Surgeon/Doctor' where chargehead_id='COSOPE';