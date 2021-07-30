delete from qrtz_cron_triggers where trigger_name = 'jobMonitorTrigger';
delete from qrtz_triggers where trigger_name = 'jobMonitorTrigger';
delete from qrtz_job_details where job_name = 'jobMonitorDetail';
