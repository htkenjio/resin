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
 *   Free SoftwareFoundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */

package com.caucho.ejb.cfg;

import java.util.*;
import java.lang.reflect.*;

import com.caucho.util.*;
import com.caucho.vfs.*;
import com.caucho.ejb.*;
import com.caucho.config.ConfigException;

/**
 * <pre>
 * query-method ::= (method-name, method-params)
 * </pre>            
 */
public class QueryMethod {
  private static L10N L = new L10N(QueryMethod.class);

  private MethodSignature _signature;
  
  private String _methodName;
  
  private ArrayList _methodParams;

  public QueryMethod()
  {
    _signature = new MethodSignature();
  }

  public void setValue(String id)
    throws ConfigException
  {
    _signature.setName(id);
  }

  public void setId(String id)
    throws ConfigException
  {
    _signature.setName(id);
  }

  public void setMethodName(String methodName)
    throws ConfigException
  {
    _signature.setName(methodName);
  }

  public void setMethodParams(MethodParams methodParams)
  {
  }

  public MethodSignature getSignature()
  {
    return _signature;
  }

  public static class MethodParams {
    private ArrayList _methodParams = new ArrayList();
    
    public void addMethodParam(String param)
      throws ConfigException
    {
      if (param.equals(""))
        throw new ConfigException(L.l("method-param must not be empty."));
    }
  }
}
