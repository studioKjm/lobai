# Context7 MCP Setup Guide

**Version**: 1.0
**Date**: 2025-12-30
**Status**: ✅ Recommended

---

## Overview

**Context7 MCP**는 프로젝트의 Markdown 문서를 자동으로 인덱싱하고, 자연어 쿼리로 검색할 수 있게 해주는 MCP 서버입니다. Claude Code에 기본 내장되어 있어 별도 설치 없이 사용 가능합니다.

---

## Features

- **자동 인덱싱**: `.md` 파일 자동 인덱스 생성
- **자연어 검색**: "TECHNICAL_GUIDE.md에서 테스트 전략 알려줘"
- **실시간 업데이트**: 문서 변경 시 자동 재인덱싱
- **컨텍스트 유지**: 대화 중 문서 참조 자동

---

## Installation

Context7은 Claude Code에 기본 내장되어 있습니다.

### 확인 방법

1. Claude Code 실행
2. 다음 쿼리 테스트:
   ```
   "TECHNICAL_GUIDE.md에 어떤 내용이 있어?"
   ```
3. 문서 내용이 반환되면 활성화 성공 ✅

### 수동 활성화 (필요 시)

Context7이 작동하지 않는 경우:

1. **설정 파일 위치 확인**:
   - macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
   - Windows: `%APPDATA%/Claude/claude_desktop_config.json`
   - Linux: `~/.config/Claude/claude_desktop_config.json`

2. **설정 추가** (파일이 없으면 생성):
   ```json
   {
     "mcpServers": {
       "context7": {
         "command": "npx",
         "args": ["-y", "@context7/mcp-server"]
       }
     }
   }
   ```

3. **Claude Code 재시작**

---

## Usage

### Basic Queries

```
"TECHNICAL_GUIDE.md에서 Gemini API 호출 부분 알려줘"
"guideline.md에 Feature Planner 설명 있어?"
"ADR 문서 목록 보여줘"
"plan-tamplate.md의 Quality Gate 섹션 읽어줘"
```

### Advanced Queries

```
"모든 ADR 문서에서 'API' 관련 결정사항 찾아줘"
"테스트 전략에 대한 문서들 요약해줘"
"보안 관련 문서 모두 찾아서 체크리스트 만들어줘"
```

### Cross-Document References

```
User: "Architecture Agent로 설계한 ADR 참고해서 테스트 작성해줘"

Context7 자동 동작:
1. 최근 생성된 ADR 문서 검색
2. Test Engineer Agent가 ADR 내용 참조
3. 설계에 맞는 테스트 코드 생성
```

---

## Indexed Files

Context7은 다음 파일들을 자동 인덱싱합니다:

### 프로젝트 루트
```
├── README.md
├── CLAUDE.md
└── LobAI_PRD_v3.md
```

### docs/ 폴더
```
docs/
├── TECHNICAL_GUIDE.md
├── adr/
│   └── ADR-*.md
├── plans/
│   └── PLAN_*.md
└── workflows/
    └── *.md
```

### .claude/ 폴더
```
.claude/
├── agents/
│   └── README.md
├── skills/
│   ├── README.md
│   ├── feature-planner.md
│   ├── code-review-checklist.md
│   ├── test-strategy.md
│   └── security-checklist.md
├── mcp-configs/
│   └── README.md
└── reference/
    ├── guideline.md
    ├── plan-tamplate.md
    └── SKILL.md
```

---

## Best Practices

### 1. 명확한 파일명

```
✅ PLAN_user_authentication_system.md
✅ ADR-001-component-separation.md
✅ TECHNICAL_GUIDE.md

❌ plan1.md
❌ doc.md
❌ temp.md
```

### 2. 구조화된 섹션 헤더

```markdown
## Gemini API 호출 로직          ✅ 명확
### sendMessage 함수 구현         ✅ 명확

## 코드                          ❌ 모호
## 기타                          ❌ 모호
```

### 3. 메타데이터 포함

```markdown
---
title: GENKUB Technical Guide
version: 1.0.0
date: 2025-12-30
---
```

### 4. 크로스 레퍼런스

```markdown
## Related Documents
- [Architecture Decision](../adr/ADR-001-component-separation.md)
- [Test Strategy](../testing/test-strategy.md)
```

---

## Examples

### Example 1: 문서 내용 검색

```
User: "TECHNICAL_GUIDE.md에서 테스트 전략 섹션 알려줘"

Context7 Response:
"테스트 전략은 다음과 같습니다:

## 테스트 전략

### Test Pyramid 구조
- Unit Tests: 70% (비즈니스 로직)
- Integration Tests: 20% (컴포넌트 상호작용)
- E2E Tests: 10% (핵심 시나리오)

### 커버리지 목표
- 비즈니스 로직: ≥90%
- API 레이어: ≥70%
- UI: Integration으로 대체

..."
```

### Example 2: 여러 문서 검색

```
User: "보안 관련 문서들 모두 찾아줘"

Context7 검색:
1. security-checklist.md
2. TECHNICAL_GUIDE.md (보안 섹션)
3. guideline.md (보안 에이전트)

Response:
"보안 관련 문서 3개 발견:
1. security-checklist.md: OWASP Top 10 체크리스트
2. TECHNICAL_GUIDE.md: API Key 보호, XSS 방지
3. guideline.md: Security Review Agent 설명
..."
```

