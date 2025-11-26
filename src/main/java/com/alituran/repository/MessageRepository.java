package com.alituran.repository;

import com.alituran.enums.MessageType;
import com.alituran.model.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Public chat mesajlarını getir
    List<Message> findByMessageTypeOrderByTimestampDesc(MessageType messageType, Pageable pageable);

    // İki kullanıcı arasındaki private mesajları getir
    @Query("SELECT m FROM Message m WHERE m.messageType = 'PRIVATE' " +
           "AND ((m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1)) " +
           "ORDER BY m.timestamp ASC")
    List<Message> findPrivateMessagesBetweenUsers(@Param("user1") String user1, @Param("user2") String user2);

    // Bir kullanıcının tüm private mesajlarını getir (gönderdiği veya aldığı)
    @Query("SELECT m FROM Message m WHERE m.messageType = 'PRIVATE' " +
           "AND (m.sender = :username OR m.receiver = :username) " +
           "ORDER BY m.timestamp DESC")
    List<Message> findAllPrivateMessagesForUser(@Param("username") String username, Pageable pageable);
}

