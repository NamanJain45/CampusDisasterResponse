import app from "./app";
import { env } from "./config/env";

const server = app.listen(env.port, env.host, () => {
  console.log(
    `Campus Disaster backend running at http://${env.host}:${env.port}`
  );
});

const shutdown = (signal: string) => {
  console.log(`${signal} received. Shutting down backend...`);

  server.close(() => {
    console.log("Campus Disaster backend stopped.");
    process.exit(0);
  });
};

process.on("SIGTERM", () => shutdown("SIGTERM"));
process.on("SIGINT", () => shutdown("SIGINT"));
