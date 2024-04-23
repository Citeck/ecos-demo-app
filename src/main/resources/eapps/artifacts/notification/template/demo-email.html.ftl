<#escape x as x?html>
    <html lang="en">
    <head>
        <title></title>
        <style>
            table {
                font-family: Arial, Helvetica, sans-serif;
                border-collapse: collapse;
            }

            table td, table th {
                border: 1px solid #ddd;
                padding: 8px;
            }

            table tr:nth-child(even){background-color: #f2f2f2;}

            table tr:hover {background-color: #ddd;}

            table th {
                padding-top: 6px;
                padding-bottom: 6px;
                text-align: left;
                background-color: rgba(186, 255, 229, 0.5);
                color: #000000;
            }
        </style>
    </head>
    <body>
    <div>Information about '${docName}'</div>
    <br/>
    <#if children?has_content>
        <table>
            <caption>Children</caption>
            <tr>
                <th>name</th>
                <th>number</th>
            </tr>
            <#list children as child>
                <tr>
                    <td>${child.name}</td>
                    <td>${child.number}</td>
                </tr>
            </#list>
        </table>
    <#else>
        <div>Children is empty</div>
    </#if>
    <br/>
    <#if comment?has_content>
        <div>Comment: ${comment}</div>
    <#else>
        <div>Comment is empty</div>
    </#if>
    <br/>
    <a href="${link.getRecordLink(docRef)}" target="_blank"><strong>Document link</strong></a>
    </body>
    </html>
</#escape>
