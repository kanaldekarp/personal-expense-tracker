# QuickExpense — Free Permanent Hosting Guide

Deploy your QuickExpense app for **free forever** using **Render** (web hosting) + **Neon** (PostgreSQL database).

---

## Step 1: Create a Free PostgreSQL Database (Neon)

1. Go to **https://neon.tech** and sign up (use GitHub login)
2. Click **"New Project"** → name it `quickexpense`
3. Choose the **Free** plan and region closest to you
4. Once created, go to your project **Dashboard**
5. Click **"SQL Editor"** in the left sidebar
6. Open the file `schema.sql` from this project, **copy ALL the SQL**, paste it into the SQL Editor, and click **Run**
7. All 9 tables will be created
8. Go to **Dashboard** → copy the **Connection string** (it looks like: `postgresql://username:password@host/dbname?sslmode=require`)
   - Save this — you'll need it in Step 3

---

## Step 2: Push Code to GitHub

Open a terminal in this project folder and run:

```bash
git init
git add .
git commit -m "Initial commit - QuickExpense Tracker"
```

Then create a new repository on GitHub:
1. Go to **https://github.com/new**
2. Name it `quickexpense-tracker` (or anything you like)
3. Keep it **Public** or **Private** (your choice)
4. Do NOT initialize with README
5. Click **Create Repository**
6. Copy the commands shown and run them:

```bash
git remote add origin https://github.com/YOUR_USERNAME/quickexpense-tracker.git
git branch -M main
git push -u origin main
```

> **Note:** Your `db.properties` file (with local passwords) is already in `.gitignore` and will NOT be uploaded to GitHub.

---

## Step 3: Deploy on Render (Free Forever)

1. Go to **https://render.com** and sign up (use GitHub login)
2. Click **"New +"** → **"Web Service"**
3. Connect your GitHub account and select the `quickexpense-tracker` repository
4. Configure the service:
   - **Name:** `quickexpense`
   - **Region:** Choose the one closest to you
   - **Runtime:** `Docker`
   - **Instance Type:** `Free`
5. Click **"Advanced"** → **"Add Environment Variable"** and add these:

| Key | Value |
|-----|-------|
| `DATABASE_URL` | Your Neon connection string from Step 1 (e.g., `postgresql://user:pass@host/dbname?sslmode=require`) |
| `SMTP_EMAIL` | `noreplycomitly@gmail.com` |
| `SMTP_PASSWORD` | `iaxi pifm ygfj fhel` |
| `SMTP_HOST` | `smtp.gmail.com` |
| `SMTP_PORT` | `587` |
| `SMTP_SENDER_NAME` | `QuickExpense` |

6. Click **"Create Web Service"**
7. Wait 5-10 minutes for the first build to complete
8. Your app will be live at: **`https://quickexpense.onrender.com`** (or similar URL shown in the dashboard)

---

## Important Notes

### Free Tier Behavior
- **Render free tier**: Your app will **spin down after 15 minutes of inactivity**. The first request after that takes ~30-60 seconds to wake up. This is normal for free hosting.
- **Neon free tier**: 0.5 GB storage, always-on. More than enough for personal use.

### Updating Your App
After making changes locally, just push to GitHub and Render will automatically redeploy:
```bash
git add .
git commit -m "Your update message"
git push
```

### Custom Domain (Optional)
In Render dashboard → your service → **Settings** → **Custom Domains** to connect your own domain.

### Local Development
Your local setup continues to work unchanged — it reads from `db.properties` as before.
Environment variables only apply on Render.

---

## Summary of What Was Set Up

| File | Purpose |
|------|---------|
| `Dockerfile` | Multi-stage build: Maven → Tomcat 10.1 |
| `.gitignore` | Excludes credentials, build files from Git |
| `.dockerignore` | Excludes unnecessary files from Docker build |
| `render.yaml` | Render deployment blueprint |
| `schema.sql` | Complete database schema for cloud setup |
| `db.properties.example` | Template for local development config |

### Code Changes
- `DBConnection.java` — Now reads `DATABASE_URL` env var (falls back to `db.properties` locally)
- `EmailService.java` — Now reads `SMTP_*` env vars (falls back to `db.properties` locally)
- `pom.xml` — Java version changed from 22 to 21 (Docker compatibility)
