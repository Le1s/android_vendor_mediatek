package com.mediatek.mage.plant.p2;

import com.mediatek.ngin3d.Quaternion;
import com.mediatek.ngin3d.Vec3;

/*
 * Encapsulates a single joint from the plant model.
 */
class MathUtil {
    /**
     * Set given quaternion to rotation from one vector to another.
     * The new rotation will transform the "startingDirection" vector to
     * the "finishingDirection" vector, using the shortest arc possible.
     *
     * @param startingDirection  Direction to transform from
     * @param finishingDirection Direction to transform to
     */
    public static final Quaternion setQuaternionFromTo(Quaternion q,
            Vec3 startingDirection, Vec3 finishingDirection) {

        final float ZERO_THRESHOLD = 0.0001f;
        /* Note that the Vec3 class is not used (except in the corner-case of
         * needing a rotation of 180 degrees) to avoid creating work for the
         * garbage collector.
         */

        float fromRecLen = 1.0f / startingDirection.getLength();
        float fromX = fromRecLen * startingDirection.x;
        float fromY = fromRecLen * startingDirection.y;
        float fromZ = fromRecLen * startingDirection.z;

        float toRecLen = 1.0f / finishingDirection.getLength();
        float toX = toRecLen * finishingDirection.x;
        float toY = toRecLen * finishingDirection.y;
        float toZ = toRecLen * finishingDirection.z;

        float halfX = fromX + toX;
        float halfY = fromY + toY;
        float halfZ = fromZ + toZ;
        float halfLength =
            (float) Math.sqrt(halfX * halfX + halfY * halfY + halfZ * halfZ);

        // First find a quaternion which will rotate the "from" vector to the
        // "to" vector
        if (halfLength < ZERO_THRESHOLD) {
            Vec3 axis = new Vec3(fromX, fromY, fromZ).getOrthogonal();
            q.set(axis, 180.f);
        } else {
            float halfRecLen = 1.0f / halfLength;
            halfX *= halfRecLen;
            halfY *= halfRecLen;
            halfZ *= halfRecLen;
            float dot = fromX * halfX + fromY * halfY + fromZ * halfZ;
            float crossX = fromY * halfZ - fromZ * halfY;
            float crossY = fromZ * halfX - fromX * halfZ;
            float crossZ = fromX * halfY - fromY * halfX;
            q.set(dot, crossX, crossY, crossZ);
            q.nor();
        }
        return q;
    }
    /**
     * Set vector to the product of a quaternion and a vector
     *
     * @param q Quaterion representing a rotation
     * @param v Original vector
     * @return This vector
     */
    public static final Vec3 setVectorToProductOf(Vec3 result, Quaternion q, Vec3 v) {

        float xOrig = v.x;
        float yOrig = v.y;
        float zOrig = v.z;

        float q0 = q.getQ0();
        float q1 = q.getQ1();
        float q2 = q.getQ2();
        float q3 = q.getQ3();

        float s = q1 * xOrig + q2 * yOrig + q3 * zOrig;
        float m0 = -q0;

        result.x = 2 * (m0 * (xOrig * m0 - (q2 * zOrig - q3 * yOrig)) + s * q1) - xOrig;
        result.y = 2 * (m0 * (yOrig * m0 - (q3 * xOrig - q1 * zOrig)) + s * q2) - yOrig;
        result.z = 2 * (m0 * (zOrig * m0 - (q1 * yOrig - q2 * xOrig)) + s * q3) - zOrig;
        return result;
    }

    /**
     * Set this vector to the sum of two vectors
     *
     * @param v1 1st vector
     * @param v2 2nd vector
     * @return This vector
     */
    public static final Vec3 setVectorToSumOf(Vec3 result, Vec3 v1, Vec3 v2) {
        result.x = v1.x + v2.x;
        result.y = v1.y + v2.y;
        result.z = v1.z + v2.z;
        return result;
    }

    /**
     * Set this vector to the difference of two vectors
     *
     * @param v1 1st vector
     * @param v2 2nd vector
     * @return This vector
     */
    public static final Vec3 setVectorToDifferenceOf(Vec3 result, Vec3 v1, Vec3 v2) {
        result.x = v1.x - v2.x;
        result.y = v1.y - v2.y;
        result.z = v1.z - v2.z;
        return result;
    }

}

