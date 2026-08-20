package uz.hemis.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.service.integration.model.GatewayResult;

import java.util.Map;

/**
 * Gateway Service — tashqi tizimlardan (api-mspd) olingan ma'lumotlarni
 * universitet tarafga passthrough qiladi (status code va body o'zgarmasdan).
 * DB ga saqlamaydi.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GatewayService {

    private final ApiMspdClient apiMspdClient;

    /**
     * Kadastr raqami bo'yicha tashqi api-mspd dan ma'lumot oladi.
     * Tashqi API'ning status code va response body shundayligicha qaytariladi.
     * POST /kadastr/by-cadnum  body: {"cad_num":"..."}
     */
    public GatewayResult getCadastreByCadnum(String cadNumber) {
        if (cadNumber == null || cadNumber.isBlank()) {
            throw new BadRequestException("cadNumber must not be blank");
        }
        return apiMspdClient.post("/kadastr/by-cadnum", Map.of("cad_num", cadNumber));
    }

    /**
     * INN (TIN) bo'yicha kadastr obyektlari RO'YXATI (cad_number'lar).
     * Javob {@code cadastr_list} + {@code cadastr_count} — to'liq detal emas;
     * har cad_number bo'yicha {@link #getCadastreByCadnum} chaqiriladi (ingest).
     */
    public GatewayResult getCadastreByInn(String tin) {
        if (tin == null || tin.isBlank()) {
            throw new BadRequestException("tin must not be blank");
        }
        return apiMspdClient.post("/kadastr/by-inn", Map.of("tin", tin));
    }
}
