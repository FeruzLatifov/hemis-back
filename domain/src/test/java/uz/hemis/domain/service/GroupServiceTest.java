package uz.hemis.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import uz.hemis.common.dto.GroupDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.Group;
import uz.hemis.service.mapper.GroupMapper;
import uz.hemis.domain.repository.GroupRepository;
import uz.hemis.service.GroupService;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock private GroupRepository groupRepository;
    @Mock private GroupMapper groupMapper;
    @InjectMocks private GroupService groupService;

    private Group testGroup;
    private GroupDto testGroupDto;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testGroup = new Group();
        testGroup.setId(testId);
        testGroup.setGroupName("CS-101");
        testGroup.setUniversity("UNI001");
        testGroup.setActive(true);

        testGroupDto = new GroupDto();
        testGroupDto.setId(testId);
        testGroupDto.setGroupName("CS-101");
        testGroupDto.setUniversity("UNI001");
        testGroupDto.setActive(true);
    }

    @Test
    void findById_WithExistingId_ShouldReturnGroup() {
        when(groupRepository.findById(testId)).thenReturn(Optional.of(testGroup));
        when(groupMapper.toDto(testGroup)).thenReturn(testGroupDto);

        GroupDto result = groupService.findById(testId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testId);
    }

    @Test
    void findById_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        when(groupRepository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.findById(testId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAll_ShouldReturnPageOfGroups() {
        Page<Group> page = new PageImpl<>(Arrays.asList(testGroup));
        when(groupRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(groupMapper.toDto(testGroup)).thenReturn(testGroupDto);

        Page<GroupDto> result = groupService.findAll(PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void update_WithValidData_ShouldReturnUpdatedGroup() {
        when(groupRepository.findById(testId)).thenReturn(Optional.of(testGroup));
        when(groupRepository.save(testGroup)).thenReturn(testGroup);
        when(groupMapper.toDto(testGroup)).thenReturn(testGroupDto);

        GroupDto result = groupService.update(testId, new GroupDto());

        assertThat(result).isNotNull();
        verify(groupRepository).save(testGroup);
    }
}
