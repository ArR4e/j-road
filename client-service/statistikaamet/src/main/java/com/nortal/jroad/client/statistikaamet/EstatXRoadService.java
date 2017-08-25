package com.nortal.jroad.client.statistikaamet;

import com.nortal.jroad.client.exception.XTeeServiceConsumptionException;
import com.nortal.jroad.client.statistikaamet.types.ee.riik.xtee.estat.producers.producer.estat.ReturnDataResponse;
import com.nortal.jroad.client.statistikaamet.types.ee.riik.xtee.estat.producers.producer.estat.ReturnErrorResponse;
import com.nortal.jroad.client.statistikaamet.types.ee.riik.xtee.estat.producers.producer.estat.SubmitDataResponse;
import javax.activation.DataHandler;

/**
 * @author Kristjan Hendrik Küngas (KristjanHendrik.Kyngas@nortal.com)
 */
public interface EstatXRoadService {

  SubmitDataResponse submitData(String filename, DataHandler data, boolean validationOnly)
      throws XTeeServiceConsumptionException;

  ReturnDataResponse returnData(String submitId) throws XTeeServiceConsumptionException;

  ReturnErrorResponse returnError(String submitId) throws XTeeServiceConsumptionException;

}
