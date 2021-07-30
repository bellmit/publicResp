<Claims>
    <Claim>
        <ID>[#if claimId??]${claimId?c}[/#if]</ID>
        <Person>
            [#if patientId??]<ID>${patientId}</ID>[/#if]
            [#if dateofbirth??]
            <BirthDate>${dateofbirth?string["dd/MM/yyyy"]}</BirthDate>
            [/#if]
            [#if gender??]
            <Gender>${gender}</Gender>
            [/#if]
            [#if weight??]
            <Weight>${weight?c}</Weight>
            [/#if]
        </Person>
        <Encounter>
            [#if encounterType??]<Type>${encounterType}</Type>[/#if]
            [#if encounterStartType??]<StartType>${encounterStartType}</StartType>[/#if]
            [#if encounterEndType??]<EndType>${encounterEndType}</EndType>[/#if]
        </Encounter>

        [#if diagnoses??]
            [#list diagnoses as diagnosis]
                <Diagnosis>
                    [#if diagnosis.code_type_classification??]<CodeTerm>${diagnosis.code_type_classification}</CodeTerm>[/#if]
                    [#if diagnosis.diag_type??][#if diagnosis.diag_type == "P" ]<Type>Principal</Type>[/#if][#if diagnosis.diag_type == "S" ]<Type>Secondary</Type>[/#if][/#if]
                    [#if diagnosis.icd_code??]<Code>${diagnosis.icd_code}</Code>[/#if]
                </Diagnosis>
            [/#list]
        [/#if]

        [#if activities??]
            [#list activities as activity]
                <Activity>
                    [#if activity.patient_presc_id??][#if activity.patient_presc_id?is_number]<ID>${activity.patient_presc_id?c}</ID>[#else]<ID>${activity.patient_presc_id}</ID>[/#if][/#if]
                    [#if activity.code_type_classification??]<CodeTerm>${activity.code_type_classification}</CodeTerm>[/#if]
                    [#if activity.prescribed_date??]<Start>${activity.prescribed_date?string["dd/MM/yyyy HH:mm"]}</Start>[/#if]
                    [#if activity.item_code??]<Code>${activity.item_code}</Code>[/#if]
                    [#if activity.qty?? && activity.qty!=0]<Quantity>${activity.qty?c}</Quantity>[#else]<Quantity>1</Quantity>[/#if]
                    [#if observations?? && activity.patient_presc_id?? && observations[activity.patient_presc_id]??]
                        [#assign observations_for_charge_id = observations[activity.patient_presc_id]]
                        [#list observations_for_charge_id as observation]
                            <Observation>
                                [#if observation.observation_id??]<ID>${observation.observation_id?c}</ID>[/#if]
                                [#if observation.observation_type??]<Type>${observation.observation_type}</Type>[/#if]
                                [#if observation.code??]<Code>${observation.code}</Code>[/#if]
                                [#if observation.value??]<Value>${observation.value}</Value>[/#if]
                                [#if observation.value_type??]<ValueType>${observation.value_type}</ValueType>[/#if]
                            </Observation>
                        [/#list]
                    [/#if]
                </Activity>
            [/#list]
        [/#if]
        
        
    </Claim>
</Claims>