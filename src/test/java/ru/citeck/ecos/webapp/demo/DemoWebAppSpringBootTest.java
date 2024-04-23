package ru.citeck.ecos.webapp.demo;

import lombok.Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.citeck.ecos.records2.predicate.model.Predicates;
import ru.citeck.ecos.records3.RecordsService;
import ru.citeck.ecos.records3.record.atts.schema.annotation.AttName;
import ru.citeck.ecos.records3.record.dao.query.dto.query.RecordsQuery;
import ru.citeck.ecos.webapp.api.entity.EntityRef;
import ru.citeck.ecos.webapp.demo.records.DemoInMemRecordsDao;
import ru.citeck.ecos.webapp.lib.spring.test.extension.EcosSpringExtension;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Spring boot tests should use EcosSpringExtension
 * instead of SpringExtension for correct work.
 */
@ExtendWith(EcosSpringExtension.class)
@SpringBootTest(classes = { EcosDemoApp.class })
public class DemoWebAppSpringBootTest {

    @Autowired
    public RecordsService recordsService;

    @Test
    public void test() {

        final int numFieldInitialValue = 123;

        MutationAtts simpleDto = new MutationAtts();
        simpleDto.setId("test-id");
        simpleDto.setNumField(numFieldInitialValue);

        // method 'create' is alias for mutation:
        //
        // mutate(EntityRef.valueOf(sourceId + '@'), attributes)
        //
        // mutation with empty localId used for creation of record
        EntityRef recordRef = recordsService.create(DemoInMemRecordsDao.ID, simpleDto);

        // 'getAtt' or 'getAtts' methods used to load attribute values from any record in system.
        // result of getAtt always will be not null DataValue, which is universal wrapper
        // for any json value (string/number/boolean/array/object/null)
        // any method of DataValue designed to be non-dangerous if it is possible.
        // E.g. asText() will return empty string when called for null value and newer throw NullPointerException.
        int numFieldValue = recordsService.getAtt(recordRef, "numField").asInt();

        assertThat(numFieldValue).isEqualTo(numFieldInitialValue);

        // getAtts can be used with any DTO and result will be new instance of this DTO filled with data.
        // You can use annotation @AttName to define custom attribute names for loading.
        GetAttsDto atts = recordsService.getAtts(recordRef, GetAttsDto.class);
        assertThat(atts.getCustomNumField()).isEqualTo(numFieldInitialValue);

        // 'delete' method can be used to delete any records
        recordsService.delete(recordRef);

        // if we try to load attribute from non-existent record,
        // then we'll get null value and asInt(-1) return -1 in this case.
        int numFieldValue2 = recordsService.getAtt(recordRef, "numField").asInt(-1);
        assertThat(numFieldValue2).isEqualTo(-1);
    }

    @Test
    public void queryTest() {

        // Create 10 records in DemoInMemRecordsDao
        for (int i = 0; i < 10; i++) {
            MutationAtts dto = new MutationAtts();
            dto.setId("id-" + i);
            dto.setNumField(i);
            recordsService.create(DemoInMemRecordsDao.ID, dto);
        }

        // Query records from DemoInMemRecordsDao where numField greater or equal to 5
        List<EntityRef> records = recordsService.query(RecordsQuery.create()
            .withSourceId(DemoInMemRecordsDao.ID)
            .withQuery(Predicates.ge("numField", 5))
            .build()
        ).getRecords();

        assertEquals(5, records.size());
    }

    @Data
    static class GetAttsDto {
        // field can be named as 'numField' and this annotation
        // won't be required but this construction is used for example.
        @AttName("numField")
        private double customNumField;
    }

    @Data
    static class MutationAtts {
        private String id;
        private int numField;
    }
}
