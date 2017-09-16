#ifndef cl_clang_storage_class_specifiers
#error Implementation requires cl_clang_storage_class_specifiers extension!
#endif

#pragma OPENCL EXTENSION cl_clang_storage_class_specifiers : enable

#ifdef cl_khr_fp64
#pragma OPENCL EXTENSION cl_khr_fp64 : enable
#endif

#ifdef cl_khr_fp16
#pragma OPENCL EXTENSION cl_khr_fp16 : enable
#endif

#define kernel __kernel
#define printf cl_printf

#define __IMAGE_SUPPORT__ 1

#define __ENDIAN_LITTLE__ 1
#define __OPENCL_VERSION__ 120
#define __OPENCL_C_VERSION__ 120

#define __kernel_exec(X, typen) __kernel __attribute__((work_group_size_hint(X, 1, 1))) __attribute__((vec_type_hint(typen)))
#define kernel_exec(X, typen) __kernel __attribute__((work_group_size_hint(X, 1, 1))) __attribute__((vec_type_hint(typen)))

/* Function Attributes */
#include <clc/clcfunc.h>

/* Pattern Macro Definitions */
#include <clc/clcmacro.h>

/* 6.1 Supported Data Types */
#include <clc/clctypes.h>

/* 6.2.3 Explicit Conversions */
#include <clc/convert.h>

/* 6.2.4.2 Reinterpreting Types Using as_type() and as_typen() */
#include <clc/as_type.h>

/* 6.11.1 Work-Item Functions */
#include <clc/workitem/get_global_size.h>
#include <clc/workitem/get_global_id.h>
#include <clc/workitem/get_local_size.h>
#include <clc/workitem/get_local_id.h>
#include <clc/workitem/get_num_groups.h>
#include <clc/workitem/get_group_id.h>
#include <clc/workitem/get_work_dim.h>
#include <clc/workitem/get_global_offset.h>

/* 6.11.2 Math Functions */
#include <clc/math/acos.h>
#include <clc/math/acosh.h>
#include <clc/math/acospi.h>
#include <clc/math/asin.h>
#include <clc/math/asinh.h>
#include <clc/math/asinpi.h>
#include <clc/math/atan.h>
#include <clc/math/atan2.h>
#include <clc/math/atan2pi.h>
#include <clc/math/atanh.h>
#include <clc/math/atanpi.h>
#include <clc/math/cbrt.h>
#include <clc/math/copysign.h>
#include <clc/math/cos.h>
#include <clc/math/cosh.h>
#include <clc/math/cospi.h>
#include <clc/math/ceil.h>
#include <clc/math/exp.h>
#include <clc/math/exp10.h>
#include <clc/math/exp2.h>
#include <clc/math/expm1.h>
#include <clc/math/fabs.h>
#include <clc/math/fdim.h>
#include <clc/math/floor.h>
#include <clc/math/fma.h>
#include <clc/math/fmax.h>
#include <clc/math/fmin.h>
#include <clc/math/fmod.h>
#include <clc/math/fract.h>
#include <clc/math/frexp.h>
#include <clc/math/half_divide.h>
#include <clc/math/half_recip.h>
#include <clc/math/half_sqrt.h>
#include <clc/math/half_rsqrt.h>
#include <clc/math/half-xxx.h>
#include <clc/math/hypot.h>
#include <clc/math/ilogb.h>
#include <clc/math/ldexp.h>
#include <clc/math/lgamma.h>
#include <clc/math/lgamma_r.h>
#include <clc/math/log.h>
#include <clc/math/log1p.h>
#include <clc/math/log2.h>
#include <clc/math/log10.h>
#include <clc/math/logb.h>
#include <clc/math/mad.h>
#include <clc/math/maxmag.h>
#include <clc/math/minmag.h>
#include <clc/math/modf.h>
#include <clc/math/nan.h>
#include <clc/math/nextafter.h>
#include <clc/math/pow.h>
#include <clc/math/pown.h>
#include <clc/math/powr.h>
#include <clc/math/remainder.h>
#include <clc/math/remquo.h>
#include <clc/math/rint.h>
#include <clc/math/round.h>
#include <clc/math/rootn.h>
#include <clc/math/sin.h>
#include <clc/math/sinh.h>
#include <clc/math/sinpi.h>
#include <clc/math/sincos.h>
#include <clc/math/sqrt.h>
#include <clc/math/trunc.h>
#include <clc/math/native_cos.h>
#include <clc/math/native_divide.h>
#include <clc/math/native_exp.h>
#include <clc/math/native_exp2.h>
#include <clc/math/native_log.h>
#include <clc/math/native_log2.h>
#include <clc/math/native_powr.h>
#include <clc/math/native_sin.h>
#include <clc/math/native_sqrt.h>
#include <clc/math/rsqrt.h>
#include <clc/math/tan.h>
#include <clc/math/tanh.h>
#include <clc/math/tanpi.h>

/* 6.11.2.1 Floating-point macros */
#include <clc/float/definitions.h>
#include <clc/mathconstants.h>

/* 6.11.3 Integer Functions */
#include <clc/integer/abs.h>
#include <clc/integer/abs_diff.h>
#include <clc/integer/add_sat.h>
#include <clc/integer/clz.h>
#include <clc/integer/hadd.h>
#include <clc/integer/mad24.h>
#include <clc/integer/mad_hi.h>
#include <clc/integer/mad_sat.h>
#include <clc/integer/mul24.h>
#include <clc/integer/mul_hi.h>
#include <clc/integer/popcount.h>
#include <clc/integer/rhadd.h>
#include <clc/integer/rotate.h>
#include <clc/integer/sub_sat.h>
#include <clc/integer/upsample.h>

