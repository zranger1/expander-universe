#version 120

// original https://www.shadertoy.com/view/NllGRX

uniform float time;
uniform vec2 mouse;
uniform vec2 resolution;

/////////////////////////////
// ORIGINALLY - BRADY'S VOLUMETRIC FIRE //
/////////////////////////////

// thanks brady, great shader btw.

// adapted for 1D LED strips - ZRanger1.  Volumetric Raymarched Fire -- this is the most excessive
// overkill 1D fire effect possible!


float noise( vec3 P )
{
    vec3 Pi = floor(P);
    vec3 Pf = P - Pi;
    vec3 Pf_min1 = Pf - 1.0;
    Pi.xyz = Pi.xyz - floor(Pi.xyz * ( 1.0 / 69.0 )) * 69.0;
    vec3 Pi_inc1 = step( Pi, vec3( 69.0 - 1.5 ) ) * ( Pi + 1.0 );
    vec4 Pt = vec4( Pi.xy, Pi_inc1.xy ) + vec2( 50.0, 161.0 ).xyxy;
    Pt *= Pt;
    Pt = Pt.xzxz * Pt.yyww;
    vec2 hash_mod = vec2( 1.0 / ( 635.298681 + vec2( Pi.z, Pi_inc1.z ) * 48.500388 ) );
    vec4 hash_lowz = fract( Pt * hash_mod.xxxx );
    vec4 hash_highz = fract( Pt * hash_mod.yyyy );
    vec3 blend = Pf * Pf * Pf * (Pf * (Pf * 6.0 - 15.0) + 10.0);
    vec4 res0 = mix( hash_lowz, hash_highz, blend.z );
    vec4 blend2 = vec4( blend.xy, vec2( 1.0 - blend.xy ) );
    return dot( res0, blend2.zxzx * blend2.wwyy );
}

float fnoise(vec3 p, float time)
{
    float f = 0.0;
    p = p - vec3(0.0, 1.0, 0.0) * .5 * time;
    p = p * 3.0;
    f += 0.50000 * noise(p); p = 2.0 * p;
    f += 0.25000 * noise(p); p = 2.0 * p;
    f += 0.12500 * noise(p); p = 2.0 * p;
    f += 0.06250 * noise(p); p = 2.0 * p;
    f += 0.03125 * noise(p); p = 2.0 * p;
    f += 0.015625 * noise(p);
    
    return f;
}

float model( in vec3 p )
{
    p.y *= 0.75;
    p.xz *= 0.5;

    float sphere = length(p)-1.05;
    float fire = fnoise(p*4.5, time*3.);
    
    float res = sphere + fire * 0.5f;
    return res * 0.633f;
}

float raymarch(in vec3 ro, in vec3 rd)
{
    float dist = 0.;
    for(int i = 0; i < 40; i++)
    {
        float m = model(ro+rd*dist);
        dist += m;
        
        if(m < .001) return dist;
        //else if(dist > 2.) break;
    }
    return -1.;
}

vec3 background(in vec2 p)
{
    return vec3(0.);
}

vec3 blackbody(float t){
    // http://en.wikipedia.org/wiki/Planckian_locus

    vec4 vx = vec4( -0.2661239e9, -0.2343580e6, 0.8776956e3, 0.179910   );
    vec4 vy = vec4( -1.1063814,   -1.34811020,  2.18555832, -0.20219683 );
    //vec4 vy = vec4(-0.9549476,-1.37418593,2.09137015,-0.16748867); //>2222K
    float it = 1. / t;
    float it2= it * it;
    float x = dot( vx, vec4( it*it2, it2, it, 1. ) );
    float x2 = x * x;
    float y = dot( vy, vec4( x*x2, x2, x, 1. ) );
    
    // http://www.brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html
    mat3 xyzToSrgb = mat3(
         3.2404542,-1.5371385,-0.4985314,
        -0.9692660, 1.8760108, 0.0415560,
         0.0556434,-0.2040259, 1.0572252
    );

    vec3 srgb = vec3( x/y, 1., (1.-x-y)/y ) * xyzToSrgb;

    return max( srgb, 0. );
}

vec3 volume(in vec3 p, in vec3 rd, in vec3 ld, in vec2 sp)
{
    vec3 op = p;
    float trans = 1.0;
    float td = 0.0;
    float emit = 0.0;
    float steps = 30.; // increase to smooth
    
    // march
    for(float i = 0.; i < steps; i++)
    {
        float m = model(p);
        p += rd*.03;
        
        float dens = exp2(-m * 0.85f) * 0.85f;
        td += dens * trans;
        trans *= dens;
        
        if(trans < 0.001f)
        {
            break;
        }
    }
    
    vec3 extreme;
    extreme = mix(vec3(0), blackbody(400.0f * td * (1.0f - trans)), trans);
    
    extreme = mix(extreme, blackbody(450.0f * td), trans);
    
    extreme += extreme * extreme;
    
    return extreme;
}

void main(void)
{
    vec2 p = (gl_FragCoord.xy - .5*resolution.xy)/resolution.xy;
    
    float rs = .5;
    vec3 ro = vec3(cos(mouse.x*resolution.xy.x/100.), 1., sin(mouse.x*resolution.xy.x/100.))*1.35;
    vec3 ta = vec3(0., 0.1, .0);                
    
    vec3 w = normalize (ta-ro);
    vec3 u = normalize (cross (w, vec3(0., 1., 0.)));
    vec3 v = normalize (cross (u, w));
    mat3 mat = mat3(u, v, w);
    vec3 rd = normalize (mat*vec3(p.xy,1.));
    
    float dist = raymarch(ro, rd);
    vec3 ld = vec3(-1., 1., 0.);
    vec3 col = dist > 0. ? volume(ro+rd*dist, rd, ld, p) : background(p);
    
    gl_FragColor = vec4(col ,1.0);
}
