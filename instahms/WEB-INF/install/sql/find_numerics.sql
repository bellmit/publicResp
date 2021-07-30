--
-- Script to find all the numeric data type columns, and output the
-- necessary script to modify the column types to numeric(11,3).
-- Run from command line like this:
--   psql --quiet --variable SCHEMA=\'local\' -f find_numerics.sql > precision_3.sql
--
-- After generating, look for _rate, qty, _per and if the field looks like a non-amount
-- field, comment out that line.
-- 

\a
\t

SELECT '-- Generated from find_numerics.sql. After generating, remove/comment those fields that do not need to be altered (eg, quantity fields)';

SELECT 'ALTER TABLE ' || relname || ' ALTER COLUMN ' || attname || ' TYPE numeric(16,3);'
	|| ' -- original: ' || format_type(a.atttypid, a.atttypmod)
FROM pg_attribute a 
	JOIN pg_class c ON (c.oid = a.attrelid)
	JOIN pg_namespace ns ON (c.relnamespace = ns.oid)
WHERE  relkind = 'r'AND nspname = :SCHEMA
	AND format_type(a.atttypid, a.atttypmod) like 'numeric%,2)'
ORDER by relname;

