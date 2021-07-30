-- liquibase formatted sql
-- changeset AdeshAtole:coder-email-notification


insert into message_events values ('coder_review_update', 'Coder Review Update', 'Event triggered when any coder review is updated or created. For triggering email.');

insert into message_types values('email_on_coder_review_update', 'Coder Review Update' ,'Email to stakeholders on coder review update',
    null, null,'Coder Review Update', 'Dear ${recipient_name},<br/><br />
<#list activity_data as activity>
 <#switch activity.activity>
  <#case "CREATE_REVIEW">
   A new review has been created by ${change_by}.
   <ul>
    <li>Reason : ${activity.title}
    <li>Review Type : ${activity.message_type}
    <li>Details : ${activity.body}
    <li>Role : ${activity.role}
    <li>Assignee : ${activity.assignee!""}
   </ul>
   <#break>
  <#case "COMMENT">
   ${change_by} Added a comment <br /><q>${activity.new_value}</q>
   <#break>
  <#case "UPDATE_TITLE">
   Review updated <s>${activity.old_value}</s> -> ${activity.new_value}
   <#break>
  <#case "UPDATE_MESSAGE_TYPE">
   Review type updated ${activity.old_value} -> ${activity.new_value}
   <#break>
  <#case "UPDATE_ROLE">
   Role updated ${activity.old_value} -> ${activity.new_value}
   <#break>
  <#case "UPDATE_ASSIGNEE">
   Assignee updated ${activity.old_value!"Not Assigned "} -> ${activity.new_value!" Not Assigned "}
   <#break>
  <#case "UPDATE_BODY">
   Body updated ${activity.old_value} -> ${activity.new_value}
   <#break>
  <#case "UPDATE_STATUS">
   Review Status updated ${activity.old_value} -> ${activity.new_value}
   <#break>
 </#switch>
 <br/><br/>
</#list>

Click <a href="${link_to_ticket}">here</a> to know more.
<br /><br />
This is a system generated mail.', null, null, 'coder_review_update', 'EMAIL', 'A', null, 'general', 'A', 'null'
);