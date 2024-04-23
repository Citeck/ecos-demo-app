package ru.citeck.ecos.webapp.demo.commands;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.commands.CommandExecutor;
import ru.citeck.ecos.commands.annotation.CommandType;
import ru.citeck.ecos.webapp.api.entity.EntityRef;

/**
 * Commands primary used for async messaging between applications.
 * This demo executor is not very useful, but required to understand base concepts.
 * Command type will be calculated from annotation CommandType on generic type of CommandExecutor.
 */
@Slf4j
@Component
public class DemoCommandExecutor implements CommandExecutor<DemoCommandExecutor.DemoCommandDto> {

    public static final String TYPE = "demo-command";

    @Nullable
    @Override
    public Object execute(DemoCommandDto demoCommandDto) {
        log.info("Command received: " + demoCommandDto);
        return null;
    }

    @Data
    @CommandType(TYPE)
    public static class DemoCommandDto {
        private EntityRef entityRef;
        private String comment;
    }
}
