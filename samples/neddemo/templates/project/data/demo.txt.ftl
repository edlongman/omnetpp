Apps:
<#list apps as i>
    ${i}
</#list>

Layers:
<#list layers?keys?sort as i>
    ${i}:
<#if layers[i]?is_sequence>
        <#list layers[i] as j>${j} </#list>
<#else>
        ${layers[i]}
</#if>
</#list>
