# Admin User Management API - Quick Reference

## Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/admin/users` | List all users with pagination | ADMIN |
| GET | `/api/admin/users/{id}` | Get user details with stats | ADMIN |
| PUT | `/api/admin/users/{id}` | Update user profile | ADMIN |
| DELETE | `/api/admin/users/{id}` | Soft delete user | ADMIN |
| PATCH | `/api/admin/users/{id}/role` | Change user role | ADMIN |
| PATCH | `/api/admin/users/{id}/status` | Change user status | ADMIN |

## Query Parameters (GET /api/admin/users)

- `page` (int, default: 0) - Page number
- `size` (int, default: 25) - Items per page
- `role` (string) - Filter by role: USER, ADMIN
- `status` (string) - Filter by status: active, suspended, deleted
- `search` (string) - Search by email or username

## Request Bodies

### Update User (PUT /api/admin/users/{id})
```json
{
  "email": "newemail@example.com",
  "username": "New Username"
}
```

### Change Role (PATCH /api/admin/users/{id}/role)
```json
{
  "role": "ADMIN"  // or "USER"
}
```

### Change Status (PATCH /api/admin/users/{id}/status)
```json
{
  "status": "suspended"  // or "active", "deleted"
}
```

## Business Rules

| Rule | Description |
|------|-------------|
| Self-Protection | Cannot change own role or status |
| Admin Protection | Cannot delete last ADMIN user |
| Admin Protection | Cannot change last ADMIN to USER |
| Email Uniqueness | Email must be unique across all users |
| Soft Delete | Delete preserves data (sets status + deletedAt) |

## Response Format

All responses follow this structure:
```json
{
  "success": true,
  "message": "Operation description",
  "data": { /* response data */ }
}
```

## User List Response
```json
{
  "id": 1,
  "email": "user@example.com",
  "username": "User Name",
  "role": "USER",
  "status": "active",
  "createdAt": "2025-01-01T00:00:00",
  "lastLoginAt": "2025-01-02T12:00:00"
}
```

## User Detail Response
```json
{
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
```

## Error Responses

```json
{
  "success": false,
  "message": "Error description",
  "errorCode": "ERROR_CODE"
}
```

Common errors:
- `IllegalArgumentException` - Invalid input (e.g., user not found, duplicate email)
- `IllegalStateException` - Business rule violation (e.g., cannot delete last ADMIN)

## Database Changes

Run this migration on your database:

```sql
ALTER TABLE users
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'active'
COMMENT '사용자 상태 (active, suspended, deleted)';

ALTER TABLE users
ADD COLUMN deleted_at TIMESTAMP
COMMENT '삭제 시각 (soft delete)';

ALTER TABLE users
ADD INDEX idx_status (status);
```

Or recreate the database using the updated `schema.sql` file.

## Testing Checklist

- [ ] List users with pagination
- [ ] Filter users by role
- [ ] Filter users by status
- [ ] Search users by email/username
- [ ] Get user detail with stats
- [ ] Update user email
- [ ] Update user username
- [ ] Change user role (USER → ADMIN)
- [ ] Change user role (ADMIN → USER)
- [ ] Change user status (active → suspended)
- [ ] Soft delete user
- [ ] Verify cannot change own role
- [ ] Verify cannot delete last ADMIN
- [ ] Verify email uniqueness validation

## curl Examples

### List Users
```bash
curl -X GET "http://localhost:8080/api/admin/users?page=0&size=25" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### Get User Detail
```bash
curl -X GET "http://localhost:8080/api/admin/users/1" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### Update User
```bash
curl -X PUT "http://localhost:8080/api/admin/users/1" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"email":"new@example.com","username":"New Name"}'
```

### Change Role
```bash
curl -X PATCH "http://localhost:8080/api/admin/users/1/role" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"role":"ADMIN"}'
```

### Change Status
```bash
curl -X PATCH "http://localhost:8080/api/admin/users/1/status" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"suspended"}'
```

### Delete User
```bash
curl -X DELETE "http://localhost:8080/api/admin/users/1" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

## Implementation Files

| File | Path |
|------|------|
| Controller | `/backend/src/main/java/com/lobai/controller/UserAdminController.java` |
| Service | `/backend/src/main/java/com/lobai/service/UserAdminService.java` |
| Request DTOs | `/backend/src/main/java/com/lobai/dto/request/` |
| Response DTOs | `/backend/src/main/java/com/lobai/dto/response/` |
| Entity | `/backend/src/main/java/com/lobai/entity/User.java` |
| Repository | `/backend/src/main/java/com/lobai/repository/UserRepository.java` |
| Schema | `/backend/src/main/resources/db/schema.sql` |
