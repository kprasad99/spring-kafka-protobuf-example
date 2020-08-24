package io.github.kprasad99.kafka.serde;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaProtobufDeserializer<T extends MessageLite> implements Deserializer<T> {

	private final Class<T> clazz;
	private final ConcurrentHashMap<String, Class<?>> registeredClazz;

	public KafkaProtobufDeserializer() {
		this(null);
	}

	/**
	 * Returns a new instance of {@link KafkaProtobufDeserializer}.
	 *
	 * @param parser The Protobuf {@link Parser}.
	 */
	public KafkaProtobufDeserializer(@Nullable Class<T> clazz) {
		this.clazz = clazz;
		this.registeredClazz = new ConcurrentHashMap<>();
	}

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public T deserialize(String topic, byte[] data) {
		log.info("called here");
		try {
			if (clazz == null) {
				throw new Exception("No target type provided");
			}
			Method m = ReflectionUtils.findMethod(clazz, "parseFrom", byte[].class);
			if (null == m) {
				throw new Exception(String.format("The message class [%s] must have a parseFrom(byte[] bytes) method",
						clazz.getName()));
			}
			return (T) m.invoke(null, (Object) data);
		} catch (InvalidProtocolBufferException e) {
			throw new SerializationException("Error deserializing from Protobuf message", e);
		} catch (Exception e) {
			throw new SerializationException("Error deserializing from Protobuf message", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public T deserialize(String topic, Headers headers, byte[] data) {
		try {
			Method m = null;
			if (clazz != null) {
				m = ReflectionUtils.findMethod(clazz, "parseFrom", byte[].class);
			} else {
				Header header = headers.lastHeader("com.google.protobuf.type");
				if (header != null) {
					String type = new String(header.value());
					if(!registeredClazz.containsKey(type)) {
						registeredClazz.put(type, ClassUtils.forName(type, getClass().getClassLoader()));
					}
					Class<?> clazz = registeredClazz.get(type);
					if (!MessageLite.class.isAssignableFrom(clazz)) {
						throw new Exception(String.format("The provided [%s] is not a subtype of protobuf.Message.",
								clazz.getName()));
					}
					m = ReflectionUtils.findMethod(clazz, "parseFrom", byte[].class);
				}
				if (null == m) {
					throw new Exception(String.format(
							"The message class [%s] must have a parseFrom(byte[] bytes) method", clazz.getName()));
				}
			}
			return (T) m.invoke(null, (Object) data);
		} catch (InvalidProtocolBufferException e) {
			throw new SerializationException("Error deserializing from Protobuf message", e);
		} catch (Exception e) {
			throw new SerializationException("Error deserializing from Protobuf message", e);
		}
	}

	@Override
	public void close() {
	}
}
