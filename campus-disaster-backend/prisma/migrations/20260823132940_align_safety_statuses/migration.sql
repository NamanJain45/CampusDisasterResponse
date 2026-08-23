/*
  Warnings:

  - The values [INJURED] on the enum `SafetyStatus` will be removed. If these variants are still used in the database, this will fail.

*/
-- AlterEnum
BEGIN;
CREATE TYPE "SafetyStatus_new" AS ENUM ('UNKNOWN', 'SAFE', 'TRAPPED', 'NEED_FIRST_AID', 'NEED_ASSISTANCE');
ALTER TABLE "UserStatus" ALTER COLUMN "status" TYPE "SafetyStatus_new" USING ("status"::text::"SafetyStatus_new");
ALTER TYPE "SafetyStatus" RENAME TO "SafetyStatus_old";
ALTER TYPE "SafetyStatus_new" RENAME TO "SafetyStatus";
DROP TYPE "public"."SafetyStatus_old";
COMMIT;
