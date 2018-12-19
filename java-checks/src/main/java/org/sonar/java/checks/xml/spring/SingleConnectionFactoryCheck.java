/*
 * SonarQube Java
 * Copyright (C) 2012-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.java.checks.xml.spring;

import org.sonar.check.Rule;
import org.sonar.java.AnalysisException;
import org.sonar.java.checks.xml.AbstractXPathBasedCheck;
import org.sonar.java.xml.XmlCheckUtils;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

@Rule(key = "S3438")
public class SingleConnectionFactoryCheck extends AbstractXPathBasedCheck {

  private XPathExpression singleConnectionFactoryBeansExpression = getXPathExpression("beans/bean[@class='org.springframework.jms.connection.SingleConnectionFactory']");
  private XPathExpression reconnectOnExceptionPropertyValueExpression = getXPathExpression("property[@name='reconnectOnException' and value='true']");

  @Override
  protected void scanFile(XmlFile file) {
    try {
      NodeList nodeList = (NodeList)singleConnectionFactoryBeansExpression.evaluate(file.getNamespaceUnawareDocument(), XPathConstants.NODESET);
      for (int i = 0; i < nodeList.getLength(); i++) {
        if (!hasPropertyAsAttribute(nodeList.item(i)) && !hasPropertyAsChild(nodeList.item(i))) {
          reportIssue(nodeList.item(i), "Add a \"reconnectOnException\" property, set to \"true\"");
        }
      }
    } catch (XPathExpressionException e) {
      throw new AnalysisException("Unable to evaluate XPath expression", e);
    }
  }

  private static boolean hasPropertyAsAttribute(Node bean) {
    Node attribute = XmlCheckUtils.nodeAttribute(bean, "p:reconnectOnException");
    return attribute != null && "true".equals(attribute.getNodeValue());
  }

  private boolean hasPropertyAsChild(Node bean) {
    NodeList nodeList;
    try {
      nodeList = (NodeList)reconnectOnExceptionPropertyValueExpression.evaluate(bean, XPathConstants.NODESET);
    } catch (XPathExpressionException e) {
      throw new AnalysisException("Unable to evaluate XPath expression", e);
    }
    return (nodeList.getLength() != 0);
  }

}
