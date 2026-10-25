package com.carTracker.carTracker.maintenanceRecord;

import com.carTracker.carTracker.mileage.MileageCheck;
import com.carTracker.carTracker.mileage.MileageStats;

import java.util.List;

public interface MaintenanceRecordInterface {
    MaintenanceRecord getMaintenanceRecord(Long id);
    MileageCheck addMaintenanceRecord(Long carId, MaintenanceRecord record);
    void deleteMaintenanceRecord(Long id);
    List<MaintenanceRecord> getAllMaintenanceRecordsToCar(Long carId);
    MileageStats getMileageStats(Long carId);
}
