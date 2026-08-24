import { Response } from "express";
import { prisma } from "../config/db";
import { AuthenticatedRequest } from "../middlewares/auth.middleware";

export async function updateStatus(req: AuthenticatedRequest, res: Response): Promise<void> {
  if (!req.user) {
    res.status(401).json({ message: "Authentication required" });
    return;
  }

  const { status, message, clientId } = req.body;

  const record = await prisma.userStatus.create({
    data: {
      userId: req.user.id,
      status,
      message,
      clientId
    }
  });

  res.status(201).json(record);
}

export async function listLatestStatuses(req: AuthenticatedRequest, res: Response): Promise<void> {
  if (!req.user) {
    res.status(401).json({ message: "Authentication required" });
    return;
  }

  const users = await prisma.user.findMany({
    where: { role: "STUDENT" },
    select: {
      id: true,
      name: true,
      email: true,
      statuses: {
        orderBy: { createdAt: "desc" },
        take: 1
      }
    },
    orderBy: { name: "asc" }
  });

  res.json(users.map((user) => ({
    id: user.id,
    name: user.name,
    email: user.email,
    status: user.statuses[0]?.status ?? "UNKNOWN",
    message: user.statuses[0]?.message ?? null,
    updatedAt: user.statuses[0]?.createdAt ?? null
  })));
}
