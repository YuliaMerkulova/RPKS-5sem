package com.example.lab77;

import javafx.fxml.FXML;

import javafx.fxml.Initializable;

import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.google.gson.Gson;


public class HelloController implements Initializable {
    @FXML
    public TabPane tabPanel;
    @FXML
    public Button newTabBtn;

    @FXML
    private ScrollPane scrollPane;


    @FXML
    private TextField addressBar;

    @FXML
    private MenuButton menuBtn;

    private HashMap<Tab, WebView> webViewMap = new HashMap<>();
    private HashMap<Tab, VBox> htmlViewMap = new HashMap<>();

    private HashSet<String> exceptionHistory = new HashSet<>();
    private Boolean isWriteHistory = true;
    private SingleSelectionModel<Tab> selectionModel;

    public Gson gson = new Gson();
    public void writeGsonHistory(Date date_, Long period_, String URL_){
        try(FileWriter history = new FileWriter("History.json", true);
        BufferedWriter bufferedWriter = new BufferedWriter(history);){
        InfoLog info = new InfoLog(date_, period_, URL_);
        gson.toJson(info, bufferedWriter);
        bufferedWriter.newLine();}
        catch (IOException e) {
           System.out.println("Не удалось записать историю.");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectionModel = tabPanel.getSelectionModel();
        tabPanel.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        try(FileReader fr = new FileReader("Favorite.txt");) {
            BufferedReader reader = new BufferedReader(fr);
            while (reader.ready()) {
                String line = reader.readLine();
                String urlLine = line.substring(0, line.indexOf(' '));
                String titleLine = line.substring(line.indexOf(' ') + 1);
                MenuItem name = new MenuItem(titleLine);
                menuBtn.getItems().add(name);
                name.setOnAction(ev -> {
                    createNewTab(urlLine);
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        createNewTab("https://www.google.ru/");
    }

    public void createNewTabAddr(){
        createNewTab(addressBar.getText());
    }
    public void createNewTabBtn() {
        createNewTab("https://www.google.ru/");
    }
    public void onOrOffHistory(){
        isWriteHistory = !isWriteHistory;
    }
    public void createNewTabHTML()
    {
        Tab newTab = new Tab("MY");
        newTab.setOnClosed(event -> {
            htmlViewMap.remove(newTab);
            if (htmlViewMap.isEmpty())
            {
                scrollPane.setContent(null);
            }
        });
        WebView newWebView = new WebView();
        WebEngine webEngine = newWebView.getEngine();
        newTab.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                scrollPane.setContent(htmlViewMap.get(newTab));
                addressBar.setText("");
            }
        });
        TextArea text_ = new TextArea();
        text_.textProperty().addListener((observable, oldValue, newValue) -> webEngine.loadContent(newValue));
        VBox vbox = new VBox(text_, newWebView);
        htmlViewMap.put(newTab, vbox);
        tabPanel.getTabs().add(newTab);
        scrollPane.setContent(vbox);
        selectionModel.select(newTab);

    }
    public void createContextMenu(WebView newWebView, WebEngine webEngine){
        ContextMenu contextMenu = new ContextMenu();
        MenuItem reload = new MenuItem("Reload");
        MenuItem savePage = new MenuItem("Save Page");
        reload.setOnAction(e -> newWebView.getEngine().reload());
        MenuItem goBack = new MenuItem("Back");
        MenuItem goForward = new MenuItem("Forward");
        MenuItem showHTML = new MenuItem("ShowHTML");
        MenuItem addFav = new MenuItem("AddToFavorite");
        MenuItem historyOff = new MenuItem("History Off");
        savePage.setOnAction(e -> {
            try {
                FileOutputStream zipFile = new FileOutputStream("D:\\5sem\\" + newWebView.getEngine().getTitle() + ".zip");
                ZipOutputStream zip = new ZipOutputStream(zipFile);
                zip.putNextEntry(new ZipEntry("Page.html"));
                InputStream stream = new URL(newWebView.getEngine().getLocation()).openStream();
                stream.transferTo(zip);
                stream.close();
                zip.close();
            }
            catch(Exception ex){
                System.out.println(ex.getMessage());
            } });

        historyOff.setOnAction( event -> {
            exceptionHistory.add(webEngine.getLocation());
        });

        addFav.setOnAction(e -> {
            String nameURL = webEngine.getLocation();
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter("Favorite.txt", true));
                MenuItem name = new MenuItem(webEngine.getTitle());
                menuBtn.getItems().add(name);
                name.setOnAction(ev -> {
                    createNewTab(nameURL);
                });
                writer.write(nameURL);
                writer.write(" ");
                writer.write(webEngine.getTitle());
                writer.write("\n");
                writer.flush();
                writer.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        showHTML.setOnAction(e -> {
            TextArea text_ = new TextArea();
            text_.setText(((String) webEngine.executeScript("document.documentElement.outerHTML")));

            text_.textProperty().addListener((observable, oldValue, newValue) -> webEngine.loadContent(newValue));

            HBox newHBox = new HBox(text_, newWebView);
            scrollPane.setContent(newHBox);


        });
        goBack.setOnAction(e -> {
            try{
                webEngine.getHistory().go(-1);
            }catch (IndexOutOfBoundsException er) {
                return;
            }});
        goForward.setOnAction(e -> {
            try{
                webEngine.getHistory().go(1);
            }catch (IndexOutOfBoundsException er) {
                return;
            }});
        contextMenu.getItems().addAll(reload, savePage, goBack, goForward, showHTML, addFav, historyOff);
        newWebView.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(newWebView, e.getScreenX(), e.getScreenY());
            } else {
                contextMenu.hide();
            }
        });

    }
    public void createNewTab(String address) {
        Tab newTab = new Tab("");
        newTab.setOnClosed(event -> {
            int indexEntry = webViewMap.get(newTab).getEngine().getHistory().getCurrentIndex();
            List<WebHistory.Entry> my_ = webViewMap.get(newTab).getEngine().getHistory().getEntries();
            if (!exceptionHistory.contains(my_.get(indexEntry).getUrl()) && isWriteHistory) {
                Date d = new Date();
                writeGsonHistory(my_.get(indexEntry).getLastVisitedDate(),
                        (d.getTime() - my_.get(indexEntry).getLastVisitedDate().getTime()) / 1000,  my_.get(indexEntry).getUrl());
                System.out.println((d.getTime() - my_.get(indexEntry).getLastVisitedDate().getTime()) / 1000 + "s " + " " + my_.get(indexEntry).getUrl());
            }
            webViewMap.remove(newTab);
            if (webViewMap.isEmpty())
            {
                scrollPane.setContent(null);
            }
        });
        WebView newWebView = new WebView();
        WebEngine webEngine = newWebView.getEngine();
        webViewMap.put(newTab, newWebView);
        newWebView.setContextMenuEnabled(false);
        createContextMenu(newWebView, webEngine);
        newTab.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                scrollPane.setContent(webViewMap.get(newTab));
                addressBar.setText(webViewMap.get(newTab).getEngine().getLocation());
            }
        });
        webEngine.load(address);
        webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
            if (isWriteHistory){
                int indexEntry = webEngine.getHistory().getCurrentIndex();
                List<WebHistory.Entry> my_ = webEngine.getHistory().getEntries();
                if (!exceptionHistory.contains(my_.get(indexEntry).getUrl())) {
                    Date d = new Date();
                    writeGsonHistory(my_.get(indexEntry).getLastVisitedDate(),
                            (d.getTime() - my_.get(indexEntry).getLastVisitedDate().getTime()) / 1000,  my_.get(indexEntry).getUrl());
                    System.out.println((d.getTime() - my_.get(indexEntry).getLastVisitedDate().getTime()) / 1000 + "s " + " " + my_.get(indexEntry).getUrl());
                    addressBar.setText(newValue);
                }
            }
        });
        ImageView iv = new ImageView(new javafx.scene.image.Image("http://favicon.yandex.net/favicon/"+webEngine.getLocation(), true));
        newTab.setGraphic(iv);
        tabPanel.getTabs().add(newTab);
        scrollPane.setContent(newWebView);
        selectionModel.selectLast();
    }


}
