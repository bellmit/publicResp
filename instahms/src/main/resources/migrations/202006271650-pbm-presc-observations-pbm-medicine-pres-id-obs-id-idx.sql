-- liquibase formatted sql
-- changeset rajendratalekar:pbm-presc-observations-pbm-medicine-pres-id-obs-id-idx failOnError:false

create index pbm_presc_observations_pbm_medicine_pres_id_obs_id_idx on pbm_presc_observations(pbm_medicine_pres_id,obs_id);	
