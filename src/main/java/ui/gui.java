package ui;

import oshi.SystemInfo;
import ui.panel.MemoryPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static systemstress.HDStress.stressHD;
import static systemstress.ProcessorStress.stressCPU;
import static systemstress.RAMStress.stressRAM;

public class gui extends JDialog {
    private JPanel contentPane;
    private JButton stresserbt;
    private JButton cancelbt;
    private JTabbedPane stresserTabbePane;
    private JCheckBox RAMcb;
    private JCheckBox CPUcb;
    private JCheckBox HDcb;
    private JPanel stresserPanel;
    private JTextField timetf;
    private JLabel timelb;
    private JPanel monitoringPanel;

    public gui() {
        SystemInfo si = new SystemInfo();

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(stresserbt);
        setResizable(true);

        monitoringPanel.setLayout(new BorderLayout());
        monitoringPanel.add(new MemoryPanel(si), BorderLayout.CENTER);

        stresserbt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        cancelbt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

            }
        });
    }

    private void onOK() {
        String timeText = timetf.getText();
        int tempo;
        try {
            tempo = Integer.parseInt(timeText);
            if (tempo <= 0 || tempo > 60) {
                JOptionPane.showMessageDialog(this, "Por favor, insira um tempo entre 1 e 60 segundos.", "Tempo Inválido", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Por favor, insira um valor numérico válido para o tempo.", "Entrada Inválida", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Executor para rodar os testes em threads separadas
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Verifica quais opções foram selecionadas e executa
        if (CPUcb.isSelected()) {
            executor.submit(() -> stressCPU(tempo));
        }
        if (RAMcb.isSelected()) {
            executor.submit(() -> stressRAM(tempo));
        }
        if (HDcb.isSelected()) {
            executor.submit(() -> stressHD(tempo));
        }

        executor.shutdown();
        JOptionPane.showMessageDialog(this, "Teste iniciado. Verifique o sistema.", "Iniciado", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        gui dialog = new gui();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
