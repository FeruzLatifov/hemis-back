package uz.hemis.service.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.util.List;

/**
 * Jackson module for {@link PageImpl} deserialization from Redis cache.
 *
 * <p>{@code PageImpl} has no default constructor, so Jackson cannot deserialize it
 * without guidance. This module provides a custom deserializer that reconstructs
 * {@code PageImpl} from its serialized JSON properties.</p>
 *
 * <p>Works with {@code activateDefaultTyping(NON_FINAL, As.PROPERTY)} — typed
 * collection wrappers ({@code ["java.util.ArrayList", [...]]}) and per-item
 * {@code @class} properties are handled transparently via {@code treeToValue}.</p>
 *
 * @since 2.0.0
 */
class PageJacksonModule extends SimpleModule {

    PageJacksonModule() {
        super("PageJacksonModule");
        addDeserializer(PageImpl.class, new PageImplDeserializer());
    }

    private static class PageImplDeserializer extends JsonDeserializer<PageImpl<?>> {

        @Override
        public PageImpl<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            ObjectMapper mapper = (ObjectMapper) p.getCodec();
            JsonNode node = mapper.readTree(p);

            // Deserialize content list — treeToValue handles typed wrappers
            JsonNode contentNode = node.get("content");
            List<?> content;
            if (contentNode != null) {
                content = mapper.treeToValue(contentNode, List.class);
            } else {
                content = List.of();
            }

            int number = node.path("number").asInt(0);
            int size = node.path("size").asInt(20);
            long totalElements = node.path("totalElements").asLong(content.size());

            return new PageImpl<>(content, PageRequest.of(number, Math.max(size, 1)), totalElements);
        }
    }
}
