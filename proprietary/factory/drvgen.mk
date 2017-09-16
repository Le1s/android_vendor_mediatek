PRIVATE_CUSTOM_KERNEL_DCT:= $(if $(CUSTOM_KERNEL_DCT),$(CUSTOM_KERNEL_DCT),dct)
DRVGEN_TOOL := $(LOCAL_PATH)/dct/DrvGen
DWS_FILE := vendor/mediatek/proprietary/custom/$(MTK_BASE_PROJECT)/kernel/dct/$(PRIVATE_CUSTOM_KERNEL_DCT)/codegen.dws
ifeq ($(wildcard $(DWS_FILE)),)
    $(error $(DWS_FILE) is not exist!)
endif  

DRVGEN_FILE_LIST := $(factory_intermediates)/inc/cust_kpd.h \
                    $(factory_intermediates)/inc/cust_eint.h

$(factory_intermediates)/inc/cust_kpd.h: $(DRVGEN_TOOL) $(DWS_FILE)
	@mkdir -p $(dir $@)
	@$(DRVGEN_TOOL) $(DWS_FILE) $(factory_intermediates)/inc $(factory_intermediates)/inc kpd_h

$(factory_intermediates)/inc/cust_eint.h: $(DRVGEN_TOOL) $(DWS_FILE)
	@mkdir -p $(dir $@)
	@$(DRVGEN_TOOL) $(DWS_FILE) $(factory_intermediates)/inc $(factory_intermediates)/inc eint_h
