import { Router } from "express";
import {
  createReport,
  listMyReports,
  listPendingReports,
  reviewReport
} from "../controllers/report.controller";
import { requireAuth, requireRole } from "../middlewares/auth.middleware";

const router = Router();

router.use(requireAuth);
router.post("/", createReport);
router.get("/mine", listMyReports);
router.get("/pending", requireRole("STAFF", "ADMIN"), listPendingReports);
router.patch("/:id/review", requireRole("STAFF", "ADMIN"), reviewReport);

export default router;
