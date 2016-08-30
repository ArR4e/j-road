/**
 * Copyright 2015 Nortal Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 **/

package com.nortal.jroad.client.service.consumer.v2;

import com.nortal.jroad.client.exception.NonTechnicalFaultException;
import com.nortal.jroad.client.exception.XTeeServiceConsumptionException;
import com.nortal.jroad.client.service.callback.CustomCallback;
import com.nortal.jroad.client.service.callback.StandardXTeeConsumerCallback;
import com.nortal.jroad.client.service.callback.XTeeMessageCallback;
import com.nortal.jroad.client.service.callback.v2.XteeMessageCallbackNamespaceStrategy;
import com.nortal.jroad.client.service.configuration.BaseXRoadServiceConfiguration;
import com.nortal.jroad.client.service.consumer.XRoadConsumer;
import com.nortal.jroad.client.service.extractor.CustomExtractor;
import com.nortal.jroad.client.service.extractor.StandardXTeeConsumerMessageExtractor;
import com.nortal.jroad.client.util.WSConsumptionLoggingInterceptor;
import com.nortal.jroad.client.util.XmlBeansUtil;
import com.nortal.jroad.model.XTeeAttachment;
import com.nortal.jroad.model.XTeeMessage;
import com.nortal.jroad.model.XmlBeansXTeeMetadata;
import com.nortal.jroad.util.AttachmentUtil;
import java.lang.reflect.Method;
import java.util.Map;
import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.springframework.oxm.xmlbeans.XmlBeansMarshaller;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceMessageExtractor;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.client.SoapFaultClientException;

/**
 * Standard {@link XRoadConsumer} implementation.
 *
 * @author Dmitri Danilkin
 * @author Roman Tekhov
 * @author Rando Mihkelsaar
 */
@Service("xTeeConsumer")
public class StandardXTeeConsumer extends WebServiceGatewaySupport implements XRoadConsumer {
  private Map<String, XmlBeansXTeeMetadata> metadata;
  public static final String ROOT_NS = "ns5";

  @Override
  protected void initGateway() throws Exception {
    XmlBeansMarshaller marshaller = new XmlBeansMarshaller();
    metadata = XmlBeansUtil.loadMetadata();

    setMarshaller(marshaller);
    setUnmarshaller(marshaller);

    ClientInterceptor[] interceptors = new ClientInterceptor[] { new WSConsumptionLoggingInterceptor() };
    setInterceptors(interceptors);

    getWebServiceTemplate().setCheckConnectionForFault(false);
  }

  public <I, O> XTeeMessage<O> sendRequest(XTeeMessage<I> input, BaseXRoadServiceConfiguration xTeeServiceConfiguration)
      throws XTeeServiceConsumptionException {
    return sendRealRequest(input, xTeeServiceConfiguration, null, null);
  }

  public <I, O> XTeeMessage<O> sendRequest(XTeeMessage<I> input,
                                           BaseXRoadServiceConfiguration xTeeServiceConfiguration,
                                           CustomCallback callback,
                                           CustomExtractor extractor) throws XTeeServiceConsumptionException {
    return sendRealRequest(input, xTeeServiceConfiguration, callback, extractor);
  }

