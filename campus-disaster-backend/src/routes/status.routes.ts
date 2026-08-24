import { Router } from "express";
import { listLatestStatuses, updateStatus } from "../controllers/status.controller";
import { requireAuth, requireRole } from "../middlewares/auth.middleware";

const router = Router();

router.post("/update", requireAuth, updateStatus);
router.get("/latest", requireAuth, requireRole("STAFF", "ADMIN"), listLatestStatuses);

export default router;
