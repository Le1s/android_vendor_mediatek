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
 * Fragment shader used for per vertex lighting of glofile contents exported
 * from 3ds Max.
 */

/*
 * Macro definitions should be preprended to this source to provide support
 * for different features as follows:
 *
 * DIFF - diffuse lighting and material support
 * SPEC - specular lighting and material support
 * AMBI - ambient lighting and material support
 * EMIS - emissive material support
 * REFL - spherical environment mapping support
 *
 * Macros can be predefined using the ShaderProgramLoader class by appending
 * the names you need to be predefined seperated by the '$' character.
 *
 * e.g. calling:
 *
 *   shaderProgramLoader->load( cache, "max_pvl.sp$DIFF$REFL" );
 *
 * will prepend the vertex shader (this file) and fragment shader with:
 *
 * #define DIFF
 * #define REFL
 *
 * The glo file loader will append these strings to the shader program name
 * used for each appearance depending on the lighting/material requiements.
 */

precision mediump float;

#define DIFF
#define SPEC
#define AMBI
#define SKIN
#define INVSQROOT

/* This is just below 1/256, which would be zero when quantized to an 8-bit
 * value. */
#define FOG_OPAQUE_EPSILON 0.0039

/* Material uniforms */
uniform sampler2D u_m_diffuseTexture;    // Diffuse texture including alpha
uniform sampler2D u_m_specularTexture;   // Specular texture (alpha ignored)
uniform sampler2D u_m_ambientTexture;    // Ambient texture (alpha ignored)
uniform sampler2D u_m_emissiveTexture;   // Emissive texture (alpha ignored)

#if defined( CUBE )
uniform samplerCube u_m_reflectionTexture; // Cube-mapped environment texture
#else
uniform sampler2D u_m_reflectionTexture; // Sphere-mapped environment texture
                                         // (alpha ignored)
#endif

uniform vec4 u_m_specularColour;  // Specular colour (alpha ignored)
uniform float u_m_specularLevel;

uniform vec4 u_m_emissiveColour;  // Emissive material colour

/* Scene-wide uniforms */
uniform vec4 u_fogColour;

uniform float u_animationTime;
uniform bool u_charging;


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
varying mediump vec3 v_vertexToCamera;
varying mediump vec3 v_normal;
#else
// Texture coordinate for sphere-mapped reflections
varying lowp vec2 v_sphereCoord;
#endif

#endif

varying float v_fogTransparency;

void main()
{
  // Return early if fog is completely opaque.
  if (v_fogTransparency < FOG_OPAQUE_EPSILON)
  {
    gl_FragColor = u_fogColour;
    return;
  }

  // Look up value from diffuse texture map
  lowp vec4 diffuseTex = texture2D( u_m_diffuseTexture, v_texCoord );

  // Multiply by diffuse colour
  lowp vec4 diffuse = diffuseTex * v_diffuseColour;

  // Look up value from specular texture map
#if defined( SPEC ) || defined( REFL )
  lowp vec4 specularTex = texture2D( u_m_specularTexture, v_texCoord );
#endif

#if defined( REFL ) && defined( CUBE )
  // Cube map just needs un-normalized reflection vector.
  // We flip the z-axis, as cube map look-up is left-handed when we are viewing
  // as an observer inside the cube.
  mediump vec3 reflectWorld = -reflect( v_vertexToCamera, v_normal );
  reflectWorld.z = -reflectWorld.z;
#endif

  // Colour calculated from sum of:
  // diffuse colour as calculated above,
  // specular colour (specular texture X specular lighting component),
  // ambient colour (ambient texture X ambient lighting X diffuse texture),
  // emmisive colour (emissive texture X emissive colour),
  // reflection (specular texture X environment map texture look-up)
  vec4 colour = vec4( 0., 0., 0., 0. )
#if defined( DIFF )
    + diffuse
#endif
#if defined( SPEC )
    + specularTex * v_specularColour
#endif
#if defined( AMBI )
    + texture2D( u_m_ambientTexture, v_texCoord ) * v_ambientColour * diffuseTex
#endif
#if defined( EMIS )
    + texture2D( u_m_emissiveTexture, v_texCoord ) * u_m_emissiveColour
#endif
#if defined( REFL )
    + specularTex * u_m_specularColour *

#if defined( CUBE )
      textureCube( u_m_reflectionTexture, reflectWorld )
#else
      texture2D( u_m_reflectionTexture, v_sphereCoord )
#endif // defined( CUBE )

#endif // defined( REFL )
    ;

  // Use diffuse component alpha
  colour.a = diffuse.a;

  // If there is any fog for this pixel, blend fog with rest of scene.
  if (v_fogTransparency < 1.0)
  {
    colour.rgb = mix(u_fogColour.rgb, colour.rgb, v_fogTransparency);
  }

  if( u_charging )
  {
      float pos = (1.0 - v_texCoord.y ) + texture2D( u_m_emissiveTexture, v_texCoord * 0.5 ).r * 0.04;
      float amt = smoothstep( 0.02, 0.0, abs( pos - fract( u_animationTime ) ) );
      //colour.rgb = colour.rgb + amt * vec3(0.5, 0.5, 1.0);
      //colour.rgb = colour.rgb + amt * vec3(0.5, 0.5, 4.0);
      colour.rgb = colour.rgb * (1.0 + amt * 2.0);
  }

  gl_FragColor = colour;
}
