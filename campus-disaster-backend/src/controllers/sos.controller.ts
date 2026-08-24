import { Response } from "express";
import { prisma } from "../config/db";
import { AuthenticatedRequest } from "../middlewares/auth.middleware";
import { notifyUsers } from "../services/notification.service";

export async function createSos(req: AuthenticatedRequest, res: Response): Promise<void> {
  if (!req.user) { res.status(401).json({ message: "Authentication required" }); return; }
  const { latitude, longitude, message, clientId } = req.body;

  const sos = await prisma.sOS.create({
    data: {
      userId: req.user.id,
      latitude: typeof latitude === "number" ? latitude : null,
      longitude: typeof longitude === "number" ? longitude : null,
      message: typeof message === "string" ? message.trim() || null : null,
      clientId: typeof clientId === "string" ? clientId : null
    }
  });

  await prisma.auditLog.create({
    data: { actorId: req.user.id, action: "SOS_CREATED", entityType: "SOS", entityId: sos.id, details: sos.message }
  });

  await notifyUsers({
    type: "SOS",
    title: "High-priority SOS",
    message: sos.message || "A campus user has triggered an SOS.",
    relatedType: "SOS",
    relatedId: sos.id,
    roles: ["STAFF", "ADMIN"]
  });

  res.status(201).json(sos);
}

export async function getActiveSos(req: AuthenticatedRequest, res: Response): Promise<void> {
  if (!req.user) { res.status(401).json({ message: "Authentication required" }); return; }
  const events = await prisma.sOS.findMany({
    where: { active: true },
    include: { user: { select: { id: true, name: true, email: true, role: true } } },
    orderBy: { createdAt: "desc" }
  });
  res.json(events);
}
