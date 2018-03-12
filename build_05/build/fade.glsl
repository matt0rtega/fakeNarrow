#define PROCESSING_TEXTURE_SHADER

uniform sampler2D texture;
uniform float fadeLevel;
varying vec4 vertColor;
varying vec4 vertTexCoord;

void main(void) {
    vec3 curr = texture2D(texture, vertTexCoord.st).rgb;
    gl_FragColor = vec4(curr.r-fadeLevel, curr.g-fadeLevel, curr.b-fadeLevel, 1.0) * vertColor;
}
