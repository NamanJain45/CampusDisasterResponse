import app from "./app";

import {
  env
} from "./config/env";

app.listen(
  env.port,
  () => {

    console.log(
      `Campus Disaster backend running on port ${env.port}`
    );
  }
);
