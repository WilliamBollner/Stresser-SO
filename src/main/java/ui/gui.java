package ui;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;
import ui.panel.MemoryPanel;
import ui.panel.ProcessorPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static systemstress.GPUStress.stressGPU;
import static systemstress.HDStress.stressHD;
import static systemstress.ProcessorStress.stressSpecificCPUs;
import static systemstress.RAMStress.stressRAM;

public class gui extends JDialog {
    private JPanel contentPane;
    private JButton stresserbt;
    private JTabbedPane stresserTabbePane;
    private JPanel stresserPanel;
    private JTextField timetf;
    private JLabel timelb;
    private JPanel RAMPanel;
    private JRadioButton CPURadioButton;
    private JRadioButton RAMRadioButton;
    private JRadioButton HDRadioButton;
    private JRadioButton GPURadioButton;
    private JPanel ProcessorPanel;
    private JTextArea textArea1;
    private JTextArea textArea2;

    public gui() {
        SystemInfo si = new SystemInfo();

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(stresserbt);
        setResizable(true);

        RAMPanel.setLayout(new BorderLayout());
        RAMPanel.add(new MemoryPanel(si), BorderLayout.CENTER);
        ProcessorPanel.setLayout(new BorderLayout());
        ProcessorPanel.add(new ProcessorPanel(si), BorderLayout.CENTER);

        stresserbt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        textArea1.append(getProc(si));
        textArea2.append(getOs(si));
        CPURadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(CPURadioButton.isSelected()) {
                    stresserbt.setEnabled(true);
                }
            }
        });
        RAMRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(RAMRadioButton.isSelected()) {
                    stresserbt.setEnabled(true);
                }
            }
        });
        GPURadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(GPURadioButton.isSelected()) {
                    stresserbt.setEnabled(true);
                }
            }
        });
        HDRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(HDRadioButton.isSelected()) {
                    stresserbt.setEnabled(true);
                }
            }
        });
    }

    private void onOK() {
        String timeText = timetf.getText();
        int tempo;

        try {
            tempo = Integer.parseInt(timeText);
            if (tempo < 10 || tempo > 60) {
                JOptionPane.showMessageDialog(this, "Por favor, insira um tempo entre 10 e 60 segundos.", "Tempo Inválido", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Por favor, insira um valor numérico válido para o tempo.", "Entrada Inválida", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();

        if (CPURadioButton.isSelected()) {
            List<String> selectedCPUs = showCPUSelectionDialog();
            if (!selectedCPUs.isEmpty()) {
                executor.submit(() -> stressSpecificCPUs(selectedCPUs, tempo));
            } else {
                JOptionPane.showMessageDialog(this, "Nenhuma CPU selecionada. Operação cancelada.", "Operação Cancelada", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else if (RAMRadioButton.isSelected()) {
            executor.submit(() -> stressRAM(tempo));
        } else if (HDRadioButton.isSelected()) {
            executor.submit(() -> stressHD(tempo));
        } else if (GPURadioButton.isSelected()) {
            executor.submit(() -> {
                try {
                    stressGPU(tempo);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, selecione uma opção para realizar o teste.", "Nenhuma Opção Selecionada", JOptionPane.WARNING_MESSAGE);
            return;
        }
        executor.shutdown();
        JOptionPane.showMessageDialog(this, "Teste iniciado. Verifique o sistema.", "Iniciado", JOptionPane.INFORMATION_MESSAGE);
    }

    private List<String> showCPUSelectionDialog() {
        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hal = systemInfo.getHardware();
        CentralProcessor processor = hal.getProcessor();

        int numCPUs = processor.getLogicalProcessorCount();
        List<JCheckBox> checkBoxes = new ArrayList<>();
        JPanel panel = new JPanel(new GridLayout(0, 1));

        for (int i = 0; i < numCPUs; i++) {
            JCheckBox checkBox = new JCheckBox("CPU " + i);
            checkBoxes.add(checkBox);
            panel.add(checkBox);
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Selecione as CPUs para estressar",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        List<String> selectedCPUs = new ArrayList<>();
        if (result == JOptionPane.OK_OPTION) {
            for (int i = 0; i < checkBoxes.size(); i++) {
                if (checkBoxes.get(i).isSelected()) {
                    selectedCPUs.add("cpu" + i);
                }
            }
        }
        return selectedCPUs;
    }

    private static String getProc(SystemInfo si) {
        StringBuilder sb = new StringBuilder();
        CentralProcessor proc = si.getHardware().getProcessor();
        sb.append(proc.toString());

        return sb.toString();
    }

    private String getOs(SystemInfo si) {
        return getOsPrefix(si) + FormatUtil.formatElapsedSecs(si.getOperatingSystem().getSystemUptime());
    }

    private static String getOsPrefix(SystemInfo si) {
        StringBuilder sb = new StringBuilder("Sistema Operacional: ");

        OperatingSystem os = si.getOperatingSystem();
        sb.append(String.valueOf(os));
        sb.append("\n\n").append("Booted: ").append(Instant.ofEpochSecond(os.getSystemBootTime())).append('\n')
                .append("Uptime: ");
        return sb.toString();
    }

    public static void main(String[] args) {
        gui dialog = new gui();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
