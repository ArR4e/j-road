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
                                                                             Date kuni,
                                                                             Set<String> tvPohjused)
            throws  XRoadServiceConsumptionException {
        if (CollectionUtils.isEmpty(isikukoodid)) {
            throw new IllegalArgumentException("At least one 'isikukood' must be provided");
        }
        TvlLoetelu2RequestType requestBody = createTvlLoetelu2V1Request(isikukoodid, alates, kuni
                , tvPohjused);
        TvlLoetelu2Document.TvlLoetelu2 request = TvlLoetelu2Document.TvlLoetelu2.Factory.newInstance();
        request.setRequest(requestBody);

        XRoadMessage<TvlLoetelu2ResponseDocument.TvlLoetelu2Response> response = send(
                new XmlBeansXRoadMessage<>(request),
                "tvl_loetelu2",
                "v1");
        return response.getContent();

    }

    private TvlLoetelu2RequestType createTvlLoetelu2V1Request(Set<String> isikukoodid,
                                                              Date alates, Date kuni,
                                                              Set<String> tvPohjused) {
        TvlLoetelu2RequestType request = TvlLoetelu2RequestType.Factory.newInstance();
        TvlOtsingType isikud = request.addNewIsikud();
        int count = 0;
        TvlOtsingItemType[] items = new TvlOtsingItemType[isikukoodid.size()];
        for (String isikukood : isikukoodid) {
            TvlOtsingItemType item = TvlOtsingItemType.Factory.newInstance();
            item.setTvpAlates(toCalendar(alates));
            item.setTvpKuni(toCalendar(kuni));
            item.setIsikukood(isikukood);
            item.setTvlPohjused(toTvlPohjused(tvPohjused));
            items[count++] = item;
        }
        isikud.setItemArray(items);
        return request;
    }

    private TvlPohjusedType toTvlPohjused(Set<String> tvPohjused) {
        TvlPohjusedType tvlPohjusedType = TvlPohjusedType.Factory.newInstance();
        if (tvPohjused == null) {
            return tvlPohjusedType;
        }
        for (String tvPohjus : tvPohjused) {
            if (tvPohjus == null) {
                continue;
            }
            tvlPohjusedType.addItem(TvPohjusType.Enum.forString(tvPohjus));
        }
        return tvlPohjusedType;
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

        XRoadMessage<KindlustusedResponse> response = send(new XmlBeansXRoadMessage<>(paring),
                "kindlustused",
                "v1");
        return response.getContent();
    }

    public Kindlustused2Response findKindlustus2(Kindlustused2 paring) throws XRoadServiceConsumptionException {
        XRoadMessage<Kindlustused2Response> response = send(new XmlBeansXRoadMessage<>(paring),
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
        Kindlustusalus insuranceBasis = Kindlustusalus.Factory.newInstance();
        KindlustusalusRequestType request = insuranceBasis.addNewRequest();
        KanneJada entries = request.addNewKanneJada();

        callback.populate(entries);

        XRoadMessage<KindlustusalusResponse> response = send(new XmlBeansXRoadMessage<>(insuranceBasis),
                "kindlustusalus",
                "v2");
        return response.getContent();
    }
}
