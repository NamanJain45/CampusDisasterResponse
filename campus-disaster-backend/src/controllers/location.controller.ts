import {
  Response
} from "express";

import { prisma } from "../config/db";

import {
  AuthenticatedRequest
} from "../middlewares/auth.middleware";

export async function updateLocations(
  req: AuthenticatedRequest,
  res: Response
): Promise<void> {

  if (!req.user) {

    res.status(401).json({
      message: "Authentication required"
    });

    return;
  }

  const locations =
    Array.isArray(req.body.locations)
      ? req.body.locations
      : [];

  const created = [];

  for (const location of locations) {

    const record =
      await prisma.location.create({
        data: {
          userId: req.user.id,

          clientId:
            location.clientId,

          latitude:
            location.latitude,

          longitude:
            location.longitude,

          recordedAt:
            new Date(
              location.timestamp ??
              Date.now()
            )
        }
      });

    created.push(record);
  }

  res.status(201).json({
    locations: created
  });
}
