#ifndef INIT_COORD_H
#define INIT_COORD_H

#define INIT_COORDint(src, dest) { \
  dest.x = src;                    \
  dest.y = 0 ;                     \
  dest.z = 0;                      \
  dest.w = 0;                      \
}

#define INIT_COORDint2(src, dest) { \
  dest.x = src.x;                   \
  dest.y = src.y;                   \
  dest.z = 0;                       \
  dest.w = 0;                       \
}

#define INIT_COORDint4(src, dest) { \
  dest.x = src.x;                   \
  dest.y = src.y;                   \
  dest.z = src.z;                   \
  dest.w = 0;                       \
}

#define INIT_COORDfloat(src, dest) { \
  dest.x = src;                      \
  dest.y = 0.0f;                     \
  dest.z = 0.0f;                     \
  dest.w = 0.0f;                     \
}

#define INIT_COORDfloat2(src, dest) { \
  dest.x = src.x;                     \
  dest.y = src.y;                     \
  dest.z = 0.0f;                      \
  dest.w = 0.0f;                      \
}

#define INIT_COORDfloat4(src, dest) { \
  dest.x = src.x;                     \
  dest.y = src.y;                     \
  dest.z = src.z;                     \
  dest.w = 0.0f;                      \
}

#endif
