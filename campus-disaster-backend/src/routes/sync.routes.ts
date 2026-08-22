import {
  Router
} from "express";

import {
  sync
} from "../controllers/sync.controller";

import {
  requireAuth
} from "../middlewares/auth.middleware";

const router = Router();

router.post(
  "/",
  requireAuth,
  sync
);

export default router;
