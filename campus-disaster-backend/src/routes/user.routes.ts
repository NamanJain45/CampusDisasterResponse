import { Router } from "express";
import { createUser, deleteUser, listUsers } from "../controllers/user.controller";
import { requireAuth, requireRole } from "../middlewares/auth.middleware";

const router = Router();
router.use(requireAuth, requireRole("ADMIN"));
router.get("/", listUsers);
router.post("/", createUser);
router.delete("/:id", deleteUser);
export default router;
