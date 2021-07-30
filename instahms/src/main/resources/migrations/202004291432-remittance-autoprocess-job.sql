-- liquibase formatted sql
-- changeset rajrajeshwarsinghrathore:job-to-auto-process-new-ra-files-jira-HMS-34290

INSERT INTO cron_job(job_group, job_name, job_time, job_allow_disable,
                     job_status, job_description)
VALUES('RemittanceAutoProcessJob', 'RemittanceAutoProcessJob', '0 30 7 * * ?', 'Y', 'I',
       '07:30:00 daily: Auto processing the RA files');
