# CI/CD Guide

**LobAI 프로젝트 CI/CD 가이드**
**Version**: 1.0.0
**Last Updated**: 2026-01-04

이 문서는 LobAI 프로젝트의 CI/CD 파이프라인, 배포 전략, 환경 관리를 다룹니다.

---

## Table of Contents

1. [CI/CD 개요](#1-cicd-개요)
2. [GitHub Actions 설정](#2-github-actions-설정)
3. [환경 분리 전략](#3-환경-분리-전략)
4. [배포 프로세스](#4-배포-프로세스)
5. [환경 변수 관리](#5-환경-변수-관리)
6. [모니터링 및 알림](#6-모니터링-및-알림)
7. [롤백 절차](#7-롤백-절차)

---

## 1. CI/CD 개요

### 1.1 파이프라인 구조

```
GitHub PR
    ↓
┌─────────────────────┐
│  CI Pipeline        │
│  - Lint             │
│  - Unit Tests       │
│  - Build            │
│  - Coverage Check   │
└─────────────────────┘
    ↓
Merge to develop
    ↓
┌─────────────────────┐
│  Staging Deployment │
│  - Integration Test │
│  - E2E Tests        │
│  - Auto Deploy      │
└─────────────────────┘
    ↓
QA Approval
    ↓
Release Branch
    ↓
┌─────────────────────┐
│  Production Deploy  │
│  - Manual Approval  │
│  - Blue-Green       │
│  - Health Check     │
└─────────────────────┘
```

### 1.2 환경별 특성

| 환경 | 브랜치 | 배포 방식 | 목적 |
|------|--------|-----------|------|
| **Development** | `develop` | 자동 | 최신 기능 통합 테스트 |
| **Staging** | `develop` | 자동 | Production 환경 시뮬레이션 |
| **Production** | `main` | 수동 승인 | 실제 서비스 운영 |

### 1.3 필수 도구

- **GitHub Actions** - CI/CD 자동화
- **Docker** - 컨테이너 빌드 및 배포
- **Vercel/Netlify** - Frontend 호스팅 (또는 자체 서버)
- **AWS/GCP/Azure** - Backend 호스팅
- **GitHub Secrets** - 민감 정보 관리

---

## 2. GitHub Actions 설정

### 2.1 Pull Request CI

**.github/workflows/pr-check.yml**:

```yaml
name: PR Checks

on:
  pull_request:
    branches:
      - develop
      - main
    types: [opened, synchronize, reopened]

jobs:
  lint-and-format:
    name: Lint & Format Check
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'

      - name: Install dependencies
        run: npm ci

      - name: Run ESLint
        run: npm run lint

      - name: Check TypeScript types
        run: npx tsc --noEmit

  frontend-tests:
    name: Frontend Tests
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'

      - name: Install dependencies
        run: npm ci

      - name: Run tests with coverage
        run: npm run test:coverage

      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v4
        with:
          files: ./coverage/lcov.info
          flags: frontend
          token: ${{ secrets.CODECOV_TOKEN }}

      - name: Comment coverage on PR
        uses: romeovs/lcov-reporter-action@v0.3.1
        with:
          lcov-file: ./coverage/lcov.info
          github-token: ${{ secrets.GITHUB_TOKEN }}

  backend-tests:
    name: Backend Tests
    runs-on: ubuntu-latest

    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: root
          MYSQL_DATABASE: lobai_test
        ports:
          - 3306:3306
        options: >-
          --health-cmd="mysqladmin ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3

    steps:
      - uses: actions/checkout@v4

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'gradle'

      - name: Grant execute permission for gradlew
        run: chmod +x backend/gradlew

      - name: Run tests
        working-directory: ./backend
        run: ./gradlew test jacocoTestReport
        env:
          SPRING_DATASOURCE_URL: jdbc:mysql://localhost:3306/lobai_test
          SPRING_DATASOURCE_USERNAME: root
          SPRING_DATASOURCE_PASSWORD: root

      - name: Upload coverage
        uses: codecov/codecov-action@v4
        with:
          files: ./backend/build/reports/jacoco/test/jacocoTestReport.xml
          flags: backend
          token: ${{ secrets.CODECOV_TOKEN }}

  build-check:
    name: Build Check
    runs-on: ubuntu-latest
    needs: [frontend-tests, backend-tests]

    steps:
      - uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'

      - name: Install dependencies
        run: npm ci

      - name: Build frontend
        run: npm run build
        env:
          VITE_API_URL: https://api.staging.lobai.com

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Build backend
        working-directory: ./backend
        run: ./gradlew build -x test

      - name: Upload build artifacts
        uses: actions/upload-artifact@v4
        with:
          name: build-artifacts
          path: |
            dist/
            backend/build/libs/
          retention-days: 7
```

### 2.2 Main Branch CI (Staging 자동 배포)

**.github/workflows/staging-deploy.yml**:

```yaml
name: Deploy to Staging

on:
  push:
    branches:
      - develop

jobs:
  test:
    name: Run Tests
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'

      - name: Install dependencies
        run: npm ci

      - name: Run tests
        run: npm test

  deploy-frontend:
    name: Deploy Frontend to Vercel
    runs-on: ubuntu-latest
    needs: test

    steps:
      - uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'

      - name: Install dependencies
        run: npm ci

      - name: Build
        run: npm run build
        env:
          VITE_API_URL: ${{ secrets.STAGING_API_URL }}

      - name: Deploy to Vercel
        uses: amondnet/vercel-action@v25
        with:
          vercel-token: ${{ secrets.VERCEL_TOKEN }}
          vercel-org-id: ${{ secrets.VERCEL_ORG_ID }}
          vercel-project-id: ${{ secrets.VERCEL_PROJECT_ID }}
          vercel-args: '--prod'

  deploy-backend:
    name: Deploy Backend to AWS
    runs-on: ubuntu-latest
    needs: test

    steps:
      - uses: actions/checkout@v4

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Build JAR
        working-directory: ./backend
        run: ./gradlew bootJar

      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ap-northeast-2

      - name: Login to Amazon ECR
        id: login-ecr
        uses: aws-actions/amazon-ecr-login@v2

      - name: Build, tag, and push Docker image
        env:
          ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
          ECR_REPOSITORY: lobai-backend-staging
          IMAGE_TAG: ${{ github.sha }}
        run: |
          docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG ./backend
          docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG
          docker tag $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG $ECR_REGISTRY/$ECR_REPOSITORY:latest
          docker push $ECR_REGISTRY/$ECR_REPOSITORY:latest

      - name: Deploy to ECS
        uses: aws-actions/amazon-ecs-deploy-task-definition@v1
        with:
          task-definition: ecs-task-definition-staging.json
          service: lobai-backend-staging
          cluster: lobai-staging
          wait-for-service-stability: true

  e2e-tests:
    name: E2E Tests on Staging
    runs-on: ubuntu-latest
    needs: [deploy-frontend, deploy-backend]

    steps:
      - uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'

      - name: Install dependencies
        run: npm ci

      - name: Install Playwright
        run: npx playwright install --with-deps

      - name: Run E2E tests
        run: npx playwright test
        env:
          BASE_URL: ${{ secrets.STAGING_FRONTEND_URL }}

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: playwright-report
          path: playwright-report/
          retention-days: 30

  notify:
    name: Notify Deployment
    runs-on: ubuntu-latest
    needs: [deploy-frontend, deploy-backend, e2e-tests]
    if: always()

    steps:
      - name: Send Slack notification
        uses: slackapi/slack-github-action@v1.25.0
        with:
          payload: |
            {
              "text": "Staging Deployment ${{ job.status }}",
              "blocks": [
                {
                  "type": "section",
                  "text": {
                    "type": "mrkdwn",
                    "text": "*Staging Deployment*\nStatus: ${{ job.status }}\nBranch: ${{ github.ref_name }}\nCommit: ${{ github.sha }}"
                  }
                }
              ]
            }
        env:
          SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
```

### 2.3 Production 배포

**.github/workflows/production-deploy.yml**:

```yaml
name: Deploy to Production

on:
  push:
    branches:
      - main
    tags:
      - 'v*'

jobs:
  deploy:
    name: Production Deployment
    runs-on: ubuntu-latest
    environment:
      name: production
      url: https://lobai.com

    steps:
      - uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'

      - name: Install dependencies
        run: npm ci

      - name: Run tests
        run: npm test

      - name: Build frontend
        run: npm run build
        env:
          VITE_API_URL: ${{ secrets.PRODUCTION_API_URL }}

      - name: Deploy to Production (Frontend)
        uses: amondnet/vercel-action@v25
        with:
          vercel-token: ${{ secrets.VERCEL_TOKEN }}
          vercel-org-id: ${{ secrets.VERCEL_ORG_ID }}
          vercel-project-id: ${{ secrets.VERCEL_PROD_PROJECT_ID }}
          alias-domains: lobai.com

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Build backend JAR
        working-directory: ./backend
        run: ./gradlew bootJar

      - name: Deploy backend (Blue-Green)
        run: |
          # Blue-Green deployment script
          ./scripts/deploy-blue-green.sh
        env:
          AWS_ACCESS_KEY_ID: ${{ secrets.AWS_ACCESS_KEY_ID }}
          AWS_SECRET_ACCESS_KEY: ${{ secrets.AWS_SECRET_ACCESS_KEY }}

      - name: Run smoke tests
        run: |
          curl -f https://api.lobai.com/health || exit 1
          curl -f https://lobai.com || exit 1

      - name: Create GitHub Release
        if: startsWith(github.ref, 'refs/tags/')
        uses: actions/create-release@v1
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        with:
          tag_name: ${{ github.ref }}
          release_name: Release ${{ github.ref }}
          draft: false
          prerelease: false
```

---

## 3. 환경 분리 전략

### 3.1 환경별 설정

#### Development (Local)

```yaml
# backend/src/main/resources/application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lobai_dev
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

logging:
  level:
    com.lobai: DEBUG
```

```env
# .env.local (Frontend)
VITE_API_URL=http://localhost:8080/api
```

#### Staging

```yaml
# backend/src/main/resources/application-staging.yml
spring:
  datasource:
    url: ${DATABASE_URL}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

logging:
  level:
    com.lobai: INFO
```

```env
# Vercel Environment Variables
VITE_API_URL=https://api.staging.lobai.com/api
```

#### Production

```yaml
# backend/src/main/resources/application-prod.yml
spring:
  datasource:
    url: ${DATABASE_URL}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

logging:
  level:
    com.lobai: WARN
    org.springframework: WARN
```

```env
# Vercel Environment Variables
VITE_API_URL=https://api.lobai.com/api
```

### 3.2 Profile 활성화

```bash
# Local
./gradlew bootRun --args='--spring.profiles.active=dev'

# Staging
java -jar app.jar --spring.profiles.active=staging

# Production
java -jar app.jar --spring.profiles.active=prod
```

---

## 4. 배포 프로세스

### 4.1 Blue-Green 배포 전략

**스크립트: scripts/deploy-blue-green.sh**:

```bash
#!/bin/bash
set -e

# Configuration
CLUSTER_NAME="lobai-production"
SERVICE_NAME="lobai-backend"
TASK_DEFINITION="lobai-backend-prod"
DESIRED_COUNT=2

# Get current task definition
CURRENT_TASK_DEF=$(aws ecs describe-services \
  --cluster $CLUSTER_NAME \
  --services $SERVICE_NAME \
  --query 'services[0].taskDefinition' \
  --output text)

echo "Current task definition: $CURRENT_TASK_DEF"

# Register new task definition
NEW_TASK_DEF=$(aws ecs register-task-definition \
  --cli-input-json file://ecs-task-definition-prod.json \
  --query 'taskDefinition.taskDefinitionArn' \
  --output text)

echo "New task definition registered: $NEW_TASK_DEF"

# Update service to use new task definition (Blue-Green)
aws ecs update-service \
  --cluster $CLUSTER_NAME \
  --service $SERVICE_NAME \
  --task-definition $NEW_TASK_DEF \
  --desired-count $DESIRED_COUNT \
  --deployment-configuration "maximumPercent=200,minimumHealthyPercent=100"

echo "Waiting for deployment to complete..."

# Wait for service stability
aws ecs wait services-stable \
  --cluster $CLUSTER_NAME \
  --services $SERVICE_NAME

echo "Deployment completed successfully!"

# Run health checks
echo "Running health checks..."
for i in {1..5}; do
  HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" https://api.lobai.com/health)
  if [ $HTTP_STATUS -eq 200 ]; then
    echo "Health check $i/5: OK"
  else
    echo "Health check $i/5: FAILED (HTTP $HTTP_STATUS)"
    exit 1
  fi
  sleep 2
done

echo "All health checks passed!"
```

### 4.2 Database 마이그레이션

**Flyway 사용 (권장)**:

```yaml
# backend/build.gradle
dependencies {
    implementation 'org.flywaydb:flyway-core:9.22.0'
    implementation 'org.flywaydb:flyway-mysql:9.22.0'
}

# application.yml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

**마이그레이션 파일**:

```sql
-- backend/src/main/resources/db/migration/V1__initial_schema.sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    username VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- backend/src/main/resources/db/migration/V2__add_role_column.sql
ALTER TABLE users ADD COLUMN role VARCHAR(20) DEFAULT 'USER';

-- backend/src/main/resources/db/migration/V3__create_attendance_table.sql
CREATE TABLE attendance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    check_in_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, check_in_date),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

**마이그레이션 실행**:

```bash
# Local
./gradlew flywayMigrate -Pflyway.url=jdbc:mysql://localhost:3306/lobai_dev

# Staging
./gradlew flywayMigrate -Pflyway.url=$STAGING_DATABASE_URL

# Production (자동 실행)
# Spring Boot 시작 시 자동으로 마이그레이션 적용
```

### 4.3 Rollback 스크립트

**scripts/rollback.sh**:

```bash
#!/bin/bash
set -e

CLUSTER_NAME="lobai-production"
SERVICE_NAME="lobai-backend"

# Get previous task definition
PREVIOUS_TASK_DEF=$(aws ecs describe-services \
  --cluster $CLUSTER_NAME \
  --services $SERVICE_NAME \
  --query 'services[0].deployments[1].taskDefinition' \
  --output text)

if [ -z "$PREVIOUS_TASK_DEF" ]; then
  echo "No previous deployment found!"
  exit 1
fi

echo "Rolling back to: $PREVIOUS_TASK_DEF"

# Rollback
aws ecs update-service \
  --cluster $CLUSTER_NAME \
  --service $SERVICE_NAME \
  --task-definition $PREVIOUS_TASK_DEF

echo "Rollback initiated. Waiting for stability..."

aws ecs wait services-stable \
  --cluster $CLUSTER_NAME \
  --services $SERVICE_NAME

echo "Rollback completed!"
```

---

## 5. 환경 변수 관리

### 5.1 GitHub Secrets 설정

**Settings → Secrets and variables → Actions → New repository secret**:

```
# AWS Credentials
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
AWS_REGION

# Database
DATABASE_URL (Production)
STAGING_DATABASE_URL

# API Keys
GEMINI_API_KEY
GEMINI_API_KEY_STAGING

# JWT
JWT_SECRET
JWT_SECRET_STAGING

# Deployment
VERCEL_TOKEN
VERCEL_ORG_ID
VERCEL_PROJECT_ID
VERCEL_PROD_PROJECT_ID

# Monitoring
SLACK_WEBHOOK_URL
SENTRY_DSN
```

### 5.2 Environment-specific Secrets

**GitHub Environments 사용**:

```yaml
# .github/workflows/production-deploy.yml
jobs:
  deploy:
    environment:
      name: production  # 이 환경의 secrets 사용
      url: https://lobai.com
```

**Settings → Environments → New environment**:
- Name: `production`
- Protection rules:
  - ✅ Required reviewers (최소 1명)
  - ✅ Wait timer (5분)
- Environment secrets:
  - `PRODUCTION_API_URL`
  - `DATABASE_URL`

### 5.3 Secret Rotation

**주기적으로 갱신 필요** (3개월마다):
1. AWS Access Keys
2. JWT Secret
3. Database passwords
4. API Keys (Gemini)

**갱신 절차**:
```bash
# 1. 새 secret 생성
aws secretsmanager create-secret --name lobai-jwt-secret-v2

# 2. GitHub Secrets 업데이트
# Settings → Secrets → Edit

# 3. 배포하여 새 secret 적용

# 4. 구 secret 비활성화 (1주일 대기)

# 5. 구 secret 삭제
```

---

## 6. 모니터링 및 알림

### 6.1 Health Check Endpoints

**backend/src/main/java/com/lobai/controller/HealthController.java**:

```java
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("version", "1.0.0");
        return ResponseEntity.ok(health);
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> readinessCheck() {
        // Database connectivity check
        // Redis check (if applicable)
        return ResponseEntity.ok(Map.of("status", "READY"));
    }

    @GetMapping("/live")
    public ResponseEntity<Map<String, String>> livenessCheck() {
        return ResponseEntity.ok(Map.of("status", "ALIVE"));
    }
}
```

### 6.2 Slack 알림

**.github/workflows/notify.yml** (재사용 가능):

```yaml
name: Notify Deployment

on:
  workflow_call:
    inputs:
      environment:
        required: true
        type: string
      status:
        required: true
        type: string

jobs:
  notify:
    runs-on: ubuntu-latest
    steps:
      - name: Send Slack notification
        uses: slackapi/slack-github-action@v1.25.0
        with:
          payload: |
            {
              "text": "Deployment to ${{ inputs.environment }}: ${{ inputs.status }}",
              "blocks": [
                {
                  "type": "header",
                  "text": {
                    "type": "plain_text",
                    "text": "🚀 Deployment Notification"
                  }
                },
                {
                  "type": "section",
                  "fields": [
                    {
                      "type": "mrkdwn",
                      "text": "*Environment:*\n${{ inputs.environment }}"
                    },
                    {
                      "type": "mrkdwn",
                      "text": "*Status:*\n${{ inputs.status }}"
                    },
                    {
                      "type": "mrkdwn",
                      "text": "*Commit:*\n${{ github.sha }}"
                    },
                    {
                      "type": "mrkdwn",
                      "text": "*Author:*\n${{ github.actor }}"
                    }
                  ]
                }
              ]
            }
        env:
          SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
```

### 6.3 에러 모니터링 (Sentry)

**Frontend (src/main.tsx)**:

```typescript
import * as Sentry from '@sentry/react';

if (import.meta.env.PROD) {
  Sentry.init({
    dsn: import.meta.env.VITE_SENTRY_DSN,
    environment: import.meta.env.MODE,
    tracesSampleRate: 1.0,
    integrations: [
      new Sentry.BrowserTracing(),
      new Sentry.Replay(),
    ],
  });
}
```

**Backend (Application.java)**:

```java
@Bean
public ServletContextInitializer sentryServletContextInitializer() {
    return servletContext -> {
        Sentry.init(options -> {
            options.setDsn(System.getenv("SENTRY_DSN"));
            options.setEnvironment(environment);
            options.setTracesSampleRate(1.0);
        });
    };
}
```

---

## 7. 롤백 절차

### 7.1 자동 롤백 (Health Check 실패 시)

**scripts/auto-rollback.sh**:

```bash
#!/bin/bash
set -e

MAX_RETRIES=5
RETRY_INTERVAL=10

for i in $(seq 1 $MAX_RETRIES); do
  HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" https://api.lobai.com/health)

  if [ $HTTP_STATUS -eq 200 ]; then
    echo "Health check $i/$MAX_RETRIES: OK"
    exit 0
  else
    echo "Health check $i/$MAX_RETRIES: FAILED (HTTP $HTTP_STATUS)"

    if [ $i -eq $MAX_RETRIES ]; then
      echo "All health checks failed. Initiating rollback..."
      ./scripts/rollback.sh
      exit 1
    fi

    sleep $RETRY_INTERVAL
  fi
done
```

### 7.2 수동 롤백

```bash
# 1. 이전 배포 버전 확인
aws ecs describe-services \
  --cluster lobai-production \
  --services lobai-backend \
  --query 'services[0].deployments'

# 2. Rollback 스크립트 실행
./scripts/rollback.sh

# 3. Frontend rollback (Vercel)
vercel rollback lobai.com --token=$VERCEL_TOKEN

# 4. Database rollback (Flyway)
./gradlew flywayUndo -Pflyway.url=$PRODUCTION_DATABASE_URL

# 5. 롤백 확인
curl https://api.lobai.com/health
curl https://lobai.com
```

### 7.3 Database 롤백

**Flyway Undo (상용 버전 필요)**:

```sql
-- backend/src/main/resources/db/migration/U2__undo_add_role_column.sql
ALTER TABLE users DROP COLUMN role;
```

```bash
./gradlew flywayUndo -Pflyway.url=$DATABASE_URL
```

**수동 롤백**:

```bash
# 1. Database backup 복원
mysql -u root -p lobai_prod < backups/lobai_prod_2026-01-04.sql

# 2. 복원 확인
mysql -u root -p lobai_prod -e "SELECT COUNT(*) FROM users;"
```

---

## Changelog

| Version | Date       | Changes                     | Author |
|---------|------------|-----------------------------|--------|
| 1.0.0   | 2026-01-04 | Initial CICD_GUIDE created  | Team   |

---

**다음 문서**: [INCIDENT_PLAYBOOK.md](../runbooks/INCIDENT_PLAYBOOK.md)
**관련 문서**: [DEV_GUIDE.md](./DEV_GUIDE.md), [TEST_GUIDE.md](./TEST_GUIDE.md)
