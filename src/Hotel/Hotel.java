package Hotel;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;

// 'extends Application' tells Java this is a GUI app, not a console script
public class Hotel extends Application {

    private HotelSystem system = new HotelSystem();

    // StackPane lets us stack UI elements. We use it to quickly swap out the middle of the screen.
    private StackPane contentArea = new StackPane();
    private Label statusBar = new Label("Ready");
    private Button[] navButtons = new Button[6];

    // Local list just to track who is checked in right now for the UI grid
    private ArrayList<String[]> checkedInGuests = new ArrayList<>();

    // The start() method is where JavaFX begins building the window
    @Override
    public void start(Stage stage) {
        DatabaseHelper.createTables(); // Ensure DB exists so it doesn't crash

        // BorderPane gives us top, bottom, left, right, and center zones to place UI elements
        BorderPane root = new BorderPane();
        root.setLeft(buildSidebar());
        root.setCenter(contentArea);

        HBox statusRow = new HBox(statusBar);
        statusRow.getStyleClass().add("status-bar-wrapper");
        statusBar.getStyleClass().add("status-bar");
        root.setBottom(statusRow);

        showDashboard();

        Scene mainScene = new Scene(root, 1100, 700);
        mainScene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        Scene loginScene = buildLoginScene(stage, mainScene);

        stage.setTitle("Hotel Management System");
        stage.setScene(loginScene); // Force the user to see the login screen first
        stage.setMinWidth(900); // Stop them from making the window too small
        stage.setMinHeight(580);
        stage.show();
    }

    // =========================================================================
    // AUTHENTICATION MODULE
    // =========================================================================
    private Scene buildLoginScene(Stage stage, Scene mainScene) {
        VBox loginLayout = new VBox(20);
        loginLayout.setAlignment(Pos.CENTER);
        loginLayout.setStyle("-fx-background-color: #f4f6f8;");

        VBox loginCard = new VBox(15);
        loginCard.getStyleClass().add("content-card");
        loginCard.setMaxWidth(350);
        loginCard.setPadding(new Insets(40));
        loginCard.setAlignment(Pos.CENTER);

        Label title = new Label("Hotel System Login");
        title.getStyleClass().add("page-title");

        Label subTitle = new Label("Please enter your credentials");
        subTitle.getStyleClass().add("page-subtitle");
        subTitle.setPadding(new Insets(0, 0, 20, 0));

        TextField userField = new TextField();
        userField.setPromptText("Username");
        userField.getStyleClass().add("styled-field");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.getStyleClass().add("styled-field");

        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        Button loginBtn = new Button("Login");
        loginBtn.getStyleClass().add("btn-primary");
        loginBtn.setPrefWidth(Double.MAX_VALUE);

        // Lambda expression handling the click event
        loginBtn.setOnAction(e -> {
            String user = userField.getText().trim();
            String pass = passField.getText().trim();

            // Hardcoded check just to meet the project requirement
            if (user.equals("admin") && pass.equals("admin")) {
                stage.setScene(mainScene); // Success! Move to dashboard.
            } else {
                errorLbl.setText("Invalid username or password.");
            }
        });

        loginCard.getChildren().addAll(title, subTitle, userField, passField, loginBtn, errorLbl);
        loginLayout.getChildren().add(loginCard);

        Scene scene = new Scene(loginLayout, 1100, 700);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        return scene;
    }

