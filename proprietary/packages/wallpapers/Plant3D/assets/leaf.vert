/**************************************************************************
 *
 * Copyright (c) 2012 MediaTek Inc. All Rights Reserved.
 * --------------------
 * This software is protected by copyright and the information contained
 * herein is confidential. The software may not be copied and the information
 * contained herein may not be used or disclosed except with the written
 * permission of MediaTek Inc.
 *
 ***************************************************************************/
/** \file
 * Vertex shader used for per vertex lighting of glofile contents exported
 * from 3ds Max.
 */

precision mediump float;

#define DIFF
#define SPEC
#define AMBI
#define SKIN
#define INVSQROOT

/* These should match the maximum values in the renderer */
#define MAX_LIGHTS 5
#define MAX_JOINTS 20


/* Transformation uniforms */
uniform highp mat4 u_t_model;               // Model to world space transform
uniform highp mat4 u_t_viewProjection;      // World to projection space transform
uniform highp mat4 u_t_modelViewProjection; // Model to projection space transform
uniform highp mat3 u_t_normal;              // Model to world space normal transform
uniform vec4 u_cameraPosition;              // Camera position in world space


/* Lighting uniforms */
uniform int u_l_count;                  // Number of lights
uniform vec4 u_l_position[MAX_LIGHTS];  // Light position in world space
uniform vec4 u_l_ambient[MAX_LIGHTS];   // Ambient colour of light
uniform vec4 u_l_diffuse[MAX_LIGHTS];   // Diffuse colour of light
uniform vec4 u_l_specular[MAX_LIGHTS];  // Specular colour of light
uniform float u_l_spot_inner_dot[MAX_LIGHTS]; // cos of inner spot half-angle
uniform float u_l_spot_outer_dot[MAX_LIGHTS]; // cos of outer spot half-angle
uniform vec3 u_l_spot_direction[MAX_LIGHTS]; // direction of spot
uniform float u_l_attenuation_near[MAX_LIGHTS];  // Distance before light attenuates

// 1 / ( attFar - attNear ) measured in world space units
uniform float u_l_attenuation_reciprocal[MAX_LIGHTS];

/* Joint uniforms */
uniform int u_j_count;                      // Number of joints per vertex
uniform highp mat4 u_j_world[MAX_JOINTS];   // Joint world space transforms

/* Material uniforms */
uniform vec4 u_m_ambientColour;   // Ambient colour (alpha ignored)
uniform vec4 u_m_diffuseColour;   // Diffuse colour with alpha.
uniform vec4 u_m_specularColour;  // Specular colour (alpha ignored)
uniform float u_m_shininess;      // Exponent used in specular calculation
uniform float u_m_specularLevel;  // Specular colour scale factor
uniform float u_m_selfIllumination; // Emissive colour scale factor
uniform float u_m_opacity;        // Opacity value

/* Scene-wide uniforms */
uniform float u_fogDensity;

/* Vertex attributes */
attribute vec4 a_position;    // Position in model space
attribute vec3 a_normal;      // Normal in model space
attribute vec2 a_uv0;         // Texture coordinate


attribute float a_jointIndex0;   // Index of attached joint
attribute float a_jointWeight0;  // Attachment weight of joint
attribute float a_jointIndex1;   // Index of attached joint
attribute float a_jointWeight1;  // Attachment weight of joint
attribute float a_jointIndex2;   // Index of attached joint
attribute float a_jointWeight2;  // Attachment weight of joint
attribute float a_jointIndex3;   // Index of attached joint
attribute float a_jointWeight3;  // Attachment weight of joint


/* Varyings */
// Apparent colour of vertex taking into account ambient colour of light,
// distance from light and ambient colour of material
#if defined( AMBI )
varying lowp vec4 v_ambientColour;
#endif

// Apparent colour of vertex taking into account diffuse colour of light,
// distance from light, diffuse colour of material and angle of incidence of
// light
varying lowp vec4 v_diffuseColour;

// Apparent colour of vertex taking into account specular colour of light,
// distance from light, specular colour and shininess of material, angle of
// incidence of light and view vector
#if defined( SPEC )
varying lowp vec4 v_specularColour;
#endif

// Texture coordinate copied from vertex attribute
varying lowp vec2 v_texCoord;

