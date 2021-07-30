-- liquibase formatted sql
-- changeset pranays:<update-bmi-chart-title-values>

UPDATE growth_chart_details SET chart_title='2 to 20 years : MALE Body mass index-for-Age :' WHERE chart_type='BMI' AND gender='M';

UPDATE growth_chart_details SET chart_title='2 to 20 years : FEMALE Body mass index-for-Age :' WHERE chart_type='BMI' AND gender='F';
