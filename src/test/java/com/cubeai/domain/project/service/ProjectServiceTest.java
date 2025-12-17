package com.cubeai.domain.project.service;

import com.cubeai.domain.curriculum.entity.Curriculum;
import com.cubeai.domain.curriculum.repository.CurriculumRepository;
import com.cubeai.domain.member.entity.Member;
import com.cubeai.domain.member.repository.MemberRepository;
import com.cubeai.domain.project.dto.request.ProjectCreateRequest;
import com.cubeai.domain.project.dto.request.ProjectSaveRequest;
import com.cubeai.domain.project.dto.response.ProjectHistoryResponse;
import com.cubeai.domain.project.dto.response.ProjectResponse;
import com.cubeai.domain.project.entity.Project;
import com.cubeai.domain.project.entity.ProjectHistory;
import com.cubeai.domain.project.repository.ProjectHistoryRepository;
import com.cubeai.domain.project.repository.ProjectRepository;
import com.cubeai.global.error.ErrorCode;
import com.cubeai.global.error.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectHistoryRepository projectHistoryRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CurriculumRepository curriculumRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void createProject_saves_project_for_member_and_curriculum() {
        Long memberId = 1L;
        Long curriculumId = 2L;
        Member member = member(memberId);
        Curriculum curriculum = curriculum(curriculumId);
        ProjectCreateRequest request = new ProjectCreateRequest(curriculumId);
        Project savedProject = Project.builder()
                .member(member)
                .curriculum(curriculum)
                .build();
        ReflectionTestUtils.setField(savedProject, "projectId", 10L);

        when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
        when(curriculumRepository.findByCurriculumId(curriculumId)).thenReturn(Optional.of(curriculum));
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        ProjectResponse response = projectService.createProject(memberId, request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.curriculumId()).isEqualTo(curriculumId);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void createProject_throws_when_member_missing() {
        when(memberRepository.findByMemberId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.createProject(99L, new ProjectCreateRequest(1L)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    void saveProject_persists_history() {
        Long projectId = 3L;
        Project project = Project.builder()
                .member(member(1L))
                .curriculum(null)
                .build();
        ReflectionTestUtils.setField(project, "projectId", projectId);
        ProjectHistory savedHistory = ProjectHistory.builder()
                .project(project)
                .structure("{ \"nodes\": [] }")
                .build();
        ReflectionTestUtils.setField(savedHistory, "projectHistoryId", 7L);

        when(projectRepository.findByProjectId(projectId)).thenReturn(Optional.of(project));
        when(projectHistoryRepository.save(any(ProjectHistory.class))).thenReturn(savedHistory);

        ProjectHistoryResponse response = projectService.saveProject(projectId, new ProjectSaveRequest("{ \"nodes\": [] }"));

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.structure()).isEqualTo("{ \"nodes\": [] }");
        verify(projectHistoryRepository).save(any(ProjectHistory.class));
    }

    @Test
    void getProjectHistory_throws_when_project_missing() {
        when(projectRepository.findByProjectId(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectHistory(42L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(ErrorCode.PROJECT_NOT_FOUND.getMessage());
    }

    @Test
    void getProjectHistoryDetail_throws_when_history_missing() {
        when(projectHistoryRepository.findByProjectHistoryId(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectHistoryDetail(100L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(ErrorCode.PROJECT_HISTORY_NOT_FOUND.getMessage());
    }

    @Test
    void getProjects_returns_project_list() {
        Member member = member(5L);
        Project project = Project.builder()
                .member(member)
                .curriculum(null)
                .build();
        ReflectionTestUtils.setField(project, "projectId", 8L);

        when(memberRepository.findByMemberId(5L)).thenReturn(Optional.of(member));
        when(projectRepository.findByMember(member)).thenReturn(List.of(project));

        var responses = projectService.getProjects(5L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(8L);
    }

    private Member member(Long id) {
        Member member = Member.builder()
                .OAuthId("oauth-" + id)
                .nickname("user-" + id)
                .profileUrl("url")
                .build();
        ReflectionTestUtils.setField(member, "memberId", id);
        return member;
    }

    private Curriculum curriculum(Long id) {
        try {
            Constructor<Curriculum> constructor = Curriculum.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Curriculum curriculum = constructor.newInstance();
            ReflectionTestUtils.setField(curriculum, "curriculumId", id);
            return curriculum;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create Curriculum for tests", e);
        }
    }
}
