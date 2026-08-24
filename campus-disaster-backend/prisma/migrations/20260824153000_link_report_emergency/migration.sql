ALTER TABLE "Emergency" ADD COLUMN "sourceReportId" TEXT;
CREATE INDEX "Emergency_sourceReportId_idx" ON "Emergency"("sourceReportId");
