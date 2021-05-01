package de.vincentscode.AINotes.Helpers

import android.graphics.PointF
import android.graphics.RectF
import androidx.core.graphics.component1
import androidx.core.graphics.component2
import kotlin.math.*


class MathHelper {
    companion object {
        fun isPointInPolygon(polygon: Array<PointF>, testPoint: PointF): Boolean {
            var result = false
            var j = polygon.size - 1

            val x = testPoint.x
            val y = testPoint.y

            for (i in polygon.indices) {
                if (polygon.get(i).y < y && polygon.get(j).y >= y || polygon.get(j).y < y && polygon.get(
                        i
                    ).y >= y
                ) if (polygon.get(i).x + (y - polygon.get(i).y) / (polygon.get(j).y - polygon.get(
                        i
                    ).y) * (polygon.get(j).x - polygon.get(i).x) < x
                ) {
                    result = !result
                }
                j = i
            }
            return result
        }

        public fun getNearestCluster(point: PointF, clusters: ArrayList<Cluster>): Cluster {
            var shortestDistanceCluster = Cluster()
            var shortestDistance = Double.MAX_VALUE

            clusters.toList().forEach {

                val ctr = it.getCenter()

                val x = ctr.x
                val y = ctr.y

                val (pX, pY) = point

                // pythagorean theorem
                val dst = sqrt((pX - x).toDouble().pow(2.0) + (pY - y).toDouble().pow(2.0))

                if (dst >= shortestDistance) return@forEach
                // updating shortest distance if dst is smaller
                shortestDistance = dst
                shortestDistanceCluster = it
            }

            return shortestDistanceCluster
        }

        fun cluster(points: ArrayList<PointF>): ArrayList<PointF> {
            if (points.size == 0) return points

            val handledPoints: ArrayList<PointF> = arrayListOf()
            val clusters: ArrayList<Cluster> = arrayListOf()

            val x = points.minBy { it.x }!!.x
            val y = points.minBy { it.y }!!.y
            val width = points.maxBy { it.x }!!.x - x
            val height = points.maxBy { it.y }!!.y - y

            val smallerSideLength = width.coerceAtLeast(height)

            // initialize
            val initialCluster = Cluster()
            initialCluster.points.add(points[0])
            clusters.add(initialCluster)
            handledPoints.add(points[0])

            points.forEach {
                if (handledPoints.contains(it)) return@forEach

                val nearestCluster = getNearestCluster(
                    it, arrayListOf(
                        clusters[0],
                        clusters[0]
                    )
                )

                val ctr = nearestCluster.getCenter()
                val (cX, xY) = ctr
                val dst = sqrt((it.x - cX).toDouble().pow(2.0) + (it.y - xY).toDouble().pow(2.0))

                if (dst < smallerSideLength / 4) {
                    nearestCluster.points.add(it)
                    handledPoints.add(it)
                } else {
                    val newCluster = Cluster()
                    newCluster.points.add(it)
                    clusters.add(newCluster)
                }
            }

            val arrayList = arrayListOf<PointF>()
            arrayList.addAll(clusters.map { cluster -> cluster.getCenter() })

            return arrayList
        }

        fun isPolylineClosed(points: ArrayList<PointF>): Boolean {
            val (fX, fY) = points.first()
            val (lX, lY) = points.last()

            val dst = sqrt((lX - fX).toDouble().pow(2.0) + (lY - fY).toDouble().pow(2.0))
            return dst < 100
        }

        // determines whether or not polyline is ellipse
        // does currently only support ellipses without specified angle
        fun isEllipse(stroke: ArrayList<PointF>): InkShape? {
            if (!isPolylineClosed(stroke)) return null

            val approximateX = stroke.minBy { pointF -> pointF.x }!!.x
            val approximateY = stroke.minBy { pointF -> pointF.y }!!.y
            val approximateWidth = stroke.maxBy { pointF -> pointF.x }!!.x - approximateX
            val approximateHeight = stroke.maxBy { pointF -> pointF.y }!!.y - approximateY

            val minSize = 150f // TODO divide by zoom factor

            if (approximateWidth < minSize || approximateHeight < minSize) return null

            var probability = 0.0

            // center and foci of the ellipse
            val approximateCenter =
                PointF(approximateX + approximateWidth / 2, approximateY + approximateHeight / 2)
            val approximateF: Double

            val approximateFocus0: PointF
            val approximateFocus1: PointF

            val pointsList = ArrayList<PointF>()

            val absR: Float

            // horizontal / vertical aligned ellipse
            if (approximateWidth > approximateHeight) {
                approximateF = sqrt(
                    (approximateWidth * 1f / 2).toDouble()
                        .pow(2.0) - (approximateHeight * 1f / 2).toDouble()
                        .pow(2.0)
                )

                approximateFocus0 =
                    PointF((approximateCenter.x - approximateF).toFloat(), approximateCenter.y)
                approximateFocus1 =
                    PointF((approximateCenter.x + approximateF).toFloat(), approximateCenter.y)

                absR = approximateWidth
            } else {
                approximateF = sqrt(
                    (approximateHeight * 1f / 2).toDouble()
                        .pow(2.0) - (approximateWidth * 1f / 2).toDouble()
                        .pow(2.0)
                )

                approximateFocus0 =
                    PointF(approximateCenter.x, ((approximateCenter.y - approximateF).toFloat()))
                approximateFocus1 =
                    PointF(approximateCenter.y, ((approximateCenter.y + approximateF).toFloat()))

                absR = approximateHeight
            }

            stroke.forEach {
                val f0d = sqrt(
                    (it.x - approximateFocus0.x).toDouble()
                        .pow(2.0) + (it.y - approximateFocus0.y).toDouble()
                        .pow(2.0)
                )
                val f1d = sqrt(
                    (it.x - approximateFocus1.x).toDouble()
                        .pow(2.0) + (it.y - approximateFocus1.y).toDouble()
                        .pow(2.0)
                )

                val d = f0d + f1d
                val error = abs(d - absR) / absR
                probability += 1 - error
                if (error > .2) probability -= .5
            }

            // TODO: improve "probability"
            probability /= stroke.size

            val stepSize = (Math.PI / stroke.size * 2)

            var factor = approximateWidth * 1f / approximateHeight
            if (Math.abs(factor - 1) < .2) {
                factor = 1f
            }
            val radius = approximateHeight / 2

            probability -= Math.abs(1 - factor) * .02
            Logger.log("MathHelper", "Ellipse Probability: $probability")

            var i = 0.0
            while (i <= 2 * Math.PI + 1) {
                i += stepSize
                val x = (approximateCenter.x + radius * cos(i) * factor).toFloat()
                val y = (approximateCenter.y + radius * sin(i)).toFloat()

                pointsList.add(PointF(x, y))
            }

            return InkShape().apply {
                this.points = pointsList
                this.probability = probability
                this.shapeType = ShapeType.Ellipse
            }
        }

        // returns whether or not lines intersect
        fun findLineIntersection(
            lineStart1: PointF,
            lineEnd1: PointF,
            lineStart2: PointF,
            lineEnd2: PointF
        ): Boolean {
            // use two-dimensional vectors to check for intersection
            val p: ArrayList<Float> = arrayListOf(
                lineStart1.x, lineStart1.y,
                lineEnd1.x - lineStart1.x, lineEnd1.y - lineStart1.y,
                lineStart2.x, lineStart2.y,
                lineEnd2.x - lineStart2.x, lineEnd2.y - lineStart2.y
            )

            if (p[6] * p[3] - p[7] * p[2] == 0f) return false

            val r =
                (p[0] * p[3] - p[1] * p[2] - p[4] * p[3] + p[5] * p[2]) / (p[6] * p[3] - p[7] * p[2])
            if (r < 0 || r > 1) return false
            val s = (p[4] + p[6] * r - p[0]) / p[2]
            return s in 0.0..1.0
        }

        fun getDotProductNormalized(p0: PointF, p1: PointF): Float {
            var (x0, y0) = p0
            var (x1, y1) = p1
            val l0 = sqrt(x0.toDouble().pow(2.0) + y0.toDouble().pow(2.0)).toFloat()
            val l1 = sqrt(x1.toDouble().pow(2.0) + y1.toDouble().pow(2.0)).toFloat()

            x0 /= l0
            y0 /= l0
            x1 /= l1
            y1 /= l1

            return x0 * x1 + y0 * y1
        }

        // returns angle (x-axis, start, end)
        fun angle(start: PointF, end: PointF): Float {
            val rad2Deg = 180.0 / Math.PI
            val (sX, xY) = start
            val (eX, eY) = end
            return (atan2((xY - eY).toDouble(), (eX - sX).toDouble()) * rad2Deg).toFloat()
        }

        fun alignPointToGrid(point: PointF): PointF {
            return point
//            var horizontal = App.EditorScreen.BackgroundCanvas.HorizontalStep
//            var vertical = App.EditorScreen.BackgroundCanvas.VerticalStep
//            var (x, y) = point
//
//            var xMod = x % horizontal
//            var yMod = y % vertical
//
//            return (PointF(xMod <= horizontal / 2 ? x - xMod : x - xMod + horizontal, yMod <= vertical / 2 ? y - yMod : y - yMod + vertical)
        }

        fun alignPolygon(polygon: InkShape): InkShape {
            // comparison threshold for opposite side lengths
            val threshold = 200
            // min dot product for normalized vectors
            val dotP = .15

            Logger.log("MathHelper", "Align Polygon with ${polygon.points.size} corners")

            // rectangle requires four corners / sides
            if (polygon.points.size == 5) {
                Logger.log("MathHelper", "Polygon has 4 corners")
                val a = polygon.points[0]
                val b = polygon.points[1]
                val c = polygon.points[2]
                val d = polygon.points[3]

                val ab = PointF(b.x - a.x, b.y - a.y)
                val bc = PointF(c.x - b.x, c.y - b.y)
                val cd = PointF(d.x - c.x, d.y - c.y)
                val da = PointF(a.x - d.x, a.y - d.y)

                val lab = sqrt(ab.x.toDouble().pow(2.0) + ab.y.toDouble().pow(2.0))
                var lbc = sqrt(bc.x.toDouble().pow(2.0) + bc.y.toDouble().pow(2.0))
                val lcd = sqrt(cd.x.toDouble().pow(2.0) + cd.y.toDouble().pow(2.0))
                val lda = sqrt(da.x.toDouble().pow(2.0) + da.y.toDouble().pow(2.0))

                Logger.log("MathHelper", "Calculated")

                // check for the lengths of opposite sides
                if (abs(lab - lcd) < threshold && Math.abs(lbc - lda) < threshold) {
                    Logger.log("MathHelper", "Side lengths almost equal")

                    // dot products for each intersection
                    val abc = abs(getDotProductNormalized(PointF(ab.x, ab.y), PointF(bc.x, bc.y)))
                    val bcd = abs(getDotProductNormalized(PointF(bc.x, bc.y), PointF(cd.x, cd.y)))
                    val cda = abs(getDotProductNormalized(PointF(cd.x, cd.y), PointF(da.x, da.y)))
                    val dab = abs(getDotProductNormalized(PointF(da.x, da.y), PointF(ab.x, ab.y)))

                    Logger.log("MathHelper", "$abc, $bcd, $cda, $dab")

                    // checking for all angles to be near 90 degrees => rectangle
                    if (abc < dotP && bcd < dotP && cda < dotP && dab < dotP) {
                        // recalculating bc for orthogonality
                        val factor = if (bc.x < 0) -1 else 1
                        bc.x = abs(bc.y * ab.y / ab.x) * factor

                        // orig dist b - c
                        val dstBc = sqrt(
                            (b.x - c.x).toDouble().pow(.0) + (b.y - c.y).toDouble()
                                .pow(2.0)
                        )

                        lbc = sqrt(bc.x.toDouble().pow(2.0) + bc.y.toDouble().pow(2.0))

                        bc.x /= lbc.toFloat()
                        bc.y /= lbc.toFloat()

                        bc.x *= dstBc.toFloat()
                        bc.y *= dstBc.toFloat()

                        lbc = dstBc

                        // grid alignment
                        val horV = PointF(1f, 0f)
                        val verV = PointF(0f, 1f)
                        if (abs(getDotProductNormalized(horV, PointF(ab.x, ab.y))) < dotP) {
                            // ab is almost vertical
                            val fab1 = if (ab.x < 0) -1 else 1
                            val fab2 = if (ab.y < 0) -1 else 1
                            ab.x = (verV.x * lab * fab1).toFloat()
                            ab.y = (verV.y * lab * fab2).toFloat()

                            val fbc1 = if (bc.x < 0) -1 else 1
                            val fbc2 = if (bc.y < 0) -1 else 1
                            bc.x = (horV.x * lbc * fbc1).toFloat()
                            bc.y = (horV.y * lbc * fbc2).toFloat()

                            polygon.alignToGrid = true
                        } else if (abs(getDotProductNormalized(verV, PointF(ab.x, ab.y))) < dotP) {
                            // ab is almost horizontal
                            val fab1 = if (ab.x < 0) -1 else 1
                            val fab2 = if (ab.y < 0) -1 else 1
                            ab.x = (horV.x * lab * fab1).toFloat()
                            ab.y = (horV.y * lab * fab2).toFloat()

                            val fbc1 = if (bc.x < 0) -1 else 1
                            val fbc2 = if (bc.y < 0) -1 else 1
                            bc.x = (verV.x * lbc * fbc1).toFloat()
                            bc.y = (verV.y * lbc * fbc2).toFloat()

                            polygon.alignToGrid = true
                        }

                        polygon.points = arrayListOf(
                            a,
                            PointF(a.x + ab.x, a.y + ab.y),
                            PointF(a.x + ab.x + bc.x, a.y + ab.y + bc.y),
                            PointF(a.x + bc.x, a.y + bc.y),
                            a
                        )
                    }
                }
            }

            if (polygon.alignToGrid) {
                polygon.points.clear()
                polygon.points.addAll(polygon.points.map { pointF -> alignPointToGrid(pointF) })
            }
            return polygon
        }

        fun removePolygonCornerOutliers(
            polygon: InkShape,
            iteration: Int,
            outliers: ArrayList<PointF> = arrayListOf()
        ): InkShape {
            if (iteration == polygon.points.size) {
                val newPolygonPoints =
                    polygon.points.filter { pointF -> !outliers.contains(pointF) }
                polygon.points.clear()
                polygon.points.addAll(newPolygonPoints)
                return polygon
            }

            polygon.points.forEach {
                val index = polygon.points.indexOf(it)
                if (index + 1 <= polygon.points.size - 2) {
                    val middlePoint = polygon.points[index + 1]
                    val endingPoint = polygon.points[index + 2]

                    if (!outliers.contains(it)) {
                        val line = isLine(
                            arrayListOf(
                                PointF(it.x, it.y),
                                PointF(middlePoint.x, middlePoint.y),
                                PointF(endingPoint.x, endingPoint.y)
                            ), 50.0
                        )

                        if (line != null && line.shapeType != ShapeType.None) {
                            outliers.add(middlePoint)
                        }
                    }
                }
            }

            val pl = polygon.points.clone() as ArrayList<PointF>
            val p0 = pl[0]
            pl.remove(p0)
            pl.add(p0)
            return removePolygonCornerOutliers(polygon, iteration + 1, outliers)
        }

        // determines whether or not poly line is polygon
        fun isPolygon(stroke: ArrayList<PointF>): InkShape? {
            if (stroke.size == 0) return null

            val strokeX = stroke.minBy { pointF -> pointF.x }!!.x
            val strokeY = stroke.minBy { pointF -> pointF.y }!!.y
            val strokeWidth = stroke.maxBy { pointF -> pointF.x }!!.x - strokeX
            val strokeHeight = stroke.maxBy { pointF -> pointF.y }!!.y - strokeY

            var inkShape = InkShape().apply {
                shapeType = ShapeType.None
                probability = 0.0
            }
            // needs more than 30 elements to calculate properly
            if (stroke.size < 30) return null

            val possibleCorners = arrayListOf<PointF>()
            var lastAngle =
                angle(PointF(stroke[0].x, stroke[0].y), PointF(stroke[1].x, stroke[1].y))

            if (!isPolylineClosed(stroke)) return null

            val stepDiff = 10

            // checking for possible corners by potting degree value in between two points - iteration

            for (i in 0..stroke.size) {
                if (stroke.size - 1 - i <= stepDiff) continue

                val (cX, cY) = stroke[i]
                val (nX, nY) = stroke[i + stepDiff]
                val angle = angle(PointF(cX, cY), PointF(nX, nY))
                val diff = abs(angle - lastAngle)

                if (diff > 5) possibleCorners.add(
                    PointF(
                        stroke[i + stepDiff / 2].x,
                        stroke[i + stepDiff / 2].y
                    )
                )
                lastAngle = angle
            }

            // creating clusters to concentrate on relevant corners
            val clustered = cluster(possibleCorners)
            if (clustered.size == 0) return null

            val clusteredX = clustered.minBy { pointF -> pointF.x }!!.x
            val clusteredY = clustered.minBy { pointF -> pointF.y }!!.y
            val clusteredWidth = clustered.maxBy { pointF -> pointF.x }!!.x - clusteredX
            val clusteredHeight = clustered.maxBy { pointF -> pointF.y }!!.y - clusteredY

            val percentageRectFilling =
                clusteredWidth * clusteredHeight / (strokeWidth * strokeHeight)

            // debug
            for (point in clustered) {
                inkShape.points.add(point)
            }

            // first clustered
            val (x, y) = clustered.first()
            inkShape.points.add(PointF(x, y))

            inkShape = removePolygonCornerOutliers(inkShape, 0)

            if (clustered.size > 8 || percentageRectFilling < .8) return null

            inkShape = alignPolygon(inkShape)

            // TODO: improve "probability"
            // TODO: replace 10 with preference
            val threshold = 10 / 100
            inkShape.probability = if (threshold < .93) threshold + .001 else .931
            if (inkShape.points.size < 5) inkShape.probability = .95
            inkShape.shapeType = ShapeType.Polygon

            return inkShape
        }

        // determines whether or not polyline is line
        // checks for each point in polyline whether is lies in a specified area defined by scope and outer points
        fun isLine(points: ArrayList<PointF>, scope: Double = 25.0): InkShape? {
            var isLine = true
            val firstPoint = points.first()
            val lastPoint = points.last()

            val (leftPointX, leftPointY) = if (firstPoint.x < lastPoint.x) firstPoint else lastPoint
            val (rightPointX, rightPointY) = if (firstPoint.x < lastPoint.x) lastPoint else firstPoint

            val degree = atan2(
                (rightPointY - leftPointY).toDouble(),
                (rightPointX - leftPointX).toDouble()
            ) * 180 / Math.PI

            val hypotenuse = sqrt(2 * scope.pow(2))

            val polygon = arrayListOf(
                PointF(
                    (leftPointX - cos(Math.PI * (degree - 45) / 180.0) * hypotenuse).toFloat(),
                    (leftPointY - sin(Math.PI * (degree - 45) / 180.0) * hypotenuse).toFloat()
                ),
                PointF(
                    (leftPointX - cos(Math.PI * (degree + 45) / 180.0) * hypotenuse).toFloat(),
                    (leftPointY - sin(Math.PI * (degree + 45) / 180.0) * hypotenuse).toFloat()
                ),
                PointF(
                    (rightPointX + cos(Math.PI * (degree - 45) / 180.0) * hypotenuse).toFloat(),
                    (rightPointY + sin(Math.PI * (degree - 45) / 180.0) * hypotenuse).toFloat()
                ),
                PointF(
                    (rightPointX + cos(Math.PI * (degree + 45) / 180.0) * hypotenuse).toFloat(),
                    (rightPointY + sin(Math.PI * (degree + 45) / 180.0) * hypotenuse).toFloat()
                )
            )

            val probability = 1.0

            var dst = sqrt(
                (rightPointX - leftPointX).toDouble()
                    .pow(2.0) + (rightPointY - leftPointY).toDouble()
                    .pow(2.0)
            )

            var minDst = 150f / 1 // replace 1 with zoom factor

            try {

                if (points.any { pointF ->
                        !isPointInPolygon(polygon.toTypedArray(), pointF)
                    }) isLine = false

                // TODO: change to preference
                val adjustLines = true

                if (!adjustLines) {
                    return if (isLine) {
                        InkShape().apply {
                            this.points = arrayListOf(
                                PointF(firstPoint.x, firstPoint.y),
                                PointF(lastPoint.x, lastPoint.y)
                            )
                            this.probability = probability
                            this.shapeType = ShapeType.Line
                        }
                    } else {
                        null
                    }
                }

                if (!isLine) return null

                if (degree < 5 && degree > -5) {
                    val newY = (firstPoint.y + lastPoint.y) / 2
                    return InkShape().apply {
                        this.points = arrayListOf(
                            PointF(firstPoint.x, newY),
                            PointF(lastPoint.x, newY)
                        )
                        this.probability = probability
                        this.shapeType = ShapeType.Line
                    }
                }

                if (degree > 85 || degree < -85) {
                    val newX = (firstPoint.x + lastPoint.x) / 2

                    return InkShape().apply {
                        this.points = arrayListOf(
                            PointF(newX, firstPoint.y),
                            PointF(newX, lastPoint.y)
                        )
                        this.probability = probability
                        this.shapeType = ShapeType.Line
                    }
                }

                if (degree < 50 && degree > 40) {
                    val length = sqrt(
                        (rightPointX - leftPointX).toDouble()
                            .pow(2.0) + (leftPointY - rightPointY).toDouble()
                            .pow(2.0)
                    )

                    val middleX = (rightPointX + leftPointX) / 2
                    val middleY = (rightPointY + leftPointY) / 2

                    val diffX = sqrt((length / 2).pow(2) / 2)
                    val diffY = sqrt((length / 2).pow(2.0) / 2)

                    return InkShape().apply {
                        this.points = arrayListOf(
                            PointF((middleX - diffX).toFloat(), (middleY - diffY).toFloat()),
                            PointF((middleX + diffX).toFloat(), (middleY + diffY).toFloat())
                        )
                        this.probability = probability
                        this.shapeType = ShapeType.Line
                    }
                }

                if (!(degree < -40) || !(degree > -50)) {
                    return InkShape().apply {
                        this.points = arrayListOf(
                            PointF(firstPoint.x, firstPoint.y),
                            PointF(lastPoint.x, lastPoint.y)
                        )
                        this.probability = probability
                        this.shapeType = ShapeType.Line
                    }
                } else {
                    val length = sqrt(
                        (rightPointX - leftPointX).toDouble()
                            .pow(2.0) + (leftPointY - rightPointY).toDouble()
                            .pow(2.0)
                    )
                    val middleX = (rightPointX + leftPointX) / 2
                    val middleY = (rightPointY + leftPointY) / 2

                    val diffX = sqrt((length / 2).pow(2.0) / 2)
                    val diffY = sqrt((length / 2).pow(2.0) / 2)

                    return InkShape().apply {
                        this.points = arrayListOf(
                            PointF((middleX - diffX).toFloat(), (middleY + diffY).toFloat()),
                            PointF((middleX + diffX).toFloat(), (middleY - diffY).toFloat())
                        )
                        this.probability = probability
                        this.shapeType = ShapeType.Line
                    }
                }

            } catch (e: java.lang.Exception) {
                Logger.log("[MathHelper]", "Exception in RecognizeLines: $e")
                return InkShape()
            }
        }

        // returns list of converted shapes with 2D point sets
        fun getShapesFromPolyline(strokes: ArrayList<ArrayList<PointF>>): ArrayList<InkShape> {
            val finalShapes = arrayListOf<InkShape>()
            val strokeBounds = HashMap<ArrayList<PointF>, RectF>()
            val handledStrokes = ArrayList<ArrayList<PointF>>()
            val strokeNodes = ArrayList<List<List<PointF>>>()

            // calculating stroke bounds
            for (stroke in strokes) {
                val x = stroke.minBy { pointF -> pointF.x }!!.x
                val y = stroke.minBy { pointF -> pointF.y }!!.y
                val width = stroke.maxBy { pointF -> pointF.x }!!.x - x
                val height = stroke.maxBy { pointF -> pointF.y }!!.y - y

                strokeBounds[stroke] = RectF(x, y, width, height)
            }

            // collecting intersecting strokes
            for ((valueTuples, value) in strokeBounds) {
                val intersectingStrokes = arrayListOf(
                    valueTuples
                )

                handledStrokes.add(valueTuples)

                for ((key, value2) in strokeBounds) {
                    if (!handledStrokes.contains(key)) {
                        if (value2.intersects(value.left, value.top, value.right, value.bottom)) {
                            intersectingStrokes.add(valueTuples)
                            handledStrokes.add(valueTuples)
                        }
                    }
                }

                strokeNodes.add(intersectingStrokes)
            }

            for (node in strokeNodes.toTypedArray()) {
                val shapes = HashMap<Double, InkShape>()
                val combinedPoints = arrayListOf<PointF>()
                for (stroke in node) combinedPoints.addAll(stroke)

                val ellipse = isEllipse(combinedPoints)
                if (ellipse != null && !shapes.containsKey(ellipse.probability)) shapes[ellipse.probability] =
                    ellipse

                val polygon = isPolygon(combinedPoints)
                if (polygon != null && !shapes.containsKey(polygon.probability)) shapes[polygon.probability] =
                    polygon

                val line = isLine(combinedPoints)
                if (line != null && !shapes.containsKey(line.probability)) shapes[line.probability] =
                    line

                if (shapes.size == 0) return finalShapes
                val highestVote = shapes.maxBy { entry -> entry.key }!!.key

                if (highestVote * 100 < 10) { // replace 10 with conversion threshold preference
                    finalShapes.add(InkShape().apply {
                        this.shapeType = ShapeType.None
                    })

                    continue
                }

                val shape = shapes[highestVote]

                val shapeX = shape?.points?.minBy { pointF -> pointF.x }!!.x
                val shapeY = shape.points.minBy { pointF -> pointF.y }!!.y
                val shapeWidth = shape.points.maxBy { pointF -> pointF.x }!!.x - shapeX
                val shapeHeight = shape.points.maxBy { pointF -> pointF.y }!!.y - shapeY

                val minLength = 150 / 1 // replace 1 with zoom factor

                if (shapeWidth < minLength && shapeHeight < minLength) {
                    finalShapes.add(InkShape().apply {
                        this.shapeType = ShapeType.None
                    })
                    continue
                }

                finalShapes.add(shape)
            }

            return finalShapes
        }
    }
}

public class Cluster {
    var points: ArrayList<PointF> = arrayListOf()

    // returns center of cluster / Points
    fun getCenter(): PointF {
        if (points.size == 0) throw Exception("Points cannot be empty.")
        var totalX = 0f
        var totalY = 0f

        points.forEach {
            totalX += it.x
            totalY += it.y
        }

        val centerX = totalX / points.size
        val centerY = totalY / points.size

        return PointF(centerX, centerY)
    }
}

enum class ShapeType {
    Polygon,
    Ellipse,
    Line,
    None
}

public class InkShape {
    // kind of shape (ShapeType)
    public var shapeType: ShapeType = ShapeType.None

    // points that define the shape
    public var points: ArrayList<PointF> = arrayListOf()

    // conversion probability
    public var probability: Double = 0.0

    // is rectangle -> shall be aligned to grid
    public var alignToGrid: Boolean = false
}