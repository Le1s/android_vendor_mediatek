ifeq ($(strip $(MTK_NFC_SUPPORT)), yes)

LOCAL_PATH:= vendor/mediatek/proprietary/external/mtknfc

########################################
# MTK NFC Clock Type & Rate Configuration
########################################

ifeq ($(wildcard device/mediatek/$(MTK_TARGET_PROJECT)/nfc.cfg),)
    PRODUCT_COPY_FILES += $(LOCAL_PATH)/nfc.cfg:system/etc/nfc.cfg
else
    PRODUCT_COPY_FILES += device/mediatek/$(MTK_TARGET_PROJECT)/nfc.cfg:system/etc/nfc.cfg
endif

#Copy Mifare lincense file
PRODUCT_COPY_FILES += $(LOCAL_PATH)/MTKNfclicense.lic:system/etc/MTKNfclicense.lic    

endif

