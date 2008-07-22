/*
 * Copyright (c) 1998-2008 Caucho Technology -- all rights reserved
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
 * @author Emil Ong
 */

package com.caucho.bayeux;

import java.io.*;
import java.util.*;

class JsonString implements JsonObject {
  private static final HashMap<String,JsonString> _strings = 
    new HashMap<String,JsonString>();

  private final String _value;

  public JsonString(String value)
  {
    _value = value;
  }

  public static JsonString valueOf(String value)
  {
    JsonString string = _strings.get(value);

    if (string == null) {
      string = new JsonString(value);
      _strings.put(value, string);
    }
    
    return string;
  }
  
  @Override
  public String toString()
  {
    return _value;
  }

  @Override
  public boolean equals(Object o)
  {
    if (o instanceof String)
      return _value.equals(o);

    else if (o instanceof JsonString)
      return _value.equals(o.toString());

    return false;
  }

  public void writeTo(PrintWriter out)
    throws IOException
  {
    out.print("\"" + _value + "\"");
  }
}