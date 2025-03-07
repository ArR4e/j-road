package com.nortal.jroad.client.rar;

import com.nortal.jroad.client.exception.XRoadServiceConsumptionException;
import com.nortal.jroad.client.rar.types.eu.x_road.rm_v6.RarVtaResponseType;

/**
*  RAR namespace in wsdl is rm-v6
*
*  @author Kauri Kägo <kauri.kago@nortal.com>
*/
public interface RarXRoadService {

    /**
     * <code>rar.rarVta.v1</code> X-tee service.
     */
    RarVtaResponseType rarVtaV1(String kood) throws XRoadServiceConsumptionException;
}
