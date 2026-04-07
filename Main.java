import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.Map;

public class Main extends Application {

    private ComboBox<String> reporterFilter, artifactFilter, statusFilter, priorityFilter;
    private TextField searchField;
    private TableView<Bug> bugTable;
    private ObservableList<Bug> masterList = FXCollections.observableArrayList();
    private FilteredList<Bug> filteredList;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        // 1. Set the main background to a dark color (e.g., Dark Charcoal)
    root.setStyle("-fx-background-color: #9398a9;");
        // ================ SIDEBAR =================
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #5e739b; -fx-border-color: #e0e1ed; -fx-border-width: 0 1 0 0;");
        sidebar.setPrefWidth(220);

        Label functionsTitle = new Label(" Functions");
        functionsTitle.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: #f3f6f9;");

        Button addBugBtn = new Button("➕ Add Bug");
        Button updateDescBtn = new Button(" Update Description");
        Button updateStatusBtn = new Button(" Update Status");
        Button deleteBugBtn = new Button(" Delete Bug");

        String btnStyle = "-fx-alignment: BASELINE_LEFT; -fx-background-radius: 5;";
        for (Button b : new Button[]{addBugBtn, updateDescBtn, updateStatusBtn, deleteBugBtn}) {
            b.setMaxWidth(Double.MAX_VALUE);
            b.setStyle(btnStyle);
        }

        sidebar.getChildren().addAll(functionsTitle, new Separator(), addBugBtn, updateDescBtn, updateStatusBtn, deleteBugBtn);
        root.setLeft(sidebar);

        // ================ TOP BAR (SEARCH) =================
        /*HBox topBar = new HBox(15);
        topBar.setPadding(new Insets(15));
        topBar.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search by title or description...");
        searchField.setPrefWidth(350);
        topBar.getChildren().addAll(new Label("Search:"), searchField);
        root.setTop(topBar);*/
        // ================ TOP BAR (SEARCH) =================
HBox topBar = new HBox(15);
topBar.setPadding(new Insets(15));
topBar.setAlignment(Pos.CENTER_LEFT);

// Dark background for the top bar to match the rest of the app
topBar.setStyle("-fx-background-color: #39404c; -fx-border-color: #333; -fx-border-width: 0 0 1 0;");

// Stylize the "Search:" Label (White and Bold)
Label searchLabel = new Label("Search:");
searchLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

searchField = new TextField();
searchField.setPromptText("Search by title or description...");
searchField.setPrefWidth(350);

// Stylize the Search Field for Dark Mode
// Darker background, white text, and a subtle border
searchField.setStyle(
    "-fx-background-color: #e5e6e8; " +
    "-fx-text-fill: black; " +
    "-fx-prompt-text-fill: #888; " + // Grey placeholder text
    "-fx-border-color: #f1f0ff; " +
    "-fx-border-radius: 5; " +
    "-fx-background-radius: 5;"
);

topBar.getChildren().addAll(searchLabel, searchField);
root.setTop(topBar);

        // ================ TABLE SETUP =================
        bugTable = new TableView<>();
        setupTableColumns();
        bugTable.setPrefHeight(400); 
        bugTable.setMaxHeight(500);

        // ================ MAIN CONTENT ==============
        VBox content = new VBox(20); 
        content.setPadding(new Insets(25)); 

        Label mainTitle = new Label("🐞 Bug Tracker");
        mainTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #000d19;");

        // ================ FILTER PANEL =================
        HBox filterBox = new HBox(20); 
        filterBox.setPadding(new Insets(15));
        filterBox.setAlignment(Pos.BOTTOM_LEFT); 
        filterBox.setStyle("-fx-background-color: #405a87; -fx-background-radius: 10; -fx-border-color: #d1d9e6; -fx-border-radius: 10;");

        // 1. Create ComboBoxes first
        statusFilter = createFilterCombo("Status", "All", "OPEN", "In Progress", "Resolved");
        priorityFilter = createFilterCombo("Priority", "All", "LOW", "MEDIUM", "HIGH");
        artifactFilter = new ComboBox<>();
        reporterFilter = new ComboBox<>();
        
        // Give them uniform width
        statusFilter.setPrefWidth(120);
        priorityFilter.setPrefWidth(120);
        artifactFilter.setPrefWidth(120);
        reporterFilter.setPrefWidth(120);

