package net.sakurain.mc.aeternumgenesis.api.exception;

import org.jetbrains.annotations.NotNull;

public class TemplateNotFoundException extends AeternumGenesisAPIException {

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
