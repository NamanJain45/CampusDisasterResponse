import { Router } from "express";
import { requireAuth, requireRole } from "../middlewares/auth.middleware";
import { listAlertHistory, listAuditHistory, listIncidentHistory } from "../controllers/history.controller";

const router = Router();
router.use(requireAuth);
router.get("/incidents", requireRole("STAFF", "ADMIN"), listIncidentHistory);
router.get("/alerts", listAlertHistory);
router.get("/audit", requireRole("STAFF", "ADMIN"), listAuditHistory);
export default router;
