# LobAI Docker 개발 환경 가이드

Docker를 사용하여 LobAI의 전체 개발 환경(MySQL, Backend, Frontend)을 한 번에 실행하고 관리하는 방법입니다.

## 🎯 Docker를 사용하는 이유

### 기존 방식의 문제점
- ❌ 백엔드 실행을 잊어버림
- ❌ GEMINI_API_KEY 환경변수 설정 누락
- ❌ 포트 충돌 (8080 이미 사용 중)
- ❌ Java 버전 불일치 문제
- ❌ 채팅 중간에 끊김

### Docker 사용 시 해결
- ✅ **한 번에 모든 서비스 시작**: `npm run docker:up`
- ✅ **환경변수 자동 로드**: `.env.docker`에서 관리
- ✅ **포트 충돌 방지**: 컨테이너 단위 관리
- ✅ **일관된 환경**: Java 17, Node.js 18 고정
- ✅ **안정적인 실행**: 컨테이너가 자동 재시작

---

## 🚀 빠른 시작

### 1. 초기 설정 (최초 1회만)

```bash
# 1. 환경변수 파일 생성
cp .env.docker.example .env.docker

# 2. .env.docker 파일 편집 (실제 API 키 입력)
# - GEMINI_API_KEY: Google Gemini API 키
# - JWT_SECRET: JWT 비밀키 (256비트 이상)
# - MySQL 비밀번호 등

# 3. 모든 서비스 시작 (빌드 포함)
npm run docker:up:build
```

### 2. 접속 확인

- **프론트엔드**: http://localhost:5175
- **백엔드 API**: http://localhost:8080
- **MySQL**: localhost:3306

---

## 📋 일상 사용

### 서비스 시작

```bash
# 모든 서비스 시작 (포그라운드)
npm run docker:up

# 백그라운드로 시작
docker-compose --env-file .env.docker up -d
```

### 서비스 중지

```bash
# 모든 서비스 중지
npm run docker:down

# 볼륨까지 삭제 (데이터 초기화)
npm run docker:down:volumes
```

### 로그 확인

```bash
# 모든 서비스 로그 (실시간)
npm run docker:logs

# 백엔드 로그만
npm run docker:logs:backend

# 프론트엔드 로그만
npm run docker:logs:frontend
```

### 서비스 재시작

```bash
# 모든 서비스 재시작
npm run docker:restart

# 특정 서비스만 재시작
docker-compose restart backend
docker-compose restart frontend
```

### 서비스 상태 확인

```bash
# 실행 중인 컨테이너 확인
npm run docker:ps

# 또는
docker-compose ps
```

---

## 🔨 코드 변경 시

### Frontend (Vite)
- **자동 반영**: 코드 변경 시 Hot Module Replacement (HMR)로 즉시 반영
- **별도 재시작 불필요**: `src/`, `public/` 디렉토리가 실시간 마운트

### Backend (Spring Boot)
- **의존성 변경 (build.gradle)**: 재빌드 필요
  ```bash
  npm run docker:up:build
  ```
- **코드 변경**: 재시작으로 반영
  ```bash
  docker-compose restart backend
  ```

---

## 🛠️ 문제 해결

### 1. 컨테이너가 시작되지 않음

```bash
# 로그 확인
docker-compose logs

# 특정 서비스 로그
docker-compose logs mysql
docker-compose logs backend
docker-compose logs frontend
```

### 2. MySQL 연결 실패

```bash
# MySQL 헬스체크 확인
docker-compose ps

# MySQL 컨테이너 재시작
docker-compose restart mysql

# MySQL 로그 확인
docker-compose logs mysql
```

### 3. 백엔드 빌드 실패

```bash
# 백엔드 재빌드
docker-compose build --no-cache backend

# 다시 시작
docker-compose up backend
```

### 4. 포트 충돌

```bash
# 기존 로컬 서비스 중지
# MySQL
brew services stop mysql

# 기존 Java 프로세스 종료
kill -9 $(lsof -t -i:8080)
```

### 5. 완전 초기화

```bash
# 모든 컨테이너, 볼륨, 네트워크 삭제
docker-compose down -v

# 이미지까지 삭제
docker-compose down -v --rmi all

# 재빌드 및 시작
npm run docker:up:build
```

### 6. 디스크 공간 부족

```bash
# 사용하지 않는 Docker 리소스 정리
docker system prune -a --volumes

# 주의: 모든 중지된 컨테이너, 이미지, 볼륨 삭제
```

