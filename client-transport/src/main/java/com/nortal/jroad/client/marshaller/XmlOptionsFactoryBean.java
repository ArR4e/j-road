
/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nortal.jroad.client.marshaller;

import java.util.Map;

import org.apache.xmlbeans.XmlOptions;

import org.springframework.beans.factory.FactoryBean;

/**
 * {@link FactoryBean} that configures an XMLBeans {@code XmlOptions} object
 * and provides it as a bean reference.
 *
 * <p>Typical usage will be to set XMLBeans options on this bean, and refer to it
 * in the {@link XmlBeansMarshaller}.
 * <br/>
 * Backport from org.springframework:spring-oxm:4.0.5
 * @author Arjen Poutsma (original author)
 * @author Juergen Hoeller (original author)
 * @see XmlOptions
 * @see #setOptions(java.util.Map)
 * @see XmlBeansMarshaller#setXmlOptions(XmlOptions)
 */
public class XmlOptionsFactoryBean implements FactoryBean<XmlOptions> {

  private XmlOptions xmlOptions = new XmlOptions();


  /**
   * Set options on the underlying {@code XmlOptions} object.
   * <p>The keys of the supplied map should be one of the String constants
   * defined in {@code XmlOptions}, the values vary per option.
   * @see XmlOptions#put(Object, Object)
   * @see XmlOptions#SAVE_PRETTY_PRINT
   * @see XmlOptions#LOAD_STRIP_COMMENTS
   */
  public void setOptions(Map<String, ?> optionsMap) {
    this.xmlOptions = new XmlOptions();
    if (optionsMap != null) {
      for (Map.Entry<String, ?> option : optionsMap.entrySet()) {
        this.xmlOptions.put(option.getKey(), option.getValue());
      }
    }
  }


  @Override
  public XmlOptions getObject() {
    return this.xmlOptions;
  }

  @Override
  public Class<? extends XmlOptions> getObjectType() {
    return XmlOptions.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

}
