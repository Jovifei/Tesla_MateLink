package com.teslamatelink.domain

/**
 * Converts between WGS-84 (GPS native) and GCJ-02 (Chinese national standard)
 * coordinate systems using the well-known eviltransform algorithm.
 *
 * China's regulatory requirements mandate that all maps published within China
 * use the GCJ-02 datum. Coordinates obtained from GPS are natively WGS-84.
 * This converter applies the standard offset so that vehicle positions align
 * correctly with Chinese map tile services (AMap / AutoNavi, Google Roadmap).
 *
 * Algorithm reference: https://github.com/googollee/eviltransform
 * Accuracy: < 0.5 m within China; coordinates outside China pass through unchanged.
 *
 * Example:
 * // Tiananmen, Beijing — WGS-84 (39.913818, 116.397828) → GCJ-02 (39.91522, 116.40407)
 * val (gcjLat, gcjLng) = GCJ02Converter.wgs84ToGcj02(39.913818, 116.397828)
 */
object GCJ02Converter {

    // MARK: - Constants

    /** Semi-major axis of the Clarke 1866 ellipsoid used by the GCJ-02 algorithm (metres). */
    private const val A = 6378245.0

    /** Squared eccentricity of the Clarke 1866 ellipsoid. */
    private const val EE = 0.00669342162296594323

    /** Bounding box of China (approximately, includes mainland + Hong Kong + Macau + Taiwan). */
    private const val CHINA_LAT_MIN = 0.8293
    private const val CHINA_LAT_MAX = 55.8271
    private const val CHINA_LNG_MIN = 72.004
    private const val CHINA_LNG_MAX = 137.8347

    // MARK: - Public API

    /**
     * Checks whether a coordinate falls within Hong Kong or Macau.
     *
     * These SARs use WGS-84 natively (no GCJ-02 offset), so coordinates
     * in these regions should pass through unchanged.
     */
    fun isInSpecialRegion(lat: Double, lng: Double): Boolean {
        // Hong Kong bounding box
        val isHK = lat in 22.15..22.55 && lng in 113.83..114.42
        // Macau bounding box
        val isMacau = lat in 22.10..22.22 && lng in 113.52..113.60
        return isHK || isMacau
    }

    /**
     * Checks whether a coordinate falls within the rough bounding box of China.
     *
     * Only coordinates inside this box are transformed; coordinates outside
     * pass through unchanged.
     */
    fun isInChina(lat: Double, lng: Double): Boolean {
        return lat >= CHINA_LAT_MIN && lat <= CHINA_LAT_MAX
                && lng >= CHINA_LNG_MIN && lng <= CHINA_LNG_MAX
    }

    /**
     * Converts a WGS-84 coordinate to GCJ-02.
     *
     * @param lat Latitude in degrees (WGS-84).
     * @param lng Longitude in degrees (WGS-84).
     * @return A Pair of (lat, lng) in the GCJ-02 datum.
     *         Coordinates outside the China bounding box are returned unchanged.
     */
    fun wgs84ToGcj02(lat: Double, lng: Double): Pair<Double, Double> {
        if (isInSpecialRegion(lat, lng)) return Pair(lat, lng)
        if (!isInChina(lat, lng)) return Pair(lat, lng)

        val dLat = transformLat(x = lng - 105.0, y = lat - 35.0)
        val dLng = transformLng(x = lng - 105.0, y = lat - 35.0)

        val radLat = lat / 180.0 * PI
        var magic = sin(radLat)
        magic = 1.0 - EE * magic * magic
        val sqrtMagic = sqrt(magic)

        val dLatAdj = (dLat * 180.0) / ((A * (1.0 - EE)) / (magic * sqrtMagic) * PI)
        val dLngAdj = (dLng * 180.0) / (A / sqrtMagic * cos(radLat) * PI)

        return Pair(lat + dLatAdj, lng + dLngAdj)
    }

    /**
     * Converts a GCJ-02 coordinate back to WGS-84 using iterative approximation.
     *
     * Since the GCJ-02 transform is not analytically invertible, this method
     * uses a fixed-point iteration that typically converges within 2-3 passes.
     *
     * @param lat Latitude in degrees (GCJ-02).
     * @param lng Longitude in degrees (GCJ-02).
     * @param iterations Number of approximation iterations. Default is 3.
     * @return A Pair of (lat, lng) in the WGS-84 datum.
     *         Coordinates outside the China bounding box are returned unchanged.
     */
    fun gcj02ToWgs84(lat: Double, lng: Double, iterations: Int = 3): Pair<Double, Double> {
        if (isInSpecialRegion(lat, lng)) return Pair(lat, lng)
        if (!isInChina(lat, lng)) return Pair(lat, lng)

        var wgsLat = lat
        var wgsLng = lng
        val count = maxOf(iterations, 1)

        for (i in 0 until count) {
            val (gcjLat, gcjLng) = wgs84ToGcj02(lat = wgsLat, lng = wgsLng)
            wgsLat += lat - gcjLat
            wgsLng += lng - gcjLng
        }

        return Pair(wgsLat, wgsLng)
    }

    // MARK: - Private Transform Helpers

    /**
     * Computes the latitude offset for the GCJ-02 transformation.
     *
     * @param x Longitude relative to the central meridian (lng - 105.0).
     * @param y Latitude relative to the central parallel (lat - 35.0).
     * @return The raw latitude delta before ellipsoid correction.
     */
    private fun transformLat(x: Double, y: Double): Double {
        var result = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * sqrt(abs(x))
        result += (20.0 * sin(6.0 * x * PI) + 20.0 * sin(2.0 * x * PI)) * 2.0 / 3.0
        result += (20.0 * sin(y * PI) + 40.0 * sin(y / 3.0 * PI)) * 2.0 / 3.0
        result += (160.0 * sin(y / 12.0 * PI) + 320.0 * sin(y * PI / 30.0)) * 2.0 / 3.0
        return result
    }

    /**
     * Computes the longitude offset for the GCJ-02 transformation.
     *
     * @param x Longitude relative to the central meridian (lng - 105.0).
     * @param y Latitude relative to the central parallel (lat - 35.0).
     * @return The raw longitude delta before ellipsoid correction.
     */
    private fun transformLng(x: Double, y: Double): Double {
        var result = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * sqrt(abs(x))
        result += (20.0 * sin(6.0 * x * PI) + 20.0 * sin(2.0 * x * PI)) * 2.0 / 3.0
        result += (20.0 * sin(x * PI) + 40.0 * sin(x / 3.0 * PI)) * 2.0 / 3.0
        result += (150.0 * sin(x / 12.0 * PI) + 300.0 * sin(x * PI / 30.0)) * 2.0 / 3.0
        return result
    }

    // MARK: - Math Helpers

    private const val PI = Math.PI

    private fun sin(x: Double): Double = Math.sin(x)
    private fun cos(x: Double): Double = Math.cos(x)
    private fun sqrt(x: Double): Double = Math.sqrt(x)
    private fun abs(x: Double): Double = Math.abs(x)
}
