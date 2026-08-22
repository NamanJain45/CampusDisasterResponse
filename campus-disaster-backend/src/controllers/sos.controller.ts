import {
  Response
} from "express";

import { prisma } from "../config/db";

import {
  AuthenticatedRequest
} from "../middlewares/auth.middleware";

export async function createSos(
  req: AuthenticatedRequest,
  res: Response
): Promise<void> {

  if (!req.user) {

    res.status(401).json({
      message: "Authentication required"
    });

    return;
  }

  const {
    latitude,
    longitude,
    message,
    clientId
  } = req.body;

  const sos =
    await prisma.sOS.create({
      data: {
        userId: req.user.id,
        latitude,
        longitude,
        message,
        clientId
      }
    });

  res.status(201).json(sos);
}

export async function getActiveSos(
  _req: AuthenticatedRequest,
  res: Response
): Promise<void> {

  const events =
    await prisma.sOS.findMany({
      where: {
        active: true
      },
      orderBy: {
        createdAt: "desc"
      }
    });

  res.json(events);
}
