package com.nortal.jroad.client.digilugu;

import com.nortal.jroad.client.digilugu.types.ee.x_road.digilugu.producer.Hl7Document;
import com.nortal.jroad.client.digilugu.types.ee.x_road.digilugu.producer.Hl7Paring;
import com.nortal.jroad.client.digilugu.types.ee.x_road.digilugu.producer.Hl7ResponseDocument.Hl7Response;
import com.nortal.jroad.client.digilugu.types.hl7_orgV3.RCMRIN000029UV01Document;
import com.nortal.jroad.client.digilugu.types.hl7_orgV3.RCMRIN000030UV01Document;
import com.nortal.jroad.client.digilugu.types.hl7_orgV3.RCMRIN000031UV01Document;
import com.nortal.jroad.client.digilugu.types.hl7_orgV3.RCMRIN000032UV01Document;
import com.nortal.jroad.client.exception.NonTechnicalFaultException;
import com.nortal.jroad.client.exception.XRoadServiceConsumptionException;
import com.nortal.jroad.client.service.callback.CustomCallback;
import com.nortal.jroad.client.service.extractor.CustomExtractor;
import com.nortal.jroad.client.service.XRoadDatabaseService;
import com.nortal.jroad.model.XRoadMessage;
import com.nortal.jroad.model.XmlBeansXRoadMessage;
import org.apache.xmlbeans.XmlException;
import org.springframework.stereotype.Service;

/**
 * <code>digilugu</code> database X-tee service implementation.
 *
 * @author Romet Piho
 */
@Service("digiluguXRoadService")
public class DigiluguXRoadServiceImpl extends XRoadDatabaseService implements DigiluguXRoadService {

    private static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final String DIGILUGU = "digilugu";
    private static final String HL7 = "hl7";
    private static final String V1 = "v1";

    @Override
    public RCMRIN000030UV01Document getHl7Document(RCMRIN000029UV01Document input, CustomCallback callback,
                                                   CustomExtractor customExtractor)
            throws XRoadServiceConsumptionException {
        Hl7Response vastus = send(input.xmlText(), callback, customExtractor);

        RCMRIN000030UV01Document document;
        try {
            document = RCMRIN000030UV01Document.Factory.parse(vastus.getResponse().getHl7OutputMessage());
        } catch (XmlException e) {
            throw new XRoadServiceConsumptionException(new NonTechnicalFaultException(null, "Error parsing xml"),
                    DIGILUGU,
                    HL7,
                    V1);
        }
        return document;

    }

    @Override
    public RCMRIN000032UV01Document getHl7TSK(RCMRIN000031UV01Document input, CustomCallback callback,
                                              CustomExtractor customExtractor) throws
            XRoadServiceConsumptionException {
        Hl7Response vastus = send(input.xmlText(), callback, customExtractor);

        RCMRIN000032UV01Document document;
        try {
            document = RCMRIN000032UV01Document.Factory.parse(vastus.getResponse().getHl7OutputMessage());
        } catch (XmlException e) {
            throw new XRoadServiceConsumptionException(new NonTechnicalFaultException(null, "Error parsing xml"),
                    DIGILUGU,
                    HL7,
                    V1);
        }
        return document;

    }

    private Hl7Response send(String input, CustomCallback callback, CustomExtractor customExtractor) throws
            XRoadServiceConsumptionException {
        Hl7Document.Hl7 hl7 = Hl7Document.Hl7.Factory.newInstance();
        Hl7Paring paring = hl7.addNewRequest();
        paring.setHl7InputMessage(XML + input);
        XRoadMessage<Hl7Response> response = send(new XmlBeansXRoadMessage<>(hl7), "hl7", "v1", callback,
                customExtractor);
        return response.getContent();
    }

}
