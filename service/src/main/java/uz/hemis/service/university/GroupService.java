package uz.hemis.service.university;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.GroupDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.Group;
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

    @Transactional
    public GroupDto create(GroupDto dto) {
        Group group = groupMapper.toEntity(dto);
        group.setId(UUID.randomUUID());
        return groupMapper.toDto(groupRepository.save(group));
    }

    public GroupDto findById(UUID id) {
        return groupRepository.findById(id)
                .map(groupMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id));
    }

    public Page<GroupDto> findAll(Pageable pageable) {
        return groupRepository.findAll(pageable).map(groupMapper::toDto);
    }

    @Transactional
    public GroupDto update(UUID id, GroupDto dto) {
        Group existing = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id));
        groupMapper.updateEntityFromDto(dto, existing);
        return groupMapper.toDto(groupRepository.save(existing));
    }

    @Transactional
    public void delete(UUID id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id));
        groupRepository.delete(group);
    }
}
