import {
  Router
} from "express";

import {
  createSos,
  getActiveSos
} from "../controllers/sos.controller";

import {
  requireAuth
} from "../middlewares/auth.middleware";

const router = Router();

router.post(
  "/",
  requireAuth,
  createSos
);

router.get(
  "/active",
  requireAuth,
  getActiveSos
);

export default router;
