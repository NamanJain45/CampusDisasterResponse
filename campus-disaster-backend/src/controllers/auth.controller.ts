import {
  Request,
  Response
} from "express";

import bcrypt from "bcrypt";
import jwt from "jsonwebtoken";

import { prisma } from "../config/db";
import { env } from "../config/env";

export async function register(
  req: Request,
  res: Response
): Promise<void> {

  const {
    name,
    email,
    password,
    role
  } = req.body;

  if (
    !name ||
    !email ||
    !password
  ) {

    res.status(400).json({
      message:
        "name, email and password are required"
    });

    return;
  }

  const existing =
    await prisma.user.findUnique({
      where: { email }
    });

  if (existing) {

    res.status(409).json({
      message: "Email already registered"
    });

    return;
  }

  const passwordHash =
    await bcrypt.hash(
      password,
      12
    );

  const user =
    await prisma.user.create({
      data: {
        name,
        email,
        passwordHash,
        role:
          role === "ADMIN" ||
          role === "STAFF"
            ? role
            : "STUDENT"
      }
    });

  res.status(201).json({
    id: user.id,
    name: user.name,
    email: user.email,
    role: user.role
  });
}

export async function login(
  req: Request,
  res: Response
): Promise<void> {

  const {
    email,
    password
  } = req.body;

  const user =
    await prisma.user.findUnique({
      where: { email }
    });

  if (!user) {

    res.status(401).json({
      message: "Invalid credentials"
    });

    return;
  }

  const valid =
    await bcrypt.compare(
      password,
      user.passwordHash
    );

  if (!valid) {

    res.status(401).json({
      message: "Invalid credentials"
    });

    return;
  }

  const token =
    jwt.sign(
      {
        userId: user.id,
        role: user.role
      },
      env.jwtSecret,
      {
        expiresIn: "7d"
      }
    );

  res.json({
    token,
    user: {
      id: user.id,
      name: user.name,
      email: user.email,
      role: user.role
    }
  });
}
