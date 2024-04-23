package ru.citeck.ecos.webapp.demo.commands;

import kotlin.Unit;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.commands.CommandsService;
import ru.citeck.ecos.commons.data.DataValue;
import ru.citeck.ecos.commons.data.ObjectData;
import ru.citeck.ecos.records3.record.dao.AbstractRecordsDao;
import ru.citeck.ecos.records3.record.dao.mutate.ValueMutateDao;
import ru.citeck.ecos.webapp.api.entity.EntityRef;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SendDemoCommandAction extends AbstractRecordsDao implements ValueMutateDao<SendDemoCommandAction.ActionData> {

    public static final String ID = "send-demo-command";

    private final CommandsService commandsService;

    @Nullable
    @Override
    public Object mutate(@NotNull ActionData actionData) {

        Map<String, Object> body = new HashMap<>();
        body.put("entityRef", actionData.entityRef);
        body.put("comment", actionData.comment);

        // Command execution result you can see in logs
        commandsService.execute(b -> {
            b.withTargetApp("ecos-demo-app"); // we send command to this app
            b.withBody(body); // body may be any Map or DTO object
            b.withType("demo-command"); // command executor will be selected by this type
            return Unit.INSTANCE;
        });

        return null;
    }

    @NotNull
    @Override
    public String getId() {
        return ID;
    }

    @Data
    public static class ActionData {
        private EntityRef entityRef;
        private String comment;
    }
}
