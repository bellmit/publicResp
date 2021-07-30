-- liquibase formatted sql
-- changeset utkarshjindal:order-tokenised-search

create table orderable_items_tokens (orderable_item_id integer, token character varying(100));

insert into orderable_items_tokens (SELECT orderable_item_id, s.token FROM  orderable_item oi, unnest(string_to_array(oi.item_name, ' ')) s(token));

insert into orderable_items_tokens (SELECT orderable_item_id, s.token FROM  orderable_item oi, unnest(string_to_array(oi.item_codes, ' ')) s(token));

create index orderable_item_token_id_idx ON orderable_items_tokens (orderable_item_id);

create index orderable_items_token_token_idx ON orderable_items_tokens (token varchar_pattern_ops);


