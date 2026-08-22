const express = require("express");
const mongoose = require("mongoose");

const app = express();

app.use(express.json());


// -----------------------------------------------------
// Emergency Event Schema
// -----------------------------------------------------

const emergencyEventSchema = new mongoose.Schema({
    eventId: {
        type: String,
        required: true,
        unique: true,
        index: true
    },

    userId: {
        type: String,
        required: true
    },

    status: {
        type: String,
        required: true,
        enum: [
            "SAFE",
            "TRAPPED",
            "NEED_FIRST_AID",
            "SOS"
        ]
    },

    location: {
        latitude: {
            type: Number
        },

        longitude: {
            type: Number
        }
    },

    clientTimestamp: {
        type: Date
    },

    createdAt: {
        type: Date,
        default: Date.now
    }
});


const EmergencyEvent =
    mongoose.model(
        "EmergencyEvent",
        emergencyEventSchema
    );


// -----------------------------------------------------
// Authentication Middleware
// -----------------------------------------------------

const authenticateToken = (
    req,
    res,
    next
) => {

    const authHeader =
        req.headers["authorization"];

    const token =
        authHeader &&
        authHeader.split(" ")[1];

    if (!token) {

        return res
            .status(401)
            .json({
                error:
                    "Unauthorized: Missing authentication token"
            });
    }

    /*
     * Prototype behavior:
     *
     * User identity is currently supplied by an
     * upstream auth gateway / X-User-ID header.
     *
     * Work 24 will reconcile this with the real JWT
     * implementation from Work 17.
     */

    req.user = {
        userId:
            req.headers["x-user-id"] ||
            "user_unknown"
    };

    next();
};


// -----------------------------------------------------
// Request Validation Middleware
// -----------------------------------------------------

const validateEmergencyPayload = (
    req,
    res,
    next
) => {

    const {
        eventId,
        status
    } = req.body;

    const validStatuses = [
        "SAFE",
        "TRAPPED",
        "NEED_FIRST_AID",
        "SOS"
    ];

    if (
        !eventId ||
        typeof eventId !== "string"
    ) {

        return res
            .status(400)
            .json({
                error:
                    "Invalid or missing unique eventId"
            });
    }

    if (
        !status ||
        !validStatuses.includes(status)
    ) {

        return res
            .status(400)
            .json({
                error:
                    `Invalid status. Allowed values: ${validStatuses.join(", ")}`
            });
    }

    next();
};


// -----------------------------------------------------
// Health Endpoint
// -----------------------------------------------------

app.get(
    "/health",
    (_req, res) => {

        res.json({
            status: "ok",
            service:
                "campus-disaster-mongo-service"
        });
    }
);


// -----------------------------------------------------
// POST /api/emergency/status
// -----------------------------------------------------

app.post(
    "/api/emergency/status",

    authenticateToken,

    validateEmergencyPayload,

    async (req, res) => {

        try {

            const {
                eventId,
                status,
                location,
                clientTimestamp
            } = req.body;

            const userId =
                req.user.userId;


            // -----------------------------------------
            // Idempotency / duplicate protection
            // -----------------------------------------

            const existingEvent =
                await EmergencyEvent.findOne({
                    eventId
                });

            if (existingEvent) {

                return res
                    .status(200)
                    .json({
                        message:
                            "Duplicate event received; returning existing record",

                        duplicate: true,

                        data:
                            existingEvent
                    });
            }


            // -----------------------------------------
            // Persist new event
            // -----------------------------------------

            const newEvent =
                new EmergencyEvent({

                    eventId,

                    userId,

                    status,

                    location,

                    clientTimestamp:
                        clientTimestamp
                            ? new Date(clientTimestamp)
                            : new Date(),

                    createdAt:
                        new Date()
                });


            await newEvent.save();


            return res
                .status(201)
                .json({
                    message:
                        "Emergency status update stored successfully",

                    duplicate: false,

                    data:
                        newEvent
                });


        } catch (error) {


            // -----------------------------------------
            // Mongo duplicate-index race condition
            // -----------------------------------------

            if (
                error &&
                error.code === 11000
            ) {

                const existingEvent =
                    await EmergencyEvent.findOne({
                        eventId:
                            req.body.eventId
                    });


                return res
                    .status(200)
                    .json({
                        message:
                            "Duplicate event received; returning existing record",

                        duplicate: true,

                        data:
                            existingEvent
                    });
            }


            return res
                .status(500)
                .json({
                    error:
                        "Internal server error",

                    details:
                        error instanceof Error
                            ? error.message
                            : String(error)
                });
        }
    }
);


// -----------------------------------------------------
// Server Initialization
// -----------------------------------------------------

const PORT =
    process.env.PORT ||
    3000;


const MONGO_URI =
    process.env.MONGO_URI ||
    "mongodb://127.0.0.1:27017/campus_disaster_db";


mongoose
    .connect(MONGO_URI)

    .then(() => {

        app.listen(
            PORT,
            () => {

                console.log(
                    `Emergency status service running on port ${PORT}`
                );
            }
        );
    })

    .catch((error) => {

        console.error(
            "Database connection failure:",
            error
        );
    });
