import { Response } from "express";
import { prisma } from "../config/db";
import { AuthenticatedRequest } from "../middlewares/auth.middleware";

export async function listIncidentHistory(req: AuthenticatedRequest, res: Response): Promise<void> {
  const reports = await prisma.emergencyReport.findMany({
    where: { status: { in: ["VERIFIED", "REJECTED", "ACTIVE", "RESOLVED"] } },
    include: {
      createdBy: { select: { id: true, name: true, email: true, role: true } },
      reviewedBy: { select: { id: true, name: true, email: true, role: true } }
    },
    orderBy: { updatedAt: "desc" },
    take: 200
  });
  res.json(reports);
}

export async function listAlertHistory(_req: AuthenticatedRequest, res: Response): Promise<void> {
  const alerts = await prisma.emergency.findMany({
    orderBy: { createdAt: "desc" },
    take: 200
  });
  res.json(alerts);
}

export async function listAuditHistory(_req: AuthenticatedRequest, res: Response): Promise<void> {
  const logs = await prisma.auditLog.findMany({
    include: { actor: { select: { id: true, name: true, email: true, role: true } } },
    orderBy: { createdAt: "desc" },
    take: 300
  });
  res.json(logs);
}
