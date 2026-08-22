import {
  Request,
  Response
} from "express";

import { prisma } from "../config/db";
import {
  AuthenticatedRequest
} from "../middlewares/auth.middleware";

export async function activateEmergency(
  req: AuthenticatedRequest,
  res: Response
): Promise<void> {

  const {
    title,
    message,
    severity = "CRITICAL"
  } = req.body;

  const emergency =
    await prisma.emergency.create({
      data: {
        title,
        message,
        severity,
        activatedBy: req.user?.id
      }
    });

  res.status(201).json(
    emergency
  );
}

export async function getActiveAlerts(
  _req: Request,
  res: Response
): Promise<void> {

  const alerts =
    await prisma.emergency.findMany({
      where: {
        active: true
      },
      orderBy: {
        createdAt: "desc"
      }
    });

  res.json(alerts);
}
