-- liquibase formatted sql
-- changeset rajrajeshwarsinghrathore:adding-a-new-column-is-cloned-to-table-preauth-prescription-JIRA-HMS-36546

alter table preauth_prescription
    add is_cloned char default 'N' not null;
