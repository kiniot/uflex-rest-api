package com.kiniot.uflex.api.device.domain.model.aggregates;

import com.kiniot.uflex.api.device.domain.exceptions.DeviceAssignmentNotAllowedException;
import com.kiniot.uflex.api.device.domain.model.events.DeviceAssignedToPatientEvent;
import com.kiniot.uflex.api.device.domain.model.events.DeviceBatteryLowEvent;
import com.kiniot.uflex.api.device.domain.model.events.DeviceRegisteredEvent;
import com.kiniot.uflex.api.device.domain.model.valueobjects.*;
import com.kiniot.uflex.api.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.shared.domain.model.valueobjects.PatientId;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
public class Device extends AuditableAbstractAggregateRoot<Device, DeviceId> {

    @EmbeddedId
    private DeviceId id;

    @Embedded
    @AttributeOverride(name = "serialNumber", column = @Column(name = "serial_number", nullable = false, unique = true, length = 64))
    private SerialNumber serialNumber;

    @Embedded
    @AttributeOverride(name = "macAddress", column = @Column(name = "mac_address", nullable = false, unique = true, length = 17))
    private MacAddress macAddress;

    @Embedded
    @AttributeOverride(name = "firmwareVersion", column = @Column(name = "firmware_version", length = 32))
    private FirmwareVersion firmwareVersion;

    @Embedded
    @AttributeOverride(name = "percentage", column = @Column(name = "battery_level", nullable = false))
    private BatteryLevel batteryLevel;

    @Embedded
    @AttributeOverride(name = "modelName", column = @Column(name = "model_name", length = 100))
    private DeviceModel model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CalibrationStatus calibrationStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeviceStatus status;

    @Column(nullable = true)
    private LocalDateTime lastSyncAt;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "clinic_id", columnDefinition = "UUID", nullable = false))
    private ClinicId clinicId;

    @Embedded
    @AttributeOverride(name = "patientId", column = @Column(name = "current_patient_id", columnDefinition = "UUID"))
    private PatientId currentPatientId;

    protected Device() {}

    public Device(SerialNumber serialNumber, MacAddress macAddress, FirmwareVersion firmwareVersion,
                  DeviceModel model, ClinicId clinicId) {
        this.id = new DeviceId();
        this.serialNumber = serialNumber;
        this.macAddress = macAddress;
        this.firmwareVersion = firmwareVersion;
        this.batteryLevel = new BatteryLevel(100);
        this.model = model;
        this.calibrationStatus = CalibrationStatus.VALID;
        this.status = DeviceStatus.AVAILABLE;
        this.lastSyncAt = null;
        this.clinicId = clinicId;
        this.currentPatientId = null;
        this.addDomainEvent(new DeviceRegisteredEvent(this, serialNumber.value(), clinicId));
    }

    public void assignToPatient(PatientId patientId) {
        if (this.status != DeviceStatus.AVAILABLE) {
            throw new DeviceAssignmentNotAllowedException("Device can only be assigned from AVAILABLE status. Current status: " + this.status);
        }
        if (this.calibrationStatus == CalibrationStatus.NEEDS_CALIBRATION) {
            throw new DeviceAssignmentNotAllowedException("Device cannot be assigned when calibration is needed");
        }
        this.currentPatientId = patientId;
        this.status = DeviceStatus.ASSIGNED;
        this.addDomainEvent(new DeviceAssignedToPatientEvent(this, this.serialNumber.value(), patientId));
    }

    public void returnFromPatient() {
        this.currentPatientId = null;
        this.status = DeviceStatus.AVAILABLE;
    }

    public void recordTelemetry(Integer batteryPercentage) {
        this.batteryLevel = new BatteryLevel(batteryPercentage);
        this.lastSyncAt = LocalDateTime.now();
        if (this.batteryLevel.isLow()) {
            this.addDomainEvent(new DeviceBatteryLowEvent(this, this.serialNumber.value(), batteryPercentage));
        }
    }

    public void markCalibrationAsInvalid() {
        this.currentPatientId = null;
        this.status = DeviceStatus.IN_MAINTENANCE;
        this.calibrationStatus = CalibrationStatus.NEEDS_CALIBRATION;
    }

    public void registerSuccessfulCalibration() {
        this.calibrationStatus = CalibrationStatus.VALID;
        this.status = DeviceStatus.AVAILABLE;
    }

    public void updateStatus(DeviceStatus newStatus) {
        this.status = newStatus;
    }

    public boolean isOffline() {
        return lastSyncAt == null || lastSyncAt.plusMinutes(15).isBefore(LocalDateTime.now());
    }

    @Override
    public DeviceId getId() {
        return id;
    }
}
