package com.restkeeper.shop.serialization;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.serializers.JavaSerializer;

public class CustomKryoFactory implements KryoFactory {

    @Override
    public Kryo create() {
        Kryo kryo = new Kryo();
        // 在 Kryo 中注册 `StackTraceElement` 自定义序列化器

        kryo.register(StackTraceElement.class, new JavaSerializer());
        return kryo;
    }
}
