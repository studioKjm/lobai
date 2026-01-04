# LobAI Documentation

**Welcome to the LobAI project documentation!**

This directory contains all the guides, runbooks, and reference materials for developing and maintaining the LobAI platform.

---

## 📚 Quick Start

New to the project? Start here:

1. **[CLAUDE.md](../CLAUDE.md)** - Project overview and quick reference
2. **[DEV_GUIDE.md](guides/DEV_GUIDE.md)** - Development setup and conventions
3. **[PROJECT_CONSTITUTION.md](../PROJECT_CONSTITUTION.md)** - Immutable architectural principles

---

## 📖 Documentation Structure

```
docs/
├── README.md                       # This file - Documentation index
│
├── guides/                         # Development & operational guides
│   ├── DEV_GUIDE.md                # Development conventions & Git workflow
│   ├── TEST_GUIDE.md               # Testing strategy & TDD guide
│   ├── CICD_GUIDE.md               # CI/CD pipeline & deployment
│   └── CONTEXT_GUIDE.md            # Claude Code collaboration tips
│
└── runbooks/                       # Incident response procedures
    └── INCIDENT_PLAYBOOK.md        # Emergency response guide
```

---

## 📋 Documents by Topic

### 🛠 Development

| Document | Purpose | When to Use |
|----------|---------|-------------|
| **[DEV_GUIDE.md](guides/DEV_GUIDE.md)** | Development environment setup, coding conventions, Git workflow | Daily development work |
| **[PROJECT_CONSTITUTION.md](../PROJECT_CONSTITUTION.md)** | Immutable architectural principles (folder structure, API contracts, business logic) | Before making architectural changes |
| **[CLAUDE.md](../CLAUDE.md)** | Project overview for Claude Code | When working with Claude Code |

### 🧪 Testing

| Document | Purpose | When to Use |
|----------|---------|-------------|
| **[TEST_GUIDE.md](guides/TEST_GUIDE.md)** | Unit, integration, and E2E testing strategies | Writing tests, setting up test infrastructure |

### 🚀 Deployment

| Document | Purpose | When to Use |
|----------|---------|-------------|
| **[CICD_GUIDE.md](guides/CICD_GUIDE.md)** | CI/CD pipeline, GitHub Actions, deployment strategies | Setting up CI/CD, deploying to production |

### 🤖 AI Collaboration

| Document | Purpose | When to Use |
|----------|---------|-------------|
| **[CONTEXT_GUIDE.md](guides/CONTEXT_GUIDE.md)** | Claude Code context management, prompt engineering | Collaborating with Claude Code |

### 🚨 Incident Response

| Document | Purpose | When to Use |
|----------|---------|-------------|
| **[INCIDENT_PLAYBOOK.md](runbooks/INCIDENT_PLAYBOOK.md)** | Emergency response procedures, troubleshooting | Service outages, production incidents |

---

## 🎯 Common Tasks

### Starting Development

```bash
# 1. Read project overview
cat CLAUDE.md

# 2. Set up environment
# See: docs/guides/DEV_GUIDE.md#1-development-environment-setup

# 3. Start dev servers
npm run dev        # Frontend (http://localhost:5173)
cd backend && ./gradlew bootRun  # Backend (http://localhost:8080)
```

### Adding a New Feature

1. **Plan**: Read [PROJECT_CONSTITUTION.md](../PROJECT_CONSTITUTION.md) for architectural constraints
2. **Develop**: Follow conventions in [DEV_GUIDE.md](guides/DEV_GUIDE.md)
3. **Test**: Write tests per [TEST_GUIDE.md](guides/TEST_GUIDE.md)
4. **Deploy**: Use CI/CD pipeline per [CICD_GUIDE.md](guides/CICD_GUIDE.md)

### Troubleshooting Production Issues

