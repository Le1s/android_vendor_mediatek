package com.mediatek.mage.plant.p2;

import com.mediatek.ngin3d.ActorNode;
import com.mediatek.ngin3d.Quaternion;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.Vec3;

/*
 * Encapsulates a single joint from the plant model.
 */
class Joint {

    private static final Vec3 BONE_DIR = Vec3.X_AXIS;
    private static final Vec3 BONE_ORTH = Vec3.Y_AXIS;

    public static class JointPhysics {
        public JointPhysics(String name, float springLength, float springStrength, float damping) {
            mName = name;
            mSpringLength = springLength;
            mSpringStrength = springStrength;
            mDamping = damping;
        }
        public float mSpringLength;
        public float mSpringStrength;
        public float mDamping;
        public String mName;
        public Quaternion mFold = new Quaternion();
    }

    public static class GlobalPhysics {
        public Vec3 mGravity = new Vec3();
        public float mExtraDamping = 1f;
        public Quaternion mFold = new Quaternion();
    }

    public ActorNode mActorNode;

    // mPosition and mOriginalRotation do not change after initialisation
    private Quaternion mOriginalRotation = new Quaternion();
    private float mRootiness;

    // Current rotation as supplied to ActorNode
    private Rotation mRotation = new Rotation();

    private Joint mPrev;

    private boolean mSimulate = false;

    private Vec3 mWorldPosition = new Vec3();
    private Quaternion mWorldRotation = new Quaternion();
    private Quaternion mWorldSpringRotation = new Quaternion();

    private Vec3 mWorldEndPosition = new Vec3();
    private Vec3 mWorldSpringPosition = new Vec3();
    private Vec3 mDirection = new Vec3();
    private Vec3 mOrthogonalTarget = new Vec3(); // Used to remove twist from rotation

    Vec3 mVelocity = new Vec3();

    private Vec3 mTempVec1 = new Vec3();
    private Vec3 mTempVec2 = new Vec3();
    private Vec3 mTempVec3 = new Vec3();
    private Vec3 mTempVec4 = new Vec3();
    private Quaternion mTempQuat = new Quaternion();
    private Rotation mTempRot = new Rotation();

    public JointPhysics mPhysics;
    private GlobalPhysics mGlobalPhysics;


    /*
     * Index represents the index of the joint (zero is the bottom joint)
     */
    public Joint(ActorNode actorNode, Joint prev,
                 float x, float y, float z,
                 float a, float b, float c, float d,
                 JointPhysics physics, GlobalPhysics globalPhysics) {
        mActorNode = actorNode;
        mPhysics = physics;
        mGlobalPhysics = globalPhysics;
        y = 0;
        z = 0;

        mPrev = prev;
        mOriginalRotation.set(a, b, c, d);
        mRotation.set(a, b, c, d, false);
        mWorldRotation.set(a, b, c, d); // Only correct for root (others will be set on first update)
        //mDirection.set(.075f, 0f, 0f);
        mDirection.set(physics.mSpringLength, 0f, 0f);
        //mDirection.set(.05f + (float)Math.random() * 0.1f, 0f, 0f);
        mOrthogonalTarget = mOriginalRotation.applyTo(BONE_ORTH);
        if (prev != null) {
            prev.mSimulate = true;
            mPrev.mWorldEndPosition.set(x, y, z);
            Vec3 dir = new Vec3(x, y, z);
            if (dir.getLength() > 0.001f) {
                //mPrev.mDirectionVector.set(x, y, z);
                mPrev.mOrthogonalTarget = mPrev.mOriginalRotation.applyTo(BONE_ORTH);
            }
        }
        while (prev != null) {
            prev.mRootiness += 1f;
            prev = prev.mPrev;
        }
    }

    public void updateSpringPosition() {
        if (mPrev != null && mSimulate) {
            mWorldPosition.set(mPrev.mWorldEndPosition);

            mWorldRotation.set(mRotation.getQuaternion());
            mWorldRotation.multiply(mPrev.mWorldRotation);

            MathUtil.setVectorToSumOf(mWorldEndPosition, mPrev.mWorldEndPosition,
                                      MathUtil.setVectorToProductOf(mTempVec1, mWorldRotation, mDirection));

            mWorldSpringRotation.set(mOriginalRotation);
            mWorldSpringRotation.multiply(mPrev.mWorldRotation);

            /* Method using only existing ngin3d (requires allocations/garbage)
             * mWorldSpringPosition.set(mPrev.mWorldEndPosition);
             * mWorldSpringPosition = mWorldSpringPosition.add(mWorldSpringRotation.applyTo(mDirection));
             * */

            // Method using new functions.
            MathUtil.setVectorToSumOf(mWorldSpringPosition, mPrev.mWorldEndPosition,
                                      MathUtil.setVectorToProductOf(mTempVec1, mWorldSpringRotation, mDirection));
        }
    }

