import bcrypt from "bcrypt";
import { prisma } from "../src/config/db";

const accounts = [
  {
    name: "Test Student",
    email: "student@campus.test",
    password: "student123",
    role: "STUDENT" as const
  },
  {
    name: "Test Staff",
    email: "staff@campus.test",
    password: "staff123",
    role: "STAFF" as const
  },
  {
    name: "Test Admin",
    email: "admin@campus.test",
    password: "admin123",
    role: "ADMIN" as const
  }
];

async function main(): Promise<void> {
  for (const account of accounts) {
    const passwordHash = await bcrypt.hash(account.password, 12);
    await prisma.user.upsert({
      where: { email: account.email },
      update: {
        name: account.name,
        passwordHash,
        role: account.role
      },
      create: {
        name: account.name,
        email: account.email,
        passwordHash,
        role: account.role
      }
    });
  }

  console.log("MVP accounts seeded:");
  console.log("Student: student@campus.test / student123");
  console.log("Staff:   staff@campus.test / staff123");
  console.log("Admin:   admin@campus.test / admin123");
}

main()
  .catch((error) => {
    console.error(error);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
