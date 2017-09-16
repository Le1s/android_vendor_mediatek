/**************************************************************************
 *
 * Copyright (c) 2013 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 ***************************************************************************/
/** \file
 * A vertex shader for shadow effect.
 *
 */

/* Transformation uniforms */
uniform highp mat4 u_t_model;               // Model to world space transform
uniform highp mat4 u_t_viewProjection;      // World to projection space transform
uniform vec3 u_shadowPlaneNormal;
uniform float u_shadowPlaneDistance;

#define MAX_LIGHTS 5
uniform vec4 u_l_position[MAX_LIGHTS];  // Light position in world space

attribute vec4 a_position;    // Vertex position (model space)
attribute vec3 a_normal;      // Vertex normal (model space)
attribute vec2 a_uv0;         // Texture coordinate

varying mediump vec2 v_texCoord0;
varying mediump float v_alpha;

void main()
{
  vec4 world = u_t_model * a_position;

  // Intersect light to vertex with plane
  vec3 light_to_leaf = ( world - u_l_position[0] ).xyz;

  float nDotA = dot( u_shadowPlaneNormal, u_l_position[0].xyz );
  float nDotBA = dot( u_shadowPlaneNormal, light_to_leaf );

  vec3 intersect = u_l_position[0].xyz + (((u_shadowPlaneDistance - nDotA)/nDotBA) * light_to_leaf);

  gl_Position = u_t_viewProjection * vec4( intersect, 1.0 );

  vec3 normal = normalize( ( u_t_model * vec4( a_normal, 0.0 ) ).xyz );

  v_texCoord0 = a_uv0;
  //v_alpha = abs( dot( normalize( light_to_leaf ), normal ) ) * 0.75;
  v_alpha = smoothstep( 0.0, 1.0, abs( dot( normalize( light_to_leaf ), normal ) ) ) * 0.75;
}
