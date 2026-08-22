package com.vjti.campusdisasterresponse.data.model

object MockCampusData {

    fun getSampleCampusData(): CampusMapData {
        return CampusMapData(
            campusName = "VJTI Main Blueprint",

            buildings = listOf(
                BuildingData(
                    id = "b1",
                    name = "Academic Building",
                    left = 0.1f,
                    top = 0.15f,
                    right = 0.45f,
                    bottom = 0.4f
                ),
                BuildingData(
                    id = "b2",
                    name = "Library & Admin",
                    left = 0.55f,
                    top = 0.15f,
                    right = 0.9f,
                    bottom = 0.35f
                ),
                BuildingData(
                    id = "b3",
                    name = "Hostel Block",
                    left = 0.1f,
                    top = 0.6f,
                    right = 0.4f,
                    bottom = 0.85f
                )
            ),

            routes = listOf(
                EvacuationRouteData(
                    id = "r1",
                    pathPoints = listOf(
                        PointF(0.27f, 0.4f),
                        PointF(0.27f, 0.52f),
                        PointF(0.5f, 0.52f),
                        PointF(0.5f, 0.88f)
                    ),
                    isPrimary = true
                )
            ),

            markers = listOf(
                MapMarkerData(
                    id = "m1",
                    label = "Main Gate Exit",
                    position = PointF(0.5f, 0.92f),
                    type = MarkerType.EXIT
                ),
                MapMarkerData(
                    id = "m2",
                    label = "Central Ground",
                    position = PointF(0.5f, 0.88f),
                    type = MarkerType.ASSEMBLY_POINT
                ),
                MapMarkerData(
                    id = "m3",
                    label = "Medical Center",
                    position = PointF(0.72f, 0.35f),
                    type = MarkerType.FIRST_AID
                ),
                MapMarkerData(
                    id = "m4",
                    label = "Open Safe Zone",
                    position = PointF(0.75f, 0.7f),
                    type = MarkerType.SAFE_AREA
                )
            )
        )
    }
}
