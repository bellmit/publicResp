-- liquibase formatted sql
-- changeset adeshatole:get-rid-of-duplicates-in-oit

DELETE FROM orderable_items_tokens;
INSERT INTO orderable_items_tokens (SELECT orderable_item_id, s.token FROM  orderable_item oi, unnest(string_to_array(regexp_replace(trim(both ' ' from oi.item_name), '\s+', ' '), ' ')) s(token));
INSERT INTO orderable_items_tokens (SELECT orderable_item_id, s.token FROM  orderable_item oi, unnest(string_to_array(regexp_replace(trim(both ' ' from oi.item_codes), '\s+', ' '), ' ')) s(token));
