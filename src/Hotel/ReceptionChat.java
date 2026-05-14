package Hotel;
//bonus
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

// This builds our fake split-screen internal messaging panel.
public class ReceptionChat {

    // Helper to make the time look like "14:30"
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // We use two different VBoxes for the messages so we can style them independently
    // (e.g., A's sent messages go on the right, B's received messages go on the left).
    private final VBox messagesA = new VBox(8);
    private final VBox messagesB = new VBox(8);

    private final ScrollPane scrollA = new ScrollPane();
    private final ScrollPane scrollB = new ScrollPane();

    public javafx.scene.Node buildView() {

        // Builds the dark banner at the top of the page
        HBox banner = new HBox();
        banner.getStyleClass().add("page-banner");
        banner.setPadding(new Insets(28, 36, 24, 36));
        banner.setAlignment(Pos.CENTER_LEFT);

        VBox bannerText = new VBox(4);
        Label title = new Label("Reception Chat");
        title.getStyleClass().add("page-title");
        Label sub = new Label("Internal messaging between front-desk staff");
        sub.getStyleClass().add("page-subtitle");
        bannerText.getChildren().addAll(title, sub);
        banner.getChildren().add(bannerText);

        // HBox is used to place the two chat panels side-by-side
        HBox chatRow = new HBox(20);
        chatRow.setPadding(new Insets(28, 36, 36, 36));
        HBox.setHgrow(chatRow, Priority.ALWAYS); // Stretch to fill the width of the screen

        VBox panelA = buildChatPanel("Receptionist A", "A", messagesA, scrollA, "B");
        VBox panelB = buildChatPanel("Receptionist B", "B", messagesB, scrollB, "A");
        HBox.setHgrow(panelA, Priority.ALWAYS);
        HBox.setHgrow(panelB, Priority.ALWAYS);

        // This digs through our layout layers to find the exact TextFields and Buttons we just made.
        // We have to grab them so we can attach an "onClick" action to them.
        TextField fieldA = (TextField) ((HBox) ((VBox) panelA.getChildren().get(2)).getChildren().get(0)).getChildren().get(0);
        Button    btnA   = (Button)    ((HBox) ((VBox) panelA.getChildren().get(2)).getChildren().get(0)).getChildren().get(1);

        TextField fieldB = (TextField) ((HBox) ((VBox) panelB.getChildren().get(2)).getChildren().get(0)).getChildren().get(0);
        Button    btnB   = (Button)    ((HBox) ((VBox) panelB.getChildren().get(2)).getChildren().get(0)).getChildren().get(1);

        // Wire up the buttons and the Enter key so messages go from one screen to the other.
        btnA.setOnAction(e -> sendMessage(fieldA, "Receptionist A", messagesA, scrollA, messagesB, scrollB, "sent", "received"));
        fieldA.setOnAction(e -> sendMessage(fieldA, "Receptionist A", messagesA, scrollA, messagesB, scrollB, "sent", "received"));

        btnB.setOnAction(e -> sendMessage(fieldB, "Receptionist B", messagesB, scrollB, messagesA, scrollA, "sent", "received"));
        fieldB.setOnAction(e -> sendMessage(fieldB, "Receptionist B", messagesB, scrollB, messagesA, scrollA, "sent", "received"));

        chatRow.getChildren().addAll(panelA, panelB);

        // Throw in a quick system message so the chat isn't blank on startup
        addSystemMessage(messagesA, "Chat started. Messages are visible to both receptionists.");
        addSystemMessage(messagesB, "Chat started. Messages are visible to both receptionists.");

        VBox page = new VBox(0);
        page.getChildren().addAll(banner, chatRow);
        VBox.setVgrow(chatRow, Priority.ALWAYS);

        ScrollPane outer = new ScrollPane(page);
        outer.setFitToWidth(true);
        outer.getStyleClass().add("main-scroll");
        return outer;
    }

