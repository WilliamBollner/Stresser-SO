package systemstress;

import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;

import java.util.ArrayList;
import java.util.List;

public class RAMStress {
    public static void stressRAM(int tempo) {
        System.out.println("Iniciando estresse na RAM...");

        SystemInfo si = new SystemInfo();
        GlobalMemory memory = si.getHardware().getMemory();

        long totalMemory = memory.getTotal();
        System.out.println("Memória total do sistema: " + (totalMemory / (1024 * 1024)) + " MB");

        long endTime = System.currentTimeMillis() + tempo * 1000;

        // Lista para manter referências e evitar que o garbage collector libere a memória
        List<byte[]> memoryList = new ArrayList<>();

        try {
            long totalAllocated = 0;

            while (System.currentTimeMillis() < endTime) {
                // Consultar memória disponível atual
                long memoryAvailable = memory.getAvailable();
                System.out.println("Memória disponível: " + (memoryAvailable / (1024 * 1024)) + " MB");

                if (memoryAvailable < 50 * 1024 * 1024) { // Parar se menos de 50 MB estiverem disponíveis
                    System.out.println("Memória disponível insuficiente para novas alocações.");
                    break;
                }

                // Calcular tamanho do próximo bloco com base na memória restante
                long blockSize = Math.min(500 * 1024 * 1024, memoryAvailable - 10 * 1024 * 1024); // Aloca blocos de até 500 MB
                if (memoryAvailable - blockSize < 50 * 1024 * 1024) {
                    blockSize = memoryAvailable - 10 * 1024 * 1024; // Na última alocação, use quase todo o restante
                }
                byte[] block = new byte[(int) blockSize];
                memoryList.add(block);

                // Inicializar o bloco para uso ativo
                for (int i = 0; i < block.length; i += 4096) {
                    block[i] = (byte) (i % 256); // Inicializa de forma dispersa
                }

                totalAllocated += blockSize;
                System.out.println("Bloco alocado: " + (blockSize / (1024 * 1024)) + " MB. Total alocado: " + (totalAllocated / (1024 * 1024)) + " MB");

                // Pequena pausa para evitar estresse excessivo ao consultar a memória
                Thread.sleep(50);
            }

            System.out.println("Alocação máxima concluída. Total alocado: " + (totalAllocated / (1024 * 1024)) + " MB");

            // Processar os blocos para manter "uso ativo" e evitar coleta pelo GC
            while (System.currentTimeMillis() < endTime) {
                for (int i = 0; i < memoryList.size(); i++) {
                    byte[] block = memoryList.get(i);

                    // Processar o bloco (manter referência ativa)
                    for (int j = 0; j < block.length; j += 4096) {
                        block[j] = (byte) ((block[j] + 1) % 256); // Modifica de forma esparsa
                    }

                    System.out.println("Bloco " + (i + 1) + " processado.");
                }
            }

        } catch (OutOfMemoryError e) {
            System.out.println("Memória insuficiente! Ajuste o tamanho da heap ou limite alocação para mais estresse.");
        } catch (InterruptedException e) {
            System.out.println("Thread interrompida durante o estresse.");
        } finally {
            // Liberar memória após o estresse
            memoryList.clear();
            System.gc(); // Sinaliza coleta de lixo
            System.out.println("Estresse na RAM concluído e memória liberada.");
        }
    }
}