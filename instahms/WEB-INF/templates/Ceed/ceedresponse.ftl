[#if response??]
    <response>
    [#list response.getClaimEdit() as claimedit]
        <ClaimEdit>
            <ID>${claimedit.getID()}</ID>
            <RefNum>${claimedit.getRefNum()}</RefNum>
            <DateTime>${claimedit.getDateTime()}</DateTime>
            <EngineVersion>${claimedit.getEngineVersion()}</EngineVersion>
            [#list claimedit.getEdit() as edit]
            <Edit>
                <ID>${edit.getID()}</ID>
                <Type>${edit.getType()}</Type>
                <SubType>${edit.getSubType()}</SubType>
                <Code>${edit.getCode()}</Code>
                <Rank>${edit.getRank()}</Rank>
                <Comment>${edit.getComment()}</Comment>
            </Edit>
            [/#list]
            [#list claimedit.getActivityEdit() as activityedit]
            <ActivityEdit>
                <ID>${activityedit.getID()}</ID>
                <Code>${activityedit.getCode()}</Code>
                [#list activityedit.getEditId() as editid]
                <EditId>${editid}</EditId>
                [/#list]
            </ActivityEdit>
            [/#list]
        </ClaimEdit>
    [/#list]
    </response>
[/#if]