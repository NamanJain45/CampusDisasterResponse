import {
  NextFunction,
  Request,
  Response
} from "express";

import jwt from "jsonwebtoken";

import { env } from "../config/env";

export type AuthRole =
  | "STUDENT"
  | "STAFF"
  | "ADMIN";

export interface AuthenticatedRequest
  extends Request {
  user?: {
    id: string;
    role: AuthRole;
  };
}

interface JwtPayload {
  userId: string;
  role: AuthRole;
}

export function requireAuth(
  req: AuthenticatedRequest,
  res: Response,
  next: NextFunction
): void {

  const header =
    req.headers.authorization;

  if (
    !header ||
    !header.startsWith("Bearer ")
  ) {
    res.status(401).json({
      message: "Authentication required"
    });

    return;
  }

  const token =
    header.substring(7);

  try {

    const decoded =
      jwt.verify(
        token,
        env.jwtSecret
      ) as JwtPayload;

    req.user = {
      id: decoded.userId,
      role: decoded.role
    };

    next();

  } catch {

    res.status(401).json({
      message: "Invalid or expired token"
    });
  }
}

export function requireRole(
  ...roles: AuthRole[]
) {

  return (
    req: AuthenticatedRequest,
    res: Response,
    next: NextFunction
  ): void => {

    if (!req.user) {

      res.status(401).json({
        message: "Authentication required"
      });

      return;
    }

    if (!roles.includes(req.user.role)) {

      res.status(403).json({
        message: "Insufficient permissions"
      });

      return;
    }

    next();
  };
}
