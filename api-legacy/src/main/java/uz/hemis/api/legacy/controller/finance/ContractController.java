package uz.hemis.api.legacy.controller.finance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.finance.ContractService;
import uz.hemis.common.dto.finance.ContractDto;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.service.util.PageResponses;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.List;
import java.util.UUID;

@Tag(name = "57.Contract", description = "Shartnomalar boshqaruvi")
@RestController
@RequestMapping("/app/rest/v2/contracts")
@RequiredArgsConstructor
@Slf4j
public class ContractController {

    private final ContractService contractService;

    @Operation(summary = "Barcha shartnomalar (paginated)")
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<ResponseWrapper<PageResponse<ContractDto>>> getAllContracts(
            @PageableDefault(size = 20, sort = "contractDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ContractDto> contracts = contractService.findAll(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(contracts)));
    }

    @Operation(summary = "Shartnomani ID bo'yicha topish")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ContractDto>> getContractById(@PathVariable UUID id) {
        ContractDto contract = contractService.findById(id);
        return ResponseEntity.ok(ResponseWrapper.success(contract));
    }

    @Operation(summary = "Shartnomani nomer bo'yicha topish")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/number/{number}")
    public ResponseEntity<ResponseWrapper<ContractDto>> getContractByNumber(@PathVariable String number) {
        ContractDto contract = contractService.findByContractNumber(number);
        return ResponseEntity.ok(ResponseWrapper.success(contract));
    }

    @Operation(summary = "OTM bo'yicha shartnomalar (paginated)")
    @PreAuthorize("isAuthenticated()")
    @GetMapping(params = "university")
    public ResponseEntity<ResponseWrapper<PageResponse<ContractDto>>> getContractsByUniversity(
            @RequestParam("university") String universityCode,
            @PageableDefault(size = 20, sort = "contractDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ContractDto> contracts = contractService.findByUniversity(universityCode, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(PageResponses.from(contracts)));
    }

    @Operation(summary = "Talaba bo'yicha shartnomalar (paginatsiyasiz)")
    @PreAuthorize("isAuthenticated()")
    @GetMapping(params = "student")
    public ResponseEntity<ResponseWrapper<List<ContractDto>>> getContractsByStudent(
            @RequestParam("student") UUID studentId
    ) {
        List<ContractDto> contracts = contractService.findByStudent(studentId);
        return ResponseEntity.ok(ResponseWrapper.success(contracts));
    }

    @Operation(summary = "Yangi shartnoma yaratish")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseWrapper<ContractDto>> createContract(@Valid @RequestBody ContractDto contractDto) {
        ContractDto created = contractService.create(contractDto);
        return ResponseEntity.ok(ResponseWrapper.success(created));
    }

    @Operation(summary = "Shartnomani yangilash")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseWrapper<ContractDto>> updateContract(
            @PathVariable UUID id,
            @Valid @RequestBody ContractDto contractDto
    ) {
        ContractDto updated = contractService.update(id, contractDto);
        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }
}
