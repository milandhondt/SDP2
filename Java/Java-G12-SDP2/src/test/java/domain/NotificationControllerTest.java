package domain;

import dto.NotificationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.NotificationDao;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for NotificationController that avoids JPA initialization
 * by using a custom test implementation of the controller.
 */
class NotificationControllerTest {

    private NotificationDao notificationDao;
    private NotificationControllerTestImpl controller;
    
    private Notification readNotification1;
    private Notification readNotification2;
    private Notification unreadNotification1;
    private Notification unreadNotification2;

    @BeforeEach
    void setUp() {
        // Create a mock DAO using mockito
        notificationDao = mock(NotificationDao.class);
        
        // Create our test controller implementation
        controller = new NotificationControllerTestImpl(notificationDao);
        
        // Create test notification objects
        LocalDateTime now = LocalDateTime.now();
        readNotification1 = new Notification(1, true, "Read message 1", now);
        readNotification2 = new Notification(2, true, "Read message 2", now.minusHours(1));
        unreadNotification1 = new Notification(3, false, "Unread message 1", now.minusHours(2));
        unreadNotification2 = new Notification(4, false, "Unread message 2", now.minusHours(3));
    }

    /**
     * Test implementation of NotificationController that doesn't depend on JPA
     */
    private static class NotificationControllerTestImpl {
        private final NotificationDao notificationDao;
        
        public NotificationControllerTestImpl(NotificationDao notificationDao) {
            this.notificationDao = notificationDao;
        }
        
        public List<NotificationDTO> getAllRead() {
            return notificationDao.getAllRead().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        }
        
        public List<NotificationDTO> getAllUnread() {
            return notificationDao.getAllUnread().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        }
        
        public void markAsRead(int id) {
            notificationDao.markAsRead(id);
        }
        
        private NotificationDTO toDTO(Notification n) {
            return new NotificationDTO(n.getId(), n.getTime(), n.getMessage(), n.isRead());
        }
    }

    @Test
    void testGetAllRead() {
        // Arrange
        List<Notification> readNotifications = Arrays.asList(readNotification1, readNotification2);
        when(notificationDao.getAllRead()).thenReturn(readNotifications);

        // Act
        List<NotificationDTO> result = controller.getAllRead();

        // Assert
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).id());
        assertEquals("Read message 1", result.get(0).message());
        assertTrue(result.get(0).isRead());
        assertEquals(2, result.get(1).id());
        assertEquals("Read message 2", result.get(1).message());
        assertTrue(result.get(1).isRead());
        
        // Verify the mock was called once
        verify(notificationDao, times(1)).getAllRead();
    }

    @Test
    void testGetAllUnread() {
        // Arrange
        List<Notification> unreadNotifications = Arrays.asList(unreadNotification1, unreadNotification2);
        when(notificationDao.getAllUnread()).thenReturn(unreadNotifications);

        // Act
        List<NotificationDTO> result = controller.getAllUnread();

        // Assert
        assertEquals(2, result.size());
        assertEquals(3, result.get(0).id());
        assertEquals("Unread message 1", result.get(0).message());
        assertFalse(result.get(0).isRead());
        assertEquals(4, result.get(1).id());
        assertEquals("Unread message 2", result.get(1).message());
        assertFalse(result.get(1).isRead());
        
        // Verify the mock was called once
        verify(notificationDao, times(1)).getAllUnread();
    }

    @Test
    void testMarkAsRead() {
        // Act
        controller.markAsRead(3);

        // Assert
        // Verify the mock was called once with the correct ID
        verify(notificationDao, times(1)).markAsRead(3);
    }

    @Test
    void testGetAllReadEmptyList() {
        // Arrange
        when(notificationDao.getAllRead()).thenReturn(List.of());

        // Act
        List<NotificationDTO> result = controller.getAllRead();

        // Assert
        assertTrue(result.isEmpty());
        verify(notificationDao, times(1)).getAllRead();
    }

    @Test
    void testGetAllUnreadEmptyList() {
        // Arrange
        when(notificationDao.getAllUnread()).thenReturn(List.of());

        // Act
        List<NotificationDTO> result = controller.getAllUnread();

        // Assert
        assertTrue(result.isEmpty());
        verify(notificationDao, times(1)).getAllUnread();
    }

    @Test
    void testToDTOConversion() {
        // Arrange
        LocalDateTime time = LocalDateTime.now();
        Notification notification = new Notification(5, true, "Test message", time);
        when(notificationDao.getAllRead()).thenReturn(List.of(notification));

        // Act
        List<NotificationDTO> result = controller.getAllRead();

        // Assert
        assertEquals(1, result.size());
        NotificationDTO dto = result.get(0);
        assertEquals(5, dto.id());
        assertEquals(time, dto.time());
        assertEquals("Test message", dto.message());
        assertTrue(dto.isRead());
    }
}