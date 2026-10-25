package com.carTracker.carTracker.reminder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceReminderRepository extends JpaRepository<ServiceReminder, Long> {
    List<ServiceReminder> findByCarId(Long carId);
}
