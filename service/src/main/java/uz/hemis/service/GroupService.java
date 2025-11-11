package uz.hemis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.GroupDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Group;
import uz.hemis.domain.mapper.GroupMapper;
import uz.hemis.domain.repository.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;
    private final UniversityRepository universityRepository;
    private final SpecialtyRepository specialtyRepository;
    private final FacultyRepository facultyRepository;

    @Transactional
    public GroupDto create(GroupDto dto) {
        validateForCreate(dto);
        Group group = groupMapper.toEntity(dto);
        return groupMapper.toDto(groupRepository.save(group));
    }

    private void validateForCreate(GroupDto dto) {
        Map<String, String> errors = new HashMap<>();
        if (dto.getName() != null && groupRepository.existsByName(dto.getName())) {
            errors.put("name", "Group name already exists");
        }
        if (dto.getUniversity() != null && !universityRepository.existsByCode(dto.getUniversity())) {
            errors.put("university", "University not found");
        }
        if (dto.getSpecialty() != null && !specialtyRepository.existsById(dto.getSpecialty())) {
            errors.put("specialty", "Specialty not found");
        }
        if (dto.getFaculty() != null && !facultyRepository.existsById(dto.getFaculty())) {
            errors.put("faculty", "Faculty not found");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Group validation failed", errors);
        }
    }

    public GroupDto findById(UUID id) {
        return groupRepository.findById(id)
                .map(groupMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id));
    }

    public Page<GroupDto> findAll(Pageable pageable) {
        return groupRepository.findAll(pageable).map(groupMapper::toDto);
    }

    public Page<GroupDto> findByUniversity(String universityCode, Pageable pageable) {
        return groupRepository.findByUniversity(universityCode, pageable).map(groupMapper::toDto);
    }

    public Page<GroupDto> findBySpecialty(UUID specialtyId, Pageable pageable) {
        return groupRepository.findBySpecialty(specialtyId, pageable).map(groupMapper::toDto);
    }

    @Transactional
    public GroupDto update(UUID id, GroupDto dto) {
        Group existing = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id));
        groupMapper.updateEntityFromDto(dto, existing);
        return groupMapper.toDto(groupRepository.save(existing));
    }

    @Transactional
    public void softDelete(UUID id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id));
        group.setDeleteTs(LocalDateTime.now());
        groupRepository.save(group);
    }

    public long countByUniversity(String universityCode) {
        return groupRepository.countByUniversity(universityCode);
    }
}
