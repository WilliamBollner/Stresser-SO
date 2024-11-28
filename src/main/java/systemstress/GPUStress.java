package systemstress;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.*;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import static jcuda.driver.JCudaDriver.*;

public class GPUStress {
    public static void stressGPU(int timeInSeconds) throws URISyntaxException {
        JCudaDriver.setExceptionsEnabled(true);

        // Inicializa o JCuda
        System.out.println("Inicializando JCuda...");
        cuInit(0);

        // Obtém o número de GPUs disponíveis
        int[] numDevices = new int[1];
        cuDeviceGetCount(numDevices);
        if (numDevices[0] < 1) {
            System.err.println("Nenhuma GPU disponível. Encerrando.");
            return;
        }

        // Seleciona a GPU e cria o contexto
        CUdevice device = new CUdevice();
        cuDeviceGet(device, 0);
        CUcontext context = new CUcontext();
        cuCtxCreate(context, 0, device);

        // Configuração para o kernel
        int numIterations = 10_000_000;
        int threadsPerBlock = 1024;
        int numBlocks = 64;

        int dataSize = threadsPerBlock * numBlocks;

        System.out.println("Configuração do Kernel: " + threadsPerBlock + " threads por bloco, " + numBlocks + " blocos.");

        // Alocação de memória na GPU
        CUdeviceptr dData = new CUdeviceptr();
        cuMemAlloc(dData, dataSize * Sizeof.FLOAT);

        // Inicializa os dados
        float[] hData = new float[dataSize];
        for (int i = 0; i < dataSize; i++) {
            hData[i] = 1.0f; // Inicializa com 1.0f
        }
        cuMemcpyHtoD(dData, Pointer.to(hData), dataSize * Sizeof.FLOAT);

        // Obtém o caminho dinâmico do projeto
        String projectPath = new File(GPUStress.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI())
                .getParentFile()
                .getParentFile()
                .getAbsolutePath();

// Concatena o caminho dinâmico com o local do arquivo PTX
        String ptxFileName = Paths.get(projectPath,
                        "src", "main", "java", "systemstress", "cuda", "matrixMultiply.ptx")
                .toString();

// Verifica se o arquivo PTX existe no caminho calculado
        File ptxFile = new File(ptxFileName);
        if (!ptxFile.exists()) {
            System.err.println("Arquivo PTX não encontrado em: " + ptxFileName);
            return;
        }

        // Carrega o módulo PTX
        CUmodule module = new CUmodule();
        cuModuleLoad(module, ptxFileName);

        // Obtém a função kernel
        CUfunction function = new CUfunction();
        cuModuleGetFunction(function, module, "stressKernel");

        // Define os parâmetros do kernel
        Pointer kernelParameters = Pointer.to(
                Pointer.to(dData),
                Pointer.to(new int[]{numIterations})
        );

        // Inicia o stress test
        System.out.println("Iniciando stress test por " + timeInSeconds + " segundos...");
        long endTime = System.currentTimeMillis() + timeInSeconds * 1000;
        while (System.currentTimeMillis() < endTime) {
            cuLaunchKernel(function,
                    numBlocks, 1, 1,      // Grid
                    threadsPerBlock, 1, 1, // Bloco
                    0, null,              // Shared memory e stream
                    kernelParameters, null);
            cuCtxSynchronize();
        }

        System.out.println("Stress test concluído.");

        // Limpeza de memória
        cuMemFree(dData);
        cuCtxDestroy(context);
    }
}