/* 6.11.3 Integer Definitions */
#include <clc/integer/definitions.h>

/* 6.11.2 and 6.11.3 Shared Integer/Math Functions */
#include <clc/shared/clamp.h>
#include <clc/shared/max.h>
#include <clc/shared/min.h>
#include <clc/shared/vload.h>
#include <clc/shared/vload_half.h>
#include <clc/shared/vstore.h>

/* 6.11.4 Common Functions */
#include <clc/common/degrees.h>
#include <clc/common/mix.h>
#include <clc/common/sign.h>
#include <clc/common/smoothstep.h>
#include <clc/common/step.h>
#include <clc/common/radians.h>

/* 6.11.5 Geometric Functions */
#include <clc/geometric/cross.h>
#include <clc/geometric/distance.h>
#include <clc/geometric/fast_distance.h>
#include <clc/geometric/dot.h>
#include <clc/geometric/length.h>
#include <clc/geometric/fast_length.h>
#include <clc/geometric/normalize.h>
#include <clc/geometric/fast_normalize.h>

/* 6.11.6 Relational Functions */
#include <clc/relational/any.h>
#include <clc/relational/all.h>
#include <clc/relational/bitselect.h>
#include <clc/relational/isfinite.h>
#include <clc/relational/isinf.h>
#include <clc/relational/is-xxx.h>
#include <clc/relational/isnan.h>
#include <clc/relational/isnormal.h>
#include <clc/relational/select.h>
#include <clc/relational/signbit.h>


/* 6.11.8 Synchronization Functions */
#include <clc/synchronization/cl_mem_fence_flags.h>
#include <clc/synchronization/barrier.h>

/* 6.11.11 Atomic Functions */
#include <clc/atomic/atomic_add.h>
#include <clc/atomic/atomic_dec.h>
#include <clc/atomic/atomic_inc.h>
#include <clc/atomic/atomic_sub.h>
#include <clc/atomic/atomic_cmpxchg.h>
#include <clc/atomic/atomic_xchg.h>
#include <clc/atomic/atomic_min.h>
#include <clc/atomic/atomic_max.h>
#include <clc/atomic/atomic_and.h>
#include <clc/atomic/atomic_or.h>
#include <clc/atomic/atomic_xor.h>

/* 6.12.10 Async Copy Functions */
#include <clc/async/async_wg_copy.h>
#include <clc/async/async_wg_strided_copy.h>
#include <clc/async/wait_group_events.h>
#include <clc/async/prefetch.h>

/* 6.12.12 Miscellaneous Vector Functions */
#include <clc/miscellaneous/shuffle.h>
#include <clc/miscellaneous/shuffle2.h>

/* [OpenCL 1.2] 6.12.13 Printf Functions */
#include <clc/charm/printf.h>
/* cl_khr_global_int32_base_atomics Extension Functions */
#include <clc/cl_khr_global_int32_base_atomics/atom_add.h>
#include <clc/cl_khr_global_int32_base_atomics/atom_dec.h>
#include <clc/cl_khr_global_int32_base_atomics/atom_inc.h>
#include <clc/cl_khr_global_int32_base_atomics/atom_sub.h>
#include <clc/cl_khr_global_int32_base_atomics/atom_xchg.h>
#include <clc/cl_khr_global_int32_base_atomics/atom_cmpxchg.h>

/* cl_khr_local_int32_base_atomics Extension Functions */
#include <clc/cl_khr_local_int32_base_atomics/atom_add.h>
#include <clc/cl_khr_local_int32_base_atomics/atom_dec.h>
#include <clc/cl_khr_local_int32_base_atomics/atom_inc.h>
#include <clc/cl_khr_local_int32_base_atomics/atom_sub.h>
#include <clc/cl_khr_local_int32_base_atomics/atom_xchg.h>
#include <clc/cl_khr_local_int32_base_atomics/atom_cmpxchg.h>

/* cl_khr_global_int32_extended_atomics Extension Functions */
#include <clc/cl_khr_global_int32_extended_atomics/atom_min.h>
#include <clc/cl_khr_global_int32_extended_atomics/atom_max.h>
#include <clc/cl_khr_global_int32_extended_atomics/atom_and.h>
#include <clc/cl_khr_global_int32_extended_atomics/atom_or.h>
#include <clc/cl_khr_global_int32_extended_atomics/atom_xor.h>

/* cl_khr_local_int32_extended_atomics Extension Functions */
#include <clc/cl_khr_local_int32_extended_atomics/atom_min.h>
#include <clc/cl_khr_local_int32_extended_atomics/atom_max.h>
#include <clc/cl_khr_local_int32_extended_atomics/atom_and.h>
#include <clc/cl_khr_local_int32_extended_atomics/atom_or.h>
#include <clc/cl_khr_local_int32_extended_atomics/atom_xor.h>

/* 6.12.14 Image Read and Write Functions */
#include <clc/clcimageformatdesc.h>
#include <clc/image/read_imagef.h>
#include <clc/image/read_imagei.h>
#include <clc/image/read_imageui.h>
#include <clc/image/write_imagef.h>
#include <clc/image/write_imagei.h>
#include <clc/image/write_imageui.h>
/*// 6.12.14.5 Built-in Image Query Functions */
#include <clc/image/get_image_width.h>
#include <clc/image/get_image_height.h>
#include <clc/image/get_image_depth.h>
#include <clc/image/get_image_channel_data_type.h>
#include <clc/image/get_image_channel_order.h>
#include <clc/image/get_image_dim.h>
#include <clc/image/get_image_array_size.h>

/* libclc internal defintions */
#ifdef __CLC_INTERNAL
#include <math/clc_nextafter.h>
#endif

#pragma OPENCL EXTENSION all : disable
