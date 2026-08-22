import {
  Router
} from "express";

import {
  updateStatus
} from "../controllers/status.controller";

import {
  requireAuth
} from "../middlewares/auth.middleware";

const router = Router();

router.post(
  "/update",
  requireAuth,
  updateStatus
);

export default router;
