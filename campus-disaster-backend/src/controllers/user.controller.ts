import { Request, Response } from "express";
import bcrypt from "bcrypt";

import { prisma } from "../config/db";
import { AuthenticatedRequest } from "../middlewares/auth.middleware";

export async function listUsers(
  _req: AuthenticatedRequest,
  res: Response
): Promise<void> {
  const users = await prisma.user.findMany({
    select: {
      id: true,
      name: true,
      email: true,
      role: true,
      createdAt: true
    },
    orderBy: { createdAt: "asc" }
  });

  res.json({ users });
}

export async function createUser(
  req: AuthenticatedRequest,
  res: Response
): Promise<void> {
  const { name, email, password, role } = req.body;

  if (!name || !email || !password) {
    res.status(400).json({ message: "name, email and password are required" });
    return;
  }

  const requestedRole = role === "STAFF" || role === "ADMIN" ? role : "STUDENT";

  if (req.user?.role === "STAFF" && requestedRole !== "STUDENT") {
    res.status(403).json({ message: "Staff can only create student accounts" });
    return;
  }

  const existing = await prisma.user.findUnique({ where: { email } });
  if (existing) {
    res.status(409).json({ message: "Email already registered" });
    return;
  }

  const passwordHash = await bcrypt.hash(password, 12);
  const user = await prisma.user.create({
    data: { name, email, passwordHash, role: requestedRole }
  });

  res.status(201).json({
    id: user.id,
    name: user.name,
    email: user.email,
    role: user.role
  });
}

export async function deleteUser(
  req: AuthenticatedRequest,
  res: Response
): Promise<void> {
  const userId = req.params.id;

  const target = await prisma.user.findUnique({ where: { id: userId } });
  if (!target) {
    res.status(404).json({ message: "User not found" });
    return;
  }

  if (target.id === req.user?.id) {
    res.status(400).json({ message: "You cannot remove your own account" });
    return;
  }

  if (req.user?.role === "STAFF" && target.role !== "STUDENT") {
    res.status(403).json({ message: "Staff can only remove student accounts" });
    return;
  }

  await prisma.user.delete({ where: { id: userId } });
  res.json({ message: "User removed" });
}
