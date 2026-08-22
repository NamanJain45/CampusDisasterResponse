import {
  Router
} from "express";

import {
  activateEmergency,
  getActiveAlerts
} from "../controllers/emergency.controller";

import {
  requireAuth,
  requireRole
} from "../middlewares/auth.middleware";

const router = Router();

router.post(
  "/activate",
  requireAuth,
  requireRole(
    "STAFF",
    "ADMIN"
  ),
  activateEmergency
);

router.get(
  "/alerts",
  requireAuth,
  getActiveAlerts
);

export default router;
