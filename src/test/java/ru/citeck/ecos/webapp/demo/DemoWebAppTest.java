package ru.citeck.ecos.webapp.demo;

import lombok.Data;
import org.junit.jupiter.api.Test;
import ru.citeck.ecos.records2.predicate.model.Predicates;
import ru.citeck.ecos.records3.RecordsService;
import ru.citeck.ecos.records3.RecordsServiceFactory;
import ru.citeck.ecos.records3.record.dao.query.dto.query.RecordsQuery;
import ru.citeck.ecos.webapp.api.entity.EntityRef;
import ru.citeck.ecos.webapp.demo.records.DemoInMemRecordsDao;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test without the Spring context. This approach is the preferred variant for testing.
 * In this case, more manual initialization is required, but it increases the speed of testing.
 */
public class DemoWebAppTest {

    @Test
    public void queryTest() {

        // RecordsService can be initialized manually everywhere by creating RecordsServiceFactory
        RecordsService recordsService = new RecordsServiceFactory().getRecordsServiceV1();
        recordsService.register(new DemoInMemRecordsDao());

        for (int i = 0; i < 10; i++) {
            MutationAtts dto = new MutationAtts();
            dto.setId("id-" + i);
            dto.setNumField(i);
            recordsService.create(DemoInMemRecordsDao.ID, dto);
        }
        List<EntityRef> records = recordsService.query(RecordsQuery.create()
            .withSourceId(DemoInMemRecordsDao.ID)
            .withQuery(Predicates.ge("numField", 5))
            .build()
        ).getRecords();

        assertEquals(5, records.size());
    }

    @Data
    static class MutationAtts {
        private String id;
        private int numField;
    }
}
