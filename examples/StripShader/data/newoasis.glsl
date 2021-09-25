#version 120

uniform float time;
uniform vec2 resolution;

// Oasis in GLSL
// 9/23/31 ZRanger1

// colorspace conversion functions from shadertoy.com.  Not sure where they
// originated - they're in lots of shaders
vec3 rgb2hsv(vec3 c) {
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

// wave - here, it is == sin, but w/input range 0..1 instead of 0..TWO_PI
float wave(float n) {
  return sin(6.282 * fract(n));
}

// generate triangle waveform over the input range 0..1
float triangle(float n) {
  return  2 * (0.5 - abs(fract(n) - 0.5));
}

void main( void ) {
   vec3 col = vec3(0.);
   float speed = 1.45;
   float waveLength = 0.5;


  float k1 = waveLength * 11.; 
  float k2 = waveLength * 15.; 
  float k3 = waveLength * 7.;  
  float k4 = waveLength * 5.;    
  
  float t1 = time * speed * .16;
  float t2 = time *speed * .1;
  float t3 = time *speed * .14;
  float t4 = time * speed * .11;
  
  float x = gl_FragCoord.x / resolution.x;

  float v =  wave((k1 * x) - t1);
  v += wave((k2 * x) + t2);
  v += wave((k3 * x) + t3);
  v += wave((k4 * x) - t4);
  v = v / 4.;

  float hue = 0.6667 - (0.1 * triangle(x+v));

  float sat = clamp(1.333 - v,0.,1.);
  col = hsv2rgb(vec3(hue, sat, max(0.045,v + 0.6)));

  gl_FragColor = vec4(col,1.0);
}