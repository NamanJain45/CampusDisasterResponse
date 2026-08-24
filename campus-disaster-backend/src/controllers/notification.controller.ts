import { Response } from "express";
import { prisma } from "../config/db";
import { AuthenticatedRequest } from "../middlewares/auth.middleware";

export async function listNotifications(req: AuthenticatedRequest, res: Response): Promise<void> {
  if (!req.user) { res.status(401).json({ message: "Authentication required" }); return; }
  const notifications = await prisma.notification.findMany({
    where: { userId: req.user.id },
    orderBy: { createdAt: "desc" },
    take: 100
  });
  res.json(notifications);
}

export async function markNotificationRead(req: AuthenticatedRequest, res: Response): Promise<void> {
  if (!req.user) { res.status(401).json({ message: "Authentication required" }); return; }
  const id = Array.isArray(req.params.id) ? req.params.id[0] : req.params.id;
  const result = await prisma.notification.updateMany({
    where: { id, userId: req.user.id },
    data: { readAt: new Date() }
  });
  if (result.count === 0) { res.status(404).json({ message: "Notification not found" }); return; }
  res.json({ ok: true });
}

export async function markAllNotificationsRead(req: AuthenticatedRequest, res: Response): Promise<void> {
  if (!req.user) { res.status(401).json({ message: "Authentication required" }); return; }
  await prisma.notification.updateMany({
    where: { userId: req.user.id, readAt: null },
    data: { readAt: new Date() }
  });
  res.json({ ok: true });
}
