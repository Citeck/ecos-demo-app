package ru.citeck.ecos.webapp.demo.actions;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.context.lib.auth.AuthContext;
import ru.citeck.ecos.model.lib.authorities.AuthorityType;
import ru.citeck.ecos.notifications.lib.Notification;
import ru.citeck.ecos.notifications.lib.NotificationType;
import ru.citeck.ecos.notifications.lib.service.NotificationService;
import ru.citeck.ecos.records3.record.dao.AbstractRecordsDao;
import ru.citeck.ecos.records3.record.dao.mutate.ValueMutateDao;
import ru.citeck.ecos.webapp.api.constants.AppName;
import ru.citeck.ecos.webapp.api.entity.EntityRef;

import java.util.HashMap;
import java.util.Map;

/**
 * Records DAO to execute action
 * Manual calling from UI code:
 * // 'ecos-demo-app' - appName, 'send-demo-email' - identifier of RecordsDAO (see SendDemoEmailAction.ID)
 * let rec = Records.getRecordToEdit('ecos-demo-app/send-demo-email@');
 * rec.att('entityRef', 'emodel/demo-type@629fbd31-788a-4232-9de9-d737e5b07795'); // any EntityRef
 * rec.att('comment', 'any comment');
 * await rec.save();
 */
@Component
@RequiredArgsConstructor
public class SendDemoEmailAction extends AbstractRecordsDao implements ValueMutateDao<SendDemoEmailAction.ActionData> {

    /**
     * RecordsDAO identifier. Used to identify which DAO should handle mutation request.
     * This is second part of EntityRef after '/' and before '@' in example above.
     * In API requests this ID combined with appName called sourceId. Example: 'ecos-demo-app/send-demo-email'
     */
    public static final String ID = "send-demo-email";

    /**
     * Email template ref
     * Loaded from src/main/resources/eapps/artifacts/notification/template/demo-email.html.ftl
     */
    private static final EntityRef TEMPLATE_REF = EntityRef.create(
            AppName.NOTIFICATIONS,
            "template",
            "demo-email"
    );

    private final NotificationService notificationService;

    @Nullable
    @Override
    public Object mutate(@NotNull ActionData actionData) {

        String currentUser = AuthContext.getCurrentUser();
        EntityRef currentUserRef = AuthorityType.PERSON.getRef(currentUser);
        String email = recordsService.getAtt(currentUserRef, "email").asText();
        if (StringUtils.isBlank(email)) {
            throw new RuntimeException("Current user doesn't have email. Please open user profile and change it");
        }

        // Additional meta may be used to add custom data while notification sending
        // Notification template may load any value from this data using '$' as prefix before key
        // For example:
        // Template model = {"anyAliasWhichCanBeUsedInFtlTemplate": "$additionalStr"}
        // Ftl template   = "Some text ${anyAliasWhichCanBeUsedInFtlTemplate}"
        // Result         = "Some text additional-string-value"
        Map<String, Object> additionalMeta = new HashMap<>();
        additionalMeta.put("additionalStr", "additional-string-value");
        // Variables may contain simple scalars (string/number/boolean/etc.) or references to any entities in system
        // We add here reference to current user for example.
        additionalMeta.put("additionalUserRef", EntityRef.create(AppName.EMODEL, "person", currentUser));
        // DTO values also can be used and template may extract data from it
        additionalMeta.put("actionData", actionData);

        // NotificationService used to manual sending of notifications
        // notification template define model with attributes which should be loaded from record and additionalMeta
        // Under the hood this service works in following way:
        // 1. Load attributes list required for provided templateRef
        // 2. Load required attributes from provided record and additionalMeta
        // 3. Send command with loaded data to notifications app through rabbitmq
        // 'send' method doesn't wait until message will be really sent
        notificationService.send(new Notification.Builder()
                .addRecipient(email)
                .record(actionData.entityRef)
                .notificationType(NotificationType.EMAIL_NOTIFICATION)
                .additionalMeta(additionalMeta)
                .templateRef(TEMPLATE_REF)
                .build());

        // with default settings you can see sent email in mailhog - http://localhost:8025/
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
