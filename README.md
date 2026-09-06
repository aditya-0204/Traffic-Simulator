# Registration No - 23BPS1185 23BPS1146

# Traffic Simulation Project

This is a full-stack traffic simulation project for designing road networks, running SUMO-based simulations, and reviewing simulation history and output statistics through a visual web interface.

This repository contains:

- a React + Vite frontend used to design networks and visualize simulation results
- a Kotlin + Spring Boot backend that stores networks, starts SUMO simulations, and exposes REST/WebSocket APIs

## Project Overview

The application lets you:

- create intersections and roads on a canvas
- edit road and intersection properties
- save and reload networks as JSON
- run desktop-based traffic simulations
- stream live simulation updates over WebSockets
- review simulation history and output statistics after a run

The frontend is currently desktop-focused. Mobile devices are intentionally blocked in the UI.

## Tech Stack

### Frontend

- React 18
- TypeScript
- Vite
- Tailwind CSS
- Zustand
- Konva / React Konva
- Jest + Testing Library

### Backend

- Kotlin
- Spring Boot 3
- Gradle
- Spring WebSocket + STOMP
- Jackson XML/JSON
- SUMO `libtraci`
- JUnit + JaCoCo

## Repository Structure

```text
project-root/
|-- vite/                   # Frontend application
|   |-- src/
|   |-- docs/
|   `-- package.json
|-- sumo-server/            # Backend application
|   |-- src/
|   |-- demo/
|   `-- build.gradle.kts
`-- README.md
```

## How It Works

1. The frontend builds a traffic network in JSON form.
2. The backend accepts that network through `POST /simulation`.
3. The backend converts and loads the network for SUMO.
4. The frontend connects to `/simulation-socket` using STOMP.
5. Live vehicle updates are published on `/topic/simulation/{id}`.
6. After the run, statistics and outputs can be fetched from backend endpoints.

By default, the frontend talks to `http://localhost:8080` and uses `ws://localhost:8080/simulation-socket`.

## Prerequisites

Before running the full system, install:

- Node.js 18+ and npm
- JDK 17
- SUMO
- the SUMO `libtracijni` native library available on your system path or Java library path

The backend depends on SUMO's Java/native integration. If `libtracijni` is missing, backend startup or tests may fail with `UnsatisfiedLinkError`.

## Quick Start

### 1. Start the backend

From the repository root:

```powershell
Set-Location .\sumo-server
$env:PATH="C:\Program Files (x86)\Eclipse\Sumo\bin;$env:PATH"
.\gradlew.bat bootRun
```

The backend uses:

- `http://localhost:8080` in the default profile
- `http://localhost:80` in the `prod` profile

### 2. Start the frontend

Open a second terminal:

```powershell
Set-Location .\vite
npm install
npm run dev
```

Then open the Vite URL shown in the terminal, typically `http://localhost:5173`.

## Frontend Commands

Run these inside the frontend directory:

```powershell
npm install
npm run dev
npm run build
npm run test
npm run lint
npm run type-check
```

## Backend Commands

Run these inside the backend directory:

```powershell
$env:PATH="C:\Program Files (x86)\Eclipse\Sumo\bin;$env:PATH"
.\gradlew.bat build
$env:PATH="C:\Program Files (x86)\Eclipse\Sumo\bin;$env:PATH"
.\gradlew.bat test
$env:PATH="C:\Program Files (x86)\Eclipse\Sumo\bin;$env:PATH"
.\gradlew.bat bootRun
```

You can also build a runnable jar:

```powershell
.\gradlew.bat build
java -jar .\build\libs\<server-
