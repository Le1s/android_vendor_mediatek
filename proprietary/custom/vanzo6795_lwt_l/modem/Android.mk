define mtk-install-modem
include $$(CLEAR_VARS)
LOCAL_MODULE := $$(notdir $(1))
LOCAL_MODULE_CLASS := ETC
LOCAL_MODULE_PATH := $(2)
LOCAL_SRC_FILES := $(1)
include $$(BUILD_PREBUILT)
MTK_MODEM_INSTALLED_MODULES += $$(LOCAL_INSTALLED_MODULE)
MTK_MODEM_BUILT_MODULES += $$(LOCAL_BUILT_MODULE)
endef

MTK_MODEM_INSTALLED_MODULES :=
MTK_MODEM_BUILT_MODULES :=
MTK_MODEM_DATABASE_FILES :=

ifneq ($(strip $(CUSTOM_MODEM)),)
LOCAL_PATH := $(call my-dir)
include $(wildcard $(foreach item,$(CUSTOM_MODEM),$(LOCAL_PATH)/$(item)/Android.mk))
ALL_DEFAULT_INSTALLED_MODULES += $(MTK_MODEM_DATABASE_FILES)

  ifneq ($(filter update-modem,$(MAKECMDGOALS)),)
MTK_MODEM_REMOVED_MODULES := $(filter-out $(MTK_MODEM_INSTALLED_MODULES),$(wildcard $(TARGET_OUT_ETC)/firmware/modem*.img $(TARGET_OUT_ETC)/mddb/*))
.PHONY: update-modem
.PHONY: $(MTK_MODEM_BUILT_MODULES)
.PHONY: $(MTK_MODEM_INSTALLED_MODULES)
    ifeq ($(filter-out $(INTERNAL_MODIFIER_TARGETS) update-modem,$(MAKECMDGOALS)),)
MAKECMDGOALS := $(filter-out update-modem,$(MAKECMDGOALS))
update-modem: snod
snod: $(MTK_MODEM_INSTALLED_MODULES)
    endif
  endif
  ifneq ($(filter clean-modem,$(MAKECMDGOALS)),)
MTK_MODEM_REMOVED_MODULES := $(wildcard $(MTK_MODEM_INSTALLED_MODULES) $(TARGET_OUT_ETC)/firmware/modem*.img $(TARGET_OUT_ETC)/mddb/*)
.PHONY: clean-modem
clean-modem:

  endif
  ifneq ($(strip $(MTK_MODEM_REMOVED_MODULES)),)
$(info clean-modem: $(MTK_MODEM_REMOVED_MODULES))
MTK_TEMP := $(shell rm -f $(MTK_MODEM_REMOVED_MODULES))
  endif

endif