#if defined( REFL )

#if defined( CUBE )
// Cube mapping requires the vertex-camera vector and surface normal, so that
// the reflection vector can be calculated.
varying mediump vec3 v_vertexToCamera;
varying mediump vec3 v_normal;
#else
// Texture coordinate for sphere-mapped reflections
varying lowp vec2 v_sphereCoord;
#endif

#endif

// Used to blend final colour and fog.
varying float v_fogTransparency;


void main()
{
  vec4 vertexPosition;
  vec3 normal;

  /* The skinning calculations performed here are duplicated in
   * max_pvl.vert, max_ppl.vert and gen_velocity.vert
   * Any changes made to one file should be made to all files.
   */
#ifdef SKIN
  // Perform a weighted sum of each joint transformation
  if (u_j_count > 0)
  {
    highp mat4 worldTransform = a_jointWeight0 * u_j_world[int(a_jointIndex0)];

    if (u_j_count > 1 && a_jointWeight1 > 0.0)
    {
      worldTransform += a_jointWeight1 * u_j_world[int(a_jointIndex1)];

      if (u_j_count > 2 && a_jointWeight2 > 0.0)
      {
        worldTransform += a_jointWeight2 * u_j_world[int(a_jointIndex2)];

        if (u_j_count > 3 && a_jointWeight3 > 0.0)
        {
          worldTransform += a_jointWeight3 * u_j_world[int(a_jointIndex3)];
        }
      }
    }

    // Apply skinning transformation to vertex data
    vertexPosition = worldTransform * a_position;
    gl_Position = u_t_viewProjection * vertexPosition;
    normal = normalize((worldTransform * vec4(a_normal, 0.0)).xyz);
  }
  else
#endif
  {
    // Final position is model position multiplied by model view projection matrix
    gl_Position = u_t_modelViewProjection * a_position;

    // Convert normal to world space and normalize
    normal = normalize( u_t_normal * a_normal );

    // Calculate vertex position in world space for lighting calulations
    vertexPosition.xyz = (u_t_model * a_position).xyz;
  }

  // Calculate fog density up front, as we can avoid lighting calculations if
  // the fog is completely opaque.
  if (u_fogDensity > 0.0)
  {
    // Calculate per-vertex fog
    float fogDepth = length(u_cameraPosition.xyz - vertexPosition.xyz);

    // Emulates the old "exp2" fixed-pipeline fog.
    v_fogTransparency = exp(-u_fogDensity * u_fogDensity * fogDepth * fogDepth);
    v_fogTransparency = clamp(v_fogTransparency, 0.0, 1.0);
  }
  else
  {
    v_fogTransparency = 1.0;
  }

  // Pass through texture coordinate
  v_texCoord = a_uv0;

  // Calculate vector from vertex to camera in world space
#if defined( SPEC ) || defined( REFL )
  vec3 vertexToCamera = u_cameraPosition.xyz - vertexPosition.xyz;
  vec3 vertexToCameraNorm = normalize( vertexToCamera );
#endif

  // Initialize colour components before accumulation for each light
  v_diffuseColour = vec4( 0., 0., 0., 1. );
#if defined( AMBI )
  v_ambientColour = vec4( 0., 0., 0., 1. );
#endif
#if defined( SPEC )
  v_specularColour = vec4( 0., 0., 0., 1. );
#endif

  // Calculate colour components for each light
#if defined( DIFF ) || defined( AMBI ) || defined( SPEC )
  for (int i = 0; i < MAX_LIGHTS; ++i)
  {
    // Ensure number of lights does not exceed array lengths
    if (i < u_l_count)
    {
      float lightLevel; // Result of attenuation due to distance and/or spot

      // Calculate vector from vertex to light in world space
      vec3 vertexToLight = u_l_position[i].xyz;

      // This assignment is to work around a possible compiler bug: testing the
      // value of u_l_position[i].w directly in the condition below results in
      // the condition never being met and u_l_position being treated as the
      // direction of the light.
      float w = u_l_position[i].w;

      if( w != 0.0 ) // Point or spot light
      {
        vertexToLight -= vertexPosition.xyz;
        // Calculate attenuation due to distance from light
        // This approximates to that used 3ds Max
        float distance = length( vertexToLight );
        float distance_x_r = (distance - u_l_attenuation_near[i]) *
                             u_l_attenuation_reciprocal[i];
#ifdef INVSQROOT // for 3ds Max
        lightLevel = ( distance_x_r > 1.0 ) ?
          0.0 : sqrt( 1.0 - distance_x_r );
#else // Generic and works for Blender
        lightLevel = ( distance_x_r > 1.0 ) ?
          0.0 : ( 1.0 - distance_x_r );
#endif
        vertexToLight = ( 1.0 / distance ) * vertexToLight;
        lightLevel = min( lightLevel, 1.0 );
      }
      else // Directional light (vertexToLight is constant)
      {
        lightLevel = 1.0;
      }

      // If lightLevel is positive then this light contributes
      if( lightLevel > 0.0 )
      {
#if defined( DIFF ) || defined( SPEC )
        // Calculate spot light fall-off
        if( u_l_spot_inner_dot[i] > -1.0 )
        {
          float dir_dot = dot( vertexToLight, u_l_spot_direction[i] );
          lightLevel *= smoothstep( u_l_spot_outer_dot[i],
                                    u_l_spot_inner_dot[i],
                                    dir_dot );
        }

        // Calculate diffuse lighting if the angle between the surface normal and
        // light ray is less than 90 degrees

#if defined( DIFF )
        float normalDotLight = abs( dot(normal, vertexToLight ) );
        //if( normalDotLight > 0.0 )
        //{
          v_diffuseColour += ( normalDotLight * lightLevel )
                             * u_l_diffuse[i];
        //}
#endif //defined( DIFF )

        // Calculate specular lighting using Blinn model.
        // Specular intensity peaks where the normal lies exactly halfway between
        // the light source and the eye
#if defined( SPEC )
        vec3 halfVector = vertexToCameraNorm + vertexToLight;
        float halfVectorLen = length( halfVector );
        if( halfVectorLen != 0.0 )
        {
          halfVector = halfVector * ( 1.0 / halfVectorLen );
          float normalDotHalf = dot( normal, halfVector );
          if( normalDotHalf > 0.0 )
          {
            v_specularColour += pow( normalDotHalf, u_m_shininess ) * lightLevel
                                * u_l_specular[i];
          }
        }
#endif //defined( SPEC )
#endif //defined( DIFF ) || defined( SPEC )

        // Simply add ambient component
#if defined( AMBI )
        v_ambientColour += lightLevel * u_l_ambient[i];
#endif
      }
    }
  }
#endif // defined( DIFF ) || defined( AMBI ) || defined( SPEC )

  // Multiply colour components already calculated by their corresponding
  // material colours
#if defined( DIFF )
  v_diffuseColour = u_m_diffuseColour * ( u_m_selfIllumination +
      ( 1.0 - u_m_selfIllumination ) * v_diffuseColour );
#endif
#if defined( SPEC )
  v_specularColour *= u_m_specularColour * u_m_specularLevel;
#endif
#if defined( AMBI )
  v_ambientColour *= u_m_ambientColour;
#endif

  // Use the diffuse colour alpha as the 'overall' alpha.
  v_diffuseColour.a = u_m_diffuseColour.a * u_m_opacity;


/* Calculate environment reflection. */
#if defined( REFL )

#if defined( CUBE )
  v_vertexToCamera = vertexToCamera;
  v_normal = normal;
#else

  // Calculate sphere map texture coordinates. This is done here to gain
  // performance at the cost of quality.

  // Calculate   m = 2 * sqrt( r.x^2 + r.y^2 + (r.z + 1)^2)
  // See http://www.reindelsoftware.com/Documents/Mapping/Mapping.html

  // Calculate reflection vector (view vector reflected off the surface) in
  // world space
  vec3 reflectCamera = -reflect( vertexToCameraNorm, normal );
  reflectCamera.z += 1.0;
  float m = 2.0 * length( reflectCamera );

  v_sphereCoord.x = reflectCamera.x / m + 0.5;
  v_sphereCoord.y = 1.0 - ( reflectCamera.y / m + 0.5 );

#endif //defined( CUBE )

#endif //defined( REFL )
}
