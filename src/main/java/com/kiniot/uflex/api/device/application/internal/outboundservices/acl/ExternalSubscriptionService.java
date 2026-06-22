package com.kiniot.uflex.api.device.application.internal.outboundservices.acl;

import com.kiniot.uflex.api.shared.domain.model.valueobjects.ClinicId;
import com.kiniot.uflex.api.subscription.interfaces.acl.ClinicEntitlement;
import com.kiniot.uflex.api.subscription.interfaces.acl.SubscriptionContextFacade;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("deviceExternalSubscriptionService")
public class ExternalSubscriptionService {

    private final SubscriptionContextFacade subscriptionContextFacade;

    public ExternalSubscriptionService(SubscriptionContextFacade subscriptionContextFacade) {
        this.subscriptionContextFacade = subscriptionContextFacade;
    }

    public int getRequestedTotalKits(ClinicId clinicId) {
        return subscriptionContextFacade.getRequestedTotalKitsByClinicId(clinicId);
    }

    public List<ClinicEntitlement> getCurrentEntitlements() {
        return subscriptionContextFacade.getCurrentEntitlements();
    }
}
