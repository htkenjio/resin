/*
 * Copyright (c) 1998-2011 Caucho Technology -- all rights reserved
 *
 * This file is part of Resin(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Resin Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
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
 * @author Alex Rojkov
 */

package com.caucho.server.admin;

import com.caucho.bam.RemoteConnectionFailedException;
import com.caucho.bam.ServiceUnavailableException;
import com.caucho.bam.actor.ActorSender;
import com.caucho.hmtp.HmtpClient;
import com.caucho.server.cluster.Server;
import com.caucho.util.L10N;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Deploy Client API
 */
public class HmuxClientFactory
{
  private String _address;
  private int _port;
  private String _userName;
  private String _password;

  public HmuxClientFactory(String address, int port,
                           String userName, String password)
  {
    _address = address;
    _port = port;
    _userName = userName;
    _password = password;
  }

  public ActorSender create()
  {
    try {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      
      Class<?> hmuxClientClass = Class.forName("com.caucho.server.cluster.HmuxClient",
                                               false,
                                               loader);
      Constructor<?> ctor = hmuxClientClass.getConstructor(String.class,
                                                           int.class,
                                                           String.class,
                                                           String.class);
      
      ActorSender sender
        = (ActorSender) ctor.newInstance(_address, _port,
                                         _userName, _password);
      
      return sender;
    } catch (InvocationTargetException e) {
      if (e.getTargetException() instanceof RuntimeException)
        throw (RuntimeException) e.getTargetException();
      else
        throw new RuntimeException(e.getTargetException());
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + _address + ":" + _port + "]";
  }
}

