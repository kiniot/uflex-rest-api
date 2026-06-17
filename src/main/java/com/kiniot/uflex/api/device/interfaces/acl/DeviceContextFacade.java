package com.kiniot.uflex.api.device.interfaces.acl;

public interface DeviceContextFacade {

    /**
     * Checks that the device exists, belongs to the given clinic and is currently
     * assigned to the given patient. The identifier may be the device UUID or its
     * serial number.
     */
    boolean isDeviceAssignedToPatient(String deviceIdentifier, String clinicId, String patientId);
}
