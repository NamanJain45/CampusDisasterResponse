import { Response } from "express";
import { prisma } from "../config/db";
import { AuthenticatedRequest } from "../middlewares/auth.middleware";
import { notifyUser, notifyUsers } from "../services/notification.service";

export async function createReport(req: AuthenticatedRequest, res: Response): Promise<void> {
  if (!req.user) { res.status(401).json({ message: "Authentication required" }); return; }
  const { type, locationText, description, photoUri, latitude, longitude } = req.body;
  if (typeof type !== "string" || !type.trim() || typeof locationText !== "string" || !locationText.trim()) {
    res.status(400).json({ message: "type and locationText are required" }); return;
  }
  const report = await prisma.emergencyReport.create({
    data: {
      type: type.trim(), locationText: locationText.trim(),
      description: typeof description === "string" ? description.trim() || null : null,
      photoUri: typeof photoUri === "string" ? photoUri.trim() || null : null,
      latitude: typeof latitude === "number" ? latitude : null,
      longitude: typeof longitude === "number" ? longitude : null,
      createdById: req.user.id
    }
  });

  await prisma.auditLog.create({
    data: { actorId: req.user.id, action: "REPORT_CREATED", entityType: "EmergencyReport", entityId: report.id, details: report.type }
  });

  await notifyUsers({
    type: "PENDING_REPORT",
    title: "New incident report",
    message: `${report.type} reported at ${report.locationText}`,
    relatedType: "EmergencyReport",
    relatedId: report.id,
    roles: ["STAFF", "ADMIN"]
  });

  res.status(201).json(report);
}

export async function listMyReports(req: AuthenticatedRequest, res: Response): Promise<void> {
  if (!req.user) { res.status(401).json({ message: "Authentication required" }); return; }
  const reports = await prisma.emergencyReport.findMany({ where: { createdById: req.user.id }, orderBy: { createdAt: "desc" } });
  res.json(reports);
}

export async function listPendingReports(req: AuthenticatedRequest, res: Response): Promise<void> {
  if (!req.user) { res.status(401).json({ message: "Authentication required" }); return; }
  const reports = await prisma.emergencyReport.findMany({
    where: { status: "PENDING" },
    include: { createdBy: { select: { id: true, name: true, email: true } } },
    orderBy: { createdAt: "asc" }
  });
  res.json(reports);
}

export async function reviewReport(req: AuthenticatedRequest, res: Response): Promise<void> {
  if (!req.user) { res.status(401).json({ message: "Authentication required" }); return; }
  const reportId = Array.isArray(req.params.id) ? req.params.id[0] : req.params.id;
  const { status, reviewNote } = req.body;
  const allowed = ["VERIFIED", "REJECTED", "ACTIVE", "RESOLVED"];
  if (!allowed.includes(status)) { res.status(400).json({ message: "Invalid report status" }); return; }

  const existing = await prisma.emergencyReport.findUnique({ where: { id: reportId }, include: { createdBy: { select: { id: true, name: true } } } });
  if (!existing) { res.status(404).json({ message: "Incident report not found" }); return; }
  if (existing.status === "REJECTED" || existing.status === "RESOLVED") {
    res.status(409).json({ message: `Report is already ${existing.status.toLowerCase()}` }); return;
  }

  const report = await prisma.emergencyReport.update({
    where: { id: reportId },
    data: {
      status,
      reviewedById: req.user.id,
      reviewedAt: new Date(),
      reviewNote: typeof reviewNote === "string" ? reviewNote.trim() || null : null
    },
    include: { createdBy: { select: { name: true } } }
  });

  await prisma.auditLog.create({
    data: {
      actorId: req.user.id,
      action: `REPORT_${status}`,
      entityType: "EmergencyReport",
      entityId: report.id,
      details: report.reviewNote
    }
  });

  const reportTitle = `${report.type} incident update`;
  const reportMessage = `${report.locationText}${report.reviewNote ? ` — ${report.reviewNote}` : ""}`;
  await notifyUser(existing.createdBy.id, {
    type: `REPORT_${status}`,
    title: reportTitle,
    message: reportMessage,
    relatedType: "EmergencyReport",
    relatedId: report.id
  });

  if (status === "VERIFIED" || status === "ACTIVE") {
    const emergency = await prisma.emergency.create({
      data: {
        title: `${report.type} reported on campus`,
        message: `${report.locationText}${report.description ? ` — ${report.description}` : ""}`,
        severity: "HIGH",
        activatedBy: req.user.id
      }
    });

    await prisma.auditLog.create({
      data: { actorId: req.user.id, action: "INCIDENT_ACTIVATED", entityType: "Emergency", entityId: emergency.id, details: `Source report ${report.id}` }
    });

    await notifyUsers({
      type: "VERIFIED_INCIDENT",
      title: emergency.title,
      message: emergency.message,
      relatedType: "EmergencyReport",
      relatedId: report.id
    });
  }

  if (status === "RESOLVED") {
    await notifyUsers({
      type: "INCIDENT_RESOLVED",
      title: `${report.type} resolved`,
      message: `The reported incident at ${report.locationText} has been resolved.`,
      relatedType: "EmergencyReport",
      relatedId: report.id
    });
  }

  res.json(report);
}
