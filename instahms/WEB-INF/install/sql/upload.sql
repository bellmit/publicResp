--
-- OP consultation forms (default forms)
--
-- Note: the following requires psql to be optionally run with
-- something like psql --variable SQLPATH=<location>/WEB-INF/install/sql if the current
-- directory is not already the directory where this file resides.
--

\cd :SQLPATH
\set file_content '\'' `base64 ../forms/Generic_Registration_Card.pdf` '\''
INSERT INTO registration_cards (card_id, card_name, visit_type, rate_plan, status, custom_reg_card_template)
  VALUES (1, 'Generic Registration Form', 'A', 'A','A',
	decode(:file_content, 'base64')
 )
;

