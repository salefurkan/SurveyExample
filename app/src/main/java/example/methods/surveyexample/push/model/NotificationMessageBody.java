package example.methods.surveyexample.push.model;


import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


public class NotificationMessageBody {
    @SerializedName("validate_only")
    private boolean validation;
    @SerializedName("message")
    private Builder.Message message;

    public NotificationMessageBody(boolean validation, Builder.Message message) {
        this.validation = validation;
        this.message = message;
    }

    private NotificationMessageBody(Builder builder) {
    }

    public static class Builder {
        private String notificationTitle;
        private String notificationBody;
        private String[] notificationPushToken;

        public Builder(String notificationTitle, String notificationBody, String pushToken) {
            this.notificationTitle = notificationTitle;
            this.notificationBody = notificationBody;
            this.notificationPushToken = new String[1];
            this.notificationPushToken[0] = pushToken;
        }

        public NotificationMessageBody build(int choose) {
            String[] inbox_content = {
                    "1. Added parameters for the API used to send messages.",
                    "2. Supported inbox-style notification messages.",
                    "3. Supported notification messages with action buttons.",
                    "4. Added the onTokenError method, which is the callback once token obtaining failure.",
                    "5. Optimized the style of messages sent from the Push Kit console."
            };

            Button buttons = new Button(0, "LEARN MORE ");
            Button buttons2 = new Button(3, "IGNORE ");
            List<Button> buttonList = new ArrayList<Button>();
            buttonList.add(buttons);
            buttonList.add(buttons2);

            Notification notification;
            ClickAction clickAction = new ClickAction(3);
            AndroidNotification simpleAndroidNotification = new AndroidNotification(notificationTitle, notificationBody, clickAction);

            AndroidNotification largeTextAndroidNotification = new AndroidNotification(notificationTitle, notificationBody,
                    clickAction, 1, "Push Kit", "Push Kit is a messaging service provided by Huawei for developers. It establishes a messaging channel from the cloud to devices.");

            AndroidNotification inboxStyleAndroidNotification = new AndroidNotification(notificationTitle, notificationBody,
                    clickAction, 3, "HMS Push SDK 4.0.2 New Features Description", inbox_content);
            AndroidNotification actionButtonsAndroidNotification = new AndroidNotification(notificationTitle, notificationBody,
                    clickAction, buttonList, 1, "HMS Push SDK 4.0.2 New Features Description", "HMS Push SDK 4.0.2 New Features Description");

            AndroidNotification androidNotification;
            switch (choose) {
                case 0:
                    androidNotification = simpleAndroidNotification;
                    //notification = new Notification(notificationTitle, notificationBody);
                    break;
                case 1:
                    androidNotification = largeTextAndroidNotification;
                    //notification = new Notification("Push Kit", "Push Kit is a messaging service provided by Huawei for developers. It establishes a messaging channel from the cloud to devices.");
                    break;
                case 2:
                    androidNotification = inboxStyleAndroidNotification;
                    //notification = new Notification("HMS Push SDK 4.0.2 New Features Description", String.valueOf(inbox_content));
                    break;
                case 3:
                    androidNotification = actionButtonsAndroidNotification;
                    //notification = new Notification("HMS Push SDK 4.0.2 New Features Description", "HMS Push SDK 4.0.2 New Features Description");
                    break;
                default:
                    androidNotification = simpleAndroidNotification;
                    //notification = new Notification(notificationTitle, notificationBody);
                    break;
            }
            AndroidConfig androidConfig = new AndroidConfig(androidNotification);
            notification = new Notification(notificationTitle, notificationBody);
            Message message = new Message(notification, androidConfig, notificationPushToken);
            NotificationMessageBody notificationMessage =
                    new NotificationMessageBody(false, message);
            return notificationMessage;
        }

        public static class Message {
            @SerializedName("notification")
            private Notification notification;
            @SerializedName("android")
            private AndroidConfig android;
            @SerializedName("token")
            private String[] arrayToken;

            public Message(Notification notification, AndroidConfig android, String[] arrayToken) {
                this.notification = notification;
                this.android = android;
                this.arrayToken = arrayToken;
            }

            public Notification getNotification() {
                return notification;
            }

