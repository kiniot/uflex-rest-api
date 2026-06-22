package com.kiniot.uflex.api.device.domain.services;

import com.kiniot.uflex.api.device.domain.model.aggregates.Device;
import com.kiniot.uflex.api.device.domain.model.queries.*;

import java.util.List;
import java.util.Optional;

public interface DeviceQueryService {
    Optional<Device> handle(GetDeviceByIdQuery query);
    Optional<Device> handle(GetDeviceBySerialNumberQuery query);
    Optional<Device> handle(GetMyAssignedDeviceQuery query);
    List<Device> handle(GetAllDevicesByClinicIdQuery query);
    ClinicFleetMetrics handle(GetClinicFleetMetricsQuery query);
    List<Device> handle(GetStockDevicesQuery query);
    GlobalDeviceOverview handle(GetGlobalDeviceOverviewQuery query);
}
