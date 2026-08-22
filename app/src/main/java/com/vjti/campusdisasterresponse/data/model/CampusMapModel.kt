package com.vjti.campusdisasterresponse.data.model

data class PointF(
    val x: Float,
    val y: Float
)

data class BuildingData(
    val id: String,
    val name: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

data class EvacuationRouteData(
    val id: String,
    val pathPoints: List<PointF>,
    val isPrimary: Boolean = true
)

enum class MarkerType {
    EXIT,
    ASSEMBLY_POINT,
    FIRST_AID,
    SAFE_AREA
}

data class MapMarkerData(
    val id: String,
    val label: String,
    val position: PointF,
    val type: MarkerType
)

data class CampusMapData(
    val campusName: String,
    val buildings: List<BuildingData>,
    val routes: List<EvacuationRouteData>,
    val markers: List<MapMarkerData>
)
