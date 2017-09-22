
# used to change from "hdpi,xhdpi" to "hdpi xhdpi"
comma:= ,
empty:=
space:= $(empty) $(empty)

MTK_RMS_ERROR_MESSAGES :=

# check default overlay path, should be device/mediatek/$(MTK_TARGET_PROJECT)/overlay & device/mediatek/$(MTK_BASE_PROJECT)/overlay
#############################################################
ifeq ( ,$(findstring banyan_x86,$(MTK_TARGET_PROJECT)))
ifneq ( ,$(strip $(MTK_TARGET_PROJECT)))
  ifeq ( ,$(findstring $(MTK_TARGET_PROJECT)/overlay,$(DEVICE_PACKAGE_OVERLAYS)))
     MTK_RMS_ERROR_MESSAGES += Please add device/<company>/$(MTK_TARGET_PROJECT)/overlay to DEVICE_PACKAGE_OVERLAYS;
  endif
endif
endif
ifeq ( ,$(findstring banyan_x86,$(MTK_BASE_PROJECT)))
ifneq ( ,$(strip $(MTK_BASE_PROJECT)))
  ifeq ( ,$(findstring $(MTK_BASE_PROJECT)/overlay,$(DEVICE_PACKAGE_OVERLAYS)))
     MTK_RMS_ERROR_MESSAGES += Please add device/<company>/$(MTK_BASE_PROJECT)/overlay to DEVICE_PACKAGE_OVERLAYS;
  endif
endif
endif


# check MTK_LCA_ROM_OPTIMIZE/MTK_LCA_RAM_OPTIMIZE & DEVICE_PACKAGE_OVERLAYS
#############################################################
ifeq (yes,$(strip $(MTK_LCA_ROM_OPTIMIZE)))
  ifeq ( ,$(filter device/mediatek/common/overlay/LCA_rom,$(DEVICE_PACKAGE_OVERLAYS)))
     MTK_RMS_ERROR_MESSAGES += Please add value LCA_rom to DEVICE_PACKAGE_OVERLAYS or turn off MTK_LCA_ROM_OPTIMIZE;
  endif
endif
ifneq (yes,$(strip $(MTK_LCA_ROM_OPTIMIZE)))
  ifneq ( ,$(filter device/mediatek/common/overlay/LCA_rom,$(DEVICE_PACKAGE_OVERLAYS)))
     MTK_RMS_ERROR_MESSAGES += Please remove LCA_rom from DEVICE_PACKAGE_OVERLAYS or turn on MTK_LCA_ROM_OPTIMIZE;
  endif
endif
ifeq (yes,$(strip $(MTK_LCA_RAM_OPTIMIZE)))
  ifeq ( ,$(filter device/mediatek/common/overlay/LCA_ram,$(DEVICE_PACKAGE_OVERLAYS)))
    MTK_RMS_ERROR_MESSAGES += Please add value LCA_ram to DEVICE_PACKAGE_OVERLAYS or turn off MTK_LCA_RAM_OPTIMIZE;
  endif
endif
ifneq (yes,$(strip $(MTK_LCA_RAM_OPTIMIZE)))
  ifneq ( ,$(filter device/mediatek/common/overlay/LCA_ram,$(DEVICE_PACKAGE_OVERLAYS)))
    MTK_RMS_ERROR_MESSAGES += Please remove LCA_ram from DEVICE_PACKAGE_OVERLAYS or turn on MTK_LCA_RAM_OPTIMIZE;
  endif
endif


