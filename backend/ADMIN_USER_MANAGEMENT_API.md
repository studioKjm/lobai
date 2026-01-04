# Admin User Management API Implementation

## Overview
Complete CRUD API for admin user management in the LobAI backend system.

## Implementation Summary

### 1. Request DTOs
**Location**: `/Users/jimin/lobai/lobai/backend/src/main/java/com/lobai/dto/request/`

- **UpdateUserRequest.java** - Update user profile (email, username)
- **ChangeRoleRequest.java** - Change user role (USER, ADMIN)
- **ChangeStatusRequest.java** - Change user status (active, suspended, deleted)

### 2. Response DTOs
**Location**: `/Users/jimin/lobai/lobai/backend/src/main/java/com/lobai/dto/response/`

- **UserListResponse.java** - User list item with basic info (id, email, username, role, status, timestamps)
- **UserDetailResponse.java** - Extended user info with stats (total messages, attendance days, affinity score)

### 3. Entity Updates
**File**: `/Users/jimin/lobai/lobai/backend/src/main/java/com/lobai/entity/User.java`

Added fields:
- `status` (VARCHAR(20), default: "active") - User status: active, suspended, deleted
- `deletedAt` (TIMESTAMP) - Soft delete timestamp

Added methods:
- `updateProfile(String email, String username)` - Update user profile
- `changeRole(Role newRole)` - Change user role
- `changeStatus(String newStatus)` - Change user status (sets deletedAt if status is "deleted")
- `softDelete()` - Perform soft delete (sets status to "deleted" and deletedAt timestamp)

### 4. Database Schema
**File**: `/Users/jimin/lobai/lobai/backend/src/main/resources/db/schema.sql`

Updated `users` table:
```sql
status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '사용자 상태 (active, suspended, deleted)',
deleted_at TIMESTAMP COMMENT '삭제 시각 (soft delete)',
INDEX idx_status (status)
```

### 5. Repository
**File**: `/Users/jimin/lobai/lobai/backend/src/main/java/com/lobai/repository/UserRepository.java`

Added methods:
- `countByRole(Role role)` - Count users by role
- `countByRoleExcludingUser(Role role, Long userId)` - Count users by role excluding specific user (for business rule validation)

### 6. Service Layer
**File**: `/Users/jimin/lobai/lobai/backend/src/main/java/com/lobai/service/UserAdminService.java`

Implemented methods:
- `getUsers(page, size, role, status, search)` - List users with pagination and filtering
- `getUserDetail(id)` - Get detailed user info with stats
- `updateUser(id, request)` - Update user profile
- `deleteUser(id)` - Soft delete user
- `changeUserRole(id, request)` - Change user role
- `changeUserStatus(id, request)` - Change user status

**Business Rules Enforced**:
- Cannot change own role
- Cannot delete last ADMIN user
- Cannot change last ADMIN to USER
- Cannot delete self
- Cannot change own status
- Email uniqueness validation on update

### 7. Controller
**File**: `/Users/jimin/lobai/lobai/backend/src/main/java/com/lobai/controller/UserAdminController.java`

REST Endpoints:
- `GET /api/admin/users` - List users (pagination, filtering, search)
- `GET /api/admin/users/{id}` - Get user detail
- `PUT /api/admin/users/{id}` - Update user
- `DELETE /api/admin/users/{id}` - Delete user (soft delete)
- `PATCH /api/admin/users/{id}/role` - Change user role
- `PATCH /api/admin/users/{id}/status` - Change user status

All endpoints require `ADMIN` role via `@PreAuthorize("hasRole('ADMIN')")`

## API Documentation

### GET /api/admin/users
List users with pagination and filtering.

**Query Parameters**:
- `page` (default: 0) - Page number
- `size` (default: 25) - Page size
- `role` (optional) - Filter by role (USER, ADMIN)
- `status` (optional) - Filter by status (active, suspended, deleted)
- `search` (optional) - Search by email or username

**Response**:
```json
{
  "success": true,
  "message": "사용자 목록 조회 성공",
  "data": {
    "content": [
      {
        "id": 1,
        "email": "user@example.com",
        "username": "User Name",
        "role": "USER",
        "status": "active",
        "createdAt": "2025-01-01T00:00:00",
        "lastLoginAt": "2025-01-02T12:00:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 4,
    "number": 0,
    "size": 25
  }
}
```

### GET /api/admin/users/{id}
Get detailed user information with stats.

**Response**:
```json
{
  "success": true,
  "message": "사용자 상세 조회 성공",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "username": "User Name",
    "role": "USER",
    "status": "active",
    "createdAt": "2025-01-01T00:00:00",
    "lastLoginAt": "2025-01-02T12:00:00",
    "totalMessages": 150,
    "totalAttendanceDays": 30,
    "affinityScore": 75.5
  }
}
```

