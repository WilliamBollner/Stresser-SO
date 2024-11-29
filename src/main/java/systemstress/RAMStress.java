package systemstress;

import java.util.ArrayList;
import java.util.List;

public class RAMStress {
    public static void stressRAM(int tempo) {
        System.out.println("Iniciando estresse na RAM...");
        long endTime = System.currentTimeMillis() + tempo * 1000;

        // Lista para manter referências e evitar que o garbage collector libere a memória
        java.util.List<byte[]> memoryList = new java.util.ArrayList<>();

        try {
            // Continuar alocando memória e preenchendo até o tempo especificado
            while (System.currentTimeMillis() < endTime) {
                // Alocar blocos de 200 MB
                byte[] block = new byte[200 * 1024 * 1024];
                memoryList.add(block); // Evitar coleta de lixo

                // Processar o bloco para garantir uso ativo
                for (int i = 0; i < block.length; i++) {
                    block[i] = (byte) (i % 256);
                }

                System.out.println("Memória ativa: " + (memoryList.size() * 200) + " MB");
            }
        } catch (OutOfMemoryError e) {
            System.out.println("Memória insuficiente! O estresse atingiu o limite.");
        }

        // Para manter o estresse ativo no restante do tempo, continue preenchendo os blocos existentes
        while (System.currentTimeMillis() < endTime) {
            for (byte[] block : memoryList) {
                for (int i = 0; i < block.length; i++) {
                    block[i] = (byte) ((block[i] + 1) % 256); // Modifica os valores para garantir processamento
                }
            }
        }

        // Liberação de memória após o término
        memoryList.clear();
        System.gc(); // Sinaliza coleta de lixo
        System.out.println("Estresse na RAM concluído.");
    }
}