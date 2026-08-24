import { prisma } from "../config/db";

export async function notifyUsers(input: {
  type: string;
  title: string;
  message: string;
  relatedType?: string;
  relatedId?: string;
  roles?: Array<"STUDENT" | "STAFF" | "ADMIN">;
  excludeUserId?: string;
}): Promise<void> {
  const users = await prisma.user.findMany({
    where: {
      ...(input.roles ? { role: { in: input.roles } } : {}),
      ...(input.excludeUserId ? { id: { not: input.excludeUserId } } : {})
    },
    select: { id: true }
  });

  if (users.length === 0) return;

  await prisma.notification.createMany({
    data: users.map((user) => ({
      userId: user.id,
      type: input.type,
      title: input.title,
      message: input.message,
      relatedType: input.relatedType,
      relatedId: input.relatedId
    }))
  });
}

export async function notifyUser(userId: string, input: Omit<Parameters<typeof notifyUsers>[0], "roles" | "excludeUserId">): Promise<void> {
  await prisma.notification.create({
    data: {
      userId,
      type: input.type,
      title: input.title,
      message: input.message,
      relatedType: input.relatedType,
      relatedId: input.relatedId
    }
  });
}