            public void setNotification(Notification notification) {
                this.notification = notification;
            }

            public AndroidConfig getAndroid() {
                return android;
            }

            public void setAndroid(AndroidConfig android) {
                this.android = android;
            }

            public String[] getToken() {
                return arrayToken;
            }

            public void setToken(String[] arrayToken) {
                this.arrayToken = arrayToken;
            }
        }

        public static class Notification {
            @SerializedName("title")
            private String title;
            @SerializedName("body")
            private String body;

            public Notification(String title, String body) {
                this.title = title;
                this.body = body;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getBody() {
                return body;
            }

            public void setBody(String body) {
                this.body = body;
            }
        }

        static class AndroidConfig {
            @SerializedName("notification")
            AndroidNotification notification;

            public AndroidConfig(AndroidNotification notification) {
                this.notification = notification;
            }

            public AndroidNotification getNotification() {
                return notification;
            }

            public void setNotification(AndroidNotification notification) {
                this.notification = notification;
            }
        }

        static class AndroidNotification {
            @SerializedName("title")
            private String title;
            @SerializedName("body")
            private String body;
            @SerializedName("click_action")
            private ClickAction clickaction;
            @SerializedName("style")
            private int style;
            @SerializedName("big_title")
            private String big_title;
            @SerializedName("big_body")
            private String big_body;
            @SerializedName("inbox_content")
            private String[] inbox_content;
            @SerializedName("buttons")
            private List<Button> buttons;


            public AndroidNotification(String title, String body, ClickAction clickAction) {
                this.title = title;
                this.body = body;
                this.clickaction = clickAction;
            }

            public AndroidNotification(String title, String body, ClickAction clickaction, int style,
                                       String big_title, String big_body) {
                this.title = title;
                this.body = body;
                this.clickaction = clickaction;
                this.style = style;
                this.big_title = big_title;
                this.big_body = big_body;
            }

            public AndroidNotification(String title, String body, ClickAction clickaction, int style,
                                       String big_title, String[] inbox_content) {
                this.title = title;
                this.body = body;
                this.clickaction = clickaction;
                this.style = style;
                this.big_title = big_title;
                this.inbox_content = inbox_content;
            }

            public AndroidNotification(String title, String body, ClickAction clickaction, List<Button> buttons, int style,
                                       String big_title, String big_body) {
                this.title = title;
                this.body = body;
                this.buttons = buttons;
                this.clickaction = clickaction;
                this.style = style;
                this.big_title = big_title;
                this.big_body = big_body;
            }


            public ClickAction getClickaction() {
                return clickaction;
            }

            public void setClickaction(ClickAction clickaction) {
                this.clickaction = clickaction;
            }

            public int getStyle() {
                return style;
            }

            public void setStyle(int style) {
                this.style = style;
            }

            public String getBig_title() {
                return big_title;
            }

            public void setBig_title(String big_title) {
                this.big_title = big_title;
            }

            public String getBig_body() {
                return big_body;
            }

            public void setBig_body(String big_body) {
                this.big_body = big_body;
            }


            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getBody() {
                return body;
            }

            public void setBody(String body) {
                this.body = body;
            }

            public ClickAction getClickAction() {
                return clickaction;
            }

            public void setClickAction(ClickAction clickAction) {
                this.clickaction = clickAction;
            }
        }

        public class Button {
            @SerializedName("action_type")
            private int action_type;
            @SerializedName("name")
            private String name;

            public Button(int action_type, String name) {
                this.action_type = action_type;
                this.name = name;
            }

            public int getAction_type() {
                return action_type;
            }

            public void setAction_type(int action_type) {
                this.action_type = action_type;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }

        public static class ClickAction {
            @SerializedName("type")
            private int type;
            @SerializedName("intent")
            private String intent;

            public ClickAction(int type) {
                this.type = type;
            }

            public ClickAction(int type, String intent) {
                this.type = type;
                this.intent = intent;
            }

            public int getType() {
                return type;
            }

            public void setType(int type) {
                this.type = type;
            }

            public String getIntent() {
                return intent;
            }

            public void setIntent(String intent) {
                this.intent = intent;
            }
        }

    }
}
