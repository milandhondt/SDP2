package domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import domain.Notification;

class NotificationTest {

    @Test
    void constructor_setsFieldsCorrectly() {
        LocalDateTime time = LocalDateTime.now();
        Notification notification = new Notification(true, "Test Message", time);

        assertTrue(notification.isRead());
        assertEquals("Test Message", notification.getMessage());
        assertEquals(time, notification.getTime());
    }

    @Test
    void settersAndGetters_workAsExpected() {
        Notification notification = new Notification();
        notification.setId(1);
        notification.setRead(false);
        notification.setMessage("Hello World");
        LocalDateTime now = LocalDateTime.now();
        notification.setTime(now);

        assertEquals(1, notification.getId());
        assertFalse(notification.isRead());
        assertEquals("Hello World", notification.getMessage());
        assertEquals(now, notification.getTime());
    }
}
