# 🎬 CINÉMA - Movie Collection Manager

A single-file Java web application for managing a personal movie collection, built with embedded Jetty server.

## Features

- Add, view, and delete movies
- Sort by rating, year, or filter top-rated films
- Live statistics (film count, avg rating, top genre)
- Dark theme UI with gold accents
- No database — in-memory storage

## Tech Stack

- **Java 17** (Servlet)
- **Embedded Jetty** (no Tomcat needed)
- Single-file architecture (`movie.java`)

## Run Locally

```bash
# Compile
javac -encoding UTF-8 -cp "jetty-server.jar:jetty-servlet.jar:jetty-util.jar:jetty-http.jar:jetty-io.jar:jetty-security.jar:servlet-api.jar" movie.java

# Package
jar cfm cinema.jar MANIFEST.MF movie.class MovieData.class MovieManager.class

# Run
java -cp "cinema.jar:jetty-server.jar:jetty-servlet.jar:jetty-util.jar:jetty-http.jar:jetty-io.jar:jetty-security.jar:servlet-api.jar:." movie
```

Open **http://localhost:9090/** in your browser.

## Deploy on Render (Free Tier)

See the step-by-step guide below.

---

## 📋 Render Deployment Guide

### Prerequisites
- A [GitHub](https://github.com) account
- A [Render](https://render.com) account (sign up free with GitHub)

### Step 1 — Push to GitHub

1. Go to [github.com/new](https://github.com/new) and create a new repository (e.g. `cinema-app`)
2. Open a terminal in this project folder and run:

```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/cinema-app.git
git push -u origin main
```

### Step 2 — Create a Web Service on Render

1. Go to [dashboard.render.com](https://dashboard.render.com)
2. Click **"New +"** → **"Web Service"**
3. Connect your GitHub account if not already connected
4. Select your **cinema-app** repository
5. Fill in the settings:

| Setting | Value |
|---|---|
| **Name** | `cinema-app` (or anything you like) |
| **Region** | Pick the closest to you |
| **Runtime** | **Docker** |
| **Instance Type** | **Free** |

> **Why Docker?** Render's free native runtimes don't include Java. But don't worry — you don't need Docker installed on your computer. Render builds it in the cloud automatically using the Dockerfile in your repo. You never touch Docker yourself.

6. Click **"Deploy Web Service"**

### Step 3 — Wait for Deploy

- Render will build and deploy your app (takes 2-3 minutes)
- Once the status shows **"Live"**, click the URL at the top (e.g. `https://cinema-app-xxxx.onrender.com`)
- 🎉 Your app is live!

### Notes

- **Free tier sleeps after 15 min of inactivity** — first visit after sleep takes ~30 seconds to wake up
- **Data resets on redeploy** since storage is in-memory
- Every push to `main` triggers auto-redeploy
