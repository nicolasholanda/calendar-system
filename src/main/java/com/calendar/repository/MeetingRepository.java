package com.calendar.repository;

import com.calendar.domain.Meeting;
import com.calendar.domain.MeetingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    List<Meeting> findByStatusOrderByStartTimeAsc(MeetingStatus status);

    @Query("""
        SELECT m FROM Meeting m JOIN m.participants p
        WHERE p.id = :userId AND m.status = 'SCHEDULED'
        ORDER BY m.startTime ASC
    """)
    List<Meeting> findScheduledMeetingsByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT m FROM Meeting m JOIN m.participants p
        WHERE p.id IN :userIds AND m.status = 'SCHEDULED'
        AND m.startTime >= :from AND m.endTime <= :to
    """)
    List<Meeting> findScheduledMeetingsForUsersInRange(
        @Param("userIds") List<Long> userIds,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );
}
