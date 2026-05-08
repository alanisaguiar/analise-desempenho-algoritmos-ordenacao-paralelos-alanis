package sorting.view;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class ChartViewer extends JFrame {

    private final Map<String, Double> data = new LinkedHashMap<>();
    private final List<Integer> sizes   = new ArrayList<>();
    private final List<String>  algos   = new ArrayList<>();

    private JComboBox<String> sizeBox;
    private JComboBox<String> viewBox;
    private ChartPanel        chartPanel;

    public ChartViewer() throws Exception {
        super("Analise de Desempenho - Algoritmos de Ordenacao");
        loadCSV("csv/results.csv");
        buildUI();
    }

    // leitura do CSV
    private void loadCSV(String path) throws Exception {
        Map<String, List<Double>> raw = new LinkedHashMap<>();
        Set<Integer> sizeSet = new TreeSet<>();
        Set<String>  algoSet = new LinkedHashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 6) continue;
                String algo    = p[0];
                String mode    = p[1];
                String threads = p[2];
                int    size    = Integer.parseInt(p[3]);
                double ms      = Double.parseDouble(p[5]);

                String key = algo + "|" + mode + "|" + threads + "|" + size;
                raw.computeIfAbsent(key, k -> new ArrayList<>()).add(ms);
                sizeSet.add(size);
                algoSet.add(algo);
            }
        }
        // calcula as medias
        raw.forEach((k, v) -> data.put(k, v.stream().mapToDouble(Double::doubleValue).average().orElse(0)));
        sizes.addAll(sizeSet);
        algos.addAll(algoSet);
    }

    // interface 
    private void buildUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 580);
        setLocationRelativeTo(null);
        setBackground(Color.WHITE);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        controls.setBackground(new Color(245, 245, 245));
        controls.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        controls.add(new JLabel("Tamanho do array:"));
        sizeBox = new JComboBox<>();
        for (int s : sizes) sizeBox.addItem(formatNum(s) + " elementos");
        controls.add(sizeBox);

        controls.add(Box.createHorizontalStrut(16));

        controls.add(new JLabel("Visualizacao:"));
        viewBox = new JComboBox<>(new String[]{
            "Tempo por algoritmo", "Speedup vs Serial"
        });
        controls.add(viewBox);

        ActionListener update = e -> chartPanel.repaint();
        sizeBox.addActionListener(update);
        viewBox.addActionListener(update);

        chartPanel = new ChartPanel();

        JPanel legend = buildLegend();

        add(controls, BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);
        add(legend, BorderLayout.SOUTH);
    }

    private JPanel buildLegend() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 6));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        String[][] items = {{"Serial","#4A90D9"},{"2 threads","#27AE60"},
                            {"4 threads","#8BC34A"},{"8 threads","#F39C12"}};
        for (String[] item : items) {
            JPanel dot = new JPanel();
            dot.setPreferredSize(new Dimension(14, 14));
            dot.setBackground(Color.decode(item[1]));
            p.add(dot);
            p.add(new JLabel(item[0]));
        }
        return p;
    }

    // desenho do grafico
    class ChartPanel extends JPanel {

        private static final Color[] COLORS = {
            new Color(74,144,217),   // Serial
            new Color(39,174,96),    // 2t
            new Color(139,195,74),   // 4t
            new Color(243,156,18)    // 8t
        };
        private static final String[][] MODES = {
            {"Serial","1"}, {"Parallel","2"}, {"Parallel","4"}, {"Parallel","8"}
        };
        private static final String[] MODE_LABELS = {"Serial","2t","4t","8t"};

        ChartPanel() {
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int selSize = sizes.get(sizeBox.getSelectedIndex());
            boolean speedupMode = viewBox.getSelectedIndex() == 1;

            int W = getWidth(), H = getHeight();
            int padL = 80, padR = 20, padT = 40, padB = 50;
            int chartW = W - padL - padR;
            int chartH = H - padT - padB;

            int nAlgos = algos.size();
            int groupW = chartW / nAlgos;
            int barW   = Math.max(8, groupW / 6);
            int gap    = Math.max(2, barW / 4);

            // valor maximo p/ escala
            double maxVal = 0.001;
            for (String algo : algos) {
                for (String[] m : MODES) {
                    String key = algo + "|" + m[0] + "|" + m[1] + "|" + selSize;
                    double v = data.getOrDefault(key, 0.0);
                    if (speedupMode) {
                        double serial = data.getOrDefault(algo+"|Serial|1|"+selSize, 1.0);
                        v = (serial > 0) ? serial / v : 0;
                    }
                    if (v > maxVal) maxVal = v;
                }
            }
            double scale = chartH / (maxVal * 1.15);

            // eixo Y
            g2.setColor(new Color(180, 180, 180));
            g2.drawLine(padL, padT, padL, padT + chartH);
            g2.drawLine(padL, padT + chartH, padL + chartW, padT + chartH);

            // linhas e labels Y
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            int nLines = 5;
            for (int i = 0; i <= nLines; i++) {
                double val = maxVal * i / nLines;
                int y = padT + chartH - (int)(val * scale);
                g2.setColor(new Color(230, 230, 230));
                g2.drawLine(padL, y, padL + chartW, y);
                g2.setColor(new Color(120, 120, 120));
                String lbl = speedupMode ? String.format("%.1f×", val) : String.format("%.0fms", val);
                g2.drawString(lbl, padL - 52, y + 4);
            }

            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.setColor(new Color(80, 80, 80));
            Graphics2D g2r = (Graphics2D) g2.create();
            g2r.rotate(-Math.PI/2, 18, padT + chartH/2);
            g2r.drawString(speedupMode ? "Speedup (vs Serial)" : "Tempo medio (ms)", 18 - 60, padT + chartH/2 + 4);
            g2r.dispose();

            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2.setColor(new Color(50, 50, 50));
            String title = speedupMode
                ? "Speedup vs Serial — " + formatNum(selSize) + " elementos"
                : "Tempo de Execucao — " + formatNum(selSize) + " elementos";
            g2.drawString(title, padL + chartW/2 - g2.getFontMetrics().stringWidth(title)/2, padT - 14);

            // linha speedup=1
            if (speedupMode) {
                int y1 = padT + chartH - (int)(1.0 * scale);
                g2.setColor(new Color(220, 80, 80, 160));
                float[] dash = {6, 4};
                g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dash, 0));
                g2.drawLine(padL, y1, padL + chartW, y1);
                g2.setStroke(new BasicStroke(1));
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                g2.setColor(new Color(200, 60, 60));
                g2.drawString("1.0× (sem ganho)", padL + 4, y1 - 4);
            }

            for (int ai = 0; ai < algos.size(); ai++) {
                String algo = algos.get(ai);
                int groupX  = padL + ai * groupW + groupW/2 - (MODES.length * (barW + gap)) / 2;

                for (int mi = 0; mi < MODES.length; mi++) {
                    String key = algo + "|" + MODES[mi][0] + "|" + MODES[mi][1] + "|" + selSize;
                    double rawVal = data.getOrDefault(key, 0.0);
                    double val = rawVal;
                    if (speedupMode) {
                        double serial = data.getOrDefault(algo+"|Serial|1|"+selSize, 1.0);
                        val = (serial > 0 && rawVal > 0) ? serial / rawVal : 0;
                    }

                    int barH = (int)(val * scale);
                    int x = groupX + mi * (barW + gap);
                    int y = padT + chartH - barH;

                    Color c = COLORS[mi];
                    if (speedupMode && val < 1.0 && mi > 0) c = new Color(220, 80, 80);

                    g2.setColor(c);
                    g2.fillRect(x, y, barW, barH);
                    g2.setColor(c.darker());
                    g2.drawRect(x, y, barW, barH);

                    g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                    g2.setColor(new Color(60, 60, 60));
                    String lbl = speedupMode ? String.format("%.2f", val) : String.format("%.0f", rawVal);
                    int lw = g2.getFontMetrics().stringWidth(lbl);
                    if (barH > 14) g2.drawString(lbl, x + barW/2 - lw/2, y - 2);
                }

                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2.setColor(new Color(60, 60, 60));
                String shortName = algo.replace("Sort","");
                int sw2 = g2.getFontMetrics().stringWidth(shortName);
                g2.drawString(shortName, padL + ai * groupW + groupW/2 - sw2/2, padT + chartH + 18);

                // labels threads
                g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                g2.setColor(new Color(140, 140, 140));
                int groupX2 = padL + ai * groupW + groupW/2 - (MODES.length * (barW + gap)) / 2;
                for (int mi = 0; mi < MODES.length; mi++) {
                    int x = groupX2 + mi * (barW + gap);
                    int lw = g2.getFontMetrics().stringWidth(MODE_LABELS[mi]);
                    g2.drawString(MODE_LABELS[mi], x + barW/2 - lw/2, padT + chartH + 32);
                }
            }
        }
    }

    private static String formatNum(int n) {
        return String.format("%,d", n).replace(',', '.');
    }

    // metodo de entrada
    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new ChartViewer().setVisible(true);
            } catch (Exception e) {
                System.err.println("Erro ao abrir o visualizador: " + e.getMessage());
            }
        });
    }
}