### Example 3: 에이전트와 통합

```
User: "Architecture Agent 설명 읽고, Stats 컴포넌트 분리 설계해줘"

Workflow:
1. Context7: guideline.md에서 Architecture Agent 섹션 검색
2. Architecture Agent 역할/제약사항 파악
3. 현재 코드베이스 분석
4. ADR 문서 작성 (docs/adr/ADR-001-stats-separation.md)
```

---

## Troubleshooting

### Issue 1: 문서가 인식되지 않음

**Symptoms**:
- Context7이 파일을 찾지 못함
- "문서를 찾을 수 없습니다" 에러

**Solutions**:
1. 파일 확장자 확인 (`.md`만 인덱싱)
2. 파일 경로 확인 (프로젝트 루트 기준)
3. Claude Code 재시작
4. 파일명 명시하여 쿼리

### Issue 2: 오래된 내용 반환

**Symptoms**:
- 문서 수정했는데 이전 내용 표시

**Solutions**:
1. Claude Code 재시작 (자동 재인덱싱)
2. 파일 저장 확인
3. 캐시 클리어 (설정에서)

### Issue 3: 빈 응답

**Symptoms**:
- 쿼리 실행되지만 결과 없음

**Solutions**:
1. 문서 내용 확인 (빈 파일인지)
2. 검색어 변경 (더 구체적으로)
3. 파일명 정확히 명시

---

## Performance Optimization

### 1. 인덱싱 최적화

**파일 크기**:
- 권장: < 100KB per file
- 큰 문서는 분할 권장

**파일 개수**:
- 수백 개까지 문제없음
- 수천 개 이상 시 폴더 구조 최적화

### 2. 쿼리 최적화

```
❌ 모호한 쿼리: "테스트 알려줘"
✅ 구체적 쿼리: "TECHNICAL_GUIDE.md에서 Vitest 설정 방법 알려줘"

❌ 광범위한 쿼리: "모든 문서 내용"
✅ 타겟 쿼리: "ADR 문서들의 결정사항 요약"
```

### 3. 문서 구조화

```markdown
<!-- ✅ 좋은 구조 -->
# Main Topic

## Subtopic 1
Content...

## Subtopic 2
Content...

<!-- ❌ 나쁜 구조 -->
모든 내용이 한 블록에...
```

---

## Integration with Agents

### Architecture Agent + Context7

```
1. User: "Architecture Agent로 인증 시스템 설계"
2. Context7: 기존 ADR 문서 검색
3. Architecture Agent: 기존 패턴 참조
4. Output: 일관된 설계 문서
```

### Test Engineer Agent + Context7

```
1. User: "Test Engineer로 테스트 작성"
2. Context7: test-strategy.md 참조
3. Test Engineer: Test Pyramid 원칙 적용
4. Output: 70% Unit, 20% Integration, 10% E2E
```

### Security Agent + Context7

```
1. User: "Security Agent로 보안 검사"
2. Context7: security-checklist.md 로드
3. Security Agent: OWASP Top 10 기준 적용
4. Output: 체크리스트 기반 보안 리포트
```

---

## Security Considerations

### 민감정보 제외

Context7은 모든 `.md` 파일을 인덱싱하므로:

```bash
# .gitignore에 추가
secrets.md
credentials.md
*_secret.md
```

### 접근 제어

- Context7은 로컬 파일만 인덱싱
- 외부 접근 불가
- 네트워크 전송 없음

---

## Comparison with Alternatives

| Feature | Context7 | Manual Search | GitHub Copilot |
|---------|----------|---------------|----------------|
| 자동 인덱싱 | ✅ | ❌ | ✅ |
| 자연어 검색 | ✅ | ❌ | ✅ |
| 로컬 실행 | ✅ | ✅ | ❌ (클라우드) |
| 실시간 업데이트 | ✅ | ❌ | ✅ |
| 무료 | ✅ | ✅ | ❌ (유료) |

---

## Future Enhancements

### 계획 중
- [ ] PDF 파일 인덱싱
- [ ] 코드 파일 인덱싱 (`.ts`, `.tsx`)
- [ ] 다국어 지원
- [ ] 벡터 검색 (Semantic Search)

---

## FAQ

**Q: Context7은 어떤 파일을 인덱싱하나요?**
A: `.md` (Markdown) 파일만 인덱싱합니다.

**Q: 실시간 업데이트가 되나요?**
A: 네, 파일 저장 시 자동으로 재인덱싱됩니다.

**Q: 오프라인에서도 작동하나요?**
A: 네, 완전히 로컬에서 동작합니다.

**Q: 성능 영향은 없나요?**
A: 수백 개 파일까지는 무시할 수준입니다.

**Q: 다른 MCP 서버와 충돌하나요?**
A: 아니요, 여러 MCP 서버를 동시에 사용 가능합니다.

---

## Related Documentation

- **MCP Configs README**: [./.claude/mcp-configs/README.md](./README.md)
- **Recommended MCP Servers**: [./recommended-mcp-servers.md](./recommended-mcp-servers.md)
- **Folder Structure Spec**: [../../docs/plans/FOLDER_STRUCTURE_SPEC.md](../../docs/plans/FOLDER_STRUCTURE_SPEC.md)

---

**Last Updated**: 2025-12-30
**Status**: Production Ready ✅
**Next Review**: MCP 서버 추가 시
