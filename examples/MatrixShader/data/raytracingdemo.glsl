#version 120

// original https://www.shadertoy.com/view/wllXRl

uniform float time;
uniform vec2 mouse;
uniform vec2 resolution;

struct Ray
{
    vec3 origin;
    vec3 direction;
};

struct Sphere
{
    vec3 position;
    float radius;
    vec3 color;
};

struct Plane
{
    vec3 normal;
    float height;
    vec3 color;
};

vec3 LightShading(vec3 N,vec3 L,vec3 V,vec3 color)
{
    vec3 diffuse = max(0.,dot(N,-L))*color;
    vec3 specular = pow(max(0.,dot(N,normalize(-L-V))),100.)*vec3(1.,1.,1.); 
    return diffuse + specular;
}

// å…‰çº¿å’Œçƒä½“è®¡ç®—äº¤ç‚¹
float Intersect(Ray ray,Sphere sphere)
{
    vec3 v = ray.origin - sphere.position;
    //b^2-4ac
    float a = dot(ray.direction,ray.direction);
    float b = dot(ray.direction,v);
    float c = dot(v,v) - sphere.radius * sphere.radius;
    float value = b*b-a*c;
    if(value < 0.)
        return -1.;
    if(value == 0.)
        return - b/a;
    float sqrtValue = sqrt(value);
    float result = -b - sqrtValue;
    if(result > 0.)
        return result/a;
    return -1.;
}

// å…‰çº¿å’Œåœ°é¢è®¡ç®—äº¤ç‚¹
float Intersect(Ray ray,Plane plane)
{
    float LDotN = dot(ray.direction,plane.normal);
    if(LDotN > 0.)
        return -1.;
    return (plane.height - ray.origin.y)/ray.direction.y;
}

vec3 rotate(vec3 origin,vec3 target,float degree)
{
//    float degree = time-1.;

    vec3 relate = origin - target;
    float x = relate.x * cos(degree) + relate.z * sin(degree) + target.x;
    float z = -relate.x * sin(degree) + relate.z * cos(degree) + target.z;

    return vec3(x,origin.y,z);
}

vec3 radiance(Ray ray)
{
    float t1 = time * 5;
    Sphere spheres[3];
    spheres[0] = Sphere(vec3(0.0,3.0+(sin(t1)+1.)*4.,8.0),3.0,vec3(1.0,0.0,0.0));
    spheres[1] = Sphere(vec3(-2.0-sin(t1)*4.,3.0,0.0),3.0,vec3(0.0,1.0,0.0));
    spheres[2] = Sphere(rotate(vec3(0.0,3.0,7.0),vec3(-2.0,3.0,0.0),t1-1),3.0,vec3(0.0,0.0,1.0));
    

    Plane plane = Plane(vec3(0.0,1.0,0.0),0.0,vec3(0.8,0.5,0.));
    vec3 skyColor = vec3(0.2,0.2,0.2);
    vec3 cameraPos = ray.origin;

    vec3 result = vec3(0.,0.,0.);
    float intensity = 1.0;
    for(int ti = 0;ti<1000;++ti)
    {
        vec3 L = ray.direction;
        int sIndex = -1;
        float t = -1.;
        for(int i=0;i<3;++i)
        {
            float tt = Intersect(ray,spheres[i]);
            if(tt > 0. && (tt < t || t < 0.))
            {
                sIndex = i;
                t = tt;
            }
        }

        float tt = Intersect(ray,plane);

        if(t<=0.)
        {
            if(tt > 0.)
            {
                vec3 pos = ray.origin + tt * ray.direction;
                vec3 V = normalize(pos - cameraPos);
                vec3 L = ray.direction;
                vec3 normal = plane.normal;
                vec3 refl = 2.*dot(normal,-ray.direction)*normal + ray.direction;
                ray = Ray(pos,refl);
                result += LightShading(normal,L,V, plane.color)*intensity; //åœ°æ¿è‰²
            }
            else
            {
                result += skyColor*intensity;//å¤©ç©ºè‰²
                break;
            }
        }
        else
        {
            if(t < tt || tt <= 0.)
            {
                vec3 pos = ray.origin + t * ray.direction;
                 vec3 V = normalize(pos - cameraPos);
                vec3 L = ray.direction;
                vec3 normal = normalize(pos - spheres[sIndex].position);
                vec3 refl = 2.*dot(normal,-ray.direction)*normal + ray.direction;
                ray = Ray(pos,refl);
                result += LightShading(normal,L,V,spheres[sIndex].color)*intensity;
            }
            else
            {
                vec3 pos = ray.origin + tt * ray.direction;
                vec3 V = normalize(pos - cameraPos);
                vec3 L = ray.direction;
                vec3 normal = plane.normal;
                vec3 refl = 2.*dot(normal,-ray.direction)*normal + ray.direction;
                ray = Ray(pos,refl);
                result += LightShading(normal,L,V,plane.color)*intensity; //åœ°æ¿è‰²
            }
        }

        intensity /= 2.;
    }
    return result;
}

void main(void)
{
    vec2 uv = (gl_FragCoord.xy/resolution.xy - vec2(0.5,0.5));
    uv.x *= (resolution.x/resolution.y);
    vec3 cameraPos = vec3(14.,5.,0.);
    Ray ray = Ray(cameraPos,normalize(vec3(-1.,uv.y,uv.x)));

    gl_FragColor = vec4(radiance(ray),1.0);
}
