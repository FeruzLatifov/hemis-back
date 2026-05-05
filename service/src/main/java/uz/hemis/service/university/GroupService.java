package uz.hemis.service.university;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.audit.AuditAction;
import uz.hemis.common.audit.Audited;
import uz.hemis.common.dto.university.GroupDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.student.Group;
import uz.hemis.service.security.TenantGuard;
import uz.hemis.service.university.mapper.GroupMapper;
import uz.hemis.domain.repository.GroupRepository;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;
    private final TenantGuard tenantGuard;

    @Audited(action = AuditAction.CREATE, entity = "Group", entityClass = Group.class)
    @Transactional
    public GroupDto create(GroupDto dto) {
        // OWASP A01 — caller cannot create Group for foreign OTM (mass-assignment).
        if (dto.getUniversity() != null) {
            tenantGuard.verifyOwnershipOrAdmin(dto.getUniversity());
        }
        Group group = groupMapper.toEntity(dto);
        group.setId(UUID.randomUUID());
        return groupMapper.toDto(groupRepository.save(group));
    }

    public GroupDto findById(UUID id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id));
        // OWASP A01 BOLA — caller must own the group's university.
        if (group.getUniversity() != null) {
            tenantGuard.verifyOwnershipOrAdmin(group.getUniversity());
        }
        return groupMapper.toDto(group);
    }

    public Page<GroupDto> findAll(Pageable pageable) {
        return groupRepository.findAll(pageable).map(groupMapper::toDto);
    }

    @Audited(action = AuditAction.UPDATE, entity = "Group", entityClass = Group.class, keyArg = "id")
    @Transactional
    public GroupDto update(UUID id, GroupDto dto) {
        Group existing = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id));
        // OWASP A01 BOLA — caller must own the group's university.
        if (existing.getUniversity() != null) {
            tenantGuard.verifyOwnershipOrAdmin(existing.getUniversity());
        }
        // Mass-assignment defense — body cannot relocate group to foreign OTM.
        String originalUniversity = existing.getUniversity();
        groupMapper.updateEntityFromDto(dto, existing);
        existing.setUniversity(originalUniversity); // pin to original
        return groupMapper.toDto(groupRepository.save(existing));
    }

    @Audited(action = AuditAction.DELETE, entity = "Group", entityClass = Group.class, keyArg = "id")
    @Transactional
    public void delete(UUID id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id));
        // OWASP A01 BOLA — caller must own the group's university.
        if (group.getUniversity() != null) {
            tenantGuard.verifyOwnershipOrAdmin(group.getUniversity());
        }
        // Group entity has no soft-delete column — fallback to active=false (soft-disable).
        // Hard delete keep audit trail blind; active=false preserves historical references.
        group.setActive(false);
        groupRepository.save(group);
    }
}
