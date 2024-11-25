package systemstress;

public class HDStress {
    public static void stressHD(int tempo) {
        System.out.println("Iniciando estresse no Disco...");
        long endTime = System.currentTimeMillis() + tempo * 1000;

        try {
            while (System.currentTimeMillis() < endTime) {
                // Criar e preencher arquivos grandes temporários
                java.nio.file.Files.write(java.nio.file.Files.createTempFile("stress", ".tmp"),
                        new byte[1024 * 1024 * 10]); // Escrevendo 10 MB
            }
        } catch (Exception e) {
            System.out.println("Erro durante o estresse no disco: " + e.getMessage());
        }
        System.out.println("Estresse no Disco concluído.");
    }
}