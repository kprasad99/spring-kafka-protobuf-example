package io.github.kprasad99.kafka.serde;

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;

public class KafkaProtobufSerde<T extends MessageLite> implements Serde<T> {

	private final Serializer<T> serializer;
	private final Deserializer<T> deserializer;

	/**
	 * Returns a new instance of {@link KafkaProtobufSerde}.
	 *
	 * @param parser The Protobuf {@link Parser}.
	 */
	public KafkaProtobufSerde() {
		serializer = new KafkaProtobufSerializer<>();
		deserializer = new KafkaProtobufDeserializer<>();
	}
	
	public KafkaProtobufSerde(Class<T> clazz) {
		serializer = new KafkaProtobufSerializer<>();
		deserializer = new KafkaProtobufDeserializer<>(clazz);
	}

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
	}

	@Override
	public void close() {
	}

	@Override
	public Serializer<T> serializer() {
		return serializer;
	}

	@Override
	public Deserializer<T> deserializer() {
		return deserializer;
	}
}
