package io.github.kprasad99.kafka.serde;

import java.util.Map;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

import com.google.protobuf.MessageLite;

public class KafkaProtobufSerializer<T extends MessageLite> implements Serializer<T> {
	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
	}

	@Override
	public byte[] serialize(String topic, T data) {
		return data.toByteArray();
	}
	@Override
	public byte[] serialize(String topic, Headers headers, T data) {
		headers.add("com.google.protobuf.type", data.getClass().getName().getBytes());
		return Serializer.super.serialize(topic, headers, data);
	}

	@Override
	public void close() {
	}
}
