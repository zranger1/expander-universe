#version 130

// original https://www.shadertoy.com/view/tsjfRw

uniform float time;
uniform vec2 mouse;
uniform vec2 resolution;

//antialising
#define AA_SAMPLES 4
//percentage of domains filled
#define DENSITY 0.3
#define CAMERA  8
 
//returns a vector pointing in the direction of the closest neighbouring cell
vec3 quadrant(vec3 p) {
    vec3 ap = abs(p);
    if (ap.x >= max(ap.y, ap.z)) return vec3(sign(p.x),0.,0.);
    if (ap.y >= max(ap.x, ap.z)) return vec3(0.,sign(p.y),0.);
    if (ap.z >= max(ap.x, ap.y)) return vec3(0.,0.,sign(p.z));
    return vec3(0);
}

float hash(float a, float b) {
    return fract(sin(a*1.2664745 + b*.9560333 + 3.) * 14958.5453);
}

bool domain_enabled(vec3 id) {
    //repeat random number along z axis so every active cell has at least one active neighbour
    id.z = floor(id.z/2.); 
    return hash(id.x, hash(id.y, id.z)) < DENSITY;
}

float linedist(vec3 p, vec3 a, vec3 b) {
    float k = dot(p-a,b-a)/dot(b-a,b-a);
    return distance(p, mix(a,b,clamp(k,0.,1.)));
}

float ball;
float scene(vec3 p) {
    float scale = 5.;
    vec3 id = floor(p/scale);
    p = (fract(p/scale)-.5)*scale;
    if (!domain_enabled(id)) {
        //return distance to sphere in adjacent domain
        p = abs(p);
        if (p.x > p.y) p.xy = p.yx;
        if (p.y > p.z) p.yz = p.zy;
        if (p.x > p.y) p.xy = p.yx;
        p.z -= scale;
        return length(p)-1.;
    }
    float dist = length(p)-1.;
    ball = dist;
    vec3 quad = quadrant(p);
    if (domain_enabled(id+quad)) {
        //add pipe
        dist = min(dist, linedist(p, vec3(0), quad*scale)-.2);
    }
    return dist;
}

vec3 norm(vec3 p) {
    mat3 k = mat3(p,p,p)-mat3(0.01);
    return normalize(scene(p) - vec3( scene(k[0]),scene(k[1]),scene(k[2]) ));
}

vec3 erot(vec3 p, vec3 ax, float ro) {
    return mix(dot(ax,p)*ax, p, cos(ro)) + sin(ro)*cross(ax,p);
}

vec3 srgb(float r, float g, float b) {
    return vec3(r*r,g*g,b*b);
}

float smoothstairs(float p, float scale) {
    p *= scale;
    p = smoothstep(0.9, 1., fract(p)) + floor(p);
    return p/scale;
}

vec3 pixel_color(vec2 uv) {
    vec3 cam = normalize(vec3(CAMERA,uv));
    vec3 init = vec3(time,0,0);

    float yrot = cos(time*.4)*.6;
    float zrot = sin(time*.4)*.6;
    cam = erot(cam, vec3(0,1,0), yrot);
    cam = erot(cam, vec3(0,0,1), zrot);
    
    vec3 p = init;
    bool hit = false;
    bool triggered = false;
    bool outline = false;
    bool type = false;
    float dist;
    //ray marching
    for (int i = 0; i < 150 && !hit; i++) {
        dist = scene(p);
        float outline_radius = 0.1*sqrt(distance(p,init))/3.;
        if (dist < outline_radius*.9 && !triggered) {
            triggered = true;
            type = dist == ball;
        }
        if (triggered) {
            float line = (outline_radius-dist);
            outline = line < dist || type != (dist == ball);
            dist = min(line, dist);
        }
        hit = dist*dist < 1e-6;
        p+=dist*cam;
        if (distance(p,init)>90.) break;
    }
    if (!hit) return vec3(0.1);
    bool is_ball = dist == ball;
    vec3 n = norm(p);
    vec3 r = reflect(cam, n);
     
    //add outline to sharp edges
    outline = outline || scene(p+n*.1) < 0.09;
    float fog = smoothstep(80.,60., distance(p,init));

    //shading
    float ao = smoothstep(.0, .5, scene(p+n*.5));
    float fact = ao*length(sin(r*vec3(3.,-2.,2.))*.5+.5)/sqrt(3.);
    float lod = smoothstep(90.,50.,distance(p,init))*5.; //make the shading simpler in the distance
    fact = smoothstairs(fact, lod)+.1;
    vec3 ballcol = abs(erot(srgb(0.6,0.7,0.8), normalize(cos(p*.5)), .3));
    vec3 matcol = is_ball ? ballcol : srgb(0.6,0.65,0.7);
    vec3 col = matcol*fact + mix(vec3(1), matcol, .4)*pow(fact, 10.)*1.5;
    col = mix(vec3(.35), outline ? vec3(0.) : col, fog);
    if (isnan(length(col))) return vec3(.1); //i have no idea where this nan is coming from
    return col;
}

vec2 weyl_2d(int n) {
    return fract(vec2(n*12664745, n*9560333)/exp2(24.));
}

void main(void)
{
    vec2 uv = (gl_FragCoord.xy-.5*resolution.xy)/resolution.y;
    gl_FragColor = vec4(0);
    for (int i = 0; i < AA_SAMPLES; i++) {
        vec2 uv2 = uv + weyl_2d(i)/resolution.y*1.25;
        gl_FragColor += vec4(pixel_color(uv2), 1.);
    }
    gl_FragColor.xyz = sqrt(gl_FragColor.xyz/gl_FragColor.w);
}
