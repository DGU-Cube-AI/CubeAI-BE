package com.cubeai.domain.project.service;

import com.cubeai.domain.curriculum.entity.Curriculum;
import com.cubeai.domain.curriculum.repository.CurriculumRepository;
import com.cubeai.domain.member.entity.Member;
import com.cubeai.domain.member.repository.MemberRepository;
import com.cubeai.domain.project.dto.request.ProjectCreateRequest;
import com.cubeai.domain.project.dto.request.ProjectSaveRequest;
import com.cubeai.domain.project.dto.response.ProjectHistoryListResponse;
import com.cubeai.domain.project.dto.response.ProjectHistoryResponse;
import com.cubeai.domain.project.dto.response.ProjectListResponse;
import com.cubeai.domain.project.dto.response.ProjectResponse;
import com.cubeai.domain.project.entity.Project;
import com.cubeai.domain.project.entity.ProjectHistory;
import com.cubeai.domain.project.repository.ProjectHistoryRepository;
import com.cubeai.domain.project.repository.ProjectRepository;
import com.cubeai.global.error.ErrorCode;
import com.cubeai.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectHistoryRepository projectHistoryRepository;
    private final MemberRepository memberRepository;
    private final CurriculumRepository curriculumRepository;

    @Transactional
    public ProjectResponse createProject(Long memberId, ProjectCreateRequest request) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        Curriculum curriculum = curriculumRepository.findByCurriculumId(request.curriculumId())
                .orElse(null);
        Project project = request.toEntity(member, curriculum);
        Project savedProject = projectRepository.save(project);
        return ProjectResponse.from(savedProject);
    }

    public List<ProjectListResponse> getProjects(Long memberId) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        List<Project> projects = projectRepository.findByMember(member);
        return projects.stream()
                .map(ProjectListResponse::from)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProject(Long projectId) {
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PROJECT_NOT_FOUND));
        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectHistoryResponse saveProject(Long projectId, ProjectSaveRequest request) {
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PROJECT_NOT_FOUND));
        ProjectHistory projectHistory = ProjectHistory.builder()
                .project(project)
                .structure(request.structure())
                .build();
        ProjectHistory savedProjectHistory = projectHistoryRepository.save(projectHistory);
        return ProjectHistoryResponse.from(savedProjectHistory);
    }

    public List<ProjectHistoryListResponse> getProjectHistory(Long projectId) {
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PROJECT_NOT_FOUND));
        List<ProjectHistory> projectHistories = projectHistoryRepository.findByProject(project);
        return projectHistories.stream()
                .map(ProjectHistoryListResponse::from)
                .collect(Collectors.toList());
    }

    public ProjectHistoryResponse getProjectHistoryDetail(Long historyId) {
        ProjectHistory projectHistory = projectHistoryRepository.findByProjectHistoryId(historyId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PROJECT_HISTORY_NOT_FOUND));
        return ProjectHistoryResponse.from(projectHistory);
    }
}
