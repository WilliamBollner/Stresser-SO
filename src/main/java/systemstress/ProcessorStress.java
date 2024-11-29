package systemstress;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.W32APIOptions;

import java.util.ArrayList;
import java.util.List;

public class ProcessorStress {
    public interface Kernel32 extends com.sun.jna.platform.win32.Kernel32 {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class, W32APIOptions.DEFAULT_OPTIONS);

        boolean SetThreadAffinityMask(WinNT.HANDLE hThread, Pointer dwThreadAffinityMask);
    }

    public static void stressSpecificCPUs(List<String> cpuList, int tempo) {
        System.out.println("Iniciando estresse nas CPUs: " + String.join(", ", cpuList));

        long endTime = System.currentTimeMillis() + tempo * 1000;

        // Lista para armazenar todas as threads
        List<Thread> threads = new ArrayList<>();

        for (String cpu : cpuList) {
            int cpuIndex = parseCPUIndex(cpu);

            Thread thread = new Thread(() -> {
                setThreadAffinityToCPU(cpuIndex);

                // Loop de cálculo pesado
                while (System.currentTimeMillis() < endTime) {
                    Math.pow(Math.random(), Math.random());
                }
            });

            threads.add(thread);
            thread.start();
        }
        // Aguarda todas as threads concluírem
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.println("Thread interrompida: " + e.getMessage());
            }
        }

        System.out.println("Estresse concluído para CPUs: " + String.join(", ", cpuList));
    }

    private static int parseCPUIndex(String cpu) {
        if (cpu.toLowerCase().startsWith("cpu")) {
            try {
                return Integer.parseInt(cpu.substring(3));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Formato inválido para CPU: " + cpu);
            }
        }
        throw new IllegalArgumentException("Formato inválido para CPU: " + cpu);
    }

    private static void setThreadAffinityToCPU(int cpuIndex) {
        WinNT.HANDLE threadHandle = Kernel32.INSTANCE.GetCurrentThread();
        long affinityMask = 1L << cpuIndex; // Define o bit correspondente à CPU
        Pointer maskPointer = Pointer.createConstant(affinityMask);

        System.out.printf("Definindo afinidade para CPU %d com máscara: 0x%X%n", cpuIndex, affinityMask);

        if (!Kernel32.INSTANCE.SetThreadAffinityMask(threadHandle, maskPointer)) {
            throw new RuntimeException("Falha ao definir afinidade para CPU " + cpuIndex);
        }
    }
}