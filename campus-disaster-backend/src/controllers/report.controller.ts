import { Response } from "express";
import { prisma } from "../config/db";
import { AuthenticatedRequest } from "../middlewares/auth.middleware";

export async function createReport(req: AuthenticatedRequest, res: Response): Promise<void> {
  if (!req.user) {
    res.status(401).json({ message: "Authentication required" });
    return;
  }

  const { type, locationText, description, photoUri, latitude, longitude } = req.body;

  if (typeof type !== "string" || !type.trim() || typeof locationText !== "string" || !locationText.trim()) {
    res.status(400).json({ message: "type and locationText are required" });
    return;
  }

  const report = await prisma.emergencyReport.create({
    data: {
      type: type.trim(),
      locationText: locationText.trim(),
      description: typeof description === "string" ? description.trim() || null : null,
      photoUri: typeof photoUri === "string" ? photoUri.trim() || null : null,
      latitude: typeof latitude === "number" ? latitude : null,
      longitude: typeof longitude === "number" ? longitude : null,
      createdById: req.user.id
    }
  });

  res.status(201).json(report);
}

export async function listMyReports(req: AuthenticatedRequest, res: Response): Promise<void> {
  if (!req.user) {
    res.status(401).json({ message: "Authentication required" });
    return;
  }

  const reports = await prisma.emergencyReport.findMany({
    where: { createdById: req.user.id },
    orderBy: { createdAt: "desc" }
  });

  res.json(reports);
}

export async function listPendingReports(req: AuthenticatedRequest, res: Response): Promise<void> {
  if (!req.user) {
    res.status(401).json({ message: "Authentication required" });
    return;
  }

  const reports = await prisma.emergencyReport.findMany({
    where: { status: "PENDING" },
    include: { createdBy: { select: { id: true, name: true, email: true } } },
    orderBy: { createdAt: "asc" }
  });

  res.json(reports);
}

export async function reviewReport(req: AuthenticatedRequest, res: Response): Promise<void> {
  if (!req.user) {
    res.status(401).json({ message: "Authentication required" });
    return;
  }

  const reportId = Array.isArray(req.params.id) ? req.params.id[0] : req.params.id;
  const { status, reviewNote } = req.body;
  const allowed = ["VERIFIED", "REJECTED", "ACTIVE", "RESOLVED"];

  if (!allowed.includes(status)) {
    res.status(400).json({ message: "Invalid report status" });
    return;
  }

  const report = await prisma.emergencyReport.update({
    where: { id: reportId },
    data: {
      status,
      reviewedById: req.user.id,
      reviewedAt: new Date(),
      reviewNote: typeof reviewNote === "string" ? reviewNote.trim() || null : null
    }
  });

  res.json(report);
}