    // =========================================================================
    // NAVIGATION & LAYOUT
    // =========================================================================
    private VBox buildSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);

        VBox logoBox = new VBox(4);
        logoBox.getStyleClass().add("sidebar-logo");
        logoBox.setPadding(new Insets(28, 20, 20, 20));

        Label logoMark = new Label("⬡");
        logoMark.getStyleClass().add("logo-mark");

        Label logoName = new Label("Hotel System");
        logoName.getStyleClass().add("logo-name");

        logoBox.getChildren().addAll(logoMark, logoName);

        Separator sep = new Separator();
        sep.getStyleClass().add("sidebar-sep");

        Label navLabel = new Label("NAVIGATION");
        navLabel.getStyleClass().add("sidebar-nav-label");
        VBox.setMargin(navLabel, new Insets(16, 20, 8, 20));

        // 2D Array pairing button names with their icons
        String[][] navItems = {
                {"Dashboard", "◈"},
                {"Room Manager", "⊞"},
                {"Reservations", "⊟"},
                {"Check-In/Out", "⊠"},
                {"Payments", "◎"},
                {"Reception Chat", "◫"}
        };

        // Method References (::) let us point to methods without actually firing them yet
        Runnable[] actions = {
                this::showDashboard,
                this::showRooms,
                this::showReservations,
                this::showCheckInOut,
                this::showPayments,
                this::showChat
        };

        VBox navList = new VBox(2);
        navList.setPadding(new Insets(0, 10, 0, 10));

        // Generate the buttons dynamically
        for (int i = 0; i < navItems.length; i++) {
            final int idx = i;
            HBox navRow = new HBox(10);
            navRow.setAlignment(Pos.CENTER_LEFT);
            navRow.getStyleClass().add("nav-item");
            navRow.setPadding(new Insets(10, 12, 10, 14));

            Label icon = new Label(navItems[i][1]);
            icon.getStyleClass().add("nav-icon");

            Label text = new Label(navItems[i][0]);
            text.getStyleClass().add("nav-text");

            navRow.getChildren().addAll(icon, text);
            navRow.setOnMouseClicked(e -> {
                setActiveNav(idx); // Make it grey
                actions[idx].run(); // Load the page
            });

            // Store the row in an array so we can loop over it later to clear styles
            navButtons[i] = new Button();
            navButtons[i].setUserData(navRow);
            navList.getChildren().add(navRow);
        }

        // Invisible spacer pushes the version text to the bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label version = new Label("v2.0  ·  Luxury Edition");
        version.getStyleClass().add("sidebar-version");
        VBox.setMargin(version, new Insets(0, 0, 16, 20));

        sidebar.getChildren().addAll(logoBox, sep, navLabel, navList, spacer, version);
        return sidebar;
    }

    private void setActiveNav(int idx) {
        for (int i = 0; i < navButtons.length; i++) {
            HBox row = (HBox) navButtons[i].getUserData();
            row.getStyleClass().remove("nav-item-active");
        }
        HBox active = (HBox) navButtons[idx].getUserData();
        active.getStyleClass().add("nav-item-active");
    }

    // =========================================================================
    // UTILITY METHODS
    // =========================================================================

    // Quick helpers to keep the code clean and prevent typing out full Alert boxes every time
    private void show(javafx.scene.Node view) {
        contentArea.getChildren().setAll(view);
    }

    private void status(String msg) {
        statusBar.setText(msg);
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private HBox buildPageBanner(String title, String subtitle) {
        HBox banner = new HBox();
        banner.getStyleClass().add("page-banner");
        banner.setPadding(new Insets(28, 36, 24, 36));
        banner.setAlignment(Pos.CENTER_LEFT);

        VBox textBox = new VBox(4);
        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("page-title");

        Label subLbl = new Label(subtitle);
        subLbl.getStyleClass().add("page-subtitle");

        textBox.getChildren().addAll(titleLbl, subLbl);
        banner.getChildren().add(textBox);
        return banner;
    }

    // Wraps elements in a white box with a shadow
    private VBox card(javafx.scene.Node... children) {
        VBox box = new VBox(14);
        box.getStyleClass().add("content-card");
        box.setPadding(new Insets(22, 26, 22, 26));
        box.getChildren().addAll(children);
        return box;
    }

    private Label cardLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("card-section-label");
        return l;
    }

    // =========================================================================
    // DASHBOARD VIEW
    // =========================================================================
    private void showDashboard() {
        setActiveNav(0);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("main-scroll");

        VBox page = new VBox(0);
        page.getChildren().add(buildPageBanner("Dashboard", "Welcome back — here's today's overview"));

        VBox body = new VBox(20);
        body.setPadding(new Insets(28, 36, 36, 36));

        ArrayList<Room> rooms = system.getAllRooms();
        ArrayList<Reservation> rsvns = system.getAllReservations();

        // Using Java Streams here to quickly count available rooms without writing a massive loop
        long available = rooms.stream().filter(Room::isAvailable).count();
        long occupied = rooms.size() - available;
        double revenue = rsvns.stream().mapToDouble(Reservation::getTotalPrice).sum();

        HBox statsRow = new HBox(16);
        statsRow.getChildren().addAll(
                buildStatCard("Total Rooms", String.valueOf(rooms.size()), "◈"),
                buildStatCard("Available", String.valueOf(available), "◉"),
                buildStatCard("Occupied", String.valueOf(occupied), "◍"),
                buildStatCard("Reservations", String.valueOf(rsvns.size()), "⊟"),
                buildStatCard("Revenue", "$" + String.format("%.0f", revenue), "◎")
        );
        for (javafx.scene.Node n : statsRow.getChildren()) {
            HBox.setHgrow(n, Priority.ALWAYS); // Stretch evenly
        }

        VBox quickCard = card(cardLabel("Quick Actions"), buildQuickActions());

        VBox roomsCard = card(cardLabel("All Rooms"), buildRoomTableView(rooms));
        ((TableView<?>) roomsCard.getChildren().get(1)).setPrefHeight(200);

        VBox rsvnCard = card(cardLabel("Recent Reservations"), buildReservationTableView(rsvns));
        ((TableView<?>) rsvnCard.getChildren().get(1)).setPrefHeight(180);

        body.getChildren().addAll(statsRow, quickCard, roomsCard, rsvnCard);
        page.getChildren().add(body);

        scroll.setContent(page);
        show(scroll);
        status("Dashboard loaded");
    }

    private VBox buildStatCard(String label, String value, String icon) {
        VBox card = new VBox(6);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(18, 20, 18, 20));
        card.setAlignment(Pos.TOP_LEFT);

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);
        Label iconLbl = new Label(icon);
        iconLbl.getStyleClass().add("stat-icon");
        top.getChildren().add(iconLbl);

        Label valueLbl = new Label(value);
        valueLbl.getStyleClass().add("stat-value");

        Label labelLbl = new Label(label.toUpperCase());
        labelLbl.getStyleClass().add("stat-label");

        card.getChildren().addAll(top, valueLbl, labelLbl);
        return card;
    }

    private HBox buildQuickActions() {
        Button qaRoom = new Button("+ Add Room");
        Button qaBook = new Button("+ New Booking");
        Button qaCI = new Button("→ Check In");
        Button qaCO = new Button("← Check Out");

        qaRoom.getStyleClass().addAll("btn-primary");
        qaBook.getStyleClass().addAll("btn-primary");
        qaCI.getStyleClass().addAll("btn-primary");
        qaCO.getStyleClass().addAll("btn-outline");

        qaRoom.setOnAction(e -> { setActiveNav(1); showRooms(); });
        qaBook.setOnAction(e -> { setActiveNav(2); showReservations(); });
        qaCI.setOnAction(e -> { setActiveNav(3); showCheckInOut(); });
        qaCO.setOnAction(e -> { setActiveNav(3); showCheckInOut(); });

        HBox row = new HBox(10, qaRoom, qaBook, qaCI, qaCO);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // =========================================================================
    // ROOM MANAGER VIEW
    // =========================================================================
    private void showRooms() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("main-scroll");

        VBox page = new VBox(0);
        page.getChildren().add(buildPageBanner("Room Manager", "Add, search and manage your property rooms"));

        VBox body = new VBox(20);
        body.setPadding(new Insets(28, 36, 36, 36));

        TextField ridTF = styledField("Room number  (e.g. 201)");
        TextField typeTF = styledField("Type  (Single / Double / Suite)");
        TextField priceTF = styledField("Price per night  (e.g. 150)");
        Button addBtn = new Button("Add Room");
        addBtn.getStyleClass().add("btn-primary");
        Label addMsg = new Label();
        addMsg.getStyleClass().add("success-msg");

        GridPane addForm = formGrid();
        addRow(addForm, "Room Number", ridTF, 0);
        addRow(addForm, "Type", typeTF, 1);
        addRow(addForm, "Price/Night", priceTF, 2);

        addBtn.setOnAction(e -> {
            try {
                int id = Integer.parseInt(ridTF.getText().trim());
                String type = typeTF.getText().trim();
                double price = Double.parseDouble(priceTF.getText().trim());

                if (type.isEmpty()) {
                    showError("Room type cannot be empty.");
                    return;
                }
                if (price <= 0) {
                    showError("Price must be greater than zero.");
                    return;
                }
                system.addRoom(new Room(id, type, price));
                addMsg.setText("✓  Room " + id + " added successfully.");

                ridTF.clear(); typeTF.clear(); priceTF.clear();
                status("Room " + id + " added");
                showRooms();
                // Catching NumberFormatException stops the app from crashing if they type letters instead of numbers
            } catch (NumberFormatException ex) {
                showError("Room Number and Price must be valid numbers.");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        VBox addCard = card(cardLabel("Add New Room"), addForm, addBtn, addMsg);

        TextField searchIdTF = styledField("Search by room number");
        TextField searchTypeTF = styledField("Search by type");
        Button searchBtn = new Button("Search");
        Button sortBtn = new Button("Sort by Price");
        Button showAllBtn = new Button("Show All");
        searchBtn.getStyleClass().add("btn-primary");
        sortBtn.getStyleClass().add("btn-ghost");
        showAllBtn.getStyleClass().add("btn-ghost");

        HBox searchRow = new HBox(10, searchIdTF, searchTypeTF, searchBtn, sortBtn, showAllBtn);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        TableView<Room> resultTable = buildRoomTableView(system.getAllRooms());
        resultTable.setPrefHeight(260);

        searchBtn.setOnAction(e -> {
            String idStr = searchIdTF.getText().trim();
            String typStr = searchTypeTF.getText().trim();
            try {
                if (!idStr.isEmpty()) {
                    Room r = system.searchRoom(Integer.parseInt(idStr));
                    ArrayList<Room> res = new ArrayList<>();
                    res.add(r);
                    resultTable.getItems().setAll(res); // Shows just one room
                } else if (!typStr.isEmpty()) {
                    ArrayList<Room> res = system.searchRoom(typStr);
                    if (res.isEmpty()) showError("No rooms found of type: " + typStr);
                    else resultTable.getItems().setAll(res); // Shows all rooms of that type
                } else {
                    resultTable.getItems().setAll(system.getAllRooms());
                }
            } catch (RoomNotFoundException ex) {
                showError(ex.getMessage());
            } catch (NumberFormatException ex) {
                showError("Room number must be a number.");
            }
        });

        sortBtn.setOnAction(e -> {
            system.sortRoomsByPrice();
            resultTable.getItems().setAll(system.getAllRooms());
        });

        showAllBtn.setOnAction(e -> resultTable.getItems().setAll(system.getAllRooms()));

        VBox searchCard = card(cardLabel("Search Rooms"), searchRow, resultTable);

        body.getChildren().addAll(addCard, searchCard);
        page.getChildren().add(body);

        scroll.setContent(page);
        show(scroll);
        status("Room Manager loaded");
    }

    // =========================================================================
    // RESERVATIONS VIEW
    // =========================================================================
    private void showReservations() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("main-scroll");

        VBox page = new VBox(0);
        page.getChildren().add(buildPageBanner("Reservations", "Create and manage guest bookings"));

        VBox body = new VBox(20);
        body.setPadding(new Insets(28, 36, 36, 36));

        TextField gNameTF = styledField("Guest full name");
        TextField gPhoneTF = styledField("Phone number");
        TextField roomTF = styledField("Room number");
        TextField nightsTF = styledField("Number of nights");

        GridPane bForm = formGrid();
        addRow(bForm, "Guest Name", gNameTF, 0);
        addRow(bForm, "Phone", gPhoneTF, 1);
        addRow(bForm, "Room Number", roomTF, 2);
        addRow(bForm, "Nights", nightsTF, 3);

        Label costPreview = new Label();
        costPreview.getStyleClass().add("cost-preview");

        Button confirmBtn = new Button("Confirm Booking");
        confirmBtn.getStyleClass().add("btn-primary");
        Label bookMsg = new Label();
        bookMsg.getStyleClass().add("success-msg");

        // Dynamically calculates the estimated cost as the user types
        Runnable updateCost = () -> {
            try {
                int rn = Integer.parseInt(roomTF.getText().trim());
                int nts = Integer.parseInt(nightsTF.getText().trim());
                Room rm = system.searchRoom(rn);
                double tot = rm.getPrice() * nts;
                costPreview.setText("Estimated total: $" + String.format("%.2f", tot)
                        + "  (" + nts + " nights × $" + rm.getPrice() + ")");
            } catch (Exception ignored) {
                costPreview.setText("");
            }
        };

        roomTF.textProperty().addListener((o, ov, nv) -> updateCost.run());
        nightsTF.textProperty().addListener((o, ov, nv) -> updateCost.run());

        confirmBtn.setOnAction(e -> {
            try {
                String name = gNameTF.getText().trim();
                String phone = gPhoneTF.getText().trim();
                int rn = Integer.parseInt(roomTF.getText().trim());
                int nts = Integer.parseInt(nightsTF.getText().trim());

                if (name.isEmpty() || phone.isEmpty()) {
                    showError("Guest name and phone are required.");
                    return;
                }
                if (nts <= 0) {
                    showError("Nights must be at least 1.");
                    return;
                }

                Room roomToBook = system.searchRoom(rn);
                if (!roomToBook.isAvailable()) {
                    showError("Room " + rn + " is already booked.");
                    return;
                }

                Guest guest = new Guest(name, phone);
                Reservation res = new Reservation(guest, roomToBook, nts);
                system.addReservation(res);

                bookMsg.setText("✓  Booking confirmed  —  Total: $" + String.format("%.2f", res.getTotalPrice()));

                gNameTF.clear(); gPhoneTF.clear(); roomTF.clear(); nightsTF.clear();
                costPreview.setText("");
                status("Booking confirmed for " + name + " — Room " + rn);
                showReservations();
            } catch (NumberFormatException ex) {
                showError("Room Number and Nights must be valid numbers.");
            } catch (RoomNotFoundException ex) {
                showError(ex.getMessage());
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        VBox bookingCard = card(cardLabel("New Booking"), bForm, costPreview, confirmBtn, bookMsg);

        TableView<Reservation> rsvnTable = buildReservationTableView(system.getAllReservations());
        rsvnTable.setPrefHeight(220);

        Button cancelBtn = new Button("Cancel Selected Booking");
        cancelBtn.getStyleClass().add("btn-outline");
        cancelBtn.setStyle("-fx-text-fill: #e74c3c; -fx-border-color: #e74c3c;");

        // Cancellation logic
        cancelBtn.setOnAction(e -> {
            Reservation selected = rsvnTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selected.getRoom().setAvailable(true); // Free the room up
                system.getAllReservations().remove(selected); // Delete from our list

                showInfo("Booking Cancelled", "The reservation for " + selected.getGuestName() + " has been cancelled.");
                status("Reservation Cancelled.");
                showReservations();
            } else {
                showError("Please click on a reservation in the list first.");
            }
        });

        VBox listCard = card(cardLabel("All Reservations — " + system.getAllReservations().size() + " total"), rsvnTable, cancelBtn);

        body.getChildren().addAll(bookingCard, listCard);
        page.getChildren().add(body);

        scroll.setContent(page);
        show(scroll);
        status("Reservations loaded");
    }

    // =========================================================================
    // CHECK-IN / CHECK-OUT VIEW
    // =========================================================================
    private void showCheckInOut() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("main-scroll");

        VBox page = new VBox(0);
        page.getChildren().add(buildPageBanner("Check-In / Check-Out", "Manage guest arrivals and departures"));

        VBox body = new VBox(20);
        body.setPadding(new Insets(28, 36, 36, 36));

        TextField ciNameTF = styledField("Guest name");
        TextField ciPhoneTF = styledField("Phone number");
        TextField ciRoomTF = styledField("Room number");
        TextField ciNightsTF = styledField("Number of nights");

        GridPane ciForm = formGrid();
        addRow(ciForm, "Guest Name", ciNameTF, 0);
        addRow(ciForm, "Phone", ciPhoneTF, 1);
        addRow(ciForm, "Room Number", ciRoomTF, 2);
        addRow(ciForm, "Nights", ciNightsTF, 3);

        Button ciBtn = new Button("Check In Guest");
        ciBtn.getStyleClass().add("btn-primary");
        Label ciMsg = new Label();
        ciMsg.getStyleClass().add("success-msg");

        ciBtn.setOnAction(e -> {
            try {
                String name = ciNameTF.getText().trim();
                String phone = ciPhoneTF.getText().trim();
                int roomId = Integer.parseInt(ciRoomTF.getText().trim());
                int nights = Integer.parseInt(ciNightsTF.getText().trim());

                if (name.isEmpty() || phone.isEmpty()) {
                    showError("All fields are required.");
                    return;
                }
                if (nights <= 0) {
                    showError("Nights must be at least 1.");
                    return;
                }

                Room room = system.searchRoom(roomId);
                if (!room.isAvailable()) {
                    showError("Room " + roomId + " is already occupied.");
                    return;
                }

                Guest guest = new Guest(name, phone);
                Reservation res = new Reservation(guest, room, nights);
                system.addReservation(res);

                // Add to our visual grid array
                checkedInGuests.add(new String[]{
                        name, phone, String.valueOf(roomId), String.valueOf(nights),
                        String.format("%.2f", res.getTotalPrice()), "Checked In"
                });
                ciMsg.setText("✓  " + name + " checked into Room " + roomId);

                ciNameTF.clear(); ciPhoneTF.clear(); ciRoomTF.clear(); ciNightsTF.clear();
                status(name + " checked in to Room " + roomId);
                showCheckInOut();
            } catch (NumberFormatException ex) {
                showError("Room Number and Nights must be valid numbers.");
            } catch (RoomNotFoundException ex) {
                showError(ex.getMessage());
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        VBox ciCard = card(cardLabel("Guest Check-In"), ciForm, ciBtn, ciMsg);

        // We use a GridPane here instead of a TableView so we can inject actual Buttons into the rows
        GridPane guestTable = new GridPane();
        guestTable.setHgap(20);
        guestTable.setVgap(10);
        guestTable.getStyleClass().add("guest-grid");
        guestTable.setPadding(new Insets(0));

        String[] headers = {"Name", "Phone", "Room", "Nights", "Total", "Status", ""};
        for (int i = 0; i < headers.length; i++) {
            Label h = new Label(headers[i].toUpperCase());
            h.getStyleClass().add("guest-table-header");
            guestTable.add(h, i, 0);
        }
        Separator divider = new Separator();
        guestTable.add(divider, 0, 1, headers.length, 1);

        if (checkedInGuests.isEmpty()) {
            Label empty = new Label("No guests currently checked in.");
            empty.getStyleClass().add("empty-state");
            guestTable.add(empty, 0, 2, headers.length, 1);
        } else {
            for (int i = 0; i < checkedInGuests.size(); i++) {
                String[] g = checkedInGuests.get(i);
                int row = i + 2;

                // Color the pill based on their status
                Label statusLbl = new Label(g[5]);
                statusLbl.getStyleClass().add(g[5].equals("Checked In") ? "badge-in" : "badge-out");

                guestTable.add(new Label(g[0]), 0, row);
                guestTable.add(new Label(g[1]), 1, row);
                guestTable.add(new Label(g[2]), 2, row);
                guestTable.add(new Label(g[3]), 3, row);
                guestTable.add(new Label("$" + g[4]), 4, row);
                guestTable.add(statusLbl, 5, row);

                // Add the checkout button only if they are actually in
                if (g[5].equals("Checked In")) {
                    final int idx = i;
                    Button coBtn = new Button("Check Out");
                    coBtn.getStyleClass().add("btn-checkout");

                    coBtn.setOnAction(ev -> {
                        checkedInGuests.get(idx)[5] = "Checked Out";
                        try {
                            Room rm = system.searchRoom(Integer.parseInt(g[2]));
                            rm.setAvailable(true);
                            DatabaseHelper.updateRoomAvailability(Integer.parseInt(g[2]), true);
                        } catch (Exception ignored) {
                        }

                        showInfo("Check-Out Complete",
                                g[0] + " has been checked out from Room " + g[2] + ".\nTotal charged: $" + g[4]);
                        status(g[0] + " checked out from Room " + g[2]);
                        showCheckInOut();
                    });
                    guestTable.add(coBtn, 6, row);
                } else {
                    guestTable.add(new Label("—"), 6, row);
                }
            }
        }

        VBox guestsCard = card(cardLabel("Current Guests"), guestTable);
        body.getChildren().addAll(ciCard, guestsCard);
        page.getChildren().add(body);

        scroll.setContent(page);
        show(scroll);
        status("Check-In/Out loaded");
    }

    // =========================================================================
    // PAYMENTS VIEW
    // =========================================================================
    private void showPayments() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("main-scroll");

        VBox page = new VBox(0);
        page.getChildren().add(buildPageBanner("Payments", "Process guest payments and view revenue"));

        VBox body = new VBox(20);
        body.setPadding(new Insets(28, 36, 36, 36));

        double totalRevenue = system.getAllReservations().stream().mapToDouble(Reservation::getTotalPrice).sum();
        int totalBookings = system.getAllReservations().size();
        double avgValue = totalBookings > 0 ? totalRevenue / totalBookings : 0;

        HBox revenueRow = new HBox(16);
        revenueRow.getChildren().addAll(
                buildStatCard("Total Revenue", "$" + String.format("%.2f", totalRevenue), "◎"),
                buildStatCard("Total Bookings", String.valueOf(totalBookings), "⊟"),
                buildStatCard("Avg Stay Value", "$" + String.format("%.2f", avgValue), "◈")
        );
        for (javafx.scene.Node n : revenueRow.getChildren()) {
            HBox.setHgrow(n, Priority.ALWAYS);
        }

        TextField pGuestTF = styledField("Guest name (optional)");
        TextField pTotalTF = styledField("Total amount due ($)");
        TextField pCashTF = styledField("Amount tendered ($)");

        // Group the radio buttons so only one can be checked at a time
        ToggleGroup methodGroup = new ToggleGroup();
        RadioButton cashRB = new RadioButton("Cash");
        RadioButton cardRB = new RadioButton("Card");
        cashRB.setToggleGroup(methodGroup);
        cardRB.setToggleGroup(methodGroup);
        cashRB.setSelected(true);
        cashRB.getStyleClass().add("styled-radio");
        cardRB.getStyleClass().add("styled-radio");

        HBox methodRow = new HBox(16, cashRB, cardRB);
        methodRow.setAlignment(Pos.CENTER_LEFT);

        GridPane pForm = formGrid();
        addRow(pForm, "Guest Name", pGuestTF, 0);
        addRow(pForm, "Total Due", pTotalTF, 1);
        addRow(pForm, "Amount Given", pCashTF, 2);
        addRow(pForm, "Method", methodRow, 3);

        Label changePreview = new Label();
        changePreview.getStyleClass().add("cost-preview");

        pTotalTF.textProperty().addListener((o, ov, nv) -> updateChangePreview(pTotalTF, pCashTF, changePreview));
        pCashTF.textProperty().addListener((o, ov, nv) -> updateChangePreview(pTotalTF, pCashTF, changePreview));

        Button payBtn = new Button("Process Payment");
        payBtn.getStyleClass().add("btn-primary");

        VBox receipt = new VBox(6);
        receipt.getStyleClass().add("receipt-box");

        payBtn.setOnAction(e -> {
            try {
                double total = Double.parseDouble(pTotalTF.getText().trim());
                double given = Double.parseDouble(pCashTF.getText().trim());

                if (total <= 0 || given <= 0) {
                    showError("Amounts must be greater than zero.");
                    return;
                }

                String method = cashRB.isSelected() ? "Cash" : "Card";
                Payment payment = new Payment(total, method);
                receipt.getChildren().clear();

                // Call the payment class to do the math
                if (payment.processPayment(given)) {
                    double change = payment.calculateChange(given);
                    receipt.getChildren().addAll(
                            receiptLine("─── PAYMENT RECEIPT ───────────────────"),
                            receiptLine("Guest:          " + (pGuestTF.getText().isEmpty() ? "N/A" : pGuestTF.getText())),
                            receiptLine("Method:         " + method),
                            receiptLine("Total Due:      $" + String.format("%.2f", total)),
                            receiptLine("Amount Given:   $" + String.format("%.2f", given)),
                            receiptLine("Change Due:     $" + String.format("%.2f", change)),
                            receiptLine("Status:         ✓ ACCEPTED")
                    );
                    status("Payment of $" + String.format("%.2f", total) + " processed via " + method);

                    pGuestTF.clear(); pTotalTF.clear(); pCashTF.clear();
                } else {
                    double shortfall = total - given;
                    receipt.getChildren().addAll(
                            receiptLine("─── PAYMENT FAILED ────────────────────"),
                            receiptLine("Total Due:      $" + String.format("%.2f", total)),
                            receiptLine("Amount Given:   $" + String.format("%.2f", given)),
                            receiptLine("Shortfall:      $" + String.format("%.2f", shortfall)),
                            receiptLine("Status:         ✗ INSUFFICIENT FUNDS")
                    );
                    status("Payment failed — insufficient funds");
                }
            } catch (NumberFormatException ex) {
                showError("Please enter valid numeric amounts.");
            }
        });

        VBox payCard = card(cardLabel("Process Payment"), pForm, changePreview, payBtn, receipt);

        body.getChildren().addAll(revenueRow, payCard);
        page.getChildren().add(body);

        scroll.setContent(page);
        show(scroll);
        status("Payments loaded");
    }

    private Label receiptLine(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("receipt-line");
        return l;
    }

    // =========================================================================
    // RECEPTION CHAT VIEW
    // =========================================================================
    private void showChat() {
        setActiveNav(5);
        ReceptionChat chatUI = new ReceptionChat();
        show(chatUI.buildView());
        status("Reception Chat loaded");
    }

    // =========================================================================
    // TABLE COMPONENT FACTORIES
    // =========================================================================

    // These connect the table columns directly to the variable names inside our classes
    private TableView<Room> buildRoomTableView(ArrayList<Room> rooms) {
        TableView<Room> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("styled-table");

        TableColumn<Room, Integer> numCol = new TableColumn<>("Room #");
        numCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("roomNumber"));

        TableColumn<Room, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("type"));

        TableColumn<Room, Double> priceCol = new TableColumn<>("Price/Night");
        priceCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("price"));

        TableColumn<Room, Boolean> availCol = new TableColumn<>("Available");
        availCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("available"));

        table.getColumns().addAll(numCol, typeCol, priceCol, availCol);
        table.getItems().addAll(rooms);
        return table;
    }

    private TableView<Reservation> buildReservationTableView(ArrayList<Reservation> rsvns) {
        TableView<Reservation> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("styled-table");

        TableColumn<Reservation, String> nameCol = new TableColumn<>("Guest Name");
        nameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("guestName"));

        TableColumn<Reservation, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("guestPhone"));

        TableColumn<Reservation, Integer> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("roomNumber"));

        TableColumn<Reservation, Integer> nightsCol = new TableColumn<>("Nights");
        nightsCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nights"));

        TableColumn<Reservation, Double> priceCol = new TableColumn<>("Total ($)");
        priceCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("totalPrice"));

        table.getColumns().addAll(nameCol, phoneCol, roomCol, nightsCol, priceCol);
        table.getItems().addAll(rsvns);
        return table;
    }

    // =========================================================================
    // FORM UTILITIES
    // =========================================================================

    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("styled-field");
        tf.setPrefWidth(260);
        return tf;
    }

    private GridPane formGrid() {
        GridPane g = new GridPane();
        g.setHgap(16);
        g.setVgap(12);
        return g;
    }

    private void addRow(GridPane g, String label, javafx.scene.Node field, int row) {
        Label l = new Label(label);
        l.getStyleClass().add("form-label");
        l.setMinWidth(110);
        g.add(l, 0, row);
        g.add(field, 1, row);
    }

    private void updateChangePreview(TextField totalTF, TextField cashTF, Label preview) {
        try {
            double total = Double.parseDouble(totalTF.getText().trim());
            double cash = Double.parseDouble(cashTF.getText().trim());
            double diff = cash - total;

            if (diff >= 0) preview.setText("Change to return:  $" + String.format("%.2f", diff));
            else preview.setText("Shortfall:  $" + String.format("%.2f", Math.abs(diff)));
        } catch (Exception ignored) {
            preview.setText("");
        }
    }
}