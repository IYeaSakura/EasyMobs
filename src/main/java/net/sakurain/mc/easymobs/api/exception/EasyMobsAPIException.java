package net.sakurain.mc.easymobs.api.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EasyMobsAPIException extends RuntimeException {

    public EasyMobsAPIException(@NotNull String message) {
        super(message);
    }

    public EasyMobsAPIException(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
