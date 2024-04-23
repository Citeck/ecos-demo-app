package ru.citeck.ecos.webapp.demo.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records2.predicate.model.Predicates;
import ru.citeck.ecos.records3.RecordsService;
import ru.citeck.ecos.records3.record.dao.query.dto.query.RecordsQuery;
import ru.citeck.ecos.records3.record.dao.query.dto.res.RecsQueryRes;
import ru.citeck.ecos.webapp.api.entity.EntityRef;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimpleAnnotatedJob {

    private final AtomicInteger counter = new AtomicInteger();

    private final RecordsService recordsService;

    /**
     * @see Scheduled
     */
    @Scheduled(fixedDelayString = "${ecos.demo.simple-annotated-job.delay}")
    void doSomeWork() {
        RecsQueryRes<EntityRef> queryRes = recordsService.query(
                RecordsQuery.create()
                        .withEcosType("demo-type")
                        // sourceId will be loaded from ecosType by default,
                        // but you can specify it explicitly
                        //.withSourceId(AppName.EMODEL + "/demo-type")
                        .withQuery(Predicates.notEmpty("childEntities"))
                        .withMaxItems(0) // Query for totalCount without records
                        .build()
        );
        log.info("Simple annotated job example #" + counter.incrementAndGet() +
                ". Demo records with children: " + queryRes.getTotalCount());
    }
}