    public void updateNodePosition() {
        if (mPrev != null && mSimulate) {
            //Vec3 oldPos = new Vec3(mWorldEndPosition);
            mTempVec1.set(mWorldEndPosition);
            float damping = mPhysics.mDamping * mGlobalPhysics.mExtraDamping;
            float springStrength = mPhysics.mSpringStrength;
            for (int i = 0; i != 4; ++i) {
                mVelocity.x += mGlobalPhysics.mGravity.x;
                mVelocity.y += mGlobalPhysics.mGravity.y;
                mVelocity.z += mGlobalPhysics.mGravity.z;

                float strength = springStrength * ((1f / mRootiness) + 0.5f);

                mVelocity.x += (mWorldSpringPosition.x - mWorldEndPosition.x) * strength;
                mVelocity.y += (mWorldSpringPosition.y - mWorldEndPosition.y) * strength;
                mVelocity.z += (mWorldSpringPosition.z - mWorldEndPosition.z) * strength;

                mVelocity.x *= damping;
                mVelocity.y *= damping;
                mVelocity.z *= damping;
                mWorldEndPosition.x += 0.01f * mVelocity.x;
                mWorldEndPosition.y += 0.01f * mVelocity.y;
                mWorldEndPosition.z += 0.01f * mVelocity.z;
                if (mWorldEndPosition.z < -0.2f) {
                    mWorldEndPosition.z = -0.2f;
                    mVelocity.z = 0f;
                }
            }

            //Vec3 from = oldPos.subtract(mPrev.mWorldEndPosition);
            //Vec3 to = mWorldEndPosition.subtract(mPrev.mWorldEndPosition);
            //mWorldRotation.multiply(Rotation.fromTo(from, to).getQuaternion());

            // from = old_world_position - parent_end
            MathUtil.setVectorToDifferenceOf(mTempVec2, mTempVec1, mPrev.mWorldEndPosition);
            // to = new_world_position - parent_end
            MathUtil.setVectorToDifferenceOf(mTempVec3, mWorldEndPosition, mPrev.mWorldEndPosition);
            mWorldRotation.multiply(MathUtil.setQuaternionFromTo(mTempQuat, mTempVec2, mTempVec3));
        }

    }

    public void updateNodeRotation() {
        if (mPrev != null && mSimulate) {
            Quaternion qRot = mRotation.getQuaternion();
            mTempQuat.set(
                mPrev.mWorldRotation.getQ0(),
                -mPrev.mWorldRotation.getQ1(),
                -mPrev.mWorldRotation.getQ2(),
                -mPrev.mWorldRotation.getQ3());
            qRot.set(mWorldRotation);
            qRot.multiply(mTempQuat);
            qRot.nor();

            /* After pointing, the rotation will have an arbitrary "roll" around
             * the "to" z-axis.  We must find the new position of the "up" vector
             * after rotation.
             */
            //Vec3 dir = qRot.applyTo(BONE_DIR);
            //Vec3 rolledUp = qRot.applyTo(BONE_ORTH);
            MathUtil.setVectorToProductOf(mTempVec1, qRot, BONE_DIR);
            MathUtil.setVectorToProductOf(mTempVec2, qRot, BONE_ORTH);

            /* We now project this vector and the original "up" vector onto a plane
             * perpendicular to the "to" vector so that we can find the extra
             * rotation needed to rotate the rolled "up" vector onto the given
             * "up" vector. Note that these vectors won't have unit length.
             */
            //Vec3 upProjected = mOrthogonalTarget.subtract(Vec3.dotProduct(mTempVec1, mOrthogonalTarget), mTempVec1);
            float dot = Vec3.dotProduct(mTempVec1, mOrthogonalTarget);
            mTempVec3.set(dot * mTempVec1.x, dot * mTempVec1.y, dot * mTempVec1.z);
            MathUtil.setVectorToDifferenceOf(mTempVec4, mOrthogonalTarget, mTempVec3);

            /* Calculate the rotation bring rolledUpProjected onto upProjected.
             * Note that this rotation will be around the "to" vector (because both
             * vectors are parallel to the "to" vector after projection).
             */
            //Rotation rollRotation = Rotation.fromTo(mTempVec2, upProjected);
            //qRot.multiply(rollRotation.getQuaternion());
            MathUtil.setQuaternionFromTo(mTempQuat, mTempVec2, mTempVec4);
            qRot.multiply(mTempQuat);

            mTempRot.getQuaternion().set(qRot);
            mTempRot.getQuaternion().multiply(mPhysics.mFold);
            mActorNode.setRotation(mTempRot);
        }
    }


    public void setRotation(Quaternion r) {
        mOriginalRotation.set(r);
        mRotation.getQuaternion().set(r);
        mWorldRotation.set(r);
        mActorNode.setRotation(mRotation);
    }
}

