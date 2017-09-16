package com.mediatek.gba.cache;

import com.mediatek.gba.element.NafId;

import java.util.Arrays;

/**
 * implementation for GbaKeysCacheEntryKey.
 *
 * @hide
 */
class GbaKeysCacheEntryKey {

    private NafId mNafId;
    private long mSubId;

    /**
     * Construction function of GbaKeysCacheEntryKey.
     *
     */
    public GbaKeysCacheEntryKey(NafId nafId, long subId) {
        mNafId = nafId;
        mSubId = subId;
    }

    public long getSubId() {
        return mSubId;
    }

    public void setSubId(long subId) {
        mSubId = subId;
    }

    public NafId getNafId() {
        return mNafId;
    }

    public void setNafId(NafId nafId) {
        mNafId = nafId;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(mNafId.getNafIdBin());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        GbaKeysCacheEntryKey other = (GbaKeysCacheEntryKey) obj;

        if (mSubId != other.mSubId) {
            return false;
        }

        if (mNafId == null) {
            if (other.mNafId != null) {
                return false;
            }
        } else if (!mNafId.equals(other.mNafId)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "GbaKeysCacheEntryKey [nafId=" + mNafId + ", subId=" + mSubId + "]";
    }

}
