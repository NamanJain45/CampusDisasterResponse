import cors from "cors";
import express from "express";
import helmet from "helmet";

import authRoutes from "./routes/auth.routes";
import emergencyRoutes from "./routes/emergency.routes";
import locationRoutes from "./routes/location.routes";
import sosRoutes from "./routes/sos.routes";
import statusRoutes from "./routes/status.routes";
import syncRoutes from "./routes/sync.routes";
import userRoutes from "./routes/user.routes";
import reportRoutes from "./routes/report.routes";
import notificationRoutes from "./routes/notification.routes";
import historyRoutes from "./routes/history.routes";

import { errorHandler } from "./middlewares/errorHandler.middleware";

const app = express();

app.disable("x-powered-by");
app.use(helmet());
app.use(cors());
app.use(express.json({ limit: "1mb" }));

app.get("/health", (_req, res) => {
  res.json({ status: "ok", service: "campus-disaster-backend" });
});

app.use("/api/v1/auth", authRoutes);
app.use("/api/v1/emergency", emergencyRoutes);
app.use("/api/v1/status", statusRoutes);
app.use("/api/v1/sos", sosRoutes);
app.use("/api/v1/locations", locationRoutes);
app.use("/api/v1/sync", syncRoutes);
app.use("/api/v1/users", userRoutes);
app.use("/api/v1/reports", reportRoutes);
app.use("/api/v1/notifications", notificationRoutes);
app.use("/api/v1/history", historyRoutes);

app.use(errorHandler);

export default app;
