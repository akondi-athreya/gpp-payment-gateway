# Railway Deployment Guide

## Overview
This guide explains how to deploy the Payment Gateway to Railway with proper configuration.

## Deployment Steps

### 1. Create Railway Project
- Go to [railway.app](https://railway.app)
- Create new project → Empty project
- Add PostgreSQL service
- Add Redis service  
- Add a new service from GitHub repository

### 2. Database Setup (PostgreSQL)

Railway provides PostgreSQL as a managed service. The connection string will be automatically set as `DATABASE_URL` environment variable.

**Verify PostgreSQL is running:**
- Service should show Status: "Running"
- Check Variables tab for `DATABASE_URL` (starts with `postgresql://`)

### 3. Redis Setup

Railway provides Redis as a managed service. Configure environment variables:

**Required Environment Variables for Redis:**
```
REDIS_HOST=<redis-service-hostname>
REDIS_PORT=6379
```

Or use `REDIS_URL` if available:
```
REDIS_URL=redis://<redis-service-hostname>:6379
```

**To get Redis hostname:**
- Click Redis service in Railway dashboard
- Go to "Variables" tab
- Copy the hostname (or use the `REDIS_HOST` environment variable Railway provides)

### 4. Backend Service Configuration

**Environment Variables (Must be set in Railway dashboard):**

| Variable | Value | Notes |
|----------|-------|-------|
| `PORT` | `8080` | Railway assigns a port; this is the listen port |
| `DATABASE_URL` | `postgresql://...` | Auto-provided by PostgreSQL service |
| `DATABASE_USERNAME` | Auto-filled | From PostgreSQL service |
| `DATABASE_PASSWORD` | Auto-filled | From PostgreSQL service |
| `REDIS_HOST` | From Redis service | Hostname of Redis container |
| `REDIS_PORT` | `6379` | Default Redis port |
| `TEST_MODE` | `false` | Set to true for test environment |
| `HEALTH_REDIS_OPTIONAL` | `false` | Require Redis for health check |
| `HEALTH_WORKER_OPTIONAL` | `true` | Worker is optional for health check |

### 5. Deploy Backend

1. In Railway dashboard, click "New" → "Service"
2. Select your GitHub repository
3. Railway will auto-detect the Dockerfile in root directory
4. Set environment variables in the service dashboard (Variables tab)
5. Click "Deploy" to trigger build

**Build Information:**
- Build uses multi-stage Docker build
- First stage: `eclipse-temurin:21-jdk-alpine` (Maven build)
- Second stage: `eclipse-temurin:21-jre` (Runtime)
- Build time: ~30 seconds
- Runtime listens on `PORT` environment variable

### 6. Verify Deployment

Once deployed, test the service:

```bash
# Get the public URL from Railway dashboard
BACKEND_URL=https://payment-gateway-api-production.railway.app

# Test health endpoint
curl $BACKEND_URL/health

# Expected response:
# {"status":"UP","components":{"db":{"status":"UP"},"redis":{"status":"UP","components":{"ping":{"status":"UP"}}},"diskSpace":{"status":"UP"}}}
```

### 7. Troubleshooting

#### Container fails to start with "executable `cd` not found"
**Solution:**
- Ensure all environment variables are set (especially `DATABASE_URL`)
- Check PostgreSQL service is running and connected
- Look at Railway logs: Service → Logs tab
- Try triggering a redeploy (Service → Deploy)

#### Database connection errors
- Verify `DATABASE_URL` is set correctly in Variables
- Check PostgreSQL service is running
- Ensure the database is initialized (Spring auto-creates tables with `ddl-auto=update`)

#### Redis connection errors
- Verify `REDIS_HOST` and `REDIS_PORT` are set
- Check Redis service is running and visible in dashboard
- Ensure `health.redis.optional=false` matches your setup

#### Build fails
- Check Maven build log in Railway Logs
- Ensure `pom.xml` and `backend/src` are present
- Verify Java 21 is available in build environment

## Service Connectivity

**From frontend to backend:**
- Frontend apps (dashboard, checkout) need the backend service's public URL
- Set `VITE_API_BASE_URL` environment variable in frontend services
- Example: `https://payment-gateway-api-production.railway.app`

## Production Checklist

- [ ] PostgreSQL service created and running
- [ ] Redis service created and running
- [ ] All environment variables set in backend service
- [ ] Backend service deployed and running
- [ ] Health endpoint returns `UP` status
- [ ] Database tables created (check `orders`, `payments`, `merchants`)
- [ ] Frontend services configured with backend API URL
- [ ] CORS properly configured (check backend `SecurityConfig`)

## Notes

- Railway automatically assigns a public domain (e.g., `payment-gateway-api-production.railway.app`)
- Database and Redis use internal networking (Railway's private network)
- PORT environment variable is dynamically assigned by Railway (usually 8080)
- All data persists in managed PostgreSQL and Redis services
