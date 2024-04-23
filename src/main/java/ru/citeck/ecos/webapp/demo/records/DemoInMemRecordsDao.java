package ru.citeck.ecos.webapp.demo.records;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.commons.data.MLText;
import ru.citeck.ecos.commons.json.Json;
import ru.citeck.ecos.context.lib.i18n.I18nContext;
import ru.citeck.ecos.records2.RecordConstants;
import ru.citeck.ecos.records2.predicate.PredicateService;
import ru.citeck.ecos.records2.predicate.model.Predicate;
import ru.citeck.ecos.records3.record.atts.dto.LocalRecordAtts;
import ru.citeck.ecos.records3.record.atts.schema.annotation.AttName;
import ru.citeck.ecos.records3.record.dao.AbstractRecordsDao;
import ru.citeck.ecos.records3.record.dao.atts.RecordAttsDao;
import ru.citeck.ecos.records3.record.dao.delete.DelStatus;
import ru.citeck.ecos.records3.record.dao.delete.RecordDeleteDao;
import ru.citeck.ecos.records3.record.dao.mutate.RecordMutateDao;
import ru.citeck.ecos.records3.record.dao.query.RecordsQueryDao;
import ru.citeck.ecos.records3.record.dao.query.dto.query.QueryPage;
import ru.citeck.ecos.records3.record.dao.query.dto.query.RecordsQuery;
import ru.citeck.ecos.records3.record.dao.query.dto.res.RecsQueryRes;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple RecordsDAO example with in-memory map-based storage.
 * DO NOT USE THIS IN PRODUCTION.
 * Warning: if you create any associations to entities in this DAO,
 * then these associations become invalid after application restart.
 * <br/>
 * This Records DAO demonstrate simplicity of base CRUD operations in Records API
 * and doesn't implement many features of other Records DAO implementations
 * (e.g. associations, content storage, permissions checking, etc.).
 */
@Component
public class DemoInMemRecordsDao extends AbstractRecordsDao
        implements RecordsQueryDao, RecordAttsDao, RecordMutateDao, RecordDeleteDao {

    public static final String ID = "demo-inmem-data";

    /**
     * Simple storage of records.
     * WARNING: All data will be lost after application restart.
     */
    private final Map<String, SimpleDto> records = new ConcurrentHashMap<>();

    /**
     * Query records. Implementation support only language 'predicate'.
     * @param recordsQuery query parameters
     * @return found records and information about total count without paging.
     */
    @Nullable
    @Override
    public RecsQueryRes<?> queryRecords(@NotNull RecordsQuery recordsQuery) {

        // You can read about predicate query language here:
        // https://citeck-ecos.readthedocs.io/ru/latest/general/%D0%AF%D0%B7%D1%8B%D0%BA_%D0%BF%D1%80%D0%B5%D0%B4%D0%B8%D0%BA%D0%B0%D1%82%D0%BE%D0%B2.html
        if (!PredicateService.LANGUAGE_PREDICATE.equals(recordsQuery.getLanguage())) {
            return null;
        }

        Predicate predicate = recordsQuery.getQuery(Predicate.class);

        QueryPage page = recordsQuery.getPage();
        List<SimpleDto> fullResult = predicateService.filterAndSort(
                records.values(),
                predicate,
                recordsQuery.getSortBy(),
                page.getSkipCount(),
                page.getMaxItems()
        );

        RecsQueryRes<SimpleDto> recsQueryRes = new RecsQueryRes<>();
        recsQueryRes.setTotalCount(records.size());
        recsQueryRes.setRecords(fullResult);

        return recsQueryRes;
    }

    /**
     * Get record data by localId.
     * @return record or null
     */
    @Nullable
    @Override
    public Object getRecordAtts(@NotNull String localId) {
        return records.get(localId);
    }

    /**
     * Creates or updates record.
     * If recordAtts.getId() is empty string, then new record with generated localId will be created.
     * @param recordAtts localId (String) and key-value map (ObjectData) with attributes of entity.
     * @return localId of existing or created record.
     */
    @NotNull
    @Override
    public String mutate(@NotNull LocalRecordAtts recordAtts) {
        SimpleDto recToMutate;
        if (recordAtts.getId().isEmpty()) {
            // Generally in other non-demo RecordsDao when getId() is empty
            // id can be specified in attributes, but here we don't implement this logic.
            // You can see source of ru.citeck.ecos.records3.record.dao.impl.mem.InMemDataRecordsDao
            // to inspect idiomatically right implementation of mutate method
            recToMutate = new SimpleDto(UUID.randomUUID().toString());
        } else {
            recToMutate = records.get(recordAtts.getId());
            if (recToMutate == null) {
                throw new IllegalArgumentException("Record with id " + recordAtts.getId() + " is not found");
            }
            recToMutate = new SimpleDto(recToMutate);
            recToMutate.modified = Instant.now();
        }
        Json.getMapper().applyData(recToMutate, recordAtts.getAtts());
        if (recToMutate.id.isBlank()) {
            throw new IllegalArgumentException("Record id is empty after mutation. Atts: " + recordAtts);
        }
        records.put(recToMutate.id, recToMutate);
        return recToMutate.id;
    }

    /**
     * Deletes specified records.
     * @param localId local identifier of deleted record.
     * @return localId of existing or created record.
     */
    @NotNull
    @Override
    public DelStatus delete(@NotNull String localId) {
        records.remove(localId);
        return DelStatus.OK;
    }

    @NotNull
    @Override
    public String getId() {
        return ID;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleDto {

        private String id;
        private String textField;
        private int numField;

        private Instant created;
        private Instant modified;

        public SimpleDto(String id) {
            this.id = id;
            created = Instant.now();
            modified = created;
        }

        public SimpleDto(SimpleDto other) {
            this.id = other.id;
            this.textField = other.textField;
            this.numField = other.numField;
            this.created = other.created;
            this.modified = other.modified;
        }

        /**
         * This method called when ?disp scalar loaded from record.
         * 'getDisplayName' is special name in DTO for '?disp' scalar.
         */
        public MLText getDisplayName() {
            Map<Locale, String> nameData = new HashMap<>();
            nameData.put(I18nContext.ENGLISH, "Demo in-mem '" + textField + "'");
            nameData.put(I18nContext.RUSSIAN, "Демо in-mem '" + textField + "'");
            return new MLText(nameData);
        }

        /**
         * This method called when '_type' attribute loaded from record.
         * 'getEcosType' is special name in DTO for '_type' attribute.
         * Engine add additional logic for this method:
         * If method result is EntityRef, or string started with 'emodel/type@',
         * then result will be EntityRef.valueOf(methodResult)
         * If method result is string and doesn't start with 'emodel/type@',
         * then engine add 'emodel/type@' prefix and return EntityRef.valueOf result.
         */
        public String getEcosType() {
            // type config: src/main/resources/eapps/artifacts/model/type/demo-inmem-type.yaml
            return "demo-inmem-type";
        }

        /**
         * Simple getter for _created attribute.
         * AttName annotation required to change default attribute name "created" to "_created".
         * '_created' is a special meta attribute for any record which should inform when record was created.
         */
        @AttName(RecordConstants.ATT_CREATED)
        public Instant getCreated() {
            return created;
        }

        /**
         * Simple getter for _modified attribute.
         * AttName annotation required to change default attribute name "modified" to "_modified".
         * '_modified' is a special meta attribute for any record which should inform when record was changed last time.
         */
        @AttName(RecordConstants.ATT_MODIFIED)
        public Instant getModified() {
            return modified;
        }
    }
}
