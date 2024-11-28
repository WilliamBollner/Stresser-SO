#include <cuda_runtime.h>
#include <cmath>

extern "C" __global__ void stressKernel(float* data, int iterations) {
    int idx = threadIdx.x + blockIdx.x * blockDim.x;

    if (idx < blockDim.x * gridDim.x) { // Ensure the index is valid
        float value = data[idx];
        for (int i = 0; i < iterations; ++i) {
            value = sinf(value) * cosf(value) + tanf(value);
        }
        data[idx] = value;  // Store the final result
    }
}