--
-- Upload Growth Chart reference data from csv file.
--

\set QUIET
\set ON_ERROR_STOP
\t ON
\pset pager off
SET client_min_messages = warning;

DROP TABLE IF EXISTS tmp_growth_chart_reference_data;
CREATE TABLE tmp_growth_chart_reference_data (
	sex text not null,
	year numeric not null,
	month numeric not null,
	L text,
	M text,
	S text,
	SD text,
	length text,
	stature text,
	per_1 text,
	per_3 text,
	per_5 text,
	per_10 text,
	per_15 text,
	per_25 text,
	per_50 text,
	per_75 text,
	per_85 text,
	per_90 text,
	per_95 text,
	per_97 text,
	per_99 text,
	chart_type text not null
);

\COPY tmp_growth_chart_reference_data FROM '/tmp/masters/growth_chart_reference_data.csv' csv header

update tmp_growth_chart_reference_data set
	sex=trim(sex),
	year=year,
	month=month,
	L=trim(L),
	M=trim(M),
	S=trim(S),
	SD=trim(SD),
	length=trim(length),
	stature=trim(stature),
	per_1=trim(per_1),
	per_3=trim(per_3),
	per_5=trim(per_5),
	per_10=trim(per_10),
	per_15=trim(per_15),
	per_25=trim(per_25),
	per_50=trim(per_50),
	per_75=trim(per_75),
	per_85=trim(per_85),
	per_90=trim(per_90),
	per_95=trim(per_95),
	per_97=trim(per_97),
	per_99=trim(per_99),
	chart_type=trim(chart_type);

INSERT INTO growth_chart_reference_data (id, sex, year, month, L, M, S, SD, length, stature, per_1, per_3, per_5, per_10, per_15, per_25,
per_50, per_75, per_85, per_90, per_95, per_97, per_99, chart_type)
SELECT nextval('growth_chart_reference_data_seq'), sex, year, month, L, M, S, SD, length, stature, per_1, per_3, per_5, per_10, per_15, per_25,
per_50, per_75, per_85, per_90, per_95, per_97, per_99, chart_type
FROM tmp_growth_chart_reference_data;

drop table if exists tmp_growth_chart_reference_data;
