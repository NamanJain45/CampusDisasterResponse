import { Response } from "express";
import { prisma } from "../config/db";
import { AuthenticatedRequest } from "../middlewares/auth.middleware";

export async function listIncidentHistory(req: AuthenticatedRequest, res: Response): Promise<void> {
  const reports = await prisma.emergencyReport.findMany({
    where: { status: { in: ["VERIFIED", "REJECTED", "ACTIVE", "RESOLVED"] } },
    include: { createdBy: { select: { id: true, name: true, email: true, role: true } }, reviewedBy: { select: { id: true, name: true, email: true, role: true } } },
    orderBy: { updatedAt: "desc" }, take: 200
  });
  res.json(reports);
}

export async function listAlertHistory(_req: AuthenticatedRequest, res: Response): Promise<void> {
  res.json(await prisma.emergency.findMany({ orderBy: { createdAt: "desc" }, take: 200 }));
}

export async function listAuditHistory(_req: AuthenticatedRequest, res: Response): Promise<void> {
  res.json(await prisma.auditLog.findMany({ include: { actor: { select: { id: true, name: true, email: true, role: true } } }, orderBy: { createdAt: "desc" }, take: 300 }));
}

export async function listMapIncidents(req: AuthenticatedRequest, res: Response): Promise<void> {
  const active = await prisma.emergency.findMany({ where: { active: true }, orderBy: { createdAt: "desc" }, take: 100 });
  const reports = await prisma.emergencyReport.findMany({
    where: req.user?.role === "STUDENT" ? { status: "VERIFIED" } : { status: { in: ["VERIFIED", "ACTIVE", "RESOLVED"] } },
    select: { id: true, type: true, locationText: true, latitude: true, longitude: true, status: true, createdAt: true, updatedAt: true },
    orderBy: { updatedAt: "desc" }, take: 200
  });
  res.json({ activeEmergencies: active, incidents: reports });
}
