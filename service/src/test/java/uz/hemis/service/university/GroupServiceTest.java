package uz.hemis.service.university;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uz.hemis.common.dto.university.GroupDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.Group;
import uz.hemis.domain.repository.GroupRepository;
import uz.hemis.service.university.mapper.GroupMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GroupService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>create - creates group and returns DTO</li>
 *   <li>findById - found and not found cases</li>
 *   <li>findAll - paginated results</li>
 *   <li>update - updates existing group, throws on not found</li>
 *   <li>delete - deletes existing group, throws on not found</li>
 * </ul>
 *
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GroupService Unit Tests")
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMapper groupMapper;

    @InjectMocks
    private GroupService groupService;

    private UUID groupId;
    private Group groupEntity;
    private GroupDto groupDto;

    @BeforeEach
    void setUp() {
        groupId = UUID.randomUUID();

        groupEntity = new Group();
        groupEntity.setId(groupId);
        groupEntity.setUniversity("UNI_001");
        groupEntity.setEducationType("11");
        groupEntity.setEducationYear("2024");
        groupEntity.setGroupId("GRP_101");
        groupEntity.setGroupName("Computer Science 101");
        groupEntity.setActive(true);

        groupDto = new GroupDto();
        groupDto.setId(groupId);
        groupDto.setUniversity("UNI_001");
        groupDto.setEducationType("11");
        groupDto.setEducationYear("2024");
        groupDto.setGroupId("GRP_101");
        groupDto.setGroupName("Computer Science 101");
        groupDto.setActive(true);
    }

    // =====================================================
    // create tests
    // =====================================================

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("creates group and returns DTO with new UUID")
        void createsGroupAndReturnsDto() {
            // Given
            Group mappedEntity = new Group();
            when(groupMapper.toEntity(groupDto)).thenReturn(mappedEntity);
            when(groupRepository.save(mappedEntity)).thenReturn(groupEntity);
            when(groupMapper.toDto(groupEntity)).thenReturn(groupDto);

            // When
            GroupDto result = groupService.create(groupDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getGroupName()).isEqualTo("Computer Science 101");
            assertThat(result.getUniversity()).isEqualTo("UNI_001");

            verify(groupMapper).toEntity(groupDto);
            verify(groupRepository).save(mappedEntity);
            verify(groupMapper).toDto(groupEntity);
        }
    }

    // =====================================================
    // findById tests
    // =====================================================

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("returns group DTO when found")
        void returnsGroupDtoWhenFound() {
            // Given
            when(groupRepository.findById(groupId)).thenReturn(Optional.of(groupEntity));
            when(groupMapper.toDto(groupEntity)).thenReturn(groupDto);

            // When
            GroupDto result = groupService.findById(groupId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(groupId);
            assertThat(result.getGroupName()).isEqualTo("Computer Science 101");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when group not found")
        void throwsNotFoundWhenMissing() {
            // Given
            UUID unknownId = UUID.randomUUID();
            when(groupRepository.findById(unknownId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> groupService.findById(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Group not found");
        }
    }

    // =====================================================
    // findAll tests
    // =====================================================

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("returns paginated group DTOs")
        void returnsPaginatedGroupDtos() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Group> entityPage = new PageImpl<>(List.of(groupEntity), pageable, 1);
            when(groupRepository.findAll(pageable)).thenReturn(entityPage);
            when(groupMapper.toDto(groupEntity)).thenReturn(groupDto);

            // When
            Page<GroupDto> result = groupService.findAll(pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getGroupName()).isEqualTo("Computer Science 101");
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("returns empty page when no groups exist")
        void returnsEmptyPageWhenNoGroups() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Group> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(groupRepository.findAll(pageable)).thenReturn(emptyPage);

            // When
            Page<GroupDto> result = groupService.findAll(pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    // =====================================================
    // update tests
    // =====================================================

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("updates existing group and returns updated DTO")
        void updatesExistingGroupAndReturnsDto() {
            // Given
            GroupDto updateDto = new GroupDto();
            updateDto.setGroupName("Updated Group Name");

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(groupEntity));
            when(groupRepository.save(groupEntity)).thenReturn(groupEntity);
            when(groupMapper.toDto(groupEntity)).thenReturn(groupDto);

            // When
            GroupDto result = groupService.update(groupId, updateDto);

            // Then
            assertThat(result).isNotNull();
            verify(groupMapper).updateEntityFromDto(updateDto, groupEntity);
            verify(groupRepository).save(groupEntity);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when updating non-existent group")
        void throwsNotFoundWhenUpdatingMissing() {
            // Given
            UUID unknownId = UUID.randomUUID();
            when(groupRepository.findById(unknownId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> groupService.update(unknownId, groupDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Group not found");

            verify(groupRepository, never()).save(any());
        }
    }

    // =====================================================
    // delete tests
    // =====================================================

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deletes existing group successfully")
        void deletesExistingGroup() {
            // Given
            when(groupRepository.findById(groupId)).thenReturn(Optional.of(groupEntity));

            // When
            groupService.delete(groupId);

            // Then
            verify(groupRepository).delete(groupEntity);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when deleting non-existent group")
        void throwsNotFoundWhenDeletingMissing() {
            // Given
            UUID unknownId = UUID.randomUUID();
            when(groupRepository.findById(unknownId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> groupService.delete(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Group not found");

            verify(groupRepository, never()).delete(any());
        }
    }
}