### PUT /api/admin/users/{id}
Update user profile.

**Request Body**:
```json
{
  "email": "newemail@example.com",
  "username": "New Username"
}
```

**Response**:
```json
{
  "success": true,
  "message": "사용자 정보 수정 성공",
  "data": {
    "id": 1,
    "email": "newemail@example.com",
    "username": "New Username",
    "currentHunger": 80,
    "currentEnergy": 90,
    "currentHappiness": 70,
    "currentPersonaId": 1
  }
}
```

### DELETE /api/admin/users/{id}
Soft delete user.

**Response**:
```json
{
  "success": true,
  "message": "사용자 삭제 성공",
  "data": null
}
```

### PATCH /api/admin/users/{id}/role
Change user role.

**Request Body**:
```json
{
  "role": "ADMIN"
}
```

**Response**:
```json
{
  "success": true,
  "message": "사용자 권한 변경 성공",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "username": "User Name",
    "currentHunger": 80,
    "currentEnergy": 90,
    "currentHappiness": 70,
    "currentPersonaId": 1
  }
}
```

### PATCH /api/admin/users/{id}/status
Change user status.

**Request Body**:
```json
{
  "status": "suspended"
}
```

**Response**:
```json
{
  "success": true,
  "message": "사용자 상태 변경 성공",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "username": "User Name",
    "currentHunger": 80,
    "currentEnergy": 90,
    "currentHappiness": 70,
    "currentPersonaId": 1
  }
}
```

## Business Rules

### Validation Rules
- Cannot change own role
- Cannot delete last ADMIN user
- Cannot change last ADMIN to USER role
- Cannot delete self
- Cannot change own status
- Email must be unique across all users

### Soft Delete
When a user is deleted:
- `status` is set to "deleted"
- `deletedAt` is set to current timestamp
- User data is preserved in database

## Architecture Compliance

This implementation follows the project constitution:
- **Controller → Service → Repository** layer structure
- No business logic in Controller (only request/response handling)
- All validation and business rules in Service layer
- Database schema changes via SQL migration file

## Testing Recommendations

1. **List Users API**
   - Test pagination (different page sizes)
   - Test filtering by role
   - Test filtering by status
   - Test search functionality

2. **Get User Detail**
   - Verify stats are correctly calculated
   - Test with user that has no messages
   - Test with user that has no affinity score

3. **Update User**
   - Test email uniqueness validation
   - Test partial updates (only email, only username)
   - Test with invalid email format

4. **Delete User**
   - Verify soft delete (status and deletedAt are set)
   - Test cannot delete self
   - Test cannot delete last ADMIN

5. **Change Role**
   - Test cannot change own role
   - Test cannot change last ADMIN to USER
   - Test successful role change

6. **Change Status**
   - Test cannot change own status
   - Test all valid statuses (active, suspended, deleted)
   - Test invalid status is rejected

## Files Created/Modified

### Created Files:
1. `/Users/jimin/lobai/lobai/backend/src/main/java/com/lobai/dto/request/UpdateUserRequest.java`
2. `/Users/jimin/lobai/lobai/backend/src/main/java/com/lobai/dto/request/ChangeRoleRequest.java`
3. `/Users/jimin/lobai/lobai/backend/src/main/java/com/lobai/dto/request/ChangeStatusRequest.java`
4. `/Users/jimin/lobai/lobai/backend/src/main/java/com/lobai/dto/response/UserListResponse.java`
5. `/Users/jimin/lobai/lobai/backend/src/main/java/com/lobai/dto/response/UserDetailResponse.java`
6. `/Users/jimin/lobai/lobai/backend/src/main/java/com/lobai/service/UserAdminService.java`
7. `/Users/jimin/lobai/lobai/backend/src/main/java/com/lobai/controller/UserAdminController.java`

### Modified Files:
1. `/Users/jimin/lobai/lobai/backend/src/main/java/com/lobai/entity/User.java`
2. `/Users/jimin/lobai/lobai/backend/src/main/java/com/lobai/repository/UserRepository.java`
3. `/Users/jimin/lobai/lobai/backend/src/main/resources/db/schema.sql`

## Next Steps

1. **Database Migration**: Run the updated schema.sql or apply the changes manually to your existing database
2. **Testing**: Create integration tests for all endpoints
3. **Frontend Integration**: Update admin panel to use these new endpoints
4. **Documentation**: Update API documentation/Swagger if applicable

## Security Notes

- All endpoints require ADMIN role
- Uses Spring Security's `@PreAuthorize` annotation
- Business rules prevent accidental admin lockout
- Soft delete preserves audit trail