  @SuppressWarnings("unchecked")
  private <I, O> XTeeMessage<O> sendRealRequest(XTeeMessage<I> input,
                                                BaseXRoadServiceConfiguration xteeServiceConfiguration,
                                                CustomCallback callback,
                                                CustomExtractor extractor) throws XTeeServiceConsumptionException {

	if(isKehaElementNeeded()) {
	    XmlOptions options =
	        ((XmlBeansMarshaller) getMarshaller()).getXmlOptions() == null
	                                                                      ? new XmlOptions()
	                                                                      : ((XmlBeansMarshaller) getMarshaller()).getXmlOptions();
	    options.setSaveSyntheticDocumentElement(new QName("keha"));
	    ((XmlBeansMarshaller) getMarshaller()).setXmlOptions(options);
	}

    try {
      // Add any swaref attachments...
      // First find all Objects.
      for (XmlObject attachmentObj : XmlBeansUtil.getAllObjects((XmlObject) input.getContent())) {

        // Introspect all methods, and find the ones that were generated during instrumentation
        for (Method method : XmlBeansUtil.getSwaRefGetters(attachmentObj)) {
          // Get the datahandler for the attachment
          DataHandler handler = (DataHandler) method.invoke(attachmentObj);

          if (handler != null) {
            String field = XmlBeansUtil.getFieldName(method);
            // Check whether the user has set a custom CID, if not, generate a random one and set it
            String cid = XmlBeansUtil.getCid(attachmentObj, field);
            if (cid == null) {
              cid = AttachmentUtil.getUniqueCid();
            } else {
              cid = cid.startsWith("cid:") ? cid.substring(4) : cid;
            }
            XmlBeansUtil.setCid(attachmentObj, field, "cid:" + cid);

            // Add a new attachment to the list
            input.getAttachments().add(new XTeeAttachment(cid, handler));
          }
        }
      }

      XmlBeansXTeeMetadata curdata =
          metadata.get(xteeServiceConfiguration.getWsdlDatabase().toLowerCase()
              + xteeServiceConfiguration.getMethod().toLowerCase());

      if (curdata == null) {
        throw new IllegalStateException(String.format("Could not find metadata for %s.%s! Most likely the method name has been specified incorrectly.",
                                                      xteeServiceConfiguration.getWsdlDatabase().toLowerCase(),
                                                      xteeServiceConfiguration.getMethod().toLowerCase()));
      }

      StandardXTeeConsumerCallback originalCallback = getNewConsumerCallback(input, xteeServiceConfiguration, curdata);
      if (callback != null) {
        callback.modifyConsumerCallback(originalCallback);
      }

      WebServiceMessageExtractor originalExtractor = new StandardXTeeConsumerMessageExtractor(curdata);

      if (callback != null) {
        callback.setOriginalCallback(originalCallback);
      }

      WebServiceMessageCallback finalCallback = callback == null ? originalCallback : callback;

      if (extractor != null) {
        extractor.setOriginalExtractor(originalExtractor);
      }

      WebServiceMessageExtractor finalExtractor = extractor == null ? originalExtractor : extractor;

      return (XTeeMessage<O>) getWebServiceTemplate().sendAndReceive(xteeServiceConfiguration.getSecurityServer(),
                                                                     finalCallback,
                                                                     finalExtractor);
    } catch (Exception e) {
      XTeeServiceConsumptionException consumptionException = resolveException(e, xteeServiceConfiguration);

      if (consumptionException != null) {
        throw consumptionException;
      }
      throw new NestableRuntimeException(e);
    }

  }


  protected boolean isKehaElementNeeded() {
	  return true;
  }

  protected <I> StandardXTeeConsumerCallback getNewConsumerCallback(XTeeMessage<I> input,
                                                                    BaseXRoadServiceConfiguration xteeServiceConfiguration,
                                                                    XmlBeansXTeeMetadata curdata) {
    return new StandardXTeeConsumerCallback(input.getContent(),
                                            getNewMessageCallback(input, xteeServiceConfiguration),
                                            getMarshaller(),
                                            curdata,
                                            getNamespace(xteeServiceConfiguration),
                                            isEncodingStyleNeeded());
  }

  protected String getNamespace(BaseXRoadServiceConfiguration xteeServiceConfiguration) {
    return String.format("http://producers.%s.xtee.riik.ee/producer/%s",
                         xteeServiceConfiguration.getDatabase(),
                         xteeServiceConfiguration.getDatabase());
  }

  protected boolean isEncodingStyleNeeded() {
    return true;
  }

  protected <I> XTeeMessageCallback getNewMessageCallback(XTeeMessage<I> input,
       BaseXRoadServiceConfiguration xteeServiceConfiguration) {
	  return new XTeeMessageCallback(xteeServiceConfiguration, input.getAttachments(),
			  new XteeMessageCallbackNamespaceStrategy());
  }

  private XTeeServiceConsumptionException resolveException(Exception e,
                                                           BaseXRoadServiceConfiguration xteeServiceConfiguration) {

    WebServiceIOException ioException = null;
    SoapFaultClientException faultException = null;

    if (e instanceof WebServiceIOException) {
      ioException = (WebServiceIOException) e;
    } else if (e instanceof SoapFaultClientException) {
      faultException = (SoapFaultClientException) e;
    }

    if (ioException != null || faultException != null) {
      String database = xteeServiceConfiguration.getDatabase();
      String method = xteeServiceConfiguration.getMethod();
      String version = xteeServiceConfiguration.getVersion();

      if (ioException != null) {
        if (ioException.getCause() instanceof NonTechnicalFaultException) {
          return new XTeeServiceConsumptionException((NonTechnicalFaultException) ioException.getCause(),
                                                     database,
                                                     method,
                                                     version);
        }

        return new XTeeServiceConsumptionException(ioException, database, method, version);
      }

      if (faultException != null) {
        return new XTeeServiceConsumptionException(faultException, database, method, version);
      }
    }

    return null;
  }
}
