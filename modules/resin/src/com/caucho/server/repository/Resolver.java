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
 * @author Scott Ferguson
 */

package com.caucho.server.repository;

import com.caucho.config.ConfigException;
import com.caucho.loader.EnvironmentLocal;
import com.caucho.util.L10N;
import com.caucho.server.resin.Resin;
import com.caucho.vfs.Path;

import java.util.ArrayList;

/**
 * The module repository holds the module jars for osgi and ivy.
 */
public class Resolver
{
  private static final L10N L = new L10N(Resolver.class);

  private String _artifactPattern;
  private String _ivyPattern;

  public void setArtifactPattern(String pattern)
  {
    _artifactPattern = pattern;
  }

  public void setIvyPattern(String pattern)
  {
    _ivyPattern = pattern;
  }
}