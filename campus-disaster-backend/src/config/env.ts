import dotenv from "dotenv";

dotenv.config();

export const env = {
  host: process.env.HOST ?? "0.0.0.0",

  port: Number(process.env.PORT ?? 3000),

  databaseUrl:
    process.env.DATABASE_URL ?? "",

  jwtSecret:
    process.env.JWT_SECRET ??
    "development-only-secret",

  jwtExpiresIn:
    process.env.JWT_EXPIRES_IN ??
    "7d"
};