---

## 📁 파일 구조

```
lobai/
├── docker-compose.yml          # 서비스 오케스트레이션
├── .env.docker                 # 환경변수 (Git에 커밋 안 됨)
├── .env.docker.example         # 환경변수 템플릿
├── Dockerfile.frontend         # 프론트엔드 이미지
├── backend/
│   └── Dockerfile              # 백엔드 이미지
└── docker/
    └── mysql/
        └── init.sql            # MySQL 초기 데이터 (백업본)
```

---

## 🔐 환경변수 설명

### `.env.docker` 파일

```env
# Database
MYSQL_ROOT_PASSWORD=루트_비밀번호
MYSQL_DATABASE=lobai_db
MYSQL_USER=lobai_user
MYSQL_PASSWORD=유저_비밀번호

# Backend API Keys
GEMINI_API_KEY=your_gemini_api_key_here
JWT_SECRET=your_jwt_secret_256_bits_long

# Ports (선택사항, 기본값)
MYSQL_PORT=3306
BACKEND_PORT=8080
FRONTEND_PORT=5175
```

**⚠️ 중요**: `.env.docker` 파일은 절대 Git에 커밋하지 마세요!

---

## 💾 데이터 백업 및 복원

### 데이터 백업

```bash
# 실행 중인 MySQL 컨테이너에서 백업
docker-compose exec mysql mysqldump -u root -p${MYSQL_ROOT_PASSWORD} ${MYSQL_DATABASE} > backup.sql
```

### 데이터 복원

```bash
# 백업 파일을 docker/mysql/init.sql로 복사
cp backup.sql docker/mysql/init.sql

# 볼륨 삭제 후 재시작 (데이터베이스 재생성)
npm run docker:down:volumes
npm run docker:up
```

---

## 🎓 유용한 Docker 명령어

```bash
# 컨테이너 내부 접속
docker-compose exec backend sh        # 백엔드 컨테이너
docker-compose exec frontend sh       # 프론트엔드 컨테이너
docker-compose exec mysql bash        # MySQL 컨테이너

# MySQL 접속
docker-compose exec mysql mysql -u lobai_user -p lobai_db

# 볼륨 확인
docker volume ls

# 네트워크 확인
docker network ls

# 이미지 확인
docker images
```

---

## 🔄 기존 로컬 환경과 전환

### Docker로 전환
```bash
# 1. 로컬 서비스 중지
brew services stop mysql
# 백엔드 프로세스 종료

# 2. Docker 시작
npm run docker:up
```

### 로컬로 전환
```bash
# 1. Docker 중지
npm run docker:down

# 2. 로컬 서비스 시작
brew services start mysql
cd backend && ./gradlew bootRun
```

---

## 📊 성능 최적화

### 빌드 캐싱
- Gradle 의존성 캐싱: `~/.gradle` 볼륨 마운트 (선택)
- npm 의존성 캐싱: `node_modules` 볼륨 사용 중

### 메모리 설정
Docker Desktop의 메모리를 최소 4GB 이상 할당하는 것을 권장합니다.

**Docker Desktop > Settings > Resources > Memory: 4GB+**

---

## ❓ FAQ

### Q: Docker를 사용하면 개발 속도가 느려지나요?
A: 아니요. Hot Reload (HMR)가 활성화되어 있어 코드 변경 시 즉시 반영됩니다.

### Q: 로컬 MySQL 데이터는 어떻게 되나요?
A: Docker MySQL은 별도 컨테이너에서 실행되며, 기존 로컬 MySQL 데이터에 영향을 주지 않습니다.

### Q: 데이터가 삭제되나요?
A: `docker-compose down`은 컨테이너만 중지합니다. 데이터는 볼륨에 보존됩니다. `docker-compose down -v`를 실행해야 볼륨(데이터)이 삭제됩니다.

### Q: 환경변수를 변경했는데 반영이 안 돼요.
A: 서비스를 재시작해야 합니다.
```bash
docker-compose down
npm run docker:up
```

---

## 📞 문제가 계속되면

1. 로그 확인: `npm run docker:logs`
2. 완전 초기화: `docker-compose down -v && npm run docker:up:build`
3. Docker 재시작: Docker Desktop 종료 후 재시작

---

**🎉 이제 `npm run docker:up` 한 번으로 전체 개발 환경을 실행할 수 있습니다!**
