-- liquibase formatted sql
-- changeset allahbakash:ahll-apollo-one-integration
-- validCheckSum: ANY

INSERT INTO insta_integration (integration_name, url, userid, password, aeskey, merchant_id, status, environment, chargebee_customer_id, host, port, application_id, application_secret, agent_port, agent_host) VALUES ('one_apollo_loyalty_card', NULL, NULL, NULL, NULL, NULL, 'I', 'PROD', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
--payment_mode_master for oneApolloPoints, Make ref_required as active
INSERT INTO payment_mode_master (mode_id, payment_mode, card_type_required, bank_required, ref_required, realization_required, status, displayorder, spl_account_name, bank_batch_required, card_auth_required, card_holder_required, card_number_required, card_expdate_required, totp_required, mobile_number_required, transaction_limit, allow_payments_more_than_transaction_limit) VALUES (-5, 'Points Redemption', 'N', 'N', 'Y', 'N', 'I', 8, 'oneApolloPoints', 'N', 'N', 'N', 'N', 'N', 'Y', 'Y', 0.00, 'A');
--oneApollo offers link
INSERT INTO insta_integration ("integration_name","status","environment") VALUES ('oneapollo_offers','I','PROD');