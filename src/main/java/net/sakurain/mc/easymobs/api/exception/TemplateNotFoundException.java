package net.sakurain.mc.easymobs.api.exception;

import org.jetbrains.annotations.NotNull;

public class TemplateNotFoundException extends EasyMobsAPIException {

    private final String templateId;

    public TemplateNotFoundException(@NotNull String templateId) {
        super("Template not found: " + templateId);
        this.templateId = templateId;
    }

    @NotNull
    public String getTemplateId() {
        return templateId;
    }
}
