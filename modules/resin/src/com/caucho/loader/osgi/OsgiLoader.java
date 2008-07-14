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
 *   Free SoftwareFoundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */

package com.caucho.loader.osgi;

import com.caucho.config.ConfigException;
import com.caucho.loader.Loader;
import com.caucho.log.Log;
import com.caucho.vfs.Path;
import com.caucho.vfs.JarPath;
import com.caucho.server.util.*;

import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Loads resources.
 */
public class OsgiLoader extends Loader
{
  private static final Logger log
    = Logger.getLogger(OsgiLoader.class.getName());

  private OsgiManager _manager;

  private ArrayList<OsgiBundle> _bundleList = new ArrayList<OsgiBundle>();

  public OsgiLoader()
  {
    _manager = OsgiManager.create();
  }

  public void addPath(Path path)
  {
    OsgiBundle bundle = _manager.addPath(path);

    _bundleList.add(bundle);
  }
  
  /**
   * Adds the classpath of this loader.
   */
  @Override
  protected void buildClassPath(ArrayList<String> list)
  {
    for (OsgiBundle bundle : _bundleList) {
      JarPath jar = bundle.getJar();
      String pathName = jar.getContainer().getNativePath();

      if (! list.contains(pathName))
	list.add(pathName);
    }
  }

  @Override
  protected Class loadClass(String name)
  {
    for (OsgiBundle bundle : _bundleList) {
      Class cl = bundle.loadClass(name);

      if (cl != null)
	return cl;
    }
    
    return null;
  }
}