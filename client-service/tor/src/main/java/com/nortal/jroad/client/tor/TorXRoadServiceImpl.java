package com.nortal.jroad.client.tor;

import com.nortal.jroad.client.exception.XRoadServiceConsumptionException;
import com.nortal.jroad.client.tor.database.TorXRoadDatabase;
import com.nortal.jroad.client.tor.types.eu.x_road.emta_v6.TORIKDocument;
import com.nortal.jroad.client.tor.types.eu.x_road.emta_v6.TORIKResponseDocument;
import com.nortal.jroad.client.tor.types.eu.x_road.emta_v6.TorikRequestType;
import java.util.Calendar;
import java.util.Date;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * <code>TOR</code> database X-tee service implementation<br>
 *
 * @author Kauri Kägo
 */
@Service("torXRoadServiceImpl")
public class TorXRoadServiceImpl implements TorXRoadService {

  @Resource
  private TorXRoadDatabase torXRoadDatabase;

  @Override
  public TORIKResponseDocument.TORIKResponse findTorik(String paringuLiik,
                                                       Date tootAlgusKp,
                                                       Date tootLoppKp,
                                                       Date muutAlgKp,
                                                       Date muutLoppKp,
                                                       String isikukood) throws XRoadServiceConsumptionException {

    TORIKDocument.TORIK torik = getTorikRequest(paringuLiik, tootAlgusKp, tootLoppKp, muutAlgKp, muutLoppKp, isikukood);

    return torXRoadDatabase.torikV2(torik);

  }

  @Override
  public TORIKDocument.TORIK getTorikRequest(String paringuLiik,
                                             Date tootAlgusKp,
                                             Date tootLoppKp,
                                             Date muutAlgKp,
                                             Date muutLoppKp,
                                             String isikukood) {
    TORIKDocument.TORIK input = TORIKDocument.TORIK.Factory.newInstance();
    TorikRequestType request = input.addNewRequest();

    request.setParinguLiik(TorikRequestType.ParinguLiik.Enum.forString(paringuLiik));
    if (tootAlgusKp != null) {
      request.setTootAlgus(getCalendar(tootAlgusKp));
    }
    if (tootLoppKp != null) {
      request.setTootLopp(getCalendar(tootLoppKp));
    }

    if (TorikRequestType.ParinguLiik.Enum.forString(paringuLiik).equals(TorikRequestType.ParinguLiik.PM)) {
      request.setMuutAlg(getCalendar(muutAlgKp));
      request.setMuutLopp(getCalendar(muutLoppKp));
    }
    request.setIsikukoodid(isikukood);
    return input;
  }

  private Calendar getCalendar(Date kuup) {
    if (kuup == null) {
      return null;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(kuup);
    return cal;
  }

}
