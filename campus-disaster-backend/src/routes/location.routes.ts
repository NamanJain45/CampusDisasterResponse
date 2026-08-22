import {
  Router
} from "express";

import {
  updateLocations
} from "../controllers/location.controller";

import {
  requireAuth
} from "../middlewares/auth.middleware";

const router = Router();

router.post(
  "/update",
  requireAuth,
  updateLocations
);

export default router;
