-- liquibase formatted sql
-- changeset janakivg:growth-chart-db-changes

create table growth_chart_descriptions(
chart_type character varying(30),
standard_deviation character varying(200),
chart_range character varying(30),
x_axis_start integer,
x_axis_end integer,
y_axis_start integer,
y_axis_end   integer,
second_axis_start integer,
second_axis_end integer,
title character varying(200)
);

INSERT INTO growth_chart_descriptions(chart_type,standard_deviation,chart_range,x_axis_start, 
x_axis_end,y_axis_start,y_axis_end,second_axis_start,second_axis_end)  
VALUES('L,WA','3,5,10,25,50,75,90,95,97','0-2',0,25,0,16,40,100);

INSERT INTO growth_chart_descriptions(chart_type,standard_deviation,chart_range,x_axis_start, 
x_axis_end,y_axis_start,y_axis_end)  
VALUES('HC','3,5,10,25,50,75,90,95,97','0-2',0,25,30,60);


INSERT INTO growth_chart_descriptions(chart_type,standard_deviation,chart_range,x_axis_start, 
x_axis_end,y_axis_start,y_axis_end)  
VALUES('WL','3,5,10,25,50,75,90,95,97','0-2',45,110,0,22);

INSERT INTO growth_chart_descriptions(chart_type,standard_deviation,chart_range,x_axis_start, 
x_axis_end,y_axis_start,y_axis_end,second_axis_start,second_axis_end)  
VALUES('S,WA','3,5,10,25,50,75,90,95,97','0-2',24,241,5,105,70,195);

INSERT INTO growth_chart_descriptions(chart_type,standard_deviation,chart_range,x_axis_start, 
x_axis_end,y_axis_start,y_axis_end)  
VALUES('BMI','3,5,10,25,50,75,90,95,97','0-2',24,240,11,37);

INSERT INTO growth_chart_descriptions(chart_type,standard_deviation,chart_range,x_axis_start, 
x_axis_end,y_axis_start,y_axis_end)  
VALUES('WS','3,5,10,25,50,75,90,95,97','0-2',77,125,7,35);

create table growth_chart_details(
chart_type character varying(30),
chart_name character varying(250),
gender character varying(1),
chart_title character varying(300)
);

INSERT INTO growth_chart_details(chart_type,chart_name,gender,chart_title) values('L,WA','Length-for-age And Weight-for-age percentiles (Birth to 2 years)','M','Birth to 24 months : MALE Length-for-Age and Weight-for-Age:');
INSERT INTO growth_chart_details(chart_type,chart_name,gender,chart_title) values('L,WA','Length-for-age And Weight-for-age percentiles (Birth to 2 years)','F','Birth to 24 months : FEMALE Length-for-Age and Weight-for-Age:');

INSERT INTO growth_chart_details(chart_type,chart_name,gender,chart_title) values('HC','Head circumference-for-age percentiles (Birth to 2 years)','M','Birth to 24 months : MALE Head circumference-for-Age:');
INSERT INTO growth_chart_details(chart_type,chart_name,gender,chart_title) values('HC','Head circumference-for-age percentiles (Birth to 2 years)','F','Birth to 24 months : FEMALE Head circumference-for-Age:');

INSERT INTO growth_chart_details(chart_type,chart_name,gender,chart_title) values('WL','Weight-for-length percentiles (Birth to 2 years)','M','Weight-for-Length: MALE ');
INSERT INTO growth_chart_details(chart_type,chart_name,gender,chart_title) values('WL','Weight-for-length percentiles (Birth to 2 years)','F','Weight-for-Length: FEMALE ');

INSERT INTO growth_chart_details(chart_type,chart_name,gender,chart_title) values('S,WA','Stature for age And weight for age percentiles (2 to 20 years)','M','2 to 20 years : MALE Stature-for-Age and Weight-for-Age:');
INSERT INTO growth_chart_details(chart_type,chart_name,gender,chart_title) values('S,WA','Stature for age And weight for age percentiles (2 to 20 years)','F','2 to 20 years : FEMALE  Stature-for-Age and Weight-for-Age:');

INSERT INTO growth_chart_details(chart_type,chart_name,gender,chart_title) values('BMI','BMI for age percentiles (2 to 20 years)','M','0 to 20 years : MALE Body mass index-for-Age :');
INSERT INTO growth_chart_details(chart_type,chart_name,gender,chart_title) values('BMI','BMI for age percentiles (2 to 20 years)','F','0 to 20 years : FEMALE Body mass index-for-Age :');

INSERT INTO growth_chart_details(chart_type,chart_name,gender,chart_title) values('WS','Weight-for-stature percentiles (2 to 20 years)','M','Weight-for-Stature: MALE ');
INSERT INTO growth_chart_details(chart_type,chart_name,gender,chart_title) values('WS','Weight-for-stature percentiles (2 to 20 years)','F','Weight-for-Stature: FEMALE ');
