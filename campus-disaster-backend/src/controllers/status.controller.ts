import {
  Response
} from "express";

import { prisma } from "../config/db";

import {
  AuthenticatedRequest
} from "../middlewares/auth.middleware";

export async function updateStatus(
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
    status,
    message,
    clientId
  } = req.body;

  const record =
    await prisma.userStatus.create({
      data: {
        userId: req.user.id,
        status,
        message,
        clientId
      }
    });

  res.status(201).json(record);
}
