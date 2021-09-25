#version 120

// original https://www.shadertoy.com/view/wtXGDN

uniform float time;
uniform vec2 mouse;
uniform vec2 resolution;

// These aren't used much, are you planning to do something interesting?
vec2 spread = vec2(.3);
vec2 offset = vec2(0.);
float shapeSize = .1;
const float s = 1.;

const int MAX_SHAPES = 3;
vec3 shapeColors[MAX_SHAPES] = vec3[MAX_SHAPES] (  
  vec3(s, 0., 0.),
  vec3(0., s, 0.),
  vec3(0., 0., s)
);

vec2 motionFunction (float i) {
  float t = time * 2;

  return vec2(( cos(t * .31 + i * 3.) + cos(t * .11 + i * 14.) + cos(t * .78 + i * 30.) + cos(t * .55 + i * 10.)) / 2., 0.);
 //   (cos(t * .13 + i * 33.) + cos(t * .66 + i * 38.) + cos(t * .42 + i * 83.) + cos(t * .9 + i * 29.)) / 2.

}

void main(void) {
    vec2 pixel = (gl_FragCoord.xy - .5 * resolution.xy) / resolution.x;
    // Change include_background to 0 or 1 to try 2 different modes
    // I didn't use #define as I'm not sure if you are familiar with those
    int include_background = 0;
    
    // With include_background == 1 and a pure white background, you don't see the shapes,
    // but try other colours
    vec3 background = vec3(0);
    vec4 totalColor;
    
    if(include_background == 0)
        // Create a colour to add to mix with background later. Initialise to 0.
        totalColor = vec4(0.);
    else
        // Initialise with background colour
        totalColor = vec4(background, 0.);
    
    for (int i = 0; i != MAX_SHAPES; ++i) {
        vec2 shapeCenter = motionFunction(float(i)) * spread + offset;
        float ratio = smoothstep(0., 1., distance(shapeCenter, pixel) / shapeSize);
//      float ratio = clamp(distance(shapeCenter, pixel) / shapeSize, 0., 1.);

        // Additive colour
        totalColor += vec4(mix(shapeColors[i], vec3(0.), ratio), 1. - ratio);
    }

    totalColor = clamp(totalColor, 0., 1.);
    if(include_background == 0){
        // Mix the combined colour with the background
        // Similar to Photoshop - Linear Dodge (Add) ?
        totalColor.rgb = mix(background, totalColor.rgb, totalColor.a);    
    } 
    
    gl_FragColor = vec4(
        // Approximately convert to sRGB colourspace with sqrt()
        sqrt(totalColor.rgb),
        1.
    );
}
