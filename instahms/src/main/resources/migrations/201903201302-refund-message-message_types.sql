-- liquibase formatted sql
-- changeset anandpatel:adding-message-event-and-message-type-for-refund-bill
INSERT INTO message_events (event_id, event_name, event_description)
VALUES ('bill_refund_message', 'Bill Refund', 'Event used for sending refund message');

INSERT INTO message_types (message_type_id, message_type_name, message_type_description, message_sender, message_to, message_subject, message_body, message_cc, message_bcc, event_id, message_mode, status, category_id, message_group, editability, message_footer)
VALUES ('sms_bill_refund', 'Bill Refund', 'Message is sent automatically to patients whenever user makes a refund.', NULL, NULL, NULL,'Dear ${recipient_name}, refund of ${currency_symbol} ${refund_amount} is initiated on ${refund_date} at ${center_name}. Please contact ${center_contact_phone} for any queries.', NULL, NULL, 'bill_refund_message', 'SMS', 'I', NULL, 'general', 'A', '');
