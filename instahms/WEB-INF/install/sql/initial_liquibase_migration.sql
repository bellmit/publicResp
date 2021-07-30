--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: databasechangelog; Type: TABLE;
--

CREATE TABLE databasechangelog (
    id character varying(255) NOT NULL,
    author character varying(255) NOT NULL,
    filename character varying(255) NOT NULL,
    dateexecuted timestamp without time zone NOT NULL,
    orderexecuted integer NOT NULL,
    exectype character varying(10) NOT NULL,
    md5sum character varying(35),
    description character varying(255),
    comments character varying(255),
    tag character varying(255),
    liquibase character varying(20),
    contexts character varying(255),
    labels character varying(255),
    deployment_id character varying(10)
);

--
-- Name: databasechangeloglock; Type: TABLE;
--

CREATE TABLE databasechangeloglock (
    id integer NOT NULL,
    locked boolean NOT NULL,
    lockgranted timestamp without time zone,
    lockedby character varying(255)
);


--
-- Data for Name: databasechangelog; Type: TABLE DATA;
--

INSERT INTO databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('setSchema', 'adityabhatia02', '/root/webapps/instahms/src/main/resources/migrations/0.sql', '2018-05-02 20:21:26.501505', 1, 'EXECUTED', '7:b456f0c9a5f6757e6004b9ba5e2cd810', 'sql', '', NULL, '3.5.0', NULL, NULL, '5272686067');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('create-tables.sql', 'adityabhatia02', '/root/webapps/instahms/src/main/resources/migrations/201711161745-create-tables.sql', '2018-05-02 20:21:32.75375', 2, 'EXECUTED', '7:c2a7c91f8145cdc8a669014f14365e02', 'sql', '', NULL, '3.5.0', NULL, NULL, '5272686067');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('insert-data.sql', 'adityabhatia02', '/root/webapps/instahms/src/main/resources/migrations/201711161745-data-insertion.sql', '2018-05-02 20:21:35.325763', 3, 'EXECUTED', '7:d4af3d52d6a8c99b670265c7410d9b59', 'sql', '', NULL, '3.5.0', NULL, NULL, '5272686067');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('create-accounting-tables-sequences.sql', 'adityabhatia02', '/root/webapps/instahms/src/main/resources/migrations/201711161747-create-accounting-tables.sql', '2018-05-02 20:21:35.976923', 4, 'EXECUTED', '7:7c6e06bacd3040e54d2852d93f3cac52', 'sql', '', NULL, '3.5.0', NULL, NULL, '5272686067');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('create-views.sql', 'adityabhatia02', '/root/webapps/instahms/src/main/resources/migrations/999999999985-vft.sql', '2018-05-02 20:23:05.565806', 5, 'EXECUTED', '7:df79bbefee13bc4ca15dca9d26da79a7', 'sql', '', NULL, '3.5.0', NULL, NULL, '5272686067');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('create-report-views.sql', 'adityabhatia02', '/root/webapps/instahms/src/main/resources/migrations/999999999986-report-views.sql', '2018-05-02 20:23:06.671612', 6, 'EXECUTED', '7:d6833fd4a343e68042b807db73c42ab4', 'sql', '', NULL, '3.5.0', NULL, NULL, '5272686067');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('accounting-views.sql', 'adityabhatia02', '/root/webapps/instahms/src/main/resources/migrations/999999999987-accounting-views.sql', '2018-05-02 20:23:26.064608', 7, 'EXECUTED', '7:99d3c1fa4d604fc2261c080c49039c65', 'sql', '', NULL, '3.5.0', NULL, NULL, '5272686067');
INSERT INTO databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('gen_report_views.sql', 'adityabhatia02', '/root/webapps/instahms/src/main/resources/migrations/999999999988-gen-report-views.sql', '2018-05-02 20:23:41.801806', 8, 'EXECUTED', '7:6fe9e99849695edf495e3dfb0023b8ce', 'sql', '', NULL, '3.5.0', NULL, NULL, '5272686067');
--
-- Data for Name: databasechangeloglock; Type: TABLE DATA;
--

INSERT INTO databasechangeloglock (id, locked, lockgranted, lockedby) VALUES (1, false, NULL, NULL);


--
-- PostgreSQL database dump complete
--