        // 2. Create Reset Button
        Button clearBtn = new Button("🔄 Reset");
        clearBtn.setOnAction(e -> resetFilters());

       // 3. Create Stylized Labels and Wrap in Vertical Groups
// Common style string for consistency
String labelStyle = "-fx-text-fill: white; -fx-font-weight: bold;";

Label statusLbl = new Label("Status");
statusLbl.setStyle(labelStyle);

Label priorityLbl = new Label("Priority");
priorityLbl.setStyle(labelStyle);

Label artifactLbl = new Label("Artifact");
artifactLbl.setStyle(labelStyle);

Label reporterLbl = new Label("Reporter");
reporterLbl.setStyle(labelStyle);

// Empty label for the reset button group to keep alignment
Label emptyLbl = new Label(" "); 

VBox statusGroup = new VBox(5, statusLbl, statusFilter);
VBox priorityGroup = new VBox(5, priorityLbl, priorityFilter);
VBox artifactGroup = new VBox(5, artifactLbl, artifactFilter);
VBox reporterGroup = new VBox(5, reporterLbl, reporterFilter);
VBox clearGroup = new VBox(5, emptyLbl, clearBtn); 

filterBox.getChildren().addAll(statusGroup, priorityGroup, artifactGroup, reporterGroup, clearGroup);
        // Assemble Content
        content.getChildren().addAll(mainTitle, filterBox, bugTable);
        root.setCenter(content);

        // Data Init
        refreshData(); 
        filteredList = new FilteredList<>(masterList, b -> true);
        bugTable.setItems(filteredList);

        // ================ LISTENERS & ACTIONS =================
        searchField.textProperty().addListener((o, old, newVal) -> applyFilters());
        statusFilter.setOnAction(e -> applyFilters());
        priorityFilter.setOnAction(e -> applyFilters());
        artifactFilter.setOnAction(e -> applyFilters());
        reporterFilter.setOnAction(e -> applyFilters());

