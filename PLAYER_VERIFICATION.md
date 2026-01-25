# Player Verification System

## Overview

The player management system uses a simple code-based verification approach to control who can add verified players and who can edit/delete existing players. This is designed to be lightweight and future-proof for when proper authentication is added.

## How It Works

### Verification Code

The verification code is configured via environment variable:
- **Environment Variable**: `VERIFICATION_SECRET`
- **Default Value**: `default-secret-change-me` (configured in `application.yml`)
- **Production**: Set this to a secure value in your deployment environment

### Player States

Players can be in one of two states:
- **Verified** (✓): Player is confirmed and available in draft pools
- **Pending** (⏳): Player is awaiting verification by an admin

### Operations

#### 1. Add Player
- **Endpoint**: `POST /api/players/manage`
- **Code Required**: Optional
- **Behavior**:
  - If verification code is provided and correct → Player is created as **verified**
  - If no code or incorrect code → Player is created as **pending**
- **Use Case**: Anyone can submit players, but only admins can verify them immediately

#### 2. Edit Player
- **Endpoint**: `PUT /api/players/manage/{id}`
- **Code Required**: Yes (in request body as `verificationCode`)
- **Behavior**: Only users with the correct verification code can edit players
- **Use Case**: Prevents unauthorized modifications to player data

#### 3. Delete Player
- **Endpoint**: `DELETE /api/players/manage/{id}?code={code}`
- **Code Required**: Yes (as query parameter)
- **Behavior**: Only users with the correct verification code can delete players
- **Use Case**: Prevents unauthorized deletion of player data

#### 4. Verify Pending Player
- **Endpoint**: `POST /api/players/manage/{id}/verify?code={code}`
- **Code Required**: Yes (as query parameter)
- **Behavior**: Changes a pending player to verified status
- **Use Case**: Admins can verify community-submitted players

#### 5. Get All Players (Management)
- **Endpoint**: `GET /api/players/manage`
- **Code Required**: No
- **Behavior**: Returns all players (verified and pending) with full details
- **Use Case**: Player management interface

**Note**: The original `GET /api/players` endpoint still exists and returns only verified players with ADP data for draft purposes.

## Frontend Implementation

### Player Management Page
- Located at `/player-management`
- Features:
  - View all players with verified/pending status
  - Filter by status (All, Verified, Pending)
  - Add new players with optional verification code
  - Edit existing players (requires code)
  - Delete players (requires code)

### Add Player Modal
- Name, Position, Team, College, Draft Year fields
- Optional "Verification Code" field
- Help text explains that leaving code blank creates a pending player

### Edit/Delete Modals
- Require verification code input
- Show clear error messages for invalid codes
- Prevent accidental modifications

## Security Considerations

### Current Approach
- Simple shared secret for verification
- No user accounts or sessions
- Code is transmitted in requests (use HTTPS in production)

### Future-Proofing
This design makes it easy to migrate to proper authentication:

1. **Add Spring Security** when ready
2. **Replace code checks** with role-based access control (RBAC)
3. **Keep the same endpoints** - just change authorization logic
4. **Frontend changes minimal** - replace code input with login flow

### Migration Path

```java
// Current (code-based)
if (!verificationService.isValidCode(code)) {
    throw new UnauthorizedException("Invalid code");
}

// Future (role-based)
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<PlayerResponse> updatePlayer(...) {
    // No code check needed
}
```

## Configuration

### Development
```yaml
app:
  verification:
    secret: dev-secret-123
```

### Production
Set environment variable:
```bash
export VERIFICATION_SECRET="your-secure-random-string-here"
```

Or in Railway/cloud platform:
```
VERIFICATION_SECRET=your-secure-random-string-here
```

## API Examples

### Add Verified Player (with code)
```bash
curl -X POST http://localhost:8080/api/players/manage \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "position": "QB",
    "team": "Team A",
    "college": "State University",
    "draftyear": 2025,
    "verificationCode": "your-secret-code"
  }'
```

### Add Pending Player (no code)
```bash
curl -X POST http://localhost:8080/api/players/manage \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Smith",
    "position": "WR",
    "team": "Team B",
    "college": "Tech College",
    "draftyear": 2026
  }'
```

### Edit Player
```bash
curl -X PUT http://localhost:8080/api/players/manage/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe Updated",
    "position": "QB",
    "team": "Team A",
    "college": "State University",
    "draftyear": 2025,
    "verificationCode": "your-secret-code"
  }'
```

### Delete Player
```bash
curl -X DELETE "http://localhost:8080/api/players/manage/1?code=your-secret-code"
```

### Verify Pending Player
```bash
curl -X POST "http://localhost:8080/api/players/manage/1/verify?code=your-secret-code"
```

### Get All Players (Management)
```bash
curl http://localhost:8080/api/players/manage
```

## Testing

1. **Set verification code** in `application.yml` or environment
2. **Access player management** at `http://localhost:8080/player-management`
3. **Test adding** players with and without code
4. **Test editing** with correct and incorrect codes
5. **Test deleting** with correct and incorrect codes
6. **Verify status filtering** works correctly

## Best Practices

1. **Keep the code secret** - don't commit it to version control
2. **Use HTTPS** in production to protect code transmission
3. **Rotate the code** periodically for security
4. **Document who has access** to the verification code
5. **Plan migration** to proper auth when user base grows
