package systemstress;

public class ProcessorStress {
    public static void stressCPU(int tempo) {
        System.out.println("Iniciando estresse na CPU...");
        long endTime = System.currentTimeMillis() + tempo * 1000;

        Thread[] threads = new Thread[Runtime.getRuntime().availableProcessors()];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                while (System.currentTimeMillis() < endTime) {
                    Math.pow(Math.random(), Math.random()); // Operações pesadas
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.println("Thread interrompida: " + e.getMessage());
            }
        }
        System.out.println("Estresse na CPU concluído.");
    }
}