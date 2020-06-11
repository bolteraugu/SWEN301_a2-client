package nz.ac.vuw.swen301.a2.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Level;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class LogMonitor extends JFrame implements ActionListener {
    private Dimension screenSize;
    private JButton submit;
    JPanel overallPanel;
    GridBagConstraints c;
    private JPanel[][] panels;
    private int tablewidth = 7;
    private int tableheight = 10000;

    public static void main(String[] args) {
        LogMonitor monitor = new LogMonitor();
    }
    public LogMonitor() {
        setVisible(true);
        setTitle("Log Monitor");
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int) Math.round(screenSize.width), (int) Math.round(screenSize.height*0.80));
        overallPanelSetup();
        createFilterSection();
        createLogTable();
        add(overallPanel);
        revalidate();
        repaint();
    }

    private void overallPanelSetup() {
        overallPanel = new JPanel();
        overallPanel.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
    }

    private void createLogTable() {
        String[] columnNames = {"ID", "Message", "Timestamp", "Thread", "Logger", "Level", "Error Details"};
        String dataString = null;
        try {
            dataString = fetchData("OFF", "5");
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        Object[][] data = null;
        if (dataString != null && dataString.length() != 0) {
            data = transformString(dataString, "5");
        }
        DefaultTableModel logsLayout = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable logs = new JTable(logsLayout);
        Dimension tableDim = new Dimension((int) Math.round(screenSize.width*0.98), (int) Math.round(screenSize.height*0.74));
        logs.setPreferredSize(tableDim);
        logs.setMaximumSize(tableDim);
        logs.setMinimumSize(tableDim);
        logs.setFont(new Font("Serif", Font.PLAIN, 16));
        panels = new JPanel[tableheight][tablewidth]; //Creating table structure
        for (int row = 0; row < 7; row++) { //Adding panels to table
            for (int col = 0; col < 7; col++) {
                JPanel p = new JPanel();
                panels[row][col] = p;
            }
        }
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 2;
        JScrollPane pane = new JScrollPane(logs);
        pane.setPreferredSize(tableDim);
        pane.setMaximumSize(tableDim);
        pane.setMinimumSize(tableDim);
        overallPanel.add(pane, c);
    }

    private Object[][] transformString(String dataString, String limit) {
        Gson g = new Gson();
        JsonArray logArray = null;
        JsonObject logObj = null;
        int logNum = 0;
        if (dataString.startsWith("[")) {
            logArray = g.fromJson(dataString, JsonArray.class);
            logNum = logArray.size();
        }
        else {
            logObj = g.fromJson(dataString, JsonObject.class);
            logNum = 1;
        }
        Object[][] data = new Object[logNum][7];
        if (logArray != null) {
            int row = 0;
            for (JsonElement log : logArray) {
                JsonObject obj = log.getAsJsonObject();
                data[row][0] = obj.get("id").toString().substring(1, obj.get("id").toString().length()-1);
                data[row][1] = obj.get("message").toString().substring(1, obj.get("message").toString().length()-1);
                data[row][2] = obj.get("timestamp").toString().substring(1, obj.get("timestamp").toString().length()-1);
                data[row][3] = obj.get("thread").toString().substring(1, obj.get("thread").toString().length()-1);
                data[row][4] = obj.get("logger").toString().substring(1, obj.get("logger").toString().length()-1);
                data[row][5] = obj.get("level").toString().substring(1, obj.get("level").toString().length()-1);
                if (obj.get("errorDetails") != null) {
                    data[row][6] = obj.get("errorDetails").toString().substring(1, obj.get("errorDetails").toString().length()-1);
                }
                row++;
            }
        }
        else {
            data[0][0] = logObj.get("id").toString().substring(1, logObj.get("id").toString().length()-1);
            data[0][1] = logObj.get("message").toString().substring(1, logObj.get("message").toString().length()-1);
            data[0][2] = logObj.get("timestamp").toString().substring(1, logObj.get("timestamp").toString().length()-1);
            data[0][3] = logObj.get("thread").toString().substring(1, logObj.get("thread").toString().length()-1);
            data[0][4] = logObj.get("logger").toString().substring(1, logObj.get("logger").toString().length()-1);
            data[0][5] = logObj.get("level").toString().substring(1, logObj.get("level").toString().length()-1);
            if (logObj.get("errorDetails") != null) {
                data[0][6] = logObj.get("errorDetails").toString().substring(1, logObj.get("errorDetails").toString().length()-1);
            }
        }
        return data;
    }

    private String fetchData(String level, String limit) throws URISyntaxException, IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost("localhost").setPort(8080).setPath("/resthome4logs/logs")
                .setParameter("level", level).setParameter("limit", limit);
        URI uri = builder.build();

        HttpGet request = new HttpGet(uri);
        HttpResponse response = httpClient.execute(request);

        // this string is the unparsed web page (=html source code)
        String content = EntityUtils.toString(response.getEntity());
        return content;
    }

    private void createFilterSection() {
        JPanel filterPanel = new JPanel();
        Dimension panelDim = new Dimension((int) Math.round(screenSize.width), (int) Math.round(screenSize.height*0.06));
        filterPanel.setPreferredSize(panelDim);
        filterPanel.setMaximumSize(panelDim);
        filterPanel.setMinimumSize(panelDim);
        filterPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        /* Creating min level drop down menu and label */
        JLabel minLevelLabel = new JLabel("Min Level:");
        String[] levels = {"OFF", "FATAL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE", "ALL"};
        JComboBox minLevel = new JComboBox(levels);
        /* Creating limit label and text field */
        JLabel limitLabel = new JLabel("Limit:");
        JTextField limitField = new JTextField("5");
        Dimension fieldDimension = new Dimension((int) Math.round(screenSize.width*0.06), (int) Math.round(screenSize.height*0.02));
        limitField.setPreferredSize(fieldDimension);
        limitField.setMaximumSize(fieldDimension);
        limitField.setMinimumSize(fieldDimension);
        /* Creating submit button */
        submit = new JButton("Fetch Data");
        submit.addActionListener(this);
        /* Adding everything to filter panel */
        filterPanel.add(minLevelLabel);
        filterPanel.add(minLevel);
        filterPanel.add(limitLabel);
        filterPanel.add(limitField);
        filterPanel.add(submit);
        /* Adding filter panel to JFrame */
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        overallPanel.add(filterPanel, c);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(submit)) {
            //HTTP GET
        }
    }
}
