#pragma OPENCL EXTENSION cl_khr_fp64 : enable

#include <stdarg.h>

struct formatAttribute {
 char specifier[32];
 int  field_width;
 char dot;
 int precision;
 char ch;
 int type_length;
};
#include <clc/clctypes.h>
#include <clc/clcfunc.h>

void* malloc (int size);
void free (void* ptr);
int snprintf(char* restrict str, int size, const __constant char* restrict fmt, ...);

#define IMPL_SCALE_PRINTF(TYPE) \
_CLC_OVERLOAD void implPrintf(TYPE val, int vecSize, struct formatAttribute attr) { \
 \
 int i = 0; \
  char* outFmt = (char*) malloc(sizeof(char)*256); \
  if (attr.dot == '.') \
   snprintf(outFmt, sizeof(char)*16, "%%%s%d%c%d%c", attr.specifier, attr.field_width, attr.dot, attr.precision, attr.ch); \
  else \
   snprintf(outFmt, sizeof(char)*16, "%%%s%d%c", attr.specifier, attr.field_width, attr.ch); \
  printf(outFmt, val); \
  free(outFmt); \
}

#define IMPL_PRINTF(TYPE, SIZE) \
_CLC_OVERLOAD void implPrintf(TYPE##SIZE val, int vecSize, struct formatAttribute attr) { \
 \
 int i = 0; \
 char lenFormat[3]; \
 lenFormat[0] = '\0'; \
 if (2 == attr.type_length) \
  strcpy(lenFormat, "h"); \
 else if (1 == attr.type_length) \
  strcpy(lenFormat, "hh");\
 char* outFmt = (char*) malloc(sizeof(char)*256); \
 for (i=0; i<vecSize; i++){ \
  if (attr.dot == '.') \
   snprintf(outFmt, sizeof(char)*16, "%%%s%d%c%d%s%c", attr.specifier, attr.field_width, attr.dot, attr.precision, lenFormat, attr.ch); \
  else \
   snprintf(outFmt, sizeof(char)*16, "%%%s%d%s%c", attr.specifier, attr.field_width, lenFormat, attr.ch); \
  printf(outFmt, (TYPE)val[i]); \
  if (i < (vecSize-1) ) { \
   char t[2] = ","; \
   printf(t); \
  } \
 } \
 free(outFmt); \
}

IMPL_SCALE_PRINTF(float);
IMPL_PRINTF(float, 2);
IMPL_PRINTF(float, 3);
IMPL_PRINTF(float, 4);
IMPL_PRINTF(float, 8);
IMPL_PRINTF(float, 16);


IMPL_SCALE_PRINTF(int);
IMPL_PRINTF(int, 2);
IMPL_PRINTF(int, 3);
IMPL_PRINTF(int, 4);
IMPL_PRINTF(int, 8);
IMPL_PRINTF(int, 16);

IMPL_SCALE_PRINTF(uint);
IMPL_PRINTF(uint, 2);
IMPL_PRINTF(uint, 3);
IMPL_PRINTF(uint, 4);
IMPL_PRINTF(uint, 8);
IMPL_PRINTF(uint, 16);

IMPL_SCALE_PRINTF(char);
IMPL_PRINTF(char, 2);
IMPL_PRINTF(char, 3);
IMPL_PRINTF(char, 4);
IMPL_PRINTF(char, 8);
IMPL_PRINTF(char, 16);

IMPL_SCALE_PRINTF(uchar);
IMPL_PRINTF(uchar, 2);
IMPL_PRINTF(uchar, 3);
IMPL_PRINTF(uchar, 4);
IMPL_PRINTF(uchar, 8);
IMPL_PRINTF(uchar, 16);

IMPL_SCALE_PRINTF(short);
IMPL_PRINTF(short, 2);
IMPL_PRINTF(short, 3);
IMPL_PRINTF(short, 4);
IMPL_PRINTF(short, 8);
IMPL_PRINTF(short, 16);

IMPL_SCALE_PRINTF(ushort);
IMPL_PRINTF(ushort, 2);
IMPL_PRINTF(ushort, 3);
IMPL_PRINTF(ushort, 4);
IMPL_PRINTF(ushort, 8);
IMPL_PRINTF(ushort, 16);

IMPL_SCALE_PRINTF(long);
IMPL_PRINTF(long, 2);
IMPL_PRINTF(long, 3);
IMPL_PRINTF(long, 4);
IMPL_PRINTF(long, 8);
IMPL_PRINTF(long, 16);

IMPL_SCALE_PRINTF(ulong);
IMPL_PRINTF(ulong, 2);
IMPL_PRINTF(ulong, 3);
IMPL_PRINTF(ulong, 4);
IMPL_PRINTF(ulong, 8);
IMPL_PRINTF(ulong, 16);

