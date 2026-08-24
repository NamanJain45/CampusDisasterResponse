import { Router } from "express";
import { createSos, getActiveSos, resolveSos } from "../controllers/sos.controller";
import { requireAuth, requireRole } from "../middlewares/auth.middleware";

const router = Router();
router.post("/", requireAuth, createSos);
router.get("/active", requireAuth, requireRole("STAFF", "ADMIN"), getActiveSos);
router.patch("/:id/resolve", requireAuth, requireRole("STAFF", "ADMIN"), resolveSos);
export default router;
