package systemstress;

import java.util.ArrayList;
import java.util.List;

public class RAMStress {
    public static void stressRAM(int tempo) {
        System.out.println("Iniciando estresse na RAM com operações...");

        List<int[]> arrays = new ArrayList<>();
        int size = 900_000_000;

        for (int j = 0; j < 5; j++) {
            int[] array = new int[size];
            for (int i = 0; i < size; i++) {
                array[i] = i;
            }
            arrays.add(array);
        }

        long endTime = System.currentTimeMillis() + tempo * 1000;

        outerLoop:
        while (System.currentTimeMillis() < endTime) {
            for (int[] array : arrays) {
                for (int i = 0; i < array.length; i++) {
                    if (System.currentTimeMillis() >= endTime) {
                        break outerLoop; // Sai imediatamente do loop externo
                    }
                    array[i] = (array[i] + 1) % 256;
                }
            }
        }
        System.out.println("Estresse na RAM concluído com múltiplos arrays.");
    }
}