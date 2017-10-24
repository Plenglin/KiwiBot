/*
 * Copyright 2016 Sławomir Czerwiński
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.info.czerwinski.physics2d

/**
 * 2D vector.
 *
 * @property x Abscissa.
 * @property y Ordinate.
 * @constructor Creates a new 2D vector.
 * @author Sławomir Czerwiński
 */
data class Vector (val x: Float, val y: Float) {

    /**
     * Vector magnitude.
     */
    val magnitude: Float = Math.sqrt(x.toDouble() * x.toDouble() + y.toDouble() * y.toDouble()).toFloat()

    val theta: Double = Math.atan2(y.toDouble(), x.toDouble())

    /**
     * Adds two vectors.
     *
     * @param v Addend.
     * @return Sum of vectors.
     */
    operator fun plus(v: Vector): Vector {
        return Vector(x + v.x, y + v.y)
    }

    /**
     * Subtracts two vectors.
     *
     * @param v Subtrahent.
     * @return Difference of vectors.
     */
    operator fun minus(v: Vector): Vector {
        return Vector(x - v.x, y - v.y)
    }

    /**
     * Calculates dot product of two vectors.
     *
     * @param v Factor.
     * @return Dot product of vectors.
     */
    operator fun times(v: Vector): Float {
        return x * v.x + y * v.y
    }

    /**
     * Multiplies vector by scalar.
     *
     * @param f Multiplicand.
     * @return Product of vector and scalar.
     */
    operator fun times(f: Float): Vector {
        return Vector(x * f, y * f)
    }

    /**
     * Divides vector by scalar.
     *
     * @param d Divisor.
     * @return Quotient of vector and scalar.
     */
    operator fun div(d: Float): Vector {
        return Vector(x / d, y / d)
    }

    /**
     * Calculates opposite vectors.
     *
     * @return Opposite vectors.
     */
    operator fun unaryMinus(): Vector {
        return Vector(-x, -y)
    }

    /**
     * Returns string representation of vector.
     */
    override fun toString(): String {
        return "[${x}, ${y}]"
    }

    fun rotated(theta: Double): Vector {
        val cos = Math.cos(theta)
        val sin = Math.sin(theta)
        return Vector((x * cos + y * sin).toFloat(), (y * cos - x * sin).toFloat())
    }

    companion object {
        fun angle(theta: Double): Vector =
                Vector(Math.cos(theta).toFloat(), Math.sin(theta).toFloat())
    }
}