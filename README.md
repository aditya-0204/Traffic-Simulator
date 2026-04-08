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
|-- frontend/               # Frontend application
|   |-- src/
|   |-- docs/
|   `-- package.json
|-- backend/                # Backend application
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
Set-Location .\<backend-directory>
.\gradlew.bat bootRun
```

The backend uses:

- `http://localhost:8080` in the default profile
- `http://localhost:80` in the `prod` profile

### 2. Start the frontend

Open a second terminal:

```powershell
Set-Location .\<frontend-directory>
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
.\gradlew.bat build
.\gradlew.bat test
.\gradlew.bat bootRun
```

You can also build a runnable jar:

```powershell
.\gradlew.bat build
java -jar .\build\libs\<server-jar-name>.jar
```

## Environment and Configuration

The backend exposes a few useful environment variables:

- frontend URL CORS setting: defaults to `http://localhost:5173`
- allow-all-origins CORS setting: can be enabled for local testing

If SUMO native libraries are not already on your system path, you may need to launch Java with:

```powershell
java -Djava.library.path="C:\path\to\sumo\bin" -jar .\build\libs\<server-jar-name>.jar
```

## Main API Endpoints

### REST

- `POST /simulation` - create and store a new simulation network
- `PUT /simulation/{id}` - update a stored simulation
- `DELETE /simulation/{id}` - delete a simulation
- `GET /simulation/{id}` - get simulation metadata
- `GET /simulations` - list all simulations
- `GET /simulation/{id}/network` - export the stored network JSON
- `GET /simulation/{id}/output/statistics` - get statistics from a finished run
- `GET /simulation/{id}/output/tripinfo`
- `GET /simulation/{id}/output/netstate`
- `GET /simulation/{id}/output/summary`

### WebSocket / STOMP

- endpoint: `/simulation-socket`
- publish destination: `/app/simulation/{id}`
- broadcast topic: `/topic/simulation/{id}`
- error topic: `/topic/simulation/{id}/error`

## Key Frontend Features

- canvas-based road network editing
- undo/redo support using command-pattern helpers
- property editors for roads and intersections
- project download/upload as JSON
- simulation timer and live playback controls
- simulation history sidebar with detailed metrics

## Documentation

Additional project docs already in the repository:

- frontend user guide: `docs/user-guide.md` inside the frontend directory
- frontend references: `RESOURCES.md` inside the frontend directory
- backend README: `README.md` inside the backend directory

## Troubleshooting

### Backend fails with `UnsatisfiedLinkError`

SUMO native libraries are not being found. Add the SUMO `bin` directory containing `libtracijni` to your system path, or pass it through `-Djava.library.path`.

### Frontend shows CORS errors

Check the backend CORS environment settings and make sure the frontend origin is allowed.

### Frontend cannot connect to simulations

Make sure the backend is running on `localhost:8080`. The frontend currently hardcodes that backend address in its simulation URL configuration file.

## Notes

- The frontend is designed for desktop screens.
- The backend includes Docker-related files, but local development is easiest when SUMO is correctly installed on the host machine.
- Some simulation/output endpoints only return data after a run has completed successfully.
