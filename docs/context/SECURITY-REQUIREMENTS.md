# LEXIA - Security Requirements

## Authentication & Authorization
- JWT-based authentication with access + refresh tokens
- Refresh tokens must be hashed before storage
- Implement token rotation for multi-device security
- Refresh tokens must include device fingerprint
- Access token expiration: 15 minutes
- Refresh token expiration: 7 days

## Encryption & Hashing
- Password hashing: Bcrypt (min cost factor 12)
- All tokens stored as SHA-256 hash
- Database encryption at rest (if production)
- HTTPS enforced (HSTS headers)

## Input Validation
- Validate all user inputs (email, password, text)
- Sanitize to prevent XSS and SQL injection
- Rate limiting on auth endpoints (5 attempts/5 min)

## Data Privacy
- Never log passwords or tokens
- Never store plain-text tokens
- Comply with data retention policies
- User can delete their data

## API Security
- CORS configured (only allowed origins)
- CSRF protection enabled
- X-Frame-Options, X-Content-Type-Options headers
- API versioning (/api/v1)