        bugTable.setRowFactory(tv -> {
            TableRow<Bug> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2)
                    showBugDetails(row.getItem());
            });
            return row;
        });

        addBugBtn.setOnAction(e -> openAddBugWindow());
        updateDescBtn.setOnAction(e -> handleSelectedAction(this::openUpdateDescriptionWindow));
        updateStatusBtn.setOnAction(e -> handleSelectedAction(this::openUpdateStatusWindow));
        deleteBugBtn.setOnAction(e -> handleSelectedAction(this::openDeleteBugWindow));

        Scene scene = new Scene(root, 1150, 750);
        stage.setScene(scene);
        stage.setTitle("Professional Bug Tracker");
        stage.show();
    }

    // ================= HELPER LOGIC =================

    private void handleSelectedAction(java.util.function.Consumer<Bug> action) {
        Bug selected = bugTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a bug from the table first.");
            alert.show();
            return;
        }
        action.accept(selected);
    }

    private void refreshData() {
        try {
            masterList.setAll(CRUD.getAllBugs());
            artifactFilter.getItems().setAll("All");
            artifactFilter.getItems().addAll(CRUD.getAllArtifactTypes());
            artifactFilter.setValue("All");

            reporterFilter.getItems().setAll("All");
            Map<String, String> reporters = CRUD.getReporterMap();
            reporters.forEach((id, name) -> reporterFilter.getItems().add(id + " - " + name));
            reporterFilter.setValue("All");
        } catch (Exception e) {
            System.err.println("Database Refresh Failed: " + e.getMessage());
        }
    }

    private void applyFilters() {
        String keyword = searchField.getText().toLowerCase();
        String status = statusFilter.getValue();
        String priority = priorityFilter.getValue();
        String artifact = artifactFilter.getValue();
        String reporter = reporterFilter.getValue();

        filteredList.setPredicate(bug -> {
            boolean matchesSearch = keyword.isEmpty() || 
                bug.getTitle().toLowerCase().contains(keyword) || 
                bug.getDescription().toLowerCase().contains(keyword);
            boolean matchesStatus = "All".equals(status) || bug.getStatus().equalsIgnoreCase(status);
            boolean matchesPriority = "All".equals(priority) || bug.getPriority().equalsIgnoreCase(priority);
            boolean matchesArtifact = "All".equals(artifact) || bug.getArtifactType().equals(artifact);
            boolean matchesReporter = "All".equals(reporter) || (reporter != null && bug.getReportedBy().equals(reporter.split(" - ")[0]));
            return matchesSearch && matchesStatus && matchesPriority && matchesArtifact && matchesReporter;
        });
    }

    private void resetFilters() {
        searchField.clear();
        statusFilter.setValue("All");
        priorityFilter.setValue("All");
        artifactFilter.setValue("All");
        reporterFilter.setValue("All");
        applyFilters();
    }

    private ComboBox<String> createFilterCombo(String label, String... items) {
        ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList(items));
        combo.setValue(items[0]);
        return combo;
    }

    private void setupTableColumns() {
        TableColumn<Bug, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("bugId"));
        idCol.setPrefWidth(50);

        TableColumn<Bug, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Bug, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setCellFactory(tc -> {
            TableCell<Bug, String> cell = new TableCell<>();
            Text text = new Text();
            text.wrappingWidthProperty().bind(descCol.widthProperty().subtract(10));
            cell.setGraphic(text);
            cell.itemProperty().addListener((obs, old, newVal) -> text.setText(newVal));
            return cell;
        });

        TableColumn<Bug, String> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));

        TableColumn<Bug, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        bugTable.getColumns().addAll(idCol, titleCol, descCol, priorityCol, statusCol);
        bugTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // ================= WINDOWS =================

    private void openAddBugWindow() {
        Stage popup = new Stage();
        popup.setTitle("Add New Bug"); // Window title bar
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        TextField titleIn = new TextField();
        TextArea descIn = new TextArea(); descIn.setPrefRowCount(3);
        TextField artIn = new TextField();
        ComboBox<String> repIn = new ComboBox<>();
        try { repIn.getItems().addAll(CRUD.getReporterMap().keySet()); } catch (Exception e) {}
        Button save = new Button("Save Bug");
        save.setOnAction(e -> {
            try {
                CRUD.addBugFromUI(titleIn.getText(), descIn.getText(), artIn.getText(), repIn.getValue(), "MEDIUM");
                refreshData();
                popup.close();
            } catch (Exception ex) { new Alert(Alert.AlertType.ERROR, ex.getMessage()).show(); }
        });
        layout.getChildren().addAll(new Label("Title"), titleIn, new Label("Description"), descIn, new Label("Artifact"), artIn, new Label("Reporter"), repIn, save);
        popup.setScene(new Scene(layout, 350, 450));
        popup.show();
    }

    private void openUpdateStatusWindow(Bug selectedBug) {
        Stage popup = new Stage();
        popup.setTitle("Update Bug Status"); // Window title bar
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        Label info = new Label("Updating Status for: " + selectedBug.getTitle());
        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList("OPEN", "In Progress", "Resolved"));
        statusBox.setValue(selectedBug.getStatus());
        Button update = new Button("Confirm Change");
        update.setOnAction(e -> {
            try {
                CRUD.updateStatus(selectedBug.getTitle(), statusBox.getValue());
                refreshData();
                popup.close();
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        layout.getChildren().addAll(info, statusBox, update);
        popup.setScene(new Scene(layout, 300, 150));
        popup.show();
    }

    private void openUpdateDescriptionWindow(Bug selectedBug) {
        Stage popup = new Stage();
        popup.setTitle("Update Bug Description"); // Window title bar
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        Label info = new Label("New description for: " + selectedBug.getTitle());
        TextArea descArea = new TextArea(selectedBug.getDescription());
        Button update = new Button("Update Description");
        update.setOnAction(e -> {
            try {
                CRUD.updateDescription(selectedBug.getTitle(), descArea.getText());
                refreshData();
                popup.close();
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        layout.getChildren().addAll(info, descArea, update);
        popup.setScene(new Scene(layout, 400, 300));
        popup.show();
    }

    private void openDeleteBugWindow(Bug selectedBug) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete bug: " + selectedBug.getTitle() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    CRUD.deleteBugByTitle(selectedBug.getTitle());
                    refreshData();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });
    }

    private void showBugDetails(Bug bug) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bug Details");
        alert.setHeaderText("Bug #" + bug.getBugId() + ": " + bug.getTitle());
        alert.setContentText(String.format("Description: %s\n\nArtifact: %s\nPriority: %s\nStatus: %s\nReported By: %s",
            bug.getDescription(), bug.getArtifactType(), bug.getPriority(), bug.getStatus(), bug.getReportedBy()));
        alert.showAndWait();
    }

    public static void main(String[] args) { launch(); }
}