package com.restkeeper.shop.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class StackTraceElementSerializer extends Serializer<StackTraceElement> {

    @Override
    public void write(Kryo kryo, Output output, StackTraceElement element) {
        // 序列化 `StackTraceElement` 的字段
        output.writeString(element.getClassName());
        output.writeString(element.getMethodName());
        output.writeString(element.getFileName());
        output.writeInt(element.getLineNumber());
    }

    @Override
    public StackTraceElement read(Kryo kryo, Input input, Class<StackTraceElement> type) {
        // 反序列化 `StackTraceElement`
        String className = input.readString();
        String methodName = input.readString();
        String fileName = input.readString();
        int lineNumber = input.readInt();
        return new StackTraceElement(className, methodName, fileName, lineNumber);
    }
}
