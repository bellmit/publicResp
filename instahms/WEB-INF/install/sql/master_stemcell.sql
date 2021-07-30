--
-- OP consultation forms for Stem Cell registry
--

INSERT INTO department VALUES ('DEP0005','Neurology', 'A');

-- Note: the following requires psql to be optionally run with
-- something like psql --variable SQLPATH=<location>/WEB-INF/install/sql if the current
-- directory is not already the directory where this file resides.
--
\cd :SQLPATH

\set file_content '\'' `base64 ../forms/adverse_event_report.pdf` '\''
INSERT INTO treatment_form_pdfs (form_id, form_title, dept_id, treatment_name, status, pdf_content,permission)
  VALUES (nextval('treatment_form_pdfs_seq'), 'Stem Cell Treatment - Parkinsons - Adverse Event Report', 'DEP0005', 'Stem Cell Treatment', 'A',
	decode(:file_content, 'base64'),'RW'
  )
;

\set file_content '\'' `base64 ../forms/Base_line_and_lab.pdf` '\''
INSERT INTO treatment_form_pdfs (form_id, form_title, dept_id, treatment_name, status, pdf_content,permission)
  VALUES (nextval('treatment_form_pdfs_seq'), 'Stem Cell Treatment - Parkinsons - Baseline Exam and Lab Test Results', 'DEP0005', 'Stem Cell Treatment', 'A',
	decode(:file_content, 'base64'),'RW'
  )
;

\set file_content '\'' `base64 ../forms/CASE_RECORD_FORM.pdf` '\''
INSERT INTO treatment_form_pdfs (form_id, form_title, dept_id, treatment_name, status, pdf_content,permission)
  VALUES (nextval('treatment_form_pdfs_seq'), 'Case Record Form', 'DEP0005', 'Stem Cell Treatment', 'A',
	decode(:file_content, 'base64'),'RW'
  )
;

\set file_content '\'' `base64 ../forms/END_OF_THE_REOPRT.pdf` '\''
INSERT INTO treatment_form_pdfs (form_id, form_title, dept_id, treatment_name, status, pdf_content,permission)
VALUES (nextval('treatment_form_pdfs_seq'), 'Stem Cell - Parkinson - End of Treatment', 'DEP0005', 'Stem Cell Treatment', 'A',
	decode(:file_content, 'base64'),'RW'
  )
;

\set file_content '\'' `base64 ../forms/Neuro_and_motor_exam.pdf` '\''
INSERT INTO treatment_form_pdfs (form_id, form_title, dept_id, treatment_name, status, pdf_content,permission)
VALUES (nextval('treatment_form_pdfs_seq'), 'Stem Cell treatment - Parkinson Neuro and Motor Examination', 'DEP0005', 'Stem Cell Treatment', 'A',
	decode(:file_content, 'base64'),'RW'
  )
;

\set file_content '\'' `base64 ../forms/Patient_data_and_history.pdf` '\''
INSERT INTO treatment_form_pdfs (form_id, form_title, dept_id, treatment_name, status, pdf_content,permission)
VALUES (nextval('treatment_form_pdfs_seq'), 'Stem Cell treatment - Parkinsons - Patient Data and Hhistory', 'DEP0005', 'Stem Cell Treatment', 'A',
	decode(:file_content, 'base64'),'RW'
  )
;

\set file_content '\'' `base64 ../forms/sensory_exam.pdf` '\''
INSERT INTO treatment_form_pdfs (form_id, form_title, dept_id, treatment_name, status, pdf_content,permission)
VALUES (nextval('treatment_form_pdfs_seq'), 'Stem Cell treatment - Parkinsons - Sensory Assessment', 'DEP0005', 'Stem Cell Treatment', 'A',
	decode(:file_content, 'base64'),'RW'
  )
;


