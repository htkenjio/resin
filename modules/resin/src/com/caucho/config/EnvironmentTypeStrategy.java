/*
 * Copyright (c) 1998-2006 Caucho Technology -- all rights reserved
 *
 * This file is part of Resin(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Resin Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Resin Open Source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Resin Open Source; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */

package com.caucho.config;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.jsp.el.VariableResolver;
import com.caucho.util.L10N;
import com.caucho.util.CharBuffer;
import com.caucho.util.NotImplementedException;
import com.caucho.xml.QName;
import com.caucho.loader.EnvironmentBean;
import com.caucho.loader.Environment;
import com.caucho.make.Dependency;
import org.w3c.dom.Node;

public class EnvironmentTypeStrategy extends BeanTypeStrategy {
  EnvironmentTypeStrategy(Class type)
  {
    super(type);
  }

  public void configureBean(NodeBuilder builder, Object bean, Node node)
         throws Exception
  {
    Thread thread = Thread.currentThread();
    ClassLoader oldLoader = thread.getContextClassLoader();

    try {
      EnvironmentBean envBean = (EnvironmentBean) bean;
      ClassLoader loader = envBean.getClassLoader();

      thread.setContextClassLoader(loader);
      // XXX: builder.setClassLoader?

      addDependencies(builder, node);

      super.configureBean(builder, bean, node);
    } finally {
      thread.setContextClassLoader(oldLoader);
    }
  }

  public void configureAttribute(NodeBuilder builder, Object bean, Node attr)
         throws Exception
  {
    Thread thread = Thread.currentThread();
    ClassLoader oldLoader = thread.getContextClassLoader();

    try {
      EnvironmentBean envBean = (EnvironmentBean) bean;
      thread.setContextClassLoader(envBean.getClassLoader());
      // XXX: builder.setClassLoader?

      addDependencies(builder, attr);

      super.configureAttribute(builder, bean, attr);
    } finally {
      thread.setContextClassLoader(oldLoader);
    }
  }

  /**
   * Adds dependencies from the DOM to the environment.
   *
   * @param builder the builder context
   * @param node the configuration node
   */
  private void addDependencies(NodeBuilder builder, Node node)
  {
    ArrayList<Dependency> dependencyList = builder.getDependencyList(node);

    if (dependencyList != null) {
      for (Dependency dependency : dependencyList) {
        Environment.addDependency(dependency);
      }
    }
  }

  /**
   * Returns the attribute builder.
   */
  public AttributeStrategy getEnvironmentAttribute(QName name)
    throws Exception
  {
    return TypeStrategyFactory.getEnvironmentAttribute(getType(), name);
  }
}
