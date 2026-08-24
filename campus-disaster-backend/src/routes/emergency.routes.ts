import { Router } from "express";
import { activateEmergency, getActiveAlerts, resolveEmergency } from "../controllers/emergency.controller";
import { requireAuth, requireRole } from "../middlewares/auth.middleware";

const router = Router();

router.post("/activate", requireAuth, requireRole("STAFF", "ADMIN"), activateEmergency);
router.get("/alerts", requireAuth, getActiveAlerts);
router.patch("/:id/resolve", requireAuth, requireRole("STAFF", "ADMIN"), resolveEmergency);

export default router;
