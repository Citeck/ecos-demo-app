package ru.citeck.ecos.webapp.demo.events;

import kotlin.Unit;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.events2.EventsService;
import ru.citeck.ecos.events2.type.RecordChangedEvent;
import ru.citeck.ecos.events2.type.RecordCreatedEvent;
import ru.citeck.ecos.records2.predicate.model.Predicate;
import ru.citeck.ecos.records2.predicate.model.Predicates;
import ru.citeck.ecos.records3.record.atts.schema.annotation.AttName;
import ru.citeck.ecos.webapp.api.entity.EntityRef;

import javax.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public class DemoEcosEventListener {

    private final EventsService eventsService;

    @PostConstruct
    public void init() {

        Predicate filter = Predicates.and(
            // For transactional listeners filtering by type is very important
            // to avoid unnecessary events emitting
            Predicates.eq("typeDef.id", "demo-type"),
            Predicates.contains("record.textField", "error")
        );

        eventsService.<UserCreatedOrUpdatedEventAtts>addListener(builder -> {

            // Event type
            //
            // Frequently used event types:
            // ru.citeck.ecos.events2.type.RecordChangedEvent.TYPE ("record-changed")
            // ru.citeck.ecos.events2.type.RecordDeletedEvent.TYPE ("record-deleted")
            // ru.citeck.ecos.events2.type.RecordStatusChangedEvent.TYPE ("record-status-changed")
            // ru.citeck.ecos.events2.type.RecordDraftStatusChangedEvent.TYPE ("record-draft-status-changed")
            // ru.citeck.ecos.events2.type.RecordCreatedEvent.TYPE ("record-created")
            // ru.citeck.ecos.events2.type.RecordContentChangedEvent.TYPE ("record-content-changed")
            //
            // Provided attributes for this event types can be found in classes above
            builder.withEventType(RecordCreatedEvent.TYPE);

            // Data class define DTO with attributes which should be loaded from event and sent to out listener
            builder.withDataClass(UserCreatedOrUpdatedEventAtts.class);

            // Transactional flag give a lot of power to our listener:
            // 1. Listener called immediately after event occurred
            // 2. If we throw error in listener, then transaction will be rolled back
            // but this flag also has drawbacks:
            // 1. If our app doesn't run and event occurred, then transaction will always be completed with error
            // 2. If our listener do some complex work, then system responsiveness will be bad
            //
            // If we chose transactional=false, then listener will be called asynchronously
            // after transaction will be committed
            builder.withTransactional(true);

            // 'J' at the end of method name mean 'Java'.
            // API methods without postfix in ECOS primary designed for using in Kotlin.
            // withAction define method which should be called when event occurred.
            builder.withActionJ(this::processCreatedOrUpdatedEvent);

            // Filter check any data in event instantly when event occurred.
            // If filter doesn't match, then event won't be emitted.
            builder.withFilter(filter);
            return Unit.INSTANCE;
        });

        // Add listener to changed event too
        eventsService.<UserCreatedOrUpdatedEventAtts>addListener(builder -> {
            builder.withEventType(RecordChangedEvent.TYPE);
            builder.withDataClass(UserCreatedOrUpdatedEventAtts.class);
            builder.withTransactional(true);
            builder.withActionJ(this::processCreatedOrUpdatedEvent);
            builder.withFilter(filter);
            return Unit.INSTANCE;
        });
    }

    private void processCreatedOrUpdatedEvent(UserCreatedOrUpdatedEventAtts event) {
        log.warn("Process created or updated event for record " + event.entityRef + ". TextField: " + event.textField);
        throw new RuntimeException("You can't write 'error' in text field. Current value: '" + event.textField + "'");
    }

    @Data
    public static class UserCreatedOrUpdatedEventAtts {
        @AttName("record?id")
        private EntityRef entityRef;
        @AttName("record.textField")
        private String textField;
    }
}
