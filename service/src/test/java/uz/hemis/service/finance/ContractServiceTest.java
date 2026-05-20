package uz.hemis.service.finance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import uz.hemis.common.dto.finance.ContractDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.finance.Contract;
import uz.hemis.domain.repository.ContractRepository;
import uz.hemis.service.finance.mapper.ContractMapper;
import uz.hemis.service.security.TenantGuard;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContractService — money + tenant guard")
class ContractServiceTest {

    @Mock private ContractRepository repository;
    @Mock private ContractMapper mapper;
    @Mock private TenantGuard tenantGuard;

    @InjectMocks
    private ContractService service;

    private UUID id;
    private Contract entity;
    private ContractDto dto;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        entity = new Contract();
        entity.setId(id);
        entity.setUniversity("337");
        entity.setContractNumber("C-001");
        dto = new ContractDto();
        dto.setId(id);
        dto.setUniversity("337");
        dto.setContractNumber("C-001");
    }

    @Test
    void findById_callsTenantGuard() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        service.findById(id);

        verify(tenantGuard).verifyOwnershipOrAdmin("337");
    }

    @Test
    void findById_notFound() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByContractNumber_tenantGuard() {
        when(repository.findByContractNumber("C-001")).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        service.findByContractNumber("C-001");

        verify(tenantGuard).verifyOwnershipOrAdmin("337");
    }

    @Test
    void findAll_adminBypass() {
        when(repository.findAll(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        service.findAll(PageRequest.of(0, 10));

        verify(tenantGuard).verifyOwnershipOrAdmin(null);
    }

    @Test
    void findByUniversity_tenantGuard() {
        when(repository.findByUniversity("337", PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        service.findByUniversity("337", PageRequest.of(0, 10));

        verify(tenantGuard).verifyOwnershipOrAdmin("337");
    }

    @Test
    void findByStudent_noTenantGuard() {
        UUID sid = UUID.randomUUID();
        when(repository.findByStudent(sid)).thenReturn(List.of(entity));
        when(mapper.toDtoList(List.of(entity))).thenReturn(List.of(dto));

        assertThat(service.findByStudent(sid)).containsExactly(dto);
    }

    @Test
    void sumContractByUniversityAndYear_nullDefaults_returnsZero() {
        when(repository.sumContractByUniversityAndYear("337", "2026")).thenReturn(null);

        assertThat(service.sumContractByUniversityAndYear("337", "2026"))
                .isEqualByComparingTo(BigDecimal.ZERO);
        verify(tenantGuard).verifyOwnershipOrAdmin("337");
    }

    @Test
    void sumPaid_returnsValue() {
        when(repository.sumPaidByUniversityAndYear("337", "2026"))
                .thenReturn(new BigDecimal("1500.50"));

        assertThat(service.sumPaidByUniversityAndYear("337", "2026"))
                .isEqualByComparingTo(new BigDecimal("1500.50"));
    }

    @Test
    void create_happyPath() {
        when(repository.existsByContractNumber("C-001")).thenReturn(false);
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        service.create(dto);

        verify(tenantGuard).verifyOwnershipOrAdmin("337");
        verify(repository).save(entity);
    }

    @Test
    void create_duplicateContractNumber_throws() {
        when(repository.existsByContractNumber("C-001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void update_massAssignmentProtection_universityCleared() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        ContractDto patch = new ContractDto();
        patch.setUniversity("999"); // attempt cross-tenant relocate

        service.update(id, patch);

        // university maydoni null'ga reset — mapper buni qabul qiladi (mass-assignment defense)
        assertThat(patch.getUniversity()).isNull();
        verify(mapper).updateEntityFromDto(patch, entity);
        verify(tenantGuard).verifyOwnershipOrAdmin("337");
    }

    @Test
    void softDelete_alreadyDeleted_noSave() {
        entity.setDeleteTs(java.time.LocalDateTime.now());
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        service.softDelete(id);

        verify(repository, never()).save(any());
    }
}
