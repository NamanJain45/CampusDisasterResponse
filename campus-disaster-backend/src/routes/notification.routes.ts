import { Router } from "express";
import { requireAuth } from "../middlewares/auth.middleware";
import { listNotifications, markAllNotificationsRead, markNotificationRead } from "../controllers/notification.controller";

const router = Router();
router.use(requireAuth);
router.get("/", listNotifications);
router.patch("/:id/read", markNotificationRead);
router.post("/read-all", markAllNotificationsRead);
export default router;
