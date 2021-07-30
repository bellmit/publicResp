-- liquibase formatted sql
-- changeset rajrajeshwarsinghrathore:adding-a-new-column-to-store-preference-named-lastNumberOfDays-for-RemittanceAutoProcessJob

alter table generic_preferences
    add ra_auto_download_last_no_of_days int default 2 not null;