package ui.panel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Second;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.TickType;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProcessorPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final String SYSTEM_CPU_USAGE = "System CPU Usage";
    private static final String PROCESSOR_CPU_USAGE = "Processor CPU Usage";

    private long[] oldTicks;
    private long[][] oldProcTicks;

    public ProcessorPanel(SystemInfo si) {
        super();
        init(si.getHardware().getProcessor());
    }

    private void init(CentralProcessor processor) {
        setLayout(new BorderLayout());

        // Inicializa os datasets
        DynamicTimeSeriesCollection sysData = createSystemCpuDataset(processor);
        DynamicTimeSeriesCollection procData = createProcessorCpuDataset(processor);

        // Cria os gráficos
        JFreeChart systemCpuChart = createChart(sysData, SYSTEM_CPU_USAGE, "% CPU");
        JFreeChart processorCpuChart = createChart(procData, PROCESSOR_CPU_USAGE, "% CPU");

        // Configura o layout dos gráficos
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2));
        chartsPanel.add(new ChartPanel(systemCpuChart));
        chartsPanel.add(new ChartPanel(processorCpuChart));

        add(chartsPanel, BorderLayout.CENTER);

        // Atualiza os gráficos periodicamente
        Timer timer = new Timer(1000, e -> {
            updateSystemCpuDataset(sysData, processor);
            updateProcessorCpuDataset(procData, processor);
        });
        timer.start();
    }

    private DynamicTimeSeriesCollection createSystemCpuDataset(CentralProcessor processor) {
        oldTicks = new long[TickType.values().length];
        DynamicTimeSeriesCollection dataset = new DynamicTimeSeriesCollection(1, 60, new Second());
        dataset.setTimeBase(new Second(getCurrentDate()));
        dataset.addSeries(toPercentArray(processor.getSystemCpuLoadBetweenTicks(oldTicks)), 0, "All CPUs");
        return dataset;
    }

    private DynamicTimeSeriesCollection createProcessorCpuDataset(CentralProcessor processor) {
        int logicalProcessorCount = processor.getLogicalProcessorCount();
        oldProcTicks = new long[logicalProcessorCount][TickType.values().length];
        DynamicTimeSeriesCollection dataset = new DynamicTimeSeriesCollection(logicalProcessorCount, 60, new Second());
        dataset.setTimeBase(new Second(getCurrentDate()));

        var logicalProcessors = processor.getLogicalProcessors();

        double[] initialLoads = processor.getProcessorCpuLoadBetweenTicks(oldProcTicks);
        Map<Integer, Double> cpuLoadMap = rearrangeCpuLoad(initialLoads, logicalProcessorCount);

        for (int i = 0; i < logicalProcessorCount; i++) {
            int cpuNumber = logicalProcessors.get(i).getProcessorNumber();
            double load = cpuLoadMap.getOrDefault(cpuNumber, 0.0);
            float initialLoad = (float) (load * 100); // Converte para porcentagem
            dataset.addSeries(new float[]{initialLoad}, i, "CPU " + cpuNumber);
        }

        return dataset;
    }

    private void updateSystemCpuDataset(DynamicTimeSeriesCollection dataset, CentralProcessor processor) {
        dataset.advanceTime();
        dataset.appendData(toPercentArray(processor.getSystemCpuLoadBetweenTicks(oldTicks)));
        oldTicks = processor.getSystemCpuLoadTicks();
    }

    private void updateProcessorCpuDataset(DynamicTimeSeriesCollection dataset, CentralProcessor processor) {
        var logicalProcessors = processor.getLogicalProcessors();
        dataset.advanceTime();
        int newestIndex = dataset.getNewestIndex();
        double[] procLoad = processor.getProcessorCpuLoadBetweenTicks(oldProcTicks);
        Map<Integer, Double> cpuLoadMap = rearrangeCpuLoad(procLoad, processor.getLogicalProcessorCount());

        for (int i = 0; i < procLoad.length; i++) {
            int cpuNumber = logicalProcessors.get(i).getProcessorNumber();
            double load = cpuLoadMap.getOrDefault(cpuNumber, 0.0);
            dataset.addValue(i, newestIndex, (float) (load * 100));
        }
        oldProcTicks = processor.getProcessorCpuLoadTicks();
    }

    private JFreeChart createChart(DynamicTimeSeriesCollection dataset, String title, String yAxisLabel) {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(title, "Time", yAxisLabel, dataset, true, true, false);
        XYPlot plot = chart.getXYPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        return chart;
    }

    private Date getCurrentDate() {
        return Date.from(LocalDateTime.now(ZoneId.systemDefault()).atZone(ZoneId.systemDefault()).toInstant());
    }

    private static float[] toPercentArray(double value) {
        return new float[]{(float) (value * 100)};
    }

    private Map<Integer, Double> rearrangeCpuLoad(double[] initialLoads, int logicalProcessorCount) {
        Map<Integer, Double> rearrangedLoads = new HashMap<>();

        // O padrão de reposicionamento conforme a quantidade de núcleos
        int[] newOrder = new int[logicalProcessorCount];

        // Para sistemas com 12 núcleos lógicos (ou mais), mapeia a ordem de acordo com o padrão fornecido
        if (logicalProcessorCount >= 12) {
            newOrder = new int[]{0, 1, 10, 11, 2, 3, 4, 5, 6, 7, 8, 9};
        } else {
            // Para menos núcleos, aplicamos uma ordem padrão ou ajustada conforme o caso
            for (int i = 0; i < logicalProcessorCount; i++) {
                newOrder[i] = i;
            }
        }

        // Preenche o mapa com as cargas reorganizadas
        for (int i = 0; i < logicalProcessorCount; i++) {
            int logicalCpuNumber = newOrder[i]; // CPU lógica reorganizada
            rearrangedLoads.put(logicalCpuNumber, initialLoads[i]);
        }

        return rearrangedLoads;
    }
}