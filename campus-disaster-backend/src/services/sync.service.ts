import type {
  SafetyStatus
} from "../generated/prisma/enums";

import { prisma } from "../config/db";

export interface PendingAction {
  clientActionId: string;

  type:
    | "STATUS_UPDATE"
    | "SOS"
    | "LOCATION_UPDATE";

  payload: Record<string, unknown>;
}

export async function processSync(
  userId: string,
  pendingActions: PendingAction[],
  lastSyncedAt?: string
) {

  const processedActions: string[] = [];

  for (
    const action of pendingActions
  ) {

    const existing =
      await prisma.syncLog.findUnique({
        where: {
          clientActionId:
            action.clientActionId
        }
      });

    if (existing?.success) {
      processedActions.push(
        action.clientActionId
      );

      continue;
    }

    try {

      if (
        action.type ===
        "STATUS_UPDATE"
      ) {

        await prisma.userStatus.create({
          data: {
            userId,

            clientId:
              String(
                action.payload.clientId ??
                action.clientActionId
              ),

            status:
              String(
                action.payload.status
              ) as SafetyStatus,

            message:
              action.payload.message
                ? String(
                    action.payload.message
                  )
                : null
          }
        });
      }

      if (
        action.type === "SOS"
      ) {

        await prisma.sOS.create({
          data: {
            userId,

            clientId:
              String(
                action.payload.clientId ??
                action.clientActionId
              ),

            latitude:
              typeof action.payload.latitude ===
              "number"
                ? action.payload.latitude
                : null,

            longitude:
              typeof action.payload.longitude ===
              "number"
                ? action.payload.longitude
                : null,

            message:
              action.payload.message
                ? String(
                    action.payload.message
                  )
                : null
          }
        });
      }

      if (
        action.type ===
        "LOCATION_UPDATE"
      ) {

        await prisma.location.create({
          data: {
            userId,

            clientId:
              String(
                action.payload.clientId ??
                action.clientActionId
              ),

            latitude:
              Number(
                action.payload.latitude
              ),

            longitude:
              Number(
                action.payload.longitude
              ),

            recordedAt:
              new Date(
                Number(
                  action.payload.timestamp ??
                  Date.now()
                )
              )
          }
        });
      }

      await prisma.syncLog.upsert({
        where: {
          clientActionId:
            action.clientActionId
        },
        update: {
          userId,
          actionType:
            action.type,
          success: true,
          errorMessage: null
        },
        create: {
          userId,
          clientActionId:
            action.clientActionId,
          actionType:
            action.type,
          success: true
        }
      });

      processedActions.push(
        action.clientActionId
      );

    } catch (error) {

      await prisma.syncLog.upsert({
        where: {
          clientActionId:
            action.clientActionId
        },
        update: {
          userId,

          actionType:
            action.type,

          success: false,

          errorMessage:
            error instanceof Error
              ? error.message
              : "Unknown sync error"
        },
        create: {
          userId,

          clientActionId:
            action.clientActionId,

          actionType:
            action.type,

          success: false,

          errorMessage:
            error instanceof Error
              ? error.message
              : "Unknown sync error"
        }
      });
    }
  }

  const since =
    lastSyncedAt
      ? new Date(lastSyncedAt)
      : new Date(0);

  const [
    emergencies,
    sosEvents,
    statuses
  ] =
    await Promise.all([

      prisma.emergency.findMany({
        where: {
          updatedAt: {
            gt: since
          }
        }
      }),

      prisma.sOS.findMany({
        where: {
          updatedAt: {
            gt: since
          }
        }
      }),

      prisma.userStatus.findMany({
        where: {
          userId,
          updatedAt: {
            gt: since
          }
        }
      })
    ]);

  return {
    serverTime:
      new Date().toISOString(),

    processedActions,

    delta: {
      emergencies,
      sosEvents,
      statuses
    }
  };
}
