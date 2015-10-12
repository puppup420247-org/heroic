package com.spotify.heroic.shell.protocol;

import eu.toolchain.serializer.AutoSerialize;
import lombok.Data;

@AutoSerialize
@Data
public class CommandsRequest implements Message {
    @Override
    public <R> R visit(Visitor<R> visitor) throws Exception {
        return visitor.visitCommandsRequest(this);
    }
}