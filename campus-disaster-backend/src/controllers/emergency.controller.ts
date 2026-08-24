import { Response } from "express";
import { prisma } from "../config/db";
import { AuthenticatedRequest } from "../middlewares/auth.middleware";
import { notifyUsers } from "../services/notification.service";

export async function activateEmergency(req: AuthenticatedRequest, res: Response): Promise<void> {
  if (!req.user) { res.status(401).json({ message: "Authentication required" }); return; }
  const { title, message, severity = "CRITICAL" } = req.body;

  if (typeof title !== "string" || !title.trim() || typeof message !== "string" || !message.trim()) {
    res.status(400).json({ message: "title and message are required" });
    return;
  }
  if (!["LOW", "MEDIUM", "HIGH", "CRITICAL"].includes(severity)) {
    res.status(400).json({ message: "Invalid emergency severity" });
    return;
  }

  const emergency = await prisma.emergency.create({
    data: { title: title.trim(), message: message.trim(), severity, activatedBy: req.user.id }
  });

  await prisma.auditLog.create({
    data: {
      actorId: req.user.id,
      action: "EMERGENCY_BROADCAST",
      entityType: "Emergency",
      entityId: emergency.id,
      details: `${emergency.severity}: ${emergency.title}`
    }
  });

  await notifyUsers({
    type: "EMERGENCY",
    title: emergency.title,
    message: emergency.message,
    relatedType: "Emergency",
    relatedId: emergency.id
  });

  res.status(201).json(emergency);
}

export async function getActiveAlerts(_req: Request, res: Response): Promise<void> {
  const alerts = await prisma.emergency.findMany({
    where: { active: true },
    orderBy: { createdAt: "desc" }
  });
  res.json(alerts);
}

export async function resolveEmergency(req: AuthenticatedRequest, res: Response): Promise<void> {
  if (!req.user) { res.status(401).json({ message: "Authentication required" }); return; }
  const id = Array.isArray(req.params.id) ? req.params.id[0] : req.params.id;

  const emergency = await prisma.emergency.update({
    where: { id },
    data: { active: false }
  });

  await prisma.auditLog.create({
    data: {
      actorId: req.user.id,
      action: "EMERGENCY_RESOLVED",
      entityType: "Emergency",
      entityId: emergency.id
    }
  });

  await notifyUsers({
    type: "EMERGENCY_RESOLVED",
    title: `Resolved: ${emergency.title}`,
    message: "This campus emergency has been marked resolved.",
    relatedType: "Emergency",
    relatedId: emergency.id
  });

  res.json(emergency);
}
