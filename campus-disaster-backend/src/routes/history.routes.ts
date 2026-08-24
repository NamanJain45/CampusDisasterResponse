import { Router } from "express";
import { requireAuth, requireRole } from "../middlewares/auth.middleware";
import { listAlertHistory, listAuditHistory, listIncidentHistory, listMapIncidents, listSosHistory } from "../controllers/history.controller";

const router = Router();
router.use(requireAuth);
router.get("/incidents", requireRole("STAFF", "ADMIN"), listIncidentHistory);
router.get("/alerts", listAlertHistory);
router.get("/sos", listSosHistory);
router.get("/audit", requireRole("STAFF", "ADMIN"), listAuditHistory);
router.get("/map", listMapIncidents);
export default router;
