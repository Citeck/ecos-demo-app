package ru.citeck.ecos.webapp.demo.exttask;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.bpmn.externaltask.impl.retry.ExternalTaskRetry;
import ru.citeck.ecos.records3.RecordsService;
import ru.citeck.ecos.webapp.lib.spring.context.txn.RunInTransaction;

/**
 * <a href="https://citeck-ecos.readthedocs.io/ru/latest/settings_kb/processes/ecos_bpmn/ecos_bpmn_external_task.html">Documentation</a>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ExternalTaskSubscription("demo-ext-task")
public class DemoExternalTask implements ExternalTaskHandler {

    private final RecordsService recordsService;

    @Override
    // If you wrapp execute method in RunInTransaction, then external task
    // in process should have flag asyncAfter to avoid transactional errors
    @RunInTransaction
    @ExternalTaskRetry(retries = 10, retryTimeout = 10_000)
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {

        String documentRef = externalTask.getVariable("documentRef");

        log.info("External task for document: " + documentRef);

        String textField = recordsService.getAtt(documentRef, "textField").asText();

        log.info("Text field: '" + textField + "'");

        /*
        You can use simple mutation of one attribute using mutateAtt method or
        use advanced method with RecordAtts. E.g.:

        RecordAtts record = new RecordAtts(documentRef);
        record.setAtt("extTaskField", "TextField: " + textField);
        record.setAtt("otherAttribute", "otherValue");
        recordsService.mutate(record);
         */
        recordsService.mutateAtt(documentRef, "extTaskField", "TextField: " + textField);

        // You can throw business error here. This error should be correctly handled in process
        // externalTaskService.handleBpmnError(externalTask, "error-code", "error-message");

        externalTaskService.complete(externalTask);
    }
}