    // Helper method to build one half of the screen. We write it once and call it twice.
    private VBox buildChatPanel(String name, String initial, VBox messages, ScrollPane scroll, String otherInitial) {

        // Header with the avatar and "Online" status
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 16, 14, 16));
        header.getStyleClass().add("chat-header");

        Label avatar = new Label(initial);
        avatar.getStyleClass().add("chat-avatar");

        VBox nameBox = new VBox(2);
        Label nameLbl = new Label(name);
        nameLbl.getStyleClass().add("chat-name");
        Label statusLbl = new Label("● Online");
        statusLbl.getStyleClass().add("chat-status");
        nameBox.getChildren().addAll(nameLbl, statusLbl);

        header.getChildren().addAll(avatar, nameBox);

        // The actual chat feed area
        messages.setPadding(new Insets(14, 14, 14, 14));
        messages.setFillWidth(true);

        scroll.setContent(messages);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("chat-scroll");
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Hide horizontal scrollbar, it's ugly
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // The input box and send button at the bottom
        TextField field = new TextField();
        field.setPromptText("Type a message…");
        field.getStyleClass().add("chat-input");
        HBox.setHgrow(field, Priority.ALWAYS);

        Button sendBtn = new Button("Send");
        sendBtn.getStyleClass().add("btn-primary");

        HBox inputRow = new HBox(8, field, sendBtn);
        inputRow.setAlignment(Pos.CENTER);
        inputRow.setPadding(new Insets(10, 14, 14, 14));

        VBox inputWrap = new VBox(inputRow);
        inputWrap.getStyleClass().add("chat-input-area");

        // Stack the header, feed, and input row vertically
        VBox panel = new VBox(0);
        panel.getStyleClass().add("chat-panel");
        panel.getChildren().addAll(header, scroll, inputWrap);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        panel.setMinHeight(420);

        return panel;
    }

    // Handles exactly what happens when someone clicks Send
    private void sendMessage(TextField field, String senderName, VBox ownFeed, ScrollPane ownScroll, VBox otherFeed, ScrollPane otherScroll, String ownStyle, String otherStyle) {
        String text = field.getText().trim();
        if (text.isEmpty()) return; // Don't send blank messages

        String time = LocalTime.now().format(TIME_FMT);

        // Add a blue bubble to my screen, and a grey bubble to the other screen
        addBubble(ownFeed, text, time, null, "bubble-sent");
        addBubble(otherFeed, text, time, senderName, "bubble-received");

        field.clear();

        // Platform.runLater is required here because JavaFX wants all visual updates
        // done safely on the main thread. This forces the scrollbar down to the newest message.
        Platform.runLater(() -> {
            ownScroll.setVvalue(1.0);
            otherScroll.setVvalue(1.0);
        });
    }

    // Builds the visual bubble
    private void addBubble(VBox feed, String text, String time, String senderLabel, String styleClass) {

        VBox bubble = new VBox(3);
        bubble.getStyleClass().addAll("chat-bubble", styleClass);
        bubble.setMaxWidth(340); // Prevents long messages from stretching awkwardly across the screen

        if (senderLabel != null) {
            Label sender = new Label(senderLabel);
            sender.getStyleClass().add("bubble-sender");
            bubble.getChildren().add(sender);
        }

        Label msg = new Label(text);
        msg.getStyleClass().add("bubble-text");
        msg.setWrapText(true); // Wraps text to a new line if it hits the max width

        Label timeLbl = new Label(time);
        timeLbl.getStyleClass().add("bubble-time");

        bubble.getChildren().addAll(msg, timeLbl);

        // Align sent messages to the right, and received messages to the left
        HBox row = new HBox(bubble);
        if (styleClass.equals("bubble-sent")) {
            row.setAlignment(Pos.CENTER_RIGHT);
        } else {
            row.setAlignment(Pos.CENTER_LEFT);
        }

        feed.getChildren().add(row);
    }

    // Plops a small grey system message in the center
    private void addSystemMessage(VBox feed, String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("chat-system-msg");
        lbl.setWrapText(true);
        HBox row = new HBox(lbl);
        row.setAlignment(Pos.CENTER);
        feed.getChildren().add(row);
    }
}