1. **Identify severity**: See [INCIDENT_PLAYBOOK.md#1-incident-severity](runbooks/INCIDENT_PLAYBOOK.md#1-장애-등급-분류)
2. **Initial response**: Follow [Initial Response Procedure](runbooks/INCIDENT_PLAYBOOK.md#2-초기-대응-절차)
3. **Root cause analysis**: Use [RCA guide](runbooks/INCIDENT_PLAYBOOK.md#3-근본-원인-분석)
4. **Recovery**: Execute [Recovery Procedures](runbooks/INCIDENT_PLAYBOOK.md#4-복구-절차)

### Working with Claude Code

1. **Context management**: See [CONTEXT_GUIDE.md](guides/CONTEXT_GUIDE.md)
2. **Effective prompts**: Read [Prompt Engineering section](guides/CONTEXT_GUIDE.md#4-프롬프트-엔지니어링)
3. **Update CLAUDE.md**: When architecture changes

---

## 📊 Project Architecture Overview

### Tech Stack

**Frontend**:
- React 19 + TypeScript + Vite
- TailwindCSS for styling
- Zustand for state management
- Axios for HTTP requests

**Backend**:
- Spring Boot 3.x + Java 17
- MySQL 8.0 database
- JWT authentication
- Gemini AI integration

**Infrastructure**:
- AWS (ECS, RDS, S3)
- Vercel (Frontend hosting)
- GitHub Actions (CI/CD)

### Folder Structure

```
lobai/
├── backend/                    # Spring Boot backend
│   └── src/main/java/com/lobai/
│       ├── controller/         # REST API endpoints
│       ├── service/            # Business logic
│       ├── repository/         # Database access
│       ├── entity/             # JPA entities
│       ├── dto/                # Data transfer objects
│       ├── config/             # Configuration
│       ├── security/           # JWT & authentication
│       └── exception/          # Error handling
│
├── src/                        # React frontend
│   ├── components/             # React components
│   ├── pages/                  # Page-level components
│   ├── services/               # API clients
│   ├── hooks/                  # Custom hooks
│   ├── stores/                 # Zustand stores
│   ├── types/                  # TypeScript types
│   └── lib/                    # Utilities
│
├── docs/                       # Documentation (this directory)
├── PROJECT_CONSTITUTION.md     # Architectural principles
└── CLAUDE.md                   # Claude Code guide
```

---

## 🔑 Key Principles

### Architectural Invariants

These rules **cannot be changed** without team consensus (see [PROJECT_CONSTITUTION.md](../PROJECT_CONSTITUTION.md)):

1. **API Response Format**: All APIs return `ApiResponse<T>` with `success`, `data`, `message` fields
2. **Stats Range**: All stats (hunger, energy, happiness) are 0-100
3. **Database Schema**: Tables cannot be deleted, only columns can be added
4. **JWT Expiry**: Access token = 15 minutes, Refresh token = 7 days
5. **Role-based Access**: Only `USER` and `ADMIN` roles allowed

### Development Principles

1. **TDD**: Write tests before code (see [TEST_GUIDE.md](guides/TEST_GUIDE.md#7-tdd-test-driven-development))
2. **Code Review**: All PRs require 1+ approval
3. **Documentation**: Update docs when changing architecture
4. **No Secrets in Code**: Use environment variables

---

## 🤝 Contributing

### Before You Start

1. ✅ Read [CLAUDE.md](../CLAUDE.md) for project overview
2. ✅ Read [PROJECT_CONSTITUTION.md](../PROJECT_CONSTITUTION.md) for immutable rules
3. ✅ Read [DEV_GUIDE.md](guides/DEV_GUIDE.md) for coding conventions
4. ✅ Set up development environment

### Workflow

```
1. Create Issue (GitHub)
   ↓
2. Create Feature Branch (git checkout -b feature/my-feature)
   ↓
3. Develop with TDD
   ↓
4. Write Tests (80%+ coverage)
   ↓
5. Create Pull Request
   ↓
6. Code Review
   ↓
7. Merge to develop
   ↓
8. Auto-deploy to Staging
   ↓
9. QA Testing
   ↓
10. Release to Production
```

See [DEV_GUIDE.md#5-development-process](guides/DEV_GUIDE.md#5-개발-프로세스) for details.

---

## 📞 Getting Help

### Internal Resources

- **Slack**: `#engineering` for general questions, `#incidents` for emergencies
- **GitHub Issues**: Bug reports and feature requests
- **Team Wiki**: Additional internal documentation

### External Resources

- **React Docs**: https://react.dev
- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **Gemini API**: https://ai.google.dev/docs

### On-call Support

See [INCIDENT_PLAYBOOK.md#6-contacts](runbooks/INCIDENT_PLAYBOOK.md#6-연락처-및-에스컬레이션) for on-call rotation and emergency contacts.

---

## 📝 Document Maintenance

### Updating Documentation

**When to update**:
- ✅ Architecture changes → Update [PROJECT_CONSTITUTION.md](../PROJECT_CONSTITUTION.md) and [CLAUDE.md](../CLAUDE.md)
- ✅ New conventions → Update [DEV_GUIDE.md](guides/DEV_GUIDE.md)
- ✅ New testing patterns → Update [TEST_GUIDE.md](guides/TEST_GUIDE.md)
- ✅ CI/CD changes → Update [CICD_GUIDE.md](guides/CICD_GUIDE.md)
- ✅ Incident resolution → Update [INCIDENT_PLAYBOOK.md](runbooks/INCIDENT_PLAYBOOK.md)

**Review schedule**:
- **Weekly**: CLAUDE.md (if architecture changed)
- **Monthly**: All guides (after major releases)
- **Quarterly**: Full documentation review

### Style Guide

See [CONTEXT_GUIDE.md#2-document-writing-principles](guides/CONTEXT_GUIDE.md#2-문서-작성-원칙):
- Use clear, concise language
- Include code examples
- Keep each doc under 1,000 lines
- Update changelog at bottom

---

## 🗂 Changelog

| Version | Date       | Changes                                | Author |
|---------|------------|----------------------------------------|--------|
| 1.0.0   | 2026-01-04 | Initial documentation structure        | Team   |

---

## 📜 License

This documentation is proprietary and confidential.
© 2026 LobAI. All rights reserved.
