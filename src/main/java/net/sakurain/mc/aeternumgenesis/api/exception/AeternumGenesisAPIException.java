package net.sakurain.mc.aeternumgenesis.api.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AeternumGenesisAPIException extends RuntimeException {

    public AeternumGenesisAPIException(@NotNull String message) {
        super(message);
    }

    public AeternumGenesisAPIException(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
