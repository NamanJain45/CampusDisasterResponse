import cors from "cors";
import express from "express";
import helmet from "helmet";

import authRoutes from "./routes/auth.routes";
import emergencyRoutes from "./routes/emergency.routes";
import locationRoutes from "./routes/location.routes";
import sosRoutes from "./routes/sos.routes";
import statusRoutes from "./routes/status.routes";
import syncRoutes from "./routes/sync.routes";

import {
  errorHandler
} from "./middlewares/errorHandler.middleware";

const app = express();

app.use(helmet());
app.use(cors());
app.use(express.json());

app.get(
  "/health",
  (_req, res) => {
    res.json({
      status: "ok",
      service:
        "campus-disaster-backend"
    });
  }
);

app.use(
  "/api/v1/auth",
  authRoutes
);

app.use(
  "/api/v1/emergency",
  emergencyRoutes
);

app.use(
  "/api/v1/status",
  statusRoutes
);

app.use(
  "/api/v1/sos",
  sosRoutes
);

app.use(
  "/api/v1/locations",
  locationRoutes
);

app.use(
  "/api/v1/sync",
  syncRoutes
);

app.use(errorHandler);

export default app;
