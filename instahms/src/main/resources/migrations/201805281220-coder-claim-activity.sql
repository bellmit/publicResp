
-- liquibase formatted sql
-- changeset AdeshAtole:coder-claim-activity

create sequence coder_claim_review_activity_seq
    START 1
	INCREMENT 1
	MINVALUE 1
	NO MAXVALUE
	CACHE 1;

create sequence coder_claim_review_activity_changeset_seq
    START 1
	INCREMENT 1
	MINVALUE 1
	NO MAXVALUE
	CACHE 1;

create table IF NOT EXISTS coder_claim_review_activity (
    activity_id integer default nextval('coder_claim_review_activity_seq') primary key,
    ticket_id integer references tickets(id) not null,
    user_id character varying(34) not null,
    activity character varying(20) not null,
    old_value text,
    new_value text,
    changeset integer not null,
    change_at timestamp default now()
);

BEGIN;
INSERT INTO coder_claim_review_activity ( ticket_id, user_id, activity , new_value, change_at, changeset ) 
	SELECT ticket_id, comment_by, 'COMMENT', comment, comment_at, nextval('coder_claim_review_activity_seq') 
		FROM ticket_comments;

COMMIT;
		