# check MTK_LCA_ROM_OPTIMIZE & PRODUCT_AAPT_CONFIG
#############################################################
ifeq (yes,$(strip $(MTK_LCA_ROM_OPTIMIZE)))
  ifneq (yes,$(strip $(MTK_TABLET_PLATFORM)))
      ifeq ($(filter -sw600dp,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
         MTK_RMS_ERROR_MESSAGES += Please add -sw600dp in PRODUCT_AAPT_CONFIG or turn on MTK_TABLET_PLATFORM or turn off MTK_LCA_ROM_OPTIMIZE;
      endif
  endif
endif
ifneq (yes,$(strip $(MTK_LCA_ROM_OPTIMIZE)))
  ifeq (yes,$(strip $(MTK_TABLET_PLATFORM)))
      ifneq ($(filter -sw600dp,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
         MTK_RMS_ERROR_MESSAGES += Please removed -sw600dp in PRODUCT_AAPT_CONFIG or turn off MTK_TABLET_PLATFORM or turn on MTK_LCA_ROM_OPTIMIZE;
      endif
  endif
endif


# check resolution & PRODUCT_AAPT_CONFIG
#############################################################
ifneq (yes,$(strip $(MTK_TABLET_PLATFORM)))
ifneq (yes,$(strip $(MTK_BSP_PACKAGE)))
  ifeq (240,$(strip $(LCM_WIDTH)))
    ifeq (320,$(strip $(LCM_HEIGHT)))
      ifeq ($(filter ldpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add ldpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
    endif
  endif
  ifeq (240,$(strip $(LCM_WIDTH)))
    ifeq (400,$(strip $(LCM_HEIGHT)))
      ifeq ($(filter ldpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add ldpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
    endif
  endif
  ifeq (240,$(strip $(LCM_WIDTH)))
    ifeq (432,$(strip $(LCM_HEIGHT)))
      ifeq ($(filter ldpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add ldpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
    endif
  endif
  ifeq (320,$(strip $(LCM_WIDTH)))
    ifeq (480,$(strip $(LCM_HEIGHT)))
      ifeq ($(filter mdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add mdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
    endif
  endif
  ifeq (480,$(strip $(LCM_WIDTH)))
    ifeq (640,$(strip $(LCM_HEIGHT)))
      ifeq ($(filter mdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add mdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
    endif
  endif
  ifeq (480,$(strip $(LCM_WIDTH)))
    ifeq (800,$(strip $(LCM_HEIGHT)))
      ifeq ($(filter hdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add hdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
    endif
  endif
  ifeq (480,$(strip $(LCM_WIDTH)))
    ifeq (854,$(strip $(LCM_HEIGHT)))
      ifeq ($(filter hdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add hdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
    endif
  endif
  ifeq (540,$(strip $(LCM_WIDTH)))
    ifeq (960,$(strip $(LCM_HEIGHT)))
      ifeq ($(filter hdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add hdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
    endif
  endif
  ifeq (600,$(strip $(LCM_WIDTH)))
    ifeq (1024,$(strip $(LCM_HEIGHT)))
      ifeq ($(filter hdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add hdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
    endif
  endif
  ifeq (720,$(strip $(LCM_WIDTH)))
    ifeq (1280,$(strip $(LCM_HEIGHT)))
      ifeq ($(filter hdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add hdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
      ifeq ($(filter xhdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add xhdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
    endif
  endif
  ifeq (768,$(strip $(LCM_WIDTH)))
    ifeq (1280,$(strip $(LCM_HEIGHT)))
      ifeq ($(filter hdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add hdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
      ifeq ($(filter xhdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add xhdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
    endif
  endif
  ifeq (800,$(strip $(LCM_WIDTH)))
    ifeq (1280,$(strip $(LCM_HEIGHT)))
      ifeq ($(filter hdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add hdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
      ifeq ($(filter xhdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add xhdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
    endif
  endif
  ifeq (1080,$(strip $(LCM_WIDTH)))
    ifeq (1920,$(strip $(LCM_HEIGHT)))
      ifeq ($(filter hdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add hdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
      ifeq ($(filter xhdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add xhdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
      ifeq ($(filter xxhdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add xxhdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
    endif
  endif
  ifeq (1200,$(strip $(LCM_WIDTH)))
    ifeq (1920,$(strip $(LCM_HEIGHT)))
      ifeq ($(filter hdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add hdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
      ifeq ($(filter xhdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add xhdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
      ifeq ($(filter xxhdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add xxhdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
    endif
  endif
  ifeq (1440,$(strip $(LCM_WIDTH)))
    ifeq (2560,$(strip $(LCM_HEIGHT)))
      ifeq ($(filter hdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add hdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
      ifeq ($(filter xhdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add xhdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
      ifeq ($(filter xxhdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add xxhdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
      ifeq ($(filter xxxhdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add xxxhdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
    endif
  endif
  ifeq (1600,$(strip $(LCM_WIDTH)))
    ifeq (2560,$(strip $(LCM_HEIGHT)))
      ifeq ($(filter hdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add hdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
      ifeq ($(filter xhdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add xhdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
      ifeq ($(filter xxhdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add xxhdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
      ifeq ($(filter xxxhdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
        MTK_RMS_ERROR_MESSAGES += Please add xxxhdpi to PRODUCT_AAPT_CONFIG or set different LCM_WIDTH and LCM_HEIGHT;
      endif
    endif
  endif
endif
endif

# check smart book & PRODUCT_AAPT_CONFIG
#############################################################
ifneq (yes,$(strip $(MTK_TABLET_PLATFORM)))
ifneq (yes,$(strip $(MTK_BSP_PACKAGE)))
  ifeq (yes,$(strip $(MTK_SMARTBOOK_SUPPORT)))
    ifeq ($(filter mdpi,$(subst $(comma),$(space),$(PRODUCT_AAPT_CONFIG))),)
      MTK_RMS_ERROR_MESSAGES += Please add mdpi to PRODUCT_AAPT_CONFIG or set MTK_SMARTBOOK_SUPPORT=no;
    endif
  endif
endif
endif

# check qHD & FWVGA
#############################################################
ifneq (yes,$(strip $(MTK_TABLET_PLATFORM)))
ifneq (yes,$(strip $(MTK_BSP_PACKAGE)))
  ifeq (480,$(strip $(LCM_WIDTH)))
    ifeq (854,$(strip $(LCM_HEIGHT)))
      ifeq ( ,$(filter device/mediatek/common/overlay/FWVGA,$(DEVICE_PACKAGE_OVERLAYS)))
        MTK_RMS_ERROR_MESSAGES += Please add value FWVGA to DEVICE_PACKAGE_OVERLAYS or set different LCM_WIDTH and LCM_HEIGHT;
      endif
    endif
  endif
  ifeq (540,$(strip $(LCM_WIDTH)))
    ifeq (960,$(strip $(LCM_HEIGHT)))
      ifeq ( ,$(filter device/mediatek/common/overlay/qHD,$(DEVICE_PACKAGE_OVERLAYS)))
        MTK_RMS_ERROR_MESSAGES += Please add value qHD to DEVICE_PACKAGE_OVERLAYS or set different LCM_WIDTH and LCM_HEIGHT;
      endif
    endif
  endif
endif
endif

# check OPTR_SPEC_SEG_DEF & DEVICE_PACKAGE_OVERLAYS
#############################################################
ifdef OPTR_SPEC_SEG_DEF
  ifneq ($(strip $(OPTR_SPEC_SEG_DEF)),NONE)
    OPTR := $(word 1,$(subst _,$(space),$(OPTR_SPEC_SEG_DEF)))
    SPEC := $(word 2,$(subst _,$(space),$(OPTR_SPEC_SEG_DEF)))
    SEG  := $(word 3,$(subst _,$(space),$(OPTR_SPEC_SEG_DEF)))

    ifeq ( ,$(filter device/mediatek/common/overlay/operator/$(OPTR)/$(SPEC)/$(SEG),$(DEVICE_PACKAGE_OVERLAYS)))
      MTK_RMS_ERROR_MESSAGES += Please correct DEVICE_PACKAGE_OVERLAYS or set different OPTR_SPEC_SEG_DEF($(OPTR_SPEC_SEG_DEF));
    endif
  endif
endif


ifneq ( ,$(strip $(MTK_RMS_ERROR_MESSAGES)))
$(info MTK_TARGET_PROJECT=$(MTK_TARGET_PROJECT))
$(info MTK_BASE_PROJECT=$(MTK_BASE_PROJECT))
$(info DEVICE_PACKAGE_OVERLAYS=$(DEVICE_PACKAGE_OVERLAYS))
$(info MTK_LCA_ROM_OPTIMIZE=$(MTK_LCA_ROM_OPTIMIZE))
$(info PRODUCT_AAPT_CONFIG=$(PRODUCT_AAPT_CONFIG))
$(info MTK_TABLET_PLATFORM=$(MTK_TABLET_PLATFORM))
$(info LCM_WIDTH=$(LCM_WIDTH))
$(info LCM_HEIGHT=$(LCM_HEIGHT))
$(info OPTR_SPEC_SEG_DEF=$(OPTR_SPEC_SEG_DEF))
$(info MTK_SMARTBOOK_SUPPORT=$(MTK_SMARTBOOK_SUPPORT))
ifneq ( ,$(filter DEVICE_PACKAGE_OVERLAYS,$(MTK_RMS_ERROR_MESSAGES)))
MTK_RMS_ERROR_MESSAGES += DEVICE_PACKAGE_OVERLAYS is set in device/<company>/$(MTK_TARGET_PROJECT)/device.mk;
endif
ifneq ( ,$(filter PRODUCT_AAPT_CONFIG,$(MTK_RMS_ERROR_MESSAGES)))
MTK_RMS_ERROR_MESSAGES += PRODUCT_AAPT_CONFIG is set in device/<company>/$(MTK_TARGET_PROJECT)/$(ROM_NAME_PREFIX)$(MTK_TARGET_PROJECT).mk;
endif
$(error $(MTK_RMS_ERROR_MESSAGES))
endif
