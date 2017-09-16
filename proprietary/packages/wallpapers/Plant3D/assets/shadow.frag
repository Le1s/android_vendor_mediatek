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
 * A fragment shader for shadow effect
 *
 */

precision mediump float;

/* Material uniforms */
uniform sampler2D u_m_diffuseTexture;

varying mediump vec2 v_texCoord0;
varying mediump float v_alpha;

void main()
{
  gl_FragColor = texture2D( u_m_diffuseTexture, v_texCoord0 ) * v_alpha;
}
