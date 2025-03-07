package com.nortal.jroad.client.service.extractor;

import org.springframework.ws.client.core.WebServiceMessageExtractor;

/**
 * Custom extractor that has a setter for the original extractor used. Can be used to alter response messages that are
 * not WSDL compliant, so that they can be unmarshalled properly.
 *
 * @author Dmitri Danilkin
 */
public abstract class CustomExtractor<T> implements WebServiceMessageExtractor<T> {
  protected WebServiceMessageExtractor<T> extractor;

  public void setOriginalExtractor(WebServiceMessageExtractor<T> extractor) {
    this.extractor = extractor;
  }
}
