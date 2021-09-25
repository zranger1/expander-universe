#version 120

uniform float time;
uniform vec2 resolution;

#define PHASE_OFFSET (-3.1415926/2.)

// GLSL version of a part of Jeff Vyduna's Pixelblaze tutorial pattern,
// "An Intro to Pixelblaze code" 
// May be the world's first 1D LED strip shader.  It moves three colored stripes back and forth
// on the display.

void main( void ) {
    vec3 col = vec3(0);
    
    int x = int(gl_FragCoord.x);
    int index = int(floor(resolution.x * fract(time * 0.5)));    
 
    if (x == index) {  // red
       col = vec3(1.0,0.0,0.0);
    } else if (x == (index - 4.)) {  // green
       col = vec3(0.0,1.0,0.0);
    } else if (x == (index - 8.)) {  // blue
       col = vec3(0.0,0.0,1.0);
    } else {
      vec3 col = vec3(0.0);    
    }

    gl_FragColor = vec4(col,1.0);
}