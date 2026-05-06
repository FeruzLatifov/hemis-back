package uz.hemis.service.integration.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Tashqi API javobining passthrough konteyneri — HTTP status va body
 * o'zgarmasdan controller'ga uzatish uchun.
 */
public record GatewayResult(int statusCode, JsonNode body) {
}
