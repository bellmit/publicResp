-- liquibase formatted sql
-- changeset eshwar-chandra:<adding-city_master-to-code_set-master>

--
-- Name: city_id_seq; Type: SEQUENCE; Schema: fresh; Owner: -
--
CREATE SEQUENCE city_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

COMMENT ON SEQUENCE city_id_seq IS '{ "type": "Txn", "comment": "city id seq" }';


ALTER TABLE city ADD id integer DEFAULT nextval('city_id_seq'::regclass) NOT NULL;

INSERT INTO code_system_categories (id, label, status, table_name, entity_name, entity_id)
VALUES (12 ,'City', 'A', 'city', 'city_name', 'id');
