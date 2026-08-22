import {
  Response
} from "express";

import {
  AuthenticatedRequest
} from "../middlewares/auth.middleware";

import {
  PendingAction,
  processSync
} from "../services/sync.service";

export async function sync(
  req: AuthenticatedRequest,
  res: Response
): Promise<void> {

  if (!req.user) {

    res.status(401).json({
      message: "Authentication required"
    });

    return;
  }

  const pendingActions =
    Array.isArray(
      req.body.pendingActions
    )
      ? req.body.pendingActions as PendingAction[]
      : [];

  const lastSyncedAt =
    typeof req.body.lastSyncedAt ===
    "string"
      ? req.body.lastSyncedAt
      : undefined;

  const result =
    await processSync(
      req.user.id,
      pendingActions,
      lastSyncedAt
    );

  res.json(result);
}
