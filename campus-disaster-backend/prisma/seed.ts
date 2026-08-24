import bcrypt from "bcrypt";
import { prisma } from "../src/config/db";

const accounts = [
  { name: "Student One", email: "student1@campus.test", password: "student123", role: "STUDENT" as const },
  { name: "Student Two", email: "student2@campus.test", password: "student123", role: "STUDENT" as const },
  { name: "Student Three", email: "student3@campus.test", password: "student123", role: "STUDENT" as const },
  { name: "Test Staff", email: "staff@campus.test", password: "staff123", role: "STAFF" as const },
  { name: "Test Admin", email: "admin@campus.test", password: "admin123", role: "ADMIN" as const }
];

async function main(): Promise<void> {
  for (const account of accounts) {
    const passwordHash = await bcrypt.hash(account.password, 12);
    await prisma.user.upsert({
      where: { email: account.email },
      update: { name: account.name, passwordHash, role: account.role },
      create: { name: account.name, email: account.email, passwordHash, role: account.role }
    });
  }

  console.log("MVP test accounts seeded:");
  for (const account of accounts) console.log(`${account.role}: ${account.email} / ${account.password}`);
}

main()
  .catch((error) => { console.error(error); process.exit(1); })
  .finally(async () => { await prisma.$disconnect(); });
