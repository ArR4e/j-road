package com.nortal.jroad.client.kirst;

import com.nortal.jroad.client.exception.XRoadServiceConsumptionException;
import com.nortal.jroad.client.kirst.types.eu.x_road.kirst.KindlustusalusDocument.Kindlustusalus;
import com.nortal.jroad.client.kirst.types.eu.x_road.kirst.*;
import com.nortal.jroad.client.kirst.types.eu.x_road.kirst.KindlustusalusRequestType.KanneJada;
import com.nortal.jroad.client.kirst.types.eu.x_road.kirst.KindlustusalusResponseDocument.KindlustusalusResponse;
import com.nortal.jroad.client.kirst.types.eu.x_road.kirst.Kindlustused2Document.Kindlustused2;
import com.nortal.jroad.client.kirst.types.eu.x_road.kirst.Kindlustused2ResponseDocument.Kindlustused2Response;
import com.nortal.jroad.client.kirst.types.eu.x_road.kirst.KindlustusedResponseDocument.KindlustusedResponse;
import com.nortal.jroad.client.kirst.types.eu.x_road.kirst.KindlustusedResponseType.Kindlustused;
import com.nortal.jroad.client.service.XRoadDatabaseService;
import com.nortal.jroad.model.XRoadMessage;
import com.nortal.jroad.model.XmlBeansXRoadMessage;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

/**
 * @author Roman Tekhov
 */
@Service("kirstXRoadService")
public class KirstXRoadServiceImpl extends XRoadDatabaseService implements KirstXRoadService {

    public TvlLoetelu2ResponseDocument.TvlLoetelu2Response findTvlLoetelu2V1(Set<String> isikukoodid, Date alates,
                                                                             Date kuni)
            throws XRoadServiceConsumptionException {
        if (CollectionUtils.isEmpty(isikukoodid)) {
            throw new IllegalArgumentException("At least one 'isikukood' must be provided");
        }
        TvlLoetelu2RequestType requestBody = createTvlLoetelu2V1Request(isikukoodid, alates, kuni);
        TvlLoetelu2Document.TvlLoetelu2 request = TvlLoetelu2Document.TvlLoetelu2.Factory.newInstance();
        request.setRequest(requestBody);
        XRoadMessage<TvlLoetelu2ResponseDocument.TvlLoetelu2Response> response = send(
                new XmlBeansXRoadMessage<TvlLoetelu2Document.TvlLoetelu2>(request),
                "tvl_loetelu2",
                "v1");

        return response.getContent();

    }

    private TvlLoetelu2RequestType createTvlLoetelu2V1Request(Set<String> isikukoodid, Date alates, Date kuni) {
        TvlLoetelu2RequestType request = TvlLoetelu2RequestType.Factory.newInstance();
        TvlOtsingType isikud = request.addNewIsikud();
        int count = 0;
        TvlOtsingItemType[] items = new TvlOtsingItemType[isikukoodid.size()];
        for (String isikukood : isikukoodid) {
            TvlOtsingItemType item = TvlOtsingItemType.Factory.newInstance();
            item.setTvpAlates(toCalendar(alates));
            item.setTvpKuni(toCalendar(kuni));
            item.setIsikukood(isikukood);
            items[count++] = item;
        }
        isikud.setItemArray(items);
        return request;
    }

    private Calendar toCalendar(Date date) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public KindlustusedResponse findKindlustusV1(XTParingKindlustusedCallback callback)
            throws XRoadServiceConsumptionException {
        if (callback == null) {
            throw new IllegalArgumentException("Callback can not be null!");
        }
        Kindlustused paring = Kindlustused.Factory.newInstance();

        callback.populate(paring);
        XRoadMessage<KindlustusedResponse> response = send(new XmlBeansXRoadMessage<Kindlustused>(paring),
                "kindlustused",
                "v1");

        return response.getContent();
    }

    public Kindlustused2Response findKindlustus2(Kindlustused2 paring) throws XRoadServiceConsumptionException {
        XRoadMessage<Kindlustused2Response> response = send(new XmlBeansXRoadMessage<Kindlustused2>(paring),
                "kindlustused2",
                "v1");

        return response.getContent();
    }

    public KindlustusalusResponseDocument.KindlustusalusResponse findKindlustusalusV2(KindlustusalusKanneJadaCallback
                                                                                              callback)
            throws XRoadServiceConsumptionException {

        if (callback == null) {
            throw new IllegalArgumentException("Callback can not be null!");
        }
        KindlustusalusRequestType keha = Kindlustusalus.Factory.newInstance().addNewRequest();
        KanneJada kanneJada = keha.addNewKanneJada();

        callback.populate(kanneJada);

        XRoadMessage<KindlustusalusResponse> response = send(new XmlBeansXRoadMessage<KindlustusalusRequestType>(keha),
                "kindlustusalus",
                "v2");
        return response.getContent();
    }
}
