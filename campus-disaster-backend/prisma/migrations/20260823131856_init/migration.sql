-- CreateEnum
CREATE TYPE "UserRole" AS ENUM ('STUDENT', 'STAFF', 'ADMIN');

-- CreateEnum
CREATE TYPE "EmergencySeverity" AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL');

-- CreateEnum
CREATE TYPE "SafetyStatus" AS ENUM ('SAFE', 'TRAPPED', 'INJURED', 'NEED_ASSISTANCE');

-- CreateTable
CREATE TABLE "User" (
    "id" TEXT NOT NULL,
    "name" TEXT NOT NULL,
    "email" TEXT NOT NULL,
    "passwordHash" TEXT NOT NULL,
    "role" "UserRole" NOT NULL DEFAULT 'STUDENT',
    "syncSequence" SERIAL NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "User_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Emergency" (
    "id" TEXT NOT NULL,
    "title" TEXT NOT NULL,
    "message" TEXT NOT NULL,
    "severity" "EmergencySeverity" NOT NULL,
    "active" BOOLEAN NOT NULL DEFAULT true,
    "activatedBy" TEXT,
    "syncSequence" SERIAL NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "Emergency_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "SOS" (
    "id" TEXT NOT NULL,
    "clientId" TEXT,
    "userId" TEXT NOT NULL,
    "latitude" DOUBLE PRECISION,
    "longitude" DOUBLE PRECISION,
    "message" TEXT,
    "active" BOOLEAN NOT NULL DEFAULT true,
    "syncSequence" SERIAL NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "SOS_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "UserStatus" (
    "id" TEXT NOT NULL,
    "clientId" TEXT,
    "userId" TEXT NOT NULL,
    "status" "SafetyStatus" NOT NULL,
    "message" TEXT,
    "syncSequence" SERIAL NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "UserStatus_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Location" (
    "id" TEXT NOT NULL,
    "clientId" TEXT,
    "userId" TEXT NOT NULL,
    "latitude" DOUBLE PRECISION NOT NULL,
    "longitude" DOUBLE PRECISION NOT NULL,
    "recordedAt" TIMESTAMP(3) NOT NULL,
    "syncSequence" SERIAL NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "Location_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "SyncLog" (
    "id" TEXT NOT NULL,
    "userId" TEXT,
    "clientActionId" TEXT,
    "actionType" TEXT NOT NULL,
    "success" BOOLEAN NOT NULL,
    "errorMessage" TEXT,
    "syncSequence" SERIAL NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "SyncLog_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "User_email_key" ON "User"("email");

-- CreateIndex
CREATE INDEX "User_updatedAt_idx" ON "User"("updatedAt");

-- CreateIndex
CREATE INDEX "User_syncSequence_idx" ON "User"("syncSequence");

-- CreateIndex
CREATE INDEX "Emergency_updatedAt_idx" ON "Emergency"("updatedAt");

-- CreateIndex
CREATE INDEX "Emergency_syncSequence_idx" ON "Emergency"("syncSequence");

-- CreateIndex
CREATE UNIQUE INDEX "SOS_clientId_key" ON "SOS"("clientId");

-- CreateIndex
CREATE INDEX "SOS_userId_idx" ON "SOS"("userId");

-- CreateIndex
CREATE INDEX "SOS_updatedAt_idx" ON "SOS"("updatedAt");

-- CreateIndex
CREATE INDEX "SOS_syncSequence_idx" ON "SOS"("syncSequence");

-- CreateIndex
CREATE UNIQUE INDEX "UserStatus_clientId_key" ON "UserStatus"("clientId");

-- CreateIndex
CREATE INDEX "UserStatus_userId_idx" ON "UserStatus"("userId");

-- CreateIndex
CREATE INDEX "UserStatus_updatedAt_idx" ON "UserStatus"("updatedAt");

-- CreateIndex
CREATE INDEX "UserStatus_syncSequence_idx" ON "UserStatus"("syncSequence");

-- CreateIndex
CREATE UNIQUE INDEX "Location_clientId_key" ON "Location"("clientId");

-- CreateIndex
CREATE INDEX "Location_userId_idx" ON "Location"("userId");

-- CreateIndex
CREATE INDEX "Location_updatedAt_idx" ON "Location"("updatedAt");

-- CreateIndex
CREATE INDEX "Location_syncSequence_idx" ON "Location"("syncSequence");

-- CreateIndex
CREATE UNIQUE INDEX "SyncLog_clientActionId_key" ON "SyncLog"("clientActionId");

-- CreateIndex
CREATE INDEX "SyncLog_updatedAt_idx" ON "SyncLog"("updatedAt");

-- CreateIndex
CREATE INDEX "SyncLog_syncSequence_idx" ON "SyncLog"("syncSequence");

-- AddForeignKey
ALTER TABLE "SOS" ADD CONSTRAINT "SOS_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "UserStatus" ADD CONSTRAINT "UserStatus_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Location" ADD CONSTRAINT "Location_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE;
